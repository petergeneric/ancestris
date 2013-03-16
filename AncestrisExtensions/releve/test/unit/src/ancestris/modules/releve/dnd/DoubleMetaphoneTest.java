package ancestris.modules.releve.dnd;

import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class DoubleMetaphoneTest extends TestCase {



    public void testIsSameName_S() {

        assertTrue(MergeQuery.isSameLastName("BARTHE", "BARTHES"));

    }
    
}
