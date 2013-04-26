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
package genj.edit.beans;

import ancestris.core.actions.AncestrisActionProvider;
import ancestris.view.SelectionSink;
import genj.common.SelectEntityWidget;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.gedcom.UnitOfWork;
import ancestris.core.actions.AbstractAncestrisAction;
import genj.util.swing.DialogHelper;
import genj.view.ViewContext;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.Action;
import org.openide.nodes.Node;

/**
 * A proxy for a property that links entities
 */
public class XRefBean extends PropertyBean implements AncestrisActionProvider{

  private Preview preview;
  private PropertyXRef xref;
  
  @Override
    public List<Action> getActions(boolean hasFocus, Node[] nodes) {
        if (!hasFocus){
            return new ArrayList<Action>();
        }
        List<Action> result = new ArrayList<Action>(1);
        if (nodes != null) {
            result.add(new Swivel());
        }
        return result;
    }

  /**
   * swivle a reference pointer
   */
  private class Swivel extends AbstractAncestrisAction {
    public Swivel() {
      setText(RESOURCES.getString("xref.swivel"));
      setImage(MetaProperty.IMG_LINK);
    }
    @Override
    public void actionPerformed(ActionEvent event) {
      
      if (xref==null)
        return;
      
      SelectEntityWidget select = new SelectEntityWidget(xref.getGedcom(), xref.getTargetType(), null);
      if (0!=DialogHelper.openDialog(
          getText(), 
          DialogHelper.QUESTION_MESSAGE, 
          select, 
          AbstractAncestrisAction.okCancel(), 
          event))
        return;

      final Entity newTarget = select.getSelection();
      
      if (xref.getTarget()!=null)
        LOG.fine("Swiveling "+xref.getEntity().getId()+"."+xref.getPath()+" from "+xref.getTarget().getEntity().getId()+" to "+newTarget.getId());
      else
        LOG.fine("Swiveling "+xref.getEntity().getId()+"."+xref.getPath()+" to "+newTarget.getId());
        
      try {
        xref.getGedcom().doUnitOfWork(new UnitOfWork() {
          public void perform(Gedcom gedcom) throws GedcomException {
            Property backpointer = xref.getTarget();
            if (backpointer!=null) {
              xref.unlink();
              backpointer.getParent().delProperty(backpointer);
            }
            xref.setValue("@"+newTarget.getId()+"@");
            xref.link();
          }
        });
      } catch (GedcomException ge) {
        DialogHelper.openDialog(
            getText(), 
            DialogHelper.WARNING_MESSAGE, 
            ge.getMessage(), 
            AbstractAncestrisAction.okOnly(), 
            event);
        LOG.log(Level.FINER, ge.getMessage(), ge);
      }
      
      // done
    }
  }
  
  public XRefBean() {
    
    preview = new Preview();
    
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, preview);
    
    preview.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        // no double-click?
        if (e.getClickCount()<2)
          return;
        // property good? (should)
        if (xref==null)
          return;
        // tell about it
        SelectionSink.Dispatcher.fireSelection(e, new ViewContext(xref));
      }
    });
  }
  
  @Override
  protected void commitImpl(Property property) {
    //noop
  }
  
  /**
   * Nothing to edit
   */  
  public boolean isEditable() {
    return false;
  }
  
  /**
   * Set context to edit
   */
  public void setPropertyImpl(Property prop) {
    
    PropertyXRef xref = (PropertyXRef)prop;
    this.xref = xref;
    
    // set preview
    if (xref!=null&&xref.getTargetEntity()!=null) 
      preview.setEntity(xref.getTargetEntity());
    else
      preview.setEntity(null);
  }
  
  /**
   * Preferred
   */
  public Dimension getPreferredSize() {
    return new Dimension(64,48);
  }

    
} //ProxyXRef
