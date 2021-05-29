/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2014 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.app;

import ancestris.api.core.Version;
import ancestris.core.pluginservice.AncestrisPlugin;
import genj.option.OptionProvider;
import genj.util.EnvironmentChecker;
import genj.util.Registry;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Main Class for Ancestris Application
 */
public class App {


    /*package*/ static Logger LOG;

    /*package*/ static File LOGFILE;
    private static Startup startup;
    public static ControlCenter center;

    /* Minimal version of Java to launch application */
    private static final String JAVA_VERSION = "1.8";

    /**
     * Ancestris Main Method
     */
    public static void main(final String[] args) {

        // we're ready to be run twice
        synchronized (App.class) {
            if (startup == null) {

                // run startup
                startup = new Startup();
                SwingUtilities.invokeLater(startup);
            }
        }

        // wait for startup do be done
        synchronized (startup) {
            if (center == null) {
                try {
                    startup.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /* TODO: sauvegarde des fichiers ouverts fait dans le hook du code exit
     * car dans le cas de la fermeture de l'application par le bouton fermer de la fenetre
     * ppale cela ne fonctionne pas.
     * Ceci est certainement du au fait que dans ce cas le thread eventqueue n'est pas active
     * avant le retour de la fonction close.
     * voir si on peut faire autrement:
     * en fait oui mais en utilisant l'api dataobject de NB et la possibilite de sauvegarder
     * tous les dataobject ouvert en quittant l'applicatuin
     */

    public static boolean closing() {
        LOG.info("Shutdown");
        saveModesIfRestartRequired();
        return true;
    }

    public static void close() {
        EnvironmentChecker.logOff();
        // persist options
        OptionProvider.persistAll();
        // Store registry
        Registry.persist();
        // Reload modes if restart required
        loadModesIfRestartRequired();
        // done

        LOG.info("/Shutdown");
        LOG.info("==================>     A N C E S T R I S      is      closed      <======================================\n\n");

    }

    /**
     * Our startup code
     */
    private static class Startup implements Runnable {

        /**
         * Constructor
         */
        @Override
        public void run() {

            // Catch anything that might happen
            try {

                // Prepare our master log and own LogManager for Ancestris
                LOG = Logger.getLogger("ancestris");

                // Create our home directory
                File home = new File(EnvironmentChecker.getProperty("user.home.ancestris", null, "determining home directory"));
                home.mkdirs();
                if (!home.exists() || !home.isDirectory()) {
                    throw new IOException("Can't initialize home directoy " + home);
                }

                // Prepare some basic logging for now
                Formatter formatter = new LogFormatter();
                setLogLevel("INFO");

                System.setOut(new PrintStream(new LogOutputStream(Level.INFO, "System", "out")));
                System.setErr(new PrintStream(new LogOutputStream(Level.WARNING, "System", "err")));

                // Initialize options first (creates a registry view within the above registry only containing the options)
                OptionProvider.getAllOptions();
                setFontSize();

                // Setup File Logging and check environment
                LOGFILE = new File(home, "ancestris.log");
                Handler handler = new FileHandler(LOGFILE.getAbsolutePath(), AppOptions.getMaxLogSizeKB() * 1024, 1, true);
                handler.setLevel(Level.ALL);
                handler.setFormatter(formatter);
                Logger.getLogger("ancestris").addHandler(handler);

                // Log is up
                LOG.info("\n\n==================>     A N C E S T R I S      is      starting      <======================================");
                LOG.info("Startup");

                // Priorite sur le parametre passe en ligne de commande
                if (System.getProperty("ancestris.debug.level") != null) {
                    setLogLevel(System.getProperty("ancestris.debug.level"));
                } else {
                    setLogLevel((Registry.get(App.class).get("logLevel", "")));
                }

                // Startup Information
                EnvironmentChecker.log();

                // Check VM version 1.8 minimum
                String version = System.getProperty("java.version");
                if (version.length()< 3) {
                    version = version + ".00";
                }
                if (JAVA_VERSION.compareTo(version.substring(0, 3)) > 0) {
                    final String errorMessage = NbBundle.getMessage(App.class, "EM_JavaVersion");
                    JOptionPane.showMessageDialog(null, errorMessage, "Error Message", JOptionPane.ERROR_MESSAGE);
                    throw new RuntimeException("Java Version not compatible.");
                }

                // Setup control center
                LOG.info("Launching control center...");
                center = new ControlCenter();

                // Done
                LOG.info("/Startup");
                LOG.info("   ");

            } catch (Throwable t) {
                LOG.log(Level.SEVERE, "Cannot instantiate the Ancestris application", t);
                System.exit(1);
                return;
            }

            synchronized (this) {
                notifyAll();
            }

        }

    } //Startup

    public static void setLogLevel(String logLevel) {
        // prepare some basic logging for now
        Logger root = Logger.getLogger("");
        Level level = Level.INFO;

        try {
            // allow command line override of debug level - set non-ancestris level a tad higher
            level = Level.parse(logLevel);
        } catch (IllegalArgumentException t) {
        }
        LOG.setLevel(level);
        Logger.getLogger("ancestris").setLevel(level);
        if (level.intValue() < Level.CONFIG.intValue()) {
            root.setLevel(Level.CONFIG);
        } else {
            root.setLevel(level);
        }
    }

    // Set fontsize (fontsize in command line option does not work consistently for all LaF (nimbus or greek for instance) and all labels across Ancestris menus (FL : 2020-07-25)
    public static void setFontSize() throws UnsupportedLookAndFeelException {
        int fontsize = Integer.valueOf(ancestris.app.AppOptions.getFontSize());
        UIDefaults uiDefaults = UIManager.getDefaults();
        LookAndFeel laf = UIManager.getLookAndFeel();
        boolean isNimbus = laf.getName().contains("Nimbus");
        LOG.info("LookAndFeel is "+laf.getName());
        if (isNimbus) {
            UIManager.setLookAndFeel(new NimbusLookAndFeel() {
                @Override
                public UIDefaults getDefaults() {
                    UIDefaults ret = super.getDefaults();
                    Font dFont = ret.getFont("defaultFont");
                    Font newFont = new Font(Font.SANS_SERIF, dFont.getStyle(), fontsize);
                    ret.put("defaultFont", newFont);
                    LOG.info("LookAndFeel : set default font to "+newFont);
                    return ret;
                }
            });
        } else {
            Enumeration enume = uiDefaults.keys();
            int i = 0;
            while (enume.hasMoreElements()) {
                Object key = enume.nextElement();
                Object o = uiDefaults.get(key);
                if (o instanceof Font) {
                    i++;
                    Font dFont = (Font) o;
                    Font newFont = new Font(dFont.getFontName(), dFont.getStyle(), fontsize);
                    uiDefaults.put(key.toString(), newFont);
                }
            }
            LOG.info("LookAndFeel (not nimbus) : set " + i + " default properties to fontsize "+ fontsize);
        }

    }

    /**
     * Our own log format
     */
    private static class LogFormatter extends Formatter {

        public String format(LogRecord record) {
            StringBuffer result = new StringBuffer(120);
            result.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(record.getMillis())));
            result.append(" = ");
            result.append(record.getLevel());
            result.append(":");
            result.append(record.getSourceClassName());
            result.append(".");
            result.append(record.getSourceMethodName());
            result.append(":   ");
            String msg = record.getMessage();
            Object[] parms = record.getParameters();
            if (parms == null || parms.length == 0) {
                result.append(record.getMessage());
            } else {
                result.append(MessageFormat.format(msg, parms));
            }
            result.append(System.getProperty("line.separator"));

            if (record.getThrown() != null) {

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                try {
                    record.getThrown().printStackTrace(pw);
                } catch (Throwable t) {
                }
                pw.close();
                result.append(sw.toString());
            }

            return result.toString();
        }
    }

    /**
     * Our STDOUT/ STDERR log outputstream
     */
    private static class LogOutputStream extends OutputStream {

        private char[] buffer = new char[256];
        private int size = 0;
        private Level level;
        private String sourceClass, sourceMethod;

        /**
         * Constructor
         */
        public LogOutputStream(Level level, String sourceClass, String sourceMethod) {
            this.level = level;
            this.sourceClass = sourceClass;
            this.sourceMethod = sourceMethod;
        }

        /**
         * collect up to limit characters
         */
        public void write(int b) throws IOException {
            if (b != '\n') {
                buffer[size++] = (char) b;
                if (size < buffer.length) {
                    return;
                }
            }
            flush();
        }

        /**
         *
         */
        public void flush() throws IOException {
            if (size > 0) {
                LOG.logp(level, sourceClass, sourceMethod, String.valueOf(buffer, 0, size).trim());
                size = 0;
            }
        }
    }

    /**
     * Exiting the application automatically saves modes stored in memory In case of restart after Wizard/Input or Options/Input, modes that were imported by the user are unfortunatelly overwritten This saves them to a backup directory before they get overwritten
     */
    static private void saveModesIfRestartRequired() {
        if (!isRestartSet()) {
            return;
        }
        // Now copy modes to a backup folder
        String baseDir = System.getProperty("netbeans.user") + File.separator + "config" + File.separator + "Windows2Local";
        File dirToSaveFile = new File(baseDir);
        File dirTempFile = new File(baseDir + "Tmp");
        try {
            FileObject dirToSave = FileUtil.createFolder(dirToSaveFile);
            FileObject dirTemp = FileUtil.createFolder(dirTempFile);
            copyFolder(dirToSave, dirTemp);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    /**
     * Exiting the application automatically saves modes stored in memory In case of restart after Wizard/Input or Options/Input, modes that were imported by the user are unfortunatelly overwritten This reloads them from a backup directory after they have been overwritten so that next start uses the modes that were imported
     */
    static private void loadModesIfRestartRequired() {
        if (!isRestartSet()) {
            return;
        }
        // Now load modes to the backup folder
        String baseDir = System.getProperty("netbeans.user") + File.separator + "config" + File.separator + "Windows2Local";
        File dirTempFile = new File(baseDir + "Tmp");
        File dirToLoadFile = new File(baseDir);
        try {
            FileObject dirTemp = FileUtil.createFolder(dirTempFile);
            FileObject dirToLoad = FileUtil.createFolder(dirToLoadFile);
            dirToLoad.delete();
            dirToLoad = FileUtil.createFolder(dirToLoadFile);
            copyFolder(dirTemp, dirToLoad);
            dirTemp.delete();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static boolean isRestartSet() {
        String userDir = new File(System.getProperty("user.home.ancestris")).getParent();   // NOI18N  // FL: netbeans.user has been obsoleted
        String restartFlagFileStr = userDir + File.separator + "var" + File.separator + "restart";
        File restartFlagFile = new File(restartFlagFileStr);
        return (restartFlagFile != null && restartFlagFile.exists());
    }

    private static void copyFolder(FileObject dirFrom, FileObject dirTo) throws IOException {
        FileObject[] children = dirFrom.getChildren();
        for (int i = 0; i < children.length; i++) {
            FileObject fileObject = children[i];
            if (fileObject.isFolder()) {
                copyFolder(fileObject, dirTo.createFolder(fileObject.getNameExt()));
            } else {
                FileUtil.copyFile(fileObject, dirTo, fileObject.getName());
            }
        }
    }

    @ServiceProvider(service = Version.class)
    public static class AppPlugin extends AncestrisPlugin implements Version {

        @Override
        public String getBuildString() {
            return getPluginVersion();
        }

        @Override
        public String getVersionString() {
            return getPluginVersion();
        }

        @Override
        public String getDescription() {
            return getPluginShortDescription();
        }
    }

} //App

