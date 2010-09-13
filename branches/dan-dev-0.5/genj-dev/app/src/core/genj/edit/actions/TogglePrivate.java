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
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;

import java.awt.event.ActionEvent;
import java.util.Collection;

/**
 * TogglePrivate - toggle "private" of a property
 */
public class TogglePrivate extends AbstractChange {
  
  /** the properties */
  private Collection<? extends Property> properties;
  
  /** make public or private */
  private boolean makePrivate;
  
  /**
   * Constructor
   */
  public TogglePrivate(Gedcom gedcom, Collection<? extends Property> properties) {
    super(gedcom, MetaProperty.IMG_PRIVATE, "");
    this.gedcom = gedcom;
    this.properties = properties;
    
    // assuming we want to make them all private
    makePrivate = true;
    for (Property p : properties)
      if (p.isPrivate()) makePrivate = false;
    setText(resources.getString(makePrivate?"private":"public"));
  }
  
  protected Context execute(Gedcom gedcom, ActionEvent event) throws GedcomException {
    
    // check if that's something we can do
    String pwd = gedcom.getPassword();
    if (pwd==Gedcom.PASSWORD_UNKNOWN) {
        DialogHelper.openDialog(
            getText(),
            DialogHelper.WARNING_MESSAGE,
            "This Gedcom file contains encrypted information that has to be decrypted before changing private/public status of other information",
            Action2.okOnly(),
            event);
        return null;              
    }
      
    // check gedcom
    if (pwd==null) {
      
      pwd = DialogHelper.openDialog(
        getText(),
        DialogHelper.QUESTION_MESSAGE,
        AbstractChange.resources.getString("password", gedcom.getName()),
        "",
        event 
      );
      
      // canceled?
      if (pwd==null)
        return null;
    }

    // check if the user wants to do it recursively
    int recursive = DialogHelper.openDialog(
        getText(),
        DialogHelper.QUESTION_MESSAGE,
        AbstractChange.resources.getString("recursive"),
        Action2.yesNo(), 
        event);

    // change it
    gedcom.setPassword(pwd); 
    
    for (Property p : properties)
      p.setPrivate(makePrivate, recursive==0);

    // done
    return null;
  }
  
} //TogglePrivate

