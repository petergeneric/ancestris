/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.webbook;

import ancestris.core.actions.AbstractAncestrisContextAction;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

@ActionID(id = "ancestris.modules.webbook.WebBookWizardAction", category = "Tools")
@ActionRegistration(
        displayName = "#CTL_WebBookAction",
        iconInMenu = true,
        lazy = false)
@ActionReferences({
    @ActionReference(path = "Menu/Tools/Multimedia", name = "WebBookWizardAction", position = 100)
})
public final class WebBookWizardAction extends AbstractAncestrisContextAction {

    private WizardDescriptor.Panel[] panels;

    public WebBookWizardAction() {
        super();
        setImage("ancestris/modules/webbook/WebBook.png");
        setText(NbBundle.getMessage(WebBookWizardAction.class, "CTL_WebBookAction"));
    }

    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        Context contextToOpen = getContext();
        if (contextToOpen != null) {
            Gedcom gedcom = contextToOpen.getGedcom();
            if (gedcom != null) {
                WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels(gedcom));
                // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
                wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
                wizardDescriptor.setTitle(NbBundle.getMessage(WebBookWizardAction.class, "CTL_WebBookTitle") + " - " + gedcom.getDisplayName());
                Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
                dialog.setVisible(true);
                dialog.toFront();
                boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
                if (!cancelled) {
                    // user pressed ok
                    WebBookStarter wbs = new WebBookStarter(gedcom);
                    wbs.start();
                } else {
                    // user pressed annuler
                }
                panels = null;
            }
        }
    }

    /**
     * Initialize panels representing individual wizard's steps and sets various
     * properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels(Gedcom gedcom) {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                new WebBookWizardPanel1(gedcom),
                new WebBookWizardPanel2(gedcom),
                new WebBookWizardPanel3(gedcom),
                new WebBookWizardPanel4(gedcom),
                new WebBookWizardPanel5(gedcom),
                new WebBookWizardPanel6(gedcom),
                new WebBookWizardPanel7(gedcom)
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

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

}
