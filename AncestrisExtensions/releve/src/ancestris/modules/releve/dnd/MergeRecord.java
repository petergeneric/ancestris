package ancestris.modules.releve.dnd;

import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import genj.gedcom.GedcomException;
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
    private PropertyDate IndiMarriedBirthDate= null;
    private PropertyDate IndiMarriedDeathDate= null;
    private PropertyDate IndiMarriedMarriageDate= null;

    private PropertyDate WifeBirthDate = null;
    private PropertyDate WifeDeathDate = null;
    private PropertyDate WifeFatherBirthDate = null;
    private PropertyDate WifeFatherDeathDate = null;
    private PropertyDate WifeMotherBirthDate = null;
    private PropertyDate WifeMotherDeathDate = null;
    private PropertyDate WifeParentMarriageDate = null;
    private PropertyDate WifeMarriedBirthDate= null;
    private PropertyDate WifeMarriedDeathDate= null;
    private PropertyDate WifeMarriedMarriageDate= null;



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
        //String cityCode = record.getEventPlace().getCityCode();
        //return String.format("%s %s Etat civil", cityCode, cityName);
        return String.format("Etat civil %s", cityName);
    }

    String getEventPage() {
        return record.getFreeComment().toString();
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
            case Death:
                return makeDeathComment();
            default:
                //TODO makeMiscComment();
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
                record.getIndiAge()!=null ? record.getIndiAge().getDelta() : new Delta(0,0,0),
                getIndiMarriedMarriageDate());
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
        if ( record.getIndiBirthPlace() != null && !record.getIndiBirthPlace().isEmpty() ) {
            return record.getIndiBirthPlace().toString();
        } else {
            return record.getEventPlace().toString();
        }
    }

    String getIndiOccupation() {
        return record.getIndiOccupation().toString();
    }

    String getIndiOccupationWithDate() {
        String occupation = record.getIndiOccupation().toString();
        if (!occupation.isEmpty()) {
            occupation += " (" + getEventDate().getDisplayValue()+")";
        }
        return occupation;
    }

    String getIndiResidence() {
        return record.getIndiResidence().toString();
    }

    //  conjoint (ou ancien conjoint) //////////////////////////////////////////

    String getIndiMarriedFirstName() {
        return record.getIndiMarriedFirstName().toString();
    }

    String getIndiMarriedLastName() {
        return record.getIndiMarriedLastName().toString();
    }

    PropertyDate getIndiMarriedBirthDate() throws Exception {
        if (IndiMarriedBirthDate == null) {
            IndiMarriedBirthDate = new PropertyDate();
            if ( record.getIndiMarriedFirstName()!= null 
                 && record.getIndiMarriedLastName()!= null 
                 && (!record.getIndiMarriedFirstName().isEmpty() ||  !record.getIndiMarriedLastName().isEmpty())
                 && getIndiMarriedMarriageDate().isComparable()) {
                // l'ex conjoint existe , la naissance est minMarriageYearOld le mariage avec l'individu
                IndiMarriedBirthDate.setValue(PropertyDate.BEFORE, getYear(getIndiMarriedMarriageDate().getStart(), -MergeQuery.minMarriageYearOld), null, "naissance avant la date du mariage -"+MergeQuery.minMarriageYearOld);
            }
        }
        return IndiMarriedBirthDate;
    }

    PropertyDate getIndiMarriedMarriageDate() throws Exception {
        if (IndiMarriedMarriageDate == null) {
            IndiMarriedMarriageDate = new PropertyDate();
            if ( record.getIndiMarriedFirstName()!= null 
                 && record.getIndiMarriedLastName()!= null 
                 && (!record.getIndiMarriedFirstName().isEmpty() ||  !record.getIndiMarriedLastName().isEmpty())
                 && getEventDate().isComparable()) {
                // l'ex conjoint existe , le mariage avec l'individu est avant la date l'evenement
                IndiMarriedMarriageDate.setValue(PropertyDate.BEFORE, getYear(getEventDate().getStart()), null, "mariage avant la date du relevé");
            }
        }
        return IndiMarriedMarriageDate;
    }

    PropertyDate getIndiMarriedDeathDate() throws Exception {
        if (IndiMarriedDeathDate == null) {
            IndiMarriedDeathDate = new PropertyDate();
            if (record.getIndiMarriedDead().getState()==true) {
                IndiMarriedDeathDate.setValue(PropertyDate.BEFORE, getYear(getEventDate().getStart()), null, "deces avant la date du relevé");
            } else {
                IndiMarriedDeathDate.setValue(PropertyDate.AFTER, getYear(getEventDate().getStart()), null, "deces aprés la date du relevé");
            }
        }
        return IndiMarriedDeathDate;
    }

    String getIndiMarriedOccupation() {
        return record.getIndiMarriedOccupation().toString();
    }

    String getIndiMarriedOccupationWithDate() {
        String occupation = record.getIndiMarriedOccupation().toString();
        if (!occupation.isEmpty()) {
            occupation += " (" + getEventDate().getDisplayValue()+")";
        }
        return occupation;
    }

    String getIndiMarriedResidence() {
        return record.getIndiMarriedResidence().toString();
    }


    //  indi father ////////////////////////////////////////////////////////////

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

    String getIndiFatherResidence() {
        String residence;
        if ( this.type == RecordType.Birth ) {
            // meme residence que le fils si c'est un relevé de naissance
            residence = record.getIndiBirthPlace().toString();
        } else {
            residence = record.getIndiFatherResidence().toString();
        }
        return residence;
    }

    String getIndiFatherOccupationWithDate() {
        String occupation = record.getIndiFatherOccupation().toString();
        if (!occupation.isEmpty()) {
            occupation += " (" + getEventDate().getDisplayValue()+")";
        }
        return occupation;
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

    String getIndiMotherOccupationWithDate() {
        String occupation = record.getIndiMotherOccupation().toString();
        if (!occupation.isEmpty()) {
            occupation += " (" + getEventDate().getDisplayValue()+")";
        }
        return occupation;
    }

    String getIndiMotherResidence() {
         String residence;
        if (this.type == RecordType.Birth) {
            // meme residence que le pere
            residence = record.getIndiBirthPlace().toString();
        } else {
            residence = record.getIndiMotherResidence().toString();
        }
        return residence;
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
                record.getWifeAge()!=null ? record.getWifeAge().getDelta() : new Delta(0,0,0),
                getWifeMarriedMarriageDate() );
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
        return record.getWifeBirthPlace().toString();
    }

    String getWifeOccupation() {
        return record.getWifeOccupation().toString();
    }

    String getWifeOccupationWithDate() {
        String occupation = record.getWifeOccupation().toString();
        if (!occupation.isEmpty()) {
            occupation += " (" + getEventDate().getDisplayValue()+")";
        }
        return occupation;
    }

    String getWifeResidence () {
        return record.getWifeResidence().toString();
    }

    //  conjoint (ou ancien conjoint) //////////////////////////////////////////
    String getWifeMarriedFirstName() {
        return record.getWifeMarriedFirstName().toString();
    }

    String getWifeMarriedLastName() {
        return record.getWifeMarriedLastName().toString();
    }

    PropertyDate getWifeMarriedBirthDate() throws Exception {
        if (WifeMarriedBirthDate == null) {
            WifeMarriedBirthDate = new PropertyDate();
            if ( record.getWifeMarriedFirstName()!= null
                 && record.getWifeMarriedLastName()!= null
                 && (!record.getWifeMarriedFirstName().isEmpty() ||  !record.getWifeMarriedLastName().isEmpty())
                 && getWifeMarriedMarriageDate().isComparable()) {
                // l'ex conjoint existe , la naissance est minMarriageYearOld le mariage avec l'individu
                WifeMarriedBirthDate.setValue(PropertyDate.BEFORE, getYear(getWifeMarriedMarriageDate().getStart(), -MergeQuery.minMarriageYearOld), null, "naissance avant la date du mariage -"+MergeQuery.minMarriageYearOld);
            }
        }
        return WifeMarriedBirthDate;
    }

    PropertyDate getWifeMarriedMarriageDate() throws Exception {
        if (WifeMarriedMarriageDate == null) {
            WifeMarriedMarriageDate = new PropertyDate();
            if ( record.getWifeMarriedFirstName()!= null  && record.getWifeMarriedLastName() != null
                    && (!record.getWifeMarriedFirstName().isEmpty() ||  !record.getWifeMarriedLastName().isEmpty())
                    && getEventDate().isComparable() ) {
                // le mariage avec l'ex conjoit
                //WifeMarriedMarriageDate.setValue(String.format("BEF %d",getEventDate().getStart().getYear()));
                WifeMarriedMarriageDate.setValue(PropertyDate.BEFORE, getYear(getEventDate().getStart()), null, "mariage avant la date du relevé");
            }
        }
        return WifeMarriedMarriageDate;
    }

    PropertyDate getWifeMarriedDeathDate() throws Exception {
        if (WifeMarriedDeathDate == null) {
            WifeMarriedDeathDate = new PropertyDate();
            if ( record.getWifeMarriedDead()!= null && record.getWifeMarriedDead().getState() ) {
                WifeMarriedDeathDate.setValue(PropertyDate.BEFORE, getYear(getEventDate().getStart()), null, "décès avant la date du relevé");
             } else {
                WifeMarriedDeathDate.setValue(PropertyDate.AFTER, getYear(getEventDate().getStart()), null, "deces aprés la date du relevé");
            }
        }
        return WifeMarriedDeathDate;
    }

    String getWifeMarriedOccupation() {
        return record.getWifeFatherOccupation().toString();
    }

    String getWifeMarriedOccupationWithDate() {
        String occupation = record.getWifeMarriedOccupation().toString();
        if (!occupation.isEmpty()) {
            occupation += " (" + getEventDate().getDisplayValue()+")";
        }
        return occupation;
    }



    //  wife father ////////////////////////////////////////////////////////////

    PropertyDate getWifeParentMarriageDate() throws Exception {
        if (WifeParentMarriageDate == null) {
            WifeParentMarriageDate = calculateParentMariageDate(getWifeBirthDate());
        }
        return WifeParentMarriageDate;
    }

    String getWifeFatherFirstName() {
        if ( record.getWifeFatherFirstName() == null) {
            return "";
        } else {
            return record.getWifeFatherFirstName().toString();
        }
    }

    String getWifeFatherLastName() {
        if ( record.getWifeFatherLastName() == null ) {
            return "";
        } else {
            return record.getWifeFatherLastName().toString();
        }
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
        if ( record.getWifeFatherOccupation() == null ) {
            return "";
        } else {
            return record.getWifeFatherOccupation().toString();
        }
    }
    
    String getWifeFatherResidence() {
        return record.getWifeFatherResidence().toString();
    }

    String getWifeFatherOccupationWithDate() {
        String occupation = getWifeFatherOccupation();
        if (!occupation.isEmpty()) {
            occupation += " (" + getEventDate().getDisplayValue()+")";
        }
        return occupation;
    }

    String getWifeMotherFirstName() {
        if( record.getWifeMotherFirstName() == null) {
            return "";
        } else {
            return record.getWifeMotherFirstName().toString();
        }
    }

    String getWifeMotherLastName() {
        if ( record.getWifeMotherLastName() == null) {
            return "";
        } else {
            return record.getWifeMotherLastName().toString();
        }
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
        if ( record.getWifeMotherOccupation() == null ) {
            return "";
        } else {
            return record.getWifeMotherOccupation().toString();
        }
    }

    String getWifeMotherOccupationWithDate() {
        String occupation = getWifeMotherOccupation();
        if (!occupation.isEmpty()) {
            occupation += " (" + getEventDate().getDisplayValue()+")";
        }
        return occupation;
    }

    /**
     *
     * @return
     */
    String getWifeMotherResidence() {
        String residence;
        if (this.type == RecordType.Birth && record.getWifeMotherResidence().isEmpty() && ! record.getWifeFatherResidence().isEmpty()) {
            residence = record.getWifeFatherResidence().toString();
        } else {
            residence = record.getWifeMotherResidence().toString();
        }
        return residence;
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

        String godFather = appendValue(
                record.getWitness1FirstName().toString() + " " + record.getWitness1LastName().toString(),
                record.getWitness1Occupation().toString(),
                record.getWitness1Comment().toString());
        if (!godFather.isEmpty()) {
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n') {
                comment += "\n";
            }
            comment += "Parrain/témoin" + ": " + godFather;
        }
        String godMother = appendValue(
                record.getWitness2FirstName().toString() + " " + record.getWitness2LastName().toString(),
                record.getWitness2Occupation().toString(),
                record.getWitness2Comment().toString());
        if (!godMother.isEmpty()) {
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n') {
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
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n') {
                comment += "\n";
            }
            comment += "Témoin(s)" + ": " + witness;
        }

        String fatherComment = appendValue(
                record.getIndiFatherComment().toString() );
        if (!fatherComment.isEmpty()) {
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n' ) {
                comment += "\n";
            }
            comment += "Commentaire père" + ": " + fatherComment;
        }

        String motherComment = appendValue(
                record.getIndiMotherComment().toString() );
        if (!motherComment.isEmpty()) {
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n') {
                comment += "\n";
            }
            comment += "Commentaire mère" + ": " + motherComment;
        }

        String generalComment = appendValue(
                record.getGeneralComment().toString()
           );
        if (!generalComment.isEmpty()) {
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n') {
                comment += "\n";
            }
            comment += generalComment;
        }

        String freeComment = appendValue(
                record.getFreeComment().toString() );
        if (!freeComment.isEmpty()) {
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n') {
                comment += "\n";
            }
            comment += "Photo"+": "+freeComment;
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

        String comment="";
        String indiComment = appendValue(record.getIndiComment().toString(),
                record.getIndiAge().toString());

       if ( !indiComment.isEmpty() ) {
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n') {
                comment += "\n";
            }
            comment += "Commentaire epoux"+": " +indiComment;
        }

        String indiFatherComment = appendValue(
                record.getIndiFatherComment().toString(),
                record.getIndiFatherAge().toString());
        if (!indiFatherComment.isEmpty()) {
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n' ) {
                comment += "\n";
            }
            comment += "Commentaire père époux" + ": " + indiFatherComment;
        }

        String indiMotherComment = appendValue(
                record.getIndiMotherComment().toString(),
                record.getIndiMotherAge().toString());
        if (!indiMotherComment.isEmpty()) {
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n') {
                comment += "\n";
            }
            comment += "Commentaire mère époux" + ": " + indiMotherComment;
        }

        String wifeComment = appendValue(record.getWifeComment().toString(),
                record.getWifeAge().toString());

        if ( !wifeComment.isEmpty() ) {
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n') {
                comment += "\n";
            }
            comment += "Commentaire épouse"+": " +wifeComment;
        }

        String wifeFatherComment = appendValue(
                record.getWifeFatherComment().toString(),
                record.getWifeFatherAge().toString());
        if (!wifeFatherComment.isEmpty()) {
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n' ) {
                comment += "\n";
            }
            comment += "Commentaire père épouse" + ": " + wifeFatherComment;
        }

        String wifeMotherComment = appendValue(
                record.getWifeMotherComment().toString(),
                record.getWifeMotherAge().toString());
        if (!wifeMotherComment.isEmpty()) {
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n') {
                comment += "\n";
            }
            comment += "Commentaire mère épouse" + ": " + wifeMotherComment;
        }


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
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n') {
                comment += "\n";
            }
            comment += "Témoin(s)" + ": " + witness;
        }

        String generalComment = appendValue(
                record.getGeneralComment().toString()
                );

        if (!generalComment.isEmpty()) {
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n') {
                comment += "\n";
            }
            comment += generalComment;
        }

       String freeComment = appendValue(
                record.getFreeComment().toString() );
        if (!freeComment.isEmpty()) {
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n') {
                comment += "\n";
            }
            comment += "Photo"+": "+freeComment;
        }

        return comment;
    }

    /**
     * genere le commentaire de la deces
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
    private String makeDeathComment() {
        String comment = appendValue(record.getIndiComment().toString());

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
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n') {
                comment += "\n";
            }
            comment += "Témoin(s)" + ": " + witness;
        }

        String fatherComment = appendValue(
                record.getIndiFatherComment().toString() );
        if (!fatherComment.isEmpty()) {
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n' ) {
                comment += "\n";
            }
            comment += "Commentaire père" + ": " + fatherComment;
        }

        String motherComment = appendValue(
                record.getIndiMotherComment().toString() );

        if (!motherComment.isEmpty()) {
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n') {
                comment += "\n";
            }
            comment += "Commentaire mère" + ": " + motherComment;
        }

        String generalComment = appendValue(record.getGeneralComment().toString());
        if (!generalComment.isEmpty()) {
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n') {
                comment += "\n";
            }
            comment += generalComment;
        }

        if ( record.getIndiAge()!= null && !record.getIndiAge().isEmpty() ) {
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n') {
                comment += "\n";
            }
            comment += "Age"+": " +record.getIndiAge().toString();
        }

        String freeComment = appendValue(
                record.getFreeComment().toString() );
        if (!freeComment.isEmpty()) {
            if (!comment.isEmpty() && comment.charAt(comment.length()-1)!= '\n') {
                comment += "\n";
            }
            comment += "Photo"+": "+freeComment;
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

    
    static private PointInTime getYear(PointInTime pit) throws GedcomException {
        PointInTime gregorianPit = pit.getPointInTime(PointInTime.GREGORIAN);
        return new PointInTime(PointInTime.UNKNOWN,PointInTime.UNKNOWN,gregorianPit.getYear());
    }
    static private PointInTime getYear(PointInTime pit, int shiftYear) throws GedcomException {
        PointInTime gregorianPit = pit.getPointInTime(PointInTime.GREGORIAN);
        return new PointInTime(PointInTime.UNKNOWN,PointInTime.UNKNOWN,gregorianPit.getYear()+shiftYear);
    }

    static protected PointInTime getYear(PointInTime refPit, Delta age) throws GedcomException {
        PointInTime gregorianPit = refPit.getPointInTime(PointInTime.GREGORIAN);
        PointInTime pit = new PointInTime(gregorianPit.getDay(), gregorianPit.getMonth(),gregorianPit.getYear());

        pit.add( -age.getDays(), -age.getMonths(), -age.getYears() );

        if ( age.getMonths() ==0 && age.getDays() ==0 ) {
            // si le nombre de mois n'est pas précisé, j'arrondis à l'année
            pit.set(PointInTime.UNKNOWN, PointInTime.UNKNOWN, pit.getYear());
        } else if ( age.getDays() ==0 ) {
            // si le nombre de jours n'est pas précisé, j'arrondis au mois
            pit.set(PointInTime.UNKNOWN, pit.getMonth(), pit.getYear());
        }
        
        return pit;
    }

    /**
     * calcule la date de naissance de l'individu ou de l'epouse
     *  si record=RecordBirth :
     *    retourne la date de naissance de l'individu ou a defaut date de l'evenement (c'est souvent la meme sauf si l'evenement est un bapteme qui a lieu apres la naissance)
     *  si record=RecordMarriage :
     *    retourne la date de naissance de l'individu ou a defaut dateBirth = date de son mariage -  minMarriageYearOld
     *  si record=RecordDeath ou RecordMisc
     *    retourne la date "avant" de la levenement arrondi a l'année. 
     * 
     * @param birthDate date de naissance de l'individu ou de l'epouse
     * @param age       age de l'individu ou de l'epouse
     * @param marriedMarriageDate     date de mariage avec l'ex conjoint (null si pas d'ex conjoint
     * @return
     */
    private PropertyDate calculateBirthDate(PropertyDate birthDate, Delta age, PropertyDate marriedMarriageDate) throws Exception {
        if (type == RecordType.Birth) {
            if ( !birthDate.isComparable() ) {
                birthDate.setValue(getEventDate().getValue());
            }
        } else if (type == RecordType.Marriage) {

            if ( ! birthDate.isComparable() &&  record.getEventDateProperty().isComparable()) {
                // la date de naissance n'est pas valide, 
                // je calcule la naissance a partir de l'age ou de la date de mariage
                if (age.getYears() != 0 || age.getMonths() != 0 || age.getDays() != 0 ) {
                    // l'age est valide,
                    if (age.getYears() != 0 && age.getMonths() != 0 && age.getDays() != 0) {
                        // l'age est precis au jour près
                        // date de naissance = date de mariage - age
                        //birthDate.setValue(record.getEventDateProperty().getValue());
                        //birthDate.getStart().add(-age.getDays(), -age.getMonths(), -age.getYears());
                        PointInTime start = null;
                        if ( record.getEventDateProperty().getStart()!= null) {
                            start = new PointInTime();
                            start.set(record.getEventDateProperty().getStart());
                            start.add(-age.getDays(), -age.getMonths(), -age.getYears());
                        }
                        PointInTime end = null;
                        if ( record.getEventDateProperty().getEnd()!= null) {
                            end = new PointInTime();
                            end.set(record.getEventDateProperty().getEnd());
                            end.add(-age.getDays(), -age.getMonths(), -age.getYears());
                        }
                        birthDate.setValue(record.getEventDateProperty().getFormat(),
                                start, end , "Date naissance = date du releve - age");

                    } else {
                        // l'age n'est pas précis au jour près.
                        // date de naissance = date de mariage (arrondi a l'année) - age (arrondi à l'année)
                        ///birthDate.setValue(String.format("CAL %d", record.getEventDateProperty().getStart().getYear() -age.getYears()));
                        birthDate.setValue(PropertyDate.CALCULATED, getYear(getEventDate().getStart(),age), null, "date naissance=date du releve - age");
                    }
                } else {
                    // l'age n'est pas valide

                    // date de naissance maximale BEF = date de mariage -  minMarriageYearOld
                    //birthDate= calulateDateBeforeMinusShift(record.getEventDateProperty(), MergeQuery.minMarriageYearOld, "date 15 ans avant le mariage");
                    birthDate= calulateDateBeforeMinusShift(getEventDate(), MergeQuery.minMarriageYearOld, "=date mariage -"+ MergeQuery.minMarriageYearOld );
                }
            }
        } else if (type == RecordType.Death) {
            if ( ! birthDate.isComparable() &&  record.getEventDateProperty().isComparable()) {
                // la date de naissance n'est pas valide,
                // je calcule la naissance a partir de l'age
                if (age.getYears() != 0 || age.getMonths() != 0 || age.getDays() != 0 ) {
                    // l'age est valide,
                    if (age.getYears() != 0 && age.getMonths() != 0 && age.getDays() != 0) {
                        // l'age est precis au jour près
                        // date de naissance = date de mariage - age
                        ///birthDate.setValue(record.getEventDateProperty().getValue());
                        ///birthDate.getStart().add(-age.getDays(), -age.getMonths(), -age.getYears());
                        PointInTime start = null;
                        if ( record.getEventDateProperty().getStart()!= null) {
                            start = new PointInTime();
                            start.set(record.getEventDateProperty().getStart());
                            start.add(-age.getDays(), -age.getMonths(), -age.getYears());
                        }
                        PointInTime end = null;
                        if ( record.getEventDateProperty().getEnd()!= null) {
                            end = new PointInTime();
                            end.set(record.getEventDateProperty().getEnd());
                            end.add(-age.getDays(), -age.getMonths(), -age.getYears());
                        }
                        birthDate.setValue(record.getEventDateProperty().getFormat(),
                                start, end , "Date naissance = date du releve - age");
                    } else {
                        // l'age n'est pas précis au jour près.
                        // date de naissance = date de mariage (arrondi a l'année) - age (arrondi à l'année)
                        ///birthDate.setValue(String.format("CAL %d", record.getEventDateProperty().getStart().getYear() - age.getYears()));
                        birthDate.setValue(PropertyDate.CALCULATED, getYear(getEventDate().getStart(),age), null, "date naissance=date du releve - age");
                        
                    }
                } else {
                    // l'age n'est pas valide
                    if (marriedMarriageDate != null && marriedMarriageDate.isComparable()) {
                        // la naissance est minMarriageYearOld avant le mariage avec l'ex conjoint
                        ///birthDate.setValue(String.format("BEF %d", marriedMarriageDate.getStart().getYear()-MergeQuery.minMarriageYearOld));
                        birthDate.setValue(PropertyDate.BEFORE, getYear(marriedMarriageDate.getStart(),-MergeQuery.minMarriageYearOld), null, "date naissance= avant date de mariage avec ex conjoint -"+ MergeQuery.minMarriageYearOld);
                    } else {
                        // la naissance est avant le deces
                        ///birthDate.setValue(String.format("BEF %d", getEventDate().getStart().getYear()));
                        birthDate.setValue(PropertyDate.BEFORE, getYear(getEventDate().getStart()), null, "date naissance= avant date du releve");
                    }
                }
            }

        } else {
            // releve divers
            if ( ! birthDate.isComparable() &&  record.getEventDateProperty().isComparable()) {
                // la date de naissance n'est pas valide,
                // je calcule la naissance a partir de l'age
                if (age.getYears() != 0 ) {
                    // l'age est valide,
                    if (age.getYears() != 0 && age.getMonths() != 0 && age.getDays() != 0) {
                        // l'age est precis au jour près
                        // date de naissance = date de mariage - age
                        ///birthDate.setValue(record.getEventDateProperty().getValue());
                        ///birthDate.getStart().add(-age.getDays(), -age.getMonths(), -age.getYears());
                        PointInTime start = null;
                        if ( record.getEventDateProperty().getStart()!= null) {
                            start = new PointInTime();
                            start.set(record.getEventDateProperty().getStart());
                            start.add(-age.getDays(), -age.getMonths(), -age.getYears());
                        }
                        PointInTime end = null;
                        if ( record.getEventDateProperty().getEnd()!= null) {
                            end = new PointInTime();
                            end.set(record.getEventDateProperty().getEnd());
                            end.add(-age.getDays(), -age.getMonths(), -age.getYears());
                        }
                        birthDate.setValue(record.getEventDateProperty().getFormat(),
                                start, end , "Date naissance = date du releve - age");
                    } else {
                        // l'age n'est pas précis au jour près.
                        // date de naissance = date de mariage (arrondi a l'année) - age (arrondi à l'année)
                        ///birthDate.setValue(String.format("CAL %d", record.getEventDateProperty().getStart().getYear() - age.getYears()));
                        birthDate.setValue(PropertyDate.CALCULATED, getYear(record.getEventDateProperty().getStart(),age), null, "date naissance=date du releve - age");
                    }
                } else {
                    // l'age n'est pas valide
                    if (marriedMarriageDate != null && marriedMarriageDate.isComparable()) {
                        // la naissance est minMarriageYearOld avant le mariage avec l'ex conjoint
                        ///birthDate.setValue(String.format("BEF %d", marriedMarriageDate.getStart().getYear()-MergeQuery.minMarriageYearOld));
                        birthDate.setValue(PropertyDate.BEFORE, getYear(marriedMarriageDate.getStart(),-MergeQuery.minMarriageYearOld), null, "date naissance= avant date de mariage avec ex conjoint -"+ MergeQuery.minMarriageYearOld);
                    } else {
                        // il n'y a pas d'ex conjoint
                        // la naissance est avant l'evenement qui concerne l'individu
                        ///birthDate.setValue(String.format("BEF %d", record.getEventDateProperty().getStart().getYear()));
                        birthDate.setValue(PropertyDate.BEFORE, getYear(getEventDate().getStart()), null, "date naissance= avant date du releve");
                    }
                }
            }
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
        // j'initialise la date de naissance du parent avec la date de naissance de l'individu
        // puis je la decrementerai en fonction des autres donnees du releve.
        ///parentBirthDate.setValue(childBirthDate.getValue());

        if ( parentAge.getYears()!=0 && record.getEventDateProperty().isComparable() ) {
            // l'age du parent est valide
            // parentBirthDate = eventDate - parentAge
            if( parentAge.getYears()!=0 && parentAge.getMonths()!=0 &&  parentAge.getDays()!=0) {
                //l'age est precis au jour pres.
                ///parentBirthDate.getStart().add(-parentAge.getDays(), -parentAge.getMonths(), -parentAge.getYears());
                PointInTime start = null;
                if ( getEventDate().getStart()!= null) {
                    start = new PointInTime();
                    start.set(getEventDate().getStart());
                    start.add(-parentAge.getDays(), -parentAge.getMonths(), -parentAge.getYears());
                }
                PointInTime end = null;
                if ( getEventDate().getEnd()!= null) {
                    end = new PointInTime();
                    end.set(getEventDate().getEnd());
                    end.add(-parentAge.getDays(), -parentAge.getMonths(), -parentAge.getYears());
                }
                parentBirthDate.setValue(getEventDate().getFormat(),
                        start, end , "Date naissance = date du releve - age");
            } else {
                // date arrondie à l'année car l'age n'est pas précis au jour près.
                ///parentBirthDate.setValue(String.format("CAL %d", parentBirthDate.getStart().add(0, 0, -parentAge.getYears()).getYear() ));
                parentBirthDate.setValue(PropertyDate.CALCULATED, getYear(getEventDate().getStart(),parentAge), null, "date naissance=date du releve - age");
            }
        } else {
            // l'age du parent n'est pas valide
            // je calcule la date maximale à partir de la date de mariage ou la date de naissance de l'enfant
            if ( childBirthDate.isComparable()) {
                // le parent doit être né  "minParentYearOld" années avant la naissance de l'individu
                // parentBirthDate = eventDat - minParentYearOld
                ///parentBirthDate.setValue(String.format("BEF %d", parentBirthDate.getStart().add(0, 0, -MergeQuery.minParentYearOld).getYear()));
                parentBirthDate.setValue(PropertyDate.BEFORE, getYear(childBirthDate.getStart(),-MergeQuery.minParentYearOld), null, "date naissance= avant naissance de l'enfant -"+MergeQuery.minParentYearOld);
            } else {
                // je ne sais pas calculer la date de naissance
                parentBirthDate = new PropertyDate();
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
                    ///parentDeathDate.setValue(String.format("BET %d AND %d", birthPit.getYear(), getEventDate().getStart().getYear()));
                    parentDeathDate.setValue(PropertyDate.BETWEEN_AND, getYear(birthPit), getYear(getEventDate().getStart()), "date deces= entre la naissance et la date du releve");
                } else {
                    // le parent est decede avant la date du releve
                    ///parentDeathDate.setValue(String.format("BEF %d", getEventDate().getStart().getYear()));
                    parentDeathDate.setValue(PropertyDate.BEFORE, getYear(getEventDate().getStart()), null, "date deces= avant la date du releve");
                }
            } else {
                if (birthDateUsefull ) {
                    // le parent est decede apres la naissance 
                    parentDeathDate.setValue(String.format("AFT %d", birthPit.getYear()));
                    parentDeathDate.setValue(PropertyDate.AFTER, getYear(birthPit), null, "date deces= apres la naissance");
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
        if (record instanceof RecordMarriage) {
            // le mariage des parents est
            //   - avant la naissance de l'epoux
            //   - avant le mariage du marié dminué de minParentYearOld
            // date de mariage = BEF date de naissance de l'epoux
            PropertyDate beforeChildBirth = calulateDateBeforeMinusShift(childBirthDate, 0, "Date mariage= avant naissance époux(se)");
            // date de mariage = EventDate - minMarriageYearOld
            PropertyDate beforeChildMarriage = calulateDateBeforeMinusShift(getEventDate(), MergeQuery.minMarriageYearOld, "Date mariage = dete du releve -"+MergeQuery.minMarriageYearOld);
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
            // le mariage des parents est avant la naissance de l'enfant arrondie a l'année
            parentMariageDate = calulateDateBeforeMinusShift(childBirthDate, 0, "Date mariage = avant la naissance de l'enfant");
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

}
