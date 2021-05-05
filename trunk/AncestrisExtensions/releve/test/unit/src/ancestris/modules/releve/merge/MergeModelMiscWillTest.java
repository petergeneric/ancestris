package ancestris.modules.releve.merge;

import ancestris.modules.releve.IgnoreOtherTestMethod;
import ancestris.modules.releve.RecordTransferHandle;
import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.dnd.TransferableRecord;
import ancestris.modules.releve.model.PlaceFormatModel;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.RecordInfoPlace;
import ancestris.modules.releve.model.RecordMisc;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Michel
 */
public class MergeModelMiscWillTest {
    @Rule
    public IgnoreOtherTestMethod rule = new IgnoreOtherTestMethod("");

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
            PlaceFormatModel.getCurrentModel().savePreferences(1,2,3,4,5,6);
            Gedcom gedcom = TestUtility.createGedcom();

            RecordMisc willRecord = new RecordMisc();
            // intervenant 1 testateur
            willRecord.setFieldValue(FieldType.eventDate, "01/03/1999");
            willRecord.setFieldValue(Record.FieldType.eventType, "testament");
            willRecord.setFieldValue(FieldType.notary,          "notaire_other");
            willRecord.setFieldValue(FieldType.cote,            "cote");
            willRecord.setFieldValue(FieldType.generalComment, "generalcomment");
            willRecord.setFieldValue(FieldType.freeComment,  "photo");
            willRecord.setIndi("accordfirstname", "ACCORDLASTNAME", "M", "50", "", "accordBirthplace", "accordBirthAddress", "accordoccupation", "accordResidence", "accordAddress", "accordcomment");
             // intervenant 2 héritier
            willRecord.setWife("Fatherfirstname", "FATHERLASTNAME", "M", "", "", "", "", "fatherOccupation2", "fatherResidence2", "fatherAddress2", "fatherComment2");
            willRecord.setWifeMarried("Motherfirstname", "MOTHERLASTNAME", "wifeoccupation2", "wifeResidence2", "wifeAddress2", "wifecomment2", "true");

            String fileName = "ville_misc.txt";
            MergeOptionPanel.SourceModel.getModel().add(fileName, gedcom.getEntity("SOUR", "S2").getPropertyDisplayValue("TITL"));
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, willRecord);
//TestUtility.showMergeDialog(data, gedcom, null);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
            MergeRecord mergeRecord = mergeManager.getMergeRecord();
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

            Indi participant1 = (Indi) mergeManager.getProposalList1().getElementAt(0).getMainEntity();
            assertEquals("Lien event vers source","@S2@", participant1.getValue(new TagPath("INDI:WILL:SOUR"),""));
            assertEquals("Source event","S2", gedcom.getEntity(participant1.getValue(new TagPath("INDI:WILL:SOUR"),"").replaceAll("@", "")).getId());
            assertEquals("Source event",willRecord.getFieldValue(Record.FieldType.cote) + ", " +willRecord.getFieldValue(Record.FieldType.freeComment), participant1.getValue(new TagPath("INDI:WILL:SOUR:PAGE"),""));
            assertEquals("Date event", true, willRecord.getField(FieldType.eventDate).equalsProperty( participant1.getProperty(new TagPath("INDI:WILL:DATE"))));
            assertEquals("Lieu event",getRecordsInfoPlace().getValue(), participant1.getValue(new TagPath("INDI:WILL:PLAC"),""));

            assertEquals("participant1 : nom",mergeRecord.getIndi().getLastName(), participant1.getLastName());
            assertEquals("participant1 : prénom",mergeRecord.getIndi().getFirstName(), participant1.getFirstName());
            assertEquals("participant1 : lieu naissance",mergeRecord.getIndi().getBirthResidence().getPlace(), participant1.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("participant1 : adresse naissance",mergeRecord.getIndi().getBirthResidence().getAddress(), participant1.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            assertEquals("participant1 : Date décès", "FROM 1999", participant1.getDeathDate().getValue());

            assertEquals("participant1 : Profession",1, participant1.getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = participant1.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("participant1 : Profession",mergeRecord.getIndi().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("participant1 : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("participant1 : Lieu Profession",mergeRecord.getIndi().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("participant1 : Adresse Profession",mergeRecord.getIndi().getResidence().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"),""));


        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    @Test
    public void testAddWill_insinuation() {
        try {
            // Merge options
            PlaceFormatModel.getCurrentModel().savePreferences(1,2,3,4,5,6);
            Gedcom gedcom = TestUtility.createGedcom();

            RecordMisc willRecord = new RecordMisc();
            // intervenant 1 testateur
            willRecord.setFieldValue(FieldType.secondDate, "05/03/1999");
            willRecord.setFieldValue(FieldType.eventDate, "01/03/1999");
            willRecord.setFieldValue(Record.FieldType.eventType, "testament");
            willRecord.setFieldValue(FieldType.notary,          "notaire_other");
            willRecord.setFieldValue(FieldType.cote,            "cote");
            willRecord.setFieldValue(FieldType.generalComment, "generalcomment");
            willRecord.setFieldValue(FieldType.freeComment,  "photo");
            willRecord.setIndi("accordfirstname", "ACCORDLASTNAME", "M", "50", "", "accordBirthplace", "accordBirthAddress", "accordoccupation", "accordResidence", "accordAddress", "accordcomment");
             // intervenant 2 héritier
            willRecord.setWife("Fatherfirstname", "FATHERLASTNAME", "M", "", "", "", "", "fatherOccupation2", "fatherResidence2", "fatherAddress2", "fatherComment2");
            willRecord.setWifeMarried("Motherfirstname", "MOTHERLASTNAME", "wifeoccupation2", "wifeResidence2", "wifeAddress2", "wifecomment2", "true");

            String fileName = "ville_misc.txt";
            MergeOptionPanel.SourceModel.getModel().add(fileName, gedcom.getEntity("SOUR", "S2").getPropertyDisplayValue("TITL"));
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, willRecord);
//TestUtility.showMergeDialog(data, gedcom, null);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
            MergeRecord mergeRecord = mergeManager.getMergeRecord();
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

            Indi participant1 = (Indi) mergeManager.getProposalList1().getElementAt(0).getMainEntity();
            Property[] events = participant1.getProperties("EVEN");
            assertEquals("Nb events",1,events.length);
            Property insinuation  = events[0];
            assertEquals("Insinuation type", mergeRecord.getInsinuationType(), insinuation.getValue(new TagPath("EVEN:TYPE"),null));
            assertEquals("Insinuation date", mergeRecord.getInsinuationDate().getValue(), insinuation.getValue(new TagPath("EVEN:DATE"),null));
            assertEquals("Insinuation place", mergeRecord.getEventPlace(), insinuation.getValue(new TagPath("EVEN:PLAC"),null));
            assertEquals("Insinuation address", null, insinuation.getValue(new TagPath("EVEN:ADDR"),null));
            assertEquals("Insinutation source","@S2@", insinuation.getValue(new TagPath("EVEN:SOUR"),null));
            assertEquals("Insinutation page", mergeRecord.getEventCote() + ", " + mergeRecord.getEventPage() , insinuation.getValue(new TagPath("EVEN:SOUR:PAGE"),null));
            assertEquals("Insinuation note", true, insinuation.getValue(new TagPath("EVEN:NOTE"),"").contains(mergeRecord.getEventComment(mergeManager.m_showFrenchCalendarDate)));
            Property[] wills = participant1.getProperties("WILL");
            assertEquals("Nb wills",1,events.length);
            Property will  = wills[0];
            assertEquals("event date",  mergeRecord.getEventDate().getValue(), will.getValue(new TagPath("WILL:DATE"), null));
            assertEquals("event place", null, will.getValue(new TagPath("WILL:PLAC"), null));
            assertEquals("event source",null, will.getValue(new TagPath("WILL:SOUR"), null));
            assertEquals("event page",  null, will.getValue(new TagPath("WILL:SOUR:PAGE"), null));
            assertEquals("event note",  mergeManager.m_helper.getReferenceNote(will, "DATE"), will.getValue(new TagPath("WILL:NOTE"), null));

            assertEquals("participant1 : nom",mergeRecord.getIndi().getLastName(), participant1.getLastName());
            assertEquals("participant1 : prénom",mergeRecord.getIndi().getFirstName(), participant1.getFirstName());
            assertEquals("participant1 : lieu naissance",mergeRecord.getIndi().getBirthResidence().getPlace(), participant1.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("participant1 : adresse naissance",mergeRecord.getIndi().getBirthResidence().getAddress(), participant1.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            assertEquals("participant1 : Date décès", "FROM 1999", participant1.getDeathDate().getValue());

            assertEquals("participant1 : Profession",1, participant1.getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = participant1.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("participant1 : Profession",mergeRecord.getIndi().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("participant1 : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("participant1 : Lieu Profession",mergeRecord.getIndi().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("participant1 : Adresse Profession",mergeRecord.getIndi().getResidence().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"),""));


        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }




}
