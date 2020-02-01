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
import org.openide.util.Lookup;

/**
 * Progress Callback
 */
public interface ProgressListener {

    /**
     * notificaton that a long running process has started
     *
     * @param process
     */
    public void processStarted(Trackable process);

    /**
     * notificaton that a long running process has finished
     *
     * @param process
     */
    public void processStopped(Trackable process);

    public void processStopAll();

    public class Dispatcher {

        public static void processStarted(Trackable process) {
            for (ProgressListener listener : Lookup.getDefault().lookupAll(ProgressListener.class)) {
                listener.processStarted(process);
            }
        }

        public static void processStopped(Trackable process) {
            for (ProgressListener listener : Lookup.getDefault().lookupAll(ProgressListener.class)) {
                listener.processStopped(process);
            }
        }

        public static void processStopAll() {
            for (ProgressListener listener : Lookup.getDefault().lookupAll(ProgressListener.class)) {
                listener.processStopAll();
            }
        }

    }
}
