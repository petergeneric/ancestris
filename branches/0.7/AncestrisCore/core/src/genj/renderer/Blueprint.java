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
package genj.renderer;

/**
 * Encapsulating name and html for rendering an entity */
public class Blueprint {
  
  /** the entity tag this bp is for */
  private String tag;
  
  /** the name of this scheme */
  private String name;
  
  /** the html of this scheme */
  private String html;
  
  /** read-only */
  private boolean isReadOnly = false;
  
  /** dirty */
  private boolean isDirty = true;

  /**
   * Constructor - temporary blueprint w/o name
   */
  public Blueprint(String hTml) {
    html = hTml;
  }
    
  /**
   * Constructor - name, html and editable
   */
  /*package*/ Blueprint(String etag, String nAme, String hTml, boolean readOnly) {
    // remember
    tag = etag;
    name = nAme;
    html = hTml;
    isReadOnly = readOnly;
    // done
  }

  /**
   * Accessor - html
   */
  public void setSource(String html) {
    // o.k.?
    if (isReadOnly()) 
      throw new IllegalArgumentException("Can't change read-only Blueprint");
    // remember
    this.html = html;
    isDirty = true;
    // done
  }
  
  /**
   * clear dirty
   */
  /*package*/ void clearDirty() {
    isDirty = false;
  }
  
  /**
   * dirty check
   */
  /*package*/ boolean isDirty() {
    return isDirty;
  }
  
  /**
   * Accessor - html
   */
  public String getHTML() {
    return html;
  }

  /**
   * Accessor - name
   */
  public String getName() {
    return name;
  }
  
  /**
   * Accessor - readonly
   */
  /*package*/ boolean isReadOnly() {
    return isReadOnly;
  }
  
  /**
   * Entity this blueprint is for
   */
  public String getTag() {
    return tag;
  }
  
  @Override
  public String toString() {
    return getName();
  }
  
} //RenderingScheme
