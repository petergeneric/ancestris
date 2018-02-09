package ancestris.modules.releve.file;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Field.FieldType;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import ancestris.modules.releve.model.RecordMisc;
import java.io.File;
import static junit.framework.Assert.assertEquals;
import junit.framework.TestCase;
import org.junit.Test;
import org.openide.util.Exceptions;

/**
 *
 * @author Michel
 */
public class ReleveFileEgmtTest extends TestCase {
    
     /**
     * Test of saveFile method, of class ReleveFileEgmt.
     */
    @Test
    public void testSaveFileBirth() {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("", "", "", "", "" );

        RecordBirth birth = TestUtility.getRecordBirth();
        dataManager.addRecord(birth);
        StringBuilder sb = ReleveFileEgmt.saveFile(dataManager, dataManager.getDataModel(), DataManager.RecordType.birth, file, false);
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
        RecordBirth birth2 = (RecordBirth) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            switch (fieldType) {
                case indiFatherLastName:
                    assertEquals(fieldType.name(),birth.getIndiLastName().toString(),birth2.getField(fieldType).toString());
                    break;
                case wifeFatherLastName:
                    assertNull(fieldType.name(),birth2.getField(fieldType));
                    break;
                case indiFatherOccupation:
                case indiMotherOccupation:
                    assertEquals(fieldType.name(), "",birth2.getField(fieldType).toString());
                    break;
                case indiFatherComment:
                    assertEquals(fieldType.name(), "indiFatherComment, indiFatherOccupation, indiFatherResidence, Age:70a",birth2.getField(fieldType).toString());
                    break;
                case indiMotherComment:
                    assertEquals(fieldType.name(), "indiMotherComment, indiMotherOccupation, indiMotherResidence, Age:72a",birth2.getField(fieldType).toString());
                    break;
                case witness1Occupation:
                case witness2Occupation:
                    assertEquals(fieldType.name(), "",birth2.getField(fieldType).toString());
                    break;
                case witness1Comment:
                    assertEquals(fieldType.name(), "w1comment, w1occupation",birth2.getField(fieldType).toString());
                    break;
                case witness2Comment:
                    assertEquals(fieldType.name(), "w2comment, w2occupation",birth2.getField(fieldType).toString());
                    break;
                case generalComment:
                    //assertEquals(fieldType.name(), "generalcomment ",birth2.getField(fieldType).toString());
                    assertEquals(fieldType.name(), "general comment, témoin(s): w3firstname w3lastname w3occupation w3comment, w4firstname w4lastname w4occupation w4comment ",birth2.getField(fieldType).toString());
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
                    assertEquals(fieldType.name(), "",birth2.getField(fieldType).toString());
                    break;
                default:
                    // autres champs
                    if (birth.getField(fieldType) == null) {
                        assertNull(fieldType.name(), birth2.getField(fieldType));
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
                            assertNotNull(fieldType.name(), birth2.getField(fieldType));
                            assertEquals(fieldType.name(), "", birth2.getField(fieldType).toString());
                        } else {
                            assertNotNull(fieldType.name(), birth2.getField(fieldType));
                            assertEquals(fieldType.name(), birth.getField(fieldType).toString(), birth2.getField(fieldType).toString());
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
        StringBuilder sb = ReleveFileEgmt.saveFile(dataManager, dataManager.getDataModel(), DataManager.RecordType.marriage, file, false);
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
        assertEquals("EventDate",   marriage.getEventDateProperty().toString(),marriage2.getEventDateProperty().toString());
        assertEquals("Cote",        marriage.getCote().toString(),marriage2.getCote().toString());
        assertEquals("parish",      marriage.getParish().toString(),marriage2.getParish().toString());
        assertEquals("EventDate",   marriage.getEventDateProperty().toString(),marriage2.getEventDateProperty().toString());
        assertNull("Notary",        marriage2.getNotary());
        assertNull("EventType",     marriage2.getEventType());
        assertEquals("FreeComment",    marriage.getFreeComment().toString(),marriage2.getFreeComment().toString());

        assertEquals("IndiFirstName",            marriage.getIndiFirstName().toString(),marriage2.getIndiFirstName().toString());
        assertEquals("IndiLastName",            marriage.getIndiLastName().toString(),marriage2.getIndiLastName().toString());
        assertEquals("IndiSex",                 marriage.getIndiSex().toString(),marriage2.getIndiSex().toString());
        assertEquals("IndiAge",                 marriage.getIndiAge().getValue(),marriage2.getIndiAge().getValue());
        assertEquals("IndiBirthDate",           "",marriage2.getIndiBirthDate().getValue());
        assertEquals("IndiBirthPlace",          "",marriage2.getIndiBirthPlace().toString());
        assertEquals("IndiOccupation",          "",marriage2.getIndiOccupation().toString());
        assertEquals("IndiComment",            "indiComment, né le 01/02/1990, indiBirthPlace, indiOccupation, Ex conjoint: indiMarriedFirstname indiMarriedLastname Décédé indiMarriedOccupation indiMarriedResidence indiMarriedComment",marriage2.getIndiComment().toString());                                                 
        assertEquals("IndiMarriedFirstName",    "",marriage2.getIndiMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     "",marriage2.getIndiMarriedLastName().toString());
        assertEquals("IndiMarriedOccupation",   "",marriage2.getIndiMarriedOccupation().toString());
        assertEquals("IndiMarriedComment",      "".toString(),marriage2.getIndiMarriedComment().toString());
        assertEquals("IndiMarriedDead",         "",marriage2.getIndiMarriedDead().toString());
        assertEquals("IndiFatherFirstName",     marriage.getIndiFatherFirstName().toString(),marriage2.getIndiFatherFirstName().toString());
        assertEquals("IndiFatherLastName",      marriage.getIndiLastName().toString(),marriage2.getIndiFatherLastName().toString());
        assertEquals("IndiFatherAge",           "",marriage2.getIndiFatherAge().toString());
        assertEquals("IndiFatherDead",          marriage.getIndiFatherDead().toString(),marriage2.getIndiFatherDead().toString());
        assertEquals("IndiFatherOccupation",    "",marriage2.getIndiFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "indiFatherComment, indiFatherOccupation, indiFatherResidence, Age:70a",marriage2.getIndiFatherComment().toString());
        assertEquals("IndiMotherFirstName",     marriage.getIndiMotherFirstName().toString(),marriage2.getIndiMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      marriage.getIndiMotherLastName().toString(),marriage2.getIndiMotherLastName().toString());
        assertEquals("IndiMotherAge",           "",marriage2.getIndiMotherAge().toString());
        assertEquals("IndiMotherDead",          marriage.getIndiMotherDead().toString(),marriage2.getIndiMotherDead().toString());
        assertEquals("IndiMotherOccupation",    "",marriage2.getIndiMotherOccupation().toString());
        assertEquals("IndiMotherComment",      "indiMotherComment, indiMotherOccupation, indiMotherResidence, Age:72a",marriage2.getIndiMotherComment().toString());

        assertEquals("WifeFirstName",           marriage.getWifeFirstName().toString(),marriage2.getWifeFirstName().toString());
        assertEquals("WifeLastName",            marriage.getWifeLastName().toString(),marriage2.getWifeLastName().toString());
        assertEquals("WifeSex",                 marriage.getWifeSex().toString(),marriage2.getWifeSex().toString());
        assertEquals("WifeAge",                 marriage.getWifeAge().toString(),marriage2.getWifeAge().toString());
        assertEquals("WifeBirthDate",           "",marriage2.getWifeBirthDate().toString());
        assertEquals("WifeBirthPlace",          "",marriage2.getWifeBirthPlace().toString());
        assertEquals("WifeOccupation",          "",marriage2.getWifeOccupation().toString());
        assertEquals("WifeResidence",           marriage.getWifeResidence().toString(),marriage2.getWifeResidence().toString());
        assertEquals("WifeComment",             "wifeComment, né le 02/02/1992, wifeBirthPlace, wifeOccupation, Ex conjoint: wifeMarriedFirstname wifeMarriedLastname wifeMarriedOccupation wifeMarriedResidence wifeMarriedComment",marriage2.getWifeComment().toString());
        assertEquals("WifeMarriedFirstName",    "",marriage2.getWifeMarriedFirstName().toString());
        assertEquals("WifeMarriedLastName",     "",marriage2.getWifeMarriedLastName().toString());
        assertEquals("WifeMarriedOccupation",   "",marriage2.getWifeMarriedOccupation().toString());
        assertEquals("WifeMarriedComment",      "",marriage2.getWifeMarriedComment().toString());
        assertEquals("WifeMarriedDead",         marriage.getWifeMarriedDead().toString(),marriage2.getWifeMarriedDead().toString());
        assertEquals("WifeFatherFirstName",     marriage.getWifeFatherFirstName().toString(),marriage2.getWifeFatherFirstName().toString());
        assertEquals("WifeFatherLastName",      marriage.getWifeLastName().toString(),marriage2.getWifeFatherLastName().toString());
        assertEquals("WifeFatherOccupation",    "",marriage2.getWifeFatherOccupation().toString());
        assertEquals("WifeFatherAge",           "",marriage2.getWifeFatherAge().toString());
        assertEquals("WifeFatherDead",          marriage.getWifeFatherDead().toString(),marriage2.getWifeFatherDead().toString());
        assertEquals("WifeFatherComment",       "wifeFatherComment, wifeFatherOccupation, wifeFatherResidence, Age:71a",marriage2.getWifeFatherComment().toString());
        assertEquals("WifeMotherFirstName",     marriage.getWifeMotherFirstName().toString(),marriage2.getWifeMotherFirstName().toString());
        assertEquals("WifeMotherLastName",      marriage.getWifeMotherLastName().toString(),marriage2.getWifeMotherLastName().toString());
        assertEquals("WifeMotherAge",           "",marriage2.getWifeMotherAge().toString());
        assertEquals("WifeMotherDead",          marriage.getWifeMotherDead().toString(),marriage2.getWifeMotherDead().toString());
        assertEquals("WifeMotherOccupation",    "",marriage2.getWifeMotherOccupation().toString());
        assertEquals("WifeMotherComment",       "wifeMotherComment, wifeMotherOccupation, wifeMotherResidence, Age:73a",marriage2.getWifeMotherComment().toString());
        
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
//        assertEquals("GeneralComment", "generalcomment, témoin(s): w3firstname w3lastname w3occupation w3comment, w4firstname w4lastname w4occupation w4comment ",marriage2.getGeneralComment().toString());
        
        file.delete();

    }
    
    private void assertWitnesses(Record record, Record record2) {
         for(int i=0 ; i < 2; i++ ) {
            assertEquals("Witness "+ (i+1)+ " FirstName" , record.getWitnesses()[i].getFirstName().toString(),record2.getWitnesses()[i].getFirstName().toString());
            assertEquals("Witness "+ (i+1)+ " LastName",   record.getWitnesses()[i].getLastName().toString(),record2.getWitnesses()[i].getLastName().toString());
            assertEquals("Witness "+ (i+1)+ " Occupation", "",  record2.getWitnesses()[i].getOccupation().toString());
            assertEquals("Witness "+ (i+1)+ " Comment",    record.getWitnesses()[i].getComment().toString()+ ", " + record.getWitnesses()[i].getOccupation().toString(),
                                                           record2.getWitnesses()[i].getComment().toString());
        }
        for(int i=2 ; i < 4; i++ ) {
            assertEquals("Witness "+ (i+1)+ " FirstName" , "", record2.getWitnesses()[i].getFirstName().toString());
            assertEquals("Witness "+ (i+1)+ " LastName",   "", record2.getWitnesses()[i].getLastName().toString());
            assertEquals("Witness "+ (i+1)+ " Occupation", "", record2.getWitnesses()[i].getOccupation().toString());
            assertEquals("Witness "+ (i+1)+ " Comment",    "", record2.getWitnesses()[i].getComment().toString());
        }
        
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
        StringBuilder sb = ReleveFileEgmt.saveFile(dataManager, dataManager.getDataModel(), DataManager.RecordType.death, file, false);
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

        assertEquals("EventDate",       death.getEventDateProperty().toString(),death2.getEventDateProperty().toString());
        assertEquals("Cote",            death.getCote().toString(),death2.getCote().toString());
        assertEquals("parish",          death.getParish().toString(),death2.getParish().toString());
        assertEquals("EventDate",       death.getEventDateProperty().toString(),death2.getEventDateProperty().toString());
        assertNull("Notary",            death2.getNotary());
        assertNull("EventType",         death2.getEventType());
        assertEquals("FreeComment",    death.getFreeComment().toString(),death2.getFreeComment().toString());

        assertEquals("IndiFirstName",  death.getIndiFirstName().toString(),death2.getIndiFirstName().toString());
        assertEquals("IndiLastName",   death.getIndiLastName().toString(),death2.getIndiLastName().toString());
        assertEquals("IndiSex",        death.getIndiSex().toString(),death2.getIndiSex().toString());
        assertEquals("IndiAge",        death.getIndiAge().toString(),death2.getIndiAge().toString());
        assertEquals("IndiBirthDate",  "",death2.getIndiBirthDate().toString());
        assertEquals("IndiBirthPlace", "",death2.getIndiBirthPlace().toString());
        assertEquals("IndiPlace",      "",death2.getIndiBirthPlace().toString());
        assertEquals("IndiOccupation", "",death2.getIndiOccupation().toString());
        assertEquals("IndiComment",    "indiComment, né le 01/02/1990, indiBirthPlace, indiOccupation",death2.getIndiComment().toString());
        assertEquals("IndiMarriedFirstName",    death.getIndiMarriedFirstName().toString(),death2.getIndiMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     death.getIndiMarriedLastName().toString(),death2.getIndiMarriedLastName().toString());
        assertEquals("IndiMarriedOccupation",   "",death2.getIndiMarriedOccupation().toString());
        assertEquals("IndiMarriedComment",      death.getIndiMarriedComment().toString(),death2.getIndiMarriedComment().toString());
        assertEquals("IndiMarriedDead",         death.getIndiMarriedDead().toString(),death2.getIndiMarriedDead().toString());

        assertEquals("IndiFatherFirstName",     death.getIndiFatherFirstName().toString(),death2.getIndiFatherFirstName().toString());
        assertEquals("IndiFatherLastName",         death.getIndiLastName().toString(),death2.getIndiFatherLastName().toString());
        assertEquals("IndiFatherAge",           "",death2.getIndiFatherAge().toString());
        assertEquals("IndiFatherDead",          death.getIndiFatherDead().toString(),death2.getIndiFatherDead().toString());
        assertEquals("IndiFatherOccupation",    "",death2.getIndiFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "indiFatherComment, indiFatherOccupation, indiFatherResidence, Age:70a",death2.getIndiFatherComment().toString());
        assertEquals("IndiMotherFirstName",     death.getIndiMotherFirstName().toString(),death2.getIndiMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      death.getIndiMotherLastName().toString(),death2.getIndiMotherLastName().toString());
        assertEquals("IndiMotherAge",           "",death2.getIndiMotherAge().toString());
        assertEquals("IndiMotherDead",          death.getIndiMotherDead().toString(),death2.getIndiMotherDead().toString());
        assertEquals("IndiMotherOccupation",    "",death2.getIndiMotherOccupation().toString());
        assertEquals("IndiMotherComment",       "indiMotherComment, indiMotherOccupation, indiMotherResidence, Age:72a",death2.getIndiMotherComment().toString());

        assertEquals("WifeFirstName",           null,death2.getWifeFirstName());
        assertEquals("WifeLastName",            null,death2.getWifeLastName());
        assertEquals("WifeSex",                 null,death2.getWifeSex());
        assertEquals("WifeAge",                 null,death2.getWifeAge());
        assertEquals("WifeBirthDate",           null,death2.getWifeBirthDate());
        assertEquals("WifePlace",               null,death2.getWifeBirthPlace());
        assertEquals("WifeOccupation",          null,death2.getWifeOccupation());
        assertEquals("WifeResidence",           null,death2.getWifeResidence());
        assertEquals("WifeComment",             null,death2.getWifeComment());
        assertEquals("WifeMarriedFirstName",    null,death2.getWifeMarriedFirstName());
        assertEquals("WifeMarriedLastName",     null,death2.getWifeMarriedLastName());
        assertEquals("WifeMarriedOccupation",   null,death2.getWifeMarriedOccupation());
        assertEquals("WifeMarriedComment",      null,death2.getWifeMarriedComment());
        assertEquals("WifeMarriedDead",         null,death2.getWifeMarriedDead());
        assertEquals("WifeFatherFirstName",     null,death2.getWifeFatherFirstName());
        assertEquals("WifeFatherLastName",      null,death2.getWifeFatherLastName());
        assertEquals("WifeFatherAge",           null,death2.getWifeFatherAge());
        assertEquals("WifeFatherDead",          null,death2.getWifeFatherDead());
        assertEquals("WifeFatherOccupation",    null,death2.getWifeFatherOccupation());
        assertEquals("WifeFatherComment",       null,death2.getWifeFatherComment());
        assertEquals("WifeMotherFirstName",     null,death2.getWifeMotherFirstName());
        assertEquals("WifeMotherLastName",      null,death2.getWifeMotherLastName());
        assertEquals("WifeMotherAge",           null,death2.getWifeMotherAge());
        assertEquals("WifeMotherDead",          null,death2.getWifeMotherDead());
        assertEquals("WifeMotherOccupation",    null,death2.getWifeMotherOccupation());
        assertEquals("WifeMotherComment",       null,death2.getWifeMotherComment());

        assertWitnesses(death, death2);
        assertEquals("GeneralComment", "generalcomment, témoin(s): w3firstname w3lastname w3occupation w3comment, w4firstname w4lastname w4occupation w4comment ",death2.getGeneralComment().toString());                                        
        
        file.delete();

    }

    /**
     * Test of saveFile method, of class ReleveFileEgmt.
     */
    @Test
    public void testSaveFileMarriageContract() {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("","","","","");

        RecordMisc misc = TestUtility.getRecordMisc();
        misc.setEventType("contrat de mariage");
        dataManager.addRecord(misc);
        StringBuilder sb = ReleveFileEgmt.saveFile(dataManager, dataManager.getDataModel(), DataManager.RecordType.misc, file, false);
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

        assertEquals("EventDate",   misc.getEventDateProperty().toString(),misc2.getEventDateProperty().toString());
        assertEquals("EventType",   "MARC",misc2.getEventType().toString());
        assertEquals("parish",      misc.getParish().toString(),misc2.getParish().toString());
        assertEquals("Notary",      misc.getNotary().toString(),misc2.getNotary().toString());
        assertEquals("Cote",        misc.getCote().toString(),misc2.getCote().toString());
        assertEquals("FreeComment", misc.getFreeComment().toString(),misc2.getFreeComment().toString());

        assertEquals("IndiFirstName",           misc.getIndiFirstName().toString(),misc2.getIndiFirstName().toString());
        assertEquals("IndiLastName",            misc.getIndiLastName().toString(),misc2.getIndiLastName().toString());
        assertEquals("IndiSex",                 misc.getIndiSex().toString(),misc2.getIndiSex().toString());
        assertEquals("IndiAge",                 misc.getIndiAge().toString(),misc2.getIndiAge().toString());
        assertEquals("IndiBirthDate",           "",misc2.getIndiBirthDate().getValue());
        assertEquals("IndiBirthPlace",          "",misc2.getIndiBirthPlace().toString());
        assertEquals("IndiPlace",               "",misc2.getIndiBirthPlace().toString());
        assertEquals("IndiOccupation",          "",misc2.getIndiOccupation().toString());
        assertEquals("IndiComment",             "indiComment, né le 01/02/1990, indiBirthPlace, indiOccupation, Ex conjoint: indiMarriedFirstname indiMarriedLastname Décédé indiMarriedOccupation indiMarriedResidence indiMarriedComment",misc2.getIndiComment().toString());
        assertEquals("IndiMarriedFirstName",    "",misc2.getIndiMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     "",misc2.getIndiMarriedLastName().toString());
        assertEquals("IndiMarriedOccupation",   "",misc2.getIndiMarriedOccupation().toString());
        assertEquals("IndiMarriedComment",      "",misc2.getIndiMarriedComment().toString());
        assertEquals("IndiMarriedDead",         "",misc2.getIndiMarriedDead().toString());
        assertEquals("IndiFatherFirstName",     misc.getIndiFatherFirstName().toString(),misc2.getIndiFatherFirstName().toString());
        assertEquals("IndiFatherLastName",      misc.getIndiLastName().toString(),misc2.getIndiFatherLastName().toString());
        assertEquals("IndiFatherAge",           "",misc2.getIndiFatherAge().toString());
        assertEquals("IndiFatherDead",          misc.getIndiFatherDead().toString(),misc2.getIndiFatherDead().toString());
        assertEquals("IndiFatherOccupation",    "",misc2.getIndiFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "indiFatherComment, indiFatherOccupation, indiFatherResidence, Age:70a",misc2.getIndiFatherComment().toString());
        assertEquals("IndiMotherFirstName",     misc.getIndiMotherFirstName().toString(),misc2.getIndiMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      misc.getIndiMotherLastName().toString(),misc2.getIndiMotherLastName().toString());
        assertEquals("IndiMotherAge",           "",misc2.getIndiMotherAge().toString());
        assertEquals("IndiMotherDead",          misc.getIndiMotherDead().toString(),misc2.getIndiMotherDead().toString());
        assertEquals("IndiMotherOccupation",    "",misc2.getIndiMotherOccupation().toString());
        assertEquals("IndiMotherComment",      "indiMotherComment, indiMotherOccupation, indiMotherResidence, Age:72a",misc2.getIndiMotherComment().toString());

        assertEquals("WifeFirstName",           misc.getWifeFirstName().toString(),misc2.getWifeFirstName().toString());
        assertEquals("WifeLastName",            misc.getWifeLastName().toString(),misc2.getWifeLastName().toString());
        assertEquals("WifeSex",                 misc.getWifeSex().toString(),misc2.getWifeSex().toString());
        assertEquals("WifeAge",                 misc.getWifeAge().toString(),misc2.getWifeAge().toString());
        assertEquals("WifeBirthDate",           "",misc2.getWifeBirthDate().toString());
        assertEquals("WifeBirthPlace",          "",misc2.getWifeBirthPlace().toString());
        assertEquals("WifeOccupation",          "",misc2.getWifeOccupation().toString());
        assertEquals("WifeResidence",           misc.getWifeResidence().toString(),misc2.getWifeResidence().toString());
        assertEquals("WifeComment",             "wifeComment, né le 02/02/1992, wifeBirthPlace, wifeOccupation, Ex conjoint: wifeMarriedFirstname wifeMarriedLastname Décédé wifeMarriedOccupation wifeMarriedResidence wifeMarriedComment",misc2.getWifeComment().toString());
        assertEquals("WifeMarriedFirstName",    "",misc2.getWifeMarriedFirstName().toString());
        assertEquals("WifeMarriedLastName",     "",misc2.getWifeMarriedLastName().toString());
        assertEquals("WifeMarriedOccupation",   "",misc2.getWifeMarriedOccupation().toString());
        assertEquals("WifeMarriedComment",      "",misc2.getWifeMarriedComment().toString());
        assertEquals("WifeMarriedDead",         "",misc2.getWifeMarriedDead().toString());
        assertEquals("WifeFatherFirstName",     misc.getWifeFatherFirstName().toString(),misc2.getWifeFatherFirstName().toString());
        assertEquals("WifeFatherLastName",      misc.getWifeLastName().toString(),misc2.getWifeFatherLastName().toString());
        assertEquals("WifeFatherAge",           "",misc2.getWifeFatherAge().toString());
        assertEquals("WifeFatherOccupation",    "",misc2.getWifeFatherOccupation().toString());
        assertEquals("WifeFatherComment",       "wifeFatherComment, wifeFatherOccupation, wifeFatherResidence, Age:71a",misc2.getWifeFatherComment().toString());
        assertEquals("WifeFatherDead",          misc.getWifeFatherDead().toString(),misc2.getWifeFatherDead().toString());
        assertEquals("WifeMotherFirstName",     misc.getWifeMotherFirstName().toString(),misc2.getWifeMotherFirstName().toString());
        assertEquals("WifeMotherLastName",      misc.getWifeMotherLastName().toString(),misc2.getWifeMotherLastName().toString());
        assertEquals("WifeMotherAge",           "",misc2.getWifeMotherAge().toString());
        assertEquals("WifeMotherDead",          misc.getWifeMotherDead().toString(),misc2.getWifeMotherDead().toString());
        assertEquals("WifeMotherOccupation",    "",misc2.getWifeMotherOccupation().toString());
        assertEquals("WifeMotherComment",       "wifeMotherComment, wifeMotherOccupation, wifeMotherResidence, Age:73a",misc2.getWifeMotherComment().toString());

        assertWitnesses(misc, misc2);
        assertEquals("GeneralComment", "generalcomment, témoin(s): w3firstname w3lastname w3occupation w3comment, w4firstname w4lastname w4occupation w4comment, insinué le 04/04/2012",misc2.getGeneralComment().toString());                                        

        
        file.delete();

    }

/**
     * Test of saveFile method, of class ReleveFileEgmt.
     */
    @Test
    public void testSaveFileTestament() {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("","","","","");

        RecordMisc misc = TestUtility.getRecordMisc();
        misc.setEventType("testament");
        dataManager.addRecord(misc);
        StringBuilder sb = ReleveFileEgmt.saveFile(dataManager, dataManager.getDataModel(), DataManager.RecordType.misc, file, false);
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

        assertEquals("EventDate",   misc.getEventDateProperty().toString(),misc2.getEventDateProperty().toString());
        assertEquals("EventType",   "WILL",misc2.getEventType().toString());
        assertEquals("parish",      misc.getParish().toString(),misc2.getParish().toString());
        assertEquals("Notary",      misc.getNotary().toString(),misc2.getNotary().toString());
        assertEquals("Cote",        misc.getCote().toString(),misc2.getCote().toString());
        assertEquals("FreeComment", misc.getFreeComment().toString(),misc2.getFreeComment().toString());

        assertEquals("IndiFirstName",           misc.getIndiFirstName().toString(),misc2.getIndiFirstName().toString());
        assertEquals("IndiLastName",            misc.getIndiLastName().toString(),misc2.getIndiLastName().toString());
        assertEquals("IndiSex",                 misc.getIndiSex().toString(),misc2.getIndiSex().toString());
        assertEquals("IndiAge",                 misc.getIndiAge().toString(),misc2.getIndiAge().toString());
        assertEquals("IndiBirthDate",           "",misc2.getIndiBirthDate().getValue());
        assertEquals("IndiBirthPlace",          "",misc2.getIndiBirthPlace().toString());
        assertEquals("IndiPlace",               "",misc2.getIndiBirthPlace().toString());
        assertEquals("IndiOccupation",          "",misc2.getIndiOccupation().toString());
        assertEquals("IndiComment",             "indiComment, né le 01/02/1990, indiBirthPlace, indiOccupation",misc2.getIndiComment().toString());

        assertEquals("IndiMarriedFirstName",    misc.getIndiMarriedFirstName().toString(),misc2.getIndiMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     misc.getIndiMarriedLastName().toString(),misc2.getIndiMarriedLastName().toString());
        //assertEquals("WifeSex",                 misc.getIndiMarriedSex().toString(),misc2.getWifeSex().toString());
        //assertEquals("WifeAge",                 misc.getWifeAge().toString(),misc2.getWifeAge().toString());
        //assertEquals("WifeBirthDate",           misc.getIndiMarriedDead().toString(),misc2.getWifeD().toString());
        assertEquals("IndiMarriedDead",         misc.getIndiMarriedDead().toString(),misc2.getIndiMarriedDead().toString());
        //assertEquals("WifeBirthPlace",          "",misc2.getWifeBirthPlace().toString());
        assertEquals("IndiMarriedOccupation",   "",misc2.getIndiMarriedOccupation().toString());
        assertEquals("IndiMarriedResidence",    misc.getIndiMarriedResidence().toString(),misc2.getIndiMarriedResidence().toString());
        assertEquals("IndiMarriedComment",      "indiMarriedOccupation, indiMarriedComment",misc2.getIndiMarriedComment().toString());

        assertEquals("IndiFatherFirstName",     misc.getIndiFatherFirstName().toString(),misc2.getIndiFatherFirstName().toString());
        assertEquals("IndiFatherLastName",      misc.getIndiLastName().toString(),misc2.getIndiFatherLastName().toString());
        assertEquals("IndiFatherAge",           "",misc2.getIndiFatherAge().toString());
        assertEquals("IndiFatherDead",          misc.getIndiFatherDead().toString(),misc2.getIndiFatherDead().toString());
        assertEquals("IndiFatherOccupation",    "",misc2.getIndiFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "indiFatherComment, indiFatherOccupation, indiFatherResidence, Age:70a",misc2.getIndiFatherComment().toString());
        assertEquals("IndiMotherFirstName",     misc.getIndiMotherFirstName().toString(),misc2.getIndiMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      misc.getIndiMotherLastName().toString(),misc2.getIndiMotherLastName().toString());
        assertEquals("IndiMotherAge",           "",misc2.getIndiMotherAge().toString());
        assertEquals("IndiMotherDead",          misc.getIndiMotherDead().toString(),misc2.getIndiMotherDead().toString());
        assertEquals("IndiMotherOccupation",    "",misc2.getIndiMotherOccupation().toString());
        assertEquals("IndiMotherComment",       "indiMotherComment, indiMotherOccupation, indiMotherResidence, Age:72a",misc2.getIndiMotherComment().toString());

        assertEquals("WifeFirstName",           "",misc2.getWifeFirstName().toString());
        assertEquals("WifeLastName",            "",misc2.getWifeLastName().toString());
        assertEquals("WifeSex",                 "",misc2.getWifeSex().toString());
        assertEquals("WifeAge",                 "",misc2.getWifeAge().toString());
        assertEquals("WifeBirthDate",           "",misc2.getWifeBirthDate().toString());
        assertEquals("WifeBirthPlace",          "",misc2.getWifeBirthPlace().toString());
        assertEquals("WifeOccupation",          "",misc2.getWifeOccupation().toString());
        assertEquals("WifeResidence",           "",misc2.getWifeResidence().toString());
        assertEquals("WifeComment",             "",misc2.getWifeComment().toString());
        assertEquals("WifeMarriedFirstName",    "",misc2.getWifeMarriedFirstName().toString());
        assertEquals("WifeMarriedLastName",     "",misc2.getWifeMarriedLastName().toString());
        assertEquals("WifeMarriedOccupation",   "",misc2.getWifeMarriedOccupation().toString());
        assertEquals("WifeMarriedComment",      "",misc2.getWifeMarriedComment().toString());
        assertEquals("WifeMarriedDead",         "",misc2.getWifeMarriedDead().toString());
        assertEquals("WifeFatherFirstName",     "",misc2.getWifeFatherFirstName().toString());
        assertEquals("WifeFatherLastName",      "",misc2.getWifeFatherLastName().toString());
        assertEquals("WifeFatherAge",           "",misc2.getWifeFatherAge().toString());
        assertEquals("WifeFatherOccupation",    "",misc2.getWifeFatherOccupation().toString());
        assertEquals("WifeFatherComment",       "",misc2.getWifeFatherComment().toString());
        assertEquals("WifeFatherDead",          "",misc2.getWifeFatherDead().toString());
        assertEquals("WifeMotherFirstName",     "",misc2.getWifeMotherFirstName().toString());
        assertEquals("WifeMotherLastName",      "",misc2.getWifeMotherLastName().toString());
        assertEquals("WifeMotherAge",           "",misc2.getWifeMotherAge().toString());
        assertEquals("WifeMotherDead",          "",misc2.getWifeMotherDead().toString());
        assertEquals("WifeMotherOccupation",    "",misc2.getWifeMotherOccupation().toString());
        assertEquals("WifeMotherComment",       "",misc2.getWifeMotherComment().toString());

        assertWitnesses(misc, misc2);
        assertEquals("GeneralComment", "generalcomment, témoin(s): w3firstname w3lastname w3occupation w3comment, w4firstname w4lastname w4occupation w4comment, insinué le 04/04/2012, Héritier: wifeFirstname wifeLastname, né le 02/02/1992 wifeBirthPlace, wifeComment, wifeOccupation, wifeResidence, Père de l'héritier: wifeFatherFirstname wifeFatherLastname Age:71a wifeFatherOccupation wifeFatherResidence wifeFatherComment, Mère de l'héritier: wifeMotherFirstname wifeMotherLastname Age:73a wifeMotherOccupation wifeMotherResidence wifeMotherComment, Conjoint de l'héritier: wifeMarriedFirstname wifeMarriedLastname Décédé wifeMarriedOccupation wifeMarriedResidence wifeMotherComment",misc2.getGeneralComment().toString());                                        

        file.delete();

    }

    /**
     * Test of saveFile method, of class ReleveFileEgmt.
     */
    @Test
    public void testSaveFileMisc() {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("","","","","");

        RecordMisc misc = TestUtility.getRecordMisc();
        dataManager.addRecord(misc);
        StringBuilder sb = ReleveFileEgmt.saveFile(dataManager, dataManager.getDataModel(), DataManager.RecordType.misc, file, false);
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

        assertEquals("EventDate",   misc.getEventDateProperty().toString(),misc2.getEventDateProperty().toString());
        assertEquals("EventType",   misc.getEventType().toString(),misc2.getEventType().toString());
        assertEquals("parish",      misc.getParish().toString(),misc2.getParish().toString());
        assertEquals("Notary",      misc.getNotary().toString(),misc2.getNotary().toString());
        assertEquals("Cote",        misc.getCote().toString(),misc2.getCote().toString());
        assertEquals("FreeComment", misc.getFreeComment().toString(),misc2.getFreeComment().toString());

        assertEquals("IndiFirstName",           misc.getIndiFirstName().toString(),misc2.getIndiFirstName().toString());
        assertEquals("IndiLastName",            misc.getIndiLastName().toString(),misc2.getIndiLastName().toString());
        assertEquals("IndiSex",                 misc.getIndiSex().toString(),misc2.getIndiSex().toString());
        assertEquals("IndiAge",                 misc.getIndiAge().toString(),misc2.getIndiAge().toString());
        assertEquals("IndiBirthDate",           "",misc2.getIndiBirthDate().getValue());
        assertEquals("IndiBirthPlace",          "",misc2.getIndiBirthPlace().toString());
        assertEquals("IndiPlace",               "",misc2.getIndiBirthPlace().toString());
        assertEquals("IndiOccupation",          "",misc2.getIndiOccupation().toString());
        assertEquals("IndiComment",             "indiComment, né le 01/02/1990, indiBirthPlace, indiOccupation",misc2.getIndiComment().toString());

        assertEquals("IndiMarriedFirstName",    misc.getIndiMarriedFirstName().toString(),misc2.getIndiMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     misc.getIndiMarriedLastName().toString(),misc2.getIndiMarriedLastName().toString());
        assertEquals("IndiMarriedDead",         misc.getIndiMarriedDead().toString(),misc2.getIndiMarriedDead().toString());
        assertEquals("IndiMarriedOccupation",   "",misc2.getIndiMarriedOccupation().toString());
        assertEquals("IndiMarriedResidence",    misc.getIndiMarriedResidence().toString(),misc2.getIndiMarriedResidence().toString());
        assertEquals("IndiMarriedComment",      "indiMarriedOccupation, indiMarriedComment",misc2.getIndiMarriedComment().toString());

        assertEquals("IndiFatherFirstName",     misc.getIndiFatherFirstName().toString(),misc2.getIndiFatherFirstName().toString());
        assertEquals("IndiFatherLastName",      misc.getIndiLastName().toString(),misc2.getIndiFatherLastName().toString());
        assertEquals("IndiFatherAge",           "",misc2.getIndiFatherAge().toString());
        assertEquals("IndiFatherDead",          misc.getIndiFatherDead().toString(),misc2.getIndiFatherDead().toString());
        assertEquals("IndiFatherOccupation",    "",misc2.getIndiFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "indiFatherComment, indiFatherOccupation, indiFatherResidence, Age:70a",misc2.getIndiFatherComment().toString());
        assertEquals("IndiMotherFirstName",     misc.getIndiMotherFirstName().toString(),misc2.getIndiMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      misc.getIndiMotherLastName().toString(),misc2.getIndiMotherLastName().toString());
        assertEquals("IndiMotherAge",           "",misc2.getIndiMotherAge().toString());
        assertEquals("IndiMotherDead",          misc.getIndiMotherDead().toString(),misc2.getIndiMotherDead().toString());
        assertEquals("IndiMotherOccupation",    "",misc2.getIndiMotherOccupation().toString());
        assertEquals("IndiMotherComment",      "indiMotherComment, indiMotherOccupation, indiMotherResidence, Age:72a",misc2.getIndiMotherComment().toString());

        assertEquals("WifeFirstName",           "",misc2.getWifeFirstName().toString());
        assertEquals("WifeLastName",            "",misc2.getWifeLastName().toString());
        assertEquals("WifeSex",                 "",misc2.getWifeSex().toString());
        assertEquals("WifeAge",                 "",misc2.getWifeAge().toString());
        assertEquals("WifeBirthDate",           "",misc2.getWifeBirthDate().toString());
        assertEquals("WifeBirthPlace",          "",misc2.getWifeBirthPlace().toString());
        assertEquals("WifeOccupation",          "",misc2.getWifeOccupation().toString());
        assertEquals("WifeResidence",           "",misc2.getWifeResidence().toString());
        assertEquals("WifeComment",             "",misc2.getWifeComment().toString());
        assertEquals("WifeMarriedFirstName",    "",misc2.getWifeMarriedFirstName().toString());
        assertEquals("WifeMarriedLastName",     "",misc2.getWifeMarriedLastName().toString());
        assertEquals("WifeMarriedOccupation",   "",misc2.getWifeMarriedOccupation().toString());
        assertEquals("WifeMarriedComment",      "",misc2.getWifeMarriedComment().toString());
        assertEquals("WifeMarriedDead",         "",misc2.getWifeMarriedDead().toString());
        assertEquals("WifeFatherFirstName",     "",misc2.getWifeFatherFirstName().toString());
        assertEquals("WifeFatherLastName",      "",misc2.getWifeFatherLastName().toString());
        assertEquals("WifeFatherAge",           "",misc2.getWifeFatherAge().toString());
        assertEquals("WifeFatherOccupation",    "",misc2.getWifeFatherOccupation().toString());
        assertEquals("WifeFatherComment",       "",misc2.getWifeFatherComment().toString());
        assertEquals("WifeFatherDead",          "",misc2.getWifeFatherDead().toString());
        assertEquals("WifeMotherFirstName",     "",misc2.getWifeMotherFirstName().toString());
        assertEquals("WifeMotherLastName",      "",misc2.getWifeMotherLastName().toString());
        assertEquals("WifeMotherAge",           "",misc2.getWifeMotherAge().toString());
        assertEquals("WifeMotherDead",          "",misc2.getWifeMotherDead().toString());
        assertEquals("WifeMotherOccupation",    "",misc2.getWifeMotherOccupation().toString());
        assertEquals("WifeMotherComment",       "",misc2.getWifeMotherComment().toString());

        assertWitnesses(misc, misc2);
        assertEquals("generalcomment, Autre intervenant: wifeFirstname wifeLastname, né le 02/02/1992 wifeBirthPlace, wifeComment, wifeOccupation, wifeResidence, Père de l'intervenant: wifeFatherFirstname wifeFatherLastname Age:71a wifeFatherOccupation wifeFatherResidence wifeFatherComment, Mère de l'intervenant: wifeMotherFirstname wifeMotherLastname Age:73a wifeMotherOccupation wifeMotherResidence wifeMotherComment, Conjoint de l'intervenant: wifeMarriedFirstname wifeMarriedLastname Décédé wifeMarriedOccupation wifeMarriedResidence wifeMotherComment, témoin(s): w3firstname w3lastname w3occupation w3comment, w4firstname w4lastname w4occupation w4comment, insinué le 04/04/2012",misc2.getGeneralComment().toString());

        file.delete();

    }


    @Test
    public void testFormatAgeToField() {

         assertEquals("formatAgeToField 76", "76y", ReleveFileEgmt.formatAgeToField("76"));
         assertEquals("formatAgeToField 76", "76y", ReleveFileEgmt.formatAgeToField("76 ans"));
         assertEquals("formatAgeToField 76", "8d", ReleveFileEgmt.formatAgeToField("8 jours"));

     }

}
