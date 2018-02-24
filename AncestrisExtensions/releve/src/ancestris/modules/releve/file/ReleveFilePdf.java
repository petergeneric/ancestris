package ancestris.modules.releve.file;

import ancestris.modules.releve.model.PlaceManager;
import ancestris.modules.releve.model.RecordModel;
import ancestris.modules.releve.model.RecordMisc;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordMarriage;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.file.FileManager.Line;
import ancestris.modules.releve.model.FieldAge;
import ancestris.modules.releve.model.Record.RecordType;
import genj.fo.Document;
import genj.fo.Format;
import genj.fo.PDFFormat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;

/**
 *
 * @author Michel
 */
public class ReleveFilePdf {

    final static private String fileSignature = "ANCESTRISV3";
    final static private char fieldSeparator = ';';
    final static private int nbFields = 90;
    static private OutputStreamWriter writer = null;
    static private Document doc ;

    /**
     * verifie si la premere ligne est conforme au format 
     * @param inputFile
     * @param sb  message d'erreur
     * @return
     */
    public static boolean isValidFile(File inputFile, StringBuilder sb) {
        return false;
    }

    
    // Format d'un releve
    enum Field {
        ancetris,
        nomCommune, codeCommune, nomDepartement, stateName, countryName, parish,
        eventType, eventTypeName, eventTypeComment,
        eventDate, cote, freeComment, notaryComment,
        indiLastName, indiFirstName, indiSex, indiBirthPlace, indiBirthDate, indiAge, indiOccupation, indiResidence,indiComment,
        indiMarriedLastName, indiMarriedFirstName, indiMarriedDead, indiMarriedOccupation, indiMarriedResidence,indiMarriedComment,
        indiFatherLastName, indiFatherFirstName, indiFatherAge, indiFatherDead, indiFatherOccupation, indiFatherResidence,indiFatherComment,
        indiMotherLastName, indiMotherFirstName, indiMotherAge, indiMotherDead, indiMotherOccupation, indiMotherResidence, indiMotherComment,
        wifeLastName, wifeFirstName, wifeSex, wifeBirthPlace, wifeBirthDate, wifeAge, wifeOccupation, wifeResidence,wifeComment,
        wifeMarriedLastName, wifeMarriedFirstName, wifeMarriedDead, wifeMarriedOccupation, wifeMarriedResidence, wifeMarriedComment,
        wifeFatherLastName, wifeFatherFirstName, wifeFatherAge, wifeFatherDead, wifeFatherOccupation, wifeFatherResidence, wifeFatherComment,
        wifeMotherLastName, wifeMotherFirstName, wifeMotherAge, wifeMotherDead, wifeMotherOccupation, wifeMotherResidence, wifeMotherComment,
        witness1LastName, witness1FirstName, witness1Occupation, witness1Comment,
        witness2LastName, witness2FirstName, witness2Occupation, witness2Comment,
        witness3LastName, witness3FirstName, witness3Occupation, witness3Comment,
        witness4LastName, witness4FirstName, witness4Occupation, witness4Comment,
        generalComment,
        recordNo
    }

    /**
     *
     * @param fileName
     */
    public static FileBuffer loadFile(File inputFile ) { 
        return null;
    }

    public static FileBuffer loadFile( InputStream inputStream ) {
        return null;
    }

    /**
     * Comparateur de transposition 
     */
    public static class RecordComparator implements Comparator<Record> {
        @Override
        public int compare(Record record1, Record record2) {
            if( record1.getField(FieldType.eventDate) == null) {
                if( record2.getField(FieldType.eventDate) == null) {
                    return 0;
                } else {
                    return 1;
                }
            } else {
                if( record2.getField(FieldType.eventDate) == null) {
                    return -1;
                } else {
                    return record1.getField(FieldType.eventDate).compareTo(record2.getField(FieldType.eventDate));
                }
            }
            
        }
    }

   /**
     * Sauvegarde les données dans un fichier
     * @param placeManager  founisseur du lieu
     * @param recordModel   modele de donnees a sauvegarder
     * @param fileName      nom du fichier de sauvegarde
     * @param append        true : ajouter aux données existantes dans le fichier,
     *                      false : remplacer les données dans le fichier.
     * @return StringBuilder est vide s'il n'y a pas d'erreur, sinon il contient les messages d'erreur.
     */
    public static StringBuilder saveFile(PlaceManager placeManager, RecordModel recordModel, RecordType recordType, File fileName, boolean append) {

        StringBuilder sb = new StringBuilder();
//        try {
//            //create BufferedReader to read csv file
//            //create BufferedReader to read csv file
//
//            ArrayList<Record> recordTree = new ArrayList<Record>();
//            for(int i=0; i < recordModel.getRowCount(); i++) {
//                recordTree.add(recordModel.getRecord(i));
//            }
//            //Collections.sort(recordTree, new RecordComparator());
//            
//            doc = new Document("");
//
//            // j'ajoute les autres lignes
//            for (int index = 0; index < recordTree.size(); index++) {
//                Record record = recordTree.get(index);
//                if( recordType != null && recordType != record.getType()) {
//                    continue;
//                }
//                Line line = new Line(fieldSeparator);
//                try {
//                    if ( record instanceof RecordBirth ) {
//                        line.appendCsvFn(fileSignature);
//                        line.appendCsvFn(placeManager.getCityName());
//                        line.appendCsvFn(placeManager.getCityCode());
//                        line.appendCsvFn(placeManager.getCountyName());
//                        line.appendCsvFn(placeManager.getStateName());
//                        line.appendCsvFn(placeManager.getCountryName());
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.parish));
//                        line.appendCsvFn("N");
//                        line.appendCsvFn(""); //eventTypeTag
//                        line.appendCsvFn(""); //eventTypeName
//                        line.appendCsvFn(record.getFieldValue(FieldType.eventDate));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.cote));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.freeComment));
//                        line.appendCsvFn(""); // notary
//
//                        writeTitle(1,
//                                record.getFieldValue(FieldType.eventDate),
//                                "Naissance",
//                                record.getFieldValue(Record.FieldType.cote),
//                                record.getFieldValue(Record.FieldType.freeComment)
//                                );
//
//
//                        line.appendCsvFn(record.getIndi().getLastName().getValue());
//                        line.appendCsvFn(record.getIndi().getFirstName().getValue());
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiSex));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiBirthPlace)); // place
//                        line.appendCsvFn(record.getFieldValue(FieldType.indiBirthDate)); // IndiBirthDate
//                        line.appendCsvFn(""); // age
//                        line.appendCsvFn(""); // occupation
//                        line.appendCsvFn(""); // residence
//                        line.appendCsvFn(record.getFieldValue(FieldType.indiComment));
//
//                        line.appendCsvFn(""); // IndiMarriedLastName
//                        line.appendCsvFn(""); // IndiMarriedFirstName
//                        line.appendCsvFn(""); // IndiMarriedDead
//                        line.appendCsvFn(""); // IndiMarriedOccupation
//                        line.appendCsvFn(""); // residence
//                        line.appendCsvFn(""); // IndiMarriedComment
//
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherLastName));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherFirstName));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherAge));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherDead));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherOccupation));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherResidence));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherComment));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherLastName));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherFirstName));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherAge));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherDead));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherOccupation));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherResidence));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherComment));
//
//                        line.appendCsvFn(""); // WifeLastName
//                        line.appendCsvFn(""); // WifeFirstName
//                        line.appendCsvFn(""); // WifeSex
//                        line.appendCsvFn(""); // WifePlace
//                        line.appendCsvFn(""); // WifeBirthDate
//                        line.appendCsvFn(""); // WifeAge
//                        line.appendCsvFn(""); // WifeOccupation
//                        line.appendCsvFn(""); // WifeResidence
//                        line.appendCsvFn(""); // WifeComment
//
//                        line.appendCsvFn(""); // WifeMarriedLastName
//                        line.appendCsvFn(""); // WifeMarriedFirstName
//                        line.appendCsvFn(""); // WifeMarriedDead
//                        line.appendCsvFn(""); // WifeMarriedOccupation
//                        line.appendCsvFn(""); // WifeMarriedResidence
//                        line.appendCsvFn(""); // WifeMarriedComment
//
//                        line.appendCsvFn(""); // WifeFatherLastName
//                        line.appendCsvFn(""); // WifeFatherFirstName
//                        line.appendCsvFn(""); // WifeFatherAge
//                        line.appendCsvFn(""); // WifeFatherDead
//                        line.appendCsvFn(""); // WifeFatherOccupation
//                        line.appendCsvFn(""); // WifeFatherResidence
//                        line.appendCsvFn(""); // WifeFatherComment
//                        line.appendCsvFn(""); // WifeMotherLastName
//                        line.appendCsvFn(""); // WifeMotherFirstName
//                        line.appendCsvFn(""); // WifeMotherAge
//                        line.appendCsvFn(""); // WifeMotherDead
//                        line.appendCsvFn(""); // WifeMotherOccupation
//                        line.appendCsvFn(""); // WifeMotherResidence
//                        line.appendCsvFn(""); // WifeMotherComment
//
//                        for(Record.Witness witness : record.getWitnesses()) {
//                            line.appendCsvFn(witness.getLastName().toString());
//                            line.appendCsvFn(witness.getFirstName().toString());
//                            line.appendCsvFn(witness.getOccupation().toString());
//                            line.appendCsvFn(witness.getFieldValue(Record.FieldType.indiComment));
//                        }
//
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.generalComment));
//                        line.appendCsv(String.valueOf(index)); // numero d'enregistrement
//
//                    } if ( record instanceof RecordMarriage ) {
//
//                        line.appendCsvFn(fileSignature);
//                        line.appendCsvFn(placeManager.getCityName());
//                        line.appendCsvFn(placeManager.getCityCode());
//                        line.appendCsvFn(placeManager.getCountyName());
//                        line.appendCsvFn(placeManager.getStateName());
//                        line.appendCsvFn(placeManager.getCountryName());
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.parish));
//                        line.appendCsvFn("M");
//                        line.appendCsvFn(""); //eventTypeTag
//                        line.appendCsvFn(""); //eventTypeName
//                        line.appendCsvFn(record.getFieldValue(FieldType.eventDate));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.cote));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.freeComment));
//                        line.appendCsvFn(""); // notary
//
//                        writeTitle(1,
//                                record.getFieldValue(FieldType.eventDate),
//                                "Mariage",
//                                record.getFieldValue(Record.FieldType.cote),
//                                record.getFieldValue(Record.FieldType.freeComment)
//                                );
//
//
//                        line.appendCsvFn(record.getIndi().getLastName().getValue());
//                        line.appendCsvFn(record.getIndi().getFirstName().getValue());
//                        line.appendCsvFn(""); // IndiSex
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiBirthPlace));
//                        line.appendCsvFn(record.getFieldValue(FieldType.indiBirthDate));
//                        line.appendCsvFn(record.getFieldValue(FieldType.indiAge));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiOccupation));
//                        line.appendCsvFn(record.getIndi().getResidence() .toString());
//                        line.appendCsvFn(record.getFieldValue(FieldType.indiComment));
//                        
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMarriedLastName));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMarriedFirstName));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMarriedDead));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMarriedOccupation));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMarriedResidence));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMarriedComment));
//
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherLastName));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherFirstName));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherAge));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherDead));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherOccupation));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherResidence));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherComment));
//                        
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherLastName));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherFirstName));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherAge));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherDead));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherOccupation));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherResidence));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherComment));
//                        
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeLastName));
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeFirstName));
//                        line.appendCsvFn(""); //WifeSex
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeBirthPlace));
//                        line.appendCsvFn(record.getWife().getBirthDate().toString());
//                        line.appendCsvFn(record.getWife().getAge().getValue());
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeOccupation));
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeResidence));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.wifeComment));
//                        
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeMarriedLastName));
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeMarriedFirstName));
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeMarriedDead));
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeMarriedOccupation));
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeMarriedResidence));
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeMarriedComment));
//
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.wifeFatherLastName));
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeFatherFirstName));
//                        line.appendCsvFn(record.getWife().getFatherAge().getValue());
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeFatherDead));
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeFatherOccupation));
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeFatherResidence));
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeFatherComment));
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeMotherLastName));
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeMotherFirstName));
//                        line.appendCsvFn(record.getWife().getMotherAge().getValue());
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeMotherDead));
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeMotherOccupation));
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeMotherResidence));
//                        line.appendCsvFn(record.getFieldValue(FieldType.wifeMotherComment));
//
//                        for(Record.Witness witness : record.getWitnesses()) {
//                            line.appendCsvFn(witness.getLastName().toString());
//                            line.appendCsvFn(witness.getFirstName().toString());
//                            line.appendCsvFn(witness.getOccupation().toString());
//                            line.appendCsvFn(witness.getFieldValue(Record.FieldType.indiComment));
//                        }
//
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.generalComment));
//                        line.appendCsv(String.valueOf(index)); // numero d'enregistrement
//
//                    } else if ( record instanceof RecordDeath ) {
//
//                        line.appendCsvFn(fileSignature);
//                        line.appendCsvFn(placeManager.getCityName());
//                        line.appendCsvFn(placeManager.getCityCode());
//                        line.appendCsvFn(placeManager.getCountyName());
//                        line.appendCsvFn(placeManager.getStateName());
//                        line.appendCsvFn(placeManager.getCountryName());
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.parish));
//                        line.appendCsvFn("D");
//                        line.appendCsvFn(""); //eventTypeTag
//                        line.appendCsvFn(""); //eventTypeName
//                        line.appendCsvFn(record.getFieldValue(FieldType.eventDate));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.cote));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.freeComment));
//                        line.appendCsvFn(""); // notary
//
//                        writeTitle(1,
//                                record.getFieldValue(FieldType.eventDate),
//                                "Décés",
//                                record.getFieldValue(Record.FieldType.cote),
//                                record.getFieldValue(Record.FieldType.freeComment)
//                                );
//
//
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiLastName));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFirstName));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiSex));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiBirthPlace));
//                        line.appendCsvFn(record.getFieldValue(FieldType.indiBirthDate));
//                        line.appendCsvFn(record.getFieldValue(FieldType.indiAge));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiOccupation));
//                        line.appendCsvFn(record.getIndi().getResidence() .toString());
//                        line.appendCsvFn(record.getFieldValue(FieldType.indiComment));
//
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMarriedLastName));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMarriedFirstName));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMarriedDead));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMarriedOccupation));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMarriedResidence));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMarriedComment));
//
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherLastName));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherFirstName));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherAge));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherDead));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherOccupation));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherResidence));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiFatherComment));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherLastName));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherFirstName));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherAge));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherDead));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherOccupation));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherResidence));
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.indiMotherComment));
//
//                        line.appendCsvFn(""); // WifeLastName
//                        line.appendCsvFn(""); // WifeFirstName
//                        line.appendCsvFn(""); // WifeSex
//                        line.appendCsvFn(""); // WifePlace
//                        line.appendCsvFn(""); // WifeBirthDate
//                        line.appendCsvFn(""); // WifeAge
//                        line.appendCsvFn(""); // WifeOccupation
//                        line.appendCsvFn(""); // WifeResidence
//                        line.appendCsvFn(""); // WifeComment
//
//                        line.appendCsvFn(""); // WifeMarriedLastName
//                        line.appendCsvFn(""); // WifeMarriedFirstName
//                        line.appendCsvFn(""); // WifeMarriedDead
//                        line.appendCsvFn(""); // WifeMarriedOccupation
//                        line.appendCsvFn(""); // WifeMarriedResidence
//                        line.appendCsvFn(""); // WifeMarriedComment
//
//                        line.appendCsvFn(""); // WifeFatherLastName
//                        line.appendCsvFn(""); // WifeFatherFirstName
//                        line.appendCsvFn(""); // WifeFatherAge
//                        line.appendCsvFn(""); // WifeFatherDead
//                        line.appendCsvFn(""); // WifeFatherOccupation
//                        line.appendCsvFn(""); // WifeFatherResidence
//                        line.appendCsvFn(""); // WifeFatherComment
//                        line.appendCsvFn(""); // WifeMotherLastName
//                        line.appendCsvFn(""); // WifeMotherFirstName
//                        line.appendCsvFn(""); // WifeMotherAge
//                        line.appendCsvFn(""); // WifeMotherDead
//                        line.appendCsvFn(""); // WifeMotherOccupation
//                        line.appendCsvFn(""); // WifeMotherResidence
//                        line.appendCsvFn(""); // WifeMotherComment
//
//                        for(Record.Witness witness : record.getWitnesses()) {
//                            line.appendCsvFn(witness.getLastName().toString());
//                            line.appendCsvFn(witness.getFirstName().toString());
//                            line.appendCsvFn(witness.getOccupation().toString());
//                            line.appendCsvFn(witness.getFieldValue(Record.FieldType.indiComment));
//                        }
//
//                        line.appendCsvFn(record.getFieldValue(Record.FieldType.generalComment));
//                        line.appendCsv(String.valueOf(index)); // numero d'enregistrement
//
//                    } else if ( record instanceof RecordMisc ) {
//
////                        line.appendCsvFn(fileSignature);
////                        line.appendCsvFn(placeManager.getCityName());
////                        line.appendCsvFn(placeManager.getCityCode());
////                        line.appendCsvFn(placeManager.getCountyName());
////                        line.appendCsvFn(placeManager.getStateName());
////                        line.appendCsvFn(placeManager.getCountryName());
////                        line.appendCsvFn(record.getFieldValue(Record.FieldType.parish));
//                        String participantNames = record.getFieldValue(Record.FieldType.indiLastName);
//                        if( !record.getWife().getLastName().isEmpty()) {
//                            participantNames += " et " + record.getFieldValue(FieldType.wifeLastName);
//                        }
//                        writeTitle(1,
//                                record.getFieldValue(FieldType.eventDate),
//                                record.getFieldValue(FieldType.eventType),
//                                participantNames,
//                                record.getEventSecondDateString(),
//                                record.getFieldValue(Record.FieldType.cote),
//                                record.getFieldValue(Record.FieldType.freeComment)
//                                );
//
//                        writeValue(1,"Notaire:",
//                            record.getFieldValue(Record.FieldType.notary)
//                            );
//
//                        writeValue(1,"Intervenant 1:",
//                                record.getFieldValue(Record.FieldType.indiFirstName),
//                                record.getFieldValue(Record.FieldType.indiLastName)
//                                );
//                        writeValue(2,"Naissance:",
//                                record.getFieldValue(FieldType.indiBirthDate),
//                                record.getFieldValue(Record.FieldType.indiBirthPlace)
//                                );
//                        writeValue(2,"Age:",
//                                formatAgeToAge(record.getIndi().getAge())
//                                );
//                        writeValue(2,"Profession:",
//                                record.getFieldValue(Record.FieldType.indiOccupation)
//                                );
//                        writeValue(2,"Domicile:",
//                                record.getIndi().getResidence() .toString()
//                                );
//                        writeValue(2,"Info:",
//                                record.getFieldValue(FieldType.indiComment)
//                                );
//
//                        writeValue(2,"Conjoint:",
//                                record.getFieldValue(Record.FieldType.indiMarriedFirstName),
//                                record.getFieldValue(Record.FieldType.indiMarriedLastName),
//                                record.getFieldValue(Record.FieldType.indiMarriedDead)
//                                );
//                        writeValue(3,"Profession:",
//                                record.getFieldValue(Record.FieldType.indiMarriedOccupation)
//                                );
//                        writeValue(3,"Domicile:",
//                                record.getFieldValue(Record.FieldType.indiMarriedResidence)
//                                );
//                        writeValue(3,"Info:",
//                                record.getFieldValue(Record.FieldType.indiMarriedComment)
//                                );
//
//                        writeValue(2,"Père:",
//                            record.getFieldValue(Record.FieldType.indiFatherFirstName),
//                            record.getFieldValue(Record.FieldType.indiFatherLastName),
//                            record.getFieldValue(Record.FieldType.indiFatherDead)
//                            );
//                        writeValue(3,"Age:",
//                                formatAgeToAge(record.getIndi().getFatherAge())
//                                );
//                        writeValue(3,"Profession:",
//                                record.getFieldValue(Record.FieldType.indiFatherOccupation)
//                                );
//                        writeValue(3,"Domicile:",
//                                record.getFieldValue(Record.FieldType.indiFatherResidence)
//                                );
//                        writeValue(3,"Info:",
//                                record.getFieldValue(Record.FieldType.indiFatherComment)
//                                );
//
//                        writeValue(2,"Mère:",
//                            record.getFieldValue(Record.FieldType.indiMotherFirstName),
//                            record.getFieldValue(Record.FieldType.indiMotherLastName),
//                            record.getFieldValue(Record.FieldType.indiMotherDead)
//                            );
//                        writeValue(3,"Age:",
//                                formatAgeToAge(record.getIndi().getMotherAge())
//                                );
//                        writeValue(3,"Profession:",
//                                record.getFieldValue(Record.FieldType.indiMotherOccupation)
//                                );
//                        writeValue(3,"Domicile:",
//                                record.getFieldValue(Record.FieldType.indiMotherResidence)
//                                );
//                        writeValue(3,"Info:",
//                                record.getFieldValue(Record.FieldType.indiMotherComment)
//                                );
//                        
//                        ///////////////////////////////////////////////////////
//                        writeValue(1,"Intervenant 2:",
//                                record.getFieldValue(FieldType.wifeFirstName),
//                                record.getFieldValue(FieldType.wifeLastName)
//                                );
//                        writeValue(2,"Naissance:",
//                                record.getWife().getBirthDate().toString(),
//                                record.getFieldValue(FieldType.wifeBirthPlace)
//                                );
//                        writeValue(2,"Age:",
//                                formatAgeToAge(record.getWife().getAge())
//                                );
//                        writeValue(2,"Profession:",
//                                record.getFieldValue(FieldType.wifeOccupation)
//                                );
//                        writeValue(2,"Domicile:",
//                                record.getFieldValue(FieldType.wifeResidence)
//                                );
//                        writeValue(2,"Info:",
//                                record.getFieldValue(Record.FieldType.wifeComment)
//                                );
//
//                        writeValue(2,"Conjoint:",
//                                record.getFieldValue(FieldType.wifeMarriedFirstName),
//                                record.getFieldValue(FieldType.wifeMarriedLastName),
//                                record.getFieldValue(FieldType.wifeMarriedDead)
//                                );
//                        writeValue(3,"Profession:",
//                                record.getFieldValue(FieldType.wifeMarriedOccupation)
//                                );
//                        writeValue(3,"Domicile:",
//                                record.getFieldValue(FieldType.wifeMarriedResidence)
//                                );
//                        writeValue(3,"Info:",
//                                record.getFieldValue(FieldType.wifeMarriedComment)
//                                );
//
//                        writeValue(2,"Père:",
//                            record.getFieldValue(FieldType.wifeFatherFirstName),
//                            record.getFieldValue(Record.FieldType.wifeFatherLastName),
//                            record.getFieldValue(FieldType.wifeFatherDead)
//                            );
//                        writeValue(3,"Age:",
//                                formatAgeToAge(record.getWife().getFatherAge())
//                                );
//                        writeValue(3,"Profession:",
//                                record.getFieldValue(FieldType.wifeFatherOccupation)
//                                );
//                        writeValue(3,"Domicile:",
//                                record.getFieldValue(FieldType.wifeFatherResidence)
//                                );
//                        writeValue(3,"Info:",
//                                record.getFieldValue(FieldType.wifeFatherComment)
//                                );
//
//                        writeValue(2,"Mère:",
//                            record.getFieldValue(FieldType.wifeMotherFirstName),
//                            record.getFieldValue(FieldType.wifeMotherLastName),
//                            record.getFieldValue(FieldType.wifeMotherDead)
//                            );
//                        writeValue(3,"Age:",
//                                formatAgeToAge(record.getWife().getMotherAge())
//                                );
//                        writeValue(3,"Profession:",
//                                record.getFieldValue(FieldType.wifeMotherOccupation)
//                                );
//                        writeValue(3,"Domicile:",
//                                record.getFieldValue(FieldType.wifeMotherResidence)
//                                );
//                        writeValue(3,"Info:",
//                                record.getFieldValue(FieldType.wifeMotherComment)
//                                );
//
//                        for (Record.Witness witness : record.getWitnesses()) {
//                            line.appendCsvFn(witness.getLastName().toString());
//                            line.appendCsvFn(witness.getFirstName().toString());
//                            line.appendCsvFn(witness.getOccupation().toString());
//                            line.appendCsvFn(witness.getFieldValue(Record.FieldType.indiComment));
//                        }
//
//                         writeValue(1,"Info:",
//                                record.getFieldValue(Record.FieldType.generalComment)
//                                );
//                        line.appendCsv(String.valueOf(index)); // numero d'enregistrement
//                    }
//
//                } catch (IOException e) {
//                    sb.append("Line ").append(" " ).append(e).append("\n");
//                    sb.append("   ").append(line).append("\n");
//                }
//            }
//            //writer.close();
//            FileOutputStream fileOutputStream = new FileOutputStream(fileName, append);
//            Format format = new PDFFormat();
//            format.format(doc, fileOutputStream);
//
//        } catch (IOException e) {
//            sb.append(e).append("\n");
//        }
        return sb;
    }
    
    /**
     * concatene plusieurs commentaires dans une chaine , séparés par un espace
     * Le lobellé est ajouté au debut si les valeurs ne sont pas vides .
     */
    static private void writeTitle(int indent, String... otherValues) throws IOException {
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        for (int i = 0; i < otherValues.length -3; i++) {

            // j'ajoute les valeurs supplémentaires séparées par des virgules
            if (!otherValues[i].trim().isEmpty()) {
                // je concantene les valeurs en inserant une virgule dans
                // si la valeur précedente n'est pas vide
                if (sb1.length() > 0) {
                    sb1.append(" ");
                }
                sb1.append(otherValues[i].trim());
            }
        }

        for (int i = otherValues.length -3; i < otherValues.length; i++) {

            // j'ajoute les valeurs supplémentaires séparées par des virgules
            if (!otherValues[i].trim().isEmpty()) {
                // je concantene les valeurs en inserant une virgule dans
                // si la valeur précedente n'est pas vide
                if (sb2.length() > 0) {
                    sb2.append(" ");
                }
                sb2.append(otherValues[i].trim());
            }
        }

        if( sb1.length() > 0) {
            doc.nextParagraph("start-indent=0pt,space-before=10pt");
            doc.addText(" ");
            doc.startTable("width=100%");
            doc.addTableColumn("column-width=70%");
            doc.addTableColumn("column-width=30%");
            doc.nextTableRow("text-align=left");
            doc.addText(sb1.toString(), "text-decoration=underline,font-size=11pt,font-weight=bold");
            doc.nextTableCell("text-align=right");
            doc.addText(sb2.toString(), "font-size=10pt");
            doc.endTable();

            //doc.nextParagraph("text-decoration=underline,space-before=10pt,text-align=center");
            //doc.addText(sb.toString(), "font-size=12pt,font-weight=bold");
            //doc.addText(sb.toString(), "font-size=12pt,font-weight=bold,text-align=right");
            //font-weight=normal,
        }
    }


    
    /**
     * concatene plusieurs commentaires dans une chaine , séparés par un espace
     * Le lobellé est ajouté au debut si les valeurs ne sont pas vides .
     */
    static private void writeValue(int indent, String label, String... otherValues) throws IOException {
        StringBuilder sb = new StringBuilder();

        for (String otherValue : otherValues) {
            // j'ajoute les valeurs supplémentaires séparées par des virgules
            if (!otherValue.trim().isEmpty()) {
                // je concantene les valeurs en inserant une virgule dans
                // si la valeur précedente n'est pas vide
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(otherValue.trim());
            }
        }

        if( sb.length() > 0) {
            doc.nextParagraph("start-indent="+20*indent+"pt");
            if (!label.trim().isEmpty()) {
                doc.addText(label.trim()+ " ", "font-size=10pt,font-weight=bold");
            }
            doc.addText(sb.toString(),"font-size=10pt");
        }
    }



    /**
     * concatene plusieurs commentaires dans une chaine , séparés par un espace
     * Le lobellé est ajouté au debut si les valeurs ne sont pas vides .
     */
//    static private void writeLabelValue(int indent, String label, String... otherValues) throws IOException {
//        StringBuilder sb = new StringBuilder();
//
//        for (String otherValue : otherValues) {
//            // j'ajoute les valeurs supplémentaires séparées par des virgules
//            if (!otherValue.trim().isEmpty()) {
//                // je concantene les valeurs en inserant une virgule dans
//                // si la valeur précedente n'est pas vide
//                if (sb.length() > 0) {
//                    sb.append(" ");
//                }
//                sb.append(otherValue.trim());
//            }
//        }
//        if( sb.length() > 0) {
//            if (!label.trim().isEmpty()) {
//                sb.insert(0, label.trim() + " ");
//            }
//            for (int i = 0 ; i< indent; i++) {
//                sb.insert(0, "    ");
//            }
//            sb.append("\r\n");
//            writer.write(sb.toString());
//        }
//    }

    /**
     * Convertit l'age normalisé "9y 9m 9d"
     * en une expression française "9a 9m 9d"
     * @param agedField
     * @return
     */
    static private String formatAgeToAge(FieldAge agedField) {
        String ageString;
        if (agedField != null) {
            if (agedField.getValue().equals("0d")) {
                ageString = "";
            } else {
                ageString = agedField.getValue().replace('y', 'a').replace('d', 'j');
            }
        } else {
            ageString = "";
        }
        return ageString;
    }
   
}
