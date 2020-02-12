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
package ancestris.modules.views.graph.graphstream;

import org.graphstream.graph.implementations.MultiGraph;

/**
 * Ancestris version to allow to do clear without clearing all settings.
 * @author Zurga
 */
public class AncestrisMultiGraph extends MultiGraph {

    public AncestrisMultiGraph(String id) {
       super(id);
    }
    
    @Override
    protected void clearAttributesWithNoEvent() {
        // Nothing to do.
    }
}
