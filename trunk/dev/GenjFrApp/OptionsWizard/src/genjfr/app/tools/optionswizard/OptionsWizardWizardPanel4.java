/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.optionswizard;

import genjfr.app.App;
import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;

public class OptionsWizardWizardPanel4 implements WizardDescriptor.Panel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new OptionsWizardVisualPanel4();
        }
        return component;
    }

    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
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
     ((OptionsWizardVisualPanel4) getComponent()).setGedcomFile(NbPreferences.forModule(App.class).get("gedcomFile", ""));
     ((OptionsWizardVisualPanel4) getComponent()).setReportDir(NbPreferences.forModule(App.class).get("reportDir", ""));
     ((OptionsWizardVisualPanel4) getComponent()).setAssoTxt(NbPreferences.forModule(App.class).get("assoTxt", ""));
     ((OptionsWizardVisualPanel4) getComponent()).setAssoOffice(NbPreferences.forModule(App.class).get("assoOffice", ""));
     ((OptionsWizardVisualPanel4) getComponent()).setAssoAdobe(NbPreferences.forModule(App.class).get("assoAdobe", ""));
     ((OptionsWizardVisualPanel4) getComponent()).setAssoImages(NbPreferences.forModule(App.class).get("assoImages", ""));
     ((OptionsWizardVisualPanel4) getComponent()).setAssoSound(NbPreferences.forModule(App.class).get("assoSound", ""));
     ((OptionsWizardVisualPanel4) getComponent()).setAssoWeb(NbPreferences.forModule(App.class).get("assoWeb", ""));
     ((OptionsWizardVisualPanel4) getComponent()).setLogSize(NbPreferences.forModule(App.class).get("logSize", ""));

    }

    public void storeSettings(Object settings) {
     NbPreferences.forModule(App.class).put("gedcomFile", ((OptionsWizardVisualPanel4) getComponent()).getGedcomFile());
     NbPreferences.forModule(App.class).put("reportDir", ((OptionsWizardVisualPanel4) getComponent()).getReportDir());
     NbPreferences.forModule(App.class).put("assoTxt", ((OptionsWizardVisualPanel4) getComponent()).getAssoTxt());
     NbPreferences.forModule(App.class).put("assoOffice", ((OptionsWizardVisualPanel4) getComponent()).getAssoOffice());
     NbPreferences.forModule(App.class).put("assoAdobe", ((OptionsWizardVisualPanel4) getComponent()).getAssoAdobe());
     NbPreferences.forModule(App.class).put("assoImages", ((OptionsWizardVisualPanel4) getComponent()).getAssoImages());
     NbPreferences.forModule(App.class).put("assoSound", ((OptionsWizardVisualPanel4) getComponent()).getAssoSound());
     NbPreferences.forModule(App.class).put("assoWeb", ((OptionsWizardVisualPanel4) getComponent()).getAssoWeb());
     NbPreferences.forModule(App.class).put("logSize", ((OptionsWizardVisualPanel4) getComponent()).getLogSize());
    }
}




