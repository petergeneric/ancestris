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
public class ReleveFileAncestrisV1Test extends TestCase {
    
    public ReleveFileAncestrisV1Test(String testName) {
        super(testName);
    }

    /**
     * Test of isValidFile method, of class ReleveFileAncestrisV1.
     */
    @Test
    public void testIsValidFile() {
        File file;
        try {
            String data;
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
    @Test
    public void testLoadFile(){
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
            assertEquals("Pas de type d'acte", "Ligne 1.\nType d'acte inconnu. \n",  sb.getError());
            file.delete();

            data = "ANCESTRISV1;;;;;;;X;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV1.loadFile(file);
            assertEquals("type acte=X", "Ligne 1.\nType d'acte inconnu. X\n",  sb.getError());
            file.delete();

            data = "ANCESTRISV1;;;;;;;N;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV1.loadFile(file);
            assertEquals("Naissance minimal", "",  sb.getError());
            file.delete();

            data = "ANCESTRISV1;;;;;;;M;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV1.loadFile(file);
            assertEquals("Mariage minimal", "",  sb.getError());
            file.delete();

            data = "ANCESTRISV1;;;;;;;D;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV1.loadFile(file);
            assertEquals("Deces minimal", "",  sb.getError());
            file.delete();

            data = "ANCESTRISV1;;;;;;;V;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV1.loadFile(file);
            assertEquals("Divers minimal", "",  sb.getError());
            file.delete();

        } catch (IOException ex) {
            fail("IOException "+ ex.toString());
        }
    }

    /**
     * Test of saveFile method, of class ReleveFileAncestrisV1.
     */
    @Test
    public void SaveFileBirthUtf8() {
        File file = new File(System.getProperty("user.home") + File.separator +"testsaveFile.txt");
        
        DataManager dataManager = new DataManager();
        dataManager.setPlace("cityname","citycode","county","state","country");
        String place = dataManager.getPlace().getValue();
        
        RecordBirth birth = TestUtility.getRecordBirth();
        dataManager.addRecord(birth);
        StringBuilder sb = ReleveFileAncestrisV1.saveFile(dataManager, dataManager.getDataModel(), DataManager.RecordType.birth, file, false);
        assertEquals("verify save error", "", sb.toString());

        FileBuffer fb = ReleveFileAncestrisV1.loadFile(file);
        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getBirthCount());
        RecordBirth birth2 = (RecordBirth) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (birth.getField(fieldType) == null) {
                assertNull(fieldType.name(), birth2.getField(fieldType));
            } else {
                if ( fieldType == FieldType.indiFatherAge || fieldType == FieldType.indiMotherAge
                     || fieldType == FieldType.wifeFatherAge || fieldType == FieldType.wifeMotherAge
                     || fieldType == FieldType.indiResidence || fieldType == FieldType.indiMarriedResidence
                     || fieldType == FieldType.indiFatherResidence || fieldType == FieldType.indiMotherResidence
                     || fieldType == FieldType.wifeResidence || fieldType == FieldType.wifeMarriedResidence
                     || fieldType == FieldType.wifeFatherResidence || fieldType == FieldType.wifeMotherResidence
                     || fieldType == FieldType.secondDate
                     || fieldType == FieldType.indiBirthAddress || fieldType == FieldType.indiAddress 
                     || fieldType == FieldType.indiMarriedAddress
                     || fieldType == FieldType.indiFatherAddress || fieldType == FieldType.indiMotherAddress
                     || fieldType == FieldType.wifeBirthAddress || fieldType == FieldType.wifeAddress 
                     || fieldType == FieldType.wifeMarriedAddress
                     || fieldType == FieldType.wifeFatherAddress || fieldType == FieldType.wifeMotherAddress                     
                     ) {
                    assertNotNull(fieldType.name(), birth2.getField(fieldType));
                    assertEquals(fieldType.name(), "", birth2.getField(fieldType).toString());
                } else {
                    assertNotNull(fieldType.name(), birth2.getField(fieldType));
                    assertEquals(fieldType.name(), birth.getField(fieldType).toString(), birth2.getField(fieldType).toString());
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
    @Test
    public void testSaveFileMarriage() {
        File file = new File(System.getProperty("user.home") + File.separator +"testsaveFile2.txt");
        
        DataManager dataManager = new DataManager();
        dataManager.setPlace("cityname","citycode","county","state","country");
        String place = dataManager.getPlace().getValue();
        
        RecordMarriage marriage = TestUtility.getRecordMarriage();
        dataManager.addRecord(marriage);
        StringBuilder sb = ReleveFileAncestrisV1.saveFile(dataManager, dataManager.getDataModel(), DataManager.RecordType.marriage, file, false);
        assertEquals("save result", "", sb.toString());

        FileBuffer fb = ReleveFileAncestrisV1.loadFile(file);
        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getMarriageCount());
        RecordMarriage marriage2 = (RecordMarriage) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (marriage.getField(fieldType) == null) {
                assertNull(fieldType.name(), marriage2.getField(fieldType));
            } else {
                if ( fieldType == FieldType.indiFatherAge || fieldType == FieldType.indiMotherAge
                     || fieldType == FieldType.wifeFatherAge || fieldType == FieldType.wifeMotherAge
                     || fieldType == FieldType.indiResidence || fieldType == FieldType.indiMarriedResidence
                     || fieldType == FieldType.indiFatherResidence || fieldType == FieldType.indiMotherResidence
                     || fieldType == FieldType.wifeResidence || fieldType == FieldType.wifeMarriedResidence
                     || fieldType == FieldType.wifeFatherResidence || fieldType == FieldType.wifeMotherResidence
                     || fieldType == FieldType.secondDate
                     || fieldType == FieldType.indiBirthAddress || fieldType == FieldType.indiAddress 
                     || fieldType == FieldType.indiMarriedAddress
                     || fieldType == FieldType.indiFatherAddress || fieldType == FieldType.indiMotherAddress
                     || fieldType == FieldType.wifeBirthAddress || fieldType == FieldType.wifeAddress 
                     || fieldType == FieldType.wifeMarriedAddress
                     || fieldType == FieldType.wifeFatherAddress || fieldType == FieldType.wifeMotherAddress                     
                     ) {
                    assertNotNull(fieldType.name(), marriage2.getField(fieldType));
                    assertEquals(fieldType.name(), "", marriage2.getField(fieldType).toString());
                } else {
                    assertNotNull(fieldType.name(), marriage2.getField(fieldType));
                    assertEquals(fieldType.name(), marriage.getField(fieldType).toString(), marriage2.getField(fieldType).toString());
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
    @Test
    public void testSaveFileDeath() {
        File file = new File(System.getProperty("user.home") + File.separator +"testsaveFile.txt");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("cityname","citycode","county","state","country");
        String place = dataManager.getPlace().getValue();
        
        RecordDeath death = TestUtility.getRecordDeath();
        dataManager.addRecord(death);
        StringBuilder sb = ReleveFileAncestrisV1.saveFile(dataManager, dataManager.getDataModel(), DataManager.RecordType.death, file, false);
        assertEquals("verify save error", "", sb.toString());

        FileBuffer fb = ReleveFileAncestrisV1.loadFile(file);
        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getDeathCount());
        RecordDeath death2 = (RecordDeath) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (death.getField(fieldType) == null) {
                assertNull(fieldType.name(), death2.getField(fieldType));
            } else {
                if ( fieldType == FieldType.indiFatherAge || fieldType == FieldType.indiMotherAge
                     || fieldType == FieldType.wifeFatherAge || fieldType == FieldType.wifeMotherAge
                     || fieldType == FieldType.indiResidence || fieldType == FieldType.indiMarriedResidence
                     || fieldType == FieldType.indiFatherResidence || fieldType == FieldType.indiMotherResidence
                     || fieldType == FieldType.wifeResidence || fieldType == FieldType.wifeMarriedResidence
                     || fieldType == FieldType.wifeFatherResidence || fieldType == FieldType.wifeMotherResidence
                     || fieldType == FieldType.secondDate
                     || fieldType == FieldType.indiBirthAddress || fieldType == FieldType.indiAddress 
                     || fieldType == FieldType.indiMarriedAddress
                     || fieldType == FieldType.indiFatherAddress || fieldType == FieldType.indiMotherAddress
                     || fieldType == FieldType.wifeBirthAddress || fieldType == FieldType.wifeAddress 
                     || fieldType == FieldType.wifeMarriedAddress
                     || fieldType == FieldType.wifeFatherAddress || fieldType == FieldType.wifeMotherAddress                     
                     ) {
                    assertNotNull(fieldType.name(), death2.getField(fieldType));
                    assertEquals(fieldType.name(), "", death2.getField(fieldType).toString());
                } else {
                assertNotNull(fieldType.name(), death2.getField(fieldType));
                assertEquals(fieldType.name(), death.getField(fieldType).toString(), death2.getField(fieldType).toString());
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
    @Test
    public void testSaveFileMisc() {
        File file;
        try {
            file = File.createTempFile("testsaveFile", "txt");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            fail(ex.getMessage());
            return;
        }
        
        DataManager dataManager = new DataManager();
        dataManager.setPlace("cityname","citycode","county","state","country");
        String place = dataManager.getPlace().getValue();
        
        RecordMisc record = TestUtility.getRecordMisc();
        dataManager.addRecord(record);
        StringBuilder sb = ReleveFileAncestrisV1.saveFile(dataManager, dataManager.getDataModel(), DataManager.RecordType.misc, file, false);
        System.out.println(sb);
        assertEquals("verify save error", "", sb.toString());

        FileBuffer fb = ReleveFileAncestrisV1.loadFile(file);
        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getMiscCount());
        RecordMisc record2 = (RecordMisc) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (record.getField(fieldType) == null) {
                assertNull(fieldType.name(), record2.getField(fieldType));
            } else {
                if ( fieldType == FieldType.indiFatherAge || fieldType == FieldType.indiMotherAge
                     || fieldType == FieldType.wifeFatherAge || fieldType == FieldType.wifeMotherAge
                     || fieldType == FieldType.indiResidence || fieldType == FieldType.indiMarriedResidence
                     || fieldType == FieldType.indiFatherResidence || fieldType == FieldType.indiMotherResidence
                     || fieldType == FieldType.wifeResidence || fieldType == FieldType.wifeMarriedResidence
                     || fieldType == FieldType.wifeFatherResidence || fieldType == FieldType.wifeMotherResidence
                     || fieldType == FieldType.secondDate
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
