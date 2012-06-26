/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.views.table;

import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisViewInterface;
import ancestris.view.GenjViewTopComponent;
import genj.table.TableViewFactory;
import genj.view.ViewFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.RetainLocation;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//ancestris.app//Table//EN",
autostore = false)
@RetainLocation(AncestrisDockModes.TABLE)
@ServiceProvider(service = AncestrisViewInterface.class)
public final class TableTopComponent extends GenjViewTopComponent {

    private static final String PREFERRED_ID = "TableTopComponent";
    private static TableTopComponent factory;
    private static ViewFactory viewfactory = new TableViewFactory();

    @Override
    public ViewFactory getViewFactory() {
        // to call setcontext when table size panel is correct
        setSizeCorrect(false);
        return viewfactory;
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized TableTopComponent getFactory() {
        if (factory == null) {
            factory = new TableTopComponent();
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
