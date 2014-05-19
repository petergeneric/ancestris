package ancestris.modules.releve.dnd;

import ancestris.modules.releve.dnd.TransferableRecord.TransferableData;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.tree.TreeView;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import org.openide.util.Exceptions;

/**
 * Cette classe contient les methode pour permmettre a un arbre dynamique d'etre
 * destinataire d'un DnD.
 *
 * la fonction DragAndDrop ne peut fonctionner qui si on ajoute la méthode
 * TreeView.getEntityAt(location) dans la classe genj.tree.TreeView
 * et si on décommenter les trois appels à cette methode dans le code ci dessous.
 * @author Michel
 */

/**
  * genj.tree.TreeView.getEntityAt(Point entityPos)
  * retreive entity at given cooodinates
  * @param entityPos  Point in TreeView coordinates
  * @return
  */
/*
  public Entity getEntityAt(Point entityPos){
      if (model == null) return null;

      // je recupere la position de Content / Treeview
      ViewPortAdapter va = (ViewPortAdapter) content.getParent();
      JViewport vp = (JViewport) va.getParent();
      Point viewPosition = vp.getViewPosition();
      // je recupere la position décalée de "content" due au centrage
      // qui n'est pas nul quand "content" est plus petit que viewport
      Point contentShift = content.getLocation();

      // je change de repere TreeView => Content
      Point entityContentPos = new Point();
      entityContentPos.x = entityPos.x + viewPosition.x - contentShift.x;
      entityContentPos.y = entityPos.y + viewPosition.y - contentShift.y;
      // je change de repere Content => model
      Point modelPos = view2model(entityContentPos);
      // je recherche l'entité a cette position dans le modele
      return model.getEntityAt(modelPos.x, modelPos.y);
  }

 */

public class TreeViewDropTarget {

    TreeView treeView;

    /**
     * cree un objet DropTarget associé au TreeView
     * @param treeView
     * @return
     */
    public DropTarget createDropTarget (TreeView treeView ) {
        this.treeView = treeView;
        DropTarget dropTarget = new DropTarget(treeView, new TreeViewDropTargetListener());
        return dropTarget;
    }

    /**
     * cette classe décrit le comportement du drop d'un relevé dans un TreeView
     */
    private class TreeViewDropTargetListener implements DropTargetListener {

        @Override
        public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
            Point location = dropTargetDragEvent.getLocation();

            Entity entity = null;
            entity = treeView.getEntityAt(location);
            if ( entity == null || entity instanceof Indi || entity instanceof Fam) {
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
            //System.out.println("dragOver "+dropTargetDragEvent.getSource().getClass().getName());
            Point location = dropTargetDragEvent.getLocation();
            Entity entity = null;
            entity = treeView.getEntityAt(location);
            if ( entity == null || entity instanceof Indi || entity instanceof Fam) {
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
            Point location = dropTargetDropEvent.getLocation();
            Entity entity = treeView.getEntityAt(location);
            if (entity == null || entity instanceof Indi || entity instanceof Fam) {
                Transferable t = dropTargetDropEvent.getTransferable();

                // Check for types of data that we support
                if (t.isDataFlavorSupported(TransferableRecord.recordFlavor)) {
                    // If it was a color, accept it, and use it as the background color
                    dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY);
                    try {
                        if (treeView.getGedcom() != null) {
                            TransferableRecord.TransferableData data = (TransferableData) t.getTransferData(TransferableRecord.recordFlavor);
                            MergeDialog.show(treeView, treeView.getGedcom(), entity, data.mergeRecord, true);
                            dropTargetDropEvent.dropComplete(true);
                        } else {
                            dropTargetDropEvent.dropComplete(false);
                        }
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                        dropTargetDropEvent.dropComplete(false);
                    }
                }

            } else {
                dropTargetDropEvent.dropComplete(false);
            }
        }
    }
}
