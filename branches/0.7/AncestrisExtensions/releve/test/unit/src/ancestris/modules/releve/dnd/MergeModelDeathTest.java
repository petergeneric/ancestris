package ancestris.modules.releve.dnd;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.dnd.MergeModel.MergeRow;
import ancestris.modules.releve.model.RecordDeath;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import java.util.List;
import javax.swing.JFrame;
import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class MergeModelDeathTest extends TestCase {

    static public RecordDeath createDeathRecord(String firstName) {

        if ( firstName.equals("sansfamille1")) {
            RecordDeath record = new RecordDeath();
                record.setEventDate("01/01/2003");
                record.setCote("cote");
                record.setFreeComment("photo");
                record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "3y", "", "indiplace", "indioccupation", "indiResidence", "indicomment");
                record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "indiFatherResidence", "comment", "", "70y");
                record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "comment", "dead", "72y");
                record.setIndiMarried("Marriedfirstname", "MARRIEDLASTNAME", "marriedOccupation", "indiMarriedResidence", "marriedcomment", "dead");
                record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
                record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
                record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
                record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
                record.setEventPlace("Paris","75000","","state","country");
                record.setGeneralComment("generalcomment");
            return record;
        } else {
            RecordDeath record = new RecordDeath();
                record.setEventDate("01/01/1988");
                record.setCote("cote");
                record.setFreeComment("photo");
                record.setIndi("Fatherfirstname", "FATHERLASTNAME", "M", "8y", "BEF 1981", "indiplace", "indioccupation", "indiResidence", "indicomment");
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
     * test_RecordBirth_copyRecordToEntity_Date
     */
    public void test_RecordBirth_copyRecordToEntity_Date() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("sansfamille1");
            MergeRecord mergeRecord = new MergeRecord(createDeathRecord("sansfamille1"));
            List<MergeModel> models;

            // je memorise la date de naissance de l'enfant
            String previousIndiBirthDate = ((Indi)gedcom.getEntity("sansfamille1")).getBirthDate().getValue();
            // je memorise la date de naissance du pere
            String previousFatherBirthDate = ((Indi)gedcom.getEntity("I1")).getBirthDate().getValue();

            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);

            assertEquals("Nombre model",3,models.size());
            models.get(0).copyRecordToEntity();

            assertEquals("famille","F1", indi.getFamilyWhereBiologicalChild().getId());
            assertEquals("Mariage date","BEF 2000", indi.getFamilyWhereBiologicalChild().getMarriageDate().getValue());

            assertEquals("indiBirthDate",previousIndiBirthDate, indi.getBirthDate().getValue());

            Indi father = indi.getBiologicalFather();
            assertEquals("fatherFirstName",mergeRecord.getIndiFatherFirstName(), father.getFirstName());
            // la date de naissance du pere n'est pas changée car elle est plus précise que celle du releve
            assertEquals("Naissance du pere",previousFatherBirthDate, father.getBirthDate().getValue());
            assertEquals("deces du pere",   "AFT 1999", father.getDeathDate().getValue());

            Indi mother = indi.getBiologicalMother();
            assertEquals("Naissance de la mere","CAL 1931", mother.getBirthDate().getValue());
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
            RecordDeath record = createDeathRecord("sansfamille1");
            MergeRecord mergeRecord = new MergeRecord(record);
            List<MergeModel> models;

            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",3,models.size());
            models.get(0).copyRecordToEntity();

            String expected = "";
            expected +="indicomment\n";
            expected +="Témoin(s): w1firstname w1lastname, w1occupation, w1comment, w2firstname w2lastname, w2occupation, w2comment, w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment\n";
            expected +="Commentaire père: comment\n";
            expected +="Commentaire mère: comment\n";
            expected +="generalcomment\n";
            expected +="Age: "+record.getIndiAge().toString()+ "\n";
            expected +="Photo: photo";
            assertEquals("comment1",expected, indi.getPropertyByPath("INDI:DEAT:NOTE").getValue());

            // je verifie que le nouveau commentaire contient la concatenation de l'ancien commentaire et du nouveau
            indi.getPropertyByPath("INDI:DEAT:NOTE").setValue("oldcomment");
            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",1,models.size());
            models.get(0).copyRecordToEntity();
            expected ="indicomment\n";
            expected +="Témoin(s): w1firstname w1lastname, w1occupation, w1comment, w2firstname w2lastname, w2occupation, w2comment, w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment\n";
            expected +="Commentaire père: comment\n";
            expected +="Commentaire mère: comment\n";
            expected +="generalcomment\n";
            expected +="Age: "+record.getIndiAge().toString()+ "\n";
            expected +="Photo: photo\n";
            expected += "oldcomment";
            assertEquals("comment2",expected, indi.getPropertyByPath("INDI:DEAT:NOTE").getValue());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }


    /**
     * test de la recherche de l'ex conjoint
     */
    public void testFindMarried() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();

            RecordDeath record = new RecordDeath();
                record.setEventDate("01/01/1988");
                record.setCote("cote");
                record.setFreeComment("photo");
                record.setIndi("Fatherfirstname", "FATHERLASTNAME", "M", "", "BEF 1981", "indiplace", "indioccupation", "indiResidence", "indicomment");
                record.setIndiMarried("Motherfirstname", "MOTHERLASTNAME", "", "", "", "");
            MergeRecord mergeRecord = new MergeRecord(record);

            List<MergeModel> models;
            models = MergeModel.createMergeModel(mergeRecord, gedcom, null);
            assertEquals("Nombre model",2,models.size());


            MergeRow IndiMarriedLastNameRow =  models.get(0).getRow(MergeModel.RowType.IndiMarriedLastName);
            assertNotNull("IndiMarriedLastName", IndiMarriedLastNameRow.entityValue);
            assertEquals("IndiMarriedLastName not null", "MOTHERLASTNAME", IndiMarriedLastNameRow.entityValue.toString());
            //MergeDialog dialog = MergeDialog.show(new JFrame(), gedcom, null, record, true);
            //Thread.sleep(10000);
           
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }
    
}
