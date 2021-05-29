package ancestris.modules.releve.model;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.Record.FieldType;
import genj.gedcom.Gedcom;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.openide.util.Exceptions;

/**
 *
 * @author Michel
 */
public class CompletionProviderTest  {

    /**
     * test ajout d'une naissance
     */
    @Test
    public void testAddBirthRecord() {
        try {
            RecordBirth record = new RecordBirth();
            record.setFieldValue(FieldType.eventDate, "01/01/2000");
            record.setFieldValue(FieldType.cote, "cote");
            record.setFieldValue(FieldType.freeComment,  "photo");
            record.setIndi("OneFirstName", "FATHERLASTNAME", "F", "", "", "indiBirthplace", "indiBirthAddress", "indioccupation", "indiResidence", "indiAddress", "indicomment");
            record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "indiFatherResidence", "indiFatherAddress", "comment", "dead", "70y");
            record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "indiMotherAddress", "comment", "dead", "72y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
            record.setFieldValue(FieldType.generalComment, "generalcomment");

            CompletionProvider completionProvider = new CompletionProvider();
            completionProvider.addRecord(record);
            // je verifie que les données ont bien été ajoutées
            assertEquals("Nombre de prenoms",     7,  completionProvider.getFirstNames().getAll().size());
            assertEquals("Nombre de noms",        6,  completionProvider.getLastNames().getAll().size());
            assertEquals("Nombre de professions", 7,  completionProvider.getOccupations().getAll().size());
            assertEquals("Nombre de lieux",       4,  completionProvider.getPlaces().getAll().size());

            completionProvider.removeRecord(record);
            assertEquals("Nombre de prenoms",     0,  completionProvider.getFirstNames().getAll().size());
            assertEquals("Nombre de noms",        0,  completionProvider.getLastNames().getAll().size());
            assertEquals("Nombre de professions", 0,  completionProvider.getOccupations().getAll().size());
            assertEquals("Nombre de lieux",       0,  completionProvider.getPlaces().getAll().size());

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail("GedcomException "+ ex.toString());
        }
    }


    /**
     * test ajout d'une naissance
     */
    @Test
    public void testAddMiscRecord() {
        try {
            RecordMisc record1 = new RecordMisc();
            record1.setFieldValue(FieldType.eventDate, "01/01/2012");
            record1.setFieldValue(FieldType.cote, "cote");
            record1.setFieldValue(FieldType.parish, "parish");
            record1.setFieldValue(FieldType.notary, "Notary");
            record1.setFieldValue(Record.FieldType.eventType, "eventname");
            record1.setFieldValue(FieldType.generalComment, "generalcomment");
            record1.setFieldValue(FieldType.freeComment,"photo");
            record1.setIndi("indifirstname", "indilastname", "M", "30y", "01/01/1990", "indiplace", "indiBirthAddress", "indioccupation", "indiResidence", "indiAddress", "indicomment");
            record1.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indiMarriedResidence", "indiMarriedAddress", "indimarriedcomment", "false");
            record1.setIndiFather("indifathername", "indifatherlastname", "indifatheroccupation", "indiFatherResidence", "indiFatherAddress","indifathercomment", "false", "70y");
            record1.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indiMotherResidence", "indiMotherAddress", "indimothercomment", "false", "72y");
            record1.setWife("wifefirstname", "wifelastname", "F", "wifeage", "02/02/1992", "wifeplace", "wifeBirthAddress", "wifeoccupation", "wifeResidence", "wifeAddress", "wifecomment");
            record1.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifeMarriedResidence", "wifeMarriedAddress", "wifemarriedcomment", "false");
            record1.setWifeFather("wifefathername", "wifefatherlastname", "wifefatheroccupation", "wifeFatherResidence", "wiferFatherAddress", "wifefathercomment", "false", "70y");
            record1.setWifeMother("wifemothername", "wifemotherlastname", "wifemotheroccupation", "wifeMotherResidence", "wiferMotherAddress", "wifemothercomment", "false", "72y");
            record1.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record1.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record1.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record1.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

            RecordMisc record2 = new RecordMisc();
            record2.setFieldValue(FieldType.eventDate, "02/01/2012");
            record2.setFieldValue(FieldType.cote, "cote");
            record2.setFieldValue(FieldType.parish, "parish");
            record2.setFieldValue(FieldType.notary, "Notary2");
            record2.setFieldValue(Record.FieldType.eventType, "eventname");
            record2.setFieldValue(FieldType.generalComment, "generalcomment");
            record2.setFieldValue(FieldType.freeComment,"photo");
            record2.setIndi("indifirstname", "indilastname", "M", "30y", "01/01/1990", "indiplace", "indiBirthAddress", "indioccupation", "indiResidence", "indiAddress", "indicomment");
            record2.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indiMarriedResidence", "indiMarriedAddress", "indimarriedcomment", "false");
            record2.setIndiFather("indifathername", "indifatherlastname", "indifatheroccupation", "indiFatherResidence", "indiFatherAddress", "indifathercomment", "false", "70y");
            record2.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indiMotherResidence", "indiMotherAddress", "indimothercomment", "false", "72y");
            record2.setWife("wifefirstname", "wifelastname", "F", "wifeage", "02/02/1992", "wifeplace", "wifeBirthAddress", "wifeoccupation", "wifeResidence", "wifeAddress", "wifecomment");
            record2.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifeMarriedResidence", "wifeMarriedAddress", "wifemarriedcomment", "false");
            record2.setWifeFather("wifefathername", "wifefatherlastname", "wifefatheroccupation", "wifeFatherResidence", "wiferFatherAddress", "wifefathercomment", "false", "70y");
            record2.setWifeMother("wifemothername", "wifemotherlastname", "wifemotheroccupation", "wifeMotherResidence", "wiferMotherAddress", "wifemothercomment", "false", "72y");
            record2.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record2.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record2.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record2.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

            RecordMisc record3 = new RecordMisc();
            record3.setFieldValue(FieldType.eventDate, "03/01/2012");
            record3.setFieldValue(FieldType.cote, "cote");
            record3.setFieldValue(FieldType.parish, "parish");
            record3.setFieldValue(FieldType.notary, "Notary2");
            record3.setFieldValue(Record.FieldType.eventType, "eventname");
            record3.setFieldValue(FieldType.generalComment, "generalcomment");
            record3.setFieldValue(FieldType.freeComment,"photo");
            record3.setIndi("indifirstname", "indilastname", "M", "30y", "01/01/1990", "indiplace", "indiBirthAddress", "indioccupation", "indiResidence", "indiAddress", "indicomment");
            record3.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indiMarriedResidence", "indiMarriedAddress", "indimarriedcomment", "false");
            record3.setIndiFather("indifathername", "indifatherlastname", "indifatheroccupation", "indiFatherResidence", "indiFatherAddress", "indifathercomment", "false", "70y");
            record3.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indiMotherResidence", "indiMotherAddress", "indimothercomment", "false", "72y");
            record3.setWife("wifefirstname", "wifelastname", "F", "wifeage", "02/02/1992", "wifeplace", "wifeBirthAddress", "wifeoccupation", "wifeResidence", "wifeAddress", "wifecomment");
            record3.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifeMarriedResidence", "wifeMarriedAddress", "wifemarriedcomment", "false");
            record3.setWifeFather("wifefathername", "wifefatherlastname", "wifefatheroccupation", "wifeFatherResidence", "wiferFatherAddress", "wifefathercomment", "false", "70y");
            record3.setWifeMother("wifemothername", "wifemotherlastname", "wifemotheroccupation", "wifeMotherResidence", "wiferMotherAddress", "wifemothercomment", "false", "72y");
            record3.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record3.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record3.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record3.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

            CompletionProvider completionProvider = new CompletionProvider();
            completionProvider.addRecord(record1);
            completionProvider.addRecord(record2);
            completionProvider.addRecord(record3);
            // je verifie que les données ont bien été ajoutées
            assertEquals("Nombre de prenoms",     12,  completionProvider.getFirstNames().getAll().size());
            assertEquals("Nombre de noms",        12,  completionProvider.getLastNames().getAll().size());
            assertEquals("Nombre de professions", 12,  completionProvider.getOccupations().getAll().size());
            assertEquals("Nombre de lieux",       10,  completionProvider.getPlaces().getAll().size());
            assertEquals("Nombre de notaires",     2,  completionProvider.getNotaries().getAll().size());

            completionProvider.removeRecord(record3);
            assertEquals("Nombre de prenoms",     12,  completionProvider.getFirstNames().getAll().size());
            assertEquals("Nombre de noms",        12,  completionProvider.getLastNames().getAll().size());
            assertEquals("Nombre de professions", 12,  completionProvider.getOccupations().getAll().size());
            assertEquals("Nombre de lieux",       10,  completionProvider.getPlaces().getAll().size());
            assertEquals("Nombre de notaires",    2,   completionProvider.getNotaries().getAll().size());

            completionProvider.removeRecord(record2);
            assertEquals("Nombre de prenoms",     12,  completionProvider.getFirstNames().getAll().size());
            assertEquals("Nombre de noms",        12,  completionProvider.getLastNames().getAll().size());
            assertEquals("Nombre de professions", 12,  completionProvider.getOccupations().getAll().size());
            assertEquals("Nombre de lieux",       10,  completionProvider.getPlaces().getAll().size());
            assertEquals("Nombre de notaires",    1,   completionProvider.getNotaries().getAll().size());

            completionProvider.removeRecord(record1);
            assertEquals("Nombre de prenoms",     0,  completionProvider.getFirstNames().getAll().size());
            assertEquals("Nombre de noms",        0,  completionProvider.getLastNames().getAll().size());
            assertEquals("Nombre de professions", 0,  completionProvider.getOccupations().getAll().size());
            assertEquals("Nombre de lieux",       0,  completionProvider.getPlaces().getAll().size());
            assertEquals("Nombre de notaires",    0,  completionProvider.getNotaries().getAll().size());

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail("GedcomException "+ ex.toString());
        }
    }

    /**
     * Test of addGedcomCompletion method, of class CompletionProvider.
     */
    @Test
    public void testAddGedcom() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            CompletionProvider completionProvider = new CompletionProvider();
            completionProvider.addGedcomCompletion(gedcom);
            // je verifie que les données ont bien été ajoutées
            assertEquals("Nombre de prenoms", 7,  completionProvider.getFirstNames().getAll().size());
            assertEquals("Nombre de noms", 2,  completionProvider.getLastNames().getAll().size());
            assertEquals("Nombre de professions", 1,  completionProvider.getOccupations().getAll().size());
        } catch (Exception ex) {
            fail("GedcomException "+ ex.toString());
        }

    }

    /**
     * test exlusion de prenom et de nom
     */
    @Test
    public void testExludeFirstName() {
        try {
            RecordBirth record = new RecordBirth();
            record.setFieldValue(FieldType.eventDate, "01/01/2000");
            record.setFieldValue(FieldType.cote, "cote");
            record.setFieldValue(FieldType.freeComment,  "photo");
            record.setIndi("OneFirstName", "FATHERLASTNAME", "F", "", "", "indiBirthplace", "indiBirthAddress", "indioccupation", "indiResidence", "indiAddress", "indicomment");
            record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "indiFatherResidence", "indiFatherAddress", "comment", "dead", "70y");
            record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "indiMotherAddress", "comment", "dead", "72y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
            record.setFieldValue(FieldType.generalComment, "generalcomment");

            ArrayList<String> excludedFirstNameList = new ArrayList<String>();
            boolean add1 = excludedFirstNameList.add("Motherfirstname");
            boolean add2 = excludedFirstNameList.add("w1firstname");

            ArrayList<String> excludedLastNameList = new ArrayList<String>();
            boolean add3 = excludedLastNameList.add("w3lastname");

            ArrayList<String> excludedOccupationList = new ArrayList<String>();
            boolean add4 = excludedOccupationList.add("w4occupation");

            CompletionProvider.saveExcludedCompletion(excludedFirstNameList, CompletionProvider.CompletionType.firstName);
            CompletionProvider.saveExcludedCompletion(excludedLastNameList, CompletionProvider.CompletionType.lastName);
            CompletionProvider.saveExcludedCompletion(excludedOccupationList, CompletionProvider.CompletionType.occupation);

            CompletionProvider completionProvider = new CompletionProvider();
            completionProvider.addRecord(record);
            // je verifie que les données ont bien été ajoutées
            assertEquals("Nombre de prenoms 7-2", 5,  completionProvider.getFirstNames().getIncluded().size());
            assertEquals("Nombre de noms  6-1",   5,  completionProvider.getLastNames().getIncluded().size());
            assertEquals("Nombre de professions 7-1", 6,  completionProvider.getOccupations().getIncluded().size());

            completionProvider.removeRecord(record);
            assertEquals("Nombre de prenoms",     0,  completionProvider.getFirstNames().getAll().size());
            assertEquals("Nombre de noms",        0,  completionProvider.getLastNames().getAll().size());
            assertEquals("Nombre de professions", 0,  completionProvider.getOccupations().getAll().size());
            assertEquals("Nombre de prenoms inclues",     0,  completionProvider.getFirstNames().getIncluded().size());
            assertEquals("Nombre de noms inclues",        0,  completionProvider.getLastNames().getIncluded().size());
            assertEquals("Nombre de professions inclues", 0,  completionProvider.getOccupations().getIncluded().size());

            // je nettoie les liste des valeurs exclues
            excludedFirstNameList.clear();
            excludedLastNameList.clear();
            excludedOccupationList.clear();

            CompletionProvider.saveExcludedCompletion(excludedFirstNameList, CompletionProvider.CompletionType.firstName);
            CompletionProvider.saveExcludedCompletion(excludedLastNameList, CompletionProvider.CompletionType.lastName);
            CompletionProvider.saveExcludedCompletion(excludedOccupationList, CompletionProvider.CompletionType.occupation);
            completionProvider.refreshExcludeCompletion(CompletionProvider.CompletionType.firstName);
            completionProvider.refreshExcludeCompletion(CompletionProvider.CompletionType.lastName);
            completionProvider.refreshExcludeCompletion(CompletionProvider.CompletionType.occupation);

            completionProvider.addRecord(record);
            // je verifie que les données ont bien été ajoutées
            assertEquals("Nombre de prenoms 7", 7,  completionProvider.getFirstNames().getIncluded().size());
            assertEquals("Nombre de noms  6",   6,  completionProvider.getLastNames().getIncluded().size());
            assertEquals("Nombre de professions", 7,  completionProvider.getOccupations().getIncluded().size());

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail("GedcomException "+ ex.toString());
        }
    }

    /**
     * Test of addGedcomCompletion method, of class CompletionProvider.
     */
    @Test
    public void testSortFirstName() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            CompletionProvider completionProvider = new CompletionProvider();
            completionProvider.addGedcomCompletion(gedcom);

            RecordBirth record = new RecordBirth();
            record.setFieldValue(FieldType.eventDate, "01/01/2000");
            record.setFieldValue(FieldType.cote, "cote");
            record.setFieldValue(FieldType.freeComment,  "photo");
            record.setIndi("OneFirstName Second", "FATHERLASTNAME", "F", "", "", "indiBirthplace", "indiBirthAddress", "indioccupation", "indiResidence", "indiAddress", "indicomment");
            record.setIndiFather("Sansfamille1 second", "FATHERLASTNAME", "fatherOccupation", "indiFatherResidence", "indiFatherAddress", "comment", "dead", "70y");
            record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "indiMotherAddress", "comment", "dead", "72y");
            completionProvider.addRecord(record);

            // je verifie que les prénoms composés sont dans l'ordre alphabétique
            assertEquals("liste",
                    "[cousin, Fatherfirstname, Motherfirstname, One First Name, OneFirstName Second, sansfamille1, Sansfamille1 second, Three First Name, Two-First-Name]",
                    completionProvider.getFirstNames().getIncluded().toString() );
        } catch (Exception ex) {
            fail("GedcomException "+ ex.toString());
        }
    }
}
