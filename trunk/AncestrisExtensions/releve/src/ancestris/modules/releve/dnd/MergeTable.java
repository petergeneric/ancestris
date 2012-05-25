package ancestris.modules.releve.dnd;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.PropertyDate;
import genj.gedcom.Source;
import genj.util.WordBuffer;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.openide.util.NbPreferences;

/**
 * La table affiche les données du relevé et les données de l'individu
 * dans deux colonnes afin de pouvoir les comparer facilement
 * et sélectionner les données du relevé à copier dans l'individu
 * et désélectionner celles que l'on ne veut pas copier
 * @author Michel
 */
public class MergeTable extends JTable {
    private EntityActionManager entityActionManager = null;

    public  MergeTable() {
        setPreferredSize(null);
        setAutoResizeMode(AUTO_RESIZE_OFF );
        setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN );
        loadColumnLayout();
    }

    public void setModel(MergeModel model) {
        super.setModel(model);
        MergeTableRenderer mergeTableRenderer = new MergeTableRenderer(model);
        setDefaultRenderer(Object.class, mergeTableRenderer);
        
        loadColumnLayout();

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                JTable target = (JTable) e.getSource();
                int row = target.rowAtPoint(e.getPoint());
                int column = target.columnAtPoint(e.getPoint());

                if (column == 4) {
                    Entity entity = (Entity) ((MergeModel)getModel()).getValueAt(row, column);
                    if ( entityActionManager != null && entity != null) {
                        entityActionManager.showEntity(entity, false);
                    }
                }

            }
        });

    }

    @Override
    public TableCellEditor getCellEditor(final int row, int column) {
        //int modelColumn = convertColumnIndexToModel(column);
        final int modelColumn = column;
        if ((modelColumn == 1 || modelColumn == 3) && ((MergeModel)getModel()).getChoice(row, modelColumn) != null) {
            JComboBox comboBox = new JComboBox(((MergeModel)getModel()).getChoice(row, modelColumn));
            comboBox.setEditable(false);
            comboBox.setRenderer(new MergeCellComboRenderer());
            return new DefaultCellEditor(comboBox);
        } else {
            return super.getCellEditor(row, column);
        }
    }

    /**
     * Enregistrer la largeur des colonnes
     * Cette méthode est appelée par la fenêtre principal avant la fermeture
     */
    protected void componentClosed() {
        saveColumnLayout();

    }

    /**
     * Set column layout from bundle configuration
     */
    private void loadColumnLayout() {
        String columnLayout = NbPreferences.forModule(MergeTable.class).get("MergeColumnLayout", "4,100,120,20,120");
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

    /**
     * Return column layout - a string that can be used to return column widths and sorting
     */
    private void saveColumnLayout() {

        TableColumnModel columns = this.getColumnModel();

        WordBuffer columnLayout = new WordBuffer(",");
        columnLayout.append(columns.getColumnCount());

        for (int c = 0; c < columns.getColumnCount(); c++) {
            columnLayout.append(columns.getColumn(c).getWidth());
        }
        NbPreferences.forModule(MergeTable.class).put("MergeColumnLayout", columnLayout.toString());

    }

    void setEntityActionManager(EntityActionManager entityActionManager) {
        this.entityActionManager = entityActionManager;
    }



    /**
     * Cette classe gère l'affichage des celleules de la table
     */
    private class MergeTableRenderer extends JLabel implements TableCellRenderer {
        private MergeModel model;

        public MergeTableRenderer(MergeModel model) {
            this.model = model;
            setOpaque(true);
        }

        /**
         *
         * @param table
         * @param value
         * @param isSelected
         * @param hasFocus
         * @param row
         * @param column
         * @return
         */
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            //int modelColumn = table.convertColumnIndexToModel(column);

            // je choisis le format d'affichage du texte
            int modelColumn = column;
            if ( value != null ) {
                if (value instanceof PropertyDate) {
                    setText(((PropertyDate)value).getDisplayValue());
                } else if (value instanceof Entity) {
                    setText(((Entity)value).getId());
                } else {
                    setText(value.toString());
                }
            } else {
                setText("");
            }

            // je choisis la couleur de fond
            switch (modelColumn) {
                case 0:
                    //setBackground(Color.lightGray);
                    setBackground(new Color(240,240,240));
                    setForeground(table.getForeground());
                    break;
                case 1:
                case 2:
                case 3:
                    switch(model.dataList.get(row).compareResult) {
                        case NOT_APPLICABLE:
                            setBackground(new Color(240,240,240));
                            break;
                        case CONFLIT:
                            setBackground(Color.red);
                            break;
                        case EQUAL:
                            setBackground(table.getBackground());
                            break;
                        default:
                            setBackground(Color.ORANGE);
                    }
                    setForeground(table.getForeground());
                    setOpaque(true);
                    break;
                case 4:
                    setForeground(Color.blue);
                    setOpaque(true);
                    break;
                default:
                    setBackground(table.getBackground());
                    setForeground(table.getForeground());
                    setOpaque(true);
                    break;
             }
             return this;

        }
    }

    private class MergeCellComboRenderer extends JLabel implements ListCellRenderer {

        public MergeCellComboRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            if (value != null) {
                if (value instanceof Source) {
                    setText(((Source) value).toString());
                } else {
                    setText(value.toString());
                }
            } else {
                setText("");
            }
            return this;
        }
    }


}
