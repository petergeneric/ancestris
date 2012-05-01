package ancestris.modules.releve.model;

/**
 *
 * @author Michel
 */
public class FieldSimpleValue extends Field {

    private String value ="";
    @Override
    public String toString() {
        return value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = value.toString();
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }
}
