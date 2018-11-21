/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright Ancestris
 *
 * Author: Frederic Lapeyre (frederic-at-ancestris-dot-org). 2006-2016
 * Author: Dominique Baron (lemovice-at-ancestris-dot-org). 2012
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.sosanumbers;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

/**
 *
 * @author Zurga
 */
class Task extends SwingWorker<Void, Void> {

    private final ProgressMonitor pm;
    private int maxp = 0;

    public Task(ProgressMonitor progressMonitor, int maxProgress) {
        pm = progressMonitor;
        maxp = maxProgress;
    }

    @Override
    public Void doInBackground() {
        return null;
    }

    @Override
    public void done() {
        if (pm != null) {
            pm.setProgress(maxp);
        }
    }

}
