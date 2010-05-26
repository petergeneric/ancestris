/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app.pluginservice;

import org.openide.util.NbBundle;

/**
 *
 * @author daniel
 */
public abstract class GenjFrPlugin implements PluginInterface {
    public String getPluginName() {
        String name = NbBundle.getMessage(this.getClass(),"OpenIDE-Module-Name");
        if (name != null)
            return name;
        return PluginHelper.getManifestMainAttributes(this.getClass()).getValue("OpenIDE-Module");
    }

    public String getPluginVersion() {
        return PluginHelper.getManifestMainAttributes(this.getClass()).getValue("OpenIDE-Module-Specification-Version");
    }

    public boolean launchModule(Object o) {
        return true;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

}
