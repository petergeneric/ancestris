package ancestris.modules.console;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

// FL : 2016-01-23 - de-activate until option to activate or desactivate is clearly identified
//@OptionsPanelController.SubRegistration(location = "Advanced",
//displayName = "#AdvancedOption_DisplayName_ConsoleOptions",
//keywords = "#AdvancedOption_Keywords_ConsoleOptions",
//keywordsCategory = "Extensions/ConsoleOptions")
public final class ConsoleOptionsPanelController extends OptionsPanelController {

    private ConsoleOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;

    @Override
    public void update() {
        getPanel().load();
        //changed = false;
    }

    @Override
    public void applyChanges() {
        getPanel().store();
        //changed = false;
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        return true; //getPanel().valid();
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private ConsoleOptionsPanel getPanel() {
        if (panel == null) {
            panel = new ConsoleOptionsPanel(this);
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
