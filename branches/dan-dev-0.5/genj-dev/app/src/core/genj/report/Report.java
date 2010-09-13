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
 * $Revision: 1.142 $ $Author: nmeier $ $Date: 2010-01-28 02:51:13 $
 */
package genj.report;

import genj.common.SelectEntityWidget;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.time.PointInTime;
import genj.option.Option;
import genj.option.OptionsWidget;
import genj.option.PropertyOption;
import genj.util.EnvironmentChecker;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.DialogHelper;

import java.awt.Component;
import java.awt.Graphics;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;


/**
 * Base-class of all GenJ reports. Sub-classes that are compiled
 * and available in ./report will be loaded by GenJ automatically
 * and can be reloaded during runtime.
 * 
 * categories: text,utility,chart,graph
 */
public abstract class Report implements Cloneable {
  
  private final static PrintWriter NUL = new PrintWriter(new OutputStream() { @Override public void write(int b) { }} );

  protected final static Logger LOG = Logger.getLogger("genj.report");

  protected final static Icon DEFAULT_ICON = new Icon() {
    public int getIconHeight() {
      return 16;
    }
    public int getIconWidth() {
      return 16;
    }
    public void paintIcon(Component c, Graphics g, int x, int y) {
    }
  };

  /** global report options */
  protected Options OPTIONS = Options.getInstance();

  /** options */
  protected final static int
    OPTION_YESNO    = 0,
    OPTION_OKCANCEL = 1,
    OPTION_OK       = 2;

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
  protected Registry registry;

  /** language we're trying to use */
  private final static String userLanguage = Locale.getDefault().getLanguage();

  /** translation resources common to all reports */
  static final Resources COMMON_RESOURCES = Resources.get(Report.class);

  /** translation texts */
  private Resources resources;

  /** options */
  private List<Option> options;

  /** image */
  private Icon icon;

  /** file */
  private File file;
  
  /** outs */
  private PrintWriter out = NUL;
  private Component owner = null;
  

  /**
   * Constructor
   */
  protected Report() {
    registry = new Registry(Registry.get(Report.class), getClass().getName());
  }

  /**
   * integration - log a message
   */
  /*package*/ void log(String txt) {
    getOut().println(txt);
  }
  
  /**
   * Get a logging out
   */
  public PrintWriter getOut() {
    return out;
  }
  
  /** 
   * Set logging out (this is a thread local operation)
   */
  /*package*/ void setOut(PrintWriter set) {
    out = set;
  }

  /** 
   * Set owner (this is a thread local operation)
   */
  /*package*/ void setOwner(Component set) {
    owner = set;
  }

  /**
   * Store report's options
   */
  /*package*/ void saveOptions() {
    // if known
    if (options==null)
      return;
    // save 'em
    for (Option option : options)
      if (option instanceof PropertyOption)
        ((PropertyOption)option).persist(registry);
      else
        option.persist();
    // done
  }
  
  protected Registry getRegistry() {
    return registry;
  }

  /**
   * Get report's options
   */
  public final List<? extends Option> getOptions() {

    // already calculated
    if (options!=null)
      return options;

    options = new ArrayList<Option>();
    
    // calculate options
    // 20091205 going recursive here is new to support Przemek's case of settings on report's components
    List<PropertyOption> props = PropertyOption.introspect(this, true);

    // restore options values
    for (PropertyOption prop : props) {
      // restore old value
      prop.restore(registry);
      // options do try to localize the name and tool tip based on a properties file
      // in the same package as the instance - problem is that this
      // won't work with our special way of resolving i18n in reports
      // so we have to do that manually
      String oname = translateOption(prop.getProperty());
      if (oname.length()>0) prop.setName(oname);
      String toolTipKey = prop.getProperty() + ".tip";
      String toolTip = translateOption(toolTipKey);
      if (toolTip.length() > 0 && !toolTip.equals(toolTipKey))
        prop.setToolTip(toolTip);
      // set default category
      if (prop.getCategory()==null)
        prop.setCategory(getName());
      else
        prop.setCategory(translateOption(prop.getCategory()));

      // remember
      options.add(prop);
    }
    
    // done
    return options;
  }
  
  /**
   * An image
   */
  public Icon getIcon() {
    
    // got it?
    if (icon!=null)
      return icon;

    // find category in report settings
    String cat = translate("category");
    if (cat.equals("category")||cat.length()==0) {
      icon = DEFAULT_ICON;
    } else {
      // resolve an image
      String file = "Category"+Character.toUpperCase(cat.charAt(0))+cat.substring(1)+".png";
      InputStream in = null;
      try {
        in = Report.class.getResourceAsStream(file);
        icon = new genj.util.swing.ImageIcon(file, in);
      } catch (Throwable t) {
        icon = DEFAULT_ICON;
      } finally {
        if (in!=null) try { in.close(); } catch (IOException e) {}
      }
    }
    
    // done
    return icon;
  }

  /**
   * Returns the report category
   */
  public final String getCategory() {
    // find category in report settings
    String cat = translate("category");
    if (cat.equals("category")||cat.length()==0)
      return "";
    
    // try to translate
    String result = COMMON_RESOURCES.getString("category."+cat, false);
    if (result==null) {
      LOG.fine("report's category "+cat+" doesn't exist");
      return COMMON_RESOURCES.getString("category.utility");
    }    
    
    return result;
  }

  /**
   * When a report is executed all its text output is gathered and
   * shown to the user (if run through ReportView). A sub-class can
   * flush the current log with this method.
   */
  public final void flush() {
    getOut().flush();
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

    // show filechooser
    String dir = registry.get("file", EnvironmentChecker.getProperty("user.home", ".", "looking for report dir to let the user choose from"));
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
      rc = DialogHelper.openDialog(title, DialogHelper.WARNING_MESSAGE, ReportView.RESOURCES.getString("report.file.overwrite"), Action2.yesNo(), owner);
      if (rc!=0)
        return null;
    }

    // keep it
    registry.put("file", result.getParent().toString());
    return result;
  }

  /**
   * An implementation of Report can ask the user for a directory with this method.
   */
  public File getDirectoryFromUser(String title, String button) {

    // show directory chooser
    String dir = registry.get("dir", EnvironmentChecker.getProperty("user.home", ".", "looking for report dir to let the user choose from"));
    JFileChooser chooser = new JFileChooser(dir);
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setDialogTitle(title);
    int rc = chooser.showDialog(owner,button);

    // check result
    File result = chooser.getSelectedFile();
    if (rc!=JFileChooser.APPROVE_OPTION||result==null)
      return null;

    // keep it
    registry.put(dir, result.toString());
    return result;
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
    int rc = DialogHelper.openDialog(getName(),DialogHelper.QUESTION_MESSAGE,new JComponent[]{new JLabel(msg),select},Action2.okCancel(),owner);
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
//    int rc = WindowManager.openDialog(null, getName(), WindowManager.QUESTION_MESSAGE, new JComponent[]{options}, Action2.okCancel(), owner.get());
//    return rc==0;
//  }

  /**
   * A sub-class can query the user for a selection of given choices with this method
   */
  public final Object getValueFromUser(String msg, Object[] choices, Object selected) {

    ChoiceWidget choice = new ChoiceWidget(choices, selected);
    choice.setEditable(false);

    int rc = DialogHelper.openDialog(getName(),DialogHelper.QUESTION_MESSAGE,new JComponent[]{new JLabel(msg),choice},Action2.okCancel(),owner);

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
      String[] presets = registry.get(key, (String[])null);
      if (presets != null)
        defaultChoices = presets;
    }

    // show 'em
    ChoiceWidget choice = new ChoiceWidget(defaultChoices, defaultChoices.length>0 ? defaultChoices[0] : "");
    int rc = DialogHelper.openDialog(getName(),DialogHelper.QUESTION_MESSAGE,new JComponent[]{new JLabel(msg),choice},Action2.okCancel(),owner);
    String result = rc==0 ? choice.getText() : null;

    // Remember?
    if (key!=null&&result!=null&&result.length()>0) {
      List<String> values = new ArrayList<String>(defaultChoices.length+1);
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
    List<PropertyOption> os = PropertyOption.introspect(options);

    // calculate a logical prefix for this options object (strip packages and enclosing type info)
    String prefix = options.getClass().getName();

    int i = prefix.lastIndexOf('.');
    if (i>0) prefix = prefix.substring(i+1);

    i = prefix.lastIndexOf('$');
    if (i>0) prefix = prefix.substring(i+1);

    // restore parameters
    for (PropertyOption option : os) {
      
      option.restore(registry);

      // translate the options as a courtesy now - while options do try
      // to localize the name they base that on a properties file in the
      // same package as the instance - problem is that this won't work
      // with our special way of resolving i18n in reports
      String oname = translate(prefix+"."+option.getName());
      if (oname.length()>0) option.setName(oname);
    }

    // show to user and check for non-ok
    OptionsWidget widget = new OptionsWidget(title, os);
    int rc = DialogHelper.openDialog(getName(), DialogHelper.QUESTION_MESSAGE, widget, Action2.okCancel(), owner);
    if (rc!=0)
      return false;

    // save parameters
    widget.stopEditing();
    for (PropertyOption option : os)
      option.persist(registry);

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

    return DialogHelper.openDialog(getName(), DialogHelper.QUESTION_MESSAGE, msg, as, owner);

  }

  /**
   * Translates the name of a report option. First tries to translate the usual way,
   * by calling translate(). If this is unsuccessful, tries to use properties file from GenJ report package.
   * @param key  property name to look up
   */
  public String translateOption(String key)
  {
	  String result = translate(key);
	  if (result.equals(key))
	  {
		  String optionKey = "option." + key;
		  String optionName = COMMON_RESOURCES.getString(optionKey);
		  if (!optionName.equals(optionKey))
			  result = optionName;
	  }
	  return result;
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
   * @param values values to replace %[0..] in resource strings
   */
  public final String translate(String key, Object... values) {
    return translate(key, (Locale)null, values);
  }
  
  public final String translate(String key, Locale locale, Object... values) {

    Resources resources = getResources();
    if (resources==null)
      return key;

    // look it up in language
    String result = null;
    String lang = locale!=null ? locale.getLanguage() : userLanguage;
    if (lang!=null) {
      String locKey = key+'.'+lang;
      result = resources.getString(locKey, values);
      if (result!=locKey)
        return result;
    }

    // fallback if necessary
    result = resources.getString(key, values);

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
        resources.load(in, true);
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
    public static String getIndent(int level, int spacesPerLevel, String prefix) {
        StringBuffer oneLevel = new StringBuffer();
        while(oneLevel.length() != spacesPerLevel)
            oneLevel.append(" ");
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
  public static String align(String txt, int length, int alignment) {

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
  public Object start(Object context) throws Throwable {
    try {
      return getStartMethod(context).invoke(this, new Object[]{ context });
    } catch (InvocationTargetException t) {
      throw ((InvocationTargetException)t).getTargetException();
    }
  }

  /**
   * Tells wether this report doesn't change information in the Gedcom-file
   */
  public boolean isReadOnly() {
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
    public String accepts(Object context) {
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
          Class<?>[] params = methods[m].getParameterTypes();
          if (params.length!=1) continue;
          // keep it
          Class<?> param = params[0];
          if (param.isAssignableFrom(context.getClass()))
            return methods[m];
          // try next
        }
      } catch (Throwable t) {
      }
      // n/a
      return null;
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
