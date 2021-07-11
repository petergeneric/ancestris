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
package genj.almanac;

import genj.gedcom.GedcomException;
import genj.gedcom.time.PointInTime;
import genj.util.WordBuffer;
import java.util.List;

/**
 * A CDay event
 */
public class Event implements Comparable<Event> {
  
  private final PointInTime pit;
  private final String desc;
  private final String almanac;
  private final List<String> cats;
  private final int sigLevel;
  private final long julianDay;
  
  /** constructor */
  public Event(String setAlmanac, List<String> setCats, int setSigLevel, PointInTime setTime, String setText) throws GedcomException {
    pit = setTime;
    almanac = setAlmanac;
    cats = setCats;
    sigLevel = setSigLevel;
    desc = setText;
    // make sure its julian day is good
    julianDay = pit.getJulianDay();
  }
  
  /** the julian day */
  protected long getJulian() {
    return julianDay;
  }
  
  /* Test for country */
  protected boolean isAlmanac(List<String> criteria) {
    return criteria.contains(almanac);
  }
  
  /* Test for category */
  protected boolean isCategory(List<String> criteria) {
    for (int c=0; c<cats.size(); c++) {
      if (criteria.contains(cats.get(c)))
        return true;
    }
    return false;
  }
  
  /* Test for level */
  protected boolean isLevel(int criteria) {
    return criteria >= sigLevel;
  }
  
  /** to String */
  @Override
  public String toString() {
    WordBuffer result = new WordBuffer();
    result.append(pit.toString());
    result.append(desc);
    return result.toString();
  }
  
  /** comparison */
  @Override
  public int compareTo(Event that) {
    return this.pit.compareTo(that.pit);
  }
  
  /**
   * Accessor
   */
  public PointInTime getTime() {
    return pit;
  }
  
  /**
   * Accessor
   */
  public String getAlmanac() {
    return almanac;
  }
  
  /**
   * Accessor
   */
  public List<String> getCategories() {
    return cats;
  }
  
  /**
   * Accessor
   */
  public int getSigLevel() {
    return sigLevel;
  }
  
  /**
   * Accessor
   */
  public String getDescription() {
    return desc;
  }
  
} //Event