package ancestris.modules.releve.file;

import ancestris.modules.releve.model.PlaceManager;
import ancestris.modules.releve.model.RecordModel;
import ancestris.modules.releve.model.RecordMisc;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordMarriage;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.Record;
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
    public static StringBuilder saveFile(PlaceManager placeManager, RecordModel recordModel, RecordType recordType, File fileName, boolean append) {

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


                        line.appendCsvFn(record.getIndi().getLastName().getValue());
                        line.appendCsvFn(record.getIndi().getFirstName().getValue());
                        line.appendCsvFn(record.getIndi().getSex().toString());
                        line.appendCsvFn(record.getIndi().getBirthPlace().toString()); // place
                        line.appendCsvFn(record.getIndi().getBirthDate().toString()); // IndiBirthDate
                        line.appendCsvFn(""); // age
                        line.appendCsvFn(""); // occupation
                        line.appendCsvFn(""); // residence
                        line.appendCsvFn(record.getIndi().getComment() .toString());

                        line.appendCsvFn(""); // IndiMarriedLastName
                        line.appendCsvFn(""); // IndiMarriedFirstName
                        line.appendCsvFn(""); // IndiMarriedDead
                        line.appendCsvFn(""); // IndiMarriedOccupation
                        line.appendCsvFn(""); // residence
                        line.appendCsvFn(""); // IndiMarriedComment

                        line.appendCsvFn(record.getIndi().getFatherLastName().toString());
                        line.appendCsvFn(record.getIndi().getFatherFirstName().toString());
                        line.appendCsvFn(record.getIndi().getFatherAge().getValue());
                        line.appendCsvFn(record.getIndi().getFatherDead().toString());
                        line.appendCsvFn(record.getIndi().getFatherOccupation().toString());
                        line.appendCsvFn(record.getIndi().getFatherResidence().toString());
                        line.appendCsvFn(record.getIndi().getFatherComment().toString());
                        line.appendCsvFn(record.getIndi().getMotherLastName().toString());
                        line.appendCsvFn(record.getIndi().getMotherFirstName().toString());
                        line.appendCsvFn(record.getIndi().getMotherAge().getValue());
                        line.appendCsvFn(record.getIndi().getMotherDead().toString());
                        line.appendCsvFn(record.getIndi().getMotherOccupation().toString());
                        line.appendCsvFn(record.getIndi().getMotherResidence().toString());
                        line.appendCsvFn(record.getIndi().getMotherComment().toString());

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

                        for(Record.Witness witness : record.getWitnesses()) {
                            line.appendCsvFn(witness.getLastName().toString());
                            line.appendCsvFn(witness.getFirstName().toString());
                            line.appendCsvFn(witness.getOccupation().toString());
                            line.appendCsvFn(witness.getComment().toString());
                        }

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


                        line.appendCsvFn(record.getIndi().getLastName().getValue());
                        line.appendCsvFn(record.getIndi().getFirstName().getValue());
                        line.appendCsvFn(""); // IndiSex
                        line.appendCsvFn(record.getIndi().getBirthPlace().toString());
                        line.appendCsvFn(record.getIndi().getBirthDate().toString());
                        line.appendCsvFn(record.getIndi().getAge().getValue());
                        line.appendCsvFn(record.getIndi().getOccupation().toString());
                        line.appendCsvFn(record.getIndi().getResidence() .toString());
                        line.appendCsvFn(record.getIndi().getComment() .toString());
                        
                        line.appendCsvFn(record.getIndi().getMarriedLastName().toString());
                        line.appendCsvFn(record.getIndi().getMarriedFirstName().toString());
                        line.appendCsvFn(record.getIndi().getMarriedDead().toString());
                        line.appendCsvFn(record.getIndi().getMarriedOccupation().toString());
                        line.appendCsvFn(record.getIndi().getMarriedResidence().toString());
                        line.appendCsvFn(record.getIndi().getMarriedComment().toString());

                        line.appendCsvFn(record.getIndi().getFatherLastName().toString());
                        line.appendCsvFn(record.getIndi().getFatherFirstName().toString());
                        line.appendCsvFn(record.getIndi().getFatherAge().getValue());
                        line.appendCsvFn(record.getIndi().getFatherDead().toString());
                        line.appendCsvFn(record.getIndi().getFatherOccupation().toString());
                        line.appendCsvFn(record.getIndi().getFatherResidence().toString());
                        line.appendCsvFn(record.getIndi().getFatherComment().toString());
                        
                        line.appendCsvFn(record.getIndi().getMotherLastName().toString());
                        line.appendCsvFn(record.getIndi().getMotherFirstName().toString());
                        line.appendCsvFn(record.getIndi().getMotherAge().getValue());
                        line.appendCsvFn(record.getIndi().getMotherDead().toString());
                        line.appendCsvFn(record.getIndi().getMotherOccupation().toString());
                        line.appendCsvFn(record.getIndi().getMotherResidence().toString());
                        line.appendCsvFn(record.getIndi().getMotherComment().toString());
                        
                        line.appendCsvFn(record.getWife().getLastName().toString());
                        line.appendCsvFn(record.getWife().getFirstName().toString());
                        line.appendCsvFn(""); //WifeSex
                        line.appendCsvFn(record.getWife().getBirthPlace().toString());
                        line.appendCsvFn(record.getWife().getBirthDate().toString());
                        line.appendCsvFn(record.getWife().getAge().getValue());
                        line.appendCsvFn(record.getWife().getOccupation().toString());
                        line.appendCsvFn(record.getWife().getResidence().toString());
                        line.appendCsvFn(record.getWife().getComment().toString());
                        
                        line.appendCsvFn(record.getWife().getMarriedLastName().toString());
                        line.appendCsvFn(record.getWife().getMarriedFirstName().toString());
                        line.appendCsvFn(record.getWife().getMarriedDead().toString());
                        line.appendCsvFn(record.getWife().getMarriedOccupation().toString());
                        line.appendCsvFn(record.getWife().getMarriedResidence().toString());
                        line.appendCsvFn(record.getWife().getMarriedComment().toString());

                        line.appendCsvFn(record.getWife().getFatherLastName().toString());
                        line.appendCsvFn(record.getWife().getFatherFirstName().toString());
                        line.appendCsvFn(record.getWife().getFatherAge().getValue());
                        line.appendCsvFn(record.getWife().getFatherDead().toString());
                        line.appendCsvFn(record.getWife().getFatherOccupation().toString());
                        line.appendCsvFn(record.getWife().getFatherResidence().toString());
                        line.appendCsvFn(record.getWife().getFatherComment().toString());
                        line.appendCsvFn(record.getWife().getMotherLastName().toString());
                        line.appendCsvFn(record.getWife().getMotherFirstName().toString());
                        line.appendCsvFn(record.getWife().getMotherAge().getValue());
                        line.appendCsvFn(record.getWife().getMotherDead().toString());
                        line.appendCsvFn(record.getWife().getMotherOccupation().toString());
                        line.appendCsvFn(record.getWife().getMotherResidence().toString());
                        line.appendCsvFn(record.getWife().getMotherComment().toString());

                        for(Record.Witness witness : record.getWitnesses()) {
                            line.appendCsvFn(witness.getLastName().toString());
                            line.appendCsvFn(witness.getFirstName().toString());
                            line.appendCsvFn(witness.getOccupation().toString());
                            line.appendCsvFn(witness.getComment().toString());
                        }

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


                        line.appendCsvFn(record.getIndi().getLastName().toString());
                        line.appendCsvFn(record.getIndi().getFirstName().toString());
                        line.appendCsvFn(record.getIndi().getSex().toString());
                        line.appendCsvFn(record.getIndi().getBirthPlace().toString());
                        line.appendCsvFn(record.getIndi().getBirthDate().toString());
                        line.appendCsvFn(record.getIndi().getAge().getValue());
                        line.appendCsvFn(record.getIndi().getOccupation().toString());
                        line.appendCsvFn(record.getIndi().getResidence() .toString());
                        line.appendCsvFn(record.getIndi().getComment() .toString());

                        line.appendCsvFn(record.getIndi().getMarriedLastName().toString());
                        line.appendCsvFn(record.getIndi().getMarriedFirstName().toString());
                        line.appendCsvFn(record.getIndi().getMarriedDead().toString());
                        line.appendCsvFn(record.getIndi().getMarriedOccupation().toString());
                        line.appendCsvFn(record.getIndi().getMarriedResidence().toString());
                        line.appendCsvFn(record.getIndi().getMarriedComment().toString());

                        line.appendCsvFn(record.getIndi().getFatherLastName().toString());
                        line.appendCsvFn(record.getIndi().getFatherFirstName().toString());
                        line.appendCsvFn(record.getIndi().getFatherAge().getValue());
                        line.appendCsvFn(record.getIndi().getFatherDead().toString());
                        line.appendCsvFn(record.getIndi().getFatherOccupation().toString());
                        line.appendCsvFn(record.getIndi().getFatherResidence().toString());
                        line.appendCsvFn(record.getIndi().getFatherComment().toString());
                        line.appendCsvFn(record.getIndi().getMotherLastName().toString());
                        line.appendCsvFn(record.getIndi().getMotherFirstName().toString());
                        line.appendCsvFn(record.getIndi().getMotherAge().getValue());
                        line.appendCsvFn(record.getIndi().getMotherDead().toString());
                        line.appendCsvFn(record.getIndi().getMotherOccupation().toString());
                        line.appendCsvFn(record.getIndi().getMotherResidence().toString());
                        line.appendCsvFn(record.getIndi().getMotherComment().toString());

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

                        for(Record.Witness witness : record.getWitnesses()) {
                            line.appendCsvFn(witness.getLastName().toString());
                            line.appendCsvFn(witness.getFirstName().toString());
                            line.appendCsvFn(witness.getOccupation().toString());
                            line.appendCsvFn(witness.getComment().toString());
                        }

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
                        String participantNames = record.getIndi().getLastName().toString();
                        if( !record.getWife().getLastName().isEmpty()) {
                            participantNames += " et " + record.getWife().getLastName().toString();
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
                                record.getIndi().getFirstName().toString(),
                                record.getIndi().getLastName().toString()
                                );
                        writeValue(2,"Naissance:",
                                record.getIndi().getBirthDate().toString(),
                                record.getIndi().getBirthPlace().toString()
                                );
                        writeValue(2,"Age:",
                                formatAgeToAge(record.getIndi().getAge())
                                );
                        writeValue(2,"Profession:",
                                record.getIndi().getOccupation().toString()
                                );
                        writeValue(2,"Domicile:",
                                record.getIndi().getResidence() .toString()
                                );
                        writeValue(2,"Info:",
                                record.getIndi().getComment() .toString()
                                );

                        writeValue(2,"Conjoint:",
                                record.getIndi().getMarriedFirstName().toString(),
                                record.getIndi().getMarriedLastName().toString(),
                                record.getIndi().getMarriedDead().toString()
                                );
                        writeValue(3,"Profession:",
                                record.getIndi().getMarriedOccupation().toString()
                                );
                        writeValue(3,"Domicile:",
                                record.getIndi().getMarriedResidence().toString()
                                );
                        writeValue(3,"Info:",
                                record.getIndi().getMarriedComment().toString()
                                );

                        writeValue(2,"Père:",
                            record.getIndi().getFatherFirstName().toString(),
                            record.getIndi().getFatherLastName().toString(),
                            record.getIndi().getFatherDead().toString()
                            );
                        writeValue(3,"Age:",
                                formatAgeToAge(record.getIndi().getFatherAge())
                                );
                        writeValue(3,"Profession:",
                                record.getIndi().getFatherOccupation().toString()
                                );
                        writeValue(3,"Domicile:",
                                record.getIndi().getFatherResidence().toString()
                                );
                        writeValue(3,"Info:",
                                record.getIndi().getFatherComment().toString()
                                );

                        writeValue(2,"Mère:",
                            record.getIndi().getMotherFirstName().toString(),
                            record.getIndi().getMotherLastName().toString(),
                            record.getIndi().getMotherDead().toString()
                            );
                        writeValue(3,"Age:",
                                formatAgeToAge(record.getIndi().getMotherAge())
                                );
                        writeValue(3,"Profession:",
                                record.getIndi().getMotherOccupation().toString()
                                );
                        writeValue(3,"Domicile:",
                                record.getIndi().getMotherResidence().toString()
                                );
                        writeValue(3,"Info:",
                                record.getIndi().getMotherComment().toString()
                                );
                        
                        ///////////////////////////////////////////////////////
                        writeValue(1,"Intervenant 2:",
                                record.getWife().getFirstName().toString(),
                                record.getWife().getLastName().toString()
                                );
                        writeValue(2,"Naissance:",
                                record.getWife().getBirthDate().toString(),
                                record.getWife().getBirthPlace().toString()
                                );
                        writeValue(2,"Age:",
                                formatAgeToAge(record.getWife().getAge())
                                );
                        writeValue(2,"Profession:",
                                record.getWife().getOccupation().toString()
                                );
                        writeValue(2,"Domicile:",
                                record.getWife().getResidence().toString()
                                );
                        writeValue(2,"Info:",
                                record.getWife().getComment().toString()
                                );

                        writeValue(2,"Conjoint:",
                                record.getWife().getMarriedFirstName().toString(),
                                record.getWife().getMarriedLastName().toString(),
                                record.getWife().getMarriedDead().toString()
                                );
                        writeValue(3,"Profession:",
                                record.getWife().getMarriedOccupation().toString()
                                );
                        writeValue(3,"Domicile:",
                                record.getWife().getMarriedResidence().toString()
                                );
                        writeValue(3,"Info:",
                                record.getWife().getMarriedComment().toString()
                                );

                        writeValue(2,"Père:",
                            record.getWife().getFatherFirstName().toString(),
                            record.getWife().getFatherLastName().toString(),
                            record.getWife().getFatherDead().toString()
                            );
                        writeValue(3,"Age:",
                                formatAgeToAge(record.getWife().getFatherAge())
                                );
                        writeValue(3,"Profession:",
                                record.getWife().getFatherOccupation().toString()
                                );
                        writeValue(3,"Domicile:",
                                record.getWife().getFatherResidence().toString()
                                );
                        writeValue(3,"Info:",
                                record.getWife().getFatherComment().toString()
                                );

                        writeValue(2,"Mère:",
                            record.getWife().getMotherFirstName().toString(),
                            record.getWife().getMotherLastName().toString(),
                            record.getWife().getMotherDead().toString()
                            );
                        writeValue(3,"Age:",
                                formatAgeToAge(record.getWife().getMotherAge())
                                );
                        writeValue(3,"Profession:",
                                record.getWife().getMotherOccupation().toString()
                                );
                        writeValue(3,"Domicile:",
                                record.getWife().getMotherResidence().toString()
                                );
                        writeValue(3,"Info:",
                                record.getWife().getMotherComment().toString()
                                );

                        for (Record.Witness witness : record.getWitnesses()) {
                            line.appendCsvFn(witness.getLastName().toString());
                            line.appendCsvFn(witness.getFirstName().toString());
                            line.appendCsvFn(witness.getOccupation().toString());
                            line.appendCsvFn(witness.getComment().toString());
                        }

                         writeValue(1,"Info:",
                                record.getGeneralComment().toString()
                                );
                        line.appendCsv(String.valueOf(index)); // numero d'enregistrement
                    }

                } catch (IOException e) {
                    sb.append("Line ").append(" " ).append(e).append("\n");
                    sb.append("   ").append(line).append("\n");
                }
            }
            //writer.close();
            FileOutputStream fileOutputStream = new FileOutputStream(fileName, append);
            Format format = new PDFFormat();
            format.format(doc, fileOutputStream);

        } catch (IOException e) {
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
