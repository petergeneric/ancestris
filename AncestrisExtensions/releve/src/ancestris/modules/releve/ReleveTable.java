package ancestris.modules.releve;

import ancestris.modules.releve.dnd.RecordTransferHandle;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.ModelAbstract;
import ancestris.modules.releve.model.FieldSex;
import ancestris.modules.releve.model.Record;
import genj.gedcom.PropertyDate;
import genj.util.WordBuffer;
import genj.util.swing.HeadlessLabel;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;
import javax.swing.DropMode;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Michel
 */
public class ReleveTable extends JTable {

    private TableSelectionListener selectionListeners = null;
    DataManager dataManager = null;

    public ReleveTable () {
        super();

        // pas de redimensionnement automatique, voir loadColumnLayout()
        setAutoResizeMode(AUTO_RESIZE_OFF );
        // setFocusable(false); a ne pas mettre, sinon les deplacements avec les fleches du clavier ne fonctionnent plus

        // j'ajoute le renderer pour l'affichage des dates et le choix de la font
        setDefaultRenderer(Object.class, new Renderer());
        
        Renderer r = new Renderer();
        r.setFont(getFont());

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setDragEnabled(true);
        setDropMode(DropMode.USE_SELECTION);
        setTransferHandler(new RecordTransferHandle());
        setFillsViewportHeight(true);

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
     * Initialise le modele de données de la JTable
     * @param model
     */
   public void setModel(DataManager dataManager, DataManager.ModelType modelType) {
       this.dataManager = dataManager;
       // save previous column layout
        saveColumnLayout();
        super.setModel(dataManager.getModel(modelType));
        this.setAutoCreateRowSorter(true);
        //TableRowSorter tableRowSorter = new TableRowSorter(model);
        //this.setRowSorter(tableRowSorter);
        // j'applique le nouveau layout
        loadColumnLayout();
    }


    public void dropRecord(Record record) {
        int recordIndex = dataManager.addRecord(record);

        ((ModelAbstract)getModel()).fireTableRowsInserted(recordIndex, recordIndex);
    }
    ///////////////////////////////////////////////////////////////////////////
    // Manager VerificationListener
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @param validationListeners the validationListeners to set
     */
    public void setTableSelectionListener(TableSelectionListener listener) {
        selectionListeners = listener;
    }

    /**
     *
     * @param recordIndex index du relevé à selectionner 
     * @param isNewRecord true si c'est un nouveau releve, false si c'est
     */public void fireTableSelectionListener(int recordIndex, boolean isNewRecord) {
        if ( selectionListeners != null) {
            selectionListeners.rowSelected(recordIndex, isNewRecord);
        }
    }

    /**
     * Verification de la ligne courante avant de selectionner une autre ligne
     * avec un clic gauche de la souris
     * @param e
     */
    @Override
    protected void processMouseEvent(MouseEvent e) {
        if (e.getID() == MouseEvent.MOUSE_PRESSED) {
            boolean result = true;
            if (getSelectedRowCount() > 0) {
                if (getSelectedRow() != -1) {
                    String errorMessage = ((ModelAbstract)getModel()).verifyRecord();
                    if ( !errorMessage.isEmpty()) {
                        // j'affiche le message d'erreur
                        JOptionPane.showMessageDialog(getTopLevelAncestor(), errorMessage, "Relevé", JOptionPane.ERROR_MESSAGE);
                        result = false;
                    }
                }
            }
            if (result == true) {
                // je continue le traitement de l'evenement
                super.processMouseEvent(e);
            } else {
                // j'abandonne le traitement de  l'evenement
                e.consume();
            }
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
    public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        // j'execute le traitement par defaut
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
        // j'informe les listeners de la nouvelle selection
        if (!toggle && !extend) {
            int recordIndex = convertRowIndexToModel(rowIndex);
            fireTableSelectionListener(recordIndex, false);
        }
    }

    /**
     * Cette methode est appelée par le modele de donnée quand des rleves sont
     * ajoutés ou supprimés.
     * Si une nouvelle ligne est ajoutée, je sélectionne la nouvelle ligne.
     * Si une ligne est supprimée, je sélectionne la ligne suivante.
     *
     * Je laisse le ocmportement par defaut quand une ligne est modifiée.
     * @param e
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        switch (e.getType()) {
            case TableModelEvent.INSERT:
                super.tableChanged(e);
                // si la notification vient de l'editeur principal
                // je selectionne le releve qui vient d'etre cree
                int currentRecordIndex = selectionListeners.getCurrentRecordIndex();
                if ( e.getFirstRow() == currentRecordIndex){
                    setRowSelectionInterval(convertRowIndexToView(e.getFirstRow()), convertRowIndexToView(e.getLastRow()));
                    // je scrolle pour voir la ligne
                    Rectangle cellRect = getCellRect(getSelectedRow(), getSelectedColumn(), false);
                    if (cellRect != null) {
                        scrollRectToVisible(cellRect);
                    }
                    selectionListeners.rowSelected(currentRecordIndex, true);
                }
                break;
            case TableModelEvent.DELETE:
                if (getRowCount() > 0) {
                    int rowIndex;
                    if (e.getFirstRow() < getRowCount() - 1) {
                        // je recupere l'index de la ligne à supprimer dans la table.
                        // ATTENTION : convertRowIndexToView doit être utilisé
                        // avant d'appeler super.tableChanged(e) sinon le resultat
                        // n'est plus valable
                        rowIndex = convertRowIndexToView(e.getFirstRow());
                    } else {
                        // c'est le dernier enregistrement
                        rowIndex = getRowCount() - 1;
                    }

                    if (rowIndex < getRowCount() - 1) {
                        super.tableChanged(e);
                        // je verifie s'il reste au moins une ligne
                        if (getRowCount() > 0 ) {
                            // je verifie si la ligne suivante est encore presente.
                            if ( rowIndex < getRowCount() ) {
                                // je selectionne la ligne suivante qui est le devenu le
                                // la ligne courante apres avoir appelé super.tableChanged(e);
                            } else {
                                // je selectionne la dernière ligne
                                rowIndex = getRowCount() -1;
                            }
                            setRowSelectionInterval(rowIndex, rowIndex);
                            // je scrolle la fenetre de l'editeur pour voir le champ
                            Rectangle cellRect2 = getCellRect(rowIndex, getSelectedColumn(), false);
                            if (cellRect2 != null) {
                                scrollRectToVisible(cellRect2);
                            }
                            // je notifie les listeners (editeur)
                            int recordIndex = convertRowIndexToModel(rowIndex);
                            fireTableSelectionListener(recordIndex, false);
                        } else {
                            // il n'y a plus de ligne
                            // je previens l'editeur
                            fireTableSelectionListener(-1, false);
                        }
                    } else {
                        // la derniere ligne est selectionnee
                        super.tableChanged(e);
                        if (getRowCount() > 0) {
                            // je selectionne l'avant derniere ligne qui est devenue
                            // la derniere apres avoir appelé super.tableChanged(e)
                            rowIndex = getRowCount() - 1;
                            setRowSelectionInterval(rowIndex, rowIndex);
                            // je notifie les listeners
                            int recordIndex = convertRowIndexToModel(rowIndex);
                            fireTableSelectionListener(recordIndex, false);
                        } else {
                            // il n'y a plus de ligne
                            fireTableSelectionListener(-1, false);
                        }
                    }
                } else {
                    // il n'y a plus de releve
                    super.tableChanged(e);
                    fireTableSelectionListener(-1, false);
                }

                break;
            default:
                super.tableChanged(e);
        }
    }

    /**
     * Set column layout from bundle configuration
     */
    public void loadColumnLayout() {
        String columnLayout = "";
        if (getModel() instanceof ModelAbstract && getModel() != null) {
            // je recupere la largeur des colonnes de la session precedente
            columnLayout = ((ModelAbstract)getModel()).getColumnLayout();

            try {
                StringTokenizer tokens = new StringTokenizer(columnLayout, ",");
                int n = Integer.parseInt(tokens.nextToken());
                TableColumnModel columns = this.getColumnModel();
                for (int i = 0; i < n && i < columns.getColumnCount() ; i++) {
                    TableColumn col = columns.getColumn(i);
                    int w = Integer.parseInt(tokens.nextToken());
                    col.setWidth(w);
                    col.setPreferredWidth(w);
                }
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    /**
     * Return column layout - a string that can be used to return column widths and sorting
     */
    public void saveColumnLayout() {

        if (getModel() instanceof ModelAbstract && getModel() != null) {

            // e.g. 4, 40, 60, 70, 48, 0, -1, 1, 1
            // for a table with 4 columns and two sort directives

            //TableModel model = this.getModel();
            TableColumnModel columns = this.getColumnModel();
            //List<Directive> directives = model.getDirectives();

            WordBuffer columnLayout = new WordBuffer(",");
            columnLayout.append(columns.getColumnCount());

            for (int c = 0; c < columns.getColumnCount(); c++) {
                columnLayout.append(columns.getColumn(c).getWidth());
            }

    //        for (int d = 0; d < directives.size(); d++) {
    //            SortableTableModel.Directive dir = directives.get(d);
    //            result.append(dir.getColumn());
    //            result.append(dir.getDirection());
    //        }

           ((ModelAbstract)getModel()).putColumnLoyout(columnLayout.toString());
        }

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
                } else {
                    setText(value.toString());
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
            } else {
                setText("");
                setHorizontalAlignment(SwingConstants.LEFT);
            }

            // background?
            if (selected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
                setOpaque(true);
            } else {
                setForeground(table.getForeground());
                setOpaque(false);
            }
            // ready
            return this;
        }
    }    
}
