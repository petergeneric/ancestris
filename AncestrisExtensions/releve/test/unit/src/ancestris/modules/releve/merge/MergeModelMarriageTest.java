package ancestris.modules.releve.merge;

import ancestris.modules.releve.IgnoreOtherTestMethod;
import ancestris.modules.releve.RecordTransferHandle;
import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.dnd.TransferableRecord;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.RecordInfoPlace;
import ancestris.modules.releve.model.RecordMarriage;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.Source;
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
public class MergeModelMarriageTest {
    @Rule
    public IgnoreOtherTestMethod rule = new IgnoreOtherTestMethod("");

    static public String getSourceTitle() {
        return "";
    }

    static public RecordInfoPlace getRecordsInfoPlace() {
        RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
        recordsInfoPlace.setValue("Paris","75000","","state","country");
        return recordsInfoPlace;
    }

    static public RecordInfoPlace getRecordsInfoPlaceVilleMariage() {
        RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
        recordsInfoPlace.setValue("ville_mariage","code_mariage","departement_mariage","region_mariage","pays_mariage");
        return recordsInfoPlace;
    }

    public static RecordMarriage createMarriageRecord(String id) {
        RecordMarriage record = new RecordMarriage();
        if ( id.equals("M1")) {
            record.setFieldValue(FieldType.eventDate, "01/03/1999");
            record.setFieldValue(FieldType.cote, "cote");
            record.setFieldValue(FieldType.generalComment, "generalcomment");
            record.setFieldValue(FieldType.freeComment,  "photo");
            record.setIndi("Fatherfirstname", "FATHERLASTNAME", "M", "indiage", "01/01/1970", "indiBirthplace", "indiBirthAddress", "indioccupation22", "indiResidence", "indiAddress", "indicomments");
            record.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indiMarriedResidence", "indiMarriedAddress", "indimarriedcomment", "false");
            record.setIndiFather("indifathername", "FATHERLASTNAME", "indifatheroccupation", "indiFatherResidence", "indiFatherAddress", "indifathercomment", "false", "70y");
            record.setIndiMother("indimothername", "MOTHERLASTNAME", "indimotheroccupation", "indiMotherResidence", "indiMotherAddress", "indimothercomment", "false", "72y");
            record.setWife("Motherfirstname", "WIFEFATHERLASTNAME", "F", "wifeage", "03/03/1973", "wifeplace", "wifeBirthAddress", "wifeoccupation", "wifeResidence",  "wifeAddress", "wifecomment");
            record.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifeMarriedResidence", "wifeMarriedAddress", "wifemarriedcomment", "false");
            record.setWifeFather("wifefathername", "WIFEFATHERLASTNAME", "wifefatheroccupation", "wiferFatherResidence", "wiferFatherAddress", "wifefathercomment", "false", "60y");
            record.setWifeMother("wifemothername", "WIFEMOTHERLASTNAME", "wifemotheroccupation", "wifeMotherResidence", "wifeMotherAddress", "wifemothercomment", "false", "62y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

        } else if ( id.equals("IndiWithoutAddress")) {
            record.setFieldValue(FieldType.eventDate, "01/03/1999");
            record.setFieldValue(FieldType.cote, "cote");
            record.setFieldValue(FieldType.generalComment, "generalcomment");
            record.setFieldValue(FieldType.freeComment,  "photo");
            record.setIndi("Fatherfirstname", "PREMIER", "M", "indiage", "01/01/1970", "indiBirthplace", "indiBirthAddress", "indioccupation22", "indiResidence", "", "indicomments");
            record.setIndiMarried("indimarriedname", "indimarriedlastname", "indimarriedoccupation", "indiMarriedResidence", "indiMarriedAddress", "indimarriedcomment", "false");
            record.setIndiFather("indifathername", "PREMIER", "indifatheroccupation", "indiFatherResidence", "indiFatherAddress", "indifathercomment", "false", "70y");
            record.setIndiMother("indimothername", "MOTHERLASTNAME", "indimotheroccupation", "indiMotherResidence", "indiMotherAddress", "indimothercomment", "false", "72y");
            record.setWife("Motherfirstname", "DEUXIEME", "F", "wifeage", "03/03/1973", "wifeplace", "wifeBirthAddress", "wifeoccupation", "wifeResidence",  "wifeAddress", "wifecomment");
            record.setWifeMarried("wifemarriedname", "wifemarriedlastname", "wifemarriedoccupation", "wifeMarriedResidence", "wifeMarriedAddress", "wifemarriedcomment", "false");
            record.setWifeFather("wifefathername", "DEUXIEME", "wifefatheroccupation", "wiferFatherResidence", "wiferFatherAddress", "wifefathercomment", "false", "60y");
            record.setWifeMother("wifemothername", "WIFEMOTHERLASTNAME", "wifemotheroccupation", "wifeMotherResidence", "wifeMotherAddress", "wifemothercomment", "false", "62y");
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
    public void testAddMarriage() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            String fileName = "releve paris.txt";
            String sourceTitle = ((Source)gedcom.getEntity("S1") ).getTitle();
            MergeOptionPanel.SourceModel.getModel().add(fileName, sourceTitle);
            RecordMarriage marriageRecord = createMarriageRecord("M1");

            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlaceVilleMariage(),fileName, marriageRecord);

            Fam indiParentFamily;
            Indi indiFather;
            Indi indiMother;
            Fam wifeParentFamily;
            Indi wifeFather;
            Indi wifeMother;
//TestUtility.showMergeDialog(data, gedcom, null);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
            MergeRecord mergeRecord = mergeManager.getMergeRecord();
            assertEquals("Nombre model",2,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

            Fam fam = (Fam) mergeManager.getProposalList1().getElementAt(0).getMainEntity();
            assertEquals("Lien mariage vers source","@S1@", fam.getValue(new TagPath("FAM:MARR:SOUR"),""));
            assertEquals("Source mariage","S1", gedcom.getEntity(fam.getValue(new TagPath("FAM:MARR:SOUR"),"").replaceAll("@", "")).getId());
            assertEquals("Date mariage",mergeRecord.getEventDate().getValue(), fam.getMarriageDate().getValue());
            assertEquals("Lieu mariage",mergeRecord.getEventPlace(), fam.getValue(new TagPath("FAM:MARR:PLAC"),""));
            assertEquals("Note mariage",1534, fam.getValue(new TagPath("FAM:MARR:NOTE"),"").length());

            assertEquals("Indi : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), fam.getHusband().getBirthDate().getValue());
            // le lieu et commentaire de naissance sont modifiés car ils sont plus précis , m^ême si la date de naissance du releve n'est pas plus precise
            assertEquals("Indi : lieu naissance",mergeRecord.getIndi().getBirthResidence().getPlace(), fam.getHusband().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Indi : adresse naissance",mergeRecord.getIndi().getBirthResidence().getAddress(), fam.getHusband().getValue(new TagPath("INDI:BIRT:ADDR"),""));
            assertNotSame("Indi : Note naissance",  "", fam.getHusband().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Indi : Profession",2, fam.getHusband().getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = fam.getHusband().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Indi : Profession",mergeRecord.getIndi().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Indi : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Indi : Lieu Profession",mergeRecord.getIndi().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Indi : adresse Profession",mergeRecord.getIndi().getResidence().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"),""));
            assertEquals("Indi : Note Profession","Profession indiquée dans l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                    occupation.getValue(new TagPath("OCCU:NOTE"),""));

            assertEquals("Indi : Date deces",mergeRecord.getIndi().getDeathDate().getValue(), fam.getHusband().getDeathDate().getValue());
            assertEquals("Indi : lieu deces", null, fam.getHusband().getValue(new TagPath("INDI:DEAT:PLAC"), null));


            indiParentFamily = fam.getHusband().getFamilyWhereBiologicalChild();
            assertNotSame("IndiParent mariage date inchangé",mergeRecord.getIndi().getParentFamily().getMarriageDate().getValue(),  indiParentFamily.getMarriageDate().getValue());
            assertEquals("IndiParent mariage comment",true,  indiParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            indiFather = indiParentFamily.getHusband();
            assertEquals("IndiFather: nom",mergeRecord.getIndi().getFather().getLastName(), indiFather.getLastName());
            assertEquals("IndiFather: prenom",mergeRecord.getIndi().getFather().getFirstName(),  indiFather.getFirstName());
            assertEquals("IndiFather: profession",mergeRecord.getIndi().getFather().getOccupation(),  indiFather.getValue(new TagPath("INDI:OCCU"),""));
            assertEquals("IndiFather: Lieu Profession",mergeRecord.getIndi().getFather().getResidence().getPlace(), indiFather.getValue(new TagPath("INDI:OCCU:PLAC"),""));
            assertEquals("IndiFather: adresse Profession",mergeRecord.getIndi().getFather().getResidence().getAddress(), indiFather.getValue(new TagPath("INDI:OCCU:ADDR"),""));
            indiMother = indiParentFamily.getWife();
            assertEquals("IndiMother: nom",mergeRecord.getIndi().getMother().getLastName(), indiMother.getLastName());
            assertEquals("IndiMother: prenom",mergeRecord.getIndi().getMother().getFirstName(),  indiMother.getFirstName());
            assertEquals("IndiMother: profession",mergeRecord.getIndi().getMother().getOccupation(),  indiMother.getValue(new TagPath("INDI:OCCU"),""));
            assertEquals("IndiMother: Lieu Profession",mergeRecord.getIndi().getMother().getResidence().getPlace(), indiMother.getValue(new TagPath("INDI:OCCU:PLAC"),""));
            assertEquals("IndiMother: adresse Profession",mergeRecord.getIndi().getMother().getResidence().getAddress(), indiMother.getValue(new TagPath("INDI:OCCU:ADDR"),""));

            assertEquals("Wife: Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife: lieu naissance",mergeRecord.getWife().getBirthResidence().getPlace(), fam.getWife().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Wife: adresse naissance",mergeRecord.getWife().getBirthResidence().getAddress(), fam.getWife().getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            assertEquals("Wife: Note naissance",
                    "Date de naissance 3 mar 1973 déduite de l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                    fam.getWife().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Wife: Profession",1, fam.getWife().getProperties(new TagPath("INDI:OCCU")).length);
            occupation = fam.getWife().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Wife : Profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Wife : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Wife : Lieu Profession",mergeRecord.getWife().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Wife : Adresse Profession",mergeRecord.getWife().getResidence().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"),""));
            assertEquals("Wife : Note Profession",
                    "Profession indiquée dans l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                    occupation.getValue(new TagPath("OCCU:NOTE"),""));

            assertEquals("Wife : Date deces",mergeRecord.getWife().getDeathDate().getValue(), fam.getWife().getDeathDate().getValue());
            assertEquals("Wife : lieu deces", null, fam.getWife().getValue(new TagPath("INDI:DEAT:PLAC"), null));

            wifeParentFamily = fam.getWife().getFamilyWhereBiologicalChild();
            assertEquals("Femme parent mariage date",mergeRecord.getWife().getParentFamily().getMarriageDate().getValue(),  wifeParentFamily.getMarriageDate().getValue());
            assertEquals("Femme parent mariage comment",true,  wifeParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            wifeFather = wifeParentFamily.getHusband();
            assertEquals("Femme pere nom",mergeRecord.getWife().getFather().getLastName(), wifeFather.getLastName());
            assertEquals("Femme pere prenom",mergeRecord.getWife().getFather().getFirstName(),  wifeFather.getFirstName());
            assertEquals("Femme pere profession",mergeRecord.getWife().getFather().getOccupation(),  wifeFather.getProperty("OCCU").getValue());
            assertEquals("WifeFather: Lieu Profession",mergeRecord.getWife().getFather().getResidence().getPlace(), wifeFather.getValue(new TagPath("INDI:OCCU:PLAC"),""));
            assertEquals("WifeFather: adresse Profession",mergeRecord.getWife().getFather().getResidence().getAddress(), wifeFather.getValue(new TagPath("INDI:OCCU:ADDR"),""));
            wifeMother = wifeParentFamily.getWife();
            assertEquals("Femme mere nom",mergeRecord.getWife().getMother().getLastName(), wifeMother.getLastName());
            assertEquals("Femme mere prenom",mergeRecord.getWife().getMother().getFirstName(),  wifeMother.getFirstName());
            assertEquals("Femme mere profession",mergeRecord.getWife().getMother().getOccupation(),  wifeMother.getProperty("OCCU").getValue());
            assertEquals("WifeMother: Lieu Profession",mergeRecord.getWife().getMother().getResidence().getPlace(), wifeMother.getValue(new TagPath("INDI:OCCU:PLAC"),""));
            assertEquals("WifeMother: adresse Profession",mergeRecord.getWife().getMother().getResidence().getAddress(), wifeMother.getValue(new TagPath("INDI:OCCU:ADDR"),""));


        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testAddMarriageWithoutIndiAddress() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            String fileName = "releve paris.txt";
            String sourceTitle = "BMS Paris";
            MergeOptionPanel.SourceModel.getModel().add(fileName, sourceTitle);
            Record record = createMarriageRecord("IndiWithoutAddress");
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlaceVilleMariage(),fileName, record);

//TestUtility.showMergeDialog(data, gedcom, null);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
            MergeRecord mergeRecord = mergeManager.getMergeRecord();
            assertEquals("Nombre model",1,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

            Fam indiParentFamily;
            Indi indiFather;
            Indi indiMother;
            Fam wifeParentFamily;
            Indi wifeFather;
            Indi wifeMother;

            Fam fam = (Fam) mergeManager.getProposalList1().getElementAt(0).getMainEntity();
            assertEquals("Lien mariage vers source","@S1@", fam.getValue(new TagPath("FAM:MARR:SOUR"),""));
            assertEquals("Source mariage","S1", gedcom.getEntity(fam.getValue(new TagPath("FAM:MARR:SOUR"),"").replaceAll("@", "")).getId());
            assertEquals("Date mariage",mergeRecord.getEventDate().getValue(), fam.getMarriageDate().getValue());
            assertEquals("Lieu mariage",mergeRecord.getEventPlace(), fam.getValue(new TagPath("FAM:MARR:PLAC"),""));
            //assertEquals("Note mariage",1534, fam.getValue(new TagPath("FAM:MARR:NOTE"),"").length());

            assertEquals("Indi : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), fam.getHusband().getBirthDate().getValue());
            // le lieu et commentaire ne sont pas modifiés car la date de naissance du releve n'est pas plus precise
            assertEquals("Indi : lieu naissance",mergeRecord.getIndi().getBirthResidence().getPlace(), fam.getHusband().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Indi : adresse naissance",mergeRecord.getIndi().getBirthResidence().getAddress(), fam.getHusband().getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            //assertEquals("Indi : Note naissance","", fam.getHusband().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Indi : Profession",1, fam.getHusband().getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = fam.getHusband().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Indi : Profession",mergeRecord.getIndi().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Indi : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Indi : Lieu Profession",mergeRecord.getIndi().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Indi : Adresse Profession",mergeRecord.getIndi().getResidence().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"),""));
            assertEquals("Indi : Note Profession","Profession indiquée dans l'acte de mariage de Fatherfirstname PREMIER et Motherfirstname DEUXIEME le 01/03/1999 (ville_mariage)",
                    occupation.getValue(new TagPath("OCCU:NOTE"),""));

            indiParentFamily = fam.getHusband().getFamilyWhereBiologicalChild();
            assertNotSame("Mari parent mariage date inchangé",mergeRecord.getIndi().getParentFamily().getMarriageDate().getValue(),  indiParentFamily.getMarriageDate().getValue());
            assertEquals("Mari parent mariage comment",true,  indiParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            indiFather = indiParentFamily.getHusband();
            assertEquals("IndiFather nom",mergeRecord.getIndi().getFather().getLastName(), indiFather.getLastName());
            assertEquals("IndiFather prenom",mergeRecord.getIndi().getFather().getFirstName(),  indiFather.getFirstName());
            assertEquals("IndiFather profession",mergeRecord.getIndi().getFather().getOccupation(),  indiFather.getProperty("OCCU").getValue());
            indiMother = indiParentFamily.getWife();
            assertEquals("IndiMother nom",mergeRecord.getIndi().getMother().getLastName(), indiMother.getLastName());
            assertEquals("IndiMother prenom",mergeRecord.getIndi().getMother().getFirstName(),  indiMother.getFirstName());
            assertEquals("IndiMother profession",mergeRecord.getIndi().getMother().getOccupation(),  indiMother.getProperty("OCCU").getValue());

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : lieu naissance",mergeRecord.getWife().getBirthResidence().getPlace(), fam.getWife().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Wife : Note naissance",
                    "Date de naissance 3 mar 1973 déduite de l'acte de mariage de Fatherfirstname PREMIER et Motherfirstname DEUXIEME le 01/03/1999 (ville_mariage)",
                    fam.getWife().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : Profession",1, fam.getWife().getProperties(new TagPath("INDI:OCCU")).length);
            occupation = fam.getWife().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Wife : Profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Wife : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Wife : Lieu Profession",mergeRecord.getWife().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Wife : Adresse Profession",mergeRecord.getWife().getResidence().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"),""));
            assertEquals("Wife : Note Profession",
                    "Profession indiquée dans l'acte de mariage de Fatherfirstname PREMIER et Motherfirstname DEUXIEME le 01/03/1999 (ville_mariage)",
                    occupation.getValue(new TagPath("OCCU:NOTE"),""));

            wifeParentFamily = fam.getWife().getFamilyWhereBiologicalChild();
            assertEquals("Femme parent mariage date",mergeRecord.getWife().getParentFamily().getMarriageDate().getValue(),  wifeParentFamily.getMarriageDate().getValue());
            assertEquals("Femme parent mariage comment",true,  wifeParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            wifeFather = wifeParentFamily.getHusband();
            assertEquals("Femme pere nom",mergeRecord.getWife().getFather().getLastName(), wifeFather.getLastName());
            assertEquals("Femme pere prenom",mergeRecord.getWife().getFather().getFirstName(),  wifeFather.getFirstName());
            assertEquals("Femme pere profession",mergeRecord.getWife().getFather().getOccupation(),  wifeFather.getProperty("OCCU").getValue());
            wifeMother = wifeParentFamily.getWife();
            assertEquals("Femme mere nom",mergeRecord.getWife().getMother().getLastName(), wifeMother.getLastName());
            assertEquals("Femme mere prenom",mergeRecord.getWife().getMother().getFirstName(),  wifeMother.getFirstName());
            assertEquals("Femme mere profession",mergeRecord.getWife().getMother().getOccupation(),  wifeMother.getProperty("OCCU").getValue());


        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * testSaveDataComment
     */
    @Test
    public void testUpdateMariageAndFatherBirthDate() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            // je change la date de naissance du pere pour permettre la modification
            ((Indi)gedcom.getEntity("I1")).getBirthDate().setValue("BEF 1971");

            String fileName = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlaceVilleMariage(),fileName, createMarriageRecord("M1"));
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
            MergeRecord mergeRecord = mergeManager.getMergeRecord();


            assertEquals("Nombre model",2,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

            Fam fam = (Fam) mergeManager.getProposalList1().getElementAt(0).getMainEntity();
            assertEquals("Lien mariage vers source","", fam.getValue(new TagPath("FAM:MARR:SOUR"),""));
            //assertEquals("Source mariage","S00004", gedcom.getEntity(fam.getValue(new TagPath("FAM:MARR:SOUR"),"").replaceAll("@", "")).getId());
            assertEquals("Date mariage",mergeRecord.getEventDate().getValue(), fam.getMarriageDate().getValue());
            assertEquals("Lieu mariage",mergeRecord.getEventPlace(), fam.getValue(new TagPath("FAM:MARR:PLAC"),""));
            assertEquals("Note mariage",
                "Date de l'acte: 01/03/1999"+"\n"
                +"Epoux: Fatherfirstname FATHERLASTNAME, né le 01/01/1970 à indiBirthAddress, indiBirthplace, indioccupation22, domicile indiAddress, indiResidence, indicomments"+"\n"
                +"Ex conjoint époux: indimarriedname indimarriedlastname, Vivant, indimarriedoccupation, domicile indiMarriedAddress, indiMarriedResidence, indimarriedcomment"+"\n"
                +"Père époux: indifathername FATHERLASTNAME, 70 années, Vivant, indifatheroccupation, domicile indiFatherAddress, indiFatherResidence, indifathercomment"+"\n"
                +"Mère époux: indimothername MOTHERLASTNAME, 72 années, Vivant, indimotheroccupation, domicile indiMotherAddress, indiMotherResidence, indimothercomment"+"\n"
                +"Epouse: Motherfirstname WIFEFATHERLASTNAME, né le 03/03/1973 à wifeBirthAddress, wifeplace, wifeoccupation, domicile wifeAddress, wifeResidence, wifecomment"+"\n"
                +"Ex conjoint épouse: wifemarriedname wifemarriedlastname, Vivant, wifemarriedoccupation, domicile wifeMarriedAddress, wifeMarriedResidence, wifemarriedcomment"+"\n"
                +"Père épouse: wifefathername WIFEFATHERLASTNAME, 60 années, Vivant, wifefatheroccupation, domicile wiferFatherAddress, wiferFatherResidence, wifefathercomment"+"\n"
                +"Mère épouse: wifemothername WIFEMOTHERLASTNAME, 62 années, Vivant, wifemotheroccupation, domicile wifeMotherAddress, wifeMotherResidence, wifemothercomment"+"\n"
                +"Témoin(s): w1firstname w1lastname, w1occupation, w1comment, w2firstname w2lastname, w2occupation, w2comment, w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment"+"\n"
                +"Commentaire général: generalcomment"+"\n"
                +"Cote: cote, photo",
                fam.getValue(new TagPath("FAM:MARR:NOTE"),""));

            assertEquals("Indi : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), fam.getHusband().getBirthDate().getValue());
            assertEquals("Indi : lieu naissance",mergeRecord.getIndi().getBirthResidence().getPlace(), fam.getHusband().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Indi : Note naissance",
                "Date de naissance 1 jan 1970 déduite de l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                fam.getHusband().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Indi : Profession",2, fam.getHusband().getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = fam.getHusband().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Indi : Profession",mergeRecord.getIndi().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Indi : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Indi : Lieu Profession",mergeRecord.getIndi().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Indi : Note Profession",
                "Profession indiquée dans l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                occupation.getValue(new TagPath("OCCU:NOTE"),""));

            // épouse
            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : lieu naissance",mergeRecord.getWife().getBirthResidence().getPlace(), fam.getWife().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Wife : Note naissance",
                "Date de naissance 3 mar 1973 déduite de l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                fam.getWife().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : Profession",1, fam.getWife().getProperties(new TagPath("INDI:OCCU")).length);
            occupation = fam.getWife().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Wife : Profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Wife : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Wife : Lieu Profession",mergeRecord.getWife().getPlace(),     occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Wife : Note Profession",
                "Profession indiquée dans l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                occupation.getValue(new TagPath("OCCU:NOTE"),""));


        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * test l'ajout de l'epouse dans un mariage deja existant
     */
     @Test
    public void testUpdateMarriageWithoutWife() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();

            // j'ajoute un mariage sans epouse
            Indi husband = (Indi) gedcom.createEntity(Gedcom.INDI, "I30");
            husband.setName("PerePrenomAA", "PERENOM_AA");
            husband.setSex(PropertySex.MALE);
            husband.addProperty("OCCU", "profession1");

            Indi child1 = (Indi) gedcom.createEntity(Gedcom.INDI, "I31");
            child1.setName("FilsPrenomBB", "PERENOM_AA");
            child1.setSex(PropertySex.FEMALE);
            child1.addProperty("OCCU", "profession1");

            Fam family = (Fam) gedcom.createEntity(Gedcom.FAM, "F30");
            family.setHusband(husband);
            family.addChild(child1);

            // je cree le relevé contenant le nom de la mere qui n'existe pas dans le gedcom
            RecordMarriage record = new RecordMarriage();
            record.setFieldValue(FieldType.eventDate, "01/03/1999");
            record.setFieldValue(FieldType.cote, "cote");
            record.setFieldValue(FieldType.generalComment, "generalcomment");
            record.setFieldValue(FieldType.freeComment,  "photo");
            record.setIndi("PerePrenomAA", "PERENOM_AA", "M", "30y", "02/02/1970", "indiplace", "indiBirthAddress", "indioccupation", "indiResidence", "indiAddress", "indicomments");
            record.setWife("MerePrenomBB", "MERENOM_BB", "F", "28y", "03/03/1973", "wifeplace", "wifeBirthAddress", "wifeoccupation", "wifeResidence", "wifeAddress", "wifecomment");
            record.setWifeFather("PerePrenom", "PERENOM_BB", "wifefatheroccupation", "wiferFatherResidence", "wiferFatherAddress", "wifefathercomment", "wifefatherdead", "60y");
            record.setWifeMother("Mere prenom CC", "MERENOM_CC", "wifemotheroccupation", "wifeMotherResidence", "wiferMotherAddress", "wifemothercomment", "wifemotherdead", "62y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

            String fileName = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlaceVilleMariage(),fileName, record);

            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
            MergeRecord mergeRecord = mergeManager.getMergeRecord();

            assertEquals("Nombre de propositions",2,mergeManager.getProposalList1().getSize());
            // je verifie la premiere proposition
            String summary = mergeManager.getProposalList1().getElementAt(0).getSummary(false);
            //assertEquals("Proposition", "Modifier le mariage PERENOM_AA, PerePrenomAA x ---", summary);

            // j'applique la premiere proposition
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

            Fam fam = (Fam) gedcom.getEntity("F30");
            assertEquals("Date mariage",mergeRecord.getEventDate().getValue(), fam.getMarriageDate().getValue());
            assertEquals("Lieu mariage",mergeRecord.getEventPlace(), fam.getValue(new TagPath("FAM:MARR:PLAC"),""));
//            assertEquals("Note mariage",GedcomOptions.getInstance().replaceSpaceSeparatorWithComma()? 877 : 879, fam.getValue(new TagPath("FAM:MARR:NOTE"),"").length());

            assertNotSame("Indi : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), fam.getHusband().getBirthDate().getValue());
            assertEquals("Indi : lieu naissance",mergeRecord.getIndi().getBirthResidence().getPlace(), fam.getHusband().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Indi : Note naissance",141, fam.getHusband().getValue(new TagPath("INDI:BIRT:NOTE"), "").length());

            assertEquals("Indi : Profession",2, fam.getHusband().getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = fam.getHusband().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Indi : Profession",mergeRecord.getIndi().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Indi : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Indi : Lieu Profession",mergeRecord.getIndi().getPlace(),     occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Indi : Note Profession",126, occupation.getValue(new TagPath("OCCU:NOTE"),"").length());

            assertEquals("Wife : Nom",mergeRecord.getWife().getLastName(), fam.getWife().getLastName());
            assertEquals("Wife : Prénom",mergeRecord.getWife().getFirstName(), fam.getWife().getFirstName());
            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : lieu naissance",mergeRecord.getWife().getBirthResidence().getPlace(), fam.getWife().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Wife : Note naissance",141, fam.getWife().getValue(new TagPath("INDI:BIRT:NOTE"), "").length());

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : Profession",1, fam.getWife().getProperties(new TagPath("INDI:OCCU")).length);
            occupation = fam.getWife().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Wife : Profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Wife : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Wife : Lieu Profession",mergeRecord.getWife().getPlace(),     occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Wife : Note Profession",126, occupation.getValue(new TagPath("OCCU:NOTE"),"").length());

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }


    /**
     * modification de la date de deces du pere
     */
       @Test
    public void testUpdateMariageAndFatherDeathDate() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            RecordMarriage mariageRecord = createMarriageRecord("M1");
            // je change la date de deces du pere et de la mere
            mariageRecord.setIndiFather("indifathername", "FATHERLASTNAME", "indifatheroccupation", "indiFatherResidence", "indiFatherAddress", "indifathercomment", "true", "");
            mariageRecord.setIndiMother("indimothername", "MOTHERLASTNAME", "indimotheroccupation", "indiMotherResidence", "indiMotherAddress", "indimothercomment", "true", "");
            String fileName = "Etat civil Paris.txt";
            String sourceTitle = "Etat civil Paris";
            MergeOptionPanel.SourceModel.getModel().add(fileName, sourceTitle);
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlaceVilleMariage(), fileName, mariageRecord);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
            MergeRecord mergeRecord = mergeManager.getMergeRecord();

            assertEquals("Nombre model",2,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

            Fam fam = (Fam) mergeManager.getProposalList1().getElementAt(0).getMainEntity();
            assertEquals("Lien mariage vers source","@S2@", fam.getValue(new TagPath("FAM:MARR:SOUR"),""));
            assertEquals("Source mariage","S2", gedcom.getEntity(fam.getValue(new TagPath("FAM:MARR:SOUR"),"").replaceAll("@", "")).getId());

            assertEquals("IndiMother date deces","BET 1970 AND 1999", mergeRecord.getIndi().getMother().getDeathDate().getValue());

            Fam indiParentFamily = fam.getHusband().getFamilyWhereBiologicalChild();
            Indi indiFather = indiParentFamily.getHusband();
            assertEquals("IndiFather date deces","BET 1969 AND 1999", indiFather.getDeathDate().getValue());
            Indi indiMother = indiParentFamily.getWife();
            assertEquals("IndiMother date deces","BET 1970 AND 1999", indiMother.getDeathDate().getValue());


        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * modification de la date de deces du pere
     * @throws java.lang.Exception
     */
    @Test
    public void testMariage_wifeParent() throws Exception {
        Gedcom gedcom = TestUtility.createGedcomEmpty();
        Property property;

        //je cree les parents de l'epouse
        Indi wifeFather = (Indi) gedcom.createEntity(Gedcom.INDI);
        wifeFather.setName("Jean", "WIFENAME");
        wifeFather.setSex(PropertySex.MALE);
        property = wifeFather.addProperty("BIRT", "");
        property.addProperty("DATE", "BEF 1758");

        Indi wifeMother = (Indi) gedcom.createEntity(Gedcom.INDI);
        wifeMother.setName("Jeanne", "WIFEMOTHERNAME");
        wifeMother.setSex(PropertySex.FEMALE);
        property = wifeMother.addProperty("BIRT", "");
        property.addProperty("DATE", "BEF 1758");
        property = wifeMother.addProperty("DEAT", "");
        property.addProperty("DATE", "BET 1776 AND 1802");

        Fam parentFamily = (Fam) gedcom.createEntity(Gedcom.FAM);
        parentFamily.setHusband(wifeFather);
        parentFamily.setWife(wifeMother);
        property = parentFamily.addProperty("MARR", "");
        property.addProperty("DATE", "BEF 1776");

        RecordMarriage record = new RecordMarriage();
        record.setFieldValue(FieldType.eventDate, "23/01/1803");
        record.setIndi("Joseph", "HUSBANDNAME", "M", "26a", "08/05/1778", "indiBirthplace", "indiBirthAddress", "Laboureur", "indiResidence", "", "indicomments");
        record.setIndiFather("Bernard", "HUSBANDNAME", "indifatheroccupation", "indiFatherResidence", "indiFatherAddress", "indifathercomment", "true", "");
        record.setIndiMother("Jeanne", "INDIMOTHERNAME", "indimotheroccupation", "indiMotherResidence", "indiMotherAddress", "indimothercomment", "", "");
        record.setWife("Catherine", "WIFENAME", "F", "19a", "10/05/1783", "wifeplace", "wifeBirthAddress", "wifeoccupation", "wifeResidence", "wifeAddress", "wifecomment");
        record.setWifeFather("Jean", "WIFENAME", "wifefatheroccupation", "wiferFatherResidence", "wiferFatherAddress", "wifefathercomment", "", "");
        record.setWifeMother("Jeanne", "WIFEMOTHERNAME", "wifemotheroccupation", "wifeMotherResidence", "wifeMotherAddress", "wifemothercomment", "true", "");
        String fileName = "Etat civil Paris.txt";
        String sourceTitle = "Etat civil Paris";
        MergeOptionPanel.SourceModel.getModel().add(fileName, sourceTitle);
        TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlaceVilleMariage(), fileName, record);
//TestUtility.showMergeDialog(data, gedcom, null);
        MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
        MergeRecord mergeRecord = mergeManager.getMergeRecord();

        assertEquals("Nombre model", 2, mergeManager.getProposalList1().getSize());
        mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

        Fam fam = (Fam) mergeManager.getProposalList1().getElementAt(0).getMainEntity();

        assertEquals("Wife parent family ID", parentFamily.getId(), fam.getWife().getFamilyWhereBiologicalChild().getId());
        assertEquals("Wife father ID", wifeFather.getId(), fam.getWife().getBiologicalFather().getId());
        assertEquals("Wife mother ID", wifeMother.getId(), fam.getWife().getBiologicalMother().getId());

    }
}
