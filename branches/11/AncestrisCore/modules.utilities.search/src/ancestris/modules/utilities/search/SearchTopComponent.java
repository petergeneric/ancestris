/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.utilities.search;

import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisViewInterface;
import ancestris.view.GenjViewTopComponent;
import genj.search.SearchViewFactory;
import genj.view.ViewFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Top component which displays something.
 */
@ServiceProvider(service = AncestrisViewInterface.class)
public final class SearchTopComponent extends GenjViewTopComponent {

    private static final String PREFERRED_ID = "SearchTopComponent";
    private static SearchTopComponent factory;
    private final static ViewFactory VIEW_FACTORY = new SearchViewFactory();

    @Override
    public String getAncestrisDockMode() {
        return AncestrisDockModes.PROPERTIES;
    }

    @Override
    public ViewFactory getViewFactory() {
        return VIEW_FACTORY;
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     * @return 
     */
    public static synchronized SearchTopComponent getFactory() {
        if (factory == null) {
            factory = new SearchTopComponent();
        }
        return factory;
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

}
