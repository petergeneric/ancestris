package ancestris.modules.releve;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.SubRegistration(
location = "Extensions",
id = "Releve",
displayName = "#OpenIDE-Module-Name",
keywords = "#AdvancedOption_Keywords_Registers",
keywordsCategory = "Extensions/Releve",
position=400)
@OptionsPanelController.Keywords(keywords={"#AdvancedOption_Keywords_Registers"}, location="Extensions", tabTitle="#OpenIDE-Module-Name")
public final class ReleveOptionsPanelController extends OptionsPanelController {

    ReleveOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;

    @Override
    public void update() {
        getPanel().loadPreferences();
        changed = false;
    }

    @Override
    public void applyChanges() {
        getPanel().savePreferences();
        changed = false;
    }

    @Override
    public void cancel() {
        // rien a faire
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private ReleveOptionsPanel getPanel() {
        if (panel == null) {
            panel = new ReleveOptionsPanel();
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
