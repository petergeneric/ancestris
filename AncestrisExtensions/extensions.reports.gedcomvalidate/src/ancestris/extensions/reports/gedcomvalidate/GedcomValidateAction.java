package ancestris.extensions.reports.gedcomvalidate;

import ancestris.app.App;
import ancestris.extensions.reports.view.ReportViewTopComponent;
import genj.gedcom.Context;
import genj.view.ViewContext;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public final class GedcomValidateAction implements ActionListener {

    private Context context;
    private ReportViewTopComponent window = null;
    private List<ViewContext> result;

    @Override
    public void actionPerformed(ActionEvent e) {
        window = ReportViewTopComponent.findInstance();
        Preferences modulePreferences = NbPreferences.forModule(GedcomValidate.class);

        context = App.center.getSelectedContext(true);
        if (context != null) {
            if (modulePreferences.getInt("maxLife", -1) == -1) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(GedcomValidate.class, "setParameters"), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);

                OptionsDisplayer.getDefault().open("Extensions/GedcomValidateOptions");
            } else {
                result = new GedcomValidate().start(context.getGedcom());

                String title = "Report";
                genj.fo.Document doc = new genj.fo.Document(title);
                doc.startSection(title);
                Iterator iterator = result.listIterator();
                while (iterator.hasNext()) {
                    Context c = (Context) iterator.next();

                    doc.addText(c.getEntity() + ":" + ((ViewContext) c).getText());
                    doc.nextParagraph();
                }
                if (doc != null) {
                    window.displayDocument(doc, modulePreferences);
                    window.open();
                    window.requestActive();
                }
            }
        }
    }
}
