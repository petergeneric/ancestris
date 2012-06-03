package ancestris.modules.releve.dnd;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.RecordMarriage;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class MergeModelMarriageTest extends TestCase {

    public static RecordMarriage createMarriageRecord(String id) {
        RecordMarriage record = new RecordMarriage();
        if ( id.equals("M1")) {
            record.setEventPlace("ville_mariage","code_mariage","departement_mariage","region_mariage","pays_mariage");
            record.setEventDate("01/03/1999");
            record.setCote("cote");
            record.setGeneralComment("generalcomment");
            record.setFreeComment("photo");
            record.setIndi("Fatherfirstname", "FATHERLASTNAME", "M", "indiage", "02/02/1970", "indiplace", "indioccupation", "indicomment");
            record.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indimarriedcomment", "indimarrieddead");
            record.setIndiFather("indifathername", "INDIFATHERLASTNAME", "indifatheroccupation", "indifathercomment", "indifatherdead", "70y");
            record.setIndiMother("indimothername", "INDIMOTHERLASTNAME", "indimotheroccupation", "indimothercomment", "indimotherdead", "72y");
            record.setWife("Motherfirstname", "MOTHERLASTNAME", "F", "wifeage", "03/03/1973", "wifeplace", "wifeoccupation", "wifecomment");
            record.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifemarriedcomment", "wifemarrieddead");
            record.setWifeFather("wifefathername", "WIFEFATHERLASTNAME", "wifefatheroccupation", "wifefathercomment", "wifefatherdead", "60y");
            record.setWifeMother("wifemothername", "WIFEMOTHERLASTNAME", "wifemotheroccupation", "wifemothercomment", "wifemotherdead", "62y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
        }
        return record;
        
    }

    /**
     * testSaveDataComment
     */
    public void testSaveDataMarriageDate() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            MergeRecord mergeRecord = new MergeRecord(createMarriageRecord("M1"));

            List<MergeModel> models;
            Fam indiParentFamily;
            Indi indiFather;
            Indi indiMother;
            Fam wifeParentFamily;
            Indi wifeFather;
            Indi wifeMother;

            models = MergeModel.createMergeModel(mergeRecord, gedcom, null);
            assertEquals("Nombre model",2,models.size());
            models.get(0).copyRecordToEntity();

            Fam fam = (Fam) gedcom.getEntity("F00004");
            assertEquals("Lien mariage vers source","@S00004@", fam.getValue(new TagPath("FAM:MARR:SOUR"),""));
            assertEquals("Source mariage","S00004", gedcom.getEntity(fam.getValue(new TagPath("FAM:MARR:SOUR"),"").replaceAll("@", "")).getId());
            assertEquals("Date mariage",mergeRecord.getEventDate().getValue(), fam.getMarriageDate().getValue());
            assertEquals("Lieu mariage",mergeRecord.getEventPlace(), fam.getValue(new TagPath("FAM:MARR:PLAC"),""));
            assertEquals("Note mariage",253, fam.getValue(new TagPath("FAM:MARR:NOTE"),"").length());

            assertNotSame("Mari : Date naissance",mergeRecord.getIndiBirthDate().getValue(), fam.getHusband().getBirthDate().getValue());
            // le lieu et la date ne sont pas modifiés car la date de naissance du releve n'est pas plus precise
            assertEquals("Mari : lieu naissance","", fam.getHusband().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Mari : Note naissance","", fam.getHusband().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Mari : Profession",2, fam.getHusband().getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = fam.getHusband().getProperties(new TagPath("INDI:OCCU"))[1];
            assertEquals("Mari : Profession",mergeRecord.getIndiOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Mari : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Mari : Lieu Profession",mergeRecord.getEventPlace(),     occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Mari : Note Profession",143,                                   occupation.getValue(new TagPath("OCCU:NOTE"),"").length());

            indiParentFamily = fam.getHusband().getFamilyWhereBiologicalChild();
            assertNotSame("Mari parent mariage date inchangé",mergeRecord.getIndiParentMarriageDate().getValue(),  indiParentFamily.getMarriageDate().getValue());
            assertEquals("Mari parent mariage comment",true,  indiParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            indiFather = indiParentFamily.getHusband();
            assertEquals("Mari pere nom",mergeRecord.getIndiFatherLastName(), indiFather.getLastName());
            assertEquals("Mari pere prenom",mergeRecord.getIndiFatherFirstName(),  indiFather.getFirstName());
            assertEquals("Mari pere profession",mergeRecord.getIndiFatherOccupation(),  indiFather.getProperty("OCCU").getValue());
            indiMother = indiParentFamily.getWife();
            assertEquals("Mari mere nom",mergeRecord.getIndiMotherLastName(), indiMother.getLastName());
            assertEquals("Mari mere prenom",mergeRecord.getIndiMotherFirstName(),  indiMother.getFirstName());
            assertEquals("Mari mere profession",mergeRecord.getIndiMotherOccupation(),  indiMother.getProperty("OCCU").getValue());

            assertEquals("Femme : Date naissance",mergeRecord.getWifeBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Femme : lieu naissance",mergeRecord.getWifePlace(), fam.getWife().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Femme : Note naissance",142, fam.getWife().getValue(new TagPath("INDI:BIRT:NOTE"), "").length());

            assertEquals("Femme : Date naissance",mergeRecord.getWifeBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Femme : Profession",1, fam.getWife().getProperties(new TagPath("INDI:OCCU")).length);
            occupation = fam.getWife().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Femme : Profession",mergeRecord.getWifeOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Femme : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Femme : Lieu Profession",mergeRecord.getEventPlace(),     occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Femme : Note Profession",143,                                   occupation.getValue(new TagPath("OCCU:NOTE"),"").length());

            wifeParentFamily = fam.getWife().getFamilyWhereBiologicalChild();
            assertEquals("Femme parent mariage date",mergeRecord.getWifeParentMarriageDate().getValue(),  wifeParentFamily.getMarriageDate().getValue());
            assertEquals("Femme parent mariage comment",true,  wifeParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            wifeFather = wifeParentFamily.getHusband();
            assertEquals("Femme pere nom",mergeRecord.getWifeFatherLastName(), wifeFather.getLastName());
            assertEquals("Femme pere prenom",mergeRecord.getWifeFatherFirstName(),  wifeFather.getFirstName());
            assertEquals("Femme pere profession",mergeRecord.getWifeFatherOccupation(),  wifeFather.getProperty("OCCU").getValue());
            wifeMother = wifeParentFamily.getWife();
            assertEquals("Femme mere nom",mergeRecord.getWifeMotherLastName(), wifeMother.getLastName());
            assertEquals("Femme mere prenom",mergeRecord.getWifeMotherFirstName(),  wifeMother.getFirstName());
            assertEquals("Femme mere profession",mergeRecord.getWifeMotherOccupation(),  wifeMother.getProperty("OCCU").getValue());


        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * testSaveDataComment
     */
    public void testSaveDataMarraigeDate2() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            MergeRecord mergeRecord = new MergeRecord(createMarriageRecord("M1"));
            List<MergeModel> models;
            // je change la date de naissance du pere pour permettre la modification
            ((Indi)gedcom.getEntity("I1")).getBirthDate().setValue("BEF 1971");

            models = MergeModel.createMergeModel(mergeRecord, gedcom, null);
            assertEquals("Nombre model",2,models.size());
            models.get(0).copyRecordToEntity();

            Fam fam = (Fam) gedcom.getEntity("F00004");
            assertEquals("Lien mariage vers source","@S00004@", fam.getValue(new TagPath("FAM:MARR:SOUR"),""));
            assertEquals("Source mariage","S00004", gedcom.getEntity(fam.getValue(new TagPath("FAM:MARR:SOUR"),"").replaceAll("@", "")).getId());
            assertEquals("Date mariage",mergeRecord.getEventDate().getValue(), fam.getMarriageDate().getValue());
            assertEquals("Lieu mariage",mergeRecord.getEventPlace(), fam.getValue(new TagPath("FAM:MARR:PLAC"),""));
            assertEquals("Note mariage",253, fam.getValue(new TagPath("FAM:MARR:NOTE"),"").length());

            assertNotSame("Mari : Date naissance",mergeRecord.getIndiBirthDate().getValue(), fam.getHusband().getBirthDate().getValue());
            assertEquals("Mari : lieu naissance",mergeRecord.getIndiPlace(), fam.getHusband().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Mari : Note naissance",142, fam.getHusband().getValue(new TagPath("INDI:BIRT:NOTE"), "").length());

            assertEquals("Mari : Profession",2, fam.getHusband().getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = fam.getHusband().getProperties(new TagPath("INDI:OCCU"))[1];
            assertEquals("Mari : Profession",mergeRecord.getIndiOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Mari : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Mari : Lieu Profession",mergeRecord.getEventPlace(),     occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Mari : Note Profession",143,                                   occupation.getValue(new TagPath("OCCU:NOTE"),"").length());

            assertEquals("Femme : Date naissance",mergeRecord.getWifeBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Femme : lieu naissance",mergeRecord.getWifePlace(), fam.getWife().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Femme : Note naissance",142, fam.getWife().getValue(new TagPath("INDI:BIRT:NOTE"), "").length());

            assertEquals("Femme : Date naissance",mergeRecord.getWifeBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Femme : Profession",1, fam.getWife().getProperties(new TagPath("INDI:OCCU")).length);
            occupation = fam.getWife().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Femme : Profession",mergeRecord.getWifeOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Femme : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Femme : Lieu Profession",mergeRecord.getEventPlace(),     occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Femme : Note Profession",143,                                   occupation.getValue(new TagPath("OCCU:NOTE"),"").length());


//            assertEquals("famille","F1", indi.getFamilyWhereBiologicalChild().getId());
//
//            Indi father = indi.getBiologicalFather();
//            assertEquals("fatherFirstName",record.getIndiFatherFirstName(), father.getFirstName());
//            // la date de naissance du pere n'est pas changée car elle est plus précise que celle du releve
//            assertEquals("Naissance du pere","1 jan 1970", father.calculateBirthDate().getDisplayValue());
//            assertEquals("deces du pere",   "apr 1999", father.calculateDeathDate().getDisplayValue());
//
//            Indi mother = indi.getBiologicalMother();
//            assertEquals("Naissance du pere","ava 1985", mother.calculateBirthDate().getDisplayValue());
//            assertEquals("deces du pere",   "apr 2000", mother.calculateDeathDate().getDisplayValue());

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }


    
}
