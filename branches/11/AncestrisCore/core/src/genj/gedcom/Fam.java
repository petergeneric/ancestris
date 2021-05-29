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

import ancestris.core.TextOptions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.openide.util.Exceptions;

/**
 * Class for encapsulating a family with parents and children
 */
public class Fam extends Entity {

    public final static TagPath PATH_FAMMARRDATE = new TagPath("FAM:MARR:DATE"),
            PATH_FAMMARRPLAC = new TagPath("FAM:MARR:PLAC"),
            PATH_FAMDIVDATE = new TagPath("FAM:DIV:DATE"),
            PATH_FAMDIVPLAC = new TagPath("FAM:DIV:PLAC");

    private final static TagPath SORT_SIBLINGS = new TagPath("CHIL:*:..:BIRT:DATE");

    
    public final static String TAG_PREF = "_PREF";


    /** comparator for CHIL nodes - by child's birth date and position if necessary */
    private class CHILComparator extends PropertyComparator {

        CHILComparator() {
            super(SORT_SIBLINGS);
        }

        @Override
        public int compare(Property p1, Property p2) {
            int result = super.compare(p1, p2);
            return result != 0 ? result : getPropertyPosition(p1) - getPropertyPosition(p2);
        }
    };

    /**
     * Comparator for family by husband name the wife name
     */
    @Override
    public PropertyComparator2 getDisplayComparator() {
        return FAMComparator.getInstance();
    }

    private static class FAMComparator extends PropertyComparator2.Default<Fam> {

        private static final FAMComparator INSTANCE = new FAMComparator();

        public static PropertyComparator2 getInstance() {
            return INSTANCE;
        }

        /**
         * Fam SortGroup is Husband SortGroup or "".
         *
         * @param p
         *
         * @return
         */
        @Override
        public String getSortGroup(Fam p) {
            Indi h = p.getHusband();
            if (h == null) {
                return "";
            } else {
                return h.getDisplayComparator().getSortGroup(h);
            }
        }

        /**
         * compare by husband then by wife
         *
         * @param f1
         * @param f2
         *
         * @return
         */
        @Override
        public int compare(Fam f1, Fam f2) {
            int r = compareNull(f1, f2);
            if (r != Integer.MAX_VALUE) {
                return r;
            }
            // compare husbands
            Indi i1 = f1.getHusband();
            Indi i2 = f2.getHusband();
            r = compareNull(i1, i2);
            if (r == Integer.MAX_VALUE) {
                r = i1.getDisplayComparator().compare(i1, i2);
            }
            if (r != 0) {
                return r;
            }

            // compare wives
            i1 = f1.getWife();
            i2 = f2.getWife();
            r = compareNull(i1, i2);
            if (r == Integer.MAX_VALUE) {
                r = i1.getDisplayComparator().compare(i1, i2);
            }
            return r;
        }

    }

    /**
     * need tag,id-arguments constructor for all entities
     */
    public Fam(String tag, String id) {
        super(tag, id);
        assertTag(Gedcom.FAM);
    }

    
    /**
     * Family is valid if value is empty
     * @return boolean
     */
    @Override
    public boolean isValid() {
        return getValue().isEmpty();
    }
    
    @Override
    public void moveEntityValue() {
        super.moveEntityValue();
    }

    
    
    
    /**
     * Returns child #i
     */
    public Indi getChild(int which) {

        for (int i = 0, j = getNoOfProperties(); i < j; i++) {
            Property prop = getProperty(i);
            if ("CHIL".equals(prop.getTag()) && prop.isValid()) {
                if (which == 0) {
                    return ((PropertyChild) prop).getChild();
                }
                which--;
            }
        }

        throw new IllegalArgumentException("no such child");
    }

    /**
     * Returns children
     */
    public Indi[] getChildren() {
        return getChildren(true);
    }

    /**
     * Returns children
     */
    public Indi[] getChildren(boolean sorted) {

        // look for all valid CHIL
        List<PropertyChild> CHILs = new ArrayList<PropertyChild>(getNoOfProperties());
        for (PropertyChild prop : getProperties(PropertyChild.class)) {
            if (prop.isValid()) {
                CHILs.add(prop);
                // we don't sort children if there is one or many without a proper date
                // will have to depend on the natural order of the CHIL tags then
                // FL: 2017-05-26 - let sort happen, even if some children do not have a birth date.
//                if (sorted) {
//                    Property sortby = prop.getProperty(SORT_SIBLINGS);
//                    if (sortby == null || !sortby.isValid()) {
//                        sorted = false;
//                    }
//                }
            }
        }

        // convert to array & sort
        if (sorted) {
            Collections.sort(CHILs, new CHILComparator());
        }

        // grab children now
        List<Indi> children = new ArrayList<Indi>(CHILs.size());
        for (int i = 0; i < CHILs.size(); i++) {
            Indi child = (CHILs.get(i)).getChild();
            if (!children.contains(child)) {
                children.add(child);
            }
        }

        // done
        return Indi.toIndiArray(children);
    }

    /**
     * Returns the husband of the family
     */
    public Indi getHusband() {
        Property husb = getProperty("HUSB", true);
        if (husb instanceof PropertyHusband) {
            return ((PropertyHusband) husb).getHusband();
        }
        return null;
    }

    /**
     * The number of children
     */
    public int getNoOfChildren() {
        int result = 0;
        for (int i = 0, j = getNoOfProperties(); i < j; i++) {
            Property prop = getProperty(i);
            if (prop.getClass() == PropertyChild.class && prop.isValid()) {
                result++;
            }
        }
        return result;
    }

    /**
     * The number of spouses
     */
    public int getNoOfSpouses() {
        int result = 0;
        if (getHusband() != null) {
            result++;
        }
        if (getWife() != null) {
            result++;
        }
        return result;
    }

    /**
     * Spouse by index
     */
    public Indi getSpouse(int which) {
        Indi husband = getHusband();
        if (husband != null) {
            if (which == 0) {
                return husband;
            }
            which--;
        }
        Indi wife = getWife();
        if (wife != null) {
            if (which == 0) {
                return wife;
            }
            which--;
        }
        throw new IllegalArgumentException("No such spouse");
    }

    /**
     * convenient spouses as list
     */
    public List<Indi> getSpouses() {
        List<Indi> spouses = new ArrayList<Indi>();
        if (getHusband() != null) {
            spouses.add(getHusband());
        }
        if (getWife() != null) {
            spouses.add(getWife());
        }
        return spouses;
    }

    /**
     * Returns the other parent to the given one
     */
    public Indi getOtherSpouse(Indi spouse) {
        Indi wife = getWife();
        if (wife == spouse) {
            return getHusband();
        }
        return wife;
    }

    /**
     * Returns the wife of the family
     */
    public Indi getWife() {

        Property wife = getProperty("WIFE", true);
        if (wife instanceof PropertyWife) {
            return ((PropertyWife) wife).getWife();
        }
        return null;
    }

    /**
     * Sets the husband of this family
     */
    public PropertyXRef setHusband(Indi husband) throws GedcomException {

        // Remove old husband (first valid one would be the one)
        for (int i = 0, j = getNoOfProperties(); i < j; i++) {
            Property prop = getProperty(i);
            if ("HUSB".equals(prop.getTag()) && prop.isValid()) {
                delProperty(prop);
                break;
            }
        }

        // done?
        if (husband == null) {
            return null;
        }

        // Add new husband
        PropertyHusband ph = new PropertyHusband();
        ph.setValue(husband.getId());
        addProperty(ph);

        // Link !
        try {
            ph.link();
        } catch (GedcomException ex) {
            delProperty(ph);
            throw ex;
        }

        // check sex of husband
        if (husband.getSex() != PropertySex.MALE) {
            husband.setSex(PropertySex.MALE);
        }

        // done    
        return ph;
    }

    /**
     * Sets the wife of the family
     */
    public PropertyXRef setWife(Indi wife) throws GedcomException {

        // Remove old wife (first valid one would be the one)
        for (int i = 0, j = getNoOfProperties(); i < j; i++) {
            Property prop = getProperty(i);
            if ("WIFE".equals(prop.getTag()) && prop.isValid()) {
                delProperty(prop);
                break;
            }
        }

        // done?
        if (wife == null) {
            return null;
        }

        // Add new wife
        PropertyWife pw = new PropertyWife();
        pw.setValue(wife.getId());
        addProperty(pw);

        // Link !
        try {
            pw.link();
        } catch (GedcomException ex) {
            delProperty(pw);
            throw ex;
        }

        // check sex of wife
        if (wife.getSex() != PropertySex.FEMALE) {
            wife.setSex(PropertySex.FEMALE);
        }

        // Done
        return pw;
    }

    
    public boolean acceptSpouse(int sex) {
        // already 2 spouses, false
        if (getNoOfSpouses() == 2) {
            return false;
        }

        // if a husband already there of Male sex and sex is female, false
        Indi husb = getHusband();
        if (husb != null && husb.getSex() == PropertySex.MALE && sex != PropertySex.FEMALE) {
            return false;
        }
        
        // if a wife already there of Female sex and sex is not male, false
        Indi wife = getWife();
        if (wife != null && wife.getSex() == PropertySex.FEMALE && sex != PropertySex.MALE) {
            return false;
        }
        
        return true;
    }
    
    
    
    /**
     * Sets one of the spouses
     *
     * @param spouse the spouse to set as husband or wife
     *
     * @return the property pointing to spouse after the change
     */
    public PropertyXRef setSpouse(Indi spouse) throws GedcomException {

        Indi husband = getHusband();
        Indi wife = getWife();

        // won't do if husband and wife already known
        if (husband != null && wife != null) {
            throw new GedcomException(resources.getString("error.already.spouses", this));
        }

        // check gender of spouse 
        PropertyXRef HUSBorWIFE;
        switch (spouse.getSex()) {
            default:
            case PropertySex.UNKNOWN:
                // set as blank spouse
                HUSBorWIFE = husband != null ? setWife(spouse) : setHusband(spouse);
                break;
            case PropertySex.MALE:
                // overwrite husband
                HUSBorWIFE = setHusband(spouse);
                if (wife != null && wife.getSex() != PropertySex.FEMALE) {
                    wife.setSex(PropertySex.FEMALE);
                }
                // keep old husband as wife if necessary
                if (husband != null) {
                    setWife(husband);
                }
                break;
            case PropertySex.FEMALE:
                // overwrite wife
                HUSBorWIFE = setWife(spouse);
                if (husband != null && husband.getSex() != PropertySex.MALE) {
                    husband.setSex(PropertySex.MALE);
                }
                // keep old wife as husband if necessary
                if (wife != null) {
                    setHusband(wife);
                }
                break;
        }

        // done
        return HUSBorWIFE;
    }

    /**
     * Adds another child to the family
     */
    public PropertyXRef addChild(Indi newChild) throws GedcomException {

        // Remember Indi who is child
        PropertyChild pc = new PropertyChild();
        pc.setValue(newChild.getId());
        addProperty(pc);

        // Link !
        try {
            pc.link();
        } catch (GedcomException ex) {
            delProperty(pc);
            throw ex;
        }

        return pc;
    }

    /**
     * list of famas to array
     */
    /*package*/ static Fam[] toFamArray(Collection<Fam> c) {
        return c.toArray(new Fam[c.size()]);
    }

    /**
     * Meier, Magdalene (I1) & Meier, Lars (I2) ...
     */
    @Override
    protected String getToStringPrefix(boolean showIds) {

        StringBuilder result = new StringBuilder();

        Indi husband = getHusband();
        if (husband != null) {
            result.append(husband.toString(showIds));
            result.append(" ");
            result.append(TextOptions.getInstance().getMarriageSymbol());
        }

        Indi wife = getWife();
        if (wife != null) {
            result.append(wife.toString(showIds));
        }

        // Done
        return result.toString();
    }

    /**
     * Returns a user-readable fam title
     * @return 
     */
    @Override
    public String getDisplayTitle() {
        return getDisplayTitle(true);
    }
    
    public String getDisplayTitle(boolean showid) {
        String husbname = "?";
        if (getHusband() != null) {
            String lastNames[] = getHusband().getLastName().split(",");
            husbname = lastNames.length > 0 ? lastNames[0] : "?";
        }
        
        String wifename = "?";
        if (getWife() != null) {
            String lastNames[] = getWife().getLastName().split(",");
            wifename = lastNames.length > 0 ? lastNames[0] : "?";
        }
        String str = getMarriageAsString();
        str = str.isEmpty() ? "" : (" (" + str + ")");
        
        return (showid ? getId() + " - " : "") + husbname + " " + TextOptions.getInstance().getMarriageSymbol() + " " + wifename + str;
    }

    public String getDisplayFullNames(boolean showid) {
        String husbname = "?";
        if (getHusband() != null) {
            husbname = getHusband().getDisplayTitle(showid);
        }
        
        String wifename = "?";
        if (getWife() != null) {
            wifename = getWife().getDisplayTitle(showid);
        }

        String str = getMarriageAsString();
        str = str.isEmpty() ? "" : ("(" + str + ") ");
        
        return (showid ? getId() + " = " : "") + husbname + " " + TextOptions.getInstance().getMarriageSymbol() + str + " " + wifename;
    }

    /**
     * Meier, Magdalene (I1) & Meier, Lars (I2) ...
     */
    protected String getNames() {

        StringBuilder result = new StringBuilder();

        Indi husband = getHusband();
        if (husband != null) {
            result.append(husband.getName());
        } else {
            result.append(resources.getString("prop.event.knwontohavehappened"));
        }

        result.append(" ");
        result.append(TextOptions.getInstance().getMarriageSymbol());
        result.append(" ");
        
        Indi wife = getWife();
        if (wife != null) {
            result.append(wife.getName());
        } else {
            result.append(resources.getString("prop.event.knwontohavehappened"));
        }

        // Done
        return result.toString();
    }

    /**
     * Calculate fam's Marriage date
     *
     * @return date or null
     */
    public PropertyDate getMarriageDate() {
        return getMarriageDate(false);
        // Calculate MARR|DATE
    }
    
    /**
     * Calculate fam's Marriage date as string
     */
    public String getMarriageAsString() {

        PropertyDate p = getMarriageDate();
        if (p == null) {
            return "";
        }

        // Return string value
        return p.getDisplayValue();
    }

    

    /**
     * returns a PropertyDate view on the marriage date of this family
     *
     * @param create if true, and the property doesn't already exist, initialize the Property
     *
     * @return a PropertyDate or null. If create is true, this method will not return null
     */
    public PropertyDate getMarriageDate(boolean create) {
        PropertyDate date = (PropertyDate) getProperty(PATH_FAMMARRDATE);
        if (null != date || !create) {
            return date;
        }
        setValue(PATH_FAMMARRDATE, "");
        return (PropertyDate) getProperty(PATH_FAMMARRDATE);
    }

    /**
     * Get fam's Marriage place
     *
     * @return date or null
     */
    public PropertyPlace getMarriagePlace() {
        return getMarriagePlace(false);
        // Calculate MARR|DATE
    }

    /**
     * returns a PropertyPlace view on the marriage place of this family
     *
     * @param create if true, and the property doesn't already exist, initialize the Property
     *
     * @return a PropertyPlace or null. If create is true, this method will not return null
     */
    public PropertyPlace getMarriagePlace(boolean create) {
        PropertyPlace place = (PropertyPlace) getProperty(PATH_FAMMARRPLAC);
        if (null != place || !create) {
            return place;
        }
        setValue(PATH_FAMMARRPLAC, "");
        return (PropertyPlace) getProperty(PATH_FAMMARRPLAC);
    }

    /**
     * Calculate fam's divorce date
     *
     * @return date or null
     */
    public PropertyDate getDivorceDate() {
        // Calculate DIV|DATE
        return (PropertyDate) getProperty(PATH_FAMDIVDATE);
    }

    /**
     * Swap spouses
     */
    public void swapSpouses() throws GedcomException {

        PropertyHusband husband = (PropertyHusband) getProperty("HUSB", true);
        PropertyWife wife = (PropertyWife) getProperty("WIFE", true);

        // noop?
        if (husband == null && wife == null) {
            return;
        }

        // swivel pointers
        PropertyFamilySpouse famsh = null;
        PropertyFamilySpouse famsw = null;

        if (husband != null) {
            famsh = (PropertyFamilySpouse) husband.getTarget();
            husband.unlink();
        }

        if (wife != null) {
            famsw = (PropertyFamilySpouse) wife.getTarget();
            wife.unlink();
        }

        // only one? (leave this code after unlink)
        if (husband == null) {
            setHusband((Indi) famsw.getEntity());
            delProperty(wife);
            return;
        }

        if (wife == null) {
            setWife((Indi) famsh.getEntity());
            delProperty(husband);
            return;
        }

        husband.link(famsw);
        wife.link(famsh);

    }

    /**
     * Set as the preferred family for a couple
     * In case set is true : add PREF tag
     * In case set is false : remove PREF tag
     * @param set
     */
    public void setPreferred(boolean set) {
        
        Property pref = getProperty(TAG_PREF);
        if (pref != null && !set) {
            delProperty(pref);
        } else if (pref == null && set) {
            try {
                addProperty(TAG_PREF, "", 0);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Accessor
     * @return 
     */
    public boolean isPreferred() {
        return getProperty(TAG_PREF) != null;
    }
    
    
    
    public List<PropertyEventDetails> getEvents() {

        List<PropertyEventDetails> eventList = new ArrayList<>();
        
        getAllProperties(null).stream().filter((prop) -> (prop.isEvent())).forEachOrdered((prop) -> {
            eventList.add((PropertyEventDetails)prop);
        });
        
        return eventList;
    }
    
    
} //Fam
