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

import ancestris.gedcom.GedcomDirectory.ContextNotFoundException;
import ancestris.util.AUtilities;
import genj.gedcom.Context;
import java.util.List;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author daniel
 */
//XXX: this class is buggy when context.getProperties().size()>1
public class PropertyNode extends AbstractNode {

    private final InstanceContent lookupContents;
    private genj.gedcom.Property property;
    
    static public Children getChildren(Context context){
        return new PropertyChildren(context);
    }

    public PropertyNode(Context context) {
        this(context, new InstanceContent());
        this.property = null;
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

    private PropertyNode(Context context, InstanceContent ic) {
        super(Children.LEAF, createLookup(context, ic));
        this.lookupContents = ic;
    }

    // We create a proxy lookup here to expose DataObject Lookup to TopComponent
    // That way the SaveCookie in dao lookup is seen in TP and can be used to enable Save Action
    static private Lookup createLookup(Context context, InstanceContent ic) {
        try {
            return new ProxyLookup(new AbstractLookup(ic), GedcomDirectory.getDefault().getDataObject(context).getLookup());
        } catch (ContextNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new AbstractLookup(ic);
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
        List<? extends Action> myActions = AUtilities.actionsForPath("Ancestris/Actions/GedcomProperty");

        return myActions.toArray(new Action[myActions.size()]);
    }
    
    
    
    private static class PropertyChildren extends Children.Keys{
        private Context context;

        public PropertyChildren(Context context) {
            super();
            this.context = context;
        }

        @Override
        protected void addNotify() {
            if (!context.getProperties().isEmpty()){
                setKeys(context.getProperties());
            } else if (!context.getEntities().isEmpty()){
                setKeys(context.getEntities());
            }        
        }

        
        @Override
        protected Node[] createNodes(Object key) {
            genj.gedcom.Property prop = (genj.gedcom.Property) key;
            return new Node[] { new PropertyNode(new Context(prop)) };
        }
    }
}
