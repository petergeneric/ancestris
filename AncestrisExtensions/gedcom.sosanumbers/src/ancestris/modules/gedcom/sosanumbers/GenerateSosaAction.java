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
import ancestris.util.swing.DialogManager;
import genj.gedcom.Context;
import genj.gedcom.Indi;
import java.awt.event.ActionEvent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(id = "ancestris.modules.gedcom.sosanumbers.GenerateSosaAction", category = "Tools")
@ActionRegistration(
        displayName = "#CTL_GenerateSosaAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/Tools/Gedcom", name = "GenerateSosaAction", position = 300)
public final class GenerateSosaAction extends AbstractAncestrisContextAction implements Constants {

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
        
        // Ask user to choose numbering preferences
        SosaPanel sosaPanel = new SosaPanel(context);
        Object choice = DialogManager.create(NbBundle.getMessage(GenerateSosaAction.class, "GenerateSosaAction.AskDeCujus"), sosaPanel)
                .setMessageType(DialogManager.PLAIN_MESSAGE)
                .setOptionType(DialogManager.OK_CANCEL_OPTION)
                .show();

        // Return if cancelled
        if (choice == DialogManager.CANCEL_OPTION) {
            return false;
        }
        
        // Perform selected action
        Indi indiDeCujus = null;
        indiDeCujus = (Indi) sosaPanel.getSelection();
        sosaPanel.savePreferences();
        int changes = new SosaNumbersGenerator().run(context.getGedcom(), indiDeCujus);
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(sosaPanel.getResultMessage(changes), NotifyDescriptor.INFORMATION_MESSAGE));
        return true;
    }
}
