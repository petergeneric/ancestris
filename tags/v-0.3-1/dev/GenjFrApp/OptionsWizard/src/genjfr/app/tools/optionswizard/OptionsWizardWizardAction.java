/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.optionswizard;

import genj.util.Registry;
import genjfr.app.App;
import genjfr.app.pluginservice.PluginInterface;
import java.awt.Component;
import java.awt.Dialog;
import java.text.MessageFormat;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.CallableSystemAction;

// An example action demonstrating how the wizard could be called from within
// your code. You can copy-paste the code below wherever you need.
public final class OptionsWizardWizardAction extends CallableSystemAction implements PluginInterface {

    private static final String PREFERRED_PLUGIN_NAME = "OptionsWizard";
    private Registry registry;
    private WizardDescriptor.Panel[] panels;
    private boolean exitFlag = false;

    public String getPluginName() {
        return PREFERRED_PLUGIN_NAME;
    }

    public boolean launchModule(Object o) {

        System.out.println("=== DEBUT DU WIZARD ===");

        this.registry = (Registry) o;

        if (toBeLaunched()) {
            performAction();
        }

        if (exitFlag == true) {
            NbPreferences.forModule(App.class).put("optionswizard", "4"); // should be same as below
        }
        
        if (registry != null) {
            putRegistryFromSettings(registry);
        }
        
        System.out.println("=== FIN  DU  WIZARD === ("+exitFlag+")");
        return exitFlag;
    }

    
    private boolean toBeLaunched() {

        boolean launch = false;

        // Check if wizardflag shows it needs to be launched
        String flag = NbPreferences.forModule(App.class).get("optionswizard", "");

        // If number matches, set launch on
        if (!flag.equals("4")) { // increase number here each time we want to force wizard launch for users at a new release
            launch = true;
        }

        return launch;
    }

    @SuppressWarnings("unchecked")
    public void performAction() {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(NbBundle.getMessage(OptionsWizardWizardAction.class, "CTL_WizardTitle"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        exitFlag = (wizardDescriptor.getValue() == WizardDescriptor.FINISH_OPTION);
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                        new OptionsWizardWizardPanel1(),
                        new OptionsWizardWizardPanel2(),
                        new OptionsWizardWizardPanel3(),
                        new OptionsWizardWizardPanel4()
                    };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }
        return panels;
    }

    public String getName() {
        return NbBundle.getMessage(OptionsWizardWizardAction.class, "CTL_WizardTitle");
    }

    @Override
    public String iconResource() {
        return null;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private void putRegistryFromSettings(Registry registry) {
        App.putRegistryFromSettings(registry);
        Registry.persist();

    }
}


