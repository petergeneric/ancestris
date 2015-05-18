package ancestris.modules.gedcom.mergeentity.actions;

import ancestris.modules.gedcom.mergeentity.panels.MergeEntityPanel;
import genj.gedcom.Context;
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
import org.openide.util.Utilities;

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
        Context context;
        if ((context = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {
            MergeEntityPanel mergeEntityPanel = new MergeEntityPanel();
            mergeEntityPanel.setGedcom(context.getGedcom());
            
            // display merge result
            DialogDescriptor gedcomMergeResultDescriptor = new DialogDescriptor(
                    mergeEntityPanel,
                    "xxx",
                    false,
                    new Object[]{NotifyDescriptor.CLOSED_OPTION},
                    DialogDescriptor.CLOSED_OPTION,
                    DialogDescriptor.DEFAULT_ALIGN,
                    null,
                    null);
            DialogDisplayer.getDefault().createDialog(gedcomMergeResultDescriptor).setVisible(true);
        }
    }
}
