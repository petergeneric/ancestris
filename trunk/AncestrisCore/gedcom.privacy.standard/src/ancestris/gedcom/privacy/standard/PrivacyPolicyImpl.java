/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.gedcom.privacy.standard;

import ancestris.gedcom.privacy.PrivacyPolicy;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.time.Delta;
import genj.io.Filter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

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
 */
/**
 *
 * @author put in plugin by daniel
 */
@ServiceProviders({
    @ServiceProvider(service = PrivacyPolicy.class),
    @ServiceProvider(service = Filter.class)
})
public class PrivacyPolicyImpl extends PrivacyPolicy implements Filter {

    // For performance reasons, keep track of private years entities and alive entities
    private final Set<Entity> privateYearsEntities = new HashSet<>();
    private final Set<Entity> aliveEntities = new HashSet<>();
    //
    private static final PrivacyPolicy PUBLIC = new PrivacyPolicyImpl() {

        @Override
        public boolean isPrivate(Property prop) {
            return false;
        }
    };
    private static final PrivacyPolicy PRIVATE = new PrivacyPolicyImpl() {

        @Override
        public boolean isPrivate(Property prop) {
            return true;
        }
    };

    @Override
    public PrivacyPolicy getAllPublic() {
        return PUBLIC;
    }

    @Override
    public PrivacyPolicy getAllPrivate() {
        return PRIVATE;
    }

    @Override
    public String getPrivateMask() {
        return Options.getInstance().getPrivateMask();
    }

    @Override
    public void openPreferences() {
        OptionsDisplayer.getDefault().open("OptionFormat/Privacy");
    }

    /**
     * return true if property prop is considered as private
     * @param prop
     * @return
     */
    @Override
    public boolean isPrivate(Property prop) {

        // treat exception first : not private if property belongs to deceased
        if (Options.getInstance().deadIsPublic() && isInfoOfDeceased(prop)) {
            return false;
        }

        // if info recent?
        if (Options.getInstance().getPrivateYears() > 0 && isWithinPrivateYears(prop)) {
            return true;
        }

        // if person alive?
        if (Options.getInstance().aliveIsPrivate() && isInfoOfAlive(prop)) {
            return true;
        }
        // is property tagged as private?
        return Options.getInstance().getPrivateTag() != null && hasTagMarkingPrivate(prop);
    }

    /**
     * Elemental checker :
     * Check whether this property is a "private years property"
     * Returns true (private years) if one of two cases:
     * <li/> prop belongs to an INDI or FAM which has a private years date
     * <li/> prop does not belong to INDI nor FAM, and it is related to an entity
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
                for (Entity entity : list) {
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
                for (Entity entity : list) {
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
    private boolean hasTagMarkingPrivate(Property prop) {
        if (prop.getProperty(Options.getInstance().getPrivateTag()) != null) {
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
        for (PropertyDate propertyDate : dateProps) {
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
        return anniversary != null && anniversary.getYears() < Options.getInstance().getPrivateYears();
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
        return (ddate == null) && (anniversary != null) && (anniversary.getYears() < Options.getInstance().getYearsIndiCanBeAlive());
    }

    private List<Entity> getRelatedPeople(Entity ent) {
        List<Entity> list = new ArrayList<>();
        for (PropertyXRef propertyXRef : ent.getProperties(PropertyXRef.class)) {
            Entity target = propertyXRef.getTargetEntity();
            if (list.contains(target)) {
                continue;
            }
            if ((target instanceof Indi) || (target instanceof Fam)) {
                list.add(target);
            }
            // If Source or Note, get related to these
            if (target instanceof Source || target instanceof Note){
                list.addAll(getRelatedPeople(target));
            }
        }
        return list;
    }

    /** Filter interface implementation */
    @Override
    public String getFilterName() {
        return NbBundle.getMessage(PrivacyPolicyImpl.class, "PrivacyFilterName");
    }

    @Override
    public boolean veto(Property property) {
        return isPrivate(property);
    }

    @Override
    public boolean veto(Entity entity) {
        return isPrivate(entity);
    }

    @Override
    public boolean canApplyTo(Gedcom gedcom) {
        return true;
    }

    public void clear() {
        privateYearsEntities.clear();
        aliveEntities.clear();
    }

}
