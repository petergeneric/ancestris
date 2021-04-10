/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.editors.gedcom;

import ancestris.core.pluginservice.AncestrisPlugin;
import genj.edit.actions.GedcomEditorAction;
import genj.gedcom.GedcomOptions;
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
public class GedcomEditorPlugin extends AncestrisPlugin {
    
    @Override
    public Collection<Class<? extends TopComponent>> getDefaultOpenedViews() {
        List<Class<? extends TopComponent>> result = new ArrayList<Class<? extends TopComponent>>(1);
        if (GedcomEditorAction.class.getCanonicalName().startsWith(GedcomOptions.getInstance().getDefaultEditor())) {
            result.add(GedcomTopComponent.class);
        }
        return result;
    }
}
