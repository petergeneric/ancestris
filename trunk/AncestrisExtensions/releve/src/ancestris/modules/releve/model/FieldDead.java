package ancestris.modules.releve.model;

import java.util.Locale;

/**
 *
 * @author Michel
 */
public class FieldDead extends Field {

    public enum DeadState { unknown, dead, alive }
    private DeadState value = DeadState.unknown;
    public static String deadLabel = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Dead");
    public static String aliveLabel = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Alive");
    public static String unknownLabel = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Unknown");

    public DeadState getState() {
        return value;
    }

    public void setState(DeadState state) {
        value = state;
    }

    @Override
    public String getValue() {
        switch (value) {
            case dead  : return DeadState.dead.name();
            case alive : return DeadState.alive.name();
            default: return "";
        }
    }

    @Override
    public void setValue(Object stringValue) {
        String inputValue = stringValue.toString().toLowerCase();
        if ( "true".equalsIgnoreCase( inputValue)
                || inputValue.indexOf(deadLabel.toLowerCase()) != -1
                || inputValue.indexOf("dead") != -1
                || inputValue.indexOf("dcd") != -1
                || inputValue.indexOf("feu") != -1
                ) {
             value = DeadState.dead;
        } else if (inputValue.indexOf(aliveLabel.toLowerCase()) != -1
                || "alive".equalsIgnoreCase(stringValue.toString())) {
             value = DeadState.alive;
        } else {
            value = DeadState.unknown;
        }
    }


    @Override
    public String toString() {
        switch (value) {
            case dead  : return deadLabel;
            case alive : return aliveLabel;
            default: return "";
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public FieldDead clone() {
        return (FieldDead) super.clone();
    }

}
