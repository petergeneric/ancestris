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
package genj.app;

import genj.Version;
import genj.gedcom.Gedcom;
import genj.option.OptionProvider;
import genj.util.EnvironmentChecker;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.window.DefaultWindowManager;
import genj.window.WindowManager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
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

import javax.swing.SwingUtilities;

import sun.rmi.log.LogHandler;

/**
 * Main Class for GenJ Application
 */
public class App {
  
  /*package*/ static Logger LOG;
  
  /*package*/ static File LOGFILE; 
  
  private static Startup startup;
  
  /**
   * GenJ Main Method
   */
  public static void main(final String[] args) {
    
    // we're ready to be run twice
    synchronized (App.class) {
      if (startup==null)  {
        // run startup
        startup = new Startup();
        SwingUtilities.invokeLater(startup);
      }
    }
    
    // wait for startup do be done
    synchronized (startup) {
      if (startup.center==null) try {
        startup.wait();
      } catch (InterruptedException e) {
      }
    }

  // load
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        startup.center.load(args);
      }
    } );
    
  }
  
  /**
   * Our startup code
   */
  private static class Startup implements Runnable {
    
    ControlCenter center;

    /**
     * Constructor
     */
    public void run() {
      
      // Catch anything that might happen
      try {
        
        // create our home directory
        File home = new File(EnvironmentChecker.getProperty(App.class, "user.home.genj", null, "determining home directory"));
        home.mkdirs();
        if (!home.exists()||!home.isDirectory()) 
          throw new IOException("Can't initialize home directoy "+home);
        
        // prepare our master log and own LogManager for GenJ
        LOG = Logger.getLogger("genj");
        
        // prepare some basic logging for now
        Formatter formatter = new LogFormatter();
        Logger root = Logger.getLogger("");
        
        try {
          // allow command line override of debug level - set non-genj level a tad higher
          Level level = Level.parse(System.getProperty("genj.debug.level"));
          LOG.setLevel(level);
          if (Integer.MAX_VALUE!=level.intValue())
            root.setLevel(new Level("genj.debug.level+1", level.intValue()+1) {} );
        } catch (Throwable t) {
        }
        
        Handler[] handlers = root.getHandlers();
        for (int i=0;i<handlers.length;i++) root.removeHandler(handlers[i]);
        BufferedHandler bufferedLogHandler = new BufferedHandler();
        root.addHandler(bufferedLogHandler);
        root.addHandler(new FlushingHandler(new StreamHandler(System.out, formatter)));
        System.setOut(new PrintStream(new LogOutputStream(Level.INFO, "System", "out")));
        System.setErr(new PrintStream(new LogOutputStream(Level.WARNING, "System", "err")));

        // Log is up
        LOG.info("Startup");
        
        // init our data
        Registry registry = new Registry("genj");
        
        // initialize options first
        OptionProvider.getAllOptions(registry);
        
        // Setup File Logging and check environment
        LOGFILE = new File(home, "genj.log");
        Handler handler = new FileHandler(LOGFILE.getAbsolutePath(), Options.getInstance().getMaxLogSizeKB()*1024, 1, true);
        handler.setLevel(Level.ALL);
        handler.setFormatter(formatter);
        LOG.addHandler(handler);
        root.removeHandler(bufferedLogHandler);
        bufferedLogHandler.flush(handler);
        
        // Startup Information
        LOG.info("version = "+Version.getInstance().getBuildString());
        LOG.info("date = "+new Date());
        EnvironmentChecker.log();
        
        // patch up GenJ for Mac if applicable
        if (EnvironmentChecker.isMac()) {
          LOG.info("Setting up MacOs adjustments");
          System.setProperty("apple.laf.useScreenMenuBar","true");
          System.setProperty("com.apple.mrj.application.apple.menu.about.name","GenealogyJ");
        }
        
        // check VM version
        if (!EnvironmentChecker.isJava14(App.class)) {
          if (EnvironmentChecker.getProperty(App.class, "genj.forcevm", null, "Check force of VM")==null) {
            LOG.severe("Need Java 1.4 to run GenJ");
            System.exit(1);
            return;
          }
        }
        
        // get app resources now
        Resources resources = Resources.get(App.class);
  
        // create window manager
        WindowManager winMgr = new DefaultWindowManager(new Registry(registry, "window"), Gedcom.getImage());
        
        // Disclaimer - check version and registry value
        String version = Version.getInstance().getVersionString();
        if (!version.equals(registry.get("disclaimer",""))) {
          // keep it      
          registry.put("disclaimer", version);
          // show disclaimer
          winMgr.openDialog("disclaimer", "Disclaimer", WindowManager.INFORMATION_MESSAGE, resources.getString("app.disclaimer"), Action2.okOnly(), null);    
        }
        
        // setup control center
        center = new ControlCenter(registry, winMgr, new Shutdown(registry));
  
        // show it
        winMgr.openWindow("cc", resources.getString("app.title"), Gedcom.getImage(), center, center.getMenuBar(), center.getExitAction());
  
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
      LOG.info("Shutdown");
	    // persist options
	    OptionProvider.persistAll(registry);
	    // Store registry 
	    Registry.persist();      
	    // done
      LOG.info("/Shutdown");
      // let VM do it's thing
      System.exit(0);
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
      for (LogRecord record : buffer)
        other.publish(record);
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
      if (parms==null||parms.length==0)
        result.append(record.getMessage());
      else 
        result.append(MessageFormat.format(msg, parms));
      result.append(System.getProperty("line.separator"));

      if (record.getThrown()!= null) {
        
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
      if (b!='\n') {
       buffer[size++] = (char)b;
       if (size<buffer.length) 
         return;
      }
      flush();
    }

    /**
     * 
     */
    public void flush() throws IOException {
      if (size>0) {
        LOG.logp(level, sourceClass, sourceMethod, String.valueOf(buffer, 0, size).trim());
        size = 0;
      }
    }
  }
    
} //App
