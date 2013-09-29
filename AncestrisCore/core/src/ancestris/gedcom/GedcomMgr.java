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
import ancestris.util.ProgressListener;
import ancestris.util.TimingUtility;
import ancestris.view.AncestrisViewInterface;
import genj.common.ContextListWidget;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
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
import genj.util.Origin;
import genj.util.Registry;
import genj.util.Resources;
import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.util.swing.DialogManager;
import genj.util.swing.DialogHelper;
import genj.view.ViewContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import spin.Spin;

/**
 * This class deals with gedcom files operation (open, save or new file creation).
 *
 * Generally, this class must not be overidden, consider {@link GedcomDirectory} to
 * extend internal gedcom registry functionnality. The only case where overiding this
 * class is necessary is to provide another backend or gedcom file syntax.
 * <p>All file operations are done thru {@link FileObject} objects.
 *
 * @author daniel
 */
//XXX: cleanup this api
/** singleton pattern */
public abstract class GedcomMgr {

    final static Logger LOG = Logger.getLogger("ancestris.app");
    final static Resources RES = Resources.get(GedcomMgr.class);
    final static Registry REGISTRY = Registry.get(GedcomMgr.class);

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

    public boolean saveGedcom(Context context, FileObject output) {

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

        // Remember some previous values before setting them
        String prevPassword = gedcom.getPassword();
        String prevEncoding = gedcom.getEncoding();
        Origin prevOrigin = gedcom.getOrigin();

        gedcom.setPassword(options.getPassword());
        gedcom.setEncoding(options.getEncoding());

        Origin newOrigin;
        // .. create new origin
        try {
            newOrigin = Origin.create(output.getURL());
            gedcom.setOrigin(newOrigin);
        } catch (Throwable t) {
            LOG.log(Level.FINER, "Failed to create origin for file " + output, t);
            // restore
            gedcom.setEncoding(prevEncoding);
            gedcom.setPassword(prevPassword);
            gedcom.setOrigin(prevOrigin);
            return null;
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
        return null;
    }

    public boolean gedcomClose(Context context) {
        // tell
        gedcomClosed(context.getGedcom());

        // remember context
        context.getGedcom().getRegistry().put("context", context.toString());

        // closes all views
        for (AncestrisViewInterface gjvTc : AncestrisPlugin.lookupAll(AncestrisViewInterface.class)) {
            if (context.getGedcom().equals(gjvTc.getGedcom())) {
                if (!gjvTc.close()) {
                    return false;
                }
            }
        }
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

    private static class DefaultGedcomMgrImpl extends GedcomMgr {

        @Override
        public Context openGedcom(final FileObject input) {
            if (input == null) {
                return null;
            }

            // Open Connection and get input stream
            Origin origin = Origin.create(input);
            final List<ViewContext> warnings = new ArrayList<ViewContext>();
            GedcomReader reader;
            try {

                // .. prepare our reader
                reader = (GedcomReader) Spin.off(GedcomReaderFactory.createReader(origin, (GedcomReaderContext) Spin.over(new GedcomReaderContext() {

                    public String getPassword() {
                        return DialogManager.create(FileUtil.getFileDisplayName(input), RES.getString("cc.provide_password"), "")
                                .show();
                    }

                    public void handleWarning(int line, String warning, Context context) {
                        warnings.add(new ViewContext(RES.getString("cc.open.warning", new Object[]{Integer.valueOf(line), warning}), context));
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
                if (!warnings.isEmpty()) {
                    NotifyDescriptor nd = new DialogDescriptor(
                            new JScrollPane(new ContextListWidget(warnings)), RES.getString("cc.open.warnings", context.getGedcom().getName()),
                            false,
                            new Object[]{NotifyDescriptor.CLOSED_OPTION},
                            null,
                            DialogDescriptor.DEFAULT_ALIGN,
                            null, null);
                    DialogDisplayer.getDefault().notify(nd);
                }
            } catch (GedcomIOException ex) {
                // tell the user about it
                DialogManager.createError(FileUtil.getFileDisplayName(input), 
                        RES.getString("cc.open.read_error", "" + ex.getLine()) + ":\n" + ex.getMessage()).show();
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

        public Context setGedcom(Gedcom gedcom) {
            LOG.log(Level.FINE, "{0}: setGedcom", TimingUtility.geInstance().getTime());
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
            LOG.log(Level.FINE, "{0}: gedcomOpened", TimingUtility.geInstance().getTime());
            gedcomOpened(gedcom);

            LOG.log(Level.FINE, "{0}: fireSelection", TimingUtility.geInstance().getTime());
            //FIXME: done also in GedcomDirectory. Must not be done here as this class is general purpose
            //SelectionSink.Dispatcher.fireSelection(null, context, true);
            return context;

            // done
        }

        /**
         * save gedcom file
         */
        //XXX: use fileobject api and outfile parameter
        public boolean saveGedcomImpl(Gedcom gedcom, Collection<Filter> filters, FileObject outFile) {
            IGedcomWriter writer;

            try {

                // prep files and writer
                File file, temp;
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
                    return false;
                } catch (IOException ex) {
                    DialogManager.createError(gedcom.getName(), RES.getString("cc.save.open_error", gedcom.getOrigin().getFile().getAbsolutePath())).show();
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

            } catch (GedcomIOException gioex) {
                DialogManager.createError(gedcom.getName(),  RES.getString("cc.save.write_error", "" + gioex.getLine()) + ":\n" + gioex.getMessage()).show();
                return false;
            }

            // .. done
            return true;
        }
    }
}
