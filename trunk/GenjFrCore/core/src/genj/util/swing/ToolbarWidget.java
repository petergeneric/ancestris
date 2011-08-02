/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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
package genj.util.swing;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * A toolbar with a genj touch
 */
public class ToolbarWidget extends JToolBar {
  
  @Override
  public JButton add(Action a) {
    return patch(super.add(a));
  }
  
  public static JButton patch(JButton button) {
    // no focus please
    button.setRequestFocusEnabled(false);
    button.setFocusable(false);

    // patch disable image
    Icon i = button.getIcon();
    if (i instanceof ImageIcon)
      button.setDisabledIcon( ((ImageIcon)i).getGrayedOut() );
    
    return button;
  }
  

}
