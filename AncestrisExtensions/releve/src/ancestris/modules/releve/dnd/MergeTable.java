package ancestris.modules.releve.dnd;

import genj.gedcom.PropertyDate;
import genj.gedcom.Source;
import genj.util.WordBuffer;
import java.awt.Color;
import java.awt.Component;
import java.util.StringTokenizer;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
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

    public  MergeTable() {
        setPreferredSize(null);
        setAutoResizeMode(AUTO_RESIZE_OFF );
        setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN );
        loadColumnLayout();
    }

    public void setModel(MergeModel model) {
        super.setModel(model);
        setDefaultRenderer(Object.class, new MergeTableRenderer(model));
        loadColumnLayout();
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        //int modelColumn = convertColumnIndexToModel(column);
        int modelColumn = column;
        if ((modelColumn == 1 || modelColumn == 2) && ((MergeModel)getModel()).getChoice(row, modelColumn) != null) {
            JComboBox comboBox = new JComboBox(((MergeModel)getModel()).getChoice(row, modelColumn));
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
            int modelColumn = column;
            if ( value != null ) {
                if (value instanceof PropertyDate) {
                    setText(((PropertyDate)value).getDisplayValue());
                } else {
                    setText(value.toString());
                }
            } else {
                setText("");
            }

            switch (modelColumn) {
                case 0:
                    //setBackground(Color.lightGray);
                    setBackground(new Color(240,240,240));
                    setForeground(table.getForeground());
                    break;
                case 1:
                case 2:
                case 3:
                    switch(model.dataList.get(row).sameValue) {
                        case -2:
                            setBackground(Color.lightGray);
                            break;
                        case -1:
                            setBackground(Color.red);
                            break;
                        case 0:
                            setBackground(table.getBackground());
                            break;
                        default:
                            setBackground(Color.ORANGE);
                    }

//                    if ( model.dataList.get(row).recordValue == null) {
//                        setBackground(Color.lightGray);
//                    } else if (model.dataList.get(row).entityValue == null) {
//                        setBackground(Color.ORANGE);
//                    } else if ( model.dataList.get(row).entityValue instanceof PropertyDate) {
//                        if ( model.isBestBirthDate((PropertyDate)model.dataList.get(row).recordValue, (PropertyDate)model.dataList.get(row).entityValue, null)) {
//                            setBackground(Color.ORANGE);
//                        } else {
//                            setBackground(table.getBackground());
//                        }
//
//                    } else if ( model.dataList.get(row).entityValue instanceof Entity) {
//                        // je compare d'abord avec compareTo() avant equals() à
//                        // cause des objets de type Property qui doit être comparé avec compareTo()
//
//                        if ( ((Entity)model.dataList.get(row).entityValue).compareTo((Entity)model.dataList.get(row).recordValue)==0)  {
//                            setBackground(table.getBackground());
//                        } else {
//                            setBackground(Color.ORANGE);
//                        }
//                    } else if ( model.dataList.get(row).entityValue.equals(model.dataList.get(row).recordValue)) {
//                        setBackground(table.getBackground());
//                    } else {
//                        setBackground(Color.ORANGE);
//                    }
                    setForeground(table.getForeground());
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
