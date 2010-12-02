/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2010 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genjfr.app.tools.optionswizard;

import genjfr.app.App;
import javax.swing.JOptionPane;
import org.openide.LifecycleManager;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall implements Runnable{

    @Override
    public void restored() {
        // Run wizard if necessary
        // FIXME: le wizard doit etre lance dans le moduleinstall du wizard et non de l'application
        if (!NbPreferences.forModule(this.getClass()).get("optionswizard", "").equals("4")) {
            WindowManager.getDefault().invokeWhenUIReady(this);
        }
    }

    public void run() {
                    if (OptionsWizardWizardAction.getDefault().launchModule(null)) {
                        JOptionPane.showMessageDialog(null, NbBundle.getMessage(App.class, "WillRestart.text"));
                        LifecycleManager.getDefault().markForRestart();
                        LifecycleManager.getDefault().exit();
                    }
    }
}

