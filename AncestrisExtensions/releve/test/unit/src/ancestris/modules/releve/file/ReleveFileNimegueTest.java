package ancestris.modules.releve.file;

import ancestris.modules.releve.TestUtility;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class ReleveFileNimegueTest extends TestCase {
    
    public ReleveFileNimegueTest(String testName) {
        super(testName);
    }

    /**
     * Test of isValidFile method, of class ReleveFileNimegue.
     */
    public void testIsValidFile() {
    }

    /**
     * Test of loadFile method, of class ReleveFileNimegue.
     */
    public void testLoadFile() throws Exception {
        try {
            File file ;
            String data;
            FileBuffer sb;

            data = "";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("fichier vide" , sb.getError().length(), 0);
            file.delete();

            data = "NIMEGUEV3;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("Pas de type d'acte",  sb.getError().length()> 0, true);
            file.delete();

            data = "NIMEGUEV3;;;;;X;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("type acte=X",  sb.getError().length()> 0, true);
            file.delete();

            data = "NIMEGUEV3;;;;;N;;;;;";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("Nb champs incorrection",  sb.getError().length()> 0, true);
            file.delete();

            data = "NIMEGUEV3;;;;;N;;;;;;;;;;;;;;;;;;;;;;;;12";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("Naissance minimal sans separateur supplémentaire", sb.getError().length()> 0, true);
            file.delete();

            data = "NIMEGUEV3;;;;;N;;;;;;;;;;;;;;;;;;;;;;;;12;";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("Naissance minimal",  sb.getError().toString(), "");
            file.delete();


            data = "NIMEGUEV3;;;;;M;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;13;";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("Mariage minimal",  sb.getError().toString(), "");
            file.delete();

            data = "NIMEGUEV3;;;;;D;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;14;";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("Deces minimal",  sb.getError().toString(), "");
            file.delete();

            data = "NIMEGUEV3;;;;;V;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;15;";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("Divers minimal",  sb.getError().toString(), "");
            file.delete();

            data = "NIMEGUEV3;09202;Montégut-Plantaurel;09;Ariège;V;01/03/1818;;;;PM;PM;CROUZET;Pierre;M;Pailhès (09);          ;23;;;;;;CROUZET;Guilhaume;;;BARRÈRE;Jeanne;;;LOUBIÈRES;Pauline;F;Montégut;          ;34;;;;;;LOUBIÈRES;Jacques;;;ARMINGAUD;Anne;;;;;;;;;;;;;;;;1;";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("Divers exemple",  sb.getError().toString(), "");
            file.delete();

        } catch (IOException ex) {
            fail("IOException "+ ex.toString());
        }
    }

    /**
     * Test of saveFile method, of class ReleveFileNimegue.
     */
    public void testSaveFile() {
    }

}
