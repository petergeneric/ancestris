package ancestris.modules.releve.model;

/**
 *
 * @author Michel
 */
public class RecordMarriage extends Record {

    public RecordMarriage() {
        super();
       
        indiFirstName        = new FieldSimpleValue();
        indiLastName        = new FieldSimpleValue();
        indiSex             = new FieldSex();
        indiAge             = new FieldSimpleValue();
        indiBirthDate       = new FieldDate();
        indiPlace           = new FieldPlace();
        indiOccupation      = new FieldOccupation();
        indiComment         = new FieldComment();

        indiMarriedFirstName= new FieldSimpleValue();
        indiMarriedLastName = new FieldSimpleValue();
        //indiMarriedSex       = new FieldSex();
        indiMarriedDead      = new FieldDead();
        indiMarriedOccupation= new FieldOccupation();
        indiMarriedComment   = new FieldComment();
        
        indiFatherFirstName = new FieldSimpleValue();
        indiFatherLastName  = new FieldSimpleValue();
        indiFatherDead      = new FieldDead();
        indiFatherOccupation= new FieldOccupation();
        indiFatherComment   = new FieldComment();
        
        indiMotherFirstName = new FieldSimpleValue();
        indiMotherLastName  = new FieldSimpleValue();
        indiMotherDead      = new FieldDead();
        indiMotherOccupation= new FieldOccupation();
        indiMotherComment   = new FieldComment();
        
        wifeFirstName       = new FieldSimpleValue();
        wifeLastName        = new FieldSimpleValue();
        wifeSex             = new FieldSex();
        wifeAge             = new FieldSimpleValue();
        wifeBirthDate       = new FieldDate();
        wifePlace           = new FieldPlace();
        wifeOccupation      = new FieldOccupation();
        wifeComment         = new FieldComment();

        wifeMarriedFirstName= new FieldSimpleValue();
        wifeMarriedLastName = new FieldSimpleValue();
        //wifeMarriedSex       = new FieldSex();
        wifeMarriedDead      = new FieldDead();
        wifeMarriedOccupation= new FieldOccupation();
        wifeMarriedComment   = new FieldComment();
        
        wifeFatherFirstName = new FieldSimpleValue();
        wifeFatherLastName  = new FieldSimpleValue();
        wifeFatherDead      = new FieldDead();
        wifeFatherOccupation= new FieldOccupation();
        wifeFatherComment   = new FieldComment();
        
        wifeMotherFirstName = new FieldSimpleValue();
        wifeMotherLastName  = new FieldSimpleValue();
        wifeMotherDead      = new FieldDead();
        wifeMotherOccupation= new FieldOccupation();
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
}
