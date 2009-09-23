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
 *
 * $Revision: 1.135 $ $Author: nmeier $ $Date: 2009/02/14 23:55:32 $
 */
package genj.report;

import genj.chart.Chart;
import genj.common.ContextListWidget;
import genj.common.SelectEntityWidget;
import genj.fo.Document;
import genj.fo.Format;
import genj.fo.FormatOptionsWidget;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.time.PointInTime;
import genj.io.FileAssociation;
import genj.option.Option;
import genj.option.OptionsWidget;
import genj.option.PropertyOption;
import genj.util.EnvironmentChecker;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ChoiceWidget;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;


/**
 * Base-class of all GenJ reports. Sub-classes that are compiled
 * and available in ./report will be loaded by GenJ automatically
 * and can be reloaded during runtime.
 */
public abstract class Report implements Cloneable {

  protected final static Logger LOG = Logger.getLogger("genj.report");

  protected final static ImageIcon
    IMG_SHELL = new genj.util.swing.ImageIcon(ReportView.class,"ReportShell"),
    IMG_FO    = new genj.util.swing.ImageIcon(ReportView.class,"ReportFO"  ),
    IMG_GUI   = new genj.util.swing.ImageIcon(ReportView.class,"ReportGui"  );

  /** global report options */
  protected Options OPTIONS = Options.getInstance();

  /** options */
  protected final static int
    OPTION_YESNO    = 0,
    OPTION_OKCANCEL = 1,
    OPTION_OK       = 2;

  /** categories */
  private static final Category DEFAULT_CATEGORY = new Category("Other", IMG_SHELL);
  private static final Categories categories = new Categories();
  static {
      // Default category when category isn't defined in properties file
      categories.add(DEFAULT_CATEGORY);
  }

  private final static String[][] OPTION_TEXTS = {
    new String[]{Action2.TXT_YES, Action2.TXT_NO     },
    new String[]{Action2.TXT_OK , Action2.TXT_CANCEL },
    new String[]{Action2.TXT_OK }
  };

  /** alignment options */
  protected final static int
    ALIGN_LEFT   = 0,
    ALIGN_CENTER = 1,
    ALIGN_RIGHT  = 2;

  /** one report for all reports */
  protected final static Registry registry = new Registry("genj-reports");

  /** language we're trying to use */
  private final static String lang = Locale.getDefault().getLanguage();

  /** translation texts */
  private Resources resources;

  /** out */
  protected PrintWriter out;

  /** a window  manager */
  private WindowManager windowManager;

  /** owning component */
  private Component owner;

  /** options */
  private List options;

  /** image */
  private ImageIcon image;

  /** file */
  private File file;

  /**
   * Constructor
   */
  protected Report() {

  }

  /**
   * integration - private instance for a run
   */
  /*package*/ Report getInstance(Component owner, PrintWriter out) {

    try {

      // make sure options are initialized
      getOptions();

      // clone this
      Report result = (Report)clone();

      // remember context for result
      result.windowManager = WindowManager.getInstance(owner);
      result.out = out;
      result.owner = owner;

      // done
      return result;

    } catch (CloneNotSupportedException e) {
      ReportView.LOG.log(Level.SEVERE, "couldn't clone report", e);
      throw new RuntimeException("getInstance() failed");
    }
  }

  /**
   * integration - log a message
   */
  /*package*/ void log(String txt) {
    if (out!=null)
      out.println(txt);
  }

  /**
   * Store report's options
   */
  public void saveOptions() {
    // if known
    if (options==null)
      return;
    // save 'em
    Iterator it = options.iterator();
    while (it.hasNext())
      ((Option)it.next()).persist(registry);
    // done
  }

  /**
   * Get report's options
   */
  public List getOptions() {

    // already calculated
    if (options!=null)
      return options;

    // calculate options
    options = PropertyOption.introspect(this);

    // restore options values
    Iterator it = options.iterator();
    while (it.hasNext()) {
      PropertyOption option = (PropertyOption)it.next();
      // restore old value
      option.restore(registry);
      // options do try to localize the name and tool tip based on a properties file
      // in the same package as the instance - problem is that this
      // won't work with our special way of resolving i18n in reports
      // so we have to do that manually
      String oname = translate(option.getProperty());
      if (oname.length()>0) option.setName(oname);
      String toolTipKey = option.getProperty() + ".tip";
      String toolTip = translate(toolTipKey);
      if (toolTip.length() > 0 && !toolTip.equals(toolTipKey))
          option.setToolTip(toolTip);
      // set category
      option.setCategory(getName());
    }

    // done
    return options;
  }

  /**
   * An image
   */
  protected ImageIcon getImage() {

    // resolve an image
    if (image==null) try {
      String file = getTypeName()+".png";
      InputStream in = getClass().getResourceAsStream(file);
      if (in==null) {
        // fallback to gif if possible
        file = getTypeName()+".gif";
        in = getClass().getResourceAsStream(file);
      }
      image = new genj.util.swing.ImageIcon(file, in);
    } catch (Throwable t) {
      image = usesStandardOut() ? IMG_SHELL : IMG_GUI;
    }

    // done
    return image;
  }

  /**
   * Returns the report category. If the category is not defined in the report's
   * properties file, the category is set to "Other".
   */
  public Category getCategory() {
      String name = translate("category");
      if (name.equals("category"))
          return DEFAULT_CATEGORY;

      Category category = (Category)categories.get(name);
      if (category == null) {
          category = createCategory(name);
          categories.add(category);
      }
      return category;
  }

  private Category createCategory(String name) {
      String file = "Category" + name + ".png";

      InputStream in = Report.class.getResourceAsStream(file);
      if (in == null)
          in = getClass().getResourceAsStream(file);

      ImageIcon image;
      if (in != null)
          image = new genj.util.swing.ImageIcon(file, in);
      else
          image = IMG_SHELL;
      return new Category(name, image);
  }

  /**
   * When a report is executed all its text output is gathered and
   * shown to the user (if run through ReportView). A sub-class can
   * flush the current log with this method.
   */
  public final void flush() {
    if (out!=null)
      out.flush();
  }

  /**
   * When a report is executed all its text output is gathered and
   * shown to the user (if run through ReportView). A sub-class can
   * append a new line to the current log with this method.
   */
  public final void println() {
    println("");
  }

  /**
   * When a report is executed all its text output is gathered and
   * shown to the user (if run through ReportView). A sub-class can
   * append the text-representation of an object (toString) to the
   * current log with this method.
   */
  public final void println(Object o) {
    // nothing to do?
    if (o==null)
      return;
    // Our hook into checking for Interrupt
    if (Thread.interrupted())
      throw new RuntimeException(new InterruptedException());
    // Append it
    log(o.toString());
    // Done
  }

  /**
   * When a report is executed all its text output is gathered and
   * shown to the user (if run through ReportView). A sub-class can
   * let the user know about an exception with this method. The
   * information about the exception is appended in text-form to
   * the current log.
   */
  public final void println(Throwable t) {
    CharArrayWriter awriter = new CharArrayWriter(256);
    t.printStackTrace(new PrintWriter(awriter));
    log(awriter.toString());
  }

  /**
   * An implementation of Report can ask the user for a file with this method.
   */
  public File getFileFromUser(String title, String button) {
    return getFileFromUser(title, button, false);
  }

  /**
   * An implementation of Report can ask the user for a file with this method.
   */
  public File getFileFromUser(String title, String button, boolean askForOverwrite) {
      return getFileFromUser(title, button, askForOverwrite, null);
  }

  /**
   * An implementation of Report can ask the user for a file with this method.
   *
   * @param title  file dialog title
   * @param button  file dialog OK button text
   * @param askForOverwrite  whether to confirm overwriting files
   * @param extension  extension of files to display
   */
  public File getFileFromUser(String title, String button, boolean askForOverwrite, String extension) {

    String key = getClass().getName()+".file";

    // show filechooser
    String dir = registry.get(key, EnvironmentChecker.getProperty(this, "user.home", ".", "looking for report dir to let the user choose from"));
    JFileChooser chooser = new JFileChooser(dir);
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setDialogTitle(title);
    if (extension != null)
        chooser.setFileFilter(new FileExtensionFilter(extension));

    int rc = chooser.showDialog(owner,button);

    // check result
    File result = chooser.getSelectedFile();
    if (rc!=JFileChooser.APPROVE_OPTION||result==null)
      return null;

    // choose an existing file?
    if (result.exists()&&askForOverwrite) {
      rc = windowManager.openDialog(null, title, WindowManager.WARNING_MESSAGE, ReportView.RESOURCES.getString("report.file.overwrite"), Action2.yesNo(), owner);
      if (rc!=0)
        return null;
    }

    // keep it
    registry.put(key, result.getParent().toString());
    return result;
  }

  /**
   * An implementation of Report can ask the user for a directory with this method.
   */
  public File getDirectoryFromUser(String title, String button) {

    String key = getClass().getName()+".dir";

    // show directory chooser
    String dir = registry.get(key, EnvironmentChecker.getProperty(this, "user.home", ".", "looking for report dir to let the user choose from"));
    JFileChooser chooser = new JFileChooser(dir);
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setDialogTitle(title);
    int rc = chooser.showDialog(owner,button);

    // check result
    File result = chooser.getSelectedFile();
    if (rc!=JFileChooser.APPROVE_OPTION||result==null)
      return null;

    // keep it
    registry.put(key, result.toString());
    return result;
  }

  /**
   * A sub-class can show a document to the user with this method allowing
   * to save, transform and view it
   */
  public void showDocumentToUser(Document doc) {

    String title = "Document "+doc.getTitle();

    Registry foRegistry = new Registry(registry, getClass().getName()+".fo");

    Action[] actions = Action2.okCancel();
    FormatOptionsWidget output = new FormatOptionsWidget(doc, foRegistry);
    output.connect(actions[0]);
    int rc = windowManager.openDialog("reportdoc", title, WindowManager.QUESTION_MESSAGE, output, actions, owner);

    // cancel?
    if (rc!=0)
      return;

    // grab formatter and output file
    Format formatter = output.getFormat();

    File file = null;
    String progress = null;
    if (formatter.getFileExtension()!=null) {

      file = output.getFile();
      if (file==null)
        return;
      file.getParentFile().mkdirs();

      // show a progress dialog
      progress = windowManager.openNonModalDialog(
          null, title, WindowManager.INFORMATION_MESSAGE, new JLabel("Writing Document to file "+file+" ..."), Action2.okOnly(), owner);

    }

    // store options
    output.remember(foRegistry);

    // format and write
    try {
      formatter.format(doc, file);
    } catch (Throwable t) {
      LOG.log(Level.WARNING, "formatting "+doc+" failed", t);
      windowManager.openDialog(null, "Formatting "+doc+" failed", WindowManager.ERROR_MESSAGE, t.getMessage(), Action2.okOnly(), owner);
      file = null;
    }

    // close progress dialog
    if (progress!=null)
      windowManager.close(progress);

    // open document
    if (file!=null) {

      // let ReportView show the file or show it in external application
      if (owner instanceof ReportView && file.getName().endsWith(".html")) {
        try {
          log(""+file.toURI().toURL());
        } catch (MalformedURLException e) {}
      } else {
        FileAssociation association = FileAssociation.get(formatter.getFileExtension(), formatter.getFileExtension(), "Open", owner);
        if (association!=null)
          association.execute(file);
      }
    }

    // done
  }

  /**
   * Show a file if there's a file association for it
   */
  public void showFileToUser(File file) {
    FileAssociation association = FileAssociation.get(file, "Open", owner);
    if (association!=null)
      association.execute(file);
  }

  /**
   * A sub-class can show a chart to the user with this method
   */
  public final void showChartToUser(Chart chart) {
    showComponentToUser(chart);
  }

  /**
   * A sub-class can show a Java Swing component to the user with this method
   */
  public void showComponentToUser(JComponent component) {

    // open a non-modal dialog
    windowManager.openNonModalDialog(getClass().getName()+"#component",getName(), WindowManager.INFORMATION_MESSAGE,component,Action2.okOnly(),owner);

    // done
  }

  /**
   * Show annotations containing text and references to Gedcom objects
   */
  public final void showAnnotationsToUser(Gedcom gedcom, String msg, List annotations) {

    // prepare content
    JPanel content = new JPanel(new BorderLayout());
    content.add(BorderLayout.NORTH, new JLabel(msg));
    content.add(BorderLayout.CENTER, new JScrollPane(new ContextListWidget(gedcom, annotations)));

    // open a non-modal dialog
    windowManager.openNonModalDialog(getClass().getName()+"#items",getName(),WindowManager.INFORMATION_MESSAGE,content,Action2.okOnly(),owner);

    // done
  }

  /**
   * A sub-class can open a browser that will show the given URL with this method
   */
  public final void showBrowserToUser(URL url) {
    FileAssociation.open(url, owner);
  }

  /**
   * A sub-class can ask the user for an entity (e.g. Individual) with this method
   * @param msg a message for letting the user know what and why he's choosing
   * @param gedcom to use
   * @param tag tag of entities to show for selection (e.g. Gedcom.INDI)
   */
  public final Entity getEntityFromUser(String msg, Gedcom gedcom, String tag) {

    SelectEntityWidget select = new SelectEntityWidget(gedcom, tag, null);

    // preselect something?
    Entity entity = gedcom.getEntity(registry.get("select."+tag, (String)null));
    if (entity!=null)
      select.setSelection(entity);

    // show it
    int rc = windowManager.openDialog("select."+tag,getName(),WindowManager.QUESTION_MESSAGE,new JComponent[]{new JLabel(msg),select},Action2.okCancel(),owner);
    if (rc!=0)
      return null;

    // remember selection
    Entity result = select.getSelection();
    if (result==null)
      return null;
    registry.put("select."+result.getTag(), result.getId());

    // done
    return result;
  }

// could do that too - simply show a component to the user
//
//  /**
//   * A sub-class can query the user to choose a value that is somehow represented by given component
//   */
//  public final boolean getValueFromUser(JComponent options) {
//    int rc = windowManager.openDialog(null, getName(), WindowManager.QUESTION_MESSAGE, new JComponent[]{options}, Action2.okCancel(), owner);
//    return rc==0;
//  }

  /**
   * A sub-class can query the user for a selection of given choices with this method
   */
  public final Object getValueFromUser(String msg, Object[] choices, Object selected) {

    ChoiceWidget choice = new ChoiceWidget(choices, selected);
    choice.setEditable(false);

    int rc = windowManager.openDialog(null,getName(),WindowManager.QUESTION_MESSAGE,new JComponent[]{new JLabel(msg),choice},Action2.okCancel(),owner);

    return rc==0 ? choice.getSelectedItem() : null;
  }

  /**
   * A sub-class can query the user for a text value with this method. The value
   * that was selected the last time is automatically suggested.
   */
  public final String getValueFromUser(String key, String msg) {
    return getValueFromUser(key, msg, new String[0]);
  }

  /**
   * A sub-class can query the user for a text value with this method. The value
   * that was selected the last time is automatically suggested.
   */
  public final String getValueFromUser(String key, String msg, String[] defaultChoices) {

    // try to find previously entered choices
    if (key!=null) {
      key = getClass().getName()+"."+key;
      String[] presets = registry.get(key, (String[])null);
      if (presets != null)
        defaultChoices = presets;
    }

    // show 'em
    ChoiceWidget choice = new ChoiceWidget(defaultChoices, defaultChoices.length>0 ? defaultChoices[0] : "");
    int rc = windowManager.openDialog(null,getName(),WindowManager.QUESTION_MESSAGE,new JComponent[]{new JLabel(msg),choice},Action2.okCancel(),owner);
    String result = rc==0 ? choice.getText() : null;

    // Remember?
    if (key!=null&&result!=null&&result.length()>0) {
      List values = new ArrayList(defaultChoices.length+1);
      values.add(result);
      for (int d=0;d<defaultChoices.length&&d<20;d++)
        if (!result.equalsIgnoreCase(defaultChoices[d]))
          values.add(defaultChoices[d]);
      registry.put(key, values);
    }

    // Done
    return result;
  }

  /**
   * A sub-class can query the user for input to given options
   */
  public final boolean getOptionsFromUser(String title, Object options) {

    // grab options by introspection
    List os = PropertyOption.introspect(options);

    // calculate a logical prefix for this options object (strip packages and enclosing type info)
    String prefix = options.getClass().getName();

    int i = prefix.lastIndexOf('.');
    if (i>0) prefix = prefix.substring(i+1);

    i = prefix.lastIndexOf('$');
    if (i>0) prefix = prefix.substring(i+1);

    // restore parameters
    Registry r = new Registry(registry, prefix);
    Iterator it = os.iterator();
    while (it.hasNext()) {
      PropertyOption option  = (PropertyOption)it.next();
      option.restore(r);

      // translate the options as a courtesy now - while options do try
      // to localize the name they base that on a properties file in the
      // same package as the instance - problem is that this won't work
      // with our special way of resolving i18n in reports
      String oname = translate(prefix+"."+option.getName());
      if (oname.length()>0) option.setName(oname);

    }

    // show to user and check for non-ok
    OptionsWidget widget = new OptionsWidget(title, os);
    int rc = windowManager.openDialog(null, getName(), WindowManager.QUESTION_MESSAGE, widget, Action2.okCancel(), owner);
    if (rc!=0)
      return false;

    // save parameters
    widget.stopEditing();
    it = os.iterator();
    while (it.hasNext())
      ((Option)it.next()).persist(r);

    // done
    return true;
  }

  /**
   * A sub-class can query the user for a simple yes/no selection with
   * this method.
   * @param msg the message explaining to the user what he's choosing
   * @param option one of OPTION_YESNO, OPTION_OKCANCEL, OPTION_OK
   */
  public final boolean getOptionFromUser(String msg, int option) {
    return 0==getOptionFromUser(msg, OPTION_TEXTS[option]);
  }

  /**
   * Helper method that queries the user for yes/no input
   */
  private int getOptionFromUser(String msg, String[] actions) {

    Action[] as  = new Action[actions.length];
    for (int i=0;i<as.length;i++)
      as[i]  = new Action2(actions[i]);

    return windowManager.openDialog(null, getName(), WindowManager.QUESTION_MESSAGE, msg, as, owner);

  }

  /**
   * Sub-classes that are accompanied by a [ReportName].properties file
   * containing simple key=value pairs can lookup internationalized
   * text-values with this method.
   * @param key the key to lookup in [ReportName].properties
   */
  public final String translate(String key) {
    return translate(key, (Object[])null);
  }

  /**
   * Sub-classes that are accompanied by a [ReportName].properties file
   * containing simple key=value pairs can lookup internationalized
   * text-values with this method.
   * @param key the key to lookup in [ReportName].properties
   * @param value an integer value to replace %1 in value with
   */
  public final String translate(String key, int value) {
    return translate(key, new Integer(value));
  }

  /**
   * Sub-classes that are accompanied by a [ReportName].properties file
   * containing simple key=value pairs can lookup internationalized
   * text-values with this method.
   * @param key the key to lookup in [ReportName].properties
   * @param value an object value to replace %1 in value with
   */
  public final String translate(String key, Object value) {
    return translate(key, new Object[]{value});
  }

  /**
   * Sub-classes that are accompanied by a [ReportName].properties file
   * containing simple key=value pairs can lookup internationalized
   * text-values with this method.
   * @param key the key to lookup in [ReportName].properties
   * @param values an array of values to replace %1, %2, ... in value with
   */
  public String translate(String key, Object[] values) {

    Resources resources = getResources();
    if (resources==null)
      return key;

    // look it up in language
    String result = null;
    if (lang!=null)
      result = resources.getString(key+'.'+lang, values, false);

    // fallback if necessary
    if (result==null)
      result = resources.getString(key, values, true);

    // done
    return result;
  }

  /**
   * Filename
   */
  public void putFile(File setFile) {
    // setFile would make it appear as report option
    file = setFile;
  }

  public File getFile() {
    return file;
  }

  /**
   * Type name (name without packages)
   */
  private String getTypeName() {
    String rtype = getClass().getName();
    while (rtype.indexOf('.') >= 0)
      rtype = rtype.substring(rtype.indexOf('.')+1);
    return rtype;
  }

  /**
   * Access to report properties
   */
  protected Resources getResources() {
    if (resources==null) {
      // initialize resources with old way of pulling from .properties file
      resources = new Resources(getClass().getResourceAsStream(getTypeName()+".properties"));
      // check if new style resources are available from .java src
      try {
        // ... checking filesystem in developer mode, resource otherwise
        File reports = new File("./src/report");
        String src = getClass().getName().replace('.', '/')+".java";
        InputStream in = (reports.exists()&&reports.isDirectory()) ?
            new FileInputStream(new File(reports, src)) :
            getClass().getResourceAsStream(src);
        resources.load(in);
      } catch (IOException e) {
        // ignore
      }
    }
    return resources;
  }

  /**
   * Creates an indent for text outputs. The method supports several levels
   * and front strings.
   *
   * @param level for indent (can be thought of columns)
   * @param spacesPerLevel space character between one level
   * @param prefix String in front of the indented text (can be null)
   */
    public final String getIndent(int level, int spacesPerLevel, String prefix) {
        String oneLevel = "";
        while(oneLevel.length() != spacesPerLevel)
            oneLevel=oneLevel+" ";
        StringBuffer buffer = new StringBuffer(256);
        while (--level>0) {
            buffer.append(oneLevel);
        }
        if (prefix!=null)
          buffer.append(prefix);
        return buffer.toString();
    }

    /**
     * Creates an empty String for text output. Spaces per Level are taken from
     * OPTIONS.getIndentPerLevel()
     */
    public final String getIndent(int level) {
      return getIndent(level, OPTIONS.getIndentPerLevel(), null);
    }


  /**
   * Aligns a simple text for text outputs.
   * @param txt the text to align
   * @param length the length of the result
   * @param alignment one of LEFT,CENTER,RIGHT
   */
  public final String align(String txt, int length, int alignment) {

    // check txt length
    int n = txt.length();
    if (n>length)
      return txt.substring(0, length);
    n = length-n;

    // prepare result
    StringBuffer buffer = new StringBuffer(length);

    int before,after;
    switch (alignment) {
      default:
      case ALIGN_LEFT:
        before = 0;
        break;
      case ALIGN_CENTER:
        before = (int)(n*0.5F);
        break;
      case ALIGN_RIGHT:
        before = n;
        break;
    }
    after = n-before;

    // space before
    for (int i=0; i<before; i++)
      buffer.append(' ');

    // txt
    buffer.append(txt);

    // space after
    for (int i=0; i<after; i++)
      buffer.append(' ');

    // done
    return buffer.toString();
  }

  /**
   * Returns the name of a report - this by default is the value of key "name"
   * in the file [ReportName].properties. A report has to override this method
   * to provide a localized name if that file doesn't exist.
   * @return name of the report
   */
  public String getName() {
    String name =  translate("name");
    if (name.length()==0||name.equals("name")) name = getTypeName();
    return name;
  }

  /**
   * Returns the author of a report - this by default is the value of key "author"
   * in the file [ReportName].properties. A report has to override this method
   * to provide the author if that file doesn't exist.
   * @return name of the author
   */
  public String getAuthor() {
    return translate("author");
  }

  /**
   * Returns the version of a report - this by default is the value of key "version"
   * in the file [ReportName].properties. A report has to override this method
   * to provide the version if that file doesn't exist.
   * @return version of report
   */
  public String getVersion() {
    return translate("version");
  }

  private final static Pattern PATTERN_CVS_DATE  = Pattern.compile("\\$"+"Date: (\\d\\d\\d\\d)/(\\d\\d)/(\\d\\d)( \\d\\d:\\d\\d:\\d\\d) *\\$"); // don't user [dollar]Date to avoid keywords substitution here :)

  /**
   * Returns the last update tag  - this by default is the value of key "date"
   * in the file [ReportName].properties.
   */
  public String getLastUpdate() {
    // check for updated key
    String result = translate("updated");
      if ("updated".equals(result))
      return null;
    // look for cvs date - grab date and time
    Matcher cvsdata = PATTERN_CVS_DATE.matcher(result);
    if (cvsdata.matches()) try {
      // we've got a "yyyy/mm/dd hh:mm:ss"
      result = new PointInTime(cvsdata.group(1)+cvsdata.group(2)+cvsdata.group(3)) + cvsdata.group(4);
    } catch (GedcomException e) {
    }
    // done - either whatever was found or beautified cvs keyword value
    return result;
  }

  /**
   * Returns information about a report - this by default is the value of key "info"
   * in the file [ReportName].properties. A report has to override this method
   * to provide localized information if that file doesn't exist.
   * @return information about report
   */
  public String getInfo() {
    return translate("info");
  }

  /**
   * Called by GenJ to start this report's execution - can be overriden by a user defined report.
   * @param context normally an instance of type Gedcom but depending on
   *    accepts() could also be of type Entity or Property
   */
  public void start(Object context) throws Throwable {
    try {
      getStartMethod(context).invoke(this, new Object[]{ context });
    } catch (Throwable t) {
      String msg = "can't run report on input";
      if (t instanceof InvocationTargetException)
        throw ((InvocationTargetException)t).getTargetException();
      throw t;
    }
  }

  public void start(Object context, Object parameter) throws Throwable {
	  Method startMethod = getStartMethod(context,parameter);
	  // if start(context,parameter) doesn't exists, try start(context)
	  if (startMethod == null) 
		  start(context);
	  else
		try {
	      getStartMethod(context,parameter).invoke(this, new Object[]{ context , parameter});
	    } catch (Throwable t) {
	      String msg = "can't run report on input";
	      if (t instanceof InvocationTargetException)
	        throw ((InvocationTargetException)t).getTargetException();
	      throw t;
	    }
	  }


  /**
   * Tells wether this report doesn't change information in the Gedcom-file
   */
  public boolean isReadOnly() {
    return true;
  }

  /**
   * Returns true if this report uses STDOUT
   */
  public boolean usesStandardOut() {
    return true;
  }

    /**
     * Whether the report allows to be run on a given context - default
     * checks for methods called
     * <il>
     *  <li>start(Gedcom|Object)
     *  <li>start(Property)
     *  <li>start(Entity)
     *  <li>start(Indi[])
     *  <li>...
     * </il>
     * @return title of action for given context or null for n/a
     */
    public Object accepts(Object context) {
      return getStartMethod(context)!=null ? getName() : null;
    }

    /**
     * resolve start method to use for given argument type
     */
    /*package*/ Method getStartMethod(Object context) {

      // check for what this report accepts
      try {
        Method[] methods = getClass().getDeclaredMethods();
        for (int m = 0; m < methods.length; m++) {
          // needs to be named start
          if (!methods[m].getName().equals("start")) continue;
          // make sure its a one-arg
          Class[] params = methods[m].getParameterTypes();
          if (params.length!=1) continue;
          // keep it
          Class param = params[0];
          if (param.isAssignableFrom(context.getClass()))
            return methods[m];
          // try next
        }
      } catch (Throwable t) {
      }
      // n/a
      return null;
    }

    /*package*/ Method getStartMethod(Object context, Object parameter) {

        // check for what this report accepts
        try {
          Method[] methods = getClass().getDeclaredMethods();
          for (int m = 0; m < methods.length; m++) {
            // needs to be named start
            if (!methods[m].getName().equals("start")) continue;
            // make sure its a two-arg
            Class[] params = methods[m].getParameterTypes();
            if (params.length!=2) continue;
            // keep it
            Class param = params[0];
            if (param.isAssignableFrom(context.getClass()))
              return methods[m];
            // try next
          }
        } catch (Throwable t) {
        }
        // n/a:
        return null;
      }

    /**
     * Represents the report category.
     */
    public static class Category
    {
        private String name;
        private ImageIcon image;

        public Category(String name, ImageIcon image) {
            this.name = name;
            this.image = image;
        }

        public String getName() {
            return name;
        }

        public ImageIcon getImage() {
            return image;
        }
    }

    private static class Categories extends TreeMap {
        void add(Category category) {
            put(category.getName(), category);
        }
    }

    /**
     * Filters files using a specified extension.
     */
    private class FileExtensionFilter extends FileFilter {

        private String extension;

        public FileExtensionFilter(String extension) {
            this.extension = extension.toLowerCase();
        }

        /**
         * Returns true if file name has the right extension.
         */
        public boolean accept(File f) {
            if (f == null)
                return false;
            if (f.isDirectory())
                return true;
            return f.getName().toLowerCase().endsWith("." + extension);
        }

        public String getDescription() {
            return extension.toUpperCase() + " files";
        }
    }


} //Report
