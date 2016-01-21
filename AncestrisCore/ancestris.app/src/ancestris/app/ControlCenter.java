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
package ancestris.app;

import ancestris.gedcom.GedcomDirectory;
import genj.util.DirectAccessTokenizer;

import java.util.ArrayList;
import java.util.Collection;


import org.openide.util.Exceptions;
import genj.gedcom.Context;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * The central component of the Ancestris application
 */
// XXX: this entire class must be at least redesigned or removed
public class ControlCenter {

    /** members */

    private int isLoaded = 1;
    final private Object loadLock = new Object();
    private static final Logger LOG = Logger.getLogger("ancestris");

    /**
     * Loads gedcom files
     * - if files = null, case of request to load default gedcom
     *    - if exist, load it
     *    - otherwise, if no files to load, then ask user
     * - otherwise load files
     *
     */
    public void load(Collection<FileObject> files) {
        SwingUtilities.invokeLater(new ActionAutoOpen(files));
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

    public void nbDoExit(Runnable postExitCode) {
        final Semaphore sem = new Semaphore();
        sem.acquire();

        StartupFiles.getDefault().addOpenedGedcoms();
        for (Context context:GedcomDirectory.getDefault().getContexts()){
            if (!GedcomDirectory.getDefault().closeGedcom(context))
                    postExitCode = null;
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
        private Collection<FileObject> files = null;

        /** constructor */
        private ActionAutoOpen(Collection<FileObject> theFiles) {
            if (theFiles == null) {
                theFiles = new ArrayList<FileObject>();
            }
// TODO: demander l'ouverture d'un fichier gedcom si aucun fichier n'est ouvert. Comment?
            files = theFiles;
        }

        /** run */
        public void run() {

            // Loop over files to open
            if (files != null && !files.isEmpty()) {
                for (FileObject file: files) {
                        GedcomDirectory.getDefault().openGedcom(file);
                        // FIXME: should we save and restore passwords, and how?
                }
            }
            //XXX: activate welcome TC if opened just after opening all files.
            // This must not be done here but activated in welcome module. In fact this is already done
            // but as all other TC are "manually" opened at startup (ie do not use NB persistence
            // topcomponent) we must open welcome here.
            // We will have to investigate to properly use Persistence API for TC
            TopComponent welcome = WindowManager.getDefault().findTopComponent("Welcome");
            if (welcome!= null){
                welcome.requestActive();
            }

            // done
            App.center.isReady(-1);
        }
    } //LastOpenLoader

    /**
     * @deprecated will use GedcomDirectory API
     */
    @Deprecated
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
        for (Context context: GedcomDirectory.getDefault().getContexts()){
            if (url.equals(context.getGedcom().getOrigin().toString())){
                return context;
            }
        }
        return null;
    }

} //ControlCenter

