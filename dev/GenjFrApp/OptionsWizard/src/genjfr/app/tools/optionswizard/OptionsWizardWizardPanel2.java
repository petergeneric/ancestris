/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.optionswizard;

import genj.app.App;
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
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolBirt(NbPreferences.forModule(App.class).get("symbolBirth", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolBapm(NbPreferences.forModule(App.class).get("symbolBapm", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolChildOf(NbPreferences.forModule(App.class).get("symbolChildOf", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolEngm(NbPreferences.forModule(App.class).get("symbolEngm", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolMarr(NbPreferences.forModule(App.class).get("symbolMarr", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolDivc(NbPreferences.forModule(App.class).get("symbolDivc", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolOccu(NbPreferences.forModule(App.class).get("symbolOccu", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolResi(NbPreferences.forModule(App.class).get("symbolResi", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolDeat(NbPreferences.forModule(App.class).get("symbolDeat", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setSymbolBuri(NbPreferences.forModule(App.class).get("symbolBuri", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setPrivDisplay(NbPreferences.forModule(App.class).get("privDisplay", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setPrivFlag(NbPreferences.forModule(App.class).get("privFlag", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setPrivAlive(NbPreferences.forModule(App.class).get("privAlive", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setPrivYears(NbPreferences.forModule(App.class).get("privYears", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setLineBreak(NbPreferences.forModule(App.class).get("txtLineBreak", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setImageSize(NbPreferences.forModule(App.class).get("imageSize", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setDisplayNames(NbPreferences.forModule(App.class).get("displayNames", ""));
     ((OptionsWizardVisualPanel2) getComponent()).setDisplayDates(NbPreferences.forModule(App.class).get("displayDates", ""));

    }

    public void storeSettings(Object settings) {
     NbPreferences.forModule(App.class).put("symbolBirth", ((OptionsWizardVisualPanel2) getComponent()).getSymbolBirt());
     NbPreferences.forModule(App.class).put("symbolBapm", ((OptionsWizardVisualPanel2) getComponent()).getSymbolBapm());
     NbPreferences.forModule(App.class).put("symbolChildOf", ((OptionsWizardVisualPanel2) getComponent()).getSymbolChildOf());
     NbPreferences.forModule(App.class).put("symbolEngm", ((OptionsWizardVisualPanel2) getComponent()).getSymbolEngm());
     NbPreferences.forModule(App.class).put("symbolMarr", ((OptionsWizardVisualPanel2) getComponent()).getSymbolMarr());
     NbPreferences.forModule(App.class).put("symbolDivc", ((OptionsWizardVisualPanel2) getComponent()).getSymbolDivc());
     NbPreferences.forModule(App.class).put("symbolOccu", ((OptionsWizardVisualPanel2) getComponent()).getSymbolOccu());
     NbPreferences.forModule(App.class).put("symbolResi", ((OptionsWizardVisualPanel2) getComponent()).getSymbolResi());
     NbPreferences.forModule(App.class).put("symbolDeat", ((OptionsWizardVisualPanel2) getComponent()).getSymbolDeat());
     NbPreferences.forModule(App.class).put("symbolBuri", ((OptionsWizardVisualPanel2) getComponent()).getSymbolBuri());
     NbPreferences.forModule(App.class).put("privDisplay", ((OptionsWizardVisualPanel2) getComponent()).getPrivDisplay());
     NbPreferences.forModule(App.class).put("privFlag", ((OptionsWizardVisualPanel2) getComponent()).getPrivFlag());
     NbPreferences.forModule(App.class).put("privAlive", ((OptionsWizardVisualPanel2) getComponent()).getPrivAlive());
     NbPreferences.forModule(App.class).put("privYears", ((OptionsWizardVisualPanel2) getComponent()).getPrivYears());
     NbPreferences.forModule(App.class).put("txtLineBreak", ((OptionsWizardVisualPanel2) getComponent()).getLineBreak());
     NbPreferences.forModule(App.class).put("imageSize", ((OptionsWizardVisualPanel2) getComponent()).getImageSize());
     NbPreferences.forModule(App.class).put("displayNames", ((OptionsWizardVisualPanel2) getComponent()).getDisplayNames());
     NbPreferences.forModule(App.class).put("displayDates", ((OptionsWizardVisualPanel2) getComponent()).getDisplayDates());

    }
}

