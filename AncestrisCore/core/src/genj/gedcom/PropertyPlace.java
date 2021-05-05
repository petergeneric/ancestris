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

import genj.util.DirectAccessTokenizer;
import genj.util.ReferenceSet;
import genj.util.swing.ImageIcon;
import java.text.Collator;
import java.util.Collection;
import java.util.Set;

/**
 * PLAC a choice value with brains for understanding sub-property FORM
 */
public class PropertyPlace extends PropertyChoiceValue {

    private final static boolean USE_SPACES = GedcomOptions.getInstance().isUseSpacedPlaces();

    public final static ImageIcon IMAGE = Grammar.V55.getMeta(new TagPath("INDI:BIRT:PLAC")).getImage();

    public final static String JURISDICTION_SEPARATOR = ",";

    public final static String TAG = "PLAC",
            FORM = "FORM";

    /**
     * need tag-argument constructor for all properties
     */
    public PropertyPlace(String tag) {
        super(tag);
    }

    /**
     * Tells whether this place is valid
     *
     * @return <code>boolean</code> indicating validity (not empty or includes sub-tags)
     */
    @Override
    public boolean isValid() {
        return getValue().trim().length() != 0 || getNoOfProperties() != 0;
    }

    /**
     * Overridden - special trim
     */
    @Override
    protected String trim(String value) {

        /*
         20051212 at some point we switched to trimming values on places
         here, making sure that the separator only is between jurisdictions.
         Peter asked me to add spaces as well for readability:
         2 PLAC Hamburg, Schleswig Holstein, Deutschland
         instead of
         2 PLAC Hamburg,Schleswig Holstein,Deutschland

         But Francois reminded me that we didn't want to have spaces in
         the Gedcom file - the spec doesn't explicitly disallow it but especially
         in Francois' way of keeping place information
         2 PLAC ,Allanche,,Cantal,Auvergne,
         adding spaces doesn't look good
         2 PLAC , Allanche, , Cantal, Auvergne,

         We played with the idea of using space-comma in getDisplayValue()
         and comma-only in getValue()/trim() - problem is that it takes mem
         to cache or runtime performance to calculate that. It's also problematic
         that the display value would be different from the choices remembered
         (one with space the other without)

         So finally we decided to put in a global option that lets the user
         make the choice - internally getValue()-wize we handle this uniformly then
         */
        // trim each jurisdiction separately
        int numberValue = value.split(JURISDICTION_SEPARATOR, -1).length;
        int numberFormat = getFormatAsString().split(JURISDICTION_SEPARATOR, -1).length;
        if (numberFormat < numberValue) {
            numberFormat = numberValue;
        }
        StringBuilder buf = new StringBuilder(value.length());
        DirectAccessTokenizer jurisdictions = new DirectAccessTokenizer(value, JURISDICTION_SEPARATOR);
        for (int i = 0; i < numberFormat; i++) {
            String jurisdiction = jurisdictions.get(i, true);
            if (i > 0) {
                buf.append(JURISDICTION_SEPARATOR);
                if (USE_SPACES) {
                    buf.append(' ');
                }
            }
            if (jurisdiction != null) {
                buf.append(jurisdiction);
            }
        }
        final String retour = buf.toString().intern();
        if ("".equals(retour.replaceAll(JURISDICTION_SEPARATOR, "").trim())) {
            return "";
        }
        return retour;
    }

    static public String formatSpaces(String str) {
        String addStr = PropertyPlace.JURISDICTION_SEPARATOR + (USE_SPACES ? " " : "");
        String[] placeFormatList = PropertyPlace.getFormat(str);
        String value = "";
        for (int i = 0; i < placeFormatList.length; i++) {
            String p = placeFormatList[i].trim();
            if (i == placeFormatList.length - 1) {
                addStr = "";
            }
            value += p + addStr;
        }
        return value;
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
    public static String[] getFormat(String str) {
        return toJurisdictions(str);
    }

    /**
     * Returns Place Format for gedcom.
     *
     * @param gedcom
     * @return
     */
    public static String[] getFormat(Gedcom gedcom) {
        return getFormat(gedcom, false);
    }

    /**
     * Returns Place Format for gedcom. fallback to AncestrisSetting
     *
     * @param gedcom
     * @return
     */
    public static String[] getFormat(Gedcom gedcom, boolean fallback) {
        String[] format = toJurisdictions(gedcom.getPlaceFormat());
        if (fallback && (format == null || format.length == 0)) {
            format = toJurisdictions(GedcomOptions.getInstance().getPlaceFormat());
        }
        return format;
    }

    private static String[] toJurisdictions(String value) {
        final DirectAccessTokenizer dat = new DirectAccessTokenizer(value, JURISDICTION_SEPARATOR);
        return dat.getTokens(true);
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
     * Accessor - all places with the same jurisdiction for given hierarchy
     * level
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
     * Accessor - all places with the same jurisdiction for given hierarchy
     * level
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
     * @param hierarchyLevel either a zero-based level or -1 for whole place
     * values
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

    public String getLastAvailableJurisdiction() {
        DirectAccessTokenizer jurisdictions = new DirectAccessTokenizer(getValue(), JURISDICTION_SEPARATOR);
        String result = "";
        for (int i = jurisdictions.count()-1; result.length() == 0 && i>=0; i--) {
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
     * Accessor - jurisdictions
     */
    public boolean setJurisdictions(Gedcom gedcom, String[] locs) {
        int nbLocs = PropertyPlace.getFormat(gedcom.getPlaceFormat()).length;

        // If nb of jurisdictions of correct length, set it and return true
        if (locs.length == nbLocs) {
            setValue(arrayToString(locs));
            return true;
        }

        // If too short, fill in first jurisdictions
        if (locs.length < nbLocs) {
            String[] newLocs = new String[nbLocs];
            for (int i = 0; i < nbLocs - locs.length; i++) {
                newLocs[i] = "";
            }
            for (int i = nbLocs - locs.length; i < nbLocs; i++) {
                newLocs[i] = locs[i - (nbLocs - locs.length)];
            }
            setValue(arrayToString(newLocs));
            return false;
        }

        // If too long, truncate empty jurisdictions on the right, then group them on the right
        if (locs.length > nbLocs) {
            String[] newLocs = new String[nbLocs];
            int end = locs.length;
            // Trim on the right first
            int idx = end - 1;
            while (idx >= 0 && locs[idx].trim().isEmpty()) {
                end--;
                if (end == nbLocs) {
                    break;
                }
                idx--;
            }

            // Now group the necessary number of first jurisdictions on the left side
            int tmp = end - nbLocs;
            while (tmp > 0) {
                if (!locs[tmp - 1].trim().isEmpty()) {
                    locs[end - nbLocs] += " - " + locs[tmp - 1];
                }
                tmp--;
            }
            
            // Now allocate
            for (int i = 0; i < nbLocs; i++) {
                newLocs[i] = locs[end - nbLocs + i];
            }
            setValue(arrayToString(newLocs));
            return false;
        }

        return true;
    }

    public static String arrayToString(String[] locs) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < locs.length; i++) {
            if (i > 0) {
                result.append(JURISDICTION_SEPARATOR);
            }
            result.append(locs[i]);
        }
        return result.toString();
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

    public String getCountry() {
        int countryIndex = getCountryIndex();
        if (countryIndex < 0) {
            return getLastAvailableJurisdiction();
        }
        String country = new DirectAccessTokenizer(getValue(), JURISDICTION_SEPARATOR).get(countryIndex, true);
        return country != null ? country : "";
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
        return new DirectAccessTokenizer(result, JURISDICTION_SEPARATOR).getSubstringFrom(cityIndex);
    }

    /**
     * Accessor - display value with geo coordinates
     */
    public String getGeoValue() {
        Property latitude = getLatitude(false);
        Property longitude = getLongitude(false);

        String gedcomPlace = getDisplayValue()
                + PropertyPlace.JURISDICTION_SEPARATOR
                + (latitude != null ? latitude.getValue() : "")
                + PropertyPlace.JURISDICTION_SEPARATOR
                + (longitude != null ? longitude.getValue() : "");

        return gedcomPlace;
    }

    /**
     * Accessor - numerical jurisdictions
     */
    public String getNumericalJurisdictions() {
        String str = "";
        String[] juris = toJurisdictions(getValue());
        for (int i = 0; i < juris.length; i++) {
            String juri = juris[i].replace(" ", "");
            if (juri.matches("-?\\d+(\\.\\d+)?")) {         //match a number with optional '-' and decimal.
                str += juri + JURISDICTION_SEPARATOR;
            }
        }
        return str;
    }

    /**
     * Derive index of city value in the list of jurisdictions in this place
     *
     * @return zero based index or -1 if not determined
     */
    public int getCityIndex() {

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

    public int getCountryIndex() {

        // try to get a place format
        if (getFormatAsString().length() == 0) {
            return -1;
        }

        // look for a country key in the hierarchy
        Set<String> countryKeys = GedcomOptions.getInstance().getPlaceHierarchyCountryKeys();
        String[] format = getFormat();
        for (int i = 0; i < format.length; i++) {
            if (countryKeys.contains(format[i].toLowerCase())) {
                return i;
            }
        }

        // don't know
        return -1;
    }
    public static String getCityTag(Gedcom gedcom) {
        Set<String> cityKeys = GedcomOptions.getInstance().getPlaceHierarchyCityKeys();
        String[] format = getFormat(gedcom);
        for (int i = 0; i < format.length; i++) {
            if (cityKeys.contains(format[i].toLowerCase())) {
                return format[i];
            }
        }
        return "";
    }

    /**
     * Return PropertyMap for this Place. Resolve aginst gedcom version
     *
     * @return
     */
    public PropertyMap getMap() {
        if (isVersion55()) {
            return (PropertyMap) getProperty("_MAP");
        } else {
            return (PropertyMap) getProperty("MAP");
        }
    }

    /**
     * Shortcut for PropertyPlace.getMap.getLatitude(). returns null if not
     * available. If strict is true, return null if Longitude is not available
     * meaning that a Latitude with no Longitude set has no real sens
     *
     * @param strict
     * @return
     */
    public PropertyLatitude getLatitude(boolean strict) {
        PropertyMap map = getMap();
        if (map == null) {
            return null;
        }
        PropertyLatitude lat = map.getLatitude();
        if (lat == null) {
            return null;
        }
        if (strict && map.getLongitude() == null) {
            return null;
        }
        return lat;
    }

    /**
     * Shortcut for PropertyPlace.getMap.getLongitude(). returns null if not
     * available. If strict is true, return null if Latitude is not available
     * meaning that a Longitude with no Latitude set has no real sens
     *
     * @param strict
     * @return
     */
    public PropertyLongitude getLongitude(boolean strict) {
        PropertyMap map = getMap();
        if (map == null) {
            return null;
        }
        PropertyLongitude longitude = map.getLongitude();
        if (longitude == null) {
            return null;
        }
        if (strict && map.getLatitude() == null) {
            return null;
        }
        return longitude;
    }

    /**
     * Set PLAC::MAP::LATI value, adding properties if necessary.
     *
     * @param latitude
     * @param longitude
     */
    public void setCoordinates(String latitude, String longitude) {
        if (latitude.isEmpty() && longitude.isEmpty()) {
            PropertyMap map = getMap();
            if (map == null) {
                return;
            }
            map.delProperties();
            delProperty(map);
            return;
        }

        PropertyMap map = getMap();
        boolean is55 = isVersion55();
        if (map == null) {
            //add map property
            map = new PropertyMap(is55);
            addProperty(map);
        }
        Property lat = map.getProperty("LATI", false); // do not use getLatitude which uses strict, we need to replace an invalid value
        if (lat == null) {
            // Add latitude
            lat = new PropertyLatitude(is55);
            map.addProperty(lat);
        }
        lat.setValue(latitude);
        Property lon = map.getProperty("LONG", false); // do not use getLatitude which uses strict, we need to replace an invalid value
        if (lon == null) {
            // Add latitude
            lon = new PropertyLongitude(is55);
            map.addProperty(lon);
        }
        lon.setValue(longitude);
    }

    /**
     * Set coordinates as global change (look for the property place which value
     * is the same and where coordinates exist)
     */
    public void setCoordinates(boolean global) {

        if (global) {
            // change coordinates of all places with same value
            Property[] others = getSameChoices();
            for (Property other : others) {
                if (other instanceof PropertyPlace && other != this) {
                    ((PropertyPlace) other).setCoordinates();
                }
            }
        }

        // set coordinates of this place
        setCoordinates();
    }

    /**
     * Set coordinates from first matching place with coordinates in the gedcom
     * with same value
     */
    public void setCoordinates() {
        
        if (getValue().isEmpty()) {
            setCoordinates("", "");
            return;
        }
        
        // Get first place with same value where coordinates exist
        PropertyPlace source = null;
        Property[] others = getSameChoices();
        for (Property other : others) {
            if (other instanceof PropertyPlace && other != this && other.getValue().equals(getValue())) {
                if (((PropertyPlace) other).getMap() != null) {
                    source = (PropertyPlace) other;
                    break;
                }
            }
        }

        // Put these found coordinates to this place
        if (source != null) {
            PropertyLatitude lat = source.getLatitude(false);
            PropertyLongitude lon = source.getLongitude(false);
            if (lat != null && lon != null) {
                setCoordinates(lat.getValue(), lon.getValue());
            }
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
        Gedcom localGedcom = getGedcom();
        if (format == null) {
            format = (localGedcom == null) ? null : localGedcom.getPlaceDisplayFormat();
        }
        if (format == null) {
            return this.getDisplayValue().replaceAll(JURISDICTION_SEPARATOR, " ").replaceAll(" +", " ");   //getFirstAvailableJurisdiction();
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
        // TODO: // replace only digit not surrounded by other digits (ex: zip code is not to be replaced)
        // Is it still used in geo?
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

    public String getPlaceToLocalFormat() {
        return getValueStartingWithCity().replaceAll(PropertyPlace.JURISDICTION_SEPARATOR, " ").replaceAll(" +", " ").trim();
    }

} //PropertyPlace
