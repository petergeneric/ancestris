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

/**
 * Class for encapsulating a repository
 */
public class Repository extends Entity {

  /**
   * need tag,id-arguments constructor for all entities
   */
  public Repository(String tag, String id) {
    super(tag, id);
    assertTag(Gedcom.REPO);
  }


    /**
     * Repository is valid if value is empty
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
            if (getRepositoryName().isEmpty()) {
                setRepositoryName(value);
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
    return getRepositoryName();
  }

    /**
     * Returns a user-readable repository title
     * @return 
     */
    @Override
    public String getDisplayTitle() {
        int maxLen = 30;
        String str = getRepositoryName().trim();
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
   * Returns the name of this repository
   */
  public String getRepositoryName() {
    Property name = getProperty("NAME");
    return name!=null ? name.getValue() : ""; 
  }
  
  /**
   * Sets the name of this repository
   */
  public void setRepositoryName(String name) {
    Property property = getProperty("NAME");
    if (property == null) {
        property = addProperty("NAME", name);
    } else {
        property.setValue(name);
    }
  }
  
} //Repository
