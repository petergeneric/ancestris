package ancestris.modules.releve.file;

import java.io.BufferedReader;
import java.io.IOException;
import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class FileManagerTest extends TestCase {
    
   
    /**
     * Test of saveFile method, of class FileManager.
     */
    public void testLine() {
        char fieldSeparator = ';';
        String[] fields = { "aaa", "bbb;bb", "cc\"c\"", ""};
        FileManager.Line line = new FileManager.Line(fieldSeparator) ;

        for(int i=0; i<fields.length -1; i++) {
            line.appendCsvFn(fields[i]);
        }
        line.appendCsv(fields[fields.length -1]);

        String csvString = line.toString();

        String[] fields2 = null;
        try {
            fields2 = FileManager.Line.splitCSV(new BufferedReader(new java.io.StringReader(csvString)), fieldSeparator);
        } catch (IOException ex) {
            fail("IOException "+ ex.toString());
        }

        assertNotNull(fields2);
        assertEquals(fields.length,fields2.length);
        for(int i=0; i<fields.length; i++) {
            assertEquals("field "+i, fields[i], fields2[i]);
        }


    }

}
