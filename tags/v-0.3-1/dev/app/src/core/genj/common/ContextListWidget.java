/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2006 Nils Meier <nils@meiers.net>
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

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.view.ContextProvider;
import genj.view.ContextSelectionEvent;
import genj.view.ViewContext;
import genj.window.WindowManager;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import spin.Spin;

/**
 * A widget for rendering a list of contexts
 */
public class ContextListWidget extends JList implements ContextProvider {

  private Gedcom ged;
  
  private Callback callback = new Callback();
  
  /** 
   * Constructor
   */
  public ContextListWidget(Gedcom gedcom) {
    super(new Model());
    ged = gedcom;
    setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    setCellRenderer(callback);
    addListSelectionListener(callback);
  }

  /** 
   * Constructor
   */
  public ContextListWidget(Gedcom gedcom, List contextList) {
    this(gedcom);
    setContextList(contextList);
  }
  
  /**
   * Provides a 'current' context
   */
  public ViewContext getContext() {
    
    Object[] selection = getSelectedValues();
    
    // one selected?
    if (selection.length==1&&selection[0] instanceof ViewContext)
      return (ViewContext)selection[0];
    
    // merge
    ViewContext result = new ViewContext(ged);
    for (int i = 0; i < selection.length; i++) {
      Context context = (Context)selection[i];
      result.addContext(context);
    }
    
    // done
    return result;
  }
  
  /**
   * @see JComponent#addNotify()
   */
  public void addNotify() {
    // let super do its thing
    super.addNotify();
    // listen to gedcom 
    ged.addGedcomListener((GedcomListener)Spin.over(getModel()));
  }
  
  /**
   * @see JComponent#removeNotify()
   */
  public void removeNotify() {
    // disconnect from gedcom
    ged.removeGedcomListener((GedcomListener)Spin.over(getModel()));
    // let super continue
    super.removeNotify();
  }
  
  /**
   * Set the list to show
   */
  public void setContextList(List contextList) {
    ((Model)getModel()).setContextList(contextList);
  }
  
  /**
   * @see JList#setModel(javax.swing.ListModel)
   */
  public void setModel(ListModel model) {
    if (!(model instanceof Model))
      throw new IllegalArgumentException("setModel() n/a");
    super.setModel(model);
  }

  /**
   * @see JList#setListData(java.lang.Object[])
   */
  public void setListData(Object[] listData) {
    throw new IllegalArgumentException("setListData() n/a");
  }
  
  /**
   * @see JList#setListData(java.util.Vector)
   */
  public void setListData(Vector listData) {
    throw new IllegalArgumentException("setListData() n/a");
  }
  
  /**
   * our model
   */
  private static class Model extends AbstractListModel implements GedcomListener {
    
    private List list = new ArrayList();
    
    private void setContextList(List set) {
      // clear old
      int n = list.size();
      list.clear();
      if (n>0)
        fireIntervalRemoved(this, 0, n-1);
      // keep new
      list.addAll(set);
      n = list.size();
      if (n>0)
        fireIntervalAdded(this, 0, n-1);
      // done
    }

    public int getSize() {
      return list.size();
    }

    public Object getElementAt(int index) {
      return list.get(index);
    }
    
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
      // ignore
    }

    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
      for (Iterator it=list.iterator(); it.hasNext(); ) {
        Context context = (Context)it.next();
        context.removeEntities(Collections.singletonList(entity));
      }
      // TODO this could be less coarse grained
      fireContentsChanged(this, 0, list.size());
    }

    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
      // ignore
    }

    public void gedcomPropertyChanged(Gedcom gedcom, Property prop) {
      // TODO this could be less coarse grained
      fireContentsChanged(this, 0, list.size());
    }

    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property removed) {
      for (Iterator it=list.iterator(); it.hasNext(); ) {
        Context context = (Context)it.next();
        context.removeProperties(Collections.singletonList(removed));
      }
      // TODO this could be less coarse grained
      fireContentsChanged(this, 0, list.size());
    }
    
  } //Model
  
  /** 
   * various callbacks in here
   */
  private class Callback extends DefaultListCellRenderer implements ListSelectionListener {
    
    /** propagate selection changes */
    public void valueChanged(ListSelectionEvent e) {
      if (e.getValueIsAdjusting())
        return;
      ViewContext context = getContext();
      if (context!=null)
        WindowManager.broadcast(new ContextSelectionEvent(context, ContextListWidget.this));
    }
    
    /** our patched rendering */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      // let super do its thing
      super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
      // patch up
      Context ctx = (Context)value;
      setIcon(ctx.getImage());
      setText(ctx.getText());
      // done
      return this;
    }
  } //Renderer
}
