package ancestris.modules.releve.file;

import ancestris.modules.releve.ConfigPanel;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Field.FieldType;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import ancestris.modules.releve.model.RecordMisc;
import java.io.File;
import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class ReleveFileEgmtTest extends TestCase {
    
     /**
     * Test of saveFile method, of class ReleveFileEgmt.
     */
    public void testSaveFileBirth() throws Exception {
        File file = new File("testsaveFile.txt");

        ConfigPanel configPanel = new ConfigPanel();
        DataManager dateManager = new DataManager(configPanel);

        RecordBirth birth = new RecordBirth();
        birth.setEventDate("11/11/2000");
        birth.setCote("cote");
        birth.setFreeComment("photo");
        birth.setIndi("firstname", "lastname", "M", "", "", "place", "occupation", "comment");
        birth.setIndiFather("indifathername", "indifatherlastname", "indifatheroccupation", "indifathercomment", "indifatherdead");
        birth.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indimothercomment", "indimotherdead");
        birth.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        birth.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        birth.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        birth.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
        birth.setGeneralComment("generalcomment");

        dateManager.addRecord(birth);
        StringBuilder sb = ReleveFileEgmt.saveFile(dateManager, dateManager.getReleveBirthModel(), file, false);
        assertEquals("verify save error", sb.length(), 0);

        FileBuffer fb = ReleveFileEgmt.loadFile(file);
        assertEquals("load result", "", fb.getError().toString());
        assertEquals("load count", 1, fb.getBirthCount());
        RecordBirth birth2 = (RecordBirth) fb.getRecords().get(0);

        // je compare tous les champs
        for (FieldType fieldType : FieldType.values()) {
            switch (fieldType) {
                case indiFatherLastName:
                    assertEquals(String.valueOf(fieldType.ordinal()),birth.getIndiLastName().toString(),birth2.getField(fieldType).toString());
                    break;
                case wifeFatherLastName:
                    assertNull(String.valueOf(fieldType.ordinal()),birth2.getField(fieldType));
                    break;
                case indiFatherOccupation:
                case indiMotherOccupation:
                    assertEquals(String.valueOf(fieldType.ordinal()), "",birth2.getField(fieldType).toString());
                    break;
                case indiFatherComment:
                    assertEquals(String.valueOf(fieldType.ordinal()), "indifathercomment, indifatheroccupation",birth2.getField(fieldType).toString());
                    break;
                case indiMotherComment:
                    assertEquals(String.valueOf(fieldType.ordinal()), "indimothercomment, indimotheroccupation",birth2.getField(fieldType).toString());
                    break;
                case witness1Occupation:
                case witness2Occupation:
                    assertEquals(String.valueOf(fieldType.ordinal()), "",birth2.getField(fieldType).toString());
                    break;
                case witness1Comment:
                    assertEquals(String.valueOf(fieldType.ordinal()), "w1comment, w1occupation",birth2.getField(fieldType).toString());
                    break;
                case witness2Comment:
                    assertEquals(String.valueOf(fieldType.ordinal()), "w2comment, w2occupation",birth2.getField(fieldType).toString());
                    break;
                case generalComment:
                    //assertEquals(String.valueOf(fieldType.ordinal()), "generalcomment ",birth2.getField(fieldType).toString());
                    assertEquals(String.valueOf(fieldType.ordinal()), "generalcomment, témoin: w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment ",birth2.getField(fieldType).toString());
                    break;
                case witness3FirstName:
                case witness3LastName:
                case witness3Occupation:
                case witness3Comment:
                case witness4FirstName:
                case witness4LastName:
                case witness4Occupation:
                case witness4Comment:
                    assertEquals(String.valueOf(fieldType.ordinal()), "",birth2.getField(fieldType).toString());
                    break;
                default:
                    // autres champs
                    if (birth.getField(fieldType) == null) {
                        assertNull(String.valueOf(fieldType.ordinal()), birth2.getField(fieldType));
                    } else {
                        assertNotNull(String.valueOf(fieldType.ordinal()), birth2.getField(fieldType));
                        assertEquals(String.valueOf(fieldType.ordinal()), birth.getField(fieldType).toString(), birth2.getField(fieldType).toString());
                    }
            }
        }

        file.delete();

    }

    /**
     * Test of saveFile method, of class ReleveFileEgmt.
     */
    public void testSaveFileMarriage() throws Exception {
        File file = new File("testsaveFile.txt");

        ConfigPanel configPanel = new ConfigPanel();
        DataManager dateManager = new DataManager(configPanel);

        RecordMarriage marriage = new RecordMarriage();
        marriage.setEventDate("11/01/2000");
        marriage.setCote("cote");
        marriage.setFreeComment("photo");
        marriage.setIndi("indifirstname", "indilastname", "M", "indiage", "01/01/1980", "indiplace", "indioccupation", "indicomment");
        marriage.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indimarriedcomment", "indimarrieddead");
        marriage.setIndiFather("indifathername", "indifatherlastname", "indifatheroccupation", "indifathercomment", "indifatherdead");
        marriage.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indimothercomment", "indimotherdead");
        marriage.setWife("wifefirstname", "wifelastname", "F", "wifeage", "02/02/1982", "wifeplace", "wifeoccupation", "wifecomment");
        marriage.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifemarriedcomment", "wifemarrieddead");
        marriage.setWifeFather("wifefathername", "wifefatherlastname", "wifefatheroccupation", "wifefathercomment", "wifefatherdead");
        marriage.setWifeMother("wifemothername", "wifemotherlastname", "wifemotheroccupation", "wifemothercomment", "wifemotherdead");
        marriage.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        marriage.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        marriage.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        marriage.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
        marriage.setGeneralComment("generalcomment");

        dateManager.addRecord(marriage);
        StringBuilder sb = ReleveFileEgmt.saveFile(dateManager, dateManager.getReleveMarriageModel(), file, false);
        assertEquals("verify save error", 0, sb.length());

        FileBuffer fb = ReleveFileEgmt.loadFile(file);
        assertEquals("load result", "", fb.getError().toString());
        assertEquals("load count", 1, fb.getBirthCount());
        RecordMarriage marriage2 = (RecordMarriage) fb.getRecords().get(0);

        // je compare tous les champs
        assertEquals("EventDate",   marriage.getEventDateField().toString(),marriage2.getEventDateField().toString());
        assertEquals("Cote",        marriage.getCote().toString(),marriage2.getCote().toString());
        assertEquals("parish",      marriage.getParish().toString(),marriage2.getParish().toString());
        assertEquals("EventDate",   marriage.getEventDateField().toString(),marriage2.getEventDateField().toString());
        assertNull("Notary",        marriage2.getNotary());
        assertNull("EventType",     marriage2.getEventType());
        assertEquals("FreeComment",    marriage.getFreeComment().toString(),marriage2.getFreeComment().toString());

        assertEquals("IndiFirstName",            marriage.getIndiFirstName().toString(),marriage2.getIndiFirstName().toString());
        assertEquals("IndiLastName",            marriage.getIndiLastName().toString(),marriage2.getIndiLastName().toString());
        assertEquals("IndiSex",                 marriage.getIndiSex().toString(),marriage2.getIndiSex().toString());
        assertEquals("IndiAge",                 marriage.getIndiAge().toString(),marriage2.getIndiAge().toString());
        assertEquals("IndiBirthDate",           "",marriage2.getIndiBirthDate().getValue());
        assertEquals("IndiPlace",               marriage.getIndiPlace().toString(),marriage2.getIndiPlace().toString());
        assertEquals("IndiOccupation",          "",marriage2.getIndiOccupation().toString());
        assertEquals("IndiComment",             "indicomment, indioccupation, né le 01/01/1980, conjoint: indimarriedlastnameindimarriedname indimarriedcomment, indimarriedoccupation, indimarriedcomment",marriage2.getIndiComment().toString());
        assertEquals("IndiMarriedFirstName",    "",marriage2.getIndiMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     "",marriage2.getIndiMarriedLastName().toString());
        assertEquals("IndiMarriedOccupation",   "",marriage2.getIndiMarriedOccupation().toString());
        assertEquals("IndiMarriedComment",      "".toString(),marriage2.getIndiMarriedComment().toString());
        assertEquals("IndiMarriedDead",         "",marriage2.getIndiMarriedDead().toString());
        assertEquals("IndiFatherFirstName",     marriage.getIndiFatherFirstName().toString(),marriage2.getIndiFatherFirstName().toString());
        assertEquals("IndiFatherLastName",      /*Father*/marriage.getIndiLastName().toString(),marriage2.getIndiFatherLastName().toString());
        assertEquals("IndiFatherOccupation",    "",marriage2.getIndiFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "indifathercomment, indifatheroccupation",marriage2.getIndiFatherComment().toString());
        assertEquals("IndiFatherDead",          marriage.getIndiFatherDead().toString(),marriage2.getIndiFatherDead().toString());
        assertEquals("IndiMotherFirstName",     marriage.getIndiMotherFirstName().toString(),marriage2.getIndiMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      marriage.getIndiMotherLastName().toString(),marriage2.getIndiMotherLastName().toString());
        assertEquals("IndiMotherOccupation",    "",marriage2.getIndiMotherOccupation().toString());
        assertEquals("IndiMotherComment",      "indimothercomment, indimotheroccupation",marriage2.getIndiMotherComment().toString());
        assertEquals("IndiMotherDead",          marriage.getIndiMotherDead().toString(),marriage2.getIndiMotherDead().toString());

        assertEquals("WifeFirstName",           marriage.getWifeFirstName().toString(),marriage2.getWifeFirstName().toString());
        assertEquals("WifeLastName",            marriage.getWifeLastName().toString(),marriage2.getWifeLastName().toString());
        assertEquals("WifeSex",                 marriage.getWifeSex().toString(),marriage2.getWifeSex().toString());
        assertEquals("WifeAge",                 marriage.getWifeAge().toString(),marriage2.getWifeAge().toString());
        assertEquals("WifeBirthDate",           "",marriage2.getWifeBirthDate().toString());
        assertEquals("WifePlace",               marriage.getWifePlace().toString(),marriage2.getWifePlace().toString());
        assertEquals("WifeOccupation",          "",marriage2.getWifeOccupation().toString());
        assertEquals("WifeComment",             "wifecomment, wifeoccupation, né le 02/02/1982, conjoint: wifemarriedlastnamewifemarriedname wifemarriedcomment, wifemarriedoccupation, wifemarriedcomment",marriage2.getWifeComment().toString());
        assertEquals("WifeMarriedFirstName",    "",marriage2.getWifeMarriedFirstName().toString());
        assertEquals("WifeMarriedLastName",     "",marriage2.getWifeMarriedLastName().toString());
        assertEquals("WifeMarriedOccupation",   "",marriage2.getWifeMarriedOccupation().toString());
        assertEquals("WifeMarriedComment",      "",marriage2.getWifeMarriedComment().toString());
        assertEquals("WifeMarriedDead",         marriage.getWifeMarriedDead().toString(),marriage2.getWifeMarriedDead().toString());
        assertEquals("WifeFatherFirstName",     marriage.getWifeFatherFirstName().toString(),marriage2.getWifeFatherFirstName().toString());
        assertEquals("WifeFatherLastName",      /*Father*/marriage.getWifeLastName().toString(),marriage2.getWifeFatherLastName().toString());
        assertEquals("WifeFatherOccupation",    "",marriage2.getWifeFatherOccupation().toString());
        assertEquals("WifeFatherComment",       "wifefathercomment, wifefatheroccupation",marriage2.getWifeFatherComment().toString());
        assertEquals("WifeFatherDead",          marriage.getWifeFatherDead().toString(),marriage2.getWifeFatherDead().toString());
        assertEquals("WifeMotherFirstName",     marriage.getWifeMotherFirstName().toString(),marriage2.getWifeMotherFirstName().toString());
        assertEquals("WifeMotherLastName",      marriage.getWifeMotherLastName().toString(),marriage2.getWifeMotherLastName().toString());
        assertEquals("WifeMotherOccupation",    "",marriage2.getWifeMotherOccupation().toString());
        assertEquals("WifeMotherComment",       "wifemothercomment, wifemotheroccupation",marriage2.getWifeMotherComment().toString());
        assertEquals("WifeMotherDead",          marriage.getWifeMotherDead().toString(),marriage2.getWifeMotherDead().toString());

        assertEquals("Witness1FirstName",     marriage.getWitness1FirstName().toString(),marriage2.getWitness1FirstName().toString());
        assertEquals("Witness1LastName",      marriage.getWitness1LastName().toString(),marriage2.getWitness1LastName().toString());
        assertEquals("Witness1Occupation",    "",marriage2.getWitness1Occupation().toString());
        assertEquals("Witness1Comment",       "w1comment, w1occupation",marriage2.getWitness1Comment().toString());
        assertEquals("Witness2FirstName",     marriage.getWitness2FirstName().toString(),marriage2.getWitness2FirstName().toString());
        assertEquals("Witness2LastName",      marriage.getWitness2LastName().toString(),marriage2.getWitness2LastName().toString());
        assertEquals("Witness2Occupation",    "",marriage2.getWitness2Occupation().toString());
        assertEquals("Witness2Comment",       "w2comment, w2occupation",marriage2.getWitness2Comment().toString());
        assertEquals("Witness3FirstName",     "",marriage2.getWitness3FirstName().toString());
        assertEquals("Witness3LastName",      "",marriage2.getWitness3LastName().toString());
        assertEquals("Witness3Occupation",    "",marriage2.getWitness3Occupation().toString());
        assertEquals("Witness3Comment",       "",marriage2.getWitness3Comment().toString());
        assertEquals("Witness4FirstName",     "",marriage2.getWitness4FirstName().toString());
        assertEquals("Witness4LastName",      "",marriage2.getWitness4LastName().toString());
        assertEquals("Witness4Occupation",    "",marriage2.getWitness4Occupation().toString());
        assertEquals("Witness4Comment",       "",marriage2.getWitness4Comment().toString());

        assertEquals("GeneralComment", "generalcomment, témoin: w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment ",marriage2.getGeneralComment().toString());
        
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

        RecordDeath death = new RecordDeath();
        death.setEventDate("11/11/2000");
        death.setCote("cote");
        death.setGeneralComment("generalcomment");
        death.setFreeComment("photo");
        death.setIndi("indifirstname", "indilastname", "M", "indiage", "01/01/1990", "indiplace", "indioccupation", "indicomment");
        death.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indimarriedcomment", "indimarrieddead");
        death.setIndiFather("indifatherfirstname", "indifatherlastname", "indifatheroccupation", "indifathercomment", "indifatherdead");
        death.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indimothercomment", "indimotherdead");
        death.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        death.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        death.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        death.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

        dateManager.addRecord(death);
        StringBuilder sb = ReleveFileEgmt.saveFile(dateManager, dateManager.getReleveDeathModel(), file, false);
        assertEquals("verify save error", "", sb.toString());

        FileBuffer fb = ReleveFileEgmt.loadFile(file);
        assertEquals("load result", "", fb.getError().toString());
        assertEquals("load count", 1, fb.getBirthCount());
        RecordDeath death2 = (RecordDeath) fb.getRecords().get(0);

        // je compare tous les champs

        assertEquals("EventDate",       death.getEventDateField().toString(),death2.getEventDateField().toString());
        assertEquals("Cote",            death.getCote().toString(),death2.getCote().toString());
        assertEquals("parish",          death.getParish().toString(),death2.getParish().toString());
        assertEquals("EventDate",       death.getEventDateField().toString(),death2.getEventDateField().toString());
        assertNull("Notary",            death2.getNotary());
        assertNull("EventType",         death2.getEventType());
        assertEquals("FreeComment",    death.getFreeComment().toString(),death2.getFreeComment().toString());

        assertEquals("IndiFirstName",  death.getIndiFirstName().toString(),death2.getIndiFirstName().toString());
        assertEquals("IndiLastName",   death.getIndiLastName().toString(),death2.getIndiLastName().toString());
        assertEquals("IndiSex",        death.getIndiSex().toString(),death2.getIndiSex().toString());
        assertEquals("IndiAge",        death.getIndiAge().toString(),death2.getIndiAge().toString());
        assertEquals("IndiBirthDate",  "",death2.getIndiBirthDate().toString());
        assertEquals("IndiPlace",      death.getIndiPlace().toString(),death2.getIndiPlace().toString());
        assertEquals("IndiOccupation", "",death2.getIndiOccupation().toString());
        assertEquals("IndiComment",    "indicomment, indioccupation, né le 01/01/1990, conjoint: indimarriedlastnameindimarriedname indimarriedcomment, indimarriedoccupation, indimarriedcomment",death2.getIndiComment().toString());
        assertEquals("IndiMarriedFirstName",    death.getIndiMarriedFirstName().toString(),death2.getIndiMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     death.getIndiMarriedLastName().toString(),death2.getIndiMarriedLastName().toString());
        assertEquals("IndiMarriedOccupation",   "",death2.getIndiMarriedOccupation().toString());
        assertEquals("IndiMarriedComment",      death.getIndiMarriedComment().toString(),death2.getIndiMarriedComment().toString());
        assertEquals("IndiMarriedDead",         death.getIndiMarriedDead().toString(),death2.getIndiMarriedDead().toString());

        assertEquals("IndiFatherFirstName",     death.getIndiFatherFirstName().toString(),death2.getIndiFatherFirstName().toString());
        assertEquals("IndiFatherLastName",         death.getIndiLastName().toString(),death2.getIndiFatherLastName().toString());
        assertEquals("IndiFatherOccupation",    "",death2.getIndiFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "indifathercomment, indifatheroccupation",death2.getIndiFatherComment().toString());
        assertEquals("IndiFatherDead",          death.getIndiFatherDead().toString(),death2.getIndiFatherDead().toString());
        assertEquals("IndiMotherFirstName",     death.getIndiMotherFirstName().toString(),death2.getIndiMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      death.getIndiMotherLastName().toString(),death2.getIndiMotherLastName().toString());
        assertEquals("IndiMotherOccupation",    "",death2.getIndiMotherOccupation().toString());
        assertEquals("IndiMotherComment",       "indimothercomment, indimotheroccupation",death2.getIndiMotherComment().toString());
        assertEquals("IndiMotherDead",          death.getIndiMotherDead().toString(),death2.getIndiMotherDead().toString());

        assertEquals("WifeFirstName",           null,death2.getWifeFirstName());
        assertEquals("WifeLastName",            null,death2.getWifeLastName());
        assertEquals("WifeSex",                 null,death2.getWifeSex());
        assertEquals("WifeAge",                 null,death2.getWifeAge());
        assertEquals("WifeBirthDate",           null,death2.getWifeBirthDate());
        assertEquals("WifePlace",               null,death2.getWifePlace());
        assertEquals("WifeOccupation",          null,death2.getWifeOccupation());
        assertEquals("WifeComment",             null,death2.getWifeComment());
        assertEquals("WifeMarriedFirstName",    null,death2.getWifeMarriedFirstName());
        assertEquals("WifeMarriedLastName",     null,death2.getWifeMarriedLastName());
        assertEquals("WifeMarriedOccupation",   null,death2.getWifeMarriedOccupation());
        assertEquals("WifeMarriedComment",      null,death2.getWifeMarriedComment());
        assertEquals("WifeMarriedDead",         null,death2.getWifeMarriedDead());
        assertEquals("WifeFatherFirstName",     null,death2.getWifeFatherFirstName());
        assertEquals("WifeFatherLastName",      null,death2.getWifeFatherLastName());
        assertEquals("WifeFatherOccupation",    null,death2.getWifeFatherOccupation());
        assertEquals("WifeFatherComment",       null,death2.getWifeFatherComment());
        assertEquals("WifeFatherDead",          null,death2.getWifeFatherDead());
        assertEquals("WifeMotherFirstName",     null,death2.getWifeMotherFirstName());
        assertEquals("WifeMotherLastName",      null,death2.getWifeMotherLastName());
        assertEquals("WifeMotherOccupation",    null,death2.getWifeMotherOccupation());
        assertEquals("WifeMotherComment",       null,death2.getWifeMotherComment());
        assertEquals("WifeMotherDead",          null,death2.getWifeMotherDead());

        assertEquals("Witness1FirstName",     death.getWitness1FirstName().toString(),death2.getWitness1FirstName().toString());
        assertEquals("Witness1LastName",      death.getWitness1LastName().toString(),death2.getWitness1LastName().toString());
        assertEquals("Witness1Occupation",    "",death2.getWitness1Occupation().toString());
        assertEquals("Witness1Comment",       "w1comment, w1occupation",death2.getWitness1Comment().toString());
        assertEquals("Witness2FirstName",     death.getWitness2FirstName().toString(),death2.getWitness2FirstName().toString());
        assertEquals("Witness2LastName",      death.getWitness2LastName().toString(),death2.getWitness2LastName().toString());
        assertEquals("Witness2Occupation",    "",death2.getWitness2Occupation().toString());
        assertEquals("Witness2Comment",       "w2comment, w2occupation",death2.getWitness2Comment().toString());
        assertEquals("Witness3FirstName",     "",death2.getWitness3FirstName().toString());
        assertEquals("Witness3LastName",      "",death2.getWitness3LastName().toString());
        assertEquals("Witness3Occupation",    "",death2.getWitness3Occupation().toString());
        assertEquals("Witness3Comment",       "",death2.getWitness3Comment().toString());
        assertEquals("Witness4FirstName",     "",death2.getWitness4FirstName().toString());
        assertEquals("Witness4LastName",      "",death2.getWitness4LastName().toString());
        assertEquals("Witness4Occupation",    "",death2.getWitness4Occupation().toString());
        assertEquals("Witness4Comment",       "",death2.getWitness4Comment().toString());


        assertEquals("GeneralComment", "generalcomment, témoin: w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment ",death2.getGeneralComment().toString());
        

        file.delete();

    }
        /**
     * Test of saveFile method, of class ReleveFileEgmt.
     */
    public void testSaveFileMisc() throws Exception {
        File file = new File("testsaveFile.txt");

        ConfigPanel configPanel = new ConfigPanel();
        DataManager dateManager = new DataManager(configPanel);

        RecordMisc misc = new RecordMisc();
        misc.setEventDate("11/01/2000");
        misc.setCote("cote");
        misc.setParish("parish");
        misc.setNotary("Notary");
        misc.setEventType("eventtag", "");
        misc.setGeneralComment("generalcomment");
        misc.setFreeComment("photo");
        misc.setIndi("indifirstname", "indilastname", "M", "indiage", "01/01/1980", "indiplace", "indioccupation", "indicomment");
        misc.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indimarriedcomment", "indimarrieddead");
        misc.setIndiFather("indifathername", "indifatherlastname", "indifatheroccupation", "indifathercomment", "indifatherdead");
        misc.setIndiMother("indimothername", "indimotherlastname", "indimotheroccupation", "indimothercomment", "indimotherdead");
        misc.setWife("wifefirstname", "wifelastname", "F", "wifeage", "02/02/1982", "wifeplace", "wifeoccupation", "wifecomment");
        misc.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifemarriedcomment", "wifemarrieddead");
        misc.setWifeFather("wifefathername", "wifefatherlastname", "wifefatheroccupation", "wifefathercomment", "wifefatherdead");
        misc.setWifeMother("wifemothername", "wifemotherlastname", "wifemotheroccupation", "wifemothercomment", "wifemotherdead");
        misc.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        misc.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        misc.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        misc.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

        dateManager.addRecord(misc);
        StringBuilder sb = ReleveFileEgmt.saveFile(dateManager, dateManager.getReleveMiscModel(), file, false);
        assertEquals("verify save error", 0, sb.length());

        FileBuffer fb = ReleveFileEgmt.loadFile(file);
        assertEquals("load result", "", fb.getError().toString());
        assertEquals("load count", 1, fb.getBirthCount());
        RecordMisc misc2 = (RecordMisc) fb.getRecords().get(0);

        assertEquals("EventDate",   misc.getEventDateField().toString(),misc2.getEventDateField().toString());
        assertEquals("EventType",   misc.getEventType().toString(),misc2.getEventType().toString());
        assertEquals("parish",      misc.getParish().toString(),misc2.getParish().toString());
        assertEquals("Notary",      misc.getNotary().toString(),misc2.getNotary().toString());
        assertEquals("Cote",        misc.getCote().toString(),misc2.getCote().toString());
        assertEquals("FreeComment",    misc.getFreeComment().toString(),misc2.getFreeComment().toString());

        assertEquals("IndiFirstName",            misc.getIndiFirstName().toString(),misc2.getIndiFirstName().toString());
        assertEquals("IndiLastName",            misc.getIndiLastName().toString(),misc2.getIndiLastName().toString());
        assertEquals("IndiSex",                 misc.getIndiSex().toString(),misc2.getIndiSex().toString());
        assertEquals("IndiAge",                 misc.getIndiAge().toString(),misc2.getIndiAge().toString());
        assertEquals("IndiBirthDate",           "",misc2.getIndiBirthDate().getValue());
        assertEquals("IndiPlace",               misc.getIndiPlace().toString(),misc2.getIndiPlace().toString());
        assertEquals("IndiOccupation",          "",misc2.getIndiOccupation().toString());
        assertEquals("IndiComment",             "indicomment, indioccupation, né le 01/01/1980, conjoint: indimarriedlastnameindimarriedname indimarriedcomment, indimarriedoccupation, indimarriedcomment",misc2.getIndiComment().toString());
        assertEquals("IndiMarriedFirstName",    "",misc2.getIndiMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     "",misc2.getIndiMarriedLastName().toString());
        assertEquals("IndiMarriedOccupation",   "",misc2.getIndiMarriedOccupation().toString());
        assertEquals("IndiMarriedComment",      "".toString(),misc2.getIndiMarriedComment().toString());
        assertEquals("IndiMarriedDead",         "",misc2.getIndiMarriedDead().toString());
        assertEquals("IndiFatherFirstName",     misc.getIndiFatherFirstName().toString(),misc2.getIndiFatherFirstName().toString());
        assertEquals("IndiFatherLastName",      /*Father*/misc.getIndiLastName().toString(),misc2.getIndiFatherLastName().toString());
        assertEquals("IndiFatherOccupation",    "",misc2.getIndiFatherOccupation().toString());
        assertEquals("IndiFatherComment",       "indifathercomment, indifatheroccupation",misc2.getIndiFatherComment().toString());
        assertEquals("IndiFatherDead",          misc.getIndiFatherDead().toString(),misc2.getIndiFatherDead().toString());
        assertEquals("IndiMotherFirstName",     misc.getIndiMotherFirstName().toString(),misc2.getIndiMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      misc.getIndiMotherLastName().toString(),misc2.getIndiMotherLastName().toString());
        assertEquals("IndiMotherOccupation",    "",misc2.getIndiMotherOccupation().toString());
        assertEquals("IndiMotherComment",      "indimothercomment, indimotheroccupation",misc2.getIndiMotherComment().toString());
        assertEquals("IndiMotherDead",          misc.getIndiMotherDead().toString(),misc2.getIndiMotherDead().toString());

        assertEquals("WifeFirstName",           misc.getWifeFirstName().toString(),misc2.getWifeFirstName().toString());
        assertEquals("WifeLastName",            misc.getWifeLastName().toString(),misc2.getWifeLastName().toString());
        assertEquals("WifeSex",                 misc.getWifeSex().toString(),misc2.getWifeSex().toString());
        assertEquals("WifeAge",                 misc.getWifeAge().toString(),misc2.getWifeAge().toString());
        assertEquals("WifeBirthDate",           "",misc2.getWifeBirthDate().toString());
        assertEquals("WifePlace",               misc.getWifePlace().toString(),misc2.getWifePlace().toString());
        assertEquals("WifeOccupation",          "",misc2.getWifeOccupation().toString());
        assertEquals("WifeComment",             "wifecomment, wifeoccupation, né le 02/02/1982, conjoint: wifemarriedlastnamewifemarriedname wifemarriedcomment, wifemarriedoccupation, wifemarriedcomment",misc2.getWifeComment().toString());
        assertEquals("WifeMarriedFirstName",    "",misc2.getWifeMarriedFirstName().toString());
        assertEquals("WifeMarriedLastName",     "",misc2.getWifeMarriedLastName().toString());
        assertEquals("WifeMarriedOccupation",   "",misc2.getWifeMarriedOccupation().toString());
        assertEquals("WifeMarriedComment",      "",misc2.getWifeMarriedComment().toString());
        assertEquals("WifeMarriedDead",         misc.getWifeMarriedDead().toString(),misc2.getWifeMarriedDead().toString());
        assertEquals("WifeFatherFirstName",     misc.getWifeFatherFirstName().toString(),misc2.getWifeFatherFirstName().toString());
        assertEquals("WifeFatherLastName",      /*Father*/misc.getWifeLastName().toString(),misc2.getWifeFatherLastName().toString());
        assertEquals("WifeFatherOccupation",    "",misc2.getWifeFatherOccupation().toString());
        assertEquals("WifeFatherComment",       "wifefathercomment, wifefatheroccupation",misc2.getWifeFatherComment().toString());
        assertEquals("WifeFatherDead",          misc.getWifeFatherDead().toString(),misc2.getWifeFatherDead().toString());
        assertEquals("WifeMotherFirstName",     misc.getWifeMotherFirstName().toString(),misc2.getWifeMotherFirstName().toString());
        assertEquals("WifeMotherLastName",      misc.getWifeMotherLastName().toString(),misc2.getWifeMotherLastName().toString());
        assertEquals("WifeMotherOccupation",    "",misc2.getWifeMotherOccupation().toString());
        assertEquals("WifeMotherComment",       "wifemothercomment, wifemotheroccupation",misc2.getWifeMotherComment().toString());
        assertEquals("WifeMotherDead",          misc.getWifeMotherDead().toString(),misc2.getWifeMotherDead().toString());

        assertEquals("Witness1FirstName",     misc.getWitness1FirstName().toString(),misc2.getWitness1FirstName().toString());
        assertEquals("Witness1LastName",      misc.getWitness1LastName().toString(),misc2.getWitness1LastName().toString());
        assertEquals("Witness1Occupation",    "",misc2.getWitness1Occupation().toString());
        assertEquals("Witness1Comment",       "w1comment, w1occupation",misc2.getWitness1Comment().toString());
        assertEquals("Witness2FirstName",     misc.getWitness2FirstName().toString(),misc2.getWitness2FirstName().toString());
        assertEquals("Witness2LastName",      misc.getWitness2LastName().toString(),misc2.getWitness2LastName().toString());
        assertEquals("Witness2Occupation",    "",misc2.getWitness2Occupation().toString());
        assertEquals("Witness2Comment",       "w2comment, w2occupation",misc2.getWitness2Comment().toString());
        assertEquals("Witness3FirstName",     "",misc2.getWitness3FirstName().toString());
        assertEquals("Witness3LastName",      "",misc2.getWitness3LastName().toString());
        assertEquals("Witness3Occupation",    "",misc2.getWitness3Occupation().toString());
        assertEquals("Witness3Comment",       "",misc2.getWitness3Comment().toString());
        assertEquals("Witness4FirstName",     "",misc2.getWitness4FirstName().toString());
        assertEquals("Witness4LastName",      "",misc2.getWitness4LastName().toString());
        assertEquals("Witness4Occupation",    "",misc2.getWitness4Occupation().toString());
        assertEquals("Witness4Comment",       "",misc2.getWitness4Comment().toString());

        assertEquals("GeneralComment", "generalcomment, témoin: w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment ",misc2.getGeneralComment().toString());

        file.delete();

    }

}

/*
 *
 * assertEquals("EventDate",   misc.getEventDateField().toString(),misc2.getEventDateField().toString());
        assertEquals("Cote",        misc.getCote().toString(),misc2.getCote().toString());
        assertEquals("parish",      misc.getParish().toString(),misc2.getParish().toString());
        assertEquals("EventDate",   misc.getEventDateField().toString(),misc2.getEventDateField().toString());
        assertEquals("Notary",      misc.getNotary().toString(),misc2.getNotary().toString());
        assertEquals("EventType",      misc.getEventType().toString(),misc2.getEventType().toString());
        assertEquals("GeneralComment", misc.getGeneralComment().toString(),misc2.getGeneralComment().toString());
        assertEquals("FreeComment",    misc.getFreeComment().toString(),misc2.getFreeComment().toString());

        assertEquals("IndiFirstName",  misc.getIndiFirstName().toString(),misc2.getIndiFirstName().toString());
        assertEquals("IndiLastName",   misc.getIndiLastName().toString(),misc2.getIndiLastName().toString());
        assertEquals("IndiSex",        misc.getIndiSex().toString(),misc2.getIndiSex().toString());
        assertEquals("IndiAge",        misc.getIndiAge().toString(),misc2.getIndiAge().toString());
        assertEquals("IndiBirthDate",  misc.getIndiBirthDate().toString(),misc2.getIndiBirthDate().toString());
        assertEquals("IndiPlace",      misc.getIndiPlace().toString(),misc2.getIndiPlace().toString());
        assertEquals("IndiOccupation", misc.getIndiOccupation().toString(),misc2.getIndiOccupation().toString());
        assertEquals("IndiComment",    misc.getIndiComment().toString(),misc2.getIndiComment().toString());
        assertEquals("IndiMarriedFirstName",    misc.getIndiMarriedFirstName().toString(),misc2.getIndiMarriedFirstName().toString());
        assertEquals("IndiMarriedLastName",     misc.getIndiMarriedLastName().toString(),misc2.getIndiMarriedLastName().toString());
        assertEquals("IndiMarriedOccupation",   misc.getIndiMarriedOccupation().toString(),misc2.getIndiMarriedOccupation().toString());
        assertEquals("IndiMarriedComment",      misc.getIndiMarriedComment().toString(),misc2.getIndiMarriedComment().toString());
        assertEquals("IndiMarriedDead",         misc.getIndiMarriedDead().toString(),misc2.getIndiMarriedDead().toString());
        assertEquals("IndiFatherFirstName",     misc.getIndiFatherFirstName().toString(),misc2.getIndiFatherFirstName().toString());
        assertEquals("IndiFatherLastName",      misc.getIndiFatherLastName().toString(),misc2.getIndiFatherLastName().toString());
        assertEquals("IndiFatherOccupation",    misc.getIndiFatherOccupation().toString(),misc2.getIndiFatherOccupation().toString());
        assertEquals("IndiFatherComment",       misc.getIndiFatherComment().toString(),misc2.getIndiFatherComment().toString());
        assertEquals("IndiFatherDead",          misc.getIndiFatherDead().toString(),misc2.getIndiFatherDead().toString());
        assertEquals("IndiMotherFirstName",     misc.getIndiMotherFirstName().toString(),misc2.getIndiMotherFirstName().toString());
        assertEquals("IndiMotherLastName",      misc.getIndiMotherLastName().toString(),misc2.getIndiMotherLastName().toString());
        assertEquals("IndiMotherOccupation",    misc.getIndiMotherOccupation().toString(),misc2.getIndiMotherOccupation().toString());
        assertEquals("IndiMotherComment",       misc.getIndiMotherComment().toString(),misc2.getIndiMotherComment().toString());
        assertEquals("IndiMotherDead",          misc.getIndiMotherDead().toString(),misc2.getIndiMotherDead().toString());

        assertEquals("WifeFirstName",  misc.getWifeFirstName().toString(),misc2.getWifeFirstName().toString());
        assertEquals("WifeLastName",   misc.getWifeLastName().toString(),misc2.getWifeLastName().toString());
        assertEquals("WifeSex",        misc.getWifeSex().toString(),misc2.getWifeSex().toString());
        assertEquals("WifeAge",        misc.getWifeAge().toString(),misc2.getWifeAge().toString());
        assertEquals("WifeBirthDate",  misc.getWifeBirthDate().toString(),misc2.getWifeBirthDate().toString());
        assertEquals("WifePlace",      misc.getWifePlace().toString(),misc2.getWifePlace().toString());
        assertEquals("WifeOccupation", misc.getWifeOccupation().toString(),misc2.getWifeOccupation().toString());
        assertEquals("WifeComment",    misc.getWifeComment().toString(),misc2.getWifeComment().toString());
        assertEquals("WifeMarriedFirstName",    misc.getWifeMarriedFirstName().toString(),misc2.getWifeMarriedFirstName().toString());
        assertEquals("WifeMarriedLastName",     misc.getWifeMarriedLastName().toString(),misc2.getWifeMarriedLastName().toString());
        assertEquals("WifeMarriedOccupation",   misc.getWifeMarriedOccupation().toString(),misc2.getWifeMarriedOccupation().toString());
        assertEquals("WifeMarriedComment",      misc.getWifeMarriedComment().toString(),misc2.getWifeMarriedComment().toString());
        assertEquals("WifeMarriedDead",         misc.getWifeMarriedDead().toString(),misc2.getWifeMarriedDead().toString());
        assertEquals("WifeFatherFirstName",     misc.getWifeFatherFirstName().toString(),misc2.getWifeFatherFirstName().toString());
        assertEquals("WifeFatherLastName",      misc.getWifeFatherLastName().toString(),misc2.getWifeFatherLastName().toString());
        assertEquals("WifeFatherOccupation",    misc.getWifeFatherOccupation().toString(),misc2.getWifeFatherOccupation().toString());
        assertEquals("WifeFatherComment",       misc.getWifeFatherComment().toString(),misc2.getWifeFatherComment().toString());
        assertEquals("WifeFatherDead",          misc.getWifeFatherDead().toString(),misc2.getWifeFatherDead().toString());
        assertEquals("WifeMotherFirstName",     misc.getWifeMotherFirstName().toString(),misc2.getWifeMotherFirstName().toString());
        assertEquals("WifeMotherLastName",      misc.getWifeMotherLastName().toString(),misc2.getWifeMotherLastName().toString());
        assertEquals("WifeMotherOccupation",    misc.getWifeMotherOccupation().toString(),misc2.getWifeMotherOccupation().toString());
        assertEquals("WifeMotherComment",       misc.getWifeMotherComment().toString(),misc2.getWifeMotherComment().toString());
        assertEquals("WifeMotherDead",          misc.getWifeMotherDead().toString(),misc2.getWifeMotherDead().toString());

        assertEquals("Witness1FirstName",     misc.getWitness1FirstName().toString(),misc2.getWitness1FirstName().toString());
        assertEquals("Witness1LastName",      misc.getWitness1LastName().toString(),misc2.getWitness1LastName().toString());
        assertEquals("Witness1Occupation",    misc.getWitness1Occupation().toString(),misc2.getWitness1Occupation().toString());
        assertEquals("Witness1Comment",       misc.getWitness1Comment().toString(),misc2.getWitness1Comment().toString());
        assertEquals("Witness2FirstName",     misc.getWitness2FirstName().toString(),misc2.getWitness2FirstName().toString());
        assertEquals("Witness2LastName",      misc.getWitness2LastName().toString(),misc2.getWitness2LastName().toString());
        assertEquals("Witness2Occupation",    misc.getWitness2Occupation().toString(),misc2.getWitness2Occupation().toString());
        assertEquals("Witness2Comment",       misc.getWitness2Comment().toString(),misc2.getWitness2Comment().toString());
        assertEquals("Witness2FirstName",     misc.getWitness3FirstName().toString(),misc2.getWitness3FirstName().toString());
        assertEquals("Witness3LastName",      misc.getWitness3LastName().toString(),misc2.getWitness3LastName().toString());
        assertEquals("Witness3Occupation",    misc.getWitness3Occupation().toString(),misc2.getWitness3Occupation().toString());
        assertEquals("Witness3Comment",       misc.getWitness3Comment().toString(),misc2.getWitness3Comment().toString());
        assertEquals("Witness4FirstName",     misc.getWitness4FirstName().toString(),misc2.getWitness4FirstName().toString());
        assertEquals("Witness4LastName",      misc.getWitness4LastName().toString(),misc2.getWitness4LastName().toString());
        assertEquals("Witness4Occupation",    misc.getWitness4Occupation().toString(),misc2.getWitness4Occupation().toString());
        assertEquals("Witness4Comment",       misc.getWitness4Comment().toString(),misc2.getWitness4Comment().toString());
 */
