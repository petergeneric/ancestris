/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * pour changer le comportement sur exit. Voir http://forums.netbeans.org/topic15678.html
 *
 */
package ancestris.app;

import java.awt.EventQueue;
import java.util.Collection;
import org.openide.LifecycleManager;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */

@ServiceProvider(service=LifecycleManager.class, position=1)
public class MyLifecycleManager extends LifecycleManager {

    Runnable postExitCode = new Runnable() {
        public void run() {
            hookedExit();
        }
    };

    @Override
    public void exit() {
        final Semaphore sem = new Semaphore();
        sem.acquire();
        if (EventQueue.isDispatchThread()) {
            App.center.nbDoExit(postExitCode);
        } else {
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    App.center.nbDoExit(postExitCode);
                }
            });
        }
    }

    private void hookedExit() {

       Collection<? extends LifecycleManager> c = Lookup.getDefault().lookupAll(LifecycleManager.class);
       for (LifecycleManager lm: c) {
           if (lm != this) {
               lm.exit();
           }
       }
    }

    @Override
    public void saveAll() {
       Collection<? extends LifecycleManager> c = Lookup.getDefault().lookupAll(LifecycleManager.class);
       for (LifecycleManager lm: c) {
           if (lm != this) {
               lm.saveAll();
           }
       }
    }

    @Override
    public void markForRestart() {
       Collection<? extends LifecycleManager> c = Lookup.getDefault().lookupAll(LifecycleManager.class);
       for (LifecycleManager lm: c) {
           if (lm != this) {
               lm.markForRestart();
           }
       }
    }
}


