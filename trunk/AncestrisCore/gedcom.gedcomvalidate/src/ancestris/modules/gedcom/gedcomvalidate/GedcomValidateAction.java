package ancestris.modules.gedcom.gedcomvalidate;

import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.modules.document.view.FopDocumentView;
import ancestris.util.ProgressListener;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.Validator;
import genj.view.ViewContext;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import spin.Spin;

@ActionID(id = "ancestris.modules.gedcom.gedcomvalidate.GedcomValidateAction", category = "Tools")
@ActionRegistration(
        displayName = "#CTL_GedcomValidateAction",
        iconInMenu = true,
        lazy = false)
@ActionReferences({
    @ActionReference(path = "Toolbars/Misc", position = 95),
    @ActionReference(path = "Menu/Tools", name = "GedcomValidateAction", position = 60),
})
public final class GedcomValidateAction extends AbstractAncestrisContextAction {

    private List<ViewContext> result;

    public GedcomValidateAction() {
        super();
        //setImage("ancestris/modules/gedcom/gedcomvalidate/GedcomValidateIcon.png");
        putValue("iconBase", "ancestris/modules/gedcom/gedcomvalidate/GedcomValidateIcon.png"); // FL: use this instead to have both icon in 16x16 and 24x24 size for toolbar
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

            // Dialog asking if extract necessary in > 5000 lines to display
            final int size = result != null ? result.size() : 0;
            Object rc = null;
            JButton buttonFull = new JButton(NbBundle.getMessage(GedcomValidateAction.class, "mode.displayFullList"));
            JButton buttonExtract = new JButton(NbBundle.getMessage(GedcomValidateAction.class, "mode.displayExtractOnly"));
            if (size > 1000) {
                Object[] buttons;
                buttons = new Object[]{ buttonFull, buttonExtract };
                rc = DialogManager.create(NbBundle.getMessage(GedcomValidateAction.class, "CTL_GedcomValidateAction").replace("&",""),
                        NbBundle.getMessage(GedcomValidateAction.class, "extractorfull")
                        ).setMessageType(DialogManager.INFORMATION_MESSAGE).setOptions(buttons).show();
            }

            final boolean extract = (rc == buttonExtract);
            final ProgressMonitor progressMonitor = new ProgressMonitor(null, NbBundle.getMessage(GedcomValidate.class, "doc.title", size), "", 0, size);
            final String title = contextToOpen.getGedcom().getDisplayName() + " - " + NbBundle.getMessage(GedcomValidate.class, "OpenIDE-Module-Name");
            progressMonitor.setProgress(0);
            Task fullTask = new Task(progressMonitor, size) {
                @Override
                public Void doInBackground() {
                    final genj.fo.Document doc = new genj.fo.Document(title);
                    String goToToc = "  \u2191";
                    String tocAnchor = "toc";

                    if (result != null) {
                        doc.nextParagraph("text-align=center, space-before=2cm, space-after=1cm");
                        doc.addText(title + "  (" + result.size() + ")", "font-size=20, font-weight=bold");
                        doc.nextParagraph();

                        Collections.sort(result, (Object o1, Object o2) -> {
                            ViewContext vc1 = (ViewContext) o1;
                            ViewContext vc2 = (ViewContext) o2;
                            String str1 = vc1.getCode();
                            String str2 = vc2.getCode();
                            if (str1.equals(str2)) {
                                if (vc1.getEntity().getTag().equals(vc2.getEntity().getTag())) {
                                    return vc1.getEntity().getComparator().compare(vc1.getEntity(), vc2.getEntity());
                                } else {
                                    return order(vc1.getEntity().getTag()).compareTo(order(vc2.getEntity().getTag()));
                                }
                            } else {
                                return str1.compareTo(str2);
                            }
                        });

                        String section = "";
                        Iterator<ViewContext> iterator = result.listIterator();
                        int p = 0;
                        int counterPerSection = 0;
                        Set<String> extractedMsgs = new HashSet<>();
                        boolean truncated = false;
                        while (iterator.hasNext() && !progressMonitor.isCanceled()) {
                            p++;
                            progressMonitor.setProgress(p);
                            ViewContext c = iterator.next();
                            if (c != null && !section.equals(c.getCode())) {
                                if (!section.isEmpty()) {
                                    doc.endTable();
                                }
                                section = c.getCode();
                                counterPerSection = 0;
                                extractedMsgs.clear();
                                truncated = false;
                                doc.nextParagraph();
                                doc.addText(" ", "font-size=14");
                                doc.nextParagraph();
                                doc.addText(" ", "font-size=14");
                                doc.nextParagraph();
                                String sectionStr = getSectionName(section) + "  (" + getSectionCount(section) + ")";
                                doc.addText(sectionStr, "font-size=14, font-weight=bold, space-before=2cm, space-after=1cm, keep-with-next.within-page=always, text-decoration=underline");
                                doc.addLink(goToToc, tocAnchor);
                                doc.addTOCEntry(sectionStr);
                                doc.nextParagraph();
                                doc.addText(" ", "font-size=14");
                                doc.nextParagraph();
                                doc.startTable("genj:csv=true,width=100%");
                                doc.addTableColumn("column-width=10%");
                                doc.addTableColumn("column-width=25%");
                                doc.addTableColumn("column-width=65%");
                            }
                            // Display line for each correction
                            String msg = c.getText();
                            counterPerSection++;


                            // In extract mode, do not display line above a certain limit: display only different tags after the limit of first 10
                            if (extract && counterPerSection > 150 && (extractedMsgs.contains(msg) || counterPerSection > 500)) {
                                if (!truncated) {
                                    doc.nextTableRow();
                                    doc.addText("...");
                                    truncated = true;
                                }
                                continue;  
                            }
                            extractedMsgs.add(msg);
                            truncated = false;
                            
                            // col1
                            doc.nextTableRow();
                            doc.addLink(c.getEntity().getId(), c.getEntity().getAnchor());
                            doc.nextTableCell();
                            
                            // col2
                            String entityString = c.getEntity().toString(false);
                            if (entityString.length() > 100) {
                                entityString = entityString.substring(0, 75);
                            }
                            doc.addText(entityString);
                            doc.nextTableCell();
                            
                            // col3
                            doc.addText(c.getText());
                        }
                        doc.endTable();
                        doc.nextParagraph(); doc.addText("   ");
                        doc.nextParagraph(); doc.addText("   ");
                        doc.nextParagraph(); doc.addText("   ");
                        doc.addLink(goToToc, tocAnchor);
                    } else {
                        doc.addText(NbBundle.getMessage(GedcomValidate.class, "noissues"));
                    }

                    SwingUtilities.invokeLater(() -> {
                        FopDocumentView window = new FopDocumentView(contextToOpen, contextToOpen.getGedcom().getDisplayName(),
                                NbBundle.getMessage(GedcomValidate.class, "OpenIDE-Module-Name"));
                        window.displayDocument(doc, modulePreferences);
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
            "00-0", "00-1", "00-2", "00-3", "00-4", "00-5", "01-1", "01-2", "01-3", "01-4", "01-5", "01-6", "01-7", "01-8",
            "02", "03", "04", "05", "06", "07", "08", "09", "11", "12", "15"
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

    private Integer order(String entityTag) {
        int order = 9;
        
        for (String type : Gedcom.ENTITIES) {
            if (type.equals(entityTag)) {
                return order;
            }
            order++;
        }
        return order;
    }

    private class Task extends SwingWorker<Void, Void> {

        private final ProgressMonitor pm;
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
            if (pm != null) {
                pm.setProgress(maxp);
            }
        }
    }

}
