/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import genj.util.Registry;
import genjfr.app.pluginservice.PluginInterface;
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

    @Override
    public void restored() {
        // By default, do nothing.
        // Put your startup code here.
            App.main(new String[]{});
    if (!NbPreferences.forModule(App.class).get("optionswizard", "").equals("3"))

    WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
        public void run() {
            // any code here will be run with the UI is available
                App.REGISTRY = checkOptionsWizard(App.REGISTRY);
                LifecycleManager.getDefault().markForRestart();
                LifecycleManager.getDefault().exit();
        }
       }
    );
    }

    @Override
    public boolean closing(){
        return App.closing();
    }

    @Override
    public void close(){
        App.close();
    }

        /**
         * Launches Wizard for the options if never done and the module exists
         *
         * @param registry
         * @return
         */
        private Registry checkOptionsWizard(Registry registry) {

            // Lookup wizard module (it actually loads all the modules corresponding to PluginInterface)
            PluginInterface pi = null;
            for (PluginInterface sInterface : Lookup.getDefault().lookupAll(PluginInterface.class)) {
                System.out.println("Plugin " + sInterface.getPluginName() + " loaded successfully.");
                if (sInterface.getPluginName().equals("OptionsWizard")) {
                    pi = sInterface;
                    break;
                }
            }

            // Run wizard module when found
            // Also reload registry because the wizard does save a new set of options
            if (pi != null) {
                System.out.println("Launching Wizard...");
                if (pi.launchModule(registry)) {
                    registry = new Registry("genj");
                }
            } else {
                JOptionPane.showMessageDialog(null, NbBundle.getMessage(App.class, "Error.noWizard.text"));
            }

            return registry;

        }

}
