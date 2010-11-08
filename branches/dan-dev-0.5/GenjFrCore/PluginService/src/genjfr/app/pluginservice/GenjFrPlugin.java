/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app.pluginservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author daniel
 */
public abstract class GenjFrPlugin implements PluginInterface {
    private final static InstanceContent ic = new InstanceContent();
    private static AbstractLookup abstractLookup = new AbstractLookup(ic);

    public static void register (Object o){
        ic.add(o);
    }

    public static void unregister (Object o){
        ic.remove(o);
    }

    public static <T> Collection<? extends T> lookupAll(Class<T> clazz) {
        return abstractLookup.lookupAll(clazz);
    }

    public static <T> T lookup(Class<T> clazz) {
        return abstractLookup.lookup(clazz);
    }

    public static Class lookupForName(Class clazz,String name){
        for (Object sInterface : Lookup.getDefault().lookupAll(clazz)) {
                if (sInterface.getClass().getCanonicalName().equals(name))
                    return sInterface.getClass();
            }
        return null;
    }

    public static Collection<Class> lookupForName(Class clazz,String[] names){
        List<Class> openedViews = new ArrayList<Class>();

        if (names != null){
            List<String> namesList = Arrays.asList(names);
            for (Object sInterface : Lookup.getDefault().lookupAll(clazz)) {
                    if (namesList.contains(sInterface.getClass().getCanonicalName()))
                        openedViews.add(sInterface.getClass());
                }
        }
        return openedViews;
    }
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
        return version;
//        String buildVersion = PluginHelper.getManifestMainAttributes(this.getClass()).getValue("OpenIDE-Module-Build-Version");
//        if (buildVersion == null || buildVersion.isEmpty())
//            buildVersion = PluginHelper.getManifestMainAttributes(this.getClass()).getValue("OpenIDE-Module-Implementation-Version");
//        return version.replaceFirst("1\\.0\\.0", "0")+(buildVersion!=null?"-r"+buildVersion:"");
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

    public Collection<Class> getDefaultOpenedViews() {
        return new ArrayList<Class>();
    }
}
