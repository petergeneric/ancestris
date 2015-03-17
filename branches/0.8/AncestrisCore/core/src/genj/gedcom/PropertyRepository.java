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
 * Gedcom Property : REPO 
 * Class for encapsulating a repository as property
 */
public class PropertyRepository extends PropertyXRef {

  /** the repository's content */
  private String repository;
  
  /**
   * need tag-argument constructor for all properties
   */
  public PropertyRepository(String tag) {
    super(tag);
    assertTag("REPO");
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when processing link would result in inconsistent state
   */
  public void link() throws GedcomException {

    // Look for Repository
    Repository repository = (Repository)getCandidate();

    // Create Backlink
    PropertyForeignXRef fxref = new PropertyForeignXRef();
    repository.addProperty(fxref);

    // ... and point
    link(fxref);

    // don't delete anything because we may have children, like PAGE
  }

  /**
   * The expected referenced type
   */
  public String getTargetType() {
    return Gedcom.REPO;
  }
  
  /**
   * @see genj.gedcom.PropertyXRef#isValid()
   */
  public boolean isValid() {
    // always
    return true;
  }
  
} //PropertyRepository
