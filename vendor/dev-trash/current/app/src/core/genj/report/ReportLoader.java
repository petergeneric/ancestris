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
package genj.report;

import genj.util.EnvironmentChecker;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * ClassLoad for Reports
 */
public class ReportLoader {

  /** reports we have */
  private List instances = new ArrayList(10);
  
  /** report files */
  private Map file2reportclass = new HashMap(10);
  
  /** classpath */
  private List classpath = new ArrayList(10);
  
  /** whether reports are in classpath */
  private boolean isReportsInClasspath = false;
  
  /** a singleton */
  private static ReportLoader singleton;
  
  /**
   * Clears the report loader's state effectively forcing a reload
   */
  /*package*/ static void clear() {
    singleton = null;
  }
  
  /** 
   * Access
   */
  public static ReportLoader getInstance() {
    
    // not known yet?
    if (singleton==null) {
      synchronized (ReportLoader.class) {
        if (singleton==null) {
          singleton = new ReportLoader();
        }
      }
    }
      
    // done
    return singleton;
      
  }
  
  /**
   * dir resolver
   */
  public static File getReportDirectory() {
    
    // where are the reports 
    return new File(EnvironmentChecker.getProperty(ReportLoader.class,
      new String[]{ "genj.report.dir", "user.dir/report"},
      "report",
      "find report class-files"
    ));
  }
  
  /**
   * Constructor
   */
  private ReportLoader() {

    File base = getReportDirectory();
    ReportView.LOG.info("Reading reports from "+base);
      
    // parse report directory
    try {
      classpath.add(base.toURI().toURL());
    } catch (MalformedURLException e) {
      // n/a
    }
    parseDir(base, null);
    
    // Prepare classloader
    URLClassLoader cl = new URLClassLoader((URL[])classpath.toArray(new URL[classpath.size()]), getClass().getClassLoader());
    
    // Load reports
    for (Iterator files = file2reportclass.keySet().iterator(); files.hasNext(); ) {
      File file = (File)files.next();
      String clazz = (String)file2reportclass.get(file); 
      try {
        Report r = (Report)cl.loadClass(clazz).newInstance();
        r.putFile(file);
        if (!isReportsInClasspath&&r.getClass().getClassLoader()!=cl) {
          ReportView.LOG.warning("Reports are in classpath and can't be reloaded");
          isReportsInClasspath = true;
        }
        instances.add(r);
      } catch (Throwable t) {
        ReportView.LOG.log(Level.WARNING, "Failed to instantiate "+clazz, t);
      }
    }
    
    // sort 'em
    Collections.sort(instances, new Comparator() { 
      public int compare(Object a, Object b) {
        // 20063008 this can actually fail if the report is bad
        try {
          return ((Report)a).getName().compareTo(((Report)b).getName());
        } catch (Throwable t) {
          return 0;
        }
      }
    });
    
    // done
  }
  
  /**
   * Parse directory for lib- and report files
   */
  private void parseDir(File dir, String pkg) { 

    // make sure dir is good
    if (!dir.isDirectory())
      return;

    // loop files and directories
    String[] files = dir.list();
    for (int i=0;i<files.length;i++) {
      File file = new File(dir, files[i]);
      
      // dir?
      if (file.isDirectory()) {
        parseDir(file, (pkg==null?"":pkg+".")+file.getName());
        continue;
      }

      // report class file?
      String report = isReport(file, pkg);
      if (report!=null) {
        file2reportclass.put(file, report);
        continue;
      } 
      
      // library?
      if (isLibrary(file)) {
        try {
          ReportView.LOG.info("report library "+file.toURI().toURL());
          classpath.add(file.toURI().toURL());
        } catch (MalformedURLException e) {
          // n/a
        }
      }
      
      // next 
    }
      
    // done
  }
  
  /**
   * Criteria for library
   */
  private boolean isLibrary(File file) {
    return 
      !file.isDirectory() &&
      (file.getName().endsWith(".jar") || file.getName().endsWith(".zip"));
  }
  
  /**
   * Criteria for report
   */
  private String isReport(File file, String pkg) {
    if ( (pkg!=null&&pkg.startsWith("genj")) || 
         file.isDirectory() ||
         !file.getName().endsWith(".class") ||
         !file.getName().startsWith("Report") ||
         file.getName().indexOf("$")>0 )
      return null;
    String name = file.getName();
    return (pkg==null?"":pkg+".") + name.substring(0, name.length()-".class".length());
  }

  /**
   * Which reports do we have
   */
  public Report[] getReports() {
    return (Report[])instances.toArray(new Report[instances.size()]);
  }

  /**
   * Save options of all reports
   */
  /*package*/ void saveOptions() {
    Report[] rs = getReports();
    for (int r=0;r<rs.length;r++)
      rs[r].saveOptions();
  }
  
  /**
   * Whether reports are in classpath
   */
  /*package*/ boolean isReportsInClasspath() {
    return isReportsInClasspath;
  }

} //ReportLoader
