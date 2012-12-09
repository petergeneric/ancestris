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
     * test copyMarriageDate
     */
    public void test_CopyMarriageDate() {
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
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    /**
     * test_copyOccupation
     */
    public void test_copyBirthOccupation() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("I1");
            MergeRecord mergeRecord = new MergeRecord(MergeModelBirthTest.createBirthRecord("sansfamille1"));

            MergeModel.copyOccupation(indi, mergeRecord.getIndiFatherOccupation(), mergeRecord.getIndiFatherResidence(), mergeRecord);
            
            assertEquals("Nombre de profession", 2, indi.getProperties("OCCU").length);
            Property occupationProperty = indi.getProperties("OCCU")[1];
            assertEquals("Profession de l'individu", mergeRecord.getIndiFatherOccupation(), occupationProperty.getDisplayValue());
            assertEquals("Date de la profession", mergeRecord.getEventDate().getDisplayValue(), occupationProperty.getProperty("DATE").getDisplayValue());
            assertEquals("Lieu de la profession(=lieu de naissance)", mergeRecord.getIndiBirthPlace(), occupationProperty.getProperty("PLAC").getDisplayValue());
            assertEquals("Note de la profession", false, occupationProperty.getProperty("NOTE").getDisplayValue().isEmpty());

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }



    
}
