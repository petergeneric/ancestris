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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.UnitOfWork;
import genj.io.FileAssociation;
import genj.option.OptionsWidget;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImageIcon;
import genj.view.ContextSelectionEvent;
import genj.view.ToolBarSupport;
import genj.view.ViewContext;
import genj.view.ViewManager;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

/**
 * Component for running reports on genealogic data
 */
public class ReportView extends JPanel implements ToolBarSupport {

  /*package*/ static Logger LOG = Logger.getLogger("genj.report");

  /** time between flush of output writer to output text area */
  private final static long FLUSH_WAIT = 200;
  private final static String EOL= System.getProperty("line.separator");

  /** statics */
  private final static ImageIcon
    imgStart = new ImageIcon(ReportView.class,"Start"      ),
    imgStop  = new ImageIcon(ReportView.class,"Stop"       ),
    imgSave  = new ImageIcon(ReportView.class,"Save"       ),
    imgReload= new ImageIcon(ReportView.class,"Reload"     ),
    imgGroup = new ImageIcon(ReportView.class,"Group"      );


  /** gedcom this view is for */
  private Gedcom      gedcom;

  /** components to show report info */
  private JLabel      lFile,lAuthor,lVersion;
  private JTextPane   tpInfo;
  private JEditorPane taOutput;
  private ReportList  listOfReports;
  private JTabbedPane tabbedPane;
  private ActionStart actionStart = new ActionStart();
  private ActionStop actionStop = new ActionStop(actionStart);
  private OptionsWidget owOptions;

  private HTMLEditorKit editorKit;

  /** registry for settings */
  private Registry registry;

  /** resources */
  /*package*/ static final Resources RESOURCES = Resources.get(ReportView.class);

  /** manager */
  private ViewManager manager ;

  /** title of this view */
  private String title;

  /**
   * Constructor
   */
  public ReportView(String theTitle, Gedcom theGedcom, Registry theRegistry, ViewManager theManager) {

    // data
    gedcom   = theGedcom;
    registry = theRegistry;
    manager  = theManager;
    title    = theTitle;

    // Layout for this component
    setLayout(new BorderLayout());

    // Noteboook in Center
    tabbedPane = new JTabbedPane();
    add(tabbedPane,"Center");

    // three tabs
    Callback callback = new Callback();
    tabbedPane.add(RESOURCES.getString("report.reports"),createReportList(callback));
    tabbedPane.add(RESOURCES.getString("report.options"), createReportOptions());
    tabbedPane.add(RESOURCES.getString("report.output"),createReportOutput(callback));

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
   * Create tab content for report list/info
   */
  private JPanel createReportList(Callback callback) {

    // Panel for Report
    JPanel reportPanel = new JPanel();
    reportPanel.setBorder(new EmptyBorder(3,3,3,3));
    GridBagHelper gh = new GridBagHelper(reportPanel);

    // ... List of reports
    listOfReports = new ReportList(ReportLoader.getInstance().getReports(),
            registry.get("group", ReportList.VIEW_TREE), registry);
    listOfReports.setSelectionListener(callback);

    JScrollPane spList = new JScrollPane(listOfReports) {
      /** min = preferred */
      public Dimension getMinimumSize() {
        return super.getPreferredSize();
      }
    };
    spList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    gh.add(spList,1,0,1,5,GridBagHelper.GROWFILL_VERTICAL);

    // ... Report's filename
    gh.setParameter(GridBagHelper.FILL_HORIZONTAL);
    gh.setInsets(new Insets(0, 0, 0, 5));

    lFile = new JLabel("");
    lFile.setForeground(Color.black);

    gh.add(new JLabel(RESOURCES.getString("report.file")),2,0);
    gh.add(lFile,3,0,1,1,GridBagHelper.GROWFILL_HORIZONTAL);

    // ... Report's author

    lAuthor = new JLabel("");
    lAuthor.setForeground(Color.black);

    gh.add(new JLabel(RESOURCES.getString("report.author")),2,1);
    gh.add(lAuthor,3,1,1,1,GridBagHelper.GROWFILL_HORIZONTAL);

    // ... Report's version
    lVersion = new JLabel();
    lVersion.setForeground(Color.black);

    gh.add(new JLabel(RESOURCES.getString("report.version")),2,2);
    gh.add(lVersion,3,2);

    editorKit = new HTMLEditorKit(this.getClass());
    // ... Report's infos
    tpInfo = new JTextPane();
    tpInfo.setEditable(false);
    tpInfo.setEditorKit(editorKit);
    tpInfo.setFont(new JTextField().getFont()); //don't use standard clunky text area font
    tpInfo.addHyperlinkListener(new FollowHyperlink(tpInfo));
    JScrollPane spInfo = new JScrollPane(tpInfo);
    gh.add(new JLabel(RESOURCES.getString("report.info")),2,3);
    gh.add(spInfo,2,4,2,1,GridBagHelper.FILL_BOTH);

    // done
    return reportPanel;

  }

  /**
   * Create the tab content for report output
   * Output tab is a JEditorPane that displays either plain text or html text:
   */
  private JComponent createReportOutput(Callback callback) {

    // Panel for Report Output
    taOutput = new JEditorPane();
    taOutput.setContentType("text/plain");
    taOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
    taOutput.setEditable(false);
    taOutput.addHyperlinkListener(new FollowHyperlink(taOutput));
    taOutput.addMouseMotionListener(callback);
    taOutput.addMouseListener(callback);

    // Done
    return new JScrollPane(taOutput);
  }

  /**
   * Create the tab content for report options
   */
  private JComponent createReportOptions() {
    owOptions = new OptionsWidget(getName());
    return owOptions;
  }

  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(480,320);
  }

  /**
   * Returns the view manager
   */
  /*package*/ ViewManager getViewManager() {
    return manager;
  }

  /**
   * Runs a specific report
   */
  /*package*/ void run(Report report, Object context) {
    // not if running
    if (!actionStart.isEnabled()) 
      return;
    // to front
    manager.showView(this);
    // start it
    listOfReports.setSelection(report);
    // TODO this is a hack - I want to pass the context over but also use the same ActionStart instance
    actionStart.setContext(context);
    actionStart.trigger();
  }

  /**
   * Helper that sets buttons states
   */
  private boolean setRunning(boolean on) {

    // Show it on buttons
    actionStart.setEnabled(!on);
    actionStop .setEnabled(on);

    taOutput.setCursor(Cursor.getPredefinedCursor(
      on?Cursor.WAIT_CURSOR:Cursor.DEFAULT_CURSOR
    ));

    // Done
    return true;
  }

  /**
   * @see genj.view.ToolBarSupport#populate(javax.swing.JToolBar)
   */
  public void populate(JToolBar bar) {

    // Buttons at bottom
    ButtonHelper bh = new ButtonHelper().setContainer(bar).setInsets(0);

    bh.create(actionStart);
    bh.create(actionStop);
    bh.create(new ActionSave());
    bh.create(new ActionReload());
    bh.create(new ActionGroup());

    // done
  }

  /**
   * Action: RELOAD
   */
  private class ActionReload extends Action2 {
    protected ActionReload() {
      setImage(imgReload);
      setTip(RESOURCES, "report.reload.tip");
      setEnabled(!ReportLoader.getInstance().isReportsInClasspath());
    }
    protected void execute() {
      // show first page and unselect report
      tabbedPane.getModel().setSelectedIndex(0);
      listOfReports.setSelection(null);
      // .. do it (forced!);
      ReportLoader.clear();
      // .. get them
      Report reports[] = ReportLoader.getInstance().getReports();
      // .. update
      listOfReports.setReports(reports);
      // .. done
    }
  } //ActionReload

  /**
   * Action: STOP
   */
  private class ActionStop extends Action2 {
    private Action2 start;
    protected ActionStop(Action2 start) {
      setImage(imgStop);
      setTip(RESOURCES, "report.stop.tip");
      setEnabled(false);
      this.start=start;
    }
    protected void execute() {
      start.cancel(false);
    }
  } //ActionStop

  /**
   * Action: START
   */
  private class ActionStart extends Action2 {

    /** context to run on */
    private Object context;

    /** the running report */
    private Report instance;

    /** an output writer */
    private PrintWriter out;

    /** constructor */
    protected ActionStart() {
      // setup async
      setAsync(ASYNC_SAME_INSTANCE);
      // show
      setImage(imgStart);
      setTip(RESOURCES, "report.start.tip");
    }
    
    protected void setContext(Object context) {
      this.context = context;
    }

    /**
     * pre execute
     */
    protected boolean preExecute() {

      // commit options
      owOptions.stopEditing();

      // .. change buttons
      setRunning(true);

      // Calc Report
      Report report = listOfReports.getSelection();
      if (report==null)
        return false;

      out = new PrintWriter(new OutputWriter());

      // create our own private instance
      instance = report.getInstance(ReportView.this, out);

      // either use preset context, gedcom file or ask for entity
      Object useContext = context;
      context = null;
      
      if (useContext==null) {
        if (instance.getStartMethod(gedcom)!=null)
          useContext = gedcom;
        else  for (int i=0;i<Gedcom.ENTITIES.length;i++) {
          String tag = Gedcom.ENTITIES[i];
          Entity sample = gedcom.getFirstEntity(tag);
          if (instance.accepts(sample)!=null) {
            
            // give the report a chance to name our dialog
            String txt = instance.accepts(sample.getClass());
            if (txt==null) Gedcom.getName(tag);
            
            // ask user for context now
            useContext = instance.getEntityFromUser(txt, gedcom, tag);
            if (useContext==null) 
              return false;
            break;
          }
        }
      }

      // check if appropriate
      if (useContext==null||report.accepts(useContext)==null) {
        WindowManager.getInstance(getTarget()).openDialog(null,report.getName(),WindowManager.ERROR_MESSAGE,RESOURCES.getString("report.noaccept"),Action2.okOnly(),ReportView.this);
        return false;
      }
      context = useContext;

      // clear the current output
      taOutput.setContentType("text/plain");
      taOutput.setText("");

      // done
      return true;
    }
    /**
     * execute
     */
    protected void execute() {

      try{
        
        if (instance.isReadOnly())
          instance.start(context);
        else
          gedcom.doUnitOfWork(new UnitOfWork() {
            public void perform(Gedcom gedcom) {
              try {
                instance.start(context);
              } catch (Throwable t) {
                throw new RuntimeException(t);
              }
            }
          });
      
      } catch (Throwable t) {
        Throwable cause = t.getCause();
        if (cause instanceof InterruptedException)
          instance.println("***cancelled");
        else
          instance.println(cause!=null?cause:t);
      }
    }

    /**
     * post execute
     */
    protected void postExecute(boolean preExecuteResult) {
      
      context = null;

      // stop run
      setRunning(false);

      // flush
      if (out!=null) {
        out.flush();
        out.close();
      }

      // no more cleanup to do?
      if (!preExecuteResult)
        return;

      // check last line for url
      URL url = null;
      try {
        AbstractDocument doc = (AbstractDocument)taOutput.getDocument();
        Element p = doc.getParagraphElement(doc.getLength()-1);
        String line = doc.getText(p.getStartOffset(), p.getEndOffset()-p.getStartOffset());
        url = new URL(line);
      } catch (Throwable t) {
      }

      if (url!=null) {
        try {
          taOutput.setPage(url);
        } catch (IOException e) {
          LOG.log(Level.WARNING, "couldn't show html in report output", e);
        }
      }

      // done
    }
  } //ActionStart

  /**
   * Action: SAVE
   */
  private class ActionSave extends Action2 {
    protected ActionSave() {
      setImage(imgSave);
      setTip(RESOURCES, "report.save.tip");
    }
    protected void execute() {
      
      // .. choose file
      JFileChooser chooser = new JFileChooser(".");
      chooser.setDialogTitle("Save Output");

      if (JFileChooser.APPROVE_OPTION != chooser.showDialog(ReportView.this,"Save")) {
        return;
      }
      File file = chooser.getSelectedFile();
      if (file==null) {
        return;
      }

      // .. exits ?
      if (file.exists()) {
        int rc = WindowManager.getInstance(getTarget()).openDialog(null, title, WindowManager.WARNING_MESSAGE, "File exists. Overwrite?", Action2.yesNo(), ReportView.this);
        if (rc!=0) {
          return;
        }
      }

      // .. open file
      final OutputStreamWriter out;
      try {
        out = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF8"));
      } catch (IOException ex) {
        WindowManager.getInstance(getTarget()).openDialog(null,title,WindowManager.ERROR_MESSAGE,"Error while saving to\n"+file.getAbsolutePath(),Action2.okOnly(),ReportView.this);
        return;
      }

      // .. save data
      try {
        Document doc = taOutput.getDocument();
        BufferedReader in = new BufferedReader(new StringReader(doc.getText(0, doc.getLength())));
        while (true) {
          String line = in.readLine();
          if (line==null) break;
          out.write(line);
          out.write("\n");
        }
        in.close();
        out.close();

      } catch (Exception ex) {
      }

      // .. done
    }

  } //ActionSave

  /**
   * Toggles gouping of reports into categories.
   * Action: GROUP
   */
  private class ActionGroup extends Action2 {
    /**
     * Creates the action object.
     */
    protected ActionGroup() {
      setImage(imgGroup);
      setTip(RESOURCES, "report.group.tip");
    }

    /**
     * Toggles grouping of reports.
     */
    protected void execute() {
        int viewType = listOfReports.getViewType();
        if (viewType == ReportList.VIEW_LIST)
            listOfReports.setViewType(ReportList.VIEW_TREE);
        else
            listOfReports.setViewType(ReportList.VIEW_LIST);
        registry.put("group", listOfReports.getViewType());
    }
  } //ActionGroup

  /**
   * A Hyperlink Follow Action
   */
  private class FollowHyperlink implements HyperlinkListener {

    private JEditorPane editor;

    /** constructor */
    private FollowHyperlink(JEditorPane editor) {
      this.editor = editor;
    }

    /** callback - link clicked */
    public void hyperlinkUpdate(HyperlinkEvent e) {
      // need activate
      if (e.getEventType()!=HyperlinkEvent.EventType.ACTIVATED)
        return;
      // internal?
      try {
        if (e.getDescription().startsWith("#")) 
            editor.scrollToReference(e.getDescription().substring(1));
        else {
          // assemble a relative URL
          Report report = listOfReports.getSelection();
          URL url = report!=null ? new URL(report.getFile().toURI().toURL(), e.getDescription()) : new URL(e.getDescription());
          FileAssociation.open(url, editor);
        }          
          
      } catch (Throwable t) {
        LOG.log(Level.FINE, "Can't handle URL for "+e.getDescription());
      }
      // done
    }

  } //FollowHyperlink

  /**
   * A private callback for various messages coming in
   */
  private class Callback extends MouseAdapter implements MouseMotionListener,
      ReportSelectionListener {

    /** the currently found entity id */
    private String id = null;

    /**
     * Monitor changes to selection of reports
     */
    public void valueChanged(Report report) {
      // update info
      if (report == null) {
        lFile    .setText("");
        lAuthor  .setText("");
        lVersion .setText("");
        tpInfo   .setText("");
        owOptions.setOptions(Collections.EMPTY_LIST);
      } else {
        editorKit.setFrom(report.getClass());
        lFile    .setText(report.getFile().getName());
        lAuthor  .setText(report.getAuthor());
        lVersion .setText(getReportVersion(report));
        tpInfo   .setText(report.getInfo().replaceAll("\n", "<br>"));
        tpInfo   .setCaretPosition(0);
        owOptions.setOptions(report.getOptions());
      }
    }

    /**
     * Returns the report version with last update date
     * @param report the report
     * @return version information
     */
    private String getReportVersion(Report report) {
      String version = report.getVersion();
      String update = report.getLastUpdate();
      if (update != null)
        version += " - " + RESOURCES.getString("report.updated") + ": " + update;
      return version;
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
      if (id!=null) {
        Entity entity = gedcom.getEntity(id);
        if (entity!=null)
          WindowManager.broadcast(new ContextSelectionEvent(new ViewContext(entity), ReportView.this, e.getClickCount()>1));
      }
    }

    /**
     * Tries to find an entity id at given position in output
     */
    private String markIDat(Point loc) {

      try {
        // do we get a position in the model?
        int pos = taOutput.viewToModel(loc);
        if (pos<0)
          return null;

        // scan doc
        Document doc = taOutput.getDocument();

        // find ' ' to the left
        for (int i=0;;i++) {
          // stop looking after 10
          if (i==10)
            return null;
          // check for starting line or non digit/character
          if (pos==0 || !Character.isLetterOrDigit(doc.getText(pos-1, 1).charAt(0)) )
            break;
          // continue
          pos--;
        }

        // find ' ' to the right
        int len = 0;
        while (true) {
          // stop looking after 10
          if (len==10)
            return null;
          // stop at end of doc
          if (pos+len==doc.getLength())
            break;
          // or non digit/character
          if (!Character.isLetterOrDigit(doc.getText(pos+len, 1).charAt(0)))
            break;
          // continue
          len++;
        }

        // check if it's an ID
        if (len<2)
          return null;
        String id = doc.getText(pos, len);
        if (gedcom.getEntity(id)==null)
          return null;

        // mark it
        taOutput.requestFocusInWindow();
        taOutput.setCaretPosition(pos);
        taOutput.moveCaretPosition(pos+len);

        // return in betwee
        return id;

        // done
      } catch (BadLocationException ble) {
      }

      // not found
      return null;
    }

    /**
     * have to implement MouseMotionListener.mouseDragger()
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent e) {
      // ignored
    }

  } //Callback

  /**
   * A printwriter that directs output to the text area
   */
  private class OutputWriter extends Writer {

    /** buffer */
    private StringBuffer buffer = new StringBuffer(4*1024);

    /** timer */
    private long lastFlush = -1;

    /**
     * @see java.io.Writer#close()
     */
    public void close() {
      // clear buffer
      buffer.setLength(0);
    }

    /**
     * @see java.io.Writer#flush()
     */
    public void flush() {

      // something to flush?
      if (buffer.length()==0)
        return;

      // make sure we see output pane
      tabbedPane.getModel().setSelectedIndex(2);

      // mark
      lastFlush = System.currentTimeMillis();

      // grab text, reset buffer and dump it
      String txt = buffer.toString();
      buffer.setLength(0);
      Document doc = taOutput.getDocument();
      try {
        doc.insertString(doc.getLength(), txt, null);
      } catch (Throwable t) {
      }

      // done
    }

    /**
     * @see java.io.Writer#write(char[], int, int)
     */
    public void write(char[] cbuf, int off, int len) throws IOException {
      // append to buffer - strip any \r from \r\n
      for (int i=0;i<len;i++) {
        char c = cbuf[off+i];
        if (c!='\r') buffer.append(c);
      }
      // check flush
      if (System.currentTimeMillis()-lastFlush > FLUSH_WAIT)
        flush();
      // done
    }

  } //OutputWriter

} //ReportView
