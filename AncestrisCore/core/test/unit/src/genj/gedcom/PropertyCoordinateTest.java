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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author daniel
 */
public class PropertyCoordinateTest {
    
    PropertyCoordinate propCoord;
    
    public PropertyCoordinateTest() {
    }
    
    @Before
    public void setUp() {
        propCoord = new PropertyCoordinateImpl("_COORD");
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of isValid method, of class PropertyCoordinate.
     */
    @Test
    public void testIsValid() {
        PropertyCoordinate instance = new PropertyCoordinateImpl("_COORD");
        instance.setValue("N12.345");
        assertTrue("N12.345 form", instance.isValid());
        instance.setValue("W12.345");
        assertFalse("W12.345 form", instance.isValid());
    }

    /**
     * Test of getValue method, of class PropertyCoordinate.
     * Values are always returnedas gedcom coordinates
     */
    @Test
    public void testGetValue() {
        PropertyCoordinate instance = new PropertyCoordinateImpl("_COORD");
        instance.setValue("N12.345");
        assertEquals("N12.345 form", "N12.345", instance.getValue());
        instance.setValue("S12.345");
        assertEquals("S12.345 form", "S12.345", instance.getValue());
        instance.setValue("12°23'54\"");
        assertEquals("12°23'54\" form", "N12.3983", instance.getValue());
        instance.setValue("12°23'54\"N");
        assertEquals("12°23'54\"N form", "N12.3983", instance.getValue());
        instance.setValue("12°23'54\"S");
        assertEquals("12°23'54\"S form", "S12.3983", instance.getValue());
    }

    /**
     * Test of setValue method, of class PropertyCoordinate.
     */
    @Test
    public void testSetValue() {
        PropertyCoordinate instance = new PropertyCoordinateImpl("_COORD");
        instance.setValue("N12.345");
        assertEquals("N12.345 form", 12.345, instance.getDoubleValue(), 0);
        instance.setValue("S12.345");
        assertEquals("S12.345 form", -12.345, instance.getDoubleValue(), 0);
        instance.setValue("12°23'54\"");
        assertEquals("12°23'54\" form", 12.0+23.0/60+54.0/3600, instance.getDoubleValue(), 0.00001);
        instance.setValue("12°23'54\"N");
        assertEquals("12°23'54\"N form", 12.0+23.0/60+54.0/3600, instance.getDoubleValue(), 0.00001);
        instance.setValue("12°23'54\"S");
        assertEquals("12°23'54\"S form", -(12.0+23.0/60+54.0/3600), instance.getDoubleValue(), 0.00001);
    }

    public class PropertyCoordinateImpl extends PropertyCoordinate {

        public PropertyCoordinateImpl(String tag) {
            super(tag);
        }

        @Override
        public char getDirection(double coordinate) {
            return coordinate<0?'S':'N';
        }
    }
    
}
