/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: Dominique Baron (lemovice-at-ancestris-dot-org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.removetag;

import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.modules.gedcom.utilities.GedcomUtilities;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.UnitOfWork;
import java.awt.event.ActionEvent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

@ActionID(id = "ancestris.modules.gedcom.removetag.RemoveTagAction", category = "Edit")
@ActionRegistration(
        displayName = "#CTL_RemoveTagAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/Edit", name = "RemoveTagAction", position = 2200)
public final class RemoveTagAction extends AbstractAncestrisContextAction {

    public RemoveTagAction() {
        super();
        setImage("ancestris/modules/gedcom/removetag/RemoveTagIcon.png");
        setText(NbBundle.getMessage(RemoveTagAction.class, "CTL_RemoveTagAction"));
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

            // Create a custom NotifyDescriptor, specify the panel instance as a parameter + other params
            RemoveTagPanel removeTagPanel = new RemoveTagPanel();
            Object choice = DialogManager.create(NbBundle.getMessage(RemoveTagAction.class, "CTL_RemoveTagTitle"), removeTagPanel)
                    .setMessageType(DialogManager.QUESTION_MESSAGE)
                    .setOptionType(DialogManager.OK_CANCEL_OPTION)
                    .setDialogId("removeTagPanel")
                    .show();

             if (choice == DialogManager.OK_OPTION) {
                final String tag = removeTagPanel.getTag();
                final int selectedentity = removeTagPanel.getSelectedEntityIndex();
                final boolean emptyTagOnly = removeTagPanel.getSelectedEmptyTag();
                try {
                    gedcom.doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            GedcomUtilities.deleteTags(gedcom, tag, selectedentity, emptyTagOnly);
                        }
                    }); // end of doUnitOfWork
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(RemoveTagAction.class, "RemoveTagAction.done", tag, removeTagPanel.getSelectedEntityItem()), NotifyDescriptor.INFORMATION_MESSAGE));
            }
        }
    }
}
