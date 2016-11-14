/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.gedcom.sosanumbers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;
import javax.swing.JComponent;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

/**
 *
 * @author frederic
 */
public class ProgressBar implements PropertyChangeListener {

    private ProgressMonitor progressMonitor;
    private Task task;

    /**
     * Creates new form ProgressPanel
     */
    public ProgressBar(JComponent component) {
        progressMonitor = new ProgressMonitor(component, "Running a Long Task", "", 0, 100);
        progressMonitor.setProgress(0);
        task = new Task();
        task.addPropertyChangeListener(this);
        task.execute();
        
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName() ) {
            int progress = (Integer) evt.getNewValue();
            String message = String.format("Completed %d%%.\n", progress);
            progressMonitor.setNote(message);
            progressMonitor.setProgress(progress);
            if (progressMonitor.isCanceled() || task.isDone()) {
                if (progressMonitor.isCanceled()) {
                    task.cancel(true);
                }
            }
        }
 
    }
    
    
    class Task extends SwingWorker<Void, Void> {
        
        @Override
        public Void doInBackground() {
            Random random = new Random();
            int progress = 0;
            setProgress(0);
            try {
                Thread.sleep(1000);
                while (progress < 100 && !isCancelled()) {
                    //Sleep for up to one second.
                    Thread.sleep(random.nextInt(1000));
                    //Make random progress.
                    progress += random.nextInt(10);
                    setProgress(Math.min(progress, 100));
                }
            } catch (InterruptedException ignore) {}
            return null;
        }
 
        @Override
        public void done() {
            progressMonitor.setProgress(100);
        }
    }
    
}
