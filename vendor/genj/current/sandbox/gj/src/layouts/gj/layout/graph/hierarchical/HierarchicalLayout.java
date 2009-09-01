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
package gj.layout.graph.hierarchical;

import static gj.geom.Geometry.getBounds;
import static gj.util.LayoutHelper.getPort;
import gj.geom.ShapeHelper;
import gj.layout.Graph2D;
import gj.layout.GraphLayout;
import gj.layout.LayoutContext;
import gj.layout.LayoutException;
import gj.layout.Port;
import gj.layout.graph.hierarchical.LayerAssignment.DummyVertex;
import gj.layout.graph.hierarchical.LayerAssignment.Routing;
import gj.model.Edge;
import gj.model.Vertex;
import gj.util.LayoutHelper;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A hierarchical layout
 */
public class HierarchicalLayout implements GraphLayout {

  private boolean pickPorts = true;
  private double distanceBetweenLayers = 20; 
  private double distanceBetweenVertices= 20; 
  private boolean isSinksAtBottom = true;
  private double alignmentOfLayers = 0.5;
  private Comparator<Vertex> orderOfVerticesInLayer = null;
  private VertexPositioning positioning = VertexPositioning.Simplex;
  
  /**
   * do the layout
   */
  public Shape apply(Graph2D graph2d, LayoutContext context) throws LayoutException {

    // empty case?
    if (graph2d.getVertices().isEmpty())
      return new Rectangle2D.Double();
    
    // wrap layout into dummy aware one
    graph2d = new DummyAwareGraph2D(graph2d);
    
    // 1st step - calculate layering
    LayerAssignment layerAssignment = new LongestPathLA();
    layerAssignment.assignLayers(graph2d, orderOfVerticesInLayer);
    
    context.getLogger().fine("Layer assignment with "+layerAssignment.getHeight()+" layers, maximum width "+layerAssignment.getWidth()+", "+layerAssignment.getNumDummyVertices()+" dummy vertices");
    
    // 2nd step - crossing reduction
    new LayerByLayerSweepCR().reduceCrossings(layerAssignment);
    
    // 3rd step - vertex positioning and edge routing
    return new CompactVertexPositioning().apply(graph2d, layerAssignment);
  }


  /**
   * Accessor - distance between layers
   */
  public void setDistanceBetweenLayers(double distanceBetweenLayers) {
    this.distanceBetweenLayers = distanceBetweenLayers;
  }

  /**
   * Accessor - distance between layers
   */
  public double getDistanceBetweenLayers() {
    return distanceBetweenLayers;
  }

  /**
   * Accessor - distance between verts
   */
  public void setDistanceBetweenVertices(double distanceBetweenVertices) {
    this.distanceBetweenVertices = distanceBetweenVertices;
  }

  /**
   * Accessor - distance between verts
   */
  public double getDistanceBetweenVertices() {
    return distanceBetweenVertices;
  }

  /**
   * Accessor - sinks at bottom or not
   */
  public void setSinksAtBottom(boolean sinksAtBottom) {
    this.isSinksAtBottom = sinksAtBottom;
  }

  /**
   * Accessor - sinks at bottom or not
   */
  public boolean getSinksAtBottom() {
    return isSinksAtBottom;
  }

  /**
   * Accessor - alignment of layers
   */
  public void setAlignmentOfLayers(double alignmentOfLayers) {
    this.alignmentOfLayers = Math.min(1,Math.max(0, alignmentOfLayers));
  }

  /**
   * Accessor - alignment of layers
   */
  public double getAlignmentOfLayers() {
    return alignmentOfLayers;
  }

  /**
   * Accessor - ordering of vertices in layers
   */
  public Comparator<Vertex> getOrderOfVerticesInLayer() {
    return orderOfVerticesInLayer;
  }

  /**
   * Accessor - ordering of vertices in layers
   */
  public void setOrderOfVerticesInLayer(Comparator<Vertex> orderOfVerticesInLayer) {
    this.orderOfVerticesInLayer = orderOfVerticesInLayer;
  }
  
  /**
   * Accessor - horizontal positioning
   */
  public VertexPositioning getVertexPositioning() {
    return positioning;
  }
  
  /**
   * Accessor - horizontal positioning
   */
  public void setVertexPositioning(VertexPositioning positioning) {
    this.positioning = positioning;
  }
  
  /**
   * Accessor - whether to pick ports for edges
   */
  public boolean isPickPorts() {
    return pickPorts;
  }

  /**
   * Accessor - whether to pick ports for edges
   */
  public void setPickPorts(boolean pickPorts) {
    this.pickPorts = pickPorts;
  }

  /**
   * our supported horizontal positioning
   */
  public enum VertexPositioning {
    
    Compact,
    Simplex 
    
  } //VertexPositioning
  
  /**
   * a class for handling the vertex positioning
   */
  private class CompactVertexPositioning {
    
    private double totalHeight = 0;
    private double totalWidth = 0;
    private double[] layerWidths = null;
    private double[] layerHeights = null;
    private Rectangle2D[][] cellBounds = null;
    private Rectangle2D[][] vertexBounds = null;
    private Port top, bottom;
    
    private Rectangle2D apply(Graph2D graph2d, LayerAssignment layerAssignment) {

      int layers = layerAssignment.getHeight();
      
      if (isSinksAtBottom) {
        top = Port.North;
        bottom = Port.South;
      } else {
        top = Port.South;
        bottom = Port.North;
      }
      
      // init structures
      layerWidths = new double[layers];
      layerHeights = new double[layers];
      cellBounds = new Rectangle2D[layers][layerAssignment.getWidth()];
      vertexBounds = new Rectangle2D[layers][layerAssignment.getWidth()];
      
      // calculate true width of widest layer in points
      for (int i=0;i<layers;i++) {
        for (int j=0;j<layerAssignment.getWidth(i);j++) {
          if (j>0) layerWidths[i]+=distanceBetweenVertices;
          Vertex v = layerAssignment.getVertex(i, j);
          Rectangle2D r = getBounds(graph2d.getShape(v));
          vertexBounds[i][j] = r;
          layerWidths[i] += r.getWidth();
          layerHeights[i] = Math.max(layerHeights[i], r.getHeight());
        }
        totalHeight += layerHeights[i];
        if (layerWidths[i]>totalWidth) totalWidth = layerWidths[i];
      }
      totalHeight += distanceBetweenLayers*(layers-1);

      // do it
      Rectangle2D bounds = assignPositions(graph2d, layerAssignment);
      routeEdges(graph2d, layerAssignment);
      
      // done
      return bounds;
    }
    
    /**
     * assign positions to vertices
     */
    private Rectangle2D assignPositions(Graph2D graph2d, LayerAssignment layerAssignment) {
      
      // FIXME need options for node placement 
      //  max # of bends
      //  balance nodes left/center/right

      // loop over layers and place vertices 
      double y = 0;
      for (int i=0;i<layerAssignment.getHeight();i++) {
        double x = (totalWidth-layerWidths[i])*alignmentOfLayers;
        y += isSinksAtBottom ? -layerHeights[i] : 0;
        for (int j=0; j<layerAssignment.getWidth(i); j++) {
          Vertex vertex = layerAssignment.getVertex(i,j);
          if (j>0) x += (vertex instanceof DummyVertex || layerAssignment.getVertex(i,j-1) instanceof DummyVertex) ? distanceBetweenVertices/2 : distanceBetweenVertices;
          Rectangle2D r = vertexBounds[i][j];
          
          cellBounds[i][j] = new Rectangle2D.Double(x, y, r.getWidth(), layerHeights[i]);
          
          // FIXME graph2d.setPosition(vertex, new Point2D.Double(x - r.getMinX(), y - r.getMinY() + (layerHeights[i]-r.getHeight())/2 ));
          graph2d.setShape(vertex, ShapeHelper.createShape(graph2d.getShape(vertex),
              new Point2D.Double(x + r.getWidth()/2, y + r.getHeight()/2 + (layerHeights[i]-r.getHeight())/2 )
          ));
          
          x += r.getWidth();
        }
        y += isSinksAtBottom ? -distanceBetweenLayers : layerHeights[i] + distanceBetweenLayers;
      }
      
      // done
      return new Rectangle2D.Double(0,isSinksAtBottom?-totalHeight:0,totalWidth,totalHeight);
    }
    
    /**
     * layout edges between vertices in layers
     */
    private void routeEdges(Graph2D graph2d, LayerAssignment layerAssignment) {
      
      // route edges appropriately
      for (Edge edge : graph2d.getEdges()) {
        
        Routing routing = layerAssignment.getRouting(edge);
        List<Point2D> points = new ArrayList<Point2D>(routing.len);

        points.add(getPort(cellBounds[routing.layers[0]][routing.positions[0]], 
            routing.outIndex, routing.outDegree, 
            pickPorts ? bottom : Port.None));
        
        for (int r=1;r<routing.len-1;r++) {
          points.add(getPort(cellBounds[routing.layers[r]][routing.positions[r]], 0, 1, top));
          points.add(getPort(cellBounds[routing.layers[r]][routing.positions[r]], 0, 1, bottom));
        }
        
        points.add(getPort(cellBounds[routing.layers[routing.len-1]][routing.positions[routing.len-1]], 
            routing.inIndex, routing.inDegree, 
            pickPorts ? top : Port.None));
        
        graph2d.setRouting(edge, 
            LayoutHelper.getRouting(points, 
                graph2d.getShape(edge.getStart()), 
                graph2d.getShape(edge.getEnd()), 
                false
            )
        );
      }
      
      // done
    }
    
  } //VertexPositioning
  
} //HierarchicalLayout
