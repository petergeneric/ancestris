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

import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.util.Origin;

import java.util.Locale;

import junit.framework.TestCase;

/**
 * Test Geo Location functionality
 */
public class GeoLocationTest extends TestCase {

  private Indi indi;
  
  /**
   * Prepare a fake indi
   */
  protected void setUp() throws Exception {

    // create gedcom
    Gedcom gedcom = new Gedcom(Origin.create("file://foo.ged"));
    
    // and individual
    indi = (Indi)gedcom.createEntity(Gedcom.INDI);
    
  }
  
  /**
   * location parsing
   */
  public void testParsing() {

    Locale.setDefault(Locale.GERMAN);
    
    // these should all be the same
    GeoLocation[] locations = {
      locate(place("Timaru, Neuseeland")),
      locate(place("Timaru, New Zealand")),
      locate(addr("Timaru", "New Zealand")),
      locate(addr("Timaru", "Neuseeland")),
    };
    
    for (int l = 1; l < locations.length; l++) {
      assertEquals(locations[l-1], locations[l]);
    }
    
    // but they shouldn't be the same as this one
    GeoLocation other = locate(place("Timaru"));
    for (int l = 0; l < locations.length; l++) {
      // interesting - use Object cast otherwise GeoLocation's super.equals() is called
      assertFalse(other.equals((Object)locations[l])); 
    }
    
    // US state shouldn't be taken as countries (e.g. IL for Illinois vs. Israel)
    assertNull("don't consider iso country codes", locate(place("Washington, IL")).getCountry());
    assertEquals("consider country names", "il", locate(place("Washington, Israel")).getCountry().getCode());
    

    // done
  }
  
  private GeoLocation locate(Property prop) {
    return new GeoLocation(prop);
  }

  private Property addr(String city, String country) {
    
    Property event = indi.addProperty("EVEN", "");
    
    Property addr = event.addProperty("ADDR", "");
    addr.addProperty("CITY", city);
    addr.addProperty("CTRY", country);
    
    return event;
  }
    
  private Property place(String value) {
    Property event = indi.addProperty("EVEN", "");
    event.addProperty("PLAC", value);
    return event;
  }

}
