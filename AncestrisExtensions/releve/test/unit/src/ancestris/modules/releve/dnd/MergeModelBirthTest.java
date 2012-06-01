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
                record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "", "", "indiplace", "indioccupation", "indicomment");
                record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "comment", "dead");
                record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "comment", "dead");
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
                record.setIndi("OneFirstName", "FATHERLASTNAME", "F", "", "", "indiplace", "indioccupation", "indicomment");
                record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "comment", "dead");
                record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "comment", "dead");
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
                record.setIndi("Fatherfirstname", "FATHERLASTNAME", "M", "BEF 1981", "", "indiplace", "indioccupation", "indicomment");
                //record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "occupation", "comment", "dead");
                //record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "occupation", "comment", "dead");
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
     * testSaveDataComment
     */
    public void testSaveDataMarriageDate() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("sansfamille1");
            MergeRecord mergeRecord = new MergeRecord(createBirthRecord("sansfamille1"));
            List<MergeModel> models;
            // je memorise la date de naissance du pere
            String fatherBirthDate = ((Indi)gedcom.getEntity("I1")).getBirthDate().getDisplayValue();

            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);

            assertEquals("Nombre model",3,models.size());
            models.get(1).copyRecordToEntity();

            assertEquals("famille","F1", indi.getFamilyWhereBiologicalChild().getId());

            Indi father = indi.getBiologicalFather();
            assertEquals("fatherFirstName",mergeRecord.getIndiFatherFirstName(), father.getFirstName());
            // la date de naissance du pere n'est pas changée car elle est plus précise que celle du releve
            assertEquals("Naissance du pere",fatherBirthDate, father.getBirthDate().getDisplayValue());
            assertEquals("deces du pere",   "apr 1999", father.getDeathDate().getDisplayValue());

            Indi mother = indi.getBiologicalMother();
            assertEquals("Naissance de la mere","ava 1985", mother.getBirthDate().getDisplayValue());
            assertEquals("deces de la mere",   "apr 1999", mother.getDeathDate().getDisplayValue());

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }


    /**
     * testSaveDataComment
     */
    public void testSaveDataParent() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("sansfamille1");
            MergeRecord mergeRecord = new MergeRecord(createBirthRecord("sansfamille1"));
            List<MergeModel> models;

            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",3,models.size());
            models.get(0).copyRecordToEntity();

            assertEquals("famille","F1", indi.getFamilyWhereBiologicalChild().getId());

            assertEquals("Mariage date","BEF 2000", indi.getFamilyWhereBiologicalChild().getMarriageDate().getValue());

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }


    /**
     * testSaveDataComment
     */
    public void testSaveDataComment() {
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
            expected +="generalcomment, photo\n";
            expected +="Commentaire père: comment\n";
            expected +="Commentaire mère: comment\n";
            expected +="Parrain/témoin: w1firstname w1lastname, w1occupation, w1comment\n";
            expected +="Marraine/témoin: w2firstname w2lastname, w2occupation, w2comment\n";
            expected +="Témoin(s): w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment";
            assertEquals("comment1",expected, indi.getPropertyByPath("INDI:BIRT:NOTE").getValue());

            // je verifie que le nouveau commentaire contient la concatenation de l'ancien commentaire et du nouveau
            indi.getPropertyByPath("INDI:BIRT:NOTE").setValue("oldcomment");
            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",1,models.size());
            models.get(0).copyRecordToEntity();
            expected = "oldcomment\n";
            expected +="indicomment\n";
            expected +="generalcomment, photo\n";
            expected +="Commentaire père: comment\n";
            expected +="Commentaire mère: comment\n";
            expected +="Parrain/témoin: w1firstname w1lastname, w1occupation, w1comment\n";
            expected +="Marraine/témoin: w2firstname w2lastname, w2occupation, w2comment\n";
            expected +="Témoin(s): w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment";
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
            assertEquals("IndiBirthDate","75000 Paris BMS", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );

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
            assertEquals("IndiBirthDate","35000 Brest Etat civil", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );

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
            assertEquals("Nb birthsource",2,sourceLink.length );
            assertEquals("IndiBirthDate","75009 Paris Etat civil", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );

        } catch (Exception ex) {
            ex.printStackTrace();
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

            assertEquals("otherIndi",0, MergeQuery.findIndiCompatibleWithBirthRecord(mergeRecord, gedcom, indi).size());

            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",1,models.size());
            models.get(0).copyRecordToEntity();
            assertEquals("Indi First Name",mergeRecord.getIndiFirstName(), indi.getFirstName());
            assertEquals("Indi Last Name",mergeRecord.getIndiLastName(), indi.getLastName());
            assertEquals("Indi Sex",mergeRecord.getIndiSex(), indi.getSex());
            assertEquals("Indi Birth Date",0, indi.getBirthDate().compareTo(mergeRecord.getEventDate()));
            Property[] sourceLink = indi.getProperties(new TagPath("INDI:BIRT:SOUR"));
            assertEquals("Nb birthsource",2,sourceLink.length );
            assertEquals("source 0","75009 Paris Etat civil", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );
            assertEquals("sourec 1","75000 Paris BMS", ((Source)((PropertyXRef)sourceLink[1]).getTargetEntity()).getTitle() );

        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }


}
