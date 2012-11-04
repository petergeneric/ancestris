package ancestris.modules.releve.file;

import ancestris.modules.releve.model.PlaceManager;
import ancestris.modules.releve.model.ModelAbstract;
import ancestris.modules.releve.model.RecordMisc;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordMarriage;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.file.FileManager.Line;
import ancestris.modules.releve.model.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

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
            br = new BufferedReader( new InputStreamReader(new FileInputStream(inputFile),"UTF-8"));
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
        indiLastName, indiFirstName, indiSex, indiAge, indiBirthPlace, indiComment,
        indiFatherFirstName, indiFatherDead, indiFatherComment,
        indiMotherLastName, indiMotherFirstName, indiMotherDead, indiMotherComment,
        wifeLastName, wifeFirstName, wifeDead, wifeAge, wifeBirthPlace, wifeComment,
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
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),"UTF-8"));
            String strLine = "";
            int lineNumber = 0;

            String[] fields ;
            //read comma separated file line by line
            while ((fields = splitLine(br)) != null) {
                lineNumber++;
                try {
                    if (fields != null) {
                        if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("naissance")) {
                            RecordBirth record = new RecordBirth();
                            record.setEventPlace(
                                    fields[EgmtField.nomCommune.ordinal()],
                                    "", // codecommune
                                    fields[EgmtField.codeDepartement.ordinal()],
                                    "", // stateName
                                    "" ); // countryName
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
                                    "", // pas de lieu a la naissance
                                    "", // pas de profession a la naissance
                                    "", // pas de residence dans ce format                                
                                    fields[EgmtField.indiComment.ordinal()]);
                            record.setIndiFather(
                                    fields[EgmtField.indiFatherFirstName.ordinal()],
                                    fields[EgmtField.indiLastName.ordinal()], // meme nom que Indi
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
                            fileBuffer.loadRecord(record);

                        } else if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("mariage")) {
                            RecordMarriage record = new RecordMarriage();
                            record.setEventPlace(
                                    fields[EgmtField.nomCommune.ordinal()],
                                    "", // codecommune
                                    fields[EgmtField.codeDepartement.ordinal()],
                                    "", // stateName
                                    "" ); // countryName
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
                                    fields[EgmtField.indiAge.ordinal()],
                                    "", // pas de date de naissance a la naissance
                                    fields[EgmtField.indiBirthPlace.ordinal()],
                                    "", // pas de profession dans ce format 
                                    "", // pas de residence dans ce format                                
                                    fields[EgmtField.indiComment.ordinal()]);

                            record.setIndiFather(
                                    fields[EgmtField.indiFatherFirstName.ordinal()],
                                    fields[EgmtField.indiLastName.ordinal()], // meme nom que Indi
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
                                    fields[EgmtField.wifeAge.ordinal()],
                                    "" , // date de naissance
                                    fields[EgmtField.wifeBirthPlace.ordinal()],
                                    "" , // pas de profession dans ce format 
                                    "", // pas de residence dans ce format                                
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
                            fileBuffer.loadRecord(record);

                        } else if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("décès")) {
                            RecordDeath record = new RecordDeath();
                            record.setEventPlace(
                                    fields[EgmtField.nomCommune.ordinal()],
                                    "", // codecommune
                                    fields[EgmtField.codeDepartement.ordinal()],
                                    "", // stateName
                                    "" ); // countryName
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
                                    fields[EgmtField.indiAge.ordinal()],
                                    "", // date de naissance
                                    fields[EgmtField.indiBirthPlace.ordinal()],
                                    "", // pas de profession dans ce format
                                    "", // pas de residence dans ce format                                
                                    fields[EgmtField.indiComment.ordinal()]);

                             record.setIndiFather(
                                    fields[EgmtField.indiFatherFirstName.ordinal()],
                                    fields[EgmtField.indiLastName.ordinal()], // meme nom que Indi
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
                            fileBuffer.loadRecord(record);

                        } else  {
                            RecordMisc record = new RecordMisc();
                            if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("contrat de mariage")) {
                                record.setEventType("MARC");
                            } else if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("testament")) {
                                record.setEventType("WILL");
                            } else if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("evenement")) {
                                // j'ignore la ligne d'entete
                                continue;
                            } else {
                                record.setEventType(fields[EgmtField.typeActe.ordinal()]);
                            }


                            record.setEventPlace(
                                    fields[EgmtField.nomCommune.ordinal()],
                                    "", // codecommune
                                    fields[EgmtField.codeDepartement.ordinal()],
                                    "", // stateName
                                    "" ); // countryName

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
                                    fields[EgmtField.indiAge.ordinal()],
                                    "", // date de naissance
                                    fields[EgmtField.indiBirthPlace.ordinal()],
                                    "", // pas de profession dans ce format
                                    "", // pas de residence dans ce format                                
                                    fields[EgmtField.indiComment.ordinal()]);
                            
                            record.setIndiFather(
                                    fields[EgmtField.indiFatherFirstName.ordinal()],
                                    fields[EgmtField.indiLastName.ordinal()], // meme nom que Indi
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
                                    fields[EgmtField.wifeAge.ordinal()],
                                    "", // date de naissance
                                    fields[EgmtField.wifeBirthPlace.ordinal()],
                                    "" , // profession
                                    "", // pas de residence dans ce format                                
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
                            fileBuffer.loadRecord(record);

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
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileName, append), "UTF-8") ;
            
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
                    line.appendCsvFn(placeManager.getCountryName());
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
                    if (!(record instanceof RecordBirth)) {
                        
                        line.appendCsvFn(formatAge(record.getIndiAge()));
                        line.appendCsvFn(record.getIndiPlace().toString());
                    } else {
                        line.appendCsvFn("");
                        line.appendCsvFn("");
                    }

                    if ( record instanceof RecordBirth) {
                        line.appendCsvFn(record.getIndiComment().toString());
                    } else {
                        String birthDate = "";
                        if (!record.getIndiBirthDate().toString().isEmpty() ) {
                            birthDate = "né le "+record.getIndiBirthDate().toString();
                        }
                        String marriedName = "";
                        if ( ! record.getIndiMarriedLastName().toString().isEmpty()){
                            marriedName = "conjoint: " + record.getIndiMarriedLastName()
                                    + record.getIndiMarriedFirstName() +" "
                                    + record.getIndiMarriedComment();
                        }
                        line.appendCsvFn(record.getIndiComment().toString(),
                            record.getIndiOccupation().toString(),
                            record.getIndiResidence().toString(),
                            birthDate.toString(),
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
                            record.getIndiFatherResidence().toString(),
                            formatAge(record.getIndiFatherAge()));
                    line.appendCsvFn(record.getIndiMotherLastName().toString());
                    line.appendCsvFn(record.getIndiMotherFirstName().toString());
                    line.appendCsvFn(record.getIndiMotherDead().toString());
                    line.appendCsvFn(record.getIndiMotherComment().toString(),
                            record.getIndiMotherOccupation().toString(),
                            record.getIndiMotherResidence().toString(),
                            formatAge(record.getIndiMotherAge()));
                    
                    if ((record instanceof RecordMarriage) || (record instanceof RecordMisc)) {
                        line.appendCsvFn(record.getWifeLastName().toString());
                        line.appendCsvFn(record.getWifeFirstName().toString());
                        line.appendCsvFn(""); //wifeDead
                        line.appendCsvFn(formatAge(record.getWifeAge()));
                        line.appendCsvFn(record.getWifePlace().toString());
                        
                        String birthDate = "";
                        if (!record.getWifeBirthDate().toString().isEmpty() ) {
                            birthDate = "né le "+record.getWifeBirthDate().toString();
                        }
                        String marriedName = "";
                        if ( ! record.getWifeMarriedLastName().toString().isEmpty()){
                            marriedName = "conjoint: " + record.getWifeMarriedLastName()
                                    + record.getWifeMarriedFirstName() +" "
                                    + record.getWifeMarriedComment();
                        }
                        line.appendCsvFn(record.getWifeComment().toString(),
                            record.getWifeOccupation().toString(),
                            record.getWifeResidence().toString(),
                            birthDate.toString(),
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
                            formatAge(record.getWifeFatherAge()));
                        line.appendCsvFn(record.getWifeMotherLastName().toString());
                        line.appendCsvFn(record.getWifeMotherFirstName().toString());
                        line.appendCsvFn(record.getWifeMotherDead().toString());
                        line.appendCsvFn(record.getWifeMotherComment().toString(), 
                            record.getWifeMotherOccupation().toString(),
                            record.getWifeMotherResidence().toString(),
                            formatAge(record.getWifeMotherAge()));

                    } else  if (record instanceof RecordDeath ) {
                        line.appendCsvFn(record.getIndiMarriedLastName().toString());
                        line.appendCsvFn(record.getIndiMarriedFirstName().toString());
                        line.appendCsvFn(record.getIndiMarriedDead().toString()); //wifeDead
                        line.appendCsvFn(""); // age
                        line.appendCsvFn(""); //place
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
     * 
     * @param agedField
     * @return 
     */
    static private String formatAge(FieldAge agedField) {
        String age;
        if (agedField.getValue().equals("0d")) {
            age = "";
        } else {
            age = "Age:" + agedField.getValue().replace('y', 'a').replace('d', 'j');
        }
        return age;
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
        line.appendCsvFn("Lieu naissance"); 
        line.appendCsvFn("Commentaire individu");
        
        line.appendCsvFn("Prenom pere"); 
        line.appendCsvFn("Pere decede"); 
        line.appendCsvFn("Commentaire pere");

        line.appendCsvFn("Nom mere"); 
        line.appendCsvFn("Prenom mere"); 
        line.appendCsvFn("Mere decede");
        line.appendCsvFn("Commenaire mere");
        
        line.appendCsvFn("Nom epouse"); 
        line.appendCsvFn("Prenom epouse"); 
        line.appendCsvFn("Epouse decedee");
        line.appendCsvFn("Age epouse");
        line.appendCsvFn("Lieu naissance");
        line.appendCsvFn("Commentaire epouse");
        
        line.appendCsvFn("Prenom pere epouse");
        line.appendCsvFn("Pere decede"); 
        line.appendCsvFn("Commentaire pere epouse");
        
        line.appendCsvFn("Nom mere epouse");
        line.appendCsvFn("Prenom mere epouse");
        line.appendCsvFn("Mere epouse decedee");
        line.appendCsvFn("Commentaire mere epouse");
        
        line.appendCsvFn("Heritiers");
        line.appendCsvFn("Nom temoin 1");
        line.appendCsvFn("Prenom temoin 1");
        line.appendCsvFn("Commentaire temoin 1");
        line.appendCsvFn("Nom temoin 2");
        line.appendCsvFn("Prenom temoin 2");
        line.appendCsvFn("Commentaire temoin 2");
        line.appendCsv("Commentaire general");

        line.appendCsv("\n");
        return line;
    }
   
}
