package ancestris.modules.releve.model;

/**
 *
 * @author Michel
 */
public abstract class Field implements Comparable<Field> {

    


    /**
     * @return the display value
     */
    @Override
    public abstract String toString();
    
    /**
     * @return the label
     */
    public abstract Object getValue();

    /**
     * @return the label
     */
    public abstract void setValue( Object value);

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

    public static enum FieldType {
        title,
        eventType,
        cityCode,
        cityName,
        countyCode,
        countyName,
        cote,
        freeComment,
        generalComment,
        eventDate,
        notary,
        parish,
        //  indi ///////////////////////////////////////////////////////////////////
        indiFirstName,
        indiLastName,
        indiSex,
        indiAge,
        indiBirthDate,
        indiPlace,
        indiOccupation,
        indiComment,
        //  conjoint (ou ancien conjoint) //////////////////////////////////////////
        indiMarriedFirstName,
        indiMarriedLastName,
        //indiMarriedSex,
        indiMarriedDead,
        indiMarriedOccupation,
        indiMarriedComment,
        //  indi father ////////////////////////////////////////////////////////////
        indiFatherFirstName,
        indiFatherLastName,
        indiFatherDead,
        indiFatherOccupation,
        indiFatherComment,
        indiMotherFirstName,
        indiMotherLastName,
        indiMotherDead,
        indiMotherOccupation,
        indiMotherComment,
        //  wife ///////////////////////////////////////////////////////////////////
        wifeFirstName,
        wifeLastName,
        wifeSex,
        //wifeDead,
        wifeAge,
        wifeBirthDate,
        wifePlace,
        wifeOccupation,
        wifeComment,
        //  wifeMarried ///////////////////////////////////////////////////////////
        wifeMarriedFirstName,
        wifeMarriedLastName,
        //wifeMarriedSex,
        wifeMarriedDead,
        wifeMarriedOccupation,
        wifeMarriedComment,
        //  wifeFather ///////////////////////////////////////////////////////////
        wifeFatherFirstName,
        wifeFatherLastName,
        wifeFatherDead,
        wifeFatherOccupation,
        wifeFatherComment,
        wifeMotherFirstName,
        wifeMotherLastName,
        wifeMotherDead,
        wifeMotherOccupation,
        wifeMotherComment,
        // wintness ///////////////////////////////////////////////////////////////
        witness1FirstName,
        witness1LastName,
        witness1Occupation,
        witness1Comment,
        witness2FirstName,
        witness2LastName,
        witness2Occupation,
        witness2Comment,
        witness3FirstName,
        witness3LastName,
        witness3Occupation,
        witness3Comment,
        witness4FirstName,
        witness4LastName,
        witness4Occupation,
        witness4Comment
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }
}
