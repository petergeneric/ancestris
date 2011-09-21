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
package ancestris.core.modules.nav;

import ancestris.view.GenjViewTopComponent;
import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisViewInterface;
import ancestris.core.modules.nav.NavigatorViewFactory;
import genj.view.ViewFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.lookup.ServiceProvider;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//ancestris.app//Navigator//EN",
autostore = false)
@ServiceProvider(service = AncestrisViewInterface.class)
public final class NavigatorTopComponent extends GenjViewTopComponent {

    private static final String PREFERRED_ID = "NavigatorTopComponent";
    private static NavigatorTopComponent factory;
    private static ViewFactory viewfactory = new NavigatorViewFactory();

    @Override
    public String getDefaultFactoryMode() {
        return AncestrisDockModes.NAV;
    }

    public ViewFactory getViewFactory() {
        return viewfactory;
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized NavigatorTopComponent getFactory() {
        if (factory == null) {
            factory = new NavigatorTopComponent();
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
