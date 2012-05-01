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
        indiFatherDead      = new FieldDead();
        indiFatherOccupation= new FieldOccupation();
        indiFatherComment   = new FieldComment();

        indiMotherFirstName = new FieldSimpleValue();
        indiMotherLastName  = new FieldSimpleValue();
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

}
