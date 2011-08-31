/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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
package genj.app;

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
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import genj.view.MySelectionListener;
import genj.view.SelectionSink;
import genj.view.View;
import genj.view.ViewContext;
import genj.view.ViewFactory;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.core.pluginservice.PluginInterface;
import ancestris.gedcom.GedcomDirectory;
import ancestris.util.ProgressBar;
import ancestris.view.AncestrisViewInterface;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Submitter;
import genj.util.AncestrisPreferences;
import genj.util.EnvironmentChecker;
import genj.util.ServiceLookup;
import genj.util.Trackable;
import genj.util.swing.FileChooser;
import genj.view.SelectionListener;
import java.awt.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;


import spin.Spin;

/**
 * The central component of the GenJ application
 */
public class Workbench /*extends JPanel*/ implements SelectionSink, GedcomMetaListener {

    /*package*/ final static Logger LOG = Logger.getLogger("genj.app");
    /*package*/ final static Resources RES = Resources.get(Workbench.class);
    /*package*/ final static Registry REGISTRY = Registry.get(Workbench.class);
// Callback
//      IWorkbenchHelper helper;
    // Instance
    private ProgressBar progress;
    private static Workbench instance = null;
    /** members */
    private IGedcomWriter writer = null;

    private Workbench() {
        progress = new ProgressBar();
        AncestrisPlugin.register(progress);
    }

    public static Workbench getInstance() {
        if (instance == null) {
            instance = new Workbench();
            for (PluginFactory pf : ServiceLookup.lookup(PluginFactory.class)) {
              LOG.info("Activate plugin "+pf.getClass());
              Object plugin = pf.createPlugin();
            }
        }
        return instance;
    }

    /**
     * create a new gedcom file
     */
    public Context newGedcom() {

        // let user choose a file
        File file = chooseFile(RES.getString("cc.create.title"), RES.getString("cc.create.action"), null);
        if (file == null) {
            return null;
        }
        if (!file.getName().endsWith(".ged")) {
            file = new File(file.getAbsolutePath() + ".ged");
        }
        if (file.exists()) {
            int rc = DialogHelper.openDialog(RES.getString("cc.create.title"), DialogHelper.WARNING_MESSAGE, RES.getString("cc.open.file_exists", file.getName()), Action2.yesNo(), null);
            if (rc != 0) {
                return null;
            }
        }

        // form the origin
        Gedcom gedcom;
        try {
            gedcom = new Gedcom(Origin.create(new URL("file:" + file.getAbsolutePath())));
        } catch (MalformedURLException e) {
            LOG.log(Level.WARNING, "unexpected exception creating new gedcom", e);
            return null;
        }

        // done
        //FIXME: changer le nouveau gedcom cree par defaut!
        Context context = setGedcom(gedcom);
        try {
            setDefault(gedcom);
            // remember
            GedcomDirectory.getInstance().registerGedcom(context);
            Indi firstIndi = (Indi) context.getGedcom().getFirstEntity(Gedcom.INDI);
            if (firstIndi == null) {
                firstIndi = (Indi) context.getGedcom().createEntity(Gedcom.INDI);
            }
            GedcomDirectory.getInstance().updateModified(gedcom);
            openDefaultViews(new Context(firstIndi));
            SelectionSink.Dispatcher.fireSelection((Component) null, new Context(firstIndi), true);
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return context;
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

        // form origin
        try {
            return openGedcom(new URL("file:" + file.getAbsolutePath()));
        } catch (Throwable t) {
            // shouldn't
            LOG.info(t.toString());
            return null;
        }
        // done
    }

    /**
     * loads gedcom file
     */
    public Context openGedcom(URL url) {

        // open connection
        final Origin origin = Origin.create(url);

        // Open Connection and get input stream
        final List<ViewContext> warnings = new ArrayList<ViewContext>();
        GedcomReader reader;
        try {

            // .. prepare our reader
            reader = (GedcomReader) Spin.off(GedcomReaderFactory.createReader(origin, (GedcomReaderContext) Spin.over(new GedcomReaderContext() {

                public String getPassword() {
                    return DialogHelper.openDialog(origin.getName(), DialogHelper.QUESTION_MESSAGE, RES.getString("cc.provide_password"), "", null);
                }

                public void handleWarning(int line, String warning, Context context) {
                    warnings.add(new ViewContext(RES.getString("cc.open.warning", new Object[]{new Integer(line), warning}), context));
                }
            })));

        } catch (IOException ex) {
            String txt = RES.getString("cc.open.no_connect_to", origin) + "\n[" + ex.getMessage() + "]";
            DialogHelper.openDialog(origin.getName(), DialogHelper.ERROR_MESSAGE, txt, Action2.okOnly(), null);
            return null;
        }

        Context context = null;
        try {
            processStarted(reader);
            context = setGedcom(reader.read());
            // FIXME: Afficher la liste des erreurs
//      if (!warnings.isEmpty()) {
//        dockingPane.putDockable("warnings", new GedcomDockable(this,
//            RES.getString("cc.open.warnings", context.getGedcom().getName()),
//            IMG_OPEN,
//            new JScrollPane(new ContextListWidget(warnings)))
//        );
//      }
        } catch (GedcomIOException ex) {
            // tell the user about it
            DialogHelper.openDialog(origin.getName(), DialogHelper.ERROR_MESSAGE, RES.getString("cc.open.read_error", "" + ex.getLine()) + ":\n" + ex.getMessage(), Action2.okOnly(), null);
            // abort
            return null;
        } finally {
            processStopped(reader);
        }

        // remember
        List<String> history = REGISTRY.get("history", new ArrayList<String>());
        history.remove(origin.toString());
        history.add(0, origin.toString());
        if (history.size() > 5) {
            history.remove(history.size() - 1);
        }
        REGISTRY.put("history", history);

        // done
        setContext(context);
        return context;
    }

    private void setContext(Context context) {
        if (context == null) {
            return;
        }
        GedcomDirectory.getInstance().registerGedcom(context);
        openDefaultViews(context);
        //FIXME: etait true. Cela faisait changer le root dans l'arbre
        // bizarre car cela ne devrait pas etre le cas meme avec true...
        // Voir si avec false il n'y a pas d'effet de bord et si cela corrige le pb de prise en compte du root
        SelectionSink.Dispatcher.fireSelection((Component) null, context, false);
    }

    public Context setGedcom(Gedcom gedcom) {
        Context context = new Context();

        // restore context
        try {
//      context = Context.fromString(gedcom, REGISTRY.get(gedcom.getName()+".context", gedcom.getName()));
            Registry r = gedcom.getRegistry();
            context = Context.fromString(gedcom, r/*gedcom.getRegistry()*/.get("context", gedcom.getName()));
        } catch (GedcomException ge) {
        } finally {
            // fixup context if necessary - start with adam if available
            Entity adam = gedcom.getFirstEntity(Gedcom.INDI);
            if (context.getEntities().isEmpty()) {
                context = new Context(gedcom, adam != null ? Collections.singletonList(adam) : null, null);
            }
        }

        // tell everone
        gedcomOpened(gedcom);

        fireSelection(null, context, true);
        return context;

        // done
    }

    public boolean saveAsGedcom(Context context) {
        Origin o = saveAsGedcomImpl(context);
        if (o != null) {
            try {
                openGedcom(o.getFile().toURL());
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
            return true;
        } else {
            if (context != null) {
                GedcomDirectory.getInstance().unregisterGedcom(context);
                GedcomDirectory.getInstance().registerGedcom(context);
            }
            return false;
        }
    }

    /**
     * save gedcom to a new file
     * @return new origin if filters applied (ie exported to a new file), null otherwise
     */
    private Origin saveAsGedcomImpl(Context context) {

        if (context == null || context.getGedcom() == null) {
            return null;
        }

        // ask everyone to commit their data
        fireCommit(context);

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
        File file = chooseFile(RES.getString("cc.save.title",context.getGedcom().toString()), RES.getString("cc.save.action"), options);
        if (file == null) {
            return null;
        }

        // .. take chosen one & filters
        if (!file.getName().endsWith(".ged")) {
            file = new File(file.getAbsolutePath() + ".ged");
        }

        // Need confirmation if File exists?
        if (file.exists()) {
            int rc = DialogHelper.openDialog(RES.getString("cc.save.title",context.getGedcom().toString()), DialogHelper.WARNING_MESSAGE, RES.getString("cc.open.file_exists", file.getName()), Action2.yesNo(), null);
            if (rc != 0) {
                return null;
            }
        }

        Gedcom gedcom = context.getGedcom();

        // Remember some previous values before setting them
        String prevPassword = gedcom.getPassword();
        String prevEncoding = gedcom.getEncoding();
        Origin prevOrigin = gedcom.getOrigin();

        gedcom.setPassword(options.getPassword());
        gedcom.setEncoding(options.getEncoding());

        Origin newOrigin = null;
        // .. create new origin
        try {
            newOrigin = Origin.create(new URL("file", "", file.getAbsolutePath()));
            gedcom.setOrigin(newOrigin);
        } catch (Throwable t) {
            LOG.log(Level.FINER, "Failed to create origin for file " + file, t);
            // restore
            gedcom.setEncoding(prevEncoding);
            gedcom.setPassword(prevPassword);
            gedcom.setOrigin(prevOrigin);
            return null;
        }

        // save
        if (!saveGedcomImpl(gedcom, options.getFilters())) {
            gedcom.setEncoding(prevEncoding);
            gedcom.setPassword(prevPassword);
            gedcom.setOrigin(prevOrigin);
            return null;
        }
        if (writer.hasFiltersVetoed()) {
            gedcom.setEncoding(prevEncoding);
            gedcom.setPassword(prevPassword);
            gedcom.setOrigin(prevOrigin);
            return newOrigin;
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
        return null;
    }

    /**
     * save gedcom file
     */
    public boolean saveGedcom(Context context) {
        if (context != null && context.getGedcom().getOrigin() == null) {
            return saveAsGedcom(context);
        }
        if (context.getGedcom() == null) {
            return false;
        }

        // ask everyone to commit their data
        fireCommit(context);

        // do it
        Gedcom gedcom = context.getGedcom();
        if (!saveGedcomImpl(gedcom, null)) {
            return false;
        }
        // .. note changes are saved now
        if (gedcom.hasChanged()) {
            gedcom.doMuteUnitOfWork(new UnitOfWork() {

                public void perform(Gedcom gedcom) throws GedcomException {
                    gedcom.setUnchanged();
                }
            });
        }

        // .. done
        return true;

    }

    /**
     * save gedcom file
     */
    public boolean saveGedcomImpl(Gedcom gedcom, Collection<Filter> filters) {

//  // .. open progress dialog
//  progress = WindowManager.openNonModalDialog(null, RES.getString("cc.save.saving", file.getName()), WindowManager.INFORMATION_MESSAGE, new ProgressWidget(gedWriter, getThread()), Action2.cancelOnly(), getTarget());

        try {

            // prep files and writer
            writer = null;
            File file = null, temp = null;
            try {
                // .. resolve to canonical file now to make sure we're writing to the
                // file being pointed to by a symbolic link
                file = gedcom.getOrigin().getFile().getCanonicalFile();

                // .. create a temporary output
                temp = File.createTempFile("ancestris", ".ged", file.getParentFile());

                // .. create writer
                writer = (IGedcomWriter) Spin.off(new GedcomWriter(gedcom, new FileOutputStream(temp)));


            } catch (GedcomEncodingException gee) {
                DialogHelper.openDialog(gedcom.getName(), DialogHelper.ERROR_MESSAGE, RES.getString("cc.save.write_encoding_error", gee.getMessage()), Action2.okOnly(), null);
                return false;
            } catch (IOException ex) {
                DialogHelper.openDialog(gedcom.getName(), DialogHelper.ERROR_MESSAGE, RES.getString("cc.save.open_error", gedcom.getOrigin().getFile().getAbsolutePath()), Action2.okOnly(), null);
                return false;
            }

            if (filters != null) {
                writer.setFilters(filters);
            }

            // .. write it
            try {
                processStarted(writer);
                writer.write();
            } finally {
                processStopped(writer);
            }

            // .. make backup
            BackupFile.createBackup(file);
//      if (file.exists()) {
//        File bak = new File(file.getAbsolutePath() + "~");
//        if (bak.exists()&&!bak.delete())
//          throw new GedcomIOException("Couldn't delete backup file " + bak.getName(), -1);
//        if (!file.renameTo(bak))
//          throw new GedcomIOException("Couldn't create backup for " + file.getName(), -1);
//      }

            // .. and now !finally! move from temp to result
            if (!temp.renameTo(file)) {
                throw new GedcomIOException("Couldn't move temporary " + temp.getName() + " to " + file.getName(), -1);
            }

        } catch (GedcomIOException gioex) {
            DialogHelper.openDialog(gedcom.getName(), DialogHelper.ERROR_MESSAGE, RES.getString("cc.save.write_error", "" + gioex.getLine()) + ":\n" + gioex.getMessage(), Action2.okOnly(), null);
            return false;
        }

//  // close progress
//  WindowManager.close(progress);

        // .. done
        return true;
    }

    /**
     * closes gedcom file
     */
    public boolean closeGedcom(Context context) {
        return closeGedcom(context, true);
    }

    public boolean closeGedcom(Context context, boolean unregister) {

        // noop?
        if (context.getGedcom() == null) {
            return true;
        }

        // commit changes
        fireCommit(context);

        // changes?
        if (context.getGedcom().hasChanged()) {

            // close file officially
            int rc = DialogHelper.openDialog(null, DialogHelper.WARNING_MESSAGE, RES.getString("cc.savechanges?", context.getGedcom().getName()), Action2.yesNoCancel(), null);
            // cancel - we're done
            if (rc == 2) {
                return false;
            }
            // yes - close'n save it
            if (rc == 0) {
                if (!saveGedcom(context)) {
                    return false;
                }
            }

        }

        // tell
        gedcomClosed(context.getGedcom());

        // remember context
        context.getGedcom().getRegistry().put("context", context.toString());

        // FIXME: place in a gedcomlistener
//        ActionSaveLayout.saveLayout(context.getGedcom());

        // closes all views
        for (AncestrisViewInterface gjvTc : AncestrisPlugin.lookupAll(AncestrisViewInterface.class)) {
            if (context.getGedcom().equals(gjvTc.getGedcom())) {
                if (!gjvTc.close()) {
                    return false;
                }
            }
        }

        if (unregister) {
            GedcomDirectory.getInstance().unregisterGedcom(context);
        }
        // done
        return true;
    }

    /**
     * Restores last loaded gedcom file
     */
    @SuppressWarnings("deprecation")
    public void restoreGedcom() {

        String restore = REGISTRY.get("restore.url", (String) null);
        try {
            // no known key means load default
            if (restore == null) // we're intentionally not going through toURI.toURL here since
            // that would add space-to-%20 conversion which kills our relative
            // file check operations down the line
            {
                restore = new File("gedcom/example.ged").toURL().toString();
            }
            // known key needs value
            if (restore.length() > 0) {
                openGedcom(new URL(restore));
            }
        } catch (Throwable t) {
            LOG.log(Level.WARNING, "unexpected error", t);
        }
    }

    public void fireCommit(Context context) {
        commitRequested(context);
    }

    public void fireSelection(MySelectionListener from, Context context, boolean isActionPerformed) {
//TODO: mieux controler. Devra atre refait lors du basculement total dans l'environnement NB
//    // appropriate?
//    if (context.getGedcom()!= this.context.getGedcom()) {
//      LOG.log(Level.FINER, "context selection on unknown gedcom", new Throwable());
//      return;
//    }
        // following a link?
        if (isActionPerformed && context.getProperties().size() == 1) {
            Property p = context.getProperty();
            if (p instanceof PropertyXRef) {
                context = new Context(((PropertyXRef) p).getTarget());
            }
        }
//
//    // already known?
//    if (!isActionPerformed && this.context.equals(context))
//      return;
//
//    LOG.fine("fireSelection("+context+","+isActionPerformed+")");
//
//    // remember
//    this.context = context;
//
//    if (context.getGedcom()!=null)
//      REGISTRY.put(context.getGedcom().getName()+".context", context.toString());
//
        // notify
        selectionChanged(from, context, isActionPerformed);
    }

    public void addWorkbenchListener(WorkbenchListener listener) {
        AncestrisPlugin.register(listener);
    }

    public void removeWorkbenchListener(WorkbenchListener listener) {
        AncestrisPlugin.unregister(listener);
    }

    /**
     * (re)open a view
     */
    public View openView(Class<? extends ViewFactory> factory) {
        return openView(factory, getContext());
    }

    /**
     * (re)open a view
     */
    public View openView(Class<? extends ViewFactory> factory, Context context) {
        try {
            return openViewImpl(factory.newInstance(), context);
        } catch (InstantiationException ex) {
            Logger.getLogger(Workbench.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Workbench.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    // Workbench helper
    public static void openDefaultViews(Context context) {

        AncestrisPreferences prefs = Registry.get(AncestrisViewInterface.class);
        List<Class> openedViews = new ArrayList<Class>();

        // Always open explorer (if not opened)
        // FIXME: GedcomTC is not known from this module
        // XXX: Must be donne with lookups will be done later
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

    public void setDefault(Gedcom gedcom) {
        try {
            // note: dans ce cas pas besoin de memoriser dans le undo history mais cela
            // permet de positionner le gedcom dans l'etat change
            gedcom.doUnitOfWork(new UnitOfWork() {

                public void perform(Gedcom gedcom) throws GedcomException {

                    AncestrisPreferences submPref = Registry.get(genj.gedcom.Options.class);

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
                    gedcom.setPlaceFormat(genj.gedcom.Options.getInstance().getPlaceFormat());
                    gedcom.setShowJuridictions(genj.gedcom.Options.getInstance().getShowJuridictions());
                    gedcom.setPlaceSortOrder(genj.gedcom.Options.getInstance().getPlaceSortOrder());
                    gedcom.setPlaceDisplayFormat(genj.gedcom.Options.getInstance().getPlaceDisplayFormat());
                }
            });
        } catch (GedcomException e) {
            Exceptions.printStackTrace(e);
        }
    }

    /** getDefaultFile() **/
    private String getDefaultFile(boolean dirOnly) {
        String defaultFile = ancestris.core.Options.getDefaultGedcom();
        if (defaultFile.isEmpty()) {
            return "";
        }
        File local = new File(defaultFile);
        if (dirOnly) {
            return local.getParent();
        }
        if (!local.exists()) {
            return "";
        }
        try {
            local.toURI().toURL().toString();
        } catch (MalformedURLException ex) {
            LOG.info(ex.toString());
//fixme:dans nb      Exceptions.printStackTrace(ex);
        }
        return "";
    }

    /**
     * Let the user choose a file
     */
    public File chooseFile(String title, String action, JComponent accessory) {
        FileChooser chooser = new FileChooser(
                null, title, action, "ged",
                EnvironmentChecker.getProperty(new String[]{"ancestris.gedcom.dir", "user.home"}, ".", "choose gedcom file"));

        String gedcomDir = getDefaultFile(true);
        if (gedcomDir == null || gedcomDir.trim().isEmpty()) {
            gedcomDir = "user.home";
        }
        chooser.setCurrentDirectory(new File(Registry.get(Workbench.class).get("last.dir", gedcomDir)));
        if (accessory != null) {
            chooser.setAccessory(accessory);
        }
        if (JFileChooser.APPROVE_OPTION != chooser.showDialog()) {
            return null;
        }
        // check the selection
        File file = chooser.getSelectedFile();
        if (file == null) {
            return null;
        }
        // remember last directory
        Registry.get(Workbench.class).put("last.dir", file.getParentFile().getAbsolutePath());
        // done
        return file;
    }
    // IWorkbenchHelper Implementation

    public void selectionChanged(MySelectionListener from, Context context, boolean isActionPerformed) {
        for (WorkbenchListener listener : AncestrisPlugin.lookupAll(WorkbenchListener.class)) {
            listener.selectionChanged(context, isActionPerformed);
        }
        for (SelectionListener listener : AncestrisPlugin.lookupAll(SelectionListener.class)) {
            if (!listener.equals(from)) {
                listener.setContext(context, isActionPerformed);
            }
        }
        if (from != null) {
            from.setMyContext(context, isActionPerformed);
        }
    }

    public void selectionChanged(Context context, boolean isActionPerformed) {
        selectionChanged(null, context, isActionPerformed);
    }

    public void processStarted(Trackable process) {
        for (WorkbenchListener listener : AncestrisPlugin.lookupAll(WorkbenchListener.class)) {
            listener.processStarted(process);
        }
    }

    public void processStopped(Trackable process) {
        for (WorkbenchListener listener : AncestrisPlugin.lookupAll(WorkbenchListener.class)) {
            listener.processStopped(process);
        }
    }

    public void commitRequested(Context context) {
        for (WorkbenchListener listener : AncestrisPlugin.lookupAll(WorkbenchListener.class)) {
            listener.commitRequested(context);
        }
    }

    public void workbenchClosing() {
        for (WorkbenchListener listener : AncestrisPlugin.lookupAll(WorkbenchListener.class)) {
            listener.workbenchClosing();
        }
    }

    public void gedcomClosed(Gedcom gedcom) {
        for (WorkbenchListener listener : AncestrisPlugin.lookupAll(WorkbenchListener.class)) {
            listener.gedcomClosed(gedcom);
        }
        gedcom.removeGedcomListener(this);
    }

    public void gedcomOpened(Gedcom gedcom) {
        for (WorkbenchListener listener : AncestrisPlugin.lookupAll(WorkbenchListener.class)) {
            listener.gedcomOpened(gedcom);
        }
        gedcom.addGedcomListener(this);
    }

    public void viewOpened(View view) {
        for (WorkbenchListener listener : AncestrisPlugin.lookupAll(WorkbenchListener.class)) {
            listener.viewOpened(view);
        }
    }

    public void viewClosed(View view) {
        for (WorkbenchListener listener : AncestrisPlugin.lookupAll(WorkbenchListener.class)) {
            listener.viewClosed(view);
        }
    }

    // Gedcom Listener
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
        for (GedcomListener listener : AncestrisPlugin.lookupAll(GedcomListener.class)) {
            listener.gedcomEntityAdded(gedcom, entity);
        }
    }

    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        for (GedcomListener listener : AncestrisPlugin.lookupAll(GedcomListener.class)) {
            listener.gedcomEntityDeleted(gedcom, entity);
        }
    }

    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        for (GedcomListener listener : AncestrisPlugin.lookupAll(GedcomListener.class)) {
            listener.gedcomPropertyChanged(gedcom, property);
        }
    }

    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        for (GedcomListener listener : AncestrisPlugin.lookupAll(GedcomListener.class)) {
            listener.gedcomPropertyAdded(gedcom, property, pos, added);
        }
    }

    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
        for (GedcomListener listener : AncestrisPlugin.lookupAll(GedcomListener.class)) {
            listener.gedcomPropertyDeleted(gedcom, property, pos, deleted);
        }
    }

    public void gedcomHeaderChanged(Gedcom gedcom) {
        for (GedcomMetaListener listener : AncestrisPlugin.lookupAll(GedcomMetaListener.class)) {
            listener.gedcomHeaderChanged(gedcom);
        }
    }

    public void gedcomWriteLockAcquired(Gedcom gedcom) {
        for (GedcomMetaListener listener : AncestrisPlugin.lookupAll(GedcomMetaListener.class)) {
            listener.gedcomWriteLockAcquired(gedcom);
        }
    }

    public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
        for (GedcomMetaListener listener : AncestrisPlugin.lookupAll(GedcomMetaListener.class)) {
            listener.gedcomBeforeUnitOfWork(gedcom);
        }
    }

    public void gedcomAfterUnitOfWork(Gedcom gedcom) {
        for (GedcomMetaListener listener : AncestrisPlugin.lookupAll(GedcomMetaListener.class)) {
            listener.gedcomAfterUnitOfWork(gedcom);
        }
    }

    public void gedcomWriteLockReleased(Gedcom gedcom) {
        for (GedcomMetaListener listener : AncestrisPlugin.lookupAll(GedcomMetaListener.class)) {
            listener.gedcomWriteLockReleased(gedcom);
        }
    }

    public View openViewImpl(ViewFactory factory, Context context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private Context getContext() {
        return null;
//        return App.center.getSelectedContext(true);
    }
} // WorkBench

