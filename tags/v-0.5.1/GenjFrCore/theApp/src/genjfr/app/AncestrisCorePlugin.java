/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app;

import genjfr.app.pluginservice.GenjFrPlugin;
import java.util.Arrays;
import java.util.Collection;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service=genjfr.app.pluginservice.PluginInterface.class)
public class AncestrisCorePlugin extends GenjFrPlugin{

    @Override
    public Collection<Class> getDefaultOpenedViews() {
        return Arrays.asList(new Class[]{
            TreeTopComponent.class,
            TableTopComponent.class,
            EditTopComponent.class
        });
    }

}
