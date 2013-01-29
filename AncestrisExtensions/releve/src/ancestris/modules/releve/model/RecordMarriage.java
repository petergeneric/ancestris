package ancestris.modules.releve.model;

import ancestris.modules.releve.model.DataManager.RecordType;

/**
 *
 * @author Michel
 */
public class RecordMarriage extends Record implements Cloneable{

    public RecordMarriage() {
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
        //indiMarriedSex       = new FieldSex();
        indiMarriedDead      = new FieldDead();
        indiMarriedOccupation= new FieldOccupation();
        indiMarriedResidence = new FieldPlace();
        indiMarriedComment   = new FieldComment();
        
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
        
        wifeFirstName       = new FieldSimpleValue();
        wifeLastName        = new FieldSimpleValue();
        wifeSex             = new FieldSex();
        wifeAge             = new FieldAge();
        wifeBirthDate       = new FieldDate();
        wifeBirthPlace      = new FieldPlace();
        wifeOccupation      = new FieldOccupation();
        wifeResidence       = new FieldPlace();
        wifeComment         = new FieldComment();

        wifeMarriedFirstName= new FieldSimpleValue();
        wifeMarriedLastName = new FieldSimpleValue();
        //wifeMarriedSex       = new FieldSex();
        wifeMarriedDead      = new FieldDead();
        wifeMarriedOccupation= new FieldOccupation();
        wifeMarriedResidence = new FieldPlace();
        wifeMarriedComment   = new FieldComment();
        
        wifeFatherFirstName = new FieldSimpleValue();
        wifeFatherLastName  = new FieldSimpleValue();
        wifeFatherAge       = new FieldAge();
        wifeFatherDead      = new FieldDead();
        wifeFatherOccupation= new FieldOccupation();
        wifeFatherResidence = new FieldPlace();
        wifeFatherComment   = new FieldComment();
        
        wifeMotherFirstName = new FieldSimpleValue();
        wifeMotherLastName  = new FieldSimpleValue();
        wifeMotherAge       = new FieldAge();
        wifeMotherDead      = new FieldDead();
        wifeMotherOccupation= new FieldOccupation();
        wifeMotherResidence = new FieldPlace();
        wifeMotherComment   = new FieldComment();
        
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
        return RecordType.marriage;
    }

    @Override
    public RecordMarriage clone() {
	RecordMarriage object = (RecordMarriage) super.clone();
        
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

        object.wifeFirstName       = wifeFirstName.clone();
        object.wifeLastName        = wifeLastName.clone();
        object.wifeSex             = wifeSex.clone();
        object.wifeAge             = wifeAge.clone();
        object.wifeBirthDate       = wifeBirthDate.clone();
        object.wifeBirthPlace      = wifeBirthPlace.clone();
        object.wifeOccupation      = wifeOccupation.clone();
        object.wifeResidence       = wifeResidence.clone();
        object.wifeComment         = wifeComment.clone();

        object.wifeMarriedFirstName= wifeMarriedFirstName.clone();
        object.wifeMarriedLastName = wifeMarriedLastName.clone();
        object.wifeMarriedDead      = wifeMarriedDead.clone();
        object.wifeMarriedOccupation= wifeMarriedOccupation.clone();
        object.wifeMarriedResidence = wifeMarriedResidence.clone();
        object.wifeMarriedComment   = wifeMarriedComment.clone();

        object.wifeFatherFirstName = wifeFatherFirstName.clone();
        object.wifeFatherLastName  = wifeFatherLastName.clone();
        object.wifeFatherAge       = wifeFatherAge.clone();
        object.wifeFatherDead      = wifeFatherDead.clone();
        object.wifeFatherOccupation= wifeFatherOccupation.clone();
        object.wifeFatherResidence = wifeFatherResidence.clone();
        object.wifeFatherComment   = wifeFatherComment.clone();

        object.wifeMotherFirstName = wifeMotherFirstName.clone();
        object.wifeMotherLastName  = wifeMotherLastName.clone();
        object.wifeMotherAge       = wifeMotherAge.clone();
        object.wifeMotherDead      = wifeMotherDead.clone();
        object.wifeMotherOccupation= wifeMotherOccupation.clone();
        object.wifeMotherResidence = wifeMotherResidence.clone();
        object.wifeMotherComment   = wifeMotherComment.clone();

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
