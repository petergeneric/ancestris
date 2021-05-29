package ancestris.modules.releve.merge;

import ancestris.modules.releve.dnd.TransferableRecord;
import ancestris.modules.releve.model.RecordInfoPlace;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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


    protected static enum DeadState {
        UNKNOWN,
        DEAD,
        ALIVE;

        public static String deadLabel = java.util.ResourceBundle.getBundle("ancestris/modules/releve/merge/Bundle").getString("MergeRecord.DeadState.Dead");
        public static String aliveLabel = java.util.ResourceBundle.getBundle("ancestris/modules/releve/merge/Bundle").getString("MergeRecord.DeadState.Alive");
        public static String unknownLabel = java.util.ResourceBundle.getBundle("ancestris/modules/releve/merge/Bundle").getString("MergeRecord.DeadState.Unknown");

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
    protected static class SexType {
        int sex = PropertySex.UNKNOWN;

        public static SexType fromString(String value) {
            return new SexType(value);
        }

        public SexType(String value) {
            sex = PropertySex.UNKNOWN;
            if ("M".equals(value)) {
                sex = PropertySex.MALE;
            } else if ("F".equals(value)) {
                sex = PropertySex.FEMALE;
            }
        }

        public int getSex() {
            return sex;
        }

        public static String getLabelForSex(int value) {
            switch(value) {
                case PropertySex.MALE:
                case PropertySex.FEMALE:
                    return PropertySex.getLabelForSex(value);
                default:
                    return "";

            }
        }

    }

    public enum MergeParticipantType {
        participant1,
        participant2
    };

    private final RecordInfoPlace recordInfoPlace = new RecordInfoPlace();
    private final String fileName;
    //private String eventSourceTitle;

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
    private RecordResidence m_eventResidence;

    private final RecordParticipant participant1;
    private final RecordParticipant participant2;
    private final RecordWitness witness1 = new RecordWitness();
    private final RecordWitness witness2 = new RecordWitness();
    private final RecordWitness witness3 = new RecordWitness();
    private final RecordWitness witness4 = new RecordWitness();
    private final RecordMarriageFamily family;

    /**
     * type du releve
     */
    public MergeRecord(TransferableRecord.TransferableData data) {

        fileName = data.fileName;

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
        eventDate = parseDateString(data.eventDate);
        eventSecondDate = parseDateString(data.secondDate);
        generalComment = data.generalComment;
        notary = data.notary;
        parish = data.parish;

        participant1 = new RecordParticipant(this, MergeParticipantType.participant1);
        participant1.m_FirstName              = formatFirstName(data.participant1.firstName);
        participant1.m_LastName               = data.participant1.lastName;
        participant1.m_Sex                    = SexType.fromString(data.participant1.sex);
        participant1.m_Age.setValue(data.participant1.age);
        participant1.m_BirthDate              = parseDateString(data.participant1.birthDate);
        participant1.m_BirthPlace             = data.participant1.birthPlace;
        participant1.m_BirthAddress           = data.participant1.birthAddress;
        participant1.m_Occupation             = data.participant1.occupation;
        participant1.m_Residence.m_place      = data.participant1.residence;
        participant1.m_Residence.m_address    = data.participant1.address;
        participant1.m_Comment                = data.participant1.comment;

        participant1.m_marriedFamily.m_married.m_firstName    = formatFirstName(data.participant1.marriedFirstName);
        participant1.m_marriedFamily.m_married.m_lastName     = data.participant1.marriedLastName;
        participant1.m_marriedFamily.m_married.m_sex          = participant1.getSex() == PropertySex.UNKNOWN || participant1.m_marriedFamily.m_married.m_lastName.isEmpty() ? new SexType("U") : participant1.getSex()== PropertySex.MALE ? new SexType("F"): new SexType("M");
        participant1.m_marriedFamily.m_married.m_comment      = data.participant1.marriedComment;
        participant1.m_marriedFamily.m_married.m_occupation   = data.participant1.marriedOccupation;
        participant1.m_marriedFamily.m_married.m_residence.m_place   = data.participant1.marriedResidence;
        participant1.m_marriedFamily.m_married.m_residence.m_address = data.participant1.marriedAddress;
        participant1.m_marriedFamily.m_married.m_dead         = DeadState.fromString(data.participant1.marriedDead);

        participant1.getFather().m_firstName    = formatFirstName(data.participant1.fatherFirstName);
        participant1.getFather().m_lastName     = data.participant1.fatherLastName;
        participant1.getFather().m_sex          = SexType.fromString("M");
        participant1.getFather().m_occupation   = data.participant1.fatherOccupation;
        participant1.getFather().m_residence.m_place   = data.participant1.fatherResidence;
        participant1.getFather().m_residence.m_address = data.participant1.fatherAddress;
        participant1.getFather().m_age.setValue(data.participant1.fatherAge);
        participant1.getFather().m_dead         = DeadState.fromString(data.participant1.fatherDead);
        participant1.getFather().m_comment      = data.participant1.fatherComment;

        participant1.getMother().m_firstName    = formatFirstName(data.participant1.motherFirstName);
        participant1.getMother().m_lastName     = data.participant1.motherLastName;
        participant1.getMother().m_sex          = SexType.fromString("F");
        participant1.getMother().m_occupation   = data.participant1.motherOccupation;
        participant1.getMother().m_residence.m_place    = data.participant1.motherResidence;
        participant1.getMother().m_residence.m_address      = data.participant1.motherAddress;
        participant1.getMother().m_age.setValue(data.participant1.motherAge);
        participant1.getMother().m_dead         = DeadState.fromString(data.participant1.motherDead);
        participant1.getMother().m_comment      = data.participant1.motherComment;

        participant2 = new RecordParticipant(this, MergeParticipantType.participant2);
        participant2.m_FirstName              = formatFirstName(data.participant2.firstName);
        participant2.m_LastName               = data.participant2.lastName;
        participant2.m_Sex                    = SexType.fromString(data.participant2.sex);
        participant2.m_Age.setValue(data.participant2.age);
        participant2.m_BirthDate              = parseDateString(data.participant2.birthDate);
        participant2.m_BirthPlace             = data.participant2.birthPlace;
        participant2.m_BirthAddress           = data.participant2.birthAddress;
        participant2.m_Occupation             = data.participant2.occupation;
        participant2.m_Residence.m_place      = data.participant2.residence;
        participant2.m_Residence.m_address    = data.participant2.address;
        participant2.m_Comment                = data.participant2.comment;

        participant2.m_marriedFamily.m_married.m_firstName    = formatFirstName(data.participant2.marriedFirstName);
        participant2.m_marriedFamily.m_married.m_lastName     = data.participant2.marriedLastName;
        participant2.m_marriedFamily.m_married.m_sex          = participant2.getSex() == PropertySex.UNKNOWN || participant2.m_marriedFamily.m_married.m_lastName.isEmpty() ? new SexType("U") : participant2.getSex()== PropertySex.MALE ? new SexType("F"): new SexType("M");
        participant2.m_marriedFamily.m_married.m_comment      = data.participant2.marriedComment;
        participant2.m_marriedFamily.m_married.m_occupation   = data.participant2.marriedOccupation;
        participant2.m_marriedFamily.m_married.m_residence.m_place   = data.participant2.marriedResidence;
        participant2.m_marriedFamily.m_married.m_residence.m_address = data.participant2.marriedAddress;
        participant2.m_marriedFamily.m_married.m_dead         = DeadState.fromString(data.participant2.marriedDead);

        participant2.getFather().m_firstName    = formatFirstName(data.participant2.fatherFirstName);
        participant2.getFather().m_lastName     = data.participant2.fatherLastName;
        participant2.getFather().m_sex          = SexType.fromString("M");
        participant2.getFather().m_occupation   = data.participant2.fatherOccupation;
        participant2.getFather().m_residence.m_place    = data.participant2.fatherResidence;
        participant2.getFather().m_residence.m_address  = data.participant2.fatherAddress;
        participant2.getFather().m_age.setValue(data.participant2.fatherAge);
        participant2.getFather().m_dead         = DeadState.fromString(data.participant2.fatherDead);
        participant2.getFather().m_comment      = data.participant2.fatherComment;

        participant2.getMother().m_firstName    = formatFirstName(data.participant2.motherFirstName);
        participant2.getMother().m_lastName     = data.participant2.motherLastName;
        participant2.getMother().m_sex          = SexType.fromString("F");
        participant2.getMother().m_occupation   = data.participant2.motherOccupation;
        participant2.getMother().m_residence.m_place = data.participant2.motherResidence;
        participant2.getMother().m_residence.m_address  = data.participant2.motherAddress;
        participant2.getMother().m_age.setValue(data.participant2.motherAge);
        participant2.getMother().m_dead         = DeadState.fromString(data.participant2.motherDead);
        participant2.getMother().m_comment      = data.participant2.motherComment;

        witness1.m_firstName  = formatFirstName(data.witness1.firstName);
        witness1.m_lastName   = data.witness1.lastName;
        witness1.m_occupation = data.witness1.occupation;
        witness1.m_comment    = data.witness1.comment;

        witness2.m_firstName  = formatFirstName(data.witness2.firstName);
        witness2.m_lastName   = data.witness2.lastName;
        witness2.m_occupation = data.witness2.occupation;
        witness2.m_comment    = data.witness2.comment;

        witness3.m_firstName  = formatFirstName(data.witness3.firstName);
        witness3.m_lastName   = data.witness3.lastName;
        witness3.m_occupation = data.witness3.occupation;
        witness3.m_comment    = data.witness3.comment;

        witness4.m_firstName  = formatFirstName(data.witness4.firstName);
        witness4.m_lastName   = data.witness4.lastName;
        witness4.m_occupation = data.witness4.occupation;
        witness4.m_comment    = data.witness4.comment;

        family = new RecordMarriageFamily( participant1, participant2);

    }

    private static final Pattern jjmmaaaa = Pattern.compile("([0-9]{1,2})/([0-9]{1,2})/([0-9]{4,4})");
    private static final Pattern mmaaaa = Pattern.compile("([0-9]{1,2})/([0-9]{4,4})");
    private static final Pattern aaaa = Pattern.compile("([0-9]{4,4})");

    private PropertyDate parseDateString(String dateString) {
        PropertyDate dateResult = new PropertyDate();
        String inputDate = dateString.trim();
        Matcher matcher = jjmmaaaa.matcher(inputDate);

        if (matcher.matches()) {
            PointInTime pit = new PointInTime(
                    Integer.parseInt(matcher.group(1)) - 1,
                    Integer.parseInt(matcher.group(2)) - 1,
                    Integer.parseInt(matcher.group(3)));
            dateResult.setValue(dateResult.getFormat(), pit, null, null);
        } else {
            matcher = mmaaaa.matcher(inputDate);
            if (matcher.matches()) {
                PointInTime pit = new PointInTime(
                        PointInTime.UNKNOWN,
                        Integer.parseInt(matcher.group(1)) - 1,
                        Integer.parseInt(matcher.group(2)));
                dateResult.setValue(dateResult.getFormat(), pit, null, null);
            } else {
                matcher = aaaa.matcher(inputDate);
                if (matcher.matches()) {
                    PointInTime pit = new PointInTime(
                            PointInTime.UNKNOWN,
                            PointInTime.UNKNOWN,
                            Integer.parseInt(matcher.group(1)));
                    dateResult.setValue(dateResult.getFormat(), pit, null, null);
                } else {
                    dateResult.setValue(inputDate);
                }
            }
        }
        return dateResult;

    }

    ///////////////////////////////////////////////////////////////////////////
    // accesseurs
    //////////////////////////////////////////////////////////////////////////
    RecordParticipant getIndi() {
        return participant1;
    }

    RecordParticipant getWife() {
        return participant2;
    }

    RecordMarriageFamily getFamily () {
        return family;
    }

    RecordParticipant getParticipant(MergeParticipantType mergeParticipantType) {
        if (mergeParticipantType == MergeParticipantType.participant1) {
            return participant1;
        } else {
            return participant2;
        }
    }

    RecordType getRecordType() {
        return recordType;
    }

    EventTypeTag getEventTypeTag() {
        return eventTypeTag;
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

    private String formatFirstName(String namePiece) {
        //return firstName.replaceAll(" *, *", GedcomOptions.getInstance().replaceSpaceSeparatorWithComma() ? ", " : " ");
        if (namePiece.isEmpty()) {
            return "";
        }
        String result = namePiece.trim().replaceAll(" +", " ").replaceAll(" *, *", ",");
        // FIXME: formatFirstName should certainly be tuned as PropertyName API has changed
//        if (GedcomOptions.getInstance().replaceSpaceSeparatorWithComma()) {
//            result = result.replaceAll(" +", ",");
//        }
        return result.replaceAll(",", ", ");
    }

    static String formatDateDDMMYYYY(PropertyDate dateProperty) {
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

    RecordResidence getEventResidence() {
        if(m_eventResidence == null) {
            m_eventResidence = new RecordResidence();
            m_eventResidence.m_address = "";
            m_eventResidence.m_place = recordInfoPlace.getValue();
        }
        return m_eventResidence;
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

    static boolean isEmpty(Delta delta) {
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
        comment = appendComment(comment, "Nouveau né", participant1.makeParticipantComment(showFrenchCalendarDate));
        comment = appendComment(comment, "Père", participant1.getFather().makeComment());
        comment = appendComment(comment, "Mère", participant1.getMother().makeComment());

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

        comment = appendComment(comment, "Epoux", participant1.makeParticipantComment(showFrenchCalendarDate));
        comment = appendComment(comment, "Ex conjoint époux", participant1.getMarriedFamily().getMarried().makeComment());
        comment = appendComment(comment, "Père époux", participant1.getFather().makeComment());
        comment = appendComment(comment, "Mère époux", participant1.getMother().makeComment());

        comment = appendComment(comment, "Epouse", participant2.makeParticipantComment(showFrenchCalendarDate));
        comment = appendComment(comment, "Ex conjoint épouse", participant2.getMarriedFamily().getMarried().makeComment());
        comment = appendComment(comment, "Père épouse", participant2.getFather().makeComment());
        comment = appendComment(comment, "Mère épouse", participant2.getMother().makeComment());

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

        comment = appendComment(comment, "Défunt", participant1.makeParticipantComment(showFrenchCalendarDate));
        comment = appendComment(comment, "Conjoint", participant1.getMarriedFamily().getMarried().makeComment());
        comment = appendComment(comment, "Père", participant1.getFather().makeComment());
        comment = appendComment(comment, "Mère", participant1.getMother().makeComment());

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

        comment = appendComment(comment, "Intervenant 1", participant1.makeParticipantComment(showFrenchCalendarDate));
        comment = appendComment(comment, "Conjoint intervenant 1", participant1.getMarriedFamily().getMarried().makeComment());
        comment = appendComment(comment, "Père intervenant 1", participant1.getFather().makeComment());
        comment = appendComment(comment, "Mère intervenant 1", participant1.getMother().makeComment());

        comment = appendComment(comment, "Intervenant 2", participant2.makeParticipantComment(showFrenchCalendarDate));
        comment = appendComment(comment, "Conjoint intervenant 2", participant2.getMarriedFamily().getMarried().makeComment());
        comment = appendComment(comment, "Père intervenant 2", participant2.getFather().makeComment());
        comment = appendComment(comment, "Mère intervenant 2", participant2.getMother().makeComment());

        comment = appendComment(comment, "Témoin(s)", makeWitnessComment());
        comment = appendComment(comment, "Commentaire général", generalComment);
        comment = appendComment(comment, "Cote", makeEventPage());

        return comment;
    }

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
    static protected String appendValue(String... otherValues) {
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
     * virgule.
     * N'affiche pas la premiere valeur si la suite est vide
     */
    static private String appendPrefixValue(String prefix, String... otherValues) {
        StringBuilder sb = new StringBuilder();
        for (String otherValue : otherValues) {
            // j'ajoute les valeurs supplémentaires séparées par des virgules
            if (otherValue != null && !otherValue.trim().isEmpty()) {
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
                        // l'age n'est pas valide

                        // date de naissance maximale BEF = date de mariage -  minMarriageYearOld
                        //birthDate= calulateDateBeforeMinusShift(eventDate, MergeQuery.minMarriageYearOld, "date 15 ans avant le mariage");
                        resultDate = calulateDateBeforeMinusShift(getEventDate(), MergeQuery.minMarriageYearOld, "=date mariage -" + MergeQuery.minMarriageYearOld);

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
    static private PropertyDate calulateDateBeforeMinusShift(PropertyDate birthDate, int yearShift, String phrase) {
        try {
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
        } catch (GedcomException ex) {
            // en cas d'exception de getYear je retourne une date invalide (non initialisée)
            Exceptions.printStackTrace(ex);
            return new PropertyDate();
        }
    }


    /**
     *
     */
    static protected abstract class RecordEntity  {
    }

    static protected abstract class RecordIndi extends RecordEntity {
        abstract String getLastName();
        abstract String getFirstName();
        abstract PropertyDate getBirthDate();
        abstract PropertyDate getDeathDate() throws Exception;
        abstract String getOccupationWithDate();
        abstract int getSex();
        abstract RecordResidence getBirthResidence();
        abstract RecordResidence getDeathResidence();
        abstract RecordResidence getResidence();
        abstract String getOccupation();

        @Override
        public String toString() {
            return getFirstName() + " " + getLastName();
        }
    }

    /**
     *
     */
    static protected abstract class RecordFamily extends RecordEntity {
        abstract RecordIndi getHusband();
        abstract RecordIndi getWife();
        abstract PropertyDate getMarriageDate();
    }

    /**
     *
     */
    static protected class RecordMarriageFamily extends RecordFamily {
        RecordParticipant m_husband;
        RecordParticipant m_wife;

        RecordMarriageFamily(RecordParticipant husband, RecordParticipant wife) {
            m_husband = husband;
            m_wife = wife;
        }

        @Override
        RecordIndi getHusband() {
            return m_husband;
        }
        @Override
        RecordIndi getWife() {
            return m_wife;
        }

        @Override
        PropertyDate getMarriageDate() {
            if (m_husband.getMergeRecord().getRecordType() == RecordType.MARRIAGE) {
                return m_husband.getMergeRecord().getEventDate();
            } else {
                //TODO
                return new PropertyDate();
            }
        }
    }

    /**
     *
     */
    static protected class RecordMarriedFamily extends RecordFamily {
        RecordParticipant m_indi;
        RecordMarried m_married;
        SpouseTag spousTag;

        private PropertyDate MarriedMarriageDate = null;

        RecordMarriedFamily(RecordParticipant indi) {
            m_indi = indi;
            m_married = new RecordMarried();
            if( m_indi.m_participantType == MergeRecord.MergeParticipantType.participant1) {
                spousTag = SpouseTag.HUSB;
            } else {
                spousTag = SpouseTag.WIFE;
            }
            init();
        }


        RecordMarriedFamily(RecordParticipant husband, RecordMarried wife ) {
            m_indi = husband;
            m_married = wife;
            spousTag = SpouseTag.HUSB;
            init();
        }

        RecordMarriedFamily(RecordMarried husband, RecordParticipant wife ) {
            m_indi = wife;
            m_married = husband;
            spousTag = SpouseTag.WIFE;
            init();
        }
        private void init() {
            m_indi.m_marriedFamily = this;
            m_married.m_marriedFamily = this;
        }

        @Override
        RecordIndi getHusband() {
            return spousTag == SpouseTag.HUSB ? m_indi : m_married;
        }

        @Override
        RecordIndi getWife() {
            return spousTag == SpouseTag.WIFE ? m_indi : m_married;
        }

        RecordMarried getMarried() {
            return m_married;
        }

        @Override
        PropertyDate getMarriageDate() {
            if (MarriedMarriageDate == null) {
                MarriedMarriageDate = new PropertyDate();
                if (m_married.m_firstName != null
                        && m_married.m_lastName != null
                        && (!m_married.m_firstName.isEmpty() || !m_married.m_lastName.isEmpty())
                        && m_indi.getMergeRecord().getEventDate().isComparable()) {
                    try {
                        // l'ex conjoint existe , le mariage avec l'individu est avant la date l'evenement
                        MarriedMarriageDate.setValue(PropertyDate.BEFORE, getYear(m_indi.getMergeRecord().getEventDate().getStart()), null, "mariage avant la date du relevé");
                    } catch (GedcomException ex) {
                        // en cas d'exception de getYear je retourne une date invalide (non initialisée
                        MarriedMarriageDate = new PropertyDate();
                        Exceptions.printStackTrace(ex);
                    }
                } else if (m_indi.m_participantType== MergeParticipantType.participant1 && m_indi.getMergeRecord().getRecordType() == RecordType.DEATH) {
                    //TODO la date du mariage est avant le deces
                    // non car il n'est pas forcément marié
                    //MarriedMarriageDate.setValue(PropertyDate.BEFORE, getYear(getEventDate().getStart()), null, "mariage avant la date du relevé");
                }
            }
            return MarriedMarriageDate;
        }
    }

    /**
     *
     */
    static protected class RecordParentFamily extends RecordFamily {
        RecordParticipant m_child;
        RecordParent m_father;
        RecordParent m_mother;
        private PropertyDate computedParentMarriageDate = null;

        RecordParentFamily( RecordParent father, RecordParent mother, RecordParticipant child) {
            m_child = child;
            m_father = father;
            m_mother = mother;
            init();
        }

        private void init() {
            m_father.m_family = this;
            m_mother.m_family = this;
        }

        @Override
        RecordParent getHusband() {
            return m_father;
        }
        @Override
        RecordParent getWife() {
            return m_mother;
        }

        @Override
        PropertyDate getMarriageDate() {
            if (computedParentMarriageDate == null) {
                computedParentMarriageDate = calculateParentMariageDate(m_child.getBirthDate(), m_child.getMergeRecord().getEventDate(), m_child.getMergeRecord().getRecordType());
            }
            return computedParentMarriageDate;
        }

        /**
         * calcule la date de mariage des parents
         *
         * @param record
         * @return
         */
        private PropertyDate calculateParentMariageDate(PropertyDate childBirthDate, PropertyDate eventDate, RecordType recordType) {
            PropertyDate parentMariageDate;
            if (recordType == RecordType.MARRIAGE) {
                // le mariage des parents est
                //   - avant la naissance de l'epoux
                //   - avant le mariage du marié diminué de minParentYearOld
                // date de mariage = BEF date de naissance de l'epoux
                PropertyDate beforeChildBirth = calulateDateBeforeMinusShift(childBirthDate, 0, "Date mariage= avant naissance époux(se)");
                // date de mariage = EventDate - minMarriageYearOld
                PropertyDate beforeChildMarriage = calulateDateBeforeMinusShift(eventDate, MergeQuery.minMarriageYearOld, "Date mariage = dete du releve -" + MergeQuery.minMarriageYearOld);
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
    }

    /**
     *
     */
    static protected class RecordParticipant extends RecordIndi {
        final private MergeParticipantType m_participantType;
        private final MergeRecord m_mergeRecord;

        private String m_FirstName;
        private String m_LastName;
        private SexType m_Sex;
        private Delta m_Age = new Delta(0,0,0);
        private PropertyDate m_BirthDate;
        private RecordResidence m_BirthResidence;
        private RecordResidence m_DeathResidence;
        private String m_BirthPlace;
        private String m_BirthAddress;
        private String m_Occupation;
        private final RecordResidence m_Residence = new RecordResidence();
        private String m_Comment;

        private RecordMarriedFamily m_marriedFamily;
        private final RecordParentFamily m_parentFamily;

        // memorise les dates calculees (pour éviter de les recalculer a chaque consultation)
        private PropertyDate computedBirthDate = null;
        private PropertyDate computedDeathDate = null;

        RecordParticipant(MergeRecord mergeRecord, MergeParticipantType participantType) {
            this.m_participantType = participantType;
            m_mergeRecord = mergeRecord;
            m_parentFamily = new RecordParentFamily(new RecordParent(), new RecordParent(), this);
            m_marriedFamily = new RecordMarriedFamily(this);
        }

        MergeRecord getMergeRecord() {
            return m_mergeRecord;
        }

        MergeParticipantType getParticipantType() {
            return m_participantType;
        }

        @Override
        String getFirstName() {
            return m_FirstName;
        }

        @Override
        String getLastName() {
            return m_LastName;
        }

        @Override
        int getSex() {
            return m_Sex.getSex();
        }

        @Override
        PropertyDate getBirthDate() {
            if (computedBirthDate == null) {
                computedBirthDate = m_mergeRecord.calculateBirthDate(m_participantType,
                        m_BirthDate != null ? m_BirthDate : new PropertyDate(),
                        m_Age != null ? m_Age : new Delta(0, 0, 0),
                        getMarriedFamily().getMarriageDate());
            }
            return computedBirthDate;
        }

        @Override
        PropertyDate getDeathDate() throws Exception {
            if (computedDeathDate == null) {
                computedDeathDate = m_mergeRecord.calculateDeathDate();
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
        @Override
        RecordResidence getBirthResidence() {
            if( m_BirthResidence == null) {
                if (m_BirthPlace != null && !m_BirthPlace.isEmpty()) {
                     m_BirthResidence = new RecordResidence(m_BirthPlace, m_BirthAddress);
                } else {
                    if (m_mergeRecord.recordType == RecordType.BIRTH && m_participantType == MergeParticipantType.participant1) {
                        if (m_Residence.m_place != null && !m_Residence.m_place.isEmpty()) {
                            m_BirthResidence = m_Residence;
                        } else {
                            if (!m_parentFamily.m_father.getResidence().getPlace().isEmpty()) {
                                m_BirthResidence = m_parentFamily.m_father.getResidence();
                            } else {
                                m_BirthResidence = m_mergeRecord.getEventResidence();
                            }
                        }
                    } else {
                        m_BirthResidence = new RecordResidence();
                    }
                }

            }
            return m_BirthResidence;
        }

        /**
         * retourne la residence de l'individu, ou à défaut EventPlace si le
         * deces est l'evenement principal
         *
         * @return
         */
        @Override
        RecordResidence getDeathResidence() {
            if( m_DeathResidence == null) {
                if (m_participantType == MergeParticipantType.participant1
                        && m_mergeRecord.recordType == RecordType.DEATH ) {
                    if (m_Residence.m_place != null && !m_Residence.m_place.isEmpty()) {
                        m_DeathResidence = m_Residence;
                    } else {
                        m_DeathResidence = new RecordResidence(m_mergeRecord.getEventPlace(), "");
                    }
                } else {
                    // je cree une residence vide
                    m_DeathResidence = new RecordResidence();
                }
            }
            return m_DeathResidence;
        }



        @Override
        String getOccupation() {
            if (m_Occupation != null) {
                return m_Occupation;
            } else {
                // n'est pas renseignée pour les naissances
                return null;
            }
        }

        @Override
        String getOccupationWithDate() {
            String occupation = appendValue(m_Occupation, m_Residence.m_place, m_Residence.m_address);
            if (!occupation.isEmpty()) {
                occupation += " (" + m_mergeRecord.getEventDate().getDisplayValue() + ")";
            }
            return occupation;
        }


        @Override
        RecordResidence getResidence() {
            return m_Residence;
        }

        String getPlace() {
            return m_Residence.m_place;
        }

        String getAddress() {
            return m_Residence.m_address;
        }


        RecordMarriedFamily getMarriedFamily() {
            return m_marriedFamily;
        }


        RecordParentFamily getParentFamily() {
            return m_parentFamily;
        }
        RecordParent getFather() {
            return m_parentFamily.m_father;
        }

        RecordParent getMother() {
            return m_parentFamily.m_mother;
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

            comment += appendPrefixValue(
                    comment.isEmpty() ? "né à"  :  " " + "à" ,
                    m_BirthAddress, m_BirthPlace);
            return comment;
        }

        private String makeParticipantComment(boolean showFrenchCalendarDate) {
            String comment = appendValue(m_FirstName + " " + m_LastName,
                    m_Age == null || isEmpty(m_Age) ? "" : m_Age.toString(),
                    makeParticipantBirthComment(showFrenchCalendarDate),
                    m_Occupation,
                    m_Residence.makeComment(),
                    m_Comment
            );
            return comment;
        }

    }

    static protected class RecordMarried extends RecordIndi {
        private String m_firstName;
        private String m_lastName;
        private SexType m_sex;
        private String m_occupation;
        private RecordResidence m_residence = new RecordResidence();
        private DeadState m_dead;
        private String m_comment;

        private PropertyDate computedBirthDate = null;
        private PropertyDate computedDeathDate = null;

        RecordMarriedFamily m_marriedFamily;

        RecordMarried() {

        }

        @Override
        String getFirstName() {
            return m_firstName;
        }

        @Override
        String getLastName() {
            return m_lastName;
        }

        @Override
        int getSex() {
            return m_sex.getSex();
        }

        @Override
        String getOccupation() {
            return m_occupation;
        }

        /**
         * ajouté pour implemeneter RecorIndi
         * @return
         */
        @Override
        RecordResidence getBirthResidence() {
            return new RecordResidence();
        }

        @Override
        RecordResidence getResidence() {
            return m_residence;
        }

        @Override
        RecordResidence getDeathResidence() {
            return new RecordResidence();
        }

        String makeComment() {
            String comment = appendValue(
                    m_firstName + " " + m_lastName,
                    m_dead.toString(),
                    m_occupation,
                    m_residence.makeComment(),
                    m_comment
            );
            return comment;
        }

        @Override
        PropertyDate getBirthDate() {
            if (computedBirthDate == null) {
                computedBirthDate = new PropertyDate();
                if (m_firstName != null
                        && m_lastName != null
                        && (!m_firstName.isEmpty() || !m_lastName.isEmpty())
                        && m_marriedFamily.getMarriageDate().isComparable()) {
                    try {
                        // l'ex conjoint existe , la naissance est minMarriageYearOld le mariage avec l'individu
                        computedBirthDate.setValue(PropertyDate.BEFORE, getYear(m_marriedFamily.getMarriageDate().getStart(), -MergeQuery.minMarriageYearOld), null, "naissance avant la date du mariage -" + MergeQuery.minMarriageYearOld);
                    } catch (GedcomException ex) {
                        // je laisse la date vide
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            return computedBirthDate;
        }

        @Override
        String getOccupationWithDate() {
            String occupation = appendValue(m_occupation, m_residence.m_address, m_residence.m_place);
            if (!occupation.isEmpty()) {
                occupation += " (" + m_marriedFamily.m_indi.getMergeRecord().getEventDate().getDisplayValue() + ")";
            }
            return occupation;
        }


        @Override
        PropertyDate getDeathDate() throws Exception {
            if (computedDeathDate == null) {
                computedDeathDate = new PropertyDate();

                switch (m_dead) {
                    case DEAD:
                        computedDeathDate.setValue(PropertyDate.BEFORE, getYear(m_marriedFamily.m_indi.getMergeRecord().getEventDate().getStart()), null, "deces avant la date du relevé");
                        break;
                    case ALIVE:
                        computedDeathDate.setValue(PropertyDate.AFTER, getYear(m_marriedFamily.m_indi.getMergeRecord().getEventDate().getStart()), null, "deces apres la date du relevé");
                        break;
                    default:
                        // je ne sais pas, la date reste vide
                        break;
                }
            }
            return computedDeathDate;
        }
    }

    static protected class RecordParent  extends RecordIndi {
        private String m_firstName;
        private String m_lastName;
        private SexType m_sex;
        private String m_occupation;
        private final RecordResidence m_residence = new RecordResidence();
        private Delta m_age  = new Delta(0,0,0);
        private DeadState m_dead;
        private String m_comment;

        private PropertyDate computedBirthDate = null;
        private PropertyDate computedDeathDate = null;
        RecordParentFamily m_family;

        RecordParent() {
        }

        @Override
        String getFirstName() {
            return m_firstName;
        }

        @Override
        String getLastName() {
            return m_lastName;
        }

        @Override
        int getSex() {
            return m_sex.getSex();
        }

        @Override
        String getOccupation() {
            return m_occupation;
        }

        @Override
        RecordResidence getBirthResidence() {
            return new RecordResidence();
        }

        @Override
        RecordResidence getResidence() {
            return m_residence;
        }

        @Override
        RecordResidence getDeathResidence() {
            return new RecordResidence();
        }

        @Override
        PropertyDate getBirthDate() {
            if (computedBirthDate == null) {
                try {
                    computedBirthDate = calculateParentBirthDate(m_age, m_family.m_child.getBirthDate(), m_family.m_child.getMergeRecord().getEventDate());
                } catch (Exception ex) {
                    computedBirthDate = new PropertyDate();
                    Exceptions.printStackTrace(ex);
                }
            }
            return computedBirthDate;
        }

        @Override
        PropertyDate getDeathDate() throws Exception {
            if (computedDeathDate == null) {
                computedDeathDate = calculateParentDeathDate(m_dead != null ? m_dead : DeadState.UNKNOWN,
                        m_family.m_child.getBirthDate(),
                        m_sex.getSex() == PropertySex.FEMALE ? 0 : 9 , // le mere peut être decedee au plus tot 0 mois avant le naissance (par opposition au pere qui peut etre decede 9 mois avant la naissance)
                        m_family.m_child.getMergeRecord().getEventDate()
                );
            }
            return computedDeathDate;
        }

        @Override
        String getOccupationWithDate() {
            String occupation = appendValue(m_occupation, m_residence.m_address, m_residence.m_place);
            if (!occupation.isEmpty()) {
                occupation += " (" + m_family.m_child.getMergeRecord().getEventDate().getDisplayValue() + ")";
            }
            return occupation;
        }

        String makeComment() {
            String comment = appendValue(
                    m_firstName + " " + m_lastName,
                    m_age == null || isEmpty(m_age) ? "" : m_age.toString(),
                    m_dead.toString(),
                    m_occupation,
                    m_residence.makeComment(),
                    m_comment
            );
            return comment;
        }

        /**
        * calcule la date de naissance d'un parent a partir - soit de l'age du
         * parent s'il est precisé dans le releve - soit de la date de naissance
         * de l'enfant si elle est precisée dans le relevé - soit de la date de
         * mariage de l'enfant (seulement pour le releve de mariage)
         *
         * @param requiredBirth
         * @return
         */
        private PropertyDate calculateParentBirthDate(Delta parentAge, PropertyDate childBirthDate, PropertyDate eventDate) throws Exception {

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
                    if (eventDate.getStart() != null) {
                        start = new PointInTime();
                        start.set(eventDate.getStart());
                        start.add(-parentAge.getDays(), -parentAge.getMonths(), -parentAge.getYears());
                    }
                    PointInTime end = null;
                    if (eventDate.getEnd() != null) {
                        end = new PointInTime();
                        end.set(eventDate.getEnd());
                        end.add(-parentAge.getDays(), -parentAge.getMonths(), -parentAge.getYears());
                    }
                    parentBirthDate.setValue(eventDate.getFormat(),
                            start, end, "Date naissance = date du releve - age");
                } else {
                    // date arrondie à l'année car l'age n'est pas précis au jour près.
                    ///parentBirthDate.setValue(String.format("CAL %d", parentBirthDate.getStart().add(0, 0, -parentAge.getYears()).getYear() ));
                    parentBirthDate.setValue(PropertyDate.CALCULATED, getYear(eventDate.getStart(), parentAge), null, "date naissance=date du releve - age");
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
         * calcule la date de deces d'un parent a partir de la date du relevé et
         * la date de naissance d'un enfant.
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
        private PropertyDate calculateParentDeathDate(DeadState dead, PropertyDate childBirthDate, int monthBeforeBirth, PropertyDate eventDate) throws Exception {

            PropertyDate parentDeathDate = new PropertyDate();

            if (eventDate.isComparable()) {
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
                            parentDeathDate.setValue(PropertyDate.BETWEEN_AND, getYear(childBirthPit), getYear(eventDate.getStart()), "date deces= entre la naissance de l'enfant et la date du releve");
                        } else {
                            parentDeathDate.setValue(PropertyDate.BETWEEN_AND, getYear(childBirthPit), getYear(eventDate.getStart()), "date deces= entre la conceptionde l'enfant et la date du releve");
                        }
                    } else {
                        // le parent est decede avant la date du releve
                        parentDeathDate.setValue(PropertyDate.BEFORE, getYear(eventDate.getStart()), null, "date deces= avant la date du releve");
                    }
                } else if (dead == DeadState.ALIVE) {
                    // le parent est decede apres la date du releve
                    parentDeathDate.setValue(PropertyDate.AFTER, getYear(eventDate.getStart()), null, "date deces= apres la date du releve");
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

    }

    static protected class RecordResidence {
        private String m_place;
        private String m_address;

        RecordResidence() {
            m_place = "";
            m_address = "";
        }

        RecordResidence(String place, String address) {
            m_place = place;
            m_address = address;
        }

        String getPlace() {
            return m_place;
        }

        String getAddress() {
            return m_address;
        }

        private String makeComment() {
            String comment = appendPrefixValue("domicile", m_address, m_place);
            return comment;
        }

        boolean isEmpty() {
            return m_address.isEmpty() && m_place.isEmpty();
        }

        @Override
        public String toString() {
            return makeComment();
        }
    }


    static protected class RecordWitness {
        private String m_firstName;
        private String m_lastName;
        private String m_occupation;
        private String m_comment;

        String getFirstName() {
            return m_firstName;
        }

        String getLastName() {
            return m_lastName;
        }

        String getOccupation() {
            return m_occupation;
        }

        String getComment() {
            return m_comment;
        }

    }
}
