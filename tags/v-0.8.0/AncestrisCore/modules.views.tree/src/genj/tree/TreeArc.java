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

import gj.awt.geom.Path;
import gj.layout.tree.ArcOptions;
import gj.layout.tree.Orientation;
import gj.model.Arc;
import gj.model.Node;

/**
 * An arc in our genealogy tree
 */
/*package*/ class TreeArc implements Arc, ArcOptions {
  
  /** start */
  private TreeNode start;
   
  /** end */
  private TreeNode end;
   
  /** path */
  private Path path;
  
  /**
   * Constructor
   * @param n1 first node
   * @param n2 second node
   * @param p whether the arc should be visible (has a path) or not
   */
  /*package*/ TreeArc(TreeNode n1, TreeNode n2, boolean p) {
    // remember
    start = n1;
    end   = n2;
    if (p) path = new Path();
    // register
    n1.arcs.add(this);
    n2.arcs.add(this);
    // done  
  }
  /**
   * @see gj.model.Arc#getEnd()
   */
  public Node getEnd() {
    return end;
  }
  /**
   * @see gj.model.Arc#getStart()
   */
  public Node getStart() {
    return start;
  }
  /**
   * @see gj.model.Arc#getPath()
   */
  public Path getPath() {
    return path;
  }
  
  /**
   * @see gj.layout.tree.ArcOptions#getPort(gj.model.Arc, gj.model.Node)
   */
  public Point2D getPort(Arc arc, Node node, Orientation o) {
    return node.getPosition();
  }

} //TreeArc
  