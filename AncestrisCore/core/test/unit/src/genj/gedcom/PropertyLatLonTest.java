/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.gedcom;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author daniel
 */
public class PropertyLatLonTest {
    
    public PropertyLatLonTest() {
    }

    /**
     * Test for setvalue -> getvalue give same result.
     */
    @Test
    public void testLatSameValue() {
        System.out.println("Latitude set->get : same value");
        PropertyLatitude lat = new PropertyLatitude(true);
        
        lat.setValue("N12.34");
        assertEquals("N12.34", lat.getValue());
        
        lat.setValue("S12.34");
        assertEquals("S12.34", lat.getValue());
    }

    /**
     * Test for setvalue -> getvalue give same result.
     */
    @Test
    public void testLonSameValue() {
        System.out.println("Longitudeset->get : same value");
        PropertyLongitude lon = new PropertyLongitude(false);
        
        lon.setValue("W12.34");
        assertEquals("W12.34", lon.getValue());
        
        lon.setValue("E12.34");
        assertEquals("E12.34", lon.getValue());
    }
    
    @Test
    public void testValid(){
        System.out.println("Longitude E50.65 valid");

        PropertyLongitude lon = new PropertyLongitude(false);
        lon.setValue("E50.65");
        assertEquals("E50.65", lon.getValue());
        assertTrue(lon.isValid());
        
        PropertyLatitude lat = new PropertyLatitude(true);
        lat.setValue("N50.65");
        assertEquals("N50.65", lat.getValue());
        assertTrue(lat.isValid());

        lat.setValue("E50.65");
        assertFalse(lat.isValid());
        assertEquals("E50.65", lat.getValue());
    }
}
