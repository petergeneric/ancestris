package ancestris.modules.releve.merge;

import ancestris.modules.releve.RecordTransferHandle;
import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.dnd.TransferableRecord;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.RecordInfoPlace;
import ancestris.modules.releve.model.RecordMarriage;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomOptions;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author Michel
 */
public class MergeModelMarriageTest extends TestCase {

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
            String sourceTitle = "BMS Paris";
            MergeOptionPanel.SourceModel.getModel().add(fileName, sourceTitle);
            
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlaceVilleMariage(),fileName, createMarriageRecord("M1"));
            MergeRecord mergeRecord = new MergeRecord(data);
                        
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

            Fam fam = (Fam) gedcom.getEntity("F4");
            assertEquals("Lien mariage vers source","@S1@", fam.getValue(new TagPath("FAM:MARR:SOUR"),""));
            assertEquals("Source mariage","S1", gedcom.getEntity(fam.getValue(new TagPath("FAM:MARR:SOUR"),"").replaceAll("@", "")).getId());
            assertEquals("Date mariage",mergeRecord.getEventDate().getValue(), fam.getMarriageDate().getValue());
            assertEquals("Lieu mariage",mergeRecord.getEventPlace(), fam.getValue(new TagPath("FAM:MARR:PLAC"),""));
            assertEquals("Note mariage",1534, fam.getValue(new TagPath("FAM:MARR:NOTE"),"").length());

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
            assertEquals("Indi : Note Profession","Profession indiquée dans l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                    occupation.getValue(new TagPath("OCCU:NOTE"),""));

            indiParentFamily = fam.getHusband().getFamilyWhereBiologicalChild();
            assertNotSame("Mari parent mariage date inchangé",mergeRecord.getIndi().getParentMarriageDate().getValue(),  indiParentFamily.getMarriageDate().getValue());
            assertEquals("Mari parent mariage comment",true,  indiParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
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
                    "Date de naissance 3 mar 1973 déduite de l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                    fam.getWife().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : Profession",1, fam.getWife().getProperties(new TagPath("INDI:OCCU")).length);
            occupation = fam.getWife().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Wife : Profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Wife : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Wife : Lieu Profession",mergeRecord.getWife().getResidence(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Wife : Note Profession",
                    "Profession indiquée dans l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                    occupation.getValue(new TagPath("OCCU:NOTE"),""));

            wifeParentFamily = fam.getWife().getFamilyWhereBiologicalChild();
            assertEquals("Femme parent mariage date",mergeRecord.getWife().getParentMarriageDate().getValue(),  wifeParentFamily.getMarriageDate().getValue());
            assertEquals("Femme parent mariage comment",true,  wifeParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            wifeFather = wifeParentFamily.getHusband();
            assertEquals("Femme pere nom",mergeRecord.getWife().getFatherLastName(), wifeFather.getLastName());
            assertEquals("Femme pere prenom",mergeRecord.getWife().getFatherFirstName(),  wifeFather.getFirstName());
            assertEquals("Femme pere profession",mergeRecord.getWife().getFatherOccupation(),  wifeFather.getProperty("OCCU").getValue());
            wifeMother = wifeParentFamily.getWife();
            assertEquals("Femme mere nom",mergeRecord.getWife().getMotherLastName(), wifeMother.getLastName());
            assertEquals("Femme mere prenom",mergeRecord.getWife().getMotherFirstName(),  wifeMother.getFirstName());
            assertEquals("Femme mere profession",mergeRecord.getWife().getMotherOccupation(),  wifeMother.getProperty("OCCU").getValue());


        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

/**
     * 
     */
    @Test
    public void testAddMarriageWithoutIndiAddress() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            String fileName = "releve paris.txt";
            String sourceTitle = "BMS Paris";
            MergeOptionPanel.SourceModel.getModel().add(fileName, sourceTitle);
            Record record = createMarriageRecord("IndiWithoutAddress");
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlaceVilleMariage(),fileName, record);
            MergeRecord mergeRecord = new MergeRecord(data);
                        
            List<MergeModel> models;
            Fam indiParentFamily;
            Indi indiFather;
            Indi indiMother;
            Fam wifeParentFamily;
            Indi wifeFather;
            Indi wifeMother;

            models = MergeModel.createMergeModel(mergeRecord, gedcom, null);
            assertEquals("Nombre model",1,models.size());
            Property result = models.get(0).copyRecordToEntity();

            Fam fam = (Fam) gedcom.getEntity(result.getEntity().getId());
            assertEquals("Lien mariage vers source","@S1@", fam.getValue(new TagPath("FAM:MARR:SOUR"),""));
            assertEquals("Source mariage","S1", gedcom.getEntity(fam.getValue(new TagPath("FAM:MARR:SOUR"),"").replaceAll("@", "")).getId());
            assertEquals("Date mariage",mergeRecord.getEventDate().getValue(), fam.getMarriageDate().getValue());
            assertEquals("Lieu mariage",mergeRecord.getEventPlace(), fam.getValue(new TagPath("FAM:MARR:PLAC"),""));
            //assertEquals("Note mariage",1534, fam.getValue(new TagPath("FAM:MARR:NOTE"),"").length());

            assertEquals("Indi : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), fam.getHusband().getBirthDate().getValue());
            // le lieu et commentaire ne sont pas modifiés car la date de naissance du releve n'est pas plus precise
            assertEquals("Indi : lieu naissance",mergeRecord.getIndi().getBirthPlace(), fam.getHusband().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            //assertEquals("Indi : Note naissance","", fam.getHusband().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Indi : Profession",1, fam.getHusband().getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = fam.getHusband().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Indi : Profession",mergeRecord.getIndi().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Indi : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Indi : Lieu Profession",mergeRecord.getIndi().getResidence(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Indi : Adresse Profession",mergeRecord.getIndi().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"),""));
            assertEquals("Indi : Note Profession","Profession indiquée dans l'acte de mariage de Fatherfirstname PREMIER et Motherfirstname DEUXIEME le 01/03/1999 (ville_mariage)",
                    occupation.getValue(new TagPath("OCCU:NOTE"),""));

            indiParentFamily = fam.getHusband().getFamilyWhereBiologicalChild();
            assertNotSame("Mari parent mariage date inchangé",mergeRecord.getIndi().getParentMarriageDate().getValue(),  indiParentFamily.getMarriageDate().getValue());
            assertEquals("Mari parent mariage comment",true,  indiParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
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
                    "Date de naissance 3 mar 1973 déduite de l'acte de mariage de Fatherfirstname PREMIER et Motherfirstname DEUXIEME le 01/03/1999 (ville_mariage)",
                    fam.getWife().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : Profession",1, fam.getWife().getProperties(new TagPath("INDI:OCCU")).length);
            occupation = fam.getWife().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Wife : Profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Wife : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Wife : Lieu Profession",mergeRecord.getWife().getResidence(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Wife : Adresse Profession",mergeRecord.getWife().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"),""));
            assertEquals("Wife : Note Profession",
                    "Profession indiquée dans l'acte de mariage de Fatherfirstname PREMIER et Motherfirstname DEUXIEME le 01/03/1999 (ville_mariage)",
                    occupation.getValue(new TagPath("OCCU:NOTE"),""));

            wifeParentFamily = fam.getWife().getFamilyWhereBiologicalChild();
            assertEquals("Femme parent mariage date",mergeRecord.getWife().getParentMarriageDate().getValue(),  wifeParentFamily.getMarriageDate().getValue());
            assertEquals("Femme parent mariage comment",true,  wifeParentFamily.getValue(new TagPath("FAM:MARR:NOTE"),"").contains(mergeRecord.getEventPlaceCityName()));
            wifeFather = wifeParentFamily.getHusband();
            assertEquals("Femme pere nom",mergeRecord.getWife().getFatherLastName(), wifeFather.getLastName());
            assertEquals("Femme pere prenom",mergeRecord.getWife().getFatherFirstName(),  wifeFather.getFirstName());
            assertEquals("Femme pere profession",mergeRecord.getWife().getFatherOccupation(),  wifeFather.getProperty("OCCU").getValue());
            wifeMother = wifeParentFamily.getWife();
            assertEquals("Femme mere nom",mergeRecord.getWife().getMotherLastName(), wifeMother.getLastName());
            assertEquals("Femme mere prenom",mergeRecord.getWife().getMotherFirstName(),  wifeMother.getFirstName());
            assertEquals("Femme mere profession",mergeRecord.getWife().getMotherOccupation(),  wifeMother.getProperty("OCCU").getValue());


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
            String fileName = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlaceVilleMariage(),fileName, createMarriageRecord("M1"));
            MergeRecord mergeRecord = new MergeRecord(data);
            
            List<MergeModel> models;
            // je change la date de naissance du pere pour permettre la modification
            ((Indi)gedcom.getEntity("I1")).getBirthDate().setValue("BEF 1971");

            models = MergeModel.createMergeModel(mergeRecord, gedcom, null);
            assertEquals("Nombre model",2,models.size());
            models.get(0).copyRecordToEntity();

            Fam fam = (Fam) gedcom.getEntity("F4");
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
            assertEquals("Indi : lieu naissance",mergeRecord.getIndi().getBirthPlace(), fam.getHusband().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Indi : Note naissance",
                "Date de naissance 1 jan 1970 déduite de l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                fam.getHusband().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Indi : Profession",2, fam.getHusband().getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = fam.getHusband().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Indi : Profession",mergeRecord.getIndi().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Indi : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Indi : Lieu Profession",mergeRecord.getIndi().getResidence(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Indi : Note Profession",
                "Profession indiquée dans l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                occupation.getValue(new TagPath("OCCU:NOTE"),""));

            // épouse
            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : lieu naissance",mergeRecord.getWife().getBirthPlace(), fam.getWife().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Wife : Note naissance",
                "Date de naissance 3 mar 1973 déduite de l'acte de mariage de Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 (ville_mariage)",
                fam.getWife().getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : Profession",1, fam.getWife().getProperties(new TagPath("INDI:OCCU")).length);
            occupation = fam.getWife().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Wife : Profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Wife : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Wife : Lieu Profession",mergeRecord.getWife().getResidence(),     occupation.getValue(new TagPath("OCCU:PLAC"),""));
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
            MergeRecord mergeRecord = new MergeRecord(data);
            
            List<MergeModel> models;
            models = MergeModel.createMergeModel(mergeRecord, gedcom, null);

            assertEquals("Nombre de propositions",2,models.size());
            // je verifie la premiere proposition
            String summary = models.get(0).getSummary(family);
            assertEquals("Proposition", "Modifier le mariage PERENOM_AA, PerePrenomAA x ---", summary);

            // j'applique la premiere proposition
            models.get(0).copyRecordToEntity();

            Fam fam = (Fam) gedcom.getEntity("F30");
            assertEquals("Date mariage",mergeRecord.getEventDate().getValue(), fam.getMarriageDate().getValue());
            assertEquals("Lieu mariage",mergeRecord.getEventPlace(), fam.getValue(new TagPath("FAM:MARR:PLAC"),""));
            assertEquals("Note mariage",GedcomOptions.getInstance().replaceSpaceSeparatorWithComma()? 877 : 879, fam.getValue(new TagPath("FAM:MARR:NOTE"),"").length());

            assertNotSame("Indi : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), fam.getHusband().getBirthDate().getValue());
            assertEquals("Indi : lieu naissance",mergeRecord.getIndi().getBirthPlace(), fam.getHusband().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Indi : Note naissance",141, fam.getHusband().getValue(new TagPath("INDI:BIRT:NOTE"), "").length());

            assertEquals("Indi : Profession",2, fam.getHusband().getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = fam.getHusband().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Indi : Profession",mergeRecord.getIndi().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Indi : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Indi : Lieu Profession",mergeRecord.getIndi().getResidence(),     occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("Indi : Note Profession",126, occupation.getValue(new TagPath("OCCU:NOTE"),"").length());

            assertEquals("Wife : Nom",mergeRecord.getWife().getLastName(), fam.getWife().getLastName());
            assertEquals("Wife : Prénom",mergeRecord.getWife().getFirstName(), fam.getWife().getFirstName());
            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : lieu naissance",mergeRecord.getWife().getBirthPlace(), fam.getWife().getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Wife : Note naissance",141, fam.getWife().getValue(new TagPath("INDI:BIRT:NOTE"), "").length());

            assertEquals("Wife : Date naissance",mergeRecord.getWife().getBirthDate().getValue(), fam.getWife().getBirthDate().getValue());
            assertEquals("Wife : Profession",1, fam.getWife().getProperties(new TagPath("INDI:OCCU")).length);
            occupation = fam.getWife().getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("Wife : Profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("Wife : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("Wife : Lieu Profession",mergeRecord.getWife().getResidence(),     occupation.getValue(new TagPath("OCCU:PLAC"),""));
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
            MergeRecord mergeRecord = new MergeRecord(data);
            
            List<MergeModel> models;

            models = MergeModel.createMergeModel(mergeRecord, gedcom, null);
            assertEquals("Nombre model",2,models.size());
            models.get(0).copyRecordToEntity();

            Fam fam = (Fam) gedcom.getEntity("F4");
            assertEquals("Lien mariage vers source","@S2@", fam.getValue(new TagPath("FAM:MARR:SOUR"),""));
            assertEquals("Source mariage","S2", gedcom.getEntity(fam.getValue(new TagPath("FAM:MARR:SOUR"),"").replaceAll("@", "")).getId());

            assertEquals("Mari mere date deces","BET 1970 AND 1999", mergeRecord.getIndi().getMotherDeathDate().getValue());

            Fam indiParentFamily = fam.getHusband().getFamilyWhereBiologicalChild();
            Indi indiFather = indiParentFamily.getHusband();
            assertEquals("Mari pere date deces","BET 1969 AND 1999", indiFather.getDeathDate().getValue());
            Indi indiMother = indiParentFamily.getWife();
            assertEquals("Mari mere date deces","BET 1970 AND 1999", indiMother.getDeathDate().getValue());


        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
}
