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

import ancestris.modules.views.tree.style.Style;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import gj.layout.LayoutException;
import gj.layout.tree.TreeLayout;
import gj.model.Node;
import java.awt.Cursor;
import java.awt.Point;
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
import org.openide.windows.WindowManager;

/**
 * Model of our tree
 */
/*package*/ class Model {
  
  /** our gedcom callback */
  private final Callback callback = new Callback();
  
  /** listeners */
  private final List<ModelListener> listeners = new CopyOnWriteArrayList<>();

  /** arcs */
  private final Collection<TreeArc> arcs = new ArrayList<>(100);

  /** nodes */
  private final Map<Entity,TreeNode> entities2nodes = new HashMap<>(100);
  private final Collection<TreeNode> nodes = new ArrayList<>(100);

  /** bounds */
  private final Rectangle bounds = new Rectangle();
  
  /** caching */
  private GridCache cache = null;
  
  /** whether we're vertical or not */
  private boolean isVertical = true;
  
  /** whether we model families or not */
  private boolean isFamilies = true;
  
  /** whether we bend arcs or not */
  private Style style;

  /** whether we show toggles for un/folding */
  private boolean isFoldSymbols = true;
  
  /** max number of generations */
  private int maxGenerations = 20;
  
  /** individuals whose ancestors we're not interested in */
  private final Set<String> hideAncestors = new HashSet<>();
  private final Set<String> hideAncestorsTmp = new HashSet<>();

  /** individuals whose descendants we're not interested in */
  private final Set<String> hideDescendants = new HashSet<>();
  private final Set<String> hideDescendantsTmp = new HashSet<>();
  
  /** individuals' family */
  private final Map<Indi,Fam> indi2fam = new HashMap<>();

  /** the root we've used */
  private Entity root;

  /** visible fallback nodes to recenter on, in case of tree change */
  private final List<Entity> fallbackEntities = new ArrayList<>();

  /** calling view - used to calcultae center of view on restructure of model */
  private TreeView view = null;

  /** metrics */
  //private TreeMetrics metrics = new TreeMetrics( 66, 40, 80, 7, 10 );
  
  /** bookmarks */
  //XXX: We must write a standalone bookmark manager with all stuff
  //from tree module related to bookmark
  //also actions will go to the main toolbar to be used even if 
  //treeview is not opened
  private final LinkedList<Bookmark> bookmarks = new LinkedList<>();
  
  /**
   * Constructor
   */
  public Model(TreeView view, Style style) {
      this.view = view;
      this.style = style;
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
   * Accessor - get max number of generations
   */
  public int getMaxGenerations() {
    return maxGenerations;
  }
  
  /**
   * Accessor - wether we're vertical
   */
  public void setMaxGenerations(int gen) {
    if (gen < 1) {
        gen = 1;
    }
    if (gen > 100) {
        gen = 100;
    }
    maxGenerations = gen;
    update();
  }
  
  /**
   * Accessor - wether we bend arcs or not
   */
  public boolean isBendArcs() {
    return style.bend;
  }
  
  /**
   * Accessor - wether we bend arcs or not
   */
  public void setBendArcs(boolean set) {
    if (style.bend==set) return;
    style.bend = set;
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
    getCenteredEntities();
    update();
  } 
  
  /**
   * Access - isMarrSymbol
   */
  public boolean isMarrSymbols() {
    return style.marr;
  }

  /**
   * Access - isShowMarrSymbol
   */
  public void setMarrSymbols(boolean set) {
    if (style.marr==set) return;
    style.marr = set;
    update();
  }

  /**
   * Access - setFoldSymbols
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
     * Access - FoldAll (refold)
     */
    public void foldAll() {
        // refold all levels up
        if (!hideAncestorsTmp.isEmpty()) {
            setHideAncestorsIDs(hideAncestorsTmp);
        }
        
        // refold all levels down
        if (!hideDescendantsTmp.isEmpty()) {
            setHideDescendantsIDs(hideDescendantsTmp);
        }
        
        update();
    }

    /**
     * Access - UnfoldAll
     */
    public void unfoldAll() {
        // unfold all levels up
        hideAncestorsTmp.clear();
        hideAncestorsTmp.addAll(hideAncestors);
        setHideAncestorsIDs(new ArrayList<>());
        
        // unfold all levels down
        hideDescendantsTmp.clear();
        hideDescendantsTmp.addAll(hideDescendants);
        setHideDescendantsIDs(new ArrayList<>());

        update();
    }

  /**
   * Accessor - Style
   */
  public void setStyle(Style set) {
      // change?
      if (set == null || style.equals(set)) {
          return;
      }
      style = set;
      update();
  } 
  

  /**
   * Access - rectangle shape
   */
  public boolean isRoundedRectangle() {
    return style.roundrect;
  }

  /**
   * Accessor - rectangle shape
   */
  public void setRoundedRectangle(boolean set) {
    if (style.roundrect==set) return;
    style.roundrect = set;
    update();
  } 
  
  /**
   * Accessor - the metrics
   */
  public TreeMetrics getMetrics() {
    return style.tm;
  } 
  
  /**
   * Accessor - the metrics
   */
  public void setMetrics(TreeMetrics set) {
    if (style.tm.equals(set)) return;
    style.tm = set;
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
    @SuppressWarnings("unchecked")
  public Collection<? extends TreeNode> getNodesIn(Rectangle range) {
    if (cache==null) 
      return new HashSet<>();
    return cache.get(range);
  }

  /**
   * Arcs by range
   */
  public Collection<TreeArc> getArcsIn(Rectangle range) {
    List<TreeArc> result = new ArrayList<>(arcs.size());
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
      w = Math.max(style.tm.wIndis, style.tm.wFams),
      h = Math.max(style.tm.hIndis, style.tm.hFams);
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
    return entities2nodes.get(e);
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
    List<String> result = new ArrayList<>();
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
      if (fams.length > 0) {
          // Identify first preferred fam
          Fam preferredFam = fams[0];
          for (Fam f : fams) {
              if (f.isPreferred()) {
                  preferredFam = f;
              }
          }
          
          // Lookup map and if not already mapped, use preferredFam
          Fam fam = indi2fam.get(indi);
          if (fam == null) {
              fam = preferredFam;
          }
          
          // If next fam required (in circular loop), return it 
          for (int f = 0; f < fams.length; f++) {
              if (fams[f] == fam) {
                  return fams[(f + (next ? 1 : 0)) % fams.length];
              }
          }
          
          // If fam returned is not valid, remove it from map
          indi2fam.remove(indi);
      }
      // done
      return fams[0];
  }
  
  /**
   * Adds a node
   */
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
   * Adds an arc
   */
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
   * Parses the current model starting at root
   */
  private void update() {
    
      WindowManager.getDefault().getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      // clear old
      arcs.clear();
      nodes.clear();
      entities2nodes.clear();
      bounds.setFrame(0, 0, 0, 0);

      // nothing to do if no root set
      if (root == null) {
          fireStructureChanged();
          WindowManager.getDefault().getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          return;
      }

      // parse and layout    
      try {
          // make sure families only when root is not family
          boolean isFams = isFamilies || root instanceof Fam;
          // parse its descendants
          Parser descendants = Parser.getInstance(false, isFams, this, style.tm);
          bounds.add(layout(descendants.parse(root), true));
          // parse its ancestors 
          bounds.add(layout(descendants.align(Parser.getInstance(true, isFams, this, style.tm).parse(root)), false));
      } catch (LayoutException e) {
          e.printStackTrace();
          root = null;
          update();
          WindowManager.getDefault().getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          return;
      }

      // create gridcache
      cache = new GridCache(bounds, 3 * style.tm.calcMax());
      for (TreeNode n : nodes) {
          if (n.shape != null) {
              cache.put(n, n.shape.getBounds(), n.pos);
          }
      }

      // notify
      fireStructureChanged();
      WindowManager.getDefault().getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
    layout.setBendArcs(style.bend);
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
      (listeners.get(l)).structureChanged(this);
    }
  }
  
  /**
   * Fire event
   */
    @SuppressWarnings("unchecked")
  private void fireNodesChanged(Collection nodes) {
    for (int l=listeners.size()-1; l>=0; l--) {
      (listeners.get(l)).nodesChanged(this, nodes);
    }
  }
  
  
    public void getCenteredEntities() {
        Point p = view.getCenter();
        if (p == null) {
            return;
        }
        int x = p.x;
        int y = p.y;
        int s = 0;
        int inc = +10;
        Entity entity = null;
        while (entity == null) {
            entity = getEntityAt(x, y - s);
            s += inc;
            if ((y - s) < bounds.y) {
                inc = -10;
                y = p.y;
                s = 0;
            }
            if ((y - s) > (bounds.y + bounds.height)) {
                break;
            }
        }
        if (entity == null) {
            return; // not found, so leave past result unchanged
        }

        // We have found one so clear past results
        fallbackEntities.clear();
        fallbackEntities.add(entity);
        if (entity instanceof Fam) {
            fallbackEntities.add(entity);
            Fam fam = (Fam) entity;
            Indi husb = fam.getHusband();
            if (husb != null) {
                fallbackEntities.add(husb);
            }
            Indi wife = fam.getWife();
            if (wife != null) {
                fallbackEntities.add(wife);
            }
        }
    }

    
    public List<Entity> getDefaultEntities() {
        return fallbackEntities;
    }
  
    public void clearDefaultEntities() {
        fallbackEntities.clear();
    }
  
  
  
  /**
   * NextFamily
   */
  /*package*/ class NextFamily implements Runnable {
    /** indi */
    private final Indi indi;
    /** next fams */
    private final Fam fam;
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
    @Override
    public void run() {
      indi2fam.put(indi, fam);
      fallbackEntities.clear();
      fallbackEntities.add(indi);
      update();
    }
    /**
     * access
     */
    public int getSpouseSex() {
        Indi spouse = fam.getOtherSpouse(indi);
        return spouse != null ? spouse.getSex() : PropertySex.UNKNOWN;
    }

    public Entity getSpouse() {
        return fam.getOtherSpouse(indi);
    }
  } //NextFamily
  
  /**
   * FoldUnfold
   */
  /*package*/ class FoldUnfold implements Runnable {
    /** indi */
    private final Indi indi;
    /** set to change */
    private final Set<String> set;
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
    @Override
    public void run() {
      if (!set.remove(indi.getId())) set.add(indi.getId());
      fallbackEntities.clear();
      fallbackEntities.add(indi);
      update();
    }

    public void fold() {
      set.add(indi.getId());
      update();
    }

    public void unfold() {
      set.remove(indi.getId());
      update();
    }
  } //FoldUnfold

  /**
   * Our gedcom Callbacks 
   */
  private class Callback extends GedcomListenerAdapter {
    
    private final Set<Node> repaint = new HashSet<>();
    private boolean update = false;
    private Entity added;  // first added indi (or fam otherwise) in a block unit of change
    private boolean isFamAdded = false;

    
    @Override
    public void gedcomWriteLockAcquired(Gedcom gedcom) {
      added = null;
      repaint.clear();
    }
    
    @Override
        public void gedcomWriteLockReleased(Gedcom gedcom) {

            if (isFamAdded) {  // force family mode
                setFamilies(true);
                view.forceFamilies(true);
                isFamAdded = false;
            }
            
            // if added entity (only the first one) is an indi that is not ancestor or descendant of root, it will not be visible. Signal user or change root.
            if (root != null && added != null && added instanceof Indi) {
                Indi addedIndi = (Indi) added;
                Entity newRoot = null;
                if (!isDirectOf(addedIndi, root)) {
                    Fam[] fams = addedIndi.getFamiliesWhereChild();
                    if (fams != null && fams.length != 0) {
                        newRoot = fams[0];
                    } else {
                        Indi[] children = addedIndi.getChildren();
                        if (children != null && children.length != 0) {
                            newRoot = children[0];
                        } else {
                            // give up, entity is isolated
                        }
                    }
                    
                }
                if (newRoot != null) {
                    root = newRoot;
                    view.setRoot(root);
                    update();
                    return;
                }
            }
            
            // if we're without root, which one to pick ? => centered or else first entity
            if (root == null) {
                if (added == null || !gedcom.contains(added)) {
                    getCenteredEntities();
                    boolean found = false;
                    for (Entity ent : fallbackEntities) {
                        if (ent != null && gedcom.contains(ent)) {
                            added = ent;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        added = gedcom.getFirstEntity(Gedcom.INDI);
                    }
                }
                root = added;
                view.setRoot(root);
                update();
                return;
            }

            // update necessary?
            if (update) {
                update();
                return;
            }

            // signal repaint 
            if (!repaint.isEmpty()) {
                fireNodesChanged(repaint);   // this just does an overall repaint
            }
        }

    @Override
        public void gedcomEntityAdded(Gedcom gedcom, Entity added) {
            if (added instanceof Fam || added instanceof Indi) {
                if (!(this.added instanceof Indi) && added instanceof Indi) {
                    this.added = added; // first indi seen
                }
                if (!isFamAdded) {
                    isFamAdded = (added instanceof Fam);
                }
            }
        }

    @Override
        public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
            // clear root?
            if (entity == root) {
                root = null;
            }

            // clear bookmarks?
            ListIterator it = bookmarks.listIterator();
            while (it.hasNext()) {
                Bookmark b = (Bookmark) it.next();
                if (entity == b.getEntity()) {
                    it.remove();
                }
            }

            // clear indi2fam?
            indi2fam.keySet().remove(entity);

        }

        @Override
        public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
            gedcomPropertyChanged(gedcom, added);
        }

    @Override
        public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
            // a reference update?
            if (property instanceof PropertyXRef) {
                update = true;
                return;
            }
            // a family preference change ?
            if (property.getTag().equals(Fam.TAG_PREF)) {
                Fam fam = (Fam) property.getEntity();
                Indi husb = fam.getHusband();
                if (husb != null) {
                    indi2fam.keySet().remove(husb);
                }
                Indi wife = fam.getWife();
                if (wife != null) {
                    indi2fam.keySet().remove(wife);
                }
                update = true;
                return;
            }
            // something visible?
            Node node = getNode(property.getEntity());
            if (node != null) {
                //fireNodesChanged(Collections.singletonList(node)); // FL: 2017-03-14 - for mass updates, this line significantly slows down performance, not necessary, add this other line instead.
                repaint.add(getNode(property.getEntity()));
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
            // a reference update?
            if (deleted instanceof PropertyXRef) {
                update = true;
            }
            // repaint still makes sense?
            if (root != null) {
                repaint.add(getNode(property.getEntity()));
            }
        }

        private boolean isDirectOf(Indi addedIndi, Entity root) {
            if (root instanceof Indi) {
                return (addedIndi.isAncestorOf((Indi) root) || addedIndi.isDescendantOf((Indi) root));
            }
            if (root instanceof Fam) {
                return (addedIndi.isAncestorOf((Fam) root) && !addedIndi.isDescendantOf((Fam) root));
            }
            return false;
        }
        
    } // Callback
} //Model
