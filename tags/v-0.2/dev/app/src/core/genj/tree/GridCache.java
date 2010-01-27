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
package genj.tree;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * GridCache - caching information in a 2d grid */
public class GridCache {
  
  /** an empty list */
  private final static List EMPTY = new ArrayList(0);

  /** the grids */
  private Object[][] grid;
 
  /** the system we're spanning */
  private Rectangle2D system;

  /** the resolution we're using */
  private double resolution;
  
  /**
   * Constructor
   */
  public GridCache(Rectangle2D syStem, double resolUtion) {
    // remember
    system = syStem;
    resolution = resolUtion;
    // calc rows/cols
    int 
      cols = (int)Math.ceil(system.getWidth ()/resolution),
      rows = (int)Math.ceil(system.getHeight()/resolution);
    // init grids
    grid = new Object[rows][cols];
    // done
  }
  
  /**
   * Adds an Object to the grid
   */
  public void put(Object object, Rectangle2D range, Point2D pos) {
    // Clip
    int
      scol = (int)Math.floor((range.getMinX()+pos.getX() - system.getMinX())/resolution),
      srow = (int)Math.floor((range.getMinY()+pos.getY() - system.getMinY())/resolution),
      ecol = (int)Math.ceil ((range.getMaxX()+pos.getX() - system.getMinX())/resolution),
      erow = (int)Math.ceil ((range.getMaxY()+pos.getY() - system.getMinY())/resolution);
      
    if (scol>grid[0].length||srow>grid.length||ecol<0||erow<0) return;
    if (srow<0) srow = 0;
    if (erow>grid.length) erow = grid.length;
    if (scol<0) scol = 0;
    if (ecol>grid[0].length) ecol = grid[0].length;
      
    // keep it
    for (int row=srow;row<erow;row++) {
      for (int col=scol;col<ecol;col++) {
        put(object, row, col);
      }
    }
    // done
  }
  
  /**
   * Adds an Object to the grid
   */
  public void put(Object object, int row, int col) {
    // what's there right now?
    Object old = grid[row][col]; 
    if (old==null) {
      // keep as simple entry if possible
      grid[row][col] = object;
    } else {
      // add to existing list or create new list
      if (old instanceof EntryList) {
        ((EntryList)old).add(object);
      } else {
        List l = new EntryList();
        l.add(old);
        l.add(object);
        grid[row][col] = l;
      }
    }
    // done   
  }  

  /**
   * Gets objects by coordinate
   */
  public Set get(Rectangle2D range) {
    
    Set result = new HashSet();

    // Clip
    int
      scol = (int)Math.floor((range.getMinX() - system.getMinX())/resolution),
      srow = (int)Math.floor((range.getMinY() - system.getMinY())/resolution),
      ecol = (int)Math.ceil ((range.getMaxX() - system.getMinX())/resolution),
      erow = (int)Math.ceil ((range.getMaxY() - system.getMinY())/resolution);
      
    if (scol>grid[0].length||srow>grid.length||ecol<0||erow<0) return result;
    if (srow<0) srow = 0;
    if (erow>grid.length) erow = grid.length;
    if (scol<0) scol = 0;
    if (ecol>grid[0].length) ecol = grid[0].length;
      
    // look it
    for (int row=srow;row<erow;row++) {
      for (int col=scol;col<ecol;col++) {
        get(result, row, col);
      }
    }
    
    // done
    return result;
  }

  /**
   * Get the content of a grid cell   */
  public void get(Set set, int row, int col) {
    // what's in the grid?
    Object o = grid[row][col];
    if (o==null) return;
    if (o instanceof EntryList) set.addAll((EntryList)o);
    else set.add(o);
    // done
  }

  /**
   * Our own list used in the grid
   */
  private class EntryList extends ArrayList {
    /**
     * Constructor     */
    private EntryList() {
      super(8);
    }
  } //EntryList       
} //GridCache
