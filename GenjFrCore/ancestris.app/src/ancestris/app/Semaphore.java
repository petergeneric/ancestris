/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import java.awt.EventQueue;

/**
 *
 * @author daniel
 */
public class Semaphore {

    int countUsed = 0;

    public synchronized void acquire() {
        countUsed++;
        this.notifyAll();
    }

    public void release() {
        release(null);
    }

    public synchronized void release(Runnable runIfCounterReache0) {
        if (countUsed > 0) {
            countUsed--;
            if (countUsed == 0 && runIfCounterReache0 != null) {
                EventQueue.invokeLater(runIfCounterReache0);
            }
        }
        this.notifyAll();
    }
}
