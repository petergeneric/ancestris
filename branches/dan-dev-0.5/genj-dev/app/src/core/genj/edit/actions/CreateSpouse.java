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
import genj.gedcom.Property;

/**
 * Action that knows how to create a spouse for an individual 
 */
public class CreateSpouse extends CreateRelationship {
  
  private Indi spouse;
  
  /** constructor */
  public CreateSpouse(Indi spouse) {
    super( resources.getString("create.spouse"), spouse.getGedcom(), Gedcom.INDI);
    this.spouse = spouse;
    setImage(Indi.IMG_UNKNOWN);
  }
  
  /** warn about a spouse that already has spouses */
  public String getWarning(Entity target) {
    int n = spouse.getNoOfFams();
    if (n>0)
      return resources.getString("create.spouse.warning", spouse.toString(), ""+n );
    return null;
  }
  
  /** more about what this does */
  public String getDescription() {
    // "Spouse of Meier, Nils" or "Spouse"
    return resources.getString("create.spouse.of", spouse);
  }

  /** do it - add a target spouse (might be new) to our well known spouse */
  protected Property change(Entity target, boolean targetIsNew) throws GedcomException {
    
    // lookup family for spouse
    Fam[] fams = spouse.getFamiliesWhereSpouse();
    Fam fam = null;
    if (fams.length>0)
      fam = fams[0];
    if (fam==null||fam.getNoOfSpouses()>=2) {
      fam = (Fam)spouse.getGedcom().createEntity(Gedcom.FAM).addDefaultProperties();
      fam.setSpouse(spouse);
    }

    // done
    return fam.setSpouse((Indi)target).getTarget();
  }
  

}
