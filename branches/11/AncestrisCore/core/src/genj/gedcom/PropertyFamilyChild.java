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
package genj.gedcom;

import java.util.List;

/**
 * Gedcom Property : FAMC
 * A property wrapping the condition of being a child in a family
 */
public class PropertyFamilyChild extends PropertyXRef {

  /**
   * Empty Constructor
   */
  /*package*/ PropertyFamilyChild() {
    super("FAMC");
  }
  
  /**
   * need tag-argument constructor for all properties
   */
  /*package*/ PropertyFamilyChild(String tag) {
    super(tag);
    assertTag("FAMC");
  }
  
  /**
   * Check if this is a biological link (not necessarily deterministic)
   * @return Boolean.True if biological, Boolean.FALSE if not biological, null otherwise (unknown)
   */
  public Boolean isBiological() {
    // certainly not if contained in ADOPtion event
    String parent = getParent().getTag();
    if ("ADOP".equals(parent))
      return Boolean.FALSE;
    // certainly yes if contained in BIRTh event
    if ("BIRT".equals(parent))
      return Boolean.TRUE;
    // check for PEDI? could be if not present
    Property pedi = getProperty("PEDI");
    if (pedi!=null) {
      String value = pedi.getValue();
      if ("birth".equals(value)) return Boolean.TRUE;
      if ("adopted".equals(value)) return Boolean.FALSE;
      if ("foster".equals(value)) return Boolean.FALSE; 
      if ("sealing".equals(value)) return Boolean.FALSE;
    }
    // dunno
    return null;
  }

  /**
   * @see genj.gedcom.PropertyXRef#getForeignDisplayValue()
   */
  protected String getForeignDisplayValue() {
    // can only really be called if this is an ADOPtion case
    Property adop = getParent();
    if (adop instanceof PropertyEvent&&adop.getTag().equals("ADOP"))
      return resources.getString("foreign.ADOP", getEntity().toString());
    // fallback
    return super.getForeignDisplayValue();
  }
  
  /**
   * Returns the reference to family
   */
  public Fam getFamily() {
    return (Fam)getTargetEntity();
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when property has no parent property,
   * referenced individual is child, wife or husband in enclosing family
   * or it would become ancestor of itself by this action.
   */
  public void link() throws GedcomException {

    // Get enclosing individual ?
    Indi indi;
    try {
      indi = (Indi)getEntity();
    } catch (ClassCastException ex) {
      throw new GedcomException(resources.getString("error.noenclosingindi"));
    }
    
    // Look for family
    Fam fam = (Fam)getCandidate();

    // Make sure the child is not ancestor of the family (father,grandfather,grandgrandfather,...)
    // .. that would introduce a circle
    if (indi.isAncestorOf(fam))
      throw new GedcomException(resources.getString("error.already.ancestor", indi.toString(), fam.toString() ));

    // NM20070921 - since we're handling multiple references to FAMC in getFamiliesWhereChild now e.g.
    // 0 INDI
    // 0  FAMC @F@
    // 0  BIRT
    // 1   FAMC @F@
    // we can allow multiple famc pointing at the same family
    
    // Connect back from family (maybe using invalid back reference) 
    List<PropertyChild> childs = fam.getProperties(PropertyChild.class);
    for (PropertyChild prop : childs) {
      if (prop.isCandidate(indi)) {
        link(prop);
        return;
      }
    }

    // .. new back referencing property
    PropertyXRef xref = new PropertyChild();
    fam.addProperty(xref);
    link(xref);

    // Done
  }

  /**
   * The expected referenced type
   */
  public String getTargetType() {
    return Gedcom.FAM;

  }

} //PropertyFamilyChild
