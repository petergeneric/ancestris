package ancestris.modules.gedcom.mergefile;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.util.swing.DialogManager;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(id = "ancestris.modules.gedcom.merge.GedcomMergeAction", category = "File")
@ActionRegistration(
        displayName = "#CTL_GedcomMergeAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/File", name = "GedcomMergeWizardAction", position = 300)
public final class GedcomMergeWizardAction extends AbstractAncestrisAction { 

    public GedcomMergeWizardAction() {
        super();
        setImage("ancestris/modules/gedcom/mergefile/MergeFileIcon.png");
        setText(NbBundle.getMessage(GedcomMergeWizardAction.class, "CTL_GedcomMergeAction"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        JButton OKButton = new JButton(NbBundle.getMessage(GedcomMergeWizardAction.class, "ok_button"));
        JButton cancelButton = new JButton(NbBundle.getMessage(GedcomMergeWizardAction.class, "cancel_button"));
        Object[] options = new Object[] { OKButton, cancelButton };
        GedcomMergePanel panel = new GedcomMergePanel(OKButton);
        Object o = DialogManager.create(NbBundle.getMessage(GedcomMergeWizardAction.class, "CTL_GedcomMergeAction").replaceAll("&", ""), panel).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(options).setDialogId("mergeWindow").show();
        if (o == OKButton) {
            new GedcomMerge(panel.getLeftGedcomFile(), panel.getRightGedcomFile(), panel.getGedcomMergeFile()).run();
        }
    }
}
