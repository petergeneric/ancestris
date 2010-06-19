/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.webbook;

import genj.gedcom.Gedcom;
import genj.gedcom.GedcomDirectory;
import genjfr.app.App;
import java.awt.Component;
import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.Iterator;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

// An example action demonstrating how the wizard could be called from within
// your code. You can copy-paste the code below wherever you need.
public final class WebBookWizardAction extends CallableSystemAction {

    private WizardDescriptor.Panel[] panels;

    @SuppressWarnings("unchecked")
    public void performAction() {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(NbBundle.getMessage(WebBookWizardAction.class, "CTL_WebBookTitle"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            // user pressed ok
            WebBookStarter wbs = new WebBookStarter(getGedcom());
            wbs.start();
        } else {
            // user pressed annuler
        }
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                        new WebBookWizardPanel1(),
                        new WebBookWizardPanel2(),
                        new WebBookWizardPanel3(),
                        new WebBookWizardPanel4(),
                        new WebBookWizardPanel5(),
                        new WebBookWizardPanel6(),
                        new WebBookWizardPanel7()
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
                    // Set background image
                    jc.putClientProperty("WizardPanel_image", ImageUtilities.loadImage(NbBundle.getMessage(WebBookWizardAction.class, "CTL_WebBookBckImage")));
                    // Turn on an help tab
                    //jc.putClientProperty("WizardPanel_helpDisplayed", Boolean.TRUE);
                }
            }
        }
        return panels;
    }

    public String getName() {
        return NbBundle.getMessage(WebBookWizardAction.class, "CTL_WebBookAction");
    }

    @Override
    public String iconResource() {
        return NbBundle.getMessage(WebBookWizardAction.class, "CTL_WebBookActionIcon");
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    public static Gedcom getGedcom() {
        Gedcom gedcom = null;
        if (gedcom == null) {
            gedcom = App.center.getSelectedGedcom(); // get selected gedcom
            if (gedcom == null) { // if none selected, take first one
                Iterator it = GedcomDirectory.getInstance().getGedcoms().iterator();
                if (it.hasNext()) { // well, apparently no gedcom exist in the list
                    gedcom = (Gedcom) it.next();
                }
            }
        }
        return gedcom;
    }

}
