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
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.RetainLocation;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//ancestris.app//Search//EN",
autostore = false)
@ServiceProvider(service = AncestrisViewInterface.class)
@RetainLocation(AncestrisDockModes.PROPERTIES)
public final class SearchTopComponent extends GenjViewTopComponent {

    private static final String PREFERRED_ID = "SearchTopComponent";
    private static SearchTopComponent factory;
    private final static ViewFactory VIEW_FACTORY = new SearchViewFactory();

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
    public void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        super.writeProperties(p);
    }

    @Override
    public void readProperties(java.util.Properties p) {
        super.readProperties(p);
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

}
