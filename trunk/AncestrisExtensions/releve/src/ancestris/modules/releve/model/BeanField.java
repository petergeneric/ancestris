package ancestris.modules.releve.model;

import ancestris.modules.releve.model.Field.FieldType;
import javax.swing.KeyStroke;

/**
 *
 * @author Michel
 */
public class BeanField {

    public BeanField ( Record record, FieldType fieldType) {
        this.record = record;
        this.fieldType = fieldType;
        this.field = null;
    }

    public BeanField ( Record record, String title, KeyStroke ks) {
        this.record = record;
        this.fieldType = FieldType.title;
        this.field = new FieldTitle(title, ks);
    }

    /**
     * @return the field
     */
    public Field getField() {

        if (field != null) {
            return field;
        } else {
            return record.getField(fieldType);
        }
    }

    /**
     * @return the record
     */
    public Record getRecord() {
        return record;
    }

    /**
     * @return the fieldType
     */
    public FieldType getFieldType() {
        return fieldType;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        String label;
        switch (fieldType) {
            case indiFirstName:
            case indiMarriedFirstName:
            case indiFatherFirstName:
            case indiMotherFirstName:
            case wifeFirstName:
            case wifeMarriedFirstName:
            case wifeFatherFirstName:
            case wifeMotherFirstName:
            case witness1FirstName:
            case witness2FirstName:
            case witness3FirstName:
            case witness4FirstName:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.FirstName");
                break;
            case indiLastName:
            case indiMarriedLastName:
            case indiFatherLastName:
            case indiMotherLastName:
            case wifeLastName:
            case wifeMarriedLastName:
            case wifeFatherLastName:
            case wifeMotherLastName:
            case witness1LastName:
            case witness2LastName:
            case witness3LastName:
            case witness4LastName:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.LastName");
                break;
            case indiOccupation:
            case indiMarriedOccupation:
            case indiFatherOccupation:
            case indiMotherOccupation:
            case wifeOccupation:
            case wifeMarriedOccupation:
            case wifeFatherOccupation:
            case wifeMotherOccupation:
            case witness1Occupation:
            case witness2Occupation:
            case witness3Occupation:
            case witness4Occupation:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Occupation");
                break;
            case indiComment:
            case indiMarriedComment:
            case indiFatherComment:
            case indiMotherComment:
            case wifeComment:
            case wifeMarriedComment:
            case wifeFatherComment:
            case wifeMotherComment:
            case witness1Comment:
            case witness2Comment:
            case witness3Comment:
            case witness4Comment:
            case generalComment:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Comment");
                break;
            case indiSex:
            case wifeSex:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Sex");
                 break;
            case indiMarriedDead:
            case indiFatherDead:
            case indiMotherDead:
            case wifeMarriedDead:
            case wifeFatherDead:
            case wifeMotherDead:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Dead");
                break;
            case indiPlace:
            case wifePlace:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Place");
                break;
            case indiAge:
            case wifeAge:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Age");
                break;
            case indiBirthDate:
            case wifeBirthDate:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Birth");
                break;

            case eventDate:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Date");
                break;
            case cote:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Cote");
                break;
            case parish:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Parish");
                break;
            case eventType:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.EventType");
                break;
            case freeComment:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Picture");
                break;
            case notary:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Notary");
                break;
            case title:
                label = field.toString();
                break;
            default:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Value");
                break;
        }

        return label;
    }

    
    
    private Field field;
    private Record record;
    private FieldType fieldType;

}
