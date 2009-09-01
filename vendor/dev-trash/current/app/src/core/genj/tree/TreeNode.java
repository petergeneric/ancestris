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

import gj.layout.tree.Branch;
import gj.layout.tree.NodeOptions;
import gj.layout.tree.Orientation;
import gj.model.Node;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A node in our genealogy tree */
/*package*/ class TreeNode implements Node, NodeOptions {
  
  /** no padding */
  private final static int[] NO_PADDING = new int[4];

  /** the content */
  /*package*/ Object content;
  
  /** arcs of this entity */
  /*package*/ List arcs = new ArrayList(5);
  
  /** position of this entity */
  /*package*/ Point pos = new Point();
  
  /** the shape */
  /*package*/ Shape shape;
  
  /** padding */
  /*package*/ int[] padding;
  
  /** alignment switch */
  /*package*/ int align = 0;
  
  /**
   * Constructor
   */
  /*package*/ TreeNode(Object cOntent, Shape sHape, int[] padDing) {
    // remember
    content = cOntent;
    shape = sHape;
    padding = padDing!=null ? padDing : NO_PADDING;
    // done
  }
  
  /**
   * @see gj.model.Node#getArcs()
   */
  public List getArcs() {
    return arcs;
  }

  /**
   * @see gj.model.Node#getContent()
   */
  public Object getContent() {
    return content;
  }

  /**
   * @see gj.model.Node#getPosition()
   */
  public Point2D getPosition() {
    return pos;
  }

  /**
   * @see gj.model.Node#getShape()
   */
  public Shape getShape() {
    return shape;
  }
  
  /**
   * @see gj.layout.tree.NodeOptions#getLongitude(gj.model.Node, gj.layout.tree.Branch[], gj.layout.tree.Orientation)
   */
  public int getLongitude(Node node, Branch[] children, Orientation o) {
    // centered
    if (align==0) 
      return Branch.getLongitude(children, 0.5, o);
    // west of branches
    if (align<0)
      return Branch.getMaxLongitude(children) + align;
    // east of branches
    return Branch.getMinLongitude(children) + align;
  }
  
  /**
   * @see gj.layout.tree.NodeOptions#getPadding(gj.model.Node, gj.layout.tree.Orientation)
   */
  public int[] getPadding(Node node, Orientation o) {
    return padding;
  }

} //TreeNode

