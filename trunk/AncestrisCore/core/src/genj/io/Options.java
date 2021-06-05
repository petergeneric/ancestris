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

package genj.io;

import genj.util.Registry;

/**
 *
 * @author daniel
 */
public class Options {

        //XXX: preference path must be defined in core options namespace
    static public int getNbBackups() {
        return Registry.get(Options.class).get("backups.nb",10);
    }

    static public void setNbBackups(int nbBackups) {
        Registry.get(Options.class).put("backups.nb",nbBackups);
    }
    
    static public String getBackupDirectory() {
        return Registry.get(Options.class).get("backups.directory", "");
    }

    static public void setBackupDirectory(String backupDirectory) {
        Registry.get(Options.class).put("backups.directory",backupDirectory);
    }
}
