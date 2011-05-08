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
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
//import org.openide.util.NbPreferences;

public class OptionsWizardWizardPanel3 implements WizardDescriptor.ValidatingPanel {
    private final AncestrisPreferences gedcomPrefs = AncestrisPreferences.get(genj.gedcom.Options.class);
    private final AncestrisPreferences appPrefs = AncestrisPreferences.get(genj.app.Options.class);
    private final AncestrisPreferences reportPrefs = AncestrisPreferences.get(genj.report.Options.class);
    private final AncestrisPreferences editPrefs = AncestrisPreferences.get(genj.edit.Options.class);


    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private OptionsWizardVisualPanel3 component;

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
        return new HelpCtx(OptionsWizardWizardPanel3.class);
    }

    public boolean isValid() {
        return true;
    }

    public void validate() throws WizardValidationException {
        if (!component.valid()) {
            throw new WizardValidationException(null, NbBundle.getMessage(OptionsWizardWizardPanel3.class, "ERR_InvalidLevels"), null);
        }
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
        ((OptionsWizardVisualPanel3) getComponent()).setSubmName(gedcomPrefs.get("submName", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setSubmCity(gedcomPrefs.get("submCity", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setSubmPhone(gedcomPrefs.get("submPhone", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setSubmPostCode(gedcomPrefs.get("submPostCode", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setSubmEmail(gedcomPrefs.get("submEmail", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setSubmCountry(gedcomPrefs.get("submCountry", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setSubmWeb(gedcomPrefs.get("submWeb", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setNamesUppercase(gedcomPrefs.get("isUpperCaseNames", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setNamesSpouse(gedcomPrefs.get("setWifeLastname", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setAddress1(gedcomPrefs.get("fmt_address1", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setAddress2(gedcomPrefs.get("fmt_address2", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setAddress3(gedcomPrefs.get("fmt_address3", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setAddress4(gedcomPrefs.get("fmt_address4", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setAddress5(gedcomPrefs.get("fmt_address5", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setAddress6(gedcomPrefs.get("fmt_address6", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setAddress7(gedcomPrefs.get("fmt_address7", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setAddress1Mand(gedcomPrefs.get("fmt_address1_mand", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setAddress2Mand(gedcomPrefs.get("fmt_address2_mand", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setAddress3Mand(gedcomPrefs.get("fmt_address3_mand", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setAddress4Mand(gedcomPrefs.get("fmt_address4_mand", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setAddress5Mand(gedcomPrefs.get("fmt_address5_mand", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setAddress6Mand(gedcomPrefs.get("fmt_address6_mand", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setAddress7Mand(gedcomPrefs.get("fmt_address7_mand", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setAddressSpaces(gedcomPrefs.get("isUseSpacedPlaces", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setIDFilling(gedcomPrefs.get("isFillGapsInIDs", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setEncoding(gedcomPrefs.get("defaultEncoding", ""));
        ((OptionsWizardVisualPanel3) getComponent()).setBOM(appPrefs.get("isWriteBOM", ""));

    }

    public void storeSettings(Object settings) {
        gedcomPrefs.put("submName", ((OptionsWizardVisualPanel3) getComponent()).getSubmName());
        gedcomPrefs.put("submCity", ((OptionsWizardVisualPanel3) getComponent()).getSubmCity());
        gedcomPrefs.put("submPhone", ((OptionsWizardVisualPanel3) getComponent()).getSubmPhone());
        gedcomPrefs.put("submPostCode", ((OptionsWizardVisualPanel3) getComponent()).getSubmPostCode());
        gedcomPrefs.put("submEmail", ((OptionsWizardVisualPanel3) getComponent()).getSubmEmail());
        gedcomPrefs.put("submCountry", ((OptionsWizardVisualPanel3) getComponent()).getSubmCountry());
        gedcomPrefs.put("submWeb", ((OptionsWizardVisualPanel3) getComponent()).getSubmWeb());
        gedcomPrefs.put("isUpperCaseNames", ((OptionsWizardVisualPanel3) getComponent()).getNamesUppercase());
        gedcomPrefs.put("setWifeLastname", ((OptionsWizardVisualPanel3) getComponent()).getNamesSpouse());
        gedcomPrefs.put("fmt_address1", ((OptionsWizardVisualPanel3) getComponent()).getAddress1());
        gedcomPrefs.put("fmt_address2", ((OptionsWizardVisualPanel3) getComponent()).getAddress2());
        gedcomPrefs.put("fmt_address3", ((OptionsWizardVisualPanel3) getComponent()).getAddress3());
        gedcomPrefs.put("fmt_address4", ((OptionsWizardVisualPanel3) getComponent()).getAddress4());
        gedcomPrefs.put("fmt_address5", ((OptionsWizardVisualPanel3) getComponent()).getAddress5());
        gedcomPrefs.put("fmt_address6", ((OptionsWizardVisualPanel3) getComponent()).getAddress6());
        gedcomPrefs.put("fmt_address7", ((OptionsWizardVisualPanel3) getComponent()).getAddress7());
        gedcomPrefs.put("fmt_address1_mand", ((OptionsWizardVisualPanel3) getComponent()).getAddress1Mand());
        gedcomPrefs.put("fmt_address2_mand", ((OptionsWizardVisualPanel3) getComponent()).getAddress2Mand());
        gedcomPrefs.put("fmt_address3_mand", ((OptionsWizardVisualPanel3) getComponent()).getAddress3Mand());
        gedcomPrefs.put("fmt_address4_mand", ((OptionsWizardVisualPanel3) getComponent()).getAddress4Mand());
        gedcomPrefs.put("fmt_address5_mand", ((OptionsWizardVisualPanel3) getComponent()).getAddress5Mand());
        gedcomPrefs.put("fmt_address6_mand", ((OptionsWizardVisualPanel3) getComponent()).getAddress6Mand());
        gedcomPrefs.put("fmt_address7_mand", ((OptionsWizardVisualPanel3) getComponent()).getAddress7Mand());
        gedcomPrefs.put("isUseSpacedPlaces", ((OptionsWizardVisualPanel3) getComponent()).getAddressSpaces());
        gedcomPrefs.put("isFillGapsInIDs", ((OptionsWizardVisualPanel3) getComponent()).getIdFilling());
        gedcomPrefs.put("defaultEncoding", ((OptionsWizardVisualPanel3) getComponent()).getEncoding());
        appPrefs.put("isWriteBOM", ((OptionsWizardVisualPanel3) getComponent()).getBOM());
    }

}

