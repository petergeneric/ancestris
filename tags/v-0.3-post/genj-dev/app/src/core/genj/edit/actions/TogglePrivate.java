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

import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.util.swing.Action2;
import genj.view.ViewManager;
import genj.window.WindowManager;

import java.util.Collection;
import java.util.Iterator;

/**
 * TogglePrivate - toggle "private" of a property
 */
public class TogglePrivate extends AbstractChange {
  
  /** the properties */
  private Collection properties;
  
  /** make public or private */
  private boolean makePrivate;
  
  /**
   * Constructor
   */
  public TogglePrivate(Gedcom gedcom, Collection properties, ViewManager mgr) {
    super(gedcom, MetaProperty.IMG_PRIVATE, "", mgr);
    this.gedcom = gedcom;
    this.properties = properties;
    
    // assuming we want to make them all private
    makePrivate = true;
    for (Iterator ps = properties.iterator(); ps.hasNext();) {
      Property p = (Property) ps.next();
      if (p.isPrivate()) makePrivate = false;
    }
    setText(resources.getString(makePrivate?"private":"public"));
  }
  
  public void perform(Gedcom gedcom) throws GedcomException {
    
    // check if that's something we can do
    String pwd = gedcom.getPassword();
    if (pwd==Gedcom.PASSWORD_UNKNOWN) {
        WindowManager.getInstance(getTarget()).openDialog(null,getText(),WindowManager.WARNING_MESSAGE,"This Gedcom file contains encrypted information that has to be decrypted before changing private/public status of other information",Action2.okOnly(),getTarget());
        return;              
    }
      
    // check gedcom
    if (pwd==Gedcom.PASSWORD_NOT_SET) {
      
      pwd = WindowManager.getInstance(getTarget()).openDialog(
        null,
        getText(),
        WindowManager.QUESTION_MESSAGE,
        AbstractChange.resources.getString("password", gedcom.getName()),
        "",
        getTarget() 
      );
      
      // canceled?
      if (pwd==null)
        return;
    }

    // check if the user wants to do it recursively
    int recursive = WindowManager.getInstance(getTarget()).openDialog(null,getText(),WindowManager.QUESTION_MESSAGE,AbstractChange.resources.getString("recursive"), Action2.okCancel(),getTarget());

    // change it
    gedcom.setPassword(pwd); 
    
    for (Iterator ps = properties.iterator(); ps.hasNext();) {
      Property p = (Property) ps.next();
      p.setPrivate(makePrivate, recursive==0);
    }

    // done
  }
  
} //TogglePrivate

