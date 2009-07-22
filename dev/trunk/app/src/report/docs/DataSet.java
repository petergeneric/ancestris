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
package docs;

import genj.gedcom.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Component for editing genealogic entity properties
 */
public class DataSet {

  /** the gedcom we're looking at */
  public int panelWidth = 930;

  /** the gedcom we're looking at */
  public Gedcom gedcom;

  /** data */
  public Entity[] families;
  public Entity[] indis;
  public Entity[] sources;
  public Entity[] repos;
  public String[] places;
  public String[] occupations;
  public String[] relations;

  /**
   * Constructor
   */
  public DataSet(Gedcom gedcom) {
    this.gedcom = gedcom;
    initialiseData();
    }


  /**
   * Initialises all the data to pass it on to the panels
   */
  public void initialiseData() {
    families = gedcom.getEntities(Gedcom.FAM, "FAM");
    indis = gedcom.getEntities(Gedcom.INDI, "INDI:NAME");
    sources = gedcom.getEntities(Gedcom.SOUR, "SOUR");
    repos = gedcom.getEntities(Gedcom.REPO, "REPO");
    places = buildList(gedcom, "PLAC");
    occupations = buildList(gedcom, "OCCU");
    relations = buildList(gedcom, "RELA");
  }

  /**
   * Find all places in Gedcom
   */
  private String[] buildList(Gedcom gedcom, String tag) {

     Collection entities = gedcom.getEntities();
     List<Property> props = new ArrayList();
     List<String> listStr = new ArrayList();
     for (Iterator it = entities.iterator(); it.hasNext();) {
        Entity ent = (Entity) it.next();
        getPropertiesRecursively((Property)ent, props, tag);
        }

     for (Iterator it = props.iterator(); it.hasNext();) {
        Property prop = (Property) it.next();
        String str = prop.toString();
        if ((str.length() != 0) && !listStr.contains(str)) {
           listStr.add(str);
           }
        }

     Collections.sort(listStr);
     return (String[]) (listStr.toArray(new String[listStr.size()]));
     }


  private void getPropertiesRecursively(Property parent, List props, String tag) {
      Property[] children = parent.getProperties();
      for (int c=0;c<children.length;c++) {
        Property child = children[c];
        if (child.getTag().equals(tag)) {
          props.add(child);
          }
        getPropertiesRecursively(child, props, tag);
        }
      }

}
