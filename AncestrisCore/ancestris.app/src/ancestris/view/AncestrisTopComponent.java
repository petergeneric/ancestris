/* * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.view;

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.GedcomDirectory;
import ancestris.app.App;
import ancestris.app.ModePersisterTopComponent;
import ancestris.app.OpenGenjViewAction;
import genj.view.SelectionListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.openide.awt.UndoRedo;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.Mode;
import org.openide.windows.RetainLocation;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
// TODO: regarder en detail cette faq: http://wiki.netbeans.org/DevFaqNonSingletonTopComponents
//TODO: delete@ConvertAsProperties(
//    dtd="-//ancestris.app//ControlCenter//EN",
//    autostore=false
//)
/*
 * on sauvegarde les modes
 * voir http://netbeans.org/bugzilla/show_bug.cgi?id=179526
 * Pour que les modes (TOUS et meme ceux crees par l'utilisateur et donc anonymous)
 * il faut mettre la persistence a ALWAYS
 * or cela conduit a un nombre de fichiers assez important dans le userdir.
 * XXX: il faut donc trouver un moyen pour que la persistence des modes fonctionne.
 * Pour le moment on laisse a ALWAYS
 * Pour qu'un mode soit persistent il doit:
 * - etre non vide
 * - etre marque comme permanent mais ce n'est possible que via le fichier de descrition du mode (donc pas pour les nouveaux modes crees)
 * - Note: il n'existe pas de possibilite de mettre un mode permanent via l'api (voire DefaultModeModel)
 *
 * Les possibilites:
 * - mettre un 'dummy' TC dans les modes pour les rendre permanent
 *   => en attendant une modif des sources
 * - modifier les sources de NB
 *   => trop lourd!
 * - voir si on peut faire comme pour le lifecycle
 *   => non car le DefautModeModel n'est pas instancie via lookup
 * - autre ???
 */
public class AncestrisTopComponent extends TopComponent implements AncestrisViewInterface, SelectionListener {

    private static final String PREFERRED_ID = "AncestrisTopComponent";
    private javax.swing.JComponent panel;
    private boolean isRestored = false;
    private final static Logger LOG = Logger.getLogger("genj.app");
    private Context context;
    InstanceContent ic = new InstanceContent();
    Lookup tcLookup = new AbstractLookup(ic);
    Node dummyNode = null;

    public AncestrisTopComponent() {
        super();
//        associateLookup(tcLookup);
        // toutes les fenetres peuvent aller dans tous les modes
        putClientProperty("TopComponentAllowDockAnywhere", Boolean.TRUE);
    }

    /*
     * voir les explications ici: http://blogs.sun.com/geertjan/entry/savecookie_part_2
     * aussi on aurait pu utiliser http://blogs.sun.com/geertjan/entry/the_divorce_of_savecookies_from
     * pour ne pas etre oblige d'utiliser un dummynode mais dans ce cas on ne peut pas
     * avoir un partage du savecookie entre plusieurs vues comme ce doit etre le cas dans ancestris
     */
    @Override
    public Lookup getLookup() {
        if (dummyNode == null) {
            return tcLookup;
        }
        return new ProxyLookup(new Lookup[]{tcLookup, dummyNode.getLookup()});
    }

    @Override
    public UndoRedo getUndoRedo() {
        return GedcomDirectory.getInstance().getUndoRedo(context);
    }

    String getDefaultMode() {
        String modeName = genj.util.Registry.get(this).get(preferredID() + ".dockMode", (String)null);
        if (modeName == null) {
            modeName = getClass().getAnnotation(RetainLocation.class).value();
        }
        if (modeName == null)
            modeName = AncestrisDockModes.EDITOR;
        return modeName;
    }

    public void setDefaultMode(String mode) {
        genj.util.Registry.get(this).put(preferredID() + ".dockMode", mode);
    }

    public void setDefaultMode(Mode mode) {
        setDefaultMode(mode.getName());
    }

    /**
     * Returns true if only one View can be opened on one gedcom file. Default to false
     * @return
     */
    boolean isSingleView(){
        return false;
    }
    @Override
    public void open() {
        if (context == null) {
            return;
        }
        if (!isRestored) {
            String modeName = getGedcom().getRegistry().get(preferredID() + ".dockMode", getDefaultMode());

            Mode m = WindowManager.getDefault().findMode(modeName);
            if (m != null) {
                m.dockInto(this);
            }
        }
        super.open();
    }

    public Gedcom getGedcom() {
        return context == null ? null : context.getGedcom();
    }

        final public void setContext(Context context, boolean isActionPerformed) {
            // appropriate?
            if (this.context != null && !this.context.sameGedcom(context)) {
                LOG.log(Level.FINER, "context selection on unknown gedcom");
                return;
            }

            // already known?
            if (!isActionPerformed && this.context != null && this.context.equals(context)) {
                return;
            }

            LOG.log(Level.FINER, "fireSelection({0},{1})", new Object[]{context, isActionPerformed});

            // remember
        this.context = context;
            if (context.getGedcom() != null) {
                context.getGedcom().getRegistry().put("context", context.toString());
            }

        AbstractNode n = GedcomDirectory.getInstance().getDummyNode(context);
        if (n != null && n != dummyNode) {
            // Create a dummy node for the save button
            setActivatedNodes(new Node[]{n});
            dummyNode = n;
        }
        ic.add(context);


            setContextImpl(context, isActionPerformed);
        }

    protected void setContextImpl(Context context,boolean isActionPerformed){
    }

    public Context getContext() {
        return context;
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
     * @return
     */
    public Image getImageIcon() {
        return null;
    }

    /**
     * sets the display name (title) of this TopComponent. Usually displayed in
     * a tab.
     */
    public void setName() {
        String name;
        try{
            name = NbBundle.getMessage(this.getClass(), "CTL_"+preferredID());
        } catch (MissingResourceException ex){
            name = preferredID();
        }
        setName(name);
    }

    /**
     * Sets tooltip text
     */
    public void setToolTipText() {
        String name;
        try{
            name = NbBundle.getMessage(this.getClass(), "HINT_"+preferredID());
        } catch (MissingResourceException ex){
            name = preferredID();
        }
        setToolTipText(name);
    }
    /*
     * lors de l'initialisation de la vue la taille du panel n'est pas correcte (0,0)
     * donc le node n'est pas centre dans la vue. Ce flag permet de lancer un recentrage lorsque
     * la taille a ete positionnee correctement et donc de refaire un centrage correct
     * apres.
     *
     * This is specially true for GenjViewTopComponent
     */
    private boolean sizeCorrect = true;

    public boolean isSizeCorrect() {
        return sizeCorrect;
    }

    public void setSizeCorrect(boolean value) {
        sizeCorrect = value;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (!isSizeCorrect()) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    runWhenSizeIsCorrect();
                }
            });
            setSizeCorrect(true);
        }
    }

    public void runWhenSizeIsCorrect() {
    }

    public boolean createPanel() {
        return false;
    }

    public void refreshPanel(Context context) {
        return;
    }

    @Override
    public int getPersistenceType() {
//        return TopComponent.PERSISTENCE_ONLY_OPENED;
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentClosed() {
        persistMode();
    }

    // code pour forcer la persistence des mode (place ici aussi car ne fonctionne pas tjs dans close
    @Override
    public boolean canClose() {
        persistMode();
        return true;
    }

    private void persistMode() {
        Mode mode = getMode();
        if (mode == null) {
            return;
        }
        for (TopComponent tc : mode.getTopComponents()) {
            if (tc instanceof ModePersisterTopComponent) {
                return;
            }
        }
        mode.dockInto(new ModePersisterTopComponent());
    }

    public void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        p.setProperty("gedcom", getGedcom().getOrigin().toString());
        // TODO store your settings
    }

    public void readProperties(java.util.Properties p) {
        readPropertiesImpl(p);
    }

    void readPropertiesImpl(java.util.Properties p) {
// version not used        String version = p.getProperty("version");
        final String gedName = p.getProperty("gedcom");
        if (gedName == null) {
            close();
        }
        setRestored(true);
        waitStartup(gedName);
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    /**
     * return
     * @return
     * XXX:
     */
    public String getPreferencesKey(String key) {
        return PREFERRED_ID + "." + key;
    }

    public AncestrisTopComponent create(Context context) {
        Gedcom gedcom = context == null ? null : context.getGedcom();
        AncestrisTopComponent topComponent = null;
        if (gedcom != null && !isSingleView()) {
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
            //return Constructor.newInstance(this);
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
     * @param component
     * @param displayName
     * @param iconBase
     * @param noIconInMenu
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

    static public Action openAction(Map map) {
        return new OpenGenjViewAction((AncestrisTopComponent) map.get("component"), map);
    }

    public void init(Context context) {
        setName();
        setToolTipText();
        setContext(context,false);
        if (getImageIcon() != null) {
            setIcon(getImageIcon());
        }
        if (context == null || context.getGedcom() == null) {
            return;
        }
        if (!createPanel()) {
            return;
        }
        AncestrisPlugin.register(this);

        String gedcomName;
        if ((getGedcom() != null) && ((gedcomName = getGedcom().getName()) != null)) {
            setName(gedcomName);
            setToolTipText(getToolTipText() + ": " + gedcomName);
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

    //FIXME: revoir la synchro avec le CC
    public void waitStartup(String name) {
        final String gedName = name;
        new Thread(new Runnable() {

            @SuppressWarnings("empty-statement")
            public void run() {
                while (!App.center.isReady(0));
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        if (App.center.getOpenedContext(gedName) == null) {
                            close();
                        } else {
                            init(App.center.getOpenedContext(gedName));
                            open();
                        }
                    }
                });
            }
        }).start();
    }

    public Mode getMode() {
        return WindowManager.getDefault().findMode(this);
    }

    public void setRestored(boolean b) {
        isRestored = b;
    }
    // ToolBar support
    private JToolBar bar = null;

    public void setToolBar(JToolBar bar) {
        this.bar = bar;

        if (bar != null) {
            add(bar, genj.util.Registry.get(this).get("toolbar", BorderLayout.NORTH));
        }
    }

    /**
     * When adding components we fix a Toolbar's sub-component's orientation
     */
    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        // restore toolbar orientation?
        if ((bar != null) && (comp == bar)) {
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
}
