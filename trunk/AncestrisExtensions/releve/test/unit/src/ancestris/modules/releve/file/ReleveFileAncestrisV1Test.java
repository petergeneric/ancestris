package ancestris.modules.releve.file;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Field.FieldType;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import ancestris.modules.releve.model.RecordMisc;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
import org.openide.util.Exceptions;

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
        File file;
        try {
            String data;
            data = "";
            StringBuilder sb;
 
            data = "";
            file = TestUtility.createFile(data);
            sb = new StringBuilder();
            boolean isValid = ReleveFileAncestrisV1.isValidFile(file, sb);
            assertEquals("fichier vide", false, isValid);

            data = "ANCESTRISV1;;;;;;;;;;;;;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            isValid = ReleveFileAncestrisV1.isValidFile(file, sb);
            assertEquals("ligne incomplete", false, isValid);

            data = "ANCESTRISV1;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            isValid = ReleveFileAncestrisV1.isValidFile(file, sb);
            assertEquals("points virgules collés", true, isValid);

            data = "ANCESTRISV1; ; ; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            isValid = ReleveFileAncestrisV1.isValidFile(file, sb);
            assertEquals(true, isValid);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
            
        
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
            assertEquals("Pas de type d'acte", "Ligne 1.\nType d'acte inconnu. \n",  sb.getError().toString());
            file.delete();

            data = "ANCESTRISV1;;;;;;;X;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV1.loadFile(file);
            assertEquals("type acte=X", "Ligne 1.\nType d'acte inconnu. X\n",  sb.getError().toString());
            file.delete();

            data = "ANCESTRISV1;;;;;;;N;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV1.loadFile(file);
            assertEquals("Naissance minimal", "",  sb.getError().toString());
            file.delete();

            data = "ANCESTRISV1;;;;;;;M;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV1.loadFile(file);
            assertEquals("Mariage minimal", "",  sb.getError().toString());
            file.delete();

            data = "ANCESTRISV1;;;;;;;D;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV1.loadFile(file);
            assertEquals("Deces minimal", "",  sb.getError().toString());
            file.delete();

            data = "ANCESTRISV1;;;;;;;V;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV1.loadFile(file);
            assertEquals("Divers minimal", "",  sb.getError().toString());
            file.delete();

        } catch (IOException ex) {
            fail("IOException "+ ex.toString());
        }
    }

    /**
     * Test of saveFile method, of class ReleveFileAncestrisV1.
     */
    public void testSaveFileBirthUtf8() throws Exception {
        File file = new File(System.getProperty("user.home") + File.separator +"testsaveFile.txt");
        
        String place = "cityname,citycode,county,state,country,";
        
        DataManager dataManager = new DataManager();
        dataManager.setPlace(place);

        RecordBirth birth = new RecordBirth();
        birth.setEventDate("01/01/2000");
        birth.setCote("cote");
        birth.setFreeComment("photo");
        birth.setIndi("Élisabeth-Adélaîde", "lastname", "M", "", "", "place", "occupation", "comment");
        birth.setIndiFather("fathername", "fatherlastname", "occupation", "comment", "dead", "70y");
        birth.setIndiMother("mothername", "motherlastname", "occupation", "comment", "dead", "72y");
        birth.setWitness1("wfirstname", "wlastname", "woccupation", "wcomment");
        birth.setWitness2("wfirstname", "wlastname", "woccupation", "wcomment");
        birth.setWitness3("wfirstname", "wlastname", "woccupation", "wcomment");
        birth.setWitness4("wfirstname", "wlastname", "woccupation", "wcomment");

        dataManager.addRecord(birth,false);
        StringBuilder sb = ReleveFileAncestrisV1.saveFile(dataManager, dataManager.getReleveBirthModel(), file, false);
        assertEquals("verify save error", "", sb.toString());

        FileBuffer fb = ReleveFileAncestrisV1.loadFile(file);
        assertEquals("load result", "", fb.getError().toString());
        assertEquals("load count", 1, fb.getBirthCount());
        RecordBirth birth2 = (RecordBirth) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (birth.getField(fieldType) == null) {
                assertNull(String.valueOf(fieldType.ordinal()), birth2.getField(fieldType));
            } else {
                if ( fieldType == FieldType.indiFatherAge || fieldType == FieldType.indiMotherAge) {
                    assertNotNull(String.valueOf(fieldType.ordinal()), birth2.getField(fieldType));
                    assertEquals(String.valueOf(fieldType.ordinal()), "", birth2.getField(fieldType).toString());
                } else {
                    assertNotNull(String.valueOf(fieldType.ordinal()), birth2.getField(fieldType));
                    assertEquals(String.valueOf(fieldType.ordinal()), birth.getField(fieldType).toString(), birth2.getField(fieldType).toString());
                }
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
        File file = new File(System.getProperty("user.home") + File.separator +"testsaveFile2.txt");
        
        String place = "cityname,citycode,county,state,country,";
        
        DataManager dataManager = new DataManager();
        dataManager.setPlace(place);

        RecordMarriage marriage = new RecordMarriage();
        marriage.setEventDate("01/01/2000");
        marriage.setCote("cote");
        marriage.setFreeComment("photo");
        marriage.setIndi("indifirstname", "indilastname", "M", "indiage", "01/02/1990", "indiplace", "indioccupation", "indicomment");
        marriage.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indimarriedcomment", "indimarrieddead");
        marriage.setIndiFather("indifathername", "indifatherlastname", "indifatheroccupation", "indifathercomment", "indifatherdead", "70y");
        marriage.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indimothercomment", "indimotherdead", "72y");
        marriage.setWife("wifefirstname", "wifelastname", "F", "wifeage", "02/02/1992", "wifeplace", "wifeoccupation", "wifecomment");
        marriage.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifemarriedcomment", "wifemarrieddead");
        marriage.setWifeFather("wifefathername", "wifefatherlastname", "wifefatheroccupation", "wifefathercomment", "wifefatherdead", "70y");
        marriage.setWifeMother("wifemothername", "wifemotherlastname", "wifemotheroccupation", "wifemothercomment", "wifemotherdead", "72y");
        marriage.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        marriage.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        marriage.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        marriage.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

        dataManager.addRecord(marriage,false);
        StringBuilder sb = ReleveFileAncestrisV1.saveFile(dataManager, dataManager.getReleveMarriageModel(), file, false);
        assertEquals("save result", "", sb.toString());

        FileBuffer fb = ReleveFileAncestrisV1.loadFile(file);
        assertEquals("load result", "", fb.getError().toString());
        assertEquals("load count", 1, fb.getMarriageCount());
        RecordMarriage marriage2 = (RecordMarriage) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (marriage.getField(fieldType) == null) {
                assertNull(String.valueOf(fieldType.ordinal()), marriage2.getField(fieldType));
            } else {
                if ( fieldType == FieldType.indiFatherAge || fieldType == FieldType.indiMotherAge
                        || fieldType == FieldType.wifeFatherAge || fieldType == FieldType.wifeMotherAge) {
                    assertNotNull(String.valueOf(fieldType.ordinal()), marriage2.getField(fieldType));
                    assertEquals(String.valueOf(fieldType.ordinal()), "", marriage2.getField(fieldType).toString());
                } else {
                    assertNotNull(String.valueOf(fieldType.ordinal()), marriage2.getField(fieldType));
                    assertEquals(String.valueOf(fieldType.ordinal()), marriage.getField(fieldType).toString(), marriage2.getField(fieldType).toString());
                }
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
        File file = new File(System.getProperty("user.home") + File.separator +"testsaveFile.txt");

        String place = "cityname,citycode,county,state,country,";
        
        DataManager dataManager = new DataManager();
        dataManager.setPlace(place);

        RecordDeath death = new RecordDeath();
        death.setEventDate("11/11/2000");
        death.setCote("cote");
        death.setGeneralComment("generalcomment");
        death.setFreeComment("photo");
        death.setIndi("indifirstname", "indilastname", "M", "indiage", "01/01/1990", "indiplace", "indioccupation", "indicomment");
        death.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indimarriedcomment", "indimarrieddead");
        death.setIndiFather("indifathername", "indifatherlastname", "indifatheroccupation", "indifathercomment", "indifatherdead", "70y");
        death.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indimothercomment", "indimotherdead", "72y");
        death.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        death.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        death.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        death.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

        dataManager.addRecord(death,false);
        StringBuilder sb = ReleveFileAncestrisV1.saveFile(dataManager, dataManager.getReleveDeathModel(), file, false);
        assertEquals("verify save error", "", sb.toString());

        FileBuffer fb = ReleveFileAncestrisV1.loadFile(file);
        assertEquals("load result", "", fb.getError().toString());
        assertEquals("load count", 1, fb.getDeathCount());
        RecordDeath death2 = (RecordDeath) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (death.getField(fieldType) == null) {
                assertNull(String.valueOf(fieldType.ordinal()), death2.getField(fieldType));
            } else {
                if ( fieldType == FieldType.indiFatherAge || fieldType == FieldType.indiMotherAge
                        || fieldType == FieldType.wifeFatherAge || fieldType == FieldType.wifeMotherAge) {
                    assertNotNull(String.valueOf(fieldType.ordinal()), death2.getField(fieldType));
                    assertEquals(String.valueOf(fieldType.ordinal()), "", death2.getField(fieldType).toString());
                } else {
                assertNotNull(String.valueOf(fieldType.ordinal()), death2.getField(fieldType));
                assertEquals(String.valueOf(fieldType.ordinal()), death.getField(fieldType).toString(), death2.getField(fieldType).toString());
                }
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
        File file = File.createTempFile("testsaveFile", "txt");
        
        String place = "cityname,citycode,county,state,country,";
        DataManager dataManager = new DataManager();
        dataManager.setPlace(place);

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
        record.setIndiFather("indifathername", "indifatherlastname", "indifatheroccupation", "indifathercomment", "indifatherdead", "70y");
        record.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indimothercomment", "indimotherdead", "72y");
        record.setWife("wifefirstname", "wifelastname", "F", "wifeage", "02/02/1992", "wifeplace", "wifeoccupation", "wifecomment");
        record.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifemarriedcomment", "wifemarrieddead");
        record.setWifeFather("wifefathername", "wifefatherlastname", "wifefatheroccupation", "wifefathercomment", "wifefatherdead", "70y");
        record.setWifeMother("wifemothername", "wifemotherlastname", "wifemotheroccupation", "wifemothercomment", "wifemotherdead", "72y");
        record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

        dataManager.addRecord(record,false);
        StringBuilder sb = ReleveFileAncestrisV1.saveFile(dataManager, dataManager.getReleveMiscModel(), file, false);
        System.out.println(sb);
        assertEquals("verify save error", "", sb.toString());

        FileBuffer fb = ReleveFileAncestrisV1.loadFile(file);
        assertEquals("load result", "", fb.getError().toString());
        assertEquals("load count", 1, fb.getMiscCount());
        RecordMisc record2 = (RecordMisc) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (record.getField(fieldType) == null) {
                assertNull(String.valueOf(fieldType.ordinal()), record2.getField(fieldType));
            } else {
                if ( fieldType == FieldType.indiFatherAge || fieldType == FieldType.indiMotherAge
                        || fieldType == FieldType.wifeFatherAge || fieldType == FieldType.wifeMotherAge) {
                    assertNotNull(String.valueOf(fieldType.ordinal()), record2.getField(fieldType));
                    assertEquals(String.valueOf(fieldType.ordinal()), "", record2.getField(fieldType).toString());
                } else {
                assertNotNull(String.valueOf(fieldType.ordinal()), record2.getField(fieldType));
                assertEquals(String.valueOf(fieldType.ordinal()), record.getField(fieldType).toString(), record2.getField(fieldType).toString());
                }
            }
        }
        assertEquals("place count", 1, fb.getPlaces().size());
        assertEquals("place", place, fb.getPlaces().get(0));


        file.delete();

    }

}
