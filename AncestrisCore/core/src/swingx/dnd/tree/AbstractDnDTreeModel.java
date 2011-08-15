/*
 * swingx - Swing eXtensions
 * Copyright (C) 2004 Sven Meier
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package swingx.dnd.tree;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import swingx.dnd.ObjectTransferable;
import swingx.tree.AbstractTreeModel;

/**
 * Abstract base class for DnD tree models.
 */
public abstract class AbstractDnDTreeModel extends AbstractTreeModel implements DnDTreeModel {

    private static AbstractDnDTreeModel currentDrag;
    
    private static boolean dragYielded;

    public Transferable createTransferable(Object[] nodes) {
        currentDrag = this;
        dragYielded = false;

        return new ObjectTransferable(nodes);
    }

    protected Object[] getNodes(Transferable transferable) throws UnsupportedFlavorException, IOException {
        return (Object[])ObjectTransferable.getObject(transferable);
    }
    
    public int getDragActions(Transferable transferable) {
        try {
            return getDragActions(getNodes(transferable));
        } catch (Exception ex) {
            // no drag allowed
        }
        return 0;
    }
    
    protected int getDragActions(Object[] nodes) {
        for (int c = 0; c < nodes.length; c++) {
            if (getParent(nodes[c]) == null) {
                return 0;
            }            
        }
        return MOVE;
    }

    public int getDropActions(Transferable transferable, Object parent, int index) {
        try {
            return getDropActions((Object[])ObjectTransferable.getObject(transferable), parent, index);
        } catch (Exception ex) {
            // no drop allowed
        }
        return 0;
    }

    protected int getDropActions(Object[] nodes, Object parent, int index) {
        for (int c = 0; c < nodes.length; c++) {
            if (isNodeAncestor(nodes[c], parent)) {
                return 0;
            }            
        }
        return MOVE;
    }
    
    public void drag(Transferable transferable, int action) throws UnsupportedFlavorException, IOException {
        if (!dragYielded) {
            if (action == MOVE) {
                Object[] nodes = getNodes(transferable);
    
                for (int n = nodes.length - 1; n >= 0; n--) {
                    removeNodeFromParent(nodes[n]);
                }
            }
        }
    }

    protected abstract void removeNodeFromParent(Object node);

    public void drop(Transferable transferable, Object parent, int index, int action) throws UnsupportedFlavorException, IOException {
        if (action != MOVE) {
            throw new IllegalArgumentException("action not supported: " + action);
        }
        
        if (currentDrag != null) {
            index = yieldDrag(transferable, parent, index, action);
        }
        
        Object[] nodes = getNodes(transferable);        
        for (int n = 0; n < nodes.length; n++) {
            insertNodeInto(nodes[n], parent, index + n);
        }
    }

    protected int yieldDrag(Transferable transferable, Object parent, int index, int action) throws UnsupportedFlavorException, IOException {
        Object[] nodes = getNodes(transferable);
        for (int n = 0; n < nodes.length; n++) {
            if (getParent(nodes[n]) == parent && getIndexOfChild(parent, nodes[n]) < index) {
                index--;
            }
        }
        currentDrag.drag(transferable, action);
        
        dragYielded = true;
        
        return index;
    }
    
    protected abstract void insertNodeInto(Object node, Object parent, int index);

    public void releaseTransferable(Transferable transferable) {
        currentDrag = null;
        dragYielded = false;
    }

    private boolean isNodeAncestor(Object ancestoreCandidate, Object node) {
        do {
            if (ancestoreCandidate == node) {
                return true;
            }        
        } while((node = getParent(node)) != null);

        return false;
    }
}