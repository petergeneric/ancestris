package ancestris.modules.releve.merge;

import ancestris.modules.releve.RecordTransferHandle;
import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.dnd.TransferableRecord;
import ancestris.modules.releve.merge.MergeModel.RowType;
import ancestris.modules.releve.model.RecordBirth;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.TagPath;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author Michel
 */
public class MergeModelTest extends TestCase {

    /**
     * test copyMarriageDate
     */
    @Test
    public void test_CopyMarriageDate() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Fam fam = (Fam)gedcom.getEntity("F1");
            RecordBirth record = MergeModelBirthTest.createBirthRecord("sansfamille1");
            String fileName = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, record);
            MergeRecord mergeRecord = new MergeRecord(data);            

            MergeModel mergeModel = new MergeModelBirth(mergeRecord, gedcom);
            PropertyDate marriageDate = new PropertyDate();
            

            marriageDate.setValue("1 NOV 1970");
            mergeModel.addRow(RowType.MarriageDate, marriageDate, fam.getMarriageDate());
            mergeModel.copyMarriageDate(fam, mergeModel.getRow(RowType.MarriageDate), mergeRecord);
            assertEquals("Date marriage", marriageDate.getValue(), fam.getMarriageDate().getValue());
            assertEquals("Note marriage",
                    "Date de mariage 1 nov 1970 déduite de l'acte de naissance de sansfamille1 FATHERLASTNAME le 01/01/2000 (Paris)",
                    fam.getProperty( new TagPath("FAM:MARR:NOTE")).getValue().substring(fam.getProperty( new TagPath("FAM:MARR:NOTE")).getValue().lastIndexOf("\n")+1));

            marriageDate.setValue("22 NOV 1972");
            mergeModel.addRow(RowType.MarriageDate, marriageDate, fam.getMarriageDate());
            mergeModel.addRow(RowType.MarriageDate, marriageDate, fam.getMarriageDate());
            mergeModel.copyMarriageDate(fam, mergeModel.getRow(RowType.MarriageDate), mergeRecord);
            assertEquals("Date marriage", marriageDate.getValue(), fam.getMarriageDate().getValue());
            assertEquals("Note marriage", 
                    "Date de mariage 22 nov 1972 déduite de l'acte de naissance de sansfamille1 FATHERLASTNAME le 01/01/2000 (Paris)",
                    fam.getProperty( new TagPath("FAM:MARR:NOTE")).getValue().substring(fam.getProperty( new TagPath("FAM:MARR:NOTE")).getValue().lastIndexOf("\n")+1));
            

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    /**
     * test_copyOccupation
     */
    @Test
    public void test_copyBirthOccupation() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("I1");
            String fileName = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, MergeModelBirthTest.getRecordsInfoPlace(), fileName, MergeModelBirthTest.createBirthRecord("sansfamille1"));
            MergeRecord mergeRecord = new MergeRecord(data);            

            MergeModelBirth mergeModel = new MergeModelBirth(mergeRecord, gedcom);

            mergeModel.copyOccupation(indi, mergeRecord.getIndi().getFatherOccupation(), mergeRecord.getIndi().getFatherResidence(), mergeRecord.getIndi().getFatherAddress(), true, mergeRecord);
            
            assertEquals("Nombre de profession", 2, indi.getProperties("OCCU").length);
            Property occupationProperty = indi.getProperties("OCCU")[0];
            assertEquals("Profession de l'individu", mergeRecord.getIndi().getFatherOccupation(), occupationProperty.getDisplayValue());
            assertEquals("Date de la profession", mergeRecord.getEventDate().getDisplayValue(), occupationProperty.getProperty("DATE").getDisplayValue());
            assertEquals("Lieu de la profession", mergeRecord.getIndi().getFatherResidence(), occupationProperty.getProperty("PLAC").getDisplayValue());
            assertEquals("Adresse de la profession", mergeRecord.getIndi().getFatherAddress(), occupationProperty.getProperty("ADDR").getDisplayValue());
            assertEquals("Note de la profession", false, occupationProperty.getProperty("NOTE").getDisplayValue().isEmpty());

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }



    
}
