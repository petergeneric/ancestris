package ancestris.modules.releve.dnd;

import ancestris.modules.releve.model.FieldSex;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertySource;
import genj.gedcom.Source;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Cette classe contient les requetes pour chercher des entités dans un gedcom
 * compatibles avec les informations d'un relevé.
 * @author Michel
 */
public class MergeQuery {

    protected static int minMarriageYearOld = 15; // age minimum pour etre marié
    protected static int minParentYearOld = 15;   // age minimum pour etre parent
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
    static protected List<Fam> findFamilyCompatibleWithIndiParents(MergeRecord record, Gedcom gedcom) throws Exception {
        List<Fam> parentFamilies = new ArrayList<Fam>();

        // j'arret la recherche si le nom des parents n'est pas renseigné
        if ( record.getIndiFatherLastName().isEmpty() && record.getIndiFatherFirstName().isEmpty()
                && record.getWifeFatherLastName().isEmpty() && record.getWifeFatherFirstName().isEmpty() ) {
            return parentFamilies;
        }

        // je recupere la date de naissance du releve
        PropertyDate recordBirthDate = record.getIndiBirthDate();
        
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
                if (!isRecordAfterThanDate(record.getIndiDeathDate(), marriageDate, 0, 0)) {
                    continue;
                }
            } 

            if (father != null) {

                // meme nom du pere
                if (!record.getIndiFatherLastName().isEmpty() ) {
                    if ( !isSameName(record.getIndiFatherLastName(), father.getLastName())) {
                        continue;
                    }
                } else {
                    // si le nom du pere est vide dans le releve, je verifie que le nom de l'individu est identique
                    // au nom du pere de la famille (pour eviter de trop nombreuses réponses sans rapport)
                    if ( !isSameName(record.getIndiLastName(), father.getLastName())) {
                        continue;
                    }
                }
                //meme prénom du pere
                if (!record.getIndiFatherFirstName().isEmpty()
                        && !isSameName(record.getIndiFatherFirstName(), father.getFirstName())) {
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
                if (!record.getIndiMotherLastName().isEmpty()
                        && !isSameName(record.getIndiMotherLastName(), mother.getLastName())) {
                    continue;
                }
                //meme prénom de la mere
                if (!record.getIndiMotherFirstName().isEmpty()
                        && !isSameName(record.getIndiMotherFirstName(), mother.getFirstName())) {
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
                PointInTime minChildPith = new PointInTime();;
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
     * Recherche les familles de de l'individu avec l'ex conjoint
     *
     * @param RecordBirth  relevé de naissance
     * @param gedcom
     * @param selectedIndi individu selectionné
     * @return Liste des fmailles
     */
    static protected List<Fam> findFamilyCompatibleWithMarried(MergeRecord record, Gedcom gedcom) throws Exception {
        List<Fam> parentFamilies = new ArrayList<Fam>();

        // j'arrete la recherche si le nom de l'ex conjoint n'est pas renseigné.
        if ( record.getIndiMarriedLastName().isEmpty() ) {
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

            if ( record.getIndiSex() == PropertySex.MALE) {
                // je verifie si l'epoux est compatible avec currentIndi

                // meme nom de l'epoux
                if (!record.getIndiLastName().isEmpty() ) {
                    if ( !isSameName(record.getIndiLastName(), husband.getLastName())) {
                        continue;
                    }
                }

                //meme prénom de l'epoux
                if (!record.getIndiFirstName().isEmpty()
                        && !isSameName(record.getIndiFirstName(), husband.getFirstName())) {
                    continue;
                }

                // le releve doit etre minMarriageYearOld années apres la naissance de l'epoux
                if (!isRecordAfterThanDate(record.getEventDate(), husband.getBirthDate(), 0, minMarriageYearOld)) {
                    continue;
                }

                // naissance de l'epoux
                if (!isCompatible(record.getIndiBirthDate(), husband.getBirthDate() )) {
                    continue;
                }

                // le releve doit etre avant le deces de l'epoux
                if (!isRecordBeforeThanDate(record.getEventDate(), husband.getDeathDate(), 0, 0)) {
                    continue;
                }

                // meme nom de l'ex epouse
                if (!record.getIndiMarriedLastName().isEmpty()
                        && !isSameName(record.getIndiMarriedLastName(), wife.getLastName())) {
                    continue;
                }
                
                //meme prénom de l'ex epouse
                if (!record.getIndiMarriedFirstName().isEmpty()
                        && !isSameName(record.getIndiMarriedFirstName(), wife.getFirstName())) {
                    continue;
                }

                // l'ex epouse doit avoir au moins minMarriageYearOld
                if (!isRecordAfterThanDate(record.getEventDate(), wife.getBirthDate(), 0, minMarriageYearOld)) {
                    continue;
                }


                 // la date de deces de l'ex conjoint doit etre compatible avec le deces de l'epoux
                if (!isCompatible(record.getIndiMarriedDeathDate(), wife.getDeathDate() )) {
                    continue;
                }
            } else {
                // je verifie si l'individu est compatible avec l'epouse

                // meme nom de l'epoux
                if (!record.getIndiLastName().isEmpty() ) {
                    if ( !isSameName(record.getIndiLastName(), wife.getLastName())) {
                        continue;
                    }
                }

                //meme prénom de l'epoux
                if (!record.getIndiFirstName().isEmpty()
                        && !isSameName(record.getIndiFirstName(), wife.getFirstName())) {
                    continue;
                }

                // le releve doit etre minMarriageYearOld années apres la naissance de l'epoux
                if (!isRecordAfterThanDate(record.getEventDate(), wife.getBirthDate(), 0, minMarriageYearOld)) {
                    continue;
                }

                // naissance de l'epoux
                if (!isCompatible(record.getIndiBirthDate(), wife.getBirthDate() )) {
                    continue;
                }

                // le releve doit etre avant le deces de l'epoux
                if (!isRecordBeforeThanDate(record.getEventDate(), wife.getDeathDate(), 0, 0)) {
                    continue;
                }


                // meme nom de l'ex epoux
                if (!record.getIndiMarriedLastName().isEmpty()
                        && !isSameName(record.getIndiMarriedLastName(), husband.getLastName())) {
                    continue;
                }

                //meme prénom de l'ex epoux
                if (!record.getIndiMarriedFirstName().isEmpty()
                        && !isSameName(record.getIndiMarriedFirstName(), husband.getFirstName())) {
                    continue;
                }

                // l'ex epoux doit avoir au moins minMarriageYearOld
                if (!isRecordAfterThanDate(record.getEventDate(), husband.getBirthDate(), 0, minMarriageYearOld)) {
                    continue;
                }


                 // la date de deces de l'ex conjoint doit etre compatible avec le deces de l'epoux
                if (!isCompatible(record.getIndiMarriedDeathDate(), husband.getDeathDate() )) {
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
     * Recherche les familles de parents compatibles avec les parents de l'individu 1 du releve
     *
     * @param RecordBirth  relevé de naissance
     * @param gedcom
     * @param selectedIndi individu selectionné
     * @return Liste des fmailles
     */
    static protected List<Fam> findFamilyCompatibleWithWifeParents(MergeRecord record, Gedcom gedcom) throws Exception {
        List<Fam> parentFamilies = new ArrayList<Fam>();

        // je recupere la date de naissance du releve
        PropertyDate recordBirthDate = record.getWifeBirthDate();

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
                if (!record.getWifeFatherLastName().isEmpty() ) {
                    if ( !isSameName(record.getWifeFatherLastName(), father.getLastName())) {
                        continue;
                    }
                } else {
                    // si le nom du pere est vide dans le releve, je verifie que le nom de l'individu est identique
                    // au nom du pere de la famille (pour eviter de trop nombreuses réponses sans rapport)
                    if ( !isSameName(record.getWifeLastName(), father.getLastName())) {
                        continue;
                    }
                }
                //meme prénom du pere
                if (!record.getWifeFatherFirstName().isEmpty()
                        && !isSameName(record.getWifeFatherFirstName(), father.getFirstName())) {
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
                if (!record.getWifeMotherLastName().isEmpty()
                        && !isSameName(record.getWifeMotherLastName(), mother.getLastName())) {
                    continue;
                }
                //meme prénom de la mere
                if (!record.getWifeMotherFirstName().isEmpty()
                        && !isSameName(record.getWifeMotherFirstName(), mother.getFirstName())) {
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

                if (!marriageRecord.getIndiFatherLastName().isEmpty() ) {
                    if ( !isSameName(marriageRecord.getIndiFatherLastName(), husband.getLastName())) {
                        continue;
                    }
                } else {
                    // si le nom du pere est vide dans le releve, je verifie que le nom du pere du gedcom est identique
                    // au nom de l'individu du releve bien qu'ne theorie le nom de l'enfant peut etre diffrent de celui du pere
                    // (ceci pour eviter de trop nombreuses réponses sans rapport)
                    if ( !isSameName(marriageRecord.getIndiLastName(), husband.getLastName())) {
                        continue;
                    }
                }

                //meme prénom de l'epoux
                if (!marriageRecord.getIndiFirstName().isEmpty()
                        && !isSameName(marriageRecord.getIndiFirstName(), husband.getFirstName())) {
                    continue;
                }
                // l'epoux doit avoir au moins minMarriageYearOld
                if (!isRecordAfterThanDate(marriageDate, husband.getBirthDate(), 0, minMarriageYearOld)) {
                    continue;
                }
                // l'epoux ne doit pas etre decede avant le mariage
                if (!isRecordBeforeThanDate(marriageDate, husband.getDeathDate(), 0, 0)) {
                    continue;
                }
                // je verifie les parents de l'epoux
                Indi indiFather = husband.getBiologicalFather();
                if (indiFather != null) {
                    // meme nom du pere de l'epoux
                    if (!marriageRecord.getIndiFatherLastName().isEmpty()
                            && !isSameName(marriageRecord.getIndiFatherLastName(), indiFather.getLastName())) {
                        continue;
                    }

                    //meme prénom du pere de l'epoux
                    if (!marriageRecord.getIndiFatherFirstName().isEmpty()
                            && !isSameName(marriageRecord.getIndiFatherFirstName(), indiFather.getFirstName())) {
                        continue;
                    }

                    // le pere doit avoir au moins minParentYearOld+minMarriageYearOld
                    if (!isRecordAfterThanDate(marriageDate, indiFather.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }

                    // le pere ne doit pas etre decede 9 mois avant la date de naissance de l'epoux
                    if (!isRecordBeforeThanDate(marriageRecord.getIndiBirthDate(), indiFather.getDeathDate(), 9, 0)) {
                        continue;
                    }
                }

                Indi indiMother = husband.getBiologicalMother();
                if (indiMother != null) {
                    // meme nom de la mere de l'epoux
                    if (!marriageRecord.getIndiMotherLastName().isEmpty()
                            && !isSameName(marriageRecord.getIndiMotherLastName(), indiMother.getLastName())) {
                        continue;
                    }

                    //meme prénom de la mere de l'epoux
                    if (!marriageRecord.getIndiMotherFirstName().isEmpty()
                            && !isSameName(marriageRecord.getIndiMotherFirstName(), indiMother.getFirstName())) {
                        continue;
                    }

                    // la mere doit avoir au moins minParentYearOld+minMarriageYearOld
                    if (!isRecordAfterThanDate(marriageDate, indiMother.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }

                    // la mere ne doit pas etre decedee avant la date de naissance
                    if (!isRecordBeforeThanDate(marriageRecord.getIndiBirthDate(), indiMother.getDeathDate(), 0, 0)) {
                        continue;
                    }
                }                
            }

            if (wife != null) {
                // meme nom de l'epouse
                if (!marriageRecord.getWifeLastName().isEmpty()
                        && !isSameName(marriageRecord.getWifeLastName(), wife.getLastName())) {
                    continue;
                }
                //meme prénom de l'epouse
                if (!marriageRecord.getWifeFirstName().isEmpty()
                        && !isSameName(marriageRecord.getWifeFirstName(), wife.getFirstName())) {
                    continue;
                }

                // l'epouse doit avoir au moins minMarriageYearOld
                if (!isRecordAfterThanDate(marriageDate, wife.getBirthDate(), 0, minMarriageYearOld)) {
                    continue;
                }

                // l'epouse ne doit pas etre decedee avant le mariage
                if (!isRecordBeforeThanDate(marriageDate, wife.getDeathDate(), 0, 0)) {
                    continue;
                }

                // je verifie les parents de l'epoux
                Indi wifeFather = wife.getBiologicalFather();
                if (wifeFather != null) {
                    // meme nom du pere de l'epouse
                    if (!marriageRecord.getWifeFatherLastName().isEmpty()
                            && !isSameName(marriageRecord.getWifeFatherLastName(), wifeFather.getLastName())) {
                        continue;
                    }

                    //meme prénom du pere de l'epouse
                    if (!marriageRecord.getWifeFatherFirstName().isEmpty()
                            && !isSameName(marriageRecord.getWifeFatherFirstName(), wifeFather.getFirstName())) {
                        continue;
                    }

                    // le pere doit etre né au moins minParentYearOld+minMarriageYearOld avant la date du mariage
                    if (!isRecordAfterThanDate(marriageDate, wifeFather.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }

                    // le pere ne doit pas etre decede 9 mois avant la date de naissance de l'epouse
                    if (!isRecordBeforeThanDate(marriageRecord.getWifeBirthDate(), wifeFather.getDeathDate(), 9, 0)) {
                        continue;
                    }
                }

                Indi wifeMother = wife.getBiologicalMother();
                if (wifeMother != null) {
                    // meme nom de la mere de l'epouse
                    if (!marriageRecord.getWifeMotherLastName().isEmpty()
                            && !isSameName(marriageRecord.getWifeMotherLastName(), wifeMother.getLastName())) {
                        continue;
                    }

                    //meme prénom de la mere de l'epouse
                    if (!marriageRecord.getWifeMotherFirstName().isEmpty()
                            && !isSameName(marriageRecord.getWifeMotherFirstName(), wifeMother.getFirstName())) {
                        continue;
                    }

                    // la mere doit avoir au moins minParentYearOld+minMarriageYearOld
                    if (!isRecordAfterThanDate(marriageDate, wifeMother.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }

                    // la mer ne doit pas etre decede avant la date de naissance de l'epouse
                    if (!isRecordBeforeThanDate(marriageRecord.getWifeBirthDate(), wifeMother.getDeathDate(), 9, 0)) {
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
    static protected void findFatherMotherCompatibleWithBirthRecord(MergeRecord record, Gedcom gedcom, List<Fam> families, List<Indi> fathers, List<Indi> mothers) throws Exception {
        // je recupere la date de naissance du releve
        PropertyDate recordBirthDate = record.getIndiBirthDate();
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
                if (!record.getIndiFatherLastName().isEmpty() ) {
                    if ( !isSameName(record.getIndiFatherLastName(), indi.getLastName())) {
                        continue;
                    }
                } else {
                    // si le nom du pere est vide dans le releve, je verifie que le nom du pere du gedcom est identique
                    // au nom de l'individu du releve (pour eviter de trop nombreuses réponses sans rapport)
                    if ( !isSameName(record.getIndiLastName(), indi.getLastName())) {
                        continue;
                    }
                }

                //  le prénom du pere ne doit pas vide et identique au nom du pere dans le releve
                if (record.getIndiFatherFirstName().isEmpty() || indi.getFirstName().isEmpty()
                        || !isSameName(record.getIndiFatherFirstName(), indi.getFirstName())) {
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
                            if ( !record.getIndiMotherLastName().isEmpty()
                                    &&!isSameName(record.getIndiMotherLastName(), wife.getLastName())
                                    && !record.getIndiMotherFirstName().isEmpty()
                                    && !isSameName(record.getIndiMotherFirstName(), wife.getFirstName() )
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
                if ( record.getIndiMotherLastName().isEmpty() || indi.getLastName().isEmpty()
                        || !isSameName(record.getIndiMotherLastName(), indi.getLastName())) {
                    continue;
                }

                //meme prénom de la mere, le prenom de la mere ne doit pas etre vide
                if (record.getIndiMotherFirstName().isEmpty() ||  !indi.getFirstName().isEmpty()
                        && !isSameName(record.getIndiMotherFirstName(), indi.getFirstName())) {
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
                         && !record.getIndiFatherLastName().isEmpty()
                         && !isSameName(record.getIndiFatherLastName(), fam.getHusband().getLastName())
                         && !record.getIndiFatherFirstName().isEmpty()
                         && !isSameName(record.getIndiFatherFirstName(), fam.getHusband().getFirstName())
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
                if (!marriageRecord.getIndiLastName().isEmpty()
                        && !isSameName(marriageRecord.getIndiLastName(), husband.getLastName())) {
                    continue;
                }
                //meme prénom de l'epoux
                if (!marriageRecord.getIndiFirstName().isEmpty()
                        && !isSameName(marriageRecord.getIndiFirstName(), husband.getFirstName())) {
                    continue;
                }
                // l'epoux doit avoir au moins minMarriageYearOld
                if (!isRecordAfterThanDate(marriageDate, husband.getBirthDate(), 0, minMarriageYearOld)) {
                    continue;
                }

                // l'epoux ne doit pas etre decede  avant le mariage
                if (!isRecordBeforeThanDate(marriageDate, husband.getDeathDate(), 0, 0)) {
                    continue;
                }

                // je verifie les parents de l'epoux
                Indi indiFather = husband.getBiologicalFather();
                if (indiFather != null) {
                    // meme nom du pere de l'epoux
                    if (!marriageRecord.getIndiFatherLastName().isEmpty()
                            && !isSameName(marriageRecord.getIndiFatherLastName(), indiFather.getLastName())) {
                        continue;
                    }

                    //meme prénom du pere de l'epoux
                    if (!marriageRecord.getIndiFatherFirstName().isEmpty()
                            && !isSameName(marriageRecord.getIndiFatherFirstName(), indiFather.getFirstName())) {
                        continue;
                    }

                    // le pere doit etre ne au moins minParentYearOld+minMarriageYearOld avant le mariage
                    if (!isRecordAfterThanDate(marriageDate, indiFather.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }
                    // le pere ne doit pas etre decede 9 mois avant la date de naissance de l'epoux
                    if (!isRecordBeforeThanDate(marriageRecord.getIndiBirthDate(), indiFather.getDeathDate(), 9, 0)) {
                        continue;
                    }
                }

                Indi indiMother = husband.getBiologicalMother();
                if (indiMother != null) {
                    // meme nom de la mere de l'epoux
                    if (!marriageRecord.getIndiMotherLastName().isEmpty()
                            && !isSameName(marriageRecord.getIndiMotherLastName(), indiMother.getLastName())) {
                        continue;
                    }

                    //meme prénom de la mere  de l'epoux
                    if (!marriageRecord.getIndiMotherFirstName().isEmpty()
                            && !isSameName(marriageRecord.getIndiMotherFirstName(), indiMother.getFirstName())) {
                        continue;
                    }

                    // la mere doit etre ne au moins minParentYearOld+minMarriageYearOld avant le mariage
                    if (!isRecordAfterThanDate(marriageDate, indiMother.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }

                    // la mere ne doit pas etre decede avant la date de naissance de l'epoux
                    if (!isRecordBeforeThanDate(marriageRecord.getIndiBirthDate(), indiFather.getDeathDate(), 0, 0)) {
                        continue;
                    }
                }

                // j'ajoute l'epoux
                husbands.add(husband);

            } else if (indi.getSex() == PropertySex.FEMALE) {
                Indi wife = indi;

                // meme nom de l'epouse
                if (!marriageRecord.getWifeLastName().isEmpty()
                        && !isSameName(marriageRecord.getWifeLastName(), wife.getLastName())) {
                    continue;
                }
                //meme prénom de l'epouse
                if (!marriageRecord.getWifeFirstName().isEmpty()
                        && !isSameName(marriageRecord.getWifeFirstName(), wife.getFirstName())) {
                    continue;
                }
                // l'epouse ne doit pas etre decedee avant  le mariage
                if (!isRecordBeforeThanDate(marriageDate, wife.getDeathDate(), 0, 0)) {
                    continue;
                }

                // je verifie les parents de l'epouse
                Indi wifeFather = wife.getBiologicalFather();
                if (wifeFather != null) {
                    // meme nom du pere de l'epouse
                    if (!marriageRecord.getWifeFatherLastName().isEmpty()
                            && !isSameName(marriageRecord.getWifeFatherLastName(), wifeFather.getLastName())) {
                        continue;
                    }

                    //meme prénom du pere de l'epouse
                    if (!marriageRecord.getWifeFatherFirstName().isEmpty()
                            && !isSameName(marriageRecord.getWifeFatherFirstName(), wifeFather.getFirstName())) {
                        continue;
                    }

                     // le pere doit etre ne au moins minParentYearOld+minMarriageYearOld avant le mariage
                    if (!isRecordAfterThanDate(marriageDate, wifeFather.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }
                    // le pere ne doit pas etre decede 9 mois avant la date de naissance de l'epouse
                    if (!isRecordBeforeThanDate(marriageRecord.getWifeBirthDate(), wifeFather.getDeathDate(), 9, 0)) {
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
                    if (!marriageRecord.getWifeMotherLastName().isEmpty()
                            && !isSameName(marriageRecord.getWifeMotherLastName(), wifeMother.getLastName())) {
                        continue;
                    }

                    //meme prénom de la mere de l'epouse
                    if (!marriageRecord.getWifeMotherFirstName().isEmpty()
                            && !isSameName(marriageRecord.getWifeMotherFirstName(), wifeMother.getFirstName())) {
                        continue;
                    }

                   // la mere doit etre ne au moins minParentYearOld+minMarriageYearOld avant le mariage
                    if (!isRecordAfterThanDate(marriageDate, wifeMother.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        continue;
                    }

                    // la mere ne doit pas etre decede avant la date de naissance de l'epouse
                    if (!isRecordBeforeThanDate(marriageRecord.getWifeBirthDate(), wifeMother.getDeathDate(), 0, 0)) {
                        continue;
                    }

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
    static protected List<Indi> findIndiCompatibleWithRecord(MergeRecord record,  Gedcom gedcom, Indi excludeIndi) throws Exception {
        List<Indi> sameIndis = new ArrayList<Indi>();

        for (Indi indi : gedcom.getIndis()) {

            // individu a exlure
            if (excludeIndi != null && excludeIndi.compareTo(indi) == 0) {
                continue;
            }

            // meme sexe de l'individu
            if (record.getIndiSex() != FieldSex.UNKNOWN
                    && indi.getSex() != PropertySex.UNKNOWN
                    && record.getIndiSex() != indi.getSex()) {
                continue;
            }

            // meme nom de l'individu
            if (!record.getIndiLastName().isEmpty()
                    && !isSameName(record.getIndiLastName(), indi.getLastName())) {
                continue;
            }

            // meme prenom de l'individu
            if (!record.getIndiFirstName().isEmpty()
                    && !isSameName(record.getIndiFirstName(), indi.getFirstName())) {
                continue;
            }

            // la date de naissance doit etre renseignee
            if (!record.getIndiBirthDate().isComparable()) {
                // j'abandonne si la date de naissance du relevé n'est pas renseignée
                continue;
            }

            // la date de naissance du relevé doit être compatible avec celle de l'individu
            // petit raccourci pour gagner du temps
            PropertyDate indiBirtDate = indi.getBirthDate();
            if (indiBirtDate != null) {
                if (!isCompatible(record.getIndiBirthDate(), indiBirtDate)) {
                    // la date de naissance de l'individu n'est pas compatible avec la date du relevé
                    continue;
                }
            }

            // la date de décès doit être compatible doit être compatible avec celle de l'individu
            // petit raccourci pour gagner du temps
            PropertyDate indiDeathDate = indi.getDeathDate();
            if (indiDeathDate != null) {
                if (!isCompatible(record.getIndiDeathDate(), indiDeathDate)) {
                    continue;
                }
            }


            Fam parentFamily = indi.getFamilyWhereBiologicalChild();
            if (parentFamily != null) {
                PropertyDate marriageDate = parentFamily.getMarriageDate();
                if (marriageDate != null) {
                    // la naissance doit être après la date mariage.
                    if (!isRecordAfterThanDate(record.getIndiBirthDate(), marriageDate, 0, 0)) {
                        continue;
                    }
                    // le deces doit être après la date mariage.
                    if (!isRecordAfterThanDate(record.getIndiDeathDate(), marriageDate, 0, 0)) {
                        continue;
                    }
                }

                Indi father = parentFamily.getHusband();
                if (father != null) {

                    // meme nom du pere
                    if (!record.getIndiFatherLastName().isEmpty()
                            && !isSameName(record.getIndiFatherLastName(), father.getLastName())) {
                        continue;
                    }
                    //meme prénom du pere
                    if (!record.getIndiFatherFirstName().isEmpty()
                            && !isSameName(record.getIndiFatherFirstName(), father.getFirstName())) {
                        continue;
                    }

                    // le pere doit avoir au moins minParentYearOld
                    if (!isRecordAfterThanDate(record.getIndiBirthDate(), father.getBirthDate(), 0, minParentYearOld)) {
                        continue;
                    }
                    // le pere ne doit pas etre decede 9 mois avant la date de naissance
                    if (!isRecordBeforeThanDate(record.getIndiBirthDate(), father.getDeathDate(), 9, 0)) {
                        continue;
                    }
                }

                Indi mother = parentFamily.getWife();
                if (mother != null) {
                    // meme nom de la mere
                    if (!record.getIndiMotherLastName().isEmpty()
                            && !isSameName(record.getIndiMotherLastName(), mother.getLastName())) {
                        continue;
                    }
                    //meme prénom de la mere
                    if (!record.getIndiMotherFirstName().isEmpty()
                            && !isSameName(record.getIndiMotherFirstName(), mother.getFirstName())) {
                        continue;
                    }
                    // la mere doit avoir au moins minParentYearOld
                    if (!isRecordAfterThanDate(record.getIndiBirthDate(), mother.getBirthDate(), 0, minParentYearOld)) {
                        continue;
                    }
                    // la mere ne doit pas etre decedee avant la date de naissance
                    if (!isRecordBeforeThanDate(record.getIndiBirthDate(), mother.getDeathDate(), 0, 0)) {
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
        PropertyDate recordBirthDate = birthRecord.getIndiBirthDate();
        
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
//                if (!birthRecord.getIndiFatherLastName().isEmpty()
//                        && !isSameName(birthRecord.getIndiFatherLastName(), father.getLastName())) {
//                    //throw new Exception("le nom du pere est different");
//                }
//                //meme prénom du pere
//                if (!birthRecord.getIndiFatherFirstName().isEmpty()
//                        && !isSameName(birthRecord.getIndiFatherFirstName(), father.getFirstName())) {
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
//                if (!birthRecord.getIndiMotherLastName().isEmpty()
//                        && !isSameName(birthRecord.getIndiMotherLastName(), mother.getLastName())) {
//                    //throw new Exception("le nom de la mere est different");
//                }
//                //meme prénom de la mere
//                if (!birthRecord.getIndiMotherFirstName().isEmpty()
//                        && !isSameName(birthRecord.getIndiMotherFirstName(), mother.getFirstName())) {
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
                if (birthRecord.getIndiSex() != FieldSex.UNKNOWN
                        && child.getSex() != PropertySex.UNKNOWN
                        && birthRecord.getIndiSex() != child.getSex()) {
                    continue;
                }

                // meme nom de l'enfant
                if (!birthRecord.getIndiLastName().isEmpty()
                        && !isSameName(birthRecord.getIndiLastName(), child.getLastName())) {
                    continue;
                }

                // meme prenom de l'enfant
                if (!birthRecord.getIndiFirstName().isEmpty()
                        && !isSameName(birthRecord.getIndiFirstName(), child.getFirstName())) {
                    continue;
                }

                
                // date de naissance compatible
                // petit raccourci pour gagner du temps
                PropertyDate indiBirtDate = child.getBirthDate();
                if (indiBirtDate != null) {
                    if (!isCompatible(recordBirthDate, indiBirtDate)) {
                        // la date de naissance de l'individu n'est pas compatible avec la date du relevé
                        continue;
                    }
                }

                // date de décés compatible
                PropertyDate childDeathDate = child.getDeathDate();
                if (childDeathDate != null) {
                    if (!isCompatible(birthRecord.getIndiDeathDate(), childDeathDate)) {
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
//   
    static protected boolean isCompatible(PropertyDate recordDate, PropertyDate entityDate) {
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
                PointInTime pit = new PointInTime();
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
            if ( recordEnd >= entityStart   ) {
               if  ( entityEnd >= recordStart) {
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
     * retounr true si str1 est égal ou se prononce comme str2
     * @param str1
     * @param str2
     * @return
     */
    static protected boolean isSameName(String str1, String str2) {        
        return dm.encode(str1).equals(dm.encode(str2));
        //return str1.equals(str2);
    }

    /**
     * retourne true si la date de naissance du parent est inférieure à la date
     * du relevé (diminuée de l'age minimum pour être parent)
     * et si le parent a moins de 100 ans à la date du relevé.
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
     *  retourne vrai si la date de naissance du releve est plus precise
     *  que la date de naissance de l'entité
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
     * @param birthDate date de naissance
     * @return
     */
    static protected boolean isBestBirthDate(PropertyDate recordDate, PropertyDate birthDate) {
        boolean result;
        try {
            if ( !birthDate.isValid() ) {
                result = true;
            } else if (birthDate.getFormat() == PropertyDate.DATE) {
                if (recordDate.getFormat() == PropertyDate.DATE) {
                    // je compare l'année , puis le mois , puis le jour
                    if ( birthDate.getStart().getYear() == PointInTime.UNKNOWN ) {
                        if ( recordDate.getStart().getYear() != PointInTime.UNKNOWN ) {
                           result = true;
                        } else {
                           result = false;
                        }
                    } else if ( birthDate.getStart().getMonth() == PointInTime.UNKNOWN ) {
                        if ( recordDate.getStart().getMonth() != PointInTime.UNKNOWN ) {
                           result = true;
                        } else {
                           result = false;
                        }
                    } else  if ( birthDate.getStart().getDay() == PointInTime.UNKNOWN ) {
                        if ( recordDate.getStart().getDay() != PointInTime.UNKNOWN ) {
                           result = true;
                        } else {
                           result = false;
                        }
                    } else {
                        result = false;
                    }
                } else {
                    result = false;
                }
            } else {
                int start1;
                int start2;
                int end1;
                int end2;
                if (recordDate.getFormat() == PropertyDate.DATE) {
                    start1 = recordDate.getStart().getJulianDay();
                    end1 = start1;
                } else if (recordDate.getFormat() == PropertyDate.BEFORE || recordDate.getFormat() == PropertyDate.TO) {
                    start1 = Integer.MIN_VALUE;
                    end1 = recordDate.getStart().getJulianDay();
                } else if (recordDate.getFormat() == PropertyDate.AFTER || recordDate.getFormat() == PropertyDate.FROM) {
                    start1 = recordDate.getStart().getJulianDay();
                    end1 = Integer.MAX_VALUE;
                } else {
                    // ABOUT, ESTIMATED, CALCULATED
                    PointInTime startPit = new PointInTime();
                    startPit.set(recordDate.getStart());
                    start1 = startPit.add(0, 0, -aboutYear).getJulianDay();
                    PointInTime endPit = new PointInTime();
                    endPit.set(recordDate.getStart());
                    end1 = startPit.add(0, 0, +aboutYear).getJulianDay();
                } 

                if (birthDate.getFormat() == PropertyDate.DATE) {
                    // intervalle [start2 , start2]
                    start2 = birthDate.getStart().getJulianDay();
                    end2 = start2;
                } else if (birthDate.getFormat() == PropertyDate.BEFORE || birthDate.getFormat() == PropertyDate.TO) {
                    // intervalle [start2 - 100 ans , start2]
                    PointInTime startPit = new PointInTime();
                    startPit.set(birthDate.getStart());
                    start2 = Integer.MIN_VALUE; 
                    end2 = birthDate.getStart().getJulianDay();
                } else if (birthDate.getFormat() == PropertyDate.AFTER || birthDate.getFormat() == PropertyDate.FROM) {
                    start2 = birthDate.getStart().getJulianDay();
                    PointInTime startPit = new PointInTime();
                    startPit.set(birthDate.getStart());
                    end2 = Integer.MAX_VALUE;
                } else {
                    // ABOUT, ESTIMATED, CALCULATED
                    // intervalle [start2 , end2]
                    PointInTime startPit = new PointInTime();
                    startPit.set(birthDate.getStart());
                    start2 = startPit.add(0, 0, -aboutYear).getJulianDay();
                    PointInTime endPit = new PointInTime();
                    endPit.set(birthDate.getStart());
                    end2 = startPit.add(0, 0, +aboutYear).getJulianDay();
                } 

                // je verifie si l'intervalle 1 est inclus dans l'intervalle 2
                if (start1 >= start2  && end1 <= end2 ) {
                    result = true;
                } else {
                    // l'intersection entre les intervalles est nulle
                    result = false;
                }
            }
        } catch (GedcomException ex) {
            result = false;
        }

        return result;
    }



    /**
     * retourne la profession d'un individu.
     * S'il ya plusieurs profession, retourne celle qui a la date la plus proche de
     * la date donnée en paramètre
     * @param indi
     * @param occupation
     * @param occupationDate
     * @return occution property or null
     */   
    static protected String findOccupation(Indi indi, PropertyDate occupationDate) {
        Property occupationProperty = null;
        for (Property occu : indi.getProperties("OCCU")) {
            for (Property occuDate : occu.getProperties("DATE")) {
                if (occupationProperty == null) {
                    occupationProperty = occu;
                } else {
                    if (Math.abs(occupationDate.compareTo((PropertyDate) occuDate)) <= Math.abs(occupationDate.compareTo( occupationDate))) {
                        occupationProperty = occu;
                    }
                }

            }
        }
        String result = "";
        if (occupationProperty != null) {
            result = occupationProperty.getValue();
            String date = occupationProperty.getPropertyDisplayValue("DATE");
            if (!date.isEmpty()) {
                result += " (" + date + ")";
            }
        }
        return result;
    }

    /**
     * retourne la source d'un releve
     * @param entityProperty
     * @param gedcom
     * @return
     */
    static protected Source findSource(MergeRecord record, Gedcom gedcom) {
        Source source = null;

        // je verifie si la source existe deja dans le gescom
        String cityName = record.getEventPlaceCityName();
        String cityCode = record.getEventPlaceCityCode();
        String countyName = record.getEventPlaceCountyName();
        String stringPatter = String.format("(?:%s|%s)(?:\\s++)%s(?:\\s++)(?:BMS|Etat\\scivil)", countyName, cityCode, cityName);
        Pattern pattern = Pattern.compile(stringPatter);
        Collection<? extends Entity> sources = gedcom.getEntities("SOUR");
        for (Entity gedComSource : sources) {
            if (pattern.matcher(((Source)gedComSource).getTitle()).matches()) {
                source = (Source)gedComSource;
            }
        }

        return source;
    }

    /**
     * retourne la source d'un evenement 
     * @param entityProperty
     * @param gedcom
     * @return
     */
    static protected Property findSource(MergeRecord record, Gedcom gedcom, Property eventProperty) {
        Property sourceProperty = null;

        if (eventProperty != null) {
            Property[] sourceProperties = eventProperty.getProperties("SOUR", false);
            for (int i = 0; i < sourceProperties.length; i++) {
                Source eventSource = (Source) ((PropertySource) sourceProperties[i]).getTargetEntity();
                if (record.getEventSource().compareTo(eventSource.getTitle()) == 0) {
                    sourceProperty = sourceProperties[i];
                    break;
                }
            }
        }

        return sourceProperty;
    }

     /**
     * retourne la source d'un evenement
     * @param entityProperty
     * @param gedcom
     * @return
     */
    static protected String findSourceTitle(Property sourceProperty, Gedcom gedcom) {
        String sourceTitle = "";

        if (sourceProperty != null) {
             Source eventSource = (Source) ((PropertySource) sourceProperty).getTargetEntity();
             sourceTitle = eventSource.getTitle();
        }

        return sourceTitle;
    }


    /**
     * retourne la page de la source d'un releve
     * @param entityProperty
     * @param gedcom
     * @return page de la source , ou chaine vide si la source ou sa page n'existent pas
     */
    static protected String findSourcePage( MergeRecord record, Property sourceProperty, Gedcom gedcom) {
        String sourcePage = "";

        if (sourceProperty != null) {
            for (Property pageProperty : sourceProperty.getProperties("PAGE")) {
                if (record.getEventPage().equals(pageProperty.getValue())) {
                    sourcePage = pageProperty.getValue();
                    break;
                }
            }
        }

        return sourcePage;
    }

}
