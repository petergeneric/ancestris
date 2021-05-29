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
package ancestris.gedcom.privacy.standard;

import ancestris.core.pluginservice.AncestrisPlugin;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.SubRegistration(
        location = "OptionFormat",
        displayName = "#AdvancedOption_DisplayName_Privacy",
        keywords = "#AdvancedOption_Keywords_Privacy",
        keywordsCategory = "OptionFormat/Privacy",
        id = "Privacy",
        position = 200)
@OptionsPanelController.Keywords(keywords={"#AdvancedOption_Keywords_Privacy"}, location="OptionFormat", tabTitle="#AdvancedOption_DisplayName_Privacy")
public final class PrivacyOptionsPanelController extends OptionsPanelController {

    public static String PRIVACY_OPTIONS_CHANGED = "PrivacyOptionsChanged";
    
    private PrivacyPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;

    public void update() {
        getPanel().load();
        //changed = false;
    }

    public void applyChanges() {
        getPanel().store();
        //changed = false;
    }

    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    public boolean isValid() {
        return true; //getPanel().valid();
    }

    public boolean isChanged() {
        return changed;
    }

    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }

    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private PrivacyPanel getPanel() {
        if (panel == null) {
            panel = new PrivacyPanel(this);
            changed = true;   // changed needs to be true to enable the Apply Button. I choose to always enable it.
        }
        return panel;
    }

    public void changed() {
        if (!changed) {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
        
        // Tell the others
        for (PropertyChangeListener listener : AncestrisPlugin.lookupAll(PropertyChangeListener.class)) {
            listener.propertyChange(new PropertyChangeEvent(this, PRIVACY_OPTIONS_CHANGED, null, null));
        }
    }
}
