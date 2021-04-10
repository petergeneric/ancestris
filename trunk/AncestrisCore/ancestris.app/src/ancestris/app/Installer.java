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
package ancestris.app;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.explorer.GedcomExplorerTopComponent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.filesystems.FileObject;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    /* package */ final static Logger LOG = Logger.getLogger("ancestris.app");

    // On verifie a chaque demarrage les mises a jour de plugin
    // a moins que l'utilisateur n'ait change le reglage
    @Override
    public void validate() throws IllegalStateException {
        Preferences p = NbPreferences.root().node("/org/netbeans/modules/autoupdate");
        p.putInt("period", p.getInt("period", 0));
//        MyLayoutStyle.register();
    }

    @Override
    public void restored() {
        // Launches main application
        App.main(new String[]{});
        //FIXME: should we put the register statement in constructor or here?
        AncestrisPlugin.register(new ActionSaveLayout());

        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

            @Override
            public void run() {
                LOG.fine("open files");
                List<FileObject> filesToOpen = StartupFiles.getDefault().getCommandLineFiles();
                LOG.log(Level.FINE, "... tries {0}", filesToOpen);
                if (filesToOpen.isEmpty()) {
                    filesToOpen = StartupFiles.getDefault().getAll();
                }
                App.center.load(filesToOpen);
                GedcomExplorerTopComponent.getDefault().open();
            }
        });
    }

    @Override
    //XXX: this doesn't seem to be called before gedcom unregister
    // XXX: unregisters are done in nbDoExit, to be refactored
    public boolean closing() {
        // StartupFiles.getDefault().addOpenedGedcoms();
        return App.closing();
    }

    @Override
    public void close() {
        App.close();
    }
}
