/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.gedcom;

import ancestris.core.TextOptions;
import ancestris.gedcom.GedcomDirectory;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;

import java.text.Collator;

import javax.swing.ImageIcon;

/**
 * Gedcom Property : AGE
 */
public class PropertyAge extends Property {

    public final static ImageIcon IMG = Grammar.V55.getMeta(new TagPath("INDI:BIRT:AGE")).getImage();

    /**
     * the age
     */
    private Delta age = new Delta(0, 0, 0);
    private int younger_exactly_older = 0;

    /**
     * as string
     */
    private String ageAsString;

    public static String[] PHRASES = {
        "CHILD", "INFANT", "STILLBORN"
    };

    /**
     * need tag-argument constructor for all properties
     */
    public PropertyAge(String tag) {
        super(tag);
    }

    /**
     * Returns <b>true</b> if this property is valid
     */
    public boolean isValid() {

        Collator c = getGedcom().getCollator();

        if (ageAsString == null) {
            return true;
        }
        for (int i = 0; i < PHRASES.length; i++) {
            if (c.compare(PHRASES[i], ageAsString) == 0) {
                return true;
            }
        }

        return super.isValid();
    }

    /**
     * @see genj.gedcom.Property#addNotify(genj.gedcom.Property)
     */
    /*package*/ void afterAddNotify() {
        // continue
        super.afterAddNotify();
        // try to update age 
        // FL 2021-05-25 : calculate age only if age calculation is automatic and if gedcom file is fully loaded
        // Indeed, if gedcom is loading, not all properties and events are ready, family links with individuals are not established and age calculation is only partial.
        // Additionnaly, if gedcom file was saved with certain values for ages, this would reopen it with potentially different ages
        // It is safer to let the user force the recalculation (edit/calculate ages) rather than having all ages being recalculated automatically by default even if settings says so.
        if (GedcomOptions.getInstance().isAddAge() && GedcomDirectory.getDefault().isGedcomRegistered(getGedcom())) {
            updateAge(false);
        }
        // done
    }

    /**
     * Label for Age
     */
    public static String getLabelForAge() {
        return Gedcom.getName("AGE");
    }

    /**
     * Accessor Value
     * @return 
     */
    public String getValue() {

        if (ageAsString != null) {
            return ageAsString;
        }

        // since we're expected to return a Gedcom compliant value
        // here we're not localizing the return value (e.g. 1y 2m 3d)       
        if (younger_exactly_older > 0) {
            return ">" + age.getValue();
        }
        if (younger_exactly_older < 0) {
            return "<" + age.getValue();
        }
        return age.getValue();
    }
    
    public Delta getAge() {
        return age;
    }

    @Override
    public String getDisplayValue() {

        if (ageAsString != null) {
            String result = resources.getString("prop.age." + ageAsString, false);
            if (result != null) {
                return result;
            } else {
                return ageAsString;
            }
        }

        // since we're expected to return a Gedcom compliant value
        // here we're not localizing the return value (e.g. 1y 2m 3d)       
        if (younger_exactly_older > 0) {
            return ">" + age.toString();
        }
        if (younger_exactly_older < 0) {
            return "<" + age.toString();
        }
        return age.toString();
    }

    /**
     * Accessor Value
     * @param newValue
     */
    @Override
    public void setValue(String newValue) {
        
        String old = getValue();

        // try to parse
        if (newValue.startsWith(">")) {
            newValue = newValue.substring(1);
            younger_exactly_older = 1;
        } else if (newValue.startsWith("<")) {
            newValue = newValue.substring(1);
            younger_exactly_older = -1;
        }
        
        // default a numeric value to a number of years
        if (newValue != null && !newValue.isEmpty() && newValue.replaceAll("[^\\d]", "").equals(newValue)) {
            newValue += "y";
        }

        if (age.setValue(newValue)) {
            ageAsString = null;
        } else {
            ageAsString = newValue;
        }

        // notify
        propagatePropertyChanged(this, old);

        // Done
    }

    public void setValue(Delta age) {
        String old = getValue();

        this.age.setValue(age);
        
        younger_exactly_older = 0;
        ageAsString = null;
        
        // notify
        propagatePropertyChanged(this, old);
    }

    /**
     * Calculate the age at a given pit
     */
    public static Delta getAge(Indi indi, PointInTime pit) {

        PointInTime start = getEarlier(indi, null);
        return start == null || start.compareTo(pit) > 0 ? null : Delta.get(start, pit);
    }

    public static PointInTime getStartPITOfAge(Indi indi) {

        return getEarlier(indi, null);
    }

    /**
     * Update the age
     */
    public boolean updateAge(boolean overwriteString) {
        // calc delta
        PointInTime start = getEarlier();
        PointInTime end = getLater();
        return updateAge(start == null || start.compareTo(end) > 0 ? null : Delta.get(start, end), overwriteString);
    }

    public boolean updateAge(Delta delta, boolean overwriteString) {

        if (delta == null) {
            return false;
        }
        
        if (ageAsString != null && !overwriteString) {
            return false;
        }

        setValue(delta);
        
        // done
        return true;
    }

    /**
     * @see genj.gedcom.Property#compareTo(java.lang.Object)
     */
    public int compareTo(Property other) {
        if (!isValid() || !other.isValid()) {
            return super.compareTo(other);
        }
        return age.compareTo(((PropertyAge) other).age);
    }

    /**
     * Calculates earlier point in time (the birth)
     *
     * INDI:EVENT:AGE -> INDI:BIRT:DATE
     *
     * FAM:MARR:HUSB:AGE -> FAM:HUSB -> INDI:BIRT:DATE
     *
     * FAM:MARR:WIFE:AGE -> FAM:WIFE -> INDI:BIRT:DATE
     */
    public PointInTime getEarlier() {
        return getEarlier(getEntity(), getParent());
    }

    public static PointInTime getEarlier(Entity e, Property parent) {

        // might FAM:MARR:WIFE|HUSB:AGE
        if (e instanceof Fam) {
            if (parent.getTag().equals("HUSB")) {
                e = ((Fam) e).getHusband();
            }
            if (parent.getTag().equals("WIFE")) {
                e = ((Fam) e).getWife();
            }
        }
        // check individual?
        if (!(e instanceof Indi)) {
            return null;
        }
        // date
        PropertyDate birth = ((Indi) e).getBirthDate();

        if (birth == null && TextOptions.getInstance().isUseChr()) {
            Property p = ((Indi) e).getPropertyByPath("INDI:CHR:DATE");
            if (p != null && p instanceof PropertyDate) {
                birth = (PropertyDate) p;
            }
        }

        return birth != null ? birth.getStart() : null;
    }

    /**
     * Calculates later point in time (the event)
     *
     * INDI:EVENT:AGE -> INDI:EVENT:DATE
     *
     * FAM:EVENT:HUSB:AGE -> FAM:EVENT:DATE
     *
     * FAM:EVENT:WIFE:AGE -> FAM:EVENT:DATE
     *
     */
    public PointInTime getLater() {
        return getLater(getParent());
    }

    public PointInTime getLater(Property parent) {

        // might FAM:MARR:WIFE|HUSB:AGE
        if (parent.getTag().equals("HUSB") || parent.getTag().equals("WIFE")) {
            // one more up
            parent = parent.getParent();
        }
        // check event
//    if (!(parent instanceof PropertyEvent))
//      return null;
//    PropertyDate date = ((PropertyEvent) parent).getDate();
        Property prop = parent.getProperty("DATE", true);
        if (prop == null) {
            return null;
        }

        if (!(prop instanceof PropertyDate)) {
            return null;
        }
        PropertyDate date = (PropertyDate) prop;

        // start of date
        return date != null ? date.getStart() : null;
    }

} //PropertyAge
