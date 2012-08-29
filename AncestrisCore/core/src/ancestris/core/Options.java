/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2010 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.core;

import genj.util.AncestrisPreferences;
import genj.util.Registry;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.openide.util.Exceptions;

/**
 *
 * @author daniel
 */
public class Options {
    private static AncestrisPreferences appOptions;
    //FIXME: use enums?
    public final static int NAME_NONE=0;
    public final static int NAME_FIRSTCAP=1;
    public final static int NAME_ALLCAPS=2;

    private Options() {
        appOptions = Registry.get(Options.class);
    }
    
    public static Options getInstance() {
        return OptionsHolder.INSTANCE;
    }
    
    private static class OptionsHolder {

        private static final Options INSTANCE = new Options();
    }

  /*
 * XXX: will have to find some smart way to provide custom editor for those properties
  private final static Resources RES = Resources.get(Options.class);
  public final static String[] correctNames = { 
    RES.getString("option.correctName.none"),
    RES.getString("option.correctName.caps"),
    RES.getString("option.correctName.allcaps")
  };
 */

  /** option - whether we correct names */
    public int getCorrectName() {
        return appOptions.get("correctName",0);
    }

    public void setCorrectName(int correctName) {
        appOptions.put("correctName",correctName);
    }

  /** option - whether to split jurisdictions into their components when editing places */
    public boolean isSplitJurisdictions() {
        return appOptions.get("isSplitJurisdictions",false);
    }

    public void setSplitJurisdictions(boolean isSplitJurisdictions) {
        appOptions.put("isSplitJurisdictions",isSplitJurisdictions);
    }
  
  public URL getDefaultGedcom(){
        try {
            String defaultFile = appOptions.get("gedcomFile","");
          return new URL(defaultFile);
        } catch (MalformedURLException ex) {
            return null;
        }
  }
  
    public void setDefaultGedcom(URL def){
        appOptions.put("gedcomFile",def.toString());
    }

    public boolean  getAlwaysOpenDefault(){
        return  appOptions.get("alwaysOpenDefault",false);
    }

    public void setAlwaysOpenDefault(boolean alwaysOpen){
        appOptions.put("alwaysOpenDefault",alwaysOpen);
    }

    /**
     * Show/Hide hidden files in open file dialog box.
     * Defaults to "Don't show hidden files"
     * @param def
     */
    public void setShowHidden(boolean  def){
        appOptions.put("showHidden",def);
    }
    public boolean getShowHidden(){
        return appOptions.get("showHidden",false);
    }
}
