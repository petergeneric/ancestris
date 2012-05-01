package ancestris.modules.releve.model;



/**
 *
 * @author Michel
 */
public class FieldDead extends Field {

    private boolean value;

    public boolean getState() {
        return value;
    }

    public void setState(boolean state) {
        value = state;
    }

    @Override
    public String getValue() {
        return String.valueOf(value);
    }

    @Override
    public void setValue(Object value) {
        this.value = Boolean.getBoolean(value.toString());
    }


    @Override
    public String toString() {
        if (value) {
            return "décédé";
        } else {
            return "";
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    
}
