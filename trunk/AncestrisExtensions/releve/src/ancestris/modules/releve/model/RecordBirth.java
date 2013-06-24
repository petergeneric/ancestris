package ancestris.modules.releve.model;

import ancestris.modules.releve.model.DataManager.RecordType;

/**
 *
 * @author Michel
 */
public class RecordBirth extends Record {

    
    public RecordBirth() {
        super();
        
        getIndi().firstName       = new FieldSimpleValue();
        getIndi().lastName        = new FieldSimpleValue();
        getIndi().sex             = new FieldSex();
        getIndi().birthDate       = new FieldDate();
        getIndi().birthPlace      = new FieldPlace();
        getIndi().comment         = new FieldComment();

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
        return RecordType.birth;
    }

    @Override
    public RecordBirth clone() {
	RecordBirth object = (RecordBirth) super.clone();

//        object.indi.firstName       = indi.firstName.clone();
//        object.indi.lastName        = indi.lastName.clone();
//        object.indi.sex             = indi.sex.clone();
//        object.indi.birthDate       = indi.birthDate.clone();
//        object.indi.birthPlace      = indi.birthPlace.clone();
//        object.indi.comment         = indi.comment.clone();
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
