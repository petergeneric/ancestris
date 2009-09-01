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
import java.awt.dnd.DnDConstants;
import java.io.IOException;

import javax.swing.tree.*;

/**
 * A tree model that offers altering through DnD.
 */
public interface DnDTreeModel extends TreeModel {
    
    /**
     * Constant indicating a <code>COPY</code> DnD action.
     */
    public static final int COPY = DnDConstants.ACTION_COPY;

    /**
     * Constant indicating a <code>MOVE</code> DnD action.
     */
    public static final int MOVE = DnDConstants.ACTION_MOVE;

    /**
     * Constant indicating a <code>LINK</code> DnD action.
     */
    public static final int LINK = DnDConstants.ACTION_LINK;

    /**
     * Create a transferable for the given children.
     * 
     * @param children  children to get transferable for
     * @return          transferable
     * @see #releaseTransferable(Transferable)
     */
    public Transferable createTransferable(Object[] children);

    /**
     * Get the actions for the given transferable supported for a drag.
     * 
     * @param transferable      transferable to get actions for
     * @return                  actions, {@link #COPY}, {@link #MOVE} and {@link #LINK}
     */
    public int getDragActions(Transferable transferable);
    
    /**
     * Get the actions for the given transferable supported for a drop.
     * 
     * @param transferable      transferable to get actions for
     * @return                  actions, {@link #COPY}, {@link #MOVE} and {@link #LINK}
     */
    public int getDropActions(Transferable transferable, Object parent, int index);
    
    /**
     * Perform a drag of the given transferable.
     * 
     * @param transferable      transferable that is dragged
     * @param action            action of drag
     * @throws UnsupportedFlavorException if action is not supported
     */
    public void drag(Transferable transferable, int action) throws UnsupportedFlavorException, IOException;
    
    /**
     * Perform a drop of the given transferable.
     * 
     * @param transferable      transferable that is dropped
     * @param action            action of drag
     * @throws UnsupportedFlavorException if action is not supported
     */
    public void drop(Transferable transferable, Object parent, int index, int action) throws UnsupportedFlavorException, IOException;
    
    /**
     * Release a transferable previously create in {@link #createTransferable(Object[])}.
     * 
     * @param transferable  transferable to release
     */
    public void releaseTransferable(Transferable transferable);
}