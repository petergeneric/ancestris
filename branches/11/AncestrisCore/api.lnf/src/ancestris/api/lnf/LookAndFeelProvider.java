/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.api.lnf;

import javax.swing.ImageIcon;
import org.openide.util.Lookup;

/**
 * @author daniel
 */
public abstract class LookAndFeelProvider {

    /**
     * Return the uniq name for this LnF provider. .
     * It can be used to store setting. For instance this name is used in 
     * {@link LookAndFeelProvider#getProviderFromName(java.lang.String) }.
     * 
     * @return The uniq name for this LnF provider. 
     * 
     */
    public abstract String getName();

    /**
     * Returns the jar class path for this lnf implementor or null if core lnf
     * @return
     */
    public String getClassPath(){
        return null;
    }

    /**
     * A localized display name for this provider.
     * @return 
     */
    public abstract String getDisplayName();

    /**
     * A localized sample image for this provider.
     * @return 
     */
    public abstract ImageIcon getSampleImage();

    @Override
    public String toString() {
        return getDisplayName();
    }

    /**
     * Gets all LookAndFeel Providers installed in system.
     * @return 
     */
    public static LookAndFeelProvider[] getProviders() {
        return (Lookup.getDefault().lookupAll(LookAndFeelProvider.class)).toArray(new LookAndFeelProvider[]{});
    }

    /**
     * retreive LnF provider from its uniq name. Default to standard LnF
     * @param name
     * @return 
     */
    public static LookAndFeelProvider getProviderFromName(String name) {
        if (name != null) {
            for (LookAndFeelProvider provider : Lookup.getDefault().lookupAll(LookAndFeelProvider.class)) {
                if (name.equals(provider.getName())) {
                    return provider;
                }
            }
        }
        return LnFStandard.getDefault();
    }
}
