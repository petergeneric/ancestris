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
package genj.tree;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.util.swing.Action2;

/**
 * A Bookmark in viewing a tree
 * <il>
 * <li>the bookmark'd entity
 * <li>the name
 * </il>
 */
public class Bookmark extends Action2 {
  
  /** the tree */
  private TreeView tree;
  
  /** the name */
  private String name;
  
  /** the entity */
  private Entity entity;
  
  /**
   * Internal Constructor
   */
  /*package*/ Bookmark(TreeView t, Gedcom ged, String s) throws IllegalArgumentException {
    
    // grab name and id from s
    int at = s.indexOf('#');
    if (at<0) throw new IllegalArgumentException("id#expected name");
    
    tree = t;
    name = s.substring(at+1);
    String id = s.substring(0,at);
    
    // resolve entity
    entity = ged.getEntity(id);
    if (!(entity instanceof Indi||entity instanceof Fam))
      throw new IllegalArgumentException("id "+id+" doesn't point to Indi or Fam");
  
    // setup text
    setText(name);
    setImage(Gedcom.getEntityImage(entity.getTag()));
  }
  
  /**
   * Constructor
   */
  public Bookmark(TreeView t, String n, Entity e) {
    tree = t;
    name = n;
    entity = e;
  
    setText(name);
    setImage(Gedcom.getEntityImage(entity.getTag()));
  }
  
  /**
   * Accessor - name
   */
  public String getName() {
    return name;
  }
  
  /**
   * Accessor - entity
   */
  public Entity getEntity() {
    return entity;
  }
  
  /**
   * @see genj.util.swing.Action2#execute()
   */
  protected void execute() {
    // Either scroll to or change root
    TreeNode node = tree.getModel().getNode(entity);
    if (node!=null)
      tree.setCurrent(entity);
    else
      tree.setRoot(entity);
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return entity.getId()+'#'+name;
  }
  

} //Bookmark
