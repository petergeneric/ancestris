package ancestris.modules.releve.model;
import ancestris.modules.releve.TestUtility;
import genj.gedcom.Gedcom;

import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class DataManagerTest extends TestCase {
    
    public DataManagerTest(String testName) {
        super(testName);
    }

        /**
     * Test of removeGedcomCompletion method, of class CompletionProvider.
     */
    public void testAddGedcom() {
       try {
            DataManager dataManager = new DataManager();

            RecordBirth record = new RecordBirth();
            record.setEventDate("01/01/2000");
            record.setCote("cote");
            record.setFreeComment("photo");
            record.setIndi("OneFirstName", "FATHERLASTNAME_GEDCOM", "F", "", "", "indiBirthplace", "indioccupation", "indiResidence", "indicomment");
            record.setIndiFather("Fatherfirstname", "FATHERLASTNAME_GEDCOM", "fatherOccupation", "indiFatherResidence", "comment", "dead", "70y");
            record.setIndiMother("Motherfirstname", "MOTHERLASTNAME_GEDCOM", "motherOccupation", "indiMotherResidence", "comment", "dead", "72y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
            record.setGeneralComment("generalcomment");

            dataManager.addRecord(record);
           // je verifie que les données ont bien été supprimées
            assertEquals("Nombre de prenoms apres suppression", 7, dataManager.getCompletionProvider().getFirstNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de noms apres suppression", 6, dataManager.getCompletionProvider().getLastNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de professions apres suppression", 6, dataManager.getCompletionProvider().getOccupations(CompletionProvider.IncludeFilter.ALL).size());

            Gedcom gedcom = TestUtility.createGedcom2();
            dataManager.addGedcomCompletion(gedcom);

            assertEquals("Nombre de prenoms", 7+4, dataManager.getCompletionProvider().getFirstNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de noms", 6+2, dataManager.getCompletionProvider().getLastNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de professions", 6+2, dataManager.getCompletionProvider().getOccupations(CompletionProvider.IncludeFilter.ALL).size());

            dataManager.removeGedcomCompletion();
            assertEquals("Nombre de prenoms apres suppression", 7, dataManager.getCompletionProvider().getFirstNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de noms apres suppression", 6, dataManager.getCompletionProvider().getLastNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de professions apres suppression", 6, dataManager.getCompletionProvider().getOccupations(CompletionProvider.IncludeFilter.ALL).size());

            dataManager.removeRecord(record);
            assertEquals("Nombre de prenoms apres suppression", 0, dataManager.getCompletionProvider().getFirstNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de noms apres suppression", 0, dataManager.getCompletionProvider().getLastNames(CompletionProvider.IncludeFilter.ALL).size());
            assertEquals("Nombre de professions apres suppression", 0, dataManager.getCompletionProvider().getOccupations(CompletionProvider.IncludeFilter.ALL).size());

        } catch (Exception ex) {
            fail("GedcomException " + ex.toString());
        }
    }



}
