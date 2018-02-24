package ancestris.modules.releve.merge;

import ancestris.modules.releve.RecordTransferHandle;
import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.dnd.TransferableRecord;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordInfoPlace;
import ancestris.modules.releve.model.RecordMarriage;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import javax.swing.JFrame;
import junit.framework.TestCase;
import org.junit.Test;
import org.openide.util.Exceptions;

/**
 *
 * @author Michel
 */
public class MergeDialogTest extends TestCase {

    /**
     * testMergeRecordBirth avec source existante
     */
    @Test
    public void testMergeRecordMarriage() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            RecordMarriage record = MergeModelMarriageTest.createMarriageRecord("M1");
            
            RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
            recordsInfoPlace.setValue("Paris,75000,,state,country");
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, recordsInfoPlace, "fileName", record);
            MergeRecord mergeRecord = new MergeRecord(data);

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
    @Test
    public void testMergeRecordBirth() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("child2");

            RecordBirth record = new RecordBirth();
            record.setFieldValue(FieldType.eventDate, "01/01/2000");
            record.setFieldValue(FieldType.cote, "cote");
            record.setFieldValue(FieldType.freeComment,  "photo");
            record.setIndi("Firstname1", "FATHERLASTNAME", "M", "", "", "place", "birthAddress", "occupation", "indiResidence", "indiAaddress", "comment");
            record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "occupation", "indiFatherResidence", "indiFatherAddress", "comment", "dead", "70y");
            record.setIndiMother("mothername", "MOTHERLASTNAME", "occupation", "indiMoatherResidence", "indiMotherAddress", "comment", "dead", "72y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
            RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
            recordsInfoPlace.setValue("Paris,75000,,state,country");
            String sourceTitle = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, recordsInfoPlace,sourceTitle, record);
            MergeRecord mergeRecord = new MergeRecord(data);
            
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
    @Test
    public void testMergeRecordBirthIndiNull() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = null;

            RecordBirth record = new RecordBirth();
            record.setFieldValue(FieldType.eventDate, "01/01/2000");
            record.setFieldValue(FieldType.cote, "cote");
            record.setFieldValue(FieldType.freeComment,  "photo");
            record.setIndi("Firstname1", "FATHERLASTNAME", "M", "", "", "place", "birthAddress", "occupation", "indiResidence", "indiAddress", "comment");
            record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "occupation", "indiFatherResidence", "indiFatherAddress", "comment", "dead", "70y");
            record.setIndiMother("mothername", "MOTHERLASTNAME", "occupation", "indiMotherResidence", "indiMotherAddress", "comment", "dead", "72y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

            RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
            recordsInfoPlace.setValue("Paris,75000,,state,country");
            String sourceTitle = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, recordsInfoPlace,sourceTitle, record);
            MergeRecord mergeRecord = new MergeRecord(data);
            MergeDialog dialog = MergeDialog.show(new JFrame(), gedcom, indi, mergeRecord, false);
            dialog.copyRecordToEntity();
            dialog.componentClosed();
            dialog.dispose();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail(ex.getMessage());
        }
    }

    /**
     * testMergeRecordBirth avec nouvel individu
     */
    @Test
    public void testMergeRecordDeathIndiNull() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = null;

            RecordDeath record = new RecordDeath();
            record.setFieldValue(FieldType.eventDate, "01/01/2003");
            record.setFieldValue(FieldType.cote, "cote");
            record.setFieldValue(FieldType.freeComment,  "photo");
            record.setIndi("Firstname1", "FATHERLASTNAME", "M", "3y", "", "place", "birthAddress", "occupation", "indiResidence", "indiAddress", "comment");
            record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "occupation", "indiFatherResidence", "indiFatherAddress", "comment", "dead", "70y");
            record.setIndiMother("mothername", "MOTHERLASTNAME", "occupation", "indiMotherResidence", "indiMotherAddress", "comment", "dead", "72y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
            
            RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
            recordsInfoPlace.setValue("Paris,75000,,state,country");
            String sourceTitle = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, recordsInfoPlace,sourceTitle, record);
            MergeRecord mergeRecord = new MergeRecord(data);
            MergeDialog dialog = MergeDialog.show(new JFrame(), gedcom, indi, mergeRecord, false);
            dialog.copyRecordToEntity();
            dialog.componentClosed();
            dialog.dispose();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
}
