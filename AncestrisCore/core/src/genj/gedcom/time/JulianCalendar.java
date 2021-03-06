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
package genj.gedcom.time;



/**
 * Our own julian
 */
public class JulianCalendar extends GregorianCalendar {

  /**
   * Constructor
   */
  protected JulianCalendar() {
    super("@#DJULIAN@", "julian", "images/Julian");
  }
  
  /**
   * @see genj.gedcom.PointInTime.GregorianCalendar#isLeap(int)
   */
  protected boolean isLeap(int year) {
    return (year%4 == 0);
  }
  
  /**
   * @see genj.gedcom.PointInTime.GregorianCalendar#toJulianDay(int, int, int)
   */
  protected int toJulianDay(int day, int month, int year) {

    // there's no year 0 - anything B.C. has to be shifted
    if (year<0)
      year++;

    // switched to algorithm under
    // https://en.wikipedia.org/wiki/Julian_day#Converting_Julian_or_Gregorian_calendar_date_to_Julian_day_number
    // to address issue with diff between simple 29/Feb - 1/Mar calculations as spotted by alwa83
    
    int a = (14 - (month+1) )/12;
    int y = year + 4800 - a;
    int m = (month+1) + 12 * a - 3; 
    
    return (day+1) + (153 * m +2)/5 + (365 * y) + (y/4) - 32083 ;

  }
  
  /**
   * @see genj.gedcom.PointInTime.GregorianCalendar#getPointInTime(int)
   */
  protected PointInTime toPointInTime(int julianDay) {

    // see toJulianDay
    
    int
      Z = julianDay,
      B = Z+1524,
      C = (int)((B-122.1)/365.25),
      D = (int)(365.25*C),
      E = (int)((B-D)/30.6001),
      F = (int)(30.6001*E),
      d = B-D-F,
      m = E-1 <= 12 ? E-1 : E-13,
      y = C-(m<3?4715:4716);  
    
    return new PointInTime(d-1,m-1,y<=0?y-1:y,this);
  }

} //JulianCalendar