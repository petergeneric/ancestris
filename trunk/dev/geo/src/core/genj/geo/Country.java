/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2005 Nils Meier <nils@meiers.net>
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
package genj.geo;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A country - why isn't that in java.util
 */
public class Country implements Comparable {
  
  public final static Country HERE = Country.get(Locale.getDefault().getCountry());
  
  private static Country[] ALL_COUNTRIES = null;
  
  private static Country DEFAULT_COUNTRY = null;
  
  private final static Map locale2countries = new HashMap();
  
  private final static Map displayName2Country = new WeakHashMap();
  
  /** state */
  private String iso;
  private String fips;
  private String displayName;
  
  /** constructor */
  private Country(String code) {
    iso = code.toLowerCase();
    displayName =  new Locale(Locale.getDefault().getLanguage(), code).getDisplayCountry();
  }
  
  /** constructor */
  private Country(String code, String displayName) {
    iso = code.toLowerCase();
    this.displayName = displayName;
  }
  
  /** name */
  public String getDisplayName() {
    return displayName;
  }
  
  /** string representation - it's name */
  public String toString() {
    return displayName;
  }
  
  /** iso code */
  public String getCode() {
    return iso;
  }
  
  /**
   * hash code - iso 
   */
  public int hashCode() {
    return iso.hashCode();
  }
  
  /** comparison - by name */
  public int compareTo(Object o) {
    return toString().compareTo(o.toString());
  }
  
  /** equals - same country name */
  public boolean equals(Object obj) {
    if (obj==null) return false;
    Country that = (Country)obj;
    return iso.equals(that.iso);
  }
  
  /**
   * Get default country
   */
  public static Country getDefaultCountry() {
    getAllCountries();
    return DEFAULT_COUNTRY;
  }
  
  /**
   * Get all countries
   */
  public static Country[] getAllCountries() {
    
    // known?
    if (ALL_COUNTRIES==null) {
    
      // grab all country codes
      String[] codes = Locale.getISOCountries(); 
      ALL_COUNTRIES = new Country[codes.length];
      for (int i=0;i<codes.length;i++) 
        ALL_COUNTRIES[i] = new Country(codes[i]);
    
      Arrays.sort(ALL_COUNTRIES);
      
      // grab default country
      DEFAULT_COUNTRY = get(Locale.getDefault().getCountry());
      
    }    
    
    // done
    return ALL_COUNTRIES;
  }
  
  /**
   * Lookup a country by code
   */
  public static Country get(String code) {
    return new Country(code);
  }
  
  /**
   * Lookup a country by name
   */
  public static Country get(Locale locale, String displayName) {
    
    // first a quick check for cached display names
    if (displayName2Country.containsKey(displayName))
      return (Country)displayName2Country.get(displayName);
    
    // do we have countries in given locale already?
    List countries = (List)locale2countries.get(locale);
    if (countries==null) {
      // build country list for that locale
      String[] codes = Locale.getISOCountries(); 
      countries = new ArrayList(codes.length);
      for (int i = 0; i < codes.length; i++) 
        countries.add(new Country(codes[i], new Locale(locale.getLanguage(), codes[i]).getDisplayCountry(locale)));
      // remember
      locale2countries.put(locale, countries);
    }
    
    // loop over names now
    Country result  = null;
    
    Collator collator = Collator.getInstance(locale);
    collator.setStrength(Collator.PRIMARY);
    for (int i = 0; i < countries.size(); i++) {
      Country country = (Country)countries.get(i);
      // compare country code
// 20060314 nice idea to try to match country iso codes
// problem is that they also match US states which *will*
// show up in the jurisdictions. For that reason I'm
// reverting this back - users will better write readable
// country names instead of iso country codes
//      if (country.iso.equalsIgnoreCase(displayName)) {
//        result = country;
//        break;
//      } else 
      if (collator.compare(country.getDisplayName(), displayName)==0) {
        // create a private instance for this display name
        result = new Country(country.iso, displayName);
        break;
      }
    }
    
    // not found? try english
    if (result==null&&!locale.getLanguage().equals(Locale.ENGLISH.getLanguage()))
        result = get(Locale.ENGLISH, displayName);

    // cache it under displayName for next time
    displayName2Country.put(displayName, result);
    
    // done
    return result;
  }

} //Country