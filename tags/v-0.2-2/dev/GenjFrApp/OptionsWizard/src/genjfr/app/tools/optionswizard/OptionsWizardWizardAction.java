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

        if (exitFlag = true) {
            NbPreferences.forModule(App.class).put("optionswizard", "3"); // should be same as below
        }
        
        if (registry != null) {
            putRegistryFromSettings(registry);
        }
        
        System.out.println("=== FIN  DU  WIZARD ===");
        return exitFlag;
    }

    
    private boolean toBeLaunched() {

        boolean launch = false;

        // Check if wizardflag shows it neesd to be launched
        String flag = NbPreferences.forModule(App.class).get("optionswizard", "");

        // If number matches, set launch on
        if (!flag.equals("3")) { // increase number here each time we want to force wizard launch for users at a new release
            launch = true;
        }

        return launch;
    }

    public void performAction() {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(NbBundle.getMessage(OptionsWizardWizardAction.class, "CTL_WizardTitle"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            exitFlag = true;
        }
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
        registry.put("options.genj.app.Options.language", NbPreferences.forModule(App.class).get("language", ""));
        registry.put("options.genj.app.Options.lookAndFeel", NbPreferences.forModule(App.class).get("skin", ""));
        registry.put("options.genj.app.Options.isRestoreViews", NbPreferences.forModule(App.class).get("restoreWindows", ""));
        registry.put("options.genj.edit.Options.isAutoCommit", NbPreferences.forModule(App.class).get("autoCommit", ""));
        registry.put("options.genj.gedcom.Options.numberOfUndos", NbPreferences.forModule(App.class).get("undos", ""));
        registry.put("options.genj.edit.Options.isSplitJurisdictions", NbPreferences.forModule(App.class).get("splitJurisdiction", ""));
        registry.put("options.genj.edit.Options.isOpenEditor", NbPreferences.forModule(App.class).get("OpenEditor", ""));

        registry.put("options.genj.report.Options.birthSymbol", NbPreferences.forModule(App.class).get("symbolBirth", ""));
        registry.put("options.genj.report.Options.baptismSymbol", NbPreferences.forModule(App.class).get("symbolBapm", ""));
        registry.put("options.genj.report.Options.childOfSymbol", NbPreferences.forModule(App.class).get("symbolChildOf", ""));
        registry.put("options.genj.report.Options.engagingSymbol", NbPreferences.forModule(App.class).get("symbolEngm", ""));
        registry.put("options.genj.gedcom.Options.txtMarriageSymbol", NbPreferences.forModule(App.class).get("symbolMarr", ""));
        registry.put("options.genj.report.Options.divorceSymbol", NbPreferences.forModule(App.class).get("symbolDivc", ""));
        registry.put("options.genj.report.Options.occuSymbol", NbPreferences.forModule(App.class).get("symbolOccu", ""));
        registry.put("options.genj.report.Options.resiSymbol", NbPreferences.forModule(App.class).get("symbolResi", ""));
        registry.put("options.genj.report.Options.deathSymbol", NbPreferences.forModule(App.class).get("symbolDeat", ""));
        registry.put("options.genj.report.Options.burialSymbol", NbPreferences.forModule(App.class).get("symbolBuri", ""));
        registry.put("options.genj.gedcom.Options.maskPrivate", NbPreferences.forModule(App.class).get("privDisplay", ""));
        registry.put("options.genj.report.Options.privateTag", NbPreferences.forModule(App.class).get("privFlag", ""));
        registry.put("options.genj.report.Options.deceasedIsPublic", NbPreferences.forModule(App.class).get("privAlive", ""));
        registry.put("options.genj.report.Options.yearsEventsArePrivate", NbPreferences.forModule(App.class).get("privYears", ""));
        registry.put("options.genj.gedcom.Options.valueLineBreak", NbPreferences.forModule(App.class).get("txtLineBreak", ""));
        registry.put("options.genj.gedcom.Options.maxImageFileSizeKB", NbPreferences.forModule(App.class).get("imageSize", ""));
        registry.put("options.genj.gedcom.Options.nameFormat", NbPreferences.forModule(App.class).get("displayNames", ""));
        registry.put("options.genj.gedcom.Options.dateFormat", NbPreferences.forModule(App.class).get("displayDates", ""));

        registry.put("options.genj.gedcom.Options.submName", NbPreferences.forModule(App.class).get("submName", ""));
        registry.put("options.genj.gedcom.Options.submCity", NbPreferences.forModule(App.class).get("submCity", ""));
        registry.put("options.genj.gedcom.Options.submPhone", NbPreferences.forModule(App.class).get("submPhone", ""));
        registry.put("options.genj.gedcom.Options.submPostCode", NbPreferences.forModule(App.class).get("submPostCode", ""));
        registry.put("options.genj.gedcom.Options.submEmail", NbPreferences.forModule(App.class).get("submEmail", ""));
        registry.put("options.genj.gedcom.Options.submCountry", NbPreferences.forModule(App.class).get("submCountry", ""));
        registry.put("options.genj.gedcom.Options.submWeb", NbPreferences.forModule(App.class).get("submWeb", ""));
        registry.put("options.genj.gedcom.Options.isUpperCaseNames", NbPreferences.forModule(App.class).get("NamesUppercase", ""));
        registry.put("options.genj.gedcom.Options.setWifeLastname", NbPreferences.forModule(App.class).get("NamesSpouse", ""));
        registry.put("options.genj.gedcom.Options.fmt_address1", NbPreferences.forModule(App.class).get("fmt_address1", ""));
        registry.put("options.genj.gedcom.Options.fmt_address2", NbPreferences.forModule(App.class).get("fmt_address2", ""));
        registry.put("options.genj.gedcom.Options.fmt_address3", NbPreferences.forModule(App.class).get("fmt_address3", ""));
        registry.put("options.genj.gedcom.Options.fmt_address4", NbPreferences.forModule(App.class).get("fmt_address4", ""));
        registry.put("options.genj.gedcom.Options.fmt_address5", NbPreferences.forModule(App.class).get("fmt_address5", ""));
        registry.put("options.genj.gedcom.Options.fmt_address6", NbPreferences.forModule(App.class).get("fmt_address6", ""));
        registry.put("options.genj.gedcom.Options.fmt_address7", NbPreferences.forModule(App.class).get("fmt_address7", ""));
        registry.put("options.genj.gedcom.Options.fmt_address1_mand", NbPreferences.forModule(App.class).get("fmt_address1_mand", ""));
        registry.put("options.genj.gedcom.Options.fmt_address2_mand", NbPreferences.forModule(App.class).get("fmt_address2_mand", ""));
        registry.put("options.genj.gedcom.Options.fmt_address3_mand", NbPreferences.forModule(App.class).get("fmt_address3_mand", ""));
        registry.put("options.genj.gedcom.Options.fmt_address4_mand", NbPreferences.forModule(App.class).get("fmt_address4_mand", ""));
        registry.put("options.genj.gedcom.Options.fmt_address5_mand", NbPreferences.forModule(App.class).get("fmt_address5_mand", ""));
        registry.put("options.genj.gedcom.Options.fmt_address6_mand", NbPreferences.forModule(App.class).get("fmt_address6_mand", ""));
        registry.put("options.genj.gedcom.Options.fmt_address7_mand", NbPreferences.forModule(App.class).get("fmt_address7_mand", ""));
        registry.put("options.genj.gedcom.Options.isUseSpacedPlaces", NbPreferences.forModule(App.class).get("address_splitspaces", ""));
        registry.put("options.genj.gedcom.Options.isFillGapsInIDs", NbPreferences.forModule(App.class).get("IDFilling", ""));
        registry.put("options.genj.gedcom.Options.defaultEncoding", NbPreferences.forModule(App.class).get("encoding", ""));
        registry.put("options.genj.app.Options.isWriteBOM", NbPreferences.forModule(App.class).get("BOM", ""));

        registry.put("options.genj.gedcom.Options.gedcomFile", NbPreferences.forModule(App.class).get("gedcomFile", ""));
        registry.put("options.genj.gedcom.Options.reportDir", NbPreferences.forModule(App.class).get("reportDir", ""));
        registry.put("options.associations", NbPreferences.forModule(App.class).get("6", ""));
        registry.put("options.associations.1", NbPreferences.forModule(App.class).get("assoTxt", ""));
        registry.put("options.associations.2", NbPreferences.forModule(App.class).get("assoOffice", ""));
        registry.put("options.associations.3", NbPreferences.forModule(App.class).get("assoAdobe", ""));
        registry.put("options.associations.4", NbPreferences.forModule(App.class).get("assoImages", ""));
        registry.put("options.associations.5", NbPreferences.forModule(App.class).get("assoSound", ""));
        registry.put("options.associations.6", NbPreferences.forModule(App.class).get("assoWeb", ""));
        registry.put("options.genj.app.Options.maxLogSizeKB", NbPreferences.forModule(App.class).get("logSize", ""));

        Registry.persist();

    }
}


