/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.app;

import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisViewInterface;
import genj.gedcom.Property;
import genj.search.SearchView;
import genj.search.SearchViewFactory;
import genj.view.ViewFactory;
import java.util.List;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.lookup.ServiceProvider;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
    dtd="-//ancestris.app//Search//EN",
    autostore=false
)
@ServiceProvider(service=AncestrisViewInterface.class)
public final class SearchTopComponent extends GenjViewTopComponent {

    private static final String PREFERRED_ID = "SearchTopComponent";
    private static SearchTopComponent factory;
    private static ViewFactory viewfactory = new SearchViewFactory();

    public ViewFactory getViewFactory() {
        return viewfactory;
    }


    @Override
    public String getDefaultFactoryMode() {return AncestrisDockModes.PROPERTIES;}

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
    public static synchronized SearchTopComponent getFactory() {
        if (factory == null) {
            factory = new SearchTopComponent();
        }
        return factory;
    }

    public void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        super.writeProperties(p);
    }

    public Object readProperties(java.util.Properties p) {
        super.readProperties(p);
        return this;
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public List<Property> getResultProperties() {
//        return ((SearchViewFactory)viewfactory).getEditView().getResults();
        return ((SearchView) getView()).getResults();
}

}
