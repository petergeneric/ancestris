package ancestris.modules.releve.dnd;

import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
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

        assertEquals("jours",  "DEC 1998",  MergeRecord.getYear(pit, delta).getValue());
    }
    
    /**
     * test_getIndiMarriedMarriageDate
     */
    public void test_getIndiMarriedMarriageDate() {
        try {
            
            {
                RecordDeath deathRecord;
                MergeRecord mergeRecord;

                deathRecord = MergeModelDeathTest.createDeathRecord("sansfamille1");
                mergeRecord = new MergeRecord(deathRecord);
                deathRecord.getEventDateProperty().setValue("");
                deathRecord.getIndiBirthDate().getPropertyDate().setValue("");
                assertEquals("RecordDeath Date mariage ex conjoint", "",  mergeRecord.getIndiMarriedMarriageDate().getValue());

                deathRecord = MergeModelDeathTest.createDeathRecord("sansfamille1");
                mergeRecord = new MergeRecord(deathRecord);
                deathRecord.getEventDateProperty().setValue("20 JAN 2000");
                deathRecord.getIndiBirthDate().getPropertyDate().setValue("");
                deathRecord.getIndiAge().setValue("");
                assertEquals("RecordDeath Date mariage avec ex conjoint", "BEF 2000",  mergeRecord.getIndiMarriedMarriageDate().getValue());
                assertEquals("RecordDeath Date naissance indi ", "BEF 1985",  mergeRecord.getIndiBirthDate().getValue());


            }

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    /**
     * test_getIndiFatherBirthDate
     */
    public void test_getIndiFatherBirthDate() {
        try {
            PropertyDate birthDate;

            {
                RecordBirth birthRecord;
                MergeRecord mergeRecord;

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(birthRecord);
                birthRecord.getEventDateProperty().setValue("");
                birthRecord.getIndiBirthDate().getPropertyDate().setValue("");
                birthDate = mergeRecord.getIndiFatherBirthDate();
                assertEquals("RecordBirth Date naissance pere = null", "", birthDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(birthRecord);
                birthRecord.getEventDateProperty().setValue("20 JAN 2000");
                birthRecord.getIndiBirthDate().getPropertyDate().setValue("");
                birthRecord.getIndiFatherAge().getDelta().setValue("70y");
                birthDate = mergeRecord.getIndiFatherBirthDate();
                assertEquals("RecordBirth Date naissance pere = eventDate - parentAge", "CAL 1930", birthDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(birthRecord);
                birthRecord.getEventDateProperty().setValue("20 JAN 2000");
                birthRecord.getIndiBirthDate().getPropertyDate().setValue("31 JAN 2000");
                birthRecord.getIndiFatherAge().getDelta().setValue("");
                birthDate = mergeRecord.getIndiFatherBirthDate();
                assertEquals("RecordBirth Date naissance pere = eventDate - minParentYearsOld", "BEF 1985", birthDate.getValue());

            }

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    /**
     * test_getIndiFatherDeathDate
     */
    public void test_getIndiFatherDeathDate() {
        try {
            PropertyDate deathDate;

            {
                RecordBirth birthRecord;
                MergeRecord mergeRecord;

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(birthRecord);
                birthRecord.getEventDateProperty().setValue("");
                birthRecord.getIndiBirthDate().getPropertyDate().setValue("");
                birthRecord.getIndiFatherDead().setState(false);
                deathDate = mergeRecord.getIndiFatherDeathDate();
                assertEquals("RecordBirth Date deces pere = null", "", deathDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(birthRecord);
                birthRecord.getEventDateProperty().setValue("20 JAN 2000");
                birthRecord.getIndiBirthDate().getPropertyDate().setValue("");
                birthRecord.getIndiFatherDead().setState(false);
                deathDate = mergeRecord.getIndiFatherDeathDate();
                assertEquals("RecordBirth Date deces pere = BEF eventDate", "AFT 1999", deathDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(birthRecord);
                birthRecord.getEventDateProperty().setValue("20 JAN 2000");
                birthRecord.getIndiBirthDate().getPropertyDate().setValue("31 JAN 2000");
                birthRecord.getIndiFatherDead().setState(true);
                deathDate = mergeRecord.getIndiFatherDeathDate();
                assertEquals("RecordBirth Date deces pere = BEF eventDate", "BET 1999 AND 2000", deathDate.getValue());

            }

            {
                RecordMarriage marriageRecord;
                MergeRecord mergeRecord;

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(marriageRecord);
                marriageRecord.getEventDateProperty().setValue("");
                marriageRecord.getIndiBirthDate().getPropertyDate().setValue("");
                marriageRecord.getIndiFatherDead().setState(false);
                deathDate = mergeRecord.getIndiFatherDeathDate();
                assertEquals("RecordMarriage Date deces pere = null", "", deathDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(marriageRecord);
                marriageRecord.getEventDateProperty().setValue("1 JAN 2021");
                marriageRecord.getIndiBirthDate().getPropertyDate().setValue("");
                marriageRecord.getIndiFatherDead().setState(true);
                deathDate = mergeRecord.getIndiFatherDeathDate();
                assertEquals("RecordMarriage Date deces pere = BEF eventDate", "BEF 2021", deathDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(marriageRecord);
                marriageRecord.getEventDateProperty().setValue("1 JAN 2021");
                marriageRecord.getIndiBirthDate().getPropertyDate().setValue("31 JAN 2000");
                marriageRecord.getIndiFatherDead().setState(false);
                deathDate = mergeRecord.getIndiFatherDeathDate();
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
    public void test_getIndiBirthDate() {
        try {
            PropertyDate birthDate;

            {
                RecordBirth birthRecord;
                MergeRecord mergeRecord;

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(birthRecord);                
                birthRecord.getEventDateProperty().setValue("1 JAN 2000");
                birthRecord.getIndiBirthDate().getPropertyDate().setValue("2 FEB 1970");
                birthDate = mergeRecord.getIndiBirthDate();
                assertEquals("RecordBirth Date naissance complete != date event", birthRecord.getIndiBirthDate().getPropertyDate().getValue(), birthDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(birthRecord);
                birthRecord.getEventDateProperty().setValue("1 JAN 2000");
                birthRecord.getIndiBirthDate().getPropertyDate().setValue("");
                birthDate = mergeRecord.getIndiBirthDate();
                assertEquals("RecordBirth Date naissance = date event", birthRecord.getEventDateProperty().getValue(), birthDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(birthRecord);
                birthRecord.getEventDateProperty().setValue("1 JAN 2000");
                birthRecord.getIndiBirthDate().getPropertyDate().setValue("2 FEB 1970");
                birthDate = mergeRecord.getIndiBirthDate();
                assertEquals("RecordBirth Date naissance ", birthRecord.getIndiBirthDate().getPropertyDate().getValue(), birthDate.getValue());
            }

            {
                RecordMarriage marriageRecord;
                MergeRecord mergeRecord;

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(marriageRecord);
                marriageRecord.getEventDateProperty().setValue("1 JAN 2000");
                marriageRecord.getIndiBirthDate().getPropertyDate().setValue("2 FEB 1970");
                birthDate = mergeRecord.getIndiBirthDate();
                assertEquals("RecordMarriage Date naissance ", marriageRecord.getIndiBirthDate().getPropertyDate().getValue(), birthDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(marriageRecord);
                marriageRecord.getEventDateProperty().setValue("1 JAN 2000");
                marriageRecord.getIndiBirthDate().getPropertyDate().setValue("");
                birthDate = mergeRecord.getIndiBirthDate();
                assertEquals("RecordMarriage naissance=mariage - minMarriageYearOld ", "BEF 1985", birthDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(marriageRecord);
                marriageRecord.getEventDateProperty().setValue("BEF 1999");
                marriageRecord.getIndiBirthDate().getPropertyDate().setValue("");
                birthDate = mergeRecord.getIndiBirthDate();
                assertEquals("RecordMarriage sDate naissance ", "BEF 1984", birthDate.getValue());
            }

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    /**
     * test_getIndiParentMariageDate
     */
    public void test_getIndiParentMariageDate() {
        try {
            {
                RecordBirth birthRecord;
                MergeRecord mergeRecord;
                PropertyDate parentMarriageDate;

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(birthRecord);
                birthRecord.getEventDateProperty().setValue("1 JAN 2000");
                parentMarriageDate = mergeRecord.getIndiParentMarriageDate();
                assertEquals("RecordBirth Date exacte", "BEF 2000", parentMarriageDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(birthRecord);
                birthRecord.getEventDateProperty().setValue("BEF 1999");
                parentMarriageDate = mergeRecord.getIndiParentMarriageDate();
                assertEquals("RecordBirth Date BEF", "BEF 1999", parentMarriageDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(birthRecord);
                birthRecord.getEventDateProperty().setValue("AFT 2000");
                parentMarriageDate = mergeRecord.getIndiParentMarriageDate();
                assertEquals("RecordBirth Date AFT", "", parentMarriageDate.getValue());

                birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                mergeRecord = new MergeRecord(birthRecord);
                birthRecord.getEventDateProperty().setValue("BET 1998 AND 2000");
                parentMarriageDate = mergeRecord.getIndiParentMarriageDate();
                assertEquals("RecordBirth Date BET", "BEF 2000", parentMarriageDate.getValue());
            }

            {
                RecordMarriage marriageRecord;
                MergeRecord mergeRecord;
                PropertyDate parentMarriageDate;

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(marriageRecord);
                marriageRecord.getEventDateProperty().setValue("1 JAN 2000");
                marriageRecord.getIndiBirthDate().getPropertyDate().setValue("30 JUN 1993");
                parentMarriageDate = mergeRecord.getIndiParentMarriageDate();
                assertEquals("RecordMarriage Date exacte", "BEF 1985", parentMarriageDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(marriageRecord);                                
                marriageRecord.getEventDateProperty().setValue("1 JAN 2000");
                marriageRecord.getIndiBirthDate().getPropertyDate().setValue("30 JUN 1980");
                parentMarriageDate = mergeRecord.getIndiParentMarriageDate();
                assertEquals("RecordMarriage Date exacte", "BEF 1980", parentMarriageDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(marriageRecord);                                
                marriageRecord.getEventDateProperty().setValue("BET 1985 AND 2000");
                marriageRecord.getIndiBirthDate().getPropertyDate().setValue("BET 1983 AND 1987");
                parentMarriageDate = mergeRecord.getIndiParentMarriageDate();
                assertEquals("RecordMarriage Date exacte", "BEF 1985", parentMarriageDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(marriageRecord);                                
                marriageRecord.getEventDateProperty().setValue("ABT 2000");
                marriageRecord.getIndiBirthDate().getPropertyDate().setValue("AFT 1980");
                parentMarriageDate = mergeRecord.getIndiParentMarriageDate();
                assertEquals("RecordMarriage Date exacte", "BEF 1985", parentMarriageDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(marriageRecord);                                
                marriageRecord.getEventDateProperty().setValue("ABT 1970");
                marriageRecord.getIndiBirthDate().getPropertyDate().setValue("BEF 1980");
                parentMarriageDate = mergeRecord.getIndiParentMarriageDate();
                assertEquals("RecordMarriageDate exacte", "BEF 1955", parentMarriageDate.getValue());

                marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                mergeRecord = new MergeRecord(marriageRecord);                                
                marriageRecord.getEventDateProperty().setValue("BEF 1980");
                marriageRecord.getIndiBirthDate().getPropertyDate().setValue("ABT 1970");
                parentMarriageDate = mergeRecord.getIndiParentMarriageDate();
                assertEquals("RecordMarriageDate exacte", "BEF 1965", parentMarriageDate.getValue());
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
    
}
