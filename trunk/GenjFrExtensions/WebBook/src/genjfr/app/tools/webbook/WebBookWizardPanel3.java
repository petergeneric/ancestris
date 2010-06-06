/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.webbook;

import genj.gedcom.Gedcom;
import genjfr.app.pluginservice.PluginInterface;
import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public class WebBookWizardPanel3 implements WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {

    // Gedcom is used to load and store settings for the webbook as "one set of settings per gedcom"
    private Gedcom gedcom = WebBookWizardAction.getGedcom();

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private WebBookVisualPanel3 component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new WebBookVisualPanel3();
        }
        return component;
    }

    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        //return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        return new HelpCtx("ancestris.app.tools.webbook.step3");
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
        ((WebBookVisualPanel3) getComponent()).setPref01(NbPreferences.forModule(WebBookWizardPanel3.class).get(gedName+".media_GeneSources", ""));
        ((WebBookVisualPanel3) getComponent()).setPref02(NbPreferences.forModule(WebBookWizardPanel3.class).get(gedName+".media_DisplaySources", ""));
        ((WebBookVisualPanel3) getComponent()).setPref03(NbPreferences.forModule(WebBookWizardPanel3.class).get(gedName+".media_CopySources", ""));
        ((WebBookVisualPanel3) getComponent()).setPref04(NbPreferences.forModule(WebBookWizardPanel3.class).get(gedName+".media_GeneMedia", ""));
        ((WebBookVisualPanel3) getComponent()).setPref05(NbPreferences.forModule(WebBookWizardPanel3.class).get(gedName+".media_CopyMedia", ""));
        ((WebBookVisualPanel3) getComponent()).setPref06(NbPreferences.forModule(WebBookWizardPanel3.class).get(gedName+".media_GeneMap", ""));
        ((WebBookVisualPanel3) getComponent()).setPref07(NbPreferences.forModule(WebBookWizardPanel3.class).get(gedName+".media_DispUnknownLoc", ""));
        ((WebBookVisualPanel3) getComponent()).setPref08(NbPreferences.forModule(WebBookWizardPanel3.class).get(gedName+".media_GoogleKey", ""));
        component.setComponents();
    }

    public void storeSettings(Object settings) {
        if (gedcom == null) {
            return;
        }
        String gedName = gedcom.getName();
        NbPreferences.forModule(WebBookWizardPanel3.class).put(gedName+".media_GeneSources", ((WebBookVisualPanel3) getComponent()).getPref01());
        NbPreferences.forModule(WebBookWizardPanel3.class).put(gedName+".media_DisplaySources", ((WebBookVisualPanel3) getComponent()).getPref02());
        NbPreferences.forModule(WebBookWizardPanel3.class).put(gedName+".media_CopySources", ((WebBookVisualPanel3) getComponent()).getPref03());
        NbPreferences.forModule(WebBookWizardPanel3.class).put(gedName+".media_GeneMedia", ((WebBookVisualPanel3) getComponent()).getPref04());
        NbPreferences.forModule(WebBookWizardPanel3.class).put(gedName+".media_CopyMedia", ((WebBookVisualPanel3) getComponent()).getPref05());
        NbPreferences.forModule(WebBookWizardPanel3.class).put(gedName+".media_GeneMap", ((WebBookVisualPanel3) getComponent()).getPref06());
        NbPreferences.forModule(WebBookWizardPanel3.class).put(gedName+".media_DispUnknownLoc", ((WebBookVisualPanel3) getComponent()).getPref07());
        NbPreferences.forModule(WebBookWizardPanel3.class).put(gedName+".media_GoogleKey", ((WebBookVisualPanel3) getComponent()).getPref08());
    }

    /*
     * Allow the finish button for this panel
     */
    public boolean isFinishPanel() {
        return false;
    }

    public void validate() throws WizardValidationException {
        String name = component.getPref08();
        String geoSelected = component.getPref06();
        if (name.trim().isEmpty() && geoSelected.equals("1")) {
            throw new WizardValidationException(null, NbBundle.getMessage(WebBookWizardAction.class, "CTRL_Mandatory_GoogleKey"), null);
        }
        boolean isGeoFound=false;
        PluginInterface pi = null;
        for (PluginInterface sInterface : Lookup.getDefault().lookupAll(PluginInterface.class)) {
            if ("genjfr.app.geo".equals(sInterface.getPluginName())) {
                // found module geo
                isGeoFound = true;
                break;
            }
        }
        if (geoSelected.equals("1") && !isGeoFound) {
            throw new WizardValidationException(null, NbBundle.getMessage(WebBookWizardAction.class, "CTRL_GeoModuleNotFound"), null);
        }

    }
}

