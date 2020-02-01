/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.treesharing;

import ancestris.core.pluginservice.AncestrisPlugin;
import org.openide.modules.ModuleInstall;
import org.openide.windows.WindowManager;

public class Installer extends ModuleInstall {

    TreeSharingPlugin treeSharingPlugin = new TreeSharingPlugin();

    @Override
    public void restored() {

        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

            @Override
            public void run() {
                AncestrisPlugin.register(treeSharingPlugin);
            }
        });
    }

    @Override
    public void uninstalled() {
        AncestrisPlugin.unregister(treeSharingPlugin);
    }
    

}
