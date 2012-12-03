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
import org.openide.util.AUtilities;

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
            AncestrisActionProvider provider = AncestrisActionProvider.Lookup.lookup(source);

            List<Action> actions = new ArrayList<Action>();
            if (provider != null) {
                actions.addAll(provider.getActions(selNodes));
                actions.add(null);
            }
            if (selNodes.length > 0) {
                actions.addAll(Arrays.asList(NodeOp.findActions(selNodes)));

                if (actions.size() > 0) {
                    createPopup(p, AUtilities.actionsToPopup(actions.toArray(new Action[0]), source));
                }
            }
        }
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
