package ancestris.modules.gedcom.mergeentity;

import static ancestris.modules.gedcom.mergeentity.Bundle.merge_title;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Tools",
id = "ancestris.modules.gedcom.mergeentity.MergeEntityAction")
@ActionRegistration(iconBase = "ancestris/modules/gedcom/mergeentity/mergeEntity.png",
displayName = "#CTL_MergeEntityAction")
@ActionReferences({
    @ActionReference(path = "Menu/Tools/Gedcom", position = 3333)
})
@NbBundle.Messages({
    "CTL_MergeEntityAction=Merge 2 Entity",
    "merge.title=Merge Entities"
})
public final class MergeEntityAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        MergeEntityPanel mergeEntityPanel = new MergeEntityPanel();

        // display merge result
        DialogDescriptor gedcomMergeResultDescriptor = new DialogDescriptor(
                mergeEntityPanel,
                merge_title(),
                false,
                new Object[]{NotifyDescriptor.OK_OPTION, NotifyDescriptor.CLOSED_OPTION},
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        DialogDisplayer.getDefault().createDialog(gedcomMergeResultDescriptor).setVisible(true);
    }
}
