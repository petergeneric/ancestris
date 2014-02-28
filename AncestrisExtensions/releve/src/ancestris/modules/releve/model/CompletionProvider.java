package ancestris.modules.releve.model;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.releve.ReleveTopComponent;
import genj.gedcom.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.openide.util.NbPreferences;

/**
 *
 * @author Michels
 */
public class CompletionProvider {
    private FirstNameCompletionSet firstNames = new FirstNameCompletionSet();
    private LastNameCompletionSet lastNames = new LastNameCompletionSet();
    private OccupationCompletionSet occupations = new OccupationCompletionSet();
    private NotaryCompletionSet notaries = new NotaryCompletionSet();
    private PlaceCompletionSet places = new PlaceCompletionSet();
    private CompletionSet eventTypes = new CompletionSet();

    private HashMap<String, Integer> firstNameSex = new HashMap<String, Integer>();

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
        firstNames.add(record.getIndiFirstName(), record.getIndiSex().getValue());
        firstNames.add(record.getIndiMarriedFirstName(), record.getIndiSex().getOppositeString());
        firstNames.add(record.getIndiFatherFirstName(), FieldSex.MALE_STRING);
        firstNames.add(record.getIndiMotherFirstName(), FieldSex.FEMALE_STRING);

        firstNames.add(record.getWifeFirstName(), record.getWifeSex() != null ? record.getWifeSex().getValue() : null );
        firstNames.add(record.getWifeMarriedFirstName(), record.getWifeSex() != null ? record.getWifeSex().getOppositeString() : null);
        firstNames.add(record.getWifeFatherFirstName(), FieldSex.MALE_STRING);
        firstNames.add(record.getWifeMotherFirstName(), FieldSex.FEMALE_STRING);

        firstNames.add(record.getWitness1FirstName(), null);
        firstNames.add(record.getWitness2FirstName(), null);
        firstNames.add(record.getWitness3FirstName(), null);
        firstNames.add(record.getWitness4FirstName(), null);

        // LastName
        lastNames.add(record.getIndiLastName());
        lastNames.add(record.getIndiMarriedLastName());
        lastNames.add(record.getIndiFatherLastName());
        lastNames.add(record.getIndiMotherLastName());

        lastNames.add(record.getWifeLastName());
        lastNames.add(record.getWifeMarriedLastName());
        lastNames.add(record.getWifeFatherLastName());
        lastNames.add(record.getWifeMotherLastName());

        lastNames.add(record.getWitness1LastName());
        lastNames.add(record.getWitness2LastName());
        lastNames.add(record.getWitness3LastName());
        lastNames.add(record.getWitness4LastName());
        
        // Indi Occupation
        occupations.add(record.getIndiOccupation());
        occupations.add(record.getIndiMarriedOccupation());
        occupations.add(record.getIndiFatherOccupation());
        occupations.add(record.getIndiMotherOccupation());
        // Wife Occupation
        occupations.add(record.getWifeOccupation());
        occupations.add(record.getWifeMarriedOccupation());
        occupations.add(record.getWifeFatherOccupation());
        occupations.add(record.getWifeMotherOccupation());
        // witness Occupation
        occupations.add(record.getWitness1Occupation());
        occupations.add(record.getWitness2Occupation());
        occupations.add(record.getWitness3Occupation());
        occupations.add(record.getWitness4Occupation());

        notaries.add(record.getNotary());

        places.add(record.getIndiBirthPlace());
        places.add(record.getIndiResidence());
        places.add(record.getIndiMarriedResidence());
        places.add(record.getIndiFatherResidence());
        places.add(record.getIndiMotherResidence());

        places.add(record.getWifeBirthPlace());
        places.add(record.getWifeResidence());
        places.add(record.getWifeMarriedResidence());
        places.add(record.getWifeFatherResidence());
        places.add(record.getWifeMotherResidence());

        // EventType
        if ( record.getEventType()!= null && record.getEventType().isEmpty()== false) {
            eventTypes.add(record.getEventType().getName());
        }
    }

    /**
     * supprime les prenoms, lee noms et les professions des listes de completion
     * @param record
     */
    protected void removeRecord(final Record record) {

        // FirstName
        firstNames.remove(record.getIndiFirstName(), record.getIndiSex().getValue());
        firstNames.remove(record.getIndiMarriedFirstName(), record.getIndiSex().getOppositeString());
        firstNames.remove(record.getIndiFatherFirstName(), FieldSex.MALE_STRING);
        firstNames.remove(record.getIndiMotherFirstName(), FieldSex.FEMALE_STRING);

        firstNames.remove(record.getWifeFirstName(), record.getWifeSex() != null ? record.getWifeSex().getValue() : null);
        firstNames.remove(record.getWifeMarriedFirstName(), record.getWifeSex() != null ? record.getWifeSex().getOppositeString() : null);
        firstNames.remove(record.getWifeFatherFirstName(), FieldSex.MALE_STRING);
        firstNames.remove(record.getWifeMotherFirstName(), FieldSex.FEMALE_STRING);

        firstNames.remove(record.getWitness1FirstName(), null);
        firstNames.remove(record.getWitness2FirstName(), null);
        firstNames.remove(record.getWitness3FirstName(), null);
        firstNames.remove(record.getWitness4FirstName(), null);

        // LastName
        lastNames.remove(record.getIndiLastName());
        lastNames.remove(record.getIndiMarriedLastName());
        lastNames.remove(record.getIndiFatherLastName());
        lastNames.remove(record.getIndiMotherLastName());

        lastNames.remove(record.getWifeLastName());
        lastNames.remove(record.getWifeMarriedLastName());
        lastNames.remove(record.getWifeFatherLastName());
        lastNames.remove(record.getWifeMotherLastName());

        lastNames.remove(record.getWitness1LastName());
        lastNames.remove(record.getWitness2LastName());
        lastNames.remove(record.getWitness3LastName());
        lastNames.remove(record.getWitness4LastName());
        
        // Indi Occupation
        occupations.remove(record.getIndiOccupation());
        occupations.remove(record.getIndiMarriedOccupation());
        occupations.remove(record.getIndiFatherOccupation());
        occupations.remove(record.getIndiMotherOccupation());
        // Wife Occupation
        occupations.remove(record.getWifeOccupation());
        occupations.remove(record.getWifeMarriedOccupation());
        occupations.remove(record.getWifeFatherOccupation());
        occupations.remove(record.getWifeMotherOccupation());
        // witness Occupation
        occupations.remove(record.getWitness1Occupation());
        occupations.remove(record.getWitness2Occupation());
        occupations.remove(record.getWitness3Occupation());
        occupations.remove(record.getWitness4Occupation());

        notaries.remove(record.getNotary());

        // places
        //Indi places
        places.remove(record.getIndiBirthPlace());
        places.remove(record.getIndiResidence());
        places.remove(record.getIndiMarriedResidence());
        places.remove(record.getIndiFatherResidence());
        places.remove(record.getIndiMotherResidence());
        // wife places
        places.remove(record.getWifeBirthPlace());
        places.remove(record.getWifeResidence());
        places.remove(record.getWifeMarriedResidence());
        places.remove(record.getWifeFatherResidence());
        places.remove(record.getWifeMotherResidence());

        // EventType
        if ( record.getEventType()!= null && record.getEventType().isEmpty()== false) {
            eventTypes.remove(record.getEventType().getName());
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
    public int getFirstNameSex(String firstName) {
        int count = 0;
        // je cherche parmi les releves
        Integer releveCount = firstNameSex.get(firstName);
        if (releveCount != null) {
            count += releveCount;
        }

        if (count > 0 ) {
            return FieldSex.MALE;
        } else if (count < 0 ) {
            return FieldSex.FEMALE;
        } else {
            return FieldSex.UNKNOWN;
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
     * supprime l'ancienne valeur oldValue et ajoute la nouvelle valeur qui est dasn field
     * @param firstNameField
     * @param oldFirstName
     */
    public void updateFirstName( Field firstNameField, String sex, String oldFirstName, String oldSex) {
        firstNames.remove(oldFirstName, oldSex, true);
        firstNames.add(firstNameField, sex);
    }

    /**
     * met a jour la liste de completion lastNames
     * supprime l'ancienne valeur oldValue et ajoute la nouvelle valeur qui est dasn field
     * @param field
     * @param oldValue
     */
    public void updateLastName( Field field, String oldValue) {
        lastNames.remove(oldValue, true);
        lastNames.add(field);
    }


    public void updateOccupation(Field field, String oldValue) {
        occupations.remove(oldValue, true);
        occupations.add(field);
    }

    public void updateNotary(FieldNotary field, String oldValue) {
        notaries.remove(oldValue, true);
        notaries.add(field);
    }

    public void updatePlaces(Field field, String oldValue) {
        places.remove(oldValue, true);
        places.add(field);
    }

    public void updateEventType(FieldEventType field, String oldValue) {
        eventTypes.remove(oldValue);
        eventTypes.add(field.getName());
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
               return;
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
        public void add( Field firstNameField, String sex) {
            if ( firstNameField != null ) {
                super.add(firstNameField.toString(), true);
                updateFirstNameSex(null, null, firstNameField.toString(), sex);
            }
        }
        
        public void remove(String firstName, String sex, boolean fireListeners) {
            super.remove(firstName, fireListeners );
            updateFirstNameSex(firstName, sex, null, null);
        }

        private void remove(Field firstNameField, String sex) {
            if (firstNameField != null ) {
                super.remove(firstNameField.toString(), true);
                updateFirstNameSex(firstNameField.toString(), sex, null, null);
            }
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

        public void add(Field lastNameField) {
            if ( lastNameField != null && lastNameField.isEmpty()==false ) {
                super.add(lastNameField.toString(), true);
            }
        }

        private void remove(String lastName, boolean fireListeners) {
            super.remove(lastName, fireListeners);
        }

        private void remove(Field lastNameField) {
            if (lastNameField != null) {
                super.remove(lastNameField.toString(), true);
            }
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

        public void add( Field occupationField) {
            if ( occupationField != null ) {
                super.add(occupationField.toString(), true);
            }
        }

        private void remove(String occupation, boolean fireListeners) {
            super.remove(occupation, fireListeners);
        }

        private void remove(Field occupationField) {
            if (occupationField != null) {
                super.remove(occupationField.toString(), true);
            }
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

        public void add( FieldNotary notaryField) {
            if ( notaryField != null ) {
                super.add(notaryField.toString(), true);
            }
        }

        private void remove(String notary, boolean fireListeners) {
            super.remove(notary, fireListeners);
        }

        private void remove(FieldNotary notaryField) {
            if (notaryField != null) {
                super.remove(notaryField.toString(), true);
            }
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

        public void add( Field placeField) {
            if ( placeField != null) {
                super.add(placeField.toString(), true);
            }
        }

         private void remove(String place, boolean fireListeners) {
            super.remove(place, fireListeners);
        }

        private void remove(Field placeField) {
            if (placeField != null) {
                super.remove(placeField.toString(), true);
            }
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
        private Map<String, Integer> key2references = new TreeMap<String, Integer>();
        // liste des listeners a notifier quand on change une valeur dans key2references
        private List<CompletionListener> keysListener = new ArrayList<CompletionListener>();
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
