package ancestris.modules.releve.model;

import ancestris.modules.releve.model.DataManager.RecordType;

/**
 *
 * @author Michel
 */
public class RecordDeath extends Record {

    
    public RecordDeath() {
        super();
        
        indiFirstName       = new FieldSimpleValue();
        indiLastName        = new FieldSimpleValue();
        indiSex             = new FieldSex();
        indiAge             = new FieldAge();
        indiBirthDate       = new FieldDate();
        indiBirthPlace      = new FieldPlace();
        indiOccupation      = new FieldOccupation();
        indiResidence       = new FieldPlace();
        indiComment         = new FieldComment();

        indiMarriedFirstName= new FieldSimpleValue();
        indiMarriedLastName = new FieldSimpleValue();
        //indiMarriedSex      = new FieldSex();
        indiMarriedDead     = new FieldDead();
        indiMarriedOccupation = new FieldOccupation();
        indiMarriedResidence  = new FieldPlace();
        indiMarriedComment  = new FieldComment();
        
        indiFatherFirstName = new FieldSimpleValue();
        indiFatherLastName  = new FieldSimpleValue();
        indiFatherAge       = new FieldAge();
        indiFatherDead      = new FieldDead();
        indiFatherOccupation= new FieldOccupation();
        indiFatherResidence = new FieldPlace();
        indiFatherComment   = new FieldComment();
        
        indiMotherFirstName = new FieldSimpleValue();
        indiMotherLastName  = new FieldSimpleValue();
        indiMotherAge       = new FieldAge();
        indiMotherDead      = new FieldDead();
        indiMotherOccupation= new FieldOccupation();
        indiMotherResidence = new FieldPlace();
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
    public RecordType getType() {
        return RecordType.death;
    }

    @Override
    public RecordDeath clone() {
	    RecordDeath object = (RecordDeath) super.clone();

        object.indiFirstName       = indiFirstName.clone();
        object.indiLastName        = indiLastName.clone();
        object.indiSex             = indiSex.clone();
        object.indiAge             = indiAge.clone();
        object.indiBirthDate       = indiBirthDate.clone();
        object.indiBirthPlace      = indiBirthPlace.clone();
        object.indiOccupation      = indiOccupation.clone();
        object.indiResidence       = indiResidence.clone();
        object.indiComment         = indiComment.clone();

        object.indiMarriedFirstName= indiMarriedFirstName.clone();
        object.indiMarriedLastName = indiMarriedLastName.clone();
        object.indiMarriedDead      = indiMarriedDead.clone();
        object.indiMarriedOccupation= indiMarriedOccupation.clone();
        object.indiMarriedResidence = indiMarriedResidence.clone();
        object.indiMarriedComment   = indiMarriedComment.clone();

        object.indiFatherFirstName = indiFatherFirstName.clone();
        object.indiFatherLastName  = indiFatherLastName.clone();
        object.indiFatherAge       = indiFatherAge.clone();
        object.indiFatherDead      = indiFatherDead.clone();
        object.indiFatherOccupation= indiFatherOccupation.clone();
        object.indiFatherResidence = indiFatherResidence.clone();
        object.indiFatherComment   = indiFatherComment.clone();

        object.indiMotherFirstName = indiMotherFirstName.clone();
        object.indiMotherLastName  = indiMotherLastName.clone();
        object.indiMotherAge       = indiMotherAge.clone();
        object.indiMotherDead      = indiMotherDead.clone();
        object.indiMotherOccupation= indiMotherOccupation.clone();
        object.indiMotherResidence = indiMotherResidence.clone();
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
