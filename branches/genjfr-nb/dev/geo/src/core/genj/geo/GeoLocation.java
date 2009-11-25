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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyComparator;
import genj.gedcom.PropertyPlace;
import genj.io.Filter;
import genj.util.DirectAccessTokenizer;
import genj.util.WordBuffer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;

/**
 *  Information about a geographic location
 */
public class GeoLocation extends Point implements Feature, Comparable {

  /** "locale to displayCountries to country-codes"*/
  private static Map locale2displayCountry2code = new HashMap();
  
  /** 
   * our schema - could be more complicated like
   * schema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
   * schema.addAttribute("PLAC", AttributeType.STRING);
   */
  /*package*/ final static FeatureSchema SCHEMA = new FeatureSchema();
  
  /*package*/  final static GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  /** the coordinate of this location */
  private Coordinate coordinate;

  /** city state and country */
  private String city;
  private Country country;
  private List jurisdictions = new ArrayList(3);
  private int hash;
  
  /** properties at that location */
  protected List properties = new ArrayList();
  
  /** match count - 0 = couldn't be matched - 1 = exact match - n = too many matches */
  private int matches = 0;
  
  /**
   * Constructor
   * @param prop property that needs location
   */
  /*package*/ GeoLocation(String city, String jurisdiction, Country country) {
    
    super(GEOMETRY_FACTORY.getCoordinateSequenceFactory().create(new Coordinate[]{ new Coordinate() } ), GEOMETRY_FACTORY);
    coordinate = super.getCoordinate();
    
    this.city = city;
    if (jurisdiction!=null) this.jurisdictions.add(jurisdiction);
    this.country = country;
    
  }
  
  /**
   * Constructor
   * @param prop property that needs location
   */
  public GeoLocation(Property prop) {
    super(GEOMETRY_FACTORY.getCoordinateSequenceFactory().create(new Coordinate[]{ new Coordinate() } ), GEOMETRY_FACTORY);
    
    // remember coordinate
    coordinate = super.getCoordinate();
    
    // remember property
    properties.add(prop);
    
    // init
    Property plac = prop.getProperty("PLAC");
    Property addr = prop.getProperty("ADDR");
    
    // got a place?
    if (plac instanceof PropertyPlace) {
       parsePlace( (PropertyPlace)plac );
    } else if (addr!=null) {
        parseAddress(addr);
    } else {
      throw new IllegalArgumentException("can't locate "+prop.getTag()+" "+prop);
    }
    
    // done
  }
  
  /**
   * Jurisdictions as comma-separated Strings
   */
  public String getJurisdictionsAsString() {
    WordBuffer result = new WordBuffer(", ");
    result.append(city);
    for (int i = 0; i < jurisdictions.size(); i++) 
      result.append(jurisdictions.get(i));
    result.append(country);
    return result.toString();
  }  
  
  /**
   * Convert coord to lat/lon String
   */
  public String getCoordinateAsString() {
    return getCoordinateAsString(coordinate);
  }
  public static String getCoordinateAsString(Coordinate coord) {
    return getCoordinateAsString(coord.y, coord.x);
  }
  public static String getCoordinateAsString(double lat, double lon) {
    if (Double.isNaN(lat)||Double.isNaN(lon))
      return "n/a";
    char we = 'E', ns = 'N';
    if (lat<0) { lat = -lat; ns='S'; }
    if (lon<0) { lon = -lon; we='W'; }
    DecimalFormat format = new DecimalFormat("0.0");
    return ns + format.format(lat) + " " + we + format.format(lon);
  }
  
  /** 
   * Our coordinate
   */
  public Coordinate getCoordinate() {
    return coordinate;
  }

  /**
   * Create locations for all properties contained in given entities
   * @param entitie entities to consider
   * @return set of GeoLocations
   */
  public static Set parseEntities(Collection entities) {
    return parseEntities(entities, null);
  }
  
  /**
   * Create locations for all properties contained in given entities
   * @param entitie entities to consider
   * @return set of GeoLocations
   */
  public static Set parseEntities(Collection entities, Filter filter) {
    
    // loop over entities
    List props = new ArrayList(100);
    for (Iterator it=entities.iterator(); it.hasNext(); ) {
      Entity entity = (Entity)it.next();
      for (int p=0; p<entity.getNoOfProperties(); p++) {
        Property prop = entity.getProperty(p);
        if ( (prop.getProperty("PLAC")!=null||prop.getProperty("ADDR")!=null) && (filter==null||filter.checkFilter(prop))) 
          props.add(prop);
      }
    }
    
    // check properties now
    return parseProperties(props);
  }

  /**
   * Create locations for given collection of properties
   * @param properties the properties to match
   * @return set of GeoLocations
   */
  public static Set parseProperties(Collection properties) {
    
    // prepare result
    Map result = new HashMap(properties.size());
    List todo = new ArrayList();
    
    // loop over properties
    for (Iterator it = properties.iterator(); it.hasNext(); ) {
      
      Property prop = (Property)it.next();

      // create a new location
      GeoLocation location;
      try {
        location = new GeoLocation(prop);
      } catch (IllegalArgumentException e) {
        continue;
      }
    
      // check if we have a location like that or keep as first
      GeoLocation other = (GeoLocation)result.get(location);
      if (other!=null) {
        other.add(location);
        continue;
      }
      result.put(location, location);
    
      // next property
    }
    
    // done
    return result.keySet();
  }
  
  /**
   * Init for Address
   */
  private void parseAddress(Property addr) {
    
    Gedcom ged =addr.getGedcom();
    
    // 20050615 we don't look for a country in PLAC since that's really CTRY there for - since
    // STAtE doesn't fit addresses outside the US we're using the PLAC parsing mechanism
    // for topmost jurisdiction as well though
    Property pcity = addr.getProperty("CITY");
    if (pcity==null)
      throw new IllegalArgumentException("can't determine city from address");
    parseJurisdictions( pcity.getDisplayValue(), ged, false);
    
    // how about a country?
    Locale locale = addr.getGedcom().getLocale();
    Property pcountry = addr.getProperty("CTRY");
    if (pcountry!=null)  
      country = Country.get(ged.getLocale(), trim(pcountry.getDisplayValue()));
    
    // still need a a state?
    Property pstate = addr.getProperty("STAE");
    if (pstate!=null) {
      String state = pstate.getDisplayValue();
      if (state.length()>0) jurisdictions.add(state);
    }
    
    // good
    return;
  }
  
  /**
   * Init for Place
   */
  private void parsePlace(PropertyPlace place) {
    // go ahead - check jurisdictions now starting with city
    parseJurisdictions(place.getValueStartingWithCity(), place.getGedcom(), true);
  }
  
  /**
   * Parse jurisdictions
   */
  private void parseJurisdictions(String jurisdictions, Gedcom gedcom, boolean lookForCountry) {
    
    DirectAccessTokenizer tokens = new DirectAccessTokenizer(jurisdictions, PropertyPlace.JURISDICTION_SEPARATOR);
    int first = 0, last = tokens.count()-1;
    
    // city is simply the first non-empty jurisdiction and required
    while (true) {
      city = trim(tokens.get(first++));
      if (city==null)
        throw new IllegalArgumentException("can't determine jurisdiction's city");
      if (city.length()>0) 
        break;
    }
    
    // look for country in rightmost jurisdiction
    if (lookForCountry) {
      Locale locale = gedcom.getLocale();
      if (last>=first) {
        country = Country.get(gedcom.getLocale(), trim(tokens.get(last)));
        if (country!=null) 
          last--;
      }
    }
    
    // grab all the rest as jurisdictions 
    for (int i=first; i<=last; i++) {
      String jurisdiction = trim(tokens.get(i));
      if (jurisdiction.length()>0) this.jurisdictions.add(jurisdiction);
    }
    
    // done
  }
  
  /**
   * trim a jurisdiction
   */
  private String trim(String jurisdiction) {
    if (jurisdiction==null)
      return null;
    for (int i=0, j=jurisdiction.length(); i<j ;i++) {
      char c = jurisdiction.charAt(i); 
      if (c=='(' || c=='[' || c=='/' || c=='\\')
         return jurisdiction.substring(0, i).trim();
    }
    return jurisdiction.trim();
  }
  
  /**
   * Add poperties from another instance
   */
  public void add(GeoLocation other) {
    for (Iterator it = other.properties.iterator(); it.hasNext(); ) {
      Object prop = it.next();
      if (!properties.contains(prop))
        properties.add(prop);
    }
    Collections.sort(properties, new PropertyComparator(".:DATE"));
  }
  
  /**
   * Remove properties for given entities from this location
   */
  public void removeEntities(Set entities) {
    for (ListIterator it = properties.listIterator(); it.hasNext(); ) {
      Property prop = (Property)it.next();
      if (entities.contains(prop.getEntity()))
        it.remove();
    }
  }
  
  /**
   * Remove properties for given entity
   */
  public void removeEntity(Entity entity) {
    for (ListIterator it = properties.listIterator(); it.hasNext(); ) {
      Property prop = (Property)it.next();
      if (entity == prop.getEntity())
        it.remove();
    }
  }
  
  /**
   * How many matches this location had
   */
  public int getMatches() {
    return matches;
  }
  
  /**
   * Validity test
   */
  public boolean isValid() {
    return matches>0 && !Double.isNaN(coordinate.x) && !Double.isNaN(coordinate.y);
  }
  
  /**
   * Gedcom this location is for
   */
  public Gedcom getGedcom() {
    return ((Property)properties.get(0)).getGedcom();
  }

  /**
   * identify is defined as city, state and country
   */
  public int hashCode() {
    if (hash==0) {
      // calculate hash code now
      if (city!=null) hash += city.toLowerCase().hashCode();
      for (int i=0;i<jurisdictions.size();i++)
          hash += jurisdictions.get(i).toString().toLowerCase().hashCode();
      if (country!=null) hash += country.getCode().toLowerCase().hashCode();
    }
    return hash;
  }

  /**
   * identify is defined as city, state and country
   */
  public boolean equals(Object obj) {
    GeoLocation that = (GeoLocation)obj;
    return equals(this.city, that.city) && this.jurisdictions.equals(that.jurisdictions) && equals(this.country, that.country);
  }
  
  private static boolean equals(Object o1, Object o2) {
    if (o1==null&&o2==null)
      return true;
    if (o1==null||o2==null)
      return false;
    return o1.equals(o2);
  }

  /**
   * City (never null)
   */
  public String getCity() {
    return city;
  }

  /**
   * Identified Top Level Jurisdictions
   */
  public List getJurisdictions() {
    return jurisdictions;
  }
  
  GeoLocation addJurisdiction(String j) {
    jurisdictions.add(j);
    return this;
  }

  /**
   * Country or null
   */
  public Country  getCountry() {
    return country;
  }
  
  /**
   * Contained properties
   */
  public int getNumProperties() {
    return properties.size();
  }
  
  public Property getProperty(int i) {
    return (Property)properties.get(i);
  }
  
  public int getPropertyIndex(Property prop) {
    return properties.indexOf(prop);
  }
  
  /**
   * Set location lat,lon
   */
  protected void setCoordinate(Coordinate coord) {
    setCoordinate(coord.y, coord.x);
  }
  
  /**
   * Set location lat,lon
   */
  protected void setCoordinate(double lat, double lon) {
    coordinate.x = lon;
    coordinate.y = lat;
    matches = 1;
  }
  
  /**
   * Set # of matches
   */
  protected void setMatches(int set) {
    matches = set;
  }
  
  /**
   * String representation
   */
  public String toString() {
    return getJurisdictionsAsString();
  }
  
  /**
   * Feature - set attributes
   */
  public void setAttributes(Object[] arg0) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - set schema
   */
  public void setSchema(FeatureSchema arg0) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - id
   */
  public int getID() {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - attribute
   */
  public void setAttribute(int arg0, Object arg1) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - attribute
   */
  public void setAttribute(String arg0, Object arg1) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - geometry
   */
  public void setGeometry(Geometry arg0) {
    throw new IllegalArgumentException();
  }
  
  /**
   * Feature - attribute
   */
  public Object getAttribute(int arg0) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - attribute by name - we always return city
   */
  public Object getAttribute(String arg0) {
    return city;
  }

  /**
   * Feature - attribute by index
   */
  public String getString(int arg0) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - attribute by index
   */
  public int getInteger(int arg0) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - attribute by index
   */
  public double getDouble(int arg0) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - attribute by name
   */
  public String getString(String arg0) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - geometry
   */
  public Geometry getGeometry() {
    return this;
  }

  /**
   * Feature - schema
   */
  public FeatureSchema getSchema() {
    return SCHEMA;
  }

  /**
   * Feature - clonig
   */
  public Object clone() {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - cloing
   */
  public Feature clone(boolean arg0) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - attributes
   */
  public Object[] getAttributes() {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - comparison
   */
  public int compareTo(Object o) {
    GeoLocation that = (GeoLocation)o;
    return this.city.compareToIgnoreCase(that.city);
  }
  
}
