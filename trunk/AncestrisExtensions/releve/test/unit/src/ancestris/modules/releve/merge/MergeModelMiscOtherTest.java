package ancestris.modules.releve.merge;

import ancestris.modules.releve.RecordTransferHandle;
import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.dnd.TransferableRecord;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.PlaceFormatModel;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.RecordInfoPlace;
import ancestris.modules.releve.model.RecordMisc;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import java.util.List;
import javax.swing.JFrame;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author Michel
 */
public class MergeModelMiscOtherTest extends TestCase {

     static public RecordInfoPlace getRecordsInfoPlace() {
        RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
        recordsInfoPlace.setValue("ville_misc","code_misc","departement_misc","region_misc","pays_misc");
        return recordsInfoPlace;
    }

    /**
     *
    */
    public void atestAddOther() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();

            RecordMisc miscRecord = new RecordMisc();
            miscRecord.setFieldValue(FieldType.eventDate, "01/03/1999");
            miscRecord.setFieldValue(Record.FieldType.eventType, "Accord ");
            miscRecord.setFieldValue(FieldType.notary, "notaire_other");
            miscRecord.setFieldValue(FieldType.cote, "cote");
            miscRecord.setFieldValue(FieldType.generalComment, "generalcomment");
            miscRecord.setFieldValue(FieldType.freeComment,  "photo");
            miscRecord.setIndi("accordfirstname", "ACCORDLASTNAME", "M", "50", "", "accordBirthplace", "accordBirthAddress", "accordoccupation", "accordResidence", "accordAddress", "accordcomment");
            // intervenant 2
            miscRecord.setWife("Fatherfirstname", "FATHERLASTNAME", "M", "", "",  "", "", "fatherOccupation2", "fatherResidence2", "fatherAddress2", "fatherComment2");
            miscRecord.setWifeMarried("Motherfirstname", "MOTHERLASTNAME", "wifeoccupation2", "wifeResidence2", "wifeAddress2", "wifecomment2", "true");

            String fileName = "ville_misc.txt";
            MergeOptionPanel.SourceModel.getModel().add(fileName, gedcom.getEntity("SOUR", "S2").getPropertyDisplayValue("TITL"));
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, miscRecord);
            
            Fam participant2Family;
            Indi participant2Wife;
            Indi participant2Husband;

            //List<MergeModel> models = MergeModel.createMergeModel(mergeRecord, gedcom, null);
            //assertEquals("Nombre model",3,models.size());           
            //models.get(0).copyRecordToEntity();
            
            MergeDialog dialog = MergeDialog.show(new JFrame(), gedcom, null, data, false);
            //TestUtility.waitForDialogClose(dialog);
            dialog.copyRecordToEntity();
            MergeRecord mergeRecord = dialog.getMergeRecord();
            
            Indi participant1 = (Indi) gedcom.getEntity("I7");
            assertEquals("Lien event vers source","@S2@", participant1.getValue(new TagPath("INDI:EVEN:SOUR"),""));
            assertEquals("Source event","S2", gedcom.getEntity(participant1.getValue(new TagPath("INDI:EVEN:SOUR"),"").replaceAll("@", "")).getId());
            assertEquals("Source event",miscRecord.getFieldValue(FieldType.cote) + ", " +miscRecord.getFieldValue(FieldType.freeComment), participant1.getValue(new TagPath("INDI:EVEN:SOUR:PAGE"),""));
            assertEquals("Date event",true, miscRecord.getField(FieldType.eventDate).equalsProperty(participant1.getValue(new TagPath("INDI:EVEN:DATE"),"")));
            assertEquals("Lieu event",getRecordsInfoPlace().getValue(), participant1.getValue(new TagPath("INDI:EVEN:PLAC"),""));
            
            assertEquals("participant1 : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), participant1.getBirthDate().getValue());
            assertEquals("participant1 : lieu naissance",mergeRecord.getIndi().getBirthPlace(), participant1.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("participant1 : Date décès","FROM 1999", participant1.getDeathDate().getValue());
            
            assertEquals("participant1 : Profession",1, participant1.getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = participant1.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("participant1 : Profession",mergeRecord.getIndi().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("participant1 : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("participant1 : Lieu Profession",mergeRecord.getIndi().getResidence(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            Property link = participant1.getProperty(new TagPath("INDI:EVEN:XREF"));
            assertEquals("participant1 association vers participant2","@I1@", link.getValue() );

            Indi participant2 = (Indi) gedcom.getEntity("I1");
            participant2Family = participant2.getFamiliesWhereSpouse()[0];

            participant2Husband = participant2Family.getHusband();
            assertEquals("participant2 nom",mergeRecord.getWife().getLastName(), participant2Husband.getLastName());
            assertEquals("participant2 prenom",mergeRecord.getWife().getFirstName(),  participant2Husband.getFirstName());
            assertEquals("participant2 Date naissance","1 JAN 1970", participant2Husband.getBirthDate().getValue());
            assertEquals("participant2 Date deces","FROM 1999", participant2Husband.getDeathDate().getValue());
            assertEquals("participant2 : nb profession",2, participant2.getProperties(new TagPath("INDI:OCCU")).length);
            occupation = participant2Husband.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("participant2 profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("participant2 date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("participant2 lieu Profession",mergeRecord.getWife().getResidence(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("participant2 lien vers participant1","@I7@", participant2.getValue(new TagPath("INDI:ASSO"),""));
            assertEquals("participant2 lien vers participant1","INDI", participant2.getValue(new TagPath("INDI:ASSO:TYPE"),""));
            assertEquals("participant2 lien vers participant1","Présent@INDI:EVEN", participant2.getValue(new TagPath("INDI:ASSO:RELA"),""));


            participant2Wife = participant2Family.getWife();
            assertEquals("participant2 femme nom",mergeRecord.getWife().getMarriedLastName(), participant2Wife.getLastName());
            assertEquals("participant2 femme prenom",mergeRecord.getWife().getMarriedFirstName(),  participant2Wife.getFirstName());
            // la date de deces a ete ajoutée
            assertEquals("participant2 femme deces","BEF 1999", participant2Wife.getDeathDate().getValue());
            occupation = participant2Wife.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("participant2 femme Profession",mergeRecord.getWife().getMarriedOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("participant2 femme Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("participant2 femme Lieu Profession",mergeRecord.getWife().getMarriedResidence(), occupation.getValue(new TagPath("OCCU:PLAC"),""));

            //MergeDialog dialog = MergeDialog.show(new javax.swing.JFrame(), gedcom, null, miscRecord, true);
            //Thread.sleep(10000);

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }


    /**
     *
    */
    @Test
    public void testAddOtherParticipant2Wife() {
        try {
            // Merge options
            PlaceFormatModel.getModel().savePreferences(0,1,2,3,4, 6);
            
            Gedcom gedcom = TestUtility.createGedcom();


            RecordMisc miscRecord = new RecordMisc();
            miscRecord.setFieldValue(FieldType.eventDate, "01/03/1999");
            miscRecord.setFieldValue(FieldType.eventType, "Accord ");
            miscRecord.setFieldValue(FieldType.notary, "notaire_other");
            miscRecord.setFieldValue(FieldType.cote, "cote");
            miscRecord.setFieldValue(FieldType.generalComment, "generalcomment");
            miscRecord.setFieldValue(FieldType.freeComment,  "photo");
            miscRecord.setIndi("accordfirstname", "ACCORDLASTNAME", "M", "50", "", "accordBirthplace", "accordBirthAddress", "accordoccupation", "accordResidence", "accordAddress", "accordcomment");
            // je place l'epouse en premier et l'epoux en second
            miscRecord.setWife("Motherfirstname", "MOTHERLASTNAME", "F", "", "", "", "", "wifeoccupation2", "wifeResidence2",  "wifeAddress2", "wifecomment2");
            miscRecord.setWifeMarried("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation2", "fatherResidence2", "fatherAddress2", "fatherComment", "true");
            
            String fileName = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, miscRecord);            

            List<MergeModel> models;
            Fam indiParentFamily;
            Indi indiFather;
            Indi indiMother;
            Fam participant2Family;
            Indi participant2Husband;
            Indi participant2Wife;
            MergeRecord mergeRecord = new MergeRecord(data);
            models = MergeModel.createMergeModel(mergeRecord, gedcom, null);
            assertEquals("Nombre model",3,models.size());

            MergeDialog dialog = MergeDialog.show(new javax.swing.JFrame(), gedcom, null, data, false);
            //TestUtility.waitForDialogClose(dialog);

            //models.get(0).copyRecordToEntity();
            dialog.copyRecordToEntity();

            // nouvel individu cree dans Gedcom
            Indi participant1 = (Indi) gedcom.getEntity("I7");
            assertEquals("Lien event vers source","", participant1.getValue(new TagPath("INDI:EVEN:SOUR"),""));
            assertEquals("Source event","", participant1.getValue(new TagPath("INDI:EVEN:SOUR:PAGE"),""));
            assertEquals("Date event", true, miscRecord.getField(FieldType.eventDate).equalsProperty( participant1.getPropertyByPath("INDI:EVEN:DATE")));
            assertEquals("Lieu event",getRecordsInfoPlace().getValue(), participant1.getValue(new TagPath("INDI:EVEN:PLAC"),""));

            assertEquals("participant1 : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), participant1.getBirthDate().getValue());
            assertEquals("participant1 : Lieu naissance",mergeRecord.getIndi().getBirthPlace(), participant1.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("participant1 : Note naissance","Date de naissance ava 1999 déduite de l'acte 'Accord ' entre accordfirstname ACCORDLASTNAME et Motherfirstname MOTHERLASTNAME le 01/03/1999 (ville_misc, notaire_other)", participant1.getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("participant1 : Profession",1, participant1.getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = participant1.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("participant1 : Profession",     mergeRecord.getIndi().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("participant1 : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("participant1 : Lieu Profession",mergeRecord.getIndi().getResidence(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            Property link = participant1.getProperty(new TagPath("INDI:EVEN:XREF"));
            assertEquals("participant1 association vers participant2","@Wife2@", link.getValue() );

            //assertEquals("IndiBirthDate","BMS Paris", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );
            //assertEquals("Indi : Note Profession","Profession indiquée dans l'acte CM entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 ( ville_misc, notaire_misc) ",
            //        occupation.getValue(new TagPath("OCCU:NOTE"),""));


            Indi participant2 = (Indi) gedcom.getEntity("Wife2");
            //assertEquals("Wife : Note Profession",
            //        "Profession indiquée dans l'acte CM entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 ( ville_misc, notaire_misc) ",
            //        occupation.getValue(new TagPath("OCCU:NOTE"),""));

            participant2Family = participant2.getFamiliesWhereSpouse()[0];

            participant2Husband = participant2Family.getHusband();
            assertEquals("participant2 mari nom",mergeRecord.getWife().getMarriedLastName(), participant2Husband.getLastName());
            assertEquals("participant2 mari prenom",mergeRecord.getWife().getMarriedFirstName(),  participant2Husband.getFirstName());
            assertEquals("participant2 mari nombre profession",2,  participant2Husband.getProperties("OCCU").length);
            assertEquals("participant2 mari profession",mergeRecord.getWife().getMarriedOccupation(),  participant2Husband.getProperty("OCCU").getValue());
            assertEquals("participant2 mari naissance","1 JAN 1970", participant2Husband.getBirthDate().getValue());
            assertEquals("participant2 mari deces","BEF 1999", participant2Husband.getDeathDate().getValue());

            participant2Wife = participant2Family.getWife();
            assertEquals("participant2 femme nom",mergeRecord.getWife().getLastName(), participant2Wife.getLastName());
            assertEquals("participant2 femme prenom",mergeRecord.getWife().getFirstName(),  participant2Wife.getFirstName());
            assertEquals("participant2 femme Date naissance",mergeRecord.getWife().getBirthDate().getValue(), participant2Wife.getBirthDate().getValue());
            assertEquals("participant2 marie deces","FROM 1999", participant2Wife.getDeathDate().getValue());
            occupation = participant2Wife.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("participant2 femme Profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("participant2 femme Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("participant2 femme lieu Profession",mergeRecord.getWife().getResidence(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("participant2 lien vers participant1","@I7@", participant2.getValue(new TagPath("INDI:ASSO"),""));
            assertEquals("participant2 lien vers participant1","INDI", participant2.getValue(new TagPath("INDI:ASSO:TYPE"),""));
            assertEquals("participant2 lien vers participant1","Présent@INDI:EVEN", participant2.getValue(new TagPath("INDI:ASSO:RELA"),""));


            //MergeDialog dialog = MergeDialog.show(new javax.swing.JFrame(), gedcom, null, miscRecord, true);
            //Thread.sleep(10000);

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }



}
