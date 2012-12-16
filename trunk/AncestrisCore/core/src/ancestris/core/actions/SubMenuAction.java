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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/**
 *
 * @author daniel
 */
public class SubMenuAction
        extends AbstractAction
        // FIXME: we certainly don't need this implementation 
        implements ContextAwareAction {

    private List<Action> actions = new ArrayList<Action>(5);
    private boolean submenuInContext = true;

    public SubMenuAction(String displayName) {
        super(displayName);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // XXX: we must show a submenu for actions linked to a button
        // this = submenu => do nothing
    }

    /**
     * Show submenu in context menu. If false, all actions are put in context menu
     * insteadof a submenu. Usefull for inserting several menu items with
     * only one registration.
     *
     * @return
     */
    public boolean isSubmenuInContext() {
        return submenuInContext;
    }

    public void setSubmenuInContext(boolean submenuInContext) {
        this.submenuInContext = submenuInContext;
    }

    public void setActions(List<Action> actions) {
        this.actions.addAll(actions);
    }

    public void addAction(Action action) {
        this.actions.add(action);
    }

    public List<Action> getActions() {
        return actions;
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
//            Gedcom gedcom = context.lookup(Gedcom.class);
        return this;
//            return CommonActions.NOOP;
    }
}
