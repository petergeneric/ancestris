package ancestris.modules.commonAncestor;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.awt.Actions;

/**
 * Action pour creer une intance de TodoTopComponent.
 * 
 * TodoOpenAction est associee au menu ancestris-modules-todoreport-TodoOpenActionNew.shadow"
 * dans layer.xml .
 *
 * Une instance de TodoOpenAction est cree par layer.xml au chargement de la librairie (voir attribut delegate)
 *
 * @author Michel
 */
public final class CommonAncestorOpenAction extends AbstractAction {

    /**
     * contructeur par défaut requis par l'attribut "delegate" dans layer.xml
     */
    public CommonAncestorOpenAction ( ) {
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
        CommonAncestorTopComponent component = CommonAncestorTopComponent.createInstance();

        if (component != null) {
            // j'affecte l'icone du menu de l'action au component
            if (e.getSource() instanceof org.openide.awt.Actions.MenuItem) {
                Actions.MenuItem action = (Actions.MenuItem) e.getSource();
                if (action.getIcon() instanceof ImageIcon) {
                    ImageIcon icon = (ImageIcon) action.getIcon();
                    component.setIcon(icon.getImage());
                }
            }
        }
    }
}
