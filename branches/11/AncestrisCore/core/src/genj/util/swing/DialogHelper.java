/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2012-2013 Ancestris
 *
 * Author: Daniel Andre (daniel@ancestris.org).
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.util.swing;

import java.awt.Component;
import java.awt.Window;
import java.util.EventObject;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Helper for interacting with Dialogs and Windows
 * We put all interface between old Genj Dialogs and NetBeans Dialog box here
 * because refactoring the application
 * to use only DialogManager is too time consuming. May be we will
 * do this task later. All API of this class is marked as deprecated
 */
// XXX: will have to refactor this class and use NB API
@Deprecated
public class DialogHelper {


    public static Window getWindow(EventObject event) {
        if (!(event.getSource() instanceof Component)) {
            throw new IllegalArgumentException("can't find window for event without component source");
        }
        if (event.getSource() instanceof Window) {
            return (Window) event.getSource();
        }
        return (Window) visitOwners((Component) event.getSource(), new ComponentVisitor() {

            public Component visit(Component parent, Component child) {
                return parent instanceof Window ? parent : null;
            }
        });
    }

    /**
     * Visit containers of a component recursively. This method follows the getParent()
     * hierarchy.
     */
    public static Component visitContainers(Component component, ComponentVisitor visitor) {
        do {
            Component parent = component.getParent();

            Component result = visitor.visit(parent, component);
            if (result != null) {
                return result;
            }

            component = parent;

        } while (component != null);

        return null;
    }

    /**
     * Visit owners of a component recursively. This method takes (popup) menu containment
     * into account so one can recursively go from a component in a menu up to the owning
     * component showing the menu.
     */
    public static Component visitOwners(Component component, ComponentVisitor visitor) {

        do {
            Component parent;
            if (component instanceof JPopupMenu) {
                parent = ((JPopupMenu) component).getInvoker();
            } else if (component instanceof JMenu) {
                parent = ((JMenu) component).getParent();
            } else if (component instanceof JMenuItem) {
                parent = ((JMenuItem) component).getParent();
            } else if (component instanceof JDialog) {
                parent = ((JDialog) component).getOwner();
            } else if (component != null) {
                parent = component.getParent();
            } else {
                return null;
            }

            Component result = visitor.visit(parent, component);
            if (result != null) {
                return result;
            }

            component = parent;

        } while (component != null);

        return null;
    }

    public static Component visitOwners(EventObject event, ComponentVisitor visitor) {
        return visitOwners((Component) event.getSource(), visitor);
    }

    /**
     * interface for visiting components
     */
    public interface ComponentVisitor {

        /**
         * visit a component (owner or container) and its child
         *
         * @return null to continue in the parent hierarchy, !null to abort otherwise
         */
        public Component visit(Component component, Component child);
    }

} //AbstractWindowManager
