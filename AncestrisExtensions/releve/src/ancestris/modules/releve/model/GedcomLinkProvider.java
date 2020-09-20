 package ancestris.modules.releve.model;

import ancestris.modules.releve.RelevePanel;
import ancestris.modules.releve.merge.MergeQuery;
import static ancestris.modules.releve.merge.MergeQuery.isSameFirstName;
import static ancestris.modules.releve.merge.MergeQuery.isSameLastName;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.utils.CompareString;
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
import javax.swing.JOptionPane;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * recherche les relevés correspondant à des évènements dans un fichier gedcom 
 * Les relevés qui ont évènement correspondant sont signalés dans la table des relevés 
 * ( identifiant en rouge)
 * Correspondances recherchées : 
 * relevés de naissance <=> Tag BIRT
 * relevés de décés  <=> Tag DEAT
 * relevés de mariage <=> tag MARR
 * relevés de ban de mariage <=> tag MARB , MARC
 * relevés de testament <=>  tag WILL
 * relevés divers  <=> tag EVEN
 * 
 * Les correspondances sont établies en fonction du type dévènement, de la date 
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
    
    public void init(final RecordModel recordModel, Gedcom gedcom, boolean showGedcomLink, boolean quiet) {
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


        if (gedcom != null && showGedcomLink) {            
            
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
            if (recordModel.getRowCount()>0) {
                recordModel.fireRecordModelUpdated(0, recordModel.getRowCount() - 1);
            }
            
            if (!quiet && gedcomLinkList.isEmpty()) {
                JOptionPane.showConfirmDialog(null, NbBundle.getMessage(RelevePanel.class, "GedcomLinkProvider.HighlightNone"), NbBundle.getMessage(RelevePanel.class, "GedcomLinkProvider.title"), JOptionPane.PLAIN_MESSAGE);  
                
                
            }
            
            
            // je trace le resultat
//            for (GedcomLink link :gedcomLinkList.values() ) {
//                System.out.println(link.getRecord().getType() + "; "
//                        + link.getProperty().getPath().toString()+ "; "
//                        + link.getRecord().getFieldValue(FieldType.indiLastName) + " "
//                        + link.getRecord().getFieldValue(FieldType.indiFirstName) + "; "
//                        + link.getEntity().toString());
//            }

        } else {
            if (!quiet && showGedcomLink) {            
                JOptionPane.showConfirmDialog(null, NbBundle.getMessage(RelevePanel.class, "GedcomLinkProvider.HighlightNeedGedcom"), NbBundle.getMessage(RelevePanel.class, "GedcomLinkProvider.title"), JOptionPane.PLAIN_MESSAGE); 
            }
            gedcomLinkList.clear();
            if (recordModel.getRowCount()>0) {
                recordModel.fireRecordModelUpdated(0, recordModel.getRowCount() - 1);
            }
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
                    createFamList(famMarriage, famMisc);
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
                    createFamList(famMarriage, famMisc);
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
                        // si la date de naissance n'est pas renseignée, j'utilise la date de l'évènement
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
                result = CompareString.compareStringUTF8(this.record.getFieldValue(FieldType.indiLastName),that.record.getFieldValue(FieldType.indiLastName));
                if (result == 0) {
                    result = CompareString.compareStringUTF8(this.record.getFieldValue(FieldType.indiFirstName), that.record.getFieldValue(FieldType.indiFirstName));
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
                    int result = CompareString.compareStringUTF8(record.getFieldValue(Record.FieldType.indiLastName), indi.getLastName());
                    if (result == 0) {
                        result = CompareString.compareStringUTF8(record.getFieldValue(Record.FieldType.indiFirstName), indi.getFirstName());
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
                    int result = CompareString.compareStringUTF8(record.getFieldValue(Record.FieldType.indiLastName), husband.getLastName());
                    if (result == 0) {
                        result = CompareString.compareStringUTF8(record.getFieldValue(Record.FieldType.indiFirstName), husband.getFirstName());
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
                result = CompareString.compareStringUTF8(this.entity.getLastName(), that.entity.getLastName());
                if( result == 0) {
                    result = CompareString.compareStringUTF8(this.entity.getFirstName(), that.entity.getFirstName());
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
                    result = CompareString.compareStringUTF8(this.entity.getHusband().getLastName(), that.entity.getHusband().getLastName());
                    if( result == 0) {
                        result = CompareString.compareStringUTF8(this.entity.getHusband().getFirstName(), that.entity.getHusband().getFirstName());
                    }
                }
            }
            return result ;
        }
    }
}
    