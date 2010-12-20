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

package ancestris.samples;

import ancestris.samples.api.SampleProvider;
import genjfr.app.pluginservice.GenjFrPlugin;
import java.net.URL;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service=genjfr.app.pluginservice.PluginInterface.class)
public class SamplePlugin extends GenjFrPlugin implements SampleProvider {

    @Override
    public URL getSampleGedcomURL() {
        return this.getClass().getResource("resources/bourbon.ged");
    }

}
