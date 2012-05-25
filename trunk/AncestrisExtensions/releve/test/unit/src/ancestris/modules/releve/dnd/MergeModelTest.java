package ancestris.modules.releve.dnd;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.RecordBirth;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class MergeModelTest extends TestCase {


    /**
     * testaddOccupation
     */
    public void testAddOccupation() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("I1");
            RecordBirth record = TestUtility.createBirthRecord("sansfamille1");

            MergeModel.addOccupation(indi, record.getIndiFatherOccupation().toString(), record.getEventDateField(), record);
            
            assertEquals("Nombre de profession", 2, indi.getProperties("OCCU").length);
            Property occupationProperty = indi.getProperties("OCCU")[1];
            assertEquals("Profession de l'individu", record.getIndiFatherOccupation().toString(), occupationProperty.getDisplayValue());
            assertEquals("Date de la profession", record.getEventDateField().getDisplayValue(), occupationProperty.getProperty("DATE").getDisplayValue());
            assertEquals("Lieu de la profession", record.getEventPlace().toString(), occupationProperty.getProperty("PLAC").getDisplayValue());
            assertEquals("Note de la profession", false, occupationProperty.getProperty("NOTE").getDisplayValue().isEmpty());

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * testFindParentsFamily
     */
    public void testIsRecordAfterThanDate() {
        try {
            PropertyDate recordDate = new PropertyDate();
            PropertyDate parentBirthDate = new PropertyDate();
            boolean result;

            recordDate.setValue("1 FEB 2000");
            parentBirthDate.setValue("1 FEB 1970");
            result = MergeModel.isRecordAfterThanDate(recordDate, parentBirthDate, 0, MergeModel.minParentYearOld);
            assertEquals("le releve est apres la naissance du pere ", true, result);

            recordDate.setValue("1 FEB 1976");
            parentBirthDate.setValue("1 FEB 1970");
            result = MergeModel.isRecordAfterThanDate(recordDate, parentBirthDate, 0, MergeModel.minParentYearOld);
            assertEquals("le releve est apres la naissance du pere ", false, result);

            recordDate.setValue("1 FEB 1969");
            parentBirthDate.setValue("1 FEB 1970");
            result = MergeModel.isRecordAfterThanDate(recordDate, parentBirthDate, 0, MergeModel.minParentYearOld);
            assertEquals("le releve est apres la naissance du pere ", false, result);

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * testFindParentsFamily
     */
    public void testFindSameChild() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Fam family = (Fam)gedcom.getEntity("F1");
            RecordBirth record;
            List<Indi> children;

            record = TestUtility.createBirthRecord("sansfamille1");
            children = MergeModel.findSameChild(record, gedcom, family);
            assertEquals("l'enfant n'existe pas dans la famille", 0, children.size());
            
            record = TestUtility.createBirthRecord("child1");
            children = MergeModel.findSameChild(record, gedcom, family);
            assertEquals("l'enfant existe deja dans la famille", 1, children.size());
            assertEquals("Nom de l'enfant", record.getIndiFirstName().toString(), children.get(0).getFirstName());

            record = TestUtility.createBirthRecord("child1");
            Indi father = family.getHusband();
            PropertyDate fatherBirtDate =  father.getBirthDate();
            // je l'individu est né 10 ans apres la naissance du pere
            record.getEventDateField().getStart().set(fatherBirtDate.getStart());
            record.getEventDateField().getStart().add(0, 0, 10);
            try {
                children = MergeModel.findSameChild(record, gedcom, family);
                fail("exception non levee pour pere trop jeune");
            } catch (Exception e) {
                // ok
            }

            record = TestUtility.createBirthRecord("child1");
            family.getHusband().setName("firstname", "DIFFERENTNAMETHANCHILD");
            try {
                children = MergeModel.findSameChild(record, gedcom, family);
                fail("exception non levee pour nom du pere different");
            } catch (Exception e) {
                // ok
            }


        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

   /**
     * testFindParentsFamily
     */
    public void testFindParentsFamily() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("child1");
            RecordBirth record = TestUtility.createBirthRecord("sansfamille1");

            List<Fam> fams = MergeModel.findParentFamily(record, gedcom, indi);
            assertEquals("l'individu existe et a deja une famille", 1, fams.size());
            assertEquals("Nom de la mere", record.getIndiMotherLastName().toString(), fams.get(0).getWife().getLastName());

            indi = (Indi)gedcom.getEntity("sansfamille1");
            fams = MergeModel.findParentFamily(record, gedcom, indi);
            assertEquals("l'individu existe mais n'avait pas de famille", 1, fams.size());
            assertEquals("Nom de la mere", record.getIndiMotherLastName().toString(), fams.get(0).getWife().getLastName());


            indi = (Indi)gedcom.getEntity("sansfamille1");
            Indi father = (Indi) gedcom.getEntity("I1");
            PropertyDate fatherBirtDate =  father.getBirthDate();
            // je l'individu est né 10 ans apres la naissance du pere
            record.getEventDateField().getStart().set(fatherBirtDate.getStart());
            record.getEventDateField().getStart().add(0, 0, 10);
            fams = MergeModel.findParentFamily(record, gedcom, indi);
            assertEquals("l'individu existe mais n'a que 10 ans de plus que le pere", 0, fams.size());

            record.getEventDateField().setValue("1 JAN 2000");

            fatherBirtDate.setValue("BEF 1880");
            fams = MergeModel.findParentFamily(record, gedcom, indi);
            assertEquals("Father="+fatherBirtDate.getValue(), 0, fams.size());

            fatherBirtDate.setValue("BEF 1990");
            fams = MergeModel.findParentFamily(record, gedcom, indi);
            assertEquals("Father="+fatherBirtDate.getValue(), 1, fams.size());


            fatherBirtDate.setValue("BET 1980 AND 1990");
            fams = MergeModel.findParentFamily(record, gedcom, indi);
            assertEquals("Father="+fatherBirtDate.getValue(), 1, fams.size());

            fatherBirtDate.setValue("BET 1990 AND 2000");
            fams = MergeModel.findParentFamily(record, gedcom, indi);
            assertEquals("Father="+fatherBirtDate.getValue(), 0, fams.size());

            fatherBirtDate.setValue("AFT 2001");
            fams = MergeModel.findParentFamily(record, gedcom, indi);
            assertEquals("Father="+fatherBirtDate.getValue(), 0, fams.size());



            fatherBirtDate.setValue("EST 1870");
            fams = MergeModel.findParentFamily(record, gedcom, indi);
            assertEquals("Father="+fatherBirtDate.getValue(), 0, fams.size());

            fatherBirtDate.setValue("EST 1894");
            fams = MergeModel.findParentFamily(record, gedcom, indi);
            assertEquals("Father="+fatherBirtDate.getValue(), 0, fams.size());

            fatherBirtDate.setValue("EST 1989");
            fams = MergeModel.findParentFamily(record, gedcom, indi);
            assertEquals("Father="+fatherBirtDate.getValue(), 1, fams.size());

            fatherBirtDate.setValue("EST 2001");
            fams = MergeModel.findParentFamily(record, gedcom, indi);
            assertEquals("Father="+fatherBirtDate.getValue(), 0, fams.size());


        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * testfindSameIndi 
     */
    public void testfindSameIndi() {
        try {
            Gedcom gedcom = TestUtility.createGedcom();
            Indi indi = (Indi)gedcom.getEntity("sansfamille1");
            indi.getBirthDate().setValue("1 APR 2000");
            RecordBirth record = TestUtility.createBirthRecord("sansfamille1");
            record.setEventDate("01/04/2000");

            // je cree deuxième individu avec le meme nom et la meme date de naissance
            Indi sansfamille2 = (Indi) gedcom.createEntity(Gedcom.INDI);
            sansfamille2.setName(indi.getFirstName(), indi.getLastName());
            sansfamille2.setSex(PropertySex.MALE);
            Property birth = sansfamille2.addProperty("BIRT","" );
            PropertyDate birthDate = (PropertyDate) birth.addProperty("DATE","");

            // liste des individu de meme nom et meme date de naissance, mais diffrents de l'individu selectionné
            birthDate.setValue(indi.getBirthDate().getValue());
            assertEquals("otherIndi 1",1, MergeModel.findIndiWithCompatibleBirth(record, gedcom, indi).size());

            // liste des individu de meme nom et meme date de naissance
            assertEquals("otherIndi 2",1, MergeModel.findIndiWithCompatibleBirth(record, gedcom, indi).size());

            birthDate.setValue("BEF 1999");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),0, MergeModel.findIndiWithCompatibleBirth(record, gedcom, indi).size());
            birthDate.setValue("BEF 2000");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),0, MergeModel.findIndiWithCompatibleBirth(record, gedcom, indi).size());
            birthDate.setValue("BEF 28 FEB 2000");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),0, MergeModel.findIndiWithCompatibleBirth(record, gedcom, indi).size());
            birthDate.setValue("BEF 5 MAY 2000");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),1, MergeModel.findIndiWithCompatibleBirth(record, gedcom, indi).size());
            birthDate.setValue("BEF 2002");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),1, MergeModel.findIndiWithCompatibleBirth(record, gedcom, indi).size());
            birthDate.setValue("BEF 2222");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),0, MergeModel.findIndiWithCompatibleBirth(record, gedcom, indi).size());

            birthDate.setValue("BET 1990 AND 1998");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),0, MergeModel.findIndiWithCompatibleBirth(record, gedcom, indi).size());
            birthDate.setValue("BET 1990 AND 2002");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),1, MergeModel.findIndiWithCompatibleBirth(record, gedcom, indi).size());
            birthDate.setValue("BET 5 MAY 2000 AND 2002");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),0, MergeModel.findIndiWithCompatibleBirth(record, gedcom, indi).size());

            birthDate.setValue("AFT 1880");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),0, MergeModel.findIndiWithCompatibleBirth(record, gedcom, indi).size());
            birthDate.setValue("AFT 2000");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),1, MergeModel.findIndiWithCompatibleBirth(record, gedcom, indi).size());
            birthDate.setValue("AFT MAY 2000");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),0, MergeModel.findIndiWithCompatibleBirth(record, gedcom, indi).size());

            //  "@#DFRENCH R@ 25 VEND 2"

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }


    /**
     * testfindSameIndi
     */
    public void testisBestBirthDate() {
        try {
            PropertyDate recordDate = new PropertyDate();
            PropertyDate birthDate = new PropertyDate();

            recordDate.setValue("BEF 1970");
            birthDate.setValue("BEF 1972");
            assertEquals("isBestBirthDate 2",true, MergeModel.isBestBirthDate(recordDate, birthDate, null));

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

        /**
     * testfindSameIndi
     */
    public void testisBestBirthDate2() {
        try {
            PropertyDate recordDate = new PropertyDate();
            PropertyDate birthDate = new PropertyDate();

            recordDate.setValue("BEF 1793");
            birthDate.setValue("BEF 1774");
            assertEquals("isBestBirthDate 2",false, MergeModel.isBestBirthDate(recordDate, birthDate, null));

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

}
