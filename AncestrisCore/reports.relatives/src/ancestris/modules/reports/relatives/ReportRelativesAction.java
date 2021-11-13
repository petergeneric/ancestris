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
import ancestris.modules.document.view.WidgetDocumentView;
import static ancestris.modules.reports.relatives.Bundle.tabtitle;
import static ancestris.modules.reports.relatives.Bundle.tabtitle_short;
import ancestris.reports.relatives.ReportRelatives;
import ancestris.util.swing.DialogManager;
import ancestris.util.swing.SelectEntityPanel;
import genj.common.ContextListWidget;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.*;
@ActionID(id = "ancestris.modules.reports.relatives.ReportRelativesAction", category = "Tools")
@ActionRegistration(
        displayName = "#CTL_ReportRelativesAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/Tools/Reports", name = "ReportRelativesAction", position = 300)
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

            Entity entity = contextToOpen.getEntity();
            Indi indiDeCujus = null;
            if (entity instanceof Indi) {
                indiDeCujus = (Indi) entity;
            } else {
                // Selection box
                SelectEntityPanel select = new SelectEntityPanel(myGedcom, Gedcom.INDI, NbBundle.getMessage(this.getClass(), "ReportRelatives.AskDeCujus"),
                        contextToOpen.getEntity());
                if (DialogManager.OK_OPTION != DialogManager.create(NbBundle.getMessage(this.getClass(), "CTL_ReportRelativesAction"), select)
                        .setMessageType(DialogManager.QUESTION_MESSAGE).setOptionType(DialogManager.OK_CANCEL_OPTION).setDialogId("report.ReportRelatives").show()) {
                    return;
                }
                indiDeCujus = (Indi) select.getSelection();
            }

            if (indiDeCujus != null) {
                Object object = new ReportRelatives().start(indiDeCujus);
                object = new ContextListWidget((List<Context>) object);
                new WidgetDocumentView(new Context(myGedcom), tabtitle_short(),tabtitle(contextToOpen.toString()), ((JComponent) object));
            }
        }
    }
}
