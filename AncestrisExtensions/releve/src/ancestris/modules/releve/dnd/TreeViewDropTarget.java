package ancestris.modules.releve.dnd;

import ancestris.modules.releve.dnd.TransferableRecord.TransferableData;
import genj.gedcom.Entity;
import genj.tree.TreeView;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import javax.swing.SwingUtilities;

/**
 * Cette classe contient les methode pour permmettre au un arbre dynamique d'etre
 * destinataire d'un DnD
 * @author Michel
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
     * cette classe decrit le comportement du drop d'un relvé dans un TreeView
     */
    private class TreeViewDropTargetListener implements DropTargetListener {

        @Override
        public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
            Point location = dropTargetDragEvent.getLocation();

            //TODO créer la méthode treeView.getEntityAt(location)
            //Entity entity = treeView.getEntityAt(location);
            Entity entity = null;
            if (entity != null) {
                dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY);
                //System.out.println("dragOver "+ entity.getId() + " " + entity.toString());
            } else {
                dropTargetDragEvent.rejectDrag();
                //System.out.println("dragOver "+ location.toString());
            }

        }

        @Override
        public void dragExit(DropTargetEvent dropTargetEvent) {
        }

        @Override
        public void dragOver(DropTargetDragEvent dropTargetDragEvent) {
            //System.out.println("dragOver "+dropTargetDragEvent.getSource().getClass().getName());
            Point location = dropTargetDragEvent.getLocation();
            //TODO créer la méthode treeView.getEntityAt(location)
            //Entity entity = treeView.getEntityAt(location);
            Entity entity = null;
            if (entity != null) {
                dropTargetDragEvent.acceptDrag(dropTargetDragEvent.getSourceActions());
                //System.out.println("dragOver "+ entity.getId() + " " + entity.toString());
            } else {
                dropTargetDragEvent.rejectDrag();
                //System.out.println("dragOver "+ location.toString());
            }
        }

        @Override
		public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent) {
		}

        @Override
		public synchronized void drop(DropTargetDropEvent dropTargetDropEvent) {
			Point location = dropTargetDropEvent.getLocation();
            //TODO créer la méthode treeView.getEntityAt(location)
            //Entity entity = treeView.getEntityAt(location);
            Entity entity = null;
            if ( entity != null) {
                Transferable t = dropTargetDropEvent.getTransferable();

                // Check for types of data that we support
                if (t.isDataFlavorSupported(TransferableRecord.recordFlavor)) {
                    // If it was a color, accept it, and use it as the background color
                    dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY);
                    try {
                        TransferableRecord.TransferableData data = (TransferableData) t.getTransferData(TransferableRecord.recordFlavor);
                        System.out.println("drop "+ data.record.getEventDateString() + " to " + entity.toString() );
                        dropTargetDropEvent.dropComplete(true);
                        SwingUtilities.convertPointToScreen(location, treeView);
                        MergeDialog.show(location, entity, data.record);
                    } catch (Exception ex) {
                        dropTargetDropEvent.dropComplete(false);
                    }
                }

            } else {
                dropTargetDropEvent.dropComplete(false);
                System.out.println("drop "+ location.toString());
            }
        }
	}



}
