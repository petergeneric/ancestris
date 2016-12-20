/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.webbook;

import genj.gedcom.Gedcom;
import genj.util.Registry;
import java.awt.Component;
import java.io.File;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class WebBookWizardPanel5 implements WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {

    // Gedcom is used to load and store settings for the webbook as "one set of settings per gedcom"
    private Gedcom gedcom;
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private WebBookVisualPanel5 component;

    /**
     * Constructor
     * @param gedcom
     */
    WebBookWizardPanel5(Gedcom gedcom) {
        this.gedcom = gedcom;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new WebBookVisualPanel5();
        }
        return component;
    }

    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        //return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        return new HelpCtx("ancestris.app.tools.webbook.step5");
    }

    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
    }

    public final void addChangeListener(ChangeListener l) {
    }

    public final void removeChangeListener(ChangeListener l) {
    }
    /*
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
    public final void addChangeListener(ChangeListener l) {
    synchronized (listeners) {
    listeners.add(l);
    }
    }
    public final void removeChangeListener(ChangeListener l) {
    synchronized (listeners) {
    listeners.remove(l);
    }
    }
    protected final void fireChangeEvent() {
    Iterator<ChangeListener> it;
    synchronized (listeners) {
    it = new HashSet<ChangeListener>(listeners).iterator();
    }
    ChangeEvent ev = new ChangeEvent(this);
    while (it.hasNext()) {
    it.next().stateChanged(ev);
    }
    }
     */

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        if (gedcom == null) {
            return;
        }
        Registry gedcomSettings = gedcom.getRegistry();

        ((WebBookVisualPanel5) getComponent()).setPref01(gedcomSettings.get(WebBookParams.WB_PREFIX + ".localWebDir", ""));
        ((WebBookVisualPanel5) getComponent()).setPref02(gedcomSettings.get(WebBookParams.WB_PREFIX + ".logFile", ""));
        component.setComponents();
    }

    public void storeSettings(Object settings) {
        if (gedcom == null) {
            return;
        }
        Registry gedcomSettings = gedcom.getRegistry();

        gedcomSettings.put(WebBookParams.WB_PREFIX + ".localWebDir", ((WebBookVisualPanel5) getComponent()).getPref01());
        gedcomSettings.put(WebBookParams.WB_PREFIX + ".logFile", ((WebBookVisualPanel5) getComponent()).getPref02());
    }

    /*
     * Allow the finish button for this panel
     */
    public boolean isFinishPanel() {
        return false;
    }

    public void validate() throws WizardValidationException {
        String name = component.getPref01();
        // check if file name has been provided
        if (name.trim().isEmpty()) {
            throw new WizardValidationException(null, NbBundle.getMessage(WebBookWizardAction.class, "CTRL_Mandatory_LocalWebDir"), null);
        }
        // Check if file exists
        File file = new File(name);
        if (!file.exists()) {
            throw new WizardValidationException(null, NbBundle.getMessage(WebBookWizardAction.class, "CTRL_Invalid_LocalWebDir"), null);
        }
        // Check if file is a directory
        if (!file.isDirectory()) {
            throw new WizardValidationException(null, NbBundle.getMessage(WebBookWizardAction.class, "CTRL_NotDir_LocalWebDir"), null);
        }
        // Check that directory is writable
        if (!file.canWrite()) {
            throw new WizardValidationException(null, NbBundle.getMessage(WebBookWizardAction.class, "CTRL_NotWritable_LocalWebDir"), null);
        }
        // Check that if directory is not empty, that if contains recognisable webbook directories
        if (file.list().length != 0) {
            String[] expectedFilesArray = {"names", "persons", "cities", "theme", "daysdetails", "repsosa", "citiesdetails"};
            int foundWebbookFiles = 0;
            for (int i = 0; i < expectedFilesArray.length; i++) {
                file = new File(name + File.separator + expectedFilesArray[i]);
                if (file != null && file.exists() && file.isDirectory()) {
                    foundWebbookFiles++;
                }
            }
            if (foundWebbookFiles < 3) {
                throw new WizardValidationException(null, NbBundle.getMessage(WebBookWizardAction.class, "CTRL_CautionForDeletion_LocalWebDir"), null);
            }
        }
        //
        name = component.getPref02();
        if (name.trim().isEmpty()) {
            throw new WizardValidationException(null, NbBundle.getMessage(WebBookWizardAction.class, "CTRL_Mandatory_LogFile"), null);
        } else {
            file = new File(name);
            if (!file.getParentFile().exists()) {
                throw new WizardValidationException(null, NbBundle.getMessage(WebBookWizardAction.class, "CTRL_Invalid_LogDir"), null);
            }
        }
    }
}
