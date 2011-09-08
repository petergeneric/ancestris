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

package ancestris.modules.nav;

import ancestris.core.pluginservice.AncestrisPlugin;
import java.util.Arrays;
import java.util.Collection;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service=ancestris.core.pluginservice.PluginInterface.class)
public class NavigatorPlugin extends AncestrisPlugin{

    @Override
    public Collection<Class> getDefaultOpenedViews() {
        return Arrays.asList(new Class[]{NavigatorTopComponent.class});
    }
}
