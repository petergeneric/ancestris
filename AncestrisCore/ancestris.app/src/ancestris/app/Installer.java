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
import java.util.Collection;
import java.util.prefs.Preferences;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

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
        AncestrisPlugin.register(new ActionSaveLayout());

        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

            public void run() {
                Collection<String> pfiles = genj.util.Registry.get(App.class).get("gedcoms", (Collection<String>) null);
                App.center.load(pfiles);
                GedcomExplorerTopComponent.getDefault().open();
            }
        });
    }

    @Override
    public boolean closing() {
        genj.util.Registry.get(App.class).put("gedcoms", App.center.getOpenedGedcoms());
        return App.closing();
    }

    @Override
    public void close() {
        App.close();
    }
}
