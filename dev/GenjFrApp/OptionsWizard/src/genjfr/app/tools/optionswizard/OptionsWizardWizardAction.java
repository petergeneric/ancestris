/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.optionswizard;

import genj.util.Registry;
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

        System.out.println("======================= DEBUT DU WIZARD =========================");
        this.registry = (Registry) o;
        performAction();
        return exitFlag;
    }

    public void performAction() {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        getSettingsFromRegistry(registry);
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(NbBundle.getMessage(OptionsWizardWizardAction.class, "CTL_WizardTitle"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            putRegistryFromSettings(registry);
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

    private void getSettingsFromRegistry(Registry registry) {
        System.out.println("*****getSettingsFromRegistry : " + registry.get("options.genj.app.Options.language", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel1.class).put("language", registry.get("options.genj.app.Options.language", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel1.class).put("skin", registry.get("options.genj.app.Options.lookAndFeel", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel1.class).put("restoreWindows", registry.get("options.genj.app.Options.isRestoreViews", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel1.class).put("autoCommit", registry.get("options.genj.edit.Options.isAutoCommit", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel1.class).put("undos", registry.get("options.genj.gedcom.Options.numberOfUndos", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel1.class).put("splitJurisdiction", registry.get("options.genj.edit.Options.isSplitJurisdictions", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel1.class).put("OpenEditor", registry.get("options.genj.edit.Options.isOpenEditor", ""));

        NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolBirth", registry.get("options.genj.report.Options.birthSymbol", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolBapm", registry.get("options.genj.report.Options.baptismSymbol", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolChildOf", registry.get("options.genj.report.Options.childOfSymbol", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolEngm", registry.get("options.genj.report.Options.engagingSymbol", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolMarr", registry.get("options.genj.gedcom.Options.txtMarriageSymbol", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolDivc", registry.get("options.genj.report.Options.divorceSymbol", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolOccu", registry.get("options.genj.report.Options.occuSymbol", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolResi", registry.get("options.genj.report.Options.resiSymbol", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolDeat", registry.get("options.genj.report.Options.deathSymbol", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("symbolBuri", registry.get("options.genj.report.Options.burialSymbol", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("privDisplay", registry.get("options.genj.gedcom.Options.maskPrivate", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("privFlag", registry.get("options.genj.report.Options.privateTag", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("privAlive", registry.get("options.genj.report.Options.deceasedIsPublic", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("privYears", registry.get("options.genj.report.Options.yearsEventsArePrivate", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("txtLineBreak", registry.get("options.genj.gedcom.Options.valueLineBreak", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("imageSize", registry.get("options.genj.gedcom.Options.maxImageFileSizeKB", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("displayNames", registry.get("options.genj.gedcom.Options.nameFormat", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel2.class).put("displayDates", registry.get("options.genj.gedcom.Options.dateFormat", ""));

        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("submName", registry.get("options.genj.gedcom.Options.submName", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("submCity", registry.get("options.genj.gedcom.Options.submCity", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("submPhone", registry.get("options.genj.gedcom.Options.submPhone", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("submPostCode", registry.get("options.genj.gedcom.Options.submPostCode", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("submEmail", registry.get("options.genj.gedcom.Options.submEmail", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("submCountry", registry.get("options.genj.gedcom.Options.submCountry", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("submWeb", registry.get("options.genj.gedcom.Options.submWeb", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("NamesUppercase", registry.get("options.genj.gedcom.Options.isUpperCaseNames", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("NamesSpouse", registry.get("options.genj.gedcom.Options.setWifeLastname", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address1", registry.get("options.genj.gedcom.Options.fmt_address1", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address2", registry.get("options.genj.gedcom.Options.fmt_address2", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address3", registry.get("options.genj.gedcom.Options.fmt_address3", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address4", registry.get("options.genj.gedcom.Options.fmt_address4", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address5", registry.get("options.genj.gedcom.Options.fmt_address5", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address6", registry.get("options.genj.gedcom.Options.fmt_address6", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address7", registry.get("options.genj.gedcom.Options.fmt_address7", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address1_mand", registry.get("options.genj.gedcom.Options.fmt_address1_mand", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address2_mand", registry.get("options.genj.gedcom.Options.fmt_address2_mand", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address3_mand", registry.get("options.genj.gedcom.Options.fmt_address3_mand", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address4_mand", registry.get("options.genj.gedcom.Options.fmt_address4_mand", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address5_mand", registry.get("options.genj.gedcom.Options.fmt_address5_mand", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address6_mand", registry.get("options.genj.gedcom.Options.fmt_address6_mand", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("fmt_address7_mand", registry.get("options.genj.gedcom.Options.fmt_address7_mand", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("address_splitspaces", registry.get("options.genj.gedcom.Options.isUseSpacedPlaces", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("IDFilling", registry.get("options.genj.gedcom.Options.isFillGapsInIDs", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("encoding", registry.get("options.genj.gedcom.Options.defaultEncoding", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel3.class).put("BOM", registry.get("options.genj.app.Options.isWriteBOM", ""));

        NbPreferences.forModule(OptionsWizardWizardPanel4.class).put("gedcomDir", registry.get("options.genj.gedcom.Options.gedcomDir", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel4.class).put("gedcomFile", registry.get("options.genj.gedcom.Options.gedcomFile", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel4.class).put("reportDir", registry.get("options.genj.gedcom.Options.reportDir", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel4.class).put("assoTxt", registry.get("options.associations.1", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel4.class).put("assoOffice", registry.get("options.associations.2", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel4.class).put("assoAdobe", registry.get("options.associations.3", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel4.class).put("assoImages", registry.get("options.associations.4", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel4.class).put("assoSound", registry.get("options.associations.5", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel4.class).put("assoWeb", registry.get("options.associations.6", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel4.class).put("logSize", registry.get("options.genj.app.Options.maxLogSizeKB", ""));
        NbPreferences.forModule(OptionsWizardWizardPanel4.class).put("proxy", registry.get("options.genj.app.Options.httpProxy", ""));

    }


    private void putRegistryFromSettings(Registry registry) {
        registry.put("options.genj.app.Options.language", NbPreferences.forModule(OptionsWizardWizardPanel1.class).get("language", ""));
        registry.put("options.genj.app.Options.lookAndFeel", NbPreferences.forModule(OptionsWizardWizardPanel1.class).get("skin", ""));
        registry.put("options.genj.app.Options.isRestoreViews", NbPreferences.forModule(OptionsWizardWizardPanel1.class).get("restoreWindows", ""));
        registry.put("options.genj.edit.Options.isAutoCommit", NbPreferences.forModule(OptionsWizardWizardPanel1.class).get("autoCommit", ""));
        registry.put("options.genj.gedcom.Options.numberOfUndos", NbPreferences.forModule(OptionsWizardWizardPanel1.class).get("undos", ""));
        registry.put("options.genj.edit.Options.isSplitJurisdictions", NbPreferences.forModule(OptionsWizardWizardPanel1.class).get("splitJurisdiction", ""));
        registry.put("options.genj.edit.Options.isOpenEditor", NbPreferences.forModule(OptionsWizardWizardPanel1.class).get("OpenEditor", ""));

        registry.put("options.genj.report.Options.birthSymbol", NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolBirth", ""));
        registry.put("options.genj.report.Options.baptismSymbol", NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolBapm", ""));
        registry.put("options.genj.report.Options.childOfSymbol", NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolChildOf", ""));
        registry.put("options.genj.report.Options.engagingSymbol", NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolEngm", ""));
        registry.put("options.genj.gedcom.Options.txtMarriageSymbol", NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolMarr", ""));
        registry.put("options.genj.report.Options.divorceSymbol", NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolDivc", ""));
        registry.put("options.genj.report.Options.occuSymbol", NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolOccu", ""));
        registry.put("options.genj.report.Options.resiSymbol", NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolResi", ""));
        registry.put("options.genj.report.Options.deathSymbol", NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolDeat", ""));
        registry.put("options.genj.report.Options.burialSymbol", NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("symbolBuri", ""));
        registry.put("options.genj.gedcom.Options.maskPrivate", NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("privDisplay", ""));
        registry.put("options.genj.report.Options.privateTag", NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("privFlag", ""));
        registry.put("options.genj.report.Options.deceasedIsPublic", NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("privAlive", ""));
        registry.put("options.genj.report.Options.yearsEventsArePrivate", NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("privYears", ""));
        registry.put("options.genj.gedcom.Options.valueLineBreak", NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("txtLineBreak", ""));
        registry.put("options.genj.gedcom.Options.maxImageFileSizeKB", NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("imageSize", ""));
        registry.put("options.genj.gedcom.Options.nameFormat", NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("displayNames", ""));
        registry.put("options.genj.gedcom.Options.dateFormat", NbPreferences.forModule(OptionsWizardWizardPanel2.class).get("displayDates", ""));

        registry.put("options.genj.gedcom.Options.submName", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("submName", ""));
        registry.put("options.genj.gedcom.Options.submCity", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("submCity", ""));
        registry.put("options.genj.gedcom.Options.submPhone", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("submPhone", ""));
        registry.put("options.genj.gedcom.Options.submPostCode", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("submPostCode", ""));
        registry.put("options.genj.gedcom.Options.submEmail", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("submEmail", ""));
        registry.put("options.genj.gedcom.Options.submCountry", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("submCountry", ""));
        registry.put("options.genj.gedcom.Options.submWeb", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("submWeb", ""));
        registry.put("options.genj.gedcom.Options.isUpperCaseNames", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("NamesUppercase", ""));
        registry.put("options.genj.gedcom.Options.setWifeLastname", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("NamesSpouse", ""));
        registry.put("options.genj.gedcom.Options.fmt_address1", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address1", ""));
        registry.put("options.genj.gedcom.Options.fmt_address2", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address2", ""));
        registry.put("options.genj.gedcom.Options.fmt_address3", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address3", ""));
        registry.put("options.genj.gedcom.Options.fmt_address4", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address4", ""));
        registry.put("options.genj.gedcom.Options.fmt_address5", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address5", ""));
        registry.put("options.genj.gedcom.Options.fmt_address6", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address6", ""));
        registry.put("options.genj.gedcom.Options.fmt_address7", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address7", ""));
        registry.put("options.genj.gedcom.Options.fmt_address1_mand", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address1_mand", ""));
        registry.put("options.genj.gedcom.Options.fmt_address2_mand", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address2_mand", ""));
        registry.put("options.genj.gedcom.Options.fmt_address3_mand", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address3_mand", ""));
        registry.put("options.genj.gedcom.Options.fmt_address4_mand", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address4_mand", ""));
        registry.put("options.genj.gedcom.Options.fmt_address5_mand", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address5_mand", ""));
        registry.put("options.genj.gedcom.Options.fmt_address6_mand", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address6_mand", ""));
        registry.put("options.genj.gedcom.Options.fmt_address7_mand", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("fmt_address7_mand", ""));
        registry.put("options.genj.gedcom.Options.isUseSpacedPlaces", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("address_splitspaces", ""));
        registry.put("options.genj.gedcom.Options.isFillGapsInIDs", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("IDFilling", ""));
        registry.put("options.genj.gedcom.Options.defaultEncoding", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("encoding", ""));
        registry.put("options.genj.app.Options.isWriteBOM", NbPreferences.forModule(OptionsWizardWizardPanel3.class).get("BOM", ""));

        registry.put("options.genj.gedcom.Options.gedcomDir", NbPreferences.forModule(OptionsWizardWizardPanel4.class).get("gedcomDir", ""));
        registry.put("options.genj.gedcom.Options.gedcomFile", NbPreferences.forModule(OptionsWizardWizardPanel4.class).get("gedcomFile", ""));
        registry.put("options.genj.gedcom.Options.reportDir", NbPreferences.forModule(OptionsWizardWizardPanel4.class).get("reportDir", ""));
        registry.put("options.associations", NbPreferences.forModule(OptionsWizardWizardPanel4.class).get("6", ""));
        registry.put("options.associations.1", NbPreferences.forModule(OptionsWizardWizardPanel4.class).get("assoTxt", ""));
        registry.put("options.associations.2", NbPreferences.forModule(OptionsWizardWizardPanel4.class).get("assoOffice", ""));
        registry.put("options.associations.3", NbPreferences.forModule(OptionsWizardWizardPanel4.class).get("assoAdobe", ""));
        registry.put("options.associations.4", NbPreferences.forModule(OptionsWizardWizardPanel4.class).get("assoImages", ""));
        registry.put("options.associations.5", NbPreferences.forModule(OptionsWizardWizardPanel4.class).get("assoSound", ""));
        registry.put("options.associations.6", NbPreferences.forModule(OptionsWizardWizardPanel4.class).get("assoWeb", ""));
        registry.put("options.genj.app.Options.maxLogSizeKB", NbPreferences.forModule(OptionsWizardWizardPanel4.class).get("logSize", ""));
        registry.put("options.genj.app.Options.httpProxy", NbPreferences.forModule(OptionsWizardWizardPanel4.class).get("proxy", ""));

        Registry.persist();

    }
}


