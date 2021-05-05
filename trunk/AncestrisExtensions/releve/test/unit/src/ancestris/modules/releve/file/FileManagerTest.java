package ancestris.modules.releve.file;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.RecordBirth;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Michel
 */
public class FileManagerTest  {


    /**
     * test splitCSV
     */
    @Test
    public void testLineSplitCSV() {
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

     /**
     * test saveFile avec un repertoire de fichier inexistant
     */
    @Test
    public void testLoadFileOneLine() {
        File file = null;
        try {
            String data;
            data = "ANCESTRISV2;;;;;;;M;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            FileBuffer fb = FileManager.loadFile(file);
            file.delete();
            assertEquals("Mariage minimal", "", fb.getError());

        } catch (Exception ex) {
            fail("IOException "+ ex.toString());
            if (file!= null) {
                file.delete();
            }
        }
    }

    /**
     * test saveFile avec un repertoire de fichier inexistant
     */
    @Test
    public void testSaveFile() {
        File saveFile = new File("xxxx/xxx.txt");

        DataManager dateManager = new DataManager();
        dateManager.setPlace("cityname","citycode","county","state","country");
        RecordBirth record = new RecordBirth();
        dateManager.addRecord(record);

        StringBuilder sb = FileManager.saveFile(dateManager, dateManager, saveFile, FileManager.FileFormat.FILE_TYPE_ANCESTRISV5);
        assertEquals("save result", true, sb.toString().contains("java.io.FileNotFoundException"));

    }

}
