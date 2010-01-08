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

public class OptionsWizardWizardPanel2 implements WizardDescriptor.Panel {

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
            component = new OptionsWizardVisualPanel2();
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
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolBirt(NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolBirth", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolBapm(NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolBapm", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolChildOf(NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolChildOf", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolEngm(NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolEngm", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolMarr(NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolMarr", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolDivc(NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolDivc", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolOccu(NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolOccu", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolResi(NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolResi", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolDeat(NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolDeat", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolBuri(NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolBuri", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setPrivDisplay(NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("privDisplay", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setPrivFlag(NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("privFlag", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setPrivDeceased(NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("privDeceased", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setPrivYears(NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("privYears", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setLineBreak(NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("txtLineBreak", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setImageSize(NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("imageSize", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setDisplayNames(NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("displayNames", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setDisplayDates(NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("displayDates", ""));

    }

    public void storeSettings(Object settings) {
     NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolBirth", ((OptionsWizardVisualPanel2) getComponent()).getSymbolBirt());
     NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolBapm", ((OptionsWizardVisualPanel2) getComponent()).getSymbolBapm());
     NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolChildOf", ((OptionsWizardVisualPanel2) getComponent()).getSymbolChildOf());
     NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolEngm", ((OptionsWizardVisualPanel2) getComponent()).getSymbolEngm());
     NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolMarr", ((OptionsWizardVisualPanel2) getComponent()).getSymbolMarr());
     NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolDivc", ((OptionsWizardVisualPanel2) getComponent()).getSymbolDivc());
     NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolOccu", ((OptionsWizardVisualPanel2) getComponent()).getSymbolOccu());
     NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolResi", ((OptionsWizardVisualPanel2) getComponent()).getSymbolResi());
     NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolDeat", ((OptionsWizardVisualPanel2) getComponent()).getSymbolDeat());
     NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolBuri", ((OptionsWizardVisualPanel2) getComponent()).getSymbolBuri());
     NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("privDisplay", ((OptionsWizardVisualPanel2) getComponent()).getPrivDisplay());
     NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("privFlag", ((OptionsWizardVisualPanel2) getComponent()).getPrivFlag());
     NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("privDeceased", ((OptionsWizardVisualPanel2) getComponent()).getPrivDeceased());
     NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("privYears", ((OptionsWizardVisualPanel2) getComponent()).getPrivYears());
     NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("txtLineBreak", ((OptionsWizardVisualPanel2) getComponent()).getLineBreak());
     NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("imageSize", ((OptionsWizardVisualPanel2) getComponent()).getImageSize());
     NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("displayNames", ((OptionsWizardVisualPanel2) getComponent()).getDisplayNames());
     NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("displayDates", ((OptionsWizardVisualPanel2) getComponent()).getDisplayDates());

    }
}

