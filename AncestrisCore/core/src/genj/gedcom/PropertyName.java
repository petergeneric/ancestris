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
    private final static String KEY_LASTNAME = "NAME.last",
            KEY_FIRSTNAME = "NAME.first";
    /** the first + last name */
    private String lastName = "",
            firstName = "",
            suffix = "";
    // XXX:nameValue should probably be replaced by nameAsString
    private String nameTagValue;
    /** the name if unparsable */
    private String nameAsString;
    private boolean mutePC = false;

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

    /**
     * Constructor
     */
    public PropertyName(String first, String last) {
        this();
        setName(first, last);
    }

    @Override
    public PropertyComparator2 getComparator() {
        return NAMEComparator.getInstance();
    }

    /**
     * the first name
     */
    public String getFirstName() {
        return getFirstName(false);
    }

    /**
     * the first name
     */
    public String getFirstName(boolean displayValue) {
        if (displayValue) {
            return firstName.replaceAll(" *, *", " ");
        } else {
            return firstName;
        }
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
    public boolean isValid() {
        /// no indi -> true
        if (!(getEntity() instanceof Indi || getEntity() instanceof Submitter)) {
            return true;
        }
        if (nameAsString != null) {
            return false;
        }
        if (nameTagValue == null) {
            return true;
        }
        // NAME is considered valid if NAME TAG value is equivalent to computed NAME TAG value from all subtags.
        // We do not consider an space character around / (for geneatique compatibility
        // We do consider the case of char (ie SURN may be UPPER where NAME is not)
        // XXX: We should consider the case where there is no sub tags in NAME structure
        return nameTagValue.replaceAll(" */ *", "/").replaceAll(" +", " ").equalsIgnoreCase(computeNameValue().replaceAll(" */ *", "/"));
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
        return getLastName(false);
    }

    /**
     * the last name
     */
    public String getLastName(boolean displayValue) {
        if (displayValue) {
            if (lastName.indexOf(',') < 0) {
                return lastName;
            } else {
                return lastName.substring(0, lastName.indexOf(','));
            }
        } else {
            return lastName;
        }
    }

    /**
     * @param prefixPresentation
     *
     * @return 'de Vries' in case of PREFIX_AS_IS.
     *         'Vries' in case of IGNORE_PREFIX.
     *         'Vries, de' in case of PREFIX_LAST.
     *
     * @deprecated use gedLastName()
     */
    @Deprecated
    public String getLastName(int prefixPresentation) {
        return getLastName();
    }

    /**
     * the suffix
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * nested nickname
     */
    public String getNick() {
        return getPropertyValue("NICK");
    }

    public String getSurnamePrefix() {
        return getPropertyValue("SPFX");
    }

    public String getNamePrefix() {
        return getPropertyValue("NPFX");
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
     * the name (e.g. "Meier, Nils")
     *
     * @deprecated use getDisplayValue instead
     */
    @Deprecated
    public String getName() {
        return getDisplayValue();
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

    /**
     * the Name Value computed by appending each name parts (given, surname, prefix, suffix).
     * This value is used when there is no conflict between those parts and the gedcom NAME value.
     * (In this case nameValue is null).
     *
     * @return
     */
    private String computeNameValue() {
        return computeNameValue(
                getNamePrefix(),
                getFirstName(true),
                getSurnamePrefix(),
                getLastName(true),
                suffix);

    }

    private String computeNameValue(String npfx, String first, String spfx, String last, String nsfx) {
        WordBuffer wb = new WordBuffer();

        if (!npfx.isEmpty()) {
            wb.append(npfx);
        }
        if (!first.isEmpty()) {
            wb.append(first);
        }

        String name = spfx;
        if (!name.isEmpty() && !last.isEmpty()) {
            name += " ";
        }
        name += last;
        // 20050328 need last name //'s if there's a suffix
        if (name.length() > 0 || nsfx.length() > 0) {
            wb.append("/" + name + "/");
        }
        if (nsfx.length() > 0) {
            wb.append(nsfx);
        }
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

        // if not valid, return name tag value
        if (!isValid() && nameTagValue != null) {
            return nameTagValue;
        }

        WordBuffer b = new WordBuffer();

        if (GedcomOptions.getInstance().getNameFormat() == GedcomOptions.NameFormat.LAST) {

            String last = getLastName(true);
            if (last.length() == 0) {
                last = "?";
            }
            b.append(getSurnamePrefix());
            b.append(last);
            b.append(getSuffix());
            b.setFiller(", ");
            b.append(getFirstName(true));

        } else {

            b.append(getFirstName(true));
            b.append(getSurnamePrefix());
            b.append(getLastName(true));

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
        return setName(setFirst, setLast, setSuffix, false);
    }

    /**
     * Sets name to a new value
     */
    public PropertyName setName(String first, String last, String suff, boolean replaceAllLastNames) {
        return setName(getPropertyValue("NPFX"), first, getPropertyValue("SPFX"), last, suff, replaceAllLastNames);
    }

    public PropertyName setName(
            String nPfx,
            String first,
            String sPfx,
            String last,
            String suff,
            boolean replaceAllLastNames) {

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
        // save overall memorey
        first = normalizeName(first, GedcomOptions.getInstance().spaceIsSeparator());
        last = normalizeName(last, false);
        suff = suff.trim();
        nPfx = nPfx.trim();
        sPfx = sPfx.trim();

        // replace all last names?
        if (replaceAllLastNames) {
            // change value of all with value
            Property[] others = getSameLastNames();
            for (Property other : others) {
                if (other instanceof PropertyName && other != this) {
                    ((PropertyName) other).setName(last);
                }
            }
        }

        // remember us
        remember(first, last);

        // update GIVN|SURN - IF we have a parent
        if (hasParent) {
            boolean add = GedcomOptions.getInstance().getAddNameSubtags();

            addNameSubProperty(add || !nPfx.isEmpty() || first.matches(".*[^,] .*"), "GIVN", first);
            addNameSubProperty(add || !sPfx.isEmpty() || last.contains(","), "SURN", last);
            addNameSubProperty(add, "NSFX", suff);
            addNameSubProperty(add || !nPfx.isEmpty(), "NPFX", nPfx);
            addNameSubProperty(add || !sPfx.isEmpty(), "SPFX", sPfx);
        }

        // Make sure no Information is kept in base class
        nameAsString = null;
        lastName = last;
        firstName = first;
        suffix = suff;
        // clear NAME tag value
        this.nameTagValue = null;

        // tell about it
        if (old != null) {
            propagatePropertyChanged(this, old);
        }

        // Done
        mutePC = false;
        return this;
    }

    /**
     * Add or update a subproperty to a name tag
     *
     * @param force if true, add a property if no sub property is present.
     *              Otherwise no property is added
     * @param tag   the TAG
     * @param value property's value. If null no property is added and the previous is deleted
     */
    private void addNameSubProperty(boolean force, String tag, String value) {
        Property sub = getProperty(tag);

        if (value.isEmpty()) {
            if (sub != null) {
                delProperty(sub);
            }
            return;
        }
        if (sub == null) {
            sub = addProperty(tag, value);
        } else {
            sub.setValue(value);
        }
        sub.setGuessed(!force);
        sub.setReadOnly(true);
    }

    private static String normalizeName(String namePiece, boolean spaceIsSeparator) {
        String result = namePiece.trim().replaceAll(" *, *", ",");
        if (spaceIsSeparator) {
            result = result.replaceAll(" +", ",");
        }
        return result.replaceAll(",", ", ");
    }

    /**
     * Hook:
     * + Remember last names in reference set
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
     * + Forget last names in reference set
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

        // Only name specified ?
        if (newValue.indexOf('/') < 0) {
            setName(newValue, "", "");
            return;
        }

        // Name AND First name
        String f = newValue.substring(0, newValue.indexOf('/')).trim();
        String l = newValue.substring(newValue.indexOf('/') + 1);

        // ... wrong format (2 x '/'s !)
        if (l.indexOf('/') == -1) {
            setName("", "", "");
            nameAsString = newValue;
            return;
        }

        // ... format ok
        String s = l.substring(l.indexOf('/') + 1);
        l = l.substring(0, l.indexOf('/'));

        f = f.replaceAll(",", " ");// remove commas
        f = f.replaceAll(" +", " ");// normalize
        // rewrite name TAG value (normalize)
        newValue = computeNameValue("", f, "", l, s);

        String npfx = getPropertyValue("NPFX");
        f = stripPrefix(f, npfx);

        String spfx = getPropertyValue("SPFX");
        l = stripPrefix(l, spfx);

        // Format GIVN Tag (' ' char replaced by ', ')
        f = f.replaceAll(" +", ", ");// Normalize

        // Replace first, last and suffix by tag values if present
        if (getProperty("SURN") != null && !getProperty("SURN").isGuessed()) {
            l = getPropertyValue("SURN");
        }
        if (getProperty("GIVN") != null && !getProperty("GIVN").isGuessed()) {
            f = getPropertyValue("GIVN");
        }
        if (getProperty("NSFX") != null && !getProperty("NSFX").isGuessed()) {
            s = getPropertyValue("NSFX");
        }
        // keep
        setName(getPropertyValue("NPFX"), f, getPropertyValue("SPFX"), l, s, false);
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
    private void refresh() {
        setName(getPropertyValue("NPFX"),
                getPropertyValue("GIVN"),
                getPropertyValue("SPFX"),
                getPropertyValue("SURN"),
                getPropertyValue("NSFX"),
                false);
    }

    @Override
    void propagatePropertyDeleted(Property container, int pos, Property deleted) {
//XXX:        setValue(getValue());
        refresh();
        super.propagatePropertyDeleted(container, pos, deleted);
    }

    @Override
    void propagatePropertyChanged(Property property, String oldValue) {
        if (!mutePC) {
            mutePC = true;
//XXX:            setValue(getValue());
            refresh();
        }
        super.propagatePropertyChanged(property, oldValue);
    }

    /**
     * Return all last names
     */
    public List<String> getLastNames(boolean sortByName) {
        Gedcom gedcom = getGedcom();
        if (gedcom == null) {
            return new ArrayList<String>(0);
        }
        return getLastNames(gedcom, sortByName);
    }

    /**
     * Return all first names
     */
    public List<String> getFirstNames(boolean sortByName) {
        Gedcom gedcom = getGedcom();
        if (gedcom == null) {
            return new ArrayList<String>(0);
        }
        return getFirstNames(gedcom, sortByName);
    }

    /**
     * Return all last names
     */
    public static List<String> getLastNames(Gedcom gedcom, boolean sortByName) {
        return gedcom.getReferenceSet(KEY_LASTNAME).getKeys(sortByName ? gedcom.getCollator() : null);
    }

    /**
     * Return all first names
     */
    public static List<String> getFirstNames(Gedcom gedcom, boolean sortByName) {
        return gedcom.getReferenceSet(KEY_FIRSTNAME).getKeys(sortByName ? gedcom.getCollator() : null);
    }

    /**
     * Returns all PropertyNames that contain the same name
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
        return toArray(getGedcom().getReferenceSet(KEY_LASTNAME).getReferences(getLastName()));
    }

    /**
     * Remember a last name
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
            refSet.add(newFirst, this);
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
            return shortcut(p.getLastName().trim(), 1);
        }

        @Override
        public int compare(PropertyName p1, PropertyName p2) {

            int r = compareNull(p1, p2);
            if (r != Integer.MAX_VALUE) {
                return r;
            }
            Collator c = p1.getGedcom().getCollator();

            // check last name initially
            r = c.compare(p1.getLastName(), p2.getLastName());
            if (r != 0) {
                return r;
            }
            // advance to first name
            return c.compare(p1.getFirstName(), p2.getFirstName());
        }
    }
} //PropertyName

