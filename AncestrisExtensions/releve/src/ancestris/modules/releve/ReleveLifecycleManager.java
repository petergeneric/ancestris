package ancestris.modules.releve;

import ancestris.app.Semaphore;
import ancestris.core.pluginservice.AncestrisPlugin;

import java.awt.EventQueue;
import java.util.Collection;

import org.openide.LifecycleManager;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Intercepte l'evenement de fermeture de NetBean
 * la position relative doit être inférieure à celle
 * de A
 * @author Michel
 */
@ServiceProvider(service = LifecycleManager.class, position = 0)
public class ReleveLifecycleManager extends LifecycleManager {

    @Override
    public void exit() {
        final Semaphore sem = new Semaphore();
        sem.acquire();
        if (EventQueue.isDispatchThread()) {
            exitReleve();
        } else {
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    exitReleve();
                }
            });
        }
    }

    void exitReleve() {
        final Semaphore sem = new Semaphore();
        sem.acquire();
        boolean canClose = true;
        for (ReleveTopComponent tc : AncestrisPlugin.lookupAll(ReleveTopComponent.class)) {
            // Si les données ont été modifiées, je demande à l'utilisateur s'il
            // veut les sauvegarder.
            // canClose retourne false si l'utilisateur refuse de sauvegarder et fermer l'application
            canClose &= tc.canClose();
        }

        if (canClose) {
            // on continue la fermeture
            // je sauvegarde les parametres
            for (ReleveTopComponent tc : AncestrisPlugin.lookupAll(ReleveTopComponent.class)) {
                tc.componentClosed();
            }
            // je lance l'exit de l'application suivante.
            sem.release(postExitCode);
        } else {
            // j'interrompt la fermeture
            sem.release(null);
        }
    }
    
    Runnable postExitCode = new Runnable() {

        @Override
        public void run() {
            hookedExit();
        }
    };

    /**
     *  lance l'exit du module suivant celui-ci
     */
    private void hookedExit() {
        Collection<? extends LifecycleManager> c = Lookup.getDefault().lookupAll(LifecycleManager.class);
        LifecycleManager releveLm = null;
        for (LifecycleManager lm : c) {
            if (lm == this) {
                releveLm = lm;
            } else {
                if (releveLm != null) {
                    // je lance l'exit LM suivant releveLM
                    lm.exit();
                    break;
                }
            }
        }
    }

    @Override
    public void saveAll() {
        Collection<? extends LifecycleManager> c = Lookup.getDefault().lookupAll(LifecycleManager.class);
        for (LifecycleManager lm : c) {
            if (lm != this) {
                lm.saveAll();
            }
        }
    }

    @Override
    public void markForRestart() {
        Collection<? extends LifecycleManager> c = Lookup.getDefault().lookupAll(LifecycleManager.class);
        for (LifecycleManager lm : c) {
            if (lm != this) {
                lm.markForRestart();
            }
        }
    }
}
