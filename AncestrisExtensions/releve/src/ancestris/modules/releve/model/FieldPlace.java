package ancestris.modules.releve.model;

/**
 *
 * @author Michel
 */
public class FieldPlace extends Field {

    private String place = "";

    @Override
    public FieldPlace clone() {
        return (FieldPlace) super.clone();
    }

    @Override
    public String getValue() {
        if (isEmpty()) {
            return "";
        } else {
            return place;
        }
    }

    @Override
    public void setValue(Object value) {
        place = value.toString();
    }

    @Override
    public String toString() {
        return getValue();
    }

    @Override
    public boolean isEmpty() {
        return place.isEmpty();
    }

    public String getDisplayValue() {
        return place;
    }
}
