package ancestris.modules.imports.wizard;

import ancestris.api.imports.Import;
import ancestris.gedcom.GedcomDirectory;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.Resources;
import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.text.MessageFormat;
import javax.swing.JComponent;
import org.openide.*;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

// An example action demonstrating how the wizard could be called from within
// your code. You can copy-paste the code below wherever you need.
public final class ImportWizardAction extends CallableSystemAction {

    private WizardDescriptor.Panel<WizardDescriptor>[] panels;

    @Override
    public void performAction() {
        Import importMethod;
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(NbBundle.getMessage(ImportWizardAction.class, "ImportWizardAction.title"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        ImportVisualImport importPanel = null;
        try {
            importPanel = ((ImportVisualImport) (panels[1].getComponent()));
        } catch (Exception e) {
        }
        if (!cancelled && importPanel != null && importPanel.getInputFile() != null) {
            importMethod = importPanel.getImportClass();
            if (importMethod == null) {
                return;
            }
            importMethod.setTabName(NbBundle.getMessage(ImportWizardAction.class, "OpenIDE-Module-Name") + " - " + importMethod.toString());
            File inputFile = importPanel.getInputFile();
            File outFile = new File(inputFile.getParent() + System.getProperty("file.separator") + inputFile.getName().replaceFirst("[.][^.]+$", "") +"_ancestris.ged");   // System.getProperty("java.io.tmpdir") + System.getProperty("file.separator")
            // Import file fixing most issues
            if (importMethod.run(inputFile, outFile) == true) {
                Context context = GedcomDirectory.getDefault().openAncestrisGedcom(FileUtil.toFileObject(outFile));
                if (context != null) {
                    // If imported correctly, fix remaining gedcom issues
                    Gedcom importedGedcom = context.getGedcom();
                    importedGedcom.setName(inputFile.getName());
                    importMethod.fixGedcom(importedGedcom);
                    importMethod.complete();
                    
                    // Save as a new file
                    GedcomDirectory.getDefault().saveAsGedcom(context, outFile);
                    
                    // Popup results
                    Resources RES = Resources.get(GedcomDirectory.class);
                    Object rc = DialogManager.create(RES.getString("cc.open.title"),
                            RES.getString("cc.importResults?", inputFile.getName(), importMethod.toString(),
                                    importMethod.getIndisNb(), importMethod.getFamsNb(), importMethod.getNotesNb(), importMethod.getObjesNb(),
                                    importMethod.getSoursNb(), importMethod.getReposNb(), importMethod.getSubmsNb(), importMethod.getChangesNb()))
                            .setMessageType(DialogManager.WARNING_MESSAGE).setOptionType(DialogManager.YES_NO_OPTION).show();
                    if (rc == DialogManager.YES_OPTION) {
                        importMethod.showDetails();
                    }
                }
                //outFile.delete();
            }
        }
    }

    /**
     * Initialize panels representing individual wizard's steps and sets various
     * properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                new ImportWizardWarning(),
                new ImportWizardImport()
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
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", i);
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

    @Override
    public String getName() {
        return NbBundle.getMessage(ImportWizardAction.class, "ImportWizardAction.title");
    }

    @Override
    public String iconResource() {
        return null;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
