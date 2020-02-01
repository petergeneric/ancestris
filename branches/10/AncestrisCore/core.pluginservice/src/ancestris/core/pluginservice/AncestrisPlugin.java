/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.core.pluginservice;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/**
 *
 * @author daniel
 */
public abstract class AncestrisPlugin implements PluginInterface {

    private final static InstanceContent ic = new InstanceContent();
    private static final AbstractLookup abstractLookup = new AbstractLookup(ic);
    private final ModuleInfo info;

    public AncestrisPlugin() {
        info = Modules.getDefault().ownerOf(getClass());
    }

    public static void register(Object o) {
        ic.add(o);
    }

    public static void unregister(Object o) {
        ic.remove(o);
    }

    public static <T> Collection<? extends T> lookupAll(Class<T> clazz) {
        return abstractLookup.lookupAll(clazz);
    }

    public static <T> T lookup(Class<T> clazz) {
        return abstractLookup.lookup(clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> lookupForName(Class<T> clazz, String name) {
        for (T sInterface : Lookup.getDefault().lookupAll(clazz)) {
            if (sInterface.getClass().getCanonicalName().equals(name)) {
                return (Class<T>) sInterface.getClass();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> Collection<Class<T>> lookupForName(Class<T> clazz, String[] names) {
        List<Class<T>> openedViews = new ArrayList<Class<T>>();

        if (names != null) {
            List<String> namesList = Arrays.asList(names);
            for (T sInterface : Lookup.getDefault().lookupAll(clazz)) {
                if (namesList.contains(sInterface.getClass().getCanonicalName())) {
                    openedViews.add((Class<T>) (sInterface.getClass()));
                }
            }
        }
        return openedViews;
    }

    @Override
    public String getPluginName() {
        return info.getCodeNameBase();
    }

    @Override
    public String getPluginDisplayName() {
        return info.getDisplayName();
    }

    /**
     * gets Plugin (module) specification version string. As of development
     * version, this string must be in the form 1.0.0.t (see
     * http://trac.ancestris.org/ancestris/wiki/AncestrisDevPlugins). We are
     * still in pre 1.0 release version. if this is true, this method returns
     * 0.t (eg 0.3.1). Otherwise return full specification version string.
     *
     * If Build-Version or Implementation-Version is available, appends it to
     * the retruned version string
     *
     * @return AncestrisPlugin version string
     */
    @Override
    public String getPluginVersion() {
        return info.getSpecificationVersion().toString();
    }

    @Override
    public String getPluginShortDescription() {
        return info.getLocalizedAttribute("OpenIDE-Module-Short-Description").toString();
    }

    @Override
    public String getPluginDescription() {
        return info.getLocalizedAttribute("OpenIDE-Module-Long-Description").toString();
    }

    @Override
    public boolean launchModule(Object o) {
        return true;
    }

    @Override
    public Collection<Class<? extends TopComponent>> getDefaultOpenedViews() {
        return new ArrayList<Class<? extends TopComponent>>();
    }

    @Override
    public int compareTo(PluginInterface o) {
        if (o == null){
            return 1;
        }
        return safeCompare(this.getPluginDisplayName(),o.getPluginDisplayName());
    }
    
    private int safeCompare(String s1, String s2){
        //FIXME: share collator somewhere?
        Collator coll = Collator.getInstance(Locale.getDefault());
        coll.setStrength(Collator.PRIMARY);
        if (s1 == null){
            return s2 == null?0:-1;
        } else {
            return s2 == null?1:coll.compare(s1, s2);
        }
    }
}
