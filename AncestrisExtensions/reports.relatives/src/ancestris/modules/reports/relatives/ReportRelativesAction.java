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
package ancestris.modules.reports.relatives;

import ancestris.modules.document.view.FopDocumentView;
import ancestris.util.swing.SelectEntityDialog;
import genj.fo.Document;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

public final class ReportRelativesAction implements ActionListener {

    private final Preferences modulePreferences = NbPreferences.forModule(ReportRelatives.class);

    @Override
    public void actionPerformed(ActionEvent e) {
        Context context;

        if ((context = Utilities.actionsGlobalContext().lookup(Context.class)) != null) {
            Gedcom myGedcom = context.getGedcom();

            SelectEntityDialog selectEntityDialog = new SelectEntityDialog(NbBundle.getMessage(this.getClass(), "ReportRelatives.AskDeCujus"), myGedcom, Gedcom.INDI);
            Indi indiDeCujus = (Indi) selectEntityDialog.getEntity();
            if (indiDeCujus != null) {
                Document document = new ReportRelatives().start(indiDeCujus);
                if (document != null) {
                    FopDocumentView window = new FopDocumentView(context, "relatives");//XXX: bundle
                    window.displayDocument(document, modulePreferences);
                }
            }
        }
    }
}
