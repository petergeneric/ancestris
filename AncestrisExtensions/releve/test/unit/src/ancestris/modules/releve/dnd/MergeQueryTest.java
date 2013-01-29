package ancestris.modules.releve.dnd;

import ancestris.modules.releve.TestUtility;
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
public class MergeQueryTest extends TestCase {


    /**
     * test_isCompatible
     */
    public void test_isCompatible() {
        try {
            PropertyDate recordDate = new PropertyDate();
            PropertyDate parentBirthDate = new PropertyDate();
            boolean result;

            recordDate.setValue("CALC MAR 1970");
            parentBirthDate.setValue("12 JUN 1970");
            result = MergeQuery.isCompatible(recordDate, parentBirthDate);
            assertEquals("Dates calculee compatible", true, result);

         } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * testIsRecordBeforeThanDate
     */
    public void testIsRecordBeforeThanDate() {
        try {
            PropertyDate recordDate = new PropertyDate();
            PropertyDate parentBirthDate = new PropertyDate();
            boolean result;

            recordDate.setValue("1 FEB 1970");
            parentBirthDate.setValue("1 FEB 2000");
            result = MergeQuery.isRecordBeforeThanDate(recordDate, parentBirthDate, 0, 0);
            assertEquals("Dates exacte inferieure ", true, result);

            recordDate.setValue("1 FEB 1976");
            parentBirthDate.setValue("1 FEB 1970");
            result = MergeQuery.isRecordBeforeThanDate(recordDate, parentBirthDate, 0, 0);
            assertEquals("Date exacte superieure ", false, result);

            recordDate.setValue("1 FEB 1970");
            parentBirthDate.setValue("1 FEB 1970");
            result = MergeQuery.isRecordBeforeThanDate(recordDate, parentBirthDate, 0, 0);
            assertEquals("Date exacte egale", true, result);

            recordDate.setValue("BEF 1969");
            parentBirthDate.setValue("1 FEB 1970");
            result = MergeQuery.isRecordBeforeThanDate(recordDate, parentBirthDate, 0, 0);
            assertEquals("intervalle BEF inferieur", true, result);

            recordDate.setValue("BEF 1569");
            parentBirthDate.setValue("1 FEB 1970");
            result = MergeQuery.isRecordBeforeThanDate(recordDate, parentBirthDate, 0, 0);
            assertEquals("intervalle BEF tres inferieur", false, result);

            recordDate.setValue("BEF 1972");
            parentBirthDate.setValue("1 FEB 1970");
            result = MergeQuery.isRecordBeforeThanDate(recordDate, parentBirthDate, 0, 0);
            assertEquals("intervalle BEF superieur", true, result);

            recordDate.setValue("AFT 1972");
            parentBirthDate.setValue("1 FEB 1970");
            result = MergeQuery.isRecordBeforeThanDate(recordDate, parentBirthDate, 0, 0);
            assertEquals("intervalle AFT superieur", false, result);

            recordDate.setValue("AFT 1966");
            parentBirthDate.setValue("1 FEB 1970");
            result = MergeQuery.isRecordBeforeThanDate(recordDate, parentBirthDate, 0, 0);
            assertEquals("intervalle AFT inferieur", true, result);

            recordDate.setValue("AFT 1666");
            parentBirthDate.setValue("1 FEB 1970");
            result = MergeQuery.isRecordBeforeThanDate(recordDate, parentBirthDate, 0, 0);
            assertEquals("intervalle AFT tres inferieur", false , result);

            recordDate.setValue("BET 1969  AND 1972");
            parentBirthDate.setValue("1 FEB 1970");
            result = MergeQuery.isRecordBeforeThanDate(recordDate, parentBirthDate, 0, 0);
            assertEquals("intervalle BET ", true , result);

            recordDate.setValue("BET 1966  AND 1969");
            parentBirthDate.setValue("1 FEB 1970");
            result = MergeQuery.isRecordBeforeThanDate(recordDate, parentBirthDate, 0, 0);
            assertEquals("intervalle BET inferieur", true , result);

            recordDate.setValue("BET 1999  AND 2002");
            parentBirthDate.setValue("1 FEB 1970");
            result = MergeQuery.isRecordBeforeThanDate(recordDate, parentBirthDate, 0, 0);
            assertEquals("intervalle BET superieur", false , result);




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
            result = MergeQuery.isRecordAfterThanDate(recordDate, parentBirthDate, 0, MergeQuery.minParentYearOld);
            assertEquals("le releve est apres la naissance du pere ", true, result);

            recordDate.setValue("1 FEB 1976");
            parentBirthDate.setValue("1 FEB 1970");
            result = MergeQuery.isRecordAfterThanDate(recordDate, parentBirthDate, 0, MergeQuery.minParentYearOld);
            assertEquals("le releve est apres la naissance du pere ", false, result);

            recordDate.setValue("1 FEB 1969");
            parentBirthDate.setValue("1 FEB 1970");
            result = MergeQuery.isRecordAfterThanDate(recordDate, parentBirthDate, 0, MergeQuery.minParentYearOld);
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
            MergeRecord mergeRecord;
            List<Indi> children;

            mergeRecord = new MergeRecord(MergeModelBirthTest.createBirthRecord("sansfamille1"));
            children = MergeQuery.findSameChild(mergeRecord, gedcom, family);
            assertEquals("l'enfant n'existe pas dans la famille", 0, children.size());

            mergeRecord = new MergeRecord(MergeModelBirthTest.createBirthRecord("child1"));
            children = MergeQuery.findSameChild(mergeRecord, gedcom, family);
            assertEquals("l'enfant existe deja dans la famille", 1, children.size());
            assertEquals("Nom de l'enfant", mergeRecord.getIndiFirstName(), children.get(0).getFirstName());

            mergeRecord = new MergeRecord(MergeModelBirthTest.createBirthRecord("child1"));
            Indi father = family.getHusband();
            PropertyDate fatherBirtDate =  father.getBirthDate();
            //  l'individu est né 10 ans apres la naissance du pere
            mergeRecord.getEventDate().getStart().set(fatherBirtDate.getStart());
            mergeRecord.getEventDate().getStart().add(0, 0, 10);
            children = MergeQuery.findSameChild(mergeRecord, gedcom, family);
            assertEquals("l'individu est né 10 ans apres la naissance du pere", 0, children.size());

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
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
            MergeRecord mergeRecord = new MergeRecord(MergeModelBirthTest.createBirthRecord("sansfamille1"));


            List<Fam> fams = MergeQuery.findFamilyCompatibleWithIndiParents(mergeRecord, gedcom);
            assertEquals("l'individu existe et a deja une famille", 1, fams.size());
            assertEquals("Nom de la mere", mergeRecord.getIndiMotherLastName(), fams.get(0).getWife().getLastName());

            indi = (Indi)gedcom.getEntity("sansfamille1");
            fams = MergeQuery.findFamilyCompatibleWithIndiParents(mergeRecord, gedcom);
            assertEquals("l'individu existe mais n'avait pas de famille", 1, fams.size());
            assertEquals("Nom de la mere", mergeRecord.getIndiMotherLastName(), fams.get(0).getWife().getLastName());


            indi = (Indi)gedcom.getEntity("sansfamille1");
            Indi father = (Indi) gedcom.getEntity("I1");
            PropertyDate fatherBirtDate =  father.getBirthDate();
            // je l'individu est né 10 ans apres la naissance du pere
            mergeRecord.getEventDate().getStart().set(fatherBirtDate.getStart());
            mergeRecord.getEventDate().getStart().add(0, 0, 10);
            mergeRecord.getIndiBirthDate().setValue(PropertyDate.DATE,  mergeRecord.getEventDate().getStart(),null, "");
            fams = MergeQuery.findFamilyCompatibleWithIndiParents(mergeRecord, gedcom);
            assertEquals("l'individu existe mais n'a que 10 ans de plus que le pere", 0, fams.size());

            mergeRecord = new MergeRecord(MergeModelBirthTest.createBirthRecord("sansfamille1"));
            mergeRecord.getEventDate().setValue("1 JAN 2000");

            fatherBirtDate.setValue("BEF 1880");
            fams = MergeQuery.findFamilyCompatibleWithIndiParents(mergeRecord, gedcom);
            assertEquals("Father="+fatherBirtDate.getValue(), 0, fams.size());

            fatherBirtDate.setValue("BEF 1990");
            fams = MergeQuery.findFamilyCompatibleWithIndiParents(mergeRecord, gedcom);
            assertEquals("Father="+fatherBirtDate.getValue(), 1, fams.size());


            fatherBirtDate.setValue("BET 1980 AND 1990");
            fams = MergeQuery.findFamilyCompatibleWithIndiParents(mergeRecord, gedcom);
            assertEquals("Father="+fatherBirtDate.getValue(), 1, fams.size());

            fatherBirtDate.setValue("BET 1990 AND 2000");
            fams = MergeQuery.findFamilyCompatibleWithIndiParents(mergeRecord, gedcom);
            assertEquals("Father="+fatherBirtDate.getValue(), 0, fams.size());

            fatherBirtDate.setValue("AFT 2001");
            fams = MergeQuery.findFamilyCompatibleWithIndiParents(mergeRecord, gedcom);
            assertEquals("Father="+fatherBirtDate.getValue(), 0, fams.size());



            fatherBirtDate.setValue("EST 1870");
            fams = MergeQuery.findFamilyCompatibleWithIndiParents(mergeRecord, gedcom);
            assertEquals("Father="+fatherBirtDate.getValue(), 0, fams.size());

            fatherBirtDate.setValue("EST 1894");
            fams = MergeQuery.findFamilyCompatibleWithIndiParents(mergeRecord, gedcom);
            assertEquals("Father="+fatherBirtDate.getValue(), 0, fams.size());

            fatherBirtDate.setValue("EST 1989");
            fams = MergeQuery.findFamilyCompatibleWithIndiParents(mergeRecord, gedcom);
            assertEquals("Father="+fatherBirtDate.getValue(), 1, fams.size());

            fatherBirtDate.setValue("EST 2001");
            fams = MergeQuery.findFamilyCompatibleWithIndiParents(mergeRecord, gedcom);
            assertEquals("Father="+fatherBirtDate.getValue(), 0, fams.size());


        } catch (Exception ex) {
            ex.printStackTrace(System.err);
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
            MergeRecord mergeRecord = new MergeRecord(MergeModelBirthTest.createBirthRecord("sansfamille1"));

            mergeRecord.getEventDate().setValue("1 APR 2000");

            // je cree deuxième individu avec le meme nom et la meme date de naissance
            Indi sansfamille2 = (Indi) gedcom.createEntity(Gedcom.INDI);
            sansfamille2.setName(indi.getFirstName(), indi.getLastName());
            sansfamille2.setSex(PropertySex.MALE);
            Property birth = sansfamille2.addProperty("BIRT","" );
            PropertyDate birthDate = (PropertyDate) birth.addProperty("DATE","");

            // liste des individu de meme nom et meme date de naissance, mais diffrents de l'individu selectionné
            birthDate.setValue(indi.getBirthDate().getValue());
            assertEquals("otherIndi 1",1, MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, indi).size());

            // liste des individu de meme nom et meme date de naissance
            assertEquals("otherIndi 2",1, MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, indi).size());

            birthDate.setValue("BEF 1999");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),0, MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, indi).size());
            birthDate.setValue("BEF 2000");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),0, MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, indi).size());
            birthDate.setValue("BEF 28 FEB 2000");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),0, MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, indi).size());
            birthDate.setValue("BEF 5 MAY 2000");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),1, MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, indi).size());
            birthDate.setValue("BEF 2002");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),1, MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, indi).size());
            birthDate.setValue("BEF 2222");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),0, MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, indi).size());

            birthDate.setValue("BET 1990 AND 1998");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),0, MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, indi).size());
            birthDate.setValue("BET 1990 AND 2002");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),1, MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, indi).size());
            birthDate.setValue("BET 5 MAY 2000 AND 2002");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),0, MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, indi).size());

            birthDate.setValue("AFT 1880");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),0, MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, indi).size());
            birthDate.setValue("AFT 2000");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),1, MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, indi).size());
            birthDate.setValue("AFT MAY 2000");
            assertEquals("record=1/4/2000 indi="+birthDate.getValue(),0, MergeQuery.findIndiCompatibleWithRecord(mergeRecord, gedcom, indi).size());

            //  "@#DFRENCH R@ 25 VEND 2"

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }


    /**
     * testfindSameIndi
     */
    public void testIsBestBirthDate() {
        try {
            PropertyDate recordDate = new PropertyDate();
            PropertyDate birthDate = new PropertyDate();

            recordDate.setValue("BEF 1970");
            birthDate.setValue("BEF 1972");
            assertEquals("Date du releve BEF plus precises",recordDate, MergeQuery.getMostAccurateDate(recordDate, birthDate) );

            recordDate.setValue("BEF 1793");
            birthDate.setValue("BEF 1770");
            assertEquals("Date du releve BEF moins precises",birthDate, MergeQuery.getMostAccurateDate(recordDate, birthDate) );

            recordDate.setValue("AFT 1970");
            birthDate.setValue("AFT 1972");
            assertEquals("Date du releve AFT moins precises",birthDate, MergeQuery.getMostAccurateDate(recordDate, birthDate) );

            recordDate.setValue("AFT 1972");
            birthDate.setValue("AFT 1970");
            assertEquals("Date du releve AFT plus precises",recordDate, MergeQuery.getMostAccurateDate(recordDate, birthDate) );

            recordDate.setValue("AFT 1770");
            birthDate.setValue("BEF 1774");
            assertEquals("Date du releve entre AFT et BEF","BET 1770 AND 1774", MergeQuery.getMostAccurateDate(recordDate, birthDate).getValue() );

            recordDate.setValue("AFT 1774");
            birthDate.setValue("BEF 1770");
            assertEquals("Date du releve incompatible",null, MergeQuery.getMostAccurateDate(recordDate, birthDate) );

            recordDate.setValue("BEF 1774");
            birthDate.setValue("AFT 1770");
            assertEquals("Date du releve entre AFT et BEF","BET 1770 AND 1774", MergeQuery.getMostAccurateDate(recordDate, birthDate).getValue() );

            recordDate.setValue("BEF 1770");
            birthDate.setValue("AFT 1774");
            assertEquals("Date du releve incompatible",null, MergeQuery.getMostAccurateDate(recordDate, birthDate) );

            recordDate.setValue("1 FEB 1980");
            birthDate.setValue("2 MAR 1980");
            assertEquals("Date precises incompatibles",null, MergeQuery.getMostAccurateDate(recordDate, birthDate) );

            recordDate.setValue("1 FEB 1980");
            birthDate.setValue("1980");
            assertEquals("Date du releve plus precises",recordDate, MergeQuery.getMostAccurateDate(recordDate, birthDate) );

            recordDate.setValue("1980");
            birthDate.setValue("1 FEB 1980");
            assertEquals("Date du releve incomplete moins precise",birthDate, MergeQuery.getMostAccurateDate(recordDate, birthDate) );

            recordDate.setValue(" FEB 1980");
            birthDate.setValue("1 FEB 1980");
            assertEquals("Date du releve incomplete moins precise",birthDate, MergeQuery.getMostAccurateDate(recordDate, birthDate) );

            recordDate.setValue("22 FEB 1980");
            birthDate.setValue(" FEB 1980");
            assertEquals("Date du releve plus precise",recordDate, MergeQuery.getMostAccurateDate(recordDate, birthDate) );

            recordDate.setValue("22 FEB 1980");
            birthDate.setValue(" MAR 1980");
            assertEquals("Date du releve plus precise",recordDate, MergeQuery.getMostAccurateDate(recordDate, birthDate) );

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    public void testIsSameName() {

        assertFalse(MergeQuery.isSameName("VON DER PFALZ-SIMMERN", "VENTRÉ"));
        assertTrue(MergeQuery.isSameName("AGUILHÉ", "AGUILLÉ"));
        assertTrue(MergeQuery.isSameName("VENTRE", "VENTRÉ"));

    }
 
}
