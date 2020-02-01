/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package modules.editors.gedcomproperties;

import ancestris.core.pluginservice.AncestrisPlugin;
import genj.gedcom.Context;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service=ancestris.core.pluginservice.PluginInterface.class)
public class GedcomPropertiesPlugin extends AncestrisPlugin{
    
    @Override
    public boolean launchModule(Object o) {
        InvokeGedcomPropertiesModifier module = new InvokeGedcomPropertiesModifier();
        module.update((Context) o);
        return true;
    }
    
    
}
