package ancestris.modules.releve.file;

import ancestris.modules.releve.model.PlaceManager;
import ancestris.modules.releve.model.RecordModel;
import ancestris.modules.releve.model.RecordMisc;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordMarriage;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.file.FileManager.Line;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.FieldAge;
import genj.fo.Document;
import genj.fo.Format;
import genj.fo.PDFFormat;
import java.io.BufferedReader;
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

    
    /**
     * decoupe une ligne
     * @param strLine
     * @return fields[] ou null si la ligne n'est pas valide
     */
    private static String[] splitLine(BufferedReader br) throws Exception {
            return null;
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
     * Comparateur de transposition ( ignore la casse des noms )
     */
    public static class RecordComparator implements Comparator<Record> {
        @Override
        public int compare(Record record1, Record record2) {
            return record1.getEventDateProperty().compareTo(record2.getEventDateProperty());
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
    public static StringBuilder saveFile(PlaceManager placeManager, RecordModel recordModel, DataManager.RecordType recordType, File fileName, boolean append) {

        StringBuilder sb = new StringBuilder();
        try {
            //create BufferedReader to read csv file
            //create BufferedReader to read csv file

            ArrayList<Record> recordTree = new ArrayList<Record>();
            for(int i=0; i < recordModel.getRowCount(); i++) {
                recordTree.add(recordModel.getRecord(i));
            }
            //Collections.sort(recordTree, new RecordComparator());
            
            doc = new Document("");

            // j'ajoute les autres lignes
            for (int index = 0; index < recordTree.size(); index++) {
                Record record = recordTree.get(index);
                if( recordType != null && recordType != record.getType()) {
                    continue;
                }
                Line line = new Line(fieldSeparator);
                try {
                    if ( record instanceof RecordBirth ) {
                        line.appendCsvFn(fileSignature);
                        line.appendCsvFn(placeManager.getCityName());
                        line.appendCsvFn(placeManager.getCityCode());
                        line.appendCsvFn(placeManager.getCountyName());
                        line.appendCsvFn(placeManager.getStateName());
                        line.appendCsvFn(placeManager.getCountryName());
                        line.appendCsvFn(record.getParish().toString());
                        line.appendCsvFn("N");
                        line.appendCsvFn(""); //eventTypeTag
                        line.appendCsvFn(""); //eventTypeName
                        line.appendCsvFn(record.getEventDateString());
                        line.appendCsvFn(record.getCote().toString());
                        line.appendCsvFn(record.getFreeComment().toString());
                        line.appendCsvFn(""); // notary

                        writeTitle(1,
                                record.getEventDateString(),
                                "Naissance",
                                record.getCote().toString(),
                                record.getFreeComment().toString()
                                );


                        line.appendCsvFn(record.getIndiLastName().getValue());
                        line.appendCsvFn(record.getIndiFirstName().getValue());
                        line.appendCsvFn(record.getIndiSex().toString());
                        line.appendCsvFn(record.getIndiBirthPlace().toString()); // place
                        line.appendCsvFn(record.getIndiBirthDate().toString()); // IndiBirthDate
                        line.appendCsvFn(""); // age
                        line.appendCsvFn(""); // occupation
                        line.appendCsvFn(""); // residence
                        line.appendCsvFn(record.getIndiComment().toString());

                        line.appendCsvFn(""); // IndiMarriedLastName
                        line.appendCsvFn(""); // IndiMarriedFirstName
                        line.appendCsvFn(""); // IndiMarriedDead
                        line.appendCsvFn(""); // IndiMarriedOccupation
                        line.appendCsvFn(""); // residence
                        line.appendCsvFn(""); // IndiMarriedComment

                        line.appendCsvFn(record.getIndiFatherLastName().toString());
                        line.appendCsvFn(record.getIndiFatherFirstName().toString());
                        line.appendCsvFn(record.getIndiFatherAge().getValue());
                        line.appendCsvFn(record.getIndiFatherDead().toString());
                        line.appendCsvFn(record.getIndiFatherOccupation().toString());
                        line.appendCsvFn(record.getIndiFatherResidence().toString());
                        line.appendCsvFn(record.getIndiFatherComment().toString());
                        line.appendCsvFn(record.getIndiMotherLastName().toString());
                        line.appendCsvFn(record.getIndiMotherFirstName().toString());
                        line.appendCsvFn(record.getIndiMotherAge().getValue());
                        line.appendCsvFn(record.getIndiMotherDead().toString());
                        line.appendCsvFn(record.getIndiMotherOccupation().toString());
                        line.appendCsvFn(record.getIndiMotherResidence().toString());
                        line.appendCsvFn(record.getIndiMotherComment().toString());

                        line.appendCsvFn(""); // WifeLastName
                        line.appendCsvFn(""); // WifeFirstName
                        line.appendCsvFn(""); // WifeSex
                        line.appendCsvFn(""); // WifePlace
                        line.appendCsvFn(""); // WifeBirthDate
                        line.appendCsvFn(""); // WifeAge
                        line.appendCsvFn(""); // WifeOccupation
                        line.appendCsvFn(""); // WifeResidence
                        line.appendCsvFn(""); // WifeComment

                        line.appendCsvFn(""); // WifeMarriedLastName
                        line.appendCsvFn(""); // WifeMarriedFirstName
                        line.appendCsvFn(""); // WifeMarriedDead
                        line.appendCsvFn(""); // WifeMarriedOccupation
                        line.appendCsvFn(""); // WifeMarriedResidence
                        line.appendCsvFn(""); // WifeMarriedComment

                        line.appendCsvFn(""); // WifeFatherLastName
                        line.appendCsvFn(""); // WifeFatherFirstName
                        line.appendCsvFn(""); // WifeFatherAge
                        line.appendCsvFn(""); // WifeFatherDead
                        line.appendCsvFn(""); // WifeFatherOccupation
                        line.appendCsvFn(""); // WifeFatherResidence
                        line.appendCsvFn(""); // WifeFatherComment
                        line.appendCsvFn(""); // WifeMotherLastName
                        line.appendCsvFn(""); // WifeMotherFirstName
                        line.appendCsvFn(""); // WifeMotherAge
                        line.appendCsvFn(""); // WifeMotherDead
                        line.appendCsvFn(""); // WifeMotherOccupation
                        line.appendCsvFn(""); // WifeMotherResidence
                        line.appendCsvFn(""); // WifeMotherComment

                        line.appendCsvFn(record.getWitness1LastName().toString());
                        line.appendCsvFn(record.getWitness1FirstName().toString());
                        line.appendCsvFn(record.getWitness1Occupation().toString());
                        line.appendCsvFn(record.getWitness1Comment().toString());

                        line.appendCsvFn(record.getWitness2LastName().toString());
                        line.appendCsvFn(record.getWitness2FirstName().toString());
                        line.appendCsvFn(record.getWitness2Occupation().toString());
                        line.appendCsvFn(record.getWitness2Comment().toString());

                        line.appendCsvFn(record.getWitness3LastName().toString());
                        line.appendCsvFn(record.getWitness3FirstName().toString());
                        line.appendCsvFn(record.getWitness3Occupation().toString());
                        line.appendCsvFn(record.getWitness3Comment().toString());

                        line.appendCsvFn(record.getWitness4LastName().toString());
                        line.appendCsvFn(record.getWitness4FirstName().toString());
                        line.appendCsvFn(record.getWitness4Occupation().toString());
                        line.appendCsvFn(record.getWitness4Comment().toString());

                        line.appendCsvFn(record.getGeneralComment().toString());
                        line.appendCsv(String.valueOf(index)); // numero d'enregistrement

                    } if ( record instanceof RecordMarriage ) {

                        line.appendCsvFn(fileSignature);
                        line.appendCsvFn(placeManager.getCityName());
                        line.appendCsvFn(placeManager.getCityCode());
                        line.appendCsvFn(placeManager.getCountyName());
                        line.appendCsvFn(placeManager.getStateName());
                        line.appendCsvFn(placeManager.getCountryName());
                        line.appendCsvFn(record.getParish().toString());
                        line.appendCsvFn("M");
                        line.appendCsvFn(""); //eventTypeTag
                        line.appendCsvFn(""); //eventTypeName
                        line.appendCsvFn(record.getEventDateString());
                        line.appendCsvFn(record.getCote().toString());
                        line.appendCsvFn(record.getFreeComment().toString());
                        line.appendCsvFn(""); // notary

                        writeTitle(1,
                                record.getEventDateString(),
                                "Mariage",
                                record.getCote().toString(),
                                record.getFreeComment().toString()
                                );


                        line.appendCsvFn(record.getIndiLastName().getValue());
                        line.appendCsvFn(record.getIndiFirstName().getValue());
                        line.appendCsvFn(""); // IndiSex
                        line.appendCsvFn(record.getIndiBirthPlace().toString());
                        line.appendCsvFn(record.getIndiBirthDate().toString());
                        line.appendCsvFn(record.getIndiAge().getValue());
                        line.appendCsvFn(record.getIndiOccupation().toString());
                        line.appendCsvFn(record.getIndiResidence().toString());
                        line.appendCsvFn(record.getIndiComment().toString());
                        
                        line.appendCsvFn(record.getIndiMarriedLastName().toString());
                        line.appendCsvFn(record.getIndiMarriedFirstName().toString());
                        line.appendCsvFn(record.getIndiMarriedDead().toString());
                        line.appendCsvFn(record.getIndiMarriedOccupation().toString());
                        line.appendCsvFn(record.getIndiMarriedResidence().toString());
                        line.appendCsvFn(record.getIndiMarriedComment().toString());

                        line.appendCsvFn(record.getIndiFatherLastName().toString());
                        line.appendCsvFn(record.getIndiFatherFirstName().toString());
                        line.appendCsvFn(record.getIndiFatherAge().getValue());
                        line.appendCsvFn(record.getIndiFatherDead().toString());
                        line.appendCsvFn(record.getIndiFatherOccupation().toString());
                        line.appendCsvFn(record.getIndiFatherResidence().toString());
                        line.appendCsvFn(record.getIndiFatherComment().toString());
                        
                        line.appendCsvFn(record.getIndiMotherLastName().toString());
                        line.appendCsvFn(record.getIndiMotherFirstName().toString());
                        line.appendCsvFn(record.getIndiMotherAge().getValue());
                        line.appendCsvFn(record.getIndiMotherDead().toString());
                        line.appendCsvFn(record.getIndiMotherOccupation().toString());
                        line.appendCsvFn(record.getIndiMotherResidence().toString());
                        line.appendCsvFn(record.getIndiMotherComment().toString());
                        
                        line.appendCsvFn(record.getWifeLastName().toString());
                        line.appendCsvFn(record.getWifeFirstName().toString());
                        line.appendCsvFn(""); //WifeSex
                        line.appendCsvFn(record.getWifeBirthPlace().toString());
                        line.appendCsvFn(record.getWifeBirthDate().toString());
                        line.appendCsvFn(record.getWifeAge().getValue());
                        line.appendCsvFn(record.getWifeOccupation().toString());
                        line.appendCsvFn(record.getWifeResidence().toString());
                        line.appendCsvFn(record.getWifeComment().toString());
                        
                        line.appendCsvFn(record.getWifeMarriedLastName().toString());
                        line.appendCsvFn(record.getWifeMarriedFirstName().toString());
                        line.appendCsvFn(record.getWifeMarriedDead().toString());
                        line.appendCsvFn(record.getWifeMarriedOccupation().toString());
                        line.appendCsvFn(record.getWifeMarriedResidence().toString());
                        line.appendCsvFn(record.getWifeMarriedComment().toString());

                        line.appendCsvFn(record.getWifeFatherLastName().toString());
                        line.appendCsvFn(record.getWifeFatherFirstName().toString());
                        line.appendCsvFn(record.getWifeFatherAge().getValue());
                        line.appendCsvFn(record.getWifeFatherDead().toString());
                        line.appendCsvFn(record.getWifeFatherOccupation().toString());
                        line.appendCsvFn(record.getWifeFatherResidence().toString());
                        line.appendCsvFn(record.getWifeFatherComment().toString());
                        line.appendCsvFn(record.getWifeMotherLastName().toString());
                        line.appendCsvFn(record.getWifeMotherFirstName().toString());
                        line.appendCsvFn(record.getWifeMotherAge().getValue());
                        line.appendCsvFn(record.getWifeMotherDead().toString());
                        line.appendCsvFn(record.getWifeMotherOccupation().toString());
                        line.appendCsvFn(record.getWifeMotherResidence().toString());
                        line.appendCsvFn(record.getWifeMotherComment().toString());

                        line.appendCsvFn(record.getWitness1LastName().toString());
                        line.appendCsvFn(record.getWitness1FirstName().toString());
                        line.appendCsvFn(record.getWitness1Occupation().toString());
                        line.appendCsvFn(record.getWitness1Comment().toString());

                        line.appendCsvFn(record.getWitness2LastName().toString());
                        line.appendCsvFn(record.getWitness2FirstName().toString());
                        line.appendCsvFn(record.getWitness2Occupation().toString());
                        line.appendCsvFn(record.getWitness2Comment().toString());

                        line.appendCsvFn(record.getWitness3LastName().toString());
                        line.appendCsvFn(record.getWitness3FirstName().toString());
                        line.appendCsvFn(record.getWitness3Occupation().toString());
                        line.appendCsvFn(record.getWitness3Comment().toString());

                        line.appendCsvFn(record.getWitness4LastName().toString());
                        line.appendCsvFn(record.getWitness4FirstName().toString());
                        line.appendCsvFn(record.getWitness4Occupation().toString());
                        line.appendCsvFn(record.getWitness4Comment().toString());

                        line.appendCsvFn(record.getGeneralComment().toString());
                        line.appendCsv(String.valueOf(index)); // numero d'enregistrement

                    } else if ( record instanceof RecordDeath ) {

                        line.appendCsvFn(fileSignature);
                        line.appendCsvFn(placeManager.getCityName());
                        line.appendCsvFn(placeManager.getCityCode());
                        line.appendCsvFn(placeManager.getCountyName());
                        line.appendCsvFn(placeManager.getStateName());
                        line.appendCsvFn(placeManager.getCountryName());
                        line.appendCsvFn(record.getParish().toString());
                        line.appendCsvFn("D");
                        line.appendCsvFn(""); //eventTypeTag
                        line.appendCsvFn(""); //eventTypeName
                        line.appendCsvFn(record.getEventDateString());
                        line.appendCsvFn(record.getCote().toString());
                        line.appendCsvFn(record.getFreeComment().toString());
                        line.appendCsvFn(""); // notary

                        writeTitle(1,
                                record.getEventDateString(),
                                "Décés",
                                record.getCote().toString(),
                                record.getFreeComment().toString()
                                );


                        line.appendCsvFn(record.getIndiLastName().toString());
                        line.appendCsvFn(record.getIndiFirstName().toString());
                        line.appendCsvFn(record.getIndiSex().toString());
                        line.appendCsvFn(record.getIndiBirthPlace().toString());
                        line.appendCsvFn(record.getIndiBirthDate().toString());
                        line.appendCsvFn(record.getIndiAge().getValue());
                        line.appendCsvFn(record.getIndiOccupation().toString());
                        line.appendCsvFn(record.getIndiResidence().toString());
                        line.appendCsvFn(record.getIndiComment().toString());

                        line.appendCsvFn(record.getIndiMarriedLastName().toString());
                        line.appendCsvFn(record.getIndiMarriedFirstName().toString());
                        line.appendCsvFn(record.getIndiMarriedDead().toString());
                        line.appendCsvFn(record.getIndiMarriedOccupation().toString());
                        line.appendCsvFn(record.getIndiMarriedResidence().toString());
                        line.appendCsvFn(record.getIndiMarriedComment().toString());

                        line.appendCsvFn(record.getIndiFatherLastName().toString());
                        line.appendCsvFn(record.getIndiFatherFirstName().toString());
                        line.appendCsvFn(record.getIndiFatherAge().getValue());
                        line.appendCsvFn(record.getIndiFatherDead().toString());
                        line.appendCsvFn(record.getIndiFatherOccupation().toString());
                        line.appendCsvFn(record.getIndiFatherResidence().toString());
                        line.appendCsvFn(record.getIndiFatherComment().toString());
                        line.appendCsvFn(record.getIndiMotherLastName().toString());
                        line.appendCsvFn(record.getIndiMotherFirstName().toString());
                        line.appendCsvFn(record.getIndiMotherAge().getValue());
                        line.appendCsvFn(record.getIndiMotherDead().toString());
                        line.appendCsvFn(record.getIndiMotherOccupation().toString());
                        line.appendCsvFn(record.getIndiMotherResidence().toString());
                        line.appendCsvFn(record.getIndiMotherComment().toString());

                        line.appendCsvFn(""); // WifeLastName
                        line.appendCsvFn(""); // WifeFirstName
                        line.appendCsvFn(""); // WifeSex
                        line.appendCsvFn(""); // WifePlace
                        line.appendCsvFn(""); // WifeBirthDate
                        line.appendCsvFn(""); // WifeAge
                        line.appendCsvFn(""); // WifeOccupation
                        line.appendCsvFn(""); // WifeResidence
                        line.appendCsvFn(""); // WifeComment

                        line.appendCsvFn(""); // WifeMarriedLastName
                        line.appendCsvFn(""); // WifeMarriedFirstName
                        line.appendCsvFn(""); // WifeMarriedDead
                        line.appendCsvFn(""); // WifeMarriedOccupation
                        line.appendCsvFn(""); // WifeMarriedResidence
                        line.appendCsvFn(""); // WifeMarriedComment

                        line.appendCsvFn(""); // WifeFatherLastName
                        line.appendCsvFn(""); // WifeFatherFirstName
                        line.appendCsvFn(""); // WifeFatherAge
                        line.appendCsvFn(""); // WifeFatherDead
                        line.appendCsvFn(""); // WifeFatherOccupation
                        line.appendCsvFn(""); // WifeFatherResidence
                        line.appendCsvFn(""); // WifeFatherComment
                        line.appendCsvFn(""); // WifeMotherLastName
                        line.appendCsvFn(""); // WifeMotherFirstName
                        line.appendCsvFn(""); // WifeMotherAge
                        line.appendCsvFn(""); // WifeMotherDead
                        line.appendCsvFn(""); // WifeMotherOccupation
                        line.appendCsvFn(""); // WifeMotherResidence
                        line.appendCsvFn(""); // WifeMotherComment

                        line.appendCsvFn(record.getWitness1LastName().toString());
                        line.appendCsvFn(record.getWitness1FirstName().toString());
                        line.appendCsvFn(record.getWitness1Occupation().toString());
                        line.appendCsvFn(record.getWitness1Comment().toString());

                        line.appendCsvFn(record.getWitness2LastName().toString());
                        line.appendCsvFn(record.getWitness2FirstName().toString());
                        line.appendCsvFn(record.getWitness2Occupation().toString());
                        line.appendCsvFn(record.getWitness2Comment().toString());

                        line.appendCsvFn(record.getWitness3LastName().toString());
                        line.appendCsvFn(record.getWitness3FirstName().toString());
                        line.appendCsvFn(record.getWitness3Occupation().toString());
                        line.appendCsvFn(record.getWitness3Comment().toString());

                        line.appendCsvFn(record.getWitness4LastName().toString());
                        line.appendCsvFn(record.getWitness4FirstName().toString());
                        line.appendCsvFn(record.getWitness4Occupation().toString());
                        line.appendCsvFn(record.getWitness4Comment().toString());

                        line.appendCsvFn(record.getGeneralComment().toString());
                        line.appendCsv(String.valueOf(index)); // numero d'enregistrement

                    } else if ( record instanceof RecordMisc ) {

//                        line.appendCsvFn(fileSignature);
//                        line.appendCsvFn(placeManager.getCityName());
//                        line.appendCsvFn(placeManager.getCityCode());
//                        line.appendCsvFn(placeManager.getCountyName());
//                        line.appendCsvFn(placeManager.getStateName());
//                        line.appendCsvFn(placeManager.getCountryName());
//                        line.appendCsvFn(record.getParish().toString());
                        String participantNames = record.getIndiLastName().toString();
                        if( !record.getWifeLastName().isEmpty()) {
                            participantNames += " et " + record.getWifeLastName().toString();
                        }
                        writeTitle(1,
                                record.getEventDateString(),
                                record.getEventType().getName(),
                                participantNames,
                                record.getEventSecondDateString(),
                                record.getCote().toString(),
                                record.getFreeComment().toString()
                                );

                        writeValue(1,"Notaire:",
                            record.getNotary().toString()
                            );

                        writeValue(1,"Intervenant 1:",
                                record.getIndiFirstName().toString(),
                                record.getIndiLastName().toString()
                                );
                        writeValue(2,"Naissance:",
                                record.getIndiBirthDate().toString(),
                                record.getIndiBirthPlace().toString()
                                );
                        writeValue(2,"Age:",
                                formatAgeToAge(record.getIndiAge())
                                );
                        writeValue(2,"Profession:",
                                record.getIndiOccupation().toString()
                                );
                        writeValue(2,"Domicile:",
                                record.getIndiResidence().toString()
                                );
                        writeValue(2,"Info:",
                                record.getIndiComment().toString()
                                );

                        writeValue(2,"Conjoint:",
                                record.getIndiMarriedFirstName().toString(),
                                record.getIndiMarriedLastName().toString(),
                                record.getIndiMarriedDead().toString()
                                );
                        writeValue(3,"Profession:",
                                record.getIndiMarriedOccupation().toString()
                                );
                        writeValue(3,"Domicile:",
                                record.getIndiMarriedResidence().toString()
                                );
                        writeValue(3,"Info:",
                                record.getIndiMarriedComment().toString()
                                );

                        writeValue(2,"Père:",
                            record.getIndiFatherFirstName().toString(),
                            record.getIndiFatherLastName().toString(),
                            record.getIndiFatherDead().toString()
                            );
                        writeValue(3,"Age:",
                                formatAgeToAge(record.getIndiFatherAge())
                                );
                        writeValue(3,"Profession:",
                                record.getIndiFatherOccupation().toString()
                                );
                        writeValue(3,"Domicile:",
                                record.getIndiFatherResidence().toString()
                                );
                        writeValue(3,"Info:",
                                record.getIndiFatherComment().toString()
                                );

                        writeValue(2,"Mère:",
                            record.getIndiMotherFirstName().toString(),
                            record.getIndiMotherLastName().toString(),
                            record.getIndiMotherDead().toString()
                            );
                        writeValue(3,"Age:",
                                formatAgeToAge(record.getIndiMotherAge())
                                );
                        writeValue(3,"Profession:",
                                record.getIndiMotherOccupation().toString()
                                );
                        writeValue(3,"Domicile:",
                                record.getIndiMotherResidence().toString()
                                );
                        writeValue(3,"Info:",
                                record.getIndiMotherComment().toString()
                                );
                        
                        ///////////////////////////////////////////////////////
                        writeValue(1,"Intervenant 2:",
                                record.getWifeFirstName().toString(),
                                record.getWifeLastName().toString()
                                );
                        writeValue(2,"Naissance:",
                                record.getWifeBirthDate().toString(),
                                record.getWifeBirthPlace().toString()
                                );
                        writeValue(2,"Age:",
                                formatAgeToAge(record.getWifeAge())
                                );
                        writeValue(2,"Profession:",
                                record.getWifeOccupation().toString()
                                );
                        writeValue(2,"Domicile:",
                                record.getWifeResidence().toString()
                                );
                        writeValue(2,"Info:",
                                record.getWifeComment().toString()
                                );

                        writeValue(2,"Conjoint:",
                                record.getWifeMarriedFirstName().toString(),
                                record.getWifeMarriedLastName().toString(),
                                record.getWifeMarriedDead().toString()
                                );
                        writeValue(3,"Profession:",
                                record.getWifeMarriedOccupation().toString()
                                );
                        writeValue(3,"Domicile:",
                                record.getWifeMarriedResidence().toString()
                                );
                        writeValue(3,"Info:",
                                record.getWifeMarriedComment().toString()
                                );

                        writeValue(2,"Père:",
                            record.getWifeFatherFirstName().toString(),
                            record.getWifeFatherLastName().toString(),
                            record.getWifeFatherDead().toString()
                            );
                        writeValue(3,"Age:",
                                formatAgeToAge(record.getWifeFatherAge())
                                );
                        writeValue(3,"Profession:",
                                record.getWifeFatherOccupation().toString()
                                );
                        writeValue(3,"Domicile:",
                                record.getWifeFatherResidence().toString()
                                );
                        writeValue(3,"Info:",
                                record.getWifeFatherComment().toString()
                                );

                        writeValue(2,"Mère:",
                            record.getWifeMotherFirstName().toString(),
                            record.getWifeMotherLastName().toString(),
                            record.getWifeMotherDead().toString()
                            );
                        writeValue(3,"Age:",
                                formatAgeToAge(record.getWifeMotherAge())
                                );
                        writeValue(3,"Profession:",
                                record.getWifeMotherOccupation().toString()
                                );
                        writeValue(3,"Domicile:",
                                record.getWifeMotherResidence().toString()
                                );
                        writeValue(3,"Info:",
                                record.getWifeMotherComment().toString()
                                );


//                        line.appendCsvFn(record.getWitness1LastName().toString());
//                        line.appendCsvFn(record.getWitness1FirstName().toString());
//                        line.appendCsvFn(record.getWitness1Occupation().toString());
//                        line.appendCsvFn(record.getWitness1Comment().toString());
//
//                        line.appendCsvFn(record.getWitness2LastName().toString());
//                        line.appendCsvFn(record.getWitness2FirstName().toString());
//                        line.appendCsvFn(record.getWitness2Occupation().toString());
//                        line.appendCsvFn(record.getWitness2Comment().toString());
//
//                        line.appendCsvFn(record.getWitness3LastName().toString());
//                        line.appendCsvFn(record.getWitness3FirstName().toString());
//                        line.appendCsvFn(record.getWitness3Occupation().toString());
//                        line.appendCsvFn(record.getWitness3Comment().toString());
//
//                        line.appendCsvFn(record.getWitness4LastName().toString());
//                        line.appendCsvFn(record.getWitness4FirstName().toString());
//                        line.appendCsvFn(record.getWitness4Occupation().toString());
//                        line.appendCsvFn(record.getWitness4Comment().toString());

                         writeValue(1,"Info:",
                                record.getGeneralComment().toString()
                                );
                        line.appendCsv(String.valueOf(index)); // numero d'enregistrement
                    }

                } catch (Exception e) {
                    sb.append("Line ").append(" " ).append(e).append("\n");
                    sb.append("   ").append(line).append("\n");
                }
            }
            //writer.close();
            FileOutputStream fileOutputStream = new FileOutputStream(fileName, append);
            Format format = new PDFFormat();
            format.format(doc, fileOutputStream);

        } catch (Exception e) {
            sb.append(e).append("\n");
        }
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
