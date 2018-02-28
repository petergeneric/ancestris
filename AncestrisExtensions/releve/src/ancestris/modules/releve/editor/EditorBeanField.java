package ancestris.modules.releve.editor;

import ancestris.modules.releve.editor.EditorBeanGroup.GroupId;
import ancestris.modules.releve.model.Record.RecordType;
import ancestris.modules.releve.model.Record.FieldType;

/**
 *
 * @author Michel
 */
public class EditorBeanField {


    /**
     * Utilisation et affichage par defaut des champs dans l'editeur
     */
    static void init() {
        //   group                  Field                            Birth          Marriage       Death          Misc
	//                                                           used   visible used   visible used   visible used   visible
	init(GroupId.general,       FieldType.eventType,             false, false,  false, false,  false, false,  true,  true );
        init(GroupId.general,       FieldType.secondDate,            false, false,  false, false,  false, false,  true,  true );
        init(GroupId.general,       FieldType.eventDate,             true,  true,   true,  true,   true,  true,   true,  true );
        init(GroupId.general,       FieldType.parish,     	     true,  true,   true,  true,   true,  true,   true,  true );
        init(GroupId.general,       FieldType.notary,                false, false,  false, false,  false, false,  true,  true );
        init(GroupId.general,       FieldType.cote,                  true,  true,   true,  true,   true,  true,   true,  true );
        init(GroupId.general,       FieldType.freeComment,           true,  true,   true,  true,   true,  true,   true,  true );
        
	init(GroupId.indi,          FieldType.indiLastName,          true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.indi,          FieldType.indiFirstName,         true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.indi,          FieldType.indiSex,               true,  true,   false, false,  true,  true,   true,  true );
	init(GroupId.indi,          FieldType.indiAge,               false, false,  true,  true,   true,  true,   true,  true );
	init(GroupId.indi,          FieldType.indiBirthDate,         true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.indi,          FieldType.indiBirthPlace,        true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.indi,          FieldType.indiBirthAddress,      true,  true,   true,  false,  true,  false,  true,  false);
	init(GroupId.indi,          FieldType.indiOccupation,        false, false,  true,  true,   true,  true,   true,  true );
	init(GroupId.indi,          FieldType.indiResidence,         false, false,  true,  true,   true,  true,   true,  true );
	init(GroupId.indi,          FieldType.indiAddress,           false, false,  true,  true,   true,  true,   true,  true );
	init(GroupId.indi,          FieldType.indiComment,           true,  true,   true,  true,   true,  true,   true,  true );

	init(GroupId.indiMarried,   FieldType.indiMarriedLastName,   false, false,  true,  true,   true,  true,   true,  true );
	init(GroupId.indiMarried,   FieldType.indiMarriedFirstName,  false, false,  true,  true,   true,  true,   true,  true );
	init(GroupId.indiMarried,   FieldType.indiMarriedDead,       false, false,  true,  true,   true,  true,   true,  true );
	init(GroupId.indiMarried,   FieldType.indiMarriedOccupation, false, false,  true,  true,   true,  true,   true,  true );
	init(GroupId.indiMarried,   FieldType.indiMarriedResidence,  false, false,  true,  true,   true,  true,   true,  true );
	init(GroupId.indiMarried,   FieldType.indiMarriedAddress,    true,  false,  true,  false,  true,  false,  true,  false);
	init(GroupId.indiMarried,   FieldType.indiMarriedComment,    false, false,  true,  true,   true,  true,   true,  true );

	init(GroupId.indiFather,    FieldType.indiFatherLastName,    true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.indiFather,    FieldType.indiFatherFirstName,   true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.indiFather,    FieldType.indiFatherAge,         true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.indiFather,    FieldType.indiFatherDead,        true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.indiFather,    FieldType.indiFatherOccupation,  true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.indiFather,    FieldType.indiFatherResidence,   true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.indiFather,    FieldType.indiFatherAddress,     true,  false,  true,  false,  true,  false,  true,  false);
	init(GroupId.indiFather,    FieldType.indiFatherComment,     true,  true,   true,  true,   true,  true,   true,  true );

	init(GroupId.indiMother,    FieldType.indiMotherLastName,    true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.indiMother,    FieldType.indiMotherFirstName,   true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.indiMother,    FieldType.indiMotherAge,         true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.indiMother,    FieldType.indiMotherDead,        false, false,  true,  true,   true,  true,   true,  true );
	init(GroupId.indiMother,    FieldType.indiMotherOccupation,  true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.indiMother,    FieldType.indiMotherResidence,   true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.indiMother,    FieldType.indiMotherAddress,     true,  false,  true,  false,  true,  false,  true,  false);
	init(GroupId.indiMother,    FieldType.indiMotherComment,     true,  true,   true,  true,   true,  true,   true,  true );

	init(GroupId.wife,          FieldType.wifeLastName,          false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wife,          FieldType.wifeFirstName,         false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wife,          FieldType.wifeSex,               false, false,  false, false,  false, false,  true,  true );
	init(GroupId.wife,          FieldType.wifeAge,               false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wife,          FieldType.wifeBirthDate,         false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wife,          FieldType.wifeBirthPlace,        false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wife,          FieldType.wifeBirthAddress,      false, false,  true,  false,  true,  false,  true,  false);
	init(GroupId.wife,          FieldType.wifeOccupation,        false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wife,          FieldType.wifeResidence,         false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wife,          FieldType.wifeAddress,           false, false,  true,  false,  true,  false,  true,  false);
	init(GroupId.wife,          FieldType.wifeComment,           false, false,  true,  true,   false, false,  true,  true );

	init(GroupId.wifeMarried,   FieldType.wifeMarriedLastName,   false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wifeMarried,   FieldType.wifeMarriedFirstName,  false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wifeMarried,   FieldType.wifeMarriedDead,       false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wifeMarried,   FieldType.wifeMarriedOccupation, false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wifeMarried,   FieldType.wifeMarriedResidence,  false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wifeMarried,   FieldType.wifeMarriedAddress,    false, false,  true,  false,  true,  false,  true,  false);
	init(GroupId.wifeMarried,   FieldType.wifeMarriedComment,    false, false,  true,  true,   false, false,  true,  true );

	init(GroupId.wifeFather,    FieldType.wifeFatherLastName,    false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wifeFather,    FieldType.wifeFatherFirstName,   false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wifeFather,    FieldType.wifeFatherAge,         false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wifeFather,    FieldType.wifeFatherDead,        false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wifeFather,    FieldType.wifeFatherOccupation,  false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wifeFather,    FieldType.wifeFatherResidence,   false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wifeFather,    FieldType.wifeFatherAddress,     false, false,  true,  false,  true,  false,  true,  false);
	init(GroupId.wifeFather,    FieldType.wifeFatherComment,     false, false,  true,  true,   false, false,  true,  true );

	init(GroupId.wifeMother,    FieldType.wifeMotherLastName,    false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wifeMother,    FieldType.wifeMotherFirstName,   false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wifeMother,    FieldType.wifeMotherAge,         false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wifeMother,    FieldType.wifeMotherDead,        false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wifeMother,    FieldType.wifeMotherOccupation,  false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wifeMother,    FieldType.wifeMotherResidence,   false, false,  true,  true,   false, false,  true,  true );
	init(GroupId.wifeMother,    FieldType.wifeMotherAddress,     false, false,  true,  false,  true,  false,  true,  false);
	init(GroupId.wifeMother,    FieldType.wifeMotherComment,     false, false,  true,  true,   false, false,  true,  true );

	init(GroupId.witness1,      FieldType.witness1LastName,      true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.witness1,      FieldType.witness1FirstName,     true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.witness1,      FieldType.witness1Occupation,    true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.witness1,      FieldType.witness1Comment,       true,  true,   true,  true,   true,  true,   true,  true );

        init(GroupId.witness2,      FieldType.witness2LastName,      true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.witness2,      FieldType.witness2FirstName,     true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.witness2,      FieldType.witness2Occupation,    true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.witness2,      FieldType.witness2Comment,       true,  true,   true,  true,   true,  true,   true,  true );

        init(GroupId.witness3,      FieldType.witness3LastName,      true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.witness3,      FieldType.witness3FirstName,     true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.witness3,      FieldType.witness3Occupation,    true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.witness3,      FieldType.witness3Comment,       true,  true,   true,  true,   true,  true,   true,  true );

	init(GroupId.witness4,      FieldType.witness4LastName,      true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.witness4,      FieldType.witness4FirstName,     true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.witness4,      FieldType.witness4Occupation,    true,  true,   true,  true,   true,  true,   true,  true );
	init(GroupId.witness4,      FieldType.witness4Comment,       true,  true,   true,  true,   true,  true,   true,  true );

        init(GroupId.generalComment, FieldType.generalComment,        true,  true,   true,  true,   true,  true,   true,  true );

    }

    static void init(GroupId groupId, FieldType fieldType,
            boolean birthUse, boolean birthVisible,
            boolean marriageUse, boolean marriageVisible,
            boolean deathUse, boolean deathVisible,
            boolean miscUse, boolean miscVisible
            ) {
            EditorBeanGroup.addField(RecordType.BIRTH,    groupId, new EditorBeanField( fieldType, birthUse, birthVisible ));
            EditorBeanGroup.addField(RecordType.MARRIAGE, groupId, new EditorBeanField( fieldType, marriageUse, marriageVisible ));
            EditorBeanGroup.addField(RecordType.DEATH,    groupId, new EditorBeanField( fieldType, deathUse, deathVisible ));
            EditorBeanGroup.addField(RecordType.MISC,     groupId, new EditorBeanField( fieldType, miscUse, miscVisible ));

    }

    private final FieldType fieldType ;
    private final boolean used;
    private boolean visible;

    public EditorBeanField(FieldType fieldType, boolean used, boolean visible) {
        this.fieldType = fieldType;
        this.used =used;
        this.visible = visible;
    }

    /**
     * @return the fieldType
     */
    public FieldType getFieldType() {
        return fieldType;
    }

    /**
     * @return the used
     */
    public boolean isUsed() {
        return used;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return getLabel(fieldType);
    }
    
    /**
     * @return the visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @return the visible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }


    /**
     * @return the label
     */
    static public String getLabel(FieldType fieldType) {
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
            case indiBirthPlace:
            case wifeBirthPlace:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.BirthPlace");
                break;

            case indiBirthAddress:
            case wifeBirthAddress:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.BirthAddress");
                break;
    
            case indiResidence:
            case indiMarriedResidence:
            case indiFatherResidence:
            case indiMotherResidence:
            case wifeResidence:
            case wifeMarriedResidence:
            case wifeFatherResidence:
            case wifeMotherResidence:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Residence");
                break;

            case indiAddress:
            case indiMarriedAddress:
            case indiFatherAddress:
            case indiMotherAddress:
            case wifeAddress:
            case wifeMarriedAddress:
            case wifeFatherAddress:
            case wifeMotherAddress:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Address");
                break;

            case indiAge:
            case indiFatherAge:
            case indiMotherAge:
            case wifeAge:
            case wifeFatherAge:
            case wifeMotherAge:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Age");
                break;
            case indiBirthDate:
            case wifeBirthDate:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Birth");
                break;

            case eventDate:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Date");
                break;
            case secondDate:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.secondDate");
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
            default:
                label = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Value");
                break;
        }

        return label;
    }


}
