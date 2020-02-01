 package ancestris.modules.releve.editor;

import ancestris.modules.releve.TestUtility;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldSimpleValue;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.RecordBirth;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michel
 */


public class BeanFirstNameTest {
    
    DataManager dataManager;
    Record record;
    Field field;
    
    public static RecordBirth getRecordBirthPeter() {
        RecordBirth record = new RecordBirth();
        record.setFieldValue(Record.FieldType.eventDate, "01/01/2000");
        record.setFieldValue(Record.FieldType.cote, "cote");
        record.setFieldValue(Record.FieldType.freeComment,  "photo");
        record.setFieldValue(Record.FieldType.generalComment, "general comment");
        record.setIndi("Peter", "indiLastname", "M", "", "5/4/1842", "indiBirthPlace", "indiBirthAddress", "", "indiResidence", "indiAddress", "indiComment");
        record.setIndiFather("indiFatherFirstName", "indiFatherLastname", "indiFatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "indiFatherDead", "70y");
        record.setIndiMother("indiMotherFirstName", "indiMotherLastname", "indiMotherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "indiMotherDead", "72y");
        record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
        return record;
    }
    
        public static RecordBirth getRecordBirthSuzan() {
        RecordBirth record = new RecordBirth();
        record.setFieldValue(Record.FieldType.eventDate, "01/01/2000");
        record.setFieldValue(Record.FieldType.cote, "cote");
        record.setFieldValue(Record.FieldType.freeComment,  "photo");
        record.setFieldValue(Record.FieldType.generalComment, "general comment");
        record.setIndi("Suzan", "indiLastname", "F", "", "5/4/1842", "indiBirthPlace", "indiBirthAddress", "", "indiResidence", "indiAddress", "indiComment");
        record.setIndiFather("indiFatherFirstName", "indiFatherLastname", "indiFatherOccupation", "indiFatherResidence", "indiFatherAddress", "indiFatherComment", "indiFatherDead", "70y");
        record.setIndiMother("indiMotherFirstName", "indiMotherLastname", "indiMotherOccupation", "indiMotherResidence", "indiMotherAddress", "indiMotherComment", "indiMotherDead", "72y");
        record.setWitness1("w1firstname", "w1lastname", "w1occupation", "w1comment");
        record.setWitness2("w2firstname", "w2lastname", "w2occupation", "w2comment");
        record.setWitness3("w3firstname", "w3lastname", "w3occupation", "w3comment");
        record.setWitness4("w4firstname", "w4lastname", "w4occupation", "w4comment");
        return record;
    }
                
    @Before
    public void setUp() {
        dataManager = new DataManager();
        record = TestUtility.getRecordBirth();
        dataManager.addRecord(record);
        field = new FieldSimpleValue();
    }
    
    @After
    public void tearDown() {
        dataManager = null; 
        record = null;
        field = null;
    }

    /**
     * Test of setFieldImpl method, of class BeanFirstName.
     */
    @Test
    public void testSetFieldImpl() {
        BeanFirstName instance = new BeanFirstName( dataManager.getCompletionProvider().getFirstNames() );
        instance.setContext(record, Record.FieldType.indiFirstName);
        field.setValue("jean");
        instance.replaceValue( field );        
        instance.commitImpl();      
        // je verifie que la première lettre est convertie en majuscule
        assertEquals("replaceValue", "Jean", record.getFieldValue(Record.FieldType.indiFirstName) );        
        
    }

    /**
     * Test of removeNotify method, of class BeanFirstName.
     */
    @Test
    public void testAddNotify() {
//        BeanFirstName instance = new BeanFirstName( dataManager.getCompletionProvider() );
//        instance.addNotify();
//        instance.setContext(record, Record.FieldType.indiFirstName);
//        Java2sAutoTextField cFirst = (Java2sAutoTextField) (instance.getComponent(0));
//        assertEquals(dataManager.getCompletionProvider().getFirstNames(CompletionProvider.IncludeFilter.ALL).toString(), cFirst.getDataList().toString()); 
//        // add new firstname 
//        dataManager.addRecord(getRecordBirthPeter() );
//        //try { Thread.sleep(1000); } catch (InterruptedException e) { }
//        assertEquals(true, cFirst.getDataList().contains("Peter"));
    }
    
    /**
     * Test of removeNotify method, of class BeanFirstName.
     */
    @Test
    public void testRemoveNotify() {
//        BeanFirstName instance =new BeanFirstName( dataManager.getCompletionProvider() );
//        // j'active les mises à jours
//        instance.setContext(record, Record.FieldType.indiFirstName);
//      
//        Java2sAutoTextField cFirst = (Java2sAutoTextField) (instance.getComponent(0));
//        assertEquals(dataManager.getCompletionProvider().getFirstNames(CompletionProvider.IncludeFilter.ALL).toString(), cFirst.getDataList().toString()); 
//        
//        // add new firstname 
//        dataManager.addRecord(getRecordBirthPeter() );
//        try { Thread.sleep(50); } catch (InterruptedException e) { }
//        assertEquals("add peter", true, cFirst.getDataList().contains("Peter"));
//        
//        // je desactive les mises à jours
//        instance.removeNotify();
//        dataManager.addRecord(getRecordBirthSuzan() );
//        cFirst.getToolkit().sync();
//        try { Thread.sleep(50); } catch (InterruptedException e) { }
//        assertEquals("add suzan without nottify", false, cFirst.getDataList().contains("Suzan"));      
    }
    
}
