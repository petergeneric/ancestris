package ancestris.modules.releve.merge;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Michel
 */
public class DoubleMetaphoneTest {



    @Test
    public void testIsSameName_S() {

        assertTrue(MergeQuery.isSameLastName("BARTHE", "BARTHES"));

    }

    @Test
    public void testIsSameName_E_Accent() {

        assertTrue(MergeQuery.isSameLastName("VERGE", "VERGÉ"));

    }

}
