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
package ancestris.modules.gedcom.gedcomvalidate;

import genj.util.AncestrisPreferences;
import genj.util.Registry;

/**
 *
 * @author daniel
 */
public class GedcomValidateOptions {

    /** singleton */
    private static AncestrisPreferences gedcomValidateOptions;

    private GedcomValidateOptions() {
        gedcomValidateOptions = Registry.get(GedcomValidateOptions.class);
    }

    private AncestrisPreferences getPreferences() {
        return gedcomValidateOptions;
    }

    private static final String MIN_YEAR = "year.min";         // NOI18N

    public void setMinYear(int value) {
        if (value == 0){
            value = 1;
        }
        getPreferences().put(MIN_YEAR, value);
    }

    public int getMinYear() {
        return getPreferences().get(MIN_YEAR, 1);
    }

    private static final String MAX_YEAR = "year.max";         // NOI18N

    public void setMaxYear(int value) {
        getPreferences().put(MAX_YEAR, value);
    }

    public int getMaxYear() {
        return getPreferences().get(MAX_YEAR, 2100);
    }

    public static GedcomValidateOptions getInstance() {
        return GedcomValidateOptionsHolder.INSTANCE;
    }

    private static class GedcomValidateOptionsHolder {

        private static final GedcomValidateOptions INSTANCE = new GedcomValidateOptions();
    }

}
