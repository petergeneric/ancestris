package ancestris.modules.releve.merge;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.views.tree.TreeTopComponent;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.tree.TreeView;

/**
 * Cette classe encapsule les appels Aux vues d'Ancestris
   - appel à la méthode setRootEntity(Entity)
   - appel à la méthode setShow(Entity)
 * @author Michel
 */
public class SelectionManager {

    /**
     * selectionne une entité en tant que root dans les TreeView associées au gedcom
     * de la propriété de cette entité
     * @param property
     */
    static public void setRootEntity(Property property) {

        if (property != null) {
            Entity entity ;
            if (property instanceof Entity) {
                entity = (Entity) property;
            } else {
                entity = property.getEntity();
            }
            if ( entity != null ) {
                for (TreeTopComponent tc : AncestrisPlugin.lookupAll(TreeTopComponent.class)) {
                    TreeView treeView = (TreeView) tc.getView();
                    if (treeView.getGedcom().equals(property.getGedcom())) {
                        treeView.setRoot(entity);
                        // TODO traiter le cas ou la vue est lockée (chercher d'autres arbres)
                        break;
                    }
                }
            }

            // je selectionne la propriété
            SelectionDispatcher.fireSelection(new Context(property));
        }
    }

    /**
     * selectionne une propriété dans les vues du gedcom de la propriété
     * @param property
     */
    static public void showEntity(Property property) {
        SelectionDispatcher.fireSelection(new Context(property));
    }

}
