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
package ancestris.gedcom.privacy.standard;

import ancestris.core.pluginservice.AncestrisPlugin;
import org.openide.modules.ModuleInstall;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {
    private PrivacyPolicyImpl privacy;

    @Override
    // XXX: should we used ServiceProvider registration instead?
    public void restored() {
        privacy = new PrivacyPolicyImpl();
        AncestrisPlugin.register(privacy);
    }
}
