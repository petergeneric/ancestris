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
 *XXX: to be documented
 * @author daniel
 */
public abstract class LookAndFeelProvider {

    public abstract String getName();

    public abstract String getDisplayName();

    public abstract ImageIcon getSampleImage();

    @Override
    public String toString() {
        return getDisplayName();
    }

    public static LookAndFeelProvider[] getProviders() {
        return (Lookup.getDefault().lookupAll(LookAndFeelProvider.class)).toArray(new LookAndFeelProvider[]{});
    }

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
