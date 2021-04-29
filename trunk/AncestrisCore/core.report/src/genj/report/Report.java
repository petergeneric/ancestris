/**
 * Ancestris
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 * Copyright (C) 2003 - 2018 Frederic Lapeyre <frederic@ancestris.org>
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
 *
 * $Revision: 1.142 $ $Author: nmeier $ $Date: 2010-01-28 02:51:13 $
 */
package genj.report;

import ancestris.api.search.SearchCommunicator;
import ancestris.core.TextOptions;
import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.resources.Images;
import ancestris.gedcom.GedcomDirectory;
import ancestris.util.swing.DialogManager;
import ancestris.util.swing.FileChooserBuilder;
import ancestris.util.swing.SelectEntityPanel;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.time.PointInTime;
import genj.option.Option;
import genj.option.OptionsWidget;
import genj.option.PropertyOption;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.Resources.ResourcesProvider;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.ImageIcon;
import java.awt.Component;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;

/**
 * Base-class of all GenJ reports. Sub-classes that are compiled and available
 * in ./report will be loaded by GenJ automatically and can be reloaded during
 * runtime.
 *
 * categories: text,utility,chart,graph
 */
public abstract class Report implements Cloneable, ResourcesProvider {

    private final static PrintWriter NUL = new PrintWriter(new OutputStream() {
        @Override
        public void write(int b) {
        }
    });

    protected final static Logger LOG = Logger.getLogger("ancestris.report");

    /**
     * global report options
     */
    protected TextOptions OPTIONS = TextOptions.getInstance();

    /**
     * options
     */
    protected final static int OPTION_YESNO = 0,
            OPTION_OKCANCEL = 1,
            OPTION_OK = 2;

    private final static String[][] OPTION_TEXTS = {
        new String[]{AbstractAncestrisAction.TXT_YES, AbstractAncestrisAction.TXT_NO},
        new String[]{AbstractAncestrisAction.TXT_OK, AbstractAncestrisAction.TXT_CANCEL},
        new String[]{AbstractAncestrisAction.TXT_OK}
    };
    private final static int[] OPTION_TYPE = {
        DialogManager.YES_NO_OPTION,
        DialogManager.OK_CANCEL_OPTION,
        DialogManager.OK_ONLY_OPTION
    };

    /**
     * alignment options
     */
    protected final static int ALIGN_LEFT = 0,
            ALIGN_CENTER = 1,
            ALIGN_RIGHT = 2;

    /**
     * one report for all reports
     */
    protected Registry registry;

    /**
     * language we're trying to use
     */
    private final static String USER_LANGUAGE = Locale.getDefault().getLanguage();

    /**
     * translation resources common to all reports
     */
    private static final Resources COMMON_RESOURCES = ReportResources.get(Report.class);

    private final Map<Locale, Resources> LOCALE_2_RESOURCES = new HashMap<>(3);

    /**
     * options
     */
    private List<Option> options;

    /**
     * image
     */
    private Icon icon;

    /**
     * outs
     */
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
     * @return writer
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
    /*package*/ public void saveOptions() {
        // if known
        if (options == null) {
            return;
        }
        // save 'em
        for (Option option : options) {
            if (option instanceof PropertyOption) {
                ((PropertyOption) option).persist(registry);
            } else {
                option.persist();
            }
        }
        // done
    }

    protected Registry getRegistry() {
        return registry;
    }

    /**
     * Get report's options
     */
    public final List<? extends Option> getOptions() {
        return getOptions(false);
    }

    /**
     * Get report options. if forceRead is true, cache is cleared to ensure that
     * options are read from file
     *
     * @param forceRead
     * @return
     */
    public final List<? extends Option> getOptions(boolean forceRead) {

        if (forceRead) {
            options = null;
        }
        // already calculated
        if (options != null) {
            return options;
        }

        options = new ArrayList<>();

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
            if (oname.length() > 0) {
                prop.setName(oname);
            }
            String toolTipKey = prop.getProperty() + ".tip";
            String toolTip = translateOption(toolTipKey);
            if (toolTip.length() > 0 && !toolTip.equals(toolTipKey)) {
                prop.setToolTip(toolTip);
            }
            // set default category
            if (prop.getCategory() == null) {
                prop.setCategory(getName());
            } else {
                prop.setCategory(translateOption(prop.getCategory()));
            }

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
        if (icon != null) {
            return icon;
        }
        icon = getCategory().getImage();

        // done
        return icon;
    }

    /**
     * Returns the report category. If the category is not defined in the
     * report's properties file, the category is set to "Other".
     */
    public Category getCategory() {
        //XXX: category names must not be translated
        String name = translateGUI("category");
        if (name.equals("category") || name.length() == 0) {
            return Category.DEFAULT_CATEGORY;
        }
      //XXX: see how to set translated display text for non "standard" categories
        // ie new categories set by a report. Maybe we could add this in ReportLoader
        return Category.get(name);
    }

    /**
     * When a report is executed all its text output is gathered and shown to
     * the user (if run through ReportView). A sub-class can flush the current
     * log with this method.
     */
    public final void flush() {
        getOut().flush();
    }

    /**
     * When a report is executed all its text output is gathered and shown to
     * the user (if run through ReportView). A sub-class can append a new line
     * to the current log with this method.
     */
    public final void println() {
        println("");
    }

    /**
     * When a report is executed all its text output is gathered and shown to
     * the user (if run through ReportView). A sub-class can append the
     * text-representation of an object (toString) to the current log with this
     * method.
     */
    public final void println(Object o) {
        // nothing to do?
        if (o == null) {
            return;
        }
        // Our hook into checking for Interrupt
        if (Thread.interrupted()) {
            throw new RuntimeException(new InterruptedException());
        }
        // Append it
        log(o.toString());
        // Done
    }

    /**
     * When a report is executed all its text output is gathered and shown to
     * the user (if run through ReportView). A sub-class can let the user know
     * about an exception with this method. The information about the exception
     * is appended in text-form to the current log.
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
     * @param title file dialog title
     * @param button file dialog OK button text
     * @param askForOverwrite whether to confirm overwriting files
     * @param extension extension of files to display
     * @return
     */
    public File getFileFromUser(String title, String button, boolean askForOverwrite, String extension) {

    // show filechooser
        File file = new FileChooserBuilder(Report.class.getCanonicalName()+"file")
                .setFilesOnly(true)
                .setDefaultBadgeProvider()
                .setTitle(title)
                .setApproveText(button)
                .setDefaultExtension((extension != null && !extension.isEmpty()) ? extension : FileChooserBuilder.getTextFilter().getExtensions()[0])
                .setFileFilter(new FileExtensionFilter(extension))
                .setAcceptAllFileFilterUsed(true)
                .setFileHiding(true)
                .setParent(owner)
                .setDefaultDirAsReportDirectory()
                .showSaveDialog(askForOverwrite);
        
        if (file != null) {
            registry.put("file", file.getParent());
        }
        return file;
    }

    /**
     * An implementation of Report can ask the user for a directory with this
     * method.
     */
    public File getDirectoryFromUser(String title, String button) {

        // show directory chooser
        File file = new FileChooserBuilder(Report.class.getCanonicalName()+"dir")
                .setDirectoriesOnly(true)
                .setDefaultBadgeProvider()
                .setTitle(title)
                .setApproveText(button)
                .setFileHiding(true)
                .setParent(owner)
                .setDefaultDirAsReportDirectory()
                .showOpenDialog();

        if (file != null) {
            registry.put("dir", file.toString());
        }
        return file;
    }

    /**
     * A sub-class can ask the user for an entity (e.g. Individual) with this
     * method
     *
     * @param msg a message for letting the user know what and why he's choosing
     * @param gedcom to use
     * @param tag tag of entities to show for selection (e.g. Gedcom.INDI)
     */
    public final Entity getEntityFromUser(String msg, Gedcom gedcom, String tag) {
        return getEntityFromUser(msg, gedcom, tag, null);
    }
    
    public final Entity getEntityFromUser(String msg, Gedcom gedcom, String tag, Entity defaultEntity) {

        // Selection box
        if (defaultEntity == null) {
            defaultEntity = gedcom.getEntity(registry.get("select." + tag, (String) null));
        }
        SelectEntityPanel select = new SelectEntityPanel(gedcom, tag, COMMON_RESOURCES.getString("choose.entity", Gedcom.getName(tag)), defaultEntity);
        if (DialogManager.OK_OPTION != DialogManager.create(getName(), select).setMessageType(DialogManager.QUESTION_MESSAGE).setOptionType(DialogManager.OK_CANCEL_OPTION)
                .setDialogId("report.entityfromuser").show()) {
            return null;
        }

        // remember selection
        Entity result = select.getSelection();
        if (result == null) {
            return null;
        }
        registry.put("select." + result.getTag(), result.getId());

        // done
        return result;
    }

// could do that too - simply show a component to the user
//
//  /**
//   * A sub-class can query the user to choose a value that is somehow represented by given component
//   */
//  public final boolean getValueFromUser(JComponent options) {
//    int rc = WindowManager.openDialog(null, getName(), WindowManager.QUESTION_MESSAGE, new JComponent[]{options}, AbstractAncestrisAction.okCancel(), owner.get());
//    return rc==0;
//  }
    /**
     * A sub-class can query the user for a selection of given choices with this
     * method
     */
    public final Object getValueFromUser(String msg, Object[] choices, Object selected) {

        ChoiceWidget choice = new ChoiceWidget(choices, selected);
        choice.setEditable(false);

        // show it
        if (DialogManager.create(getName(), new JComponent[]{new JLabel(msg), choice})
                .setMessageType(DialogManager.QUESTION_MESSAGE)
                .setOptionType(DialogManager.OK_CANCEL_OPTION)
                .setDialogId("report.valuefromuser")
                .show() != DialogManager.OK_OPTION) {
            return null;
        }

        return choice.getSelectedItem();
    }

    /**
     * A sub-class can query the user for a text value with this method. The
     * value that was selected the last time is automatically suggested.
     */
    public final String getValueFromUser(String key, String msg) {
        return getValueFromUser(key, msg, new String[0]);
    }

    /**
     * A sub-class can query the user for a text value with this method. The
     * value that was selected the last time is automatically suggested.
     */
    public final String getValueFromUser(String key, String msg, String[] defaultChoices) {

        // try to find previously entered choices
        if (key != null) {
            String[] presets = registry.get(key, (String[]) null);
            if (presets != null) {
                defaultChoices = presets;
            }
        }

        // show 'em
        ChoiceWidget choice = new ChoiceWidget(defaultChoices, defaultChoices.length > 0 ? defaultChoices[0] : "");
        // show it
        if (DialogManager.create(getName(), new JComponent[]{new JLabel(msg), choice})
                .setMessageType(DialogManager.QUESTION_MESSAGE)
                .setOptionType(DialogManager.OK_CANCEL_OPTION)
                .setDialogId("report.valuefromuser")
                .show() != DialogManager.OK_OPTION) {
            return null;
        }
        String result = choice.getText();

        // Remember?
        if (key != null && result != null && result.length() > 0) {
            List<String> values = new ArrayList<String>(defaultChoices.length + 1);
            values.add(result);
            for (int d = 0; d < defaultChoices.length && d < 20; d++) {
                if (!result.equalsIgnoreCase(defaultChoices[d])) {
                    values.add(defaultChoices[d]);
                }
            }
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
        if (i > 0) {
            prefix = prefix.substring(i + 1);
        }

        i = prefix.lastIndexOf('$');
        if (i > 0) {
            prefix = prefix.substring(i + 1);
        }

        // restore parameters
        for (PropertyOption option : os) {

            option.restore(registry);

      // translate the options as a courtesy now - while options do try
            // to localize the name they base that on a properties file in the
            // same package as the instance - problem is that this won't work
            // with our special way of resolving i18n in reports
            String oname = translateGUI(prefix + "." + option.getName());
            if (oname.length() > 0) {
                option.setName(oname);
            }
        }

        // show to user and check for non-ok
        OptionsWidget widget = new OptionsWidget(title, os);
        Object rc = DialogManager.create(getName(), widget)
                .setOptionType(DialogManager.OK_CANCEL_OPTION)
                .setDialogId("report.optionsfromuser")
                .show();

        if (rc != DialogManager.OK_OPTION) {
            return false;
        }

        // save parameters
        widget.stopEditing();
        for (PropertyOption option : os) {
            option.persist(registry);
        }

        // done
        return true;
    }

    /**
     * A sub-class can query the user for a simple yes/no selection with this
     * method.
     *
     * @param msg the message explaining to the user what he's choosing
     * @param option one of OPTION_YESNO, OPTION_OKCANCEL, OPTION_OK
     */
    public final boolean getOptionFromUser(String msg, int option) {
        Object result = DialogManager.create(getName(), msg)
                .setMessageType(DialogManager.QUESTION_MESSAGE)
                .setOptionType(OPTION_TYPE[option])
                .setDialogId("report.optionsfromuser")
                .show();
        return result == DialogManager.OK_OPTION || result == DialogManager.YES_OPTION;
    }

    /**
     * Translates the name of a report option. First tries to translate the
     * usual way, by calling translate(). If this is unsuccessful, tries to use
     * properties file from GenJ report package.
     *
     * @param key property name to look up
     */
    public String translateOption(String key) {
        String result = translateGUI(key); // use GUI locale for options
        if (result.equals(key)) {
            String optionKey = "option." + key;
            String optionName = COMMON_RESOURCES.getString(optionKey);
            if (!optionName.equals(optionKey)) {
                result = optionName;
            }
        }
        return result;
    }

    /**
     * Sub-classes that are accompanied by a [ReportName].properties file
     * containing simple key=value pairs can lookup internationalized
     * text-values with this method.
     *
     * @param key the key to lookup in [ReportName].properties
     */
    public final String translate(String key) {
        return translate(key, (Object[]) null);
    }

    public final String translateGUI(String key) {
        return translateGUI(key, (Object[]) null);
    }

    /**
     * Sub-classes that are accompanied by a [ReportName].properties file
     * containing simple key=value pairs can lookup internationalized
     * text-values with this method.
     *
     * @param key the key to lookup in [ReportName].properties
     * @param values values to replace %[0..] in resource strings
     */
    public final String translate(String key, Object... values) {
        return translate(key, (Locale) null, values);
    }

    public final String translateGUI(String key, Object... values) {
        return translate(key, Locale.getDefault(), values);
    }

    public final String translate(String key, Locale locale, Object... values) {

        if (locale == null) {
            locale = OPTIONS.getOutputLocale();
        }
        Resources resources = getResources(locale);
        if (resources == null) {
            return key;
        }

        return resources.getString(key, values);

    }

    /**
     * Type name (name without packages)
     */
    private String getTypeName() {
        String rtype = getClass().getName();
        while (rtype.indexOf('.') >= 0) {
            rtype = rtype.substring(rtype.indexOf('.') + 1);
        }
        return rtype;
    }

    /**
     * Access to report properties
     */
    /*protected*/
    public Resources getResources() {
        return getResources(null);
    }
    /*protected*/

    public Resources getResources(Locale locale) {
        Resources resources = LOCALE_2_RESOURCES.get(locale);
        if (resources == null) {
            // initialize resources with old way of pulling from .properties file
            InputStream in = getClass().getResourceAsStream(getTypeName() + ".properties");
            if (in != null) {
                resources = new ReportResources(in, locale);
            }
            // no .properties file, tries Bundle
            if (resources == null) {
                resources = ReportResources.get(this.getClass(), locale);
            }
            LOCALE_2_RESOURCES.put(locale, resources);
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
        final StringBuilder oneLevel = new StringBuilder();
        while (oneLevel.length() != spacesPerLevel) {
            oneLevel.append(" ");
        }
        final StringBuilder buffer = new StringBuilder(256);
        while (--level > 0) {
            buffer.append(oneLevel);
        }
        if (prefix != null) {
            buffer.append(prefix);
        }
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
     *
     * @param txt the text to align
     * @param length the length of the result
     * @param alignment one of LEFT,CENTER,RIGHT
     * @return string aligned.
     */
    public static String align(String txt, int length, int alignment) {

        // check txt length
        int n = txt.length();
        if (n > length) {
            return txt.substring(0, length);
        }
        n = length - n;

        // prepare result
        final StringBuilder buffer = new StringBuilder(length);

        int before, after;
        switch (alignment) {
            default:
            case ALIGN_LEFT:
                before = 0;
                break;
            case ALIGN_CENTER:
                before = (int) (n * 0.5F);
                break;
            case ALIGN_RIGHT:
                before = n;
                break;
        }
        after = n - before;

        // space before
        for (int i = 0; i < before; i++) {
            buffer.append(' ');
        }

        // txt
        buffer.append(txt);

        // space after
        for (int i = 0; i < after; i++) {
            buffer.append(' ');
        }

        // done
        return buffer.toString();
    }

    /**
     * Returns the name of a report - this by default is the value of key "name"
     * in the file [ReportName].properties. A report has to override this method
     * to provide a localized name if that file doesn't exist.
     *
     * @return name of the report
     */
    public String getName() {
        String name = translateGUI("name");
        if (name.length() == 0 || name.equals("name")) {
            name = getTypeName();
        }
        return name;
    }

    /**
     * Returns the short name of a report - this by default is the value of key
     * "name.short" in the file [ReportName].properties. This shortname is
     * intended to be displayed in tabs. A report has to override this method to
     * provide a localized name if that file doesn't exist. If name.short key is
     * not available, revert to {@link #getName() }
     *
     * @return short name of the report
     */
    public String getShortName() {
        String name = translateGUI("name.short");
        if (name.length() == 0 || name.equals("name.short")) {
            name = getName();
        }
        return name;
    }

    /**
     * Returns the author of a report - this by default is the value of key
     * "author" in the file [ReportName].properties. A report has to override
     * this method to provide the author if that file doesn't exist.
     *
     * @return name of the author
     */
    public String getAuthor() {
        return translateGUI("author");
    }

    /**
     * Returns the version of a report - this by default is the value of key
     * "version" in the file [ReportName].properties. A report has to override
     * this method to provide the version if that file doesn't exist.
     *
     * @return version of report
     */
    public String getVersion() {
        return translateGUI("version");
    }

    private final static Pattern PATTERN_CVS_DATE = Pattern.compile("\\$" + "Date: (\\d\\d\\d\\d)/(\\d\\d)/(\\d\\d)( \\d\\d:\\d\\d:\\d\\d) *\\$"); // don't user [dollar]Date to avoid keywords substitution here :)

    /**
     * Returns the last update tag - this by default is the value of key "date"
     * in the file [ReportName].properties.
     */
    public String getLastUpdate() {
        // check for updated key
        String result = translateGUI("updated");
        if ("updated".equals(result)) {
            return null;
        }
        // look for cvs date - grab date and time
        Matcher cvsdata = PATTERN_CVS_DATE.matcher(result);
        if (cvsdata.matches()) {
            try {
                // we've got a "yyyy/mm/dd hh:mm:ss"
                result = new PointInTime(cvsdata.group(1) + cvsdata.group(2) + cvsdata.group(3)) + cvsdata.group(4);
            } catch (GedcomException e) {
            }
        }
        // done - either whatever was found or beautified cvs keyword value
        return result;
    }

    /**
     * Returns information about a report - this by default is the value of key
     * "info" in the file [ReportName].properties. A report has to override this
     * method to provide localized information if that file doesn't exist.
     *
     * @return information about report
     */
    public String getInfo() {
        return translateGUI("info");
    }

    /**
     * Called by GenJ to start this report's execution - can be overriden by a
     * user defined report.
     *
     * @param context normally an instance of type Gedcom but depending on
     * accepts() could also be of type Entity or Property
     */
    public Object start(Object context) throws Throwable {
        try {
            return getStartMethod(context).invoke(this, new Object[]{context});
        } catch (InvocationTargetException t) {
            throw (t).getTargetException();
        }
    }

    /**
     * Tells wether this report doesn't change information in the Gedcom-file
     */
    public boolean isReadOnly() {
        return true;
    }

    /**
     * Whether the report allows to be run on a given context - default checks
     * for methods called
     * <ul>
     * <li>start(Gedcom|Object)
     * <li>start(Property)
     * <li>start(Entity)
     * <li>start(Indi[])
     * <li>...
     * </ul>
     *
     * @return title of action for given context or null for n/a
     */
    public String accepts(Object context) {
        return getStartMethod(context) != null ? getName() : null;
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
                if (!methods[m].getName().equals("start")) {
                    continue;
                }
                // make sure its a one-arg
                Class<?>[] params = methods[m].getParameterTypes();
                if (params.length != 1) {
                    continue;
                }
                // keep it
                Class<?> param = params[0];
                if (param.isAssignableFrom(context.getClass())) {
                    return methods[m];
                }
                // try next
            }
        } catch (Throwable t) {
        }
        // n/a
        return null;
    }

    /**
     * return true if report is not shown in report list view
     *
     * @return
     */
    public boolean isHidden() {
        return false;
    }

    /**
     * Represents the report category.
     */
    public static class Category {

        private static final ImageIcon DEFAULT_ICON = Images.imgQuestion;
        private static final Category DEFAULT_CATEGORY = createCategory("Other");
        private String name;
        private String displayName;
        private final ImageIcon image;

        private Category(String name, ImageIcon image) {
            this.name = name.toLowerCase();
            this.image = image;
            // try to translate
            String result = COMMON_RESOURCES.getString("category." + this.name, false);
            if (result != null) {
                displayName = result;
            }
        }

        private static Category createCategory(String name) {
            if (name == null) {
                return DEFAULT_CATEGORY;
            }
            // get image from name
            ImageIcon image;
            String file = "Category" + Category.UcFirst(name) + ".png";

            InputStream in = null;
            try {
                in = Report.class.getResourceAsStream(file);
                image = new genj.util.swing.ImageIcon(file, in);
            } catch (Throwable t) {
                image = DEFAULT_ICON;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }
            return new Category(name, image);
        }

        /**
         * Return this category name (non translated).
         *
         * @return
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the translated category name.
         */
        public String getDisplayName() {
            if (displayName == null) {
                return name;
            }
            return displayName;
        }

        /**
         * return this category icon.
         *
         * @return
         */
        public ImageIcon getImage() {
            return image == null ? new genj.util.swing.ImageIcon(DEFAULT_ICON) : image;
        }

        /**
         * UcFisrt utility.
         */
        private static String UcFirst(String s) {
            String out = s;
            if (s != null && !s.isEmpty()) {
                out = Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
            }
            return out;
        }
        private static final TreeMap<String, Category> CATEGORIES = new TreeMap<String, Category>();

        static {
            // Default category when category isn't defined in properties file
            CATEGORIES.put(DEFAULT_CATEGORY.name, DEFAULT_CATEGORY);
        }

        private static Category get(String name) {
            Category category = (Category) CATEGORIES.get(name);
            if (category == null) {
                category = createCategory(name);
                CATEGORIES.put(name, category);
            }
            return category;
        }
    }

    
    
    /**
     * Common report tools
     * 
     */
    
    /**
     * Get all individuals who are somewhere in the search dialog result
     *
     * @param gedcom
     * @return
     */
    public List<Entity> getSearchEntities(Gedcom gedcom) {
        return SearchCommunicator.getResultEntities(gedcom);
    }

    /**
     * Get active individual
     *
     * @param gedcom
     * @return
     */
    public Indi getActiveIndi(Gedcom gedcom) {
        List<Context> gedcontexts = GedcomDirectory.getDefault().getContexts();
        for (Context ctx : gedcontexts) {
            if (ctx.getGedcom() == gedcom && ctx.getEntity() != null) {
                Entity activeEntity = ctx.getEntity();
                if (activeEntity instanceof Fam) {
                    Fam f = (Fam)activeEntity;
                    activeEntity = f.getHusband();
                    if (activeEntity == null) {
                        activeEntity = f.getWife();
                    }
                } 
                if (activeEntity instanceof Indi) {
                    return (Indi)activeEntity;
                }
            }
        }
        return null;
    }    
    
    
    /**
     * Filters files using a specified extension.
     */
    private static class FileExtensionFilter extends FileFilter {

        private final String extension;

        public FileExtensionFilter(String extension) {
            this.extension = extension != null ? extension.toLowerCase() : FileChooserBuilder.getHtmlFilter().getExtensions()[0];
        }

        /**
         * Returns true if file name has the right extension.
         */
        @Override
        public boolean accept(File f) {
            if (f == null) {
                return false;
            }
            if (f.isDirectory()) {
                return true;
            }
            return f.getName().toLowerCase().endsWith("." + extension);
        }

        @Override
        public String getDescription() {
            return extension;
        }
    }

} //Report
