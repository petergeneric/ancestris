/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.marking;

import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.modules.document.view.DocumentViewTopComponent;
import ancestris.util.ProgressListener;
import ancestris.util.swing.DialogManager;
import ancestris.util.swing.SelectEntityPanel;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import java.awt.event.ActionEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.*;
import spin.Spin;

/**
 *
 * @author frederic
 */
@ActionID(id = "ancestris.modules.gedcom.marking.MarkingAction", category = "Edit")
@ActionRegistration(
        displayName = "#CTL_MarkingAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/Edit", name = "MarkingAction", position = 2500)
public final class MarkingAction extends AbstractAncestrisContextAction {

    private Gedcom gedcom = null;
    
    public MarkingAction() {
        super();
        setImage("ancestris/modules/gedcom/marking/MarkingIcon.png");
        setText(NbBundle.getMessage(MarkingAction.class, "CTL_MarkingAction"));
    }

    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        // Ask user to choose numbering preferences
        Context contextToOpen = getContext();
        gedcom = contextToOpen.getGedcom();

        MarkingPanel markingPanel = new MarkingPanel(getContext());
        Object choice = DialogManager.create(NbBundle.getMessage(MarkingAction.class, "MarkingAction.AskParams"), markingPanel)
                .setMessageType(DialogManager.QUESTION_MESSAGE)
                .setOptionType(DialogManager.OK_CANCEL_OPTION)
                .setDialogId("markingPanel")
                .show();

        if (choice == DialogManager.OK_OPTION) {
            markingPanel.savePreferences();
            
            // Get a first individual
            Indi indiDeCujus = null;
            if (markingPanel.getSettings().isTreeTop || markingPanel.getSettings().isTreeBottom) {
                Entity entity = contextToOpen.getEntity();
                if (entity instanceof Indi) {
                    indiDeCujus = (Indi) entity;
                } else {
                    // Selection box
                    SelectEntityPanel select = new SelectEntityPanel(gedcom, Gedcom.INDI, NbBundle.getMessage(this.getClass(), "MarkingAction.AskIndividual"),
                            contextToOpen.getEntity());
                    if (DialogManager.OK_OPTION != DialogManager.create(NbBundle.getMessage(this.getClass(), "CTL_MarkingAction"), select)
                            .setMessageType(DialogManager.QUESTION_MESSAGE).setOptionType(DialogManager.OK_CANCEL_OPTION).setDialogId("markingPanelIndi").show()) {
                        return;
                    }
                    indiDeCujus = (Indi) select.getSelection();
                }
                if (indiDeCujus == null) {
                    return;
                }
                // we have our root indi

            }
            
            // Caution : a separate thread takes longer with the gedcom update modifications.
            // It is therefore important not to have to many windows open
            // In particular the Documents Window must be closed as it may contain a lot of entities with tags to delete (could take a minute longer just to delete)
            // So close the Documents window as it slows down the whole process
            DocumentViewTopComponent.findInstance().close();
            
            // Now run the task
            MarkingTask task = (MarkingTask) Spin.off(MarkingTaskFactory.create(contextToOpen, indiDeCujus, markingPanel.getSettings()));
            ProgressListener.Dispatcher.processStarted(task);
            commit(task);
            ProgressListener.Dispatcher.processStopped(task);
        }

    }
    
    private void commit(final Runnable task) {
        try {
            if (gedcom.isWriteLocked()) {
                task.run();
            } else {
                gedcom.doUnitOfWork((Gedcom localGedcom) -> {
                    task.run();
                });
            }
        } catch (GedcomException ge) {
            Exceptions.printStackTrace(ge);
        } finally {
        }
    }
    
    


}
