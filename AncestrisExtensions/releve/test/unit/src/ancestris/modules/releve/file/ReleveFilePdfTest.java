package ancestris.modules.releve.file;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Record.RecordType;
import ancestris.modules.releve.model.RecordMisc;
import genj.fo.Document;
import genj.fo.Format;
import genj.fo.PDFFormat;
import java.io.File;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.openide.util.Exceptions;

/**
 *
 * @author Michel
 */
public class ReleveFilePdfTest {


    /**
     * Test of saveFile method, of class ReleveFileEgmt.
     */
    @Test
    public void testSaveFileMisc() {
        File file = new File(System.getProperty("user.home") + File.separator + "testpdf.PDF");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("");

        RecordMisc misc = TestUtility.getRecordMisc();
        dataManager.addRecord(misc);
        //dataManager.addRecord(misc);
        StringBuilder sb = ReleveFilePdf.saveFile(dataManager, dataManager.getDataModel(), RecordType.MISC, file, false);
        assertEquals("verify save error", 0, sb.length());

        file.delete();

    }
    private final static String ROW_FORMAT_HEADER1 = "font-size=larger,background-color=#00ccff,font-weight=bold";
    private final static String FORMAT_HEADER2 = "font-size=large,background-color=#33ffff,font-weight=bold";
    private final static String FORMAT_HEADER3 = "background-color=#ffffcc,font-weight=bold";
    private final static String FORMAT_HEADER3_TODO = "background-color=#99cccc,font-weight=bold";
    private final static String FORMAT_HEADER4 = "background-color=#ffffcc";
    private final static String FORMAT_EMPHASIS = "font-weight=italic";
    private final static String FORMAT_STRONG = "font-weight=bold";

    @Test
    public void testPDF() {

        File file = new File(System.getProperty("user.home") + File.separator + "testpdfdocformat.PDF");
        Format format = new PDFFormat();
        Document doc = new Document("Mon Titre");

        doc.addText("texte 1  large", ROW_FORMAT_HEADER1);
        doc.nextParagraph();
        doc.addText("texte 2 xx-small", FORMAT_HEADER2);
        doc.nextParagraph("start-indent=60pt");
        doc.addText("texte 3 small");
        doc.nextParagraph();
        doc.addText("texte 3 small");
        doc.nextParagraph("start-indent=120pt");
        doc.addText("texte 3 small");
        doc.nextParagraph();
        doc.addText("texte 3 small");
        doc.nextParagraph("family-font=Verdana");
        doc.addText("texte 3 police small", "font-size=12pt");
        doc.nextParagraph();
        doc.addText("texte 3 small", "font-size=10pt");
        doc.nextParagraph();
        doc.addText("texte 4", "font-size=8pt");
        doc.nextParagraph();
        doc.nextParagraph();
        try {
            // continue
            format.format(doc, file);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            fail(ex.getMessage());
        }

        file.delete();

    }


}
