/* * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import ancestris.util.AncestrisPreferences;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genjfr.app.pluginservice.GenjFrPlugin;
import genjfr.util.GedcomDirectory;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.openide.awt.UndoRedo;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
// TODO: regarder en detail cette faq: http://wiki.netbeans.org/DevFaqNonSingletonTopComponents
//TODO: delete@ConvertAsProperties(
//    dtd="-//genjfr.app//ControlCenter//EN",
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
@ServiceProvider(service=GenjViewInterface.class)
public class AncestrisTopComponent extends TopComponent implements GenjViewInterface{

    private static final String PREFERRED_ID = "AncestrisTopComponent";
    private javax.swing.JPanel panel;
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
        if (dummyNode == null) { return tcLookup; }
        return new ProxyLookup(new Lookup[] {tcLookup, dummyNode.getLookup()});
    }

    @Override
    public UndoRedo getUndoRedo() {
        return GedcomDirectory.getInstance().getUndoRedo(context);
    }

    public String getDefaultFactoryMode() {return "genjfr-editor";}

    String getDefaultMode(){
        return AncestrisPreferences.get(this).get(preferredID()+".dockMode",getDefaultFactoryMode());
    }

    public void setDefaultMode(String mode) {
        AncestrisPreferences.get(this).put(preferredID()+".dockMode", mode);
    }

    public void setDefaultMode(Mode mode) {
        setDefaultMode(mode.getName());
    }

    @Override
    public void open() {
        if (context == null)
            return;
        if (!isRestored) {
            String modeName = App.getRegistry(getGedcom()).get(preferredID()+".dockMode", getDefaultMode()) ;
            
             Mode m = WindowManager.getDefault().findMode (modeName);
             if (m != null) {
                m.dockInto(this);
             }
        }
        super.open();
    }

    public Gedcom getGedcom() {
        return context==null?null:context.getGedcom();
    }
    public void setContext(Context context){
        this.context=context;
        AbstractNode n = GedcomDirectory.getInstance().getDummyNode(context);
        if (n != null && n != dummyNode){
            // Create a dummy node for the save button
            setActivatedNodes(new Node[]{n});
            dummyNode = n;
        }
        ic.add(context);
    }

    public Context getContext() {
        return context;
    }
    
/**
 *
 * @param gedcom
 * @deprecated
 */
    public void setGedcom(Gedcom gedcom) {
        LOG.warning("setGedcom obsolete, try workaround...");
        setContext(new Context(gedcom));
    }


    /**
     * @deprecated : use GenjFrPlugin.register(this)
     */
    public void addLookup() {
        GenjFrPlugin.register(this);
    }

    public void setPanel(JPanel jpanel) {
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

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
    public boolean canClose(){
        persistMode();
        return true;
    }
    private void persistMode(){
        Mode mode = getMode();
        if (mode == null)
            return;
        for (TopComponent tc: mode.getTopComponents()) {
            if (tc instanceof ModePersisterTopComponent)
                return;
        }
        mode.dockInto(new ModePersisterTopComponent());
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        p.setProperty("gedcom",getGedcom().getOrigin().toString());
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        readPropertiesImpl(p);
        return this;
    }

    void readPropertiesImpl(java.util.Properties p) {
// version not used        String version = p.getProperty("version");
        final String gedName = p.getProperty("gedcom");
//        if (gedName==null) return;
        if (gedName==null)
            close();
        isRestored = true;
        waitStartup(gedName);
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public AncestrisTopComponent create() {
        try {
            return this.getClass().newInstance();
            //return Constructor.newInstance(this);
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
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
        if (getImageIcon() != null)
            setIcon(getImageIcon());
        if (context == null || context.getGedcom() == null) {
            return;
        }
        if (!createPanel())
            return;
        GenjFrPlugin.register(this);
        setContext(context);

        String gedcomName;
        if ((getGedcom() != null) && ((gedcomName = getGedcom().getName())!=null)){
            setName(gedcomName);
            setToolTipText(getToolTipText() + ": " + gedcomName);
        }
        // Modification du titre de la fenetre si undockee
        // voir ici: http://old.nabble.com/Look-and-feel-issues-td21583766.html
        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent evt) {
                Window w = SwingUtilities.getWindowAncestor(AncestrisTopComponent.this);
                if(w!=null && w instanceof JFrame && ! (w.equals(WindowManager.getDefault().getMainWindow()))){
//                if(w!=null && w instanceof JFrame){
                    ((JFrame)w).setTitle(getName());
                    ((JFrame)w).setIconImage(getIcon());
                }
            }

        });

    }

    public boolean createPanel(){
        return false;
    }

    public void refreshPanel(Context context) {
        return;
    }

     public Image getImageIcon(){
        return null;
    }
    public void setName() {
        setName("");
    }
    public void setToolTipText(){
        setToolTipText("");
    }

    //FIXME: revoir la synchro avec le CC
    public void waitStartup(String name){
        final String gedName = name;
                new Thread(new Runnable() {
            @SuppressWarnings("empty-statement")
            public void run() {
                while (!App.center.isReady(0))
                        ;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                    if (App.center.getOpenedContext(gedName) == null)
                        close();
                    else {
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
}
