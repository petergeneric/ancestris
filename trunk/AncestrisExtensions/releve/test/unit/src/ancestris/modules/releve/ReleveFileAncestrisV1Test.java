package ancestris.modules.releve;

import ancestris.modules.releve.file.FileBuffer;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.file.ReleveFileAncestrisV1;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class ReleveFileAncestrisV1Test extends TestCase {
    
    public ReleveFileAncestrisV1Test(String testName) {
        super(testName);
    }

    /**
     * Test of isValidFile method, of class ReleveFileAncestrisV1.
     */
    public void testIsValidFile() {
            String data;
            data= "";
            boolean isValid = ReleveFileAncestrisV1.isValidFile(data);
            assertEquals("ligne vide" , isValid, false);

            data = "ANCESTRISV1;;;;;;;;;;;;;;;;;;;;;;;;";
            isValid = ReleveFileAncestrisV1.isValidFile(data);
            assertEquals("ligne incomplete", isValid, false);

            data = "ANCESTRISV1;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            isValid = ReleveFileAncestrisV1.isValidFile(data);
            assertEquals("points virgules collés", isValid, true);

            data = "ANCESTRISV1; ; ; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            isValid = ReleveFileAncestrisV1.isValidFile(data);
            assertEquals(isValid, true);
            
        
    }

    
    /**
     * Test of loadFile method, of class ReleveFileAncestrisV1.
     */
    public void testLoadFile() throws Exception {
        try {
            File file ;
            String data;
            FileBuffer sb;

            data = "";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV1.loadFile(file);
            assertEquals("fichier vide" , sb.getError().length(), 0);
            file.delete();

            data = "ANCESTRISV1;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV1.loadFile(file);
            assertEquals("Pas de type d'acte",  sb.getError().toString(), "Line 1 Type d'acte inconnu \n");
            file.delete();

            data = "ANCESTRISV1;;;;;;;X;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV1.loadFile(file);
            assertEquals("type acte=X",  sb.getError().toString(), "Line 1 Type d'acte inconnu X\n");
            file.delete();

            data = "ANCESTRISV1;;;;;;;N;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV1.loadFile(file);
            assertEquals("Naissance minimal",  sb.getError().toString(), "");
            file.delete();

            data = "ANCESTRISV1;;;;;;;M;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV1.loadFile(file);
            assertEquals("Mariage minimal",  sb.getError().toString(), "");
            file.delete();

            data = "ANCESTRISV1;;;;;;;D;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV1.loadFile(file);
            assertEquals("Deces minimal",  sb.getError().toString(), "");
            file.delete();

            data = "ANCESTRISV1;;;;;;;V;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV1.loadFile(file);
            assertEquals("Divers minimal",  sb.getError().toString(), "");
            file.delete();

        } catch (IOException ex) {
            fail("IOException "+ ex.toString());
        }
    }

    /**
     * Test of saveFile method, of class ReleveFileAncestrisV1.
     */
    public void testSaveFile() {
    }

}
