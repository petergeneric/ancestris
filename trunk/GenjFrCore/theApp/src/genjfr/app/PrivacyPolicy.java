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
 *
 * La loi du 6 janvier 1978 définit les données à caractère personnel comme
 * étant toutes informations relatives à une personne physique identifiée ou
 * qui peut être identifiée, directement ou indirectement. La loi sur ce point
 * est en parfaite adéquation avec la Directive européenne du 24 octobre
 * 2005.
 * 
 * La loi n°79-18 du 3 janvier 1979 trouve à s’appliquer. Son
 * article 7 dispose expressément que :
 * « Le délai au-delà duquel les documents d'archives publiques peuvent être
 * librement consultés est porté à :
 * 1° Cent cinquante ans à compter de la date de naissance pour les
 * documents comportant des renseignements individuels de caractère
 * médical ;
 * 2° Cent vingt ans à compter de la date de naissance pour les dossiers de
 * personnel ;
 * 3° Cent ans à compter de la date de l'acte ou de la clôture du dossier pour
 * les documents relatifs aux affaires portées devant les juridictions, y
 * compris les décisions de grâce, pour les minutes et répertoires des
 * notaires ainsi que pour les registres de l'état civil et de l'enregistrement
 * (…) ».
 * 
 */
public class PrivacyPolicy {

    // GENERAL PRIVACY RULE 1 (GP1) : Any information (property) dated within the most recent n years is private.
    // GP1-1 : All information, regardless of the date, rolling up to a private information, including linked SOUR, REPO, NOTE, MEDIA, is considered private
    //      (e.g. if a SOUR entity or a MEDIA supports a private birth, it is also private, even if this SOUR or MEDIA has no date or is not marked private itself.
    //      (this is to protect sources documents for instance where the date is not an info of SOUR but still is 
    //      (somewhere in the document, the picture, etc.
    // GP1-2 : All 'entities' a private info belongs to, regardless of their other dates, are also considered private (this re-inforces protection by default)
    //      (for instance, a person whose death is private, is private, even if its birth says it is not.
    //      (No info of a private entity can be displayed, including its SOUR, REPO, NOTE, MEDIA contained in it
    private int yearsInfoIsPrivate;
    //
    // GENERAL PRIVACY RULE 2 (GP2) : Alive persons are private
    // All alive person (or supposed alive : no death event and birth < 120 years) is private, even though the rule of "n" years does not capture it as private
    // All info of a private personne is considered private (applying GR1-1).
    private boolean infoOfAliveIsPrivate;
    //
    // LOCAL PRIVACY RULE (LP1) : All individuals or information *marked* private is private along with information held "below" such mark
    // As a result, information held "above" such mark is not private, therefore is public (GR1-2 not applied here)
    private String tagMarkingPrivate;
    //
    //  LOCAL PRIVACY RULE (LP2) : certain events or tags are considered private ; this does not imply that corresponding individuals are private
    // Events/tags to be selected by preferences (using a map between property tag and string value "contained"
    // If string is "*" then all such tags are considered private
    // For instance:
    // - all divorces or adoptions are private, religion, political affiliations, medical information, etc.
    // - certain occupations are private
    // - certain locations are private
    private Map<String, String> privateEvents = new TreeMap<String, String>();
    //
    // EXCEPTION RULE (ER1) : All information of deceased is public, even though any of the above rules says it should be private
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
     * General privacy checker
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

        // if person alive?
        if (infoOfAliveIsPrivate && isInfoOfAlive(prop)) {
            return true;
        }

        // is event private?
        if (privateEvents != null && isPrivateEvent(prop)) {
            return true;
        }

        // is prop tagged?
        if (tagMarkingPrivate != null && hasTagMarkingPrivate(prop)) {
            return true;
        }

// FL: supprimé d'ici car doit être placé au niveau de certains tests élémentaires
// (en effet: si on test la naissance, la personne peut ne pas avoir l'air privée sur la règle des années
// alors qu'elle le serait sur le décès). Il faut donc ne pas se contenter de remonter l'arbre des propriétés.
//        // is parent property is private?
//        prop = prop.getParent();
//        if (prop != null) {
//            return isPrivate(prop);
//        }
        return false;
    }

    /**
     * Elemental checker :
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
     * Elemental checker :
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
     * Elemental checker :
     * Check whether a prop belongs to a person alive or supposed alive : no death event and birth < 120 years
     */
    private boolean isInfoOfAlive(Property prop) {
        return false;
    }

    /**
     * Elemental checker :
     * Check for marked with tag
     */
    private boolean isPrivateEvent(Property prop) {
        return false;
// TODO: à ajouter
//        // is parent property is private?
//        prop = prop.getParent();
//        if (prop != null) {
//            return isPrivate(prop);
//        }
    }

    /**
     * Elemental checker :
     * Check for marked with tag
     */
    private boolean hasTagMarkingPrivate(Property prop) {
        return getPropertyFor(prop, tagMarkingPrivate, Property.class) != null;
// TODO: à ajouter
//        // is parent property is private?
//        prop = prop.getParent();
//        if (prop != null) {
//            return isPrivate(prop);
//        }
    }

    /**
     * Supportting methods folow
     */
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
