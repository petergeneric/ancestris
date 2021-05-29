package ancestris.modules.releve.table;

import ancestris.modules.releve.editor.EditorBeanField;
import ancestris.modules.releve.editor.EditorBeanGroup;
import ancestris.modules.releve.model.Record.RecordType;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.RecordModel;
import ancestris.modules.releve.model.FieldSimpleValue;
import ancestris.modules.releve.model.Record;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Michel
 */
public class TableModelRecordCheck extends AbstractTableModel {

   private final ArrayList<FieldType> fieldTypeList = new ArrayList<FieldType>();
   private final ArrayList<String> columnNameList = new ArrayList<String>();
   private final RecordModel modelParent;

   private final static Field birthField = new FieldSimpleValue();
   private final static Field marriageField = new FieldSimpleValue();
   private final static Field deathField = new FieldSimpleValue();

   static {
       birthField.setValue(java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.row.Birth"));
       marriageField.setValue(java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.row.Marriage"));
       deathField.setValue(java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.row.Death"));
   }

    /**
     * Constructor
     * @param dataManager
     */
    public TableModelRecordCheck(RecordModel modelAll) {
        this.modelParent = modelAll;
       for (EditorBeanGroup group : EditorBeanGroup.getGroups(RecordType.MISC)) {
            for (EditorBeanField editorBeanField : group.getFields()) {
                fieldTypeList.add(editorBeanField.getFieldType());
                columnNameList.add(editorBeanField.getLabel());
            }
       }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Implement AbstractTableModel methods
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public int getColumnCount() {
        return columnNameList.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNameList.get(col);
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return Field.class;
    }
    
    @Override
    public int getRowCount() {
        return modelParent.getRowCount();
    }
    
    @Override
    public Object getValueAt(int row, int col) {
        Record record = modelParent.getRecord(row);
        
        Field field;
        if ( col == 0 ) {
            // la colonne 0 contient le type d'acte
            // si le releve est une naissance, deces, ou maraige, j'affiche une constante
            // si type de releve = misc , j'affiche le contenu du champ EventType
            switch (record.getType()) {
                case BIRTH:
                    field = birthField;
                    break;
                case MARRIAGE:
                    field = marriageField;
                    break;
                case DEATH:
                    field = deathField;
                    break;
                default:
                    field = record.getField(fieldTypeList.get(col));
            }
        } else {
            field = record.getField(fieldTypeList.get(col));
        }

        if (field != null) {
            return field;
        } else {
            return "";
        }
    }


}
