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

import ancestris.api.imports.Import;
import ancestris.core.beans.ConfirmChangeWidget;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.core.pluginservice.PluginInterface;
import static ancestris.gedcom.Bundle.*;
import ancestris.util.TimingUtility;
import ancestris.util.swing.DialogManager;
import ancestris.util.swing.FileChooserBuilder;
import ancestris.view.AncestrisViewInterface;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.*;
import genj.io.*;
import genj.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * A static registry for Gedcom instances. This registry bridges Gedcom and
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
     *
     * @return
     */
    public abstract List<Context> getContexts();

    /**
     * Test if a gedcom file is registered
     *
     * @return
     */
    public abstract boolean isGedcomRegistered(Gedcom gedcom);

    /**
     * Gets context for a FileObject if it has been registered null otherwise
     *
     * @param file
     *
     * @return
     */
    public abstract Context getContext(FileObject file);

    /**
     * Gets the {@link GedcomDataObject} registered for context
     *
     * @param context
     *
     * @return
     *
     * @throws .gedcom.GedcomDirectory.ContextNotFoundException
     */
    public abstract GedcomDataObject getDataObject(Context context) throws ContextNotFoundException;

    /**
     * register a {@link GedcomDataObject}. The context key is taken from
     * {@link GedcomDataObject#getContext()}
     *
     * @param dao
     *
     * @return
     */
    protected abstract boolean registerGedcomImpl(GedcomDataObject dao);

    /**
     * remove registration for this dao.
     *
     * @param context
     *
     * @return
     */
    protected abstract boolean unregisterGedcomImpl(Context context);

    /**
     * Set autosave delay
     *
     * @return
     */
    public abstract void setAutosaveDelay();

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
        return newGedcom(null, null, null, true);
    }

    public Context newGedcom(Gedcom gedcomProvided, String title, String defaultFilename, boolean setDefaults) {
        /*
         * when creating a new gedcom, the new file is always created on disk ATM
         * TODO: should we change this behaviour?
         */
        //FIXME: use dao.createfromtemplate?
        //FIXME: use DataObject template/wizard. the file is created from data
        // in setGedcom

        // let user choose a file
        File file = null;
        boolean fileOK = false;
        while (!fileOK) {
            file = new FileChooserBuilder(GedcomDirectory.class)
                    .setFilesOnly(true)
                    .setDefaultBadgeProvider()
                    .setTitle(title)
                    .setApproveText(create_action())
                    .setDefaultExtension(FileChooserBuilder.getGedcomFilter().getExtensions()[0])
                    .setFileFilter(FileChooserBuilder.getGedcomFilter())
                    .setAcceptAllFileFilterUsed(true)
                    .setFileHiding(true)
                    .setDefaultWorkingDirectory(new File(EnvironmentChecker.getProperty(new String[]{"ancestris.gedcom.dir", "user.home"}, ".", "choose gedcom file")))
                    .setSelectedFile(new File(defaultFilename + ".ged"))
                    .showSaveDialog(false);

            if (file == null) {
                LOG.log(Level.SEVERE, "problem defining the gedcom file in given directory");
                return null;
            }
            if (!file.getName().endsWith(".ged")) {
                file = new File(file.getAbsolutePath() + File.separator + defaultFilename + ".ged");
            }
            if (file.exists()) {
                if (DialogManager.YES_OPTION == DialogManager.createYesNo(title == null ? create_title() : title, file_exists(file.getName())).setMessageType(DialogManager.WARNING_MESSAGE).show()) {
                    fileOK = true;
                }
            } else {
                fileOK = true;
            }
        }

        // form the origin
        Gedcom gedcom = gedcomProvided;
        try {
            if (gedcomProvided == null) {
                gedcom = new Gedcom(Origin.create(file.toURI().toURL()));
            } else {
                gedcom.setOrigin(Origin.create(file.toURI().toURL()));
            }
        } catch (MalformedURLException e) {
            LOG.log(Level.SEVERE, "unexpected exception creating new gedcom", e);
            return null;
        }

        // done
        //FIXME: changer le nouveau gedcom cree par defaut!
        Context context = GedcomMgr.getDefault().setGedcom(gedcom);
        try {
            if (setDefaults) {
                setDefault(gedcom);
            }
            Indi firstIndi = (Indi) context.getGedcom().getFirstEntity(Gedcom.INDI);
            if (firstIndi == null) {
                firstIndi = (Indi) context.getGedcom().createEntity(Gedcom.INDI);
                firstIndi.addDefaultProperties();
            }

            // save gedcom file and close
            GedcomMgr.getDefault().saveGedcom(new Context(firstIndi));
            GedcomMgr.getDefault().gedcomClose(context);

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
        File file = chooseFile(RES.getString("cc.open.title"), RES.getString("cc.open.action"), null, false);
        if (file == null) {
            return null;
        }
        REGISTRY.put("last.dir", file.getParentFile().getAbsolutePath());
        return openGedcom(FileUtil.toFileObject(file));
    }

    /**
     * This is equivalent to openGedcom(FileUtil.toFileObject(input))
     *
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
     * Main opening Gedcom method. Assumes gedcom could be coming from any other
     * software
     *
     * FL: 2017-09-24 : Auto-detect where gedcom is coming from and choose
     * proper import module before opening Gedcom
     *
     * Logic is: - Detect Gedcom origin - Open file header line "1 SOUR xxxx" =>
     * software = xxxx - Depending on origin, select import or none if coming
     * from Ancestris - If software not Ancestris: - Detect name of software
     * among an import list (lookup xxxx) - Popup user - "File is not an
     * Ancestris Gedcom file, it is coming from software xxxx and needs to be
     * modified to be 100% Gedcom compatible" - "Ancestris will create a copy
     * 'filename_ancestris.ged', modify it and save it to ....." - "OK to
     * proceed ?" else cancel operation. - Run corresponding import - Else -
     * Open Gedcom normally (progress bar)
     *
     * @param foInput
     * @return context to display
     */
    public Context openGedcom(FileObject foInput) {

        if (foInput == null) {
            LOG.severe("File to open no longer seems to exists");
            return null;
        }
        if (foInput.getName().length() > 75) { // max is 80 char with extension ".ged"
            String error = RES.getString("cc.save.file_too_long") + "\n(" + foInput.getName() + ").";
            LOG.severe(error);
            DialogManager.createError(RES.getString("cc.open.title"), error).show();
            return null;
        }

        // Detect Gedcom origin : 
        // - Open file header line "1 SOUR xxxx" => software = xxxx
        // -                       "2 NAME xxxx" => software = xxxx if nothing in SOUR
        String software = "";
        boolean stop = false;
        GedcomFileReader input = null;
        try {
            input = GedcomFileReader.create(new File(foInput.getPath()));
            try {
                while ((input.getNextLine(true)) != null && !stop) {
                    if (input.getLevel() == 0 && input.getTag().equals("HEAD")) {
                        continue;
                    }
                    if (input.getLevel() == 1 && input.getTag().equals("SOUR")) {
                        software = input.getValue();
                        if (software.isEmpty()) {
                            continue;
                        }
                        stop = true;
                    }
                    if (input.getLevel() == 2 && input.getTag().equals("NAME")) {
                        software = input.getValue();
                        stop = true;
                    }
                    if (input.getLevel() == 0 && !input.getTag().equals("HEAD")) {
                        stop = true;
                    }
                }
            } finally {
                if (input != null) {
                    input.close();
                }
            }
        } catch (GedcomFormatException e) {
            String errLine = input != null ? " - " + input.getLine() : "";
            String errMsg = e.getMessage() + "\n" + NbBundle.getMessage(Import.class, "error.line", e.getLine() + errLine);
            JOptionPane.showMessageDialog(null, errMsg);
            LOG.log(Level.SEVERE, errMsg, e);
            return null;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error during opening.", e);
            String errLine = input != null ? " - " + input.getLine() : "";
            JOptionPane.showMessageDialog(null, NbBundle.getMessage(Import.class, "error.line", e.getMessage() + " : " + errLine));
            return null;
        }

        // If software is Ancestris, open file normally
        if (software.equalsIgnoreCase("ANCESTRIS")) {
            return openAncestrisGedcom(foInput);
        }

        // Detect name of software among the import list (lookup xxxx)
        Import identifiedImport = null;
        Import defaultImport = null;
        Collection<? extends Import> c = Lookup.getDefault().lookupAll(Import.class);
        for (Import o : c) {
            if (o.isGeneric()) {
                defaultImport = o;
            }
            if (o.toString().toLowerCase().contains(software.toLowerCase())) {
                identifiedImport = o;
                break;
            }
            if (software.toLowerCase().contains(o.toString().toLowerCase())) {
                identifiedImport = o;
                break;
            }
        }
        if (identifiedImport == null || software.isEmpty()) {
            identifiedImport = defaultImport;
        }
        if (identifiedImport == null) {
            LOG.info("Opening a non Ancestris file from '" + software + "' and cannot find any import module to be used. Using normal Ancestris file open.");
            return openAncestrisGedcom(foInput);
        }

        // Popup confirmation to user
        String dirname = foInput.getParent().getPath() + System.getProperty("file.separator"); // System.getProperty("java.io.tmpdir") + System.getProperty("file.separator");
        String tmpFileName = foInput.getName() + "_ancestris.ged";
        LOG.info("Opening a non Ancestris file from " + software + ". Asking confirmation to user to use the corresponding import module or not.");
        String message = RES.getString("cc.importGedcom?", foInput.getNameExt(), software, tmpFileName, dirname);
        JButton convertButton = new JButton(RES.getString("cc.button.convert"));
        JButton asisButton = new JButton(RES.getString("cc.button.asis"));
        JButton cancelButton = new JButton(RES.getString("cc.button.cancel"));
        Object[] options = new Object[]{convertButton, asisButton, cancelButton};
        Object rc = DialogManager.create(RES.getString("cc.open.title"), message).setMessageType(DialogManager.WARNING_MESSAGE).setOptions(options).show();
        if (rc == cancelButton || rc == DialogManager.CANCEL_OPTION || rc == DialogManager.CLOSED_OPTION) {
            return null;
        }
        if (rc == asisButton) {
            LOG.info("Conversion of file from " + software + " not confirmed by user. Opening file as is.");
            return openAncestrisGedcom(foInput);
        }

        // Run corresponding import
        LOG.info("Conversion of file from " + software + " confirmed by user.");
        identifiedImport.launch(new File(foInput.getPath()), new File(dirname + tmpFileName));

        return null;
    }

    /**
     *
     * Open Gedcom normally (without any correction) : assumes input file is an
     * Ancestris gedcom file.
     *
     * Opens a Gedcom FileObject.
     *
     * Use DataObject loaders to find the proper handler and then register it in
     * local gedcom registry. If file is already opened, doesn't open twice and
     * return the saved context
     *
     * @param input Gedcom FileObject
     *
     * @return
     */
    public Context openAncestrisGedcom(FileObject input) {
        Context context = getContext(input);
        if (context != null) {
            return context;
        }
        try {
            DataObject dao = DataObject.find(input);  // finds the GedcomDataObject and instantiates it, which loads the Gedcom because the constructor includes load()
            GedcomDataObject gdao = dao.getLookup().lookup(GedcomDataObject.class);
            if (gdao == null) {
                return null;
            }
            if (gdao.isCancelled()) {
                gdao.setCancelled(false);
                return null;
            }
            if (gdao.getContext() == null) {
                if (!gdao.load()) {  // loads gedcom file here (if not already loaded)
                    return null;
                }
            }
            registerGedcom(gdao);
            context = gdao.getContext();
            if (!GedcomMgr.getDefault().isQuiet()) {
                openDefaultViews(context);
                SelectionDispatcher.fireSelection(context);
            }
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
        if (context != null
                && (context.getGedcom().getOrigin() == null
                || !context.getGedcom().getOrigin().getFile().exists())) {
            return saveAsGedcom(context, null);
        }
        return GedcomMgr.getDefault().saveGedcom(context);
    }

    /**
     * save gedcom to a new file.
     *
     * Ask for a file and options and seve curent context gedcom file to it
     *
     * @param context
     * @param outputFile
     *
     * @return
     */
    public boolean saveAsGedcom(Context context, File outputFile) {

        if (context == null || context.getGedcom() == null) {
            return false;
        }

        // ask everyone to commit their data
        //XXX: we should move this to GedcomMgr. we must have a close look to filters if data are to be committed
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
        ArrayList<Filter> theFilters = new ArrayList<Filter>(5);
        for (Filter f : AncestrisPlugin.lookupAll(Filter.class)) {
            if (f.canApplyTo(context.getGedcom())) {
                theFilters.add(f);
            }
        }
        for (Filter f : Lookup.getDefault().lookupAll(Filter.class)) {
            if (f.canApplyTo(context.getGedcom())) {
                theFilters.add(f);
            }
        }

        SaveOptionsWidget options = new SaveOptionsWidget(context.getGedcom(), theFilters.toArray(new Filter[]{}));//, (Filter[])viewManager.getViews(Filter.class, gedcomBeingSaved));

        if (outputFile == null) {

            File file = new FileChooserBuilder(GedcomDirectory.class)
                    .setDirectoriesOnly(false)
                    .setDefaultBadgeProvider()
                    .setAccessory(options)
                    .setTitle(RES.getString("cc.save.title", context.getGedcom().toString()))
                    .setApproveText(RES.getString("cc.save.action"))
                    .setDefaultExtension(FileChooserBuilder.getGedcomFilter().getExtensions()[0])
                    .setFileFilter(FileChooserBuilder.getGedcomFilter())
                    .setAcceptAllFileFilterUsed(false)
                    .setDefaultWorkingDirectory(new File(EnvironmentChecker.getProperty(new String[]{"ancestris.gedcom.dir", "user.home"}, ".", "choose gedcom file")))
                    .setSelectedFile(context.getGedcom().getOrigin().getFile())
                    .showSaveDialog(false);

            if (file == null) {
                return false;
            }

            // .. take chosen one & filters
            if (!file.getName().endsWith(".ged")) {
                file = new File(file.getAbsolutePath() + ".ged");
            }

            // Need confirmation if File exists?
            if (file.exists()) {
                if (DialogManager.YES_OPTION
                        != DialogManager.createYesNo(
                                RES.getString("cc.save.title", context.getGedcom().toString()),
                                file_exists(file.getName())).setMessageType(DialogManager.WARNING_MESSAGE).show()) {
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
            outputFile = file;
        }

        Origin o = GedcomMgr.getDefault().saveGedcomAs(context, options, FileUtil.toFileObject(outputFile));
        //XXX: must handle close old file and open new

        if (o == null) {
            return false;
        } else {
            if (context != null) {
                closeGedcom(context); // was:unregisterGedcom(context);
                openAncestrisGedcom(FileUtil.toFileObject(o.getFile()));
            }
            return true;
        }
    }

    /**
     * closes gedcom file.
     *
     * @param context to be closed
     * @return true if gedcom has been close (ie not canceleld by user)
     */
    public boolean closeGedcom(Context context) {
        // noop?
        if (context == null || context.getGedcom() == null) {
            return true;
        }
        try {
            // if gedcom is no longer in directory don't close it again. (it will generate an exception catched below with no error. 
            // this situation is seen when the last closed window closes the gedcom file
            getDataObject(context);
            // commit changes
            GedcomMgr.getDefault().commitRequested(context);

            // changes?
            if (context.getGedcom().hasChanged() || !context.getGedcom().getOrigin().getFile().exists()) {

                // close file officially
                Object rc = DialogManager.create(null, RES.getString("cc.savechanges?", context.getGedcom().getName())).setMessageType(DialogManager.WARNING_MESSAGE).setOptionType(DialogManager.YES_NO_CANCEL_OPTION).show();
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
            // First unregister gedcom, wihch  (otherwise, components when they close call getUndoRedo which which ich will close afterwards will 
            unregisterGedcom(context);

            // Then close gedcom.
            GedcomMgr.getDefault().gedcomClose(context);

        } catch (ContextNotFoundException e) {

        }
        return true;
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
        if (!registerGedcomImpl(gedcomObject)) {
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
        if (!unregisterGedcomImpl(context)) {
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
     * @deprecated we will use
     * Utilities.actionsGlobalContext().lookup(Context.class). If it is null, we
     * must not use the first available context as it is not deterministic. So
     * this call is now equivalent to
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
     * @return @deprecated
     */
    @Deprecated
    public Context getLastContext() {
        throw new UnsupportedOperationException("GedcomDirectory does not provide last context anymore. use private cache or lookup");
    }

    /*
     * Utilities methods
     */
    /**
     * Let the user choose a file (false) or create one (true)
     *
     * @param title
     * @param action
     * @param accessory
     * @param create
     * @return
     */
    public File chooseFile(String title, String action, JComponent accessory, boolean create) {
        return chooseFile(title, action, accessory, null, create);
    }

    public File chooseFile(String title, String action, JComponent accessory, String defaultFilename, boolean create) {

        FileChooserBuilder fbc = new FileChooserBuilder(GedcomDirectory.class)
                .setDirectoriesOnly(false)
                .setDefaultBadgeProvider()
                .setAccessory(accessory)
                .setTitle(title)
                .setApproveText(action)
                .setDefaultExtension(FileChooserBuilder.getGedcomFilter().getExtensions()[0])
                .setFileFilter(FileChooserBuilder.getGedcomFilter())
                .setAcceptAllFileFilterUsed(false)
                .setDefaultWorkingDirectory(new File(EnvironmentChecker.getProperty(new String[]{"ancestris.gedcom.dir", "user.home"}, ".", "choose gedcom file")));

        if (defaultFilename != null) {
            fbc = fbc.setSelectedFile(new File(defaultFilename));
        }

        File file = create ? fbc.showSaveDialog(false) : fbc.showOpenDialog();

        // done
        return file;
    }

    /**
     * utilities
     *
     * @param context
     */
    public static void openDefaultViews(Context context) {

        List<Class> openedViews = new ArrayList<>();

        // 1/ Try gedcom properties from last Ancestris use
        Registry gedcomSettings = context.getGedcom().getRegistry(); // .ancestris/config/Preferences/gedcoms/settings/kennedy.ged
        String ovs[] = gedcomSettings.get("openViews", (String[]) null);
        openedViews.addAll(AncestrisPlugin.lookupForName(AncestrisViewInterface.class, ovs));

        // 2/ If none, try from default user settings (saveLayout action)
        if (openedViews.isEmpty()) {
            AncestrisPreferences prefs = Registry.get(AncestrisViewInterface.class);   // .ancestris/config/Preferences/ancestris/core/ancestris-view.properties
            openedViews.addAll(AncestrisPlugin.lookupForName(AncestrisViewInterface.class, prefs.get("openViews", (String[]) null)));
        }

        // 3/ If none, default to views defined in each plugin itself
        if (openedViews.isEmpty()) {
            // Open default views
            for (PluginInterface sInterface : Lookup.getDefault().lookupAll(PluginInterface.class)) {    // each module/plugin
                openedViews.addAll(sInterface.getDefaultOpenedViews());
            }
        }

        // Then open these views...
        TopComponent tc = null;
        Map<String, TopComponent> name2tc = new HashMap<String, TopComponent>();
        for (Class clazz : openedViews) {
            LOG.log(Level.FINE, "{0}: {1} opened", new Object[]{TimingUtility.getInstance().getTime(), clazz.getCanonicalName()});
            try {
                // create temporary instance just to be able to call to "create"
                tc = (TopComponent) clazz.newInstance();
                if (tc instanceof AncestrisViewInterface) {
                    tc = ((AncestrisViewInterface) tc).create(context);
                }
                tc.open(); // this is where the windows gets docked where gedcom properties or ancestris properties were saved (if property is ALWAYS)
                name2tc.put(clazz.getCanonicalName(), tc);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            if (tc != null) {
                tc.requestActive();  // force focus on all in case next step is not executed
            }
        }

        // Regarding focus, we only have the last gedcom muse 
        for (String name : gedcomSettings.get("focusViews", new String[]{})) {
            TopComponent tcToFocus = name2tc.get(name);
            if (tcToFocus != null) {
                tcToFocus.requestActive();
            }
        }

        // Clean mode directory to avoid piling up unused files
        // 1/ Removed Anonymous mode directories which are not used
        // 2/ Leave only one TopComponent of each type in the COmponent folder and in each mode directory
        // 
        // Get list of used modes (scan files under Preferences/.../*.properties, check for each file if a ".dockMode" line exists and get value)
        Set<String> modes = new HashSet<>();
        String absolutePath = EnvironmentChecker.getProperty("user.home.ancestris", "", "");
        if (!absolutePath.isEmpty()) {
            absolutePath += "/../config/Preferences/ancestris/modules/";
            File dir = new File(absolutePath);
            for (File file : FileUtils.listFiles(dir, new String[]{"properties"}, true)) {
                if (file.getAbsolutePath().contains("ancestris-modules")) {
                    Registry reg = Registry.get(getNodeFromFile(file));
                    for (String key : reg.getProperties()) {
                        if (key.endsWith(".dockMode")) {
                            modes.add(reg.get(key, ""));
                        }
                    }
                }
            }
        }
        absolutePath = EnvironmentChecker.getProperty("user.home.ancestris", "", "");
        if (!absolutePath.isEmpty()) {
            absolutePath += "/../config/Preferences/gedcoms";
            File dir = new File(absolutePath);
            for (File file : FileUtils.listFiles(dir, new String[]{"properties"}, true)) {
                Registry reg = Registry.get(getNodeFromFile(file));
                for (String key : reg.getProperties()) {
                    if (key.endsWith(".dockMode")) {
                        modes.add(reg.get(key, ""));
                    }
                }
            }
        }

        // Remove directory which names are not in the list
        absolutePath = EnvironmentChecker.getProperty("user.home.ancestris", "", "");
        if (!absolutePath.isEmpty()) {
            absolutePath += "/../config/Windows2Local/Modes/";
            File dir = new File(absolutePath);
            // 1. Remove unsed anonymous directories
            for (File file : dir.listFiles()) {
                String name = FilenameUtils.removeExtension(file.getName());
                if (name.startsWith("anonymousMode") && !modes.contains(name)) {
                    try {
                        if (file.isDirectory()) {
                            FileUtils.deleteDirectory(file);
                        } else {
                            file.delete();
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            // 2. Scan each directory and only leave one file of each component name
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    keepOneFileOfEachComponent(file);
                }
            }

        }
        absolutePath = EnvironmentChecker.getProperty("user.home.ancestris", "", "");
        if (!absolutePath.isEmpty()) {
            absolutePath += "/../config/Windows2Local/Components/";
            File dir = new File(absolutePath);
            keepOneFileOfEachComponent(dir);
        }

    }

    static private String getNodeFromFile(File file) {
        String filename = file.getAbsolutePath();
        int i = filename.indexOf("Preferences");
        int j = filename.indexOf(".properties");
        String ret = filename.substring(i + 12, j);   // 12 = len of "Preferences/"
        return ret;
    }

    static private String getComponentFromFile(File file) {
        String filename = file.getName();
        String[] split = filename.split("(\\.|\\_)");
        String ret = split[0];
        return ret;
    }

    static private void keepOneFileOfEachComponent(File file) {
        Set<String> components = new HashSet<>();
        for (File componentFile : file.listFiles()) {
            String name = getComponentFromFile(componentFile);
            if (components.contains(name)) {
                componentFile.delete();
            } else {
                components.add(name);
            }
        }
    }

    /**
     * Exception that can be thrown by operation on trying to use a non existent
     * context
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
        private Map<Gedcom, Timer> gedcomsTimers = new HashMap<Gedcom, Timer>(5);

        /**
         * register gedcom file
         */
        @Override
        protected boolean registerGedcomImpl(GedcomDataObject gedcomObject) {
            Gedcom gedcom = gedcomObject.getContext().getGedcom();
            if (!gedcomsOpened.containsKey(gedcom)) {
                gedcomsOpened.put(gedcom, gedcomObject);
                ActivateTopComponent();
                setAutoSave(gedcomObject.getContext());
            }
            return true;
        }

        /**
         * unregister gedcom file
         */
        //FIXME: we could use vetoable setValid(false) to prevent closing dao if used in editor
        protected boolean unregisterGedcomImpl(Context context) {
            GedcomDataObject gdao = gedcomsOpened.get(context.getGedcom());
            if (gdao != null) {
                try {
                    gdao.setValid(false);
                } catch (PropertyVetoException ex) {
                    return false;
                }
            }
            gedcomsOpened.remove(context.getGedcom());
            removeAutoSave(context);
            return true;
        }

        /**
         *
         * @param gedName
         *
         * @return
         */
        @Override
        public Context getContext(FileObject file) {

            if (file == null) {
                return null;
            }
            for (Gedcom g : gedcomsOpened.keySet()) {
                if (file.equals(gedcomsOpened.get(g).getPrimaryFile())) {
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

        public boolean isGedcomRegistered(Gedcom gedcom) {
            for (Gedcom g : gedcomsOpened.keySet()) {
                if (g == gedcom) {
                    return true;
                }
            }
            return false;
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

        /**
         * Make sure Menu and Tools menu items are enabled at startup FL
         * 2017-12-19 - In case Welcome View is opened at startup, focus needs
         * to be put on a gedcom-context-TopComponent to enable menu actions
         * because registering the gedcom comes after TopComponents are opened
         * and before resultChangedd is triggered
         */
        public void ActivateTopComponent() {
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                @Override
                public void run() {
                    TopComponent tc = WindowManager.getDefault().findTopComponent("CygnusTopComponent"); // Try Cygnus editor
                    if (tc == null) {
                        tc = WindowManager.getDefault().findTopComponent("AriesTopComponent"); // Else Aries editor
                    }
                    if (tc == null) {
                        tc = WindowManager.getDefault().findTopComponent("GedcomTopComponent"); // Else Gedcom editor
                    }
                    if (tc != null) {  // else give up
                        tc.requestActive();
                    }
                }
            });
        }

        public void setAutosaveDelay() {
            for (Context context : getContexts()) {
                setAutoSave(context);
            }
        }

        private void setAutoSave(Context context) {

            // Get autosave option
            int min = ancestris.core.CoreOptions.getInstance().getMinAutosave();
            Timer timer = gedcomsTimers.get(context.getGedcom());
            if (timer == null && min != 0) {
                timer = getNewTimer(min, context);
                gedcomsTimers.put(context.getGedcom(), timer);
                timer.start();
            } else if (timer != null && min != 0) {
                timer.setDelay(min * 1000 * 60);
                timer.start();
            } else if (timer != null && min == 0) {
                timer.stop();
            }
        }

        private void removeAutoSave(Context context) {
            // Stop gedcom timer
            Timer timer = gedcomsTimers.get(context.getGedcom());
            if (timer != null) {
                timer.stop();
            }
        }

        private Timer getNewTimer(int min, final Context context) {
            // Set a gedcom timer to call check autosave every minute
            Timer timer = new Timer(min * 1000 * 60, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Commit all editors without confirmation
                    for (ConfirmChangeWidget.ConfirmChangeCallBack widget : AncestrisPlugin.lookupAll(ConfirmChangeWidget.ConfirmChangeCallBack.class)) {
                        widget.commit(false);
                    }
                    // If gedcom has changed, save it
                    if (context.getGedcom().hasChanged()) {
                        GedcomDirectory.getDefault().saveGedcom(context);
                    }
                }
            });
            timer.setRepeats(true);
            return timer;
        }

    }
}
