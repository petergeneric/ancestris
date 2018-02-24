package ancestris.modules.releve.merge;

import ancestris.modules.releve.RecordTransferHandle;
import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.dnd.TransferableRecord;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.PlaceFormatModel;
import ancestris.modules.releve.model.RecordInfoPlace;
import ancestris.modules.releve.model.RecordMisc;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import javax.swing.JFrame;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author Michel
 */
public class MergeModelMiscWillTest extends TestCase {

     static public RecordInfoPlace getRecordsInfoPlace() {
        RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
        recordsInfoPlace.setValue("ville_misc","code_misc","departement_misc","region_misc","pays_misc");
        return recordsInfoPlace;
    }

    
    /**
     *
    */
    @Test
    public void testAddOther() {
        try {
            // Merge options
            PlaceFormatModel.getModel().savePreferences(0,1,2,3,4, 6);
            
            Gedcom gedcom = TestUtility.createGedcom();


            RecordMisc willRecord = new RecordMisc();
            willRecord.setFieldValue(FieldType.eventDate, "01/03/1999");
            willRecord.setFieldValue(Record.FieldType.eventType, "testament");
            willRecord.setFieldValue(FieldType.notary, "notaire_other");
            willRecord.setFieldValue(FieldType.cote, "cote");
            willRecord.setFieldValue(FieldType.generalComment, "generalcomment");
            willRecord.setFieldValue(FieldType.freeComment,  "photo");
            willRecord.setIndi("accordfirstname", "ACCORDLASTNAME", "M", "50", "", "accordBirthplace", "accordBirthAddress", "accordoccupation", "accordResidence", "accordAddress", "accordcomment");
            
             // intervenant 2 héritier
            willRecord.setWife("Fatherfirstname", "FATHERLASTNAME", "M", "", "", "", "", "fatherOccupation2", "fatherResidence2", "fatherAddress2", "fatherComment2");
            willRecord.setWifeMarried("Motherfirstname", "MOTHERLASTNAME", "wifeoccupation2", "wifeResidence2", "wifeAddress2", "wifecomment2", "true");
            
            String fileName = "ville_misc.txt";
            MergeOptionPanel.SourceModel.getModel().add(fileName, gedcom.getEntity("SOUR", "S2").getPropertyDisplayValue("TITL"));
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, willRecord);
            MergeRecord mergeRecord = new MergeRecord(data);            

            Fam participant2Family;
            Indi participant2Wife;
            Indi participant2Husband;

            //List<MergeModel> models = MergeModel.createMergeModel(mergeRecord, gedcom, null);
            //assertEquals("Nombre model",3,models.size());           
            //models.get(0).copyRecordToEntity();
            
            MergeDialog dialog = MergeDialog.show(new JFrame(), gedcom, null, mergeRecord, false);
            //TestUtility.waitForDialogClose(dialog);
            dialog.copyRecordToEntity();

            
            Indi participant1 = (Indi) gedcom.getEntity("I7");
            assertEquals("Lien event vers source","@S2@", participant1.getValue(new TagPath("INDI:WILL:SOUR"),""));
            assertEquals("Source event","S2", gedcom.getEntity(participant1.getValue(new TagPath("INDI:WILL:SOUR"),"").replaceAll("@", "")).getId());
            assertEquals("Source event",willRecord.getFieldValue(Record.FieldType.cote) + ", " +willRecord.getFieldValue(Record.FieldType.freeComment), participant1.getValue(new TagPath("INDI:WILL:SOUR:PAGE"),""));
            assertEquals("Date event", true, willRecord.getField(FieldType.eventDate).equalsProperty( participant1.getProperty(new TagPath("INDI:WILL:DATE"))));
            assertEquals("Lieu event",getRecordsInfoPlace().getValue(), participant1.getValue(new TagPath("INDI:WILL:PLAC"),""));
            
            assertEquals("participant1 : nom",mergeRecord.getIndi().getLastName(), participant1.getLastName());
            assertEquals("participant1 : prénom",mergeRecord.getIndi().getFirstName(), participant1.getFirstName());
            assertEquals("participant1 : lieu naissance",mergeRecord.getIndi().getBirthPlace(), participant1.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("participant1 : Date décès", "FROM 1999", participant1.getDeathDate().getValue());
            
            assertEquals("participant1 : Profession",1, participant1.getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = participant1.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("participant1 : Profession",mergeRecord.getIndi().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("participant1 : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("participant1 : Lieu Profession",mergeRecord.getIndi().getResidence(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            
                        
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }


    

}
