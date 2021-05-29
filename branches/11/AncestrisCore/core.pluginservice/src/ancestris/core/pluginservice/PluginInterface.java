/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.core.pluginservice;

import java.util.Collection;
import org.openide.windows.TopComponent;

/**
 *
 * @author frederic
 */
public interface PluginInterface extends Comparable<PluginInterface>{

    public String getPluginName();
    public String getPluginDisplayName();
    public String getPluginShortDescription();
    public String getPluginDescription();

    public String getPluginVersion();
    public boolean launchModule(Object o);
    public Collection<Class<? extends TopComponent>> getDefaultOpenedViews();
}
