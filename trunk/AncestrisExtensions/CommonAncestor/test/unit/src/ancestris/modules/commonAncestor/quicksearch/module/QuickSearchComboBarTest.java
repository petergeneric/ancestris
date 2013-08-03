
package ancestris.modules.commonAncestor.quicksearch.module;

import junit.framework.TestCase;

/**
 *
 * @author Michel
 */
public class QuickSearchComboBarTest extends TestCase {
    
    public QuickSearchComboBarTest(String testName) {
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

    public void testCreateCombobar() {
        // TODO review the generated test code and remove the default call to fail.
        AbstractQuickSearchComboBar comboBar = new QuickSearchComboBar(
                "QuickSearchIndividu1", "displayname", 
                null, null, 
                javax.swing.KeyStroke.getKeyStroke("F6"), 
                7, 30, QuickSearchPopup.WidthMode.FIXED);
        //comboBar.displayer.explicitlyInvoked();
        comboBar.evaluateCategory(null, false);
    }

    
}
