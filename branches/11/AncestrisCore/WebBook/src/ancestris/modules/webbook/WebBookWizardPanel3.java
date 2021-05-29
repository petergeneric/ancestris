/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.webbook;

import ancestris.core.pluginservice.PluginInterface;
import genj.gedcom.Gedcom;
import genj.util.Registry;
import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class WebBookWizardPanel3 implements WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {

    // Gedcom is used to load and store settings for the webbook as "one set of settings per gedcom"
    private Gedcom gedcom;
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private WebBookVisualPanel3 component;
    private boolean isGeoFound = false;

    /**
     * Constructor
     * @param gedcom
     */
    WebBookWizardPanel3(Gedcom gedcom) {
        this.gedcom = gedcom;
        PluginInterface pi = null;
        for (PluginInterface sInterface : Lookup.getDefault().lookupAll(PluginInterface.class)) {
            if ("ancestris.modules.geo".equals(sInterface.getPluginName())) {
                // found module geo
                isGeoFound = true;
                break;
            }
        }
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new WebBookVisualPanel3();
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("ancestris.app.tools.webbook.step3");
    }

    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }
   

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(Object settings) {
        if (gedcom == null) {
            return;
        }
        Registry gedcomSettings = gedcom.getRegistry();

        ((WebBookVisualPanel3) getComponent()).setPref01(gedcomSettings.get(WebBookParams.WB_PREFIX + ".media_GeneSources", " "));
        ((WebBookVisualPanel3) getComponent()).setPref02(gedcomSettings.get(WebBookParams.WB_PREFIX + ".media_DisplaySources", " "));
        ((WebBookVisualPanel3) getComponent()).setPref03(gedcomSettings.get(WebBookParams.WB_PREFIX + ".media_CopySources", " "));
        ((WebBookVisualPanel3) getComponent()).setPref04(gedcomSettings.get(WebBookParams.WB_PREFIX + ".media_GeneMedia", " "));
        ((WebBookVisualPanel3) getComponent()).setPref05(gedcomSettings.get(WebBookParams.WB_PREFIX + ".media_CopyMedia", " "));
        if (isGeoFound) {
            ((WebBookVisualPanel3) getComponent()).setPref06(gedcomSettings.get(WebBookParams.WB_PREFIX + ".media_GeneMap", " "));
            ((WebBookVisualPanel3) getComponent()).setPref07(gedcomSettings.get(WebBookParams.WB_PREFIX + ".media_DispUnknownLoc", " "));
        } else {
            ((WebBookVisualPanel3) getComponent()).disablePref06();
        }
        component.setComponents();
    }

    @Override
    public void storeSettings(Object settings) {
        if (gedcom == null) {
            return;
        }
        Registry gedcomSettings = gedcom.getRegistry();

        gedcomSettings.put(WebBookParams.WB_PREFIX + ".media_GeneSources", ((WebBookVisualPanel3) getComponent()).getPref01());
        gedcomSettings.put(WebBookParams.WB_PREFIX + ".media_DisplaySources", ((WebBookVisualPanel3) getComponent()).getPref02());
        gedcomSettings.put(WebBookParams.WB_PREFIX + ".media_CopySources", ((WebBookVisualPanel3) getComponent()).getPref03());
        gedcomSettings.put(WebBookParams.WB_PREFIX + ".media_GeneMedia", ((WebBookVisualPanel3) getComponent()).getPref04());
        gedcomSettings.put(WebBookParams.WB_PREFIX + ".media_CopyMedia", ((WebBookVisualPanel3) getComponent()).getPref05());
        gedcomSettings.put(WebBookParams.WB_PREFIX + ".media_GeneMap", ((WebBookVisualPanel3) getComponent()).getPref06());
        gedcomSettings.put(WebBookParams.WB_PREFIX + ".media_DispUnknownLoc", ((WebBookVisualPanel3) getComponent()).getPref07());
    }

    /*
     * Allow the finish button for this panel
     */
    @Override
    public boolean isFinishPanel() {
        return false;
    }

    @Override
    public void validate() throws WizardValidationException {
        String geoSelected = component.getPref06();
        if (geoSelected.equals("1") && !isGeoFound) {
            throw new WizardValidationException(null, NbBundle.getMessage(WebBookWizardAction.class, "CTRL_GeoModuleNotFound"), null);
        }

    }
}
