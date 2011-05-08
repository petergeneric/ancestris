/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.util;

import genj.app.Workbench;
import genj.app.WorkbenchListener;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.Trackable;
import genj.view.View;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Cancellable;

/**
 *
 * @author daniel
 */

public class ProgressBar implements WorkbenchListener {

    ProgressHandle handle;
    Trackable track;
    /** timer */
    private Timer timer;

    public ProgressBar() {
    }

    public void processStarted(Workbench workbench, Trackable process) {
        track = process;
        handle = ProgressHandleFactory.createHandle(process.getTaskName(), new Cancellable() {

            public boolean cancel() {
                track.cancelTrackable();
                return true;
            }
        });

        // we have 100 workunits
        // at this point the task appears in status bar.
        handle.start(100);

        // prepare timer
        timer = new Timer(100, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // update progress bar
                try{
                    handle.progress(track.getState(), track.getProgress());
                } catch (IllegalArgumentException ex){
                    // NoOp : le trackable envoie des process decroissants
                    handle.progress(track.getState());
                }
            }
        });
        timer.start();

    }

    public void processStopped(Workbench workbench, Trackable process) {
        timer.stop();
        // at this point the task is finished and removed from status bar
        // it's not realy necessary to count all the way to the limit, finish can be called earlier.
        // however it has to be called at the end of the processing.
        handle.finish();
    }

    public void gedcomClosed(Workbench workbench, Gedcom gedcom) {
    }

    public void gedcomOpened(Workbench workbench, Gedcom gedcom) {
    }

    public void selectionChanged(Workbench workbench, Context context, boolean isActionPerformed) {
    }

    public void viewClosed(Workbench workbench, View view) {
    }

    public void viewOpened(Workbench workbench, View view) {
    }

    public void workbenchClosing(Workbench workbench) {
    }

    public void commitRequested(Workbench workbench, Context context) {
    }
}
