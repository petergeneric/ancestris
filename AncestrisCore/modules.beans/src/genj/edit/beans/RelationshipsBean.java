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
import genj.gedcom.Grammar;
import genj.gedcom.Indi;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyChild;
import genj.gedcom.PropertyHusband;
import genj.gedcom.PropertyWife;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

/**
 * A complex bean displaying families of an individual
 */
public class RelationshipsBean extends PropertyBean {

  public static Icon IMG = Grammar.V55.getMeta(new TagPath("FAM")).getImage().getOverLayed(MetaProperty.IMG_LINK);
    
  private PropertyTableWidget table;
  private Map<Property,String> relationships = new HashMap<Property,String>();
  
  public RelationshipsBean() {
    
    // prepare a simple table
    table = new PropertyTableWidget();
    table.setVisibleRowCount(2);
    
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, table);

  }
  
  @Override
  public void removeNotify() {
    REGISTRY.put("relcols", table.getColumnLayout());
    super.removeNotify();
  }
  
  @Override
  protected void commitImpl(Property property) {
    // noop
  }

  /**
   * Set context to edit
   */
  protected void setPropertyImpl(Property prop) {
    
    relationships.clear();

    Model model = null;
    if (prop instanceof Indi)
      model = getModel((Indi)prop);
    if (prop instanceof Fam)
      model = getModel((Fam)prop);
      
    table.setModel(model);
    table.setColumnLayout(REGISTRY.get("relcols",""));
  }
  
  private Model getModel(Fam fam) {
    
    TagPath[] columns = new TagPath[] {
      new TagPath(".", RESOURCES.getString("relationship")), // relationship
      new TagPath("*:..", Gedcom.getName("INDI")), // person's id
      new TagPath("*:..:NAME"), // person's name
      new TagPath("*:..:BIRT:DATE") // person's birth date
    };
    
    List<Property> rows = new ArrayList<Property>();
    
    // father and mother
    Property husband = fam.getProperty("HUSB");
    if (husband instanceof PropertyXRef && husband.isValid()) {
      relationships.put(husband, PropertyHusband.LABEL_FATHER);
      rows.add(husband);
    }
    Property wife = fam.getProperty("WIFE");
    if (wife instanceof PropertyWife && wife.isValid()) {
      relationships.put(wife, PropertyWife.LABEL_MOTHER);
      rows.add(wife);
    }
    
    for (Property child : fam.getProperties("CHIL")) {
      if (child instanceof PropertyXRef && child.isValid()) {
        relationships.put(child, child.getPropertyName());
        rows.add(child);
      }
    }
    
    return new Model(fam.getGedcom(), columns, rows);
  }
  
  private Model getModel(Indi indi) {
    
    TagPath[] columns = new TagPath[] {
      new TagPath(".", RESOURCES.getString("relationship")), // relationship
      new TagPath("*:..", Gedcom.getName("INDI")), // person's id
      new TagPath("*:..:NAME"), // person's name
      new TagPath("*:..:BIRT:DATE"), // person's birth date
      new TagPath("..", Gedcom.getName("FAM")), // families id
      new TagPath("..:MARR:DATE") // families id
    };
    
    List<Property> rows = new ArrayList<Property>();
    
    // parental family
    Fam parental = indi.getFamilyWhereBiologicalChild();
    if (parental!=null) {
      Property husband = parental.getProperty("HUSB");
      if (husband instanceof PropertyXRef && husband.isValid()) {
        relationships.put(husband, PropertyHusband.LABEL_FATHER);
        rows.add(husband);
      }
      Property wife = parental.getProperty("WIFE");
      if (wife instanceof PropertyWife && wife.isValid()) {
        relationships.put(wife, PropertyWife.LABEL_MOTHER);
        rows.add(wife);
      }
    }
      
    // spousal family
    for (Fam spousal : indi.getFamiliesWhereSpouse()) {
      Property spouse = spousal.getProperty("HUSB");
      if (spouse instanceof PropertyXRef && spouse.isValid() && ((PropertyXRef)spouse).getTargetEntity()!=indi) {
        relationships.put(spouse, spouse.getPropertyName());
        rows.add(spouse);
      } else {
        spouse = spousal.getProperty("WIFE");
        if (spouse instanceof PropertyXRef && spouse.isValid() && ((PropertyXRef)spouse).getTargetEntity()!=indi) {
          relationships.put(spouse, spouse.getPropertyName());
          rows.add(spouse);
        }
      }
      for (PropertyChild child : spousal.getProperties(PropertyChild.class)) {
        if (child.isValid()) {
          relationships.put(child, child.getPropertyName());
          rows.add(child);
        }
      }
    }

    return new Model(indi.getGedcom(), columns, rows);
  }
  
  private class Model extends AbstractPropertyTableModel {
    
    private TagPath[] columns;
    private List<Property> rows;
    
    public Model(Gedcom gedcom, TagPath[] columns, List<Property> rows) {
      super(gedcom);
      this.columns = columns;
      this.rows = rows;
    }
    
    public int getNumCols() {
      return columns.length;
    }

    public int getNumRows() {
      return rows.size();
    }
    
    public TagPath getColPath(int col) {
      return columns[col];
    }

    public Property getRowRoot(int row) {
      return rows.get(row);
    }
    
    @Override
    public String getCellValue(Property property, int row, int col) {
      String relationship = relationships.get(property);
      return relationship!=null ? relationship : super.getCellValue(property, row, col);
    }
  }
} 
