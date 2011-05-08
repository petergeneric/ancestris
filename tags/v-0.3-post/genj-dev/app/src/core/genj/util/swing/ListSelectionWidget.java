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
package genj.util.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * A component that shows a list of elements the user can choose - based on whether an initial 
 * selection is provided selections are shown with checkboxes, otherwise only the elements themselves are rendered.
 */
public class ListSelectionWidget extends JComponent {

  /** list showing tag paths */
  private JList lChoose;
  
  /** the tag paths */
  private List choices = new ArrayList();
  
  /** the selection */
  private Set selection = null;

  /**
   * Constructor
   */
  public ListSelectionWidget() {

    // Layout
    lChoose = new JList();
    lChoose.setCellRenderer(new Renderer());
    lChoose.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    lChoose.addMouseListener(new SelectionListener());

    setLayout(new BorderLayout());
    add(new JScrollPane(lChoose),"Center");

    // Done
  }

  /**
   * Returns the preferred size of this component
   */
  public Dimension getPreferredSize() {
    return new Dimension(64,64);
  }

  /**
   * propagates the current state of paths to the list
   */
  private void update() {
    lChoose.setListData(choices.toArray(new Object[choices.size()]));
  }
  
  /**
   * Adds a choice to this list
   */
  public void addChoice(Object choice) {
    choices.add(choice);
    update();
  }

  /**
   * Removes a choice from this list
   */
  public void removeChoice(Object choice) {
    choices.remove(choice);
    update();
  }

  /**
   * Returns the choices
   */
  public List getChoices() {
    return Collections.unmodifiableList(choices);
  }

  /**
   * Sets the choices
   */
  public void setChoices(Object[] set) {
    choices.clear();
    choices.addAll(Arrays.asList(set));
    update();
  }

  /**
   * Sets the choices
   */
  public void setChoices(Collection c) {
    choices = new ArrayList(c);
    update();
  }
  
  /**
   * Set selection
   */
  public void setSelection(Set set) {
    selection = new HashSet(set);
    selection.retainAll(choices);
  }
  
  /**
   * Return selection
   */
  public Set getSelection() {
    if (selection==null) selection = new HashSet();
    return Collections.unmodifiableSet(selection);
  }

  /**
   * Moves currently selected paths up
   */
  public void up() {

    // Find out which row is selected now
    int row = lChoose.getSelectedIndex();
    if ((row==-1)||(row==0)) {
      return;
    }

    // Move it down
    Object o = choices.get(row);
    choices.set(row, choices.get(row-1));
    choices.set(row-1, o);

    // Show it
    update();
    lChoose.setSelectedIndex(row-1);
  }          

  /**
   * Moves currently selected paths down
   */
  public void down() {

    // Find out which row is selected now
    int row = lChoose.getSelectedIndex();
    if ((row==-1)||(row==choices.size()-1))
      return;

    // Move it down
    Object o = choices.get(row);
    choices.set(row, choices.get(row+1));
    choices.set(row+1, o);

    // Show it
    update();
    lChoose.setSelectedIndex(row+1);
  }
  
  /**
   * Override to provide custom text
   */
  protected String getText(Object choice) {
    return choice.toString();
  }

  /**
   * Override to provide custom text
   */
  protected ImageIcon getIcon(Object choice) {
    return null;
  }

  /**
   * Tag List Cell Renderer
   */
  private class Renderer extends DefaultListCellRenderer{

    /** members */
    private JPanel        panel = new JPanel();
    private JCheckBox     check = new JCheckBox();

    /** Constructor */
    private Renderer() {
      check.setOpaque(false);
      panel.setOpaque(false);
      panel.setLayout(new BorderLayout());
      panel.add(check,"West");
      panel.add(this,"Center");
    }

    /** callback for component that renders element */
    public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
      // prepare its data
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      setText( ListSelectionWidget.this.getText(value) );
      setIcon( ListSelectionWidget.this.getIcon(value) );
      // with selection or not?
      if (selection==null) 
        return this;
      // update 
      check.setSelected( selection.contains(value) );
      return panel;
    }

  } //Renderer

  /**
   * Listening to mouse input    
   */
  private class SelectionListener extends MouseAdapter {
    /** press */
    public void mousePressed(MouseEvent me) {
      // no selections?
      if (selection==null) return;
      // Check wether some valid position has been clicked on
      int pos = lChoose.locationToIndex(me.getPoint());
      if (pos==-1) return;
      // Get entry and invert selection
      Object choice = choices.get(pos);
      if (!selection.remove(choice)) 
        selection.add(choice);
      // Show it 
      lChoose.repaint(lChoose.getCellBounds(pos,pos));
    }
  } //SelectionListener
  
} //ListSelectionWidget
