package ancestris.modules.releve.file;

import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.RecordMisc;
import genj.fo.Document;
import genj.fo.Format;
import genj.fo.PDFFormat;
import java.io.File;
import java.io.FileOutputStream;
import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class ReleveFilePdfTest extends TestCase {
    
    
    /**
     * Test of saveFile method, of class ReleveFileEgmt.
     */
    public void testSaveFileMisc() throws Exception {
        File file = new File(System.getProperty("user.home") + File.separator + "testpdf.PDF");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("");

        RecordMisc misc = new RecordMisc();
        misc.setEventDate("11/01/2000");
        misc.setCote("cote");
        misc.setParish("parish");
        misc.setNotary("Notary");
        misc.setEventType("eventname");
        misc.setGeneralComment("generalcomment");
        misc.setFreeComment("photo");
        misc.setIndi("indifirstname", "indilastname", "M", "24y", "01/01/1980", "indiBirthPlace", "indioccupation", "indiResidence", "indicomment");
        misc.setIndiMarried("indimarriedfirstname", "indimarriedlastname", "indimarriedoccupation", "indiMarriedResidence", "indimarriedcomment", "false");
        misc.setIndiFather("indifathername", "indifatherlastname", "indifatheroccupation", "indiFatherResidence", "indifathercomment", "false", "70y");
        misc.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indiMotherResidence", "indimothercomment", "false", "72y");
        misc.setWife("wifefirstname", "wifelastname", "F", "22y", "02/02/1982", "wifeBirthPlace", "wifeoccupation", "wifeResidence", "wifecomment");
        misc.setWifeMarried("wifemarriedfirstname", "wifemarriedlastname", "wifemarriedoccupation", "wifeMarriedResidence", "wifemarriedcomment", "true");
        misc.setWifeFather("wifefathername", "wifefatherlastname", "wifefatheroccupation", "wifeFatherResidence", "wifefathercomment", "true", "60y");
        misc.setWifeMother("wifemothername", "wifemotherlastname", "wifemotheroccupation", "wifeMotherResidence", "wifemothercomment", "false", "62y");
        misc.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        misc.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        misc.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        misc.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

        dataManager.addRecord(misc);
        dataManager.addRecord(misc);
        StringBuilder sb = ReleveFilePdf.saveFile(dataManager, dataManager.getDataModel(), DataManager.RecordType.misc, file, false);
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

    public void testPDF() throws Exception {

        File file = new File("C:\\Users\\Michel\\Desktop\\aaa.PDF");
        // try to create output stream
        FileOutputStream out = new FileOutputStream(file);
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
        // continue
        format.format(doc, file);

        file.delete();

    }

    
}
