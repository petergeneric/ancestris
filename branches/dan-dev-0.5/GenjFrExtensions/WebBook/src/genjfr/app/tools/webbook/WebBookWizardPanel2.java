/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.webbook;

import genj.gedcom.Gedcom;
import genj.util.Registry;
import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class WebBookWizardPanel2 implements WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {

    private static final String WB_PREFIX = "webbook";


    // Gedcom is used to load and store settings for the webbook as "one set of settings per gedcom"
    private Gedcom gedcom;

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private WebBookVisualPanel2 component;

    /**
     * Constructor
     * @param gedcom
     */
    WebBookWizardPanel2(Gedcom gedcom) {
        this.gedcom = gedcom;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new WebBookVisualPanel2(gedcom);
        }
        return component;
    }

    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        //return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        return new HelpCtx("ancestris.app.tools.webbook.step2");
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

        ((WebBookVisualPanel2) getComponent()).setPref01(gedcomSettings.get(WB_PREFIX+".decujus", ""));
        ((WebBookVisualPanel2) getComponent()).setPref02(gedcomSettings.get(WB_PREFIX+".unknown", ""));
        ((WebBookVisualPanel2) getComponent()).setPref03(gedcomSettings.get(WB_PREFIX+".dispSpouse", ""));
        ((WebBookVisualPanel2) getComponent()).setPref04(gedcomSettings.get(WB_PREFIX+".dispKids", ""));
        ((WebBookVisualPanel2) getComponent()).setPref05(gedcomSettings.get(WB_PREFIX+".dispSiblings", ""));
        ((WebBookVisualPanel2) getComponent()).setPref06(gedcomSettings.get(WB_PREFIX+".dispRelations", ""));
        ((WebBookVisualPanel2) getComponent()).setPref07(gedcomSettings.get(WB_PREFIX+".dispNotes", ""));
        ((WebBookVisualPanel2) getComponent()).setPref08(gedcomSettings.get(WB_PREFIX+".dispId", ""));
        ((WebBookVisualPanel2) getComponent()).setPref09(gedcomSettings.get(WB_PREFIX+".dispEmailButton", ""));
    }

    public void storeSettings(Object settings) {
        if (gedcom == null) {
            return;
        }
        Registry gedcomSettings = gedcom.getRegistry();

        gedcomSettings.put(WB_PREFIX+".decujus", ((WebBookVisualPanel2) getComponent()).getPref01());
        gedcomSettings.put(WB_PREFIX+".unknown", ((WebBookVisualPanel2) getComponent()).getPref02());
        gedcomSettings.put(WB_PREFIX+".dispSpouse", ((WebBookVisualPanel2) getComponent()).getPref03());
        gedcomSettings.put(WB_PREFIX+".dispKids", ((WebBookVisualPanel2) getComponent()).getPref04());
        gedcomSettings.put(WB_PREFIX+".dispSiblings", ((WebBookVisualPanel2) getComponent()).getPref05());
        gedcomSettings.put(WB_PREFIX+".dispRelations", ((WebBookVisualPanel2) getComponent()).getPref06());
        gedcomSettings.put(WB_PREFIX+".dispNotes", ((WebBookVisualPanel2) getComponent()).getPref07());
        gedcomSettings.put(WB_PREFIX+".dispId", ((WebBookVisualPanel2) getComponent()).getPref08());
        gedcomSettings.put(WB_PREFIX+".dispEmailButton", ((WebBookVisualPanel2) getComponent()).getPref09());
}

    /*
     * Allow the finish button for this panel
     */
    public boolean isFinishPanel() {
        return false;
    }

    public void validate() throws WizardValidationException {
        String name = component.getPref01();
        if (name.trim().isEmpty()) {
            throw new WizardValidationException(null, NbBundle.getMessage(WebBookWizardAction.class, "CTRL_Mandatory_Decujus"), null);
        }
        name = component.getPref02();
        if (name.trim().isEmpty()) {
            throw new WizardValidationException(null, NbBundle.getMessage(WebBookWizardAction.class, "CTRL_Mandatory_Unknown"), null);
        }
    }
}

