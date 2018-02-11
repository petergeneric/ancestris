package ancestris.modules.releve.file;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Field.FieldType;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.Record.RecordType;
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
        RecordBirth birth2 = (RecordBirth) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            switch (fieldType) {
                case indiFatherLastName:
                    assertEquals(fieldType.name(),birth.getIndi().getLastName().toString(),birth2.getField(fieldType).toString());
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
        assertEquals("EventDate",   marriage.getEventDateProperty().toString(),marriage2.getEventDateProperty().toString());
        assertEquals("Cote",        marriage.getCote().toString(),marriage2.getCote().toString());
        assertEquals("parish",      marriage.getParish().toString(),marriage2.getParish().toString());
        assertEquals("EventDate",   marriage.getEventDateProperty().toString(),marriage2.getEventDateProperty().toString());
        assertNull("Notary",        marriage2.getNotary());
        assertNull("EventType",     marriage2.getEventType());
        assertEquals("FreeComment",    marriage.getFreeComment().toString(),marriage2.getFreeComment().toString());

        assertEquals("IndiFirstName",            marriage.getIndi().getFirstName().toString(),marriage2.getIndi().getFirstName().toString());
        assertEquals("IndiLastName",            marriage.getIndi().getLastName().toString(),marriage2.getIndi().getLastName().toString());
        assertEquals("IndiSex",                 marriage.getIndi().getSex().toString(),marriage2.getIndi().getSex().toString());
        assertEquals("IndiAge",                 marriage.getIndi().getAge().getValue(),marriage2.getIndi().getAge().getValue());
        assertEquals("IndiBirthDate",           "",marriage2.getIndi().getBirthDate().getValue());
        assertEquals("IndiBirthPlace",          "",marriage2.getIndi().getBirthPlace().toString());
        assertEquals("IndiOccupation",          "",marriage2.getIndi().getOccupation().toString());
        assertEquals("IndiComment",            "indiComment, né le 01/02/1990, indiBirthPlace, indiOccupation, Ex conjoint: indiMarriedFirstname indiMarriedLastname Décédé indiMarriedOccupation indiMarriedResidence indiMarriedComment",marriage2.getIndi().getComment() .toString());                                                 
        assertEquals("IndiMarriedFirstName",    "",marriage2.getIndi().getMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     "",marriage2.getIndi().getMarriedLastName().toString());
        assertEquals("IndiMarriedOccupation",   "",marriage2.getIndi().getMarriedOccupation().toString());
        assertEquals("IndiMarriedComment",      "".toString(),marriage2.getIndi().getMarriedComment().toString());
        assertEquals("IndiMarriedDead",         "",marriage2.getIndi().getMarriedDead().toString());
        assertEquals("IndiFatherFirstName",     marriage.getIndi().getFatherFirstName().toString(),marriage2.getIndi().getFatherFirstName().toString());
        assertEquals("IndiFatherLastName",      marriage.getIndi().getLastName().toString(),marriage2.getIndi().getFatherLastName().toString());
        assertEquals("IndiFatherAge",           "",marriage2.getIndi().getFatherAge().toString());
        assertEquals("IndiFatherDead",          marriage.getIndi().getFatherDead().toString(),marriage2.getIndi().getFatherDead().toString());
        assertEquals("IndiFatherOccupation",    "",marriage2.getIndi().getFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "indiFatherComment, indiFatherOccupation, indiFatherResidence, Age:70a",marriage2.getIndi().getFatherComment().toString());
        assertEquals("IndiMotherFirstName",     marriage.getIndi().getMotherFirstName().toString(),marriage2.getIndi().getMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      marriage.getIndi().getMotherLastName().toString(),marriage2.getIndi().getMotherLastName().toString());
        assertEquals("IndiMotherAge",           "",marriage2.getIndi().getMotherAge().toString());
        assertEquals("IndiMotherDead",          marriage.getIndi().getMotherDead().toString(),marriage2.getIndi().getMotherDead().toString());
        assertEquals("IndiMotherOccupation",    "",marriage2.getIndi().getMotherOccupation().toString());
        assertEquals("IndiMotherComment",      "indiMotherComment, indiMotherOccupation, indiMotherResidence, Age:72a",marriage2.getIndi().getMotherComment().toString());

        assertEquals("WifeFirstName",           marriage.getWife().getFirstName().toString(),marriage2.getWife().getFirstName().toString());
        assertEquals("WifeLastName",            marriage.getWife().getLastName().toString(),marriage2.getWife().getLastName().toString());
        assertEquals("WifeSex",                 marriage.getWife().getSex().toString(),marriage2.getWife().getSex().toString());
        assertEquals("WifeAge",                 marriage.getWife().getAge().toString(),marriage2.getWife().getAge().toString());
        assertEquals("WifeBirthDate",           "",marriage2.getWife().getBirthDate().toString());
        assertEquals("WifeBirthPlace",          "",marriage2.getWife().getBirthPlace().toString());
        assertEquals("WifeOccupation",          "",marriage2.getWife().getOccupation().toString());
        assertEquals("WifeResidence",           marriage.getWife().getResidence().toString(),marriage2.getWife().getResidence().toString());
        assertEquals("WifeComment",             "wifeComment, né le 02/02/1992, wifeBirthPlace, wifeOccupation, Ex conjoint: wifeMarriedFirstname wifeMarriedLastname Vivant wifeMarriedOccupation wifeMarriedResidence wifeMarriedComment",marriage2.getWife().getComment().toString());
        assertEquals("WifeMarriedFirstName",    "",marriage2.getWife().getMarriedFirstName().toString());
        assertEquals("WifeMarriedLastName",     "",marriage2.getWife().getMarriedLastName().toString());
        assertEquals("WifeMarriedOccupation",   "",marriage2.getWife().getMarriedOccupation().toString());
        assertEquals("WifeMarriedComment",      "",marriage2.getWife().getMarriedComment().toString());
        assertEquals("WifeMarriedDead",         "",marriage2.getWife().getMarriedDead().toString());
        assertEquals("WifeFatherFirstName",     marriage.getWife().getFatherFirstName().toString(),marriage2.getWife().getFatherFirstName().toString());
        assertEquals("WifeFatherLastName",      marriage.getWife().getLastName().toString(),marriage2.getWife().getFatherLastName().toString());
        assertEquals("WifeFatherOccupation",    "",marriage2.getWife().getFatherOccupation().toString());
        assertEquals("WifeFatherAge",           "",marriage2.getWife().getFatherAge().toString());
        assertEquals("WifeFatherDead",          marriage.getWife().getFatherDead().toString(),marriage2.getWife().getFatherDead().toString());
        assertEquals("WifeFatherComment",       "wifeFatherComment, wifeFatherOccupation, wifeFatherResidence, Age:71a",marriage2.getWife().getFatherComment().toString());
        assertEquals("WifeMotherFirstName",     marriage.getWife().getMotherFirstName().toString(),marriage2.getWife().getMotherFirstName().toString());
        assertEquals("WifeMotherLastName",      marriage.getWife().getMotherLastName().toString(),marriage2.getWife().getMotherLastName().toString());
        assertEquals("WifeMotherAge",           "",marriage2.getWife().getMotherAge().toString());
        assertEquals("WifeMotherDead",          marriage.getWife().getMotherDead().toString(),marriage2.getWife().getMotherDead().toString());
        assertEquals("WifeMotherOccupation",    "",marriage2.getWife().getMotherOccupation().toString());
        assertEquals("WifeMotherComment",       "wifeMotherComment, wifeMotherOccupation, wifeMotherResidence, Age:73a",marriage2.getWife().getMotherComment().toString());
        
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

        assertEquals("EventDate",       death.getEventDateProperty().toString(),death2.getEventDateProperty().toString());
        assertEquals("Cote",            death.getCote().toString(),death2.getCote().toString());
        assertEquals("parish",          death.getParish().toString(),death2.getParish().toString());
        assertEquals("EventDate",       death.getEventDateProperty().toString(),death2.getEventDateProperty().toString());
        assertNull("Notary",            death2.getNotary());
        assertNull("EventType",         death2.getEventType());
        assertEquals("FreeComment",    death.getFreeComment().toString(),death2.getFreeComment().toString());

        assertEquals("IndiFirstName",  death.getIndi().getFirstName().toString(),death2.getIndi().getFirstName().toString());
        assertEquals("IndiLastName",   death.getIndi().getLastName().toString(),death2.getIndi().getLastName().toString());
        assertEquals("IndiSex",        death.getIndi().getSex().toString(),death2.getIndi().getSex().toString());
        assertEquals("IndiAge",        death.getIndi().getAge().toString(),death2.getIndi().getAge().toString());
        assertEquals("IndiBirthDate",  "",death2.getIndi().getBirthDate().toString());
        assertEquals("IndiBirthPlace", "",death2.getIndi().getBirthPlace().toString());
        assertEquals("IndiPlace",      "",death2.getIndi().getBirthPlace().toString());
        assertEquals("IndiOccupation", "",death2.getIndi().getOccupation().toString());
        assertEquals("IndiComment",    "indiComment, né le 01/02/1990, indiBirthPlace, indiOccupation",death2.getIndi().getComment() .toString());
        assertEquals("IndiMarriedFirstName",    death.getIndi().getMarriedFirstName().toString(),death2.getIndi().getMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     death.getIndi().getMarriedLastName().toString(),death2.getIndi().getMarriedLastName().toString());
        assertEquals("IndiMarriedOccupation",   "",death2.getIndi().getMarriedOccupation().toString());
        assertEquals("IndiMarriedComment",      death.getIndi().getMarriedComment().toString(),death2.getIndi().getMarriedComment().toString());
        assertEquals("IndiMarriedDead",         death.getIndi().getMarriedDead().toString(),death2.getIndi().getMarriedDead().toString());

        assertEquals("IndiFatherFirstName",     death.getIndi().getFatherFirstName().toString(),death2.getIndi().getFatherFirstName().toString());
        assertEquals("IndiFatherLastName",         death.getIndi().getLastName().toString(),death2.getIndi().getFatherLastName().toString());
        assertEquals("IndiFatherAge",           "",death2.getIndi().getFatherAge().toString());
        assertEquals("IndiFatherDead",          death.getIndi().getFatherDead().toString(),death2.getIndi().getFatherDead().toString());
        assertEquals("IndiFatherOccupation",    "",death2.getIndi().getFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "indiFatherComment, indiFatherOccupation, indiFatherResidence, Age:70a",death2.getIndi().getFatherComment().toString());
        assertEquals("IndiMotherFirstName",     death.getIndi().getMotherFirstName().toString(),death2.getIndi().getMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      death.getIndi().getMotherLastName().toString(),death2.getIndi().getMotherLastName().toString());
        assertEquals("IndiMotherAge",           "",death2.getIndi().getMotherAge().toString());
        assertEquals("IndiMotherDead",          death.getIndi().getMotherDead().toString(),death2.getIndi().getMotherDead().toString());
        assertEquals("IndiMotherOccupation",    "",death2.getIndi().getMotherOccupation().toString());
        assertEquals("IndiMotherComment",       "indiMotherComment, indiMotherOccupation, indiMotherResidence, Age:72a",death2.getIndi().getMotherComment().toString());

        assertEquals("WifeFirstName",           null,death2.getWife().getFirstName());
        assertEquals("WifeLastName",            null,death2.getWife().getLastName());
        assertEquals("WifeSex",                 null,death2.getWife().getSex());
        assertEquals("WifeAge",                 null,death2.getWife().getAge());
        assertEquals("WifeBirthDate",           null,death2.getWife().getBirthDate());
        assertEquals("WifePlace",               null,death2.getWife().getBirthPlace());
        assertEquals("WifeOccupation",          null,death2.getWife().getOccupation());
        assertEquals("WifeResidence",           null,death2.getWife().getResidence());
        assertEquals("WifeComment",             null,death2.getWife().getComment());
        assertEquals("WifeMarriedFirstName",    null,death2.getWife().getMarriedFirstName());
        assertEquals("WifeMarriedLastName",     null,death2.getWife().getMarriedLastName());
        assertEquals("WifeMarriedOccupation",   null,death2.getWife().getMarriedOccupation());
        assertEquals("WifeMarriedComment",      null,death2.getWife().getMarriedComment());
        assertEquals("WifeMarriedDead",         null,death2.getWife().getMarriedDead());
        assertEquals("WifeFatherFirstName",     null,death2.getWife().getFatherFirstName());
        assertEquals("WifeFatherLastName",      null,death2.getWife().getFatherLastName());
        assertEquals("WifeFatherAge",           null,death2.getWife().getFatherAge());
        assertEquals("WifeFatherDead",          null,death2.getWife().getFatherDead());
        assertEquals("WifeFatherOccupation",    null,death2.getWife().getFatherOccupation());
        assertEquals("WifeFatherComment",       null,death2.getWife().getFatherComment());
        assertEquals("WifeMotherFirstName",     null,death2.getWife().getMotherFirstName());
        assertEquals("WifeMotherLastName",      null,death2.getWife().getMotherLastName());
        assertEquals("WifeMotherAge",           null,death2.getWife().getMotherAge());
        assertEquals("WifeMotherDead",          null,death2.getWife().getMotherDead());
        assertEquals("WifeMotherOccupation",    null,death2.getWife().getMotherOccupation());
        assertEquals("WifeMotherComment",       null,death2.getWife().getMotherComment());

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

        assertEquals("EventDate",   misc.getEventDateProperty().toString(),misc2.getEventDateProperty().toString());
        assertEquals("EventType",   "MARC",misc2.getEventType().toString());
        assertEquals("parish",      misc.getParish().toString(),misc2.getParish().toString());
        assertEquals("Notary",      misc.getNotary().toString(),misc2.getNotary().toString());
        assertEquals("Cote",        misc.getCote().toString(),misc2.getCote().toString());
        assertEquals("FreeComment", misc.getFreeComment().toString(),misc2.getFreeComment().toString());

        assertEquals("IndiFirstName",           misc.getIndi().getFirstName().toString(),misc2.getIndi().getFirstName().toString());
        assertEquals("IndiLastName",            misc.getIndi().getLastName().toString(),misc2.getIndi().getLastName().toString());
        assertEquals("IndiSex",                 misc.getIndi().getSex().toString(),misc2.getIndi().getSex().toString());
        assertEquals("IndiAge",                 misc.getIndi().getAge().toString(),misc2.getIndi().getAge().toString());
        assertEquals("IndiBirthDate",           "",misc2.getIndi().getBirthDate().getValue());
        assertEquals("IndiBirthPlace",          "",misc2.getIndi().getBirthPlace().toString());
        assertEquals("IndiPlace",               "",misc2.getIndi().getBirthPlace().toString());
        assertEquals("IndiOccupation",          "",misc2.getIndi().getOccupation().toString());
        assertEquals("IndiComment",             "indiComment, né le 01/02/1990, indiBirthPlace, indiOccupation, Ex conjoint: indiMarriedFirstname indiMarriedLastname Décédé indiMarriedOccupation indiMarriedResidence indiMarriedComment",misc2.getIndi().getComment() .toString());
        assertEquals("IndiMarriedFirstName",    "",misc2.getIndi().getMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     "",misc2.getIndi().getMarriedLastName().toString());
        assertEquals("IndiMarriedOccupation",   "",misc2.getIndi().getMarriedOccupation().toString());
        assertEquals("IndiMarriedComment",      "",misc2.getIndi().getMarriedComment().toString());
        assertEquals("IndiMarriedDead",         "",misc2.getIndi().getMarriedDead().toString());
        assertEquals("IndiFatherFirstName",     misc.getIndi().getFatherFirstName().toString(),misc2.getIndi().getFatherFirstName().toString());
        assertEquals("IndiFatherLastName",      misc.getIndi().getLastName().toString(),misc2.getIndi().getFatherLastName().toString());
        assertEquals("IndiFatherAge",           "",misc2.getIndi().getFatherAge().toString());
        assertEquals("IndiFatherDead",          misc.getIndi().getFatherDead().toString(),misc2.getIndi().getFatherDead().toString());
        assertEquals("IndiFatherOccupation",    "",misc2.getIndi().getFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "indiFatherComment, indiFatherOccupation, indiFatherResidence, Age:70a",misc2.getIndi().getFatherComment().toString());
        assertEquals("IndiMotherFirstName",     misc.getIndi().getMotherFirstName().toString(),misc2.getIndi().getMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      misc.getIndi().getMotherLastName().toString(),misc2.getIndi().getMotherLastName().toString());
        assertEquals("IndiMotherAge",           "",misc2.getIndi().getMotherAge().toString());
        assertEquals("IndiMotherDead",          misc.getIndi().getMotherDead().toString(),misc2.getIndi().getMotherDead().toString());
        assertEquals("IndiMotherOccupation",    "",misc2.getIndi().getMotherOccupation().toString());
        assertEquals("IndiMotherComment",      "indiMotherComment, indiMotherOccupation, indiMotherResidence, Age:72a",misc2.getIndi().getMotherComment().toString());

        assertEquals("WifeFirstName",           misc.getWife().getFirstName().toString(),misc2.getWife().getFirstName().toString());
        assertEquals("WifeLastName",            misc.getWife().getLastName().toString(),misc2.getWife().getLastName().toString());
        assertEquals("WifeSex",                 misc.getWife().getSex().toString(),misc2.getWife().getSex().toString());
        assertEquals("WifeAge",                 misc.getWife().getAge().toString(),misc2.getWife().getAge().toString());
        assertEquals("WifeBirthDate",           "",misc2.getWife().getBirthDate().toString());
        assertEquals("WifeBirthPlace",          "",misc2.getWife().getBirthPlace().toString());
        assertEquals("WifeOccupation",          "",misc2.getWife().getOccupation().toString());
        assertEquals("WifeResidence",           misc.getWife().getResidence().toString(),misc2.getWife().getResidence().toString());
        assertEquals("WifeComment",             "wifeComment, né le 02/02/1992, wifeBirthPlace, wifeOccupation, Ex conjoint: wifeMarriedFirstname wifeMarriedLastname Décédé wifeMarriedOccupation wifeMarriedResidence wifeMarriedComment",misc2.getWife().getComment().toString());
        assertEquals("WifeMarriedFirstName",    "",misc2.getWife().getMarriedFirstName().toString());
        assertEquals("WifeMarriedLastName",     "",misc2.getWife().getMarriedLastName().toString());
        assertEquals("WifeMarriedOccupation",   "",misc2.getWife().getMarriedOccupation().toString());
        assertEquals("WifeMarriedComment",      "",misc2.getWife().getMarriedComment().toString());
        assertEquals("WifeMarriedDead",         "",misc2.getWife().getMarriedDead().toString());
        assertEquals("WifeFatherFirstName",     misc.getWife().getFatherFirstName().toString(),misc2.getWife().getFatherFirstName().toString());
        assertEquals("WifeFatherLastName",      misc.getWife().getLastName().toString(),misc2.getWife().getFatherLastName().toString());
        assertEquals("WifeFatherAge",           "",misc2.getWife().getFatherAge().toString());
        assertEquals("WifeFatherOccupation",    "",misc2.getWife().getFatherOccupation().toString());
        assertEquals("WifeFatherComment",       "wifeFatherComment, wifeFatherOccupation, wifeFatherResidence, Age:71a",misc2.getWife().getFatherComment().toString());
        assertEquals("WifeFatherDead",          misc.getWife().getFatherDead().toString(),misc2.getWife().getFatherDead().toString());
        assertEquals("WifeMotherFirstName",     misc.getWife().getMotherFirstName().toString(),misc2.getWife().getMotherFirstName().toString());
        assertEquals("WifeMotherLastName",      misc.getWife().getMotherLastName().toString(),misc2.getWife().getMotherLastName().toString());
        assertEquals("WifeMotherAge",           "",misc2.getWife().getMotherAge().toString());
        assertEquals("WifeMotherDead",          misc.getWife().getMotherDead().toString(),misc2.getWife().getMotherDead().toString());
        assertEquals("WifeMotherOccupation",    "",misc2.getWife().getMotherOccupation().toString());
        assertEquals("WifeMotherComment",       "wifeMotherComment, wifeMotherOccupation, wifeMotherResidence, Age:73a",misc2.getWife().getMotherComment().toString());

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

        assertEquals("EventDate",   misc.getEventDateProperty().toString(),misc2.getEventDateProperty().toString());
        assertEquals("EventType",   "WILL",misc2.getEventType().toString());
        assertEquals("parish",      misc.getParish().toString(),misc2.getParish().toString());
        assertEquals("Notary",      misc.getNotary().toString(),misc2.getNotary().toString());
        assertEquals("Cote",        misc.getCote().toString(),misc2.getCote().toString());
        assertEquals("FreeComment", misc.getFreeComment().toString(),misc2.getFreeComment().toString());

        assertEquals("IndiFirstName",           misc.getIndi().getFirstName().toString(),misc2.getIndi().getFirstName().toString());
        assertEquals("IndiLastName",            misc.getIndi().getLastName().toString(),misc2.getIndi().getLastName().toString());
        assertEquals("IndiSex",                 misc.getIndi().getSex().toString(),misc2.getIndi().getSex().toString());
        assertEquals("IndiAge",                 misc.getIndi().getAge().toString(),misc2.getIndi().getAge().toString());
        assertEquals("IndiBirthDate",           "",misc2.getIndi().getBirthDate().getValue());
        assertEquals("IndiBirthPlace",          "",misc2.getIndi().getBirthPlace().toString());
        assertEquals("IndiPlace",               "",misc2.getIndi().getBirthPlace().toString());
        assertEquals("IndiOccupation",          "",misc2.getIndi().getOccupation().toString());
        assertEquals("IndiComment",             "indiComment, né le 01/02/1990, indiBirthPlace, indiOccupation",misc2.getIndi().getComment() .toString());

        assertEquals("IndiMarriedFirstName",    misc.getIndi().getMarriedFirstName().toString(),misc2.getIndi().getMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     misc.getIndi().getMarriedLastName().toString(),misc2.getIndi().getMarriedLastName().toString());
        //assertEquals("WifeSex",                 misc.getIndi().getMarriedSex().toString(),misc2.getWife().getSex().toString());
        //assertEquals("WifeAge",                 misc.getWife().getAge().toString(),misc2.getWife().getAge().toString());
        //assertEquals("WifeBirthDate",           misc.getIndi().getMarriedDead().toString(),misc2.getWifeD().toString());
        assertEquals("IndiMarriedDead",         misc.getIndi().getMarriedDead().toString(),misc2.getIndi().getMarriedDead().toString());
        //assertEquals("WifeBirthPlace",          "",misc2.getWife().getBirthPlace().toString());
        assertEquals("IndiMarriedOccupation",   "",misc2.getIndi().getMarriedOccupation().toString());
        assertEquals("IndiMarriedResidence",    misc.getIndi().getMarriedResidence().toString(),misc2.getIndi().getMarriedResidence().toString());
        assertEquals("IndiMarriedComment",      "indiMarriedOccupation, indiMarriedComment",misc2.getIndi().getMarriedComment().toString());

        assertEquals("IndiFatherFirstName",     misc.getIndi().getFatherFirstName().toString(),misc2.getIndi().getFatherFirstName().toString());
        assertEquals("IndiFatherLastName",      misc.getIndi().getLastName().toString(),misc2.getIndi().getFatherLastName().toString());
        assertEquals("IndiFatherAge",           "",misc2.getIndi().getFatherAge().toString());
        assertEquals("IndiFatherDead",          misc.getIndi().getFatherDead().toString(),misc2.getIndi().getFatherDead().toString());
        assertEquals("IndiFatherOccupation",    "",misc2.getIndi().getFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "indiFatherComment, indiFatherOccupation, indiFatherResidence, Age:70a",misc2.getIndi().getFatherComment().toString());
        assertEquals("IndiMotherFirstName",     misc.getIndi().getMotherFirstName().toString(),misc2.getIndi().getMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      misc.getIndi().getMotherLastName().toString(),misc2.getIndi().getMotherLastName().toString());
        assertEquals("IndiMotherAge",           "",misc2.getIndi().getMotherAge().toString());
        assertEquals("IndiMotherDead",          misc.getIndi().getMotherDead().toString(),misc2.getIndi().getMotherDead().toString());
        assertEquals("IndiMotherOccupation",    "",misc2.getIndi().getMotherOccupation().toString());
        assertEquals("IndiMotherComment",       "indiMotherComment, indiMotherOccupation, indiMotherResidence, Age:72a",misc2.getIndi().getMotherComment().toString());

        assertEquals("WifeFirstName",           "",misc2.getWife().getFirstName().toString());
        assertEquals("WifeLastName",            "",misc2.getWife().getLastName().toString());
        assertEquals("WifeSex",                 "",misc2.getWife().getSex().toString());
        assertEquals("WifeAge",                 "",misc2.getWife().getAge().toString());
        assertEquals("WifeBirthDate",           "",misc2.getWife().getBirthDate().toString());
        assertEquals("WifeBirthPlace",          "",misc2.getWife().getBirthPlace().toString());
        assertEquals("WifeOccupation",          "",misc2.getWife().getOccupation().toString());
        assertEquals("WifeResidence",           "",misc2.getWife().getResidence().toString());
        assertEquals("WifeComment",             "",misc2.getWife().getComment().toString());
        assertEquals("WifeMarriedFirstName",    "",misc2.getWife().getMarriedFirstName().toString());
        assertEquals("WifeMarriedLastName",     "",misc2.getWife().getMarriedLastName().toString());
        assertEquals("WifeMarriedOccupation",   "",misc2.getWife().getMarriedOccupation().toString());
        assertEquals("WifeMarriedComment",      "",misc2.getWife().getMarriedComment().toString());
        assertEquals("WifeMarriedDead",         "",misc2.getWife().getMarriedDead().toString());
        assertEquals("WifeFatherFirstName",     "",misc2.getWife().getFatherFirstName().toString());
        assertEquals("WifeFatherLastName",      "",misc2.getWife().getFatherLastName().toString());
        assertEquals("WifeFatherAge",           "",misc2.getWife().getFatherAge().toString());
        assertEquals("WifeFatherOccupation",    "",misc2.getWife().getFatherOccupation().toString());
        assertEquals("WifeFatherComment",       "",misc2.getWife().getFatherComment().toString());
        assertEquals("WifeFatherDead",          "",misc2.getWife().getFatherDead().toString());
        assertEquals("WifeMotherFirstName",     "",misc2.getWife().getMotherFirstName().toString());
        assertEquals("WifeMotherLastName",      "",misc2.getWife().getMotherLastName().toString());
        assertEquals("WifeMotherAge",           "",misc2.getWife().getMotherAge().toString());
        assertEquals("WifeMotherDead",          "",misc2.getWife().getMotherDead().toString());
        assertEquals("WifeMotherOccupation",    "",misc2.getWife().getMotherOccupation().toString());
        assertEquals("WifeMotherComment",       "",misc2.getWife().getMotherComment().toString());

        assertWitnesses(misc, misc2);
        assertEquals("GeneralComment", "generalcomment, témoin(s): w3firstname w3lastname w3occupation w3comment, w4firstname w4lastname w4occupation w4comment, insinué le 04/04/2012, Héritier: wifeFirstname wifeLastname, né le 02/02/1992 wifeBirthPlace, wifeComment, wifeOccupation, wifeResidence, Père de l'héritier: wifeFatherFirstname wifeFatherLastname Age:71a Vivant wifeFatherOccupation wifeFatherResidence wifeFatherComment, Mère de l'héritier: wifeMotherFirstname wifeMotherLastname Age:73a Vivant wifeMotherOccupation wifeMotherResidence wifeMotherComment, Conjoint de l'héritier: wifeMarriedFirstname wifeMarriedLastname Décédé wifeMarriedOccupation wifeMarriedResidence wifeMotherComment",misc2.getGeneralComment().toString());                                        

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

        assertEquals("EventDate",   misc.getEventDateProperty().toString(),misc2.getEventDateProperty().toString());
        assertEquals("EventType",   misc.getEventType().toString(),misc2.getEventType().toString());
        assertEquals("parish",      misc.getParish().toString(),misc2.getParish().toString());
        assertEquals("Notary",      misc.getNotary().toString(),misc2.getNotary().toString());
        assertEquals("Cote",        misc.getCote().toString(),misc2.getCote().toString());
        assertEquals("FreeComment", misc.getFreeComment().toString(),misc2.getFreeComment().toString());

        assertEquals("IndiFirstName",           misc.getIndi().getFirstName().toString(),misc2.getIndi().getFirstName().toString());
        assertEquals("IndiLastName",            misc.getIndi().getLastName().toString(),misc2.getIndi().getLastName().toString());
        assertEquals("IndiSex",                 misc.getIndi().getSex().toString(),misc2.getIndi().getSex().toString());
        assertEquals("IndiAge",                 misc.getIndi().getAge().toString(),misc2.getIndi().getAge().toString());
        assertEquals("IndiBirthDate",           "",misc2.getIndi().getBirthDate().getValue());
        assertEquals("IndiBirthPlace",          "",misc2.getIndi().getBirthPlace().toString());
        assertEquals("IndiPlace",               "",misc2.getIndi().getBirthPlace().toString());
        assertEquals("IndiOccupation",          "",misc2.getIndi().getOccupation().toString());
        assertEquals("IndiComment",             "indiComment, né le 01/02/1990, indiBirthPlace, indiOccupation",misc2.getIndi().getComment() .toString());

        assertEquals("IndiMarriedFirstName",    misc.getIndi().getMarriedFirstName().toString(),misc2.getIndi().getMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     misc.getIndi().getMarriedLastName().toString(),misc2.getIndi().getMarriedLastName().toString());
        assertEquals("IndiMarriedDead",         misc.getIndi().getMarriedDead().toString(),misc2.getIndi().getMarriedDead().toString());
        assertEquals("IndiMarriedOccupation",   "",misc2.getIndi().getMarriedOccupation().toString());
        assertEquals("IndiMarriedResidence",    misc.getIndi().getMarriedResidence().toString(),misc2.getIndi().getMarriedResidence().toString());
        assertEquals("IndiMarriedComment",      "indiMarriedOccupation, indiMarriedComment",misc2.getIndi().getMarriedComment().toString());

        assertEquals("IndiFatherFirstName",     misc.getIndi().getFatherFirstName().toString(),misc2.getIndi().getFatherFirstName().toString());
        assertEquals("IndiFatherLastName",      misc.getIndi().getLastName().toString(),misc2.getIndi().getFatherLastName().toString());
        assertEquals("IndiFatherAge",           "",misc2.getIndi().getFatherAge().toString());
        assertEquals("IndiFatherDead",          misc.getIndi().getFatherDead().toString(),misc2.getIndi().getFatherDead().toString());
        assertEquals("IndiFatherOccupation",    "",misc2.getIndi().getFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "indiFatherComment, indiFatherOccupation, indiFatherResidence, Age:70a",misc2.getIndi().getFatherComment().toString());
        assertEquals("IndiMotherFirstName",     misc.getIndi().getMotherFirstName().toString(),misc2.getIndi().getMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      misc.getIndi().getMotherLastName().toString(),misc2.getIndi().getMotherLastName().toString());
        assertEquals("IndiMotherAge",           "",misc2.getIndi().getMotherAge().toString());
        assertEquals("IndiMotherDead",          misc.getIndi().getMotherDead().toString(),misc2.getIndi().getMotherDead().toString());
        assertEquals("IndiMotherOccupation",    "",misc2.getIndi().getMotherOccupation().toString());
        assertEquals("IndiMotherComment",      "indiMotherComment, indiMotherOccupation, indiMotherResidence, Age:72a",misc2.getIndi().getMotherComment().toString());

        assertEquals("WifeFirstName",           "",misc2.getWife().getFirstName().toString());
        assertEquals("WifeLastName",            "",misc2.getWife().getLastName().toString());
        assertEquals("WifeSex",                 "",misc2.getWife().getSex().toString());
        assertEquals("WifeAge",                 "",misc2.getWife().getAge().toString());
        assertEquals("WifeBirthDate",           "",misc2.getWife().getBirthDate().toString());
        assertEquals("WifeBirthPlace",          "",misc2.getWife().getBirthPlace().toString());
        assertEquals("WifeOccupation",          "",misc2.getWife().getOccupation().toString());
        assertEquals("WifeResidence",           "",misc2.getWife().getResidence().toString());
        assertEquals("WifeComment",             "",misc2.getWife().getComment().toString());
        assertEquals("WifeMarriedFirstName",    "",misc2.getWife().getMarriedFirstName().toString());
        assertEquals("WifeMarriedLastName",     "",misc2.getWife().getMarriedLastName().toString());
        assertEquals("WifeMarriedOccupation",   "",misc2.getWife().getMarriedOccupation().toString());
        assertEquals("WifeMarriedComment",      "",misc2.getWife().getMarriedComment().toString());
        assertEquals("WifeMarriedDead",         "",misc2.getWife().getMarriedDead().toString());
        assertEquals("WifeFatherFirstName",     "",misc2.getWife().getFatherFirstName().toString());
        assertEquals("WifeFatherLastName",      "",misc2.getWife().getFatherLastName().toString());
        assertEquals("WifeFatherAge",           "",misc2.getWife().getFatherAge().toString());
        assertEquals("WifeFatherOccupation",    "",misc2.getWife().getFatherOccupation().toString());
        assertEquals("WifeFatherComment",       "",misc2.getWife().getFatherComment().toString());
        assertEquals("WifeFatherDead",          "",misc2.getWife().getFatherDead().toString());
        assertEquals("WifeMotherFirstName",     "",misc2.getWife().getMotherFirstName().toString());
        assertEquals("WifeMotherLastName",      "",misc2.getWife().getMotherLastName().toString());
        assertEquals("WifeMotherAge",           "",misc2.getWife().getMotherAge().toString());
        assertEquals("WifeMotherDead",          "",misc2.getWife().getMotherDead().toString());
        assertEquals("WifeMotherOccupation",    "",misc2.getWife().getMotherOccupation().toString());
        assertEquals("WifeMotherComment",       "",misc2.getWife().getMotherComment().toString());

        assertWitnesses(misc, misc2);
        assertEquals("generalcomment, Autre intervenant: wifeFirstname wifeLastname, né le 02/02/1992 wifeBirthPlace, wifeComment, wifeOccupation, wifeResidence, Père de l'intervenant: wifeFatherFirstname wifeFatherLastname Age:71a Vivant wifeFatherOccupation wifeFatherResidence wifeFatherComment, Mère de l'intervenant: wifeMotherFirstname wifeMotherLastname Age:73a Vivant wifeMotherOccupation wifeMotherResidence wifeMotherComment, Conjoint de l'intervenant: wifeMarriedFirstname wifeMarriedLastname Décédé wifeMarriedOccupation wifeMarriedResidence wifeMotherComment, témoin(s): w3firstname w3lastname w3occupation w3comment, w4firstname w4lastname w4occupation w4comment, insinué le 04/04/2012",misc2.getGeneralComment().toString());

        file.delete();

    }


    @Test
    public void testFormatAgeToField() {

         assertEquals("formatAgeToField 76", "76y", ReleveFileEgmt.formatAgeToField("76"));
         assertEquals("formatAgeToField 76", "76y", ReleveFileEgmt.formatAgeToField("76 ans"));
         assertEquals("formatAgeToField 76", "8d", ReleveFileEgmt.formatAgeToField("8 jours"));

     }

}
