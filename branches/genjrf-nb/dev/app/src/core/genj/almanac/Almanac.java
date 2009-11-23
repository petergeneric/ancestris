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
import genj.util.EnvironmentChecker;
import genj.util.Resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A global Almanac for all kinds of historic events
 */
public class Almanac {
  
  private final static Logger LOG = Logger.getLogger("genj.almanac");
  
  private final static Resources RESOURCES = Resources.get(Almanac.class);
  
  /** language we use for events */
  private final static String LANG = Locale.getDefault().getLanguage();
  
  /** listeners */
  private List<ChangeListener> listeners = new ArrayList<ChangeListener>(10);
  
  /** singleton */
  private static Almanac instance;
  
  /** events */
  private List<Event> events = new ArrayList<Event>();
  
  /** categories */
  private Set<String> categories = new HashSet<String>();
  
  /** whether we've loaded all events */
  private boolean isLoaded = false;
  
  /** 
   * Singleton Accessor 
   */
  public static Almanac getInstance() {
    if (instance==null)
      instance = new Almanac();
    return instance;
  }
  
  /**
   * Constructor
   */
  private Almanac() {
    // load what we can find async
    new Thread(new Runnable() {
      public void run() {
        try {
          if ("fr".equals(Locale.getDefault().getLanguage()))
            new AlmanacLoader().load();
          else
            new WikipediaLoader().load();
        } catch (Throwable t) {
        }
  	    LOG.info("Loaded "+events.size()+" events");
  	    synchronized (events) {
  	      isLoaded = true;
  	      events.notifyAll();
  	    }
      }
    }).start();
    // done for now
  }
  
  /**
   * Wait for events to be loaded (this blocks)
   */
  public boolean waitLoaded() {
    synchronized (events) {
      while (!isLoaded) try {
        events.wait();
      } catch (InterruptedException e) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * Add a change listener
   */
  public void addChangeListener(ChangeListener l) {
    listeners.add(l);
  }
  
  /**
   * Remove a change listener
   */
  public void removeChangeListener(ChangeListener l) {
    listeners.remove(l);
  }
  
  /**
   * Registers another category
   */
  protected String addCategory(String name) {
    
    synchronized (categories) {
      categories.add(name);
    }
    
    return name;
  }
  
  /**
   * Update listeners
   */
  protected void fireStateChanged() {
    ChangeEvent e = new ChangeEvent(this);
    ChangeListener[] ls = (ChangeListener[])listeners.toArray(new ChangeListener[listeners.size()]);
    for (int l = 0; l < ls.length; l++) {
      ls[l].stateChanged(e);
    }
  }
  
  /**
   * Accessor - categories
   */
  public List getCategories() {
    synchronized (categories) {
      return new ArrayList(categories);
    }
  }
  
  /**
   * Accessor - events by point in time
   */
  public Iterator getEvents(PointInTime when, int days, Set cats) throws GedcomException {
    return new Range(when, days, cats);
  }
  
  /**
   * Accessor - a range of events by (gregorian) year
   */
  public Iterator<Event> getEvents(PointInTime from, PointInTime to, Set<String> cats) {
    return new Range(from, to, cats);
  }
  
  /**
   * A loader for almanac files
   */  
  private abstract class Loader implements FilenameFilter {
    
		/**
		 * async load
		 */
		protected void load() {
		  
		  // look into dir
		  File[] files;
		  
		  File dir = getDirectory();
		  if (!dir.exists()||!dir.isDirectory())
		    files = new File[0];
		  else
		    files = dir.listFiles();
		  
		  if (files.length==0) {
		    LOG.info("Found no file(s) in "+dir.getAbsoluteFile());
		    return;
		  }
		  
		  // load each one
		  for (int f = 0; f < files.length; f++) {
		    File file = files[f];
		    if (accept(dir, file.getName())) {
    	    LOG.info("Loading "+file.getAbsoluteFile());
    	    try {
	          load(file);
	        } catch (IOException e) {
	    	    LOG.log(Level.WARNING, "IO Problem reading "+file.getAbsoluteFile(), e);
	        }
		    }
      }
		  
		  // done
		}    
		
		/**
		 * load one file
		 */
		protected void load(File file) throws IOException {
		  
		  // read its lines
		  BufferedReader in = open(file);
		  for (String line = in.readLine(); line!=null; line = in.readLine())  {
		    
		    try {
			    Event event = load(line);
			    if (event!=null) {
			      // Search for correct index
			      int index = Collections.binarySearch(events, event);
			      if (index < 0) 
			        index = -index-1;
					  // keep
			      synchronized (events) {
			        events.add(index, event);
			      }
			    }
		    } catch (Throwable t) {
		    }
		  
		    // next
		  }
		    
		  // notify about changes
      fireStateChanged(); 
		  
		  // done
		}
    
    /**
     * get buffered reader from file
     */
    protected abstract BufferedReader open(File file) throws IOException;
    
		/**
		 * load one line and create an Event (or null)
		 */
		protected abstract Event load(String line) throws GedcomException ;
		
		/**
		 * resolve directory to look for files in
		 */
    protected abstract File getDirectory();
    
  } //Loader
 
  /**
	 * This class adds support for the ALMANAC style event repository
	 * (our own invention)
	 */
  private class AlmanacLoader extends Loader {
    /** only .almanac */
    public boolean accept(File dir, String name) {
      return name.toLowerCase().endsWith(".almanac");
    }
    /** look into ./contrib/almanac */
    protected File getDirectory() {
      return new File(EnvironmentChecker.getProperty(this,
          new String[]{ "genj.almanac.dir", "user.dir/contrib/almanac"},
          "contrib/almanac",
          "find almanac files"
        ));
    }
    /**
     * get buffered reader from file
     */
    protected BufferedReader open(File file) throws IOException {
      return new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
    }
    /** create an event */
    protected Event load(String line) throws GedcomException {
      // comment?
      if (line.startsWith("#"))
        return null;
      // break it by ';'
      StringTokenizer cols = new StringTokenizer(line, ";", true);
      // #1 date YYYYMMDD
      String date = cols.nextToken().trim();cols.nextToken();
      if (date.length()<4)
        return null;
      int year  = Integer.parseInt(date.substring(0, 4));
      int month = date.length()>=6 ? Integer.parseInt(date.substring(4, 6))-1 : PointInTime.UNKNOWN;
      int day   = date.length()>=8 ? Integer.parseInt(date.substring(6, 8))-1 : PointInTime.UNKNOWN;
      PointInTime time = new PointInTime(day, month, year);
      if (!time.isValid())
        return null;
      // #2 date
      String date2 = cols.nextToken();
      if (!date2.equals(";")) {
        cols.nextToken();
      }
      // #3 country
      String country = cols.nextToken().trim();
      if (!country.equals(";")) {
        cols.nextToken();
      }
      // #4 significance
      int sig = Integer.parseInt(cols.nextToken());
      cols.nextToken();
      // #5 type
      List cats = getCategories(cols.nextToken().trim());
      if (cats.isEmpty())
        return null;
      cols.nextToken();
      // #6 and following description
      String desc = null;
      while (cols.hasMoreTokens()) {
        String translation = cols.nextToken().trim();
        if (translation.equals(";"))
          continue;
        int i = translation.indexOf('=');
        if (i<0)
          continue;
        String lang = translation.substring(0,i);
        if (desc==null||LANG.equals(lang)) 
          desc = translation.substring(i+1);
      }
      // got a description?
      if (desc==null)
        return null;
      // done
      return new Event(cats, time, desc);
    }
    /** derive category names for key */
    private List getCategories(String cats) {
      
      List result = new ArrayList();
      for (int c=0;c<cats.length();c++) {
        String key = cats.substring(c,c+1);
        String cat = RESOURCES.getString("category."+key, false);
        if (cat==null)
          cat = RESOURCES.getString("category.*");
        result.add(addCategory(cat));
      }
      
      return result;
    }
  } //AlmanacLoader
  
  /**
	 * This class adds support for a CDAY style event repository with
	 * entries one per line. This code respects births (B) and event (S)
	 * with a date-format of MMDDYYYY.
	 * <code>
	 *  B01011919 J. D. Salinger, author of 'Catcher in the Rye'.
	 *  S04121961 Cosmonaut Yuri Alexeyevich Gagarin becomes first man in orbit.
	 * </code>
	 * The files considered as input have to reside in ./contrib/cday and end in
	 * <code>
	 *  .own
	 *  .all
	 *  .jan, .feb, .mar, .apr, .may, .jun, .jul, .oct, .sep, .nov, .dec
	 * </code>
	 * @see http://cday.sourceforge.net
   */
  private class WikipediaLoader extends Loader {
    
    // 19700525\Births\Nils Meier
    private Pattern REGEX_LINE = Pattern.compile("(.*?)\\\\(.*?)\\\\(.*)");
    
    private String SUFFIX = ".wikipedia.zip";
    
    private String file;
    
    /** our directory */
    protected File getDirectory() {
      
      // we know were those are
      File result = new File(EnvironmentChecker.getProperty(this,
          new String[]{ "genj.wikipedia.dir", "user.dir/contrib/wikipedia"}, "contrib/wikipedia",
          "find wikipedia files"
        ));
      
      // look for applicable one (language)
      String lang = Locale.getDefault().getLanguage();
      String[] list = result.list(this);
      if (list!=null) {
        List files = Arrays.asList(list);
        
        // en.wikipedia?
        if (files.contains(lang+SUFFIX)) 
          file = lang+SUFFIX;
        else if (files.contains("en"+SUFFIX))
          file = "en"+SUFFIX;
        else if (!files.isEmpty())
          file = (String)files.get(0);
      }
      
      // done
      return result;
    }

    /** filter files */
    public boolean accept(File dir, String name) {
      return file==null ? name.endsWith(SUFFIX) : file.equals(name);
    }
    
    /**
     * get buffered reader from file
     */
    protected BufferedReader open(File file) throws IOException {
      ZipInputStream in = new ZipInputStream(new FileInputStream(file));
      ZipEntry entry = in.getNextEntry();
      if (!file.getName().startsWith(entry.getName()))
        throw new IOException("Unexpected entry "+entry+" in "+file);
      return new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
    }
    
    /** create an event */
    protected Event load(String line) throws GedcomException {
      
      // comment?
      if (line.startsWith("#"))
        return null;
      
      // match "YYYYMMDD;group;text"
      Matcher match = REGEX_LINE.matcher(line);
      if (!match.matches())
        return null;

      String yyyymmdd = match.group(1);
      String group = match.group(2) + " (Wikipedia)";
      String text = match.group(3);

      PointInTime pit = new PointInTime(yyyymmdd);
      if (!pit.isValid())
        return null;
      
		  // lookup category
		  List cats = Collections.singletonList(addCategory(group));

      // create event
		  return new Event(cats, pit, text); 
    }
    
  } //CDAY
  
  /**
   * An iterator over a range of events
   */
  private class Range implements Iterator<Event> {
    
    private int start, end;
    
    private PointInTime earliest, latest;
    
    private long origin = -1;
    private long originDelta;
    
    private Event next;

    private Set cats;
    
    /**
     * Constructor
     */
    Range(PointInTime when, int days, Set cats) throws GedcomException {

      earliest = new PointInTime(1-1,1-1,when.getYear()-1);
      latest = new PointInTime(31-1,12-1,when.getYear()+1);
      
	    // convert to julian day
	    origin = when.getJulianDay();
	    originDelta = days;
	    
	    // init
	    init(cats);
  	    
      // done
    }
    
    /**
     * Constructor
     */
    Range(PointInTime from, PointInTime to, Set cats) {
      
      if (!from.isValid()||!to.isValid())
        throw new IllegalArgumentException();
      
      earliest = from;
      latest = to;

      // init
      init(cats);
    }
    
    private void init(Set cats) {
      
      this.cats = cats;
      
	    synchronized (events) {
	      end = events.size();
	      start = getStartIndex(earliest.getYear());
        hasNext();
	    }
    }
    
    /**
     * end
     */
    boolean end() {
      next = null;
      start = end;
      return false;
    }
    
    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
      // one waiting?
      if (next!=null)
        return true;
      // sync'up
      synchronized (events) {
        // events changed?
        if (events.size()!=end)
          return end();
	      // check Event
	      while (true) {
	        // reached the end?
	        if (start==end)
	          return end();
	        // here's the next
		      next = (Event)events.get(start++);
		      // good category?
		      if (cats!=null&&!next.isCategory(cats)) 
	          continue;
		      // before earliest?
		      PointInTime time = next.getTime();
		      if (time.compareTo(earliest)<0)
		        continue;
	        // after latest?
		      if (time.compareTo(latest)>0) 
		        return end();
		      // check against origin?
		      if (origin>0) {
		        long delta = next.getJulian() - origin;
		        if (delta>originDelta) 
			        return end();
		        if (delta<-originDelta)
		          continue;
		      }
		      // found next
	        return true;
	      }
      }
    }
    
    /**
     * @see java.util.Iterator#next()
     */
    public Event next() {
      if (next==null&&!hasNext())
        throw new IllegalArgumentException("no next");
      Event result = next;
      next = null;
      return result;
    }
    
    /**
     * n/a
     * @see java.util.Iterator#remove()
     */
    public void remove() {
      throw new UnsupportedOperationException();
    }
    
    /**
     * find start index of given year in events (log n)
     */
  	private int getStartIndex(int year) {
  	  if (events.isEmpty())
  	    return 0;
      return getStartIndex(year, 0, events.size()-1);
    }
    private int getStartIndex(int year, int start, int end) {
      
      // no range?
      if (end==start)
        return start;

      int pivot = (start + end)/2;

      int y = ((Event)events.get(pivot)).getTime().getYear();
      if (y<year)
        return getStartIndex(year, pivot+1, end);
      return getStartIndex(year, start, pivot);
    }
    
  } //Range
  
} //CDay
