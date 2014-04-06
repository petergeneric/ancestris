 package ancestris.modules.releve.model;

import ancestris.modules.releve.dnd.MergeQuery;
import static ancestris.modules.releve.dnd.MergeQuery.isSameFirstName;
import static ancestris.modules.releve.dnd.MergeQuery.isSameLastName;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
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
    
    
    public void init(final RecordModel recordModel, Gedcom gedcom, boolean state) {
        this.gedcom = gedcom;
        this.showGedcomLink = state;

        if (gedcom != null && showGedcomLink == true ) {

//           (new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        
//                        for (int i = 0; i < recordModel.getRowCount(); i++) {
//                            Record record = recordModel.getRecord(i);
//                            addRecord(record);
//                            
//                        }
//                        recordModel.fireRecordModelUpdated(0, recordModel.getRowCount() -1);
//                    }
//
//                })).start();
            for (int i = 0; i < recordModel.getRowCount(); i++) {
                Record record = recordModel.getRecord(i);
                addRecord(record);
            }
            recordModel.fireRecordModelUpdated(0, recordModel.getRowCount() - 1);

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
                case birth:
                    gedcomLink = findBirth(record);
                    break;
                case marriage:
                    gedcomLink = findMariage(record);
                    break;
                case death:
                    gedcomLink = findDeath(record);
                    break;
                default:  // misc
                    gedcomLink = null;
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
        
        PropertyDate recordBirthDate = birthRecord.getIndiBirthDate().getPropertyDate();
        if ( !recordBirthDate.isComparable() ) {
            recordBirthDate = birthRecord.getEventDateProperty();   
        } 
        
        Collection<Indi> indiList = gedcom.getIndis();
        
        for( Indi indi : indiList) {

            // date de naissance egale 
            PropertyDate gedcomBirthDate = indi.getBirthDate();
            if (recordBirthDate != null && gedcomBirthDate != null && recordBirthDate.getStart().isValid() && gedcomBirthDate.getStart().isValid()) {
                boolean equal;
                try {
                    int recordJulianDay = recordBirthDate.getStart().getJulianDay();
                    int childJulianDay = gedcomBirthDate.getStart().getJulianDay();
                    equal = recordJulianDay == childJulianDay;
                } catch (GedcomException ex) {
                    equal = false;
                }

                if (!equal) {
                    //if (!MergeQuery.isCompatible(recordBirthDate, child.getBirthDate(), 1)) {
                    // la date de naissance de l'individu n'est pas compatible avec la date du relevé
                    continue;
                }
            } else {
                continue;
            }

            // meme sexe de l'enfant
            if (birthRecord.getIndiSex().getSex() != FieldSex.UNKNOWN
                    && indi.getSex() != PropertySex.UNKNOWN
                    && birthRecord.getIndiSex().getSex() != indi.getSex()) {
                continue;
            }

            // meme nom de l'enfant
            if (!birthRecord.getIndi().getLastName().isEmpty()
                    && !MergeQuery.isSameLastName(birthRecord.getIndiLastName().getValue(), indi.getLastName())) {
                continue;
            }

            // meme prenom de l'enfant
            if (!birthRecord.getIndi().getFirstName().isEmpty()
                    && !MergeQuery.isSameFirstName(birthRecord.getIndiFirstName().getValue(), indi.getFirstName())) {
                continue;
            }

            gedcomLink.setEntity(indi);
            gedcomLink.setProperty(recordBirthDate);
            gedcomLink.setCompareResult(GedcomLink.CompareResult.EQUAL);
            break;

        }

        
        return gedcomLink;
    }
    
    private GedcomLink findDeath(Record deathRecord) {
        GedcomLink gedcomLink = new GedcomLink(deathRecord);
        
        PropertyDate recordDeathDate = deathRecord.getEventDateProperty();
        
        Collection<Indi> indiList = gedcom.getIndis();
        
        for( Indi indi : indiList) {

            // date de deces egale   
            PropertyDate gedcomDate = indi.getDeathDate();
            if (recordDeathDate != null && gedcomDate != null && recordDeathDate.getStart().isValid() && gedcomDate.getStart().isValid() ) {
                boolean equal;
                try {
                    int recordJulianDay = recordDeathDate.getStart().getJulianDay();
                    int childJulianDay = gedcomDate.getStart().getJulianDay();
                    equal = recordJulianDay == childJulianDay;
                } catch (GedcomException ex) {
                    equal = false;
                }

                if (!equal) {
                    //if (!MergeQuery.isCompatible(recordBirthDate, child.getBirthDate(), 1)) {
                    // la date de naissance de l'individu n'est pas compatible avec la date du relevé
                    continue;
                }
            } else {
                continue;
            }

            // meme sexe de l'enfant
            if (deathRecord.getIndiSex().getSex() != FieldSex.UNKNOWN
                    && indi.getSex() != PropertySex.UNKNOWN
                    && deathRecord.getIndiSex().getSex() != indi.getSex()) {
                continue;
            }

            // meme nom de l'enfant
            if (!deathRecord.getIndi().getLastName().isEmpty()
                    && !MergeQuery.isSameLastName(deathRecord.getIndiLastName().getValue(), indi.getLastName())) {
                continue;
            }

            // meme prenom de l'enfant
            if (!deathRecord.getIndi().getFirstName().isEmpty()
                    && !MergeQuery.isSameFirstName(deathRecord.getIndiFirstName().getValue(), indi.getFirstName())) {
                continue;
            }

            gedcomLink.setEntity(indi);
            gedcomLink.setProperty(recordDeathDate);            
            gedcomLink.setCompareResult(GedcomLink.CompareResult.EQUAL);
            break;

        }

        return gedcomLink;
    }
    
    private GedcomLink findMariage(Record marriageRecord) {
        GedcomLink gedcomLink = new GedcomLink(marriageRecord);

        PropertyDate recordMarriageDate = marriageRecord.getEventDateProperty();

        Collection<Fam> famList = gedcom.getFamilies();

        for (Fam fam : famList) {
            Indi husband = fam.getHusband();
            Indi wife = fam.getWife();

            if (husband == null && wife == null) {
                continue;
            }

            // date de mariage egale      
            PropertyDate famMarriageDate = fam.getMarriageDate();
            if (recordMarriageDate != null && famMarriageDate != null && famMarriageDate.getStart().isValid()) {
                boolean equal;
                try {
                    int recordJulianDay = recordMarriageDate.getStart().getJulianDay();
                    int gedcomJulianDay = fam.getMarriageDate().getStart().getJulianDay();
                    equal = recordJulianDay == gedcomJulianDay;
                } catch (GedcomException ex) {
                    equal = false;
                }

                if (!equal) {
                    continue;
                }
            } else {
                continue;
            }

            if (husband != null) {

                // meme nom de l'epoux
                if (!marriageRecord.getIndiLastName().isEmpty()
                        && !isSameLastName(marriageRecord.getIndiLastName().getValue(), husband.getLastName())) {
                    continue;
                }

                //meme prénom de l'epoux
                if (!marriageRecord.getIndi().getFirstName().isEmpty()
                        && !isSameFirstName(marriageRecord.getIndiFirstName().getValue(), husband.getFirstName())) {
                    continue;
                }

            }

            if (wife != null) {
                // meme nom de l'epouse
                if (!marriageRecord.getWife().getLastName().isEmpty()
                        && !isSameLastName(marriageRecord.getWifeLastName().getValue(), wife.getLastName())) {
                    continue;
                }
                //meme prénom de l'epouse
                if (!marriageRecord.getWife().getFirstName().isEmpty()
                        && !isSameFirstName(marriageRecord.getWifeFirstName().getValue(), wife.getFirstName())) {
                    continue;
                }

            }

            gedcomLink.setEntity(fam);
            gedcomLink.setProperty(recordMarriageDate);            
            gedcomLink.setCompareResult(GedcomLink.CompareResult.EQUAL);
            break;

        }

        
        return gedcomLink;
    }
    
    
    
    
}


