package ancestris.modules.releve.model;

/**
 *
 * @author Michel
 */
public class FieldEventType extends Field {

    private String name ="";
    
    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getValue() {
        return name;
    }

    @Override
    public void setValue(String value) {
        name = value;
    }

    @Override
    public boolean isEmpty() {
        return name.isEmpty();
    }

}
