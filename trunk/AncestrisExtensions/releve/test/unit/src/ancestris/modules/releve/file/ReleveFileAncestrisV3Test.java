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
import org.junit.Test;
import org.openide.util.Exceptions;

/**
 *
 * @author Michel
 */
public class ReleveFileAncestrisV3Test extends TestCase {
    
    public ReleveFileAncestrisV3Test(String testName) {
        super(testName);
    }

    /**
     * Test of isValidFile method, of class ReleveFileAncestrisV3.
     */
    @Test
    public void testIsValidFile() {
        File file;
        try {
            String data;
            data = "";
            StringBuilder sb;
 
            data = "";
            file = TestUtility.createFile(data);
            sb = new StringBuilder();
            boolean isValid = ReleveFileAncestrisV3.isValidFile(file, sb);
            assertEquals("fichier vide", false, isValid);

            data = "ANCESTRISV3;;;;;;;;;;;;;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            isValid = ReleveFileAncestrisV3.isValidFile(file, sb);
            assertEquals("ligne incomplete", false, isValid);

            data = "ANCESTRISV3;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            isValid = ReleveFileAncestrisV3.isValidFile(file, sb);
            assertEquals("points virgules collés", true, isValid);

            data = "ANCESTRISV3; ; ; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            isValid = ReleveFileAncestrisV3.isValidFile(file, sb);
            assertEquals(true, isValid);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
            
        
    }

    
    /**
     * Test of loadFile method, of class ReleveFileAncestrisV3.
     */
    @Test
    public void testLoadFile() {
        try {
            File file ;
            String data;
            FileBuffer sb;

            data = "";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV3.loadFile(file);
            assertEquals("File empty" , sb.getError().isEmpty(), true);
            file.delete();

            data = "ANCESTRISV3;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV3.loadFile(file);
            assertEquals("Pas de type d'acte", "Ligne 1.\nType d'acte inconnu. \n",  sb.getError().toString());
            file.delete();

            data = "ANCESTRISV3;;;;;;;X;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV3.loadFile(file);
            assertEquals("type acte=X", "Ligne 1.\nType d'acte inconnu. X\n",  sb.getError().toString());
            file.delete();

            data = "ANCESTRISV3;;;;;;;N;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV3.loadFile(file);
            assertEquals("Naissance minimal", "",  sb.getError().toString());
            file.delete();

            data = "ANCESTRISV3;;;;;;;M;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV3.loadFile(file);
            assertEquals("Mariage minimal", "",  sb.getError().toString());
            file.delete();

            data = "ANCESTRISV3;;;;;;;D;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV3.loadFile(file);
            assertEquals("Deces minimal", "",  sb.getError().toString());
            file.delete();

            data = "ANCESTRISV3;;;;;;;V;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV3.loadFile(file);
            assertEquals("Divers minimal", "",  sb.getError().toString());
            file.delete();

        } catch (IOException ex) {
            fail("IOException "+ ex.toString());
        }
    }

    /**
     * Test of saveFile method, of class ReleveFileAncestrisV3.
     */
    @Test
    public void testSaveFileBirthUtf8() {
        File file = new File(System.getProperty("user.home") + File.separator +"testsaveFile.txt");
        
        DataManager dataManager = new DataManager();
        dataManager.setPlace("cityname","citycode","county","state","country");
        String place = dataManager.getPlace().getValue();
        
        RecordBirth record = TestUtility.getRecordBirth();
        dataManager.addRecord(record);
        StringBuilder sb = ReleveFileAncestrisV3.saveFile(dataManager, dataManager.getDataModel(), DataManager.RecordType.birth, file, false);
        assertEquals("verify save error", sb.length(), 0);

        FileBuffer fb = ReleveFileAncestrisV3.loadFile(file);
        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getBirthCount());
        RecordBirth record2 = (RecordBirth) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (record.getField(fieldType) == null) {
                assertNull(fieldType.name(), record2.getField(fieldType));
            } else {
                if ( fieldType == FieldType.secondDate
                     || fieldType == FieldType.indiBirthAddress || fieldType == FieldType.indiAddress 
                     || fieldType == FieldType.indiMarriedAddress
                     || fieldType == FieldType.indiFatherAddress || fieldType == FieldType.indiMotherAddress
                     || fieldType == FieldType.wifeBirthAddress || fieldType == FieldType.wifeAddress 
                     || fieldType == FieldType.wifeMarriedAddress
                     || fieldType == FieldType.wifeFatherAddress || fieldType == FieldType.wifeMotherAddress                     
                     ) {
                    assertNotNull(fieldType.name(), record2.getField(fieldType));
                    assertEquals(fieldType.name(), "", record2.getField(fieldType).toString());
                } else {
                    assertNotNull(fieldType.name(), record2.getField(fieldType));
                    assertEquals(fieldType.name(), record.getField(fieldType).toString(), record2.getField(fieldType).toString());
                }
            }
        }
        assertEquals("place count", 1, fb.getPlaces().size());
        assertEquals("place", place, fb.getPlaces().get(0));

        file.delete();

    }

    /**
     * Test of saveFile method, of class ReleveFileAncestrisV3.
     */
    @Test
    public void testSaveFileMarriage() {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");
        
        DataManager dataManager = new DataManager();
        dataManager.setPlace("cityname","citycode","county","state","country");
        String place = dataManager.getPlace().getValue();
        
        RecordMarriage record = TestUtility.getRecordMarriage();      
        dataManager.addRecord(record);
        StringBuilder sb = ReleveFileAncestrisV3.saveFile(dataManager, dataManager.getDataModel(), DataManager.RecordType.marriage, file, false);
        assertEquals("save result", 0, sb.length());

        FileBuffer fb = ReleveFileAncestrisV3.loadFile(file);
        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getMarriageCount());
        RecordMarriage record2 = (RecordMarriage) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (record.getField(fieldType) == null) {
                assertNull(fieldType.name(), record2.getField(fieldType));
            } else {
                if ( fieldType == FieldType.secondDate
                     || fieldType == FieldType.indiBirthAddress || fieldType == FieldType.indiAddress 
                     || fieldType == FieldType.indiMarriedAddress
                     || fieldType == FieldType.indiFatherAddress || fieldType == FieldType.indiMotherAddress
                     || fieldType == FieldType.wifeBirthAddress || fieldType == FieldType.wifeAddress 
                     || fieldType == FieldType.wifeMarriedAddress
                     || fieldType == FieldType.wifeFatherAddress || fieldType == FieldType.wifeMotherAddress                     
                     ) {
                    assertNotNull(fieldType.name(), record2.getField(fieldType));
                    assertEquals(fieldType.name(), "", record2.getField(fieldType).toString());
                } else {
                    assertNotNull(fieldType.name(), record2.getField(fieldType));
                    assertEquals(fieldType.name(), record.getField(fieldType).toString(), record2.getField(fieldType).toString());
                }
            }
        }
        assertEquals("place count", 1, fb.getPlaces().size());
        assertEquals("place", place, fb.getPlaces().get(0));

        file.delete();

    }

    /**
     * Test of saveFile method, of class ReleveFileAncestrisV3.
     */
    @Test
    public void testSaveFileDeath() {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("cityname","citycode","county","state","country");
        String place = dataManager.getPlace().getValue();
        
        RecordDeath record = TestUtility.getRecordDeath();
        dataManager.addRecord(record);
        StringBuilder sb = ReleveFileAncestrisV3.saveFile(dataManager, dataManager.getDataModel(), DataManager.RecordType.death, file, false);
        assertEquals("verify save error", 0, sb.length());

        FileBuffer fb = ReleveFileAncestrisV3.loadFile(file);
        assertEquals("load result", "", fb.getError().toString());
        assertEquals("load count", 1, fb.getDeathCount());
        RecordDeath record2 = (RecordDeath) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (record.getField(fieldType) == null) {
                assertNull(fieldType.name(), record2.getField(fieldType));
            } else {
               if ( fieldType == FieldType.secondDate
                     || fieldType == FieldType.indiBirthAddress || fieldType == FieldType.indiAddress 
                     || fieldType == FieldType.indiMarriedAddress
                     || fieldType == FieldType.indiFatherAddress || fieldType == FieldType.indiMotherAddress
                     || fieldType == FieldType.wifeBirthAddress || fieldType == FieldType.wifeAddress 
                     || fieldType == FieldType.wifeMarriedAddress
                     || fieldType == FieldType.wifeFatherAddress || fieldType == FieldType.wifeMotherAddress                     
                     ) {
                    assertNotNull(fieldType.name(), record2.getField(fieldType));
                    assertEquals(fieldType.name(), "", record2.getField(fieldType).toString());
                } else {
                    assertNotNull(fieldType.name(), record2.getField(fieldType));
                    assertEquals(fieldType.name(), record.getField(fieldType).toString(), record2.getField(fieldType).toString());
                }
            }
        }
        assertEquals("place count", 1, fb.getPlaces().size());
        assertEquals("place", place, fb.getPlaces().get(0));

        file.delete();

    }

    /**
     * Test of saveFile method, of class ReleveFileAncestrisV3.
     */
    @Test
    public void testSaveFileMisc() {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("cityname","citycode","county","state","country");
        String place = dataManager.getPlace().getValue();
        
        RecordMisc record = TestUtility.getRecordMisc();
        dataManager.addRecord(record);
        StringBuilder sb = ReleveFileAncestrisV3.saveFile(dataManager, dataManager.getDataModel(), DataManager.RecordType.misc, file, false);
        assertEquals("verify save error", 0, sb.length());

        FileBuffer fb = ReleveFileAncestrisV3.loadFile(file);
        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getMiscCount());
        RecordMisc record2 = (RecordMisc) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (record.getField(fieldType) == null) {
                assertNull(fieldType.name(), record2.getField(fieldType));
            } else {
                if ( fieldType == FieldType.secondDate
                     || fieldType == FieldType.indiBirthAddress || fieldType == FieldType.indiAddress 
                     || fieldType == FieldType.indiMarriedAddress
                     || fieldType == FieldType.indiFatherAddress || fieldType == FieldType.indiMotherAddress
                     || fieldType == FieldType.wifeBirthAddress || fieldType == FieldType.wifeAddress 
                     || fieldType == FieldType.wifeMarriedAddress
                     || fieldType == FieldType.wifeFatherAddress || fieldType == FieldType.wifeMotherAddress                     
                     ) {
                    assertNotNull(fieldType.name(), record2.getField(fieldType));
                    assertEquals(fieldType.name(), "", record2.getField(fieldType).toString());
                } else {
                    assertNotNull(fieldType.name(), record2.getField(fieldType));
                    assertEquals(fieldType.name(), record.getField(fieldType).toString(), record2.getField(fieldType).toString());
                }
            }
        }
        assertEquals("place count", 1, fb.getPlaces().size());
        assertEquals("place", place, fb.getPlaces().get(0));


        file.delete();

    }

}
