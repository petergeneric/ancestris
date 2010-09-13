/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.common;

import genj.gedcom.Gedcom;
import genj.gedcom.Grammar;
import genj.gedcom.TagPath;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Component that allows to look at a Tree of TagPaths
 */
public class PathTreeWidget extends JScrollPane {

  /** list listeners */
  private List<Listener> listeners = new ArrayList<Listener>();
  
  /** grammar */
  private Grammar grammar = Grammar.V55;
  
  /** the tree we use for display */
  private JTree   tree;
  
  /** our tree's model */
  private Model model = new Model();

  /**
   * Constructor
   */
  public PathTreeWidget() {

    Callback callback = new Callback();
    
    // Prepare tree
    tree = new JTree(model);
    tree.setShowsRootHandles(false);
    tree.setRootVisible(false);
    tree.setCellRenderer(new Renderer());
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.addMouseListener(callback);
    tree.addTreeWillExpandListener(callback);

    // Do a little bit of layouting
    setMinimumSize(new Dimension(160, 160));
    setPreferredSize(new Dimension(160, 160));
    getViewport().setView(tree);

    // Done
  }
  
  public void setGrammar(Grammar grammar) {
    this.grammar = grammar;
  }

  /**
   * Adds another listener to this
   */
  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  /**
   * Signals a change in a node to the listeners
   * @param path the TreePath to the selected node
   */
  private void fireSelectionChanged(TagPath path, boolean on) {
    // Go through listeners
    Listener[] ls = (Listener[])listeners.toArray(new Listener[listeners.size()]);
    for (int l=0;l<ls.length;l++)
      ls[l].handleSelection(path,on);
    // done
  }
  
  /**
   * Removes one of ther listener to this
   */
  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  /**
   * Sets the TagPaths to choose from
   */
  public void setPaths(TagPath[] paths, TagPath[] selection) {
    model.setPaths(paths, selection);

    // ensure all first level paths are expanded
    for (TagPath path : paths) {
      Object[] treepath = new Object[2];
      treepath[0] = model;
      treepath[1] = new TagPath(path.get(0));
      tree.expandPath(new TreePath(treepath));
    }
    
    // ensure selected nodes are expanded
    for (TagPath path : selection) {
      Object[] treepath = new Object[path.length()];
      treepath[0] = model;
      for (int i=1;i<path.length();i++) {
        treepath[i] = new TagPath(path, i);
      }
      tree.expandPath(new TreePath(treepath));
    }
    
  }

  public void setSelected(TagPath path, boolean set) {
    model.setSelected(path, set);
  }
  
  /**
   * Returns the selected TagPaths
   */
  public TagPath[] getSelection() {
    return (TagPath[])model.getSelection().toArray(new TagPath[0]);
  }

  /**
   * Our Renderer
   */
  private class Renderer extends DefaultTreeCellRenderer {

    /** members */
    private JPanel        panel = new JPanel();
    private JCheckBox     checkbox = new JCheckBox();

    /** constructor */
    private Renderer() {
      panel.setLayout(new BorderLayout());
      panel.add(checkbox,"West");
      panel.add(this    ,"Center");
      checkbox.setOpaque(false);
      panel.setOpaque(false);
    }
    
    /** callback for component that's responsible for rendering */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      // let super do its work
      super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
      // change for tag path
      if (value instanceof TagPath) {
        TagPath path = (TagPath)value; 
        setText( path.getLast() + " ("+Gedcom.getName(path.getLast())+")");
        setIcon( grammar.getMeta(path).getImage() );
        checkbox.setSelected(model.getSelection().contains(value));
        panel.invalidate(); // make sure no preferred side is cached
      }      
      // done
      return panel;
    }

  } //Renderer

  /**
   * A model for the paths mapped to a tree
   */
  private class Model implements TreeModel {
    
    /** the tag-paths to choose from */
    private Set<TagPath> paths = new HashSet<TagPath>();

    /** the selection */
    private Set<TagPath> selection = new HashSet<TagPath>();
    
    /** map a path to its children */
    private Map<TagPath,TagPath[]> path2childen = new HashMap<TagPath,TagPath[]>(); 
    
    /** listeners */
    private List<TreeModelListener> tmlisteners = new CopyOnWriteArrayList<TreeModelListener>();
  
    /**
     * Sets the TagPaths to choose from
     */
    public void setPaths(TagPath[] ps, TagPath[] ss) {

      // remember selection
      selection = new HashSet<TagPath>(Arrays.asList(ss));
      
      // keep paths
      paths = new HashSet<TagPath>();
      paths.addAll(Arrays.asList(ps));
      paths.addAll(Arrays.asList(ss));
      
      // notify
      TreeModelEvent e = new TreeModelEvent(this, new Object[]{ this });
      for (TreeModelListener listener : tmlisteners)
        listener.treeStructureChanged(e);
      
      // done
    }
    
    private void setSelected(TagPath path, boolean set) {

      if (!paths.contains(path))
        throw new IllegalArgumentException("path not a choice");
      
      // in/out
      if (set)
        selection.add(path);
      else
        selection.remove(path);
      
      // notify
      TreeModelEvent e = new TreeModelEvent(this, new Object[]{ this });
      for (TreeModelListener listener : tmlisteners) 
        listener.treeNodesChanged(e);
      
      fireSelectionChanged(path, set);
      
    }
    
    /**
     * Toggle selection
     */
    private void toggleSelection(TagPath path) {
      
      if (selection.contains(path))
        setSelected(path, false);
      else
        setSelected(path, true);
    }
    
    /**
     * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
     */
    public void addTreeModelListener(TreeModelListener l) {
      tmlisteners.add(l);
    }
  
    /**
     * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
     */
    public void removeTreeModelListener(TreeModelListener l) {
      tmlisteners.remove(l);
    }

    /**
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    public Object getChild(Object parent, int index) {
      return getChildren(parent)[index];
    }
  
    /**
     * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
     */
    public int getChildCount(Object parent) {
      return getChildren(parent).length;
    }
    
    /**
     * Return the children of a node 
     */
    private TagPath[] getChildren(Object node) {
      // lazy
      TagPath[] result = path2childen.get(node);
      if (result==null) {
        result = node==this ? getChildrenOfRoot() : getChildrenOfNode((TagPath)node);
      }
      // done
      return result;
    }
    
    /**
     * Return the children of a path
     */
    private TagPath[] getChildrenOfNode(TagPath path) {
      // all paths starting with path
      List<TagPath> children = new ArrayList<TagPath>(8);
      for (TagPath p : paths) {
        if (p.length()>path.length()&&p.startsWith(path)) 
          add(new TagPath(p, path.length()+1), children);
      }
      for (TagPath sel : selection) {
        if (sel.length()>path.length()&&sel.startsWith(path)) 
          add(new TagPath(sel, path.length()+1), children);
      }
      // done
      return TagPath.toArray(children);
    }
    
    /** 
     * Return the children for root
     */
    private TagPath[] getChildrenOfRoot() {
      // all paths' first tag 
      List<TagPath> children = new ArrayList<TagPath>(8);
      for (TagPath path : paths) 
        add(new TagPath(path, 1), children);
      for (TagPath path : selection) 
        add(new TagPath(path, 1), children);
      // done
      return TagPath.toArray(children);
    }
    
    private void add(TagPath path, List<TagPath> list) {
      if (!list.contains(path))
        list.add(path);
    }
    
    /**
     * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
     */
    public int getIndexOfChild(Object parent, Object child) {
      return 0;
    }
  
    /**
     * @see javax.swing.tree.TreeModel#getRoot()
     */
    public Object getRoot() {
      return this;
    }
  
    /**
     * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
     */
    public boolean isLeaf(Object node) {
      return getChildren(node).length==0;
    }
  
    /**
     * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
      //ignored
    }
  
    /**
     * Returns the selected TagPaths
     */
    public Collection<TagPath> getSelection() {
      return selection;
    }

  } //Model

  /**
   * Notification Interface for selections on the TagPathTree component
   */
  public interface Listener {

    /**
     * Notification in case a selection for a TagPath has changed
     */
    public void handleSelection(TagPath path, boolean on);

  } //Listener

  /**
   * Callback for mouse click selects and expand ops
   */
  private class Callback extends MouseAdapter implements TreeWillExpandListener {
    /** 
     * callback for mouse click 
     */
    public void mousePressed(MouseEvent me) {
      // Check wether some valid path has been clicked on
      TreePath path = tree.getPathForLocation(me.getX(),me.getY());
      if (path==null)
        return;
      // last segment is the path we're interested in
      model.toggleSelection((TagPath)path.getLastPathComponent());
      // done
    }

    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
      if (event.getPath().getPathCount()<3)
        throw new ExpandVetoException(event);
    }

    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
    }
  } //Selector
  
} //TagPathTree
