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
package genj.renderer;

import genj.util.EnvironmentChecker;
import java.io.File;

/**
 *
 * @author daniel
 */
public class Installer {

    public static void restored() {
        // create user directories for timeline
        File dir = new File(EnvironmentChecker.getProperty(
                "user.home.ancestris/blueprints", "?",
                "Looking for blueprints"));

        if (!dir.exists()) {
            dir.mkdirs();
        }

    }
}
