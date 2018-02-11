package ancestris.modules.releve.merge;

import ancestris.modules.releve.merge.MergeQuery;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author Michel
 */
public class DoubleMetaphoneTest extends TestCase {



    @Test
    public void testIsSameName_S() {

        assertTrue(MergeQuery.isSameLastName("BARTHE", "BARTHES"));

    }

    @Test
    public void testIsSameName_E_Accent() {

        assertTrue(MergeQuery.isSameLastName("VERGE", "VERGÉ"));

    }

}
