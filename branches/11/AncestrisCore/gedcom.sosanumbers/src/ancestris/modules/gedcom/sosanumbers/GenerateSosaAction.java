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
package ancestris.modules.gedcom.sosanumbers;

import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.util.ProgressListener;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import java.awt.event.ActionEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import spin.Spin;

@ActionID(id = "ancestris.modules.gedcom.sosanumbers.GenerateSosaAction", category = "Edit")
@ActionRegistration(
        displayName = "#CTL_GenerateSosaAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/Edit", name = "GenerateSosaAction", position = 2400)
public final class GenerateSosaAction extends AbstractAncestrisContextAction implements Constants {

    private static Gedcom gedcom = null;
    
    public GenerateSosaAction() {
        super();
        setImage("ancestris/modules/gedcom/sosanumbers/SosaNumbersIcon.png");
        setText(NbBundle.getMessage(GenerateSosaAction.class, "CTL_GenerateSosaAction"));
    }
    
    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    /**
     * Performs action from menu
     * @param event 
     */
    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        runSosaAction(getContext());
    }
    
    public static boolean runSosaAction(Context context) {
        // Return if no context
        if (context == null) {
            return false;
        }
        gedcom = context.getGedcom();
        
        // Ask user to choose numbering preferences
        SosaPanel sosaPanel = new SosaPanel(context);
        Object choice = DialogManager.create(NbBundle.getMessage(GenerateSosaAction.class, "GenerateSosaAction.AskDeCujus"), sosaPanel)
                .setMessageType(DialogManager.PLAIN_MESSAGE)
                .setOptionType(DialogManager.OK_CANCEL_OPTION)
                .setDialogId("sosaPanel")
                .show();

        if (choice == DialogManager.OK_OPTION) {
            // Perform selected action
            sosaPanel.savePreferences();
            SosaNumbersTask task = (SosaNumbersTask) Spin.off(SosaNumbersTaskFactory.create(gedcom, sosaPanel.getSelection(), sosaPanel.getResultMessage()));
            ProgressListener.Dispatcher.processStarted(task);
            commit(task);
            ProgressListener.Dispatcher.processStopped(task);
            return true;
        } else {
            return false;
        }
        
    }
    
    private static void commit(final Runnable task) {
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
