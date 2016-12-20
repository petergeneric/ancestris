package ancestris.modules.releve.model;

import genj.gedcom.time.Delta;

/**
 *
 * @author Michel
 */
public class FieldAge extends Field {

    private Delta delta = new Delta(0,0,0);

    @Override
    public FieldAge clone() {
        FieldAge object = null;
        object = new FieldAge();
        object.setValue(delta);
		// je renvoie le clone
		return object;
  	}

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

    @Override
    public void setValue(Object value) {
        delta.setValue(value.toString());
    }

    public void setValue(Delta other) {
        delta.setValue(other);
    }

    @Override
    public boolean isEmpty() {
        return delta.getYears()==0 && delta.getMonths()==0 && delta.getDays()==0;
    }
}