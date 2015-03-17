package ancestris.modules.exports.website;

import ancestris.core.pluginservice.AncestrisPlugin;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service=ancestris.core.pluginservice.PluginInterface.class)
public class WebSiteExportPlugin extends AncestrisPlugin{
    private static ReportWebsite instance = null;

    static ReportWebsite getReport(){
        if (instance== null)
            instance = new ReportWebsite();
        return instance;
    }

}
