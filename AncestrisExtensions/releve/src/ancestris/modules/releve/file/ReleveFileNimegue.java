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
                throw new Exception(String.format("Line %d: eventType=%s, Must be N, M, D or V", lineNo, fields[BirthField.typeActe.ordinal()]));
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

                        record.setFieldValue(FieldType.eventDate, fields[BirthField.dateNaissance.ordinal()]);
                        //record.dateIncomplete = fields[BirthField.dateIncomplete.ordinal()];

                        record.setFieldValue(FieldType.cote, fields[BirthField.cote.ordinal()]);
                        record.setFieldValue(FieldType.freeComment,  fields[BirthField.photo.ordinal()]);
                        record.setIndi(
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

                        record.setIndiFather(
                                fields[BirthField.indiFatherFirstName.ordinal()],
                                fields[BirthField.indiFatherLastName.ordinal()],
                                fields[BirthField.indiFatherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[BirthField.indiFatherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.setIndiMother(
                                fields[BirthField.indiMotherFirstName.ordinal()],
                                fields[BirthField.indiMotherLastName.ordinal()],
                                fields[BirthField.indiMotherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[BirthField.indiMotherComment.ordinal()],
                                "",   //décédé
                                "");  //age

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

                        record.setFieldValue(FieldType.generalComment, fields[BirthField.generalComment.ordinal()]);

                        fileBuffer.addRecord(record);

                    } else if (fields[MarrField.typeActe.ordinal()].equals("M")) {
                        RecordMarriage record = new RecordMarriage();

                        record.setFieldValue(FieldType.eventDate, fields[MarrField.eventDate.ordinal()]);
                        //record.dateIncomplete = fields[MarrField.dateIncomplete.ordinal()];

                        record.setFieldValue(FieldType.cote, fields[MarrField.cote.ordinal()]);
                        record.setFieldValue(FieldType.freeComment,  fields[MarrField.libre.ordinal()]);

                        record.setIndi(
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

                        record.setIndiMarried(
                                fields[MarrField.indiMarriedFirstName.ordinal()],
                                fields[MarrField.indiMarriedLastName.ordinal()],
                                //"F",
                                "", // pas de profession dans ce format
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MarrField.indiMarriedComment.ordinal()],
                                "");  //décédé

                        record.setIndiFather(
                                fields[MarrField.indiFatherFirstName.ordinal()],
                                fields[MarrField.indiFatherLastName.ordinal()],
                                fields[MarrField.indiFatherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MarrField.indiFatherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.setIndiMother(
                                fields[MarrField.indiMotherFirstName.ordinal()],
                                fields[MarrField.indiMotherLastName.ordinal()],
                                fields[MarrField.indiMotherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MarrField.indiMotherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.setWife(
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

                        record.setWifeMarried(
                                fields[MarrField.wifeMarriedFirstName.ordinal()],
                                fields[MarrField.wifeMarriedLastName.ordinal()],
                                //M",
                                "",
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MarrField.wifeMarriedComment.ordinal()],
                                "");  //décédé

                        record.setWifeFather(
                                fields[MarrField.wifeFatherFirstName.ordinal()],
                                fields[MarrField.wifeFatherLastName.ordinal()],
                                fields[MarrField.wifeFatherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MarrField.wifeFatherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.setWifeMother(
                                fields[MarrField.wifeMotherFirstName.ordinal()],
                                fields[MarrField.wifeMotherLastName.ordinal()],
                                fields[MarrField.wifeMotherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MarrField.wifeMotherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.setWitness1(
                                fields[MarrField.witness1FirstName.ordinal()],
                                fields[MarrField.witness1LastName.ordinal()],
                                "",
                                fields[MarrField.witness1Comment.ordinal()]);
                        record.setWitness2(
                                fields[MarrField.witness2FirstName.ordinal()],
                                fields[MarrField.witness2LastName.ordinal()],
                                "",
                                fields[MarrField.witness2Comment.ordinal()]);
                        record.setWitness3(
                                fields[MarrField.witness3FirstName.ordinal()],
                                fields[MarrField.witness3LastName.ordinal()],
                                "",
                                fields[MarrField.witness3Comment.ordinal()]);
                        record.setWitness4(
                                fields[MarrField.witness4FirstName.ordinal()],
                                fields[MarrField.witness4LastName.ordinal()],
                               "",
                                fields[MarrField.witness4Comment.ordinal()]);

                        record.setFieldValue(FieldType.generalComment, fields[MarrField.generalComment.ordinal()]);

                        fileBuffer.addRecord(record);

                    } else if (fields[DeathField.typeActe.ordinal()].equals("D")) {
                        RecordDeath record = new RecordDeath();

                        record.setFieldValue(FieldType.eventDate, fields[DeathField.eventDate.ordinal()]);
                        //record.dateIncomplete = fields[DeathField.dateIncomplete.ordinal()];

                        record.setFieldValue(FieldType.cote, fields[DeathField.cote.ordinal()]);
                        record.setFieldValue(FieldType.freeComment,  fields[DeathField.libre.ordinal()]);

                        record.setIndi(
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

                        record.setIndiMarried(
                                fields[DeathField.wifeFirstName.ordinal()],
                                fields[DeathField.wifeLastName.ordinal()],
                                //fields[DeathField.indiSex.ordinal()].equals("M") ? "F" : "M",
                                fields[DeathField.wifeOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[DeathField.wifeComment.ordinal()],
                                "");  //décédé

                        record.setIndiFather(
                                fields[DeathField.indiFatherFirstName.ordinal()],
                                fields[DeathField.indiFatherLastName.ordinal()],
                                fields[DeathField.indiFatherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[DeathField.indiFatherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.setIndiMother(
                                fields[DeathField.indiMotherFirstName.ordinal()],
                                fields[DeathField.indiMotherLastName.ordinal()],
                                fields[DeathField.indiMotherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[DeathField.indiMotherComment.ordinal()],
                                "",   //décédé
                                "");  //age

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

                        record.setFieldValue(FieldType.generalComment, fields[DeathField.generalComment.ordinal()]);
                        fileBuffer.addRecord(record);
                    } else if (fields[MiscField.typeActe.ordinal()].equals("V")) {
                        RecordMisc record = new RecordMisc();

                        record.setFieldValue(FieldType.eventDate, fields[MiscField.eventDate.ordinal()]);
                        //record.dateIncomplete = fields[MiscField.dateIncomplete.ordinal()];
                        record.setFieldValue(FieldType.eventType, fields[MiscField.eventTypeName.ordinal()]);

                        record.setFieldValue(FieldType.cote, fields[MiscField.cote.ordinal()]);
                        record.setFieldValue(FieldType.freeComment,  fields[MiscField.libre.ordinal()]);

                        record.setIndi(
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

                        record.setIndiMarried(
                                fields[MiscField.indiMarriedFirstName.ordinal()],
                                fields[MiscField.indiMarriedLastName.ordinal()],
                                //fields[MiscField.indiSex.ordinal()].equals("M") ? "F" : "M",
                                "", // profession
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MiscField.indiMarriedComment.ordinal()],
                                "");  //décédé

                        record.setIndiFather(
                                fields[MiscField.indiFatherFirstName.ordinal()],
                                fields[MiscField.indiFatherLastName.ordinal()],
                                fields[MiscField.indiFatherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MiscField.indiFatherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.setIndiMother(
                                fields[MiscField.indiMotherFirstName.ordinal()],
                                fields[MiscField.indiMotherLastName.ordinal()],
                                fields[MiscField.indiMotherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MiscField.indiMotherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.setWife(
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

                        record.setWifeMarried(
                                fields[MiscField.wifeMarriedFirstName.ordinal()],
                                fields[MiscField.wifeMarriedLastName.ordinal()],
                                //fields[MiscField.indiSex.ordinal()].equals("M") ? "F" : "M",
                                "", // profession
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MiscField.wifeMarriedComment.ordinal()],
                                "");  //décédé

                        record.setWifeFather(
                                fields[MiscField.wifeFatherFirstName.ordinal()],
                                fields[MiscField.wifeFatherLastName.ordinal()],
                                fields[MiscField.wifeFatherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MiscField.wifeFatherComment.ordinal()],
                                "",   //décédé
                                "");  //age

                        record.setWifeMother(
                                fields[MiscField.wifeMotherFirstName.ordinal()],
                                fields[MiscField.wifeMotherLastName.ordinal()],
                                fields[MiscField.wifeMotherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[MiscField.wifeMotherComment.ordinal()],
                                "",   //décédé
                                "");  //age

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

                        record.setFieldValue(FieldType.generalComment, fields[MiscField.generalComment.ordinal()]);
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
                    //e.printStackTrace(System.err);
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
                        line.appendNimegueFn(record.getFieldValue(FieldType.eventDate));
                        line.appendNimegueFn("");
                        line.appendNimegueFn(record.getFieldValue(FieldType.cote));
                        line.appendNimegueFn(record.getFieldValue(FieldType.freeComment));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiSex));
                        String birthPlace = "";
                        if (!record.getFieldValue(FieldType.indiBirthPlace).isEmpty() ) {
                            birthPlace = "Lieu: "+record.getFieldValue(FieldType.indiBirthPlace);
                        }
                        line.appendNimegueFn(birthPlace, record.getFieldValue(FieldType.indiComment));

                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFatherLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFatherFirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFatherAge),
                                record.getFieldString(FieldType.indiFatherDead),
                                record.getFieldValue(FieldType.indiFatherResidence),
                                record.getFieldValue(FieldType.indiFatherComment) );
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFatherOccupation));

                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMotherLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMotherFirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMotherAge),
                                record.getFieldString(FieldType.indiMotherDead),
                                record.getFieldValue(FieldType.indiMotherResidence),
                                record.getFieldValue(FieldType.indiMotherComment));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMotherOccupation));

                        line.appendNimegueFn(record.getFieldValue(FieldType.witness1LastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness1FirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness1Comment),
                            record.getFieldValue(FieldType.witness1Occupation) );

                        line.appendNimegueFn(record.getFieldValue(FieldType.witness2LastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness2FirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness2Comment),
                            record.getFieldValue(FieldType.witness2Occupation) );

                        if (record.getFieldValue(FieldType.witness3LastName).isEmpty() && record.getFieldValue(FieldType.witness4LastName).isEmpty()) {
                            line.appendNimegueFn(record.getFieldValue(FieldType.generalComment));
                        } else {
                            line.appendNimegueFn(record.getFieldValue(FieldType.generalComment),
                                    "témoin: " + record.getFieldValue(FieldType.witness3FirstName) + " " + record.getFieldValue(FieldType.witness3LastName),
                                    record.getFieldValue(FieldType.witness3Occupation),
                                    record.getFieldValue(FieldType.witness3Comment),
                                    record.getFieldValue(FieldType.witness4FirstName) + " " + record.getFieldValue(FieldType.witness4LastName),
                                    record.getFieldValue(FieldType.witness4Occupation),
                                    record.getFieldValue(FieldType.witness4Comment));
                        }
                        line.appendNimegueFn(String.valueOf(index)); // numero d'enregistrement

                    } if ( record instanceof RecordMarriage ) {

                        line.appendNimegueFn("NIMEGUEV3");
                        line.appendNimegueFn(placeManager.getCityCode());
                        line.appendNimegueFn(placeManager.getCityName());
                        line.appendNimegueFn(placeManager.getCountryName());
                        line.appendNimegueFn(placeManager.getCountyName());
                        line.appendNimegueFn("M");
                        line.appendNimegueFn(record.getFieldValue(FieldType.eventDate));
                        line.appendNimegueFn("");
                        line.appendNimegueFn(record.getFieldValue(FieldType.cote));
                        line.appendNimegueFn(record.getFieldValue(FieldType.freeComment));

                        line.appendNimegueFn(record.getFieldValue(FieldType.indiLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiBirthPlace));
                        if (record.getFieldValue(FieldType.indiBirthDate).trim().equals("")) {
                            line.appendNimegueFn("          ");
                        } else {
                            line.appendNimegueFn(record.getFieldValue(FieldType.indiBirthDate));
                        }
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiAge));
                        line.appendNimegueFn(
                                record.getFieldValue(FieldType.indiComment),
                                record.getFieldValue(FieldType.indiResidence)
                                );
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiOccupation));

                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMarriedLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMarriedFirstName));
                        line.appendNimegueFn(record.getFieldString(FieldType.indiMarriedDead), 
                                record.getFieldValue(FieldType.indiMarriedOccupation),
                                record.getFieldValue(FieldType.indiMarriedResidence),
                                record.getFieldValue(FieldType.indiMarriedComment));

                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFatherLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFatherFirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFatherAge),
                                record.getFieldString(FieldType.indiFatherDead),
                                record.getFieldValue(FieldType.indiFatherResidence),
                                record.getFieldValue(FieldType.indiFatherComment));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFatherOccupation));

                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMotherLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMotherFirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMotherAge),
                                record.getFieldString(FieldType.indiMotherDead),
                                record.getFieldValue(FieldType.indiMotherResidence),
                                record.getFieldValue(FieldType.indiMotherComment));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMotherOccupation));

                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeFirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeBirthPlace));
                        if (record.getFieldValue(FieldType.wifeBirthDate).equals("")){
                            line.appendNimegueFn("          ");
                        } else {
                            line.appendNimegueFn(record.getFieldValue(FieldType.wifeBirthDate));
                        }
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeAge));
                        line.appendNimegueFn(
                                record.getFieldValue(FieldType.wifeResidence),
                                record.getFieldValue(FieldType.wifeComment)
                                );
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeOccupation));

                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeMarriedLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeMarriedFirstName));
                        line.appendNimegueFn(
                                record.getFieldString(FieldType.wifeMarriedDead), 
                                record.getFieldValue(FieldType.wifeMarriedOccupation), 
                                record.getFieldValue(FieldType.wifeMarriedResidence), 
                                record.getFieldValue(FieldType.wifeMarriedComment)
                                );

                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeFatherLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeFatherFirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeFatherAge),
                                record.getFieldString(FieldType.wifeFatherDead),
                                record.getFieldValue(FieldType.wifeFatherResidence),
                                record.getFieldValue(FieldType.wifeFatherComment));
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeFatherOccupation));

                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeMotherLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeMotherFirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeMotherAge),
                                record.getFieldString(FieldType.wifeMotherDead),
                                record.getFieldValue(FieldType.wifeMotherResidence),
                                record.getFieldValue(FieldType.wifeMotherComment));
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeMotherOccupation));

                        line.appendNimegueFn(record.getFieldValue(FieldType.witness1LastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness1FirstName));
                        line.appendNimegueFn(
                                record.getFieldValue(FieldType.witness1Comment),
                                record.getFieldValue(FieldType.witness1Occupation) 
                                );

                        line.appendNimegueFn(record.getFieldValue(FieldType.witness2LastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness2FirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness2Comment),
                            record.getFieldValue(FieldType.witness2Occupation) );

                        line.appendNimegueFn(record.getFieldValue(FieldType.witness3LastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness3FirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness3Comment),
                            record.getFieldValue(FieldType.witness3Occupation) );

                        line.appendNimegueFn(record.getFieldValue(FieldType.witness4LastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness4FirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness4Comment),
                            record.getFieldValue(FieldType.witness4Occupation) );

                        line.appendNimegueFn(record.getFieldValue(FieldType.generalComment));
                        line.appendNimegueFn(String.valueOf(index)); // numero d'enregistrement

                    } else if ( record instanceof RecordDeath ) {

                        line.appendNimegueFn("NIMEGUEV3");
                        line.appendNimegueFn(placeManager.getCityCode());
                        line.appendNimegueFn(placeManager.getCityName());
                        line.appendNimegueFn(placeManager.getCountryName());
                        line.appendNimegueFn(placeManager.getCountyName());
                        line.appendNimegueFn("D");
                        line.appendNimegueFn(record.getFieldValue(FieldType.eventDate));
                        line.appendNimegueFn("");
                        line.appendNimegueFn(record.getFieldValue(FieldType.cote));
                        line.appendNimegueFn(record.getFieldValue(FieldType.freeComment));

                        line.appendNimegueFn(record.getFieldValue(FieldType.indiLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiBirthPlace));
                        if (record.getFieldValue(FieldType.indiBirthDate).trim().equals("")){
                            line.appendNimegueFn("          ");
                        } else {
                            line.appendNimegueFn(record.getFieldValue(FieldType.indiBirthDate));
                        }
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiSex));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiAge));
                        line.appendNimegueFn(
                                record.getFieldValue(FieldType.indiResidence),
                                record.getFieldValue(FieldType.indiComment)
                                );
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiOccupation));

                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMarriedLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMarriedFirstName));
                        line.appendNimegueFn(
                                record.getFieldString(FieldType.indiMarriedDead), 
                                record.getFieldValue(FieldType.indiMarriedResidence), 
                                record.getFieldValue(FieldType.indiMarriedComment)
                                );
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMarriedOccupation));

                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFatherLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFatherFirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFatherAge),
                                record.getFieldString(FieldType.indiFatherDead),
                                record.getFieldValue(FieldType.indiFatherResidence),
                                record.getFieldValue(FieldType.indiFatherComment)
                                );
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFatherOccupation));

                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMotherLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMotherFirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMotherAge),
                                record.getFieldString(FieldType.indiMotherDead),
                                record.getFieldValue(FieldType.indiMotherResidence),
                                record.getFieldValue(FieldType.indiMotherComment)
                                );
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMotherOccupation));

                        line.appendNimegueFn(record.getFieldValue(FieldType.witness1LastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness1FirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness1Comment),
                            record.getFieldValue(FieldType.witness1Occupation) );

                        line.appendNimegueFn(record.getFieldValue(FieldType.witness2LastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness2FirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness2Comment),
                            record.getFieldValue(FieldType.witness2Occupation) );

                       if (record.getFieldValue(FieldType.witness3LastName).isEmpty() && record.getFieldValue(FieldType.witness4LastName).isEmpty()) {
                            line.appendNimegueFn(record.getFieldValue(FieldType.generalComment));
                        } else {
                            line.appendNimegueFn(record.getFieldValue(FieldType.generalComment),
                                    "témoin: " + record.getFieldValue(FieldType.witness3FirstName) + " " + record.getFieldValue(FieldType.witness3LastName),
                                    record.getFieldValue(FieldType.witness3Occupation),
                                    record.getFieldValue(FieldType.witness3Comment),
                                    record.getFieldValue(FieldType.witness4FirstName) + " " + record.getFieldValue(FieldType.witness4LastName),
                                    record.getFieldValue(FieldType.witness4Occupation),
                                    record.getFieldValue(FieldType.witness4Comment));
                        }
                        line.appendNimegueFn(String.valueOf(index)); // numero d'enregistrement

                    } else if ( record instanceof RecordMisc ) {

                        line.appendNimegueFn("NIMEGUEV3");
                        line.appendNimegueFn(placeManager.getCityCode());
                        line.appendNimegueFn(placeManager.getCityName());
                        line.appendNimegueFn(placeManager.getCountryName());
                        line.appendNimegueFn(placeManager.getCountyName());
                        line.appendNimegueFn("V");
                        line.appendNimegueFn(record.getFieldValue(FieldType.eventDate));
                        line.appendNimegueFn("");
                        line.appendNimegueFn(record.getFieldValue(FieldType.cote));
                        line.appendNimegueFn(record.getFieldValue(FieldType.freeComment));

                        line.appendNimegueFn(record.getFieldValue(FieldType.eventType));
                        line.appendNimegueFn(""); // EventTypeTag
                        
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiSex));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiBirthPlace));
                        if (record.getFieldValue(FieldType.indiBirthDate).trim().equals("")) {
                            line.appendNimegueFn("          ");
                        } else {
                            line.appendNimegueFn(record.getFieldValue(FieldType.indiBirthDate));
                        }
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiAge));
                        line.appendNimegueFn(
                                record.getFieldValue(FieldType.indiResidence),
                                record.getFieldValue(FieldType.indiComment)
                                );
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiOccupation));

                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMarriedLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMarriedFirstName));
                        line.appendNimegueFn(
                                record.getFieldString(FieldType.indiMarriedDead), 
                                record.getFieldValue(FieldType.indiMarriedOccupation), 
                                record.getFieldValue(FieldType.indiMarriedResidence), 
                                record.getFieldValue(FieldType.indiMarriedComment)
                                );

                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFatherLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFatherFirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFatherAge),
                                record.getFieldString(FieldType.indiFatherDead),
                                record.getFieldValue(FieldType.indiFatherResidence),
                                record.getFieldValue(FieldType.indiFatherComment)
                                );
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiFatherOccupation));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMotherLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMotherFirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMotherAge),
                                record.getFieldString(FieldType.indiMotherDead),
                                record.getFieldValue(FieldType.indiMotherResidence),
                                record.getFieldValue(FieldType.indiMotherComment));
                        line.appendNimegueFn(record.getFieldValue(FieldType.indiMotherOccupation));

                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeFirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeSex));
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeBirthPlace));
                        if (record.getFieldValue(FieldType.wifeBirthDate).equals("")){
                            line.appendNimegueFn("          ");
                        } else {
                            line.appendNimegueFn(record.getFieldValue(FieldType.wifeBirthDate));
                        }
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeAge));
                        line.appendNimegueFn(
                                record.getFieldValue(FieldType.wifeResidence),
                                record.getFieldValue(FieldType.wifeComment)
                                );
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeOccupation));

                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeMarriedLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeMarriedFirstName));
                        line.appendNimegueFn(
                                record.getFieldString(FieldType.wifeMarriedDead), 
                                record.getFieldValue(FieldType.wifeMarriedOccupation), 
                                record.getFieldValue(FieldType.wifeMarriedResidence), 
                                record.getFieldValue(FieldType.wifeMarriedComment)
                                );
                        
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeFatherLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeFatherFirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeFatherAge),
                                record.getFieldString(FieldType.wifeFatherDead),
                                record.getFieldValue(FieldType.wifeFatherResidence),
                                record.getFieldValue(FieldType.wifeFatherComment)
                                );
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeFatherOccupation));
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeMotherLastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeMotherFirstName));
                        line.appendNimegueFn(
                                record.getFieldValue(FieldType.wifeMotherAge),
                                record.getFieldString(FieldType.wifeMotherDead),
                                record.getFieldValue(FieldType.wifeMotherResidence),
                                record.getFieldValue(FieldType.wifeMotherComment)
                                );
                        line.appendNimegueFn(record.getFieldValue(FieldType.wifeMotherOccupation));

                        line.appendNimegueFn(record.getFieldValue(FieldType.witness1LastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness1FirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness1Comment),
                            record.getFieldValue(FieldType.witness1Occupation) );

                        line.appendNimegueFn(record.getFieldValue(FieldType.witness2LastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness2FirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness2Comment),
                            record.getFieldValue(FieldType.witness2Occupation) );

                        line.appendNimegueFn(record.getFieldValue(FieldType.witness3LastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness3FirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness3Comment),
                            record.getFieldValue(FieldType.witness3Occupation) );

                        line.appendNimegueFn(record.getFieldValue(FieldType.witness4LastName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness4FirstName));
                        line.appendNimegueFn(record.getFieldValue(FieldType.witness4Comment),
                            record.getFieldValue(FieldType.witness4Occupation) );
                        
                        line.appendNimegueFn(record.getFieldValue(FieldType.generalComment));
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
