/* 
 * 
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 * GedcomDelTest.java
 *  $Header$
 */

package genj.gedcom;

import genj.io.GedcomIOException;
import genj.io.GedcomWriter;

import java.io.IOException;
import java.io.OutputStream;

import junit.framework.TestCase;

/**
 * Test for inconsistencies in the Entity.delete() method.
 * This was originally created to track the assicated 
 * <a href="http://sourceforge.net/tracker/index.php?func=detail&aid=1489891&group_id=46817&atid=447494">SF bug</a>
 * It now looks like a transaction wrapping the delete seems to be the workaround, which means that 
 * this is either a documentation bug (in delete) or a bug in the delete implementation.
 * 
 * Update NM: there was a problem that backlinking references where not delete when there isn't
 * a transaction as Danny found out - rectified that today. Manipulating gedcom without transactions
 * is not recommended but possible so this fix was necessary.
 * 
 * @author Danny Sadinoff
 *
 */
public class GedcomDelTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(GedcomDelTest.class);
    }

    private Fam createTestFamily() throws GedcomException, IOException
    {
        Gedcom gedcom = new Gedcom();
        
        gedcom.doUnitOfWork(new UnitOfWork() {
          public void perform(Gedcom gedcom) throws GedcomException {
            Indi husband = (Indi) gedcom.createEntity(Gedcom.INDI, "Ihusband");
            Indi wife = (Indi) gedcom.createEntity(Gedcom.INDI, "Iwife");
            Indi child = (Indi) gedcom.createEntity(Gedcom.INDI, "Ikid");
            Fam family = (Fam) gedcom.createEntity(Gedcom.FAM,"F1");
            family.setHusband(husband);
            family.setWife(wife);
            family.addChild(child);
          }
        });
        
        /* baseline*/
        validate(gedcom);
        
        return (Fam)gedcom.getEntity("F1");
    }
    
    /*
     * Test method for 'genj.gedcom.Gedcom.deleteEntity(Entity)'
     */
    public void testDeleteEntity()  throws Exception{
        testDeleteKid(createTestFamily());
        testDeleteParent(createTestFamily() );
        testDeleteFamily(createTestFamily());
    }
    
    /**
     * Throws some sort of exception if the argument isn't a valid gedcom.
     * @param gedcom
     * @throws GedcomIOException
     */
    private static void validate(Gedcom gedcom) throws IOException
    {
        OutputStream sink = new OutputStream() {
            public void write(int arg0) { /* nop */ }
        };
        
        GedcomWriter writer = new GedcomWriter(gedcom,"test",null,sink);                    
        writer.write(); //closes fos
    }

    private void testDeleteFamily(Fam fam) throws Exception {
        Gedcom gedcom = fam.getGedcom();
//        gedcom.startTransaction();
        gedcom.deleteEntity(fam);
//        gedcom.endTransaction();
        validate(gedcom);
    }

    private void testDeleteParent(Fam fam) throws Exception {
        Gedcom gedcom = fam.getGedcom();
//        gedcom.startTransaction();
        gedcom.deleteEntity(fam.getHusband());
//        gedcom.endTransaction();
        validate(gedcom);
    }

    private void testDeleteKid( Fam fam) throws Exception {
        Gedcom gedcom = fam.getGedcom();
//        gedcom.startTransaction();
        gedcom.deleteEntity(fam.getChild(0));
//        gedcom.endTransaction();
        validate(gedcom);
    }

}