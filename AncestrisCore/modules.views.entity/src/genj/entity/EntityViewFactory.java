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
package genj.entity;

import genj.util.swing.ImageIcon;
import genj.view.View;
import genj.view.ViewFactory;


/**
 * The factory for the EntityView
 */
public class EntityViewFactory implements ViewFactory {

  /**
   * @see genj.view.ViewFactory#createView(String, Gedcom, Registry)
   */
  public View createView() {
    return new EntityView();
  }

  /**
   * @see genj.view.ViewFactory#getImage()
   */
  public ImageIcon getImage() {
    return new ImageIcon(this, "images/View");
  }

  /**
   * @see genj.view.ViewFactory#getName(boolean)
   */
  public String getTitle() {
    return EntityView.resources.getString("title");
  }

  @Override
  public String getTooltip() {
    return EntityView.resources.getString("tooltip");
  }

  
} //EntityViewFactory
