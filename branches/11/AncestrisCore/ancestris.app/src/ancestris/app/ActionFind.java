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

public final class ActionFind extends AbstractAncestrisContextAction {

    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        Gedcom gedcom = getContext().getGedcom();
        ReplacePanel panel = new ReplacePanel(gedcom, false);
        Object o = DialogManager.create(NbBundle.getMessage(getClass(), "TITL_Find", gedcom.getName()), panel).setMessageType(DialogManager.PLAIN_MESSAGE).setOptionType(DialogManager.OK_CANCEL_OPTION).setDialogId("findreplace").show();
        panel.saveParams();
    }
    
}

