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
import java.io.IOException;

/**
 *
 * @author Michel
 */
public class ReleveFileNimegue {

    final static private char fieldSeparator = ';';
    final static private String fileSignature = "NIMEGUEV3";

    /**
     * verifie si la premere ligne est conforme au format
     * @param inputFile
     * @param sb  message d'erreur
     * @return
     */
    public static boolean isValidFile(File inputFile, StringBuilder sb) {
        BufferedReader br = null;
        try {
            //create BufferedReader to read file
            br = new BufferedReader(new FileReader(inputFile));
            String strLine = br.readLine();
            int lineNo = 1;
            String[] fields = splitLine(strLine, lineNo);

            if (fields == null) {
                sb.append(fileSignature + " ").append(String.format("Le fichier %s est vide.", inputFile.getName()));
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
    private static String[] splitLine(String strLine, int lineNo)throws Exception  {
        String[] fields = strLine.split(String.valueOf(fieldSeparator), 100);
        // nombre de champs +1 car NIMEGUE ajout un point virgule apres le dernier champ

        if (fields.length >= BirthField.typeActe.ordinal()) {
            if (fields[BirthField.typeActe.ordinal()].equals("N")
                    || fields[MarrField.typeActe.ordinal()].equals("M")
                    || fields[DeathField.typeActe.ordinal()].equals("D")
                    || fields[MiscField.typeActe.ordinal()].equals("V")) {
                if (fields.length == 30 + 1 && fields[BirthField.typeActe.ordinal()].equals("N")
                        || fields.length == 60 + 1 && fields[MarrField.typeActe.ordinal()].equals("M")
                        || fields.length == 38 + 1 && fields[DeathField.typeActe.ordinal()].equals("D")
                        || fields.length == 64 + 1 && fields[MiscField.typeActe.ordinal()].equals("V")) {

                    if (fields[0].equals(fileSignature)) {
                        return fields;
                    } else {
                        throw new Exception(String.format("Line %d: signature=%s, Must be %s", lineNo, fields[0], fileSignature));
                    }
                } else {
                    throw new Exception(String.format("Line %d: fields=%d, Must be 30, 38, 60 or 65", lineNo, fields.length));
                }
            } else {
                throw new Exception(String.format("Line %d: evntType=%s, Must be N, M, D or V", lineNo, fields[BirthField.typeActe.ordinal()]));
            }
        } else {
            throw new Exception(String.format("Line %d: fields=%d, Must be 30, 38, 60 or 65", lineNo, fields.length));
        }
    }

    // Foramt d'un releve d'une naissance
    enum BirthField {

        nimegue3,
        codeCommune, nomCommune, codeDepartement, nomDepartement,
        typeActe,
        dateNaissance, dateIncomplete, cote, photo,
        indiLastName, indiFirstName, indiSex, indiComment,
        indiFatherLastName, indiFatherFirstName, indiFatherComment, indiFatherOccupation,
        indiMotherLastName, indiMotherFirstName, indiMotherComment, indiMotherOccupation,
        witness1LastName, witness1FirstName, witness1Comment,
        witness2LastName, witness2FirstName, witness2Comment,
        generalComment, recordNo
    }

    // Format d'un releve d'un marriage
    enum MarrField {

        nimegue3,
        codeCommune, nomCommune, codeDepartement, nomDepartement,
        typeActe,
        eventDate, dateIncomplete, cote, libre,
        indiLastName, indiFirstName, indiPlace, indiBirthDate, indiAge, indiComment, indiOccupation,
        indiMarriedLastName, indiMarriedFirstName, indiMarriedComment,
        indiFatherLastName, indiFatherFirstName, indiFatherComment, indiFatherOccupation,
        indiMotherLastName, indiMotherFirstName, indiMotherComment, indiMotherOccupation,
        wifeLastName, wifeFirstName, wifePlace, wifeBirthDate, wifeAge, wifeComment, wifeOccupation,
        wifeMarriedLastName, wifeMarriedFirstName, wifeMarriedComment,
        wifeFatherLastName, wifeFatherFirstName, wifeFatherComment, wifeFatherOccupation,
        wifeMotherLastName, wifeMotherFirstName, wifeMotherComment, wifeMotherOccupation,
        witness1LastName, witness1FirstName, witness1Comment,
        witness2LastName, witness2FirstName, witness2Comment,
        witness3LastName, witness3FirstName, witness3Comment,
        witness4LastName, witness4FirstName, witness4Comment,
        generalComment, recordNo
    }

    // Format d'un releve d'un deces
    enum DeathField {

        nimegue3,
        codeCommune, nomCommune, codeDepartement, nomDepartement,
        typeActe,
        eventDate, dateIncomplete, cote, libre,
        indiLastName, indiFirstName, indiPlace, indiBirthDate, indiSex, indiAge, indiComment, indiOccupation,
        wifeLastName, wifeFirstName, wifeComment, wifeOccupation,
        indiFatherLastName, indiFatherFirstName, indiFatherComment, indiFatherOccupation,
        indiMotherLastName, indiMotherFirstName, indiMotherComment, indiMotherOccupation,
        witness1LastName, witness1FirstName, witness1Comment,
        witness2LastName, witness2FirstName, witness2Comment,
        generalComment, recordNo
    }

    // Format d'un releve d'un acte divers
    enum MiscField {

        nimegue3,
        codeCommune, nomCommune, codeDepartement, nomDepartement,
        typeActe,
        eventDate, dateIncomplete, cote, libre,
        eventTypeName, eventTypeTag,
        indiLastName, indiFirstName, indiSex, indiPlace, indiBirthDate, indiAge, indiComment, indiOccupation,
        indiMarriedLastName, indiMarriedFirstName, indiMarriedComment,
        indiFatherLastName, indiFatherFirstName, indiFatherComment, indiFatherOccupation,
        indiMotherLastName, indiMotherFirstName, indiMotherComment, indiMotherOccupation,
        wifeLastName, wifeFirstName, wifeSex, wifePlace, wifeBirthDate, wifeAge, wifeComment, wifeOccupation,
        wifeMarriedLastName, wifeMarriedFirstName, wifeMarriedComment,
        wifeFatherLastName, wifeFatherFirstName, wifeFatherComment, wifeFatherOccupation,
        wifeMotherLastName, wifeMotherFirstName, wifeMotherComment, wifeMotherOccupation,
        witness1LastName, witness1FirstName, witness1Comment,
        witness2LastName, witness2FirstName, witness2Comment,
        witness3LastName, witness3FirstName, witness3Comment,
        witness4LastName, witness4FirstName, witness4Comment,
        generalComment, recordNo
    }

    /**
     *
     * @param fileName
     * TODO gérer la dat iincomplete
     */
    public static FileBuffer loadFile(File inputFile) {

//        // exemple de code pour traiter le cas où les champs contiennent ";"
//        Pattern p = Pattern.compile("\"([^\"]|\"\")*\"(;|$)|[^;]*(;|$)");
//        Matcher m = p.matcher(test);
//        while ( m.find() )
//            System.out.println(m.group());

        FileBuffer fileBuffer = new FileBuffer();
        try {

            //create BufferedReader to read file
            BufferedReader br = new BufferedReader(new FileReader(inputFile));
            String strLine = "";
            int lineNumber = 0;

            //read comma separated file line by line
            while ((strLine = br.readLine()) != null) {
                lineNumber++;

                try {

                    String[] fields = splitLine(strLine, lineNumber);

                    if (fields == null) {
                        continue;
                    }

                    if (fields[BirthField.typeActe.ordinal()].equals("N")) {
                        RecordBirth record = new RecordBirth();

                        record.setEventPlace(
                                fields[BirthField.nomCommune.ordinal()],
                                fields[BirthField.codeCommune.ordinal()],
                                fields[BirthField.nomDepartement.ordinal()].isEmpty() ? fields[BirthField.codeDepartement.ordinal()] : fields[BirthField.nomDepartement.ordinal()],
                                "", // stateName
                                ""  // contryName
                                );

                        record.setEventDate(fields[BirthField.dateNaissance.ordinal()]);
                        //record.dateIncomplete = fields[BirthField.dateIncomplete.ordinal()];

                        record.setCote(fields[BirthField.cote.ordinal()]);
                        record.setFreeComment(fields[BirthField.photo.ordinal()]);
                        record.setIndi(
                                fields[BirthField.indiFirstName.ordinal()],
                                fields[BirthField.indiLastName.ordinal()],
                                fields[BirthField.indiSex.ordinal()],
                                "" , // age
                                "" , // date naissance
                                "" , // place
                                "", // pas de profession a la naissance
                                fields[BirthField.indiComment.ordinal()]);

                        record.setIndiFather(
                                fields[BirthField.indiFatherFirstName.ordinal()],
                                fields[BirthField.indiFatherLastName.ordinal()],
                                fields[BirthField.indiFatherOccupation.ordinal()],
                                fields[BirthField.indiFatherComment.ordinal()],
                                "");  //décédé

                        record.setIndiMother(
                                fields[BirthField.indiMotherFirstName.ordinal()],
                                fields[BirthField.indiMotherLastName.ordinal()],
                                fields[BirthField.indiMotherOccupation.ordinal()],
                                fields[BirthField.indiMotherComment.ordinal()],
                                "");  //décédé

                        record.setWitness1(
                                fields[BirthField.witness1FirstName.ordinal()],
                                fields[BirthField.witness1LastName.ordinal()],
                                "",
                                fields[BirthField.witness1Comment.ordinal()]);

                        record.setWitness2(
                                fields[BirthField.witness2FirstName.ordinal()],
                                fields[BirthField.witness2LastName.ordinal()],
                                "",
                                fields[BirthField.witness2Comment.ordinal()]);

                        record.setGeneralComment(fields[BirthField.generalComment.ordinal()]);

                        try {
                            record.recordNo = Integer.valueOf(fields[BirthField.recordNo.ordinal()]);
                        } catch (NumberFormatException ex) {
                            record.recordNo = 0;
                        }

                        fileBuffer.loadRecord(record);

                    } else if (fields[MarrField.typeActe.ordinal()].equals("M")) {
                        RecordMarriage record = new RecordMarriage();

                        record.setEventPlace(
                                fields[MarrField.nomCommune.ordinal()],
                                fields[MarrField.codeCommune.ordinal()],
                                fields[MarrField.nomDepartement.ordinal()].isEmpty() ? fields[MarrField.codeDepartement.ordinal()] : fields[MarrField.nomDepartement.ordinal()],
                                "", // stateName
                                ""  // contryName
                                );

                        record.setEventDate(fields[MarrField.eventDate.ordinal()]);
                        //record.dateIncomplete = fields[MarrField.dateIncomplete.ordinal()];

                        record.setCote(fields[MarrField.cote.ordinal()]);
                        record.setFreeComment(fields[MarrField.libre.ordinal()]);

                        record.setIndi(
                                fields[MarrField.indiFirstName.ordinal()],
                                fields[MarrField.indiLastName.ordinal()],
                                "M",
                                fields[MarrField.indiAge.ordinal()],
                                fields[MarrField.indiBirthDate.ordinal()],
                                fields[MarrField.indiPlace.ordinal()],
                                fields[MarrField.indiOccupation.ordinal()],
                                fields[MarrField.indiComment.ordinal()]);

                        record.setIndiMarried(
                                fields[MarrField.indiMarriedFirstName.ordinal()],
                                fields[MarrField.indiMarriedLastName.ordinal()],
                                //"F",
                                "",
                                fields[MarrField.indiMarriedComment.ordinal()],
                                "");  //décédé

                        record.setIndiFather(
                                fields[MarrField.indiFatherFirstName.ordinal()],
                                fields[MarrField.indiFatherLastName.ordinal()],
                                fields[MarrField.indiFatherOccupation.ordinal()],
                                fields[MarrField.indiFatherComment.ordinal()],
                                "");  //décédé

                        record.setIndiMother(
                                fields[MarrField.indiMotherFirstName.ordinal()],
                                fields[MarrField.indiMotherLastName.ordinal()],
                                fields[MarrField.indiMotherOccupation.ordinal()],
                                fields[MarrField.indiMotherComment.ordinal()],
                                "");  //décédé

                        record.setWife(
                                fields[MarrField.wifeFirstName.ordinal()],
                                fields[MarrField.wifeLastName.ordinal()],
                                "F",
                                fields[MarrField.wifeAge.ordinal()],
                                fields[MarrField.wifeBirthDate.ordinal()],
                                fields[MarrField.wifePlace.ordinal()],
                                fields[MarrField.wifeOccupation.ordinal()],
                                fields[MarrField.wifeComment.ordinal()]); 

                        record.setWifeMarried(
                                fields[MarrField.wifeMarriedFirstName.ordinal()],
                                fields[MarrField.wifeMarriedLastName.ordinal()],
                                //M",
                                "",
                                fields[MarrField.wifeMarriedComment.ordinal()],
                                "");  //décédé

                        record.setWifeFather(
                                fields[MarrField.wifeFatherFirstName.ordinal()],
                                fields[MarrField.wifeFatherLastName.ordinal()],
                                fields[MarrField.wifeFatherOccupation.ordinal()],
                                fields[MarrField.wifeFatherComment.ordinal()],
                                "");  //décédé

                        record.setWifeMother(
                                fields[MarrField.wifeMotherFirstName.ordinal()],
                                fields[MarrField.wifeMotherLastName.ordinal()],
                                fields[MarrField.wifeMotherOccupation.ordinal()],
                                fields[MarrField.wifeMotherComment.ordinal()],
                                "");  //décédé

                        record.setWitness1(
                                fields[MarrField.witness1FirstName.ordinal()],
                                fields[MarrField.witness1LastName.ordinal()],
                                "", // profession
                                fields[MarrField.witness1Comment.ordinal()]);

                        record.setWitness2(
                                fields[MarrField.witness2FirstName.ordinal()],
                                fields[MarrField.witness2LastName.ordinal()],
                                "", // profession
                                fields[MarrField.witness2Comment.ordinal()]);

                        record.setWitness3(
                                fields[MarrField.witness3FirstName.ordinal()],
                                fields[MarrField.witness3LastName.ordinal()],
                                "", // profession
                                fields[MarrField.witness3Comment.ordinal()]);

                        record.setWitness4(
                                fields[MarrField.witness4FirstName.ordinal()],
                                fields[MarrField.witness4LastName.ordinal()],
                                "", // profession
                                fields[MarrField.witness4Comment.ordinal()]);

                        record.setGeneralComment(fields[MarrField.generalComment.ordinal()]);

                        try {
                            record.recordNo = Integer.valueOf(fields[MarrField.recordNo.ordinal()]);
                        } catch (NumberFormatException ex) {
                            record.recordNo = 0;
                        }

                        fileBuffer.loadRecord(record);

                    } else if (fields[DeathField.typeActe.ordinal()].equals("D")) {
                        RecordDeath record = new RecordDeath();

                        record.setEventPlace(
                                fields[DeathField.nomCommune.ordinal()],
                                fields[DeathField.codeCommune.ordinal()],
                                fields[DeathField.nomDepartement.ordinal()].isEmpty() ? fields[DeathField.codeDepartement.ordinal()] : fields[DeathField.nomDepartement.ordinal()],
                                "", // stateName
                                ""  // contryName
                                );

                        record.setEventDate(fields[DeathField.eventDate.ordinal()]);
                        //record.dateIncomplete = fields[DeathField.dateIncomplete.ordinal()];

                        record.setCote(fields[DeathField.cote.ordinal()]);
                        record.setFreeComment(fields[DeathField.libre.ordinal()]);

                        record.setIndi(
                                fields[DeathField.indiFirstName.ordinal()],
                                fields[DeathField.indiLastName.ordinal()],
                                fields[DeathField.indiSex.ordinal()],
                                fields[DeathField.indiAge.ordinal()],
                                fields[DeathField.indiBirthDate.ordinal()],
                                fields[DeathField.indiPlace.ordinal()],
                                fields[DeathField.indiOccupation.ordinal()],
                                fields[DeathField.indiComment.ordinal()]);

                        record.setIndiMarried(
                                fields[DeathField.wifeFirstName.ordinal()],
                                fields[DeathField.wifeLastName.ordinal()],
                                //fields[DeathField.indiSex.ordinal()].equals("M") ? "F" : "M",
                                fields[DeathField.wifeOccupation.ordinal()],
                                fields[DeathField.wifeComment.ordinal()],
                                "");  //décédé

                        record.setIndiFather(
                                fields[DeathField.indiFatherFirstName.ordinal()],
                                fields[DeathField.indiFatherLastName.ordinal()],
                                fields[DeathField.indiFatherOccupation.ordinal()],
                                fields[DeathField.indiFatherComment.ordinal()],
                                "");  //décédé

                        record.setIndiMother(
                                fields[DeathField.indiMotherFirstName.ordinal()],
                                fields[DeathField.indiMotherLastName.ordinal()],
                                fields[DeathField.indiMotherOccupation.ordinal()],
                                fields[DeathField.indiMotherComment.ordinal()],
                                "");  //décédé

                        record.setWitness1(
                                fields[DeathField.witness1FirstName.ordinal()],
                                fields[DeathField.witness1LastName.ordinal()],
                                "",
                                fields[DeathField.witness1Comment.ordinal()]);

                        record.setWitness2(
                                fields[DeathField.witness2FirstName.ordinal()],
                                fields[DeathField.witness2LastName.ordinal()],
                                "",
                                fields[DeathField.witness2Comment.ordinal()]);

                        record.setGeneralComment(fields[DeathField.generalComment.ordinal()]);
                        try {
                            record.recordNo = Integer.valueOf(fields[DeathField.recordNo.ordinal()]);
                        } catch (NumberFormatException ex) {
                            record.recordNo = 0;
                        }

                        fileBuffer.loadRecord(record);
                    } else if (fields[MiscField.typeActe.ordinal()].equals("V")) {
                        RecordMisc record = new RecordMisc();

                        record.setEventPlace(
                                fields[MiscField.nomCommune.ordinal()],
                                fields[MiscField.codeCommune.ordinal()],
                                fields[MiscField.nomDepartement.ordinal()].isEmpty() ? fields[MiscField.codeDepartement.ordinal()] : fields[MiscField.nomDepartement.ordinal()],
                                "", // stateName
                                ""  // contryName
                                );

                        record.setEventDate(fields[MiscField.eventDate.ordinal()]);
                        //record.dateIncomplete = fields[MiscField.dateIncomplete.ordinal()];
                        record.setEventType(
                                fields[MiscField.eventTypeName.ordinal()],
                                fields[MiscField.eventTypeTag.ordinal()]);

                        record.setCote(fields[MiscField.cote.ordinal()]);
                        record.setFreeComment(fields[MiscField.libre.ordinal()]);

                        record.setIndi(
                                fields[MiscField.indiFirstName.ordinal()],
                                fields[MiscField.indiLastName.ordinal()],
                                fields[MiscField.indiSex.ordinal()],
                                fields[MiscField.indiAge.ordinal()],
                                fields[MiscField.indiBirthDate.ordinal()],
                                fields[MiscField.indiPlace.ordinal()],
                                fields[MiscField.indiOccupation.ordinal()],
                                fields[MiscField.indiComment.ordinal()]);

                        record.setIndiMarried(
                                fields[MiscField.indiMarriedFirstName.ordinal()],
                                fields[MiscField.indiMarriedLastName.ordinal()],
                                //fields[MiscField.indiSex.ordinal()].equals("M") ? "F" : "M",
                                "", // profession
                                fields[MiscField.indiMarriedComment.ordinal()],
                                "");  //décédé

                        record.setIndiFather(
                                fields[MiscField.indiFatherFirstName.ordinal()],
                                fields[MiscField.indiFatherLastName.ordinal()],
                                fields[MiscField.indiFatherOccupation.ordinal()],
                                fields[MiscField.indiFatherComment.ordinal()],
                                "");  //décédé

                        record.setIndiMother(
                                fields[MiscField.indiMotherFirstName.ordinal()],
                                fields[MiscField.indiMotherLastName.ordinal()],
                                fields[MiscField.indiMotherOccupation.ordinal()],
                                fields[MiscField.indiMotherComment.ordinal()],
                                "");  //décédé

                        record.setWife(
                                fields[MiscField.wifeFirstName.ordinal()],
                                fields[MiscField.wifeLastName.ordinal()],
                                fields[MiscField.wifeSex.ordinal()],
                                fields[MiscField.wifeAge.ordinal()],
                                fields[MiscField.wifeBirthDate.ordinal()],
                                fields[MiscField.wifePlace.ordinal()],
                                fields[MiscField.wifeOccupation.ordinal()],
                                fields[MiscField.wifeComment.ordinal()]); 

                        record.setWifeMarried(
                                fields[MiscField.wifeMarriedFirstName.ordinal()],
                                fields[MiscField.wifeMarriedLastName.ordinal()],
                                //fields[MiscField.indiSex.ordinal()].equals("M") ? "F" : "M",
                                "", // profession
                                fields[MiscField.wifeMarriedComment.ordinal()],
                                "");  //décédé

                        record.setWifeFather(
                                fields[MiscField.wifeFatherFirstName.ordinal()],
                                fields[MiscField.wifeFatherLastName.ordinal()],
                                fields[MiscField.wifeFatherOccupation.ordinal()],
                                fields[MiscField.wifeFatherComment.ordinal()],
                                "");  //décédé

                        record.setWifeMother(
                                fields[MiscField.wifeMotherFirstName.ordinal()],
                                fields[MiscField.wifeMotherLastName.ordinal()],
                                fields[MiscField.wifeMotherOccupation.ordinal()],
                                fields[MiscField.wifeMotherComment.ordinal()],
                                "");  //décédé

                        record.setWitness1(
                                fields[MiscField.witness1FirstName.ordinal()],
                                fields[MiscField.witness1LastName.ordinal()],
                                "", // profession
                                fields[MiscField.witness1Comment.ordinal()]);

                        record.setWitness2(
                                fields[MiscField.witness2FirstName.ordinal()],
                                fields[MiscField.witness2LastName.ordinal()],
                                "", // profession
                                fields[MiscField.witness2Comment.ordinal()]);

                        record.setWitness3(
                                fields[MiscField.witness3FirstName.ordinal()],
                                fields[MiscField.witness3LastName.ordinal()],
                                "", // profession
                                fields[MiscField.witness3Comment.ordinal()]);

                        record.setWitness4(
                                fields[MiscField.witness4FirstName.ordinal()],
                                fields[MiscField.witness4LastName.ordinal()],
                                "", // profession
                                fields[MiscField.witness4Comment.ordinal()]);

                        record.setGeneralComment(fields[MiscField.generalComment.ordinal()]);
                        try {
                            record.recordNo = Integer.valueOf(fields[MiscField.recordNo.ordinal()]);
                        } catch (NumberFormatException ex) {
                            record.recordNo = 0;
                        }
                        fileBuffer.loadRecord(record);
                        
                    } else {
                        fileBuffer.append("Line ").append(lineNumber).append(": ");
                        fileBuffer.append("Type d'acte inconnu").append(" ");
                        fileBuffer.append(fields[BirthField.typeActe.ordinal()]).append("\n");
                    }
                } catch (Exception e) {
                    fileBuffer.append("Line ").append(lineNumber).append(": ").append(e.toString()).append("\n");
                    fileBuffer.append(strLine).append("\n");
                    e.printStackTrace();
                }
            } // for

        } catch (Exception e) {
            fileBuffer.append(e.toString()).append("\n");
        }

        return fileBuffer;
    }

    /**
     *
     * @param fileName
     * TODO gérer la dat iincomplete
     */
    public static StringBuilder saveFile(DataManager dataManager, ModelAbstract recordModel, File fileName, boolean append) {
        // exemple
        // NIMEGUEV3;09195;Monesple;09;Ariège;N;07/07/1674;07/1674;pujagou;6204;
        // DEJEAN;Jean;M;indiComment fils;
        // DEJEAN;Jean;;;
        // SOULA;Raymonde;;;
        // CALLES;Bernard;indiComment parrain;
        // DEJEAN;Jeanne;indiComment marraine;
        // commnetaire general;273;

//        // exemple de code pour traiter le cas où les champs contiennent ";"
//        Pattern p = Pattern.compile("\"([^\"]|\"\")*\"(;|$)|[^;]*(;|$)");
//        Matcher m = p.matcher(test);
//        while ( m.find() )
//            System.out.println(m.group());

        StringBuilder sb = new StringBuilder();
        try {
            //create BufferedReader to read csv file
            FileWriter writer = new FileWriter(fileName, append);
            for (int index = 0; index < recordModel.getRowCount(); index++) {
                Line line = new Line(fieldSeparator);
                Record record = recordModel.getRecord(index);
                try {
                    if ( record instanceof RecordBirth ) {
                        line.appendNimegueFn("NIMEGUEV3");
                        line.appendNimegueFn(dataManager.getCityCode());
                        line.appendNimegueFn(dataManager.getCityName());
                        line.appendNimegueFn(dataManager.getCountryName());
                        line.appendNimegueFn(dataManager.getCountyName());
                        line.appendNimegueFn("N");
                        line.appendNimegueFn(record.getEventDateString());
                        line.appendNimegueFn("");
                        line.appendNimegueFn(record.getCote().toString());
                        line.appendNimegueFn(record.getFreeComment().toString());
                        line.appendNimegueFn(record.getIndiLastName().toString());
                        line.appendNimegueFn(record.getIndiFirstName().toString());
                        line.appendNimegueFn(record.getIndiSex().toString());
                        line.appendNimegueFn(record.getIndiComment().toString());

                        line.appendNimegueFn(record.getIndiFatherLastName().toString());
                        line.appendNimegueFn(record.getIndiFatherFirstName().toString());
                        line.appendNimegueFn(record.getIndiFatherDead().toString(), record.getIndiFatherComment().toString());
                        line.appendNimegueFn(record.getIndiFatherOccupation().toString());

                        line.appendNimegueFn(record.getIndiMotherLastName().toString());
                        line.appendNimegueFn(record.getIndiMotherFirstName().toString());
                        line.appendNimegueFn(record.getIndiMotherDead().toString(), record.getIndiMotherComment().toString());
                        line.appendNimegueFn(record.getIndiMotherOccupation().toString());

                        line.appendNimegueFn(record.getWitness1LastName().toString());
                        line.appendNimegueFn(record.getWitness1FirstName().toString());
                        line.appendNimegueFn(record.getWitness1Comment().toString(),
                            record.getWitness1Occupation().toString() );

                        line.appendNimegueFn(record.getWitness2LastName().toString());
                        line.appendNimegueFn(record.getWitness2FirstName().toString());
                        line.appendNimegueFn(record.getWitness2Comment().toString(),
                            record.getWitness2Occupation().toString() );

                        if (record.getWitness3LastName().isEmpty() && record.getWitness4LastName().isEmpty()) {
                            line.appendNimegueFn(record.getGeneralComment().toString());
                        } else {
                            line.appendNimegueFn(record.getGeneralComment().toString(),
                                    "témoin: " + record.getWitness3FirstName().toString() + " " + record.getWitness3LastName().toString(),
                                    record.getWitness3Occupation().toString(),
                                    record.getWitness3Comment().toString(),
                                    record.getWitness4FirstName().toString() + " " + record.getWitness4LastName().toString(),
                                    record.getWitness4Occupation().toString(),
                                    record.getWitness4Comment().toString());
                        }
                        line.appendNimegueFn(String.valueOf(record.recordNo)); // numero d'enregistrement

                    } if ( record instanceof RecordMarriage ) {

                        line.appendNimegueFn("NIMEGUEV3");
                        line.appendNimegueFn(dataManager.getCityCode());
                        line.appendNimegueFn(dataManager.getCityName());
                        line.appendNimegueFn(dataManager.getCountryName());
                        line.appendNimegueFn(dataManager.getCountyName());
                        line.appendNimegueFn("M");
                        line.appendNimegueFn(record.getEventDateString());
                        line.appendNimegueFn("");
                        line.appendNimegueFn(record.getCote().toString());
                        line.appendNimegueFn(record.getFreeComment().toString());

                        line.appendNimegueFn(record.getIndiLastName().getValue());
                        line.appendNimegueFn(record.getIndiFirstName().getValue());
                        line.appendNimegueFn(record.getIndiPlace().toString());
                        if (record.getIndiBirthDate().toString().trim().equals("")) {
                            line.appendNimegueFn("          ");
                        } else {
                            line.appendNimegueFn(record.getIndiBirthDate().toString());
                        }
                        line.appendNimegueFn(record.getIndiAge().toString());
                        line.appendNimegueFn(record.getIndiComment().toString());
                        line.appendNimegueFn(record.getIndiOccupation().toString());

                        line.appendNimegueFn(record.getIndiMarriedLastName().toString());
                        line.appendNimegueFn(record.getIndiMarriedFirstName().toString());
                        line.appendNimegueFn(record.getIndiMarriedDead().toString(), record.getIndiMarriedOccupation().toString(), record.getIndiMarriedComment().toString());

                        line.appendNimegueFn(record.getIndiFatherLastName().toString());
                        line.appendNimegueFn(record.getIndiFatherFirstName().toString());
                        line.appendNimegueFn(record.getIndiFatherDead().toString(), record.getIndiFatherComment().toString());
                        line.appendNimegueFn(record.getIndiFatherOccupation().toString());

                        line.appendNimegueFn(record.getIndiMotherLastName().toString());
                        line.appendNimegueFn(record.getIndiMotherFirstName().toString());
                        line.appendNimegueFn(record.getIndiMotherDead().toString(), record.getIndiMotherComment().toString());
                        line.appendNimegueFn(record.getIndiMotherOccupation().toString());

                        line.appendNimegueFn(record.getWifeLastName().toString());
                        line.appendNimegueFn(record.getWifeFirstName().toString());
                        line.appendNimegueFn(record.getWifePlace().toString());
                        if (record.getWifeBirthDate().toString().trim().equals("")){
                            line.appendNimegueFn("          ");
                        } else {
                            line.appendNimegueFn(record.getWifeBirthDate().toString());
                        }
                        line.appendNimegueFn(record.getWifeAge().toString());
                        line.appendNimegueFn(record.getWifeComment().toString());
                        line.appendNimegueFn(record.getWifeOccupation().toString());

                        line.appendNimegueFn(record.getWifeMarriedLastName().toString());
                        line.appendNimegueFn(record.getWifeMarriedFirstName().toString());
                        line.appendNimegueFn(record.getWifeMarriedDead().toString(), record.getWifeMarriedOccupation().toString(), record.getWifeMarriedComment().toString());

                        line.appendNimegueFn(record.getWifeFatherLastName().toString());
                        line.appendNimegueFn(record.getWifeFatherFirstName().toString());
                        line.appendNimegueFn(record.getWifeFatherDead().toString(), record.getWifeFatherComment().toString());
                        line.appendNimegueFn(record.getWifeFatherOccupation().toString());

                        line.appendNimegueFn(record.getWifeMotherLastName().toString());
                        line.appendNimegueFn(record.getWifeMotherFirstName().toString());
                        line.appendNimegueFn(record.getWifeMotherDead().toString(), record.getWifeMotherComment().toString());
                        line.appendNimegueFn(record.getWifeMotherOccupation().toString());

                        line.appendNimegueFn(record.getWitness1LastName().toString());
                        line.appendNimegueFn(record.getWitness1FirstName().toString());
                        line.appendNimegueFn(record.getWitness1Comment().toString(),
                            record.getWitness1Occupation().toString() );

                        line.appendNimegueFn(record.getWitness2LastName().toString());
                        line.appendNimegueFn(record.getWitness2FirstName().toString());
                        line.appendNimegueFn(record.getWitness2Comment().toString(),
                            record.getWitness2Occupation().toString() );

                        line.appendNimegueFn(record.getWitness3LastName().toString());
                        line.appendNimegueFn(record.getWitness3FirstName().toString());
                        line.appendNimegueFn(record.getWitness3Comment().toString(),
                            record.getWitness3Occupation().toString() );

                        line.appendNimegueFn(record.getWitness4LastName().toString());
                        line.appendNimegueFn(record.getWitness4FirstName().toString());
                        line.appendNimegueFn(record.getWitness4Comment().toString(),
                            record.getWitness4Occupation().toString() );

                        line.appendNimegueFn(record.getGeneralComment().toString());
                        line.appendNimegueFn(String.valueOf(record.recordNo)); // numero d'enregistrement

                    } else if ( record instanceof RecordDeath ) {

                        line.appendNimegueFn("NIMEGUEV3");
                        line.appendNimegueFn(dataManager.getCityCode());
                        line.appendNimegueFn(dataManager.getCityName());
                        line.appendNimegueFn(dataManager.getCountryName());
                        line.appendNimegueFn(dataManager.getCountyName());
                        line.appendNimegueFn("D");
                        line.appendNimegueFn(record.getEventDateString());
                        line.appendNimegueFn("");
                        line.appendNimegueFn(record.getCote().toString());
                        line.appendNimegueFn(record.getFreeComment().toString());

                        line.appendNimegueFn(record.getIndiLastName().getValue());
                        line.appendNimegueFn(record.getIndiFirstName().getValue());
                        line.appendNimegueFn(record.getIndiPlace().toString());
                        if (record.getIndiBirthDate().toString().trim().equals("")){
                            line.appendNimegueFn("          ");
                        } else {
                            line.appendNimegueFn(record.getIndiBirthDate().toString());
                        }
                        line.appendNimegueFn(record.getIndiSex().toString());
                        line.appendNimegueFn(record.getIndiAge().toString());
                        line.appendNimegueFn(record.getIndiComment().toString());
                        line.appendNimegueFn(record.getIndiOccupation().toString());

                        line.appendNimegueFn(record.getIndiMarriedLastName().toString());
                        line.appendNimegueFn(record.getIndiMarriedFirstName().toString());
                        line.appendNimegueFn(record.getIndiMarriedDead().toString(), record.getIndiMarriedComment().toString());
                        line.appendNimegueFn(record.getIndiMarriedOccupation().toString());

                        line.appendNimegueFn(record.getIndiFatherLastName().toString());
                        line.appendNimegueFn(record.getIndiFatherFirstName().toString());
                        line.appendNimegueFn(record.getIndiFatherDead().toString(), record.getIndiFatherComment().toString());
                        line.appendNimegueFn(record.getIndiFatherOccupation().toString());

                        line.appendNimegueFn(record.getIndiMotherLastName().toString());
                        line.appendNimegueFn(record.getIndiMotherFirstName().toString());
                        line.appendNimegueFn(record.getIndiMotherDead().toString(), record.getIndiMotherComment().toString());
                        line.appendNimegueFn(record.getIndiMotherOccupation().toString());

                        line.appendNimegueFn(record.getWitness1LastName().toString());
                        line.appendNimegueFn(record.getWitness1FirstName().toString());
                        line.appendNimegueFn(record.getWitness1Comment().toString(),
                            record.getWitness1Occupation().toString() );

                        line.appendNimegueFn(record.getWitness2LastName().toString());
                        line.appendNimegueFn(record.getWitness2FirstName().toString());
                        line.appendNimegueFn(record.getWitness2Comment().toString(),
                            record.getWitness2Occupation().toString() );

                       if (record.getWitness3LastName().isEmpty() && record.getWitness4LastName().isEmpty()) {
                            line.appendNimegueFn(record.getGeneralComment().toString());
                        } else {
                            line.appendNimegueFn(record.getGeneralComment().toString(),
                                    "témoin: " + record.getWitness3FirstName().toString() + " " + record.getWitness3LastName().toString(),
                                    record.getWitness3Occupation().toString(),
                                    record.getWitness3Comment().toString(),
                                    record.getWitness4FirstName().toString() + " " + record.getWitness4LastName().toString(),
                                    record.getWitness4Occupation().toString(),
                                    record.getWitness4Comment().toString());
                        }
                        line.appendNimegueFn(String.valueOf(record.recordNo)); // numero d'enregistrement

                    } else if ( record instanceof RecordMisc ) {

                        line.appendNimegueFn("NIMEGUEV3");
                        line.appendNimegueFn(dataManager.getCityCode());
                        line.appendNimegueFn(dataManager.getCityName());
                        line.appendNimegueFn(dataManager.getCountryName());
                        line.appendNimegueFn(dataManager.getCountyName());
                        line.appendNimegueFn("V");
                        line.appendNimegueFn(record.getEventDateString());
                        line.appendNimegueFn("");
                        line.appendNimegueFn(record.getCote().toString());
                        line.appendNimegueFn(record.getFreeComment().toString());

                        line.appendNimegueFn(record.getEventType().getName());
                        line.appendNimegueFn(record.getEventType().getTag());

                        line.appendNimegueFn(record.getIndiLastName().getValue());
                        line.appendNimegueFn(record.getIndiFirstName().getValue());
                        line.appendNimegueFn(record.getIndiSex().toString());
                        line.appendNimegueFn(record.getIndiPlace().toString());
                        if (record.getIndiBirthDate().toString().trim().equals("")) {
                            line.appendNimegueFn("          ");
                        } else {
                            line.appendNimegueFn(record.getIndiBirthDate().toString());
                        }
                        line.appendNimegueFn(record.getIndiAge().toString());
                        line.appendNimegueFn(record.getIndiComment().toString());
                        line.appendNimegueFn(record.getIndiOccupation().toString());

                        line.appendNimegueFn(record.getIndiMarriedLastName().toString());
                        line.appendNimegueFn(record.getIndiMarriedFirstName().toString());
                        line.appendNimegueFn(record.getIndiMarriedDead().toString(), record.getIndiMarriedOccupation().toString(), record.getIndiMarriedComment().toString());

                        line.appendNimegueFn(record.getIndiFatherLastName().toString());
                        line.appendNimegueFn(record.getIndiFatherFirstName().toString());
                        line.appendNimegueFn(record.getIndiFatherDead().toString(), record.getIndiFatherComment().toString());
                        line.appendNimegueFn(record.getIndiFatherOccupation().toString());
                        line.appendNimegueFn(record.getIndiMotherLastName().toString());
                        line.appendNimegueFn(record.getIndiMotherFirstName().toString());
                        line.appendNimegueFn(record.getIndiMotherDead().toString(), record.getIndiMotherComment().toString());
                        line.appendNimegueFn(record.getIndiMotherOccupation().toString());

                        line.appendNimegueFn(record.getWifeLastName().toString());
                        line.appendNimegueFn(record.getWifeFirstName().toString());
                        line.appendNimegueFn(record.getWifeSex().toString());
                        line.appendNimegueFn(record.getWifePlace().toString());
                        if (record.getWifeBirthDate().toString().trim().equals("")){
                            line.appendNimegueFn("          ");
                        } else {
                            line.appendNimegueFn(record.getWifeBirthDate().toString());
                        }
                        line.appendNimegueFn(record.getWifeAge().toString());
                        line.appendNimegueFn(record.getWifeComment().toString());
                        line.appendNimegueFn(record.getWifeOccupation().toString());

                        line.appendNimegueFn(record.getWifeMarriedLastName().toString());
                        line.appendNimegueFn(record.getWifeMarriedFirstName().toString());
                        line.appendNimegueFn(record.getWifeMarriedDead().toString(), record.getWifeMarriedOccupation().toString(), record.getWifeMarriedComment().toString());
                        
                        line.appendNimegueFn(record.getWifeFatherLastName().toString());
                        line.appendNimegueFn(record.getWifeFatherFirstName().toString());
                        line.appendNimegueFn(record.getWifeFatherDead().toString(), record.getWifeFatherComment().toString());
                        line.appendNimegueFn(record.getWifeFatherOccupation().toString());
                        line.appendNimegueFn(record.getWifeMotherLastName().toString());
                        line.appendNimegueFn(record.getWifeMotherFirstName().toString());
                        line.appendNimegueFn(record.getWifeMotherDead().toString(), record.getWifeMotherComment().toString());
                        line.appendNimegueFn(record.getWifeMotherOccupation().toString());

                        line.appendNimegueFn(record.getWitness1LastName().toString());
                        line.appendNimegueFn(record.getWitness1FirstName().toString());
                        line.appendNimegueFn(record.getWitness1Comment().toString(),
                            record.getWitness1Occupation().toString() );

                        line.appendNimegueFn(record.getWitness2LastName().toString());
                        line.appendNimegueFn(record.getWitness2FirstName().toString());
                        line.appendNimegueFn(record.getWitness2Comment().toString(),
                            record.getWitness2Occupation().toString() );

                        line.appendNimegueFn(record.getWitness3LastName().toString());
                        line.appendNimegueFn(record.getWitness3FirstName().toString());
                        line.appendNimegueFn(record.getWitness3Comment().toString(),
                            record.getWitness3Occupation().toString() );

                        line.appendNimegueFn(record.getWitness4LastName().toString());
                        line.appendNimegueFn(record.getWitness4FirstName().toString());
                        line.appendNimegueFn(record.getWitness4Comment().toString(),
                            record.getWitness4Occupation().toString() );
                        
                        line.appendNimegueFn(record.getGeneralComment().toString());
                        line.appendNimegueFn(String.valueOf(record.recordNo)); // numero d'enregistrement
                    }
                    // Nimegue exige les retours chariot au format Windows CR+LF
                    line.appendNimegue("\r\n");
                    writer.write(line.toString());

                } catch (Exception e) {
                    sb.append("Line ").append(line).append("\n   ");
                    sb.append("Error ").append(e).append("\n");
                    e.printStackTrace();
                }
            }
            writer.close();

        } catch (Exception e) {
            sb.append("Error ").append(e).append("\n");
            e.printStackTrace();
        }
        return sb;
    }

   
}
