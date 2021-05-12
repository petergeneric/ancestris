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
    private final Gedcom gedcom;
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
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new WebBookVisualPanel5();
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("ancestris.app.tools.webbook.step5");
    }

    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(Object settings) {
        if (gedcom == null) {
            return;
        }
        Registry gedcomSettings = gedcom.getRegistry();
        
        String localWebDir = gedcomSettings.get(WebBookParams.WB_PREFIX + ".localWebDir", "");
        if (localWebDir.isEmpty()) {
            localWebDir = System.getProperty("netbeans.user") + File.separator + "WebBook";
        }
        ((WebBookVisualPanel5) getComponent()).setPref01(localWebDir);
        
        String logFile = gedcomSettings.get(WebBookParams.WB_PREFIX + ".logFile", "");
        if (logFile.isEmpty()) {
            logFile = localWebDir + File.separator + "webbookLog.txt";
        }
        ((WebBookVisualPanel5) getComponent()).setPref02(logFile);
        component.setComponents();
    }

    @Override
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
    @Override
    public boolean isFinishPanel() {
        return false;
    }

    @Override
    public void validate() throws WizardValidationException {
        String name = component.getPref01();
        // check if file name has been provided
        if (name.trim().isEmpty()) {
            throw new WizardValidationException(null, NbBundle.getMessage(WebBookWizardAction.class, "CTRL_Mandatory_LocalWebDir"), null);
        }
        new File(name).mkdirs();
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
                if (file.exists() && file.isDirectory()) {
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
