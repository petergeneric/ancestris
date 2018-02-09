package ancestris.modules.releve.model;

import ancestris.modules.releve.model.DataManager.RecordType;

/**
 *
 * @author Michel
 */
public class RecordBirth extends Record {

    
    public RecordBirth() {
        super();
        
        indi.firstName       = new FieldSimpleValue();
        indi.lastName        = new FieldSimpleValue();
        indi.sex             = new FieldSex();
        indi.birthDate       = new FieldDate();
        indi.birthPlace      = new FieldPlace();
        indi.birthAddress    = new FieldAddress();
        indi.comment         = new FieldComment();

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
        
        for(Witness witness : witnesses) {
            witness.firstName  = new FieldSimpleValue();
            witness.lastName   = new FieldSimpleValue();
            witness.occupation = new FieldOccupation();
            witness.comment    = new FieldComment();
        }

    }

    @Override
    public RecordType getType() {
        return RecordType.birth;
    }

    @Override
    public RecordBirth clone() throws CloneNotSupportedException {
        return (RecordBirth) super.clone();
    }
}
