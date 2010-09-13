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
 * Gedcom Property : WIFE
 * Property wrapping the condition of having a wife in a family
 */
public class PropertyWife extends PropertyXRef {

  private final static TagPath
    PATH_INDIFAMS = new TagPath("INDI:FAMS");
  
  public final static String TAG = "WIFE";

  public final static String LABEL_MOTHER = Gedcom.resources.getString("WIFE.mother");
  
  /**
   * Empty Constructor
   */
  public PropertyWife() {
    super("WIFE");
  }
  
  /**
   * need tag-argument constructor for all properties
   */
  protected PropertyWife(String tag) {
    super(tag);
    assertTag("WIFE");
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
    return resources.getString("prop.wife.veto");
  }

  /**
   * Returns the wife
   */
  public Indi getWife() {
    return (Indi)getTargetEntity();
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when property has no parent property,
   * or a double husband/wife situation would be the result
   */
  public void link() throws GedcomException {

    // Get enclosing family ?
    Fam fam = null;
    try {
      fam = (Fam)getEntity();
    } catch (ClassCastException ex) {
    }
    if (fam==null)
      throw new GedcomException(resources.getString("error.noenclosingfam"));

    // Prepare some VARs
    Property p;
    Property ps[];

    // Enclosing family has a wife already ?
    if (fam.getWife()!=null)
      throw new GedcomException(resources.getString("error.already.spouse", fam.getWife().toString(), fam.toString()));

    // Look for wife (not-existing -> Gedcom throws Exception)
    Indi wife = (Indi)getCandidate();

    // make sure wife isn't also husband
    if (fam.getHusband()==wife)
      throw new GedcomException(resources.getString("error.already.spouse", wife.toString(), fam.toString()));

    // make sure the wife isn't descendant of family
    if (wife.isDescendantOf(fam))
      throw new GedcomException(resources.getString("error.already.descendant", wife.toString(), fam.toString()));

    // Connect back from husband (maybe using invalid back reference)
    ps = wife.getProperties(PATH_INDIFAMS);
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
    wife.addProperty(pfs);
    link(pfs);

    // Done
  }

  /**
   * The expected referenced type
   */
  public String getTargetType() {
    return Gedcom.INDI;
  }

} //PropertyWife
