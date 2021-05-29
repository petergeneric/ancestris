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
package ancestris.modules.gedcom.genids;

import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.util.ProgressListener;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import java.awt.event.ActionEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import spin.Spin;

/**
 *
 * @author frederic
 */
@ActionID(id = "ancestris.modules.gedcom.genids.GenerateIDAction", category = "Edit")
@ActionRegistration(
        displayName = "#CTL_GenerateIDAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/Edit", name = "GenerateIDAction", position = 2450)
public final class GenerateIDAction extends AbstractAncestrisContextAction {

    private Gedcom gedcom = null;
    
    public GenerateIDAction() {
        super();
        setImage("ancestris/modules/gedcom/genids/GenIDsIcon.png");
        setText(NbBundle.getMessage(GenerateIDAction.class, "CTL_GenerateIDAction"));
    }

    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        // Ask user to choose numbering preferences
        gedcom = getContext().getGedcom();
        GenIDPanel genidPanel = new GenIDPanel(getContext());
        Object choice = DialogManager.create(NbBundle.getMessage(GenerateIDAction.class, "GenerateIDAction.AskParams"), genidPanel)
                .setMessageType(DialogManager.QUESTION_MESSAGE)
                .setOptionType(DialogManager.OK_CANCEL_OPTION)
                .setDialogId("genidPanel")
                .show();

        if (choice == DialogManager.OK_OPTION) {
            genidPanel.savePreferences();
            GenerateIDTask task = (GenerateIDTask) Spin.off(GenerateIDTaskFactory.create(gedcom, genidPanel.getSettings()));
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
