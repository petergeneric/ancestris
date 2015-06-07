/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/*
 * DAN 20150501: I have not succeeded to completely hide Netbeans Appearance 
 * option category pannel so we use it and place display option
 * panel in a tab.
*/
@OptionsPanelController.SubRegistration(
        location = "Appearance",
        displayName = "#OptionsTab_Name_OptionDisplay",
        //iconBase = "ancestris/app/OptionFormat.png",
        keywords = "#OptionsCategory_Keywords_OptionDisplay",
        keywordsCategory = "OptionDisplay",
        id="General",
        position = 1)
public final class OptionDisplayOptionsPanelController extends OptionsPanelController {

    private OptionDisplayPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;

    public void update() {
        getPanel().load();
        changed = false;
    }

    public void applyChanges() {
        getPanel().store();
        changed = false;
    }

    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    public boolean isValid() {
        return getPanel().valid();
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

    private OptionDisplayPanel getPanel() {
        if (panel == null) {
            panel = new OptionDisplayPanel(this);
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
