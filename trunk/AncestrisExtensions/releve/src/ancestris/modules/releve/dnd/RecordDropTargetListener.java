package ancestris.modules.releve.dnd;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.editors.gedcom.GedcomTopComponent;
import ancestris.modules.editors.genealogyeditor.AriesTopComponent;
import ancestris.modules.editors.standard.CygnusTopComponent;
import ancestris.modules.releve.merge.MergeDialog;
import ancestris.modules.views.tree.TreeTopComponent;
import ancestris.view.AncestrisTopComponent;
import genj.edit.EditView;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.tree.TreeView;
import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;


/**
 * cette classe décrit le comportement du drop d'un relevé dans une autre vue d'Ancestris
 */
public class RecordDropTargetListener implements DropTargetListener {        // 2021-02-10 FL : deprecated - replaced with Ancestris DND system

    
    // ce listener est commun a toutes les instances du plugin releve
    static private PropertyChangeListener propertyChangeListener;
    static private int propertyChangeListenerCount = 0;

    /**
     * active le drag and drop pour les vues d'Ancestris déjà ouverts
     * et demarre un listener pour intercepter l'ouverture des nouvelles vues 
     * et activer le drag and drop.
     * Cette methode est appelée au démarrage de ReleveTopComponent.
     * 
     */
    static public void addTreeViewListener() {
        // j'active le listener s'il n'a pas déja été créé par une autre instance de ReleveTopComponent 
        if ( propertyChangeListener == null ) {
            // j'active le drag and drop pour les futures vues
            
            propertyChangeListener = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    // j'active le DnD si c'est un evenement tcOpened d'ouverture d'une vue
                    if ("tcOpened".equals(evt.getPropertyName())) {
                        if (evt.getNewValue() instanceof Component) {
                            Component component = (Component) evt.getNewValue();
                            // je verifie si c'est un composant géré 
                            Component droppableCoponent = getDroppableComponent(component);
                            if (droppableCoponent != null) {
                                DropTarget dropTarget = new DropTarget(droppableCoponent, new RecordDropTargetListener());
                            }
                        }
                    }
                }
            };

            WindowManager.getDefault().getRegistry().addPropertyChangeListener(propertyChangeListener);
            
            // j'active le drag and drop pour toutes les vues déjà ouvertes
            for (AncestrisTopComponent component : AncestrisPlugin.lookupAll(AncestrisTopComponent.class)) {
                Component droppableCoponent = getDroppableComponent(component);
                if (droppableCoponent != null) {
                    DropTarget dropTarget = new DropTarget(droppableCoponent, new RecordDropTargetListener());
                }
            }            
        }
        // je compte le nombre d'instances de ReleveTopComponent qui ont appelé cette méthode
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
    
    /**
     * verifie la classe des composantn ou un lreleve peut être déposé
     * @param component
     * @return 
     */
    public static Component getDroppableComponent(Component component) {
        Component result = null;
        if (component instanceof TreeTopComponent) {            
            result = ((TreeTopComponent) component).getView();
        } else if (component instanceof GedcomTopComponent) {            
            result = ((GedcomTopComponent) component).getView();          
        } else if (component instanceof CygnusTopComponent) {
            result = component;
        } else if (component instanceof AriesTopComponent) {
            result = component;
        }
        return result;
    }
    
    /**
     * verifie la classe des composantn ou un lreleve peut être déposé
     * @param component
     * @return 
     */
    public static boolean verifyDroppableComponent(Component component) {
        boolean result = false;
        if (component instanceof TreeView) {            
            result = true;
        } else if (component instanceof EditView) {
            result = true;
        } else if (component instanceof CygnusTopComponent) {
            result = true;
        } else if (component instanceof AriesTopComponent) {
            result = true;
        }
        return result;
    }
    
    
    ///////////////////////////////////////////////////////////////////////////
    // Implementation de DropTargetListener
    ///////////////////////////////////////////////////////////////////////////
    
    @Override
    public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
        if (verifyDroppableComponent(dropTargetDragEvent.getDropTargetContext().getComponent())) {
            dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY);
        } else {
            dropTargetDragEvent.rejectDrag();
        }
    }

    @Override
    public void dragExit(DropTargetEvent dropTargetEvent) {
    }

    @Override
    public void dragOver(DropTargetDragEvent dropTargetDragEvent) {
        if (verifyDroppableComponent(dropTargetDragEvent.getDropTargetContext().getComponent())) {
            dropTargetDragEvent.acceptDrag(dropTargetDragEvent.getSourceActions());
        } else {
            dropTargetDragEvent.rejectDrag();
        }
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent) {
    }

    @Override
    public synchronized void drop(DropTargetDropEvent dropTargetDropEvent) {
        Entity entity = null;
        Gedcom gedcom = null;
        Component component = dropTargetDropEvent.getDropTargetContext().getComponent();

        if (component instanceof TreeView) {
            TreeView view = (TreeView) component;
            Point location = dropTargetDropEvent.getLocation();
            entity = view.getEntityAt(location, true);
            gedcom = view.getGedcom();
        } else if (component instanceof EditView) {
            EditView view = (EditView) component;
            entity = view.getEntity();
            gedcom = view.getGedcom();
        } else if (component instanceof AriesTopComponent) {
            AriesTopComponent ariesTopComponent = (AriesTopComponent)component;
            if (ariesTopComponent.getContext() != null ) {
                entity = ariesTopComponent.getContext().getEntity();
            }
            gedcom = ariesTopComponent.getGedcom();
        } else if (component instanceof CygnusTopComponent) {
            CygnusTopComponent cygnusTopComponent = (CygnusTopComponent) component;
            if (cygnusTopComponent.getContext() != null ) {
                entity = cygnusTopComponent.getContext().getEntity();
            }
            gedcom = cygnusTopComponent.getGedcom();
        }
        if (entity == null || entity instanceof Indi || entity instanceof Fam) {
            Transferable t = dropTargetDropEvent.getTransferable();
            // Check for types of data that we support
            if (t.isDataFlavorSupported(TransferableRecord.recordFlavor)) {
                dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY);
                try {
                    if (gedcom != null) {
                        TransferableRecord.TransferableData data = (TransferableRecord.TransferableData) t.getTransferData(TransferableRecord.recordFlavor);
                        // j'affiche la fenetre MergeDialog pour permettre à l'utilisateur
                        // de sélectionner les données à fusionner dans la vue cible
                        MergeDialog.show(component, gedcom, entity, data, true);
                        dropTargetDropEvent.dropComplete(true);
                    } else {
                        dropTargetDropEvent.dropComplete(false);
                    }
                } catch (UnsupportedFlavorException ex) {
                    Exceptions.printStackTrace(ex);
                    dropTargetDropEvent.dropComplete(false);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    dropTargetDropEvent.dropComplete(false);
                }
            }
        } else {
            dropTargetDropEvent.dropComplete(false);
        }
    }
}

