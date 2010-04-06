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
package gj.layout.graph.tree;

import static gj.geom.Geometry.getMax;
import static gj.geom.Geometry.getPoint;
import static gj.geom.Geometry.getRadian;
import static gj.geom.ShapeHelper.getCenter;
import static gj.util.LayoutHelper.getNormalizedEdges;
import static gj.util.LayoutHelper.getOther;
import static gj.util.LayoutHelper.getPort;
import static gj.util.LayoutHelper.getRouting;
import gj.geom.Geometry;
import gj.layout.Graph2D;
import gj.layout.GraphNotSupportedException;
import gj.layout.LayoutContext;
import gj.layout.Port;
import gj.layout.Routing;
import gj.layout.edge.visibility.EuclideanShortestPathLayout;
import gj.model.Edge;
import gj.model.Vertex;
import gj.util.LayoutHelper;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Our edge control
 */
public enum EdgeLayout {
  
  Polyline { 
    @Override protected Routing routing(Graph2D graph2d, Vertex parent, Edge edge, Vertex child, int i, int j, TreeLayout layout) {
      return getRouting(edge, graph2d);
    }
  },
  
  PortPolyline {
    
    /** vertex port side for current orientation */
    private Port side(TreeLayout layout) {
      if (layout.getOrientation()==0)
        return Port.North;
      if (layout.getOrientation()==180)
        return Port.South;
      if (layout.getOrientation()==90)
        return Port.East;
      if (layout.getOrientation()==270)
        return Port.West;
      // only support NWES
      return Port.None;
    }

    @Override protected Routing routing(Graph2D graph2d, Vertex parent, Edge edge, Vertex child, int i, int j, TreeLayout layout) {

      // FIXME ports are wrong for re-ordered children
      // FIXME port polyline count for destinations isn't correct for acyclic DAGs

      Port side = side(layout);
      if (edge.getEnd().equals(parent))
        side = side.opposite();
      return getRouting(
          graph2d.getShape(edge.getStart()), getPort(graph2d.getShape(edge.getStart()), i, j, side           ),
          graph2d.getShape(edge.getEnd  ()), getPort(graph2d.getShape(edge.getEnd  ()), 0, 1, side.opposite())
      );
    }
  },
  
  Orthogonal {
    @Override protected Routing routing(Graph2D graph2d, Vertex parent, Edge edge, Vertex child, int i, int j, TreeLayout layout) {
      
      double layoutAxis = getRadian(layout.getOrientation());
      
      Point2D s = getCenter(graph2d.getShape(parent));
      Point2D e = getCenter(graph2d.getShape(child));
      
      Point2D[] points;
      Point2D c;
      switch (layout.getAlignmentOfParents()) {
        case LeftOffset:
        case RightOffset:
          c = Geometry.getIntersection(s, layoutAxis-Geometry.QUARTER_RADIAN, e, layoutAxis);
          points = new Point2D[]{s, c, e};
          break;
        default:
          c = getPoint(getMax(graph2d.getShape(parent), layoutAxis), layoutAxis, layout.getDistanceBetweenGenerations()/2);
          points = new Point2D[]{s, 
              Geometry.getIntersection(s, layoutAxis, c, layoutAxis-Geometry.QUARTER_RADIAN),
              Geometry.getIntersection(e, layoutAxis, c, layoutAxis-Geometry.QUARTER_RADIAN),
              e};
      }
      return getRouting(points, graph2d.getShape(parent), graph2d.getShape(child), !edge.getStart().equals(parent));
    }
  };
  
  /** the layout specific routing */
  protected abstract Routing routing(Graph2D graph2d, Vertex parent, Edge edge, Vertex child, int i, int j, TreeLayout layout);

  /** apply */
  protected void apply(Graph2D graph2d, Branch branch, TreeLayout layout, LayoutContext context)  throws GraphNotSupportedException {
    
    // layout edges
    Vertex parent = branch.getRoot();
    List<Edge> edges = getNormalizedEdges(parent);
    int i = 0;
    int j = LayoutHelper.getOutDegree(parent);
    for (Edge edge : edges) {
      
      // an outgoing edge
      if (edge.getStart().equals(parent))
        apply(graph2d, branch, parent, edge, i++, j, layout, context);
      
      // recurse
      for (Branch sub : branch.getBranches())
        apply(graph2d, sub, layout, context);
    }
    
    // done
  }

  /** apply to one edge */
  protected void apply(Graph2D graph2d, Branch branch, Vertex parent, Edge edge, int i, int j, TreeLayout layout, LayoutContext context) throws GraphNotSupportedException {
    
    Vertex child = getOther(edge, parent);
    
    // a contained child?
    for (Branch sub : branch.getBranches()) {
      if (sub.getRoot().equals(child)) {
        graph2d.setRouting(edge, routing(graph2d, parent, edge, child, i, j, layout));
        return;
      }
    }

    // an edge we have to route independently
    context.getLogger().info("Routing edge ["+edge+"] via EuclideanShortestPathLayout");
    new EuclideanShortestPathLayout( Math.min(layout.getDistanceBetweenGenerations(), layout.getDistanceInGeneration())/4 )
        .apply(edge, graph2d, context);
  
    // done
  }
  
} //EdgeLayout

