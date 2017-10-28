/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Gedcom;
import java.awt.event.ActionEvent;
import org.openide.util.NbBundle;

public final class ActionReplace extends AbstractAncestrisContextAction {

    // Redo elements
    private int undoNb;
    
    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        Gedcom gedcom = getContext().getGedcom();

        // Save undo index for use in cancel
        undoNb = gedcom.getUndoNb();
        
        ReplacePanel panel = new ReplacePanel(gedcom, true);
        Object o = DialogManager.create(NbBundle.getMessage(getClass(), "TITL_Replace", gedcom.getName()), panel).setMessageType(DialogManager.PLAIN_MESSAGE).setOptionType(DialogManager.OK_CANCEL_OPTION).setDialogId("findreplace").show();
        panel.saveParams();
        if (o != DialogManager.OK_OPTION) {
            panel.cancel(gedcom, undoNb);
        }
    }
    
}

