package ancestris.modules.releve.model;

/**
 * Remarque : cette classe herite diretement de Property car il n'est pas possible de la
 * faire heriter de PropertySex a cause du constructeur protected PropertySex(tag)
 * inaccessible depuis ici.
 * @author Michel
 */
public class FieldSex extends Field {

    /** sexes */
    public static final int UNKNOWN = 0;
    public static final int MALE = 1;
    public static final int FEMALE = 2;
    public static final String UNKNOWN_STRING = "";
    public static final String MALE_STRING = "M";
    public static final String FEMALE_STRING = "F";
    private int sex = UNKNOWN;
    private String sexAsString;

    /**
     * Accessor for Sex
     */
    public void setSex(int newSex) {
        String old = getValue();
        sexAsString = null;
        sex = newSex;
    }

    public int getSex() {
        return sex;
    }
    
    public int getOppositeInt() {
        switch (sex) {
            case MALE:
                return FEMALE;
            case FEMALE:
                return MALE;
            default:
                return UNKNOWN;
        }
    }

    public String getOppositeString() {
        switch (sex) {
            case MALE:
                return FEMALE_STRING;
            case FEMALE:
                return MALE_STRING;
            default:
                return UNKNOWN_STRING;
        }
    }
    
    @Override
    public String getValue() {
        if (sexAsString != null) {
            return sexAsString;
        }
        if (sex == MALE) {
            return MALE_STRING;
        }
        if (sex == FEMALE) {
            return FEMALE_STRING;
        }
        return UNKNOWN_STRING;
    }

    @Override
    public void setValue(Object value) {

        String newValue = value.toString();
        // Cannot parse anything longer than 1
        if (newValue.trim().length() > 1) {
            sexAsString = newValue;
        } else {
            // zero length -> unknown
            if (newValue.length() == 0) {
                sexAsString = null;
                sex = UNKNOWN;
            } else {
                // Female or Male ?
                switch (newValue.charAt(0)) {
                    case 'f':
                    case 'F':
                        sex = FEMALE;
                        sexAsString = null;
                        break;
                    case 'm':
                    case 'M':
                        sex = MALE;
                        sexAsString = null;
                        break;
                    case 'u':
                    case 'U':
                        sex = UNKNOWN;
                        sexAsString = null;
                        break;
                    default:
                        sexAsString = newValue;
                        break;
                }
            }
        }
    }

       
    @Override
    public String toString() {
        return getValue();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
