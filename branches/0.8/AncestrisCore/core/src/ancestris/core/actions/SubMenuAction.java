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
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.ActionPresenterProvider;
import org.openide.util.actions.Presenter;

/**
 *
 * @author daniel
 */
public class SubMenuAction
        extends AbstractAncestrisAction
        // This action is a context aware action so we get the context from getContextAction
        // This context will then be used in Presenter.Popup to get the 
        // correct submenu for this context
        implements ContextAwareAction, Presenter.Popup {

    private List<Action> actions = new ArrayList<Action>(5);

    public SubMenuAction() {
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
    }

    public SubMenuAction(String displayName) {
        this();
        setText(displayName);
    }

    public SubMenuAction(String displayName, Icon icon) {
        this(displayName);
        setImage(icon);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JPopupMenu menu = Utilities.actionsToPopup(actions.toArray(new Action[]{}), Lookup.EMPTY);
        Component source = (Component) e.getSource();
        menu.show(source, 0, source.getHeight());
    }

    /**
     * Removes all of the elements from this SubMenu.
     * The list of actions this menu contains will be empty after this call returns.
     * Usually called before {@link #addActions(java.util.Collection) to get a 
     * new context.
     */
    public void clearActions() {
        this.actions.clear();
    }
    public void addActions(Collection<? extends Action> actions) {
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
        this.context = context;
        return this;
    }
    private Lookup context;

    public JMenuItem getPopupPresenter() {
        JMenuItem item = actionsToMenu(context);
        item.putClientProperty(DynamicMenuContent.HIDE_WHEN_DISABLED,getValue(DynamicMenuContent.HIDE_WHEN_DISABLED));
        return item;
    }

    /** Builds a menu from a SubMenuAction for provided context specified by
     * <code>Lookup</code>.
     * Takes a SubMenuAction and for actions which are instances of
     * <code>ContextAwareAction</code> creates and uses the context aware instance.
     * Then gets the action presenter or simple menu item for the action to the
     * popup menu for each action (or separator for each 'lonely' null array member).
     *
     * @param subMenu SubMenuAction to build menu for. Can contain null
     * elements, they will be replaced by separators
     * @param context the context for which the menu is build
     *
     * @return the constructed menu
     *
     * @see ContextAwareAction
     * @since 3.29
     */
    // FIXME: we should share counted Hesh with actionsToPopup
    private JMenu actionsToMenu(Lookup context) {
        // keeps actions for which was menu item created already (do not add them twice)
        Set<Action> counted = new HashSet<Action>();
        // components to be added (separators are null)
        List<Component> components = new ArrayList<Component>();

        for (Action action : getActions()) {
            if (action != null && counted.add(action)) {
                JMenuItem item = actionToMenuItem(action, context);
                for (Component c : ActionPresenterProvider.getDefault().convertComponents(item)) {
                    if (c instanceof JSeparator) {
                        components.add(null);
                    } else {
                        components.add(c);
                    }
                }
            } else {
                components.add(null);
            }
        }

        // Now create actual menu. Strip adjacent, leading, and trailing separators.
        JMenu menu = new JMenu(this);
        boolean nonempty = false; // has anything been added yet?
        boolean pendingSep = false; // should there be a separator before any following item?
        for (Component c : components) {
            try {
                if (c == null) {
                    pendingSep = nonempty;
                } else {
                    nonempty = true;
                    if (pendingSep) {
                        pendingSep = false;
                        menu.addSeparator();
                    }
                    menu.add(c);
                }
            } catch (RuntimeException ex) {
                Exceptions.attachMessage(ex, "Current component: " + c); // NOI18N
                Exceptions.attachMessage(ex, "List of components: " + components); // NOI18N
                Exceptions.attachMessage(ex, "List of actions: " + getActions()); // NOI18N
                Exceptions.printStackTrace(ex);
            }
        }

        if (!nonempty) {
            menu.setEnabled(false);
        }

        return menu;
    }

    // This code is copied from openide Utilities class
    private static JMenuItem actionToMenuItem(Action action, Lookup context) {
        //FIXME: quick fix. This function is called with action null parameter. This must not happen...
        if (action == null) {
            return null;
        }

        // switch to replacement action if there is some
        if (action instanceof ContextAwareAction) {
            Action contextAwareAction = ((ContextAwareAction) action).createContextAwareInstance(context);
            if (contextAwareAction == null) {
                Logger.getLogger(Utilities.class.getName()).log(Level.WARNING, "ContextAwareAction.createContextAwareInstance(context) returns null. That is illegal!" + " action={0}, context={1}", new Object[]{action, context});
            } else {
                action = contextAwareAction;
            }
        }

        JMenuItem item;
        if (action instanceof Presenter.Popup) {
            item = ((Presenter.Popup) action).getPopupPresenter();
            if (item == null) {
                Logger.getLogger(Utilities.class.getName()).log(Level.WARNING, "findContextMenuImpl, getPopupPresenter returning null for {0}", action);
            }
        } else {
            // We need to correctly handle mnemonics with '&' etc.
            item = ActionPresenterProvider.getDefault().createPopupPresenter(action);
        }
        return item;

    }
}