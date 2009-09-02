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
package gj.layout;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;

/**
 * A context for a layout which basically offers optional information
 */
public interface LayoutContext {

  /**
   * the preferred bound for the result of the layout
   */
  public Rectangle2D getPreferredBounds();
  
  /**
   * whether debugging information is expected 
   */
  public boolean isDebug();
  
  /**
   * add a debug shape
   */
  public void addDebugShape(Shape shape);
  
  /**
   * access to logger
   */
  public Logger getLogger();

}
