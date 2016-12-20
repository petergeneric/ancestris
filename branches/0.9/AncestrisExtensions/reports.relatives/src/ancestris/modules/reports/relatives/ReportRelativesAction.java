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

import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.modules.document.view.FopDocumentView;
import static ancestris.modules.reports.relatives.Bundle.*;
import ancestris.util.swing.SelectEntityDialog;
import genj.fo.Document;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
@ActionID(id = "ancestris.modules.reports.relatives.ReportRelativesAction", category = "Tools")
@ActionRegistration(
        displayName = "#CTL_ReportRelativesAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/View/Reports", name = "ReportRelativesAction", position = 30)
@NbBundle.Messages({
    "tabtitle.short=Relatives",
    "tabtitle={0}: Close Relatives"
})
public final class ReportRelativesAction extends AbstractAncestrisContextAction {

    private final Preferences modulePreferences = NbPreferences.forModule(ReportRelatives.class);

    public ReportRelativesAction() {
        super();
        setImage("ancestris/modules/reports/relatives/relatives.png");
        setText(NbBundle.getMessage(ReportRelativesAction.class, "CTL_ReportRelativesAction"));
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
            Gedcom myGedcom = contextToOpen.getGedcom();

            SelectEntityDialog selectEntityDialog = new SelectEntityDialog(NbBundle.getMessage(this.getClass(), "ReportRelatives.AskDeCujus"), myGedcom, Gedcom.INDI);
            Indi indiDeCujus = (Indi) selectEntityDialog.getEntity();
            if (indiDeCujus != null) {
                Document document = new ReportRelatives().start(indiDeCujus);
                if (document != null) {
                    FopDocumentView window = new FopDocumentView(contextToOpen,  tabtitle_short(),tabtitle(contextToOpen.toString()));
                    window.displayDocument(document, modulePreferences);
                }
            }
        }
    }
}
