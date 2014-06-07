package ancestris.modules.releve.table;

import ancestris.modules.releve.dnd.ViewWrapperManager;
import ancestris.modules.releve.model.FieldSex;
import ancestris.modules.releve.model.FieldSimpleValue;
import ancestris.modules.releve.model.GedcomLink;
import ancestris.modules.releve.model.Record;
import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.util.swing.HeadlessLabel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
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
                int column = target.columnAtPoint(e.getPoint());

                if (column == 0) {
                    GedcomLink gedcomLink = (GedcomLink) getModel().getValueAt(convertRowIndexToModel(row), -1);
                    if (gedcomLink != null) {
                        Property property = gedcomLink.getProperty();

                        if (property != null) {
                            if (e.getClickCount() == 2) {
                                ViewWrapperManager.setRootAllTreeview(property);
                            } else {
                                ViewWrapperManager.showEntityAllTreeview(property);
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

        NbPreferences.forModule(ReleveTable.class).put(
                ((TableModelRecordAbstract) getModel()).getModelName() + "ColumnLayout",
                columnLayout.toString());
    }

    /**
     * Renderer pour afficher les dates avec le format jj/mm/aaaa
     */
    private class Renderer extends HeadlessLabel implements TableCellRenderer {

        Renderer() {
            setPadding(2);
        }

        /**
         * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)
         */
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focs, int row, int col) {

            setFont(table.getFont());
            if (getRowHeight() != getPreferredSize().height) {
                setRowHeight(getPreferredSize().height);
            }

            if ( value != null) {
                if (value instanceof PropertyDate) {
                    setText(((PropertyDate) value).getDisplayValue());
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
                if (gedcomLink != null && gedcomLink.getCompareResult()== GedcomLink.CompareResult.EQUAL) {
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
}
