package ancestris.modules.releve.dnd;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.RecordBirth;
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
    public void testMergeRecordBirth() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("child2");

            RecordBirth record = new RecordBirth();
            record.setEventDate("01/01/2000");
            record.setCote("cote");
            record.setFreeComment("photo");
            record.setIndi("Firstname1", "FATHERLASTNAME", "M", "", "", "place", "occupation", "comment");
            record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "occupation", "comment", "dead");
            record.setIndiMother("mothername", "MOTHERLASTNAME", "occupation", "comment", "dead");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
            record.setEventPlace("Paris","75000","","state","country");

            MergeDialog dialog = MergeDialog.show(new JFrame(), gedcom, indi, record, false);
            dialog.tableModel.copyRecordToEntity();
            dialog.dispose();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * testMergeRecordBirth avec source existante
     */
    public void testMergeRecordBirthIndiNull() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = null;

            RecordBirth record = new RecordBirth();
            record.setEventDate("01/01/2000");
            record.setCote("cote");
            record.setFreeComment("photo");
            record.setIndi("Firstname1", "FATHERLASTNAME", "M", "", "", "place", "occupation", "comment");
            record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "occupation", "comment", "dead");
            record.setIndiMother("mothername", "MOTHERLASTNAME", "occupation", "comment", "dead");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
            record.setEventPlace("Paris","75000","","state","country");

            MergeDialog dialog = MergeDialog.show(new JFrame(), gedcom, indi, record, false);
            dialog.tableModel.copyRecordToEntity();
            dialog.dispose();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
}
