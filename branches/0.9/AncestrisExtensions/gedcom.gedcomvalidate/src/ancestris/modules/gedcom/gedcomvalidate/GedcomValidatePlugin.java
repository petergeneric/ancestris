package ancestris.modules.gedcom.gedcomvalidate;

import ancestris.core.pluginservice.AncestrisPlugin;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.util.Validator;
import genj.view.ViewContext;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service=ancestris.core.pluginservice.PluginInterface.class)
public class GedcomValidatePlugin extends AncestrisPlugin implements Validator {
    
    @Override
    public List<ViewContext> start(Gedcom gedcom) {
        return new GedcomValidate().start(gedcom);
    }

    @Override
    public List<ViewContext> start(Entity e) {
        return new GedcomValidate().start(e);
    }

    @Override
    public void cancelTrackable() {
    }

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public String getState() {
        return "";
    }

    @Override
    public String getTaskName() {
        return "";
    }


}
