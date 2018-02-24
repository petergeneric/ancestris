 package ancestris.modules.releve.model;

import ancestris.modules.releve.merge.MergeQuery;
import static ancestris.modules.releve.merge.MergeQuery.isSameFirstName;
import static ancestris.modules.releve.merge.MergeQuery.isSameLastName;
import ancestris.modules.releve.model.Record.FieldType;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author Michel
 */


public class GedcomLinkProvider {
    private final HashMap<Record, GedcomLink> gedcomLinkList = new HashMap<Record, GedcomLink>();
    private Gedcom gedcom = null; 
    private boolean showGedcomLink = false;
    
    static final TagPath marrDateTag = new TagPath("FAM:MARR:DATE");
    static final TagPath marbDateTag = new TagPath("FAM:MARB:DATE");
    static final TagPath marcDateTag = new TagPath("FAM:MARC:DATE");
    static final TagPath marlDateTag = new TagPath("FAM:MARL:DATE");
    static final TagPath evenDateTag = new TagPath("FAM:EVEN:DATE");
    static final TagPath indiEvenDateTag = new TagPath("INDI:EVEN:DATE");
    static final TagPath willDateTag = new TagPath("INDI:WILL:DATE");
    
    // attributs temporaires 
    Collection<Indi> indiList;
    Collection<Fam> famList;
    
    public void init(final RecordModel recordModel, Gedcom gedcom, boolean state) {
        this.gedcom = gedcom;
        this.showGedcomLink = state;

        if (gedcom != null && showGedcomLink == true ) {            
            // je recupere les individus du gedcom dans l'attibut temporaire pour optimiser les recherches
            indiList = gedcom.getIndis();
            // je recupere les familles du gedcom dans l'attibut temporaire pour optimiser les recherches 
            famList = gedcom.getFamilies();
            
            // je nettoie le reultat avant de faire la recherche 
            gedcomLinkList.clear();
            int rwoCount = recordModel.getRowCount();
            for (int i = 0; i < rwoCount; i++) {
                Record record = recordModel.getRecord(i);
                addRecord(record);
            }
            // j'affiche le resultat 
            recordModel.fireRecordModelUpdated(0, recordModel.getRowCount() - 1);

            // je nettoie les attributs temporaires;
            //indiList.clear();
            //famList.clear();
            
        } else {
            gedcomLinkList.clear();
            recordModel.fireRecordModelUpdated(0, recordModel.getRowCount() - 1);
        }

    }
    
    public GedcomLink getgedcomLink(Record record) {
        return gedcomLinkList.get(record);
    }
    
    
    public void addRecord(Record record) {
        if (record != null  && gedcom != null && showGedcomLink == true) {
            GedcomLink gedcomLink;
            // je recherche l'évènement dans le gedcom
            switch (record.getType()) {
                case BIRTH:
                    gedcomLink = findBirth(record);
                    break;
                case MARRIAGE:
                    gedcomLink = findMarriage(record, marrDateTag );
                    break;
                case DEATH:
                    gedcomLink = findDeath(record);
                    break;
                default:  // misc
                    gedcomLink = findMisc(record);
            }
            if (gedcomLink != null && gedcomLink.getCompareResult() == GedcomLink.CompareResult.EQUAL) {
                gedcomLinkList.put(record, gedcomLink);
            }
        }
    }
    
    public void removeRecord(Record record) {
        gedcomLinkList.remove(record); 
    }

    public void removeAll() {
        gedcomLinkList.clear(); 
    }
    
    private GedcomLink findBirth(Record birthRecord) {
        GedcomLink gedcomLink = new GedcomLink(birthRecord);
        
        Field recordBirthDate = birthRecord.getField(FieldType.indiBirthDate);
        if ( recordBirthDate == null || recordBirthDate.isEmpty() ) {
            recordBirthDate = birthRecord.getField(FieldType.eventDate);   
        } 
        
        if( recordBirthDate != null ) {

            for (Indi indi : indiList) {

                // date de naissance egale 
                PropertyDate gedcomBirthDate = indi.getBirthDate();

                if ( gedcomBirthDate == null
                     ||! recordBirthDate.equalsProperty(gedcomBirthDate)) {
                    continue;
                }

                // meme sexe de l'enfant
                if ( indi.getSex() == PropertySex.UNKNOWN
                     || birthRecord.getField(FieldType.indiSex) == null 
                     ||! birthRecord.getField(FieldType.indiSex).equalsProperty(indi.getProperty("SEX")) ) {
                    continue;
                }

                // meme nom de l'enfant
                if ( birthRecord.getFieldValue(Record.FieldType.indiLastName).isEmpty()
                     || !MergeQuery.isSameLastName(birthRecord.getFieldValue(Record.FieldType.indiLastName), indi.getLastName())) {
                    continue;
                }

                // meme prenom de l'enfant
                if ( birthRecord.getFieldValue(Record.FieldType.indiFirstName).isEmpty()
                        || !MergeQuery.isSameFirstName(birthRecord.getFieldValue(Record.FieldType.indiFirstName), indi.getFirstName())) {
                    continue;
                }

                gedcomLink.setEntity(indi);
                gedcomLink.setProperty(gedcomBirthDate);
                gedcomLink.setCompareResult(GedcomLink.CompareResult.EQUAL);
                break;

            }
        }

        
        return gedcomLink;
    }
    
    private GedcomLink findDeath(Record deathRecord) {
        GedcomLink gedcomLink = new GedcomLink(deathRecord);
        
        Field recordDeathDate = deathRecord.getField(FieldType.eventDate);
        if (recordDeathDate != null && !recordDeathDate.isEmpty() ) {

            for (Indi indi : indiList) {

                // date de deces egale   
                PropertyDate gedcomDeathDate = indi.getDeathDate();

                if (!recordDeathDate.equalsProperty(gedcomDeathDate) || !gedcomDeathDate.getStart().isValid() ) {
                    continue;
                }

                // meme sexe de l'enfant
                if ( indi.getSex() == PropertySex.UNKNOWN
                        || deathRecord.getField(FieldType.indiSex) == null
                        || !deathRecord.getField(FieldType.indiSex).equalsProperty(indi.getProperty("SEX")) ) {
                    continue;
                }

                // meme nom de l'enfant
                if ( deathRecord.getFieldValue(Record.FieldType.indiLastName).isEmpty()
                        || !MergeQuery.isSameLastName(deathRecord.getFieldValue(Record.FieldType.indiLastName), indi.getLastName())) {
                    continue;
                }

                // meme prenom de l'enfant
                if ( deathRecord.getFieldValue(Record.FieldType.indiFirstName).isEmpty()
                        || !MergeQuery.isSameFirstName(deathRecord.getFieldValue(Record.FieldType.indiFirstName), indi.getFirstName())) {
                    continue;
                }

                gedcomLink.setEntity(indi);
                gedcomLink.setProperty(gedcomDeathDate);
                gedcomLink.setCompareResult(GedcomLink.CompareResult.EQUAL);
                break;

            }
        }

        return gedcomLink;
    }
    
    private GedcomLink findMarriage(Record marriageRecord, TagPath ... tagPathList) {
        GedcomLink gedcomLink = new GedcomLink(marriageRecord);

        Field recordMarriageDate = marriageRecord.getField(FieldType.eventDate);
        if (recordMarriageDate != null && !recordMarriageDate.isEmpty() ) {
            Property gedcomFamMarriageDate = null;

            for (Fam fam : famList) {
                Indi husband = fam.getHusband();
                Indi wife = fam.getWife();

                if (husband == null && wife == null) {
                    continue;
                }

                // date de mariage egale
                boolean eventDateFound = false;
                for (TagPath tagPath : tagPathList) {
                    for (Property famEventDate : fam.getProperties(tagPath)) {
                        if (recordMarriageDate.equalsProperty(famEventDate)) {
                            eventDateFound = true;
                            gedcomFamMarriageDate = famEventDate;
                            break;
                        }
                    }
                    if (eventDateFound) {
                        break;
                    }
                }
                if (!eventDateFound) {
                    continue;
                }

                if (husband != null) {

                    // meme nom de l'epoux
                    if (marriageRecord.getFieldValue(Record.FieldType.indiLastName).isEmpty()
                            || !isSameLastName(marriageRecord.getFieldValue(Record.FieldType.indiLastName), husband.getLastName())) {
                        continue;
                    }

                    //meme prénom de l'epoux
                    if ( marriageRecord.getFieldValue(Record.FieldType.indiFirstName).isEmpty()
                            || !isSameFirstName(marriageRecord.getFieldValue(Record.FieldType.indiFirstName), husband.getFirstName())) {
                        continue;
                    }

                }

                if (wife != null) {
                    // meme nom de l'epouse
                    if ( marriageRecord.getFieldValue(Record.FieldType.wifeLastName).isEmpty()
                            || !isSameLastName(marriageRecord.getFieldValue(Record.FieldType.wifeLastName), wife.getLastName())) {
                        continue;
                    }
                    //meme prénom de l'epouse
                    if ( marriageRecord.getFieldValue(Record.FieldType.wifeFirstName).isEmpty()
                            || !isSameFirstName(marriageRecord.getFieldValue(Record.FieldType.wifeFirstName), wife.getFirstName())) {
                        continue;
                    }

                }

                gedcomLink.setEntity(fam);
                gedcomLink.setProperty(gedcomFamMarriageDate);
                gedcomLink.setCompareResult(GedcomLink.CompareResult.EQUAL);
                break;

            }
        }

        return gedcomLink;
    }
    
    
    private GedcomLink findMisc(Record miscRecord) {
        GedcomLink gedcomLink = null;

        
        // j'utilise la date d'insinuation si elle est renseignée
        Field recordEventDate = miscRecord.getField(FieldType.secondDate);
        if( recordEventDate == null || recordEventDate.isEmpty()) {
            // si la date d'insinuation n'est pas renseignée, j'utilise la date de l'évènement
            recordEventDate = miscRecord.getField(FieldType.eventDate);
        }
        if (recordEventDate != null && !recordEventDate.isEmpty()) {
            String eventType = miscRecord.getFieldValue(Record.FieldType.eventType).toLowerCase();
            if (eventType.contains("cm") || eventType.contains("mariage")) {
                // je cherche l'évènement sur la famille
                gedcomLink = findMarriage(miscRecord, marbDateTag, marcDateTag, marlDateTag, evenDateTag);

            }
            if (gedcomLink == null) {
                Property gedcomMiscEventDate = null;
                // je cherche l'évènement sur l'individu
                TagPath[] tagPathList = {indiEvenDateTag, willDateTag};
                for (Indi indi : indiList) {
                    boolean eventDateFound = false;
                    for (TagPath tagPath : tagPathList) {
                        for (Property indiEventDate : indi.getProperties(tagPath)) {
                            if (!recordEventDate.equalsProperty(indiEventDate)) {
                                eventDateFound = true;
                                gedcomMiscEventDate = indiEventDate;
                                break;
                            }
                        }
                        if (eventDateFound) {
                            break;
                        }
                    }
                    if (!eventDateFound) {
                        continue;
                    }
                    
                    // meme sexe de l'individu
                    if ( indi.getSex() == PropertySex.UNKNOWN
                        || miscRecord.getField(FieldType.indiSex) != null
                        || !miscRecord.getField(FieldType.indiSex).equalsProperty(indi.getProperty("SEX")) ) {
                        continue;
                    }
                    
                    // meme nom de l'individu
                    if (miscRecord.getFieldValue(Record.FieldType.indiLastName).isEmpty()
                        || !MergeQuery.isSameLastName(miscRecord.getFieldValue(FieldType.indiLastName), indi.getLastName())) {
                        continue;
                    }
                    
                    // meme prenom de l'individu
                    if ( miscRecord.getFieldValue(Record.FieldType.indiFirstName).isEmpty()
                         || !MergeQuery.isSameFirstName(miscRecord.getFieldValue(FieldType.indiFirstName), indi.getFirstName())) {
                        continue;
                    }
                    
                    gedcomLink = new GedcomLink(miscRecord);
                    gedcomLink.setEntity(indi);
                    gedcomLink.setProperty(gedcomMiscEventDate);
                    gedcomLink.setCompareResult(GedcomLink.CompareResult.EQUAL);
                    break;
                    
                }
            }
        }


        return gedcomLink;
    }

    
    
}


