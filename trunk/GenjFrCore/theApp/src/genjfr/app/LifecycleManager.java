/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * pour changer le comportement sur exit. Voir http://forums.netbeans.org/topic15678.html
 *
 */
package genjfr.app;

import java.awt.EventQueue;
import org.openide.util.Exceptions;

/**
 *
 * @author daniel
 */
//@ServiceProvider(service=org.openide.LifecycleManager.class)
public class LifecycleManager extends org.openide.LifecycleManager {

    Runnable postExitCode = new Runnable() {

        public void run() {
            hookedExit();
        }
    };

    @Override
    public void saveAll() {
        try {
            Class lcmClass = Class.forName("org.netbeans.core.NbTopManager$NbLifecycleManager", true, Thread.currentThread().getContextClassLoader());
            org.openide.LifecycleManager lcm = (org.openide.LifecycleManager) lcmClass.newInstance();
            lcm.saveAll();
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void markForRestart() {
        try {
            Class lcmClass = Class.forName("org.netbeans.core.NbTopManager$NbLifecycleManager", true, Thread.currentThread().getContextClassLoader());
            org.openide.LifecycleManager lcm = (org.openide.LifecycleManager) lcmClass.newInstance();
            lcm.markForRestart();
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void exit() {
        final Semaphore sem = new Semaphore();
        sem.acquire();
        if (!EventQueue.isDispatchThread()) {
            App.center.nbDoExit(postExitCode);
        } else {
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    App.center.nbDoExit(postExitCode);
                }
            });
        }
    }

    private static void hookedExit() {

        try {
            Class lcmClass = Class.forName("org.netbeans.core.NbTopManager$NbLifecycleManager", true, Thread.currentThread().getContextClassLoader());
            org.openide.LifecycleManager lcm = (org.openide.LifecycleManager) lcmClass.newInstance();
            lcm.exit();
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
