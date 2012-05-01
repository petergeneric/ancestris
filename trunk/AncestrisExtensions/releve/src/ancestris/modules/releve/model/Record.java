package ancestris.modules.releve.model;

import ancestris.modules.releve.model.Field.FieldType;
import genj.gedcom.PropertyDate;

/**
 *
 * @author Michel
 */
public abstract class Record {

    public int recordNo;
    protected FieldEventType eventType;
    protected FieldPlace eventPlace;
    protected FieldSimpleValue cote;
    protected FieldPicture freeComment;
    protected FieldDate eventDate;
    protected FieldComment generalComment;
    protected FieldComment notary;
    protected FieldSimpleValue parish;

    protected FieldSimpleValue indiFirstName;
    protected FieldSimpleValue indiLastName;
    protected FieldSex indiSex;
    protected FieldSimpleValue indiAge;
    protected FieldDate indiBirthDate;
    protected FieldOccupation indiOccupation;
    protected FieldPlace indiPlace;
    protected FieldComment indiComment;
    
    protected FieldSimpleValue indiMarriedFirstName;
    protected FieldSimpleValue indiMarriedLastName;
    //protected FieldSex indiMarriedSex;
    protected FieldComment indiMarriedComment;
    protected FieldOccupation indiMarriedOccupation;
    protected FieldDead indiMarriedDead;

    protected FieldSimpleValue indiFatherFirstName;
    protected FieldSimpleValue indiFatherLastName;
    protected FieldOccupation indiFatherOccupation;
    protected FieldDead indiFatherDead;
    protected FieldComment indiFatherComment;
    
    protected FieldSimpleValue indiMotherFirstName;
    protected FieldSimpleValue indiMotherLastName;
    protected FieldOccupation indiMotherOccupation;
    protected FieldDead indiMotherDead;
    protected FieldComment indiMotherComment;

    protected FieldSimpleValue wifeFirstName;
    protected FieldSimpleValue wifeLastName;
    protected FieldSex wifeSex;
    protected FieldSimpleValue wifeAge;
    protected FieldDate wifeBirthDate;
    protected FieldOccupation wifeOccupation;
    protected FieldPlace wifePlace;
    protected FieldComment wifeComment;
    
    protected FieldSimpleValue wifeMarriedFirstName;
    protected FieldSimpleValue wifeMarriedLastName;
    //protected FieldSex wifeMarriedSex;
    protected FieldComment wifeMarriedComment;
    protected FieldOccupation wifeMarriedOccupation;
    protected FieldDead wifeMarriedDead;
    
    protected FieldSimpleValue wifeFatherFirstName;
    protected FieldSimpleValue wifeFatherLastName;
    protected FieldOccupation wifeFatherOccupation;
    protected FieldDead wifeFatherDead;
    protected FieldComment wifeFatherComment;
    
    protected FieldSimpleValue wifeMotherFirstName;
    protected FieldSimpleValue wifeMotherLastName;
    protected FieldOccupation wifeMotherOccupation;
    protected FieldDead wifeMotherDead;
    protected FieldComment wifeMotherComment;

    protected FieldSimpleValue witness1FirstName;
    protected FieldSimpleValue witness1LastName;
    protected FieldOccupation witness1Occupation;
    protected FieldComment witness1Comment;
    protected FieldSimpleValue witness2FirstName;
    protected FieldSimpleValue witness2LastName;
    protected FieldOccupation witness2Occupation;
    protected FieldComment witness2Comment;
    protected FieldSimpleValue witness3FirstName;
    protected FieldSimpleValue witness3LastName;
    protected FieldOccupation witness3Occupation;
    protected FieldComment witness3Comment;
    protected FieldSimpleValue witness4FirstName;
    protected FieldSimpleValue witness4LastName;
    protected FieldOccupation witness4Occupation;
    protected FieldComment witness4Comment;

    public Record() {
        eventPlace = new FieldPlace();
        eventDate = new FieldDate();
        cote = new FieldSimpleValue(); 
        parish = new FieldSimpleValue(); 
        freeComment = new FieldPicture();
        generalComment = new FieldComment();
    }

    public int getRecordNo() {
        return  recordNo;
    }

    public FieldEventType getEventType() {
        return  eventType;
    }

    public PropertyDate getEventDateField() {
        return eventDate.getPropertyDate();
    }

    public String getEventDateString() {
        return eventDate.getValue();
    }

//    public String getStateName() {
//        return eventPlace.getStateName();
//    }
//
//    public String getCityName() {
//        return eventPlace.getCityName();
//    }
//
//    public String getCountryName() {
//        return eventPlace.getCountryName();
//    }
//
//    public String getCountyName() {
//        return eventPlace.getCountyName();
//    }
//
    public FieldPlace getEventPlace() {
        return eventPlace;
    }

    public String getCote() {
        return cote.getValue();
    }

    public String getFreeComment() {
        return freeComment.getValue();
    }

    public String getGeneralComment() {
        return generalComment.getValue();
    }

    public String getNotary() {
        return notary.getValue();
    }

    public String getParish() {
        return parish.getValue();
    }

    //  indi ///////////////////////////////////////////////////////////////////

//    public FieldSimpleValue getIndiName() {
//        return indiFirstName;
//    }

    public FieldSimpleValue getIndiLastName() {
        return indiLastName;
    }

    public FieldSimpleValue getIndiFirstName() {
        return indiFirstName;
    }

    public String getIndiSex() {
        return indiSex.getValue();
    }

    public FieldSex getIndiSexField() {
        return indiSex;
    }

    public String getIndiAge() {
        return indiAge.getValue();
    }

    public String getIndiBirthDate() {
        return indiBirthDate.getValue();
    }

    public FieldPlace getIndiPlace() {
        return indiPlace;
    }

    public FieldOccupation getIndiOccupation() {
        return indiOccupation;
    }

    public String getIndiComment() {
        return indiComment.getValue();
    }

    //  conjoint (ou ancien conjoint) //////////////////////////////////////////
    public FieldSimpleValue getIndiMarriedLastName() {
        return indiMarriedLastName;
    }

    public FieldSimpleValue getIndiMarriedFirstName() {
        return indiMarriedFirstName;
    }

//    public FieldSex getIndiMarriedSex() {
//        return indiMarriedSex;
//    }

    public FieldDead getIndiMarriedDead() {
        return indiMarriedDead;
    }

    public FieldOccupation getIndiMarriedOccupation() {
        return indiMarriedOccupation;
    }

    public String getIndiMarriedComment() {
        return indiMarriedComment.getValue();
    }

    //  indi father ////////////////////////////////////////////////////////////
    public FieldSimpleValue getIndiFatherLastName() {
        return indiFatherLastName;
    }

    public FieldSimpleValue getIndiFatherFirstName() {
        return indiFatherFirstName;
    }

    public FieldDead getIndiFatherDead() {
        return indiFatherDead;
    }

    public FieldOccupation getIndiFatherOccupation() {
        return indiFatherOccupation;
    }

    public String getIndiFatherComment() {
        return indiFatherComment.getValue();
    }


    public FieldSimpleValue getIndiMotherLastName() {
        return indiMotherLastName;
    }

    public FieldSimpleValue getIndiMotherFirstName() {
        return indiMotherFirstName;
    }

    public FieldDead getIndiMotherDead() {
        return indiMotherDead;
    }

    public FieldOccupation getIndiMotherOccupation() {
        return indiMotherOccupation;
    }

    public String getIndiMotherComment() {
        return indiMotherComment.getValue();
    }

    //  wife ///////////////////////////////////////////////////////////////////
    public FieldSimpleValue getWifeLastName() {
        return wifeLastName;
    }

    public FieldSimpleValue getWifeFirstName() {
        return wifeFirstName;
    }

    public String getWifeSex() {
        return wifeSex.getValue();
    }

    public FieldSex getWifeSexField() {
        return wifeSex;
    }

    public String getWifeAge() {
        return wifeAge.getValue();
    }

    public String getWifeBirthDate() {
        return wifeBirthDate.getValue();
    }

    public FieldPlace getWifePlace() {
        return wifePlace;
    }

    public FieldOccupation getWifeOccupation() {
        return wifeOccupation;
    }

    public String getWifeComment() {
        return wifeComment.getValue();
    }

    //  wifeMarried ///////////////////////////////////////////////////////////
    public FieldSimpleValue getWifeMarriedLastName() {
        return wifeMarriedLastName;
    }

    public FieldSimpleValue getWifeMarriedFirstName() {
        return wifeMarriedFirstName;
    }

//    public FieldSex getWifeMarriedSex() {
//        return wifeMarriedSex;
//    }

    public FieldDead getWifeMarriedDead() {
        return wifeMarriedDead;
    }

    public FieldOccupation getWifeMarriedOccupation() {
        return wifeMarriedOccupation;
    }

    public String getWifeMarriedComment() {
        return wifeMarriedComment.getValue();
    }

    //  wifeFather ///////////////////////////////////////////////////////////
    public FieldSimpleValue getWifeFatherLastName() {
        return wifeFatherLastName;
    }

    public FieldSimpleValue getWifeFatherFirstName() {
        return wifeFatherFirstName;
    }

    public FieldDead getWifeFatherDead() {
        return wifeFatherDead;
    }

    public FieldOccupation getWifeFatherOccupation() {
        return wifeFatherOccupation;
    }

    public String getWifeFatherComment() {
        return wifeFatherComment.getValue();
    }

    public FieldSimpleValue getWifeMotherLastName() {
        return wifeMotherLastName;
    }

    public FieldSimpleValue getWifeMotherFirstName() {
        return wifeMotherFirstName;
    }

    public FieldDead getWifeMotherDead() {
        return wifeMotherDead;
    }

    public FieldOccupation getWifeMotherOccupation() {
        return wifeMotherOccupation;
    }

    public String getWifeMotherComment() {
        return wifeMotherComment.getValue();
    }

    // wintness ///////////////////////////////////////////////////////////////
    public FieldSimpleValue getWitness1LastName() {
        return witness1LastName;
    }

    public FieldSimpleValue getWitness1FirstName() {
        return witness1FirstName;
    }

    public FieldOccupation getWitness1Occupation() {
        return witness1Occupation;
    }

    public String getWitness1Comment() {
        return witness1Comment.getValue();
    }

    public FieldSimpleValue getWitness2LastName() {
        return witness2LastName;
    }

    public FieldSimpleValue getWitness2FirstName() {
        return witness2FirstName;
    }

    public FieldOccupation getWitness2Occupation() {
        return witness2Occupation;
    }

    public String getWitness2Comment() {
        return witness2Comment.getValue();
    }

    public FieldSimpleValue getWitness3LastName() {
        return witness3LastName;
    }

    public FieldSimpleValue getWitness3FirstName() {
        return witness3FirstName;
    }

    public FieldOccupation getWitness3Occupation() {
        return witness3Occupation;
    }

    public String getWitness3Comment() {
        return witness3Comment.getValue();
    }

    public FieldSimpleValue getWitness4LastName() {
        return witness4LastName;
    }

    public FieldSimpleValue getWitness4FirstName() {
        return witness4FirstName;
    }

    public FieldOccupation getWitness4Occupation() {
        return witness4Occupation;
    }

    public String getWitness4Comment() {
        return witness4Comment.getValue();
    }

//    public void setEventPlace(String juridictions) {
//        eventPlace.setValue(juridictions);
//    }

    public void setEventPlace(String cityName, String cityCode, String countyName, String stateName, String countryName) {
        eventPlace.setCityName(cityName.trim());
        eventPlace.setCityCode(cityCode.trim());
        eventPlace.setCountryName(countyName.trim());
        eventPlace.setStateName(stateName.trim());
        eventPlace.setCountyName(countryName.trim());
    }


    /////////////////////////////////////////////////////////////////////////
   /**
     * @return the field
     */
    public Field getField(FieldType fieldType) {
        Field field = null;
            switch (fieldType) {
                case eventType:
                    field = eventType;
                    break;
                case cote:
                    field = cote;
                    break;
                case freeComment:
                    field = freeComment;
                    break;
                case generalComment:
                    field = generalComment;
                    break;
                case eventDate:
                    field = eventDate;
                    break;
                case notary:
                    field = notary;
                    break;
                case parish:
                    field = parish;
                    break;
                //indi///////////////////////////////////////////////////////////////////
                case indiFirstName:
                    field = indiFirstName;
                    break;
                case indiLastName:
                    field = indiLastName;
                    break;
                case indiSex:
                    field = indiSex;
                    break;
                case indiAge:
                    field = indiAge;
                    break;
                case indiBirthDate:
                    field = indiBirthDate;
                    break;
                case indiPlace:
                    field = indiPlace;
                    break;
                case indiOccupation:
                    field = indiOccupation;
                    break;
                case indiComment:
                    field = indiComment;
                    break;
                //conjoint(ouancienconjoint)//////////////////////////////////////////
                case indiMarriedFirstName:
                    field = indiMarriedFirstName;
                    break;
                case indiMarriedLastName:
                    field = indiMarriedLastName;
                    break;
//                case indiMarriedSex:
//                    field = indiMarriedSex;
//                    break;
                case indiMarriedDead:
                    field = indiMarriedDead;
                    break;
                case indiMarriedOccupation:
                    field = indiMarriedOccupation;
                    break;
                case indiMarriedComment:
                    field = indiMarriedComment;
                    break;
                //indifather////////////////////////////////////////////////////////////
                case indiFatherFirstName:
                    field = indiFatherFirstName;
                    break;
                case indiFatherLastName:
                    field = indiFatherLastName;
                    break;
                case indiFatherDead:
                    field = indiFatherDead;
                    break;
                case indiFatherOccupation:
                    field = indiFatherOccupation;
                    break;
                case indiFatherComment:
                    field = indiFatherComment;
                    break;
                case indiMotherFirstName:
                    field = indiMotherFirstName;
                    break;
                case indiMotherLastName:
                    field = indiMotherLastName;
                    break;
                case indiMotherDead:
                    field = indiMotherDead;
                    break;
                case indiMotherOccupation:
                    field = indiMotherOccupation;
                    break;
                case indiMotherComment:
                    field = indiMotherComment;
                    break;
                //wife///////////////////////////////////////////////////////////////////
                case wifeFirstName:
                    field = wifeFirstName;
                    break;
                case wifeLastName:
                    field = wifeLastName;
                    break;
                case wifeSex:
                    field = wifeSex;
                    break;
//case 	wifeDead	 :	 field = wifeDead	;  break;
                case wifeAge:
                    field = wifeAge;
                    break;
                case wifeBirthDate:
                    field = wifeBirthDate;
                    break;
                case wifePlace:
                    field = wifePlace;
                    break;
                case wifeOccupation:
                    field = wifeOccupation;
                    break;
                case wifeComment:
                    field = wifeComment;
                    break;
                //wifeMarried///////////////////////////////////////////////////////////			//wifeMarried///////////////////////////////////////////////////////////
                case wifeMarriedFirstName:
                    field = wifeMarriedFirstName;
                    break;
                case wifeMarriedLastName:
                    field = wifeMarriedLastName;
                    break;
                case wifeMarriedDead:
                    field = wifeMarriedDead;
                    break;
//                case wifeMarriedSex:
//                    field = wifeMarriedSex;
//                    break;
                case wifeMarriedOccupation:
                    field = wifeMarriedOccupation;
                    break;
                case wifeMarriedComment:
                    field = wifeMarriedComment;
                    break;
                //wifeFather///////////////////////////////////////////////////////////
                case wifeFatherFirstName:
                    field = wifeFatherFirstName;
                    break;
                case wifeFatherLastName:
                    field = wifeFatherLastName;
                    break;
                case wifeFatherDead:
                    field = wifeFatherDead;
                    break;
                case wifeFatherOccupation:
                    field = wifeFatherOccupation;
                    break;
                case wifeFatherComment:
                    field = wifeFatherComment;
                    break;
                case wifeMotherFirstName:
                    field = wifeMotherFirstName;
                    break;
                case wifeMotherLastName:
                    field = wifeMotherLastName;
                    break;
                case wifeMotherDead:
                    field = wifeMotherDead;
                    break;
                case wifeMotherOccupation:
                    field = wifeMotherOccupation;
                    break;
                case wifeMotherComment:
                    field = wifeMotherComment;
                    break;
                //wintness///////////////////////////////////////////////////////////////
                case witness1FirstName:
                    field = witness1FirstName;
                    break;
                case witness1LastName:
                    field = witness1LastName;
                    break;
                case witness1Occupation:
                    field = witness1Occupation;
                    break;
                case witness1Comment:
                    field = witness1Comment;
                    break;
                case witness2FirstName:
                    field = witness2FirstName;
                    break;
                case witness2LastName:
                    field = witness2LastName;
                    break;
                case witness2Occupation:
                    field = witness2Occupation;
                    break;
                case witness2Comment:
                    field = witness2Comment;
                    break;
                case witness3FirstName:
                    field = witness3FirstName;
                    break;
                case witness3LastName:
                    field = witness3LastName;
                    break;
                case witness3Occupation:
                    field = witness3Occupation;
                    break;
                case witness3Comment:
                    field = witness3Comment;
                    break;
                case witness4FirstName:
                    field = witness4FirstName;
                    break;
                case witness4LastName:
                    field = witness4LastName;
                    break;
                case witness4Occupation:
                    field = witness4Occupation;
                    break;
                case witness4Comment:
                    field = witness4Comment;
                    break;
            }

        return field;
    }

    
    
    public void setEventType(String name, String tag) {
        eventType.setName(name);
        eventType.setTag(tag);
    }

    public void setEventDate(String dateString) {
        eventDate.setValue(dateString);
    }

    public void setEventDateString(String value) {
        eventDate.setValue(value);
    }

    public void setEventDate(String strDay, String strMonth, String strYear) throws NumberFormatException {
        eventDate.setValue(strDay, strMonth, strYear);
    }

//    public void setStateName(String value) {
//        eventPlace.setStateName(value);
//    }
//
//    public void setCityName(String value) {
//        eventPlace.setCityName(value);
//    }
//
//    public void setCountryName(String value) {
//        eventPlace.setCountryName(value);
//    }
//
//    public void setCountyName(String value) {
//        eventPlace.setCountyName(value);
//    }

    public void setCote(String value) {
        cote.setValue(value);
    }

    public void setFreeComment(String value) {
        freeComment.setValue(value);
    }

    public void setGeneralComment(String value) {
        generalComment.setValue(value);
    }

    
    public void setNotary(String value) {
        notary.setValue(value);
    }

    public void setParish(String value) {
        parish.setValue(value);
    }

    //  indi ///////////////////////////////////////////////////////////////////
//    public void setIndiLastName(String value) {
//        indiName.setLastName(value);
//    }
//
//    public void setIndiFirstName(String value) {
//        indiName.setFirstName(value);
//    }

//    public void setIndiSex(String value) {
//        indiSex.setValue(value);
//    }
//
//    public void setIndiAge(String value) {
//        if (indiAge != null ) {
//            indiAge.setValue(value);
//        }
//    }
//
//    public void setIndiBirthDate(String value) {
//        indiBirthDate.setValue(value);
//    }
//
//    public void setIndiPlace(String value) {
//        indiPlace.setValue(value);
//    }
//
//    public void setIndiComment(String value) {
//        indiComment.setValue(value);
//    }

//    public void setFatherLastName(String value) {
//        indiFatherName.getLastName();
//    }
//
//    public void setFatherFirstName(String value) {
//        indiFatherName.setFirstName(value);
//    }

    public void setFatherOccupation(String value) {
        indiFatherOccupation.setValue(value);
    }

    public void setFatherComment(String value) {
        indiFatherComment.setValue(value);
    }

//    public void setMotherLastName(String value) {
//        indiMotherName.setLastName(value);
//    }
//
//    public void setMotherFirstName(String value) {
//        indiMotherName.setFirstName(value);
//    }

    public void setMotherOccupation(String value) {
        indiMotherOccupation.setValue(value);
    }

    public void setMotherComment(String value) {
        indiMotherComment.setValue(value);
    }

    //  wife //////////////////////////////////////////////////////////////////
//    public void setWifeAge(String value) {
//        wifeAge.setValue(value);
//    }
//
//    public void setWifeBirthDate(String value) {
//        wifeBirthDate.setValue(value);
//    }
//
//    public void setWifePlace(String value) {
//        wifePlace.setValue(value);
//    }

    //  witness //////////////////////////////////////////////////////////////////
//    public void setWitness1LastName(String value) {
//        witness1Name.getLastName();
//    }
//
//    public void setWitness1FirstName(String value) {
//        witness1Name.getFirstName();
//    }
//
//    public void setWitness1Occupation(String value) {
//        witness1Occupation.setValue(value);
//    }
//
//    public void setWitness1Comment(String value) {
//        witness1Comment.setValue(value);
//    }
//
//    public void setWitness2LastName(String value) {
//        witness2Name.getLastName();
//    }
//
//    public void setWitness2FirstName(String value) {
//        witness2Name.getFirstName();
//    }
//
//    public void setWitness2Occupation(String value) {
//        witness2Occupation.setValue(value);
//    }
//
//    public void setWitness2Comment(String value) {
//        witness2Comment.setValue(value);
//    }

    ///////////////////////////////////////////////////////////////////////////
    public void setIndi(String firstName, String lastName, String stringSexe,
            String stringAge, String stringBirthDate,
            String stringPlace, String profession, String comment) {
        indiFirstName.setValue(firstName.trim());
        indiLastName.setValue(lastName.trim());
        indiSex.setValue(stringSexe.trim());
        if (indiAge != null) {
            // l'age n'est pas utilisé pour une naissance
            indiAge.setValue(stringAge.trim());
        }
        if (indiBirthDate != null) {
            // la date de naissance n'est pas utilisée pour une naissance car c'est la même que la date de l'evenement
            indiBirthDate.setValue(stringBirthDate.trim());
        }
        if (indiPlace != null) {
            // le lieu n'est pas utilisée pour une naissance
            indiPlace.setValue(stringPlace.trim());
        }
        if (indiOccupation != null) {
            // la profession n'est pas utilisée pour une naissance
            indiOccupation.setValue(profession.trim());
        }
        indiComment.setValue(comment.trim());
    }

    public void setIndiMarried(String firstName, String lastName, /*String stringSexe, */String profession, String comment, String dead) {
        indiMarriedFirstName.setValue(firstName.trim());
        indiMarriedLastName.setValue(lastName.trim());
        //indiMarriedSex.setValue(stringSexe.trim());
        indiMarriedOccupation.setValue(profession.trim());
        indiMarriedComment.setValue(comment.trim());
        if (indiMarriedDead != null) {
            indiMarriedDead.setValue(dead.trim());
        }
    }

    public void setIndiFather(String firstName, String lastName, String profession, String comment, String dead) {
        indiFatherFirstName.setValue(firstName.trim());
        indiFatherLastName.setValue(lastName.trim());
        indiFatherOccupation.setValue(profession.trim());
        indiFatherComment.setValue(comment.trim());
        indiFatherDead.setValue(dead.trim());
    }

    public void setIndiMother(String firstName, String lastName, String profession, String comment, String dead) {
        indiMotherFirstName.setValue(firstName.trim());
        indiMotherLastName.setValue(lastName.trim());
        indiMotherOccupation.setValue(profession.trim());
        indiMotherComment.setValue(comment.trim());
        indiMotherDead.setValue(dead.trim());
    }

    public void setWife(String firstName, String lastName, String sex,
            String stringAge, String stringBirthDate,
            String stringPlace, String profession, String comment) {
        wifeFirstName.setValue(firstName.trim());
        wifeLastName.setValue(lastName.trim());
        if (wifeSex != null) {
            // le sexe n'est utilise que dans les actes divers
            wifeSex.setValue(sex.trim());
        }
        wifeAge.setValue(stringAge.trim());
        wifeBirthDate.setValue(stringBirthDate.trim());
        wifePlace.setValue(stringPlace.trim());
        wifeOccupation.setValue(profession.trim());
        wifeComment.setValue(comment.trim());
    }

    public void setWifeMarried(String firstName, String lastName, /*String stringSexe, */String profession, String comment, String dead) {
        wifeMarriedFirstName.setValue(firstName.trim());
        wifeMarriedLastName.setValue(lastName.trim());
        //wifeMarriedSex.setValue(stringSexe.trim());
        wifeMarriedOccupation.setValue(profession.trim());
        wifeMarriedComment.setValue(comment.trim());
        wifeMarriedDead.setValue(dead.trim());
    }

    public void setWifeFather(String firstName, String lastName, String profession, String comment, String dead) {
        wifeFatherFirstName.setValue(firstName.trim());
        wifeFatherLastName.setValue(lastName.trim());
        wifeFatherOccupation.setValue(profession.trim());
        wifeFatherComment.setValue(comment.trim());
        wifeFatherDead.setValue(dead.trim());
    }

    public void setWifeMother(String firstName, String lastName, String profession, String comment, String dead) {
        wifeMotherFirstName.setValue(firstName.trim());
        wifeMotherLastName.setValue(lastName.trim());
        wifeMotherOccupation.setValue(profession.trim());
        wifeMotherComment.setValue(comment.trim());
        wifeMotherDead.setValue(dead.trim());
    }

    public void setWitness1(String firstName, String lastName, String profession, String comment) {
        witness1FirstName.setValue(firstName.trim());
        witness1LastName.setValue(lastName.trim());
        witness1Occupation.setValue(profession.trim());
        witness1Comment.setValue(comment.trim());
    }

    public void setWitness2(String firstName, String lastName, String profession, String comment) {
        witness2FirstName.setValue(firstName.trim());
        witness2LastName.setValue(lastName.trim());
        witness2Occupation.setValue(profession.trim());
        witness2Comment.setValue(comment.trim());
    }

    public void setWitness3(String firstName, String lastName, String profession, String comment) {
        witness3FirstName.setValue(firstName.trim());
        witness3LastName.setValue(lastName.trim());
        witness3Occupation.setValue(profession.trim());
        witness3Comment.setValue(comment.trim());
    }

    public void setWitness4(String firstName, String lastName, String profession, String comment) {
        witness4FirstName.setValue(firstName.trim());
        witness4LastName.setValue(lastName.trim());
        witness4Occupation.setValue(profession.trim());
        witness4Comment.setValue(comment.trim());
    }

}
