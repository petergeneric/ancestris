package ancestris.modules.releve.file;

import ancestris.modules.releve.model.PlaceManager;
import ancestris.modules.releve.model.RecordModel;
import ancestris.modules.releve.model.RecordMisc;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordMarriage;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.file.FileManager.Line;
import ancestris.modules.releve.model.Record.RecordType;
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
                sb.append(fileSignature);
                sb.append(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString(" ")).append(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.EmptyFile"), inputFile.getName()));
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
        indiLastName, indiFirstName, indiBirthPlace, indiBirthDate, indiAge, indiComment, indiOccupation,
        indiMarriedLastName, indiMarriedFirstName, indiMarriedComment,
        indiFatherLastName, indiFatherFirstName, indiFatherComment, indiFatherOccupation,
        indiMotherLastName, indiMotherFirstName, indiMotherComment, indiMotherOccupation,
        wifeLastName, wifeFirstName, wifeBirthPlace, wifeBirthDate, wifeAge, wifeComment, wifeOccupation,
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
        indiLastName, indiFirstName, indiBirthPlace, indiBirthDate, indiSex, indiAge, indiComment, indiOccupation,
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
        indiLastName, indiFirstName, indiSex, indiBirthPlace, indiBirthDate, indiAge, indiComment, indiOccupation,
        indiMarriedLastName, indiMarriedFirstName, indiMarriedComment,
        indiFatherLastName, indiFatherFirstName, indiFatherComment, indiFatherOccupation,
        indiMotherLastName, indiMotherFirstName, indiMotherComment, indiMotherOccupation,
        wifeLastName, wifeFirstName, wifeSex, wifeBirthPlace, wifeBirthDate, wifeAge, wifeComment, wifeOccupation,
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
            String strLine;
            int lineNumber = 0;

            //read comma separated file line by line
            while ((strLine = br.readLine()) != null) {
                lineNumber++;

                try {

                    String[] fields = splitLine(strLine, lineNumber);

                    if (fields == null) {
                        continue;
                    }

                    if ( lineNumber == 1) {
                        fileBuffer.setRegisterInfoPlace(
                                fields[BirthField.nomCommune.ordinal()],
                                fields[BirthField.codeCommune.ordinal()],
                                fields[BirthField.nomDepartement.ordinal()].isEmpty() ? fields[BirthField.codeDepartement.ordinal()] : fields[BirthField.nomDepartement.ordinal()],
                                "", // stateName
                                ""  // contryName
                                );
                    }

                    if (fields[BirthField.typeActe.ordinal()].equals("N")) {
                        RecordBirth record = new RecordBirth();

                        record.setEventDate(fields[BirthField.dateNaissance.ordinal()]);
                        //record.dateIncomplete = fields[BirthField.dateIncomplete.ordinal()];

                        record.setCote(fields[BirthField.cote.ordinal()]);
                        record.setFreeComment(fields[BirthField.photo.ordinal()]);
                        record.getIndi().set(
                                fields[BirthField.indiFirstName.ordinal()],
                                fields[BirthField.indiLastName.ordinal()],
                                fields[BirthField.indiSex.ordinal()],
                                "" , // age
                                "" , // date naissance
                                "" , // place
                                "", // pas d'adresse dans ce format
                                "", // pas de profession a la naissance
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[BirthField.indiComment.ordinal()]);

                        record.getIndi().setFather(
                                fields[BirthField.indiFatherFirstName.ordinal()],
                                fields[BirthField.indiFatherLastName.ordinal()],
                                fields[BirthField.indiFatherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[BirthField.indiFatherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.getIndi().setMother(
                                fields[BirthField.indiMotherFirstName.ordinal()],
                                fields[BirthField.indiMotherLastName.ordinal()],
                                fields[BirthField.indiMotherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[BirthField.indiMotherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.getWitness1().setValue(
                                fields[BirthField.witness1FirstName.ordinal()],
                                fields[BirthField.witness1LastName.ordinal()],
                                "",
                                fields[BirthField.witness1Comment.ordinal()]);

                        record.getWitness2().setValue(
                                fields[BirthField.witness2FirstName.ordinal()],
                                fields[BirthField.witness2LastName.ordinal()],
                                "",
                                fields[BirthField.witness2Comment.ordinal()]);

                        record.setGeneralComment(fields[BirthField.generalComment.ordinal()]);

                        fileBuffer.addRecord(record);

                    } else if (fields[MarrField.typeActe.ordinal()].equals("M")) {
                        RecordMarriage record = new RecordMarriage();

                        record.setEventDate(fields[MarrField.eventDate.ordinal()]);
                        //record.dateIncomplete = fields[MarrField.dateIncomplete.ordinal()];

                        record.setCote(fields[MarrField.cote.ordinal()]);
                        record.setFreeComment(fields[MarrField.libre.ordinal()]);

                        record.getIndi().set(
                                fields[MarrField.indiFirstName.ordinal()],
                                fields[MarrField.indiLastName.ordinal()],
                                "M",
                                fields[MarrField.indiAge.ordinal()],
                                fields[MarrField.indiBirthDate.ordinal()],
                                fields[MarrField.indiBirthPlace.ordinal()],
                                "", // pas d'adresse dans ce format
                                fields[MarrField.indiOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MarrField.indiComment.ordinal()]);

                        record.getIndi().setMarried(
                                fields[MarrField.indiMarriedFirstName.ordinal()],
                                fields[MarrField.indiMarriedLastName.ordinal()],
                                //"F",
                                "", // pas de profession dans ce format
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MarrField.indiMarriedComment.ordinal()],
                                "");  //décédé

                        record.getIndi().setFather(
                                fields[MarrField.indiFatherFirstName.ordinal()],
                                fields[MarrField.indiFatherLastName.ordinal()],
                                fields[MarrField.indiFatherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MarrField.indiFatherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.getIndi().setMother(
                                fields[MarrField.indiMotherFirstName.ordinal()],
                                fields[MarrField.indiMotherLastName.ordinal()],
                                fields[MarrField.indiMotherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MarrField.indiMotherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.getWife().set(
                                fields[MarrField.wifeFirstName.ordinal()],
                                fields[MarrField.wifeLastName.ordinal()],
                                "F",
                                fields[MarrField.wifeAge.ordinal()],
                                fields[MarrField.wifeBirthDate.ordinal()],
                                fields[MarrField.wifeBirthPlace.ordinal()],
                                "", // pas d'adresse dans ce format
                                fields[MarrField.wifeOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MarrField.wifeComment.ordinal()]); 

                        record.getWife().setMarried(
                                fields[MarrField.wifeMarriedFirstName.ordinal()],
                                fields[MarrField.wifeMarriedLastName.ordinal()],
                                //M",
                                "",
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MarrField.wifeMarriedComment.ordinal()],
                                "");  //décédé

                        record.getWife().setFather(
                                fields[MarrField.wifeFatherFirstName.ordinal()],
                                fields[MarrField.wifeFatherLastName.ordinal()],
                                fields[MarrField.wifeFatherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MarrField.wifeFatherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.getWife().setMother(
                                fields[MarrField.wifeMotherFirstName.ordinal()],
                                fields[MarrField.wifeMotherLastName.ordinal()],
                                fields[MarrField.wifeMotherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MarrField.wifeMotherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        int ordinal= MarrField.witness1FirstName.ordinal();
                        for(Record.Witness witness : record.getWitnesses()) {
                            witness.setValue(fields[ordinal++], fields[ordinal++], "", fields[ordinal++]);
                        }

                        record.setGeneralComment(fields[MarrField.generalComment.ordinal()]);

                        fileBuffer.addRecord(record);

                    } else if (fields[DeathField.typeActe.ordinal()].equals("D")) {
                        RecordDeath record = new RecordDeath();

                        record.setEventDate(fields[DeathField.eventDate.ordinal()]);
                        //record.dateIncomplete = fields[DeathField.dateIncomplete.ordinal()];

                        record.setCote(fields[DeathField.cote.ordinal()]);
                        record.setFreeComment(fields[DeathField.libre.ordinal()]);

                        record.getIndi().set(
                                fields[DeathField.indiFirstName.ordinal()],
                                fields[DeathField.indiLastName.ordinal()],
                                fields[DeathField.indiSex.ordinal()],
                                fields[DeathField.indiAge.ordinal()],
                                fields[DeathField.indiBirthDate.ordinal()],
                                fields[DeathField.indiBirthPlace.ordinal()],
                                "", // pas d'adresse dans ce format
                                fields[DeathField.indiOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[DeathField.indiComment.ordinal()]);

                        record.getIndi().setMarried(
                                fields[DeathField.wifeFirstName.ordinal()],
                                fields[DeathField.wifeLastName.ordinal()],
                                //fields[DeathField.indiSex.ordinal()].equals("M") ? "F" : "M",
                                fields[DeathField.wifeOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[DeathField.wifeComment.ordinal()],
                                "");  //décédé

                        record.getIndi().setFather(
                                fields[DeathField.indiFatherFirstName.ordinal()],
                                fields[DeathField.indiFatherLastName.ordinal()],
                                fields[DeathField.indiFatherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[DeathField.indiFatherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.getIndi().setMother(
                                fields[DeathField.indiMotherFirstName.ordinal()],
                                fields[DeathField.indiMotherLastName.ordinal()],
                                fields[DeathField.indiMotherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[DeathField.indiMotherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.getWitness1().setValue(
                                fields[DeathField.witness1FirstName.ordinal()],
                                fields[DeathField.witness1LastName.ordinal()],
                                "",
                                fields[DeathField.witness1Comment.ordinal()]);

                        record.getWitness2().setValue(
                                fields[DeathField.witness2FirstName.ordinal()],
                                fields[DeathField.witness2LastName.ordinal()],
                                "",
                                fields[DeathField.witness2Comment.ordinal()]);

                        record.setGeneralComment(fields[DeathField.generalComment.ordinal()]);
                        fileBuffer.addRecord(record);
                    } else if (fields[MiscField.typeActe.ordinal()].equals("V")) {
                        RecordMisc record = new RecordMisc();

                        record.setEventDate(fields[MiscField.eventDate.ordinal()]);
                        //record.dateIncomplete = fields[MiscField.dateIncomplete.ordinal()];
                        record.setEventType(
                                fields[MiscField.eventTypeName.ordinal()]);

                        record.setCote(fields[MiscField.cote.ordinal()]);
                        record.setFreeComment(fields[MiscField.libre.ordinal()]);

                        record.getIndi().set(
                                fields[MiscField.indiFirstName.ordinal()],
                                fields[MiscField.indiLastName.ordinal()],
                                fields[MiscField.indiSex.ordinal()],
                                fields[MiscField.indiAge.ordinal()],
                                fields[MiscField.indiBirthDate.ordinal()],
                                fields[MiscField.indiBirthPlace.ordinal()],
                                "", // pas d'adresse dans ce format
                                fields[MiscField.indiOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MiscField.indiComment.ordinal()]);

                        record.getIndi().setMarried(
                                fields[MiscField.indiMarriedFirstName.ordinal()],
                                fields[MiscField.indiMarriedLastName.ordinal()],
                                //fields[MiscField.indiSex.ordinal()].equals("M") ? "F" : "M",
                                "", // profession
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MiscField.indiMarriedComment.ordinal()],
                                "");  //décédé

                        record.getIndi().setFather(
                                fields[MiscField.indiFatherFirstName.ordinal()],
                                fields[MiscField.indiFatherLastName.ordinal()],
                                fields[MiscField.indiFatherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MiscField.indiFatherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.getIndi().setMother(
                                fields[MiscField.indiMotherFirstName.ordinal()],
                                fields[MiscField.indiMotherLastName.ordinal()],
                                fields[MiscField.indiMotherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MiscField.indiMotherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.getWife().set(
                                fields[MiscField.wifeFirstName.ordinal()],
                                fields[MiscField.wifeLastName.ordinal()],
                                fields[MiscField.wifeSex.ordinal()],
                                fields[MiscField.wifeAge.ordinal()],
                                fields[MiscField.wifeBirthDate.ordinal()],
                                fields[MiscField.wifeBirthPlace.ordinal()],
                                "", // pas d'adresse dans ce format
                                fields[MiscField.wifeOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MiscField.wifeComment.ordinal()]); 

                        record.getWife().setMarried(
                                fields[MiscField.wifeMarriedFirstName.ordinal()],
                                fields[MiscField.wifeMarriedLastName.ordinal()],
                                //fields[MiscField.indiSex.ordinal()].equals("M") ? "F" : "M",
                                "", // profession
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MiscField.wifeMarriedComment.ordinal()],
                                "");  //décédé

                        record.getWife().setFather(
                                fields[MiscField.wifeFatherFirstName.ordinal()],
                                fields[MiscField.wifeFatherLastName.ordinal()],
                                fields[MiscField.wifeFatherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MiscField.wifeFatherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.getWife().setMother(
                                fields[MiscField.wifeMotherFirstName.ordinal()],
                                fields[MiscField.wifeMotherLastName.ordinal()],
                                fields[MiscField.wifeMotherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MiscField.wifeMotherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.getWitness1().setValue(
                                fields[MiscField.witness1FirstName.ordinal()],
                                fields[MiscField.witness1LastName.ordinal()],
                                "", // profession
                                fields[MiscField.witness1Comment.ordinal()]);

                        record.getWitness2().setValue(
                                fields[MiscField.witness2FirstName.ordinal()],
                                fields[MiscField.witness2LastName.ordinal()],
                                "", // profession
                                fields[MiscField.witness2Comment.ordinal()]);

                        record.getWitness3().setValue(
                                fields[MiscField.witness3FirstName.ordinal()],
                                fields[MiscField.witness3LastName.ordinal()],
                                "", // profession
                                fields[MiscField.witness3Comment.ordinal()]);

                        record.getWitness4().setValue(
                                fields[MiscField.witness4FirstName.ordinal()],
                                fields[MiscField.witness4LastName.ordinal()],
                                "", // profession
                                fields[MiscField.witness4Comment.ordinal()]);

                        record.setGeneralComment(fields[MiscField.generalComment.ordinal()]);
                        fileBuffer.addRecord(record);
                        
                    } else {
                        fileBuffer.append(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.LineNo"), lineNumber ));
                        fileBuffer.append("\n");
                        fileBuffer.append(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.UnknownEventType")).append(" ");
                        fileBuffer.append(fields[BirthField.typeActe.ordinal()]).append("\n");
                    }
                } catch (Exception e) {
                    fileBuffer.append(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.LineNo"), lineNumber ));
                    fileBuffer.append("\n");
                    fileBuffer.append(strLine).append("\n");
                    e.printStackTrace(System.err);
                }
            } // for
            br.close();
        } catch (IOException e) {
            fileBuffer.append(e.toString()).append("\n");
        }

        return fileBuffer;
    }

    /**
     *
     * @param fileName
     * TODO gérer la dat iincomplete
     */
    public static StringBuilder saveFile(PlaceManager placeManager, RecordModel recordModel, RecordType recordType, File fileName, boolean append) {
        StringBuilder sb = new StringBuilder();
        try {
            //create BufferedReader to read csv file
            FileWriter writer = new FileWriter(fileName, append);
            for (int index = 0; index < recordModel.getRowCount(); index++) {
                Record record = recordModel.getRecord(index);
                if( recordType != null && recordType != record.getType()) {
                    continue;
                }
                Line line = new Line(fieldSeparator);
                try {
                    if ( record instanceof RecordBirth ) {
                        line.appendNimegueFn("NIMEGUEV3");
                        line.appendNimegueFn(placeManager.getCityCode());
                        line.appendNimegueFn(placeManager.getCityName());
                        line.appendNimegueFn(placeManager.getCountryName());
                        line.appendNimegueFn(placeManager.getCountyName());
                        line.appendNimegueFn("N");
                        line.appendNimegueFn(record.getEventDateString());
                        line.appendNimegueFn("");
                        line.appendNimegueFn(record.getCote().toString());
                        line.appendNimegueFn(record.getFreeComment().toString());
                        line.appendNimegueFn(record.getIndi().getLastName().toString());
                        line.appendNimegueFn(record.getIndi().getFirstName().toString());
                        line.appendNimegueFn(record.getIndi().getSex().toString());
                        String birthPlace = "";
                        if (!record.getIndi().getBirthPlace().toString().isEmpty() ) {
                            birthPlace = "Lieu: "+record.getIndi().getBirthPlace().toString();
                        }
                        line.appendNimegueFn(birthPlace, record.getIndi().getComment() .toString());

                        line.appendNimegueFn(record.getIndi().getFatherLastName().toString());
                        line.appendNimegueFn(record.getIndi().getFatherFirstName().toString());
                        line.appendNimegueFn(record.getIndi().getFatherAge().getValue(),
                                record.getIndi().getFatherDead().toString(),
                                record.getIndi().getFatherResidence()!=null? record.getIndi().getFatherResidence().toString() : "",
                                record.getIndi().getFatherComment().toString() );
                        line.appendNimegueFn(record.getIndi().getFatherOccupation().toString());

                        line.appendNimegueFn(record.getIndi().getMotherLastName().toString());
                        line.appendNimegueFn(record.getIndi().getMotherFirstName().toString());
                        line.appendNimegueFn(record.getIndi().getMotherAge().getValue(),
                                record.getIndi().getMotherDead().toString(),
                                record.getIndi().getMotherResidence() != null ? record.getIndi().getMotherResidence().toString() : "",
                                record.getIndi().getMotherComment().toString());
                        line.appendNimegueFn(record.getIndi().getMotherOccupation().toString());

                        line.appendNimegueFn(record.getWitness1().getLastName().toString());
                        line.appendNimegueFn(record.getWitness1().getFirstName().toString());
                        line.appendNimegueFn(record.getWitness1().getComment().toString(),
                            record.getWitness1().getOccupation().toString() );

                        line.appendNimegueFn(record.getWitness2().getLastName().toString());
                        line.appendNimegueFn(record.getWitness2().getFirstName().toString());
                        line.appendNimegueFn(record.getWitness2().getComment().toString(),
                            record.getWitness2().getOccupation().toString() );

                        if (record.getWitness3().getLastName().isEmpty() && record.getWitness4().getLastName().isEmpty()) {
                            line.appendNimegueFn(record.getGeneralComment().toString());
                        } else {
                            line.appendNimegueFn(record.getGeneralComment().toString(),
                                    "témoin: " + record.getWitness3().getFirstName().toString() + " " + record.getWitness3().getLastName().toString(),
                                    record.getWitness3().getOccupation().toString(),
                                    record.getWitness3().getComment().toString(),
                                    record.getWitness4().getFirstName().toString() + " " + record.getWitness4().getLastName().toString(),
                                    record.getWitness4().getOccupation().toString(),
                                    record.getWitness4().getComment().toString());
                        }
                        line.appendNimegueFn(String.valueOf(index)); // numero d'enregistrement

                    } if ( record instanceof RecordMarriage ) {

                        line.appendNimegueFn("NIMEGUEV3");
                        line.appendNimegueFn(placeManager.getCityCode());
                        line.appendNimegueFn(placeManager.getCityName());
                        line.appendNimegueFn(placeManager.getCountryName());
                        line.appendNimegueFn(placeManager.getCountyName());
                        line.appendNimegueFn("M");
                        line.appendNimegueFn(record.getEventDateString());
                        line.appendNimegueFn("");
                        line.appendNimegueFn(record.getCote().toString());
                        line.appendNimegueFn(record.getFreeComment().toString());

                        line.appendNimegueFn(record.getIndi().getLastName().getValue());
                        line.appendNimegueFn(record.getIndi().getFirstName().getValue());
                        line.appendNimegueFn(record.getIndi().getBirthPlace().toString());
                        if (record.getIndi().getBirthDate().toString().trim().equals("")) {
                            line.appendNimegueFn("          ");
                        } else {
                            line.appendNimegueFn(record.getIndi().getBirthDate().toString());
                        }
                        line.appendNimegueFn(record.getIndi().getAge().getValue());
                        line.appendNimegueFn(
                                record.getIndi().getComment() .toString(),
                                record.getIndi().getResidence() .toString()
                                );
                        line.appendNimegueFn(record.getIndi().getOccupation().toString());

                        line.appendNimegueFn(record.getIndi().getMarriedLastName().toString());
                        line.appendNimegueFn(record.getIndi().getMarriedFirstName().toString());
                        line.appendNimegueFn(record.getIndi().getMarriedDead().toString(), 
                                record.getIndi().getMarriedOccupation().toString(),
                                record.getIndi().getMarriedResidence().toString(),
                                record.getIndi().getMarriedComment().toString());

                        line.appendNimegueFn(record.getIndi().getFatherLastName().toString());
                        line.appendNimegueFn(record.getIndi().getFatherFirstName().toString());
                        line.appendNimegueFn(record.getIndi().getFatherAge().getValue(),
                                record.getIndi().getFatherDead().toString(),
                                record.getIndi().getFatherResidence().toString(),
                                record.getIndi().getFatherComment().toString());
                        line.appendNimegueFn(record.getIndi().getFatherOccupation().toString());

                        line.appendNimegueFn(record.getIndi().getMotherLastName().toString());
                        line.appendNimegueFn(record.getIndi().getMotherFirstName().toString());
                        line.appendNimegueFn(record.getIndi().getMotherAge().getValue(),
                                record.getIndi().getMotherDead().toString(),
                                record.getIndi().getMotherResidence().toString(),
                                record.getIndi().getMotherComment().toString());
                        line.appendNimegueFn(record.getIndi().getMotherOccupation().toString());

                        line.appendNimegueFn(record.getWife().getLastName().toString());
                        line.appendNimegueFn(record.getWife().getFirstName().toString());
                        line.appendNimegueFn(record.getWife().getBirthPlace().toString());
                        if (record.getWife().getBirthDate().toString().trim().equals("")){
                            line.appendNimegueFn("          ");
                        } else {
                            line.appendNimegueFn(record.getWife().getBirthDate().toString());
                        }
                        line.appendNimegueFn(record.getWife().getAge().getValue());
                        line.appendNimegueFn(
                                record.getWife().getResidence().toString(),
                                record.getWife().getComment().toString()
                                );
                        line.appendNimegueFn(record.getWife().getOccupation().toString());

                        line.appendNimegueFn(record.getWife().getMarriedLastName().toString());
                        line.appendNimegueFn(record.getWife().getMarriedFirstName().toString());
                        line.appendNimegueFn(
                                record.getWife().getMarriedDead().toString(), 
                                record.getWife().getMarriedOccupation().toString(), 
                                record.getWife().getMarriedResidence().toString(), 
                                record.getWife().getMarriedComment().toString()
                                );

                        line.appendNimegueFn(record.getWife().getFatherLastName().toString());
                        line.appendNimegueFn(record.getWife().getFatherFirstName().toString());
                        line.appendNimegueFn(record.getWife().getFatherAge().getValue(),
                                record.getWife().getFatherDead().toString(),
                                record.getWife().getFatherResidence().toString(),
                                record.getWife().getFatherComment().toString());
                        line.appendNimegueFn(record.getWife().getFatherOccupation().toString());

                        line.appendNimegueFn(record.getWife().getMotherLastName().toString());
                        line.appendNimegueFn(record.getWife().getMotherFirstName().toString());
                        line.appendNimegueFn(record.getWife().getMotherAge().getValue(),
                                record.getWife().getMotherDead().toString(),
                                record.getWife().getMotherResidence().toString(),
                                record.getWife().getMotherComment().toString());
                        line.appendNimegueFn(record.getWife().getMotherOccupation().toString());

                        line.appendNimegueFn(record.getWitness1().getLastName().toString());
                        line.appendNimegueFn(record.getWitness1().getFirstName().toString());
                        line.appendNimegueFn(
                                record.getWitness1().getComment().toString(),
                                record.getWitness1().getOccupation().toString() 
                                );

                        line.appendNimegueFn(record.getWitness2().getLastName().toString());
                        line.appendNimegueFn(record.getWitness2().getFirstName().toString());
                        line.appendNimegueFn(record.getWitness2().getComment().toString(),
                            record.getWitness2().getOccupation().toString() );

                        line.appendNimegueFn(record.getWitness3().getLastName().toString());
                        line.appendNimegueFn(record.getWitness3().getFirstName().toString());
                        line.appendNimegueFn(record.getWitness3().getComment().toString(),
                            record.getWitness3().getOccupation().toString() );

                        line.appendNimegueFn(record.getWitness4().getLastName().toString());
                        line.appendNimegueFn(record.getWitness4().getFirstName().toString());
                        line.appendNimegueFn(record.getWitness4().getComment().toString(),
                            record.getWitness4().getOccupation().toString() );

                        line.appendNimegueFn(record.getGeneralComment().toString());
                        line.appendNimegueFn(String.valueOf(index)); // numero d'enregistrement

                    } else if ( record instanceof RecordDeath ) {

                        line.appendNimegueFn("NIMEGUEV3");
                        line.appendNimegueFn(placeManager.getCityCode());
                        line.appendNimegueFn(placeManager.getCityName());
                        line.appendNimegueFn(placeManager.getCountryName());
                        line.appendNimegueFn(placeManager.getCountyName());
                        line.appendNimegueFn("D");
                        line.appendNimegueFn(record.getEventDateString());
                        line.appendNimegueFn("");
                        line.appendNimegueFn(record.getCote().toString());
                        line.appendNimegueFn(record.getFreeComment().toString());

                        line.appendNimegueFn(record.getIndi().getLastName().getValue());
                        line.appendNimegueFn(record.getIndi().getFirstName().getValue());
                        line.appendNimegueFn(record.getIndi().getBirthPlace().toString());
                        if (record.getIndi().getBirthDate().toString().trim().equals("")){
                            line.appendNimegueFn("          ");
                        } else {
                            line.appendNimegueFn(record.getIndi().getBirthDate().toString());
                        }
                        line.appendNimegueFn(record.getIndi().getSex().toString());
                        line.appendNimegueFn(record.getIndi().getAge().getValue());
                        line.appendNimegueFn(
                                record.getIndi().getResidence() .toString(),
                                record.getIndi().getComment() .toString()
                                );
                        line.appendNimegueFn(record.getIndi().getOccupation().toString());

                        line.appendNimegueFn(record.getIndi().getMarriedLastName().toString());
                        line.appendNimegueFn(record.getIndi().getMarriedFirstName().toString());
                        line.appendNimegueFn(
                                record.getIndi().getMarriedDead().toString(), 
                                record.getIndi().getMarriedResidence().toString(), 
                                record.getIndi().getMarriedComment().toString()
                                );
                        line.appendNimegueFn(record.getIndi().getMarriedOccupation().toString());

                        line.appendNimegueFn(record.getIndi().getFatherLastName().toString());
                        line.appendNimegueFn(record.getIndi().getFatherFirstName().toString());
                        line.appendNimegueFn(record.getIndi().getFatherAge().getValue(),
                                record.getIndi().getFatherDead().toString(),
                                record.getIndi().getFatherResidence().toString(),
                                record.getIndi().getFatherComment().toString()
                                );
                        line.appendNimegueFn(record.getIndi().getFatherOccupation().toString());

                        line.appendNimegueFn(record.getIndi().getMotherLastName().toString());
                        line.appendNimegueFn(record.getIndi().getMotherFirstName().toString());
                        line.appendNimegueFn(record.getIndi().getMotherAge().getValue(),
                                record.getIndi().getMotherDead().toString(),
                                record.getIndi().getMotherResidence().toString(),
                                record.getIndi().getMotherComment().toString()
                                );
                        line.appendNimegueFn(record.getIndi().getMotherOccupation().toString());

                        line.appendNimegueFn(record.getWitness1().getLastName().toString());
                        line.appendNimegueFn(record.getWitness1().getFirstName().toString());
                        line.appendNimegueFn(record.getWitness1().getComment().toString(),
                            record.getWitness1().getOccupation().toString() );

                        line.appendNimegueFn(record.getWitness2().getLastName().toString());
                        line.appendNimegueFn(record.getWitness2().getFirstName().toString());
                        line.appendNimegueFn(record.getWitness2().getComment().toString(),
                            record.getWitness2().getOccupation().toString() );

                       if (record.getWitness3().getLastName().isEmpty() && record.getWitness4().getLastName().isEmpty()) {
                            line.appendNimegueFn(record.getGeneralComment().toString());
                        } else {
                            line.appendNimegueFn(record.getGeneralComment().toString(),
                                    "témoin: " + record.getWitness3().getFirstName().toString() + " " + record.getWitness3().getLastName().toString(),
                                    record.getWitness3().getOccupation().toString(),
                                    record.getWitness3().getComment().toString(),
                                    record.getWitness4().getFirstName().toString() + " " + record.getWitness4().getLastName().toString(),
                                    record.getWitness4().getOccupation().toString(),
                                    record.getWitness4().getComment().toString());
                        }
                        line.appendNimegueFn(String.valueOf(index)); // numero d'enregistrement

                    } else if ( record instanceof RecordMisc ) {

                        line.appendNimegueFn("NIMEGUEV3");
                        line.appendNimegueFn(placeManager.getCityCode());
                        line.appendNimegueFn(placeManager.getCityName());
                        line.appendNimegueFn(placeManager.getCountryName());
                        line.appendNimegueFn(placeManager.getCountyName());
                        line.appendNimegueFn("V");
                        line.appendNimegueFn(record.getEventDateString());
                        line.appendNimegueFn("");
                        line.appendNimegueFn(record.getCote().toString());
                        line.appendNimegueFn(record.getFreeComment().toString());

                        line.appendNimegueFn(record.getEventType().getName());
                        line.appendNimegueFn(""); // EventTypeTag
                        
                        line.appendNimegueFn(record.getIndi().getLastName().getValue());
                        line.appendNimegueFn(record.getIndi().getFirstName().getValue());
                        line.appendNimegueFn(record.getIndi().getSex().toString());
                        line.appendNimegueFn(record.getIndi().getBirthPlace().toString());
                        if (record.getIndi().getBirthDate().toString().trim().equals("")) {
                            line.appendNimegueFn("          ");
                        } else {
                            line.appendNimegueFn(record.getIndi().getBirthDate().toString());
                        }
                        line.appendNimegueFn(record.getIndi().getAge().getValue());
                        line.appendNimegueFn(
                                record.getIndi().getResidence() .toString(),
                                record.getIndi().getComment() .toString()
                                );
                        line.appendNimegueFn(record.getIndi().getOccupation().toString());

                        line.appendNimegueFn(record.getIndi().getMarriedLastName().toString());
                        line.appendNimegueFn(record.getIndi().getMarriedFirstName().toString());
                        line.appendNimegueFn(
                                record.getIndi().getMarriedDead().toString(), 
                                record.getIndi().getMarriedOccupation().toString(), 
                                record.getIndi().getMarriedResidence().toString(), 
                                record.getIndi().getMarriedComment().toString()
                                );

                        line.appendNimegueFn(record.getIndi().getFatherLastName().toString());
                        line.appendNimegueFn(record.getIndi().getFatherFirstName().toString());
                        line.appendNimegueFn(record.getIndi().getFatherAge().getValue(),
                                record.getIndi().getFatherDead().toString(),
                                record.getIndi().getFatherResidence().toString(),
                                record.getIndi().getFatherComment().toString()
                                );
                        line.appendNimegueFn(record.getIndi().getFatherOccupation().toString());
                        line.appendNimegueFn(record.getIndi().getMotherLastName().toString());
                        line.appendNimegueFn(record.getIndi().getMotherFirstName().toString());
                        line.appendNimegueFn(record.getIndi().getMotherAge().getValue(),
                                record.getIndi().getMotherDead().toString(),
                                record.getIndi().getMotherResidence().toString(),
                                record.getIndi().getMotherComment().toString());
                        line.appendNimegueFn(record.getIndi().getMotherOccupation().toString());

                        line.appendNimegueFn(record.getWife().getLastName().toString());
                        line.appendNimegueFn(record.getWife().getFirstName().toString());
                        line.appendNimegueFn(record.getWife().getSex().toString());
                        line.appendNimegueFn(record.getWife().getBirthPlace().toString());
                        if (record.getWife().getBirthDate().toString().trim().equals("")){
                            line.appendNimegueFn("          ");
                        } else {
                            line.appendNimegueFn(record.getWife().getBirthDate().toString());
                        }
                        line.appendNimegueFn(record.getWife().getAge().getValue());
                        line.appendNimegueFn(
                                record.getWife().getResidence().toString(),
                                record.getWife().getComment().toString()
                                );
                        line.appendNimegueFn(record.getWife().getOccupation().toString());

                        line.appendNimegueFn(record.getWife().getMarriedLastName().toString());
                        line.appendNimegueFn(record.getWife().getMarriedFirstName().toString());
                        line.appendNimegueFn(
                                record.getWife().getMarriedDead().toString(), 
                                record.getWife().getMarriedOccupation().toString(), 
                                record.getWife().getMarriedResidence().toString(), 
                                record.getWife().getMarriedComment().toString()
                                );
                        
                        line.appendNimegueFn(record.getWife().getFatherLastName().toString());
                        line.appendNimegueFn(record.getWife().getFatherFirstName().toString());
                        line.appendNimegueFn(record.getWife().getFatherAge().getValue(),
                                record.getWife().getFatherDead().toString(),
                                record.getWife().getFatherResidence().toString(),
                                record.getWife().getFatherComment().toString()
                                );
                        line.appendNimegueFn(record.getWife().getFatherOccupation().toString());
                        line.appendNimegueFn(record.getWife().getMotherLastName().toString());
                        line.appendNimegueFn(record.getWife().getMotherFirstName().toString());
                        line.appendNimegueFn(
                                record.getWife().getMotherAge().getValue(),
                                record.getWife().getMotherDead().toString(),
                                record.getWife().getMotherResidence().toString(),
                                record.getWife().getMotherComment().toString()
                                );
                        line.appendNimegueFn(record.getWife().getMotherOccupation().toString());

                        line.appendNimegueFn(record.getWitness1().getLastName().toString());
                        line.appendNimegueFn(record.getWitness1().getFirstName().toString());
                        line.appendNimegueFn(record.getWitness1().getComment().toString(),
                            record.getWitness1().getOccupation().toString() );

                        line.appendNimegueFn(record.getWitness2().getLastName().toString());
                        line.appendNimegueFn(record.getWitness2().getFirstName().toString());
                        line.appendNimegueFn(record.getWitness2().getComment().toString(),
                            record.getWitness2().getOccupation().toString() );

                        line.appendNimegueFn(record.getWitness3().getLastName().toString());
                        line.appendNimegueFn(record.getWitness3().getFirstName().toString());
                        line.appendNimegueFn(record.getWitness3().getComment().toString(),
                            record.getWitness3().getOccupation().toString() );

                        line.appendNimegueFn(record.getWitness4().getLastName().toString());
                        line.appendNimegueFn(record.getWitness4().getFirstName().toString());
                        line.appendNimegueFn(record.getWitness4().getComment().toString(),
                            record.getWitness4().getOccupation().toString() );
                        
                        line.appendNimegueFn(record.getGeneralComment().toString());
                        line.appendNimegueFn(String.valueOf(index)); // numero d'enregistrement
                    }
                    // Nimegue exige les retours chariot au format Windows CR+LF
                    line.appendNimegue("\r\n");
                    writer.write(line.toString());

                } catch (IOException e) {
                    sb.append(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.LineNo"), index ));
                    sb.append("\n");
                    sb.append(line).append("\n   ");
                    sb.append(e.getMessage()).append("\n");
                    e.printStackTrace(System.err);
                }
            }
            writer.close();

        } catch (IOException e) {
            sb.append(e.getMessage()).append("\n");
            e.printStackTrace(System.err);
        }
        return sb;
    }

   
}
