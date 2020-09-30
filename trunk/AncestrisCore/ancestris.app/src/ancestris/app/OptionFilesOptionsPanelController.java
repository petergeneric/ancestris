/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.TopLevelRegistration(
        categoryName = "#OptionsCategory_Name_OptionFiles",
        iconBase = "ancestris/view/images/OptionFiles.png",
        id = "OptionFiles",
        keywords = "#OptionsCategory_Keywords_OptionFiles",
        keywordsCategory = "OptionFiles",
        position = 3)
@OptionsPanelController.Keywords(keywords={"#OptionsCategory_Keywords_OptionFiles"}, location="OptionFiles", tabTitle="default")
public final class OptionFilesOptionsPanelController extends OptionsPanelController {

    private OptionFilesPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;

    public void update() {
        getPanel().load();
        //changed = false;
    }

    public void applyChanges() {
        getPanel().store();
        App.setLogLevel(getPanel().getLogLevel());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ActionOpenDefault action = Lookup.getDefault().lookup(ActionOpenDefault.class);
                action.putValue("Name", action.getName());
            }
        });
        //changed = false;
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

    private OptionFilesPanel getPanel() {
        if (panel == null) {
            panel = new OptionFilesPanel(this);
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
