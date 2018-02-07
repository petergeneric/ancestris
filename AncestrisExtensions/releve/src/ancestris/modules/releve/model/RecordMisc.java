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
        getWife().birthPlace      = new FieldPlace();
        getWife().birthDate       = new FieldDate();
        getWife().age             = new FieldAge();
        getWife().occupation      = new FieldOccupation();
        getWife().residence       = new FieldPlace();
        getWife().comment         = new FieldComment();
        
        getWife().marriedFirstName= new FieldSimpleValue();
        getWife().marriedLastName = new FieldSimpleValue();
        //getWife().marriedSex      = new FieldSex();
        getWife().marriedDead     = new FieldDead();
        getWife().marriedOccupation= new FieldOccupation();
        getWife().marriedResidence = new FieldPlace();
        getWife().marriedComment  = new FieldComment();

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
        return RecordType.misc;
    }

    @Override
    public RecordMisc clone() throws CloneNotSupportedException {
        return (RecordMisc) super.clone();
    }
}
