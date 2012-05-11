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
        if (java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Dead").equals(value.toString())) {
             this.value = true;
        } else {
            this.value = Boolean.getBoolean(value.toString());
        }
    }


    @Override
    public String toString() {
        if (value) {
            return java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Dead");
        } else {
            return "";
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    
}
