package ancestris.modules.releve.file;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.Record.RecordType;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import ancestris.modules.releve.model.RecordMisc;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author Michel
 */
public class ReleveFileNimegueTest extends TestCase {
    
    public ReleveFileNimegueTest(String testName) {
        super(testName);
    }

    /**
     * Test of isValidFile method, of class ReleveFileNimegue.
     */
    @Test
    public void testIsValidFile() {
    }

    /**
     * Test of loadFile method, of class ReleveFileNimegue.
     */
    @Test
    public void testLoadFile() throws Exception {
        try {
            File file ;
            String data;
            FileBuffer sb;

            data = "";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("fichier vide" , sb.getError().length(), 0);
            file.delete();

            data = "NIMEGUEV3;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("Pas de type d'acte",  sb.getError().length()> 0, true);
            file.delete();

            data = "NIMEGUEV3;;;;;X;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("type acte=X",  sb.getError().length()> 0, true);
            file.delete();

            data = "NIMEGUEV3;;;;;N;;;;;";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("Nb champs incorrection",  sb.getError().length()> 0, true);
            file.delete();

            data = "NIMEGUEV3;;;;;N;;;;;;;;;;;;;;;;;;;;;;;;12";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("Naissance minimal sans separateur supplémentaire", sb.getError().length()> 0, true);
            file.delete();

            data = "NIMEGUEV3;;;;;N;;;;;;;;;;;;;;;;;;;;;;;;12;";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("Naissance minimal",  sb.getError().toString(), "");
            file.delete();


            data = "NIMEGUEV3;;;;;M;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;13;";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("Mariage minimal",  sb.getError().toString(), "");
            file.delete();

            data = "NIMEGUEV3;;;;;D;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;14;";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("Deces minimal",  sb.getError().toString(), "");
            file.delete();

            data = "NIMEGUEV3;;;;;V;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;15;";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("Divers minimal",  sb.getError().toString(), "");
            file.delete();

            data = "NIMEGUEV3;09202;Montégut-Plantaurel;09;Ariège;V;01/03/1818;;;;PM;PM;CROUZET;Pierre;M;Pailhès (09);          ;23;;;;;;CROUZET;Guilhaume;;;BARRÈRE;Jeanne;;;LOUBIÈRES;Pauline;F;Montégut;          ;34;;;;;;LOUBIÈRES;Jacques;;;ARMINGAUD;Anne;;;;;;;;;;;;;;;;1;";
            file = TestUtility.createFile(data);
            sb = ReleveFileNimegue.loadFile(file);
            assertEquals("Divers exemple",  sb.getError().toString(), "");
            file.delete();

        } catch (IOException ex) {
            fail("IOException "+ ex.toString());
        }
    }

    /**
     * Test de l'enregistrement d'une naissance
     */
    @Test
    public void testSaveFileBirth() throws Exception {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");

        DataManager dataManager = new DataManager();
        dataManager.setPlace("cityname","citycode","county","state","country");

        RecordBirth record = TestUtility.getRecordBirth();
        dataManager.addRecord(record);
        StringBuilder sb = ReleveFileNimegue.saveFile(dataManager, dataManager.getDataModel(), RecordType.BIRTH, file, false);
        assertEquals("verify save error", "", sb.toString());

        FileBuffer fb = ReleveFileNimegue.loadFile(file);
        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getBirthCount());
        RecordBirth record2 = (RecordBirth) fb.getRecords().get(0);

        // je compare tous les champs

        assertEquals("EventDate",               record.getEventDateProperty().toString(),record2.getEventDateProperty().toString());
        assertEquals("Cote",                    record.getCote().toString(),record2.getCote().toString());
        assertEquals("parish",                  "",record2.getParish().toString());
        assertEquals("Notary",                  null,record2.getNotary());
        assertEquals("EventType",               null,record2.getEventType());
        assertEquals("FreeComment",             record.getFreeComment().toString(),record2.getFreeComment().toString());

        assertEquals("IndiFirstName",           record.getIndi().getFirstName().toString(),record2.getIndi().getFirstName().toString());
        assertEquals("IndiLastName",            record.getIndi().getLastName().toString(),record2.getIndi().getLastName().toString());
        assertEquals("IndiSex",                 record.getIndi().getSex().toString(),record2.getIndi().getSex().toString());
        assertEquals("IndiAge",                 null,record2.getIndi().getAge());
        assertEquals("IndiBirthDate",           "",record2.getIndi().getBirthDate().toString());
        assertEquals("IndiPlace",               "",record2.getIndi().getBirthPlace().toString());
        assertEquals("IndiOccupation",          null,record2.getIndi().getOccupation());
        assertEquals("IndiComment",             "Lieu: indiBirthPlace, indiComment",record2.getIndi().getComment() .toString());
        assertEquals("IndiMarriedFirstName",    null,record2.getIndi().getMarriedFirstName());
        assertEquals("IndiMarriedLastName",     null,record2.getIndi().getMarriedLastName());
        assertEquals("IndiMarriedOccupation",   null,record2.getIndi().getMarriedOccupation());
        assertEquals("IndiMarriedComment",      null,record2.getIndi().getMarriedComment());
        assertEquals("IndiMarriedDead",         null,record2.getIndi().getMarriedDead());

        assertEquals("IndiFatherFirstName",     record.getIndi().getFatherFirstName().toString(),record2.getIndi().getFatherFirstName().toString());
        assertEquals("IndiFatherLastName",      record.getIndi().getFatherLastName().toString(),record2.getIndi().getFatherLastName().toString());
        assertEquals("IndiFatherAge",           "",record2.getIndi().getFatherAge().toString());
        assertEquals("IndiFatherDead",          "",record2.getIndi().getFatherDead().toString());
        assertEquals("IndiFatherOccupation",    record.getIndi().getFatherOccupation().toString(),record2.getIndi().getFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "70y, indiFatherResidence, indiFatherComment",record2.getIndi().getFatherComment().toString());
        assertEquals("IndiMotherFirstName",     record.getIndi().getMotherFirstName().toString(),record2.getIndi().getMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      record.getIndi().getMotherLastName().toString(),record2.getIndi().getMotherLastName().toString());
        assertEquals("IndiMotherAge",           "",record2.getIndi().getMotherAge().toString());
        assertEquals("IndiMotherDead",          "",record2.getIndi().getMotherDead().toString());
        assertEquals("IndiMotherOccupation",    record.getIndi().getMotherOccupation().toString(),record2.getIndi().getMotherOccupation().toString());
        assertEquals("IndiMotherComment",       "72y, indiMotherResidence, indiMotherComment",record2.getIndi().getMotherComment().toString());
        
        assertEquals("WifeFirstName",           null,record2.getWife().getFirstName());
        assertEquals("WifeLastName",            null,record2.getWife().getLastName());
        assertEquals("WifeSex",                 null,record2.getWife().getSex());
        assertEquals("WifeAge",                 null,record2.getWife().getAge());
        assertEquals("WifeBirthDate",           null,record2.getWife().getBirthDate());
        assertEquals("WifePlace",               null,record2.getWife().getBirthPlace());
        assertEquals("WifeOccupation",          null,record2.getWife().getOccupation());
        assertEquals("WifeComment",             null,record2.getWife().getComment());
        assertEquals("WifeMarriedFirstName",    null,record2.getWife().getMarriedFirstName());
        assertEquals("WifeMarriedLastName",     null,record2.getWife().getMarriedLastName());
        assertEquals("WifeMarriedOccupation",   null,record2.getWife().getMarriedOccupation());
        assertEquals("WifeMarriedComment",      null,record2.getWife().getMarriedComment());
        assertEquals("WifeMarriedDead",         null,record2.getWife().getMarriedDead());
        assertEquals("WifeFatherFirstName",     null,record2.getWife().getFatherFirstName());
        assertEquals("WifeFatherLastName",      null,record2.getWife().getFatherLastName());
        assertEquals("WifeFatherAge",           null,record2.getWife().getFatherAge());
        assertEquals("WifeFatherDead",          null,record2.getWife().getFatherDead());
        assertEquals("WifeFatherOccupation",    null,record2.getWife().getFatherOccupation());
        assertEquals("WifeFatherComment",       null,record2.getWife().getFatherComment());
        assertEquals("WifeMotherFirstName",     null,record2.getWife().getMotherFirstName());
        assertEquals("WifeMotherLastName",      null,record2.getWife().getMotherLastName());
        assertEquals("WifeMotherAge",           null,record2.getWife().getMotherAge());
        assertEquals("WifeMotherDead",          null,record2.getWife().getMotherDead());
        assertEquals("WifeMotherOccupation",    null,record2.getWife().getMotherOccupation());
        assertEquals("WifeMotherComment",       null,record2.getWife().getMotherComment());
        
//        assertEquals("Witness1FirstName",     record.getWitness1FirstName().toString(),record2.getWitness1FirstName().toString());
//        assertEquals("Witness1LastName",      record.getWitness1LastName().toString(),record2.getWitness1LastName().toString());
//        assertEquals("Witness1Occupation",    "",record2.getWitness1Occupation().toString());
//        assertEquals("Witness1Comment",       "w1comment, w1occupation",record2.getWitness1Comment().toString());
//        assertEquals("Witness2FirstName",     record.getWitness2FirstName().toString(),record2.getWitness2FirstName().toString());
//        assertEquals("Witness2LastName",      record.getWitness2LastName().toString(),record2.getWitness2LastName().toString());
//        assertEquals("Witness2Occupation",    "",record2.getWitness2Occupation().toString());
//        assertEquals("Witness2Comment",       "w2comment, w2occupation",record2.getWitness2Comment().toString());
//        assertEquals("Witness3FirstName",     "",record2.getWitness3FirstName().toString());
//        assertEquals("Witness3LastName",      "",record2.getWitness3LastName().toString());
//        assertEquals("Witness3Occupation",    "",record2.getWitness3Occupation().toString());
//        assertEquals("Witness3Comment",       "",record2.getWitness3Comment().toString());
//        assertEquals("Witness4FirstName",     "",record2.getWitness4FirstName().toString());
//        assertEquals("Witness4LastName",      "",record2.getWitness4LastName().toString());
//        assertEquals("Witness4Occupation",    "",record2.getWitness4Occupation().toString());
//        assertEquals("Witness4Comment",       "",record2.getWitness4Comment().toString());

        assertWitnesses(record, record2);

        file.delete();

    }
    
    private void assertWitnesses(Record record, Record record2) {
//         for(int i=0 ; i < 2; i++ ) {
//            assertEquals("Witness "+ (i+1)+ " FirstName" , record.getWitnesses()[i].getFirstName().toString(),record2.getWitnesses()[i].getFirstName().toString());
//            assertEquals("Witness "+ (i+1)+ " LastName",   record.getWitnesses()[i].getLastName().toString(),record2.getWitnesses()[i].getLastName().toString());
//            assertEquals("Witness "+ (i+1)+ " Occupation", "",  record2.getWitnesses()[i].getOccupation().toString());
//            assertEquals("Witness "+ (i+1)+ " Comment",    record.getWitnesses()[i].getComment().toString()+ ", " + record.getWitnesses()[i].getOccupation().toString(),
//                                                           record2.getWitnesses()[i].getComment().toString());
//        }
//        for(int i=2 ; i < 4; i++ ) {
//            assertEquals("Witness "+ (i+1)+ " FirstName" , "", record2.getWitnesses()[i].getFirstName().toString());
//            assertEquals("Witness "+ (i+1)+ " LastName",   "", record2.getWitnesses()[i].getLastName().toString());
//            assertEquals("Witness "+ (i+1)+ " Occupation", "", record2.getWitnesses()[i].getOccupation().toString());
//            assertEquals("Witness "+ (i+1)+ " Comment",    "", record2.getWitnesses()[i].getComment().toString());
//        }
//        
//        assertEquals("GeneralComment", "generalcomment, témoin: w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment",record2.getGeneralComment().toString());

    }


    /**
     * Test de l'enregistrement d'un mariage
     */
    @Test
    public void testSaveFileMarriage() throws Exception {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");
        
        DataManager dataManager = new DataManager();
        dataManager.setPlace("cityname","citycode","county","state","country");

        RecordMarriage record = TestUtility.getRecordMarriage();
        dataManager.addRecord(record);
        StringBuilder sb = ReleveFileNimegue.saveFile(dataManager, dataManager.getDataModel(), RecordType.MARRIAGE, file, false);
        assertEquals("save result", "", sb.toString());

        FileBuffer fb = ReleveFileNimegue.loadFile(file);
        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getMarriageCount());
        RecordMarriage record2 = (RecordMarriage) fb.getRecords().get(0);

        // je compare tous les champs

        assertEquals("EventDate",      record.getEventDateProperty().toString(),record2.getEventDateProperty().toString());
        assertEquals("Cote",           record.getCote().toString(),record2.getCote().toString());
        assertEquals("parish",         "",record2.getParish().toString());
        assertEquals("Notary",         null,record2.getNotary());
        assertEquals("EventType",      null,record2.getEventType());
        assertEquals("FreeComment",    record.getFreeComment().toString(),record2.getFreeComment().toString());

        assertEquals("IndiFirstName",  record.getIndi().getFirstName().toString(),record2.getIndi().getFirstName().toString());
        assertEquals("IndiLastName",   record.getIndi().getLastName().toString(),record2.getIndi().getLastName().toString());
        assertEquals("IndiSex",        record.getIndi().getSex().toString(),record2.getIndi().getSex().toString());
        assertEquals("IndiAge",        record.getIndi().getAge().toString(),record2.getIndi().getAge().toString());
        assertEquals("IndiBirthDate",  record.getIndi().getBirthDate().toString(),record2.getIndi().getBirthDate().toString());
        assertEquals("IndiPlace",      record.getIndi().getBirthPlace().toString(),record2.getIndi().getBirthPlace().toString());
        assertEquals("IndiOccupation", record.getIndi().getOccupation().toString(),record2.getIndi().getOccupation().toString());
        assertEquals("IndiComment",    record2.getIndi().getComment() .toString(),record2.getIndi().getComment() .toString());
        assertEquals("IndiMarriedFirstName",    record.getIndi().getMarriedFirstName().toString(),record2.getIndi().getMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     record.getIndi().getMarriedLastName().toString(),record2.getIndi().getMarriedLastName().toString());
        assertEquals("IndiMarriedOccupation",   "",record2.getIndi().getMarriedOccupation().toString());
        assertEquals("IndiMarriedComment",      "Décédé, indiMarriedOccupation, indiMarriedResidence, indiMarriedComment",record2.getIndi().getMarriedComment().toString());
        assertEquals("IndiMarriedDead",         "",record2.getIndi().getMarriedDead().toString());

        assertEquals("IndiFatherFirstName",     record.getIndi().getFatherFirstName().toString(),record2.getIndi().getFatherFirstName().toString());
        assertEquals("IndiFatherLastName",      record.getIndi().getFatherLastName().toString(),record2.getIndi().getFatherLastName().toString());
        assertEquals("IndiFatherAge",           "",record2.getIndi().getFatherAge().toString());
        assertEquals("IndiFatherDead",          "",record2.getIndi().getFatherDead().toString());
        assertEquals("IndiFatherOccupation",    record.getIndi().getFatherOccupation().toString(),record2.getIndi().getFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "70y, Vivant, indiFatherResidence, indiFatherComment",record2.getIndi().getFatherComment().toString());
        assertEquals("IndiMotherFirstName",     record.getIndi().getMotherFirstName().toString(),record2.getIndi().getMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      record.getIndi().getMotherLastName().toString(),record2.getIndi().getMotherLastName().toString());
        assertEquals("IndiMotherAge",           "",record2.getIndi().getMotherAge().toString());
        assertEquals("IndiMotherDead",          "",record2.getIndi().getMotherDead().toString());
        assertEquals("IndiMotherOccupation",    record.getIndi().getMotherOccupation().toString(),record2.getIndi().getMotherOccupation().toString());
        assertEquals("IndiMotherComment",       "72y, Vivant, indiMotherResidence, indiMotherComment",record2.getIndi().getMotherComment().toString());

        assertEquals("WifeFirstName",           record.getWife().getFirstName().toString(),record2.getWife().getFirstName().toString());
        assertEquals("WifeLastName",            record.getWife().getLastName().toString(),record2.getWife().getLastName().toString());
        assertEquals("WifeSex",                 record.getWife().getSex().toString(),record2.getWife().getSex().toString());
        assertEquals("WifeAge",                 record.getWife().getAge().toString(),record2.getWife().getAge().toString());
        assertEquals("WifeBirthDate",           record.getWife().getBirthDate().toString(),record2.getWife().getBirthDate().toString());
        assertEquals("WifePlace",               record.getWife().getBirthPlace().toString(),record2.getWife().getBirthPlace().toString());
        assertEquals("WifeOccupation",          record.getWife().getOccupation().toString(),record2.getWife().getOccupation().toString());
        assertEquals("WifeComment",             record2.getWife().getComment().toString(),record2.getWife().getComment().toString());
        assertEquals("WifeMarriedFirstName",    record.getWife().getMarriedFirstName().toString(),record2.getWife().getMarriedFirstName().toString());
        assertEquals("WifeMarriedLastName",     record.getWife().getMarriedLastName().toString(),record2.getWife().getMarriedLastName().toString());
        assertEquals("WifeMarriedDead",         "",record2.getWife().getMarriedDead().toString());
        assertEquals("WifeMarriedOccupation",   "",record2.getWife().getMarriedOccupation().toString());
        assertEquals("WifeMarriedComment",      "Vivant, wifeMarriedOccupation, wifeMarriedResidence, wifeMarriedComment",record2.getWife().getMarriedComment().toString());

        assertEquals("WifeFatherFirstName",     record.getWife().getFatherFirstName().toString(),record2.getWife().getFatherFirstName().toString());
        assertEquals("WifeFatherLastName",      record.getWife().getFatherLastName().toString(),record2.getWife().getFatherLastName().toString());
        assertEquals("WifeFatherAge",           "",record2.getWife().getFatherAge().toString());
        assertEquals("WifeFatherDead",          "",record2.getWife().getFatherDead().toString());
        assertEquals("WifeFatherOccupation",    record.getWife().getFatherOccupation().toString(),record2.getWife().getFatherOccupation().toString());
        assertEquals("WifeFatherComment",       "71y, Vivant, wifeFatherResidence, wifeFatherComment",record2.getWife().getFatherComment().toString());
        assertEquals("WifeMotherFirstName",     record.getWife().getMotherFirstName().toString(),record2.getWife().getMotherFirstName().toString());
        assertEquals("WifeMotherLastName",      record.getWife().getMotherLastName().toString(),record2.getWife().getMotherLastName().toString());
        assertEquals("WifeMotherAge",           "",record2.getWife().getMotherAge().toString());
        assertEquals("WifeMotherDead",          "",record2.getWife().getMotherDead().toString());
        assertEquals("WifeMotherOccupation",    record.getWife().getMotherOccupation().toString(),record2.getWife().getMotherOccupation().toString());
        assertEquals("WifeMotherComment",       "73y, Vivant, wifeMotherResidence, wifeMotherComment",record2.getWife().getMotherComment().toString());

        assertWitnesses(record, record2);

        file.delete();
    }

    /**
     * Test de l'enregistrement d'un deces
     */
    @Test
    public void testSaveFileDeath() throws Exception {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");
        
        DataManager dataManager = new DataManager();
        dataManager.setPlace("cityname","citycode","county","state","country");

        RecordDeath record = TestUtility.getRecordDeath();
        dataManager.addRecord(record);
        StringBuilder sb = ReleveFileNimegue.saveFile(dataManager, dataManager.getDataModel(), RecordType.DEATH, file, false);
        assertEquals("verify save error", "", sb.toString());

        FileBuffer fb = ReleveFileNimegue.loadFile(file);
        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getDeathCount());
        RecordDeath record2 = (RecordDeath) fb.getRecords().get(0);

        // je compare tous les champs

        assertEquals("EventDate",       record.getEventDateProperty().toString(),record2.getEventDateProperty().toString());
        assertEquals("Cote",            record.getCote().toString(),record2.getCote().toString());
        assertEquals("parish",          "",record2.getParish().toString());
        assertEquals("Notary",          null,record2.getNotary());
        assertEquals("EventType",       null,record2.getEventType());
        assertEquals("FreeComment",    record.getFreeComment().toString(),record2.getFreeComment().toString());

        assertEquals("IndiFirstName",  record.getIndi().getFirstName().toString(),record2.getIndi().getFirstName().toString());
        assertEquals("IndiLastName",   record.getIndi().getLastName().toString(),record2.getIndi().getLastName().toString());
        assertEquals("IndiSex",        record.getIndi().getSex().toString(),record2.getIndi().getSex().toString());
        assertEquals("IndiAge",        record.getIndi().getAge().toString(),record2.getIndi().getAge().toString());
        assertEquals("IndiBirthDate",  record.getIndi().getBirthDate().toString(),record2.getIndi().getBirthDate().toString());
        assertEquals("IndiPlace",      record.getIndi().getBirthPlace().toString(),record2.getIndi().getBirthPlace().toString());
        assertEquals("IndiOccupation", record.getIndi().getOccupation().toString(),record2.getIndi().getOccupation().toString());
        assertEquals("IndiComment",    record2.getIndi().getComment() .toString(),record2.getIndi().getComment() .toString());
        assertEquals("IndiMarriedFirstName",    record.getIndi().getMarriedFirstName().toString(),record2.getIndi().getMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     record.getIndi().getMarriedLastName().toString(),record2.getIndi().getMarriedLastName().toString());
        assertEquals("IndiMarriedOccupation",   record.getIndi().getMarriedOccupation().toString(),record2.getIndi().getMarriedOccupation().toString());
        assertEquals("IndiMarriedComment",      "Vivant, indiMarriedResidence, indiMarriedComment",record2.getIndi().getMarriedComment().toString());
        assertEquals("IndiMarriedDead",         "",record2.getIndi().getMarriedDead().toString());

        assertEquals("IndiFatherFirstName",     record.getIndi().getFatherFirstName().toString(),record2.getIndi().getFatherFirstName().toString());
        assertEquals("IndiFatherLastName",      record.getIndi().getFatherLastName().toString(),record2.getIndi().getFatherLastName().toString());
        assertEquals("IndiFatherAge",           "",record2.getIndi().getFatherAge().toString());
        assertEquals("IndiFatherDead",          "",record2.getIndi().getFatherDead().toString());
        assertEquals("IndiFatherOccupation",    record.getIndi().getFatherOccupation().toString(),record2.getIndi().getFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "70y, Vivant, indiFatherResidence, indiFatherComment",record2.getIndi().getFatherComment().toString());
        assertEquals("IndiMotherFirstName",     record.getIndi().getMotherFirstName().toString(),record2.getIndi().getMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      record.getIndi().getMotherLastName().toString(),record2.getIndi().getMotherLastName().toString());
        assertEquals("IndiMotherAge",           "",record2.getIndi().getMotherAge().toString());
        assertEquals("IndiMotherDead",          "",record2.getIndi().getMotherDead().toString());
        assertEquals("IndiMotherOccupation",    record.getIndi().getMotherOccupation().toString(),record2.getIndi().getMotherOccupation().toString());
        assertEquals("IndiMotherComment",       "72y, Vivant, indiMotherResidence, indiMotherComment",record2.getIndi().getMotherComment().toString());

        assertEquals("WifeFirstName",           null,record2.getWife().getFirstName());
        assertEquals("WifeLastName",            null,record2.getWife().getLastName());
        assertEquals("WifeSex",                 null,record2.getWife().getSex());
        assertEquals("WifeAge",                 null,record2.getWife().getAge());
        assertEquals("WifeBirthDate",           null,record2.getWife().getBirthDate());
        assertEquals("WifePlace",               null,record2.getWife().getBirthPlace());
        assertEquals("WifeOccupation",          null,record2.getWife().getOccupation());
        assertEquals("WifeComment",             null,record2.getWife().getComment());
        assertEquals("WifeMarriedFirstName",    null,record2.getWife().getMarriedFirstName());
        assertEquals("WifeMarriedLastName",     null,record2.getWife().getMarriedLastName());
        assertEquals("WifeMarriedOccupation",   null,record2.getWife().getMarriedOccupation());
        assertEquals("WifeMarriedComment",      null,record2.getWife().getMarriedComment());
        assertEquals("WifeMarriedDead",         null,record2.getWife().getMarriedDead());
        assertEquals("WifeFatherFirstName",     null,record2.getWife().getFatherFirstName());
        assertEquals("WifeFatherLastName",      null,record2.getWife().getFatherLastName());
        assertEquals("WifeFatherAge",           null,record2.getWife().getFatherAge());
        assertEquals("WifeFatherDead",          null,record2.getWife().getFatherDead());
        assertEquals("WifeFatherOccupation",    null,record2.getWife().getFatherOccupation());
        assertEquals("WifeFatherComment",       null,record2.getWife().getFatherComment());
        assertEquals("WifeMotherFirstName",     null,record2.getWife().getMotherFirstName());
        assertEquals("WifeMotherLastName",      null,record2.getWife().getMotherLastName());
        assertEquals("WifeMotherAge",           null,record2.getWife().getMotherAge());
        assertEquals("WifeMotherDead",          null,record2.getWife().getMotherDead());
        assertEquals("WifeMotherOccupation",    null,record2.getWife().getMotherOccupation());
        assertEquals("WifeMotherComment",       null,record2.getWife().getMotherComment());

        assertWitnesses(record, record2);

        file.delete();

    }

    /**
     * Test de l'enregistrement d'un divers
     */
    @Test
    public void testSaveFileMisc() throws Exception {
        File file = new File(System.getProperty("user.home") + File.separator + "testsaveFile.txt");
        
        DataManager dataManager = new DataManager();
        dataManager.setPlace("cityname","citycode","county","state","country");

        RecordMisc record = TestUtility.getRecordMisc();
        dataManager.addRecord(record);
        StringBuilder sb = ReleveFileNimegue.saveFile(dataManager, dataManager.getDataModel(), RecordType.MISC, file, false);
        assertEquals("save result", "", sb.toString());

        FileBuffer fb = ReleveFileNimegue.loadFile(file);
        assertEquals("load result", "", fb.getError());
        assertEquals("load count", 1, fb.getMiscCount());
        RecordMisc record2 = (RecordMisc) fb.getRecords().get(0);

        // je compare tous les champs

        assertEquals("EventDate",      record.getEventDateProperty().toString(),record2.getEventDateProperty().toString());
        assertEquals("Cote",           record.getCote().toString(),record2.getCote().toString());
        assertEquals("parish",         "",record2.getParish().toString());
        assertEquals("Notary",         "",record2.getNotary().toString());
        assertEquals("EventType",      record.getEventType().toString(),record2.getEventType().toString());
        assertEquals("FreeComment",    record.getFreeComment().toString(),record2.getFreeComment().toString());

        assertEquals("IndiFirstName",  record.getIndi().getFirstName().toString(),record2.getIndi().getFirstName().toString());
        assertEquals("IndiLastName",   record.getIndi().getLastName().toString(),record2.getIndi().getLastName().toString());
        assertEquals("IndiSex",        record.getIndi().getSex().toString(),record2.getIndi().getSex().toString());
        assertEquals("IndiAge",        record.getIndi().getAge().toString(),record2.getIndi().getAge().toString());
        assertEquals("IndiBirthDate",  record.getIndi().getBirthDate().toString(),record2.getIndi().getBirthDate().toString());
        assertEquals("IndiPlace",      record.getIndi().getBirthPlace().toString(),record2.getIndi().getBirthPlace().toString());
        assertEquals("IndiOccupation", record.getIndi().getOccupation().toString(),record2.getIndi().getOccupation().toString());
        assertEquals("IndiComment",    record2.getIndi().getComment() .toString(),record2.getIndi().getComment() .toString());
        assertEquals("IndiMarriedFirstName",    record.getIndi().getMarriedFirstName().toString(),record2.getIndi().getMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     record.getIndi().getMarriedLastName().toString(),record2.getIndi().getMarriedLastName().toString());
        assertEquals("IndiMarriedOccupation",   "",record2.getIndi().getMarriedOccupation().toString());
        assertEquals("IndiMarriedComment",      "Décédé, indiMarriedOccupation, indiMarriedResidence, indiMarriedComment",record2.getIndi().getMarriedComment().toString());
        assertEquals("IndiMarriedDead",         "",record2.getIndi().getMarriedDead().toString());

        assertEquals("IndiFatherFirstName",     record.getIndi().getFatherFirstName().toString(),record2.getIndi().getFatherFirstName().toString());
        assertEquals("IndiFatherLastName",      record.getIndi().getFatherLastName().toString(),record2.getIndi().getFatherLastName().toString());
        assertEquals("IndiFatherAge",           "",record2.getIndi().getFatherAge().toString());
        assertEquals("IndiFatherDead",          "",record2.getIndi().getFatherDead().toString());
        assertEquals("IndiFatherOccupation",    record.getIndi().getFatherOccupation().toString(),record2.getIndi().getFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "70y, Décédé, indiFatherResidence, indiFatherComment",record2.getIndi().getFatherComment().toString());
        assertEquals("IndiMotherFirstName",     record.getIndi().getMotherFirstName().toString(),record2.getIndi().getMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      record.getIndi().getMotherLastName().toString(),record2.getIndi().getMotherLastName().toString());
        assertEquals("IndiMotherAge",           "",record2.getIndi().getMotherAge().toString());
        assertEquals("IndiMotherDead",          "",record2.getIndi().getMotherDead().toString());
        assertEquals("IndiMotherOccupation",    record.getIndi().getMotherOccupation().toString(),record2.getIndi().getMotherOccupation().toString());
        assertEquals("IndiMotherComment",       "72y, Décédé, indiMotherResidence, indiMotherComment",record2.getIndi().getMotherComment().toString());

        assertEquals("WifeFirstName",           record.getWife().getFirstName().toString(),record2.getWife().getFirstName().toString());
        assertEquals("WifeLastName",            record.getWife().getLastName().toString(),record2.getWife().getLastName().toString());
        assertEquals("WifeSex",                 record.getWife().getSex().toString(),record2.getWife().getSex().toString());
        assertEquals("WifeAge",                 record.getWife().getAge().toString(),record2.getWife().getAge().toString());
        assertEquals("WifeBirthDate",           record.getWife().getBirthDate().toString(),record2.getWife().getBirthDate().toString());
        assertEquals("WifePlace",               record.getWife().getBirthPlace().toString(),record2.getWife().getBirthPlace().toString());
        assertEquals("WifeOccupation",          record.getWife().getOccupation().toString(),record2.getWife().getOccupation().toString());
        assertEquals("WifeComment",             record2.getWife().getComment().toString(),record2.getWife().getComment().toString());
        assertEquals("WifeMarriedFirstName",    record.getWife().getMarriedFirstName().toString(),record2.getWife().getMarriedFirstName().toString());
        assertEquals("WifeMarriedLastName",     record.getWife().getMarriedLastName().toString(),record2.getWife().getMarriedLastName().toString());
        assertEquals("WifeMarriedOccupation",   "",record2.getWife().getMarriedOccupation().toString());
        assertEquals("WifeMarriedComment",      "Décédé, wifeMarriedOccupation, wifeMarriedResidence, wifeMarriedComment",record2.getWife().getMarriedComment().toString());
        assertEquals("WifeMarriedDead",         "",record2.getWife().getMarriedDead().toString());

        assertEquals("WifeFatherFirstName",     record.getWife().getFatherFirstName().toString(),record2.getWife().getFatherFirstName().toString());
        assertEquals("WifeFatherLastName",      record.getWife().getFatherLastName().toString(),record2.getWife().getFatherLastName().toString());
        assertEquals("WifeFatherAge",           "",record2.getWife().getFatherAge().toString());
        assertEquals("WifeFatherDead",          "",record2.getWife().getFatherDead().toString());
        assertEquals("WifeFatherOccupation",    record.getWife().getFatherOccupation().toString(),record2.getWife().getFatherOccupation().toString());
        assertEquals("WifeFatherComment",       "71y, Vivant, wifeFatherResidence, wifeFatherComment",record2.getWife().getFatherComment().toString());
        assertEquals("WifeMotherFirstName",     record.getWife().getMotherFirstName().toString(),record2.getWife().getMotherFirstName().toString());
        assertEquals("WifeMotherLastName",      record.getWife().getMotherLastName().toString(),record2.getWife().getMotherLastName().toString());
        assertEquals("WifeMotherAge",           "",record2.getWife().getMotherAge().toString());
        assertEquals("WifeMotherDead",          "",record2.getWife().getMotherDead().toString());
        assertEquals("WifeMotherOccupation",    record.getWife().getMotherOccupation().toString(),record2.getWife().getMotherOccupation().toString());
        assertEquals("WifeMotherComment",       "73y, Vivant, wifeMotherResidence, wifeMotherComment",record2.getWife().getMotherComment().toString());
        
        assertWitnesses(record, record2);

        file.delete();
    }
}
