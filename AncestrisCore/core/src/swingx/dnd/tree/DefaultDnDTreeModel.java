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

import javax.swing.tree.*;

import swingx.dnd.ObjectTransferable;

public class DefaultDnDTreeModel extends DefaultTreeModel implements DnDTreeModel {

    private static DefaultDnDTreeModel currentDrag;
    
    private static boolean dragYielded;
    
    public DefaultDnDTreeModel(TreeNode root) {
        super(root);
    }

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
            Object[] nodes = getNodes(transferable);
            
            for (int c = 0; c < nodes.length; c++) {
                if (((MutableTreeNode)nodes[c]).getParent() == null) {
                    return 0;
                }            
            }
            return MOVE;
        } catch (Exception ex) {
            // no drag allowed
        }
        return 0;
    }
    
    public int getDropActions(Transferable transferable, Object parent, int index) {
        try {
            Object[] nodes = (Object[])ObjectTransferable.getObject(transferable);

            for (int c = 0; c < nodes.length; c++) {
                if (isNodeAncestor((MutableTreeNode)nodes[c], (MutableTreeNode)parent)) {
                    return 0;
                }            
            }
            return MOVE;
        } catch (Exception ex) {
            // no drop allowed
        }
        return 0;
    }
    
    public void drag(Transferable transferable, int action) throws UnsupportedFlavorException, IOException {
        if (!dragYielded) {
            if (action == MOVE) {
                Object[] nodes = getNodes(transferable);

                for (int n = nodes.length - 1; n >= 0; n--) {
                    removeNodeFromParent((MutableTreeNode)nodes[n]);
                }
            }
        }
        currentDrag = null;
    }
    
    public void drop(Transferable transferable, Object parent, int index, int action) throws UnsupportedFlavorException, IOException {
        if (action != MOVE) {
            throw new IllegalArgumentException("action not supported: " + action);
        }

        if (currentDrag != null) {
            index = yieldDrag(transferable, parent, index, action);
        }        
       
        Object[] nodes = getNodes(transferable);
        for (int n = 0; n < nodes.length; n++) {
            insertNodeInto((MutableTreeNode)nodes[n], (MutableTreeNode)parent, index + n);
        }        
    }
    
    protected int yieldDrag(Transferable transferable, Object parent, int index, int action) throws UnsupportedFlavorException, IOException {
        Object[] nodes = getNodes(transferable);
        for (int n = 0; n < nodes.length; n++) {
            TreeNode node = (TreeNode)nodes[n];
            if (node.getParent() == parent && node.getParent().getIndex(node) < index) {
                index--;
            }
        }
        currentDrag.drag(transferable, action);
        
        dragYielded = true;
        
        return index;
    }
    
    public void releaseTransferable(Transferable transferable) {
        currentDrag = null;
        dragYielded = false;
    }

    /**
     * Utility method to test if a given candidate is an ancestorship of another
     * node.
     * 
     * @param ancestoreCandidate    ancestor candidate
     * @param node                  node
     * @return                      <code>true</code> if candidate is ancestor
     *                              of given node
     */
    private static boolean isNodeAncestor(TreeNode ancestoreCandidate, TreeNode node) {
        do {
            if (ancestoreCandidate == node) {
                return true;
            }        
        } while((node = node.getParent()) != null);

        return false;
    }
}