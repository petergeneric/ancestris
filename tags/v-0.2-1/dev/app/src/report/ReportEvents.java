/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.time.PointInTime;
import genj.report.Report;
import genj.util.WordBuffer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * GenJ - Report
 * @author Nils Meier nils@meiers.net
 * @author Carsten Muessig carsten.muessig@gmx.net
 */
public class ReportEvents extends Report {
  
    /** whether we sort by day-of-month or date */
    public boolean isSortDay = true;
    /** whether births should be reported */
    public boolean reportBirth = true;
    /** whether baptisms should be reported */
    public boolean reportBaptism = true;
    /** whether marriages should be reported */
    public boolean reportMarriage = true;
    /** whether divorces should be reported */
    public boolean reportDivorce = true;
    /** whether emigration should be reported */
    public boolean reportEmigration = true;
    /** whether immigration should be reported */
    public boolean reportImmigration = true;
    /** whether naturalization should be reported */
    public boolean reportNaturalization = true;
    /** whether deaths should be reported */
    public boolean reportDeath = true;
    /** whether the output should be in icalendar format */
    public boolean isOutputICal = false;

    /** wether dead persons' events should be filtered out */
    public boolean isNoDead = false;
    
    /** filter place */
    public String place = "";
    
    /** filter gender */
    public int sex = 3;
    public String[] sexs = {PropertySex.TXT_MALE, PropertySex.TXT_FEMALE, PropertySex.TXT_UNKNOWN, ""};

    /** day of the date limit */
    public String day = "";

    /** month of the date limit */
    public String month = "";

    /** year of the date limit */
    public String year = "";

    /** the marriage symbol */
    private final static String TXT_MARR_SYMBOL = genj.gedcom.Options.getInstance().getTxtMarriageSymbol();
    
    /** time reporting variables */
    private String timestamp;
    private final static SimpleDateFormat 
      formatDTSTAMP = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'"),
      formatDTSTART = new SimpleDateFormat("yyyyMMdd");


    /**
     * Main for argument Gedcom
     */
    public void start(Gedcom gedcom) throws GedcomException {

        // check that something is selected
        if ((!reportBirth) && (!reportBaptism) && (!reportDeath) && (!reportMarriage) && (!reportDivorce) && (!reportEmigration) && (!reportImmigration) && (!reportNaturalization))
            return;
        
        // initialize timestamp
        timestamp = toISO(System.currentTimeMillis(), true);
        formatDTSTART.setTimeZone(TimeZone.getTimeZone("GMT")); // make sure all event timestamps are considered to be GMT
        formatDTSTAMP.setTimeZone(TimeZone.getDefault()); // apply local timezone to dtstamp formatting (it's considered Z)

        // collect evens for all individuals/families
        Map<String, List<Hit>> tag2events = new HashMap<String, List<Hit>>();
        if (reportBirth) tag2events.put("BIRT", new ArrayList<Hit>());
        if (reportBaptism) tag2events.put("BAPM|BAPL|CHR|CHRA",  new ArrayList<Hit>());
        if (reportMarriage) tag2events.put("MARR", new ArrayList<Hit>());
        if (reportDivorce) tag2events.put("DIV", new ArrayList<Hit>());
        if (reportEmigration) tag2events.put("EMI", new ArrayList<Hit>());
        if (reportImmigration) tag2events.put("IMMI", new ArrayList<Hit>());
        if (reportNaturalization) tag2events.put("NATU", new ArrayList<Hit>());
        if (reportDeath) tag2events.put("DEAT", new ArrayList<Hit>());

        // loop individuals
        for (Entity indi : (Collection<Entity>)gedcom.getEntities(Gedcom.INDI)) {
            analyze((Indi)indi, tag2events);
        }
        
        // output header
        if (isOutputICal) {
          println("BEGIN:VCALENDAR");
          println("PRODID:-//Genealogy//ReportEvents/EN");
          println("VERSION:2.0");
          println("METHOD:PUBLISH");
        } else {
          if (isNoDead)
            println(translate("isNoDead"));
          if (place.length()>0)
            println(translate("place") + " = " + place);
          if (sex!=3)
            println(translate("sex") + " = " + sexs[sex]);
          if (day.length()>0)
            println(translate("day") + " = " + day);
          if (month.length()>0)
            println(translate("month") + " = " + month);
          if (year.length()>0)
            println(translate("year")+ " = " + year);
          println();
        }
        
        // ... output events type by type
        for (String tag : tag2events.keySet()) {
          List<Hit> events = tag2events.get(tag);
          if (!events.isEmpty()) {
            
            Collections.sort(events);
            
            if (!isOutputICal) 
              println(getIndent(2) + Gedcom.getName(new StringTokenizer(tag, "|").nextToken()));

            for (Hit event : events) 
              println(event);
            
            if (!isOutputICal) 
              println();
            
            events.clear();
          }
        }
        
        // output footer
        if (isOutputICal) {
          println("END:VCALENDAR");
        }

        // done
    }

    /**
     * Analyze one individual
     */
    private void analyze(Indi indi, Map<String,List<Hit>> tag2events) {

        // filter dead?
        if (isNoDead && indi.getDeathDate() != null && indi.getDeathDate().isValid())
            return;

        if(checkSex(indi)==false)
            return;

        // look for events
        analyzeEvents(indi, tag2events);
        
        // check all marriages
        Fam[] fams = indi.getFamiliesWhereSpouse();
        for (int f = 0; f < fams.length; f++) {
          Fam fam = fams[f];
          analyzeEvents(fam, tag2events);
        }

        // done
    }
    
    private void analyzeEvents(Entity entity, Map<String, List<Hit>> tag2events) {
      
      for (String tag : tag2events.keySet()) {
        List<Hit> events = tag2events.get(tag);
        
        Property[] props = getProperties(entity, tag);
        for (int i = 0; i < props.length; i++) {
          Property event = props[i];
          if (event instanceof PropertyEvent) {
            Property date = event.getProperty("DATE");
            if (date instanceof PropertyDate) {
              Hit hit = new Hit(entity, (PropertyEvent)event, i, (PropertyDate)date);
              
              // already known?
              if (events.contains(hit))
                continue;
              // bad date?
              if (!checkDate((PropertyDate)date))
                continue;
              // bad place?
              if (!checkPlace(event))
                continue;
              // got it
              events.add(hit); 
            }
          }
        }
      }
      
    }
    
    private Property[] getProperties(Entity entity, String tag) {
      ArrayList<Property> result = new ArrayList<Property>();
      for (int i=0, j = entity.getNoOfProperties(); i<j ; i++) {
        Property prop = entity.getProperty(i);
        if (prop.getTag().matches(tag))
          result.add(prop);
      }
      return Property.toArray(result);
    }
    
    /**
     * checks if the place of an event matches the user's choice
     */
    private boolean checkPlace(Property event) {
      
      // no filter = always matching
      if (place.length()==0)
        return true;
      
      // need place
      PropertyPlace pp = (PropertyPlace)event.getProperty("PLAC");
      if (pp==null)
        return false;
      
      // need matching value
      return pp.getValue().matches("(?i).*"+place+".*");
    }

    /** 
     * checks if the sex of an indi matches the user choice
     * @param indi to check
     * @return boolean the result
     */
    private boolean checkSex(Indi indi) {

        if(sex==3)
            return true;

        switch(indi.getSex()) {

            case PropertySex.MALE:
                if(sex!=0)
                    return false;
                else
                    return true;

            case PropertySex.FEMALE:
                if(sex!=1)
                    return false;
                else
                    return true;

            case PropertySex.UNKNOWN:
                if(sex!=2)
                    return false;
                else
                    return true;

            default: return false;
        }
    }

    /**
     * checks if the date of an event matches the given values of the user (fix, max, min, ...)
     * @return boolean to indicate the result
     */
    private boolean checkDate(PropertyDate date) {

        // has to be valid
        if (date==null||!date.isValid())
            return false;

        PointInTime start = date.getStart();
        if (start.getCalendar()!=PointInTime.GREGORIAN)
            return false;
        
        if (checkValue(start.getDay()+1, day) && checkValue(start.getMonth()+1, month) && checkValue(start.getYear(), year))
          return true;

        return false; 
    }
    
    private boolean checkValue(int value, String filter) {
      // no filter - matched!
      if (filter.length()==0)
        return true;
      // parse filter
      try {
        // filter '>'
        if (filter.startsWith(">"))
          return Integer.parseInt(filter.substring(1)) < value;
        // filter '<'
        if (filter.charAt(0)=='<')
          return Integer.parseInt(filter.substring(1)) > value;
        // filter '='
        if (filter.charAt(0)=='=')
          return Integer.parseInt(filter.substring(1)) == value;
        // filter '-'
        int range = filter.indexOf("-");
        if (range>0) {
          return Integer.parseInt(filter.substring(0,range)) <= value
            && Integer.parseInt(filter.substring(range+1)) >= value;
        }
        return Integer.parseInt(filter) == value;
      } catch (NumberFormatException e) {
        return false;
      }
    }
    
    private String toISO(long timeMillis, boolean time) {
      Date date = new Date(timeMillis);
      return time ? formatDTSTAMP.format(date) : formatDTSTART.format(date);
    }        

    /**
     * Wrapping an Event hit
     */
    private class Hit implements Comparable<Hit> {
      
      Entity who;
      int num;
      Property event;
      PropertyDate date;
      PointInTime compare;
        
      /** Constructor*/
      Hit(Entity entity, PropertyEvent event, int num, PropertyDate date) {
        this.who = entity;
        this.event = event;
        this.num = num;
        this.date = date;
        PointInTime when = date.getStart();
        if (isSortDay)
          // blocking out year (to a Gregorian LEAP 4 - don't want to make it invalid) so that month and day count
          compare = new PointInTime(when.getDay(), when.getMonth(), 4, when.getCalendar());
        else
          compare = when;
      }
      // comparison
      public int compareTo(Hit other) {
          return compare.compareTo(other.compare);
      }
      // equals
      public boolean equals(Object that) {
        return event==((Hit)that).event;
      }
      // toString
      public String toString() {
        try {
          if (isOutputICal) {
            WordBuffer result = new WordBuffer("\n");
            result.append("BEGIN:VEVENT");
            result.append("DTSTART:"+toISO(date.getStart().getTimeMillis(), false));
            result.append("UID:"+who.getGedcom().getName()+"|"+who.getId()+"|"+event.getTag()+"|"+num);
            result.append("DTSTAMP:"+timestamp);
            result.append("SUMMARY:"+Gedcom.getName(event.getTag())+" "+icalescape(who.toString()));
            Property where  = event.getProperty("PLAC");
            if (where!=null)
              result.append("LOCATION:"+icalescape(where.getDisplayValue()));
            if (event.getTag().equals("BIRT"))
              result.append("RRULE:FREQ=YEARLY");
            result.append("DESCRIPTION:");
            result.append("END:VEVENT");
            return result.toString();
          } else {
            StringBuffer result = new StringBuffer();
            result.append(getIndent(3));
            result.append(date);
            result.append(" ");
            result.append(who);
            return result.toString();
          }
        } catch (Throwable t) {
          throw new RuntimeException();
        }
      }
      private String icalescape(String string) {
        return string.replaceAll(",", "\\\\,");
      }
    } //Hit

} //ReportEvents
