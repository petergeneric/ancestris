/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import genj.gedcom.Gedcom;
import genj.view.ViewContainer;
import genj.view.ViewFactory;
import genj.view.ViewHandle;
import genj.view.ViewManager;
import genj.window.GenjFrWindowManager;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.ImageUtilities;

/**
 * Top component which displays something.
 */
//TODO: delete@ConvertAsProperties(
//    dtd="-//genjfr.app//ControlCenter//EN",
//    autostore=false
//)
@ServiceProvider(service=GenjInterface.class)
public class GenjViewTopComponent extends TopComponent implements GenjInterface {

//    static GenjViewTopComponent factory;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "GenjViewTopComponent";
    private static javax.swing.JPanel panel;
    private static InstanceContent ic = new InstanceContent();
    Gedcom gedcom = null;
    private static AbstractLookup abstractLookup = new AbstractLookup(ic);
    private boolean isRestored = false;

    String getDefaultMode(){return "genjfr-editor";};

    @Override
    public void open() {
        if (!isRestored) {
             Mode m = WindowManager.getDefault().findMode (getDefaultMode());
             if (m != null) {
                m.dockInto(this);
             }
        }
        super.open();
    }

    public Gedcom getGedcom() {
        return gedcom;
    }

    public void setGedcom(Gedcom gedcom) {
        this.gedcom = gedcom;
    }

    public GenjViewTopComponent() {
        super();
        // toutes les fenetres peuvent aller dans tous les modes
            putClientProperty("TopComponentAllowDockAnywhere", Boolean.TRUE); 
    }

    public void setPanel(JPanel jpanel) {

        panel = jpanel;
        if (panel == null) {
            return;
        }

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
//        setName(NbBundle.getMessage(GenjViewTopComponent.class, "CTL_"+preferredID()));
//        setToolTipText(NbBundle.getMessage(GenjViewTopComponent.class, "HINT_"+preferredID()));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

    }

    void setPanel(ViewFactory factory) {
        Gedcom gedcom = App.center.getSelectedGedcom();
        setPanel(gedcom, factory);
    }

    void setPanel(Gedcom gedcom, ViewFactory factory) {
        this.gedcom = gedcom;
        if (gedcom == null) {
            return;
        }

        ViewHandle handle = App.center.getViewManager().openView(gedcom, factory);

        // get a registry

        genj.util.Registry registry = new genj.util.Registry(ViewManager.getRegistry(gedcom), getPackage(factory)/*+"."+sequence*/);

        // title
        String title = gedcom.getName() + " - " + factory.getTitle(false) + " (" + registry.getViewSuffix() + ")";

        // create the view
        final JComponent view = factory.createView(title, gedcom, registry, App.center.getViewManager());
        final ViewHandle vhandle = new ViewHandle(App.center.getViewManager(), gedcom, title, registry, factory, view, 1/*sequence*/);
        final ViewContainer container = new ViewContainer(vhandle);
        genj.window.WindowManager wm = App.center.getWindowManager();
        setPanel((JPanel) ((GenjFrWindowManager) wm).getFrame(wm.openWindow(vhandle.getKey(), title, factory.getImage(), container, null, null)));

//        setPanel((JPanel)handle.getView());
//    setPanel((JPanel) factory.createView(title, gedcom, registry, App.center.getViewManager()));


//clipse    	setPartName(gedcom.getName());

        ic.add(this);
    }

    static Lookup getMyLookup() {
        return abstractLookup;
    }
    /**
     * Get the package name of a Factory
     */
    /*package*/ String getPackage(ViewFactory factory) {

        Matcher m = Pattern.compile(".*\\.(.*)\\..*").matcher(factory.getClass().getName());
        if (!m.find()) {
            throw new IllegalArgumentException("can't resolve package for " + factory);
        }
        return m.group(1);

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
    /**
     * Obtain the panel for this view
     */
//    static public JPanel getPanel() {
//        return panel;
//    }
    /**
     * Obtain the prefered ID
     */
    static String getPreferredId() {
        return PREFERRED_ID;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }


    Object readProperties(java.util.Properties p) {
//FIXME        EditTopComponent singleton = EditTopComponent.getDefault();
//        singleton.readPropertiesImpl(p);
//        return singleton;
        readPropertiesImpl(p);
        return this;
    }

    void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        final String gedName = p.getProperty("gedcom");
        if (gedName==null) return;
        isRestored = true;
        waitStartup(gedName);
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public GenjViewTopComponent create() {
        try {
            return this.getClass().newInstance();
            //return Constructor.newInstance(this);
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
        //return Constructor.newInstance(this);
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
//        map.put("iconBase", Images((new EditViewFactory()).getImage().toString())); // NOI18N
//        map.put("noIconInMenu", false); // NOI18N
return                 new OpenGenjViewAction((GenjViewTopComponent) map.get("component"), map);
//
//
//        return Actions.alwaysEnabled(
//                new OpenGenjViewAction((GenjViewTopComponent) map.get("component"), map),
//                (String) map.get("displayName"), // NOI18N
//                (String) map.get("iconBase"), // NOI18N
//                Boolean.TRUE.equals(map.get("noIconInMenu")) // NOI18N
//                );
    }

    ViewFactory getViewFactory() {
        return null;
    }

    void init() {
        setName();
        setToolTipText();
        if (getViewFactory() == null)
            return;
        setPanel(getViewFactory());
        String gedcomName;
        if ((gedcom != null) && ((gedcomName = gedcom.getName())!=null)){
            setName(gedcomName);
            setToolTipText(getToolTipText() + ": " + gedcomName);
        }
        setIcon(getViewFactory().getImage().getImage());
    }

    // TODO: refactor
    void init(Gedcom ged) {
        setName();
        setToolTipText();
        if (getViewFactory() == null)
            return;
        setPanel(ged,getViewFactory());
        String gedcomName;
        if ((gedcom != null) && ((gedcomName = gedcom.getName())!=null)){
            setName(gedcomName);
            setToolTipText(getToolTipText() + ": " + gedcomName);
        }
        setIcon(getViewFactory().getImage().getImage());
    }

    //FIXME: revoir la synchro avec le CC
    void waitStartup(String name){
        final String gedName = name;
                new Thread(new Runnable() {
            public void run() {
                while (!App.center.isReady(0))
                        ;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                    init(App.center.getOpenedGedcom(gedName));
                    open();
                }
            });
            }
        }).start();

    }


    void setName() {
        setName(getViewFactory().getTitle(true));
    }
    void setToolTipText(){
        setToolTipText(getViewFactory().getTitle(false));
    }

    public Gedcom getSelectedGedcom() {
        return App.center.getSelectedGedcom();
    }

}
