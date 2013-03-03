package ancestris.modules.releve.file;

import ancestris.modules.releve.file.FileManager.Line;
import ancestris.modules.releve.model.*;
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
                            record.setCote(fields[EgmtField.cote.ordinal()]);
                            record.setParish(fields[EgmtField.paroisse.ordinal()]);
                            record.setFreeComment(fields[EgmtField.folio.ordinal()]);

                            record.setEventDate(
                                    fields[EgmtField.day.ordinal()],
                                    fields[EgmtField.month.ordinal()],
                                    fields[EgmtField.year.ordinal()] );

                            record.setIndi(
                                    fields[EgmtField.indiFirstName.ordinal()],
                                    fields[EgmtField.indiLastName.ordinal()],
                                    fields[EgmtField.indiSex.ordinal()],
                                    "", // pas d'age a la naissance
                                    "", // pas de date de naissance 
                                    fields[EgmtField.indiPlace.ordinal()],
                                    "", // pas de profession a la naissance
                                    "", // pas de residence dans ce format                                
                                    fields[EgmtField.indiComment.ordinal()]);
                            record.setIndiFather(
                                    fields[EgmtField.indiFatherFirstName.ordinal()],
                                    getFatherLastName(fields), // meme nom que Indi
                                    "", // profession
                                    "", // pas de residence dans ce format                                
                                    fields[EgmtField.indiFatherComment.ordinal()],
                                    fields[EgmtField.indiFatherDead.ordinal()],
                                    "");  //age

                            record.setIndiMother(
                                    fields[EgmtField.indiMotherFirstName.ordinal()],
                                    fields[EgmtField.indiMotherLastName.ordinal()],
                                    "", //profession
                                    "", // pas de residence dans ce format                                
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
                                record.setGeneralComment(
                                   fields[EgmtField.generalComment.ordinal()]
                                   + " " +
                                   fields[EgmtField.heirComment.ordinal()] );
                            } else {
                                record.setGeneralComment(fields[EgmtField.generalComment.ordinal()]);
                            }
                            record.recordNo = lineNumber;
                            fileBuffer.addRecord(record);

                        } else if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("mariage")) {
                            RecordMarriage record = new RecordMarriage();
                            record.setCote(fields[EgmtField.cote.ordinal()]);
                            record.setParish(fields[EgmtField.paroisse.ordinal()]);
                            record.setFreeComment(fields[EgmtField.folio.ordinal()]);

                            record.setEventDate(
                                    fields[EgmtField.day.ordinal()],
                                    fields[EgmtField.month.ordinal()],
                                    fields[EgmtField.year.ordinal()] );

                            record.setIndi(
                                    fields[EgmtField.indiFirstName.ordinal()],
                                    fields[EgmtField.indiLastName.ordinal()],
                                    fields[EgmtField.indiSex.ordinal()],
                                    formatAgeToField(fields[EgmtField.indiAge.ordinal()]),
                                    "", // pas de date de naissance a la naissance
                                    "", // pas de lieu de naissance dans ce format
                                    "", // pas de profession dans ce format
                                    fields[EgmtField.indiPlace.ordinal()],
                                    fields[EgmtField.indiComment.ordinal()]);

                            record.setIndiFather(
                                    fields[EgmtField.indiFatherFirstName.ordinal()],
                                    getFatherLastName(fields), // meme nom que Indi
                                    "", // profession
                                    "", // pas de residence dans ce format                                
                                    fields[EgmtField.indiFatherComment.ordinal()],
                                    fields[EgmtField.indiFatherDead.ordinal()],
                                    "");  //age

                            record.setIndiMother(
                                    fields[EgmtField.indiMotherFirstName.ordinal()],
                                    fields[EgmtField.indiMotherLastName.ordinal()],
                                    "", //profession
                                    "", // pas de residence dans ce format                                
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
                                    "" , // pas de profession dans ce format
                                    fields[EgmtField.wifePlace.ordinal()],
                                    fields[EgmtField.wifeComment.ordinal()]);
                            //TODO : deces de l'epouse est ignoré , fields[EgmtField.wifeDead.ordinal()]
                                
                             record.setWifeFather(
                                    fields[EgmtField.wifeFatherFirstName.ordinal()],
                                    fields[EgmtField.wifeLastName.ordinal()], // meme nom que la femme
                                    "" , // profession
                                    "", // pas de residence dans ce format                                
                                    fields[EgmtField.wifeFatherComment.ordinal()],
                                    fields[EgmtField.wifeFatherDead.ordinal()],
                                    "");  //age

                                record.setWifeMother(
                                    fields[EgmtField.wifeMotherFirstName.ordinal()],
                                    fields[EgmtField.wifeMotherLastName.ordinal()],
                                    "" , // profession
                                    "", // pas de residence dans ce format                                
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
                                record.setGeneralComment(
                                   fields[EgmtField.generalComment.ordinal()]
                                   + " " +
                                   fields[EgmtField.heirComment.ordinal()] );
                            } else {
                                record.setGeneralComment(fields[EgmtField.generalComment.ordinal()]);
                            }
                            record.recordNo = lineNumber;
                            fileBuffer.addRecord(record);

                        } else if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("décès")) {
                            RecordDeath record = new RecordDeath();
                            record.setCote(fields[EgmtField.cote.ordinal()]);
                            record.setParish(fields[EgmtField.paroisse.ordinal()]);
                            record.setFreeComment(fields[EgmtField.folio.ordinal()]);

                            record.setEventDate(
                                    fields[EgmtField.day.ordinal()],
                                    fields[EgmtField.month.ordinal()],
                                    fields[EgmtField.year.ordinal()] );

                            record.setIndi(
                                    fields[EgmtField.indiFirstName.ordinal()],
                                    fields[EgmtField.indiLastName.ordinal()],
                                    fields[EgmtField.indiSex.ordinal()],
                                    formatAgeToField(fields[EgmtField.indiAge.ordinal()]),
                                    "", // date de naissance
                                    "", // pas de lieu de naissance dans ce format
                                    "", // pas de profession dans ce format
                                    fields[EgmtField.indiPlace.ordinal()],
                                    fields[EgmtField.indiComment.ordinal()]);

                             record.setIndiFather(
                                    fields[EgmtField.indiFatherFirstName.ordinal()],
                                    getFatherLastName(fields), // meme nom que Indi
                                    "", // profession
                                    "", // pas de residence dans ce format                                
                                    fields[EgmtField.indiFatherComment.ordinal()],
                                    fields[EgmtField.indiFatherDead.ordinal()] ,
                                    "");  //age

                            record.setIndiMother(
                                    fields[EgmtField.indiMotherFirstName.ordinal()],
                                    fields[EgmtField.indiMotherLastName.ordinal()],
                                    "", //profession
                                    "", // pas de residence dans ce format                                
                                    fields[EgmtField.indiMotherComment.ordinal()],
                                    fields[EgmtField.indiMotherDead.ordinal()],
                                    "");  //age

                            record.setIndiMarried(
                                    fields[EgmtField.wifeFirstName.ordinal()],
                                    fields[EgmtField.wifeLastName.ordinal()],
                                    "" , // profession
                                    "", // pas de residence dans ce format                                
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
                                record.setGeneralComment(
                                   fields[EgmtField.generalComment.ordinal()]
                                   + " " +
                                   fields[EgmtField.heirComment.ordinal()] );
                            } else {
                                record.setGeneralComment(fields[EgmtField.generalComment.ordinal()]);
                            }
                            record.recordNo = lineNumber;
                            fileBuffer.addRecord(record);

                        } else  {
                            RecordMisc record = new RecordMisc();
                            if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("contrat de mariage")) {
                                record.setEventType("MARC");
                            } else if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("testament")) {
                                record.setEventType("WILL");
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
                                record.setEventType(fields[EgmtField.typeActe.ordinal()]);
                            }

                            record.setParish(fields[EgmtField.paroisse.ordinal()]);
                            // le notaire est utilisé seelement pour les actes divers
                            record.setNotary(fields[EgmtField.notaire.ordinal()]);
                            record.setCote(fields[EgmtField.cote.ordinal()]);
                            record.setFreeComment(fields[EgmtField.folio.ordinal()]);

                            record.setEventDate(
                                    fields[EgmtField.day.ordinal()],
                                    fields[EgmtField.month.ordinal()],
                                    fields[EgmtField.year.ordinal()] );

                            record.setIndi(
                                    fields[EgmtField.indiFirstName.ordinal()],
                                    fields[EgmtField.indiLastName.ordinal()],
                                    fields[EgmtField.indiSex.ordinal()],
                                    formatAgeToField(fields[EgmtField.indiAge.ordinal()]),
                                    "", // date de naissance
                                    "", // pas de lieu de naissance dans ce format
                                    "", // pas de profession dans ce format
                                    fields[EgmtField.indiPlace.ordinal()],
                                    fields[EgmtField.indiComment.ordinal()]);
                            
                            record.setIndiFather(
                                    fields[EgmtField.indiFatherFirstName.ordinal()],
                                    getFatherLastName(fields), // meme nom que Indi
                                    "", // profession
                                    "", // pas de residence dans ce format                                
                                    fields[EgmtField.indiFatherComment.ordinal()],
                                    fields[EgmtField.indiFatherDead.ordinal()] ,
                                    "");  //age

                            record.setIndiMother(
                                    fields[EgmtField.indiMotherFirstName.ordinal()],
                                    fields[EgmtField.indiMotherLastName.ordinal()],
                                    "", //profession
                                    "", // pas de residence dans ce format                                
                                    fields[EgmtField.indiMotherComment.ordinal()],
                                    fields[EgmtField.indiMotherDead.ordinal()],
                                    "");  //age

                            record.setWife(
                                    fields[EgmtField.wifeFirstName.ordinal()],
                                    fields[EgmtField.wifeLastName.ordinal()],
                                    fields[EgmtField.indiSex.ordinal()].equals("M") ? "F" : "M",
                                    formatAgeToField(fields[EgmtField.wifeAge.ordinal()]),
                                    "", // date de naissance
                                    "", // pas de lei de naissance dans ce format
                                    "" , // profession
                                    fields[EgmtField.wifePlace.ordinal()],
                                    fields[EgmtField.wifeComment.ordinal()]);
                                
                            record.setWifeFather(
                                    fields[EgmtField.wifeFatherFirstName.ordinal()],
                                    fields[EgmtField.wifeLastName.ordinal()], // meme nom que la femme
                                    "" , // profession
                                    "", // pas de residence dans ce format                                
                                    fields[EgmtField.wifeFatherComment.ordinal()],
                                    fields[EgmtField.wifeFatherDead.ordinal()] ,
                                    "");  //age

                            record.setWifeMother(
                                    fields[EgmtField.wifeMotherFirstName.ordinal()],
                                    fields[EgmtField.wifeMotherLastName.ordinal()],
                                    "" , // profession
                                    "", // pas de residence dans ce format                                
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
                                record.setGeneralComment(
                                   fields[EgmtField.generalComment.ordinal()]
                                   + " " +
                                   fields[EgmtField.heirComment.ordinal()] );
                            } else {
                                record.setGeneralComment(fields[EgmtField.generalComment.ordinal()]);
                            }
                            record.recordNo = lineNumber;
                            fileBuffer.addRecord(record);

                        }

                    } else {
                        fileBuffer.append(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.LineNo"), lineNumber ));
                        fileBuffer.append("\n");
                        fileBuffer.append(strLine).append("\n   ");
                        fileBuffer.append(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.InvalidLine"));
                    }
                } catch (Exception e) {
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
    public static StringBuilder saveFile(PlaceManager placeManager, ModelAbstract recordModel, File fileName, boolean append) {
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
                
                Line line = new Line(fieldSeparator);
                final Record record = recordModel.getRecord(index);
                try {
                    if ( record instanceof RecordBirth ) {
                       line.appendCsvFn("naissance");
                    } else if (record instanceof RecordMarriage) {
                        line.appendCsvFn("mariage");
                    } else if (record instanceof RecordDeath) {
                        line.appendCsvFn("décès");
                    } else {
                        line.appendCsvFn(record.getEventType().getName());
                    }
                    line.appendCsvFn(placeManager.getCountyName());
                    line.appendCsvFn(placeManager.getCityName());
                    line.appendCsvFn(record.getParish().toString());
                    if ( record instanceof RecordMisc ) {
                        line.appendCsvFn(record.getNotary().toString());
                    } else {
                        line.appendCsvFn("");
                    }
                    line.appendCsvFn(record.getCote().toString());
                    line.appendCsvFn(record.getFreeComment().toString());
                    line.appendCsvFn(String.format("%02d", record.getEventDateProperty().getStart().getDay()+1));
                    line.appendCsvFn(String.format("%02d", record.getEventDateProperty().getStart().getMonth()+1));
                    line.appendCsvFn(String.format("%4d", record.getEventDateProperty().getStart().getYear()));

                    line.appendCsvFn(record.getIndiLastName().getValue());
                    line.appendCsvFn(record.getIndiFirstName().getValue());
                    line.appendCsvFn(record.getIndiSex().toString());
                    line.appendCsvFn(formatAgeToAge(record.getIndiAge()));
                    if ( record instanceof RecordBirth) {
                        line.appendCsvFn(record.getIndiBirthPlace().toString());
                    } else {
                        line.appendCsvFn(record.getIndiResidence().toString());
                    }
                    
                    if ( record instanceof RecordBirth) {
                        line.appendCsvFn(record.getIndiComment().toString());
                    } else {
                        String birthDate = "";
                        if ( ! record.getIndiBirthDate().isEmpty()){
                            birthDate = "né le " + record.getIndiBirthDate();
                        }
                        String marriedName = "";
                        if ( ! record.getIndiMarriedLastName().toString().isEmpty()){
                            marriedName = "conjoint: " + record.getIndiMarriedLastName() + " "
                                    + record.getIndiMarriedFirstName() +" "
                                    + record.getIndiMarriedComment();
                        }
                        line.appendCsvFn(
                            record.getIndiComment().toString(),
                            birthDate,
                            record.getIndiBirthPlace().toString(),
                            record.getIndiOccupation().toString(),
                            marriedName,
                            record.getIndiMarriedOccupation().toString(),
                            record.getIndiMarriedResidence().toString(),
                            record.getIndiMarriedComment().toString()
                            );
                    }
                    
                    line.appendCsvFn(record.getIndiFatherFirstName().toString());
                    line.appendCsvFn(record.getIndiFatherDead().toString());
                    line.appendCsvFn(record.getIndiFatherComment().toString(),
                            record.getIndiFatherOccupation().toString(),
                            record.getIndiFatherResidence() != null ? record.getIndiFatherResidence().toString() : "",
                            formatAgeToComment(record.getIndiFatherAge()));
                    line.appendCsvFn(record.getIndiMotherLastName().toString());
                    line.appendCsvFn(record.getIndiMotherFirstName().toString());
                    line.appendCsvFn(record.getIndiMotherDead().toString());
                    line.appendCsvFn(record.getIndiMotherComment().toString(),
                            record.getIndiMotherOccupation().toString(),
                            record.getIndiMotherResidence() != null ? record.getIndiMotherResidence().toString() : "",
                            formatAgeToComment(record.getIndiMotherAge()));
                    
                    if ((record instanceof RecordMarriage) || (record instanceof RecordMisc)) {
                        line.appendCsvFn(record.getWifeLastName().toString());
                        line.appendCsvFn(record.getWifeFirstName().toString());
                        line.appendCsvFn(""); //wifeDead
                        line.appendCsvFn(formatAgeToAge(record.getWifeAge()));
                        line.appendCsvFn(record.getWifeResidence().toString());
                        
                        String birthDate = "";
                        if (!record.getWifeBirthDate().isEmpty() ) {
                            birthDate = "né le "+record.getWifeBirthDate().toString();
                        }
                        String marriedName = "";
                        if ( ! record.getWifeMarriedLastName().toString().isEmpty()){
                            marriedName = "conjoint: " + record.getWifeMarriedLastName()+ " "
                                    + record.getWifeMarriedFirstName() +" "
                                    + record.getWifeMarriedComment();
                        }
                        line.appendCsvFn(
                            record.getWifeComment().toString(),
                            birthDate,
                            record.getWifeBirthPlace().toString(),
                            record.getWifeOccupation().toString(),
                            marriedName,
                            record.getWifeMarriedOccupation().toString(),
                            record.getWifeMarriedResidence().toString(),
                            record.getWifeMarriedComment().toString()
                            );

                        line.appendCsvFn(record.getWifeFatherFirstName().toString());
                        line.appendCsvFn(record.getWifeFatherDead().getValue());
                        line.appendCsvFn(record.getWifeFatherComment().toString(), 
                            record.getWifeFatherOccupation().toString(),
                            record.getWifeFatherResidence().toString(),
                            formatAgeToComment(record.getWifeFatherAge()));
                        line.appendCsvFn(record.getWifeMotherLastName().toString());
                        line.appendCsvFn(record.getWifeMotherFirstName().toString());
                        line.appendCsvFn(record.getWifeMotherDead().toString());
                        line.appendCsvFn(record.getWifeMotherComment().toString(), 
                            record.getWifeMotherOccupation().toString(),
                            record.getWifeMotherResidence().toString(),
                            formatAgeToComment(record.getWifeMotherAge()));

                    } else  if (record instanceof RecordDeath ) {
                        line.appendCsvFn(record.getIndiMarriedLastName().toString());
                        line.appendCsvFn(record.getIndiMarriedFirstName().toString());
                        line.appendCsvFn(record.getIndiMarriedDead().toString()); //wifeDead
                        line.appendCsvFn(""); // age
                        line.appendCsvFn(""); // birth place
                        line.appendCsvFn(record.getIndiMarriedComment().toString());

                        line.appendCsvFn("");
                        line.appendCsvFn("");
                        line.appendCsvFn("");
                        line.appendCsvFn("");
                        line.appendCsvFn("");
                        line.appendCsvFn("");
                        line.appendCsvFn("");

                     } else {
                        line.appendCsvFn("");
                        line.appendCsvFn("");
                        line.appendCsvFn("");
                        line.appendCsvFn("");
                        line.appendCsvFn("");
                        line.appendCsvFn("");

                        line.appendCsvFn("");
                        line.appendCsvFn("");
                        line.appendCsvFn("");
                        line.appendCsvFn("");
                        line.appendCsvFn("");
                        line.appendCsvFn("");
                        line.appendCsvFn("");
                     }

                    line.appendCsvFn(""); // heirComment

                    line.appendCsvFn(record.getWitness1LastName().toString());
                    line.appendCsvFn(record.getWitness1FirstName().toString());
                    line.appendCsvFn(record.getWitness1Comment().toString(),
                        record.getWitness1Occupation().toString() );
                    line.appendCsvFn(record.getWitness2LastName().toString());
                    line.appendCsvFn(record.getWitness2FirstName().toString());
                    line.appendCsvFn(record.getWitness2Comment().toString(),
                         record.getWitness2Occupation().toString() );
                    if( record.getWitness3LastName().isEmpty() && record.getWitness4LastName().isEmpty()) {
                       line.appendCsv(record.getGeneralComment().toString());
                    } else {
                        line.appendCsv(record.getGeneralComment().toString(),
                           "témoin: " + record.getWitness3FirstName().toString() + " "+ record.getWitness3LastName().toString(),
                           record.getWitness3Occupation().toString(),
                           record.getWitness3Comment().toString(),
                           record.getWitness4FirstName().toString() + " "+ record.getWitness4LastName().toString(),
                           record.getWitness4Occupation().toString(),
                           record.getWitness4Comment().toString()
                           );
                    }

                    line.appendCsv("\r\n");
                    writer.write(line.toString());

                } catch (Exception e) {
                    sb.append(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.LineNo"), index ));
                    sb.append("\n");
                    sb.append(line).append("\n   ");
                    sb.append(e.getMessage()).append("\n");
                }
            }
            writer.close();

        } catch (Exception e) {
            System.out.println("Exception while reading file: " + e);
        }
        return sb;
    }

    /**
     * Convertit l'age normalisé "9y 9m 9d"
     * en une expression française "9a 9m 9d"
     * @param agedField
     * @return 
     */
    static private String formatAgeToComment(FieldAge agedField) {
        String ageString;
        if (agedField.getValue().equals("0d")) {
            ageString = "";
        } else {
            ageString = "Age:"+agedField.getValue().replace('y', 'a').replace('d', 'j');
        }
        return ageString;
    }
    
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
     * Convertit une chaine de caractere contenant l'age 
     * en une chaine normalisée "9y 9m 9d"
     * @param ageString
     * @return 
     */
    static Pattern agePattern = Pattern.compile( "([0-9]*)" );
    static private String formatAgeToField(String ageString) {
        String ageField;
        ageString = ageString.trim();
        Matcher m = agePattern.matcher(ageString);
        if( m.matches() ) {
            // je considere qu'un nombre seul (sans unité) est un nombre d'années
            ageField = ageString.concat("y");
        } else {
            ageField = ageString.replace('a', 'y').replace('j', 'd');        
        }
        return ageField;
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

        line.appendCsv("\n");
        return line;
    }
   
}
