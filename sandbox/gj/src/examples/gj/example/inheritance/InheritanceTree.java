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
package gj.example.inheritance;

import gj.example.Example;
import gj.geom.Geometry;
import gj.geom.ShapeHelper;
import gj.geom.TransformedShape;
import gj.layout.Graph2D;
import gj.layout.LayoutException;
import gj.layout.graph.radial.RadialLayout;
import gj.model.Vertex;
import gj.ui.DefaultGraphRenderer;
import gj.ui.GraphWidget;
import gj.util.DefaultGraph;
import gj.util.DefaultLayoutContext;
import gj.util.TreeGraphAdapter;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * A simple example of using the graph API for showing a family tree
 */
public class InheritanceTree implements Example {

  private static final Class<?> root = JLabel.class;
  
  public String getName() {
    return "Inheritance of "+root;
  }
  
  public JComponent prepare(GraphWidget widget) {
    
    // prepare our relationships
    TreeGraphAdapter.Tree<Class<?>> tree = new TreeGraphAdapter.Tree<Class<?>>() {
      public List<Class<?>> getChildren(Class<?> parent) {
        List<Class<?>> result = new ArrayList<Class<?>>();
        for (Class<?> c : Arrays.asList(parent.getInterfaces()))
          result.add(c);
        result.remove(Serializable.class);
        if (parent.getSuperclass()!=null)
          result.add(parent.getSuperclass());
        return result;
      }
      public Class<?> getParent(Class<?> child) {
        return getParent(getRoot(), child);
      }
      private Class<?> getParent(Class<?> parent, Class<?> child) {
        throw new IllegalArgumentException("not supported");
      }
      public Class<?> getRoot() {
        return root;
      }
    };

    final TreeGraphAdapter<Class<?>> adapter  = new TreeGraphAdapter<Class<?>>(tree);
 
    // apply radial layout
    final int w = 150, h = 16;
    
    Graph2D graph2d = new DefaultGraph(adapter, new Rectangle2D.Double(-h/2,-w/2,h,w));
    
    try {
      RadialLayout r = new RadialLayout();
      r.setDistanceBetweenGenerations(220);
      r.apply(graph2d, new DefaultLayoutContext());
    } catch (LayoutException e) {
      throw new RuntimeException("hmm, can't layout inheritance of "+root, e);
    }
    
    // stuff into a graph widget
    widget.setGraph2D(graph2d);
    
    // special layout
    widget.setRenderer(new DefaultGraphRenderer() {
      @Override
      protected void renderVertex(Graph2D graph2d, Vertex vertex, java.awt.Graphics2D graphics) {
        
        // clip, position and re-apply transformation if we have it
        AffineTransform oldt = graphics.getTransform();
        Point2D pos = ShapeHelper.getCenter(graph2d.getShape(vertex));
        graphics.translate(pos.getX(), pos.getY());
        
        Shape shape = graph2d.getShape(vertex);
        if (shape instanceof TransformedShape) {
          graphics.transform(((TransformedShape)shape).getTransformation());
          graphics.transform(AffineTransform.getRotateInstance(Geometry.QUARTER_RADIAN));
        }
        
        // draw text 
        Class<?> clazz = adapter.getContent(vertex);
        StringBuffer content = new StringBuffer();
        content.append(clazz.getSimpleName());
        Method[] methods = clazz.getDeclaredMethods();
        for (int i=0,j=0;j<5 && i<methods.length;i++) {
          if (methods[i].getName().startsWith("get")) {
            content.append("\n"+methods[i].getName()+"()");
            j++;
          }
        }
        draw(content.toString(), null, new Rectangle2D.Double(-w/2,-h/2,w,h), 0, 0.5, graphics);
        
        // restore
        graphics.setTransform(oldt);
      }
    });
 
    // done
    return widget;
  }

}
