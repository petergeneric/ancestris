package ancestris.modules.releve.file;

import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.ModelAbstract;
import ancestris.modules.releve.model.RecordMisc;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordMarriage;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.file.FileManager.Line;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 *
 * @author Michel
 */
public class ReleveFileEgmt {

    final static char fieldSeparator = ';';

    /**
     * verifie le format de la premiere ligne du fichier
     * @param strLine
     * @return
     */
    public static boolean isValidFile( BufferedReader br) {
        try {
            String[] fields = splitLine(br);
            if (fields == null ) {
                return false;
            }
        } catch (Exception ex) {
            return false;
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
                throw new Exception(String.format("Line contains %s fields. Must be %d fields", fields.length, 44));
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
            BufferedReader br = new BufferedReader(new FileReader(inputFile));
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
                                    fields[EgmtField.indiComment.ordinal()]);
                            record.setIndiFather(
                                    fields[EgmtField.indiFatherFirstName.ordinal()],
                                    fields[EgmtField.indiLastName.ordinal()], // meme nom que Indi
                                    "", // profession
                                    fields[EgmtField.indiFatherComment.ordinal()],
                                    fields[EgmtField.indiFatherDead.ordinal()] );  //décédé

                            record.setIndiMother(
                                    fields[EgmtField.indiMotherFirstName.ordinal()],
                                    fields[EgmtField.indiMotherLastName.ordinal()],
                                    "", //profession
                                    fields[EgmtField.indiMotherComment.ordinal()],
                                    fields[EgmtField.indiMotherDead.ordinal()]);  //décédé

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
                                    fields[EgmtField.indiPlace.ordinal()],
                                    "", // pas de profession a la naissance
                                    fields[EgmtField.indiComment.ordinal()]);

                            record.setIndiFather(
                                    fields[EgmtField.indiFatherFirstName.ordinal()],
                                    fields[EgmtField.indiLastName.ordinal()], // meme nom que Indi
                                    "", // profession
                                    fields[EgmtField.indiFatherComment.ordinal()],
                                    fields[EgmtField.indiFatherDead.ordinal()] );  //décédé

                            record.setIndiMother(
                                    fields[EgmtField.indiMotherFirstName.ordinal()],
                                    fields[EgmtField.indiMotherLastName.ordinal()],
                                    "", //profession
                                    fields[EgmtField.indiMotherComment.ordinal()],
                                    fields[EgmtField.indiMotherDead.ordinal()]);  //décédé
                            record.setWife(
                                    fields[EgmtField.wifeFirstName.ordinal()],
                                    fields[EgmtField.wifeLastName.ordinal()],
                                    fields[EgmtField.indiSex.ordinal()].equals("M") ? "F" : "M",
                                    fields[EgmtField.wifeAge.ordinal()],
                                    "" , // date de naissance
                                    fields[EgmtField.wifePlace.ordinal()],
                                    "" , // profession
                                    fields[EgmtField.wifeComment.ordinal()]);
                            //TODO : deces de l'epouse est ignoré , fields[EgmtField.wifeDead.ordinal()]
                                
                             record.setWifeFather(
                                    fields[EgmtField.wifeFatherFirstName.ordinal()],
                                    fields[EgmtField.wifeLastName.ordinal()], // meme nom que la femme
                                    "" , // profession
                                    fields[EgmtField.wifeFatherComment.ordinal()],
                                    fields[EgmtField.wifeFatherDead.ordinal()] );

                                record.setWifeMother(
                                    fields[EgmtField.wifeMotherFirstName.ordinal()],
                                    fields[EgmtField.wifeMotherLastName.ordinal()],
                                    "" , // profession
                                    fields[EgmtField.wifeMotherComment.ordinal()],
                                    fields[EgmtField.wifeMotherDead.ordinal()] );

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
                                    fields[EgmtField.indiPlace.ordinal()],
                                    "", // pas de profession a la naissance
                                    fields[EgmtField.indiComment.ordinal()]);

                             record.setIndiFather(
                                    fields[EgmtField.indiFatherFirstName.ordinal()],
                                    fields[EgmtField.indiLastName.ordinal()], // meme nom que Indi
                                    "", // profession
                                    fields[EgmtField.indiFatherComment.ordinal()],
                                    fields[EgmtField.indiFatherDead.ordinal()] );  //décédé

                            record.setIndiMother(
                                    fields[EgmtField.indiMotherFirstName.ordinal()],
                                    fields[EgmtField.indiMotherLastName.ordinal()],
                                    "", //profession
                                    fields[EgmtField.indiMotherComment.ordinal()],
                                    fields[EgmtField.indiMotherDead.ordinal()]);  //décédé

                            record.setIndiMarried(
                                    fields[EgmtField.wifeFirstName.ordinal()],
                                    fields[EgmtField.wifeLastName.ordinal()],
                                    "" , // profession
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
                                record.setEventType("contrat de mariage", "MARC");
                            } else if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("testament")) {
                                record.setEventType("testament", "WILL");
                            } else {
                                record.setEventType(fields[EgmtField.typeActe.ordinal()], "");
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
                                    fields[EgmtField.indiPlace.ordinal()],
                                    "", // pas de profession a la naissance
                                    fields[EgmtField.indiComment.ordinal()]);
                            
                            record.setIndiFather(
                                    fields[EgmtField.indiFatherFirstName.ordinal()],
                                    fields[EgmtField.indiLastName.ordinal()], // meme nom que Indi
                                    "", // profession
                                    fields[EgmtField.indiFatherComment.ordinal()],
                                    fields[EgmtField.indiFatherDead.ordinal()] );

                            record.setIndiMother(
                                    fields[EgmtField.indiMotherFirstName.ordinal()],
                                    fields[EgmtField.indiMotherLastName.ordinal()],
                                    "", //profession
                                    fields[EgmtField.indiMotherComment.ordinal()],
                                    fields[EgmtField.indiMotherDead.ordinal()]); 
                            record.setWife(
                                    fields[EgmtField.wifeFirstName.ordinal()],
                                    fields[EgmtField.wifeLastName.ordinal()],
                                    fields[EgmtField.indiSex.ordinal()].equals("M") ? "F" : "M",
                                    fields[EgmtField.wifeAge.ordinal()],
                                    "", // date de naissance
                                    fields[EgmtField.wifePlace.ordinal()],
                                    "" , // profession
                                    fields[EgmtField.wifeComment.ordinal()]);
                            //TODO :  fields[EgmtField.wifeDead.ordinal()]
                                
                            record.setWifeFather(
                                    fields[EgmtField.wifeFatherFirstName.ordinal()],
                                    fields[EgmtField.wifeLastName.ordinal()], // meme nom que la femme
                                    "" , // profession
                                    fields[EgmtField.wifeFatherComment.ordinal()],
                                    fields[EgmtField.wifeFatherDead.ordinal()] );

                            record.setWifeMother(
                                    fields[EgmtField.wifeMotherFirstName.ordinal()],
                                    fields[EgmtField.wifeMotherLastName.ordinal()],
                                    "" , // profession
                                    fields[EgmtField.wifeMotherComment.ordinal()],
                                    fields[EgmtField.wifeMotherDead.ordinal()] );

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
                        fileBuffer.append("Line ").append(lineNumber).append("\n   ");
                        fileBuffer.append(strLine).append("\n   ");
                        fileBuffer.append("Error ").append("invalid line");
                    }
                } catch (Exception e) {
                    fileBuffer.append("Line ").append(lineNumber).append("\n   ");
                    fileBuffer.append(strLine).append("\n   ");
                    fileBuffer.append("Error ").append(e).append("\n");
                }
            } // for

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
    public static StringBuilder saveFile(DataManager dataManager, ModelAbstract recordModel, File fileName, boolean append) {
        StringBuilder sb = new StringBuilder();

        try {
            //create BufferedReader to read csv file
            FileWriter writer = new FileWriter(fileName, append);
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
                    line.appendCsvFn(dataManager.getCountryName());
                    line.appendCsvFn(dataManager.getCityName());
                    line.appendCsvFn(record.getParish().toString());
                    if ( record instanceof RecordMisc ) {
                        line.appendCsvFn(record.getNotary().toString());
                    } else {
                        line.appendCsvFn("");
                    }
                    line.appendCsvFn(record.getCote().toString());
                    line.appendCsvFn(record.getFreeComment().toString());
                    line.appendCsvFn(String.format("%02d", record.getEventDateField().getStart().getDay()+1));
                    line.appendCsvFn(String.format("%02d", record.getEventDateField().getStart().getMonth()+1));
                    line.appendCsvFn(String.format("%4d", record.getEventDateField().getStart().getYear()));

                    line.appendCsvFn(record.getIndiLastName().getValue());
                    line.appendCsvFn(record.getIndiFirstName().getValue());
                    line.appendCsvFn(record.getIndiSex().toString());
                    if (!(record instanceof RecordBirth)) {
                        line.appendCsvFn(record.getIndiAge().toString());
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
                            birthDate.toString(),
                            marriedName,
                            record.getIndiMarriedOccupation().toString(),
                            record.getIndiMarriedComment().toString()
                            );
                    }
                    
                    line.appendCsvFn(record.getIndiFatherFirstName().toString());
                    line.appendCsvFn(record.getIndiFatherDead().toString());
                    line.appendCsvFn(record.getIndiFatherComment().toString(),
                            record.getIndiFatherOccupation().toString());
                    line.appendCsvFn(record.getIndiMotherLastName().toString());
                    line.appendCsvFn(record.getIndiMotherFirstName().toString());
                    line.appendCsvFn(record.getIndiMotherDead().toString());
                    line.appendCsvFn(record.getIndiMotherComment().toString(),
                            record.getIndiMotherOccupation().toString());
                    
                    if ((record instanceof RecordMarriage) || (record instanceof RecordMisc)) {
                        line.appendCsvFn(record.getWifeLastName().toString());
                        line.appendCsvFn(record.getWifeFirstName().toString());
                        line.appendCsvFn(""); //wifeDead
                        line.appendCsvFn(record.getWifeAge().toString());
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
                            birthDate.toString(),
                            marriedName,
                            record.getWifeMarriedOccupation().toString(),
                            record.getWifeMarriedComment().toString()
                            );

                        line.appendCsvFn(record.getWifeFatherFirstName().toString());
                        line.appendCsvFn(record.getWifeFatherDead().getValue());
                        line.appendCsvFn(record.getWifeFatherComment().toString(), record.getWifeFatherOccupation().toString());
                        line.appendCsvFn(record.getWifeMotherLastName().toString());
                        line.appendCsvFn(record.getWifeMotherFirstName().toString());
                        line.appendCsvFn(record.getWifeMotherDead().toString());
                        line.appendCsvFn(record.getWifeMotherComment().toString(), record.getWifeMotherOccupation().toString());
//                        line.appendCsvFn(record.getWifeMotherOccupation());
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
                    sb.append("Releve ").append(line).append("\n   ");
                    sb.append("Error ").append(e).append("\n");
                }
            }
            writer.close();

        } catch (Exception e) {
            System.out.println("Exception while reading file: " + e);
        }
        return sb;
    }

   
}
