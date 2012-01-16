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

import ancestris.modules.gedcom.utlilities.SelectEntityDialog;
import ancestris.app.App;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

public final class GenerateSosaAction implements ActionListener {

    Gedcom myGedcom = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        Context context;

        if ((context = App.center.getSelectedContext(true)) != null) {
            myGedcom = context.getGedcom();

            SelectEntityDialog selectEntityDialog = new SelectEntityDialog(NbBundle.getMessage(this.getClass(), "AskDeCujus"), myGedcom, Gedcom.INDI);
            Indi indiDeCujus = (Indi) selectEntityDialog.getEntity();
            if (indiDeCujus != null) {
                new SosaNumbers().generateSosaNbs(myGedcom, indiDeCujus);
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Hello...", NotifyDescriptor.INFORMATION_MESSAGE));
            }
        }
    }
}
