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
import ancestris.util.GedcomUtilities;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.UnitOfWork;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
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
    
    private Gedcom gedcom = null;
    private int nbTagsRemoved = 0;

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
            gedcom = contextToOpen.getGedcom();

            RemoveTagPanel removeTagPanel = new RemoveTagPanel(gedcom);
            
            JButton deleteButton = new JButton(NbBundle.getMessage(getClass(), "Button_Delete"));
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    removeTagPanel.savePreferences();
                     if (deleteTags(removeTagPanel.getSettings())) {
                         DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(RemoveTagAction.class, "RemoveTagAction.done", nbTagsRemoved, removeTagPanel.getSettings().tag), NotifyDescriptor.INFORMATION_MESSAGE));
                     }
                }
            });
            JButton cancelButton = new JButton(NbBundle.getMessage(getClass(), "Button_Cancel"));
            Object[] options = new Object[]{deleteButton, cancelButton};

            // Create a custom NotifyDescriptor, specify the panel instance as a parameter + other params
            DialogManager.create(NbBundle.getMessage(RemoveTagAction.class, "CTL_RemoveTagTitle"), removeTagPanel, false)
                    .setMessageType(DialogManager.QUESTION_MESSAGE)
                    .setOptionType(DialogManager.OK_CANCEL_OPTION)
                    .setOptions(options)
                    .setDialogId("removeTagPanel")
                    .show();

        }
    }

    private boolean deleteTags(final RemoveTagPanel.Settings settings) {
        if (settings.tag.isEmpty()) {
            return false;
        }

        try {
            gedcom.doUnitOfWork(new UnitOfWork() {
                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    nbTagsRemoved = 0;
                    for (String entTag : settings.entsTags) {
                        if (!entTag.isEmpty()) {
                            nbTagsRemoved += GedcomUtilities.deleteTags(gedcom, settings.tag, entTag, settings.emptyOnly);
                        }
                    }
                }
            }); // end of doUnitOfWork
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
        return true;
    }
}
