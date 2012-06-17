package ancestris.modules.releve.dnd;

import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class DoubleMetaphoneTest extends TestCase {



    public void testIsSameName_S() {

        assertTrue(MergeQuery.isSameName("BARTHE", "BARTHES"));

    }

    public void testIsSameName() {

        assertFalse(MergeQuery.isSameName("VON DER PFALZ-SIMMERN", "VENTRÉ"));
        assertTrue(MergeQuery.isSameName("AGUILHÉ", "AGUILLÉ"));
        assertTrue(MergeQuery.isSameName("VENTRE", "VENTRÉ"));

    }
 
}
