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

import genj.option.Option;
import genj.option.OptionProvider;
import genj.option.PropertyOption;
import genj.util.Registry;
import genj.util.Resources;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Application options
 */
public class Options extends OptionProvider {
  
  private final static Resources RESOURCES = Resources.get(Options.class);
  
  /** singleton */
  private final static Options instance = new Options();
  
  /** option - whether to use spaces in separating places */
  public boolean isUseSpacedPlaces = true;
  
  /** option - whether id-gaps should be filled */
  public boolean isFillGapsInIDs = false;
  
  /** option - whether to convert last names to uppercase */
  //public boolean isUpperCaseNames = false;
    private static final String UPPERCASE_NAME   = "gedcom.isUpperCaseNames";         // NOI18N
    public void setUpperCaseNames(boolean value) {
        getPreferences().put(UPPERCASE_NAME, value);
    }
    public boolean isUpperCaseNames() {
        return getPreferences().get(UPPERCASE_NAME, true);
    }

  
  /** option - whether to set wife's last name when person is created */
  public boolean setWifeLastname = true;
  
  /** option - whether to add GIVN|SURN information on name changes */
  public boolean isAddGivenSurname = false;
  
  /** option - whether to main AGE information for events */
  public boolean isAddAge = false;
  
  /** option - whether to prefer inline over records for notes and medias */
  public boolean isUseInline = false;
  
  /** option - whether to use "last,first" or "first last" */
  public int nameFormat = 1;
  public final static String[] nameFormats = {
    RESOURCES.getString("option.nameFormat.first"),
    RESOURCES.getString("option.nameFormat.last")
};

  /** option - maximum image files size to be loaded */  
  private int maxImageFileSizeKB = 128;
  
  /** option - where lines of multi line values should be broken */
  private int valueLineBreak = 255;
  
  /** option - text symbol for marriage */
  protected String txtMarriageSymbol = "+";

  /** option - number of undos */
  protected int numberOfUndos = 10;
  
  /** option - place hierarchy keys for city NOT EDITABLE ATM */
  protected Set<String> placeHierarchyCityKeys = new HashSet<String>(Arrays.asList(new String[]{ "city", "commune", "ville", "stadt"}));
  
  /** option - private information mask */
  public String maskPrivate = "...";
    
  /** option - default encoding is the last one in gedcom's list available */
  protected int defaultEncoding = Gedcom.ENCODINGS.length-1;
  
  /** option - how to display dates */
  public int dateFormat = 1;
  
  public final static String[] dateFormats = {
      RESOURCES.getString("option.dateFormat.gedcom"),
      RESOURCES.getString("option.dateFormat.short"),
      RESOURCES.getString("option.dateFormat.long"),
      RESOURCES.getString("option.dateFormat.numeric")
  };

  /**
   * Singleton access
   */
  public static Options getInstance() {
    return instance;
  }

  /**
   * accessor - maxImageFileSizeKB
   */
  public void setMaxImageFileSizeKB(int max) {
    maxImageFileSizeKB = Math.max(4,max);
  }
  
  /**
   * accessor - maxImageFileSizeKB
   */
  public int getMaxImageFileSizeKB() {
    return maxImageFileSizeKB;
  }
  
  /**
   * accessor - valueLineBreak
   */
  public int getValueLineBreak() {
    return (valueLineBreak>246)?246:valueLineBreak;
  }

  /**
   * accessor - valueLineBreak
   */
  public void setValueLineBreak(int set) {
    valueLineBreak = Math.max(40,set);
  }

  /**
   * accessor - text marriage symbol
   */
  public String getTxtMarriageSymbol() {
    return txtMarriageSymbol;
  }

  /**
   * accessor - text marriage symbol
   */
  public void setTxtMarriageSymbol(String set) {
    if (set!=null&&set.trim().length()>0)
      txtMarriageSymbol = ' '+set.trim()+' ';
    else
      txtMarriageSymbol = " + ";
  }

  /**
   * accessor - number of undos
   */
  public int getNumberOfUndos() {
    return numberOfUndos;
  }

  /**
   * accessor - number of undos
   */
  public void setNumberOfUndos(int i) {
    numberOfUndos = Math.max(10,i);
  }

  /** 
   * Provider callback 
   */
  public List<? extends Option> getOptions() {
    return PropertyOption.introspect(instance);
  }

  /**
   * accessor - default encoding
   */
  public int getDefaultEncoding() {
    return defaultEncoding;
  }

  /**
   * accessor - default encoding
   */
  public void setDefaultEncoding(int setEncoding) {
    if (setEncoding>=0&&setEncoding<Gedcom.ENCODINGS.length)
      defaultEncoding = setEncoding;
  }
  
  public static String[] getDefaultEncodings() {
    return Gedcom.ENCODINGS;
  }

  /**
   * Ancestris way
   */
    private Registry getPreferences() {
        return Registry.get(genj.gedcom.Options.class);
    }

    private static final String ADD_NAME_SUBTAGS   = "gedcom.addNameSubtags";         // NOI18N
    public void setAddNameSubtags(boolean value) {
        getPreferences().put(ADD_NAME_SUBTAGS, value);
    }
    public boolean getAddNameSubtags() {
        return getPreferences().get(ADD_NAME_SUBTAGS, false);
    }

    /**
     * Given Name Choice (Prefer given name et special tag?)
     */
    private static final String GIVEN_TAG   = "gedcom.givenNameTag";         // NOI18N
    public void setGivenTag(String value) {
        getPreferences().put(GIVEN_TAG, value);
    }
    public String getGivenTag() {
        return getPreferences().get(GIVEN_TAG, "NICK");
    }

//XXX:    private static final String NAME_SPECIAL   = "gedcom.specialCharInName";         // NOI18N
//    /**
//     * Can NAME tag have special chars. For instance comma separators or bracket markers.
//     * <ul><li/>If so, NAME tag can be fully parsed to get every NAME structure parts in most cases.
//     * <li/>If not, to help parsing and to provide additionnal informations, subtags must be provided
//     * </ul>in any cases subtag may be provided and will have higher priority over informations
//     * found in NAME tag.
//     */
//    public boolean hasSpecialCharInName1() {
//        return getPreferences().get(NAME_SPECIAL, false);
//    }
//    public void setSpecialCharInName(boolean  value) {
//        getPreferences().put(NAME_SPECIAL, value);
//    }
//

    private static final String SPACE_IS_SEPARATOR   = "gedcom.spaceIsSeparator";         // NOI18N
    /**
     * True if space is considered as separator (same as comma) in name pieces
     * defaults to true
     */
    public boolean spaceIsSeparator() {
        return getPreferences().get(SPACE_IS_SEPARATOR, false);
    }
    public void setSpaceIsSeparator(boolean  value) {
        getPreferences().put(SPACE_IS_SEPARATOR, value);
    }

    private static final String ENTITY_ID_LENGTH   = "gedcom.entityIdLength";         // NOI18N
    /**
     * Length for entity Ids (excluding prefix). Default is 5 ie I99999
     */
    public int getEntityIdLength() {
        return getPreferences().get(ENTITY_ID_LENGTH, 5);
    }
    public void setEntityIdLength(int value) {
        if (value<0)
            value = 0;
        if (value>20)
            value = 20;
        getPreferences().put(ENTITY_ID_LENGTH, value);
    }

    private static final String CREATE_SPOUSE   = "gedcom.createSpouse";         // NOI18N
    public void setCreateSpouse(boolean createSpouse) {
        getPreferences().put(CREATE_SPOUSE, createSpouse);
    }
    public boolean getCreateSpouse() {
        return getPreferences().get(CREATE_SPOUSE, false);
    }

    private static final String PLACE_FORMAT = "gedcom.placeFormat";         // NOI18N
    public void setPlaceFormat(String placeFormatString) {
        getPreferences().put(PLACE_FORMAT,placeFormatString);
    }

    public String getPlaceFormat() {
        return getPreferences().get(PLACE_FORMAT,RESOURCES.getString("option.placeFormat"));
    }

    /**
     * Display Format
     */
    public static final String PLACE_DISPLAY_FORMAT = "gedcom.placeDisplayFormat";         // NOI18N
    public void setPlaceDisplayFormat(String format) {
        getPreferences().put(PLACE_DISPLAY_FORMAT,format);
    }

    public String getPlaceDisplayFormat(){
        return getPreferences().get(PLACE_DISPLAY_FORMAT, (String) null );
    }

    /**
     * Sort order
     */
    public static final String PLACE_SORT_ORDER = "gedcom.placeSortOrder";         // NOI18N
    public void setPlaceSortOrder(String order) {
        getPreferences().put(PLACE_SORT_ORDER,order);
    }

    public String getPlaceSortOrder(){
        return getPreferences().get(PLACE_SORT_ORDER, (String) null );
    }

    public static final String SHOW_PLACE_FORMAT = "gedcom.showJuridictions";         // NOI18N
    public void setShowJuridictions(Boolean [] showFormatString) {
        getPreferences().put(SHOW_PLACE_FORMAT,showFormatString);
    }

    public Boolean[] getShowJuridictions(){
        return getPreferences().get(SHOW_PLACE_FORMAT, getDefaultShowJuridictions() );
    }
    private Boolean [] getDefaultShowJuridictions(){
        String showJuri[] = RESOURCES.getString("option.showJuridictions").split(",");
        if (showJuri.length == 1)
            return new Boolean[]{true, true,true,true, true,true,true, true,true,true} ;
        Boolean result[] = new Boolean[showJuri.length];
        for (int i = 0; i<showJuri.length;i++){
            result[i] = Boolean.valueOf(showJuri[i].trim());
        }
        return result;
    }
} //Options
