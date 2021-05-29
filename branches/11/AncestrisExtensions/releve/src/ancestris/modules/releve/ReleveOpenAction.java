package ancestris.modules.releve;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Action pour creer une intance de ReleveTopComponent.
 * 
 * @author Michel
 */
public final class ReleveOpenAction extends AbstractAction {

    /**
     * contructeur par défaut requis par l'attribut "delegate" dans layer.xml
     */
    public ReleveOpenAction ( ) {
        super();
    }

    /**
     * Creation du composant et affectation de la meme icone que celle du menu.
     * Cette methode est appelee quand l'utilsateur clique sur le menu du plugin 
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // je cree le composant
        ReleveTopComponent component = new ReleveTopComponent();
//        Mode mode = WindowManager.getDefault().findMode("ancestris-output");
//        if (mode != null) {
//            mode.dockInto(component);
//        }
        component.open();
        component.requestActive();        
    }
}
