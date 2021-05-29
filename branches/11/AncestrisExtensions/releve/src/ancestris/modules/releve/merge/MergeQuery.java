package ancestris.modules.releve.merge;

import static ancestris.modules.releve.merge.MergeLogger.ACCEPT;
import static ancestris.modules.releve.merge.MergeLogger.LOG;
import static ancestris.modules.releve.merge.MergeLogger.REFUSE;
import static ancestris.modules.releve.merge.MergeLogger.getAccept;
import static ancestris.modules.releve.merge.MergeLogger.getRefuse;
import ancestris.modules.releve.merge.MergeRecord.RecordMarried;
import ancestris.modules.releve.merge.MergeRecord.RecordParent;
import ancestris.modules.releve.merge.MergeRecord.RecordParticipant;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

/**
 * Cette classe contient les requetes pour chercher des entités dans un gedcom
 * compatibles avec les informations d'un relevé.
 * @author Michel
 */
public class MergeQuery {

    protected static int minMarriageYearOld = 18; // age minimum pour etre marié
    protected static int minParentYearOld = 18;   // age minimum pour etre parent
    protected static int minMajorityYearOld = 18;   // age minimum de majorité
    protected static int indiMaxYearOld = 100;    // age maximum d'un individu
    protected static int aboutYear = 5;           // marge d'incertitude ( date ABOUT ou ESTMATED ou CALCULATED)
    protected static int maxParentYearOld = 60;    // age maximum d'un parent

    private static final DoubleMetaphone dm = new DoubleMetaphone();
    static {
        dm.setMaxCodeLen(5);

    }

    /**
     * Recherche les familles de parents compatibles avec les parents de l'individu du releve
     *
     * @param RecordBirth  relevé de naissance
     * @param gedcom
     * @param selectedIndi individu selectionné
     * @return Liste des fmailles
     */
    static protected List<Fam> findFamilyCompatibleWithParticipantParents(MergeRecord record, RecordParticipant participant, Gedcom gedcom) throws Exception {
        if (LOG.isLoggable(Level.FINER)){
            LOG.entering(MergeQuery.class.getName(), "findFamilyCompatibleWithParticipantParents", participant.getParticipantType());
        }
        List<Fam> parentFamilies = new ArrayList<Fam>();

        RecordParent mergeFather = participant.getFather();
        RecordParent mergeMother = participant.getMother();

        // j'arrete la recherche si aucun nom et prenom des parents de l'individu n'est renseigné
        if (mergeFather.getLastName().isEmpty() && mergeFather.getFirstName().isEmpty()
                && mergeMother.getLastName().isEmpty() && mergeMother.getFirstName().isEmpty()) {
            if (LOG.isLoggable(Level.FINER)){
                LOG.exiting(MergeQuery.class.getName(), "findFamilyCompatibleWithParticipantParents", parentFamilies);
            }
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
                if (LOG.isLoggable(REFUSE)){
                    LOG.log(getRefuse("%s father == null && mother == null", fam) );
                }
                continue;
            }

            // La naissance et le deces doivent être après la date mariage des parents
            PropertyDate marriageDate = fam.getMarriageDate();
            if (marriageDate != null && marriageDate.isComparable()) {
                // la naissance doit être après la date mariage des parents
                if (!isRecordAfterThanDate(recordBirthDate, marriageDate, 0, 0)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(getRefuse("%s birth¨%s must be after marriage %s",
                                fam, recordBirthDate, marriageDate ));
                    }
                    continue;
                }

                // le deces doit être après la date mariage des parents
                if (!isRecordAfterThanDate(participant.getDeathDate(), marriageDate, 0, 0)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(getRefuse("%s death¨%s must be after parent mariage %s",
                                fam, participant.getDeathDate() , marriageDate));
                    }
                    continue;
                }
            }

            if (father != null) {
                // meme nom du pere
                if (!mergeFather.getLastName().isEmpty()) {
                    if (!isSameLastName(mergeFather.getLastName(), father.getLastName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeFather.getLastName()¨%s must be equal father.getLastName() %s %s",
                                    fam, mergeFather.getLastName(), father, father.getLastName()));
                        }
                        continue;
                    }
                } else {
                    // si le nom du pere est vide dans le releve, je verifie que le nom de l'individu est identique
                    // au nom du pere de la famille (pour eviter de trop nombreuses réponses sans rapport)
                    if (!isSameLastName(participant.getLastName(), father.getLastName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s participant.getLastName() %s must be equal father.getLastName() %s %s",
                                    fam, participant.getLastName(), father, father.getLastName()));
                        }
                        continue;
                    }
                }
                //meme prénom du pere
                if (!mergeFather.getFirstName().isEmpty()
                        && !father.getFirstName().isEmpty()
                        && !isSameFirstName(mergeFather.getFirstName(), father.getFirstName())) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(getRefuse("%s mergeFather.getFirstName() %s must be equal father.getFirstName() %s %s",
                                fam, mergeFather.getFirstName(), father, father.getFirstName()));
                    }
                    continue;
                }

                // naissance du pere
                if (record.getRecordType() == MergeRecord.RecordType.BIRTH) {
                    // la naissance doit etre au moins minParentYearOld apres la naissance du pere
                    if (!isRecordAfterThanDate(recordBirthDate, father.getBirthDate(), 0, minParentYearOld)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s recordBirthDate %s must be after father.getBirthDate() %s %s + %dy",
                                    fam, recordBirthDate, father, father.getBirthDate(), minParentYearOld ));
                        }
                        continue;
                    }
                } else if (record.getRecordType() == MergeRecord.RecordType.MARRIAGE) {
                    // l'évènement doit être au moins minParentYearOld+minMarriageYearOld après la naissance du pere
                    if (!isRecordAfterThanDate(record.getEventDate(), father.getBirthDate(), 0, minParentYearOld+minMarriageYearOld)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s record.getEventDate %s must be after father.getBirthDate() %s %s + %dy",
                                    fam, record.getEventDate(), father, father.getBirthDate(), minParentYearOld + minMarriageYearOld ));
                        }
                        continue;
                    }
                }

                // le pere ne doit pas etre decede 9 mois avant la date de naissance
                if (!isRecordBeforeThanDate(recordBirthDate, father.getDeathDate(), 9, 0)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(getRefuse("%s recordBirthDate %s must be before father.getDeathDate() %s %s - %d month",
                                fam, recordBirthDate, father, father.getDeathDate(), 9 ));
                    }
                    continue;
                }
            }

            if (mother != null) {
                // meme nom de la mere
                if (!mergeMother.getLastName().isEmpty()
                        && !isSameLastName(mergeMother.getLastName(), mother.getLastName())) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(getRefuse("%s mergeMother.getLastName()¨%s must be same as mother.getLastName() %s %s",
                                fam, mergeMother.getLastName(), mother, mother.getLastName()));
                    }
                    continue;
                }
                //meme prénom de la mere
                if (!mergeMother.getFirstName().isEmpty()
                        && !mother.getFirstName().isEmpty()
                        && !isSameFirstName(mergeMother.getFirstName(), mother.getFirstName())) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(getRefuse("%s mergeMother.getFirstName()¨%s must be same as mother.getFirstName() %s %s",
                                fam, mergeMother.getFirstName(), mother, mother.getFirstName()));
                    }
                    continue;
                }

                // naissance de la mere
                if (record.getRecordType() == MergeRecord.RecordType.BIRTH) {
                    // la naissance doit etre au moins minParentYearOld apres la naissance de la mere
                    if (!isRecordAfterThanDate(recordBirthDate, mother.getBirthDate(), 0, minParentYearOld)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s recordBirthDate¨%s must be after mother.getBirthDate() %s %s + %dy",
                                    fam, recordBirthDate, mother, mother.getBirthDate(), minParentYearOld ));
                        }
                        continue;
                    }
                } else if (record.getRecordType() == MergeRecord.RecordType.MARRIAGE) {
                    // le mariage doit être au moins  minParentYearOld+minMarriageYearOld la naissance de la mere
                    if (!isRecordAfterThanDate(record.getEventDate(), mother.getBirthDate(), 0, minParentYearOld + minMarriageYearOld)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s record.getEventDate()¨%s must be after mother.getBirthDate() %s %s + %dy",
                                    fam, record.getEventDate(), mother, mother.getBirthDate(), minParentYearOld + minMarriageYearOld ));
                        }
                        continue;
                    }
                }

                // la mere ne doit pas etre decedee avant la date de naissance
                if (!isRecordBeforeThanDate(recordBirthDate, mother.getDeathDate(), 0, 0)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(getRefuse("%s recordBirthDate %s must be before mother.getDeathDate() %s %s ",
                                fam, recordBirthDate, mother, mother.getDeathDate()));
                    }
                    continue;
                }
            }

            //il doit y avoir moins de maxParentYearOld - minMarriageYearOld  entre la naissance des enfants
            //  cela permet d'avoir une borne inférieure pour la date de naissance.
            Indi[] children = fam.getChildren();
            boolean foundChild = false;
            for (int i = 0; i < children.length && foundChild == false; i++) {
                PropertyDate childBirth = children[i].getBirthDate();
                PointInTime maxChildPith = new PointInTime();
                PointInTime minChildPith = new PointInTime();
                if (childBirth != null && childBirth.isComparable()) {
                    if (childBirth.getFormat() == PropertyDate.DATE) {
                        minChildPith.set(childBirth.getStart());
                        maxChildPith.set(childBirth.getStart());
                    } else if (childBirth.getFormat() == PropertyDate.BETWEEN_AND) {
                        minChildPith.set(childBirth.getStart());
                        maxChildPith.set(childBirth.getEnd());
                    } else if (childBirth.getFormat() == PropertyDate.AFTER || childBirth.getFormat() == PropertyDate.FROM) {
                        minChildPith.set(recordBirthDate.getStart());
                        maxChildPith = childBirth.getStart();
                    } else if (childBirth.getFormat() == PropertyDate.BEFORE || childBirth.getFormat() == PropertyDate.TO) {
                        minChildPith.set(childBirth.getStart());
                        maxChildPith.set(recordBirthDate.getStart());
                    } else {
                        minChildPith.set(childBirth.getStart());
                        minChildPith.add(0, 0, -aboutYear);
                        maxChildPith.set(childBirth.getStart());
                        maxChildPith.add(0, 0, +aboutYear);
                    }

                    Delta delta = Delta.get(maxChildPith, recordBirthDate.getStart(), PointInTime.GREGORIAN);
                    if (delta.getYears() > maxParentYearOld - minMarriageYearOld) {
                        foundChild = true;
                    }

                    delta = Delta.get(recordBirthDate.getStart(), maxChildPith, PointInTime.GREGORIAN);
                    if (delta.getYears() > maxParentYearOld - minMarriageYearOld) {
                        foundChild = true;
                    }
                }
            }
            if (foundChild) {
                if (LOG.isLoggable(REFUSE)){
                    LOG.log(getRefuse("%s children found before %s",
                            fam, recordBirthDate ));
                }
                continue;
            }

            // j'ajoute la famille dans la liste résultat si elle n'y est pas déjà
            if (!parentFamilies.contains(fam)) {
                parentFamilies.add(fam);
            } else {
                if (LOG.isLoggable(REFUSE)){
                    LOG.log(getRefuse("%s already exists in parentFamilies",
                            fam ));
                }
            }
        }

        if (LOG.isLoggable(Level.FINER)){
            StringBuilder result = new StringBuilder("RETURN");
            for(Fam fam : parentFamilies) {
                result.append(" ").append(fam.getId());
            }
            LOG.finer(result.toString());
        }

        return parentFamilies;
    }

    /**
     * Recherche les familles de l'individu avec l'ex conjoint
     *
     * @param RecordBirth relevé de naissance
     * @param gedcom
     * @param selectedIndi individu selectionné
     * @return Liste des fmailles
     */
    static protected List<SpouseFamily> findFamilyCompatibleWithParticipantMarried(MergeRecord record, RecordParticipant participant, Gedcom gedcom) throws Exception {
        if (LOG.isLoggable(Level.FINER)){
            LOG.entering(MergeQuery.class.getName(), "findFamilyCompatibleWithParticipantMarried", participant.getParticipantType());
        }

        List<SpouseFamily> marriedFamilies = new ArrayList<SpouseFamily>();

        RecordMarried mergeMarried = participant.getMarriedFamily().getMarried();
        // j'arrete la recherche si le nom de l'ex conjoint n'est pas renseigné.
        if (mergeMarried.getLastName().isEmpty()) {
            if (LOG.isLoggable(Level.FINER)){
                LOG.exiting(MergeQuery.class.getName(), "findFamilyCompatibleWithParticipantMarried", marriedFamilies);
            }
            return marriedFamilies;
        }

        // Je recherche une famille avec un pere et une mere qui portent le même nom
        // et dont les dates de naissance, de deces et de mariage sont compatibles
        for (Fam fam : gedcom.getFamilies()) {
            SpouseFamily spouse;

            Indi husband = fam.getHusband();
            Indi wife = fam.getWife();

            if (husband == null || wife == null) {
                continue;
            }

            if (participant.getSex() == PropertySex.MALE) {
                // je verifie si l'epoux est compatible avec currentIndi

                // meme nom de l'epoux
                if (!participant.getLastName().isEmpty()) {
                    if (!isSameLastName(participant.getLastName(), husband.getLastName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(REFUSE,  String.format("REFUSE %s participant.getLastName() %s must be same as husband.getLastName() %s %s",
                                    fam, participant.getLastName(), husband, husband.getLastName() ));
                        }
                        continue;
                    }
                }

                //meme prénom de l'epoux
                if (!participant.getFirstName().isEmpty()
                        && !isSameFirstName(participant.getFirstName(), husband.getFirstName())) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s participant.getFirstName() %s must be same as husband.getFirstName() %s %s",
                                fam, participant.getFirstName(), husband, husband.getFirstName() ));
                    }
                    continue;
                }

                // le releve doit etre minMarriageYearOld années apres la naissance de l'epoux
                if (!isRecordAfterThanDate(record.getEventDate(), husband.getBirthDate(), 0, minMarriageYearOld)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s record.getEventDate() %s must be after husband.getBirthDate() %s %s + %dy",
                                fam, record.getEventDate(), husband, husband.getBirthDate(), minMarriageYearOld ));
                    }
                    continue;
                }

                // naissance de l'epoux
                if (!isCompatible(participant.getBirthDate(), husband.getBirthDate(), 1)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s participant.getBirthDate() %s must be compatible with husband.getBirthDate() %s %s",
                                fam, participant.getBirthDate(), husband, husband.getBirthDate() ));
                    }
                    continue;
                }

                // le releve doit etre avant le deces de l'epoux
                if (!isRecordBeforeThanDate(record.getEventDate(), husband.getDeathDate(), 0, 0)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s record.getEventDate() %s must be before husband.getDeathDate() %s %s",
                                fam, record.getEventDate(), husband, husband.getDeathDate() ));
                    }
                    continue;
                }

                // meme nom de l'ex epouse
                if (!mergeMarried.getLastName().isEmpty()
                        && !isSameLastName(mergeMarried.getLastName(), wife.getLastName())) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s mergeMarried.getLastName() %s must be same as wife.getLastName() %s %s",
                                fam, mergeMarried.getLastName(), wife, wife.getLastName() ));
                    }
                    continue;
                }

                // meme prénom de l'ex epouse
                if (!mergeMarried.getFirstName().isEmpty()
                        && !isSameFirstName(mergeMarried.getFirstName(), wife.getFirstName())) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s mergeMarried.getFirstName() %s must be same as wife.getFirstName() %s %s",
                                fam, mergeMarried.getFirstName(), wife, wife.getFirstName() ));
                    }
                    continue;
                }

                // le releve doit etre apres le mariage (=naissance + minMarriageYearOld)
                if (!isRecordAfterThanDate(record.getEventDate(), wife.getBirthDate(), 0, minMarriageYearOld)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s record.getEventDate() %s must after wife.getBirthDate() %s %s + %dy",
                                fam, record.getEventDate(), wife, wife.getBirthDate(), minMarriageYearOld ));
                    }
                    continue;
                }

                // le releve doit etre apres la date de mariage
                if (!isRecordAfterThanDate(record.getEventDate(), fam.getMarriageDate(), 0, 0)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s record.getEventDate() %s must after fam.getMarriageDate() %s",
                                fam, record.getEventDate(), fam.getMarriageDate() ));
                    }
                    continue;
                }

                // l'ex epouse doit avoir au moins minMarriageYearOld
                if (!isRecordAfterThanDate(record.getEventDate(), wife.getBirthDate(), 0, minMarriageYearOld)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s record.getEventDate() %s must after wife.getBirthDate() %s %s + %dy",
                                fam, record.getEventDate(), wife, wife.getBirthDate(), minMarriageYearOld ));
                    }
                    continue;
                }

                // la date de deces de l'ex conjoint doit etre compatible avec le deces de l'ex époux
                if (!isCompatible(mergeMarried.getDeathDate(), wife.getDeathDate(), 1)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s mergeMarried.getDeathDate() %s must compatible with wife.getDeathDate() %s %s ",
                                fam, mergeMarried.getDeathDate(), wife, wife.getDeathDate() ));
                    }
                    continue;
                }

                // si l'epouse est decedee dans le gedcom avant la date du relevé,
                // alors la date deces du releve doit être aussi avant la date du releve
                if (isRecordAfterThanDate(record.getEventDate(), wife.getDeathDate(), 0, 0)) {
                    if (!isRecordAfterThanDate(record.getEventDate(), mergeMarried.getDeathDate(), 0, 0)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(REFUSE,  String.format("REFUSE %s record.getEventDate() %s must be after mergeMarried.getDeathDate() %s (married dead)",
                                    fam, record.getEventDate(), mergeMarried.getDeathDate() ));
                        }
                        continue;
                    }
                } else {
                    // si l'epouse n'est pas decedee dans le gedcom, le releve doit etre avant son deces
                    if (!isRecordBeforeThanDate(record.getEventDate(), mergeMarried.getDeathDate(), 0, 0)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(REFUSE,  String.format("REFUSE %s record.getEventDate() %s must be before mergeMarried.getDeathDate() %s (married alive)",
                                    fam, record.getEventDate(), mergeMarried.getDeathDate() ));
                        }
                        continue;
                    }
                }

                spouse = new SpouseFamily(fam, SpouseTag.HUSB);
            } else {
                // je verifie si l'individu est compatible avec l'epouse

                // meme nom de l'epoux
                if (!participant.getLastName().isEmpty()) {
                    if (!isSameLastName(participant.getLastName(), wife.getLastName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(REFUSE,  String.format("REFUSE %s participant.getLastName() %s must be same as wife.getLastName() %s %s",
                                    fam, participant.getLastName(), wife, wife.getLastName()));
                        }
                        continue;
                    }
                }

                //meme prénom de l'epoux
                if (!participant.getFirstName().isEmpty()
                        && !isSameFirstName(participant.getFirstName(), wife.getFirstName())) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s participant.getFirstName() %s must be same as wife.getFirstName() %s %s",
                                fam, participant.getFirstName(), wife, wife.getFirstName()));
                    }
                    continue;
                }

                // le releve doit etre minMarriageYearOld années apres la naissance de l'epouse
                if (!isRecordAfterThanDate(record.getEventDate(), wife.getBirthDate(), 0, minMarriageYearOld)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s record.getEventDate() %s must be after wife.getBirthDate() %s %s + %dy",
                                fam, record.getEventDate(), wife, wife.getBirthDate(), minMarriageYearOld ));
                    }
                    continue;
                }

                // naissance de l'epouse
                if (!isCompatible(participant.getBirthDate(), wife.getBirthDate(), 1)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s participant.getBirthDate() %s must be compatible with wife.getBirthDate() %s %s",
                                fam, participant.getBirthDate(), wife, wife.getBirthDate() ));
                    }
                    continue;
                }

                // le releve doit etre avant le deces de l'epouse
                if (!isRecordBeforeThanDate(record.getEventDate(), wife.getDeathDate(), 0, 0)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s record.getEventDate() %s must be before wife.getDeathDate() %s %s",
                                fam, record.getEventDate(), wife, wife.getDeathDate() ));
                    }
                    continue;
                }

                // meme nom de l'ex epoux
                if (!mergeMarried.getLastName().isEmpty()
                        && !isSameLastName(mergeMarried.getLastName(), husband.getLastName())) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s mergeMarried.getLastName() %s must be same as husband.getLastName() %s %s",
                                fam, mergeMarried.getLastName(), wife, husband.getLastName() ));
                    }
                    continue;
                }

                //meme prénom de l'ex epoux
                if (!mergeMarried.getFirstName().isEmpty()
                        && !isSameFirstName(mergeMarried.getFirstName(), husband.getFirstName())) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s mergeMarried.getFirstName() %s must be same as husband.getFirstName() %s %s",
                                fam, mergeMarried.getFirstName(), wife, husband.getFirstName() ));
                    }
                    continue;
                }

                // le releve doit etre apres le mariage (=naissance de l'ex conjoint + minMarriageYearOld)
                if (!isRecordAfterThanDate(record.getEventDate(), husband.getBirthDate(), 0, minMarriageYearOld)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s record.getEventDate() %s must after husband.getBirthDate() %s %s + %dy",
                                fam, record.getEventDate(), husband, husband.getBirthDate(), minMarriageYearOld ));
                    }
                    continue;
                }

                // le releve doit etre apres la date de mariage
                if (!isRecordAfterThanDate(record.getEventDate(), fam.getMarriageDate(), 0, 0)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s record.getEventDate() %s must after fam.getMarriageDate() %s",
                                fam, record.getEventDate(), fam.getMarriageDate() ));
                    }
                    continue;
                }

                // l'ex epoux doit avoir au moins minMarriageYearOld
                if (!isRecordAfterThanDate(record.getEventDate(), husband.getBirthDate(), 0, minMarriageYearOld)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s record.getEventDate() %s must after husband.getBirthDate() %s %s + %dy",
                                fam, record.getEventDate(), husband, husband.getBirthDate(), minMarriageYearOld ));
                    }
                    continue;
                }

                // la date de deces de l'ex conjoint doit etre compatible avec le deces de l'ex epoux
                if (!isCompatible(mergeMarried.getDeathDate(), husband.getDeathDate(), 1)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s mergeMarried.getDeathDate() %s must compatible with husband.getDeathDate() %s %s ",
                                fam, mergeMarried.getDeathDate(), husband, husband.getDeathDate() ));
                    }
                    continue;
                }

                // si l'epoux est decede dans le gedcom avant le releve,
                // alors la date deces du releve doit être aussi avant la date du releve
                if (isRecordAfterThanDate(record.getEventDate(), husband.getDeathDate(), 0, 0)) {
                    if (!isRecordAfterThanDate(record.getEventDate(), mergeMarried.getDeathDate(), 0, 0)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(REFUSE,  String.format("REFUSE %s record.getEventDate() %s must be after mergeMarried.getDeathDate() %s (married dead)",
                                    fam, record.getEventDate(), mergeMarried.getDeathDate() ));
                        }
                        continue;
                    }
                } else {
                    // si l'epoux n'est pas decede dans le gedcom, le releve doit etre avant son deces
                    if (!isRecordBeforeThanDate(record.getEventDate(), mergeMarried.getDeathDate(), 0, 0)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(REFUSE,  String.format("REFUSE %s record.getEventDate() %s must be before mergeMarried.getDeathDate() %s (married alive)",
                                    fam, record.getEventDate(), mergeMarried.getDeathDate() ));
                        }
                        continue;
                    }
                }
                spouse = new SpouseFamily(fam, SpouseTag.WIFE);
            }

            // j'ajoute la famille dans la liste résultat si elle n'y est pas déjà
            if (!marriedFamilies.contains(spouse)) {
                marriedFamilies.add(spouse);
            }
        }

        if (LOG.isLoggable(Level.FINER)){
            StringBuilder result = new StringBuilder("RETURN marriedFamilies");
            for(SpouseFamily fam : marriedFamilies) {
                result.append(" ").append(fam.family.getId() + " "+ fam.tag);
            }
            LOG.finer(result.toString());
            //LOG.exiting(MergeQuery.class.getName(), "findFamilyCompatibleWithParticipantMarried");
        }

        return marriedFamilies;
    }

    /**
     * Recherche les familles compatibles avec le releve de mariage Si
     * selectedFamily n'est pas nul , la famille selectionnée est ajoutée
     * d'office dans la réponse.
     *
     * @param marriageRecord relevé de mariage
     * @param gedcom gedcom
     * @param gedcom selectedFamily
     * @return liste des familles compatibles avec le relevé.
     *
     */
    static protected List<Fam> findFamilyCompatibleWithMarriageRecord(MergeRecord marriageRecord, Gedcom gedcom, Fam selectedFamily) throws Exception {
        if (LOG.isLoggable(Level.FINER)){
            LOG.entering(MergeQuery.class.getName(), "findFamilyCompatibleWithMarriageRecord");
        }
        List<Fam> families = new ArrayList<Fam>();

        if (selectedFamily != null) {
            families.add(selectedFamily);
            if (LOG.isLoggable(ACCEPT)){
                LOG.log(getAccept("%s selectedFamily", selectedFamily) );
            }
        }

        // je recupere la date de mariage du releve
        PropertyDate marriageDate = marriageRecord.getEventDate();

        // 1) je recherche une famille avec un mari et une femme qui portent le même nom
        //     et dont les dates de naissance, de deces et de mariage sont compatibles
        for (Fam fam : gedcom.getFamilies()) {
            Indi husband = fam.getHusband();
            Indi wife = fam.getWife();

            if (husband == null && wife == null) {
                if (LOG.isLoggable(REFUSE)){
                    LOG.log(getRefuse("%s father == null && mother == null", fam) );
                }
                continue;
            }

            if (husband != null) {
                RecordParent mergeFather = marriageRecord.getIndi().getFather();
                if (!mergeFather.getLastName().isEmpty()) {
                    if (!isSameLastName(mergeFather.getLastName(), husband.getLastName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeFather.getLastName()¨%s must be same as husband.getLastName() %s %s",
                                    fam, mergeFather.getLastName() , husband, husband.getLastName()));
                        }
                        continue;
                    }
                } else {
                    // si le nom du pere est vide dans le releve, je verifie que le nom du pere du gedcom est identique
                    // au nom de l'individu du releve bien qu'ne theorie le nom de l'enfant peut etre diffrent de celui du pere
                    // (ceci pour eviter de trop nombreuses réponses sans rapport)
                    if (!isSameLastName(marriageRecord.getIndi().getLastName(), husband.getLastName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s marriageRecord.getIndi().getLastName() %s must be same as husband.getLastName() %s %s",
                                    fam, marriageRecord.getIndi().getLastName(), husband, husband.getLastName()));
                        }
                        continue;
                    }
                }

                //meme prénom de l'epoux
                if (!marriageRecord.getIndi().getFirstName().isEmpty()
                        && !isSameFirstName(marriageRecord.getIndi().getFirstName(), husband.getFirstName())) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(getRefuse("%s marriageRecord.getIndi().getFirstName() %s must be same as  husband.getFirstName() %s %s",
                                fam, marriageRecord.getIndi().getFirstName(), husband, husband.getFirstName() ));
                    }
                    continue;
                }

                // l'epoux doit avoir une date de naissance compatible
                if (!isCompatible(marriageRecord.getIndi().getBirthDate(), husband.getBirthDate(), 1)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(getRefuse("%s marriageRecord.getIndi().getBirthDate() %s must be compatible with husband.getBirthDate() %s %s",
                                fam, marriageRecord.getIndi().getBirthDate(), husband, husband.getBirthDate() ));
                    }
                    continue;
                }

                // si la date de naissance de l'individu n'est pas precisée , l'epoux doit avoir au moins minMarriageYearOld
                if (!marriageRecord.getIndi().getBirthDate().isValid()) {
                    if (!isRecordAfterThanDate(marriageDate, husband.getBirthDate(), 0, minMarriageYearOld)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s marriageDate %s must be before husband.getBirthDate() %s %s + %dy",
                                    fam, marriageDate, husband, husband.getBirthDate(), minMarriageYearOld ));
                        }
                        continue;
                    }
                }

                // l'epoux ne doit pas etre decede avant le mariage
                if (!isRecordBeforeThanDate(marriageDate, husband.getDeathDate(), 0, 0)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(getRefuse("%s marriageDate %s must be before husband.getDeathDate() %s %s",
                                fam, marriageDate, husband, husband.getDeathDate() ));
                    }
                    continue;
                }

                // l'epoux doit avoir une date de deces compatible
                if (!isCompatible(marriageRecord.getIndi().getDeathDate(), husband.getDeathDate(), 1)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(getRefuse("%s marriageRecord.getIndi().getDeathDate() %s must be compatible with husband.getDeathDate() %s %s",
                                fam, marriageRecord.getIndi().getDeathDate(), husband, husband.getDeathDate() ));
                    }
                    continue;
                }

                // je verifie les parents de l'epoux
                Indi indiFather = husband.getBiologicalFather();
                if (indiFather != null) {
                    // meme nom du pere de l'epoux
                    if (!mergeFather.getLastName().isEmpty()
                            && !isSameLastName(mergeFather.getLastName(), indiFather.getLastName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeFather.getLastName()¨%s must be same as indiFather.getLastName() %s %s",
                                    fam, mergeFather.getLastName(), indiFather, indiFather.getLastName() ));
                        }
                        continue;
                    }

                    //meme prénom du pere de l'epoux
                    if (!mergeFather.getFirstName().isEmpty()
                            && !isSameFirstName(mergeFather.getFirstName(), indiFather.getFirstName() )) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeFather.getFirstName()¨%s must be same as indiFather.getFirstName() %s %s",
                                    fam, mergeFather.getFirstName(), indiFather, indiFather.getFirstName() ));
                        }
                        continue;
                    }

                    // le pere doit avoir au moins minParentYearOld+minMarriageYearOld
                    if (!isRecordAfterThanDate(marriageDate, indiFather.getBirthDate(), 0, minParentYearOld + minMarriageYearOld)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s marriageDate %s must be after indiFather.getBirthDate() %s  %s + %dy",
                                    fam, marriageDate, indiFather, indiFather.getBirthDate(), minParentYearOld + minMarriageYearOld));
                        }
                        continue;
                    }

                    // le pere ne doit pas etre decede 9 mois avant la date de naissance de l'epoux
                    if (!isRecordBeforeThanDate(marriageRecord.getIndi().getBirthDate(), indiFather.getDeathDate(), 9, 0)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s marriageRecord.getIndi().getBirthDate() %s must be before indiFather.getDeathDate()  %s + %d month",
                                    fam, marriageRecord.getIndi().getBirthDate(), indiFather.getDeathDate(), 9 ));
                        }
                        continue;
                    }
                }

                Indi indiMother = husband.getBiologicalMother();
                RecordParent mergeMother = marriageRecord.getIndi().getMother();
                if (indiMother != null) {
                    // meme nom de la mere de l'epoux
                    if (!mergeMother.getLastName().isEmpty()
                            && !isSameLastName(mergeMother.getLastName(), indiMother.getLastName() )) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeMother.getLastName()¨%s must be same as indiMother.getLastName() %s %s",
                                    fam, mergeMother.getLastName() , indiMother, indiMother.getLastName() ));
                        }
                        continue;
                    }

                    //meme prénom de la mere de l'epoux
                    if (!mergeMother.getFirstName().isEmpty()
                            && !isSameFirstName(mergeMother.getFirstName(), indiMother.getFirstName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeMother.getFirstName()¨%s must be same as indiMother.getFirstName() %s %s",
                                    fam, mergeMother.getFirstName() , indiMother, indiMother.getFirstName() ));
                        }
                        continue;
                    }

                    // la mere doit etre ne au moins minParentYearOld+minMarriageYearOld avant le mariage
                    if (!isRecordAfterThanDate(marriageDate, indiMother.getBirthDate(), 0, minParentYearOld + minMarriageYearOld)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s marriageDate¨%s must be after indiMother.getBirthDate() %s %s + %dy",
                                    fam, marriageDate, indiMother, indiMother.getBirthDate(), minParentYearOld + minMarriageYearOld ));
                        }
                        continue;
                    }

                    // la mere ne doit pas etre decedee avant la date de naissance
                    if (!isRecordBeforeThanDate(marriageRecord.getIndi().getBirthDate(), indiMother.getDeathDate(), 0, 0)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s marriageRecord.getIndi().getBirthDate()¨%s must be before indiMother.getDeathDate() %s %s",
                                    fam, marriageRecord.getIndi().getBirthDate(), indiMother, indiMother.getDeathDate() ));
                        }
                        continue;
                    }
                }
            }

            if (wife != null) {
                // meme nom de l'epouse
                if (!marriageRecord.getWife().getLastName().isEmpty()
                        && !isSameLastName(marriageRecord.getWife().getLastName(), wife.getLastName())) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s marriageRecord.getWife().getLastName() %s must be same as wife.getLastName() %s %s",
                                fam, marriageRecord.getWife().getLastName(), wife, wife.getLastName() ));
                    }
                    continue;
                }
                //meme prénom de l'epouse
                if (!marriageRecord.getWife().getFirstName().isEmpty()
                        && !isSameFirstName(marriageRecord.getWife().getFirstName(), wife.getFirstName())) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s marriageRecord.getWife().getFirstName() %s must be same as wife.getFirstName() %s %s",
                                fam, marriageRecord.getWife().getFirstName(), wife, wife.getFirstName() ));
                    }
                    continue;
                }

                // l'epouse doit avoir une date de naissance compatible
                if (!isCompatible(marriageRecord.getWife().getBirthDate(), wife.getBirthDate(), 1)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s marriageRecord.getWife().getBirthDate() %s must compatible with wife.getBirthDate() %s %s ",
                                fam, marriageRecord.getWife().getBirthDate(), wife, wife.getBirthDate() ));
                    }
                    continue;
                }

                // si la date de naissance de la femme n'est pas precisée, l'epouse doit avoir au moins minMarriageYearOld
                if (!marriageRecord.getWife().getBirthDate().isValid()) {
                    if (!isRecordAfterThanDate(marriageDate, wife.getBirthDate(), 0, minMarriageYearOld)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(REFUSE,  String.format("REFUSE %s marriageDate %s must be after wife.getBirthDate() %s %s + %dy",
                                    fam, marriageDate, wife, wife.getBirthDate(), minMarriageYearOld ));
                        }
                        continue;
                    }
                }

                // l'epouse ne doit pas etre decedee avant le mariage
                if (!isRecordBeforeThanDate(marriageDate, wife.getDeathDate(), 0, 0)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s marriageDate %s must be before wife.getDeathDate() %s %s",
                                fam, marriageDate, wife, wife.getDeathDate() ));
                    }
                    continue;
                }

                // l'epouse doit avoir une date de deces compatible
                if (!isCompatible(marriageRecord.getWife().getDeathDate(), wife.getDeathDate(), 1)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s marriageRecord.getWife().getDeathDate() %s must be compatible with wife.getDeathDate() %s %s",
                                fam, marriageRecord.getWife().getDeathDate(), wife, wife.getDeathDate() ));
                    }
                    continue;
                }

                // je verifie les parents de l'epoux
                Indi wifeFather = wife.getBiologicalFather();
                if (wifeFather != null) {
                    RecordParent mergeWifeFather = marriageRecord.getWife().getFather();
                    // meme nom du pere de l'epouse
                    if (!mergeWifeFather.getLastName().isEmpty()
                            && !isSameLastName(mergeWifeFather.getLastName(), wifeFather.getLastName() )) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeWifeFather.getLastName()¨%s must be same as wifeFather.getLastName() %s %s",
                                    fam, mergeWifeFather.getLastName() , wifeFather, wifeFather.getLastName() ));
                        }
                        continue;
                    }

                    //meme prénom du pere de l'epouse
                    if (!mergeWifeFather.getFirstName().isEmpty()
                            && !isSameFirstName(mergeWifeFather.getFirstName(), wifeFather.getFirstName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeWifeFather.getFirstName()¨%s must be same as wifeFather.getFirstName() %s %s",
                                    fam, mergeWifeFather.getFirstName() , wifeFather, wifeFather.getFirstName() ));
                        }
                        continue;
                    }

                    // le pere doit etre né au moins minParentYearOld+minMarriageYearOld avant la date du mariage
                    if (!isRecordAfterThanDate(marriageDate, wifeFather.getBirthDate(), 0, minParentYearOld + minMarriageYearOld)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s marriageDate¨%s must be after wifeFather.getBirthDate() %s %s + %dy",
                                    fam, marriageDate, wifeFather, wifeFather.getBirthDate(), minParentYearOld + minMarriageYearOld ));
                        }
                        continue;
                    }

                    // le pere ne doit pas etre decede 9 mois avant la date de naissance de l'epouse
                    if (!isRecordBeforeThanDate(marriageRecord.getWife().getBirthDate(), wifeFather.getDeathDate(), 9, 0)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s marriageRecord.getWife().getBirthDate()¨%s must be before wifeFather.getDeathDate() %s %s -%d month",
                                    fam, marriageRecord.getWife().getBirthDate(), wifeFather, wifeFather.getDeathDate(), 9 ));
                        }
                        continue;
                    }
                }

                Indi wifeMother = wife.getBiologicalMother();
                if (wifeMother != null) {
                    RecordParent mergeWifeMother = marriageRecord.getWife().getMother();
                    // meme nom de la mere de l'epouse
                    if (!mergeWifeMother.getLastName().isEmpty()
                            && !isSameLastName(mergeWifeMother.getLastName(), wifeMother.getLastName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeWifeMother.getLastName()¨%s must be same as wifeMother.getLastName() %s %s",
                                    fam, mergeWifeMother.getLastName() , wifeMother, wifeMother.getLastName() ));
                        }
                        continue;
                    }

                    //meme prénom de la mere de l'epouse
                    if (!mergeWifeMother.getFirstName().isEmpty()
                            && !isSameFirstName(mergeWifeMother.getFirstName(), wifeMother.getFirstName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeWifeMother.getFirstName()¨%s must be same as wifeMother.getFirstName() %s %s",
                                    fam, mergeWifeMother.getFirstName() , wifeMother, wifeMother.getFirstName() ));
                        }
                        continue;
                    }

                    // la mere doit etre ne au moins minParentYearOld+minMarriageYearOld avant le mariage
                    if (!isRecordAfterThanDate(marriageDate, wifeMother.getBirthDate(), 0, minParentYearOld + minMarriageYearOld)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s marriageDate¨%s must be after wifeMother.getBirthDate() %s %s + %dy",
                                    fam, marriageDate, wifeMother, wifeMother.getBirthDate(), minParentYearOld + minMarriageYearOld ));
                        }
                        continue;
                    }

                    // la mere ne doit pas etre decede avant la date de naissance de l'epouse
                    if (!isRecordBeforeThanDate(marriageRecord.getWife().getBirthDate(), wifeMother.getDeathDate(), 9, 0)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s marriageRecord.getWife().getBirthDate()¨%s must be before wifeMother.getDeathDate() %s %s -%d month",
                                    fam, marriageRecord.getWife().getBirthDate(), wifeMother, wifeMother.getDeathDate(), 9 ));
                        }
                        continue;
                    }
                }
            }

            // j'ajoute la famille dans la liste résultat si elle n'y est pas déjà
            if (!families.contains(fam)) {
                families.add(fam);
            }
        }

        if (LOG.isLoggable(Level.FINER)){
            StringBuilder result = new StringBuilder("RETURN families");
            for(Fam fam : families) {
                result.append(" ").append(fam.getId());
            }
            LOG.finer(result.toString());
        }

        return families;
    }

    /**
     * Recherche les individus correspondant aux parents du releve de naissance
     * mais qui ne sont pas mariés ensemble dans une des familles de la liste
     * "families"
     *
     * @param record relevé de naissance
     * @param gedcom
     * @param excludedFamilies familles des individus a exclure de la réponse
     * @param fathers (OUT) liste des hommes qui pourraient être le père
     * @param mothers (OUT) liste des femmes qui pourraient être la mère
     */
    static protected void findFatherMotherCompatibleWithBirthParticipant(MergeRecord record, Gedcom gedcom, List<Fam> excludedFamilies, List<Indi> fathers, List<Indi> mothers) throws Exception {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.entering(MergeQuery.class.getName(), "findFatherMotherCompatibleWithBirthParticipant");
        }
        // je recupere la date de naissance du releve
        PropertyDate recordBirthDate = record.getIndi().getBirthDate();
        if (!recordBirthDate.isComparable()) {
            recordBirthDate = record.getEventDate();
        }

        RecordParticipant participant = record.getIndi();

        Collection<Indi> entities = gedcom.getIndis();
        for (Indi parent : entities) {
            if (excludedFamilies != null) {
                boolean found = false;
                for (Fam fam : excludedFamilies) {
                    Indi husband = fam.getHusband();
                    Indi wife = fam.getWife();

                    if ((husband != null && parent.compareTo(husband) == 0)
                            || (wife != null && parent.compareTo(wife) == 0)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    continue;
                }
            }

            RecordParent mergeFather = participant.getFather();
            RecordParent mergeMother = participant.getMother();

            if (parent.getSex() == PropertySex.MALE) {
                Indi father = parent;
                // meme nom du pere
                if (!mergeFather.getLastName().isEmpty()) {
                    if (!isSameLastName(mergeFather.getLastName(), father.getLastName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(REFUSE,  String.format("REFUSE %s participant.getLastName() %s must be same as father.getLastName() %s %s",
                                    father, participant.getLastName(), father, father.getLastName() ));
                        }
                        continue;
                    }
                } else {
                    // si le nom du pere est vide dans le releve, je verifie que le nom du pere du gedcom est identique
                    // au nom de l'individu du releve (pour eviter de trop nombreuses réponses sans rapport)
                    if (!isSameLastName(participant.getLastName(), father.getLastName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s participant.getLastName()¨%s must be same as father.getLastName() %s %s",
                                    father, participant.getLastName(), father, father.getLastName()));
                        }
                        continue;
                    }
                }

                //  le prénom du pere ne doit pas vide et identique au nom du pere dans le releve
                if (mergeFather.getFirstName().isEmpty() || father.getFirstName().isEmpty()
                        || !isSameFirstName(mergeFather.getFirstName(), father.getFirstName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeFather.getFirstName()¨%s must be same as father.getFirstName() %s %s",
                                    father, mergeFather.getFirstName(), father, father.getFirstName()));
                        }
                    continue;
                }

                // le pere doit avoir au moins minParentYearOld
                if (!isRecordAfterThanDate(recordBirthDate, father.getBirthDate(), 0, minParentYearOld)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s recordBirthDate %s must be after father.getBirthDate() %s %s + %dy",
                                father, recordBirthDate, father, father.getBirthDate(), minMarriageYearOld ));
                    }
                    continue;
                }

                // le pere ne doit pas etre decede 9 mois avant la date de naissance
                if (!isRecordBeforeThanDate(recordBirthDate, father.getDeathDate(), 9, 0)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s recordBirthDate %s must be before father.getDeathDate() %s %s + 9m",
                                father, recordBirthDate, father, father.getDeathDate() ));
                    }
                    continue;
                }

                // Le pere ne doit pas etre deja marié avec une autre personne avant la date du releve
                // et ne doit pas avoir un enfant avant la date du releve et un autre apres la date du releve
                Fam[] fams = father.getFamiliesWhereSpouse();
                boolean incompatible = false;
                for (Fam fam : fams) {
                    PropertyDate marriageDate = fam.getMarriageDate();
                    Indi[] children = fam.getChildren(true);  // sorted = true
                    PropertyDate firstChildBirthDate = null;
                    PropertyDate lastChildBirthDate = null;
                    if (children.length > 0) {
                        firstChildBirthDate = children[0].getBirthDate();
                        lastChildBirthDate = children[children.length - 1].getBirthDate();
                    }
                    if (!isRecordBeforeThanDate(recordBirthDate, marriageDate, 0, 0)
                            && !isRecordBeforeThanDate(recordBirthDate, firstChildBirthDate, 0, 0)
                            && !isRecordAfterThanDate(recordBirthDate, lastChildBirthDate, 0, 0)) {
                        Indi wife = fam.getWife();
                        if (wife != null) {
                            if (!mergeMother.getLastName().isEmpty()
                                    && !isSameLastName(mergeMother.getLastName(), wife.getLastName())
                                    && !mergeMother.getFirstName().isEmpty()
                                    && !isSameFirstName(mergeMother.getFirstName(), wife.getFirstName())) {
                                incompatible = true;
                                if (LOG.isLoggable(REFUSE)){
                                    LOG.log(REFUSE,  String.format("REFUSE %s father must not have child with other spouse at %s ; but found family %s with spouse %s and first child birth %s and last child birth %s",
                                            father, recordBirthDate, fam, wife, firstChildBirthDate, lastChildBirthDate ));
                                }
                                break;

                            }
                        }
                    }
                }
                if (incompatible) {
                    continue;
                }

                fathers.add(father);

            } else if (parent.getSex() == PropertySex.FEMALE) {
                Indi mother = parent;

                // meme nom de la mere , le nom de la mere ne doit pas être vide
                if (mergeMother.getLastName().isEmpty() || mother.getLastName().isEmpty()
                        || !isSameLastName(mergeMother.getLastName(), mother.getLastName())) {
                    if (LOG.isLoggable(REFUSE)) {
                        LOG.log(getRefuse("%s mergeMother.getLastName() %s must be same as mother.getLastName() %s %s",
                                mother, mergeMother.getLastName(), mother, mother.getLastName()));
                    }
                    continue;
                }

                //meme prénom de la mere, le prenom de la mere ne doit pas etre vide
                if (mergeMother.getFirstName().isEmpty() || !mother.getFirstName().isEmpty()
                        && !isSameFirstName(mergeMother.getFirstName(), mother.getFirstName())) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s mergeMother.getFirstName() %s must be same as mother.getFirstName() %s %s",
                                mother, mergeMother.getFirstName(), mother, mother.getFirstName() ));
                    }
                    continue;
                }

                // la mere doit avoir au moins minParentYearOld
                if (!isRecordAfterThanDate(recordBirthDate, mother.getBirthDate(), 0, minParentYearOld)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s recordBirthDate %s must be after mother.getBirthDate() %s %s + %dy",
                                mother, recordBirthDate, mother, mother.getBirthDate(), minMarriageYearOld ));
                    }
                    continue;
                }

                // la mere ne doit pas etre decedee avant la date de naissance
                if (!isRecordBeforeThanDate(recordBirthDate, mother.getDeathDate(), 0, 0)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s recordBirthDate %s must be before mother.getDeathDate() %s %s",
                                mother, recordBirthDate, mother, mother.getDeathDate() ));
                    }
                    continue;
                }

                // La mere ne doit pas etre deja mariée avec une autre personne avant la date de la naissance
                // et avoir au moins un enfant apres la date du releve avec cette autre personne
                // ou
                // elle ne doit pas avoir un enfant avant la date du releve et un autre enfant après la date du releve de naissance
                Fam[] fams = mother.getFamiliesWhereSpouse();
                boolean incompatible = false;
                for (Fam fam : fams) {
                    PropertyDate marriageDate = fam.getMarriageDate();
                    Indi[] children = fam.getChildren(true);
                    PropertyDate firstChildBirthDate = null;
                    PropertyDate lastChildBirthDate = null;
                    if (children.length > 0) {
                        firstChildBirthDate = children[0].getBirthDate();
                        lastChildBirthDate = children[children.length - 1].getBirthDate();
                    }
                    if (!isRecordBeforeThanDate(recordBirthDate, marriageDate, 0, 0)
                            && !isRecordBeforeThanDate(recordBirthDate, firstChildBirthDate, 0, 0)
                            && !isRecordAfterThanDate(recordBirthDate, lastChildBirthDate, 0, 0)
                            && !mergeFather.getLastName().isEmpty()
                            && !isSameLastName(mergeFather.getLastName(), fam.getHusband().getLastName())
                            && !mergeFather.getFirstName().isEmpty()
                            && !isSameFirstName(mergeFather.getFirstName(), fam.getHusband().getFirstName())) {
                        incompatible = true;
                        if (LOG.isLoggable(REFUSE)) {
                            LOG.log(REFUSE, String.format("REFUSE %s mother must not have child with other spouse at %s ; but found family %s with spouse %s and first child birth %s and last child birth %s",
                                    mother, recordBirthDate, fam, fam.getHusband(), firstChildBirthDate, lastChildBirthDate));
                        }
                        break;
                    }
                }
                if (incompatible) {
                    continue;
                }

                mothers.add(parent);
            }
        }

        if (LOG.isLoggable(Level.FINER)){
            StringBuilder result = new StringBuilder("RETURN fathers");
            for(Indi father : fathers) {
                result.append(" ").append(father.getId());
            }
            result.append(" mothers ");
            for(Indi mother : mothers) {
                result.append(" ").append(mother.getId());
            }
            LOG.finer(result.toString());
        }

    }

    /**
     * recherche les individus qui sont compatibles avec les epoux du releve de
     * mariage et qui ne font pas partie des familles excludedFamilies
     *
     * @param marriageRecord releve de mariage
     * @param gedcom
     * @param excludedFamilies
     * @param husbands liste des pères (résultat de la recherche)
     * @param wifes liste des mères (résultat de la recherche)
     * @return
     */
    static protected void findHusbanWifeCompatibleWithMarriageRecord(MergeRecord marriageRecord, Gedcom gedcom, List<Fam> excludedFamilies, List<Indi> husbands, List<Indi> wifes) throws Exception {
        if (LOG.isLoggable(Level.FINER)){
            LOG.entering(MergeQuery.class.getName(), "findHusbanWifeCompatibleWithMarriageRecord");
        }
        // je recupere la date de mariage du releve
        PropertyDate marriageDate = marriageRecord.getEventDate();

        Collection<Indi> entities = gedcom.getIndis();
        for (Indi indi : entities) {
            if (excludedFamilies != null) {
                boolean found = false;
                for (Fam fam : excludedFamilies) {
                    Indi husband = fam.getHusband();
                    Indi wife = fam.getWife();
                    // je compare les ID s'il n'est pas null
                    if ((husband != null && indi.compareTo(husband) == 0)
                            || (wife != null && indi.compareTo(wife) == 0)) {
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
                        && !isSameLastName(marriageRecord.getIndi().getLastName(), husband.getLastName() )) {
                    if (LOG.isLoggable(REFUSE)) {
                        LOG.log(getRefuse("%s marriageRecord.getIndi().getLastName() %s must be same as husband.getLastName() %s %s",
                                husband, marriageRecord.getIndi().getLastName(), husband, husband.getLastName() ));
                    }
                    continue;
                }
                //meme prénom de l'epoux
                if (!marriageRecord.getIndi().getFirstName().isEmpty()
                        && !isSameFirstName(marriageRecord.getIndi().getFirstName(), husband.getFirstName() )) {
                    if (LOG.isLoggable(REFUSE)) {
                        LOG.log(getRefuse("%s marriageRecord.getIndi().getFirstName() %s must be same as husband.getFirstName() %s %s",
                                husband, marriageRecord.getIndi().getFirstName(), husband, husband.getFirstName() ));
                    }
                    continue;
                }

                // l'epoux doit avoir une date de naissance compatible
                if (!isCompatible(marriageRecord.getIndi().getBirthDate(), husband.getBirthDate(), 1)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s marriageRecord.getIndi().getBirthDate() %s must be compatible with husband.getBirthDate() %s %s",
                                husband, marriageRecord.getIndi().getBirthDate(), husband, husband.getBirthDate() ));
                    }
                    continue;
                }

                // si la date de naissance de l'individu n'est pas precisée , l'epoux doit avoir au moins minMarriageYearOld
                if (!marriageRecord.getIndi().getBirthDate().isValid()) {
                    if (!isRecordAfterThanDate(marriageDate, husband.getBirthDate(), 0, minMarriageYearOld)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s marriageDate¨%s must be after husband.getBirthDate() %s %s + %dy",
                                    husband, marriageDate, husband, husband.getBirthDate(), minParentYearOld + minMarriageYearOld ));
                        }
                        continue;
                    }
                }

                // l'epoux ne doit pas etre decede avant le mariage
                if (!isRecordBeforeThanDate(marriageDate, husband.getDeathDate(), 0, 0)) {
                    if (LOG.isLoggable(REFUSE)) {
                        LOG.log(getRefuse("%s marriageDate¨%s must be before husband.getDeathDate() %s %s",
                                husband, marriageDate, husband, husband.getDeathDate()));
                    }
                    continue;
                }

                // l'epoux doit avoir une date de deces compatible
                if (!isCompatible(marriageRecord.getIndi().getDeathDate(), husband.getDeathDate(), 1)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s marriageRecord.getIndi().getDeathDate() %s must be compatible with husband.getDeathDate() %s %s",
                                husband, marriageRecord.getIndi().getDeathDate(), husband, husband.getDeathDate() ));
                    }
                    continue;
                }

                // je verifie les parents de l'epoux
                Indi indiFather = husband.getBiologicalFather();
                if (indiFather != null) {
                    RecordParent mergeFather = marriageRecord.getIndi().getFather();
                    // meme nom du pere de l'epoux
                    if (!mergeFather.getLastName().isEmpty()
                            && !isSameLastName(mergeFather.getLastName(), indiFather.getLastName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeFather.getLastName()¨%s must be same as indiFather.getLastName() %s %s",
                                    husband, mergeFather.getLastName(), indiFather, indiFather.getLastName() ));
                        }
                        continue;
                    }

                    //meme prénom du pere de l'epoux
                    if (!mergeFather.getFirstName().isEmpty()
                            && !isSameFirstName(mergeFather.getFirstName(), indiFather.getFirstName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeFather.getFirstName()¨%s must be same as indiFather.getFirstName() %s %s",
                                    husband, mergeFather.getFirstName(), indiFather, indiFather.getFirstName() ));
                        }
                        continue;
                    }

                    // le pere doit etre ne au moins minParentYearOld+minMarriageYearOld avant le mariage
                    if (!isRecordAfterThanDate(marriageDate, indiFather.getBirthDate(), 0, minParentYearOld + minMarriageYearOld)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s marriageDate %s must be after indiFather.getBirthDate() %s  %s + %dy",
                                    husband, marriageDate, indiFather, indiFather.getBirthDate(), minParentYearOld + minMarriageYearOld));
                        }
                        continue;
                    }
                    // le pere ne doit pas etre decede 9 mois avant la date de naissance de l'epoux
                    if (!isRecordBeforeThanDate(marriageRecord.getIndi().getBirthDate(), indiFather.getDeathDate(), 9, 0)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s marriageRecord.getIndi().getBirthDate() %s must be before indiFather.getDeathDate()  %s + %d month",
                                    husband, marriageRecord.getIndi().getBirthDate(), indiFather.getDeathDate(), 9 ));
                        }
                        continue;
                    }
                }

                Indi indiMother = husband.getBiologicalMother();
                if (indiMother != null) {
                    RecordParent mergeMother = marriageRecord.getIndi().getMother();
                    // meme nom de la mere de l'epoux
                    if (!mergeMother.getLastName().isEmpty()
                            && !isSameLastName(mergeMother.getLastName(), indiMother.getLastName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeMother.getLastName()¨%s must be same as indiMother.getLastName() %s %s",
                                    husband, mergeMother.getLastName(), indiMother, indiMother.getLastName() ));
                        }
                        continue;
                    }

                    //meme prénom de la mere  de l'epoux
                    if (!mergeMother.getFirstName().isEmpty()
                            && !isSameFirstName(mergeMother.getFirstName(), indiMother.getFirstName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeMother.getFirstName()¨%s must be same as indiMother.getFirstName() %s %s",
                                    husband, mergeMother.getFirstName(), indiMother, indiMother.getFirstName() ));
                        }
                        continue;
                    }

                    // la mere doit etre ne au moins minParentYearOld+minMarriageYearOld avant le mariage
                    if (!isRecordAfterThanDate(marriageDate, indiMother.getBirthDate(), 0, minParentYearOld + minMarriageYearOld)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s marriageDate¨%s must be after indiMother.getBirthDate() %s %s + %dy",
                                    husband, marriageDate, indiMother, indiMother.getBirthDate(), minParentYearOld + minMarriageYearOld ));
                        }
                        continue;
                    }

                    // la mere ne doit pas etre decede avant la date de naissance de l'epoux
                    if (!isRecordBeforeThanDate(marriageRecord.getIndi().getBirthDate(), indiMother.getDeathDate(), 0, 0)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s marriageRecord.getIndi().getBirthDate()¨%s must be before indiMother.getDeathDate() %s %s",
                                    husband, marriageRecord.getIndi().getBirthDate(), indiMother, indiMother.getDeathDate() ));
                        }
                        continue;
                    }
                }

                // je verifie s'il a un conjoint en vie avec un nom différent et un prenom different
                boolean oftherSpouseFound = false;
                Fam[] spouseFamList = husband.getFamiliesWhereSpouse();
                for (Fam fam : spouseFamList) {
                    Indi wife = fam.getWife();

                    if (!(isSameLastName(marriageRecord.getWife().getLastName(), wife.getLastName())
                            && isSameLastName(marriageRecord.getWife().getFirstName(), wife.getFirstName()))) {

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
                if (oftherSpouseFound) {
                    if (LOG.isLoggable(REFUSE)) {
                        LOG.log(getRefuse("%s husband must not have spouse with other name",
                                husband ));
                    }
                    continue;
                }

                // j'ajoute l'epoux
                husbands.add(husband);

            } else if (indi.getSex() == PropertySex.FEMALE) {
                Indi wife = indi;

                // meme nom de l'epouse
                if (!marriageRecord.getWife().getLastName().isEmpty()
                        && !isSameLastName(marriageRecord.getWife().getLastName(), wife.getLastName())) {
                    if (LOG.isLoggable(REFUSE)) {
                        LOG.log(getRefuse("%s marriageRecord.getWife().getLastName() %s must be same as wife.getLastName() %s %s",
                                wife, marriageRecord.getWife().getLastName(), wife, wife.getLastName()));
                    }
                    continue;
                }
                //meme prénom de l'epouse
                if (!marriageRecord.getWife().getFirstName().isEmpty()
                        && !isSameFirstName(marriageRecord.getWife().getFirstName(), wife.getFirstName())) {
                    if (LOG.isLoggable(REFUSE)) {
                        LOG.log(getRefuse("%s marriageRecord.getWife().getFirstName() %s must be same as wife.getFirstName() %s %s",
                                wife, marriageRecord.getWife().getFirstName(), wife, wife.getFirstName()));
                    }
                    continue;
                }

                // l'epouse doit avoir une date de naissance compatible
                if (!isCompatible(marriageRecord.getWife().getBirthDate(), wife.getBirthDate(), 1)) {
                    if (LOG.isLoggable(REFUSE)) {
                        LOG.log(getRefuse("%s marriageRecord.getWife().getBirthDate() %s must compatible with wife.getBirthDate() %s %s ",
                                wife, marriageRecord.getWife().getBirthDate(), wife, wife.getBirthDate()));
                        continue;
                    }
                }

                // si la date de naissance de la femme n'est pas precisée, l'epouse doit avoir au moins minMarriageYearOld
                if (!marriageRecord.getWife().getBirthDate().isValid()) {
                    if (!isRecordAfterThanDate(marriageDate, wife.getBirthDate(), 0, minMarriageYearOld)) {
                        if (LOG.isLoggable(REFUSE)) {
                            LOG.log(getRefuse("%s marriageDate %s must be after wife.getBirthDate() %s %s + %dy",
                                    wife, marriageDate, wife, wife.getBirthDate(), minMarriageYearOld));
                        }
                        continue;
                    }
                }

                // l'epouse ne doit pas etre decedee avant le mariage
                if (!isRecordBeforeThanDate(marriageDate, wife.getDeathDate(), 0, 0)) {
                    if (LOG.isLoggable(REFUSE)) {
                        LOG.log(getRefuse("%s marriageDate %s must be before wife.getDeathDate() %s %s",
                                wife, marriageDate, wife, wife.getDeathDate()));
                    }
                    continue;
                }

                // l'epouse doit avoir une date de deces compatible
                if (!isCompatible(marriageRecord.getWife().getDeathDate(), wife.getDeathDate(), 1)) {
                    if (LOG.isLoggable(REFUSE)) {
                        LOG.log(getRefuse("%s marriageRecord.getWife().getDeathDate() %s must be compatible with wife.getDeathDate() %s %s",
                                wife, marriageRecord.getWife().getDeathDate(), wife, wife.getDeathDate()));
                    }
                    continue;
                }

                // je verifie les parents de l'epouse
                Indi wifeFather = wife.getBiologicalFather();
                if (wifeFather != null) {
                    RecordParent mergeWifeFather = marriageRecord.getWife().getFather();
                    // meme nom du pere de l'epouse
                    if (!mergeWifeFather.getLastName().isEmpty()
                            && !isSameLastName(mergeWifeFather.getLastName(), wifeFather.getLastName())) {
                        if (LOG.isLoggable(REFUSE)) {
                            LOG.log(getRefuse("%s mergeWifeFather.getLastName()¨%s must be same as wifeFather.getLastName() %s %s",
                                    wife, mergeWifeFather.getLastName(), wifeFather, wifeFather.getLastName()));
                        }
                        continue;
                    }

                    //meme prénom du pere de l'epouse
                    if (!mergeWifeFather.getFirstName().isEmpty()
                            && !isSameFirstName(mergeWifeFather.getFirstName(), wifeFather.getFirstName())) {
                        if (LOG.isLoggable(REFUSE)) {
                            LOG.log(getRefuse("%s mergeWifeFather.getFirstName()¨%s must be same as wifeFather.getFirstName() %s %s",
                                    wife, mergeWifeFather.getFirstName(), wifeFather, wifeFather.getFirstName()));
                        }
                        continue;
                    }

                    // le pere doit etre ne au moins minParentYearOld+minMarriageYearOld avant le mariage
                    if (!isRecordAfterThanDate(marriageDate, wifeFather.getBirthDate(), 0, minParentYearOld + minMarriageYearOld)) {
                        if (LOG.isLoggable(REFUSE)) {
                            LOG.log(getRefuse("%s marriageDate¨%s must be after wifeFather.getBirthDate() %s %s + %dy",
                                    wife, marriageDate, wifeFather, wifeFather.getBirthDate(), minParentYearOld + minMarriageYearOld));
                        }
                        continue;
                    }
                    // le pere ne doit pas etre decede 9 mois avant la date de naissance de l'epouse
                    if (!isRecordBeforeThanDate(marriageRecord.getWife().getBirthDate(), wifeFather.getDeathDate(), 9, 0)) {
                        if (LOG.isLoggable(REFUSE)) {
                            LOG.log(getRefuse("%s marriageRecord.getWife().getBirthDate()¨%s must be before wifeFather.getDeathDate() %s %s -%d month",
                                    wife, marriageRecord.getWife().getBirthDate(), wifeFather, wifeFather.getDeathDate(), 9));
                        }
                        continue;
                    }

                }


                // l'epouse doit avoir au moins minMarriageYearOld
                if (!isRecordAfterThanDate(marriageDate, wife.getBirthDate(), 0, minMarriageYearOld)) {
                    if (LOG.isLoggable(REFUSE)) {
                        LOG.log(getRefuse("%s marriageDate %s must be after wife.getBirthDate() %s %s + %dy",
                                wife, marriageDate, wife, wife.getBirthDate(), minMarriageYearOld));
                    }
                    continue;
                }

                Indi wifeMother = wife.getBiologicalMother();
                if (wifeMother != null) {
                    RecordParent mergeWifeMother = marriageRecord.getWife().getMother();
                    // meme nom de la mere de l'epouse
                    if (!mergeWifeMother.getLastName().isEmpty()
                            && !isSameLastName(mergeWifeMother.getLastName(), wifeMother.getLastName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeWifeMother.getLastName()¨%s must be same as wifeMother.getLastName() %s %s",
                                    wife, mergeWifeMother.getLastName() , wifeMother, wifeMother.getLastName() ));
                        }
                        continue;
                    }

                    //meme prénom de la mere de l'epouse
                    if (!mergeWifeMother.getFirstName().isEmpty()
                            && !isSameFirstName(mergeWifeMother.getFirstName(), wifeMother.getFirstName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeWifeMother.getFirstName()¨%s must be same as wifeMother.getFirstName() %s %s",
                                    wife, mergeWifeMother.getFirstName() , wifeMother, wifeMother.getFirstName() ));
                        }
                        continue;
                    }

                    // la mere doit etre ne au moins minParentYearOld+minMarriageYearOld avant le mariage
                    if (!isRecordAfterThanDate(marriageDate, wifeMother.getBirthDate(), 0, minParentYearOld + minMarriageYearOld)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s marriageDate¨%s must be after wifeMother.getBirthDate() %s %s + %dy",
                                    wife, marriageDate, wifeMother, wifeMother.getBirthDate(), minParentYearOld + minMarriageYearOld ));
                        }
                        continue;
                    }

                    // la mere ne doit pas etre decede avant la date de naissance de l'epouse
                    if (!isRecordBeforeThanDate(marriageRecord.getWife().getBirthDate(), wifeMother.getDeathDate(), 9, 0)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s marriageRecord.getWife().getBirthDate()¨%s must be before wifeMother.getDeathDate() %s %s -%d month",
                                    wife, marriageRecord.getWife().getBirthDate(), wifeMother, wifeMother.getDeathDate(), 9 ));
                        }
                        continue;
                    }

                }

                // je verifie s'il a un conjoint en vie avec un nom différent et un prenom different
                boolean oftherSpouseFound = false;
                Fam[] spouseFamList = wife.getFamiliesWhereSpouse();
                for (Fam fam : spouseFamList) {
                    Indi husband = fam.getHusband();

                    if (!(isSameLastName(marriageRecord.getIndi().getLastName(), husband.getLastName())
                            && isSameLastName(marriageRecord.getIndi().getFirstName(), husband.getFirstName()))) {

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
                if (oftherSpouseFound) {
                    if (LOG.isLoggable(REFUSE)) {
                        LOG.log(getRefuse("%s wife must not have spouse with other name",
                                wife ));
                    }
                    continue;
                }

                wifes.add(wife);
            }
        }

        if (LOG.isLoggable(Level.FINER)){
            StringBuilder result = new StringBuilder("RETURN husbands");
            for(Indi husband : husbands) {
                result.append(" ").append(husband.getId());
            }
            result.append(" wifes ");
            for(Indi wife : wifes) {
                result.append(" ").append(wife.getId());
            }
            LOG.finer(result.toString());
        }

    }

    /**
     * retourne la liste des individus qui ont le même nom et une date de
     * naissance compatible avec celle du relevé mais qui ont un identifiant
     * different de l'individu excludeIndi
     *
     * @param record
     * @param gedcom
     * @param excludeIndi
     * @return liste des individus
     */
    static protected List<Indi> findIndiCompatibleWithParticipant(MergeRecord record, RecordParticipant participant, Gedcom gedcom, Indi excludeIndi) throws Exception {
        if (LOG.isLoggable(Level.FINER)){
            LOG.entering(MergeQuery.class.getName(), "findIndiCompatibleWithParticipant");
        }
        List<Indi> sameIndis = new ArrayList<Indi>();

        // la date de naissance doit etre renseignee
//        if (!participant.getBirthDate().isComparable()) {
//            // j'abandonne si la date de naissance du relevé n'est pas renseignée
//            continue;
//        }



        for (Indi indi : gedcom.getIndis()) {

            // individu a exclure
            if (excludeIndi != null && excludeIndi.compareTo(indi) == 0) {
                if (LOG.isLoggable(REFUSE)) {
                    LOG.log(getRefuse("%s indi in  exclude",
                            indi));
                }
                continue;
            }

            // meme sexe de l'individu
            if (participant.getSex() != PropertySex.UNKNOWN
                    && indi.getSex() != PropertySex.UNKNOWN
                    && participant.getSex() != indi.getSex()) {
                if (LOG.isLoggable(REFUSE)) {
                    LOG.log(getRefuse("%s participant %d must be same sex as indi %d ",
                            indi, participant.getSex(), indi.getSex() ));
                }
                continue;
            }

            // meme nom de l'individu
            if (!participant.getLastName().isEmpty()
                    && !isSameLastName(participant.getLastName(), indi.getLastName() )) {
                if (LOG.isLoggable(REFUSE)) {
                    LOG.log(getRefuse("%s participant.getLastName() %s must be same as indi.getLastName() %s %s",
                            indi, participant.getLastName() , indi, indi.getLastName() ));
                }
                continue;
            }

            // meme prenom de l'individu
            if (!participant.getFirstName().isEmpty()
                    && !isSameFirstName(participant.getFirstName(), indi.getFirstName())) {
                if (LOG.isLoggable(REFUSE)) {
                    LOG.log(getRefuse("%s participant.getFirstName() %s must be same as indi.getFirstName() %s %s",
                            indi, participant.getFirstName(), indi, indi.getFirstName()));
                }
                continue;
            }

            // la date de naissance du relevé doit être compatible avec celle de l'individu
            // petit raccourci pour gagner du temps
            PropertyDate indiBirtDate = indi.getBirthDate();
            if (indiBirtDate != null) {
                // je tolere un jour d'écart pour la date de naissance
                if (!isCompatible(participant.getBirthDate(), indiBirtDate, 1)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s participant.getBirthDate() %s must be compatible with indiBirtDate %s %s",
                                indi, participant.getBirthDate(), indi, indiBirtDate ));
                    }
                    continue;
                }
            }

            // la date de décès doit être compatible doit être compatible avec celle de l'individu
            // petit raccourci pour gagner du temps
            PropertyDate indiDeathDate = indi.getDeathDate();
            if (indiDeathDate != null) {
                if (!isCompatible(participant.getDeathDate(), indiDeathDate, 1)) {
                    if (LOG.isLoggable(REFUSE)){
                        LOG.log(REFUSE,  String.format("REFUSE %s participant.getDeathDate() %s must be compatible with indiDeathDate) %s %s",
                                indi, participant.getDeathDate(), indi, indiDeathDate ));
                    }
                    continue;
                }
            }

            // la date de décès du relevé doit être après le mariage de l'individu
            Fam[] sameIndiFamiliesWhereSpouse = indi.getFamiliesWhereSpouse();
            Fam famConflict = null;
            for (Fam fam : sameIndiFamiliesWhereSpouse) {
                if (!MergeQuery.isRecordAfterThanDate(participant.getDeathDate(), fam.getMarriageDate(), 0, 0)) {
                    famConflict = fam;
                    break;
                }
            }
            if (famConflict != null) {
                // il a conflit , je ne retiens pas cet individu
                if (LOG.isLoggable(REFUSE)) {
                    LOG.log(getRefuse("%s participant.getDeathDate()¨%s must be after fam.getMarriageDate() %s %s",
                            indi, participant.getDeathDate(), famConflict, famConflict.getMarriageDate() ));
                }
                continue;
            }

            Fam parentFamily = indi.getFamilyWhereBiologicalChild();
            if (parentFamily != null) {
                PropertyDate parentMarriageDate = parentFamily.getMarriageDate();
                if (parentMarriageDate != null) {
                    // la naissance doit être après la date mariage des parents.
                    if (!isRecordAfterThanDate(participant.getBirthDate(), parentMarriageDate, 0, 0)) {
                        if (LOG.isLoggable(REFUSE)) {
                            LOG.log(getRefuse("%s participant.getBirthDate()¨%s must be after parentMarriageDate %s %s",
                                    indi, participant.getBirthDate(), parentFamily, parentMarriageDate ));
                        }
                        continue;
                    }
                    // le deces doit être après la date mariage des parents.
                    if (!isRecordAfterThanDate(participant.getDeathDate(), parentMarriageDate, 0, 0)) {
                        if (LOG.isLoggable(REFUSE)) {
                            LOG.log(getRefuse("%s participant.getBirthDate()¨%s must be after parentMarriageDate %s %s",
                                    indi, participant.getDeathDate(), parentFamily, parentMarriageDate ));
                        }
                        continue;
                    }
                }

                Indi indiFather = parentFamily.getHusband();
                if (indiFather != null) {
                    RecordParent mergeFather = participant.getFather();
                    // meme nom du pere
                    if (!mergeFather.getLastName().isEmpty()
                            && !isSameLastName(mergeFather.getLastName(), indiFather.getLastName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeFather.getLastName()¨%s must be same as indiFather.getLastName() %s %s",
                                    indi, mergeFather.getLastName(), indiFather, indiFather.getLastName() ));
                        }
                        continue;
                    }
                    //meme prénom du pere
                    if (!mergeFather.getFirstName().isEmpty()
                            && !isSameFirstName(mergeFather.getFirstName(), indiFather.getFirstName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeFather.getFirstName()¨%s must be same as indiFather.getFirstName() %s %s",
                                    indi, mergeFather.getFirstName(), indiFather, indiFather.getFirstName() ));
                        }
                        continue;
                    }

                    // date de naissance apres la date de naissance du père + minParentYearOld
                    if (!isRecordAfterThanDate(participant.getBirthDate(), indiFather.getBirthDate(), 0, minParentYearOld)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s participant.getBirthDate() %s must be after indiFather.getBirthDate() %s  %s + %dy",
                                    indi, participant.getBirthDate(), indiFather, indiFather.getBirthDate(), minParentYearOld));
                        }
                        continue;
                    }
                    // le pere ne doit pas etre decede 9 mois avant la date de naissance
                    if (!isRecordBeforeThanDate(participant.getBirthDate(), indiFather.getDeathDate(), 9, 0)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s participant.getBirthDate() %s must be before indiFather.getDeathDate()  %s + %d month",
                                    indi, participant.getBirthDate(), indiFather.getDeathDate(), 9 ));
                        }
                        continue;
                    }
                }

                Indi indiMother = parentFamily.getWife();
                if (indiMother != null) {
                    RecordParent mergeMother = participant.getMother();
                    // meme nom de la mere
                    if (!mergeMother.getLastName().isEmpty()
                            && !isSameLastName(mergeMother.getLastName(), indiMother.getLastName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeMother.getLastName()¨%s must be same as indiMother.getLastName() %s %s",
                                    indi, mergeMother.getLastName(), indiMother, indiMother.getLastName() ));
                        }
                        continue;
                    }
                    //meme prénom de la mere
                    if (!mergeMother.getFirstName().isEmpty()
                            && !isSameFirstName(mergeMother.getFirstName(), indiMother.getFirstName())) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s mergeMother.getFirstName()¨%s must be same as indiMother.getFirstName() %s %s",
                                    indi, mergeMother.getFirstName(), indiMother, indiMother.getFirstName() ));
                        }
                        continue;
                    }
                    // date de naissance apres la date de naissance de la mère + minParentYearOld
                    if (!isRecordAfterThanDate(participant.getBirthDate(), indiMother.getBirthDate(), 0, minParentYearOld)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s participant.getBirthDate()¨%s must be after indiMother.getBirthDate() %s %s + %dy",
                                    indi, participant.getBirthDate(), indiMother, indiMother.getBirthDate(), minParentYearOld ));
                        }
                        continue;
                    }
                    // la mere ne doit pas etre decedee avant la date de naissance
                    if (!isRecordBeforeThanDate(participant.getBirthDate(), indiMother.getDeathDate(), 0, 0)) {
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s participant.getBirthDate()¨%s must be before indiMother.getDeathDate() %s %s",
                                    indi, participant.getBirthDate(), indiMother, indiMother.getDeathDate() ));
                        }
                        continue;
                    }
                }
            }
            // j'ajoute l'individu dans la liste
            sameIndis.add(indi);

        }

        if (LOG.isLoggable(Level.FINER)){
            StringBuilder result = new StringBuilder("RETURN");
            result.append(" sameIndis ");
            for(Indi indi : sameIndis) {
                result.append(" ").append(indi.getId());
            }
            LOG.finer(result.toString());
        }

        return sameIndis;
    }

    /**
     * retourne la liste des enfants de la famille sélectionnée qui ont le même
     * prénom et des dates de naissance et de décès compatiblew avec celle du
     * relevé. Génère une exception si le nom des parents de la famille
     * sélectionnée est different du nom des parents du releve, ou si leur age
     * ou la date de mariage n'est pas compatible avec la date de naissance.
     *
     * @param birthRecord
     * @param gedcom
     * @param selectedFamily famille sélectionnée
     * @return liste des enfants
     */
    static protected List<Indi> findSameChild(MergeRecord birthRecord, Gedcom gedcom, Fam selectedFamily) throws Exception {
        if (LOG.isLoggable(Level.FINER)){
            LOG.entering(MergeQuery.class.getName(), "findSameChild");
        }

        List<Indi> sameChildren = new ArrayList<Indi>();

        // je recupere la date de naissance du releve
        RecordParticipant participant = birthRecord.getIndi();
        PropertyDate recordBirthDate = participant.getBirthDate();

        if (selectedFamily != null) {

            // je recherche les enfants conpatibles avec le relevé
            for (Indi indi : selectedFamily.getChildren()) {

                // meme sexe de l'enfant
                if (participant.getSex() != PropertySex.UNKNOWN
                        && indi.getSex() != PropertySex.UNKNOWN
                        && participant.getSex() != indi.getSex()) {
                    if (LOG.isLoggable(REFUSE)) {
                        LOG.log(getRefuse("%s participant %d must be same sex as child %d ",
                                indi, participant.getSex(), indi.getSex() ));
                    }
                    continue;
                }

                // meme nom de l'enfant
                if (!participant.getLastName().isEmpty()
                        && !isSameLastName(participant.getLastName(), indi.getLastName())) {
                    if (LOG.isLoggable(REFUSE)) {
                        LOG.log(getRefuse("%s participant.getLastName() %s must be same as indi.getLastName() %s %s",
                                indi, participant.getLastName() , indi, indi.getLastName() ));
                    }
                    continue;
                }

                // meme prenom de l'enfant
                if (!participant.getFirstName().isEmpty()
                        && !isSameFirstName(participant.getFirstName(), indi.getFirstName())) {
                    if (LOG.isLoggable(REFUSE)) {
                        LOG.log(getRefuse("%s participant.getFirstName() %s must be same as indi.getFirstName() %s %s",
                                indi, participant.getFirstName(), indi, indi.getFirstName()));
                    }
                    continue;
                }

                // petit raccourci pour gagner du temps
                PropertyDate indiBirtDate = indi.getBirthDate();

                // date de naissance compatible
                if (indiBirtDate != null) {
                    if (!isCompatible(recordBirthDate, indiBirtDate, 1)) {
                        // la date de naissance de l'individu n'est pas compatible avec la date du relevé
                        if (LOG.isLoggable(REFUSE)) {
                            LOG.log(getRefuse("%s participant.getBirthDate() %s must be compatible with indiBirtDate %s %s",
                                    indi, participant.getBirthDate(), indi, indiBirtDate));
                        }
                        continue;
                    }
                }

                // date de naissance apres la date de naissance du père + minParentYearOld
                Indi indiFather = selectedFamily.getHusband();
                if (indiFather != null) {
                    if (!isRecordAfterThanDate(recordBirthDate, indiFather.getBirthDate(), 0, minParentYearOld)) {
                        // la date de naissance de l'individu n'est pas apres avec la date de naissance du pere
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s participant.getBirthDate() %s must be after indiFather.getBirthDate() %s  %s + %dy",
                                    indi, participant.getBirthDate(), indiFather, indiFather.getBirthDate(), minParentYearOld));
                        }
                        continue;
                    }
                }

                // date de naissance apres la date de naissance de la mère + minParentYearOld
                Indi indiMother = selectedFamily.getWife();
                if (indiMother != null) {
                    if (!isRecordAfterThanDate(recordBirthDate, indiMother.getBirthDate(), 0, minParentYearOld)) {
                        // la date de naissance de l'individu n'est pas apres avec la date de naissance de la mere
                        if (LOG.isLoggable(REFUSE)){
                            LOG.log(getRefuse("%s participant.getBirthDate()¨%s must be after indiMother.getBirthDate() %s %s + %dy",
                                    indi, participant.getBirthDate(), indiMother, indiMother.getBirthDate(), minParentYearOld ));
                        }
                        continue;
                    }
                }

                // date de décés compatible
                PropertyDate childDeathDate = indi.getDeathDate();
                if (childDeathDate != null) {
                    if (!isCompatible(participant.getDeathDate(), childDeathDate, 1)) {
                        // la date de décès de l'individu n'est pas compatible avec la date du relevé
                        if (LOG.isLoggable(REFUSE)) {
                            LOG.log(getRefuse("%s participant.getDeathDate(() %s must be compatible with indi.getDeathDate() %s %s",
                                    indi, participant.getDeathDate(), indi, indi.getDeathDate()));
                        }
                        continue;
                    }
                }

                // j'ajoute l'enfant dans la liste
                sameChildren.add(indi);
            }
        }

        if (LOG.isLoggable(Level.FINER)){
            StringBuilder result = new StringBuilder("RETURN sameChildren");
            for(Indi indi : sameChildren) {
                result.append(" ").append(indi.getId());
            }
            LOG.finer(result.toString());
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

        return isCompatible(recordDate, entityDate, 0);
    }

    /**
     * retourne "true" si la date du releve est egale ou est compatible avec la
     * date de l'entite du gedcom avec une marge exprimée en jour
     *
     * @param recordDate
     * @param entityDate
     * @return
     */
    public static boolean isCompatible(PropertyDate recordDate, PropertyDate entityDate, int marge) {
        boolean result;
        if (recordDate == null) {
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
                recordEnd = recordStart;
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
                if (pit.getMonth() == PointInTime.UNKNOWN) {
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
                entityEnd = entityStart;
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
            if (recordEnd >= entityStart - marge) {
                result = entityEnd >= recordStart - marge;
            } else {
                result = false;

            }
        } catch (GedcomException ex) {
            result = false;
        }
        return result;
    }

    /**
     * retourne true si str1 est égal ou se prononce comme str2 ou si str1 est
     * vide
     * si plusieurs mots sont présents séparés par une virgule ou un espace
     * chaque mot est compare individuellement
     *
     * @param str1
     * @param str2
     * @return
     */
    static public boolean isSameLastName(String str1, String str2) {
        boolean result = false;
        if( str1 != null && ! str1.isEmpty()) {
            String[] names1 = str1.split(",");
            String[] names2 = str2.split(",");

            for( String name1 : names1) {
                String similarName1 = SimilarNameSet.getSimilarLastName().getSimilarName(name1);
                for( String name2 : names2) {
                    result |= dm.encode(similarName1).equals(dm.encode(SimilarNameSet.getSimilarLastName().getSimilarName(name2)));
                    if (result) {
                        break;
                    }
                }
                if (result) {
                    break;
                }
            }
        } else {
            //
            return true;
        }
        return result;
    }

    /**
     * retourne true si str1 est égal ou se prononce comme str2
     * si plusieurs mots sont présents séparés par une virgule ou un espace
     * chaque mot est compare individuellement
     *
     * @param str1
     * @param str2
     * @return
     */
    public static boolean isSameFirstName(String str1, String str2) {
        boolean result = false;

        if( str1 != null && ! str1.isEmpty()) {
            String[] names1 = str1.split("[\\,|\\ ]+"); // The + after treats consecutive delimiter chars as one.
            String[] names2 = str2.split("[\\,|\\ ]+");

            for( String name1 : names1) {
                String similarName1 = SimilarNameSet.getSimilarFirstName().getSimilarName(name1);
                for( String name2 : names2) {
                    result |= dm.encode(similarName1).equals(dm.encode(SimilarNameSet.getSimilarFirstName().getSimilarName(name2)));
                    if( result) {
                        break;
                    }
                }
                if (result) {
                    break;
                }
            }
        } else {
            return true;
        }
        return result;
    }

    /**
     * retourne true si la date de naissance du parent est inférieure à la date
     * du relevé (diminuée de l'age minimum pour être parent) et si le parent a
     * moins de indiMaxYearOld (=100 ans) à la date du relevé. Autrement dit :
     * recordDate - indiMaxYearOld < parentBirthDate  < recordDate - (minMonthShift + minYearShift)
     *
     *   recordDate-100ans  parentBirthDate    recordDate-minYearShift
     *   ----[}------------------[******]------[******]------------  => true
     *
     * r-100ans recordDate-minYearShift parentBirthDate ----[}--------
     * [******]-----------------[******]---------- => false
     *
     * parentBirthDate recordDate-100ans recordDate-minYearShift
     * ---[******]------------[}---------------- [******]-------- => false
     *
     * @param recordDate date du relevé
     * @param parentDeathDate date de naissance du parent
     * @param minDiff
     * @return true si indiFirthDate + minMonthDiff + minYearDiff <=
     * recordBirthDate
     */
    static protected boolean isRecordAfterThanDate(PropertyDate recordDate, PropertyDate parentBirthDate, int minMonthShift, int minYearShift) {
        boolean result;
        if (recordDate == null) {
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
                minEnd = minStart;

                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                maxStart = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
                maxEnd = maxStart;
            } else if (recordDate.getFormat() == PropertyDate.BETWEEN_AND || recordDate.getFormat() == PropertyDate.FROM_TO) {
                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                minStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
                pit.set(recordDate.getEnd().getPointInTime(PointInTime.GREGORIAN));
                minEnd = pit.add(0, 0, -indiMaxYearOld).getJulianDay();

                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                //maxStart = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
                pit.set(recordDate.getEnd().getPointInTime(PointInTime.GREGORIAN));
                maxEnd = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
            } else if (recordDate.getFormat() == PropertyDate.FROM || recordDate.getFormat() == PropertyDate.AFTER) {
                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                minStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();
                minEnd = Integer.MAX_VALUE;

                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                //maxStart = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
                maxEnd = Integer.MAX_VALUE;
            } else if (recordDate.getFormat() == PropertyDate.TO || recordDate.getFormat() == PropertyDate.BEFORE) {
                minStart = Integer.MIN_VALUE;
                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                minEnd = pit.add(0, 0, -indiMaxYearOld).getJulianDay();

                //maxStart = Integer.MIN_VALUE;
                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                maxEnd = pit.add(0, -minMonthShift, -minYearShift).getJulianDay();
            } else {
                // ABOUT, ESTIMATED, CALCULATED
                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                minStart = pit.add(0, 0, -indiMaxYearOld - aboutYear).getJulianDay();
                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                minEnd = pit.add(0, 0, -indiMaxYearOld + aboutYear).getJulianDay();

                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                //maxStart = pit.add(0, -minMonthShift, -minYearShift-aboutYear).getJulianDay();
                pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                maxEnd = pit.add(0, -minMonthShift, -minYearShift + aboutYear).getJulianDay();
            }

            int birthStart;
            int birthEnd;

            if (parentBirthDate.getFormat() == PropertyDate.DATE) {
                birthStart = parentBirthDate.getStart().getJulianDay();
                birthEnd = birthStart;
            } else if (parentBirthDate.getFormat() == PropertyDate.BETWEEN_AND || parentBirthDate.getFormat() == PropertyDate.FROM_TO) {
                birthStart = parentBirthDate.getStart().getJulianDay();
                birthEnd = parentBirthDate.getEnd().getJulianDay();
            } else if (parentBirthDate.getFormat() == PropertyDate.FROM || parentBirthDate.getFormat() == PropertyDate.AFTER) {
                birthStart = parentBirthDate.getStart().getPointInTime(PointInTime.GREGORIAN).getJulianDay();
                birthEnd = Integer.MAX_VALUE;
            } else if (parentBirthDate.getFormat() == PropertyDate.TO || parentBirthDate.getFormat() == PropertyDate.BEFORE) {
                //birthStart =  Integer.MIN_VALUE;
                pit.set(parentBirthDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                birthStart = pit.add(0, 0, -indiMaxYearOld).getJulianDay();

                birthEnd = parentBirthDate.getStart().getJulianDay();
            } else {
                // ABOUT, ESTIMATED, CALCULATED
                PointInTime startPit = new PointInTime();
                startPit.set(parentBirthDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                birthStart = startPit.add(0, 0, -aboutYear).getJulianDay();
                PointInTime endPit = new PointInTime();
                endPit.set(parentBirthDate.getStart().getPointInTime(PointInTime.GREGORIAN));
                birthEnd = startPit.add(0, 0, +aboutYear).getJulianDay();
            }

            if (birthStart > maxEnd) {
                result = false;
            } else {
                result = minStart <= birthEnd;
            }
        } catch (GedcomException ex) {
            result = false;
        }
        return result;
    }

    /**
     * retourne true si la date de deces du parent est superieure à la date du
     * relevé et si le parent est né moins de 100 ans avant la date du relevé.
     * Autrement dit : deaththDate -100 < recordDate - (minMonthShift +
     * minYearShift) < deaththDate
     *
     *   deathDate-100ans  recordDate-minMonthShift     deathDate
     *   ----[}------------------[******]---------------[******]----  => true
     *
     * r-100ans recordDate-minYearShift parentBirthDate ----[}--------
     * [******]-----------------[******]---------- => false
     *
     * parentBirthDate recordDate-100ans recordDate-minYearShift
     * ---[******]------------[}---------------- [******]-------- => false
     *
     * @param recordDate date du relevé
     * @param parentBirthDate date de naissance du parent
     * @param minDiff
     * @return true si indiFirthDate + minMonthDiff + minYearDiff <=
     * recordBirthDate
     */
    static protected boolean isRecordBeforeThanDate(PropertyDate recordDate, PropertyDate parentDeathDate, int minMonthShift, int minYearShift) throws GedcomException {
        boolean result;
        if (recordDate == null) {
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
            recEnd = recStart;
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
            recStart = pit.add(0, -minMonthShift, -minYearShift - aboutYear).getJulianDay();
            pit.set(recordDate.getStart().getPointInTime(PointInTime.GREGORIAN));
            recEnd = pit.add(0, -minMonthShift, -minYearShift + aboutYear).getJulianDay();
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
        if (recEnd >= maxStart && maxEnd >= recStart) {
            result = true;
        } else {
            result = false;

        }
        return result;
    }

    /**
     * retourne true si l'intervalle [start end ] contient refTime
     *
     * refTime --- [****]----- [*****X*****]------[*****] ----- false true false
     *
     * @param refTime
     * @param start
     * @param end
     * @return
     */
//    static private boolean containsRefTime(final PointInTime refTime, final PointInTime start, final PointInTime end) throws GedcomException {
//        int jdRef = refTime.getJulianDay();
//        int jdStart = start.getJulianDay();
//        int jdEnd = end.getJulianDay();
//        return jdStart <= jdRef && jdRef <= jdEnd;
//    }

    /**
     * retourne la date la plus précise entre la date du releve la date de
     * l'entité
     *
     * record birth date date vrai si record est plus precis que birth date BEF
     * vrai si record est avant birth date AFT vrai si record est apres birth
     * date range vrai si record est dans l'intervalle
     *
     * BEF date faux BEF BEF vrai si record est avant birth BEF AFT vrai si
     * record est apres birth BEF range vrai si record est entièrement dans
     * l'intervalle birth
     *
     * AFT date faux AFT BEF vrai si record est avant birth AFT AFT vrai si
     * record est apres birth AFT range vrai si record est dans l'intervalle
     *
     * range date faux range BEF vrai si l'intervalle de record est avant birth
     * range AFT vrai si l'intervalle de record est apres birth range range vrai
     * si l'intersection des intervalles n'est pas vide
     *
     * @param recordDate date du releve
     * @param gedcomDate date de l'individu Gedcom
     * @return null si les dates sont incompatibles recordDate si la date du
     * relevé est plus précise gedcomDate si la date de l'entité est plus
     * précise mergeDate si une intersection plus precise existe entre les deux
     * dates
     */
    static public PropertyDate getMostAccurateDate(PropertyDate recordDate, PropertyDate gedcomDate) {
        PropertyDate result;
        try {

            if (!gedcomDate.isValid()) {
                result = recordDate;
            } else if (gedcomDate.getFormat() == PropertyDate.DATE) {
                if (recordDate.getFormat() == PropertyDate.DATE) {
                    // je compare l'année , puis le mois , puis le jour
                    if (gedcomDate.getStart().getYear() == PointInTime.UNKNOWN) {
                        if (recordDate.getStart().getYear() != PointInTime.UNKNOWN) {
                            result = recordDate;
                        } else {
                            result = gedcomDate;
                        }
                    } else if (gedcomDate.getStart().getMonth() == PointInTime.UNKNOWN) {
                        if (recordDate.getStart().getMonth() != PointInTime.UNKNOWN) {
                            result = recordDate;
                        } else {
                            result = gedcomDate;
                        }
                    } else if (gedcomDate.getStart().getDay() == PointInTime.UNKNOWN) {
                        if (recordDate.getStart().getDay() != PointInTime.UNKNOWN) {
                            result = recordDate;
                        } else {
                            result = gedcomDate;
                        }
                    } else {
                        if (recordDate.getStart().getYear() != PointInTime.UNKNOWN
                                && recordDate.getStart().getMonth() != PointInTime.UNKNOWN
                                && recordDate.getStart().getDay() != PointInTime.UNKNOWN) {
                            if (recordDate.getStart().getYear() == gedcomDate.getStart().getYear()
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
                } else if (recordDate.getFormat() == PropertyDate.BETWEEN_AND || recordDate.getFormat() == PropertyDate.FROM_TO) {
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
                } else if (gedcomDate.getFormat() == PropertyDate.BETWEEN_AND || gedcomDate.getFormat() == PropertyDate.FROM_TO) {
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
                boolean aboutStart;
                boolean aboutEnd;
                // start = max (start1, start2)
                if (start1 > start2) {
                    start = start1;
                    aboutStart = about1;
                } else {
                    start = start2;
                    aboutStart = about2;
                }
                // end = min (end1, end2)
                if (end1 > end2) {
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
     * retourne la profession d'un individu, avec le lieu et la date S'il y a
     * plusieurs profession, retourne celle qui a la date la plus proche de la
     * date donnée en paramètre. Si audune profession n'est trouvée, retourne le
     * domicile
     *
     * @param indi
     * @param occupationDate
     * @return occupation+residence+date or empty string
     */
    static protected String findOccupation(Indi indi, PropertyDate occupationDate) {
        Property foundOccupation = null;
        Property foundDate = null;
        for (Property iterationOccupation : indi.getProperties("OCCU")) {
            // je recherche les dates meme si elles ne sont pas valides
            for (Property iterationDate : iterationOccupation.getProperties("DATE", false)) {
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
        String result;
        if (foundOccupation != null) {
            result = foundOccupation.getValue();
            Property address = foundOccupation.getProperty("ADDR");
            if (address != null && !address.getValue().isEmpty()) {
                if (!result.isEmpty()) {
                    result += ", ";
                }
                result += address.getValue();
            }

            Property place = foundOccupation.getProperty("PLAC");
            if (place != null && !place.getValue().isEmpty()) {
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
     * retourne le domicile d'un individu. S'il y a plusieurs domiciles,
     * retourne celui qui a la date la plus proche de la date donnée en
     * paramètre
     *
     * @param indi
     * @param residenceDate
     * @return residence+date or empty string
     */
    static protected String findResidence(Indi indi, PropertyDate residenceDate) {
        Property foundResidence = null;
        Property foundDate = null;
        for (Property iterationResidence : indi.getProperties("RESI")) {
            // je recherche les dates meme si elles ne sont pas valides
            for (Property iterationDate : iterationResidence.getProperties("DATE", false)) {
                if (foundResidence == null) {
                    foundResidence = iterationResidence;
                    foundDate = iterationDate;
                } else {
                    if (Math.abs(residenceDate.compareTo((PropertyDate) iterationDate)) <= Math.abs(residenceDate.compareTo(foundDate))) {
                        foundResidence = iterationResidence;
                        foundDate = iterationDate;
                    }
                }
            }
        }
        String result = "";
        if (foundResidence != null) {
            result = foundResidence.getValue();
            Property address = foundResidence.getProperty("ADDR");
            if (address != null && !address.getValue().isEmpty()) {
                if (!result.isEmpty()) {
                    result += ", ";
                }
                result += address.getValue();
            }

            Property place = foundResidence.getProperty("PLAC");
            if (place != null && !place.getValue().isEmpty()) {
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

}
