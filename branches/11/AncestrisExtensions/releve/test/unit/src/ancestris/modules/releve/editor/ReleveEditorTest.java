package ancestris.modules.releve.editor;

import ancestris.modules.releve.MenuCommandProvider;
import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.PlaceFormatModel;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.Record.FieldType;
import genj.gedcom.GedcomOptions;
import genj.util.swing.DateWidget;
import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 *
 * @author michel
 */


public class ReleveEditorTest {
    DataManager m_dataManager = null;
    //MenuCommandProvider menuCommandeProvider = new MenuCommandProviderImpl();
    protected static JFrameOperator mainFrame  ;    
    private JFrame m_frame;
    private ReleveEditor m_releveEditor;
    JFrameOperator m_jfo; 
    
    public ReleveEditorTest() {
    }
    
    @Before
    public void setUp() {
        GedcomOptions.getInstance().setUseSpacedPlaces(false);
        PlaceFormatModel.getCurrentModel().savePreferences(1,2,3,4,5,6);
        m_dataManager = new DataManager();
        m_dataManager.setPlace(",Paris,,Paris,,,");
        m_releveEditor = new ReleveEditor();
        m_releveEditor.initModel(m_dataManager, new MenuCommandProviderImpl());
        
        m_frame = new JFrame();
        m_frame.setVisible(true);
        m_frame.setLocationRelativeTo(null);
        m_frame.add(m_releveEditor);
        m_frame.pack();
        
        m_jfo = new JFrameOperator(m_frame);         
        
    }
    
    @After
    public void tearDown() {
        m_dataManager = null; 
        
        m_frame.setVisible(false);
        m_frame.dispose();
        m_frame = null;
    }

    @Test
    public void testInitModel() throws Exception {
        m_dataManager.addRecord(TestUtility.getRecordBirth());
        m_releveEditor.selectRecord(0);
        
        
        // je verifie que "place" est affiché dans placeLabel
        JLabelOperator labelOperator = new JLabelOperator(m_jfo , m_dataManager.getPlace().getValue());
        assertEquals( "placeLabel", m_dataManager.getPlace().getValue(), labelOperator.getText());
    }
    
    @Test
    public void copyIndiNameToIndiFatherNameTest() {
        Record record = TestUtility.getRecordBirth();
        record.setFieldValue(FieldType.indiFatherLastName, "xxx");
        m_dataManager.addRecord(record);
        m_releveEditor.selectRecord(0);

        ComponentOperator indiLastNameOperator  = new ComponentOperator(m_jfo , new NameComponentChooser(FieldType.indiLastName.name()));
        ContainerOperator<Bean> indiFatherLastNameOperator = new ContainerOperator<Bean>(m_jfo , new NameComponentChooser(FieldType.indiFatherLastName.name()));
        assertEquals( "indiLastName", record.getFieldValue(FieldType.indiLastName), ((Bean) indiLastNameOperator.getSource()).getFieldValue());
        // je simule ALT-X
        indiLastNameOperator.pressKey(KeyEvent.VK_X , InputEvent.ALT_MASK);
        // j'attends que le nom soit affiché dans indiFatherLastNameOperator
        JTextFieldOperator jtfo = new JTextFieldOperator(indiFatherLastNameOperator, record.getFieldValue(FieldType.indiLastName) );
        assertEquals( "indiFatherLastName jtfo", record.getFieldValue(FieldType.indiLastName), jtfo.getText());
        // je vérifie que indiFatherLastName est comité dans le record
        assertEquals( "indiFatherLastName record", record.getFieldValue(FieldType.indiLastName), record.getFieldValue(FieldType.indiFatherLastName));
        
    }
    
    @Test
    public void copyWifeNameToWifeFatherNameTest() {
        Record record = TestUtility.getRecordMarriage();
        record.setFieldValue(FieldType.wifeFatherLastName, "xxx");
        m_dataManager.addRecord(record);
        m_releveEditor.selectRecord(0);

        ComponentOperator wifeLastNameOperator  = new ComponentOperator(m_jfo , new NameComponentChooser(FieldType.wifeLastName.name()));
        ContainerOperator<Bean> wifeFatherLastNameOperator   = new ContainerOperator<Bean>(m_jfo , new NameComponentChooser(FieldType.wifeFatherLastName.name()));
        assertEquals( "wifeLastName", record.getFieldValue(FieldType.wifeLastName), ((Bean) wifeLastNameOperator.getSource()).getFieldValue());
        // je simule ALT-X
        wifeLastNameOperator.pressKey(KeyEvent.VK_Y , InputEvent.ALT_MASK);
        // j'attends que le nom soit affiché dans wifeFatherLastName
        JTextFieldOperator jtfo = new JTextFieldOperator(wifeFatherLastNameOperator, record.getFieldValue(FieldType.wifeLastName) );
        assertEquals( "wifeFatherLastName jtfo", record.getFieldValue(FieldType.wifeLastName), jtfo.getText());
        // je vérifie que wifeFatherLastName est comité dans le record
        assertEquals( "wifeFatherLastName record", record.getFieldValue(FieldType.wifeLastName), record.getFieldValue(FieldType.wifeFatherLastName));
        
    }
    
    @Test
    public void copyEventDateToIndiBirthDateTest() {
        Record record = TestUtility.getRecordBirth();
        m_dataManager.addRecord(record);
        m_releveEditor.selectRecord(0);

        ComponentOperator eventDateOperator  = new ComponentOperator(m_jfo , new NameComponentChooser(FieldType.eventDate.name()));
        ContainerOperator<BeanDate> indiBirthDateOperator  = new ContainerOperator<BeanDate>(m_jfo , new NameComponentChooser(FieldType.indiBirthDate.name()));
        assertEquals("eventDate", true, record.getField(FieldType.eventDate).equalsProperty(((DateWidget)((BeanDate) eventDateOperator.getSource()).getComponent(0)).getValue()));
        assertEquals("eventDate != indiBirthDate", false, record.getField(FieldType.eventDate).equalsProperty(((DateWidget)((BeanDate) indiBirthDateOperator.getSource()).getComponent(0)).getValue()));
        // je simule ALT-B
        eventDateOperator.pressKey(KeyEvent.VK_B , InputEvent.ALT_MASK);
        // j'attends que le champ soit mis à jour
        JTextFieldOperator jtfo = new JTextFieldOperator(indiBirthDateOperator, "2000");

        // je vérifie que indiLastName est copié dans indiFatherLastName
        assertEquals("eventDate == indiBirthDate", true, record.getField(FieldType.eventDate).equalsProperty(((DateWidget)((BeanDate) indiBirthDateOperator.getSource()).getComponent(0)).getValue()));
    }
    

   
    /**
     * implemente un MenuCommandProvider necessaire a la creation d'une instance de releveEditor
     */ 
    private static class MenuCommandProviderImpl implements MenuCommandProvider {

        public MenuCommandProviderImpl() {
        }

        @Override
        public void showPopupMenu(Component invoker, int x, int y) {
            
        }

        @Override
        public void showStandalone() {
            
        }

        @Override
        public void showStandalone(int panelIndex, int recordNo) {
            
        }

        @Override
        public void showConfigPanel() {
           
        }

        @Override
        public void showOptionPanel() {
            
        }

        @Override
        public void showToFront() {
            
        }

        @Override
        public void setBrowserVisible(boolean visible) {
           
        }

        @Override
        public void toggleBrowserVisible() {
            
        }

        @Override
        public void setGedcomLinkSelected(boolean selected) {
            
        }

        @Override
        public void showImage() {
            
        }
    }
}
