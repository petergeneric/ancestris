/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app;

import genj.tree.TreeViewFactory;
import genj.view.ViewFactory;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
    dtd="-//genjfr.app//Tree//EN",
    autostore=false
)
public final class TreeTopComponent extends GenjViewTopComponent {

    private static TreeTopComponent factory;
    private static ViewFactory viewfactory = new TreeViewFactory();

    private static final String PREFERRED_ID = "TreeTopComponent";

    ViewFactory getViewFactory() {
        return viewfactory;
    }


    @Override
    String getDefaultMode() {return "genjfr-output";}
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
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized TreeTopComponent getFactory() {
        if (factory == null) {
            factory = new TreeTopComponent();
        }
        return factory;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
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
        p.setProperty("gedcom",gedcom.getOrigin().toString());
        // TODO store your settings
    }


    Object readProperties(java.util.Properties p) {
//FIXME        EditTopComponent singleton = EditTopComponent.getDefault();
//        singleton.readPropertiesImpl(p);
//        return singleton;
        readPropertiesImpl(p);
        return this;
    }

//    private void readPropertiesImpl(java.util.Properties p) {
//        String version = p.getProperty("version");
////        String gedName = p.getProperty("gedcom");
////        if (gedName!=null)
////            App.center.load(new String[]{gedName});
////        setPanel(App.center.getOpenedGedcom(gedName),new EditViewFactory());
//        // TODO read your settings according to their version
//    }
    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
