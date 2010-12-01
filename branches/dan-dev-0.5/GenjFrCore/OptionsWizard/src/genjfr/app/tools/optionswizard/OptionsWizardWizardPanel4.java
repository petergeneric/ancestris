/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.optionswizard;

import ancestris.util.AncestrisPreferences;
import genjfr.app.App;
import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;

public class OptionsWizardWizardPanel4 implements WizardDescriptor.Panel {
    private final AncestrisPreferences gedcomPrefs = AncestrisPreferences.get(genj.gedcom.Options.class);
    private final AncestrisPreferences appPrefs = AncestrisPreferences.get(genj.app.Options.class);
    private final AncestrisPreferences reportPrefs = AncestrisPreferences.get(genj.report.Options.class);
    private final AncestrisPreferences editPrefs = AncestrisPreferences.get(genj.edit.Options.class);

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
//        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        return new HelpCtx(OptionsWizardWizardPanel4.class);
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
        ((OptionsWizardVisualPanel4) getComponent()).setGedcomFile(gedcomPrefs.get("gedcomFile", ""));
        ((OptionsWizardVisualPanel4) getComponent()).setReportDir(gedcomPrefs.get("reportDir", ""));
        ((OptionsWizardVisualPanel4) getComponent()).setAssoTxt("");
        ((OptionsWizardVisualPanel4) getComponent()).setAssoOffice("");
        ((OptionsWizardVisualPanel4) getComponent()).setAssoAdobe("");
        ((OptionsWizardVisualPanel4) getComponent()).setAssoImages("");
        ((OptionsWizardVisualPanel4) getComponent()).setAssoSound("");
        ((OptionsWizardVisualPanel4) getComponent()).setAssoWeb("");
        ((OptionsWizardVisualPanel4) getComponent()).setLogSize(appPrefs.get("maxLogSizeKB", ""));
    }

    public void storeSettings(Object settings) {
        gedcomPrefs.put("gedcomFile", ((OptionsWizardVisualPanel4) getComponent()).getGedcomFile());
        gedcomPrefs.put("reportDir", ((OptionsWizardVisualPanel4) getComponent()).getReportDir());
//        NbPreferences.forModule(App.class).put("assoTxt", getAssoTxt());
//        NbPreferences.forModule(App.class).put("assoOffice", getAssoOffice());
//        NbPreferences.forModule(App.class).put("assoAdobe", getAssoAdobe());
//        NbPreferences.forModule(App.class).put("assoImages", getAssoImages());
//        NbPreferences.forModule(App.class).put("assoSound", getAssoSound());
//        NbPreferences.forModule(App.class).put("assoWeb", getAssoWeb());
        appPrefs.put("maxLogSizeKB", ((OptionsWizardVisualPanel4) getComponent()).getLogSize());
    }
}




