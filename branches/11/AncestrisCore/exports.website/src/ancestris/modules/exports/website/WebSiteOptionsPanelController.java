/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.exports.website;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.SubRegistration(location = "Extensions", id = "WebSiteOptions",
displayName = "#AdvancedOption_DisplayName_WebSite",
keywords = "#AdvancedOption_Keywords_WebSite",
keywordsCategory = "General/WebSite",
position=700)
@OptionsPanelController.Keywords(keywords={"#AdvancedOption_Keywords_WebSite"}, location="Extensions", tabTitle="#AdvancedOption_DisplayName_WebSite")
public final class WebSiteOptionsPanelController extends OptionsPanelController {

    private WebSitePanel panel;
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
        getPanel().cancel();
        changed = false;
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

    private WebSitePanel getPanel() {
        if (panel == null) {
            panel = new WebSitePanel(this);
            changed = true;   // changed needs to be true to enable the Apply Button. I choose to always enable it.
        }
        return panel;
    }

    void changed() {
        if (!changed) {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
}
