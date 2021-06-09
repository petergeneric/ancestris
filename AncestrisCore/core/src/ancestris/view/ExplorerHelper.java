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

import ancestris.core.actions.AncestrisAction;
import ancestris.core.actions.AncestrisActionProvider;
import ancestris.core.actions.CommonActions;
import ancestris.gedcom.PropertyNode;
import genj.gedcom.Property;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import org.openide.awt.MouseUtils;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 *
 * @author Daniel (2012) - helper
 * @author Frédéric (2021) - DND
 * 
 * 
 */
public class ExplorerHelper {

    private final Component source;
    /** Explorer manager, valid when this view is showing */
    private ExplorerManager manager;

    /** importedEntity of drag and drop */
    private EntityTransferHandler entityTransferHandler;
    
    /** not null if popup menu enabled */
    transient MouseContextListener mouseContextListener;
    
    private boolean doubleClickInProgress = false;
    
    
    
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
        return mouseContextListener != null;
    }

    /** Enable/disable displaying popup menus on tree view items.
     * Default is enabled.
     *
     * @param value <
     * code>true</code> to enable
     */
    public void setPopupAllowed(boolean value) {
        if (mouseContextListener == null) {
            mouseContextListener = new MouseContextListener();
        }
        if (value) {
            // on
            addMouseContextListener(mouseContextListener);
        } else {
            // off
            removeMouseContextListener(mouseContextListener);
            mouseContextListener = null;
        }
    }

    public void addMouseContextListener(MouseContextListener mcl) {
        if (manager == null) {
            manager = lookupExplorerManager(source);
        }
        if (entityTransferHandler == null) {
            entityTransferHandler = new EntityTransferHandler();
        }
        source.addMouseListener(mcl);
        if (isSourceDNDEligible(source)) {
            source.addMouseMotionListener(mcl);
            ((JComponent)source).setTransferHandler(entityTransferHandler);
        }
    }

    public void removeMouseContextListener(MouseContextListener mcl) {
        source.removeMouseListener(mcl);
        if (isSourceDNDEligible(source)) {
            source.removeMouseMotionListener(mcl);
            ((JComponent)source).setTransferHandler(null);
        }
    }

    public static ExplorerManager lookupExplorerManager(Component source) {
        if (source instanceof ExplorerManager.Provider) { // look at same level : (find() starts looking at parent level and it has to work if source is the Provider)
            return ((ExplorerManager.Provider) source).getExplorerManager();
        }
        return ExplorerManager.find(source);   // look into parent hierarchy (find() starts looking at parent level)
    }

    private boolean isSourceDNDEligible(Component source) {
        
        // Exclude Gedcom Editor from this drag because it has its own DND system
        if (source.toString().contains("AdvancedEditor$Tree")) {
            return false;
        }
        
        if (source instanceof JComponent) {
           return true; 
        }
        return false;
    }
    
    /**
     * Mouse listener that invokes :
     * - a popup menu: the actual *** Context Menu *** !
     * - drag and drop of importedEntity
     * 
     */
    private class MouseContextListener extends MouseUtils.PopupMouseAdapter {
        
        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            if (!doubleClickInProgress && MouseUtils.isDoubleClick(e)) {
                doubleClickInProgress = true;
                Node[] selNodes = manager.getSelectedNodes();
                Property prop = getPropertyFromNodes(selNodes);
                final Action action = getDefaultAction(e.getComponent(), selNodes, prop);
                if (action != null) {
                    WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                        @Override
                        public void run() {
                            action.actionPerformed(new ActionEvent(e.getSource(), e.getButton(), "dblclk"));
                            doubleClickInProgress = false;
                        }
                    });
                }
                
            }
        }

        @Override
        protected void showPopup(MouseEvent e) {
            Node[] selNodes = manager.getSelectedNodes();
            Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), source);
            createContextMenuPopup(e.getComponent(), p, selNodes);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!e.isConsumed() && (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0 && isSourceDNDEligible(source)) {
                // First try from PropertyProvider (sticky editors in particular)
                Property property = PropertyProvider.getPropertyFromComponent(source, e.getPoint());
                // Else try from context
                if (property == null) {
                    property = getPropertyFromNodes(manager.getSelectedNodes());
                }        
                if (property != null) {
                    entityTransferHandler.setEntity(property.getEntity());
                    entityTransferHandler.exportAsDrag((JComponent)source, e, TransferHandler.COPY);
                }
            }
        }
    }
    
    
        
        
    protected void createContextMenuPopup(Component clickedComponent, Point p, Node[] selNodes) {
        if (!isPopupAllowed()) {
            return;
        }
        
        // Our list of actions
        List<Action> actions = getActionsFromComponent(clickedComponent, selNodes);

        // If actions exist display popup
        if (actions.size() > 0) {
            // If nodes >=1, insert title at the top 
            if (selNodes.length > 0) {
                Property property = getPropertyFromNodes(selNodes);
                Action menuTitleItem = CommonActions.createTitleAction(CommonActions.TYPE_CONTEXT_MENU, property);
                actions.add(0, menuTitleItem);
                actions.add(1, null);  // add separator
            }

            // Builds actions with currently selected context (regardless of nodes)
            JPopupMenu popup = Utilities.actionsToPopup(actions.toArray(new Action[0]), clickedComponent);
            popup.setPopupSize(Math.min(450, popup.getPreferredSize().width), popup.getPreferredSize().height);
            if (popup.getSubElements().length > 0) {
                popup.show(source, p.x, p.y);
            }
        }
    }

    
    private List<Action> getActionsFromComponent(Component clickedComponent, Node[] selNodes) {

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
            actions.add(null); // add separator
        }

        // Get actions from any action providers in lookups
        List<Action> aactions = new ArrayList<Action>();
        for (AncestrisActionProvider aap : Lookup.getDefault().lookupAll(AncestrisActionProvider.class)) {
            aactions.addAll(aap.getActions(false, selNodes));
        }
        actions.addAll(aactions);

        return actions;
    } 
    
    
    public static Property getPropertyFromNodes(Node[] nodes) {
        Property property = null;
        if (nodes != null && nodes.length == 1 && nodes[0] instanceof PropertyNode) {
            property = ((PropertyNode) nodes[0]).getProperty();
        }
        return property;
    }

    
    private Action getDefaultAction(Component component, Node[] selNodes, Property prop) {
        List<Action> actions = getActionsFromComponent(component, selNodes);
        for (Action action : actions) {
            if (action instanceof AncestrisAction) {
                AncestrisAction aaction = (AncestrisAction) action;
                if (aaction.isDefault(prop)) {
                    return action;
                }
            }
        }
        return null;
    }
    
    
    
}
