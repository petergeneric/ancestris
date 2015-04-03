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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.gedcom;

import genj.util.DirectAccessTokenizer;
import genj.util.ReferenceSet;
import genj.util.swing.ImageIcon;
import java.text.Collator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * PLAC a choice value with brains for understanding sub-property FORM
 */
public class PropertyPlace extends PropertyChoiceValue {

    private final static boolean USE_SPACES = GedcomOptions.getInstance().isUseSpacedPlaces();
    ;

  public final static ImageIcon IMAGE = Grammar.V55.getMeta(new TagPath("INDI:BIRT:PLAC")).getImage();

    public final static String JURISDICTION_SEPARATOR = ",";

    private final static String JURISDICTION_RESOURCE_PREFIX = "prop.plac.jurisdiction.";

    public final static String TAG = "PLAC",
            FORM = "FORM";

    /**
     * need tag-argument constructor for all properties
     */
    public PropertyPlace(String tag) {
        super(tag);
    }

    /**
     * Overridden - special trim
     */
    protected String trim(String value) {
        StringBuilder buf = new StringBuilder(value.length());
        DirectAccessTokenizer jurisdictions = new DirectAccessTokenizer(value, JURISDICTION_SEPARATOR);
        for (int i = 0;; i++) {
            String jurisdiction = jurisdictions.get(i, true);
            if (jurisdiction == null) {
                break;
            }
            if (i > 0) {
                buf.append(JURISDICTION_SEPARATOR);
                if (USE_SPACES) {
                    buf.append(' ');
                }
            }
            buf.append(jurisdiction);
        }
        return buf.toString().intern();
    }

    /**
     * Remember a jurisdiction's value
     */
    @Override
    protected boolean remember(String theOld, String theNew) {

        // let super do its stuff
        if (!super.remember(theOld, theNew)) {
            return false;
        }
        Gedcom gedcom = getGedcom();

        // forget old jurisdictions
        DirectAccessTokenizer jurisdictions = new DirectAccessTokenizer(theOld, JURISDICTION_SEPARATOR);
        for (int i = 0;; i++) {
            String jurisdiction = jurisdictions.get(i, true);
            if (jurisdiction == null) {
                break;
            }
            // forget PLAC.n
            if (jurisdiction.length() > 0) {
                gedcom.getReferenceSet(TAG + "." + i).remove(jurisdiction, this);
            }
            // next
        }

        // remember new jurisdictions
        jurisdictions = new DirectAccessTokenizer(theNew, JURISDICTION_SEPARATOR);
        for (int i = 0;; i++) {
            String jurisdiction = jurisdictions.get(i, true);
            if (jurisdiction == null) {
                break;
            }
            // remember PLAC.n
            if (jurisdiction.length() > 0) {
                gedcom.getReferenceSet(TAG + "." + i).add(jurisdiction.intern(), this);
            }
            // next
        }

        // done
        return true;
    }

    /**
     * Accessor - format
     */
    public String[] getFormat() {
        return toJurisdictions(getFormatAsString());
    }

    /**
     * Accessor - format
     */
    public static String[] getFormat(Gedcom gedcom) {
        return toJurisdictions(gedcom.getPlaceFormat());
    }

    private static String[] toJurisdictions(String value) {
        ArrayList<String> result = new ArrayList<String>(10);
        String lastToken = JURISDICTION_SEPARATOR;
        for (StringTokenizer tokens = new StringTokenizer(value, ",", true); tokens.hasMoreTokens();) {
            String token = tokens.nextToken();
            if (!JURISDICTION_SEPARATOR.equals(token)) {
                result.add(token);
            } else if (JURISDICTION_SEPARATOR.equals(lastToken)) {
                result.add("");
            }
            lastToken = token;
        }
        if (JURISDICTION_SEPARATOR.equals(lastToken)) {
            result.add("");
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Accessor - the format of this place's value (non localized)
     */
    public String getFormatAsString() {
        // look it up
        String result = "";
        Property pformat = getProperty(FORM);
        if (pformat != null) {
            result = pformat.getValue();
        } else {
            Gedcom ged = getGedcom();
            if (ged != null) {
                result = ged.getPlaceFormat();
            }
        }
        // done
        return result;
    }

    /**
     * Accessor - the hierarchy of this place's value (non localized)
     */
    public void setFormatAsString(boolean global, String format) {
        if (!global) {
            throw new IllegalArgumentException("non-global n/a");
        }
        // propagate
        getGedcom().setPlaceFormat(format);
        // mark changed
        propagatePropertyChanged(this, getValue());
    }

    /**
     * Accessor - all places with the same jurisdiction for given hierarchy level
     */
    public PropertyPlace[] getSameChoices(int hierarchyLevel) {
        String jurisdiction = getJurisdiction(hierarchyLevel);
        if (jurisdiction == null) {
            return null;
        }
        Collection<Property> places = getGedcom().getReferenceSet(TAG + "." + hierarchyLevel).getReferences(jurisdiction);
        return places.toArray(new PropertyPlace[places.size()]);
    }

    /**
     * Accessor - all places with the same jurisdiction for given hierarchy level
     */
    public static PropertyPlace[] getSameChoices(Gedcom gedcom, int hierarchyLevel, String jurisdiction) {
        if (jurisdiction == null) {
            return null;
        }
        Collection<Property> places = gedcom.getReferenceSet(TAG + "." + hierarchyLevel).getReferences(jurisdiction);
        return places.toArray(new PropertyPlace[places.size()]);
    }

    /**
     * Accessor - all jurisdictions of given level in same gedcom file
     */
    public String[] getAllJurisdictions(int hierarchyLevel, boolean sort) {
        Gedcom gedcom = getGedcom();
        if (gedcom == null) {
            return new String[0];
        }
        return getAllJurisdictions(gedcom, hierarchyLevel, sort);
    }

    /**
     * Accessor - all jurisdictions of given level in gedcom
     *
     * @param hierarchyLevel either a zero-based level or -1 for whole place values
     */
    public static String[] getAllJurisdictions(Gedcom gedcom, int hierarchyLevel, boolean sort) {
        ReferenceSet<String, Property> refset = gedcom.getReferenceSet(hierarchyLevel < 0 ? TAG : TAG + "." + hierarchyLevel);
        Collection<String> jurisdictions = refset.getKeys(sort ? gedcom.getCollator() : null);
        return jurisdictions.toArray(new String[jurisdictions.size()]);
    }

    /**
     * Accessor - first non-empty jurisdiction
     *
     * @return jurisdiction of zero+ length
     */
    public String getFirstAvailableJurisdiction() {
        DirectAccessTokenizer jurisdictions = new DirectAccessTokenizer(getValue(), JURISDICTION_SEPARATOR);
        String result = "";
        for (int i = 0; result.length() == 0; i++) {
            result = jurisdictions.get(i, true);
            if (result == null) {
                return "";
            }
        }
        return result;
    }

    /**
     * Accessor - jurisdiction of given level
     *
     * @return jurisdiction of zero+ length or null if n/a
     */
    public String getJurisdiction(int hierarchyLevel) {
        return new DirectAccessTokenizer(getValue(), JURISDICTION_SEPARATOR).get(hierarchyLevel, true);
    }

    /**
     * Accessor - jurisdictions
     */
    public String[] getJurisdictions() {
        return toJurisdictions(getValue());
    }

    /**
     * Accessor - jurisdictions that is the city
     */
    public String getCity() {
        int cityIndex = getCityIndex();
        if (cityIndex < 0) {
            return getFirstAvailableJurisdiction();
        }
        String city = new DirectAccessTokenizer(getValue(), JURISDICTION_SEPARATOR).get(cityIndex, true);
        return city != null ? city : "";
    }

    /**
     * Accessor - all jurisdictions starting with city
     */
    public String getValueStartingWithCity() {
        // grab result
        String result = getValue();
        // check city index - we assume it start with the first if n/a
        int cityIndex = getCityIndex();
        if (cityIndex <= 0) {
            return result;
        }
        // grab sub
        return new DirectAccessTokenizer(result, JURISDICTION_SEPARATOR).getSubstring(cityIndex);
    }

    /**
     * Derive index of city value in the list of jurisdictions in this place
     *
     * @return zero based index or -1 if not determined
     */
    private int getCityIndex() {

        // try to get a place format
        if (getFormatAsString().length() == 0) {
            return -1;
        }

        // look for a city key in the hierarchy
        Set<String> cityKeys = GedcomOptions.getInstance().getPlaceHierarchyCityKeys();
        String[] format = getFormat();
        for (int i = 0; i < format.length; i++) {
            if (cityKeys.contains(format[i].toLowerCase())) {
                return i;
            }
        }

        // don't know
        return -1;
    }

    /**
     * Return PropertyMap for this Place.
     * Resolve aginst gedcom version
     * @return 
     */
    public PropertyMap getMap(){
        if (isVersion55()){
            return (PropertyMap)getProperty("_MAP");
        } else {
            return (PropertyMap)getProperty("MAP");
        }
    }

    /**
     * Display value for a place where format is one of
     * <pre>
     *  null      1st available
     *  ""        1st available
     *  "all"     all jurisdictions
     *  "0,1 (3)" 1st, 2nd (3rd)
     * @return format format
     */
    @Override
    public String format(String format) {

        if (format == null) {
            format = getGedcom() == null ? null : getGedcom().getPlaceDisplayFormat();
        }
        if (format == null) {
            return getFirstAvailableJurisdiction();
        }

        String f = format.trim();

        if (f.equals("")) {
            return getFirstAvailableJurisdiction();
        }

        if (f.equals("all")) {
            return getDisplayValue();
        }
        StringBuilder result = new StringBuilder();
        String[] jurisdictions = getJurisdictions();
        for (int i = 0; i < f.length(); i++) {
            char c = f.charAt(i);
            if (Character.isDigit(c)) {
                int j = Character.digit(c, 10);
                if (j < jurisdictions.length) {
                    result.append(jurisdictions[j].trim());
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    @Override
    public PropertyComparator2 getComparator() {
        return PLACComparator.getInstance();
    }

    private static class PLACComparator extends PropertyComparator2.Default<PropertyPlace> {

        private static final PLACComparator INSTANCE = new PLACComparator();

        public static PropertyComparator2 getInstance() {
            return INSTANCE;
        }

        @Override
        public int compare(PropertyPlace p1, PropertyPlace p2) {

            int r = compareNull(p1, p2);
            if (r != Integer.MAX_VALUE) {
                return r;
            }
            Collator c = p1.getGedcom().getCollator();
            String sortFormat = p1.getGedcom().getPlaceSortOrder();
            if (sortFormat == null) {
                return c.compare(p1.getValueStartingWithCity(), p2.getValueStartingWithCity());
            } else {
                return c.compare(p1.format(sortFormat), p2.format(sortFormat));
            }
        }

        @Override
        public String getSortGroup(PropertyPlace p) {
            String sortFormat = p.getGedcom().getPlaceSortOrder();
            String value = (sortFormat == null)
                    ? p.getValueStartingWithCity()
                    : p.format(sortFormat);
            return shortcut(value, 1);
        }
    }
} //PropertyPlace
