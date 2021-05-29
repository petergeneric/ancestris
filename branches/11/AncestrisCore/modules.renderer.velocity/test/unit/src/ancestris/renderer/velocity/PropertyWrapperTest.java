/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2013 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.renderer.velocity;

import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import junit.framework.TestCase;

/**
 *
 * @author daniel
 */
public class PropertyWrapperTest extends TestCase {
    
    public PropertyWrapperTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

//    /**
//     * Test of create method, of class PropertyWrapper.
//     */
//    public void testCreate() {
//        System.out.println("create");
//        Property p = null;
//        PropertyWrapper instance = null;
//        PropertyWrapper expResult = null;
//        PropertyWrapper result = instance.create(p);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
    /**
     * Test of compareTo method, of class PropertyWrapper.
     */
    public void testCompareTo() {
        PropertyWrapper o = PropertyWrapper.create(new PropertyDate(1900));
        PropertyWrapper instance = PropertyWrapper.create(new PropertyDate(1901));
        int result = instance.compareTo(o);
        assertTrue("year 1901 is after year 1900",result>0);
    }

//    /**
//     * Test of getProperty method, of class PropertyWrapper.
//     */
//    public void testGetProperty() {
//        System.out.println("getProperty");
//        String tagPath = "";
//        PropertyWrapper instance = null;
//        PropertyWrapper expResult = null;
//        PropertyWrapper result = instance.getProperty(tagPath);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of get method, of class PropertyWrapper.
//     */
//    public void testGet() {
//        System.out.println("get");
//        String tag = "";
//        PropertyWrapper instance = null;
//        Object expResult = null;
//        Object result = instance.get(tag);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getProperties method, of class PropertyWrapper.
//     */
//    public void testGetProperties() {
//        System.out.println("getProperties");
//        String tagPath = "";
//        PropertyWrapper instance = null;
//        PropertyWrapper[] expResult = null;
//        PropertyWrapper[] result = instance.getProperties(tagPath);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPath method, of class PropertyWrapper.
//     */
//    public void testGetPath() {
//        System.out.println("getPath");
//        PropertyWrapper instance = null;
//        String expResult = "";
//        String result = instance.getPath();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDate method, of class PropertyWrapper.
//     */
//    public void testGetDate() {
//        System.out.println("getDate");
//        PropertyWrapper instance = null;
//        String expResult = "";
//        String result = instance.getDate();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getValue method, of class PropertyWrapper.
//     */
//    public void testGetValue() {
//        System.out.println("getValue");
//        PropertyWrapper instance = null;
//        String expResult = "";
//        String result = instance.getValue();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getName method, of class PropertyWrapper.
//     */
//    public void testGetName() {
//        System.out.println("getName");
//        PropertyWrapper instance = null;
//        String expResult = "";
//        String result = instance.getName();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPlace method, of class PropertyWrapper.
//     */
//    public void testGetPlace() {
//        System.out.println("getPlace");
//        PropertyWrapper instance = null;
//        String expResult = "";
//        String result = instance.getPlace();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of toString method, of class PropertyWrapper.
//     */
//    public void testToString() {
//        System.out.println("toString");
//        PropertyWrapper instance = null;
//        String expResult = "";
//        String result = instance.toString();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of format method, of class PropertyWrapper.
//     */
//    public void testFormat() {
//        System.out.println("format");
//        String fmtstr = "";
//        PropertyWrapper instance = null;
//        String expResult = "";
//        String result = instance.format(fmtstr);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
