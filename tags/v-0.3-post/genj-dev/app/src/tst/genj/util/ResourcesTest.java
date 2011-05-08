/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * Test language resources for completeness
 */
public class ResourcesTest extends TestCase {
  
  private final static File dir = new File("./language");

  /** check english resources against german*/
  public void testDE() throws IOException {
    testTranslated("de");
  }
  
  /** check english resources against german*/
  public void testFR() throws IOException {
    testTranslated("fr");
  }
  
  private void testTranslated(String lang) throws IOException {
    assertEquals("differences in translation between en and "+lang, 0, diff("en", lang).size());
  }
  
  /**
   * Run externally
   */
  public static void main(String[] args) {
    
    // a directory given?
    if (args.length==1 && args[0].equals("all")) {
      File[] translations = dir.listFiles();
      for (int i=0;i<translations.length;i++) {
        if (!translations[i].isDirectory() ||translations[i].getName().startsWith(".") || translations[i].getName().equals("en")) 
          continue;
        diff(translations[i].getName());
      }
      return;
    }
    
    String translation = Locale.getDefault().getLanguage();
    if (translation.equals("en")) {
      System.out.println("Set language to diff with -Duser.language=xx");
      return;
    }

    diff(translation);
  }
    
  private static void diff(String translation) {
    
    System.out.println("Diffing en against "+translation);
    try  {
      List diffs = new ResourcesTest().diff("en", translation);
      if (diffs.isEmpty())
        System.out.println("No differences found - Good Job!");
      else {
        System.out.println(diffs.size()+" differences found:");
        for (Iterator it=diffs.iterator();it.hasNext();)
          System.out.println(it.next());
      }      
    } catch (IOException e) {
      System.out.println("IOException during diff: "+e.getMessage());
    }
  }
  
  /**
   * Diff directories
   */
  private List diff(String original, String translation) throws IOException {
    return diffResources(loadResources(original), loadResources(translation));
  }
  
  /** 
   * Diff resources of the original vs the translation
   */
  private List diffResources(Map originals, Map translations) {
    
    List diffs = new ArrayList();

    // go package by package
    for (Iterator packages = originals.keySet().iterator(); packages.hasNext(); ) {
      // grab package info and resources in original and translation
      String pckg = (String)packages.next();
      Resources original = (Resources)originals.get(pckg);
      Resources translation = (Resources)translations.get(pckg);
      if (translation==null)
        diffs.add(pckg+",*,not translated");
      else
        diffResource(pckg, original, translation, diffs);
    }
    
    // done
    return diffs;
  }
  
  private final static Pattern PATTERN_IGNORE = Pattern.compile(".*[A-Z]{2}.*");
  
  private void diffResource(String pckg, Resources original, Resources translation, List diffs) {
    // go key bey key
    for (Iterator keys = original.getKeys().iterator(); keys.hasNext(); ) {
      // grab key, original value and translated value
      String key = (String)keys.next();
      String val1 = (String)original.getString(key);
      String val2 = (String)translation.getString(key, false);
      // ignore key?
      if (PATTERN_IGNORE.matcher(key).matches())
        continue;
      // check translation
      if (val2==null) {
        diffs.add(pckg+","+key+",not translated");
      } else {
        try {
          int fs1 = Resources.getMessageFormat(val1).getFormats().length;
          int fs2 = Resources.getMessageFormat(val2).getFormats().length;
          if (fs1!=fs2) {
            diffs.add(pckg+","+key+",wrong # of {n}s");
          }
        } catch (IllegalArgumentException e) {
          // some values contain e.g. '{n}' which doesn't go with MessageFormat - ignored
        }
      }
      // next key
    }
    // check for unnecessary keys in translation
    for (Iterator keys = translation.getKeys().iterator(); keys.hasNext(); ) {
      // grab key and check against original
      String key = (String)keys.next();
      // any uppercase in it and we assume it's something special
      if (!key.toLowerCase().equals(key))
        continue;
      // compare!
      if (!original.contains(key)) {
        diffs.add(pckg+","+key+",translated but not in original");
      }
    }
    // done
  }
  
  /**
   * Load language resources for given language
   * @return mapping of package to Resources
   */
  private Map loadResources(String language)  throws IOException {
    return loadResources(new File(dir, language),  "", new TreeMap());
  }
  
  private Map loadResources(File dir, String pckg, Map result) throws IOException {
    
    // check files in dir
    File[] resources = dir.listFiles();
    for (int i=0;i<resources.length;i++) {
      File resource = resources[i];
      if (resource.isDirectory())
        loadResources(resource, pckg+"/"+resource.getName(), result);
      else if (resource.getName().endsWith(".properties"))
        result.put(pckg, new Resources(new FileInputStream(resource)));
    }
    
    // done
    return result;
  }
  
}
