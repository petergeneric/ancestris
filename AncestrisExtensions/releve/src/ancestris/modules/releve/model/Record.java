package ancestris.modules.releve.model;

import ancestris.modules.releve.model.Field.FieldType;
import genj.gedcom.PropertyDate;

/**
 *
 * @author Michel
 */
public abstract class Record implements Cloneable{

    public int recordNo;
    protected FieldEventType eventType;
    //protected FieldPlace eventPlace;
    protected FieldSimpleValue cote;
    protected FieldPicture freeComment;
    protected FieldDate eventDate;
    protected FieldComment generalComment;
    protected FieldNotary notary;
    protected FieldSimpleValue parish;
    private Participant indi;
    private Participant wife;
    Witness witness1;
    Witness witness2;
    Witness witness3;
    Witness witness4;

    public class Participant implements Cloneable {
        protected FieldSimpleValue firstName;
        protected FieldSimpleValue lastName;
        protected FieldSex sex;
        protected FieldAge age;
        protected FieldDate birthDate;
        protected FieldPlace birthPlace;
        protected FieldOccupation occupation;
        protected FieldPlace residence;
        protected FieldComment comment;
        protected FieldSimpleValue marriedFirstName;
        protected FieldSimpleValue marriedLastName;
        //protected FieldSex marriedSex;
        protected FieldComment marriedComment;
        protected FieldOccupation marriedOccupation;
        protected FieldPlace marriedResidence;
        protected FieldDead marriedDead;
        protected FieldSimpleValue fatherFirstName;
        protected FieldSimpleValue fatherLastName;
        protected FieldOccupation fatherOccupation;
        protected FieldPlace fatherResidence;
        protected FieldAge fatherAge;
        protected FieldDead fatherDead;
        protected FieldComment fatherComment;
        protected FieldSimpleValue motherFirstName;
        protected FieldSimpleValue motherLastName;
        protected FieldOccupation motherOccupation;
        protected FieldPlace motherResidence;
        protected FieldAge motherAge;
        protected FieldDead motherDead;
        protected FieldComment motherComment;

        @Override
        public Participant clone() throws CloneNotSupportedException {
            Participant object = (Participant) super.clone();

            if (firstName != null) {
                firstName = firstName.clone();
            }
            if (lastName != null) {
                lastName = lastName.clone();
            }
            if (sex != null) {
                sex = sex.clone();
            }
            if (age != null) {
                age = age.clone();
            }
            if (birthDate != null) {
                birthDate = birthDate.clone();
            }
            if (birthPlace != null) {
                birthPlace = birthPlace.clone();
            }
            if (occupation != null) {
                occupation = occupation.clone();
            }
            if (residence != null) {
                residence = residence.clone();
            }
            if (comment != null) {
                comment = comment.clone();
            }
            
            if (marriedFirstName != null) {
                marriedFirstName = marriedFirstName.clone();
            }
            if (marriedLastName != null) {
                marriedLastName = marriedLastName.clone();
            }
            if (marriedDead != null) {
                marriedDead = marriedDead.clone();
            }
            if (marriedOccupation != null) {
                marriedOccupation = marriedOccupation.clone();
            }
            if (marriedResidence != null) {
                marriedResidence = marriedResidence.clone();
            }
            if (marriedComment != null) {
                marriedComment = marriedComment.clone();
            }

            if (fatherFirstName != null) {
                fatherFirstName = fatherFirstName.clone();
            }
            if (fatherLastName != null) {
                fatherLastName = fatherLastName.clone();
            }
            if (fatherAge != null) {
                fatherAge = fatherAge.clone();
            }
            if (fatherDead != null) {
                fatherDead = fatherDead.clone();
            }
            if (fatherOccupation != null) {
                fatherOccupation = fatherOccupation.clone();
            }
            if (fatherResidence != null) {
                fatherResidence = fatherResidence.clone();
            }
            if (fatherComment != null) {
                fatherComment = fatherComment.clone();
            }

            if (motherFirstName != null) {
                motherFirstName = motherFirstName.clone();
            }
            if (motherLastName != null) {
                motherLastName = motherLastName.clone();
            }
            if (motherAge != null) {
                motherAge = motherAge.clone();
            }
            if (motherDead != null) {
                motherDead = motherDead.clone();
            }
            if (motherOccupation != null) {
                motherOccupation = motherOccupation.clone();
            }
            if (motherResidence != null) {
                motherResidence = motherResidence.clone();
            }
            if (motherComment != null) {
                motherComment = motherComment.clone();
            }
            
            // je renvoie le clone
            return object;
        }

        public FieldSimpleValue getLastName() {
            return lastName;
        }

        public FieldSimpleValue getFirstName() {
            return firstName;
        }

        public FieldSex getSex() {
            return sex;
        }

        public FieldAge getAge() {
            return age;
        }

        public FieldDate getBirthDate() {
            return birthDate;
        }

        public FieldPlace getBirthPlace() {
            return birthPlace;
        }

        public FieldOccupation getOccupation() {
            return occupation;
        }

        public FieldPlace getResidence() {
            return residence;
        }

        public FieldSimpleValue getComment() {
            return comment;
        }

        //  conjoint (ou ancien conjoint) //////////////////////////////////////////
        public FieldSimpleValue getMarriedLastName() {
            return marriedLastName;
        }

        public FieldSimpleValue getMarriedFirstName() {
            return marriedFirstName;
        }

//    public FieldSex getMarriedSex() {
//        return marriedSex;
//    }
        public FieldDead getMarriedDead() {
            return marriedDead;
        }

        public FieldOccupation getMarriedOccupation() {
            return marriedOccupation;
        }

        public FieldPlace getMarriedResidence() {
            return marriedResidence;
        }

        public FieldSimpleValue getMarriedComment() {
            return marriedComment;
        }

        //  indi father ////////////////////////////////////////////////////////////
        public FieldSimpleValue getFatherLastName() {
            return fatherLastName;
        }

        public FieldSimpleValue getFatherFirstName() {
            return fatherFirstName;
        }

        public FieldAge getFatherAge() {
            return fatherAge;
        }

        public FieldDead getFatherDead() {
            return fatherDead;
        }

        public FieldOccupation getFatherOccupation() {
            return fatherOccupation;
        }

        public FieldPlace getFatherResidence() {
            return fatherResidence;
        }

        public FieldSimpleValue getFatherComment() {
            return fatherComment;
        }

        public FieldSimpleValue getMotherLastName() {
            return motherLastName;
        }

        public FieldSimpleValue getMotherFirstName() {
            return motherFirstName;
        }

        public FieldAge getMotherAge() {
            return motherAge;
        }

        public FieldDead getMotherDead() {
            return motherDead;
        }

        public FieldOccupation getMotherOccupation() {
            return motherOccupation;
        }

        public FieldPlace getMotherResidence() {
            return motherResidence;
        }

        public FieldSimpleValue getMotherComment() {
            return motherComment;
        }
    }

    protected class Witness implements Cloneable {
        protected FieldSimpleValue firstName;
        protected FieldSimpleValue lastName;
        protected FieldOccupation occupation;
        protected FieldComment comment;

        @Override
        public Witness clone() throws CloneNotSupportedException {
            Witness object = (Witness) super.clone();

            if (firstName != null) {
                object.firstName = firstName.clone();
            }
            if (lastName != null) {
                object.lastName = lastName.clone();
            }
            if (occupation != null) {
                object.occupation = occupation.clone();
            }
            if (comment != null) {
                object.comment = comment.clone();
            }
            // je renvoie le clone
            return object;
        }

    }

    public Record() {
        eventDate = new FieldDate();
        cote = new FieldSimpleValue(); 
        parish = new FieldSimpleValue(); 
        freeComment = new FieldPicture();
        generalComment = new FieldComment();
        indi = new Participant();
        wife = new Participant();
        witness1 = new Witness();
        witness2 = new Witness();
        witness3 = new Witness();
        witness4 = new Witness();

    }

    public Participant getIndi() {
        return indi;
    }

    public Participant getWife() {
        return wife;
    }

    abstract public DataManager.RecordType getType();
    
    @Override
    public Record clone() {
        Record object = null;
        try {
            object = (Record) super.clone();

            if (eventDate != null) {
                object.eventDate = eventDate.clone();
            }
            if (cote != null) {
                object.cote = cote.clone();
            }
            if (parish != null) {
                object.parish = parish.clone();
            }
            if (freeComment != null) {
                object.freeComment = freeComment.clone();
            }
            if (generalComment != null) {
                object.generalComment = generalComment.clone();
            }

            if (indi != null) {
                object.indi = indi.clone();
            }

            if (wife != null) {
                object.wife = wife.clone();
            }

            if (witness1 != null) {
                object.witness1 = witness1.clone();
            }
            if (witness2 != null) {
                object.witness2 = witness2.clone();
            }
            if (witness3 != null) {
                object.witness3 = witness3.clone();
            }
            if (witness4 != null) {
                object.witness4 = witness4.clone();
            }
            
        } catch (CloneNotSupportedException cnse) {
            cnse.printStackTrace(System.err);
        }
        // je renvoie le clone
        return object;
    }

    public int getRecordNo() {
        return  recordNo;
    }

    public FieldEventType getEventType() {
        return  eventType;
    }

    public PropertyDate getEventDateProperty() {
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
   
    public FieldSimpleValue getCote() {
        return cote;
    }

    public FieldPicture getFreeComment() {
        return freeComment;
    }

    public FieldSimpleValue getGeneralComment() {
        return generalComment;
    }

    public FieldNotary getNotary() {
        return notary;
    }

    public FieldSimpleValue getParish() {
        return parish;
    }

    //  indi ///////////////////////////////////////////////////////////////////


    public FieldSimpleValue getIndiLastName() {
        return indi.lastName;
    }

    public FieldSimpleValue getIndiFirstName() {
        return indi.firstName;
    }

    public FieldSex getIndiSex() {
        return indi.sex;
    }

    public FieldAge getIndiAge() {
        return indi.age;
    }

    public FieldDate getIndiBirthDate() {
        return indi.birthDate;
    }

    public FieldPlace getIndiBirthPlace() {
        return indi.birthPlace;
    }

    public FieldOccupation getIndiOccupation() {
        return indi.occupation;
    }

    public FieldPlace getIndiResidence() {
        return indi.residence;
    }
    
    public FieldSimpleValue getIndiComment() {
        return indi.comment;
    }

    //  conjoint (ou ancien conjoint) //////////////////////////////////////////
    public FieldSimpleValue getIndiMarriedLastName() {
        return indi.marriedLastName;
    }

    public FieldSimpleValue getIndiMarriedFirstName() {
        return indi.marriedFirstName;
    }

//    public FieldSex getIndiMarriedSex() {
//        return indi.marriedSex;
//    }

    public FieldDead getIndiMarriedDead() {
        return indi.marriedDead;
    }

    public FieldOccupation getIndiMarriedOccupation() {
        return indi.marriedOccupation;
    }

    public FieldPlace getIndiMarriedResidence() {
        return indi.marriedResidence;
    }

    public FieldSimpleValue getIndiMarriedComment() {
        return indi.marriedComment;
    }

    //  indi father ////////////////////////////////////////////////////////////
    public FieldSimpleValue getIndiFatherLastName() {
        return indi.fatherLastName;
    }

    public FieldSimpleValue getIndiFatherFirstName() {
        return indi.fatherFirstName;
    }

    public FieldAge getIndiFatherAge() {
        return indi.fatherAge;
    }

    public FieldDead getIndiFatherDead() {
        return indi.fatherDead;
    }

    public FieldOccupation getIndiFatherOccupation() {
        return indi.fatherOccupation;
    }

    public FieldPlace getIndiFatherResidence() {
        return indi.fatherResidence;
    }

    public FieldSimpleValue getIndiFatherComment() {
        return indi.fatherComment;
    }


    public FieldSimpleValue getIndiMotherLastName() {
        return indi.motherLastName;
    }

    public FieldSimpleValue getIndiMotherFirstName() {
        return indi.motherFirstName;
    }

    public FieldAge getIndiMotherAge() {
        return indi.motherAge;
    }

    public FieldDead getIndiMotherDead() {
        return indi.motherDead;
    }

    public FieldOccupation getIndiMotherOccupation() {
        return indi.motherOccupation;
    }

    public FieldPlace getIndiMotherResidence() {
        return indi.motherResidence;
    }

    public FieldSimpleValue getIndiMotherComment() {
        return indi.motherComment;
    }

    //  wife ///////////////////////////////////////////////////////////////////
    public FieldSimpleValue getWifeLastName() {
        return wife.lastName;
    }

    public FieldSimpleValue getWifeFirstName() {
        return wife.firstName;
    }

    public FieldSex getWifeSex() {
        return wife.sex;
    }

    public FieldAge getWifeAge() {
        return wife.age;
    }

    public FieldDate getWifeBirthDate() {
        return wife.birthDate;
    }

    public FieldPlace getWifeBirthPlace() {
        return wife.birthPlace;
    }

    public FieldOccupation getWifeOccupation() {
        return wife.occupation;
    }

    public FieldPlace getWifeResidence() {
        return wife.residence;
    }

    public FieldSimpleValue getWifeComment() {
        return wife.comment;
    }

    //  wife.married ///////////////////////////////////////////////////////////
    public FieldSimpleValue getWifeMarriedLastName() {
        return wife.marriedLastName;
    }

    public FieldSimpleValue getWifeMarriedFirstName() {
        return wife.marriedFirstName;
    }

//    public FieldSex getWifeMarriedSex() {
//        return wife.marriedSex;
//    }

    public FieldDead getWifeMarriedDead() {
        return wife.marriedDead;
    }

    public FieldOccupation getWifeMarriedOccupation() {
        return wife.marriedOccupation;
    }

    public FieldPlace getWifeMarriedResidence() {
        return wife.marriedResidence;
    }

    public FieldSimpleValue getWifeMarriedComment() {
        return wife.marriedComment;
    }

    //  wife.father ///////////////////////////////////////////////////////////
    public FieldSimpleValue getWifeFatherLastName() {
        return wife.fatherLastName;
    }

    public FieldSimpleValue getWifeFatherFirstName() {
        return wife.fatherFirstName;
    }

    public FieldAge getWifeFatherAge() {
        return wife.fatherAge;
    }

    public FieldDead getWifeFatherDead() {
        return wife.fatherDead;
    }

    public FieldOccupation getWifeFatherOccupation() {
        return wife.fatherOccupation;
    }

    public FieldPlace getWifeFatherResidence() {
        return wife.fatherResidence;
    }

    public FieldSimpleValue getWifeFatherComment() {
        return wife.fatherComment;
    }

    public FieldSimpleValue getWifeMotherLastName() {
        return wife.motherLastName;
    }

    public FieldSimpleValue getWifeMotherFirstName() {
        return wife.motherFirstName;
    }

    public FieldAge getWifeMotherAge() {
        return wife.motherAge;
    }

    public FieldDead getWifeMotherDead() {
        return wife.motherDead;
    }

    public FieldOccupation getWifeMotherOccupation() {
        return wife.motherOccupation;
    }

    public FieldPlace getWifeMotherResidence() {
        return wife.motherResidence;
    }

    public FieldSimpleValue getWifeMotherComment() {
        return wife.motherComment;
    }

    // wintness ///////////////////////////////////////////////////////////////
    public FieldSimpleValue getWitness1LastName() {
        return witness1.lastName;
    }

    public FieldSimpleValue getWitness1FirstName() {
        return witness1.firstName;
    }

    public FieldOccupation getWitness1Occupation() {
        return witness1.occupation;
    }

    public FieldSimpleValue getWitness1Comment() {
        return witness1.comment;
    }

    public FieldSimpleValue getWitness2LastName() {
        return witness2.lastName;
    }

    public FieldSimpleValue getWitness2FirstName() {
        return witness2.firstName;
    }

    public FieldOccupation getWitness2Occupation() {
        return witness2.occupation;
    }

    public FieldSimpleValue getWitness2Comment() {
        return witness2.comment;
    }

    public FieldSimpleValue getWitness3LastName() {
        return witness3.lastName;
    }

    public FieldSimpleValue getWitness3FirstName() {
        return witness3.firstName;
    }

    public FieldOccupation getWitness3Occupation() {
        return witness3.occupation;
    }

    public FieldSimpleValue getWitness3Comment() {
        return witness3.comment;
    }

    public FieldSimpleValue getWitness4LastName() {
        return witness4.lastName;
    }

    public FieldSimpleValue getWitness4FirstName() {
        return witness4.firstName;
    }

    public FieldOccupation getWitness4Occupation() {
        return witness4.occupation;
    }

    public FieldSimpleValue getWitness4Comment() {
        return witness4.comment;
    }

//    public void setEventPlace(String juridictions) {
//        eventPlace.setValue(juridictions);
//    }

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
                    field = indi.firstName;
                    break;
                case indiLastName:
                    field = indi.lastName;
                    break;
                case indiSex:
                    field = indi.sex;
                    break;
                case indiAge:
                    field = indi.age;
                    break;
                case indiBirthDate:
                    field = indi.birthDate;
                    break;
                case indiBirthPlace:
                    field = indi.birthPlace;
                    break;
                case indiOccupation:
                    field = indi.occupation;
                    break;
                case indiResidence:
                    field = indi.residence;
                    break;
                case indiComment:
                    field = indi.comment;
                    break;
                //conjoint(ouancienconjoint)//////////////////////////////////////////
                case indiMarriedFirstName:
                    field = indi.marriedFirstName;
                    break;
                case indiMarriedLastName:
                    field = indi.marriedLastName;
                    break;
//                case indi.marriedSex:
//                    field = indi.marriedSex;
//                    break;
                case indiMarriedDead:
                    field = indi.marriedDead;
                    break;
                case indiMarriedOccupation:
                    field = indi.marriedOccupation;
                    break;
                case indiMarriedResidence:
                    field = indi.marriedResidence;
                    break;
                case indiMarriedComment:
                    field = indi.marriedComment;
                    break;
                //indifather////////////////////////////////////////////////////////////
                case indiFatherFirstName:
                    field = indi.fatherFirstName;
                    break;
                case indiFatherLastName:
                    field = indi.fatherLastName;
                    break;
                case indiFatherAge:
                    field = indi.fatherAge;
                    break;
                case indiFatherDead:
                    field = indi.fatherDead;
                    break;
                case indiFatherOccupation:
                    field = indi.fatherOccupation;
                    break;
                case indiFatherResidence:
                    field = indi.fatherResidence;
                    break;
                case indiFatherComment:
                    field = indi.fatherComment;
                    break;
                case indiMotherFirstName:
                    field = indi.motherFirstName;
                    break;
                case indiMotherLastName:
                    field = indi.motherLastName;
                    break;
                case indiMotherAge:
                    field = indi.motherAge;
                    break;
                case indiMotherDead:
                    field = indi.motherDead;
                    break;
                case indiMotherOccupation:
                    field = indi.motherOccupation;
                    break;
                case indiMotherResidence:
                    field = indi.motherResidence;
                    break;
                case indiMotherComment:
                    field = indi.motherComment;
                    break;
                //wife///////////////////////////////////////////////////////////////////
                case wifeFirstName:
                    field = wife.firstName;
                    break;
                case wifeLastName:
                    field = wife.lastName;
                    break;
                case wifeSex:
                    field = wife.sex;
                    break;
//case 	wifeDead	 :	 field = wifeDead	;  break;
                case wifeAge:
                    field = wife.age;
                    break;
                case wifeBirthDate:
                    field = wife.birthDate;
                    break;
                case wifePlace:
                    field = wife.birthPlace;
                    break;
                case wifeOccupation:
                    field = wife.occupation;
                    break;
                case wifeResidence:
                    field = wife.residence;
                    break;
                case wifeComment:
                    field = wife.comment;
                    break;
                //wife.married///////////////////////////////////////////////////////////			//wife.married///////////////////////////////////////////////////////////
                case wifeMarriedFirstName:
                    field = wife.marriedFirstName;
                    break;
                case wifeMarriedLastName:
                    field = wife.marriedLastName;
                    break;
                case wifeMarriedDead:
                    field = wife.marriedDead;
                    break;
//                case wife.marriedSex:
//                    field = wife.marriedSex;
//                    break;
                case wifeMarriedOccupation:
                    field = wife.marriedOccupation;
                    break;
                case wifeMarriedResidence:
                    field = wife.marriedResidence;
                    break;
                case wifeMarriedComment:
                    field = wife.marriedComment;
                    break;
                //wife.father///////////////////////////////////////////////////////////
                case wifeFatherFirstName:
                    field = wife.fatherFirstName;
                    break;
                case wifeFatherLastName:
                    field = wife.fatherLastName;
                    break;
                case wifeFatherAge:
                    field = wife.fatherAge;
                    break;
                case wifeFatherDead:
                    field = wife.fatherDead;
                    break;
                case wifeFatherOccupation:
                    field = wife.fatherOccupation;
                    break;
                case wifeFatherResidence:
                    field = wife.fatherResidence;
                    break;
                case wifeFatherComment:
                    field = wife.fatherComment;
                    break;
                case wifeMotherFirstName:
                    field = wife.motherFirstName;
                    break;
                case wifeMotherLastName:
                    field = wife.motherLastName;
                    break;
                case wifeMotherAge:
                    field = wife.motherAge;
                    break;
                case wifeMotherDead:
                    field = wife.motherDead;
                    break;
                case wifeMotherOccupation:
                    field = wife.motherOccupation;
                    break;
                case wifeMotherResidence:
                    field = wife.motherResidence;
                    break;
                case wifeMotherComment:
                    field = wife.motherComment;
                    break;
                //wintness///////////////////////////////////////////////////////////////
                case witness1FirstName:
                    field = witness1.firstName;
                    break;
                case witness1LastName:
                    field = witness1.lastName;
                    break;
                case witness1Occupation:
                    field = witness1.occupation;
                    break;
                case witness1Comment:
                    field = witness1.comment;
                    break;
                case witness2FirstName:
                    field = witness2.firstName;
                    break;
                case witness2LastName:
                    field = witness2.lastName;
                    break;
                case witness2Occupation:
                    field = witness2.occupation;
                    break;
                case witness2Comment:
                    field = witness2.comment;
                    break;
                case witness3FirstName:
                    field = witness3.firstName;
                    break;
                case witness3LastName:
                    field = witness3.lastName;
                    break;
                case witness3Occupation:
                    field = witness3.occupation;
                    break;
                case witness3Comment:
                    field = witness3.comment;
                    break;
                case witness4FirstName:
                    field = witness4.firstName;
                    break;
                case witness4LastName:
                    field = witness4.lastName;
                    break;
                case witness4Occupation:
                    field = witness4.occupation;
                    break;
                case witness4Comment:
                    field = witness4.comment;
                    break;
            }

        return field;
    }


   
    public void setEventType(String name) {
        eventType.setName(name);
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
        if (cote != null) {
            cote.setValue(value);
        }
    }

    public void setFreeComment(String value) {
        freeComment.setValue(value);
    }

    public void setGeneralComment(String value) {
        generalComment.setValue(value);
    }

    
    public void setNotary(String value) {
        if( notary != null ) {
            notary.setValue(value);
        }
    }

    public void setParish(String value) {
        parish.setValue(value);
    }

    ///////////////////////////////////////////////////////////////////////////
    public void setIndi(String firstName, String lastName, String stringSexe,
            String stringAge, String stringBirthDate,
            String stringBirthPlace, String profession, String residence, String comment) {

        indi.firstName.setValue(firstName.trim());
        indi.lastName.setValue(lastName.trim());
        indi.sex.setValue(stringSexe.trim());
        if (indi.age != null) {
            // l'age n'est pas utilisé pour une naissance
            indi.age.setValue(stringAge.trim());
        }
        if (indi.birthDate != null) {
            // la date de naissance n'est pas utilisée pour une naissance car c'est la même que la date de l'evenement
            indi.birthDate.setValue(stringBirthDate.trim());
        }
        if (indi.birthPlace != null) {
            // le lieu n'est pas utilisée pour une naissance
            indi.birthPlace.setValue(stringBirthPlace.trim());
        }
        if (indi.occupation != null) {
            // la profession n'est pas utilisée pour une naissance
            indi.occupation.setValue(profession.trim());
        }
        if (indi.residence != null) {
            // la profession n'est pas utilisée pour une naissance
            indi.residence.setValue(residence.trim());
        }
        indi.comment.setValue(comment.trim());
    }

    public void setIndiMarried(String firstName, String lastName, /*String stringSexe, */String profession, String residence, String comment, String dead) {
        indi.marriedFirstName.setValue(firstName.trim());
        indi.marriedLastName.setValue(lastName.trim());
        //indi.marriedSex.setValue(stringSexe.trim());
        indi.marriedOccupation.setValue(profession.trim());
        indi.marriedResidence.setValue(residence.trim());
        indi.marriedComment.setValue(comment.trim());
        if (indi.marriedDead != null) {
            indi.marriedDead.setValue(dead.trim());
        }
    }

    public void setIndiFather(String firstName, String lastName, String profession, String residence, String comment, String dead, String age) {
        indi.fatherFirstName.setValue(firstName.trim());
        indi.fatherLastName.setValue(lastName.trim());
        indi.fatherOccupation.setValue(profession.trim());
        if (indi.fatherResidence != null) {
            indi.fatherResidence.setValue(residence.trim());
        }
        indi.fatherComment.setValue(comment.trim());
        indi.fatherDead.setValue(dead.trim());
        indi.fatherAge.setValue(age.trim());
    }

    public void setIndiMother(String firstName, String lastName, String profession, String residence, String comment, String dead, String age) {
        indi.motherFirstName.setValue(firstName.trim());
        indi.motherLastName.setValue(lastName.trim());
        indi.motherOccupation.setValue(profession.trim());
        if (indi.motherResidence != null) {
            indi.motherResidence.setValue(residence.trim());
        }
        indi.motherComment.setValue(comment.trim());
        indi.motherDead.setValue(dead.trim());
        indi.motherAge.setValue(age.trim());
    }

    public void setWife(String firstName, String lastName, String sex,
            String stringAge, String stringBirthDate,
            String stringBirthPlace, String profession, String residence, String comment) {
        wife.firstName.setValue(firstName.trim());
        wife.lastName.setValue(lastName.trim());
        if (wife.sex != null) {
            // le sexe n'est utilise que dans les actes divers
            wife.sex.setValue(sex.trim());
        }
        wife.age.setValue(stringAge.trim());
        wife.birthDate.setValue(stringBirthDate.trim());
        wife.birthPlace.setValue(stringBirthPlace.trim());
        wife.occupation.setValue(profession.trim());
        wife.residence.setValue(residence.trim());
        wife.comment.setValue(comment.trim());
    }

    public void setWifeMarried(String firstName, String lastName, /*String stringSexe, */String profession, String residence, String comment, String dead) {
        wife.marriedFirstName.setValue(firstName.trim());
        wife.marriedLastName.setValue(lastName.trim());
        //wife.marriedSex.setValue(stringSexe.trim());
        wife.marriedOccupation.setValue(profession.trim());
        wife.marriedResidence.setValue(residence.trim());
        wife.marriedComment.setValue(comment.trim());
        wife.marriedDead.setValue(dead.trim());
    }

    public void setWifeFather(String firstName, String lastName, String profession, String residence, String comment, String dead, String age) {
        wife.fatherFirstName.setValue(firstName.trim());
        wife.fatherLastName.setValue(lastName.trim());
        wife.fatherOccupation.setValue(profession.trim());
        wife.fatherResidence.setValue(residence.trim());
        wife.fatherComment.setValue(comment.trim());
        wife.fatherDead.setValue(dead.trim());
        wife.fatherAge.setValue(age.trim());
    }

    public void setWifeMother(String firstName, String lastName, String profession, String residence, String comment, String dead, String age) {
        wife.motherFirstName.setValue(firstName.trim());
        wife.motherLastName.setValue(lastName.trim());
        wife.motherOccupation.setValue(profession.trim());
        wife.motherResidence.setValue(residence.trim());
        wife.motherComment.setValue(comment.trim());
        wife.motherDead.setValue(dead.trim());
        wife.motherAge.setValue(age.trim());
    }

    public void setWitness1(String firstName, String lastName, String profession, String comment) {
        witness1.firstName.setValue(firstName.trim());
        witness1.lastName.setValue(lastName.trim());
        witness1.occupation.setValue(profession.trim());
        witness1.comment.setValue(comment.trim());
    }

    public void setWitness2(String firstName, String lastName, String profession, String comment) {
        witness2.firstName.setValue(firstName.trim());
        witness2.lastName.setValue(lastName.trim());
        witness2.occupation.setValue(profession.trim());
        witness2.comment.setValue(comment.trim());
    }

    public void setWitness3(String firstName, String lastName, String profession, String comment) {
        witness3.firstName.setValue(firstName.trim());
        witness3.lastName.setValue(lastName.trim());
        witness3.occupation.setValue(profession.trim());
        witness3.comment.setValue(comment.trim());
    }

    public void setWitness4(String firstName, String lastName, String profession, String comment) {
        witness4.firstName.setValue(firstName.trim());
        witness4.lastName.setValue(lastName.trim());
        witness4.occupation.setValue(profession.trim());
        witness4.comment.setValue(comment.trim());
    }

}