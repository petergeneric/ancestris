package ancestris.modules.flashlist;

import ancestris.app.App;
import ancestris.modules.document.view.DocumentViewTopComponent;
import genj.fo.Document;
import genj.gedcom.Context;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public final class FlashListAction implements ActionListener {

    private Context context;
    private DocumentViewTopComponent window = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        window = DocumentViewTopComponent.findInstance();
        Preferences modulePreferences = NbPreferences.forModule(ReportFlashList.class);

        context = App.center.getSelectedContext(true);
        if (context != null) {
            if (modulePreferences.getInt("displayLegendComboBox", 3) == 3) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(FlashListPanel.class, "FlashListPanel.setParameters"), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);

                OptionsDisplayer.getDefault().open("Extensions/FlashList");
            } else {
                Document doc = new ReportFlashList().start(context.getGedcom(), modulePreferences.get("reportFilename", "flash-list"));
                if (doc != null) {
                    window.displayDocument(doc, modulePreferences);
                    window.open();
                    window.requestActive();
                }
            }
        }
    }
}
