/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.webbook;

import genj.gedcom.Gedcom;
import java.awt.Component;
import java.io.File;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public class WebBookWizardPanel7 implements WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {

    // Gedcom is used to load and store settings for the webbook as "one set of settings per gedcom"
    private Gedcom gedcom = WebBookWizardAction.getGedcom();
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private WebBookVisualPanel7 component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new WebBookVisualPanel7();
        }
        return component;
    }

    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        //return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        return new HelpCtx("ancestris.app.tools.webbook.step7");
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
        String gedName = gedcom.getName();
        ((WebBookVisualPanel7) getComponent()).setPref01(NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_Support", ""));
        ((WebBookVisualPanel7) getComponent()).setPref02(NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_Test", ""));
        ((WebBookVisualPanel7) getComponent()).setPref03(NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_Integrate", ""));
        ((WebBookVisualPanel7) getComponent()).setPref04(NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_Init", ""));
        ((WebBookVisualPanel7) getComponent()).setPref05(NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_MyScript", ""));
        ((WebBookVisualPanel7) getComponent()).setPref06(NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_HeadStart", ""));
        ((WebBookVisualPanel7) getComponent()).setPref07(NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_HeadCSS", ""));
        ((WebBookVisualPanel7) getComponent()).setPref08(NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_HeadEnd", ""));
        ((WebBookVisualPanel7) getComponent()).setPref09(NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_HeadBody", ""));
        ((WebBookVisualPanel7) getComponent()).setPref10(NbPreferences.forModule(WebBookWizardPanel7.class).get(gedName + ".PHP_Footer", ""));
        component.setComponents();
    }

    public void storeSettings(Object settings) {
        if (gedcom == null) {
            return;
        }
        String gedName = gedcom.getName();
        NbPreferences.forModule(WebBookWizardPanel7.class).put(gedName + ".PHP_Support", ((WebBookVisualPanel7) getComponent()).getPref01());
        NbPreferences.forModule(WebBookWizardPanel7.class).put(gedName + ".PHP_Test", ((WebBookVisualPanel7) getComponent()).getPref02());
        NbPreferences.forModule(WebBookWizardPanel7.class).put(gedName + ".PHP_Integrate", ((WebBookVisualPanel7) getComponent()).getPref03());
        NbPreferences.forModule(WebBookWizardPanel7.class).put(gedName + ".PHP_Init", ((WebBookVisualPanel7) getComponent()).getPref04());
        NbPreferences.forModule(WebBookWizardPanel7.class).put(gedName + ".PHP_MyScript", ((WebBookVisualPanel7) getComponent()).getPref05());
        NbPreferences.forModule(WebBookWizardPanel7.class).put(gedName + ".PHP_HeadStart", ((WebBookVisualPanel7) getComponent()).getPref06());
        NbPreferences.forModule(WebBookWizardPanel7.class).put(gedName + ".PHP_HeadCSS", ((WebBookVisualPanel7) getComponent()).getPref07());
        NbPreferences.forModule(WebBookWizardPanel7.class).put(gedName + ".PHP_HeadEnd", ((WebBookVisualPanel7) getComponent()).getPref08());
        NbPreferences.forModule(WebBookWizardPanel7.class).put(gedName + ".PHP_HeadBody", ((WebBookVisualPanel7) getComponent()).getPref09());
        NbPreferences.forModule(WebBookWizardPanel7.class).put(gedName + ".PHP_Footer", ((WebBookVisualPanel7) getComponent()).getPref10());
    }

    /*
     * Allow the finish button for this panel
     */
    public boolean isFinishPanel() {
        return true;
    }

    public void validate() throws WizardValidationException {
        if (component.getPref01().equals("0") && component.getPref03().equals("1")) {
            throw new WizardValidationException(null, NbBundle.getMessage(WebBookWizardAction.class, "CTRL_CannotIntegrate"), null);
        }
    }
}

