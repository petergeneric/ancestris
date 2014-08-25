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

import ancestris.util.swing.DialogManager;
import ancestris.util.swing.SelectEntityPanel;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

@ActionID(id = "ancestris.modules.gedcom.sosanumbers.GenerateSosaAction",
        category = "Tools")
@ActionRegistration(iconBase = "ancestris/modules/gedcom/sosanumbers/SosaNumbersIcon.png",
        iconInMenu = true,
        displayName = "#CTL_GenerateSosaAction")
@ActionReference(path = "Menu/Tools/Gedcom")
public final class GenerateSosaAction implements ActionListener {

    Gedcom myGedcom = null;
    private final Preferences modulePreferences = NbPreferences.forModule(SosaNumbers.class);

    @Override
    public void actionPerformed(ActionEvent e) {
        Context context;

        if ((context = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {
            myGedcom = context.getGedcom();
            SelectEntityPanel selectEntityPanel = new SelectEntityPanel(myGedcom, Gedcom.INDI);

            String selectedEntityID = modulePreferences.get("SelectEntityDialog." + myGedcom.getName() + "." + Gedcom.INDI, "");
            if (!selectedEntityID.isEmpty()) {
                Entity selectedEntity = myGedcom.getEntity(selectedEntityID);
                if (selectedEntity != null) {
                    selectEntityPanel.setSelection(selectedEntity);
                }
            }

            String clearChoice = new String(NbBundle.getMessage(GenerateSosaAction.class, "GenerateSosaAction.clear"));
            Object choice = DialogManager.create(
                    NbBundle.getMessage(this.getClass(), "GenerateSosaAction.AskDeCujus"),
                    selectEntityPanel)
                    .setMessageType(DialogManager.QUESTION_MESSAGE)
                    .setOptions(new Object[]{DialogManager.OK_OPTION, DialogManager.CANCEL_OPTION})
                    .setAdditionalOptions(new Object[]{clearChoice})
                    .show();

            if (choice != DialogManager.CANCEL_OPTION) {
                Indi indiDeCujus = null;
                if (choice == DialogManager.OK_OPTION) {
                    indiDeCujus = (Indi) selectEntityPanel.getSelection();
                }
                // else null means clear sosa
                new SosaNumbers().generateSosaNbs(myGedcom, indiDeCujus);
                if (indiDeCujus == null) {
                    modulePreferences.remove("SelectEntityDialog." + myGedcom.getName());
                } else {
                    modulePreferences.put("SelectEntityDialog." + myGedcom.getName(), indiDeCujus.getId());
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(GenerateSosaAction.class, "GenerateSosaAction.done", indiDeCujus.getName()), NotifyDescriptor.INFORMATION_MESSAGE));
                }
            }
        }
    }
}
