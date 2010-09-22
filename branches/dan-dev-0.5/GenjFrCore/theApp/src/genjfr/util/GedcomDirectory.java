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
package genjfr.util;

import genj.gedcom.Context;
import java.util.ArrayList;
import java.util.List;

/**
 * A static registry for Gedcom instances
 */
public class GedcomDirectory {
  
  private static GedcomDirectory instance = new GedcomDirectory();
  
  private List<Context> gedcoms = new ArrayList<Context>();
  private List<Listener> listeners = new ArrayList<Listener>();
  
  /** singleton constructor */
  private GedcomDirectory() {
  }

  /** singleton accessor */
  public static GedcomDirectory getInstance() {
    return instance;
  }
  
  /** register gedcom file */
  public void registerGedcom(Context context) {
    gedcoms.add(context);
    List<Listener> ls = new ArrayList<Listener>(listeners);
    for (Listener listener : ls) 
      listener.gedcomRegistered(context);
  }
  
  /** unregister gedcom file */
  public void unregisterGedcom(Context context) {
      for (Context c:findContext(context)){
    gedcoms.remove(c);
    List<Listener> ls = new ArrayList<Listener>(listeners);
    for (Listener listener : ls) 
      listener.gedcomUnregistered(context);
      }
  }

  /** accessor gedcoms */
  public List<Context> getGedcoms() {
    return new ArrayList<Context>(gedcoms);
  }


  
  /** listener */
  public void addListener(Listener listener) {
    listeners.add(listener);
  }
  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

    public void selectionChanged(Context context) {
        for (int i=0;i<gedcoms.size();i++){
            if (gedcoms.get(i).getGedcom().equals(context.getGedcom()))
                gedcoms.set(i, context);
        }
    }

  public interface Listener {
    public void gedcomRegistered(Context context);
    public void gedcomUnregistered(Context context);
  }

  private List<Context> findContext(Context ctx){
      List<Context> result = new ArrayList<Context>();
    for (Context c:gedcoms){
        if (c.getGedcom().equals(ctx.getGedcom()))
            result.add(c);
    }
    return result;
    }
}
