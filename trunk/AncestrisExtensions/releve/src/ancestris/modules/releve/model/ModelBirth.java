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
public class ModelBirth extends ModelAbstract {

    final String columnName[] = {"Id", "Nom", "Sexe", "Date", "Père", "Mère", "Photo"};
    final Class columnType[] = {Integer.class, String.class, FieldSex.class, FieldDate.class, String.class, String.class, FieldPicture.class};

    /**
     * Constructor
     * @param dataManager
     */
    public ModelBirth() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Implement ModelAbstract methods
    ///////////////////////////////////////////////////////////////////////////
    /**
     * ajout un nouveau releve dans le modele
     * @return indexRecord
     */
    @Override
    public Record createRecord() {
        return new RecordBirth();
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
                value = getRecord(row).getIndiFatherLastName().toString() + " " + getRecord(row).getIndiFatherFirstName().toString();
                break;
            case 5:
                value = getRecord(row).getIndiMotherLastName().toString() + " " + getRecord(row).getIndiMotherFirstName().toString();
                break;
            case 6:
                value = getRecord(row).getFreeComment();
                break;
            default:
                value = "";
                break;
        }
        return value;
    }

    /**
     * retourne la liste des champs affichables dans l'editeur
     * @return
     */
    @Override
    public BeanField[] getFieldList(int recordIndex) {

        Record record = getRecord(recordIndex);
        if (record == null) {
            // y a rien a voir
            return new BeanField[0];
        }

        KeyStroke ks1 = KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.ALT_DOWN_MASK);
        KeyStroke ks2 = KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.ALT_DOWN_MASK);
        KeyStroke ks3 = KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.ALT_DOWN_MASK);
        KeyStroke ks4 = KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.ALT_DOWN_MASK);
        KeyStroke ks8 = KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.ALT_DOWN_MASK);
        KeyStroke ks9 = KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.ALT_DOWN_MASK);

        BeanField beanField[] = {
            //new BeanField(record, record.getCityName() + "," + record.getCityCode() + "," + record.getCountyName(), null),
            new BeanField(record, "Naissances", ks1),
            new BeanField(record, Field.FieldType.eventDate),
            new BeanField(record, Field.FieldType.parish),
            new BeanField(record, Field.FieldType.cote),
            new BeanField(record, Field.FieldType.freeComment),
            new BeanField(record, "Enfant", ks2),
            new BeanField(record, Field.FieldType.indiLastName),
            new BeanField(record, Field.FieldType.indiFirstName),
            new BeanField(record, Field.FieldType.indiSex),
            new BeanField(record, Field.FieldType.indiBirthDate),
            new BeanField(record, Field.FieldType.indiComment),
            new BeanField(record, "Père", ks3),
            new BeanField(record, Field.FieldType.indiFatherLastName),
            new BeanField(record, Field.FieldType.indiFatherFirstName),
            new BeanField(record, Field.FieldType.indiFatherOccupation),
            new BeanField(record, Field.FieldType.indiFatherComment),
            new BeanField(record, "Mère", ks4),
            new BeanField(record, Field.FieldType.indiMotherLastName),
            new BeanField(record, Field.FieldType.indiMotherFirstName),
            new BeanField(record, Field.FieldType.indiMotherOccupation),
            new BeanField(record, Field.FieldType.indiMotherComment),
            new BeanField(record, "Parrain/Témoin 1", ks8),
            new BeanField(record, Field.FieldType.witness1LastName),
            new BeanField(record, Field.FieldType.witness1FirstName),
            new BeanField(record, Field.FieldType.witness1Occupation),
            new BeanField(record, Field.FieldType.witness1Comment),
            new BeanField(record, "Parrain/Témoin 2", null),
            new BeanField(record, Field.FieldType.witness2LastName),
            new BeanField(record, Field.FieldType.witness2FirstName),
            new BeanField(record, Field.FieldType.witness2Occupation),
            new BeanField(record, Field.FieldType.witness2Comment),
            new BeanField(record, "Témoin 3", null),
            new BeanField(record, Field.FieldType.witness3LastName),
            new BeanField(record, Field.FieldType.witness3FirstName),
            new BeanField(record, Field.FieldType.witness3Occupation),
            new BeanField(record, Field.FieldType.witness3Comment),
            new BeanField(record, "Témoin 4", null),
            new BeanField(record, Field.FieldType.witness4LastName),
            new BeanField(record, Field.FieldType.witness4FirstName),
            new BeanField(record, Field.FieldType.witness4Occupation),
            new BeanField(record, Field.FieldType.witness4Comment),
            new BeanField(record, "Commentaire général", ks9),
            new BeanField(record, Field.FieldType.generalComment)
        };


        return beanField;
    }

    @Override
    public String getColumnLayout() {
        return NbPreferences.forModule(ReleveTopComponent.class).get("BirthColumnLayout", "");
    }

    @Override
    public void putColumnLayout(String columnLayout) {
        NbPreferences.forModule(ReleveTopComponent.class).put("BirthColumnLayout", columnLayout);
    }

    @Override
    public int getEditorWidth() {
        return Integer.valueOf(NbPreferences.forModule(ReleveTopComponent.class).get("BirthEditorWidth", "270"));
    }

    @Override
    public void putEditorWidth(int width) {
        NbPreferences.forModule(ReleveTopComponent.class).put("BirthEditorWidth", String.valueOf(width));
    }
}
