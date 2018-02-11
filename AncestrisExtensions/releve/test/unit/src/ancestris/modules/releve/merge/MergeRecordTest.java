package ancestris.modules.releve.merge;

import ancestris.modules.releve.RecordTransferHandle;
import ancestris.modules.releve.model.FieldDead;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import genj.gedcom.GedcomException;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author Michel
 */
public class MergeRecordTest extends TestCase {

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
     * test_getIndi().getMarriedMarriageDate
     */
    @Test
    public void test_getIndiMarriedMarriageDate() {
        try {
            
            {
                RecordDeath deathRecord;
                MergeRecord mergeRecord;

                deathRecord = MergeModelDeathTest.createDeathRecord("sansfamille1");
                String fileName = "";
                deathRecord.getEventDateProperty().setValue("");
                deathRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelDeathTest.getRecordsInfoPlace(), fileName, deathRecord));            
                assertEquals("RecordDeath Date mariage ex conjoint", "",  mergeRecord.getIndi().getMarriedMarriageDate().getValue());

                deathRecord = MergeModelDeathTest.createDeathRecord("sansfamille1");
                deathRecord.getEventDateProperty().setValue("20 JAN 2000");
                deathRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                deathRecord.getIndi().getAge().setValue("");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelDeathTest.getRecordsInfoPlace(), fileName, deathRecord)); 
                assertEquals("RecordDeath Date mariage avec ex conjoint", "BEF 2000",  mergeRecord.getIndi().getMarriedMarriageDate().getValue());
                assertEquals("RecordDeath Date naissance indi ", "BEF 1982",  mergeRecord.getIndi().getBirthDate().getValue());
            }

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    /**
     * test_getIndi().getFatherBirthDate
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
                birthRecord.getEventDateProperty().setValue("");
                birthRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, birthRecord)); 
                birthDate = mergeRecord.getIndi().getFatherBirthDate();
                assertEquals("RecordBirth Date naissance pere = null", "", birthDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                birthRecord.getEventDateProperty().setValue("20 JAN 2000");
                birthRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                birthRecord.getIndi().getFatherAge().getDelta().setValue("70y");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, birthRecord));                 
                birthDate = mergeRecord.getIndi().getFatherBirthDate();
                assertEquals("RecordBirth Date naissance pere = eventDate - parentAge", "CAL 1930", birthDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                birthRecord.getEventDateProperty().setValue("20 JAN 2000");
                birthRecord.getIndi().getBirthDate().getPropertyDate().setValue("31 JAN 2000");
                birthRecord.getIndi().getFatherAge().getDelta().setValue("");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, birthRecord));                 
                birthDate = mergeRecord.getIndi().getFatherBirthDate();
                assertEquals("RecordBirth Date naissance pere = eventDate - minParentYearsOld", "BEF 1982", birthDate.getValue());

            }

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    /**
     * test_getIndi().getFatherDeathDate
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
                birthRecord.getEventDateProperty().setValue("");
                birthRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                birthRecord.getIndi().getFatherDead().setState(FieldDead.DeadState.UNKNOWN);
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, birthRecord));                 
                deathDate = mergeRecord.getIndi().getFatherDeathDate();
                assertEquals("RecordBirth Date deces pere = null", "", deathDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, birthRecord)); 
                birthRecord.getEventDateProperty().setValue("20 JAN 2000");
                birthRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                birthRecord.getIndi().getFatherDead().setState(FieldDead.DeadState.UNKNOWN);
                deathDate = mergeRecord.getIndi().getFatherDeathDate();
                assertEquals("RecordBirth Date deces pere = BEF eventDate", "AFT 1999", deathDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                birthRecord.getEventDateProperty().setValue("21 JAN 2000");                
                birthRecord.getIndi().getBirthDate().getPropertyDate().setValue("20 JAN 2000");
                birthRecord.getIndi().getFatherDead().setState(FieldDead.DeadState.DEAD);
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, birthRecord)); 
                deathDate = mergeRecord.getIndi().getFatherDeathDate();
                assertEquals("RecordBirth Date deces pere = BEF eventDate", "BET 1999 AND 2000", deathDate.getValue());

            }

            {
                RecordMarriage marriageRecord;
                MergeRecord mergeRecord;
                String fileName = "";

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                marriageRecord.getEventDateProperty().setValue("");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                marriageRecord.getIndi().getFatherDead().setState(FieldDead.DeadState.UNKNOWN);
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, marriageRecord)); 
                deathDate = mergeRecord.getIndi().getFatherDeathDate();
                assertEquals("RecordMarriage Date deces pere = null", "", deathDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                marriageRecord.getEventDateProperty().setValue("1 JAN 2021");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                marriageRecord.getIndi().getFatherDead().setState(FieldDead.DeadState.DEAD);
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, marriageRecord));
                deathDate = mergeRecord.getIndi().getFatherDeathDate();
                assertEquals("RecordMarriage Date deces pere = BEF eventDate", "BEF 2021", deathDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                marriageRecord.getEventDateProperty().setValue("1 JAN 2021");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("31 JAN 2000");
                marriageRecord.getIndi().getFatherDead().setState(FieldDead.DeadState.UNKNOWN);
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, marriageRecord));
                deathDate = mergeRecord.getIndi().getFatherDeathDate();
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
                birthRecord.getEventDateProperty().setValue("1 JAN 2000");
                birthRecord.getIndi().getBirthDate().getPropertyDate().setValue("2 FEB 1970");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, birthRecord));
                birthDate = mergeRecord.getIndi().getBirthDate();
                assertEquals("RecordBirth Date naissance complete != date event", birthRecord.getIndi().getBirthDate().getPropertyDate().getValue(), birthDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                birthRecord.getEventDateProperty().setValue("1 JAN 2000");
                birthRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, birthRecord));
                birthDate = mergeRecord.getIndi().getBirthDate();
                assertEquals("RecordBirth Date naissance = date event", birthRecord.getEventDateProperty().getValue(), birthDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                birthRecord.getEventDateProperty().setValue("1 JAN 2000");
                birthRecord.getIndi().getBirthDate().getPropertyDate().setValue("2 FEB 1970");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, birthRecord));
                birthDate = mergeRecord.getIndi().getBirthDate();
                assertEquals("RecordBirth Date naissance ", birthRecord.getIndi().getBirthDate().getPropertyDate().getValue(), birthDate.getValue());
            }

            {
                RecordMarriage marriageRecord;
                MergeRecord mergeRecord;
                String fileName = "";

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                marriageRecord.getEventDateProperty().setValue("1 JAN 2000");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("2 FEB 1970");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, marriageRecord));
                birthDate = mergeRecord.getIndi().getBirthDate();
                assertEquals("RecordMarriage Date naissance ", marriageRecord.getIndi().getBirthDate().getPropertyDate().getValue(), birthDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                marriageRecord.getEventDateProperty().setValue("1 JAN 2000");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, marriageRecord));
                birthDate = mergeRecord.getIndi().getBirthDate();
                assertEquals("RecordMarriage naissance=mariage - minMarriageYearOld ", "BEF 1982", birthDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                marriageRecord.getEventDateProperty().setValue("BEF 1999");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
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
                RecordBirth marriageRecord;
                MergeRecord mergeRecord;
                PropertyDate parentMarriageDate;
                String fileName = "";

                marriageRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                marriageRecord.getEventDateProperty().setValue("1 JAN 2000");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, marriageRecord));
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordBirth Date exacte", "BEF 2000", parentMarriageDate.getValue());

                marriageRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                marriageRecord.getEventDateProperty().setValue("BEF 1999");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, marriageRecord));
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordBirth Date BEF", "BEF 1999", parentMarriageDate.getValue());

                marriageRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                marriageRecord.getEventDateProperty().setValue("AFT 2000");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, marriageRecord));
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordBirth Date AFT", "", parentMarriageDate.getValue());

                marriageRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                marriageRecord.getEventDateProperty().setValue("BET 1998 AND 2000");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, marriageRecord));
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordBirth Date BET", "BEF 2000", parentMarriageDate.getValue());
            }

            {
                RecordMarriage marriageRecord;
                MergeRecord mergeRecord;
                PropertyDate parentMarriageDate;
                String fileName = "";

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                marriageRecord.getEventDateProperty().setValue("1 JAN 2000");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("30 JUN 1993");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, marriageRecord));
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordMarriage Date exacte", "BEF 1982", parentMarriageDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                marriageRecord.getEventDateProperty().setValue("1 JAN 2000");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("30 JUN 1980");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, marriageRecord));
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordMarriage Date exacte", "BEF 1980", parentMarriageDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                marriageRecord.getEventDateProperty().setValue("BET 1985 AND 2000");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("BET 1983 AND 1987");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, marriageRecord));
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordMarriage Date exacte", "BEF 1982", parentMarriageDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                marriageRecord.getEventDateProperty().setValue("ABT 2000");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("AFT 1980");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, marriageRecord));
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordMarriage Date exacte", "BEF 1982", parentMarriageDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                marriageRecord.getEventDateProperty().setValue("ABT 1970");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("BEF 1980");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, marriageRecord));
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordMarriageDate exacte", "BEF 1952", parentMarriageDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                marriageRecord.getEventDateProperty().setValue("BEF 1980");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("ABT 1970");
                mergeRecord = new MergeRecord(RecordTransferHandle.createTransferableData(null, MergeModelMarriageTest.getRecordsInfoPlace(), fileName, marriageRecord));
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordMarriageDate exacte", "BEF 1962", parentMarriageDate.getValue());
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
    
}
