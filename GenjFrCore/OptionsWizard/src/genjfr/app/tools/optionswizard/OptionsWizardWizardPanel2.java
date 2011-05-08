/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.optionswizard;

import ancestris.util.AncestrisPreferences;
import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
//import org.openide.util.NbPreferences;

public class OptionsWizardWizardPanel2 implements WizardDescriptor.Panel {
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
            component = new OptionsWizardVisualPanel2();
        }
        return component;
    }

    public HelpCtx getHelp() {
        // Show no Help button for this panel:
//        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        return new HelpCtx(OptionsWizardWizardPanel2.class);
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
        ((OptionsWizardVisualPanel2) getComponent()).setSymbolBirt(reportPrefs.get("birthSymbol", ""));
        ((OptionsWizardVisualPanel2) getComponent()).setSymbolBapm(reportPrefs.get("baptismSymbol", ""));
        ((OptionsWizardVisualPanel2) getComponent()).setSymbolChildOf(reportPrefs.get("childOfSymbol", ""));
        ((OptionsWizardVisualPanel2) getComponent()).setSymbolEngm(reportPrefs.get("engagingSymbol", ""));
        ((OptionsWizardVisualPanel2) getComponent()).setSymbolMarr(reportPrefs.get("marriageSymbol", ""));
        ((OptionsWizardVisualPanel2) getComponent()).setSymbolDivc(reportPrefs.get("divorceSymbol", ""));
        ((OptionsWizardVisualPanel2) getComponent()).setSymbolOccu(reportPrefs.get("occuSymbol", ""));
        ((OptionsWizardVisualPanel2) getComponent()).setSymbolResi(reportPrefs.get("resiSymbol", ""));
        ((OptionsWizardVisualPanel2) getComponent()).setSymbolDeat(reportPrefs.get("deathSymbol", ""));
        ((OptionsWizardVisualPanel2) getComponent()).setSymbolBuri(reportPrefs.get("burialSymbol", ""));
        ((OptionsWizardVisualPanel2) getComponent()).setPrivDisplay(gedcomPrefs.get("maskPrivate", ""));
        ((OptionsWizardVisualPanel2) getComponent()).setPrivFlag(reportPrefs.get("privateTag", ""));
        ((OptionsWizardVisualPanel2) getComponent()).setPrivAlive(reportPrefs.get("deceasedIsPublic", ""));
        ((OptionsWizardVisualPanel2) getComponent()).setPrivYears(reportPrefs.get("yearsEventsArePrivate", ""));
        ((OptionsWizardVisualPanel2) getComponent()).setLineBreak(gedcomPrefs.get("valueLineBreak", ""));
        ((OptionsWizardVisualPanel2) getComponent()).setImageSize(gedcomPrefs.get("maxImageFileSizeKB", ""));
        ((OptionsWizardVisualPanel2) getComponent()).setDisplayNames(gedcomPrefs.get("nameFormat", ""));
        ((OptionsWizardVisualPanel2) getComponent()).setDisplayDates(gedcomPrefs.get("dateFormat", ""));
    }

    public void storeSettings(Object settings) {
        reportPrefs.put("birthSymbol", ((OptionsWizardVisualPanel2) getComponent()).getSymbolBirt());
        reportPrefs.put("baptismSymbol", ((OptionsWizardVisualPanel2) getComponent()).getSymbolBapm());
        reportPrefs.put("childOfSymbol", ((OptionsWizardVisualPanel2) getComponent()).getSymbolChildOf());
        reportPrefs.put("engagingSymbol", ((OptionsWizardVisualPanel2) getComponent()).getSymbolEngm());
        reportPrefs.put("marriageSymbol", ((OptionsWizardVisualPanel2) getComponent()).getSymbolMarr());
        reportPrefs.put("divorceSymbol", ((OptionsWizardVisualPanel2) getComponent()).getSymbolDivc());
        reportPrefs.put("occuSymbol", ((OptionsWizardVisualPanel2) getComponent()).getSymbolOccu());
        reportPrefs.put("resiSymbol", ((OptionsWizardVisualPanel2) getComponent()).getSymbolResi());
        reportPrefs.put("deathSymbol", ((OptionsWizardVisualPanel2) getComponent()).getSymbolDeat());
        reportPrefs.put("burialSymbol", ((OptionsWizardVisualPanel2) getComponent()).getSymbolBuri());
        gedcomPrefs.put("maskPrivate", ((OptionsWizardVisualPanel2) getComponent()).getPrivDisplay());
        reportPrefs.put("privateTag", ((OptionsWizardVisualPanel2) getComponent()).getPrivFlag());
        reportPrefs.put("deceasedIsPublic", ((OptionsWizardVisualPanel2) getComponent()).getPrivAlive());
        reportPrefs.put("yearsEventsArePrivate", ((OptionsWizardVisualPanel2) getComponent()).getPrivYears());
        gedcomPrefs.put("valueLineBreak", ((OptionsWizardVisualPanel2) getComponent()).getLineBreak());
        gedcomPrefs.put("maxImageFileSizeKB", ((OptionsWizardVisualPanel2) getComponent()).getImageSize());
        gedcomPrefs.put("nameFormat", ((OptionsWizardVisualPanel2) getComponent()).getDisplayNames());
        gedcomPrefs.put("dateFormat", ((OptionsWizardVisualPanel2) getComponent()).getDisplayDates());
    }
}

