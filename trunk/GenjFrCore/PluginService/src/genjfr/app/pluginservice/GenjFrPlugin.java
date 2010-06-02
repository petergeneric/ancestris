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
        return PluginHelper.getManifestMainAttributes(this.getClass()).getValue("OpenIDE-Module");
    }

    public String getPluginDisplayName() {
        String name = NbBundle.getMessage(this.getClass(),"OpenIDE-Module-Name");
        if (name != null)
            return name;
        return getPluginName();
    }

    /**
     * gets Plugin (module) specification version string. As of development version, this string must be
     * in the form 1.0.0.t (see http://trac.arvernes.dyndns.org/genjf/wiki/GenjFrDevPlugins). We are still in
     * pre 1.0 release version. if this is true, this method returns 0.t (eg 0.3.1). Otherwise return full
     * specification version string.
     *
     * If Build-Version or Implementation-Version is available, appends it to the retruned version string
     * @return GenjFrPlugin version string
     */
    public String getPluginVersion() {
        String version = PluginHelper.getManifestMainAttributes(this.getClass()).getValue("OpenIDE-Module-Specification-Version");
        String buildVersion = PluginHelper.getManifestMainAttributes(this.getClass()).getValue("OpenIDE-Module-Build-Version");
        if (buildVersion == null || buildVersion.isEmpty())
            buildVersion = PluginHelper.getManifestMainAttributes(this.getClass()).getValue("OpenIDE-Module-Implementation-Version");
        return version.replaceFirst("1\\.0\\.0", "0")+(buildVersion!=null?"-r"+buildVersion:"");
    }

    public String getPluginShortDescription() {
        String name = NbBundle.getMessage(this.getClass(),"OpenIDE-Module-Short-Description");
        return name;
    }

    public String getPluginDescription() {
        String name = NbBundle.getMessage(this.getClass(),"OpenIDE-Module-Long-Description");
        return name;
    }

    public boolean launchModule(Object o) {
        return true;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

}
