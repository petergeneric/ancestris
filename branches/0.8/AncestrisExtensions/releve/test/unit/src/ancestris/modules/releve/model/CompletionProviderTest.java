package ancestris.modules.releve.model;

import ancestris.modules.releve.TestUtility;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import java.util.ArrayList;
import junit.framework.TestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author Michel
 */
public class CompletionProviderTest extends TestCase {
    
    public CompletionProviderTest(String testName) {
        super(testName);
    }


    /**
     * test ajout d'une naissance
     */
    public void testAddBirthRecord() {
        try {
            RecordBirth record = new RecordBirth();
            record.setEventDate("01/01/2000");
            record.setCote("cote");
            record.setFreeComment("photo");
            record.setIndi("OneFirstName", "FATHERLASTNAME", "F", "", "", "indiBirthplace", "indioccupation", "indiResidence", "indicomment");
            record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "indiFatherResidence", "comment", "dead", "70y");
            record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "comment", "dead", "72y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
            record.setGeneralComment("generalcomment");
            
            CompletionProvider completionProvider = new CompletionProvider();
            completionProvider.addRecord(record);
            // je verifie que les données ont bien été ajoutées
            assertEquals("Nombre de prenoms",     7,  completionProvider.getFirstNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de noms",        6,  completionProvider.getLastNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de professions", 6,  completionProvider.getOccupations(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de lieux",       3,  completionProvider.getPlaces(CompletionProvider.IncludeFilter.ALL).size());

            completionProvider.removeRecord(record);
            assertEquals("Nombre de prenoms",     0,  completionProvider.getFirstNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de noms",        0,  completionProvider.getLastNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de professions", 0,  completionProvider.getOccupations(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de lieux",       0,  completionProvider.getPlaces(CompletionProvider.IncludeFilter.ALL).size());
            
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail("GedcomException "+ ex.toString());
        }
    }


    /**
     * test ajout d'une naissance
     */
    public void testAddMiscRecord() {
        try {
            RecordMisc record1 = new RecordMisc();
            record1.setEventDate("01/01/2012");
            record1.setCote("cote");
            record1.setParish("parish");
            record1.setNotary("Notary");
            record1.setEventType("eventname");
            record1.setGeneralComment("generalcomment");
            record1.setFreeComment("photo");
            record1.setIndi("indifirstname", "indilastname", "M", "30y", "01/01/1990", "indiplace", "indioccupation", "indiResidence", "indicomment");
            record1.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indiMarriedResidence", "indimarriedcomment", "false");
            record1.setIndiFather("indifathername", "indifatherlastname", "indifatheroccupation", "indiFatherResidence", "indifathercomment", "false", "70y");
            record1.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indiMotherResidence", "indimothercomment", "false", "72y");
            record1.setWife("wifefirstname", "wifelastname", "F", "wifeage", "02/02/1992", "wifeplace", "wifeoccupation", "wifeResidence", "wifecomment");
            record1.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifeMarriedResidence", "wifemarriedcomment", "false");
            record1.setWifeFather("wifefathername", "wifefatherlastname", "wifefatheroccupation", "wifeFatherResidence", "wifefathercomment", "false", "70y");
            record1.setWifeMother("wifemothername", "wifemotherlastname", "wifemotheroccupation", "wifeMotherResidence", "wifemothercomment", "false", "72y");
            record1.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record1.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record1.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record1.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

            RecordMisc record2 = new RecordMisc();
            record2.setEventDate("02/01/2012");
            record2.setCote("cote");
            record2.setParish("parish");
            record2.setNotary("Notary2");
            record2.setEventType("eventname");
            record2.setGeneralComment("generalcomment");
            record2.setFreeComment("photo");
            record2.setIndi("indifirstname", "indilastname", "M", "30y", "01/01/1990", "indiplace", "indioccupation", "indiResidence", "indicomment");
            record2.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indiMarriedResidence", "indimarriedcomment", "false");
            record2.setIndiFather("indifathername", "indifatherlastname", "indifatheroccupation", "indiFatherResidence", "indifathercomment", "false", "70y");
            record2.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indiMotherResidence", "indimothercomment", "false", "72y");
            record2.setWife("wifefirstname", "wifelastname", "F", "wifeage", "02/02/1992", "wifeplace", "wifeoccupation", "wifeResidence", "wifecomment");
            record2.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifeMarriedResidence", "wifemarriedcomment", "false");
            record2.setWifeFather("wifefathername", "wifefatherlastname", "wifefatheroccupation", "wifeFatherResidence", "wifefathercomment", "false", "70y");
            record2.setWifeMother("wifemothername", "wifemotherlastname", "wifemotheroccupation", "wifeMotherResidence", "wifemothercomment", "false", "72y");
            record2.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record2.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record2.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record2.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

            RecordMisc record3 = new RecordMisc();
            record3.setEventDate("03/01/2012");
            record3.setCote("cote");
            record3.setParish("parish");
            record3.setNotary("Notary2");
            record3.setEventType("eventname");
            record3.setGeneralComment("generalcomment");
            record3.setFreeComment("photo");
            record3.setIndi("indifirstname", "indilastname", "M", "30y", "01/01/1990", "indiplace", "indioccupation", "indiResidence", "indicomment");
            record3.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indiMarriedResidence", "indimarriedcomment", "false");
            record3.setIndiFather("indifathername", "indifatherlastname", "indifatheroccupation", "indiFatherResidence", "indifathercomment", "false", "70y");
            record3.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indiMotherResidence", "indimothercomment", "false", "72y");
            record3.setWife("wifefirstname", "wifelastname", "F", "wifeage", "02/02/1992", "wifeplace", "wifeoccupation", "wifeResidence", "wifecomment");
            record3.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifeMarriedResidence", "wifemarriedcomment", "false");
            record3.setWifeFather("wifefathername", "wifefatherlastname", "wifefatheroccupation", "wifeFatherResidence", "wifefathercomment", "false", "70y");
            record3.setWifeMother("wifemothername", "wifemotherlastname", "wifemotheroccupation", "wifeMotherResidence", "wifemothercomment", "false", "72y");
            record3.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record3.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record3.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record3.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

            CompletionProvider completionProvider = new CompletionProvider();
            completionProvider.addRecord(record1);
            completionProvider.addRecord(record2);
            completionProvider.addRecord(record3);
            // je verifie que les données ont bien été ajoutées
            assertEquals("Nombre de prenoms",     12,  completionProvider.getFirstNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de noms",        12,  completionProvider.getLastNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de professions", 12,  completionProvider.getOccupations(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de lieux",       10,  completionProvider.getPlaces(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de notaires",     2,  completionProvider.getNotaries(CompletionProvider.IncludeFilter.ALL).size());

            completionProvider.removeRecord(record3);
            assertEquals("Nombre de prenoms",     12,  completionProvider.getFirstNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de noms",        12,  completionProvider.getLastNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de professions", 12,  completionProvider.getOccupations(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de lieux",       10,  completionProvider.getPlaces(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de notaires",    2,  completionProvider.getNotaries(CompletionProvider.IncludeFilter.ALL).size());

            completionProvider.removeRecord(record2);
            assertEquals("Nombre de prenoms",     12,  completionProvider.getFirstNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de noms",        12,  completionProvider.getLastNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de professions", 12,  completionProvider.getOccupations(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de lieux",       10,  completionProvider.getPlaces(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de notaires",    1,  completionProvider.getNotaries(CompletionProvider.IncludeFilter.ALL).size());

            completionProvider.removeRecord(record1);
            assertEquals("Nombre de prenoms",     0,  completionProvider.getFirstNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de noms",        0,  completionProvider.getLastNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de professions", 0,  completionProvider.getOccupations(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de lieux",       0,  completionProvider.getPlaces(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de notaires",    0,  completionProvider.getNotaries(CompletionProvider.IncludeFilter.ALL).size());

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail("GedcomException "+ ex.toString());
        }
    }
   
    /**
     * Test of addGedcomCompletion method, of class CompletionProvider.
     */
    public void testAddGedcom() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            CompletionProvider completionProvider = new CompletionProvider();
            completionProvider.addGedcomCompletion(gedcom);
            // je verifie que les données ont bien été ajoutées
            assertEquals("Nombre de prenoms", 7,  completionProvider.getFirstNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de noms", 2,  completionProvider.getLastNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de professions", 1,  completionProvider.getOccupations(CompletionProvider.IncludeFilter.ALL).size());
        } catch (Exception ex) {
            fail("GedcomException "+ ex.toString());
        }

    }
    
    /**
     * test exlusion de prenom et de nom
     */
    public void testExludeFirstName() {
        try {
            RecordBirth record = new RecordBirth();
            record.setEventDate("01/01/2000");
            record.setCote("cote");
            record.setFreeComment("photo");
            record.setIndi("OneFirstName", "FATHERLASTNAME", "F", "", "", "indiBirthplace", "indioccupation", "indiResidence", "indicomment");
            record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "indiFatherResidence", "comment", "dead", "70y");
            record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "comment", "dead", "72y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
            record.setGeneralComment("generalcomment");

            ArrayList<String> excludedFirstNameList = new ArrayList<String>();
            boolean add1 = excludedFirstNameList.add("Motherfirstname");
            boolean add2 = excludedFirstNameList.add("w1firstname");

            ArrayList<String> excludedLastNameList = new ArrayList<String>();
            boolean add3 = excludedLastNameList.add("w3lastname");

            CompletionProvider.saveExcludedCompletion(excludedFirstNameList, CompletionProvider.CompletionType.firstName);
            CompletionProvider.saveExcludedCompletion(excludedLastNameList, CompletionProvider.CompletionType.lastName);

            CompletionProvider completionProvider = new CompletionProvider();
            completionProvider.addRecord(record);
            // je verifie que les données ont bien été ajoutées
            assertEquals("Nombre de prenoms 7-2", 5,  completionProvider.getFirstNames(CompletionProvider.IncludeFilter.INCLUDED).size());
            assertEquals("Nombre de noms  6-1",   5,  completionProvider.getLastNames(CompletionProvider.IncludeFilter.INCLUDED).size());
            assertEquals("Nombre de professions", 6,  completionProvider.getOccupations(CompletionProvider.IncludeFilter.INCLUDED).size());

            completionProvider.removeRecord(record);
            assertEquals("Nombre de prenoms",     0,  completionProvider.getFirstNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de noms",        0,  completionProvider.getLastNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de professions", 0,  completionProvider.getOccupations(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de prenoms inclues",     0,  completionProvider.getFirstNames(CompletionProvider.IncludeFilter.INCLUDED).size());
            assertEquals("Nombre de noms inclues",        0,  completionProvider.getLastNames(CompletionProvider.IncludeFilter.INCLUDED).size());
            assertEquals("Nombre de professions inclues", 0,  completionProvider.getOccupations(CompletionProvider.IncludeFilter.INCLUDED).size());

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail("GedcomException "+ ex.toString());
        }
    }

    /**
     * Test of addGedcomCompletion method, of class CompletionProvider.
     */
    public void testSortFirstName() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            CompletionProvider completionProvider = new CompletionProvider();
            completionProvider.addGedcomCompletion(gedcom);

            RecordBirth record = new RecordBirth();
            record.setEventDate("01/01/2000");
            record.setCote("cote");
            record.setFreeComment("photo");
            record.setIndi("OneFirstName Second", "FATHERLASTNAME", "F", "", "", "indiBirthplace", "indioccupation", "indiResidence", "indicomment");
            record.setIndiFather("Sansfamille1 second", "FATHERLASTNAME", "fatherOccupation", "indiFatherResidence", "comment", "dead", "70y");
            record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "comment", "dead", "72y");
            completionProvider.addRecord(record);
            
            // je verifie que les prénoms composés sont dans l'ordre alphabétique
            assertEquals("liste", 
                    "[cousin, Fatherfirstname, OneFirstName, OneFirstName Second, sansfamille1, Sansfamille1 second, ThreeFirstName, TwoFirstName]",
                    completionProvider.getFirstNames(CompletionProvider.IncludeFilter.INCLUDED).toString() );
        } catch (Exception ex) {
            fail("GedcomException "+ ex.toString());
        }
    }
}
