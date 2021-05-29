/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.core.report;

import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisViewInterface;
import ancestris.view.GenjViewTopComponent;
import genj.report.ReportViewFactory;
import genj.view.ViewFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Top component which displays something.
 */
@ServiceProvider(service = AncestrisViewInterface.class)
public final class ReportTopComponent extends GenjViewTopComponent {

    private static final String PREFERRED_ID = "ReportTopComponent";
    private static ReportTopComponent factory;
    private static ViewFactory viewfactory = new ReportViewFactory();

    @Override
    public String getAncestrisDockMode() {
        return AncestrisDockModes.OUTPUT;
    }

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

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
