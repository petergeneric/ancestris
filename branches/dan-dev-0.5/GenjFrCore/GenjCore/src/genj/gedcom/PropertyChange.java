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

import genj.gedcom.time.PointInTime;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * CHAN is used by Gedcom to keep track of changes done to entity records.
 * We simply fold
 * <pre>
 *  1 CHAN
 *  2  DATE
 *  3   TIME
 * </pre>
 * into this one property, update the pointintime and time and offer
 * the same three lines on save
 * @author nmeier
 */
public class PropertyChange extends Property implements MultiLineProperty {

  private final static DecimalFormat decimal = new DecimalFormat("00");

  public final static String
   CHAN = "CHAN",
   TIME = "TIME",
   DATE = "DATE";

  private long time = -1;
  
  public PropertyChange() {
    super(CHAN);
    setTime(System.currentTimeMillis());
  }

  /**
   * need tag-argument constructor for all properties
   */
  public PropertyChange(String tag) {
    super(tag);
    assertTag(CHAN);
  }

  /**
   * @see genj.gedcom.Property#isReadOnly()
   */
  public boolean isReadOnly() {
    return true;
  }

  /**
   * Get the last change display value - the date/time localized
   */
  public String getDisplayValue() {
    return time<0 ? "" : getDateDisplayValue() +", "+getTimeDisplayValue();
  }

  /**
   * Get the last change display date
   */
  public String getDateDisplayValue() {
    return time<0 ? "" : PointInTime.getPointInTime(toLocal(time)).toString();
  }

  /**
   * Get the last change display time (local time)
   */
  public String getTimeDisplayValue() {
    return time<=0 ? "" : toString(toLocal(time));
  }

  private long toLocal(long utc) {
    java.util.Calendar c = java.util.Calendar.getInstance();
    return utc + c.get(java.util.Calendar.ZONE_OFFSET) + c.get(java.util.Calendar.DST_OFFSET);
  }

  private String toString(long time) {

    long
      sec = (time/1000)%60,
      min = (time/1000/60)%60,
      hr  = (time/1000/60/60)%24;

    StringBuffer buffer = new StringBuffer();
    buffer.append(decimal.format(hr));
    buffer.append(':');
    buffer.append(decimal.format(min));
    buffer.append(':');
    buffer.append(decimal.format(sec));

    return buffer.toString();
  }

  /**
   * @see genj.gedcom.MultiLineProperty#getLineCollector()()
   */
  public Collector getLineCollector() {
    return new DateTimeCollector();
  }

  /**
   * @see genj.gedcom.MultiLineProperty#getLineIterator()
   */
  public Iterator getLineIterator() {
    return new DateTimeIterator();
  }

  /**
   * Gets change time
   */
  public long getTime() {
    return time;
  }

  /**
   * Set current value
   */
  public void setTime(long set) {

    // known?
    if (time==set)
      return;

    String old = getValue();

    // keep time before propagate so no endless loop happens
    time = set;

    // notify
    propagatePropertyChanged(this, old);

    // done
  }

  /**
   * Interpret a gedcom value as "date, UTF" as passed in by DateTimeCollector
   * @see genj.gedcom.Property#setValue(java.lang.String)
   */
  public void setValue(String value) {

    String old = getValue();

    // must look like 19 DEC 2003,14:50
    int i = value.indexOf(',');
    if (i<0)
      return;

    try {
      time = 0;

      // parse time hh:mm:ss
      StringTokenizer tokens = new StringTokenizer(value.substring(i+1), ":");
      if (tokens.hasMoreTokens())
        time += Integer.parseInt(tokens.nextToken()) * 60 * 60 * 1000;
      if (tokens.hasMoreTokens())
        time += Integer.parseInt(tokens.nextToken()) * 60 * 1000;
      if (tokens.hasMoreTokens())
        time += Integer.parseInt(tokens.nextToken()) * 1000;

      // parse date
      time += PointInTime.getPointInTime(value.substring(0,i)).getTimeMillis();

      // update gedcom's last change time
      getGedcom().updateLastChange(this);

    } catch (Throwable t) {

      time = -1;
    }

    // notify
    propagatePropertyChanged(this, old);

    // done
  }

  /**
   * Gedcom value - this is an intermittend value only that won't be saved (it's not Gedcom compliant but contains a valid gedcom date)
   */
  public String getValue() {
    return time<0 ? "" : PointInTime.getPointInTime(time).getValue() +','+toString(time);
  }

  /**
   * @see genj.gedcom.Property#compareTo(java.lang.Object)
   */
  public int compareTo(Property other) {
    // compare time
    if (time<((PropertyChange)other).time)
      return -1;
    if (time>((PropertyChange)other).time)
      return 1;
    return 0;
  }

  /**
   * @see genj.gedcom.Property#setPrivate(boolean, boolean)
   */
  public void setPrivate(boolean set, boolean recursively) {
    // ignored
  }

  /**
   * Continuation for handling multiple lines concerning this change
   */
  private class DateTimeCollector implements MultiLineProperty.Collector {

    private String dateCollected, timeCollected;

    /**
     * @see genj.gedcom.MultiLineSupport.Continuation#append(int, java.lang.String, java.lang.String)
     */
    public boolean append(int indent, String tag, String value) {

      // DATE
      if (indent==1&&DATE.equals(tag)) {
        dateCollected = value;
        return true;
      }

      // TIME
      if (indent==2&&TIME.equals(tag)) {
        timeCollected = value;
        return true;
      }

      // unknown
      return false;
    }

    /**
     * @see genj.gedcom.MultiLineProperty.Collector#getValue()
     */
    public String getValue() {
      return dateCollected+','+timeCollected;
    }

  } //MyContinuation

  /**
   * Iterator for lines wrapped in this change
   */
  private class DateTimeIterator implements MultiLineProperty.Iterator {

    /** tracking line index */
    int i = 0;

    /** lines */
    private String[]
      tags = { CHAN, DATE, TIME  },
      values = { "", PointInTime.getPointInTime(time).getValue(), PropertyChange.this.toString(time) };

    /**
     * @see genj.gedcom.MultiLineProperty.Iterator#setValue(java.lang.String)
     */
    public void setValue(String value) {
      // ignored
    }

    /**
     * @see genj.gedcom.MultiLineSupport.LineIterator#getIndent()
     */
    public int getIndent() {
      return i;
    }

    /**
     * @see genj.gedcom.MultiLineSupport.Line#getTag()
     */
    public String getTag() {
      return tags[i];
    }

    /**
     * @see genj.gedcom.MultiLineSupport.Line#getValue()
     */
    public String getValue() {
      return values[i];
    }
    /**
     * @see genj.gedcom.MultiLineSupport.Line#next()
     */
    public boolean next() {
      return time>=0 && ++i!=tags.length;
    }

  } //Lines

  /**
   * A gedcom listener that will update CHANs
   */
  /*package*/ static class Monitor extends GedcomListenerAdapter {

    private Set<Entity> updated = new HashSet<Entity>();

    /** update entity for given property */
    private void update(Property where) {

      Entity entity = where.getEntity();
      if (updated.contains(entity))
        return;

      // ignore if something happened below PropertyChange
      while (where!=null) {
        if (where instanceof PropertyChange)
          return;
        where = where.getParent();
      }

      // update it
      Gedcom.LOG.finer("updating CHAN for "+entity.getId());

      // is allowed?
      MetaProperty meta = entity.getMetaProperty();
      if (!meta.allows(PropertyChange.CHAN))
        return;

      // update values (tx time is UTC time!)
      PropertyChange prop = (PropertyChange)entity.getProperty(PropertyChange.CHAN);
      if (prop==null)
        prop = (PropertyChange)entity.addProperty(new PropertyChange());
      else
        prop.setTime(System.currentTimeMillis());

      // remember
      updated.add(entity);
      entity.getGedcom().updateLastChange(prop);
    }

    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
      update(entity);
    }

    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
      updated.remove(entity);
    }

    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
      update(added);
    }

    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
      update(property);
    }

    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
      if (!(deleted instanceof PropertyChange))
        update(property);
    }

  } //Tracker

} //PropertyChange