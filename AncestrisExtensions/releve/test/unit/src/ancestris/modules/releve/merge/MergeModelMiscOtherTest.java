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
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Michel
 */
public class MergeModelMiscOtherTest {
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
            miscRecord.setWifeFather("FatherMotherFirstName", "FatherMotherLastname", "FatherMotherOccupation", "FatherMotherPlace", "FatherMotherAdress", "FatherMotherComment", "false", "70");

            String fileName = "ville_misc.txt";
            MergeOptionPanel.SourceModel.getModel().add(fileName, gedcom.getEntity("SOUR", "S2").getPropertyDisplayValue("TITL"));
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(), fileName, miscRecord);

            Fam indi2Family;
            Indi indi2Wife;
            Indi indi2Husband;
            Indi indi2 = (Indi) gedcom.getEntity("I1");
            String indi2BirthDate = indi2.getBirthDate().getValue();

//TestUtility.showMergeDialog(data, gedcom, null);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
            assertEquals("Nombre model",1,mergeManager.getProposalList1().getSize());
            assertEquals("Nombre model",2,mergeManager.getProposalList2().getSize());
            mergeManager.copyRecordToEntity(mergeManager.getProposalList1().getElementAt(0), mergeManager.getProposalList2().getElementAt(0));

            MergeRecord mergeRecord = mergeManager.getMergeRecord();
            Indi indi1 = (Indi) mergeManager.getProposalList1().getElementAt(0).getMainEntity();
            assertEquals("Lien event vers source","@S2@", indi1.getValue(new TagPath("INDI:EVEN:SOUR"),""));
            assertEquals("Source event","S2", gedcom.getEntity(indi1.getValue(new TagPath("INDI:EVEN:SOUR"),"").replaceAll("@", "")).getId());
            assertEquals("Page event",miscRecord.getFieldValue(FieldType.cote) + ", " +miscRecord.getFieldValue(FieldType.freeComment), indi1.getValue(new TagPath("INDI:EVEN:SOUR:PAGE"),""));
            assertEquals("Date event",mergeRecord.getEventDate().getValue(), indi1.getValue(new TagPath("INDI:EVEN:DATE"),""));
            assertEquals("Lieu event",getRecordsInfoPlace().getValue(), indi1.getValue(new TagPath("INDI:EVEN:PLAC"),""));

            assertEquals("participant1 : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), indi1.getBirthDate().getValue());
            assertEquals("participant1 : lieu naissance",mergeRecord.getIndi().getBirthResidence().getPlace(), indi1.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("participant1 : adresse naissance",mergeRecord.getIndi().getBirthResidence().getAddress(), indi1.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            assertEquals("participant1 : Date décès","FROM 1999", indi1.getDeathDate().getValue());
            assertEquals("participant1 : lieu décès", null, indi1.getValue(new TagPath("INDI:DEAT:PLAC"), null));

            assertEquals("participant1 : Profession",1, indi1.getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = indi1.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("participant1 : Profession",mergeRecord.getIndi().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("participant1 : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("participant1 : Lieu Profession",mergeRecord.getIndi().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("participant1 : Adresse Profession",mergeRecord.getIndi().getResidence().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"),""));
            Property link = indi1.getProperty(new TagPath("INDI:EVEN:XREF"));
            assertEquals("participant1 association vers participant2","@I1@", link.getValue() );

            assertEquals("participant1 parent Family", null,  indi1.getFamilyWhereBiologicalChild() );

            indi2Family = indi2.getFamiliesWhereSpouse()[0];

            indi2Husband = indi2Family.getHusband();
            assertEquals("participant2 nom",mergeRecord.getWife().getLastName(), indi2Husband.getLastName());
            assertEquals("participant2 prenom",mergeRecord.getWife().getFirstName(),  indi2Husband.getFirstName());
            // la date de naissance n'a pas changé (car moins précise dans le releve)
            assertEquals("participant2 Date naissance",indi2BirthDate, indi2Husband.getBirthDate().getValue());
            assertNotSame("participant2 Lieu naissance",mergeRecord.getWife().getBirthResidence().getPlace(), indi2Husband.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("participant2 adresse naissance",mergeRecord.getWife().getBirthResidence().getAddress(), indi2Husband.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            assertEquals("participant2 Date deces","FROM 1999", indi2Husband.getDeathDate().getValue());
            assertEquals("participant2 : nb profession",2, indi2.getProperties(new TagPath("INDI:OCCU")).length);
            occupation = indi2Husband.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("participant2 profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("participant2 date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("participant2 lieu Profession",mergeRecord.getWife().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("participant2 adresse Profession",mergeRecord.getWife().getResidence().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"),""));
            assertEquals("participant2 lien vers participant1","@I7@", indi2.getValue(new TagPath("INDI:ASSO"),""));
            assertEquals("participant2 lien vers participant1","INDI", indi2.getValue(new TagPath("INDI:ASSO:TYPE"),""));
            assertEquals("participant2 lien vers participant1","Présent@INDI:EVEN", indi2.getValue(new TagPath("INDI:ASSO:RELA"),""));


            indi2Wife = indi2Family.getWife();
            // je verifie que la source n'est pas ajoutée
            assertEquals("Lien event vers source",null, indi2.getValue(new TagPath("INDI:EVEN:SOUR"),null));
            assertEquals("participant2 conjoint nom",mergeRecord.getWife().getMarriedFamily().getMarried().getLastName(), indi2Wife.getLastName());
            assertEquals("participant2 conjoint prenom",mergeRecord.getWife().getMarriedFamily().getMarried().getFirstName(),  indi2Wife.getFirstName());
            // la date de deces a ete ajoutée
            assertEquals("participant2 conjoint deces","BEF 1999", indi2Wife.getDeathDate().getValue());
            occupation = indi2Wife.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("participant2 conjoint Profession",mergeRecord.getWife().getMarriedFamily().getMarried().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("participant2 conjoint Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("participant2 conjoint Lieu Profession",mergeRecord.getWife().getMarriedFamily().getMarried().getResidence().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("participant2 conjoint Addresse Profession",mergeRecord.getWife().getMarriedFamily().getMarried().getResidence().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"),""));
            // je verifie que le pere est cree dans le gedcom
            Fam wifeParentFamily = indi2.getFamilyWhereBiologicalChild();
            assertEquals("participant2 parent marriage date","BEF 1981", wifeParentFamily.getMarriageDate().getValue() );
            Indi wifeFather =  indi2.getBiologicalFather();
            assertNotSame("participant2 father",null, wifeFather );
            // je verifie que la mere n'est pas cree dans le gedcom car ses nom et prénom sont vides
            assertEquals("participant2 mother ",null, indi2.getBiologicalMother() );


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
            PlaceFormatModel.getCurrentModel().savePreferences(1,2,3,4,5,6);

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
//TestUtility.showMergeDialog(data, gedcom, null);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
            MergeRecord mergeRecord = mergeManager.getMergeRecord();

            assertEquals("Nombre model",1,mergeManager.getProposalList1().getSize());
            assertEquals("Nombre model",2,mergeManager.getProposalList2().getSize());

            mergeManager.copyRecordToEntity(mergeManager.getProposalList1().getElementAt(0), mergeManager.getProposalList2().getElementAt(0));

            Fam participant2Family;
            Indi participant2Husband;
            Indi participant2Wife;

            // nouvel individu cree dans Gedcom
            Indi participant1 = (Indi) gedcom.getEntity("I7");
            assertEquals("Lien event vers source","", participant1.getValue(new TagPath("INDI:EVEN:SOUR"),""));
            assertEquals("Source event","", participant1.getValue(new TagPath("INDI:EVEN:SOUR:PAGE"),""));
            assertEquals("Date event", true, miscRecord.getField(FieldType.eventDate).equalsProperty( participant1.getPropertyByPath("INDI:EVEN:DATE")));
            assertEquals("Lieu event",getRecordsInfoPlace().getValue(), participant1.getValue(new TagPath("INDI:EVEN:PLAC"),""));

            assertEquals("participant1 : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), participant1.getBirthDate().getValue());
            assertEquals("participant1 : Lieu naissance",mergeRecord.getIndi().getBirthResidence().getPlace(), participant1.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("participant1 : Note naissance","Date de naissance ava 1999 déduite de l'acte 'Accord ' entre accordfirstname ACCORDLASTNAME et Motherfirstname MOTHERLASTNAME le 01/03/1999 (ville_misc, notaire_other)", participant1.getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("participant1 : Profession",1, participant1.getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = participant1.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("participant1 : Profession",     mergeRecord.getIndi().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("participant1 : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("participant1 : Lieu Profession",mergeRecord.getIndi().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            Property link = participant1.getProperty(new TagPath("INDI:EVEN:XREF"));
            assertEquals("participant1 association vers participant2","@Wife2@", link.getValue() );

            //assertEquals("IndiBirthDate","BMS Paris", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );
            //assertEquals("Indi : Note Profession","Profession indiquée dans l'acte CM entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 ( ville_misc, notaire_misc) ",
            //        occupation.getValue(new TagPath("OCCU:NOTE"),""));

            assertEquals("Nombre model",2,mergeManager.getProposalList2().getSize());
            Indi participant2 = (Indi) gedcom.getEntity("Wife2");
            //assertEquals("Wife : Note Profession",
            //        "Profession indiquée dans l'acte CM entre Fatherfirstname FATHERLASTNAME et Motherfirstname WIFEFATHERLASTNAME le 01/03/1999 ( ville_misc, notaire_misc) ",
            //        occupation.getValue(new TagPath("OCCU:NOTE"),""));

            participant2Family = participant2.getFamiliesWhereSpouse()[0];

            participant2Husband = participant2Family.getHusband();
            assertEquals("participant2 mari nom",mergeRecord.getWife().getMarriedFamily().getMarried().getLastName(), participant2Husband.getLastName());
            assertEquals("participant2 mari prenom",mergeRecord.getWife().getMarriedFamily().getMarried().getFirstName(),  participant2Husband.getFirstName());
            assertEquals("participant2 mari nombre profession",2,  participant2Husband.getProperties("OCCU").length);
            assertEquals("participant2 mari profession",mergeRecord.getWife().getMarriedFamily().getMarried().getOccupation(),  participant2Husband.getProperty("OCCU").getValue());
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
            assertEquals("participant2 femme lieu Profession",mergeRecord.getWife().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("participant2 lien vers participant1","@I7@", participant2.getValue(new TagPath("INDI:ASSO"),""));
            assertEquals("participant2 lien vers participant1","INDI", participant2.getValue(new TagPath("INDI:ASSO:TYPE"),""));
            assertEquals("participant2 lien vers participant1","Présent@INDI:EVEN", participant2.getValue(new TagPath("INDI:ASSO:RELA"),""));

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    /**
     *
    */
    @Test
    public void testAddOther_insinuation_accord() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();

            RecordMisc miscRecord = new RecordMisc();
            miscRecord.setFieldValue(FieldType.secondDate, "22/03/1999");
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
            Indi participant2 = (Indi) gedcom.getEntity("I1");
            String participant2BirthDate = participant2.getBirthDate().getValue();

//TestUtility.showMergeDialog(data, gedcom, null);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
            assertEquals("Nombre model",1,mergeManager.getProposalList1().getSize());
            assertEquals("Nombre model",2,mergeManager.getProposalList2().getSize());
            mergeManager.copyRecordToEntity(mergeManager.getProposalList1().getElementAt(0), mergeManager.getProposalList2().getElementAt(0));

            MergeRecord mergeRecord = mergeManager.getMergeRecord();
            Indi participant1 = (Indi) mergeManager.getProposalList1().getElementAt(0).getMainEntity();
            Property[] events = participant1.getProperties("EVEN");
            assertEquals("Nombre events",2,events.length);
            Property insinuation  = events[1];
            assertEquals("Insinuation type", mergeRecord.getInsinuationType(), insinuation.getValue(new TagPath("EVEN:TYPE"),null));
            assertEquals("Insinuation date", mergeRecord.getInsinuationDate().getValue(), insinuation.getValue(new TagPath("EVEN:DATE"),null));
            assertEquals("Insinuation place", mergeRecord.getEventPlace(), insinuation.getValue(new TagPath("EVEN:PLAC"),null));
            assertEquals("Insinuation addresse", null, insinuation.getValue(new TagPath("EVEN:ADDR"),null));
            assertEquals("Insinutation source","@S2@", insinuation.getValue(new TagPath("EVEN:SOUR"),null));
            assertEquals("Insinutation page", mergeRecord.getEventCote() + ", " + mergeRecord.getEventPage() , insinuation.getValue(new TagPath("EVEN:SOUR:PAGE"),null));
            assertEquals("Insinuation note", true, insinuation.getValue(new TagPath("EVEN:NOTE"),"").contains(mergeRecord.getEventComment(mergeManager.m_showFrenchCalendarDate)));

            Property accord  = events[0];
            //assertEquals("event ",  null, accord.getValue(new TagPath("EVEN"), null));
            assertEquals("event type",  mergeRecord.getEventType(), accord.getValue(new TagPath("EVEN:TYPE"), null));
            assertEquals("event date",  mergeRecord.getEventDate().getValue(), accord.getValue(new TagPath("EVEN:DATE"), null));
            assertEquals("event place", null, accord.getValue(new TagPath("EVEN:PLAC"), null));
            assertEquals("event source", null, accord.getValue(new TagPath("EVEN:SOUR"), null));
            assertEquals("event page", null, accord.getValue(new TagPath("EVEN:SOUR:PAGE"), null));
            assertEquals("marc note",  mergeManager.m_helper.getReferenceNote(accord, "DATE"), accord.getValue(new TagPath("EVEN:NOTE"), null));

            assertEquals("participant1 : Date naissance",mergeRecord.getIndi().getBirthDate().getValue(), participant1.getBirthDate().getValue());
            assertEquals("participant1 : lieu naissance",mergeRecord.getIndi().getBirthResidence().getPlace(), participant1.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("participant1 : adresse naissance",mergeRecord.getIndi().getBirthResidence().getAddress(), participant1.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            assertEquals("participant1 : Date décès","FROM 1999", participant1.getDeathDate().getValue());

            assertEquals("participant1 : Profession",1, participant1.getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = participant1.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("participant1 : Profession",mergeRecord.getIndi().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("participant1 : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("participant1 : Lieu Profession",mergeRecord.getIndi().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("participant1 : Adresse Profession",mergeRecord.getIndi().getResidence().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"),""));
            Property link = participant1.getProperty(new TagPath("INDI:EVEN:XREF"));
            assertEquals("participant1 association vers participant2","@I1@", link.getValue() );

            participant2Family = participant2.getFamiliesWhereSpouse()[0];

            participant2Husband = participant2Family.getHusband();
            assertEquals("participant2 nom",mergeRecord.getWife().getLastName(), participant2Husband.getLastName());
            assertEquals("participant2 prenom",mergeRecord.getWife().getFirstName(),  participant2Husband.getFirstName());
            // la date de naissance n'a pas changé (car moins précise dans le releve)
            assertEquals("participant2 Date naissance",participant2BirthDate, participant2Husband.getBirthDate().getValue());
            assertNotSame("participant2 Lieu naissance",mergeRecord.getWife().getBirthResidence().getPlace(), participant2Husband.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("participant2 adresse naissance",mergeRecord.getWife().getBirthResidence().getAddress(), participant2Husband.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            assertEquals("participant2 Date deces","FROM 1999", participant2Husband.getDeathDate().getValue());
            assertEquals("participant2 : nb profession",2, participant2.getProperties(new TagPath("INDI:OCCU")).length);
            occupation = participant2Husband.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("participant2 profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("participant2 date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("participant2 lieu Profession",mergeRecord.getWife().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("participant2 adresse Profession",mergeRecord.getWife().getResidence().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"),""));
            assertEquals("participant2 lien vers participant1","@I7@", participant2.getValue(new TagPath("INDI:ASSO"),""));
            assertEquals("participant2 lien vers participant1","INDI", participant2.getValue(new TagPath("INDI:ASSO:TYPE"),""));
            assertEquals("participant2 lien vers participant1","Présent@INDI:EVEN#1", participant2.getValue(new TagPath("INDI:ASSO:RELA"),""));


            participant2Wife = participant2Family.getWife();
            assertEquals("participant2 femme nom",mergeRecord.getWife().getMarriedFamily().getMarried().getLastName(), participant2Wife.getLastName());
            assertEquals("participant2 femme prenom",mergeRecord.getWife().getMarriedFamily().getMarried().getFirstName(),  participant2Wife.getFirstName());
            // la date de deces a ete ajoutée
            assertEquals("participant2 femme deces","BEF 1999", participant2Wife.getDeathDate().getValue());
            occupation = participant2Wife.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("participant2 femme Profession",mergeRecord.getWife().getMarriedFamily().getMarried().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("participant2 femme Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("participant2 femme Lieu Profession",mergeRecord.getWife().getMarriedFamily().getMarried().getResidence().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("participant2 femme Addresse Profession",mergeRecord.getWife().getMarriedFamily().getMarried().getResidence().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"),""));

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    /**
     *
    */
    @Test
    public void testAddOther_insinuation_indi_exist() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            {   // j'ajoute indi
                Indi indi = (Indi) gedcom.createEntity(Gedcom.INDI);
                indi.setName("accordfirstname", "ACCORDLASTNAME");
                indi.setSex(PropertySex.MALE);
                Property birth = indi.addProperty("BIRT", "");
                birth.addProperty("DATE", "1949");
            }
            RecordMisc miscRecord = new RecordMisc();
            miscRecord.setFieldValue(FieldType.secondDate, "22/03/1999");
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
            Indi participant2 = (Indi) gedcom.getEntity("I1");
            String participant2BirthDate = participant2.getBirthDate().getValue();

//TestUtility.showMergeDialog(data, gedcom, null);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
            assertEquals("Nombre model",2,mergeManager.getProposalList1().getSize());
            assertEquals("Nombre model",2,mergeManager.getProposalList2().getSize());
            mergeManager.copyRecordToEntity(mergeManager.getProposalList1().getElementAt(0), mergeManager.getProposalList2().getElementAt(0));

            MergeRecord mergeRecord = mergeManager.getMergeRecord();
            Indi participant1 = (Indi) mergeManager.getProposalList1().getElementAt(0).getMainEntity();
            Property[] events = participant1.getProperties("EVEN");
            assertEquals("Nombre events",2,events.length);
            Property insinuation  = events[1];
            assertEquals("Insinuation type", mergeRecord.getInsinuationType(), insinuation.getValue(new TagPath("EVEN:TYPE"),null));
            assertEquals("Insinuation date", mergeRecord.getInsinuationDate().getValue(), insinuation.getValue(new TagPath("EVEN:DATE"),null));
            assertEquals("Insinuation place", mergeRecord.getEventPlace(), insinuation.getValue(new TagPath("EVEN:PLAC"),null));
            assertEquals("Insinuation addresse", null, insinuation.getValue(new TagPath("EVEN:ADDR"),null));
            assertEquals("Insinutation source","@S2@", insinuation.getValue(new TagPath("EVEN:SOUR"),null));
            assertEquals("Insinutation page", mergeRecord.getEventCote() + ", " + mergeRecord.getEventPage() , insinuation.getValue(new TagPath("EVEN:SOUR:PAGE"),null));
            assertEquals("Insinuation note", true, insinuation.getValue(new TagPath("EVEN:NOTE"),"").contains(mergeRecord.getEventComment(mergeManager.m_showFrenchCalendarDate)));

            Property accord  = events[0];
            //assertEquals("event ",  null, accord.getValue(new TagPath("EVEN"), null));
            assertEquals("event type",  mergeRecord.getEventType(), accord.getValue(new TagPath("EVEN:TYPE"), null));
            assertEquals("event date",  mergeRecord.getEventDate().getValue(), accord.getValue(new TagPath("EVEN:DATE"), null));
            assertEquals("event place", null, accord.getValue(new TagPath("EVEN:PLAC"), null));
            assertEquals("event source", null, accord.getValue(new TagPath("EVEN:SOUR"), null));
            assertEquals("event page", null, accord.getValue(new TagPath("EVEN:SOUR:PAGE"), null));
            assertEquals("marc note",  mergeManager.m_helper.getReferenceNote(accord, "DATE"), accord.getValue(new TagPath("EVEN:NOTE"), null));

            assertEquals("participant1 : Date naissance inchangée","1949", participant1.getBirthDate().getValue());
            assertEquals("participant1 : lieu naissance",mergeRecord.getIndi().getBirthResidence().getPlace(), participant1.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("participant1 : adresse naissance",mergeRecord.getIndi().getBirthResidence().getAddress(), participant1.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            assertEquals("participant1 : Date décès","FROM 1999", participant1.getDeathDate().getValue());

            assertEquals("participant1 : Profession",1, participant1.getProperties(new TagPath("INDI:OCCU")).length);
            Property occupation = participant1.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("participant1 : Profession",mergeRecord.getIndi().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("participant1 : Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("participant1 : Lieu Profession",mergeRecord.getIndi().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("participant1 : Adresse Profession",mergeRecord.getIndi().getResidence().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"),""));
            Property link = participant1.getProperty(new TagPath("INDI:EVEN:XREF"));
            assertEquals("participant1 association vers participant2","@I1@", link.getValue() );

            participant2Family = participant2.getFamiliesWhereSpouse()[0];

            participant2Husband = participant2Family.getHusband();
            assertEquals("participant2 nom",mergeRecord.getWife().getLastName(), participant2Husband.getLastName());
            assertEquals("participant2 prenom",mergeRecord.getWife().getFirstName(),  participant2Husband.getFirstName());
            // la date de naissance n'a pas changé (car moins précise dans le releve)
            assertEquals("participant2 Date naissance",participant2BirthDate, participant2Husband.getBirthDate().getValue());
            assertNotSame("participant2 Lieu naissance",mergeRecord.getWife().getBirthResidence().getPlace(), participant2Husband.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("participant2 adresse naissance",mergeRecord.getWife().getBirthResidence().getAddress(), participant2Husband.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            assertEquals("participant2 Date deces","FROM 1999", participant2Husband.getDeathDate().getValue());
            assertEquals("participant2 : nb profession",2, participant2.getProperties(new TagPath("INDI:OCCU")).length);
            occupation = participant2Husband.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("participant2 profession",mergeRecord.getWife().getOccupation(),      occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("participant2 date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("participant2 lieu Profession",mergeRecord.getWife().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("participant2 adresse Profession",mergeRecord.getWife().getResidence().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"),""));
            assertEquals("participant2 lien vers participant1","@I7@", participant2.getValue(new TagPath("INDI:ASSO"),""));
            assertEquals("participant2 lien vers participant1","INDI", participant2.getValue(new TagPath("INDI:ASSO:TYPE"),""));
            assertEquals("participant2 lien vers participant1","Présent@INDI:EVEN#1", participant2.getValue(new TagPath("INDI:ASSO:RELA"),""));


            participant2Wife = participant2Family.getWife();
            assertEquals("participant2 femme nom",mergeRecord.getWife().getMarriedFamily().getMarried().getLastName(), participant2Wife.getLastName());
            assertEquals("participant2 femme prenom",mergeRecord.getWife().getMarriedFamily().getMarried().getFirstName(),  participant2Wife.getFirstName());
            // la date de deces a ete ajoutée
            assertEquals("participant2 femme deces","BEF 1999", participant2Wife.getDeathDate().getValue());
            occupation = participant2Wife.getProperties(new TagPath("INDI:OCCU"))[0];
            assertEquals("participant2 femme Profession",mergeRecord.getWife().getMarriedFamily().getMarried().getOccupation(), occupation.getValue(new TagPath("OCCU"),""));
            assertEquals("participant2 femme Date Profession",mergeRecord.getEventDate().getValue(), occupation.getValue(new TagPath("OCCU:DATE"),""));
            assertEquals("participant2 femme Lieu Profession",mergeRecord.getWife().getMarriedFamily().getMarried().getResidence().getPlace(), occupation.getValue(new TagPath("OCCU:PLAC"),""));
            assertEquals("participant2 femme Addresse Profession",mergeRecord.getWife().getMarriedFamily().getMarried().getResidence().getAddress(), occupation.getValue(new TagPath("OCCU:ADDR"),""));

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

}
