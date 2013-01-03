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
package org.openide.util;

import ancestris.core.actions.CommonActions;
import ancestris.core.actions.SubMenuAction;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.actions.ActionPresenterProvider;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 * This class provides extension to {@link org.openide.util.Utilities} to handle
 * subMenus in context menus.
 * Each folder in System FileSystem from root path provided is shown as submenu.
 *
 * @author daniel
 */
public class AUtilities {

    // Static members only
    private AUtilities() {
    }

    private static List<JMenuItem> actionToMenuItems(Action action, Lookup context) {
        List<JMenuItem> items = new ArrayList<JMenuItem>();
        // switch to replacement action if there is some
        if (action instanceof ContextAwareAction) {
            Action contextAwareAction = ((ContextAwareAction) action).createContextAwareInstance(context);
            if (contextAwareAction == null) {
                Logger.getLogger(Utilities.class.getName()).log(Level.WARNING, "ContextAwareAction.createContextAwareInstance(context) returns null. That is illegal!" + " action={0}, context={1}", new Object[]{action, context});
            } else {
                action = contextAwareAction;
            }
        }
        if (action instanceof SubMenuAction && !((SubMenuAction) action).isSubmenuInContext()) {
            for (Action a : ((SubMenuAction) action).getActions()) {
                items.addAll(actionToMenuItems(a, context));
            }
        } else {
            items.add(actionToMenuItem(action, context));
        }
        return items;
    }

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
        } else if (action instanceof SubMenuAction) {
            item = actionsToMenu((SubMenuAction) action, context);
            if (item == null) {
                Logger.getLogger(Utilities.class.getName()).log(Level.WARNING, "findContextMenuImpl, getSubMenu returning null for {0}", action);
            }
        } else {
            // We need to correctly handle mnemonics with '&' etc.
            item = ActionPresenterProvider.getDefault().createPopupPresenter(action);
        }
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
    private static JMenu actionsToMenu(SubMenuAction subMenu, Lookup context) {
        // keeps actions for which was menu item created already (do not add them twice)
        Set<Action> counted = new HashSet<Action>();
        // components to be added (separators are null)
        List<Component> components = new ArrayList<Component>();

        for (Action action : subMenu.getActions()) {
            if (action != null && counted.add(action)) {
                List<JMenuItem> items = actionToMenuItems(action, context);
                if (items == null || items.isEmpty()) {
                    continue;
                }
                for (JMenuItem item : items) {
                    for (Component c : ActionPresenterProvider.getDefault().convertComponents(item)) {
                        if (c instanceof JSeparator) {
                            components.add(null);
                        } else {
                            components.add(c);
                        }
                    }
                }
            } else {
                components.add(null);
            }
        }

        // Now create actual menu. Strip adjacent, leading, and trailing separators.
        JMenu menu = new JMenu(subMenu);
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
                Exceptions.attachMessage(ex, "List of actions: " + subMenu.getActions()); // NOI18N
                Exceptions.printStackTrace(ex);
            }
        }
        return menu;
    }

    /** Builds a popup menu from actions for provided context specified by
     * <code>Lookup</code>.
     * This function is strictly equivalent to {@link Utilities.actionsToPopup}
     * except that it handle submenun entries in layer.xml files.
     *
     * Takes list of actions and for actions whic are instances of
     * <code>ContextAwareAction</code> creates and uses the context aware instance.
     * Then gets the action presenter or simple menu item for the action to the
     * popup menu for each action (or separator for each 'lonely' null array member).
     *
     * @param actions array of actions to build menu for. Can contain null
     * elements, they will be replaced by separators
     * @param context the context for which the popup is build
     *
     * @return the constructed popup menu
     *
     * @see ContextAwareAction
     * @since 3.29
     */
    public static JPopupMenu actionsToPopup(Action[] actions, Lookup context) {
        // keeps actions for which was menu item created already (do not add them twice)
        Set<Action> counted = new HashSet<Action>();
        // components to be added (separators are null)
        List<Component> components = new ArrayList<Component>();

        for (Action action : actions) {
            if (action != null && counted.add(action)) {
                List<JMenuItem> items = actionToMenuItems(action, context);
                if (items == null || items.isEmpty()) {
                    continue;
                }
                for (JMenuItem item : items) {
                    for (Component c : ActionPresenterProvider.getDefault().convertComponents(item)) {
                        if (c instanceof JSeparator) {
                            components.add(null);
                        } else {
                            components.add(c);
                        }
                    }
                }
            } else {
                components.add(null);
            }
        }

        // Now create actual menu. Strip adjacent, leading, and trailing separators.
        JPopupMenu menu = ActionPresenterProvider.getDefault().createEmptyPopup();
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
                Exceptions.attachMessage(ex, "List of actions: " + Arrays.asList(actions)); // NOI18N
                Exceptions.printStackTrace(ex);
            }
        }
        return menu;
    }

    /** Builds a popup menu for provided component. It retrieves context
     * (lookup) from provided component instance or one of its parent
     * (it searches up to the hierarchy for
     * <code>Lookup.Provider</code> instance).
     * If none of the components is
     * <code>Lookup.Provider</code> instance, then
     * it is created context which is fed with composite ActionMap which delegates
     * to all components up to hierarchy started from the specified one.
     * Then
     * <code>actionsToPopup(Action[],&nbsp;Lookup)</code>} is called with
     * the found
     * <code>Lookup</code> instance, which actually creates a popup menu.
     *
     * @param actions   array of actions to build menu for. Can contain null
     * elements, they will be replaced by separators
     * @param component a component in which to search for a context
     *
     * @return the constructed popup menu
     *
     * @see Lookup.Provider
     * @see #actionsToPopup(Action[], Lookup)
     * @since 3.29
     */
    public static javax.swing.JPopupMenu actionsToPopup(Action[] actions, java.awt.Component component) {
        Lookup lookup = null;

        for (Component c = component; c != null; c = c.getParent()) {
            if (c instanceof Lookup.Provider) {
                lookup = ((Lookup.Provider) c).getLookup();

                if (lookup != null) {
                    break;
                }
            }
        }

        if (lookup == null) {
            // Fallback to composite action map, even it is questionable,
            // whether we should support component which is not (nor
            // none of its parents) lookup provider.
            UtilitiesCompositeActionMap map = new UtilitiesCompositeActionMap(component);
            lookup = org.openide.util.lookup.Lookups.singleton(map);
        }

        return actionsToPopup(actions, lookup);
    }

    // this code borrows ideas from 
    // openide.util.Utilities.actionsForPath
    // http://blogs.kiyut.com/tonny/2007/09/20/netbeans-platform-parsing-layerxml/#.UIb3bHZnmhc
    // From http://forums.netbeans.org/ptopic43419.html
    /**
     * Load a menu sequence from a lookup path.
     * Any {@link Action} instances are returned as is;
     * any {@link JSeparator} instances are translated to nulls.
     * Warnings are logged for any other instances.
     *
     * @param path a path as given to {@link Lookups#forPath}, generally a layer folder name
     *
     * @return a list of actions interspersed with null separators
     *
     * @since org.openide.util 7.14
     */
    public static List<Action> actionsForPath(String path) {
        List<Action> actions = new ArrayList<Action>();

        FileObject fo = FileUtil.getConfigFile(path);

        if (fo != null) {
            buildActions(fo, actions);
        }

        return actions;
    }

    /** Recursive Actions
     *
     * @param fo      FileObject as the DataFolder
     * @param actions
     */
    static private void buildActions(FileObject fo, List<Action> actions) {
        DataObject[] childs = DataFolder.findFolder(fo).getChildren();
        Object instanceObj;

        for (DataObject dob : childs) {
            if (dob.getPrimaryFile().isFolder()) {
                FileObject childFo = dob.getPrimaryFile();
                List<Action> subActions = new ArrayList<Action>();
                buildActions(childFo, subActions);

                if (!subActions.isEmpty()) {
                    SubMenuAction a = new SubMenuAction(dob.getNodeDelegate().getDisplayName());
                    a.setActions(subActions);
                    actions.add(a);
                }
            } else {
                InstanceCookie ck = (InstanceCookie) dob.getCookie(InstanceCookie.class);
                try {
                    instanceObj = ck.instanceCreate();
                } catch (Exception ex) {
                    instanceObj = null;
                }

                if (instanceObj == null) {
                    continue;
                }

                addActions(actions, instanceObj);
            }
        }
    }

    private static void addActions(List<Action> actions, Object instanceObj) {
        if (!CommonActions.NOOP.equals(instanceObj)) {
            if (instanceObj instanceof JSeparator) {
                actions.add(null);
            } else if (instanceObj instanceof SubMenuAction) {
                SubMenuAction smenu = (SubMenuAction) instanceObj;
                if (!smenu.isSubmenuInContext()) {
                    for (Action o : smenu.getActions()) {
                        addActions(actions, o);
                    }
                } else {
                    actions.add(smenu);
                }
            } else if (instanceObj instanceof Action) {
                actions.add((Action) instanceObj);
            }
        }
    }
}
