/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: Dominique Baron (lemovice-at-ancestris-dot-org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.sosanumbers;

import ancestris.core.pluginservice.AncestrisPlugin;
import org.openide.modules.ModuleInstall;
import org.openide.windows.WindowManager;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    SosaNumbersPlugin sosaNumbersPlugin = new SosaNumbersPlugin();

    @Override
    public void restored() {

        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

            @Override
            public void run() {
                AncestrisPlugin.register(sosaNumbersPlugin);
            }
        });
    }

    @Override
    public void uninstalled() {
        AncestrisPlugin.unregister(sosaNumbersPlugin);
    }
}
