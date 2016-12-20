package ancestris.modules.releve.model;

import ancestris.modules.releve.model.DataManager.RecordType;

/**
 *
 * @author Michel
 */
public class RecordMarriage extends Record implements Cloneable{

    public RecordMarriage() {
        super();
       
        getIndi().firstName       = new FieldSimpleValue();
        getIndi().lastName        = new FieldSimpleValue();
        getIndi().sex             = new FieldSex();
        getIndi().age             = new FieldAge();
        getIndi().birthDate       = new FieldDate();
        getIndi().birthPlace      = new FieldPlace();
        getIndi().occupation      = new FieldOccupation();
        getIndi().residence       = new FieldPlace();
        getIndi().comment         = new FieldComment();

        getIndi().marriedFirstName= new FieldSimpleValue();
        getIndi().marriedLastName = new FieldSimpleValue();
        //getIndi().marriedSex       = new FieldSex();
        getIndi().marriedDead      = new FieldDead();
        getIndi().marriedOccupation= new FieldOccupation();
        getIndi().marriedResidence = new FieldPlace();
        getIndi().marriedComment   = new FieldComment();
        
        getIndi().fatherFirstName = new FieldSimpleValue();
        getIndi().fatherLastName  = new FieldSimpleValue();
        getIndi().fatherAge       = new FieldAge();
        getIndi().fatherDead      = new FieldDead();
        getIndi().fatherOccupation= new FieldOccupation();
        getIndi().fatherResidence = new FieldPlace();
        getIndi().fatherComment   = new FieldComment();
        
        getIndi().motherFirstName = new FieldSimpleValue();
        getIndi().motherLastName  = new FieldSimpleValue();
        getIndi().motherAge       = new FieldAge();
        getIndi().motherDead      = new FieldDead();
        getIndi().motherOccupation= new FieldOccupation();
        getIndi().motherResidence = new FieldPlace();
        getIndi().motherComment   = new FieldComment();
        
        getWife().firstName       = new FieldSimpleValue();
        getWife().lastName        = new FieldSimpleValue();
        getWife().sex             = new FieldSex();
        getWife().age             = new FieldAge();
        getWife().birthDate       = new FieldDate();
        getWife().birthPlace      = new FieldPlace();
        getWife().occupation      = new FieldOccupation();
        getWife().residence       = new FieldPlace();
        getWife().comment         = new FieldComment();

        getWife().marriedFirstName= new FieldSimpleValue();
        getWife().marriedLastName = new FieldSimpleValue();
        //getWife().marriedSex       = new FieldSex();
        getWife().marriedDead      = new FieldDead();
        getWife().marriedOccupation= new FieldOccupation();
        getWife().marriedResidence = new FieldPlace();
        getWife().marriedComment   = new FieldComment();
        
        getWife().fatherFirstName = new FieldSimpleValue();
        getWife().fatherLastName  = new FieldSimpleValue();
        getWife().fatherAge       = new FieldAge();
        getWife().fatherDead      = new FieldDead();
        getWife().fatherOccupation= new FieldOccupation();
        getWife().fatherResidence = new FieldPlace();
        getWife().fatherComment   = new FieldComment();
        
        getWife().motherFirstName = new FieldSimpleValue();
        getWife().motherLastName  = new FieldSimpleValue();
        getWife().motherAge       = new FieldAge();
        getWife().motherDead      = new FieldDead();
        getWife().motherOccupation= new FieldOccupation();
        getWife().motherResidence = new FieldPlace();
        getWife().motherComment   = new FieldComment();
        
        witness1.firstName   = new FieldSimpleValue();
        witness1.lastName    = new FieldSimpleValue();
        witness1.occupation  = new FieldOccupation();
        witness1.comment     = new FieldComment();
        witness2.firstName   = new FieldSimpleValue();
        witness2.lastName    = new FieldSimpleValue();
        witness2.occupation  = new FieldOccupation();
        witness2.comment     = new FieldComment();
        witness3.firstName   = new FieldSimpleValue();
        witness3.lastName    = new FieldSimpleValue();
        witness3.occupation  = new FieldOccupation();
        witness3.comment     = new FieldComment();
        witness4.firstName   = new FieldSimpleValue();
        witness4.lastName    = new FieldSimpleValue();
        witness4.occupation  = new FieldOccupation();
        witness4.comment     = new FieldComment();
    }

    @Override
    public RecordType getType() {
        return RecordType.marriage;
    }

    @Override
    public RecordMarriage clone() {
	RecordMarriage object = (RecordMarriage) super.clone();
        
//        object.indi.firstName       = indi.firstName.clone();
//        object.indi.lastName        = indi.lastName.clone();
//        object.indi.sex             = indi.sex.clone();
//        object.indi.age             = indi.age.clone();
//        object.indi.birthDate       = indi.birthDate.clone();
//        object.indi.birthPlace      = indi.birthPlace.clone();
//        object.indi.occupation      = indi.occupation.clone();
//        object.indi.residence       = indi.residence.clone();
//        object.indi.comment         = indi.comment.clone();
//
//        object.indi.marriedFirstName= indi.marriedFirstName.clone();
//        object.indi.marriedLastName = indi.marriedLastName.clone();
//        object.indi.marriedDead      = indi.marriedDead.clone();
//        object.indi.marriedOccupation= indi.marriedOccupation.clone();
//        object.indi.marriedResidence = indi.marriedResidence.clone();
//        object.indi.marriedComment   = indi.marriedComment.clone();
//
//        object.indi.fatherFirstName = indi.fatherFirstName.clone();
//        object.indi.fatherLastName  = indi.fatherLastName.clone();
//        object.indi.fatherAge       = indi.fatherAge.clone();
//        object.indi.fatherDead      = indi.fatherDead.clone();
//        object.indi.fatherOccupation= indi.fatherOccupation.clone();
//        object.indi.fatherResidence = indi.fatherResidence.clone();
//        object.indi.fatherComment   = indi.fatherComment.clone();
//
//        object.indi.motherFirstName = indi.motherFirstName.clone();
//        object.indi.motherLastName  = indi.motherLastName.clone();
//        object.indi.motherAge       = indi.motherAge.clone();
//        object.indi.motherDead      = indi.motherDead.clone();
//        object.indi.motherOccupation= indi.motherOccupation.clone();
//        object.indi.motherResidence = indi.motherResidence.clone();
//        object.indi.motherComment   = indi.motherComment.clone();
//
//        object.wife.firstName       = wife.firstName.clone();
//        object.wife.lastName        = wife.lastName.clone();
//        object.wife.sex             = wife.sex.clone();
//        object.wife.age             = wife.age.clone();
//        object.wife.birthDate       = wife.birthDate.clone();
//        object.wife.birthPlace      = wife.birthPlace.clone();
//        object.wife.occupation      = wife.occupation.clone();
//        object.wife.residence       = wife.residence.clone();
//        object.wife.comment         = wife.comment.clone();
//
//        object.wife.marriedFirstName= wife.marriedFirstName.clone();
//        object.wife.marriedLastName = wife.marriedLastName.clone();
//        object.wife.marriedDead      = wife.marriedDead.clone();
//        object.wife.marriedOccupation= wife.marriedOccupation.clone();
//        object.wife.marriedResidence = wife.marriedResidence.clone();
//        object.wife.marriedComment   = wife.marriedComment.clone();
//
//        object.wife.fatherFirstName = wife.fatherFirstName.clone();
//        object.wife.fatherLastName  = wife.fatherLastName.clone();
//        object.wife.fatherAge       = wife.fatherAge.clone();
//        object.wife.fatherDead      = wife.fatherDead.clone();
//        object.wife.fatherOccupation= wife.fatherOccupation.clone();
//        object.wife.fatherResidence = wife.fatherResidence.clone();
//        object.wife.fatherComment   = wife.fatherComment.clone();
//
//        object.wife.motherFirstName = wife.motherFirstName.clone();
//        object.wife.motherLastName  = wife.motherLastName.clone();
//        object.wife.motherAge       = wife.motherAge.clone();
//        object.wife.motherDead      = wife.motherDead.clone();
//        object.wife.motherOccupation= wife.motherOccupation.clone();
//        object.wife.motherResidence = wife.motherResidence.clone();
//        object.wife.motherComment   = wife.motherComment.clone();
//
//        object.witness1FirstName   = witness1FirstName.clone();
//        object.witness1LastName    = witness1LastName.clone();
//        object.witness1Occupation  = witness1Occupation.clone();
//        object.witness1Comment     = witness1Comment.clone();
//        object.witness2FirstName   = witness2FirstName.clone();
//        object.witness2LastName    = witness2LastName.clone();
//        object.witness2Occupation  = witness2Occupation.clone();
//        object.witness2Comment     = witness2Comment.clone();
//        object.witness3FirstName   = witness3FirstName.clone();
//        object.witness3LastName    = witness3LastName.clone();
//        object.witness3Occupation  = witness3Occupation.clone();
//        object.witness3Comment     = witness3Comment.clone();
//        object.witness4FirstName   = witness4FirstName.clone();
//        object.witness4LastName    = witness4LastName.clone();
//        object.witness4Occupation  = witness4Occupation.clone();
//        object.witness4Comment     = witness4Comment.clone();
		// je renvoie le clone
		return object;
  	}


}
