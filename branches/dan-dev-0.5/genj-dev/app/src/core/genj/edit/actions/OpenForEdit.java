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

import java.awt.event.ActionEvent;

import genj.app.Workbench;
import genj.edit.EditViewFactory;
import genj.edit.Images;
import genj.gedcom.Context;
import genj.util.swing.Action2;

/**
 * ActionEdit - edit an entity
 */
public class OpenForEdit extends Action2 {
  private Context context;
  private Workbench workbench;
  
  /**
   * Constructor
   */
  public OpenForEdit(Workbench workbench, Context context) {
    this.context = context;
    this.workbench = workbench;
    setImage(Images.imgView);
    setText(EditViewFactory.NAME);
  }
  
  /**
   * @see genj.util.swing.Action2#execute()
   */
  public void actionPerformed(ActionEvent event) {
    workbench.openView(EditViewFactory.class, context);
  }
  
} //OpenForEdit

