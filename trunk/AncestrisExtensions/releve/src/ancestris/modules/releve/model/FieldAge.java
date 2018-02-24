package ancestris.modules.releve.model;

import genj.gedcom.time.Delta;

/**
 *
 * @author Michel
 */
public class FieldAge extends Field {

    private final Delta delta = new Delta(0,0,0);

    @Override
    public String toString() {
        if ( isEmpty() ) {
            return "";
        } else {
            return delta.toString();
        }
    }

    @Override
    public String getValue() {
        return delta.getValue();
    }


    public Delta getDelta() {
        return delta;
    }

    /**
     * format <year>y <month>m <day>d
     * @param value 
     */
    @Override
    public void setValue(String value) {
        delta.setValue(value);
    }

    public void setValue(Delta other) {
        delta.setValue(other);
    }

    @Override
    public boolean isEmpty() {
        return delta.getYears()==0 && delta.getMonths()==0 && delta.getDays()==0;
    }
}