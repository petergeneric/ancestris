package ancestris.modules.releve.merge;

import ancestris.modules.releve.dnd.TransferableRecord;
import ancestris.modules.releve.model.RecordInfoPlace;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import org.openide.util.Exceptions;

/**
 * Cette classe encapsule un objet de la classe Record et ajoute des méthodes
 * pour calculer les dates de naissance , mariage , deces et des methodes pour
 * assembler les commentaires.
 */
public class MergeRecord {

    /**
     * liste des types de releves
     */
    protected static enum RecordType {
        BIRTH,
        MARRIAGE,
        DEATH,
        MISC
    }

    /**
     * liste des types de releves
     */
    protected static enum EventTypeTag {
        BIRT,
        DEAT,
        MARR,
        MARC,
        MARB,
        MARL,
        WILL,
        EVEN
    }

    public static String deadLabel = java.util.ResourceBundle.getBundle("ancestris/modules/releve/merge/Bundle").getString("MergeModel.DeadState.Dead");
    public static String aliveLabel = java.util.ResourceBundle.getBundle("ancestris/modules/releve/merge/Bundle").getString("MergeModel.DeadState.Alive");
    public static String unknownLabel = java.util.ResourceBundle.getBundle("ancestris/modules/releve/merge/Bundle").getString("MergeModel.DeadState.Unknown");

    protected static enum DeadState {
        UNKNOWN,
        DEAD,
        ALIVE;

        public static DeadState fromString(String value) {
            DeadState death = UNKNOWN;
            if (DeadState.DEAD.name().equals(value)) {
                death = DeadState.DEAD;
            } else if (value.equals(DeadState.ALIVE.name())) {
                death = DeadState.ALIVE;
            }
            return death;
        }

        @Override
        public String toString() {
            switch (this) {
                case DEAD:
                    return deadLabel;
                case ALIVE:
                    return aliveLabel;
                default:
                    return "";
            }
        }
    }

    protected static enum SexType {
        U,
        M,
        F;

        public static SexType fromString(String value) {
            SexType sex = U;
            if (SexType.M.name().equals(value)) {
                sex = SexType.M;
            } else if (value.equals(SexType.F.name())) {
                sex = SexType.F;
            }
            return sex;
        }

        public int getSex() {
            switch (this) {
                case M:
                    return 1;
                case F:
                    return 2;
                default:
                    return 0;
            }
        }

    }

    public enum MergeParticipantType {
        participant1,
        participant2
    };

    private final RecordInfoPlace recordInfoPlace = new RecordInfoPlace();
    private final String fileName;
    private String eventSourceTitle;
    //private final Record        record;

    private final RecordType recordType;
    private final EventTypeTag eventTypeTag;

    private final String eventType;
    private final String eventCote;
    private final String freeComment;
    private final PropertyDate eventDate;
    private final PropertyDate eventSecondDate;
    private final String generalComment;
    private final String notary;
    private final String parish;

    private final MergeParticipant partipant1;
    private final MergeParticipant partipant2;
    private final MergeWitness witness1 = new MergeWitness();
    private final MergeWitness witness2 = new MergeWitness();
    private final MergeWitness witness3 = new MergeWitness();
    private final MergeWitness witness4 = new MergeWitness();

    /**
     * type du releve
     */
    MergeRecord(TransferableRecord.TransferableData data) {

        fileName = data.fileName;

        recordInfoPlace.setLocality(data.locality);
        recordInfoPlace.setCityName(data.cityName);
        recordInfoPlace.setCityCode(data.cityCode);
        recordInfoPlace.setCountyName(data.countyName);
        recordInfoPlace.setStateName(data.stateName);
        recordInfoPlace.setCountryName(data.countryName);

        
        if (data.recordType.equals(RecordType.BIRTH.name())) {
            recordType = RecordType.BIRTH;
            eventTypeTag = EventTypeTag.BIRT;
        } else if (data.recordType.equals(RecordType.MARRIAGE.name())) {
            recordType = RecordType.MARRIAGE;
            eventTypeTag = EventTypeTag.MARR;
        } else if (data.recordType.equals(RecordType.DEATH.name())) {
            recordType = RecordType.DEATH;
            eventTypeTag = EventTypeTag.DEAT;
        } else {
            recordType = RecordType.MISC;
            String lowerCaseEventType = data.eventType.toLowerCase();
            if (lowerCaseEventType.equals("cm") || lowerCaseEventType.contains("mariage")) {
                if (lowerCaseEventType.contains("ban") || lowerCaseEventType.contains("publication")) {
                    eventTypeTag = EventTypeTag.MARB;
                } else if (lowerCaseEventType.contains("certificat")) {
                    eventTypeTag = EventTypeTag.MARL;
                } else {
                    eventTypeTag = EventTypeTag.MARC;
                }
            } else if (lowerCaseEventType.equals("testament")) {
                eventTypeTag = EventTypeTag.WILL;
            } else {
                eventTypeTag = EventTypeTag.EVEN;
            }
        }

        eventType = data.eventType;
        eventCote = data.cote;
        freeComment = data.freeComment;
        eventDate = data.eventDate;
        eventSecondDate = data.secondDate;
        generalComment = data.generalComment;
        notary = data.notary;
        parish = data.parish;

        partipant1 = new MergeParticipant(MergeParticipantType.participant1);
        partipant1.m_FirstName = data.participant1.firstName;
        partipant1.m_LastName = data.participant1.lastName;
        partipant1.m_Sex = SexType.fromString(data.participant1.sex);
        partipant1.m_Age = data.participant1.age;
        partipant1.m_BirthDate = data.participant1.birthDate;
        partipant1.m_BirthPlace = data.participant1.birthPlace;
        partipant1.m_BirthAddress = data.participant1.birthAddress;
        partipant1.m_Occupation = data.participant1.occupation;
        partipant1.m_Residence = data.participant1.residence;
        partipant1.m_Address = data.participant1.address;
        partipant1.m_Comment = data.participant1.comment;
        partipant1.m_MarriedFirstName = data.participant1.marriedFirstName;
        partipant1.m_MarriedLastName = data.participant1.marriedLastName;
        partipant1.m_MarriedComment = data.participant1.marriedComment;
        partipant1.m_MarriedOccupation = data.participant1.marriedOccupation;
        partipant1.m_MarriedResidence = data.participant1.marriedResidence;
        partipant1.m_MarriedAddress = data.participant1.marriedAddress;
        partipant1.m_MarriedDead = DeadState.fromString(data.participant1.marriedDead);
        partipant1.m_FatherFirstName = data.participant1.fatherFirstName;
        partipant1.m_FatherLastName = data.participant1.fatherLastName;
        partipant1.m_FatherOccupation = data.participant1.fatherOccupation;
        partipant1.m_FatherResidence = data.participant1.fatherResidence;
        partipant1.m_FatherAddress = data.participant1.fatherAddress;
        partipant1.m_FatherAge = data.participant1.fatherAge;
        partipant1.m_FatherDead = DeadState.fromString(data.participant1.fatherDead);
        partipant1.m_FatherComment = data.participant1.fatherComment;
        partipant1.m_MotherFirstName = data.participant1.motherFirstName;
        partipant1.m_MotherLastName = data.participant1.motherLastName;
        partipant1.m_MotherOccupation = data.participant1.motherOccupation;
        partipant1.m_MotherResidence = data.participant1.motherResidence;
        partipant1.m_MotherAddress = data.participant1.motherAddress;
        partipant1.m_MotherAge = data.participant1.motherAge;
        partipant1.m_MotherDead = DeadState.fromString(data.participant1.motherDead);
        partipant1.m_MotherComment = data.participant1.motherComment;

        partipant2 = new MergeParticipant(MergeParticipantType.participant2);
        partipant2.m_FirstName = data.participant2.firstName;
        partipant2.m_LastName = data.participant2.lastName;
        partipant2.m_Sex = SexType.fromString(data.participant2.sex);
        partipant2.m_Age = data.participant2.age;
        partipant2.m_BirthDate = data.participant2.birthDate;
        partipant2.m_BirthPlace = data.participant2.birthPlace;
        partipant2.m_BirthAddress = data.participant2.birthAddress;
        partipant2.m_Occupation = data.participant2.occupation;
        partipant2.m_Residence = data.participant2.residence;
        partipant2.m_Address = data.participant2.address;
        partipant2.m_Comment = data.participant2.comment;
        partipant2.m_MarriedFirstName = data.participant2.marriedFirstName;
        partipant2.m_MarriedLastName = data.participant2.marriedLastName;
        partipant2.m_MarriedComment = data.participant2.marriedComment;
        partipant2.m_MarriedOccupation = data.participant2.marriedOccupation;
        partipant2.m_MarriedResidence = data.participant2.marriedResidence;
        partipant2.m_MarriedAddress = data.participant2.marriedAddress;
        partipant2.m_MarriedDead = DeadState.fromString(data.participant2.marriedDead);
        partipant2.m_FatherFirstName = data.participant2.fatherFirstName;
        partipant2.m_FatherLastName = data.participant2.fatherLastName;
        partipant2.m_FatherOccupation = data.participant2.fatherOccupation;
        partipant2.m_FatherResidence = data.participant2.fatherResidence;
        partipant2.m_FatherAddress = data.participant2.fatherAddress;
        partipant2.m_FatherAge = data.participant2.fatherAge;
        partipant2.m_FatherDead = DeadState.fromString(data.participant2.fatherDead);
        partipant2.m_FatherComment = data.participant2.fatherComment;
        partipant2.m_MotherFirstName = data.participant2.motherFirstName;
        partipant2.m_MotherLastName = data.participant2.motherLastName;
        partipant2.m_MotherOccupation = data.participant2.motherOccupation;
        partipant2.m_MotherResidence = data.participant2.motherResidence;
        partipant2.m_MotherAddress = data.participant2.motherAddress;
        partipant2.m_MotherAge = data.participant2.motherAge;
        partipant2.m_MotherDead = DeadState.fromString(data.participant2.motherDead);
        partipant2.m_MotherComment = data.participant2.motherComment;

        witness1.firstName = data.witness1.firstName;
        witness1.lastName = data.witness1.lastName;
        witness1.occupation = data.witness1.occupation;
        witness1.comment = data.witness1.comment;

        witness2.firstName = data.witness2.firstName;
        witness2.lastName = data.witness2.lastName;
        witness2.occupation = data.witness2.occupation;
        witness2.comment = data.witness2.comment;

        witness3.firstName = data.witness3.firstName;
        witness3.lastName = data.witness3.lastName;
        witness3.occupation = data.witness3.occupation;
        witness3.comment = data.witness3.comment;

        witness4.firstName = data.witness4.firstName;
        witness4.lastName = data.witness4.lastName;
        witness4.occupation = data.witness4.occupation;
        witness4.comment = data.witness4.comment;

    }

//
//     /**
//     * constructeur
//     * @param record
//     */
//    protected MergeRecord( RecordInfoPlace recordsInfoPlace, String fileName, Record record)  {
//        this.recordInfoPlace = recordsInfoPlace;
//        if (fileName != null) {
//            this.fileName = fileName;
//        } else {
//            this.fileName = "";
//        }
//        calculateSourceTitle();
//
//        //this.record = record;
//        if (record instanceof RecordBirth) {
//            recordType = RecordType.Birth;
//            eventTypeTag = EventTypeTag.BIRT;
//        } else if (record instanceof RecordMarriage) {
//            recordType = RecordType.Marriage;
//            eventTypeTag = EventTypeTag.MARR;
//        } else if (record instanceof RecordDeath) {
//            recordType = RecordType.Death;
//            eventTypeTag = EventTypeTag.DEAT;
//        } else {
//            recordType = RecordType.Misc;
//            String eventType = record.getEventType().toString().toLowerCase();
//            if (eventType.equals("cm") || eventType.indexOf("mariage") != -1) {
//                if ( eventType.contains("ban") || eventType.contains("publication")) {
//                    eventTypeTag = EventTypeTag.MARB;
//                } else if ( eventType.contains("certificat") ) {
//                    eventTypeTag = EventTypeTag.MARL;
//                } else{
//                    eventTypeTag = EventTypeTag.MARC;
//                }
//            } else if (record.getEventType().toString().toLowerCase().equals("testament")) {
//                eventTypeTag = EventTypeTag.WILL;
//            } else {
//                eventTypeTag = EventTypeTag.EVEN;
//            }
//        }
//
//        partipant1 = new MergeParticipant(MergeParticipantType.participant1, record.getIndi());
//        partipant2 = new MergeParticipant(MergeParticipantType.participant2, record.getWife());
//
//    }
    ///////////////////////////////////////////////////////////////////////////
    // accesseurs
    //////////////////////////////////////////////////////////////////////////
    MergeParticipant getIndi() {
        return partipant1;
    }

    MergeParticipant getWife() {
        return partipant2;
    }

    MergeParticipant getParticipant(MergeParticipantType mergeParticipanType) {
        if (mergeParticipanType == MergeParticipantType.participant1) {
            return partipant1;
        } else {
            return partipant2;
        }
    }

    RecordType getType() {
        return recordType;
    }

    EventTypeTag getEventTypeTag() {
        return eventTypeTag;
    }

    public String getEventTag() {
        String tag;
        switch (eventTypeTag) {
            case BIRT:
                tag = "BIRT";
                break;
            case DEAT:
                tag = "DEAT";
                break;
            case MARR:
                tag = "MARR";
                break;
            case MARB:
                tag = "MARB";
                break;
            case MARC:
                tag = "MARC";
                break;
            case MARL:
                tag = "MARL";
                break;
            case WILL:
                tag = "WILL";
                break;
            default:
                tag = "EVEN";
                break;
        }
        return tag;
    }

    String getEventTypeWithDate() {
        String eventTypeWithDate = eventType;
        if (!recordInfoPlace.getValue().isEmpty()) {
            if (!eventTypeWithDate.isEmpty()) {
                eventTypeWithDate += ", ";
            }
            eventTypeWithDate += recordInfoPlace.getValue();
        }
        if (!eventTypeWithDate.isEmpty()) {
            eventTypeWithDate += " (" + getEventDate().getDisplayValue() + ")";
        }

        return eventTypeWithDate;
    }

    String getFileName() {
        return fileName;
    }

    String getEventSourceTitle() {
        if (eventSourceTitle == null) {
            calculateSourceTitle();
        }
        return eventSourceTitle;
    }

    String getEventCote() {
        return eventCote;
    }

    String getEventPage() {
        return freeComment;
    }

    PropertyDate getEventDate() {
        return eventDate;
    }

    String getEventDateDDMMYYYY(boolean showFrenchCalendarDate) {
        String result = formatDateDDMMYYYY(eventDate);

        if (showFrenchCalendarDate) {
            try {
                String frenchCalendarDate = eventDate.getStart().getPointInTime(PointInTime.FRENCHR).toString();
                if (!frenchCalendarDate.isEmpty()) {
                    result += " (" + frenchCalendarDate + ")";
                }
            } catch (GedcomException ex) {
                // rien a faire
            }
        }
        return result;
    }

    PropertyDate getInsinuationDate() {
        return eventSecondDate;
    }

    String formatDateDDMMYYYY(PropertyDate dateProperty) {
        try {
            String result;
            // je recupere la date dans le calendrier gregorien
            PointInTime pit = dateProperty.getStart().getPointInTime(PointInTime.GREGORIAN);
            if (pit.getYear() == PointInTime.UNKNOWN || pit.getYear() == Integer.MIN_VALUE) {
                result = "";
            } else if (pit.getMonth() == PointInTime.UNKNOWN || pit.getMonth() == Integer.MIN_VALUE) {
                result = String.format("%04d", pit.getYear());
            } else if (pit.getDay() == PointInTime.UNKNOWN || pit.getDay() == Integer.MIN_VALUE) {
                result = String.format("%02d/%04d", pit.getMonth() + 1, pit.getYear());
            } else {
                result = String.format("%02d/%02d/%04d", pit.getDay() + 1, pit.getMonth() + 1, pit.getYear());
            }

            return result;
        } catch (GedcomException ex) {
            return "";
        }

    }

    String getInsinuationDateDDMMYYYY(boolean showFrenchCalendarDate) {
        String result = formatDateDDMMYYYY(eventSecondDate);
        if (showFrenchCalendarDate) {
            try {
                String frenchCalendarDate = eventSecondDate.getStart().getPointInTime(PointInTime.FRENCHR).toString();
                if (!frenchCalendarDate.isEmpty()) {
                    result += " (" + frenchCalendarDate + ")";
                }
            } catch (GedcomException ex) {
                // rien a faire
            }
        }
        return result;

    }

    String getInsinuationType() {
        return "Insinuation " + getEventType();
    }

    String getEventPlace() {
        return recordInfoPlace.getValue();
    }

    String getEventPlaceCityName() {
        return recordInfoPlace.getCityName();
    }

    String getEventPlaceCityCode() {
        return recordInfoPlace.getCityCode();
    }

    String getEventPlaceCountyName() {
        return recordInfoPlace.getCountyName();
    }

    String getEventType() {
        String result;
        switch (eventTypeTag) {
            case BIRT:
                result = Gedcom.getName("BIRT");
                break;
            case DEAT:
                result = Gedcom.getName("DEAT");
                break;
            case MARR:
                result = Gedcom.getName("MARR");
                break;
            case MARB:
            case MARC:
            case MARL:
            case WILL:
            default:
                result = eventType;
                break;
        }
        return result;
    }

    String getNotary() {
        return notary;
    }

    String getEventComment(boolean showFrenchCalendarDate) {
        String comment;
        switch (recordType) {
            case BIRTH:
                comment = makeBirthComment(showFrenchCalendarDate);
                break;
            case MARRIAGE:
                comment = makeMarriageComment(showFrenchCalendarDate);
                break;
            case DEATH:
                comment = makeDeathComment(showFrenchCalendarDate);
                break;
            default:
                comment = makeMiscComment(showFrenchCalendarDate);
                break;
        }
        return comment.replaceAll(",+", ",");
    }

    boolean isInsinuation() {
        return eventSecondDate != null && eventSecondDate.isComparable();
    }
    
    boolean isEmpty(Delta delta) {
        return delta.getYears()==0 && delta.getMonths()==0 && delta.getDays()==0;
    }

    /**
     * genere le commentaire de la naissance composé de : commentaire de
     * l'individu , commentaire genéral commentaire photo commentaire père
     * commentaire mère noms, prénom et commentaires des parrain, marraine et
     * témoins
     *
     * @return
     */
    private String makeBirthComment(boolean showFrenchCalendarDate) {

        String comment;
        comment = "Date de l'acte: " + getEventDateDDMMYYYY(showFrenchCalendarDate);
        comment = appendComment(comment, "Nouveau né", partipant1.makeParticipantComment(showFrenchCalendarDate));
        comment = appendComment(comment, "Père", partipant1.makeParticipantFatherComment());
        comment = appendComment(comment, "Mère", partipant1.makeParticipantMotherComment());

        String godFather = appendValue(
                witness1.getFirstName() + " " + witness1.getLastName(),
                witness1.getOccupation(),
                witness1.getComment());
        if (!godFather.isEmpty()) {
            if (!comment.isEmpty() && comment.charAt(comment.length() - 1) != '\n') {
                comment += "\n";
            }
            comment += "Parrain/témoin" + ": " + godFather;
        }
        String godMother = appendValue(
                witness2.getFirstName() + " " + witness2.getLastName(),
                witness2.getOccupation(),
                witness2.getComment());
        if (!godMother.isEmpty()) {
            if (!comment.isEmpty() && comment.charAt(comment.length() - 1) != '\n') {
                comment += "\n";
            }
            comment += "Marraine/témoin" + ": " + godMother;
        }

        String witness = appendValue(
                witness3.getFirstName() + " " + witness3.getLastName(),
                witness3.getOccupation(),
                witness3.getComment(),
                witness4.getFirstName() + " " + witness4.getLastName(),
                witness4.getOccupation(),
                witness4.getComment());
        comment = appendComment(comment, "Témoin(s)", witness);

        comment = appendComment(comment, "Commentaire général", generalComment);
        comment = appendComment(comment, "Cote", makeEventPage());

        return comment;
    }

    /**
     * genere le commentaire de la naissance en concatenant : commentaire
     * general, commentaire libre, commentaire de l'epoux commentaire de
     * l'epouse noms, prénom et commentaires des témoins
     *
     * @return
     */
    private String makeMarriageComment(boolean showFrenchCalendarDate) {

        String comment;
        comment = "Date de l'acte: " + getEventDateDDMMYYYY(showFrenchCalendarDate);

        comment = appendComment(comment, "Epoux", partipant1.makeParticipantComment(showFrenchCalendarDate));
        comment = appendComment(comment, "Ex conjoint époux", partipant1.makeParticipantMarriedComment());
        comment = appendComment(comment, "Père époux", partipant1.makeParticipantFatherComment());
        comment = appendComment(comment, "Mère époux", partipant1.makeParticipantMotherComment());

        comment = appendComment(comment, "Epouse", partipant2.makeParticipantComment(showFrenchCalendarDate));
        comment = appendComment(comment, "Ex conjoint épouse", partipant2.makeParticipantMarriedComment());
        comment = appendComment(comment, "Père épouse", partipant2.makeParticipantFatherComment());
        comment = appendComment(comment, "Mère épouse", partipant2.makeParticipantMotherComment());

        comment = appendComment(comment, "Témoin(s)", makeWitnessComment());
        comment = appendComment(comment, "Commentaire général", generalComment);
        comment = appendComment(comment, "Cote", makeEventPage());

        return comment;
    }

    /**
     * genere le commentaire de la deces composé de : commentaire de l'individu
     * , commentaire genéral commentaire photo commentaire père commentaire mère
     * noms, prénom et commentaires des parrain, marraine et témoins
     *
     * @return
     */
    private String makeDeathComment(boolean showFrenchCalendarDate) {
        String comment;
        comment = "Date de l'acte: " + getEventDateDDMMYYYY(showFrenchCalendarDate);

        comment = appendComment(comment, "Défunt", partipant1.makeParticipantComment(showFrenchCalendarDate));
        comment = appendComment(comment, "Conjoint", partipant1.makeParticipantMarriedComment());
        comment = appendComment(comment, "Père", partipant1.makeParticipantFatherComment());
        comment = appendComment(comment, "Mère", partipant1.makeParticipantMotherComment());

        comment = appendComment(comment, "Témoin(s)", makeWitnessComment());
        comment = appendComment(comment, "Commentaire général", generalComment);
        comment = appendComment(comment, "Cote", makeEventPage());

        return comment;
    }

    /**
     * genere le commentaire d'un evenement divers en concatenant : commentaire
     * de l'individu commentaire du pere de l'individu commentaire de la mere de
     * l'individu noms, prénom et commentaires des témoins commentaire general
     * commentaire libre
     *
     * @return
     */
    private String makeMiscComment(boolean showFrenchCalendarDate) {
        String comment;
        comment = "Date de l'acte: " + getEventDateDDMMYYYY(showFrenchCalendarDate);

        // commentaire de l'insinuation
        if (isInsinuation()) {
            String insinuationComment = appendValue(
                    "Insinuation de l'acte ''" + getEventType() + "'' du " + getInsinuationDateDDMMYYYY(showFrenchCalendarDate),
                    notary.isEmpty() ? "" : "retenu par " + notary);

            if (!insinuationComment.isEmpty()) {
                if (!comment.isEmpty() && comment.charAt(comment.length() - 1) != '\n') {
                    comment += "\n";
                }
                comment += insinuationComment;
            }
        }

        comment = appendComment(comment, "Intervenant 1", partipant1.makeParticipantComment(showFrenchCalendarDate));
        comment = appendComment(comment, "Conjoint intervenant 1", partipant1.makeParticipantMarriedComment());
        comment = appendComment(comment, "Père intervenant 1", partipant1.makeParticipantFatherComment());
        comment = appendComment(comment, "Mère intervenant 1", partipant1.makeParticipantMotherComment());

        comment = appendComment(comment, "Intervenant 2", partipant2.makeParticipantComment(showFrenchCalendarDate));
        comment = appendComment(comment, "Conjoint intervenant 2", partipant2.makeParticipantMarriedComment());
        comment = appendComment(comment, "Père intervenant 2", partipant2.makeParticipantFatherComment());
        comment = appendComment(comment, "Mère intervenant 2", partipant2.makeParticipantMotherComment());

        comment = appendComment(comment, "Témoin(s)", makeWitnessComment());
        comment = appendComment(comment, "Commentaire général", generalComment);
        comment = appendComment(comment, "Cote", makeEventPage());

        return comment;
    }

    //
//    private String makeIndiComment(boolean showFrenchCalendarDate) {
//        String comment = appendValue(
//                record.getIndi().getFirstName() + " " + record.getIndi().getLastName(),
//                record.getIndi().getAge()==null ? "" :record.getIndi().getAge().toString(),
//                makeParticipantBirthComment(record.getIndi().getBirthDate(), showFrenchCalendarDate, record.getIndi().getBirthPlace()),
//                record.getIndi().getOccupation()==null ? "" : record.getIndi().getOccupation().toString(),
//                appendPrefixValue("domicile", record.getIndi().getResidence() == null ? "" :record.getIndi().getResidence() .toString()),
//                record.getIndi().getComment() .toString()
//                );
//        return comment;
//    }
//
//    private String makeIndiMarriedComment(  ) {
//           String comment = appendValue(
//                record.getIndi().getMarriedFirstName() + " " + record.getIndi().getMarriedLastName() ,
//                record.getIndi().getMarriedDead().toString(),
//                record.getIndi().getMarriedOccupation().toString(),
//                appendPrefixValue("domicile", record.getIndi().getMarriedResidence().toString()),
//                record.getIndi().getMarriedComment().toString()
//                );
//
//        return comment;
//    }
//
//    private String makeIndiFatherComment(  ) {
//        String comment = appendValue(
//                record.getIndi().getFatherFirstName() + " " + record.getIndi().getFatherLastName(),
//                record.getIndi().getFatherAge().toString(),
//                record.getIndi().getFatherDead().toString(),
//                record.getIndi().getFatherOccupation().toString(),
//                appendPrefixValue("domicile", record.getIndi().getFatherResidence().toString()),
//                record.getIndi().getFatherComment().toString()
//                );
//        return comment;
//    }
//
//    private String makeIndiMotherComment(  ) {
//         String comment = appendValue(
//                record.getIndi().getMotherFirstName() + " " + record.getIndi().getMotherLastName(),
//                record.getIndi().getMotherAge().toString(),
//                record.getIndi().getMotherDead().toString(),
//                record.getIndi().getMotherOccupation().toString(),
//                appendPrefixValue("domicile", record.getIndi().getMotherResidence().toString()),
//                record.getIndi().getMotherComment().toString()
//                );
//        return comment;
//    }
//    private String makeWifeComment( boolean showFrenchCalendarDate ) {
//        String comment = appendValue(
//                record.getWife().getFirstName() + " " + record.getWife().getLastName(),
//                record.getWife().getAge().toString(),
//                makeParticipantBirthComment(record.getWife().getBirthDate(), showFrenchCalendarDate, record.getWife().getBirthPlace()),
//                record.getWife().getOccupation().toString(),
//                appendPrefixValue("domicile", record.getWife().getResidence().toString()),
//                record.getWife().getComment().toString()
//                );
//        return comment;
//    }
//
//    private String makeWifeMarriedComment(  ) {
//         String comment = appendValue(
//                record.getWife().getMarriedFirstName() + " " + record.getWife().getMarriedLastName(),
//                record.getWife().getMarriedDead().toString(),
//                record.getWife().getMarriedOccupation().toString(),
//                appendPrefixValue("domicile", record.getWife().getMarriedResidence().toString()),
//                record.getWife().getMarriedComment().toString()
//                );
//        return comment;
//    }
//
//    private String makeWifeFatherComment(  ) {
//         String comment = appendValue(
//                record.getWife().getFatherFirstName() + " " + record.getWife().getFatherLastName(),
//                record.getWife().getFatherAge().toString(),
//                record.getWife().getFatherDead().toString(),
//                record.getWife().getFatherOccupation().toString(),
//                appendPrefixValue("domicile", record.getWife().getFatherResidence().toString()),
//                record.getWife().getFatherComment().toString()
//                );
//        return comment;
//    }
//
//    private String makeWifeMotherComment(  ) {
//        String comment = appendValue(
//                record.getWife().getMotherFirstName() + " " + record.getWife().getMotherLastName(),
//                record.getWife().getMotherAge().toString(),
//                record.getWife().getMotherDead().toString(),
//                record.getWife().getMotherOccupation().toString(),
//                appendPrefixValue("domicile", record.getWife().getMotherResidence().toString()),
//                record.getWife().getMotherComment().toString()
//                );
//        return comment;
//    }
    private String makeWitnessComment() {
        String comment = appendValue(
                witness1.getFirstName() + " " + witness1.getLastName(),
                witness1.getOccupation(),
                witness1.getComment(),
                witness2.getFirstName() + " " + witness2.getLastName(),
                witness2.getOccupation(),
                witness2.getComment(),
                witness3.getFirstName() + " " + witness3.getLastName(),
                witness3.getOccupation(),
                witness3.getComment(),
                witness4.getFirstName() + " " + witness4.getLastName(),
                witness4.getOccupation(),
                witness4.getComment());
        return comment;
    }

    /**
     * genere le commentaire mentionnant la reference à une insinuation
     *
     * @return
     */
    protected String makeInsinuationReferenceComment(boolean showFrenchCalendarDate) {
        String comment;

        // commentaire de l'insinuation
        comment = "Voir l'insinuation de l'acte ''" + getEventType() + "''"
                + " du " + getInsinuationDateDDMMYYYY(showFrenchCalendarDate)
                + " (" + getEventPlaceCityName() + ")"
                + (getNotary().isEmpty() ? "" : " retenu par " + getNotary());

        return comment;
    }

    protected String makeEventPage() {
        String eventPage = eventCote;
        if (!freeComment.isEmpty()) {
            if (!eventPage.isEmpty()) {
                eventPage += ", ";
            }
            eventPage += freeComment;
        }
        return eventPage;
    }

    /**
     * concatene plusieurs commentaires dans une chaine , séparés par une
     * virgule
     */
    private String appendComment(String comment, String label, String newComment) {
        if (!newComment.isEmpty()) {
            if (!comment.isEmpty() && comment.charAt(comment.length() - 1) != '\n') {
                comment += "\n";
            }
            comment += label + ": " + newComment;
        }
        return comment;
    }

    /**
     * concatene plusieurs commentaires dans une chaine , séparés par une
     * virgule
     */
    private String appendValue(String... otherValues) {
        StringBuilder sb = new StringBuilder();
        for (String otherValue : otherValues) {
            // j'ajoute les valeurs supplémentaires séparées par des virgules
            if (!otherValue.trim().isEmpty()) {
                // je concantene les valeurs en inserant une virgule dans
                // si la valeur précedente n'est pas vide
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(otherValue.trim());
            }
        }

        return sb.toString();
    }

    /**
     * concatene plusieurs commentaires dans une chaine , séparés par une
     * virgule
     */
    private String appendPrefixValue(String prefix, String... otherValues) {
        StringBuilder sb = new StringBuilder();
        for (String otherValue : otherValues) {
            // j'ajoute les valeurs supplémentaires séparées par des virgules
            if (!otherValue.trim().isEmpty()) {
                // je concantene les valeurs en inserant une virgule dans
                // si la valeur précedente n'est pas vide
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(otherValue.trim());
            }
        }
        if (sb.length() > 0) {
            sb.insert(0, prefix + " ");
        }

        return sb.toString();
    }

    static private PointInTime getYear(PointInTime pit) throws GedcomException {
        PointInTime gregorianPit = pit.getPointInTime(PointInTime.GREGORIAN);
        return new PointInTime(PointInTime.UNKNOWN, PointInTime.UNKNOWN, gregorianPit.getYear());
    }

    static private PointInTime getYear(PointInTime pit, int shiftYear) throws GedcomException {
        PointInTime gregorianPit = pit.getPointInTime(PointInTime.GREGORIAN);
        return new PointInTime(PointInTime.UNKNOWN, PointInTime.UNKNOWN, gregorianPit.getYear() + shiftYear);
    }

    static protected PointInTime getYear(PointInTime refPit, Delta age) throws GedcomException {
        PointInTime gregorianPit = refPit.getPointInTime(PointInTime.GREGORIAN);
        PointInTime pit = new PointInTime(gregorianPit.getDay(), gregorianPit.getMonth(), gregorianPit.getYear());

        pit.add(-age.getDays(), -age.getMonths(), -age.getYears());

        if (age.getMonths() == 0 && age.getDays() == 0) {
            // si le nombre de mois n'est pas précisé, j'arrondis à l'année
            pit.set(PointInTime.UNKNOWN, PointInTime.UNKNOWN, pit.getYear());
        } else if (age.getDays() == 0) {
            // si le nombre de jours n'est pas précisé, j'arrondis au mois
            pit.set(PointInTime.UNKNOWN, pit.getMonth(), pit.getYear());
        }

        return pit;
    }

    /**
     * calcule la date de naissance de l'individu ou de l'epouse si
     * record=RecordBirth : retourne la date de naissance de l'individu ou a
     * defaut date de l'evenement (c'est souvent la meme sauf si l'evenement est
     * un bapteme qui a lieu apres la naissance) si record=RecordMarriage :
     * retourne la date de naissance de l'individu ou a defaut dateBirth = date
     * de son mariage - minMarriageYearOld si record=RecordDeath ou RecordMisc
     * retourne la date "avant" de la levenement arrondi a l'année.
     *
     * @param birthDate date de naissance de l'individu ou de l'epouse
     * @param age age de l'individu ou de l'epouse
     * @param marriedMarriageDate date de mariage avec l'ex conjoint (null si
     * pas d'ex conjoint
     * @return
     */
    private PropertyDate calculateBirthDate(MergeParticipantType participantType, PropertyDate recordBirthDate, Delta age, PropertyDate marriedMarriageDate) {
        PropertyDate resultDate = new PropertyDate();
        resultDate.setValue(recordBirthDate.getValue());
        if (recordType == RecordType.BIRTH) {
            if (!recordBirthDate.isComparable()) {
                resultDate.setValue(getEventDate().getValue());
            }
        } else if (recordType == RecordType.MARRIAGE
                || (recordType == RecordType.MISC && (eventTypeTag == EventTypeTag.MARB || eventTypeTag == EventTypeTag.MARC || eventTypeTag == EventTypeTag.MARL))) {

            if (!recordBirthDate.isComparable() && eventDate.isComparable()) {
                // la date de naissance n'est pas valide, 
                // je calcule la naissance a partir de l'age ou de la date de mariage
                if (age.getYears() != 0 || age.getMonths() != 0 || age.getDays() != 0) {
                    // l'age est valide,
                    if (age.getYears() != 0 && age.getMonths() != 0 && age.getDays() != 0) {
                        // l'age est precis au jour près
                        // date de naissance = date de mariage - age
                        //birthDate.setValue(eventDate.getValue());
                        //birthDate.getStart().add(-age.getDays(), -age.getMonths(), -age.getYears());
                        PointInTime start = null;
                        if (eventDate.getStart() != null) {
                            start = new PointInTime();
                            start.set(eventDate.getStart());
                            start.add(-age.getDays(), -age.getMonths(), -age.getYears());
                        }
                        PointInTime end = null;
                        if (eventDate.getEnd() != null) {
                            end = new PointInTime();
                            end.set(eventDate.getEnd());
                            end.add(-age.getDays(), -age.getMonths(), -age.getYears());
                        }
                        resultDate.setValue(eventDate.getFormat(),
                                start, end, "Date naissance = date du releve - age");

                    } else {
                        try {
                            // l'age n'est pas précis au jour près.
                            // date de naissance = date de mariage (arrondi a l'année) - age (arrondi à l'année)
                            ///birthDate.setValue(String.format("CAL %d", eventDate.getStart().getYear() -age.getYears()));
                            resultDate.setValue(PropertyDate.CALCULATED, getYear(getEventDate().getStart(), age), null, "date naissance=date du releve - age");
                        } catch (GedcomException ex) {
                            // en cas d'exception de getYear je retourne une date invalide (non initialisée
                            resultDate = new PropertyDate();
                            Exceptions.printStackTrace(ex);
                        }
                    }
                } else {
                    try {
                        // l'age n'est pas valide

                        // date de naissance maximale BEF = date de mariage -  minMarriageYearOld
                        //birthDate= calulateDateBeforeMinusShift(eventDate, MergeQuery.minMarriageYearOld, "date 15 ans avant le mariage");
                        resultDate = calulateDateBeforeMinusShift(getEventDate(), MergeQuery.minMarriageYearOld, "=date mariage -" + MergeQuery.minMarriageYearOld);
                    } catch (GedcomException ex) {
                        // en cas d'exception de getYear je retourne une date invalide (non initialisée
                        resultDate = new PropertyDate();
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        } else if (recordType == RecordType.DEATH) {
            if (!recordBirthDate.isComparable() && eventDate.isComparable()) {
                // la date de naissance n'est pas valide,
                // je calcule la naissance a partir de l'age
                if (age.getYears() != 0 || age.getMonths() != 0 || age.getDays() != 0) {
                    // l'age est valide,
                    if (age.getYears() != 0 && age.getMonths() != 0 && age.getDays() != 0) {
                        // l'age est precis au jour près
                        // date de naissance = date de mariage - age
                        ///birthDate.setValue(eventDate.getValue());
                        ///birthDate.getStart().add(-age.getDays(), -age.getMonths(), -age.getYears());
                        PointInTime start = null;
                        if (eventDate.getStart() != null) {
                            start = new PointInTime();
                            start.set(eventDate.getStart());
                            start.add(-age.getDays(), -age.getMonths(), -age.getYears());
                        }
                        PointInTime end = null;
                        if (eventDate.getEnd() != null) {
                            end = new PointInTime();
                            end.set(eventDate.getEnd());
                            end.add(-age.getDays(), -age.getMonths(), -age.getYears());
                        }
                        resultDate.setValue(eventDate.getFormat(),
                                start, end, "Date naissance = date du releve - age");
                    } else {
                        try {
                            // l'age n'est pas précis au jour près.
                            // date de naissance = date de mariage (arrondi a l'année) - age (arrondi à l'année)
                            ///birthDate.setValue(String.format("CAL %d", eventDate.getStart().getYear() - age.getYears()));
                            resultDate.setValue(PropertyDate.CALCULATED, getYear(getEventDate().getStart(), age), null, "date naissance=date du releve - age");
                        } catch (GedcomException ex) {
                            // en cas d'exception de getYear je retourne une date invalide (non initialisée
                            resultDate = new PropertyDate();
                            Exceptions.printStackTrace(ex);
                        }

                    }
                } else {
                    // l'age n'est pas valide
                    if (marriedMarriageDate != null && marriedMarriageDate.isComparable()) {
                        try {
                            // la naissance est minMarriageYearOld avant le mariage avec l'ex conjoint
                            ///birthDate.setValue(String.format("BEF %d", marriedMarriageDate.getStart().getYear()-MergeQuery.minMarriageYearOld));
                            resultDate.setValue(PropertyDate.BEFORE, getYear(marriedMarriageDate.getStart(), -MergeQuery.minMarriageYearOld), null, "date naissance= avant date de mariage avec ex conjoint -" + MergeQuery.minMarriageYearOld);
                        } catch (GedcomException ex) {
                            // en cas d'exception de getYear je retourne une date invalide (non initialisée
                            resultDate = new PropertyDate();
                            Exceptions.printStackTrace(ex);
                        }
                    } else {
                        try {
                            // la naissance est avant le deces
                            ///birthDate.setValue(String.format("BEF %d", getEventDate().getStart().getYear()));
                            resultDate.setValue(PropertyDate.BEFORE, getYear(getEventDate().getStart()), null, "date naissance= avant date du releve");
                        } catch (GedcomException ex) {
                            // en cas d'exception de getYear je retourne une date invalide (non initialisée
                            resultDate = new PropertyDate();
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }

        } else {
            // releve divers
            if (!recordBirthDate.isComparable() && eventDate.isComparable()) {
                // la date de naissance n'est pas valide,
                // je calcule la naissance a partir de l'age
                if (age.getYears() != 0) {
                    // l'age est valide,
                    if (age.getYears() != 0 && age.getMonths() != 0 && age.getDays() != 0) {
                        // l'age est precis au jour près
                        // date de naissance = date de mariage - age
                        ///birthDate.setValue(eventDate.getValue());
                        ///birthDate.getStart().add(-age.getDays(), -age.getMonths(), -age.getYears());
                        PointInTime start = null;
                        if (eventDate.getStart() != null) {
                            start = new PointInTime();
                            start.set(eventDate.getStart());
                            start.add(-age.getDays(), -age.getMonths(), -age.getYears());
                        }
                        PointInTime end = null;
                        if (eventDate.getEnd() != null) {
                            end = new PointInTime();
                            end.set(eventDate.getEnd());
                            end.add(-age.getDays(), -age.getMonths(), -age.getYears());
                        }
                        resultDate.setValue(eventDate.getFormat(),
                                start, end, "Date naissance = date du releve - age");
                    } else {
                        try {
                            // l'age n'est pas précis au jour près.
                            // date de naissance = date de mariage (arrondi a l'année) - age (arrondi à l'année)
                            ///birthDate.setValue(String.format("CAL %d", eventDate.getStart().getYear() - age.getYears()));
                            resultDate.setValue(PropertyDate.CALCULATED, getYear(eventDate.getStart(), age), null, "date naissance=date du releve - age");
                        } catch (GedcomException ex) {
                            // en cas d'exception de getYear je retourne une date invalide (non initialisée
                            resultDate = new PropertyDate();
                            Exceptions.printStackTrace(ex);
                        }
                    }
                } else {
                    // l'age n'est pas valide
                    if (marriedMarriageDate != null && marriedMarriageDate.isComparable()) {
                        try {
                            // la naissance est minMarriageYearOld avant le mariage avec l'ex conjoint
                            ///birthDate.setValue(String.format("BEF %d", marriedMarriageDate.getStart().getYear()-MergeQuery.minMarriageYearOld));
                            resultDate.setValue(PropertyDate.BEFORE, getYear(marriedMarriageDate.getStart(), -MergeQuery.minMarriageYearOld), null, "date naissance= avant ladate de mariage avec ex conjoint -" + MergeQuery.minMarriageYearOld);
                        } catch (GedcomException ex) {
                            // en cas d'exception de getYear je retourne une date invalide (non initialisée
                            resultDate = new PropertyDate();
                            Exceptions.printStackTrace(ex);
                        }
                    } else {
                        // il n'y a pas d'ex conjoint

                        if ((eventTypeTag == EventTypeTag.WILL && participantType == MergeParticipantType.participant1)
                                || eventTypeTag == EventTypeTag.MARB
                                || eventTypeTag == EventTypeTag.MARC
                                || eventTypeTag == EventTypeTag.MARL) {
                            try {
                                // pour un certificat de mariage, les deux participants doivent être majeurs
                                // pour un testament le partipant doit être majeur
                                resultDate.setValue(PropertyDate.BEFORE, getYear(getEventDate().getStart(), -MergeQuery.minMajorityYearOld), null, "date naissance= avant la date du releve - " + MergeQuery.minMajorityYearOld + "(personne majeure)");
                            } catch (GedcomException ex) {
                                // en cas d'exception de getYear je retourne une date invalide (non initialisée
                                resultDate = new PropertyDate();
                                Exceptions.printStackTrace(ex);
                            }

                        } else {
                            try {
                                // la naissance est avant l'evenement qui concerne l'individu
                                resultDate.setValue(PropertyDate.BEFORE, getYear(getEventDate().getStart()), null, "date naissance= avant la date du releve");
                            } catch (GedcomException ex) {
                                // en cas d'exception de getYear je retourne une date invalide (non initialisée
                                resultDate = new PropertyDate();
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            }
        }

        return resultDate;
    }

    /**
     * calcule la date de deces de l'individu ou de l'epouse si
     * record=RecordBirth : retourne "apres" la date de l'évènement arrondie à
     * l'année. si record=RecordMarriage : retourne "apres" la date de mariage
     * l'évènement arrondie à l'année si record=RecordDeath retourne la date
     * l'évènement. si record=RecordMisc retourne la date "a partir de" la date
     * de l'évènement arrondie a l'année.
     *
     * @param birthDate date de naissance de l'individu ou de l'épouse
     * @param age age de l'individu ou de l'épouse
     * @param marriedMarriageDate date de mariage avec l'ex conjoint (null si
     * pas d'ex conjoint
     * @return
     */
    private PropertyDate calculateDeathDate() throws Exception {
        PropertyDate resultDate = new PropertyDate();
        if (recordType == RecordType.DEATH) {
            resultDate.setValue(getEventDate().getValue());
        } else {
            resultDate.setValue(PropertyDate.FROM, getYear(getEventDate().getStart()), null, "date décès= à partir de la date de l'évènement");
        }

        return resultDate;
    }

    /**
     * calcule la date de naissance d'un parent a partir - soit de l'age du
     * parent s'il est precisé dans le releve - soit de la date de naissance de
     * l'enfant si elle est precisée dans le relevé - soit de la date de mariage
     * de l'enfant (seulement pour le releve de mariage)
     *
     * @param requiredBirth
     * @return
     */
    private PropertyDate calculateParentBirthDate(Delta parentAge, PropertyDate childBirthDate) throws Exception {

        PropertyDate parentBirthDate = new PropertyDate();
        // j'initialise la date de naissance du parent avec la date de naissance de l'individu
        // puis je la decrementerai en fonction des autres donnees du releve.
        ///parentBirthDate.setValue(childBirthDate.getValue());

        if (parentAge != null && parentAge.getYears() != 0 && eventDate.isComparable()) {
            // l'age du parent est valide
            // parentBirthDate = eventDate - parentAge
            if (parentAge.getYears() != 0 && parentAge.getMonths() != 0 && parentAge.getDays() != 0) {
                //l'age est precis au jour pres.
                ///parentBirthDate.getStart().add(-parentAge.getDays(), -parentAge.getMonths(), -parentAge.getYears());
                PointInTime start = null;
                if (getEventDate().getStart() != null) {
                    start = new PointInTime();
                    start.set(getEventDate().getStart());
                    start.add(-parentAge.getDays(), -parentAge.getMonths(), -parentAge.getYears());
                }
                PointInTime end = null;
                if (getEventDate().getEnd() != null) {
                    end = new PointInTime();
                    end.set(getEventDate().getEnd());
                    end.add(-parentAge.getDays(), -parentAge.getMonths(), -parentAge.getYears());
                }
                parentBirthDate.setValue(getEventDate().getFormat(),
                        start, end, "Date naissance = date du releve - age");
            } else {
                // date arrondie à l'année car l'age n'est pas précis au jour près.
                ///parentBirthDate.setValue(String.format("CAL %d", parentBirthDate.getStart().add(0, 0, -parentAge.getYears()).getYear() ));
                parentBirthDate.setValue(PropertyDate.CALCULATED, getYear(getEventDate().getStart(), parentAge), null, "date naissance=date du releve - age");
            }
        } else {
            // l'age du parent n'est pas valide
            // je calcule la date maximale à partir de la date de mariage ou la date de naissance de l'enfant
            if (childBirthDate.isComparable()) {
                // le parent doit être né  "minParentYearOld" années avant la naissance de l'individu
                // parentBirthDate = eventDat - minParentYearOld
                ///parentBirthDate.setValue(String.format("BEF %d", parentBirthDate.getStart().add(0, 0, -MergeQuery.minParentYearOld).getYear()));
                parentBirthDate.setValue(PropertyDate.BEFORE, getYear(childBirthDate.getStart(), -MergeQuery.minParentYearOld), null, "date naissance= avant naissance de l'enfant -" + MergeQuery.minParentYearOld);
            } else {
                // je ne sais pas calculer la date de naissance
                parentBirthDate = new PropertyDate();
            }
        }

        return parentBirthDate;
    }

    /**
     * calcule la date de deces d'un parent a partir de la date du relevé et la
     * date de naissance d'un enfant.
     *
     * enfant
     *
     * @param record
     * @param dead true si le parent est decede avant la date de l'evenement
     * @param childBirthDate date de naissance de l'enfant
     * @param monthBeforeBirth nombre de mois du deces avant la naissance de
     * l'enfant (9 mois pour le pere, 0 mois pour la mere)
     * @return date de deces du parent
     */
    private PropertyDate calculateParentDeathDate(DeadState dead, PropertyDate childBirthDate, int monthBeforeBirth) throws Exception {

        PropertyDate parentDeathDate = new PropertyDate();

        if (getEventDate().isComparable()) {
            // la naissance de l'individu n'est utilisable pour determiner la date de minimale de deces du pere
            // que si c'est une date exacte ou une date avec une borne inferieure
            // J'ignore la date si c'est une date maximale (BEF) ou estimée
            PointInTime childBirthPit = new PointInTime();
            boolean birthDateUsefull;
            if (childBirthDate.isValid()) {
                if (childBirthDate.getFormat() == PropertyDate.DATE
                        || childBirthDate.getFormat() == PropertyDate.AFTER
                        || childBirthDate.getFormat() == PropertyDate.BETWEEN_AND
                        || childBirthDate.getFormat() == PropertyDate.CALCULATED) {
                    birthDateUsefull = true;
                    childBirthPit.set(childBirthDate.getStart());
                    childBirthPit.add(0, -monthBeforeBirth, 0);
                } else {
                    birthDateUsefull = false;
                }
            } else {
                birthDateUsefull = false;
            }

            if (dead == DeadState.DEAD) {
                if (birthDateUsefull) {
                    // le parent est decede entre la date de naissance et la date du releve
                    // parentDeathDate.setValue(String.format("BEF %d", getEventDate().getStart().getYear()));
                    // parentDeathDate.setValue(String.format("BET %d AND %d", birthPit.getYear(), getEventDate().getStart().getYear()));
                    if (monthBeforeBirth == 0) {
                        parentDeathDate.setValue(PropertyDate.BETWEEN_AND, getYear(childBirthPit), getYear(getEventDate().getStart()), "date deces= entre la naissance de l'enfant et la date du releve");
                    } else {
                        parentDeathDate.setValue(PropertyDate.BETWEEN_AND, getYear(childBirthPit), getYear(getEventDate().getStart()), "date deces= entre la conceptionde l'enfant et la date du releve");
                    }
                } else {
                    // le parent est decede avant la date du releve
                    parentDeathDate.setValue(PropertyDate.BEFORE, getYear(getEventDate().getStart()), null, "date deces= avant la date du releve");
                }
            } else if (dead == DeadState.ALIVE) {
                // le parent est decede apres la date du releve
                parentDeathDate.setValue(PropertyDate.AFTER, getYear(getEventDate().getStart()), null, "date deces= apres la date du releve");
            } else {
                if (birthDateUsefull) {
                    // le parent est decede apres la naissance 
                    parentDeathDate.setValue(String.format("AFT %d", childBirthPit.getYear()));
                    if (monthBeforeBirth == 0) {
                        parentDeathDate.setValue(PropertyDate.AFTER, getYear(childBirthPit), null, "date deces= apres la naissance de l'enfant");
                    } else {
                        parentDeathDate.setValue(PropertyDate.AFTER, getYear(childBirthPit), null, "date deces= apres la conception de l'enfant");
                    }
                } else {
                    // date inconnue, rien a faire
                }
            }
        } else {
            // date inconnue, rien a faire
        }

        return parentDeathDate;
    }

    /**
     * calcule la date de mariage des parents
     *
     * @param record
     * @return
     */
    private PropertyDate calculateParentMariageDate(PropertyDate childBirthDate) throws Exception {
        PropertyDate parentMariageDate;
        if (recordType == RecordType.MARRIAGE) {
            // le mariage des parents est
            //   - avant la naissance de l'epoux
            //   - avant le mariage du marié dminué de minParentYearOld
            // date de mariage = BEF date de naissance de l'epoux
            PropertyDate beforeChildBirth = calulateDateBeforeMinusShift(childBirthDate, 0, "Date mariage= avant naissance époux(se)");
            // date de mariage = EventDate - minMarriageYearOld
            PropertyDate beforeChildMarriage = calulateDateBeforeMinusShift(getEventDate(), MergeQuery.minMarriageYearOld, "Date mariage = dete du releve -" + MergeQuery.minMarriageYearOld);
            // je retiens la date la plus petite
            boolean beforeChildBirthComparable = beforeChildBirth.isComparable();
            boolean beforeChildMarriageComparable = beforeChildMarriage.isComparable();
            if (beforeChildBirthComparable && beforeChildMarriageComparable) {
                // les dates sont comparables , je retiens la plus petite
                if (MergeQuery.getMostAccurateDate(beforeChildBirth, beforeChildMarriage) == beforeChildBirth) {
                    parentMariageDate = beforeChildBirth;
                } else {
                    parentMariageDate = beforeChildMarriage;
                }
            } else if (beforeChildBirthComparable) {
                parentMariageDate = beforeChildBirth;
            } else {
                parentMariageDate = beforeChildMarriage;
            }
        } else {
            // le mariage des parents est avant la naissance de l'enfant arrondie a l'année
            parentMariageDate = calulateDateBeforeMinusShift(childBirthDate, 0, "Date mariage = avant la naissance de l'enfant");
        }
        return parentMariageDate;
    }

    /**
     * calcule la date de mariage a partir de la date du contrat de mariage
     *
     * @param record
     * @return
     */
    protected PropertyDate calculateMariageDateFromMarc(PropertyDate marcDate) {

        PropertyDate marriageDate = new PropertyDate();
        if (marcDate.getFormat() == PropertyDate.DATE) {
            try {
                marriageDate.setValue(PropertyDate.ABOUT, getYear(marcDate.getStart(), 0), null, "date mariage=environ la date du contrat ");
            } catch (GedcomException ex) {
                // j'ignore l'eeereur et je retourne une date de mariage nulle
            }
        }
        return marriageDate;

    }

    /**
     * retourne une date arrondie à l'année immediatement inferieure ou egale a
     * la date donnée, dimminée de yearShift
     *
     * @param birthDate
     * @param yearShift
     * @return
     */
    static private PropertyDate calulateDateBeforeMinusShift(PropertyDate birthDate, int yearShift, String phrase) throws GedcomException {
        // le mariage des parents est avant la naissance de l'enfant arrondie a l'année
        PropertyDate marriageDate = new PropertyDate();
        if (birthDate.getFormat() == PropertyDate.BETWEEN_AND || birthDate.getFormat() == PropertyDate.FROM_TO) {
            //marriageDate.setValue(String.format("BEF %d", birthDate.getEnd().getYear() - yearShift));
            marriageDate.setValue(PropertyDate.BEFORE, getYear(birthDate.getEnd(), -yearShift), null, phrase);
        } else if (birthDate.getFormat() == PropertyDate.FROM || birthDate.getFormat() == PropertyDate.AFTER) {
            // Unknwon
            marriageDate.setValue("");
        } else if (birthDate.getFormat() == PropertyDate.TO || birthDate.getFormat() == PropertyDate.BEFORE) {
            //marriageDate.setValue(String.format("BEF %d", birthDate.getStart().getYear() - yearShift));
            marriageDate.setValue(PropertyDate.BEFORE, getYear(birthDate.getStart(), -yearShift), null, phrase);
        } else if (birthDate.getFormat() == PropertyDate.ABOUT || birthDate.getFormat() == PropertyDate.ESTIMATED) {
            //marriageDate.setValue(String.format("BEF %d", birthDate.getStart().getYear() - yearShift));
            marriageDate.setValue(PropertyDate.BEFORE, getYear(birthDate.getStart(), -yearShift), null, phrase);
        } else {
            //marriageDate.setValue(String.format("BEF %d", birthDate.getStart().getYear() - yearShift));
            marriageDate.setValue(PropertyDate.BEFORE, getYear(birthDate.getStart(), -yearShift), null, phrase);
        }
        return marriageDate;
    }

    protected final void calculateSourceTitle() {
//         if ( eventSourceTitle.isEmpty()) {
//            if (record.getType() == RecordType.Misc) {
//                if (!record.getNotary().isEmpty()) {
//                    eventSourceTitle = String.format("Notaire %getSelectedEntitys", record.getNotary());
//                } else {
//                    eventSourceTitle = "";
//                }
//            } else {
//                String cityName = record.getEventPlaceCityName();
//
//                if (record.getEventDate().getStart().getYear() <= 1792) {
//                    eventSourceTitle = String.format("BMS %s", cityName);
//                } else {
//                    eventSourceTitle = String.format("Etat civil %s", cityName);
//                }
//            }
//        }

//        if ( eventSourceTitle.isEmpty()) {
//            eventSourceTitle = recordsInfoPlace.getCityName();
//        }
        // je cherche la source dans le gedcom
//        Entity[] sources = gedcom.getEntities("SOUR", "SOUR:TITL");
//        for (Entity source : sources) {
//            if (((Source) source).getTitle().equals(recordSourceTitle)) {
//                mergeRow.entityValue = source;
//                mergeRow.entityObject = source;                
//                break;
//            }
//        }
//         // retourne la source d'un evenement 
//         if (eventProperty != null) {
//            Property[] sourceProperties = eventProperty.getProperties("SOUR", false);
//            for (int i = 0; i < sourceProperties.length; i++) {
//                // remarque : verification de classe PropertySource avant de faire le cast en PropertySource pour eliminer
//                // les cas anormaux , par exemple une source "multiline"
//                if ( sourceProperties[i] instanceof PropertySource) {
//                    Source eventSource = (Source) ((PropertySource) sourceProperties[i]).getTargetEntity();
//                    if (record.getEventSourceTitle().compareTo(eventSource.getTitle()) == 0) {
//                        sourceProperty = sourceProperties[i];
//                        break;
//                    }
//                }
//            }
//        }
        //
//            // je verifie si la source existe deja dans le gedcom
//            String cityName = record.getEventPlaceCityName();
//            String cityCode = record.getEventPlaceCityCode();
//            String countyName = record.getEventPlaceCountyName();
//            //String stringPatter = String.format("(?:%s|%s)(?:\\s++)%s(?:\\s++)(?:BMS|Etat\\scivil)", countyName, cityCode, cityName);
//            String stringPatter = String.format("(?:BMS|Etat\\scivil)(?:\\s++)%s", cityName);
//            Pattern pattern = Pattern.compile(stringPatter);
//            Collection<? extends Entity> sources = gedcom.getEntities("SOUR");
//            for (Entity gedComSource : sources) {
//                if (pattern.matcher(((Source) gedComSource).getTitle()).matches()) {
//                    source = (Source) gedComSource;
//                }
//            }
        if (fileName != null) {
            eventSourceTitle = MergeOptionPanel.SourceModel.getModel().getSource(fileName);
        } else {
            eventSourceTitle = "";
        }

    }

    /**
     *
     */
    protected class MergeParticipant {

        MergeParticipantType participantType;
        //Record.Participant participant;

        private String m_FirstName;
        private String m_LastName;
        private SexType m_Sex;
        private Delta m_Age;
        private PropertyDate m_BirthDate;
        private String m_BirthPlace;
        private String m_BirthAddress;
        private String m_Occupation;
        private String m_Residence;
        private String m_Address;
        private String m_Comment;
        private String m_MarriedFirstName;
        private String m_MarriedLastName;
        private String m_MarriedComment;
        private String m_MarriedOccupation;
        private String m_MarriedResidence;
        private String m_MarriedAddress;
        private DeadState m_MarriedDead;
        private String m_FatherFirstName;
        private String m_FatherLastName;
        private String m_FatherOccupation;
        private String m_FatherResidence;
        private String m_FatherAddress;
        private Delta m_FatherAge;
        private DeadState m_FatherDead;
        private String m_FatherComment;
        private String m_MotherFirstName;
        private String m_MotherLastName;
        private String m_MotherOccupation;
        private String m_MotherResidence;
        private String m_MotherAddress;
        private Delta m_MotherAge;
        private DeadState m_MotherDead;
        private String m_MotherComment;

        // memorise les dates calculees (pour éviter de les recalculer a chaque consultation)
        private PropertyDate computedBirthDate = null;
        private PropertyDate computedDeathDate = null;
        private PropertyDate computedFatherBirthDate = null;
        private PropertyDate computedFatherDeathDate = null;
        private PropertyDate computedMotherBirthDate = null;
        private PropertyDate computedMotherDeathDate = null;
        private PropertyDate computedParentMarriageDate = null;
        private PropertyDate computedMarriedBirthDate = null;
        private PropertyDate computedMarriedDeathDate = null;
        private PropertyDate MarriedMarriageDate = null;

        MergeParticipant(MergeParticipantType participantType) {
            this.participantType = participantType;
            //this.participant = participant;
        }

        String getFirstName() {
            return m_FirstName;
        }

        String getLastName() {
            return m_LastName;
        }

        int getSex() {
            return m_Sex.getSex();
        }

        String getSexString() {
            return m_Sex.name();
        }

        PropertyDate getBirthDate() {
            if (computedBirthDate == null) {
                computedBirthDate = calculateBirthDate(participantType,
                        m_BirthDate != null ? m_BirthDate : new PropertyDate(),
                        m_Age != null ? m_Age : new Delta(0, 0, 0),
                        getMarriedMarriageDate());
            }
            return computedBirthDate;
        }

        PropertyDate getDeathDate() throws Exception {
            if (computedDeathDate == null) {
                computedDeathDate = calculateDeathDate();
            }
            return computedDeathDate;
        }

        Delta getAge() {
            return m_Age;
        }

        /**
         * retourne le lieu de naissance . Si c'est un acte de naissance et si
         * IndiBirthPlace est vide retourne IndiFatherResidence ou, à défaut,
         * EventPlace
         *
         * @return
         */
        String getBirthPlace() {
            if (m_BirthPlace != null && !m_BirthPlace.isEmpty()) {
                return m_BirthPlace;
            } else {
                if (recordType == RecordType.BIRTH) {
                    if (m_Residence != null && !m_Residence.isEmpty()) {
                        return m_Residence;
                    } else {
                        if (participantType == MergeParticipantType.participant1) {
                            if (m_FatherResidence != null && !m_FatherResidence.isEmpty()) {
                                return m_FatherResidence;
                            } else {
                                return getEventPlace();
                            }
                        } else {
                            return "";
                        }
                    }
                } else {
                    return "";
                }
            }
        }

        /**
         * retourne le lieu de naissance . Si c'est un acte de naissance et si
         * IndiBirthPlace est vide retourne IndiFatherResidence ou, à défaut,
         * EventPlace
         *
         * @return
         */
        String getDeathPlace() {
            if (m_Residence != null && !m_Residence.isEmpty()) {
                return m_Residence;
            } else {
                return getEventPlace();
            }
        }

        String getOccupation() {
            if (m_Occupation != null) {
                return m_Occupation;
            } else {
                // n'est pas renseignée pour les naissances
                return null;
            }
        }

        String getOccupationWithDate() {
            String occupation = m_Occupation;
            if (!m_Residence.isEmpty()) {
                if (!occupation.isEmpty()) {
                    occupation += ", ";
                }
                occupation += m_Residence;
            }
            if (!occupation.isEmpty()) {
                occupation += " (" + getEventDate().getDisplayValue() + ")";
            }

            return occupation;
        }

        String getResidence() {
            if (m_Residence != null) {
                return m_Residence;
            } else {
                // n'est pas renseignée pour les naissances
                return "";
            }
        }

        //  conjoint (ou ancien conjoint) //////////////////////////////////////////
        String getMarriedFirstName() {
            return m_MarriedFirstName;
        }

        String getMarriedLastName() {
            return m_MarriedLastName;
        }

        PropertyDate getMarriedBirthDate() throws Exception {
            if (computedMarriedBirthDate == null) {
                computedMarriedBirthDate = new PropertyDate();
                if (m_MarriedFirstName != null
                        && m_MarriedLastName != null
                        && (!m_MarriedFirstName.isEmpty() || !m_MarriedLastName.isEmpty())
                        && getMarriedMarriageDate().isComparable()) {
                    // l'ex conjoint existe , la naissance est minMarriageYearOld le mariage avec l'individu
                    computedMarriedBirthDate.setValue(PropertyDate.BEFORE, getYear(getMarriedMarriageDate().getStart(), -MergeQuery.minMarriageYearOld), null, "naissance avant la date du mariage -" + MergeQuery.minMarriageYearOld);
                }
            }
            return computedMarriedBirthDate;
        }

        PropertyDate getMarriedMarriageDate() {
            if (MarriedMarriageDate == null) {
                MarriedMarriageDate = new PropertyDate();
                if (m_MarriedFirstName != null
                        && m_MarriedLastName != null
                        && (!m_MarriedFirstName.isEmpty() || !m_MarriedLastName.isEmpty())
                        && getEventDate().isComparable()) {
                    try {
                        // l'ex conjoint existe , le mariage avec l'individu est avant la date l'evenement
                        MarriedMarriageDate.setValue(PropertyDate.BEFORE, getYear(getEventDate().getStart()), null, "mariage avant la date du relevé");
                    } catch (GedcomException ex) {
                        // en cas d'exception de getYear je retourne une date invalide (non initialisée
                        MarriedMarriageDate = new PropertyDate();
                        Exceptions.printStackTrace(ex);
                    }
                } else if (participantType == MergeParticipantType.participant1 && getType() == RecordType.DEATH) {
                    // la date du mariage est avant le deces
                    // non car il n'est pas forcément marié
                    //MarriedMarriageDate.setValue(PropertyDate.BEFORE, getYear(getEventDate().getStart()), null, "mariage avant la date du relevé");
                }
            }
            return MarriedMarriageDate;
        }

        PropertyDate getMarriedDeathDate() throws Exception {
            if (computedMarriedDeathDate == null) {
                computedMarriedDeathDate = new PropertyDate();

                switch (m_MarriedDead) {
                    case DEAD:
                        computedMarriedDeathDate.setValue(PropertyDate.BEFORE, getYear(getEventDate().getStart()), null, "deces avant la date du relevé");
                        break;
                    case ALIVE:
                        computedMarriedDeathDate.setValue(PropertyDate.AFTER, getYear(getEventDate().getStart()), null, "deces apres la date du relevé");
                        break;
                    default:
                        // je ne sais pas
                        break;
                }
            }
            return computedMarriedDeathDate;
        }

        String getMarriedOccupation() {
            return m_MarriedOccupation;
        }

        String getMarriedOccupationWithDate() {
            String occupation = m_MarriedOccupation;
            if (!m_MarriedResidence.isEmpty()) {
                if (!occupation.isEmpty()) {
                    occupation += ", ";
                }
                occupation += m_MarriedResidence;
            }
            if (!occupation.isEmpty()) {
                occupation += " (" + getEventDate().getDisplayValue() + ")";
            }
            return occupation;
        }

        String getMarriedResidence() {
            return m_MarriedResidence;
        }

        //  indi father ////////////////////////////////////////////////////////////
        PropertyDate getParentMarriageDate() throws Exception {
            if (computedParentMarriageDate == null) {
                computedParentMarriageDate = calculateParentMariageDate(getBirthDate());
            }
            return computedParentMarriageDate;
        }

        String getFatherFirstName() {
            return m_FatherFirstName;
        }

        String getFatherLastName() {
            return m_FatherLastName;
        }

        PropertyDate getFatherBirthDate() throws Exception {
            if (computedFatherBirthDate == null) {
                computedFatherBirthDate = calculateParentBirthDate(m_FatherAge, getBirthDate());
            }
            return computedFatherBirthDate;
        }

        PropertyDate getFatherDeathDate() throws Exception {
            if (computedFatherDeathDate == null) {
                computedFatherDeathDate = calculateParentDeathDate(
                        m_FatherDead != null ? m_FatherDead : DeadState.UNKNOWN,
                        getBirthDate(),
                        9 // le pere peut être decede au plus tot apres la conception, soit 9 mois avant la naissance
                );
            }
            return computedFatherDeathDate;
        }

        String getFatherOccupation() {
            return m_FatherOccupation;
        }

        String getFatherResidence() {
            return m_FatherResidence;
        }

        String getFatherOccupationWithDate() {
            String occupation = m_FatherOccupation;
            if (!m_FatherResidence.isEmpty()) {
                if (!occupation.isEmpty()) {
                    occupation += ", ";
                }
                occupation += m_FatherResidence;
            }
            if (!occupation.isEmpty()) {
                occupation += " (" + getEventDate().getDisplayValue() + ")";
            }
            return occupation;
        }

        String getMotherFirstName() {
            if (m_MotherFirstName == null) {
                return "";
            } else {
                return m_MotherFirstName;
            }
        }

        String getMotherLastName() {
            if (m_MotherLastName == null) {
                return "";
            } else {
                return m_MotherLastName;
            }
        }

        PropertyDate getMotherBirthDate() throws Exception {
            if (computedMotherBirthDate == null) {
                computedMotherBirthDate = calculateParentBirthDate(m_MotherAge, getBirthDate());
            }
            return computedMotherBirthDate;
        }

        PropertyDate getMotherDeathDate() throws Exception {
            if (computedMotherDeathDate == null) {
                computedMotherDeathDate = calculateParentDeathDate(
                        m_MotherDead != null ? m_MotherDead : DeadState.UNKNOWN,
                        getBirthDate(),
                        0 // le mere peut être decedee au plus tot 0 mois avant le naissance (par opposition au pere qui peut etre decede 9 mois avant la naissance)
                );
            }
            return computedMotherDeathDate;
        }

        String getMotherOccupation() {
            if (m_MotherOccupation == null) {
                return "";
            } else {
                return m_MotherOccupation;
            }
        }

        String getMotherOccupationWithDate() {
            String occupation = m_MotherOccupation;
            if (!m_MotherResidence.isEmpty()) {
                if (!occupation.isEmpty()) {
                    occupation += ", ";
                }
                occupation += m_MotherResidence;
            }
            if (!occupation.isEmpty()) {
                occupation += " (" + getEventDate().getDisplayValue() + ")";
            }

            return occupation;
        }

        String getMotherResidence() {
            return m_MotherResidence;
        }

        private String makeParticipantBirthComment(boolean showFrenchCalendarDate) {
            String comment = "";
            PropertyDate birthDate = m_BirthDate;
            if (birthDate != null && birthDate.isValid()) {
                comment = "né le" + " " + formatDateDDMMYYYY(birthDate);
                if (showFrenchCalendarDate) {
                    try {
                        String frenchCalendarDate = birthDate.getStart().getPointInTime(PointInTime.FRENCHR).toString();
                        if (!frenchCalendarDate.isEmpty()) {
                            comment += " (" + frenchCalendarDate + ")";
                        }
                    } catch (GedcomException ex) {
                        // rien a faire
                    }
                }
            }

            if (m_BirthPlace != null && !m_BirthPlace.isEmpty()) {
                if (comment.isEmpty()) {
                    comment = "né à" + " " + m_BirthPlace;
                } else {
                    comment += " " + "à" + " " + m_BirthPlace;
                }
            }
            return comment;
        }

        private String makeParticipantComment(boolean showFrenchCalendarDate) {
            String comment = appendValue(m_FirstName + " " + m_LastName,
                    m_Age == null || isEmpty(m_Age) ? "" : m_Age.toString(),
                    makeParticipantBirthComment(showFrenchCalendarDate),
                    m_Occupation,
                    appendPrefixValue("domicile", m_Residence == null ? "" : m_Residence),
                    m_Comment
            );
            return comment;
        }

        private String makeParticipantMarriedComment() {
            String comment = appendValue(
                    m_MarriedFirstName + " " + m_MarriedLastName,
                    m_MarriedDead.toString(),
                    m_MarriedOccupation,
                    appendPrefixValue("domicile", m_MarriedResidence),
                    m_MarriedComment
            );

            return comment;
        }

        private String makeParticipantFatherComment() {
            String comment = appendValue(
                    m_FatherFirstName + " " + m_FatherLastName,
                    m_FatherAge == null || isEmpty(m_FatherAge) ? "" : m_FatherAge.toString(),
                    m_FatherDead.toString(),
                    m_FatherOccupation,
                    appendPrefixValue("domicile", m_FatherResidence),
                    m_FatherComment
            );
            return comment;
        }

        private String makeParticipantMotherComment() {
            String comment = appendValue(
                    m_MotherFirstName + " " + m_MotherLastName,
                    m_MotherAge == null || isEmpty(m_MotherAge) ? "" : m_MotherAge.toString(),
                    m_MotherDead.toString(),
                    m_MotherOccupation,
                    appendPrefixValue("domicile", m_MotherResidence),
                    m_MotherComment
            );
            return comment;
        }
    }

    protected class MergeWitness {

        String getFirstName() {
            return firstName;
        }

        String getLastName() {
            return lastName;
        }

        String getOccupation() {
            return occupation;
        }

        String getComment() {
            return comment;
        }

        private String firstName;
        private String lastName;
        private String occupation;
        private String comment;
    }
}
