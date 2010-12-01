/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import ancestris.util.AncestrisPreferences;
import genjfr.app.pluginservice.PluginInterface;
import java.util.Collection;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import org.openide.LifecycleManager;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    private boolean restart = false;

    // On verifie a chaque demarrage les mises a jour de plugin
    // a moins que l'utilisateur n'ait change le reglage
    @Override
    public void validate() throws IllegalStateException {
        Preferences p = NbPreferences.root().node("/org/netbeans/modules/autoupdate");
        p.putInt("period", p.getInt("period",0));
    }


    @Override
    public void restored() {
        // Launches main application
        App.main(new String[]{});

        // Run wizard if necessary
        // FIXME: le wizard doit etre lance dans le moduleinstall du wizard et non de l'application
        if (!NbPreferences.forModule(App.class).get("optionswizard", "").equals("4")) {
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                public void run() {
                    // Any code here will be run with the UI is available
                    checkOptionsWizard();
                    if (restart) {
                        JOptionPane.showMessageDialog(null, NbBundle.getMessage(App.class, "WillRestart.text"));
                        LifecycleManager.getDefault().markForRestart();
                        LifecycleManager.getDefault().exit();
                    }
            }
        });
        }
    WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
        public void run() {
        Collection pfiles = AncestrisPreferences.get(App.class).get("gedcoms", (Collection) null);
        App.center.load(pfiles);
        }
    });
    }

    @Override
    public boolean closing() {
        AncestrisPreferences.get(App.class).put("gedcoms", App.center.getOpenedGedcoms());
        return App.closing();
    }

    @Override
    public void close() {
        App.close();
    }

    /**
     * Launches Wizard for the options if never done and the module exists
     *
     */
    private void checkOptionsWizard() {

        // Lookup wizard module (it actually loads all the modules corresponding to PluginInterface)
        PluginInterface pi = null;
        for (PluginInterface sInterface : Lookup.getDefault().lookupAll(PluginInterface.class)) {
            if ("genjfr.app.tools.optionswizard".equals(sInterface.getPluginName())) {
                pi = sInterface;
                break;
            }
        }

        // Run wizard module when found
        // Also reload registry because the wizard does save a new set of options
        if (pi != null) {
            System.out.println("Launching Wizard...");
            restart = pi.launchModule(null);
        } else {
            JOptionPane.showMessageDialog(null, NbBundle.getMessage(App.class, "Error.noWizard.text"));
        }

    }
}
