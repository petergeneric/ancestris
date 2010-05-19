/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.io;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.util.Origin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * Testing Gedcom read/write diff
 */
public class GedcomReadWriteTest extends TestCase {
  
  /**
   * test read/write with encrypted content
   */
  public void testEncryptDecrypt() throws IOException, GedcomException {
    
    // we don't need log output for this
    Logger.getLogger("").setLevel(Level.OFF);

    // read it
    File original = new File("./gedcom/example.ged");
    Gedcom ged = new GedcomReader(Origin.create(original.toURL())).read();
    
    // set everything to private
    ged.setPassword("password");
    for (Iterator ents = ged.getEntities().iterator(); ents.hasNext(); ) {
      Entity ent = (Entity)ents.next();
      ent.setPrivate(true, true);
    }
    
    // write it encrypted
    File temp = File.createTempFile("test", ".ged");
    FileOutputStream out = new FileOutputStream(temp);
    new GedcomWriter(ged, temp.getName(), null, out).write();
    out.close();
    
    // read again - first without then with password
    try {
      GedcomReader reader = new GedcomReader(Origin.create(temp.toURL()));
      ged = reader.read();
      fail("reading without password should fail");
    } catch (GedcomIOException e) {
      // this should happen
    }
    GedcomReader reader = new GedcomReader(Origin.create(temp.toURL()));
    reader.setPassword(Gedcom.PASSWORD_UNKNOWN);
    ged = reader.read();
    
    // write it encrypted a second time
    temp = File.createTempFile("test", ".ged");
    out = new FileOutputStream(temp);
    new GedcomWriter(ged, temp.getName(), null, out).write();
    out.close();
    
    // read again - this time with password
    reader = new GedcomReader(Origin.create(temp.toURL()));
    reader.setPassword("password");
    ged = reader.read();
    
    // write it deencrypted (without password) 
    temp = File.createTempFile("test", ".ged");
    out = new FileOutputStream(temp);
    ged.setPassword("");
    new GedcomWriter(ged, temp.getName(), null, out).write();
    out.close();
    
    // compare original to last temp now
    assertEquals( Collections.EMPTY_LIST, diff(original, temp) );
    
    // done
    
  }
  
  /**
   * Read a stress file
   */
  public void testStressFile() throws IOException, GedcomException {
    
    // we don't need log output for this
    Logger.getLogger("").setLevel(Level.OFF);

    // try to read file
    Gedcom ged = new GedcomReader(getClass().getResourceAsStream("stress.ged")).read();
    
    // write it again
    File temp = File.createTempFile("test", ".ged");
    OutputStream out = new FileOutputStream(temp);
    new GedcomWriter(ged, temp.getName(), null, out).write();
    out.close();
    
    // compare line by line
    assertEquals( Collections.singletonList("2 _TAG<>"), diff(temp, getClass().getResourceAsStream("stress.ged")) );
  }
  
  /**
   * Read a file / write it / compare
   */
  public void testReadWrite() throws IOException, GedcomException {
    
    // we don't need log output for this
    Logger.getLogger("").setLevel(Level.OFF);

    // read/write file
    File original = new File("./gedcom/example.ged");
    File temp = File.createTempFile("test", ".ged");
    
    // read it
    Gedcom ged = new GedcomReader(Origin.create(original.toURL())).read();
    
    // write it
    FileOutputStream out = new FileOutputStream(temp);
    new GedcomWriter(ged, temp.getName(), null, out).write();
    out.close();
    
    // diff files and there should be one difference
    assertEquals(Collections.EMPTY_LIST, diff(original, temp));
    
  }
  
  private List diff(File file1, File file2) throws IOException {
    return diff(file1, new FileInputStream(file2));
  }
    
  private List diff(File file1, InputStream file2) throws IOException {
    
    List result = new ArrayList();
    
    BufferedReader left = new BufferedReader(new InputStreamReader(new FileInputStream(file1)));
    BufferedReader right = new BufferedReader(new InputStreamReader(file2));
    
    // read past the header
    String lineLeft = left.readLine();
    while (true) {
      left.mark(256);
      lineLeft = left.readLine();
      if (lineLeft.startsWith("0")) break;
    }
    left.reset();
    String lineRight = right.readLine();
    while (true) {
      right.mark(256);
      lineRight = right.readLine();
      if (lineRight.startsWith("0")) break;
    }
    right.reset();
    
    // compare records
    while (true) {
      
      left.mark(256); right.mark(256);
      lineLeft = left.readLine();
      lineRight = right.readLine();

      // done?
      if (lineLeft==null&&lineRight==null)
        break;
      
      // assume "," equals ", "
      if (lineLeft==null||lineRight==null) {
        result.add(lineLeft+"<>"+lineRight);
        break;
      }
        
      // assert equal
      if (!matches(lineLeft, lineRight)) {
        
        // maybe next line matches again?
        left.mark(256);
        if (matches(left.readLine(), lineRight))
          result.add(lineLeft+"<>");
        else
          result.add(lineLeft+"<>"+lineRight);
        left.reset();
        right.reset();
      }
    }
    
    left.close();
    right.close();
    
    // done
    return result;
  }
  
  private static Pattern COMMASPACE = Pattern.compile(", ");
  private static String COMMA = ",";

  private boolean matches(String left, String right) {
    if (left==null||right==null)
      return false;
    return COMMASPACE.matcher(left).replaceAll(COMMA).equals(COMMASPACE.matcher(right).replaceAll(COMMA));
  }
  
} //GedcomIDTest
