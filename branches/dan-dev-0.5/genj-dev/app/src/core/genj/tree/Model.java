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
package genj.tree;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import gj.layout.LayoutException;
import gj.layout.tree.TreeLayout;
import gj.model.Node;

import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Model of our tree
 */
/*package*/ class Model {
  
  /** our gedcom callback */
  private Callback callback = new Callback();
  
  /** listeners */
  private List<ModelListener> listeners = new CopyOnWriteArrayList<ModelListener>();

  /** arcs */
  private Collection<TreeArc> arcs = new ArrayList<TreeArc>(100);

  /** nodes */
  private Map<Entity,TreeNode> entities2nodes = new HashMap<Entity, TreeNode>(100);
  private Collection<TreeNode> nodes = new ArrayList<TreeNode>(100);

  /** bounds */
  private Rectangle bounds = new Rectangle();
  
  /** caching */
  private GridCache cache = null;
  
  /** whether we're vertical or not */
  private boolean isVertical = true;
  
  /** whether we model families or not */
  private boolean isFamilies = true;
  
  /** whether we bend arcs or not */
  private boolean isBendArcs = true;
  
  /** whether we show marriage symbols */
  private boolean isMarrSymbols = true;
  
  /** whether we show toggles for un/folding */
  private boolean isFoldSymbols = true;
  
  /** individuals whose ancestors we're not interested in */
  private Set<String> hideAncestors = new HashSet<String>();

  /** individuals whose descendants we're not interested in */
  private Set<String> hideDescendants = new HashSet<String>();
  
  /** individuals' family */
  private Map<Indi,Fam> indi2fam = new HashMap<Indi, Fam>();

  /** the root we've used */
  private Entity root;

  /** metrics */
  private TreeMetrics metrics = new TreeMetrics( 60, 30, 30, 15, 10 );
  
  /** bookmarks */
  private LinkedList<Bookmark> bookmarks = new LinkedList<Bookmark>();
  
  /**
   * Constructor
   */
  public Model() {
  }
  
  /**
   * Accessor - current root
   */
  public void setRoot(Entity entity) {
    
    // no change?
    if (root==entity) 
      return;
    
    // detach
    if (root!=null) {
      root.getGedcom().removeGedcomListener(callback);
      root = null;
    }
    
    // attach
    if (entity instanceof Indi ||entity instanceof Fam) {
      root = entity;
      root.getGedcom().addGedcomListener(callback);
    }
    
    // start fresh
    bookmarks.clear();
    
    // parse the current information
    update();
    
    // done
  }

  /**
   * Accessor - current root
   */
  public Entity getRoot() {
    return root;
  }
    
  /**
   * Accessor - wether we're vertical
   */
  public boolean isVertical() {
    return isVertical;
  }
  
  /**
   * Accessor - wether we're vertical
   */
  public void setVertical(boolean set) {
    if (isVertical==set) return;
    isVertical = set;
    update();
  }
  
  /**
   * Accessor - wether we bend arcs or not
   */
  public boolean isBendArcs() {
    return isBendArcs;
  }
  
  /**
   * Accessor - wether we bend arcs or not
   */
  public void setBendArcs(boolean set) {
    if (isBendArcs==set) return;
    isBendArcs = set;
    update();
  }
  
  /**
   * Accessor - whether we model families
   */
  public boolean isFamilies() {
    return isFamilies;
  } 
  
  /**
   * Accessor - whether we model families
   */
  public void setFamilies(boolean set) {
    if (isFamilies==set) return;
    isFamilies = set;
    update();
  } 
  
  /**
   * Access - isMarrSymbol
   */
  public boolean isMarrSymbols() {
    return isMarrSymbols;
  }

  /**
   * Access - isShowMarrSymbol
   */
  public void setMarrSymbols(boolean set) {
    if (isMarrSymbols==set) return;
    isMarrSymbols = set;
    update();
  }

  /**
   * Access - isFoldSymbols
   */
  public void setFoldSymbols(boolean set) {
    if (isFoldSymbols==set) return;
    isFoldSymbols = set;
    update();
  }

  /**
   * Access - isToggles
   */
  public boolean isFoldSymbols() {
    return isFoldSymbols;
  }

  /**
   * Accessor - the metrics   */
  public TreeMetrics getMetrics() {
    return metrics;
  } 
  
  /**
   * Accessor - the metrics
   */
  public void setMetrics(TreeMetrics set) {
    if (metrics.equals(set)) return;
    metrics = set;
    update();
  } 
  
  /**
   * Add listener
   */
  public void addListener(ModelListener l) {
    listeners.add(l);
  }
  
  /**
   * Remove listener
   */
  public void removeListener(ModelListener l) {
    listeners.remove(l);
 }
  
  /**
   * Nodes by range
   */
  public Collection<? extends TreeNode> getNodesIn(Rectangle range) {
    if (cache==null) 
      return new HashSet<TreeNode>();
    return cache.get(range);
  }

  /**
   * Arcs by range
   */
  public Collection<TreeArc> getArcsIn(Rectangle range) {
    List<TreeArc> result = new ArrayList<TreeArc>(arcs.size());
    for (TreeArc arc : arcs) {
      if (arc.getPath()!=null && arc.getPath().intersects(range))
        result.add(arc);
    }
    return result;
  }

  /**
   * An node by position
   */
  public TreeNode getNodeAt(int x, int y) {
    // do we have a cache?
    if (cache==null) return null;
    // get nodes in possible range
    int
      w = Math.max(metrics.wIndis, metrics.wFams),
      h = Math.max(metrics.hIndis, metrics.hFams);
    Rectangle range = new Rectangle(x-w/2, y-h/2, w, h);
    // loop nodes
    Iterator it = cache.get(range).iterator();
    while (it.hasNext()) {
      TreeNode node = (TreeNode)it.next();
      Shape shape = node.getShape();
      if (shape!=null&&shape.getBounds2D().contains(x-node.pos.x,y-node.pos.y))
        return node;
    }
    
    // nothing found
    return null;
  }
     /**
   * Content by position
   */
  public Object getContentAt(int x, int y) {
    TreeNode node = getNodeAt(x, y);
    return node!=null ? node.getContent() : null;
  }

  /**
   * An entity by position
   */
  public Entity getEntityAt(int x, int y) {
    Object content = getContentAt(x, y);
    return content instanceof Entity ? (Entity)content : null;
  }
  
  /**
   * A node for entity (might be null)
   */
  public TreeNode getNode(Entity e) {
    return (TreeNode)entities2nodes.get(e);
  }
  
  /**
   * The models space bounds
   */
  public Rectangle getBounds() {
    return bounds;
  }

  /**
   * Add a bookmark
   */
  public void addBookmark(Bookmark b) {
    bookmarks.addFirst(b);
    if (bookmarks.size()>16) bookmarks.removeLast();
  }
  
  /**
   * Accessor - bookmarks
   */
  public List<Bookmark> getBookmarks() {
    return Collections.unmodifiableList(bookmarks);
  }
  
  /**
   * Accessor - bookmarks
   */
  public void setBookmarks(List<Bookmark> set) {
    bookmarks.clear();
    bookmarks.addAll(set);
  }
  
  /**
   * Accessor - id's of entities hiding ancestors
   */
  public Collection<String> getHideAncestorsIDs() {
    return hideAncestors;
  }
  
  /**
   * Accessor - id's of entities hiding ancestors
   */
  public void setHideAncestorsIDs(Collection<String> ids) {
    hideAncestors.clear();
    hideAncestors.addAll(ids);
  }

  /**
   * Accessor - id's of entities hiding descendants
   */
  public Collection<String> getHideDescendantsIDs() {
    return hideDescendants;
  }
  
  /**
   * Accessor - id's of entities hiding descendants
   */
  public void setHideDescendantsIDs(Collection<String> ids) {
    hideDescendants.clear();
    hideDescendants.addAll(ids);
  }

  /**
   * Helper - get ids from collection of entities
   */  
  private Collection<String> getIds(Collection<Entity> entities) {
    List<String> result = new ArrayList<String>();
    for (Entity e : entities) 
      result.add(e.getId());
    return result;    
  }
  
  /**
   * Whether we're hiding descendants of given entity
   */
  /*package*/ boolean isHideDescendants(Indi indi) {
    return hideDescendants.contains(indi.getId());
  }
  
  /**
   * Whether we're hiding ancestors of given entity
   */
  /*package*/ boolean isHideAncestors(Indi indi) {
    return hideAncestors.contains(indi.getId());
  }
  
  /** 
   * The current family of individual
   */
  /*package*/ Fam getFamily(Indi indi, Fam fams[], boolean next) {
    // only one?
    if (fams.length>0) {
      // lookup map
      Fam fam = (Fam)indi2fam.get(indi);
      if (fam==null) fam = fams[0];
      for (int f=0;f<fams.length;f++) {
        if (fams[f]==fam) 
          return fams[(f+(next?1:0))%fams.length];
      }
      // invalid fam
      indi2fam.remove(indi);
    }
    // done
    return fams[0];
  }
  
  /**
   * Adds a node   */
  /*package*/ TreeNode add(TreeNode node) {
    // check content
    Object content = node.getContent();
    if (content instanceof Entity) {
      entities2nodes.put((Entity)content, node);
    }
    nodes.add(node);
    return node;
  }
  
  /**
   * Adds an arc   */
  /*package*/ TreeArc add(TreeArc arc) {
    arcs.add(arc);
    return arc;
  }
  
  /**
   * Currently shown entities
   */
  /*package*/ Set getEntities() {
    return entities2nodes.keySet();
  }

  /**
   * Parses the current model starting at root   */
  private void update() {
    
    // clear old
    arcs.clear();
    nodes.clear();
    entities2nodes.clear();
    bounds.setFrame(0,0,0,0);
    
    // nothing to do if no root set
    if (root==null) {
      fireStructureChanged();
      return;
    }

    // parse and layout    
    try {
      // make sure families only when root is not family
      boolean isFams = isFamilies || root instanceof Fam;
      // parse its descendants
      Parser descendants = Parser.getInstance(false, isFams, this, metrics);
      bounds.add(layout(descendants.parse(root), true));
      // parse its ancestors 
      bounds.add(layout(descendants.align(Parser.getInstance(true, isFams, this, metrics).parse(root)), false));
    } catch (LayoutException e) {
      e.printStackTrace();
      root = null;
      update();
      return;
    }
    
    // create gridcache
    cache = new GridCache(
      bounds, 3*metrics.calcMax()
    );
    Iterator it = nodes.iterator();
    while (it.hasNext()) {
      TreeNode n = (TreeNode)it.next();
      if (n.shape!=null) cache.put(n, n.shape.getBounds(), n.pos);
    }
    
    // notify
    fireStructureChanged();
    // done
  }

  /**
   * Helper that runs a TreeLayout
   */
  private Rectangle layout(TreeNode root, boolean isTopDown) throws LayoutException {
    
    // prepare theta
    double theta = 0;
    if (!isTopDown) theta += 180;
    if (!isVertical) theta -= 90;
    
    // layout
    TreeLayout layout = new TreeLayout();
    layout.setBendArcs(isBendArcs);
    layout.setDebug(false);
    layout.setIgnoreUnreachables(true);
    layout.setBalanceChildren(false);
    layout.setRoot(root);
    layout.setOrientation(theta);
    
    // done
    return layout.layout(root, nodes.size()).getBounds();
  }
  
  
  /**
   * Fire event
   */
  private void fireStructureChanged() {
    for (int l=listeners.size()-1; l>=0; l--) {
      ((ModelListener)listeners.get(l)).structureChanged(this);
    }
  }
  
  /**
   * Fire event
   */
  private void fireNodesChanged(Collection nodes) {
    for (int l=listeners.size()-1; l>=0; l--) {
      ((ModelListener)listeners.get(l)).nodesChanged(this, nodes);
    }
  }
  
  /**
   * NextFamily
   */
  /*package*/ class NextFamily implements Runnable {
    /** indi */
    private Indi indi;
    /** next fams */
    private Fam fam;
    /**
     * constructor
     * @param individual indi to un/fold
     * @param fams number of fams to roll over
     */
    protected NextFamily(Indi individual, Fam[] fams) {
      indi = individual;
      fam = getFamily(indi, fams, true);
    }
    /**
     * perform 
     */
    public void run() {
      indi2fam.put(indi, fam);
      update();
    }
  } //NextFamily
  
  /**
   * FoldUnfold
   */
  /*package*/ class FoldUnfold implements Runnable {
    /** indi */
    private Indi indi;
    /** set to change */
    private Set<String> set;
    /**
     * constructor
     * @param individual indi to un/fold
     * @param ancestors whether to change its ancestors/descendants
     */
    protected FoldUnfold(Indi individual, boolean ancestors) {
      indi = individual;
      set = ancestors ? hideAncestors : hideDescendants; 
    }
    /**
     * perform 
     */
    public void run() {
      if (!set.remove(indi.getId())) set.add(indi.getId());
      update();
    }
  } //FoldUnfold

  /**
   * Our gedcom Callbacks 
   */
  private class Callback extends GedcomListenerAdapter implements GedcomMetaListener {
    
    private Set repaint = new HashSet();
    private boolean update = false;
    private Entity added;
    
    public void gedcomWriteLockAcquired(Gedcom gedcom) {
      added = null;
      repaint.clear();
    }
    
    public void gedcomWriteLockReleased(Gedcom gedcom) {
        
      // we're without root we could set now?
      if (root==null) {
        if (added==null||!gedcom.contains(added))
          added = gedcom.getFirstEntity(Gedcom.INDI);
        root = added;
        update();
        return;
      }
      
      // update necessary?
      if (update) {
        update();
        return;
      }

      // signal repaint 
      if (!repaint.isEmpty()) 
        fireNodesChanged(repaint);
   
    }
    
    public void gedcomEntityAdded(Gedcom gedcom, Entity added) {
      if (added instanceof Fam || added instanceof Indi) {
        if ( !(this.added instanceof Indi) || added instanceof Indi)
          this.added = added;
      }
    }
  
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
      // clear root?
      if (entity == root) 
        root = null;
      
      // clear bookmarks?
      ListIterator it = bookmarks.listIterator();
      while (it.hasNext()) {
        Bookmark b = (Bookmark)it.next();
        if (entity == b.getEntity()) it.remove();
      }
      
      // clear indi2fam?
      indi2fam.keySet().remove(entity);
      
    }
  
    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
      gedcomPropertyChanged(gedcom, added);
    }
  
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
      // a reference update?
      if (property instanceof PropertyXRef) {
        update = true;
        return;
      }
      // something visible?
      Node node = getNode(property.getEntity());
      if (node!=null) 
        fireNodesChanged(Collections.singletonList(node));
    }
  
    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
      // a reference update?
      if (deleted instanceof PropertyXRef)
        update = true;
      // repaint still makes sense?
      if (root!=null)
        repaint.add(getNode(property.getEntity()));
    }
  } // Callback
} //Model
