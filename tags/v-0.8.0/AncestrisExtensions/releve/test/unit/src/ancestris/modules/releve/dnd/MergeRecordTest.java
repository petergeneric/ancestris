package ancestris.modules.releve.dnd;

import ancestris.modules.releve.model.FieldDead;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import genj.gedcom.GedcomException;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class MergeRecordTest extends TestCase {

    /**
     * test match age
     */
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
    public void test_getIndiMarriedMarriageDate() {
        try {
            
            {
                RecordDeath deathRecord;
                MergeRecord mergeRecord;

                deathRecord = MergeModelDeathTest.createDeathRecord("sansfamille1");
                String sourceTitle = "";
                mergeRecord = new MergeRecord(MergeModelDeathTest.getRecordsInfoPlace(), sourceTitle, deathRecord);
                deathRecord.getEventDateProperty().setValue("");
                deathRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                assertEquals("RecordDeath Date mariage ex conjoint", "",  mergeRecord.getIndi().getMarriedMarriageDate().getValue());

                deathRecord = MergeModelDeathTest.createDeathRecord("sansfamille1");
                mergeRecord = new MergeRecord(MergeModelDeathTest.getRecordsInfoPlace(), sourceTitle, deathRecord);
                deathRecord.getEventDateProperty().setValue("20 JAN 2000");
                deathRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                deathRecord.getIndi().getAge().setValue("");
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
    public void test_getFatherBirthDate() {
        try {
            PropertyDate birthDate;

            {
                RecordBirth birthRecord;
                MergeRecord mergeRecord;
                String sourceTitle = "";

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(MergeModelBirthTest.getRecordsInfoPlace(), sourceTitle, birthRecord);
                birthRecord.getEventDateProperty().setValue("");
                birthRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                birthDate = mergeRecord.getIndi().getFatherBirthDate();
                assertEquals("RecordBirth Date naissance pere = null", "", birthDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(MergeModelBirthTest.getRecordsInfoPlace(), sourceTitle, birthRecord);
                birthRecord.getEventDateProperty().setValue("20 JAN 2000");
                birthRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                birthRecord.getIndi().getFatherAge().getDelta().setValue("70y");
                birthDate = mergeRecord.getIndi().getFatherBirthDate();
                assertEquals("RecordBirth Date naissance pere = eventDate - parentAge", "CAL 1930", birthDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(MergeModelBirthTest.getRecordsInfoPlace(), sourceTitle, birthRecord);
                birthRecord.getEventDateProperty().setValue("20 JAN 2000");
                birthRecord.getIndi().getBirthDate().getPropertyDate().setValue("31 JAN 2000");
                birthRecord.getIndi().getFatherAge().getDelta().setValue("");
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
    public void test_getFatherDeathDate() {
        try {
            PropertyDate deathDate;

            {
                RecordBirth birthRecord;
                MergeRecord mergeRecord;
                String sourceTitle = "";

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(MergeModelBirthTest.getRecordsInfoPlace(), sourceTitle, birthRecord);
                birthRecord.getEventDateProperty().setValue("");
                birthRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                birthRecord.getIndi().getFatherDead().setState(FieldDead.DeadState.unknown);
                deathDate = mergeRecord.getIndi().getFatherDeathDate();
                assertEquals("RecordBirth Date deces pere = null", "", deathDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(MergeModelBirthTest.getRecordsInfoPlace(), sourceTitle, birthRecord);
                birthRecord.getEventDateProperty().setValue("20 JAN 2000");
                birthRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                birthRecord.getIndi().getFatherDead().setState(FieldDead.DeadState.unknown);
                deathDate = mergeRecord.getIndi().getFatherDeathDate();
                assertEquals("RecordBirth Date deces pere = BEF eventDate", "AFT 1999", deathDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(MergeModelBirthTest.getRecordsInfoPlace(), sourceTitle, birthRecord);
                birthRecord.getEventDateProperty().setValue("20 JAN 2000");
                birthRecord.getIndi().getBirthDate().getPropertyDate().setValue("31 JAN 2000");
                birthRecord.getIndi().getFatherDead().setState(FieldDead.DeadState.dead);
                deathDate = mergeRecord.getIndi().getFatherDeathDate();
                assertEquals("RecordBirth Date deces pere = BEF eventDate", "BET 1999 AND 2000", deathDate.getValue());

            }

            {
                RecordMarriage marriageRecord;
                MergeRecord mergeRecord;
                String sourceTitle = "";

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(MergeModelMarriageTest.getRecordsInfoPlace(), sourceTitle, marriageRecord);
                marriageRecord.getEventDateProperty().setValue("");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                marriageRecord.getIndi().getFatherDead().setState(FieldDead.DeadState.unknown);
                deathDate = mergeRecord.getIndi().getFatherDeathDate();
                assertEquals("RecordMarriage Date deces pere = null", "", deathDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(MergeModelMarriageTest.getRecordsInfoPlace(), sourceTitle,marriageRecord);
                marriageRecord.getEventDateProperty().setValue("1 JAN 2021");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                marriageRecord.getIndi().getFatherDead().setState(FieldDead.DeadState.dead);
                deathDate = mergeRecord.getIndi().getFatherDeathDate();
                assertEquals("RecordMarriage Date deces pere = BEF eventDate", "BEF 2021", deathDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(MergeModelMarriageTest.getRecordsInfoPlace(), sourceTitle, marriageRecord);
                marriageRecord.getEventDateProperty().setValue("1 JAN 2021");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("31 JAN 2000");
                marriageRecord.getIndi().getFatherDead().setState(FieldDead.DeadState.unknown);
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
    public void test_getBirthDate() {
        try {
            PropertyDate birthDate;

            {
                RecordBirth birthRecord;
                MergeRecord mergeRecord;
                String sourceTitle = "";

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(MergeModelBirthTest.getRecordsInfoPlace(), sourceTitle, birthRecord);
                birthRecord.getEventDateProperty().setValue("1 JAN 2000");
                birthRecord.getIndi().getBirthDate().getPropertyDate().setValue("2 FEB 1970");
                birthDate = mergeRecord.getIndi().getBirthDate();
                assertEquals("RecordBirth Date naissance complete != date event", birthRecord.getIndi().getBirthDate().getPropertyDate().getValue(), birthDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(MergeModelBirthTest.getRecordsInfoPlace(), sourceTitle, birthRecord);
                birthRecord.getEventDateProperty().setValue("1 JAN 2000");
                birthRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                birthDate = mergeRecord.getIndi().getBirthDate();
                assertEquals("RecordBirth Date naissance = date event", birthRecord.getEventDateProperty().getValue(), birthDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(MergeModelBirthTest.getRecordsInfoPlace(), sourceTitle, birthRecord);
                birthRecord.getEventDateProperty().setValue("1 JAN 2000");
                birthRecord.getIndi().getBirthDate().getPropertyDate().setValue("2 FEB 1970");
                birthDate = mergeRecord.getIndi().getBirthDate();
                assertEquals("RecordBirth Date naissance ", birthRecord.getIndi().getBirthDate().getPropertyDate().getValue(), birthDate.getValue());
            }

            {
                RecordMarriage marriageRecord;
                MergeRecord mergeRecord;
                String sourceTitle = "";

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(MergeModelMarriageTest.getRecordsInfoPlace(), sourceTitle, marriageRecord);
                marriageRecord.getEventDateProperty().setValue("1 JAN 2000");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("2 FEB 1970");
                birthDate = mergeRecord.getIndi().getBirthDate();
                assertEquals("RecordMarriage Date naissance ", marriageRecord.getIndi().getBirthDate().getPropertyDate().getValue(), birthDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(MergeModelMarriageTest.getRecordsInfoPlace(), sourceTitle, marriageRecord);
                marriageRecord.getEventDateProperty().setValue("1 JAN 2000");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
                birthDate = mergeRecord.getIndi().getBirthDate();
                assertEquals("RecordMarriage naissance=mariage - minMarriageYearOld ", "BEF 1982", birthDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(MergeModelMarriageTest.getRecordsInfoPlace(), sourceTitle, marriageRecord);
                marriageRecord.getEventDateProperty().setValue("BEF 1999");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("");
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
    public void test_getParentMariageDate() {
        try {
            {
                RecordBirth birthRecord;
                MergeRecord mergeRecord;
                PropertyDate parentMarriageDate;
                String sourceTitle = "";

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(MergeModelBirthTest.getRecordsInfoPlace(), sourceTitle, birthRecord);
                birthRecord.getEventDateProperty().setValue("1 JAN 2000");
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordBirth Date exacte", "BEF 2000", parentMarriageDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(MergeModelBirthTest.getRecordsInfoPlace(), sourceTitle, birthRecord);
                birthRecord.getEventDateProperty().setValue("BEF 1999");
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordBirth Date BEF", "BEF 1999", parentMarriageDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(MergeModelBirthTest.getRecordsInfoPlace(), sourceTitle, birthRecord);
                birthRecord.getEventDateProperty().setValue("AFT 2000");
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordBirth Date AFT", "", parentMarriageDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(MergeModelBirthTest.getRecordsInfoPlace(), sourceTitle, birthRecord);
                birthRecord.getEventDateProperty().setValue("BET 1998 AND 2000");
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordBirth Date BET", "BEF 2000", parentMarriageDate.getValue());
            }

            {
                RecordMarriage marriageRecord;
                MergeRecord mergeRecord;
                PropertyDate parentMarriageDate;
                String sourceTitle = "";

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(MergeModelMarriageTest.getRecordsInfoPlace(), sourceTitle, marriageRecord);
                marriageRecord.getEventDateProperty().setValue("1 JAN 2000");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("30 JUN 1993");
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordMarriage Date exacte", "BEF 1982", parentMarriageDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(MergeModelMarriageTest.getRecordsInfoPlace(), sourceTitle, marriageRecord);
                marriageRecord.getEventDateProperty().setValue("1 JAN 2000");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("30 JUN 1980");
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordMarriage Date exacte", "BEF 1980", parentMarriageDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(MergeModelMarriageTest.getRecordsInfoPlace(), sourceTitle, marriageRecord);
                marriageRecord.getEventDateProperty().setValue("BET 1985 AND 2000");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("BET 1983 AND 1987");
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordMarriage Date exacte", "BEF 1982", parentMarriageDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(MergeModelMarriageTest.getRecordsInfoPlace(), sourceTitle, marriageRecord);
                marriageRecord.getEventDateProperty().setValue("ABT 2000");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("AFT 1980");
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordMarriage Date exacte", "BEF 1982", parentMarriageDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(MergeModelMarriageTest.getRecordsInfoPlace(), sourceTitle, marriageRecord);
                marriageRecord.getEventDateProperty().setValue("ABT 1970");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("BEF 1980");
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordMarriageDate exacte", "BEF 1952", parentMarriageDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(MergeModelMarriageTest.getRecordsInfoPlace(), sourceTitle, marriageRecord);
                marriageRecord.getEventDateProperty().setValue("BEF 1980");
                marriageRecord.getIndi().getBirthDate().getPropertyDate().setValue("ABT 1970");
                parentMarriageDate = mergeRecord.getIndi().getParentMarriageDate();
                assertEquals("RecordMarriageDate exacte", "BEF 1962", parentMarriageDate.getValue());
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
    
}
