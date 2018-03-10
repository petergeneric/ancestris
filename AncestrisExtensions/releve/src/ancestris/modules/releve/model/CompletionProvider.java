package ancestris.modules.releve.model;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.releve.ReleveTopComponent;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.utils.CompareString;
import genj.gedcom.*;
import java.util.*;
import org.openide.util.NbPreferences;

/**
 *
 * @author Michels
 */
public class CompletionProvider {
    private final CompletionSet firstNames = new CompletionSet(CompletionType.firstName);
    private final CompletionSet lastNames = new CompletionSet(CompletionType.lastName);
    private final CompletionSet occupations = new CompletionSet(CompletionType.occupation);
    private final CompletionSet notaries = new CompletionSet(CompletionType.notary);
    private final CompletionSet places = new CompletionSet(CompletionType.place);
    private final CompletionSet eventTypes = new CompletionSet(CompletionType.eventType);

    private final HashMap<String, Integer> firstNameSex = new HashMap<String, Integer>();

    public static enum CompletionType {
        eventType,
        firstName,
        lastName,
        notary,
        occupation,
        place
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
        // EventType
        eventTypes.add(record.getFieldValue(FieldType.eventType));

        // FirstName
        firstNames.add(record.getFieldValue(FieldType.indiFirstName));
        firstNames.add(record.getFieldValue(FieldType.indiMarriedFirstName));
        firstNames.add(record.getFieldValue(FieldType.indiFatherFirstName));
        firstNames.add(record.getFieldValue(FieldType.indiMotherFirstName));
        addFirstNameSex(record.getFieldValue(FieldType.indiFirstName), record.getFieldValue(FieldType.indiSex));
        addFirstNameSex(record.getFieldValue(FieldType.indiMarriedFirstName), FieldSex.getOppositeString(record.getFieldValue(FieldType.indiSex)));
        addFirstNameSex(record.getFieldValue(FieldType.indiFatherFirstName), FieldSex.MALE_STRING);
        addFirstNameSex(record.getFieldValue(FieldType.indiMotherFirstName), FieldSex.FEMALE_STRING);

        firstNames.add(record.getFieldValue(FieldType.wifeFirstName));
        firstNames.add(record.getFieldValue(FieldType.wifeMarriedFirstName));
        firstNames.add(record.getFieldValue(FieldType.wifeFatherFirstName));
        firstNames.add(record.getFieldValue(FieldType.wifeMotherFirstName));
        addFirstNameSex(record.getFieldValue(FieldType.wifeFirstName), record.getFieldValue(FieldType.wifeSex));
        addFirstNameSex(record.getFieldValue(FieldType.wifeMarriedFirstName), FieldSex.getOppositeString(record.getFieldValue(FieldType.wifeSex)));
        addFirstNameSex(record.getFieldValue(FieldType.wifeFatherFirstName), FieldSex.MALE_STRING);
        addFirstNameSex(record.getFieldValue(FieldType.wifeMotherFirstName), FieldSex.FEMALE_STRING);
        
        firstNames.add(record.getFieldValue(FieldType.witness1FirstName));
        firstNames.add(record.getFieldValue(FieldType.witness2FirstName));
        firstNames.add(record.getFieldValue(FieldType.witness3FirstName));
        firstNames.add(record.getFieldValue(FieldType.witness4FirstName));


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

    }

    /**
     * supprime les prenoms, lee noms et les professions des listes de completion
     * @param record
     */
    protected void removeRecord(final Record record) {
        // EventType
        eventTypes.remove(record.getFieldValue(FieldType.eventType));
        
        // FirstName
        firstNames.remove(record.getFieldValue(FieldType.indiFirstName));
        firstNames.remove(record.getFieldValue(FieldType.indiMarriedFirstName));
        firstNames.remove(record.getFieldValue(FieldType.indiFatherFirstName));
        firstNames.remove(record.getFieldValue(FieldType.indiMotherFirstName));
        removeFirstNameSex(record.getFieldValue(FieldType.indiFirstName), record.getFieldValue(FieldType.indiSex));
        removeFirstNameSex(record.getFieldValue(FieldType.indiMarriedFirstName), FieldSex.getOppositeString(record.getFieldValue(FieldType.indiSex)));
        removeFirstNameSex(record.getFieldValue(FieldType.indiFatherFirstName), FieldSex.MALE_STRING);
        removeFirstNameSex(record.getFieldValue(FieldType.indiMotherFirstName), FieldSex.FEMALE_STRING);
        
        firstNames.remove(record.getFieldValue(FieldType.wifeFirstName));
        firstNames.remove(record.getFieldValue(FieldType.wifeMarriedFirstName));
        firstNames.remove(record.getFieldValue(FieldType.wifeFatherFirstName));
        firstNames.remove(record.getFieldValue(FieldType.wifeMotherFirstName));
        removeFirstNameSex(record.getFieldValue(FieldType.wifeFirstName), record.getFieldValue(FieldType.wifeSex));
        removeFirstNameSex(record.getFieldValue(FieldType.wifeMarriedFirstName), FieldSex.getOppositeString(record.getFieldValue(FieldType.wifeSex)));
        removeFirstNameSex(record.getFieldValue(FieldType.wifeFatherFirstName), FieldSex.MALE_STRING);
        removeFirstNameSex(record.getFieldValue(FieldType.wifeMotherFirstName), FieldSex.FEMALE_STRING);
        
        firstNames.remove(record.getFieldValue(FieldType.witness1FirstName));
        firstNames.remove(record.getFieldValue(FieldType.witness2FirstName));
        firstNames.remove(record.getFieldValue(FieldType.witness3FirstName));
        firstNames.remove(record.getFieldValue(FieldType.witness4FirstName));

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
    public void addFirstNameSex(String firstName, String sex) {

        if (firstName != null && ! firstName.isEmpty() && sex != null) {
            int count = firstNameSex.containsKey(firstName) ? firstNameSex.get(firstName) : 0;
            if (sex.equals(FieldSex.MALE_STRING)) {
                firstNameSex.put(firstName, count + 1);
            } else if (sex.equals(FieldSex.FEMALE_STRING)) {
                firstNameSex.put(firstName, count - 1);
            }
        }
    }
    
    public void removeFirstNameSex(String oldFirstName, String oldSex) {
        if (oldFirstName != null && ! oldFirstName.isEmpty() && oldSex != null ) {
            int count = firstNameSex.containsKey(oldFirstName) ? firstNameSex.get(oldFirstName) : 0;
            if (oldSex.equals(FieldSex.MALE_STRING)) {
                firstNameSex.put(oldFirstName, count - 1);
            } else  if (oldSex.equals(FieldSex.FEMALE_STRING)) {
                firstNameSex.put(oldFirstName, count + 1);
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
            String firstName = indi.getFirstName().replaceAll(",", "");
            firstNames.add( firstName );
            addFirstNameSex(firstName, FieldSex.convertValue(indi.getSex()));
        }

        for ( String lastName : PropertyName.getLastNames(gedcom, false)) {
            lastNames.add(lastName);
        }
        for ( String occupation : PropertyChoiceValue.getChoices(gedcom, "OCCU", false)) {
            occupations.add(occupation);
        }
        for ( String place : PropertyChoiceValue.getChoices(gedcom, "PLAC", false)) {
            places.add(place);
        }

    }
    
    ///////////////////////////////////////////////////////////////////////////
    // accesseurs aux listes de completion
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * retourne les prénoms triées par ordre alphabétique
     */
    public CompletionSource getFirstNames() {
       return firstNames;
    }

    /**
     * retourne les noms triées par ordre alphabétique
     */
    public CompletionSource getLastNames() {
        return lastNames;
    }

    /**
     * retourne les professions triées par ordre alphabétique
     */
    public CompletionSource getOccupations() {
       return occupations;
    }

    /**
     * retourne les professions triées par ordre alphabétique
     */
    public CompletionSource getNotaries() {
       return notaries;
    }

    /**
     * retourne les tags des types d'evenement triées par ordre alphabétique
     */
    public CompletionSource getEventTypes() {
        return eventTypes;
    }

    /**
     * retourne les lieux triées par ordre alphabétique
     */
    public CompletionSource getPlaces() {
        return places;
    }


    /**
     * met a jour la liste de completion firstNames
     * supprime l'ancienne valeur oldValue et ajoute la nouvelle valeur qui est dans field
     * @param firstNameField
     * @param oldFirstName
     */
    public void updateFirstName( String firstName, String sex, String oldFirstName, String oldSex) {
        firstNames.remove(oldFirstName);
        removeFirstNameSex(oldFirstName, oldSex);
        firstNames.add(firstName);
        addFirstNameSex(firstName, sex);
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
     * refraichit la liste des valeurs exclues en rechargeant la liste à partir
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
    
    static public interface CompletionSource {
        public List<String> getAll();
        public List<String> getExcluded();
        public List<String> getIncluded();  
        public Locale getLocale();
    }

    /**
     * ajout de la methote put() pour vérifier que l'élement n'existe pas déjà 
     * puis trier les elements au fur et à mesure q'ils qu'ils sont ajoutés . 
     * 
     * Environ 5 fois plus rapide que :
     *    if( ! list.contains(key) )
     *      list.add(key)
     *      Collection.sort(list)
     *      index = list.indexOf(key)
     *  
     */
    static class SortedList extends ArrayList<String> {
        private static final int INSERTIONSORT_THRESHOLD = 7;
        
        public int put(String  newElement ) {
            int index = insert(newElement, 0, this.size());            
            return index;
        }
        
        private int insert(String newElement, int low, int high ) {
            int lentgh = high - low;
            if (lentgh >= INSERTIONSORT_THRESHOLD) {
                // il y a 7 élements ou plus dans la liste  
                // recherche par dichotomie (appels recursifs)
                // voir l'algo dans le source de java.util.Arrays.mergeSort(...)
                int mid = (low + high) >>> 1;
                int result = CompareString.compareStringUTF8(this.get(mid), newElement);
                if( result > 0) {
                    return insert(newElement, low, mid);
                } else if (result < 0) {                
                    return insert(newElement, mid, high);
                } else {
                    return -1;
                }
            } else if (lentgh == 0) {
                 //il n'y a pas d'element dans la liste 
                this.add(newElement);
                return 0;
                
            } else {
                // il y a moins de 7 éléments dans la liste
                // recherche lineaire 
                int index;
                for (index=low; index<high; index++) {
                    int result = CompareString.compareStringUTF8(this.get(index), newElement);
                    if( result < 0) {
                        //newElement est plus grand : 
                    } else if (result > 0) {
                        //newElement est plus petit
                        this.add(index, newElement);
                        return index;
                    } else {
                        //newElement est égal : l'élement est déjà dasn la liste
                        return -1;
                    }
                }
                if( index >= this.size() ) {
                    this.add(newElement); 
                    return index;
                } else {
                    this.add(index,newElement);    
                    return index;
                }
            }                                  
        }      
    }

    /**
     * Liste des valeurs avec les "Field" de référence
     */
    private class CompletionSet implements CompletionSource {
        CompletionType completionType;
        
        // liste TreeMap triée sur la clé
        private final Map<String, Integer> key2references = new TreeMap<String, Integer>();
        private final TreeSet<String> excluded = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        private final SortedList  included = new SortedList();
       
        
        /**
         * Constructor - uses a TreeMap that keeps
         * keys sorted by their natural order
         */
        public CompletionSet(CompletionType completionType) {
            this.completionType = completionType;
            loadExclude();
        }
        
        /**
         * charge les valeurs a exclure
         */
        private void loadExclude() {
            setExclude(loadExcludeCompletion(completionType));
        }
        
        /**
         * Add a key and its reference
         * @return whether the reference was actually added (could have been known already)
         */
        private boolean add(String key) {
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
                if (!excluded.contains(key) ) {
                    // j'ajoute la valeur dans included
                    included.put(key);
                }
                addedInReference = true;

            } else {
                key2references.put(key, references +1);
                addedInReference = false;
            }

            return addedInReference;
        }

        /**
         * Remove a reference for given key
         */
        private boolean remove(String key) {
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
                // je la supprime de key2references 
                key2references.remove(key);
                // je la supprime de included
                included.remove(key);
                removedFromFreference = true;
            } else {
                key2references.put(key,references -1);
                removedFromFreference = false;
            }

            return removedFromFreference;
        }

        protected void removeAll() {
            key2references.clear();
            included.clear();
        }

        /**
         * initialise les valeurs inclues et exclues
         * @param newExcluded
         */
        protected void setExclude(List<String> newExcluded) {
            // je charge la nouvelle le liste des valeurs à exclure
            excluded.clear();
            excluded.addAll(newExcluded);
            // je reconstitue la liste included
            included.clear();
            for (String key : key2references.keySet()) {
                if (!excluded.contains(key)) {
                    included.put(key);
                }
            }
        }

        
        ////////////////////////////////////////////////////////////////////////
        // implements CompletionSource
        ////////////////////////////////////////////////////////////////////////
        @Override
        public List<String> getAll() {
            return new ArrayList<String>(key2references.keySet());
        }
        
        @Override
        public List<String> getExcluded() {
            return new ArrayList<String>(excluded);
        }
                
        @Override
        public List<String> getIncluded() {
            return included;
        }

        @Override
        public Locale getLocale() {
            return locale;
        }

    }
}
