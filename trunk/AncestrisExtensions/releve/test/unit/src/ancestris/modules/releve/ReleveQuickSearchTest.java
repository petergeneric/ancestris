package ancestris.modules.releve;

import ancestris.modules.releve.editor.EditorBeanGroup;
import ancestris.modules.releve.model.Record;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.quicksearch.CommandEvaluator;
import org.netbeans.modules.quicksearch.ResultsModel;
import org.openide.util.RequestProcessor;

/**
 *
 * @author michel
 */


public class ReleveQuickSearchTest {
    
    ReleveTopComponent m_tc;

    public ReleveQuickSearchTest() {
    }
    
    @Before
    public void setUp() throws Exception {
        // je déclare le service de recherche ReleveQuickSearch
        UnitTestUtils.prepareTest(new String [] { "/ancestris/modules/releve/resources/releveProvider.xml" });
        // je crée une instance de ReleveTopComponent qui héberge le provider ReleveQuickSearch
        m_tc = new ReleveTopComponent();
        m_tc.setVisible(false);
        m_tc.open();        
    }
    
    @After
    public void tearDown() throws Exception {
        m_tc.getDataManager().removeAll();
        m_tc.close();
        m_tc = null;
    }

    @Test
    public void testEvaluate1() throws Exception {
        // j'ajoute un relevé
        m_tc.getDataManager().addRecord(TestUtility.getRecordBirth());

        ResultsModel rm = ResultsModel.getInstance();
        // je lance la recherche        
        org.openide.util.Task task = CommandEvaluator.evaluate("indiFirstname", rm);
        RequestProcessor.getDefault().post(task);
        // j'attends la fin de la recherche
        task.waitFinished();
        // je vérifie le résultat
        assertEquals("Nombre de résultats", 1, rm.getSize());
        assertEquals("Category", "Releve", rm.getContent().get(0).getCategory().getDisplayName());
        assertEquals("Releve", "ReleveQuickSearch", rm.getContent().get(0).getCategory().getProviders().get(0).getClass().getSimpleName());
        assertEquals("DisplayName", "<html>indiLastname <b>indiFirstname</b>, Enfant, 01/01/2000", rm.getContent().get(0).getItems().get(0).getDisplayName());
    }
    
    @Test
    public void testEvaluateAvecAccent() throws Exception {
        // j'ajoute un relevé
        Record record = TestUtility.getRecordBirth();
        record.getField(Record.FieldType.indiLastName).setValue("ÉTÈ");
        m_tc.getDataManager().setPlace("ville", "", "", "", "");
        m_tc.getDataManager().addRecord(record);

        ResultsModel rm = ResultsModel.getInstance();
        // je lance la recherche        
        org.openide.util.Task task = CommandEvaluator.evaluate("ETE", rm);
        RequestProcessor.getDefault().post(task);
        // j'attends la fin de la recherche
        task.waitFinished();
        // je vérifie le résultat
        assertEquals("Nombre de résultats", 1, rm.getSize());
        assertEquals("Category", "Releve", rm.getContent().get(0).getCategory().getDisplayName());
        assertEquals("Releve", "ReleveQuickSearch", rm.getContent().get(0).getCategory().getProviders().get(0).getClass().getSimpleName());
        //assertEquals("DisplayName", "<html> indiLastname <b>indiFirstname</b> , Enfant , 01/01/2000", rm.getContent().get(0).getItems().get(0).getDisplayName());
        String format = "<html><b>%s</b> %s, %s, %s %s" ; // nom , prénom , enfant , date  ville
        String expected = String.format(format, 
                record.getFieldValue(Record.FieldType.indiLastName),
                record.getFieldValue(Record.FieldType.indiFirstName),
                EditorBeanGroup.getGroup(record.getType(), Record.FieldType.indiFirstName).getTitle(), 
                record.getFieldValue(Record.FieldType.eventDate),
                m_tc.getDataManager().getCityName()
        );
        assertEquals("DisplayName", expected, rm.getContent().get(0).getItems().get(0).getDisplayName());                       
    }
}
