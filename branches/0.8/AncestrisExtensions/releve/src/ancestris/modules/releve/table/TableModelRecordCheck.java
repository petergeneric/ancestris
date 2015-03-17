package ancestris.modules.releve.table;

import ancestris.modules.releve.editor.EditorBeanField;
import ancestris.modules.releve.editor.EditorBeanGroup;
import ancestris.modules.releve.model.DataManager.RecordType;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.Field.FieldType;
import ancestris.modules.releve.model.RecordModel;
import ancestris.modules.releve.model.FieldSimpleValue;
import ancestris.modules.releve.model.Record;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Michel
 */
public class TableModelRecordCheck extends AbstractTableModel {

   private ArrayList<FieldType> fieldTypeList = new ArrayList<FieldType>();
   private ArrayList<String> columnNameList = new ArrayList<String>();
   private RecordModel modelParent;

   private final static Field birthField = new FieldSimpleValue();
   private final static Field marriageField = new FieldSimpleValue();
   private final static Field deathField = new FieldSimpleValue();

   static {
       birthField.setValue("Naissance");
       marriageField.setValue("Mariage");
       deathField.setValue("Décès");
   }

    /**
     * Constructor
     * @param dataManager
     */
    public TableModelRecordCheck(RecordModel modelAll) {
        this.modelParent = modelAll;
        int i = 0;
//        for (FieldType fieldType : FieldType.values() ) {
//            fieldTypeList[i] = fieldType;
//            columnNameList[i] = EditorBeanField.getLabel(fieldType);
//            i++;
//        }

        for (Iterator<EditorBeanGroup> groupIter =  EditorBeanGroup.getGroups(RecordType.misc).iterator() ; groupIter.hasNext(); ) {
                EditorBeanGroup group =groupIter.next();
                for (Iterator<EditorBeanField> fieldIter = group.getFields().iterator(); fieldIter.hasNext(); ) {
                    EditorBeanField editorBeanField = fieldIter.next();
                    fieldTypeList.add(editorBeanField.getFieldType());
                    columnNameList.add(editorBeanField.getLabel());
                    i++;
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
                case birth:
                    field = birthField;
                    break;
                case marriage:
                    field = marriageField;
                    break;
                case death:
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
