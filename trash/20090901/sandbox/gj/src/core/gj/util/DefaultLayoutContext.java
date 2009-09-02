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
package gj.util;

import gj.layout.LayoutContext;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * A default implementation of a layout context
 */
public class DefaultLayoutContext implements LayoutContext {
  
  private Collection<Shape> debugShapes = null;
  private Logger logger;
  private Rectangle2D preferredBounds;
  
  private final static Logger DEFAULTLOGGER = Logger.getLogger("layout");
  
  /**
   * Constructor
   */
  public DefaultLayoutContext() {
    this(null, DEFAULTLOGGER, null);
  }
  
  /**
   * Constructor
   */
  public DefaultLayoutContext(Collection<Shape> debugShape, Logger logger, Rectangle2D preferredBounds) {
    this.debugShapes = debugShape;
    this.logger = logger;
    this.preferredBounds = preferredBounds;
  }

  /**
   * whether to debug or not
   */
  public boolean isDebug() {
    return debugShapes!=null;
  }
  
  /** 
   * debug shapes
   */
  public void addDebugShape(Shape shape) {
    if (debugShapes!=null)
      debugShapes.add(shape);  
  }

  /**
   * logger
   */
  public Logger getLogger() {
    return logger;
  }

  /**
   * preferred bounds
   */
  public Rectangle2D getPreferredBounds() {
    return preferredBounds;
  }


}
