package ancestris.modules.commonAncestor;

import genj.gedcom.Context;
import genj.gedcom.GedcomException;
import genj.util.Registry;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.Set;
import javax.swing.JComboBox;

import junit.framework.TestCase;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author michel
 */
public class SamePanelTest extends TestCase {

    String savedFileType;
    Registry registry;

    public SamePanelTest(String testName) {
        super(testName);

        registry = new Registry(Registry.get(SamePanel.class), SamePanel.class.getName());
        savedFileType = registry.get(SamePanel.DEFAULT_FILE_TYPE_NAME, "");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (!"".equals(savedFileType)) {
            registry.put(SamePanel.DEFAULT_FILE_TYPE_NAME, savedFileType);
        }

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of setContext method, of class SamePanel.
     */
    public void testConstructorContextValid() {
        try {
            System.out.println("testConstructorContextValid");
            Context context = TestUtility.createContext();
            TopComponent mainFrame = new TopComponent();
            SamePanel samePanel = new SamePanel();
            samePanel.init(context);
            mainFrame.setLayout(new BorderLayout());
            mainFrame.add(samePanel, BorderLayout.CENTER);
            mainFrame.open();  
            mainFrame.requestActive();
            mainFrame.validate();
            assertEquals(true,samePanel.isVisible());
        } catch (GedcomException ex) {
            fail(ex.toString());
        }
    }
    
    /**
     * Test of setContext method, of class SamePanel.
     */
    public void testConstructorContextNull() {
            System.out.println("testConstructorContextNull");
            Context context = null;
            TopComponent mainFrame = new TopComponent();
            SamePanel samePanel = new SamePanel();
            samePanel.init(context);
            mainFrame.setLayout(new BorderLayout());
            mainFrame.add(samePanel, BorderLayout.CENTER);
            mainFrame.open();  
            mainFrame.requestActive();
            mainFrame.validate();
            assertEquals(true,samePanel.isVisible());
        
    }

    public void testConstructorFileTypeNameNoDefault() {
        try {
            System.out.println("testConstructorFileTypeNameNoDefault");
            Context context = TestUtility.createContext();
            registry.remove(SamePanel.DEFAULT_FILE_TYPE_NAME);

            SamePanel samePanel = new SamePanel();
            samePanel.init(context);
            Component component = getComponentByName(samePanel, "jComboBoxFileType");
            if (component != null) {
                String fileType = ((JComboBox) component).getSelectedItem().toString();
                assertEquals("pdf", fileType);
            } else {
                fail("jComboBoxFileType not found");
            }
        } catch (GedcomException ex) {
            fail(ex.toString());
        }
    }

    public void testConstructorFileTypeNameSvg() {
        try {
            System.out.println("testConstructorFileTypeNameSvg");
            Context context = TestUtility.createContext();
            registry.put(SamePanel.DEFAULT_FILE_TYPE_NAME, "svg");

            SamePanel samePanel = new SamePanel();
            samePanel.init(context);
            Component component = getComponentByName(samePanel, "jComboBoxFileType");
            if (component != null) {
                String fileType = ((JComboBox) component).getSelectedItem().toString();
                assertEquals("svg", fileType);
            } else {
                fail("jComboBoxFileType not found");
            }
        } catch (GedcomException ex) {
            fail(ex.toString());
        }
    }

    private Component getComponentByName(Component parent, String name) {

        if (parent.getName() != null && parent.getName().equals(name)) {
            return parent;
        }

        if (parent instanceof Container) {
            Component[] components = ((Container) parent).getComponents();
            for (int i = 0; i < components.length; i++) {
                Component child = getComponentByName(components[i], name);
                if (child != null) {
                    return child;
                }
            }
        }
        return null;
    }

    public void testListModes() {
            System.out.println("testListModes");
            Set<? extends Mode> modes = WindowManager.getDefault().getModes();
            for(Mode mode : modes ) {
                System.out.println("mode "+ mode.toString());
                System.out.println("   "+mode.getDisplayName());
                System.out.println("   "+mode.getName());
            }
            
            
    }
     
}
