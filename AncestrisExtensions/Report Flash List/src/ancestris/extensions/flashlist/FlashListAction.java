package ancestris.extensions.flashlist;

import ancestris.app.App;
import ancestris.extensions.reportsview.ReportViewTopComponent;
import genj.fo.Document;
import genj.gedcom.Context;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class FlashListAction implements ActionListener {

    private Context context;
    private ReportViewTopComponent window = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        window = ReportViewTopComponent.findInstance();

        context = App.center.getSelectedContext(true);
        if (context != null) {
            Document doc = new ReportFlashList().start(context.getGedcom());
            window.openAtTabPosition(0);
            window.requestActive();
            window.getReportViewScrollPane();
            window.test (doc);
        }
    }
}
