package ancestris.modules.releve.table;

import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.FieldDate;
import ancestris.modules.releve.model.FieldPicture;
import ancestris.modules.releve.model.FieldSex;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.Record.RecordType;
import javax.swing.RowFilter;
import javax.swing.RowFilter.Entry;

/**
 *
 * @author Michel
 */
public class TableModelRecordBirth extends TableModelRecordAbstract {

    final String columnName[] = {
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Id"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Name"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Sex"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Date"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Father"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Mother"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Picture")
    };
    final Class<?> columnType[] = {Integer.class, String.class, FieldSex.class, FieldDate.class, String.class, String.class, FieldPicture.class};

    /**
     * Constructor
     * @param dataManager
     */
    public TableModelRecordBirth(DataManager dataManager) {
        super(dataManager);
                
    }

    ///////////////////////////////////////////////////////////////////////////
    // Implement AbstractTableModel methods
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public int getColumnCount() {
        return columnName.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnName[col];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return columnType[column];
    }

    /**
     * Cette methode est appelée par les tables qui utilisent ce modele
     * Elle retourne la valeur pour remplir la cellule (row, col)
     * ATTENTION : la date est retounée sous forme de propriete pour permettre
     * le tri par date en utilisant le comparateur de ProperyDate.
     * Par ailleurs l'affichage de la date doit être mis en en forme par le renderer
     * de la table ( jj/mm/aaa)
     *
     * @param row
     * @param col
     * @return
     */
    @Override
    public Object getValueAt(int row, int col) {
        Record record = getRecord(row);
        Object value;
        switch (col) {
            case 0:
                value = row + 1;
                break;
            case 1:
                value = record.getFieldValue(FieldType.indiLastName) + " " + record.getFieldValue(FieldType.indiFirstName);
                break;
            case 2:
                value = record.getFieldValue(FieldType.indiSex);
                break;
            case 3:
                value = record.getField(FieldType.eventDate);
                break;
            case 4:
                value = record.getFieldValue(FieldType.indiFatherLastName) + " " + record.getFieldValue(FieldType.indiFatherFirstName);
                break;
            case 5:
                value = record.getFieldValue(FieldType.indiMotherLastName) + " " + record.getFieldValue(FieldType.indiMotherFirstName);
                break;
            case 6:
                value = record.getFieldValue(FieldType.cote) + " " + record.getFieldValue(FieldType.freeComment);
                break;
            default:
                value = super.getValueAt(row, col);
                break;
        }
        return value;
    }
    
    @Override
    public RowFilter<TableModelRecordAbstract, Integer> getRecordFilter() {
        return new RowFilter<TableModelRecordAbstract, Integer>() {

            @Override
            public boolean include(Entry<? extends TableModelRecordAbstract, ? extends Integer> entry) {
                return getRecord(entry.getIdentifier()).getType() == RecordType.BIRTH;
            }
        };
    }

    @Override
    public String getModelName() {
        return "birth";
    }

}
