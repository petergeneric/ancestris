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
package genj.edit.actions;

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.util.swing.NestedBlockLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Add a new entity  
 */
public class CreateEntity extends AbstractChange {

  /** the type of the added entity*/
  private String etag;
  
  /** text field for entering id */
  private JTextField requestID;
  
  /**
   * Constructor
   */
  public CreateEntity(Gedcom ged, String tag) {
    super(ged, Gedcom.getEntityImage(tag).getOverLayed(imgNew), resources.getString("new", Gedcom.getName(tag, false) ));
    etag = tag;
  }
  
  /**
   * Override content components to show to user 
   */
  protected JPanel getDialogContent() {
    
    JPanel result = new JPanel(new NestedBlockLayout("<col><row><text wx=\"1\" wy=\"1\"/></row><row><check/><text/></row></col>"));

    // prepare id checkbox and textfield
    requestID = new JTextField(gedcom.getNextAvailableID(etag), 8);
    requestID.setEditable(false);
    
    final JCheckBox check = new JCheckBox(resources.getString("assign_id"));
    check.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        requestID.setEditable(check.isSelected());
        if (check.isSelected())  requestID.requestFocusInWindow();
      }
    });
    
    result.add(getConfirmComponent());
    result.add(check);
    result.add(requestID);
    
    // done
    return result;
  }
  
  /**
   * @see genj.edit.EditViewFactory.Change#getConfirmMessage()
   */
  protected String getConfirmMessage() {
    // You are about to create a {0} in {1}!
    String about = resources.getString("confirm.new", new Object[]{ Gedcom.getName(etag,false), gedcom});
    // This entity will not be connected ... 
    String detail = resources.getString("confirm.new.unrelated");
    // done
    return about + '\n' + detail;
  }
  
  /**
   * @see genj.edit.EditViewFactory.Change#change()
   */
  protected Context execute(Gedcom gedcom, ActionEvent event) throws GedcomException {
    // check id
    String id = null;
    if (requestID.isEditable()) {
      id = requestID.getText();
      if (gedcom.getEntity(etag, id)!=null)
        throw new GedcomException(resources.getString("assign_id_error", id));
    }
    // create the entity
    Entity entity = gedcom.createEntity(etag, id);
    entity.addDefaultProperties();
    // done
    return new Context(entity);
  }
  
} //Create

