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
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Michel
 */
public class ReleveFileNimegueTest {

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

        assertEquals("EventDate",               record.getFieldValue(FieldType.eventDate),record2.getFieldValue(FieldType.eventDate));
        assertEquals("Cote",                    record.getFieldValue(FieldType.cote),record2.getFieldValue(FieldType.cote));
        assertEquals("parish",                  "",record2.getFieldValue(FieldType.parish));
        assertEquals("Notary",                  "",record2.getFieldValue(FieldType.notary));
        assertEquals("EventType",               "",record2.getFieldValue(FieldType.eventType));
        assertEquals("FreeComment",             record.getFieldValue(FieldType.freeComment),record2.getFieldValue(FieldType.freeComment));

        assertEquals("IndiFirstName",           record.getFieldValue(FieldType.indiFirstName),record2.getFieldValue(FieldType.indiFirstName));
        assertEquals("IndiLastName",            record.getFieldValue(FieldType.indiLastName),record2.getFieldValue(FieldType.indiLastName));
        assertEquals("IndiSex",                 record.getFieldValue(FieldType.indiSex),record2.getFieldValue(FieldType.indiSex));
        assertEquals("IndiAge",                 "",record2.getFieldValue(FieldType.indiAge));
        assertEquals("IndiBirthDate",           "",record2.getFieldValue(FieldType.indiBirthDate));
        assertEquals("IndiPlace",               "",record2.getFieldValue(FieldType.indiBirthPlace));
        assertEquals("IndiOccupation",          "",record2.getFieldValue(FieldType.indiOccupation));
        assertEquals("IndiComment",             "Lieu: indiBirthPlace, indiComment",record2.getFieldValue(FieldType.indiComment));
        assertEquals("IndiMarriedFirstName",    "",record2.getFieldValue(FieldType.indiMarriedFirstName));
        assertEquals("IndiMarriedLastName",     "",record2.getFieldValue(FieldType.indiMarriedLastName));
        assertEquals("IndiMarriedOccupation",   "",record2.getFieldValue(FieldType.indiMarriedOccupation));
        assertEquals("IndiMarriedComment",      "",record2.getFieldValue(FieldType.indiMarriedComment));
        assertEquals("IndiMarriedDead",         "",record2.getFieldValue(FieldType.indiMarriedDead));

        assertEquals("IndiFatherFirstName",     record.getFieldValue(FieldType.indiFatherFirstName),record2.getFieldValue(FieldType.indiFatherFirstName));
        assertEquals("IndiFatherLastName",      record.getFieldValue(FieldType.indiFatherLastName),record2.getFieldValue(FieldType.indiFatherLastName));
        assertEquals("IndiFatherAge",           "",record2.getFieldValue(FieldType.indiFatherAge));
        assertEquals("IndiFatherDead",          "",record2.getFieldValue(FieldType.indiFatherDead));
        assertEquals("IndiFatherOccupation",    record.getFieldValue(FieldType.indiFatherOccupation),record2.getFieldValue(FieldType.indiFatherOccupation));
        assertEquals("IndiFatherComment",       "70y, indiFatherResidence, indiFatherComment",record2.getFieldValue(FieldType.indiFatherComment));
        assertEquals("IndiMotherFirstName",     record.getFieldValue(FieldType.indiMotherFirstName),record2.getFieldValue(FieldType.indiMotherFirstName));
        assertEquals("IndiMotherLastName",      record.getFieldValue(FieldType.indiMotherLastName),record2.getFieldValue(FieldType.indiMotherLastName));
        assertEquals("IndiMotherAge",           "",record2.getFieldValue(FieldType.indiMotherAge));
        assertEquals("IndiMotherDead",          "",record2.getFieldValue(FieldType.indiMotherDead));
        assertEquals("IndiMotherOccupation",    record.getFieldValue(FieldType.indiMotherOccupation),record2.getFieldValue(FieldType.indiMotherOccupation));
        assertEquals("IndiMotherComment",       "72y, indiMotherResidence, indiMotherComment",record2.getFieldValue(FieldType.indiMotherComment));

        assertEquals("WifeFirstName",           "",record2.getFieldValue(FieldType.wifeFirstName));
        assertEquals("WifeLastName",            "",record2.getFieldValue(FieldType.wifeLastName));
        assertEquals("WifeSex",                 "",record2.getFieldValue(FieldType.wifeSex));
        assertEquals("WifeAge",                 "",record2.getFieldValue(FieldType.wifeAge));
        assertEquals("WifeBirthDate",           "",record2.getFieldValue(FieldType.wifeBirthDate));
        assertEquals("WifePlace",               "",record2.getFieldValue(FieldType.wifeBirthPlace));
        assertEquals("WifeOccupation",          "",record2.getFieldValue(FieldType.wifeOccupation));
        assertEquals("WifeComment",             "",record2.getFieldValue(FieldType.wifeComment));
        assertEquals("WifeMarriedFirstName",    "",record2.getFieldValue(FieldType.wifeMarriedFirstName));
        assertEquals("WifeMarriedLastName",     "",record2.getFieldValue(FieldType.wifeMarriedLastName));
        assertEquals("WifeMarriedOccupation",   "",record2.getFieldValue(FieldType.wifeMarriedOccupation));
        assertEquals("WifeMarriedComment",      "",record2.getFieldValue(FieldType.wifeMarriedComment));
        assertEquals("WifeMarriedDead",         "",record2.getFieldValue(FieldType.wifeMarriedDead));
        assertEquals("WifeFatherFirstName",     "",record2.getFieldValue(FieldType.wifeFatherFirstName));
        assertEquals("WifeFatherLastName",      "",record2.getFieldValue(FieldType.wifeFatherLastName));
        assertEquals("WifeFatherAge",           "",record2.getFieldValue(FieldType.wifeFatherAge));
        assertEquals("WifeFatherDead",          "",record2.getFieldValue(FieldType.wifeFatherDead));
        assertEquals("WifeFatherOccupation",    "",record2.getFieldValue(FieldType.wifeFatherOccupation));
        assertEquals("WifeFatherComment",       "",record2.getFieldValue(FieldType.wifeFatherComment));
        assertEquals("WifeMotherFirstName",     "",record2.getFieldValue(FieldType.wifeMotherFirstName));
        assertEquals("WifeMotherLastName",      "",record2.getFieldValue(FieldType.wifeMotherLastName));
        assertEquals("WifeMotherAge",           "",record2.getFieldValue(FieldType.wifeMotherAge));
        assertEquals("WifeMotherDead",          "",record2.getFieldValue(FieldType.wifeMotherDead));
        assertEquals("WifeMotherOccupation",    "",record2.getFieldValue(FieldType.wifeMotherOccupation));
        assertEquals("WifeMotherComment",       "",record2.getFieldValue(FieldType.wifeMotherComment));

        assertWitnesses(record, record2);

        file.delete();

    }

    private void assertWitnesses(Record record, Record record2) {

        assertEquals("Witness1 " + " FirstName" , record.getFieldValue(FieldType.witness1FirstName),record2.getFieldValue(FieldType.witness1FirstName));
        assertEquals("Witness1 " + " LastName" ,  record.getFieldValue(FieldType.witness1LastName), record2.getFieldValue(FieldType.witness1LastName));
        assertEquals("Witness1 " + " Occupation", "",record2.getFieldValue(FieldType.witness1Occupation));
        assertEquals("Witness1 " + " Comment",    record.getFieldValue(FieldType.witness1Comment)+ ", " + record.getFieldValue(FieldType.witness1Occupation)
                     , record2.getFieldValue(FieldType.witness1Comment));

        assertEquals("Witness2 " + " FirstName" , record.getFieldValue(FieldType.witness2FirstName),record2.getFieldValue(FieldType.witness2FirstName));
        assertEquals("Witness2 " + " LastName" ,  record.getFieldValue(FieldType.witness2LastName),record2.getFieldValue(FieldType.witness2LastName));
        assertEquals("Witness2 " + " Occupation" , "",record2.getFieldValue(FieldType.witness2Occupation));
        assertEquals("Witness2 " + " Comment",    record.getFieldValue(FieldType.witness2Comment)+ ", " + record.getFieldValue(FieldType.witness2Occupation)
                     , record2.getFieldValue(FieldType.witness2Comment));

        assertEquals("Witness3 " + " FirstName" ,  "", record2.getFieldValue(FieldType.witness3FirstName));
        assertEquals("Witness3 " + " LastName" ,   "", record2.getFieldValue(FieldType.witness3LastName));
        assertEquals("Witness3 " + " Occupation",  "", record2.getFieldValue(FieldType.witness3Occupation));
        assertEquals("Witness3 "+ " Comment",      "", record2.getFieldValue(FieldType.witness3Comment));

        assertEquals("Witness4 " + " FirstName" ,  "", record2.getFieldValue(FieldType.witness4FirstName));
        assertEquals("Witness4 " + " LastName" ,   "", record2.getFieldValue(FieldType.witness4LastName));
        assertEquals("Witness4 " + " Occupation",  "", record2.getFieldValue(FieldType.witness4Occupation));
        assertEquals("Witness4 "+ " Comment",      "", record2.getFieldValue(FieldType.witness4Comment));

        //assertEquals("GeneralComment", "generalcomment, témoin: w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment",record2.getFieldValue(FieldType.generalComment));

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

        assertEquals("EventDate",      record.getFieldValue(FieldType.eventDate),record2.getFieldValue(FieldType.eventDate));
        assertEquals("Cote",           record.getFieldValue(FieldType.cote),record2.getFieldValue(FieldType.cote));
        assertEquals("parish",         "",record2.getFieldValue(FieldType.parish));
        assertEquals("Notary",         "",record2.getFieldValue(FieldType.notary));
        assertEquals("EventType",      "",record2.getFieldValue(FieldType.eventType));
        assertEquals("FreeComment",    record.getFieldValue(FieldType.freeComment),record2.getFieldValue(FieldType.freeComment));

        assertEquals("IndiFirstName",  record.getFieldValue(FieldType.indiFirstName),record2.getFieldValue(FieldType.indiFirstName));
        assertEquals("IndiLastName",   record.getFieldValue(FieldType.indiLastName),record2.getFieldValue(FieldType.indiLastName));
        assertEquals("IndiSex",        record.getFieldValue(FieldType.indiSex),record2.getFieldValue(FieldType.indiSex));
        assertEquals("IndiAge",        record.getFieldValue(FieldType.indiAge),record2.getFieldValue(FieldType.indiAge));
        assertEquals("IndiBirthDate",  record.getFieldValue(FieldType.indiBirthDate),record2.getFieldValue(FieldType.indiBirthDate));
        assertEquals("IndiPlace",      record.getFieldValue(FieldType.indiBirthPlace),record2.getFieldValue(FieldType.indiBirthPlace));
        assertEquals("IndiOccupation", record.getFieldValue(FieldType.indiOccupation),record2.getFieldValue(FieldType.indiOccupation));
        assertEquals("IndiComment",    record2.getFieldValue(FieldType.indiComment),record2.getFieldValue(FieldType.indiComment));
        assertEquals("IndiMarriedFirstName",    record.getFieldValue(FieldType.indiMarriedFirstName),record2.getFieldValue(FieldType.indiMarriedFirstName));
        assertEquals("IndiMarriedLastName",     record.getFieldValue(FieldType.indiMarriedLastName),record2.getFieldValue(FieldType.indiMarriedLastName));
        assertEquals("IndiMarriedOccupation",   "",record2.getFieldValue(FieldType.indiMarriedOccupation));
        assertEquals("IndiMarriedComment",      "Décédé, indiMarriedOccupation, indiMarriedResidence, indiMarriedComment",record2.getFieldValue(FieldType.indiMarriedComment));
        assertEquals("IndiMarriedDead",         "",record2.getFieldValue(FieldType.indiMarriedDead));

        assertEquals("IndiFatherFirstName",     record.getFieldValue(FieldType.indiFatherFirstName),record2.getFieldValue(FieldType.indiFatherFirstName));
        assertEquals("IndiFatherLastName",      record.getFieldValue(FieldType.indiFatherLastName),record2.getFieldValue(FieldType.indiFatherLastName));
        assertEquals("IndiFatherAge",           "",record2.getFieldValue(FieldType.indiFatherAge));
        assertEquals("IndiFatherDead",          "",record2.getFieldValue(FieldType.indiFatherDead));
        assertEquals("IndiFatherOccupation",    record.getFieldValue(FieldType.indiFatherOccupation),record2.getFieldValue(FieldType.indiFatherOccupation));
        assertEquals("IndiFatherComment",       "70y, Vivant, indiFatherResidence, indiFatherComment",record2.getFieldValue(FieldType.indiFatherComment));
        assertEquals("IndiMotherFirstName",     record.getFieldValue(FieldType.indiMotherFirstName),record2.getFieldValue(FieldType.indiMotherFirstName));
        assertEquals("IndiMotherLastName",      record.getFieldValue(FieldType.indiMotherLastName),record2.getFieldValue(FieldType.indiMotherLastName));
        assertEquals("IndiMotherAge",           "",record2.getFieldValue(FieldType.indiMotherAge));
        assertEquals("IndiMotherDead",          "",record2.getFieldValue(FieldType.indiMotherDead));
        assertEquals("IndiMotherOccupation",    record.getFieldValue(FieldType.indiMotherOccupation),record2.getFieldValue(FieldType.indiMotherOccupation));
        assertEquals("IndiMotherComment",       "72y, Vivant, indiMotherResidence, indiMotherComment",record2.getFieldValue(FieldType.indiMotherComment));

        assertEquals("WifeFirstName",           record.getFieldValue(FieldType.wifeFirstName),record2.getFieldValue(FieldType.wifeFirstName));
        assertEquals("WifeLastName",            record.getFieldValue(FieldType.wifeLastName),record2.getFieldValue(FieldType.wifeLastName));
        assertEquals("WifeSex",                 record.getFieldValue(FieldType.wifeSex),record2.getFieldValue(FieldType.wifeSex));
        assertEquals("WifeAge",                 record.getFieldValue(FieldType.wifeAge),record2.getFieldValue(FieldType.wifeAge));
        assertEquals("WifeBirthDate",           record.getFieldValue(FieldType.wifeBirthDate),record2.getFieldValue(FieldType.wifeBirthDate));
        assertEquals("WifePlace",               record.getFieldValue(FieldType.wifeBirthPlace),record2.getFieldValue(FieldType.wifeBirthPlace));
        assertEquals("WifeOccupation",          record.getFieldValue(FieldType.wifeOccupation),record2.getFieldValue(FieldType.wifeOccupation));
        assertEquals("WifeComment",             record2.getFieldValue(FieldType.wifeComment),record2.getFieldValue(FieldType.wifeComment));
        assertEquals("WifeMarriedFirstName",    record.getFieldValue(FieldType.wifeMarriedFirstName),record2.getFieldValue(FieldType.wifeMarriedFirstName));
        assertEquals("WifeMarriedLastName",     record.getFieldValue(FieldType.wifeMarriedLastName),record2.getFieldValue(FieldType.wifeMarriedLastName));
        assertEquals("WifeMarriedDead",         "",record2.getFieldValue(FieldType.wifeMarriedDead));
        assertEquals("WifeMarriedOccupation",   "",record2.getFieldValue(FieldType.wifeMarriedOccupation));
        assertEquals("WifeMarriedComment",      "Vivant, wifeMarriedOccupation, wifeMarriedResidence, wifeMarriedComment",record2.getFieldValue(FieldType.wifeMarriedComment));

        assertEquals("WifeFatherFirstName",     record.getFieldValue(FieldType.wifeFatherFirstName),record2.getFieldValue(FieldType.wifeFatherFirstName));
        assertEquals("WifeFatherLastName",      record.getFieldValue(FieldType.wifeFatherLastName),record2.getFieldValue(FieldType.wifeFatherLastName));
        assertEquals("WifeFatherAge",           "",record2.getFieldValue(FieldType.wifeFatherAge));
        assertEquals("WifeFatherDead",          "",record2.getFieldValue(FieldType.wifeFatherDead));
        assertEquals("WifeFatherOccupation",    record.getFieldValue(FieldType.wifeFatherOccupation),record2.getFieldValue(FieldType.wifeFatherOccupation));
        assertEquals("WifeFatherComment",       "71y, Vivant, wifeFatherResidence, wifeFatherComment",record2.getFieldValue(FieldType.wifeFatherComment));
        assertEquals("WifeMotherFirstName",     record.getFieldValue(FieldType.wifeMotherFirstName),record2.getFieldValue(FieldType.wifeMotherFirstName));
        assertEquals("WifeMotherLastName",      record.getFieldValue(FieldType.wifeMotherLastName),record2.getFieldValue(FieldType.wifeMotherLastName));
        assertEquals("WifeMotherAge",           "",record2.getFieldValue(FieldType.wifeMotherAge));
        assertEquals("WifeMotherDead",          "",record2.getFieldValue(FieldType.wifeMotherDead));
        assertEquals("WifeMotherOccupation",    record.getFieldValue(FieldType.wifeMotherOccupation),record2.getFieldValue(FieldType.wifeMotherOccupation));
        assertEquals("WifeMotherComment",       "73y, Vivant, wifeMotherResidence, wifeMotherComment",record2.getFieldValue(FieldType.wifeMotherComment));

        assertEquals("Witness1 " + " FirstName" , record.getFieldValue(FieldType.witness1FirstName),record2.getFieldValue(FieldType.witness1FirstName));
        assertEquals("Witness1 " + " LastName" ,  record.getFieldValue(FieldType.witness1LastName), record2.getFieldValue(FieldType.witness1LastName));
        assertEquals("Witness1 " + " Occupation", "",record2.getFieldValue(FieldType.witness1Occupation));
        assertEquals("Witness1 " + " Comment",    record.getFieldValue(FieldType.witness1Comment)+ ", " + record.getFieldValue(FieldType.witness1Occupation)
                     , record2.getFieldValue(FieldType.witness1Comment));

        assertEquals("Witness2 " + " FirstName" , record.getFieldValue(FieldType.witness2FirstName),record2.getFieldValue(FieldType.witness2FirstName));
        assertEquals("Witness2 " + " LastName" ,  record.getFieldValue(FieldType.witness2LastName),record2.getFieldValue(FieldType.witness2LastName));
        assertEquals("Witness2 " + " Occupation" , "",record2.getFieldValue(FieldType.witness2Occupation));
        assertEquals("Witness2 " + " Comment",    record.getFieldValue(FieldType.witness2Comment)+ ", " + record.getFieldValue(FieldType.witness2Occupation)
                     , record2.getFieldValue(FieldType.witness2Comment));

        assertEquals("Witness3 " + " FirstName" ,  record.getFieldValue(FieldType.witness3FirstName) , record2.getFieldValue(FieldType.witness3FirstName));
        assertEquals("Witness3 " + " LastName" ,   record.getFieldValue(FieldType.witness3LastName),  record2.getFieldValue(FieldType.witness3LastName));
        assertEquals("Witness3 " + " Occupation",  "",record2.getFieldValue(FieldType.witness3Occupation));
        assertEquals("Witness3 "+ " Comment",      record.getFieldValue(FieldType.witness3Comment)+ ", " + record.getFieldValue(FieldType.witness3Occupation)
                     , record2.getFieldValue(FieldType.witness3Comment));

        assertEquals("Witness4 " + " FirstName" ,  record.getFieldValue(FieldType.witness4FirstName) , record2.getFieldValue(FieldType.witness4FirstName));
        assertEquals("Witness4 " + " LastName" ,   record.getFieldValue(FieldType.witness4LastName),  record2.getFieldValue(FieldType.witness4LastName));
        assertEquals("Witness4 " + " Occupation",  "",record2.getFieldValue(FieldType.witness4Occupation));
        assertEquals("Witness4 "+ " Comment",      record.getFieldValue(FieldType.witness4Comment)+ ", " + record.getFieldValue(FieldType.witness4Occupation)
                     , record2.getFieldValue(FieldType.witness4Comment));

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

        assertEquals("EventDate",       record.getFieldValue(FieldType.eventDate),record2.getFieldValue(FieldType.eventDate));
        assertEquals("Cote",            record.getFieldValue(FieldType.cote),record2.getFieldValue(FieldType.cote));
        assertEquals("parish",          "",record2.getFieldValue(FieldType.parish));
        assertEquals("Notary",          "",record2.getFieldValue(FieldType.notary));
        assertEquals("EventType",       "",record2.getFieldValue(FieldType.eventType));
        assertEquals("FreeComment",    record.getFieldValue(FieldType.freeComment),record2.getFieldValue(FieldType.freeComment));

        assertEquals("IndiFirstName",  record.getFieldValue(FieldType.indiFirstName),record2.getFieldValue(FieldType.indiFirstName));
        assertEquals("IndiLastName",   record.getFieldValue(FieldType.indiLastName),record2.getFieldValue(FieldType.indiLastName));
        assertEquals("IndiSex",        record.getFieldValue(FieldType.indiSex),record2.getFieldValue(FieldType.indiSex));
        assertEquals("IndiAge",        record.getFieldValue(FieldType.indiAge),record2.getFieldValue(FieldType.indiAge));
        assertEquals("IndiBirthDate",  record.getFieldValue(FieldType.indiBirthDate),record2.getFieldValue(FieldType.indiBirthDate));
        assertEquals("IndiPlace",      record.getFieldValue(FieldType.indiBirthPlace),record2.getFieldValue(FieldType.indiBirthPlace));
        assertEquals("IndiOccupation", record.getFieldValue(FieldType.indiOccupation),record2.getFieldValue(FieldType.indiOccupation));
        assertEquals("IndiComment",    record2.getFieldValue(FieldType.indiComment),record2.getFieldValue(FieldType.indiComment));
        assertEquals("IndiMarriedFirstName",    record.getFieldValue(FieldType.indiMarriedFirstName),record2.getFieldValue(FieldType.indiMarriedFirstName));
        assertEquals("IndiMarriedLastName",     record.getFieldValue(FieldType.indiMarriedLastName),record2.getFieldValue(FieldType.indiMarriedLastName));
        assertEquals("IndiMarriedOccupation",   record.getFieldValue(FieldType.indiMarriedOccupation),record2.getFieldValue(FieldType.indiMarriedOccupation));
        assertEquals("IndiMarriedComment",      "Vivant, indiMarriedResidence, indiMarriedComment",record2.getFieldValue(FieldType.indiMarriedComment));
        assertEquals("IndiMarriedDead",         "",record2.getFieldValue(FieldType.indiMarriedDead));

        assertEquals("IndiFatherFirstName",     record.getFieldValue(FieldType.indiFatherFirstName),record2.getFieldValue(FieldType.indiFatherFirstName));
        assertEquals("IndiFatherLastName",      record.getFieldValue(FieldType.indiFatherLastName),record2.getFieldValue(FieldType.indiFatherLastName));
        assertEquals("IndiFatherAge",           "",record2.getFieldValue(FieldType.indiFatherAge));
        assertEquals("IndiFatherDead",          "",record2.getFieldValue(FieldType.indiFatherDead));
        assertEquals("IndiFatherOccupation",    record.getFieldValue(FieldType.indiFatherOccupation),record2.getFieldValue(FieldType.indiFatherOccupation));
        assertEquals("IndiFatherComment",       "70y, Vivant, indiFatherResidence, indiFatherComment",record2.getFieldValue(FieldType.indiFatherComment));
        assertEquals("IndiMotherFirstName",     record.getFieldValue(FieldType.indiMotherFirstName),record2.getFieldValue(FieldType.indiMotherFirstName));
        assertEquals("IndiMotherLastName",      record.getFieldValue(FieldType.indiMotherLastName),record2.getFieldValue(FieldType.indiMotherLastName));
        assertEquals("IndiMotherAge",           "",record2.getFieldValue(FieldType.indiMotherAge));
        assertEquals("IndiMotherDead",          "",record2.getFieldValue(FieldType.indiMotherDead));
        assertEquals("IndiMotherOccupation",    record.getFieldValue(FieldType.indiMotherOccupation),record2.getFieldValue(FieldType.indiMotherOccupation));
        assertEquals("IndiMotherComment",       "72y, Vivant, indiMotherResidence, indiMotherComment",record2.getFieldValue(FieldType.indiMotherComment));

        assertEquals("WifeFirstName",           "",record2.getFieldValue(FieldType.wifeFirstName));
        assertEquals("WifeLastName",            "",record2.getFieldValue(FieldType.wifeLastName));
        assertEquals("WifeSex",                 "",record2.getFieldValue(FieldType.wifeSex));
        assertEquals("WifeAge",                 "",record2.getFieldValue(FieldType.wifeAge));
        assertEquals("WifeBirthDate",           "",record2.getFieldValue(FieldType.wifeBirthDate));
        assertEquals("WifePlace",               "",record2.getFieldValue(FieldType.wifeBirthPlace));
        assertEquals("WifeOccupation",          "",record2.getFieldValue(FieldType.wifeOccupation));
        assertEquals("WifeComment",             "",record2.getFieldValue(FieldType.wifeComment));
        assertEquals("WifeMarriedFirstName",    "",record2.getFieldValue(FieldType.wifeMarriedFirstName));
        assertEquals("WifeMarriedLastName",     "",record2.getFieldValue(FieldType.wifeMarriedLastName));
        assertEquals("WifeMarriedOccupation",   "",record2.getFieldValue(FieldType.wifeMarriedOccupation));
        assertEquals("WifeMarriedComment",      "",record2.getFieldValue(FieldType.wifeMarriedComment));
        assertEquals("WifeMarriedDead",         "",record2.getFieldValue(FieldType.wifeMarriedDead));
        assertEquals("WifeFatherFirstName",     "",record2.getFieldValue(FieldType.wifeFatherFirstName));
        assertEquals("WifeFatherLastName",      "",record2.getFieldValue(FieldType.wifeFatherLastName));
        assertEquals("WifeFatherAge",           "",record2.getFieldValue(FieldType.wifeFatherAge));
        assertEquals("WifeFatherDead",          "",record2.getFieldValue(FieldType.wifeFatherDead));
        assertEquals("WifeFatherOccupation",    "",record2.getFieldValue(FieldType.wifeFatherOccupation));
        assertEquals("WifeFatherComment",       "",record2.getFieldValue(FieldType.wifeFatherComment));
        assertEquals("WifeMotherFirstName",     "",record2.getFieldValue(FieldType.wifeMotherFirstName));
        assertEquals("WifeMotherLastName",      "",record2.getFieldValue(FieldType.wifeMotherLastName));
        assertEquals("WifeMotherAge",           "",record2.getFieldValue(FieldType.wifeMotherAge));
        assertEquals("WifeMotherDead",          "",record2.getFieldValue(FieldType.wifeMotherDead));
        assertEquals("WifeMotherOccupation",    "",record2.getFieldValue(FieldType.wifeMotherOccupation));
        assertEquals("WifeMotherComment",       "",record2.getFieldValue(FieldType.wifeMotherComment));

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

        assertEquals("EventDate",      record.getFieldValue(FieldType.eventDate),record2.getFieldValue(FieldType.eventDate));
        assertEquals("Cote",           record.getFieldValue(FieldType.cote),record2.getFieldValue(FieldType.cote));
        assertEquals("parish",         "",record2.getFieldValue(FieldType.parish));
        assertEquals("Notary",         "",record2.getFieldValue(FieldType.notary));
        assertEquals("EventType",      record.getFieldValue(FieldType.eventType),record2.getFieldValue(FieldType.eventType));
        assertEquals("FreeComment",    record.getFieldValue(FieldType.freeComment),record2.getFieldValue(FieldType.freeComment));

        assertEquals("IndiFirstName",  record.getFieldValue(FieldType.indiFirstName),record2.getFieldValue(FieldType.indiFirstName));
        assertEquals("IndiLastName",   record.getFieldValue(FieldType.indiLastName),record2.getFieldValue(FieldType.indiLastName));
        assertEquals("IndiSex",        record.getFieldValue(FieldType.indiSex),record2.getFieldValue(FieldType.indiSex));
        assertEquals("IndiAge",        record.getFieldValue(FieldType.indiAge),record2.getFieldValue(FieldType.indiAge));
        assertEquals("IndiBirthDate",  record.getFieldValue(FieldType.indiBirthDate),record2.getFieldValue(FieldType.indiBirthDate));
        assertEquals("IndiPlace",      record.getFieldValue(FieldType.indiBirthPlace),record2.getFieldValue(FieldType.indiBirthPlace));
        assertEquals("IndiOccupation", record.getFieldValue(FieldType.indiOccupation),record2.getFieldValue(FieldType.indiOccupation));
        assertEquals("IndiComment",    record2.getFieldValue(FieldType.indiComment),record2.getFieldValue(FieldType.indiComment));
        assertEquals("IndiMarriedFirstName",    record.getFieldValue(FieldType.indiMarriedFirstName),record2.getFieldValue(FieldType.indiMarriedFirstName));
        assertEquals("IndiMarriedLastName",     record.getFieldValue(FieldType.indiMarriedLastName),record2.getFieldValue(FieldType.indiMarriedLastName));
        assertEquals("IndiMarriedOccupation",   "",record2.getFieldValue(FieldType.indiMarriedOccupation));
        assertEquals("IndiMarriedComment",      "Décédé, indiMarriedOccupation, indiMarriedResidence, indiMarriedComment",record2.getFieldValue(FieldType.indiMarriedComment));
        assertEquals("IndiMarriedDead",         "",record2.getFieldValue(FieldType.indiMarriedDead));

        assertEquals("IndiFatherFirstName",     record.getFieldValue(FieldType.indiFatherFirstName),record2.getFieldValue(FieldType.indiFatherFirstName));
        assertEquals("IndiFatherLastName",      record.getFieldValue(FieldType.indiFatherLastName),record2.getFieldValue(FieldType.indiFatherLastName));
        assertEquals("IndiFatherAge",           "",record2.getFieldValue(FieldType.indiFatherAge));
        assertEquals("IndiFatherDead",          "",record2.getFieldValue(FieldType.indiFatherDead));
        assertEquals("IndiFatherOccupation",    record.getFieldValue(FieldType.indiFatherOccupation),record2.getFieldValue(FieldType.indiFatherOccupation));
        assertEquals("IndiFatherComment",       "70y, Décédé, indiFatherResidence, indiFatherComment",record2.getFieldValue(FieldType.indiFatherComment));
        assertEquals("IndiMotherFirstName",     record.getFieldValue(FieldType.indiMotherFirstName),record2.getFieldValue(FieldType.indiMotherFirstName));
        assertEquals("IndiMotherLastName",      record.getFieldValue(FieldType.indiMotherLastName),record2.getFieldValue(FieldType.indiMotherLastName));
        assertEquals("IndiMotherAge",           "",record2.getFieldValue(FieldType.indiMotherAge));
        assertEquals("IndiMotherDead",          "",record2.getFieldValue(FieldType.indiMotherDead));
        assertEquals("IndiMotherOccupation",    record.getFieldValue(FieldType.indiMotherOccupation),record2.getFieldValue(FieldType.indiMotherOccupation));
        assertEquals("IndiMotherComment",       "72y, Décédé, indiMotherResidence, indiMotherComment",record2.getFieldValue(FieldType.indiMotherComment));

        assertEquals("WifeFirstName",           record.getFieldValue(FieldType.wifeFirstName),record2.getFieldValue(FieldType.wifeFirstName));
        assertEquals("WifeLastName",            record.getFieldValue(FieldType.wifeLastName),record2.getFieldValue(FieldType.wifeLastName));
        assertEquals("WifeSex",                 record.getFieldValue(FieldType.wifeSex),record2.getFieldValue(FieldType.wifeSex));
        assertEquals("WifeAge",                 record.getFieldValue(FieldType.wifeAge),record2.getFieldValue(FieldType.wifeAge));
        assertEquals("WifeBirthDate",           record.getFieldValue(FieldType.wifeBirthDate),record2.getFieldValue(FieldType.wifeBirthDate));
        assertEquals("WifePlace",               record.getFieldValue(FieldType.wifeBirthPlace),record2.getFieldValue(FieldType.wifeBirthPlace));
        assertEquals("WifeOccupation",          record.getFieldValue(FieldType.wifeOccupation),record2.getFieldValue(FieldType.wifeOccupation));
        assertEquals("WifeComment",             record2.getFieldValue(FieldType.wifeComment),record2.getFieldValue(FieldType.wifeComment));
        assertEquals("WifeMarriedFirstName",    record.getFieldValue(FieldType.wifeMarriedFirstName),record2.getFieldValue(FieldType.wifeMarriedFirstName));
        assertEquals("WifeMarriedLastName",     record.getFieldValue(FieldType.wifeMarriedLastName),record2.getFieldValue(FieldType.wifeMarriedLastName));
        assertEquals("WifeMarriedOccupation",   "",record2.getFieldValue(FieldType.wifeMarriedOccupation));
        assertEquals("WifeMarriedComment",      "Décédé, wifeMarriedOccupation, wifeMarriedResidence, wifeMarriedComment",record2.getFieldValue(FieldType.wifeMarriedComment));
        assertEquals("WifeMarriedDead",         "",record2.getFieldValue(FieldType.wifeMarriedDead));

        assertEquals("WifeFatherFirstName",     record.getFieldValue(FieldType.wifeFatherFirstName),record2.getFieldValue(FieldType.wifeFatherFirstName));
        assertEquals("WifeFatherLastName",      record.getFieldValue(FieldType.wifeFatherLastName),record2.getFieldValue(FieldType.wifeFatherLastName));
        assertEquals("WifeFatherAge",           "",record2.getFieldValue(FieldType.wifeFatherAge));
        assertEquals("WifeFatherDead",          "",record2.getFieldValue(FieldType.wifeFatherDead));
        assertEquals("WifeFatherOccupation",    record.getFieldValue(FieldType.wifeFatherOccupation),record2.getFieldValue(FieldType.wifeFatherOccupation));
        assertEquals("WifeFatherComment",       "71y, Vivant, wifeFatherResidence, wifeFatherComment",record2.getFieldValue(FieldType.wifeFatherComment));
        assertEquals("WifeMotherFirstName",     record.getFieldValue(FieldType.wifeMotherFirstName),record2.getFieldValue(FieldType.wifeMotherFirstName));
        assertEquals("WifeMotherLastName",      record.getFieldValue(FieldType.wifeMotherLastName),record2.getFieldValue(FieldType.wifeMotherLastName));
        assertEquals("WifeMotherAge",           "",record2.getFieldValue(FieldType.wifeMotherAge));
        assertEquals("WifeMotherDead",          "",record2.getFieldValue(FieldType.wifeMotherDead));
        assertEquals("WifeMotherOccupation",    record.getFieldValue(FieldType.wifeMotherOccupation),record2.getFieldValue(FieldType.wifeMotherOccupation));
        assertEquals("WifeMotherComment",       "73y, Vivant, wifeMotherResidence, wifeMotherComment",record2.getFieldValue(FieldType.wifeMotherComment));

        assertEquals("Witness1 " + " FirstName" , record.getFieldValue(FieldType.witness1FirstName),record2.getFieldValue(FieldType.witness1FirstName));
        assertEquals("Witness1 " + " LastName" ,  record.getFieldValue(FieldType.witness1LastName), record2.getFieldValue(FieldType.witness1LastName));
        assertEquals("Witness1 " + " Occupation", "",record2.getFieldValue(FieldType.witness1Occupation));
        assertEquals("Witness1 " + " Comment",    record.getFieldValue(FieldType.witness1Comment)+ ", " + record.getFieldValue(FieldType.witness1Occupation)
                     , record2.getFieldValue(FieldType.witness1Comment));

        assertEquals("Witness2 " + " FirstName" , record.getFieldValue(FieldType.witness2FirstName),record2.getFieldValue(FieldType.witness2FirstName));
        assertEquals("Witness2 " + " LastName" ,  record.getFieldValue(FieldType.witness2LastName),record2.getFieldValue(FieldType.witness2LastName));
        assertEquals("Witness2 " + " Occupation" , "",record2.getFieldValue(FieldType.witness2Occupation));
        assertEquals("Witness2 " + " Comment",    record.getFieldValue(FieldType.witness2Comment)+ ", " + record.getFieldValue(FieldType.witness2Occupation)
                     , record2.getFieldValue(FieldType.witness2Comment));

        assertEquals("Witness3 " + " FirstName" ,  record.getFieldValue(FieldType.witness3FirstName) , record2.getFieldValue(FieldType.witness3FirstName));
        assertEquals("Witness3 " + " LastName" ,   record.getFieldValue(FieldType.witness3LastName),  record2.getFieldValue(FieldType.witness3LastName));
        assertEquals("Witness3 " + " Occupation",  "",record2.getFieldValue(FieldType.witness3Occupation));
        assertEquals("Witness3 "+ " Comment",      record.getFieldValue(FieldType.witness3Comment)+ ", " + record.getFieldValue(FieldType.witness3Occupation)
                     , record2.getFieldValue(FieldType.witness3Comment));

        assertEquals("Witness4 " + " FirstName" ,  record.getFieldValue(FieldType.witness4FirstName) , record2.getFieldValue(FieldType.witness4FirstName));
        assertEquals("Witness4 " + " LastName" ,   record.getFieldValue(FieldType.witness4LastName),  record2.getFieldValue(FieldType.witness4LastName));
        assertEquals("Witness4 " + " Occupation",  "",record2.getFieldValue(FieldType.witness4Occupation));
        assertEquals("Witness4 "+ " Comment",      record.getFieldValue(FieldType.witness4Comment)+ ", " + record.getFieldValue(FieldType.witness4Occupation)
                     , record2.getFieldValue(FieldType.witness4Comment));

        file.delete();
    }
}
