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
package genj.renderer;

import genj.gedcom.Gedcom;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import genj.util.swing.HeadlessLabel;
import genj.window.WindowManager;

import java.awt.Color;
import java.awt.Component;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import swingx.tree.AbstractTreeModel;

/** 
 * A list of editable BluePrints */
public class BlueprintList extends JSplitPane {
  
  /** selection of blueprints */
  private Map selection = new HashMap();
  
  /** we keep one editor */
  private BlueprintEditor editor;

  /** tree of blueprints */
  private JTree treeBlueprints;
  
  /** our actions */
  private Add add = new Add();
  private Del del = new Del();
  
  /** resources */
  private final static Resources resources = Resources.get(BlueprintEditor.class);
 
  /** the current Gedcom */
  private Gedcom gedcom; 
  
  /** model used for tree on left */
  private Model model = new Model();
  
  /**
   * Constructor   */
  public BlueprintList(BlueprintManager bpMgr) {
    
    // create editor
    editor = new BlueprintEditor(bpMgr);
    
    // prepare tree
    treeBlueprints = new JTree(model);
    treeBlueprints.setRootVisible(false);
    treeBlueprints.setShowsRootHandles(true);
    treeBlueprints.setCellRenderer(new Renderer());
    treeBlueprints.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    treeBlueprints.getSelectionModel().addTreeSelectionListener(new SelectionListener());
    
    // left section
    Box left = new Box(BoxLayout.Y_AXIS);
    JScrollPane scroll = new JScrollPane(treeBlueprints);
    scroll.setAlignmentX(0);
    left.add(scroll);
    
    ButtonHelper bh = new ButtonHelper().setContainer(left);
    bh.create(add);
    bh.create(del);
    
    // children
    setLeftComponent(left);
    setRightComponent(editor);
    // done    
  }
  
  /**
   * Sets the Gedcom to use   */
  public void setGedcom(Gedcom geDcom) {
    gedcom = geDcom;
  }
  
  /**
   * Acessor - selection   */
  public Map getSelection() {
    editor.commit();
    return selection;
  }
  
  /**
   * Acessor - selection
   */
  public void setSelection(Map selEction) {
    selection = selEction;
    treeBlueprints.repaint();
  }
  
  /**
   * Action Add
   */
  private class Add extends Action2 {
    /**
     * Constructor     */
    private Add() {
      super.setText(resources, "blueprint.add");
      super.setEnabled(false);
    }
    /**
     * @see genj.util.swing.Action2#execute()
     */
    protected void execute() {
      // check selection
      TreePath path = treeBlueprints.getSelectionPath();
      if (path==null) 
        return;
      Object node = path.getLastPathComponent();
      // get name
      String name = WindowManager.getInstance(BlueprintList.this).openDialog(
        null,
        null,
        WindowManager.QUESTION_MESSAGE,
        resources.getString("blueprint.add.confirm"),
        "",
        BlueprintList.this
      );
      if (name==null||name.length()==0) 
        return;
      // get html
      String html = node instanceof Blueprint ? ((Blueprint)node).getHTML() : "";
      // add it
      try {
        Blueprint blueprint = BlueprintManager.getInstance().addBlueprint(new Blueprint(
          node instanceof Blueprint ? ((Blueprint)node).getTag() : (String)node,
          name, html, false
        ));
        // update model
        model.fireStructureChanged();
        // re-select and make html visible
        treeBlueprints.setSelectionPath(new TreePath(model.getPathToRoot(blueprint)));
        editor.setHTMLVisible(true);
      } catch (IOException e) {
        // TODO add user dialog 
      }
      // done
    }
  } //ActionAdd

  /**
   * Action Remove
   */
  private class Del extends Action2 {
    /**
     * Constructor
     */
    private Del() {
      super.setText(resources, "blueprint.del");
      super.setEnabled(false);
    }
    /**
     * @see genj.util.swing.Action2#execute()
     */
    protected void execute() {
      // check selection
      TreePath path = treeBlueprints.getSelectionPath();
      if (path==null) 
        return;
      Object node = path.getLastPathComponent();
      if (!(node instanceof Blueprint)) 
        return;
      // confirm
      Blueprint blueprint = (Blueprint)node;
      int rc = WindowManager.getInstance(BlueprintList.this).openDialog(null,null,WindowManager.QUESTION_MESSAGE,resources.getString("blueprint.del.confirm", blueprint.getName()),Action2.okCancel(),BlueprintList.this); 
      if (rc!=0) 
        return;
      // remove selection
      selection.remove(blueprint.getTag());
      // delete it
      try {
        BlueprintManager.getInstance().delBlueprint(blueprint);
      } catch (IOException e) {
        // TODO show a warning dialog
      }
      // show it
      model.fireStructureChanged();
      // done
    }
  } //ActionAdd

  /**
   * Our tree cell renderer
   */
  private class Renderer implements TreeCellRenderer {

    /** a label */
    private HeadlessLabel label = new HeadlessLabel();
    
    /** a color for selection backgrounds */
    private Color cSelection = new DefaultTreeCellRenderer().getBackgroundSelectionColor();
    
    /** a radiobutton */
    private JRadioButton button = new JRadioButton();

    /**
     * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

      // blueprint?
      if (value instanceof Blueprint) {
        Blueprint bp = (Blueprint)value;
        button.setOpaque(selected);
        button.setBackground(cSelection);
        button.setText(bp.getName());
        button.setSelected(selection.get(bp.getTag())==bp);
        // done
        return button; 
      }
      
      // type tag?
      if (value instanceof String) {
        String tag = (String)value;
        label.setOpaque(selected);
        label.setBackground(cSelection);
        label.setText(Gedcom.getName(tag, true));
        label.setIcon(Gedcom.getEntityImage(tag));
      }
      
      // done
      return label;
    }
    
  } //Renderer

  /**
   * Our selection listener
   */
  private class SelectionListener implements TreeSelectionListener  {

    /**
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent e) {
      
      // commit editor's changes
      editor.commit();
      
      // selection?
      if (e.getNewLeadSelectionPath()!=null) {

        // analyse node
        Object node = e.getNewLeadSelectionPath().getLastPathComponent();
        
        // different Blueprint selected -> o.k.
        if (node instanceof Blueprint) {
          Blueprint bp = (Blueprint)node;
          // .. update selection
          selection.put(bp.getTag(), bp);
          // .. gotta repaint for old
          treeBlueprints.repaint();
          // .. buttons
          add.setEnabled(true);
          del.setEnabled(!bp.isReadOnly());
          // .. editor
          editor.set(gedcom, bp, !bp.isReadOnly());
          return;
        }
      
        // different type section selected 
        add.setEnabled(true);
        del.setEnabled(false);

      } else {

        add.setEnabled(false);
        del.setEnabled(false);
        
      }
            
      // .. editor
      editor.set(null, null, false);

      // done
    }
    
  } //SelectionListener

  /**
   * Our tree model
   */
  private class Model extends AbstractTreeModel {
     
    /**
     * change notification 
     */
    protected void fireStructureChanged() {
      fireTreeStructureChanged(this, new Object[] { this }, null, null);
    }

    /**
     * impl - leaf test
     */    
    public boolean isLeaf(Object node) {
      return node instanceof Blueprint;
    }
    
    /**
     * impl - index of child
     */
    public int getIndexOfChild(Object parent, Object child) {

      // child one of the blueprints?
      if (child instanceof Blueprint) {
        Blueprint bp = (Blueprint)child;
        return BlueprintManager.getInstance().getBlueprints(bp.getTag()).indexOf(bp);
      }
  
      // must be tag
      String tag = (String)child;
      for (int i=0;i<Gedcom.ENTITIES.length;i++)
        if (Gedcom.ENTITIES[i].equals(tag))
          return i;
          
      // unknown
      throw new IllegalArgumentException();
    }

    /**
     * impl - parent of node
     */    
    protected Object getParent(Object node) {
      // root has no parents
      if (node==this)
        return null;
      // blueprints are leaves under tags
      if (node instanceof Blueprint)
        return ((Blueprint)node).getTag();
      // tags have this as parent/root
      return this;
    }

    /**
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    public Object getChild(Object parent, int index) {
      // root this?
      if (parent==this)
        return Gedcom.ENTITIES[index];
      // has to be entity tag
      String tag = (String)parent;
      return BlueprintManager.getInstance().getBlueprints(tag).get(index);
    }

    /**
     * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
     */
    public int getChildCount(Object parent) {
      // root this?
      if (parent==this)
        return Gedcom.ENTITIES.length;
      // blueprint leaf?
      if (parent instanceof Blueprint)
        return 0;
      // entity tag
      String tag = (String)parent;
      return BlueprintManager.getInstance().getBlueprints(tag).size();
    }

    /**
     * @see javax.swing.tree.TreeModel#getRoot()
     */
    public Object getRoot() {
      return this;
    }

  } // Model
  
} //BluePrintList
