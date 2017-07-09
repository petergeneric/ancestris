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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
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
//            if (modulePreferences.getInt("maxLife", -1) == -1) {
//                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(GedcomValidate.class, "setParameters"), NotifyDescriptor.INFORMATION_MESSAGE);
//                DialogDisplayer.getDefault().notify(nd);
//                OptionsDisplayer.getDefault().open("Extensions/GedcomValidateOptions");
//            } 
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
            doc.nextParagraph();
            doc.addText(" ", "font-size=14, space-after=1cm");
            doc.nextParagraph();
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
                Collections.sort(result, new Comparator() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        ViewContext vc1 = (ViewContext) o1;
                        ViewContext vc2 = (ViewContext) o2;
                        String str1 = vc1.getCode() + vc1.getEntity().getId();
                        String str2 = vc2.getCode() + vc2.getEntity().getId();
                        return str1.compareTo(str2);
                    }
                });
                
                String section = "";
                Iterator<ViewContext> iterator = result.listIterator();
                while (iterator.hasNext()) {
                    ViewContext c = iterator.next();
                    if (!c.getCode().equals(section)) {
                        if (!section.isEmpty()) {
                            doc.endTable();
                        }
                        section = c.getCode();
                        doc.nextParagraph();
                        doc.addText(" ", "font-size=14");
                        doc.nextParagraph();
                        doc.addText(" ", "font-size=14");
                        doc.nextParagraph();
                        doc.addText(getSectionName(section), "font-size=14, font-weight=bold, space-before=2cm, space-after=1cm, keep-with-next.within-page=always, text-decoration=underline");
                        doc.nextParagraph();
                        doc.addText(" ", "font-size=14");
                        doc.nextParagraph();
                        doc.startTable("genj:csv=true,width=100%");
                        doc.addTableColumn("column-width=10%");
                        doc.addTableColumn("column-width=25%");
                        doc.addTableColumn("column-width=65%");
                    }
                    doc.nextTableRow();
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

    private String getSectionName(String code) {
        String[] codeTable = new String[] {
            "01-1", "01-2", "01-3", "01-4", "01-5", "01-6", "01-7", "01-8", 
            "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13"
        };
        for (String item : codeTable) {
            if (item.equals(code)) {
                return NbBundle.getMessage(GedcomValidate.class, "section."+item);
            }
        }
        return "";
    }
}
