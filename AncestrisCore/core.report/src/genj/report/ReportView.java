/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.report;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.gedcom.GedcomDirectory;
import ancestris.modules.document.view.DocumentViewTopComponent;
import ancestris.modules.document.view.HyperLinkTextDocumentView;
import ancestris.modules.document.view.WidgetDocumentView;
import ancestris.swing.ToolBar;
import ancestris.util.swing.DialogManager;
import ancestris.util.swing.FileChooserBuilder;
import genj.common.ContextListWidget;
import genj.fo.Format;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.io.FileAssociation;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ImageIcon;
import genj.view.View;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import spin.Spin;

/**
 * Component for running reports on genealogic data
 */
//XXX: needs to be refactored in depth
public class ReportView extends View {

    /* package */
    static final Logger LOG = Logger.getLogger("ancestris.report");
    /**
     * statics
     */
    private final static ImageIcon imgStart = new ImageIcon(ReportView.class, "Start"),
            imgStop = new ImageIcon(ReportView.class, "Stop");
    /**
     * components to show report info
     */
    private HyperLinkTextDocumentView output;
    private ActionStart actionStart = new ActionStart();
    private ActionStop actionStop = new ActionStop();
    /**
     * registry for settings
     */
    private final static Registry REGISTRY = Registry.get(ReportView.class);
    /**
     * resources
     */
    /* package */
    static final Resources RESOURCES = Resources.get(ReportView.class);
    private ReportSelector selector;

    /**
     * Constructor
     */
    public ReportView() {
        selector = new ReportSelector();
        setLayout(new BorderLayout());
        add(selector, BorderLayout.CENTER);
    }

    /**
     * @see javax.swing.JComponent#removeNotify()
     */
    @Override
    public void removeNotify() {
        // continue
        super.removeNotify();
        // save report options
        ReportLoader.getInstance().saveOptions();
    }

    /**
     * Get selected gedcom from global selection lookup. no need to listen for
     * context change
     *
     * @return Gedcom for selected property
     */
    private Gedcom getSelectedGedcom() {
        Context selected = Utilities.actionsGlobalContext().lookup(Context.class);
        return selected == null ? null : selected.getGedcom();
    }

    /**
     * start a report
     */
//    public void startReport(final Report report, Object context) {
//        startReport(report, context, getSelectedGedcom());
//    }
    public void startReport(final Report report, Object context, Gedcom gedcom) {

        if (!actionStart.isEnabled()) {
            return;
        }
//        Gedcom gedcom = getSelectedGedcom();

        // create a new tab for this run
        output = new HyperLinkTextDocumentView(
                new Context(gedcom),
                report.getShortName(),
                NbBundle.getMessage(DocumentViewTopComponent.class, "HINT_DocumentResult", gedcom.getDisplayName(), report.getName()));

        if (report.getStartMethod(context) == null) {

            // Report cannot be run on the whole gedcom, so try to see if report accepts current selected entity type as context and default to it
            boolean found = false;
            List<Context> gedcontexts = GedcomDirectory.getDefault().getContexts();
            for (Context ctx : gedcontexts) {
                if (ctx.getGedcom() == gedcom && ctx.getEntity() != null) {
                    Entity sample = ctx.getEntity();
                    String tag = sample.getTag();
                    String txt = report.accepts(sample);
                    if (txt != null) {
                        context = report.getEntityFromUser(txt, gedcom, tag, sample);
                        if (context == null) {
                            return;
                        }
                        found = true;
                    }
                }
            }

            // Try any first entity
            if (!found) {
                for (int i = 0; i < Gedcom.ENTITIES.length; i++) {
                    String tag = Gedcom.ENTITIES[i];
                    Entity sample = gedcom.getFirstEntity(tag);
                    if (sample != null && report.accepts(sample) != null) {

                        // give the report a chance to name our dialog
                        String txt = report.accepts(sample);
                        if (txt == null) {
                            Gedcom.getName(tag);
                        }

                        // ask user for context now
                        context = report.getEntityFromUser(txt, gedcom, tag, null);
                        if (context == null) {
                            return;
                        }
                        break;
                    }
                }
            }
        }

        // check if appropriate
        if (context == null || report.accepts(context) == null) {
            DialogManager.createError(report.getName(), RESOURCES.getString("report.noaccept")).show();
            return;
        }

        // remember
        REGISTRY.put("lastreport", report.getClass().getName());

        // set report ui context
        report.setOwner(this);

        // clear the current output and show coming
        output.clear();
//XXX:    output.show();

        // set running
        actionStart.setEnabled(false);
        actionStop.setEnabled(true);

        // kick it off
        new Thread(new Runner(gedcom, context, report, (Runner.Callback) Spin.over(new RunnerCallback()))).start();

    }

    /**
     * callback for runner
     */
    private class RunnerCallback implements Runner.Callback {

        @Override
        public void handleOutput(Report report, String s) {

            output.add(s);
        }

        @Override
        public void handleResult(Report report, Object result) {

            LOG.log(Level.FINE, "Result of report {0} = {1}", new Object[]{report.getName(), result});

            // let report happend again
            actionStart.setEnabled(getSelectedGedcom() != null);
            actionStop.setEnabled(false);

            // handle result
            showResult(result);
        }
    }

//    
//    
//    
//    /**
//     * start a report
//     * XXX: will be moved to Report class
//     */
//    public static void runReport(final Report report, Object context) {
//        Gedcom gedcom=null;
//        if (context instanceof Gedcom)
//            gedcom = (Gedcom)context;
//        if (context instanceof Property){
//            gedcom = ((Property)context).getGedcom();
//        }
//        if (gedcom == null)
//            return;
//        // create a new tab for this run
//        final HyperLinkTextDocumentView 
//        output = new HyperLinkTextDocumentView(
//                new Context(gedcom),
//                report.getShortName(),
//                gedcom.getName() + ": " + report.getName());
//
//        if (report.getStartMethod(context) == null) {
//            for (int i = 0; i < Gedcom.ENTITIES.length; i++) {
//                String tag = Gedcom.ENTITIES[i];
//                Entity sample = gedcom.getFirstEntity(tag);
//                if (sample != null && report.accepts(sample) != null) {
//
//                    // give the report a chance to name our dialog
//                    String txt = report.accepts(sample.getClass());
//                    if (txt == null) {
//                        Gedcom.getName(tag);
//                    }
//
//                    // ask user for context now
//                    context = report.getEntityFromUser(txt, gedcom, tag);
//                    if (context == null) {
//                        return;
//                    }
//                    break;
//                }
//            }
//        }
//
//        // check if appropriate
//        if (context == null || report.accepts(context) == null) {
//            DialogHelper.openDialog(report.getName(), DialogHelper.ERROR_MESSAGE, RESOURCES.getString("report.noaccept"), AbstractAncestrisAction.okOnly(), null);
//            return;
//        }
//
//        // set report ui context
//        report.setOwner(null);
//
//        // clear the current output and show coming
//        output.clear();
//        // kick it off
//        Runner.Callback cb = new Runner.Callback(){
//
//            @Override
//            public void handleOutput(Report report, String s) {
//                output.add(s);
//            }
//
//            @Override
//            public void handleResult(Report report, Object result) {
//                showResult(result);
//            }
//        };
//        new Thread(new Runner(gedcom, context, report, (Runner.Callback) Spin.over(cb))).start();
//
//    }
//
    /**
     * Start a report after selection
     */
    public void startReport() {
        // minimum we can work on?
        Gedcom gedcom = getSelectedGedcom();
        if (gedcom == null) {
            return;
        }
        Report report = selector.getReport();
        if (report == null) {
            return;
        }
        startReport(report, gedcom, gedcom);
    }

    /**
     * stop any running report
     */
    public void stopReport() {
        // TODO: there's no way to stop a running java report atm
    }

//    @Override
//    public void setContext(Context context) {
//        Gedcom gedcom = context.getGedcom();
//        // enable if none running and data available
//        actionStart.setEnabled(!actionStop.isEnabled() && gedcom != null);
//    }
    /**
     * show result of a report run
     */
    /* package */
    void showResult(Object object) {

        // none?
        if (object == null) {
            if (output.isEmpty()) {
                output.add(NbBundle.getMessage(getClass(), "msg.noresult"));
            }
            return;
        }

        // Exception?
        if (object instanceof InterruptedException) {
            output.add(NbBundle.getMessage(getClass(), "msg.cancelled"));
            return;
        }

        if (object instanceof Throwable) {
            CharArrayWriter buf = new CharArrayWriter(256);
            ((Throwable) object).printStackTrace(new PrintWriter(buf));
            output.add(NbBundle.getMessage(getClass(), "msg.exception", buf));

            LOG.log(Level.WARNING, "Exception caught ", (Throwable) object);
            return;
        }

        // remember title for next document view creation
        String tabName = output.getName();
        String tabToolTip = output.getToolTipText();
        try {
            // File?
            if (object instanceof File) {
                File file = (File) object;
                // FIXME: if filename ends with htm, displays in DocumentView, 
                // if ends with html use external browser. because internal broxser doesn't display 
                // css correctly
                //            if (file.getName().endsWith(".htm") || file.getName().endsWith(".html")) {
                if (file.getName().endsWith(".htm")) {
                    try {
                        object = file.toURI().toURL();
                    } catch (Throwable t) {
                        // can't happen
                    }
                } else {
                    try {
                        FileAssociation.getDefault().execute(file.getAbsolutePath());
                    } catch (Throwable t) {
                        Logger.getLogger("ancestris.report").log(Level.INFO, "cannot open " + file, t);
                        output.add(NbBundle.getMessage(getClass(), "msg.cannotopenfile", file));
                    }
                    return;
                }
            }

            // URL?
            if (object instanceof URL) {
                try {
                    output.setPage((URL) object);
                } catch (IOException e) {
                    output.add(NbBundle.getMessage(getClass(), "msg.cannotopenurl", object, e.getMessage()));
                }
                //XXX:      output.show();
                return;
            }

            // context list?
            if (object instanceof List<?>) {
                try {
                    object = new ContextListWidget((List<Context>) object);
                } catch (Throwable t) {
                }
            }

            // component?
            if (object instanceof JComponent) {
                new WidgetDocumentView(new Context(getSelectedGedcom()), tabName, tabToolTip, ((JComponent) object));
                
                return;
            }

            // document
            if (object instanceof genj.fo.Document) {

                genj.fo.Document doc = (genj.fo.Document) object;

                Format[] formats = Format.getFormats();
                Map<String, String> fmts = new HashMap<>();   // description, extension
                for (int i = 0; i < formats.length; i++) {
                    Format format = formats[i];
                    fmts.put(format.getFormat(), format.getFileExtension());
                }

                FileChooserBuilder fcb = new FileChooserBuilder(genj.fo.Document.class)
                        .setFilesOnly(true)
                        .setDefaultBadgeProvider()
                        .setTitle(NbBundle.getMessage(getClass(), "Fo_Document", doc.getTitle()))
                        .setApproveText(NbBundle.getMessage(getClass(), "Fo_OK_Select"))
                        .setDefaultExtension(formats[0].getFileExtension())
                        .setFileFilters(fmts)
                        .setAcceptAllFileFilterUsed(false)
                        .setDefaultDirAsReportDirectory()
                        .setFileHiding(true);
                
                File file = fcb.showSaveDialog();
                if (file == null) {
                    showResult(null);
                    return;
                }
                
                Format formatter = Format.getFormatFromExtension(fcb.getExtension(file.getName()));
                
//                Registry foRegistry = Registry.get(getClass());
//
//                FormatOptionsWidget options = new FormatOptionsWidget(doc, foRegistry);
//
//                DialogManager dialog = DialogManager.create(NbBundle.getMessage(getClass(), "Fo_Document", doc.getTitle()), options).
//                        setOptionType(DialogManager.OK_CANCEL_OPTION).
//                        setDialogId("report.optionsfromuser");
//                options.connect(dialog);
//                Object rc = dialog.show();
//
//                Format formatter = options.getFormat();
//                File file = options.getFile();
//                if (rc != DialogManager.OK_OPTION || formatter.getFileExtension() == null || file == null) {
//                    showResult(null);
//                    return;
//                }
//
//                // store options
//                options.remember(foRegistry);

                // format and write
                try {
                    file.getParentFile().mkdirs();
                    formatter.format(doc, file);
                } catch (Throwable t) {
                    LOG.log(Level.WARNING, "formatting " + doc + " failed", t);
                    output.add(NbBundle.getMessage(getClass(), "msg.formatting", doc));
                    //XXX: show a dialog to user if file creation failed
                    return;
                }

                // go back to document's file
                showResult(file);

                return;
            }
        } finally {
            if (output.isEmpty()) {
                output.close();
            }
        }

        // unknown
        output.add(NbBundle.getMessage(getClass(), "msg.unknownresult", object));
    }

    /**
     * @see genj.view.ToolBarSupport#populate(javax.swing.JToolBar)
     */
    public void populate(ToolBar toolbar) {

        toolbar.add(actionStart);

        // TODO stopping report doesn't really work anyways
        toolbar.add(actionStop);
        toolbar.add(selector.getActionGroup());

        // done
    }

    /**
     * Action: STOP
     */
    private class ActionStop extends AbstractAncestrisAction {

        protected ActionStop() {
            setImage(imgStop);
            setTip(RESOURCES.getString("report.stop.tip"));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            stopReport();
        }
    } // ActionStop

    /**
     * Action: START
     */
    // FIXME: we must enable this action only if there is a valid gedcom selected in lookup
    private class ActionStart extends AbstractAncestrisAction {

        /**
         * constructor
         */
        protected ActionStart() {
            // show
            setImage(imgStart);
            setTip(RESOURCES.getString("report.start.tip"));
        }

        /**
         * execute
         */
        public void actionPerformed(ActionEvent event) {
            startReport();
        }
    } // ActionStart
//XXX: ActionSave is now in AbstractDocumentViewer. We must recode ContextWidget save
//  /**
//   * Action: SAVE
//   */
//  private class ActionSave extends AbstractAncestrisAction {
//    protected ActionSave() {
//      setImage(Images.imgSave);
//      setTip(RESOURCES.getString("report.save.tip"));
//    }
//
//        @Override
//    public void actionPerformed(ActionEvent event) {
//      
//      // user looking at a context-list?
////      if (result.isVisible() && result.getViewport().getView() instanceof ContextListWidget) {
////        ContextListWidget list = (ContextListWidget)result.getViewport().getView();
////        String title = REGISTRY.get("lastreport", "Report");
////                genj.fo.Document doc = new genj.fo.Document(title);
////        doc.startSection(title);
////        for (Context c : list.getContexts()) {
////          if (c instanceof ViewContext)
////            doc.addText(c.getEntity()+":"+((ViewContext)c).getText());
////          else
////            doc.addText(c.toString());
////          doc.nextParagraph();
////        }
////        showResult(doc);
////        // done
////        return;
////      }
//        }
//  } // ActionSave
//
} // ReportView

