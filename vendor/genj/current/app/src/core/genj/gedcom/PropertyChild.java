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

import genj.util.swing.ImageIcon;

/**
 * Gedcom Property : CHIL
 * The property wrapping the condition of having a child in a family
 */
public class PropertyChild extends PropertyXRef {

  private final static TagPath
    PATH_FAMCHIL = new TagPath("FAM:CHIL");
  
  public final static ImageIcon
    IMG_MALE    = Grammar.V55.getMeta(PATH_FAMCHIL).getImage("male"),
    IMG_FEMALE  = Grammar.V55.getMeta(PATH_FAMCHIL).getImage("female"),
    IMG_UNKNOWN = Grammar.V55.getMeta(PATH_FAMCHIL).getImage();

  /**
   * Empty Constructor
   */
  public PropertyChild() {
  }
  
  /**
   * Constructor
   */
  protected PropertyChild(String target) {
    setValue(target);
  }
  
  /**
   * Returns the child
   * @return referenced child
   */
  public Indi getChild() {
    return (Indi)getTargetEntity();
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
    return resources.getString("prop.chil.veto");
  }

  /**
   * Returns the Gedcom-Tag of this property
   * @return tag as <code>String</code>
   */
  public String getTag() {
    return "CHIL";
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when property has no parent property,
   * referenced individual is child, wife or husband in enclosing family
   * or it would become ancestor of itself by this action.
   */
  public void link() throws GedcomException {

    // Get enclosing family ?
    Fam fam;
    try {
      fam = (Fam)getEntity();
    } catch (ClassCastException ex) {
      throw new GedcomException(resources.getString("error.noenclosingfam"));
    }
    Indi father = fam.getHusband();
    Indi mother = fam.getWife();

    // Prepare some VARs
    Property p;
    Property ps[];
    Gedcom gedcom = getGedcom();

    // Look for child (not-existing -> Gedcom throws Exception)
    Indi child = (Indi)getCandidate();

    // Make sure the child is not ancestor of family already (father, mother, grandfather, grandgrandfather, ...) 
    // .. that would introduce a circle
    if (child.isAncestorOf(fam)) 
      throw new GedcomException(resources.getString("error.already.ancestor", new String[]{ child.toString(), fam.toString()}));

    // NM20070921 - see PropertyFamilyChild.link()
    
    // Connect back from child (maybe using back reference)
    List famcs = child.getProperties(PropertyFamilyChild.class);
    for (int i=0, j=famcs.size(); i<j; i++) {
      
      PropertyFamilyChild pfc = (PropertyFamilyChild)famcs.get(i);
      if (pfc.isCandidate(fam)) {
        link(pfc);
        return;
      }        
      
    }

    // .. new back referencing property
    PropertyFamilyChild pfc = new PropertyFamilyChild();
    child.addProperty(pfc);
    link(pfc);

    // Done
  }
  
  /**
   * an externalized label we need in some ui actions
   */
  public static String getLabelChildAlreadyinFamily(Indi child, Fam fam) {
    return resources.getString("error.already.child", new String[]{ child.toString(), fam.toString()});
  }

  /**
   * The expected referenced type
   */
  public String getTargetType() {
    return Gedcom.INDI;
  }
  
  /**
   * @see genj.gedcom.PropertyXRef#getImage(boolean)
   */
  public ImageIcon getImage(boolean checkValid) {
     // check it
    Indi child = getChild();
    if (child==null) return super.getImage(checkValid);
    switch (child.getSex()) {
      case PropertySex.MALE: return overlay(IMG_MALE);
      case PropertySex.FEMALE: return overlay(IMG_FEMALE);
      default: return overlay(IMG_UNKNOWN);
    }
  }

} //PropertyChild
