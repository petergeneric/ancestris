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

/**
 *
 * @author daniel
 */
public class Options {
    private static AncestrisPreferences appOptions = Registry.get(Options.class);

    public static String getDefaultGedcom(){
        return  appOptions.get("gedcomFile","");
    }
    public static boolean  getAlwaysOpenDefault(){
        return  appOptions.get("alwaysOpenDefault",false);
    }

    public static void setDefaultGedcom(String def){
        appOptions.put("gedcomFile",def);
    }
    public static void setAlwaysOpenDefault(boolean alwaysOpen){
        appOptions.put("alwaysOpenDefault",alwaysOpen);
    }
}
