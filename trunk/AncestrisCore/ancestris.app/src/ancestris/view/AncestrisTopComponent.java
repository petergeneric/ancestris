/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012-2021 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 *         Fredeeric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.view;

import ancestris.app.OpenGenjViewAction;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.GedcomDataObject;
import ancestris.gedcom.GedcomDirectory;
import ancestris.gedcom.GedcomDirectory.ContextNotFoundException;
import ancestris.gedcom.PropertyNode;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.view.SelectionListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
public class AncestrisTopComponent extends TopComponent implements ExplorerManager.Provider, Lookup.Provider, AncestrisViewInterface, SelectionListener {

    private static final String PREFERRED_ID = "AncestrisTopComponent";
    private javax.swing.JComponent panel;
    private final static Logger LOG = Logger.getLogger("ancestris.app");
    private Context currentContext;

    /** 
     * We use ExplorerManager here to handle all selection and the ***Context Menu*** with
     * netbeans api (see for instance http://www.antonioshome.net/kitchen/swingnbrcp/swingnbrcp-explorer.php )
     * As of 2020-02-10 (FL), we also use ExplorerHelper and ExplorerManager to manage general DND across any component that has a ContextMenu
     * Principle: any "context-menu-entity or property" can be dragged to itself or to another one in same gedcom or another one
     * Compatible with existing DND (gedcom explorer, gedcom editor, Relevé)
     * Possibilities are thus to copy entities across gedcoms or merge within the same gedcom
     * It was already possible to copy properties across gedcoms using the gedcom editor
     */
    private final ExplorerManager manager;
    
    public boolean isOpen = false; // method isOpened() cannot be called from withing a thread that is not AWT, so define this flag instead.

    public AncestrisTopComponent() {
        super();
        // Toutes les fenetres peuvent aller dans tous les modes
        putClientProperty("TopComponentAllowDockAnywhere", Boolean.TRUE);

        // Set the manager which will hold selected nodes and be used by the explorer helper to manage the Context Menu
        this.manager = new ExplorerManager();
        
        // Keys for find & replace
        ActionMap map = this.getActionMap();
        map.put("org.openide.actions.FindAction", FileUtil.getConfigObject("Actions/Edit/ancestris-app-ActionFind.instance", Action.class));
        map.put("org.openide.actions.ReplaceAction", FileUtil.getConfigObject("Actions/Edit/ancestris-app-ActionReplace.instance", Action.class));
        
        // The following line tells the top component which lookup should be associated with it
        associateLookup(ExplorerUtils.createLookup(manager, map));

    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    // It is good idea to switch all listeners on and off when the
    // component is shown or hidden. In the case of TopComponent use:

    @Override
    protected void componentActivated() {
        ExplorerUtils.activateActions(manager, true);
    }

    @Override
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
    }


    @Override
    public UndoRedo getUndoRedo() {
        if (currentContext == null) {
            return null;
        }
        try {
            return GedcomDirectory.getDefault().getDataObject(currentContext).getLookup().lookup(GedcomDataObject.class).getUndoRedo();
        } catch (ContextNotFoundException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public String getAncestrisDockMode() {
        return AncestrisDockModes.EDITOR;
    }

    public String getDefaultMode() {
        String modeName = genj.util.Registry.get(this).get(preferredID() + ".dockMode", getAncestrisDockMode());
        if (modeName == null) {
            modeName = AncestrisDockModes.EDITOR;
        }
        return modeName;
    }

    @Override
    public void setDefaultMode(String mode) {
        genj.util.Registry.get(this).put(preferredID() + ".dockMode", mode);
    }

    @Override
    public void setDefaultMode(Mode mode) {
        setDefaultMode(mode.getName());
    }

    /**
     * Returns true if only one View can be opened on one gedcom file. Default to false
     *
     * @return
     */
    boolean isSingleView() {
        return false;
    }

    @Override
    public void open() {
        if (currentContext == null) {
            return;
        }
        String modeName = getGedcom().getRegistry().get(preferredID() + ".dockMode", getDefaultMode());  // docks as gedcom properties or else default saved

        Mode m = WindowManager.getDefault().findMode(modeName);
        if (m != null) {
            m.dockInto(this);
        }
        super.open();
        isOpen = true;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;   // 2020-10-29 FL : ALWAYS is necessary to recover any kind of window configuration but creates many files in modes folder... (see 'create' on a hoock to avoid)
    }

    @Override
    public Gedcom getGedcom() {
        return currentContext == null ? null : currentContext.getGedcom();
    }

    @Override
    final public void setContext(Context context) {

        // appropriate?
        if (this.currentContext != null && !this.currentContext.sameGedcom(context)) {
            LOG.log(Level.FINER, "context selection on unknown gedcom");
            return;
        }

        LOG.log(Level.FINER, "setContext({0},{1})", new Object[]{context});

        // remember
        this.currentContext = context;
        if (context.getGedcom() != null) {
            context.getGedcom().getRegistry().put("context", context.toString());
        }
        try {
            //FIXME: Hack to get set selected node in manager. This will be changed
            // When the whole gedcom will be manage as a nodes tree
            // Typically, for gedcom editor, root will be set to entity
            // for table, root will be set to gedcom
            Children children = PropertyNode.getChildren(context);
            manager.setRootContext(new AbstractNode(children));
            manager.setSelectedNodes(children.getNodes());
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        }

        setContextImpl(context);
    }

    protected void setContextImpl(Context context) {
    }

    public Context getContext() {
        return currentContext;
    }

    public void setPanel(JComponent jpanel) {
        removeAll();
        repaint();
        panel = jpanel;
        if (panel == null) {
            return;
        }

        // setup layout
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
    }

    /**
     * return icon image for this TopComponent
     *
     * @return
     */
    public Image getImageIcon() {
        return null;
    }

    public Image getImageIcon(String resource) {
        return ImageUtilities.loadImage(resource, true);
    }

    /**
     * sets the display name (title) of this TopComponent. Usually displayed in a tab.
     */
    public void setName() {
        String name;
        try {
            name = NbBundle.getMessage(this.getClass(), "CTL_" + preferredID());
        } catch (MissingResourceException ex) {
            name = preferredID();
        }
        setName(name);
    }

    /**
     * Sets tooltip text
     */
    public void setToolTipText() {
        String name;
        try {
            name = NbBundle.getMessage(this.getClass(), "HINT_" + preferredID());
        } catch (MissingResourceException ex) {
            name = preferredID();
        }
        setToolTipText(name);
    }

    public boolean createPanel() {
        return false;
    }

    public void refreshPanel(Context context) {
        return;
    }

    @Override
    public void componentClosed() {
        if (!isOpen) {
            return;
        }
        isOpen = false;
        AncestrisPlugin.unregister(this);

        // Find whether this is the last window opened on this gedcom
        Context ctx = getContext();
        int viewCount = 0;
        for (AncestrisViewInterface gjvTc : AncestrisPlugin.lookupAll(AncestrisViewInterface.class)) {
            if (ctx.getGedcom().equals(gjvTc.getGedcom()) && !this.equals(gjvTc)) {
                viewCount++;
            }
        }
        // In which case, close Gedcom
        if (viewCount == 0) {
            GedcomDirectory.getDefault().closeGedcom(ctx);
        }
        currentContext = null;
    }

    @Override
    public boolean canClose() {
        return true;
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public String getPreferencesKey(String key) {
        return preferredID() + "." + key;
    }

    @Override
    public AncestrisTopComponent create(Context context) {
        Gedcom gedcom = context == null ? null : context.getGedcom();
        AncestrisTopComponent topComponent = null;
        if (gedcom != null && isSingleView()) {
            for (AncestrisTopComponent tc : AncestrisPlugin.lookupAll(this.getClass())) {
                if (gedcom.equals(tc.getGedcom())) {
                    topComponent = tc;
                    break;
                }
            }
        }
        if (topComponent != null) {
            return topComponent;
        }
        try {
            topComponent = this.getClass().newInstance();
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        }
        topComponent.init(context);

        return topComponent;
    }

    /**
     * Gets an action to display a GenjTopComponent. Used in layer.xml
     *
     * @param component
     * @param displayName
     * @param iconBase
     * @param noIconInMenu
     *
     * @return the action
     */
    static public Action openAction(TopComponent component, String displayName, String iconBase, boolean noIconInMenu) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("displayName", displayName); // NOI18N
        map.put("iconBase", iconBase); // NOI18N
        map.put("noIconInMenu", noIconInMenu); // NOI18N
        map.put("component", component); // NOI18N

        return openAction(map);
    }

    static public Action openAction(Map<String, Object> map) {
        return new OpenGenjViewAction((AncestrisTopComponent) map.get("component"), map);
    }

    @Override
    public void init(Context context) {
        setName();
        setToolTipText();
        setContext(context);
        Image icon = getImageIcon();
        if (icon != null) {
            setIcon(icon);
        }
        if (context == null || context.getGedcom() == null) {
            return;
        }
        if (!createPanel()) {
            return;
        }
        AncestrisPlugin.register(this);

        String gedcomName;
        if ((getGedcom() != null) && ((gedcomName = getGedcom().getDisplayName()) != null)) {
            setName(gedcomName);
            String toolTip = getToolTipText(); // make sure we still have the gedcom name in case translation misses {0}.
            if (!toolTip.contains("{0}")) {
                toolTip += " - " + gedcomName;
            } else {
                toolTip = toolTip.replace("{0}", gedcomName);
            }
            setToolTipText(toolTip);
        }
        // Modification du titre de la fenetre si undockee
        // voir ici: http://old.nabble.com/Look-and-feel-issues-td21583766.html
        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent evt) {
                Window w = SwingUtilities.getWindowAncestor(AncestrisTopComponent.this);
                if (w != null && w instanceof JFrame && !(w.equals(WindowManager.getDefault().getMainWindow()))) {
                    ((JFrame) w).setTitle(getName());
                    ((JFrame) w).setIconImage(getIcon());
                }
            }
        });

    }

    @Override
    public Mode getMode() {
        return WindowManager.getDefault().findMode(this);
    }

    // ToolBar support
    private JToolBar bar = null;

    public void addToolBar() {
        this.bar = getToolBar();

        if (bar != null) {
            add(bar, genj.util.Registry.get(this).get("toolbar", BorderLayout.NORTH));
        }
    }

    /**
     * Return toolbar associated to this TopComponent. If no toolbar exists, this function must return null. Default value is no toolbar.
     *
     * @return
     */
    public JToolBar getToolBar() {
        return null;
    }

    /**
     * When adding components we fix a Toolbar's sub-component's orientation
     */
    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        // restore toolbar orientation?
        if ((bar != null) && (comp == bar)) {
            constraints = getToolBarConstraints(bar, constraints);
            // remember
            genj.util.Registry.get(this).put("toolbar", constraints.toString());
            // find orientation
            int orientation = SwingConstants.HORIZONTAL;
            if (BorderLayout.WEST.equals(constraints) || BorderLayout.EAST.equals(constraints)) {
                orientation = SwingConstants.VERTICAL;
            }
            // fix orientation for toolbar
            bar.setOrientation(orientation);
            // toolbar o.k.
        }
        // go ahead with super
        super.addImpl(comp, constraints, index);
        // done
    }

    /**
     * Return Tool Bar constraints. Defult return constrain parameter. Can be overidden to prevent toolbar to be put vertically.
     *
     * @param bar
     * @param constraints
     *
     * @return Tool Bar constraints
     */
    protected Object getToolBarConstraints(JToolBar bar, Object constraints) {
        return constraints;
    }

}
