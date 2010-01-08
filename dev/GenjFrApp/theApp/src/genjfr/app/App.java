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
import genj.util.swing.Action2;
import genj.window.GenjFrWindowManager;
import genj.window.WindowManager;

import genjfr.app.pluginservice.PluginInterface;
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
import java.lang.reflect.InvocationTargetException;
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
import java.util.logging.StreamHandler;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Main Class for GenJ Application
 */
public class App {

    /*package*/ static Logger LOG;

    /*package*/ static File LOGFILE;
    private static Startup startup;
    public static ControlCenter center;
    public static WindowManager genjWindowManager;
    private static Shutdown shutDownTask;
    private static boolean x11ErrorHandlerFixInstalled = false;

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

    public static void shutDown() {
        shutDownTask.run();
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
                Logger root = Logger.getLogger("");

                try {
                    // allow command line override of debug level - set non-genj level a tad higher
                    Level level = Level.parse(System.getProperty("genj.debug.level"));
                    LOG.setLevel(level);
                    if (Integer.MAX_VALUE != level.intValue()) {
                        root.setLevel(new Level("genj.debug.level+1", level.intValue() + 1) {
                        });
                    }
                } catch (Throwable t) {
                }

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
                Registry registry = new Registry("genj");
                registry = checkOptionsWizard(registry);

                // initialize options first (creates a registry view within the above registry only containing the options)
                OptionProvider.getAllOptions(registry);

                // Setup File Logging and check environment
                LOGFILE = new File(home, "genj.log");
                Handler handler = new FileHandler(LOGFILE.getAbsolutePath(), Options.getInstance().getMaxLogSizeKB() * 1024, 1, true);
                handler.setLevel(Level.ALL);
                handler.setFormatter(formatter);
                LOG.addHandler(handler);
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
                winMgr = new GenjFrWindowManager(new Registry(registry, "window"), Gedcom.getImage());
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
                shutDownTask = new Shutdown(registry);
                center = new ControlCenter(registry, winMgr, new Shutdown(registry));

                // show it
//        winMgr.openWindow("cc", resources.getString("app.title"), Gedcom.getImage(), center, center.getMenuBar(), center.getExitAction());
                winMgr.openWindow("cc", resources.getString("app.title"), Gedcom.getImage(), center, null, null);

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

        /**
         * Launches Wizard for the options if never done and the module exists
         * 
         * @param registry
         * @return
         */
        private Registry checkOptionsWizard(Registry registry) {

            // Check if options wizard has ever been run (when it has, there is a property set in genj.properties)
            if (registry == null) {
                return null;
            }
            String done = registry.get("optionswizard", "");
 done = "0"; // do not forget to remove this line !
            if (done.equals("1")) {
                return registry;
            }

            // Lookup wizard module (it actually loads all the modules corresponding to PluginInterface)
            PluginInterface pi = null;
            for (PluginInterface sInterface : Lookup.getDefault().lookupAll(PluginInterface.class)) {
                System.out.println("Plugin " + sInterface.getPluginName() + " loaded successfully.");
                if (sInterface.getPluginName().equals("OptionsWizard")) {
                    pi = sInterface;
                    break;
                }
            }

            // Run wizard module when found
            // Also reload registry because the wizard does save a new set of options
            if (pi != null) {
                System.out.println("Launching Wizard...");
                if (pi.launchModule(registry)) {
                    registry.put("optionswizard", "1");
                    Registry.persist();
                    registry = new Registry("genj");
                }
            }

            return registry;

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
            LOG.info("Shutdown");
            // persist options
            OptionProvider.persistAll(registry);
            // Store registry
            Registry.persist();
            // done
            LOG.info("/Shutdown");
            // let VM do it's thing
//            System.exit(0);
            // done
        }
    } //Shutdown

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
} //App

