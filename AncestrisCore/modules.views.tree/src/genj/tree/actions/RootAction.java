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
package genj.tree.actions;

import ancestris.core.actions.CommonActions;
import ancestris.core.pluginservice.AncestrisPlugin;
import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.tree.TreeView;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;

/**
 *
 * @author daniel
 */
@ActionID(category = "Tree", id = "genj.tree.actions.RootAction")
@ActionRegistration(displayName = "SetRoot",lazy = false)
@ActionReferences({
    @ActionReference(path = "Ancestris/Actions/GedcomProperty/Tools", separatorBefore = 950, position = 1000)})
/**
 * ActionRoot
 */
public class RootAction
        extends AbstractAction
        implements ContextAwareAction {

    public @Override
    void actionPerformed(ActionEvent e) {
        assert false;
    }

    public @Override
    Action createContextAwareInstance(org.openide.util.Lookup context) {

        Entity e = null;
        Property p = context.lookup(Property.class);
        if (p instanceof Entity){
            e = (Entity)p;
        }
        if (p instanceof PropertyXRef){
            e = ((PropertyXRef)p).getTargetEntity();
        }
        
        if (e == null) {
            return CommonActions.NOOP;
        }
        for (TreeView v : AncestrisPlugin.lookupAll(TreeView.class)) {
            if (v.getGedcom() == e.getGedcom()) {
                return v.getRootAction(e, false);
            }
        }
        return CommonActions.NOOP;
    }
}
