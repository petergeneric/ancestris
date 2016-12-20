/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.views.tree;

import ancestris.core.pluginservice.AncestrisPlugin;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author daniel
 */
@ServiceProvider(service = ancestris.core.pluginservice.PluginInterface.class)
public class TreeViewPlugin extends AncestrisPlugin {

    @Override
    public Collection<Class<? extends TopComponent>> getDefaultOpenedViews() {
        List<Class<? extends TopComponent>> result = new ArrayList<Class<? extends TopComponent>>(1);
        result.add(TreeTopComponent.class);
        return result;

    }
}
