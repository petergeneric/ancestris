package ancestris.modules.releve.model;

import ancestris.modules.releve.model.DataManager.RecordType;

/**
 *
 * @author Michel
 */
public class RecordMisc extends Record {

    public RecordMisc() {
        super();

        eventType           = new FieldEventType();
        notary              = new FieldNotary();
        
        indi.firstName       = new FieldSimpleValue();
        indi.lastName        = new FieldSimpleValue();
        indi.sex             = new FieldSex();
        indi.age             = new FieldAge();
        indi.birthDate       = new FieldDate();
        indi.birthPlace      = new FieldPlace();
        indi.birthAddress    = new FieldAddress();
        indi.occupation      = new FieldOccupation();
        indi.residence       = new FieldPlace();
        indi.address         = new FieldAddress();
        indi.comment         = new FieldComment();

        indi.marriedFirstName= new FieldSimpleValue();
        indi.marriedLastName = new FieldSimpleValue();
        //indi.marriedSex       = new FieldSex();
        indi.marriedDead      = new FieldDead();
        indi.marriedOccupation= new FieldOccupation();
        indi.marriedResidence = new FieldPlace();
        indi.marriedAddress   = new FieldAddress();
        indi.marriedComment   = new FieldComment();
        
        indi.fatherFirstName = new FieldSimpleValue();
        indi.fatherLastName  = new FieldSimpleValue();
        indi.fatherAge       = new FieldAge();
        indi.fatherDead      = new FieldDead();
        indi.fatherOccupation= new FieldOccupation();
        indi.fatherResidence = new FieldPlace();
        indi.fatherAddress   = new FieldAddress();
        indi.fatherComment   = new FieldComment();
        
        indi.motherFirstName = new FieldSimpleValue();
        indi.motherLastName  = new FieldSimpleValue();
        indi.motherAge       = new FieldAge();
        indi.motherDead      = new FieldDead();
        indi.motherOccupation= new FieldOccupation();
        indi.motherResidence = new FieldPlace();
        indi.motherAddress   = new FieldAddress();
        indi.motherComment   = new FieldComment();
        
        wife.firstName       = new FieldSimpleValue();
        wife.lastName        = new FieldSimpleValue();
        wife.sex             = new FieldSex();
        wife.birthPlace      = new FieldPlace();
        wife.birthAddress    = new FieldAddress();
        wife.birthDate       = new FieldDate();
        wife.age             = new FieldAge();
        wife.occupation      = new FieldOccupation();
        wife.residence       = new FieldPlace();
        wife.comment         = new FieldComment();
        
        wife.marriedFirstName= new FieldSimpleValue();
        wife.marriedLastName = new FieldSimpleValue();
        //wife.marriedSex      = new FieldSex();
        wife.marriedDead     = new FieldDead();
        wife.marriedOccupation= new FieldOccupation();
        wife.marriedResidence = new FieldPlace();
        wife.marriedAddress   = new FieldAddress();
        wife.marriedComment  = new FieldComment();

        wife.fatherFirstName = new FieldSimpleValue();
        wife.fatherLastName  = new FieldSimpleValue();
        wife.fatherAge       = new FieldAge();
        wife.fatherDead      = new FieldDead();
        wife.fatherOccupation= new FieldOccupation();
        wife.fatherResidence = new FieldPlace();
        wife.fatherAddress   = new FieldAddress();
        wife.fatherComment   = new FieldComment();

        wife.motherFirstName = new FieldSimpleValue();
        wife.motherLastName  = new FieldSimpleValue();
        wife.motherAge       = new FieldAge();
        wife.motherDead      = new FieldDead();
        wife.motherOccupation= new FieldOccupation();
        wife.motherResidence = new FieldPlace();
        wife.motherAddress   = new FieldAddress();
        wife.motherComment   = new FieldComment();
        
        for(Witness witness : witnesses) {
            witness.firstName  = new FieldSimpleValue();
            witness.lastName   = new FieldSimpleValue();
            witness.occupation = new FieldOccupation();
            witness.comment    = new FieldComment();
        }
    }

    @Override
    public RecordType getType() {
        return RecordType.misc;
    }

    @Override
    public RecordMisc clone() throws CloneNotSupportedException {
        return (RecordMisc) super.clone();
    }
}
