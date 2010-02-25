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

import java.util.ArrayList;
import java.util.List;

import spin.Spin;

/**
 * A default base-type for property models
 */
public abstract class AbstractPropertyTableModel implements PropertyTableModel, GedcomListener {
  
  private List listeners = new ArrayList(3);
  private Gedcom gedcom = null;
  private GedcomListener callback;
  
  /** 
   * Add listener
   */
  public void addListener(PropertyTableModelListener listener) {
    listeners.add(listener);
    if (listeners.size()==1) {
      // cache gedcom now
      if (gedcom==null) gedcom=getGedcom();
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
  public String getName(int col) {
    return getPath(col).getName();    
  }
  
  /**
   * Structure change
   */
  protected void fireRowsChanged(int rowStart, int rowEnd, int col) {
    for (int i=0;i<listeners.size();i++)
      ((PropertyTableModelListener)listeners.get(i)).handleRowsChanged(this, rowStart, rowEnd, col);
  }
  
  /**
   * Structure change
   */
  protected void fireRowsAdded(int rowStart, int rowEnd) {
    for (int i=0;i<listeners.size();i++)
      ((PropertyTableModelListener)listeners.get(i)).handleRowsAdded(this, rowStart, rowEnd);
  }

  /**
   * Structure change
   */
  protected void fireRowsDeleted(int rowStart, int rowEnd) {
    for (int i=0;i<listeners.size();i++)
      ((PropertyTableModelListener)listeners.get(i)).handleRowsDeleted(this, rowStart, rowEnd);
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

}
