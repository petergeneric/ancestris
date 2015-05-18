/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLClassLoader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for interacting with environment/system settings/parameters
 */
public class EnvironmentChecker {
  
  private static Logger LOG = Logger.getLogger("genj.util");

  private final static String[] SYSTEM_PROPERTIES = {
        "java.vendor", "java.vendor.url",
        "java.version", "java.class.version",
        "os.name", "os.arch", "os.version",
        "browser", "browser.vendor", "browser.version",
        "user.name", "user.dir", "user.home", "all.home", "user.home.ancestris", "all.home.ancestris"
  };
  
  private final static Set<String> NOOVERRIDE = new HashSet<String>();
  
  
  /**
   * Check for Java 1.6 and higher
   */
  public static boolean isJava16() {
    String version = getProperty("java.version", "", "Checking Java VM version");
    return version.matches("1\\.[6789].*");
  }
  
  /**
   * Check for Mac
   */
  public static boolean isMac() {
    return getProperty("mrj.version", null, "isMac()")!=null;
  }
  
  /**
   * Check for Windows
   */
  public static boolean isWindows() {
    return getProperty("os.name", "", "isWindows()").indexOf("Windows")>-1;
  }
  
  private static String getDatePattern(int format) {
    try {
      return ((SimpleDateFormat)DateFormat.getDateInstance(format)).toPattern();
    } catch (Throwable t) {
      return "?";
    }
  }

  /**
   * Check the environment
   */
  public static void log() {
    
    // Go through system properties
    for (int i=0; i<SYSTEM_PROPERTIES.length; i++) {
      String key = SYSTEM_PROPERTIES[i];
      String msg = key + " = "+getProperty(SYSTEM_PROPERTIES[i], "", "check system props");
      if (NOOVERRIDE.contains(key))
        msg += " (no override)";
      LOG.info(msg);
    }
    
    // check locale specific stuff
    LOG.info("Locale = "+Locale.getDefault());
    LOG.info("DateFormat (short) = "+getDatePattern(DateFormat.SHORT));
    LOG.info("DateFormat (medium) = "+getDatePattern(DateFormat.MEDIUM));
    LOG.info("DateFormat (long) = "+getDatePattern(DateFormat.LONG));
    LOG.info("DateFormat (full) = "+getDatePattern(DateFormat.FULL));

      try {
        
      // check classpath
      String cpath = getProperty("java.class.path", "", "check classpath");
      StringTokenizer tokens = new StringTokenizer(cpath,System.getProperty("path.separator"),false);
      while (tokens.hasMoreTokens()) {
        String entry = tokens.nextToken();
        String stat = checkClasspathEntry(entry) ? " (does exist)" : "";
        LOG.info("Classpath = "+entry+stat);
      }
      
      // check classloaders
      ClassLoader cl = EnvironmentChecker.class.getClassLoader();
      while (cl!=null) {
        if (cl instanceof URLClassLoader) {
          LOG.info("URLClassloader "+cl + Arrays.asList(((URLClassLoader)cl).getURLs()));
        } else {
          LOG.info("Classloader "+cl);
        }
        cl = cl.getParent();
      }
      
      // check memory
      Runtime r = Runtime.getRuntime();
      LOG.log(Level.INFO, "Memory Max={0}/Total={1}/Free={2}", new Long[]{ new Long(r.maxMemory()), new Long(r.totalMemory()), new Long(r.freeMemory()) });

      // DONE
    } catch (Throwable t) {
      LOG.log(Level.WARNING, "unexpected exception in log()", t);
    }
  }

  /**
   * check individual classpath entry
   */
  private static boolean checkClasspathEntry(String entry) {
    try {
      return new File(entry).exists();
    } catch (Throwable t) {
    }
    return false;
  }

  /**
   * Returns a (system) property
   */
  public static String getProperty(String key, String fallback, String msg) {
    return getProperty(new String[]{key}, fallback, msg);
  }
  
  /**
   * Logfile
   */
  public static File getLog() {
    
    File home = new File(getProperty("user.home.ancestris", null, "log file"));
    if ( !(home.exists()||home.mkdirs()) && !home.isDirectory()) 
      throw new Error("Can't initialize home directoy "+home);
    
    return new File(home, "ancestris.log");
  }

  /**
   * Returns a (system) property
   */
  public static String getProperty(String[] keys, String fallback, String msg) {
    // see if one key fits
    String key = null, val, postfix;
    try {
      for (int i=0; i<keys.length; i++) {
        // get the key
        key = keys[i];
        // there might be a prefix in there
        int pf = key.indexOf('/');
        if (pf<0) pf = key.length();
        postfix = key.substring(pf);
        key = key.substring(0,pf);
        // ask the System
        val = System.getProperty(key);
        // found it ?
        if (val!=null) {
          LOG.finer("Using system-property "+key+'='+val+" ("+msg+')');
          return val+postfix;
        }
      }
    } catch (Throwable t) {
      LOG.log(Level.INFO, "Couldn't access system property "+key+" ("+t.getMessage()+")");
    }
    // fallback
    if (fallback!=null)
      LOG.fine("Using fallback for system-property "+key+'='+fallback+" ("+msg+')');
    return fallback;
  }
  
  /**
   * Load system properties from file
   */
  public static void loadSystemProperties(InputStream in) throws IOException {
    try {
      Properties props = new Properties();
      props.load(in);
      for (Object key : props.keySet()) {
        if (System.getProperty((String)key)==null)
          setProperty((String)key, props.getProperty((String)key));
      }
    } catch (Throwable t) {
      if (t instanceof IOException)
        throw (IOException)t;
      throw new IOException("unexpected throwable "+t.getMessage());
    }
  }
  
  /**
   * Set a system property while not overriding existing values
   */
  private static void setProperty(String key, String val) {
    String old = System.getProperty(key); 
    if (old==null) {
      LOG.fine("Setting system property "+key);
      System.setProperty(key, val);
    } else {
      LOG.fine("Not overriding system property "+key);
      NOOVERRIDE.add(key);
    }
  }
  
  /**
   * read system properties from file
   */
  static {
    
    try {
      EnvironmentChecker.loadSystemProperties(new FileInputStream(new File("system.properties")));
    } catch (IOException e) {
    }
  }

  /**
   * all.home - the shared home directory of all users (windows only)
   */
  static {
    
    // check the registry - this is windows only 
    if (isWindows()) {
      
      String QUERY = "reg query \"HKLM\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\ProfileList\"";
      Pattern PATTERN  = Pattern.compile(".*AllUsersProfile\tREG_SZ\t(.*)");
      String value = null;
      try {
        Process process = Runtime.getRuntime().exec(QUERY);
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while (true) {
          String line = in.readLine();
          if (line==null) break;
          Matcher match = PATTERN.matcher(line);
          if (match.matches()) {
            File home = new File(new File(System.getProperty("user.home")).getParent(), match.group(1));
            if (home.isDirectory())
              setProperty("all.home", home.getAbsolutePath());
            break;
          }
        }
        in.close();
      } catch (Throwable t) {
      }
    }
    // done
  }
  
  /**
   * "user.home.ancestris" the ancestris application data directory
   *  C:/Documents and Settings/%USERNAME%/Application Data/ancestris on windows
   *  ~/Library/Application Support/ancestris on Mac
   *  ~/.ancestris otherwise
   */
  static {
      // TODO: faire autrement (c'est une rapide modif pour pouvoir sauver les options sans toucher au "classique")
      // Peut-etre utiliser InstalledFileLocator
      File nbuserdir = new File(System.getProperty("netbeans.user"));

      setProperty("user.home.ancestris", (new File(nbuserdir,"ancestris")).getAbsolutePath());
  }  
}
