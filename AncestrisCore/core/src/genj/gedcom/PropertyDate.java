/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.gedcom;

import genj.gedcom.time.Calendar;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import genj.util.DirectAccessTokenizer;
import genj.util.WordBuffer;

/**
 * Gedcom Property : DATE
 */
public class PropertyDate extends Property {

    /**
     * time values
     */
    private final PIT start = new PIT(),
            end = new PIT();
    private boolean isAdjusting = false;
    private String valueAsString = null;

    /**
     * the format of the contained date
     */
    private Format format = DATE;

    /**
     * as string
     */
    private String phrase = "";

    /**
     * format definitions
     */
    public final static Format DATE = new Format("", ""),
            FROM_TO = new Format("FROM", "TO"),
            FROM = new Format("FROM", ""),
            TO = new Format("TO", ""),
            BETWEEN_AND = new Format("BET", "AND"),
            BEFORE = new Format("BEF", ""),
            AFTER = new Format("AFT", ""),
            ABOUT = new Format("ABT", ""),
            CALCULATED = new Format("CAL", ""),
            ESTIMATED = new Format("EST", ""),
            INTERPRETED = new Interpreted();

    public final static Format[] FORMATS = {
        DATE, FROM_TO, FROM, TO, BETWEEN_AND, BEFORE, AFTER, ABOUT, CALCULATED, ESTIMATED, INTERPRETED
    };

    /**
     * need tag-argument constructor for all properties
     */
    public PropertyDate(String tag) {
        super(tag);
    }

    public PropertyDate() {
        super("DATE");
    }

    /**
     * Constructor
     */
    public PropertyDate(int year) {
        super("DATE");
        getStart().set(PointInTime.UNKNOWN, PointInTime.UNKNOWN, year);
    }

    /**
     * @see java.lang.Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(Property other) {
        if (other instanceof PropertyDate) {
            return start.compareTo(((PropertyDate) other).start);
        } else {
            return getValue().compareTo(other.getValue());
        }
    }

    @Override
    public PropertyComparator2 getComparator() {
        return DATEComparator.getInstance();
    }

    private static class DATEComparator extends PropertyComparator2.Default<PropertyDate> {

        private static final DATEComparator INSTANCE = new DATEComparator();

        public static PropertyComparator2 getInstance() {
            return INSTANCE;
        }

        /**
         * compare start date.
         *
         * @param d1
         * @param d2
         *
         * @return
         */
        @Override
        public int compare(PropertyDate d1, PropertyDate d2) {
            return d1.start.compareTo(d2.start);
        }

        /**
         * return start year padded with 0 to allow comparison using string
         * comparison.
         *
         * @param p
         *
         * @return
         */
        @Override
        public String getSortGroup(PropertyDate p) {
            if (!p.isValid()) {
                return "";
            }
            String value = "0000" + String.valueOf(p.start.getYear());
            return value.substring(value.length() - 4);
        }
    }

    /**
     * Returns an optional date phrase
     */
    public String getPhrase() {
        return phrase;
    }

    /**
     * Returns starting point
     */
    public PointInTime getStart() {
        return start;
    }

    /**
     * Returns ending point
     */
    public PointInTime getEnd() {
        return end;
    }

    /**
     * Returns the format of this date
     */
    public Format getFormat() {
        return format;
    }

    /**
     * Accessor Value
     */
    @Override
    public String getValue() {
        return valueAsString != null ? valueAsString : format.getValue(this);
    }

    /**
     * Returns whether this date is a range (fromto, betand)
     */
    public boolean isRange() {
        return format.isRange();
    }

    /**
     * Tells whether this date is valid
     *
     * @return <code>boolean</code> indicating validity
     */
    @Override
    public boolean isValid() {
        return (getValue().trim().isEmpty() && super.isValid()) || (valueAsString == null && format.isValid(this));
    }

    /**
     * Check whether this date can be compared successfully to another
     */
    public boolean isComparable() {
        // gotta have a start
        return start.isValid();
    }

    /**
     * Accessir value
     */
    public void setValue(Format newFormat, PointInTime newStart, PointInTime newEnd, String newPhrase) {

        String old = getValue();

        // do an atomic change
        isAdjusting = true;
        try {
            // keep it
            if (newStart == null) {
                start.reset();
            } else {
                start.set(newStart);
            }
            if (newEnd == null) {
                end.reset();
            } else {
                end.set(newEnd);
            }
            phrase = newPhrase;
            valueAsString = null;

            format = (newFormat.needsValidStart() && !start.isValid()) || (newFormat.needsValidEnd() && !end.isValid()) ? DATE : newFormat;
        } finally {
            isAdjusting = false;
        }

        // remember as modified      
        propagatePropertyChanged(this, old);

        // Done
    }

    /**
     * Accessor Format
     */
    public void setFormat(Format set) {

        String old = getValue();

        // do an atomic change
        isAdjusting = true;
        try {
            // set end == start?
            if (!isRange() && set.isRange()) {
                end.set(start);
            }
            // remember
            format = set;
        } finally {
            isAdjusting = false;
        }

        // remember as modified      
        propagatePropertyChanged(this, old);

        // Done
    }

    /**
     * Accessor Value
     */
    @Override
    public void setValue(String newValue) {

        // 20070128 don't bother with calculating old if this is happening in init()
        String old = getParent() == null ? null : getValue();

        // do an atomic change
        isAdjusting = true;
        try {

            // Reset value
            start.reset();
            end.reset();
            format = DATE;
            phrase = "";
            valueAsString = newValue.trim();

            // try to apply one of the formats for non empty
            if (valueAsString.length() > 0) {
                for (Format FORMATS1 : FORMATS) {
                    if (FORMATS1.setValue(newValue, this)) {
                        format = FORMATS1;
                        valueAsString = null;
                        break;
                    }
                }
            }

        } finally {
            isAdjusting = false;
        }

        // remember as modified      
        if (old != null) {
            propagatePropertyChanged(this, old);
        }

        // done
    }

    /**
     * Returns this date as a localized string for display
     */
    @Override
    public String getDisplayValue() {
        return getDisplayValue(null);
    }

    /**
     * Returns this date as a localized string for display
     */
    public String getDisplayValue(Calendar calendar) {
        if (valueAsString != null) {
            return valueAsString;
        }
        return format.getDisplayValue(this, calendar);
    }

    /**
     * @see genj.gedcom.Property#getPropertyInfo()
     */
    @Override
    public String getPropertyInfo() {
        WordBuffer result = new WordBuffer();
        result.append(super.getPropertyInfo());
        result.append("<br>");
        result.append(getDisplayValue());
        if (!(getStart().isGregorian() && getEnd().isGregorian())) {
            result.append("<br>");
            result.append(getDisplayValue(PointInTime.GREGORIAN));
            result.append("(" + PointInTime.GREGORIAN.getName() + ")");
        }
        return result.toString();
    }

    /**
     * Calculate how far this date lies in the past
     *
     * @return the delta or null if n/a
     */
    public Delta getAnniversary() {
        return getAnniversary(PointInTime.getNow());
    }

    /**
     * Calculate how far this date lies in the past from given 'now'
     *
     * @return the delta or null if n/a
     */
    public Delta getAnniversary(PointInTime now) {

        // simply validity check
        if (!isValid()) {
            return null;
        }

        // calculate comparables
        PointInTime pit = isRange() ? getEnd() : getStart();

        // date is in the future?
        if (now.compareTo(pit) < 0) {
            return null;
        }

        // compute the delta
        return Delta.get(pit, now);
    }

    /**
     * A point in time
     */
    private final class PIT extends PointInTime {

        /**
         * Setter
         */
        @Override
        public void set(int d, int m, int y) {

            // adjusting? simply set
            if (isAdjusting) {
                super.set(d, m, y);
            } else {
                // grab old
                String old = super.getValue();
                // set it
                super.set(d, m, y);
                // notify about change 
                propagatePropertyChanged(PropertyDate.this, old);
            }

            // done
        }

    } // class PointInTime

    /**
     * A format definition
     */
    public static class Format {

        protected String start, end;

        private Format(String s, String e) {
            start = s;
            end = e;
        }

        @Override
        public String toString() {
            return start + end;
        }

        public boolean usesPhrase() {
            return false;
        }

        public boolean isRange() {
            return end.length() > 0;
        }

        protected boolean needsValidStart() {
            return true;
        }

        protected boolean needsValidEnd() {
            return isRange();
        }

        public String getName() {
            String key = (start + end).toLowerCase();
            if (key.length() == 0) {
                key = "date";
            }
            return resources.getString("prop.date." + key);
        }

        public String getPrefix1Name() {
            if (start.length() == 0) {
                return null;
            }
            return resources.getString("prop.date.mod." + start, false);
        }

        public String getPrefix2Name() {
            if (end.length() == 0) {
                return null;
            }
            return resources.getString("prop.date.mod." + end, false);
        }

        protected boolean isValid(PropertyDate date) {
            // valid point in times?
            return date.start.isValid() && (!isRange() || date.end.isValid());
        }

        protected String getValue(PropertyDate date) {

            // collect information
            WordBuffer result = new WordBuffer();
            result.append(start);
            date.start.getValue(result);
            if (isRange()) {
                result.append(end);
                date.end.getValue(result);
            }

            // done    
            return result.toString();
        }

        protected String getDisplayValue(PropertyDate date, Calendar calendar) {

            // collect information
            try {
                WordBuffer result = new WordBuffer();

                // start modifier & point in time
                if (start.length() > 0) {
                    result.append(Gedcom.getResources().getString("prop.date.mod." + start));
                }
                if (calendar == null || date.start.getCalendar() == calendar) {
                    date.start.toString(result);
                } else {
                    date.start.getPointInTime(calendar).toString(result);
                }

                // end modifier & point in time
                if (isRange()) {
                    result.append(Gedcom.getResources().getString("prop.date.mod." + end));
                    if (calendar == null || date.end.getCalendar() == calendar) {
                        date.end.toString(result);
                    } else {
                        date.end.getPointInTime(calendar).toString(result);
                    }
                }

                // done    
                return result.toString();

            } catch (GedcomException e) {
                // done in case of error
                return "";
            }
        }

        protected boolean setValue(String text, PropertyDate date) {

            DirectAccessTokenizer tokens = new DirectAccessTokenizer(text, " ", true);
            int afterFirst = 0;
            // check start
            if (start.length() > 0) {
                String first = tokens.get(0);
                if (!first.equalsIgnoreCase(start)) {
                    return false;
                }
                afterFirst = 1;
            }

            // no range?
            if (!isRange()) {
                return date.start.set(tokens.getSubstringFrom(afterFirst));
            }

            // find end
            for (int pos = 1; pos < tokens.count(); pos++) {
                String token = tokens.get(pos);
                if (token.equalsIgnoreCase(end)) {
                    return date.start.set(tokens.getSubstring(afterFirst, pos)) && date.end.set(tokens.getSubstringFrom(pos + 1));
                }
            }

            // didn't work
            return false;
        }

    } //Format

    /**
     * A special format definition
     */
    private static class Interpreted extends Format {

        private Interpreted() {
            super("INT", "");
        }

        @Override
        public boolean usesPhrase() {
            return true;
        }

        @Override
        public boolean isRange() {
            return false;
        }

        @Override
        protected boolean needsValidStart() {
            return false;
        }

        @Override
        protected boolean needsValidEnd() {
            return false;
        }

        @Override
        protected boolean isValid(PropertyDate date) {
            // always true
            return true;
        }

        @Override
        protected boolean setValue(String text, PropertyDate date) {

            // looks like 'INT ...'?
            if (text.length() > start.length() && text.substring(0, start.length()).equalsIgnoreCase(start)) {

                // further 'INT ... ( ...' ?
                int bracket = text.indexOf('(');
                if (bracket > 0 && date.start.set(text.substring(start.length(), bracket))) {
                    date.phrase = text.substring(bracket + 1, text.endsWith(")") ? text.length() - 1 : text.length());
                    return true;
                }
                // maybe 'INT ...'
                if (date.start.set(text.substring(start.length()))) {
                    date.phrase = "";
                    return true;
                }
            }

            // need bracketed phrase ''(...)'?
            if (!text.startsWith("(") || !text.endsWith(")")) {
                return false;
            }

            date.phrase = text.substring(1, text.length() - 1).trim();

            // didn't work
            return true;
        }

        @Override
        protected String getDisplayValue(PropertyDate date, Calendar calendar) {

            try {
                WordBuffer result = new WordBuffer();

                // start modifier & point in time
                if (date.start.isValid()) {
                    if (calendar == null || date.start.getCalendar() == calendar) {
                        date.start.toString(result);
                    } else {
                        date.start.getPointInTime(calendar).toString(result);
                    }
                }

                // phrase
                result.append("(" + date.phrase + ")");

                // done    
                return result.toString();

            } catch (GedcomException e) {
                // done in case of error
                return "";
            }
        }

        @Override
        protected String getValue(PropertyDate date) {

            // collect information
            WordBuffer result = new WordBuffer();

            if (date.start.isValid()) {
                result.append(start);
                date.start.getValue(result);
            }

            result.append("(" + date.phrase + ")");

            // done    
            return result.toString();
        }

    }

} //PropertyDate
