package ancestris.modules.gedcom.gedcomvalidate;

import genj.util.Validator;
import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.modules.document.view.FopDocumentView;
import ancestris.util.ProgressListener;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.view.ViewContext;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import spin.Spin;

@ActionID(id = "ancestris.modules.gedcom.gedcomvalidate.GedcomValidateAction", category = "Tools")
@ActionRegistration(
        displayName = "#CTL_GedcomValidateAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/Tools", name = "GedcomValidateAction", position = 60)
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

        final Context contextToOpen = getContext();
        if (contextToOpen != null) {
            Validator validator = (Validator) Spin.off(new GedcomValidate());

            try {
                ProgressListener.Dispatcher.processStarted(validator);
                result = validator.start(contextToOpen.getGedcom());
            } finally {
                ProgressListener.Dispatcher.processStopped(validator);
            }

            final int size = result != null ? result.size() : 0;
            final ProgressMonitor progressMonitor = new ProgressMonitor(null, NbBundle.getMessage(GedcomValidate.class, "doc.title", size), "", 0, size);
            progressMonitor.setProgress(0);
            Task fullTask = new Task(progressMonitor, size) {
                @Override
                public Void doInBackground() {
                    String title = NbBundle.getMessage(GedcomValidate.class, "name");
                    final genj.fo.Document doc = new genj.fo.Document(title);

                    if (result != null) {
                        doc.nextParagraph("text-align=center, space-before=2cm, space-after=1cm");
                        doc.addText(title + "  (" + result.size() + ")", "font-size=20, font-weight=bold");
                        doc.nextParagraph();

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
                        int p = 0;
                        while (iterator.hasNext() && !progressMonitor.isCanceled()) {
                            p++;
                            progressMonitor.setProgress(p);
                            ViewContext c = iterator.next();
                            if (c != null && !section.equals(c.getCode())) {
                                if (!section.isEmpty()) {
                                    doc.endTable();
                                }
                                section = c.getCode();
                                doc.nextParagraph();
                                doc.addText(" ", "font-size=14");
                                doc.nextParagraph();
                                doc.addText(" ", "font-size=14");
                                doc.nextParagraph();
                                String sectionStr = getSectionName(section);
                                doc.addText(sectionStr, "font-size=14, font-weight=bold, space-before=2cm, space-after=1cm, keep-with-next.within-page=always, text-decoration=underline");
                                doc.addTOCEntry(sectionStr + "  (" + getSectionCount(section) + ")");
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

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            FopDocumentView window = new FopDocumentView(contextToOpen, NbBundle.getMessage(GedcomValidate.class, "name.short"), 
                                    NbBundle.getMessage(GedcomValidate.class, "name"));
                            window.displayDocument(doc, modulePreferences);
                        }
                    });

                    progressMonitor.setProgress(size);
                    return null;
                }
            };
            fullTask.execute();

        }
    }

    private String getSectionName(String code) {
        String[] codeTable = new String[]{
            "00-0", "00-1", "00-2", "00-3", "00-4", "01-1", "01-2", "01-3", "01-4", "01-5", "01-6", "01-7", "01-8",
            "02", "03", "04", "05", "06", "07", "08", "09", "11", "12"
        };
        for (String item : codeTable) {
            if (item.equals(code)) {
                return NbBundle.getMessage(GedcomValidate.class, "section." + item);
            }
        }
        return "";
    }

    private String getSectionCount(String section) {
        int count = 0;
        for (ViewContext c : result) {
            count += c.getCode().equals(section) ? 1 : 0;
        }
        return "" + count;
    }

    private class Task extends SwingWorker<Void, Void> {

        private ProgressMonitor pm;
        private int maxp = 0;

        public Task(ProgressMonitor progressMonitor, int maxProgress) {
            pm = progressMonitor;
            maxp = maxProgress;
        }

        @Override
        public Void doInBackground() {
            return null;
        }

        @Override
        public void done() {
            System.out.println("ancestris.modules.gedcom.gedcomvalidate.GedcomValidateAction.Task.done() - DEBUG - DONE");
            if (pm != null) {
                pm.setProgress(maxp);
            }
        }
    }

}
