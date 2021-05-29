package ancestris.modules.releve.model;

/**
 *
 * @author Michel
 */
public abstract class Field implements Comparable<Field>{
    
    /**
     * @return the label
     */
    public abstract String getValue();

    /**
     * @return the label
     */
    public abstract void setValue( String value);

    /**
     * @return the label
     */
    public abstract boolean isEmpty();

    /**
     * Compares this field to another field
     * @return  a negative integer, zero, or a positive integer as this object
     *      is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Field that) {
        return this.toString().compareTo(that.toString());
    }

    @Override
    public boolean equals(Object that) {
        if (this.getClass().equals(that.getClass())) {
            return this.toString().equals(that.toString());
        } else {
            return false;
        }
    }
    
    public boolean equalsProperty(Object that) {
        if (this.getClass().equals(that.getClass())) {
            return this.toString().equals(that.toString());
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    

}
