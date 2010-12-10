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
package genj.common;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyNumericValue;
import genj.gedcom.PropertySex;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.SwingConstants;

import spin.Spin;

/**
 * A default base-type for property models
 */
public abstract class AbstractPropertyTableModel implements PropertyTableModel, GedcomListener {
  
  protected final static int 
    LEFT = SwingConstants.LEFT,
    CENTER = SwingConstants.CENTER,
    RIGHT = SwingConstants.RIGHT;
  
  private List<PropertyTableModelListener> listeners = new CopyOnWriteArrayList<PropertyTableModelListener>();
  private Gedcom gedcom = null;
  private GedcomListener callback;
  
  protected AbstractPropertyTableModel(Gedcom gedcom) {
    this.gedcom = gedcom;
  }
  
  final public Gedcom getGedcom() {
    return gedcom;
  }
  
  /** 
   * Add listener
   */
  public void addListener(PropertyTableModelListener listener) {
    listeners.add(listener);
    if (listeners.size()==1) {
      // and start listening (make sure events are spin over to the EDT)
      gedcom.addGedcomListener((GedcomListener)Spin.over((GedcomListener)this));
    }
  }
  
  /** 
   * Remove listener
   */
  public void removeListener(PropertyTableModelListener listener) {
    listeners.remove(listener);
    // stop listening
    if (listeners.isEmpty())
      gedcom.removeGedcomListener((GedcomListener)Spin.over(this));
  }
  
  /**
   * Column name
   */
  public String getColName(int col) {
    return getColPath(col).getName();    
  }
  
  /**
   * Structure change
   */
  protected void fireRowsChanged(int rowStart, int rowEnd, int col) {
    for (PropertyTableModelListener listener : listeners)
      listener.handleRowsChanged(this, rowStart, rowEnd, col);
  }
  
  /**
   * Structure change
   */
  protected void fireRowsAdded(int rowStart, int rowEnd) {
    for (PropertyTableModelListener listener : listeners)
      listener.handleRowsAdded(this, rowStart, rowEnd);
  }

  /**
   * Structure change
   */
  protected void fireRowsDeleted(int rowStart, int rowEnd) {
    for (PropertyTableModelListener listener : listeners)
      listener.handleRowsDeleted(this, rowStart, rowEnd);
  }

  /**
   * Gedcom callback
   */
  public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
    // ignored
  }

  /**
   * Gedcom callback
   */
  public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
    // ignored
  }

  /**
   * Gedcom callback
   */
  public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
    // ignored
  }

  /**
   * Gedcom callback
   */
  public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
    // ignored
  }

  /**
   * Gedcom callback
   */
  public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
    // ignored
  }

  public String getCellValue(Property property, int row, int col) {
    return getDefaultCellValue(property, row, col);
  }
  
  /*package*/ static String getDefaultCellValue(Property property, int ro, int col) {
    if (property==null)
      return "";
    if (property instanceof Entity) 
      return ((Entity)property).getId();
    if (property instanceof PropertySex) 
      return Character.toString(((PropertySex)property).getDisplayValue().charAt(0));
    return property.getDisplayValue();
  }
  
  public int getCellAlignment(Property property, int row, int col) {
    return getDefaultCellAlignment(property, row, col);
  }
  
  /*package*/ static int getDefaultCellAlignment(Property property, int row, int col) {
    if (property instanceof Entity) 
      return RIGHT;
    if (property instanceof PropertyDate) 
      return RIGHT;
    if (property instanceof PropertyNumericValue) 
      return RIGHT;
    if (property instanceof PropertySex) 
      return CENTER;
    return LEFT;
  }

  public int compare(Property valueA, Property valueB, int col) {
    return defaultCompare(valueA, valueB, col);
  }

  /*package*/ static int defaultCompare(Property valueA, Property valueB, int col) {
    return valueA.compareTo(valueB);
  }
}
