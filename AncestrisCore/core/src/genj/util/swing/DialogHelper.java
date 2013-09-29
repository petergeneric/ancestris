/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.util.swing;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.util.swing.DialogManager;
import genj.util.Registry;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.EventObject;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import org.openide.NotifyDescriptor;

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

    /** screen we're dealing with */
    private final static Rectangle screen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    /** message types */
    public static final int ERROR_MESSAGE = NotifyDescriptor.ERROR_MESSAGE,
            INFORMATION_MESSAGE = NotifyDescriptor.INFORMATION_MESSAGE,
            WARNING_MESSAGE = NotifyDescriptor.WARNING_MESSAGE,
            QUESTION_MESSAGE = NotifyDescriptor.QUESTION_MESSAGE,
            PLAIN_MESSAGE = NotifyDescriptor.PLAIN_MESSAGE;

    /**
     * Convert actions argument to openDialog (old Genj API) to options
     * argument for NetBeans framework.
     *
     * @param actions
     *
     * @return
     */
    private static Object[] actions2options(Action[] actions) {
        Object options[] = new Object[actions.length];
        for (int i = 0; i < actions.length; i++) {
            if (actions[i] instanceof AbstractAncestrisAction) {
                AbstractAncestrisAction a = (AbstractAncestrisAction) (actions[i]);
                if (a.equals(AbstractAncestrisAction.cancel())) {
                    options[i] = NotifyDescriptor.CANCEL_OPTION;
                    continue;
                }
                if (a.equals(AbstractAncestrisAction.ok())) {
                    options[i] = NotifyDescriptor.OK_OPTION;
                    continue;
                }
                if (a.equals(AbstractAncestrisAction.yes())) {
                    options[i] = NotifyDescriptor.YES_OPTION;
                    continue;
                }
                if (a.equals(AbstractAncestrisAction.no())) {
                    options[i] = NotifyDescriptor.NO_OPTION;
                    continue;
                }
                options[i] = a.getText();
                continue;
            }
            options[i] = actions[i];
        }
        return options;
    }

    /**
     * convert NetBeans object dialog return value to old (Genj) action index.
     * In fact return index in previously computed options array of return value. If not found
     * returns Cancel object index or -1.
     *
     * @param returnValue
param options
     *
     * @return
     */
    private static int getResult(Object returnValue, Object[] options) {
        // close w/o any button means cancel
        if (NotifyDescriptor.CLOSED_OPTION.equals(returnValue)) {
            returnValue = NotifyDescriptor.CANCEL_OPTION;
        }

        for (int a = 0; a < options.length; a++) {
            if (returnValue == options[a]) {
                return a;
            }
        }

        // None found: tries with cancel
        returnValue = NotifyDescriptor.CANCEL_OPTION;
        for (int a = 0; a < options.length; a++) {
            if (returnValue == options[a]) {
                return a;
            }
        }
        return -1;
    }

    /**
     *
     * @param title
     *param messageType
param txt
     * @param actions
    param source
     *
     * @return
     *
     * @deprecated use DialogManager
     */
    @Deprecated
    public static int openDialog(String title, int messageType, String txt, Action[] actions, Object source) {

        Object options[] = actions2options(actions);
        return getResult(DialogManager.show(title, messageType, txt, options), options);
    }

    /**
     * @see genj.window.WindowManager#openDialog(java.lang.String, java.lang.String, javax.swing.Icon, java.awt.Dimension, javax.swing.JComponent[], java.lang.String[], javax.swing.JComponent)
     */
    @Deprecated
    public static int openDialog(String title, int messageType, JComponent[] content, Action[] actions, Object source) {
        // assemble content into Box (don't use Box here because
        // Box extends Container in pre JDK 1.4)
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        for (int i = 0; i < content.length; i++) {
            if (content[i] == null) {
                continue;
            }
            box.add(content[i]);
            content[i].setAlignmentX(0F);

        }
        Object options[] = actions2options(actions);

        // delegate
        return getResult(DialogManager.create(title, box)
                .setMessageType(messageType)
                .setOptions(options)
                //XXX: .setDialogId("")
                .show(),options);
    }

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

    // FIXME: This class is deprecated and used only in GedcomEditor. 
    @Deprecated
    public static class Dialog {

        private String title;
        private int messageType;
        private final JComponent content;
        private Action[] actions;
        private Component parent;
        private JDialog dlg;

        public Dialog(String title, int messageType, final JComponent content, Action[] actions, Object source) {

            this.title = title;
            this.messageType = messageType;
            this.content = content;
            this.actions = actions;

            // find window for source
            parent = null;
            if (source instanceof Component) {
                parent = (Component) source;
            } else if (source instanceof EventObject && ((EventObject) source).getSource() instanceof Component) {
                parent = visitOwners((Component) ((EventObject) source).getSource(), new ComponentVisitor() {

                    public Component visit(Component parent, Component child) {
                        return parent == null ? child : null;
                    }
                });
            }

            // patch opaqueness of content
            patchOpaque(content, true);

            // done for now
        }

        public int show() {

            // create an option pane
            final JOptionPane optionPane = new Content(messageType, content, actions);

            // create the dialog and content
            dlg = optionPane.createDialog(parent, title);
            dlg.setResizable(true);
            dlg.setModal(true);
            dlg.pack();
            dlg.setMinimumSize(content.getMinimumSize());

            // restore bounds
            StackTraceElement caller = getCaller();
            final Registry registry = Registry.get(caller.getClassName());
// pour le moment on ne sauvegarde pas les dimensions des dialog box
            final String key = caller.getMethodName() + (caller.getLineNumber() > 0 ? caller.getLineNumber() : "") + ".dialog";
//      Dimension bounds = null; //registry.get(key, (Dimension)null);
            Dimension bounds = registry.get(key, (Dimension) null);
            if (bounds != null) {
                bounds.width = Math.max(bounds.width, dlg.getWidth());
                bounds.height = Math.max(bounds.height, dlg.getHeight());
                dlg.setBounds(new Rectangle(bounds).intersection(screen));
            }
            dlg.setLocationRelativeTo(parent);

            // hook up to the dialog being hidden by the optionpane - that's what is being called after the user selected a button (setValue())
            dlg.addComponentListener(new ComponentAdapter() {

                public void componentHidden(ComponentEvent e) {
                    registry.put(key, dlg.getSize());
                    dlg.dispose();
                    dlg.removeComponentListener(this);
                    dlg = null;
                }
            });

            // show it
            dlg.setVisible(true);

            // analyze - check which action was responsible for close
            Object rc = optionPane.getValue();
            for (int a = 0; a < actions.length; a++) {
                if (rc == actions[a]) {
                    return a;
                }
            }
            return -1;
        }

        public void cancel() {
            if (dlg == null) {
                throw new IllegalStateException("not showing");
            }
            dlg.dispose();
        }
    }

    private static StackTraceElement getCaller() {
        String clazz = DialogHelper.class.getName();
        for (StackTraceElement element : new Throwable().getStackTrace()) {
            if (!clazz.equals(element.getClassName())) {
                return element;
            }
        }
        // shouldn't happen
        return new StackTraceElement("Class", "method", "file", 0);
    }

    /**
     * A patched up JOptionPane
     */
    private static class Content extends JOptionPane {

        private JDialog dlg;
        private JComponent content;

        /** constructor */
        private Content(int messageType, JComponent content, Action[] actions) {
            super(new JLabel(), messageType, JOptionPane.DEFAULT_OPTION, null, new String[0]);

            this.content = content;

            // wrap content in a JPanel - the OptionPaneUI has some code that
            // depends on this to stretch it :(
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.add(BorderLayout.CENTER, content);
            setMessage(wrapper);

            // create our action buttons
            Option[] options = new Option[actions.length];
            for (int i = 0; i < actions.length; i++) {
                options[i] = new Option(actions[i]);
            }
            setOptions(options);

            // set defalut?
            if (options.length > 0) {
                setInitialValue(options[0]);
            }

            // done
        }

        @Override
        public JDialog createDialog(Component parentComponent, String title) throws HeadlessException {
            dlg = super.createDialog(parentComponent, title);
            return dlg;
        }

        public void doLayout() {

            super.doLayout();

            // check min size on dialog
            if (dlg != null) {
                Dimension c = getSize();
                Dimension m = getMinimumSize();

                Dimension size = dlg.getSize();
                boolean set = false;
                if ((set |= m.width > c.width)) {
                    size.width += m.width - c.width;
                }
                if ((set |= m.height > c.height)) {
                    size.height += m.height - c.height;
                }

                if (set) {
                    dlg.setSize(size);
                }
            }
        }

        /** an option in our option-pane */
        private class Option extends JButton implements ActionListener {

            /** constructor */
            private Option(Action action) {
                super(action);
                addActionListener(this);
            }

            /** trigger */
            public void actionPerformed(ActionEvent e) {
                // this will actually force the dialog to hide - JOptionPane listens to property changes
                setValue(getAction());
            }
        } //Action2Button
    } // Content 

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

    /**
     * scan for JTabbedPanes and make their contained components opaque
     */
    private static void patchOpaque(Component component, boolean set) {

        if (component instanceof JTabbedPane) {
            set = false;
        }

        if (component instanceof JComponent && !(component instanceof JTextField) && !(component instanceof JScrollPane)) {
            if (!set) {
                ((JComponent) component).setOpaque(set);
            }
        }

        if (component instanceof Container && !(component instanceof JScrollPane)) {
            for (Component c : ((Container) component).getComponents()) {
                patchOpaque(c, set);
            }
        }

    }

    /**
     * Check containment
     *
     * @param component component to look for
     * @param container container to look in
     *
     * @return true if component.getParent()*==container
     */
    public static boolean isContained(Component component, final Container container) {
        return container == visitContainers(component, new ComponentVisitor() {

            @Override
            public Component visit(Component parent, Component child) {
                if (parent == container) {
                    return parent;
                }
                return null;
            }
        });
    }
} //AbstractWindowManager
