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
@ActionID(category = "Tree", id = "genj.tree.actions.BookmarkAction")
@ActionRegistration(displayName = "BookMark",lazy = false)
@ActionReferences({
    @ActionReference(path = "Ancestris/Actions/GedcomProperty", position = 720)})
/**
 * ActionRoot
 */
public class BookmarkAction extends AbstractAction implements ContextAwareAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(org.openide.util.Lookup context) {

        Entity e = context.lookup(Entity.class);
        if (e == null) {
            return CommonActions.NOOP;
        }
        for (TreeView v : AncestrisPlugin.lookupAll(TreeView.class)) {
            if (v.getGedcom() == e.getGedcom()) {
                return v.getBookmarkAction(e, false);
            }
        }
        return CommonActions.NOOP;
    }
}
