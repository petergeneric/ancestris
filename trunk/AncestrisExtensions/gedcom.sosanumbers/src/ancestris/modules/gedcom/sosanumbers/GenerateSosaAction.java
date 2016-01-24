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
import ancestris.util.swing.SelectEntityPanel;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.util.Registry;
import java.awt.event.ActionEvent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import static ancestris.modules.gedcom.sosanumbers.Bundle.*;

@ActionID(id = "ancestris.modules.gedcom.sosanumbers.GenerateSosaAction", category = "Tools")
@ActionRegistration(
        displayName = "#CTL_GenerateSosaAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/Tools/Gedcom", name = "GenerateSosaAction", position = 300)
@NbBundle.Messages("ok.label=Generate SOSA Numbering")
public final class GenerateSosaAction extends AbstractAncestrisContextAction {

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

    Gedcom myGedcom = null;

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        Context contextToOpen = getContext();

        if (contextToOpen != null) {
            myGedcom = contextToOpen.getGedcom();
            Registry registry = myGedcom.getRegistry();

            SelectEntityPanel selectEntityPanel = new SelectEntityPanel(myGedcom, Gedcom.INDI);

            String selectedEntityID = registry.get("INDI.decujus.id", "");
            if (!selectedEntityID.isEmpty()) {
                Entity selectedEntity = myGedcom.getEntity(selectedEntityID);
                if (selectedEntity != null) {
                    selectEntityPanel.setSelection(selectedEntity);
                }
            }

            String clearChoice = NbBundle.getMessage(GenerateSosaAction.class, "GenerateSosaAction.clear");
            String okText = ok_label();
            Object choice = DialogManager.create(
                    NbBundle.getMessage(this.getClass(), "GenerateSosaAction.AskDeCujus"),
                    selectEntityPanel)
                    .setMessageType(DialogManager.QUESTION_MESSAGE)
                    .setOptions(new Object[]{okText, DialogManager.CANCEL_OPTION})
                    .setAdditionalOptions(new Object[]{clearChoice})
                    .show();

            if (choice != DialogManager.CANCEL_OPTION) {
                Indi indiDeCujus = null;
                if (choice == okText) {
                    indiDeCujus = (Indi) selectEntityPanel.getSelection();
                }
                // else null means clear sosa
                new SosaNumbers().generateSosaNbs(myGedcom, indiDeCujus);
                if (indiDeCujus == null) {
                    registry.remove("INDI.decujus.id");
                } else {
                    registry.put("INDI.decujus.id", indiDeCujus.getId());
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(GenerateSosaAction.class, "GenerateSosaAction.done", indiDeCujus.getName()), NotifyDescriptor.INFORMATION_MESSAGE));
                }
            }
        }
    }
}
