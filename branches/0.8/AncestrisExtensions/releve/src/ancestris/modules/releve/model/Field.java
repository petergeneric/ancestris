package ancestris.modules.releve.model;

/**
 *
 * @author Michel
 */
public abstract class Field implements Comparable<Field> , Cloneable{

    


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

    @Override
    public Field clone() {
	    Field object = null;
        try {
            object = (Field) super.clone();
        } catch (CloneNotSupportedException cnse) {
            cnse.printStackTrace(System.err);
        }
		// je renvoie le clone
		return object;
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
        secondDate,
        notary,
        parish,    // 13 + 29 + 29 + 16 =  77
        //  indi ///////////////////////////////////////////////////////////////////
        indiFirstName,
        indiLastName,
        indiSex,
        indiAge,
        indiBirthDate,
        indiBirthPlace,
        indiOccupation,
        indiResidence,
        indiComment,
        //  conjoint (ou ancien conjoint) //////////////////////////////////////////
        indiMarriedFirstName,
        indiMarriedLastName,
        //indiMarriedSex,
        indiMarriedDead,
        indiMarriedOccupation,
        indiMarriedResidence,
        indiMarriedComment,
        //  indi father ////////////////////////////////////////////////////////////
        indiFatherFirstName,
        indiFatherLastName,
        indiFatherAge,
        indiFatherDead,
        indiFatherOccupation,
        indiFatherResidence,
        indiFatherComment,
        indiMotherFirstName,
        indiMotherLastName,
        indiMotherAge,
        indiMotherDead,
        indiMotherOccupation,
        indiMotherResidence,
        indiMotherComment,  //29
        //  wife ///////////////////////////////////////////////////////////////////
        wifeFirstName,
        wifeLastName,
        wifeSex,
        //wifeDead,
        wifeAge,
        wifeBirthDate,
        wifePlace,
        wifeOccupation,
        wifeResidence,
        wifeComment,
        //  wifeMarried ///////////////////////////////////////////////////////////
        wifeMarriedFirstName,
        wifeMarriedLastName,
        //wifeMarriedSex,
        wifeMarriedDead,
        wifeMarriedOccupation,
        wifeMarriedResidence,
        wifeMarriedComment,
        //  wifeFather ///////////////////////////////////////////////////////////
        wifeFatherFirstName,
        wifeFatherLastName,
        wifeFatherAge,
        wifeFatherDead,
        wifeFatherOccupation,
        wifeFatherResidence,
        wifeFatherComment,
        wifeMotherFirstName,
        wifeMotherLastName,
        wifeMotherAge,
        wifeMotherDead,
        wifeMotherOccupation,
        wifeMotherResidence,
        wifeMotherComment,  // 29
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
        witness4Comment      // 16
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }
}
