package ancestris.modules.releve.model;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.releve.ReleveTopComponent;
import ancestris.gedcom.GedcomFileListener;
import genj.gedcom.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.openide.util.NbPreferences;

/**
 *
 * @author Michels
 */
public class CompletionProvider implements GedcomFileListener {
    Gedcom completionGedcom;
    private FirstNameCompletionSet firstNames = new FirstNameCompletionSet();
    private LastNameCompletionSet lastNames = new LastNameCompletionSet();
    private OccupationCompletionSet occupations = new OccupationCompletionSet();
    private PlaceCompletionSet places = new PlaceCompletionSet();
    private CompletionSet<Field> eventTypes = new CompletionSet<Field>();

    private HashMap<String, Integer> firstNameSex = new HashMap<String, Integer>();
    private HashMap<String, Integer> gedcomFirstNameSex = new HashMap<String, Integer>();

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
            eventTypes.add(record.getEventType().getName(), record.getEventType());
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

//        updateFirstNameSex(record.getIndiFirstName(),       record.getIndiSex().getValue(), null, null);
//        updateFirstNameSex(record.getIndiMarriedFirstName(), record.getIndiSex().getOppositeString(), null, null);
//        updateFirstNameSex(record.getIndiFatherFirstName(), FieldSex.MALE_STRING, null, null);
//        updateFirstNameSex(record.getIndiMotherFirstName(), FieldSex.FEMALE_STRING, null, null);
//
//        updateFirstNameSex(record.getWifeFirstName(), record.getWifeSex() != null ? record.getWifeSex().getValue() : null, null, null);
//        updateFirstNameSex(record.getWifeMarriedFirstName(), record.getWifeSex() != null ? record.getWifeSex().getOppositeString() : null, null, null);
//        updateFirstNameSex(record.getWifeFatherFirstName(), FieldSex.MALE_STRING, null, null);
//        updateFirstNameSex(record.getWifeMotherFirstName(), FieldSex.FEMALE_STRING, null, null);

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
            eventTypes.remove(record.getEventType().getName(), record.getEventType());
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
        places.removeAll();
        eventTypes.removeAll();
        completionGedcom = null;
    }

//    protected void removeRecord(List<Record> recordList) {
//        for(Record record : recordList) {
//             removeRecord( record);
//        }
//    }

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
        // je cherche aussi parmi les individus du gedcom
        Integer gedcomCount = gedcomFirstNameSex.get(firstName);
        if (gedcomCount != null) {
            count += gedcomCount;
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

        if ( completionGedcom != null && completionGedcom.equals(gedcom)) {
            // rien a faire car c'est le meme gedcom
            return;
        }
        this.locale = gedcom.getLocale();
        this.completionGedcom = gedcom;

        // je cree un objet Field associé au Gedcom pour pouvoir le referencer
        // dans les listes de completion
        FieldGedcom gedcomField = new FieldGedcom();
        gedcomField.setValue(gedcom);


        // j'ajoute les prénoms, noms, professions et lieux du Gedcom dans les
        // listes de completion
        for ( Indi indi : gedcom.getIndis()) {
            firstNames.addGedcom(gedcomField, indi.getFirstName(), FieldSex.convertValue(indi.getSex()) );
        }

        for ( String lastName : PropertyName.getLastNames(gedcom, false)) {
            lastNames.addGedcom(gedcomField, lastName);
        }
        for ( String occupation : PropertyChoiceValue.getChoices(gedcom, "OCCU", false)) {
            occupations.addGedcom(gedcomField, occupation);
        }
        for ( String place : PropertyChoiceValue.getChoices(gedcom, "PLAC", false)) {
            places.addGedcom(gedcomField, place);
        }

        firstNames.fireListener();
        lastNames.fireListener();
        occupations.fireListener();
        places.fireListener();

        // je crée une statistique des sexes/ prénom pour les prénoms du gedcom
        for(Indi indi : gedcom.getIndis()) {
            String firstName = indi.getFirstName() ;
            if ( firstName != null && !firstName.isEmpty()) {
                int count = firstNameSex.containsKey(firstName) ? firstNameSex.get(firstName) : 0;
                int sex = indi.getSex();
                if (sex == FieldSex.MALE) {
                    gedcomFirstNameSex.put(firstName, count + 1);
                } else {
                    gedcomFirstNameSex.put(firstName, count - 1);
                }
            }
        }
    }

    /**
     * supprime les noms , prénoms et professions d'un fichier Gedcom
     * des listes de completion.
     * @param gedcom
     */
    protected void removeGedcomCompletion() {
        if (completionGedcom == null) {
            return;
        }
        this.locale = Locale.getDefault();

        FieldGedcom gedcomField = new FieldGedcom();
        gedcomField.setValue(completionGedcom);

        // je supprime les prénoms, noms, professions et lieux du Gedcom des
        // listes de completion
        for ( String firstName : firstNames.getKeys(IncludeFilter.ALL)) {
            firstNames.remove(gedcomField, firstName, null, false);
        }
        for ( String lastName : lastNames.getKeys(IncludeFilter.ALL)) {
            lastNames.remove(gedcomField, lastName, false);
        }
        for ( String occupation : occupations.getKeys(IncludeFilter.ALL)) {
            occupations.removeGedcom(gedcomField, occupation);
        }
        for ( String place : places.getKeys(IncludeFilter.ALL)) {
            places.removeGedcom(gedcomField, place);
        }

        firstNames.fireListener();
        lastNames.fireListener();
        occupations.fireListener();
        places.fireListener();
        
        // je vide la statistique des sexes/ prénom des prénoms du gedcom
        gedcomFirstNameSex.clear();

        this.completionGedcom = null;


    }

    /**
     * cette classe permet d'encapsuler la reference d'un  GEDCOM
     * dans un objet Field et de referencer les noms , prenoms et profession
     * dans les listes de completion
     */
    private class FieldGedcom extends Field {

        Gedcom gedcom = null;

        public FieldGedcom() {
        }

        @Override
        public String toString() {
            return gedcom.getName();
        }

        @Override
        public Object getValue() {
            return gedcom;
        }

        @Override
        public void setValue(Object value) {
            this.gedcom = (Gedcom) value;
        }

        @Override
        public boolean isEmpty() {
            return gedcom == null;
        }

        /**
         * comparateur utilisé en particulier par CompletionProvider
         * La valeur du champ contient un pointeur de Gedcom : gedcomField.setValue(gedcom);
         * @param other
         * @return
         */
        @Override
        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            if (!(other instanceof FieldGedcom)) {
                return false;
            } else {
                return ((FieldGedcom) other).gedcom.equals(gedcom);
            }
        }

        @Override
        public int hashCode() {
            return gedcom.hashCode();
        }
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
        if (oldFirstName != null && ! oldFirstName.isEmpty()) {
            firstNames.remove(firstNameField, oldFirstName, oldSex);
        }
        if ( firstNameField.toString().isEmpty()==false) {
            firstNames.add(firstNameField, sex);
        }
    }

    /**
     * met a jour la liste de completion lastNames
     * supprime l'ancienne valeur oldValue et ajoute la nouvelle valeur qui est dasn field
     * @param field
     * @param oldValue
     */
    public void updateLastName( Field field, String oldValue) {
        if (oldValue != null && ! oldValue.isEmpty()) {
            lastNames.remove(field, oldValue);
        }
        if ( field.toString().isEmpty()==false) {
            lastNames.add(field);
        }
    }


    public void updateOccupation(Field field, String oldValue) {
        if (oldValue != null && ! oldValue.isEmpty()) {
            occupations.remove(field, oldValue);
        }
        if ( field.toString().isEmpty()==false) {
            occupations.add(field);
        }
    }

    public void updatePlaces(Field field, String oldValue) {
        if (oldValue != null && ! oldValue.isEmpty()) {
            places.remove(field, oldValue);
        }
        if ( field.toString().isEmpty()==false) {
            places.add(field);
        }
    }

    public void updateEventType(FieldEventType field, String oldValue) {
        if (oldValue != null && ! oldValue.isEmpty()) {
            eventTypes.remove(oldValue, field);
        }
        if ( field.getName().isEmpty()==false) {
            eventTypes.add(field.getName(), field);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Liste des valeurs exclues de la completion
    ///////////////////////////////////////////////////////////////////////////
    static final private String ExcludedFirstNameList = "ExcludedFirstNameList";
    static final private String ExcludedLastNameList = "ExcludedLastNameList";
    static final private String ExcludedOccupationList = "ExcludedOccupationList";
    static final private String ExcludedPlaceList = "ExcludedPlaceList";

    static public List<String> loadExcludeCompletion(CompletionType completionType) {
        ArrayList<String> excluded = new ArrayList<String>();

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
               return excluded;
        }

        // je recupere la liste des valeurs exclus
        String exludedString = NbPreferences.forModule(ReleveTopComponent.class).get(
                    preferenceList,
                    "");
        try {
            StringTokenizer tokens = new StringTokenizer(exludedString, ";");
            int n = tokens.countTokens();
            for (int i = 0; i < n ; i++) {
                excluded.add(tokens.nextToken());
            }
        } catch (Throwable t) {
            // ignore
        }
        return excluded;
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

     ///////////////////////////////////////////////////////////////////////////
    // Implement GedcomFileListener
    ///////////////////////////////////////////////////////////////////////////

    /**
     * desactive la completion avec gedcom si le fichier gedcom utilisé
     * est fermé par l'utilisateur
     * @param gedcom
     */
    @Override
    public void gedcomClosed(Gedcom gedcom) {
        if ( completionGedcom != null && completionGedcom.equals(gedcom) ){
            removeGedcomCompletion();
        }
    }

    @Override
    public void commitRequested(Context context) {
        //rien à faire
    }

    @Override
    public void gedcomOpened(Gedcom gedcom) {
        // rien à faire
    }

    /**
     * Liste contenant les prenoms avec les "Field" ou ils sont utilises
     */
    private class FirstNameCompletionSet extends CompletionSet<Field> {
        public FirstNameCompletionSet() {
            loadExclude();
        }

        /**
         * charge les valeurs a exclure
         */
        private void loadExclude() {
            setExclude(loadExcludeCompletion(CompletionType.firstName));
            fireListener();
        }

        /**
         * ajoute le prenom dans la liste de completion
         * et met a jour le sexe correspondant au prénom
         * @param firstNameField
         * @param sexField
         */
        public void add( Field firstNameField, String sex) {
            if ( firstNameField != null && firstNameField.isEmpty() == false) {
                super.add(firstNameField.toString(), firstNameField, true);
                updateFirstNameSex(null, null, firstNameField.toString(), sex);
            }
        }
        
        /**
         * ajoute le prenom dans la lsite de completion
         * et met a jour le sexe correspondant au prénom
         * @param gedcomField
         * @param sexField
         */
        public void addGedcom( Field gedcomField, String firstName, String sex) {
            if (gedcomField != null && gedcomField.isEmpty() == false) {
                super.add(firstName, gedcomField, false);
                updateFirstNameSex(null, null, firstName, sex);
            }
        }

        public void remove(Field lastNameField, String sex) {
            if ( lastNameField != null && lastNameField.toString().isEmpty()==false ) {
                remove(lastNameField, lastNameField.toString(), sex, true );
            }
        }

        public void remove(Field lastNameField, String oldFirstName, String oldSex) {
            if ( oldFirstName != null && oldFirstName.isEmpty()==false ) {
                remove(lastNameField, oldFirstName, oldSex, true );
            }
        }

        public void removeGedcom(Field lastNameField, String firstName, String sex) {
            if ( firstName != null && firstName.isEmpty()==false ) {
                remove(lastNameField, firstName, sex, false );
            }
        }

        private void remove(Field firstNameField, String firstName, String sex, boolean fireListeners ) {
            if (firstName != null && firstNameField != null) {
                super.remove(firstName, firstNameField, fireListeners);
                updateFirstNameSex(firstName, sex, null, null);
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
    private class LastNameCompletionSet extends CompletionSet<Field> {

        public LastNameCompletionSet() {
            loadExclude();
        }
        
        /**
         * charge les valeurs a exclure
         */
        private void loadExclude() {
            setExclude(loadExcludeCompletion(CompletionType.lastName));
            fireListener();
        }
        
        public void add( Field lastNameField) {
            if ( lastNameField != null && lastNameField.isEmpty()==false ) {
                super.add(lastNameField.toString(), lastNameField, true);
            }
        }

        public void addGedcom( Field gedcomField, String lastName) {
            if ( gedcomField != null && gedcomField.isEmpty()==false) {
                super.add(lastName, gedcomField, false);
            }
        }

        public void remove(Field lastNameField) {
            if (lastNameField != null) {
                remove(lastNameField, lastNameField.toString(), true);
            }
        }

        public void remove(Field lastNameField, String oldLastName ) {
            remove(lastNameField, oldLastName, true );
        }

        public void removeGedcom(Field lastNameField, String oldLastName ) {
            remove(lastNameField, oldLastName, false );
        }

        private void remove(Field lastNameField, String oldLastName, boolean fireListeners) {
            if (oldLastName != null && lastNameField != null) {
                super.remove(oldLastName, lastNameField, fireListeners);
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
    private class OccupationCompletionSet extends CompletionSet<Field> {

        public OccupationCompletionSet() {
            loadExclude();
        }

        /**
         * charge les valeurs a exclure
         */
        private void loadExclude() {
            setExclude(loadExcludeCompletion(CompletionType.occupation));
            fireListener();
        }

        public void add( Field occupationField) {
            if ( occupationField != null && occupationField.isEmpty()==false ) {
                super.add(occupationField.toString(), occupationField, true);
            }
        }

        public void addGedcom( Field occupationField, String occupationValue) {
            if ( occupationField != null && occupationField.isEmpty()==false ) {
                super.add(occupationValue, occupationField, false);
            }
        }

        public void remove(Field occupationField) {
            if (occupationField != null) {
                remove(occupationField, occupationField.toString(), true);
            }
        }

        public void remove(Field occupationField, String oldOccupationValue ) {
            remove(occupationField, oldOccupationValue, true );
        }

        public void removeGedcom(Field occupationField, String oldOccupationValue ) {
            remove(occupationField, oldOccupationValue, false );
        }

        private void remove(Field occupationField, String oldOccupationValue, boolean fireListeners) {
            if (oldOccupationValue != null && occupationField != null) {
                super.remove(oldOccupationValue, occupationField, fireListeners);
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
    private class PlaceCompletionSet extends CompletionSet<Field> {
        public PlaceCompletionSet() {
            loadExclude();
        }

        /**
         * charge les valeurs a exclure
         */
        private void loadExclude() {
            setExclude(loadExcludeCompletion(CompletionType.place));
            fireListener();
        }

        public void add( Field placeField) {
            if ( placeField != null && placeField.isEmpty()==false) {
                super.add(placeField.toString(), placeField, true);
            }
        }

        public void addGedcom( Field placeField, String placeValue) {
            if ( placeField != null && placeField.isEmpty()==false) {
                super.add(placeValue, placeField, false);
            }
        }

        public void remove(Field placeField) {
            if (placeField != null) {
                remove(placeField, placeField.toString(), true);
            }
        }

        public void remove(Field placeField, String oldPlaceValue ) {
            remove(placeField, oldPlaceValue, true );
        }

        public void removeGedcom(Field placeField, String oldPlaceValue ) {
            remove(placeField, oldPlaceValue, false );
        }

        private void remove(Field placeField, String oldPlaceValue, boolean fireListeners) {
            if (oldPlaceValue != null && placeField != null) {
                super.remove(oldPlaceValue, placeField, fireListeners);
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
    private class CompletionSet<REF> {

        // liste TreeMap triée sur la clé
        private Map<String, Set<REF>> key2references = new TreeMap<String, Set<REF>>();
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
        public Set<REF> getReferences(String key) {
            // null is ignored
            if (key == null) {
                return new HashSet<REF>();
            }
            // lookup
            Set<REF> references = key2references.get(key);
            if (references == null) {
                return new HashSet<REF>();
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
            Set<REF> references = key2references.get(key);
            if (references == null) {
                return 0;
            }
            // done
            return references.size();
        }

        /**
         * Add a key and its reference
         * @return whether the reference was actually added (could have been known already)
         */
        private boolean add(String key, REF reference) {
            return add(key, reference, true);
        }

        /**
         * Add a key and its reference
         * @return whether the reference was actually added (could have been known already)
         */
        private boolean add(String key, REF reference, boolean fireListeners) {
            // null is ignored
            if (key == null || key.equals("")) {
                return false;
            }
            // lookup
            Set<REF> references = key2references.get(key);
            if (references == null) {
                references = new HashSet<REF>();
                key2references.put(key, references);
                if (fireListeners) {
                    fireListener();
                }
            }
            // safety check for reference==null - might be
            // and still was necessary to keep key
            if (reference == null) {
                return false;
            }
            // add
            if (!references.add(reference)) {
                return false;
            }

            if ( !excluded.contains(key) && !included.contains(key))  {
                included.add(key);
            }
            return true;
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

        private boolean remove(String key, REF reference) {
            return remove(key, reference, true);

        }

        /**
         * Remove a reference for given key
         */
        private boolean remove(String key, REF reference, boolean fireListeners) {
            // null is ignored
            if (key == null || key.equals("")) {
                return false;
            }
            // lookup
            Set<REF> references = key2references.get(key);
            if (references == null) {
                return false;
            }
            // remove
            if (!references.remove(reference)) {
                return false;
            }
            // remove value
            if (references.isEmpty()) {
                key2references.remove(key);
                if(fireListeners) {
                    fireListener();
                }
            }

            included.remove(key);
            return true;
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
         * notify les listeners d'une mise a jour
         */
        protected void fireListener() {
            final List<String> keys = new ArrayList<String>(key2references.keySet());
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    for (CompletionListener listener : keysListener) {
                        listener.keyUpdated(keys);
                    }
                }
            });

        }
    }
}
