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
package genj.view;

import genj.gedcom.Gedcom;
import genj.util.Registry;

import javax.swing.JComponent;

/**
 * A handle to an open view
 */
public class ViewHandle {
  
  /** the view manager */
  private ViewManager manager;

  /** the gedcom file the view is for */
  private Gedcom gedcom;
  
  /** the view */
  private JComponent view;
  
  /** the title of the view */
  private String title;
  
  /** the registry of the view */
  private Registry registry;
  
  /** the factory of the view */
  private ViewFactory factory;
  
  /** the number of open views of the same type */
  private int sequence;
  
  /** constructor */
  /*package*/ ViewHandle(ViewManager manager, Gedcom gedcom, String title, Registry registry, ViewFactory factory, JComponent view, int sequence) {
    this.manager = manager;
    this.gedcom = gedcom;
    this.title = title;
    this.registry = registry;
    this.view = view;
    this.factory = factory;
    this.sequence = sequence;
  }
  
  /** the gedcom file */
  public Gedcom getGedcom() {
    return gedcom;
  }
  
  /** the title */
  public String getTitle() {
    return title;
  }
  
  /** the view */
  public JComponent getView() {
    return view;
  }
  
  /** the factory */
  public ViewFactory getFactory() {
    return factory;
  }
  
  /** the sequence number */
  public int getSequence() {
    return sequence;
  }
  
  /** the registry */
  public Registry getRegistry() {
    return registry;
  }
  
  /** the view manager */
  public ViewManager getManager() {
    return manager;
  }
  
  /** the window key */
  /*package*/ String getKey() {
    return gedcom.getName() + "." + manager.getPackage(factory) + "." + sequence;
  }
  
  /** persist */
  public String persist() {
    return factory.getClass().getName() + "#" + sequence;
  }
  
  /** 
   * Restore a view based on a persisted string from persist()
   * @return handle of opened view or null
   */
  public static ViewHandle restore(ViewManager manager, Gedcom gedcom, String persisted) {

    try {
      int  hash = persisted.indexOf('#');
      ViewFactory factory = (ViewFactory)Class.forName(persisted.substring(0,hash).trim()).newInstance();
      int sequence = Integer.parseInt(persisted.substring(hash+1).trim());
      return manager.openView(gedcom, factory, sequence); 
    } catch (Throwable t) {
      return null;
    }
 
    // done
  }
  
}
