/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2014 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.libs.geonames;

import genj.util.AncestrisPreferences;
import genj.util.Registry;

/**
 *
 * @author daniel
 */
public class GeonamesOptions {
    

        //XXX: preference path must be defined in core options namespace
    private static AncestrisPreferences geonamesOptions;

    private GeonamesOptions() {
        geonamesOptions = Registry.get(GeonamesOptions.class);
    }

    public static GeonamesOptions getInstance() {
        return OptionsHolder.INSTANCE;
    }

    private static class OptionsHolder {

        private static final GeonamesOptions INSTANCE = new GeonamesOptions();
    }

    public String getUserName(){
        return geonamesOptions.get("username", "ancestris");
    }
    public void setUserName(String name){
        geonamesOptions.put("username", name);
    }

    public boolean searchPostalCodes(){
        return geonamesOptions.get("postalcodes", false);
    }
    public void setPostalCodes(boolean set){
        geonamesOptions.put("postalcodes", set);
    }

}
