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

import genj.gedcom.Context;
import genj.gedcom.Property;
import genj.report.Report;
import genj.report.ReportPlugin;
import genj.report.ReportView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;


@ActionID(category = "Tools",
id = "ancestris.modules.exports.website.WebSiteAction")
@ActionRegistration(
        displayName = "#CTL_WebSiteAction"
        ,iconBase="ancestris/modules/exports/website/icone_multimedia_16.png"
        )
@ActionReferences({
    @ActionReference(path = "Menu/Tools/Multimedia", position = -90)
})

public final class WebSiteAction implements ActionListener {

    private final Property context;

    public WebSiteAction(Property context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        Report report = WebSiteExportPlugin.getReport();
//        ReportView view = AncestrisPlugin.lookup(GenjViewInterface.class).getReportView(context);
        ReportView view = ReportPlugin.getReportView(new Context(context));
        if (view != null) {
            view.startReport(report, context.getGedcom());
        }
    }
}
