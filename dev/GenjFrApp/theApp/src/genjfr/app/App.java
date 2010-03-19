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
package genjfr.app;

import genj.Version;
import genj.app.Options;
//import genj.app.SplashWindow;
import genj.gedcom.Gedcom;
import genj.option.OptionProvider;
import genj.util.EnvironmentChecker;
import genj.util.Registry;
import genj.util.Resources;
import genj.window.GenjFrWindowManager;
import genj.window.WindowManager;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 * Main Class for GenJ Application
 */
public class App {

    /*package*/ static Logger LOG;

    /*package*/ static File LOGFILE;
    private static Startup startup;
    public static ControlCenter center;
    public static WindowManager genjWindowManager;
//    private static Shutdown shutDownTask;
    private static boolean x11ErrorHandlerFixInstalled = false;
    public static Registry REGISTRY = new Registry("genj");

    /**
     * GenJ Main Method
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
            if (startup.center == null) {
                try {
                    startup.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        center = startup.center;
        genj.app.App.LOG = LOG; // FIXME: horrible hack!
        genj.app.App.LOGFILE = LOGFILE; // FIXME: horrible hack!
        genj.app.App.genjfrWindowManager = startup.winMgr; // TODO: horrible hack!

        if (!x11ErrorHandlerFixInstalled && !EnvironmentChecker.isMac() && !EnvironmentChecker.isWindows()) {
            x11ErrorHandlerFixInstalled = true;
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    initX11ErrorHandlerFix();
                }
            });
        }
// load
//        startup.center.load(args);

//Runnable r = new Runnable() {
//      public void run() {
//        startup.center.load(args);
//      }
//    } ;
//        try {
//            SwingUtilities.invokeAndWait(r);
//        } catch (InterruptedException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (InvocationTargetException ex) {
//            Exceptions.printStackTrace(ex);
//        }
    }

    public static boolean closing() {
        LOG.info("Shutdown");
        saveModesIfRestartRequired();
        return center.nbDoExit();
    }

    public static void close() {
        // persist options
        OptionProvider.persistAll(REGISTRY);
        // Store registry
        Registry.persist();
        // Reload modes if restart required
        loadModesIfRestartRequired();
        // done
        LOG.info("/Shutdown");

    }

    /**
     * Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=171432
     * http://bugs.sun.com/view_bug.do?bug_id=6678385
     * TODO: voir http://www.netbeans.org/issues/show_bug.cgi?id=115606
     */
    private static void initX11ErrorHandlerFix() {
        assert EventQueue.isDispatchThread();

        try {
            // get XlibWrapper.SetToolkitErrorHandler() and XSetErrorHandler() methods
            Class xlibwrapperClass = Class.forName("sun.awt.X11.XlibWrapper");
            final Method setToolkitErrorHandlerMethod = xlibwrapperClass.getDeclaredMethod("SetToolkitErrorHandler", null);
            final Method setErrorHandlerMethod = xlibwrapperClass.getDeclaredMethod("XSetErrorHandler", new Class[]{Long.TYPE});
            setToolkitErrorHandlerMethod.setAccessible(true);
            setErrorHandlerMethod.setAccessible(true);

            // get XToolkit.saved_error_handler field
            Class xtoolkitClass = Class.forName("sun.awt.X11.XToolkit");
            final Field savedErrorHandlerField = xtoolkitClass.getDeclaredField("saved_error_handler");
            savedErrorHandlerField.setAccessible(true);

            // determine the current error handler and the value of XLibWrapper.ToolkitErrorHandler
            // (XlibWrapper.SetToolkitErrorHandler() sets the X11 error handler to
            // XLibWrapper.ToolkitErrorHandler and returns the old error handler)
            final Object defaultErrorHandler = setToolkitErrorHandlerMethod.invoke(null, null);
            final Object toolkitErrorHandler = setToolkitErrorHandlerMethod.invoke(null, null);
            setErrorHandlerMethod.invoke(null, new Object[]{defaultErrorHandler});

            // create timer that watches XToolkit.saved_error_handler whether its value is equal
            // to XLibWrapper.ToolkitErrorHandler, which indicates the start of the trouble
            Timer timer = new Timer(200, new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        Object savedErrorHandler = savedErrorHandlerField.get(null);
                        if (toolkitErrorHandler.equals(savedErrorHandler)) {
                            // Last saved error handler in XToolkit.WITH_XERROR_HANDLER
                            // is XLibWrapper.ToolkitErrorHandler, which will cause
                            // the StackOverflowError when the next X11 error occurs.
                            // Workaround: restore the default error handler.
                            // Also update XToolkit.saved_error_handler so that
                            // this is done only once.
                            setErrorHandlerMethod.invoke(null, new Object[]{defaultErrorHandler});
                            savedErrorHandlerField.setLong(null, ((Long) defaultErrorHandler).longValue());
                        }
                    } catch (Exception ex) {
                        // ignore
                    }

                }
            });
            timer.start();
        } catch (Exception ex) {
            // ignore
        }
    }

    /**
     * Our startup code
     */
    private static class Startup implements Runnable {

        ControlCenter center;
        WindowManager winMgr;

        /**
         * Constructor
         */
        public void run() {

//    	SplashWindow sw = new SplashWindow("genjfrsplash",new JFrame(),10000);

            // Catch anything that might happen
            try {

                // create our home directory
                File home = new File(EnvironmentChecker.getProperty(App.class, "user.home.genj", null, "determining home directory"));
                home.mkdirs();
                if (!home.exists() || !home.isDirectory()) {
                    throw new IOException("Can't initialize home directoy " + home);
                }

                // prepare our master log and own LogManager for GenJ
                LOG = Logger.getLogger("genj");

                // prepare some basic logging for now
                Formatter formatter = new LogFormatter();
                setLogLevel("INFO");


//        Handler[] handlers = root.getHandlers();
//        for (int i=0;i<handlers.length;i++) root.removeHandler(handlers[i]);
//        BufferedHandler bufferedLogHandler = new BufferedHandler();
//        root.addHandler(bufferedLogHandler);
//        root.addHandler(new FlushingHandler(new StreamHandler(System.out, formatter)));
                System.setOut(new PrintStream(new LogOutputStream(Level.INFO, "System", "out")));
                System.setErr(new PrintStream(new LogOutputStream(Level.WARNING, "System", "err")));

                // Log is up
                LOG.info("\n\n==================8<================================================================");
                LOG.info("Startup");

                // init our data (file user.home.genj/genj.properties is read and properties are stored into registry)
//                REGISTRY = checkOptionsWizard(REGISTRY);
                if (NbPreferences.forModule(App.class).get("optionswizard", "").equals("3")) {
                    putRegistryFromSettings(REGISTRY);
                }
                // Pour le moment il ne faut pas que je lnf soit mis a autre chose que java.
                // ni pas les options, ni par l'assistant.
                // On maintient la possibilite de changer mais le lnf reel utilise pas l'appli est java
                REGISTRY.put("options.genj.app.Options.lookAndFeel", "1");
                REGISTRY.persist();
                REGISTRY = new Registry("genj");
                // initialize options first (creates a registry view within the above registry only containing the options)
                OptionProvider.getAllOptions(REGISTRY);

                // Setup File Logging and check environment
                LOGFILE = new File(home, "genjfr.log");
                Handler handler = new FileHandler(LOGFILE.getAbsolutePath(), Options.getInstance().getMaxLogSizeKB() * 1024, 1, true);
                handler.setLevel(Level.ALL);
                handler.setFormatter(formatter);
                LOG.addHandler(handler);
                
                // Priorite sur le parametre passe en ligne de commande
                if (System.getProperty("genj.debug.level") != null) {
                    setLogLevel(System.getProperty("genj.debug.level"));
                } else {
                    setLogLevel((NbPreferences.forModule(App.class).get("logLevel","")));
                }

//        root.removeHandler(bufferedLogHandler);
//        bufferedLogHandler.flush(handler);

                // Startup Information
                LOG.info("version = " + Version.getInstance().getBuildString());
                LOG.info("date = " + new Date());
                EnvironmentChecker.log();

                // patch up GenJ for Mac if applicable
                if (EnvironmentChecker.isMac()) {
                    LOG.info("Setting up MacOs adjustments");
                    System.setProperty("apple.laf.useScreenMenuBar", "true");
                    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "GenealogyJ");
                }

                // check VM version
                if (!EnvironmentChecker.isJava14(App.class)) {
                    if (EnvironmentChecker.getProperty(App.class, "genj.forcevm", null, "Check force of VM") == null) {
                        LOG.severe("Need Java 1.4 to run GenJ");
                        System.exit(1);
                        return;
                    }
                }

                // get app resources now
                Resources resources = Resources.get(App.class);

                // create window manager
//        WindowManager
                winMgr = new GenjFrWindowManager(new Registry(REGISTRY, "window"), Gedcom.getImage());
                WindowManager.setDefaultWm(winMgr);

                // Disclaimer - check version and registry value
//        String version = Version.getInstance().getVersionString();
//        if (!version.equals(registry.get("disclaimer",""))) {
//          // keep it
//          registry.put("disclaimer", version);
//          // show disclaimer
//          winMgr.openDialog("disclaimer", "Disclaimer", WindowManager.INFORMATION_MESSAGE, resources.getString("app.disclaimer"), Action2.okOnly(), null);
//        }

                // setup control center
                center = new ControlCenter(REGISTRY, winMgr, new Shutdown(REGISTRY));

                // show it
//        winMgr.openWindow("cc", resources.getString("app.title"), Gedcom.getImage(), center, center.getMenuBar(), center.getExitAction());
//FIXME: ne semble plus servir a rien                winMgr.openWindow("cc", resources.getString("app.title"), Gedcom.getImage(), center, null, null);

                // done
                LOG.info("/Startup");

            } catch (Throwable t) {
                LOG.log(Level.SEVERE, "Cannot instantiate App", t);
                System.exit(1);
                return;
            }

            synchronized (this) {
                notifyAll();
            }

        }

    } //Startup

    /**
     * Our shutdown code
     */
    private static class Shutdown implements Runnable {

        private Registry registry;

        /**
         * Constructor
         */
        private Shutdown(Registry registry) {
            this.registry = registry;
        }

        /**
         * do the shutdown
         */
        public void run() {
            close();
        }
    } //Shutdown

    public static void setLogLevel(String logLevel) {
        // prepare some basic logging for now
        Logger root = Logger.getLogger("");
        Level level = Level.INFO;

        try {
            // allow command line override of debug level - set non-genj level a tad higher
            level = Level.parse(logLevel);
        } catch (Throwable t) {
        }
        LOG.setLevel(level);
        if (level.intValue() < Level.CONFIG.intValue()) {
            root.setLevel(Level.CONFIG);
        } else {
            root.setLevel(level);
        }
    }
    /**
     * a log handler that buffers
     */
    private static class BufferedHandler extends Handler {

        private List<LogRecord> buffer = new ArrayList<LogRecord>();

        @Override
        public void close() throws SecurityException {
            // noop
        }

        @Override
        public void flush() {
        }

        private void flush(Handler other) {
            for (LogRecord record : buffer) {
                other.publish(record);
            }
            buffer.clear();
        }

        @Override
        public void publish(LogRecord record) {
            buffer.add(record);
        }
    }

    /**
     * a log handler that flushes on publish
     */
    private static class FlushingHandler extends Handler {

        private Handler wrapped;

        private FlushingHandler(Handler wrapped) {
            this.wrapped = wrapped;
            wrapped.setLevel(Level.ALL);
            setLevel(Level.ALL);
        }

        public void publish(LogRecord record) {
            wrapped.publish(record);
            flush();
        }

        public void flush() {
            wrapped.flush();
        }

        public void close() throws SecurityException {
            flush();
            wrapped.close();
        }
    }

    /**
     * Our own log format
     */
    private static class LogFormatter extends Formatter {

        public String format(LogRecord record) {
            StringBuffer result = new StringBuffer(80);
            result.append(record.getLevel());
            result.append(":");
            result.append(record.getSourceClassName());
            result.append(".");
            result.append(record.getSourceMethodName());
            result.append(":");
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

    public static void putRegistryFromSettings(Registry registry) {
        registry.put("options.genj.app.Options.language", NbPreferences.forModule(App.class).get("language", ""));
        registry.put("options.genj.app.Options.lookAndFeel", NbPreferences.forModule(App.class).get("skin", ""));
        registry.put("options.genj.app.Options.isRestoreViews", NbPreferences.forModule(App.class).get("restoreWindows", ""));
        registry.put("options.genj.edit.Options.isAutoCommit", NbPreferences.forModule(App.class).get("autoCommit", ""));
        registry.put("options.genj.gedcom.Options.numberOfUndos", NbPreferences.forModule(App.class).get("undos", ""));
        registry.put("options.genj.edit.Options.isSplitJurisdictions", NbPreferences.forModule(App.class).get("splitJurisdiction", ""));
        registry.put("options.genj.edit.Options.isOpenEditor", NbPreferences.forModule(App.class).get("OpenEditor", ""));

        registry.put("options.genj.report.Options.birthSymbol", NbPreferences.forModule(App.class).get("symbolBirth", ""));
        registry.put("options.genj.report.Options.baptismSymbol", NbPreferences.forModule(App.class).get("symbolBapm", ""));
        registry.put("options.genj.report.Options.childOfSymbol", NbPreferences.forModule(App.class).get("symbolChildOf", ""));
        registry.put("options.genj.report.Options.engagingSymbol", NbPreferences.forModule(App.class).get("symbolEngm", ""));
        registry.put("options.genj.gedcom.Options.txtMarriageSymbol", NbPreferences.forModule(App.class).get("symbolMarr", ""));
        registry.put("options.genj.report.Options.divorceSymbol", NbPreferences.forModule(App.class).get("symbolDivc", ""));
        registry.put("options.genj.report.Options.occuSymbol", NbPreferences.forModule(App.class).get("symbolOccu", ""));
        registry.put("options.genj.report.Options.resiSymbol", NbPreferences.forModule(App.class).get("symbolResi", ""));
        registry.put("options.genj.report.Options.deathSymbol", NbPreferences.forModule(App.class).get("symbolDeat", ""));
        registry.put("options.genj.report.Options.burialSymbol", NbPreferences.forModule(App.class).get("symbolBuri", ""));
        registry.put("options.genj.gedcom.Options.maskPrivate", NbPreferences.forModule(App.class).get("privDisplay", ""));
        registry.put("options.genj.report.Options.privateTag", NbPreferences.forModule(App.class).get("privFlag", ""));
        registry.put("options.genj.report.Options.deceasedIsPublic", NbPreferences.forModule(App.class).get("privAlive", ""));
        registry.put("options.genj.report.Options.yearsEventsArePrivate", NbPreferences.forModule(App.class).get("privYears", ""));
        registry.put("options.genj.gedcom.Options.valueLineBreak", NbPreferences.forModule(App.class).get("txtLineBreak", ""));
        registry.put("options.genj.gedcom.Options.maxImageFileSizeKB", NbPreferences.forModule(App.class).get("imageSize", ""));
        registry.put("options.genj.gedcom.Options.nameFormat", NbPreferences.forModule(App.class).get("displayNames", ""));
        registry.put("options.genj.gedcom.Options.dateFormat", NbPreferences.forModule(App.class).get("displayDates", ""));

        registry.put("options.genj.gedcom.Options.submName", NbPreferences.forModule(App.class).get("submName", ""));
        registry.put("options.genj.gedcom.Options.submCity", NbPreferences.forModule(App.class).get("submCity", ""));
        registry.put("options.genj.gedcom.Options.submPhone", NbPreferences.forModule(App.class).get("submPhone", ""));
        registry.put("options.genj.gedcom.Options.submPostCode", NbPreferences.forModule(App.class).get("submPostCode", ""));
        registry.put("options.genj.gedcom.Options.submEmail", NbPreferences.forModule(App.class).get("submEmail", ""));
        registry.put("options.genj.gedcom.Options.submCountry", NbPreferences.forModule(App.class).get("submCountry", ""));
        registry.put("options.genj.gedcom.Options.submWeb", NbPreferences.forModule(App.class).get("submWeb", ""));
        registry.put("options.genj.gedcom.Options.isUpperCaseNames", NbPreferences.forModule(App.class).get("NamesUppercase", ""));
        registry.put("options.genj.gedcom.Options.setWifeLastname", NbPreferences.forModule(App.class).get("NamesSpouse", ""));
        registry.put("options.genj.gedcom.Options.fmt_address1", NbPreferences.forModule(App.class).get("fmt_address1", ""));
        registry.put("options.genj.gedcom.Options.fmt_address2", NbPreferences.forModule(App.class).get("fmt_address2", ""));
        registry.put("options.genj.gedcom.Options.fmt_address3", NbPreferences.forModule(App.class).get("fmt_address3", ""));
        registry.put("options.genj.gedcom.Options.fmt_address4", NbPreferences.forModule(App.class).get("fmt_address4", ""));
        registry.put("options.genj.gedcom.Options.fmt_address5", NbPreferences.forModule(App.class).get("fmt_address5", ""));
        registry.put("options.genj.gedcom.Options.fmt_address6", NbPreferences.forModule(App.class).get("fmt_address6", ""));
        registry.put("options.genj.gedcom.Options.fmt_address7", NbPreferences.forModule(App.class).get("fmt_address7", ""));
        registry.put("options.genj.gedcom.Options.fmt_address1_mand", NbPreferences.forModule(App.class).get("fmt_address1_mand", ""));
        registry.put("options.genj.gedcom.Options.fmt_address2_mand", NbPreferences.forModule(App.class).get("fmt_address2_mand", ""));
        registry.put("options.genj.gedcom.Options.fmt_address3_mand", NbPreferences.forModule(App.class).get("fmt_address3_mand", ""));
        registry.put("options.genj.gedcom.Options.fmt_address4_mand", NbPreferences.forModule(App.class).get("fmt_address4_mand", ""));
        registry.put("options.genj.gedcom.Options.fmt_address5_mand", NbPreferences.forModule(App.class).get("fmt_address5_mand", ""));
        registry.put("options.genj.gedcom.Options.fmt_address6_mand", NbPreferences.forModule(App.class).get("fmt_address6_mand", ""));
        registry.put("options.genj.gedcom.Options.fmt_address7_mand", NbPreferences.forModule(App.class).get("fmt_address7_mand", ""));
        registry.put("options.genj.gedcom.Options.isUseSpacedPlaces", NbPreferences.forModule(App.class).get("address_splitspaces", ""));
        registry.put("options.genj.gedcom.Options.isFillGapsInIDs", NbPreferences.forModule(App.class).get("IDFilling", ""));
        registry.put("options.genj.gedcom.Options.defaultEncoding", NbPreferences.forModule(App.class).get("encoding", ""));
        registry.put("options.genj.app.Options.isWriteBOM", NbPreferences.forModule(App.class).get("BOM", ""));

        registry.put("options.genj.gedcom.Options.gedcomFile", NbPreferences.forModule(App.class).get("gedcomFile", ""));
        registry.put("options.genj.gedcom.Options.reportDir", NbPreferences.forModule(App.class).get("reportDir", ""));
        registry.put("options.associations", NbPreferences.forModule(App.class).get("6", ""));
        registry.put("options.associations.1", NbPreferences.forModule(App.class).get("assoTxt", ""));
        registry.put("options.associations.2", NbPreferences.forModule(App.class).get("assoOffice", ""));
        registry.put("options.associations.3", NbPreferences.forModule(App.class).get("assoAdobe", ""));
        registry.put("options.associations.4", NbPreferences.forModule(App.class).get("assoImages", ""));
        registry.put("options.associations.5", NbPreferences.forModule(App.class).get("assoSound", ""));
        registry.put("options.associations.6", NbPreferences.forModule(App.class).get("assoWeb", ""));
        registry.put("options.genj.app.Options.maxLogSizeKB", NbPreferences.forModule(App.class).get("logSize", ""));
    }

    /**
     * Exiting the application automatically saves modes stored in memory
     * In case of restart after Wizard/Input or Options/Input, modes that were imported by the user are unfortunatelly overwritten
     * This saves them to a backup directory before they get overwritten
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
     * Exiting the application automatically saves modes stored in memory
     * In case of restart after Wizard/Input or Options/Input, modes that were imported by the user are unfortunatelly overwritten
     * This reloads them from a backup directory after they have been overwritten so that next start uses the modes that were imported
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
        String restartFlagFileStr = System.getProperty("netbeans.user") + File.separator + "var" + File.separator + "restart";
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

} //App

