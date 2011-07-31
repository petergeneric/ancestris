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

import genj.option.Option;
import genj.option.OptionProvider;
import genj.option.PropertyOption;
import genj.util.EnvironmentChecker;
import genj.util.Resources;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.UIManager;

/**
 * Application options
 */
public class Options extends OptionProvider {
  
  private final static Logger LOG = Logger.getLogger("genj.app");

  /** constants */
  private final static String SWING_RESOURCES_KEY_PREFIX = "swing.";

  /** singleton */
  private final static Options instance = new Options();

  /** resources */
  private Resources resources;

  /** maximum log file size */
  private int maxLogSizeKB = 128;

  /** the current looknfeel */
  private int lookAndFeel = -1;

  /** write BOM on save */
  public boolean isWriteBOM = false;

  /**
   * Instance access
   */
  public static Options getInstance() {
    return instance;
  }

//FIXME: To be removed as LnF will be refactored
//  /**
//   * Getter - looknfeel
//   */
//  public int getLookAndFeel() {
//    // this is invoked once on option introspection
//    if (lookAndFeel<0)
//      setLookAndFeel(0);
//    return lookAndFeel;
//  }
//
//  /**
//   * Setter - looknfeel
//   */
//  public void setLookAndFeel(int set) {
//
//    // Check against available LnFs
//    LnF[] lnfs = LnF.getLnFs();
//    if (set<0||set>lnfs.length-1)
//      set = 0;
//
//    // set it - 20091134 making this a restart only change
//    lnfs[set].apply(null);
//
//    // remember for restart
//    lookAndFeel = set;
//
//    // done
//  }
//
//  /**
//   * Getter - looknfeels
//   */
//  public LnF[] getLookAndFeels() {
//    return LnF.getLnFs();
//  }
//

  /**
   * Getter - maximum log size
   */
  public int getMaxLogSizeKB() {
    return maxLogSizeKB;
  }

  /**
   * Setter - maximum log size
   */
  public void setMaxLogSizeKB(int set) {
    maxLogSizeKB = Math.max(128, set);
  }

  /**
   * Getter - http proxy
   */
  public String getHttpProxy() {
    String host = System.getProperty("http.proxyHost");
    String port = System.getProperty("http.proxyPort");
    if (host==null)
      return "";
    return port!=null&&port.length()>0 ? host+":"+port : host;
  }

  /**
   * Setter - http proxy
   */
  public void setHttpProxy(String set) {
    int colon = set.indexOf(":");
    String port = colon>=0 ? set.substring(colon+1) : "";
    String host = colon>=0 ? set.substring(0,colon) : set;
    if (host.length()==0) port = "";
    System.setProperty("http.proxyHost", host);
    System.setProperty("http.proxyPort", port);
  }

  /**
   * Lazy resources
   */
  private Resources getResources() {
    if (resources==null)
      resources = Resources.get(this);
    return resources;
  }

  /**
   * Provider callback
   */

  public List<? extends Option> getOptions() {
    // bean property options of instance
    List<Option> result = new ArrayList<Option>(PropertyOption.introspect(instance));
    // add an otion for user.home.dir
    //result.add(new UserHomeGenJOption());
    // done
    return result;
  }
  /*
  private static class UserHomeGenJOption extends Option implements OptionUI {

    public String getName() {
      return getInstance().getResources().getString("option.userhomegenj.name");
    }

    public String getToolTip() {
      return getInstance().getResources().getString("option.userhomegenj.name.tip", false);
    }

    public OptionUI getUI(OptionsWidget widget) {
      return this;
    }

    public void persist() {
      // we're not storing anything
    }

    public void restore() {
      // no state to restore
    }

    public void endRepresentation() {
    }

    public JComponent getComponentRepresentation() {
      JButton button = new JButton(new Open());
      // small guy
      button.setMargin(new Insets(2,2,2,2));
      // done
      return button;
    }

    public String getTextRepresentation() {
      // none
      return null;
    }

    /**
     * Action for UI
     
    
    private class Open extends Action2 {
      Open() {
        setText("...");
      }
     
      public void actionPerformed(ActionEvent event) {
        File user_home_genj = new File(EnvironmentChecker.getProperty("user.home.genj", null, "trying to open user.home.genj")) ;
        try {
          Desktop.getDesktop().open(user_home_genj);
        } catch (Throwable t) {
          Logger.getLogger("genj.io").log(Level.INFO, "can't open user.home.genj", t);
        }
      }
     
  }

  }
*/
} //Options
