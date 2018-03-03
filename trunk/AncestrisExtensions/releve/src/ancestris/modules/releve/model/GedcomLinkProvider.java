 package ancestris.modules.releve.model;

import ancestris.modules.releve.merge.MergeQuery;
import static ancestris.modules.releve.merge.MergeQuery.isSameFirstName;
import static ancestris.modules.releve.merge.MergeQuery.isSameLastName;
import ancestris.modules.releve.model.Record.FieldType;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.openide.util.Exceptions;

/**
 * recherche les relevés correspondant à des évènements dans un fichier gedcom 
 * Les relevés qui ont évènement correspondant sont signalés dans la table des relevés 
 * ( identifiant en rouge)
 * Correspondanes recherchées : 
 * relevés de naissance <=> Tag BIRT
 * relevés de décés  <=> Tag DEAT
 * relevés de mariage <=> tag MARR
 * relevés de ban de mariage <=> tag MARB , MARC
 * relevés de testament <=>  tag WILL
 * relevés divers  <=> tag EVEN
 * 
 * Les correspondances sont établis en focntion du type dévènement, de la date 
 * du nom et du prénom de l'individu concerné. 
 */
public class GedcomLinkProvider {
    private final HashMap<Record, GedcomLink> gedcomLinkList = new HashMap<Record, GedcomLink>();
    private Gedcom gedcom = null; 
    private boolean showGedcomLink = false;
    
    static private final TagPath marrDateTag = new TagPath("FAM:MARR:DATE");
    static private final TagPath marbDateTag = new TagPath("FAM:MARB:DATE");
    static private final TagPath marcDateTag = new TagPath("FAM:MARC:DATE");
    static private final TagPath marlDateTag = new TagPath("FAM:MARL:DATE");
    static private final TagPath evenDateTag = new TagPath("FAM:EVEN:DATE");
    static private final TagPath indiEvenDateTag = new TagPath("INDI:EVEN:DATE");
    static private final TagPath willDateTag = new TagPath("INDI:WILL:DATE");
    
    static private final TagPath[] miscIndiTagPathList = {indiEvenDateTag, willDateTag};
    static private final TagPath[] miscFamTagPathList = {marbDateTag, marcDateTag, marlDateTag, evenDateTag};
//    // attributs temporaires 
//    Collection<Indi> indiList;
//    Collection<Fam> famList;
    
    public void init(final RecordModel recordModel, Gedcom gedcom, boolean showGedcomLink) {
        this.gedcom = gedcom;
        this.showGedcomLink = showGedcomLink;
        
        ArrayList<RecordEntry> recordBirth = new ArrayList<RecordEntry> ();
        ArrayList<RecordEntry> recordDeath = new ArrayList<RecordEntry>();
        ArrayList<RecordEntry> recordMarriage = new ArrayList<RecordEntry>();
        ArrayList<RecordEntry> recordMisc = new ArrayList<RecordEntry>();
        
        ArrayList<IndiEntry> indiBirth = new ArrayList<IndiEntry>();
        ArrayList<IndiEntry> indiDeath = new ArrayList<IndiEntry>();
        ArrayList<IndiEntry> indiMisc = new ArrayList<IndiEntry>();
        ArrayList<FamEntry> famMarriage = new ArrayList<FamEntry>();
        ArrayList<FamEntry> famMisc = new ArrayList<FamEntry>();
        
        if (gedcom != null && showGedcomLink == true ) {            
            
            // Je classe  les relevés dans listes trièes par date 
            int rwoCount = recordModel.getRowCount();
            for (int i = 0; i < rwoCount; i++) {
                Record record = recordModel.getRecord(i);
                createRecordList(record, recordBirth, recordDeath, recordMarriage, recordMisc);
            }
            
            // je classe les naissances, décès et autres évènement des individus 
            // dans 3 listes trièes par date
            createIndiList(indiBirth, indiDeath, indiMisc);
            // je classe les mariages et autres évènements des familles 
            // dans 2 listes trièes par date
            createFamList(famMarriage, famMisc);
            
            // je nettoie le resultat avant de faire la recherche 
            gedcomLinkList.clear();
            
            // je recherche les corrspondances
            compareList(recordBirth, indiBirth);            
            compareList(recordDeath, indiDeath);            
            compareListMarriage(recordMarriage, famMarriage);            
            compareList(recordMisc, indiMisc);
            compareListMarriage(recordMisc, famMisc);
            
            // j'affiche le resultat 
            recordModel.fireRecordModelUpdated(0, recordModel.getRowCount() - 1);
            
            // je trace le resultat
//            for (GedcomLink link :gedcomLinkList.values() ) {
//                System.out.println(link.getRecord().getType() + "; "
//                        + link.getProperty().getPath().toString()+ "; "
//                        + link.getRecord().getFieldValue(FieldType.indiLastName) + " "
//                        + link.getRecord().getFieldValue(FieldType.indiFirstName) + "; "
//                        + link.getEntity().toString());
//            }

        } else {
            gedcomLinkList.clear();
            recordModel.fireRecordModelUpdated(0, recordModel.getRowCount() - 1);
        }

    }
    
    public void addRecord(Record record) {
        ArrayList<RecordEntry> recordBirth = new ArrayList<RecordEntry> ();
        ArrayList<RecordEntry> recordDeath = new ArrayList<RecordEntry>();
        ArrayList<RecordEntry> recordMisc = new ArrayList<RecordEntry>();
        ArrayList<RecordEntry> recordMarriage = new ArrayList<RecordEntry>();
        
        ArrayList<IndiEntry> indiBirth = new ArrayList<IndiEntry>();
        ArrayList<IndiEntry> indiDeath = new ArrayList<IndiEntry>();
        ArrayList<IndiEntry> indiMisc = new ArrayList<IndiEntry>();
        ArrayList<FamEntry> famMarriage = new ArrayList<FamEntry>();
        ArrayList<FamEntry> famMisc = new ArrayList<FamEntry>();
        
        if (record != null  && gedcom != null && showGedcomLink == true) {
            // je recherche l'évènement dans le gedcom
            switch (record.getType()) {
                case BIRTH:
                    createRecordList(record, recordBirth, recordDeath, recordMarriage, recordMisc);
                    createIndiList(indiBirth, indiDeath, indiMisc);
                    compareList(recordBirth, indiBirth);
                    break;
                case MARRIAGE:
                    createRecordList(record, recordBirth, recordDeath, recordMarriage, recordMisc);
                    createIndiList(indiBirth, indiDeath, indiMisc);
                    compareListMarriage(recordMarriage, famMarriage);
                    break;
                case DEATH:
                    createRecordList(record, recordBirth, recordDeath, recordMarriage, recordMisc);
                    createIndiList(indiBirth, indiDeath, indiMisc);
                    compareList(recordDeath, indiDeath);
                    break;
                case MISC:
                default:
                    createRecordList(record, recordBirth, recordDeath, recordMarriage, recordMisc);
                    createIndiList(indiBirth, indiDeath, indiMisc);
                    compareList(recordMisc, indiMisc);
                    compareListMarriage(recordMisc, famMisc);
                    break;
            }
        }
    }
    
    
    private void createRecordList(Record record, ArrayList<RecordEntry> recordBirth, ArrayList<RecordEntry> recordDeath, ArrayList<RecordEntry> recordMarriage, ArrayList<RecordEntry> recordMisc) {
        try {
            switch (record.getType()) {
                case BIRTH: {
                    Field recordDate = record.getField(FieldType.indiBirthDate);
                    if (recordDate == null || recordDate.isEmpty()) {
                        // si la date de nqissqnce n'est pas renseignée, j'utilise la date de l'évènement
                        recordDate = record.getField(FieldType.eventDate);
                    }
                    if (recordDate != null && !recordDate.isEmpty()) {
                        int julian = ((FieldDate) recordDate).getPropertyDate().getStart().getJulianDay();
                        recordBirth.add(new RecordEntry(julian, record));
                    }
                    break;
                }
                case MARRIAGE: {
                    Field recordDate = record.getField(FieldType.eventDate);
                    if (recordDate != null && !recordDate.isEmpty()) {
                        int julian = ((FieldDate) recordDate).getPropertyDate().getStart().getJulianDay();
                        recordMarriage.add(new RecordEntry(julian, record));
                    }
                    break;
                }
                case DEATH: {
                    Field recordDate = record.getField(FieldType.eventDate);
                    if (recordDate != null && !recordDate.isEmpty()) {
                        int julian = ((FieldDate) recordDate).getPropertyDate().getStart().getJulianDay();
                        recordDeath.add(new RecordEntry(julian, record));
                    }
                    break;
                }
                case MISC: {
                    // j'utilise la date d'insinuation si elle est renseignée
                    Field recordDate = record.getField(FieldType.secondDate);
                    if (recordDate == null || recordDate.isEmpty()) {
                        // si la date d'insinuation n'est pas renseignée, j'utilise la date de l'évènement
                        recordDate = record.getField(FieldType.eventDate);
                    }

                    if (recordDate != null && !recordDate.isEmpty()) {
                        int julian = ((FieldDate) recordDate).getPropertyDate().getStart().getJulianDay();
                        recordMisc.add(new RecordEntry(julian, record));
                    }
                }
            }
            Collections.sort(recordBirth);
            Collections.sort(recordDeath);
            Collections.sort(recordMarriage);
            Collections.sort(recordMisc);
            
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    
    private void createIndiList(ArrayList<IndiEntry> indiBirth, ArrayList<IndiEntry> indiDeath, ArrayList<IndiEntry> indiMisc ) {
        if (gedcom != null && showGedcomLink == true) {
            for (Indi indi : gedcom.getIndis()) {

                // naissances 
                PropertyDate birthDate = indi.getBirthDate();
                if (birthDate != null) {
                    try {
                        indiBirth.add(new IndiEntry(birthDate.getStart().getJulianDay(), indi, birthDate));
                    } catch (GedcomException ex) {
                        //Exceptions.printStackTrace(ex);
                    }
                }

                // décès
                PropertyDate deathDate = indi.getDeathDate();
                if (deathDate != null) {
                    try {
                        indiDeath.add(new IndiEntry(deathDate.getStart().getJulianDay(), indi, deathDate));
                    } catch (GedcomException ex) {
                        //Exceptions.printStackTrace(ex);
                    }
                }

                // evenements divers
                for (TagPath tagPath : miscIndiTagPathList) {
                    for (Property indiEventDate : indi.getProperties(tagPath)) {
                        try {
                            indiMisc.add(new IndiEntry(((PropertyDate) indiEventDate).getStart().getJulianDay(), indi, indiEventDate));
                        } catch (GedcomException ex) {
                            // j'ignore cet evenement
                        }
                    }
                }
            }
            Collections.sort(indiBirth);
            Collections.sort(indiDeath);
            Collections.sort(indiMisc);            
        }
    }
    
    private void createFamList(ArrayList<FamEntry> famMarriage, ArrayList<FamEntry> famMisc) {
        if (gedcom != null && showGedcomLink == true) {
            for (Fam fam : gedcom.getFamilies()) {

                // evenements divers
                for (Property marriageDate : fam.getProperties(marrDateTag)) {
                    try {
                        famMarriage.add(new FamEntry(((PropertyDate) marriageDate).getStart().getJulianDay(), fam, marriageDate));
                    } catch (GedcomException ex) {
                        // j'ignore cet evenement
                    }
                }
                // evenements divers

                for (TagPath tagPath : miscFamTagPathList) {
                    for (Property eventDate : fam.getProperties(tagPath)) {
                        try {
                            famMisc.add(new FamEntry(((PropertyDate) eventDate).getStart().getJulianDay(), fam, eventDate));
                        } catch (GedcomException ex) {
                            // j'ignore cet evenement
                        }
                    }
                }
            }
            Collections.sort(famMarriage); 
            Collections.sort(famMisc); 
        }
    }
    
    
    private void compareList( ArrayList<RecordEntry> recordEntries , ArrayList<IndiEntry> indiEntries ) {
        
        int recordCount = 0;
        int recordCountMax = recordEntries.size();
        int indiCount = 0;
        int indiCountMax = indiEntries.size();
        while (indiCount < indiCountMax && recordCount < recordCountMax) {
            RecordEntry recordEntry = recordEntries.get(recordCount);
            IndiEntry indiEntry = indiEntries.get(indiCount);
            int result = recordEntries.get(recordCount).compareTo(indiEntries.get(indiCount));
            if (result == 0 ) {
                gedcomLinkList.put(recordEntry.record, new GedcomLink(recordEntry.record, indiEntry.entity, indiEntry.property));
                recordCount++;
            } else if (result > 0) {
                indiCount++;
            } else {
                recordCount++;
            }

        }
    }
    
    private void compareListMarriage( ArrayList<RecordEntry> recordEntries , ArrayList<FamEntry> famEntries ) {
        
        int recordCount = 0;
        int recordCountMax = recordEntries.size();
        int indiCount = 0;
        int indiCountMax = famEntries.size();
        while (indiCount < indiCountMax && recordCount < recordCountMax) {
            RecordEntry recordEntry = recordEntries.get(recordCount);
            FamEntry indiEntry = famEntries.get(indiCount);
            int result = recordEntries.get(recordCount).compareTo(famEntries.get(indiCount));
            if (result == 0 ) {
                gedcomLinkList.put(recordEntry.record, new GedcomLink(recordEntry.record, indiEntry.entity, indiEntry.property));
                recordCount++;
            } else if (result > 0) {
                indiCount++;
            } else {
                recordCount++;
            }

        }
    }
    
        public GedcomLink getGedcomLink(Record record) {
        return gedcomLinkList.get(record);
    }
    
    
    
    public void removeRecord(Record record) {
        gedcomLinkList.remove(record); 
    }

    public void removeAll() {
        gedcomLinkList.clear(); 
    }
    
    
//    public void init1(final RecordModel recordModel, Gedcom gedcom, boolean state) {
//        this.gedcom = gedcom;
//        this.showGedcomLink = state;
//    
//        if (gedcom != null && showGedcomLink == true ) {            
//            // je recupere les individus du gedcom dans l'attibut temporaire pour optimiser les recherches
//            indiList = gedcom.getIndis();
//            // je recupere les familles du gedcom dans l'attibut temporaire pour optimiser les recherches 
//            famList = gedcom.getFamilies();
//            
//            // je nettoie le reultat avant de faire la recherche 
//            gedcomLinkList.clear();
//            int rwoCount = recordModel.getRowCount();
//            for (int i = 0; i < rwoCount; i++) {
//                Record record = recordModel.getRecord(i);
//                addRecord(record);
//            }
//            // j'affiche le resultat 
//            recordModel.fireRecordModelUpdated(0, recordModel.getRowCount() - 1);
//
//            // je nettoie les attributs temporaires;
//            //indiList.clear();
//            //famList.clear();
//            
//            for (GedcomLink link :gedcomLinkList.values()) {
//                System.out.println(link.getRecord().getType() + "; "
//                        + link.getProperty().getPath().toString()+ "; "
//                        + link.getRecord().getFieldValue(FieldType.indiLastName) + " "
//                        + link.getRecord().getFieldValue(FieldType.indiFirstName) + "; "
//                        + link.getEntity().toString());
//            }
//            
//        } else {
//            gedcomLinkList.clear();
//            recordModel.fireRecordModelUpdated(0, recordModel.getRowCount() - 1);
//        }
//
//    }
    

//    private GedcomLink findBirth(Record birthRecord) {
//        GedcomLink gedcomLink = null;
//        
//        Field recordBirthDate = birthRecord.getField(FieldType.indiBirthDate);
//        if ( recordBirthDate == null || recordBirthDate.isEmpty() ) {
//            recordBirthDate = birthRecord.getField(FieldType.eventDate);   
//        } 
//        
//        if( recordBirthDate != null ) {
//
//            for (Indi indi : indiList) {
//
//                // date de naissance egale 
//                PropertyDate gedcomBirthDate = indi.getBirthDate();
//
//                if ( gedcomBirthDate == null
//                     ||! recordBirthDate.equalsProperty(gedcomBirthDate)) {
//                    continue;
//                }
//
//                // meme sexe de l'enfant
//                if ( indi.getSex() == PropertySex.UNKNOWN
//                     || birthRecord.getField(FieldType.indiSex) == null 
//                     ||! birthRecord.getField(FieldType.indiSex).equalsProperty(indi.getProperty("SEX")) ) {
//                    continue;
//                }
//
//                // meme nom de l'enfant
//                if ( birthRecord.getFieldValue(Record.FieldType.indiLastName).isEmpty()
//                     || !MergeQuery.isSameLastName(birthRecord.getFieldValue(Record.FieldType.indiLastName), indi.getLastName())) {
//                    continue;
//                }
//
//                // meme prenom de l'enfant
//                if ( birthRecord.getFieldValue(Record.FieldType.indiFirstName).isEmpty()
//                        || !MergeQuery.isSameFirstName(birthRecord.getFieldValue(Record.FieldType.indiFirstName), indi.getFirstName())) {
//                    continue;
//                }
//                gedcomLink =  new GedcomLink(birthRecord, indi, gedcomBirthDate);
//                break;
//
//            }
//        }
//
//        
//        return gedcomLink;
//    }
//    
//    private GedcomLink findDeath(Record deathRecord) {
//        GedcomLink gedcomLink = null;
//        
//        Field recordDeathDate = deathRecord.getField(FieldType.eventDate);
//        if (recordDeathDate != null && !recordDeathDate.isEmpty() ) {
//
//            for (Indi indi : indiList) {
//
//                // date de deces egale   
//                PropertyDate gedcomDeathDate = indi.getDeathDate();
//
//                if (!recordDeathDate.equalsProperty(gedcomDeathDate) || !gedcomDeathDate.getStart().isValid() ) {
//                    continue;
//                }
//
//                // meme sexe de l'enfant
//                if ( indi.getSex() == PropertySex.UNKNOWN
//                        || deathRecord.getField(FieldType.indiSex) == null
//                        || !deathRecord.getField(FieldType.indiSex).equalsProperty(indi.getProperty("SEX")) ) {
//                    continue;
//                }
//
//                // meme nom de l'enfant
//                if ( deathRecord.getFieldValue(Record.FieldType.indiLastName).isEmpty()
//                        || !MergeQuery.isSameLastName(deathRecord.getFieldValue(Record.FieldType.indiLastName), indi.getLastName())) {
//                    continue;
//                }
//
//                // meme prenom de l'enfant
//                if ( deathRecord.getFieldValue(Record.FieldType.indiFirstName).isEmpty()
//                        || !MergeQuery.isSameFirstName(deathRecord.getFieldValue(Record.FieldType.indiFirstName), indi.getFirstName())) {
//                    continue;
//                }
//
//                gedcomLink =  new GedcomLink(deathRecord, indi, gedcomDeathDate);
//
//                break;
//
//            }
//        }
//
//        return gedcomLink;
//    }
//    
//    private GedcomLink findMarriage(Record marriageRecord, TagPath ... tagPathList) {
//        GedcomLink gedcomLink = null;
//
//        Field recordMarriageDate = marriageRecord.getField(FieldType.eventDate);
//        if (recordMarriageDate != null && !recordMarriageDate.isEmpty() ) {
//            Property gedcomFamMarriageDate = null;
//
//            for (Fam fam : famList) {
//                Indi husband = fam.getHusband();
//                Indi wife = fam.getWife();
//
//                if (husband == null && wife == null) {
//                    continue;
//                }
//
//                // date de mariage egale
//                boolean eventDateFound = false;
//                for (TagPath tagPath : tagPathList) {
//                    for (Property famEventDate : fam.getProperties(tagPath)) {
//                        if (recordMarriageDate.equalsProperty(famEventDate)) {
//                            eventDateFound = true;
//                            gedcomFamMarriageDate = famEventDate;
//                            break;
//                        }
//                    }
//                    if (eventDateFound) {
//                        break;
//                    }
//                }
//                if (!eventDateFound) {
//                    continue;
//                }
//
//                if (husband != null) {
//
//                    // meme nom de l'epoux
//                    if (marriageRecord.getFieldValue(Record.FieldType.indiLastName).isEmpty()
//                            || !isSameLastName(marriageRecord.getFieldValue(Record.FieldType.indiLastName), husband.getLastName())) {
//                        continue;
//                    }
//
//                    //meme prénom de l'epoux
//                    if ( marriageRecord.getFieldValue(Record.FieldType.indiFirstName).isEmpty()
//                            || !isSameFirstName(marriageRecord.getFieldValue(Record.FieldType.indiFirstName), husband.getFirstName())) {
//                        continue;
//                    }
//
//                }
//
//                if (wife != null) {
//                    // meme nom de l'epouse
//                    if ( marriageRecord.getFieldValue(Record.FieldType.wifeLastName).isEmpty()
//                            || !isSameLastName(marriageRecord.getFieldValue(Record.FieldType.wifeLastName), wife.getLastName())) {
//                        continue;
//                    }
//                    //meme prénom de l'epouse
//                    if ( marriageRecord.getFieldValue(Record.FieldType.wifeFirstName).isEmpty()
//                            || !isSameFirstName(marriageRecord.getFieldValue(Record.FieldType.wifeFirstName), wife.getFirstName())) {
//                        continue;
//                    }
//
//                }
//
//                gedcomLink = new GedcomLink(marriageRecord, fam, gedcomFamMarriageDate);
//
//                break;
//
//            }
//        }
//
//        return gedcomLink;
//    }
//    
//    
//    private GedcomLink findMisc(Record miscRecord) {
//        GedcomLink gedcomLink = null;
//
//        
//        // j'utilise la date d'insinuation si elle est renseignée
//        Field recordEventDate = miscRecord.getField(FieldType.secondDate);
//        if( recordEventDate == null || recordEventDate.isEmpty()) {
//            // si la date d'insinuation n'est pas renseignée, j'utilise la date de l'évènement
//            recordEventDate = miscRecord.getField(FieldType.eventDate);
//        }
//        if (recordEventDate != null && !recordEventDate.isEmpty()) {
//            String eventType = miscRecord.getFieldValue(Record.FieldType.eventType).toLowerCase();
//            if (eventType.contains("cm") || eventType.contains("mariage")) {
//                // je cherche l'évènement sur la famille
//                gedcomLink = findMarriage(miscRecord, marbDateTag, marcDateTag, marlDateTag, evenDateTag);
//
//            }
//            if (gedcomLink == null) {
//                Property gedcomMiscEventDate = null;
//                // je cherche l'évènement sur l'individu
//                TagPath[] tagPathList = {indiEvenDateTag, willDateTag};
//                for (Indi indi : indiList) {
//                    boolean eventDateFound = false;
//                    for (TagPath tagPath : tagPathList) {
//                        for (Property indiEventDate : indi.getProperties(tagPath)) {
//                            if (recordEventDate.equalsProperty(indiEventDate)) {
//                                eventDateFound = true;
//                                gedcomMiscEventDate = indiEventDate;
//                                break;
//                            }
//                        }
//                        if (eventDateFound) {
//                            break;
//                        }
//                    }
//                    if (!eventDateFound) {
//                        continue;
//                    }
//                    
//                    // meme sexe de l'individu
//                    if ( indi.getSex() == PropertySex.UNKNOWN
//                        || miscRecord.getField(FieldType.indiSex) == null
//                        || !miscRecord.getField(FieldType.indiSex).equalsProperty(indi.getProperty("SEX")) ) {
//                        continue;
//                    }
//                    
//                    // meme nom de l'individu
//                    if (miscRecord.getFieldValue(Record.FieldType.indiLastName).isEmpty()
//                        || !MergeQuery.isSameLastName(miscRecord.getFieldValue(FieldType.indiLastName), indi.getLastName())) {
//                        continue;
//                    }
//                    
//                    // meme prenom de l'individu
//                    if ( miscRecord.getFieldValue(Record.FieldType.indiFirstName).isEmpty()
//                         || !MergeQuery.isSameFirstName(miscRecord.getFieldValue(FieldType.indiFirstName), indi.getFirstName())) {
//                        continue;
//                    }
//                    
//                    gedcomLink = new GedcomLink(miscRecord, indi, gedcomMiscEventDate);
//
//                    break;
//                    
//                }
//            }
//        }
//
//
//        return gedcomLink;
//    }
//    
    /**
     * compare deux string UTF8 en ignorant la casse et les accents 
     * 
     * @param str1
     * @param str2
     * @return 
     */
    static private int compareString(String str1 , String str2) {
        int len1 = str1.length();
        int len2 = str2.length();
        int n = Math.min(len1, len2);
        //char v1[] = str1.toCharArray();
        //char v2[] = str2.toCharArray();
        int k = 0;
        int lim = n ;
        while (k < lim) {
            char c1 = str1.charAt(k);
            char c2 = str2.charAt(k);
            if (c1 != c2) {
                c1 = utf8Map[c1];
                c2 = utf8Map[c2];
                if (c1 != c2) {
                    return c1 - c2;
                }
            }
            k++;
        }
        return len1 - len2;
    }
    
    static private class RecordEntry implements Comparable<RecordEntry>{
        private final int julianDay;
        private final Record record;

        public RecordEntry(int julianDay, Record record) {
            this.julianDay = julianDay;
            this.record = record;
        }

        @Override
        public int compareTo(RecordEntry that) {
            int result = this.julianDay - that.julianDay;
            if (result == 0) {
                result = compareString(this.record.getFieldValue(FieldType.indiLastName),that.record.getFieldValue(FieldType.indiLastName));
                if (result == 0) {
                    result = compareString(this.record.getFieldValue(FieldType.indiFirstName), that.record.getFieldValue(FieldType.indiFirstName));
                }
            }
            return result;
        }
        
        public int compareTo(IndiEntry indiEntry) {
            if (indiEntry.julianDay == this.julianDay) {
                Indi indi = indiEntry.entity;
                // meme sexe de l'enfant
                if ( indi.getSex() != PropertySex.UNKNOWN
                        && record.getField(FieldType.indiSex) != null
                        && record.getField(FieldType.indiSex).equalsProperty(indi.getProperty("SEX"))
                        && !record.getFieldValue(Record.FieldType.indiLastName).isEmpty()
                        && MergeQuery.isSameLastName(record.getFieldValue(Record.FieldType.indiLastName), indi.getLastName())
                        && !record.getFieldValue(Record.FieldType.indiFirstName).isEmpty()
                        && MergeQuery.isSameFirstName(record.getFieldValue(Record.FieldType.indiFirstName), indi.getFirstName())) {
                    return 0;
                } else {
                    // je compare le nom et le prénom
                    //int result = record.getFieldValue(Record.FieldType.indiLastName).compareTo(indi.getLastName());
                    int result = compareString(record.getFieldValue(Record.FieldType.indiLastName), indi.getLastName());
                    if (result == 0) {
                        result = compareString(record.getFieldValue(Record.FieldType.indiFirstName), indi.getFirstName());
                        if (result > 0) {
                            return 1;
                        } else {
                            return -1;
                        }
                    } else if (result > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                }

            } else {
                return this.julianDay - indiEntry.julianDay;
            }
        }
        
        public int compareTo(FamEntry famEntry) {
            if (famEntry.julianDay == this.julianDay) {
                Indi husband = famEntry.entity.getHusband();
                Indi wife = famEntry.entity.getWife();

                if (husband == null && wife == null) {
                    return -1;
                }

                //  // meme nom et prénom de l'epoux  ou même nom et prénom de l'épouse
                if ( ( husband != null
                        && ! record.getFieldValue(Record.FieldType.indiLastName).isEmpty()
                        && ! record.getFieldValue(Record.FieldType.indiFirstName).isEmpty()
                        && isSameLastName(record.getFieldValue(Record.FieldType.indiLastName), husband.getLastName())
                        && isSameFirstName(record.getFieldValue(Record.FieldType.indiFirstName), husband.getFirstName())
                        )
                        ||
                        ( wife != null
                          && ! record.getFieldValue(Record.FieldType.wifeLastName).isEmpty()
                          && ! record.getFieldValue(Record.FieldType.wifeFirstName).isEmpty()
                          && isSameLastName(record.getFieldValue(Record.FieldType.wifeLastName), wife.getLastName())
                          && isSameFirstName(record.getFieldValue(Record.FieldType.wifeFirstName), wife.getFirstName())
                        )
                        
                        ) {
                    return 0;
                } else {
                    // je compare le nom et le prénom du mari
                    int result = compareString(record.getFieldValue(Record.FieldType.indiLastName), husband.getLastName());
                    if (result == 0) {
                        result = compareString(record.getFieldValue(Record.FieldType.indiFirstName), husband.getFirstName());
                        if (result > 0) {
                            return 1;
                        } else {
                            return -1;
                        }
                    } else if (result > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            } else {
                 return this.julianDay - famEntry.julianDay;
            }
        }
    }

    static private class IndiEntry implements Comparable<IndiEntry> {
        private final int julianDay;
        private final Indi entity;
        private final Property property;

        public IndiEntry(int julianDay, Indi entity, Property property) {
            this.julianDay = julianDay;
            this.entity = entity;
            this.property = property;
        }

        @Override
        public int compareTo(IndiEntry that) {
            int result = this.julianDay - that.julianDay;
            if( result == 0) {
                result = compareString(this.entity.getLastName(), that.entity.getLastName());
                if( result == 0) {
                    result = compareString(this.entity.getFirstName(), that.entity.getFirstName());
                }
            }
            return result ;
        }
    }
    
    static private class FamEntry implements Comparable<FamEntry>{
        private final int julianDay;
        private final Fam entity;
        private final Property property;

        public FamEntry(int julianDay, Fam entity, Property property) {
            this.julianDay = julianDay;
            this.entity = entity;
            this.property = property;
        }
        
        @Override
        public int compareTo(FamEntry that) {
            int result = this.julianDay - that.julianDay;
            if( result == 0 ) {
                if( this.entity.getHusband() != null && that.entity.getHusband() != null ) {
                    result = compareString(this.entity.getHusband().getLastName(), that.entity.getHusband().getLastName());
                    if( result == 0) {
                        result = compareString(this.entity.getHusband().getFirstName(), that.entity.getHusband().getFirstName());
                    }
                }
            }
            return result ;
        }
    }
    
    
    /**
     * map to convert to lower case and remove accents
     */
    private final static char[] utf8Map = {
        '\u0000', // 	00	<control>
        '\u0001', // 	01	<control>
        '\u0002', // 	02	<control>
        '\u0003', // 	03	<control>
        '\u0004', // 	04	<control>
        '\u0005', // 	05	<control>
        '\u0006', // 	06	<control>
        '\u0007', // 	07	<control>
        '\u0008', // 	08	<control>
        '\u0009', // 	09	<control>
        10, // 	0a	<control>
        '\u000B', // 	0b	<control>
        '\u000C', // 	0c	<control>
        13, //	0d      <control>
        '\u000E', // 	0e	<control>
        '\u000F', // 	0f	<control>
        '\u0010', // 	10	<control>
        '\u0011', // 	11	<control>
        '\u0012', // 	12	<control>
        '\u0013', // 	13	<control>
        '\u0014', // 	14	<control>
        '\u0015', // 	15	<control>
        '\u0016', // 	16	<control>
        '\u0017', //  	17	<control>
        '\u0018', //  	18	<control>
        '\u0019', //  	19	<control>
        '\u001A', //  	1a	<control>
        '\u001B', //  	1b	<control>
        '\u001C', //  	1c	<control>
        '\u001D', //  	1d	<control>
        '\u001E', //  	1e	<control>
        '\u001F', //  	1f	<control>
        '\u0020', // 	20	SPACE
        '\u0021', // !	21	EXCLAMATION MARK
        '\u0022', // "	22	QUOTATION MARK
        '\u0023', // #	23	NUMBER SIGN
        '\u0024', // $	24	DOLLAR SIGN
        '\u0025', // %	25	PERCENT SIGN
        '\u0026', // &	26	AMPERSAND
        27, // 	27	APOSTROPHE
        '\u0028', // (	28	LEFT PARENTHESIS
        '\u0029', // )	29	RIGHT PARENTHESIS
        '\u002A', // *	2a	ASTERISK
        '\u002B', // +	2b	PLUS SIGN
        '\u002C', // ,	2c	COMMA
        '\u002D', // -	2d	HYPHEN-MINUS
        '\u002E', // .	2e	FULL STOP
        '\u002F', // /	2f	SOLIDUS
        '\u0030', // 0	30	DIGIT ZERO
        '\u0031', // 1	31	DIGIT ONE
        '\u0032', // 2	32	DIGIT TWO
        '\u0033', // 3	33	DIGIT THREE
        '\u0034', // 4	34	DIGIT FOUR
        '\u0035', // 5	35	DIGIT FIVE
        '\u0036', // 6	36	DIGIT SIX
        '\u0037', // 7	37	DIGIT SEVEN
        '\u0038', // 8	38	DIGIT EIGHT
        '\u0039', // 9	39	DIGIT NINE
        '\u003A', // :	3a	COLON
        '\u003B', // ;	3b	SEMICOLON
        '\u003C', // <	3c	LESS-THAN SIGN
        '\u003D', // =	3d	EQUALS SIGN
        '\u003E', // >	3e	GREATER-THAN SIGN
        '\u003F', // ?	3f	QUESTION MARK
        '\u0040', // @	40	COMMERCIAL AT
        'a', // A	41	LATIN CAPITAL LETTER A
        'b', // B	42	LATIN CAPITAL LETTER B
        'c', // C	43	LATIN CAPITAL LETTER C
        'd', // D	44	LATIN CAPITAL LETTER D
        'e', // E	45	LATIN CAPITAL LETTER E
        'f', // F	46	LATIN CAPITAL LETTER F
        'g', // G	47	LATIN CAPITAL LETTER G
        'h', // H	48	LATIN CAPITAL LETTER H
        'i', // I	49	LATIN CAPITAL LETTER I
        'j', // J	4a	LATIN CAPITAL LETTER J
        'k', // K	4b	LATIN CAPITAL LETTER K
        'l', // L	4c	LATIN CAPITAL LETTER L
        'm', // M	4d	LATIN CAPITAL LETTER M
        'n', // N	4e	LATIN CAPITAL LETTER N
        'o', // O	4f	LATIN CAPITAL LETTER O
        'p', // P	50	LATIN CAPITAL LETTER P
        'q', // Q	51	LATIN CAPITAL LETTER Q
        'r', // R	52	LATIN CAPITAL LETTER R
        's', // S	53	LATIN CAPITAL LETTER S
        't', // T	54	LATIN CAPITAL LETTER T
        'u', // U	55	LATIN CAPITAL LETTER U
        'v', // V	56	LATIN CAPITAL LETTER V
        'w', // W	57	LATIN CAPITAL LETTER W
        'x', // X	58	LATIN CAPITAL LETTER X
        'y', // Y	59	LATIN CAPITAL LETTER Y
        'z', // Z	5a	LATIN CAPITAL LETTER Z
        '\u005B', // [	5b	LEFT SQUARE BRACKET
        92, // \	5c	REVERSE SOLIDUS
        '\u005D', // ]	5d	RIGHT SQUARE BRACKET
        '\u005E', // ^	5e	CIRCUMFLEX ACCENT
        '\u005F', // _	5f	LOW LINE
        '\u0060', // `	60	GRAVE ACCENT
        '\u0061', // a	61	LATIN SMALL LETTER A
        '\u0062', // b	62	LATIN SMALL LETTER B
        '\u0063', // c	63	LATIN SMALL LETTER C
        '\u0064', // d	64	LATIN SMALL LETTER D
        '\u0065', // e	65	LATIN SMALL LETTER E
        '\u0066', // f	66	LATIN SMALL LETTER F
        '\u0067', // g	67	LATIN SMALL LETTER G
        '\u0068', // h	68	LATIN SMALL LETTER H
        '\u0069', // i	69	LATIN SMALL LETTER I
        '\u006A', // j	6a	LATIN SMALL LETTER J
        '\u006B', // k	6b	LATIN SMALL LETTER K
        '\u006C', // l	6c	LATIN SMALL LETTER L
        '\u006D', // m	6d	LATIN SMALL LETTER M
        '\u006E', // n	6e	LATIN SMALL LETTER N
        '\u006F', // o	6f	LATIN SMALL LETTER O
        '\u0070', // p	70	LATIN SMALL LETTER P
        '\u0071', // q	71	LATIN SMALL LETTER Q
        '\u0072', // r	72	LATIN SMALL LETTER R
        '\u0073', // s	73	LATIN SMALL LETTER S
        '\u0074', // t	74	LATIN SMALL LETTER T
        '\u0075', // u	75	LATIN SMALL LETTER U
        '\u0076', // v	76	LATIN SMALL LETTER V
        '\u0077', // w	77	LATIN SMALL LETTER W
        '\u0078', // x	78	LATIN SMALL LETTER X
        '\u0079', // y	79	LATIN SMALL LETTER Y
        '\u007A', // z	7a	LATIN SMALL LETTER Z
        '\u007B', // {	7b	LEFT CURLY BRACKET
        '\u007C', // |	7c	VERTICAL LINE
        '\u007D', // }	7d	RIGHT CURLY BRACKET
        '\u007E', // ~	7e	TILDE
        '\u007F', //  	7f	<control>
        '\u0080', //  	c2 80	<control>
        '\u0081', //  	c2 81	<control>
        '\u0082', //  	c2 82	<control>
        '\u0083', //  	c2 83	<control>
        '\u0084', //  	c2 84	<control>
        '\u0085', //  	c2 85	<control>
        '\u0086', //  	c2 86	<control>
        '\u0087', //  	c2 87	<control>
        '\u0088', //  	c2 88	<control>
        '\u0089', //  	c2 89	<control>
        '\u008A', //  	c2 8a	<control>
        '\u008B', //  	c2 8b	<control>
        '\u008C', //  	c2 8c	<control>
        '\u008D', //  	c2 8d	<control>
        '\u008E', //  	c2 8e	<control>
        '\u008F', //  	c2 8f	<control>
        '\u0090', //  	c2 90	<control>
        '\u0091', //  	c2 91	<control>
        '\u0092', //  	c2 92	<control>
        '\u0093', //  	c2 93	<control>
        '\u0094', //  	c2 94	<control>
        '\u0095', //  	c2 95	<control>
        '\u0096', //  	c2 96	<control>
        '\u0097', //  	c2 97	<control>
        '\u0098', //  	c2 98	<control>
        '\u0099', //  	c2 99	<control>
        '\u009A', //  	c2 9a	<control>
        '\u009B', //  	c2 9b	<control>
        '\u009C', //  	c2 9c	<control>
        '\u009D', //  	c2 9d	<control>
        '\u009E', //  	c2 9e	<control>
        '\u009F', //  	c2 9f	<control>
        '\u00A0', //  	c2 a0	NO-BREAK SPACE
        '\u00A1', // ¡	c2 a1	INVERTED EXCLAMATION MARK
        '\u00A2', // ¢	c2 a2	CENT SIGN
        '\u00A3', // £	c2 a3	POUND SIGN
        '\u00A4', // ¤	c2 a4	CURRENCY SIGN
        '\u00A5', // ¥	c2 a5	YEN SIGN
        '\u00A6', // ¦	c2 a6	BROKEN BAR
        '\u00A7', // §	c2 a7	SECTION SIGN
        '\u00A8', // ¨	c2 a8	DIAERESIS
        '\u00A9', // ©	c2 a9	COPYRIGHT SIGN
        '\u00AA', // ª	c2 aa	FEMININE ORDINAL INDICATOR
        '\u00AB', // «	c2 ab	LEFT-POINTING DOUBLE ANGLE QUOTATION MARK
        '\u00AC', // ¬	c2 ac	NOT SIGN
        '\u00AD', // ­	c2 ad	SOFT HYPHEN
        '\u00AE', // ®	c2 ae	REGISTERED SIGN
        '\u00AF', // ¯	c2 af	MACRON
        '\u00B0', // °	c2 b0	DEGREE SIGN
        '\u00B1', // ±	c2 b1	PLUS-MINUS SIGN
        '\u00B2', // ²	c2 b2	SUPERSCRIPT TWO
        '\u00B3', // ³	c2 b3	SUPERSCRIPT THREE
        '\u00B4', // ´	c2 b4	ACUTE ACCENT
        '\u00B5', // µ	c2 b5	MICRO SIGN
        '\u00B6', // ¶	c2 b6	PILCROW SIGN
        '\u00B7', // ·	c2 b7	MIDDLE DOT
        '\u00B8', // ¸	c2 b8	CEDILLA
        '\u00B9', // ¹	c2 b9	SUPERSCRIPT ONE
        '\u00BA', // º	c2 ba	MASCULINE ORDINAL INDICATOR
        '\u00BB', // »	c2 bb	RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK
        '\u00BC', // ¼	c2 bc	VULGAR FRACTION ONE QUARTER
        '\u00BD', // ½	c2 bd	VULGAR FRACTION ONE HALF
        '\u00BE', // ¾	c2 be	VULGAR FRACTION THREE QUARTERS
        '\u00BF', // ¿	c2 bf	INVERTED QUESTION MARK
        'a', // À	c3 80	LATIN CAPITAL LETTER A WITH GRAVE
        'a', // Á	c3 81	LATIN CAPITAL LETTER A WITH ACUTE
        'a', // Â	c3 82	LATIN CAPITAL LETTER A WITH CIRCUMFLEX
        'a', // Ã	c3 83	LATIN CAPITAL LETTER A WITH TILDE
        'a', // Ä	c3 84	LATIN CAPITAL LETTER A WITH DIAERESIS
        'a', // Å	c3 85	LATIN CAPITAL LETTER A WITH RING ABOVE
        'a', // Æ	c3 86	LATIN CAPITAL LETTER AE
        'c', // Ç	c3 87	LATIN CAPITAL LETTER C WITH CEDILLA
        'e', // È	c3 88	LATIN CAPITAL LETTER E WITH GRAVE
        'e', // É	c3 89	LATIN CAPITAL LETTER E WITH ACUTE
        'e', // Ê	c3 8a	LATIN CAPITAL LETTER E WITH CIRCUMFLEX
        'e', // Ë	c3 8b	LATIN CAPITAL LETTER E WITH DIAERESIS
        'i', // Ì	c3 8c	LATIN CAPITAL LETTER I WITH GRAVE
        'i', // Í	c3 8d	LATIN CAPITAL LETTER I WITH ACUTE
        'i', // Î	c3 8e	LATIN CAPITAL LETTER I WITH CIRCUMFLEX
        'i', // Ï	c3 8f	LATIN CAPITAL LETTER I WITH DIAERESIS
        'e', // Ð	c3 90	LATIN CAPITAL LETTER ETH
        'n', // Ñ	c3 91	LATIN CAPITAL LETTER N WITH TILDE
        'o', // Ò	c3 92	LATIN CAPITAL LETTER O WITH GRAVE
        'o', // Ó	c3 93	LATIN CAPITAL LETTER O WITH ACUTE
        'o', // Ô	c3 94	LATIN CAPITAL LETTER O WITH CIRCUMFLEX
        'o', // Õ	c3 95	LATIN CAPITAL LETTER O WITH TILDE
        'o', // Ö	c3 96	LATIN CAPITAL LETTER O WITH DIAERESIS
        '\u00D7', // ×	c3 97	MULTIPLICATION SIGN
        'o', // Ø	c3 98	LATIN CAPITAL LETTER O WITH STROKE
        'u', // Ù	c3 99	LATIN CAPITAL LETTER U WITH GRAVE
        'u', // Ú	c3 9a	LATIN CAPITAL LETTER U WITH ACUTE
        'u', // Û	c3 9b	LATIN CAPITAL LETTER U WITH CIRCUMFLEX
        'u', // Ü	c3 9c	LATIN CAPITAL LETTER U WITH DIAERESIS
        'y', // Ý	c3 9d	LATIN CAPITAL LETTER Y WITH ACUTE
        't', // Þ	c3 9e	LATIN CAPITAL LETTER THORN
        's', // ß	c3 9f	LATIN SMALL LETTER SHARP S
        'a', // à	c3 a0	LATIN SMALL LETTER A WITH GRAVE
        'a', // á	c3 a1	LATIN SMALL LETTER A WITH ACUTE
        'a', // â	c3 a2	LATIN SMALL LETTER A WITH CIRCUMFLEX
        'a', // ã	c3 a3	LATIN SMALL LETTER A WITH TILDE
        'a', // ä	c3 a4	LATIN SMALL LETTER A WITH DIAERESIS
        'a', // å	c3 a5	LATIN SMALL LETTER A WITH RING ABOVE
        'a', // æ	c3 a6	LATIN SMALL LETTER AE
        'c', // ç	c3 a7	LATIN SMALL LETTER C WITH CEDILLA
        'e', // è	c3 a8	LATIN SMALL LETTER E WITH GRAVE
        'e', // é	c3 a9	LATIN SMALL LETTER E WITH ACUTE
        'e', // ê	c3 aa	LATIN SMALL LETTER E WITH CIRCUMFLEX
        'e', // ë	c3 ab	LATIN SMALL LETTER E WITH DIAERESIS
        'i', // ì	c3 ac	LATIN SMALL LETTER I WITH GRAVE
        'i', // í	c3 ad	LATIN SMALL LETTER I WITH ACUTE
        'i', // î	c3 ae	LATIN SMALL LETTER I WITH CIRCUMFLEX
        'i', // ï	c3 af	LATIN SMALL LETTER I WITH DIAERESIS
        'e', // ð	c3 b0	LATIN SMALL LETTER ETH
        'n', // ñ	c3 b1	LATIN SMALL LETTER N WITH TILDE
        'o', // ò	c3 b2	LATIN SMALL LETTER O WITH GRAVE
        'o', // ó	c3 b3	LATIN SMALL LETTER O WITH ACUTE
        'o', // ô	c3 b4	LATIN SMALL LETTER O WITH CIRCUMFLEX
        'o', // õ	c3 b5	LATIN SMALL LETTER O WITH TILDE
        'o', // ö	c3 b6	LATIN SMALL LETTER O WITH DIAERESIS
        '\u00F7', // ÷	c3 b7	DIVISION SIGN
        'o', // ø	c3 b8	LATIN SMALL LETTER O WITH STROKE
        'u', // ù	c3 b9	LATIN SMALL LETTER U WITH GRAVE
        'u', // ú	c3 ba	LATIN SMALL LETTER U WITH ACUTE
        'u', // û	c3 bb	LATIN SMALL LETTER U WITH CIRCUMFLEX
        'u', // ü	c3 bc	LATIN SMALL LETTER U WITH DIAERESIS
        'y', // ý	c3 bd	LATIN SMALL LETTER Y WITH ACUTE
        't', // þ	c3 be	LATIN SMALL LETTER THORN
        'y'  // ÿ	c3 bf	LATIN SMALL LETTER Y WITH DIAERESIS
    };
}
