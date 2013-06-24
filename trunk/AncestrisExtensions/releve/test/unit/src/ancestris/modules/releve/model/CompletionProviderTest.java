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
            assertEquals("Nombre de prenoms",     7,  completionProvider.getFirstNames().size());
            assertEquals("Nombre de noms",        6,  completionProvider.getLastNames().size());
            assertEquals("Nombre de professions", 6,  completionProvider.getOccupations().size());
            assertEquals("Nombre de lieux",       3,  completionProvider.getPlaces().size());

            completionProvider.removeRecord(record);
            assertEquals("Nombre de prenoms",     0,  completionProvider.getFirstNames().size());
            assertEquals("Nombre de noms",        0,  completionProvider.getLastNames().size());
            assertEquals("Nombre de professions", 0,  completionProvider.getOccupations().size());
            assertEquals("Nombre de lieux",       0,  completionProvider.getPlaces().size());
            
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
            RecordMisc record = new RecordMisc();
            record.setEventDate("29/02/2012");
            record.setCote("cote");
            record.setParish("parish");
            record.setNotary("Notary");
            record.setEventType("eventname");
            record.setGeneralComment("generalcomment");
            record.setFreeComment("photo");
            record.setIndi("indifirstname", "indilastname", "M", "30y", "01/01/1990", "indiplace", "indioccupation", "indiResidence", "indicomment");
            record.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indiMarriedResidence", "indimarriedcomment", "indimarrieddead");
            record.setIndiFather("indifathername", "indifatherlastname", "indifatheroccupation", "indiFatherResidence", "indifathercomment", "indifatherdead", "70y");
            record.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indiMotherResidence", "indimothercomment", "indimotherdead", "72y");
            record.setWife("wifefirstname", "wifelastname", "F", "wifeage", "02/02/1992", "wifeplace", "wifeoccupation", "wifeResidence", "wifecomment");
            record.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifeMarriedResidence", "wifemarriedcomment", "wifemarrieddead");
            record.setWifeFather("wifefathername", "wifefatherlastname", "wifefatheroccupation", "wifeFatherResidence", "wifefathercomment", "wifefatherdead", "70y");
            record.setWifeMother("wifemothername", "wifemotherlastname", "wifemotheroccupation", "wifeMotherResidence", "wifemothercomment", "wifemotherdead", "72y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

            CompletionProvider completionProvider = new CompletionProvider();
            completionProvider.addRecord(record);
            // je verifie que les données ont bien été ajoutées
            assertEquals("Nombre de prenoms",     12,  completionProvider.getFirstNames().size());
            assertEquals("Nombre de noms",        12,  completionProvider.getLastNames().size());
            assertEquals("Nombre de professions", 12,  completionProvider.getOccupations().size());
            assertEquals("Nombre de lieux",       10,  completionProvider.getPlaces().size());

            completionProvider.removeRecord(record);
            assertEquals("Nombre de prenoms",     0,  completionProvider.getFirstNames().size());
            assertEquals("Nombre de noms",        0,  completionProvider.getLastNames().size());
            assertEquals("Nombre de professions", 0,  completionProvider.getOccupations().size());
            assertEquals("Nombre de lieux",       0,  completionProvider.getPlaces().size());

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
            assertEquals("Nombre de prenoms", 7,  completionProvider.getFirstNames().size());
            assertEquals("Nombre de noms", 2,  completionProvider.getLastNames().size());
            assertEquals("Nombre de professions", 1,  completionProvider.getOccupations().size());
        } catch (Exception ex) {
            fail("GedcomException "+ ex.toString());
        }

    }

    /**
     * Test of removeGedcomCompletion method, of class CompletionProvider.
     */
    public void testRemoveGedcom() {
            try {
            Gedcom gedcom = TestUtility.createGedcom2();
            CompletionProvider completionProvider = new CompletionProvider();
            completionProvider.addGedcomCompletion(gedcom);
            // je verifie que les données ont bien été ajoutées
            assertEquals("Nombre de prenoms", 4, completionProvider.getFirstNames().size());
            assertEquals("Nombre de noms", 2, completionProvider.getLastNames().size());
            assertEquals("Nombre de professions", 2, completionProvider.getOccupations().size());

            completionProvider.removeGedcomCompletion();
            // je verifie que les données ont bien été supprimées
            assertEquals("Nombre de prenoms apres suppression", 0, completionProvider.getFirstNames().size());
            assertEquals("Nombre de noms apres suppression", 0, completionProvider.getLastNames().size());
            assertEquals("Nombre de professions apres suppression", 0, completionProvider.getOccupations().size());


        } catch (GedcomException ex) {
            fail("GedcomException " + ex.toString());
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
            assertEquals("Nombre de prenoms 7-2", 5,  completionProvider.getFirstNames().size());
            assertEquals("Nombre de noms  6-1",   5,  completionProvider.getLastNames().size());
            assertEquals("Nombre de professions", 6,  completionProvider.getOccupations().size());

            completionProvider.removeRecord(record);
            assertEquals("Nombre de prenoms",     0,  completionProvider.getFirstNames().size());
            assertEquals("Nombre de noms",        0,  completionProvider.getLastNames().size());
            assertEquals("Nombre de professions", 0,  completionProvider.getOccupations().size());

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail("GedcomException "+ ex.toString());
        }
    }


}
