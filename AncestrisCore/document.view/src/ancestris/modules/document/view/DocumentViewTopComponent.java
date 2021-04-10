/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2013 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.document.view;

import genj.gedcom.Context;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.MouseUtils;
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * This TopComponent is intended to display various documents in tabs.
 * All Tabs displayed are derived from {@link AbstractDocumentView} class
 *
 * @author Daniel ANDRE
 */
@ConvertAsProperties(dtd = "-//ancestris.modules.documents.view//DocumentView//EN",
autostore = false)
public class DocumentViewTopComponent extends TopComponent {

    private static DocumentViewTopComponent instance = null;
    private transient boolean isVisible = false;
    private JPopupMenu pop;
    /** Popup menu listener */
    private PopupListener listener;
    private CloseListener closeL;
    private static Image DOC_ICON = ImageUtilities.loadImage("ancestris/modules/document/view/View.png", true); // NOI18N
    private static final String PREFERRED_ID = "DocumentViewTopComponent";
    // Lookup
    InstanceContent ic = new InstanceContent();
    Lookup tcLookup = new AbstractLookup(ic);

    public DocumentViewTopComponent() {
        this(NbBundle.getMessage(DocumentViewTopComponent.class, "LBL_Documents"));
    }

    /** Creates new form DocumentViewTopComponent */
    private DocumentViewTopComponent(String name) {
        super();
        associateLookup(tcLookup);
        setName(name);
        setToolTipText(NbBundle.getMessage(DocumentViewTopComponent.class, "HINT_DocumentViewTopComponent"));
        setIcon(DOC_ICON);
        setFocusable(true);
        setLayout(new java.awt.BorderLayout());
        setMinimumSize(new Dimension(1, 1));
        pop = new JPopupMenu();
        pop.add(new Close());
        pop.add(new CloseAll());
        pop.add(new CloseAllButCurrent());
        listener = new PopupListener();
        closeL = new CloseListener();
        setFocusCycleRoot(true);
        JLabel label = new JLabel(NbBundle.getMessage(DocumentViewTopComponent.class, "LBL_NoOutput"));
        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label.setEnabled(false);
        this.add(label, BorderLayout.CENTER);
    }

    /**
     * Add a {@link AbstractDocumentView} new tab. I no document is displayed,
     * no new tab is created and this Panel is displayed in the main windows array.
     * If there is at least one {@link AbstractDocumentView} displayed, a new
     * tab is created. I there was only one component, a new {@link JTabbedPane}
     * is created and both (old and new) Panel are moved to it.
     *
     * @param panel
     */
    public void addPanel(AbstractDocumentView panel) {
        if (getComponentCount() == 0) {
            add(panel, BorderLayout.CENTER);
            ic.set(new ArrayList<Context>(), null);
            ic.add(panel.getContext());
        } else {
            Component comp = getComponent(0);
            if (comp instanceof JTabbedPane) {
                ((JTabbedPane) comp).addTab(panel.getName(), null, panel, panel.getToolTipText());
                ((JTabbedPane) comp).setSelectedComponent(panel);
                comp.validate();
            } else if (comp instanceof JLabel) {
                remove(comp);
                add(panel, BorderLayout.CENTER);
                if (panel instanceof AbstractDocumentView) {
                    ic.set(new ArrayList<Context>(), null);
                    ic.add(panel.getContext());
                }
            } else {
                remove(comp);
                JTabbedPane pane = TabbedPaneFactory.createCloseButtonTabbedPane();
                pane.addChangeListener(new ChangeListener() {

                    @Override
                    public void stateChanged(ChangeEvent e) {
                        JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
                        Component c = sourceTabbedPane.getSelectedComponent();
                        if (c instanceof AbstractDocumentView) {
                            ic.set(new ArrayList<Context>(), null);
                            ic.add(((AbstractDocumentView) c).getContext());
                        }
                    }
                });
                pane.addMouseListener(listener);
                pane.addPropertyChangeListener(closeL);
                add(pane, BorderLayout.CENTER);
                pane.addTab(comp.getName(), null, comp, ((JPanel) comp).getToolTipText());
                pane.addTab(panel.getName(), null, panel, panel.getToolTipText());
                pane.setSelectedComponent(panel);
                pane.validate();
            }
        }
        if (!isVisible) {
            isVisible = true;
            open();
        }
        validate();
        requestActive();
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        JPanel panel = getCurrentPanel();
        if (panel != null) {
            panel.requestFocus();
        }
    }

    /**
     * Remove tab cintaining panel or close the Window.
     *
     * @param panel
     */
    public void removePanel(JPanel panel) {
        Component comp = getComponentCount() > 0 ? getComponent(0) : null;
        if (comp instanceof JTabbedPane) {
            JTabbedPane tabs = (JTabbedPane) comp;
            if (panel == null) {
                panel = (JPanel) tabs.getSelectedComponent();
            }
            tabs.remove(panel);
            if (tabs.getTabCount() == 1) {
                Component c = tabs.getComponentAt(0);
                tabs.removeMouseListener(listener);
                tabs.removePropertyChangeListener(closeL);
                remove(tabs);
                add(c, BorderLayout.CENTER);
            }
        } else {
            if (comp != null) {
                remove(comp);
            }
            isVisible = false;
            close();
        }
        validate();
    }

    /**
     * Return current (selected) panel. If none, returns null.
     *
     * @return
     */
    //XXX: should be of type AbstractDocumentView?
    public JPanel getCurrentPanel() {
        if (getComponentCount() > 0) {
            Component comp = getComponent(0);
            if (comp instanceof JTabbedPane) {
                JTabbedPane tabs = (JTabbedPane) comp;
                return (JPanel) tabs.getSelectedComponent();
            } else {
                if (comp instanceof JPanel) {
                    return (JPanel) comp;
                }
            }
        }
        return null;
    }

    /**
     * Closes all tabs except currently selected tab.
     */
    private void closeAllButCurrent() {
        Component comp = getComponent(0);
        if (comp instanceof JTabbedPane) {
            JTabbedPane tabs = (JTabbedPane) comp;
            Component current = tabs.getSelectedComponent();
            int tabCount = tabs.getTabCount();
            // #172039: do not use tabs.getComponents()
            Component[] c = new Component[tabCount - 1];
            for (int i = 0, j = 0; i < tabCount; i++) {
                Component tab = tabs.getComponentAt(i);
                if (tab != current) {
                    c[j++] = tab;
                }
            }
            for (int i = 0; i < c.length; i++) {
                ((AbstractDocumentView) c[i]).close();
            }
        }
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized DocumentViewTopComponent getDefault() {
        if (instance == null) {
            instance = new DocumentViewTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the DocumentViewTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized DocumentViewTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(DocumentViewTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof DocumentViewTopComponent) {
            return (DocumentViewTopComponent) win;
        }
        Logger.getLogger(DocumentViewTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    /**
     * On component close, all tabs must be closed too.
     */
    @Override
    protected void componentClosed() {
        isVisible = false;
        if (getComponentCount() == 0) {
            return;
        }
        Component comp = getComponent(0);
        if (comp instanceof JTabbedPane) {
            JTabbedPane pane = (JTabbedPane) comp;
            // #172039: do not use tabs.getComponents()
            Component[] c = new Component[pane.getTabCount()];
            for (int i = 0; i < c.length; i++) {
                c[i] = pane.getComponentAt(i);
            }
            for (int i = 0; i < c.length; i++) {
                ((AbstractDocumentView) c[i]).close();
            }
        } else if (comp instanceof AbstractDocumentView) {
            ((AbstractDocumentView) comp).close();
        }
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    private class CloseListener implements PropertyChangeListener {

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (TabbedPaneFactory.PROP_CLOSE.equals(evt.getPropertyName())) {
                removePanel((JPanel) evt.getNewValue());
            }
        }
    }

    /**
     * Class to showing popup menu
     */
    private class PopupListener extends MouseUtils.PopupMouseAdapter {

        /**
         * Called when the sequence of mouse events should lead to actual showing popup menu
         */
        @Override
        protected void showPopup(MouseEvent e) {
            pop.show(DocumentViewTopComponent.this, e.getX(), e.getY());
        }
    } // end of PopupListener

    private class Close extends AbstractAction {

        public Close() {
            super(NbBundle.getMessage(DocumentViewTopComponent.class, "LBL_CloseWindow"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            removePanel(null);
        }
    }

    private final class CloseAll extends AbstractAction {

        public CloseAll() {
            super(NbBundle.getMessage(DocumentViewTopComponent.class, "LBL_CloseAll"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            close();
        }
    }

    private class CloseAllButCurrent extends AbstractAction {

        public CloseAllButCurrent() {
            super(NbBundle.getMessage(DocumentViewTopComponent.class, "LBL_CloseAllButCurrent"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            closeAllButCurrent();
        }
    }
}
