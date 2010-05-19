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
package genjreports.docs;

import genj.gedcom.Gedcom;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.GedcomException;
import genj.util.Registry;

import javax.swing.JPanel;

/**
 * The base class for our two editors basic and advanced
 */
/*package*/ abstract class EditorDocs extends JPanel {

  /**
   * Initializer (post constructor)
   */
  public abstract void init(Gedcom gedcom, Entity entity, EditDocsPanel view, Registry registry, DataSet dataSet);

  /**
   * Get name
   */
  public abstract String getTitle();

  /**
   * Update Gedcom
   */
  public abstract Entity updateGedcom() throws GedcomException;

} //Editor
