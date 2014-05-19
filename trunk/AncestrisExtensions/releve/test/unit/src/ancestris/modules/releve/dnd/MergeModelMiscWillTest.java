package ancestris.modules.releve.dnd;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.FieldPlace;
import ancestris.modules.releve.model.RecordMisc;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import java.util.List;
import javax.swing.JFrame;
import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class MergeModelMiscWillTest extends TestCase {

     static public FieldPlace getRecordsInfoPlace() {
        FieldPlace recordsInfoPlace = new FieldPlace();
        recordsInfoPlace.setValue("ville_misc,code_misc,departement_misc,region_misc,pays_misc");
        return recordsInfoPlace;
    }

    
    /**
     *
    */
    public void testAddOther() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();


            RecordMisc willRecord = new RecordMisc();
            willRecord.setEventDate("01/03/1999");
            willRecord.setEventType("testament");
            willRecord.setNotary("notaire_other");
            willRecord.setCote("cote");
            willRecord.setGeneralComment("generalcomment");
            willRecord.setFreeComment("photo");
            willRecord.setIndi("accordfirstname", "ACCORDLASTNAME", "M", "50", "", "accordBirthplace", "accordoccupation", "accordResidence", "accordcomment");
            
             // intervenant 2 héritier
            willRecord.setWife("Fatherfirstname", "FATHERLASTNAME", "M", "", "", "", "fatherOccupation2", "fatherResidence2", "fatherComment2");
            willRecord.setWifeMarried("Motherfirstname", "MOTHERLASTNAME", "wifeoccupation2", "wifeResidence2", "wifecomment2", "true");
            
            String fileName = "ville_misc.txt";
            MergeOptionPanel.SourceModel.getModel().add(fileName, gedcom.getEntity("SOUR", "S2").getPropertyDisplayValue("TITL"));
            MergeRecord mergeRecord = new MergeRecord(getRecordsInfoPlace(), fileName, willRecord);

            Fam participant2Family;
            Indi participant2Wife;
            Indi participant2Husband;

            //List<MergeModel> models = MergeModel.createMergeModel(mergeRecord, gedcom, null);
            //assertEquals("Nombre model",3,models.size());           
            //models.get(0).copyRecordToEntity();
            
            MergeDialog dialog = MergeDialog.show(new JFrame(), gedcom, null, mergeRecord, false);
            //TestUtility.waitForDialogClose(dialog);
            dialog.copyRecordToEntity();

            
            Indi participant1 = (Indi) gedcom.getEntity("I00007");
            assertEquals("Lien event vers source","@S2@", participant1.getValue(new TagPath("INDI:WILL:SOUR"),""));
            assertEquals("Source event","S2", gedcom.getEntity(participant1.getValue(new TagPath("INDI:WILL:SOUR"),"").replaceAll("@", "")).getId());
            assertEquals("Source event",willRecord.getCote().getValue() + ", " +willRecord.getFreeComment().getValue(), participant1.getValue(new TagPath("INDI:WILL:SOUR:PAGE"),""));
            assertEquals("Date event",willRecord.getEventDateProperty().getValue(), participant1.getValue(new TagPath("INDI:WILL:DATE"),""));
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
