package ancestris.modules.releve.table;

import ancestris.modules.releve.merge.SelectionManager;
import ancestris.modules.releve.model.FieldDate;
import ancestris.modules.releve.model.FieldSex;
import ancestris.modules.releve.model.FieldSimpleValue;
import ancestris.modules.releve.model.GedcomLink;
import ancestris.modules.releve.model.Record;
import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.util.swing.ImageIcon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.openide.util.NbPreferences;

/**
 *
 * @author Michel
 */
public class ReleveTable extends JTable {

    private ReleveTableListener releveTableListener = null;

    public ReleveTable () {
        super();

        // pas de redimensionnement automatique, voir loadColumnLayout()
        setAutoResizeMode(AUTO_RESIZE_OFF );
        setRowSelectionAllowed(true);
        
        // j'ajoute le renderer pour l'affichage des dates et le choix de la font
        setDefaultRenderer(Object.class, new Renderer());
        setDefaultRenderer(Integer.class, new Renderer());
        
        Renderer r = new Renderer();
        r.setFont(getFont());

        // Ne pas utiliser setSelectionMode car si setSelectionMode est utilisé en meme temps
        // que setDragEnabled(true), alors les fleches UP etDOWN du clavier ne fonctionnent plus.
        //setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Ne pas utiliser setFocusable, sinon les deplacements avec les
        // fleches UP etDOWN du clavier ne fonctionnent plus
        //setFocusable(true);


        // je configure la table pour qu'elle s'étende sur toute la hauteur de son
        // container afin de pouvoir faire des DnD même si elle ne contient
        // aucun element.
        setFillsViewportHeight(true);

        getInputMap(JComponent.WHEN_FOCUSED).remove( KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        getInputMap(JComponent.WHEN_FOCUSED).put( KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK), "shortCutKeyDown");
        getActionMap().put("shortCutKeyDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                releveTableListener.swapRecordNext();
            }
        });

        getInputMap(JComponent.WHEN_FOCUSED).remove( KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        getInputMap(JComponent.WHEN_FOCUSED).put( KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK), "shortCutKeyUp");
        getActionMap().put("shortCutKeyUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                releveTableListener.swapRecordPrevious();
            }
        });
        
        // je gère le tri sur plusieurs colonnes
        this.getTableHeader().addMouseListener(new HeaderMouseHandler());
        this.getTableHeader().setDefaultRenderer(new SortableHeaderRenderer(this.getTableHeader().getDefaultRenderer()));
//      
        // je branche le clic du bouton gauche de la souris sur les titres des colonnes
        // pour garder visible la ligne sélectionnée quand on change l'ordre de tri
        getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if ( getSelectedRowCount() > 0 ) {
                        // je rends visible la premiere ligne selectionnée et de la colonne triée
                        int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                        Rectangle cellRect = getCellRect(getSelectedRows()[0], viewColumn, false);
                        if (cellRect != null) {
                            scrollRectToVisible(cellRect);
                        }
                    }
                }
            }
        });
    }

   /**
     * Initialise le modèle de données de la JTable
     * @param model
     */
   public void setModel(TableModelRecordAbstract tableModel) {

        TableRowSorter<TableModelRecordAbstract> tableRowSorter = new TableRowSorter<TableModelRecordAbstract>(tableModel);
        tableRowSorter.setRowFilter(tableModel.getRecordFilter());
        tableRowSorter.setSortsOnUpdates(true);
        setRowSorter(tableRowSorter);
        super.setModel(tableModel);
        // j'applique le nouveau layout
        loadColumnLayout();
        
        // j'ajoute un listener pour intercepter les clics sur la première colonne ( ID du record) 
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                JTable target = (JTable) e.getSource();
                int row = target.rowAtPoint(e.getPoint());
                if (row == -1) {
                    return;
                }
                int column = target.columnAtPoint(e.getPoint());

                if (column == 0) {
                    GedcomLink gedcomLink = (GedcomLink) getModel().getValueAt(convertRowIndexToModel(row), -1);
                    if (gedcomLink != null) {
                        Property property = gedcomLink.getProperty();
                        Entity entity = gedcomLink.getEntity();
                        if (entity != null) {
                            if (e.getClickCount() == 2) {
                                SelectionManager.setRootEntity(property);
                            } else {
                                SelectionManager.showEntity(property);
                            }
                        }
                    }
                }
            }        
        });
    }

    public void componentClosed() {
        saveColumnLayout();
    }

    public void dropRecord(Record record) {
        ((TableModelRecordAbstract)getModel()).addRecord(record);
    }

    /**
     * affiche un relevé
     * si le relevé est nul, nettoie l'affichage
     * @param record
     */
    public void selectRecord(int recordIndex) {
        if (recordIndex != -1) {
            int currentRow = convertRowIndexToView(recordIndex);
            if (currentRow != -1) {
                // je rafraichis le tri de la table en appelant repaint()
                repaint();
                currentRow = convertRowIndexToView(recordIndex);
                setRowSelectionInterval(currentRow, currentRow);
                // je scrolle pour voir la ligne
                Rectangle cellRect = getCellRect(getSelectedRow(), getSelectedColumn(), false);
                if (cellRect != null) {
                    scrollRectToVisible(cellRect);
                }
            }
        }
    }
    
    public int getSelectedRecordIndex() {
        int selectedRow = getSelectedRow();
        if( selectedRow != -1 ) {
            return convertRowIndexToModel(selectedRow);
        } else {
            return -1;
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    // Manager VerificationListener
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @param validationListeners the validationListeners to set
     */
    public void setTableSelectionListener(ReleveTableListener listener) {
        releveTableListener = listener;
    }

    /**
     *
     * @param recordIndex index du relevé à selectionner 
     * @param isNewRecord true si c'est un nouveau releve, false sinon
     */
    public void fireTableSelectionListener(int recordIndex, boolean isNewRecord) {
        if ( releveTableListener != null) {
            releveTableListener.tableRecordSelected(recordIndex, isNewRecord);
        }
    }

    /**
     * Propage la selection d'un ligne vers les listeners
     * @param rowIndex
     * @param columnIndex
     * @param toggle
     * @param extend
     */
    @Override
    public void changeSelection(int rowIndex, int columnIndex, final boolean toggle, final boolean extend) {
        if (releveTableListener.verifyCurrentRecord() == true) {
            // j'execute le traitement par defaut
            super.changeSelection(rowIndex, columnIndex, toggle, extend);
            if (!toggle && !extend) {
                final int recordIndex = convertRowIndexToModel(rowIndex);

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        // j'informe les listeners de la nouvelle selection
                        fireTableSelectionListener(recordIndex, false);
                    }
                });
            }
        }
    }
    
    /**
     * Set column layout from bundle configuration
     */
    public void loadColumnLayout() {
        // je recupere la largeur des colonnes de la session precedente
        String columnLayout = NbPreferences.forModule(ReleveTable.class).get(
                ((TableModelRecordAbstract) getModel()).getModelName() + "ColumnLayout",
                "");

        TableColumnModel columns = this.getColumnModel();

        if (columnLayout.isEmpty()) {
            columnLayout += columns.getColumnCount();
            for (int i = 0; i < columns.getColumnCount(); i++) {
                columnLayout += ",70";
            }
        }

        StringTokenizer tokens = new StringTokenizer(columnLayout, ",");
        int n = Integer.parseInt(tokens.nextToken());
        for (int i = 0; i < n && i < columns.getColumnCount(); i++) {
            TableColumn col = columns.getColumn(i);
            int w = Integer.parseInt(tokens.nextToken());
            col.setWidth(w);
            col.setPreferredWidth(w);
        }
    }

    /**
     * Return column layout - a string that can be used to return column widths and sorting
     */
    public void saveColumnLayout() {
        TableColumnModel columns = this.getColumnModel();
        String columnLayout = "" + columns.getColumnCount();

        for (int c = 0; c < columns.getColumnCount(); c++) {
            columnLayout += "," + columns.getColumn(c).getWidth();
        }

        NbPreferences.forModule(ReleveTable.class).put(((TableModelRecordAbstract) getModel()).getModelName() + "ColumnLayout", columnLayout);
    }

    /**
     * Renderer pour afficher les dates avec le format jj/mm/aaaa
     */
    private class Renderer extends DefaultTreeCellRenderer implements TableCellRenderer {

        /**
         * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)
         */
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focs, int row, int col) {

            setFont(table.getFont());

            if ( value != null) {
                if (value instanceof FieldDate) {
                    setText(((FieldDate) value).getDisplayValue());
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (value instanceof FieldSex) {
                    setText(((FieldSex) value).toString());
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else if (value instanceof FieldSimpleValue) {
                    setText(((FieldSimpleValue) value).toString());
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else {
                    setText(value.toString());
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
            } else {
                setText("");
                setHorizontalAlignment(SwingConstants.LEFT);
            }

            // je choisis les couleurs
            boolean gedcomLinked = false;
            
            if ( col == 0) {
                GedcomLink gedcomLink = (GedcomLink) table.getModel().getValueAt(table.convertRowIndexToModel(row), -1);
                if (gedcomLink != null ) {
                    gedcomLinked = true;
                }
            }
            
            if (selected) {
                // noir sur fond magenta
                if (gedcomLinked) {
                    // noir sur fond magenta
                    setForeground(table.getSelectionForeground());
                    setBackground(Color.MAGENTA);
                    setOpaque(true);
                } else {
                    setBackground(table.getSelectionBackground());
                    setForeground(table.getSelectionForeground());
                    setOpaque(true);
                }
            } else {

                if (gedcomLinked) {
                    // noir sur fond rose
                    setForeground(table.getForeground());
                    setBackground(Color.PINK);
                    setOpaque(true);
                } else {
                    setForeground(table.getForeground());
                    setOpaque(false);
                }

            }
            // ready
            return this;
        }
    }    
    
    /////////////////////////////////////////////////////////////////////////
    // Multi columns sorting
    /////////////////////////////////////////////////////////////////////////

    private final List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
    
    public void setSortingStatus(int column, SortOrder sortOrder) {

        RowSorter.SortKey directive = getSortKey(column);
        if (directive != null) {
            sortKeys.remove(directive);
        }
        if (sortOrder != SortOrder.UNSORTED) {
            sortKeys.add(new RowSorter.SortKey(column, sortOrder));
        }
        sortingStatusChanged();
    }

    public void cancelSorting() {
        sortKeys.clear();
        sortingStatusChanged();
    }

    /*
     * applies new sortKeysList
     */
    private void sortingStatusChanged() {
        this.getRowSorter().setSortKeys( sortKeys );
        // je supprime cette instruction car elle semble inutile et fait perdre la selection courante
        //todoModel.fireTableDataChanged();
        if (tableHeader != null) {
            tableHeader.repaint();
        }
    }

    private RowSorter.SortKey getSortKey(int column) {
        for (int i = 0; i < sortKeys.size(); i++) {
            RowSorter.SortKey sortKey = sortKeys.get(i);
            if (sortKey.getColumn() == column) {
                return sortKey;
            }
        }
        return null;
    }

    protected Icon getHeaderRendererIcon(int column, int size) {
        RowSorter.SortKey sortKey = getSortKey(column);
        if (sortKey == null) {
            return null;
        }
        return new Arrow(sortKey.getSortOrder() == SortOrder.DESCENDING, size, sortKeys.indexOf(sortKey));
    }

    private class HeaderMouseHandler extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            JTableHeader h = (JTableHeader) e.getSource();
            TableColumnModel columnModel = h.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            if (viewColumn < 0) {
                return;
            }
            int column = columnModel.getColumn(viewColumn).getModelIndex();
            if (column < 0) {
                return;
            }
            RowSorter.SortKey sortKey = getSortKey(column);
            SortOrder sortOrder;
            if (!e.isControlDown()) {
                sortKeys.clear();
            }

            if ( sortKey != null ) {
                sortOrder = sortKey.getSortOrder();

                if (e.isShiftDown()) {
                    if (null == sortOrder) {
                        sortOrder = SortOrder.UNSORTED;
                    } else // for shift we cycle forward {NOT_SORTED, ASCENDING, DESCENDING}
                    switch (sortOrder) {
                        case UNSORTED:
                            sortOrder = SortOrder.ASCENDING;
                            break;
                        case ASCENDING:
                            sortOrder = SortOrder.DESCENDING;
                            break;
                        default:
                            sortOrder = SortOrder.UNSORTED;
                            break;
                    }
                } else {
                    // inverse le sortOrder
                    sortOrder = (sortOrder == SortOrder.DESCENDING) ? SortOrder.ASCENDING : SortOrder.DESCENDING;
                }

            } else {
                sortOrder = SortOrder.ASCENDING;
            }
            setSortingStatus(column, sortOrder);
        }
    }
    
    private class SortableHeaderRenderer implements TableCellRenderer {
        private final TableCellRenderer tableCellRenderer;

        public SortableHeaderRenderer(TableCellRenderer tableCellRenderer) {
            this.tableCellRenderer = tableCellRenderer;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            Component c = tableCellRenderer.getTableCellRendererComponent(table,
                    value, isSelected, hasFocus, row, column);
            if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                if (value instanceof ImageIcon) {
                  l.setIcon((ImageIcon)value);
                  l.setText(null);
                  l.setHorizontalAlignment(JLabel.CENTER);
                } else {
                  l.setIcon(null);
                  l.setText(value.toString());
                  l.setHorizontalAlignment(JLabel.LEFT);
                }

                int modelColumn = table.convertColumnIndexToModel(column);
                Icon indicator = getHeaderRendererIcon(modelColumn, l.getFont().getSize());
                if (indicator!=null) {
                  l.setHorizontalTextPosition(JLabel.LEFT);
                  l.setIcon(indicator);
                }
            }
            return c;
        }
    }
    
    private static class Arrow implements Icon {
        private final boolean descending;
        private final int size;
        private final int priority;

        public Arrow(boolean descending, int size, int priority) {
            this.descending = descending;
            this.size = size;
            this.priority = priority;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Color color = c == null ? Color.GRAY : c.getBackground();
            // In a compound sort, make each succesive triangle 20%
            // smaller than the previous one.
            int dx = (int)(Math.pow(0.8, priority)*size/2);
            int dy = descending ? dx : -dx;
            // Align icon (roughly) with font baseline.
            y = y + 5*size/6 + (descending ? -dy : 0);
            int shift = descending ? 1 : -1;
            g.translate(x, y);

            // Right diagonal.
            g.setColor(color.darker());
            g.drawLine(dx / 2, dy, 0, 0);
            g.drawLine(dx / 2, dy + shift, 0, shift);

            // Left diagonal.
            g.setColor(color.brighter());
            g.drawLine(dx / 2, dy, dx, 0);
            g.drawLine(dx / 2, dy + shift, dx, shift);

            // Horizontal line.
            if (descending) {
                g.setColor(color.darker().darker());
            } else {
                g.setColor(color.brighter().brighter());
            }
            g.drawLine(dx, 0, 0, 0);

            g.setColor(color);
            g.translate(-x, -y);
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
    
    
}
