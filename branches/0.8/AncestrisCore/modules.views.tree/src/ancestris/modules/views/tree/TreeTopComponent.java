/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2011 Ancestris
 *
 * Author: Daniel Andre (daniel@ancestris.org).
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.views.tree;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisViewInterface;
import ancestris.view.GenjViewTopComponent;
import genj.tree.TreeView;
import genj.tree.TreeViewFactory;
import genj.view.ViewFactory;
import java.awt.BorderLayout;
import javax.swing.JToolBar;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.RetainLocation;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//ancestris.app//Tree//EN",
        autostore = false)
@RetainLocation(AncestrisDockModes.OUTPUT)
@ServiceProvider(service = AncestrisViewInterface.class)
public final class TreeTopComponent extends GenjViewTopComponent {

    private static TreeTopComponent factory;
    private static final ViewFactory viewfactory = new TreeViewFactory();
    private static final String PREFERRED_ID = "TreeTopComponent";

    @Override
    public ViewFactory getViewFactory() {
        return viewfactory;
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     * @return Factory for this TC
     */
    public static synchronized TreeTopComponent getFactory() {
        if (factory == null) {
            factory = new TreeTopComponent();
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
    public boolean createPanel() {
        if (!super.createPanel()) {
            return false;
        }
        //XXX: must be redesign to merge treeTC and TreeView. that way proxying GenjView
        //will not be necessary and lookup interface will be better
        AncestrisPlugin.register(this);
        String root = getContext().getGedcom().getRegistry().get("tree.root", (String) null);
        TreeView v = (TreeView) getView();
        if (root != null) {
            v.setRoot(getContext().getGedcom().getEntity(root));
        }
        if (v.getRoot() == null) {
            v.setRoot(getContext().getEntity());
        }
        return true;
    }

    /**
     * ToolBar allways at top
     * @param bar
     * @param constraints 
     * @return  BorderLayout.NORTH
     */
    @Override
    protected Object getToolBarConstraints(JToolBar bar, Object constraints){
        return BorderLayout.NORTH;
    }

    // FIXME: we save treeview settings here because TreeView.remove is called twice
    // The first time it is called the view is centered on root entity then we loose 
    // the tree view placement.
    @Override
    public void componentClosed() {
        TreeView v = (TreeView) getView();
        v.writeProperties();
        super.componentClosed();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
