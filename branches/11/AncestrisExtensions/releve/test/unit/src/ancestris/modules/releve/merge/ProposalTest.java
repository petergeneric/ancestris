package ancestris.modules.releve.merge;

import ancestris.modules.releve.IgnoreOtherTestMethod;
import ancestris.modules.releve.RecordTransferHandle;
import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.dnd.TransferableRecord;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.RecordInfoPlace;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author michel
 */


public final class ProposalTest {
    @Rule
    public IgnoreOtherTestMethod rule = new IgnoreOtherTestMethod("");

    ProposalHelper createProposalHelper(Record record, Gedcom gedcom ) throws Exception {
        RecordInfoPlace infoPlace = TestUtility.getRecordsInfoPlace();
        String fileName = "";
        TransferableRecord.TransferableData data = RecordTransferHandle.createTransferableData(null, infoPlace, fileName, record);
        MergeRecord mergeRecord = new MergeRecord(data);
        return new ProposalHelper(mergeRecord, null, gedcom);
    }


    public ProposalTest() throws Exception {

    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() {

    }


    ////////////////////////////////////////////////////////////////////////////
    // testEqualAs
    ////////////////////////////////////////////////////////////////////////////
    @Test
    public void testEqualAs_birth1() throws Exception {
        Gedcom gedcom = TestUtility.createGedcomEmpty();
        ProposalHelper m_helper = createProposalHelper( TestUtility.getRecordBirth(), gedcom);
        Proposal proposal1 = new Proposal(m_helper,
                (Indi) gedcom.createEntity(Gedcom.INDI, "child1"),
                null,
                null,
                null);
        Proposal proposal2 = new Proposal(m_helper,
                (Indi) gedcom.getEntity(Gedcom.INDI, "child1"),
                null,
                null,
                null);

        assertEquals(true,proposal1.equalAs(proposal2) );
    }

    @Test
    public void testEqualAs_birth2() throws Exception {
        Gedcom gedcom = TestUtility.createGedcomEmpty();
        ProposalHelper m_helper = createProposalHelper( TestUtility.getRecordBirth(), gedcom);
        Proposal proposal1 = new Proposal(m_helper,
                (Indi) gedcom.createEntity(Gedcom.INDI, "child1"),
                (Fam)  gedcom.createEntity(Gedcom.FAM, "parentFamily"),
                null,
                null);
        Proposal proposal2 = new Proposal(m_helper,
                (Indi) gedcom.getEntity(Gedcom.INDI, "child1"),
                (Fam)  gedcom.getEntity(Gedcom.FAM, "parentFamily"),
                null,
                null);
        assertEquals(true,proposal1.equalAs(proposal2) );
    }

    @Test
    public void testEqualAs_death1() throws Exception {
        Gedcom gedcom = TestUtility.createGedcomEmpty();
        ProposalHelper m_helper = createProposalHelper( TestUtility.getRecordDeath(), gedcom);
        Proposal proposal1 = new Proposal(m_helper,
            (Indi) gedcom.createEntity(Gedcom.INDI, "indi"),
            (Fam)  gedcom.createEntity(Gedcom.FAM, "parentFamily"),
            (Indi) gedcom.createEntity(Gedcom.INDI, "father"),
            (Indi) gedcom.createEntity(Gedcom.INDI, "mother")
        );
        Proposal proposal2 = new Proposal(m_helper,
            (Indi) gedcom.getEntity(Gedcom.INDI, "indi"),
            (Fam)  gedcom.getEntity(Gedcom.FAM, "parentFamily"),
            (Indi) gedcom.getEntity(Gedcom.INDI, "father"),
            (Indi) gedcom.getEntity(Gedcom.INDI, "mother")
        );
        assertEquals(true,proposal1.equalAs(proposal2) );
    }

    @Test
    public void testEqualAs_death2() throws Exception {
        Gedcom gedcom = TestUtility.createGedcomEmpty();
        ProposalHelper m_helper = createProposalHelper( TestUtility.getRecordDeath(), gedcom);
        Proposal proposal1 = new Proposal(m_helper,
             m_helper.getRecord().getParticipant(MergeRecord.MergeParticipantType.participant1),
            (Indi) gedcom.createEntity(Gedcom.INDI, "indi"),
            (Fam) gedcom.createEntity(Gedcom.FAM, "marriedFamily"),
             SpouseTag.HUSB,
            (Fam)  gedcom.createEntity(Gedcom.FAM, "parentFamily"),
            (Indi) gedcom.createEntity(Gedcom.INDI, "father"),
            (Indi) gedcom.createEntity(Gedcom.INDI, "mother")
        );

        Proposal proposal2 = new Proposal(m_helper,
            (Indi) gedcom.getEntity(Gedcom.INDI, "indi"),
            (Fam)  gedcom.getEntity(Gedcom.FAM, "marriedFamily"),
            SpouseTag.HUSB,
            (Fam)  gedcom.getEntity(Gedcom.FAM, "parentFamily"),
            (Indi) gedcom.getEntity(Gedcom.INDI, "father"),
            (Indi) gedcom.getEntity(Gedcom.INDI, "mother")
        );
        assertEquals(true,proposal1.equalAs(proposal2) );
    }

     @Test
    public void testEqualAs_marriage0() throws Exception {
        Gedcom gedcom = TestUtility.createGedcomEmpty();
        ProposalHelper m_helper = createProposalHelper( TestUtility.getRecordMarriage(), gedcom);
         Proposal proposal1 = new Proposal(m_helper,
            (Fam) gedcom.createEntity(Gedcom.FAM, "family"),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
         );
        Proposal proposal2 = new Proposal(m_helper,
            (Fam) gedcom.getEntity(Gedcom.FAM, "family"),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
        assertEquals(true,proposal1.equalAs(proposal2) );
    }

    @Test
    public void testEqualAs_marriage1() throws Exception {
        Gedcom gedcom = TestUtility.createGedcomEmpty();
        ProposalHelper m_helper = createProposalHelper( TestUtility.getRecordMarriage(), gedcom);
        Proposal proposal1 = new Proposal(m_helper,
            (Fam) gedcom.createEntity(Gedcom.FAM, "family"),

            (Indi) gedcom.createEntity(Gedcom.INDI, "husband"),
            (Fam)  gedcom.createEntity(Gedcom.FAM, "husbandParentFamily"),
            (Indi) gedcom.createEntity(Gedcom.INDI, "husbandFather"),
            (Indi) gedcom.createEntity(Gedcom.INDI, "husbandMother"),

            (Indi) gedcom.createEntity(Gedcom.INDI, "wife"),
            (Fam)  gedcom.createEntity(Gedcom.FAM, "wifeParentFamily"),
            (Indi) gedcom.createEntity(Gedcom.INDI, "wifeFather"),
            (Indi) gedcom.createEntity(Gedcom.INDI, "wifeMother")
        );
        Proposal proposal2 = new Proposal(m_helper,
            (Fam) gedcom.getEntity(Gedcom.FAM, "family"),

            (Indi) gedcom.getEntity(Gedcom.INDI, "husband"),
            (Fam)  gedcom.getEntity(Gedcom.FAM, "husbandParentFamily"),
            (Indi) gedcom.getEntity(Gedcom.INDI, "husbandFather"),
            (Indi) gedcom.getEntity(Gedcom.INDI, "husbandMother"),

            (Indi) gedcom.getEntity(Gedcom.INDI, "wife"),
            (Fam)  gedcom.getEntity(Gedcom.FAM, "wifeParentFamily"),
            (Indi) gedcom.getEntity(Gedcom.INDI, "wifeFather"),
            (Indi) gedcom.getEntity(Gedcom.INDI, "wifeMother")
        );
        assertEquals(true,proposal1.equalAs(proposal2) );
    }

    @Test
    public void testEqualAs_marriage2() throws Exception {
        Gedcom gedcom = TestUtility.createGedcomEmpty();
        ProposalHelper m_helper = createProposalHelper( TestUtility.getRecordMarriage(), gedcom);
        Proposal proposal1 = new Proposal(m_helper,
            (Fam)  gedcom.createEntity(Gedcom.FAM, "F1"),

            (Indi) gedcom.createEntity(Gedcom.INDI, "I1"),
            (Fam)  gedcom.createEntity(Gedcom.FAM, "husbandParentFamily"),
            (Indi) gedcom.createEntity(Gedcom.INDI, "husbandFather"),
            (Indi) gedcom.createEntity(Gedcom.INDI, "husbandMother"),

            (Indi) gedcom.createEntity(Gedcom.INDI, "Wife2"),
            (Fam)  gedcom.createEntity(Gedcom.FAM, "wifeParentFamily"),
            (Indi) gedcom.createEntity(Gedcom.INDI, "wifeFather"),
            (Indi) gedcom.createEntity(Gedcom.INDI, "wifeMother")
        );
        Proposal proposal2 = new Proposal(m_helper,
            (Fam)  gedcom.getEntity(Gedcom.FAM, "F1"),

            (Indi) gedcom.getEntity(Gedcom.INDI, "I1"),
            (Fam)  gedcom.getEntity(Gedcom.FAM, "husbandParentFamily"),
            (Indi) gedcom.getEntity(Gedcom.INDI, "husbandFather"),
            (Indi) gedcom.getEntity(Gedcom.INDI, "husbandMother"),

            (Indi) gedcom.getEntity(Gedcom.INDI,"Wife2"),
            (Fam)  gedcom.getEntity(Gedcom.FAM, "wifeParentFamily"),
            (Indi) gedcom.getEntity(Gedcom.INDI, "wifeFather"),
            (Indi) gedcom.createEntity(Gedcom.INDI, "MOTHER")
        );
        assertEquals(false, proposal1.equalAs(proposal2) );
    }

    ////////////////////////////////////////////////////////////////////////////
    // testGetMainEntity
    ////////////////////////////////////////////////////////////////////////////
    @Test
    public void testGetMainEntity() throws Exception {
        Gedcom gedcom = TestUtility.createGedcomF2();
        ProposalHelper m_helper = createProposalHelper( TestUtility.getRecordBirthF2(), gedcom);
        Proposal proposal1 = new Proposal(m_helper,
            (Indi) gedcom.getEntity(Gedcom.INDI, "IF2child1"),
            null,
            null,
            null
        );

        assertEquals(gedcom.getEntity(Gedcom.INDI, "IF2child1"), proposal1.getMainEntity());
    }

    @Test
    public void testMainEvent() throws Exception {
        Gedcom gedcom = TestUtility.createGedcomF2();
        ProposalHelper m_helper = createProposalHelper( TestUtility.getRecordBirthF2(), gedcom);
        Proposal proposal1 = new Proposal(m_helper,
            (Indi) gedcom.getEntity(Gedcom.INDI, "IF2child1"),
            null,
            null,
            null
        );
        proposal1.copyRecordToEntity();
        Property event = gedcom.getEntity(Gedcom.INDI, "IF2child1").getPropertyByPath("INDI:BIRT");
        assertNotNull(event);
        assertNotNull(proposal1.getMainEvent());
        assertEquals(event, proposal1.getMainEvent());
    }

    ////////////////////////////////////////////////////////////////////////////
    // testGetSumary
    ////////////////////////////////////////////////////////////////////////////
    @Test
    public void testGetSumary_birth() throws Exception {
        Gedcom gedcom = TestUtility.createGedcomF2();
        ProposalHelper m_helper = createProposalHelper( TestUtility.getRecordBirthF2(), gedcom);
        Proposal proposal1 = new Proposal(m_helper,
            (Indi) gedcom.getEntity(Gedcom.INDI, "IF2child1"),
            null,
            null,
            null
        );
        assertEquals("Mercure PLANET °1980 (IF2child1), Parents: Nouveau père x Nouvelle mère", proposal1.getSummary(false));
    }

     ////////////////////////////////////////////////////////////////////////////
    // testGetSumary
    ////////////////////////////////////////////////////////////////////////////
    @Test
    public void testGetSumary_Html_birth() throws Exception {
        Gedcom gedcom = TestUtility.createGedcomF2();
        ProposalHelper m_helper = createProposalHelper( TestUtility.getRecordBirthF2(), gedcom);
        Proposal proposal1 = new Proposal(m_helper,
            (Indi) gedcom.getEntity(Gedcom.INDI, "IF2child1"),
            null,
            null,
            null
        );
        assertEquals(true, proposal1.getSummary(true).contains("<ul><li>Mercure PLANET °1980 (IF2child1)</li><li>Parents: Nouveau père x Nouvelle mère</li></ul>"));
    }

    @Test
    public void testGetSummary_empty_birthdate() throws Exception {
        Gedcom gedcom = TestUtility.createGedcomEmpty();
        Indi child1 = (Indi) gedcom.createEntity(Gedcom.INDI, "IF2child1");
        child1.setName("Mercure", "PLANET");
        child1.setSex(PropertySex.MALE);
        Property birth = child1.addProperty("BIRT", "");
        birth.addProperty("DATE", "");
        ProposalHelper m_helper = createProposalHelper( TestUtility.getRecordBirthF2(), gedcom);
        Proposal proposal1 = new Proposal(m_helper,
            (Indi) gedcom.getEntity(Gedcom.INDI, "IF2child1"),
            null,
            null,
            null
        );
        assertEquals("Mercure PLANET (IF2child1), Parents: Nouveau père x Nouvelle mère", proposal1.getSummary(false));
    }


    ////////////////////////////////////////////////////////////////////////////
    // testSourceUpdated
    ////////////////////////////////////////////////////////////////////////////
    @Test
    public void testSourceUpdated_birth() throws Exception {
        Gedcom gedcom = TestUtility.createGedcom();
        ProposalHelper m_helper = createProposalHelper(TestUtility.getRecordBirth(), gedcom);
        Proposal proposal1 = new Proposal(m_helper,
                null,
                null,
                null,
                null
        );
        // je change la source
        proposal1.sourceUpdated((Source) gedcom.getEntity(Gedcom.SOUR, "S3"));
        proposal1.copyRecordToEntity();
        Property[] sourceProperties = proposal1.getMainEvent().getProperties("SOUR");
        assertEquals("Nb sources ", 1, sourceProperties.length);
        assertEquals("Source id", "@S3@", sourceProperties[0].getValue());

    }

    @Test
    public void testSourceUpdated_birth_add_second_source() throws Exception {
        Gedcom gedcom = TestUtility.createGedcomF2();
        ProposalHelper m_helper = createProposalHelper(TestUtility.getRecordBirthF2(), gedcom);
        Proposal proposal1 = new Proposal(m_helper,
                (Indi) gedcom.getEntity(Gedcom.INDI, "IF2child1"),
                null,
                null,
                null
        );
        // je verifie que la naissance a deja une source
        assertEquals("Initial soruce", "@S2@", proposal1.getMainEntity().getValue(new TagPath("INDI:BIRT:SOUR"),null) );
        // je change la source
        proposal1.sourceUpdated((Source) gedcom.getEntity(Gedcom.SOUR, "S1"));
        proposal1.copyRecordToEntity();
        Property[] sourceProperties = proposal1.getMainEvent().getProperties("SOUR");
        assertEquals("Nb sources ", 2, sourceProperties.length);
        assertEquals("Source id", "@S2@", sourceProperties[0].getValue());
        assertEquals("Source id", "@S1@", sourceProperties[1].getValue());

    }

}
