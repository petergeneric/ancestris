package ancestris.modules.releve.model;

/**
 *
 * @author Michel
 */
public class FieldEventType extends Field {

    private String name ="";
    
    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String [] getValue() {
        String[] value= new String[1];
        value[0] = getName();
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.setName(value.toString());
    }

    @Override
    public boolean isEmpty() {
        return name.isEmpty();
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public FieldEventType clone() {
		return (FieldEventType) super.clone();
  	}
}
