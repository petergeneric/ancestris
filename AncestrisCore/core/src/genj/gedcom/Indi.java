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

import ancestris.core.TextOptions;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import genj.io.InputSource;
import genj.util.swing.ImageIcon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Class for encapsulating a person
 */
public class Indi extends Entity {

    private final static TagPath PATH_INDI = new TagPath("INDI"),
            PATH_INDIFAMS = new TagPath("INDI:FAMS"),
            PATH_INDIFAMC = new TagPath("INDI:FAMC"),
            PATH_INDIBIRTDATE = new TagPath("INDI:BIRT:DATE"),
            PATH_INDIDEATDATE = new TagPath("INDI:DEAT:DATE"),
            PATH_INDIBIRTPLACE = new TagPath("INDI:BIRT:PLAC"),
            PATH_INDIDEATPLACE = new TagPath("INDI:DEAT:PLAC"),
            PATH_INDIDEAT = new TagPath("INDI:DEAT");

    public final static ImageIcon IMG_MALE = Grammar.V55.getMeta(PATH_INDI).getImage("male"),
            IMG_FEMALE = Grammar.V55.getMeta(PATH_INDI).getImage("female"),
            IMG_UNKNOWN = Grammar.V55.getMeta(PATH_INDI).getImage();

    // Specific tags
    public static String TAG_SOSADABOVILLE = "_SOSADABOVILLE";
    public static String TAG_SOSA = "_SOSA";
    public static String TAG_DABOVILLE = "_DABOVILLE";
    
    private boolean nouveau = false;

    public Indi() {
        super(Gedcom.INDI, "?");
    }

    /**
     * need tag,id-arguments constructor for all entities
     */
    public Indi(String tag, String id) {
        super(tag, id);
        assertTag(Gedcom.INDI);
    }

    /**
     * Indi is valid if value is empty
     * @return boolean
     */
    @Override
    public boolean isValid() {
        return getValue().isEmpty();
    }
    
    @Override
    public void setNew() {
        nouveau = true;
    }
    
    @Override
    public void setOld() {
        nouveau = false;
    }
    
    /**
     * @return true if the entity is new, false if entity is old. 
     */
    public boolean isNew() {
        return nouveau;
    }
    
    @Override
    public void moveEntityValue() {
        if (!getValue().isEmpty()) {
            try {
                addProperty("NAME", getValue(), 0);
                setValue("");
            } catch (GedcomException ex) {
                super.moveEntityValue();
            }
            
        }
    }

    
    /**
     * @return a PropertyDate corresponding to the INDI:BIRT:DATE property.
     * Return null if the property is unset.
     *
     */
    public PropertyDate getBirthDate() {
        return getBirthDate(false);
    }

    /**
     * Calculate the INDI's Birthdate
     *
     * @param create if false, return null when the property is unset. If true,
     * return an empty PropertyDate when the property is unset
     *
     * @return date or null unless create
     */
    public PropertyDate getBirthDate(boolean create) {
        PropertyDate date = (PropertyDate) getProperty(PATH_INDIBIRTDATE);
        if (null != date || !create) {
            return date;
        }
        return (PropertyDate) setValue(PATH_INDIBIRTDATE, "");
    }

    /**
     * Calculate the INDI's Birthplace
     *
     * @return date or null
     */
    public PropertyPlace getBirthPlace() {
        return (PropertyPlace) getProperty(PATH_INDIBIRTPLACE);
    }

    /**
     * Calculate the death date of the Indi.
     *
     * @return a PropertyDate corresponding to the INDI:DEAT:DATE property.
     * Return null if the property is unset.
     */
    public PropertyDate getDeathDate() {
        return getDeathDate(false);
    }

    /**
     * Calculate indi's death date.
     *
     * @param create if false, return null when the property is unset. If true,
     * return an empty PropertyDate when the property is unset
     */
    public PropertyDate getDeathDate(boolean create) {
        PropertyDate date = (PropertyDate) getProperty(PATH_INDIDEATDATE);
        if (null != date || !create) {
            return date;
        }
        return (PropertyDate) setValue(PATH_INDIDEATDATE, "");
    }

    /**
     * Calculate the INDI's Deathplace
     *
     * @return date or null
     */
    public PropertyPlace getDeathPlace() {
        return (PropertyPlace) getProperty(PATH_INDIDEATPLACE);
    }

    /**
     * Get brothers including unknown or not
     *
     * @param includeUnknown
     * @return
     */
    public Indi[] getBrothers(boolean includeUnknown) {
        return getSiblings(PropertySex.MALE, includeUnknown);
    }

    /**
     * Get sisters including unknown or not
     *
     * @param includeUnknown
     * @return
     */
    public Indi[] getSisters(boolean includeUnknown) {
        return getSiblings(PropertySex.FEMALE, includeUnknown);
    }

    private Indi[] getSiblings(int sex, boolean includeUnknown) {
        List<Indi> l = new ArrayList<>();
        for (Indi i : getSiblings(false)) {
            int s = i.getSex();
            if (s == sex || (includeUnknown && s == PropertySex.UNKNOWN)) {
                l.add(i);
            }
        }
        Indi[] result = new Indi[l.size()];
        l.toArray(result);
        return result;
    }

    /**
     * Calculate all siblings (biological)
     */
    public Indi[] getSiblings(boolean includeMe) {

        // collect siblings
        Fam fam = getFamilyWhereBiologicalChild();
        if (fam == null) {
            return new Indi[0];
        }
        List<Indi> result = new ArrayList<>(fam.getNoOfChildren());
        Indi[] siblings = fam.getChildren();
        for (Indi sibling : siblings) {
            if (includeMe || sibling != this) {
                result.add(sibling);
            }
        }

        // done
        return toIndiArray(result);

    }

    /**
     * Calculate the 'younger' siblings - a list ordered by position in fam
     */
    public Indi[] getYoungerSiblings() {

        // grab 'em all
        Indi[] siblings = getSiblings(true);

        // sort by date
        Arrays.sort(siblings, new PropertyComparator("INDI:BIRT:DATE"));

        // grab everything up to me
        List<Indi> result = new ArrayList<>(siblings.length);
        for (int i = siblings.length - 1; i >= 0; i--) {
            if (siblings[i] == this) {
                break;
            }
            result.add(0, siblings[i]);
        }

        // done
        return toIndiArray(result);
    }

    /*
     * Get living address of en entity. It is the first address attached to
     * a RESI event without an end date
     */
    public PropertyMultilineValue getAddress() {

        // lookup RESIdences
        Property[] rs = getProperties("RESI", false);
        for (Property r : rs) {
            // there must be an address tag
            PropertyMultilineValue address = (PropertyMultilineValue) r.getProperty("ADDR");
            if (address == null) {
                continue;
            }
            // check if there's an ending date
            PropertyDate date = (PropertyDate) r.getProperty("DATE");
            if (date != null && date.isRange()) {
                continue;
            }
            // got it
            return address;
        }

        // not found
        return null;
    }

    /**
     * Calculate the 'older' sibling
     */
    public Indi[] getOlderSiblings() {

        // grab 'em all
        Indi[] siblings = getSiblings(true);

        // sort by date
        Arrays.sort(siblings, new PropertyComparator("INDI:BIRT:DATE"));

        // grab everything up older than me
        List<Indi> result = new ArrayList<>(siblings.length);
        for (int i = 0, j = siblings.length; i < j; i++) {
            if (siblings[i] == this) {
                break;
            }
            result.add(siblings[i]);
        }

        // done
        return toIndiArray(result);
    }

    /**
     * Determine if two persons are sibling in a family (biological or any)
     * @param indi2
     * @return 
     */
    public boolean isSiblingOf(Indi indi2, boolean biological) {
        
        Fam[] fams = new Fam[1];
        if (biological) {
            Fam fam = getFamilyWhereBiologicalChild();
            if (fam == null) {
                return false;
            }
            fams[0] = fam;
        } else {
            fams = getFamiliesWhereChild();
        }
        for (Fam fam : fams) {
            Indi[] children = fam.getChildren();
            for (Indi child : children) {
                if (child == indi2) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean isChildIn(Fam fam2) {
        for (Indi indi : fam2.getChildren()) {
            if (this == indi) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Calculate indi's partners. The number of partners can be smaller than the
     * number of families this individual is part of because spouses in families
     * don't have to be defined.
     */
    public Indi[] getPartners() {
        // Look at all families and remember spouses
        Fam[] fs = getFamiliesWhereSpouse();
        List<Indi> l = new ArrayList<>(fs.length);
        for (Fam f : fs) {
            Indi p = f.getOtherSpouse(this);
            if (p != null) {
                l.add(p);
            }
        }
        // Return result
        Indi[] result = new Indi[l.size()];
        l.toArray(result);
        return result;
    }

    public boolean isSpouseOf(Indi indi2) {
        Indi[] partners = getPartners();
        for (Indi partner : partners) {
            if (partner == indi2) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isSpouseIn(Fam fam2) {
        return (this == fam2.getHusband() || this == fam2.getWife());
    }
    
    /**
     * Calculate indi's parents. The number of partners can be smaller than the
     * number of families this individual is child in because spouses in
     * families don't have to be defined.
     */
    public List<Indi> getParents() {
        List<Indi> parents = new ArrayList<>(2);
        for (Fam fam : getFamiliesWhereChild()) {
            Indi husband = fam.getHusband();
            if (husband != null) {
                parents.add(husband);
            }
            Indi wife = fam.getWife();
            if (wife != null) {
                parents.add(wife);
            }
        }
        return parents;
    }

    /**
     * Calculate indi's children
     */
    public Indi[] getChildren() {
        // Look at all families and remember children
        Fam[] fs = getFamiliesWhereSpouse();
        List<Indi> l = new ArrayList<>(fs.length);
        for (Fam f : fs) {
            Indi[] cs = f.getChildren();
            for (Indi c : cs) {
                if (!l.contains(c)) {
                    l.add(c);
                }
            }
        }
        // Return result
        Indi[] result = new Indi[l.size()];
        l.toArray(result);
        return result;
    }

    /**
     * Calculate indi's birth date
     */
    public String getBirthAsString() {

        PropertyDate p = getBirthDate();
        if (p == null) {
            return "";
        }

        // Return string value
        return p.getDisplayValue();
    }

    /**
     * Calculate indi's death date
     */
    public String getDeathAsString() {

        PropertyDate p = getDeathDate();
        if (p == null) {
            return "";
        }

        // Return string value
        return p.getDisplayValue();
    }

    /**
     * Returns the families in which this individual is a spouse
     */
    public Fam[] getFamiliesWhereSpouse() {
        return getFamiliesWhereSpouse(true);
    }

    public Fam[] getFamiliesWhereSpouse(boolean sorted) {
        List<Fam> result = new ArrayList<>(getNoOfProperties());
        for (int i = 0, j = getNoOfProperties(); i < j; i++) {
            Property prop = getProperty(i);
            if ("FAMS".equals(prop.getTag()) && prop.isValid() && prop instanceof PropertyFamilySpouse) {
                result.add(((PropertyFamilySpouse) prop).getFamily());
            }
        }
        Fam[] fams = Fam.toFamArray(result);
        if (sorted) {
            Arrays.sort(fams, new PropertyComparator("FAM:MARR:DATE"));
        }
        return fams;
    }

    
    /**
     * Returns the preferred family for the individual : null if no family, first one when sorted if none marked as preferred
     */
    public Fam getPreferredFamily() {
        Fam preferredFam = null;
        Fam[] fams = getFamiliesWhereSpouse(true);
        if (fams.length > 0) {
            // Identify first preferred fam
            preferredFam = fams[0];
            for (Fam f : fams) {
                if (f.isPreferred()) {
                    preferredFam = f;
                    break;
                }
            }
        }
        return preferredFam;
    }
    
    /**
     * Returns the families in which the person is child (biological, foster,
     * etc.)
     */
    public Fam[] getFamiliesWhereChild() {

        List<PropertyFamilyChild> famcs = getProperties(PropertyFamilyChild.class);
        Set<Fam> result = new HashSet<>(famcs.size());
        for (int i = 0; i < famcs.size(); i++) {
            PropertyFamilyChild famc = famcs.get(i);
            if (famc.isValid()) {
                result.add((Fam) famc.getTargetEntity());
            }
        }

        return Fam.toFamArray(result);
    }

    /**
     * Returns the family in which the person is biological child
     *
     * @return reference to 1st family or family with 'PEDI birth'
     */
    public Fam getFamilyWhereBiologicalChild() {

        // look at all FAMCs
        Fam result = null;
        List<PropertyFamilyChild> famcs = getProperties(PropertyFamilyChild.class);
        for (int i = 0; i < famcs.size(); i++) {
            PropertyFamilyChild famc = famcs.get(i);
            // not valid - not interesting
            if (!famc.isValid()) {
                continue;
            }
            Boolean biological = famc.isBiological();
            // stop if confirmed (first) biological
            if (Boolean.TRUE.equals(biological)) {
                return (Fam) famc.getTargetEntity();
            }
            // keep if maybe biological and first
            if (biological == null && result == null) {
                result = (Fam) famc.getTargetEntity();
            }
        }

        // done
        return result;
    }

    /**
     * Returns indi's first name
     */
    public String getFirstName() {
        PropertyName p = (PropertyName) getProperty("NAME", false);
        return p != null ? p.getFirstName() : "";
    }

    /**
     * Calculate indi's first names
     */
    public String[] getFirstNames() {
        Property[] ps = getProperties("NAME", false);
        if (ps == null) {
            return null;
        }
        String[] firstNames = new String[ps.length];
        int i = 0;
        for (Property prop : ps) {
            firstNames[i++] = ((PropertyName) prop).getFirstName();
        }
        return firstNames;
    }

    /**
     * Calculate indi's last name
     */
    public String getLastName() {
        PropertyName p = (PropertyName) getProperty("NAME", false);
        return p != null ? p.getLastName() : "";
    }

    /**
     * Calculate indi's last names
     */
    public String[] getLastNames() {
        Property[] ps = getProperties("NAME", false);
        if (ps == null) {
            return null;
        }
        String[] lastNames = new String[ps.length];
        int i = 0;
        for (Property prop : ps) {
            lastNames[i++] = ((PropertyName) prop).getLastName();
        }
        return lastNames;
    }

    /**
     * Calculate indi's spouse last names
     */
    public String[] getPartnersLastNames() {
        
        Set<String> lastNames = new HashSet<>();
        for (Indi partner : getPartners()) {
            lastNames.addAll(Arrays.asList(partner.getLastNames()));
        }
        return lastNames.toArray(new String[lastNames.size()]);
    }

    /**
     * Calculate indi's spouse first names
     */
    public String[] getPartnersFirstNames() {
        
        Set<String> firstNames = new HashSet<>();
        for (Indi partner : getPartners()) {
            firstNames.addAll(Arrays.asList(partner.getFirstNames()));
        }
        return firstNames.toArray(new String[firstNames.size()]);
    }

    /**
     * Calculate indi's name suffix
     */
    public String getNameSuffix() {
        PropertyName p = (PropertyName) getProperty("NAME", true);
        return p != null ? p.getSuffix() : "";
    }

    /**
     * Sets indi's name
     */
    public void setName(String first, String last) {
        PropertyName p = (PropertyName) getProperty("NAME", true);
        if (p == null) {
            p = (PropertyName) addProperty(new PropertyName());
        }
        p.setName(first, last);
    }

    /**
     * Returns indi's name (e.g. "Meier, Nils")
     */
    public String getName() {
        PropertyName p = (PropertyName) getProperty("NAME", true);
        if (p == null) {
            return "";
        }
        return p.getDisplayValue();
    }

    /**
     * Returns indi's name property.
     *
     * @return The propertyName.
     */
    public PropertyName getNameProperty() {
        PropertyName p = (PropertyName) getProperty("NAME", true);
        if (p == null) {
            return null;
        }
        return p;
    }

    /**
     * Returns the number of families in which the individual is a partner
     */
    public int getNoOfFams() {
        int result = 0;
        for (int i = 0, j = getNoOfProperties(); i < j; i++) {
            Property prop = getProperty(i);
            if ("FAMS".equals(prop.getTag()) && prop.isValid()) {
                result++;
            }
        }
        return result;
    }

    /**
     * Returns indi's sex
     */
    public int getSex() {
        PropertySex p = (PropertySex) getProperty("SEX", true);
        return p != null ? p.getSex() : PropertySex.UNKNOWN;
    }

    /**
     * Set indi's sex
     *
     * @param sex one of PropertySex.MALE or PropertySex.FEMALE
     */
    public void setSex(int sex) {
        // check whatever sex is there
        PropertySex p = (PropertySex) getProperty("SEX", false);
        // don't change what's wrong
        if (p != null && !p.isValid()) {
            return;
        }
        // add it if necessary
        if (p == null) {
            p = (PropertySex) addProperty(new PropertySex());
        }
        // change
        p.setSex(sex);
    }

    /**
     * Check wether this person is descendant of given person
     */
    public boolean isDescendantOf(Indi indi) {
        return indi.isAncestorOf(this);
    }

    /**
     * Check wether this person is ancestor of given person
     */
    public boolean isAncestorOf(Indi indi) {
        // 20070115 while we make sure that no circle exists in our gedcom data (invariants) there are cases where sub-trees of a tree occur multiple times
        // (e.g. cousin marrying cousin, ancestor marrying descendant, cloned families pointing to identical ancestors, ...)
        // So we're carrying a set of visited indis to abbreviate the ancestor check by looking for revisits.
        return recursiveIsAncestorOf(indi, new HashSet<>());
    }

    private boolean recursiveIsAncestorOf(Indi indi, Set<Indi> visited) {

        // if we've visited the individual already then there's obviously no need to check twice 
        if (visited.contains(indi)) {
            return false;
        }
        visited.add(indi);

        // check all possible of indi's parents
        List<PropertyFamilyChild> famcs = indi.getProperties(PropertyFamilyChild.class);
        for (int i = 0; i < famcs.size(); i++) {

            PropertyFamilyChild famc = famcs.get(i);

            // not valid or not biological- not interesting
            if (!famc.isValid() || Boolean.FALSE.equals(famc.isBiological())) {
                continue;
            }

            Fam fam = famc.getFamily();

            // check his mom/dad
            Indi father = fam.getHusband();
            if (father != null) {
                if (father == this) {
                    return true;
                }
                if (recursiveIsAncestorOf(father, visited)) {
                    return true;
                }
            }
            Indi mother = fam.getWife();
            if (mother != null) {
                if (mother == this) {
                    return true;
                }
                if (recursiveIsAncestorOf(mother, visited)) {
                    return true;
                }
            }

        }

        // nope
        return false;

    }

    public List<List<Indi>> getAncestorLinesWith(Indi indi) {
        List<List<Indi>> lines = new ArrayList<>();
        recursiveLinesToAncestorFrom(indi, lines, new ArrayList<>());
        return lines;
    }
    
    private boolean recursiveLinesToAncestorFrom(Indi indi, List<List<Indi>> lines, List<Indi> currentLine) {

        // There is one recursive loop per line.
        // If we've visited the individual already in this line, then there's obviously no need to check twice 
        if (currentLine.contains(indi)) {
            return false;
        }
        currentLine.add(indi);

        // check all possible of indi's parents
        List<PropertyFamilyChild> famcs = indi.getProperties(PropertyFamilyChild.class);
        for (int i = 0; i < famcs.size(); i++) {

            PropertyFamilyChild famc = famcs.get(i);

            // not valid or not biological- not interesting
            if (!famc.isValid() || Boolean.FALSE.equals(famc.isBiological())) {
                continue;
            }

            Fam fam = famc.getFamily();

            // check his mom/dad
            Indi father = fam.getHusband();
            if (father != null) {
                // start a new line
                List<Indi> fatherLine = new ArrayList<>();
                fatherLine.addAll(currentLine);
                if (father == this) {
                    fatherLine.add(father);
                    lines.add(fatherLine);
                    return true;
                }
                recursiveLinesToAncestorFrom(father, lines, fatherLine);
            }
            Indi mother = fam.getWife();
            if (mother != null) {
                // start a new line
                List<Indi> motherLine = new ArrayList<>();
                motherLine.addAll(currentLine);
                if (mother == this) {
                    motherLine.add(mother);
                    lines.add(motherLine);
                    return true;
                }
                recursiveLinesToAncestorFrom(mother, lines, motherLine);
            }

        }

        // nope
        return false;

    }
    
    
    
    /**
     * Check wether this person is descendant of given family
     */
    public boolean isDescendantOf(Fam fam) {

        // check the family's children
        // NM 20070128 don't sort for existance check only - that's expensive
        Indi[] children = fam.getChildren(false);
        for (Indi child : children) {
            if (child == this) {
                return true;
            }
        }

        // nope
        return false;
    }

    /**
     * Check wether this person is ancestor of given family
     */
    public boolean isAncestorOf(Fam fam) {

        // Husband ?
        Indi husband = fam.getHusband();
        if (husband != null) {
            if (husband == this) {
                return true;
            }
            if (isAncestorOf(husband)) {
                return true;
            }
        }

        // Wife ?
        Indi wife = fam.getWife();
        if (wife != null) {
            if (wife == this) {
                return true;
            }
            if (isAncestorOf(wife)) {
                return true;
            }
        }

        // nope
        return false;
    }

    /**
     * Name ...
     */
    @Override
    protected String getToStringPrefix(boolean showIds) {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(" (");
        // Birth management
        String birth = getBirthAsString();
        final String chr = getCHRAsString();
        if ("".equals(birth) && TextOptions.getInstance().isUseChr() && !"".equals(chr)) {
            sb.append(TextOptions.getInstance().getBaptismSymbol());
            birth = chr;
        } else {
            sb.append(TextOptions.getInstance().getBirthSymbol());
        }
        sb.append(birth);
        sb.append(' ');
        
        // Death management
        String death = getDeathAsString();
        final String burial = getBuriAsString();
        if ("".equals(death) && TextOptions.getInstance().isUseBuri() && !"".equals(burial)) {
            sb.append(TextOptions.getInstance().getBurialSymbol());
            death = burial;
        } else {
            sb.append(TextOptions.getInstance().getDeathSymbol());
        }
        sb.append(death);
        sb.append(')');
        return sb.toString();
    }

    public String getCHRAsString() {
        PropertyDate p = (PropertyDate) getProperty(new TagPath("INDI:CHR:DATE"));
        if (p == null) {
            return "";
        }

        // Return string value
        return p.getDisplayValue();
    }
    
     /**
     * Calculate indi's buri date
     */
    public String getBuriAsString() {
        PropertyDate p = (PropertyDate) getProperty(new TagPath("INDI:BURI:DATE"));
        if (p == null) {
            return "";
        }

        // Return string value
        return p.getDisplayValue();
    }

    /**
     * Returns a user-readable indi title
     *
     * @return
     */
    @Override
    public String getDisplayTitle() {
        return getDisplayTitle(true);
    }

    public String getDisplayTitle(boolean showid) {
        PropertyName p = getNameProperty();
        String spfx = (p != null) ? p.getSurnamePrefix() + " " : "";
        String lastNames[] = getLastName().split(",");
        String firstNames[] = getFirstName().split(",");
        String lastname = lastNames.length > 0 ? spfx + lastNames[0] : "?";
        String firstname = firstNames.length > 0 ? firstNames[0] : "?";

        String birthDate = getBirthAsString();
        if ("".equals(birthDate) && TextOptions.getInstance().isUseChr() && !"".equals(getCHRAsString())) {
            birthDate = TextOptions.getInstance().getBaptismSymbol() + getCHRAsString();
        }

        return (showid ? getId() + " - " : "") + firstname + " " + lastname + " (" + birthDate + " - " + getDeathAsString() + ")";
    }

    /**
     * list of indis to array
     */
    /*package*/ static Indi[] toIndiArray(Collection<Indi> c) {
        return c.toArray(new Indi[c.size()]);
    }

    /**
     * Image
     */
    @Override
    public ImageIcon getImage(boolean checkValid) {
        // check sex (no need to check valid here)
        switch (getSex()) {
            case PropertySex.MALE:
                return IMG_MALE;
            case PropertySex.FEMALE:
                return IMG_FEMALE;
            default:
                return IMG_UNKNOWN;
        }
    }

    /**
     * Calculate indi's age at given point in time
     */
    public String getAgeString(PointInTime pit) {
        Delta delta = getAge(pit);
        return delta != null ? delta.toString() : "";
    }

    /**
     * Calculate indi's age at given point in time or null if an error occurred
     */
    public Delta getAge(PointInTime pit) {
        return PropertyAge.getAge(this, pit);
    }

    public PointInTime getStartPITOfAge() {
        return PropertyAge.getStartPITOfAge(this);
    }

    public List<PropertyEventDetails> getEvents() {

        List<PropertyEventDetails> eventList = new ArrayList<>();
        
        getAllProperties(null).stream().filter((prop) -> (prop.isEvent())).forEachOrdered((prop) -> {
            eventList.add((PropertyEventDetails)prop);
        });
        
        for (PropertyFamilySpouse fam : getProperties(PropertyFamilySpouse.class)) {
            Fam targetFam = fam.getFamily();
            if (targetFam == null) {
                continue;
            }
            targetFam.getAllProperties(null).stream().filter((prop) -> (prop.isEvent())).forEachOrdered((prop) -> {
                eventList.add((PropertyEventDetails)prop);
            });
        }
        return eventList;
    }

    /**
     * Calculate indi's father
     */
    public Indi getBiologicalFather() {
        Fam f = getFamilyWhereBiologicalChild();
        return f != null ? f.getHusband() : null;
    }

    /**
     * Calculate indi's mother
     */
    public Indi getBiologicalMother() {
        Fam f = getFamilyWhereBiologicalChild();
        return f != null ? f.getWife() : null;
    }

    /**
     * Checks whether this person is deceased
     */
    public boolean isDeceased() {
        // check death event
        PropertyEvent deat = (PropertyEvent) getProperty("DEAT");
        if (deat != null) {
            // known to have happened?
            if (deat.isKnownToHaveHappened()) {
                return true;
            }
            // valid date?
            Property date = deat.getProperty("DATE");
            if (date != null && date.isValid()) {
                return true;
            }
        }
        // TODO: we should have a configurable value for max age 

        // born more than 100 years ago?
        PropertyDate birt = getBirthDate();
        if (birt != null) {
            Delta delta = birt.getAnniversary();
            if (delta != null && delta.getYears() > 100) {
                return true;
            }
        }
        // not afaik
        return false;
    }

    /**
     * Get first media file found for the indi
     */
    public InputSource getMediaFile() {
        Property obje = getProperty("OBJE");
        if (obje != null) {
            if (obje instanceof PropertyMedia) {
                PropertyMedia pm = (PropertyMedia) obje;
                Media media = (Media) pm.getTargetEntity();
                if (media != null) {
                    return media.getFile();
                }
            } else {
                PropertyFile file = (PropertyFile) obje.getProperty("FILE");
                if (file != null) {
                    Optional<InputSource> ois = file.getInput();
                    if (ois.isPresent()) {
                        return ois.get();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get sosa number starting from SOSADABOVILLE, SOSA, DABOVILLE
     *
     * @param validOnly
     * @return
     */
    public Property getSosa(boolean validOnly) {
        Property p = getProperty(Indi.TAG_SOSADABOVILLE, validOnly);
        if (p != null) {
            return p;
        }
        p = getProperty(Indi.TAG_SOSA, validOnly);
        if (p != null) {
            return p;
        }
        p = getProperty(Indi.TAG_DABOVILLE, validOnly);
        if (p != null) {
            return p;
        }
        return null;
    }

    public String getSosaString() {
        Property p = getSosa(true);
        if (p != null) {
            return p.getDisplayValue();
        }
        return "";
    }

    @Override
    public PropertyComparator2 getDisplayComparator() {
        return INDIComparator.getInstance();
    }

    private static class INDIComparator extends PropertyComparator2.Default<Indi> {

        private static final PropertyComparator2 INSTANCE = new INDIComparator();

        public static PropertyComparator2 getInstance() {
            return INSTANCE;
        }

        @Override
        public int compare(Indi i1, Indi i2) {
            int r = compareNull(i1, i2);
            if (r == Integer.MAX_VALUE) {
                Property n1 = i1.getProperty("NAME", false);
                Property n2 = i2.getProperty("NAME", false);
                r = compareNull(n1, n2);
                if (r == Integer.MAX_VALUE) {
                    r = n1.getDisplayComparator().compare(n1, n2);
                }
            }
            return r;
        }

        @Override
        public String getSortGroup(Indi p) {
            PropertyName name = (PropertyName) p.getProperty("NAME", false);
            if (name == null) {
                return "";
            }
            return shortcut(name.getDisplayValue(), 1);

        }

    }
} //Indi
