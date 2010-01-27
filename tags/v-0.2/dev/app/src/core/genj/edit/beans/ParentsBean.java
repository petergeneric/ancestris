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
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyHusband;
import genj.gedcom.PropertyWife;
import genj.gedcom.TagPath;
import genj.util.Registry;

import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * A complex bean displaying parents of an individual
 */
public class ParentsBean extends PropertyBean {
  
  private final static String COLS_KEY = "bean.parents.cols";
  
  private PropertyTableWidget table;
  
  void initialize(Registry setRegistry) {
    super.initialize(setRegistry);
    
    // setup layout & table
    table = new PropertyTableWidget();
    table.setPreferredSize(new Dimension(64,64));
    
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, table);
    
    // done
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
    return prop instanceof Indi;
  }
  
  public void setPropertyImpl(Property indi) {
    table.setModel(indi!=null ? new ParentsOfChild((Indi)indi) : null);
  }
  
  public Property getProperty() {
    // we're not really looking at any property to be focussed or committed
    return null;
  }
  
  private static class ParentsOfChild extends AbstractPropertyTableModel {
    
    private final static TagPath PATHS[] = {
      new TagPath("FAM"),  
      new TagPath("FAM:HUSB:*:..", PropertyHusband.LABEL_FATHER),  
      new TagPath("FAM:HUSB:*:..:NAME"),  
      new TagPath("FAM:WIFE:*:..", PropertyWife.LABEL_MOTHER),
      new TagPath("FAM:WIFE:*:..:NAME")
    };
    
    private Indi child;
    private Fam[] familiesWhereChild;
    
    private ParentsOfChild(Indi child) {
      this.child = child;
      familiesWhereChild = child.getFamiliesWhereChild();
    }
      
    public Gedcom getGedcom() {
      return child.getGedcom();
    }
    public int getNumCols() {
      return PATHS.length;
    }
    public int getNumRows() {
      return familiesWhereChild.length;
    }
    public TagPath getPath(int col) {
      return PATHS[col];
    }
    public Property getProperty(int row) {
      return familiesWhereChild[row];
    }
  }

} //ParentsBean
