package ancestris.modules.editors.genealogyeditor;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.editors.genealogyeditor.actions.GenealogyEditorAction;
import genj.gedcom.GedcomOptions;
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
        List<Class<? extends TopComponent>> result = new ArrayList<>(1);
        if (GenealogyEditorAction.class.getCanonicalName().startsWith(GedcomOptions.getInstance().getDefaultEditor())) {
            result.add(AriesTopComponent.class);
        }
        return result;
    }
}
