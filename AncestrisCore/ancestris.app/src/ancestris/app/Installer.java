/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2011 Ancestris
 *
 * Author: Daniel Andre (daniel@ancestris.org).
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.app;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.explorer.GedcomExplorerTopComponent;
import ancestris.view.SelectionSink;
import genj.gedcom.Context;
import genj.util.swing.Action2;
import genj.util.swing.MenuHelper;
import genj.view.ActionProvider;
import genj.view.ContextProvider;
import genj.view.ViewContext;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {
    
    /*package*/ final static Logger LOG = Logger.getLogger("ancestris.app");
    private final static ContextHook HOOK = new ContextHook();

    // On verifie a chaque demarrage les mises a jour de plugin
    // a moins que l'utilisateur n'ait change le reglage
    @Override
    public void validate() throws IllegalStateException {
        Preferences p = NbPreferences.root().node("/org/netbeans/modules/autoupdate");
        p.putInt("period", p.getInt("period", 0));
//        MyLayoutStyle.register();
    }

    @Override
    public void restored() {
        // Launches main application
        App.main(new String[]{});
        //FIXME: should we put the register statement in constructor or here?
        AncestrisPlugin.register(new ActionSaveLayout());

        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

            public void run() {
                Collection<String> pfiles = genj.util.Registry.get(App.class).get("gedcoms", (Collection<String>) null);
                App.center.load(pfiles);
                GedcomExplorerTopComponent.getDefault().open();
            }
        });
    }

    @Override
    //XXX: this doesn't seem to be called before gedcom unregister
    public boolean closing() {
        genj.util.Registry.get(App.class).put("gedcoms", App.center.getOpenedGedcoms());
        return App.closing();
    }

    @Override
    public void close() {
        App.close();
    }
    
    /**
     * Our hook into keyboard and mouse operated context changes / menu
     */
    //XXX: may be we could use NB API here
    private static class ContextHook extends Action2 implements AWTEventListener {

        /** constructor */
        private ContextHook() {
            try {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {

                    public Void run() {
                        Toolkit.getDefaultToolkit().addAWTEventListener(ContextHook.this, AWTEvent.MOUSE_EVENT_MASK);
                        return null;
                    }
                });
            } catch (Throwable t) {
                LOG.log(Level.WARNING, "Cannot install ContextHook", t);
            }
        }

        /**
         * Find workbench for given component
         * @return workbench or null
         */
        /**
         * A Key press initiation of the context menu
         */
        @Override
        public void actionPerformed(ActionEvent event) {
            // only for jcomponents with focus
            Component focus = FocusManager.getCurrentManager().getFocusOwner();
            if (!(focus instanceof JComponent)) {
                return;
            }
            // look for ContextProvider and show menu if appropriate
            ViewContext context = new ContextProvider.Lookup(focus).getContext();
            if (context != null) {
                JPopupMenu popup = getContextMenu(context);
                if (popup != null) {
                    popup.show(focus, 0, 0);
                }
            }
            // done
        }

        /**
         * A mouse click initiation of the context menu
         */
        public void eventDispatched(AWTEvent event) {

            // a mouse popup/click event?
            if (!(event instanceof MouseEvent)) {
                return;
            }
            final MouseEvent me = (MouseEvent) event;
            if (!(me.isPopupTrigger() || me.getID() == MouseEvent.MOUSE_CLICKED)) {
                return;
            }

            // NM 20080130 do the component/context calculation in another event to
            // allow everyone to catch up
            // Peter reported that the context menu is the wrong one as
            // PropertyTreeWidget
            // changes the selection on mouse clicks (following right-clicks).
            // It might be that eventDispatched() is called before the mouse click is
            // propagated to the
            // component thus calculates the menu before the selection changes.
            // So I'm trying now to show the popup this in a later event to make sure
            // everyone caught up to the event

            // find workbench now (popup menu might go away after this method call)
//      final Workbench workbench = getWorkbench((Component)me.getSource());
//      if (workbench==null)
//        return;

            // find context at point
            final Component source = SwingUtilities.getDeepestComponentAt(me.getComponent(), me.getX(), me.getY());
            final ContextProvider.Lookup lookup = new ContextProvider.Lookup(source);
            if (lookup.getContext() == null) {
                return;
            }

            final Point point = SwingUtilities.convertPoint(me.getComponent(), me.getX(), me.getY(), me.getComponent());

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {

                    // a double-click on provider?
                    if (lookup.getProvider() == source
                            && me.getButton() == MouseEvent.BUTTON1
                            && me.getID() == MouseEvent.MOUSE_CLICKED
                            && me.getClickCount() == 2) {
                        SelectionSink.Dispatcher.fireSelection(me.getComponent(), lookup.getContext(), true);
                        return;
                    }

                    // a popup?
                    if (me.isPopupTrigger()) {

                        // cancel any menu
                        MenuSelectionManager.defaultManager().clearSelectedPath();

                        // show context menu
                        JPopupMenu popup = getContextMenu(lookup.getContext());
                        if (popup != null) {
                            popup.show(me.getComponent(), point.x, point.y);
                        }

                    }
                }
            });

            // done
        }

        /**
         * Create a popup menu for given context
         */
        private JPopupMenu getContextMenu(ViewContext context) {

            // make sure context is valid
            if (context == null) {
                return null;
            }

            // make sure any existing popup is cleared
            MenuSelectionManager.defaultManager().clearSelectedPath();

            // create a popup
            MenuHelper mh = new MenuHelper();
            JPopupMenu popup = mh.createPopup();

            // popup local actions?
            mh.createItems(context.getActions());

            // get and merge all actions
            List<Action2> groups = new ArrayList<Action2>(8);
            List<Action2> singles = new ArrayList<Action2>(8);
            Map<Action2.Group, Action2.Group> lookup = new HashMap<Action2.Group, Action2.Group>();

            for (Action2 action : getProvidedActions(context)) {
                if (action instanceof Action2.Group) {
                    Action2.Group group = lookup.get(action);
                    if (group != null) {
                        group.add(new ActionProvider.SeparatorAction());
                        group.addAll((Action2.Group) action);
                    } else {
                        lookup.put((Action2.Group) action, (Action2.Group) action);
                        groups.add((Action2.Group) action);
                    }
                } else {
                    singles.add(action);
                }
            }

            // add to menu
            mh.createItems(groups);
            mh.createItems(singles);

            // done
            return popup;
        }

        private Action2.Group getProvidedActions(Context context) {
            Action2.Group group = new Action2.Group("");
            // ask the action providers
            for (ActionProvider provider : AncestrisPlugin.lookupAll(ActionProvider.class)) {
                provider.createActions(context, ActionProvider.Purpose.CONTEXT, group);
            }
            // done
            return group;
        }
    } //ContextHook

    
}
