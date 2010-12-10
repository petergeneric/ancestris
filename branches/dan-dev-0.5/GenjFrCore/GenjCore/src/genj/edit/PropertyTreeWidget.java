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
package genj.edit;
 
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomListener;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyChange;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.gedcom.UnitOfWork;
import genj.io.PropertyReader;
import genj.io.PropertyTransferable;
import genj.util.swing.HeadlessLabel;
import genj.util.swing.ImageIcon;
import genj.view.ContextProvider;
import genj.view.ViewContext;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import spin.Spin;
import swingx.dnd.tree.DnDTree;
import swingx.dnd.tree.DnDTreeModel;
import swingx.tree.AbstractTreeModel;

/**
 * A Property Tree
 */
public class PropertyTreeWidget extends DnDTree implements ContextProvider {
  
  private final static String UNIX_DND_FILE_PREFIX = "file:";
  
  /** a default renderer we keep around for colors */
  private DefaultTreeCellRenderer defaultRenderer;
  
  /** stored gedcom */
  private Gedcom gedcom;

  /**
   * Constructor
   */
  public PropertyTreeWidget(Gedcom gedcom) {

    // initialize an empty model
    super.setModel(new Model(gedcom));

    // remember
    this.gedcom = gedcom;
    
    // setup callbacks
    setCellRenderer(new Renderer());
    getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    setToggleClickCount(Integer.MAX_VALUE);
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        // default JTree doesn't react to right-mouse clicks -  we're trying harder
        if (e.getButton()==1)
          return;
        // something selectable?
        TreePath path = getPathForLocation(e.getX(), e.getY());
        if (path==null)
          return;
        // make sure it's selected
        if (!getSelection().contains(path.getLastPathComponent()))
          setSelection(Collections.singletonList((Property)path.getLastPathComponent()));
      }
    });

    setExpandsSelectedPaths(true);
    ToolTipManager.sharedInstance().registerComponent(this);
    
    // done
  }
  
  /**
   * Accessor - current context 
   * @return Gedcom tree's root and selection 
   */
  public ViewContext getContext() {
    // no root - it's the gedcom itself
    Entity root = (Entity)getRoot();
    if (root==null) 
      return new ViewContext(gedcom);
    // no selection - it's the root
    List<Property> selection = getSelection();
    if (selection.isEmpty())
      return new ViewContext(root);
    // we can be specific now
    return new ViewContext(gedcom, new ArrayList<Entity>(), selection);
  }
  
  /**
   * Dont' allow to change the underlying model
   */
  public void setModel() {
    throw new IllegalArgumentException();
  }

  /**
   * Access to our specialized model
   */
  private Model getPropertyModel() {
    return (Model)getModel();
  }
  
  /**
   * return a path for a property
   */
  public Object[] getPathFor(Property property) {
    return getPropertyModel().getPathToRoot(property);
  }

  /**
   * track usage in ui
   */
  public void addNotify() {
    // continue
    super.addNotify();
    // connect model to gedcom
    gedcom.addGedcomListener((GedcomListener)Spin.over(getPropertyModel()));
  }

  
  /**
   * track usage in ui
   */
  public void removeNotify() {
    // disconnect model from gedcom
    gedcom.removeGedcomListener((GedcomListener)Spin.over(getPropertyModel()));
    // continue
    super.removeNotify();
  }
  
  /**
   * @see javax.swing.JTree#getPreferredScrollableViewportSize()
   */
  public Dimension getPreferredScrollableViewportSize() {
    return new Dimension(256,128);
  }
  
  /**
   * Set the current root
   */
  public void setRoot(Property property) {
    // change?
    if (getPropertyModel().getRoot()==property)
      return;
    // propagate to model
    getPropertyModel().setRoot(property);
    // done
  }
  
  /**
   * Expand all rows
   */
  public void expandAllRows() {
    for (int i=0;i<getRowCount();i++)
      expandRow(i); 
  }
  
  /**
   * Expand by path
   */
  public void expand(TagPath path) {
    rows: for (int row=getRowCount()-1;row>=0;row--) {
      TreePath tp = getPathForRow(row);
      if (path.length()==tp.getPathCount()) {
        for (int i=0;i<path.length();i++) {
          if (!path.get(i).equals( ((Property)tp.getPathComponent(i)).getTag() ))
            continue rows;
        }
        expandRow(row);
      }
    }
  }
    
 
//  /**
//   * Expand 'under' path
//   */
//  public void expandAll(TreePath root) {
//    
//    //collapsePath(root);
//    expandPath(root);
//    
//    Model model = getPropertyModel();
//    Object node = root.getLastPathComponent();
//    for (int i=0;i<model.getChildCount(node);i++)
//      expandAll(root.pathByAddingChild(model.getChild(node, i)));
//  }
  
  /**
   * The current root
   */
  public Property getRoot() {
    return getPropertyModel().getPropertyRoot();
  }

  /**
   * Some LnFs have the habit of fixing the row height which
   * we don't want
   */  
  public void setRowHeight(int rowHeight) {
    super.setRowHeight(0);
  }
  
  /**
   * Selects a property
   */
  public void setSelection(List<? extends Property> select) {
    clearSelection();
    // safety check
    Property root = (Property)getPropertyModel().getRoot();
    if (root==null) 
      return;
    // add to selection
    TreePath first = null;
    for (Property p : select) {
      try {
        TreePath path = new TreePath(getPropertyModel().getPathToRoot(p));
        addSelectionPath(path);
        if (first==null) first = path;
      } catch (IllegalArgumentException e) {
        // ignore
      }
    }
    // show it 
    if (first!=null)
      scrollPathToVisible(first);
    // done
  }
  
  /**
   * returns the currently selected properties
   */
  public List<Property> getSelection() {
    // go through selection paths
    List<Property> result = new ArrayList<Property>();
    TreePath[] paths = getSelectionPaths();
    for (int i=0;paths!=null&&i<paths.length;i++) {
      result.add((Property)paths[i].getLastPathComponent());
    }
    // done
    return result;
  }
  
  /**
   * Resolve property for location
   */
  public Property getPropertyAt(int x, int y) {
    // calc path to node under mouse
    TreePath path = super.getPathForLocation(x, y);
    if ((path==null) || (path.getPathCount()==0)) 
      return null;
    // done
    return (Property)path.getLastPathComponent();
  }

  /**
   * Resolve property at given point
   */
  public Property getPropertyAt(Point pos) {
    return getPropertyAt(pos.x, pos.y);
  }
  
  /**
   * @see javax.swing.JTree#getToolTipText(MouseEvent)
   */
  public String getToolTipText(MouseEvent event) {
    // lookup property
    Property prop = getPropertyAt(event.getX(),event.getY());
    if (prop==null) return null;
    // .. transient?
    if (prop.isTransient()) return null;
    // .. won't work if property is not part of entity (e.g. Cliboard.Copy)
    if (prop.getEntity()==null) return null;
    // .. calc information text
    String info = prop.getPropertyInfo();
    if (info==null) 
      return null;
    // return text wrapped to 200 pixels
    return "<html><table width=200><tr><td>"+info+"</td></tr></table></html";    
  }

  /**
   * Intercept new ui to get default renderer that provides us with colors
   */  
  public void setUI(TreeUI ui) {
    // continue
    super.setUI(ui);
    // grab the default renderer now
    defaultRenderer = new DefaultTreeCellRenderer();
  }
  
  /**
   * override to provide simple text value (is used by getNextMatch on key presses) 
   *
   */
  public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    if (value instanceof Property)
      return ((Property)value).getTag();
    return "";
  }
  
  /** the gedcom where a drag originated from */
  private static Gedcom draggingFrom = null;
  
  /**
   * Our model
   */
  private class Model extends AbstractTreeModel implements DnDTreeModel, GedcomListener {

    /** root of tree */
    private Property root = null;

    /** the gedcom we're looking at */
    private Gedcom ged;
    
    /**
     * constructor
     */
    protected Model(Gedcom gedcom) {
      this.ged = gedcom;
    }
    
    /**
     * Set the root
     */
    protected void setRoot(Property set) {
      // remember
      root = set;
      // .. tell about it
      rootExchanged();
      // make sure we don't show null-root
      setRootVisible(root!=null);
    }
    
    public Property getPropertyRoot() {
      return root;
    }

    /**
     * DND support - transferable
     */
    public Transferable createTransferable(Object[] nodes) {
      
      // remember where we're dragging from
      draggingFrom = ged;
      
      // normalize selection
      List<Property> props = new ArrayList<Property>(nodes.length);
      for (Object node : nodes)
        props.add((Property)node);
      List<Property> list = Property.normalize(props);
      
      // done 
      return new PropertyTransferable(list);
    }

    public int getDragActions(Transferable transferable) {
      return COPY | MOVE;
    }

    @SuppressWarnings("unchecked")
    public int getDropActions(Transferable transferable, Object parent, int index) {

      try {

        // an in-vm dnd?
        if (transferable.isDataFlavorSupported(PropertyTransferable.VMLOCAL_FLAVOR)) {
          // we don't allow drop on parent if parent is in list of dragged (recursive)
          List<Property> dragged = (List<Property>)transferable.getTransferData(PropertyTransferable.VMLOCAL_FLAVOR);
          Property pparent = (Property)parent;
          while (pparent!=null) {
            if (dragged.contains(pparent)) 
              return 0;
            pparent = pparent.getParent();
          }
          return COPY | MOVE;
        }
        
        // a string dnd?
        if (transferable.isDataFlavorSupported(PropertyTransferable.STRING_FLAVOR)) 
          return COPY | MOVE;
        
        // files are ok as well
        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) 
          return COPY | MOVE;

      } catch (Exception e) {
      }
      
      // fallthrough result
      return 0;
    }
    
    /**
     * DND support - drop comes first.
     */
    @SuppressWarnings("unchecked")
    public void drop(final Transferable transferable, final Object parent, final int index, final int action) throws IOException, UnsupportedFlavorException {

      final Property pparent = (Property)parent;

      // an in-vm drag?
      if (transferable.isDataFlavorSupported(PropertyTransferable.VMLOCAL_FLAVOR)) {

        // dragging children for reordeing in place?
        final List<Property> children = (List<Property>)transferable.getTransferData(PropertyTransferable.VMLOCAL_FLAVOR);
        if (action==MOVE&&pparent.hasProperties(children)) {
          ged.doMuteUnitOfWork(new UnitOfWork() {
            public void perform(Gedcom gedcom) throws GedcomException {
              pparent.moveProperties(children, index);
            }
          });
          return;
        }
        
        // do a cut&paste
        ged.doMuteUnitOfWork(new IOUnitOfWork() {
          protected void performIO(Gedcom gedcom) throws IOException, UnsupportedFlavorException {
            
            // keep track of transferred xrefs
            List<PropertyXRef> xrefs = new ArrayList<PropertyXRef>();
            String string = transferable.getTransferData(PropertyTransferable.STRING_FLAVOR).toString();
            
            // paste text keep track of xrefs
            new PropertyReader(new StringReader(string), xrefs, true).read(pparent, index);
            
            // delete children for MOVE within same gedcom (drag won't do it)
            if (action==MOVE&&draggingFrom==gedcom) {
              for (Property child : children) 
                child.getParent().delProperty(child);
            }
            
            // link references now that already existing props have been deleted
            for (PropertyXRef xref : xrefs) {
              try { 
                xref.link(); 
              } catch (Throwable t) {
                EditView.LOG.log(Level.WARNING, "caught exception during dnd trying to link xrefs", t);
              }
            }
          }
        });
        
        return;
      }
      
      // a list of files (apparently only on windows)?
      if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        
        ged.doMuteUnitOfWork(new IOUnitOfWork() {
          protected void performIO(Gedcom gedcom) throws IOException, UnsupportedFlavorException {
            for (File file : (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor))
              pparent.addFile(file);
          }
        });
        
        return;
      }
      
      // not even the string case?
      if (!transferable.isDataFlavorSupported(PropertyTransferable.STRING_FLAVOR))
        return;
      
      final String string = transferable.getTransferData(PropertyTransferable.STRING_FLAVOR).toString();
      if (string.length()<4)
        return;
      
      // a file drop? apparently a file drop is a simple text starting with file: on linux (kde/gnome)
      if (string.startsWith(UNIX_DND_FILE_PREFIX)) {
        ged.doMuteUnitOfWork(new UnitOfWork() {
          public void perform(Gedcom gedcom) {
            for (StringTokenizer files = new StringTokenizer(string, "\n"); files.hasMoreTokens(); ) {
              String file = files.nextToken().trim();
              if (file.startsWith(UNIX_DND_FILE_PREFIX)) 
                pparent.addFile(new File(file.substring(UNIX_DND_FILE_PREFIX.length())));
            }
          }
        });
        return;
      }
      
      // still some text we can paste into new parent?
      EditView.LOG.fine("reading dropped text '"+string+"'");
      ged.doMuteUnitOfWork(new IOUnitOfWork() {
        protected void performIO(Gedcom gedcom) throws IOException, UnsupportedFlavorException {
          new PropertyReader(new StringReader(string), null, true).read(pparent, index);
        }
      });
      
      // done      
    }

    /**
     * DND support - drag after drop!
     */
    @SuppressWarnings("unchecked")
    public void drag(Transferable transferable, int action) throws UnsupportedFlavorException, IOException {
      
      // anything to drag?
      final List<Property> children = (List<Property>)transferable.getTransferData(PropertyTransferable.VMLOCAL_FLAVOR);
      if (children.isEmpty())
        return;
      
      // drag out children if it's a move to a different gedcom
      if (action==MOVE &&draggingFrom!=ged) {
        
        ged.doMuteUnitOfWork(new UnitOfWork() {
          public void perform(Gedcom gedcom) {
            for (int i=0;i<children.size();i++) {
              Property child = (Property)children.get(i);
              child.getParent().delProperty(child);
            }
          }
        });
      }
      
      // done
    }
       
    public void releaseTransferable(Transferable transferable) {
      draggingFrom  = null;
    }
    
    /**
     * Returns parent of node
     */  
    protected Object getParent(Object node) {
      // none for root
      if (node==root)
        return null;
      // otherwise its parent
      return ((Property)node).getParent();
    }
  
    /**
     * Returns child by index of parent
     */
    public Object getChild(Object parent, int index) {
      return ((Property)parent).getProperty(index);
    }          
  
    /**
     * Returns child count of parent
     */
    public int getChildCount(Object parent) {
      return ((Property)parent).getNoOfProperties();
    }
    
    /**
     * Returns index of given child from parent
     */
    public int getIndexOfChild(Object parent, Object child) {
      try {
        return ((Property)parent).getPropertyPosition((Property)child);
      } catch (Throwable t) {
        return -1;
      }
    }          
  
    /**
     * Returns root of tree
     */
    public Object getRoot() {
      return root;
    }          
  
    /**
     * Tells wether object is a leaf
     */
    public boolean isLeaf(Object node) {
      // nothing is leaf - allows to drag everywhere
      return ((Property)node).getNoOfProperties()==0;
    }          
  
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
      // ignored
    }

    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
      // can't serve anymore?
      if (root==entity)
        setRoot(null);
    }

    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
      // TODO we should check if added belongs to a property one of our xrefs is pointing to
      // us?
      if (root!=property.getEntity())
        return;
      // update
      Object[] path = getPathFor(property);
      fireTreeNodesInserted(this, path, new int[] { pos }, new Property[]{ added });
      // expand all rows
      //expandAllRows();
     
     // NM 20070520 - used to follow selection here but
     // a) this is not good as the selection gets propagated to listeners who haven't received the new property yet
     // b) a selection change happens anyways (the action for adding a property does so)
     // ... removing for now
//      // selection (but no CHAN)
//      if (!(added instanceof PropertyChange))
//        setSelection(Collections.singletonList(added));
      // done
    }

    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
      // us?
      if (root!=property.getEntity())
        return;
      // update
      fireTreeNodesChanged(this, getPathFor(property), null, null);
    }

    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
      // us?
      if (root!=property.getEntity())
        return;
      // just a CHAN?
      if (property instanceof PropertyChange)
        return;
      // update
      fireTreeNodesRemoved(this, getPathFor(property), new int[] { pos }, new Property[]{ deleted });
      // done
    }
  
  } //Model

  /**
   * Our renderer
   */
  private class Renderer extends HeadlessLabel implements TreeCellRenderer {
    
    /**
     * Constructor
     */
    private Renderer() {
      setOpaque(true);
    }
    
    /**
     * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object object, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

      // no property no luck      
      if (!(object instanceof Property))
        return this;
      Property prop = (Property)object;

      // prepare color
      if (defaultRenderer!=null) {
        if (sel) {
          setForeground(defaultRenderer.getTextSelectionColor());
          setBackground(defaultRenderer.getBackgroundSelectionColor());
        } else {
          setForeground(defaultRenderer.getTextNonSelectionColor());
          setBackground(defaultRenderer.getBackgroundNonSelectionColor());
        }
      }

      // calc image        
      ImageIcon img = prop.getImage(true);
      if (prop.isPrivate()) 
        img = img.getOverLayed(MetaProperty.IMG_PRIVATE);
      setIcon(img);

      // calc text
      setText(prop instanceof Entity ? calcText((Entity)prop) : calcText(prop));

      // done
      return this;
    }

    private String calcText(Entity entity) {        
      return "@" + entity.getId() + "@ " + entity.getTag();
    } 
      
    private String calcText(Property prop) {

      StringBuffer result = new StringBuffer();
      
      if (!prop.isTransient()) {
        result.append(prop.getTag());
        result.append(' ');
      }

      // private?
      if (prop.isSecret()) {
        result.append("*****");
      } else {
        String val = prop.getDisplayValue();
        int nl = val.indexOf('\n');
        if (nl>=0) val = val.substring(0, nl) + "...";
        result.append(val);
      }
      
      // done
      return result.toString();
    }
    
  } //Renderer
  
  private abstract class IOUnitOfWork implements UnitOfWork {
    public final void perform(Gedcom gedcom) throws GedcomException {
      try {
        performIO(gedcom);
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }
    protected abstract void performIO(Gedcom gedcom) throws IOException, UnsupportedFlavorException;
  }

} //PropertyTree
