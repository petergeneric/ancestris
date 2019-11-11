/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2019 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.views.graph;

import ancestris.core.pluginservice.AncestrisPlugin;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Zurga
 */
@ServiceProvider(service = ancestris.core.pluginservice.PluginInterface.class)
public class GraphPlugin extends AncestrisPlugin {

    @Override
    public boolean launchModule(Object o) {
        if (o instanceof Gedcom) {
            Gedcom gedcom = (Gedcom) o;
            GraphTopComponent tc = new GraphTopComponent();
            tc.init(new Context(gedcom));
            tc.open();
            tc.requestActive();
        }

        return true;
    }
}
