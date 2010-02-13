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
 */
package genjfr.app;

import genj.app.GedcomTableWidget;
import genj.app.Images;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Property;
import genj.gedcom.GedcomDirectory;
import genj.util.DirectAccessTokenizer;
import genj.util.EnvironmentChecker;
import genj.util.MnemonicAndText;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.WordBuffer;
import genj.util.swing.Action2;
import genj.util.swing.FileChooser;
import genj.util.swing.HeapStatusWidget;
import genj.util.swing.NestedBlockLayout;
import genj.view.CommitRequestedEvent;
import genj.view.ViewContext;
import genj.view.ViewFactory;
import genj.view.ViewHandle;
import genj.view.ViewManager;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 * The central component of the GenJ application
 */
public class ControlCenter extends JPanel {

    private final static String ACC_SAVE = "ctrl S",
            ACC_EXIT = "ctrl X",
            ACC_NEW = "ctrl N",
            ACC_OPEN = "ctrl O";
    /** members */
    private JMenuBar menuBar;
    private GedcomTableWidget tGedcoms;
    protected Registry registry;
    private Resources resources = Resources.get(genj.app.ControlCenter.class);
    private WindowManager windowManager;
    private ViewManager viewManager;
    private List gedcomActions = new ArrayList();
    private List toolbarActions = new ArrayList();
    protected Stats stats = new Stats();
    private ActionExit exit = new ActionExit();
    private Runnable runOnExit;
    private Runnable loading;
    private int isLoaded = 1;
    final private Object loadLock = new Object();

    /**
     * Constructor
     */
    public ControlCenter(Registry setRegistry, WindowManager winManager, Runnable onExit) {

        // Initialize data
        registry = new Registry(setRegistry, "cc");
        windowManager = winManager;
        viewManager = new ViewManager(windowManager);
        runOnExit = onExit;

        // Table of Gedcoms
        tGedcoms = new GedcomTableWidget(viewManager, registry) {

            @Override
            public ViewContext getContext() {
                ViewContext result = super.getContext();
                if (result != null) {
                    result.addAction(new ActionSave(false, true));
                    result.addAction(new ActionClose(true));
                }
                return result;
            }

            ;
        };

        // ... Listening
        tGedcoms.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                for (int i = 0; i < gedcomActions.size(); i++) {
                    ((Action2) gedcomActions.get(i)).setEnabled(tGedcoms.getSelectedGedcom() != null);
                }
            }
        });

        // Layout
        setLayout(new BorderLayout());
//    add(createToolBar(), BorderLayout.NORTH);
        add(new JScrollPane(tGedcoms), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        // Init menu bar at this point (so it's ready when the first file is loaded)
//    menuBar = createMenuBar();

        // Done
    }

    /**
     * loads gedcom files
     */
    public void load(String[] files) {
        // Load known gedcoms
//      Runnable r = new ActionAutoOpen(files);
//    SwingUtilities.invokeLater(r);
        loading = new ActionAutoOpen(files);
        try {
            SwingUtilities.invokeAndWait(loading);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

//  public void waitLoad() {
//      loading.
//  }
    /**
     * loads gedcom files
     */
    public void load(Collection files) {
        // Load known gedcoms
        Runnable r = new ActionAutoOpen(files);
        SwingUtilities.invokeLater(r);
    }

    /**
     * Exit action
     */
//  /*package*/ Action2 getExitAction() {
//    return exit;
//  }
    /**
     * Returns a menu for frame showing this controlcenter
     */
//  /*package*/ JMenuBar getMenuBar() {
//    return menuBar;
//  }
    /**
     * Returns a status bar for the bottom
     */
    private JPanel createStatusBar() {

        HeapStatusWidget mem = new HeapStatusWidget();
        mem.setToolTipText(resources.getString("cc.heap"));

        JPanel result = new JPanel(new NestedBlockLayout("<row><info wx=\"1\" gx=\"1\"/><mem/></row>"));
        result.add(stats);
        result.add(mem);

        return result;
    }

    /**
     * Let the user choose a file
     */
    protected File chooseFile(String title, String action, JComponent accessory) {
        FileChooser chooser = new FileChooser(
                ControlCenter.this, title, action, "ged",
                EnvironmentChecker.getProperty(ControlCenter.this, new String[]{"genj.gedcom.dir", "user.home"}, ".", "choose gedcom file"));

        String gedcomDir = getDefaultFile(true);
        if (gedcomDir.trim().isEmpty()) {
            gedcomDir = "user.home";
        }
        chooser.setCurrentDirectory(new File(registry.get("last.dir", gedcomDir)));
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
        registry.put("last.dir", file.getParentFile().getAbsolutePath());
        // done
        return file;
    }

    boolean isReady(int i) {
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
                    Exceptions.printStackTrace(ex);
                    return false;
                }
            }
        }
//        return false;
    }

    /**
     * Action - exit
     */
    private class ActionExit extends Action2 {

        /** constructor */
        protected ActionExit() {
            setAccelerator(ACC_EXIT);
            setText(resources, "cc.menu.exit");
            setImage(Images.imgExit);
            setTarget(ControlCenter.this);
        }

        /** run */
        @Override
        protected void execute() {
            // force a commit
            for (Gedcom gedcom : GedcomDirectory.getInstance().getGedcoms()) {
                WindowManager.broadcast(new CommitRequestedEvent(gedcom, ControlCenter.this));
            }
            // Remember open gedcoms
            Collection save = new ArrayList();
            for (Iterator gedcoms = GedcomDirectory.getInstance().getGedcoms().iterator(); gedcoms.hasNext();) {
                // next gedcom
                Gedcom gedcom = (Gedcom) gedcoms.next();
                // changes need saving?
                if (gedcom.hasChanged()) {
                    // close file officially
                    int rc = windowManager.openDialog(
                            "confirm-exit", null, WindowManager.WARNING_MESSAGE,
                            resources.getString("cc.savechanges?", gedcom.getName()),
                            Action2.yesNoCancel(), ControlCenter.this);
                    // cancel - we're done
                    if (rc == 2) {
                        return;
                    }
                    // yes - close'n save it
                    if (rc == 0) {
                        // block exit
                        ActionExit.this.setEnabled(false);
                        // run save
                        new ActionSave(gedcom) {
                            // apres save

                            protected void postExecute(boolean preExecuteResult) {
                                try {
                                    // super first
                                    super.postExecute(preExecuteResult);
                                    // stop still unsaved changes that didn't make it through saving
                                    if (gedcomBeingSaved.hasChanged()) {
                                        return;
                                    }
                                } finally {
                                    // unblock exit
                                    ActionExit.this.setEnabled(true);
                                }
                                // continue with exit
                                ActionExit.this.trigger();
                            }
                        }.trigger();
                        return;
                    }
                    // no - skip it
                }
                // remember as being open, password and open views
                File file = gedcom.getOrigin().getFile();
                if (file == null || file.exists()) {
                    StringBuffer restore = new StringBuffer();
                    restore.append(gedcom.getOrigin());
                    restore.append(",");
                    if (gedcom.hasPassword()) {
                        restore.append(gedcom.getPassword());
                    }
                    restore.append(",");
                    ViewHandle[] views = viewManager.getViews(gedcom);
                    for (int i = 0, j = 0; i < views.length; i++) {
                        if (j++ > 0) {
                            restore.append(",");
                        }
                        restore.append(views[i].persist());
                    }
                    save.add(restore);
                }
                // next gedcom
            }
            registry.put("open", save);

            // Close all Windows
            windowManager.closeAll();

            // Shutdown
            runOnExit.run();

            // Done
        }
    } //ActionExit

    public boolean nbDoExit() {
        // force a commit
        for (Gedcom gedcom : GedcomDirectory.getInstance().getGedcoms()) {
            WindowManager.broadcast(new CommitRequestedEvent(gedcom, ControlCenter.this));
        }
        for (Iterator gedcoms = GedcomDirectory.getInstance().getGedcoms().iterator(); gedcoms.hasNext();) {
            // next gedcom
            Gedcom gedcom = (Gedcom) gedcoms.next();
            // changes need saving?
            if (gedcom.hasChanged()) {
                // close file officially
                int rc = windowManager.openDialog(
                        "confirm-exit", null, WindowManager.WARNING_MESSAGE,
                        resources.getString("cc.savechanges?", gedcom.getName()),
                        Action2.yesNoCancel(), ControlCenter.this);
                // cancel - we're done
                if (rc == 2) {
                    return false;
                }
                // yes - close'n save it
                if (rc == 0) {
                    // block exit
                    // run save
                    new ActionSave(gedcom, false) {
                        // apres save

                        @Override
                        protected void postExecute(boolean preExecuteResult) {
                            try {
                                // super first
                                super.postExecute(preExecuteResult);
                                // stop still unsaved changes that didn't make it through saving
                                if (gedcomBeingSaved.hasChanged()) {
                                    return;
                                }
                            } finally {
                                // unblock exit
                            }
                            // continue with exit
                        }
                    }.trigger();
//            return;
                }
                // no - skip it
            }
            // next gedcom
        }
        // Done
        return true;
    }

    public Collection getOpenedGedcoms() {
        // Remember open gedcoms
        Collection save = new ArrayList();
        for (Iterator gedcoms = GedcomDirectory.getInstance().getGedcoms().iterator(); gedcoms.hasNext();) {
            // next gedcom
            Gedcom gedcom = (Gedcom) gedcoms.next();
            // remember as being open, password and open views
            File file = gedcom.getOrigin().getFile();
            if (file == null || file.exists()) {
                StringBuffer restore = new StringBuffer();
                restore.append(gedcom.getOrigin());
                restore.append(",");
                if (gedcom.hasPassword()) {
                    restore.append(gedcom.getPassword());
                }
                restore.append(",");
                ViewHandle[] views = viewManager.getViews(gedcom);
                for (int i = 0, j = 0; i < views.length; i++) {
                    if (j++ > 0) {
                        restore.append(",");
                    }
                    restore.append(views[i].persist());
                }
                save.add(restore);
            }
            // next gedcom
        }
        // Done
        return save;
    }

    public Gedcom getOpenedGedcom(String gedName) {
        if (gedName == null) {
            return null;
        }
        // grab "file[, password][, view#x]"
        DirectAccessTokenizer tokens = new DirectAccessTokenizer(gedName, ",", false);
        String url = tokens.get(0);
        if (url == null) {
            return null;
        }
        for (Iterator gedcoms = GedcomDirectory.getInstance().getGedcoms().iterator(); gedcoms.hasNext();) {
            // next gedcom
            Gedcom gedcom = (Gedcom) gedcoms.next();
            if (url.equals(gedcom.getOrigin().toString())) {
                return gedcom;
            }
        }
        return null;
    }

    /**
     * Action - LoadLastOpen
     */
    private class ActionAutoOpen extends Action2 {

        /** files to load */
        private Collection files;

        /** constructor */
        private ActionAutoOpen(String[] args) {
            // if we got files then we don't open old ones
            if (args.length > 0) {
                files = Arrays.asList(args);
                return;
            }

            // by default we offer the user to load default gedcom file
            HashSet deflt = new HashSet();
            if (args.length == 0) {
                try {
                    deflt.add(getDefaultFile(false));
                } catch (Throwable t) {
                    // ignored
                }
            }

            // check registry for the previously opened now
            files = (Set) registry.get("open", deflt);

        }

        private ActionAutoOpen(Collection theFiles) {
            files = theFiles;
            if (files == null || files.isEmpty()) {
                List list = new ArrayList();
                String defaultFile = getDefaultFile(false);
                if (defaultFile != null) {
                    list.add(defaultFile);
                    files = list;
                } else {
                    files = null;
                }
            }
        }

        /** run */
        @Override
        public void execute() {

            // Loop over files to open
            if (files != null && !files.isEmpty()) {
                for (Iterator it = files.iterator(); it.hasNext();) {
                    String uriStr = it.next().toString();
                    if (getOpenedGedcom(uriStr) != null) {
                        break;
                    }
                    try {
                        DirectAccessTokenizer tokens = new DirectAccessTokenizer(uriStr, ",", false);
                        String restore = tokens.get(0);

                        // check if it's a local file
                        File local = new File(new URI(restore));
                        if (!local.exists()) {
                            continue;
                        }

                        ActionOpen open = new ActionOpen(restore) {

                            @Override
                            protected void postExecute(boolean b) {
                                super.postExecute(b);
                                App.center.isReady(-1);
                            }
                        };
                        App.center.isReady(1);
                        open.trigger();
                    } catch (Throwable t) {
                        App.LOG.log(Level.WARNING, "cannot restore " + uriStr, t);
                    }

                    // next
                }
            }

            // done
            App.center.isReady(-1);
        }
    } //LastOpenLoader

    /**
     * Action - View
     */
    private class ActionView extends Action2 {

        /** which ViewFactory */
        private ViewFactory factory;

        /** constructor */
        protected ActionView(int i, ViewFactory vw) {
            factory = vw;
            if (i > 0) {
                setText(Integer.toString(i) + " " + new MnemonicAndText(factory.getTitle(false)).getText());
            } else {
                setText(factory.getTitle(true));
            }
            setTip(resources.getString("cc.tip.open_view", factory.getTitle(false)));
            setImage(factory.getImage());
            setEnabled(false);
        }

        /** run */
        protected void execute() {
            // grab current Gedcom
            final Gedcom gedcom = tGedcoms.getSelectedGedcom();
            if (gedcom == null) {
                return;
            }
            // create new View
            ViewHandle handle = viewManager.openView(gedcom, factory);
            // install some accelerators
            new ActionSave(gedcom).setTarget(handle.getView()).install(handle.getView(), JComponent.WHEN_IN_FOCUSED_WINDOW);
        }
    } //ActionView

    public Gedcom getSelectedGedcom() {
        return tGedcoms.getSelectedGedcom();
    }

    public ViewManager getViewManager() {
        return viewManager;
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public Stats getStats() {
        return stats;
    }

    /**
     * a little status tracker
     */
    protected class Stats extends JLabel implements GedcomMetaListener, GedcomDirectory.Listener {

        private int commits;
        private int read, written;

        private Stats() {
            setHorizontalAlignment(SwingConstants.LEFT);
            GedcomDirectory.getInstance().addListener(this);
        }

        public void gedcomWriteLockReleased(Gedcom gedcom) {
            commits++;
            update();
        }

        public synchronized void handleRead(int lines) {
            read += lines;
            update();
        }

        public synchronized void handleWrite(int lines) {
            written += lines;
            update();
        }

        private void update() {
            WordBuffer buf = new WordBuffer(", ");
            if (commits > 0) {
                buf.append(resources.getString("stat.commits", new Integer(commits)));
            }
            if (read > 0) {
                buf.append(resources.getString("stat.lines.read", new Integer(read)));
            }
            if (written > 0) {
                buf.append(resources.getString("stat.lines.written", new Integer(written)));
            }
            setText(buf.toString());
        }

        public void gedcomHeaderChanged(Gedcom gedcom) {
        }

        public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
        }

        public void gedcomAfterUnitOfWork(Gedcom gedcom) {
        }

        public void gedcomWriteLockAcquired(Gedcom gedcom) {
        }

        public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
        }

        public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        }

        public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        }

        public void gedcomPropertyChanged(Gedcom gedcom, Property prop) {
        }

        public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property removed) {
        }

        public void gedcomRegistered(int num, Gedcom gedcom) {
            gedcom.addGedcomListener(this);
        }

        public void gedcomUnregistered(int num, Gedcom gedcom) {
            gedcom.removeGedcomListener(this);
        }
    } //Stats

    /** getDefaultFile() **/
    private String getDefaultFile(boolean dirOnly) {
        String defaultFile = NbPreferences.forModule(App.class).get("gedcomFile", "");
        if (defaultFile.isEmpty()) {
            return null;
        }
        File local = new File(defaultFile);
        if (dirOnly) {
            return local.getParent();
        }
        try {
            return new File(defaultFile).toURI().toURL().toString();
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
} //ControlCenter

