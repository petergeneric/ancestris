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
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.util.Registry;

import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * A complex bean displaying spouses of a family
 */
public class SpousesBean extends PropertyBean {
  
  private final static String COLS_KEY = "bean.spouses.cols";
  
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
    return prop instanceof Fam;
  }

  public void setPropertyImpl(Property fam) {
    table.setModel(fam!=null?new SpousesInFamily((Fam)fam):null);
  }
  
  public Property getProperty() {
    // we're not really looking at any property to be focussed or committed
    return null;
  }
  
  private static class SpousesInFamily extends AbstractPropertyTableModel {
    
    private final static TagPath[] PATHS = {
//        new TagPath("FAM:HUSB:*:..", Relationship.LABEL_FATHER),  
//        new TagPath("FAM:HUSB:*:..:NAME"),  
//        new TagPath("FAM:WIFE:*:..", Relationship.LABEL_MOTHER),
//        new TagPath("FAM:WIFE:*:..:NAME")
        new TagPath("INDI"),
        new TagPath("INDI:NAME"),
        new TagPath("INDI:BIRT:DATE"),
        new TagPath("INDI:BIRT:PLAC"),
    };
    
    private Fam fam;
    
    private SpousesInFamily(Fam fam) {
      this.fam = fam;
    }
    public Gedcom getGedcom() {
      return fam.getGedcom();
    }
    public int getNumCols() {
      return PATHS.length;
    }
    public int getNumRows() {
      return fam.getNoOfSpouses();
    }
    public TagPath getPath(int col) {
      return PATHS[col];
    }
    public Property getProperty(int row) {
      return fam.getSpouse(row);
    }
  }

} //ParentsBean
