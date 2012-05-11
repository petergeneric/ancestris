package ancestris.modules.releve.file;

import ancestris.modules.releve.ConfigPanel;
import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import ancestris.modules.releve.model.RecordMisc;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;

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
    public void testIsValidFile() {
    }

    /**
     * Test of loadFile method, of class ReleveFileNimegue.
     */
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
    public void testSaveFileBirth() throws Exception {
        File file = new File("testsaveFile.txt");

        ConfigPanel configPanel = new ConfigPanel();
        String place = "cityname,citycode,county,state,country";
        configPanel.setPlace(place);

        DataManager dateManager = new DataManager(configPanel);

        RecordBirth record = new RecordBirth();
        record.setEventDate("11/11/2000");
        record.setCote("cote");
        record.setGeneralComment("generalcomment");
        record.setFreeComment("photo");
        record.setIndi("indifirstname", "indilastname", "M", "indiage", "01/01/1990", "indiplace", "indioccupation", "indicomment");
        //record.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indimarriedcomment", "Décédé");
        record.setIndiFather("indifatherfirstname", "indifatherlastname", "indifatheroccupation", "indifathercomment", "Décédé");
        record.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indimothercomment", "Décédé");
        record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

        dateManager.addRecord(record);
        StringBuilder sb = ReleveFileNimegue.saveFile(dateManager, dateManager.getReleveBirthModel(), file, false);
        assertEquals("verify save error", "", sb.toString());

        FileBuffer fb = ReleveFileNimegue.loadFile(file);
        assertEquals("load result", "", fb.getError().toString());
        assertEquals("load count", 1, fb.getBirthCount());
        RecordBirth record2 = (RecordBirth) fb.getRecords().get(0);

        // je compare tous les champs

        assertEquals("EventDate",       record.getEventDateField().toString(),record2.getEventDateField().toString());
        assertEquals("Cote",            record.getCote().toString(),record2.getCote().toString());
        assertEquals("parish",          "",record2.getParish().toString());
        assertEquals("Notary",          null,record2.getNotary());
        assertEquals("EventType",       null,record2.getEventType());
        assertEquals("FreeComment",    record.getFreeComment().toString(),record2.getFreeComment().toString());

        assertEquals("IndiFirstName",  record.getIndiFirstName().toString(),record2.getIndiFirstName().toString());
        assertEquals("IndiLastName",   record.getIndiLastName().toString(),record2.getIndiLastName().toString());
        assertEquals("IndiSex",        record.getIndiSex().toString(),record2.getIndiSex().toString());
        assertEquals("IndiAge",        null,record2.getIndiAge());
        assertEquals("IndiBirthDate",  "",record2.getIndiBirthDate().toString());
        assertEquals("IndiPlace",      null,record2.getIndiPlace());
        assertEquals("IndiOccupation", null,record2.getIndiOccupation());
        assertEquals("IndiComment",    record2.getIndiComment().toString(),record2.getIndiComment().toString());
        assertEquals("IndiMarriedFirstName",    null,record2.getIndiMarriedFirstName());
        assertEquals("IndiMarriedLastName",     null,record2.getIndiMarriedLastName());
        assertEquals("IndiMarriedOccupation",   null,record2.getIndiMarriedOccupation());
        assertEquals("IndiMarriedComment",      null,record2.getIndiMarriedComment());
        assertEquals("IndiMarriedDead",         null,record2.getIndiMarriedDead());

        assertEquals("IndiFatherFirstName",     record.getIndiFatherFirstName().toString(),record2.getIndiFatherFirstName().toString());
        assertEquals("IndiFatherLastName",      record.getIndiFatherLastName().toString(),record2.getIndiFatherLastName().toString());
        assertEquals("IndiFatherOccupation",    record.getIndiFatherOccupation().toString(),record2.getIndiFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "Décédé, indifathercomment",record2.getIndiFatherComment().toString());
        assertEquals("IndiFatherDead",          "",record2.getIndiFatherDead().toString());
        assertEquals("IndiMotherFirstName",     record.getIndiMotherFirstName().toString(),record2.getIndiMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      record.getIndiMotherLastName().toString(),record2.getIndiMotherLastName().toString());
        assertEquals("IndiMotherOccupation",    record.getIndiMotherOccupation().toString(),record2.getIndiMotherOccupation().toString());
        assertEquals("IndiMotherComment",       "Décédé, indimothercomment",record2.getIndiMotherComment().toString());
        assertEquals("IndiMotherDead",          "",record2.getIndiMotherDead().toString());

        assertEquals("WifeFirstName",           null,record2.getWifeFirstName());
        assertEquals("WifeLastName",            null,record2.getWifeLastName());
        assertEquals("WifeSex",                 null,record2.getWifeSex());
        assertEquals("WifeAge",                 null,record2.getWifeAge());
        assertEquals("WifeBirthDate",           null,record2.getWifeBirthDate());
        assertEquals("WifePlace",               null,record2.getWifePlace());
        assertEquals("WifeOccupation",          null,record2.getWifeOccupation());
        assertEquals("WifeComment",             null,record2.getWifeComment());
        assertEquals("WifeMarriedFirstName",    null,record2.getWifeMarriedFirstName());
        assertEquals("WifeMarriedLastName",     null,record2.getWifeMarriedLastName());
        assertEquals("WifeMarriedOccupation",   null,record2.getWifeMarriedOccupation());
        assertEquals("WifeMarriedComment",      null,record2.getWifeMarriedComment());
        assertEquals("WifeMarriedDead",         null,record2.getWifeMarriedDead());
        assertEquals("WifeFatherFirstName",     null,record2.getWifeFatherFirstName());
        assertEquals("WifeFatherLastName",      null,record2.getWifeFatherLastName());
        assertEquals("WifeFatherOccupation",    null,record2.getWifeFatherOccupation());
        assertEquals("WifeFatherComment",       null,record2.getWifeFatherComment());
        assertEquals("WifeFatherDead",          null,record2.getWifeFatherDead());
        assertEquals("WifeMotherFirstName",     null,record2.getWifeMotherFirstName());
        assertEquals("WifeMotherLastName",      null,record2.getWifeMotherLastName());
        assertEquals("WifeMotherOccupation",    null,record2.getWifeMotherOccupation());
        assertEquals("WifeMotherComment",       null,record2.getWifeMotherComment());
        assertEquals("WifeMotherDead",          null,record2.getWifeMotherDead());

        assertEquals("Witness1FirstName",     record.getWitness1FirstName().toString(),record2.getWitness1FirstName().toString());
        assertEquals("Witness1LastName",      record.getWitness1LastName().toString(),record2.getWitness1LastName().toString());
        assertEquals("Witness1Occupation",    "",record2.getWitness1Occupation().toString());
        assertEquals("Witness1Comment",       "w1comment, w1occupation",record2.getWitness1Comment().toString());
        assertEquals("Witness2FirstName",     record.getWitness2FirstName().toString(),record2.getWitness2FirstName().toString());
        assertEquals("Witness2LastName",      record.getWitness2LastName().toString(),record2.getWitness2LastName().toString());
        assertEquals("Witness2Occupation",    "",record2.getWitness2Occupation().toString());
        assertEquals("Witness2Comment",       "w2comment, w2occupation",record2.getWitness2Comment().toString());
        assertEquals("Witness3FirstName",     "",record2.getWitness3FirstName().toString());
        assertEquals("Witness3LastName",      "",record2.getWitness3LastName().toString());
        assertEquals("Witness3Occupation",    "",record2.getWitness3Occupation().toString());
        assertEquals("Witness3Comment",       "",record2.getWitness3Comment().toString());
        assertEquals("Witness4FirstName",     "",record2.getWitness4FirstName().toString());
        assertEquals("Witness4LastName",      "",record2.getWitness4LastName().toString());
        assertEquals("Witness4Occupation",    "",record2.getWitness4Occupation().toString());
        assertEquals("Witness4Comment",       "",record2.getWitness4Comment().toString());

        assertEquals("GeneralComment", "generalcomment, témoin: w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment",record2.getGeneralComment().toString());

        file.delete();

    }


    /**
     * Test de l'enregistrement d'un mariage
     */
    public void testSaveFileMarriage() throws Exception {
        File file = new File("testsaveFile.txt");

        ConfigPanel configPanel = new ConfigPanel();
        String place = "cityname,citycode,county,state,country";
        configPanel.setPlace(place);

        DataManager dateManager = new DataManager(configPanel);

        RecordMarriage record = new RecordMarriage();
        record.setEventDate("29/02/2012");
        record.setCote("cote");
        record.setParish("parish");
        record.setGeneralComment("generalcomment");
        record.setFreeComment("photo");
        record.setIndi("indifirstname", "indilastname", "M", "indiage", "01/01/1990", "indiplace", "indioccupation", "indicomment");
        record.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indimarriedcomment", "Décédé");
        record.setIndiFather("indifathername", "indifatherlastname", "indifatheroccupation", "indifathercomment", "Décédé");
        record.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indimothercomment", "Décédé");
        record.setWife("wifefirstname", "wifelastname", "F", "wifeage", "02/02/1992", "wifeplace", "wifeoccupation", "wifecomment");
        record.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifemarriedcomment", "Décédé");
        record.setWifeFather("wifefathername", "wifefatherlastname", "wifefatheroccupation", "wifefathercomment", "Décédé");
        record.setWifeMother("wifemothername", "wifemotherlastname", "wifemotheroccupation", "wifemothercomment", "Décédé");
        record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
        dateManager.addRecord(record);
        StringBuilder sb = ReleveFileNimegue.saveFile(dateManager, dateManager.getReleveMarriageModel(), file, false);
        assertEquals("save result", "", sb.toString());

        FileBuffer fb = ReleveFileNimegue.loadFile(file);
        assertEquals("load result", "", fb.getError().toString());
        assertEquals("load count", 1, fb.getMarriageCount());
        RecordMarriage record2 = (RecordMarriage) fb.getRecords().get(0);

        // je compare tous les champs

        assertEquals("EventDate",      record.getEventDateField().toString(),record2.getEventDateField().toString());
        assertEquals("Cote",           record.getCote().toString(),record2.getCote().toString());
        assertEquals("parish",         "",record2.getParish().toString());
        assertEquals("Notary",         null,record2.getNotary());
        assertEquals("EventType",      null,record2.getEventType());
        assertEquals("FreeComment",    record.getFreeComment().toString(),record2.getFreeComment().toString());

        assertEquals("IndiFirstName",  record.getIndiFirstName().toString(),record2.getIndiFirstName().toString());
        assertEquals("IndiLastName",   record.getIndiLastName().toString(),record2.getIndiLastName().toString());
        assertEquals("IndiSex",        record.getIndiSex().toString(),record2.getIndiSex().toString());
        assertEquals("IndiAge",        record.getIndiAge().toString(),record2.getIndiAge().toString());
        assertEquals("IndiBirthDate",  record.getIndiBirthDate().toString(),record2.getIndiBirthDate().toString());
        assertEquals("IndiPlace",      record.getIndiPlace().toString(),record2.getIndiPlace().toString());
        assertEquals("IndiOccupation", record.getIndiOccupation().toString(),record2.getIndiOccupation().toString());
        assertEquals("IndiComment",    record2.getIndiComment().toString(),record2.getIndiComment().toString());
        assertEquals("IndiMarriedFirstName",    record.getIndiMarriedFirstName().toString(),record2.getIndiMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     record.getIndiMarriedLastName().toString(),record2.getIndiMarriedLastName().toString());
        assertEquals("IndiMarriedOccupation",   "",record2.getIndiMarriedOccupation().toString());
        assertEquals("IndiMarriedComment",      "Décédé, indimarriedoccupation, indimarriedcomment",record2.getIndiMarriedComment().toString());
        assertEquals("IndiMarriedDead",         "",record2.getIndiMarriedDead().toString());

        assertEquals("IndiFatherFirstName",     record.getIndiFatherFirstName().toString(),record2.getIndiFatherFirstName().toString());
        assertEquals("IndiFatherLastName",      record.getIndiFatherLastName().toString(),record2.getIndiFatherLastName().toString());
        assertEquals("IndiFatherOccupation",    record.getIndiFatherOccupation().toString(),record2.getIndiFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "Décédé, indifathercomment",record2.getIndiFatherComment().toString());
        assertEquals("IndiFatherDead",          "",record2.getIndiFatherDead().toString());
        assertEquals("IndiMotherFirstName",     record.getIndiMotherFirstName().toString(),record2.getIndiMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      record.getIndiMotherLastName().toString(),record2.getIndiMotherLastName().toString());
        assertEquals("IndiMotherOccupation",    record.getIndiMotherOccupation().toString(),record2.getIndiMotherOccupation().toString());
        assertEquals("IndiMotherComment",       "Décédé, indimothercomment",record2.getIndiMotherComment().toString());
        assertEquals("IndiMotherDead",          "",record2.getIndiMotherDead().toString());

        assertEquals("WifeFirstName",           record.getWifeFirstName().toString(),record2.getWifeFirstName().toString());
        assertEquals("WifeLastName",            record.getWifeLastName().toString(),record2.getWifeLastName().toString());
        assertEquals("WifeSex",                 record.getWifeSex().toString(),record2.getWifeSex().toString());
        assertEquals("WifeAge",                 record.getWifeAge().toString(),record2.getWifeAge().toString());
        assertEquals("WifeBirthDate",           record.getWifeBirthDate().toString(),record2.getWifeBirthDate().toString());
        assertEquals("WifePlace",               record.getWifePlace().toString(),record2.getWifePlace().toString());
        assertEquals("WifeOccupation",          record.getWifeOccupation().toString(),record2.getWifeOccupation().toString());
        assertEquals("WifeComment",             record2.getWifeComment().toString(),record2.getWifeComment().toString());
        assertEquals("WifeMarriedFirstName",    record.getWifeMarriedFirstName().toString(),record2.getWifeMarriedFirstName().toString());
        assertEquals("WifeMarriedLastName",     record.getWifeMarriedLastName().toString(),record2.getWifeMarriedLastName().toString());
        assertEquals("WifeMarriedOccupation",   "",record2.getWifeMarriedOccupation().toString());
        assertEquals("WifeMarriedComment",      "Décédé, wifemarriedoccupation, wifemarriedcomment",record2.getWifeMarriedComment().toString());
        assertEquals("WifeMarriedDead",         "",record2.getWifeMarriedDead().toString());

        assertEquals("WifeFatherFirstName",     record.getWifeFatherFirstName().toString(),record2.getWifeFatherFirstName().toString());
        assertEquals("WifeFatherLastName",      record.getWifeFatherLastName().toString(),record2.getWifeFatherLastName().toString());
        assertEquals("WifeFatherOccupation",    record.getWifeFatherOccupation().toString(),record2.getWifeFatherOccupation().toString());
        assertEquals("WifeFatherComment",       "Décédé, wifefathercomment",record2.getWifeFatherComment().toString());
        assertEquals("WifeFatherDead",          "",record2.getWifeFatherDead().toString());
        assertEquals("WifeMotherFirstName",     record.getWifeMotherFirstName().toString(),record2.getWifeMotherFirstName().toString());
        assertEquals("WifeMotherLastName",      record.getWifeMotherLastName().toString(),record2.getWifeMotherLastName().toString());
        assertEquals("WifeMotherOccupation",    record.getWifeMotherOccupation().toString(),record2.getWifeMotherOccupation().toString());
        assertEquals("WifeMotherComment",       "Décédé, wifemothercomment",record2.getWifeMotherComment().toString());
        assertEquals("WifeMotherDead",          "",record2.getWifeMotherDead().toString());

        assertEquals("Witness1FirstName",     record.getWitness1FirstName().toString(),record2.getWitness1FirstName().toString());
        assertEquals("Witness1LastName",      record.getWitness1LastName().toString(),record2.getWitness1LastName().toString());
        assertEquals("Witness1Occupation",    "",record2.getWitness1Occupation().toString());
        assertEquals("Witness1Comment",       "w1comment, w1occupation",record2.getWitness1Comment().toString());
        assertEquals("Witness2FirstName",     record.getWitness2FirstName().toString(),record2.getWitness2FirstName().toString());
        assertEquals("Witness2LastName",      record.getWitness2LastName().toString(),record2.getWitness2LastName().toString());
        assertEquals("Witness2Occupation",    "",record2.getWitness2Occupation().toString());
        assertEquals("Witness2Comment",       "w2comment, w2occupation",record2.getWitness2Comment().toString());
        assertEquals("Witness3FirstName",     record.getWitness3FirstName().toString(),record2.getWitness3FirstName().toString());
        assertEquals("Witness3LastName",      record.getWitness3LastName().toString(),record2.getWitness3LastName().toString());
        assertEquals("Witness3Occupation",    "",record2.getWitness3Occupation().toString());
        assertEquals("Witness3Comment",       "w3comment, w3occupation",record2.getWitness3Comment().toString());
        assertEquals("Witness4FirstName",     record.getWitness4FirstName().toString(),record2.getWitness4FirstName().toString());
        assertEquals("Witness4LastName",      record.getWitness4LastName().toString(),record2.getWitness4LastName().toString());
        assertEquals("Witness4Occupation",    "",record2.getWitness4Occupation().toString());
        assertEquals("Witness4Comment",       "w4comment, w4occupation",record2.getWitness4Comment().toString());

        assertEquals("GeneralComment", "generalcomment",record2.getGeneralComment().toString());

        file.delete();
    }

    /**
     * Test de l'enregistrement d'un deces
     */
    public void testSaveFileDeath() throws Exception {
        File file = new File("testsaveFile.txt");

        ConfigPanel configPanel = new ConfigPanel();
        String place = "cityname,citycode,county,state,country";
        configPanel.setPlace(place);

        DataManager dateManager = new DataManager(configPanel);

        RecordDeath record = new RecordDeath();
        record.setEventDate("11/11/2000");
        record.setCote("cote");
        record.setGeneralComment("generalcomment");
        record.setFreeComment("photo");
        record.setIndi("indifirstname", "indilastname", "M", "indiage", "01/01/1990", "indiplace", "indioccupation", "indicomment");
        record.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indimarriedcomment", "Décédé");
        record.setIndiFather("indifatherfirstname", "indifatherlastname", "indifatheroccupation", "indifathercomment", "Décédé");
        record.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indimothercomment", "Décédé");
        record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

        dateManager.addRecord(record);
        StringBuilder sb = ReleveFileNimegue.saveFile(dateManager, dateManager.getReleveDeathModel(), file, false);
        assertEquals("verify save error", "", sb.toString());

        FileBuffer fb = ReleveFileNimegue.loadFile(file);
        assertEquals("load result", "", fb.getError().toString());
        assertEquals("load count", 1, fb.getDeathCount());
        RecordDeath record2 = (RecordDeath) fb.getRecords().get(0);

        // je compare tous les champs

        assertEquals("EventDate",       record.getEventDateField().toString(),record2.getEventDateField().toString());
        assertEquals("Cote",            record.getCote().toString(),record2.getCote().toString());
        assertEquals("parish",          "",record2.getParish().toString());
        assertEquals("Notary",          null,record2.getNotary());
        assertEquals("EventType",       null,record2.getEventType());
        assertEquals("FreeComment",    record.getFreeComment().toString(),record2.getFreeComment().toString());

        assertEquals("IndiFirstName",  record.getIndiFirstName().toString(),record2.getIndiFirstName().toString());
        assertEquals("IndiLastName",   record.getIndiLastName().toString(),record2.getIndiLastName().toString());
        assertEquals("IndiSex",        record.getIndiSex().toString(),record2.getIndiSex().toString());
        assertEquals("IndiAge",        record.getIndiAge().toString(),record2.getIndiAge().toString());
        assertEquals("IndiBirthDate",  record.getIndiBirthDate().toString(),record2.getIndiBirthDate().toString());
        assertEquals("IndiPlace",      record.getIndiPlace().toString(),record2.getIndiPlace().toString());
        assertEquals("IndiOccupation", record.getIndiOccupation().toString(),record2.getIndiOccupation().toString());
        assertEquals("IndiComment",    record2.getIndiComment().toString(),record2.getIndiComment().toString());
        assertEquals("IndiMarriedFirstName",    record.getIndiMarriedFirstName().toString(),record2.getIndiMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     record.getIndiMarriedLastName().toString(),record2.getIndiMarriedLastName().toString());
        assertEquals("IndiMarriedOccupation",   record.getIndiMarriedOccupation().toString(),record2.getIndiMarriedOccupation().toString());
        assertEquals("IndiMarriedComment",      "Décédé, indimarriedcomment",record2.getIndiMarriedComment().toString());
        assertEquals("IndiMarriedDead",         "",record2.getIndiMarriedDead().toString());

        assertEquals("IndiFatherFirstName",     record.getIndiFatherFirstName().toString(),record2.getIndiFatherFirstName().toString());
        assertEquals("IndiFatherLastName",      record.getIndiFatherLastName().toString(),record2.getIndiFatherLastName().toString());
        assertEquals("IndiFatherOccupation",    record.getIndiFatherOccupation().toString(),record2.getIndiFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "Décédé, indifathercomment",record2.getIndiFatherComment().toString());
        assertEquals("IndiFatherDead",          "",record2.getIndiFatherDead().toString());
        assertEquals("IndiMotherFirstName",     record.getIndiMotherFirstName().toString(),record2.getIndiMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      record.getIndiMotherLastName().toString(),record2.getIndiMotherLastName().toString());
        assertEquals("IndiMotherOccupation",    record.getIndiMotherOccupation().toString(),record2.getIndiMotherOccupation().toString());
        assertEquals("IndiMotherComment",       "Décédé, indimothercomment",record2.getIndiMotherComment().toString());
        assertEquals("IndiMotherDead",          "",record2.getIndiMotherDead().toString());

        assertEquals("WifeFirstName",           null,record2.getWifeFirstName());
        assertEquals("WifeLastName",            null,record2.getWifeLastName());
        assertEquals("WifeSex",                 null,record2.getWifeSex());
        assertEquals("WifeAge",                 null,record2.getWifeAge());
        assertEquals("WifeBirthDate",           null,record2.getWifeBirthDate());
        assertEquals("WifePlace",               null,record2.getWifePlace());
        assertEquals("WifeOccupation",          null,record2.getWifeOccupation());
        assertEquals("WifeComment",             null,record2.getWifeComment());
        assertEquals("WifeMarriedFirstName",    null,record2.getWifeMarriedFirstName());
        assertEquals("WifeMarriedLastName",     null,record2.getWifeMarriedLastName());
        assertEquals("WifeMarriedOccupation",   null,record2.getWifeMarriedOccupation());
        assertEquals("WifeMarriedComment",      null,record2.getWifeMarriedComment());
        assertEquals("WifeMarriedDead",         null,record2.getWifeMarriedDead());
        assertEquals("WifeFatherFirstName",     null,record2.getWifeFatherFirstName());
        assertEquals("WifeFatherLastName",      null,record2.getWifeFatherLastName());
        assertEquals("WifeFatherOccupation",    null,record2.getWifeFatherOccupation());
        assertEquals("WifeFatherComment",       null,record2.getWifeFatherComment());
        assertEquals("WifeFatherDead",          null,record2.getWifeFatherDead());
        assertEquals("WifeMotherFirstName",     null,record2.getWifeMotherFirstName());
        assertEquals("WifeMotherLastName",      null,record2.getWifeMotherLastName());
        assertEquals("WifeMotherOccupation",    null,record2.getWifeMotherOccupation());
        assertEquals("WifeMotherComment",       null,record2.getWifeMotherComment());
        assertEquals("WifeMotherDead",          null,record2.getWifeMotherDead());

        assertEquals("Witness1FirstName",     record.getWitness1FirstName().toString(),record2.getWitness1FirstName().toString());
        assertEquals("Witness1LastName",      record.getWitness1LastName().toString(),record2.getWitness1LastName().toString());
        assertEquals("Witness1Occupation",    "",record2.getWitness1Occupation().toString());
        assertEquals("Witness1Comment",       "w1comment, w1occupation",record2.getWitness1Comment().toString());
        assertEquals("Witness2FirstName",     record.getWitness2FirstName().toString(),record2.getWitness2FirstName().toString());
        assertEquals("Witness2LastName",      record.getWitness2LastName().toString(),record2.getWitness2LastName().toString());
        assertEquals("Witness2Occupation",    "",record2.getWitness2Occupation().toString());
        assertEquals("Witness2Comment",       "w2comment, w2occupation",record2.getWitness2Comment().toString());
        assertEquals("Witness3FirstName",     "",record2.getWitness3FirstName().toString());
        assertEquals("Witness3LastName",      "",record2.getWitness3LastName().toString());
        assertEquals("Witness3Occupation",    "",record2.getWitness3Occupation().toString());
        assertEquals("Witness3Comment",       "",record2.getWitness3Comment().toString());
        assertEquals("Witness4FirstName",     "",record2.getWitness4FirstName().toString());
        assertEquals("Witness4LastName",      "",record2.getWitness4LastName().toString());
        assertEquals("Witness4Occupation",    "",record2.getWitness4Occupation().toString());
        assertEquals("Witness4Comment",       "",record2.getWitness4Comment().toString());

        assertEquals("GeneralComment", "generalcomment, témoin: w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment",record2.getGeneralComment().toString());

        file.delete();

    }

    /**
     * Test de l'enregistrement d'un divers
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
        record.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indimarriedcomment", "Décédé");
        record.setIndiFather("indifathername", "indifatherlastname", "indifatheroccupation", "indifathercomment", "Décédé");
        record.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indimothercomment", "Décédé");
        record.setWife("wifefirstname", "wifelastname", "F", "wifeage", "02/02/1992", "wifeplace", "wifeoccupation", "wifecomment");
        record.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifemarriedcomment", "Décédé");
        record.setWifeFather("wifefathername", "wifefatherlastname", "wifefatheroccupation", "wifefathercomment", "Décédé");
        record.setWifeMother("wifemothername", "wifemotherlastname", "wifemotheroccupation", "wifemothercomment", "Décédé");
        record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
        dateManager.addRecord(record);
        StringBuilder sb = ReleveFileNimegue.saveFile(dateManager, dateManager.getReleveMiscModel(), file, false);
        assertEquals("save result", "", sb.toString());

        FileBuffer fb = ReleveFileNimegue.loadFile(file);
        assertEquals("load result", "", fb.getError().toString());
        assertEquals("load count", 1, fb.getMiscCount());
        RecordMisc record2 = (RecordMisc) fb.getRecords().get(0);

        // je compare tous les champs

        assertEquals("EventDate",      record.getEventDateField().toString(),record2.getEventDateField().toString());
        assertEquals("Cote",           record.getCote().toString(),record2.getCote().toString());
        assertEquals("parish",         "",record2.getParish().toString());
        assertEquals("Notary",         "",record2.getNotary().toString());
        assertEquals("EventType",      record.getEventType().toString(),record2.getEventType().toString());
        assertEquals("FreeComment",    record.getFreeComment().toString(),record2.getFreeComment().toString());

        assertEquals("IndiFirstName",  record.getIndiFirstName().toString(),record2.getIndiFirstName().toString());
        assertEquals("IndiLastName",   record.getIndiLastName().toString(),record2.getIndiLastName().toString());
        assertEquals("IndiSex",        record.getIndiSex().toString(),record2.getIndiSex().toString());
        assertEquals("IndiAge",        record.getIndiAge().toString(),record2.getIndiAge().toString());
        assertEquals("IndiBirthDate",  record.getIndiBirthDate().toString(),record2.getIndiBirthDate().toString());
        assertEquals("IndiPlace",      record.getIndiPlace().toString(),record2.getIndiPlace().toString());
        assertEquals("IndiOccupation", record.getIndiOccupation().toString(),record2.getIndiOccupation().toString());
        assertEquals("IndiComment",    record2.getIndiComment().toString(),record2.getIndiComment().toString());
        assertEquals("IndiMarriedFirstName",    record.getIndiMarriedFirstName().toString(),record2.getIndiMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     record.getIndiMarriedLastName().toString(),record2.getIndiMarriedLastName().toString());
        assertEquals("IndiMarriedOccupation",   "",record2.getIndiMarriedOccupation().toString());
        assertEquals("IndiMarriedComment",      "Décédé, indimarriedoccupation, indimarriedcomment",record2.getIndiMarriedComment().toString());
        assertEquals("IndiMarriedDead",         "",record2.getIndiMarriedDead().toString());

        assertEquals("IndiFatherFirstName",     record.getIndiFatherFirstName().toString(),record2.getIndiFatherFirstName().toString());
        assertEquals("IndiFatherLastName",      record.getIndiFatherLastName().toString(),record2.getIndiFatherLastName().toString());
        assertEquals("IndiFatherOccupation",    record.getIndiFatherOccupation().toString(),record2.getIndiFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "Décédé, indifathercomment",record2.getIndiFatherComment().toString());
        assertEquals("IndiFatherDead",          "",record2.getIndiFatherDead().toString());
        assertEquals("IndiMotherFirstName",     record.getIndiMotherFirstName().toString(),record2.getIndiMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      record.getIndiMotherLastName().toString(),record2.getIndiMotherLastName().toString());
        assertEquals("IndiMotherOccupation",    record.getIndiMotherOccupation().toString(),record2.getIndiMotherOccupation().toString());
        assertEquals("IndiMotherComment",       "Décédé, indimothercomment",record2.getIndiMotherComment().toString());
        assertEquals("IndiMotherDead",          "",record2.getIndiMotherDead().toString());

        assertEquals("WifeFirstName",           record.getWifeFirstName().toString(),record2.getWifeFirstName().toString());
        assertEquals("WifeLastName",            record.getWifeLastName().toString(),record2.getWifeLastName().toString());
        assertEquals("WifeSex",                 record.getWifeSex().toString(),record2.getWifeSex().toString());
        assertEquals("WifeAge",                 record.getWifeAge().toString(),record2.getWifeAge().toString());
        assertEquals("WifeBirthDate",           record.getWifeBirthDate().toString(),record2.getWifeBirthDate().toString());
        assertEquals("WifePlace",               record.getWifePlace().toString(),record2.getWifePlace().toString());
        assertEquals("WifeOccupation",          record.getWifeOccupation().toString(),record2.getWifeOccupation().toString());
        assertEquals("WifeComment",             record2.getWifeComment().toString(),record2.getWifeComment().toString());
        assertEquals("WifeMarriedFirstName",    record.getWifeMarriedFirstName().toString(),record2.getWifeMarriedFirstName().toString());
        assertEquals("WifeMarriedLastName",     record.getWifeMarriedLastName().toString(),record2.getWifeMarriedLastName().toString());
        assertEquals("WifeMarriedOccupation",   "",record2.getWifeMarriedOccupation().toString());
        assertEquals("WifeMarriedComment",      "Décédé, wifemarriedoccupation, wifemarriedcomment",record2.getWifeMarriedComment().toString());
        assertEquals("WifeMarriedDead",         "",record2.getWifeMarriedDead().toString());

        assertEquals("WifeFatherFirstName",     record.getWifeFatherFirstName().toString(),record2.getWifeFatherFirstName().toString());
        assertEquals("WifeFatherLastName",      record.getWifeFatherLastName().toString(),record2.getWifeFatherLastName().toString());
        assertEquals("WifeFatherOccupation",    record.getWifeFatherOccupation().toString(),record2.getWifeFatherOccupation().toString());
        assertEquals("WifeFatherComment",       "Décédé, wifefathercomment",record2.getWifeFatherComment().toString());
        assertEquals("WifeFatherDead",          "",record2.getWifeFatherDead().toString());
        assertEquals("WifeMotherFirstName",     record.getWifeMotherFirstName().toString(),record2.getWifeMotherFirstName().toString());
        assertEquals("WifeMotherLastName",      record.getWifeMotherLastName().toString(),record2.getWifeMotherLastName().toString());
        assertEquals("WifeMotherOccupation",    record.getWifeMotherOccupation().toString(),record2.getWifeMotherOccupation().toString());
        assertEquals("WifeMotherComment",       "Décédé, wifemothercomment",record2.getWifeMotherComment().toString());
        assertEquals("WifeMotherDead",          "",record2.getWifeMotherDead().toString());
        
        assertEquals("Witness1FirstName",     record.getWitness1FirstName().toString(),record2.getWitness1FirstName().toString());
        assertEquals("Witness1LastName",      record.getWitness1LastName().toString(),record2.getWitness1LastName().toString());
        assertEquals("Witness1Occupation",    "",record2.getWitness1Occupation().toString());
        assertEquals("Witness1Comment",       "w1comment, w1occupation",record2.getWitness1Comment().toString());
        assertEquals("Witness2FirstName",     record.getWitness2FirstName().toString(),record2.getWitness2FirstName().toString());
        assertEquals("Witness2LastName",      record.getWitness2LastName().toString(),record2.getWitness2LastName().toString());
        assertEquals("Witness2Occupation",    "",record2.getWitness2Occupation().toString());
        assertEquals("Witness2Comment",       "w2comment, w2occupation",record2.getWitness2Comment().toString());
        assertEquals("Witness3FirstName",     record.getWitness3FirstName().toString(),record2.getWitness3FirstName().toString());
        assertEquals("Witness3LastName",      record.getWitness3LastName().toString(),record2.getWitness3LastName().toString());
        assertEquals("Witness3Occupation",    "",record2.getWitness3Occupation().toString());
        assertEquals("Witness3Comment",       "w3comment, w3occupation",record2.getWitness3Comment().toString());
        assertEquals("Witness4FirstName",     record.getWitness4FirstName().toString(),record2.getWitness4FirstName().toString());
        assertEquals("Witness4LastName",      record.getWitness4LastName().toString(),record2.getWitness4LastName().toString());
        assertEquals("Witness4Occupation",    "",record2.getWitness4Occupation().toString());
        assertEquals("Witness4Comment",       "w4comment, w4occupation",record2.getWitness4Comment().toString());

        assertEquals("GeneralComment", "generalcomment",record2.getGeneralComment().toString());

        file.delete();
    }
}
