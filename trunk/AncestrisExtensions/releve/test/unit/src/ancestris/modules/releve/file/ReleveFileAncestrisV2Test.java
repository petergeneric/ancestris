package ancestris.modules.releve.file;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.Record.RecordType;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import ancestris.modules.releve.model.RecordMisc;
import java.io.File;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.openide.util.Exceptions;

/**
 *
 * @author Michel
 */
public class ReleveFileAncestrisV2Test{

    /**
     * Test of isValidFile method, of class ReleveFileAncestrisV2.
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
            boolean isValid = ReleveFileAncestrisV2.isValidFile(file, sb);
            assertEquals("fichier vide", false, isValid);

            data = "ANCESTRISV2;;;;;;;;;;;;;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            isValid = ReleveFileAncestrisV2.isValidFile(file, sb);
            assertEquals("ligne incomplete", false, isValid);

            data = "ANCESTRISV2;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            isValid = ReleveFileAncestrisV2.isValidFile(file, sb);
            assertEquals("points virgules collés", true, isValid);

            data = "ANCESTRISV2; ; ; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            isValid = ReleveFileAncestrisV2.isValidFile(file, sb);
            assertEquals(true, isValid);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }


    }


    /**
     * Test of loadFile method, of class ReleveFileAncestrisV2.
     */
    @Test
    public void testLoadFile() {
        try {
            File file ;
            String data;
            FileBuffer sb;

            data = "";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV2.loadFile(file);
            assertEquals("File empty" , sb.getError().isEmpty(), true);
            file.delete();

            data = "ANCESTRISV2;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV2.loadFile(file);
            assertEquals("Pas de type d'acte", "Ligne 1.\nType d'acte inconnu. \n",  sb.getError());
            file.delete();

            data = "ANCESTRISV2;;;;;;;X;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV2.loadFile(file);
            assertEquals("type acte=X", "Ligne 1.\nType d'acte inconnu. X\n",  sb.getError());
            file.delete();

            data = "ANCESTRISV2;;;;;;;N;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV2.loadFile(file);
            assertEquals("Naissance minimal", "",  sb.getError());
            file.delete();

            data = "ANCESTRISV2;;;;;;;M;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV2.loadFile(file);
            assertEquals("Mariage minimal", "",  sb.getError());
            file.delete();

            data = "ANCESTRISV2;;;;;;;D;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV2.loadFile(file);
            assertEquals("Deces minimal", "",  sb.getError());
            file.delete();

            data = "ANCESTRISV2;;;;;;;V;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;1";
            file = TestUtility.createFile(data);
            sb = ReleveFileAncestrisV2.loadFile(file);
            assertEquals("Divers minimal", "",  sb.getError());
            file.delete();

        } catch (IOException ex) {
            fail("IOException "+ ex.toString());
        }
    }

    /**
     * Test of saveFile method, of class ReleveFileAncestrisV2.
     */
    @Test
    public void testSaveFileBirthUtf8() {
        File file = new File(System.getProperty("user.home") + File.separator +"testsaveFile.txt");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("cityname","citycode","county","state","country");
        String place = dataManager.getPlace().getValue();

        RecordBirth record = TestUtility.getRecordBirth();
        dataManager.addRecord(record);
        StringBuilder sb = ReleveFileAncestrisV2.saveFile(dataManager, dataManager.getDataModel(), RecordType.BIRTH, file, false);
        assertEquals("verify save error", sb.length(), 0);

        FileBuffer fb = ReleveFileAncestrisV2.loadFile(file);
        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getBirthCount());
        RecordBirth record2 = (RecordBirth) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (record.getField(fieldType) == null) {
                assertNull(fieldType.name(), record2.getField(fieldType));
            } else {
                if ( fieldType == FieldType.indiResidence || fieldType == FieldType.indiMarriedResidence
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
                    assertNull(fieldType.name(), record2.getField(fieldType));
                    assertEquals(fieldType.name(), "", record2.getFieldValue(fieldType));
                } else {
                    assertEquals(fieldType.name(), record.getFieldValue(fieldType), record2.getFieldValue(fieldType));
                }
            }
        }
        assertEquals("place count", 1, fb.getPlaces().size());
        assertEquals("place", place, fb.getPlaces().get(0));

        file.delete();

    }

    /**
     * Test of saveFile method, of class ReleveFileAncestrisV2.
     */
    @Test
    public void testSaveFileMarriage() {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("cityname","citycode","county","state","country");
        String place = dataManager.getPlace().getValue();

        RecordMarriage record = TestUtility.getRecordMarriage();
        record.setFieldValue(FieldType.eventDate, "01/01/2000");
        dataManager.addRecord(record);
        StringBuilder sb = ReleveFileAncestrisV2.saveFile(dataManager, dataManager.getDataModel(), RecordType.MARRIAGE, file, false);
        assertEquals("save result", 0, sb.length());

        FileBuffer fb = ReleveFileAncestrisV2.loadFile(file);
        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getMarriageCount());
        RecordMarriage record2 = (RecordMarriage) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (record.getField(fieldType) == null) {
                assertNull(fieldType.name(), record2.getField(fieldType));
            } else {
                if ( fieldType == FieldType.indiResidence || fieldType == FieldType.indiMarriedResidence
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
                    assertNull(fieldType.name(), record2.getField(fieldType));
                    assertEquals(fieldType.name(), "", record2.getFieldValue(fieldType));
                } else {
                    //assertNotNull(fieldType.name(), record2.getField(fieldType));
                    assertEquals(fieldType.name(), record.getFieldValue(fieldType), record2.getFieldValue(fieldType));
                }
            }
        }
        assertEquals("place count", 1, fb.getPlaces().size());
        assertEquals("place", place, fb.getPlaces().get(0));

        file.delete();

    }

    /**
     * Test of saveFile method, of class ReleveFileAncestrisV2.
     */
    @Test
    public void testSaveFileDeath() {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("cityname","citycode","county","state","country");
        String place = dataManager.getPlace().getValue();

        RecordDeath record = TestUtility.getRecordDeath();
        dataManager.addRecord(record);
        StringBuilder sb = ReleveFileAncestrisV2.saveFile(dataManager, dataManager.getDataModel(), RecordType.DEATH, file, false);
        assertEquals("verify save error", 0, sb.length());

        FileBuffer fb = ReleveFileAncestrisV2.loadFile(file);
        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getDeathCount());
        RecordDeath record2 = (RecordDeath) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (record.getField(fieldType) == null) {
                assertNull(fieldType.name(), record2.getField(fieldType));
            } else {
                if ( fieldType == FieldType.indiResidence || fieldType == FieldType.indiMarriedResidence
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
                    assertNull(fieldType.name(), record2.getField(fieldType));
                    assertEquals(fieldType.name(), "", record2.getFieldValue(fieldType));
                } else {
                    //assertNotNull(fieldType.name(), record2.getField(fieldType));
                    assertEquals(fieldType.name(), record.getFieldValue(fieldType), record2.getFieldValue(fieldType));
                }
            }
        }
        assertEquals("place count", 1, fb.getPlaces().size());
        assertEquals("place", place, fb.getPlaces().get(0));

        file.delete();

    }

    /**
     * Test of saveFile method, of class ReleveFileAncestrisV2.
     */
    @Test
    public void testSaveFileMisc() {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("cityname","citycode","county","state","country");
        String place = dataManager.getPlace().getValue();

        RecordMisc record = TestUtility.getRecordMisc();
        dataManager.addRecord(record);
        StringBuilder sb = ReleveFileAncestrisV2.saveFile(dataManager, dataManager.getDataModel(), RecordType.MISC, file, false);
        assertEquals("verify save error", 0, sb.length());

        FileBuffer fb = ReleveFileAncestrisV2.loadFile(file);
        assertEquals("load result", "", fb.getError().toString());
        assertEquals("load count", 1, fb.getMiscCount());
        RecordMisc record2 = (RecordMisc) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            if (record.getField(fieldType) == null) {
                assertNull(fieldType.name(), record2.getField(fieldType));
            } else {
                if ( fieldType == FieldType.indiResidence || fieldType == FieldType.indiMarriedResidence
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
                    assertNull(fieldType.name(), record2.getField(fieldType));
                    assertEquals(fieldType.name(), "", record2.getFieldValue(fieldType));
                } else {
                    //assertNotNull(fieldType.name(), record2.getField(fieldType));
                    assertEquals(fieldType.name(), record.getFieldValue(fieldType), record2.getFieldValue(fieldType));
                }
            }
        }
        assertEquals("place count", 1, fb.getPlaces().size());
        assertEquals("place", place, fb.getPlaces().get(0));


        file.delete();

    }

}
