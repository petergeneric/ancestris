package ancestris.modules.releve.merge;

import ancestris.modules.releve.IgnoreOtherTestMethod;
import ancestris.modules.releve.RecordTransferHandle;
import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.dnd.TransferableRecord;
import ancestris.modules.releve.merge.MergeRecord.MergeParticipantType;
import ancestris.modules.releve.model.PlaceFormatModel;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordInfoPlace;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import genj.util.ReferenceSet;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.Test;
import org.openide.util.Exceptions;

/**
 *
 * @author Michel
 */
public class MergeModelBirthTest  {
    @Rule
    public IgnoreOtherTestMethod rule = new IgnoreOtherTestMethod("");

    static public RecordInfoPlace getRecordsInfoPlace() {
        RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
        recordsInfoPlace.setValue("Paris","75000","","state","country");
        return recordsInfoPlace;
    }

    static public RecordBirth createBirthRecord(String firstName) {

        if ( firstName.equals("sansfamille1")) {
            RecordBirth record = new RecordBirth();
                record.setFieldValue(FieldType.eventDate, "02/01/2000");
                record.setFieldValue(FieldType.cote, "cote");
                record.setFieldValue(FieldType.freeComment,  "photo");
                record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "", "01/01/2000", "indiBirthplace", "birthAddress", "indiOccupation", "indiResidence", "indiAddress", "indiComment");
                record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "", "30y");
                record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "true", "28y");
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
                record.setIndi("Two-First-Name", "FATHERLASTNAME", "M", "03 MAR 2003", "", "indiBirthplace", "birthAddress", "indiOccupation", "indiResidence", "indiAddress", "indiComment");
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
    public void test_RecordBirth_copyRecordToEntity() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("sansfamille1");
            RecordInfoPlace fullInfoPlace = new RecordInfoPlace();
            fullInfoPlace.setValue("Paris","75000","county","state","country");
            String fileName = "releve paris.txt";
            String sourceTitle = ((Source)gedcom.getEntity("S1") ).getTitle();
            MergeOptionPanel.SourceModel.getModel().add(fileName, sourceTitle);
           // je memorise la date de naissance du pere
            String previousFatherBirthDate = ((Indi)gedcom.getEntity("I1")).getBirthDate().getValue();
           TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, fullInfoPlace, fileName, createBirthRecord("sansfamille1"));

//TestUtility.showMergeDialog(data, gedcom, indi);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, indi);
            MergeRecord mergeRecord = mergeManager.getMergeRecord();
            assertEquals("Nombre model",2,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

            assertEquals("Indi : date naissance",mergeRecord.getIndi().getBirthDate().getValue(), indi.getBirthDate().getValue() );
            assertEquals("Indi : lieu naissance",mergeRecord.getIndi().getBirthResidence().getPlace(), indi.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("Indi : adresse naissance",mergeRecord.getIndi().getBirthResidence().getAddress(), indi.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            //assertEquals("Indi : note naissance", "", indi.getValue(new TagPath("INDI:BIRT:NOTE"), ""));
            Property[] sourceLink = indi.getProperties(new TagPath("INDI:BIRT:SOUR"));
            assertEquals("Nb birthsource",1,sourceLink.length );
            assertEquals("Source name","BMS Paris", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );

            assertEquals("famille","F1", indi.getFamilyWhereBiologicalChild().getId());
            assertEquals("Mariage date","BEF 2000", indi.getFamilyWhereBiologicalChild().getMarriageDate().getValue());

            assertEquals("indiBirthDate",mergeRecord.getIndi().getBirthDate().getValue(), indi.getBirthDate().getValue());

            Indi father = indi.getBiologicalFather();
            assertEquals("father FirstName",mergeRecord.getIndi().getFather().getFirstName(), father.getFirstName());
            // la date de naissance du pere n'est pas changée car elle est plus précise que celle du releve
            assertEquals("father : birth date", previousFatherBirthDate, father.getBirthDate().getValue());
            assertEquals("father : death date",   "AFT 1999", father.getDeathDate().getValue());
            Property occupation = father.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("father : occupation", mergeRecord.getIndi().getFather().getOccupation(), occupation.getValue(new TagPath("OCCU"), ""));
            assertEquals("father : place",      mergeRecord.getIndi().getFather().getResidence().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"), ""));
            assertEquals("father : address",    mergeRecord.getIndi().getFather().getResidence().getAddress(),   occupation.getValue(new TagPath("OCCU:ADDR"), ""));

            Indi mother = indi.getBiologicalMother();
            assertEquals("mother : birth date", "CAL 1972", mother.getBirthDate().getValue());
            assertEquals("mother : death date", "BET 2000 AND 2000", mother.getDeathDate().getValue());
            occupation = mother.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("mother : occupation", mergeRecord.getIndi().getMother().getOccupation(), occupation.getValue(new TagPath("OCCU"), ""));
            assertEquals("mother : place",      mergeRecord.getIndi().getMother().getResidence().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"), ""));
            assertEquals("mother : address",    mergeRecord.getIndi().getMother().getResidence().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"), ""));

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
    public void test_RecordBirth_copyRecordToEntity_Date() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("sansfamille1");
            RecordInfoPlace fullInfoPlace = new RecordInfoPlace();
            fullInfoPlace.setValue("Paris","75000","county","state","country");
            String fileName = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, fullInfoPlace, fileName, createBirthRecord("sansfamille1"));
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, indi);
            MergeRecord mergeRecord = mergeManager.getMergeRecord();

            // je memorise la date de naissance du pere
            String previousFatherBirthDate = ((Indi)gedcom.getEntity("I1")).getBirthDate().getValue();

            assertEquals("Nombre model",2,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

            assertEquals("famille","F1", indi.getFamilyWhereBiologicalChild().getId());
            assertEquals("Mariage date","BEF 2000", indi.getFamilyWhereBiologicalChild().getMarriageDate().getValue());

            assertEquals("indiBirthDate",mergeRecord.getIndi().getBirthDate().getValue(), indi.getBirthDate().getValue());

            Indi father = indi.getBiologicalFather();
            assertEquals("fatherFirstName",mergeRecord.getIndi().getFather().getFirstName(), father.getFirstName());
            // la date de naissance du pere n'est pas changée car elle est plus précise que celle du releve
            assertEquals("Naissance du pere",previousFatherBirthDate, father.getBirthDate().getValue());
            assertEquals("deces du pere",   "AFT 1999", father.getDeathDate().getValue());

            Indi mother = indi.getBiologicalMother();
            assertEquals("Naissance de la mere","CAL 1972", mother.getBirthDate().getValue());
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
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, indi);

            assertEquals("Nombre model", 3, mergeManager.getProposalList1().getSize());

            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

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
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, indi);

            assertEquals("Nombre model",2,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

            String expected = "";
            expected +="Date de l'acte: 02/01/2000\n";
            expected +="Nouveau né: sansfamille1 FATHERLASTNAME, né le 01/01/2000 à birthAddress, indiBirthplace, indiOccupation, domicile indiAddress, indiResidence, indiComment\n";
            expected +="Père: Fatherfirstname FATHERLASTNAME, 30 années, fatherOccupation, domicile indiFatherAddress, indiFatherResidence, indiFatherComment\n";
            expected +="Mère: Motherfirstname MOTHERLASTNAME, 28 années, Décédé, motherOccupation, domicile indiMotherAddress, indiMotherResidence, indiMotherComment\n";
            expected +="Parrain/témoin: w1firstname w1lastname, w1occupation, w1comment\n";
            expected +="Marraine/témoin: w2firstname w2lastname, w2occupation, w2comment\n";
            expected +="Témoin(s): w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment\n";
            expected +="Commentaire général: generalcomment\n";
            expected +="Cote: cote, photo";
            assertEquals("comment1",expected, indi.getPropertyByPath("INDI:BIRT:NOTE").getValue());

            // je verifie que le nouveau commentaire contient la concatenation de l'ancien commentaire et du nouveau
            indi.getPropertyByPath("INDI:BIRT:NOTE").setValue("oldcomment");
            mergeManager = TestUtility.createMergeManager(data, gedcom, indi);
            assertEquals("Nombre model",1,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();
            expected ="Date de l'acte: 02/01/2000\n";
            expected +="Nouveau né: sansfamille1 FATHERLASTNAME, né le 01/01/2000 à birthAddress, indiBirthplace, indiOccupation, domicile indiAddress, indiResidence, indiComment\n";
            expected +="Père: Fatherfirstname FATHERLASTNAME, 30 années, fatherOccupation, domicile indiFatherAddress, indiFatherResidence, indiFatherComment\n";
            expected +="Mère: Motherfirstname MOTHERLASTNAME, 28 années, Décédé, motherOccupation, domicile indiMotherAddress, indiMotherResidence, indiMotherComment\n";
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
            RecordBirth record = createBirthRecord("I1");
            String fileName = "releve paris.txt";
            String sourceTitle = "BMS Paris";
            MergeOptionPanel.SourceModel.getModel().add(fileName, sourceTitle);
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(),fileName, record);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, indi);
            MergeRecord mergeRecord = mergeManager.getMergeRecord();

            assertEquals("Nombre model",1,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();
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
            int indiSex = indi.getSex();
            RecordBirth record= createBirthRecord("child2");
            RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
            recordsInfoPlace.setValue("Brest","35000","","state","country");
            String fileName = "releve brest.txt";
            String sourceTitle = "Etat civil Brest";
            MergeOptionPanel.SourceModel.getModel().add(fileName, sourceTitle);
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, record);

            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, indi);
            MergeRecord mergeRecord = mergeManager.getMergeRecord();

            assertEquals("Nombre model",1,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();
            assertEquals("IndiFirstName",mergeRecord.getIndi().getFirstName(), indi.getFirstName());
            assertEquals("IndiLastName",mergeRecord.getIndi().getLastName(), indi.getLastName());
            assertEquals("IndiSex un changed",  indiSex, indi.getSex());
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
            RecordBirth record = createBirthRecord("I1");
            RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
            recordsInfoPlace.setValue("Versailles","75009","","state","country");
            String fileName = "BMS Paris";
            MergeOptionPanel.SourceModel.getModel().add(fileName, "BMS Paris");
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, record);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, indi);
            MergeRecord mergeRecord = mergeManager.getMergeRecord();

            assertEquals("Nombre model",1,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();
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
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, indi);
            MergeRecord mergeRecord = mergeManager.getMergeRecord();

            assertEquals("otherIndi",0, MergeQuery.findIndiCompatibleWithParticipant(mergeRecord, mergeRecord.getParticipant(MergeParticipantType.participant1), gedcom, indi).size());

            assertEquals("Nombre model",1,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();
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

            PlaceFormatModel.getCurrentModel().savePreferences(1,2,3,4,5,6);

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

            assertEquals("Indi Birth place=IndiFatherResidence",record.getFieldValue(FieldType.indiFatherResidence), mergeRecord.getIndi().getBirthResidence().getPlace());

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

            Gedcom gedcom = TestUtility.createGedcom();
            data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, record);
//TestUtility.showMergeDialog(data, gedcom, gedcom.getEntity("child3"));
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
            mergeRecord = mergeManager.getMergeRecord();
            assertEquals("Indi Birth place=eventPlace",getRecordsInfoPlace().toString(), mergeRecord.getIndi().getBirthResidence().getPlace());
            assertEquals("Indi Birth address is empty","", mergeRecord.getIndi().getBirthResidence().getAddress());

//            mergeManager.getProposalList1().showAllProposal(true);
            assertEquals("Nombre model",4,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();
            Fam family = (Fam)gedcom.getEntity("F1");
            Indi[] children = family.getChildren(false);   // sorted=true => classé par ordre de date de naissance , sinon par ordre de création
            assertEquals("Nombre d'enfants",4,children.length);
            Indi indi = children[3];  // je recupere le 4ieme enfant par ordre de creation
            // je verifie que le lieu de naissance du 4ieme enfant a ete renseigne avec le lieu par défaut du releve
            assertEquals("Indi : Lieu de naissance",getRecordsInfoPlace().getValue(), indi.getBirthPlace().getValue());
            assertEquals("Indi : adresse naissance",mergeRecord.getIndi().getBirthResidence().getAddress(), indi.getValue(new TagPath("INDI:BIRT:ADDR"), ""));

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


    /**
     * testMergeRecordBirth avec nouvelle source et avec le lieu avec des coordonnées
     */
     @Test
    public void testMergeRecordBirthIndiBirthAddress() {
        try {
            RecordBirth record;

            PlaceFormatModel.getCurrentModel().savePreferences(1,2,3,4,5,6);
            String fileName = "";

            // cas : indiBirthPlace = "" et indiBirthAdress =""
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

            {
                // record  birthPlace=""  birthAddress=""
                // entity  birthPlace=""  birthAddress=""
                Gedcom gedcom = TestUtility.createGedcom();
                Indi indi = (Indi)gedcom.getEntity("sansfamille1");
                TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, record);
//TestUtility.showMergeDialog(data, gedcom, null);
                MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
                mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

                assertEquals("Indi : birth place",record.getFieldValue(FieldType.indiFatherResidence), indi.getBirthPlace().getValue());
                assertEquals("Indi : birth address",record.getFieldValue(FieldType.indiFatherAddress), indi.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            }

            {
                // record birthPlace=""  birthAddress="indiBirthAddress"
                // entity birthPlace=""  birthAddress=""
                record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "", "", "", "indiBirthAddress", "", "","", "indicomment");

                Gedcom gedcom = TestUtility.createGedcom();
                Indi indi = (Indi)gedcom.getEntity("sansfamille1");
                TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, record);
                MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
                mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

                assertEquals("Indi : birth place",record.getFieldValue(FieldType.indiFatherResidence), indi.getBirthPlace().getValue());
                assertEquals("Indi : birth address",record.getFieldValue(FieldType.indiFatherAddress), indi.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            }

            {
                // record birthPlace="indiFatherResidence"  birthAddress="indiBirthAddress"
                // entity birthPlace=""  birthAddress=""
                record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "", "", "indiFatherResidence", "indiBirthAddress", "", "","", "indicomment");

                Gedcom gedcom = TestUtility.createGedcom();
                Indi indi = (Indi)gedcom.getEntity("sansfamille1");
                TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, record);
                MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
                mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

                assertEquals("Indi : birth place",record.getFieldValue(FieldType.indiBirthPlace), indi.getBirthPlace().getValue());
                assertEquals("Indi : birth address",record.getFieldValue(FieldType.indiBirthAddress), indi.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            }

            {
                // record birthPlace="indiBirthPlace" birthAddress="indiBirthAddress"
                // entity birthPlace=""               birthAddress=""
                record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "", "", "indiBirthPlace", "indiBirthAddress", "", "","", "indicomment");

                Gedcom gedcom = TestUtility.createGedcom();
                Indi indi = (Indi)gedcom.getEntity("sansfamille1");
                TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, record);
                MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
                mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

                assertEquals("Indi : birth place",record.getFieldValue(FieldType.indiBirthPlace), indi.getBirthPlace().getValue());
                assertEquals("Indi : birth address",record.getFieldValue(FieldType.indiBirthAddress), indi.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            }

            {
                // record birthPlace="indiBirthPlace"  et birthAddress=""
                // entity = birthPlace=""      birthAddress=""
                record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "", "", "indiBirthPlace", "", "", "","", "indicomment");

                Gedcom gedcom = TestUtility.createGedcom();
                Indi indi = (Indi)gedcom.getEntity("sansfamille1");
                TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, record);
                MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
                mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

                assertEquals("Indi : birth place",record.getFieldValue(FieldType.indiBirthPlace), indi.getBirthPlace().getValue());
                assertEquals("Indi : birth address",record.getFieldValue(FieldType.indiBirthAddress), indi.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            }

            {
                // record birthPlace="indiBirthPlace"  birthAddress="indiBirthAddress"
                // entity birthPlace="indiBirthPlace"  birthAddress=""
                record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "", "", "indiBirthPlace", "indiBirthAddress", "", "","", "indicomment");

                Gedcom gedcom = TestUtility.createGedcom();
                Indi indi = (Indi)gedcom.getEntity("sansfamille1");
                indi.getProperty("BIRT").addProperty("PLAC", "indiBirthPlace");

                TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, record);
                MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
                mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

                assertEquals("Indi : birth place",record.getFieldValue(FieldType.indiBirthPlace), indi.getBirthPlace().getValue());
                assertEquals("Indi : birth address",record.getFieldValue(FieldType.indiBirthAddress), indi.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            }

            {
                // record birthPlace="indiBirthPlace"  birthAddress="xxxx yyy"
                // entity birthPlace="indiBirthPlace"  birthAddress="indiBirthAddress"
                // date naissance egale
                record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "", "", "indiBirthPlace", "xxxx yyy", "", "","", "indicomment");

                Gedcom gedcom = TestUtility.createGedcom();
                Indi indi = (Indi)gedcom.getEntity("sansfamille1");
                indi.getProperty("BIRT").addProperty("PLAC", "indiBirthPlace");
                indi.getProperty("BIRT").addProperty("ADDR", "indiBirthAddress");

                TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, record);
                MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
                mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

                assertEquals("Indi : birth place",record.getFieldValue(FieldType.indiBirthPlace), indi.getBirthPlace().getValue());
                assertEquals("Indi : birth address",record.getFieldValue(FieldType.indiBirthAddress), indi.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            }
            {
                // record birthPlace="indiBirthPlace"  birthAddress="xxxx yyy"
                // entity birthPlace="indiBirthPlace"  birthAddress="indiBirthAddress"
                // date naissance record plus précise que date naissance  gedcom
                record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "", "", "indiBirthPlace", "xxxx yyy", "", "","", "indicomment");

                Gedcom gedcom = TestUtility.createGedcom();
                Indi indi = (Indi)gedcom.getEntity("sansfamille1");
                indi.getProperty("BIRT").getProperty("DATE").setValue("2000");
                indi.getProperty("BIRT").addProperty("ADDR", "indiBirthAddress");
                indi.getProperty("BIRT").addProperty("PLAC", "indiBirthPlace");

                TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, record);
                MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
                mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

                assertEquals("Indi : birth place",record.getFieldValue(FieldType.indiBirthPlace), indi.getBirthPlace().getValue());
                assertEquals("Indi : birth address",record.getFieldValue(FieldType.indiBirthAddress), indi.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            }

            {
                // record birthPlace=""  birthAddress="xxxx yyy"
                // entity birthPlace="indiBirthPlace"  birthAddress="indiBirthAddress"
                // date naissance record plus précise que date naissance  gedcom
                record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "", "", "", "xxxx yyy", "", "","", "indicomment");

                Gedcom gedcom = TestUtility.createGedcom();
                Indi indi = (Indi)gedcom.getEntity("sansfamille1");
                indi.getProperty("BIRT").getProperty("DATE").setValue("2000");
                indi.getProperty("BIRT").addProperty("PLAC", "indiBirthPlace");
                indi.getProperty("BIRT").addProperty("ADDR", "indiBirthAddress");

                TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, record);
                MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
                mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

                assertEquals("Indi : birth place",record.getFieldValue(FieldType.indiFatherResidence), indi.getBirthPlace().getValue());
                assertEquals("Indi : birth address",record.getFieldValue(FieldType.indiFatherAddress), indi.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            }

            {
                // record birthPlace="indiBirthPlace"  birthAddress="indiBirthAddress"
                // entity birthPlace="indiBirthPlace"  birthAddress="indiBirthAddress"
                record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "", "", "indiBirthPlace", "indiBirthAddress", "", "","", "indicomment");

                Gedcom gedcom = TestUtility.createGedcom();
                Indi indi = (Indi)gedcom.getEntity("sansfamille1");
                indi.getProperty("BIRT").addProperty("PLAC", "indiBirthPlace");
                indi.getProperty("BIRT").addProperty("ADDR", "indiBirthAddress");

                TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, record);
                MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
                mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

                assertEquals("Indi : birth place",record.getFieldValue(FieldType.indiBirthPlace), indi.getBirthPlace().getValue());
                assertEquals("Indi : birth address",record.getFieldValue(FieldType.indiBirthAddress), indi.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            }

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }


    /**
     * test_RecordBirth_copyRecordToEntity_Date
     */
     @Test
    public void test_RecordBirth_copyRecordToEntity_Sex() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("sansfamille1");
            RecordInfoPlace infoPlace = getRecordsInfoPlace();
            String fileName = "";
            Record record = createBirthRecord("sansfamille1");
            TransferableRecord.TransferableData data;

            record.setFieldValue(FieldType.indiSex, "F");
            data = RecordTransferHandle.createTransferableData(null, infoPlace, fileName, record);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, indi);
            assertEquals("Nombre model",2,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();
            assertEquals("record.inidSex = F , indi.sex = M, resultat=M", PropertySex.MALE, indi.getSex());

            record.setFieldValue(FieldType.indiSex, "");
            data = RecordTransferHandle.createTransferableData(null, infoPlace, fileName, record);
            mergeManager = TestUtility.createMergeManager(data, gedcom, indi);
            assertEquals("Nombre model",1,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();
            assertEquals("record.inidSex = U , indi.sex = M resultat= M", PropertySex.MALE, indi.getSex());

            record.setFieldValue(FieldType.indiSex, "");
            indi.setSex(PropertySex.UNKNOWN);
            data = RecordTransferHandle.createTransferableData(null, infoPlace, fileName, record);
            mergeManager = TestUtility.createMergeManager(data, gedcom, indi);
            assertEquals("Nombre model",1,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();
            assertEquals("record.inidSex = U , indi.sex = U , resultat= U", PropertySex.UNKNOWN,indi.getSex());

            record.setFieldValue(FieldType.indiSex, "M");
            indi.setSex(PropertySex.UNKNOWN);
            data = RecordTransferHandle.createTransferableData(null, infoPlace, fileName, record);

            mergeManager = TestUtility.createMergeManager(data, gedcom, indi);
            assertEquals("Nombre model",1,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();
            assertEquals("record.inidSex = M , indi.sex = U ,resultat= M", PropertySex.MALE,indi.getSex());


        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail(ex.getMessage());
        }
    }


    /**
     * testMergeRecordBirth avec nouvelle source et avec le lieu avec des coordonnées
     */
     @Test
    public void test_MergeRecordBirth_newMariage_fatherMother() {
        try {
            RecordBirth record;

            Gedcom gedcom = TestUtility.createGedcomEmpty();
            {
                Property birth;
                Indi father = (Indi) gedcom.createEntity(Gedcom.INDI);
                father.setName("first", "NEWFATHER");
                father.setSex(PropertySex.MALE);
                birth = father.addProperty("BIRT", "");
                birth.addProperty("DATE", "BEF 1970", 1);

                Indi mother = (Indi) gedcom.createEntity(Gedcom.INDI);
                mother.setName("second", "NEWMOTHER");
                mother.setSex(PropertySex.FEMALE);
                birth = mother.addProperty("BIRT", "");
                birth.addProperty("DATE", "01 JAN 1972", 1);
            }

            PlaceFormatModel.getCurrentModel().savePreferences(1,2,3,4,5,6);

            // cas : indiBirthPlace = ""
            record = new RecordBirth();
            record.setFieldValue(FieldType.eventDate, "01/01/2000");
            record.setFieldValue(FieldType.cote, "cote");
            record.setFieldValue(FieldType.freeComment,  "photo");
            record.setIndi("one", "NEWFATHER", "M", "","", "indiBirthplace", "birthAddress", "indiOccupation", "indiResidence", "indiAddress", "indiComment");
            record.setIndiFather("first", "NEWFATHER", "fatherOccupation", "indiFatherResidence", "indiFatherAddress", "comment", "", "30y");
            record.setIndiMother("second", "NEWMOTHER", "motherOccupation", "indiMotherResidence", "indiMotherAddress", "comment", "", "28y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
            record.setFieldValue(FieldType.generalComment, "generalcomment");

            String fileName = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, record);

//TestUtility.showMergeDialog(data, gedcom, null);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
            assertEquals("Nombre model",2,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();
            MergeRecord mergeRecord = mergeManager.getMergeRecord();

            Indi indi = (Indi) mergeManager.getProposalList1().getElementAt(0).getMainEntity();
            assertEquals("Indi : Lieu de naissance",mergeRecord.getIndi().getBirthResidence().getPlace(), indi.getBirthPlace().getValue());
            assertEquals("Indi : adresse naissance",mergeRecord.getIndi().getBirthResidence().getAddress(), indi.getValue(new TagPath("INDI:BIRT:ADDR"), ""));

            Indi father = indi.getBiologicalFather();
            assertEquals("father FirstName", mergeRecord.getIndi().getFather().getFirstName(), father.getFirstName());
            assertEquals("father LastName", mergeRecord.getIndi().getFather().getLastName(), father.getLastName());

            Indi mother = indi.getBiologicalMother();
            assertEquals("father FirstName", mergeRecord.getIndi().getMother().getFirstName(), mother.getFirstName());
            assertEquals("father LastName", mergeRecord.getIndi().getMother().getLastName(), mother.getLastName());


        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }



}
