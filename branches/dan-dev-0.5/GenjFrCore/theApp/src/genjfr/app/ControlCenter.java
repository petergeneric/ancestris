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

import genjfr.explorer.GedcomTableWidget;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Property;
import genjfr.util.GedcomDirectory;
import genj.io.Filter;
import genj.util.DirectAccessTokenizer;
import genj.util.EnvironmentChecker;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.WordBuffer;
import genj.util.swing.Action2;
import genj.util.swing.FileChooser;
import genj.util.swing.HeapStatusWidget;
import genj.util.swing.NestedBlockLayout;
import genj.view.ViewContext;

import java.awt.BorderLayout;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import genj.app.*;
import genj.gedcom.Context;
import genj.util.swing.DialogHelper;
import java.net.URL;
import java.util.logging.Level;
import javax.swing.SwingUtilities;

/**
 * The central component of the GenJ application
 */
public class ControlCenter extends JPanel{


    /** members */
    private GedcomTableWidget tGedcoms;
    protected Registry registry;
    private Resources resources = Resources.get(genj.app.Workbench.class);
    private List gedcomActions = new ArrayList();
    protected Stats stats = new Stats();

    private int isLoaded = 1;
    final private Object loadLock = new Object();

    /**
     * Constructor
     */
    public ControlCenter(Registry setRegistry) {

        // Initialize data
        registry = new Registry(setRegistry, "cc");

        // Table of Gedcoms
        tGedcoms = new GedcomTableWidget(registry) {
            public ViewContext getContext() {
                ViewContext result = super.getContext();
                if (result != null) {
                    result.addAction(new ActionSave());
                    result.addAction(new ActionClose());
                }
                return result;
            }

            ;
        };

        // ... Listening
        tGedcoms.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                for (int i = 0; i < gedcomActions.size(); i++) {
                    ((Action2) gedcomActions.get(i)).setEnabled(tGedcoms.getSelectedContext() != null);
                }
            }
        });

        // Layout
        setLayout(new BorderLayout());
        add(new JScrollPane(tGedcoms), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

    }

    /**
     * Loads gedcom files
     * - if files = null, case of request to load default gedcom
     *    - if exist, load it
     *    - otherwise, if no files to load, then ask user
     * - otherwise load files
     *
     */
    public void load(Collection files) {
        SwingUtilities.invokeLater(new ActionAutoOpen(files));
    }  

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
                    Exceptions.printStackTrace(ex);
                    return false;
                }
            }
        }
//        return false;
    }

    public void nbDoExit(final Runnable postExitCode) {
        final Semaphore sem = new Semaphore();
        sem.acquire();
        // force a commit
        for (Context context:GedcomDirectory.getInstance().getGedcoms())
            App.workbenchHelper.fireCommit(context);

            for (Context context:GedcomDirectory.getInstance().getGedcoms()){
//            // next gedcom
//            Gedcom gedcom = context.getGedcom();
//            // changes need saving?
//            if (gedcom.hasChanged()) {
//                // close file officially
//                int rc = DialogHelper.openDialog(
//                        null, DialogHelper.WARNING_MESSAGE,
//                        resources.getString("cc.savechanges?", gedcom.getName()),
//                        Action2.yesNoCancel(), ControlCenter.this);
//                // cancel - we're done
//                if (rc == 2) {
//                    return;
//                }
//                // yes - close'n save it
//                if (rc == 0) {
//                    // block exit
//                    sem.acquire();
//                    // run save
//                    App.workbenchHelper.saveGedcomImpl(gedcom, new Filter[0]);
//                    sem.release(postExitCode);
////            return;
//                }
//                // no - skip it
//            }
            App.workbenchHelper.closeGedcom(context, false);
            // next gedcom
        }
        // Done
        sem.release(postExitCode);
    }

    /**
     * Action - LoadLastOpen
     */
    private class ActionAutoOpen implements Runnable {

        /** files to load */
        private Collection<String> files = null;

        /** constructor */
        private ActionAutoOpen(Collection<String> theFiles) {
            if (theFiles == null) {
                theFiles = new ArrayList<String>();
            }
//            addDefaultFile(theFiles);
            if (NbPreferences.forModule(App.class).get("optionswizard", "").equals("4")) {
                if (theFiles.isEmpty() && getDefaultFile(theFiles) == null) {
                    App.workbenchHelper.openGedcom();
                }
            }
            files = theFiles;
        }

        private String getDefaultFile(Collection<String> files) {
            File defaultFile = new File(NbPreferences.forModule(App.class).get("gedcomFile", ""));
            if (defaultFile == null) {
                return null;
            }
            if (!defaultFile.exists()) {
                return null;
            }
            String defaultFilePath = null;
            try {
                defaultFilePath = defaultFile.getCanonicalPath();
            } catch (Exception ex) {
                return null;
            }
            String filepath = null;

            for (String file : files) {
                try {
                    DirectAccessTokenizer tokens = new DirectAccessTokenizer(file, ",", false);
                    filepath = (new File(new URL(tokens.get(0)).getFile())).getCanonicalPath();
                } catch (Exception ex) {
                    continue;
                }
                if (defaultFilePath.equals(filepath)) {
                    return null;
                }
            }
            try {
                return (new URL("file", "", defaultFile.getAbsolutePath())).toString();
            } catch (Exception ex) {
                return null;
            }
        }

        /** run */
        public void run() {

            // Loop over files to open
            if (files != null && !files.isEmpty()) {
                for (String uriStr: files) {
                    if (getOpenedGedcom(uriStr) != null) {
                        break;
                    }
                    try {
                        DirectAccessTokenizer tokens = new DirectAccessTokenizer(uriStr, ",", false);
                        String restore = tokens.get(0);

                        // check if it's a local file
                        File local = new File(restore);
                        if (local.exists()) {
                            local.toURI().toURL().toString();
                        }
                        App.workbenchHelper.openGedcom(new URL(restore));

                    } catch (Throwable t) {
                        App.LOG.log(Level.WARNING, "cannot restore " + uriStr, t);
                    }

                    // next
                }
            }

            // open default file if necessary
            {
                String restore = getDefaultFile(files);
                if (restore != null && getOpenedGedcom(restore) == null) {
                    try {

                        // check if it's a local file
                        File local = new File(restore);
                        if (local.exists()) {
                            local.toURI().toURL().toString();
                        }

                        App.workbenchHelper.openGedcom(new URL(restore));
                    } catch (Throwable t) {
                        App.LOG.log(Level.WARNING, "cannot restore " + restore, t);
                    }

                    // next
                }
            }


            // done
            App.center.isReady(-1);
        }
    } //LastOpenLoader

    public Context getSelectedContext(){
        return tGedcoms.getSelectedContext();
    }

    /**
     * @deprecated use getSelectedContext().getGedcom()
     */
    public Gedcom getSelectedGedcom() {
        return getSelectedContext().getGedcom();
    }


    public Stats getStats() {
        return stats;
    }

    public void selectionChanged(Workbench workbench, Context context, boolean actionPerformed) {
        tGedcoms.selectionChanged(workbench, context, actionPerformed);
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

        public void gedcomRegistered(int num, Context context) {
            context.getGedcom().addGedcomListener(this);
        }

        public void gedcomUnregistered(int num, Context context) {
            context.getGedcom().removeGedcomListener(this);
        }
    } //Stats

    public Collection<String> getOpenedGedcoms() {
        // Remember open gedcoms
        Collection<String> save = new ArrayList();
        for (Context context: GedcomDirectory.getInstance().getGedcoms()){
            // next gedcom
            Gedcom gedcom = context.getGedcom();
            // remember as being open, password and open views
            File file = gedcom.getOrigin().getFile();
            if (file == null || file.exists()) {
                StringBuffer restore = new StringBuffer();
                restore.append(gedcom.getOrigin());
                restore.append(",");
                if (gedcom.hasPassword()) {
                    restore.append(gedcom.getPassword());
                }
                save.add(restore.toString());
            }
            // next gedcom
        }
        // Done
        return save;
    }

    /**
     * @deprecated use getOpenedContext()
     */
    public Gedcom getOpenedGedcom(String gedName) {
        Context c = getOpenedContext(gedName);
        return c == null?null:c.getGedcom();
    }

    public Context getOpenedContext(String gedName) {

        if (gedName == null) {
            return null;
        }
        // grab "file[, password][, view#x]"
        DirectAccessTokenizer tokens = new DirectAccessTokenizer(gedName, ",", false);
        String url = tokens.get(0);
        if (url == null) {
            return null;
        }
        for (Context context: GedcomDirectory.getInstance().getGedcoms()){
            if (url.equals(context.getGedcom().getOrigin().toString())){
                return context;
            }
        }
        return null;
    }

    /** getDefaultFile() **/
    private String getDefaultFile(boolean dirOnly) {
        String defaultFile = NbPreferences.forModule(App.class).get("gedcomFile", "");
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
            Exceptions.printStackTrace(ex);
        }
        return "";
    }
} //ControlCenter

