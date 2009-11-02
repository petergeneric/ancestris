/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2009 Nils Meier
 * 
 * GraphJ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GraphJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphJ; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package gj.shell.swing;

import javax.swing.JComponent;
import javax.swing.JPanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * A Layout extension for java.swing.GridBagLayout
 */
public class GBLayout {
  
  /** the layout */
  private GridBagLayout layout = new GridBagLayout();
  
  /** the panel */
  private JPanel panel;
  
  /**
   * Constructor
   */
  public GBLayout(JPanel panel) {
    this.panel = panel;
    panel.removeAll();
    panel.setLayout(layout);
  }
  
  /**
   * Adds a component
   */
  public void add(JComponent component, int x, int y, int w, int h, boolean growx, boolean growy, boolean fillx, boolean filly) {

    // add to panel
    panel.add(component);
    
    // do the constraint
    GridBagConstraints constraints = new GridBagConstraints(
      x,y,w,h,
      growx ? 1 : 0,
      growy ? 1 : 0,
      GridBagConstraints.WEST,
      fillx&&filly ? GridBagConstraints.BOTH : (fillx?GridBagConstraints.HORIZONTAL:GridBagConstraints.VERTICAL)
      ,new Insets(0,0,0,0),0,0);
      
    layout.setConstraints(component,constraints);
    
    // done
  }

}
