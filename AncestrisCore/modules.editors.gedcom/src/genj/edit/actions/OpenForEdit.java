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

import genj.edit.EditViewFactory;
import ancestris.core.resources.Images;
import genj.gedcom.Context;
import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.view.SelectionDispatcher;

/**
 * ActionEdit - edit an entity
 */
public class OpenForEdit extends AbstractAncestrisAction {
  private Context context;
  
  /**
   * Constructor
   */
  public OpenForEdit(Context context) {
    this.context = context;
    setImage(Images.imgView);
    setText(EditViewFactory.NAME);
  }
  
  /**
   * @see genj.util.swing.AbstractAncestrisAction#execute()
   */
  @Override
  public void actionPerformed(ActionEvent event) {
        SelectionDispatcher.fireSelection(context);
//    Workbench.getInstance().openView(EditViewFactory.class, context);
  }
  
} //OpenForEdit

