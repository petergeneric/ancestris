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

import genj.util.AncestrisPreferences;
import genj.util.Registry;
import genj.util.Resources;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Set of option for general gedcom file preferences
 */
public class GedcomOptions {
  
  private final static Resources RESOURCES = Resources.get(GedcomOptions.class);
  
  /** singleton */
    private static AncestrisPreferences gedcomOptions;

    private GedcomOptions() {
        //XXX: preference path must be defined in core options namespace
        gedcomOptions = Registry.get(GedcomOptions.class);
    }

    public static GedcomOptions getInstance() {
        return OptionsHolder.INSTANCE;
    }

    private static class OptionsHolder {

        private static final GedcomOptions INSTANCE = new GedcomOptions();
    }
  
 
  /** option - whether to use spaces in separating places */
    public boolean isUseSpacedPlaces() {
        return gedcomOptions.get("isUseSpacedPlaces",true);
    }

    public void setUseSpacedPlaces(boolean isUseSpacedPlaces) {
        gedcomOptions.put("isUseSpacedPlaces", isUseSpacedPlaces);
    }
  
  /** option - whether id-gaps should be filled */
    public boolean isFillGapsInIDs() {
        return gedcomOptions.get("isFillGapsInIDs",false);
    }

    public void setFillGapsInIDs(boolean isFillGapsInIDs) {
        gedcomOptions.put("isFillGapsInIDs", isFillGapsInIDs);
    }
  
  /** option - whether to convert last names to uppercase */
    private static final String UPPERCASE_NAME   = "gedcom.isUpperCaseNames";         // NOI18N
    public void setUpperCaseNames(boolean value) {
        getPreferences().put(UPPERCASE_NAME, value);
    }
    public boolean isUpperCaseNames() {
        return getPreferences().get(UPPERCASE_NAME, true);
    }

  
  /** option - whether to set wife's last name when person is created */
    public boolean isSetWifeLastname() {
        return gedcomOptions.get("setWifeLastname",true);
    }

    public void setSetWifeLastname(boolean setWifeLastname) {
        gedcomOptions.put("setWifeLastname", setWifeLastname);
    }
  
  /** option - whether to add GIVN|SURN information on name changes */
    public boolean isAddGivenSurname() {
        return gedcomOptions.get("isAddGivenSurname",false);
    }

    public void setAddGivenSurname(boolean isAddGivenSurname) {
        gedcomOptions.put("isAddGivenSurname", isAddGivenSurname);
    }
  
  /** option - whether to main AGE information for events */
    public boolean isAddAge() {
        return gedcomOptions.get("isAddAge",false);
    }

    public void setAddAge(boolean isAddAge) {
        gedcomOptions.put("isAddAge", isAddAge);
    }
  
  /** option - whether to prefer inline over records for notes and medias */
    public boolean isUseInline() {
        return gedcomOptions.get("isUseInline", false);
    }

    public void setUseInline(boolean isUseInline) {
        gedcomOptions.put("isUseInline", isUseInline);
    }
  
  /** option - whether to use "last,first" or "first last" */
    public NameFormat getNameFormat(){
        return gedcomOptions.get("nameFormat",NameFormat.LAST);
    }
  
    public void setNameFormat(NameFormat format){
        gedcomOptions.put("nameFormat",format);
    }

  public enum NameFormat{
      FIRST("option.nameFormat.first"),
      LAST("option.nameFormat.last");
        private final String description;
      private NameFormat(String desc){
          description = RESOURCES.getString(desc);
      }
        @Override
        public String toString() {
            return description;
        }      
};

  /** option - place hierarchy keys for city NOT EDITABLE ATM */
  Set<String> getPlaceHierarchyCityKeys(){
      return new HashSet<String>(Arrays.asList(new String[]{ "city", "commune", "ville", "stadt"}));
  }

  /** option - private information mask */
    public String getMaskPrivate(){
        return gedcomOptions.get("maskPrivate","...");
    }
    public void setMaskPrivate(String mask){
        gedcomOptions.put("maskPrivate", mask);
    }
    
  /** option - how to display dates */
  public GedcomDateFormat getDateFormat(){
      return gedcomOptions.get("dateFormat",GedcomDateFormat.SHORT);
  }
  public void setDateFormat(GedcomDateFormat format){
      gedcomOptions.put("dateFormat",format);
  }

  //XXX: move to pointintime?
public enum GedcomDateFormat{
      GEDCOM("option.dateFormat.gedcom"),
      SHORT("option.dateFormat.short"),
      LONG("option.dateFormat.long"),
      NUMERIC("option.dateFormat.numeric");
      private final String description;
      private GedcomDateFormat(String desc){
          description = RESOURCES.getString(desc);
      }
      public String getDescription(){
          return RESOURCES.getString(description);
      }

        @Override
        public String toString() {
            return description;
        }      
}

  /** option - maximum image files size to be loaded */  
  public void setMaxImageFileSizeKB(int max) {
    gedcomOptions.put("maxImageFileSizeKB",Math.max(4,max));
  }
  
  public int getMaxImageFileSizeKB() {
    return gedcomOptions.get("maxImageFileSizeKB",128);
  }
  
  
  /** option - where lines of multi line values should be broken */
  public int getValueLineBreak() {
      int valueLineBreak = gedcomOptions.get("valueLineBreak",246);
    return (valueLineBreak>MAX_LINE_BREAK)?MAX_LINE_BREAK:valueLineBreak;
  }

  public void setValueLineBreak(int set) {
    gedcomOptions.put("valueLineBreak",Math.max(40,Math.min(set, MAX_LINE_BREAK)));
  }
  private static final int MAX_LINE_BREAK = 246;

  /** option - text symbol for marriage */
  public String getTxtMarriageSymbol() {
    return gedcomOptions.get("txtMarriageSymbol","+");
  }
  public void setTxtMarriageSymbol(String set) {
      String txtMarriageSymbol;
    if (set!=null&&set.trim().length()>0)
      txtMarriageSymbol = ' '+set.trim()+' ';
    else
      txtMarriageSymbol = " + ";
    gedcomOptions.put("txtMarriageSymbol",txtMarriageSymbol);
  }

    /** option - number of undos */
  public int getNumberOfUndos() {
      return gedcomOptions.get("numberOfUndos",10);
  }
  public void setNumberOfUndos(int i) {
    gedcomOptions.put("numberOfUndos",Math.min(300,Math.max(10,i)));
  }

  /** option - default encoding is the last one in gedcom's list available */
  //XXX: don't rely on ENCODINGS implementation (use enums)
  public int getDefaultEncoding() {
      return gedcomOptions.get("defaultEncoding", Gedcom.ENCODINGS.length-1);
  }
  public void setDefaultEncoding(int setEncoding) {
    if (setEncoding>=0&&setEncoding<Gedcom.ENCODINGS.length)
      gedcomOptions.put("defaultEncoding", setEncoding);
  }
  
  public static String[] getDefaultEncodings() {
    return Gedcom.ENCODINGS;
  }

  /**
   * Ancestris way
   */
    private AncestrisPreferences getPreferences() {
        return gedcomOptions;
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

//TODO:    private static final String NAME_SPECIAL   = "gedcom.specialCharInName";         // NOI18N
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
        String format = getPreferences().get(PLACE_FORMAT,RESOURCES.getString("option.placeFormat"));
        format = format.replaceAll(" *, *", ",");
        if (spaceIsSeparator())
            format = format.replace(",", ", ");
        return format;
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
} 