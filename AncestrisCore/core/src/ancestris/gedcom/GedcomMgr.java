/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.gedcom;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.core.pluginservice.PluginInterface;
import ancestris.core.resources.Images;
import ancestris.util.ProgressListener;
import ancestris.util.TimingUtility;
import ancestris.util.swing.DialogManager;
import ancestris.view.AncestrisViewInterface;
import genj.common.ContextListWidget;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.PropertyFile;
import genj.gedcom.UnitOfWork;
import genj.io.BackupFile;
import genj.io.Filter;
import genj.io.GedcomEncodingException;
import genj.io.GedcomIOException;
import genj.io.GedcomReader;
import genj.io.GedcomReaderContext;
import genj.io.GedcomReaderFactory;
import genj.io.GedcomWriter;
import genj.io.IGedcomWriter;
import genj.io.InputSource;
import genj.util.Origin;
import genj.util.Registry;
import genj.util.Resources;
import genj.view.ViewContext;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import org.apache.commons.io.FileUtils;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import spin.Spin;

/**
 * This class deals with gedcom files operation (open, save or new file creation).
 *
 * Generally, this class must not be overidden, consider {@link GedcomDirectory} to
 * extend internal gedcom registry functionnality. The only case where overiding this
 * class is necessary is to provide another backend or gedcom file syntax.
 * <p>
 * All file operations are done thru {@link FileObject} objects.
 *
 * @author daniel
 */
//XXX: cleanup this api
/** singleton pattern */
public abstract class GedcomMgr {

    final static Logger LOG = Logger.getLogger("ancestris.app");
    final static Resources RES = Resources.get(GedcomMgr.class);
    final static Registry REGISTRY = Registry.get(GedcomMgr.class);
    
    private static boolean quiet = false;
    private boolean copyMediaInError = false;

    /**
     * Opens an existing gedcom file
     *
     * @param input
     *
     * @return
     */
//    public Context newGedcom(FileObject input);
//    public abstract Context newGedcom();
//    public abstract boolean saveGedcom(Context context, FileObject fo);
//    public abstract boolean saveGedcom(Context context);
    public abstract boolean saveGedcomImpl(Gedcom gedcom, Collection<Filter> filters, FileObject outFile);

    public abstract Context openGedcom(FileObject input);

    public abstract Context setGedcom(Gedcom gedcom);

    public abstract void setQuiet(boolean set);
    
    public abstract boolean isQuiet();
    
    /**
     * Handle GedcomFileEvents (notify listeners)
     */
    //XXX: we could probably put this in Gedcom.commitRequested()
    protected void commitRequested(Context context) {
        for (GedcomFileListener listener : AncestrisPlugin.lookupAll(GedcomFileListener.class)) {
            listener.commitRequested(context);
        }
    }

    protected void gedcomOpened(Gedcom gedcom) {
        for (GedcomFileListener listener : AncestrisPlugin.lookupAll(GedcomFileListener.class)) {
            listener.gedcomOpened(gedcom);
        }
    }

    /**
     * Handle GedcomFileEvents (notify listeners)
     */
    protected void gedcomClosed(Gedcom gedcom) {
        for (GedcomFileListener listener : AncestrisPlugin.lookupAll(GedcomFileListener.class)) {
            listener.gedcomClosed(gedcom);
        }
    }

    public boolean saveGedcom(Context context) {

        // ask everyone to commit their data
        commitRequested(context);

        // do it
        Gedcom gedcom = context.getGedcom();
        if (!saveGedcomImpl(gedcom, null, null)) {
            return false;
        }
        // .. note changes are saved now
        if (gedcom.hasChanged()) {
            gedcom.doMuteUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    gedcom.setUnchanged();
                }
            });
        }

        // .. done
        return true;

    }

    public Origin saveGedcomAs(Context context, SaveOptionsWidget options, FileObject output) {
        Gedcom gedcom = context.getGedcom();
        if (options == null) {
            options = new SaveOptionsWidget(gedcom);
        }

        // Remember some previous values before setting them
        String prevPassword = gedcom.getPassword();
        String prevEncoding = gedcom.getEncoding();
        Origin prevOrigin = gedcom.getOrigin();

        if (options.isIsGedcom()) {
            gedcom.setPassword(options.getPassword());
            gedcom.setEncoding(options.getEncoding());
        }

        Origin newOrigin;
        // .. create new origin
        try {
            newOrigin = Origin.create(output.toURL());
            gedcom.setOrigin(newOrigin);
        } catch (Throwable t) {
            LOG.log(Level.FINER, "Failed to create origin for file " + output, t);
            // restore
            gedcom.setEncoding(prevEncoding);
            gedcom.setPassword(prevPassword);
            gedcom.setOrigin(prevOrigin);
            return null;
        }

        // copy media files when necessary
        if (options.isIsGedcom()&& options.areMediaToBeCopied()) {
            int undoNb = gedcom.getUndoNb();
            final Origin prevOriginUoW = prevOrigin;
            final Origin newOriginUoW = newOrigin;
            copyMediaInError = false;
            final Filter filter = new Filter.Union(gedcom, options.getFilters());
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {
                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        copyMedia(gedcom, prevOriginUoW, newOriginUoW, filter);
                    }
                });
            } catch (Exception ex) {
            }
            if (copyMediaInError) {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
                gedcom.setEncoding(prevEncoding);
                gedcom.setPassword(prevPassword);
                gedcom.setOrigin(prevOrigin);
                return null;
            }
        }

        // save
        if (!saveGedcomImpl(gedcom, options.getFilters(), null)) {
            gedcom.setEncoding(prevEncoding);
            gedcom.setPassword(prevPassword);
            gedcom.setOrigin(prevOrigin);
            return null;
        }


//FIXME: temporarily removed            if (writer.hasFiltersVetoed()) {
//                gedcom.setEncoding(prevEncoding);
//                gedcom.setPassword(prevPassword);
//                gedcom.setOrigin(prevOrigin);
//                return newOrigin;
//            }

        // .. note changes are saved now
        if (gedcom.hasChanged()) {
            gedcom.doMuteUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    gedcom.setUnchanged();
                }
            });
        }

        // .. done
        return gedcom.getOrigin();
    }

    public boolean gedcomClose(final Context context) {
        // tell
        gedcomClosed(context.getGedcom());

        // remember context
        context.getGedcom().getRegistry().put("context", context.toString());

        
        // closes all views
        for (AncestrisViewInterface gjvTc : AncestrisPlugin.lookupAll(AncestrisViewInterface.class)) {
            if (context.getGedcom().equals(gjvTc.getGedcom())) {
                gjvTc.close();
            }
        }

        // Release gedcom and clear memory
        context.getGedcom().eraseAll();

        return true;
    }
    /** Instance of default gedcom manager. */
    private static GedcomMgr defaultInstance;

    /** Singleton instance accessor method for gedcom directory.
     *
     * @return instance of gedcom directory installed in the system
     */
    public static GedcomMgr getDefault() {
        GedcomMgr gdInstance = Lookup.getDefault().lookup(GedcomMgr.class);

        return (gdInstance != null) ? gdInstance : getDefaultInstance();
    }

    private static synchronized GedcomMgr getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new DefaultGedcomMgrImpl();
        }

        return defaultInstance;
    }

    private boolean copyMedia(Gedcom gedcom, Origin prevOrigin, Origin newOrigin, Filter filter) {

        // Get all file properties
        List<PropertyFile> files = (List<PropertyFile>) gedcom.getPropertiesByClass(PropertyFile.class);

        // Initialize directories
        String prevDir = null, newDir = null;
        try {
            prevDir = prevOrigin.getFile().getParentFile().getCanonicalPath();
            newDir = newOrigin.getFile().getParentFile().getCanonicalPath();

            // Loop on files to convert paths and copy them
            for (PropertyFile mediaFile : files) {
                if (filter.veto(mediaFile.getEntity()) || filter.veto(mediaFile)) {
                    continue;
                }
                File newMediafile;
                
                InputSource prevMediafile = null;
                String relPath = mediaFile.getValue();
                Path p = Paths.get(relPath).normalize();
                if (p.isAbsolute()) {
                    relPath = p.subpath(0, p.getNameCount() - 1).toString() + File.separator + p.getFileName(); // make absolute path relative
                    Optional<InputSource> ois = mediaFile.getInput();
                    if (ois.isPresent()) {
                        prevMediafile = ois.get();
                    }   
                    mediaFile.setValue(relPath);
                } else {
                    prevMediafile = InputSource.get(new File(prevDir + File.separator + relPath)).get();
                }
                newMediafile = new File(newDir + File.separator + relPath);

                // Now update mediafile value and copy file preserving file date
                if (prevMediafile != null) {
                    
                    FileUtils.copyInputStreamToFile(prevMediafile.open(), newMediafile);
                } else {
                    String fileErr = prevMediafile.getLocation();
                    String msg = RES.getString("save.options.files.medianotfound", fileErr, mediaFile.getEntity());
                    LOG.log(Level.FINER, "Failed to copy media : " + fileErr);
                    if (DialogManager.YES_OPTION != DialogManager.createYesNo(RES.getString("save.options.files"), msg).setMessageType(DialogManager.WARNING_MESSAGE).show()) {
                        copyMediaInError = true;
                        return false;
                    }
                }
            }
        } catch (Exception ex) {
            //Exceptions.printStackTrace(ex);
        }
        return true;
    }

    
    
    private static class DefaultGedcomMgrImpl extends GedcomMgr {
        
        @Override
        public void setQuiet(boolean set) {
            quiet = set;
        }

        @Override
        public boolean isQuiet() {
            return quiet;
        }

        @Override
        public Context openGedcom(final FileObject input) {
            if (input == null) {
                return null;
            }

            // Open Connection and get input stream
            Origin origin = Origin.create(input);
            final List<ViewContext> warnings = new ArrayList<>();
            final List<Object> bag = new ArrayList<>(); // stores something if header has warnings
            GedcomReader reader;
            try {

                // .. prepare our reader
                reader = (GedcomReader) Spin.off(GedcomReaderFactory.createReader(origin, (GedcomReaderContext) Spin.over(new GedcomReaderContext() {

                    @Override
                    public String getPassword() {
                        return DialogManager.create(FileUtil.getFileDisplayName(input), RES.getString("cc.provide_password"), "")
                                .show();
                    }

                    @Override
                    public void handleWarning(int line, String warning, Context context) {
                        if (line == 0 && bag.isEmpty()) {
                            bag.add("header");
                        }
                        warnings.add(new ViewContext(RES.getString("cc.open.warning", new Object[]{line, warning}), context));
                    }
                })));

            } catch (IOException ex) {
                String txt = RES.getString("cc.open.no_connect_to", input) + "\n[" + ex.getMessage() + "]";
                DialogManager.createError(FileUtil.getFileDisplayName(input), txt).show();
                return null;
            }

            final Context context;
            try {
                ProgressListener.Dispatcher.processStarted(reader);
                Gedcom gedcom = reader.read();
                if (gedcom != null) {
                    context = setGedcom(gedcom);
                } else {
                    return null;
                }
                gedcom.setWarnings(warnings);
                if (!warnings.isEmpty() && !quiet) {
                    JButton updatePropertiesButton = new JButton(RES.getString("cc.open.fixWarnings"));
                    updatePropertiesButton.addActionListener(new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            List<PluginInterface> list = (List<PluginInterface>) Lookup.getDefault().lookupAll(PluginInterface.class);
                            for (PluginInterface pi : list) {
                                if (pi.getPluginName().equals("modules.editors.gedcomproperties")) {
                                    pi.launchModule(context);
                                }
                            }
                        }
                        
                    });
                    
                    boolean hasHeaderWarnings = !bag.isEmpty();
                    JScrollPane panel = new JScrollPane(new ContextListWidget(warnings));
                    JLabel label = new JLabel(RES.getString("cc.open.warnings.tooltip"));
                    label.setIcon(Images.imgWng);
                    DialogManager.create(RES.getString("cc.open.warnings", context.getGedcom().getName()), new JComponent[] {panel, label}, false)
                            .setDialogId("cc.open.warnings")
                            .setOptions(hasHeaderWarnings ? new Object[]{updatePropertiesButton, NotifyDescriptor.CLOSED_OPTION} : new Object[]{NotifyDescriptor.CLOSED_OPTION})
                            .show();
                }
            } catch (GedcomIOException ex) {
                // Tell the user about it
                DialogManager.createError(FileUtil.getFileDisplayName(input),
                        ex.getMessage() + "\n(" + RES.getString("cc.open.read_error", "" + ex.getLine()) + ")\n" ).show();
                // For later, store the fact in the dataobject that things went wrong when loading the file
                DataObject dao;
                try {
                    dao = DataObject.find(input);
                    GedcomDataObject gdao = dao.getLookup().lookup(GedcomDataObject.class);
                    if (gdao != null) {
                        gdao.setCancelled(true);
                    }
                } catch (DataObjectNotFoundException ex1) {
                    //Exceptions.printStackTrace(ex1);
                }
                // abort
                return null;
            } finally {
                ProgressListener.Dispatcher.processStopped(reader);
            }

            // remember
            RecentFiles.getDefault().add(input);

            // done
            return context;
        }

        @Override
        public Context setGedcom(Gedcom gedcom) {
            LOG.log(Level.FINE, "{0}: setGedcom", TimingUtility.getInstance().getTime());
            Context context = new Context();

            // restore context
            try {
                Registry gedcomRegistry = gedcom.getRegistry();
                context = Context.fromString(gedcom, gedcomRegistry.get("context", gedcom.getName()));
            } catch (GedcomException ge) {
            } finally {
                // fixup context if necessary - start with adam if available
                Entity adam = gedcom.getFirstEntity(Gedcom.INDI);
                if (context.getEntities().isEmpty()) {
                    context = new Context(gedcom, adam != null ? Collections.singletonList(adam) : null, null);
                }
            }

            // tell everone
            LOG.log(Level.FINE, "{0}: gedcomOpened", TimingUtility.getInstance().getTime());
            gedcomOpened(gedcom);

            LOG.log(Level.FINE, "{0}: fireSelection", TimingUtility.getInstance().getTime());
            //FIXME: done also in GedcomDirectory. Must not be done here as this class is general purpose
            //SelectionSink.Dispatcher.fireSelection(null, context, true);
            return context;

            // done
        }

        /**
         * save gedcom file
         */
        //XXX: use fileobject api and outfile parameter
        @Override
        public boolean saveGedcomImpl(Gedcom gedcom, Collection<Filter> filters, FileObject outFile) {
            IGedcomWriter writer;

            try {

                // prep files and writer
                File file = null, temp = null;
                try {
                    // .. resolve to canonical file now to make sure we're writing to the
                    // file being pointed to by a symbolic link
                    //XXX: use fileobject api
                    file = gedcom.getOrigin().getFile().getCanonicalFile();

                    // .. create a temporary output
                    temp = File.createTempFile("ancestris", ".ged", file.getParentFile());

                    // .. create writer
                    writer = (IGedcomWriter) Spin.off(new GedcomWriter(gedcom, new FileOutputStream(temp)));

                } catch (GedcomEncodingException gee) {
                    DialogManager.createError(gedcom.getName(), RES.getString("cc.save.write_encoding_error", gee.getMessage())).show();
                    LOG.log(Level.SEVERE, "Cannot encode gedcom " + gedcom.getName() + ". Error is : "+ gee.getLocalizedMessage());
                    return false;
                } catch (IOException ex) {
                    if (file == null) {
                        DialogManager.createError(gedcom.getName(), RES.getString("cc.save.write_error", gedcom.getOrigin().getFile().getAbsolutePath(), ex.getLocalizedMessage())).show();
                        LOG.log(Level.SEVERE, "Cannot get cannonical file for gedcom " + gedcom.getName() + ". Error is : "+ ex.getLocalizedMessage());
                    }
                    if (temp == null) {
                        DialogManager.createError(gedcom.getName(), RES.getString("cc.save.write_error", file.getParentFile().getAbsolutePath(), ex.getLocalizedMessage())).show();
                        LOG.log(Level.SEVERE, "Cannot create gedcom file " + gedcom.getName() + " in directory " + file.getParentFile().getAbsolutePath() + ". Error is : "+ ex.getLocalizedMessage());
                    }
                    return false;
                }

                if (filters != null) {
                    writer.setFilters(filters);
                }

                // .. write it
                try {
                    ProgressListener.Dispatcher.processStarted(writer);
                    writer.write();
                } finally {
                    ProgressListener.Dispatcher.processStopped(writer);
                }

                // .. make backup
                if (BackupFile.createBackup(file)) {
                    file.delete();
                    // .. and now !finally! move from temp to result
                    if (!temp.renameTo(file)) {
                        throw new GedcomIOException("Couldn't move temporary " + temp.getName() + " to " + file.getName(), -1);
                    }
                }

            } catch (GedcomEncodingException gee) {
                DialogManager.createError(gedcom.getName(), RES.getString("cc.save.write_error", gee.getMessage())).show();
                LOG.log(Level.SEVERE, "Cannot encode gedcom " + gedcom.getName() + ". Error is : "+ gee.getLocalizedMessage());
                return false;
            } catch (GedcomIOException gioex) {
                DialogManager.createError(gedcom.getName(), RES.getString("cc.save.write_error", "" + gioex.getLine()) + ":\n" + gioex.getMessage()).show();
                return false;
            }

            // .. done
            return true;
        }
    }
}
