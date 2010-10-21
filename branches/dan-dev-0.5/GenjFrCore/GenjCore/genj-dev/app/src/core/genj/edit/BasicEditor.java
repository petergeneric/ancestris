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

import genj.edit.actions.EditNote;
import genj.edit.actions.EditSource;
import genj.edit.beans.PropertyBean;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.util.Registry;
import genj.view.ContextProvider;
import genj.view.SelectionSink;
import genj.view.ViewContext;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.FocusManager;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * The basic version of an editor for a entity. Tries to hide Gedcom complexity from the user while being flexible in what it offers to edit information pertaining to an entity.
 */
/* package */class BasicEditor extends Editor implements SelectionSink, ContextProvider {

  final static Registry REGISTRY = Registry.get(BasicEditor.class);
  
  /** our gedcom */
  private Gedcom gedcom = null;

  /** current entity */
  private Entity currentEntity = null;

  /** edit */
  private EditView view;
  
  /** current panels */
  private BeanPanel beanPanel;
  
  private boolean isIgnoreSetContext = false;
  
  /**
   * Constructor
   */
  public BasicEditor(Gedcom gedcom, EditView edit) {

    // remember
    this.gedcom = gedcom;
    this.view = edit;
    
    // create panel for beans
    beanPanel = new BeanPanel();
    beanPanel.addChangeListener(changes);

    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new JScrollPane(beanPanel));
    
    // done
  }
  
  /**
   * By being a selection sink ourselves we can make sure that non-context-entity
   * properties don't leak through
   */
  public void fireSelection(Context context, boolean isActionPerformed) {
    if (isActionPerformed || (context.getEntities().size()==1 && context.getEntity()==currentEntity))
      SelectionSink.Dispatcher.fireSelection(this, context, isActionPerformed);
  }

  /**
   * Callback - our current context
   */
  public ViewContext getContext() {
    // try to find a bean with focus
    PropertyBean bean = getFocus();
    if (bean!=null&&bean.getContext()!=null) 
      return bean.getContext();
    // currently edited?
    if (currentEntity!=null)
      return new ViewContext(currentEntity);
    // gedcom at least
    return new ViewContext(gedcom);
  }

  /**
   * Callback - set current context
   */
  @Override
  public void setContext(Context context) {
    
    if (isIgnoreSetContext)
      return;
    
    actions.clear();
    
    // clear?
    if (context.getGedcom()==null) {
      setEntity(null, null);
      return;
    }
    
    // existing changes or a different entity to look at?
    if (changes.hasChanged() || currentEntity != context.getEntity()) {
      
      // change entity being edited
      setEntity(context.getEntity(), context.getProperty());
      
    } else {

      // simply change focus if possible
      if (beanPanel!=null && view.isGrabFocus()) {
        if (context.getProperties().size()==1)
          beanPanel.select(context.getProperty());
        else if (context.getProperties().isEmpty()&&context.getEntities().size()==1)
          beanPanel.select(context.getEntity());
      }      
    }
    
    // grab actions
    actions.addAll(beanPanel.getActions());
    
    // done
  }
  
  @Override
  public void commit() throws GedcomException {
    
    // commit changes (without listing to the change itself)
    try {
      isIgnoreSetContext = true;
      beanPanel.commit();
    } finally {
      isIgnoreSetContext = false;
    }
    
    // lookup current focus now (any temporary props are committed now)
    PropertyBean focussedBean = getFocus();
    Property focus = focussedBean !=null ? focussedBean.getProperty() : null;
    
    // set selection
    if (view.isGrabFocus())
      beanPanel.select(focus);

  }
  
  /**
   * Set current entity
   */
  public void setEntity(Entity set, Property focus) {
    
    // remember
    currentEntity = set;
    
    // try to find focus receiver if need be
    if (focus==null) {
      // last bean's property would be most appropriate
      PropertyBean bean = getFocus();
      if (bean!=null&&bean.getProperty()!=null&&bean.getProperty().getEntity()==currentEntity) focus  = bean.getProperty();
      // fall back to entity itself
      if (focus==null) focus = currentEntity;
    }
    
    // remove all we've setup to this point
    beanPanel.setRoot(currentEntity);
    
    // add some actions
    if (currentEntity!=null && currentEntity.getMetaProperty().allows(Gedcom.NOTE))
      actions.add(new EditNote(currentEntity));

    // add some actions
    if (currentEntity!=null && currentEntity.getMetaProperty().allows(Gedcom.SOUR))
      actions.add(new EditSource(currentEntity));

    // set focus
    if (focus!=null && view.isGrabFocus())
      beanPanel.select(focus);

    // start change tracking
    changes.setChanged(false);
    
    // done
  }
  
  /**
   * Find currently focussed PropertyBean
   */
  private PropertyBean getFocus() {
    
    Component focus = FocusManager.getCurrentManager().getFocusOwner();
    while (focus!=null&&!(focus instanceof PropertyBean))
      focus = focus.getParent();
    
    if (focus==null)
      return null;
    
    return SwingUtilities.isDescendingFrom(focus, this) ? (PropertyBean)focus : null;

  }
  
} //BasicEditor
