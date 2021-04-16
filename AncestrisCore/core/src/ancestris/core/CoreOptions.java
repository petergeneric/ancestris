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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author daniel
 */
public class CoreOptions {

        //XXX: preference path must be defined in core options namespace
    private static AncestrisPreferences appOptions;
    //FIXME: use enums?
    public final static int NAME_NONE = 0;
    public final static int NAME_FIRSTCAP = 1;
    public final static int NAME_ALLCAPS = 2;
 
    public final static String P_USERNAME = "username";
    public final static String P_USEREMAIL = "useremail";
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private CoreOptions() {
        appOptions = Registry.get(CoreOptions.class);
    }

    public static CoreOptions getInstance() {
        return OptionsHolder.INSTANCE;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

     public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
     
    public void fireOptionChange(String property, Object oldvalue, Object newvalue) {
        this.pcs.firePropertyChange(property, oldvalue, newvalue);
    }
    

    
    private static class OptionsHolder {

        private static final CoreOptions INSTANCE = new CoreOptions();
    }

    /*
     * XXX: will have to find some smart way to provide custom editor for those properties
     * private final static Resources RES = Resources.get(Options.class);
     * public final static String[] correctNames = {
     * RES.getString("option.correctName.none"),
     * RES.getString("option.correctName.caps"),
     * RES.getString("option.correctName.allcaps")
     * };
     */
    /** option - whether we correct names */
    public int getCorrectName() {
        return appOptions.get("correctName", 0);
    }

    public void setCorrectName(int correctName) {
        appOptions.put("correctName", correctName);
    }

    /** option - whether to split jurisdictions into their components when editing places */
    public boolean isSplitJurisdictions() {
        return appOptions.get("isSplitJurisdictions", true);
    }

    public void setSplitJurisdictions(boolean isSplitJurisdictions) {
        appOptions.put("isSplitJurisdictions", isSplitJurisdictions);
    }

    public URL getDefaultGedcom() {
        try {
            String defaultFile = appOptions.get("gedcomFile", (String)null);
            if (defaultFile == null)
                return null;
            return new URL(defaultFile);
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    public void setDefaultGedcom(URL def) {
        appOptions.put("gedcomFile", (def == null)?(String)null:def.toString());
    }

    public boolean getAlwaysOpenDefault() {
        return appOptions.get("alwaysOpenDefault", false);
    }

    public void setAlwaysOpenDefault(boolean alwaysOpen) {
        appOptions.put("alwaysOpenDefault", alwaysOpen);
    }

    public boolean getOpenNothingAtStartup() {
        return appOptions.get("openNothingAtStartup", false);
    }

    public void setOpenNothingAtStartup(boolean openNothingAtStartup) {
        appOptions.put("openNothingAtStartup", openNothingAtStartup);
    }

    /**
     * Show/Hide hidden files in open file dialog box.
     * Defaults to "Don't show hidden files"
     *
     * @param def
     */
    public void setShowHidden(boolean def) {
        appOptions.put("showHidden", def);
    }

    public boolean getShowHidden() {
        return appOptions.get("showHidden", false);
    }
    
    
    public int getMinAutosave() {
        return appOptions.get("minautosave", 0);
    }

    public void setMinAutosave(int min) {
        appOptions.put("minautosave", min);
    }
    
    public String getUserName(String def) {
        return appOptions.get(P_USERNAME, def);
    }

    public void setUserName(String username) {
        appOptions.put(P_USERNAME, username);
    }
    
    public String getUserEmail(String def) {
        return appOptions.get(P_USEREMAIL, def);
    }

    public void setUserEmail(String useremail) {
        appOptions.put(P_USEREMAIL, useremail);
    }

    
}
