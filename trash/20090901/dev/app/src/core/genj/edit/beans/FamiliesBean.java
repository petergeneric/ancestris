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
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import genj.util.Registry;

import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * A complex bean displaying families of an individual
 */
public class FamiliesBean extends PropertyBean {

  private final static TagPath 
    PATH_FAM = new TagPath("FAM"),
    PATH_HUSB = new TagPath("FAM:HUSB:*:.."),
    PATH_WIFE = new TagPath("FAM:WIFE:*:.."),
    PATH_HUSB_NAME = new TagPath("FAM:HUSB:*:..:NAME"),
    PATH_WIFE_NAME = new TagPath("FAM:WIFE:*:..:NAME"),
    PATH_MARR_DATE = Fam.PATH_FAMMARRDATE,
    PATH_MARR_PLAC = Fam.PATH_FAMMARRPLAC;

  private PropertyTableWidget table;
  
  private final static String COLS_KEY = "bean.families.cols";

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
    return prop instanceof Indi;
  }
  public void setPropertyImpl(Property prop) {

    Indi indi = (Indi)prop;
    
    // connect to current indi
    table.setModel(indi!=null?new Families(indi):null);
    
    // done
  }
  
  public Property getProperty() {
    // we're not really looking at any property to be focussed or committed
    return null;
  }
  
  private class Families extends AbstractPropertyTableModel {
    
    private Indi indi;
    private Fam[] fams;
    
    private Families(Indi indi) {
      this.indi = indi;
      fams = indi.getFamiliesWhereSpouse();
    }
    
    public Gedcom getGedcom() {
      return indi.getGedcom();
    }
    public int getNumCols() {
      return 5;
    }
    public int getNumRows() {
      return fams.length;
    }
    public TagPath getPath(int col) {
      switch (col) {
        default:
        case 0:
          return PATH_FAM;
        case 1:
          return indi.getSex() == PropertySex.FEMALE ? PATH_HUSB : PATH_WIFE;
        case 2:
          return indi.getSex() == PropertySex.FEMALE ? PATH_HUSB_NAME : PATH_WIFE_NAME;
        case 3:
          return PATH_MARR_DATE;
        case 4:
          return PATH_MARR_PLAC;
      }
    }
    public Property getProperty(int row) {
      return fams[row];
    }
  };
  

} //FamiliesBean
