package ancestris.modules.releve.merge;

import ancestris.modules.releve.RecordTransferHandle;
import ancestris.modules.releve.model.FieldDead;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import genj.gedcom.GedcomException;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Michel
 */
public class MergeRecordTest {

    /**
     * test match age
     */
    @Test
    public void test_getYear() {

        PointInTime pit = new PointInTime(0,5,2000);
        Delta delta = new Delta(0,18,0);
        try {
            assertEquals("jours", "DEC 1998", MergeRecord.getYear(pit, delta).getValue());
        } catch (GedcomException ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    /**
     * test_getIndi().getMarriedFamily().getMarriageDate
     */
    @Test
    public void test_getIndiMarriedMarriageDate() {
        try {

            {
                RecordDeath deathRecord;
                MergeRecord mergeRecord;

                deathRecord = MergeModelDeathTest.createDeathRecord("sansfamille1");
                String fileName = "";
                deathRecord.setFieldValue(Record.FieldType.eventDate, "");
                deathRecord.setFieldValue(Record.FieldType.indiBirthDate, "");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelDeathTest.getRecordsInfoPlace(), fileName, deathRecord));
                assertEquals("RecordDeath Date mariage ex conjoint", "",  mergeRecord.getIndi().getMarriedFamily().getMarriageDate().getValue());

                deathRecord = MergeModelDeathTest.createDeathRecord("sansfamille1");
                deathRecord.setFieldValue(Record.FieldType.eventDate, "20 JAN 2000");
                deathRecord.setFieldValue(Record.FieldType.indiBirthDate, "");
                deathRecord.setFieldValue(Record.FieldType.indiAge, "");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelDeathTest.getRecordsInfoPlace(), fileName, deathRecord));
                assertEquals("RecordDeath Date mariage avec ex conjoint", "BEF 2000",  mergeRecord.getIndi().getMarriedFamily().getMarriageDate().getValue());
                assertEquals("RecordDeath Date naissance indi ", "BEF 1982",  mergeRecord.getIndi().getBirthDate().getValue());
            }

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    /**
     * test_getIndi().getFather().getBirthDate
     */
    @Test
    public void test_getFatherBirthDate() {
        try {
            PropertyDate birthDate;

            {
                RecordBirth birthRecord;
                MergeRecord mergeRecord;
                String fileName = "";

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                birthRecord.setFieldValue(Record.FieldType.eventDate, "");
                birthRecord.setFieldValue(Record.FieldType.indiBirthDate,"");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, birthRecord));
                birthDate = mergeRecord.getIndi().getFather().getBirthDate();
                assertEquals("RecordBirth Date naissance pere = null", "", birthDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                birthRecord.setFieldValue(Record.FieldType.eventDate, "20 JAN 2000");
                birthRecord.setFieldValue(Record.FieldType.indiBirthDate,"");
                birthRecord.setFieldValue(Record.FieldType.indiFatherAge,"70y");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, birthRecord));
                birthDate = mergeRecord.getIndi().getFather().getBirthDate();
                assertEquals("RecordBirth Date naissance pere = eventDate - parentAge", "CAL 1930", birthDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                birthRecord.setFieldValue(Record.FieldType.eventDate, "20 JAN 2000");
                birthRecord.setFieldValue(Record.FieldType.indiBirthDate,"31 JAN 2000");
                birthRecord.setFieldValue(Record.FieldType.indiFatherAge,"");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, birthRecord));
                birthDate = mergeRecord.getIndi().getFather().getBirthDate();
                assertEquals("RecordBirth Date naissance pere = eventDate - minParentYearsOld", "BEF 1982", birthDate.getValue());

            }

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    /**
     * test_getIndi().getFather().getDeathDate
     */
    @Test
    public void test_getFatherDeathDate() {
        try {
            PropertyDate deathDate;

            {
                RecordBirth birthRecord;
                MergeRecord mergeRecord;
                String fileName = "";

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                birthRecord.setFieldValue(Record.FieldType.eventDate, "");
                birthRecord.setFieldValue(Record.FieldType.indiBirthDate,"");
                birthRecord.setFieldValue(Record.FieldType.indiFatherDead, "");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, birthRecord));
                deathDate = mergeRecord.getIndi().getFather().getDeathDate();
                assertEquals("RecordBirth Date deces pere = null", "", deathDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, birthRecord));
                birthRecord.setFieldValue(Record.FieldType.eventDate, "20 JAN 2000");
                birthRecord.setFieldValue(Record.FieldType.indiBirthDate,"");
                birthRecord.setFieldValue(Record.FieldType.indiFatherDead, "");
                deathDate = mergeRecord.getIndi().getFather().getDeathDate();
                assertEquals("RecordBirth Date deces pere = BEF eventDate", "AFT 1999", deathDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                birthRecord.setFieldValue(Record.FieldType.eventDate, "21 JAN 2000");
                birthRecord.setFieldValue(Record.FieldType.indiBirthDate,"20 JAN 2000");
                birthRecord.setFieldValue(Record.FieldType.indiFatherDead, FieldDead.DeadState.DEAD.toString());
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, birthRecord));
                deathDate = mergeRecord.getIndi().getFather().getDeathDate();
                assertEquals("RecordBirth Date deces pere = BEF eventDate", "BET 1999 AND 2000", deathDate.getValue());

            }

            {
                RecordMarriage marriageRecord;
                MergeRecord mergeRecord;
                String fileName = "";

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                marriageRecord.setFieldValue(Record.FieldType.eventDate, "");
                marriageRecord.setFieldValue(Record.FieldType.indiBirthDate,"");
                marriageRecord.setFieldValue(Record.FieldType.indiFatherDead, "");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, marriageRecord));
                deathDate = mergeRecord.getIndi().getFather().getDeathDate();
                assertEquals("RecordMarriage Date deces pere = null", "", deathDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                marriageRecord.setFieldValue(Record.FieldType.eventDate, "1 JAN 2021");
                marriageRecord.setFieldValue(Record.FieldType.indiBirthDate,"");
                marriageRecord.setFieldValue(Record.FieldType.indiFatherDead, FieldDead.DeadState.DEAD.toString());
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, marriageRecord));
                deathDate = mergeRecord.getIndi().getFather().getDeathDate();
                assertEquals("RecordMarriage Date deces pere = BEF eventDate", "BEF 2021", deathDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                marriageRecord.setFieldValue(Record.FieldType.eventDate, "1 JAN 2021");
                marriageRecord.setFieldValue(Record.FieldType.indiBirthDate,"31 JAN 2000");
                marriageRecord.setFieldValue(Record.FieldType.indiFatherDead, "");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, marriageRecord));
                deathDate = mergeRecord.getIndi().getFather().getDeathDate();
                assertEquals("RecordMarriage Date deces pere = AFT IndiBirthDate - 9 mois", "AFT 1999", deathDate.getValue());

            }



        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    /**
     * test calculateBirthDate
     */
    @Test
    public void test_getBirthDate() {
        try {
            PropertyDate birthDate;

            {
                RecordBirth birthRecord;
                MergeRecord mergeRecord;
                String fileName = "";

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                birthRecord.setFieldValue(Record.FieldType.eventDate, "1 JAN 2000");
                birthRecord.setFieldValue(Record.FieldType.indiBirthDate,"2 FEB 1970");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, birthRecord));
                birthDate = mergeRecord.getIndi().getBirthDate();
                assertEquals("RecordBirth Date naissance complete != date event", true, birthRecord.getField(Record.FieldType.indiBirthDate).equalsProperty(birthDate));

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                birthRecord.setFieldValue(Record.FieldType.eventDate, "1 JAN 2000");
                birthRecord.setFieldValue(Record.FieldType.indiBirthDate,"");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, birthRecord));
                birthDate = mergeRecord.getIndi().getBirthDate();
                assertEquals("RecordBirth Date naissance = date event", true, birthRecord.getField(FieldType.eventDate).equalsProperty(birthDate));

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                birthRecord.setFieldValue(Record.FieldType.eventDate, "1 JAN 2000");
                birthRecord.setFieldValue(Record.FieldType.indiBirthDate,"2 FEB 1970");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, birthRecord));
                birthDate = mergeRecord.getIndi().getBirthDate();
                assertEquals("RecordBirth Date naissance ", true, birthRecord.getField(Record.FieldType.indiBirthDate).equalsProperty(birthDate));
            }

            {
                RecordMarriage marriageRecord;
                MergeRecord mergeRecord;
                String fileName = "";

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                marriageRecord.setFieldValue(Record.FieldType.eventDate, "1 JAN 2000");
                marriageRecord.setFieldValue(Record.FieldType.indiBirthDate,"2 FEB 1970");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, marriageRecord));
                birthDate = mergeRecord.getIndi().getBirthDate();
                assertEquals("RecordMarriage Date naissance ", true, marriageRecord.getField(Record.FieldType.indiBirthDate).equalsProperty(birthDate));

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                marriageRecord.setFieldValue(Record.FieldType.eventDate, "1 JAN 2000");
                marriageRecord.setFieldValue(Record.FieldType.indiBirthDate,"");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, marriageRecord));
                birthDate = mergeRecord.getIndi().getBirthDate();
                assertEquals("RecordMarriage naissance=mariage - minMarriageYearOld ", "BEF 1982", birthDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                marriageRecord.setFieldValue(Record.FieldType.eventDate, "1999");
                marriageRecord.setFieldValue(Record.FieldType.indiBirthDate,"");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, marriageRecord));
                birthDate = mergeRecord.getIndi().getBirthDate();
                assertEquals("RecordMarriage sDate naissance ", "BEF 1981", birthDate.getValue());
            }

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    /**
     * test_getIndi().getParentMariageDate
     */
    @Test
    public void test_getParentMariageDate() {
        try {
            {
                RecordBirth record;
                MergeRecord mergeRecord;
                PropertyDate parentMarriageDate;
                String fileName = "";

                record = MergeModelBirthTest.createBirthRecord("sansfamille1");
                record.setFieldValue(FieldType.eventDate, "1 JAN 2000");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, record));
                parentMarriageDate = mergeRecord.getIndi().getParentFamily().getMarriageDate();
                assertEquals("Marriage Date exacte", "BEF 2000", parentMarriageDate.getValue());

                record = MergeModelBirthTest.createBirthRecord("sansfamille1");
                record.setFieldValue(FieldType.eventDate, "1999");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, record));
                parentMarriageDate = mergeRecord.getIndi().getParentFamily().getMarriageDate();
                assertEquals("Marriage Date BEF", "BEF 2000", parentMarriageDate.getValue());

                record = MergeModelBirthTest.createBirthRecord("sansfamille1");
                record.setFieldValue(FieldType.eventDate, "11/2002");
                record.setFieldValue(FieldType.indiBirthDate, "");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, record));
                parentMarriageDate = mergeRecord.getIndi().getParentFamily().getMarriageDate();
                assertEquals("Marriage Date BEF", "BEF 2002", parentMarriageDate.getValue());

                record = MergeModelBirthTest.createBirthRecord("sansfamille1");
                record.setFieldValue(FieldType.eventDate, "1998");
                record.setFieldValue(FieldType.indiBirthDate, "");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, record));
                parentMarriageDate = mergeRecord.getIndi().getParentFamily().getMarriageDate();
                assertEquals("Marriage Date BET", "BEF 1998", parentMarriageDate.getValue());
            }

            {
                RecordMarriage record;
                MergeRecord mergeRecord;
                PropertyDate parentMarriageDate;
                String fileName = "";

                record = MergeModelMarriageTest.createMarriageRecord("M1");
                record.setFieldValue(Record.FieldType.eventDate, "1 JAN 2000");
                record.setFieldValue(Record.FieldType.indiBirthDate,"30 JUN 1993");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, record));
                parentMarriageDate = mergeRecord.getIndi().getParentFamily().getMarriageDate();
                assertEquals("RecordMarriage Date exacte", "BEF 1982", parentMarriageDate.getValue());

                record = MergeModelMarriageTest.createMarriageRecord("M1");
                record.setFieldValue(Record.FieldType.eventDate, "1 JAN 2000");
                record.setFieldValue(Record.FieldType.indiBirthDate,"30 JUN 1980");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, record));
                parentMarriageDate = mergeRecord.getIndi().getParentFamily().getMarriageDate();
                assertEquals("RecordMarriage Date exacte", "BEF 1980", parentMarriageDate.getValue());

                record = MergeModelMarriageTest.createMarriageRecord("M1");
                record.setFieldValue(Record.FieldType.eventDate, "2000");
                record.setFieldValue(Record.FieldType.indiBirthDate,"1983");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, record));
                parentMarriageDate = mergeRecord.getIndi().getParentFamily().getMarriageDate();
                assertEquals("RecordMarriage Date exacte", "BEF 1982", parentMarriageDate.getValue());

                record = MergeModelMarriageTest.createMarriageRecord("M1");
                record.setFieldValue(Record.FieldType.eventDate, "2000");
                record.setFieldValue(Record.FieldType.indiBirthDate,"");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, record));
                parentMarriageDate = mergeRecord.getIndi().getParentFamily().getMarriageDate();
                assertEquals("RecordMarriage Date exacte", "BEF 1982", parentMarriageDate.getValue());

                record = MergeModelMarriageTest.createMarriageRecord("M1");
                record.setFieldValue(Record.FieldType.eventDate, "1970");
                record.setFieldValue(Record.FieldType.indiBirthDate,"");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, record));
                parentMarriageDate = mergeRecord.getIndi().getParentFamily().getMarriageDate();
                assertEquals("RecordMarriageDate exacte", "BEF 1952", parentMarriageDate.getValue());

                record = MergeModelMarriageTest.createMarriageRecord("M1");
                record.setFieldValue(Record.FieldType.eventDate, "BEF 1980");
                record.setFieldValue(Record.FieldType.indiBirthDate,"ABT 1970");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, record));
                parentMarriageDate = mergeRecord.getIndi().getParentFamily().getMarriageDate();
                assertEquals("RecordMarriageDate exacte", "BEF 1962", parentMarriageDate.getValue());
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

}
