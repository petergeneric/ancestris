package ancestris.modules.gedcom.gedcomvalidate;

import genj.util.Validator;
import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.modules.document.view.FopDocumentView;
import ancestris.util.ProgressListener;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.view.ViewContext;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;
import spin.Spin;

@ActionID(id = "ancestris.modules.gedcom.gedcomvalidate.GedcomValidateAction", category = "Tools")
@ActionRegistration(
        displayName = "#CTL_GedcomValidateAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/Tools/Gedcom", name = "GedcomValidateAction", position = 200)
public final class GedcomValidateAction extends AbstractAncestrisContextAction {

    private List<ViewContext> result;

    public GedcomValidateAction() {
        super();
        setImage("ancestris/modules/gedcom/gedcomvalidate/GedcomValidateIcon.png");
        setText(NbBundle.getMessage(GedcomValidateAction.class, "CTL_GedcomValidateAction"));
    }
    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }


    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        final Preferences modulePreferences = NbPreferences.forModule(Gedcom.class);

        Context contextToOpen = getContext();
        if (contextToOpen != null) {
            if (modulePreferences.getInt("maxLife", -1) == -1) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(GedcomValidate.class, "setParameters"), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
                OptionsDisplayer.getDefault().open("Extensions/GedcomValidateOptions");
            } else {
                Validator validator = (Validator) Spin.off(new GedcomValidate());

                try {
                    ProgressListener.Dispatcher.processStarted(validator);
                    result = validator.start(contextToOpen.getGedcom());
                } finally {
                    ProgressListener.Dispatcher.processStopped(validator);
                }

                String title = NbBundle.getMessage(GedcomValidate.class, "name");
                final genj.fo.Document doc = new genj.fo.Document(title);
                doc.startSection(title);

                final JOptionPane optionPane = new JOptionPane(NbBundle.getMessage(GedcomValidate.class, "doc.message", result == null ? "0" : result.size()), JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
                final JDialog dialog = new JDialog(WindowManager.getDefault().getMainWindow(), NbBundle.getMessage(GedcomValidate.class, "doc.title"), false);
                if (result != null && result.size() > 2000) {
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    dialog.setLocation((screenSize.width - optionPane.getPreferredSize().width) / 2, (screenSize.height - optionPane.getPreferredSize().height) / 2);
                    dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
                    dialog.setContentPane(optionPane);
                    dialog.pack();
                    dialog.setVisible(true);
                }

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

                final FopDocumentView window = new FopDocumentView(
                        contextToOpen,
                        NbBundle.getMessage(GedcomValidate.class, "name.short"),
                        NbBundle.getMessage(GedcomValidate.class, "name"));

                
                WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                    @Override
                    public void run() {
                        window.displayDocument(doc, modulePreferences);
                        dialog.dispose();
                    }
                });
            }
        }
    }
}
