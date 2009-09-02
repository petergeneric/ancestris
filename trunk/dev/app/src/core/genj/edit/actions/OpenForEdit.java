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

import genj.edit.EditView;
import genj.edit.EditViewFactory;
import genj.edit.Images;
import genj.util.swing.Action2;
import genj.view.ViewContext;
import genj.view.ViewHandle;
import genj.view.ViewManager;

/**
 * ActionEdit - edit an entity
 */
public class OpenForEdit extends Action2 {
  /** the context to edit */
  private ViewContext context;
  /** the view manager */
  private ViewManager manager;
  /**
   * Constructor
   */
  public OpenForEdit(ViewContext ctxt, ViewManager mgr) {
    manager = mgr;
    context = ctxt;
    setImage(Images.imgView);
    setText(AbstractChange.resources.getString("edit"));
  }
  /**
   * @see genj.util.swing.Action2#execute()
   */
  protected void execute() {

    // open an EditView that isn't sticky - we have to
    // sequentially open each edit until we find a non-sticky one
    ViewHandle handle;
    while (true) {
	    handle = manager.openView(EditViewFactory.class, context.getGedcom());
	    if (!((EditView)handle.getView()).isSticky()) 
	      break;
    }
    
    // make sure the context change follows through
    ((EditView)handle.getView()).setContext(context);
  }
  
} //OpenForEdit

