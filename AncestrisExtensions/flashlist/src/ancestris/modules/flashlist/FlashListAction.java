package ancestris.modules.flashlist;

import ancestris.modules.document.view.FopDocumentView;
import static ancestris.modules.flashlist.Bundle.*;
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
import org.openide.util.Utilities;

@NbBundle.Messages("title=Flash Lists")
public final class FlashListAction implements ActionListener {

    private Context context;

    @Override
    public void actionPerformed(ActionEvent e) {
        Preferences modulePreferences = NbPreferences.forModule(ReportFlashList.class);

        context = Utilities.actionsGlobalContext().lookup(Context.class);
        if (context != null) {
            if (modulePreferences.getInt("displayLegendComboBox", 3) == 3) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(FlashListPanel.class, "FlashListPanel.setParameters"), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);

                OptionsDisplayer.getDefault().open("Extensions/FlashList");
            } else {
                Document doc = new ReportFlashList().start(context.getGedcom(), modulePreferences.get("reportFilename", "flash-list"));
                if (doc != null) {
                    FopDocumentView window = new FopDocumentView(context, title());
                    window.displayDocument(doc, modulePreferences);
                }
            }
        }
    }
}
