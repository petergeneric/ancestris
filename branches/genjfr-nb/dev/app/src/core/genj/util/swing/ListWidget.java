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
package genj.util.swing;


import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Our own version of a list
 */
public class ListWidget extends JList {
  
  /** the renderer we use */
  private static final ListCellRenderer RENDERER = new Renderer();

  /**
   * @see javax.swing.JList#getCellRenderer()
   */
  public ListCellRenderer getCellRenderer() {
    return RENDERER;
  }
  
  /**
   * our specialized renderer
   */  
  private static class Renderer extends DefaultListCellRenderer {
    /**
     * we know about action delegates and will use that here if applicable
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value instanceof Action2) {
        Action2 action = (Action2)value; 
        setText(action.getText());
        setIcon(action.getImage());
      }
      return this;
    }
  } //Renderer
  
} // JList

