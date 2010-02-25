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

import gj.shell.swing.GBLayout;
import gj.shell.util.ReflectHelper;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A widget that shows public attribues of an instance as properties 
 * in Swing components
 */ 
public class PropertyWidget extends JPanel {
  
  /** list of Boolean values */
  private final static Boolean[] BOOLEANS = new Boolean[] {
    Boolean.TRUE,Boolean.FALSE
  };
  
  /** the instance */
  private Object instance;
  
  /** its properties */
  private List<ReflectHelper.Property> properties;
  
  /** the components */
  private Map<String,JComponent> components;
  
  /** biggest preferred size */
  private Dimension biggestPreferredSize = new Dimension(0,0);
  
  /** whether we'll ignore action events for a little while */
  private boolean isIgnoreActionEvent = false;
  
  /** an ActionListener we keep for combos */
  private ActionListener alistener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      if (isIgnoreActionEvent) return;
      EventListener[] es = listenerList.getListeners(ActionListener.class);
      for (int i=0; i<es.length; i++) {
        ((ActionListener)es[i]).actionPerformed(e);
      }
      if (e.getSource() instanceof JTextField)
        ((JTextField)e.getSource()).selectAll();
    }
  };
  
  /** 
   * Constructor
   */
  public PropertyWidget() {
  }
  
  /**
   * Updates the instance with current values
   */
  public void commit() {

    // safety
    if (instance==null) 
      return;
    
    // gather the values
    for (int p=0; p<properties.size(); p++) 
      ReflectHelper.setValue(properties.get(p), getValue(components.get(properties.get(p).getName())));
    
    // done
  }
  
  /**
   * Refreshes from current instance
   */
  public void refresh() {
    
    // safety
    if (instance==null) 
      return;
    
    // get the properties again
    properties = ReflectHelper.getProperties(instance, true);
    
    // gather the values
    for (int p=0; p<properties.size(); p++) {
      setValue(components.get(properties.get(p).getName()), ReflectHelper.getValue(properties.get(p)));
    }
    
    // done
  }
  
  /**
   * Checks whether given instance has properties
   */
  public static boolean hasProperties(Object instance) {
    return ReflectHelper.getProperties(instance, true).size()!=0;
  }
  
  /** 
   * Sets the instance we're looking at
   */
  public PropertyWidget setInstance(Object instance) {
    
    // remember the instance and get its properties
    this.instance = instance;
    properties = ReflectHelper.getProperties(instance, true);
  
    // start with a GBLayout
    GBLayout layout = new GBLayout(this);
    
    // nothing to do?
    if (properties.size()==0) {
      
      layout.add(new JLabel("No Properties"),0,0,1,1,false,false,true,true);
      
    } else {
  
      // loop through properties
      components = new HashMap<String,JComponent>(properties.size());

      for (int p=0; p<properties.size(); p++) {

        ReflectHelper.Property prop = properties.get(p);
        
        JComponent component = getComponent(ReflectHelper.getValue(prop));
        components.put(prop.getName(), component);
        
        layout.add(new JLabel(prop.getName()),0,p,1,1,false,false,true,false);
        layout.add(component                 ,1,p,1,1,true ,false,true,false);
       
      }
      
      layout.add(new JLabel(),0,properties.size(),2,1,true,true,true,true);

    }
    
    // make sure that is shown
    revalidate();
    repaint();
        
    // done
    return this;
  }

  /**
   * Returns a component appropriate for editing given property
   */
  private JComponent getComponent(Object prop) {
    
    // a boolean
    if (prop instanceof Boolean) {
      JComboBox cb = new JComboBox(BOOLEANS);
      cb.setSelectedItem(prop);
      cb.addActionListener(alistener);
      return cb;
    }
    
    // an enumeration
    if (Enum.class.isAssignableFrom(prop.getClass())) {
      Class<?> c = prop.getClass();
      while (c.getSuperclass()!=Enum.class) c = c.getSuperclass();
      JComboBox cb = new JComboBox(c.getEnumConstants());
      cb.setSelectedItem(prop);
      cb.addActionListener(alistener);
      return cb;
    }
    
    // assuming text
    final JTextField result = new JTextField(prop.toString());
    result.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        result.selectAll();
      }
      public void focusLost(FocusEvent e) {
        
      }
    });
    result.addActionListener(alistener);
    return result;
  }  
  
  /**
   * Returns the value of given JComponent 
   */
  private Object getValue(JComponent component) {
    if (component instanceof JComboBox)
      return ((JComboBox)component).getSelectedItem();
    return ((JTextField)component).getText();
  }
  
  /**
   * Returns the value of given JComponent 
   */
  private void setValue(JComponent component, Object value) {
    if (component instanceof JComboBox) {
      isIgnoreActionEvent=true;
      JComboBox cb = (JComboBox)component;
      cb.setSelectedItem(value);
      isIgnoreActionEvent=false;
    } else {
      if (value==null)
        value = "";
      ((JTextField)component).setText(value.toString());
    }
  }
  
  /**
   * @see Component#getPreferredSize()
   */
  @Override
  public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.width = Math.max(d.width,biggestPreferredSize.width);
    d.height = Math.max(d.height,biggestPreferredSize.height);
    biggestPreferredSize = d;
    return d;
  }
  
  
  /**
   * Adds an ActionListener
   */
  public void addActionListener(ActionListener a) {
    listenerList.add(ActionListener.class,a);
  }
  
  /**
   * Removes an ActionListener
   */
  public void removeActionListener(ActionListener a) {
    listenerList.remove(ActionListener.class,a);
  }

}
