package ancestris.modules.releve.model;

/**
 *
 * @author Michel
 */
public class RecordBirth extends Record {

    
    public RecordBirth() {
        super();
        
        indiFirstName       = new FieldSimpleValue();
        indiLastName        = new FieldSimpleValue();
        indiSex             = new FieldSex();
        indiBirthDate       = new FieldDate();
        indiComment         = new FieldComment();

        indiFatherFirstName = new FieldSimpleValue();
        indiFatherLastName  = new FieldSimpleValue();
        indiFatherAge       = new FieldAge();
        indiFatherDead      = new FieldDead();
        indiFatherOccupation= new FieldOccupation();
        indiFatherComment   = new FieldComment();

        indiMotherFirstName = new FieldSimpleValue();
        indiMotherLastName  = new FieldSimpleValue();
        indiMotherAge       = new FieldAge();
        indiMotherDead      = new FieldDead();
        indiMotherOccupation= new FieldOccupation();
        indiMotherComment   = new FieldComment();
        
        witness1FirstName   = new FieldSimpleValue();
        witness1LastName    = new FieldSimpleValue();
        witness1Occupation  = new FieldOccupation();
        witness1Comment     = new FieldComment();
        witness2FirstName   = new FieldSimpleValue();
        witness2LastName    = new FieldSimpleValue();
        witness2Occupation  = new FieldOccupation();
        witness2Comment     = new FieldComment();
        witness3FirstName   = new FieldSimpleValue();
        witness3LastName    = new FieldSimpleValue();
        witness3Occupation  = new FieldOccupation();
        witness3Comment     = new FieldComment();
        witness4FirstName   = new FieldSimpleValue();
        witness4LastName    = new FieldSimpleValue();
        witness4Occupation  = new FieldOccupation();
        witness4Comment     = new FieldComment();
    }

    @Override
    public RecordBirth clone() {
	    RecordBirth object = (RecordBirth) super.clone();

        object.indiFirstName       = indiFirstName.clone();
        object.indiLastName        = indiLastName.clone();
        object.indiSex             = indiSex.clone();
        object.indiBirthDate       = indiBirthDate.clone();
        object.indiComment         = indiComment.clone();

        object.indiFatherFirstName = indiFatherFirstName.clone();
        object.indiFatherLastName  = indiFatherLastName.clone();
        object.indiFatherAge       = indiFatherAge.clone();
        object.indiFatherDead      = indiFatherDead.clone();
        object.indiFatherOccupation= indiFatherOccupation.clone();
        object.indiFatherComment   = indiFatherComment.clone();

        object.indiMotherFirstName = indiMotherFirstName.clone();
        object.indiMotherLastName  = indiMotherLastName.clone();
        object.indiMotherAge       = indiMotherAge.clone();
        object.indiMotherDead      = indiMotherDead.clone();
        object.indiMotherOccupation= indiMotherOccupation.clone();
        object.indiMotherComment   = indiMotherComment.clone();

        object.witness1FirstName   = witness1FirstName.clone();
        object.witness1LastName    = witness1LastName.clone();
        object.witness1Occupation  = witness1Occupation.clone();
        object.witness1Comment     = witness1Comment.clone();
        object.witness2FirstName   = witness2FirstName.clone();
        object.witness2LastName    = witness2LastName.clone();
        object.witness2Occupation  = witness2Occupation.clone();
        object.witness2Comment     = witness2Comment.clone();
        object.witness3FirstName   = witness3FirstName.clone();
        object.witness3LastName    = witness3LastName.clone();
        object.witness3Occupation  = witness3Occupation.clone();
        object.witness3Comment     = witness3Comment.clone();
        object.witness4FirstName   = witness4FirstName.clone();
        object.witness4LastName    = witness4LastName.clone();
        object.witness4Occupation  = witness4Occupation.clone();
        object.witness4Comment     = witness4Comment.clone();
		// je renvoie le clone
		return object;
  	}
}
