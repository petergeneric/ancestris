package ancestris.modules.releve.model;

import ancestris.modules.releve.TestUtility;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class CompletionProviderTest extends TestCase {
    
    public CompletionProviderTest(String testName) {
        super(testName);
    }

    /**
     * Test of addRecord method, of class CompletionProvider.
     */
    public void testAddRecord() {
    }

    /**
     * Test of removeRecord method, of class CompletionProvider.
     */
    public void testRemoveRecord_List() {
    }

    /**
     * Test of removeRecord method, of class CompletionProvider.
     */
    public void testRemoveRecord_Record() {
    }

    /**
     * Test of updateFirstName method, of class CompletionProvider.
     */
    public void testUpdateFirstName() {
    }

    /**
     * Test of updateLastName method, of class CompletionProvider.
     */
    public void testUpdateLastName() {
    }

    /**
     * Test of updateEventType method, of class CompletionProvider.
     */
    public void testUpdateEventType() {
    }

    /**
     * Test of getFirstNames method, of class CompletionProvider.
     */
    public void testGetFirstNames() {
    }

    /**
     * Test of getLastNames method, of class CompletionProvider.
     */
    public void testGetLastNames() {
    }

    /**
     * Test of getOccupations method, of class CompletionProvider.
     */
    public void testGetChoices() {
    }

    /**
     * Test of getEventTypes method, of class CompletionProvider.
     */
    public void testGetEventTypes() {
    }

    /**
     * Test of getPlaces method, of class CompletionProvider.
     */
    public void testGetPlaces() {
    }

    /**
     * Test of getLocale method, of class CompletionProvider.
     */
    public void testGetLocale() {
    }

    /**
     * Test of setLocale method, of class CompletionProvider.
     */
    public void testSetLocale() {
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
            assertEquals("Nombre de prenoms",  completionProvider.getFirstNames().size(), 4);
            assertEquals("Nombre de noms",  completionProvider.getLastNames().size(), 2);
            assertEquals("Nombre de professions",  completionProvider.getOccupations().size(), 0);

            

        } catch (GedcomException ex) {
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
            assertEquals("Nombre de prenoms", completionProvider.getFirstNames().size(), 4);
            assertEquals("Nombre de noms", completionProvider.getLastNames().size(), 2);
            assertEquals("Nombre de professions", completionProvider.getOccupations().size(), 2);

            completionProvider.removeGedcomCompletion(gedcom);
            // je verifie que les données ont bien été supprimées
            assertEquals("Nombre de prenoms apres suppression", completionProvider.getFirstNames().size(), 0);
            assertEquals("Nombre de noms apres suppression", completionProvider.getLastNames().size(), 0);
            assertEquals("Nombre de professions apres suppression", completionProvider.getOccupations().size(), 0);


        } catch (GedcomException ex) {
            fail("GedcomException " + ex.toString());
        }
    }


}
