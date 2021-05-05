package ancestris.modules.releve.merge;

import ancestris.modules.releve.IgnoreOtherTestMethod;
import ancestris.modules.releve.RecordTransferHandle;
import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.dnd.TransferableRecord;
import ancestris.modules.releve.model.PlaceFormatModel;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordInfoPlace;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.Test;
import org.openide.util.Exceptions;

/**
 *
 * @author Michel
 */
public class MergeModelDeathTest {
    @Rule
    public IgnoreOtherTestMethod rule = new IgnoreOtherTestMethod("");

    static public RecordInfoPlace getRecordsInfoPlace() {
        RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
        recordsInfoPlace.setValue("Paris","75000","","state","country");
        return recordsInfoPlace;
    }

    static public RecordDeath createDeathRecord(String firstName) {

        if ( firstName.equals("sansfamille1")) {
            RecordDeath record = new RecordDeath();
                record.setFieldValue(FieldType.eventDate, "01/01/2003");
                record.setFieldValue(FieldType.cote, "cote");
                record.setFieldValue(FieldType.freeComment,  "photo");
                record.setIndi("sansfamille1", "FATHERLASTNAME", "M", "3y", "", "indiBirthPlace", "birthAddress", "indioccupation", "indiResidence", "indiAddress", "indicomment");
                record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "", "70y");
                record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "dead", "72y");
                record.setIndiMarried("Marriedfirstname", "MARRIEDLASTNAME", "marriedOccupation", "indiMarriedResidence", "indiMarriedAddress", "marriedcomment", "dead");
                record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
                record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
                record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
                record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
                record.setFieldValue(FieldType.generalComment, "generalcomment");
            return record;
        } else {
            RecordDeath record = new RecordDeath();
                record.setFieldValue(FieldType.eventDate, "01/01/1988");
                record.setFieldValue(FieldType.cote, "cote");
                record.setFieldValue(FieldType.freeComment,  "photo");
                record.setIndi("Fatherfirstname", "FATHERLASTNAME", "M", "8y", "BEF 1981", "indiBirthPlace", "indiBirthAddress", "indioccupation", "indiResidence", "indiAddress", "indicomment");
                //record.setIndiFather("Fatherfirstname", "FATHERLASTNAME", "occupation", "comment", "dead");
                //record.setIndiMother("Motherfirstname", "MOTHERLASTNAME", "occupation", "comment", "dead");
                record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
                record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
                record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
                record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
                record.setFieldValue(FieldType.generalComment, "generalcomment");
            return record;
        }
    }


    /**
     * test_RecordDeath_copyRecordToEntity_Date
     */
    @Test
    public void test_RecordDeath_copyRecordToEntity_Date() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("sansfamille1");
            RecordDeath recordDeath = createDeathRecord("sansfamille1");
            String sourceTitle = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(),sourceTitle, recordDeath);
//TestUtility.showMergeDialog(data, gedcom, indi);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, indi);
            MergeRecord mergeRecord = mergeManager.getMergeRecord();

            // je memorise la date de naissance de l'enfant
            String previousIndiBirthDate = ((Indi)gedcom.getEntity("sansfamille1")).getBirthDate().getValue();
            // je memorise la date de naissance du pere
            String previousFatherBirthDate = ((Indi)gedcom.getEntity("I1")).getBirthDate().getValue();

            assertEquals("Nombre model", 1, mergeManager.getProposalList1().getSize());
            // je copie la premierse proposition dans l'entité
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

            assertEquals("indiDeathDate",mergeRecord.getEventDate().getValue(), indi.getValue(new TagPath("INDI:DEAT:DATE"), ""));
            assertEquals("indiDeathPlace",mergeRecord.getIndi().getPlace(), indi.getValue(new TagPath("INDI:DEAT:PLAC"), ""));
            assertEquals("indiDeathComment",
                    "Date de l'acte: 01/01/2003\n"
                    + "Défunt: sansfamille1 FATHERLASTNAME, 3 années, né à birthAddress, indiBirthPlace, indioccupation, domicile indiAddress, indiResidence, indicomment\n"
                    + "Conjoint: Marriedfirstname MARRIEDLASTNAME, Décédé, marriedOccupation, domicile indiMarriedAddress, indiMarriedResidence, marriedcomment\n"
                    + "Père: Fatherfirstname FATHERLASTNAME, 70 années, fatherOccupation, domicile indiFatherAddress, indiFatherResidence, indiFatherComment\n"
                    + "Mère: Motherfirstname MOTHERLASTNAME, 72 années, Décédé, motherOccupation, domicile indiMotherAddress, indiMotherResidence, indiMotherComment\n"
                    + "Témoin(s): w1firstname w1lastname, w1occupation, w1comment, w2firstname w2lastname, w2occupation, w2comment, w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment\n"
                    + "Commentaire général: generalcomment\n"
                    + "Cote: cote, photo",
                    indi.getValue(new TagPath("INDI:DEAT:NOTE"), ""));

            assertEquals("indiBirthDate",previousIndiBirthDate, indi.getBirthDate().getValue());
            assertEquals("indiBirthPlace",mergeRecord.getIndi().getBirthResidence().getPlace(), indi.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            assertEquals("indiBirthAddress",mergeRecord.getIndi().getBirthResidence().getAddress(), indi.getValue(new TagPath("INDI:BIRT:ADDR"), ""));
            assertEquals("indiBirthNote ","Lieu de naissance birthAddress, indiBirthPlace indiqué dans l'acte de décès de sansfamille1 FATHERLASTNAME le 01/01/2003 (Paris)", indi.getValue(new TagPath("INDI:BIRT:NOTE"), ""));

            assertEquals("indiOccupation",mergeRecord.getIndi().getOccupation(), indi.getProperty("OCCU").getValue());
            assertEquals("indiOcccupationDate",mergeRecord.getEventDate().getValue(), indi.getValue(new TagPath("INDI:OCCU:DATE"), ""));
            assertEquals("indiOcccupationResidence",mergeRecord.getIndi().getPlace(), indi.getValue(new TagPath("INDI:OCCU:PLAC"), ""));
            assertEquals("indiOcccupationAddress",mergeRecord.getIndi().getResidence().getAddress(), indi.getValue(new TagPath("INDI:OCCU:ADDR"), ""));

            // conjoint
            Indi married = indi.getFamiliesWhereSpouse()[0].getWife();
            assertEquals("marriedLastName",recordDeath.getFieldValue(FieldType.indiMarriedLastName), married.getLastName());
            assertEquals("marriedFirstName",recordDeath.getFieldValue(FieldType.indiMarriedFirstName), married.getFirstName());
            assertEquals("marriedBirth","BEF 1985", married.getBirthDate().getValue());
            assertEquals("marriedDead","BEF 2003", married.getDeathDate().getValue());
            assertEquals("marriedDeathPlace", null, married.getValue(new TagPath("INDI:DEAT:PLAC"), null));

            Property marriedOccupation = married.getProperties("OCCU")[0];
            assertEquals("marriedOccupation",recordDeath.getFieldValue(FieldType.indiMarriedOccupation), marriedOccupation.getValue());
            assertEquals("marriedOcccupationPlace",recordDeath.getFieldValue(FieldType.indiMarriedResidence), marriedOccupation.getProperty("PLAC").getValue());
            assertEquals("marriedOcccupationAddress",recordDeath.getFieldValue(FieldType.indiMarriedAddress), marriedOccupation.getProperty("ADDR").getValue());
            assertEquals("marriedOcccupationDate", true, recordDeath.getField(FieldType.eventDate).equalsProperty( marriedOccupation.getProperty("DATE")));
            assertEquals("marriedOcccupationNote","Profession indiquée dans l'acte de décès de sansfamille1 FATHERLASTNAME le 01/01/2003 (Paris)", marriedOccupation.getProperty("NOTE").getValue());

            //parents
            assertEquals("famille Parent","F1", indi.getFamilyWhereBiologicalChild().getId());
            assertEquals("Mariage Parent date","BEF 2000", indi.getFamilyWhereBiologicalChild().getMarriageDate().getValue());

            // pere
            Indi father = indi.getBiologicalFather();
            assertEquals("fatherLastName",mergeRecord.getIndi().getFather().getLastName(), father.getLastName());
            assertEquals("fatherFirstName",mergeRecord.getIndi().getFather().getFirstName(), father.getFirstName());
            // la date de naissance du pere n'est pas changée car elle est plus précise que celle du releve
            assertEquals("Naissance du pere",previousFatherBirthDate, father.getBirthDate().getValue());
            assertEquals("deces du pere",   "AFT 1999", father.getDeathDate().getValue());

            Property fatherOccupation = father.getProperties("OCCU")[0];
            assertEquals("fatherOccupation",mergeRecord.getIndi().getFather().getOccupation(), fatherOccupation.getValue());
            assertEquals("fatherOcccupationPlace",mergeRecord.getIndi().getFather().getResidence().getPlace(), fatherOccupation.getProperty("PLAC").getValue());
            assertEquals("fatherOcccupationAddress",mergeRecord.getIndi().getFather().getResidence().getAddress(), fatherOccupation.getProperty("ADDR").getValue());
            assertEquals("fatherOcccupationDate",mergeRecord.getEventDate().getValue(), fatherOccupation.getProperty("DATE").getValue());
            assertEquals("fatherOcccupationNote","Profession indiquée dans l'acte de décès de sansfamille1 FATHERLASTNAME le 01/01/2003 (Paris)", fatherOccupation.getProperty("NOTE").getValue());

            // mere
            Indi mother = indi.getBiologicalMother();
            assertEquals("Naissance de la mere","CAL 1931", mother.getBirthDate().getValue());
            assertEquals("deces de la mere",    "BET 2000 AND 2003", mother.getDeathDate().getValue());

            Property motherOccupation = mother.getProperties("OCCU")[0];
            assertEquals("motherOccupation",mergeRecord.getIndi().getMother().getOccupation(), motherOccupation.getValue());
            assertEquals("motherOcccupationPlace",mergeRecord.getIndi().getMother().getResidence().getPlace(), motherOccupation.getProperty("PLAC").getValue());
            assertEquals("motherOcccupationAddress",mergeRecord.getIndi().getMother().getResidence().getAddress(), motherOccupation.getProperty("ADDR").getValue());
            assertEquals("motherOcccupationDate",mergeRecord.getEventDate().getValue(), motherOccupation.getProperty("DATE").getValue());
            assertEquals("motherOcccupationNote","Profession indiquée dans l'acte de décès de sansfamille1 FATHERLASTNAME le 01/01/2003 (Paris)", motherOccupation.getProperty("NOTE").getValue());

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail(ex.getMessage());
        }
    }


    /**
     * test_RecordDeath_copyRecordToEntity_Date
     */
    @Test
    public void test_RecordDeath_Without_Occupation () {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            PlaceFormatModel.getCurrentModel().savePreferences(1,2,3,4,5,6);

            Indi indi = (Indi)gedcom.getEntity("sansfamille1");
            RecordDeath recordDeath = createDeathRecord("sansfamille1");
            // je supprime la profession et le lieu de naissance
            recordDeath.setIndi("sansfamille1", "FATHERLASTNAME", "M", "3y", "", "", "", "", "", "", "indicomment");

            String sourceTitle = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(),sourceTitle, recordDeath);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, indi);
            MergeRecord mergeRecord = mergeManager.getMergeRecord();


            // je memorise la date de naissance de l'enfant
            String previousIndiBirthDate = ((Indi)gedcom.getEntity("sansfamille1")).getBirthDate().getValue();
            // je memorise la date de naissance du pere
            String previousFatherBirthDate = ((Indi)gedcom.getEntity("I1")).getBirthDate().getValue();

            assertEquals("Nombre model", 1, mergeManager.getProposalList1().getSize());
            // je copie la premiere proposition dans l'entité
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

            assertEquals("indiDeathDate",mergeRecord.getEventDate().getValue(), indi.getPropertyByPath("INDI:DEAT:DATE").getValue());
            assertEquals("indiDeathPlace",mergeRecord.getEventPlace(), indi.getPropertyByPath("INDI:DEAT:PLAC").getValue());
            assertEquals("indiDeathComment",
                    "Date de l'acte: 01/01/2003\n"
                    + "Défunt: sansfamille1 FATHERLASTNAME, 3 années, indicomment\n"
                    + "Conjoint: Marriedfirstname MARRIEDLASTNAME, Décédé, marriedOccupation, domicile indiMarriedAddress, indiMarriedResidence, marriedcomment\n"
                    + "Père: Fatherfirstname FATHERLASTNAME, 70 années, fatherOccupation, domicile indiFatherAddress, indiFatherResidence, indiFatherComment\n"
                    + "Mère: Motherfirstname MOTHERLASTNAME, 72 années, Décédé, motherOccupation, domicile indiMotherAddress, indiMotherResidence, indiMotherComment\n"
                    + "Témoin(s): w1firstname w1lastname, w1occupation, w1comment, w2firstname w2lastname, w2occupation, w2comment, w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment\n"
                    + "Commentaire général: generalcomment\n"
                    + "Cote: cote, photo",
                    indi.getPropertyByPath("INDI:DEAT:NOTE").getValue());

            assertEquals("indiBirthDate",previousIndiBirthDate, indi.getBirthDate().getValue());
            assertEquals("indiBirthPlace",null, indi.getPropertyByPath("INDI:BIRT:PLAC"));

            assertEquals("indiOccupation",null, indi.getProperty("OCCU"));
            assertEquals("indiOcccupationResidence",null, indi.getPropertyByPath("INDI:OCCU:PLAC"));
            assertEquals("indiOcccupationDate",null, indi.getPropertyByPath("INDI:OCCU:DATE"));

            // conjoint
            Indi married = indi.getFamiliesWhereSpouse()[0].getWife();
            assertEquals("marriedLastName",recordDeath.getFieldValue(FieldType.indiMarriedLastName), married.getLastName());
            assertEquals("marriedFirstName",recordDeath.getFieldValue(FieldType.indiMarriedFirstName), married.getFirstName());
            assertEquals("marriedBirth","BEF 1985", married.getBirthDate().getValue());
            assertEquals("marriedDead","BEF 2003", married.getDeathDate().getValue());
            Property marriedOccupation = married.getProperties("OCCU")[0];
            assertEquals("marriedOccupation",recordDeath.getFieldValue(FieldType.indiMarriedOccupation), marriedOccupation.getValue());
            assertEquals("marriedOcccupationPlace",recordDeath.getFieldValue(FieldType.indiMarriedResidence), marriedOccupation.getProperty("PLAC").getValue());
            assertEquals("marriedOcccupationAddress",recordDeath.getFieldValue(FieldType.indiMarriedAddress), marriedOccupation.getProperty("ADDR").getValue());
            assertEquals("marriedOcccupationDate",true, recordDeath.getField(FieldType.eventDate).equalsProperty(marriedOccupation.getProperty("DATE")));
            assertEquals("marriedOcccupationNote","Profession indiquée dans l'acte de décès de sansfamille1 FATHERLASTNAME le 01/01/2003 (Paris)", marriedOccupation.getProperty("NOTE").getValue());

            //parents
            assertEquals("famille Parent","F1", indi.getFamilyWhereBiologicalChild().getId());
            assertEquals("Mariage Parent date","BEF 2000", indi.getFamilyWhereBiologicalChild().getMarriageDate().getValue());

            // pere
            Indi father = indi.getBiologicalFather();
            assertEquals("fatherLastName",mergeRecord.getIndi().getFather().getLastName(), father.getLastName());
            assertEquals("fatherFirstName",mergeRecord.getIndi().getFather().getFirstName(), father.getFirstName());
            // la date de naissance du pere n'est pas changée car elle est plus précise que celle du releve
            assertEquals("Naissance du pere",previousFatherBirthDate, father.getBirthDate().getValue());
            assertEquals("deces du pere",   "AFT 1999", father.getDeathDate().getValue());

            Property fatherOccupation = father.getProperties("OCCU")[0];
            assertEquals("fatherOccupation",mergeRecord.getIndi().getFather().getOccupation(), fatherOccupation.getValue());
            assertEquals("fatherOcccupationPlace",mergeRecord.getIndi().getFather().getResidence().getPlace(), fatherOccupation.getProperty("PLAC").getValue());
            assertEquals("fatherOcccupationAddress",mergeRecord.getIndi().getFather().getResidence().getAddress(), fatherOccupation.getProperty("ADDR").getValue());
            assertEquals("fatherOcccupationDate",mergeRecord.getEventDate().getValue(), fatherOccupation.getProperty("DATE").getValue());
            assertEquals("fatherOcccupationNote","Profession indiquée dans l'acte de décès de sansfamille1 FATHERLASTNAME le 01/01/2003 (Paris)", fatherOccupation.getProperty("NOTE").getValue());

            // mere
            Indi mother = indi.getBiologicalMother();
            assertEquals("Naissance de la mere","CAL 1931", mother.getBirthDate().getValue());
            assertEquals("deces de la mere",    "BET 2000 AND 2003", mother.getDeathDate().getValue());

            Property motherOccupation = mother.getProperties("OCCU")[0];
            assertEquals("motherOccupation",mergeRecord.getIndi().getMother().getOccupation(), motherOccupation.getValue());
            assertEquals("motherOcccupationPlace",mergeRecord.getIndi().getMother().getResidence().getPlace(), motherOccupation.getProperty("PLAC").getValue());
            assertEquals("motherOcccupationAddress",mergeRecord.getIndi().getMother().getResidence().getAddress(), motherOccupation.getProperty("ADDR").getValue());
            assertEquals("motherOcccupationDate",mergeRecord.getEventDate().getValue(), motherOccupation.getProperty("DATE").getValue());
            assertEquals("motherOcccupationNote","Profession indiquée dans l'acte de décès de sansfamille1 FATHERLASTNAME le 01/01/2003 (Paris)", motherOccupation.getProperty("NOTE").getValue());

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fail(ex.getMessage());
        }
    }

    /**
     * test_RecordDeath_copyRecordToEntity_Comment
     */
    @Test
    public void test_RecordDeath_copyRecordToEntity_Comment() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("sansfamille1");
            RecordDeath record = createDeathRecord("sansfamille1");
            String sourceTitle = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(),sourceTitle, record);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, indi);
            assertEquals("Nombre model", 1, mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();

            String expected = "";
            expected +="Date de l'acte: 01/01/2003\n";
            expected +="Défunt: sansfamille1 FATHERLASTNAME, 3 années, né à birthAddress, indiBirthPlace, indioccupation, domicile indiAddress, indiResidence, indicomment\n";
            expected +="Conjoint: Marriedfirstname MARRIEDLASTNAME, Décédé, marriedOccupation, domicile indiMarriedAddress, indiMarriedResidence, marriedcomment\n";
            expected +="Père: Fatherfirstname FATHERLASTNAME, 70 années, fatherOccupation, domicile indiFatherAddress, indiFatherResidence, indiFatherComment\n";
            expected +="Mère: Motherfirstname MOTHERLASTNAME, 72 années, Décédé, motherOccupation, domicile indiMotherAddress, indiMotherResidence, indiMotherComment\n";
            expected += "Témoin(s): w1firstname w1lastname, w1occupation, w1comment, w2firstname w2lastname, w2occupation, w2comment, w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment\n";
            expected += "Commentaire général: generalcomment\n";
            expected += "Cote: cote, photo";
            assertEquals("comment1", expected, indi.getValue(new TagPath("INDI:DEAT:NOTE"), ""));

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    /**
     * test_RecordDeath_copyRecordToEntity_Comment
     */
    @Test
    public void test_RecordDeath_copyRecordToEntity_Add_Comment() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("sansfamille1");
            RecordDeath record = createDeathRecord("sansfamille1");
            String sourceTitle = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(),sourceTitle, record);

            // je verifie que le nouveau commentaire contient la concatenation de l'ancien commentaire et du nouveau
            indi.addProperty("DEAT", "").addProperty("NOTE", "previouscomment");
            //indi.getPropertyByPath("INDI:DEAT").addProperty("NOTE", "previouscomment");
            //indi.getPropertyByPath("INDI:DEAT:NOTE").setValue("oldcomment");
//TestUtility.showMergeDialog(data, gedcom, indi);
            MergeManager mergeManager;
            mergeManager = TestUtility.createMergeManager(data, gedcom, indi);
            assertEquals("Nombre model",1,mergeManager.getProposalList1().getSize());
            mergeManager.getProposalList1().getElementAt(0).copyRecordToEntity();
            assertEquals("Nombre model", 1, mergeManager.getProposalList1().getSize());
            String expected = "Date de l'acte: 01/01/2003\n";
            expected +="Défunt: sansfamille1 FATHERLASTNAME, 3 années, né à birthAddress, indiBirthPlace, indioccupation, domicile indiAddress, indiResidence, indicomment\n";
            expected +="Conjoint: Marriedfirstname MARRIEDLASTNAME, Décédé, marriedOccupation, domicile indiMarriedAddress, indiMarriedResidence, marriedcomment\n";
            expected +="Père: Fatherfirstname FATHERLASTNAME, 70 années, fatherOccupation, domicile indiFatherAddress, indiFatherResidence, indiFatherComment\n";
            expected +="Mère: Motherfirstname MOTHERLASTNAME, 72 années, Décédé, motherOccupation, domicile indiMotherAddress, indiMotherResidence, indiMotherComment\n";
            expected +="Témoin(s): w1firstname w1lastname, w1occupation, w1comment, w2firstname w2lastname, w2occupation, w2comment, w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment\n";
            expected +="Commentaire général: generalcomment\n";
            expected +="Cote: cote, photo\n";
            expected += "previouscomment";
            assertEquals("comment2",expected, indi.getValue(new TagPath("INDI:DEAT:NOTE"), ""));
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }


    /**
     * test de la recherche de l'ex conjoint
     */
    @Test
    public void testIndiMarried() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();

            RecordDeath record = new RecordDeath();
            record.setFieldValue(FieldType.eventDate, "01/01/1988");
            record.setFieldValue(FieldType.cote, "cote");
            record.setFieldValue(FieldType.freeComment, "photo");
            record.setIndi("Fatherfirstname", "FATHERLASTNAME", "M", "", "1970", "indiplace", "birthAddress", "indioccupation", "indiResidence", "indiAddress", "indicomment");
            record.setIndiMarried("Motherfirstname", "MOTHERLASTNAME", "", "", "", "", "");
            String sourceTitle = "";
            TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, getRecordsInfoPlace(),sourceTitle, record);
            MergeManager mergeManager = TestUtility.createMergeManager(data, gedcom, null);
            assertEquals("Nombre model",2,mergeManager.getProposalList1().getSize());

            Indi indi = (Indi) mergeManager.getProposalList1().getElementAt(0).getMainEntity();
            Indi indiMarried = indi.getFamiliesWhereSpouse()[0].getOtherSpouse(indi);
            //MergeRow IndiMarriedLastNameRow =  mergeManager.getProposalList1().getElementAt(0).getRow(MergeRow.RowType.IndiMarriedLastName);

            assertNotNull("IndiMarriedLastName", indiMarried.getLastName());
            assertEquals("IndiMarriedLastName not null", "MOTHERLASTNAME", indiMarried.getLastName());


        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

}
