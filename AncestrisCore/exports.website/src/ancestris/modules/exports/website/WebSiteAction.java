/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2012 Ancestris
 *
 * Author: Daniel Andre (daniel@ancestris.org).
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.exports.website;

import ancestris.core.actions.AbstractAncestrisContextAction;
import genj.gedcom.Context;
import genj.report.Report;
import genj.report.ReportPlugin;
import genj.report.ReportView;
import java.awt.event.ActionEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(id = "ancestris.modules.exports.website.WebSiteAction", category = "Tools")
@ActionRegistration(
        displayName = "#CTL_WebSiteAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/Tools/Multimedia", name = "WebSiteOpenAction", position = 200)
public final class WebSiteAction extends AbstractAncestrisContextAction {

    public WebSiteAction() {
        super();
        setImage("ancestris/modules/exports/website/icone_multimedia_16.png");
        setText(NbBundle.getMessage(WebSiteAction.class, "CTL_WebSiteAction"));
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
            Report report = WebSiteExportPlugin.getReport();
//        ReportView view = AncestrisPlugin.lookup(GenjViewInterface.class).getReportView(context);
            ReportView view = ReportPlugin.getReportView(new Context(contextToOpen));
            if (view != null) {
                view.startReport(report, contextToOpen.getGedcom(), contextToOpen.getGedcom());
            }
        }
    }
}
