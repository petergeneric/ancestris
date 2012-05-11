package ancestris.modules.releve.model;

import ancestris.modules.releve.ReleveTopComponent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.openide.util.NbPreferences;

/**
 *
 * @author Michel
 */
public class ModelDeath extends ModelAbstract {

    final String columnName[] = {
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Id"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Name"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Sex"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Date"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Age"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Father"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Mother"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Place") ,
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Picture")
    };
    final Class columnClass[] = {Integer.class, String.class, FieldSex.class, FieldDate.class, String.class, String.class, String.class, String.class, FieldPicture.class};

    /**
     * Constructor
     * @param dataManager
     */
    public ModelDeath() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Implement ModelAbstract methods
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public RecordDeath createRecord() {
        return new RecordDeath();
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
    public Class getColumnClass(int column) {
        return columnClass[column];
    }

   @Override
    public Object getValueAt(int row, int col) {
        Object value;
        switch (col) {
            case 0:
                value = new Integer(getRecord(row).recordNo);
                break;
            case 1:
                value = getRecord(row).getIndiLastName().toString() + " " + getRecord(row).getIndiFirstName().toString();
                break;
            case 2:
                value = getRecord(row).getIndiSex();
                break;
            case 3:
                value = getRecord(row).getEventDateField();
                break;
            case 4:
                value = getRecord(row).getIndiAge();
                break;
            case 5:
                value = getRecord(row).getIndiFatherLastName().toString() + " " + getRecord(row).getIndiFatherFirstName().toString();
                break;
            case 6:
                value = getRecord(row).getIndiMotherLastName().toString() + " " + getRecord(row).getIndiMotherFirstName().toString();
                break;
            case 7:
                value = getRecord(row).getIndiPlace().toString();
                break;
            case 8:
                value = getRecord(row).getFreeComment();
                break;
            default:
                value = "";
                break;
        }
        return value;
    }

    @Override
    public String getColumnLayout() {
       return NbPreferences.forModule(ReleveTopComponent.class).get("DeathColumnLayout", "");
    }

    @Override
    public void putColumnLayout(String columnLayout) {
        NbPreferences.forModule(ReleveTopComponent.class).put("DeathColumnLayout", columnLayout);
    }

    @Override
    public int getEditorWidth() {
        return Integer.valueOf(NbPreferences.forModule(ReleveTopComponent.class).get("DeathEditorWidth", "270"));
    }

    @Override
    public void putEditorWidth(int width) {
        NbPreferences.forModule(ReleveTopComponent.class).put("DeathEditorWidth", String.valueOf(width));
    }

    
    /**
     * retourne la liste des champs affichables
     * @return
     */
    @Override
    public BeanField[] getFieldList( int row ) {
        KeyStroke ks1 = KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.ALT_DOWN_MASK);
        KeyStroke ks2 = KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.ALT_DOWN_MASK);
        KeyStroke ks3 = KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.ALT_DOWN_MASK);
        KeyStroke ks4 = KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.ALT_DOWN_MASK);
        KeyStroke ks5 = KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.ALT_DOWN_MASK);
        KeyStroke ks8 = KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.ALT_DOWN_MASK);
        KeyStroke ks9 = KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.ALT_DOWN_MASK);

        Record record = getRecord(row);
        if( record == null)  {
            return new BeanField[0];
        }

        BeanField beanField[] = {
            //new BeanField(record, record.getCityName() + "," + record.getStateName() + "," + record.getCountyName(), null),
            new BeanField(record, java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.row.Death"), ks1),
            new BeanField(record, Field.FieldType.eventDate),
            new BeanField(record, Field.FieldType.parish),
            new BeanField(record, Field.FieldType.cote),
            new BeanField(record, Field.FieldType.freeComment),

            new BeanField(record, java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.row.Dead"), ks2),
            new BeanField(record, Field.FieldType.indiLastName),
            new BeanField(record, Field.FieldType.indiFirstName),
            new BeanField(record, Field.FieldType.indiSex),
            new BeanField(record, Field.FieldType.indiAge),
            new BeanField(record, Field.FieldType.indiBirthDate),
            new BeanField(record, Field.FieldType.indiPlace),
            new BeanField(record, Field.FieldType.indiOccupation),
            new BeanField(record, Field.FieldType.indiComment),

            new BeanField(record, java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.row.Married"), null),
            new BeanField(record, Field.FieldType.indiMarriedLastName),
            new BeanField(record, Field.FieldType.indiMarriedFirstName),
            new BeanField(record, Field.FieldType.indiMarriedDead),
            new BeanField(record, Field.FieldType.indiMarriedOccupation),
            new BeanField(record, Field.FieldType.indiMarriedComment),

            new BeanField(record, java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.row.Father"), ks3),
            new BeanField(record, Field.FieldType.indiFatherLastName),
            new BeanField(record, Field.FieldType.indiFatherFirstName),
            new BeanField(record, Field.FieldType.indiFatherDead),
            new BeanField(record, Field.FieldType.indiFatherOccupation),
            new BeanField(record, Field.FieldType.indiFatherComment),

            new BeanField(record, java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.row.Mother"), ks4),
            new BeanField(record, Field.FieldType.indiMotherLastName),
            new BeanField(record, Field.FieldType.indiMotherFirstName),
            new BeanField(record, Field.FieldType.indiMotherDead),
            new BeanField(record, Field.FieldType.indiMotherOccupation),
            new BeanField(record, Field.FieldType.indiMotherComment),

            new BeanField(record, java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.row.Witness1"), ks8),
            new BeanField(record, Field.FieldType.witness1LastName),
            new BeanField(record, Field.FieldType.witness1FirstName),
            new BeanField(record, Field.FieldType.witness1Occupation),
            new BeanField(record, Field.FieldType.witness1Comment),
            new BeanField(record, java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.row.Witness2"), null),
            new BeanField(record, Field.FieldType.witness2LastName),
            new BeanField(record, Field.FieldType.witness2FirstName),
            new BeanField(record, Field.FieldType.witness2Occupation),
            new BeanField(record, Field.FieldType.witness2Comment),
            new BeanField(record, java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.row.Witness3"), null),
            new BeanField(record, Field.FieldType.witness3LastName),
            new BeanField(record, Field.FieldType.witness3FirstName),
            new BeanField(record, Field.FieldType.witness3Occupation),
            new BeanField(record, Field.FieldType.witness3Comment),
            new BeanField(record, java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.row.Witness4"), null),
            new BeanField(record, Field.FieldType.witness4LastName),
            new BeanField(record, Field.FieldType.witness4FirstName),
            new BeanField(record, Field.FieldType.witness4Occupation),
            new BeanField(record, Field.FieldType.witness4Comment),

            new BeanField(record, java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.row.GeneralComment"), ks9),
            new BeanField(record, Field.FieldType.generalComment)
        };

        return beanField;
    }

}
