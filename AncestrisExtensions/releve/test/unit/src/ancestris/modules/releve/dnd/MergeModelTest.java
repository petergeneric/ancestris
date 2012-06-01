package ancestris.modules.releve.dnd;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.dnd.MergeModel.RowType;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordMarriage;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class MergeModelTest extends TestCase {

    /**
     * test getBirthDate
     */
    public void test_getIndiBirthDate() {
        try {
            PropertyDate birthDate;

            {
                RecordBirth birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                MergeRecord mergeRecord = new MergeRecord(birthRecord);
                
                birthRecord.getEventDateProperty().setValue("1 JAN 2000");
                birthRecord.getIndiBirthDate().getPropertyDate().setValue("2 FEB 1970");
                birthDate = mergeRecord.getBirthDate(RowType.IndiBirthDate);
                assertEquals("RecordBirth Date naissance complete != date event", birthRecord.getIndiBirthDate().getPropertyDate().getValue(), birthDate.getValue());

                birthRecord.getEventDateProperty().setValue("1 JAN 2000");
                birthRecord.getIndiBirthDate().getPropertyDate().setValue("");
                birthDate = mergeRecord.getBirthDate( RowType.IndiBirthDate);
                assertEquals("RecordBirth Date naissance = date event", birthRecord.getEventDateProperty().getValue(), birthDate.getValue());

                birthRecord.getEventDateProperty().setValue("1 JAN 2000");
                birthRecord.getIndiBirthDate().getPropertyDate().setValue("2 FEB 1970");
                birthDate = mergeRecord.getBirthDate(RowType.IndiBirthDate);
                assertEquals("RecordBirth Date naissance ", birthRecord.getIndiBirthDate().getPropertyDate().getValue(), birthDate.getValue());
            }

            {
                RecordMarriage marriageRecord = MergeModelMarriageTest.createMarriageRecord("M1");
                MergeRecord mergeRecord = new MergeRecord(marriageRecord);
                
                marriageRecord.getEventDateProperty().setValue("1 JAN 2000");
                marriageRecord.getIndiBirthDate().getPropertyDate().setValue("2 FEB 1970");
                birthDate = mergeRecord.getBirthDate(RowType.IndiBirthDate);
                assertEquals("RecordMarriage Date naissance ", marriageRecord.getIndiBirthDate().getPropertyDate().getValue(), birthDate.getValue());

                marriageRecord.getEventDateProperty().setValue("1 JAN 2000");
                marriageRecord.getIndiBirthDate().getPropertyDate().setValue("");
                birthDate = mergeRecord.getBirthDate(RowType.IndiBirthDate);
                assertEquals("RecordMarriage naissance=mariage - minMarriageYearOld ", "BEF 1985", birthDate.getValue());

                marriageRecord.getEventDateProperty().setValue("BEF 1999");
                marriageRecord.getIndiBirthDate().getPropertyDate().setValue("");
                birthDate = mergeRecord.getBirthDate(RowType.IndiBirthDate);
                assertEquals("RecordMarriage sDate naissance ", "BEF 1984", birthDate.getValue());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    /**
     * test copyMarriageDate
     */
    public void testAddMarriageDate() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Fam fam = (Fam)gedcom.getEntity("F1");
            RecordBirth record = MergeModelBirthTest.createBirthRecord("sansfamille1");
            MergeRecord mergeRecord = new MergeRecord(record);
            PropertyDate marriageDate = new PropertyDate();
            marriageDate.setValue("1 NOV 1970");

            MergeModel.copyMarriageDate(fam, marriageDate, mergeRecord);
            assertEquals("Date marriage", marriageDate.getValue(), fam.getMarriageDate().getValue());

            marriageDate.setValue("22 NOV 1972");
            MergeModel.copyMarriageDate(fam, marriageDate, mergeRecord);
            assertEquals("Date marriage", marriageDate.getValue(), fam.getMarriageDate().getValue());


        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    /**
     * test getParentMariageDate
     */
    public void testGetIndiParentMariageDate() {
        try {
            {
                RecordBirth birthRecord = MergeModelBirthTest.createBirthRecord("sansfamille1");
                PropertyDate parentMarriageDate;
                MergeRecord mergeRecord = new MergeRecord(birthRecord);

                birthRecord.getEventDateProperty().setValue("1 JAN 2000");
                parentMarriageDate = mergeRecord.getParentMariageDate( RowType.IndiParentMarriageDate);
                assertEquals("RecordBirth Date exacte", "BEF 2000", parentMarriageDate.getValue());

                birthRecord.getEventDateProperty().setValue("BEF 1999");
                parentMarriageDate = mergeRecord.getParentMariageDate( RowType.IndiParentMarriageDate);
                assertEquals("RecordBirth Date BEF", "BEF 1999", parentMarriageDate.getValue());

                birthRecord.getEventDateProperty().setValue("AFT 2000");
                parentMarriageDate = mergeRecord.getParentMariageDate( RowType.IndiParentMarriageDate);
                assertEquals("RecordBirth Date AFT", "", parentMarriageDate.getValue());

                birthRecord.getEventDateProperty().setValue("BET 1998 AND 2000");
                parentMarriageDate = mergeRecord.getParentMariageDate( RowType.IndiParentMarriageDate);
                assertEquals("RecordBirth Date BET", "BEF 2000", parentMarriageDate.getValue());
            }

            {
                RecordMarriage marrRecord = MergeModelMarriageTest.createMarriageRecord("");
                MergeRecord mergeRecord = new MergeRecord(marrRecord);
                PropertyDate parentMarriageDate;

                marrRecord.getEventDateProperty().setValue("1 JAN 2000");
                marrRecord.getIndiBirthDate().getPropertyDate().setValue("30 JUN 1993");
                parentMarriageDate = mergeRecord.getParentMariageDate(RowType.IndiParentMarriageDate);
                assertEquals("RecordMarriage Date exacte", "BEF 1985", parentMarriageDate.getValue());

                marrRecord.getEventDateProperty().setValue("1 JAN 2000");
                marrRecord.getIndiBirthDate().getPropertyDate().setValue("30 JUN 1980");
                parentMarriageDate = mergeRecord.getParentMariageDate(RowType.IndiParentMarriageDate);
                assertEquals("RecordMarriage Date exacte", "BEF 1980", parentMarriageDate.getValue());

                marrRecord.getEventDateProperty().setValue("BET 1985 AND 2000");
                marrRecord.getIndiBirthDate().getPropertyDate().setValue("BET 1983 AND 1987");
                parentMarriageDate = mergeRecord.getParentMariageDate(RowType.IndiParentMarriageDate);
                assertEquals("RecordMarriage Date exacte", "BEF 1985", parentMarriageDate.getValue());

                marrRecord.getEventDateProperty().setValue("ABT 2000");
                marrRecord.getIndiBirthDate().getPropertyDate().setValue("AFT 1980");
                parentMarriageDate = mergeRecord.getParentMariageDate(RowType.IndiParentMarriageDate);
                assertEquals("RecordMarriage Date exacte", "BEF 1985", parentMarriageDate.getValue());

                marrRecord.getEventDateProperty().setValue("ABT 1970");
                marrRecord.getIndiBirthDate().getPropertyDate().setValue("BEF 1980");
                parentMarriageDate = mergeRecord.getParentMariageDate(RowType.IndiParentMarriageDate);
                assertEquals("RecordMarriageDate exacte", "BEF 1955", parentMarriageDate.getValue());

                marrRecord.getEventDateProperty().setValue("BEF 1980");
                marrRecord.getIndiBirthDate().getPropertyDate().setValue("ABT 1970");
                parentMarriageDate = mergeRecord.getParentMariageDate(RowType.IndiParentMarriageDate);
                assertEquals("RecordMarriageDate exacte", "BEF 1965", parentMarriageDate.getValue());
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * testaddOccupation
     */
    public void testAddOccupation() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("I1");
            MergeRecord mergeRecord = new MergeRecord(MergeModelBirthTest.createBirthRecord("sansfamille1"));

            MergeModel.copyOccupation(indi, mergeRecord.getIndiFatherOccupation(), mergeRecord);
            
            assertEquals("Nombre de profession", 2, indi.getProperties("OCCU").length);
            Property occupationProperty = indi.getProperties("OCCU")[1];
            assertEquals("Profession de l'individu", mergeRecord.getIndiFatherOccupation(), occupationProperty.getDisplayValue());
            assertEquals("Date de la profession", mergeRecord.getEventDate().getDisplayValue(), occupationProperty.getProperty("DATE").getDisplayValue());
            assertEquals("Lieu de la profession", mergeRecord.getEventPlace(), occupationProperty.getProperty("PLAC").getDisplayValue());
            assertEquals("Note de la profession", false, occupationProperty.getProperty("NOTE").getDisplayValue().isEmpty());

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }



    
}
