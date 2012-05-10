package ancestris.modules.releve.dnd;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.RecordBirth;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import java.awt.Point;
import javax.swing.JFrame;
import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class MergeDialogTest extends TestCase {



    /**
     * testMergeRecordBirth avec source existante
     */
    public void testMergeRecordBirthSourceExistanteDejaAssociee() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();            
            Indi indi = (Indi)gedcom.getEntity("I1");

            RecordBirth birth = new RecordBirth();
            birth.setEventDate("01/01/2000");
            birth.setCote("cote");
            birth.setFreeComment("photo");
            birth.setIndi("firstname", "FATHERLASTNAME", "M", "", "", "place", "occupation", "comment");
            birth.setIndiFather("fathername", "fatherlastname", "occupation", "comment", "dead");
            birth.setIndiMother("mothername", "motherlastname", "occupation", "comment", "dead");
            birth.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            birth.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            birth.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            birth.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

            birth.setEventPlace("Paris","75000","","state","country");

            MergeDialog dialog = MergeDialog.show(new JFrame(), new Point (200,200), indi, birth, false);
            dialog.model.saveData();
            dialog.dispose();
            assertEquals("IndiFirstName",birth.getIndiFirstName().toString(), indi.getFirstName());
            assertEquals("IndiLastName",birth.getIndiLastName().toString(), indi.getLastName());
            assertEquals("IndiSex",birth.getIndiSex().getSex(), indi.getSex());
            assertEquals("IndiBirthDate",0, indi.getBirthDate().compareTo(birth.getEventDateField()));
            Property[] sourceLink = indi.getProperties(new TagPath("INDI:BIRT:SOUR"));
            assertEquals("Nb birthsource",1,sourceLink.length );
            assertEquals("IndiBirthDate","75000 Paris BMS", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );
            
        } catch (GedcomException ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * testMergeRecordBirth avec source existante mais non associee à la naissance
     */
    public void testMergeRecordBirthSourceExistanteNonAssociee() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("I1");

            RecordBirth birth = new RecordBirth();
            birth.setEventDate("01/01/2000");
            birth.setCote("cote");
            birth.setFreeComment("photo");
            birth.setIndi("firstname", "FATHERLASTNAME", "M", "", "", "place", "occupation", "comment");
            birth.setIndiFather("fathername", "fatherlastname", "occupation", "comment", "dead");
            birth.setIndiMother("mothername", "motherlastname", "occupation", "comment", "dead");
            birth.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            birth.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            birth.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            birth.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

            birth.setEventPlace("Brest","35000","","state","country");

            MergeDialog dialog = MergeDialog.show(new JFrame(), new Point (200,200), indi, birth, false);
            dialog.model.saveData();
            dialog.dispose();
            assertEquals("IndiFirstName",birth.getIndiFirstName().toString(), indi.getFirstName());
            assertEquals("IndiLastName",birth.getIndiLastName().toString(), indi.getLastName());
            assertEquals("IndiSex",birth.getIndiSex().getSex(), indi.getSex());
            assertEquals("IndiBirthDate",0, indi.getBirthDate().compareTo(birth.getEventDateField()));
            Property[] sourceLink = indi.getProperties(new TagPath("INDI:BIRT:SOUR"));
            assertEquals("Nb birthsource",2,sourceLink.length );
            assertEquals("IndiBirthDate","35000 Brest Etat civil", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );

        } catch (GedcomException ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * testMergeRecordBirth avec nouvelle source
     */
    public void testMergeRecordBirthSourceNewSource() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();

            Indi indi = (Indi)gedcom.getEntity("I1");

            RecordBirth birth = new RecordBirth();
            birth.setEventDate("01/01/2000");
            birth.setCote("cote");
            birth.setFreeComment("photo");
            birth.setIndi("firstname", "FATHERLASTNAME", "M", "", "", "place", "occupation", "comment");
            birth.setIndiFather("fathername", "fatherlastname", "occupation", "comment", "dead");
            birth.setIndiMother("mothername", "motherlastname", "occupation", "comment", "dead");
            birth.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
            birth.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
            birth.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
            birth.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");

            birth.setEventPlace("Paris","75009","","state","country");

            MergeDialog dialog = MergeDialog.show(new JFrame(), new Point (200,200), indi, birth, false);
            dialog.model.saveData();
            dialog.dispose();
            assertEquals("IndiFirstName",birth.getIndiFirstName().toString(), indi.getFirstName());
            assertEquals("IndiLastName",birth.getIndiLastName().toString(), indi.getLastName());
            assertEquals("IndiSex",birth.getIndiSex().getSex(), indi.getSex());
            assertEquals("IndiBirthDate",0, indi.getBirthDate().compareTo(birth.getEventDateField()));
            Property[] sourceLink = indi.getProperties(new TagPath("INDI:BIRT:SOUR"));
            assertEquals("Nb birthsource",2,sourceLink.length );
            assertEquals("IndiBirthDate","75009 Paris Etat civil", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );

        } catch (GedcomException ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * testMergeRecordBirth avec nouvelle source
     */
    public void testMergeRecordBirthAutreIndiExistant() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();

            Indi indi = (Indi)gedcom.getEntity("I1");

            RecordBirth birth = new RecordBirth();
            birth.setEventDate("02/02/1972"); // meme date de naissance que cousin
            birth.setCote("cote");
            birth.setFreeComment("photo");
            birth.setIndi("cousin", "FATHERLASTNAME", "M", "", "", "place", "occupation", "comment");
            birth.setIndiFather("fathername", "fatherlastname", "occupation", "comment", "dead");
            birth.setIndiMother("mothername", "motherlastname", "occupation", "comment", "dead");
            birth.setWitness1("wfirstname", "wlastname", "woccupation", "wcomment");
            birth.setWitness2("wfirstname", "wlastname", "woccupation", "wcomment");
            birth.setWitness3("wfirstname", "wlastname", "woccupation", "wcomment");
            birth.setWitness4("wfirstname", "wlastname", "woccupation", "wcomment");

            birth.setEventPlace("Paris","75009","","state","country");

            MergeDialog dialog = MergeDialog.show(new JFrame(), new Point (200,200), indi, birth, false);
            assertEquals("otherIndi",1, dialog.model.findSameIndi().size());
            dialog.model.saveData();
            dialog.dispose();
            assertEquals("Indi First Name",birth.getIndiFirstName().toString(), indi.getFirstName());
            assertEquals("Indi Last Name",birth.getIndiLastName().toString(), indi.getLastName());
            assertEquals("Indi Sex",birth.getIndiSex().getSex(), indi.getSex());
            assertEquals("Indi Birth Date",0, indi.getBirthDate().compareTo(birth.getEventDateField()));
            Property[] sourceLink = indi.getProperties(new TagPath("INDI:BIRT:SOUR"));
            assertEquals("Nb birthsource",2,sourceLink.length );
            assertEquals("IndiBirthDate","75009 Paris Etat civil", ((Source)((PropertyXRef)sourceLink[0]).getTargetEntity()).getTitle() );

        } catch (GedcomException ex) {
            fail(ex.getMessage());
        }
    }

    
}
