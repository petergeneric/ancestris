package ancestris.modules.releve.model;
import ancestris.modules.releve.TestUtility;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;

import junit.framework.TestCase;
import org.junit.Test;

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
    @Test
    public void testAddGedcom() {
       try {
            DataManager dataManager = new DataManager();

            RecordBirth record = new RecordBirth();
            record.setEventDate("01/01/2000");
            record.setCote("cote");
            record.setFreeComment("photo");
            record.getIndi().set("OneFirstName", "FATHERLASTNAME_GEDCOM", "F", "", "", "indiBirthplace", "indiBirthAddress", "indioccupation", "indiResidence", "indiAddress", "indicomment");
            record.getIndi().setFather("Fatherfirstname", "FATHERLASTNAME_GEDCOM", "fatherOccupation", "indiFatherResidence", "indiFatherAddress", "comment", "dead", "70y");
            record.getIndi().setMother("Motherfirstname", "MOTHERLASTNAME_GEDCOM", "motherOccupation", "indiMotherResidence", "indiMotherAddress", "comment", "dead", "72y");
            record.getWitness1().setValue("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.getWitness2().setValue("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.getWitness3().setValue("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.getWitness4().setValue("w4firstname", "w4lastname", "w4occupation", "w4comment");
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

        } catch (GedcomException ex) {
            fail("GedcomException " + ex.toString());
        }
    }



}
