package ancestris.modules.releve.model;

import ancestris.modules.releve.model.Field.FieldType;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.Calendar;

/**
 *
 * @author Michel
 */
public abstract class Record implements Cloneable{

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
    Witness[] witnesses = new Witness[WITNESS_NB]; 

    public class Participant implements Cloneable {
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

        @Override
        public Participant clone() throws CloneNotSupportedException {
            Participant object = (Participant) super.clone();

            if (firstName != null) {
                object.firstName = firstName.clone();
            }
            if (lastName != null) {
                object.lastName = lastName.clone();
            }
            if (sex != null) {
                object.sex = sex.clone();
            }
            if (age != null) {
                object.age = age.clone();
            }
            if (birthDate != null) {
                object.birthDate = birthDate.clone();
            }
            if (birthPlace != null) {
                object.birthPlace = birthPlace.clone();
            }
            if (birthAddress != null) {
                object.birthAddress = birthAddress.clone();
            }
            if (occupation != null) {
                object.occupation = occupation.clone();
            }
            if (residence != null) {
                object.residence = residence.clone();
            }
            if (address != null) {
                object.address = address.clone();
            }
            if (comment != null) {
                object.comment = comment.clone();
            }
            
            if (marriedFirstName != null) {
                object.marriedFirstName = marriedFirstName.clone();
            }
            if (marriedLastName != null) {
                object.marriedLastName = marriedLastName.clone();
            }
            if (marriedDead != null) {
                object.marriedDead = marriedDead.clone();
            }
            if (marriedOccupation != null) {
                object.marriedOccupation = marriedOccupation.clone();
            }
            if (marriedResidence != null) {
                object.marriedResidence = marriedResidence.clone();
            }
            if (marriedAddress != null) {
                object.marriedAddress = marriedAddress.clone();
            }
            if (marriedComment != null) {
                object.marriedComment = marriedComment.clone();
            }

            if (fatherFirstName != null) {
                object.fatherFirstName = fatherFirstName.clone();
            }
            if (fatherLastName != null) {
                object.fatherLastName = fatherLastName.clone();
            }
            if (fatherAge != null) {
                object.fatherAge = fatherAge.clone();
            }
            if (fatherDead != null) {
                object.fatherDead = fatherDead.clone();
            }
            if (fatherOccupation != null) {
                object.fatherOccupation = fatherOccupation.clone();
            }
            if (fatherResidence != null) {
                object.fatherResidence = fatherResidence.clone();
            }
            if (fatherAddress != null) {
                object.fatherAddress = fatherAddress.clone();
            }
            if (fatherComment != null) {
                object.fatherComment = fatherComment.clone();
            }

            if (motherFirstName != null) {
                object.motherFirstName = motherFirstName.clone();
            }
            if (motherLastName != null) {
                object.motherLastName = motherLastName.clone();
            }
            if (motherAge != null) {
                object.motherAge = motherAge.clone();
            }
            if (motherDead != null) {
                motherDead = motherDead.clone();
            }
            if (motherOccupation != null) {
                object.motherOccupation = motherOccupation.clone();
            }
            if (motherResidence != null) {
                object.motherResidence = motherResidence.clone();
            }
            if (motherAddress != null) {
                object.motherAddress = motherAddress.clone();
            }
            if (motherComment != null) {
                object.motherComment = motherComment.clone();
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

    public class Witness implements Cloneable {
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
    

    abstract public DataManager.RecordType getType();
    
    @Override
    public Record clone() throws CloneNotSupportedException {
        Record object = null;
        object = (Record) super.clone();

        if (eventDate != null) {
            object.eventDate = eventDate.clone();
        }
        if (secondDate != null) {
            object.secondDate = secondDate.clone();
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
                        
        object.witness1 = witness1.clone();
        object.witness2 = witness2.clone();
        object.witness3 = witness3.clone();
        object.witness4 = witness4.clone();
        object.witnesses = witnesses.clone();
        for( int i=0 ; i<WITNESS_NB;  i++) {
            object.witnesses[i] = witnesses[i].clone(); 
        }

        // je renvoie le clone
        return object;
    }

    public FieldEventType getEventType() {
        return  eventType;
    }

    public PropertyDate getEventDateProperty() {
        return eventDate.getPropertyDate();
    }
    
    public Calendar getEventDateCalendar() {
        return eventDate.getCalendar();
    }
    
    public String getEventDateString() {
        return eventDate.getValue();
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
    
    public FieldAddress getIndiBirthAddress() {
        return indi.birthAddress;
    }

    public FieldOccupation getIndiOccupation() {
        return indi.occupation;
    }

    public FieldPlace getIndiResidence() {
        return indi.residence;
    }
    
    public FieldAddress getIndiAddress() {
        return indi.address;
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

    public FieldAddress getIndiMarriedAddress() {
        return indi.marriedAddress;
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
    
    public FieldAddress getIndiFatherAddress() {
        return indi.fatherAddress;
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

    public FieldAddress getIndiMotherAddress() {
        return indi.motherAddress;
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
    
    public FieldAddress getWifeBirthAddress() {
        return wife.birthAddress;
    }    

    public FieldOccupation getWifeOccupation() {
        return wife.occupation;
    }

    public FieldPlace getWifeResidence() {
        return wife.residence;
    }
    
    public FieldAddress getWifeAddress() {
        return wife.address;
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

    public FieldAddress getWifeMarriedAddress() {
        return wife.marriedAddress;
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

    public FieldAddress getWifeFatherAddress() {
        return wife.fatherAddress;
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
    
    public FieldAddress getWifeMotherAddress() {
        return wife.motherAddress;
    }    

    public FieldSimpleValue getWifeMotherComment() {
        return wife.motherComment;
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
                    field = witnesses[0].firstName;
                    break;
                case witness1LastName:
                    field = witnesses[0].lastName;
                    break;
                case witness1Occupation:
                    field = witnesses[0].occupation;
                    break;
                case witness1Comment:
                    field = witnesses[0].comment;
                    break;
                case witness2FirstName:
                    field = witnesses[1].firstName;
                    break;
                case witness2LastName:
                    field = witnesses[1].lastName;
                    break;
                case witness2Occupation:
                    field = witnesses[1].occupation;
                    break;
                case witness2Comment:
                    field = witnesses[1].comment;
                    break;
                case witness3FirstName:
                    field = witnesses[2].firstName;
                    break;
                case witness3LastName:
                    field = witnesses[2].lastName;
                    break;
                case witness3Occupation:
                    field = witnesses[2].occupation;
                    break;
                case witness3Comment:
                    field = witnesses[2].comment;
                    break;
                case witness4FirstName:
                    field = witnesses[3].firstName;
                    break;
                case witness4LastName:
                    field = witnesses[3].lastName;
                    break;
                case witness4Occupation:
                    field = witnesses[3].occupation;
                    break;
                case witness4Comment:
                    field = witnesses[3].comment;
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

}