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
package ancestris.sample.bourbon;

import ancestris.api.sample.SampleProvider;
import ancestris.core.pluginservice.AncestrisPlugin;
import org.openide.modules.ModuleInstall;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    private SampleProvider sampleInstance = new SamplePlugin();
    @Override
    public void restored() {
        AncestrisPlugin.register(sampleInstance);
        // By default, do nothing.
        // Put your startup code here.
    }

    @Override
    public void uninstalled() {
        super.uninstalled();
        AncestrisPlugin.unregister(sampleInstance);
    }

}
