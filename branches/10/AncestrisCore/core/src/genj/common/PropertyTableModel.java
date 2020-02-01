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
package genj.common;

import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.TagPath;

/**
 * The data source for our PropertyTableWidget
 */
public interface PropertyTableModel {
  
  /**
   * Every table model is associated with a gedcom instance
   */
  public Gedcom getGedcom();
  
  /**
   * Number of rows
   */
  public int getNumRows();
  
  /**
   * Number of columns
   */
  public int getNumCols();

  /**
   * Root-property in given row
   */
  public Property getRowRoot(int row);
  
  /**
   * Path from root to property in given column
   */
  public TagPath getColPath(int col);
  
  /**
   * Column name
   */
  public String getColName(int col);
  
  /** 
   * Add listener
   */
  public void addListener(PropertyTableModelListener listener);
  
  /** 
   * Remove listener
   */
  public void removeListener(PropertyTableModelListener listener);
  
}
