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

/**
 * Interface to a graph layout  
 */
public interface GraphLayout {

  /** 
   * Applies the layout to a given graph
   * @param graph2d the graph to layout
   * @param context bounds to adhere to if possible (not guaranteed)
   * @return resulting bounds 
   */
  public Shape apply(Graph2D graph2d, LayoutContext context) throws LayoutException;
  
} //Layout
