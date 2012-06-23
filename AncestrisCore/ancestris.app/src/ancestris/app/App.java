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
package ancestris.app;

import genj.Version;
import genj.option.OptionProvider;
import genj.util.EnvironmentChecker;
import genj.util.Registry;
import genj.util.swing.DialogHelper;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.util.DialogManagerImp;

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
import java.util.Date;
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

/**
 * Main Class for GenJ Application
 */
public class App {

    /*package*/ static Logger LOG;

    /*package*/ static File LOGFILE;
    private static Startup startup;
    public static ControlCenter center;
//    public static WorkbenchHelper workbenchHelper;

    private static boolean x11ErrorHandlerFixInstalled = false;

    private static AppPlugin appplugin = new AppPlugin();

    public static String getPluginVersion() {
        return appplugin.getPluginVersion();
    }

    public static String getPluginShortDescription() {
        return appplugin.getPluginShortDescription();
    }

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
            if (center == null) {
                try {
                    startup.wait();
                } catch (InterruptedException e) {
                }
            }
        }

        if (!x11ErrorHandlerFixInstalled && !EnvironmentChecker.isMac() && !EnvironmentChecker.isWindows()) {
            x11ErrorHandlerFixInstalled = true;
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    initX11ErrorHandlerFix();
                }
            });
        }
        /**
         * Workaround for http://bugs.sun.com/view_bug.do?bug_id=5076635
         * reflection ne fonctionne pas: http://forums.oracle.com/forums/thread.jspa?messageID=5220729&#5220729
         * voir http://forums.oracle.com/forums/thread.jspa?messageID=5208759&#5208759
         */
//        if (!EnvironmentChecker.isMac() && !EnvironmentChecker.isWindows()) {
//            try {
//                // Can't use Toolkit.getDefaultToolkit().setDesktopProperty("awt.multiClickInterval", new Integer(450));
//                // as it is protected
//                Method setDesktopPropertyMeth = Toolkit.class.getDeclaredMethod("setDesktopProperty", String.class,Object.class);
//                setDesktopPropertyMeth.setAccessible(true);
//                setDesktopPropertyMeth.invoke(Toolkit.getDefaultToolkit(), "awt.multiClickInterval",new Integer(450));
//            } catch (NoSuchMethodException ex) {
//                Exceptions.printStackTrace(ex);
//            } catch (SecurityException ex) {
//                Exceptions.printStackTrace(ex);
//            } catch (IllegalAccessException ex) {
//                Exceptions.printStackTrace(ex);
//            } catch (IllegalArgumentException ex) {
//                Exceptions.printStackTrace(ex);
//            } catch (InvocationTargetException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
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
        // persist options
        OptionProvider.persistAll();
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
    @SuppressWarnings("unchecked")
    private static void initX11ErrorHandlerFix() {
        assert EventQueue.isDispatchThread();

        try {
            // get XlibWrapper.SetToolkitErrorHandler() and XSetErrorHandler() methods
            Class<?> xlibwrapperClass = Class.forName("sun.awt.X11.XlibWrapper");
            final Method setToolkitErrorHandlerMethod = xlibwrapperClass.getDeclaredMethod("SetToolkitErrorHandler", (Class[])null);
            final Method setErrorHandlerMethod = xlibwrapperClass.getDeclaredMethod("XSetErrorHandler", new Class<?>[]{Long.TYPE});
            setToolkitErrorHandlerMethod.setAccessible(true);
            setErrorHandlerMethod.setAccessible(true);

            // get XToolkit.saved_error_handler field
            Class<?> xtoolkitClass = Class.forName("sun.awt.X11.XToolkit");
            final Field savedErrorHandlerField = xtoolkitClass.getDeclaredField("saved_error_handler");
            savedErrorHandlerField.setAccessible(true);

            // determine the current error handler and the value of XLibWrapper.ToolkitErrorHandler
            // (XlibWrapper.SetToolkitErrorHandler() sets the X11 error handler to
            // XLibWrapper.ToolkitErrorHandler and returns the old error handler)
            final Object defaultErrorHandler = setToolkitErrorHandlerMethod.invoke(null, (Object[])null);
            final Object toolkitErrorHandler = setToolkitErrorHandlerMethod.invoke(null, (Object[])null);
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

        /**
         * Constructor
         */
        public void run() {

            // Catch anything that might happen
            try {

                // prepare our master log and own LogManager for GenJ
                LOG = Logger.getLogger("genj");

                // Install our dialog handler
                DialogHelper.setDialogManager(DialogManagerImp.getInstance());

                // create our home directory
                File home = new File(EnvironmentChecker.getProperty("user.home.ancestris", null, "determining home directory"));
                home.mkdirs();
                if (!home.exists() || !home.isDirectory()) {
                    throw new IOException("Can't initialize home directoy " + home);
                }


                // prepare some basic logging for now
                Formatter formatter = new LogFormatter();
                setLogLevel("INFO");

                System.setOut(new PrintStream(new LogOutputStream(Level.INFO, "System", "out")));
                System.setErr(new PrintStream(new LogOutputStream(Level.WARNING, "System", "err")));

                // initialize options first (creates a registry view within the above registry only containing the options)
                OptionProvider.getAllOptions();

                // Setup File Logging and check environment
                LOGFILE = new File(home, "ancestris.log");
                Handler handler = new FileHandler(LOGFILE.getAbsolutePath(), Options.getMaxLogSizeKB() * 1024, 1, true);
                handler.setLevel(Level.ALL);
                handler.setFormatter(formatter);
                LOG.addHandler(handler);
                
                // Log is up
                LOG.info("\n\n==================8<================================================================");
                LOG.info("Startup");

                // Priorite sur le parametre passe en ligne de commande
                if (System.getProperty("ancestris.debug.level") != null) {
                    setLogLevel(System.getProperty("ancestris.debug.level"));
                } else {
                    setLogLevel((Registry.get(App.class).get("logLevel","")));
                }

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
//TODO: demander une version >1.6 dans NB


                // setup control center
                center = new ControlCenter();


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
        Logger.getLogger("ancestris").setLevel(level);
        if (level.intValue() < Level.CONFIG.intValue()) {
            root.setLevel(Level.CONFIG);
        } else {
            root.setLevel(level);
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

  private static class AppPlugin extends AncestrisPlugin{
  }

} //App

