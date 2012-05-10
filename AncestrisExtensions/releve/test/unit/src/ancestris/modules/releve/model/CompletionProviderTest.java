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
     * Test of addGedcomCompletion method, of class CompletionProvider.
     */
    public void testAddGedcom() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            CompletionProvider completionProvider = new CompletionProvider();
            completionProvider.addGedcomCompletion(gedcom);
            // je verifie que les données ont bien été ajoutées
            assertEquals("Nombre de prenoms", 5,  completionProvider.getFirstNames().size());
            assertEquals("Nombre de noms", 2,  completionProvider.getLastNames().size());
            assertEquals("Nombre de professions", 0,  completionProvider.getOccupations().size());

            

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
            assertEquals("Nombre de prenoms", 4, completionProvider.getFirstNames().size());
            assertEquals("Nombre de noms", 2, completionProvider.getLastNames().size());
            assertEquals("Nombre de professions", 2, completionProvider.getOccupations().size());

            completionProvider.removeGedcomCompletion(gedcom);
            // je verifie que les données ont bien été supprimées
            assertEquals("Nombre de prenoms apres suppression", 0, completionProvider.getFirstNames().size());
            assertEquals("Nombre de noms apres suppression", 0, completionProvider.getLastNames().size());
            assertEquals("Nombre de professions apres suppression", 0, completionProvider.getOccupations().size());


        } catch (GedcomException ex) {
            fail("GedcomException " + ex.toString());
        }
    }


}
