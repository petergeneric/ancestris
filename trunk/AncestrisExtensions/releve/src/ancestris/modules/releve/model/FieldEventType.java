package ancestris.modules.releve.model;

/**
 *
 * @author Michel
 */
public class FieldEventType extends Field {

    private String name ="";
    private String tag  ="";

    @Override
    public String toString() {
        return getTag()+" "+getName();
    }

    @Override
    public String [] getValue() {
        String[] value= new String[2];
        value[0] = getTag();
        value[1] = getName();
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.setTag(((String[]) value)[0]);
        this.setName(((String[]) value)[1]);
    }

    @Override
    public boolean isEmpty() {
        return tag.isEmpty() && name.isEmpty();
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

    /**
     * @return the tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * @param tag the tag to set
     */
    public void setTag(String tag) {
        this.tag = tag;
    }
}
