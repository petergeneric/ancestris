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
 * Class for encapsulating a submitter
 */
public class Submitter extends Entity {
  
  private final static TagPath PATH_NAME =new TagPath("SUBM:NAME");
  private final static TagPath PATH_ADDR =new TagPath("SUBM:ADDR");
  private final static TagPath PATH_CITY =new TagPath("SUBM:ADDR:CITY");
  private final static TagPath PATH_STAE =new TagPath("SUBM:ADDR:STAE");
  private final static TagPath PATH_POST =new TagPath("SUBM:ADDR:POST");
  private final static TagPath PATH_CTRY =new TagPath("SUBM:ADDR:CTRY");
  private final static TagPath PATH_PHON =new TagPath("SUBM:PHON");
  private final static TagPath PATH_EMAI =new TagPath("SUBM:_EMAIL");
  private final static TagPath PATH_WEB  =new TagPath("SUBM:_WWW");

  /**
   * need tag,id-arguments constructor for all entities
   */
  public Submitter(String tag, String id) {
    super(tag, id);
    assertTag(Gedcom.SUBM);
  }
  
  
    /**
     * Submitter is valid if value is empty
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
            if (getName().isEmpty()) {
                setName(value);
                setValue("");
            } else {
                super.moveEntityValue();
            }
        }
    }

    
  
  /**
   * Name ...
   */
  @Override
  protected String getToStringPrefix(boolean showIds) {
    return getName();
  }
  
    /**
     * Returns a user-readable submitter title
     * @return 
     */
    @Override
    public String getDisplayTitle() {
        int maxLen = 30;
        String str = getName().trim();
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
   * Name of Submitter
   */
  public String getName() {
    return getValue(PATH_NAME, "");
  }
  
  /**
   * Name of Submitter
   */
  public void setName(String name) {
    setValue(PATH_NAME, name);
  }

  /**
   * Address of Submitter
   */
  public String getAddress() {
    return getValue(PATH_ADDR, "");
  }

  public void setAddress(String name) {
    setValue(PATH_ADDR, name);
  }

  /**
   * City of Submitter
   */
  public String getCity() {
    return getValue(PATH_CITY, "");
  }

  public void setCity(String name) {
    setValue(PATH_CITY, name);
  }

  /**
   * State of Submitter
   */
  public String getState() {
    return getValue(PATH_STAE, "");
  }

  public void setState(String name) {
    setValue(PATH_STAE, name);
  }

  /**
   * Postcode of Submitter
   */
  public String getPostcode() {
    return getValue(PATH_POST, "");
  }

  public void setPostcode(String name) {
    setValue(PATH_POST, name);
  }

  /**
   * Country of Submitter
   */
  public String getCountry() {
    return getValue(PATH_CTRY, "");
  }

  public void setCountry(String name) {
    setValue(PATH_CTRY, name);
  }

  /**
   * Phone of Submitter
   */
  public String getPhone() {
    return getValue(PATH_PHON, "");
  }

  public void setPhone(String name) {
    setValue(PATH_PHON, name);
  }

  /**
   * Email of Submitter
   */
  public String getEmail() {
    return getValue(PATH_EMAI, "");
  }

  public void setEmail(String name) {
    setValue(PATH_EMAI, name);
  }

  /**
   * Web address of Submitter
   */
  public String getWeb() {
    return getValue(PATH_WEB, "");
  }

  public void setWeb(String name) {
    setValue(PATH_WEB, name);
  }
  
} //Submitter
