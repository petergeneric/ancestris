/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.util;

import ancestris.api.core.Version;
import ancestris.usage.UsageManager;
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
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.Lookup;

/**
 * Class for interacting with environment/system settings/parameters
 */
public class EnvironmentChecker {

    private static final Logger LOG = Logger.getLogger("ancestris.util");

    private final static String[] SYSTEM_PROPERTIES = {
        "user.name", "user.dir", "user.home", "all.home", "user.home.ancestris", "all.home.ancestris",
        "os.name", "os.version", "os.arch",
        "java.vendor", "java.vendor.url",
        "java.version", "java.class.version",
        "browser", "browser.vendor", "browser.version"
    };

    private final static Set<String> NOOVERRIDE = new HashSet<String>();

    public static String getAncestrisVersion() {
        return Lookup.getDefault().lookup(Version.class).getBuildString();
    }

    /**
     * Check for Mac
     *
     * @return
     */
    public static boolean isMac() {
    //return getProperty("mrj.version", null, "isMac()")!=null;     // FL : 2016-01-21 : obsolete

    // Another System property provided by Apple, mrj.version, can be used to a similar end. 
        // However, it is a legacy system property dating back to the Classic Mac OS, 
        // and is not guaranteed to have a deterministic format through future releases. 
        // Furthermore, Apple is trying to stay as close to the standard Java functionality as possible. 
        // For these reasons, we recommend that you instead use the more standard os.name 
        // and java.runtime.version properties for Macintosh identification in all ongoing 
        // Java development, as outlined in this Technical Note.
        // Other possibility is to use : Utilities.isMac()
        return getProperty("os.name", "", "isMac()").toLowerCase().contains("mac");
    }

    /**
     * Check for Windows
     *
     * @return
     */
    public static boolean isWindows() {
        return getProperty("os.name", "", "isWindows()").contains("Windows");
    }

    /**
     * Check for Linux
     *
     * @return
     */
    public static boolean isLinux() {
        return getProperty("os.name", "", "isLinux()").contains("Linux");
    }




    private static String getDatePattern(int format) {
        try {
            return ((SimpleDateFormat) DateFormat.getDateInstance(format)).toPattern();
        } catch (Throwable t) {
            return "?";
        }
    }

    /**
     * Check the environment
     */
    public static void log() {

        LOG.log(Level.INFO, "Date = {0}", new Date());

        LOG.log(Level.INFO, "Ancestris Version = {0}", getAncestrisVersion());

        // Go through system properties
        for (int i = 0; i < SYSTEM_PROPERTIES.length; i++) {
            String key = SYSTEM_PROPERTIES[i];
            String msg = key + " = " + getProperty(SYSTEM_PROPERTIES[i], "", "check system props");
            if (NOOVERRIDE.contains(key)) {
                msg += " (no override)";
            }
            LOG.info(msg);
        }
        remember(UsageManager.ACTION_ON);

        // Check locale specific stuff
        LOG.log(Level.INFO, "Locale = {0}", Locale.getDefault());
        LOG.log(Level.INFO, "DateFormat (short) = {0}", getDatePattern(DateFormat.SHORT));
        LOG.log(Level.INFO, "DateFormat (medium) = {0}", getDatePattern(DateFormat.MEDIUM));
        LOG.log(Level.INFO, "DateFormat (long) = {0}", getDatePattern(DateFormat.LONG));
        LOG.log(Level.INFO, "DateFormat (full) = {0}", getDatePattern(DateFormat.FULL));

        try {

            // Check classpath
            String cpath = getProperty("java.class.path", "", "check classpath");
            StringTokenizer tokens = new StringTokenizer(cpath, System.getProperty("path.separator"), false);
            while (tokens.hasMoreTokens()) {
                String entry = tokens.nextToken();
                String stat = checkClasspathEntry(entry) ? " (does exist)" : "";
                LOG.log(Level.INFO, "Classpath = {0}{1}", new Object[]{entry, stat});
            }

            // Check classloaders
            ClassLoader cl = EnvironmentChecker.class.getClassLoader();
            while (cl != null) {
                if (cl instanceof URLClassLoader) {
                    LOG.log(Level.INFO, "URLClassloader {0}{1}", new Object[]{cl, Arrays.asList(((URLClassLoader) cl).getURLs())});
                } else {
                    LOG.log(Level.INFO, "Classloader {0}", cl);
                }
                cl = cl.getParent();
            }

            // Check memory
            Runtime r = Runtime.getRuntime();
            LOG.log(Level.INFO, "Memory Max={0}/Total={1}/Free={2}", new Long[]{r.maxMemory(), r.totalMemory(), r.freeMemory()});

            // DONE
        } catch (Throwable t) {
            LOG.log(Level.WARNING, "unexpected exception in log()", t);
        }
    }

    public static void logOff() {
        remember(UsageManager.ACTION_OFF);
    }
    
    private static void remember(String action) {
        UsageManager.writeUsage(action);
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
        if (!(home.exists() || home.mkdirs()) && !home.isDirectory()) {
            throw new Error("Can't initialize home directoy " + home);
        }

        return new File(home, "ancestris.log");
    }

    /**
     * Returns a (system) property
     */
    public static String getProperty(String[] keys, String fallback, String msg) {
        // see if one key fits
        String key = null, val, postfix;
        try {
            for (int i = 0; i < keys.length; i++) {
                // get the key
                key = keys[i];
                // there might be a prefix in there
                int pf = key.indexOf('/');
                if (pf < 0) {
                    pf = key.length();
                }
                postfix = key.substring(pf);
                key = key.substring(0, pf);
                // ask the System
                val = System.getProperty(key);
                // found it ?
                if (val != null) {
                    LOG.log(Level.FINER, "Using system-property {0}={1} ({2})", new Object[]{key, val, msg});
                    return val + postfix;
                }
            }
        } catch (Throwable t) {
            LOG.log(Level.INFO, "Couldn''t access system property {0} ({1})", new Object[]{key, t.getMessage()});
        }
        // fallback
        if (fallback != null) {
            LOG.log(Level.FINE, "Using fallback for system-property {0}={1} ({2})", new Object[]{key, fallback, msg});
        }
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
                if (System.getProperty((String) key) == null) {
                    setProperty((String) key, props.getProperty((String) key));
                }
            }
        } catch (Throwable t) {
            if (t instanceof IOException) {
                throw (IOException) t;
            }
            throw new IOException("unexpected throwable " + t.getMessage());
        }
    }

    /**
     * Set a system property while not overriding existing values
     */
    private static void setProperty(String key, String val) {
        String old = System.getProperty(key);
        if (old == null) {
            LOG.log(Level.FINE, "Setting system property {0}", key);
            System.setProperty(key, val);
        } else {
            LOG.log(Level.FINE, "Not overriding system property {0}", key);
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
            // At least display the error in the console.
            // But display anxiety inducing message, todo : analyze and uncomment message.
            //e.printStackTrace();
        }
    }

    /**
     * all.home - the shared home directory of all users (windows only)
     */
    static {

        // check the registry - this is windows only 
        if (isWindows()) {

            String QUERY = "reg query \"HKLM\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\ProfileList\"";
            Pattern PATTERN = Pattern.compile(".*AllUsersProfile\tREG_SZ\t(.*)");
            String value = null;
            try {
                Process process = Runtime.getRuntime().exec(QUERY);
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    Matcher match = PATTERN.matcher(line);
                    if (match.matches()) {
                        File home = new File(new File(System.getProperty("user.home")).getParent(), match.group(1));
                        if (home.isDirectory()) {
                            setProperty("all.home", home.getAbsolutePath());
                        }
                        break;
                    }
                }
                in.close();
            } catch (IOException t) {
                //At least display the error in the console.
                // But display anxiety inducing message, todo : analyze and uncomment message.
                //t.printStackTrace();
            }
        }
        // done
    }

    /**
     * "user.home.ancestris" the ancestris application data directory
     * C:/Documents and Settings/%USERNAME%/Application Data/ancestris on
     * windows ~/Library/Application Support/ancestris on Mac ~/.ancestris
     * otherwise
     */
    static {
      // TODO: faire autrement (c'est une rapide modif pour pouvoir sauver les options sans toucher au "classique")
        // Peut-etre utiliser InstalledFileLocator
        File nbuserdir = new File(System.getProperty("netbeans.user"));

        setProperty("user.home.ancestris", (new File(nbuserdir, "ancestris")).getAbsolutePath());
    }

}
