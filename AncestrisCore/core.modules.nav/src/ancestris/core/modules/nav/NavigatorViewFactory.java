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
package ancestris.core.modules.nav;

import genj.util.swing.ImageIcon;
import genj.view.View;
import genj.view.ViewFactory;
import org.openide.util.NbBundle;


/**
 * The factory for the TableView
 */
public class NavigatorViewFactory implements ViewFactory {

  private final static ImageIcon img = new ImageIcon(NavigatorViewFactory.class,"View");

  /**
   * @see genj.view.ViewFactory#createView(String, Gedcom, Registry)
   */
    @Override
  public View createView() {
    return new NavigatorView();
  }
  
  /**
   * @see genj.view.ViewFactory#getImage()
   */
    @Override
  public ImageIcon getImage() {
    return img;
  }
  
  /**
   * @see genj.view.ViewFactory#getName(boolean)
   */
    @Override
  public String getTitle() {
    return NbBundle.getMessage(NavigatorViewFactory.class,"title");
  }
  
  
  @Override
  public String getTooltip() {
    return NbBundle.getMessage(NavigatorViewFactory.class,"tooltip");
  }

  

} //NavigatorViewFactory
