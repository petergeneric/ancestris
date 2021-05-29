package ancestris.modules.releve;

import ancestris.modules.releve.editor.EditorBeanGroup;
import ancestris.modules.releve.model.Record;
import java.text.Normalizer;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
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
        Record record = TestUtility.getRecordBirth();
        m_tc.getDataManager().addRecord(record);
        ResultsModel rm = ResultsModel.getInstance();

        // je lance la recherche
        String request = "indiFirstname";
        org.openide.util.Task task = CommandEvaluator.evaluate(request, rm);
        RequestProcessor.getDefault().post(task);

        // j'attends la fin de la recherche
        task.waitFinished();

        // je vérifie le résultat
        assertEquals("Nombre de résultats", 1, rm.getSize());
        assertEquals("Category", "Releve", rm.getContent().get(0).getCategory().getDisplayName());
        assertEquals("Releve", "ReleveQuickSearch", rm.getContent().get(0).getCategory().getProviders().get(0).getClass().getSimpleName());

        String expected = String.format("<html>%s %s, %s, %s %s",  // nom  prénom , enfant , date  ville
                record.getFieldValue(Record.FieldType.indiLastName),
                record.getFieldValue(Record.FieldType.indiFirstName),
                EditorBeanGroup.getGroup(record.getType(), Record.FieldType.indiFirstName).getTitle(),
                record.getFieldValue(Record.FieldType.eventDate),
                m_tc.getDataManager().getCityName()
        );
        expected = expected.trim();
        String displayName = rm.getContent().get(0).getItems().get(0).getDisplayName();
        for(String word : request.split(" ") ) {
            word = "<b>"+word.toLowerCase()+"</b>";
            assertEquals("DisplayName word "+word , true, displayName.toLowerCase().contains(word));
        }
        assertEquals("DisplayName without <b>", expected, displayName.replace("<b>","").replace("</b>", ""));

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
        String request = "ETE";
        org.openide.util.Task task = CommandEvaluator.evaluate(request, rm);
        RequestProcessor.getDefault().post(task);
        // j'attends la fin de la recherche
        task.waitFinished();
        // je vérifie le résultat
        assertEquals("Nombre de résultats", 1, rm.getSize());
        assertEquals("Category", "Releve", rm.getContent().get(0).getCategory().getDisplayName());
        assertEquals("Releve", "ReleveQuickSearch", rm.getContent().get(0).getCategory().getProviders().get(0).getClass().getSimpleName());

        String expected = String.format("<html>%s %s, %s, %s %s",  // nom  prénom , enfant , date  ville
                record.getFieldValue(Record.FieldType.indiLastName),
                record.getFieldValue(Record.FieldType.indiFirstName),
                EditorBeanGroup.getGroup(record.getType(), Record.FieldType.indiFirstName).getTitle(),
                record.getFieldValue(Record.FieldType.eventDate),
                m_tc.getDataManager().getCityName()
        );
        expected = expected.trim();
        String displayName = rm.getContent().get(0).getItems().get(0).getDisplayName();
        for(String word : request.split(" ") ) {
            word = "<b>"+word.toLowerCase()+"</b>";
            assertEquals("DisplayName word "+word , true, Normalizer.normalize(displayName.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").contains(word));
        }
        assertEquals("DisplayName without <b>", expected, displayName.replace("<b>","").replace("</b>", ""));
    }

    @Test
    public void testEvaluateAvecEspace() throws Exception {
        // j'ajoute un relevé
        Record record = TestUtility.getRecordBirth();
        m_tc.getDataManager().setPlace("ville", "", "", "", "");
        m_tc.getDataManager().addRecord(record);

        ResultsModel rm = ResultsModel.getInstance();

        // je lance la recherche (denier mot avec un caractère de moins
        // pour vérifier que le dernier caractère est bien copié dans le résultat
        String request= "ndiLast   indifi nam";
        org.openide.util.Task task = CommandEvaluator.evaluate(request, rm);
        RequestProcessor.getDefault().post(task);

        // j'attends la fin de la recherche
        task.waitFinished();

        // je vérifie le résultat
        assertEquals("Nombre de résultats", 1, rm.getSize());
        assertEquals("Category", "Releve", rm.getContent().get(0).getCategory().getDisplayName());
        assertEquals("Releve", "ReleveQuickSearch", rm.getContent().get(0).getCategory().getProviders().get(0).getClass().getSimpleName());

        String expected = String.format("<html>%s %s, %s, %s %s",  // nom  prénom , enfant , date  ville
                record.getFieldValue(Record.FieldType.indiLastName),
                record.getFieldValue(Record.FieldType.indiFirstName),
                EditorBeanGroup.getGroup(record.getType(), Record.FieldType.indiFirstName).getTitle(),
                record.getFieldValue(Record.FieldType.eventDate),
                m_tc.getDataManager().getCityName()
        );
        String displayName = rm.getContent().get(0).getItems().get(0).getDisplayName();
        for(String word : request.split(" +") ) {
            word = "<b>"+word.toLowerCase()+"</b>";
            assertEquals("DisplayName word "+word , true, displayName.toLowerCase().contains(word));
        }
        assertEquals("DisplayName without <b>", expected, displayName.replace("<b>","").replace("</b>", ""));

    }

}
