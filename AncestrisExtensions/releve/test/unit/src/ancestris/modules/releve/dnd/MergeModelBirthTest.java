package ancestris.modules.releve.dnd;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.dnd.MergeRecord.MergeParticipantType;
import ancestris.modules.releve.model.FieldPlace;
import ancestris.modules.releve.model.RecordBirth;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class MergeModelBirthTest extends TestCase {

    static public FieldPlace getRecordsInfoPlace() {
        FieldPlace recordsInfoPlace = new FieldPlace();
        recordsInfoPlace.setValue("Paris,75000,,state,country");
        return recordsInfoPlace;
    }

    static public RecordBirth createBirthRecord(String firstName) {

        if ( firstName.equals("sansfamille1")) {
            RecordBirth record = new RecordBirth();
                record.setEventDate("01/01/2000");
                record.setCote("cote");
                record.setFreeComment("photo");
                record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "", "", "indiBirthplace", "indioccupation", "indiResidence", "indicomment");
                record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "indiFtaerResidence", "indiFatherComment", "", "70y");
                record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "indiMotherComment", "true", "72y");
                record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
                record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
                record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
                record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
                record.setGeneralComment("generalcomment");
            return record;
        } if ( firstName.equals("child1")) {
            RecordBirth record = new RecordBirth();
                record.setEventDate("01/01/2000");
                record.setCote("cote");
                record.setFreeComment("photo");
                record.setIndi("OneFirstName", "FATHERLASTNAME", "F", "", "", "indiBirthplace", "indioccupation", "indiResidence", "indicomment");
                record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "indiFatherResidence", "indiFatherComment", "dead", "70y");
                record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "indiMotherComment", "dead", "72y");
                record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
                record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
                record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
                record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
                record.setGeneralComment("generalcomment");
            return record;
        } else {
            RecordBirth record = new RecordBirth();
                record.setEventDate("01/01/1980");
                record.setCote("cote");
                record.setFreeComment("photo");
                record.setIndi("Fatherfirstname", "FATHERLASTNAME", "M", "BEF 1981", "", "indiBirthplace", "indioccupation", "indiResidence", "indicomment");
                //record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "occupation", "indiFatherResidence", "indiFatherComment", "dead");
                //record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "occupation", "indiMotherResidence", "indiMotherComment", "dead");
                record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
                record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
                record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
                record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
                record.setGeneralComment("generalcomment");
            return record;
        }
    }


    /**
     * test_RecordBirth_copyRecordToEntity_Date
     */
    public void test_RecordBirth_copyRecordToEntity_Date() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("sansfamille1");
            String sourceTitle = "";
            MergeRecord mergeRecord = new MergeRecord(getRecordsInfoPlace(), sourceTitle, createBirthRecord("sansfamille1"));
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

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * test_RecordBirth_copyRecordToEntity_Comment
     */
    public void test_RecordBirth_copyRecordToEntity_Comment() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("sansfamille1");
            RecordBirth record = createBirthRecord("sansfamille1");
            String sourceTitle = "";
            MergeRecord mergeRecord = new MergeRecord(getRecordsInfoPlace(), sourceTitle, record);
            List<MergeModel> models;

            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",3,models.size());
            models.get(0).copyRecordToEntity();

            String expected = "";
            expected +="Date de l'acte: 01/01/2000\n";
            expected +="Nouveau né: sansfamille1 FATHERLASTNAME, né à indiBirthplace, indicomment\n";
            expected +="Père: Fatherfirstname FATHERLASTNAME, 70 années, fatherOccupation, domicile indiFtaerResidence, indiFatherComment\n";
            expected +="Mère: Motherfirstname MOTHERLASTNAME, 72 années, Décédé, motherOccupation, domicile indiMotherResidence, indiMotherComment\n";
            expected +="Parrain/témoin: w1firstname w1lastname, w1occupation, w1comment\n";
            expected +="Marraine/témoin: w2firstname w2lastname, w2occupation, w2comment\n";
            expected +="Témoin(s): w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment\n";
            expected +="Commentaire général: generalcomment\n";
            expected +="Photo: photo";
            assertEquals("comment1",expected, indi.getPropertyByPath("INDI:BIRT:NOTE").getValue());

            // je verifie que le nouveau commentaire contient la concatenation de l'ancien commentaire et du nouveau
            indi.getPropertyByPath("INDI:BIRT:NOTE").setValue("oldcomment");
            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",1,models.size());
            models.get(0).copyRecordToEntity();
            expected ="Date de l'acte: 01/01/2000\n";
            expected +="Nouveau né: sansfamille1 FATHERLASTNAME, né à indiBirthplace, indicomment\n";
            expected +="Père: Fatherfirstname FATHERLASTNAME, 70 années, fatherOccupation, domicile indiFtaerResidence, indiFatherComment\n";
            expected +="Mère: Motherfirstname MOTHERLASTNAME, 72 années, Décédé, motherOccupation, domicile indiMotherResidence, indiMotherComment\n";
            expected +="Parrain/témoin: w1firstname w1lastname, w1occupation, w1comment\n";
            expected +="Marraine/témoin: w2firstname w2lastname, w2occupation, w2comment\n";
            expected +="Témoin(s): w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment\n";
            expected +="Commentaire général: generalcomment\n";
            expected +="Photo: photo\n";
            expected += "oldcomment";
            assertEquals("comment2",expected, indi.getPropertyByPath("INDI:BIRT:NOTE").getValue());
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    
    /**
     * testMergeRecordBirth avec source existante
     */
    public void testMergeRecordBirthSourceExistanteDejaAssociee() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("I1");
            List<MergeModel> models;
            RecordBirth record = createBirthRecord("I1");
            String sourceTitle = "BMS Paris";
            MergeRecord mergeRecord = new MergeRecord(getRecordsInfoPlace(), sourceTitle, record);
            
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
    public void testMergeRecordBirthSourceExistanteNonAssociee() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("I1");
            List<MergeModel> models;

            RecordBirth record= createBirthRecord("child2");
            FieldPlace recordsInfoPlace = new FieldPlace();
            recordsInfoPlace.setValue("Brest,35000,,state,country");
            String sourceTitle = "";
            MergeRecord mergeRecord = new MergeRecord(recordsInfoPlace, sourceTitle, record);
            
            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",1,models.size());
            models.get(0).copyRecordToEntity();
            assertEquals("IndiFirstName",mergeRecord.getIndi().getFirstName(), indi.getFirstName());
            assertEquals("IndiLastName",mergeRecord.getIndi().getLastName(), indi.getLastName());
            assertEquals("IndiSex",mergeRecord.getIndi().getSex(), indi.getSex());
            assertNotSame("IndiBirthDate",mergeRecord.getEventDate().getValue(), indi.getBirthDate().getValue());
            Property[] sourceLink = indi.getProperties(new TagPath("INDI:BIRT:SOUR"));
            assertEquals("Nb birthsource",2,sourceLink.length );
            assertEquals("Source title 0","BMS Paris", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );
            assertEquals("Source title 1","", ((Source)((PropertyXRef)sourceLink[1]).getTargetEntity()).getTitle() );

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * testMergeRecordBirth avec nouvelle source
     */
    public void testMergeRecordBirthSourceNewSource() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();

            Indi indi = (Indi)gedcom.getEntity("I1");
            List<MergeModel> models;

            RecordBirth record = createBirthRecord("I1");
            FieldPlace recordsInfoPlace = new FieldPlace();
            recordsInfoPlace.setValue("Paris,75009,,state,country");
            String sourceTitle = "BMS Paris";
            MergeRecord mergeRecord = new MergeRecord(recordsInfoPlace, sourceTitle, record);
            
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
    public void testMergeRecordBirthAutreIndiExistant() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("I1");
            RecordBirth record = createBirthRecord("I1");
            String sourceTitle = "BMS Paris";
            MergeRecord mergeRecord = new MergeRecord(getRecordsInfoPlace(), sourceTitle, record);
            List<MergeModel> models;

            // je renseigne la meme date de naissance
            record.getEventDateProperty().setValue(indi.getBirthDate().getValue());            
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
            //assertEquals("sourec 1","BMS Paris", ((Source)((PropertyXRef)sourceLink[1]).getTargetEntity()).getTitle() );

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }


    /**
     * testMergeRecordBirth avec nouvelle source
     */
    public void testMergeRecordBirthIndiBirthPlace() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            RecordBirth record;
            MergeRecord mergeRecord;

            // cas : indiBirthPlace = ""
            record = new RecordBirth();
            record.setEventDate("01/01/2000");
            record.setCote("cote");
            record.setFreeComment("photo");
            record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "", "", "", "", "", "indicomment");
            record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "indiFatherResidence", "comment", "", "70y");
            record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "comment", "dead", "72y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
            record.setGeneralComment("generalcomment");

            String sourceTitle = "";
            mergeRecord = new MergeRecord(getRecordsInfoPlace(), sourceTitle, record);
            assertEquals("Indi Birth place=IndiFatherResidence",record.getIndi().getFatherResidence().toString(), mergeRecord.getIndi().getBirthPlace());

             // cas : indiBirthPlace = "" et indiFatherResidence = ""
            record = new RecordBirth();
            record.setEventDate("01/01/2000");
            record.setCote("cote");
            record.setFreeComment("photo");
            record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "", "", "", "", "", "indicomment");
            record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "", "comment", "", "70y");
            record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "comment", "dead", "72y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
            record.setGeneralComment("generalcomment");

            mergeRecord = new MergeRecord(getRecordsInfoPlace(), sourceTitle, record);
            assertEquals("Indi Birth place=eventPlace",getRecordsInfoPlace().toString(), mergeRecord.getIndi().getBirthPlace());

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }


}
