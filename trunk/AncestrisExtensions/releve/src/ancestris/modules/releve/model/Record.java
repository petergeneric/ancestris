package ancestris.modules.releve.model;

import ancestris.modules.releve.model.Record.FieldType;

/**
 *
 * @author Michel
 */
public abstract class Record {
    
    public enum RecordType { BIRTH, MARRIAGE, DEATH, MISC }

    protected Field[] fields = new Field[FieldType.values().length];
   
    public Record() {
    }
    
    abstract public RecordType getType();

   /////////////////////////////////////////////////////////////////////////
   /**
     * @return the field
     */
    public Field getField(FieldType fieldType) {
        return fields[fieldType.ordinal()];
    }
    
    public String getFieldValue(FieldType fieldType) {
        Field field = getField(fieldType);
        if( field != null ) {
            return field.getValue();            
        } else {
            return "";
        }        
    }
    
    public String getFieldString(FieldType fieldType) {
        Field field = getField(fieldType);
        if( field != null ) {
            return field.toString();            
        } else {
            return "";
        }        
    }
    
    public boolean isEmptyField(FieldType fieldType) {
        Field field = getField(fieldType);
        if( field != null ) {
            return field.isEmpty();            
        } else {
            return true;
        }
        
    }
    
    /**
     * copie une valeur dans un champ
     * Si la valeur est vide et si le champ est null, la valeur n'est pas copiée
     * dans le champ pour éviter de créer le champ inutilement et économiser la mémoire
     * @param fieldType
     * @param value 
     */
    public void setFieldValue(FieldType fieldType, String value) {
        Field field = getField(fieldType);
        if( ! (value.isEmpty() && field == null) ) {
            if( field == null ) {
                field = createField(fieldType);
            }   
            field.setValue(value);
        }        
    }
    
    public Field createField(FieldType fieldType) {
        switch(fieldType) {
            case eventType:
                fields[fieldType.ordinal()] = new FieldEventType();
                break;
            case indiAge:
            case indiFatherAge:
            case indiMotherAge:
            case wifeAge:
            case wifeFatherAge:
            case wifeMotherAge:
                fields[fieldType.ordinal()] = new FieldAge();
                break;
            case freeComment:
                fields[fieldType.ordinal()]= new FieldPicture();
                break;
            case generalComment:
            case indiComment:
            case indiMarriedComment:
            case indiFatherComment:
            case indiMotherComment:
            case wifeComment:
            case wifeMarriedComment:
            case wifeFatherComment:
            case wifeMotherComment:
               fields[fieldType.ordinal()] = new FieldComment();
                break;
            case eventDate:
            case secondDate:
            case indiBirthDate:
            case wifeBirthDate:
                fields[fieldType.ordinal()] = new FieldDate();
                break;
            case indiMarriedDead:
            case indiFatherDead:
            case indiMotherDead:
            case wifeMarriedDead:
            case wifeFatherDead:
            case wifeMotherDead:
                fields[fieldType.ordinal()] = new FieldDead();
                break;
            case indiBirthPlace:
            case indiBirthAddress:
            case indiResidence:
            case indiAddress:
            case indiMarriedResidence:
            case indiMarriedAddress:
            case indiFatherResidence:
            case indiFatherAddress:
            case indiMotherResidence:
            case indiMotherAddress:
            case wifeBirthPlace:
            case wifeBirthAddress:
            case wifeResidence:
            case wifeAddress:
            case wifeMarriedResidence:
            case wifeMarriedAddress:
            case wifeFatherResidence:
            case wifeFatherAddress:
            case wifeMotherResidence:
            case wifeMotherAddress:
                fields[fieldType.ordinal()] = new FieldPlace();
                break;
            case indiSex:
//                case indi.marriedSex:
            case wifeSex:
                fields[fieldType.ordinal()] = new FieldSex();
                break;
            case cote:
            case notary:
            case parish:
            case indiFirstName:
            case indiLastName:
            case indiOccupation:
            case indiMarriedFirstName:
            case indiMarriedLastName:
            case indiMarriedOccupation:
            case indiFatherFirstName:
            case indiFatherLastName:
            case indiFatherOccupation:
            case indiMotherFirstName:
            case indiMotherLastName:
            case indiMotherOccupation:
            case wifeFirstName:
            case wifeLastName:
            case wifeOccupation:
            case wifeMarriedFirstName:
            case wifeMarriedLastName:
            case wifeMarriedOccupation:
            case wifeFatherFirstName:
            case wifeFatherLastName:
            case wifeFatherOccupation:
            case wifeMotherFirstName:
            case wifeMotherLastName:
            case wifeMotherOccupation:
            case witness1FirstName:
            case witness1LastName:
            case witness1Occupation:
            case witness1Comment:
            case witness2FirstName:
            case witness2LastName:
            case witness2Occupation:
            case witness2Comment:
            case witness3FirstName:
            case witness3LastName:
            case witness3Occupation:
            case witness3Comment:
            case witness4FirstName:
            case witness4LastName:
            case witness4Occupation:
            case witness4Comment:
            default:
                fields[fieldType.ordinal()] = new FieldSimpleValue();
                break;

        }
             
        return fields[fieldType.ordinal()];
    }
    
    public void setIndi(String inFirstName, String inLastName, String inSexe,
            String inAge, String inBirthDate, String inBirthPlace, String inBirthAddress,
            String inOccupation, String inResidence, String inAddress, String inComment) {

        setFieldValue(FieldType.indiFirstName, inFirstName.trim());
        setFieldValue(FieldType.indiLastName, inLastName.trim());
        setFieldValue(FieldType.indiSex, inSexe.trim());
        setFieldValue(FieldType.indiAge, inAge.trim());
        setFieldValue(FieldType.indiBirthDate, inBirthDate.trim());
        setFieldValue(FieldType.indiBirthPlace, inBirthPlace.trim());
        setFieldValue(FieldType.indiBirthAddress, inBirthAddress.trim());
        setFieldValue(FieldType.indiOccupation, inOccupation.trim());
        setFieldValue(FieldType.indiResidence, inResidence.trim());
        setFieldValue(FieldType.indiAddress, inAddress.trim());
        setFieldValue(FieldType.indiComment, inComment.trim());        
    }
    
    public void setIndiMarried(String inFirstName, String inLastName, /*String stringSexe, */
            String inOccupation, String inResidence, String inAddress, String inComment, String inDead) {

        setFieldValue(FieldType.indiMarriedFirstName, inFirstName.trim());
        setFieldValue(FieldType.indiMarriedLastName, inLastName.trim());
        setFieldValue(FieldType.indiMarriedOccupation, inOccupation.trim());
        setFieldValue(FieldType.indiMarriedResidence, inResidence.trim());
        setFieldValue(FieldType.indiMarriedAddress, inAddress.trim());
        setFieldValue(FieldType.indiMarriedComment, inComment.trim());
        setFieldValue(FieldType.indiMarriedDead, inDead.trim());
    }
    
    public void setIndiFather(String inFirstName, String inLastName, String inOccupation, 
            String inResidence, String inAddress, String inComment, String inDead, String inAge) {
                
        setFieldValue(FieldType.indiFatherFirstName, inFirstName.trim());
        setFieldValue(FieldType.indiFatherLastName, inLastName.trim());
        setFieldValue(FieldType.indiFatherOccupation, inOccupation.trim());
        setFieldValue(FieldType.indiFatherResidence, inResidence.trim());
        setFieldValue(FieldType.indiFatherAddress, inAddress.trim());
        setFieldValue(FieldType.indiFatherComment, inComment.trim());
        setFieldValue(FieldType.indiFatherDead, inDead.trim());
        setFieldValue(FieldType.indiFatherAge, inAge.trim());
    }
    
    public void setIndiMother(String inFirstName, String inLastName, String inOccupation, 
            String inResidence, String inAddress, String inComment, String inDead, String inAge) {
                
        setFieldValue(FieldType.indiMotherFirstName, inFirstName.trim());
        setFieldValue(FieldType.indiMotherLastName, inLastName.trim());
        setFieldValue(FieldType.indiMotherOccupation, inOccupation.trim());
        setFieldValue(FieldType.indiMotherResidence, inResidence.trim());
        setFieldValue(FieldType.indiMotherAddress, inAddress.trim());
        setFieldValue(FieldType.indiMotherComment, inComment.trim());
        setFieldValue(FieldType.indiMotherDead, inDead.trim());
        setFieldValue(FieldType.indiMotherAge, inAge.trim());
    }
    
    // wife 
    public void setWife(String inFirstName, String inLastName, String inSexe,
            String inAge, String inBirthDate, String inBirthPlace, String inBirthAddress,
            String inOccupation, String inResidence, String inAddress, String inComment) {

        setFieldValue(FieldType.wifeFirstName, inFirstName.trim());
        setFieldValue(FieldType.wifeLastName, inLastName.trim());
        setFieldValue(FieldType.wifeSex, inSexe.trim());
        setFieldValue(FieldType.wifeAge, inAge.trim());
        setFieldValue(FieldType.wifeBirthDate, inBirthDate.trim());
        setFieldValue(FieldType.wifeBirthPlace, inBirthPlace.trim());
        setFieldValue(FieldType.wifeBirthAddress, inBirthAddress.trim());
        setFieldValue(FieldType.wifeOccupation, inOccupation.trim());
        setFieldValue(FieldType.wifeResidence, inResidence.trim());
        setFieldValue(FieldType.wifeAddress, inAddress.trim());
        setFieldValue(FieldType.wifeComment, inComment.trim());        
    }

    public void setWifeMarried(String inFirstName, String inLastName, /*String stringSexe, */
            String inOccupation, String inResidence, String inAddress, String inComment, String inDead) {

        setFieldValue(FieldType.wifeMarriedFirstName, inFirstName.trim());
        setFieldValue(FieldType.wifeMarriedLastName, inLastName.trim());
        setFieldValue(FieldType.wifeMarriedOccupation, inOccupation.trim());
        setFieldValue(FieldType.wifeMarriedResidence, inResidence.trim());
        setFieldValue(FieldType.wifeMarriedAddress, inAddress.trim());
        setFieldValue(FieldType.wifeMarriedComment, inComment.trim());
        setFieldValue(FieldType.wifeMarriedDead, inDead.trim());
    }
    
    public void setWifeFather(String inFirstName, String inLastName, String inOccupation, 
            String inResidence, String inAddress, String inComment, String inDead, String inAge) {
                
        setFieldValue(FieldType.wifeFatherFirstName, inFirstName.trim());
        setFieldValue(FieldType.wifeFatherLastName, inLastName.trim());
        setFieldValue(FieldType.wifeFatherOccupation, inOccupation.trim());
        setFieldValue(FieldType.wifeFatherResidence, inResidence.trim());
        setFieldValue(FieldType.wifeFatherAddress, inAddress.trim());
        setFieldValue(FieldType.wifeFatherComment, inComment.trim());
        setFieldValue(FieldType.wifeFatherDead, inDead.trim());
        setFieldValue(FieldType.wifeFatherAge, inAge.trim());
    }
    
    public void setWifeMother(String inFirstName, String inLastName, String inOccupation, 
            String inResidence, String inAddress, String inComment, String inDead, String inAge) {
                
        setFieldValue(FieldType.wifeMotherFirstName, inFirstName.trim());
        setFieldValue(FieldType.wifeMotherLastName, inLastName.trim());
        setFieldValue(FieldType.wifeMotherOccupation, inOccupation.trim());
        setFieldValue(FieldType.wifeMotherResidence, inResidence.trim());
        setFieldValue(FieldType.wifeMotherAddress, inAddress.trim());
        setFieldValue(FieldType.wifeMotherComment, inComment.trim());
        setFieldValue(FieldType.wifeMotherDead, inDead.trim());
        setFieldValue(FieldType.wifeMotherAge, inAge.trim());
    }
    
    public void setWitness1(String firstName, String lastName, String occupation, String comment) {
        setFieldValue(FieldType.witness1FirstName, firstName.trim());
        setFieldValue(FieldType.witness1LastName, lastName.trim());
        setFieldValue(FieldType.witness1Occupation, occupation.trim());
        setFieldValue(FieldType.witness1Comment, comment.trim());
    }
    
    public void setWitness2(String firstName, String lastName, String occupation, String comment) {
        setFieldValue(FieldType.witness2FirstName, firstName.trim());
        setFieldValue(FieldType.witness2LastName, lastName.trim());
        setFieldValue(FieldType.witness2Occupation, occupation.trim());
        setFieldValue(FieldType.witness2Comment, comment.trim());
    }
    
    public void setWitness3(String firstName, String lastName, String occupation, String comment) {
        setFieldValue(FieldType.witness3FirstName, firstName.trim());
        setFieldValue(FieldType.witness3LastName, lastName.trim());
        setFieldValue(FieldType.witness3Occupation, occupation.trim());
        setFieldValue(FieldType.witness3Comment, comment.trim());
    }
    
    public void setWitness4(String firstName, String lastName, String occupation, String comment) {
        setFieldValue(FieldType.witness4FirstName, firstName.trim());
        setFieldValue(FieldType.witness4LastName, lastName.trim());
        setFieldValue(FieldType.witness4Occupation, occupation.trim());
        setFieldValue(FieldType.witness4Comment, comment.trim());
    }
    
    public static enum FieldType {
        //title,
        //cityCode,
        //cityName,
        //countyCode,
        //countyName,
        eventType,
        cote,
        freeComment,
        generalComment,
        eventDate,
        secondDate,
        notary,
        parish,    // 13 + 29 + 29 + 16 =  77
        //  indi ///////////////////////////////////////////////////////////////////
        indiFirstName,
        indiLastName,
        indiSex,
        indiAge,
        indiBirthDate,
        indiBirthPlace,
        indiBirthAddress,
        indiOccupation,
        indiResidence,
        indiAddress,
        indiComment,
        //  conjoint (ou ancien conjoint) //////////////////////////////////////////
        indiMarriedFirstName,
        indiMarriedLastName,
        //indiMarriedSex,
        indiMarriedDead,
        indiMarriedOccupation,
        indiMarriedResidence,
        indiMarriedAddress,
        indiMarriedComment,
        //  indi father ////////////////////////////////////////////////////////////
        indiFatherFirstName,
        indiFatherLastName,
        indiFatherAge,
        indiFatherDead,
        indiFatherOccupation,
        indiFatherResidence,
        indiFatherAddress,
        indiFatherComment,
        indiMotherFirstName,
        indiMotherLastName,
        indiMotherAge,
        indiMotherDead,
        indiMotherOccupation,
        indiMotherResidence,
        indiMotherAddress,
        indiMotherComment,  //29
        //  wife ///////////////////////////////////////////////////////////////////
        wifeFirstName,
        wifeLastName,
        wifeSex,
        //wifeDead,
        wifeAge,
        wifeBirthDate,
        wifeBirthPlace,
        wifeBirthAddress,
        wifeOccupation,
        wifeResidence,
        wifeAddress,
        wifeComment,
        //  wifeMarried ///////////////////////////////////////////////////////////
        wifeMarriedFirstName,
        wifeMarriedLastName,
        //wifeMarriedSex,
        wifeMarriedDead,
        wifeMarriedOccupation,
        wifeMarriedResidence,
        wifeMarriedAddress,
        wifeMarriedComment,
        //  wifeFather ///////////////////////////////////////////////////////////
        wifeFatherFirstName,
        wifeFatherLastName,
        wifeFatherAge,
        wifeFatherDead,
        wifeFatherOccupation,
        wifeFatherResidence,
        wifeFatherAddress,
        wifeFatherComment,
        wifeMotherFirstName,
        wifeMotherLastName,
        wifeMotherAge,
        wifeMotherDead,
        wifeMotherOccupation,
        wifeMotherResidence,
        wifeMotherAddress,
        wifeMotherComment,  // 29
        // wintness ///////////////////////////////////////////////////////////////
        witness1FirstName,
        witness1LastName,
        witness1Occupation,
        witness1Comment,
        witness2FirstName,
        witness2LastName,
        witness2Occupation,
        witness2Comment,
        witness3FirstName,
        witness3LastName,
        witness3Occupation,
        witness3Comment,
        witness4FirstName,
        witness4LastName,
        witness4Occupation,
        witness4Comment      // 16
    }
}