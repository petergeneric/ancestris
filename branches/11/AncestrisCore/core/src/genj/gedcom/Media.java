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

import genj.io.InputSource;
import java.util.List;
import java.util.Optional;


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
     * OBJE is valid if value is empty
     * @return boolean
     */
    @Override
    public boolean isValid() {
        return getValue().isEmpty();
    }
    
    @Override
    public void moveEntityValue() {
        String value = getValue();
        if (!value.isEmpty()) {
            if (getTitle().isEmpty()) {
                setTitle(value);
                setValue("");
            } else {
                super.moveEntityValue();
            }
        }
    }

    

  /**
   * Title ...
   */
  @Override
  protected String getToStringPrefix(boolean showIds) {
    return getTitle();
  }
  
    /**
     * Returns a user-readable media title
     * @return 
     */
    @Override
    public String getDisplayTitle() {
        int maxLen = 30;
        String str = getTitle().trim();
        if (!str.isEmpty() && maxLen != 0) {
            int len = str.length();
            if (len > maxLen) {
                int cut = str.indexOf(" ", maxLen);
                if (cut != -1) {
                    str = str.substring(0, cut);
                }
            }
        }
        if (str.isEmpty()) {
            str = "?";
        }
        return getId() + " - " + str;
    }

  

  /**
   * Overriden - special case for file association
   */
  public boolean addFile(InputSource file) {

    // check for blob
    if (!getMetaProperty().allows("BLOB")) 
      return super.addFile(file);
      
    List<PropertyBlob> blobs = getProperties(PropertyBlob.class);
    PropertyBlob blob;
    if (blobs.isEmpty()) {
      blob = (PropertyBlob)addProperty("BLOB", "");
    } else {
      blob = blobs.get(0);
    }
    // keep it
    return blob.addFile(file);
  }

  /**
   * Returns the file (if exists) for this OBJE
   */
  public InputSource getFile() {
    Property file = getProperty("FILE", true);
    if (file instanceof PropertyFile) {
        Optional<InputSource> ois = ((PropertyFile)file).getInput();
        if (ois.isPresent()) {
            return ois.get();
        } 
    }
    return null;
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
    
    if (getMetaProperty().allows("TITL"))
      titlepath = TITLE55;
    else
      titlepath = TITLE551;
      
  }

  public void setTitle(String title) {
    setValue(titlepath, title);
  }
  
} //Media
