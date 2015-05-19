package ancestris.modules.gedcom.gedcomvalidate;

import ancestris.modules.document.view.FopDocumentView;
import ancestris.util.ProgressListener;
import genj.gedcom.Context;
import genj.view.ViewContext;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import spin.Spin;

@ActionID(id = "ancestris.modules.gedcom.gedcomvalidate.GedcomValidateAction",
        category = "Tools")
@ActionRegistration(iconInMenu = true,
        displayName = "#CTL_GedcomValidateAction",
        iconBase = "ancestris/modules/gedcom/gedcomvalidate/GedcomValidateIcon.png")
@ActionReference(path = "Menu/Tools/Gedcom", position = 95)
public final class GedcomValidateAction implements ActionListener {

    private Context context;
    private List<ViewContext> result;

    @Override
    public void actionPerformed(ActionEvent e) {
        Preferences modulePreferences = NbPreferences.forModule(GedcomValidate.class);

        context = Utilities.actionsGlobalContext().lookup(Context.class);
        if (context != null) {
            if (modulePreferences.getInt("maxLife", -1) == -1) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(GedcomValidate.class, "setParameters"), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);

                OptionsDisplayer.getDefault().open("Extensions/GedcomValidateOptions");
            } else {
                Validator validator = (Validator) Spin.off(new GedcomValidate(context.getGedcom()));

                try {
                    ProgressListener.Dispatcher.processStarted(validator);
                    result = validator.start();
                } finally {
                    ProgressListener.Dispatcher.processStopped(validator);
                }

                String title = NbBundle.getMessage(GedcomValidate.class, "name");
                genj.fo.Document doc = new genj.fo.Document(title);
                doc.startSection(title);

                if (result != null) {
                    Collections.sort(result);
                    doc.startTable("genj:csv=true,width=100%");
                    doc.addTableColumn("column-width=10%");
                    doc.addTableColumn("column-width=25%");
                    doc.addTableColumn("column-width=65%");
                    Iterator<ViewContext> iterator = result.listIterator();
                    while (iterator.hasNext()) {
                        ViewContext c = iterator.next();
                        doc.nextTableRow();
                        // doc.addText(c.getEntity().getId());
                        doc.addLink(c.getEntity().getId(), c.getEntity().getAnchor());
                        doc.nextTableCell();
                        doc.addText(c.getEntity().toString(false));
                        doc.nextTableCell();
                        doc.addText(c.getText());
                    }
                    doc.endTable();
                } else {
                    doc.addText(NbBundle.getMessage(GedcomValidate.class, "noissues"));
                }

                FopDocumentView window = new FopDocumentView(
                        context,
                        NbBundle.getMessage(GedcomValidate.class, "name.short"),
                        NbBundle.getMessage(GedcomValidate.class, "name"));

                window.displayDocument(doc, modulePreferences);
            }
        }
    }
}
