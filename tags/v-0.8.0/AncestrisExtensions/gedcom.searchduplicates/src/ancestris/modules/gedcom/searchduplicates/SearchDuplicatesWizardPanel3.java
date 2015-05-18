package ancestris.modules.gedcom.searchduplicates;

import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class SearchDuplicatesWizardPanel3 implements WizardDescriptor.Panel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SearchDuplicatesVisualPanel3 component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public SearchDuplicatesVisualPanel3 getComponent() {
        if (component == null) {
            component = new SearchDuplicatesVisualPanel3();
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        // use wiz.getProperty to retrieve previous panel state
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        wiz.putProperty("individualSelectedOptions", ((SearchDuplicatesVisualPanel3) getComponent()).getSelectedOptions());
    }
}
