/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.report;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.modules.document.view.HyperLinkTextDocumentView;
import ancestris.modules.document.view.WidgetDocumentView;
import genj.common.ContextListWidget;
import genj.fo.Format;
import genj.fo.FormatOptionsWidget;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.DialogHelper;
import genj.util.swing.ImageIcon;
import genj.view.ToolBar;
import genj.view.View;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import spin.Spin;

/**
 * Component for running reports on genealogic data
 */
//XXX: needs to be refactored in depth
public class ReportView extends View {

    /* package */
    static Logger LOG = Logger.getLogger("genj.report");
    /** statics */
    private final static ImageIcon imgStart = new ImageIcon(ReportView.class, "Start"),
            imgStop = new ImageIcon(ReportView.class, "Stop");
    /** gedcom this view is for */
    private Gedcom gedcom;
    /** components to show report info */
    private HyperLinkTextDocumentView output;
    private ActionStart actionStart = new ActionStart();
    private ActionStop actionStop = new ActionStop();
    /** registry for settings */
    private final static Registry REGISTRY = Registry.get(ReportView.class);
    /** resources */
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
     * start a report
     */
    public void startReport(final Report report, Object context) {

        if (!actionStart.isEnabled()) {
            return;
        }
        // create a new tab for this run
        output = new HyperLinkTextDocumentView(
                new Context(gedcom),
                report.getShortName(),
                gedcom.getName() + ": " + report.getName());

        if (report.getStartMethod(context) == null) {
            for (int i = 0; i < Gedcom.ENTITIES.length; i++) {
                String tag = Gedcom.ENTITIES[i];
                Entity sample = gedcom.getFirstEntity(tag);
                if (sample != null && report.accepts(sample) != null) {

                    // give the report a chance to name our dialog
                    String txt = report.accepts(sample.getClass());
                    if (txt == null) {
                        Gedcom.getName(tag);
                    }

                    // ask user for context now
                    context = report.getEntityFromUser(txt, gedcom, tag);
                    if (context == null) {
                        return;
                    }
                    break;
                }
            }
        }

        // check if appropriate
        if (context == null || report.accepts(context) == null) {
            DialogHelper.openDialog(report.getName(), DialogHelper.ERROR_MESSAGE, RESOURCES.getString("report.noaccept"), AbstractAncestrisAction.okOnly(), ReportView.this);
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
            actionStart.setEnabled(gedcom != null);
            actionStop.setEnabled(false);

            // handle result
            showResult(result);
        }
    }

    /**
     * Start a report after selection
     */
    public void startReport() {
        // minimum we can work on?
        if (gedcom == null) {
            return;
        }
        Report report = selector.getReport();
        if (report == null) {
            return;
        }
        startReport(report, gedcom);
    }

    /**
     * stop any running report
     */
    public void stopReport() {
        // TODO: there's no way to stop a running java report atm
    }

    @Override
    public void setContext(Context context) {
        gedcom = context.getGedcom();
        // enable if none running and data available
        actionStart.setEnabled(!actionStop.isEnabled() && gedcom != null);
    }

    /**
     * show result of a report run
     */
    /* package */
    void showResult(Object object) {

        // none?
        if (object == null) {
            if (output.isEmpty()) {
                output.add("*** No Result");
            }
            return;
        }

        // Exception?
        if (object instanceof InterruptedException) {
            output.add("*** cancelled");
            return;
        }

        if (object instanceof Throwable) {
            CharArrayWriter buf = new CharArrayWriter(256);
            ((Throwable) object).printStackTrace(new PrintWriter(buf));
            output.add("*** exception caught" + '\n' + buf);

            LOG.log(Level.WARNING, "Exception caught ", (Throwable) object);
            return;
        }

        // remember title for next document view creation
        String tabName = output.getName();
        String tabToolTip = output.getToolTipText();
        if (output.isEmpty()) {
            output.close();
        }

        // File?
        if (object instanceof File) {
            File file = (File) object;
            if (file.getName().endsWith(".htm") || file.getName().endsWith(".html")) {
                try {
                    object = file.toURI().toURL();
                } catch (Throwable t) {
                    // can't happen
                }
            } else {
                try {
                    Desktop.getDesktop().open(file);
                } catch (Throwable t) {
                    Logger.getLogger("genj.report").log(Level.INFO, "can't open " + file, t);
                    output.add("*** can't open file " + file);
                }
                return;
            }
        }

        // URL?
        if (object instanceof URL) {
            try {
                output.setPage((URL) object);
            } catch (IOException e) {
                output.add("*** can't open URL " + object + ": " + e.getMessage());
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
            new WidgetDocumentView(new Context(gedcom), tabName, tabToolTip, ((JComponent) object));

            return;
        }

        // document
        if (object instanceof genj.fo.Document) {

            genj.fo.Document doc = (genj.fo.Document) object;
            String title = "Document " + doc.getTitle();

            Registry foRegistry = Registry.get(getClass());

            Action[] actions = AbstractAncestrisAction.okCancel();
            FormatOptionsWidget options = new FormatOptionsWidget(doc, foRegistry);
            options.connect(actions[0]);

            int rc = DialogHelper.openDialog(title, DialogHelper.QUESTION_MESSAGE, options, actions, this);
            Format formatter = options.getFormat();
            File file = options.getFile();
            if (rc != 0 || formatter.getFileExtension() == null || file == null) {
                showResult(null);
                return;
            }

            // store options
            options.remember(foRegistry);

            // format and write
            try {
                file.getParentFile().mkdirs();
                formatter.format(doc, file);
            } catch (Throwable t) {
                LOG.log(Level.WARNING, "formatting " + doc + " failed", t);
                output.add("*** formatting " + doc + " failed");
                return;
            }

            // go back to document's file
            showResult(file);

            return;
        }

        // unknown
        output.add("*** report returned unknown result " + object);
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
    private class ActionStart extends AbstractAncestrisAction {

        /** constructor */
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

