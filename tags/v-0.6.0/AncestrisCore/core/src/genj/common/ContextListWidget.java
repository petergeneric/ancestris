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
import genj.view.SelectionSink;
import genj.view.ViewContext;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import spin.Spin;

/**
 * A widget for rendering a list of contexts
 */
public class ContextListWidget extends JList implements ContextProvider {

  private Gedcom gedcom;
  private Callback callback = new Callback();
  private List<? extends Context> contexts = new ArrayList<Context>();
  
  /** 
   * Constructor
   */
  public ContextListWidget(List<? extends Context> list) {
    
    setModel(new Model(list));
    
    setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    setCellRenderer(callback);
    addListSelectionListener(callback);
    
  }

  public List<? extends Context> getContexts() {
    return contexts;
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
    List<Property> props = new ArrayList<Property>(16);
    List<Entity> ents = new ArrayList<Entity>(16);
    
    for (int i = 0; i < selection.length; i++) {
      Context context = (Context)selection[i];
      props.addAll(context.getProperties());
      ents.addAll(context.getEntities());
    }
    
    // done
    return new ViewContext(new Context(gedcom, ents, props));
  }
  
  /**
   * Component added is listening to gedcom
   */
  @Override
  public void addNotify() {
    // let super do its thing
    super.addNotify();
    // listen to gedcom 
    gedcom.addGedcomListener((GedcomListener)Spin.over(getModel()));
  }
  
  /**
   * Component removed is not listening to gedcom
   */
  @Override
  public void removeNotify() {
    // disconnect from gedcom
    gedcom.removeGedcomListener((GedcomListener)Spin.over(getModel()));
    // let super continue
    super.removeNotify();
  }
  
  /**
   * our model
   */
  private class Model extends AbstractListModel implements GedcomListener {
    
    private List<Context> list = new ArrayList<Context>();
    
    private Model(List<? extends Context> set) {
      for (Context context : set) {
        list.add(context);
        if (gedcom==null)
          gedcom = context.getGedcom();
        else if (gedcom!=context.getGedcom())
          throw new IllegalArgumentException(gedcom+"!="+context.getGedcom());
      }
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
      for (ListIterator<Context> it = list.listIterator(); it.hasNext(); ) {
        Context context = it.next();
        if (context.getEntities().contains(entity))
          it.set(new Context(context.getGedcom()));
      }
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
      for (ListIterator<Context> it=list.listIterator(); it.hasNext(); ) {
        Context context = it.next();
        if (context.getProperties().contains(property)) {
          if (context instanceof ViewContext)
            it.set(new ViewContext( ((ViewContext)context).getText(), ((ViewContext)context).getImage(), new Context(context.getGedcom())));
          else
            it.set(new Context(context.getGedcom()));
        }
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
      Context context = getContext();
      if (context!=null)
    	  SelectionSink.Dispatcher.fireSelection(ContextListWidget.this,context, false);
    }
    
    /** our patched rendering */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      // let super do its thing
      super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
      // patch up
      if (value instanceof ViewContext) {
        ViewContext ctx = (ViewContext)value;
        setIcon(ctx.getImage());
        setText(ctx.getText());
      } else {
        setIcon(Gedcom.getImage());
        setText(value.toString());
      }
      // done
      return this;
    }
  } //Renderer
}
