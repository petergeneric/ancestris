package ancestris.modules.imports.wizard;

import ancestris.api.imports.Import;
import ancestris.gedcom.GedcomDirectory;
import genj.app.Workbench;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

// An example action demonstrating how the wizard could be called from within
// your code. You can copy-paste the code below wherever you need.
public final class ImportWizardAction extends CallableSystemAction {

    private WizardDescriptor.Panel[] panels;

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
        if (!cancelled) {
            importMethod = ((ImportVisualImport) (panels[1].getComponent())).getImportClass();
            try {
                File inputFile = ((ImportVisualImport) (panels[1].getComponent())).getInputFile();
                File outFile = new File (System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + inputFile.getName());
                if (importMethod.run(inputFile, outFile, NAME) == true) {
                    Context context = Workbench.getInstance().openGedcom(outFile.toURI().toURL());
                    Gedcom importedGedcom = context.getGedcom();
                    importedGedcom.setName(inputFile.getName());
                    importedGedcom.setOrigin(null);
                    GedcomDirectory.getInstance().getDummyNode(context).fire(true);
                    outFile.delete();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
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
