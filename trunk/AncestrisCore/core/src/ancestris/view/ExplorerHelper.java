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
package ancestris.view;

import ancestris.core.actions.AncestrisActionProvider;
import ancestris.core.actions.CommonActions;
import ancestris.gedcom.PropertyNode;
import genj.gedcom.Entity;
import genj.gedcom.Property;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.openide.awt.MouseUtils;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author daniel
 */
public class ExplorerHelper {

    private final Component source;
    /** Explorer manager, valid when this view is showing */
    private ExplorerManager manager;
    /** not null if popup menu enabled */
    transient PopupAdapter popupListener;

    /** Registers in the tree of components.
     */
    public ExplorerHelper(Component source) {
        this.source = source;

    }

    /** Is it permitted to display a popup menu?
     *
     * @return <
     * code>true</code> if so
     */
    public boolean isPopupAllowed() {
        return popupListener != null;
    }

    /** Enable/disable displaying popup menus on tree view items.
     * Default is enabled.
     *
     * @param value <
     * code>true</code> to enable
     */
    public void setPopupAllowed(boolean value) {
        if (popupListener == null) {
            popupListener = new PopupAdapter();
        }
        if (value) {
            // on
            addPopupListener(popupListener);
        } else {
            // off
            removePopupListener(popupListener);
            popupListener = null;
        }
    }

    public void addPopupListener(MouseListener pl) {
        if (manager == null) {
            manager = lookupExplorerManager(source);
        }
        source.addMouseListener(pl);
    }

    public void removePopupListener(MouseListener pl) {
        source.removeMouseListener(pl);
    }

    private ExplorerManager lookupExplorerManager(Component source) {
        ExplorerManager newManager = ExplorerManager.find(source);
        return newManager;
    }

    protected void createPopup(Component clickedComponent, Point p, Node[] selNodes) {
        if (!isPopupAllowed()) {
            return;
        }
        
        // Our list of actions
        List<Action> actions = new ArrayList<Action>();

        // Get actions from ActionProviders in parent hierarchy from source
        List<AncestrisActionProvider> providers = AncestrisActionProvider.Lookup.lookupAll(clickedComponent);
        for (AncestrisActionProvider provider : providers) {
            actions.addAll(provider.getActions(true, selNodes));
            actions.add(null); // add separator
        }
        
        // Get actions from selected context
        if (selNodes.length > 0) {
            actions.addAll(Arrays.asList(NodeOp.findActions(selNodes)));
        }

        // Get actions from any action providers in lookups
        List<Action> aactions = new ArrayList<Action>();
        for (AncestrisActionProvider aap : Lookup.getDefault().lookupAll(AncestrisActionProvider.class)) {
            aactions.addAll(aap.getActions(false, selNodes));
        }
        actions.addAll(aactions);
        
        // If actions exist and title is not null, insert title at the top and display popup
        if (actions.size() > 0) {
            String title = getTitleFromNodes(selNodes);
            if (title != null) {
                Action menuTitle;
                menuTitle = CommonActions.createSeparatorAction(title);
                actions.add(0, menuTitle);
                actions.add(1, null);  // add separator
            }

            // Builds actions with currently selected context (regardless of nodes)
            JPopupMenu popup = Utilities.actionsToPopup(actions.toArray(new Action[0]), clickedComponent);
            if (popup.getSubElements().length > 0) {
                popup.show(source, p.x, p.y);
            }
        }
    }

    private static String getTitleFromNodes(Node[] nodes) {
        String result = null;
        if (nodes != null && nodes.length == 1 && nodes[0] instanceof PropertyNode) {
            Property prop = ((PropertyNode) nodes[0]).getProperty();

            result = prop.getDisplayValue();
            if (!result.isEmpty()) {
                result = prop.getPropertyName() + ": " + result;
            }
            if (result.isEmpty() && prop instanceof Entity) {
                result = ((Entity) prop).toString(false);
            }
            if (result.isEmpty()) {
                result = prop.getEntity().toString(false) + " (" + prop.getPropertyName() + ")";
            }
        }
        return result;
    }

    /**
     * Mouse listener that invokes popup.
     */
    private class PopupAdapter extends MouseUtils.PopupMouseAdapter {

        @Override
        protected void showPopup(MouseEvent e) {
            Component c = e.getComponent();
            Node[] selNodes = manager.getSelectedNodes();
            Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), source);
            createPopup(e.getComponent(), p, selNodes);
        }

    }
}
