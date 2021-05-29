package ancestris.modules.feedback;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.SubRegistration(location = "Extensions",
id = "FeedBack",
displayName = "#AdvancedOption_DisplayName_FeedBack",
keywords = "#AdvancedOption_Keywords_FeedBack",
keywordsCategory = "Extensions/FeedBack",
position=100)
@OptionsPanelController.Keywords(keywords={"#AdvancedOption_Keywords_FeedBack"}, location="Extensions", tabTitle="#AdvancedOption_DisplayName_FeedBack")
public final class FeedBackOptionsPanelController extends OptionsPanelController {

    private FeedBackOptionPanel panel;
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

    private FeedBackOptionPanel getPanel() {
        if (panel == null) {
            panel = new FeedBackOptionPanel(this);
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
