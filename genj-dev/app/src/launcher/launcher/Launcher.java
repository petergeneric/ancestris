package launcher;
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
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.BindException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import launcher.ipc.CallHandler;
import launcher.ipc.Client;
import launcher.ipc.Server;

/**
 * A program starter that reads a classpath and runnable type from manifest-file. The
 * comma-separated classpath is expanded (each entry that is a directory is scanned
 * for jar-files) and the runnable invoked with the resulting classpath.
 * The launcher can also be configured to pass program arguments to  an already
 * running instance of Run-Class by specifying a port to commincate to (If there is
 * no Launcher already running the current Launcher will be host for other launches
 * until exited).
 * 
 * An applicable manifest file should roughly look like this example: META-INF/MANIFEST.MF
 * 
 * <pre>
 *   Main-Class: launcher.Launcher
 *   Run-Classpath: ./lib
 *   Run-Class: type of class with main(String[]) method to run 
 *   Run-IPC: some port number
 * </pre>
 *   
 * The manifest entry Main-Class makes sure that Run can be started via
 * <pre>
 *   java -jar thejar.jar
 * </pre>
 */

/*
      // Check if we can talk to another GenJ instance
      int port = 2505;
      try {
        StringBuffer msg = new StringBuffer();
        for (int i = 0; i < args.length; i++) msg.append(args[i]).append("\n");
        msg.append("\n");
        if (!"OK".equals(new Client().send(port, msg.toString()))) 
          throw new RuntimeException("no ack");
        LOG.fine("Sent application arguments to 2nd Genj instance");
        return;
      } catch (Throwable t) {
        LOG.log(Level.FINE, "No 2nd GenJ instance willing to talk", t);
      }
      
      // Be the first instance of GenJ 
      try {
        new Server(port, new CallHandler() {
          public String handleCall(String msg) {
            StringTokenizer lines = new StringTokenizer(msg, "\n");
            while (lines.hasMoreTokens()) 
              LOG.info("got message from 2nd GenJ instance: "+lines.nextToken());
            return "OK";
          }
        });
      } catch (IOException e) {
        LOG.log(Level.WARNING, "couldn't register as 1st GenJ listening on port "+port, e);
      }


 */

public class Launcher {

  private final static Logger LOG = Logger.getLogger("launcher");
  
  private static Manifest manifest;
  private static Method main;
  
  public final static String 
    MANIFEST = "META-INF/MANIFEST.MF",
    LAUNCH_CLASSPATH = "Launch-Classpath",
    LAUNCH_CLASS = "Launch-Class",
    LAUNCH_PORT = "Launch-Port";
  
  /**
   * Launcher's main
   */
  public static void main(String[] args) {
    
    try {
      
      // setup IPC connection
      if (!setupIPC(args))
        return;
      
      // cd into 'current' directoruy
      cd(Launcher.class);
      
      // call main
      callMain(args);
      
    } catch (Throwable t) {
      t.printStackTrace(System.err);
    }

    // nothing more to do here
  }
  
  /**
   * call the main method of the class being launched
   */
  private static void callMain(String[] args) throws Exception {

    // get access to main method?
    if (main==null) {

        // prepare classpath
      String[] classpath = getLaunchClasspath();
      
      // tell everyone about it
      setClasspath(classpath);
      
      // prepare classloader
      ClassLoader cl  = getClassLoader(classpath);
  
      // instantiate class and run main
      Thread.currentThread().setContextClassLoader(cl);
      Class clazz = cl.loadClass( getLaunchClass());
      main = clazz.getMethod("main", new Class[]{String[].class});
      
    }
    
    // do it
    main.invoke(null, new Object[]{args});
    
  }
  
  /**
   * Setup IPC communication to other launcher instance that can handle a launch
   * @return whether to continue into launch or
   */
  private static boolean setupIPC(String[] args) {
    
    final String launchClass = getLaunchClass();
    int port = getLaunchPort();
    
    // check for a server
    if (port>0) {
      
      // try to connect to published port
      int published = -1;
      try {
        
        published = Preferences.userNodeForPackage(Launcher.class).getInt(launchClass, 0);
        if (published>0) {
          
          // assemble message
          if ("OK".equals(new Client().send(published, encode(args)))) {
            LOG.log(Level.FINE, "sent launch to server on port "+published);
            // we're done!
            return false;
          }
        }
      } catch (Throwable t) {
        LOG.log(Level.FINE, "couldn't send launch to server on port "+published);
      }
      
      // become a server with default port
      try {
        
        CallHandler handler = new CallHandler() {
          public String handleCall(String msg) {
            try {
              callMain(decode(msg));
            } catch (Throwable t) {
              return "ERR";
            }
            return "OK";
          }
        };
        
        for (int i=0;i<10;port++,i++) {
          try {
            new Server(port, handler);
            break;
          } catch (BindException e) {
            LOG.log(Level.FINE, "couldn't bind server to port "+port);
          }
        }
        
        Preferences.userNodeForPackage(Launcher.class).putInt(launchClass, port);
        
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() { 
          public void run() {
            Preferences.userNodeForPackage(Launcher.class).putInt(launchClass, 0);
          }
        }));
      } catch (Throwable t) {
        LOG.log(Level.WARNING, "cannot become launch server", t);
      }
    }    
    
    // continue with launch
    return true;
  }
  
  private static String[] decode(String msg) {
    StringTokenizer lines = new StringTokenizer(msg, "\n");
    String[] args = new String[lines.countTokens()];
    for (int i = 0; i < args.length; i++) 
      args[i] = lines.nextToken();
    return args;
  }
  
  private static String encode(String[] args) {
    StringBuffer msg = new StringBuffer();
    for (int i = 0; i < args.length; i++) {
      if (i>0) msg.append("\n");
      msg.append(args[i]);
    }
    msg.append("\n\n");
    return msg.toString();
  }

  /**
   * Try to change into the directory of jar file that contains given class.
   * @param  clazz  class to get containing jar file for
   * @return success or not 
   */
  private static boolean cd(Class clazz) {
    
    try {         
      
      // jar:file:/C:/Program Files/FooMatic/./lib/foo.jar!/foo/Bar.class
      JarURLConnection jarCon = (JarURLConnection)getClassURL(clazz).openConnection();

      // file:/C:/Program Files/FooMatic/./lib/foo.jar
      URL jarUrl = jarCon.getJarFileURL();
  
      // /C:/Program Files/FooMatic/./lib/foo.jar
      File jarFile = new File(URLDecoder.decode(jarUrl.getPath(), "UTF-8"));   
      
      // /C:/Program Files/FooMatic/./lib
      File jarDir = jarFile.getParentFile();

      // cd C:/Program Files/FooMatic/.
      LOG.info(System.getProperty("user.dir"));
      System.setProperty("user.dir", jarDir.getAbsolutePath());
      LOG.info(System.getProperty("user.dir"));

      // done
      return true;
      
    } catch (Exception ex) {
      // didn't work
      LOG.log(Level.WARNING, "couldn't cd into directory with jar containing "+clazz, ex);
      return false;
    }

  }
  
  /**
   * Get URL of the given class.
   * 
   * @param  clazz  class to get URL for
   * @return the URL this class was loaded from
   */
  private static URL getClassURL(Class clazz) {
    String resourceName = "/" + clazz.getName().replace('.', '/') + ".class";
    return clazz.getResource(resourceName);
  }
  
  /**
   * Get launch port attribute from manifest file information
   */
  private static int getLaunchPort() {
  try {
      return Integer.parseInt(System.getProperty("launch.port"));
    } catch (Throwable t) {
    }
    try {
      return Integer.parseInt(getManifest().getMainAttributes().getValue(LAUNCH_PORT));
    } catch (Throwable t) {
      return 0;
    }
  }  
  
  
 /**
   * Get main class from manifest file information
   */
  private static String getLaunchClass() {

    String clazz = System.getProperty("launch.class");
    if (clazz==null)
      clazz = getManifest().getMainAttributes().getValue(LAUNCH_CLASS);
    if (clazz == null || clazz.length() == 0) 
      throw new Error("No " + LAUNCH_CLASS + " defined in " + MANIFEST);
    
    return clazz;
  }  
  
  /**
   * create classloader
   */
  private static ClassLoader getClassLoader(String[] classpath) throws MalformedURLException {
    
    URL[] urls = new URL[classpath.length];
    for (int i = 0; i < urls.length; i++) {
      urls[i] = new File(classpath[i]).toURI().toURL();
    }
    
    return new URLClassLoader(urls);
  }
  
  /**
   * Set java.class.path
   */
  private static void setClasspath(String[] classpath) {
    
    String separator = System.getProperty("path.separator");
    
    StringBuffer value = new StringBuffer();
    for (int i = 0; i < classpath.length; i++) {
      if (i>0) value.append(separator);
      value.append(classpath[i]);
    }
    
    System.setProperty("java.class.path", value.toString());
  }
  
  /**
   * Assemble classpath from manifest file information (optional)
   */
  private static String[] getLaunchClasspath() throws MalformedURLException {

    String classpath = expandSystemProperties(getManifest().getMainAttributes().getValue(LAUNCH_CLASSPATH));
    List result = new ArrayList();
    
    // collect a list of classloader URLs
    StringTokenizer tokens = new StringTokenizer(classpath, ",", false);
    while (tokens.hasMoreTokens()) {
      String token = tokens.nextToken().trim();
      File file = new File(token).getAbsoluteFile();
      if (!file.exists()) 
        continue;
      buildClasspath(file, result);
      // next token
    }

    // done
    return (String[])result.toArray(new String[result.size()]);
  }
  
  private static void buildClasspath(File file, List result) throws MalformedURLException {
    
    // a simple file?
    if (!file.isDirectory() && file.getName().endsWith(".jar")) {
      result.add(file.getAbsolutePath());
      return;
    }
    
    // recurse into directory
    File[] files = file.listFiles();
    if (files!=null) for (int i=0;i<files.length;i++) 
      buildClasspath(files[i], result);

    // done
  }
  
  /**
   * Get our manifest file. Normally all (parent) classloaders of a class do provide
   * resources and the enumeration returned on lookup of manifest.mf will start
   * with the topmost classloader's resources. 
   * We're inverting that order to make sure we're consulting the manifest file in 
   * the same jar as this class if available.
   */
  private static Manifest getManifest() {
    
    // cached?
    if (manifest!=null)
      return manifest;
    
    try {

      // find all manifest files
      Stack manifests = new Stack();
      for (Enumeration e = Launcher.class.getClassLoader().getResources(MANIFEST); e.hasMoreElements(); )
        manifests.add(e.nextElement());
      
      // it has to have the runnable attribute
      while (!manifests.isEmpty()) {
        URL url = (URL)manifests.pop();
        InputStream in = url.openStream();
        Manifest mf = new Manifest(in);
        in.close();
        // careful with key here since Attributes.Name are used internally by Manifest file
        if (mf.getMainAttributes().getValue(LAUNCH_CLASS)!=null) {
          manifest = mf;
          return manifest;
        }
      }
      
    } catch (Throwable t) {
      LOG.log(Level.SEVERE, "error while loading manifest", t);
    }
      
    // not found
    LOG.warning("no manifest found");
    manifest = new Manifest();
    return manifest;
  }

  /**
   * A helper for resolving system properties in form of ${key} in a string. The pattern
   * we're looking for is ${[.\w]*}
   */
  private static final Pattern PATTERN_KEY = Pattern.compile("\\$\\{[\\.\\w]*\\}");
  private static String expandSystemProperties(String string) {
    
    if (string==null)
      return "";

    StringBuffer result = new StringBuffer();
    Matcher m = PATTERN_KEY.matcher(string);

    int pos = 0;
    while (m.find()) {
      String prefix = string.substring(pos, m.start());
      String key = string.substring(m.start()+2, m.end()-1);
      
      result.append(prefix);
      result.append(System.getProperty(key));
      
      pos = m.end();
    }
    result.append(string.substring(pos));

    return result.toString();
  }

} //Run
