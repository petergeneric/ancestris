package ancestris.modules.releve.model;

import ancestris.modules.releve.TestUtility;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import java.net.MalformedURLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michel
 */


public class GedcomLinkProviderTest {
    
    RecordModel m_recordModel;
    Gedcom m_gedcom;
    
    public GedcomLinkProviderTest() {
    }
    
    @Before
    public void setUp() throws GedcomException, MalformedURLException {
         m_recordModel = new RecordModel();
         m_gedcom= TestUtility.createGedcomF2();
    }
    
    @After
    public void tearDown() {
        m_gedcom = null;
        m_recordModel = null;
    }
  

    @Test
    public void testInit() {
        Record record = TestUtility.getRecordBirthF2();
        m_recordModel.addRecord(record);
        
        GedcomLinkProvider instance = new GedcomLinkProvider();
        instance.init(m_recordModel, m_gedcom, true, false);
                
        GedcomLink gedcomLink = instance.getGedcomLink(record);
        assertNotNull("gedcomLink créé par init", gedcomLink);
        assertEquals("nom ", record.getFieldValue(Record.FieldType.indiFirstName), ((Indi)gedcomLink.getEntity()).getFirstName() );

        instance.init(m_recordModel, m_gedcom, false, false);
        gedcomLink = instance.getGedcomLink(m_recordModel.getRecord(0));
        assertNull("gedcomLink supprimé", gedcomLink);        
    }
        
    @Test
    public void testAddRecord() {
        Record record = TestUtility.getRecordBirthF2();        
        GedcomLinkProvider instance = new GedcomLinkProvider();
        instance.init(m_recordModel, m_gedcom, true, false);
        
        instance.addRecord(record);
        
        GedcomLink gedcomLink = instance.getGedcomLink(record);
        assertNotNull("gedcomLink créé par addRecord", gedcomLink);
        assertEquals("nom ", record.getFieldValue(Record.FieldType.indiFirstName), ((Indi)gedcomLink.getEntity()).getFirstName() );                
    }
    
    @Test
    public void testRemoveRecord() {
        Record birthRecord = TestUtility.getRecordBirthF2();        
        Record marriageRecord = TestUtility.getRecordMarriageF2();        
        GedcomLinkProvider instance = new GedcomLinkProvider();
        m_recordModel.addRecord(birthRecord);
        m_recordModel.addRecord(marriageRecord);
        instance.init(m_recordModel, m_gedcom, true, false);
        
        assertNotNull("gedcomLink birthRecord créé ", instance.getGedcomLink(birthRecord));
        assertNotNull("gedcomLink marriageRecord créé", instance.getGedcomLink(marriageRecord));
        
        instance.removeRecord(birthRecord);
        assertNull("gedcomLink birthRecord supprimé ", instance.getGedcomLink(birthRecord));
        assertNotNull("gedcomLink marriageRecord conservé", instance.getGedcomLink(marriageRecord));

        instance.removeRecord(marriageRecord);
        assertNull("gedcomLink marriageRecord supprimé", instance.getGedcomLink(marriageRecord));

    }
    
    @Test
    public void testRemoveAll() {
        Record birthRecord = TestUtility.getRecordBirthF2();        
        Record marriageRecord = TestUtility.getRecordMarriageF2();        
        GedcomLinkProvider instance = new GedcomLinkProvider();
        m_recordModel.addRecord(birthRecord);
        m_recordModel.addRecord(marriageRecord);
        instance.init(m_recordModel, m_gedcom, true, false);
        
        assertNotNull("gedcomLink birthRecord créé ", instance.getGedcomLink(birthRecord));
        assertNotNull("gedcomLink marriageRecord créé", instance.getGedcomLink(marriageRecord));
        
        instance.removeAll();
        
        assertNull("gedcomLink birthRecord supprimé ", instance.getGedcomLink(birthRecord));
        assertNull("gedcomLink marriageRecord supprimé", instance.getGedcomLink(marriageRecord));

    }

    
}
