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

import genj.util.Trackable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Timer;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel + frederic
 */
@ServiceProvider(service = ProgressListener.class)
public class ProgressBar implements ProgressListener {

    Map<Trackable, ProcessData> map = new HashMap<>();   // separate processes data are necessary in case multiple trackables run at the same time

    public ProgressBar() {
    }

    @Override
    public void processStarted(Trackable process) {
        final Trackable track = process;

        ProcessData data = map.get(process);
        if (data == null) {
            data = new ProcessData();
            data.handle = ProgressHandle.createHandle(process.getTaskName(), () -> {
                track.cancelTrackable();
                return true;
            });
            map.put(process, data);
        }

        // prepare timer
        data.timer = new Timer(200, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ProcessData data = map.get(track);
                if (data == null) {
                    ((Timer) e.getSource()).stop();
                    return;
                }
                // update progress bar
                try {
                    data.handle.setDisplayName(track.getState());
                    data.handle.progress(track.getState(), track.getProgress());
                } catch (IllegalArgumentException ex) {
                    // NoOp : le trackable envoie des process decroissants
                    data.handle.progress(track.getState());
                }
            }
        });
        // we have 100 workunits
        // at this point the task appears in status bar.
        data.handle.start(100);

        data.timer.start();

    }

    @Override
    public void processStopped(Trackable process) {
        ProcessData data = map.get(process);
        if (data == null) {
            return;
        }
        data.timer.stop();
        // at this point the task is finished and removed from status bar
        // it's not realy necessary to count all the way to the limit, finish can be called earlier.
        // however it has to be called at the end of the processing.
        data.handle.finish();
        map.remove(process);
    }

    @Override
    public void processStopAll() {
        for (Trackable p : map.keySet()) {
            p.cancelTrackable();
            processStopped(p);
        }
    }

    private class ProcessData {

        public ProgressHandle handle;
        public Timer timer;
    }
}
