package ancestris.modules.releve.merge;

import ancestris.modules.releve.RecordTransferHandle;
import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.dnd.TransferableRecord;
import ancestris.modules.releve.merge.MergeRecord.MergeParticipantType;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.PlaceFormatModel;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordInfoPlace;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import genj.util.ReferenceSet;
import java.util.List;
import java.util.Set;
import junit.framework.TestCase;
import org.junit.Test;
import org.openide.util.Exceptions;

/**
 *
 * @author Michel
 */
public class MergeModelBirthTest extends TestCase {

    static public RecordInfoPlace getRecordsInfoPlace() {
        RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
        recordsInfoPlace.setValue("Paris","75000","","state","country");
        return recordsInfoPlace;
    }

    static public RecordBirth createBirthRecord(String firstName) {

        if ( firstName.equals("sansfamille1")) {
            RecordBirth record = new RecordBirth();
                record.setFieldValue(FieldType.eventDate, "01/01/2000");
                record.setFieldValue(FieldType.cote, "cote");
                record.setFieldValue(FieldType.freeComment,  "photo");
                record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "", "", "indiBirthplace", "birthAddress", "indiOccupation", "indiResidence", "indiAddress", "indiComment");
                record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "", "70y");
                record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "true", "72y");
                record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
                record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
                record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
                record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
                record.setFieldValue(FieldType.generalComment, "generalcomment");
            return record;
        } if ( firstName.equals("I1")) {
            RecordBirth record = new RecordBirth();
                record.setFieldValue(FieldType.eventDate, "02 JAN 1970");
                record.setFieldValue(FieldType.cote, "cote");
                record.setFieldValue(FieldType.freeComment,  "photo");
                record.setIndi("Fatherfirstname", "FATHERLASTNAME", "M", "", "", "indiBirthplace", "birthAddress", "indiOccupation", "indiResidence", "indiAddress", "indiComment");
                record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "dead", "70y");
                record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "dead", "72y");
                record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
                record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
                record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
                record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
                record.setFieldValue(FieldType.generalComment, "generalcomment");
            return record;
        } else if ( firstName.equals("child1")) {
            RecordBirth record = new RecordBirth();
                record.setFieldValue(FieldType.eventDate, "01/01/2000");
                record.setFieldValue(FieldType.cote, "cote");
                record.setFieldValue(FieldType.freeComment,  "photo");
                record.setIndi("One First Name", "FATHERLASTNAME", "F", "", "", "indiBirthplace", "birthAddress", "indiOccupation", "indiResidence", "indiAddress", "indiComment");
                record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "dead", "70y");
                record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "dead", "72y");
                record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
                record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
                record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
                record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
                record.setFieldValue(FieldType.generalComment, "generalcomment");
            return record;
        } else if ( firstName.equals("child2")) {
            RecordBirth record = new RecordBirth();
                record.setFieldValue(FieldType.eventDate, "01/01/1980");
                record.setFieldValue(FieldType.cote, "cote");
                record.setFieldValue(FieldType.freeComment,  "photo");
                record.setIndi("Two First Name", "FATHERLASTNAME", "M", "03 MAR 2003", "", "indiBirthplace", "birthAddress", "indiOccupation", "indiResidence", "indiAddress", "indiComment");
                //record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "occupation", "indiFatherResidence", "indiFatherComment", "dead");
                //record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "occupation", "indiMotherResidence", "indiMotherComment", "dead");
                record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
                record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
                record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
                record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
                record.setFieldValue(FieldType.generalComment, "generalcomment");
            return record;
        } else if ( firstName.equals("child3")) {
            RecordBirth record = new RecordBirth();
                record.setFieldValue(FieldType.eventDate, "06/07/2001");
                record.setFieldValue(FieldType.cote, "cote");
                record.setFieldValue(FieldType.freeComment,  "photo");
                record.setIndi("Three, First, Name", "FATHERLASTNAME", "F", "", "04 JUL 2001", "indiBirthplace", "birthAddress", "indioccupation", "indiResidence", "indiAddress", "indicomment");
                record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "false", "31y");
                record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "", "30y");
                record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
                record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
                record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
                record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
                record.setFieldValue(FieldType.generalComment, "generalcomment");
            return record;
        } else {
            
            return null;
        }
    }


    /**
     * test_RecordBirth_copyRecordToEntity_Date
     */
    @Test
    public void test_RecordBirth_copyRecordToEntity_Date() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("sansfamille1");
            RecordInfoPlace fullInfoPlace = new RecordInfoPlace();
            fullInfoPlace.setValue("Paris","75000","county","state","country");
            String fileName = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, fullInfoPlace, fileName, createBirthRecord("sansfamille1"));
            MergeRecord mergeRecord = new MergeRecord(data);
            List<MergeModel> models;
            // je memorise la date de naissance du pere
            String previousFatherBirthDate = ((Indi)gedcom.getEntity("I1")).getBirthDate().getValue();

            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);

            assertEquals("Nombre model",3,models.size());
            models.get(0).copyRecordToEntity();

            assertEquals("famille","F1", indi.getFamilyWhereBiologicalChild().getId());
            assertEquals("Mariage date","BEF 2000", indi.getFamilyWhereBiologicalChild().getMarriageDate().getValue());

            assertEquals("indiBirthDate",mergeRecord.getIndi().getBirthDate().getValue(), indi.getBirthDate().getValue());

            Indi father = indi.getBiologicalFather();
            assertEquals("fatherFirstName",mergeRecord.getIndi().getFatherFirstName(), father.getFirstName());
            // la date de naissance du pere n'est pas changée car elle est plus précise que celle du releve
            assertEquals("Naissance du pere",previousFatherBirthDate, father.getBirthDate().getValue());
            assertEquals("deces du pere",   "AFT 1999", father.getDeathDate().getValue());

            Indi mother = indi.getBiologicalMother();
            assertEquals("Naissance de la mere","CAL 1928", mother.getBirthDate().getValue());
            assertEquals("deces de la mere",    "BET 2000 AND 2000", mother.getDeathDate().getValue());
            
            assertEquals("EventPlace",fullInfoPlace.toString(), mergeRecord.getEventPlace());         

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail(ex.getMessage());
        }
    }
    
    /**
     * test_RecordBirth_copyRecordToEntity_Date
     */
    @Test
    public void test_RecordBirth_copyRecordToEntity_Date2() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = null;
            String fileName = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, createBirthRecord("child3"));
            MergeRecord mergeRecord = new MergeRecord(data);            
            List<MergeModel> models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model", 3, models.size());
            for(MergeModel model : models ) {
                System.out.println("model "+ model.getTitle() + " : ProposedEntity=" + model.getProposedEntity());
            }
                        
            models.get(0).copyRecordToEntity();
            
            //assertEquals("Indi id","child3", birthProperty.getEntity().getId());
            indi = (Indi)gedcom.getEntity("child3");
            assertEquals("famille","F1", indi.getFamilyWhereBiologicalChild().getId());
            assertNotNull("IndiBirthDate",indi.getPropertyByPath("INDI:BIRT:DATE"));
            assertNotNull("IndiBirthPlace",indi.getPropertyByPath("INDI:BIRT:PLAC"));
            assertNotNull("IndiBirthComment",indi.getPropertyByPath("INDI:BIRT:NOTE"));
            
            String expected = "";
            expected +="Date de l'acte: 06/07/2001\n";
            expected +="Nouveau né: Three, First, Name FATHERLASTNAME, né le 04/07/2001 à birthAddress, indiBirthplace, indioccupation, domicile indiAddress, indiResidence, indicomment\n";
            expected +="Père: Fatherfirstname FATHERLASTNAME, 31 années, Vivant, fatherOccupation, domicile indiFatherAddress, indiFatherResidence, indiFatherComment\n";
            expected +="Mère: Motherfirstname MOTHERLASTNAME, 30 années, motherOccupation, domicile indiMotherAddress, indiMotherResidence, indiMotherComment\n";
            expected +="Parrain/témoin: w1firstname w1lastname, w1occupation, w1comment\n";
            expected +="Marraine/témoin: w2firstname w2lastname, w2occupation, w2comment\n";
            expected +="Témoin(s): w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment\n";
            expected +="Commentaire général: generalcomment\n";
            expected +="Cote: cote, photo";
            assertEquals("comment1",expected, indi.getPropertyByPath("INDI:BIRT:NOTE").getValue());

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail(ex.getMessage());
        }
    }

    /**
     * test_RecordBirth_copyRecordToEntity_Comment
     */
    @Test
    public void test_RecordBirth_copyRecordToEntity_Comment() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("sansfamille1");
            RecordBirth record = createBirthRecord("sansfamille1");
            String sourceTitle = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(),sourceTitle, record);
            MergeRecord mergeRecord = new MergeRecord(data);
            
            List<MergeModel> models;

            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",3,models.size());
            models.get(0).copyRecordToEntity();

            String expected = "";
            expected +="Date de l'acte: 01/01/2000\n";
            expected +="Nouveau né: sansfamille1 FATHERLASTNAME, né à birthAddress, indiBirthplace, indiOccupation, domicile indiAddress, indiResidence, indiComment\n";
            expected +="Père: Fatherfirstname FATHERLASTNAME, 70 années, fatherOccupation, domicile indiFatherAddress, indiFatherResidence, indiFatherComment\n";
            expected +="Mère: Motherfirstname MOTHERLASTNAME, 72 années, Décédé, motherOccupation, domicile indiMotherAddress, indiMotherResidence, indiMotherComment\n";
            expected +="Parrain/témoin: w1firstname w1lastname, w1occupation, w1comment\n";
            expected +="Marraine/témoin: w2firstname w2lastname, w2occupation, w2comment\n";
            expected +="Témoin(s): w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment\n";
            expected +="Commentaire général: generalcomment\n";
            expected +="Cote: cote, photo";
            assertEquals("comment1",expected, indi.getPropertyByPath("INDI:BIRT:NOTE").getValue());

            // je verifie que le nouveau commentaire contient la concatenation de l'ancien commentaire et du nouveau
            indi.getPropertyByPath("INDI:BIRT:NOTE").setValue("oldcomment");
            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",1,models.size());
            models.get(0).copyRecordToEntity();
            expected ="Date de l'acte: 01/01/2000\n";
            expected +="Nouveau né: sansfamille1 FATHERLASTNAME, né à birthAddress, indiBirthplace, indiOccupation, domicile indiAddress, indiResidence, indiComment\n";
            expected +="Père: Fatherfirstname FATHERLASTNAME, 70 années, fatherOccupation, domicile indiFatherAddress, indiFatherResidence, indiFatherComment\n";
            expected +="Mère: Motherfirstname MOTHERLASTNAME, 72 années, Décédé, motherOccupation, domicile indiMotherAddress, indiMotherResidence, indiMotherComment\n";
            expected +="Parrain/témoin: w1firstname w1lastname, w1occupation, w1comment\n";
            expected +="Marraine/témoin: w2firstname w2lastname, w2occupation, w2comment\n";
            expected +="Témoin(s): w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment\n";
            expected +="Commentaire général: generalcomment\n";
            expected +="Cote: cote, photo\n";
            expected += "oldcomment";
            assertEquals("comment2",expected, indi.getPropertyByPath("INDI:BIRT:NOTE").getValue());
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail(ex.getMessage());
        }
    }

    
    /**
     * testMergeRecordBirth avec source existante
     */
    @Test
    public void testMergeRecordBirthSourceExistanteDejaAssociee() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("I1");
            List<MergeModel> models;
            RecordBirth record = createBirthRecord("I1");
            String fileName = "releve paris.txt";
            String sourceTitle = "BMS Paris";
            MergeOptionPanel.SourceModel.getModel().add(fileName, sourceTitle);
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(),fileName, record);
            MergeRecord mergeRecord = new MergeRecord(data);
            
            
            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",1,models.size());
            models.get(0).copyRecordToEntity();
            assertEquals("IndiFirstName",mergeRecord.getIndi().getFirstName(), indi.getFirstName());
            assertEquals("IndiLastName",mergeRecord.getIndi().getLastName(), indi.getLastName());
            assertEquals("IndiSex",mergeRecord.getIndi().getSex(), indi.getSex());
            assertNotSame("IndiBirthDate",mergeRecord.getEventDate().getValue(), indi.getBirthDate().getValue());
            Property[] sourceLink = indi.getProperties(new TagPath("INDI:BIRT:SOUR"));
            assertEquals("Nb birthsource",1,sourceLink.length );
            assertEquals("IndiBirthDate","BMS Paris", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * testMergeRecordBirth avec ajout d'une source existante mais pas déjà associee à la naissance
     */
    @Test
    public void testMergeRecordBirthSourceExistanteNonAssociee() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("child2");
            List<MergeModel> models;
            
            RecordBirth record= createBirthRecord("child2");
            RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
            recordsInfoPlace.setValue("Brest","35000","","state","country");
            String fileName = "releve brest.txt";
            String sourceTitle = "Etat civil Brest";
            MergeOptionPanel.SourceModel.getModel().add(fileName, sourceTitle);
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, record);
            MergeRecord mergeRecord = new MergeRecord(data);
            
            
            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",1,models.size());
            models.get(0).copyRecordToEntity();
            assertEquals("IndiFirstName",mergeRecord.getIndi().getFirstName(), indi.getFirstName());
            assertEquals("IndiLastName",mergeRecord.getIndi().getLastName(), indi.getLastName());
            assertEquals("IndiSex",mergeRecord.getIndi().getSex(), indi.getSex());
            assertNotSame("IndiBirthDate",mergeRecord.getEventDate().getValue(), indi.getBirthDate().getValue());
            Property[] sourceLink = indi.getProperties(new TagPath("INDI:BIRT:SOUR"));
//            for(Property prop : sourceLink) {
//                System.out.println("source="+ ((Source)((PropertyXRef)prop).getTargetEntity()).getTitle());
//            }            
            assertEquals("Nb birthsource",1,sourceLink.length );
            assertEquals("Source title 0","Etat civil Brest", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );
            assertEquals("Source ID 0","S3", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getId());            

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * testMergeRecordBirth avec nouvelle source
     */
    @Test
    public void testMergeRecordBirthSourceNewSource() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();

            Indi indi = (Indi)gedcom.getEntity("I1");
            List<MergeModel> models;

            RecordBirth record = createBirthRecord("I1");
            RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
            recordsInfoPlace.setValue("Versailles","75009","","state","country");
            String fileName = "BMS Paris";
            MergeOptionPanel.SourceModel.getModel().add(fileName, "BMS Paris");
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, record);
            MergeRecord mergeRecord = new MergeRecord(data);
            
            
            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",1,models.size());
            models.get(0).copyRecordToEntity();
            assertEquals("IndiFirstName",mergeRecord.getIndi().getFirstName(), indi.getFirstName());
            assertEquals("IndiLastName",mergeRecord.getIndi().getLastName(), indi.getLastName());
            assertEquals("IndiSex",mergeRecord.getIndi().getSex(), indi.getSex());
            assertNotSame("IndiBirthDate",mergeRecord.getEventDate().getValue(), indi.getBirthDate().getValue());
            Property[] sourceLink = indi.getProperties(new TagPath("INDI:BIRT:SOUR"));
            assertEquals("Nb birthsource",1,sourceLink.length );
            assertEquals("IndiBirthDate","BMS Paris", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    /**
     * testMergeRecordBirth avec nouvelle source
     */
    @Test
    public void testMergeRecordBirthAutreIndiExistant() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("I1");
            RecordBirth record = createBirthRecord("I1");
            // je renseigne la meme date de naissance
            record.setFieldValue(FieldType.eventDate, indi.getBirthDate().getValue());            
            String fileName = "BMS Paris";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, record);
            MergeRecord mergeRecord = new MergeRecord(data);
            
            List<MergeModel> models;

            assertEquals("otherIndi",0, MergeQuery.findIndiCompatibleWithParticipant(mergeRecord, MergeParticipantType.participant1, gedcom, indi).size());

            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",1,models.size());
            models.get(0).copyRecordToEntity();
            assertEquals("Indi First Name",mergeRecord.getIndi().getFirstName(), indi.getFirstName());
            assertEquals("Indi Last Name",mergeRecord.getIndi().getLastName(), indi.getLastName());
            assertEquals("Indi Sex",mergeRecord.getIndi().getSex(), indi.getSex());
            assertEquals("Indi Birth Date",0, indi.getBirthDate().compareTo(mergeRecord.getEventDate()));
            Property[] sourceLink = indi.getProperties(new TagPath("INDI:BIRT:SOUR"));
            assertEquals("Nb birthsource",1,sourceLink.length );
            assertEquals("source 0","BMS Paris", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }


    /**
     * testMergeRecordBirth avec nouvelle source et avec le lieu avec des coordonnées
     */
    @Test
    public void testMergeRecordBirthIndiBirthPlace() {
        try {
            RecordBirth record;
            MergeRecord mergeRecord;
            
            PlaceFormatModel.getModel().savePreferences(0,1,2,3,4,6);
         
            // cas : indiBirthPlace = ""
            record = new RecordBirth();
            record.setFieldValue(FieldType.eventDate, "01/01/2000");
            record.setFieldValue(FieldType.cote, "cote");
            record.setFieldValue(FieldType.freeComment,  "photo");
            record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "", "", "", "", "", "","", "indicomment");
            record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "indiFatherResidence", "indiFatherAddress", "comment", "", "70y");
            record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "indiMotherAddress", "comment", "dead", "72y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
            record.setFieldValue(FieldType.generalComment, "generalcomment");

            String fileName = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, record);
            mergeRecord = new MergeRecord(data);
            
            assertEquals("Indi Birth place=IndiFatherResidence",record.getFieldValue(FieldType.indiFatherResidence), mergeRecord.getIndi().getBirthPlace());

             // cas : indiBirthPlace = "" et indiFatherResidence = ""
            record = new RecordBirth();
            record.setFieldValue(FieldType.eventDate, "01/01/2000");
            record.setFieldValue(FieldType.cote, "cote");
            record.setFieldValue(FieldType.freeComment,  "photo");
            record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "", "", "", "", "", "", "", "indicomment");
            record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "", "", "comment", "", "70y");
            record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "indiMotherAddress", "comment", "dead", "72y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
            record.setFieldValue(FieldType.generalComment, "generalcomment");

            data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, record);
            mergeRecord = new MergeRecord(data);
            assertEquals("Indi Birth place=eventPlace",getRecordsInfoPlace().toString(), mergeRecord.getIndi().getBirthPlace());

            Gedcom gedcom = TestUtility.createGedcom();
            List<MergeModel> models;
            models = MergeModel.createMergeModel(mergeRecord, gedcom, null);
            assertEquals("Nombre model",4,models.size());
            models.get(0).copyRecordToEntity();
            Fam family = (Fam)gedcom.getEntity("F1");
            Indi[] children = family.getChildren(false);   // sorted=true => classé par ordre de date de naissance , sinon par ordre de création 
            assertEquals("Nombre d'enfants",4,children.length);
            Indi indi = children[3];  // je recupere le 4ieme enfant par ordre de creation
            // je verifie que le lieu de naissance du 4ieme enfant a ete renseigne avec le lieu par défaut du releve
            assertEquals("Lieu de naissance",getRecordsInfoPlace().getValue(), indi.getBirthPlace().getValue());
            
            // je verifie les coordonnees 
            ReferenceSet<String, Property>  gedcomPlaces = gedcom.getReferenceSet("PLAC");
            Set<Property> parisPlaces = gedcomPlaces.getReferences(getRecordsInfoPlace().toString());
            PropertyPlace parisPlace = null;
            for(Property place : parisPlaces  )  {
                if( ((PropertyPlace)place).getMap() != null) {
                    parisPlace = (PropertyPlace)place;
                    break;
                }
            }
            assertNotNull("Lieu de naissance existe deja dans le gedcom",parisPlace);
            assertNotNull("Coordonnées du lieu de naissance sont présentes",parisPlace.getMap());
            
            assertEquals("Lieu de naissance","N48.8534", indi.getBirthPlace().getLatitude(true).getValue());
            assertEquals("Lieu de naissance","E2.3486", indi.getBirthPlace().getLongitude(true).getValue());            
            
            
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }


}
