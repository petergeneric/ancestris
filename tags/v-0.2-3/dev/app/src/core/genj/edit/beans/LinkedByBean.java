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
package genj.edit.beans;

import genj.common.AbstractPropertyTableModel;
import genj.common.PropertyTableWidget;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.util.Registry;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A complex bean displaying entities that link to an entity
 */
public class LinkedByBean extends PropertyBean {

  private PropertyTableWidget table;
  
  private final static String COLS_KEY = "bean.linkedby.cols";

  void initialize(Registry setRegistry) {
    super.initialize(setRegistry);
    
    // prepare a simple table
    table = new PropertyTableWidget();
    table.setPreferredSize(new Dimension(64,64));
    
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, table);
    
  }

  /**
   * on add - set column widths
   */
  public void addNotify() {
    // let super continue
    super.addNotify();
    // set widths
    table.setColumnLayout(registry.get(COLS_KEY, (String)null));
  }
  
  /**
   * on remove - keep column widths
   */
  public void removeNotify() {
    registry.put(COLS_KEY, table.getColumnLayout());
    // let super continue
    super.removeNotify();
  }
  
  /**
   * Set context to edit
   */
  boolean accepts(Property prop) {
    return prop instanceof Entity;
  }
  public void setPropertyImpl(Property property) {

    //  don't propagate property since we're technically not looking at it
    // property = indi;
    
    // connect to current indi
    table.setModel(property!=null ? new Model((Entity)property) : null);
    
    // done
  }
  
  private final static TagPath 
    DOT = new TagPath(".");
  
  private class Model extends AbstractPropertyTableModel {
    
    private List xrefs = new ArrayList();
    private Entity entity;
    
    private Model(Entity entity) {
      this.entity = entity;
      for (Iterator ps = entity.getProperties(PropertyXRef.class).iterator(); ps.hasNext(); ) {
        PropertyXRef p = (PropertyXRef)ps.next();
        if (p.getTarget()!=null)
          xrefs.add(p);
      }
    }
    
    public Gedcom getGedcom() {
      return entity.getGedcom();
    }
    
    public int getNumCols() {
      return 1;
    }
    
    public int getNumRows() {
      return xrefs.size();
    }
    
    public TagPath getPath(int col) {
      return DOT;
    }
    
    public String getName(int col) {
      return entity.getPropertyName();
    }
    
    public Property getProperty(int row) {
      return (Property)xrefs.get(row);
    }
  };
  

} //FamiliesBean
