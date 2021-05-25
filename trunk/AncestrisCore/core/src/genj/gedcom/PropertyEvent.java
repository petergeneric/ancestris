/**
 * GenJ - GenealogyJ
 * Ancestris
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 * Copyright (C) 2003 - 2021 Frederic Lapeyre <frederic@ancestris.org>
 *
 * This piece of code is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.gedcom;

import genj.util.swing.ImageIcon;
import java.util.regex.Pattern;

/**
 * Gedcom Property : EVENT
 */
public class PropertyEvent extends PropertyEventDetails {

    public static ImageIcon IMG = Grammar.V55.getMeta(new TagPath("INDI:EVEN")).getImage();

    /**
     * whether the event is known to have happened
     */
    private boolean knownToHaveHappened;

    private String description;

    /**
     * need tag-argument constructor for all properties
     */
    public PropertyEvent(String tag) {
        super(tag);
    }

    /**
     * Returns the value of this property
     */
    public String getValue() {
        if (description != null && !description.isEmpty()) {
            return description;
        }
        return knownToHaveHappened ? "Y" : "";
    }

    /**
     * Returns the display value of this property
     */
    public String getDisplayValue() {
        if (description != null && !description.isEmpty() && !knownToHaveHappened) {
            return description;
        }
        return knownToHaveHappened ? resources.getString("prop.event.knwontohavehappened") : "";
    }

    /**
     * Sets the value of this property
     * @param value
     */
    public void setValue(String value) {
        setKnownToHaveHappened(value.toLowerCase().equals("y"));
        description = value;
    }

    /**
     * Returns the list of paths which identify PropertyEvents
     * @param gedcom
     * @return 
     */
    public static TagPath[] getTagPaths(Gedcom gedcom) {
        return gedcom.getGrammar().getAllPaths(null, PropertyEvent.class);
    }

    /**
     * Access - whether this event is known to have happened
     *
     * @return null if this attribute is not supported, true or false otherwise
     */
    public Boolean isKnownToHaveHappened() {
        // patch - no known EVEN
        if (getTag().equals("EVEN")) {
            return null;
        }
        return new Boolean(knownToHaveHappened);
    }

    /**
     * Access - whether this event is known to have happened
     */
    public void setKnownToHaveHappened(boolean set) {
        String old = getValue();
        knownToHaveHappened = set;
        if (set) {
            description = "Y";
        } else {
            description = "";
        }
        propagatePropertyChanged(this, old);
    }

    /**
     * Returns <b>true</b> if this property is valid
     */
    /**
     * This tests the compliance regarding the rule of the Y value, "known-to-have happened", 
     * for compliance with the norm and with consistency to the import corrections
     * 
     * The rule depends on the norm (5.5 != 5.5.1)
     * 
     * However we can spot inconsistencies in the norm as per the annotated 5.5.1 version:
     * - in 5.5.1, EVEN should be empty except for families EVEN, but example page 48 clearly applies to an invididual ("Appointed Zoning Committee Chairperson")
     *  Hence Ancestris will accept any value for all EVEN and will skip controls for this tag (EVEN not included in the list)
     * 
     * - Difference between 5.5 and 5.5.1 does not say anything about the Y tag, not even in the annotated version, 
     * although the list of events where Y applies differ.
     * But the annotated version also says "the line value of most records may only be empty if it has subrecords; 
     * If there are no subrecords, the line value Y must be used".
     * As a result, Ancestris will require Y value in 5.5 for all events listed below and for BIRT, CHR, DEAT and MARR in 5.5.1, 
     * and will *tolerate Y value* for other events in 5.5.1 (in the editors, the Y value for all events is generated automatically).
     *  
     * "individual attributes" can have a value which is not Y, and they are not considered here, with the exception of RESI for individuals
     *  which is an attribute with no value according to the norm. RESI is ) 
     * (please note that RESI for an individual is an attribute, and RESI for a family is an event ).
     * 
     * - According to norm 5.5.1:
     *    - tag events which accept "Y" are only: (for these events, an empty value requires a PLAC or a DATE tag underneath)
     *          individuals: BIRT|CHR|DEAT
     *          families: MARR
     *    - all the other event tags listed below should have an empty value 
     *      (if the tag is there, it does not assume either a date or a place should be indicated, as per the norm, but there should be something underneath)
     *      As opposed to norm 5.5 below, for these event, the known-to-have-happaned indicator when date and place is not known, would be to put the note or source
     *      that proves that the event is known to have happened.
     * 
     * - According to norm 5.5: 
     *    - tag events which accept "Y" are for: 
     *          individuals: all events listed here
     *          families: all events listed here
     * 
     *      The occurrence of an event is asserted by the presence of either a DATE tag and value or a PLACe
     *      tag and value in the event structure. When neither the date value nor the place value are known then
     *      a Y(es) value on the parent event tag line is required to assert that the event happened. For example
     *      each of the following GEDCOM structures assert that a death happened:
     * 
     *      1 DEAT Y
     * 
     *      1 DEAT
     *       2 DATE 2 OCT 1937
     * 
     *      1 DEAT
     *       2 PLAC Cove, Cache, Utah
     * 
     *      Using this convention, as opposed to the just the presence of the tag, protects GEDCOM processors
     *      which removes (prunes) lines which have neither a value nor any subordinate line. It also allows a
     *      note or source to be attached to an event context without implying that the event occurred.
     *      It is not proper GEDCOM form to use a N(o) value with an event tag to infer that it did not happen.
     *      A convention to handle events which never happened may be defined in the future.
     * 
     * 
     */
    protected static final String INDI_TAG_YES_55 = "BIRT|CHR|DEAT|BURI|CREM|"
            + "ADOP|BAPM|BARM|BASM|BLES|CHRA|CONF|FCOM|ORDN|NATU|EMIG|IMMI|"
            + "CENS|PROB|WILL|GRAD|RETI|";
    protected static final String FAM_TAG_YES_55 = "ANUL|CENS|DIV|DIVF|"
            + "ENGA|MARR|MARB|MARC|MARL|MARS|";

    protected static Pattern tag_y_55 = Pattern.compile("(" + INDI_TAG_YES_55 + FAM_TAG_YES_55 + ")");

    protected static final String INDI_TAG_YES_551 = "BIRT|CHR|DEAT|";
    protected static final String FAM_TAG_YES_551 = "MARR|";

    protected static Pattern tag_y_551 = Pattern.compile("(" + INDI_TAG_YES_551 + FAM_TAG_YES_551 + ")");

    @Override
    public boolean isValid() {
        
        String tag = getTag();
        
        // Check if tag is among the list (it should be as only listed events are of instance PropertyEvent, so just to make sure)
        if (!tag_y_55.matcher(tag).matches()) {
            return true;
        }
        
        String value = getValue();
        Grammar grammar = getGedcom().getGrammar();

        boolean isNull = value.isEmpty();
        boolean isY = value.equals("Y");
        
        boolean isV55 = Grammar.V55.equals(grammar);
        boolean isV551 = Grammar.V551.equals(grammar);
        boolean isTagY55 = tag_y_55.matcher(tag).matches();
        boolean isTagY551 = tag_y_551.matcher(tag).matches();
        boolean isYesTag = (isV55 && isTagY55) || (isV551 && isTagY551) || (isV551 && isTagY55); // tolerate Y value in norm 5.5.1 for tags not accepting Y in 5.5.1 but in 5.5
        
        boolean hasChild = getNoOfProperties() > 0; 
        boolean hasPDChild = (getProperty("PLAC", true) != null) || (getProperty("DATE", true) != null); // correctly dated or localized

        // Null value is ok for all even tags except for yes tags with no PLACe/DATE child where Y is required
        if (isNull) {
            if (!hasChild) {
                if (isYesTag) {
                    invalidReason = "err.yestag.YvalueRequired"; // yes tag requires "Y" if no subordinate (date, place, or any other)
                    return false;
                } else {
                    return super.isValid(); // case of RESI
                }
            }
            return true;
        }
        
        // "Y" is only OK for tag accepting Y with no PD child
        if (isY) {
            if (isYesTag) {
                if (hasPDChild) {
                    invalidReason = "err.yestag.YvalueRedundant"; // Y value redundant
                    return false;
                }
                return true;
            }
            invalidReason = "err.yestag.YvalueForbidden"; // Y value only for Yes tags with no PLAC nor DATE
            return false;
        }
        
        if (!isNull) { // other values
            if (isYesTag) {
                if (hasPDChild) {
                    invalidReason = "err.yestag.nonYvalueForbidden"; // Yes tags only accept Y value, and Y would be redundant here
                } else {
                    invalidReason = "err.yestag.YvalueInstead"; // Yes tags value can only be 'Y' if event not dated nor localized
                }
            } else {
                invalidReason = "err.value.emptyRequired"; // Events tags out of Yes tags should be empty
            }
            return false;
        }
        
        // done
        
        
        return true;
    }
    
    
} //PropertyEvent
