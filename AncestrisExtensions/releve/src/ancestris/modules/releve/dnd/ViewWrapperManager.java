package ancestris.modules.releve.dnd;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.views.tree.TreeTopComponent;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.tree.TreeView;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.openide.windows.WindowManager;

/**
 * Cette classe encapsule les appels à la classe TreeView
 *   - activation du drag and drop
 *   - appel à la méthode setRoot(Entity)
 *   - appel à la méthode setShow(Entity)
 * @author Michel
 */
public class ViewWrapperManager {
    
    // listener des creations de TreeView 
    // ce listener est commun a toutes les instances du plugin releve
    static private PropertyChangeListener propertyChangeListener;
    static private int propertyChangeListenerCount = 0;
    
    /**
     * ViewWrapper factory 
     * @param component
     * @return 
     */
    static AbstractViewWrapper createViewWrapper(Component component) {
        if (component instanceof TreeView) {
            return new TreeViewWrapper( (TreeView) component);
        } else {
            return new NullViewWrapper();
        }        
    }
    
    /**
     * active le drag and drop pour toutes les vues TreeView existantes
     * et demarre un listener pour intercepter toutes les nouvelles vues 
     * et activer le drag and drop
     */
    static public void addTreeViewListener() {
        
        if ( propertyChangeListener == null ) {
            // j'active le drag and drop pour toutes les vues ouvertes
            for (TreeTopComponent tc : AncestrisPlugin.lookupAll(TreeTopComponent.class)) {
                TreeView treeView = (TreeView) tc.getView();
                TreeViewDropTarget viewDropTarget = new TreeViewDropTarget();
                viewDropTarget.createDropTarget(treeView);
            }

            // j'active le drag and drop pour le futures vues
            propertyChangeListener = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    // EditTopComponent
                    if (evt.getNewValue() instanceof TreeTopComponent  ) {
    //                    System.out.println("LookupTreeView getPropertyName " + evt.getPropertyName());
    //                    System.out.println("   LookupTreeView getOldValue " + evt.getOldValue());
    //                    System.out.println("   LookupTreeView getNewValue " + evt.getNewValue());
                        if( "tcOpened".equals(evt.getPropertyName()) ) {
                            TreeTopComponent treeTopComponent = (TreeTopComponent) evt.getNewValue();
                            if (treeTopComponent.getView() instanceof TreeView) {
                                TreeView treeView = (TreeView) treeTopComponent.getView();
                                TreeViewDropTarget viewDropTarget = new TreeViewDropTarget();
                                viewDropTarget.createDropTarget(treeView);
                                //System.out.println("    ActivateDndWithTreeView view=" + treeView.toString() + " change property=" + evt.getNewValue());
                            }
                        }
                    }
                }
            };

            WindowManager.getDefault().getRegistry().addPropertyChangeListener(propertyChangeListener);
        }
        propertyChangeListenerCount++;
        
        
    }
    
    /**
     * supprime le listener d'interception des ouvertures des nouvelles vues 
     */
    static public void removeTreeViewListener() {
        propertyChangeListenerCount--;
        if(propertyChangeListenerCount == 0) {
            WindowManager.getDefault().getRegistry().removePropertyChangeListener(propertyChangeListener);  
            propertyChangeListener = null;
        }
    }
    
    static public void setRootAllTreeview(Property property) {
        // j'active le drag and drop pour toutes les vues ouvertes avec meme gedcom que l'entité
        for (TreeTopComponent tc : AncestrisPlugin.lookupAll(TreeTopComponent.class)) {
            TreeView treeView = (TreeView) tc.getView();
            if (treeView.getGedcom().equals(property.getGedcom())) {
                TreeViewWrapper treeViewWrapper = new TreeViewWrapper(treeView);
                treeViewWrapper.setRoot(property);
            }
        }
    }
    
    static public void showEntityAllTreeview(Property property) {
        // j'active le drag and drop pour toutes les vues ouvertes avec meme gedcom que l'entité
        for (TreeTopComponent tc : AncestrisPlugin.lookupAll(TreeTopComponent.class)) {
            TreeView treeView = (TreeView) tc.getView();
            if (treeView.getGedcom().equals(property.getGedcom())) {
                TreeViewWrapper treeViewWrapper = new TreeViewWrapper(treeView);
                treeViewWrapper.show(property);
            }
        }
    }
    
   /**
    * 
    */
    protected static abstract class AbstractViewWrapper {
        abstract public void setRoot(Property property); 
        abstract boolean show(Property showProperty);
    }
    
    /**
     * 
     */
    static private class NullViewWrapper extends AbstractViewWrapper {
        
        @Override
        public void setRoot(Property property) {
        }
        
        @Override
        public boolean show(Property showProperty) {
            return true;
        }
        
    }
    
    /**
     * 
     */    
    static private class TreeViewWrapper extends AbstractViewWrapper {

        private TreeView treeView = null;

        TreeViewWrapper(Component treeView) {
            if (treeView instanceof TreeView) {
                this.treeView = (TreeView) treeView;
            }
        }

        /**
         * place l'entité en tant que root de l'arbre 
         * @param rootProperty 
         */
        @Override
        public void setRoot(Property rootProperty) {
            
            // je propage la selection l'entité a tous les outils utilisant le gedcom de l'entité
            // je simule un clic avec le bouton 1 de la souris sur l'entité
            //  attention : il faut que la source de l'evenement de MouseEvent soit un composant fils de treeView
            MouseEvent e = new MouseEvent(treeView.getComponent(0), MouseEvent.MOUSE_PRESSED, 0, MouseEvent.BUTTON1, 0, 0, 1, false);
            
            if( rootProperty instanceof Entity)  {
                // je declare l'entité comme racine de l'arbre
                Entity rootEntity = (Entity) rootProperty;
                treeView.setRoot(rootEntity);
                SelectionDispatcher.fireSelection(e, new Context(rootEntity));                
            } else {
                Entity rootEntity = rootProperty.getEntity();
                if ( rootEntity != null) {
                    treeView.setRoot(rootEntity);
                }
                List<Property> properties = new ArrayList<Property>();
                properties.add(rootProperty);
                SelectionDispatcher.fireSelection(new Context(rootProperty.getGedcom(), new ArrayList<Entity>(), properties));                
            }            
        }
        
        
         


        /**
         * sélectionne l'entité (cadre rouge) 
         * et centre l'entité dans la vue
         * @param entity 
         */
        @Override
        public boolean show(Property showProperty) {

            boolean showResult = false; 
            MouseEvent e = new MouseEvent(treeView.getComponent(0), MouseEvent.MOUSE_PRESSED, 0, MouseEvent.BUTTON1, 0, 0, 1, false);
            
            if( showProperty instanceof Entity)  {
                // je declare l'entité comme racine de l'arbre
                Entity showEntity = (Entity) showProperty;
                showResult = showTreeView(showEntity);
                // je propage la selection l'entité a tous les outils utilisant le gedcom de l'entité
                // je simule un clic avec le bouton 1 de la souris sur l'entité
                //  attention : il faut que la source de l'evenement de MouseEvent soit un composant fils de treeView            
                SelectionDispatcher.fireSelection(e, new Context(showEntity));                
            } else {
                Entity showEntity = showProperty.getEntity();
                if ( showEntity != null) {
                    showResult = showTreeView(showEntity);
                }
                List<Property> properties = new ArrayList<Property>();
                properties.add(showProperty);

                // je propage la selection l'entité a tous les outils utilisant le gedcom de l'entité
                // je simule un clic avec le bouton 1 de la souris sur l'entité
                //  attention : il faut que la source de l'evenement de MouseEvent soit un composant fils de treeView
                SelectionDispatcher.fireSelection(new Context(showProperty.getGedcom(), new ArrayList<Entity>(), properties));
                
            }
                 
            return showResult;
        }       
        
        private boolean showTreeView(Entity showEntity) {
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
            } catch (IllegalAccessException ex) {
            } catch (IllegalArgumentException ex) {
            } catch (InvocationTargetException ex) {
            }
            return showResult;
        }
              
    }
}
