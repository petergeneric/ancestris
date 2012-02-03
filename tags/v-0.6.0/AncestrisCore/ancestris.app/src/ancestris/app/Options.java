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

import genj.util.AncestrisPreferences;
import genj.util.Registry;



/**
 * Application options
 */
public class Options {
  
  /** singleton */
  private final static AncestrisPreferences coreOptions = Registry.get(Options.class);

  /**
   * Instance access
   */
  public static AncestrisPreferences getInstance() {
    return coreOptions;
  }

  /**
   * Getter - maximum log size
   */
  public static int getMaxLogSizeKB() {
      int size = coreOptions.get("maxLogSizeKB",128);
      if (size > 16384)
          return 16384;
      return size;
  }

  /**
   * Setter - maximum log size
   */
  public static void setMaxLogSizeKB(int set) {
      if (set > 16384)
          set = 16384;
    coreOptions.put("maxLogSizeKB",Math.max(128, set));
  }

    public static boolean isWriteBOM() {
        return coreOptions.get("isWriteBOM",true);
    }

    public static void setWriteBOM(boolean isWriteBOM) {
        coreOptions.put("isWriteBOM",isWriteBOM);
    }

    public static boolean isRestoreViews() {
        return coreOptions.get("isRestoreViews",true);
    }

    public static void setRestoreViews(boolean isRestoreViews) {
        coreOptions.put("isRestoreViews",isRestoreViews);
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
} //Options
