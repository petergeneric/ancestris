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
    public RecordBirth clone() throws CloneNotSupportedException {
        return (RecordBirth) super.clone();
    }
}
