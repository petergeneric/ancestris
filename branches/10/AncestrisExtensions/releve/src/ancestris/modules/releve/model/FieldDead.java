package ancestris.modules.releve.model;

/**
 *
 * @author Michel
 */
public class FieldDead extends Field {

    public static enum DeadState { NOT_SPECIFIED, DEAD, ALIVE }
    private DeadState value = DeadState.NOT_SPECIFIED;
    public static String deadLabel = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Dead");
    public static String aliveLabel = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.Alive");
    public static String notspecifiedLabel = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.label.NotSpecified");

    public DeadState getState() {
        return value;
    }

    public void setState(DeadState state) {
        value = state;
    }

    @Override
    public String getValue() {
        switch (value) {
            case DEAD  : return DeadState.DEAD.name();
            case ALIVE : return DeadState.ALIVE.name();
            default: return "";
        }
    }

    @Override
    public void setValue(String stringValue) {
        String inputValue = stringValue.toUpperCase();
        if ( DeadState.DEAD.name().equals(inputValue)
                ||"TRUE".equals( inputValue)
                || inputValue.contains(deadLabel.toUpperCase())
                || inputValue.contains("DCD")
                || inputValue.contains("FEU")
                ) {
             value = DeadState.DEAD;
        } else if (DeadState.ALIVE.name().equals(inputValue)
                || "FALSE".equals( inputValue) 
                || inputValue.contains(aliveLabel.toUpperCase())
                ) {
             value = DeadState.ALIVE;
        } else {
            value = DeadState.NOT_SPECIFIED;
        }
    }


    @Override
    public String toString() {
        switch (value) {
            case DEAD  : return deadLabel;
            case ALIVE : return aliveLabel;
            default: return "";
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
