package ancestris.modules.editors.genealogyeditor;

import ancestris.core.pluginservice.AncestrisPlugin;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author dominique
 */
@ServiceProvider(service = ancestris.core.pluginservice.PluginInterface.class)
public class GenealogyEditorPlugin extends AncestrisPlugin {
    @Override
    public Collection<Class<? extends TopComponent>> getDefaultOpenedViews() {
        List<Class<? extends TopComponent>> result = new ArrayList<Class<? extends TopComponent>>(1);
//        result.add(EditorTopComponent.class);
        return result;
    }
}
