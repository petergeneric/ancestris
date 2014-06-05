package ancestris.modules.releve.dnd;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.FieldPlace;
import ancestris.modules.releve.model.RecordMisc;
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
public class MergeModelMiscMarcTest extends TestCase {

     static public FieldPlace getRecordsInfoPlace() {
        FieldPlace recordsInfoPlace = new FieldPlace();
        recordsInfoPlace.setValue("ville_marc,code_marc,departement_marc,region_marc,pays_marc");
        return recordsInfoPlace;
    }

    public static RecordMisc createMiscMarcRecord(String id) {
        RecordMisc record = new RecordMisc();
        if (id.equals("CM1")) {
            record.setEventDate("01/03/1999");
            record.setEventType("Contrat de mariage");
            record.setNotary("notaire_marc");
            record.setCote("cote");
            record.setGeneralComment("generalcomment");
            record.setFreeComment("photo");
            record.setIndi("Fatherfirstname", "FATHERLASTNAME", "M", "indiage", "01/01/1970", "indiBirthplace", "indioccupation22", "indiResidence", "indicomments");
            record.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indiMarriedResidence", "indimarriedcomment", "indimarrieddead");
            record.setIndiFather("indifathername", "FATHERLASTNAME", "indifatheroccupation", "indiFatherResidence", "indifathercomment", "indifatherdead", "70y");
            record.setIndiMother("indimothername", "MOTHERLASTNAME", "indimotheroccupation", "indiMotherResidence", "indimothercomment", "indimotherdead", "72y");
            record.setWife("Motherfirstname", "WIFEFATHERLASTNAME", "F", "wifeage", "03/03/1973", "wifeplace", "wifeoccupation", "wifeResidence", "wifecomment");
            record.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifeMarriedResidence", "wifemarriedcomment", "wifemarrieddead");
            record.setWifeFather("wifefathername", "WIFEFATHERLASTNAME", "wifefatheroccupation", "wiferFatherResidence", "wifefathercomment", "wifefatherdead", "60y");
            record.setWifeMother("wifemothername", "WIFEMOTHERLASTNAME", "wifemotheroccupation", "wifeMotherResidence", "wifemothercomment", "wifemotherdead", "62y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
        } else if (id.equals("NEW")) {
            record.setEventDate("01/03/1999");
            record.setEventType("CM");
            record.setNotary("notaire_marc");
            record.setCote("cote");
            record.setGeneralComment("generalcomment");
            record.setFreeComment("photo");
            record.setIndi("Fatherfirstname", "NEW_FATHERLASTNAME", "M", "indiage", "", "indiBirthplace", "indioccupation22", "indiResidence", "indicomments");
            record.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indiMarriedResidence", "indimarriedcomment", "indimarrieddead");
            record.setIndiFather("indifathername", "NEW_FATHERLASTNAME", "indifatheroccupation", "indiFatherResidence", "indifathercomment", "indifatherdead", "70y");
            record.setIndiMother("indimothername", "NEW_MOTHERLASTNAME", "indimotheroccupation", "indiMotherResidence", "indimothercomment", "indimotherdead", "72y");
            record.setWife("Motherfirstname", "NEW_WIFEFATHERLASTNAME", "F", "wifeage", "03/03/1973", "wifeplace", "wifeoccupation", "wifeResidence", "wifecomment");
            record.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifeMarriedResidence", "wifemarriedcomment", "wifemarrieddead");
            record.setWifeFather("wifefathername", "NEW_WIFEFATHERLASTNAME", "wifefatheroccupation", "wiferFatherResidence", "wifefathercomment", "wifefatherdead", "60y");
            record.setWifeMother("wifemothername", "NEW_WIFEMOTHERLASTNAME", "wifemotheroccupation", "wifeMotherResidence", "wifemothercomment", "wifemotherdead", "62y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
        }
        return record;
    }

    /**
     * 
    */
    public void testAddMarcNewCM() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            
            String fileName = "ville_marc.txt";
            MergeOptionPanel.SourceModel.getModel().add(fileName, gedcom.getEntity("SOUR", "S1").getPropertyDisplayValue("TITL"));

            RecordMisc miscRecord = createMiscMarcRecord("NEW");
            MergeRecord mergeRecord = new MergeRecord(getRecordsInfoPlace(), fileName, miscRecord);

            List<MergeModel> models;
            Fam indiParentFamily;
            Indi indiFather;
            Indi indiMother;
            Fam wifeParentFamily;
            Indi wifeFather;
            Indi wifeMother;

            models = MergeModel.createMergeModel(mergeRecord, gedcom, null);
            assertEquals("Nombre model",1,models.size());

            //MergeDialog dialog = MergeDialog.show(new javax.swing.JFrame(), gedcom, null, miscRecord, true);
            //Thread.sleep(10000);

            models.get(0).copyRecordToEntity();

            Fam fam = (Fam) gedcom.getEntity("F4");
            assertEquals("Lien marc vers source","@S1@", fam.getValue(new TagPath("FAM:MARC:SOUR"),""));
            assertEquals("Source marc","S1", gedcom.getEntity(fam.getValue(new TagPath("FAM:MARC:SOUR"),"").replaceAll("@", "")).getId());
            assertEquals("Source marc",miscRecord.getCote().getValue() + ", " +miscRecord.getFreeComment().getValue(), fam.getValue(new TagPath("FAM:MARC:SOUR:PAGE"),""));
            assertEquals("Date marc",miscRecord.getEventDateProperty().getValue(), fam.getValue(new TagPath("FAM:MARC:DATE"),""));
            assertEquals("Lieu marc",getRecordsInfoPlace().getValue(), fam.getValue(new TagPath("FAM:MARC:PLAC"),""));
            //assertEquals("Note marc",miscRecord.getGeneralComment().getValue(), fam.getValue(new TagPath("FAM:MARC:NOTE"),""));

            assertEquals("Date marriage","ABT 1999", fam.getMarriageDate().getValue());


            assertEquals("Indi : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), fam.getHusband().getBirthDate().getValue());
            // le lieu et commentaire ne sont pas modifiés car la date de naissance du releve n'est pas plus precise
            assertNotSame("Indi : lieu naissance",mergeRecord.getIndi().getBirthPlace(), fam.getHusband().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            //assertEquals("Indi : Note naissance","", fam.getHusband().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Indi : Profession",1, fam.getHusband().getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = fam.getHusband().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Indi : Profession",mergeRecord.getIndi().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Indi : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Indi : Lieu Profession",mergeRecord.getIndi().getResidence(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            //assertEquals("Indi : Note Profession","Profession indiquée dans l'acte CM entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 ( ville_marc, notaire_marc) ",
            //        occupation.getValue(new TagPath("OCCU:NOTE"),""));

            indiParentFamily = fam.getHusband().getFamilyWhereBiologicalChild();
            assertNotSame("Mari parent marc date inchangé",mergeRecord.getIndi().getParentMarriageDate().getValue(),  indiParentFamily.getMarriageDate().getValue());
            assertEquals("Mari parent marc comment",true,  indiParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            indiFather = indiParentFamily.getHusband();
            assertEquals("Mari pere nom",mergeRecord.getIndi().getFatherLastName(), indiFather.getLastName());
            assertEquals("Mari pere prenom",mergeRecord.getIndi().getFatherFirstName(),  indiFather.getFirstName());
            assertEquals("Mari pere profession",mergeRecord.getIndi().getFatherOccupation(),  indiFather.getProperty("OCCU").getValue());
            indiMother = indiParentFamily.getWife();
            assertEquals("Mari mere nom",mergeRecord.getIndi().getMotherLastName(), indiMother.getLastName());
            assertEquals("Mari mere prenom",mergeRecord.getIndi().getMotherFirstName(),  indiMother.getFirstName());
            assertEquals("Mari mere profession",mergeRecord.getIndi().getMotherOccupation(),  indiMother.getProperty("OCCU").getValue());

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : lieu naissance",mergeRecord.getWife().getBirthPlace(), fam.getWife().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            //assertEquals("Wife : Note naissance",
            //        "Naissance 3 mar 1973 déduite de  l'acte CM entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 ( ville_marc, notaire_marc)",
            //        fam.getWife().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : Profession", 1, fam.getWife().getProperties(new TagPath("INDI:OCCU")).length);
            occupation = fam.getWife().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Wife : Profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Wife : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Wife : Lieu Profession",mergeRecord.getWife().getResidence(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            //assertEquals("Wife : Note Profession",
            //        "Profession indiquée dans l'acte CM entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 ( ville_marc, notaire_marc) ",
            //        occupation.getValue(new TagPath("OCCU:NOTE"),""));

            wifeParentFamily = fam.getWife().getFamilyWhereBiologicalChild();
            assertEquals("Femme parent marc date",mergeRecord.getWife().getParentMarriageDate().getValue(),  wifeParentFamily.getMarriageDate().getValue());
            assertEquals("Femme parent marc comment",true,  wifeParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            wifeFather = wifeParentFamily.getHusband();
            assertEquals("Femme pere nom",mergeRecord.getWife().getFatherLastName(), wifeFather.getLastName());
            assertEquals("Femme pere prenom",mergeRecord.getWife().getFatherFirstName(),  wifeFather.getFirstName());
            assertEquals("Femme pere profession",mergeRecord.getWife().getFatherOccupation(),  wifeFather.getProperty("OCCU").getValue());
            wifeMother = wifeParentFamily.getWife();
            assertEquals("Femme mere nom",mergeRecord.getWife().getMotherLastName(), wifeMother.getLastName());
            assertEquals("Femme mere prenom",mergeRecord.getWife().getMotherFirstName(),  wifeMother.getFirstName());
            assertEquals("Femme mere profession",mergeRecord.getWife().getMotherOccupation(),  wifeMother.getProperty("OCCU").getValue());

            //MergeDialog dialog = MergeDialog.show(new javax.swing.JFrame(), gedcom, null, miscRecord, true);
            //Thread.sleep(10000);

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    /**
     *
    */    
     public void testAddMarcCM1() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Fam f1 = (Fam) gedcom.getEntity("F1");
            Property marriageProperty = f1.addProperty("MARR", "");
            marriageProperty.addProperty("DATE", "01 JAN 2000");


            RecordMisc miscRecord = createMiscMarcRecord("CM1");
            String fileName = "";
            MergeRecord mergeRecord = new MergeRecord(getRecordsInfoPlace(), fileName, miscRecord);

            List<MergeModel> models;
            Fam indiParentFamily;
            Indi indiFather;
            Indi indiMother;
            Fam wifeParentFamily;
            Indi wifeFather;
            Indi wifeMother;

            models = MergeModel.createMergeModel(mergeRecord, gedcom, null);
            assertEquals("Nombre model",2,models.size());

            //MergeDialog dialog = MergeDialog.show(new javax.swing.JFrame(), gedcom, null, miscRecord, true);
            //Thread.sleep(10000);

            models.get(0).copyRecordToEntity();

            Fam fam = (Fam) gedcom.getEntity("F4");
            assertEquals("Lien marc vers source","", fam.getValue(new TagPath("FAM:MARC:SOUR"),""));
            assertEquals("Source marc","", fam.getValue(new TagPath("FAM:MARC:SOUR:PAGE"),""));
            assertEquals("Date marc",miscRecord.getEventDateProperty().getValue(), fam.getValue(new TagPath("FAM:MARC:DATE"),""));
            assertEquals("Lieu marc",getRecordsInfoPlace().getValue(), fam.getValue(new TagPath("FAM:MARC:PLAC"),""));
            //assertEquals("Note marc",miscRecord.getGeneralComment().getValue(), fam.getValue(new TagPath("FAM:MARC:NOTE"),""));

            assertEquals("Date marriage","ABT 1999", fam.getMarriageDate().getValue());


            assertEquals("Indi : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), fam.getHusband().getBirthDate().getValue());
            // le lieu et commentaire ne sont pas modifiés car la date de naissance du releve n'est pas plus precise
            assertNotSame("Indi : lieu naissance",mergeRecord.getIndi().getBirthPlace(), fam.getHusband().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Indi : Note naissance","",
                    fam.getHusband().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Indi : Profession",2, fam.getHusband().getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = fam.getHusband().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Indi : Profession",mergeRecord.getIndi().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Indi : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Indi : Lieu Profession",mergeRecord.getIndi().getResidence(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Indi : Note Profession","Profession indiquée dans l'acte de contrat de mariage entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_marc, notaire_marc)",
                    occupation.getValue(new TagPath("OCCU:NOTE"),""));

            indiParentFamily = fam.getHusband().getFamilyWhereBiologicalChild();
            assertNotSame("Mari parent marc date inchangé",mergeRecord.getIndi().getParentMarriageDate().getValue(),  indiParentFamily.getMarriageDate().getValue());
            assertEquals("Mari parent marc comment",true,  indiParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            indiFather = indiParentFamily.getHusband();
            assertEquals("Mari pere nom",mergeRecord.getIndi().getFatherLastName(), indiFather.getLastName());
            assertEquals("Mari pere prenom",mergeRecord.getIndi().getFatherFirstName(),  indiFather.getFirstName());
            assertEquals("Mari pere profession",mergeRecord.getIndi().getFatherOccupation(),  indiFather.getProperty("OCCU").getValue());
            indiMother = indiParentFamily.getWife();
            assertEquals("Mari mere nom",mergeRecord.getIndi().getMotherLastName(), indiMother.getLastName());
            assertEquals("Mari mere prenom",mergeRecord.getIndi().getMotherFirstName(),  indiMother.getFirstName());
            assertEquals("Mari mere profession",mergeRecord.getIndi().getMotherOccupation(),  indiMother.getProperty("OCCU").getValue());

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : lieu naissance",mergeRecord.getWife().getBirthPlace(), fam.getWife().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Wife : Note naissance",
                    "Date de naissance 3 mar 1973 déduite de l'acte de contrat de mariage entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_marc, notaire_marc)",
                    fam.getWife().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : Profession",1, fam.getWife().getProperties(new TagPath("INDI:OCCU")).length);
            occupation = fam.getWife().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Wife : Profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Wife : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Wife : Lieu Profession",mergeRecord.getWife().getResidence(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Wife : Note Profession",
                    "Profession indiquée dans l'acte de contrat de mariage entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_marc, notaire_marc)",
                    occupation.getValue(new TagPath("OCCU:NOTE"),""));

            wifeParentFamily = fam.getWife().getFamilyWhereBiologicalChild();
            assertEquals("Femme parent marc date",mergeRecord.getWife().getParentMarriageDate().getValue(),  wifeParentFamily.getMarriageDate().getValue());
            assertEquals("Femme parent marc comment",true,  wifeParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            wifeFather = wifeParentFamily.getHusband();
            assertEquals("Femme pere nom",mergeRecord.getWife().getFatherLastName(), wifeFather.getLastName());
            assertEquals("Femme pere prenom",mergeRecord.getWife().getFatherFirstName(),  wifeFather.getFirstName());
            assertEquals("Femme pere profession",mergeRecord.getWife().getFatherOccupation(),  wifeFather.getProperty("OCCU").getValue());
            wifeMother = wifeParentFamily.getWife();
            assertEquals("Femme mere nom",mergeRecord.getWife().getMotherLastName(), wifeMother.getLastName());
            assertEquals("Femme mere prenom",mergeRecord.getWife().getMotherFirstName(),  wifeMother.getFirstName());
            assertEquals("Femme mere profession",mergeRecord.getWife().getMotherOccupation(),  wifeMother.getProperty("OCCU").getValue());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }
     
 

}
