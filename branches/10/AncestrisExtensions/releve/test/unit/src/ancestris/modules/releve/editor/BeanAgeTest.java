package ancestris.modules.releve.editor;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldAge;
import ancestris.modules.releve.model.Record;
import javax.swing.JFormattedTextField;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author michel
 */


public class BeanAgeTest {
    DataManager dataManager;
    Record record;
    Field field;

    public BeanAgeTest() {
    }

    @Before
    public void setUp() {
        dataManager = new DataManager();
        record = TestUtility.getRecordMarriage();
        dataManager.addRecord(record);
        field = new FieldAge();
    }

    @After
    public void tearDown() {
        dataManager = null;
        record = null;
        field = null;
    }

    @Test
    public void testSetFieldImpl() {
        BeanAge instance = new BeanAge();
        instance.setContext(record, Record.FieldType.indiAge);
        JFormattedTextField tField = (JFormattedTextField) (instance.getComponent(0));
        assertEquals(record.getFieldValue(Record.FieldType.indiAge).substring(0, 2), tField.getText().trim().substring(0, 2));

        tField.setText(" 30103");
        assertEquals("30a  10m  3 j", tField.getText().trim());
        instance.commit();
        assertEquals("30y 10m 3d", record.getFieldValue(Record.FieldType.indiAge));
    }

    @Test
    public void testReplaceImpl() {
        BeanAge instance = new BeanAge();
        instance.setContext(record, Record.FieldType.indiAge);
        field.setValue("30y 10m 3d");
        instance.replaceValue(field);

        JFormattedTextField tField = (JFormattedTextField) (instance.getComponent(0));
        assertEquals("30a  10m   3j", tField.getText().trim());

        instance.commit();
        assertEquals("30y 10m 3d", record.getFieldValue(Record.FieldType.indiAge));

        instance.replaceValue(null);
        assertEquals("a    m    j", tField.getText().trim());
        instance.commit();
        assertEquals("0d", record.getFieldValue(Record.FieldType.indiAge));

    }


}
