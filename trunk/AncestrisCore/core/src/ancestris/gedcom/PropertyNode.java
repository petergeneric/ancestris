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
package ancestris.gedcom;

import genj.gedcom.Context;
import java.util.List;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author daniel
 */
public class PropertyNode extends AbstractNode {

    private final InstanceContent lookupContents;
    private genj.gedcom.Property property;

    public PropertyNode(Context context) {
        this((genj.gedcom.Property) null);
        if (context.getProperty() != null) {
            this.property = context.getProperty();
        } else if (context.getEntity() != null) {
            this.property = context.getEntity();
        }
        if (property == null) {
            return;
        }
        lookupContents.add(property);
        lookupContents.add(context);
    }

    public PropertyNode(genj.gedcom.Property property) {
        this(new InstanceContent());
        this.property = property;
    }

    private PropertyNode(InstanceContent ic) {
        super(Children.LEAF, new AbstractLookup(ic));
        this.lookupContents = ic;
    }

    @Override
    public Node.PropertySet[] getPropertySets() {
        return super.getPropertySets();
    }

    public genj.gedcom.Property getProperty() {
        return property;
    }

    @Override
    public Action[] getActions(boolean context) {
        // global actions
        List<? extends Action> myActions = org.openide.util.AUtilities.actionsForPath("Ancestris/Actions/GedcomProperty");

        return myActions.toArray(new Action[myActions.size()]);
    }
}
