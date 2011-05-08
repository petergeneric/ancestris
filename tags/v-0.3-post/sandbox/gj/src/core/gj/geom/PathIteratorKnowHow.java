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
package gj.geom;

import java.awt.geom.PathIterator;

/**
 * An interface wrapping constants needed for dealing with PathIterator elements
 */
public interface PathIteratorKnowHow {

  /** static values - segment type sizes */    
  public static final int[] SEG_SIZES = {2, 2, 4, 6, 0};
    
  /** static values - segment types */
  public static final byte SEG_MOVETO  = (byte) PathIterator.SEG_MOVETO;
  public static final byte SEG_LINETO  = (byte) PathIterator.SEG_LINETO;
  public static final byte SEG_QUADTO  = (byte) PathIterator.SEG_QUADTO;
  public static final byte SEG_CUBICTO = (byte) PathIterator.SEG_CUBICTO;
  public static final byte SEG_CLOSE   = (byte) PathIterator.SEG_CLOSE;

  /** static values - segment names */
  public final static String[] SEG_NAMES = {
    "moveto", "lineto", "quadto", "cubicto", "close",
  };

}

