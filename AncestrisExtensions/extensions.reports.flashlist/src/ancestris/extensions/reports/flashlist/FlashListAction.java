package ancestris.extensions.reports.flashlist;

import ancestris.app.App;
import ancestris.extensions.reports.view.ReportViewTopComponent;
import genj.fo.Document;
import genj.gedcom.Context;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

public final class FlashListAction implements ActionListener {

    private Context context;
    private ReportViewTopComponent window = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        window = ReportViewTopComponent.findInstance();
        Preferences modulePreferences = NbPreferences.forModule(ReportFlashList.class);

        context = App.center.getSelectedContext(true);
        if (context != null) {
            Document doc = new ReportFlashList().start(context.getGedcom(), modulePreferences.get("reportFilename", "flash-list"));
            window.displayDocument(doc, modulePreferences);
            window.open();
            window.requestActive();
        }
    }
}
