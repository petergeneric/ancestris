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

import genj.crypto.Enigma;
import genj.util.ReferenceSet;
import genj.util.WordBuffer;

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
    private final static String KEY_LASTNAME = "NAME.last",
            KEY_FIRSTNAME = "NAME.first";
    /** the first + last name */
    private String lastName = "",
            firstName = "",
            suffix = "";
    /** the name if unparsable */
    private String nameAsString;

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

    /**
     * @see java.lang.Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(Property other) {

        // check last name initially
        int result = compare(this.getLastName(), ((PropertyName) other).getLastName());
        if (result != 0) {
            return result;
        }

        // advance to first name
        return compare(this.getFirstName(), ((PropertyName) other).getFirstName());
    }

    /**
     * the first name
     */
    public String getFirstName() {
        return firstName;
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
        return nameAsString == null;
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
     * @param prefixPresentation
     * @return 'de Vries' in case of PREFIX_AS_IS.
     *         'Vries' in case of IGNORE_PREFIX.
     *         'Vries, de' in case of PREFIX_LAST.
     * @deprecated use gedLastName()
     */
    @Deprecated
    public String getLastName(int prefixPresentation) {
        return lastName;
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
            n = addProperty("NICK", nick);
        } else {
            n.setValue(nick);
        }
    }

    /**
     * the name (e.g. "Meier, Nils")
     */
    public String getName() {
        if (nameAsString != null) {
            return nameAsString;
        }
        if (firstName.length() == 0) {
            return lastName;
        }
        return lastName + ", " + firstName;
    }

    /**
     * the gedcom value
     */
    public String getValue() {

        if (nameAsString != null) {
            return nameAsString;
        }

        WordBuffer wb = new WordBuffer();
        wb.append(firstName);
        // 20050328 need last name //'s if there's a suffix
        if (lastName.length() > 0 || suffix.length() > 0) {
            wb.append("/" + lastName + "/");
        }
        if (suffix.length() > 0) {
            wb.append(suffix);
        }
        return wb.toString();
    }

    /**
     * a value for display
     */
    public String getDisplayValue() {

        if (isSecret()) {
            return "";
        }

        // n/a
        if (nameAsString != null) {
            return nameAsString;
        }

        WordBuffer b = new WordBuffer();

        if (Options.getInstance().nameFormat == 1) {

            String last = getLastName();
            if (last.length() == 0) {
                last = "?";
            }
            b.append(last);
            b.append(getSuffix());
            b.setFiller(", ");
            b.append(getFirstName());

        } else {

            b.append(getFirstName());
            b.append(getLastName());

        }

        return b.toString();
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

        boolean hasPfx = !nPfx.isEmpty() || !sPfx.isEmpty();

        // 20070128 don't bother with calculating old if this is happening in init()
        boolean hasParent = getParent() != null;
        String old = hasParent ? getValue() : null;

        // check for uppercase lastname
        if (Options.getInstance().isUpperCaseNames()) {
            NameParser parser = new NameParser(last);
            last = parser.getPrefix() + parser.getLast().toUpperCase();
        }

        // TUNING We expect that a lot of first and last names are the same
        // so we pay the upfront cost of reusing an intern cached String to
        // save overall memorey
        first = first.trim().intern();
        last = last.trim().intern();
        suff = suff.trim();

        // replace all last names?
        if (replaceAllLastNames) {
            // change value of all with value
            Property[] others = getSameLastNames();
            for (int i = 0; i < others.length; i++) {
                Property other = others[i];
                if (other instanceof PropertyName && other != this) {
                    ((PropertyName) other).setName(last);
                }
            }
        }

        // remember us
        remember(first, last);

        // update GIVN|SURN - IF we have a parent
        if (hasParent) {
            boolean add = Options.getInstance().isAddGivenSurname;
            addNameSubProperty(add || hasPfx, "GIVN", first);
            addNameSubProperty(add || hasPfx, "SURN", last);
            addNameSubProperty(add || hasPfx, "NSFX", suff);
            addNameSubProperty(add || hasPfx, "NPFX", nPfx);
            addNameSubProperty(add || hasPfx, "SPFX", sPfx);
        }

        // Make sure no Information is kept in base class
        nameAsString = null;
        lastName = last;
        firstName = first;
        suffix = suff;

        // tell about it
        if (old != null) {
            propagatePropertyChanged(this, old);
        }

        // Done
        return this;
    }

    /**
     * Add or update a subproperty to a name tag
     * @param force if true, add a property is no sub property is present.
     * Otherwise no property is added
     * @param tag the TAG
     * @param value value of si property. If null no property is added and the previous is deleted
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
     * @see genj.gedcom.Property#delNotify()
     */
    /*package*/ void beforeDelNotify() {
        // forget value
        remember("", "");
        // continue
        super.beforeDelNotify();
        // done
    }

    /**
     * sets the name to a new gedcom value
     */
    public void setValue(String newValue) {

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

        // If has prefix, then name is not easily parsable, so get sub tag values
        boolean hasPfx = ((getProperty("NPFX") != null) || (getProperty("SPFX") != null));
        if (hasPfx) {
            if (getProperty("SURN") != null) {
                l = getPropertyValue("SURN");
            }
            if (getProperty("GIVN") != null) {
                f = getPropertyValue("GIVN");
            }
            if (getProperty("NSFX") != null) {
                s = getPropertyValue("NSFX");
            }
        }

        // keep
        setName(getPropertyValue("NPFX"), f, getPropertyValue("SPFX"), l, s, false);

        // done
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

        private Pattern prefixPattern = Pattern.compile("(d\'|von der|von|zu|del|de las|de les|de los|de|las|la|os|das|da|dos|af|av)( +)(.*)");
        private String prefix = "";
        private String last = "";

        public NameParser(String last) {
            Matcher m = prefixPattern.matcher(last);
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
} //PropertyName

