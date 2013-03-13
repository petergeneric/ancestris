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
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.openide.nodes.Node;

/**
 *
 * @author daniel
 */
public interface AncestrisActionProvider {

    static final public List<Action> EMPTY_ACTIONS = new ArrayList<Action>();

    /**
     * Returns actions applicable to the nodes passed in parameter. The returned
     * action may depend on hasFocus paramemer to differentiate global action from
     * those applicable only when the component has focus
     *
     * @param hasFocus true if component has focus
     * @param nodes
     *
     * @return If no action returns empty list
     */
    public List<Action> getActions(boolean hasFocus, Node[] nodes);

    public static class Lookup {

        public static List<AncestrisActionProvider> lookupAll(Component component) {
            List<AncestrisActionProvider> result = new ArrayList<AncestrisActionProvider>(2);
            while (component != null) {
                // component can provide context?
                if (component instanceof AncestrisActionProvider) {
                    result.add((AncestrisActionProvider) component);
                    break;
                }
                component = component.getParent();
            }
            return result;
        }
    }
}
