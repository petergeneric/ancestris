/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2011 Ancestris
 *
 * Author: Daniel Andre (daniel@ancestris.org).
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.util;

import genj.app.ProgressListener;
import genj.util.Trackable;
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
public class ProgressBar implements ProgressListener {

    ProgressHandle handle;
    Trackable track;
    /** timer */
    private Timer timer;

    public ProgressBar() {
    }

    @Override
    public void processStarted(Trackable process) {
        track = process;
        handle = ProgressHandleFactory.createHandle(process.getTaskName(), new Cancellable() {

            @Override
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

            @Override
            public void actionPerformed(ActionEvent e) {
                // update progress bar
                try {
                    handle.progress(track.getState(), track.getProgress());
                } catch (IllegalArgumentException ex) {
                    // NoOp : le trackable envoie des process decroissants
                    handle.progress(track.getState());
                }
            }
        });
        timer.start();

    }

    @Override
    public void processStopped(Trackable process) {
        timer.stop();
        // at this point the task is finished and removed from status bar
        // it's not realy necessary to count all the way to the limit, finish can be called earlier.
        // however it has to be called at the end of the processing.
        handle.finish();
    }
}
