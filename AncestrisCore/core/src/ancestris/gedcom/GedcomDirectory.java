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
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.Timer;
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
     *
     * @return
     */
    public abstract List<Context> getContexts();

    /**
     * Gets context for a FileObject if it has been registered
     * null otherwise
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
     * register a {@link GedcomDataObject}.
     * The context key is taken from {@link GedcomDataObject#getContext()}
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
            file = chooseFile(title == null ? create_title() : title, create_action(), null, defaultFilename, true);
            if (file == null) {
                return null;
            }
            if (!file.getName().endsWith(".ged")) {
                file = new File(file.getAbsolutePath() + ".ged");
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
            LOG.log(Level.WARNING, "unexpected exception creating new gedcom", e);
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
            GedcomMgr.getDefault().saveGedcom(new Context(firstIndi), FileUtil.toFileObject(file));
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
     * Main opening Gedcom method. Assumes gedcom could be coming from any other software
     * 
     * FL: 2017-09-24 : Auto-detect where gedcom is coming from and choose proper import module before opening Gedcom
     * 
     * Logic is:
     *  - Detect Gedcom origin
     *      - Open file header line "1 SOUR xxxx" => software = xxxx
     *  - Depending on origin, select import or none if coming from Ancestris
     *      - If software not Ancestris:
     *          - Detect name of software among an import list (lookup xxxx)
     *          - Popup user 
     *              - "File is not an Ancestris Gedcom file, it is coming from software xxxx and needs to be modified to be 100% Gedcom compatible"
     *              - "Ancestris will create a copy 'filename_ancestris.ged', modify it and save it to ....."
     *              - "OK to proceed ?" else cancel operation.
     *          - Run corresponding import
     *              - Fix header and lines (progress bar) 
     *              - Open Gedcom normally (progress bar). There should be no errror message.
     *              - Fix gedcom (it will need to be reopen afterwards to take into account all modifications, therefore it needs to be saved) (progress bar)
     *              - Save gedcom with temporary name without asking user anything
     *              - Open Gedcom normally (progress bar) - this is done withing the saveas function already.
     *          - Popup user 
     *              - "File coming from "xxxx" has been correctly opened and saved to 'filename_ancestris.ged'"
     *              - "The following number of entities have been imported:
     *              - "    xxxx individuals,
     *              - "    xxxx families,
     *              - "    xxxx notes,
     *              - "    xxxx media,
     *              - "    xxxx sources,
     *              - "    xxxx repositories,
     *              - "    xxxx submitters.
     *              - "All the data has been kept but some modifications had to be made to be 100% Gedcom compatible."
     *              - "Do you want to see them ?" Yes, No.
     *              - if Yes, display console.
     *      - Else
     *          - Open Gedcom normally (progress bar)
     * 
     * @param foInput
     * @return context to display
     */
    public Context openGedcom(FileObject foInput) {
        
        // Detect Gedcom origin : Open file header line "1 SOUR xxxx" => software = xxxx
        String software = "";
        boolean stop = false;
        try {
            GedcomFileReader input = GedcomFileReader.create(new File(foInput.getPath()));
            try {
                while ((input.getNextLine(true)) != null && !stop) {
                    if (input.getLevel() == 0 && input.getTag().equals("HEAD")) {
                        continue;
                    }
                    if (input.getLevel() == 1 && input.getTag().equals("SOUR")) {
                        software = input.getValue();
                        stop = true;
                    }
                    if (input.getLevel() == 0 && !input.getTag().equals("HEAD")) {
                        stop = true;
                    }
                }
            } finally {
                input.close();
            }
        } catch (Exception e) {
            LOG.info(e.toString());
            JOptionPane.showMessageDialog(null, e.getMessage());
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
            if (o.toString().toLowerCase().contains(software.toLowerCase())) {
                identifiedImport = o;
                break;
            }
            if (software.toLowerCase().contains(o.toString().toLowerCase())) {
                identifiedImport = o;
                break;
            }
            if (o.isGeneric()) {
                defaultImport = o;
            }
        }
        if (identifiedImport == null) {
            identifiedImport = defaultImport;
        }
        if (identifiedImport == null) {
            LOG.info("Opening a non Ancestris file from " + software + " and cannot find any import module to be used. Using normal Ancestris file open.");
            return openAncestrisGedcom(foInput);
        }

        
        // Popup confirmation to user
        software = identifiedImport.toString();
        String dirname = foInput.getParent().getPath() + System.getProperty("file.separator"); // System.getProperty("java.io.tmpdir") + System.getProperty("file.separator");
        String tmpFileName = foInput.getName()+"_ancestris.ged";
        LOG.info("Opening a non Ancestris file from " + software + ". Using corresponding import module.");
        String message = identifiedImport.isGeneric() ? RES.getString("cc.importGenericGedcom?", foInput.getNameExt(), tmpFileName, dirname) : RES.getString("cc.importGedcom?", foInput.getNameExt(), software, tmpFileName, dirname);
        Object rc = DialogManager.create(RES.getString("cc.open.title"), message).setMessageType(DialogManager.WARNING_MESSAGE).setOptionType(DialogManager.OK_CANCEL_OPTION).show();
        if (rc == DialogManager.CANCEL_OPTION || rc == DialogManager.CLOSED_OPTION) {
            return null;
        }
        LOG.info("Conversion of file from " + software + " confirmed by user.");
        
        // Run corresponding import
        // - Fix header and lines (progress bar) 
        identifiedImport.setTabName(NbBundle.getMessage(Import.class, "OpenIDE-Module-Name") + " - " + identifiedImport.toString());
        File inputFile = new File(foInput.getPath());
        File outFile = new File(dirname + tmpFileName);
        boolean fixedOk = identifiedImport.run(inputFile, outFile);
        if (!fixedOk) {
            return null;  // error messages have been displayed already
        }
        // - Open Gedcom normally (progress bar). There should be no errror message.
        Context context = GedcomDirectory.getDefault().openAncestrisGedcom(FileUtil.toFileObject(outFile));
        if (context == null) {
            return null;  // error messages have been displayed already
        }
        
        // - Fix gedcom (it will need to be reopen afterwards to take into account all modifications, therefore it needs to be saved) (progress bar)
        Gedcom importedGedcom = context.getGedcom();
        importedGedcom.setName(inputFile.getName());
        identifiedImport.fixGedcom(importedGedcom);
        identifiedImport.complete();

        // - Save gedcom with temporary name without asking user anything
        LOG.info("Conversion of file from " + software + " done. Saving to temp file " + dirname + tmpFileName + ".");
        GedcomDirectory.getDefault().saveAsGedcom(context, outFile);
        // - Nothing to do (new file should be opened)

        // Popup user conversion stats
        rc = DialogManager.create(RES.getString("cc.open.title"), 
                RES.getString("cc.importResults?", foInput.getNameExt(), software, 
                        identifiedImport.getIndisNb(), identifiedImport.getFamsNb(), identifiedImport.getNotesNb(), identifiedImport.getObjesNb(),
                        identifiedImport.getSoursNb(), identifiedImport.getReposNb(), identifiedImport.getSubmsNb(), identifiedImport.getChangesNb()))
                .setMessageType(DialogManager.WARNING_MESSAGE).setOptionType(DialogManager.YES_NO_OPTION).show();
        if (rc == DialogManager.YES_OPTION) {
            identifiedImport.showDetails();
        }

        return null;
    }
    
    
    
    
    
    /**
     * 
     * Open Gedcom normally (witout any correction) : assumes input file is an Ancestris gedcom file.
     * 
     *      Opens a Gedcom FileObject.
     * 
     *      Use DataObject loaders to find the proper handler and then register it in local
     *      gedcom registry. Il file is already opened, doesn't open twice and return
     *      the saved context
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
            DataObject dao = DataObject.find(input); // loads gedcom file here
            GedcomDataObject gdao = dao.getLookup().lookup(GedcomDataObject.class);
            if (gdao == null) {
                return null;
            }
            if (gdao.isCancelled()) {
                gdao.setCancelled(false);
                return null;
            }
            if (gdao.getContext() == null) {
                if (!gdao.load()) {
                    return null;
                }
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
        if (context != null
                && (context.getGedcom().getOrigin() == null
                || !context.getGedcom().getOrigin().getFile().exists())) {
            return saveAsGedcom(context, null);
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
    public boolean saveAsGedcom(Context context, File outputFile) {

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
            File file = chooseFile(RES.getString("cc.save.title", context.getGedcom().toString()), RES.getString("cc.save.action"), options, context.getGedcom().toString(), true);
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
        try{
            // if gedcom is no longer in directory don't close it again. 
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
//            if (GedcomMgr.getDefault().gedcomClose(context)) {
            unregisterGedcom(context);
            GedcomMgr.getDefault().gedcomClose(context) ;
            return true;
//            } else {
//                return false;
//            }
        } catch (ContextNotFoundException e){
            
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
     * Let the user choose a file (false) or create one (true)
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

        FileChooserBuilder fbc  = new FileChooserBuilder(GedcomDirectory.class)
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
            LOG.log(Level.FINE, "{0}: {1} opened", new Object[]{TimingUtility.getInstance().getTime(), clazz.getCanonicalName()});
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
        private Map<Gedcom, Timer> gedcomsTimers = new HashMap<Gedcom, Timer>(5);

        /**
         * register gedcom file
         */
        @Override
        protected boolean registerGedcomImpl(GedcomDataObject gedcomObject) {
            Gedcom gedcom = gedcomObject.getContext().getGedcom();
            if (!gedcomsOpened.containsKey(gedcom)) {
                gedcomsOpened.put(gedcom, gedcomObject);
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
                    // Save gedcom has changed, save it
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