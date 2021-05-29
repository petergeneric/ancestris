/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.wizards.newgedcom;

import genj.util.AncestrisPreferences;
import genj.util.Registry;

/**
 *
 * @author daniel
 */
public class NewGedcomOptions {

    private static NewGedcomOptions instance = null;

        private NewGedcomOptions() { }

    public static NewGedcomOptions getInstance() {
        if(instance == null) {
            instance = new NewGedcomOptions();
        }
        return instance;
    }

  /**
   * Ancestris way
   */
    private AncestrisPreferences getPreferences() {
        return Registry.get(NewGedcomOptions.class);
    }
    private static final String SKIP_INTRO   = "newgedcom.skip_intro";         // NOI18N

    public void setSkipIntro(boolean skipIntro) {
        getPreferences().put(SKIP_INTRO, skipIntro);
    }
    public boolean getSkipIntro() {
        return getPreferences().get(SKIP_INTRO, false);
    }



}
