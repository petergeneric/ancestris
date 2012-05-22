package ancestris.modules.releve.dnd;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.RecordBirth;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class MergeModelBirthTest extends TestCase {

    /**
     * testSaveDataComment
     */
    public void testSaveDataParent() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("sansfamille1");
            RecordBirth record = TestUtility.createBirthRecord("sansfamille1");
            MergeModel mergeModel;

            mergeModel = MergeModel.createMergeModel(record, gedcom, indi);
            mergeModel.copyRecordToEntity();

            Indi father = indi.getBiologicalFather();
            assertEquals("fatherFirstName",record.getIndiFatherFirstName().toString(), father.getFirstName());
            // la date de naissance du pere n'est pas changée car elle est déjà preceise
            assertEquals("Naissance du pere","1 jan 1970", father.getBirthDate().getDisplayValue());
            assertEquals("deces du pere",   "apr 1999", father.getDeathDate().getDisplayValue());

            Indi mother = indi.getBiologicalMother();
            assertEquals("Naissance du pere","ava 1985", mother.getBirthDate().getDisplayValue());
            assertEquals("deces du pere",   "apr 2000", mother.getDeathDate().getDisplayValue());

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
            RecordBirth record = TestUtility.createBirthRecord("sansfamille1");
            MergeModel mergeModel;

            mergeModel = MergeModel.createMergeModel(record, gedcom, indi);
            mergeModel.copyRecordToEntity();
            String expected = "";
            expected +="indicomment, generalcomment\n";
            expected +="Parrain: w1firstname w1lastname, w1occupation, w1comment\n";
            expected +="Marraine: w2firstname w2lastname, w2occupation, w2comment\n";
            expected +="Témoin(s): w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment";
            assertEquals("comment1",expected, indi.getPropertyByPath("INDI:BIRT:NOTE").getValue());

            // je verifie que le nouveau commentaire contient la concatenation de l'ancien commentaire et du nouveau
            indi.getPropertyByPath("INDI:BIRT:NOTE").setValue("oldcomment");
            mergeModel = MergeModel.createMergeModel(record, gedcom, indi);
            mergeModel.copyRecordToEntity();
            expected = "oldcomment\n";
            expected +="indicomment, generalcomment\n";
            expected +="Parrain: w1firstname w1lastname, w1occupation, w1comment\n";
            expected +="Marraine: w2firstname w2lastname, w2occupation, w2comment\n";
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
            MergeModel mergeModel;

            RecordBirth record = TestUtility.createBirthRecord("I1");

            record.setEventPlace("Paris","75000","","state","country");

            mergeModel = MergeModel.createMergeModel(record, gedcom, indi);
            mergeModel.copyRecordToEntity();
            assertEquals("IndiFirstName",record.getIndiFirstName().toString(), indi.getFirstName());
            assertEquals("IndiLastName",record.getIndiLastName().toString(), indi.getLastName());
            assertEquals("IndiSex",record.getIndiSex().getSex(), indi.getSex());
            assertEquals("IndiBirthDate",record.getEventDateField().getValue(), indi.getBirthDate().getValue());
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
            MergeModel mergeModel;

            RecordBirth record = TestUtility.createBirthRecord("I1");

            record.setEventPlace("Brest","35000","","state","country");

            mergeModel = MergeModel.createMergeModel(record, gedcom, indi);
            mergeModel.copyRecordToEntity();
            assertEquals("IndiFirstName",record.getIndiFirstName().toString(), indi.getFirstName());
            assertEquals("IndiLastName",record.getIndiLastName().toString(), indi.getLastName());
            assertEquals("IndiSex",record.getIndiSex().getSex(), indi.getSex());
            assertEquals("IndiBirthDate",0, indi.getBirthDate().compareTo(record.getEventDateField()));
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
            MergeModel mergeModel;

            RecordBirth record = TestUtility.createBirthRecord("I1");

            record.setEventPlace("Paris","75009","","state","country");

            mergeModel = MergeModel.createMergeModel(record, gedcom, indi);
            mergeModel.copyRecordToEntity();
            assertEquals("IndiFirstName",record.getIndiFirstName().toString(), indi.getFirstName());
            assertEquals("IndiLastName",record.getIndiLastName().toString(), indi.getLastName());
            assertEquals("IndiSex",record.getIndiSex().getSex(), indi.getSex());
            assertEquals("IndiBirthDate",0, indi.getBirthDate().compareTo(record.getEventDateField()));
            Property[] sourceLink = indi.getProperties(new TagPath("INDI:BIRT:SOUR"));
            assertEquals("Nb birthsource",2,sourceLink.length );
            assertEquals("IndiBirthDate","75009 Paris Etat civil", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );

        } catch (Exception ex) {
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
            RecordBirth record = TestUtility.createBirthRecord("I1");
            MergeModel mergeModel;

            // je renseigne la meme date de naissance
            record.getEventDateField().setValue(indi.getBirthDate().getValue());
            record.setEventPlace("Paris","75009","","state","country");

            assertEquals("otherIndi",0, MergeModel.findSameIndi(record, gedcom, indi).size());

            mergeModel = MergeModel.createMergeModel(record, gedcom, indi);
            mergeModel.copyRecordToEntity();
            assertEquals("Indi First Name",record.getIndiFirstName().toString(), indi.getFirstName());
            assertEquals("Indi Last Name",record.getIndiLastName().toString(), indi.getLastName());
            assertEquals("Indi Sex",record.getIndiSex().getSex(), indi.getSex());
            assertEquals("Indi Birth Date",0, indi.getBirthDate().compareTo(record.getEventDateField()));
            Property[] sourceLink = indi.getProperties(new TagPath("INDI:BIRT:SOUR"));
            assertEquals("Nb birthsource",2,sourceLink.length );
            assertEquals("source 0","75009 Paris Etat civil", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );
            assertEquals("sourec 1","75000 Paris BMS", ((Source)((PropertyXRef)sourceLink[1]).getTargetEntity()).getTitle() );

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }


}
