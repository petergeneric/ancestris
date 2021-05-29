package ancestris.modules.releve.file;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.Record.RecordType;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import ancestris.modules.releve.model.RecordMisc;
import java.io.File;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.openide.util.Exceptions;

/**
 *
 * @author Michel
 */
public class ReleveFileEgmtTest {

     /**
     * Test of saveFile method, of class ReleveFileEgmt.
     */
    @Test
    public void testSaveFileBirth() {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("", "", "", "", "" );

        RecordBirth record = TestUtility.getRecordBirth();
        dataManager.addRecord(record);
        StringBuilder sb = ReleveFileEgmt.saveFile(dataManager, dataManager.getDataModel(), RecordType.BIRTH, file, false);
        assertEquals("verify save error", sb.length(), 0);

        FileBuffer fb;
        try {
            fb = ReleveFileEgmt.loadFile(file);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail(ex.getMessage());
            return;
        }
        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getBirthCount());
        RecordBirth record2 = (RecordBirth) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            switch (fieldType) {
                case indiFatherLastName:
                    assertEquals(fieldType.name(),record.getFieldValue(FieldType.indiLastName),record2.getFieldValue(fieldType));
                    break;
                case wifeFatherLastName:
                    assertNull(fieldType.name(),record2.getField(fieldType));
                    break;
                case indiFatherOccupation:
                case indiMotherOccupation:
                    assertEquals(fieldType.name(), "",record2.getFieldValue(fieldType));
                    break;
                case indiFatherComment:
                    assertEquals(fieldType.name(), "indiFatherComment, indiFatherOccupation, indiFatherResidence, Age:70a",record2.getFieldValue(fieldType));
                    break;
                case indiMotherComment:
                    assertEquals(fieldType.name(), "indiMotherComment, indiMotherOccupation, indiMotherResidence, Age:72a",record2.getFieldValue(fieldType));
                    break;
                case witness1Occupation:
                case witness2Occupation:
                    assertEquals(fieldType.name(), "",record2.getFieldValue(fieldType));
                    break;
                case witness1Comment:
                    assertEquals(fieldType.name(), "w1occupation, w1comment",record2.getFieldValue(fieldType));
                    break;
                case witness2Comment:
                    assertEquals(fieldType.name(), "w2occupation, w2comment",record2.getFieldValue(fieldType));
                    break;
                case generalComment:
                    //assertEquals(fieldType.name(), "generalcomment ",birth2.getFieldValue(fieldType));
                    assertEquals(fieldType.name(), "general comment, témoin(s): w3firstname w3lastname w3occupation w3comment, w4firstname w4lastname w4occupation w4comment ",record2.getFieldValue(fieldType));
                    break;
                case indiFatherAge:
                case indiMotherAge:
                case witness3FirstName:
                case witness3LastName:
                case witness3Occupation:
                case witness3Comment:
                case witness4FirstName:
                case witness4LastName:
                case witness4Occupation:
                case witness4Comment:
                    assertEquals(fieldType.name(), "",record2.getFieldValue(fieldType));
                    break;
                default:
                    // autres champs
                    if (record.getField(fieldType) == null) {
                        assertNull(fieldType.name(), record2.getField(fieldType));
                    } else {
                        if ( fieldType == FieldType.indiBirthDate
                                || fieldType == FieldType.indiResidence || fieldType == FieldType.indiMarriedResidence
                                || fieldType == FieldType.indiFatherResidence || fieldType == FieldType.indiMotherResidence
                                || fieldType == FieldType.wifeResidence || fieldType == FieldType.wifeMarriedResidence
                                || fieldType == FieldType.wifeFatherResidence || fieldType == FieldType.wifeMotherResidence
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
        }

        file.delete();

    }

    /**
     * Test of saveFile method, of class ReleveFileEgmt.
     */
    @Test
    public void testSaveFileMarriage() {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("", "", "", "", "");

        RecordMarriage marriage = TestUtility.getRecordMarriage();
        dataManager.addRecord(marriage);
        StringBuilder sb = ReleveFileEgmt.saveFile(dataManager, dataManager.getDataModel(), RecordType.MARRIAGE, file, false);
        assertEquals("verify save error", 0, sb.length());

        FileBuffer fb;
        try {
            fb = ReleveFileEgmt.loadFile(file);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail(ex.getMessage());
            return;
        }

        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getMarriageCount());
        RecordMarriage marriage2 = (RecordMarriage) fb.getRecords().get(0);

        // je compare tous les champs
        assertEquals("EventDate",   marriage.getFieldValue(FieldType.eventDate),marriage2.getFieldValue(FieldType.eventDate));
        assertEquals("Cote",        marriage.getFieldValue(FieldType.cote),marriage2.getFieldValue(FieldType.cote));
        assertEquals("parish",      marriage.getFieldValue(FieldType.parish),marriage2.getFieldValue(FieldType.parish));
        assertEquals("EventDate",   marriage.getFieldValue(FieldType.eventDate),marriage2.getFieldValue(FieldType.eventDate));
        assertEquals("Notary",      "",  marriage2.getFieldValue(FieldType.notary));
        assertEquals("EventType",   "",  marriage2.getFieldValue(FieldType.eventType));
        assertEquals("FreeComment", marriage.getFieldValue(FieldType.freeComment),marriage2.getFieldValue(FieldType.freeComment));

        assertEquals("IndiFirstName",            marriage.getFieldValue(FieldType.indiFirstName),marriage2.getFieldValue(FieldType.indiFirstName));
        assertEquals("IndiLastName",            marriage.getFieldValue(FieldType.indiLastName),marriage2.getFieldValue(FieldType.indiLastName));
        assertEquals("IndiSex",                 marriage.getFieldValue(FieldType.indiSex),marriage2.getFieldValue(FieldType.indiSex));
        assertEquals("IndiAge",                 marriage.getFieldValue(FieldType.indiAge),marriage2.getFieldValue(FieldType.indiAge));
        assertEquals("IndiBirthDate",           "",marriage2.getFieldValue(FieldType.indiBirthDate));
        assertEquals("IndiBirthPlace",          "",marriage2.getFieldValue(FieldType.indiBirthPlace));
        assertEquals("IndiOccupation",          "",marriage2.getFieldValue(FieldType.indiOccupation));
        assertEquals("IndiComment",            "indiComment, né le 01/02/1990, indiBirthPlace, indiOccupation, Ex conjoint: indiMarriedFirstname indiMarriedLastname Décédé indiMarriedOccupation indiMarriedResidence indiMarriedComment",marriage2.getFieldValue(FieldType.indiComment));
        assertEquals("IndiMarriedFirstName",    "",marriage2.getFieldValue(FieldType.indiMarriedFirstName));
        assertEquals("IndiMarriedLastName",     "",marriage2.getFieldValue(FieldType.indiMarriedLastName));
        assertEquals("IndiMarriedOccupation",   "",marriage2.getFieldValue(FieldType.indiMarriedOccupation));
        assertEquals("IndiMarriedComment",      "".toString(),marriage2.getFieldValue(FieldType.indiMarriedComment));
        assertEquals("IndiMarriedDead",         "",marriage2.getFieldValue(FieldType.indiMarriedDead));
        assertEquals("IndiFatherFirstName",     marriage.getFieldValue(FieldType.indiFatherFirstName),marriage2.getFieldValue(FieldType.indiFatherFirstName));
        assertEquals("IndiFatherLastName",      marriage.getFieldValue(FieldType.indiLastName),marriage2.getFieldValue(FieldType.indiFatherLastName));
        assertEquals("IndiFatherAge",           "",marriage2.getFieldValue(FieldType.indiFatherAge));
        assertEquals("IndiFatherDead",          marriage.getFieldValue(FieldType.indiFatherDead),marriage2.getFieldValue(FieldType.indiFatherDead));
        assertEquals("IndiFatherOccupation",    "",marriage2.getFieldValue(FieldType.indiFatherOccupation));
        assertEquals("IndiFatherComment",       "indiFatherComment, indiFatherOccupation, indiFatherResidence, Age:70a",marriage2.getFieldValue(FieldType.indiFatherComment));
        assertEquals("IndiMotherFirstName",     marriage.getFieldValue(FieldType.indiMotherFirstName),marriage2.getFieldValue(FieldType.indiMotherFirstName));
        assertEquals("IndiMotherLastName",      marriage.getFieldValue(FieldType.indiMotherLastName),marriage2.getFieldValue(FieldType.indiMotherLastName));
        assertEquals("IndiMotherAge",           "",marriage2.getFieldValue(FieldType.indiMotherAge));
        assertEquals("IndiMotherDead",          marriage.getFieldValue(FieldType.indiMotherDead),marriage2.getFieldValue(FieldType.indiMotherDead));
        assertEquals("IndiMotherOccupation",    "",marriage2.getFieldValue(FieldType.indiMotherOccupation));
        assertEquals("IndiMotherComment",      "indiMotherComment, indiMotherOccupation, indiMotherResidence, Age:72a",marriage2.getFieldValue(FieldType.indiMotherComment));

        assertEquals("WifeFirstName",           marriage.getFieldValue(FieldType.wifeFirstName),marriage2.getFieldValue(FieldType.wifeFirstName));
        assertEquals("WifeLastName",            marriage.getFieldValue(FieldType.wifeLastName),marriage2.getFieldValue(FieldType.wifeLastName));
        assertEquals("WifeSex",                 marriage.getFieldValue(FieldType.wifeSex),marriage2.getFieldValue(FieldType.wifeSex));
        assertEquals("WifeAge",                 marriage.getFieldValue(FieldType.wifeAge),marriage2.getFieldValue(FieldType.wifeAge));
        assertEquals("WifeBirthDate",           "",marriage2.getFieldValue(FieldType.wifeBirthDate));
        assertEquals("WifeBirthPlace",          "",marriage2.getFieldValue(FieldType.wifeBirthPlace));
        assertEquals("WifeOccupation",          "",marriage2.getFieldValue(FieldType.wifeOccupation));
        assertEquals("WifeResidence",           marriage.getFieldValue(FieldType.wifeResidence),marriage2.getFieldValue(FieldType.wifeResidence));
        assertEquals("WifeComment",             "wifeComment, né le 02/02/1992, wifeBirthPlace, wifeOccupation, Ex conjoint: wifeMarriedFirstname wifeMarriedLastname Vivant wifeMarriedOccupation wifeMarriedResidence wifeMarriedComment",marriage2.getFieldValue(FieldType.wifeComment));
        assertEquals("WifeMarriedFirstName",    "",marriage2.getFieldValue(FieldType.wifeMarriedFirstName));
        assertEquals("WifeMarriedLastName",     "",marriage2.getFieldValue(FieldType.wifeMarriedLastName));
        assertEquals("WifeMarriedOccupation",   "",marriage2.getFieldValue(FieldType.wifeMarriedOccupation));
        assertEquals("WifeMarriedComment",      "",marriage2.getFieldValue(FieldType.wifeMarriedComment));
        assertEquals("WifeMarriedDead",         "",marriage2.getFieldValue(FieldType.wifeMarriedDead));
        assertEquals("WifeFatherFirstName",     marriage.getFieldValue(FieldType.wifeFatherFirstName),marriage2.getFieldValue(FieldType.wifeFatherFirstName));
        assertEquals("WifeFatherLastName",      marriage.getFieldValue(FieldType.wifeLastName),marriage2.getFieldValue(FieldType.wifeFatherLastName));
        assertEquals("WifeFatherOccupation",    "",marriage2.getFieldValue(FieldType.wifeFatherOccupation));
        assertEquals("WifeFatherAge",           "",marriage2.getFieldValue(FieldType.wifeFatherAge));
        assertEquals("WifeFatherDead",          marriage.getFieldValue(FieldType.wifeFatherDead),marriage2.getFieldValue(FieldType.wifeFatherDead));
        assertEquals("WifeFatherComment",       "wifeFatherComment, wifeFatherOccupation, wifeFatherResidence, Age:71a",marriage2.getFieldValue(FieldType.wifeFatherComment));
        assertEquals("WifeMotherFirstName",     marriage.getFieldValue(FieldType.wifeMotherFirstName),marriage2.getFieldValue(FieldType.wifeMotherFirstName));
        assertEquals("WifeMotherLastName",      marriage.getFieldValue(FieldType.wifeMotherLastName),marriage2.getFieldValue(FieldType.wifeMotherLastName));
        assertEquals("WifeMotherAge",           "",marriage2.getFieldValue(FieldType.wifeMotherAge));
        assertEquals("WifeMotherDead",          marriage.getFieldValue(FieldType.wifeMotherDead),marriage2.getFieldValue(FieldType.wifeMotherDead));
        assertEquals("WifeMotherOccupation",    "",marriage2.getFieldValue(FieldType.wifeMotherOccupation));
        assertEquals("WifeMotherComment",       "wifeMotherComment, wifeMotherOccupation, wifeMotherResidence, Age:73a",marriage2.getFieldValue(FieldType.wifeMotherComment));

        assertWitnesses(marriage, marriage2);

//        assertEquals("Witness1FirstName",     marriage.getWitness1FirstName().toString(),marriage2.getWitness1FirstName().toString());
//        assertEquals("Witness1LastName",      marriage.getWitness1LastName().toString(),marriage2.getWitness1LastName().toString());
//        assertEquals("Witness1Occupation",    "",marriage2.getWitness1Occupation().toString());
//        assertEquals("Witness1Comment",       "w1comment, w1occupation",marriage2.getWitness1Comment().toString());
//        assertEquals("Witness2FirstName",     marriage.getWitness2FirstName().toString(),marriage2.getWitness2FirstName().toString());
//        assertEquals("Witness2LastName",      marriage.getWitness2LastName().toString(),marriage2.getWitness2LastName().toString());
//        assertEquals("Witness2Occupation",    "",marriage2.getWitness2Occupation().toString());
//        assertEquals("Witness2Comment",       "w2comment, w2occupation",marriage2.getWitness2Comment().toString());
//        assertEquals("Witness3FirstName",     "",marriage2.getWitness3FirstName().toString());
//        assertEquals("Witness3LastName",      "",marriage2.getWitness3LastName().toString());
//        assertEquals("Witness3Occupation",    "",marriage2.getWitness3Occupation().toString());
//        assertEquals("Witness3Comment",       "",marriage2.getWitness3Comment().toString());
//        assertEquals("Witness4FirstName",     "",marriage2.getWitness4FirstName().toString());
//        assertEquals("Witness4LastName",      "",marriage2.getWitness4LastName().toString());
//        assertEquals("Witness4Occupation",    "",marriage2.getWitness4Occupation().toString());
//        assertEquals("Witness4Comment",       "",marriage2.getWitness4Comment().toString());
//
//        assertEquals("GeneralComment", "generalcomment, témoin(s): w3firstname w3lastname w3occupation w3comment, w4firstname w4lastname w4occupation w4comment ",marriage2.getFieldValue(FieldType.generalComment));

        file.delete();

    }

    private void assertWitnesses(Record record, Record record2) {
        assertEquals("Witness1 " + " FirstName" , record.getFieldValue(FieldType.witness1FirstName),record2.getFieldValue(FieldType.witness1FirstName));
        assertEquals("Witness1 " + " LastName" ,  record.getFieldValue(FieldType.witness1LastName),record2.getFieldValue(FieldType.witness1LastName));
        assertEquals("Witness1 " + " Occupation" , "",  record2.getFieldValue(FieldType.witness1Occupation));
        assertEquals("Witness1 " + " Comment",    record.getFieldValue(FieldType.witness1Occupation)+ ", " + record.getFieldValue(FieldType.witness1Comment)
                     , record2.getFieldValue(FieldType.witness1Comment));

        assertEquals("Witness2 " + " FirstName" , record.getFieldValue(FieldType.witness2FirstName),record2.getFieldValue(FieldType.witness2FirstName));
        assertEquals("Witness2 " + " LastName" ,  record.getFieldValue(FieldType.witness2LastName),record2.getFieldValue(FieldType.witness2LastName));
        assertEquals("Witness2 " + " Occupation" ,  "",  record2.getFieldValue(FieldType.witness2Occupation));
        assertEquals("Witness2 " + " Comment",    record.getFieldValue(FieldType.witness2Occupation)+ ", " + record.getFieldValue(FieldType.witness2Comment)
                     , record2.getFieldValue(FieldType.witness2Comment));

        assertEquals("Witness3 " + " FirstName" ,  "", record2.getFieldValue(FieldType.witness3FirstName));
        assertEquals("Witness3 " + " LastName" ,   "",  record2.getFieldValue(FieldType.witness3LastName));
        assertEquals("Witness3 " + " Occupation",  "",record2.getFieldValue(FieldType.witness3Occupation));
        assertEquals("Witness3 "+ " Comment",      "", record2.getFieldValue(FieldType.witness3Comment));

        assertEquals("Witness4 " + " FirstName" ,  "", record2.getFieldValue(FieldType.witness4FirstName));
        assertEquals("Witness4 " + " LastName" ,   "",  record2.getFieldValue(FieldType.witness4LastName));
        assertEquals("Witness4 " + " Occupation",  "",record2.getFieldValue(FieldType.witness4Occupation));
        assertEquals("Witness4 "+ " Comment",      "", record2.getFieldValue(FieldType.witness4Comment));

    }

    /**
     * Test de l'enregistrement d'un deces
     */
    @Test
    public void testSaveFileDeath() {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("cityname","citycode","county","state","country");
        String place = dataManager.getPlace().getValue();

        RecordDeath death = TestUtility.getRecordDeath();
        dataManager.addRecord(death);
        StringBuilder sb = ReleveFileEgmt.saveFile(dataManager, dataManager.getDataModel(), RecordType.DEATH, file, false);
        assertEquals("verify save error", "", sb.toString());

        FileBuffer fb;
        try {
            fb = ReleveFileEgmt.loadFile(file);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail(ex.getMessage());
            return;
        }
        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getDeathCount());
        RecordDeath death2 = (RecordDeath) fb.getRecords().get(0);

        // je compare tous les champs

        assertEquals("EventDate",       death.getFieldValue(FieldType.eventDate),death2.getFieldValue(FieldType.eventDate));
        assertEquals("Cote",            death.getFieldValue(FieldType.cote),death2.getFieldValue(FieldType.cote));
        assertEquals("parish",          death.getFieldValue(FieldType.parish),death2.getFieldValue(FieldType.parish));
        assertEquals("EventDate",       death.getFieldValue(FieldType.eventDate),death2.getFieldValue(FieldType.eventDate));
        assertEquals("Notary",          "", death2.getFieldValue(FieldType.notary));
        assertEquals("EventType",       "", death2.getFieldValue(FieldType.eventType));
        assertEquals("FreeComment",    death.getFieldValue(FieldType.freeComment),death2.getFieldValue(FieldType.freeComment));

        assertEquals("IndiFirstName",  death.getFieldValue(FieldType.indiFirstName),death2.getFieldValue(FieldType.indiFirstName));
        assertEquals("IndiLastName",   death.getFieldValue(FieldType.indiLastName),death2.getFieldValue(FieldType.indiLastName));
        assertEquals("IndiSex",        death.getFieldValue(FieldType.indiSex),death2.getFieldValue(FieldType.indiSex));
        assertEquals("IndiAge",        death.getFieldValue(FieldType.indiAge),death2.getFieldValue(FieldType.indiAge));
        assertEquals("IndiBirthDate",  "",death2.getFieldValue(FieldType.indiBirthDate));
        assertEquals("IndiBirthPlace", "",death2.getFieldValue(FieldType.indiBirthPlace));
        assertEquals("IndiPlace",      "",death2.getFieldValue(FieldType.indiBirthPlace));
        assertEquals("IndiOccupation", "",death2.getFieldValue(FieldType.indiOccupation));
        assertEquals("IndiComment",    "indiComment, né le 01/02/1990, indiBirthPlace, indiOccupation",death2.getFieldValue(FieldType.indiComment));
        assertEquals("IndiMarriedFirstName",    death.getFieldValue(FieldType.indiMarriedFirstName),death2.getFieldValue(FieldType.indiMarriedFirstName));
        assertEquals("IndiMarriedLastName",     death.getFieldValue(FieldType.indiMarriedLastName),death2.getFieldValue(FieldType.indiMarriedLastName));
        assertEquals("IndiMarriedOccupation",   "",death2.getFieldValue(FieldType.indiMarriedOccupation));
        assertEquals("IndiMarriedComment",      death.getFieldValue(FieldType.indiMarriedComment),death2.getFieldValue(FieldType.indiMarriedComment));
        assertEquals("IndiMarriedDead",         death.getFieldValue(FieldType.indiMarriedDead),death2.getFieldValue(FieldType.indiMarriedDead));

        assertEquals("IndiFatherFirstName",     death.getFieldValue(FieldType.indiFatherFirstName),death2.getFieldValue(FieldType.indiFatherFirstName));
        assertEquals("IndiFatherLastName",         death.getFieldValue(FieldType.indiLastName),death2.getFieldValue(FieldType.indiFatherLastName));
        assertEquals("IndiFatherAge",           "",death2.getFieldValue(FieldType.indiFatherAge));
        assertEquals("IndiFatherDead",          death.getFieldValue(FieldType.indiFatherDead),death2.getFieldValue(FieldType.indiFatherDead));
        assertEquals("IndiFatherOccupation",    "",death2.getFieldValue(FieldType.indiFatherOccupation));
        assertEquals("IndiFatherComment",       "indiFatherComment, indiFatherOccupation, indiFatherResidence, Age:70a",death2.getFieldValue(FieldType.indiFatherComment));
        assertEquals("IndiMotherFirstName",     death.getFieldValue(FieldType.indiMotherFirstName),death2.getFieldValue(FieldType.indiMotherFirstName));
        assertEquals("IndiMotherLastName",      death.getFieldValue(FieldType.indiMotherLastName),death2.getFieldValue(FieldType.indiMotherLastName));
        assertEquals("IndiMotherAge",           "",death2.getFieldValue(FieldType.indiMotherAge));
        assertEquals("IndiMotherDead",          death.getFieldValue(FieldType.indiMotherDead),death2.getFieldValue(FieldType.indiMotherDead));
        assertEquals("IndiMotherOccupation",    "",death2.getFieldValue(FieldType.indiMotherOccupation));
        assertEquals("IndiMotherComment",       "indiMotherComment, indiMotherOccupation, indiMotherResidence, Age:72a",death2.getFieldValue(FieldType.indiMotherComment));

        assertEquals("WifeFirstName",           "", death2.getFieldValue(FieldType.wifeFirstName));
        assertEquals("WifeLastName",            "", death2.getFieldValue(FieldType.wifeLastName));
        assertEquals("WifeSex",                 "", death2.getFieldValue(FieldType.wifeSex));
        assertEquals("WifeAge",                 "", death2.getFieldValue(FieldType.wifeAge));
        assertEquals("WifeBirthDate",           "", death2.getFieldValue(FieldType.wifeBirthDate));
        assertEquals("WifePlace",               "", death2.getFieldValue(FieldType.wifeBirthPlace));
        assertEquals("WifeOccupation",          "", death2.getFieldValue(FieldType.wifeOccupation));
        assertEquals("WifeResidence",           "", death2.getFieldValue(FieldType.wifeResidence));
        assertEquals("WifeComment",             "", death2.getFieldValue(FieldType.wifeComment));
        assertEquals("WifeMarriedFirstName",    "", death2.getFieldValue(FieldType.wifeMarriedFirstName));
        assertEquals("WifeMarriedLastName",     "", death2.getFieldValue(FieldType.wifeMarriedLastName));
        assertEquals("WifeMarriedOccupation",   "", death2.getFieldValue(FieldType.wifeMarriedOccupation));
        assertEquals("WifeMarriedComment",      "", death2.getFieldValue(FieldType.wifeMarriedComment));
        assertEquals("WifeMarriedDead",         "", death2.getFieldValue(FieldType.wifeMarriedDead));
        assertEquals("WifeFatherFirstName",     "", death2.getFieldValue(FieldType.wifeFatherFirstName));
        assertEquals("WifeFatherLastName",      "", death2.getFieldValue(FieldType.wifeFatherLastName));
        assertEquals("WifeFatherAge",           "", death2.getFieldValue(FieldType.wifeFatherAge));
        assertEquals("WifeFatherDead",          "", death2.getFieldValue(FieldType.wifeFatherDead));
        assertEquals("WifeFatherOccupation",    "", death2.getFieldValue(FieldType.wifeFatherOccupation));
        assertEquals("WifeFatherComment",       "", death2.getFieldValue(FieldType.wifeFatherComment));
        assertEquals("WifeMotherFirstName",     "", death2.getFieldValue(FieldType.wifeMotherFirstName));
        assertEquals("WifeMotherLastName",      "", death2.getFieldValue(FieldType.wifeMotherLastName));
        assertEquals("WifeMotherAge",           "", death2.getFieldValue(FieldType.wifeMotherAge));
        assertEquals("WifeMotherDead",          "", death2.getFieldValue(FieldType.wifeMotherDead));
        assertEquals("WifeMotherOccupation",    "", death2.getFieldValue(FieldType.wifeMotherOccupation));
        assertEquals("WifeMotherComment",       "", death2.getFieldValue(FieldType.wifeMotherComment));

        assertWitnesses(death, death2);
        assertEquals("GeneralComment", "generalcomment, témoin(s): w3firstname w3lastname w3occupation w3comment, w4firstname w4lastname w4occupation w4comment ",death2.getFieldValue(FieldType.generalComment));

        file.delete();

    }

    /**
     * Test of saveFile method, of class ReleveFileEgmt.
     */
    @Test
    public void testSaveFileMarriageContract() {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("", "","","","");

        RecordMisc misc = TestUtility.getRecordMisc();
        misc.setFieldValue(FieldType.eventType, "contrat de mariage");
        dataManager.addRecord(misc);
        StringBuilder sb = ReleveFileEgmt.saveFile(dataManager, dataManager.getDataModel(), RecordType.MISC, file, false);
        assertEquals("verify save error", 0, sb.length());

        FileBuffer fb;
        try {
            fb = ReleveFileEgmt.loadFile(file);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail(ex.getMessage());
            return;
        }
        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getMiscCount());
        RecordMisc misc2 = (RecordMisc) fb.getRecords().get(0);

        assertEquals("EventDate",   misc.getFieldValue(FieldType.eventDate),misc2.getFieldValue(FieldType.eventDate));
        assertEquals("EventType",   "MARC",misc2.getFieldValue(FieldType.eventType));
        assertEquals("parish",      misc.getFieldValue(FieldType.parish),misc2.getFieldValue(FieldType.parish));
        assertEquals("Notary",      misc.getFieldValue(FieldType.notary),misc2.getFieldValue(FieldType.notary));
        assertEquals("Cote",        misc.getFieldValue(FieldType.cote),misc2.getFieldValue(FieldType.cote));
        assertEquals("FreeComment", misc.getFieldValue(FieldType.freeComment),misc2.getFieldValue(FieldType.freeComment));

        assertEquals("IndiFirstName",           misc.getFieldValue(FieldType.indiFirstName),misc2.getFieldValue(FieldType.indiFirstName));
        assertEquals("IndiLastName",            misc.getFieldValue(FieldType.indiLastName),misc2.getFieldValue(FieldType.indiLastName));
        assertEquals("IndiSex",                 misc.getFieldValue(FieldType.indiSex),misc2.getFieldValue(FieldType.indiSex));
        assertEquals("IndiAge",                 misc.getFieldValue(FieldType.indiAge),misc2.getFieldValue(FieldType.indiAge));
        assertEquals("IndiBirthDate",           "",misc2.getFieldValue(FieldType.indiBirthDate));
        assertEquals("IndiBirthPlace",          "",misc2.getFieldValue(FieldType.indiBirthPlace));
        assertEquals("IndiPlace",               "",misc2.getFieldValue(FieldType.indiBirthPlace));
        assertEquals("IndiOccupation",          "",misc2.getFieldValue(FieldType.indiOccupation));
        assertEquals("IndiComment",             "indiComment, né le 01/02/1990, indiBirthPlace, indiOccupation, Ex conjoint: indiMarriedFirstname indiMarriedLastname Décédé indiMarriedOccupation indiMarriedResidence indiMarriedComment",misc2.getFieldValue(FieldType.indiComment));
        assertEquals("IndiMarriedFirstName",    "",misc2.getFieldValue(FieldType.indiMarriedFirstName));
        assertEquals("IndiMarriedLastName",     "",misc2.getFieldValue(FieldType.indiMarriedLastName));
        assertEquals("IndiMarriedOccupation",   "",misc2.getFieldValue(FieldType.indiMarriedOccupation));
        assertEquals("IndiMarriedComment",      "",misc2.getFieldValue(FieldType.indiMarriedComment));
        assertEquals("IndiMarriedDead",         "",misc2.getFieldValue(FieldType.indiMarriedDead));
        assertEquals("IndiFatherFirstName",     misc.getFieldValue(FieldType.indiFatherFirstName),misc2.getFieldValue(FieldType.indiFatherFirstName));
        assertEquals("IndiFatherLastName",      misc.getFieldValue(FieldType.indiLastName),misc2.getFieldValue(FieldType.indiFatherLastName));
        assertEquals("IndiFatherAge",           "",misc2.getFieldValue(FieldType.indiFatherAge));
        assertEquals("IndiFatherDead",          misc.getFieldValue(FieldType.indiFatherDead),misc2.getFieldValue(FieldType.indiFatherDead));
        assertEquals("IndiFatherOccupation",    "",misc2.getFieldValue(FieldType.indiFatherOccupation));
        assertEquals("IndiFatherComment",       "indiFatherComment, indiFatherOccupation, indiFatherResidence, Age:70a",misc2.getFieldValue(FieldType.indiFatherComment));
        assertEquals("IndiMotherFirstName",     misc.getFieldValue(FieldType.indiMotherFirstName),misc2.getFieldValue(FieldType.indiMotherFirstName));
        assertEquals("IndiMotherLastName",      misc.getFieldValue(FieldType.indiMotherLastName),misc2.getFieldValue(FieldType.indiMotherLastName));
        assertEquals("IndiMotherAge",           "",misc2.getFieldValue(FieldType.indiMotherAge));
        assertEquals("IndiMotherDead",          misc.getFieldValue(FieldType.indiMotherDead),misc2.getFieldValue(FieldType.indiMotherDead));
        assertEquals("IndiMotherOccupation",    "",misc2.getFieldValue(FieldType.indiMotherOccupation));
        assertEquals("IndiMotherComment",      "indiMotherComment, indiMotherOccupation, indiMotherResidence, Age:72a",misc2.getFieldValue(FieldType.indiMotherComment));

        assertEquals("WifeFirstName",           misc.getFieldValue(FieldType.wifeFirstName),misc2.getFieldValue(FieldType.wifeFirstName));
        assertEquals("WifeLastName",            misc.getFieldValue(FieldType.wifeLastName),misc2.getFieldValue(FieldType.wifeLastName));
        assertEquals("WifeSex",                 misc.getFieldValue(FieldType.wifeSex),misc2.getFieldValue(FieldType.wifeSex));
        assertEquals("WifeAge",                 misc.getFieldValue(FieldType.wifeAge),misc2.getFieldValue(FieldType.wifeAge));
        assertEquals("WifeBirthDate",           "",misc2.getFieldValue(FieldType.wifeBirthDate));
        assertEquals("WifeBirthPlace",          "",misc2.getFieldValue(FieldType.wifeBirthPlace));
        assertEquals("WifeOccupation",          "",misc2.getFieldValue(FieldType.wifeOccupation));
        assertEquals("WifeResidence",           misc.getFieldValue(FieldType.wifeResidence),misc2.getFieldValue(FieldType.wifeResidence));
        assertEquals("WifeComment",             "wifeComment, né le 02/02/1992, wifeBirthPlace, wifeOccupation, Ex conjoint: wifeMarriedFirstname wifeMarriedLastname Décédé wifeMarriedOccupation wifeMarriedResidence wifeMarriedComment",misc2.getFieldValue(FieldType.wifeComment));
        assertEquals("WifeMarriedFirstName",    "",misc2.getFieldValue(FieldType.wifeMarriedFirstName));
        assertEquals("WifeMarriedLastName",     "",misc2.getFieldValue(FieldType.wifeMarriedLastName));
        assertEquals("WifeMarriedOccupation",   "",misc2.getFieldValue(FieldType.wifeMarriedOccupation));
        assertEquals("WifeMarriedComment",      "",misc2.getFieldValue(FieldType.wifeMarriedComment));
        assertEquals("WifeMarriedDead",         "",misc2.getFieldValue(FieldType.wifeMarriedDead));
        assertEquals("WifeFatherFirstName",     misc.getFieldValue(FieldType.wifeFatherFirstName),misc2.getFieldValue(FieldType.wifeFatherFirstName));
        assertEquals("WifeFatherLastName",      misc.getFieldValue(FieldType.wifeLastName),misc2.getFieldValue(FieldType.wifeFatherLastName));
        assertEquals("WifeFatherAge",           "",misc2.getFieldValue(FieldType.wifeFatherAge));
        assertEquals("WifeFatherOccupation",    "",misc2.getFieldValue(FieldType.wifeFatherOccupation));
        assertEquals("WifeFatherComment",       "wifeFatherComment, wifeFatherOccupation, wifeFatherResidence, Age:71a",misc2.getFieldValue(FieldType.wifeFatherComment));
        assertEquals("WifeFatherDead",          misc.getFieldValue(FieldType.wifeFatherDead),misc2.getFieldValue(FieldType.wifeFatherDead));
        assertEquals("WifeMotherFirstName",     misc.getFieldValue(FieldType.wifeMotherFirstName),misc2.getFieldValue(FieldType.wifeMotherFirstName));
        assertEquals("WifeMotherLastName",      misc.getFieldValue(FieldType.wifeMotherLastName),misc2.getFieldValue(FieldType.wifeMotherLastName));
        assertEquals("WifeMotherAge",           "",misc2.getFieldValue(FieldType.wifeMotherAge));
        assertEquals("WifeMotherDead",          misc.getFieldValue(FieldType.wifeMotherDead),misc2.getFieldValue(FieldType.wifeMotherDead));
        assertEquals("WifeMotherOccupation",    "",misc2.getFieldValue(FieldType.wifeMotherOccupation));
        assertEquals("WifeMotherComment",       "wifeMotherComment, wifeMotherOccupation, wifeMotherResidence, Age:73a",misc2.getFieldValue(FieldType.wifeMotherComment));

        assertWitnesses(misc, misc2);
        assertEquals("GeneralComment", "generalcomment, témoin(s): w3firstname w3lastname w3occupation w3comment, w4firstname w4lastname w4occupation w4comment, insinué le 04/04/2012",misc2.getFieldValue(FieldType.generalComment));


        file.delete();

    }

/**
     * Test of saveFile method, of class ReleveFileEgmt.
     */
    @Test
    public void testSaveFileTestament() {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("", "","","","");

        RecordMisc misc = TestUtility.getRecordMisc();
        misc.setFieldValue(FieldType.eventType, "testament");
        dataManager.addRecord(misc);
        StringBuilder sb = ReleveFileEgmt.saveFile(dataManager, dataManager.getDataModel(), RecordType.MISC, file, false);
        assertEquals("verify save error", 0, sb.length());

        FileBuffer fb;
        try {
            fb = ReleveFileEgmt.loadFile(file);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail(ex.getMessage());
            return;
        }

        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getMiscCount());
        RecordMisc misc2 = (RecordMisc) fb.getRecords().get(0);

        assertEquals("EventDate",   misc.getFieldValue(FieldType.eventDate),misc2.getFieldValue(FieldType.eventDate));
        assertEquals("EventType",   "WILL",misc2.getFieldValue(FieldType.eventType));
        assertEquals("parish",      misc.getFieldValue(FieldType.parish),misc2.getFieldValue(FieldType.parish));
        assertEquals("Notary",      misc.getFieldValue(FieldType.notary),misc2.getFieldValue(FieldType.notary));
        assertEquals("Cote",        misc.getFieldValue(FieldType.cote),misc2.getFieldValue(FieldType.cote));
        assertEquals("FreeComment", misc.getFieldValue(FieldType.freeComment),misc2.getFieldValue(FieldType.freeComment));

        assertEquals("IndiFirstName",           misc.getFieldValue(FieldType.indiFirstName),misc2.getFieldValue(FieldType.indiFirstName));
        assertEquals("IndiLastName",            misc.getFieldValue(FieldType.indiLastName),misc2.getFieldValue(FieldType.indiLastName));
        assertEquals("IndiSex",                 misc.getFieldValue(FieldType.indiSex),misc2.getFieldValue(FieldType.indiSex));
        assertEquals("IndiAge",                 misc.getFieldValue(FieldType.indiAge),misc2.getFieldValue(FieldType.indiAge));
        assertEquals("IndiBirthDate",           "",misc2.getFieldValue(FieldType.indiBirthDate));
        assertEquals("IndiBirthPlace",          "",misc2.getFieldValue(FieldType.indiBirthPlace));
        assertEquals("IndiPlace",               "",misc2.getFieldValue(FieldType.indiBirthPlace));
        assertEquals("IndiOccupation",          "",misc2.getFieldValue(FieldType.indiOccupation));
        assertEquals("IndiComment",             "indiComment, né le 01/02/1990, indiBirthPlace, indiOccupation",misc2.getFieldValue(FieldType.indiComment));

        assertEquals("IndiMarriedFirstName",    misc.getFieldValue(FieldType.indiMarriedFirstName),misc2.getFieldValue(FieldType.indiMarriedFirstName));
        assertEquals("IndiMarriedLastName",     misc.getFieldValue(FieldType.indiMarriedLastName),misc2.getFieldValue(FieldType.indiMarriedLastName));
        //assertEquals("WifeSex",                 misc.getIndi().getMarriedSex().toString(),misc2.getFieldValue(FieldType.wifeSex));
        //assertEquals("WifeAge",                 misc.getFieldValue(FieldType.wifeAge),misc2.getFieldValue(FieldType.wifeAge));
        //assertEquals("WifeBirthDate",           misc.getFieldValue(FieldType.indiMarriedDead),misc2.getWifeD().toString());
        assertEquals("IndiMarriedDead",         misc.getFieldValue(FieldType.indiMarriedDead),misc2.getFieldValue(FieldType.indiMarriedDead));
        //assertEquals("WifeBirthPlace",          "",misc2.getFieldValue(FieldType.wifeBirthPlace));
        assertEquals("IndiMarriedOccupation",   "",misc2.getFieldValue(FieldType.indiMarriedOccupation));
        assertEquals("IndiMarriedResidence",    misc.getFieldValue(FieldType.indiMarriedResidence),misc2.getFieldValue(FieldType.indiMarriedResidence));
        assertEquals("IndiMarriedComment",      "indiMarriedOccupation, indiMarriedComment",misc2.getFieldValue(FieldType.indiMarriedComment));

        assertEquals("IndiFatherFirstName",     misc.getFieldValue(FieldType.indiFatherFirstName),misc2.getFieldValue(FieldType.indiFatherFirstName));
        assertEquals("IndiFatherLastName",      misc.getFieldValue(FieldType.indiLastName),misc2.getFieldValue(FieldType.indiFatherLastName));
        assertEquals("IndiFatherAge",           "",misc2.getFieldValue(FieldType.indiFatherAge));
        assertEquals("IndiFatherDead",          misc.getFieldValue(FieldType.indiFatherDead),misc2.getFieldValue(FieldType.indiFatherDead));
        assertEquals("IndiFatherOccupation",    "",misc2.getFieldValue(FieldType.indiFatherOccupation));
        assertEquals("IndiFatherComment",       "indiFatherComment, indiFatherOccupation, indiFatherResidence, Age:70a",misc2.getFieldValue(FieldType.indiFatherComment));
        assertEquals("IndiMotherFirstName",     misc.getFieldValue(FieldType.indiMotherFirstName),misc2.getFieldValue(FieldType.indiMotherFirstName));
        assertEquals("IndiMotherLastName",      misc.getFieldValue(FieldType.indiMotherLastName),misc2.getFieldValue(FieldType.indiMotherLastName));
        assertEquals("IndiMotherAge",           "",misc2.getFieldValue(FieldType.indiMotherAge));
        assertEquals("IndiMotherDead",          misc.getFieldValue(FieldType.indiMotherDead),misc2.getFieldValue(FieldType.indiMotherDead));
        assertEquals("IndiMotherOccupation",    "",misc2.getFieldValue(FieldType.indiMotherOccupation));
        assertEquals("IndiMotherComment",       "indiMotherComment, indiMotherOccupation, indiMotherResidence, Age:72a",misc2.getFieldValue(FieldType.indiMotherComment));

        assertEquals("WifeFirstName",           "",misc2.getFieldValue(FieldType.wifeFirstName));
        assertEquals("WifeLastName",            "",misc2.getFieldValue(FieldType.wifeLastName));
        assertEquals("WifeSex",                 "",misc2.getFieldValue(FieldType.wifeSex));
        assertEquals("WifeAge",                 "",misc2.getFieldValue(FieldType.wifeAge));
        assertEquals("WifeBirthDate",           "",misc2.getFieldValue(FieldType.wifeBirthDate));
        assertEquals("WifeBirthPlace",          "",misc2.getFieldValue(FieldType.wifeBirthPlace));
        assertEquals("WifeOccupation",          "",misc2.getFieldValue(FieldType.wifeOccupation));
        assertEquals("WifeResidence",           "",misc2.getFieldValue(FieldType.wifeResidence));
        assertEquals("WifeComment",             "",misc2.getFieldValue(FieldType.wifeComment));
        assertEquals("WifeMarriedFirstName",    "",misc2.getFieldValue(FieldType.wifeMarriedFirstName));
        assertEquals("WifeMarriedLastName",     "",misc2.getFieldValue(FieldType.wifeMarriedLastName));
        assertEquals("WifeMarriedOccupation",   "",misc2.getFieldValue(FieldType.wifeMarriedOccupation));
        assertEquals("WifeMarriedComment",      "",misc2.getFieldValue(FieldType.wifeMarriedComment));
        assertEquals("WifeMarriedDead",         "",misc2.getFieldValue(FieldType.wifeMarriedDead));
        assertEquals("WifeFatherFirstName",     "",misc2.getFieldValue(FieldType.wifeFatherFirstName));
        assertEquals("WifeFatherLastName",      "",misc2.getFieldValue(FieldType.wifeFatherLastName));
        assertEquals("WifeFatherAge",           "",misc2.getFieldValue(FieldType.wifeFatherAge));
        assertEquals("WifeFatherOccupation",    "",misc2.getFieldValue(FieldType.wifeFatherOccupation));
        assertEquals("WifeFatherComment",       "",misc2.getFieldValue(FieldType.wifeFatherComment));
        assertEquals("WifeFatherDead",          "",misc2.getFieldValue(FieldType.wifeFatherDead));
        assertEquals("WifeMotherFirstName",     "",misc2.getFieldValue(FieldType.wifeMotherFirstName));
        assertEquals("WifeMotherLastName",      "",misc2.getFieldValue(FieldType.wifeMotherLastName));
        assertEquals("WifeMotherAge",           "",misc2.getFieldValue(FieldType.wifeMotherAge));
        assertEquals("WifeMotherDead",          "",misc2.getFieldValue(FieldType.wifeMotherDead));
        assertEquals("WifeMotherOccupation",    "",misc2.getFieldValue(FieldType.wifeMotherOccupation));
        assertEquals("WifeMotherComment",       "",misc2.getFieldValue(FieldType.wifeMotherComment));

        assertWitnesses(misc, misc2);
        assertEquals("GeneralComment", "generalcomment, témoin(s): w3firstname w3lastname w3occupation w3comment, w4firstname w4lastname w4occupation w4comment, insinué le 04/04/2012, Héritier: wifeFirstname wifeLastname, né le 02/02/1992 wifeBirthPlace, wifeComment, wifeOccupation, wifeResidence, Père de l'héritier: wifeFatherFirstname wifeFatherLastname Age:71a Vivant wifeFatherOccupation wifeFatherResidence wifeFatherComment, Mère de l'héritier: wifeMotherFirstname wifeMotherLastname Age:73a Vivant wifeMotherOccupation wifeMotherResidence wifeMotherComment, Conjoint de l'héritier: wifeMarriedFirstname wifeMarriedLastname Décédé wifeMarriedOccupation wifeMarriedResidence wifeMotherComment",misc2.getFieldValue(FieldType.generalComment));

        file.delete();

    }

    /**
     * Test of saveFile method, of class ReleveFileEgmt.
     */
    @Test
    public void testSaveFileMisc() {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("", "","","","");

        RecordMisc misc = TestUtility.getRecordMisc();
        dataManager.addRecord(misc);
        StringBuilder sb = ReleveFileEgmt.saveFile(dataManager, dataManager.getDataModel(), RecordType.MISC, file, false);
        assertEquals("verify save error", 0, sb.length());

        FileBuffer fb;
        try {
            fb = ReleveFileEgmt.loadFile(file);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail(ex.getMessage());
            return;
        }

        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getMiscCount());
        RecordMisc misc2 = (RecordMisc) fb.getRecords().get(0);

        assertEquals("EventDate",   misc.getFieldValue(FieldType.eventDate),misc2.getFieldValue(FieldType.eventDate));
        assertEquals("EventType",   misc.getFieldValue(FieldType.eventType),misc2.getFieldValue(FieldType.eventType));
        assertEquals("parish",      misc.getFieldValue(FieldType.parish),misc2.getFieldValue(FieldType.parish));
        assertEquals("Notary",      misc.getFieldValue(FieldType.notary),misc2.getFieldValue(FieldType.notary));
        assertEquals("Cote",        misc.getFieldValue(FieldType.cote),misc2.getFieldValue(FieldType.cote));
        assertEquals("FreeComment", misc.getFieldValue(FieldType.freeComment),misc2.getFieldValue(FieldType.freeComment));

        assertEquals("IndiFirstName",           misc.getFieldValue(FieldType.indiFirstName),misc2.getFieldValue(FieldType.indiFirstName));
        assertEquals("IndiLastName",            misc.getFieldValue(FieldType.indiLastName),misc2.getFieldValue(FieldType.indiLastName));
        assertEquals("IndiSex",                 misc.getFieldValue(FieldType.indiSex),misc2.getFieldValue(FieldType.indiSex));
        assertEquals("IndiAge",                 misc.getFieldValue(FieldType.indiAge),misc2.getFieldValue(FieldType.indiAge));
        assertEquals("IndiBirthDate",           "",misc2.getFieldValue(FieldType.indiBirthDate));
        assertEquals("IndiBirthPlace",          "",misc2.getFieldValue(FieldType.indiBirthPlace));
        assertEquals("IndiPlace",               "",misc2.getFieldValue(FieldType.indiBirthPlace));
        assertEquals("IndiOccupation",          "",misc2.getFieldValue(FieldType.indiOccupation));
        assertEquals("IndiComment",             "indiComment, né le 01/02/1990, indiBirthPlace, indiOccupation",misc2.getFieldValue(FieldType.indiComment));

        assertEquals("IndiMarriedFirstName",    misc.getFieldValue(FieldType.indiMarriedFirstName),misc2.getFieldValue(FieldType.indiMarriedFirstName));
        assertEquals("IndiMarriedLastName",     misc.getFieldValue(FieldType.indiMarriedLastName),misc2.getFieldValue(FieldType.indiMarriedLastName));
        assertEquals("IndiMarriedDead",         misc.getFieldValue(FieldType.indiMarriedDead),misc2.getFieldValue(FieldType.indiMarriedDead));
        assertEquals("IndiMarriedOccupation",   "",misc2.getFieldValue(FieldType.indiMarriedOccupation));
        assertEquals("IndiMarriedResidence",    misc.getFieldValue(FieldType.indiMarriedResidence),misc2.getFieldValue(FieldType.indiMarriedResidence));
        assertEquals("IndiMarriedComment",      "indiMarriedOccupation, indiMarriedComment",misc2.getFieldValue(FieldType.indiMarriedComment));

        assertEquals("IndiFatherFirstName",     misc.getFieldValue(FieldType.indiFatherFirstName),misc2.getFieldValue(FieldType.indiFatherFirstName));
        assertEquals("IndiFatherLastName",      misc.getFieldValue(FieldType.indiLastName),misc2.getFieldValue(FieldType.indiFatherLastName));
        assertEquals("IndiFatherAge",           "",misc2.getFieldValue(FieldType.indiFatherAge));
        assertEquals("IndiFatherDead",          misc.getFieldValue(FieldType.indiFatherDead),misc2.getFieldValue(FieldType.indiFatherDead));
        assertEquals("IndiFatherOccupation",    "",misc2.getFieldValue(FieldType.indiFatherOccupation));
        assertEquals("IndiFatherComment",       "indiFatherComment, indiFatherOccupation, indiFatherResidence, Age:70a",misc2.getFieldValue(FieldType.indiFatherComment));
        assertEquals("IndiMotherFirstName",     misc.getFieldValue(FieldType.indiMotherFirstName),misc2.getFieldValue(FieldType.indiMotherFirstName));
        assertEquals("IndiMotherLastName",      misc.getFieldValue(FieldType.indiMotherLastName),misc2.getFieldValue(FieldType.indiMotherLastName));
        assertEquals("IndiMotherAge",           "",misc2.getFieldValue(FieldType.indiMotherAge));
        assertEquals("IndiMotherDead",          misc.getFieldValue(FieldType.indiMotherDead),misc2.getFieldValue(FieldType.indiMotherDead));
        assertEquals("IndiMotherOccupation",    "",misc2.getFieldValue(FieldType.indiMotherOccupation));
        assertEquals("IndiMotherComment",      "indiMotherComment, indiMotherOccupation, indiMotherResidence, Age:72a",misc2.getFieldValue(FieldType.indiMotherComment));

        assertEquals("WifeFirstName",           "",misc2.getFieldValue(FieldType.wifeFirstName));
        assertEquals("WifeLastName",            "",misc2.getFieldValue(FieldType.wifeLastName));
        assertEquals("WifeSex",                 "",misc2.getFieldValue(FieldType.wifeSex));
        assertEquals("WifeAge",                 "",misc2.getFieldValue(FieldType.wifeAge));
        assertEquals("WifeBirthDate",           "",misc2.getFieldValue(FieldType.wifeBirthDate));
        assertEquals("WifeBirthPlace",          "",misc2.getFieldValue(FieldType.wifeBirthPlace));
        assertEquals("WifeOccupation",          "",misc2.getFieldValue(FieldType.wifeOccupation));
        assertEquals("WifeResidence",           "",misc2.getFieldValue(FieldType.wifeResidence));
        assertEquals("WifeComment",             "",misc2.getFieldValue(FieldType.wifeComment));
        assertEquals("WifeMarriedFirstName",    "",misc2.getFieldValue(FieldType.wifeMarriedFirstName));
        assertEquals("WifeMarriedLastName",     "",misc2.getFieldValue(FieldType.wifeMarriedLastName));
        assertEquals("WifeMarriedOccupation",   "",misc2.getFieldValue(FieldType.wifeMarriedOccupation));
        assertEquals("WifeMarriedComment",      "",misc2.getFieldValue(FieldType.wifeMarriedComment));
        assertEquals("WifeMarriedDead",         "",misc2.getFieldValue(FieldType.wifeMarriedDead));
        assertEquals("WifeFatherFirstName",     "",misc2.getFieldValue(FieldType.wifeFatherFirstName));
        assertEquals("WifeFatherLastName",      "",misc2.getFieldValue(FieldType.wifeFatherLastName));
        assertEquals("WifeFatherAge",           "",misc2.getFieldValue(FieldType.wifeFatherAge));
        assertEquals("WifeFatherOccupation",    "",misc2.getFieldValue(FieldType.wifeFatherOccupation));
        assertEquals("WifeFatherComment",       "",misc2.getFieldValue(FieldType.wifeFatherComment));
        assertEquals("WifeFatherDead",          "",misc2.getFieldValue(FieldType.wifeFatherDead));
        assertEquals("WifeMotherFirstName",     "",misc2.getFieldValue(FieldType.wifeMotherFirstName));
        assertEquals("WifeMotherLastName",      "",misc2.getFieldValue(FieldType.wifeMotherLastName));
        assertEquals("WifeMotherAge",           "",misc2.getFieldValue(FieldType.wifeMotherAge));
        assertEquals("WifeMotherDead",          "",misc2.getFieldValue(FieldType.wifeMotherDead));
        assertEquals("WifeMotherOccupation",    "",misc2.getFieldValue(FieldType.wifeMotherOccupation));
        assertEquals("WifeMotherComment",       "",misc2.getFieldValue(FieldType.wifeMotherComment));

        assertWitnesses(misc, misc2);
        assertEquals("generalcomment, Autre intervenant: wifeFirstname wifeLastname, né le 02/02/1992 wifeBirthPlace, wifeComment, wifeOccupation, wifeResidence, Père de l'intervenant: wifeFatherFirstname wifeFatherLastname Age:71a Vivant wifeFatherOccupation wifeFatherResidence wifeFatherComment, Mère de l'intervenant: wifeMotherFirstname wifeMotherLastname Age:73a Vivant wifeMotherOccupation wifeMotherResidence wifeMotherComment, Conjoint de l'intervenant: wifeMarriedFirstname wifeMarriedLastname Décédé wifeMarriedOccupation wifeMarriedResidence wifeMotherComment, témoin(s): w3firstname w3lastname w3occupation w3comment, w4firstname w4lastname w4occupation w4comment, insinué le 04/04/2012",misc2.getFieldValue(FieldType.generalComment));

        file.delete();

    }


    @Test
    public void testFormatAgeToField() {

         assertEquals("formatAgeToField 76", "76y", ReleveFileEgmt.formatAgeToField("76"));
         assertEquals("formatAgeToField 76", "76y", ReleveFileEgmt.formatAgeToField("76 ans"));
         assertEquals("formatAgeToField 76", "8d", ReleveFileEgmt.formatAgeToField("8 jours"));

     }

}
