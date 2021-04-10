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
import java.awt.BorderLayout;
import javax.swing.JToolBar;
import org.openide.util.lookup.ServiceProvider;

/**
 * Top component which displays something.
 */
@ServiceProvider(service = AncestrisViewInterface.class)
public final class TableTopComponent extends GenjViewTopComponent {

    private static final String PREFERRED_ID = "TableTopComponent";
    private static TableTopComponent factory;
    private static final ViewFactory viewfactory = new TableViewFactory();
 
    @Override
    public String getAncestrisDockMode() {
        return AncestrisDockModes.TABLE;
    }

    @Override
    public ViewFactory getViewFactory() {
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
    protected String preferredID() {
        return PREFERRED_ID;
    }

    /**
     * ToolBar allways at top
     *
     * @param bar
     * @param constraints
     *
     * @return
     */
    @Override
    protected Object getToolBarConstraints(JToolBar bar, Object constraints) {
        return BorderLayout.NORTH;
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
    }

    
    
}
