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
import genj.report.Report;
import genj.report.ReportPlugin;
import genj.report.ReportView;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public final class WebSiteAction implements ActionListener {

    private final Context context;

    public WebSiteAction(Context context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        Report report = WebSiteExportPlugin.getReport();
//        ReportView view = AncestrisPlugin.lookup(GenjViewInterface.class).getReportView(context);
        ReportView view = ReportPlugin.getReportView(context);
        if (view != null) {
            view.startReport(report, context.getGedcom());
        }
    }
}
