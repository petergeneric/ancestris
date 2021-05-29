/*                         
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.editors.gedcom;

import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import ancestris.view.GenjViewTopComponent;
import genj.edit.EditViewFactory;
import genj.view.ViewFactory;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ServiceProvider(service = AncestrisViewInterface.class)
public final class GedcomTopComponent extends GenjViewTopComponent implements TopComponent.Cloneable {

    private static final String PREFERRED_ID = "GedcomTopComponent";
    private static GedcomTopComponent factory;
    private ViewFactory viewfactory = new EditViewFactory();  // should not be static

    @Override
    public String getAncestrisDockMode() {
        return AncestrisDockModes.EDITOR;
    }

    public ViewFactory getViewFactory() {
        return viewfactory;
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized GedcomTopComponent getFactory() {
        if (factory == null) {
            factory = new GedcomTopComponent();
        }
        return factory;
    }

    public TopComponent cloneComponent() {
        if (getContext() == null) {
            return null;
        }

        AncestrisTopComponent topComponent = new GedcomTopComponent();
        topComponent.init(getContext());
        return topComponent;
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
    }

    
}
