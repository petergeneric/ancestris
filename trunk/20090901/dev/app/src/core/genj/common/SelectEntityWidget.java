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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Grammar;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyComparator;
import genj.gedcom.PropertyDate;
import genj.gedcom.TagPath;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.PopupWidget;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;

/**
 * A widget for choosing an entity amongst many
 */
public class SelectEntityWidget extends JPanel {
  
  private final static Resources RESOURCES = Resources.get(SelectEntityWidget.class);

  /** type of entities to choose from */
  private String type = Gedcom.INDI;
  
  /** entities to choose from */
  private Object[] list;
  private Object none;
  
  /** widgets */
  private PopupWidget sortWidget;
  private JComboBox listWidget;
  
  /** registry */
  private Registry registry = Registry.lookup("genj", null);
  
  /** sorts */
  private Sort sort;
  
  private final static String[] SORTS = {
    "INDI:NAME",
    "INDI",
    "INDI:BIRT:DATE",
    "INDI:DEAT:DATE",
    "FAM",
    "FAM:MARR:DATE",
    "OBJE", 
    "OBJE:TITL", 
    "NOTE", 
    "NOTE:NOTE", 
    "SOUR", 
    "SOUR:TITL", 
    "SOUR:AUTH", 
    "SOUR:REPO", 
    "SUBM", 
    "REPO",
    "REPO:NAME",
    "REPO:REFN",
    "REPO:RIN"
  };
  
  /**
   * Constructor
   */
  public SelectEntityWidget(Gedcom gedcom, String type, String none) {

    // remember and lookup
    this.type = type;
    this.none = none;
    
    Collection entities = gedcom.getEntities(type);

    // init list
    if (none!=null) {
      list = new Object[entities.size()+1];
      list[0] = none;
    } else {
      list = new Object[entities.size()];
    }
    Iterator es=entities.iterator();
    for (int e= none!=null ? 1 : 0;e<list.length;e++) {
      Entity ent = (Entity)es.next();
      if (!ent.getTag().equals(type))
        throw new IllegalArgumentException("Type of all entities has to be "+type);
      list[e] = ent;
    }

    // prepare sorting widget
    sortWidget = new PopupWidget();
    ArrayList sorts = new ArrayList();
    for (int i=0;i<SORTS.length;i++) {
      String path = SORTS[i];
      if (!path.startsWith(type))
        continue;
      Sort s = new Sort(path);
      sorts.add(s);
      if (sort==null||path.equals(registry.get("select.sort."+type, ""))) sort = s;
    }
    sortWidget.setActions(sorts);

    // prepare list widget    
    listWidget = new JComboBox();
    listWidget.setMaximumRowCount(16); // 20061020 as suggested by Daniel - show more
    listWidget.setEditable(false);
    listWidget.setRenderer(new Renderer());
    
    // layout
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, listWidget);
    add(BorderLayout.WEST  , sortWidget);

    // init state
    if (sort!=null) 
      sort.trigger();
    if (list.length>0) listWidget.setSelectedIndex(0);
    
    // done
  }
  
  /**
   * Override maximum size - we can't affort to stretch vertically
   */
  public Dimension getMaximumSize() {
    return new Dimension(super.getMaximumSize().width, super.getPreferredSize().height);
  }
  
  /**
   * Override preferred size - we can't affort prefer horizontally based on contents 
   */
  public Dimension getPreferredSize() {
    return new Dimension(128, super.getPreferredSize().height);
  }
  
  /**
   * Number of entities
   */
  public int getEntityCount() {
    return listWidget.getItemCount()-1;
  }
  
  /**
   * The selected entity
   */
  public Entity getSelection() {
    // check selection
    Object item = listWidget.getSelectedItem();
    if (!(item instanceof Entity))
      return null;
    // done
    return (Entity)item;
  }
  
  /**
   * The selected entity
   */
  public void setSelection(Entity set) {
    // fallback to none?
    if (set==null)
      listWidget.setSelectedItem(none!=null ? none : null);
    // applicable?
    if (!(set instanceof Entity)||!set.getTag().equals(type))
      return;
    // set it
    listWidget.setSelectedItem(set);
  }
  
  /**
   * Add a listener
   */
  public void addActionListener(ActionListener listener) {
    listWidget.addActionListener(listener);
  }
  
  /**
   * Remove a listener
   */
  public void removeActionListener(ActionListener listener) {
    listWidget.removeActionListener(listener);
  }
  
  /**
   * Entity Rendering
   */
  private class Renderer extends DefaultListCellRenderer {
    /**
     * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

      // might be text of entity
      String txt;
      if (value instanceof Entity) {
        txt = getString((Entity)value);
      } else {
        txt = value!=null ? value.toString() : "";
      }

      return super.getListCellRendererComponent(list, txt, index, isSelected, cellHasFocus);
    }

    /**
     * generate a string to show for entity&path
     */
    private String getString(Entity e) {
      
      if (sort==null)
        return e.toString();
      
      Property p = e.getProperty(sort.tagPath);
      String value;
      if (p==e)
        value = e.getId();
      else
        value = p!=null&&p.isValid() ? p.getDisplayValue() : "?";
      return value + " / " + e.toString();
    }

  } //Renderer
  
  /**
   * Sort action
   */
  private class Sort extends Action2 {

    /** path */
    private TagPath tagPath;
    
    /**
     * Constructor
     */
    private Sort(String path) {
      
      // path
      tagPath = new TagPath(path);

      // image
      MetaProperty meta;
      if (tagPath.length()>1&&tagPath.getLast().equals(PropertyDate.TAG))
        meta = Grammar.V55.getMeta(new TagPath(tagPath, tagPath.length()-1));
      else
        meta = Grammar.V55.getMeta(tagPath);
      setImage(meta.getImage());
      
      // text
      setText(RESOURCES.getString("select.sort", tagPath.length()==1?"ID":meta.getName()));
      
      // done
    }      
    
    /**
     * @see genj.util.swing.Action2#execute()
     */
    protected void execute() {
      // remember
      sort = this;
      registry.put("select.sort."+type, tagPath.toString());
      // Sort
      Comparator comparator = new PropertyComparator(tagPath);
      Arrays.sort(list, none!=null ? 1 : 0, list.length, comparator);
      // reset our data
      Entity selection = getSelection();
      listWidget.setModel(new DefaultComboBoxModel(list));
      sortWidget.setIcon(getImage());
      sortWidget.setToolTipText(getText());
      setSelection(selection);
    }
        
  } //Sort
   
} //PickEntityWidget
