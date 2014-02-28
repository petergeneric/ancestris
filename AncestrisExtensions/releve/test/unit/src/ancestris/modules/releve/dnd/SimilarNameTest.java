package ancestris.modules.releve.dnd;

import java.util.HashMap;
import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class SimilarNameTest extends TestCase {
    
    public SimilarNameTest(String testName) {
        super(testName);
    }

    public void testsaveloadSimilarFirstName() {
        HashMap<String,String> hashmap = new HashMap<String,String>();

        hashmap.put("A1", "A");
        hashmap.put("B1", "B");

        SimilarNameSet.getSimilarFirstName().save(hashmap);
        SimilarNameSet.getSimilarFirstName().reset();

        assertEquals("equivalent A1", "A", SimilarNameSet.getSimilarFirstName().getSimilarName("A"));
        assertEquals("equivalent B1", "B", SimilarNameSet.getSimilarFirstName().getSimilarName("B"));
        assertEquals("equivalent B2", "B2", SimilarNameSet.getSimilarFirstName().getSimilarName("B2"));
        assertEquals("equivalent C1", "C1", SimilarNameSet.getSimilarFirstName().getSimilarName("C1"));
    }

}
