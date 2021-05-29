package ancestris.modules.releve.merge;

import ancestris.modules.releve.TestUtility;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author michel
 */


public class ProposalHelperTest {

    public ProposalHelperTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetEventTag() {
    }

    @Test
    public void testCopyAssociation() throws Exception {
        Gedcom gedcom = TestUtility.createGedcomF2();
        MergeRecord mergeRecord = TestUtility.createMergeRecord( TestUtility.getRecordMiscOtherF2());
        ProposalHelper m_helper = new ProposalHelper(mergeRecord, null, gedcom);
        Proposal proposal1 = new Proposal(m_helper,
            m_helper.getRecord().getParticipant(MergeRecord.MergeParticipantType.participant1),
            (Indi) gedcom.getEntity(Gedcom.INDI, "IF2husband"),
            null, SpouseTag.HUSB,
            null,
            null,
            null
        );
        Proposal proposal2 = new Proposal(m_helper,
            m_helper.getRecord().getParticipant(MergeRecord.MergeParticipantType.participant1),
            null,
            null, SpouseTag.HUSB,
            null,
            null,
            null
        );
        proposal1.copyRecordToEntity();
        proposal2.copyRecordToEntity();
        Property property1 = proposal1.getMainEvent();
        Entity   entity2 = proposal2.getMainEntity();
        assertNotNull( property1 );
        assertNotNull( entity2 );

        m_helper.copyAssociation(property1, entity2);

        Property link = proposal1.getMainEntity().getPropertyByPath("INDI:EVEN:XREF");
        assertEquals("participant1 association vers participant2","@"+entity2.getId()+"@", link.getValue() );
    }



}
