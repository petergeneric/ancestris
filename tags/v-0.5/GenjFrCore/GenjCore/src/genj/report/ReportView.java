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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.report;

import genj.common.ContextListWidget;
import genj.fo.Format;
import genj.fo.FormatOptionsWidget;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import genj.util.swing.EditorHyperlinkSupport;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;
import genj.view.SelectionSink;
import genj.view.ToolBar;
import genj.view.View;
import genj.view.ViewContext;

import java.awt.CardLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;

import spin.Spin;

/**
 * Component for running reports on genealogic data
 */
public class ReportView extends View {

  /* package */static Logger LOG = Logger.getLogger("genj.report");

  /** pages to switch between */
  private final static String
    WELCOME = "welcome",
    CONSOLE = "console",
    RESULT = "result";
  private String currentPage = WELCOME;

  /** time between flush of output writer to output text area */
  private final static String EOL = System.getProperty("line.separator");

  /** statics */
  private final static ImageIcon 
    imgStart = new ImageIcon(ReportView.class, "Start"), 
    imgStop = new ImageIcon(ReportView.class, "Stop"), 
    imgSave = new ImageIcon(ReportView.class, "Save"), 
    imgConsole = new ImageIcon(ReportView.class, "ReportShell"), 
    imgGui = new ImageIcon(ReportView.class, "ReportGui");

  /** gedcom this view is for */
  private Gedcom gedcom;

  /** components to show report info */
  private Console output;
  private JScrollPane result;
  private ActionStart actionStart = new ActionStart();
  private ActionStop actionStop = new ActionStop();
  private ActionShow actionShow = new ActionShow();

  /** registry for settings */
  private final static Registry REGISTRY = Registry.get(ReportView.class);

  /** resources */
  /* package */static final Resources RESOURCES = Resources.get(ReportView.class);

  /**
   * Constructor
   */
  public ReportView() {
    
    setLayout(new CardLayout());
    
    // Output
    output = new Console();
    add(new JScrollPane(output), CONSOLE);
    
    // result
    result = new JScrollPane();
    add(result, RESULT);

    // welcome panel
    String msg = RESOURCES.getString("report.welcome");
    int i = msg.indexOf('*');
    String pre = i<0 ? "" : msg.substring(0, i);
    String post = i<0 ? "" : msg.substring(i+1);
    
    JButton b = new JButton(new ActionStart());
    b.setRequestFocusEnabled(false);
    b.setOpaque(false);
    
    JPanel welcome = new JPanel(new NestedBlockLayout("<col><row><a wx=\"1\" ax=\"1\" wy=\"1\"/><b/><c wx=\"1\"/></row></col>"));
    welcome.setBackground(output.getBackground());
    welcome.setOpaque(true);
    welcome.add(new JLabel(pre, SwingConstants.RIGHT));
    welcome.add(b);
    welcome.add(new JLabel(post));
    add(welcome, WELCOME);

    // done
  }

  /**
   * @see javax.swing.JComponent#removeNotify()
   */
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

    if (!actionStart.isEnabled())
      return;

    if (report.getStartMethod(context) == null) {
      for (int i = 0; i < Gedcom.ENTITIES.length; i++) {
        String tag = Gedcom.ENTITIES[i];
        Entity sample = gedcom.getFirstEntity(tag);
        if (sample != null && report.accepts(sample) != null) {

          // give the report a chance to name our dialog
          String txt = report.accepts(sample.getClass());
          if (txt == null)
            Gedcom.getName(tag);

          // ask user for context now
          context = report.getEntityFromUser(txt, gedcom, tag);
          if (context == null)
            return;
          break;
        }
      }
    }

    // check if appropriate
    if (context == null || report.accepts(context) == null) {
      DialogHelper.openDialog(report.getName(), DialogHelper.ERROR_MESSAGE, RESOURCES.getString("report.noaccept"), Action2.okOnly(), ReportView.this);
      return;
    }

    // remember
    REGISTRY.put("lastreport", report.getClass().getName());
    
    // set report ui context
    report.setOwner(this);

    // clear the current output and show coming
    clear();
    show(CONSOLE);
    
    // set running
    actionStart.setEnabled(false);
    actionStop.setEnabled(true);
    
    ReportPluginFactory.getInstance().setEnabled(false);

    // kick it off
    new Thread(new Runner(gedcom, context, report, (Runner.Callback) Spin.over(new RunnerCallback()))).start();

  }
  
  private void clear() {
    output.clear();
    output.setContentType("text/plain");
    result.setViewportView(null);
    actionShow.setSelected(false);
    actionShow.setEnabled(false);
    show(gedcom!=null ? WELCOME : CONSOLE);
  }

  /**
   * callback for runner
   */
  private class RunnerCallback implements Runner.Callback {

    public void handleOutput(Report report, String s) {
      
      if (currentPage!=CONSOLE)
        show(CONSOLE);
      
      output.add(s);
    }

    public void handleResult(Report report, Object result) {

      LOG.fine("Result of report " + report.getName() + " = " + result);

      // let report happend again
      actionStart.setEnabled(gedcom != null);
      actionStop.setEnabled(false);
      ReportPluginFactory.getInstance().setEnabled(true);

      // handle result
      showResult(result);

    }

  }

  /**
   * Start a report after selection
   */
  public void startReport() {
    
    // minimum we can work on?
    if (gedcom==null)
      return;

    // let user pick
    ReportSelector selector = new ReportSelector();
    try {
      selector.select(ReportLoader.getInstance().getReportByName(REGISTRY.get("lastreport", (String) null)));
    } catch (Throwable t) {
    }

    if (0 != DialogHelper.openDialog(RESOURCES.getString("report.reports"), DialogHelper.QUESTION_MESSAGE, selector, Action2.okCancel(), ReportView.this))
      return;

    Report report = selector.getReport();
    if (report == null)
      return;

    REGISTRY.put("lastreport", report.getClass().getName());

    startReport(report, gedcom);

  }

  /**
   * stop any running report
   */
  public void stopReport() {
    // TODO there's no way to stop a running java report atm
  }

  @Override
  public void setContext(Context context, boolean isActionPerformed) {

    // keep
    if (gedcom!=context.getGedcom())
      clear();
    gedcom = context.getGedcom();
    
    // enable if none running and data available
    actionStart.setEnabled(!actionStop.isEnabled() && gedcom != null);

  }
  
  /**
   * show welcome/console/output
   */
  /* package */void show(String page) {
    if (currentPage!=page) {
      ((CardLayout) getLayout()).show(this, page);
      currentPage = page;
    }
  }
  
  @SuppressWarnings("unchecked")
  /**
   * show result of a report run
   */
  /* package */void showResult(Object object) {

    // none?
    if (object == null) {
      
      // go back to welcome if there was nothing dumped to console
      if (output.getDocument().getLength()==0)
        show(WELCOME);
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
      
      LOG.log(Level.WARNING, "Exception caught ", (Throwable)object);
      return;
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
          Logger.getLogger("genj.report").log(Level.INFO, "can't open "+file, t);
          output.add("*** can't open file "+file);
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
      actionShow.setEnabled(false);
      actionShow.setSelected(false);
      show(CONSOLE);
      return;
    }

    // context list?
    if (object instanceof List<?>) {
      try {
        object = new ContextListWidget((List<Context>)object);
      } catch (Throwable t) {
      }
    }

    // component?
    if (object instanceof JComponent) {
      JComponent c = (JComponent) object;
      c.setMinimumSize(new Dimension(0, 0));
      result.setViewportView(c);
      actionShow.setEnabled(true);
      actionShow.setSelected(true);
      show(RESULT);
      return;
    }
    
    // document
    if (object instanceof genj.fo.Document) {

      genj.fo.Document doc = (genj.fo.Document) object;
      String title = "Document " + doc.getTitle();

      Registry foRegistry = Registry.get(getClass());

      Action[] actions = Action2.okCancel();
      FormatOptionsWidget options = new FormatOptionsWidget(doc, foRegistry);
      options.connect(actions[0]);
      
      int rc = DialogHelper.openDialog(title, DialogHelper.QUESTION_MESSAGE, options, actions, this);
      Format formatter = options.getFormat();
      File file = options.getFile();
      if (rc!=0 || formatter.getFileExtension() == null || file == null) {
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
    //toolbar.add(actionStop);
    
    toolbar.add(new JToggleButton(actionShow));
    toolbar.add(new ActionSave());

    // done
  }

  /**
   * Action: STOP
   */
  private class ActionStop extends Action2 {
    protected ActionStop() {
      setImage(imgStop);
      setTip(RESOURCES, "report.stop.tip");
      setEnabled(false);
    }

    public void actionPerformed(ActionEvent event) {
      stopReport();
    }
  } // ActionStop

  /**
   * Action: START
   */
  private class ActionStart extends Action2 {

    /** context to run on */
    private Object context;

    /** the running report */
    private Report report;

    /** an output writer */
    private PrintWriter out;

    /** constructor */
    protected ActionStart() {
      // show
      setImage(imgStart);
      setTip(RESOURCES, "report.start.tip");
    }

    /**
     * execute
     */
    public void actionPerformed(ActionEvent event) {
      startReport();
    }

  } // ActionStart

  /**
   * Action: Console
   */
  private class ActionShow extends Action2 {
    protected ActionShow() {
      setImage(imgConsole);
      setTip(RESOURCES, "report.output");
      setEnabled(false);
    }

    public void actionPerformed(ActionEvent event) {
      setSelected(isSelected());
    }
    
    @Override
    public boolean setSelected(boolean selected) {
      setImage(selected ? imgGui : imgConsole);
      if (selected)
        show(RESULT);
      else
        show(CONSOLE);
      return super.setSelected(selected);
    }
      
  }

  /**
   * Action: SAVE
   */
  private class ActionSave extends Action2 {
    protected ActionSave() {
      setImage(imgSave);
      setTip(RESOURCES, "report.save.tip");
    }

    public void actionPerformed(ActionEvent event) {
      
      // user looking at a context-list?
      if (result.isVisible() && result.getViewport().getView() instanceof ContextListWidget) {
        ContextListWidget list = (ContextListWidget)result.getViewport().getView();
        String title = REGISTRY.get("lastreport", "Report");
        genj.fo.Document doc = new genj.fo.Document(title);
        doc.startSection(title);
        for (Context c : list.getContexts()) {
          if (c instanceof ViewContext)
            doc.addText(c.getEntity()+":"+((ViewContext)c).getText());
          else
            doc.addText(c.toString());
          doc.nextParagraph();
        }
        showResult(doc);
        // done
        return;
      }

      // .. choose file
      JFileChooser chooser = new JFileChooser(".");
      chooser.setDialogTitle(getTip());
      chooser.setFileFilter(new FileFilter() {
        @Override
        public boolean accept(File f) {
          return f.isDirectory() || f.getName().endsWith(".txt");
        }

        @Override
        public String getDescription() {
          return "*.txt (Text)";
        }
      });

      if (JFileChooser.APPROVE_OPTION != chooser.showDialog(ReportView.this, "Save")) 
        return;
      File file = chooser.getSelectedFile();
      if (file == null) 
        return;
      if (!file.getName().endsWith("*.txt"))
        file = new File(file.getAbsolutePath()+".txt");

      // .. exits ?
      if (file.exists()) {
        int rc = DialogHelper.openDialog(RESOURCES.getString("title"), DialogHelper.WARNING_MESSAGE, "File exists. Overwrite?", Action2.yesNo(), ReportView.this);
        if (rc != 0) 
          return;
      }

      // .. open file
      final OutputStreamWriter out;
      try {
        out = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF8"));
      } catch (IOException ex) {
        DialogHelper.openDialog(RESOURCES.getString("title"), DialogHelper.ERROR_MESSAGE, "Error while saving to\n" + file.getAbsolutePath(), Action2.okOnly(), ReportView.this);
        return;
      }

      // .. save data
      try {
        String newline = System.getProperty("line.separator");
        BufferedReader in = new BufferedReader(new StringReader(output.getText()));
        while (true) {
          String line = in.readLine();
          if (line == null)
            break;
          out.write(line);
          out.write(newline);
        }
        in.close();
        out.close();

      } catch (Exception ex) {
      }

      // .. done
    }

  } // ActionSave

  /**
   * console output
   */
  private class Console extends JEditorPane implements MouseListener, MouseMotionListener {

    /** the currently found entity id */
    private String id = null;

    /** constructor */
    private Console() {
      setContentType("text/plain");
      setFont(new Font("Monospaced", Font.PLAIN, 12));
      setEditable(false);
      addHyperlinkListener(new EditorHyperlinkSupport(this));
      addMouseMotionListener(this);
      addMouseListener(this);
    }

    /**
     * Check if user moves mouse above something recognizeable in output
     */
    public void mouseMoved(MouseEvent e) {

      // try to find id at location
      id = markIDat(e.getPoint());

      // done
    }

    /**
     * Check if user clicks on marked ID
     */
    public void mouseClicked(MouseEvent e) {
      if (id != null && gedcom != null) {
        Entity entity = gedcom.getEntity(id);
        if (entity != null)
          SelectionSink.Dispatcher.fireSelection(e, new Context(entity));
      }
    }

    /**
     * Tries to find an entity id at given position in output
     */
    private String markIDat(Point loc) {

      try {
        // do we get a position in the model?
        int pos = viewToModel(loc);
        if (pos < 0)
          return null;

        // scan doc
        javax.swing.text.Document doc = getDocument();

        // find ' ' to the left
        for (int i = 0;; i++) {
          // stop looking after 10
          if (i == 10)
            return null;
          // check for starting line or non digit/character
          if (pos == 0 || !Character.isLetterOrDigit(doc.getText(pos - 1, 1).charAt(0)))
            break;
          // continue
          pos--;
        }

        // find ' ' to the right
        int len = 0;
        while (true) {
          // stop looking after 10
          if (len == 10)
            return null;
          // stop at end of doc
          if (pos + len == doc.getLength())
            break;
          // or non digit/character
          if (!Character.isLetterOrDigit(doc.getText(pos + len, 1).charAt(0)))
            break;
          // continue
          len++;
        }

        // check if it's an ID
        if (len < 2)
          return null;
        String id = doc.getText(pos, len);
        if (gedcom == null || gedcom.getEntity(id) == null)
          return null;

        // mark it
        // requestFocusInWindow();
        setCaretPosition(pos);
        moveCaretPosition(pos + len);

        // return in between
        return id;

        // done
      } catch (BadLocationException ble) {
      }

      // not found
      return null;
    }

    /**
     * have to implement MouseMotionListener.mouseDragger()
     * 
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent e) {
      // ignored
    }

    void clear() {
      setContentType("text/plain");
      setText("");
    }

    void add(String txt) {
      javax.swing.text.Document doc = getDocument();
      try {
        doc.insertString(doc.getLength(), txt, null);
      } catch (Throwable t) {
      }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

  } // Output

} // ReportView

