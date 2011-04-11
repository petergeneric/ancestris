/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2009 Nils Meier <nils@meiers.net>
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

import ancestris.util.AncestrisPreferences;
import ancestris.util.ProgressBar;
import genj.gedcom.Entity;
import genj.io.Filter;
import genj.util.Trackable;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Submitter;
import genj.gedcom.UnitOfWork;
import genj.util.EnvironmentChecker;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.FileChooser;
import genj.view.MySelectionListener;
import genj.view.SelectionListener;
import genj.view.SelectionSink;
import genj.view.View;
import genj.view.ViewFactory;
import genjfr.app.ActionSaveLayout;
import genjfr.app.App;
import genjfr.app.GenjViewInterface;
import genjfr.app.GenjViewTopComponent;
import genjfr.app.pluginservice.GenjFrPlugin;
import genjfr.app.pluginservice.PluginInterface;
import genjfr.explorer.GedcomExplorerTopComponent;
import genjfr.util.GedcomDirectory;
import java.awt.Component;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * The central component of the GenJ application
 */
public class WorkbenchHelper /*extends JPanel*/ implements SelectionSink, IWorkbenchHelper, GedcomMetaListener {

    private Workbench workbench;
    private final static Logger LOG = Logger.getLogger("genj.app");
    private final static Resources RES = Resources.get(Workbench.class);
    private int isLoaded = 1;
    final private Object loadLock = new Object();

    private ProgressBar progress;

    private static WorkbenchHelper instance = null;

    private WorkbenchHelper() {
        this.workbench = Workbench.getInstance(this);
        progress = new ProgressBar();
        GenjFrPlugin.register(progress);
    }

    public static WorkbenchHelper getinstance(){
        if (instance == null)
            instance = new WorkbenchHelper();
        return instance;
    }

    public Workbench getWorkbench() {
        return workbench;
    }

    public void fireCommit(Context context) {
        commitRequested(workbench, context);
    }

    public boolean saveGedcom(Context context) {
        if (context != null && context.getGedcom().getOrigin() == null)
            return saveAsGedcom(context);
        return workbench.saveGedcom(context);
    }

    public boolean saveAsGedcom(Context context) {
        if (workbench.saveAsGedcom(context)) {
            if (context != null) {
                GedcomDirectory.getInstance().unregisterGedcom(context);
                GedcomDirectory.getInstance().registerGedcom(context);
            }
            return true;
        }
        return false;
    }

    public Context openGedcom() {
        return workbench.openGedcom();
    }

    public Context openGedcom(URL url) {
        Context context = workbench.openGedcom(url);
        if (context != null) {
            GedcomDirectory.getInstance().registerGedcom(context);
            openDefaultViews(context);
            //FIXME: etait true. Cela faisait changer le root dans l'arbre
            // bizarre car cela ne devrait pas etre le cas meme avec true...
            // Voir si avec false il n'y a pas d'effet de bord et si cela corrige le pb de prise en compte du root
            SelectionSink.Dispatcher.fireSelection((Component) null, context, false);
        }
        return context;
    }

    public static void openDefaultViews(Context context) {

        AncestrisPreferences prefs = AncestrisPreferences.get(GenjViewTopComponent.class);
        List<Class> openedViews = new ArrayList<Class>();

        // Always open explorer (if not opened)
        GedcomExplorerTopComponent.getDefault().open();

        // try gedcom properties
        Registry gedcomSettings = context.getGedcom().getRegistry();

        // FIXME: a reecrire plus proprement
        String ovs[] = gedcomSettings.get("openViews",(String[])null);
        openedViews.addAll(GenjFrPlugin.lookupForName(GenjViewInterface.class,ovs));

        if (openedViews.isEmpty()) {
            openedViews.addAll(GenjFrPlugin.lookupForName(
                        GenjViewInterface.class,
                        prefs.get("openViews",(String[])null)));
        }
        if (openedViews.isEmpty()) {
            // Open default views
            for (PluginInterface sInterface : Lookup.getDefault().lookupAll(PluginInterface.class)) {
                openedViews.addAll(sInterface.getDefaultOpenedViews());
            }
        }

        TopComponent tc = null;
        Map<String,TopComponent> name2tc = new HashMap<String, TopComponent>();
        for (Class clazz : openedViews) {
            try {
                tc = (TopComponent) clazz.newInstance();
                if (tc instanceof GenjViewInterface) {
                    ((GenjViewInterface) tc).init(context);
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
        for (String name:gedcomSettings.get("focusViews",new String[]{})){
            TopComponent tcToFocus = name2tc.get(name);
            if (tcToFocus != null){
                tcToFocus.requestActive();
            }
        }
    }

    public void setDefault(Gedcom gedcom){
        try{
            // note: dans ce cas pas besoin de memoriser dans le undo history mais cela
            // permet de positionner le gedcom dans l'etat change
            gedcom.doUnitOfWork(new UnitOfWork() {

                public void perform(Gedcom gedcom) throws GedcomException {

                    AncestrisPreferences submPref = AncestrisPreferences.get(App.class);

                    // Create submitter
                    Submitter submitter = (Submitter) gedcom.createEntity(Gedcom.SUBM);
                    submitter.setName(submPref.get("submName", ""));
                    submitter.setCity(submPref.get("submCity", ""));
                    submitter.setPhone(submPref.get("submPhone", ""));
                    submitter.setEmail(submPref.get("submEmail", ""));
                    submitter.setCountry(submPref.get("submCountry", ""));
                    submitter.setWeb(submPref.get("submWeb", ""));

                    gedcom.createEntity("HEAD","");

                    // Create place format
                    gedcom.setPlaceFormat(genj.gedcom.Options.getInstance().getPlaceFormat());
                    gedcom.setShowJuridictions(genj.gedcom.Options.getInstance().getShowJuridictions());
                    gedcom.setPlaceSortOrder(genj.gedcom.Options.getInstance().getPlaceSortOrder());
                    gedcom.setPlaceDisplayFormat(genj.gedcom.Options.getInstance().getPlaceDisplayFormat());
                }
            });
        } catch (GedcomException e){
            Exceptions.printStackTrace(e);
        }
    }

    public Context newGedcom() {
        //FIXME: changer le nouveau gedcom cree par defaut!
        Context context = workbench.newGedcom();
        if (context == null)
            return null;
        Gedcom gedcom = context.getGedcom();
        try {
            setDefault(gedcom);
            // remember
            GedcomDirectory.getInstance().registerGedcom(context);
            openDefaultViews(context);
            SelectionSink.Dispatcher.fireSelection((Component) null, new Context(context.getGedcom().getFirstEntity(Gedcom.INDI)), true);
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return context;
    }

    public boolean closeGedcom(Context context) {
        return closeGedcom(context, true);
    }

    public boolean closeGedcom(Context context, boolean unregister) {
        if (workbench.closeGedcom(context)) {
            ActionSaveLayout.saveLayout(context.getGedcom());

            // closes all views
            for (GenjViewInterface gjvTc : GenjFrPlugin.lookupAll(GenjViewInterface.class)) {
                if (context.getGedcom().equals(gjvTc.getGedcom())) {
                    if (!gjvTc.close()) {
                        return false;
                    }
                }
            }
            if (unregister) {
                GedcomDirectory.getInstance().unregisterGedcom(context);
            }

            return true;
        } else {
            return false;
        }
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
        selectionChanged(workbench, from, context, isActionPerformed);
    }

    public boolean isReady(int i) {
        if (isLoaded == 0) {
            return true;
        }
        synchronized (loadLock) {
            if (i != 0) {
                isLoaded += i;
                loadLock.notifyAll();
                return true;
            } else {
                try {
                    while (isLoaded != 0) {
                        loadLock.wait();
                    }
                    return true;
                } catch (InterruptedException ex) {
                    LOG.info(ex.toString());
//fixme:dans nb                    Exceptions.printStackTrace(ex);
                    return false;
                }
            }
        }
//        return false;
    }

    /** getDefaultFile() **/
    private String getDefaultFile(boolean dirOnly) {
        String defaultFile = genjfr.app.Options.getDefaultGedcom();
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
                EnvironmentChecker.getProperty(new String[]{"genj.gedcom.dir", "user.home"}, ".", "choose gedcom file"));

        String gedcomDir = getDefaultFile(true);
        if (gedcomDir == null || gedcomDir.trim().isEmpty()) {
            gedcomDir = "user.home";
        }
        chooser.setCurrentDirectory(new File(AncestrisPreferences.get(Workbench.class).get("last.dir", gedcomDir)));
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
        AncestrisPreferences.get(Workbench.class).put("last.dir", file.getParentFile().getAbsolutePath());
        // done
        return file;
    }

    public void saveGedcomImpl(Gedcom gedcom, Collection<Filter> filter) {
        workbench.saveGedcomImpl(gedcom,filter);
    }

    // IWorkbenchHelper Implementation
    public void selectionChanged(Workbench workbench,MySelectionListener from, Context context, boolean isActionPerformed) {
            for (WorkbenchListener listener : GenjFrPlugin.lookupAll(WorkbenchListener.class)) {
                listener.selectionChanged(workbench, context, isActionPerformed);
            }
            for (SelectionListener listener : GenjFrPlugin.lookupAll(SelectionListener.class)) {
                if (!listener.equals(from))
                    listener.setContext(context, isActionPerformed);
            }
            if (from != null){
                from.setMyContext(context, isActionPerformed);
        }
    }
    public void selectionChanged(Workbench workbench,Context context, boolean isActionPerformed) {
        selectionChanged(workbench, null, context, isActionPerformed);
    }

    public void processStarted(Workbench workbench, Trackable process) {
        for (WorkbenchListener listener : GenjFrPlugin.lookupAll(WorkbenchListener.class)) {
            listener.processStarted(workbench, process);
        }
    }

    public void processStopped(Workbench workbench, Trackable process) {
        for (WorkbenchListener listener : GenjFrPlugin.lookupAll(WorkbenchListener.class)) {
            listener.processStopped(workbench, process);
        }
    }

    public void commitRequested(Workbench workbench, Context context) {
        for (WorkbenchListener listener : GenjFrPlugin.lookupAll(WorkbenchListener.class)) {
            listener.commitRequested(workbench, context);
        }
    }

    public void workbenchClosing(Workbench workbench) {
        for (WorkbenchListener listener : GenjFrPlugin.lookupAll(WorkbenchListener.class)) {
            listener.workbenchClosing(workbench);
        }
    }

    public void gedcomClosed(Workbench workbench, Gedcom gedcom) {
        for (WorkbenchListener listener : GenjFrPlugin.lookupAll(WorkbenchListener.class)) {
            listener.gedcomClosed(workbench, gedcom);
        }
        gedcom.removeGedcomListener(this);
    }

    public void gedcomOpened(Workbench workbench, Gedcom gedcom) {
        for (WorkbenchListener listener : GenjFrPlugin.lookupAll(WorkbenchListener.class)) {
            listener.gedcomOpened(workbench, gedcom);
        }
        gedcom.addGedcomListener(this);
    }

    public void viewOpened(Workbench workbench, View view) {
        for (WorkbenchListener listener : GenjFrPlugin.lookupAll(WorkbenchListener.class)) {
            listener.viewOpened(workbench, view);
        }
    }

    public void viewClosed(Workbench workbench, View view) {
        for (WorkbenchListener listener : GenjFrPlugin.lookupAll(WorkbenchListener.class)) {
            listener.viewClosed(workbench, view);
        }
    }

    // Gedcom Listener
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
        for (GedcomListener listener : GenjFrPlugin.lookupAll(GedcomListener.class)) {
            listener.gedcomEntityAdded(gedcom, entity);
        }
    }

    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        for (GedcomListener listener : GenjFrPlugin.lookupAll(GedcomListener.class)) {
            listener.gedcomEntityDeleted(gedcom, entity);
        }
    }

    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        for (GedcomListener listener : GenjFrPlugin.lookupAll(GedcomListener.class)) {
            listener.gedcomPropertyChanged(gedcom, property);
        }
    }

    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        for (GedcomListener listener : GenjFrPlugin.lookupAll(GedcomListener.class)) {
            listener.gedcomPropertyAdded(gedcom, property, pos, added);
        }
    }

    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
        for (GedcomListener listener : GenjFrPlugin.lookupAll(GedcomListener.class)) {
            listener.gedcomPropertyDeleted(gedcom, property, pos, deleted);
        }
    }

    public void gedcomHeaderChanged(Gedcom gedcom) {
        for (GedcomMetaListener listener : GenjFrPlugin.lookupAll(GedcomMetaListener.class)) {
            listener.gedcomHeaderChanged(gedcom);
        }
    }

    public void gedcomWriteLockAcquired(Gedcom gedcom) {
        for (GedcomMetaListener listener : GenjFrPlugin.lookupAll(GedcomMetaListener.class)) {
            listener.gedcomWriteLockAcquired(gedcom);
        }
    }

    public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
        for (GedcomMetaListener listener : GenjFrPlugin.lookupAll(GedcomMetaListener.class)) {
            listener.gedcomBeforeUnitOfWork(gedcom);
        }
    }

    public void gedcomAfterUnitOfWork(Gedcom gedcom) {
        for (GedcomMetaListener listener : GenjFrPlugin.lookupAll(GedcomMetaListener.class)) {
            listener.gedcomAfterUnitOfWork(gedcom);
        }
    }

    public void gedcomWriteLockReleased(Gedcom gedcom) {
        for (GedcomMetaListener listener : GenjFrPlugin.lookupAll(GedcomMetaListener.class)) {
            listener.gedcomWriteLockReleased(gedcom);
        }
    }

    public void register(Object o) {
        GenjFrPlugin.register(o);
    }

    public void unregister(Object o) {
        GenjFrPlugin.unregister(o);
    }

    public View openViewImpl(ViewFactory factory, Context context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Context getContext() {
        return App.center.getSelectedContext(true);
    }
}
