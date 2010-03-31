/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app;

import org.openide.util.Exceptions;

/**
 *
 * @author daniel
 */
public class Semaphore {
    int isLoaded = 0;
   public void acquire(){
        synchronized (this) {
            isLoaded ++;
            this.notifyAll();
        }
   }
   public void release() {
       synchronized (this) {
           isLoaded--;
           this.notifyAll();
       }
   }

   public boolean isBusy() {
       if (isLoaded == 0)
           return true;
       synchronized(this) {
            try {
                while (isLoaded != 0) {
                    this.wait();
                }
                return true;
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
                return false;
            }
        }
   }
}
