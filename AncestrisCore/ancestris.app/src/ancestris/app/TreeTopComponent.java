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
package ancestris.app;

import ancestris.view.GenjViewTopComponent;
import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisViewInterface;
import genj.tree.TreeView;
import genj.tree.TreeViewFactory;
import genj.view.ViewFactory;
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
    private static ViewFactory viewfactory = new TreeViewFactory();
    private static final String PREFERRED_ID = "TreeTopComponent";

    public ViewFactory getViewFactory() {
        setSizeCorrect(false);
        return viewfactory;
    }

    @Override
    public String getDefaultFactoryMode() {
        return AncestrisDockModes.OUTPUT;
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized TreeTopComponent getFactory() {
        if (factory == null) {
            factory = new TreeTopComponent();
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
    public boolean createPanel() {
        if (!super.createPanel()) {
            return false;
        }
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

    @Override
    public void runWhenSizeIsCorrect() {
        TreeView v = (TreeView) getView();
        v.setRoot(v.getRoot());
        v.show(v.getContext().getEntity(), true);
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
