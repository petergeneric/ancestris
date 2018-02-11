package ancestris.modules.releve.model;

import ancestris.modules.releve.model.Field.FieldType;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.Calendar;

/**
 *
 * @author Michel
 */
public abstract class Record {
    
    public enum RecordType { BIRTH, MARRIAGE, DEATH, MISC }

    protected FieldEventType eventType;
    //protected FieldPlace eventPlace;
    protected FieldSimpleValue cote;
    protected FieldPicture freeComment;
    protected FieldDate eventDate;
    protected FieldDate secondDate;
    protected FieldComment generalComment;
    protected FieldNotary notary;
    protected FieldSimpleValue parish;
    protected Participant indi;
    protected Participant wife;
    protected Witness witness1;
    protected Witness witness2;
    protected Witness witness3;
    protected Witness witness4;
    public static final int WITNESS_NB = 4;
    protected Witness[] witnesses = new Witness[WITNESS_NB]; 

    public Record() {
        eventDate = new FieldDate();
        secondDate = new FieldDate();
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
        witnesses[0] = witness1;
        witnesses[1] = witness2;
        witnesses[2] = witness3;
        witnesses[3] = witness4;
    }

    public Participant getIndi() {
        return indi;
    }

    public Participant getWife() {
        return wife;
    }
    
    public Witness[] getWitnesses() {
        return witnesses;
    }
    
    public Witness getWitness1() {
        return witness1;
    }
    public Witness getWitness2() {
        return witness2;
    }
    public Witness getWitness3() {
        return witness3;
    }
    public Witness getWitness4() {
        return witness4;
    }
    
    abstract public RecordType getType();
    
    public FieldEventType getEventType() {
        return  eventType;
    }

    public PropertyDate getEventDateProperty() {
        return eventDate.getPropertyDate();
    }
    
    public Calendar getEventDateCalendar() {
        return eventDate.getCalendar();
    }
    
    public FieldDate getEventDate() {
        return eventDate;
    }

    public String getEventDateString() {
        return eventDate.getValue();
    }

    public FieldDate getEventSecondDate() {
        return secondDate;
    }
    
    public PropertyDate getEventSecondDateProperty() {
        return secondDate.getPropertyDate();
    }

    public String getEventSecondDateString() {
        return secondDate.getValue();
    }

    public Calendar getEventSecondDateCalendar() {
        return secondDate.getCalendar();
    }
   
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
    
    public void setEventType(String name) {
        eventType.setName(name);
    }

    public void setEventDate(String dateString) {
        eventDate.setValue(dateString);
    }

    public void setEventCalendar(Calendar calendar) {
        if ( calendar != null) {
            eventDate.setCalendar(calendar);
        }
    }

    public void setEventDate(String strDay, String strMonth, String strYear) throws NumberFormatException {
        eventDate.setValue(strDay, strMonth, strYear);
    }

    public void setSecondDate(String dateString) {
        secondDate.setValue(dateString);
    }

    public void setSecondDate(String strDay, String strMonth, String strYear) throws NumberFormatException {
        secondDate.setValue(strDay, strMonth, strYear);
    }

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
                case secondDate:
                    field = secondDate;
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
                case indiBirthAddress:
                    field = indi.birthAddress;
                    break;
                case indiOccupation:
                    field = indi.occupation;
                    break;
                case indiResidence:
                    field = indi.residence;
                    break;
                case indiAddress:
                    field = indi.address;
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
                case indiMarriedAddress:
                    field = indi.marriedAddress;
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
                case indiFatherAddress:
                    field = indi.fatherAddress;
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
                case indiMotherAddress:
                    field = indi.motherAddress;
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
                case wifeBirthPlace:
                    field = wife.birthPlace;
                    break;
                case wifeBirthAddress:
                    field = wife.birthAddress;
                    break;
                case wifeOccupation:
                    field = wife.occupation;
                    break;
                case wifeResidence:
                    field = wife.residence;
                    break;
                case wifeAddress:
                    field = wife.address;
                    break;
                case wifeComment:
                    field = wife.comment;
                    break;
                //wife.married///////////////////////////////////////////////////////////
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
                case wifeMarriedAddress:
                    field = wife.marriedAddress;
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
                case wifeFatherAddress:
                    field = wife.fatherAddress;
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
                case wifeMotherAddress:
                    field = wife.motherAddress;
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

    public class Participant {
        protected FieldSimpleValue firstName;
        protected FieldSimpleValue lastName;
        protected FieldSex sex;
        protected FieldAge age;
        protected FieldDate birthDate;
        protected FieldPlace birthPlace;
        protected FieldAddress birthAddress;
        protected FieldOccupation occupation;
        protected FieldPlace residence;
        protected FieldAddress address;
        protected FieldComment comment;
        protected FieldSimpleValue marriedFirstName;
        protected FieldSimpleValue marriedLastName;
        //protected FieldSex marriedSex;
        protected FieldComment marriedComment;
        protected FieldOccupation marriedOccupation;
        protected FieldPlace marriedResidence;
        protected FieldAddress marriedAddress;
        protected FieldDead marriedDead;
        protected FieldSimpleValue fatherFirstName;
        protected FieldSimpleValue fatherLastName;
        protected FieldOccupation fatherOccupation;
        protected FieldPlace fatherResidence;
        protected FieldAddress fatherAddress;        
        protected FieldAge fatherAge;
        protected FieldDead fatherDead;
        protected FieldComment fatherComment;
        protected FieldSimpleValue motherFirstName;
        protected FieldSimpleValue motherLastName;
        protected FieldOccupation motherOccupation;
        protected FieldPlace motherResidence;
        protected FieldAddress motherAddress; 
        protected FieldAge motherAge;
        protected FieldDead motherDead;
        protected FieldComment motherComment;

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
        
        public FieldAddress getBirthAddress() {
            return birthAddress;
        }

        public FieldOccupation getOccupation() {
            return occupation;
        }

        public FieldPlace getResidence() {
            return residence;
        }
        
        public FieldAddress getAddress() {
            return address;
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
        
        public FieldAddress getMarriedAddress() {
            return marriedAddress;
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

        public FieldAddress getFatherAddress() {
            return fatherAddress;
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

        public FieldAddress getMotherAddress() {
            return motherAddress;
        }
        
        public FieldSimpleValue getMotherComment() {
            return motherComment;
        }
        
        public void set(String stringFirstName, String stringLastName, String stringSexe,
                String stringAge, String stringBirthDate, String stringBirthPlace, String stringBirthAddress,
                String stringProfession, String stringResidence, String stringAddress, String stringComment) {

            firstName.setValue(stringFirstName.trim());
            lastName.setValue(stringLastName.trim());
            sex.setValue(stringSexe.trim());
            if (age != null) {
                // l'age n'est pas utilisé pour une naissance
                age.setValue(stringAge.trim());
            }
            if (birthDate != null) {
                birthDate.setValue(stringBirthDate.trim());
            }
            if (birthPlace != null) {
                // le lieu n'est pas utilisée pour une naissance
                birthPlace.setValue(stringBirthPlace.trim());
            }
            if (birthAddress != null) {
                // le lieu n'est pas utilisée pour une naissance
                birthAddress.setValue(stringBirthAddress.trim());
            }
            if (occupation != null) {
                // la profession n'est pas utilisée pour une naissance
                occupation.setValue(stringProfession.trim());
            }
            if (residence != null) {
                // la profession n'est pas utilisée pour une naissance
                residence.setValue(stringResidence.trim());
            }
            if (address != null) {
                // le lieu n'est pas utilisée pour une naissance
                address.setValue(stringAddress.trim());
            }
            comment.setValue(stringComment.trim());
        }
        
        public void setMarried(String inFirstName, String inLastName, /*String stringSexe, */ 
                String inProfession, String inResidence, String inAddress, String inComment, String inDead) {
            
            marriedFirstName.setValue(inFirstName.trim());
            marriedLastName.setValue(inLastName.trim());
            //marriedSex.setValue(stringSexe.trim());
            marriedOccupation.setValue(inProfession.trim());
            marriedResidence.setValue(inResidence.trim());
            marriedAddress.setValue(inAddress.trim());
            marriedComment.setValue(inComment.trim());
            if (marriedDead != null) {
                marriedDead.setValue(inDead.trim());
            }
        }
        
        public void setFather(String inFirstName, String inLastName, String inProfession, String inResidence, String inAddress, String inComment, String inDead, String inAge) {
            fatherFirstName.setValue(inFirstName.trim());
            fatherLastName.setValue(inLastName.trim());
            fatherOccupation.setValue(inProfession.trim());
            if (fatherResidence != null) {
                fatherResidence.setValue(inResidence.trim());
            }
            if (fatherAddress != null) {
                fatherAddress.setValue(inAddress.trim());
            }
            fatherComment.setValue(inComment.trim());
            fatherDead.setValue(inDead.trim());
            fatherAge.setValue(inAge.trim());
        }

        public void setMother(String inFirstName, String inLastName, String inProfession, String inResidence, String inAddress, String inComment, String inDead, String inAge) {
            motherFirstName.setValue(inFirstName.trim());
            motherLastName.setValue(inLastName.trim());
            motherOccupation.setValue(inProfession.trim());
            if (motherResidence != null) {
                motherResidence.setValue(inResidence.trim());
            }
            if (motherAddress != null) {
                motherAddress.setValue(inAddress.trim());
            }
            motherComment.setValue(inComment.trim());
            motherDead.setValue(inDead.trim());
            motherAge.setValue(inAge.trim());
        }
    }

    public class Witness {
        protected FieldSimpleValue firstName;
        protected FieldSimpleValue lastName;
        protected FieldOccupation occupation;
        protected FieldComment comment;

        public FieldSimpleValue getLastName() {
            return lastName;
        }

        public FieldSimpleValue getFirstName() {
            return firstName;
        }

        public FieldOccupation getOccupation() {
            return occupation;
        }

        public FieldSimpleValue getComment() {
            return comment;
        }
        
        public void setValue(String firstName, String lastName, String profession, String comment) {
            this.firstName.setValue(firstName.trim());
            this.lastName.setValue(lastName.trim());
            this.occupation.setValue(profession.trim());
            this.comment.setValue(comment.trim());
        }

    }
}