package ancestris.modules.releve.file;

import ancestris.modules.releve.ConfigPanel;
import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Field.FieldType;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import ancestris.modules.releve.model.RecordMisc;
import java.io.BufferedReader;
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
            BufferedReader br;
            
            data= "";
            br = new BufferedReader(new java.io.StringReader(data));
            boolean isValid = ReleveFileAncestrisV1.isValidFile(br);
            assertEquals("fichier vide" , isValid, false);

            data = "ANCESTRISV1;;;;;;;;;;;;;;;;;;;;;;;;";
            br = new BufferedReader(new java.io.StringReader(data));
            isValid = ReleveFileAncestrisV1.isValidFile(br);
            assertEquals("ligne incomplete", isValid, false);

            data = "ANCESTRISV1;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            br = new BufferedReader(new java.io.StringReader(data));
            isValid = ReleveFileAncestrisV1.isValidFile(br);
            assertEquals("points virgules collés", isValid, true);

            data = "ANCESTRISV1; ; ; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            br = new BufferedReader(new java.io.StringReader(data));
            isValid = ReleveFileAncestrisV1.isValidFile(br);
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
            assertEquals("File empty" , sb.getError().isEmpty(), true);
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
    public void testSaveFileBirth() throws Exception {
        File file = new File("testsaveFile.txt");
        
        ConfigPanel configPanel = new ConfigPanel();
        String place = "cityname,citycode,county,state,country";
        configPanel.setPlace(place);
        
        DataManager dateManager = new DataManager(configPanel);

        RecordBirth birth = new RecordBirth();
        birth.setEventDate("01/01/2000");
        birth.setCote("cote");
        birth.setFreeComment("photo");
        birth.setIndi("firstname", "lastname", "M", "", "", "place", "occupation", "comment");
        birth.setIndiFather("fathername", "fatherlastname", "occupation", "comment", "dead");
        birth.setIndiMother("mothername", "motherlastname", "occupation", "comment", "dead");
        birth.setWitness1("wfirstname", "wlastname", "woccupation", "wcomment");
        birth.setWitness2("wfirstname", "wlastname", "woccupation", "wcomment");
        birth.setWitness3("wfirstname", "wlastname", "woccupation", "wcomment");
        birth.setWitness4("wfirstname", "wlastname", "woccupation", "wcomment");

        dateManager.addRecord(birth);
        StringBuilder sb = ReleveFileAncestrisV1.saveFile(dateManager, dateManager.getReleveBirthModel(), file, false);
        assertEquals("verify save error", sb.length(), 0);

        FileBuffer fb = ReleveFileAncestrisV1.loadFile(file);
        assertEquals("load result", "", fb.getError().toString());
        assertEquals("load count", 1, fb.getBirthCount());
        RecordBirth birth2 = (RecordBirth) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (birth.getField(fieldType) == null) {
                assertNull(String.valueOf(fieldType.ordinal()), birth2.getField(fieldType));
            } else {
                assertNotNull(String.valueOf(fieldType.ordinal()), birth2.getField(fieldType));
                assertEquals(String.valueOf(fieldType.ordinal()), birth.getField(fieldType).toString(), birth2.getField(fieldType).toString());
            }
        }
        assertEquals("place count", 1, fb.getPlaces().size());
        assertEquals("place", place, fb.getPlaces().get(0));

        file.delete();

    }

    /**
     * Test of saveFile method, of class ReleveFileAncestrisV1.
     */
    public void testSaveFileMarriage() throws Exception {
        File file = new File("testsaveFile.txt");
        
        ConfigPanel configPanel = new ConfigPanel();
        String place = "cityname,citycode,county,state,country";
        configPanel.setPlace(place);

        DataManager dateManager = new DataManager(configPanel);

        RecordMarriage marriage = new RecordMarriage();
        marriage.setEventDate("01/01/2000");
        marriage.setCote("cote");
        marriage.setFreeComment("photo");
        marriage.setIndi("indifirstname", "indilastname", "M", "indiage", "01/02/1990", "indiplace", "indioccupation", "indicomment");
        marriage.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indimarriedcomment", "indimarrieddead");
        marriage.setIndiFather("indifathername", "indifatherlastname", "indifatheroccupation", "indifathercomment", "indifatherdead");
        marriage.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indimothercomment", "indimotherdead");
        marriage.setWife("wifefirstname", "wifelastname", "F", "wifeage", "02/02/1992", "wifeplace", "wifeoccupation", "wifecomment");
        marriage.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifemarriedcomment", "wifemarrieddead");
        marriage.setWifeFather("wifefathername", "wifefatherlastname", "wifefatheroccupation", "wifefathercomment", "wifefatherdead");
        marriage.setWifeMother("wifemothername", "wifemotherlastname", "wifemotheroccupation", "wifemothercomment", "wifemotherdead");
        marriage.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        marriage.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        marriage.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        marriage.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

        dateManager.addRecord(marriage);
        StringBuilder sb = ReleveFileAncestrisV1.saveFile(dateManager, dateManager.getReleveMarriageModel(), file, false);
        assertEquals("save result", 0, sb.length());

        FileBuffer fb = ReleveFileAncestrisV1.loadFile(file);
        assertEquals("load result", "", fb.getError().toString());
        assertEquals("load count", 1, fb.getBirthCount());
        RecordMarriage marriage2 = (RecordMarriage) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (marriage.getField(fieldType) == null) {
                assertNull(String.valueOf(fieldType.ordinal()), marriage2.getField(fieldType));
            } else {
                assertNotNull(String.valueOf(fieldType.ordinal()), marriage2.getField(fieldType));
                assertEquals(String.valueOf(fieldType.ordinal()), marriage.getField(fieldType).toString(), marriage2.getField(fieldType).toString());
            }
        }
        assertEquals("place count", 1, fb.getPlaces().size());
        assertEquals("place", place, fb.getPlaces().get(0));

        file.delete();

    }

    /**
     * Test of saveFile method, of class ReleveFileAncestrisV1.
     */
    public void testSaveFileDeath() throws Exception {
        File file = new File("testsaveFile.txt");

        ConfigPanel configPanel = new ConfigPanel();
        String place = "cityname,citycode,county,state,country";
        configPanel.setPlace(place);

        DataManager dateManager = new DataManager(configPanel);

        RecordDeath death = new RecordDeath();
        death.setEventDate("11/11/2000");
        death.setCote("cote");
        death.setGeneralComment("generalcomment");
        death.setFreeComment("photo");
        death.setIndi("indifirstname", "indilastname", "M", "indiage", "01/01/1990", "indiplace", "indioccupation", "indicomment");
        death.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indimarriedcomment", "indimarrieddead");
        death.setIndiFather("indifathername", "indifatherlastname", "indifatheroccupation", "indifathercomment", "indifatherdead");
        death.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indimothercomment", "indimotherdead");
        death.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        death.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        death.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        death.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

        dateManager.addRecord(death);
        StringBuilder sb = ReleveFileAncestrisV1.saveFile(dateManager, dateManager.getReleveDeathModel(), file, false);
        assertEquals("verify save error", 0, sb.length());

        FileBuffer fb = ReleveFileAncestrisV1.loadFile(file);
        assertEquals("load result", "", fb.getError().toString());
        assertEquals("load count", 1, fb.getBirthCount());
        RecordDeath death2 = (RecordDeath) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (death.getField(fieldType) == null) {
                assertNull(String.valueOf(fieldType.ordinal()), death2.getField(fieldType));
            } else {
                assertNotNull(String.valueOf(fieldType.ordinal()), death2.getField(fieldType));
                assertEquals(String.valueOf(fieldType.ordinal()), death.getField(fieldType).toString(), death2.getField(fieldType).toString());
            }
        }
        assertEquals("place count", 1, fb.getPlaces().size());
        assertEquals("place", place, fb.getPlaces().get(0));

        file.delete();

    }

    /**
     * Test of saveFile method, of class ReleveFileAncestrisV1.
     */
    public void testSaveFileMisc() throws Exception {
        File file = new File("testsaveFile.txt");

        ConfigPanel configPanel = new ConfigPanel();
        String place = "cityname,citycode,county,state,country";
        configPanel.setPlace(place);
        DataManager dateManager = new DataManager(configPanel);

        RecordMisc record = new RecordMisc();
        record.setEventDate("29/02/2012");
        record.setCote("cote");
        record.setParish("parish");
        record.setNotary("Notary");
        record.setEventType("eventname", "eventtag");
        record.setGeneralComment("generalcomment");
        record.setFreeComment("photo");
        record.setIndi("indifirstname", "indilastname", "M", "indiage", "01/01/1990", "indiplace", "indioccupation", "indicomment");
        record.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indimarriedcomment", "indimarrieddead");
        record.setIndiFather("indifathername", "indifatherlastname", "indifatheroccupation", "indifathercomment", "indifatherdead");
        record.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indimothercomment", "indimotherdead");
        record.setWife("wifefirstname", "wifelastname", "F", "wifeage", "02/02/1992", "wifeplace", "wifeoccupation", "wifecomment");
        record.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifemarriedcomment", "wifemarrieddead");
        record.setWifeFather("wifefathername", "wifefatherlastname", "wifefatheroccupation", "wifefathercomment", "wifefatherdead");
        record.setWifeMother("wifemothername", "wifemotherlastname", "wifemotheroccupation", "wifemothercomment", "wifemotherdead");
        record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

        dateManager.addRecord(record);
        StringBuilder sb = ReleveFileAncestrisV1.saveFile(dateManager, dateManager.getReleveMiscModel(), file, false);
        assertEquals("verify save error", 0, sb.length());

        FileBuffer fb = ReleveFileAncestrisV1.loadFile(file);
        assertEquals("load result", "", fb.getError().toString());
        assertEquals("load count", 1, fb.getBirthCount());
        RecordMisc record2 = (RecordMisc) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (record.getField(fieldType) == null) {
                assertNull(String.valueOf(fieldType.ordinal()), record2.getField(fieldType));
            } else {
                assertNotNull(String.valueOf(fieldType.ordinal()), record2.getField(fieldType));
                assertEquals(String.valueOf(fieldType.ordinal()), record.getField(fieldType).toString(), record2.getField(fieldType).toString());
            }
        }
        assertEquals("place count", 1, fb.getPlaces().size());
        assertEquals("place", place, fb.getPlaces().get(0));


        file.delete();

    }

}
