/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.optionswizard;

import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;

public class OptionsWizardWizardPanel3 implements WizardDescriptor.Panel {

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
            component = new OptionsWizardVisualPanel3();
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
     ((OptionsWizardVisualPanel3) getComponent()).setSubmName(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("submName", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setSubmCity(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("submCity", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setSubmPhone(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("submPhone", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setSubmPostCode(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("submPostCode", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setSubmEmail(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("submEmail", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setSubmCountry(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("submCountry", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setSubmWeb(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("submWeb", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setNamesUppercase(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("NamesUppercase", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setNamesSpouse(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("NamesSpouse", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setAddress1(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address1", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setAddress2(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address2", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setAddress3(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address3", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setAddress4(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address4", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setAddress5(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address5", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setAddress6(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address6", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setAddress7(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address7", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setAddress1Mand(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address1_mand", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setAddress2Mand(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address2_mand", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setAddress3Mand(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address3_mand", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setAddress4Mand(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address4_mand", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setAddress5Mand(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address5_mand", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setAddress6Mand(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address6_mand", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setAddress7Mand(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address7_mand", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setAddressSpaces(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("address_splitspaces", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setIDFilling(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("IDFilling", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setEncoding(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("encoding", ""));
     ((OptionsWizardVisualPanel3) getComponent()).setBOM(NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("BOM", ""));

    }

    public void storeSettings(Object settings) {
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("submName", ((OptionsWizardVisualPanel3) getComponent()).getSubmName());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("submCity", ((OptionsWizardVisualPanel3) getComponent()).getSubmCity());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("submPhone", ((OptionsWizardVisualPanel3) getComponent()).getSubmPhone());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("submPostCode", ((OptionsWizardVisualPanel3) getComponent()).getSubmPostCode());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("submEmail", ((OptionsWizardVisualPanel3) getComponent()).getSubmEmail());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("submCountry", ((OptionsWizardVisualPanel3) getComponent()).getSubmCountry());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("submWeb", ((OptionsWizardVisualPanel3) getComponent()).getSubmWeb());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("NamesUppercase", ((OptionsWizardVisualPanel3) getComponent()).getNamesUppercase());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("NamesSpouse", ((OptionsWizardVisualPanel3) getComponent()).getNamesSpouse());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address1", ((OptionsWizardVisualPanel3) getComponent()).getAddress1());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address2", ((OptionsWizardVisualPanel3) getComponent()).getAddress2());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address3", ((OptionsWizardVisualPanel3) getComponent()).getAddress3());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address4", ((OptionsWizardVisualPanel3) getComponent()).getAddress4());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address5", ((OptionsWizardVisualPanel3) getComponent()).getAddress5());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address6", ((OptionsWizardVisualPanel3) getComponent()).getAddress6());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address7", ((OptionsWizardVisualPanel3) getComponent()).getAddress7());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address1_mand", ((OptionsWizardVisualPanel3) getComponent()).getAddress1Mand());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address2_mand", ((OptionsWizardVisualPanel3) getComponent()).getAddress2Mand());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address3_mand", ((OptionsWizardVisualPanel3) getComponent()).getAddress3Mand());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address4_mand", ((OptionsWizardVisualPanel3) getComponent()).getAddress4Mand());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address5_mand", ((OptionsWizardVisualPanel3) getComponent()).getAddress5Mand());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address6_mand", ((OptionsWizardVisualPanel3) getComponent()).getAddress6Mand());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address7_mand", ((OptionsWizardVisualPanel3) getComponent()).getAddress7Mand());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("address_splitspaces", ((OptionsWizardVisualPanel3) getComponent()).getAddressSpaces());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("IDFilling", ((OptionsWizardVisualPanel3) getComponent()).getIdFilling());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("encoding", ((OptionsWizardVisualPanel3) getComponent()).getEncoding());
     NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("BOM", ((OptionsWizardVisualPanel3) getComponent()).getBOM());

    }
}

