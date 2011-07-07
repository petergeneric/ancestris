/**
 * Class:  LiturgicalYear
 * Author: Lars Uffmann, Koeln
 * Version: (beta) of 2009-11-30
 * Purpose: calculate dates of church holidays in the Gregorian (Julian) Calendar
 * License: Free software under the terms of the GNU General Public Licence
 *          as published by the Free Software Foundation.
 */
package genj.util;

import genj.util.swing.ImageIcon;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Logic around a Christian Liturgical year
 */
public class LiturgicalYear {
  
  private final static Resources RES = Resources.get(LiturgicalYear.class);
  
  public final static ImageIcon IMAGE = new ImageIcon(LiturgicalYear.class, "LiturgicalYear.png");
  
  public final static String
    TXT_LITURGICAL_YEAR = RES.getString("liturgicalyear"),
    TXT_SUNDAY = RES.getString("liturgicalyear.sunday");

	public enum Sunday {
	  
    /* Epiphanias = 6th of January, so 1 post Epiphanias can be 7th-13th of January */
		PostEpiphanias(Integer.MAX_VALUE, 5, "post Ephiphanias") { 
		  
		  // not relative to easter
		  @Override
		  protected Calendar getDateImpl(int year, int postEventWeeks) {
	      int K;  // secular number
	      int S;  // secular sun period(? german: saekulare Sonnenschaltung)
	      int SZ; // first sunday in march
	      int dayOfYear;

	      // do all math for the previous year to get around the leap date 29th february
	      K = (year - 1) / 100;
	      S = 2 - (3*K + 3) / 4;
	      SZ = 7 - ((year - 1) + (year - 1)/4 + S) % 7; // first sunday in previous march

	      /* first possible post Epiphanias is  7th of january 
	       * from 1st of march to  7th of january  it is 31+30+31+30+31+31+30+31+30+31+6 = 312 days,
	       * So the difference between SZ and  7th of january  is 313-SZ days, because
	       * SZ starts counting with 1 on the first of march. Difference in days modulo 7 equals
	       * the difference in days of the week. If that is zero, then 1 post Epiphanias is on 7th of January,
	       * if it is 1 ( 7th of january  is a monday), then post Epiphanias is 6 days later
	       * difference in days of the week -> days to add to  7th january : (7 - d) % 7 + (count-1) * 7
	       * ==> dayOfYear = 7 + (7 - (313-SZ) % 7) % 7 + (count-1) * 7
	       */
	      dayOfYear = 7 + (7 - (313 - SZ) % 7) % 7 + (postEventWeeks-1) * 7; // add 7 days for each additional count
	      
        Calendar result = Calendar.getInstance();
	      result.set(Calendar.YEAR, year);
	      result.set(Calendar.DAY_OF_YEAR, dayOfYear);
	      return result;
		  }
		},
		
		Septuagesimae(-9, "Septuagesimae", "Circumdederunt"),
		Sexagesimae(-8, "Sexagesimae", "Exsurge"),
		Quinquagesimae(-7, "Qinquagesimae", "Estomihi"),
		Quadragesimae(-6, "Quadragesimae", "Invocavit"),
		Reminiscere(-5, "Reminiscere"),
		Oculi(-4, "Oculi"),
		Letare(-3, "Letare", "Laetare"),
		Judica(-2, "Judica"),
		Palmarum(-1, "Palmarum"),
		Paschale(0, "Paschale"),
		Quasimodogeniti(1, "Quasimodogeniti"),
		MisericordiasDomini(2, "Misericordias Domini"),
		Jubilate(3, "Jubilate"),
		Cantate(4, "Cantate"),
		Rogate(5, "Rogate"),
		Exaudi(6, "Exaudi"),
		Pentecost(7, "Pentecost"),
		Trinitatis(8, "Trinitatis"),
		PostTrinitatis(8, 27, "post Trinitatis"),
		
    /* 4 Adventis = last sunday before christmas (25. december), so 1 Adventis can be 27th november - 3rd december */
		Adventis(Integer.MAX_VALUE, 4, "Adventis") {
		  
      // not relative to easter
      @Override
		  protected Calendar getDateImpl(int year, int week) {
		      int K;  // secular number
		      int S;  // secular sun period(? german: saekulare Sonnenschaltung)
		      int SZ; // first sunday in march
		      int dayOfNovember; // adventis day counting 1 = 1st of november

		      // get first sunday in march
		      K = year / 100;
		      S = 2 - (3*K + 3) / 4;
		      SZ = 7 - (year + year/4 + S) % 7;

		      /* first possible Adventis        is 27th of november (when christmas eve is a saturday) 
		       * from 1st of march to 27th of november it is 31+30+31+30+31+31+30+31+26      = 271 days,
		       * So the difference between SZ and 26th of november is 272-SZ days, because
		       * SZ starts counting with 1 on the first of march. Difference in days modulo 7 equals
		       * the difference in days of the week. If that is zero, then 1 Adventis is on 27th of November   
		       * if it is 1 (27th of november is a monday), then 1 Adventis is 6 days later
		       * difference in days of the week -> days to add to 27th november: (7 - d) % 7 + (count-1) * 7
		       * ==> dayOfNovember = 27 + (7 - (272-SZ) % 7) % 7 + (count-1) * 7
		       */
		      dayOfNovember = 27 + (7 - (272-SZ) % 7) % 7 + (week-1) * 7;
		      
		      Calendar result = Calendar.getInstance();
		      result.set (Calendar.YEAR, year);
		      if (dayOfNovember > 30) {
		        result.set (Calendar.MONTH, 11);
		        result.set (Calendar.DAY_OF_MONTH, dayOfNovember - 30);
		      }
		      else {
		        result.set (Calendar.MONTH, 10);
		        result.set(Calendar.DAY_OF_MONTH, dayOfNovember);
		      }
		      
		      return result;
		  }
		};

    private int easterOffset;
		private int weeks;
		private List<String> names;
		
		private Sunday (int easterOffset, String... names) {
		  this.easterOffset = easterOffset;
			this.weeks = 0; 
			this.names = Collections.unmodifiableList(Arrays.asList(names));
		}

		private Sunday (int easterOffset, int weeks, String... names) {
      this.easterOffset = easterOffset;
			this.weeks = weeks;
			this.names = Collections.unmodifiableList(Arrays.asList(names));
		}

		/** the number of weeks this sunday is counted - either 0 for just one - or n for 1st to nth */
		public int getWeeks() {
			return weeks;
		}

    public String getName() {
      return names.get(0);
    }
    
		public List<String> getNames() {
			return names;
		}
		
		public Calendar getDate(int year, int relativeWeek) {
		  if (weeks>0&&relativeWeek<=0)
		    throw new IllegalArgumentException("bad relative week "+relativeWeek);
      if (relativeWeek>weeks)
        throw new IllegalArgumentException("bad relative week "+relativeWeek);
      return getDateImpl(year, relativeWeek);
		}
		
		protected Calendar getDateImpl(int year, int relativeWeek) {

		  // default - calculate easter
		  Calendar result = getEaster(year);

		  // add week
      result.add(Calendar.DAY_OF_YEAR, (easterOffset+relativeWeek)*7);

      // done
		  return result;
		}

		/** set a calendar result to easter+week in given year */
		public static Calendar getEaster(int year) {
		  
	    int K;  // secular number
	    int M;  // secular moon period(? german: saekulare Mondschaltung)
	    int S;  // secular sun period(? german: saekulare Sonnenschaltung)
	    int A;  // moon parameter
	    int D;  // seed of first full moon in spring (german: Keim ...)
	    int R;  // Calendaric correction
	    int OG; // easter limit
	    int SZ; // first sunday in march
	    int OE; // days between easter sunday and the easter limit
	    int OS; // date of easter sunday as day of march (32 = 1st of April, etc.)

	    K = year / 100;
	    M = 15 + (3*K + 3) / 4 - (8*K + 13) / 25;
	    S = 2 - (3*K + 3) / 4;
	    A = year % 19;
	    D = (19*A + M) % 30;
	    R = D/29 + (D/28 - D/29) * (A/11);
	    OG = 21 + D - R;
	    SZ = 7 - (year + year/4 + S) % 7;
	    OE = 7 - (OG - SZ) % 7;
	    OS = OG + OE;

	    Calendar result = Calendar.getInstance();
	    result.set (Calendar.YEAR, year);
	    if (OS > 31) {
	      result.set(Calendar.MONTH, 3);
	      result.set(Calendar.DAY_OF_MONTH, OS - 31);
	    }
	    else {
	      result.set(Calendar.MONTH, 2);
	      result.set(Calendar.DAY_OF_MONTH, OS);
	    }
	    return result;
	  }
		
	};

	/** demo method */
  public static void main(String[] args) {
    
    int year = 2009;
    int julianDay;
    
    Calendar date;

    for (LiturgicalYear.Sunday sunday: LiturgicalYear.Sunday.values()) {
      int i;
      if (sunday.getWeeks() > 0) i = 1;
      else i = 0;
      do {
        date = sunday.getDate(year, i);
        if (i > 0)
          System.out.format ("Julian Day for %d %s: %tD\n", i, sunday.getName(), date);
        else
          System.out.format ("Julian Day for %s: %tD\n", sunday.getName(), date);
        i++;
      } while (i <= sunday.getWeeks());
      }

  
    for (int i = 1982; i <= 2022; i+=1) {
      Calendar easterDate = Sunday.getEaster(i);
      System.out.format ("Easter Sunday in %d: %tD\n", i, easterDate);
    }
  
  }
}
