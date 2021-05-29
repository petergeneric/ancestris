/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.almanac;

import genj.util.EnvironmentChecker;
import java.io.File;
import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        // create user directories for timeline
        File dir = new File(EnvironmentChecker.getProperty(
                "user.home.ancestris/almanac", "?",
                "Looking for almanac"));

        if (!dir.exists()) {
            dir.mkdirs();
        }

    }
}
