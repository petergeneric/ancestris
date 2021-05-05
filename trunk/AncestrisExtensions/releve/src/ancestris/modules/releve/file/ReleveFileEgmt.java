package ancestris.modules.releve.file;

import ancestris.modules.releve.file.FileManager.Line;
import ancestris.modules.releve.model.PlaceManager;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.Record.RecordType;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import ancestris.modules.releve.model.RecordMisc;
import ancestris.modules.releve.model.RecordModel;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Michel
 */
public class ReleveFileEgmt {

    final static char fieldSeparator = ';';
    final static private String fileSignature = "EGMT";
    final static private String marriageContractEventType = "contrat de mariage";
    final static private String willEventType = "testament";

    /**
     * verifie si la premere ligne est conforme au format
     * @param inputFile
     * @param sb  message d'erreur
     * @return
     */
    public static boolean isValidFile(File inputFile, StringBuilder sb) {
        BufferedReader br = null;
        try {
            //br = new BufferedReader( new InputStreamReader(new FileInputStream(inputFile),"UTF-8"));
            br = new BufferedReader(new FileReader(inputFile));
            String[] fields = splitLine(br);

            if (fields == null) {
                sb.append(fileSignature + " ").append(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.EmptyFile"), inputFile.getName()));
                return false;
            }
        } catch (Exception ex) {
            sb.append(fileSignature + " ").append(ex.getMessage());
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    // rien a faire
                }
            }
        }
        return true;
    }

    /**
     * decoupe une ligne
     * @param strLine
     * @return fields[] ou null si la ligne n'est pas valide
     */
    private static String[] splitLine( BufferedReader br) throws Exception  {
        String[] fields = Line.splitCSV(br, fieldSeparator);
        if (fields != null) {
            if (fields.length == 44) {
                return fields;
             } else {
                throw new Exception(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.FieldNb"), fields.length, 44));
            }
        } else {
            return null;
        }
    }

   // Format d'un releve
    enum EgmtField {
        typeActe,
        codeDepartement, nomCommune, paroisse, notaire, cote, folio,
        day,month,year,
        indiLastName, indiFirstName, indiSex, indiAge, indiPlace, indiComment,
        indiFatherFirstName, indiFatherDead, indiFatherComment,
        indiMotherLastName, indiMotherFirstName, indiMotherDead, indiMotherComment,
        wifeLastName, wifeFirstName, wifeDead, wifeAge, wifePlace, wifeComment,
        wifeFatherFirstName, wifeFatherDead, wifeFatherComment,
        wifeMotherLastName, wifeMotherFirstName, wifeMotherDead, wifeMotherComment,
        heirComment,
        witness1LastName, witness1FirstName, witness1Comment,
        witness2LastName, witness2FirstName, witness2Comment,
        generalComment
    }

    
    /**
     *
     * @param fileName
     */
    public static FileBuffer loadFile(File inputFile) throws Exception {

        FileBuffer fileBuffer = new FileBuffer();
        try {

            //create BufferedReader to read file
            //BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),"UTF-8"));
            BufferedReader br = new BufferedReader(new FileReader(inputFile));
            String strLine = "";
            int lineNumber = 0;

            String[] fields ;
            //read comma separated file line by line
            while ((fields = splitLine(br)) != null) {
                lineNumber++;
                try {
                    if ( lineNumber == 1) {
                        fileBuffer.setRegisterInfoPlace(   
                            fields[EgmtField.nomCommune.ordinal()],
                            "", // codecommune
                            fields[EgmtField.codeDepartement.ordinal()],
                            "", // stateName
                            "" ); // countryName
                    }
                    if (fields != null) {
                        if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("naissance")) {
                            RecordBirth record = new RecordBirth();
                            record.setFieldValue(FieldType.cote, fields[EgmtField.cote.ordinal()]);
                            record.setFieldValue(FieldType.parish, fields[EgmtField.paroisse.ordinal()]);
                            record.setFieldValue(FieldType.freeComment,  fields[EgmtField.folio.ordinal()]);

                            record.setFieldValue(FieldType.eventDate, formatDate(  
                                    fields[EgmtField.day.ordinal()],
                                    fields[EgmtField.month.ordinal()],
                                    fields[EgmtField.year.ordinal()] ));

                            record.setIndi(
                                    fields[EgmtField.indiFirstName.ordinal()],
                                    fields[EgmtField.indiLastName.ordinal()],
                                    fields[EgmtField.indiSex.ordinal()],
                                    "", // pas d'age a la naissance
                                    "", // pas de date de naissance 
                                    fields[EgmtField.indiPlace.ordinal()],
                                    "", // pas d'adresse dans ce format
                                    "", // pas de profession a la naissance
                                    "", // pas de residence dans ce format
                                    "", // pas d'adresse dans ce format
                                    fields[EgmtField.indiComment.ordinal()]);
                            record.setIndiFather(
                                    fields[EgmtField.indiFatherFirstName.ordinal()],
                                    getFatherLastName(fields), // meme nom que Indi
                                    "", // profession
                                    "", // pas de residence dans ce format
                                    "", // pas d'adresse dans ce format
                                    fields[EgmtField.indiFatherComment.ordinal()],
                                    fields[EgmtField.indiFatherDead.ordinal()],
                                    "");  //age

                            record.setIndiMother(
                                    fields[EgmtField.indiMotherFirstName.ordinal()],
                                    fields[EgmtField.indiMotherLastName.ordinal()],
                                    "", //profession
                                    "", // pas de residence dans ce format
                                    "", // pas d'adresse dans ce format
                                    fields[EgmtField.indiMotherComment.ordinal()],
                                    fields[EgmtField.indiMotherDead.ordinal()],
                                    "");  //age

                            record.setWitness1(
                                    fields[EgmtField.witness1FirstName.ordinal()],
                                    fields[EgmtField.witness1LastName.ordinal()],
                                    "",
                                    fields[EgmtField.witness1Comment.ordinal()]);

                            record.setWitness2(
                                    fields[EgmtField.witness2FirstName.ordinal()],
                                    fields[EgmtField.witness2LastName.ordinal()],
                                    "",
                                    fields[EgmtField.witness2Comment.ordinal()]);

                            if (! fields[EgmtField.generalComment.ordinal()].isEmpty() ) {
                                record.setFieldValue(FieldType.generalComment, 
                                   fields[EgmtField.generalComment.ordinal()]
                                   + " " +
                                   fields[EgmtField.heirComment.ordinal()] );
                            } else {
                                record.setFieldValue(FieldType.generalComment, fields[EgmtField.generalComment.ordinal()]);
                            }
                            //record.recordNo = lineNumber;
                            fileBuffer.addRecord(record);

                        } else if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("mariage")) {
                            RecordMarriage record = new RecordMarriage();
                            record.setFieldValue(FieldType.cote, fields[EgmtField.cote.ordinal()]);
                            record.setFieldValue(FieldType.parish, fields[EgmtField.paroisse.ordinal()]);
                            record.setFieldValue(FieldType.freeComment,  fields[EgmtField.folio.ordinal()]);

                            record.setFieldValue(FieldType.eventDate, formatDate(  
                                    fields[EgmtField.day.ordinal()],
                                    fields[EgmtField.month.ordinal()],
                                    fields[EgmtField.year.ordinal()] ));

                            record.setIndi(
                                    fields[EgmtField.indiFirstName.ordinal()],
                                    fields[EgmtField.indiLastName.ordinal()],
                                    fields[EgmtField.indiSex.ordinal()],
                                    formatAgeToField(fields[EgmtField.indiAge.ordinal()]),
                                    "", // pas de date de naissance a la naissance
                                    "", // pas de lieu de naissance dans ce format
                                    "", // pas d'adresse dans ce format
                                    "", // pas de profession dans ce format
                                    fields[EgmtField.indiPlace.ordinal()],
                                    "", // pas d'adresse dans ce format
                                    fields[EgmtField.indiComment.ordinal()]);

                            record.setIndiFather(
                                    fields[EgmtField.indiFatherFirstName.ordinal()],
                                    getFatherLastName(fields), // meme nom que Indi
                                    "", // profession
                                    "", // pas de residence dans ce format
                                    "", // pas d'adresse dans ce format
                                    fields[EgmtField.indiFatherComment.ordinal()],
                                    fields[EgmtField.indiFatherDead.ordinal()],
                                    "");  //age

                            record.setIndiMother(
                                    fields[EgmtField.indiMotherFirstName.ordinal()],
                                    fields[EgmtField.indiMotherLastName.ordinal()],
                                    "", //profession
                                    "", // pas de residence dans ce format
                                    "", // pas d'adresse dans ce format
                                    fields[EgmtField.indiMotherComment.ordinal()],
                                    fields[EgmtField.indiMotherDead.ordinal()],
                                    "");  //age

                            record.setWife(
                                    fields[EgmtField.wifeFirstName.ordinal()],
                                    fields[EgmtField.wifeLastName.ordinal()],
                                    fields[EgmtField.indiSex.ordinal()].equals("M") ? "F" : "M",
                                    formatAgeToField(fields[EgmtField.wifeAge.ordinal()]),
                                    "" , // date de naissance
                                    "", // pas de lieu de naissance dans ce format
                                    "", // pas d'adresse dans ce format
                                    "" , // pas de profession dans ce format
                                    fields[EgmtField.wifePlace.ordinal()],
                                    "", // pas d'adresse dans ce format
                                    fields[EgmtField.wifeComment.ordinal()]);
                            //TODO : deces de l'epouse est ignoré , fields[EgmtField.wifeDead.ordinal()]
                                
                             record.setWifeFather(
                                    fields[EgmtField.wifeFatherFirstName.ordinal()],
                                    fields[EgmtField.wifeLastName.ordinal()], // meme nom que la femme
                                    "" , // profession
                                    "", // pas de residence dans ce format
                                    "", // pas d'adresse dans ce format
                                    fields[EgmtField.wifeFatherComment.ordinal()],
                                    fields[EgmtField.wifeFatherDead.ordinal()],
                                    "");  //age

                                record.setWifeMother(
                                    fields[EgmtField.wifeMotherFirstName.ordinal()],
                                    fields[EgmtField.wifeMotherLastName.ordinal()],
                                    "" , // profession
                                    "", // pas de residence dans ce format
                                    "", // pas d'adresse dans ce format
                                    fields[EgmtField.wifeMotherComment.ordinal()],
                                    fields[EgmtField.wifeMotherDead.ordinal()] ,
                                    "");  //age

                                record.setWitness1(
                                    fields[EgmtField.witness1FirstName.ordinal()],
                                    fields[EgmtField.witness1LastName.ordinal()],
                                    "",
                                    fields[EgmtField.witness1Comment.ordinal()]);

                                record.setWitness2(
                                    fields[EgmtField.witness2FirstName.ordinal()],
                                    fields[EgmtField.witness2LastName.ordinal()],
                                    "",
                                    fields[EgmtField.witness2Comment.ordinal()]);

                            if (! fields[EgmtField.generalComment.ordinal()].isEmpty() ) {
                                record.setFieldValue(FieldType.generalComment, 
                                   fields[EgmtField.generalComment.ordinal()]
                                   + " " +
                                   fields[EgmtField.heirComment.ordinal()] );
                            } else {
                                record.setFieldValue(FieldType.generalComment, fields[EgmtField.generalComment.ordinal()]);
                            }
                            //record.recordNo = lineNumber;
                            fileBuffer.addRecord(record);

                        } else if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("décès")) {
                            RecordDeath record = new RecordDeath();
                            record.setFieldValue(FieldType.cote, fields[EgmtField.cote.ordinal()]);
                            record.setFieldValue(FieldType.parish, fields[EgmtField.paroisse.ordinal()]);
                            record.setFieldValue(FieldType.freeComment,  fields[EgmtField.folio.ordinal()]);

                            record.setFieldValue(FieldType.eventDate, formatDate(  
                                    fields[EgmtField.day.ordinal()],
                                    fields[EgmtField.month.ordinal()],
                                    fields[EgmtField.year.ordinal()] ));

                            record.setIndi(
                                    fields[EgmtField.indiFirstName.ordinal()],
                                    fields[EgmtField.indiLastName.ordinal()],
                                    fields[EgmtField.indiSex.ordinal()],
                                    formatAgeToField(fields[EgmtField.indiAge.ordinal()]),
                                    "", // date de naissance
                                    "", // pas de lieu de naissance dans ce format
                                    "", // pas d'adresse dans ce format
                                    "", // pas de profession dans ce format
                                    fields[EgmtField.indiPlace.ordinal()],
                                    "", // pas d'adresse dans ce format
                                    fields[EgmtField.indiComment.ordinal()]);

                             record.setIndiFather(
                                    fields[EgmtField.indiFatherFirstName.ordinal()],
                                    getFatherLastName(fields), // meme nom que Indi
                                    "", // profession
                                    "", // pas de residence dans ce format
                                    "", // pas d'adresse dans ce format
                                    fields[EgmtField.indiFatherComment.ordinal()],
                                    fields[EgmtField.indiFatherDead.ordinal()] ,
                                    "");  //age

                            record.setIndiMother(
                                    fields[EgmtField.indiMotherFirstName.ordinal()],
                                    fields[EgmtField.indiMotherLastName.ordinal()],
                                    "", //profession
                                    "", // pas de residence dans ce format
                                    "", // pas d'adresse dans ce format
                                    fields[EgmtField.indiMotherComment.ordinal()],
                                    fields[EgmtField.indiMotherDead.ordinal()],
                                    "");  //age

                            record.setIndiMarried(
                                    fields[EgmtField.wifeFirstName.ordinal()],
                                    fields[EgmtField.wifeLastName.ordinal()],
                                    "" , // profession
                                    "", // pas de residence dans ce format
                                    "", // pas d'adresse dans ce format
                                    fields[EgmtField.wifeComment.ordinal()],
                                    fields[EgmtField.wifeDead.ordinal()]
                                    );
                                                                
                            record.setWitness1(
                                    fields[EgmtField.witness1FirstName.ordinal()],
                                    fields[EgmtField.witness1LastName.ordinal()],
                                    "",
                                    fields[EgmtField.witness1Comment.ordinal()]);

                            record.setWitness2(
                                    fields[EgmtField.witness2FirstName.ordinal()],
                                    fields[EgmtField.witness2LastName.ordinal()],
                                    "",
                                    fields[EgmtField.witness2Comment.ordinal()]);

                            if (! fields[EgmtField.generalComment.ordinal()].isEmpty() ) {
                                record.setFieldValue(FieldType.generalComment, 
                                   fields[EgmtField.generalComment.ordinal()]
                                   + " " +
                                   fields[EgmtField.heirComment.ordinal()] );
                            } else {
                                record.setFieldValue(FieldType.generalComment, fields[EgmtField.generalComment.ordinal()]);
                            }
                            //record.recordNo = lineNumber;
                            fileBuffer.addRecord(record);

                        } else  {
                            RecordMisc record = new RecordMisc();
                            if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("contrat de mariage")) {
                                record.setFieldValue(FieldType.eventType,"MARC");
                            } else if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("testament")) {
                                record.setFieldValue(FieldType.eventType, "WILL");
                            } else if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("evenement")) {
                                // j'ignore la ligne d'entete
                                continue;
                            } else if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("type d'acte")) {
                                // j'ignore la ligne d'entete
                                continue;
                            } else if (fields[EgmtField.typeActe.ordinal()].isEmpty()) {
                                // j'ignore une ligne vide
                                continue;
                            } else {
                                record.setFieldValue(FieldType.eventType, fields[EgmtField.typeActe.ordinal()]);
                            }

                            record.setFieldValue(FieldType.parish, fields[EgmtField.paroisse.ordinal()]);
                            // le notaire est utilisé seelement pour les actes divers
                            record.setFieldValue(FieldType.notary, fields[EgmtField.notaire.ordinal()]);
                            record.setFieldValue(FieldType.cote, fields[EgmtField.cote.ordinal()]);
                            record.setFieldValue(FieldType.freeComment,  fields[EgmtField.folio.ordinal()]);

                            record.setFieldValue(FieldType.eventDate, formatDate(  
                                    fields[EgmtField.day.ordinal()],
                                    fields[EgmtField.month.ordinal()],
                                    fields[EgmtField.year.ordinal()] ));

                            record.setIndi(
                                    fields[EgmtField.indiFirstName.ordinal()],
                                    fields[EgmtField.indiLastName.ordinal()],
                                    fields[EgmtField.indiSex.ordinal()],
                                    formatAgeToField(fields[EgmtField.indiAge.ordinal()]),
                                    "", // date de naissance
                                    "", // pas de lieu de naissance dans ce format
                                    "", // pas d'adresse dans ce format
                                    "", // pas de profession dans ce format
                                    fields[EgmtField.indiPlace.ordinal()],
                                    "", // pas d'adresse dans ce format
                                    fields[EgmtField.indiComment.ordinal()]);
                            
                            record.setIndiFather(
                                    fields[EgmtField.indiFatherFirstName.ordinal()],
                                    getFatherLastName(fields), // meme nom que Indi
                                    "", // profession
                                    "", // pas de residence dans ce format
                                    "", // pas d'adresse dans ce format
                                    fields[EgmtField.indiFatherComment.ordinal()],
                                    fields[EgmtField.indiFatherDead.ordinal()] ,
                                    "");  //age

                            record.setIndiMother(
                                    fields[EgmtField.indiMotherFirstName.ordinal()],
                                    fields[EgmtField.indiMotherLastName.ordinal()],
                                    "", //profession
                                    "", // pas de residence dans ce format
                                    "", // pas d'adresse dans ce format
                                    fields[EgmtField.indiMotherComment.ordinal()],
                                    fields[EgmtField.indiMotherDead.ordinal()],
                                    "");  //age

                            if ( record.getFieldValue(FieldType.eventType).equals("MARC")) {
                                // contrat de mariage
                                record.setWife(
                                        fields[EgmtField.wifeFirstName.ordinal()],
                                        fields[EgmtField.wifeLastName.ordinal()],
                                        fields[EgmtField.indiSex.ordinal()].equals("M") ? "F" : "M",
                                        formatAgeToField(fields[EgmtField.wifeAge.ordinal()]),
                                        "", // date de naissance
                                        "", // pas de lieu de naissance dans ce format
                                        "", // pas d'adresse dans ce format
                                        "" , // profession
                                        fields[EgmtField.wifePlace.ordinal()],
                                        "", // pas d'adresse dans ce format
                                        fields[EgmtField.wifeComment.ordinal()]);

                                record.setWifeFather(
                                        fields[EgmtField.wifeFatherFirstName.ordinal()],
                                        fields[EgmtField.wifeLastName.ordinal()], // meme nom que la femme
                                        "" , // profession
                                        "", // pas de residence dans ce format
                                        "", // pas d'adresse dans ce format
                                        fields[EgmtField.wifeFatherComment.ordinal()],
                                        fields[EgmtField.wifeFatherDead.ordinal()] ,
                                        "");  //age

                                record.setWifeMother(
                                        fields[EgmtField.wifeMotherFirstName.ordinal()],
                                        fields[EgmtField.wifeMotherLastName.ordinal()],
                                        "" , // profession
                                        "", // pas de residence dans ce format
                                        "", // pas d'adresse dans ce format
                                        fields[EgmtField.wifeMotherComment.ordinal()],
                                        fields[EgmtField.wifeMotherDead.ordinal()] ,
                                        "");  //age
                            } else {
                                // testament et autres evenements
                                record.setIndiMarried(
                                        fields[EgmtField.wifeFirstName.ordinal()],
                                        fields[EgmtField.wifeLastName.ordinal()],
                                        "", //profession
                                        fields[EgmtField.wifePlace.ordinal()],
                                        "", // pas d'adresse dans ce format
                                        fields[EgmtField.wifeComment.ordinal()],
                                        fields[EgmtField.wifeDead.ordinal()]
                                        );
                            }

                            record.setWitness1(
                                    fields[EgmtField.witness1FirstName.ordinal()],
                                    fields[EgmtField.witness1LastName.ordinal()],
                                    "",
                                    fields[EgmtField.witness1Comment.ordinal()]);

                            record.setWitness2(
                                    fields[EgmtField.witness2FirstName.ordinal()],
                                    fields[EgmtField.witness2LastName.ordinal()],
                                    "",
                                    fields[EgmtField.witness2Comment.ordinal()]);

                            if (! fields[EgmtField.heirComment.ordinal()].isEmpty() ) {
                                record.setFieldValue(FieldType.generalComment, 
                                   fields[EgmtField.generalComment.ordinal()]
                                   + ", Héritier: " +
                                   fields[EgmtField.heirComment.ordinal()] );
                            } else {
                                record.setFieldValue(FieldType.generalComment, fields[EgmtField.generalComment.ordinal()]);
                            }
                            //record.recordNo = lineNumber;
                            fileBuffer.addRecord(record);

                        }

                    } else {
                        fileBuffer.append(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.LineNo"), lineNumber ));
                        fileBuffer.append("\n");
                        fileBuffer.append(strLine).append("\n   ");
                        fileBuffer.append(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.InvalidLine"));
                    }
                } catch (NumberFormatException e) {
                    fileBuffer.append(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.LineNo"), lineNumber ));
                    fileBuffer.append("\n");
                    fileBuffer.append(strLine).append("\n   ");
                    fileBuffer.append(e.getMessage()).append("\n");
                }
            } // for
            br.close();
        } catch (Exception e) {
            System.out.println("Exception while reading file: " + e);
            throw e;
        }
        return fileBuffer;
    }

    /**
     *
     * @param fileName
     */
    public static StringBuilder saveFile(PlaceManager placeManager, RecordModel recordModel, RecordType recordType, File fileName, boolean append) {
        StringBuilder sb = new StringBuilder();

        try {
            //create BufferedReader to read csv file
            //OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileName, append), "UTF-8") ;
            FileWriter writer = new FileWriter(fileName, append);
            
            // j'ajoute l'entete si le mode append n'est pas demandé
            if( ! append) {
                writer.write(getHeader().toString());
            }
            
            for (int index = 0; index < recordModel.getRowCount(); index++) {
                final Record record = recordModel.getRecord(index);
                if( recordType != null && recordType != record.getType()) {
                    continue;
                }
                Line line = new Line(fieldSeparator);
                try {
                    if ( record instanceof RecordBirth ) {
                       line.appendCsvFn("naissance");
                    } else if (record instanceof RecordMarriage) {
                        line.appendCsvFn("mariage");
                    } else if (record instanceof RecordDeath) {
                        line.appendCsvFn("décès");
                    } else {
                        line.appendCsvFn(record.getFieldValue(FieldType.eventType).toLowerCase());
                    }
                    line.appendCsvFn(placeManager.getCountyName());
                    line.appendCsvFn(placeManager.getCityName());
                    line.appendCsvFn(record.getFieldValue(FieldType.parish));
                    if ( record instanceof RecordMisc ) {
                        line.appendCsvFn(record.getFieldValue(FieldType.notary));
                    } else {
                        line.appendCsvFn(""); // Notary
                    }
                    line.appendCsvFn(record.getFieldValue(FieldType.cote));
                    line.appendCsvFn(record.getFieldValue(FieldType.freeComment));
                    
//                    if ( record.getEventDateProperty().getStart().getDay() != Integer.MAX_VALUE ) {
//                       line.appendCsvFn(String.format("%02d", record.getEventDateProperty().getStart().getDay()+1));
//                    } else {
//                       line.appendCsvFn(""); // Day
//                    }
//                    if ( record.getEventDateProperty().getStart().getMonth() != Integer.MAX_VALUE ) {
//                       line.appendCsvFn(String.format("%02d", record.getEventDateProperty().getStart().getMonth()+1));
//                    } else {
//                       line.appendCsvFn(""); // Month
//                    }
//
//                    if ( record.getEventDateProperty().getStart().getYear() != Integer.MAX_VALUE ) {
//                       line.appendCsvFn(String.format("%02d", record.getEventDateProperty().getStart().getYear()));
//                    } else {
//                       line.appendCsvFn(""); // Year
//                    }

                    String[] eventDate = record.getFieldValue(FieldType.eventDate).split("/");
                    if ( eventDate.length == 3 ) {
                       line.appendCsvFn(eventDate[0]); // Day
                       line.appendCsvFn(eventDate[1]); // Month
                       line.appendCsvFn(eventDate[2]); // Year
                    } else if ( eventDate.length == 2 ) {
                       line.appendCsvFn(""); // Day
                       line.appendCsvFn(eventDate[0]); // Month
                       line.appendCsvFn(eventDate[2]); // Year
                    } else if ( eventDate.length == 1 ) {
                       line.appendCsvFn(""); // Day
                       line.appendCsvFn(""); // Month
                       line.appendCsvFn(eventDate[0]); // Year
                    } else {
                       line.appendCsvFn(""); // Day
                       line.appendCsvFn(""); // Month
                       line.appendCsvFn(""); // Year
                    }

                    line.appendCsvFn(record.getFieldValue(FieldType.indiLastName));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiFirstName));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiSex));
                    line.appendCsvFn(formatAgeToAge(record.getFieldValue(FieldType.indiAge)));
                    if ( record instanceof RecordBirth) {
                        line.appendCsvFn(record.getFieldValue(FieldType.indiBirthPlace));
                    } else {
                        line.appendCsvFn(record.getFieldValue(FieldType.indiResidence));
                    }
                    
                    if ( record instanceof RecordBirth) {
                        line.appendCsvFn(record.getFieldValue(FieldType.indiComment));
                    } else {
                        String birthDate = "";
                        if ( ! record.getFieldValue(FieldType.indiBirthDate).isEmpty()){
                            birthDate = "né le " + record.getFieldValue(FieldType.indiBirthDate);
                        }

                        if ((record instanceof RecordMarriage) 
                         || (record instanceof RecordMisc  && record.getFieldValue(FieldType.eventType).toLowerCase().equals(marriageContractEventType))) {
                            // mariage ou contrat de mariage
                            line.appendCsvFn(
                                record.getFieldValue(FieldType.indiComment),
                                birthDate,
                                record.getFieldValue(FieldType.indiBirthPlace),
                                record.getFieldValue(FieldType.indiOccupation),
                                appendLabelValue("Ex conjoint:",
                                        record.getFieldValue(FieldType.indiMarriedFirstName),
                                        record.getFieldValue(FieldType.indiMarriedLastName),
                                        record.getFieldString(FieldType.indiMarriedDead),
                                        record.getFieldValue(FieldType.indiMarriedOccupation),
                                        record.getFieldValue(FieldType.indiMarriedResidence),
                                        record.getFieldValue(FieldType.indiMarriedComment)
                                        )
                            );
                        } else {
                            // testament ou autre evenement
                            line.appendCsvFn(
                                record.getFieldValue(FieldType.indiComment),
                                birthDate,
                                record.getFieldValue(FieldType.indiBirthPlace),
                                record.getFieldValue(FieldType.indiOccupation)
                            );
                        }

                    }
                    
                    line.appendCsvFn(record.getFieldValue(FieldType.indiFatherFirstName));
                    line.appendCsvFn(record.getFieldString(FieldType.indiFatherDead));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiFatherComment),
                            record.getFieldValue(FieldType.indiFatherOccupation),
                            record.getFieldValue(FieldType.indiFatherResidence),
                            formatAgeToComment(record.getFieldValue(FieldType.indiFatherAge)));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiMotherLastName));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiMotherFirstName));
                    line.appendCsvFn(record.getFieldString(FieldType.indiMotherDead));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiMotherComment),
                            record.getFieldValue(FieldType.indiMotherOccupation),
                            record.getFieldValue(FieldType.indiMotherResidence),
                            formatAgeToComment(record.getFieldValue(FieldType.indiMotherAge)));

                    String otherParticipant = "";
                    if ((record instanceof RecordMarriage) 
                         || (record instanceof RecordMisc  && record.getFieldValue(FieldType.eventType).toLowerCase().equals(marriageContractEventType))) {
                        // mariage ou contrat de mariage
                        line.appendCsvFn(record.getFieldValue(FieldType.wifeLastName));  //WifeLastName
                        line.appendCsvFn(record.getFieldValue(FieldType.wifeFirstName)); //WifeFirstName
                        line.appendCsvFn(""); //wifeDead
                        line.appendCsvFn(formatAgeToAge(record.getFieldValue(FieldType.wifeAge)));  // WifeAge
                        line.appendCsvFn(record.getFieldValue(FieldType.wifeResidence)); //WifeResidence
                        
                        line.appendCsvFn(                                       //WifeComment
                            record.getFieldValue(FieldType.wifeComment),
                            appendLabelValue("né le", record.getFieldValue(FieldType.wifeBirthDate)),
                            record.getFieldValue(FieldType.wifeBirthPlace),
                            record.getFieldValue(FieldType.wifeOccupation),
                            appendLabelValue(
                                "Ex conjoint:",
                                record.getFieldValue(FieldType.wifeMarriedFirstName),
                                record.getFieldValue(FieldType.wifeMarriedLastName),
                                record.getFieldString(FieldType.wifeMarriedDead),
                                record.getFieldValue(FieldType.wifeMarriedOccupation),
                                record.getFieldValue(FieldType.wifeMarriedResidence),
                                record.getFieldValue(FieldType.wifeMarriedComment)
                                )
                            );

                        line.appendCsvFn(record.getFieldValue(FieldType.wifeFatherFirstName));
                        line.appendCsvFn(record.getFieldString(FieldType.wifeFatherDead));
                        line.appendCsvFn(record.getFieldValue(FieldType.wifeFatherComment), 
                            record.getFieldValue(FieldType.wifeFatherOccupation),
                            record.getFieldValue(FieldType.wifeFatherResidence),
                            formatAgeToComment(record.getFieldValue(FieldType.wifeFatherAge)));
                        line.appendCsvFn(record.getFieldValue(FieldType.wifeMotherLastName));
                        line.appendCsvFn(record.getFieldValue(FieldType.wifeMotherFirstName));
                        line.appendCsvFn(record.getFieldString(FieldType.wifeMotherDead));
                        line.appendCsvFn(record.getFieldValue(FieldType.wifeMotherComment), 
                            record.getFieldValue(FieldType.wifeMotherOccupation),
                            record.getFieldValue(FieldType.wifeMotherResidence),
                            formatAgeToComment(record.getFieldValue(FieldType.wifeMotherAge)));

                        line.appendCsvFn(""); // heirComment

                    } else  if (record instanceof RecordMisc  && record.getFieldValue(FieldType.eventType).toLowerCase().equals(willEventType)) {
                        // Testament
                        line.appendCsvFn(record.getFieldValue(FieldType.indiMarriedLastName)); //WifeLastName
                        line.appendCsvFn(record.getFieldValue(FieldType.indiMarriedFirstName)); //WifeFirstName
                        line.appendCsvFn(record.getFieldString(FieldType.indiMarriedDead));   //wifeDead
                        line.appendCsvFn(""); // WifeAge
                        line.appendCsvFn(record.getFieldValue(FieldType.indiMarriedResidence)); //WifeResidence
                        line.appendCsvFn( //WifeComment
                            record.getFieldValue(FieldType.indiMarriedOccupation),
                            record.getFieldValue(FieldType.indiMarriedComment)
                            );

                        line.appendCsvFn(""); //WifeFatherFirstName
                        line.appendCsvFn(""); //WifeFatherDeath
                        line.appendCsvFn(""); //WifeFatherComment
                        line.appendCsvFn(""); //WifeMotherLastName
                        line.appendCsvFn(""); //WifeMotherFirstName
                        line.appendCsvFn(""); //WifeMotherDeath
                        line.appendCsvFn(""); //WifeMotherComment

                        // héritier
                        String heirName = appendLabelValue(
                                "",  // label
                                record.getFieldValue(FieldType.wifeFirstName),
                                record.getFieldValue(FieldType.wifeLastName)
                                );

                        String heirAge = formatAgeToComment(record.getFieldValue(FieldType.wifeAge));
                        String heirBirth = appendLabelValue(
                                "né le ",
                                record.getFieldValue(FieldType.wifeBirthDate),
                                record.getFieldValue(FieldType.wifeBirthPlace)
                                );

                        String heirFather = appendLabelValue(
                                "Père de l'héritier:" ,
                                record.getFieldValue(FieldType.wifeFatherFirstName),
                                record.getFieldValue(FieldType.wifeFatherLastName),
                                formatAgeToComment(record.getFieldValue(FieldType.wifeFatherAge)),
                                record.getFieldString(FieldType.wifeFatherDead),
                                record.getFieldValue(FieldType.wifeFatherOccupation),
                                record.getFieldValue(FieldType.wifeFatherResidence),
                                record.getFieldValue(FieldType.wifeFatherComment)
                                );

                        String heirMother = appendLabelValue(
                                "Mère de l'héritier:" ,
                                record.getFieldValue(FieldType.wifeMotherFirstName),
                                record.getFieldValue(FieldType.wifeMotherLastName),
                                formatAgeToComment(record.getFieldValue(FieldType.wifeMotherAge)),
                                record.getFieldString(FieldType.wifeMotherDead),
                                record.getFieldValue(FieldType.wifeMotherOccupation),
                                record.getFieldValue(FieldType.wifeMotherResidence),
                                record.getFieldValue(FieldType.wifeMotherComment)
                                );

                        String heirMarried = appendLabelValue(
                                "Conjoint de l'héritier:",
                                record.getFieldValue(FieldType.wifeMarriedFirstName),
                                record.getFieldValue(FieldType.wifeMarriedLastName),
                                record.getFieldString(FieldType.wifeMarriedDead),
                                record.getFieldValue(FieldType.wifeMarriedOccupation),
                                record.getFieldValue(FieldType.wifeMarriedResidence),
                                record.getFieldValue(FieldType.wifeMotherComment)
                                );

                        line.appendCsvFn(  // heirComment
                            heirName,
                            heirAge,
                            heirBirth,
                            record.getFieldValue(FieldType.wifeComment),
                            record.getFieldValue(FieldType.wifeOccupation),
                            record.getFieldValue(FieldType.wifeResidence),
                            heirFather,
                            heirMother,
                            heirMarried
                         );

                    } else  if (record instanceof RecordMisc ) {
                        // autre evenement
                        line.appendCsvFn(record.getFieldValue(FieldType.indiMarriedLastName)); //WifeLastName
                        line.appendCsvFn(record.getFieldValue(FieldType.indiMarriedFirstName)); //WifeFirstName
                        line.appendCsvFn(record.getFieldValue(FieldType.indiMarriedDead));   //wifeDead
                        line.appendCsvFn(""); // WifeAge
                        line.appendCsvFn(record.getFieldValue(FieldType.indiMarriedResidence)); //WifeResidence
                        line.appendCsvFn( //WifeComment
                            record.getFieldValue(FieldType.indiMarriedOccupation),
                            record.getFieldValue(FieldType.indiMarriedComment)
                            );

                        line.appendCsvFn(""); //WifeFatherFirstName
                        line.appendCsvFn(""); //WifeFatherDeath
                        line.appendCsvFn(""); //WifeFatherComment
                        line.appendCsvFn(""); //WifeMotherLastName
                        line.appendCsvFn(""); //WifeMotherFirstName
                        line.appendCsvFn(""); //WifeMotherDeath
                        line.appendCsvFn(""); //WifeMotherComment

                        // Participant 2
                        String participantName = appendLabelValue(
                                "Autre intervenant:",  // label
                                record.getFieldValue(FieldType.wifeFirstName),
                                record.getFieldValue(FieldType.wifeLastName)
                                );

                        String participantAge = formatAgeToComment(record.getFieldValue(FieldType.wifeAge));
                        String participantBirth = appendLabelValue(
                                "né le",
                                record.getFieldValue(FieldType.wifeBirthDate),
                                record.getFieldValue(FieldType.wifeBirthPlace)
                                );

                        String participantFather = appendLabelValue(
                                "Père de l'intervenant:" ,
                                record.getFieldValue(FieldType.wifeFatherFirstName),
                                record.getFieldValue(FieldType.wifeFatherLastName),
                                formatAgeToComment(record.getFieldValue(FieldType.wifeFatherAge)),
                                record.getFieldString(FieldType.wifeFatherDead),
                                record.getFieldValue(FieldType.wifeFatherOccupation),
                                record.getFieldValue(FieldType.wifeFatherResidence),
                                record.getFieldValue(FieldType.wifeFatherComment)
                                );

                        String participantMother = appendLabelValue(
                                "Mère de l'intervenant:" ,
                                record.getFieldValue(FieldType.wifeMotherFirstName),
                                record.getFieldValue(FieldType.wifeMotherLastName),
                                formatAgeToComment(record.getFieldValue(FieldType.wifeMotherAge)),
                                record.getFieldString(FieldType.wifeMotherDead),
                                record.getFieldValue(FieldType.wifeMotherOccupation),
                                record.getFieldValue(FieldType.wifeMotherResidence),
                                record.getFieldValue(FieldType.wifeMotherComment)
                                );

                        String participantMarried = appendLabelValue(
                                "Conjoint de l'intervenant:",
                                record.getFieldValue(FieldType.wifeMarriedFirstName),
                                record.getFieldValue(FieldType.wifeMarriedLastName),
                                record.getFieldString(FieldType.wifeMarriedDead),
                                record.getFieldValue(FieldType.wifeMarriedOccupation),
                                record.getFieldValue(FieldType.wifeMarriedResidence),
                                record.getFieldValue(FieldType.wifeMotherComment)
                                );

                        otherParticipant = appendValue(
                            participantName,
                            participantAge,
                            participantBirth,
                            record.getFieldValue(FieldType.wifeComment),
                            record.getFieldValue(FieldType.wifeOccupation),
                            record.getFieldValue(FieldType.wifeResidence),
                            participantFather,
                            participantMother,
                            participantMarried
                         );

                        line.appendCsvFn(""); // heirComment

                    } else  if (record instanceof RecordDeath ) {
                        line.appendCsvFn(record.getFieldValue(FieldType.indiMarriedLastName)); //WifeLastName
                        line.appendCsvFn(record.getFieldValue(FieldType.indiMarriedFirstName)); //WifeFirstName
                        line.appendCsvFn(record.getFieldString(FieldType.indiMarriedDead)); //wifeDead
                        line.appendCsvFn(""); // Wifeage
                        line.appendCsvFn(""); // WifePlace
                        line.appendCsvFn(record.getFieldValue(FieldType.indiMarriedComment)); // WifeComment

                        line.appendCsvFn(""); //WifeFatherFirstName
                        line.appendCsvFn(""); //WifeFatherDeath
                        line.appendCsvFn(""); //WifeFatherComment
                        line.appendCsvFn(""); //WifeMotherLastName
                        line.appendCsvFn(""); //WifeMotherFirstName
                        line.appendCsvFn(""); //WifeMotherDeath
                        line.appendCsvFn(""); //WifeMotherComment

                        line.appendCsvFn(""); // heirComment

                     } else {
                        line.appendCsvFn(""); //WifeLastName
                        line.appendCsvFn(""); //WifeFirstName
                        line.appendCsvFn(""); //WifeDead
                        line.appendCsvFn(""); //WifeAge
                        line.appendCsvFn(""); //WifeResidence
                        line.appendCsvFn(""); //WifeComment

                        line.appendCsvFn(""); //WifeFatherFirstName
                        line.appendCsvFn(""); //WifeFatherDeath
                        line.appendCsvFn(""); //WifeFatherComment
                        line.appendCsvFn(""); //WifeMotherLastName
                        line.appendCsvFn(""); //WifeMotherFirstName
                        line.appendCsvFn(""); //WifeMotherDeath
                        line.appendCsvFn(""); //WifeMotherComment

                        line.appendCsvFn(""); // heirComment
                     }

                    line.appendCsvFn(record.getFieldValue(FieldType.witness1LastName));
                    line.appendCsvFn(record.getFieldValue(FieldType.witness1FirstName));
                    line.appendCsvFn(record.getFieldValue(FieldType.witness1Occupation), record.getFieldValue(FieldType.witness1Comment));
                    line.appendCsvFn(record.getFieldValue(FieldType.witness2LastName));
                    line.appendCsvFn(record.getFieldValue(FieldType.witness2FirstName));
                    line.appendCsvFn(record.getFieldValue(FieldType.witness2Occupation), record.getFieldValue(FieldType.witness2Comment));

                    String insinuation = "";
                    if( !record.isEmptyField(FieldType.secondDate)) {
                        insinuation = "insinué le "+ record.getFieldValue(FieldType.secondDate);
                    }

                    line.appendCsv(
                        record.getFieldValue(FieldType.generalComment),
                        otherParticipant,
                        appendLabelValue(
                            "témoin(s):",
                            record.getFieldValue(FieldType.witness3FirstName),
                            record.getFieldValue(FieldType.witness3LastName),
                            record.getFieldValue(FieldType.witness3Occupation),
                            record.getFieldValue(FieldType.witness3Comment)
                            ),
                        appendLabelValue("",
                            record.getFieldValue(FieldType.witness4FirstName),
                            record.getFieldValue(FieldType.witness4LastName),
                            record.getFieldValue(FieldType.witness4Occupation),
                            record.getFieldValue(FieldType.witness4Comment)
                            ),
                        insinuation
                    );
                    

                    line.appendCsv("\r\n");
                    writer.write(line.toString());

                } catch (IOException e) {
                    sb.append(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.LineNo"), index ));
                    sb.append("\n");
                    sb.append(line).append("\n   ");
                    sb.append(e.getMessage()).append("\n");
                }
            }
            writer.close();

        } catch (IOException e) {
            System.out.println("Exception while reading file: " + e);
        }
        return sb;
    }

    /**
     * Convertit l'age normalisé "9y 9m 9d"
     * en une expression française "9a 9m 9d"
     * @param value
     * @return 
     */
    static private String formatAgeToComment(String value) {
        String ageString;
        if (value.equals("0d")) {
            ageString = "";
        } else {
            ageString = "Age:"+value.replace('y', 'a').replace('d', 'j');
        }
        return ageString;
    }
    
    /**
     * Convertit l'age normalisé "9y 9m 9d"
     * en une expression française "9a 9m 9d"
     * @param agedField
     * @return 
     */
    static private String formatAgeToAge(String value) {
        String ageString;
        if (value != null) {
            if (value.equals("0d")) {
                ageString = "";
            } else {
                ageString = value.replace('y', 'a').replace('d', 'j');
            }
        } else {
            ageString = "";
        }
        return ageString;
    }
    
    static private String getFatherLastName( String[] fields ) {
        String fatherLastName ;
        if( fields[EgmtField.indiFatherFirstName.ordinal()].isEmpty()) {
            fatherLastName = "";
        } else {
            fatherLastName = fields[EgmtField.indiLastName.ordinal()];
        }
                
        return fatherLastName;
        
    }

    /**
     * concatene plusieurs commentaires dans une chaine , séparés par une virgule
     */
    static private String appendValue(String value, String... otherValues) {
        int fieldSize = value.length();
        StringBuilder sb = new StringBuilder();
        sb.append(value.trim());
        for (String otherValue : otherValues) {
            // j'ajoute les valeurs supplémentaires séparées par des virgules
            if (!otherValue.trim().isEmpty()) {
                // je concantene les valeurs en inserant une virgule dans
                // si la valeur précedente n'est pas vide
                if (fieldSize > 0) {
                    sb.append(", ");
                }
                sb.append(otherValue.trim());
                fieldSize += otherValue.length();
            }
        }

        return sb.toString();
    }

     /**
     * concatene plusieurs commentaires dans une chaine , séparés par un espace
     * Le lobellé est ajouté au debut si les valeurs ne sont pas vides .
     */
    static private String appendLabelValue(String label, String... otherValues) {
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
            if (!label.trim().isEmpty()) {
                sb.insert(0, label.trim() + " ");
            }
        }

        return sb.toString();
    }
    
    
    /**
     * Convertit une chaine de caractere contenant l'age 
     * en une chaine normalisée "9y 9m 9d"
     * @param ageString
     * @return 
     */
    static Pattern agePattern = Pattern.compile( "([0-9]*)" );
    static protected String formatAgeToField(String ageString) {
        String ageField;
        ageString = ageString.trim();
        ageString = ageString.replace(" ", "").replace("ans", "y").replace("an", "y").replace("mois", "m").replace("jours", "d").replace("jour", "d");
        Matcher m = agePattern.matcher(ageString);
        if( m.matches() ) {
            // je considere qu'un nombre seul (sans unité) est un nombre d'années
            ageField = ageString.concat("y");
        } else {
            ageField = ageString.replace('a', 'y').replace('j', 'd');        
        }
        return ageField;
    }
    
    static protected String formatDate(String strDay, String strMonth, String strYear) {
        try {
            int day=0;
            int month=0;
            int year=0;
            if (!strDay.isEmpty()) {
                day = Integer.parseInt(strDay);
            }
            if (!strMonth.isEmpty()) {
                month = Integer.parseInt(strMonth);
            }
            if (!strYear.isEmpty()) {
                year = Integer.parseInt(strYear);
            }
            return String.format("%02d/%02d/%04d", day, month, year);
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("Error "+strDay+ " " + strMonth+ " " + strYear+ " " + ex);
        }

    }
    
    /**
     * retourn la ligne d'entete
     * @return 
     */
    static private Line getHeader() {
        Line line = new Line(fieldSeparator);
        line.appendCsvFn("Evenement");
        line.appendCsvFn("Departement");
        line.appendCsvFn("Ville");
        line.appendCsvFn("Paroisse");
        line.appendCsvFn("Notaire");
        line.appendCsvFn("Cote");
        line.appendCsvFn("Photo");
        line.appendCsvFn("Jour");
        line.appendCsvFn("Mois"); 
        line.appendCsvFn("Annee");
        
        line.appendCsvFn("Nom");
        line.appendCsvFn("Prenom");
        line.appendCsvFn("Sexe");
        line.appendCsvFn("Age"); 
        line.appendCsvFn("Lieu"); 
        line.appendCsvFn("Infos");
        
        line.appendCsvFn("Prenom pere"); 
        line.appendCsvFn("Pere decede"); 
        line.appendCsvFn("Info pere");

        line.appendCsvFn("Nom mere"); 
        line.appendCsvFn("Prenom mere"); 
        line.appendCsvFn("Mere decede");
        line.appendCsvFn("Info mere");
        
        line.appendCsvFn("Nom conjoint");
        line.appendCsvFn("Prenom conjoint");
        line.appendCsvFn("Deces conjoint");
        line.appendCsvFn("Age conjoint");
        line.appendCsvFn("Lieu conjoint");
        line.appendCsvFn("Info conjoint");
        
        line.appendCsvFn("Prenom pere conjoint");
        line.appendCsvFn("Deces pere conjoint");
        line.appendCsvFn("Info pere conjoint");
        
        line.appendCsvFn("Nom mere conjoint");
        line.appendCsvFn("Prenom mere conjoint");
        line.appendCsvFn("Deces mere conjoint");
        line.appendCsvFn("Info mere conjoint");
        
        line.appendCsvFn("Heritiers");
        line.appendCsvFn("Nom parrain");
        line.appendCsvFn("Prenom parrain");
        line.appendCsvFn("Commentaire parrain");
        line.appendCsvFn("Nom marrraine");
        line.appendCsvFn("Prenom marrraine");
        line.appendCsvFn("Infos marrraine");
        line.appendCsv("Infos diverses");

        line.appendCsv("\r\n");
        return line;
    }
   
}
