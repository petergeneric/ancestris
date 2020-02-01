package ancestris.modules.releve.merge;

import java.util.HashMap;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Michel
 */
public class SimilarNameSetTest {

    @Test
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

    @Test
    public void testsaveloadSimilarLastName() {
        HashMap<String,String> hashmap = new HashMap<String,String>();

        hashmap.put("A1", "A");
        hashmap.put("B1", "B");

        SimilarNameSet.getSimilarLastName().save(hashmap);
        SimilarNameSet.getSimilarLastName().reset();

        assertEquals("equivalent A1", "A", SimilarNameSet.getSimilarLastName().getSimilarName("A"));
        assertEquals("equivalent B1", "B", SimilarNameSet.getSimilarLastName().getSimilarName("B"));
        assertEquals("equivalent B2", "B2", SimilarNameSet.getSimilarLastName().getSimilarName("B2"));
        assertEquals("equivalent C1", "C1", SimilarNameSet.getSimilarLastName().getSimilarName("C1"));
    }


}
