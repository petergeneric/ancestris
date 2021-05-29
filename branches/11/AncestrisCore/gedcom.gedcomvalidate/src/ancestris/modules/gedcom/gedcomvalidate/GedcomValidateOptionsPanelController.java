package ancestris.modules.gedcom.gedcomvalidate;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.SubRegistration(location = "Extensions",
id = "GedcomValidateOptions",
displayName = "#AdvancedOption_DisplayName_GedcomValidateOptions",
keywords = "#AdvancedOption_Keywords_GedcomValidateOptions",
keywordsCategory = "Extensions/GedcomValidateOptions",
position=500)
@OptionsPanelController.Keywords(keywords={"#AdvancedOption_Keywords_GedcomValidateOptions"}, location="Extensions", tabTitle="#AdvancedOption_DisplayName_GedcomValidateOptions")
public final class GedcomValidateOptionsPanelController extends OptionsPanelController {

    private GedcomValidateOptionsPanel panel;
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

    private GedcomValidateOptionsPanel getPanel() {
        if (panel == null) {
            panel = new GedcomValidateOptionsPanel(this);
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
