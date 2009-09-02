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

import genj.option.OptionProvider;
import genj.option.PropertyOption;
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
  public boolean isUpperCaseNames = false;
  
  /** option - wether to set wife lastname when indi is created */
  public boolean setWifeLastname = true;
  
  /** option - whether to use "last,first" or "first last" */
  public int nameFormat = 0;
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
  protected Set placeHierarchyCityKeys = new HashSet(Arrays.asList(new String[]{ "city", "commune", "ville", "stadt"}));
  
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
    return valueLineBreak;
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
  public List getOptions() {
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

} //Options
