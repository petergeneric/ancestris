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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import swingx.dnd.ObjectTransferable;

/**
 * A tree that supports editing of its tree model via DnD.
 */
public class DnDTree extends JTree implements Autoscroll {

    /**
     * The default margin for autoscrolling.
     */
    private static final int DEFAULT_AUTOSCROLL_MARGIN = 12;
    
    /**
     * The margin for autoscrolling.
     */
    private int autoscrollMargin = DEFAULT_AUTOSCROLL_MARGIN;

    private DragGestureRecognizer dragGestureRecognizer; 
    private DragHandler dragHandler;
    private DropHandler dropHandler;
    
    /**
     * This is the default constructor
     */
    public DnDTree() {
        this(getDefaultTreeModel());        
    }
    
    /**
     * Create a DnDTree for the given model.
     */
    public DnDTree(TreeModel model) {
        super(model);
        
        // enable dragging on tree with a dummy transferHandler, otherwise
        // the default node selection would interfere with drag gestures 
        setDragEnabled(true);
        setTransferHandler(new TransferHandler() {
            public int getSourceActions(JComponent c) {
                return DnDConstants.ACTION_COPY;
            }
            
            public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
                if (hasDnDModel()) {
                    TreePath[] paths = getSelectionPaths();
                    if (paths != null && paths.length > 0) {
                        Arrays.sort(paths, dragHandler);
                        
                        Object[] nodes = new Object[paths.length];
                        for (int p = 0; p < paths.length; p++) {
                            if (paths[p].getPathCount() > 1) {
                                nodes[p] = paths[p].getLastPathComponent();
                            }
                        }
                        
                        Transferable transferable = getDnDModel().createTransferable(nodes);
                        
                        try {
                            getDnDModel().drag(transferable, action);
                            clip.setContents(transferable, null);
                        } catch (Exception ex) {
                            // drag failed, nothing we can do about it
                        }
                    }
                }
            }
            
            public boolean importData(JComponent comp, Transferable t) {
                TreePath[] paths = getSelectionPaths();
                if (paths != null && paths.length == 1 && paths[0].getPathCount() > 0) {
                    if (hasDnDModel()) {
                        try {
                            getDnDModel().drop(t, paths[0].getLastPathComponent(), 0, DnDTreeModel.MOVE);
                            return true;
                        } catch (Exception ex) {
                            // drop failed, nothing we can do about it
                        }
                    }
                }
                return false;
            }
        });
    
        // handle dragging with AWT drag & drop, since Swing DnD does not
        // support select with instant start of dragging
        dragGestureRecognizer = new DragSource().createDefaultDragGestureRecognizer(this,
                                                            DnDConstants.ACTION_MOVE,
                                                            getDragSourceListener());
        // enable dropping
        new DropTarget(this, getDropTargetListener());
    }

    /**
     * Create a treeModel to use as default.
     */
    protected static TreeModel getDefaultTreeModel() {
        return new DefaultDnDTreeModel((MutableTreeNode)JTree.getDefaultTreeModel().getRoot());     
    }
    
    public boolean hasDnDModel() {
        return getModel() instanceof DnDTreeModel;
    }
    
    public DnDTreeModel getDnDModel() {
        if (getModel() instanceof DnDTreeModel) {
            return (DnDTreeModel)getModel();
        }
        throw new IllegalStateException("no DnDTreeModel");
    }

    /**
     * Get the margin used for autoscrolling while DnD.
     * 
     * @return  margin for autoscrolling
     */
    public int getAutoscrollMargin() {
        return autoscrollMargin;
    }

    /**
     * Set the margin to be used for autoscrolling while DnD.
     * 
     * @param margin    margin for autoscrolling
     */
    public void setAutoscrollMargin(int margin) {
        autoscrollMargin = margin;
    }

    public void autoscroll(Point point) {
        Dimension dimension = getParent().getSize();

        int row = getClosestRowForLocation(point.x, point.y);
        if (row != -1) {
            if (getY() + point.y < dimension.height/2) {
                row = Math.max(0, row - 1);
            } else {
                row = Math.min(row + 1, getRowCount() - 1);
            }
            scrollRowToVisible(row);
        }        
    }

    public Insets getAutoscrollInsets() {
        Rectangle bounds = getParent().getBounds();

        return new Insets(
            bounds.y - getY() + autoscrollMargin,
            bounds.x - getX() + autoscrollMargin,
            getHeight() - bounds.height - bounds.y + getY() + autoscrollMargin,
            getWidth()  - bounds.width  - bounds.x + getX() + autoscrollMargin);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        getDropTargetListener().paint(g);
    }

    protected DragHandler getDragSourceListener() {
        if (dragHandler == null) {
            dragHandler = new DragHandler();
        }
        return dragHandler;
    }

    protected DropHandler getDropTargetListener() {
        if (dropHandler == null) {
            dropHandler = new DropHandler();
        }
        return dropHandler;
    }

    /**
     * The handler of drag
     * <ul>
     *   <li>enables drag dependent on the currently selected nodes,</li>
     *   <li>starts a drag in response to a drag gestures and</li>
     *   <li>removes dragged nodes on successful move.</li>
     * </ul>
     */    
    private class DragHandler extends DragSourceAdapter implements DragGestureListener,
                                                                   Comparator {
        /**
         * The transferable to be dragged.
         */
        private Transferable transferable;
        
        /**
         * Start a drag with all currently selected nodes if one of them
         * is hit by the mouse event that originates the drag.
         */
        public void dragGestureRecognized(DragGestureEvent dge) {
            
            if (hasDnDModel()) {
                TreePath[] paths = getSelectionPaths();
                if (paths != null && paths.length > 0) {
                    Arrays.sort(paths, this);
                    
                    boolean selectionHit = false;
                    Object[] nodes = new Object[paths.length];
                    for (int p = 0; p < paths.length; p++) {
                      // [NM] this was 
                      //    if (paths[p].getPathCount() > 1) {
                      // if the user chooses the root node as well then there will be a one element path though
                      // leading to a Node array with first element null. I think any path over length 0 is ok (so
                      // also the root)
                        if (paths[p].getPathCount() > 0) {
                            nodes[p] = paths[p].getLastPathComponent();
        
                            Rectangle rect = getPathBounds(paths[p]);
                            if (rect.contains(dge.getDragOrigin())) {
                                selectionHit = true;
                            }
                        }
                    }
                    
                    if (selectionHit) {
                        transferable = getDnDModel().createTransferable(nodes);

                        dragGestureRecognizer.setSourceActions(getDnDModel().getDragActions(transferable));

                        dge.startDrag(null, 
                                      createDragImage(paths),
                                      new Point(),
                                      transferable, this);
                    }
                }
            }
        }

        /**
         * Compare two treePaths because the selectionModel doesn't keep them
         * necessarily ordered - lower rows come first.
         */
        public int compare(Object object1, Object object2) {
            TreePath path1 = (TreePath)object1;
            TreePath path2 = (TreePath)object2;
            
            int row1 = getRowForPath(path1); 
            int row2 = getRowForPath(path2);
            if (row1 < row2) {
                return -1;
            } else if (row2 < row1) {
                return 1;
            } else {
                return 0;
            }
        }

        public void dragDropEnd(DragSourceDropEvent dsde) {
            
            if (dsde.getDropSuccess()) {
                try {
                    getDnDModel().drag(transferable, dsde.getDropAction());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    // drag failed, nothing we can do about it
                }
            }
            
            getDnDModel().releaseTransferable(transferable);
            transferable = null;
            
            dragGestureRecognizer.setSourceActions(DnDConstants.ACTION_MOVE);
        }
    }

    /**
     * The handler of drop
     * <ul>
     *   <li>expandes nodes when hovered over them,</li>
     *   <li>accepts or rejects drops,</li>
     *   <li>draws a drop index indicator</li>
     *   <li>surveilles removal of nodes and</li>
     *   <li>optionally performs a drop.</li>
     * </ul>
     */    
    private class DropHandler extends DropTargetAdapter implements ActionListener, TreeModelListener {
            
        private Timer timer;
        
        private TreePath   parentPath;
        private int        childIndex = 0;
        private Rectangle  indicator;
        private List       insertions = new ArrayList();

        public DropHandler() {
            timer = new Timer(1500, this);
            timer.setRepeats(false);            
        }

        public void dropActionChanged(DropTargetDragEvent dtde) {
            dragOver(dtde);
        }

        public void dragOver(DropTargetDragEvent dtde) {
            int     action   = dtde.getDropAction();
            boolean accepted = false;
            
            if (hasDnDModel()) {
                update(dtde.getLocation());

                if (parentPath != null) {
                    Transferable transferable = ObjectTransferable.getTigerTransferable(dtde);
                    if (transferable == null) {
                        accepted = true;
                    } else {
                        Object parent = parentPath.getLastPathComponent();
                        accepted = (getDnDModel().getDropActions(transferable, parent, childIndex) & action) != 0;
                    }                        
                }
            }

            if (accepted) {
                dtde.acceptDrag(action);            
            } else {
                dtde.rejectDrag();
            }
        }

        public void dragExit(DropTargetEvent dte) {
            clear();
        }

        public void drop(DropTargetDropEvent dtde) {
            int     action   = dtde.getDropAction();
            boolean complete = false;

            getModel().addTreeModelListener(this);
            try {
                Object parent = null;
                if (hasDnDModel() && parentPath != null) {
                    dtde.acceptDrop(action);
                    
                    parent = parentPath.getLastPathComponent();
                    Transferable transferable = dtde.getTransferable();

                    if ((getDnDModel().getDropActions(transferable, parent, childIndex) & action) != 0) {

                        getDnDModel().drop(transferable, parent, childIndex, action);
                        
                        complete = true;
                    }    
                }
    
                dtde.dropComplete(complete);
                
                if (!insertions.isEmpty()&&parent!=null) {
                    getSelectionModel().clearSelection();
                    for (int i = 0; i < insertions.size(); i++) {
                      TreePath path = (TreePath)insertions.get(i);
                      if (getModel().getIndexOfChild(parent, path.getLastPathComponent())>=0)
                        getSelectionModel().addSelectionPath(path);
                    }
                }
            } catch (Exception ex) {
                // drop failed, nothing we can do about it
            }
            getModel().removeTreeModelListener(this);

            clear();
        }

        private void update(Point point) {
            TreePath oldParentPath = parentPath;
            
            TreePath path = getClosestPathForLocation(point.x, point.y);
            if (path == null) {
                parentPath = null;
                childIndex = -1;
                indicator  = null;
            } else if (path.getPathCount() == 1) {
                parentPath = path;
                childIndex = 0;
                indicator  = null;
            } else {
                parentPath = path.getParentPath();
                childIndex = getModel().getIndexOfChild(parentPath.getLastPathComponent(), path.getLastPathComponent());
                indicator  = getPathBounds(path);
                                
                if (getModel().isLeaf(path.getLastPathComponent())  ||
                    (point.y < indicator.y + indicator.height*1/4)  ||
                    (point.y > indicator.y + indicator.height*3/4 && !isExpanded(path)) ) {

                    if (point.y > indicator.y + indicator.height/2) {
                        indicator.y = indicator.y + indicator.height;
                        childIndex++; 
                    }
                    indicator.width = getWidth() - indicator.x - getInsets().right;
                    indicator.y      -= 1;
                    indicator.height  = 2;
                } else {
                    parentPath = path; 
                    indicator  = null;
                    childIndex = 0;
                }
            }

            repaint();

            if (parentPath == null) {
                if (timer.isRunning()) {
                    timer.stop();
                }
            } else {
                if (!parentPath.equals(oldParentPath)) {
                    timer.start();
                }
            }            
        }
                
        private void clear() {
            if (timer.isRunning()) {
                timer.stop();
            }
        
            parentPath   = null;
            childIndex   = -1;
            indicator    = null;
            insertions.clear();

            repaint();
        }
        
        public TreePath getParentPath() {
            return parentPath;
        }
        
        public void paint(Graphics g) {
            if (indicator != null) {
                paintIndicator(g, indicator);
            }
        }

        private void paintIndicator(Graphics g, Rectangle rect) {
            g.setColor(getForeground());
            
            g.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);

            g.drawLine(rect.x, rect.y - 2              , rect.x + 1, rect.y - 2);
            g.drawLine(rect.x, rect.y - 1              , rect.x + 2, rect.y - 1);
            g.drawLine(rect.x, rect.y + rect.height + 0, rect.x + 2, rect.y + rect.height + 0);
            g.drawLine(rect.x, rect.y + rect.height + 1, rect.x + 1, rect.y + rect.height + 1);
            
            g.drawLine(rect.x + rect.width - 2, rect.y - 2              , rect.x + rect.width - 1, rect.y - 2);
            g.drawLine(rect.x + rect.width - 3, rect.y - 1              , rect.x + rect.width - 1, rect.y - 1);
            g.drawLine(rect.x + rect.width - 3, rect.y + rect.height + 0, rect.x + rect.width - 1, rect.y + rect.height + 0);
            g.drawLine(rect.x + rect.width - 2, rect.y + rect.height + 1, rect.x + rect.width - 1, rect.y + rect.height + 1);
        }
        
        public void actionPerformed(ActionEvent e) {
            if (parentPath != null) {
                expandPath(parentPath);
            }
        }
        
        public void treeNodesChanged(TreeModelEvent e) { }
        
        public void treeNodesInserted(TreeModelEvent e) {
            Object[] children = e.getChildren();
            for (int c = 0; c < children.length; c++) {
                insertions.add(e.getTreePath().pathByAddingChild(children[c]));
            }
        }
        
        public void treeNodesRemoved(TreeModelEvent e) {
            insertions.clear();
        }
        
        public void treeStructureChanged(TreeModelEvent e) {
            insertions.clear();
            insertions.add(e.getTreePath());
        }
    }

    /**
     * Create an image representation for the selection about to be
     * dragged.
     * <br>
     * Drag images are currently not supported under Windows. On
     * Mac OS X the whole tree is used as drag image. This is why
     * this default implementation creates a dummy image.
     * <br>
     * Subclasses may subclass this method for fancy image creation.
     *  
     * @param selectionPaths    paths of selection to drag
     * @return                  image representation
     */
    protected Image createDragImage(TreePath[] selectionPaths) {
        return createImage(1, 1);
    }
}