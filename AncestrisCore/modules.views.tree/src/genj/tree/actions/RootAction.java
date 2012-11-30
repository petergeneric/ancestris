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

import ancestris.core.pluginservice.AncestrisPlugin;
import genj.gedcom.Entity;
import genj.tree.TreeView;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author daniel
 */
@ActionID(category = "Tree", id = "genj.tree.actions.RootAction")
@ActionRegistration(displayName = "toto")
@ActionReferences({
    @ActionReference(path = "Actions/GedcomProperty", separatorBefore = 950, position = 1000)})
@Messages("CTL_RootAction=Une action test applicable")
/**
 * ActionRoot
 */
public class RootAction
        extends AbstractAction
        implements ContextAwareAction {

    static final Action NOOP = new AbstractAction("noop") {

            @Override
            public void actionPerformed(ActionEvent e) {
                //noop
            }
        };
    public @Override
    void actionPerformed(ActionEvent e) {
        assert false;
    }

    public @Override
    Action createContextAwareInstance(org.openide.util.Lookup context) {
        
        Entity e = context.lookup(Entity.class);
        if (e == null) return NOOP;
        for (TreeView v:AncestrisPlugin.lookupAll(TreeView.class)){
            if (v.getGedcom() == e.getGedcom())
                return v.getRootAction(e,false);
        }
//        if (e instanceof Indi || e instanceof Fam)
//            return new ActionRoot(e,false);
        return NOOP;
    }
    
}
