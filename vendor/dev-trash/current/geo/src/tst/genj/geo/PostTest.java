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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test posting to our server side query script
 */
public class PostTest {

  /**
   * our main
   */
  public static void main(String[] args) {
    try {
      new PostTest().testWebservice();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
  
  /**
   * posting
   */
  public void testWebservice() throws Throwable {
    
    // we don't need log output for this
    Logger.getLogger("").setLevel(Level.OFF);

    GeoLocation[]  locs = {
      new GeoLocation("Lohmar", null, null),
      new GeoLocation("Siegburg", "Nordrhein-Westfalen", null),
      new GeoLocation("Siegburg", "Rhein-Sieg-Kreis", null).addJurisdiction("Nordrhein-Westfalen"),
      new GeoLocation("Köln", null, Country.get("de")),
      new GeoLocation("Rendsburg", null, null),
      new GeoLocation("Celle", null, null),
      new GeoLocation("Celle", "Niedersachsen", Country.get("de")),
      new GeoLocation("Hambu*", null, null)
    };
    
    GeoService service = GeoService.getInstance();
    
    int i=0;
    for (Iterator rows = service.webservice(GeoService.URL, Arrays.asList(locs), true).iterator(); rows.hasNext(); i++) {
      System.out.println("---"+locs[i]+"---");
      for (Iterator hits = ((Collection)rows.next()).iterator(); hits.hasNext(); )
        System.out.println(hits.next());
    }

    // done
  }

}
