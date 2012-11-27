/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.core.actions;

import java.awt.Component;
import java.util.List;
import javax.swing.Action;
import org.openide.nodes.Node;

/**
 *
 * @author daniel
 */
public interface AncestrisActionProvider {

    public List<Action> getActions(Node[] nodes);

    public static class Lookup {

        public static AncestrisActionProvider lookup(Component component) {
            while (component != null) {
                // component can provide context?
                if (component instanceof AncestrisActionProvider) {
                    break;
                }
                component = component.getParent();
            }
            return (AncestrisActionProvider)component;
        }
    }
}
