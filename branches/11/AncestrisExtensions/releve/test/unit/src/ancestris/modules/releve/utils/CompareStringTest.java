 package ancestris.modules.releve.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michel
 */


public class CompareStringTest {
    
    public CompareStringTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testCompareStringNFD() {
        
        String str1 = "Name\u0152";
        String str2 = "Name\u0152";
        int result = CompareString.compareStringNFD(str1, str2);
        assertEquals("compare "+str1, 0, result);
        
        str1 = "Name\u0152";
        str2 = "Namee";
        result = CompareString.compareStringNFD(str1, str2);
        assertEquals("compare "+str1, 238, result);
        
        str1 = "Åntöné";
        str2 = "antone";
        result = CompareString.compareStringNFD(str1, str2);
        assertEquals("compare "+str1, 0, result);                
    }

    @Test
    public void testCompareStringUTF8() {
        String str1 = "Name\u0152";
        String str2 = "Name\u0152";
        int result = CompareString.compareStringUTF8(str1, str2);
        assertEquals("compare "+str1, 0, result);
        
        str1 = "Name\u0152";
        str2 = "Namee";
        result = CompareString.compareStringUTF8(str1, str2);
        assertEquals("compare "+str1, 237, result);
        
        str1 = "Åntöné";
        str2 = "antone";
        result = CompareString.compareStringUTF8(str1, str2);
        assertEquals("compare "+str1, 0, result);
    }
    
}
