/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.views.entity;

import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisViewInterface;
import ancestris.view.GenjViewTopComponent;
import genj.entity.EntityViewFactory;
import genj.view.ViewFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Top component which displays something.
 */
@ServiceProvider(service = AncestrisViewInterface.class)
public final class EntityTopComponent extends GenjViewTopComponent {

    private static final String PREFERRED_ID = "EntityTopComponent";
    private static EntityTopComponent factory;
    private static ViewFactory viewfactory = new EntityViewFactory();

    public ViewFactory getViewFactory() {
        return viewfactory;
    }

    @Override
    public String getAncestrisDockMode() {
        return AncestrisDockModes.PROPERTIES;
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only, i.e. deserialization routines; otherwise you could get a
     * non-deserialized instance. To obtain the singleton instance, use
     * {@link #findInstance}.
     */
    public static synchronized EntityTopComponent getFactory() {
        if (factory == null) {
            factory = new EntityTopComponent();
        }
        return factory;
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
