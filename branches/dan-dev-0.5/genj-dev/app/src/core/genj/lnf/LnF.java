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
package genj.lnf;

import genj.util.EnvironmentChecker;
import genj.util.Registry;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * A Look&Feel
 */
public class LnF {
  
  private static Logger LOG = Logger.getLogger("genj.lnf");
  
  /** constants */
  static private final String 
    LNF_PROPERTIES = "lnf.properties",
    LNF_DIR        = "./lnf";

  /** Look&Feels */
  private static LnF[] instances;
  
  /** members */
  private String name,type,archive,url,version,theme;
  private ClassLoader cl;
  private LookAndFeel instance;
  
  /**
   * LnFs
   */
  public static LnF[] getLnFs() {
    
    // known?
    if (instances!=null)
      return instances;

    // create 'em      
    ArrayList result = new ArrayList();    
    
    // add default
    result.add(new LnF("System Default", UIManager.getSystemLookAndFeelClassName(), "", "", null, null));
    
    // read lnf pack
    try {
      
      // get the number of configured lnfs
      Registry r = new Registry(new FileInputStream(new File(getLnFDir(), LNF_PROPERTIES)));
      int num = r.get("lnf.count",0);
      
      // create a LnF for each
      for (int d=0; d<num; d++) {

        String prefix = "lnf."+(d+1);

        // members
        String
          name    = r.get(prefix+".name",""),
          type    = r.get(prefix+".type",""),
          url     = r.get(prefix+".url",""),
          version = r.get(prefix+".version",""),
          archive = r.get(prefix+".jar",(String)null);
        
        if (name.length()==0)
          continue;
      
        // patch name (don't want it to be too long)
        int i = name.indexOf('(');
        if (i>0)
          name = name.substring(0,i);

        // themes
        String[] ts = r.get(prefix+".themes",new String[0]);
        if (ts.length>0) {
          for (int t=0;t<ts.length;t++) {
            result.add(new LnF(name,type,url,version,archive, ts[t]));
          }
        } else {
          result.add(new LnF(name,type,url,version,archive, null));
        }
      }   

    } catch (IOException ioe) {
    }

    // add an option for using the java default (aka don't change LnF from what's setup by VM)
    result.add(new LnF("Java Default", UIManager.getLookAndFeel().getClass().getName(), "", "", null, null));

    // remember
    instances = (LnF[])result.toArray(new LnF[result.size()]);   
    
    // done    
    return instances;
  }
  
  /**
   * Directory of LnF
   */
  private static String getLnFDir() {
    return EnvironmentChecker.getProperty(
      "genj.lnf.dir",
      LNF_DIR,
      "read lnf.properties"
    );
  }
  
  /**
   * Constructor
   */
  private LnF(String name, String type, String url, String version, String archive, String theme) {
    
    // remember
    this.name = name;
    this.type = type;
    this.url  = url ;
    this.version = version;
    this.archive = archive;
    this.theme = theme;      
    
    // debug
    LOG.info("Found Look&Feel "+name+" type="+type+" version="+version+" url="+url+" archive="+archive+" theme="+theme);
    
    // done
  }
  
  /**
   * Type
   */
  private LookAndFeel getInstance() throws Exception {
    
    // create an instance once
    if (instance==null) 
      instance = (LookAndFeel)cl.loadClass(type).newInstance();      
    
    // Reset Metal's current theme (some L&Fs change it)
    if (instance.getClass()==javax.swing.plaf.metal.MetalLookAndFeel.class) {
      javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(
        new javax.swing.plaf.metal.DefaultMetalTheme()
      );
    }

    // prepare theme
    if (theme!=null) {
      
      // calc theme jar       
      String themejar =  new File(getLnFDir(), getTheme()).getAbsolutePath();
      
      // HACK: for www.lfprod.com's SkinLookAndFeel ONLY right now
      System.setProperty("skinlf.themepack", themejar);
    
      // Done
    }
    
    // here it is
    return instance;
  }
  
  /**
   * Classloader
   */
  private ClassLoader getCL() throws MalformedURLException {
    if (cl!=null) return cl;
    if (archive==null) {
      cl = getClass().getClassLoader();
    } else {
      URL urlArchive = new URL("file", "", new File(getLnFDir(), archive).getAbsolutePath());
      cl = new URLClassLoader(new URL[]{urlArchive}, getClass().getClassLoader());
    }
    return cl;
  }
  
  /**
   * String
   */
  public String toString() {
    // no theme?
    if (theme==null)
      return name;
    // check theme
    String s = theme;
    int i = s.lastIndexOf('/');
    if (i>0)
      s = s.substring(i+1);
    return name + '(' + s + ')';
  }
  
  /**
   * Name
   */
  public String getName() {
    return name;
  }
  
  /**
   * Type
   */
  public String getType() {
    return type;
  }
  
  /**
   * Archive
   */
  public String getArchive() {
    return archive;
  }
  
  /**
   * Resolves a Theme by name
   */
  public String getTheme() {
    return theme;
  }

  /**
   * Applies the LnF
   */
  public boolean apply(final List rootComponents) {
    
    // try to load LnF
    String prefix = "Look and feel #"+this+" of type "+type;
    
    // Load and apply L&F
    try {
      
      UIManager.getLookAndFeelDefaults().put("ClassLoader",getCL());
      UIManager.getDefaults().put("ClassLoader",getCL());
      
      UIManager.setLookAndFeel(getInstance());
      
    } catch (ClassNotFoundException cnfe) {
      LOG.warning(prefix+" is not accessible (ClassNotFoundException)");
      return false;
    } catch (ClassCastException cce) {
      LOG.warning(prefix+" is not a valid LookAndFeel (ClassCastException)");
      return false;
    } catch (MalformedURLException mue) {
      LOG.warning(prefix+" doesn't point to a valid archive (MalformedURLException)");
      return false;
    } catch (UnsupportedLookAndFeelException e) {
      LOG.warning(prefix+" is not supported on this platform (UnsupportedLookAndFeelException)");
      return false;
    } catch (Throwable t) {
      LOG.warning(prefix+" couldn't be set ("+t.getClass()+")");
      return false;
    }
    
    // reflect it    
    if (rootComponents!=null) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          Iterator e = rootComponents.iterator();
          while (e.hasNext()) try {
            SwingUtilities.updateComponentTreeUI((Component)e.next());
          } catch (Throwable t) {
          }
        }
      });
    }
    
    // done
    return true;
  }

} // LnF
