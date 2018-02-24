package ancestris.modules.releve.model;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.releve.ReleveTopComponent;
import ancestris.modules.releve.model.Record.FieldType;
import genj.gedcom.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.openide.util.NbPreferences;

/**
 *
 * @author Michels
 */
public class CompletionProvider {
    private final FirstNameCompletionSet firstNames = new FirstNameCompletionSet();
    private final LastNameCompletionSet lastNames = new LastNameCompletionSet();
    private final OccupationCompletionSet occupations = new OccupationCompletionSet();
    private final NotaryCompletionSet notaries = new NotaryCompletionSet();
    private final PlaceCompletionSet places = new PlaceCompletionSet();
    private final CompletionSet eventTypes = new CompletionSet();

    private final HashMap<String, Integer> firstNameSex = new HashMap<String, Integer>();

    public static enum CompletionType {
        firstName,
        lastName,
        occupation,
        place
    }

    public static enum IncludeFilter {
        ALL,
        EXCLUDED,
        INCLUDED
    }

    // Register in ancestris lookup for GedcomFileListener
    public CompletionProvider(){
        AncestrisPlugin.register(this);
    }


    /**
     * langue a utiliser pour faire la complétion.
     */
    private Locale locale= Locale.getDefault();

    /**
     * ajoute les prenoms, le noms et les professions dans les listes de completion
     * et dans les statistiques sexes/prenoms
     * @param record
     */
    protected void addRecord(Record record) {

        // FirstName
        firstNames.add(record.getFieldValue(FieldType.indiFirstName), record.getFieldValue(FieldType.indiSex));
        firstNames.add(record.getFieldValue(FieldType.indiMarriedFirstName), FieldSex.getOppositeString(record.getFieldValue(FieldType.indiSex)));
        firstNames.add(record.getFieldValue(FieldType.indiFatherFirstName), FieldSex.MALE_STRING);
        firstNames.add(record.getFieldValue(FieldType.indiMotherFirstName), FieldSex.FEMALE_STRING);

        firstNames.add(record.getFieldValue(FieldType.wifeFirstName), record.getFieldValue(FieldType.wifeSex) );
        firstNames.add(record.getFieldValue(FieldType.wifeMarriedFirstName), FieldSex.getOppositeString(record.getFieldValue(FieldType.wifeSex)));
        firstNames.add(record.getFieldValue(FieldType.wifeFatherFirstName), FieldSex.MALE_STRING);
        firstNames.add(record.getFieldValue(FieldType.wifeMotherFirstName), FieldSex.FEMALE_STRING);

        firstNames.add(record.getFieldValue(FieldType.witness1FirstName), null);
        firstNames.add(record.getFieldValue(FieldType.witness2FirstName), null);
        firstNames.add(record.getFieldValue(FieldType.witness3FirstName), null);
        firstNames.add(record.getFieldValue(FieldType.witness4FirstName), null);


        // LastName
        lastNames.add(record.getFieldValue(FieldType.indiLastName));
        lastNames.add(record.getFieldValue(FieldType.indiMarriedLastName));
        lastNames.add(record.getFieldValue(FieldType.indiFatherLastName));
        lastNames.add(record.getFieldValue(FieldType.indiMotherLastName));

        lastNames.add(record.getFieldValue(FieldType.wifeLastName));
        lastNames.add(record.getFieldValue(FieldType.wifeMarriedLastName));
        lastNames.add(record.getFieldValue(FieldType.wifeFatherLastName));
        lastNames.add(record.getFieldValue(FieldType.wifeMotherLastName));

        lastNames.add(record.getFieldValue(FieldType.witness1LastName));
        lastNames.add(record.getFieldValue(FieldType.witness2LastName));
        lastNames.add(record.getFieldValue(FieldType.witness3LastName));
        lastNames.add(record.getFieldValue(FieldType.witness4LastName));

        
        // Indi Occupation
        occupations.add(record.getFieldValue(FieldType.indiOccupation));
        occupations.add(record.getFieldValue(FieldType.indiMarriedOccupation));
        occupations.add(record.getFieldValue(FieldType.indiFatherOccupation));
        occupations.add(record.getFieldValue(FieldType.indiMotherOccupation));
        // Wife Occupation
        occupations.add(record.getFieldValue(FieldType.wifeOccupation));
        occupations.add(record.getFieldValue(FieldType.wifeMarriedOccupation));
        occupations.add(record.getFieldValue(FieldType.wifeFatherOccupation));
        occupations.add(record.getFieldValue(FieldType.wifeMotherOccupation));
        // witness Occupation
        occupations.add(record.getFieldValue(FieldType.witness1Occupation));
        occupations.add(record.getFieldValue(FieldType.witness2Occupation));
        occupations.add(record.getFieldValue(FieldType.witness3Occupation));
        occupations.add(record.getFieldValue(FieldType.witness4Occupation));
        
        notaries.add(record.getFieldValue(FieldType.notary));

        places.add(record.getFieldValue(FieldType.indiBirthPlace));
        places.add(record.getFieldValue(FieldType.indiResidence));
        places.add(record.getFieldValue(FieldType.indiMarriedResidence));
        places.add(record.getFieldValue(FieldType.indiFatherResidence));
        places.add(record.getFieldValue(FieldType.indiMotherResidence));

        places.add(record.getFieldValue(FieldType.wifeBirthPlace));
        places.add(record.getFieldValue(FieldType.wifeResidence));
        places.add(record.getFieldValue(FieldType.wifeMarriedResidence));
        places.add(record.getFieldValue(FieldType.wifeFatherResidence));
        places.add(record.getFieldValue(FieldType.wifeMotherResidence));

        // EventType
        if ( record.getFieldValue(FieldType.eventType).isEmpty()== false) {
            eventTypes.add(record.getFieldValue(FieldType.eventType));
        }
    }

    /**
     * supprime les prenoms, lee noms et les professions des listes de completion
     * @param record
     */
    protected void removeRecord(final Record record) {

        // FirstName
        firstNames.remove(record.getFieldValue(FieldType.indiFirstName), record.getFieldValue(FieldType.indiSex));
        firstNames.remove(record.getFieldValue(FieldType.indiMarriedFirstName), FieldSex.getOppositeString(record.getFieldValue(FieldType.indiSex)));
        firstNames.remove(record.getFieldValue(FieldType.indiFatherFirstName), FieldSex.MALE_STRING);
        firstNames.remove(record.getFieldValue(FieldType.indiMotherFirstName), FieldSex.FEMALE_STRING);

        firstNames.remove(record.getFieldValue(FieldType.wifeFirstName), record.getFieldValue(FieldType.wifeSex));
        firstNames.remove(record.getFieldValue(FieldType.wifeMarriedFirstName), FieldSex.getOppositeString(record.getFieldValue(FieldType.wifeSex)));
        firstNames.remove(record.getFieldValue(FieldType.wifeFatherFirstName), FieldSex.MALE_STRING);
        firstNames.remove(record.getFieldValue(FieldType.wifeMotherFirstName), FieldSex.FEMALE_STRING);

        firstNames.remove(record.getFieldValue(FieldType.witness1FirstName), null);
        firstNames.remove(record.getFieldValue(FieldType.witness2FirstName), null);
        firstNames.remove(record.getFieldValue(FieldType.witness3FirstName), null);
        firstNames.remove(record.getFieldValue(FieldType.witness4FirstName), null);

        // LastName
        lastNames.remove(record.getFieldValue(FieldType.indiLastName));
        lastNames.remove(record.getFieldValue(FieldType.indiMarriedLastName));
        lastNames.remove(record.getFieldValue(FieldType.indiFatherLastName));
        lastNames.remove(record.getFieldValue(FieldType.indiMotherLastName));

        lastNames.remove(record.getFieldValue(FieldType.wifeLastName));
        lastNames.remove(record.getFieldValue(FieldType.wifeMarriedLastName));
        lastNames.remove(record.getFieldValue(FieldType.wifeFatherLastName));
        lastNames.remove(record.getFieldValue(FieldType.wifeMotherLastName));

        lastNames.remove(record.getFieldValue(FieldType.witness1LastName));
        lastNames.remove(record.getFieldValue(FieldType.witness2LastName));
        lastNames.remove(record.getFieldValue(FieldType.witness3LastName));
        lastNames.remove(record.getFieldValue(FieldType.witness4LastName));
        
        // Indi Occupation
        occupations.remove(record.getFieldValue(FieldType.indiOccupation));
        occupations.remove(record.getFieldValue(FieldType.indiMarriedOccupation));
        occupations.remove(record.getFieldValue(FieldType.indiFatherOccupation));
        occupations.remove(record.getFieldValue(FieldType.indiMotherOccupation));
        // Wife Occupation
        occupations.remove(record.getFieldValue(FieldType.wifeOccupation));
        occupations.remove(record.getFieldValue(FieldType.wifeMarriedOccupation));
        occupations.remove(record.getFieldValue(FieldType.wifeFatherOccupation));
        occupations.remove(record.getFieldValue(FieldType.wifeMotherOccupation));
        // witness Occupation
        occupations.remove(record.getFieldValue(FieldType.witness1Occupation));
        occupations.remove(record.getFieldValue(FieldType.witness2Occupation));
        occupations.remove(record.getFieldValue(FieldType.witness3Occupation));
        occupations.remove(record.getFieldValue(FieldType.witness4Occupation));
        
        notaries.remove(record.getFieldValue(FieldType.notary));

        // places
        //Indi places
        places.remove(record.getFieldValue(FieldType.indiBirthPlace));
        places.remove(record.getFieldValue(FieldType.indiResidence));
        places.remove(record.getFieldValue(FieldType.indiMarriedResidence));
        places.remove(record.getFieldValue(FieldType.indiFatherResidence));
        places.remove(record.getFieldValue(FieldType.indiMotherResidence));
        // wife places
        places.remove(record.getFieldValue(FieldType.wifeBirthPlace));
        places.remove(record.getFieldValue(FieldType.wifeResidence));
        places.remove(record.getFieldValue(FieldType.wifeMarriedResidence));
        places.remove(record.getFieldValue(FieldType.wifeFatherResidence));
        places.remove(record.getFieldValue(FieldType.wifeMotherResidence));

        // EventType
        if ( ! record.getFieldValue(FieldType.eventType).isEmpty()) {
            eventTypes.remove(record.getFieldValue(FieldType.eventType));
        } else {
        }
    }

    /**
     * supprime toutes les données
     * mais conserve les listeners
     */
    protected void removeAll () {
        firstNames.removeAll();
        lastNames.removeAll();
        occupations.removeAll();
        notaries.removeAll();
        places.removeAll();
        eventTypes.removeAll();
    }

    /**
     * update firstname sex statistics by firstname
     * @param firstName
     * @param sex
     */
    public void updateFirstNameSex(String oldFirstName, String oldSex, String firstName, String sex) {
        if (oldFirstName != null && ! oldFirstName.isEmpty() && oldSex != null ) {
            int count = firstNameSex.containsKey(oldFirstName) ? firstNameSex.get(oldFirstName) : 0;
            if (oldSex.equals(FieldSex.MALE_STRING)) {
                firstNameSex.put(oldFirstName, count - 1);
            } else  if (oldSex.equals(FieldSex.FEMALE_STRING)) {
                firstNameSex.put(oldFirstName, count + 1);
            }
        }
        if (firstName != null && ! firstName.isEmpty() && sex != null) {
            int count = firstNameSex.containsKey(firstName) ? firstNameSex.get(firstName) : 0;
            if (sex.equals(FieldSex.MALE_STRING)) {
                firstNameSex.put(firstName, count + 1);
            } else if (sex.equals(FieldSex.FEMALE_STRING)) {
                firstNameSex.put(firstName, count - 1);
            }
        }
    }

    /**
     * retourne le sexe associé au prénom
     * @param firstName
     * @return
     */
    public String getFirstNameSex(String firstName) {
        int count = 0;
        // je cherche parmi les releves
        Integer releveCount = firstNameSex.get(firstName);
        if (releveCount != null) {
            count += releveCount;
        }

        if (count > 0 ) {
            return FieldSex.MALE_STRING;
        } else if (count < 0 ) {
            return FieldSex.FEMALE_STRING;
        } else {
            return FieldSex.UNKNOWN_STRING;
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // ajout d'un gedcom aux listes de completion
    ///////////////////////////////////////////////////////////////////////////

    /**
     * ajoute les noms , prénoms et professions d'un fichier Gedcom
     * dans les listes de completion
     * Remarque :
     * @param gedcom
     */
    protected void addGedcomCompletion(Gedcom gedcom) {
        if (gedcom == null) {
            // rien a faire
            return;
        }

        
        this.locale = gedcom.getLocale();
        
        // j'ajoute les prénoms, noms, professions et lieux du Gedcom dans les
        // listes de completion
        for ( Indi indi : gedcom.getIndis()) {
            firstNames.add(indi.getFirstName(), FieldSex.convertValue(indi.getSex()), false );
        }

        for ( String lastName : PropertyName.getLastNames(gedcom, false)) {
            lastNames.add(lastName, false);
        }
        for ( String occupation : PropertyChoiceValue.getChoices(gedcom, "OCCU", false)) {
            occupations.add(occupation, false);
        }
        for ( String place : PropertyChoiceValue.getChoices(gedcom, "PLAC", false)) {
            places.add(place, false);
        }

        firstNames.fireIncludedUpdateListener();
        lastNames.fireIncludedUpdateListener();
        occupations.fireIncludedUpdateListener();
        places.fireIncludedUpdateListener();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // accesseurs aux listes de completion
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * retourne les prénoms triées par ordre alphabétique
     */
    public List<String> getFirstNames(IncludeFilter filter) {
       return firstNames.getKeys(filter);
    }

    /**
     * retourne les noms triées par ordre alphabétique
     */
    public List<String> getLastNames(IncludeFilter filter) {
        return lastNames.getKeys(filter);
    }

    /**
     * retourne les professions triées par ordre alphabétique
     */
    public List<String> getOccupations(IncludeFilter filter) {
       return occupations.getKeys(filter);
    }

    /**
     * retourne les professions triées par ordre alphabétique
     */
    public List<String> getNotaries(IncludeFilter filter) {
       return notaries.getKeys(filter);
    }

    /**
     * retourne les tags des types d'evenement triées par ordre alphabétique
     */
    public List<String> getEventTypes(IncludeFilter filter) {
        return eventTypes.getKeys(filter);
    }

    /**
     * retourne les lieux triées par ordre alphabétique
     */
    public List<String> getPlaces(IncludeFilter filter) {
        return places.getKeys(filter);
    }

    public Locale getLocale() {
        return locale;
    }

    /**
     * ajoute un listener
     */
    public void addFirstNamesListener(CompletionListener listener) {
       firstNames.addListener(listener);
    }

    /**
     * supprime un listner
     */
    public void removeFirstNamesListener(CompletionListener listener) {
       firstNames.removeListener(listener);
    }

    /**
     * ajoute un listener
     */
    public void addLastNamesListener(CompletionListener listener) {
       lastNames.addListener(listener);
    }

    /**
     * supprime un listner
     */
    public void removeLastNamesListener(CompletionListener listener) {
       lastNames.removeListener(listener);
    }

    /**
     * ajoute un listener
     */
    public void addOccupationsListener(CompletionListener listener) {
       occupations.addListener(listener);
    }

    /**
     * supprime un listner
     */
    public void removeOccupationsListener(CompletionListener listener) {
       occupations.removeListener(listener);
    }

    /**
     * ajoute un listener
     */
    public void addNotariesListener(CompletionListener listener) {
       notaries.addListener(listener);
    }

    /**
     * supprime un listner
     */
    public void removeNotariesListener(CompletionListener listener) {
       notaries.removeListener(listener);
    }
    /**
     * ajoute un listener
     */
    public void addEventTypesListener(CompletionListener listener) {
       eventTypes.addListener(listener);
    }

    /**
     * supprime un listner
     */
    public void removeEventTypesListener(CompletionListener listener) {
       eventTypes.removeListener(listener);
    }

    /**
     * ajoute un listener
     */
    public void addPlacesListener(CompletionListener listener) {
       places.addListener(listener);
    }

    /**
     * supprime un listner
     */
    public void removePlacesListener(CompletionListener listener) {
       places.removeListener(listener);
    }

    /**
     * met a jour la liste de completion firstNames
     * supprime l'ancienne valeur oldValue et ajoute la nouvelle valeur qui est dans field
     * @param firstNameField
     * @param oldFirstName
     */
    public void updateFirstName( String firstName, String sex, String oldFirstName, String oldSex) {
        firstNames.remove(oldFirstName, oldSex);
        firstNames.add(firstName, sex);
    }

    /**
     * met a jour la liste de completion lastNames
     * supprime l'ancienne valeur oldValue et ajoute la nouvelle valeur qui est dasn field
     * @param field
     * @param oldValue
     */
    public void updateLastName( String value, String oldValue) {
        lastNames.remove(oldValue);
        lastNames.add(value);
    }


    public void updateOccupation(String value, String oldValue) {
        occupations.remove(oldValue);
        occupations.add(value);
    }

    public void updateNotary(String value, String oldValue) {
        notaries.remove(oldValue);
        notaries.add(value);
    }

    public void updatePlaces(String value, String oldValue) {
        places.remove(oldValue);
        places.add(value);
    }

    public void updateEventType(String value, String oldValue) {
        eventTypes.remove(oldValue);
        eventTypes.add(value);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Liste des valeurs exclues de la completion
    ///////////////////////////////////////////////////////////////////////////
    static final private String ExcludedFirstNameList = "ExcludedFirstNameList";
    static final private String ExcludedLastNameList = "ExcludedLastNameList";
    static final private String ExcludedOccupationList = "ExcludedOccupationList";
    static final private String ExcludedPlaceList = "ExcludedPlaceList";

    static public List<String> loadExcludeCompletion(CompletionType completionType) {
        ArrayList<String> excludedList = new ArrayList<String>();

        String preferenceList;
        switch( completionType ) {
            case firstName:
               preferenceList = ExcludedFirstNameList;
               break;
            case lastName:
               preferenceList = ExcludedLastNameList;
               break;
            case occupation:
               preferenceList = ExcludedOccupationList;
               break;
            case place:
               preferenceList = ExcludedPlaceList;
               break;
            default:
               return excludedList;
        }

        // je recupere la liste des valeurs exclus
        String exludedString = NbPreferences.forModule(ReleveTopComponent.class).get(
                    preferenceList,
                    "");
        try {
            StringTokenizer tokens = new StringTokenizer(exludedString, ";");
            int n = tokens.countTokens();
            for (int i = 0; i < n ; i++) {
                excludedList.add(tokens.nextToken());
            }
        } catch (Throwable t) {
            // ignore
        }
        return excludedList;
    }

    /**
     * enregistre les valeurs a exlure dans les preferences du module
     * @param excludeList
     * @param completionType
     */
    static public void saveExcludedCompletion(List<String> excludeList, CompletionType completionType) {
        String exludedString = "";
        
        String preferenceList;
        switch( completionType ) {
            case firstName:
               preferenceList = ExcludedFirstNameList;
               break;
            case lastName:
               preferenceList = ExcludedLastNameList;
               break;
            case occupation:
               preferenceList = ExcludedOccupationList;
               break;
            case place:
               preferenceList = ExcludedPlaceList;
               break;default:
               return;
        }
        for (Iterator<String> it = excludeList.iterator(); it.hasNext(); ) {
            exludedString += it.next().trim() + ";" ;
        }

        NbPreferences.forModule(ReleveTopComponent.class).put(
                   preferenceList,
                   exludedString);

    }

    /**
     * refraichit la liste des valeurs exclure en rechargeant la liste à partir
     * des preferences du module
     */
    public void refreshExcludeCompletion(CompletionType completionType) {
        switch( completionType ) {
            case firstName:
               firstNames.loadExclude();
               break;
            case lastName:
               lastNames.loadExclude();
               break;
            case occupation:
               occupations.loadExclude();
               break;
            case place:
               places.loadExclude();
               break;
            default:
        }
    }

    /**
     * Liste contenant les prenoms avec les "Field" ou ils sont utilises
     */
    private class FirstNameCompletionSet extends CompletionSet {
        public FirstNameCompletionSet() {
            loadExclude();
        }

        /**
         * charge les valeurs a exclure
         */
        private void loadExclude() {
            setExclude(loadExcludeCompletion(CompletionType.firstName));
            fireIncludedUpdateListener();
        }
        
        
        public void add(String firstName, String sex, boolean fireListeners) {
            super.add(firstName, fireListeners);
            updateFirstNameSex(null, null, firstName, sex);
        }

        /**
         * ajoute le prenom dans la liste de completion
         * et met a jour le sexe correspondant au prénom
         * @param firstNameField
         * @param sexField
         */
        public void add( String firstName, String sex) {
            super.add(firstName, true);
            updateFirstNameSex(null, null, firstName, sex);
        }
        
        public void remove(String firstName, String sex, boolean fireListeners) {
            super.remove(firstName, fireListeners );
            updateFirstNameSex(firstName, sex, null, null);
        }

        private void remove(String firstName, String sex) {
            remove(firstName, sex, true);            
        }

        @Override
        public void removeAll() {
            super.removeAll();
        }
    }


    /**
     * Liste contenant les noms avec les "Field" ou ils sont utilises
     */
    private class LastNameCompletionSet extends CompletionSet {

        public LastNameCompletionSet() {
            loadExclude();
        }
        
        /**
         * charge les valeurs a exclure
         */
        private void loadExclude() {
            setExclude(loadExcludeCompletion(CompletionType.lastName));
            fireIncludedUpdateListener();
        }
        
        public void add(String lastName, boolean fireListeners) {
            super.add(lastName, fireListeners);
        }

        public void add(String lastName) {
            super.add(lastName, true);
        }

        private void remove(String lastName, boolean fireListeners) {
            super.remove(lastName, fireListeners);
        }

        private void remove(String lastName) {
            super.remove(lastName, true);
        }

        @Override
        public void removeAll() {
            super.removeAll();
        }
    }


    /**
     * Liste contenant les lieux avec les "Field" ou ils sont utilises
     */
    private class OccupationCompletionSet extends CompletionSet {

        public OccupationCompletionSet() {
            loadExclude();
        }

        /**
         * charge les valeurs a exclure
         */
        private void loadExclude() {
            setExclude(loadExcludeCompletion(CompletionType.occupation));
            fireIncludedUpdateListener();
        }

        public void add(String occupation, boolean fireListeners) {
            super.add(occupation, fireListeners);
        }

        public void add(String occupation) {
            super.add(occupation, true);
        }

        private void remove(String occupation, boolean fireListeners) {
            super.remove(occupation, fireListeners);
        }

        private void remove(String occupation) {
            super.remove(occupation, true);
        }

        @Override
        public void removeAll() {
            super.removeAll();
        }
    }

    /**
     * Liste contenant les notaires avec les "Field" ou ils sont utilises
     */
    private class NotaryCompletionSet extends CompletionSet {

        public NotaryCompletionSet() {
        }

        public void add(String notary) {
            super.add(notary, true);
        }

        private void remove(String notary, boolean fireListeners) {
            super.remove(notary, fireListeners);
        }

        private void remove(String notary) {
            super.remove(notary, true);
        }

        @Override
        public void removeAll() {
            super.removeAll();
        }
    }


    /**
     * Liste contenant les lieux avec les "Field" ou ils sont utilises
     */
    private class PlaceCompletionSet extends CompletionSet {
        public PlaceCompletionSet() {
            loadExclude();
        }

        /**
         * charge les valeurs a exclure
         */
        private void loadExclude() {
            setExclude(loadExcludeCompletion(CompletionType.place));
            fireIncludedUpdateListener();
        }

        public void add(String place, boolean fireListeners) {
            super.add(place, fireListeners);
        }

        public void add(String place) {
            super.add(place, true);
        }

         private void remove(String place, boolean fireListeners) {
            super.remove(place, fireListeners);
        }

        private void remove(String place) {
            super.remove(place, true);
        }

        @Override
        public void removeAll() {
            super.removeAll();
        }
    }


    /**
     * Liste des valeurs avec les "Field" de référence
     */
//    private class CompletionSet<REF> {
//
//        // liste TreeMap triée sur la clé
//        private Map<String, Set<REF>> key2references = new TreeMap<String, Set<REF>>();
//        // liste des listeners a notifier quand on change une valeur dans key2references
//        private List<CompletionListener> keysListener = new ArrayList<CompletionListener>();
//        private TreeSet<String> excluded = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
//        private TreeSet<String> included = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
//
//
//        /**
//         * Constructor - uses a TreeMap that keeps
//         * keys sorted by their natural order
//         */
//        public CompletionSet() {
//        }
//
//        /**
//         * Returns the references for a given key
//         */
//        public Set<REF> getReferences(String key) {
//            // null is ignored
//            if (key == null) {
//                return new HashSet<REF>();
//            }
//            // lookup
//            Set<REF> references = key2references.get(key);
//            if (references == null) {
//                return new HashSet<REF>();
//            }
//            // return references
//            return references;
//        }
//
//        /**
//         * Returns the number of reference for given key
//         */
//        public int getSize(String key) {
//            // null is ignored
//            if (key == null) {
//                return 0;
//            }
//            // lookup
//            Set<REF> references = key2references.get(key);
//            if (references == null) {
//                return 0;
//            }
//            // done
//            return references.size();
//        }
//
//        /**
//         * Add a key and its reference
//         * @return whether the reference was actually added (could have been known already)
//         */
//        private boolean add(String key, REF reference) {
//            return add(key, reference, true);
//        }
//
//        /**
//         * Add a key and its reference
//         * @return whether the reference was actually added (could have been known already)
//         */
//        private boolean add(String key, REF reference, boolean fireListeners) {
//            if (key == null || key.equals("")) {
//                return false;
//            }
//            // je verifie si une reference de cette valeur existe déjà dans key2references
//            Set<REF> references = key2references.get(key);
//            if (references == null) {
//                // j'ajoute une reference dans key2references
//                references = new HashSet<REF>();
//                key2references.put(key, references);
//            }
//
//            // j'ajoute la valeur dans la reference
//            boolean addedInReference  = references.add(reference);
//            if (addedInReference) {
//                if ( !excluded.contains(key) && !included.contains(key))  {
//                    // j'ajoute la valeur dans included
//                    boolean added = included.add(key);
//                    if (added) {
//                        if (fireListeners) {
//                            fireIncludedUpdateListener();
//                        }
//                    }
//                }
//            }
//
//            return addedInReference;
//        }
//
//        /**
//         * initialise les valeurs inclues et exclues
//         * @param newExcluded
//         */
//        protected void setExclude(List<String> newExcluded) {
//            excluded = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
//            excluded.addAll(newExcluded);
//            included = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
//            for (String key : key2references.keySet()) {
//                if (!excluded.contains(key)) {
//                    included.add(key);
//                }
//            }
//        }
//
//        private boolean remove(String key, REF reference) {
//            return remove(key, reference, true);
//
//        }
//
//        /**
//         * Remove a reference for given key
//         */
//        private boolean remove(String key, REF reference, boolean fireListeners) {
//            if (key == null || key.equals("")) {
//                return false;
//            }
//            // je verifie si la valeur existe dans key2references
//            Set<REF> references = key2references.get(key);
//            if (references == null) {
//                return false;
//            }
//            // remove
//            boolean removedFromFreference = references.remove(reference);
//            if (removedFromFreference) {
//                // remove value
//                if (references.isEmpty()) {
//                    // s'il n'y a plus aucune reference pour cette valeur
//                    // je la supprime de key2references et de includeds
//                    key2references.remove(key);
//                    boolean removedFromIncluded = included.remove(key);
//                    if ( removedFromIncluded ) {
//                        if(fireListeners) {
//                            fireIncludedUpdateListener();
//                        }
//                    }
//
//                }
//            }
//
//            return removedFromFreference;
//        }
//
//        protected void removeAll() {
//            key2references.clear();
//        }
//
//        /**
//         * retourne la liste des valeurs dans l'ordre alphabetique
//         */
//        public List<String> getKeys(IncludeFilter filter) {
//            switch (filter) {
//                case EXCLUDED :
//                    return new ArrayList<String>(excluded);
//                case INCLUDED:
//                    return new ArrayList<String>(included);
//                default:
//                    return new ArrayList<String>(key2references.keySet());
//            }
//        }
//
//        /**
//         * ajoute un listener de la liste de completion
//         */
//        protected void addListener(CompletionListener listener) {
//            keysListener.add(listener);
//        }
//
//        /**
//         * supprime un listener de la liste de completion
//         */
//        protected void removeListener(CompletionListener listener) {
//            keysListener.remove(listener);
//        }
//
//        /**
//         * notify les listeners d'une mise a jour des items iindluded
//         */
//        protected void fireIncludedUpdateListener() {
//            final List<String> keys = new ArrayList<String>(included);
//            SwingUtilities.invokeLater(new Runnable() {
//
//                @Override
//                public void run() {
//                    for (CompletionListener listener : keysListener) {
//                        listener.includedKeyUpdated(keys);
//                    }
//                }
//            });
//
//        }
//    }

    /**
     * Liste des valeurs avec les "Field" de référence
     */
    private class CompletionSet {

        // liste TreeMap triée sur la clé
        private final Map<String, Integer> key2references = new TreeMap<String, Integer>();
        // liste des listeners a notifier quand on change une valeur dans key2references
        private final List<CompletionListener> keysListener = new ArrayList<CompletionListener>();
        private TreeSet<String> excluded = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        private TreeSet<String> included = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);


        /**
         * Constructor - uses a TreeMap that keeps
         * keys sorted by their natural order
         */
        public CompletionSet() {
        }

        /**
         * Returns the references for a given key
         */
        public int getReferences(String key) {
            // null is ignored
            if (key == null) {
                return 0;
            }
            // lookup
            Integer references = key2references.get(key);
            if (references == null) {
                return 0;
            }
            // return references
            return references;
        }

        /**
         * Returns the number of reference for given key
         */
        public int getSize(String key) {
            // null is ignored
            if (key == null) {
                return 0;
            }
            // lookup
            Integer  references = key2references.get(key);
            if (references == null) {
                return 0;
            }
            // done
            return references;
        }

        /**
         * Add a key and its reference
         * @return whether the reference was actually added (could have been known already)
         */
        private boolean add(String key) {
            return add(key, true);
        }

        /**
         * Add a key and its reference
         * @return whether the reference was actually added (could have been known already)
         */
        private boolean add(String key, boolean fireListeners) {
            if (key == null || key.equals("")) {
                return false;
            }
            // je verifie si une reference de cette valeur existe déjà dans key2references
            Integer references = key2references.get(key);
            boolean addedInReference ;
            if (references == null) {
                // j'ajoute une reference dans key2references
                references = 1;
                key2references.put(key, references);

                // j'ajoute la valeur dans la reference
                if (!excluded.contains(key) && !included.contains(key)) {
                    // j'ajoute la valeur dans included
                    boolean added = included.add(key);
                    if (added) {
                        if (fireListeners) {
                            fireIncludedUpdateListener();
                        }
                    }
                }
                addedInReference = true;

            } else {
                key2references.put(key, references +1);
                addedInReference = false;
            }

            return addedInReference;
        }

        /**
         * initialise les valeurs inclues et exclues
         * @param newExcluded
         */
        protected void setExclude(List<String> newExcluded) {
            excluded = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
            excluded.addAll(newExcluded);
            included = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
            for (String key : key2references.keySet()) {
                if (!excluded.contains(key)) {
                    included.add(key);
                }
            }
        }

        private boolean remove(String key) {
            return remove(key,true);

        }

        /**
         * Remove a reference for given key
         */
        private boolean remove(String key, boolean fireListeners) {
            if (key == null || key.equals("")) {
                return false;
            }
            // je verifie si la valeur existe dans key2references
            Integer references = key2references.get(key);
            if (references == null) {
                return false;
            }
            // remove
            boolean removedFromFreference;
            if (references  <= 1 ) {
                // s'il n'y a plus aucune reference pour cette valeur
                // je la supprime de key2references et de includeds
                key2references.remove(key);
                boolean removedFromIncluded = included.remove(key);
                if (removedFromIncluded) {
                    if (fireListeners) {
                        fireIncludedUpdateListener();
                    }
                }
                removedFromFreference = true;
            } else {
                key2references.put(key,references -1);
                removedFromFreference = false;
            }

            return removedFromFreference;
        }

        protected void removeAll() {
            key2references.clear();
        }

        /**
         * retourne la liste des valeurs dans l'ordre alphabetique
         */
        public List<String> getKeys(IncludeFilter filter) {
            switch (filter) {
                case EXCLUDED :
                    return new ArrayList<String>(excluded);
                case INCLUDED:
                    return new ArrayList<String>(included);
                default:
                    return new ArrayList<String>(key2references.keySet());
            }
        }

        /**
         * ajoute un listener de la liste de completion
         */
        protected void addListener(CompletionListener listener) {
            keysListener.add(listener);
        }

        /**
         * supprime un listener de la liste de completion
         */
        protected void removeListener(CompletionListener listener) {
            keysListener.remove(listener);
        }

        /**
         * notify les listeners d'une mise a jour des items included
         */
        protected void fireIncludedUpdateListener() {
            final List<String> keys = new ArrayList<String>(included);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    for (CompletionListener listener : keysListener) {
                        listener.includedKeyUpdated(keys);
                    }
                }
            });

        }
    }
}
