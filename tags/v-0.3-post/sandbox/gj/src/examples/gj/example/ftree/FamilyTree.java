/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2008 Nils Meier
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
package gj.example.ftree;

import gj.example.Example;
import gj.layout.Graph2D;
import gj.layout.LayoutException;
import gj.layout.graph.tree.TreeLayout;
import gj.model.Graph;
import gj.ui.GraphWidget;
import gj.util.DefaultGraph;
import gj.util.DefaultLayoutContext;
import gj.util.TreeGraphAdapter;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

/**
 * A simple example of using the graph API for showing a family tree
 */
public class FamilyTree implements Example {
  
  /**
   * our example name
   */
  public String getName() {
    return "Family Tree";
  }
  
  /**
   * our example preparation
   */
  public JComponent prepare(GraphWidget widget) {
    
    // prepare our relationships
    final String family = 
      "L&M>L&A,"+
      "L&M>S&S,"+
      "L&M>Nils,"+
      "L&A>Yaro,"+
      "S&S>Jonas,"+
      "S&S>Alisa,"+
      "S&S>Luka";

    // wrap in an adapter that understands our tree
    TreeGraphAdapter.Tree<String> tree = new TreeGraphAdapter.Tree<String>() {
      public List<String> getChildren(String parent) {
        List<String> result = new ArrayList<String>();
        for (String relationship : family.split(",")) {
          if (relationship.startsWith(parent+">"))
            result.add(relationship.substring(parent.length()+1));
        }
        return result;
      }
      public String getParent(String child) {
        for (String relationship : family.split(",")) {
          if (relationship.endsWith(">"+child))
            return relationship.substring(0, relationship.length()-child.length()-1);
        }
        return null;
      }
      public String getRoot() {
        return family.substring(0, family.indexOf('>'));
      }
    };

    Graph graph = new TreeGraphAdapter<String>(tree);
    
    // apply tree layout
    Graph2D graph2d = new DefaultGraph(graph, new Rectangle2D.Double(-20,-16,40,32));
    
    try {
      new TreeLayout().apply(graph2d, new DefaultLayoutContext());
    } catch (LayoutException e) {
      throw new RuntimeException("hmm, can't layout my family", e);
    }
    
    // set
    widget.setGraph2D(graph2d);
    
    // done
    return widget;
  }

}
