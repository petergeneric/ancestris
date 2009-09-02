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

/**
 * Gedcom Property : FAMS
 * The property wrapping the condition of being a spouse in a family
 */
public class PropertyFamilySpouse extends PropertyXRef {

  /**
   * Empty Constructor
   */
  public PropertyFamilySpouse() {
  }
  
  /**
   * Returns a warning string that describes what happens when this
   * property would be deleted
   * @return warning as <code>String</code>, <code>null</code> when no warning
   */
  public String getDeleteVeto() {
    // warn if linked
    if (getTargetEntity()==null) 
      return null;
    return resources.getString("prop.fams.veto");
  }

  /**
   * Returns the reference to family
   */
  public Fam getFamily() {
    return (Fam)getTargetEntity();
  }

  /**
   * Returns the Gedcom-Tag of this property
   */
  public String getTag() {
    return "FAMS";
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

    // Prepare some VARs
    Property p;
    Gedcom gedcom = getGedcom();

    // Look for family (not-existing -> Gedcom throws Exception)
    Fam fam = (Fam)getCandidate();

    // Enclosing individual is Husband/Wife in family ?
    Indi husband = fam.getHusband();
    Indi wife    = fam.getWife();

    if ((husband!=null)&&(wife!=null))
      throw new GedcomException(resources.getString("error.already.spouses", fam));

    if ((husband==indi)||(wife==indi))
      throw new GedcomException(resources.getString("error.already.spouse", new String[]{ indi.toString(), fam.toString()}));

    Fam[] familiesWhereChild = indi.getFamiliesWhereChild();
    for (int i=0; i<familiesWhereChild.length; i++) {
      if (familiesWhereChild[i]==fam)
        throw new GedcomException(resources.getString("error.already.child", new String[]{ indi.toString(), fam.toString()}));
    }
    
    // Make sure indi isn't already descendant of family 
    if (indi.isDescendantOf(fam)) 
      throw new GedcomException(resources.getString("error.already.descendant", new String[]{ indi.toString(), fam.toString()}));
    
    // place as husband or wife according to gender
    if (indi.getSex()==PropertySex.UNKNOWN) 
      indi.setSex(husband==null ? PropertySex.MALE : PropertySex.FEMALE);

    // check for already existing back reference which takes precedence
    // NM 20070128 don't use tag paths for simple sub-property get - it's expensive
    Property[] husbands = fam.getProperties("HUSB", false);
    for (int i=0;i<husbands.length;i++) {
      PropertyHusband ph = (PropertyHusband)husbands[i];
      if (ph.isCandidate(indi)) {
        link(ph);
        return;
      }
    }
    // NM 20070128 don't use tag paths for simple sub-property get - it's expensive
    Property[] wifes = fam.getProperties("WIFE", false);
    for (int i=0;i<wifes.length;i++) {
      PropertyWife pw = (PropertyWife)wifes[i];
      if (pw.isCandidate(indi)) {
        link(pw);
        return;
      }
    }
    
    // place as husband/wife as appropriately
    if (indi.getSex()==PropertySex.MALE) {
      // swap if necessary
      if (husband!=null)
        fam.swapSpouses();
      // create new back ref
      PropertyXRef backref = new PropertyHusband();
      fam.addProperty(backref);
      link(backref);
    } else {
      // swap if necessary
      if (wife!=null)
        fam.swapSpouses();
      // create new back ref
      PropertyXRef backref = new PropertyWife();
      fam.addProperty(backref);
      link(backref);
    }

    // Done
  }

  /**
   * The expected referenced type
   */
  public String getTargetType() {
    return Gedcom.FAM;

  }
  
} //PropertyFamilySpouse
