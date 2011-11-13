/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.view.GenjViewTopComponent;
import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisViewInterface;
import genj.timeline.TimelineViewFactory;
import genj.view.ViewFactory;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.RetainLocation;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//ancestris.app//Timeline//EN",
autostore = false)
@RetainLocation(AncestrisDockModes.TABLE)
@ServiceProvider(service = AncestrisViewInterface.class)
public final class TimelineTopComponent extends GenjViewTopComponent {

    private static final String PREFERRED_ID = "TimelineTopComponent";
    private static TimelineTopComponent factory;
    private static ViewFactory viewfactory = new TimelineViewFactory();

    public ViewFactory getViewFactory() {
        return viewfactory;
    }

    @Override
    public String getDefaultFactoryMode() {
        return AncestrisDockModes.TABLE;
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized TimelineTopComponent getFactory() {
        if (factory == null) {
            factory = new TimelineTopComponent();
        }
        return factory;
    }

    public void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        super.writeProperties(p);
    }

    public void readProperties(java.util.Properties p) {
        super.readProperties(p);
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
