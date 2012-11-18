package ancestris.modules.releve.dnd;

import ancestris.modules.releve.TestUtility;
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

    static public RecordBirth createBirthRecord(String firstName) {

        if ( firstName.equals("sansfamille1")) {
            RecordBirth record = new RecordBirth();
                record.setEventDate("01/01/2000");
                record.setCote("cote");
                record.setFreeComment("photo");
                record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "", "", "indiplace", "indioccupation", "indiResidence", "indicomment");
                record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "indiFtaerResidence", "comment", "", "70y");
                record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "comment", "dead", "72y");
                record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
                record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
                record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
                record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
                record.setEventPlace("Paris","75000","","state","country");
                record.setGeneralComment("generalcomment");
            return record;
        } if ( firstName.equals("child1")) {
            RecordBirth record = new RecordBirth();
                record.setEventDate("01/01/2000");
                record.setCote("cote");
                record.setFreeComment("photo");
                record.setIndi("OneFirstName", "FATHERLASTNAME", "F", "", "", "indiplace", "indioccupation", "indiResidence", "indicomment");
                record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "indiFatherResidence", "comment", "dead", "70y");
                record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "comment", "dead", "72y");
                record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
                record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
                record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
                record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
                record.setEventPlace("Paris","75000","","state","country");
                record.setGeneralComment("generalcomment");
            return record;
        } else {
            RecordBirth record = new RecordBirth();
                record.setEventDate("01/01/1980");
                record.setCote("cote");
                record.setFreeComment("photo");
                record.setIndi("Fatherfirstname", "FATHERLASTNAME", "M", "BEF 1981", "", "indiplace", "indioccupation", "indiResidence", "indicomment");
                //record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "occupation", "indiFatherResidence", "comment", "dead");
                //record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "occupation", "indiMotherResidence", "comment", "dead");
                record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
                record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
                record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
                record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
                record.setEventPlace("Paris","75000","","state","country");
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
            MergeRecord mergeRecord = new MergeRecord(createBirthRecord("sansfamille1"));
            List<MergeModel> models;
            // je memorise la date de naissance du pere
            String previousFatherBirthDate = ((Indi)gedcom.getEntity("I1")).getBirthDate().getValue();

            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);

            assertEquals("Nombre model",3,models.size());
            models.get(0).copyRecordToEntity();

            assertEquals("famille","F1", indi.getFamilyWhereBiologicalChild().getId());
            assertEquals("Mariage date","BEF 2000", indi.getFamilyWhereBiologicalChild().getMarriageDate().getValue());

            assertEquals("indiBirthDate",mergeRecord.getIndiBirthDate().getValue(), indi.getBirthDate().getValue());

            Indi father = indi.getBiologicalFather();
            assertEquals("fatherFirstName",mergeRecord.getIndiFatherFirstName(), father.getFirstName());
            // la date de naissance du pere n'est pas changée car elle est plus précise que celle du releve
            assertEquals("Naissance du pere",previousFatherBirthDate, father.getBirthDate().getValue());
            assertEquals("deces du pere",   "AFT 1999", father.getDeathDate().getValue());

            Indi mother = indi.getBiologicalMother();
            assertEquals("Naissance de la mere","CAL 1928", mother.getBirthDate().getValue());
            assertEquals("deces de la mere",    "AFT 2000", mother.getDeathDate().getValue());

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
            MergeRecord mergeRecord = new MergeRecord(record);
            List<MergeModel> models;

            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",3,models.size());
            models.get(0).copyRecordToEntity();

            String expected = "";
            expected +="indicomment\n";
            expected +="Parrain/témoin: w1firstname w1lastname, w1occupation, w1comment\n";
            expected +="Marraine/témoin: w2firstname w2lastname, w2occupation, w2comment\n";
            expected +="Témoin(s): w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment\n";
            expected +="Commentaire père: comment\n";
            expected +="Commentaire mère: comment\n";
            expected +="generalcomment\n";
            expected +="Photo: photo";
            assertEquals("comment1",expected, indi.getPropertyByPath("INDI:BIRT:NOTE").getValue());

            // je verifie que le nouveau commentaire contient la concatenation de l'ancien commentaire et du nouveau
            indi.getPropertyByPath("INDI:BIRT:NOTE").setValue("oldcomment");
            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",1,models.size());
            models.get(0).copyRecordToEntity();
            expected ="indicomment\n";
            expected +="Parrain/témoin: w1firstname w1lastname, w1occupation, w1comment\n";
            expected +="Marraine/témoin: w2firstname w2lastname, w2occupation, w2comment\n";
            expected +="Témoin(s): w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment\n";
            expected +="Commentaire père: comment\n";
            expected +="Commentaire mère: comment\n";
            expected +="generalcomment\n";
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
            record.setEventPlace("Paris","75000","","state","country");
            MergeRecord mergeRecord = new MergeRecord(record);
            
            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",1,models.size());
            models.get(0).copyRecordToEntity();
            assertEquals("IndiFirstName",mergeRecord.getIndiFirstName(), indi.getFirstName());
            assertEquals("IndiLastName",mergeRecord.getIndiLastName(), indi.getLastName());
            assertEquals("IndiSex",mergeRecord.getIndiSex(), indi.getSex());
            assertNotSame("IndiBirthDate",mergeRecord.getEventDate().getValue(), indi.getBirthDate().getValue());
            Property[] sourceLink = indi.getProperties(new TagPath("INDI:BIRT:SOUR"));
            assertEquals("Nb birthsource",1,sourceLink.length );
            assertEquals("IndiBirthDate","BMS Paris", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * testMergeRecordBirth avec source existante mais non associee à la naissance
     */
    public void testMergeRecordBirthSourceExistanteNonAssociee() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("I1");
            List<MergeModel> models;

            RecordBirth record= createBirthRecord("I1");
            record.setEventPlace("Brest","35000","","state","country");
            MergeRecord mergeRecord = new MergeRecord(record);
            
            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",1,models.size());
            models.get(0).copyRecordToEntity();
            assertEquals("IndiFirstName",mergeRecord.getIndiFirstName(), indi.getFirstName());
            assertEquals("IndiLastName",mergeRecord.getIndiLastName(), indi.getLastName());
            assertEquals("IndiSex",mergeRecord.getIndiSex(), indi.getSex());
            assertNotSame("IndiBirthDate",mergeRecord.getEventDate().getValue(), indi.getBirthDate().getValue());
            Property[] sourceLink = indi.getProperties(new TagPath("INDI:BIRT:SOUR"));
            assertEquals("Nb birthsource",2,sourceLink.length );
            assertEquals("IndiBirthDate","Etat civil Brest", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );

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
            MergeRecord mergeRecord = new MergeRecord(record);
            record.setEventPlace("Paris","75009","","state","country");

            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",1,models.size());
            models.get(0).copyRecordToEntity();
            assertEquals("IndiFirstName",mergeRecord.getIndiFirstName(), indi.getFirstName());
            assertEquals("IndiLastName",mergeRecord.getIndiLastName(), indi.getLastName());
            assertEquals("IndiSex",mergeRecord.getIndiSex(), indi.getSex());
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
            MergeRecord mergeRecord = new MergeRecord(record);
            List<MergeModel> models;

            // je renseigne la meme date de naissance
            record.getEventDateProperty().setValue(indi.getBirthDate().getValue());
            record.setEventPlace("Paris","75009","","state","country");

            assertEquals("otherIndi",0, MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, indi).size());

            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",1,models.size());
            models.get(0).copyRecordToEntity();
            assertEquals("Indi First Name",mergeRecord.getIndiFirstName(), indi.getFirstName());
            assertEquals("Indi Last Name",mergeRecord.getIndiLastName(), indi.getLastName());
            assertEquals("Indi Sex",mergeRecord.getIndiSex(), indi.getSex());
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


}
