package ancestris.modules.releve.dnd;

import ancestris.modules.releve.dnd.MergeModel.RowType;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import genj.gedcom.PropertyDate;


/**
 * Cette classe encapsule un abjet de la classe Record
 * et ajoute des méthodes pour calculer les dates de naissance , mariage , deces
 * et des methodes pour assembler les commentaires.
 */
public class MergeRecord {
    private Record record;

    // memore les dates calculees (pour éviter de les recalculer a chaque consultation)
    PropertyDate IndiBirthDate = null;
    PropertyDate IndiFatherBirthDate = null;
    PropertyDate IndiFatherDeathDate = null;
    PropertyDate IndiMotherBirthDate = null;
    PropertyDate IndiMotherDeathDate = null;
    PropertyDate IndiParentMarriageDate = null;

    PropertyDate WifeBirthDate = null;
    PropertyDate WifeFatherBirthDate = null;
    PropertyDate WifeFatherDeathDate = null;
    PropertyDate WifeMotherBirthDate = null;
    PropertyDate WifeMotherDeathDate = null;
    PropertyDate WifeParentMarriageDate = null;

    /**
     * liste des types de ligne
     */
    static protected enum RecordType {
        Birth,
        Marriage,
        Death,
        Misc
    }
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
                //TODO makeDeathComment
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
            IndiBirthDate = getBirthDate(RowType.IndiBirthDate);
        }
        return IndiBirthDate;
    }

    PropertyDate getIndiDeathDate() {
        //TODO getIndiDeathDate
        return null;
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
            IndiParentMarriageDate = getParentMariageDate(RowType.IndiParentMarriageDate);
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
            IndiFatherBirthDate = getParentBirthDate(RowType.IndiFatherBirthDate);
        }
        return IndiFatherBirthDate;
    }

    PropertyDate getIndiFatherDeathDate() throws Exception {
        if (IndiFatherDeathDate == null) {
            IndiFatherDeathDate = getParentDeathDate(RowType.IndiFatherDeathDate);
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
            IndiMotherBirthDate = getParentBirthDate(RowType.IndiMotherBirthDate);
        }
        return IndiMotherBirthDate;
    }

    PropertyDate getIndiMotherDeathDate() throws Exception {
        if (IndiMotherDeathDate == null) {
            IndiMotherDeathDate = getParentDeathDate(RowType.IndiMotherDeathDate);
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
            WifeBirthDate = getBirthDate(RowType.WifeBirthDate);
        }
        return WifeBirthDate;
    }

    PropertyDate getWifeDeathDate() {
        //TODO getWifeDeathDate
        return null;
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
            WifeParentMarriageDate = getParentMariageDate(RowType.WifeParentMarriageDate);
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
            WifeFatherBirthDate = getParentBirthDate(RowType.WifeFatherBirthDate);
        }
        return WifeFatherBirthDate;
    }

    PropertyDate getWifeFatherDeathDate() throws Exception {
        if (WifeFatherDeathDate == null) {
            WifeFatherDeathDate = getParentDeathDate(RowType.WifeFatherDeathDate);
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
            WifeMotherBirthDate = getParentBirthDate(RowType.WifeMotherBirthDate);
        }
        return WifeMotherBirthDate;
    }

    PropertyDate getWifeMotherDeathDate() throws Exception {
        if (WifeMotherDeathDate == null) {
            WifeMotherDeathDate = getParentDeathDate(RowType.WifeMotherDeathDate);
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
    public String appendValue(String value, String... otherValues) {
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
     *    retourne la date de naissance de l'individu ou a defaut date de l'evenement
     *  si record=RecordMarriage :
     *    retourne la date de naissance de l'individu ou a defaut dateBirth = date de son mariage -  minMarriageYearOld
     * @param record
     * @param requiredBirth =IndiBirthDate ou WifeBirthDate
     * @return
     */
    PropertyDate getBirthDate(RowType requiredBirth) throws Exception {
        PropertyDate birthDate;
        if (record instanceof RecordBirth) {
            switch ( requiredBirth) {
                case IndiBirthDate:
                    // j'intialise la date avec celle de l'individu
                    birthDate = record.getIndiBirthDate().isEmpty() ? record.getEventDateProperty() : record.getIndiBirthDate().getPropertyDate();
                    break;
                case WifeBirthDate:
                    // date inconnue , la naissance de la femme n'est pas indiqué dans le releve de naissance d'un individu.
                    birthDate = new PropertyDate();
                    break;
                default:
                    throw new Exception("RowType not allowed"+requiredBirth );
            }

        } else if (record instanceof RecordMarriage) {
            // TODO : calculer la date de naissance en fonction de l'age
            switch ( requiredBirth) {
                case IndiBirthDate:
                    // j'intialise la date avec la daet de naissance  de l'individu
                    birthDate = record.getIndiBirthDate().getPropertyDate();
                    break;
                case WifeBirthDate:
                    // date inconnue , la naissance de la femme n'est pas indiqué dans le releve de naissance d'un individu.
                    birthDate = record.getWifeBirthDate().getPropertyDate();
                    break;
                default:
                    throw new Exception("RowType not allowed"+requiredBirth );
            }

            if ( ! birthDate.isComparable() &&  record.getEventDateProperty().isComparable()) {
                // si la date de naisasnce n'est pas valide
                // date de son mariage -  minMarriageYearOld
                birthDate= getDateBeforeMinusShift(record.getEventDateProperty(), MergeQuery.minMarriageYearOld);
            }
        } else {
            // date inconnue
            birthDate = new PropertyDate();
        }

        return birthDate;
    }


    /**
     * calcule la date de naissance d'un parent a partir de la date de naissance d'un enfant
     * ou de la date de mariage de l'enfant
     * @param record
     * @return
     */
    PropertyDate getParentBirthDate(RowType requiredBirth) throws Exception {

        PropertyDate parentBirthDate = new PropertyDate();

        switch ( requiredBirth) {
            case IndiFatherBirthDate:
            case IndiMotherBirthDate:
                // j'intialise la date avec celle de l'individu
                parentBirthDate.setValue(getBirthDate(RowType.IndiBirthDate).getValue());
                break;
            case WifeFatherBirthDate:
            case WifeMotherBirthDate:
                // j'intialise la date avec celle de l'epouse
                parentBirthDate.setValue(getBirthDate(RowType.WifeBirthDate).getValue());
                break;
            default:
                throw new Exception("RowType not allowed"+requiredBirth );
        }

        if (record instanceof RecordBirth) {
            // le parent doit être né avant minParentYearOld avant la naissance
            parentBirthDate.setValue(String.format("BEF %d", parentBirthDate.getStart().add(0, 0, -MergeQuery.minParentYearOld).getYear()));
        } else if (record instanceof RecordMarriage) {
            // le parent doit être né avant minParentYearOld avant la naissance
            parentBirthDate.setValue(String.format("BEF %d", parentBirthDate.getStart().add(0, 0, -MergeQuery.minParentYearOld).getYear()));
        } else {
            throw new Exception("Not implemented" );
        }
        return parentBirthDate;
    }

    /**
     * calcule la date de deces d'un parent
     * @param record
     * @param requiredDeath parent concerné : IndiFatherDeathDate, IndiMotherDeathDate, WifeFatherDeathDate, WifeMotherDeathDate
     * @return date de deces du parent
     */
    PropertyDate getParentDeathDate(RowType requiredDeath) throws Exception {

        PropertyDate parentDeathDate = new PropertyDate();
         switch ( requiredDeath) {
            case IndiFatherDeathDate:
            case IndiMotherDeathDate:
                // j'initialise la date avec celle de naissance de l'individu
                parentDeathDate.setValue(getBirthDate(RowType.IndiBirthDate).getValue());
                // le pere doit être decede apres 9 mois avant la naissance
                parentDeathDate.setValue(String.format("AFT %d", parentDeathDate.getStart().add(0, -9, 0).getYear()));
                break;
            case WifeFatherDeathDate:
            case WifeMotherDeathDate:
                // j'initialise la date avec la date de naissance de l'epouse
                parentDeathDate.setValue(getBirthDate(RowType.WifeBirthDate).getValue());
                // la mere doit être decede apres la naissance
                parentDeathDate.setValue(String.format("AFT %d", parentDeathDate.getStart().add(0, 0, 0).getYear()));
                break;
            default:
                throw new Exception("RowType not allowed "+requiredDeath );
        }

        return parentDeathDate;
    }

    /**
     * calcule la date de mariage des parents
     * @param record
     * @return
     */
    PropertyDate getParentMariageDate(RowType requiredMarriage) throws Exception {
        PropertyDate parentMariageDate;
        if (record instanceof RecordBirth) {
            PropertyDate birthDate;
            switch ( requiredMarriage) {
                case IndiParentMarriageDate:
                    // j'initialise la date avec la date de naissance de l'individu
                    birthDate = record.getIndiBirthDate().isEmpty() ? record.getEventDateProperty() : record.getIndiBirthDate().getPropertyDate();
                    break;
                case WifeParentMarriageDate:
                    // date inconnue , la naissance de la femme n'est pas indiqué dans le releve de naissance d'un individu.
                    birthDate = record.getWifeBirthDate().isEmpty() ? record.getEventDateProperty() : record.getWifeBirthDate().getPropertyDate();
                    break;
                default:
                    throw new Exception("RowType not allowed"+requiredMarriage );
            }
            // le mariage des parents est avant la naissance de l'enfant arrondie a l'année
            parentMariageDate = getDateBeforeMinusShift(birthDate, 0);

        } else if (record instanceof RecordMarriage) {
            // le mariage des parents est avant la naissance de l'epoux
            // et avant le mariage du marié dminué de minParentYearOld
            // TODO : calculer la date de mariage des parents en fonction de l'age de l'epoux
            PropertyDate beforeChildBirth;
            PropertyDate beforeChildMarriage;
             switch ( requiredMarriage) {
                case IndiParentMarriageDate:
                    // date de naissance de l'epoux
                    beforeChildBirth = getDateBeforeMinusShift(record.getIndiBirthDate().getPropertyDate(), 0);
                    // date de mariage du mariage  -  minMarriageYearOld
                    beforeChildMarriage = getDateBeforeMinusShift(record.getEventDateProperty(), MergeQuery.minMarriageYearOld);
                    break;
                case WifeParentMarriageDate:
                    // date de naissance de l'epouse
                    beforeChildBirth = getDateBeforeMinusShift(record.getWifeBirthDate().getPropertyDate(), 0);
                    // date de mariage du mariage  -  minMarriageYearOld
                    beforeChildMarriage = getDateBeforeMinusShift(record.getEventDateProperty(), MergeQuery.minMarriageYearOld);
                    break;
                default:
                    throw new Exception("RowType not allowed"+requiredMarriage );
            }

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
