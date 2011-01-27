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
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Property;
import genj.view.SelectionListener;
import genjfr.app.pluginservice.GenjFrPlugin;
import genjfr.util.GedcomObject.DummyNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.awt.UndoRedo;

/**
 * A static registry for Gedcom instances
 */
public class GedcomDirectory implements SelectionListener,GedcomMetaListener{
  
  private static GedcomDirectory instance;
  
  private List<Listener> listeners = new ArrayList<Listener>();
  private Map<Gedcom,GedcomObject> gedcomsOpened = new HashMap<Gedcom, GedcomObject>(5);
    private Context lastContext = null;;
  
  /** singleton constructor */
  private GedcomDirectory() {
      GenjFrPlugin.register(this);
  }

  /** singleton accessor */
  public static GedcomDirectory getInstance() {
      if (instance == null) {
          instance  = new GedcomDirectory();
//FIXME: a effacer        GenjFrPlugin.register(instance);
      }
    return instance;
  }
  
  /** register gedcom file */
  public void registerGedcom(Context context) {
      if (context == null){
          return;
      }
      Gedcom gedcom = context.getGedcom();
      if (!gedcomsOpened.containsKey(gedcom)){
          gedcomsOpened.put(gedcom, new GedcomObject(context));
      }

    List<Listener> ls = new ArrayList<Listener>(listeners);
    for (Listener listener : ls) 
      listener.gedcomRegistered(context);
  }
  
  /** unregister gedcom file */
  public void unregisterGedcom(Context context) {
      if (context == null){
          return;
      }
      gedcomsOpened.remove(context.getGedcom());
    List<Listener> ls = new ArrayList<Listener>(listeners);
    for (Listener listener : ls) 
      listener.gedcomUnregistered(context);
  }

  /** accessor gedcoms */
  public List<Context> getContexts() {
      List<Context> result = new ArrayList<Context>();
      for (Gedcom g:gedcomsOpened.keySet()){
          result.add(gedcomsOpened.get(g).getContext());
      }
      return result;
  }

    public Context getLastContext() {
        return lastContext;
    }

  
  /** listener */
  public void addListener(Listener listener) {
    listeners.add(listener);
  }
  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

    public void setContext(Context context, boolean isActionPerformed) {
        try {
            gedcomsOpened.get(context.getGedcom()).setContext(context);
            lastContext = context;

        } catch (NullPointerException e){}
    }

    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
        updateModified(gedcom);
    }

    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        updateModified(gedcom);
    }

    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        updateModified(gedcom);
    }

    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        updateModified(gedcom);
    }

    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
        updateModified(gedcom);
    }

    public void gedcomHeaderChanged(Gedcom gedcom) {
        updateModified(gedcom);
    }

    public void gedcomWriteLockAcquired(Gedcom gedcom) {
    }

    public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
    }

    public void gedcomAfterUnitOfWork(Gedcom gedcom) {
    }

    public void gedcomWriteLockReleased(Gedcom gedcom) {
        try {
            gedcomsOpened.get(gedcom).getUndoRedo().gedcomUpdated(gedcom);
            updateModified(gedcom);
        } catch (NullPointerException e) {}
  }

  public interface Listener {
    public void gedcomRegistered(Context context);
    public void gedcomUnregistered(Context context);
  }

  public UndoRedo getUndoRedo(Context c){
      try{
          return gedcomsOpened.get(c.getGedcom()).getUndoRedo();
      } catch (NullPointerException e){
          return null;
      }
  }

  public DummyNode getDummyNode(Context c){
      try{
          return gedcomsOpened.get(c.getGedcom()).getDummyNode();
      } catch (NullPointerException e){
          return null;
      }
  }

  /**
   * @deprecated use updateModified(gedcom) and let gedcomObject set its modified state using its own data
   * @param g
   * @param modified
   */
  public void setModified(Gedcom g, boolean  modified){
          gedcomsOpened.get(g).getDummyNode().fire(modified);
    }

    public void updateModified(Gedcom gedcom){
      try {
          gedcomsOpened.get(gedcom).getDummyNode().fire(gedcom.hasChanged());
      } catch (NullPointerException e){}
    }
}
