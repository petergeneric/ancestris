package ancestris.modules.releve.merge;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.views.tree.TreeTopComponent;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.tree.TreeView;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Exceptions;

/**
 * Cette classe encapsule les appels à la classe TreeView
 *   - activation du drag and drop
 *   - appel à la méthode setRoot(Entity)
 *   - appel à la méthode setShow(Entity)
 * @author Michel
 */
public class SelectionManager {

    static public void setRootAllTreeview(Property property) {
        // j'active le drag and drop pour toutes les vues ouvertes avec meme gedcom que l'entité
        for (TreeTopComponent tc : AncestrisPlugin.lookupAll(TreeTopComponent.class)) {
            TreeView treeView = (TreeView) tc.getView();
            if (treeView.getGedcom().equals(property.getGedcom())) {
                setRoot(tc, property);
            }
        }
    }

    static public void showEntityAllTreeview(Property property) {
        // j'active le drag and drop pour toutes les vues ouvertes avec meme gedcom que l'entité
        for (TreeTopComponent tc : AncestrisPlugin.lookupAll(TreeTopComponent.class)) {
            show(tc, property);
        }
    }

    /**
     *
     */
    /**
     * place l'entité en tant que root de l'arbre
     *
     * @param rootProperty
     */
    static public void setRoot(Component component, Property rootProperty) {

        // je propage la selection l'entité a tous les outils utilisant le gedcom de l'entité
        if (rootProperty instanceof Entity) {
            // je declare l'entité comme racine de l'arbre
            Entity rootEntity = (Entity) rootProperty;
            if (component instanceof TreeTopComponent) {
                ((TreeView) ((TreeTopComponent) component).getView()).setRoot(rootEntity);
                // je simule un clic avec le bouton 1 de la souris sur l'entité
                //  attention : il faut que la source de l'evenement de MouseEvent soit un composant fils de treeView
                Component subComponent = ((TreeView) ((TreeTopComponent) component).getView()).getComponent(0);
                MouseEvent e = new MouseEvent(subComponent, MouseEvent.MOUSE_PRESSED, 0, MouseEvent.BUTTON1, 0, 0, 1, false);
                SelectionDispatcher.fireSelection(e, new Context(rootEntity));
            } else {

            }

        } else {
            Entity rootEntity = rootProperty.getEntity();
            if (rootEntity != null) {
                if (component instanceof TreeTopComponent) {
                    ((TreeView) ((TreeTopComponent) component).getView()).setRoot(rootEntity);
                }
            }
            List<Property> properties = new ArrayList<Property>();
            properties.add(rootProperty);
            SelectionDispatcher.fireSelection(new Context(rootProperty.getGedcom(), new ArrayList<Entity>(), properties));
        }
    }

    /**
     * sélectionne l'entité (cadre rouge) et centre l'entité dans la vue
     *
     * @param entity
     */
    static public boolean show(Component component, Property showProperty) {

        boolean showResult = false;

        if (showProperty instanceof Entity) {
            // je declare l'entité comme racine de l'arbre

            Entity showEntity = (Entity) showProperty;
            if (component instanceof TreeTopComponent) {
                TreeView treeView = ((TreeView) ((TreeTopComponent) component).getView());
                showResult = showTreeView(treeView, showEntity);
                // je simule un clic avec le bouton 1 de la souris sur l'entité
                //  attention : il faut que la source de l'evenement de MouseEvent soit un composant fils de treeView
                Component subComponent = ((TreeView) ((TreeTopComponent) component).getView()).getComponent(0);
                MouseEvent e = new MouseEvent(subComponent, MouseEvent.MOUSE_PRESSED, 0, MouseEvent.BUTTON1, 0, 0, 1, false);
                SelectionDispatcher.fireSelection(e, new Context(showEntity));
            } else {

            }

        } else {
            Entity showEntity = showProperty.getEntity();
            if (showEntity != null) {
                if (component instanceof TreeTopComponent) {
                    TreeView treeView = ((TreeView) ((TreeTopComponent) component).getView());
                    showResult = showTreeView(treeView, showEntity);
                }
                List<Property> properties = new ArrayList<Property>();
                properties.add(showProperty);

                // je propage la selection l'entité a tous les outils utilisant le gedcom de l'entité
                // je simule un clic avec le bouton 1 de la souris sur l'entité
                //  attention : il faut que la source de l'evenement de MouseEvent soit un composant fils de treeView
                SelectionDispatcher.fireSelection(new Context(showProperty.getGedcom(), new ArrayList<Entity>(), properties));

            }

        }
        return showResult;
    }
        
        

    static private boolean showTreeView(TreeView treeView, Entity showEntity) {
        // solution 1 : treeView.setContext , mais ne fonctionne qu'avec Ancestris 0.7 
        // je centre la vue sur l'entité
        //treeView.setContext(new Context(entity), true);

        // solution 2 : j'appelle  la méthode privée TreeView.show(Entity) en utilisant la relflexion
        // pas ortodoxe , mais ça marche 
        // il faudra demander à Daniel s'il peut rendre la méthode publique 
        boolean showResult = false;
        try {
            // je centre l'entité dans la vue
            Method privateMethod = TreeView.class.getDeclaredMethod("show", Entity.class, boolean.class);
            privateMethod.setAccessible(true);
            showResult = (Boolean) privateMethod.invoke(treeView, showEntity, true);
        } catch (NoSuchMethodException ex) {
        } catch (SecurityException ex) {
        } catch (IllegalArgumentException ex) {
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return showResult;
    }

}
