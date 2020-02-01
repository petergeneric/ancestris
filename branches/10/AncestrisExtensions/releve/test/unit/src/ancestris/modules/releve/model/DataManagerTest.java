package ancestris.modules.releve.model;
import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.Record.FieldType;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Michel
 */
public class DataManagerTest {

    /**
     * Test of removeGedcomCompletion method, of class CompletionProvider.
     */
    @Test
    public void testAddGedcom() {
       try {
            DataManager dataManager = new DataManager();

            RecordBirth record = new RecordBirth();
            record.setFieldValue(FieldType.eventDate, "01/01/2000");
            record.setFieldValue(FieldType.cote, "cote");
            record.setFieldValue(FieldType.freeComment,  "photo");
            record.setIndi("OneFirstName", "FATHERLASTNAME_GEDCOM", "F", "", "", "indiBirthplace", "indiBirthAddress", "indioccupation", "indiResidence", "indiAddress", "indicomment");
            record.setIndiFather("Fatherfirstname", "FATHERLASTNAME_GEDCOM", "fatherOccupation", "indiFatherResidence", "indiFatherAddress", "comment", "dead", "70y");
            record.setIndiMother("Motherfirstname", "MOTHERLASTNAME_GEDCOM", "motherOccupation", "indiMotherResidence", "indiMotherAddress", "comment", "dead", "72y");
            record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
            record.setFieldValue(FieldType.generalComment, "generalcomment");

            dataManager.addRecord(record);
           // je verifie que les données ont bien été supprimées
            assertEquals("Nombre de prenoms apres ajout", 7, dataManager.getCompletionProvider().getFirstNames().getAll().size());
            assertEquals("Nombre de noms apres ajout", 6, dataManager.getCompletionProvider().getLastNames().getAll().size());
            assertEquals("Nombre de professions apres ajout", 7, dataManager.getCompletionProvider().getOccupations().getAll().size());
            assertEquals("Nombre de lieux apres ajout", 4, dataManager.getCompletionProvider().getPlaces().getAll().size());

            Gedcom gedcom = TestUtility.createGedcomF2();

//            File file = new File("D:/Genealogie/GED/test1.ged");
//            boolean exis = file.exists();
//            FileObject fo = FileUtil.toFileObject(file);
//            Context context = GedcomDirectory.getDefault().openAncestrisGedcom(fo);
//            Gedcom gedcom = context.getGedcom();
//            int placeCount = gedcom.getPropertyCount("PLAC");
//            Property[] plac = gedcom.getProperties(new TagPath("INDI:PLAC"));
//
//            //List<PropertyPlace> plac2 = (List<PropertyPlace>) gedcom.getPropertiesByClass(PropertyPlace.class);
//            List<String> set1 = gedcom.getReferenceSet("PLAC").getKeys();

//            ReferenceSet<String, Property> plac3 = gedcom.getReferenceSet("PLAC.0");
//                    String[] choices = PropertyChoiceValue.getChoices(gedcom, "PLAC", false);

            dataManager.addGedcomCompletion(gedcom);

            assertEquals("Nombre de prenoms apres ajout gedcom", 7+4, dataManager.getCompletionProvider().getFirstNames().getAll().size());
            assertEquals("Nombre de noms apres ajout gedcom", 6+2, dataManager.getCompletionProvider().getLastNames().getAll().size());
            assertEquals("Nombre de professions apres ajout gedcom", 7+2, dataManager.getCompletionProvider().getOccupations().getAll().size());
            //assertEquals("Nombre de lieux apres ajout gedcom", 4+1, dataManager.getCompletionProvider().getPlaces().getAll().size());

            dataManager.removeGedcomCompletion();
            assertEquals("Nombre de prenoms apres suppression gedcom", 7, dataManager.getCompletionProvider().getFirstNames().getAll().size());
            assertEquals("Nombre de noms apres suppression gedcom", 6, dataManager.getCompletionProvider().getLastNames().getAll().size());
            assertEquals("Nombre de professions apres suppression gedcom", 7, dataManager.getCompletionProvider().getOccupations().getAll().size());
            assertEquals("Nombre de lieuxapres suppression gedcom", 4, dataManager.getCompletionProvider().getPlaces().getAll().size());

            dataManager.removeRecord(record);
            assertEquals("Nombre de prenoms apres suppression record", 0, dataManager.getCompletionProvider().getFirstNames().getAll().size());
            assertEquals("Nombre de noms apres suppression record", 0, dataManager.getCompletionProvider().getLastNames().getAll().size());
            assertEquals("Nombre de professions apres suppression record", 0, dataManager.getCompletionProvider().getOccupations().getAll().size());
            assertEquals("Nombre de lieux apres suppression record", 0, dataManager.getCompletionProvider().getPlaces().getAll().size());

        } catch (GedcomException ex) {
            fail("GedcomException " + ex.toString());
        }
    }



}
