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
package gj.example;

import gj.example.ftree.FamilyTree;
import gj.example.inheritance.InheritanceTree;
import gj.example.treemodel.SwingTree;
import gj.ui.GraphWidget;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class Main {
  
  private final static Example[] EXAMPLES = {
    new FamilyTree(),
    new InheritanceTree(),
    new SwingTree()
  };

  /**
   * main
   */
  public static void main(String[] args) {
    
    // prepare out output
    JTabbedPane pane = new JTabbedPane();

    // add example
    for (int i = 0; i < EXAMPLES.length; i++) {
      prepare(EXAMPLES[i], pane);
    }
    
    // and show
    JFrame frame = new JFrame("GraphJ - Examples");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(pane);
    frame.setSize(new Dimension(640,480));
    frame.setVisible(true);

  }
  
  private static void prepare(Example example, JTabbedPane pane) {
    
    // let example do its thing
    pane.addTab(example.getName(), new JScrollPane(example.prepare(new GraphWidget())));

  }

}
