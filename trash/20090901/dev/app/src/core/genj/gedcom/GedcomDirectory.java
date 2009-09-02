/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2008 Nils Meier <nils@meiers.net>
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
 * 
 * $Revision$ $Author$ $Date$
 */
package genj.gedcom;

import java.util.ArrayList;
import java.util.List;

/**
 * A static registry for Gedcom instances
 */
public class GedcomDirectory {
  
  private static GedcomDirectory instance = new GedcomDirectory();
  
  private List<Gedcom> gedcoms = new ArrayList<Gedcom>();
  private List<Listener> listeners = new ArrayList<Listener>();
  
  /** singleton constructor */
  private GedcomDirectory() {
  }

  /** singleton accessor */
  public static GedcomDirectory getInstance() {
    return instance;
  }
  
  /** register gedcom file */
  public void registerGedcom(Gedcom gedcom) {
    gedcoms.add(gedcom);
    List<Listener> ls = new ArrayList<Listener>(listeners);
    for (Listener listener : ls) 
      listener.gedcomRegistered(gedcoms.size()-1, gedcom);
  }
  
  /** unregister gedcom file */
  public void unregisterGedcom(Gedcom gedcom) {
    int i = gedcoms.indexOf(gedcom);
    gedcoms.remove(gedcom);
    List<Listener> ls = new ArrayList<Listener>(listeners);
    for (Listener listener : ls) 
      listener.gedcomUnregistered(i, gedcom);
  }

  /** accessor gedcoms */
  public List<Gedcom> getGedcoms() {
    return new ArrayList<Gedcom>(gedcoms);
  }

  /** gedcom lookup */
  public Gedcom getGedcom(String name) {
    for (Gedcom g : getGedcoms()) {
      if (g.getName().equals(name))
        return g;
    }
    return null;
  }

  
  /** listener */
  public void addListener(Listener listener) {
    listeners.add(listener);
  }
  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }
  public interface Listener {
    public void gedcomRegistered(int num, Gedcom gedcom);
    public void gedcomUnregistered(int num, Gedcom gedcom);
  }
}
