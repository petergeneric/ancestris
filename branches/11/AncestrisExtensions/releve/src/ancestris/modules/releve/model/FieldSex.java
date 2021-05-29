package ancestris.modules.releve.model;

import genj.gedcom.PropertySex;

/**
 * Remarque : cette classe herite diretement de Property car il n'est pas possible de la
 * faire heriter de PropertySex a cause du constructeur protected PropertySex(tag)
 * inaccessible depuis ici.
 * @author Michel
 */
public class FieldSex extends Field {

    public static String unknownLabel = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Unknown");
    public static String maleLabel = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Male");
    public static String femaleLabel = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Female");
    
    /** sexes */
    public static final int UNKNOWN = 0;
    public static final int MALE = 1;
    public static final int FEMALE = 2;
    public static final String UNKNOWN_STRING = "";
    public static final String MALE_STRING = "M";
    public static final String FEMALE_STRING = "F";
    private int sex = UNKNOWN;
    
    @Override
    public String getValue() {
        if (sex == MALE) {
            return MALE_STRING;
        }
        if (sex == FEMALE) {
            return FEMALE_STRING;
        }
        return UNKNOWN_STRING;
    }

    @Override
    public void setValue(String value) {

        String newValue = value.toUpperCase();
        if ( maleLabel.toUpperCase().equals(newValue)
             || "M".equals(newValue)
            ) {
            sex = MALE;
        } else  if (femaleLabel.toUpperCase().equals(newValue)
             || "F".equals(newValue)
            ) {
            sex = FEMALE;
        } else {
             sex = UNKNOWN;
        }
    }

    @Override
    public String toString() {
        switch (sex) {
            case MALE:
                return maleLabel;
            case FEMALE:
                return femaleLabel;
            default:
                return unknownLabel;
        }
    }

    @Override
    public boolean equalsProperty(Object that) {
        if (that instanceof PropertySex) {
            PropertySex propertySex = (PropertySex) that;
            return sex == propertySex.getSex();
        } else {
            return false;
        }
    }
    @Override
    public boolean isEmpty() {
        return false;
    }

    static public String convertValue(int intValue) {
        if (intValue == MALE) {
            return MALE_STRING;
        }
        if (intValue == FEMALE) {
            return FEMALE_STRING;
        }
        return UNKNOWN_STRING;
    }
    
    static public String getOppositeString(String inValue) {
        if (MALE_STRING.equals(inValue)) {
            return FEMALE_STRING;
        } else if (FEMALE_STRING.equals(inValue)) {
            return FEMALE_STRING;
        } else {
            return UNKNOWN_STRING;
        }        
    }

}
