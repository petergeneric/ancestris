/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app.tools.optionswizard;

import genjfr.app.pluginservice.GenjFrPlugin;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service=genjfr.app.pluginservice.PluginInterface.class)
public class WizardPlugin extends GenjFrPlugin{
    @Override
    public boolean launchModule(Object o) {
        return OptionsWizardWizardAction.getDefault().launchModule(o);
    }
}
