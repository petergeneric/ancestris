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
package genj.edit.actions;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Options;
import genj.gedcom.Property;
import genj.gedcom.PropertyChild;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import genj.view.ViewManager;

/**
 * Create a child of a family or person
 */
public class CreateParent extends CreateRelationship {
  
  /** the child and family we're creating a parent for */
  private Indi child;
  private Fam family;
  
  /** constructor */
  public CreateParent(Fam family, ViewManager mgr) {
    super(resources.getString("create.parent"), family.getGedcom(), Gedcom.INDI, mgr);
    if (family.getNoOfSpouses()>=2)
      throw new IllegalArgumentException("can't create additional parent in family with husband and wife");
    this.family = family;
    this.child = null;
  }
  
  /** constructor */
  public CreateParent(Indi child, ViewManager mgr) {
    super(resources.getString("create.parent"), child.getGedcom(), Gedcom.INDI, mgr);
    this.child = child;
    
    // check if the child already is part of a family without spouse
    Fam[] fams = child.getFamiliesWhereChild();
    for (int f = 0; f < fams.length; f++) {
      if (fams[f].getNoOfSpouses()<2) {
        family = fams[f];
        break;
      }
    }
    
    // done
  }

  /** description of what this'll do */
  public String getDescription() {
    // "Parent of Meier, Nils (I1)"
    if (child!=null)
      return resources.getString("create.parent.of", child);
    // "Parent in Meier, Sven (I1) + Radovcic Sandra (I2) (F1)"
    return resources.getString("create.parent.in", family);
  }

  /** a warning in case the target indi is already a child of another family */
  public String getWarning(Entity indi) {
    
    // do we have a child which already has parents?
    if (child!=null&&family==null) {
      Fam fam =child.getFamilyWhereBiologicalChild();
      if (fam!=null)
        return PropertyChild.getLabelChildAlreadyinFamily(child, fam);
    }
    // no problem
    return null;
    
  }

  /** change impl */
  protected Property change(Entity parent, boolean parentIsNew) throws GedcomException {
    
    String lastname;
    Gedcom ged = parent.getGedcom();
    PropertyXRef FAMS;
    
    // know the family already?
    if (family!=null) {

      FAMS = family.setSpouse((Indi)parent).getTarget();
      Indi other = family.getOtherSpouse((Indi)parent);
      lastname = other!=null ? other.getLastName() : "";
      
    } else { // need new family

      // lastname will match that of child
      lastname = child.getLastName();

      // create new family with child
      family = (Fam)ged.createEntity(Gedcom.FAM);
      family.addChild(child);
      family.addDefaultProperties();
      
      // set spouse
      FAMS = family.setSpouse((Indi)parent).getTarget();
      
      // 20040619 adding missing spouse automatically now
      // 20050405 whether we created a new family or the family didn't have all parents
      if (family.getNoOfSpouses()<2) {
        Indi spouse = (Indi)ged.createEntity(Gedcom.INDI);
        spouse.addDefaultProperties();
        family.setSpouse(spouse);
        if ( Options.getInstance().setWifeLastname || spouse.getSex() == PropertySex.MALE)  
        spouse.setName("", lastname);
      }
      
    }
    
    // set name of parent if new
    if (parentIsNew && (((Indi)parent).getSex() == PropertySex.MALE||Options.getInstance().setWifeLastname)) 
      ((Indi)parent).setName("", lastname);

    // focus goes to new parent
    return FAMS;      
  }

}
