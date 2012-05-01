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

    final static String fieldSeparator = ";";

    /**
     * verifie le format de la premiere ligne du fichier
     * @param strLine
     * @return
     */
    public static boolean isValidFile( String strLine) {
        String[] fields = splitLine(strLine);
        return fields != null;
    }

    /**
     * decoupe une ligne
     * @param strLine
     * @return fields[] ou null si la ligne n'est pas valide
     */
    private static String[] splitLine( String strLine) {
        String[] fields = strLine.split(fieldSeparator,100);
        if (fields.length == 44 ) {
           return fields;
        }
        return null;
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

            //read comma separated file line by line
            while ((strLine = br.readLine()) != null) {
                lineNumber++;
                String[] fields = splitLine(strLine);
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
                                    "", // pas de date de naossance a la naissance
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
                                    "", // pas de profedate de naissance a la naissance
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
                            record.setWife(
                                    fields[EgmtField.wifeFirstName.ordinal()],
                                    fields[EgmtField.wifeLastName.ordinal()],
                                    fields[EgmtField.indiSex.ordinal()].equals("M") ? "F" : "M",
                                    fields[EgmtField.wifeAge.ordinal()],
                                    "" , // date de naissance
                                    fields[EgmtField.wifePlace.ordinal()],
                                    "" , // profession
                                    fields[EgmtField.wifeComment.ordinal()]);
                                
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

                        } else  {
                            RecordMisc record = new RecordMisc();
                            if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("contrat de mariage")) {
                                record.setEventType("contrat de mariage", "MARC");
                            } else if (fields[EgmtField.typeActe.ordinal()].toLowerCase().equals("testament")) {
                                record.setEventType("testament", "WILL");
                            } else {
                                record.setEventType("autre", "EVEN");
                            }


                            record.setEventPlace(
                                    fields[EgmtField.nomCommune.ordinal()],
                                    "", // codecommune
                                    fields[EgmtField.codeDepartement.ordinal()],
                                    "", // stateName
                                    "" ); // countryName

                            // le notaire est utilisé suelement pour les actes divers
                            record.setNotary(fields[EgmtField.cote.ordinal()]);
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
    public static void saveFile(DataManager dataManager, ModelAbstract recordModel, File fileName, boolean append) {
        StringBuilder sb = new StringBuilder();

        try {
            //create BufferedReader to read csv file
            FileWriter writer = new FileWriter(fileName, append);
            for (int index = 0; index < recordModel.getRowCount(); index++) {
                
                Line line = new Line(fieldSeparator);
                final Record record = recordModel.getRecord(index);
                try {
                    if ( record instanceof RecordBirth ) {
                       line.appendSep("naissance");
                    } else if (record instanceof RecordMarriage) {
                        line.appendSep("mariage");
                    } else if (record instanceof RecordDeath) {
                        line.appendSep("décès");
                    } else {
                        line.appendSep(record.getEventType().getName());
                    }
                    line.appendSep(dataManager.getCountryName());
                    line.appendSep(dataManager.getCityName());
                    line.appendSep(record.getParish());
                    line.appendSep(record.getNotary());
                    line.appendSep(record.getCote());
                    line.appendSep(record.getFreeComment());
                    line.appendSep(String.format("%02d", record.getEventDateField().getStart().getDay()));
                    line.appendSep(String.format("%02d", record.getEventDateField().getStart().getMonth()));
                    line.appendSep(String.format("%4d", record.getEventDateField().getStart().getYear()));

                    line.appendSep(record.getIndiLastName().getValue());
                    line.appendSep(record.getIndiFirstName().getValue());
                    line.appendSep(record.getIndiSex());
                    if (!(record instanceof RecordBirth)) {
                        line.appendSep(record.getIndiAge());
                        line.appendSep(record.getIndiPlace().toString());
                    } else {
                        line.appendSep("");
                        line.appendSep("");
                    }

                    if ( record instanceof RecordBirth) {
                        line.appendSep(record.getIndiComment());
                    } else {
                        String birthDate = "";
                        if (!record.getIndiBirthDate().isEmpty() ) {
                            birthDate = "né le "+record.getIndiBirthDate();
                        }
                        String marriedName = "";
                        if ( ! record.getIndiMarriedLastName().isEmpty()){
                            marriedName = "conjoint: " + record.getIndiMarriedLastName()
                                    + record.getIndiMarriedFirstName() +" "
                                    + record.getIndiMarriedComment();
                        }
                        line.appendSep(record.getIndiComment(), 
                            record.getIndiOccupation().toString(),
                            birthDate, 
                            marriedName,
                            record.getIndiMarriedOccupation().toString(),
                            record.getIndiMarriedComment()
                            );
                    }
                    
                    line.appendSep(record.getIndiFatherFirstName().toString());
                    line.appendSep(record.getIndiFatherDead().toString());
                    line.appendSep(record.getIndiFatherComment(),
                            record.getIndiFatherOccupation().toString());
                    line.appendSep(record.getIndiMotherLastName().toString());
                    line.appendSep(record.getIndiMotherFirstName().toString());
                    line.appendSep(record.getIndiMotherDead().toString());
                    line.appendSep(record.getIndiMotherComment(),
                            record.getIndiMotherOccupation().toString());
                    
                    if (!(record instanceof RecordBirth)) {
                        line.appendSep(record.getWifeLastName().toString());
                        line.appendSep(record.getWifeFirstName().toString());
                        line.appendSep(record.getWifeAge());
                        line.appendSep(record.getWifePlace().toString());
                        line.appendSep(record.getWifeComment(), record.getWifeOccupation().toString());

                        String birthDate = "";
                        if (!record.getIndiBirthDate().isEmpty() ) {
                            birthDate = "né le "+record.getIndiBirthDate();
                        }
                        String marriedName = "";
                        if ( ! record.getWifeMarriedLastName().isEmpty()){
                            marriedName = "conjoint: " + record.getWifeMarriedLastName()
                                    + record.getWifeMarriedFirstName() +" "
                                    + record.getWifeMarriedComment();
                        }
                        line.appendSep(record.getWifeComment(),
                            record.getWifeOccupation().toString(),
                            birthDate,
                            marriedName,
                            record.getWifeMarriedOccupation().toString(),
                            record.getWifeMarriedComment()
                            );

                        line.appendSep(record.getWifeFatherFirstName().toString());
                        line.appendSep(record.getWifeFatherDead().getValue());
                        line.appendSep(record.getWifeFatherComment(), record.getWifeFatherOccupation().toString());
                        line.appendSep(record.getWifeMotherLastName().toString());
                        line.appendSep(record.getWifeMotherFirstName().toString());
                        line.appendSep(record.getWifeMotherDead().toString());
                        line.appendSep(record.getWifeMotherComment(), record.getWifeMotherOccupation().toString());
//                        line.appendSep(record.getWifeMotherOccupation());
                     } else {
                        line.appendSep("");
                        line.appendSep("");
                        line.appendSep("");
                        line.appendSep("");
                        line.appendSep("");
                        line.appendSep("");

                        line.appendSep("");
                        line.appendSep("");
                        line.appendSep("");
                        line.appendSep("");
                        line.appendSep("");
                        line.appendSep("");
                        line.appendSep("");
                     }

                    line.appendSep(""); // heirComment

                    line.appendSep(record.getWitness1LastName().toString());
                    line.appendSep(record.getWitness1FirstName().toString());
                    line.appendSep(record.getWitness1Comment(),
                        record.getWitness1Occupation().toString() );
                    line.appendSep(record.getWitness2LastName().toString());
                    line.appendSep(record.getWitness2FirstName().toString());
                    line.appendSep(record.getWitness2Comment(),
                         record.getWitness2Occupation().toString() );
                    if( record.getWitness3LastName().isEmpty() && record.getWitness4LastName().isEmpty()) {
                       line.appendSep(record.getGeneralComment());
                    } else {
                        line.appendSep(record.getGeneralComment(),
                           "témoin: " + record.getWitness3FirstName().toString() + " "+ record.getWitness3LastName().toString(),
                           record.getWitness3Occupation().toString(),
                           record.getWitness3Comment(),
                           record.getWitness4FirstName().toString() + " "+ record.getWitness4LastName().toString(),
                           record.getWitness4Occupation().toString(),
                           record.getWitness4Comment()
                           );
                    }

                    line.appendln("\r\n");
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
    }

   
}
