package ancestris.modules.releve.model;

/**
 *
 * @author Michel
 */
public class FieldPlace extends Field {

    private String place = "";

    @Override
    public String getValue() {
       return place;
    }

    @Override
    public void setValue(String value) {
        place = value;
    }

    @Override
    public String toString() {
        return getValue();
    }

    @Override
    public boolean isEmpty() {
        return place.isEmpty();
    }

}
