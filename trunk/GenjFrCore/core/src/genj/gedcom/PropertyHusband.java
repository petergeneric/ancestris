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
 * Gedcom Property : HUSB
 * Property wrapping the condition of having a husband in a family
 */
public class PropertyHusband extends PropertyXRef {

  public final static String LABEL_FATHER = Gedcom.resources.getString("HUSB.father");

  /**
   * Empty Constructor
   */
  /*package*/ PropertyHusband() {
    super("HUSB");
  }
  
  /**
   * need tag-argument constructor for all properties
   */
  /*package*/ PropertyHusband(String tag) {
    super(tag);
    assertTag("HUSB");
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
    return resources.getString("prop.husb.veto");
  }

  /**
   * Returns the husband
   */
  public Indi getHusband() {
    return (Indi)getTargetEntity();
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when property has no parent property,
   * or a double husband/wife situation would be the result
   */
  public void link() throws GedcomException {

    // Get enclosing family ?
    Fam fam;
    try {
      fam = (Fam)getEntity();
    } catch (ClassCastException ex) {
      throw new GedcomException(resources.getString("error.noenclosingfam"));
    }

    // Prepare some VARs
    Property p;
    Property ps[];

    // Enclosing family has a husband already ?
    if (fam.getHusband()!=null)
      throw new GedcomException(resources.getString("error.already.spouse", fam.getHusband().toString(), fam.toString()));

    // Look for husband (not-existing -> Gedcom throws Exception)
    Indi husband = (Indi)getCandidate();

    // make sure wife isn't also husband
    if (fam.getWife()==husband)
      throw new GedcomException(resources.getString("error.already.spouse", husband.toString(), fam.toString()));

    // make sure the husband isn't descendant of family
    if (husband.isDescendantOf(fam))
      throw new GedcomException(resources.getString("error.already.descendant", husband.toString(), fam.toString()));
    
    // Connect back from husband (maybe using invalid back reference)
    ps = husband.getProperties(new TagPath("INDI:FAMS"));
    PropertyFamilySpouse pfs;
    for (int i=0;i<ps.length;i++) {
      pfs = (PropertyFamilySpouse)ps[i];
      if (pfs.isCandidate(fam)) {
        link(pfs);
        return;
      }
    }

    // .. new back referencing property
    pfs = new PropertyFamilySpouse();
    husband.addProperty(pfs);
    link(pfs);

    // Done
  }

  /**
   * The expected referenced type
   */
  public String getTargetType() {
    return Gedcom.INDI;
  }
  
} //PropertyHusband
