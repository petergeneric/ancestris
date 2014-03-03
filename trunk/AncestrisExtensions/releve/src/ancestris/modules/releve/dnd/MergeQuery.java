package ancestris.modules.releve.dnd;

import ancestris.modules.releve.dnd.MergeRecord.MergeParticipant;
import ancestris.modules.releve.dnd.MergeRecord.MergeParticipantType;
import ancestris.modules.releve.model.FieldSex;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertySex;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Cette classe contient les requetes pour chercher des entités dans un gedcom
 * compatibles avec les informations d'un relevé.
 * @author Michel
 */
public class MergeQuery {

    protected static int minMarriageYearOld = 18; // age minimum pour etre marié
    protected static int minParentYearOld = 18;   // age minimum pour etre parent
    protected static int indiMaxYearOld = 100;    // age maximum d'un individu
    protected static int aboutYear = 5;           // marge d'incertitude ( date ABOUT ou ESTMATED ou CALCULATED)
    protected static int maxParentYearOld = 60;    // age maximum d'un parent

    private static final DoubleMetaphone dm = new DoubleMetaphone();
    static {
        dm.setMaxCodeLen(5);

    }

    /**
     * Recherche les familles de parents compatibles avec les parents de l'individu 1 du releve
     * 
     * @param RecordBirth  relevé de naissance
     * @param gedcom
     * @param selectedIndi individu selectionné
     * @return Liste des fmailles
     */
    static protected List<Fam> findFamilyCompatibleWithParticipantParents(MergeRecord record, MergeParticipantType participantType, Gedcom gedcom) throws Exception {
        List<Fam> parentFamilies = new ArrayList<Fam>();

        MergeParticipant participant = record.getParticipant(participantType);
        // j'arrete la recherche si aucun nom et prenom des parents de l'individu n'est renseigné
        if ( participant.getFatherLastName().isEmpty() && participant.getFatherFirstName().isEmpty()
                && participant.getMotherLastName().isEmpty() && participant.getMotherFirstName().isEmpty() ) {
            return parentFamilies;
        }

        // je recupere la date de naissance du releve
        PropertyDate recordBirthDate = participant.getBirthDate();
        
        // Je recherche une famille avec un pere et une mere qui portent le même nom
        // et dont les dates de naissance, de deces et de mariage sont compatibles
        for (Fam fam : gedcom.getFamilies()) {
            Indi father = fam.getHusband();
            Indi mother = fam.getWife();

            if (father == null && mother == null) {
                continue;
            }

            // La naissance et le deces doivent être après la date mariage des parents
            PropertyDate marriageDate = fam.getMarriageDate();
            if ( marriageDate != null && marriageDate.isComparable()) {
                // la naissance doit être après la date mariage des parents
                if (!isRecordAfterThanDate(recordBirthDate, marriageDate, 0, 0)) {
                    continue;
                }

                // le deces doit être après la date mariage des parents
                if (!isRecordAfterThanDate(participant.getDeathDate(), marriageDate, 0, 0)) {
                    continue;
                }
            } 

            if (father != null) {

                // meme nom du pere
                if (!participant.getFatherLastName().isEmpty() ) {
                    if ( !isSameLastName(participant.getFatherLastName(), father.getLastName())) {
                        continue;
                    }
                } else {
                    // si le nom du pere est vide dans le releve, je verifie que le nom de l'individu est identique
                    // au nom du pere de la famille (pour eviter de trop nombreuses réponses sans rapport)
                    if ( !isSameLastName(participant.getLastName(), father.getLastName())) {
                        continue;
                    }
                }
                //meme prénom du pere
                if (!participant.getFatherFirstName().isEmpty()
                        && !father.getFirstName().isEmpty()
                        && !isSameFirstName(participant.getFatherFirstName(), father.getFirstName())) {
                    continue;
                }

                // naissance du pere
                if (record.getType() == MergeRecord.RecordType.Birth) {
                    // la naissance doit etre au moins minParentYearOld apres la naissance du pere
                    if (!isRecordAfterThanDate(recordBirthDate, father.getBirthDate(), 0, minParentYearOld)) {
                        continue;
                    }
                } else if (record.getType() == MergeRecord.RecordType.Marriage) {
                    // le mariage doit être au moins  minParentYearOld+minMarriageYearOld la naissance du pere
                    if (!isRecordAfterThanDate(record.getEventDate(), father.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }
                }

                // le pere ne doit pas etre decede 9 mois avant la date de naissance
                if (!isRecordBeforeThanDate(recordBirthDate, father.getDeathDate(), 9, 0)) {
                    continue;
                }
            }

            if (mother != null) {
                // meme nom de la mere
                if (!participant.getMotherLastName().isEmpty()
                        && !isSameLastName(participant.getMotherLastName(), mother.getLastName())) {
                    continue;
                }
                //meme prénom de la mere
                if (!participant.getMotherFirstName().isEmpty()
                        && !mother.getFirstName().isEmpty()
                        && !isSameFirstName(participant.getMotherFirstName(), mother.getFirstName())) {
                    continue;
                }
                
                // naissance de la mere
                if (record.getType() == MergeRecord.RecordType.Birth) {
                    // la naissance doit etre au moins minParentYearOld apres la naissance de la mere
                    if (!isRecordAfterThanDate(recordBirthDate, mother.getBirthDate(), 0, minParentYearOld)) {
                        continue;
                    }
                } else if (record.getType() == MergeRecord.RecordType.Marriage) {
                    // le mariage doit être au moins  minParentYearOld+minMarriageYearOld la naissance du pere
                    if (!isRecordAfterThanDate(record.getEventDate(), mother.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }
                }

                // la mere ne doit pas etre decedee avant la date de naissance
                if (!isRecordBeforeThanDate(recordBirthDate, mother.getDeathDate(), 0, 0)) {
                    continue;
                }
            }

            //il doit y avoir moins de maxParentYearOld - minMarriageYearOld  entre la naissance des enfants
            //  cela permet d'avoir une borne inférieure pour la date de naissance.
            Indi[] children = fam.getChildren();
            boolean foundChild = false;
            for( int i = 0 ; i< children.length && foundChild == false ; i++ ) {
                PropertyDate childBirth = children[i].getBirthDate();
                PointInTime maxChildPith = new PointInTime();
                PointInTime minChildPith = new PointInTime();
                if( childBirth != null && childBirth.isComparable()) {
                    if( childBirth.getFormat() == PropertyDate.DATE) {
                        minChildPith.set(childBirth.getStart());
                        maxChildPith.set(childBirth.getStart());
                    } else if( childBirth.getFormat() == PropertyDate.BETWEEN_AND) {
                        minChildPith.set(childBirth.getStart());
                        maxChildPith.set(childBirth.getEnd());
                    } else if( childBirth.getFormat() == PropertyDate.AFTER || childBirth.getFormat() == PropertyDate.FROM) {
                        minChildPith.set(recordBirthDate.getStart());
                        maxChildPith = childBirth.getStart();
                    } else if( childBirth.getFormat() == PropertyDate.BEFORE || childBirth.getFormat() == PropertyDate.TO) {
                        minChildPith.set(childBirth.getStart());
                        maxChildPith.set(recordBirthDate.getStart());
                    } else {
                        minChildPith.set(childBirth.getStart());
                        minChildPith.add(0,0,-aboutYear);
                        maxChildPith.set(childBirth.getStart());
                        maxChildPith.add(0,0,+aboutYear);
                    }

                    Delta delta = Delta.get(maxChildPith, recordBirthDate.getStart(), PointInTime.GREGORIAN);
                    if ( delta.getYears()> maxParentYearOld - minMarriageYearOld) {
                        foundChild = true;
                    }

                    delta = Delta.get(recordBirthDate.getStart(), maxChildPith, PointInTime.GREGORIAN);
                    if ( delta.getYears()> maxParentYearOld - minMarriageYearOld) {
                        foundChild = true;
                    }
                }
            }
            if (foundChild) {
                continue;
            }


            // j'ajoute la famille dans la liste résultat si elle n'y est pas déjà
            if (!parentFamilies.contains(fam)) {
                parentFamilies.add(fam);
            }
        }

        return parentFamilies;
    }

    /**
     * Recherche les familles de l'individu avec l'ex conjoint
     *
     * @param RecordBirth  relevé de naissance
     * @param gedcom
     * @param selectedIndi individu selectionné
     * @return Liste des fmailles
     */
    static protected List<Fam> findFamilyCompatibleWithParticipantMarried(MergeRecord record, MergeParticipantType participantType, Gedcom gedcom) throws Exception {
        List<Fam> parentFamilies = new ArrayList<Fam>();

        MergeParticipant participant = record.getParticipant(participantType);
        
        // j'arrete la recherche si le nom de l'ex conjoint n'est pas renseigné.
        if ( participant.getMarriedLastName().isEmpty() ) {
            return parentFamilies;
        }

        // Je recherche une famille avec un pere et une mere qui portent le même nom
        // et dont les dates de naissance, de deces et de mariage sont compatibles
        for (Fam fam : gedcom.getFamilies()) {
            Indi husband = fam.getHusband();
            Indi wife = fam.getWife();

            if (husband == null || wife == null) {
                continue;
            }

            if ( participant.getSex() == PropertySex.MALE) {
                // je verifie si l'epoux est compatible avec currentIndi

                // meme nom de l'epoux
                if (!participant.getLastName().isEmpty() ) {
                    if ( !isSameLastName(participant.getLastName(), husband.getLastName())) {
                        continue;
                    }
                }

                //meme prénom de l'epoux
                if (!participant.getFirstName().isEmpty()
                        && !isSameFirstName(participant.getFirstName(), husband.getFirstName())) {
                    continue;
                }

                // le releve doit etre minMarriageYearOld années apres la naissance de l'epoux
                if (!isRecordAfterThanDate(record.getEventDate(), husband.getBirthDate(), 0, minMarriageYearOld)) {
                    continue;
                }

                // naissance de l'epoux
                if (!isCompatible(participant.getBirthDate(), husband.getBirthDate() )) {
                    continue;
                }

                // le releve doit etre avant le deces de l'epoux
                if (!isRecordBeforeThanDate(record.getEventDate(), husband.getDeathDate(), 0, 0)) {
                    continue;
                }

                // meme nom de l'ex epouse
                if (!participant.getMarriedLastName().isEmpty()
                        && !isSameLastName(participant.getMarriedLastName(), wife.getLastName())) {
                    continue;
                }
                
                // meme prénom de l'ex epouse
                if (!participant.getMarriedFirstName().isEmpty()
                        && !isSameFirstName(participant.getMarriedFirstName(), wife.getFirstName())) {
                    continue;
                }

                // le releve doit etre apres le mariage (=naissance + minMarriageYearOld)
                if (!isRecordAfterThanDate(record.getEventDate(), wife.getBirthDate(), 0, minMarriageYearOld)) {
                    continue;
                }

                // le releve doit etre apres la date de mariage
                if (!isRecordAfterThanDate(record.getEventDate(), fam.getMarriageDate(), 0, 0)) {
                    continue;
                }

                // l'ex epouse doit avoir au moins minMarriageYearOld
                if (!isRecordAfterThanDate(record.getEventDate(), wife.getBirthDate(), 0, minMarriageYearOld)) {
                    continue;
                }

                // la date de deces de l'ex conjoint doit etre compatible avec le deces de l'epoux
                if (!isCompatible(participant.getMarriedDeathDate(), wife.getDeathDate() )) {
                    continue;
                }

                // si l'epouse est decedee dans le gedcom avant la date du relevé,
                // alors la date deces du releve doit être aussi avant la date du releve
                if( isRecordAfterThanDate(record.getEventDate(), wife.getDeathDate(), 0, 0)) {
                    if (!isRecordAfterThanDate(record.getEventDate(), participant.getMarriedDeathDate(), 0, 0)) {
                        continue;
                    }
                } else {
                    // si l'epouse n'est pas decedee dans le gedcom, le releve doit etre avant son deces
                    if (!isRecordBeforeThanDate(record.getEventDate(), participant.getMarriedDeathDate(), 0, 0)) {
                        continue;
                    }
                }

            } else {
                // je verifie si l'individu est compatible avec l'epouse

                // meme nom de l'epoux
                if (!participant.getLastName().isEmpty() ) {
                    if ( !isSameLastName(participant.getLastName(), wife.getLastName())) {
                        continue;
                    }
                }

                //meme prénom de l'epoux
                if (!participant.getFirstName().isEmpty()
                        && !isSameFirstName(participant.getFirstName(), wife.getFirstName())) {
                    continue;
                }

                // le releve doit etre minMarriageYearOld années apres la naissance de l'epoux
                if (!isRecordAfterThanDate(record.getEventDate(), wife.getBirthDate(), 0, minMarriageYearOld)) {
                    continue;
                }

                // naissance de l'epoux
                if (!isCompatible(participant.getBirthDate(), wife.getBirthDate() )) {
                    continue;
                }

                // le releve doit etre avant le deces de l'epoux
                if (!isRecordBeforeThanDate(record.getEventDate(), wife.getDeathDate(), 0, 0)) {
                    continue;
                }


                // meme nom de l'ex epoux
                if (!participant.getMarriedLastName().isEmpty()
                        && !isSameLastName(participant.getMarriedLastName(), husband.getLastName())) {
                    continue;
                }

                //meme prénom de l'ex epoux
                if (!participant.getMarriedFirstName().isEmpty()
                        && !isSameFirstName(participant.getMarriedFirstName(), husband.getFirstName())) {
                    continue;
                }

                // le releve doit etre apres le mariage (=naissance + minMarriageYearOld)
                if (!isRecordAfterThanDate(record.getEventDate(), husband.getBirthDate(), 0, minMarriageYearOld)) {
                    continue;
                }
                
                // le releve doit etre apres la date de mariage
                if (!isRecordAfterThanDate(record.getEventDate(), fam.getMarriageDate(), 0, 0)) {
                    continue;
                }

                // la date de deces de l'ex conjoint doit etre compatible avec le deces de l'epoux
                if (!isCompatible(participant.getMarriedDeathDate(), husband.getDeathDate() )) {
                    continue;
                }

                // si l'epoux est decede dans le gedcom avant le releve,
                // alors la date deces du releve doit être aussi avant la date du releve
                if( isRecordAfterThanDate(record.getEventDate(), husband.getDeathDate(), 0, 0)) {
                    if (!isRecordAfterThanDate(record.getEventDate(), participant.getMarriedDeathDate(), 0, 0)) {
                        continue;
                    }
                } else {
                    // si l'epoux n'est pas decede dans le gedcom, le releve doit etre avant son deces
                    if (!isRecordBeforeThanDate(record.getEventDate(), participant.getMarriedDeathDate(), 0, 0)) {
                        continue;
                    }
                }
            }

            // j'ajoute la famille dans la liste résultat si elle n'y est pas déjà
            if (!parentFamilies.contains(fam)) {
                parentFamilies.add(fam);
            }
        }

        return parentFamilies;
    }
    
    /**
     * Recherche les familles de parents compatibles avec les parents de l'individu 1 du releve
     *
     * @param RecordBirth  relevé de naissance
     * @param gedcom
     * @param selectedIndi individu selectionné
     * @return Liste des fmailles
     */
    static protected List<Fam> findFamilyCompatibleWithWifeParents(MergeRecord record, Gedcom gedcom) throws Exception {
        List<Fam> parentFamilies = new ArrayList<Fam>();

        // j'arrete la recherche si aucun nom et prenom des parents de la femme n'est renseigné
        if ( record.getWife().getFatherLastName().isEmpty() && record.getWife().getFatherFirstName().isEmpty()
                && record.getWife().getMotherLastName().isEmpty() && record.getWife().getMotherFirstName().isEmpty() ) {
            return parentFamilies;
        }
        // je recupere la date de naissance du releve
        PropertyDate recordBirthDate = record.getWife().getBirthDate();

        // Je recherche une famille avec un pere et une mere qui portent le même nom
        // et dont les dates de naissance, de deces et de mariage sont compatibles
        for (Fam fam : gedcom.getFamilies()) {
            Indi father = fam.getHusband();
            Indi mother = fam.getWife();

            if (father == null && mother == null) {
                continue;
            }

            // La naissance doit être après la date mariage.
            PropertyDate marriageDate = fam.getMarriageDate();
            if ( marriageDate != null && marriageDate.isComparable()) {
                // la naissance doit être après la date mariage.
                if (!isRecordAfterThanDate(recordBirthDate, marriageDate, 0, 0)) {
                    continue;
                }
            }

            if (father != null) {

                // meme nom du pere
                if (!record.getWife().getFatherLastName().isEmpty() ) {
                    if ( !isSameLastName(record.getWife().getFatherLastName(), father.getLastName())) {
                        continue;
                    }
                } else {
                    // si le nom du pere est vide dans le releve, je verifie que le nom de l'individu est identique
                    // au nom du pere de la famille (pour eviter de trop nombreuses réponses sans rapport)
                    if ( record.getWife().getLastName().isEmpty() || !isSameLastName(record.getWife().getLastName(), father.getLastName())) {
                        continue;
                    }
                }
                //meme prénom du pere
                if (!record.getWife().getFatherFirstName().isEmpty()
                        && !isSameFirstName(record.getWife().getFatherFirstName(), father.getFirstName())) {
                    continue;
                }

                // naissance du pere
                if (record.getType() == MergeRecord.RecordType.Birth) {
                    // la naissance doit etre au moins minParentYearOld apres la naissance du pere
                    if (!isRecordAfterThanDate(recordBirthDate, father.getBirthDate(), 0, minParentYearOld)) {
                        continue;
                    }
                } else if (record.getType() == MergeRecord.RecordType.Marriage) {
                    // le mariage doit être au moins  minParentYearOld+minMarriageYearOld la naissance du pere
                    if (!isRecordAfterThanDate(record.getEventDate(), father.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }
                }

                // le pere ne doit pas etre decede 9 mois avant la date de naissance
                if (!isRecordBeforeThanDate(recordBirthDate, father.getDeathDate(), 9, 0)) {
                    continue;
                }
            }

            if (mother != null) {
                // meme nom de la mere
                if (!record.getWife().getMotherLastName().isEmpty()
                        && !isSameLastName(record.getWife().getMotherLastName(), mother.getLastName())) {
                    continue;
                }
                //meme prénom de la mere
                if (!record.getWife().getMotherFirstName().isEmpty()
                        && !isSameFirstName(record.getWife().getMotherFirstName(), mother.getFirstName())) {
                    continue;
                }

                // naissance de la mere
                if (record.getType() == MergeRecord.RecordType.Birth) {
                    // la naissance doit etre au moins minParentYearOld apres la naissance de la mere
                    if (!isRecordAfterThanDate(recordBirthDate, mother.getBirthDate(), 0, minParentYearOld)) {
                        continue;
                    }
                } else if (record.getType() == MergeRecord.RecordType.Marriage) {
                    // le mariage doit être au moins  minParentYearOld+minMarriageYearOld la naissance du pere
                    if (!isRecordAfterThanDate(record.getEventDate(), mother.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }
                }

                // la mere ne doit pas etre decedee avant la date de naissance
                if (!isRecordBeforeThanDate(recordBirthDate, mother.getDeathDate(), 0, 0)) {
                    continue;
                }

            }

            // j'ajoute la famille dans la liste résultat si elle n'y est pas déjà
            if (!parentFamilies.contains(fam)) {
                parentFamilies.add(fam);
            }
        }

        return parentFamilies;
    }

    /**
     * Recherche les familles compatibles avec le releve de mariage
     * Si selectedFamily n'est pas nul , la famille selectionnée est ajoutée d'office
     * dans la réponse.
     * @param marriageRecord  relevé de mariage
     * @param gedcom  gedcom
     * @param gedcom  selectedFamily
     * @return liste des familles compatibles avec le relevé. 
     */
    static protected List<Fam> findFamilyCompatibleWithMarriageRecord(MergeRecord marriageRecord, Gedcom gedcom, Fam selectedFamily) throws Exception {
        List<Fam> families = new ArrayList<Fam>();

        if ( selectedFamily != null) {
           families.add(selectedFamily);
        }

        // je recupere la date de mariage du releve
        PropertyDate marriageDate = marriageRecord.getEventDate();

        // 1) je recherche une famille avec un mari et une femme qui portent le même nom
        //     et dont les dates de naissance, de deces et de mariage sont compatibles
        for (Fam fam : gedcom.getFamilies()) {
            Indi husband = fam.getHusband();
            Indi wife = fam.getWife();

            if (husband == null && wife == null) {
                continue;
            }

            if (husband != null) {

                if (!marriageRecord.getIndi().getFatherLastName().isEmpty() ) {
                    if ( !isSameLastName(marriageRecord.getIndi().getFatherLastName(), husband.getLastName())) {
                        continue;
                    }
                } else {
                    // si le nom du pere est vide dans le releve, je verifie que le nom du pere du gedcom est identique
                    // au nom de l'individu du releve bien qu'ne theorie le nom de l'enfant peut etre diffrent de celui du pere
                    // (ceci pour eviter de trop nombreuses réponses sans rapport)
                    if ( !isSameLastName(marriageRecord.getIndi().getLastName(), husband.getLastName())) {
                        continue;
                    }
                }

                //meme prénom de l'epoux
                if (!marriageRecord.getIndi().getFirstName().isEmpty()
                        && !isSameFirstName(marriageRecord.getIndi().getFirstName(), husband.getFirstName())) {
                    continue;
                }
                
                // l'epoux doit avoir une date de naissance compatible
                if (!isCompatible(marriageRecord.getIndi().getBirthDate(), husband.getBirthDate())) {
                    continue;
                }
                
                // si la date de naissance de l'individu n'est pas precisée , l'epoux doit avoir au moins minMarriageYearOld 
                if ( ! marriageRecord.getIndi().getBirthDate().isValid() ) {
                    if (!isRecordAfterThanDate(marriageDate, husband.getBirthDate(), 0, minMarriageYearOld)) {
                        continue;
                    }
                }
                
                // l'epoux ne doit pas etre decede avant le mariage
                if (!isRecordBeforeThanDate(marriageDate, husband.getDeathDate(), 0, 0)) {
                    continue;
                }

                // l'epoux doit avoir une date de deces compatible
                if (!isCompatible(marriageRecord.getIndi().getDeathDate(), husband.getDeathDate())) {
                    continue;
                }

                // je verifie les parents de l'epoux
                Indi indiFather = husband.getBiologicalFather();
                if (indiFather != null) {
                    // meme nom du pere de l'epoux
                    if (!marriageRecord.getIndi().getFatherLastName().isEmpty()
                            && !isSameLastName(marriageRecord.getIndi().getFatherLastName(), indiFather.getLastName())) {
                        continue;
                    }

                    //meme prénom du pere de l'epoux
                    if (!marriageRecord.getIndi().getFatherFirstName().isEmpty()
                            && !isSameFirstName(marriageRecord.getIndi().getFatherFirstName(), indiFather.getFirstName())) {
                        continue;
                    }

                    // le pere doit avoir au moins minParentYearOld+minMarriageYearOld
                    if (!isRecordAfterThanDate(marriageDate, indiFather.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }

                    // le pere ne doit pas etre decede 9 mois avant la date de naissance de l'epoux
                    if (!isRecordBeforeThanDate(marriageRecord.getIndi().getBirthDate(), indiFather.getDeathDate(), 9, 0)) {
                        continue;
                    }
                }

                Indi indiMother = husband.getBiologicalMother();
                if (indiMother != null) {
                    // meme nom de la mere de l'epoux
                    if (!marriageRecord.getIndi().getMotherLastName().isEmpty()
                            && !isSameLastName(marriageRecord.getIndi().getMotherLastName(), indiMother.getLastName())) {
                        continue;
                    }

                    //meme prénom de la mere de l'epoux
                    if (!marriageRecord.getIndi().getMotherFirstName().isEmpty()
                            && !isSameFirstName(marriageRecord.getIndi().getMotherFirstName(), indiMother.getFirstName())) {
                        continue;
                    }

                    // la mere doit avoir au moins minParentYearOld+minMarriageYearOld
                    if (!isRecordAfterThanDate(marriageDate, indiMother.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }

                    // la mere ne doit pas etre decedee avant la date de naissance
                    if (!isRecordBeforeThanDate(marriageRecord.getIndi().getBirthDate(), indiMother.getDeathDate(), 0, 0)) {
                        continue;
                    }
                }                
            }

            if (wife != null) {
                // meme nom de l'epouse
                if (!marriageRecord.getWife().getLastName().isEmpty()
                        && !isSameLastName(marriageRecord.getWife().getLastName(), wife.getLastName())) {
                    continue;
                }
                //meme prénom de l'epouse
                if (!marriageRecord.getWife().getFirstName().isEmpty()
                        && !isSameFirstName(marriageRecord.getWife().getFirstName(), wife.getFirstName())) {
                    continue;
                }

                // l'epouse doit avoir une date de naissance compatible
                if (!isCompatible(marriageRecord.getWife().getBirthDate(), wife.getBirthDate())) {
                    continue;
                }

                // si la date de naissance de la femme n'est pas precisée, l'epouse doit avoir au moins minMarriageYearOld
                if (!marriageRecord.getWife().getBirthDate().isValid()) {
                    if (!isRecordAfterThanDate(marriageDate, wife.getBirthDate(), 0, minMarriageYearOld)) {
                        continue;
                    }
                }

                // l'epouse ne doit pas etre decedee avant le mariage
                if (!isRecordBeforeThanDate(marriageDate, wife.getDeathDate(), 0, 0)) {
                    continue;
                }

                // l'epouse doit avoir une date de deces compatible
                if (!isCompatible(marriageRecord.getWife().getDeathDate(), wife.getDeathDate())) {
                    continue;
                }

                // je verifie les parents de l'epoux
                Indi wifeFather = wife.getBiologicalFather();
                if (wifeFather != null) {
                    // meme nom du pere de l'epouse
                    if (!marriageRecord.getWife().getFatherLastName().isEmpty()
                            && !isSameLastName(marriageRecord.getWife().getFatherLastName(), wifeFather.getLastName())) {
                        continue;
                    }

                    //meme prénom du pere de l'epouse
                    if (!marriageRecord.getWife().getFatherFirstName().isEmpty()
                            && !isSameFirstName(marriageRecord.getWife().getFatherFirstName(), wifeFather.getFirstName())) {
                        continue;
                    }

                    // le pere doit etre né au moins minParentYearOld+minMarriageYearOld avant la date du mariage
                    if (!isRecordAfterThanDate(marriageDate, wifeFather.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }

                    // le pere ne doit pas etre decede 9 mois avant la date de naissance de l'epouse
                    if (!isRecordBeforeThanDate(marriageRecord.getWife().getBirthDate(), wifeFather.getDeathDate(), 9, 0)) {
                        continue;
                    }
                }

                Indi wifeMother = wife.getBiologicalMother();
                if (wifeMother != null) {
                    // meme nom de la mere de l'epouse
                    if (!marriageRecord.getWife().getMotherLastName().isEmpty()
                            && !isSameLastName(marriageRecord.getWife().getMotherLastName(), wifeMother.getLastName())) {
                        continue;
                    }

                    //meme prénom de la mere de l'epouse
                    if (!marriageRecord.getWife().getMotherFirstName().isEmpty()
                            && !isSameFirstName(marriageRecord.getWife().getMotherFirstName(), wifeMother.getFirstName())) {
                        continue;
                    }

                    // la mere doit avoir au moins minParentYearOld+minMarriageYearOld
                    if (!isRecordAfterThanDate(marriageDate, wifeMother.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }

                    // la mer ne doit pas etre decede avant la date de naissance de l'epouse
                    if (!isRecordBeforeThanDate(marriageRecord.getWife().getBirthDate(), wifeMother.getDeathDate(), 9, 0)) {
                        continue;
                    }
                }
            }

            // j'ajoute la famille dans la liste résultat si elle n'y est pas déjà
            if (!families.contains(fam)) {
                families.add(fam);
            }
        }

        return families;
    }

    /**
     * Recherche les individus correspondant aux parents du releve de naissance mais qui ne
     * sont pas mariés ensemble dans une des familles de la liste "families"
     *
     * @param record   relevé de naissance
     * @param gedcom   
     * @param families familles des individus a exlure de la réponse
     * @param fathers  (OUT) liste des hommes qui pourraient être le père
     * @param mothers  (OUT) liste des femmes qui pourraient être la mère
     */
    static protected void findFatherMotherCompatibleWithBirthParticipant(MergeRecord record, Gedcom gedcom, List<Fam> families, List<Indi> fathers, List<Indi> mothers) throws Exception {
        // je recupere la date de naissance du releve
        PropertyDate recordBirthDate = record.getIndi().getBirthDate();
        if (!recordBirthDate.isComparable()) {
            recordBirthDate = record.getEventDate();
        }

        Collection<Indi> entities = gedcom.getIndis();
        for (Iterator<Indi> it = entities.iterator(); it.hasNext();) {
            Indi indi = it.next();

            if (families!= null) {
                boolean found = false;
                for(Fam fam : families) {
                    Indi husband = fam.getHusband();
                    Indi wife = fam.getWife();

                    if( ( husband!=null && indi.compareTo(husband)== 0)
                          || ( wife!=null && indi.compareTo(wife)==0)  ) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    continue;
                }
            }

            if (indi.getSex() == PropertySex.MALE) {
                // meme nom du pere
                if (!record.getIndi().getFatherLastName().isEmpty() ) {
                    if ( !isSameFirstName(record.getIndi().getFatherLastName(), indi.getLastName())) {
                        continue;
                    }
                } else {
                    // si le nom du pere est vide dans le releve, je verifie que le nom du pere du gedcom est identique
                    // au nom de l'individu du releve (pour eviter de trop nombreuses réponses sans rapport)
                    if ( !isSameLastName(record.getIndi().getLastName(), indi.getLastName())) {
                        continue;
                    }
                }

                //  le prénom du pere ne doit pas vide et identique au nom du pere dans le releve
                if (record.getIndi().getFatherFirstName().isEmpty() || indi.getFirstName().isEmpty()
                        || !isSameFirstName(record.getIndi().getFatherFirstName(), indi.getFirstName())) {
                    continue;
                }

                // le pere doit avoir au moins minParentYearOld
                if (!isRecordAfterThanDate(recordBirthDate, indi.getBirthDate(), 0, minParentYearOld)) {
                    continue;
                }

                // le pere ne doit pas etre decede 9 mois avant la date de naissance
                if (!isRecordBeforeThanDate(recordBirthDate, indi.getDeathDate(), 9, 0)) {
                    continue;
                }

                // Le pere ne doit pas etre deja marié avec une autre personne avant la date du releve
                // et avoir avec elle un enfant avant la date du releve et un autre apres la date du releve
                Fam[] fams = indi.getFamiliesWhereSpouse();
                boolean incompatible = false;
                for( Fam fam : fams) {
                    PropertyDate marriageDate = fam.getMarriageDate();
                    Indi [] children = fam.getChildren(true);
                    PropertyDate firtChildBirthDate = null;
                    PropertyDate lastChildBirthDate = null;
                    if ( children.length > 0 ) {
                        firtChildBirthDate = children[0].getBirthDate();
                        lastChildBirthDate = children[children.length-1].getBirthDate();
                    }
                    if ( !isRecordBeforeThanDate(recordBirthDate, marriageDate,0, 0)
                         && !isRecordBeforeThanDate(recordBirthDate, firtChildBirthDate,0, 0)
                         && !isRecordAfterThanDate(recordBirthDate, lastChildBirthDate,0, 0)

                         ) {
                        Indi wife = fam.getWife();
                        if ( wife != null) {
                            if ( !record.getIndi().getMotherLastName().isEmpty()
                                    &&!isSameLastName(record.getIndi().getMotherLastName(), wife.getLastName())
                                    && !record.getIndi().getMotherFirstName().isEmpty()
                                    && !isSameFirstName(record.getIndi().getMotherFirstName(), wife.getFirstName() )
                                 ) {
                                    incompatible = true;
                                    break;

                            }
                        }
                    }
                }
                if (incompatible) {
                    continue;
                }

                fathers.add(indi);

            } else if (indi.getSex() == PropertySex.FEMALE) {

                // meme nom de la mere , le nom de la mere ne doit pas être vide
                if ( record.getIndi().getMotherLastName().isEmpty() || indi.getLastName().isEmpty()
                        || !isSameLastName(record.getIndi().getMotherLastName(), indi.getLastName())) {
                    continue;
                }

                //meme prénom de la mere, le prenom de la mere ne doit pas etre vide
                if (record.getIndi().getMotherFirstName().isEmpty() ||  !indi.getFirstName().isEmpty()
                        && !isSameFirstName(record.getIndi().getMotherFirstName(), indi.getFirstName())) {
                    continue;
                }

                // la mere doit avoir au moins minParentYearOld
                if (!isRecordAfterThanDate(recordBirthDate, indi.getBirthDate(), 0, minParentYearOld)) {
                    continue;
                }

                // la mere ne doit pas etre decedee avant la date de naissance
                if (!isRecordBeforeThanDate(recordBirthDate, indi.getDeathDate(), 0, 0)) {
                    continue;
                }

                // La mere ne doit pas etre deja mariée avec une autre personne avant la date de la naissance
                // et avoir au moins un enfant apres la date du releve avec cette autre personne
                // ou 
                // elle ne doit pas avoir un enfant avant la date du releve et un autre enfant après la date du releve de naissance
                Fam[] fams = indi.getFamiliesWhereSpouse();
                boolean incompatible = false;
                for( Fam fam : fams) {
                    PropertyDate marriageDate = fam.getMarriageDate();
                    Indi [] children = fam.getChildren(true);
                    PropertyDate firtChildBirthDate = null;
                    PropertyDate lastChildBirthDate = null;
                    if ( children.length > 0 ) {
                        firtChildBirthDate = children[0].getBirthDate();
                        lastChildBirthDate = children[children.length-1].getBirthDate();
                    }
                    if ( !isRecordBeforeThanDate(recordBirthDate, marriageDate,0, 0)
                         && !isRecordBeforeThanDate(recordBirthDate, firtChildBirthDate,0, 0)
                         && !isRecordAfterThanDate(recordBirthDate, lastChildBirthDate,0, 0)
                         && !record.getIndi().getFatherLastName().isEmpty()
                         && !isSameLastName(record.getIndi().getFatherLastName(), fam.getHusband().getLastName())
                         && !record.getIndi().getFatherFirstName().isEmpty()
                         && !isSameFirstName(record.getIndi().getFatherFirstName(), fam.getHusband().getFirstName())
                         ) {
                        incompatible = true;
                        break;
                    }
                }
                if (incompatible) {
                    continue;
                }

                mothers.add(indi);
            }
        }

    }



    /**
     * recherche les individus qui sont compatibles avec les epoux du releve de mariage
     *  et qui ne font pas partie des familles excludedFamilies
     * @param marriageRecord  releve de mariage
     * @param gedcom
     * @param excludedFamilies
     * @param husbands  liste des pères (résultat de la recherche)
     * @param wifes     liste des mères (résultat de la recherche)
     * @return
     */
    static protected void findHusbanWifeCompatibleWithMarriageRecord(MergeRecord marriageRecord, Gedcom gedcom, List<Fam> excludedFamilies, List<Indi> husbands, List<Indi> wifes) throws Exception  {
        // je recupere la date de mariage du releve
        PropertyDate marriageDate = marriageRecord.getEventDate();

        Collection<Indi> entities = gedcom.getIndis();
        for (Iterator<Indi> it = entities.iterator(); it.hasNext();) {
            Indi indi = it.next();

            if (excludedFamilies != null) {
                boolean found = false;
                for (Fam fam : excludedFamilies) {
                    Indi husband = fam.getHusband();
                    Indi wife = fam.getWife();
                    // je compare les ID s'il n'est pas null
                    if( ( husband!=null && indi.compareTo(husband)== 0)
                          || ( wife!=null && indi.compareTo(wife)==0)  ) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    continue;
                }
            }

            if (indi.getSex() == PropertySex.MALE) {
                Indi husband = indi;

                // meme nom de l'epoux
                if (!marriageRecord.getIndi().getLastName().isEmpty()
                        && !isSameLastName(marriageRecord.getIndi().getLastName(), husband.getLastName())) {
                    continue;
                }
                //meme prénom de l'epoux
                if (!marriageRecord.getIndi().getFirstName().isEmpty()
                        && !isSameFirstName(marriageRecord.getIndi().getFirstName(), husband.getFirstName())) {
                    continue;
                }
                
                // l'epoux doit avoir une date de naissance compatible
                if (!isCompatible(marriageRecord.getIndi().getBirthDate(), husband.getBirthDate())) {
                    continue;
                }

                // si la date de naissance de l'individu n'est pas precisée , l'epoux doit avoir au moins minMarriageYearOld 
                if ( ! marriageRecord.getIndi().getBirthDate().isValid() ) {
                    if (!isRecordAfterThanDate(marriageDate, husband.getBirthDate(), 0, minMarriageYearOld)) {
                        continue;
                    }
                }

                // l'epoux ne doit pas etre decede avant le mariage
                if (!isRecordBeforeThanDate(marriageDate, husband.getDeathDate(), 0, 0)) {
                    continue;
                }

                // l'epoux doit avoir une date de deces compatible
                if (!isCompatible(marriageRecord.getIndi().getDeathDate(), husband.getDeathDate())) {
                    continue;
                }

                // je verifie les parents de l'epoux
                Indi indiFather = husband.getBiologicalFather();
                if (indiFather != null) {
                    // meme nom du pere de l'epoux
                    if (!marriageRecord.getIndi().getFatherLastName().isEmpty()
                            && !isSameLastName(marriageRecord.getIndi().getFatherLastName(), indiFather.getLastName())) {
                        continue;
                    }

                    //meme prénom du pere de l'epoux
                    if (!marriageRecord.getIndi().getFatherFirstName().isEmpty()
                            && !isSameFirstName(marriageRecord.getIndi().getFatherFirstName(), indiFather.getFirstName())) {
                        continue;
                    }

                    // le pere doit etre ne au moins minParentYearOld+minMarriageYearOld avant le mariage
                    if (!isRecordAfterThanDate(marriageDate, indiFather.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }
                    // le pere ne doit pas etre decede 9 mois avant la date de naissance de l'epoux
                    if (!isRecordBeforeThanDate(marriageRecord.getIndi().getBirthDate(), indiFather.getDeathDate(), 9, 0)) {
                        continue;
                    }
                }

                Indi indiMother = husband.getBiologicalMother();
                if (indiMother != null) {
                    // meme nom de la mere de l'epoux
                    if (!marriageRecord.getIndi().getMotherLastName().isEmpty()
                            && !isSameLastName(marriageRecord.getIndi().getMotherLastName(), indiMother.getLastName())) {
                        continue;
                    }

                    //meme prénom de la mere  de l'epoux
                    if (!marriageRecord.getIndi().getMotherFirstName().isEmpty()
                            && !isSameFirstName(marriageRecord.getIndi().getMotherFirstName(), indiMother.getFirstName())) {
                        continue;
                    }

                    // la mere doit etre ne au moins minParentYearOld+minMarriageYearOld avant le mariage
                    if (!isRecordAfterThanDate(marriageDate, indiMother.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }

                    // la mere ne doit pas etre decede avant la date de naissance de l'epoux
                    if (!isRecordBeforeThanDate(marriageRecord.getIndi().getBirthDate(), indiFather.getDeathDate(), 0, 0)) {
                        continue;
                    }
                }

                // je verifie s'il a un conjoint en vie avec un nom différent et un prenom different
                boolean oftherSpouseFound = false;
                Fam [] spouseFamList = husband.getFamiliesWhereSpouse();
                for (Fam fam : spouseFamList) {
                    Indi wife = fam.getWife();

                    if ( ! (isSameLastName(marriageRecord.getWife().getLastName(), wife.getLastName())
                            && isSameLastName(marriageRecord.getWife().getFirstName(), wife.getFirstName())) ) {

                        if (!isRecordBeforeThanDate(marriageDate, fam.getMarriageDate(), 0, 0)) {
                            // si le mariage est apres le mariage avec l'autre conjoint
                            // alors le mariage doit etre apres le deces de l'autre conjoint
                            if (!isRecordAfterThanDate(marriageDate, wife.getDeathDate(), 0, 0)) {
                                oftherSpouseFound = true;
                                break;
                            }
                        }
                    }
                }
                if(oftherSpouseFound){
                    continue;
                }

                // j'ajoute l'epoux
                husbands.add(husband);

            } else if (indi.getSex() == PropertySex.FEMALE) {
                Indi wife = indi;

                // meme nom de l'epouse
                if (!marriageRecord.getWife().getLastName().isEmpty()
                        && !isSameLastName(marriageRecord.getWife().getLastName(), wife.getLastName())) {
                    continue;
                }
                //meme prénom de l'epouse
                if (!marriageRecord.getWife().getFirstName().isEmpty()
                        && !isSameFirstName(marriageRecord.getWife().getFirstName(), wife.getFirstName())) {
                    continue;
                }
                
                // l'epouse doit avoir une date de naissance compatible
                if (!isCompatible(marriageRecord.getWife().getBirthDate(), wife.getBirthDate())) {
                    continue;
                }
                
                // si la date de naissance de la femme n'est pas precisée, l'epouse doit avoir au moins minMarriageYearOld
                if (!marriageRecord.getWife().getBirthDate().isValid()) {
                    if (!isRecordAfterThanDate(marriageDate, wife.getBirthDate(), 0, minMarriageYearOld)) {
                        continue;
                    }
                }
                
                // l'epouse ne doit pas etre decedee avant  le mariage
                if (!isRecordBeforeThanDate(marriageDate, wife.getDeathDate(), 0, 0)) {
                    continue;
                }

                // l'epouse doit avoir une date de deces compatible
                if (!isCompatible(marriageRecord.getWife().getDeathDate(), wife.getDeathDate())) {
                    continue;
                }

                // je verifie les parents de l'epouse
                Indi wifeFather = wife.getBiologicalFather();
                if (wifeFather != null) {
                    // meme nom du pere de l'epouse
                    if (!marriageRecord.getWife().getFatherLastName().isEmpty()
                            && !isSameLastName(marriageRecord.getWife().getFatherLastName(), wifeFather.getLastName())) {
                        continue;
                    }

                    //meme prénom du pere de l'epouse
                    if (!marriageRecord.getWife().getFatherFirstName().isEmpty()
                            && !isSameFirstName(marriageRecord.getWife().getFatherFirstName(), wifeFather.getFirstName())) {
                        continue;
                    }

                     // le pere doit etre ne au moins minParentYearOld+minMarriageYearOld avant le mariage
                    if (!isRecordAfterThanDate(marriageDate, wifeFather.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }
                    // le pere ne doit pas etre decede 9 mois avant la date de naissance de l'epouse
                    if (!isRecordBeforeThanDate(marriageRecord.getWife().getBirthDate(), wifeFather.getDeathDate(), 9, 0)) {
                        continue;
                    }

                }

                // l'epouse doit avoir au moins minMarriageYearOld
                if (!isRecordAfterThanDate(marriageDate, wife.getBirthDate(), 0, minMarriageYearOld)) {
                    continue;
                }

                Indi wifeMother = wife.getBiologicalMother();
                if (wifeMother != null) {
                    // meme nom de la mere de l'epouse
                    if (!marriageRecord.getWife().getMotherLastName().isEmpty()
                            && !isSameLastName(marriageRecord.getWife().getMotherLastName(), wifeMother.getLastName())) {
                        continue;
                    }

                    //meme prénom de la mere de l'epouse
                    if (!marriageRecord.getWife().getMotherFirstName().isEmpty()
                            && !isSameFirstName(marriageRecord.getWife().getMotherFirstName(), wifeMother.getFirstName())) {
                        continue;
                    }

                   // la mere doit etre ne au moins minParentYearOld+minMarriageYearOld avant le mariage
                    if (!isRecordAfterThanDate(marriageDate, wifeMother.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }

                    // la mere ne doit pas etre decede avant la date de naissance de l'epouse
                    if (!isRecordBeforeThanDate(marriageRecord.getWife().getBirthDate(), wifeMother.getDeathDate(), 0, 0)) {
                        continue;
                    }

                }

                // je verifie s'il a un conjoint en vie avec un nom différent et un prenom different
                boolean oftherSpouseFound = false;
                Fam [] spouseFamList = wife.getFamiliesWhereSpouse();
                for (Fam fam : spouseFamList) {
                    Indi husband = fam.getHusband();

                    if ( ! (isSameLastName(marriageRecord.getIndi().getLastName(), husband.getLastName())
                            && isSameLastName(marriageRecord.getIndi().getFirstName(), husband.getFirstName())) ) {

                        if (!isRecordBeforeThanDate(marriageDate, fam.getMarriageDate(), 0, 0)) {
                            // si le mariage est apres le mariage avec l'autre conjoint
                            // alors le mariage doit etre apres le deces de l'autre conjoint
                            if (!isRecordAfterThanDate(marriageDate, husband.getDeathDate(), 0, 0)) {
                                oftherSpouseFound = true;
                                break;
                            }
                        }
                    }
                }
                if(oftherSpouseFound){
                    continue;
                }

                wifes.add(wife);
            }

        }

    }


    /**
     * retourne la liste des individus qui ont le même nom et une date de naissance
     * compatible avec celle du relevé
     * mais qui ont un identifiant different de l'individu excludeIndi
     * @param record
     * @param gedcom
     * @param excludeIndi
     * @return liste des individus
     */
    static protected List<Indi> findIndiCompatibleWithParticipant(MergeRecord record,  MergeParticipantType participantType, Gedcom gedcom, Indi excludeIndi) throws Exception {
        List<Indi> sameIndis = new ArrayList<Indi>();
        MergeParticipant participant = record.getParticipant(participantType);
        for (Indi indi : gedcom.getIndis()) {

            // individu a exclure
            if (excludeIndi != null && excludeIndi.compareTo(indi) == 0) {
                continue;
            }

            // meme sexe de l'individu
            if (participant.getSex() != FieldSex.UNKNOWN
                    && indi.getSex() != PropertySex.UNKNOWN
                    && participant.getSex() != indi.getSex()) {
                continue;
            }

            // meme nom de l'individu
            if (!participant.getLastName().isEmpty()
                    && !isSameLastName(participant.getLastName(), indi.getLastName())) {
                continue;
            }

            // meme prenom de l'individu
            if (!participant.getFirstName().isEmpty()
                    && !isSameFirstName(participant.getFirstName(), indi.getFirstName())) {
                continue;
            }

            // la date de naissance doit etre renseignee
            if (!participant.getBirthDate().isComparable()) {
                // j'abandonne si la date de naissance du relevé n'est pas renseignée
                continue;
            }

            // la date de naissance du relevé doit être compatible avec celle de l'individu
            // petit raccourci pour gagner du temps
            PropertyDate indiBirtDate = indi.getBirthDate();
            if (indiBirtDate != null) {
                // je tolere un jour d'écart pour la date de naissance
                if (!isCompatible(participant.getBirthDate(), indiBirtDate, 1)) {
                    // la date de naissance de l'individu n'est pas compatible avec la date du relevé
                    continue;
                }
            }

            // la date de décès doit être compatible doit être compatible avec celle de l'individu
            // petit raccourci pour gagner du temps
            PropertyDate indiDeathDate = indi.getDeathDate();
            if (indiDeathDate != null) {
                if (!isCompatible(participant.getDeathDate(), indiDeathDate)) {
                    continue;
                }
            }

            // la date de décès du relevé doit être après le mariage de l'individu
            Fam[] sameIndiFamiliesWhereSpouse = indi.getFamiliesWhereSpouse();
            boolean foundConflict = false;
            for (Fam fam : sameIndiFamiliesWhereSpouse) {
                if (!MergeQuery.isRecordAfterThanDate(participant.getDeathDate(), fam.getMarriageDate(), 0, 0)) {
                    foundConflict = true;
                }
            }
            if (foundConflict) {
                // il a conflit , je ne retiens pas cet individu
                continue;
            }

            Fam parentFamily = indi.getFamilyWhereBiologicalChild();
            if (parentFamily != null) {
                PropertyDate parentMarriageDate = parentFamily.getMarriageDate();
                if (parentMarriageDate != null) {
                    // la naissance doit être après la date mariage des parents.
                    if (!isRecordAfterThanDate(participant.getBirthDate(), parentMarriageDate, 0, 0)) {
                        continue;
                    }
                    // le deces doit être après la date mariage des parents.
                    if (!isRecordAfterThanDate(participant.getDeathDate(), parentMarriageDate, 0, 0)) {
                        continue;
                    }
                }

                Indi father = parentFamily.getHusband();
                if (father != null) {

                    // meme nom du pere
                    if (!participant.getFatherLastName().isEmpty()
                            && !isSameLastName(participant.getFatherLastName(), father.getLastName())) {
                        continue;
                    }
                    //meme prénom du pere
                    if (!participant.getFatherFirstName().isEmpty()
                            && !isSameFirstName(participant.getFatherFirstName(), father.getFirstName())) {
                        continue;
                    }

                    // le pere doit avoir au moins minParentYearOld
                    if (!isRecordAfterThanDate(participant.getBirthDate(), father.getBirthDate(), 0, minParentYearOld)) {
                        continue;
                    }
                    // le pere ne doit pas etre decede 9 mois avant la date de naissance
                    if (!isRecordBeforeThanDate(participant.getBirthDate(), father.getDeathDate(), 9, 0)) {
                        continue;
                    }
                }

                Indi mother = parentFamily.getWife();
                if (mother != null) {
                    // meme nom de la mere
                    if (!participant.getMotherLastName().isEmpty()
                            && !isSameLastName(participant.getMotherLastName(), mother.getLastName())) {
                        continue;
                    }
                    //meme prénom de la mere
                    if (!participant.getMotherFirstName().isEmpty()
                            && !isSameFirstName(participant.getMotherFirstName(), mother.getFirstName())) {
                        continue;
                    }
                    // la mere doit avoir au moins minParentYearOld
                    if (!isRecordAfterThanDate(participant.getBirthDate(), mother.getBirthDate(), 0, minParentYearOld)) {
                        continue;
                    }
                    // la mere ne doit pas etre decedee avant la date de naissance
                    if (!isRecordBeforeThanDate(participant.getBirthDate(), mother.getDeathDate(), 0, 0)) {
                        continue;
                    }
                }
            }
            // j'ajoute l'individu dans la liste
            sameIndis.add(indi);

        }
        return sameIndis;
    }


    /**
     * retourne la liste des enfants de la famille sélectionnée qui ont le même prénom
     * et des dates de naissance et de décès compatiblew avec celle du relevé.
     * Génère une exception si le nom des parents de la famille sélectionnée est different
     * du nom des parents du releve, ou si leur age ou la date de mariage n'est pas
     * compatible avec la date de naissance.
     * @param birthRecord
     * @param gedcom
     * @param selectedFamily  famille sélectionnée
     * @return liste des enfants
     */
    static protected List<Indi> findSameChild(MergeRecord birthRecord, Gedcom gedcom, Fam selectedFamily) throws Exception {
        List<Indi> sameChildren = new ArrayList<Indi>();

        // je recupere la date de naissance du releve
        PropertyDate recordBirthDate = birthRecord.getIndi().getBirthDate();
        
        if (selectedFamily != null) {
//            PropertyDate marriageDate = selectedFamily.getMarriageDate();
//            if (marriageDate != null) {
//                // le releve de naissance doit être après la date mariage.
//                if (!isRecordAfterThanDate(recordBirthDate, marriageDate, 0, 0)) {
//                    //throw new Exception("la date de naissance du releve doit être après la date mariage");
//                }
//            }
//
//            Indi father = selectedFamily.getHusband();
//            if (father != null) {
//
//                // meme nom du pere
//                if (!birthRecord.getIndi().getFatherLastName().isEmpty()
//                        && !isSameName(birthRecord.getIndi().getFatherLastName(), father.getLastName())) {
//                    //throw new Exception("le nom du pere est different");
//                }
//                //meme prénom du pere
//                if (!birthRecord.getIndi().getFatherFirstName().isEmpty()
//                        && !isSameName(birthRecord.getIndi().getFatherFirstName(), father.getFirstName())) {
//                    //throw new Exception("le prenom du pere est different");
//                }
//
//                // le pere doit avoir au moins minParentYearOld
//                if (!isRecordAfterThanDate(recordBirthDate, father.getBirthDate(), 0, minParentYearOld)) {
//                    throw new Exception("le pere n'a pas l'age requis");
//                }
//                // le pere ne doit pas etre decede 9 mois avant la date de naissance
//                if (!isRecordBeforeThanDate(recordBirthDate, father.getDeathDate(), 9, 0)) {
//                    throw new Exception("le pere est decede avant la naissance");
//                }
//            }
//
//            Indi mother = selectedFamily.getWife();
//            if (mother != null) {
//                // meme nom de la mere
//                if (!birthRecord.getIndi().getMotherLastName().isEmpty()
//                        && !isSameName(birthRecord.getIndi().getMotherLastName(), mother.getLastName())) {
//                    //throw new Exception("le nom de la mere est different");
//                }
//                //meme prénom de la mere
//                if (!birthRecord.getIndi().getMotherFirstName().isEmpty()
//                        && !isSameName(birthRecord.getIndi().getMotherFirstName(), mother.getFirstName())) {
//                    //throw new Exception("le prénom de la mere est different");
//                }
//                // la mere doit avoir au moins minParentYearOld
//                if (!isRecordAfterThanDate(recordBirthDate, mother.getBirthDate(), 0, minParentYearOld)) {
//                    //throw new Exception("la mere n'a pas l'age requis");
//                }
//                // la mere ne doit pas etre decedee avant la date de naissance
//                if (!isRecordBeforeThanDate(recordBirthDate, mother.getDeathDate(), 0, 0)) {
//                    //throw new Exception("la mere est décédée avant la naissance");
//                }
//            }

            // je recherche les enfants conpatibles avec le relevé
            for (Indi child : selectedFamily.getChildren()) {

                // meme sexe de l'enfant
                if (birthRecord.getIndi().getSex() != FieldSex.UNKNOWN
                        && child.getSex() != PropertySex.UNKNOWN
                        && birthRecord.getIndi().getSex() != child.getSex()) {
                    continue;
                }

                // meme nom de l'enfant
                if (!birthRecord.getIndi().getLastName().isEmpty()
                        && !isSameLastName(birthRecord.getIndi().getLastName(), child.getLastName())) {
                    continue;
                }

                // meme prenom de l'enfant
                if (!birthRecord.getIndi().getFirstName().isEmpty()
                        && !isSameFirstName(birthRecord.getIndi().getFirstName(), child.getFirstName())) {
                    continue;
                }

                
                // petit raccourci pour gagner du temps
                PropertyDate indiBirtDate = child.getBirthDate();
                
                // date de naissance compatible
                if (indiBirtDate != null) {
                    if (!isCompatible(recordBirthDate, indiBirtDate)) {
                        // la date de naissance de l'individu n'est pas compatible avec la date du relevé
                        continue;
                    }
                }

                // date de naissance apres la date de naissance des parents
                Indi father = selectedFamily.getHusband();
                if (father != null ) {
                    if (!isRecordAfterThanDate(recordBirthDate, father.getBirthDate(), 0, minParentYearOld)) {
                        // la date de naissance de l'individu n'est pas apres avec la date de naissance du pere
                        continue;
                    }
                }
                Indi mother = selectedFamily.getWife();
                if (mother != null ) {
                    if (!isRecordAfterThanDate(recordBirthDate, mother.getBirthDate(), 0, minParentYearOld)) {
                        // la date de naissance de l'individu n'est pas apres avec la date de naissance de la mere
                        continue;
                    }
                }

                // date de décés compatible
                PropertyDate childDeathDate = child.getDeathDate();
                if (childDeathDate != null) {
                    if (!isCompatible(birthRecord.getIndi().getDeathDate(), childDeathDate)) {
                        // la date de décès de l'individu n'est pas compatible avec la date du relevé
                        continue;
                    }
                }

                // j'ajoute l'enfant dans la liste
                sameChildren.add(child);
            }
        }
        return sameChildren;
    }


    /**
     * retourne "true" si la date du releve est egale ou est compatible avec la
     * date de l'entite du gedcom.
     *
     * @param recordDate
     * @param entityDate
     * @return
     */
    static protected boolean isCompatible(PropertyDate recordDate, PropertyDate entityDate) {

        return isCompatible( recordDate,  entityDate, 0) ;
    }


    /**
     * retourne "true" si la date du releve est egale ou est compatible avec la
     * date de l'entite du gedcom avec une marge exprimée en jour
     *
     * @param recordDate
     * @param entityDate
     * @return
     */
    static protected boolean isCompatible(PropertyDate recordDate, PropertyDate entityDate, int marge) {
        boolean result;
        if (recordDate == null ) {
            return true;
        } else if (!recordDate.isComparable()) {
            return true;
        }

        if (entityDate == null) {
            return true;
        } else if (!entityDate.isComparable()) {
            return true;
        }

        try {
            int recordStart;
            int recordEnd;

            if (recordDate.getFormat() == PropertyDate.DATE) {
                PointInTime pit = new PointInTime();
                pit.set(recordDate.getStart());
                recordStart = pit.getJulianDay();
                recordEnd   = recordStart;
            } else if (recordDate.getFormat() == PropertyDate.BETWEEN_AND || recordDate.getFormat() == PropertyDate.FROM_TO) {
                PointInTime pit = new PointInTime();
                pit.set(recordDate.getStart());
                recordStart = pit.getJulianDay();
                pit.set(recordDate.getEnd());
                recordEnd = pit.getJulianDay();
            } else if (recordDate.getFormat() == PropertyDate.FROM || recordDate.getFormat() == PropertyDate.AFTER) {
                PointInTime pit = new PointInTime();
                pit.set(recordDate.getStart());
                recordStart = pit.getJulianDay();
                pit.set(recordDate.getStart());
                recordEnd = pit.add(0, 0, +indiMaxYearOld).getJulianDay();
            } else if (recordDate.getFormat() == PropertyDate.TO || recordDate.getFormat() == PropertyDate.BEFORE) {
                PointInTime pit = new PointInTime();
                pit.set(recordDate.getStart());
                recordStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
                pit.set(recordDate.getStart());
                if ( pit.getMonth()== PointInTime.UNKNOWN) {
                    // si le mois n'est pas précisé je remplace par la fin d'année, c'est a dire le debut de l'année suivante
                    pit.add(0, 0, +1);
                }
                recordEnd = pit.getJulianDay();
            } else {
                // ABOUT, ESTIMATED, CALCULATED
                PointInTime pit = new PointInTime();
                pit.set(recordDate.getStart());
                recordStart = pit.add(0, 0, -aboutYear).getJulianDay();
                pit.set(recordDate.getStart());
                recordEnd = pit.add(0, 0, +aboutYear).getJulianDay();
            }

            int entityStart;
            int entityEnd;

            if (entityDate.getFormat() == PropertyDate.DATE) {
                PointInTime pit = new PointInTime();
                pit.set(entityDate.getStart());
                entityStart = pit.getJulianDay();
                entityEnd =  entityStart;
            } else if (entityDate.getFormat() == PropertyDate.BETWEEN_AND || entityDate.getFormat() == PropertyDate.FROM_TO) {
                entityStart = entityDate.getStart().getJulianDay();
                entityEnd = entityDate.getEnd().getJulianDay();
            } else if (entityDate.getFormat() == PropertyDate.FROM || entityDate.getFormat() == PropertyDate.AFTER) {
                PointInTime pit = new PointInTime();
                pit.set(entityDate.getStart());
                entityStart = pit.getJulianDay();
                pit.set(entityDate.getStart());
                entityEnd = pit.add(0, 0, +indiMaxYearOld).getJulianDay();
            } else if (entityDate.getFormat() == PropertyDate.TO || entityDate.getFormat() == PropertyDate.BEFORE) {
                PointInTime pit = new PointInTime();
                pit.set(entityDate.getStart());
                entityStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
                entityEnd = entityDate.getStart().getJulianDay();
            } else {
                // ABOUT, ESTIMATED, CALCULATED
                PointInTime pit = new PointInTime();
                pit.set(entityDate.getStart());
                entityStart = pit.add(0, 0, -aboutYear).getJulianDay();
                pit.set(entityDate.getStart());
                entityEnd = pit.add(0, 0, +aboutYear).getJulianDay();
            }

            // l'intersection des deux intervalles ne doit pas être vide.
            if ( recordEnd >= entityStart - marge   ) {
               if  ( entityEnd >= recordStart - marge) {
                    result = true;
                } else {
                   result = false;
                }
            } else {
                   result = false;

            }
        } catch (GedcomException ex) {
            result = false;
        }
        return result;
    }

    /**
     * retourne true si l'intervalle [start end ] contient refTime
     *
     *                       refTime
     *    --- [****]----- [*****X*****]------[*****] -----
     *        false          true             false
     *
     * @param refTime
     * @param start
     * @param end
     * @return
     */
    static private boolean containsRefTime(final PointInTime refTime, final PointInTime start , final PointInTime end) throws GedcomException {
        int jdRef = refTime.getJulianDay();
        int jdStart = start.getJulianDay();
        int jdEnd   = end.getJulianDay();
        if ( jdStart <= jdRef && jdRef <= jdEnd) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * retourne true si str1 est égal ou se prononce comme str2
     *   ou si str1 est vide
     * @param str1
     * @param str2
     * @return
     */
    static protected boolean isSameLastName(String str1, String str2) {
        boolean result = false;

        if( str1 != null && ! str1.isEmpty()) {
            String[] names1 = str1.split(",");
            String[] names2 = str2.split(",");

            for( String name1 : names1) {
                String similarName1 = SimilarNameSet.getSimilarLastName().getSimilarName(name1);
                for( String name2 : names2) {
                    result |= dm.encode(similarName1).equals(dm.encode(SimilarNameSet.getSimilarLastName().getSimilarName(name2)));
                }
            }
        } else {
            // 
            return true;
        }
        return result;
        //return dm.encode(str1).equals(dm.encode(str2));
        //return str1.equals(str2);
    }

    /**
     * retourne true si str1 est égal ou se prononce comme str2
     * @param str1
     * @param str2
     * @return
     */
    static protected boolean isSameFirstName(String str1, String str2) {
        return dm.encode(SimilarNameSet.getSimilarFirstName().getSimilarName(str1)).equals(dm.encode(SimilarNameSet.getSimilarFirstName().getSimilarName(str2)));
        //return str1.equals(str2);
    }

    /**
     * retourne true si la date de naissance du parent est inférieure à la date
     * du relevé (diminuée de l'age minimum pour être parent)
     * et si le parent a moins de indiMaxYearOld (=100 ans) à la date du relevé.
     * Autrement dit :
     *   recordDate - indiMaxYearOld <  parentBirthDate  < recordDate - (minMonthShift + minYearShift)
     *
     *   recordDate-100ans  parentBirthDate    recordDate-minYearShift
     *   ----[}------------------[******]------[******]------------  => true
     *
     *    r-100ans   recordDate-minYearShift   parentBirthDate
     *   ----[}-------- [******]-----------------[******]----------  => false
     *
     *    parentBirthDate   recordDate-100ans  recordDate-minYearShift
     *   ---[******]------------[}---------------- [******]--------  => false
     *
     * @param recordDate  date du relevé
     * @param parentDeathDate  date de naissance du parent
     * @param minDiff
     * @return true si indiFirthDate + minMonthDiff + minYearDiff <= recordBirthDate
     */
     static protected boolean isRecordAfterThanDate(PropertyDate recordDate, PropertyDate parentBirthDate, int minMonthShift, int minYearShift) {
        boolean result;
        if (recordDate == null ) {
            return true;
        } else if (!recordDate.isComparable()) {
            return true;
        }

        if (parentBirthDate == null) {
            return true;
        } else if (!parentBirthDate.isComparable()) {
            return true;
        }

        try {
            int minStart;
            int minEnd;
            int maxStart;
            int maxEnd;
            PointInTime pit = new PointInTime();

            if (recordDate.getFormat() == PropertyDate.DATE) {
                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                minStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
                minEnd   = minStart;

                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                maxStart = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
                maxEnd   = maxStart;
            } else if (recordDate.getFormat() == PropertyDate.BETWEEN_AND || recordDate.getFormat() == PropertyDate.FROM_TO) {
                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                minStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
                pit.set(recordDate.getEnd().getPointInTime(PointInTime.GREGORIAN));
                minEnd = pit.add(0, 0, -indiMaxYearOld).getJulianDay();

                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                maxStart = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
                pit.set(recordDate.getEnd().getPointInTime(PointInTime.GREGORIAN));
                maxEnd = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
            } else if (recordDate.getFormat() == PropertyDate.FROM || recordDate.getFormat() == PropertyDate.AFTER) {
                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                minStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
                minEnd = Integer.MAX_VALUE;

                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                maxStart = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
                maxEnd = Integer.MAX_VALUE;
            } else if (recordDate.getFormat() == PropertyDate.TO || recordDate.getFormat() == PropertyDate.BEFORE) {
                minStart = Integer.MIN_VALUE;
                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                minEnd = pit.add(0, 0, -indiMaxYearOld).getJulianDay();

                maxStart = Integer.MIN_VALUE;
                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                maxEnd = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
            } else  {
                // ABOUT, ESTIMATED, CALCULATED
                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                minStart = pit.add(0, 0, -indiMaxYearOld-aboutYear).getJulianDay();
                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                minEnd = pit.add(0, 0, -indiMaxYearOld+aboutYear).getJulianDay();

                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                maxStart = pit.add(0, -minMonthShift, -minYearShift-aboutYear).getJulianDay();
                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                maxEnd = pit.add(0, -minMonthShift, -minYearShift +aboutYear).getJulianDay();
            } 

            int birthStart;
            int birthEnd;

            if (parentBirthDate.getFormat() == PropertyDate.DATE) {
                birthStart = parentBirthDate.getStart().getJulianDay();
                birthEnd   = birthStart;
            } else if (parentBirthDate.getFormat() == PropertyDate.BETWEEN_AND || parentBirthDate.getFormat() == PropertyDate.FROM_TO) {
                birthStart = parentBirthDate.getStart().getJulianDay();
                birthEnd   =  parentBirthDate.getEnd().getJulianDay();
            } else if (parentBirthDate.getFormat() == PropertyDate.FROM || parentBirthDate.getFormat() == PropertyDate.AFTER) {
                birthStart = parentBirthDate.getStart().getPointInTime(PointInTime.GREGORIAN).getJulianDay();
                birthEnd   =  Integer.MAX_VALUE;
            } else if (parentBirthDate.getFormat() == PropertyDate.TO || parentBirthDate.getFormat() == PropertyDate.BEFORE) {
                //birthStart =  Integer.MIN_VALUE;
                pit.set(parentBirthDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                birthStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();

                birthEnd   =  parentBirthDate.getStart().getJulianDay();
            } else {
                // ABOUT, ESTIMATED, CALCULATED
                PointInTime startPit = new PointInTime();
                startPit.set(parentBirthDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                birthStart = startPit.add(0, 0, -aboutYear).getJulianDay();
                PointInTime endPit = new PointInTime();
                endPit.set(parentBirthDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                birthEnd = startPit.add(0, 0, +aboutYear).getJulianDay();
            } 

            if ( birthStart > maxEnd) {
                result = false;
            } else {
                if ( minStart > birthEnd) {
                    result = false;
                } else {
                    result = true;                    
                }
            }
        } catch (GedcomException ex) {
            result = false;
        }
        return result;
    }


    /**
     * retourne true si la date de deces du parent est superieure à la date
     * du relevé
     * et si le parent est né moins de 100 ans avant la date du relevé.
     * Autrement dit :
     *   deaththDate -100   < recordDate - (minMonthShift + minYearShift) < deaththDate
     *
     *   deathDate-100ans  recordDate-minMonthShift     deathDate
     *   ----[}------------------[******]---------------[******]----  => true
     *
     *    r-100ans   recordDate-minYearShift   parentBirthDate
     *   ----[}-------- [******]-----------------[******]----------  => false
     *
     *    parentBirthDate   recordDate-100ans  recordDate-minYearShift
     *   ---[******]------------[}---------------- [******]--------  => false
     *
     * @param recordDate  date du relevé
     * @param parentBirthDate  date de naissance du parent
     * @param minDiff
     * @return true si indiFirthDate + minMonthDiff + minYearDiff <= recordBirthDate
     */
    static protected boolean isRecordBeforeThanDate(PropertyDate recordDate, PropertyDate parentDeathDate, int minMonthShift, int minYearShift) throws GedcomException {
        boolean result;
        if (recordDate == null ) {
            return true;
        } else if (!recordDate.isComparable()) {
            return true;
        }

        if (parentDeathDate == null) {
            return true;
        } else if (!parentDeathDate.isComparable()) {
            return true;
        }

        int recStart;
        int recEnd;

        if (recordDate.getFormat() == PropertyDate.DATE) {
            PointInTime pit = new PointInTime();
            pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
            recStart = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
            recEnd   = recStart;
        } else if (recordDate.getFormat() == PropertyDate.BETWEEN_AND || recordDate.getFormat() == PropertyDate.FROM_TO) {
            PointInTime pit = new PointInTime();
            pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
            recStart = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
            pit.set(recordDate.getEnd().getPointInTime(PointInTime.GREGORIAN));
            recEnd = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
        } else if (recordDate.getFormat() == PropertyDate.FROM || recordDate.getFormat() == PropertyDate.AFTER) {
            PointInTime pit = new PointInTime();
            pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
            recStart = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
            pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
            recEnd = pit.add(0, 0, +indiMaxYearOld).getJulianDay();
        } else if (recordDate.getFormat() == PropertyDate.TO || recordDate.getFormat() == PropertyDate.BEFORE) {
            PointInTime pit = new PointInTime();
            pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
            recStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
            pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
            recEnd = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
        } else {
            // ABOUT, ESTIMATED, CALCULATED
            PointInTime pit = new PointInTime();
            pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
            recStart = pit.add(0, -minMonthShift, -minYearShift-aboutYear).getJulianDay();
            pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
            recEnd = pit.add(0, -minMonthShift, -minYearShift +aboutYear).getJulianDay();
        }

        int maxStart;
        int maxEnd;

        if (parentDeathDate.getFormat() == PropertyDate.DATE) {
            PointInTime pit = new PointInTime();
            pit.set(parentDeathDate.getStart().getPointInTime(PointInTime.GREGORIAN));
            maxStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
            maxEnd = parentDeathDate.getStart().getJulianDay();
        } else if (parentDeathDate.getFormat() == PropertyDate.BETWEEN_AND || parentDeathDate.getFormat() == PropertyDate.FROM_TO) {
            PointInTime pit = new PointInTime();
            pit.set(parentDeathDate.getStart().getPointInTime(PointInTime.GREGORIAN));
            maxStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
            maxEnd = parentDeathDate.getEnd().getJulianDay();
        } else if (parentDeathDate.getFormat() == PropertyDate.FROM || parentDeathDate.getFormat() == PropertyDate.AFTER) {
            PointInTime pit = new PointInTime();
            pit.set(parentDeathDate.getStart().getPointInTime(PointInTime.GREGORIAN));
            maxStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
            pit.set(parentDeathDate.getStart().getPointInTime(PointInTime.GREGORIAN));
            maxEnd = pit.add(0, 0, +indiMaxYearOld).getJulianDay();
        } else if (parentDeathDate.getFormat() == PropertyDate.TO || parentDeathDate.getFormat() == PropertyDate.BEFORE) {
            PointInTime pit = new PointInTime();
            pit.set(parentDeathDate.getStart().getPointInTime(PointInTime.GREGORIAN));
            maxStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
            maxEnd = parentDeathDate.getStart().getJulianDay();
        } else {
            // ABOUT, ESTIMATED, CALCULATED
            PointInTime pit = new PointInTime();
            pit.set(parentDeathDate.getStart().getPointInTime(PointInTime.GREGORIAN));
            maxStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
            pit.set(parentDeathDate.getStart().getPointInTime(PointInTime.GREGORIAN));
            maxEnd = pit.add(0, 0, +aboutYear).getJulianDay();
        }

        // l'intersection des deux intervalles ne doit pas être vide.
        if ( recEnd >= maxStart && maxEnd >= recStart ) {
            result = true;
        } else {
            result = false;

        }
        return result;
    }


    /**
     *  retourne la date la plus précise entre la date du releve la date de l'entité
     *
     *  record      birth
     *  date        date    vrai si record est plus precis que birth
     *  date        BEF     vrai si record est avant birth
     *  date        AFT     vrai si record est apres birth
     *  date        range   vrai si record est dans l'intervalle
     *
     *  BEF         date    faux
     *  BEF         BEF     vrai si record est avant birth
     *  BEF         AFT     vrai si record est apres birth
     *  BEF         range   vrai si record est entièrement dans l'intervalle birth
     *
     *  AFT         date    faux
     *  AFT         BEF     vrai si record est avant birth
     *  AFT         AFT     vrai si record est apres birth
     *  AFT         range   vrai si record est dans l'intervalle
     *
     *  range       date    faux
     *  range       BEF     vrai si l'intervalle de record est avant birth
     *  range       AFT     vrai si l'intervalle de record est apres birth
     *  range       range   vrai si l'intersection des intervalles n'est pas vide

     * @param recordDate date du releve
     * @param gedcomDate  date de l'individu Gedcom
     * @return
     *      null si les dates sont incompatibles
     *      recordDate si la date du relevé est plus précise
     *      gedcomDate si la date de l'entité est plus précise
     *      mergeDate  si une intersection plus precise existe entre les deux dates
     */
    static protected PropertyDate getMostAccurateDate(PropertyDate recordDate, PropertyDate gedcomDate) {
        PropertyDate result;
        try {

            if ( !gedcomDate.isValid() ) {
                result = recordDate;
            } else if (gedcomDate.getFormat() == PropertyDate.DATE) {
                if (recordDate.getFormat() == PropertyDate.DATE) {
                    // je compare l'année , puis le mois , puis le jour
                    if ( gedcomDate.getStart().getYear() == PointInTime.UNKNOWN ) {
                        if ( recordDate.getStart().getYear() != PointInTime.UNKNOWN ) {
                           result = recordDate;
                        } else {
                           result = gedcomDate;
                        }
                    } else if ( gedcomDate.getStart().getMonth() == PointInTime.UNKNOWN ) {
                        if ( recordDate.getStart().getMonth() != PointInTime.UNKNOWN ) {
                           result = recordDate;
                        } else {
                           result = gedcomDate;
                        }
                    } else  if ( gedcomDate.getStart().getDay() == PointInTime.UNKNOWN ) {
                        if ( recordDate.getStart().getDay() != PointInTime.UNKNOWN ) {
                           result = recordDate;
                        } else {
                           result = gedcomDate;
                        }
                    } else {
                        if ( recordDate.getStart().getYear() != PointInTime.UNKNOWN
                                && recordDate.getStart().getMonth() != PointInTime.UNKNOWN
                                && recordDate.getStart().getDay() != PointInTime.UNKNOWN) {
                            if ( recordDate.getStart().getYear() == gedcomDate.getStart().getYear()
                                    && recordDate.getStart().getMonth() == gedcomDate.getStart().getMonth()
                                    && recordDate.getStart().getDay() == gedcomDate.getStart().getDay()) {
                                // dates precises egales
                                return gedcomDate;
                            } else {
                                // dates precises incompatibles
                                result = null;
                            }
                        } else {
                            result = gedcomDate;
                        }
                    }
                } else {
                    result = gedcomDate;
                }
            } else {
                int start1;
                int start2;
                int end1;
                int end2;
                boolean about1 = false;
                boolean about2 = false;
                if (recordDate.getFormat() == PropertyDate.DATE) {
                    start1 = recordDate.getStart().getJulianDay();
                    end1 = start1;
                } else if (recordDate.getFormat() == PropertyDate.BEFORE || recordDate.getFormat() == PropertyDate.TO) {
                    start1 = Integer.MIN_VALUE;
                    end1 = recordDate.getStart().getJulianDay();
                } else if (recordDate.getFormat() == PropertyDate.AFTER || recordDate.getFormat() == PropertyDate.FROM) {
                    start1 = recordDate.getStart().getJulianDay();
                    end1 = Integer.MAX_VALUE;
                } else if (recordDate.getFormat() == PropertyDate.BETWEEN_AND || recordDate.getFormat() == PropertyDate.FROM_TO ) {
                    start1 = recordDate.getStart().getJulianDay();
                    end1 = recordDate.getEnd().getJulianDay();
                } else {
                    // ABOUT, ESTIMATED, CALCULATED
                    PointInTime startPit = new PointInTime();
                    startPit.set(recordDate.getStart());
                    start1 = startPit.add(0, 0, -aboutYear).getJulianDay();
                    PointInTime endPit = new PointInTime();
                    endPit.set(recordDate.getStart());
                    end1 = startPit.add(0, 0, +aboutYear).getJulianDay();
                    about1 = true;
                } 

                if (gedcomDate.getFormat() == PropertyDate.DATE) {
                    // intervalle [start2 , start2]
                    start2 = gedcomDate.getStart().getJulianDay();
                    end2 = start2;
                } else if (gedcomDate.getFormat() == PropertyDate.BEFORE || gedcomDate.getFormat() == PropertyDate.TO) {
                    // intervalle [start2 - 100 ans , start2]
                    PointInTime startPit = new PointInTime();
                    startPit.set(gedcomDate.getStart());
                    start2 = Integer.MIN_VALUE; 
                    end2 = gedcomDate.getStart().getJulianDay();
                } else if (gedcomDate.getFormat() == PropertyDate.AFTER || gedcomDate.getFormat() == PropertyDate.FROM) {
                    start2 = gedcomDate.getStart().getJulianDay();
                    PointInTime startPit = new PointInTime();
                    startPit.set(gedcomDate.getStart());
                    end2 = Integer.MAX_VALUE;
                } else if (gedcomDate.getFormat() == PropertyDate.BETWEEN_AND || gedcomDate.getFormat() == PropertyDate.FROM_TO ) {
                    start2 = gedcomDate.getStart().getJulianDay();
                    end2 = gedcomDate.getEnd().getJulianDay();
                } else {
                    // ABOUT, ESTIMATED, CALCULATED
                    // intervalle [start2 , end2]
                    PointInTime startPit = new PointInTime();
                    startPit.set(gedcomDate.getStart());
                    start2 = startPit.add(0, 0, -aboutYear).getJulianDay();
                    PointInTime endPit = new PointInTime();
                    endPit.set(gedcomDate.getStart());
                    end2 = endPit.add(0, 0, +aboutYear).getJulianDay();
                    about2 = true;
                } 

                
                int start;
                int end;
                boolean aboutStart = false;
                boolean aboutEnd = false;
                // start = max (start1, start2)
                if( start1 > start2) {
                    start = start1;
                    aboutStart = about1;
                } else {
                    start = start2;
                    aboutStart = about2;
                }
                // end = min (end1, end2)
                if( end1 > end2) {
                    end = end2;
                    aboutEnd = about2;
                } else {
                    end = end1;
                    aboutEnd = about1;
                }

                if (start <= end) {
                    if (start == start1 && end == end1) {
                        result = recordDate;
                    } else if (start == start2 && end == end2) {
                        result = gedcomDate;
                    } else if (start != Integer.MIN_VALUE && end != Integer.MAX_VALUE) {
                        if ((aboutEnd == true && end == end2) || (aboutStart == true && start == start2)) {
                            result = gedcomDate;
                        } else {
                            result = new PropertyDate();
                            result.setValue(PropertyDate.BETWEEN_AND, toPointInTime(start), toPointInTime(end), "intersection entre la date du releve et la date du gedcom");
                        }

                    } else if (start == Integer.MIN_VALUE && end != Integer.MAX_VALUE) {
                        result = new PropertyDate();
                        result.setValue(PropertyDate.BEFORE, toPointInTime(end), null, "");
                    } else if (start != Integer.MIN_VALUE && end == Integer.MAX_VALUE) {
                        result = new PropertyDate();
                        result.setValue(PropertyDate.AFTER, toPointInTime(start), null, "");
                    } else {
                        result = null;
                    }
                } else {
                    result = null;
                }

                // je verifie si l'intervalle 1 (record) est inclus dans l'intervalle 2 (gedcom)
                /*
                 if (start1 >= start2  && end1 <= end2 ) {
                    //recordDate est inclus dans gedcomDate
                    result = recordDate;
                } else if (start1 < start2  && end1 <= end2 ) {
                    PropertyDate mergeDate = new PropertyDate();
                    mergeDate.setValue(PropertyDate.BETWEEN_AND, toPointInTime(start1), toPointInTime(end2), "intersection entre la date du releve et la date du gedcom" );
                    result = mergeDate;
                } else {
                    if( start2 == Integer.MIN_VALUE && end1 == Integer.MAX_VALUE && start1 <= end2 ) {
                        // recouvrement partiel  1=AFT et 2=BEF                        
                        if ( start1 != Integer.MIN_VALUE) {
                            PropertyDate mergeDate = new PropertyDate();
                            mergeDate.setValue(PropertyDate.BETWEEN_AND, toPointInTime(start1), toPointInTime(end2), "intersection entre la date du releve et la date du gedcom" );
                            result = mergeDate;
                        } else {
                            result = gedcomDate;
                        }
                    } else if ( start1 == Integer.MIN_VALUE && (end2 == Integer.MAX_VALUE || end2 <= end1) && start2 <= end1 ) {
                        if ( start2 != Integer.MIN_VALUE) {
                            // recouvrement partiel  1=BEF (min , end1)   et 2=AFT (start2 , max) => (start2 , end1)
                            PropertyDate mergeDate = new PropertyDate();
                            mergeDate.setValue(PropertyDate.BETWEEN_AND, toPointInTime(start2), toPointInTime(end1), "intersection entre la date du releve et la date du gedcom" );
                            result = mergeDate;
                        } else {
                            result = gedcomDate;
                        }
                    } else {
                        if( start1 <= start2 && end1 == Integer.MAX_VALUE  && end2 == Integer.MAX_VALUE ) {
                            result = gedcomDate;
                        } else {
                            // l'intersection entre les intervalles est nulle
                            // les dates sont incompatibles
                            result = null;
                        }
                    }
                }
                */
            }
        } catch (GedcomException ex) {
            result = null;
        }

        return result;
    }


     static protected PointInTime toPointInTime(int julianDay) {

        // see toJulianDay
        int l = julianDay + 68569;
        int n = (4 * l) / 146097;
        l = l - (146097 * n + 3) / 4;
        int i = (4000 * (l + 1)) / 1461001;
        l = l - (1461 * i) / 4 + 31;
        int j = (80 * l) / 2447;
        int d = l - (2447 * j) / 80;
        l = j / 11;
        int m = j + 2 - (12 * l);
        int y = 100 * (n - 49) + i + l;

        return new PointInTime(PointInTime.UNKNOWN, PointInTime.UNKNOWN, y <= 0 ? y - 1 : y);
    }


    /**
     * retourne la profession d'un individu, avec le lieu et la date
     * S'il y a plusieurs profession, retourne celle qui a la date la plus proche de
     * la date donnée en paramètre.
     * Si audune profession n'est trouvée, retourne le domicile
     * @param indi
     * @param occupationDate
     * @return occupation+residence+date or empty string
     */   
    static protected String findOccupation(Indi indi, PropertyDate occupationDate) {
        Property foundOccupation = null;
        Property foundDate = null;
        for (Property iterationOccupation : indi.getProperties("OCCU")) {
            // je recherche les dates meme si elles ne sont pas valides
            for (Property iterationDate : iterationOccupation.getProperties("DATE",false)) {
                if (foundOccupation == null) {
                    foundOccupation = iterationOccupation;
                    foundDate = iterationDate;
                } else {
                    if (Math.abs(occupationDate.compareTo((PropertyDate) iterationDate)) <= Math.abs(occupationDate.compareTo(foundDate))) {
                        foundOccupation = iterationOccupation;
                        foundDate = iterationDate;
                    }
                }

            }
        }
        String result = "";
        if (foundOccupation != null) {
            result = foundOccupation.getValue();
            Property place = foundOccupation.getProperty("PLAC");
            if ( place != null && !place.getValue().isEmpty()) {
                if (!result.isEmpty()) {
                    result += ", ";
                }
                result += place.getValue();
            }
            String date = foundOccupation.getPropertyDisplayValue("DATE");
            if (!date.isEmpty()) {
                result += " (" + date + ")";
            }
        } else {
            result = findResidence(indi, occupationDate);
        }
        return result;
    }

    /**
     * retourne le domicile d'un individu.
     * S'il y a plusieurs domiciles, retourne celui qui a la date la plus proche de
     * la date donnée en paramètre
     * @param indi
     * @param residenceDate
     * @return residence+date or empty string
     */
    static protected String findResidence(Indi indi, PropertyDate residenceDate) {
        Property foundResidence = null;
        Property foundDate = null;
        for (Property iterationResidence : indi.getProperties("RESI")) {
            // je recherche les dates meme si elles ne sont pas valides
            for (Property iterationDate : iterationResidence.getProperties("DATE",false)) {
                if (foundResidence == null) {
                    foundResidence = iterationResidence;
                    foundDate = iterationDate;
                } else {
                    if (Math.abs(residenceDate.compareTo((PropertyDate) iterationDate)) <= Math.abs(residenceDate.compareTo( foundDate))) {
                        foundResidence = iterationResidence;
                        foundDate = iterationDate;
                    }
                }
            }
        }
        String result = "";
        if (foundResidence != null) {
            result = foundResidence.getValue();
            Property place = foundResidence.getProperty("PLAC");
            if ( place != null && !place.getValue().isEmpty()) {
                if (!result.isEmpty()) {
                    result += ", ";
                }
                result += place.getValue();
            }
            String date = foundResidence.getPropertyDisplayValue("DATE");
            if (!date.isEmpty()) {
                result += " (" + date + ")";
            }
        }
        return result;
    }


     /**
     * recherche un evenement du meme type et a la meme date
     * @param indi
     * @param eventType
     * @param eventDate
     * @return evenement ou null
     */
    static protected PropertyEvent findPropertyEvent(Indi indi, String eventType, PropertyDate eventDate) {
        PropertyEvent foundEvent = null;
        //Property foundDate = null;
        for (Property iterationEvent : indi.getProperties("EVEN")) {
            if (iterationEvent.getPropertyValue("TYPE").equals(eventType)) {
                PropertyDate iterationDate =  (PropertyDate) iterationEvent.getProperty("DATE",false);
                if (iterationDate != null) {
                    if ( MergeQuery.isCompatible(eventDate, iterationDate)) {
                        foundEvent = (PropertyEvent) iterationEvent;
                        //foundDate = iterationDate;
                    }
                } else { 
                    
                    if (foundEvent == null) {
                        foundEvent = (PropertyEvent) iterationEvent;
                        //foundDate = iterationDate;
                    } 
                }
            }
        }
        
        return foundEvent;
    }

    /**
     * retourne la source d'un releve
     *   "(?:%s|%s)(?:\\s++)%s(?:\\s++)(?:BMS|Etat\\scivil)", countyName, cityCode, cityName
     * @param entityProperty
     * @param gedcom
     * @return
     */
//    static protected Source findRecordSource(MergeRecord record, Gedcom gedcom) {
//        Source source = null;
//
//        /*
//        // je verifie si la source existe deja dans le gescom
//        String cityName = record.getEventPlaceCityName();
//        String cityCode = record.getEventPlaceCityCode();
//        String countyName = record.getEventPlaceCountyName();
//        //String stringPatter = String.format("(?:%s|%s)(?:\\s++)%s(?:\\s++)(?:BMS|Etat\\scivil)", countyName, cityCode, cityName);
//        String stringPatter = String.format("(?:BMS|Etat\\scivil)(?:\\s++)%s", cityName);
//        Pattern pattern = Pattern.compile(stringPatter);
//        Collection<? extends Entity> sources = gedcom.getEntities("SOUR");
//        for (Entity gedComSource : sources) {
//            if (pattern.matcher(((Source)gedComSource).getTitle()).matches()) {
//                source = (Source)gedComSource;
//            }
//        }
//         */
//
//        String sourceTitle = record.getEventSource();
//        Collection<? extends Entity> sources = gedcom.getEntities("SOUR");
//        for (Entity gedComSource : sources) {
//
//            if (((Source)gedComSource).getTitle().startsWith(sourceTitle)) {
//                source = (Source)gedComSource;
//            }
//        }
//
//        return source;
//    }

    /**
     * retourne la source d'un evenement 
     * @param entityProperty
     * @param gedcom
     * @return
     */
//    static protected Property getEntitySourceProperty(MergeRecord record, Property eventProperty) {
//        Property sourceProperty = null;
//
//        if (eventProperty != null) {
//            Property[] sourceProperties = eventProperty.getProperties("SOUR", false);
//            for (int i = 0; i < sourceProperties.length; i++) {
//                // remarque : verification de classe PropertySource avant de faire le cast en PropertySource pour eliminer
//                // les cas anormaux , par exemple une source "multiline"
//                if ( sourceProperties[i] instanceof PropertySource) {
//                    Source eventSource = (Source) ((PropertySource) sourceProperties[i]).getTargetEntity();
//                    if (record.getEventSource().compareTo(eventSource.getTitle()) == 0) {
//                        sourceProperty = sourceProperties[i];
//                        break;
//                    }
//                }
//            }
//        }
//
//        return sourceProperty;
//    }

     /**
     * retourne la source d'un evenement
     * @param entityProperty
     * @param gedcom
     * @return
     */
//    static protected String getSourceTitle(Property sourceProperty) {
//        String sourceTitle = "";
//
//        if (sourceProperty != null) {
//             Source eventSource = (Source) ((PropertySource) sourceProperty).getTargetEntity();
//             sourceTitle = eventSource.getTitle();
//        }
//
//        return sourceTitle;
//    }


    /**
     * retourne la page de la source d'un releve
     * @param entityProperty
     * @param gedcom
     * @return page de la source , ou chaine vide si la source ou sa page n'existent pas
     */
//    static protected String getSourcePage( MergeRecord record, Property sourceProperty) {
//        String sourcePage = "";
//
//        if (sourceProperty != null) {
//            for (Property pageProperty : sourceProperty.getProperties("PAGE")) {
//                if (record.getEventPage().equals(pageProperty.getValue())) {
//                    sourcePage = pageProperty.getValue();
//                    break;
//                }
//            }
//        }
//
//        return sourcePage;
//    }

}
