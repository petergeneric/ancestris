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
package genj.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Support for connecting change events sources and listeners
 */
public class ChangeSupport implements DocumentListener, ChangeListener, ActionListener {

  /** listeners */
  private List listeners = new LinkedList();
  
  /** source */
  private Object source;
  
  /** has changed */
  private boolean hasChanged = false;
  
  /**
   * Constructor
   */
  public ChangeSupport(Object source) {
    this.source = source;
  }
  
  /**
   * Whether a change has happened since first listener was added
   */
  public boolean hasChanged() {
    return hasChanged;
  }
  
  public void setChanged(boolean set) {
    hasChanged = set;
    if (set)
      fireChangeEvent();
  }

  /**
   * add listener
   */
  public void addChangeListener(ChangeListener l) {
    if (listeners.isEmpty())
      hasChanged = false;
    listeners.add(l);
  }
  
  /**
   * remove listener
   */
  public void removeChangeListener(ChangeListener l) {
    listeners.remove(l);
  }
  
  /**
   * remove all listeners
   */
  public void removeAllChangeListeners() {
    listeners.clear();
  }
  
  /**
   * fire change event
   */
  public void fireChangeEvent() {
    fireChangeEvent(source);
  }
  protected void fireChangeEvent(Object source) {
    hasChanged = true;
    ChangeEvent e = new ChangeEvent(source);
    Iterator it = new ArrayList(listeners).iterator();
    while (it.hasNext())
      ((ChangeListener)it.next()).stateChanged(e);
  }
  
  /**
   * callback - change event = fire change event
   */
  public void stateChanged(ChangeEvent e) {
    fireChangeEvent(e.getSource());
  }

  /**
   * callback - document events = fire change event
   */  
  public void changedUpdate(DocumentEvent e) {
    fireChangeEvent();
  }
  public void insertUpdate(DocumentEvent e) {
    fireChangeEvent();
  }
  public void removeUpdate(DocumentEvent e) {
    fireChangeEvent();
  }
  
  /**
   * callback - action events = fire change event
   */
  public void actionPerformed(ActionEvent e) {
    fireChangeEvent(e.getSource());
  }
  
} //ChangeSupport
