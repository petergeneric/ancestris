/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2008 Nils Meier <nils@meiers.net>
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
 * $Revision$ $Author$ $Date$
 */
package ancestris.gedcom;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.core.pluginservice.PluginInterface;
import static ancestris.gedcom.Bundle.*;
import ancestris.util.TimingUtility;
import ancestris.util.swing.DialogManager;
import ancestris.view.AncestrisViewInterface;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.*;
import genj.io.*;
import genj.util.*;
import genj.util.swing.FileChooser;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * A static registry for Gedcom instances. 
 * This registry bridges Gedcom and
 * Context object to Gedcom Data Objects. For file operations, {@link GedcomMgr}
 * service is used.
 */
public abstract class GedcomDirectory {

    final static Logger LOG = Logger.getLogger("ancestris.app");
    final static Resources RES = Resources.get(GedcomDirectory.class);
    final static Registry REGISTRY = Registry.get(GedcomDirectory.class);
    protected List<GedcomRegistryListener> listeners = new ArrayList<GedcomRegistryListener>();

    /**
     * Gets all registered contexts
     * @return 
     */
    public abstract List<Context> getContexts();

    /**
     * Gets context for a FileObject if it has been registered
     * null otherwise
     * @param file
     * @return 
     */
    public abstract Context getContext(FileObject file);

    /**
     * Gets the {@link GedcomDataObject} registered for context 
     * @param context
     * @return
     * @throws ancestris.gedcom.GedcomDirectory.ContextNotFoundException 
     */
    public abstract GedcomDataObject getDataObject(Context context) throws ContextNotFoundException;

    /**
     * register a {@link GedcomDataObject}.
     * The context key is taken from {@link GedcomDataObject#getContext()}
     * @param dao
     * @return 
     */
    protected abstract boolean registerGedcomImpl(GedcomDataObject dao);

    /**
     * remove registration for this dao.
     * 
     * @param context
     * @return 
     */
    protected abstract boolean unregisterGedcomImpl(Context context);

    /**
     * create a new gedcom file.
     * 
     */
    @NbBundle.Messages({
        "create.action=Create",
        "create.title=Create Gedcom",
        "# {0} - file path",
        "file.exists=File {0} already exists. Proceed?"
     })            
    public Context newGedcom() {
    /*
     * when creating a new gedcom, the new file is always created on disk ATM
     * TODO: should we change this behaviour?
     */
        //FIXME: use dao.createfromtemplate?
        //FIXME: use DataObject template/wizard. the file is created from data
        // in setGedcom

        // let user choose a file
        File file = chooseFile(create_title(), create_action(), null);
        if (file == null) {
            return null;
        }
        if (!file.getName().endsWith(".ged")) {
            file = new File(file.getAbsolutePath() + ".ged");
        }
        if (file.exists()) {
            if (DialogManager.YES_OPTION != 
                    DialogManager.createYesNo(create_title(), file_exists(file.getName()))
                    .setMessageType(DialogManager.WARNING_MESSAGE)
                    .show()) {
                return null;
            }
        }

        // form the origin
        Gedcom gedcom;
        try {
            gedcom = new Gedcom(Origin.create(file.toURI().toURL()));
        } catch (MalformedURLException e) {
            LOG.log(Level.WARNING, "unexpected exception creating new gedcom", e);
            return null;
        }

        // done
        //FIXME: changer le nouveau gedcom cree par defaut!
        Context context = GedcomMgr.getDefault().setGedcom(gedcom);
        try {
            setDefault(gedcom);
            Indi firstIndi = (Indi) context.getGedcom().getFirstEntity(Gedcom.INDI);
            if (firstIndi == null) {
                firstIndi = (Indi) context.getGedcom().createEntity(Gedcom.INDI);
                firstIndi.addDefaultProperties();
            }

            // save gedcom file
            GedcomMgr.getDefault().saveGedcom(new Context(firstIndi), FileUtil.toFileObject(file));
            
            // and reopens the file
            return openGedcom(FileUtil.toFileObject(file));
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return context;
    }

    //FIXME: to be removed when dao template will be used for gedcom
    private void setDefault(Gedcom gedcom) {
        try {

                    AncestrisPreferences submPref = Registry.get(genj.gedcom.GedcomOptions.class);

                    // Create submitter
                    Submitter submitter = (Submitter) gedcom.createEntity(Gedcom.SUBM);
                    submitter.setName(submPref.get("submName", ""));
                    submitter.setCity(submPref.get("submCity", ""));
                    submitter.setPhone(submPref.get("submPhone", ""));
                    submitter.setEmail(submPref.get("submEmail", ""));
                    submitter.setCountry(submPref.get("submCountry", ""));
                    submitter.setWeb(submPref.get("submWeb", ""));

                    gedcom.createEntity("HEAD", "");

                    // Create place format
                    gedcom.setPlaceFormat(genj.gedcom.GedcomOptions.getInstance().getPlaceFormat());
                    gedcom.setShowJuridictions(genj.gedcom.GedcomOptions.getInstance().getShowJuridictions());
                    gedcom.setPlaceSortOrder(genj.gedcom.GedcomOptions.getInstance().getPlaceSortOrder());
                    gedcom.setPlaceDisplayFormat(genj.gedcom.GedcomOptions.getInstance().getPlaceDisplayFormat());
        } catch (GedcomException e) {
            Exceptions.printStackTrace(e);
        }
    }

    /**
     * asks and loads gedcom file
     */
    public Context openGedcom() {

        // ask user
        File file = chooseFile(RES.getString("cc.open.title"), RES.getString("cc.open.action"), null);
        if (file == null) {
            return null;
        }
        REGISTRY.put("last.dir", file.getParentFile().getAbsolutePath());
        return openGedcom(FileUtil.toFileObject(file));
    }

    /**
     * This is equivalent to openGedcom(FileUtil.toFileObject(input))
     * @param input File to read
     *
     * @return loaded context. 
     *
     * @deprecated consider using FileObject. 
     */
    @Deprecated
    public Context openGedcom(File input) {
        if (input == null) {
            return null;
        }
        return openGedcom(FileUtil.toFileObject(input));
    }

    /**
     * Opens a Gedcom FileObject.
     *
     * Use DataObject loaders to find the proper handler and then register it in local
     * gedcom registry. Il file is already opened, doesn't open twice and return 
     * the saved context
     *
     * @param input Gedcom FileObject
     *
     * @return
     */
    public Context openGedcom(FileObject input) {
        Context context = getContext(input);
        if (context != null)
            return context;
        try {
            DataObject dao = DataObject.find(input);
            GedcomDataObject gdao = dao.getLookup().lookup(GedcomDataObject.class);
            if (gdao == null) {
                return null;
            }
            registerGedcom(gdao);
            context = gdao.getContext();
            openDefaultViews(context);
            SelectionDispatcher.fireSelection(context);
            return gdao.getContext();
        } catch (Exception e) {
            LOG.info(e.toString());
        }
        return context;
    }

    /**
     * save gedcom file
     */
    //FIXME: use dao.saveDocument instead?
    public boolean saveGedcom(Context context) {
        if (context == null || context.getGedcom() == null) {
            return false;
        }
        if (context != null && context.getGedcom().getOrigin() == null) {
            return saveAsGedcom(context);
        }
        try {
            return GedcomMgr.getDefault().saveGedcom(context, getDataObject(context).getPrimaryFile());
        } catch (ContextNotFoundException ex) {
            return false;
        }
    }

    /**
     * save gedcom to a new file.
     *
     * Ask for a file and options and seve curent context gedcom file to it
     *
     * @param context
     *
     * @return
     */
    public boolean saveAsGedcom(Context context) {

        if (context == null || context.getGedcom() == null) {
            return false;
        }

        // ask everyone to commit their data
        //XXX: we should move this to GedcomMgr. we must have a close look to filters if data ar to be committed
        GedcomMgr.getDefault().commitRequested(context);

        // .. choose file
//FIXME: DAN    Box options = new Box(BoxLayout.Y_AXIS);
//    options.add(new JLabel(RES.getString("save.options.encoding")));
//
//    ChoiceWidget comboEncodings = new ChoiceWidget(Gedcom.ENCODINGS, Gedcom.ANSEL);
//    comboEncodings.setEditable(false);
//    comboEncodings.setSelectedItem(context.getGedcom().getEncoding());
//    options.add(comboEncodings);
//
//    options.add(new JLabel(RES.getString("save.options.password")));
//    String pwd = context.getGedcom().getPassword();
//    TextFieldWidget textPassword = new TextFieldWidget(context.getGedcom().hasPassword() ? pwd : "", 10);
//    textPassword.setEditable(pwd!=Gedcom.PASSWORD_UNKNOWN);
//    options.add(textPassword);

        Collection<? extends Filter> filters = AncestrisPlugin.lookupAll(Filter.class);
        ArrayList<Filter> theFilters = new ArrayList<Filter>(5);
        for (Filter f : filters) {
            if (f.canApplyTo(context.getGedcom())) {
                theFilters.add(f);
            }
        }
        SaveOptionsWidget options = new SaveOptionsWidget(context.getGedcom(), theFilters.toArray(new Filter[]{}));//, (Filter[])viewManager.getViews(Filter.class, gedcomBeingSaved));
        File file = chooseFile(RES.getString("cc.save.title", context.getGedcom().toString()), RES.getString("cc.save.action"), options, context.getGedcom().toString());
        if (file == null) {
            return false;
        }

        // .. take chosen one & filters
        if (!file.getName().endsWith(".ged")) {
            file = new File(file.getAbsolutePath() + ".ged");
        }

        // Need confirmation if File exists?
        if (file.exists()) {
            if (DialogManager.YES_OPTION != 
                    DialogManager.createYesNo(RES.getString("cc.save.title", context.getGedcom().toString()),RES.getString("cc.open.file_exists"))
                    .setMessageType(DialogManager.WARNING_MESSAGE)
                    .show()){
                return false;
            }
        } else {
            //FIXME: if file doesn't exist, create a blank one (FileObject will then be correctly set)
            // On drawback is that an empty backup file is created.
            // FIXME: on the other hand, if file exists, the backup created will not necesserally be related 
            // To this gedcom data
            try {
                file.createNewFile();
            } catch (IOException ex) {
            }
        }

        Origin o = GedcomMgr.getDefault().saveGedcomAs(context, options, FileUtil.toFileObject(file));
        //XXX: must handle close old file and open new

        if (o != null) {
            openGedcom(o.getFile());
            return true;
        } else {
            if (context != null) {
                try {
                    FileObject newGedcom;
                    newGedcom = getDataObject(context).getPrimaryFile();
                    closeGedcom(context); // was:unregisterGedcom(context);
                    openGedcom(newGedcom);
                } catch (ContextNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return false;
        }
    }

    /**
     * closes gedcom file
     */
    public boolean closeGedcom(Context context) {
        // noop?
        if (context == null || context.getGedcom() == null) {
            return true;
        }

        // commit changes
        GedcomMgr.getDefault().commitRequested(context);

        // changes?
        if (context.getGedcom().hasChanged()) {

            // close file officially
            Object rc = DialogManager.create(null, RES.getString("cc.savechanges?", context.getGedcom().getName()))
                    .setMessageType(DialogManager.WARNING_MESSAGE)
                    .setOptionType(DialogManager.YES_NO_CANCEL_OPTION)
                    .show();
            // cancel - we're done
            if (rc == DialogManager.CANCEL_OPTION || rc == DialogManager.CLOSED_OPTION) {
                return false;
            }
            // yes - close'n save it
            if (rc == DialogManager.YES_OPTION) {
                if (!saveGedcom(context)) {
                    return false;
                }
            }

        }
        if (GedcomMgr.getDefault().gedcomClose(context)) {
            unregisterGedcom(context);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Registry management
     */
        /**
         * register gedcom file
         */
        public void registerGedcom(GedcomDataObject gedcomObject) {
            if (gedcomObject == null) {
                return;
            }
            if (!registerGedcomImpl(gedcomObject)){
                return;
            }

            // otifies listeners
            List<GedcomRegistryListener> ls = new ArrayList<GedcomRegistryListener>(listeners);
            for (GedcomRegistryListener listener : ls) {
                listener.gedcomRegistered(gedcomObject.getContext());
            }
        }

        /**
         * unregister gedcom file
         */
        public void unregisterGedcom(Context context) {
            if (context == null) {
                return;
            }
            if (!unregisterGedcomImpl(context)){
                return;
            }
            
            // Notifies
            List<GedcomRegistryListener> ls = new ArrayList<GedcomRegistryListener>(listeners);
            for (GedcomRegistryListener listener : ls) {
                listener.gedcomUnregistered(context);
            }
        }

    /**
     * gets selected context in active topComponent. If none, return first
     * opened gedcom
     *
     * @param firstIfNoneSelected
     *
     * @return
     *
     * @deprecated 
     * we will use 
     * Utilities.actionsGlobalContext().lookup(Context.class). If it is null, we must not
     * use the first available context as it is not deterministic. So this call 
     * is now equivalent to
     * Utilities.actionsGlobalContext().lookup(Context.class)
     */
    //XXX: GedcomExplorer must be actionGlobalContext provider: to be rewritten
    //XXX: in fact we must provide Context in explorer Nodes Lookup
        @Deprecated
    public Context getSelectedContext(boolean firstIfNoneSelected) {
        Context c = Utilities.actionsGlobalContext().lookup(Context.class);
//        if (!firstIfNoneSelected) {
//            return c;
//        }
//        if (c != null) {
//            return c;
//        }
//        try {
//            return getContexts().get(0);
//        } catch (IndexOutOfBoundsException e) {
//            return null;
//        }
        return c;
    }

    //XXX; must be removed, no longer supported...
    /**
     *
     * @return 
     * @deprecated
     */
        @Deprecated
    public Context getLastContext() {
        throw new UnsupportedOperationException("GedcomDirectory does not provide last context anymore. use private cache or lookup");
    }

    /*
     * Utilities methods
     */
    /**
     * Let the user choose a file
     */
    public File chooseFile(String title, String action, JComponent accessory) {
        return chooseFile(title, action, accessory, null);
    }

    public File chooseFile(String title, String action, JComponent accessory, String defaultFilename) {
        FileChooser chooser = new FileChooser(
                null, title, action, "ged",
                EnvironmentChecker.getProperty(new String[]{"ancestris.gedcom.dir", "user.home"}, ".", "choose gedcom file"));

        //FIXME: gedcoms were  opened in same dir as default gedcom
        // will be changed later
//        File gedcomDir = ancestris.core.Options.getInstance().getDefaultGedcom();
//        if (gedcomDir != null) {
//            gedcomDir = gedcomDir.getParentFile();
//        }
//        if (gedcomDir == null || gedcomDir.trim().isEmpty()) {
//            gedcomDir = "user.home";
//        }
//        File directory = REGISTRY.get("last.dir", gedcomDir);
        File directory = REGISTRY.get("last.dir", new File (""));
        chooser.setCurrentDirectory(directory);
        if (defaultFilename != null) {
            chooser.setSelectedFile(new File(directory, defaultFilename));
        }
        if (accessory != null) {
            chooser.setAccessory(accessory);
        }
        chooser.setFileHidingEnabled(!ancestris.core.CoreOptions.getInstance().getShowHidden());
        if (JFileChooser.APPROVE_OPTION != chooser.showDialog()) {
            return null;
        }
        // check the selection
        File file = chooser.getSelectedFile();
        if (file == null) {
            return null;
        }
        // remember last directory
        REGISTRY.put("last.dir", file.getParentFile().getAbsolutePath());
        // done
        return file;
    }

    /**
     * utilities
     */
    // Workbench helper
    // XXX: will be moved in another package (ie ancestris.view)
    public static void openDefaultViews(Context context) {

        AncestrisPreferences prefs = Registry.get(AncestrisViewInterface.class);
        List<Class> openedViews = new ArrayList<Class>();

        // Always open explorer (if not opened)
        // FIXME: GedcomTC is not known from this module
        // XXX: Must be done with lookups will be done later
        // GedcomExplorerTopComponent.getDefault().open();

        // try gedcom properties
        Registry gedcomSettings = context.getGedcom().getRegistry();

        // FIXME: a reecrire plus proprement
        String ovs[] = gedcomSettings.get("openViews", (String[]) null);
        openedViews.addAll(AncestrisPlugin.lookupForName(AncestrisViewInterface.class, ovs));

        if (openedViews.isEmpty()) {
            openedViews.addAll(AncestrisPlugin.lookupForName(
                    AncestrisViewInterface.class,
                    prefs.get("openViews", (String[]) null)));
        }
        if (openedViews.isEmpty()) {
            // Open default views
            for (PluginInterface sInterface : Lookup.getDefault().lookupAll(PluginInterface.class)) {
                openedViews.addAll(sInterface.getDefaultOpenedViews());
            }
        }

        TopComponent tc = null;
        Map<String, TopComponent> name2tc = new HashMap<String, TopComponent>();
        for (Class clazz : openedViews) {
            LOG.log(Level.FINE, "{0}: {1} opened", new Object[]{TimingUtility.geInstance().getTime(), clazz.getCanonicalName()});
            try {
                tc = (TopComponent) clazz.newInstance();

                if (tc instanceof AncestrisViewInterface) {
                    tc = ((AncestrisViewInterface) tc).create(context);
                }
                tc.open();
                name2tc.put(clazz.getCanonicalName(), tc);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            if (tc != null) {
                tc.requestActive();
            }
        }
        for (String name : gedcomSettings.get("focusViews", new String[]{})) {
            TopComponent tcToFocus = name2tc.get(name);
            if (tcToFocus != null) {
                tcToFocus.requestActive();
            }
        }
    }

    /**
     * Exception that can be thrown by operation on
     * trying to use a non existent context
     */
    static public class ContextNotFoundException extends Exception {

        public ContextNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * interface for gedcom directory
     */
    public interface GedcomRegistryListener {

        /**
         * callback for new gedcom registration
         *
         * @param context
         */
        public void gedcomRegistered(Context context);

        /**
         * callback for gedcom removal from registry
         *
         * @param context
         */
        public void gedcomUnregistered(Context context);
    }

    /**
     * listener
     */
    public void addListener(GedcomRegistryListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GedcomRegistryListener listener) {
        listeners.remove(listener);
    }
    /**
     * singleton pattern
     */
    /**
     * Instance of dummy gedcom manager.
     */
    private static GedcomDirectory defaultInstance;

    /**
     * Singleton instance accessor method for gedcom directory.
     *
     * @return instance of gedcom directory installed in the system
     */
    public static GedcomDirectory getDefault() {
        GedcomDirectory gdInstance = Lookup.getDefault().lookup(GedcomDirectory.class);

        return (gdInstance != null) ? gdInstance : getDefaultInstance();
    }

    private static synchronized GedcomDirectory getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new DefaultGedcomDirectoryImpl();
        }
        return defaultInstance;
    }

    /**
     * default implementation for {@link GedcomDirectory}. This class maintains
     * a map with gedcom object as key and {@link GedcomDataObject} as values.
     */
    private static class DefaultGedcomDirectoryImpl extends GedcomDirectory {

        private Map<Gedcom, GedcomDataObject> gedcomsOpened = new HashMap<Gedcom, GedcomDataObject>(5);

        /**
         * register gedcom file
         */
        @Override
        protected boolean registerGedcomImpl(GedcomDataObject gedcomObject) {
            Gedcom gedcom = gedcomObject.getContext().getGedcom();
            if (!gedcomsOpened.containsKey(gedcom)) {
                gedcomsOpened.put(gedcom, gedcomObject);
            }
            return true;
        }

        /**
         * unregister gedcom file
         */
        //FIXME: we could use vetoable setValid(false) to prevent closing dao if used in editor
        protected boolean unregisterGedcomImpl(Context context) {
            GedcomDataObject gdao = gedcomsOpened.get(context.getGedcom());
            try {
                gdao.setValid(false);
            } catch (PropertyVetoException ex) {
                return false;
            }
            gedcomsOpened.remove(context.getGedcom());
            return true;
        }
/**
 * 
 * @param gedName
 * @return 
 */
        @Override
    public Context getContext(FileObject file) {

        if (file == null) {
            return null;
        }
            for (Gedcom g : gedcomsOpened.keySet()) {
            if (file.equals(gedcomsOpened.get(g).getPrimaryFile())){
                return gedcomsOpened.get(g).getContext();
            }
        }
        return null;
    }

    /**
         * accessor gedcoms
         */
        public List<Context> getContexts() {
            List<Context> result = new ArrayList<Context>();
            for (Gedcom g : gedcomsOpened.keySet()) {
                result.add(gedcomsOpened.get(g).getContext());
            }
            return result;
        }

        public GedcomDataObject getDataObject(Context context) throws ContextNotFoundException {
            if (context == null || context.getGedcom() == null) {
                throw new ContextNotFoundException("No GedcomDAO for context " + context);
            }
            GedcomDataObject dao = gedcomsOpened.get(context.getGedcom());
            if (dao == null) {
                throw new ContextNotFoundException("No GedcomDAO for context " + context);
            }
            return dao;
        }
    }
}