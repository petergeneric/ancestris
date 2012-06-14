package ancestris.modules.releve;

import java.util.Set;
import org.openide.modules.ModuleInstall;
import org.openide.windows.TopComponent;

/**
 *
 * @author Michel
 */
public class Installer extends ModuleInstall {

    /**
     * intercepte la fermeture de Netbeans
     * et demande à l'utilisateur s'il veut sauvegarder les données
     * si elle ont été modifiées depuis la derniere sauvegarde
     *
     * @return false si l'utilisateur veut annuler la fermeture de Netbeans
     */
    @Override
    public boolean closing() {
        ReleveTopComponent topComp = null;
        boolean canClose = true;

        Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
        for (TopComponent tc: tcs) {
            if (tc instanceof ReleveTopComponent) {
                topComp = (ReleveTopComponent) tc;
                // Si les données ont été modifiées, je demande à l'utilisateur s'il
                // veut les sauvegarder.
                // canClose retourne false si l'utilisateur refuse de sauvegarder et fermer l'application
                canClose &= topComp.canClose();
                break;
            }
        }
        return canClose;
    }
}
