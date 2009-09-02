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
package gj.shell;

import gj.layout.GraphLayout;
import gj.layout.LayoutException;
import gj.shell.swing.Action2;
import gj.shell.util.ReflectHelper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;

/**
 * A widget that describes a Layout
 */
public class LayoutWidget extends JPanel {
  
  /** the layouts we've instantiated */
  private GraphLayout[] layouts = new GraphLayout[0];
  
  /** the combo of layouts */
  private JComboBox comboLayouts;

  /** the layout's properties */
  private PropertyWidget widgetProperties;
  
  /** the button */
  private JButton buttonExecute;
  
  /** the actions we keep */
  private Action 
    actionExecute = new ActionExecute(),
    actionSelect = new ActionSelect();
    
  /** listeners */
  private List<ActionListener> alisteners = new ArrayList<ActionListener>(); 

  /**
   * Constructor
   */  
  public LayoutWidget() {

    // prepare this
    setLayout(new BorderLayout());
    
    // create widgets
    comboLayouts = new JComboBox();
    widgetProperties = new PropertyWidget();
    buttonExecute = new JButton();
    
    add(comboLayouts,BorderLayout.NORTH);
    add(widgetProperties, BorderLayout.CENTER);
    add(buttonExecute, BorderLayout.SOUTH);

    // ready to listen
    widgetProperties.addActionListener(actionExecute);
    buttonExecute.setAction(actionExecute);
    
    // listening
    comboLayouts.setAction(actionSelect);
    
    // done
  }

  /**
   * editable or not
   */
  @Override
  public void setEnabled(boolean enabled) {
    actionExecute.setEnabled(enabled);
  }

  /**
   * Accessor - the default button
   */
  public JButton getDefaultButton() {
    return buttonExecute;
  }

  
  /**
   * Accessor - the layouts
   */
  public GraphLayout[] getLayouts() {
    return layouts;
  }

  /**
   * Accessor - the layouts
   */
  public void setLayouts(GraphLayout[] set) {
    layouts=set;
    comboLayouts.setModel(new DefaultComboBoxModel(layouts));
    comboLayouts.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        return super.getListCellRendererComponent(list, value!=null?ReflectHelper.getName(value.getClass()):"", index, isSelected, cellHasFocus);
      }
    });
    if (layouts.length>0) 
      comboLayouts.setSelectedItem(layouts[0]);
  }

  /**
   * Accessor - current layouts
   */
  public void setSelectedLayout(GraphLayout set) {
    comboLayouts.setSelectedItem(set);
  }
  
  /**
   * Accessor - current layout
   */
  public GraphLayout getSelectedLayouts() {
    return (GraphLayout)comboLayouts.getSelectedItem();
  }
  
  /** 
   * Adds an execute listener
   */
  public void addActionListener(ActionListener listener) {
    alisteners.add(listener);
  }
  
  /**
   * How to handle - Run the layout
   */
  protected class ActionExecute extends Action2 {
    protected ActionExecute() { 
      super("Execute"); 
      setEnabled(false); 
    }
    @Override
    public void execute() throws LayoutException {
      if (getSelectedLayouts()==null) 
        return;
      widgetProperties.commit();
      for (ActionListener listener : alisteners) 
        listener.actionPerformed(null);
      widgetProperties.refresh();
    }
  }
  
  /**
   * How to handle - Select an layout
   */
  protected class ActionSelect extends Action2 {
    protected ActionSelect() { 
      super("Select"); 
    }
    @Override
    public void execute() {
      // get the selected layout
      Object layout = comboLayouts.getModel().getSelectedItem();
      if (layout==null) 
        return;
      for (ActionListener listener : alisteners) 
        listener.actionPerformed(null);
      // show its properties
      widgetProperties.setInstance(layout);
    }
  }

  
}
