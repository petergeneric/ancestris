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

import genj.util.ChangeSupport;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;

/**
 * A component that shows a list of elements the user can choose - based on whether an initial 
 * selection is provided selections are shown with checkboxes, otherwise only the elements themselves are rendered.
 */
public class ListSelectionWidget<T> extends JComponent {

  /** list showing tag paths */
  private JList lChoose;
  
  /** the tag paths */
  private List<T> choices = new ArrayList<T>();
  
  /** the selection */
  private Set<T> selection = null;
  
  private ChangeSupport changes = new ChangeSupport(this);

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
  
  public void addChangeListener(ChangeListener listener) {
    changes.addChangeListener(listener);
  }
  
  public void removeChangeListener(ChangeListener listener) {
    changes.removeChangeListener(listener);
  }
  
  @Override
  public synchronized void addMouseListener(MouseListener l) {
    lChoose.addMouseListener(l);
  }

  @Override
  public synchronized void removeMouseListener(MouseListener l) {
    lChoose.removeMouseListener(l);
  }

  public T getChoice(Point point) {
    int i = lChoose.locationToIndex(point);
    return i<0||i>choices.size()-1 ? null : choices.get(i);
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
    changes.fireChangeEvent();
  }
  
  /**
   * Adds a choice to this list
   */
  public void addChoice(T choice) {
    choices.add(choice);
    update();
  }

  /**
   * Removes a choice from this list
   */
  public void removeChoice(T choice) {
    choices.remove(choice);
    update();
  }

  /**
   * Returns the choices
   */
  public List<T> getChoices() {
    return Collections.unmodifiableList(choices);
  }

  /**
   * Sets the choices
   */
  public void setChoices(T[] set) {
    choices.clear();
    choices.addAll(Arrays.asList(set));
    update();
  }

  /**
   * Sets the choices
   */
  public void setChoices(Collection<T> c) {
    choices = new ArrayList<T>(c);
    update();
  }
  
  /**
   * Set selection
   */
  public void setCheckedChoices(Set<T> set) {
    selection = new HashSet<T>(set);
    
    for (T t : set)
      if (!choices.contains(t))
        choices.add(t);
    
    update();
  }
  
  /**
   * Return selection
   */
  public Set<T> getCheckedChoices() {
    if (selection==null) selection = new HashSet<T>();
    return Collections.unmodifiableSet(selection);
  }
  
  
  @SuppressWarnings("unchecked")
  public T getSelectedChoice() {
    return (T)lChoose.getSelectedValue();
  }
  
  public int getSelectedIndex() {
    return lChoose.getSelectedIndex();
  }

  
  /**
   * exchange two choices
   */
  public void swapChoices(int i, int j) {
    
    int selected = lChoose.getSelectedIndex();
  
    // Move it down
    T o = choices.get(i);
    choices.set(i, choices.get(j));
    choices.set(j, o);

    // Show it
    update();

    if (selected==i)
      lChoose.setSelectedIndex(j);
    if (selected==j)
      lChoose.setSelectedIndex(i);
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
    T o = choices.get(row);
    choices.set(row, choices.get(row+1));
    choices.set(row+1, o);

    // Show it
    update();
    lChoose.setSelectedIndex(row+1);
  }
  
  /**
   * Override to provide custom text
   */
  protected String getText(T choice) {
    return choice.toString();
  }

  /**
   * Override to provide custom text
   */
  protected ImageIcon getIcon(T choice) {
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
      panel.setOpaque(true);
      panel.setLayout(new BorderLayout());
      panel.add(check,"West");
      panel.add(this,"Center");
    }

    /** callback for component that renders element */
    @SuppressWarnings("unchecked")
    public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
      // prepare its data
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      setText( ListSelectionWidget.this.getText( (T)value) );
      setIcon( ListSelectionWidget.this.getIcon( (T)value) );
      // with selection or not?
      if (selection==null) 
        return this;
      // update 
      panel.setBackground(super.getBackground());
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
      T choice = choices.get(pos);
      if (!selection.remove(choice)) 
        selection.add(choice);
      // Show it 
      lChoose.repaint(lChoose.getCellBounds(pos,pos));
      // tell
      changes.fireChangeEvent();
    }
  } //SelectionListener

  public void addSelectionListener(ListSelectionListener listener) {
    lChoose.addListSelectionListener(listener);
  }
  
  public void removeSelectionListener(ListSelectionListener listener) {
    lChoose.removeListSelectionListener(listener);
  }
  
} //ListSelectionWidget
