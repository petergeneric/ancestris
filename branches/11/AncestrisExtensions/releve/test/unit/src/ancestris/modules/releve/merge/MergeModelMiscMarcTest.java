package ancestris.modules.releve.merge;

import ancestris.modules.releve.IgnoreOtherTestMethod;
import ancestris.modules.releve.RecordTransferHandle;
import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.dnd.TransferableRecord;
import ancestris.modules.releve.model.PlaceFormatModel;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.RecordInfoPlace;
import ancestris.modules.releve.model.RecordMisc;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.TagPath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Michel
 */
public class MergeModelMiscMarcTest {
    @Rule
    public IgnoreOtherTestMethod rule = new IgnoreOtherTestMethod("");

     static public RecordInfoPlace getRecordsInfoPlace() {
        RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
        recordsInfoPlace.setValue("ville_marc","code_marc","departement_marc","region_marc","pays_marc");
        return recordsInfoPlace;
    }

    public static RecordMisc createMiscMarcRecord(String id) {
        RecordMisc record = new RecordMisc();
        if (id.equals("CM1")) {
            record.setFieldValue(FieldType.eventDate, "01/03/1999");
            record.setFieldValue(Record.FieldType.eventType, "Contrat de mariage");
            record.setFieldValue(FieldType.notary, "notaire_marc");
            record.setFieldValue(FieldType.cote, "cote");
            record.setFieldValue(FieldType.generalComment, "generalcomment");
            record.setFieldValue(FieldType.freeComment,  "photo");
            record.setIndi("Fatherfirstname", "FATHERLASTNAME", "M", "indiage", "1970", "indiBirthplace", "indiBirthAddress", "indioccupation22", "indiResidence", "indiAddress", "indicomments");
            record.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indiMarriedResidence", "indiMarriedAddress", "indimarriedcomment", "indimarrieddead");
            record.setIndiFather("indifathername", "FATHERLASTNAME", "indifatheroccupation", "indiFatherResidence", "indiFatherAddress", "indifathercomment", "indifatherdead", "70y");
            record.setIndiMother("indimothername", "MOTHERLASTNAME", "indimotheroccupation", "indiMotherResidence", "indiMotherAddress", "indimothercomment", "indimotherdead", "72y");
            record.setWife("Motherfirstname", "WIFEFATHERLASTNAME", "F", "wifeage", "03/03/1973", "wifeplace", "wifeBirthAddress", "wifeoccupation", "wifeResidence", "wifeAddress", "wifecomment");
            record.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifeMarriedResidence", "wifeMarriedAddress", "wifemarriedcomment", "wifemarrieddead");
            record.setWifeFather("wifefathername", "WIFEFATHERLASTNAME", "wifefatheroccupation", "wiferFatherResidence", "wiferFatherAddress", "wifefathercomment", "wifefatherdead", "60y");
            record.setWifeMother("wifemothername", "WIFEMOTHERLASTNAME", "wifemotheroccupation", "wifeMotherResidence", "wiferMotherAddress", "wifemothercomment", "wifemotherdead", "62y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

        } else if (id.equals("NEW")) {
            record.setFieldValue(FieldType.eventDate, "01/03/1999");
            record.setFieldValue(Record.FieldType.eventType, "CM");
            record.setFieldValue(FieldType.notary, "notaire_marc");
            record.setFieldValue(FieldType.cote, "cote");
            record.setFieldValue(FieldType.generalComment, "generalcomment");
            record.setFieldValue(FieldType.freeComment,  "photo");
            record.setIndi("Fatherfirstname", "NEW_FATHERLASTNAME", "M", "indiage", "", "indiBirthplace", "indiBirthAddress", "indioccupation22", "indiResidence", "indiAddress", "indicomments");
            record.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indiMarriedResidence", "indiMarriedAddress", "indimarriedcomment", "indimarrieddead");
            record.setIndiFather("indifathername", "NEW_FATHERLASTNAME", "indifatheroccupation", "indiFatherResidence", "indiFatherAddress", "indifathercomment", "indifatherdead", "70y");
            record.setIndiMother("indimothername", "NEW_MOTHERLASTNAME", "indimotheroccupation", "indiMotherResidence", "indiMotherAddress", "indimothercomment", "indimotherdead", "72y");
            record.setWife("Motherfirstname", "NEW_WIFEFATHERLASTNAME", "F", "wifeage", "03/03/1973", "wifeplace", "wifeBirthAddress", "wifeoccupation", "wifeResidence", "wifeAddress", "wifecomment");
            record.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifeMarriedResidence", "wifeMarriedAddress", "wifemarriedcomment", "wifemarrieddead");
            record.setWifeFather("wifefathername", "NEW_WIFEFATHERLASTNAME", "wifefatheroccupation", "wiferFatherResidence", "wiferFatherAddress", "wifefathercomment", "wifefatherdead", "60y");
            record.setWifeMother("wifemothername", "NEW_WIFEMOTHERLASTNAME", "wifemotheroccupation", "wifeMotherResidence", "wiferMotherAddress", "wifemothercomment", "wifemotherdead", "62y");
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
    @Test
    public void testAddMarcNewCM() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();

            // Merge options
            PlaceFormatModel.getCurrentModel().savePreferences(1,2,3,4,5,6);

            String fileName = "ville_marc.txt";
            MergeOptionPanel.SourceModel.getModel().add(fileName, gedcom.getEntity("SOUR", "S1").getPropertyDisplayValue("TITL"));
            RecordMisc miscRecord = createMiscMarcRecord("NEW");
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, miscRecord);

//TestUtility.showMergeDialog(data, gedcom, null);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
            MergeRecord mergeRecord = mergeManager.getMergeRecord();

            Fam indiParentFamily;
            Indi indiFather;
            Indi indiMother;
            Fam wifeParentFamily;
            Indi wifeFather;
            Indi wifeMother;

            assertEquals("Nombre model",1,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

            Fam fam = (Fam) mergeManager.getProposalList1().getElementAt(0).getMainEntity();
            assertEquals("Lien marc vers source","@S1@", fam.getValue(new TagPath("FAM:MARC:SOUR"),""));
            assertEquals("Source marc","S1", gedcom.getEntity(fam.getValue(new TagPath("FAM:MARC:SOUR"),"").replaceAll("@", "")).getId());
            assertEquals("Source marc",miscRecord.getFieldValue(Record.FieldType.cote) + ", " +miscRecord.getFieldValue(Record.FieldType.freeComment), fam.getValue(new TagPath("FAM:MARC:SOUR:PAGE"),""));
            assertEquals("Date marc", true, miscRecord.getField(FieldType.eventDate).equalsProperty(fam.getProperty(new TagPath("FAM:MARC:DATE")) ));
            assertEquals("Lieu marc",getRecordsInfoPlace().getValue(), fam.getValue(new TagPath("FAM:MARC:PLAC"),""));
            //assertEquals("Note marc",miscRecord.getGeneralComment().getValue(), fam.getValue(new TagPath("FAM:MARC:NOTE"),""));

            assertEquals("Date marriage","ABT 1999", fam.getMarriageDate().getValue());

            assertEquals("Indi : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), fam.getHusband().getBirthDate().getValue());
            assertEquals("Indi : lieu naissance",mergeRecord.getIndi().getBirthResidence().getPlace(), fam.getHusband().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Indi : adresse naissance",mergeRecord.getIndi().getBirthResidence().getAddress(), fam.getHusband().getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            assertEquals("Indi : Note naissance",
                    "Date de naissance ava 1981 déduite de l'acte de contrat de mariage entre Fatherfirstname NEW_FATHERLASTNAME et Motherfirstname NEW_WIFEFATHERLASTNAME le 01/03/1999 (ville_marc, notaire_marc)",
                    fam.getHusband().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Indi : Profession",1, fam.getHusband().getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = fam.getHusband().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Indi : Profession",mergeRecord.getIndi().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Indi : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Indi : Lieu Profession",mergeRecord.getIndi().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Indi : adresse Profession",mergeRecord.getIndi().getResidence().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"),""));
            //assertEquals("Indi : Note Profession","Profession indiquée dans l'acte CM entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 ( ville_marc, notaire_marc) ",
            //        occupation.getValue(new TagPath("OCCU:NOTE"),""));

            indiParentFamily = fam.getHusband().getFamilyWhereBiologicalChild();
            assertNotSame("Mari parent marc date inchangé",mergeRecord.getIndi().getParentFamily().getMarriageDate().getValue(),  indiParentFamily.getMarriageDate().getValue());
            assertEquals("Mari parent marc comment",true,  indiParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            indiFather = indiParentFamily.getHusband();
            assertEquals("Mari pere nom",mergeRecord.getIndi().getFather().getLastName(), indiFather.getLastName());
            assertEquals("Mari pere prenom",mergeRecord.getIndi().getFather().getFirstName(),  indiFather.getFirstName());
            assertEquals("Mari pere profession",mergeRecord.getIndi().getFather().getOccupation(),  indiFather.getProperty("OCCU").getValue());
            assertEquals("Mari pere lieu profession",mergeRecord.getIndi().getFather().getResidence().getPlace(),  indiFather.getPropertyByPath("INDI:OCCU:PLAC").getValue());
            assertEquals("Mari pere adresse profession",mergeRecord.getIndi().getFather().getResidence().getAddress(),  indiFather.getPropertyByPath("INDI:OCCU:ADDR").getValue());
            indiMother = indiParentFamily.getWife();
            assertEquals("Mari mere nom",mergeRecord.getIndi().getMother().getLastName(), indiMother.getLastName());
            assertEquals("Mari mere prenom",mergeRecord.getIndi().getMother().getFirstName(),  indiMother.getFirstName());
            assertEquals("Mari mere profession",mergeRecord.getIndi().getMother().getOccupation(),  indiMother.getProperty("OCCU").getValue());
            assertEquals("Mari mere lieu profession",mergeRecord.getIndi().getMother().getResidence().getPlace(),  indiMother.getPropertyByPath("INDI:OCCU:PLAC").getValue());
            assertEquals("Mari mere adresse profession",mergeRecord.getIndi().getMother().getResidence().getAddress(),  indiMother.getPropertyByPath("INDI:OCCU:ADDR").getValue());

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : lieu naissance",mergeRecord.getWife().getBirthResidence().getPlace(), fam.getWife().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            //assertEquals("Wife : Note naissance",
            //        "Naissance 3 mar 1973 déduite de  l'acte CM entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 ( ville_marc, notaire_marc)",
            //        fam.getWife().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : Profession", 1, fam.getWife().getProperties(new TagPath("INDI:OCCU")).length);
            occupation = fam.getWife().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Wife : Profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Wife : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Wife : Lieu Profession",mergeRecord.getWife().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Wife : Adresse Profession",mergeRecord.getWife().getResidence().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"),""));
            //assertEquals("Wife : Note Profession",
            //        "Profession indiquée dans l'acte CM entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 ( ville_marc, notaire_marc) ",
            //        occupation.getValue(new TagPath("OCCU:NOTE"),""));

            wifeParentFamily = fam.getWife().getFamilyWhereBiologicalChild();
            assertEquals("Femme parent marc date",mergeRecord.getWife().getParentFamily().getMarriageDate().getValue(),  wifeParentFamily.getMarriageDate().getValue());
            assertEquals("Femme parent marc comment",true,  wifeParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            wifeFather = wifeParentFamily.getHusband();
            assertEquals("Femme pere nom",mergeRecord.getWife().getFather().getLastName(), wifeFather.getLastName());
            assertEquals("Femme pere prenom",mergeRecord.getWife().getFather().getFirstName(),  wifeFather.getFirstName());
            assertEquals("Femme pere profession",mergeRecord.getWife().getFather().getOccupation(),  wifeFather.getProperty("OCCU").getValue());
            assertEquals("Femme pere : Lieu Profession",mergeRecord.getWife().getFather().getResidence().getPlace(), wifeFather.getValue(new TagPath("INDI:OCCU:PLAC"),""));
            assertEquals("Femme pere : Adresse Profession",mergeRecord.getWife().getFather().getResidence().getAddress(), wifeFather.getValue(new TagPath("INDI:OCCU:ADDR"),""));
            wifeMother = wifeParentFamily.getWife();
            assertEquals("Femme mere nom",mergeRecord.getWife().getMother().getLastName(), wifeMother.getLastName());
            assertEquals("Femme mere prenom",mergeRecord.getWife().getMother().getFirstName(),  wifeMother.getFirstName());
            assertEquals("Femme mere profession",mergeRecord.getWife().getMother().getOccupation(),  wifeMother.getProperty("OCCU").getValue());
            assertEquals("Femme mere : Lieu Profession",mergeRecord.getWife().getMother().getResidence().getPlace(), wifeMother.getValue(new TagPath("INDI:OCCU:PLAC"),""));
            assertEquals("Femme mere : Adresse Profession",mergeRecord.getWife().getMother().getResidence().getAddress(), wifeMother.getValue(new TagPath("INDI:OCCU:ADDR"),""));

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    /**
     *
    */
    @Test
    public void testAddMarcCM1() {
        try {
            // Merge options
            PlaceFormatModel.getCurrentModel().savePreferences(1,2,3,4,5,6);

            Gedcom gedcom = TestUtility.createGedcom();
            Fam f1 = (Fam) gedcom.getEntity("F1");
            Property marriageProperty = f1.addProperty("MARR", "");
            marriageProperty.addProperty("DATE", "01 JAN 2000");


            RecordMisc miscRecord = createMiscMarcRecord("CM1");
            miscRecord.setFieldValue(FieldType.indiBirthAddress, "");
            String fileName = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, miscRecord);
//TestUtility.showMergeDialog(data, gedcom, null);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
            MergeRecord mergeRecord = mergeManager.getMergeRecord();

            Fam indiParentFamily;
            Indi indiFather;
            Indi indiMother;
            Fam wifeParentFamily;
            Indi wifeFather;
            Indi wifeMother;

            assertEquals("Nombre model",2,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

            Fam fam = (Fam) mergeManager.getProposalList1().getElementAt(0).getMainEntity();
            assertEquals("Lien marc vers source","", fam.getValue(new TagPath("FAM:MARC:SOUR"),""));
            assertEquals("Source marc","", fam.getValue(new TagPath("FAM:MARC:SOUR:PAGE"),""));
            assertEquals("Date marc", true, miscRecord.getField(FieldType.eventDate).equalsProperty(fam.getPropertyByPath("FAM:MARC:DATE")));

            assertEquals("Lieu marc",getRecordsInfoPlace().getValue(), fam.getValue(new TagPath("FAM:MARC:PLAC"),""));
            //assertEquals("Note marc",miscRecord.getGeneralComment().getValue(), fam.getValue(new TagPath("FAM:MARC:NOTE"),""));

            assertEquals("Date marriage","ABT 1999", fam.getMarriageDate().getValue());


            assertNotSame("Indi : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), fam.getHusband().getBirthDate().getValue());
            // le lieu et commentaire ne sont pas modifiés car la date de naissance du releve n'est pas plus precise
            assertNotSame("Indi : lieu naissance",mergeRecord.getIndi().getBirthResidence().getPlace(), fam.getHusband().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Indi : Note naissance","", fam.getHusband().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Indi : Profession",2, fam.getHusband().getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = fam.getHusband().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Indi : Profession",mergeRecord.getIndi().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Indi : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Indi : Lieu Profession",mergeRecord.getIndi().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Indi : Note Profession","Profession indiquée dans l'acte de contrat de mariage entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_marc, notaire_marc)",
                    occupation.getValue(new TagPath("OCCU:NOTE"),""));

            indiParentFamily = fam.getHusband().getFamilyWhereBiologicalChild();
            assertNotSame("Mari parent marc date inchangé",mergeRecord.getIndi().getParentFamily().getMarriageDate().getValue(),  indiParentFamily.getMarriageDate().getValue());
            assertEquals("Mari parent marc comment",true,  indiParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            indiFather = indiParentFamily.getHusband();
            assertEquals("Mari pere nom",mergeRecord.getIndi().getFather().getLastName(), indiFather.getLastName());
            assertEquals("Mari pere prenom",mergeRecord.getIndi().getFather().getFirstName(),  indiFather.getFirstName());
            assertEquals("Mari pere profession",mergeRecord.getIndi().getFather().getOccupation(),  indiFather.getProperty("OCCU").getValue());
            indiMother = indiParentFamily.getWife();
            assertEquals("Mari mere nom",mergeRecord.getIndi().getMother().getLastName(), indiMother.getLastName());
            assertEquals("Mari mere prenom",mergeRecord.getIndi().getMother().getFirstName(),  indiMother.getFirstName());
            assertEquals("Mari mere profession",mergeRecord.getIndi().getMother().getOccupation(),  indiMother.getProperty("OCCU").getValue());

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : lieu naissance",mergeRecord.getWife().getBirthResidence().getPlace(), fam.getWife().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Wife : Note naissance",
                    "Date de naissance 3 mar 1973 déduite de l'acte de contrat de mariage entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_marc, notaire_marc)",
                    fam.getWife().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : Profession",1, fam.getWife().getProperties(new TagPath("INDI:OCCU")).length);
            occupation = fam.getWife().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Wife : Profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Wife : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Wife : Lieu Profession",mergeRecord.getWife().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Wife : Note Profession",
                    "Profession indiquée dans l'acte de contrat de mariage entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_marc, notaire_marc)",
                    occupation.getValue(new TagPath("OCCU:NOTE"),""));

            wifeParentFamily = fam.getWife().getFamilyWhereBiologicalChild();
            assertEquals("Femme parent marc date",mergeRecord.getWife().getParentFamily().getMarriageDate().getValue(),  wifeParentFamily.getMarriageDate().getValue());
            assertEquals("Femme parent marc comment",true,  wifeParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            wifeFather = wifeParentFamily.getHusband();
            assertEquals("Femme pere nom",mergeRecord.getWife().getFather().getLastName(), wifeFather.getLastName());
            assertEquals("Femme pere prenom",mergeRecord.getWife().getFather().getFirstName(),  wifeFather.getFirstName());
            assertEquals("Femme pere profession",mergeRecord.getWife().getFather().getOccupation(),  wifeFather.getProperty("OCCU").getValue());
            wifeMother = wifeParentFamily.getWife();
            assertEquals("Femme mere nom",mergeRecord.getWife().getMother().getLastName(), wifeMother.getLastName());
            assertEquals("Femme mere prenom",mergeRecord.getWife().getMother().getFirstName(),  wifeMother.getFirstName());
            assertEquals("Femme mere profession",mergeRecord.getWife().getMother().getOccupation(),  wifeMother.getProperty("OCCU").getValue());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    /**
     *
    */
    @Test
    public void testAddMarcCM_insinuation() {
        try {
            // Merge options
            PlaceFormatModel.getCurrentModel().savePreferences(1,2,3,4,5,6);

            Gedcom gedcom = TestUtility.createGedcom();
            Fam f1 = (Fam) gedcom.getEntity("F1");
            Property marriageProperty = f1.addProperty("MARR", "");
            marriageProperty.addProperty("DATE", "01 JAN 2000");

            RecordMisc miscRecord = createMiscMarcRecord("CM1");
            miscRecord.setFieldValue(FieldType.secondDate, "11 JAN 2000");
            String fileName = "ville_marc.txt";
            MergeOptionPanel.SourceModel.getModel().add(fileName, gedcom.getEntity("SOUR", "S1").getPropertyDisplayValue("TITL"));

//TestUtility.showMergeDialog(getRecordsInfoPlace(), fileName, miscRecord, gedcom, null);
            MergeManager mergeManager = TestUtility.createMergeManager(getRecordsInfoPlace(), fileName, miscRecord, gedcom, null);

            assertEquals("Nombre model",2,mergeManager.getProposalList1().getSize());

            // je verifie les données affichées
            ProposalRuleList ruleList =  mergeManager.getProposalList1().getElementAt(0).getDisplayRuleList();
            //proposal.getDisplayRuleList().getValueAt(0, 0)
            assertEquals("source gedcom", gedcom.getEntity("SOUR", "S1").getPropertyDisplayValue("TITL"), ruleList.getValueAt(0, 3));
            assertEquals("source identifant", "S1" , ((MergeTableAction)ruleList.getValueAt(0, 4)).getText());

            // je copie les données
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

            Fam indiParentFamily;
            Indi indiFather;
            Indi indiMother;
            Fam wifeParentFamily;
            Indi wifeFather;
            Indi wifeMother;
            MergeRecord mergeRecord = mergeManager.getMergeRecord();

            Fam fam = (Fam) mergeManager.getProposalList1().getElementAt(0).getMainEntity();
            assertEquals("Insinuation type", mergeRecord.getInsinuationType(), fam.getValue(new TagPath("FAM:EVEN:TYPE"),""));
            assertEquals("Insinuation date", mergeRecord.getInsinuationDate().getValue(), fam.getValue(new TagPath("FAM:EVEN:DATE"),""));
            assertEquals("Insinuation place", mergeRecord.getEventPlace(), fam.getValue(new TagPath("FAM:EVEN:PLAC"),""));
            assertEquals("Insinuation addresse", "", fam.getValue(new TagPath("FAM:EVEN:ADDR"),""));
            assertEquals("Insinutation source","@S1@", fam.getValue(new TagPath("FAM:EVEN:SOUR"),""));
            assertEquals("Insinutation page", mergeRecord.getEventCote() + ", " + mergeRecord.getEventPage() , fam.getValue(new TagPath("FAM:EVEN:SOUR:PAGE"),""));
            assertEquals("Insinuation note", true, fam.getValue(new TagPath("FAM:EVEN:NOTE"),"").contains(mergeRecord.getEventComment(mergeManager.m_showFrenchCalendarDate)));

            assertEquals("marc type",  null, fam.getValue(new TagPath("FAM:MARC:TYPE"),null) );
            assertEquals("marc date",  mergeRecord.getEventDate().getValue(), fam.getValue(new TagPath("FAM:MARC:DATE"),"") );
            assertEquals("marc place", null, fam.getValue(new TagPath("FAM:MARC:PLAC"),null));
            assertEquals("marc source", null, fam.getValue(new TagPath("FAM:MARC:SOUR"), null));
            assertEquals("marc page",  null, fam.getValue(new TagPath("FAM:MARC:SOUR:PAGE"), null));
            assertEquals("marc note", mergeManager.m_helper.getReferenceNote(fam.getProperty(new TagPath("FAM:EVEN")), "DATE"), fam.getValue(new TagPath("FAM:MARC:NOTE"),""));
            assertEquals("Date marriage","ABT 1999", fam.getMarriageDate().getValue());

            assertNotSame("Indi : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), fam.getHusband().getBirthDate().getValue());
            // le lieu et commentaire ne sont pas modifiés car la date de naissance du releve n'est pas plus precise
            assertNotSame("Indi : lieu naissance",mergeRecord.getIndi().getBirthResidence().getPlace(), fam.getHusband().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Indi : Note naissance","", fam.getHusband().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Indi : Profession",2, fam.getHusband().getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = fam.getHusband().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Indi : Profession",mergeRecord.getIndi().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Indi : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Indi : Lieu Profession",mergeRecord.getIndi().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Indi : Note Profession","Profession indiquée dans l'acte de contrat de mariage entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_marc, notaire_marc)",
                    occupation.getValue(new TagPath("OCCU:NOTE"),""));

            indiParentFamily = fam.getHusband().getFamilyWhereBiologicalChild();
            assertNotSame("Mari parent marc date inchangé",mergeRecord.getIndi().getParentFamily().getMarriageDate().getValue(),  indiParentFamily.getMarriageDate().getValue());
            assertEquals("Mari parent marc comment",true,  indiParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            indiFather = indiParentFamily.getHusband();
            assertEquals("Mari pere nom",mergeRecord.getIndi().getFather().getLastName(), indiFather.getLastName());
            assertEquals("Mari pere prenom",mergeRecord.getIndi().getFather().getFirstName(),  indiFather.getFirstName());
            assertEquals("Mari pere profession",mergeRecord.getIndi().getFather().getOccupation(),  indiFather.getProperty("OCCU").getValue());
            indiMother = indiParentFamily.getWife();
            assertEquals("Mari mere nom",mergeRecord.getIndi().getMother().getLastName(), indiMother.getLastName());
            assertEquals("Mari mere prenom",mergeRecord.getIndi().getMother().getFirstName(),  indiMother.getFirstName());
            assertEquals("Mari mere profession",mergeRecord.getIndi().getMother().getOccupation(),  indiMother.getProperty("OCCU").getValue());

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : lieu naissance",mergeRecord.getWife().getBirthResidence().getPlace(), fam.getWife().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Wife : Note naissance",
                    "Date de naissance 3 mar 1973 déduite de l'acte de contrat de mariage entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_marc, notaire_marc)",
                    fam.getWife().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : Profession",1, fam.getWife().getProperties(new TagPath("INDI:OCCU")).length);
            occupation = fam.getWife().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Wife : Profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Wife : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Wife : Lieu Profession",mergeRecord.getWife().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Wife : Note Profession",
                    "Profession indiquée dans l'acte de contrat de mariage entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_marc, notaire_marc)",
                    occupation.getValue(new TagPath("OCCU:NOTE"),""));

            wifeParentFamily = fam.getWife().getFamilyWhereBiologicalChild();
            assertEquals("Femme parent marc date",mergeRecord.getWife().getParentFamily().getMarriageDate().getValue(),  wifeParentFamily.getMarriageDate().getValue());
            assertEquals("Femme parent marc comment",true,  wifeParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            wifeFather = wifeParentFamily.getHusband();
            assertEquals("Femme pere nom",mergeRecord.getWife().getFather().getLastName(), wifeFather.getLastName());
            assertEquals("Femme pere prenom",mergeRecord.getWife().getFather().getFirstName(),  wifeFather.getFirstName());
            assertEquals("Femme pere profession",mergeRecord.getWife().getFather().getOccupation(),  wifeFather.getProperty("OCCU").getValue());
            wifeMother = wifeParentFamily.getWife();
            assertEquals("Femme mere nom",mergeRecord.getWife().getMother().getLastName(), wifeMother.getLastName());
            assertEquals("Femme mere prenom",mergeRecord.getWife().getMother().getFirstName(),  wifeMother.getFirstName());
            assertEquals("Femme mere profession",mergeRecord.getWife().getMother().getOccupation(),  wifeMother.getProperty("OCCU").getValue());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }


    /**
     *
    */
    @Test
    public void testAddMarcCM_insinuation_with_selectedEntity() {
        try {
            // Merge options
            PlaceFormatModel.getCurrentModel().savePreferences(1,2,3,4,5,6);

            Gedcom gedcom = TestUtility.createGedcom();

            Fam fam1 = (Fam) gedcom.getEntity("F1");
            Property insinuationProperty = fam1.addProperty("EVEN", "");
            insinuationProperty.addProperty("TYPE", "Insinuation Contrat de mariage");
            insinuationProperty.addProperty("DATE", "11 JAN 2000" );
            insinuationProperty.addProperty("PLAC", getRecordsInfoPlace().toString());
            Property marcProperty = fam1.addProperty("MARC", "");
            marcProperty.addProperty("DATE", "1 MAR 1999" );
            Property marriageProperty = fam1.addProperty("MARR", "");
            marriageProperty.addProperty("DATE", "01 JAN 2000");

            RecordMisc miscRecord = createMiscMarcRecord("CM1");
            miscRecord.setFieldValue(FieldType.secondDate, insinuationProperty.getPropertyValue("DATE"));
            String fileName = "ville_marc.txt";
            MergeOptionPanel.SourceModel.getModel().add(fileName, gedcom.getEntity("SOUR", "S1").getPropertyDisplayValue("TITL"));

//TestUtility.showMergeDialog(getRecordsInfoPlace(), fileName, miscRecord, gedcom, fam1);
            MergeManager mergeManager = TestUtility.createMergeManager(getRecordsInfoPlace(), fileName, miscRecord, gedcom, fam1);

            assertEquals("Nombre model", 1,mergeManager.getProposalList1().getSize());

            // je verifie les données affichées
            ProposalRuleList ruleList =  mergeManager.getProposalList1().getElementAt(0).getDisplayRuleList();
            assertEquals("source gedcom", gedcom.getEntity("SOUR", "S1").getPropertyDisplayValue("TITL"), ruleList.getValueAt(0, 3));
            assertEquals("source identifant", "S1" , ((MergeTableAction)ruleList.getValueAt(0, 4)).getText());
            assertEquals("insinuation title gedcom", insinuationProperty.getPropertyValue("TYPE"), ruleList.getValueAt(3, 3));
            assertEquals("insinuation date gedcom", insinuationProperty.getPropertyValue("DATE"), ((PropertyDate) ruleList.getValueAt(4, 3)).getValue() );
            assertEquals("insinuation place gedcom", getRecordsInfoPlace().getValue(), ruleList.getValueAt(5, 3) );
            assertEquals("insinuation note gedcom", "", ruleList.getValueAt(6, 3) );

            assertEquals("marc title gedcom", Gedcom.getName(marcProperty.getTag()), ruleList.getValueAt(8, 3));
            assertEquals("marc date gedcom", marcProperty.getPropertyValue("DATE"), ((PropertyDate) ruleList.getValueAt(9, 3)).getValue() );

            // je copie les données
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

            Fam indiParentFamily;
            Indi indiFather;
            Indi indiMother;
            Fam wifeParentFamily;
            Indi wifeFather;
            Indi wifeMother;
            MergeRecord mergeRecord = mergeManager.getMergeRecord();

            Fam fam = (Fam) mergeManager.getProposalList1().getElementAt(0).getMainEntity();
            assertEquals("Insinuation type", mergeRecord.getInsinuationType(), fam.getValue(new TagPath("FAM:EVEN:TYPE"),""));
            assertEquals("Insinuation date", mergeRecord.getInsinuationDate().getValue(), fam.getValue(new TagPath("FAM:EVEN:DATE"),""));
            assertEquals("Insinuation place", mergeRecord.getEventPlace(), fam.getValue(new TagPath("FAM:EVEN:PLAC"),""));
            assertEquals("Insinuation addresse", "", fam.getValue(new TagPath("FAM:EVEN:ADDR"),""));
            assertEquals("Insinutation source","@S1@", fam.getValue(new TagPath("FAM:EVEN:SOUR"),""));
            assertEquals("Insinutation page", mergeRecord.getEventCote() + ", " + mergeRecord.getEventPage() , fam.getValue(new TagPath("FAM:EVEN:SOUR:PAGE"),""));
            assertEquals("Insinuation note", true, fam.getValue(new TagPath("FAM:EVEN:NOTE"),"").contains(mergeRecord.getEventComment(mergeManager.m_showFrenchCalendarDate)));

            assertEquals("marc type",  null, fam.getValue(new TagPath("FAM:MARC:TYPE"),null) );
            assertEquals("marc date",  mergeRecord.getEventDate().getValue(), fam.getValue(new TagPath("FAM:MARC:DATE"),"") );
            assertEquals("marc place", null, fam.getValue(new TagPath("FAM:MARC:PLAC"),null));
            assertEquals("marc source", null, fam.getValue(new TagPath("FAM:MARC:SOUR"), null));
            assertEquals("marc page",  null, fam.getValue(new TagPath("FAM:MARC:SOUR:PAGE"), null));
            assertEquals("marc note", null, fam.getValue(new TagPath("FAM:MARC:NOTE"),null));
            assertEquals("Date marriage","1 JAN 2000", fam.getMarriageDate().getValue());

            assertNotSame("Indi : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), fam.getHusband().getBirthDate().getValue());
            // le lieu et commentaire ne sont pas modifiés car la date de naissance du releve n'est pas plus precise
            assertNotSame("Indi : lieu naissance",mergeRecord.getIndi().getBirthResidence().getPlace(), fam.getHusband().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Indi : Note naissance","", fam.getHusband().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Indi : Profession",2, fam.getHusband().getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = fam.getHusband().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Indi : Profession",mergeRecord.getIndi().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Indi : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Indi : Lieu Profession",mergeRecord.getIndi().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Indi : Note Profession","Profession indiquée dans l'acte de contrat de mariage entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_marc, notaire_marc)",
                    occupation.getValue(new TagPath("OCCU:NOTE"),""));

            indiParentFamily = fam.getHusband().getFamilyWhereBiologicalChild();
            assertNotSame("Mari parent marc date inchangé",mergeRecord.getIndi().getParentFamily().getMarriageDate().getValue(),  indiParentFamily.getMarriageDate().getValue());
            assertEquals("Mari parent marc comment",true,  indiParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            indiFather = indiParentFamily.getHusband();
            assertEquals("Mari pere nom",mergeRecord.getIndi().getFather().getLastName(), indiFather.getLastName());
            assertEquals("Mari pere prenom",mergeRecord.getIndi().getFather().getFirstName(),  indiFather.getFirstName());
            assertEquals("Mari pere profession",mergeRecord.getIndi().getFather().getOccupation(),  indiFather.getProperty("OCCU").getValue());
            indiMother = indiParentFamily.getWife();
            assertEquals("Mari mere nom",mergeRecord.getIndi().getMother().getLastName(), indiMother.getLastName());
            assertEquals("Mari mere prenom",mergeRecord.getIndi().getMother().getFirstName(),  indiMother.getFirstName());
            assertEquals("Mari mere profession",mergeRecord.getIndi().getMother().getOccupation(),  indiMother.getProperty("OCCU").getValue());

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : lieu naissance",mergeRecord.getWife().getBirthResidence().getPlace(), fam.getWife().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Wife : Note naissance",
                    "Date de naissance 3 mar 1973 déduite de l'acte de contrat de mariage entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_marc, notaire_marc)",
                    fam.getWife().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : Profession",1, fam.getWife().getProperties(new TagPath("INDI:OCCU")).length);
            occupation = fam.getWife().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Wife : Profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Wife : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Wife : Lieu Profession",mergeRecord.getWife().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Wife : Note Profession",
                    "Profession indiquée dans l'acte de contrat de mariage entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_marc, notaire_marc)",
                    occupation.getValue(new TagPath("OCCU:NOTE"),""));

            wifeParentFamily = fam.getWife().getFamilyWhereBiologicalChild();
            assertEquals("Femme parent marc date",mergeRecord.getWife().getParentFamily().getMarriageDate().getValue(),  wifeParentFamily.getMarriageDate().getValue());
            assertEquals("Femme parent marc comment",true,  wifeParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            wifeFather = wifeParentFamily.getHusband();
            assertEquals("Femme pere nom",mergeRecord.getWife().getFather().getLastName(), wifeFather.getLastName());
            assertEquals("Femme pere prenom",mergeRecord.getWife().getFather().getFirstName(),  wifeFather.getFirstName());
            assertEquals("Femme pere profession",mergeRecord.getWife().getFather().getOccupation(),  wifeFather.getProperty("OCCU").getValue());
            wifeMother = wifeParentFamily.getWife();
            assertEquals("Femme mere nom",mergeRecord.getWife().getMother().getLastName(), wifeMother.getLastName());
            assertEquals("Femme mere prenom",mergeRecord.getWife().getMother().getFirstName(),  wifeMother.getFirstName());
            assertEquals("Femme mere profession",mergeRecord.getWife().getMother().getOccupation(),  wifeMother.getProperty("OCCU").getValue());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }



}
