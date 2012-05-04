package ancestris.modules.releve.model;

import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertyChoiceValue;
import genj.gedcom.PropertyName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Michel
 */
public class CompletionProvider {

    private CompletionSet<String, Field> firstNames = new CompletionSet<String, Field>();
    private CompletionSet<String, Field> lastNames = new CompletionSet<String, Field>();
    private CompletionSet<String, Field> occupations = new CompletionSet<String, Field>();
    private CompletionSet<String, Field> places = new CompletionSet<String, Field>();
    private CompletionSet<String, Field> eventTypes = new CompletionSet<String, Field>();

    private HashMap<String, Integer> firstNameSex = new HashMap<String, Integer>();
    private HashMap<String, Integer> gedcomFirstNameSex = new HashMap<String, Integer>();

    /**
     * langue a utiliser pour faire la complétion.
     */
    private Locale locale= Locale.getDefault();

    /**
     * ajoute les prenoms, le noms et les professions dans les listes de completion
     *   
     * @param record
     */
    protected void addRecord(Record record) {

        // indi FirstName
        if ( record.getIndiFirstName()!= null && record.getIndiFirstName().isEmpty()==false) {
            firstNames.add(record.getIndiFirstName().getValue(), record.getIndiFirstName());
            updateFirstNameSex(null, "", record.getIndiFirstName().getValue(), record.getIndiSex().getValue());
        }        
        if ( record.getIndiMarriedFirstName()!= null && record.getIndiMarriedFirstName().isEmpty()==false) {
            firstNames.add(record.getIndiMarriedFirstName().getValue(), record.getIndiMarriedFirstName());
            //addFirstNameSex(record.getIndiMarriedFirstName(), record.getIndiMarriedSex().getFirstNameSex());
            updateFirstNameSex(null, "", record.getIndiMarriedFirstName().getValue(), record.getIndiSex().getOppositeString());
        }
        if ( record.getIndiFatherFirstName()!= null && record.getIndiFatherFirstName().isEmpty()==false) {
            firstNames.add(record.getIndiFatherFirstName().getValue(), record.getIndiFatherFirstName());
            updateFirstNameSex(null, "", record.getIndiFatherFirstName().getValue(), FieldSex.MALE_STRING);
        }
        if ( record.getIndiMotherFirstName()!= null && record.getIndiMotherFirstName().isEmpty()==false) {
            firstNames.add(record.getIndiMotherFirstName().getValue(), record.getIndiMotherFirstName());
            updateFirstNameSex(null, "", record.getIndiMotherFirstName().getValue(), FieldSex.FEMALE_STRING);
        }
        // wife FirstName
        if ( record.getWifeFirstName()!= null && record.getWifeFirstName().isEmpty()==false) {
            firstNames.add(record.getWifeFirstName().getValue(), record.getWifeFirstName());
            updateFirstNameSex(null, "", record.getWifeFirstName().getValue(), record.getWifeSex().getValue());
        }
        if ( record.getWifeMarriedFirstName()!= null && record.getWifeMarriedFirstName().isEmpty()==false) {
            firstNames.add(record.getWifeMarriedFirstName().getValue(), record.getWifeMarriedFirstName());
            //addFirstNameSex(record.getWifeMarriedFirstName(), record.getWifeMarriedSex().getFirstNameSex());
            updateFirstNameSex(null, "", record.getWifeMarriedFirstName().getValue(), record.getWifeSex().getOppositeString());
        }
        if ( record.getWifeFatherFirstName()!= null && record.getWifeFatherFirstName().isEmpty()==false) {
            firstNames.add(record.getWifeFatherFirstName().getValue(), record.getWifeFatherFirstName());
            updateFirstNameSex(null, "", record.getWifeFatherFirstName().getValue(), FieldSex.MALE_STRING);
        }
        if ( record.getWifeMotherFirstName()!= null && record.getWifeMotherFirstName().isEmpty()==false) {
            firstNames.add(record.getWifeMotherFirstName().getValue(), record.getWifeMotherFirstName());
            updateFirstNameSex(null, "", record.getWifeMotherFirstName().getValue(), FieldSex.FEMALE_STRING);
        }
        // witness FirstName
        if ( record.getWitness1FirstName()!= null && record.getWitness1FirstName().isEmpty()==false) {
            firstNames.add(record.getWitness1FirstName().getValue(), record.getWitness1FirstName());
        }
        if ( record.getWitness2FirstName()!= null && record.getWitness2FirstName().isEmpty()==false) {
            firstNames.add(record.getWitness2FirstName().getValue(), record.getWitness2FirstName());
        }
        if ( record.getWitness3FirstName()!= null && record.getWitness3FirstName().isEmpty()==false) {
            firstNames.add(record.getWitness3FirstName().getValue(), record.getWitness3FirstName());
        }
        if ( record.getWitness4FirstName()!= null && record.getWitness4FirstName().isEmpty()==false) {
            firstNames.add(record.getWitness4FirstName().getValue(), record.getWitness4FirstName());
        }

        //indi LastName
        if ( record.getIndiLastName()!= null && record.getIndiLastName().isEmpty()==false) {
            lastNames.add(record.getIndiLastName().getValue(), record.getIndiLastName());
        }
        if ( record.getIndiMarriedLastName()!= null && record.getIndiMarriedLastName().isEmpty()==false) {
            lastNames.add(record.getIndiMarriedLastName().getValue(), record.getIndiMarriedLastName());
        }
        if ( record.getIndiFatherLastName()!= null && record.getIndiFatherLastName().isEmpty()==false) {
            lastNames.add(record.getIndiFatherLastName().getValue(), record.getIndiFatherLastName());
        }
        if ( record.getIndiMotherLastName()!= null && record.getIndiMotherLastName().isEmpty()==false) {
            lastNames.add(record.getIndiMotherLastName().getValue(), record.getIndiMotherLastName());
        }
        //Wife LastName
        if ( record.getWifeLastName()!= null && record.getWifeLastName().isEmpty()==false) {
            lastNames.add(record.getWifeLastName().getValue(), record.getWifeLastName());
        }
        if ( record.getWifeMarriedLastName()!= null && record.getWifeMarriedLastName().isEmpty()==false) {
            lastNames.add(record.getWifeMarriedLastName().getValue(), record.getWifeMarriedLastName());
        }
        if ( record.getWifeFatherLastName()!= null && record.getWifeFatherLastName().isEmpty()==false) {
            lastNames.add(record.getWifeFatherLastName().getValue(), record.getWifeFatherLastName());
        }
        if ( record.getWifeMotherLastName()!= null && record.getWifeMotherLastName().isEmpty()==false) {
            lastNames.add(record.getWifeMotherLastName().getValue(), record.getWifeMotherLastName());
        }
        //witness LastName
        if ( record.getWitness1LastName()!= null && record.getWitness1LastName().isEmpty()==false) {
            lastNames.add(record.getWitness1LastName().getValue(), record.getWitness1LastName());
        }
        if ( record.getWitness2LastName()!= null && record.getWitness2LastName().isEmpty()==false) {
            lastNames.add(record.getWitness2LastName().getValue(), record.getWitness2LastName());
        }
        if ( record.getWitness3LastName()!= null && record.getWitness3LastName().isEmpty()==false) {
            lastNames.add(record.getWitness3LastName().getValue(), record.getWitness3LastName());
        }
        if ( record.getWitness4LastName()!= null && record.getWitness4LastName().isEmpty()==false) {
            lastNames.add(record.getWitness4LastName().getValue(), record.getWitness4LastName());
        }

        // Indi Occupation
        if ( record.getIndiOccupation()!= null && record.getIndiOccupation().isEmpty()==false) {
            occupations.add(record.getIndiOccupation().getValue(), record.getIndiOccupation());
        }
        if ( record.getIndiMarriedOccupation()!= null && record.getIndiMarriedOccupation().isEmpty()==false) {
            occupations.add(record.getIndiMarriedOccupation().getValue(), record.getIndiMarriedOccupation());
        }
        if ( record.getIndiFatherOccupation()!= null && record.getIndiFatherOccupation().isEmpty()==false) {
            occupations.add(record.getIndiFatherOccupation().getValue(), record.getIndiFatherOccupation());
        }
        if ( record.getIndiMotherOccupation()!= null && record.getIndiMotherOccupation().isEmpty()==false) {
            occupations.add(record.getIndiMotherOccupation().getValue(), record.getIndiMotherOccupation());
        }
        // wife Occupation
        if ( record.getWifeOccupation()!= null && record.getWifeOccupation().isEmpty()==false) {
            occupations.add(record.getWifeOccupation().getValue(), record.getWifeOccupation());
        }
        if ( record.getWifeMarriedOccupation()!= null && record.getWifeMarriedOccupation().isEmpty()==false) {
            occupations.add(record.getWifeMarriedOccupation().getValue(), record.getWifeMarriedOccupation());
        }
        if ( record.getWifeFatherOccupation()!= null && record.getWifeFatherOccupation().isEmpty()==false) {
            occupations.add(record.getWifeFatherOccupation().getValue(), record.getWifeFatherOccupation());
        }
        if ( record.getWifeMotherOccupation()!= null && record.getWifeMotherOccupation().isEmpty()==false) {
            occupations.add(record.getWifeMotherOccupation().getValue(), record.getWifeMotherOccupation());
        }

        // witness Occupation
        if ( record.getWitness1Occupation()!= null && record.getWitness1Occupation().isEmpty()==false) {
            occupations.add(record.getWitness1Occupation().getValue(), record.getWitness1Occupation());
        }
        if ( record.getWitness2Occupation()!= null && record.getWitness2Occupation().isEmpty()==false) {
            occupations.add(record.getWitness2Occupation().getValue(), record.getWitness2Occupation());
        }
        if ( record.getWitness3Occupation()!= null && record.getWitness3Occupation().isEmpty()==false) {
            occupations.add(record.getWitness3Occupation().getValue(), record.getWitness3Occupation());
        }
        if ( record.getWitness4Occupation()!= null && record.getWitness4Occupation().isEmpty()==false) {
            occupations.add(record.getWitness4Occupation().getValue(), record.getWitness4Occupation());
        }

        // EventPlace
        if ( record.getEventPlace()!= null && record.getEventPlace().isEmpty()== false) {
            places.add(record.getEventPlace().getValue(), record.getEventPlace());
        }

        // EventType
        if ( record.getEventType()!= null && record.getEventType().isEmpty()== false) {
            eventTypes.add(record.getEventType().getTag(), record.getEventType());
        }
    }

    /**
     * supprime les prenoms, lee noms et les professions des listes de completion
     * @param record
     */
    protected void removeRecord(final Record record) {

        // indi FirstName
        if ( record.getIndiFirstName()!= null && record.getIndiFirstName().isEmpty()==false) {
            firstNames.remove(record.getIndiFirstName().getValue(), record.getIndiFirstName());
            updateFirstNameSex(record.getIndiFirstName().getValue(), record.getIndiSex().getValue(), null,"");
        }
        if ( record.getIndiMarriedFirstName()!= null && record.getIndiMarriedFirstName().isEmpty()==false) {
            firstNames.remove(record.getIndiMarriedFirstName().getValue(), record.getIndiMarriedFirstName());
            //removeFirstNameSex(record.getIndiMarriedFirstName(), record.getIndiMarriedSex().getFirstNameSex());
            updateFirstNameSex(record.getIndiMarriedFirstName().getValue(), record.getIndiSex().getOppositeString(), null,"");
        }
        if ( record.getIndiFatherFirstName()!= null && record.getIndiFatherFirstName().isEmpty()==false) {
            firstNames.remove(record.getIndiFatherFirstName().getValue(), record.getIndiFatherFirstName());
            updateFirstNameSex(record.getIndiFatherFirstName().getValue(), FieldSex.MALE_STRING, null,"");
        }
        if ( record.getIndiMotherFirstName()!= null && record.getIndiMotherFirstName().isEmpty()==false) {
            firstNames.remove(record.getIndiMotherFirstName().getValue(), record.getIndiMotherFirstName());
            updateFirstNameSex(record.getIndiMotherFirstName().getValue(), FieldSex.FEMALE_STRING, null,"");
        }
        // wife FirstName
        if ( record.getWifeFirstName()!= null && record.getWifeFirstName().isEmpty()==false) {
            firstNames.remove(record.getWifeFirstName().getValue(), record.getWifeFirstName());
            updateFirstNameSex(record.getWifeFirstName().getValue(), record.getWifeSex().getValue(), null,"");
        }
        if ( record.getWifeMarriedFirstName()!= null && record.getWifeMarriedFirstName().isEmpty()==false) {
            firstNames.remove(record.getWifeMarriedFirstName().getValue(), record.getWifeMarriedFirstName());
            //removeFirstNameSex(record.getWifeMarriedFirstName(), record.getWifeMarriedSex().getFirstNameSex());
            updateFirstNameSex(record.getWifeMarriedFirstName().getValue(), record.getWifeSex().getOppositeString(), null,"");
        }
        if ( record.getWifeFatherFirstName()!= null && record.getWifeFatherFirstName().isEmpty()==false) {
            firstNames.remove(record.getWifeFatherFirstName().getValue(), record.getWifeFatherFirstName());
            updateFirstNameSex(record.getWifeFatherFirstName().getValue(), FieldSex.MALE_STRING, null,"");
        }
        if ( record.getWifeMotherFirstName()!= null && record.getWifeMotherFirstName().isEmpty()==false) {
            firstNames.remove(record.getWifeMotherFirstName().getValue(), record.getWifeMotherFirstName());
            updateFirstNameSex(record.getWifeMotherFirstName().getValue(), FieldSex.FEMALE_STRING, null,"");
        }
        // witness FirstName
        if ( record.getWitness1FirstName()!= null && record.getWitness1FirstName().isEmpty()==false) {
            firstNames.remove(record.getWitness1FirstName().getValue(), record.getWitness1FirstName());
        }
        if ( record.getWitness2FirstName()!= null && record.getWitness2FirstName().isEmpty()==false) {
            firstNames.remove(record.getWitness2FirstName().getValue(), record.getWitness2FirstName());
        }
        if ( record.getWitness3FirstName()!= null && record.getWitness3FirstName().isEmpty()==false) {
            firstNames.remove(record.getWitness3FirstName().getValue(), record.getWitness3FirstName());
        }
        if ( record.getWitness4FirstName()!= null && record.getWitness4FirstName().isEmpty()==false) {
            firstNames.remove(record.getWitness4FirstName().getValue(), record.getWitness4FirstName());
        }

        //indi LastName
        if ( record.getIndiLastName()!= null && record.getIndiLastName().isEmpty()==false) {
            lastNames.remove(record.getIndiLastName().getValue(), record.getIndiLastName());
        }
        if ( record.getIndiMarriedLastName()!= null && record.getIndiMarriedLastName().isEmpty()==false) {
            lastNames.remove(record.getIndiMarriedLastName().getValue(), record.getIndiMarriedLastName());
        }
        if ( record.getIndiFatherLastName()!= null && record.getIndiFatherLastName().isEmpty()==false) {
            lastNames.remove(record.getIndiFatherLastName().getValue(), record.getIndiFatherLastName());
        }
        if ( record.getIndiMotherLastName()!= null && record.getIndiMotherLastName().isEmpty()==false) {
            lastNames.remove(record.getIndiMotherLastName().getValue(), record.getIndiMotherLastName());
        }
        //Wife LastName
        if ( record.getWifeLastName()!= null && record.getWifeLastName().isEmpty()==false) {
            lastNames.remove(record.getWifeLastName().getValue(), record.getWifeLastName());
        }
        if ( record.getWifeMarriedLastName()!= null && record.getWifeMarriedLastName().isEmpty()==false) {
            lastNames.remove(record.getWifeMarriedLastName().getValue(), record.getWifeMarriedLastName());
        }
        if ( record.getWifeFatherLastName()!= null && record.getWifeFatherLastName().isEmpty()==false) {
            lastNames.remove(record.getWifeFatherLastName().getValue(), record.getWifeFatherLastName());
        }
        if ( record.getWifeMotherLastName()!= null && record.getWifeMotherLastName().isEmpty()==false) {
            lastNames.remove(record.getWifeMotherLastName().getValue(), record.getWifeMotherLastName());
        }
        //witness LastName
        if ( record.getWitness1LastName()!= null && record.getWitness1LastName().isEmpty()==false) {
            lastNames.remove(record.getWitness1LastName().getValue(), record.getWitness1LastName());
        }
        if ( record.getWitness2LastName()!= null && record.getWitness2LastName().isEmpty()==false) {
            lastNames.remove(record.getWitness2LastName().getValue(), record.getWitness2LastName());
        }
        if ( record.getWitness3LastName()!= null && record.getWitness3LastName().isEmpty()==false) {
            lastNames.remove(record.getWitness3LastName().getValue(), record.getWitness3LastName());
        }
        if ( record.getWitness4LastName()!= null && record.getWitness4LastName().isEmpty()==false) {
            lastNames.remove(record.getWitness4LastName().getValue(), record.getWitness4LastName());
        }

        // Indi Occupation
        if ( record.getIndiOccupation()!= null && record.getIndiOccupation().isEmpty()==false) {
            occupations.remove(record.getIndiOccupation().getValue(), record.getIndiOccupation());
        }
        if ( record.getIndiMarriedOccupation()!= null && record.getIndiMarriedOccupation().isEmpty()==false) {
            occupations.remove(record.getIndiMarriedOccupation().getValue(), record.getIndiMarriedOccupation());
        }
        if ( record.getIndiFatherOccupation()!= null && record.getIndiFatherOccupation().isEmpty()==false) {
            occupations.remove(record.getIndiFatherOccupation().getValue(), record.getIndiFatherOccupation());
        }
        if ( record.getIndiMotherOccupation()!= null && record.getIndiMotherOccupation().isEmpty()==false) {
            occupations.remove(record.getIndiMotherOccupation().getValue(), record.getIndiMotherOccupation());
        }
        // wife Occupation
        if ( record.getWifeOccupation()!= null && record.getWifeOccupation().isEmpty()==false) {
            occupations.remove(record.getWifeOccupation().getValue(), record.getWifeOccupation());
        }
        if ( record.getWifeMarriedOccupation()!= null && record.getWifeMarriedOccupation().isEmpty()==false) {
            occupations.remove(record.getWifeMarriedOccupation().getValue(), record.getWifeMarriedOccupation());
        }
        if ( record.getWifeFatherOccupation()!= null && record.getWifeFatherOccupation().isEmpty()==false) {
            occupations.remove(record.getWifeFatherOccupation().getValue(), record.getWifeFatherOccupation());
        }
        if ( record.getWifeMotherOccupation()!= null && record.getWifeMotherOccupation().isEmpty()==false) {
            occupations.remove(record.getWifeMotherOccupation().getValue(), record.getWifeMotherOccupation());
        }

        // witness Occupation
        if ( record.getWitness1Occupation()!= null && record.getWitness1Occupation().isEmpty()==false) {
            occupations.remove(record.getWitness1Occupation().getValue(), record.getWitness1Occupation());
        }
        if ( record.getWitness2Occupation()!= null && record.getWitness2Occupation().isEmpty()==false) {
            occupations.remove(record.getWitness2Occupation().getValue(), record.getWitness2Occupation());
        }
        if ( record.getWitness3Occupation()!= null && record.getWitness3Occupation().isEmpty()==false) {
            occupations.remove(record.getWitness3Occupation().getValue(), record.getWitness3Occupation());
        }
        if ( record.getWitness4Occupation()!= null && record.getWitness4Occupation().isEmpty()==false) {
            occupations.remove(record.getWitness4Occupation().getValue(), record.getWitness4Occupation());
        }

        // EventPlace
        if ( record.getEventPlace()!= null && record.getEventPlace().isEmpty()== false) {
            places.remove(record.getEventPlace().getValue(), record.getEventPlace());
        }

        // EventType
        if ( record.getEventType()!= null && record.getEventType().isEmpty()== false) {
            eventTypes.remove(record.getEventType().getTag(), record.getEventType());
        }
    }

    protected void removeAll () {
        firstNames = new CompletionSet<String, Field>();
        lastNames = new CompletionSet<String, Field>();
        occupations = new CompletionSet<String, Field>();
        places = new CompletionSet<String, Field>();
        eventTypes = new CompletionSet<String, Field>();
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
        if (oldFirstName != null && ! oldFirstName.isEmpty()) {
            int count = firstNameSex.containsKey(oldFirstName) ? firstNameSex.get(oldFirstName) : 0;
            if (oldSex.equals(FieldSex.MALE_STRING)) {
                firstNameSex.put(oldFirstName, count - 1);
            } else  if (oldSex.equals(FieldSex.FEMALE_STRING)) {
                firstNameSex.put(oldFirstName, count + 1);
            }
        }
        if (firstName != null && ! firstName.isEmpty()) {
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
        this.locale = gedcom.getLocale();

        // je cree un objet Field associé au Gedcom pour pouvoir le referencer
        // dans les listes de completion
        FieldGedcom gedcomField = new FieldGedcom();
        gedcomField.setValue(gedcom);

        // j'ajoute les prénoms, noms, professions et lieux du Gedcom dans les
        // listes de completion
        for ( String firstName : PropertyName.getFirstNames(gedcom, false)) {
            firstNames.add(firstName, gedcomField);
        }
        for ( String lastName : PropertyName.getLastNames(gedcom, false)) {
            lastNames.add(lastName, gedcomField);
        }
        for ( String occupation : PropertyChoiceValue.getChoices(gedcom, "OCCU", false)) {
            occupations.add(occupation, gedcomField);
        }

        // je créee une statistique des sexes/ prénom pour les prénoms du gedcom
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
    protected void removeGedcomCompletion(Gedcom gedcom) {
        this.locale = Locale.getDefault();

        FieldGedcom gedcomField = new FieldGedcom();
        gedcomField.setValue(gedcom);

        // je supprime les prénoms, noms, professions et lieux du Gedcom des
        // listes de completion
        for ( String firstName : firstNames.getKeys()) {
            firstNames.remove(firstName, gedcomField);
        }
        for ( String lastName : lastNames.getKeys()) {
            lastNames.remove(lastName, gedcomField);
        }
        for ( String occupation : occupations.getKeys()) {
            occupations.remove(occupation, gedcomField);
        }

        // je vide la statistique des sexes/ prénom des prénoms du gedcom
        gedcomFirstNameSex.clear();

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
    public List getFirstNames() {
       return firstNames.getKeys();
    }

    /**
     * retourne les noms triées par ordre alphabétique
     */
    public List getLastNames() {
        return lastNames.getKeys();
    }

    /**
     * retourne les professions triées par ordre alphabétique
     */
    public List getOccupations() {
       return occupations.getKeys();
    }

    /**
     * retourne les tags des types d'evenement triées par ordre alphabétique
     */
    public List getEventTypes() {
        return eventTypes.getKeys();
    }

    /**
     * retourne les lieux triées par ordre alphabétique
     */
    public List<String> getPlaces() {
        return places.getKeys();
    }

    public Locale getLocale() {
        return locale;
    }

    /**
     * met a jour la liste de completion firstNames
     * supprime l'ancienne valeur oldValue et ajoute la nouvelle valeur qui est dasn field
     * @param field
     * @param oldValue
     */
    public void updateFirstName( Field field, String oldValue) {
        if (oldValue != null && ! oldValue.isEmpty()) {
            firstNames.remove(oldValue, field);

        }
        if ( field.toString().isEmpty()==false) {
            firstNames.add(field.toString(), field);
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
            lastNames.remove(oldValue, field);
        }
        if ( field.toString().isEmpty()==false) {
            lastNames.add(field.toString(), field);
        }
    }


    public void updateOccupation(Field field, String oldValue) {
        if (oldValue != null && ! oldValue.isEmpty()) {
            occupations.remove(oldValue, field);
        }
        if ( field.toString().isEmpty()==false) {
            lastNames.add(field.toString(), field);
        }
    }

    public void updatePlaces(Field field, String oldValue) {
        if (oldValue != null && ! oldValue.isEmpty()) {
            places.remove(oldValue, field);
        }
        if ( field.toString().isEmpty()==false) {
            places.add(field.toString(), field);
        }
    }

    public void updateEventType(FieldEventType field, String oldValue) {
        if (oldValue != null && ! oldValue.isEmpty()) {
            eventTypes.remove(oldValue, field);
        }
        if ( field.getTag().isEmpty()==false) {
            eventTypes.add(field.getTag(), field);
        }
    }

    /**
     * table contenant une liste de valeur et les objet qui utilisent chaque valeur
     */
    private class CompletionSet<KEY, REF> {

        // liste TreeMap triée sur la clé
        private Map<KEY, Set<REF>> key2references = new TreeMap<KEY, Set<REF>>();

        /**
         * Constructor - uses a TreeMap that keeps
         * keys sorted by their natural order
         */
        public CompletionSet() {
        }

        /**
         * Returns the references for a given key
         */
        public Set<REF> getReferences(KEY key) {
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
         * Returns the total number of references
         */
//        public int getSize() {
//            return size;
//        }

        /**
         * Returns the number of reference for given key
         */
        public int getSize(KEY key) {
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
         * Add a key
         */
        public boolean add(KEY key) {
            return add(key, null);
        }

        /**
         * Add a key and its reference
         * @return whether the reference was actually added (could have been known already)
         */
        public boolean add(KEY key, REF reference) {
            // null is ignored
            if (key == null) {
                return false;
            }
            // lookup
            Set<REF> references = key2references.get(key);
            if (references == null) {
                references = new HashSet<REF>();
                key2references.put(key, references);
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
            return true;
        }

        /**
         * Remove a reference for given key
         */
        public boolean remove(KEY key, REF reference) {
            // null is ignored
            if (key == null) {
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
            }
            return true;
        }

        /**
         * Return all keys  (sorted by TreeMap)
         */
        public List<KEY> getKeys() {
            return new ArrayList<KEY>(key2references.keySet());
        }

    }

}
