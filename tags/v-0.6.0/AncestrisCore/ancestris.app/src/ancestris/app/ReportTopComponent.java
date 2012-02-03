/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.view.GenjViewTopComponent;
import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisViewInterface;
import genj.report.ReportViewFactory;
import genj.view.ViewFactory;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.RetainLocation;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//ancestris.app//Report//EN",
autostore = false)
@ServiceProvider(service = AncestrisViewInterface.class)
@RetainLocation(AncestrisDockModes.OUTPUT)
public final class ReportTopComponent extends GenjViewTopComponent {

    private static final String PREFERRED_ID = "ReportTopComponent";
    private static ReportTopComponent factory;
    private static ViewFactory viewfactory = new ReportViewFactory();

    public ViewFactory getViewFactory() {
        return viewfactory;
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized ReportTopComponent getFactory() {
        if (factory == null) {
            factory = new ReportTopComponent();
        }
        return factory;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
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
