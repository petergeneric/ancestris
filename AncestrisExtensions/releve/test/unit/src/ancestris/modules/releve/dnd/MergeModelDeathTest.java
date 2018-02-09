package ancestris.modules.releve.dnd;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.dnd.MergeModel.MergeRow;
import ancestris.modules.releve.model.PlaceFormatModel;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordInfoPlace;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Test;
import org.openide.util.Exceptions;

/**
 *
 * @author Michel
 */
public class MergeModelDeathTest extends TestCase {

    static public RecordInfoPlace getRecordsInfoPlace() {
        RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
        recordsInfoPlace.setValue("Paris","75000","","state","country");
        return recordsInfoPlace;
    }

    static public RecordDeath createDeathRecord(String firstName) {

        if ( firstName.equals("sansfamille1")) {
            RecordDeath record = new RecordDeath();
                record.setEventDate("01/01/2003");
                record.setCote("cote");
                record.setFreeComment("photo");
                record.getIndi().set("sansfamille1", "FATHERLASTNAME", "M", "3y", "", "indiBirthPlace", "birthAddress", "indioccupation", "indiResidence", "indiAddress", "indicomment");
                record.getIndi().setFather("Fatherfirstname", "FATHERLASTNAME", "fatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "", "70y");
                record.getIndi().setMother("Motherfirstname", "MOTHERLASTNAME", "motherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "dead", "72y");
                record.getIndi().setMarried("Marriedfirstname", "MARRIEDLASTNAME", "marriedOccupation", "indiMarriedResidence", "indiMarriedAddress", "marriedcomment", "dead");
                record.getWitness1().setValue("w1firstname", "w1lastname", "w1occupation", "w1comment");
                record.getWitness2().setValue("w2firstname", "w2lastname", "w2occupation", "w2comment");
                record.getWitness3().setValue("w3firstname", "w3lastname", "w3occupation", "w3comment");
                record.getWitness4().setValue("w4firstname", "w4lastname", "w4occupation", "w4comment");
                record.setGeneralComment("generalcomment");
            return record;
        } else {
            RecordDeath record = new RecordDeath();
                record.setEventDate("01/01/1988");
                record.setCote("cote");
                record.setFreeComment("photo");
                record.getIndi().set("Fatherfirstname", "FATHERLASTNAME", "M", "8y", "BEF 1981", "indiBirthPlace", "indiBirthAddress", "indioccupation", "indiResidence", "indiAddress", "indicomment");
                //record.getIndi().setFather("Fatherfirstname", "FATHERLASTNAME", "occupation", "comment", "dead");
                //record.getIndi().setMother("Motherfirstname", "MOTHERLASTNAME", "occupation", "comment", "dead");
                record.getWitness1().setValue("w1firstname", "w1lastname", "w1occupation", "w1comment");
                record.getWitness2().setValue("w2firstname", "w2lastname", "w2occupation", "w2comment");
                record.getWitness3().setValue("w3firstname", "w3lastname", "w3occupation", "w3comment");
                record.getWitness4().setValue("w4firstname", "w4lastname", "w4occupation", "w4comment");
                record.setGeneralComment("generalcomment");
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
            MergeRecord mergeRecord = new MergeRecord(getRecordsInfoPlace(), sourceTitle, recordDeath);
            List<MergeModel> models;

            // je memorise la date de naissance de l'enfant
            String previousIndiBirthDate = ((Indi)gedcom.getEntity("sansfamille1")).getBirthDate().getValue();
            // je memorise la date de naissance du pere
            String previousFatherBirthDate = ((Indi)gedcom.getEntity("I1")).getBirthDate().getValue();

            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);

            assertEquals("Nombre model",2,models.size());
            // je copie la premierse proposition dans l'entité
            models.get(0).copyRecordToEntity();
            
            assertEquals("indiDeathDate",mergeRecord.getEventDate().getValue(), indi.getPropertyByPath("INDI:DEAT:DATE").getValue());
            assertEquals("indiDeathPlace",mergeRecord.getIndi().getResidence(), indi.getPropertyByPath("INDI:DEAT:PLAC").getValue());
            assertEquals("indiDeathComment",
                    "Date de l'acte: 01/01/2003\n"
                    + "Défunt: sansfamille1 FATHERLASTNAME, 3 années, né à indiBirthPlace, indioccupation, domicile indiResidence, indicomment\n"
                    + "Conjoint: Marriedfirstname MARRIEDLASTNAME, Décédé, marriedOccupation, domicile indiMarriedResidence, marriedcomment\n"
                    + "Père: Fatherfirstname FATHERLASTNAME, 70 années, fatherOccupation, domicile indiFatherResidence, indiFatherComment\n"
                    + "Mère: Motherfirstname MOTHERLASTNAME, 72 années, Décédé, motherOccupation, domicile indiMotherResidence, indiMotherComment\n"
                    + "Témoin(s): w1firstname w1lastname, w1occupation, w1comment, w2firstname w2lastname, w2occupation, w2comment, w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment\n"
                    + "Commentaire général: generalcomment\n"
                    + "Cote: cote, photo",
                    indi.getPropertyByPath("INDI:DEAT:NOTE").getValue());

            assertEquals("indiBirthDate",previousIndiBirthDate, indi.getBirthDate().getValue());
            assertEquals("indiBirthPlace",mergeRecord.getIndi().getBirthPlace(), indi.getPropertyByPath("INDI:BIRT:PLAC").getValue());
            
            assertEquals("indiOccupation",mergeRecord.getIndi().getOccupation(), indi.getProperty("OCCU").getValue());
            assertEquals("indiOcccupationResidence",mergeRecord.getIndi().getResidence(), indi.getPropertyByPath("INDI:OCCU:PLAC").getValue());
            assertEquals("indiOcccupationDate",mergeRecord.getEventDate().getValue(), indi.getPropertyByPath("INDI:OCCU:DATE").getValue());

            // conjoint
            Indi married = indi.getFamiliesWhereSpouse()[0].getWife();
            assertEquals("marriedLastName",recordDeath.getIndi().getMarriedLastName().getValue(), married.getLastName());
            assertEquals("marriedFirstName",recordDeath.getIndi().getMarriedFirstName().getValue(), married.getFirstName());
            assertEquals("marriedBirth","BEF 1985", married.getBirthDate().getValue());
            assertEquals("marriedDead","BEF 2003", married.getDeathDate().getValue());
            Property marriedOccupation = married.getProperties("OCCU")[0];
            assertEquals("marriedOccupation",recordDeath.getIndi().getMarriedOccupation().getValue(), marriedOccupation.getValue());
            assertEquals("marriedOcccupationPlace",recordDeath.getIndi().getMarriedResidence().getValue(), marriedOccupation.getProperty("PLAC").getValue());
            assertEquals("marriedOcccupationDate",recordDeath.getEventDateProperty().getValue(), marriedOccupation.getProperty("DATE").getValue());
            assertEquals("marriedOcccupationNote","Profession indiquée dans l'acte de décès de sansfamille1 FATHERLASTNAME le 01/01/2003 (Paris)", marriedOccupation.getProperty("NOTE").getValue());

            //parents
            assertEquals("famille Parent","F1", indi.getFamilyWhereBiologicalChild().getId());
            assertEquals("Mariage Parent date","BEF 2000", indi.getFamilyWhereBiologicalChild().getMarriageDate().getValue());

            // pere
            Indi father = indi.getBiologicalFather();
            assertEquals("fatherLastName",mergeRecord.getIndi().getFatherLastName(), father.getLastName());
            assertEquals("fatherFirstName",mergeRecord.getIndi().getFatherFirstName(), father.getFirstName());
            // la date de naissance du pere n'est pas changée car elle est plus précise que celle du releve
            assertEquals("Naissance du pere",previousFatherBirthDate, father.getBirthDate().getValue());
            assertEquals("deces du pere",   "AFT 1999", father.getDeathDate().getValue());

            Property fatherOccupation = father.getProperties("OCCU")[0];
            assertEquals("fatherOccupation",mergeRecord.getIndi().getFatherOccupation(), fatherOccupation.getValue());
            assertEquals("fatherOcccupationPlace",mergeRecord.getIndi().getFatherResidence(), fatherOccupation.getProperty("PLAC").getValue());
            assertEquals("fatherOcccupationDate",mergeRecord.getEventDate().getValue(), fatherOccupation.getProperty("DATE").getValue());
            assertEquals("fatherOcccupationNote","Profession indiquée dans l'acte de décès de sansfamille1 FATHERLASTNAME le 01/01/2003 (Paris)", fatherOccupation.getProperty("NOTE").getValue());

            // mere
            Indi mother = indi.getBiologicalMother();
            assertEquals("Naissance de la mere","CAL 1931", mother.getBirthDate().getValue());
            assertEquals("deces de la mere",    "BET 2000 AND 2003", mother.getDeathDate().getValue());

            Property motherOccupation = mother.getProperties("OCCU")[0];
            assertEquals("motherOccupation",mergeRecord.getIndi().getMotherOccupation(), motherOccupation.getValue());
            assertEquals("motherOcccupationPlace",mergeRecord.getIndi().getMotherResidence(), motherOccupation.getProperty("PLAC").getValue());
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
            PlaceFormatModel.getModel().savePreferences(0,1,2,3,4, 6);
            
            Indi indi = (Indi)gedcom.getEntity("sansfamille1");
            RecordDeath recordDeath = createDeathRecord("sansfamille1");
            // je supprime la profession et le lieu de naissance
            recordDeath.getIndi().set("sansfamille1", "FATHERLASTNAME", "M", "3y", "", "", "", "", "", "", "indicomment");
            
            String sourceTitle = "";
            MergeRecord mergeRecord = new MergeRecord(getRecordsInfoPlace(), sourceTitle, recordDeath);
            List<MergeModel> models;

            // je memorise la date de naissance de l'enfant
            String previousIndiBirthDate = ((Indi)gedcom.getEntity("sansfamille1")).getBirthDate().getValue();
            // je memorise la date de naissance du pere
            String previousFatherBirthDate = ((Indi)gedcom.getEntity("I1")).getBirthDate().getValue();

            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);

            assertEquals("Nombre model",2,models.size());
            // je copie la premiere proposition dans l'entité
            models.get(0).copyRecordToEntity();
            
            assertEquals("indiDeathDate",mergeRecord.getEventDate().getValue(), indi.getPropertyByPath("INDI:DEAT:DATE").getValue());
            assertEquals("indiDeathPlace",mergeRecord.getEventPlace(), indi.getPropertyByPath("INDI:DEAT:PLAC").getValue());
            assertEquals("indiDeathComment",
                    "Date de l'acte: 01/01/2003\n"
                    + "Défunt: sansfamille1 FATHERLASTNAME, 3 années, indicomment\n"
                    + "Conjoint: Marriedfirstname MARRIEDLASTNAME, Décédé, marriedOccupation, domicile indiMarriedResidence, marriedcomment\n"
                    + "Père: Fatherfirstname FATHERLASTNAME, 70 années, fatherOccupation, domicile indiFatherResidence, indiFatherComment\n"
                    + "Mère: Motherfirstname MOTHERLASTNAME, 72 années, Décédé, motherOccupation, domicile indiMotherResidence, indiMotherComment\n"
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
            assertEquals("marriedLastName",recordDeath.getIndi().getMarriedLastName().getValue(), married.getLastName());
            assertEquals("marriedFirstName",recordDeath.getIndi().getMarriedFirstName().getValue(), married.getFirstName());
            assertEquals("marriedBirth","BEF 1985", married.getBirthDate().getValue());
            assertEquals("marriedDead","BEF 2003", married.getDeathDate().getValue());
            Property marriedOccupation = married.getProperties("OCCU")[0];
            assertEquals("marriedOccupation",recordDeath.getIndi().getMarriedOccupation().getValue(), marriedOccupation.getValue());
            assertEquals("marriedOcccupationPlace",recordDeath.getIndi().getMarriedResidence().getValue(), marriedOccupation.getProperty("PLAC").getValue());
            assertEquals("marriedOcccupationDate",recordDeath.getEventDateProperty().getValue(), marriedOccupation.getProperty("DATE").getValue());
            assertEquals("marriedOcccupationNote","Profession indiquée dans l'acte de décès de sansfamille1 FATHERLASTNAME le 01/01/2003 (Paris)", marriedOccupation.getProperty("NOTE").getValue());

            //parents
            assertEquals("famille Parent","F1", indi.getFamilyWhereBiologicalChild().getId());
            assertEquals("Mariage Parent date","BEF 2000", indi.getFamilyWhereBiologicalChild().getMarriageDate().getValue());

            // pere
            Indi father = indi.getBiologicalFather();
            assertEquals("fatherLastName",mergeRecord.getIndi().getFatherLastName(), father.getLastName());
            assertEquals("fatherFirstName",mergeRecord.getIndi().getFatherFirstName(), father.getFirstName());
            // la date de naissance du pere n'est pas changée car elle est plus précise que celle du releve
            assertEquals("Naissance du pere",previousFatherBirthDate, father.getBirthDate().getValue());
            assertEquals("deces du pere",   "AFT 1999", father.getDeathDate().getValue());

            Property fatherOccupation = father.getProperties("OCCU")[0];
            assertEquals("fatherOccupation",mergeRecord.getIndi().getFatherOccupation(), fatherOccupation.getValue());
            assertEquals("fatherOcccupationPlace",mergeRecord.getIndi().getFatherResidence(), fatherOccupation.getProperty("PLAC").getValue());
            assertEquals("fatherOcccupationDate",mergeRecord.getEventDate().getValue(), fatherOccupation.getProperty("DATE").getValue());
            assertEquals("fatherOcccupationNote","Profession indiquée dans l'acte de décès de sansfamille1 FATHERLASTNAME le 01/01/2003 (Paris)", fatherOccupation.getProperty("NOTE").getValue());

            // mere
            Indi mother = indi.getBiologicalMother();
            assertEquals("Naissance de la mere","CAL 1931", mother.getBirthDate().getValue());
            assertEquals("deces de la mere",    "BET 2000 AND 2003", mother.getDeathDate().getValue());

            Property motherOccupation = mother.getProperties("OCCU")[0];
            assertEquals("motherOccupation",mergeRecord.getIndi().getMotherOccupation(), motherOccupation.getValue());
            assertEquals("motherOcccupationPlace",mergeRecord.getIndi().getMotherResidence(), motherOccupation.getProperty("PLAC").getValue());
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
            MergeRecord mergeRecord = new MergeRecord(getRecordsInfoPlace(), sourceTitle, record);
            List<MergeModel> models;

            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",2,models.size());
            models.get(0).copyRecordToEntity();

            String expected = "";
            expected +="Date de l'acte: 01/01/2003\n";
            expected +="Défunt: sansfamille1 FATHERLASTNAME, 3 années, né à indiBirthPlace, indioccupation, domicile indiResidence, indicomment\n";
            expected +="Conjoint: Marriedfirstname MARRIEDLASTNAME, Décédé, marriedOccupation, domicile indiMarriedResidence, marriedcomment\n";
            expected +="Père: Fatherfirstname FATHERLASTNAME, 70 années, fatherOccupation, domicile indiFatherResidence, indiFatherComment\n";
            expected +="Mère: Motherfirstname MOTHERLASTNAME, 72 années, Décédé, motherOccupation, domicile indiMotherResidence, indiMotherComment\n";
            expected +="Témoin(s): w1firstname w1lastname, w1occupation, w1comment, w2firstname w2lastname, w2occupation, w2comment, w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment\n";
            expected +="Commentaire général: generalcomment\n";
            expected +="Cote: cote, photo";
            assertEquals("comment1",expected, indi.getPropertyByPath("INDI:DEAT:NOTE").getValue());

            // je verifie que le nouveau commentaire contient la concatenation de l'ancien commentaire et du nouveau
            indi.getPropertyByPath("INDI:DEAT:NOTE").setValue("oldcomment");
            models = MergeModel.createMergeModel(mergeRecord, gedcom, indi);
            assertEquals("Nombre model",1,models.size());
            models.get(0).copyRecordToEntity();
            expected ="Date de l'acte: 01/01/2003\n";
            expected +="Défunt: sansfamille1 FATHERLASTNAME, 3 années, né à indiBirthPlace, indioccupation, domicile indiResidence, indicomment\n";
            expected +="Conjoint: Marriedfirstname MARRIEDLASTNAME, Décédé, marriedOccupation, domicile indiMarriedResidence, marriedcomment\n";
            expected +="Père: Fatherfirstname FATHERLASTNAME, 70 années, fatherOccupation, domicile indiFatherResidence, indiFatherComment\n";
            expected +="Mère: Motherfirstname MOTHERLASTNAME, 72 années, Décédé, motherOccupation, domicile indiMotherResidence, indiMotherComment\n";
            expected +="Témoin(s): w1firstname w1lastname, w1occupation, w1comment, w2firstname w2lastname, w2occupation, w2comment, w3firstname w3lastname, w3occupation, w3comment, w4firstname w4lastname, w4occupation, w4comment\n";
            expected +="Commentaire général: generalcomment\n";
            expected +="Cote: cote, photo\n";
            expected += "oldcomment";
            assertEquals("comment2",expected, indi.getPropertyByPath("INDI:DEAT:NOTE").getValue());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }


    /**
     * test de la recherche de l'ex conjoint
     */
    @Test
    public void testFindMarried() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();

            RecordDeath record = new RecordDeath();
                record.setEventDate("01/01/1988");
                record.setCote("cote");
                record.setFreeComment("photo");
                record.getIndi().set("Fatherfirstname", "FATHERLASTNAME", "M", "", "BEF 1981", "indiplace", "birthAddress", "indioccupation", "indiResidence", "indiAddress", "indicomment");
                record.getIndi().setMarried("Motherfirstname", "MOTHERLASTNAME", "", "", "", "", "");
            String sourceTitle = "";
            MergeRecord mergeRecord = new MergeRecord(getRecordsInfoPlace(), sourceTitle, record);

            List<MergeModel> models;
            models = MergeModel.createMergeModel(mergeRecord, gedcom, null);
            assertEquals("Nombre model",2,models.size());


            MergeRow IndiMarriedLastNameRow =  models.get(0).getRow(MergeModel.RowType.IndiMarriedLastName);
            assertNotNull("IndiMarriedLastName", IndiMarriedLastNameRow.entityValue);
            assertEquals("IndiMarriedLastName not null", "MOTHERLASTNAME", IndiMarriedLastNameRow.entityValue.toString());

            //MergeDialog dialog = MergeDialog.show(new JFrame(), gedcom, null, record, true);
            //Thread.sleep(10000);
           
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }
    
}
