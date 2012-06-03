package ancestris.modules.releve.dnd;

import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;


/**
 * Cette classe encapsule un objet de la classe Record
 * et ajoute des méthodes pour calculer les dates de naissance , mariage , deces
 * et des methodes pour assembler les commentaires.
 */
public class MergeRecord {
    private Record record;

    // memorise les dates calculees (pour éviter de les recalculer a chaque consultation)
    private PropertyDate IndiBirthDate = null;
    private PropertyDate IndiDeathDate = null;
    private PropertyDate IndiFatherBirthDate = null;
    private PropertyDate IndiFatherDeathDate = null;
    private PropertyDate IndiMotherBirthDate = null;
    private PropertyDate IndiMotherDeathDate = null;
    private PropertyDate IndiParentMarriageDate = null;

    private PropertyDate WifeBirthDate = null;
    private PropertyDate WifeDeathDate = null;
    private PropertyDate WifeFatherBirthDate = null;
    private PropertyDate WifeFatherDeathDate = null;
    private PropertyDate WifeMotherBirthDate = null;
    private PropertyDate WifeMotherDeathDate = null;
    private PropertyDate WifeParentMarriageDate = null;

    /**
     * liste des types de releves
     */
    protected static enum RecordType {
        Birth,
        Marriage,
        Death,
        Misc
    }
    /**
     * type du releve 
     */
    private RecordType type;


     /**
     * constructeur
     * @param record
     */
    protected MergeRecord( Record record)  {
        this.record = record;
        if (record instanceof RecordBirth) {
            type = RecordType.Birth;
        } else if (record instanceof RecordMarriage) {
            type = RecordType.Marriage;
        } else if (record instanceof RecordDeath) {
            type = RecordType.Death;
        } else {
            type = RecordType.Misc;
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // accesseurs
    //////////////////////////////////////////////////////////////////////////
    RecordType getType() {
        return type;
    }

    String getEventSource() {
        String cityName = record.getEventPlace().getCityName();
        String cityCode = record.getEventPlace().getCityCode();
        return String.format("%s %s Etat civil", cityCode, cityName);
    }

    PropertyDate getEventDate() {
        return record.getEventDateProperty();
    }
    String getEventDateDDMMYYYY() {
        return record.getEventDateString();
    }

    String getEventPlace() {
        return record.getEventPlace().toString();
    }

    String getEventPlaceCityName() {
        return record.getEventPlace().getCityName();
    }
    String getEventPlaceCityCode() {
        return record.getEventPlace().getCityCode();
    }
    String getEventPlaceCountyName() {
        return record.getEventPlace().getCountyName();
    }

    String getEventType() {
        return record.getEventType().toString();
    }
    String getNotary() {
        return record.getNotary().toString();
    }

    String getEventComment() {
        switch (type) {
            case Birth:
                return makeBirthComment();
            case Marriage:
                return makeMarriageComment();
            default:
                //TODO makeDeathComment();
                return "";
        }        
    }

    //  indi ///////////////////////////////////////////////////////////////////
    String getIndiFirstName() {
        return record.getIndiFirstName().toString();
    }

    String getIndiLastName() {
        return record.getIndiLastName().toString();
    }

    int getIndiSex() {
        return record.getIndiSex().getSex();
    }
    String getIndiSexString() {
        return record.getIndiSex().getValue();
    }

    PropertyDate getIndiBirthDate() throws Exception {
        if (IndiBirthDate == null) {
            IndiBirthDate = calculateBirthDate(
                record.getIndiBirthDate()!=null ? record.getIndiBirthDate().getPropertyDate() : new PropertyDate(),
                record.getIndiAge()!=null ? record.getIndiAge().getDelta() : new Delta(0,0,0) );
        }
        return IndiBirthDate;
    }

    PropertyDate getIndiDeathDate() throws Exception {
        if (IndiDeathDate == null) {
            if ( type == RecordType.Death) {
                IndiDeathDate = getEventDate();
            } else {
                // date de deces inconnue
                IndiDeathDate = new PropertyDate();
            }
        }
        return IndiDeathDate;
    }

    String getIndiPlace() {
        if ( record.getIndiPlace() != null) {
            return record.getIndiPlace().toString();
        } else {
            return "";
        }
    }

    String getIndiOccupation() {
        return record.getIndiOccupation().toString();
    }

    //  conjoint (ou ancien conjoint) //////////////////////////////////////////
//    String          indiMarriedFirstName;
//    String          indiMarriedLastName;
//    String          indiMarriedDead;
//    String          indiMarriedOccupation;
//    //  indi father ////////////////////////////////////////////////////////////

    PropertyDate getIndiParentMarriageDate() throws Exception {
        if (IndiParentMarriageDate == null) {
            IndiParentMarriageDate = calculateParentMariageDate(getIndiBirthDate());
        }
        return IndiParentMarriageDate;
    }

    String getIndiFatherFirstName() {
        return record.getIndiFatherFirstName().toString();
    }

    String getIndiFatherLastName() {
        return record.getIndiFatherLastName().toString();
    }

    PropertyDate getIndiFatherBirthDate() throws Exception {
        if (IndiFatherBirthDate == null) {
            IndiFatherBirthDate = calculateParentBirthDate(record.getIndiFatherAge().getDelta(),getIndiBirthDate());
        }
        return IndiFatherBirthDate;
    }

    PropertyDate getIndiFatherDeathDate() throws Exception {
        if (IndiFatherDeathDate == null) {
            IndiFatherDeathDate = calculateParentDeathDate(
                   record.getIndiFatherDead()!=null ? record.getIndiFatherDead().getState() :false,
                   getIndiBirthDate(),
                   9 // le pere peut être decede au plus tot 9 mois avant la naissance
               );
        }
        return IndiFatherDeathDate;
    }

    String getIndiFatherOccupation() {
        return record.getIndiFatherOccupation().toString();
    }

    String getIndiMotherFirstName() {
        return record.getIndiMotherFirstName().toString();
    }

    String getIndiMotherLastName() {
        return record.getIndiMotherLastName().toString();
    }

    PropertyDate getIndiMotherBirthDate() throws Exception {
        if (IndiMotherBirthDate == null) {
            IndiMotherBirthDate = calculateParentBirthDate(record.getIndiMotherAge().getDelta(), getIndiBirthDate());
        }
        return IndiMotherBirthDate;
    }

    PropertyDate getIndiMotherDeathDate() throws Exception {
        if (IndiMotherDeathDate == null) {
            IndiMotherDeathDate = calculateParentDeathDate(
                   record.getIndiMotherDead()!=null ? record.getIndiMotherDead().getState() : false,
                   getIndiBirthDate(),
                   0 // le mere peut être decedee au plus toto 0 mois avant le naissance.
               );
        }
        return IndiMotherDeathDate;
    }

    String getIndiMotherOccupation() {
        return record.getIndiMotherOccupation().toString();
    }

    //  wife ///////////////////////////////////////////////////////////////////

    String getWifeFirstName() {
        return record.getWifeFirstName().toString();
    }

    String getWifeLastName() {
        return record.getWifeLastName().toString();
    }

    String getWifeSex() {
        return record.getWifeSex().toString();
    }

    PropertyDate getWifeBirthDate() throws Exception {
        if (WifeBirthDate == null) {
            WifeBirthDate = calculateBirthDate(
                record.getWifeBirthDate()!=null ? record.getWifeBirthDate().getPropertyDate() : new PropertyDate(),
                record.getWifeAge()!=null ? record.getWifeAge().getDelta() : new Delta(0,0,0) );
        }
        return WifeBirthDate;
    }

    PropertyDate getWifeDeathDate() throws Exception {
        if (WifeDeathDate == null) {
            if ( type == RecordType.Death) {
                IndiDeathDate = getEventDate();
            } else {
                IndiDeathDate = new PropertyDate();
            }
        }
        return WifeDeathDate;
    }

    String getWifePlace() {
        return record.getWifePlace().toString();
    }

    String getWifeOccupation() {
        return record.getWifeOccupation().toString();
    }

    //  conjoint (ou ancien conjoint) //////////////////////////////////////////
//    String          wifeMarriedFirstName;
//    String          wifeMarriedLastName;
//    String          wifeMarriedDead;
//    String          wifeMarriedOccupation;
//    //  wife father ////////////////////////////////////////////////////////////

    PropertyDate getWifeParentMarriageDate() throws Exception {
        if (WifeParentMarriageDate == null) {
            WifeParentMarriageDate = calculateParentMariageDate(getWifeBirthDate());
        }
        return WifeParentMarriageDate;
    }

    String getWifeFatherFirstName() {
        return record.getWifeFatherFirstName().toString();
    }

    String getWifeFatherLastName() {
        return record.getWifeFatherLastName().toString();
    }

    PropertyDate getWifeFatherBirthDate() throws Exception {
        if (WifeFatherBirthDate == null) {
            WifeFatherBirthDate = calculateParentBirthDate(record.getWifeFatherAge().getDelta(), getWifeBirthDate());
        }
        return WifeFatherBirthDate;
    }

    PropertyDate getWifeFatherDeathDate() throws Exception {
        if (WifeFatherDeathDate == null) {
             WifeFatherDeathDate = calculateParentDeathDate(
                   record.getWifeFatherDead()!=null ? record.getWifeFatherDead().getState() :false,
                   getWifeBirthDate(),
                   9 // le pere peut être decede avant la naissance - 9 mois
               );
        }
        return WifeFatherDeathDate;
    }

    String getWifeFatherOccupation() {
        return record.getWifeFatherOccupation().toString();
    }

    String getWifeMotherFirstName() {
        return record.getWifeMotherFirstName().toString();
    }

    String getWifeMotherLastName() {
        return record.getWifeMotherLastName().toString();
    }

    PropertyDate getWifeMotherBirthDate() throws Exception {
        if (WifeMotherBirthDate == null) {
            WifeMotherBirthDate = calculateParentBirthDate(record.getWifeMotherAge().getDelta(), getWifeBirthDate());
        }
        return WifeMotherBirthDate;
    }

    PropertyDate getWifeMotherDeathDate() throws Exception {
        if (WifeMotherDeathDate == null) {
            WifeMotherDeathDate = calculateParentDeathDate(
                   record.getWifeMotherDead()!=null ? record.getWifeMotherDead().getState() : false,
                   getWifeBirthDate(),
                   0 // le mere peut être decedee 0 mois avant le naissance (= juste apres la naissance)
               );
        }
        return WifeMotherDeathDate;
    }

    String getWifeMotherOccupation() {
        return record.getWifeMotherOccupation().toString();
    }


   

    /**
     * genere le commentaire de la naissance
     * composé de :
     *     commentaire de l'individu ,
     *     commentaire genéral
     *     commentaire photo
     *     commentaire père
     *     commentaire mère
     *     noms, prénom et commentaires des parrain, marraine et témoins
     *
     * @return
     */
    private String makeBirthComment() {
        String comment = appendValue(record.getIndiComment().toString());

        String generalComment = appendValue(
                record.getGeneralComment().toString(),
                record.getFreeComment().toString() );
        if (!generalComment.isEmpty()) {
            if (!generalComment.isEmpty()) {
                comment += "\n";
            }
            comment += generalComment;
        }

        String fatherComment = appendValue(
                record.getIndiFatherComment().toString() );
        if (!fatherComment.isEmpty()) {
            if (!fatherComment.isEmpty()) {
                comment += "\n";
            }
            comment += "Commentaire père" + ": " + fatherComment;
        }

        String motherComment = appendValue(
                record.getIndiMotherComment().toString() );
        if (!motherComment.isEmpty()) {
            if (!motherComment.isEmpty()) {
                comment += "\n";
            }
            comment += "Commentaire mère" + ": " + motherComment;
        }

        String godFather = appendValue(
                record.getWitness1FirstName().toString() + " " + record.getWitness1LastName().toString(),
                record.getWitness1Occupation().toString(),
                record.getWitness1Comment().toString());
        if (!godFather.isEmpty()) {
            if (!comment.isEmpty()) {
                comment += "\n";
            }
            comment += "Parrain/témoin" + ": " + godFather;
        }
        String godMother = appendValue(
                record.getWitness2FirstName().toString() + " " + record.getWitness2LastName().toString(),
                record.getWitness2Occupation().toString(),
                record.getWitness2Comment().toString());
        if (!godMother.isEmpty()) {
            if (!comment.isEmpty()) {
                comment += "\n";
            }
            comment += "Marraine/témoin" + ": " + godMother;
        }

        String witness = appendValue(
                record.getWitness3FirstName().toString() + " " + record.getWitness3LastName().toString(),
                record.getWitness3Occupation().toString(),
                record.getWitness3Comment().toString(),
                record.getWitness4FirstName().toString() + " " + record.getWitness4LastName().toString(),
                record.getWitness4Occupation().toString(),
                record.getWitness4Comment().toString());
        if (!witness.isEmpty()) {
            if (!comment.isEmpty()) {
                comment += "\n";
            }
            comment += "Témoin(s)" + ": " + witness;
        }
        return comment;
    }

    /**
     * genere le commentaire de la naissance
     * en concatenant :
     *     commentaire general,
     *     commentaire libre,
     *     commentaire de l'epoux
     *     commentaire de l'epouse
     *     noms, prénom et commentaires des témoins
     *
     * @return
     */
    private String makeMarriageComment() {
        String comment = appendValue(
                record.getGeneralComment().toString(),
                record.getFreeComment().toString(),
                record.getIndiComment().toString(),
                record.getWifeComment().toString()
                );

        String witness = appendValue(
                record.getWitness1FirstName().toString() + " " + record.getWitness1LastName().toString(),
                record.getWitness1Occupation().toString(),
                record.getWitness1Comment().toString(),
                record.getWitness2FirstName().toString() + " " + record.getWitness2LastName().toString(),
                record.getWitness2Occupation().toString(),
                record.getWitness2Comment().toString(),
                record.getWitness3FirstName().toString() + " " + record.getWitness3LastName().toString(),
                record.getWitness3Occupation().toString(),
                record.getWitness3Comment().toString(),
                record.getWitness4FirstName().toString() + " " + record.getWitness4LastName().toString(),
                record.getWitness4Occupation().toString(),
                record.getWitness4Comment().toString());
        if (!witness.isEmpty()) {
            if (!comment.isEmpty()) {
                comment += "\n";
            }
            comment += "Témoin(s)" + ": " + witness;
        }
        return comment;
    }


    /**
     * concatene plusieurs commentaires dans une chaine , séparés par une virgule
     */
    private String appendValue(String value, String... otherValues) {
        int fieldSize = value.length();
        StringBuilder sb = new StringBuilder();
        sb.append(value.trim());
        for (String otherValue : otherValues) {
            // j'ajoute les valeurs supplémentaires séparées par des virgules
            if (!otherValue.trim().isEmpty()) {
                // je concantene les valeurs en inserant une virgule dans
                // si la valeur précedente n'est pas vide
                if (fieldSize > 0) {
                    sb.append(", ");
                }
                sb.append(otherValue.trim());
                fieldSize += otherValue.length();
            }
        }

        return sb.toString();
    }

    /**
     * calcule la date de naissance de l'individu ou de l'epouse
     *  si record=RecordBirth :
     *    retourne la date de naissance de l'individu ou a defaut date de l'evenement (c'est souvent la meme sauf si l'evenement est un bapteme qui a lieu apres la naissance)
     *  si record=RecordMarriage :
     *    retourne la date de naissance de l'individu ou a defaut dateBirth = date de son mariage -  minMarriageYearOld
     * @param requiredBirth =IndiBirthDate ou WifeBirthDate
     * @return
     */
    private PropertyDate calculateBirthDate(PropertyDate birthDate, Delta age) throws Exception {
        //PropertyDate birthDate;

        if (type == RecordType.Birth) {
            if ( !birthDate.isComparable() ) {
                birthDate = getEventDate();
            }
        } else if (type == RecordType.Marriage) {

            if ( ! birthDate.isComparable() &&  record.getEventDateProperty().isComparable()) {
                // la date de naissance n'est pas valide, 
                // je calcule la naissance a partir de l'age ou de la date de mariage
                if (age.getYears() != 0 ) {
                    // l'age est valide,
                    if (age.getYears() != 0 && age.getMonths() != 0 && age.getDays() != 0) {
                        // l'age est precis au jour près
                        // date de naissance = date de mariage - age
                        birthDate.setValue(record.getEventDateProperty().getValue());
                        birthDate.getStart().add(-age.getDays(), -age.getMonths(), -age.getYears());
                    } else {
                        // l'age n'est pas précis au jour près.
                        // date de naissance = date de mariage (arrondi a l'année) - age (arrondi à l'année)
                        birthDate.setValue(String.format("CAL %d", record.getEventDateProperty().getStart().getYear()));
                        birthDate.getStart().add(0, 0, -age.getYears());
                    }
                } else {
                    // l'age n'est pas valide
                    // date de naissance maximale = date de mariage -  minMarriageYearOld
                    birthDate= getDateBeforeMinusShift(record.getEventDateProperty(), MergeQuery.minMarriageYearOld);
                }
            }
        } else {
            // date inconnue
            birthDate = new PropertyDate();
        }

        return birthDate;
    }


    
    /**
     * calcule la date de naissance d'un parent a partir 
     *  - soit de l'age du parent s'il est precisé dans le releve
     *  - soit de la date de naissance de l'enfant si elle est precisée dans le relevé
     *  - soit de la date de mariage de l'enfant (seulement pour le releve de mariage)
     * @param requiredBirth
     * @return
     */
    private PropertyDate calculateParentBirthDate(Delta parentAge, PropertyDate childBirthDate) throws Exception {

        PropertyDate parentBirthDate = new PropertyDate();
        parentBirthDate.setValue(childBirthDate.getValue());

        if ( parentAge.getYears()!=0 && record.getEventDateProperty().isComparable() ) {
            // l'age du parent est valide
            // parentBirthDate = eventDate - parentAge
            parentBirthDate.setValue(record.getEventDateProperty().getValue());
            if( parentAge.getYears()!=0 ) {
                // l'age est valide
                if( parentAge.getYears()!=0 && parentAge.getMonths()!=0 &&  parentAge.getDays()!=0) {
                    //l'age est precis au jour pres.
                    parentBirthDate.getStart().add(-parentAge.getDays(), -parentAge.getMonths(), -parentAge.getYears());
                } else {
                    // date appromimative car l'age n'est pas précis au jour près.
                    parentBirthDate.setValue(String.format("CAL %d", parentBirthDate.getStart().add(0, 0, -parentAge.getYears()).getYear() ));
                }
            }
        } else {
            // l'age du parent n'est pas valide
            // je calcule la date maximale à partir de la date de mariage ou la date de naissance de l'enfant
            if ( parentBirthDate.isComparable()) {
                if ( type ==  RecordType.Marriage) {
                    // le parent doit être né  "minParentYearOld + minMarriageYearOld" années avant la naissance de l'individu
                    // parentBirthDate = eventDat - minParentYearOld -minMarriageYearOld
                    parentBirthDate.setValue(String.format("BEF %d",
                            parentBirthDate.getStart().add(0, 0, -MergeQuery.minParentYearOld  -MergeQuery.minMarriageYearOld).getYear()));
                } else {
                    // le parent doit être né  "minParentYearOld" années avant la naissance de l'individu
                    // parentBirthDate = eventDat - minParentYearOld
                    parentBirthDate.setValue(String.format("BEF %d", parentBirthDate.getStart().add(0, 0, -MergeQuery.minParentYearOld).getYear()));
                }
            } else {
                if ( type == RecordType.Marriage ) {
                    // le parent doit être né  "minParentYearOld" années avant la naissance de l'individu
                    parentBirthDate.setValue(String.format("BEF %d", parentBirthDate.getStart().add(0, 0, -MergeQuery.minParentYearOld).getYear()));
                } else {
                    // je sais pas calculer la date de naissance du paretn
                    // parentBirthDate = date nulle
                    parentBirthDate = new PropertyDate();
                }
            }
        }

        return parentBirthDate;
    }

    /**
     * calcule la date de deces d'un parent
     * @param record
     * @param dead      true si le parent est decede avant la date de l'evenement
     * @param birthDate date de naissance de l'enfant
     * @param monthBeforeBirth nombre de mois du deces avant la naissance de l'enfant (9 mois pour le pere, 0 mois pour la mere)
     * @return date de deces du parent
     */
    private PropertyDate calculateParentDeathDate(boolean dead, PropertyDate birthDate, int monthBeforeBirth ) throws Exception {

        PropertyDate parentDeathDate = new PropertyDate();
        
        if (getEventDate().isComparable()) {
            // la naissance de l'individu n'est utilisable pour determiner la date de minimale de deces du pere
            // que si c'est une date exacte ou une date avec une borne inferieure
            // J'ignore la date si c'est une date maximale (BEF) ou estimée
            PointInTime birthPit = new PointInTime();
            boolean birthDateUsefull;
            if (birthDate.isValid()) {
                if( birthDate.getFormat()== PropertyDate.DATE
                      ||  birthDate.getFormat()== PropertyDate.AFTER
                      ||  birthDate.getFormat()== PropertyDate.BETWEEN_AND
                      ||  birthDate.getFormat()== PropertyDate.CALCULATED
                    ) {
                    birthDateUsefull = true;
                    birthPit.set(birthDate.getStart());
                    birthPit.add(0, -monthBeforeBirth, 0);
                } else {
                    birthDateUsefull = false;
                }
            } else {
                birthDateUsefull = false;
            }
            
            if ( dead) {
                if (birthDateUsefull) {
                    // le parent est decede entre la date de naissance et la date du releve
                    //parentDeathDate.setValue(String.format("BEF %d", getEventDate().getStart().getYear()));
                    parentDeathDate.setValue(String.format("BET %d AND %d", birthPit.getYear(), getEventDate().getStart().getYear()));
                } else {
                    // le parent est decede avant la date du releve
                    parentDeathDate.setValue(String.format("BEF %d", getEventDate().getStart().getYear()));
                }
            } else {
                if (birthDateUsefull ) {
                    // le parent est decede apres la naissance 
                    parentDeathDate.setValue(String.format("AFT %d", birthPit.getYear()));
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
     * @param record
     * @return
     */
    private PropertyDate calculateParentMariageDate(PropertyDate childBirthDate ) throws Exception {
        PropertyDate parentMariageDate;
        if (record instanceof RecordBirth) {
            // le mariage des parents est avant la naissance de l'enfant arrondie a l'année
            parentMariageDate = getDateBeforeMinusShift(childBirthDate, 0);

        } else if (record instanceof RecordMarriage) {
            // le mariage des parents est
            //   - avant la naissance de l'epoux
            //   - avant le mariage du marié dminué de minParentYearOld
            // date de mariage = BEF date de naissance de l'epoux
            PropertyDate beforeChildBirth = getDateBeforeMinusShift(childBirthDate, 0);
            // date de mariage = EventDate - minMarriageYearOld
            PropertyDate beforeChildMarriage = getDateBeforeMinusShift(getEventDate(), MergeQuery.minMarriageYearOld);
            // je retiens la date la plus petite
            boolean beforeChildBirthComparable = beforeChildBirth.isComparable();
            boolean beforeChildMarriageComparable = beforeChildMarriage.isComparable();
            if (beforeChildBirthComparable && beforeChildMarriageComparable) {
                // les dates sont comparables , je retiens la plus petite
                if (MergeQuery.isBestBirthDate(beforeChildBirth, beforeChildMarriage)) {
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
            // date inconnue
            parentMariageDate = new PropertyDate();
        }
        return parentMariageDate;
    }


    /**
     * retourne une date arrondie à l'année immediatement inferieure ou egale
     * a la date donnée, dimminée de yearShift
     * @param birthDate
     * @param yearShift
     * @return
     */
    static private PropertyDate getDateBeforeMinusShift(PropertyDate birthDate, int yearShift) {
        // le mariage des parents est avant la naissance de l'enfant arrondie a l'année
        PropertyDate marriageDate = new PropertyDate();
        if (birthDate.getFormat() == PropertyDate.BETWEEN_AND || birthDate.getFormat() == PropertyDate.FROM_TO) {
            marriageDate.setValue(String.format("BEF %d", birthDate.getEnd().getYear() - yearShift));
        } else if (birthDate.getFormat() == PropertyDate.FROM || birthDate.getFormat() == PropertyDate.AFTER) {
            // Unknwon
            marriageDate.setValue("");
        } else if (birthDate.getFormat() == PropertyDate.TO || birthDate.getFormat() == PropertyDate.BEFORE) {
            marriageDate.setValue(String.format("BEF %d", birthDate.getStart().getYear() - yearShift));
        } else if (birthDate.getFormat() == PropertyDate.ABOUT || birthDate.getFormat() == PropertyDate.ESTIMATED) {
            marriageDate.setValue(String.format("BEF %d", birthDate.getStart().getYear() - yearShift));
        } else {
            marriageDate.setValue(String.format("BEF %d", birthDate.getStart().getYear() - yearShift));
        }
        return marriageDate;
    }

}
