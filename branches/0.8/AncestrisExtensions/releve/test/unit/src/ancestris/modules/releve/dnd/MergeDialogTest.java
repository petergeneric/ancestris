package ancestris.modules.releve.dnd;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.FieldPlace;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import javax.swing.JFrame;
import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class MergeDialogTest extends TestCase {

    /**
     * testMergeRecordBirth avec source existante
     */
    public void testMergeRecordMarriage() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            RecordMarriage record = MergeModelMarriageTest.createMarriageRecord("M1");
            MergeRecord mergeRecord = new MergeRecord(MergeModelMarriageTest.getRecordsInfoPlace(),  "", record);

            MergeDialog dialog = MergeDialog.show(new JFrame(), gedcom, null, mergeRecord, false);
            dialog.copyRecordToEntity();
            dialog.componentClosed();
            dialog.dispose();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

   /**
     * testMergeRecordBirth avec source existante
     */
    public void testMergeRecordBirth() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("child2");

            RecordBirth record = new RecordBirth();
            record.setEventDate("01/01/2000");
            record.setCote("cote");
            record.setFreeComment("photo");
            record.setIndi("Firstname1", "FATHERLASTNAME", "M", "", "", "place", "occupation", "indiResidence", "comment");
            record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "occupation", "indiFatherResidence", "comment", "dead", "70y");
            record.setIndiMother("mothername", "MOTHERLASTNAME", "occupation", "indiMoatherResidence", "comment", "dead", "72y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
            FieldPlace recordsInfoPlace = new FieldPlace();
            recordsInfoPlace.setValue("Paris,75000,,state,country");
            String sourceTitle = "";
            MergeRecord mergeRecord = new MergeRecord(MergeModelMarriageTest.getRecordsInfoPlace(),  sourceTitle, record);

            MergeDialog dialog = MergeDialog.show(new JFrame(), gedcom, null, mergeRecord, false);
            dialog.copyRecordToEntity();
            dialog.componentClosed();
            dialog.dispose();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * testMergeRecordBirth avec nouvel individu
     */
    public void testMergeRecordBirthIndiNull() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = null;

            RecordBirth record = new RecordBirth();
            record.setEventDate("01/01/2000");
            record.setCote("cote");
            record.setFreeComment("photo");
            record.setIndi("Firstname1", "FATHERLASTNAME", "M", "", "", "place", "occupation", "indiResidence", "comment");
            record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "occupation", "indiFatherResidence", "comment", "dead", "70y");
            record.setIndiMother("mothername", "MOTHERLASTNAME", "occupation", "indiMotherResidence", "comment", "dead", "72y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

            FieldPlace recordsInfoPlace = new FieldPlace();
            recordsInfoPlace.setValue("Paris,75000,,state,country");
            String sourceTitle = "";
            MergeRecord mergeRecord = new MergeRecord(MergeModelMarriageTest.getRecordsInfoPlace(),  sourceTitle, record);
            MergeDialog dialog = MergeDialog.show(new JFrame(), gedcom, indi, mergeRecord, false);
            dialog.copyRecordToEntity();
            dialog.componentClosed();
            dialog.dispose();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * testMergeRecordBirth avec nouvel individu
     */
    public void testMergeRecordDeathIndiNull() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = null;

            RecordDeath record = new RecordDeath();
            record.setEventDate("01/01/2003");
            record.setCote("cote");
            record.setFreeComment("photo");
            record.setIndi("Firstname1", "FATHERLASTNAME", "M", "3y", "", "place", "occupation", "indiResidence", "comment");
            record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "occupation", "indiFatherResidence", "comment", "dead", "70y");
            record.setIndiMother("mothername", "MOTHERLASTNAME", "occupation", "indiMotherResidence", "comment", "dead", "72y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
            
            FieldPlace recordsInfoPlace = new FieldPlace();
            recordsInfoPlace.setValue("Paris,75000,,state,country");
            String sourceTitle = "";
            MergeRecord mergeRecord = new MergeRecord(MergeModelMarriageTest.getRecordsInfoPlace(),  sourceTitle, record);
            MergeDialog dialog = MergeDialog.show(new JFrame(), gedcom, indi, mergeRecord, false);
            dialog.copyRecordToEntity();
            dialog.componentClosed();
            dialog.dispose();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
}
