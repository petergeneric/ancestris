/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.optionswizard;

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;

public class OptionsWizardWizardPanel1 implements WizardDescriptor.Panel {

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
            component = new OptionsWizardVisualPanel1();
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
        ((OptionsWizardVisualPanel1) getComponent()).setLanguage(NbPreferences.forModule(OptionsWizardWizardPanel1.class).get("language", ""));
        ((OptionsWizardVisualPanel1) getComponent()).setSkin(NbPreferences.forModule(OptionsWizardWizardPanel1.class).get("skin", ""));
        ((OptionsWizardVisualPanel1) getComponent()).setRestoreWindows(NbPreferences.forModule(OptionsWizardWizardPanel1.class).get("restoreWindows", ""));
        ((OptionsWizardVisualPanel1) getComponent()).setAutoCommit(NbPreferences.forModule(OptionsWizardWizardPanel1.class).get("autoCommit", ""));
        ((OptionsWizardVisualPanel1) getComponent()).setUndos(NbPreferences.forModule(OptionsWizardWizardPanel1.class).get("undos", ""));
        ((OptionsWizardVisualPanel1) getComponent()).setSplitJurisdictions(NbPreferences.forModule(OptionsWizardWizardPanel1.class).get("splitJurisdiction", ""));
        ((OptionsWizardVisualPanel1) getComponent()).setOpenEditor(NbPreferences.forModule(OptionsWizardWizardPanel1.class).get("OpenEditor", ""));

    }

    public void storeSettings(Object settings) {
        NbPreferences.forModule(OptionsWizardWizardPanel1.class).put("language", ((OptionsWizardVisualPanel1) getComponent()).getLanguage());
        NbPreferences.forModule(OptionsWizardWizardPanel1.class).put("skin", ((OptionsWizardVisualPanel1) getComponent()).getSkin());
        NbPreferences.forModule(OptionsWizardWizardPanel1.class).put("restoreWindows", ((OptionsWizardVisualPanel1) getComponent()).getRestoreWindows());
        NbPreferences.forModule(OptionsWizardWizardPanel1.class).put("autoCommit", ((OptionsWizardVisualPanel1) getComponent()).getAutoCommit());
        NbPreferences.forModule(OptionsWizardWizardPanel1.class).put("undos", ((OptionsWizardVisualPanel1) getComponent()).getUndos());
        NbPreferences.forModule(OptionsWizardWizardPanel1.class).put("splitJurisdiction", ((OptionsWizardVisualPanel1) getComponent()).getSplitJurisdictions());
        NbPreferences.forModule(OptionsWizardWizardPanel1.class).put("OpenEditor", ((OptionsWizardVisualPanel1) getComponent()).getOpenEditor());

    }
}

