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
        if (popupListener == null && value) {
            // on
            popupListener = new PopupAdapter();
            addPopupListener(popupListener);
            return;
        }
        if (popupListener != null && !value) {
            // off
            removePopupListener(popupListener);
            popupListener = null;
            return;
        }
    }

    public void addPopupListener(MouseListener popupListener) {
        if (manager == null) {
            manager = lookupExplorerManager(source);
        }

        source.addMouseListener(popupListener);
    }

    public void removePopupListener(MouseListener popupListener) {
        source.removeMouseListener(popupListener);
    }

    private ExplorerManager lookupExplorerManager(Component source) {
        ExplorerManager newManager = ExplorerManager.find(source);
        return newManager;
    }

    private void createPopup(Point p, JPopupMenu popup) {
        if (popup.getSubElements().length > 0) {
            popup.show(source, p.x, p.y);
        }
    }

    void createPopup(Component source, Point p) {
        if (isPopupAllowed()) {
            Node[] selNodes = manager.getSelectedNodes();
            // gets actions from first ActionProvider in parent hierarchy from source
            List<AncestrisActionProvider> providers = AncestrisActionProvider.Lookup.lookupAll(source);

            List<Action> actions = new ArrayList<Action>();
            for (AncestrisActionProvider provider:providers) {
                actions.addAll(provider.getActions(true, selNodes));
                actions.add(null);
            }
            if (selNodes.length > 0) {
                actions.addAll(Arrays.asList(NodeOp.findActions(selNodes)));
            }
            
            List<Action> aactions = new ArrayList<Action>();
            for (AncestrisActionProvider aap:Lookup.getDefault().lookupAll(AncestrisActionProvider.class)){
                aactions.addAll(aap.getActions(false, selNodes));
            }
            actions.addAll(aactions);
            if (actions.size() > 0) {
                String title = getTitleFromNodes(selNodes);
                if (title != null){
                    actions.add(0,CommonActions.createSeparatorAction(title));
                    actions.add(1,null);
                }
                JPopupMenu popup = Utilities.actionsToPopup(actions.toArray(new Action[0]), source);
                createPopup(p, popup);
            }
        }
    }

    private static String getTitleFromNodes(Node[] nodes){
        String result = null;
        if (nodes != null && nodes.length == 1 && nodes[0] instanceof PropertyNode){
            result = ((PropertyNode)nodes[0]).getProperty().toString();
        }
        return result;
    }

    /**
     * Mouse listener that invokes popup.
     */
    private class PopupAdapter extends MouseUtils.PopupMouseAdapter {

        PopupAdapter() {
        }

        protected void showPopup(MouseEvent e) {
            // TODO: select correc property
            Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), source);
            if (isPopupAllowed()) {
                createPopup(e.getComponent(), p);
//                e.consume(); //XXX:?
            }
        }
    }
}
