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

import java.io.File;
import java.util.List;


/**
 * Class for encapsulating multimedia entry in a gedcom file
 */
public class Media extends Entity {
  
  private final static TagPath
    TITLE55 = new TagPath("OBJE:TITL"),
    TITLE551 = new TagPath("OBJE:FILE:TITL");
  
  private TagPath titlepath = TITLE55;

  /**
   * need tag,id-arguments constructor for all entities
   */
  public Media(String tag, String id) {
    super(tag, id);
    assertTag(Gedcom.OBJE);
  }
  
  /**
   * Title ...
   */
  @Override
  protected String getToStringPrefix(boolean showIds) {
    return getTitle();
  }
  
  /**
   * Overriden - special case for file association
   */
  public boolean addFile(File file) {

    // check for blob
    if (!getMetaProperty().allows("BLOB")) 
      return super.addFile(file);
      
    List<PropertyBlob> blobs = getProperties(PropertyBlob.class);
    PropertyBlob blob;
    if (blobs.isEmpty()) {
      blob = (PropertyBlob)addProperty("BLOB", "");
    } else {
      blob = (PropertyBlob)blobs.get(0);
    }
    // keep it
    return blob.addFile(file);
  }

  /**
   * Returns the file (if exists) for this OBJE
   */
  public File getFile() {
    Property file = getProperty("FILE", true);
    return (file instanceof PropertyFile) ? ((PropertyFile)file).getFile() : null;    
  }
  
  /**
   * Returns the property file for this OBJE
   */
  public PropertyBlob getBlob() {
    Property blob = getProperty("BLOB", true);
    return (blob instanceof PropertyBlob) ? (PropertyBlob)blob : null;    
  }
  
  /**
   * Returns the title of this OBJE
   */
  public String getTitle() {
    Property title = getProperty(titlepath);
    return title==null ? "" : title.getValue();
  }
  
  @Override
  void addNotify(Gedcom ged) {
    super.addNotify(ged);
    
    if (getMetaProperty().allows("TITLE"))
      titlepath = TITLE55;
    else
      titlepath = TITLE551;
      
  }

  public void setTitle(String title) {
    setValue(titlepath, title);
  }

} //Media
