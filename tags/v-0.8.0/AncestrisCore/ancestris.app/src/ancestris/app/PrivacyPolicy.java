/**
 * Ancestris
 *
 */
package ancestris.app;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyXRef;
import genj.gedcom.time.Delta;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A filtering scheme applying privacy to Gedcom properties
 *
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
 *
 * Loi n°2008-696 du 15 juillet 2008 relative aux archives : une adaptation
 * aux exigences actuelles de transparence et de simplification du droit
 * Cette loi adoptée après un périple législatif de près de deux ans, adapte
 * une législation inchangée depuis une trentaine d’années (loi n°79-18 du 
 * 3 janvier 1979) aux exigences actuelles de transparence de l'administration
 * et de simplification du droit. Au travers des différentes mesures exposées,
 * la loi aligne par touches successives le régime des archives publiques sur
 * celui des documents administratifs (loi n° 78-753 du 17 juillet 1978).
 *
 * FL : d'après cet alignement, je pourrais simplifier la compréhension comme
 * suit:
 * Les vivants:
 * - 125 ans pour les dossiers médicaux
 * - 100 ans pour les informations civiles des mineurs
 * - 75 ans pour les informations civiles des non mineurs
 * - 50 ans pour les données privées
 * Les décédés:
 * - 25 ans après le décès
 *
 * En terme de programmation, ces règles sont plutôt ambigües à mettre en oeuvre 
 * aussi je préfère reporter la responsabilité sur l'utilisateur en lui donnant
 * les règles ci-dessous, règles qui me paraissent à la fois traiter les cas
 * généraux et les exceptions plus ciblées.
 * 
 *
 */
public class PrivacyPolicy {

    // GENERAL PRIVACY RULE 1 (GP1) : "privacy years property"
    // GP1-1 : Any information (=property) dated within the most recent n years is private.
    //      For this we need to define the privacy date of a property : it will be the most recent date of any "DATE" property underneath that property
    // GP1-2 : Any 'entity' father of a private years property is also a private years property
    //      (for instance, a person whose death is private, is private, even if its birth says it is not.
    //      (this re-inforces protection by default)
    // GP1-3 : Any 'entity' SOUR, REPO, NOTE, MEDIA, regardless of the date, child of a private years property, is considered a private years property
    //      (this does not apply to INDI nor FAM entities.
    //      (e.g. if a SOUR entity or a MEDIA supports a private birth, it is also private, even if this SOUR or MEDIA has no date or is not marked private itself.
    //      (this is to protect sources documents for instance where the date is not an info of SOUR but still is
    //      (somewhere in the document, the picture, etc.
    private int yearsInfoIsPrivate;
    //
    // GENERAL PRIVACY RULE 2 (GP2) : "alive persons"
    // GP2 : Alive persons are private years entity
    // All alive person (or supposed alive : no death event and birth < 120 years) is private, even though the rule of "n" years does not capture it as private
    // All info of a private personne is considered private (applying GR1-1).
    private boolean infoOfAliveIsPrivate;
    //
    // LOCAL PRIVACY RULE (LP1) : "privacy marked property"
    // All individuals or information *marked* private is private along with information held "below" such mark
    // As a result, information held "above" such mark is not private marked, therefore is public
    private String tagMarkingPrivate;
    //
    //  LOCAL PRIVACY RULE (LP2) : "private events"
    // Certain events or tags are considered private ; this does not imply that corresponding individuals are private
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
    //
    //
    // For performance reasons, keep track of private years entities and alive entities
    private Set<Entity> privateYearsEntities = new HashSet<Entity>();
    private Set<Entity> aliveEntities = new HashSet<Entity>();
    //
    private int yearsIndiCanBeAlive = 130; //TODO - le mettre en paramètre dans les préférences

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

    /***************************************************************************
     * General privacy checker
     **************************************************************************/
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

        // is property tagged as private?
        if (tagMarkingPrivate != null && hasTagMarkingPrivate(prop)) {
            return true;
        }

        return false;
    }

    /***************************************************************************
     * Elemental checkers
     **************************************************************************/
    //
    //
    //
    /**
     * Elemental checker :
     * Check whether this property is a "private years property"
     * Returns true (private years) if one of two cases:
     * - prop belongs to an INDI or FAM which has a private years date
     * - prop does not belong to INDI nor FAM, and it is related to an entity
     *   INDI or FAM which has a private years date
     * Store privateYears entity in set as we go to improve performance
     */
    private boolean isWithinPrivateYears(Property prop) {
        if (prop == null) {
            return false;
        }
        Entity ent = prop.getEntity();

        // check if entity has already been considered as private before
        if (privateYearsEntities.contains(ent)) {
            return true;
        }

        if ((ent instanceof Indi) || (ent instanceof Fam)) {
            if (hasPrivateYearsDate(ent)) {
                privateYearsEntities.add(ent);
                return true;
            }
        } else {
            // get INDI and FAM related entities
            // (not all, otherwise might loop forever as I would have to use
            //  the recursive isWithinPrivateYears in here)
            List<Entity> list = getRelatedPeople(ent);
            if (!list.isEmpty()) {
                for (Iterator<Entity> it = list.iterator(); it.hasNext();) {
                    Entity entity = it.next();
                    if (privateYearsEntities.contains(entity)) {
                        return true;
                    }
                    if (hasPrivateYearsDate(entity)) {
                        privateYearsEntities.add(entity);
                        privateYearsEntities.add(ent);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Elemental checker :
     * Check whether a prop belongs to a person alive or supposed alive : no death event and birth < xxx years
     */
    private boolean isInfoOfAlive(Property prop) {
        if (prop == null) {
            return false;
        }
        Entity ent = prop.getEntity();

        // check if entity has already been considered as private before
        if (aliveEntities.contains(ent)) {
            return true;
        }

        if (ent instanceof Indi) {
            if (isAlive((Indi) ent)) {
                aliveEntities.add(ent);
                return true;
            }
        } else if (ent instanceof Fam) {
            Indi husband = ((Fam) ent).getHusband();
            if (husband != null && isAlive(husband)) {
                aliveEntities.add(husband);
                aliveEntities.add(ent);
                return true;
            }
            Indi wife = ((Fam) ent).getWife();
            if (wife != null && isAlive(wife)) {
                aliveEntities.add(wife);
                aliveEntities.add(ent);
                return true;
            }
            return false;
        } else {
            // Get related indis and families
            List<Entity> list = getRelatedPeople(ent);
            if (!list.isEmpty()) {
                for (Iterator<Entity> it = list.iterator(); it.hasNext();) {
                    Entity entity = it.next();
                    if (aliveEntities.contains(entity)) {
                        return true;
                    }
                    if ((entity instanceof Indi) && (isAlive((Indi) entity))) {
                        aliveEntities.add(entity);
                        aliveEntities.add(ent);
                        return true;
                    }
                    if (entity instanceof Fam) {
                        Indi husband = ((Fam) entity).getHusband();
                        if (husband != null && isAlive(husband)) {
                            aliveEntities.add(husband);
                            aliveEntities.add(ent);
                            return true;
                        }
                        Indi wife = ((Fam) entity).getWife();
                        if (wife != null && isAlive(wife)) {
                            aliveEntities.add(wife);
                            aliveEntities.add(ent);
                            return true;
                        }
                    }
                }
            }
        }
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
        if (getPropertyFor(prop, tagMarkingPrivate, Property.class) != null) {
            return true;
        }
        // is parent property marked?
        prop = prop.getParent();
        if (prop != null) {
            return hasTagMarkingPrivate(prop);
        }
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

    /***************************************************************************
     * Supporting methods follow
     **************************************************************************/
    private boolean hasPrivateYearsDate(Property prop) {
        List<PropertyDate> dateProps = prop.getProperties(PropertyDate.class);
        for (Iterator<PropertyDate> it = dateProps.iterator(); it.hasNext();) {
            PropertyDate propertyDate = it.next();
            if (isDatePrivate(propertyDate)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDatePrivate(PropertyDate date) {
        if (date == null) {
            return false;
        }
        // check anniversary of property's date
        Delta anniversary = date.getAnniversary();
        return anniversary != null && anniversary.getYears() < yearsInfoIsPrivate;
    }

    /**
     * Is Alive
     * Definition : person whose birth is less than 130 years ago
     * and person whith no DEAT tag present
     * @param indi
     * @return
     */
    private boolean isAlive(Indi indi) {
        PropertyDate bdate = indi.getBirthDate();
        PropertyDate ddate = indi.getDeathDate();
        if (bdate == null) {
            return false;
        }
        Delta anniversary = bdate.getAnniversary();
        return (ddate == null) && (anniversary != null) && (anniversary.getYears() < yearsIndiCanBeAlive);
    }

    private List<Entity> getRelatedPeople(Entity ent) {
        List<Entity> list = new ArrayList<Entity>();
        List<PropertyXRef> refProps = ent.getProperties(PropertyXRef.class);
        for (Iterator<PropertyXRef> it = refProps.iterator(); it.hasNext();) {
            PropertyXRef propertyXRef = it.next();
            Entity target = propertyXRef.getTargetEntity();
            if (list.contains(target)) {
                continue;
            }
            if ((target instanceof Indi) || (target instanceof Fam)) {
                list.add(target);
            }
        }
        return list;
    }

    // Find a sub-property by tag and type
    private Property getPropertyFor(Property prop, String tag, Class<?> type) {
        // check children
        for (int i = 0, j = prop.getNoOfProperties(); i < j; i++) {
            Property child = prop.getProperty(i);
            if (child.getTag().equals(tag) && type.isAssignableFrom(child.getClass())) {
                return child;
            }
        }
        return null;
    }

    public Set<Entity> getCurrentPrivateEntities() {
        return privateYearsEntities;
    }
}
