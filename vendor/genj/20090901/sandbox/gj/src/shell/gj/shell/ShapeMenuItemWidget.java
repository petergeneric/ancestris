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
package gj.shell;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;

import javax.swing.Action;
import javax.swing.JMenuItem;

/**
 * A Widget that shows a Shape
 */
public class ShapeMenuItemWidget extends JMenuItem {
  
  /** the shape we're representing */
  private Shape shape;
  
  /**
   * Constructor
   */
  public ShapeMenuItemWidget(Shape shape, Action action) {
    super(action);
    this.shape = shape;
  }

  /**
   * @see java.awt.Component#getPreferredSize()
   */
  @Override
  public Dimension getPreferredSize() {
    Dimension size = shape.getBounds().getSize();
    return new Dimension(size.width+5, size.height+5);
  }

  /**
   * @see javax.swing.JComponent#paintComponents(Graphics)
   */
  @Override
  public void paintComponent(Graphics g) {
    
    super.paintComponent(g);

    Dimension size = getSize();
    Graphics2D graphics = (Graphics2D)g;
    
    graphics.setColor(Color.black);
    graphics.translate(size.width/2, size.height/2);
    graphics.draw(shape);
    graphics.translate(-size.width/2, -size.height/2);
  }

}
