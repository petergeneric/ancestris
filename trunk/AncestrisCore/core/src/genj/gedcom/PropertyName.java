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

import genj.crypto.Enigma;
import genj.util.ReferenceSet;
import genj.util.WordBuffer;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gedcom Property : NAME
 */
// XXX: This class probably need to be design
public class PropertyName extends Property {

    // TODO change to enum when migrating to java 5
    // and apply at least to getSameLastNames and getLastNameCount
    public static final int PREFIX_AS_IS = 0;
    public static final int PREFIX_LAST = 1;
    public static final int IGNORE_PREFIX = 2;

    public final static String TAG = "NAME";
    private final static String KEY_LASTNAME = "NAME.last", KEY_FIRSTNAME = "NAME.first", KEY_NICKNAME = "NICK";
    /** the first + last name */
    private String lastName = "",
    firstName = "",
    suffix = "";
    // XXX:nameValue should probably be replaced by nameAsString
    private String nameTagValue;
    /** the name if unparsable */
    private String nameAsString;
    
    // use busy flag to avoid loopings
    private int mutePropertyChange = 0;

    /**
     * need tag-argument constructor for all properties
     */
    /*package*/ PropertyName(String tag) {
        super(tag);
    }

    /**
     * Empty Constructor
     */
    public PropertyName() {
        super("NAME");
    }

    @Override
    public PropertyComparator2 getComparator() {
        return NAMEComparator.getInstance();
    }
    public boolean isMutePropertyChange() {
        return mutePropertyChange!=0;
    }

    public boolean mutePropertyChange() {
        boolean isMute = isMutePropertyChange();
        this.mutePropertyChange++;
        return isMute;
    }
    public void unmutePropertyChange() {
        unmutePropertyChange(false);
    }
    public void unmutePropertyChange(boolean clear) {
        if (clear)
            mutePropertyChange = 0;
        else {
            this.mutePropertyChange--;
            if (mutePropertyChange<0)
                mutePropertyChange = 0;
        }
    }


    /**
     * the first name.
     * Returns First Name as display value ie in a user friendly format
     */
    public String getFirstName() {
        return gedcomToValue(firstName);
    }

    /**
     * Returns the name given to an Individual.
     *
     * @return
     */
    public String getGivenName() {
        String tagGiven = GedcomOptions.getInstance().getGivenTag();
        String firstNames[] = firstName.split(",");
        String given = null;
        if (tagGiven.isEmpty()) {
            for (String first : firstNames) {
                first = first.trim();
                if (first.matches("\"[^\"]*\"")
                        || first.matches("<[^>]*>")
                        || first.matches("\\[[^\\]]*\\]")) {
                    given = first.substring(1, first.length() - 1);
                    break;
                }
            }
        } else if (getProperty(tagGiven) != null) {
            given = getProperty(tagGiven).getValue();
        }
        return (given == null ? firstNames[0] : given);
    }

    /**
     * Returns <b>true</b> if this property is valid
     */
    @Override
    //TODO: check
    public boolean isValid() {
        /// no indi -> true
        if (!(getEntity() instanceof Indi || getEntity() instanceof Submitter)) {
            return true;
        }
        if (nameAsString != null) {
            return false;
        }
        return true;
//        if (nameTagValue == null) {
//            return true;
//        }
//        // NAME is considered valid if NAME TAG value is equivalent to computed NAME TAG value from all subtags.
//        // We do not consider space character around / (for geneatique compatibility
//        // We do consider the case of char (ie SURN may be UPPER where NAME is not)
//        // XXX: We should consider the case where there is no sub tags in NAME structure
//        return nameTagValue.replaceAll(" */ *", "/").replaceAll(" +", " ").equalsIgnoreCase(computeNameValue().replaceAll(" */ *", "/"));
    }

    /**
     * Returns true if property has some inconsticency.
     * Retuns true if is valid but contains some not properly formated data
     * or inconsticency (data syntactically correct but out of realisme, 
     * eg age older than 200 years, ...)
     */
    public boolean hasWarning() {
        Collator c = getGedcom().getCollator();

        if (isValid() && 
                ! c.equals(nameTagValue.replaceAll(" */ *", "/").replaceAll(" +", " "),
                        computeNameValue().replaceAll(" */ *", "/"))
                ){
            return true;
        }
        return false;
    }
    
    /**
     * Returns localized label for first name
     */
    static public String getLabelForFirstName() {
        return Gedcom.getResources().getString("prop.name.firstname");
    }

    /**
     * Returns localized label for last name
     */
    static public String getLabelForLastName() {
        return Gedcom.getResources().getString("prop.name.lastname");
    }

    /**
     * Returns localized label for last name
     */
    static public String getLabelForSuffix() {
        return Gedcom.getResources().getString("prop.name.suffix");
    }

    /**
     * the last name
     */
    public String getLastName() {
        return lastName;
    }
  

    /**
     * nested nickname
     */
    public String getNick() {
        return getPropertyValue("NICK");
    }

    public void setNick(String nick) {
        Property n = getProperty("NICK");
        if (n == null) {
            if (nick.length() == 0) {
                return;
            }
            addProperty("NICK", nick);
        } else {
            n.setValue(nick);
        }
    }


    /**
     * the name prefix
     */
    public String getNamePrefix() {
        return gedcomToValue(getPropertyValue("NPFX"));
    }

    public String getNamePrefix(boolean displayValue) {
        if (displayValue) {
            return getNamePrefix().replaceAll(" *, *", " ");
        } else {
            return getNamePrefix();
        }
    }

    /**
     * the surname prefix
     */
    public String getSurnamePrefix() {
        return getPropertyValue("SPFX");
    }

    /**
     * the suffix
     */
    public String getSuffix() {
        return getPropertyValue("NSFX").trim();
    }

    /**
     * the gedcom value
     */
    @Override
    public String getValue() {

        if (nameAsString != null) {
            return nameAsString;
        }
        if (nameTagValue != null) {
            return nameTagValue;
        }
        return computeNameValue();
    }

    public String getNameTagValue() {
        return nameTagValue;
    }

    /**
     * the Name Value computed by appending each name parts (given, surname, prefix, suffix).
     * This value is used when there is no conflict between those parts and the gedcom NAME value.
     * (In this case nameValue is null).
     *
     * @return
     */
    private String computeNameValue() {
        return computeNameValue(
                getNamePrefix(true),
                getFirstName(),
                getSurnamePrefix(),
                getLastName(),
                getSuffix());

    }
    /**
     * Compute and update a new TAG NAME value according to existing sub tag properties.
     */
    public void fixNameValue(){
        nameTagValue = computeNameValue();
    }
    
    /*
     * Build NAME value as "NAME_PIECE_PREFIX NAME_PIECE_GIVEN1 NAME_PIECE_GIVEN2 /NAME_PIECE_SURNAME_PREFIX NAME_PIECE_SURNAME/ NAME_PIECE_SUFFIX"
    */
    private String computeNameValue(String npfx, String first, String spfx, String last, String nsfx) {

        // Prepare string with standard filler (a space)
        WordBuffer wb = new WordBuffer();

        // Add NAME_PIECE_PREFIX and NAME_PIECE_GIVEN without any lasting comma
        first = first.replaceAll(",$", ""); // remove first name's ending comma if any
        wb.append(npfx).append(first);

        // Build surname parts : NAME_PIECE_SURNAME_PREFIX NAME_PIECE_SURNAME
        WordBuffer wpname = new WordBuffer();
        wpname.append(spfx).append(last).setFiller("");
        String name = wpname.toString();
        
        // Add surname part between slashes
        // 20050328 need last name //'s if there's a suffix
        if (name.length() > 0 || nsfx.length() > 0) {
            wb.append(" /" + name + "/");
        }
        
        // Add NAME_PIECE_SUFFIX
        wb.append(nsfx);

        return wb.toString();
    }

    /**
     * a value for display
     */
    @Override
    public String getDisplayValue() {

        if (isSecret()) {
            return "";
        }

        // n/a
        if (nameAsString != null) {
            return nameAsString;
        }

        WordBuffer b = new WordBuffer();
        String last = getLastName().split(",")[0]; // Only first last name if many
        if (last.length() == 0) {
            last = "?";
        }
        // remove trailing comma if any
        String first = getFirstName().replaceAll(", *$", "");

        if (GedcomOptions.getInstance().getNameFormat() == GedcomOptions.NameFormat.LAST) {

            b.append(getSurnamePrefix())
            .append(last)
            .append(getSuffix())
            .setFiller(", ")
            .append(first);

        } else {
            b.append(first)
                .append(getSurnamePrefix())
                .append(last);
        }

        return b.toString();
    }

    @Override
    Property.PropertyFormatter formatImpl(char marker) {
        if (marker == 'g') {
            return new PropertyFormatter(this, getGivenName());
        }
        return super.formatImpl(marker);
    }

    /**
     * Sets name to a new value
     */
    public PropertyName setName(String setLast) {
        return setName(firstName, setLast, suffix);
    }

    /**
     * Sets name to a new value
     */
    public final PropertyName setName(String setFirst, String setLast) {
        return setName(setFirst, setLast, suffix);
    }

    /**
     * Sets name to a new value
     */
    public PropertyName setName(String setFirst, String setLast, String setSuffix) {
        return setName(getPropertyValue("NPFX"), setFirst, getPropertyValue("SPFX"), setLast, setSuffix);
    }

    public PropertyName setName(String nPfx, String first, String sPfx, String last, String suff) {

        // 20070128 don't bother with calculating old if this is happening in init()
        boolean hasParent = getParent() != null;
        String old = hasParent ? getValue() : null;

        // check for uppercase lastname
        if (GedcomOptions.getInstance().isUpperCaseNames()) {
            NameParser parser = new NameParser(last);
            last = parser.getPrefix() + parser.getLast().toUpperCase();
        }

        // TUNING We expect that a lot of first and last names are the same
        // so we pay the upfront cost of reusing an intern cached String to
        // save overall memory
        //boolean rswc = GedcomOptions.getInstance().replaceSpaceSeparatorWithComma();
        // compute gedcom values from user display or input valus
        first = gedcomFromValue(first);
//        last = normalizeName(last);
        nPfx = gedcomFromValue(nPfx);
//        sPfx = gedcomFromValue(sPfx);
//        suff = gedcomFromValue(suff);

        // remember us
        remember(first, last);

        try {
            boolean isMuted = mutePropertyChange();
            // update GIVN|SURN - IF we have a parent
            if (hasParent && !isMuted) {
                addNameSubProperty(true, "GIVN", first);
                addNameSubProperty(true, "SURN", last);
                addNameSubProperty(true, "NSFX", suff);
                addNameSubProperty(true, "SPFX", sPfx);
                addNameSubProperty(true, "NPFX", nPfx);
            }

            // Make sure no Information is kept in base class
            nameAsString = null;
            lastName = last;
            firstName = first;
            suffix = suff;

            // tell about it
            if (old != null && !isMuted) {
                propagatePropertyChanged(this, old);
            }
        } finally {
            // Done
            unmutePropertyChange();
        }
        fixNameValue();
        return this;
    }

    // TODO: convert to a static function?
    public PropertyName replaceAllLastNames(String from){
        String to = getLastName();
        // change value of all with value
        Property[] others = getSameLastNames(from);
        for (Property other : others) {
            if (other instanceof PropertyName && other != this) {
                ((PropertyName) other).setName(to);
            }
        }
        return this;
    }

    /**
     * Add or update a subproperty to a name tag
     *
     * @param force if false, don't update an existing property but adds one if none exists.
     * if true, update an existing property or create one.
     * If a new property is created, the guessed attribute is set to true
     * @param tag   the TAG
     * @param value property's value. If empty no property is added and the previous is deleted
     * @return the property created or null if this property has been deleted
     */
    private String addNameSubProperty(boolean force, String tag, String value) {
        Property sub = getProperty(tag);
        String oldValue = (sub != null) ? sub.getValue() : "";

        if (sub == null) {
            if (!value.isEmpty()){
                sub = addProperty(tag, value);
                sub.setGuessed(!force);
                return sub.getValue();
            }
            return "";
        } else if (force) {
            if (value == null || value.isEmpty()) {
                if (!oldValue.isEmpty()) {
                    delProperty(sub);
                    return "";
                }
            } else {
                if (!value.equals(oldValue)) {
                    sub.setValue(value);
                }
                sub.setGuessed(false);
            }
        }
        return sub.getValue();
    }

    /**
     * Convert a gedcom sequence of names pieces separated by commas to an editable value for future editing.
     * <ul>
     *   <li/>Rule is to remove all commas only if all spaces include commas (this means we have a string made only of single words name pieces)
     *   <li/>If we have space without commas, this space is part of a name piece made of several words, because in gedcom format, name pieces should be separated by commas.
     *   Therefore do not modify commas in this case. 
     *   Rather add one at the end to remember that the space must remain as a space later, and not be turned into a comma.
     * </ul>
     *  
     * <ul>
     *   <li/>If all spaces include a comma, remove all comas with replace '*, *' by ' '. We'll assume all name pieces are made of single words
     *   <li/>Else, at least one space does not include a comma. It is part of a several-word-name piece. Leave it as is.
     *   <li/>In this case, if no comma is found, it means we have a string made of only one single name piece, made of several words. To remember this, we add a comma at the end.
     * </ul>
     * 
     * @param namePiece from user input (eb NameBean)
     * @return compliant gedcom value (with comma where applicable)
     */
    private static String gedcomToValue(String namePiece){
        if (!namePiece.matches(".*[^, ] +[^, ].*")) {
            return namePiece.replaceAll(" *, *", " ");
        }
        if (!namePiece.contains(",")) {
            namePiece = namePiece + ",";
        }
        return namePiece;
    }
    
    /**
     * Convert a namePiece from a user input to a gedcom compliant String value.
     * <ul>
     *   <li/>Rule is to store comma separated name pieces because it is the requirement of the gedocm format
     *   <li/>We must allow any one of the name pieces to be made of several words (name pieces can be separated by spaces)
     * </ul>
     * <ul>
     *   <li/>If namePiece does not contain commas, replace all space by ', ', it means we have only one single word name pieces
     *   <li/>Remove any space before a comma to trim spaces
     *   <li/>Remove trailing comma if any (comma used when editing a string of one name piece made of several words)
     * </ul>
     * 
     * @param namePiece from user input (eb NameBean)
     * @return compliant gedcom value (with comma where applicable)
     */
    private String gedcomFromValue(String namePiece){
        String np = namePiece;
        // no comma, replace ' ' by ', '
        if (np.indexOf(',') == -1){
            np = np.replaceAll(" +",", ");
        }
        np = np.replaceAll(" *, *",", ");
        // remove trailing comma
        np = np.replaceAll(", *$", "");
        return np;
    }

    /**
     * Hook:
     * + Remember names in reference set
     *
     * @see genj.gedcom.PropertyName#addNotify(genj.gedcom.Property)
     */
    /*package*/
    @Override
    void afterAddNotify() {
        // continue
        super.afterAddNotify();
        // our change to remember the last name
        remember(firstName, lastName);
        // done
    }

    /**
     * Callback:
     * + Forget names in reference set
     *
     * @see genj.gedcom.Property#delNotify()
     */
    /*package*/
    @Override
    void beforeDelNotify() {
        // forget value
        remember("", "");
        // continue
        super.beforeDelNotify();
        // done
    }

    /**
     * sets the name to a new gedcom value
     */
    @Override
    public void setValue(String newValue) {

        // remember tag value
        nameTagValue = newValue;

        // don't parse anything secret
        if (Enigma.isEncrypted(newValue)) {
            setName("", "", "");
            nameAsString = newValue;
            return;
        }

        // Parse NAME string:
        // [_firstName][/[last]/[suffix]]
        String _firstName = "";
        String _lastName ="";
        String _suffix = "";

        String[] parts = newValue.split("/",-1);
        _firstName = parts[0];
        if (parts.length == 3){
            _lastName = parts[1];
            _suffix=parts[2];
        } else if (parts.length != 1){
            // ... wrong format: must have 0 or 2 '/'
            // clears values and go on
            _firstName = "";
            _lastName = "";
            _suffix = "";
            nameAsString = newValue;
            //TODO: should we show a warning?
        }

        _firstName = stripPrefix(_firstName, getNamePrefix());

        _lastName = stripPrefix(_lastName, getSurnamePrefix());

        // remember us
        remember(_firstName, _lastName);
        try {
            boolean isMuted = mutePropertyChange();
            _firstName = addNameSubProperty(false, "GIVN", gedcomFromValue(_firstName));
            _lastName = addNameSubProperty(false, "SURN", _lastName);
            _suffix = addNameSubProperty(false, "NSFX", _suffix);

            // Make sure no Information is kept in base class
            nameAsString = null;
            lastName = _lastName;
            firstName = _firstName;
            suffix = _suffix;

        } finally {
            // Done
            unmutePropertyChange();
        }


        nameTagValue = newValue;

        // done
    }

    /**
     * if value starts with prefix, returns value with prefix removed.
     * returns value otherwise. Comparison is case insensitive
     *
     * @param value
     * @param prefix
     *
     * @return
     */
    private static String stripPrefix(String value, String prefix) {
        if (value.toLowerCase().startsWith(prefix.toLowerCase())) {
            return value.substring(prefix.length()).trim();
        }
        return value;
    }

    /**
     * refresh name structure from name value and all subtags
     */
    private void refresh(Property property) {
        // 2021-05-12 FL : only refresh if property is a name element of NAME
        String propagateTag = property.getTag();
        String[] nameTags = new String[]{"NPFX", "GIVN", "SPFX", "SURN", "NSFX"};
        boolean refresh = false;
        for (String tag : nameTags) {
            if (propagateTag.equals(tag)) {
                refresh = true;
                break;
            }
        }
        if (!refresh) {
            return;
        }
        
        // Refresh guessed and NAME without propagating this change
        try {
            if (!mutePropertyChange()) {
                for (String tag : nameTags) {
                    Property p = getProperty(tag);
                    if (p != null) {
                        p.setGuessed(false);
                    }
                }
                // FIXME: to be changed (gedcomfromvalue(gedcomtovalues(...))
                setName(
                        gedcomToValue(getPropertyValue("NPFX")),
                        gedcomToValue(getPropertyValue("GIVN")),
                        getPropertyValue("SPFX"),
                        getPropertyValue("SURN"),
                        getPropertyValue("NSFX"));
            }
        } finally {
            unmutePropertyChange();
        }
    }

    @Override
    void propagatePropertyAdded(Property property, int pos, Property added) {
        refresh(added);
        super.propagatePropertyAdded(property, pos, added);
    }

    @Override
    void propagatePropertyDeleted(Property property, int pos, Property deleted) {
        refresh(deleted);
        super.propagatePropertyDeleted(property, pos, deleted);
    }

    @Override
    void propagatePropertyChanged(Property property, String oldValue) {
        refresh(property);
        super.propagatePropertyChanged(property, oldValue);
    }

    /**
     * Return all last names
     * @param sortByName
     * @return 
     */
    public List<String> getLastNames(boolean sortByName) {
        Gedcom gedcom = getGedcom();
        if (gedcom == null) {
            return new ArrayList<>(0);
        }
        return getLastNames(gedcom, sortByName);
    }

    /**
     * Return all first names
     * @param sortByName
     * @return 
     */
    public List<String> getFirstNames(boolean sortByName) {
        Gedcom gedcom = getGedcom();
        if (gedcom == null) {
            return new ArrayList<>(0);
        }
        return getFirstNames(gedcom, sortByName);
    }

    /**
     * Return all nick names
     * @param sortByName
     * @return 
     */
    public List<String> getNickNames(boolean sortByName) {
        Gedcom gedcom = getGedcom();
        if (gedcom == null) {
            return new ArrayList<>(0);
        }
        return getNickNames(gedcom, sortByName);
    }

    /**
     * Return all last names
     * @param gedcom
     * @param sortByName
     * @return 
     */
    public static List<String> getLastNames(Gedcom gedcom, boolean sortByName) {
        return gedcom.getReferenceSet(KEY_LASTNAME).getKeys(sortByName ? gedcom.getCollator() : null);
    }

    /**
     * Return all first names
     * @param gedcom
     * @param sortByName
     * @return 
     */
    public static List<String> getFirstNames(Gedcom gedcom, boolean sortByName) {
        return gedcom.getReferenceSet(KEY_FIRSTNAME).getKeys(sortByName ? gedcom.getCollator() : null);
    }

    /**
     * Return all nick names
     * @param gedcom
     * @param sortByName
     * @return 
     */
    public static List<String> getNickNames(Gedcom gedcom, boolean sortByName) {
        return gedcom.getReferenceSet(KEY_NICKNAME).getKeys(sortByName ? gedcom.getCollator() : null);
    }

    /**
     * Returns all PropertyNames that contain the same name
     * @return 
     */
    public int getLastNameCount() {
        Gedcom gedcom = getGedcom();
        if (gedcom == null) {
            return 0;
        }
        return getLastNameCount(gedcom, getLastName());
    }

    /**
     * Returns all PropertyNames that contain the same name
     */
    public static int getLastNameCount(Gedcom gedcom, String last) {
        return gedcom.getReferenceSet(KEY_LASTNAME).getReferences(last).size();
    }

    /**
     * Returns all PropertyNames that contain the same name
     */
    public Property[] getSameLastNames() {
        return getSameLastNames(getLastName());
    }
    
    /**
     * Returns all PropertyNames that contain given lastname.
     * @param last
     * @return 
     */
    private Property[] getSameLastNames(String last) {
        return toArray(getGedcom().getReferenceSet(KEY_LASTNAME).getReferences(last));
    }

    /**
     * Remember a last and first names
     */
    private void remember(String newFirst, String newLast) {
        // got access to a reference set?
        Gedcom gedcom = getGedcom();
        if (gedcom == null) {
            return;
        }
        // forget old last and remember new
        ReferenceSet<String, Property> refSet = gedcom.getReferenceSet(KEY_LASTNAME);
        if (lastName.length() > 0) {
            refSet.remove(lastName, this);
        }
        if (newLast.length() > 0) {
            refSet.add(newLast, this);
        }
        // forget old first and remember new
        refSet = gedcom.getReferenceSet(KEY_FIRSTNAME);
        if (firstName.length() > 0) {
            refSet.remove(firstName, this);
        }
        if (newFirst.length() > 0) {
            String f = newFirst;
            refSet.add(f, this);
        }
        // done
    }

    /**
     * Helper class to parse last name and name
     */
    private static class NameParser {
        // de prefix may collide with italian or netherland conventions

        private static final Pattern PREFIX_PATTERN = Pattern.compile("(d\'|von der|von|zu|del|de las|de les|de los|de|las|la|os|das|da|dos|af|av)( +)(.*)");
        private String prefix = "";
        private String last = "";

        public NameParser(String last) {
            Matcher m = PREFIX_PATTERN.matcher(last);
            if (m.matches()) {
                this.prefix = m.group(1) + " ";
                this.last = m.group(3);
            } else {
                this.prefix = "";
                this.last = last;
            }
        }

        String getPrefix() {
            return prefix;
        }

        String getLast() {
            return last;
        }
    }

    private static class NAMEComparator extends PropertyComparator2.Default<PropertyName> {

        private static final NAMEComparator INSTANCE = new NAMEComparator();

        public static PropertyComparator2 getInstance() {
            return INSTANCE;
        }

        @Override
        public String getSortGroup(PropertyName p) {
            return shortcut(p.getDisplayValue().trim(), 1);
        }

        @Override
        public int compare(PropertyName p1, PropertyName p2) {

            int r = compareNull(p1, p2);
            if (r != Integer.MAX_VALUE) {
                return r;
            }
            Collator c = p1.getGedcom().getCollator();

            // check last name initially
            r = c.compare(p1.getDisplayValue(), p2.getDisplayValue());
            return r;
//            if (r != 0) {
//                return r;
//            }
//            // advance to first name
//            return c.compare(p1.getFirstName(), p2.getFirstName());
        }
    }
} //PropertyName

