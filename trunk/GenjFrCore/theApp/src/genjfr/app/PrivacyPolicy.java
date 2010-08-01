/**
 * Ancestris
 *
 */
package genjfr.app;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.Delta;
import java.util.Map;
import java.util.TreeMap;

/**
 * A filtering scheme applying privacy to Gedcom properties overiding genjfr one
 * @author frederic using Genj base code
 */
public class PrivacyPolicy {

    // GENERAL RULE : Any information dated within the most recent n years is private,
    // and all entities this info belongs to, regardless of their other dates, is also private
    // and all information, regardless of the date, rolling up to a private entity is considered private
    // Therefore this rule defines private entities : an entity is private if it contains private info by date
    // If an entity contains a link to a private SOUR, REPO, NOTE, or MEDIA, it is also private
    // No info of a private entity can be displayed, including its SOUR, REPO, NOTE, MEDIA contained in it
    private int yearsInfoIsPrivate;

    // GENERAL RULE : All individuals or information *marked* private is private along with information held "below" such mark
    // As a result, information held "above" such mark is not private, therefore is public
    private String tagMarkingPrivate;

    // GENERAL RULE : Alive persons are private
    // All alive person (or supposed alive : no death event and birth < 120 years) is private, even though the rule of "n" years does not capture it as private
    private boolean infoOfAliveIsPrivate;

    // EVENT RULE : certain events or tags are considered private ; this does not imply that corresponding individuals are private
    // Events/tags to be selected by preferences (using a map between property tag and string value "contained"
    // If string is "*" then all such tags are considered private
    // For instance:
    // - all divorces or adoptions are private
    // - certain occupations are private
    // - certain locations are private
    private Map<String, String> privateEvents = new TreeMap<String, String>();

    // EXCEPTION RULE :
    // All information of deceased is public, even though any of the above rules says it should be private
    private boolean infoOfDeceasedIsPublic;

    /**
     * Constructor
     */
    public PrivacyPolicy(int yearsInfoIsPrivate, String tagMarkingPrivate, boolean infoOfAliveIsPrivate, Map<String, String> privateEvents, boolean infoOfDeceasedIsPublic) {
        this.yearsInfoIsPrivate = Math.max(yearsInfoIsPrivate, 0);
        this.tagMarkingPrivate = tagMarkingPrivate == null || tagMarkingPrivate.length() == 0 ? null : tagMarkingPrivate;
        this.infoOfAliveIsPrivate = infoOfAliveIsPrivate;
        this.privateEvents = privateEvents;
        this.infoOfDeceasedIsPublic = infoOfDeceasedIsPublic;
    }

    /**
     * Check for privacy
     */
    public boolean isPrivate(Property prop) {

        // treat exception first : not private if property belongs to deceased
        if (infoOfDeceasedIsPublic && isInfoOfDeceased(prop)) {
            return false;
        }

        // if info recent?
        if (yearsInfoIsPrivate > 0 && isWithinPrivateYears(prop)) {
            return true;
        }

        // is prop tagged?
        if (tagMarkingPrivate != null && hasTagMarkingPrivate(prop)) {
            return true;
        }

        // is parent property is private?
        prop = prop.getParent();
        if (prop != null) {
            return isPrivate(prop);
        }
        return false;
    }

    /**
     * Individual check methods :
     * Check whether a property belongs to deceased individuals only
     */
    private boolean isInfoOfDeceased(Property prop) {
        // contained in indi? check death-date
        Entity e = prop.getEntity();
        if (e instanceof Indi) {
            return ((Indi) e).isDeceased();
        }

        // contained in Fam? check husband and wife
        if (e instanceof Fam) {
            Indi husband = ((Fam) e).getHusband();
            if (husband != null && !husband.isDeceased()) {
                return false;
            }
            Indi wife = ((Fam) e).getWife();
            return wife != null && wife.isDeceased();
        }

        // dunno
        return false;
    }

    /**
     * Individual check methods :
     * Check whether a prop is still within the privat years' - only if it has a date sub-property
     */
    private boolean isWithinPrivateYears(Property prop) {
        // check date
        PropertyDate date = (PropertyDate) getPropertyFor(prop, "DATE", PropertyDate.class);
        if (date == null) {
            return false;
        }
        // check anniversary of property's date
        Delta anniversary = date.getAnniversary();
        return anniversary != null && anniversary.getYears() < yearsInfoIsPrivate;
    }

    /**
     * Individual check methods :
     * Check for marked with tag
     */
    private boolean hasTagMarkingPrivate(Property prop) {
        return getPropertyFor(prop, tagMarkingPrivate, Property.class) != null;
    }

    /**
     * filter a value
     */
    // Find a sub-property by tag and type
    private Property getPropertyFor(Property prop, String tag, Class type) {
        // check children
        for (int i = 0, j = prop.getNoOfProperties(); i < j; i++) {
            Property child = prop.getProperty(i);
            if (is(child, tag, type)) {
                return child;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private boolean is(Property prop, String tag, Class type) {
        return prop.getTag().equals(tag) && type.isAssignableFrom(prop.getClass());
    }

}
