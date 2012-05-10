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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.util.Arrays;
import org.openide.util.Exceptions;

/**
 *
 * @author Michel
 */
public class ReleveFileAncestrisV1 {

    final static private String fileSignature = "ANCESTRISV1";
    final static private char fieldSeparator = ';';

    /**
     * verifie si la premere ligne est conforme au format 
     * @param inputFile
     * @param sb  message d'erreur
     * @return
     */
    public static boolean isValidFile(File inputFile, StringBuilder sb) {
        BufferedReader br = null;
        try {
            br = new BufferedReader( new InputStreamReader(checkUtf8Bom(new FileInputStream(inputFile)),"UTF-8"));
            String[] fields = splitLine(br);
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
     * filtre le BOM UTF-8
     * @param is
     * @return
     * @throws IOException
     */
    static private InputStream checkUtf8Bom(InputStream is) throws IOException {
        final byte[] utf8Bom = { (byte) 0xef, (byte) 0xbb, (byte) 0xbf};
        PushbackInputStream pis = new PushbackInputStream(is, utf8Bom.length);
        byte[] bomRead= new byte[utf8Bom.length];
        if (pis.read(bomRead, 0, utf8Bom.length) == -1) {
            return is;
        }
        if (!Arrays.equals(bomRead, utf8Bom )) {
            pis.unread(bomRead);
        }
        return pis;
    }

    /**
     * decoupe une ligne
     * @param strLine
     * @return fields[] ou null si la ligne n'est pas valide
     */
    private static String[] splitLine(BufferedReader br) throws Exception {

        String[] fields = Line.splitCSV(br, fieldSeparator);
        if (fields != null) {
            if (fields.length == 78) {
                if (fields[0].equals(fileSignature)) {
                    return fields;
                } else {
                    throw new Exception(String.format("La ligne doit commencer par %s au lieu de %s", fileSignature, fields[0]));
                }
            } else {
                throw new Exception(String.format("Line contains %s fields. Must be %d fields", fields.length, 77));
            }
        } else {
            return null;
        }
    }

    // Format d'un releve d'un marriage
    enum Field {
        ancetris,
        nomCommune, codeCommune, nomDepartement, stateName, countryName, parish,
        eventType, eventTypeTag, eventTypeName,
        eventDate, cote, freeComment, notaryComment,
        indiLastName, indiFirstName, indiSex, indiPlace, indiBirthDate, indiAge, indiOccupation, indiComment,
        indiMarriedLastName, indiMarriedFirstName, indiMarriedDead, indiMarriedOccupation, indiMarriedComment,
        indiFatherLastName, indiFatherFirstName, indiFatherDead, indiFatherOccupation, indiFatherComment,
        indiMotherLastName, indiMotherFirstName, indiMotherDead, indiMotherOccupation, indiMotherComment,
        wifeLastName, wifeFirstName, wifeSex, wifePlace, wifeBirthDate, wifeAge, wifeOccupation, wifeComment,
        wifeMarriedLastName, wifeMarriedFirstName, wifeMarriedDead, wifeMarriedOccupation, wifeMarriedComment,
        wifeFatherLastName, wifeFatherFirstName, wifeFatherDead, wifeFatherOccupation, wifeFatherComment,
        wifeMotherLastName, wifeMotherFirstName, wifeMotherDead, wifeMotherOccupation, wifeMotherComment,
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
     * TODO gérer la dat iincomplete
     */
    public static FileBuffer loadFile(File inputFile ) { 
        FileBuffer fileBuffer = new FileBuffer();
        try {
            fileBuffer = loadFile(checkUtf8Bom(new FileInputStream(inputFile)));
            return fileBuffer;
        } catch (Exception ex) {            
            Exceptions.printStackTrace(ex);
            fileBuffer.append(ex.toString()).append("\n");
            return fileBuffer;
        } 
    }

    public static FileBuffer loadFile( InputStream inputStream ) {
        FileBuffer fileBuffer = new FileBuffer();
        try {
            //create BufferedReader to read file
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
            int lineNumber = 0;
            String[] fields ;
            //read comma separated file line by line
            while ((fields = splitLine(br)) != null) {
                try {
                    lineNumber++;
                    if (fields[Field.eventType.ordinal()].equals("N")) {
                        RecordBirth record = new RecordBirth();

                        record.setEventPlace(
                                fields[Field.nomCommune.ordinal()],
                                fields[Field.codeCommune.ordinal()],
                                fields[Field.nomDepartement.ordinal()],
                                fields[Field.stateName.ordinal()],
                                fields[Field.countryName.ordinal()] );
                        record.setParish(fields[Field.parish.ordinal()]);
                        record.setEventDate(fields[Field.eventDate.ordinal()]);
                        record.setCote(fields[Field.cote.ordinal()]);
                        record.setFreeComment(fields[Field.freeComment.ordinal()]);

                        record.setIndi(
                                fields[Field.indiFirstName.ordinal()],
                                fields[Field.indiLastName.ordinal()],
                                fields[Field.indiSex.ordinal()],
                                fields[Field.indiBirthDate.ordinal()],
                                "", // pas de date de naossance a la naissance
                                "", // pas de lieu a la naissance
                                "", // pas de profession a la naissance
                                fields[Field.indiComment.ordinal()]);

                        record.setIndiFather(
                                fields[Field.indiFatherFirstName.ordinal()],
                                fields[Field.indiFatherLastName.ordinal()],
                                fields[Field.indiFatherOccupation.ordinal()],
                                fields[Field.indiFatherComment.ordinal()],
                                fields[Field.indiFatherDead.ordinal()]);

                        record.setIndiMother(
                                fields[Field.indiMotherFirstName.ordinal()],
                                fields[Field.indiMotherLastName.ordinal()],
                                fields[Field.indiMotherOccupation.ordinal()],
                                fields[Field.indiMotherComment.ordinal()],
                                fields[Field.indiMotherDead.ordinal()]);

                        record.setWitness1(
                                fields[Field.witness1FirstName.ordinal()],
                                fields[Field.witness1LastName.ordinal()],
                                fields[Field.witness1Occupation.ordinal()],
                                fields[Field.witness1Comment.ordinal()]);
                        record.setWitness2(
                                fields[Field.witness2FirstName.ordinal()],
                                fields[Field.witness2LastName.ordinal()],
                                fields[Field.witness2Occupation.ordinal()],
                                fields[Field.witness2Comment.ordinal()]);
                        record.setWitness3(
                                fields[Field.witness3FirstName.ordinal()],
                                fields[Field.witness3LastName.ordinal()],
                                fields[Field.witness3Occupation.ordinal()],
                                fields[Field.witness3Comment.ordinal()]);
                        record.setWitness4(
                                fields[Field.witness4FirstName.ordinal()],
                                fields[Field.witness4LastName.ordinal()],
                                fields[Field.witness4Occupation.ordinal()],
                                fields[Field.witness4Comment.ordinal()]);

                        record.setGeneralComment(fields[Field.generalComment.ordinal()]);
                        try {
                            record.recordNo = Integer.valueOf(fields[Field.recordNo.ordinal()]);
                        } catch ( NumberFormatException ex ) {
                            record.recordNo = 0;
                        }
                        fileBuffer.loadRecord(record);

                    } else if (fields[Field.eventType.ordinal()].equals("M")) {
                        RecordMarriage record = new RecordMarriage();

                        record.setEventPlace(
                                fields[Field.nomCommune.ordinal()],
                                fields[Field.codeCommune.ordinal()],
                                fields[Field.nomDepartement.ordinal()],
                                fields[Field.stateName.ordinal()],
                                fields[Field.countryName.ordinal()] );
                        record.setParish(fields[Field.parish.ordinal()]);
                        record.setEventDate(fields[Field.eventDate.ordinal()]);
                        record.setCote(fields[Field.cote.ordinal()]);
                        record.setFreeComment(fields[Field.freeComment.ordinal()]);

                        record.setEventDate(fields[Field.eventDate.ordinal()]);

                        record.setIndi(
                                fields[Field.indiFirstName.ordinal()],
                                fields[Field.indiLastName.ordinal()],
                                "M",
                                fields[Field.indiAge.ordinal()],
                                fields[Field.indiBirthDate.ordinal()],
                                fields[Field.indiPlace.ordinal()],
                                fields[Field.indiOccupation.ordinal()],
                                fields[Field.indiComment.ordinal()]);

                        record.setIndiMarried(
                                fields[Field.indiMarriedFirstName.ordinal()],
                                fields[Field.indiMarriedLastName.ordinal()],
                                //"F",
                                fields[Field.indiMarriedOccupation.ordinal()],
                                fields[Field.indiMarriedComment.ordinal()],
                                fields[Field.indiMarriedDead.ordinal()]);

                        record.setIndiFather(
                                fields[Field.indiFatherFirstName.ordinal()],
                                fields[Field.indiFatherLastName.ordinal()],
                                fields[Field.indiFatherOccupation.ordinal()],
                                fields[Field.indiFatherComment.ordinal()],
                                fields[Field.indiFatherDead.ordinal()]);  //décédé

                        record.setIndiMother(
                                fields[Field.indiMotherFirstName.ordinal()],
                                fields[Field.indiMotherLastName.ordinal()],
                                fields[Field.indiMotherOccupation.ordinal()],
                                fields[Field.indiMotherComment.ordinal()],
                                fields[Field.indiMotherDead.ordinal()]);  //décédé

                        record.setWife(
                                fields[Field.wifeFirstName.ordinal()],
                                fields[Field.wifeLastName.ordinal()],
                                "F",
                                fields[Field.wifeAge.ordinal()],
                                fields[Field.wifeBirthDate.ordinal()],
                                fields[Field.wifePlace.ordinal()],
                                fields[Field.wifeOccupation.ordinal()],
                                fields[Field.wifeComment.ordinal()]);

                        record.setWifeMarried(
                                fields[Field.wifeMarriedFirstName.ordinal()],
                                fields[Field.wifeMarriedLastName.ordinal()],
                                //"M",
                                fields[Field.wifeMarriedOccupation.ordinal()],
                                fields[Field.wifeMarriedComment.ordinal()],
                                fields[Field.wifeMarriedDead.ordinal()]);

                        record.setWifeFather(
                                fields[Field.wifeFatherFirstName.ordinal()],
                                fields[Field.wifeFatherLastName.ordinal()],
                                fields[Field.wifeFatherOccupation.ordinal()],
                                fields[Field.wifeFatherComment.ordinal()],
                                fields[Field.wifeFatherDead.ordinal()]);

                        record.setWifeMother(
                                fields[Field.wifeMotherFirstName.ordinal()],
                                fields[Field.wifeMotherLastName.ordinal()],
                                fields[Field.wifeMotherOccupation.ordinal()],
                                fields[Field.wifeMotherComment.ordinal()],
                                fields[Field.wifeMotherDead.ordinal()]);

                        record.setWitness1(
                                fields[Field.witness1FirstName.ordinal()],
                                fields[Field.witness1LastName.ordinal()],
                                fields[Field.witness1Occupation.ordinal()],
                                fields[Field.witness1Comment.ordinal()]);
                        record.setWitness2(
                                fields[Field.witness2FirstName.ordinal()],
                                fields[Field.witness2LastName.ordinal()],
                                fields[Field.witness2Occupation.ordinal()],
                                fields[Field.witness2Comment.ordinal()]);
                        record.setWitness3(
                                fields[Field.witness3FirstName.ordinal()],
                                fields[Field.witness3LastName.ordinal()],
                                fields[Field.witness3Occupation.ordinal()],
                                fields[Field.witness3Comment.ordinal()]);
                        record.setWitness4(
                                fields[Field.witness4FirstName.ordinal()],
                                fields[Field.witness4LastName.ordinal()],
                                fields[Field.witness4Occupation.ordinal()],
                                fields[Field.witness4Comment.ordinal()]);

                        record.setGeneralComment(fields[Field.generalComment.ordinal()]);
                        try {
                            record.recordNo = Integer.valueOf(fields[Field.recordNo.ordinal()]);
                        } catch (NumberFormatException ex) {
                            record.recordNo = 0;
                        }
                        fileBuffer.loadRecord(record);

                    } else if (fields[Field.eventType.ordinal()].equals("D")) {
                        RecordDeath record = new RecordDeath();

                        record.setEventPlace(
                                fields[Field.nomCommune.ordinal()],
                                fields[Field.codeCommune.ordinal()],
                                fields[Field.nomDepartement.ordinal()],
                                fields[Field.stateName.ordinal()],
                                fields[Field.countryName.ordinal()] );
                        record.setParish(fields[Field.parish.ordinal()]);
                        record.setEventDate(fields[Field.eventDate.ordinal()]);
                        record.setCote(fields[Field.cote.ordinal()]);
                        record.setFreeComment(fields[Field.freeComment.ordinal()]);

                        record.setEventDate(fields[Field.eventDate.ordinal()]);

                        record.setIndi(
                                fields[Field.indiFirstName.ordinal()],
                                fields[Field.indiLastName.ordinal()],
                                fields[Field.indiSex.ordinal()],
                                fields[Field.indiAge.ordinal()],
                                fields[Field.indiBirthDate.ordinal()],
                                fields[Field.indiPlace.ordinal()],
                                fields[Field.indiOccupation.ordinal()],
                                fields[Field.indiComment.ordinal()]);

                        record.setIndiMarried(
                                fields[Field.indiMarriedFirstName.ordinal()],
                                fields[Field.indiMarriedLastName.ordinal()],
                                //fields[Field.indiMarriedSex.ordinal()].equals("M") ? "F" : "M",
                                fields[Field.indiMarriedOccupation.ordinal()],
                                fields[Field.indiMarriedComment.ordinal()],
                                fields[Field.indiMarriedDead.ordinal()]);  //décédé

                        record.setIndiFather(
                                fields[Field.indiFatherFirstName.ordinal()],
                                fields[Field.indiFatherLastName.ordinal()],
                                fields[Field.indiFatherOccupation.ordinal()],
                                fields[Field.indiFatherComment.ordinal()],
                                fields[Field.indiFatherDead.ordinal()]);  //décédé

                        record.setIndiMother(
                                fields[Field.indiMotherFirstName.ordinal()],
                                fields[Field.indiMotherLastName.ordinal()],
                                fields[Field.indiMotherOccupation.ordinal()],
                                fields[Field.indiMotherComment.ordinal()],
                                fields[Field.indiMotherDead.ordinal()]);  //décédé

                        record.setWitness1(
                                fields[Field.witness1FirstName.ordinal()],
                                fields[Field.witness1LastName.ordinal()],
                                fields[Field.witness1Occupation.ordinal()],
                                fields[Field.witness1Comment.ordinal()]);
                        record.setWitness2(
                                fields[Field.witness2FirstName.ordinal()],
                                fields[Field.witness2LastName.ordinal()],
                                fields[Field.witness2Occupation.ordinal()],
                                fields[Field.witness2Comment.ordinal()]);
                        record.setWitness3(
                                fields[Field.witness3FirstName.ordinal()],
                                fields[Field.witness3LastName.ordinal()],
                                fields[Field.witness3Occupation.ordinal()],
                                fields[Field.witness3Comment.ordinal()]);
                        record.setWitness4(
                                fields[Field.witness4FirstName.ordinal()],
                                fields[Field.witness4LastName.ordinal()],
                                fields[Field.witness4Occupation.ordinal()],
                                fields[Field.witness4Comment.ordinal()]);

                        record.setGeneralComment(fields[Field.generalComment.ordinal()]);

                        try {
                            record.recordNo = Integer.valueOf(fields[Field.recordNo.ordinal()]);
                        } catch (NumberFormatException ex) {
                            record.recordNo = 0;
                        }
                        fileBuffer.loadRecord(record);

                    } else if (fields[Field.eventType.ordinal()].equals("V")) {
                        RecordMisc record = new RecordMisc();
                        record.setEventType(fields[Field.eventTypeName.ordinal()], fields[Field.eventTypeTag.ordinal()]);
                        record.setNotary(fields[Field.notaryComment.ordinal()]);

                        record.setEventPlace(
                                fields[Field.nomCommune.ordinal()],
                                fields[Field.codeCommune.ordinal()],
                                fields[Field.nomDepartement.ordinal()],
                                fields[Field.stateName.ordinal()],
                                fields[Field.countryName.ordinal()] );
                        record.setParish(fields[Field.parish.ordinal()]);
                        record.setEventDate(fields[Field.eventDate.ordinal()]);
                        record.setCote(fields[Field.cote.ordinal()]);
                        record.setFreeComment(fields[Field.freeComment.ordinal()]);

                        record.setEventDate(fields[Field.eventDate.ordinal()]);

                        record.setIndi(
                                fields[Field.indiFirstName.ordinal()],
                                fields[Field.indiLastName.ordinal()],
                                fields[Field.indiSex.ordinal()],
                                fields[Field.indiAge.ordinal()],
                                fields[Field.indiBirthDate.ordinal()],
                                fields[Field.indiPlace.ordinal()],
                                fields[Field.indiOccupation.ordinal()],
                                fields[Field.indiComment.ordinal()]);

                        record.setIndiMarried(
                                fields[Field.indiMarriedFirstName.ordinal()],
                                fields[Field.indiMarriedLastName.ordinal()],
                                //fields[Field.indiSex.ordinal()].equals("M") ? "F" : "M",
                                fields[Field.indiMarriedOccupation.ordinal()],
                                fields[Field.indiMarriedComment.ordinal()],
                                fields[Field.indiMarriedDead.ordinal()]);

                        record.setIndiFather(
                                fields[Field.indiFatherFirstName.ordinal()],
                                fields[Field.indiFatherLastName.ordinal()],
                                fields[Field.indiFatherOccupation.ordinal()],
                                fields[Field.indiFatherComment.ordinal()],
                                fields[Field.indiFatherDead.ordinal()]);

                        record.setIndiMother(
                                fields[Field.indiMotherFirstName.ordinal()],
                                fields[Field.indiMotherLastName.ordinal()],
                                fields[Field.indiMotherOccupation.ordinal()],
                                fields[Field.indiMotherComment.ordinal()],
                                fields[Field.indiMotherDead.ordinal()]);

                        record.setWife(
                                fields[Field.wifeFirstName.ordinal()],
                                fields[Field.wifeLastName.ordinal()],
                                fields[Field.wifeSex.ordinal()],
                                fields[Field.wifeAge.ordinal()],
                                fields[Field.wifeBirthDate.ordinal()],
                                fields[Field.wifePlace.ordinal()],
                                fields[Field.wifeOccupation.ordinal()],
                                fields[Field.wifeComment.ordinal()]);  //décédé

                        record.setWifeMarried(
                                fields[Field.wifeMarriedFirstName.ordinal()],
                                fields[Field.wifeMarriedLastName.ordinal()],
                                //fields[Field.wifeSex.ordinal()].equals("M") ? "F" : "M",
                                fields[Field.wifeMarriedOccupation.ordinal()],
                                fields[Field.wifeMarriedComment.ordinal()],
                                fields[Field.wifeMarriedDead.ordinal()]);

                        record.setWifeFather(
                                fields[Field.wifeFatherFirstName.ordinal()],
                                fields[Field.wifeFatherLastName.ordinal()],
                                fields[Field.wifeFatherOccupation.ordinal()],
                                fields[Field.wifeFatherComment.ordinal()],
                                fields[Field.wifeFatherDead.ordinal()]);

                        record.setWifeMother(
                                fields[Field.wifeMotherFirstName.ordinal()],
                                fields[Field.wifeMotherLastName.ordinal()],
                                fields[Field.wifeMotherOccupation.ordinal()],
                                fields[Field.wifeMotherComment.ordinal()],
                                fields[Field.wifeMotherDead.ordinal()]);

                        record.setWitness1(
                                fields[Field.witness1FirstName.ordinal()],
                                fields[Field.witness1LastName.ordinal()],
                                fields[Field.witness1Occupation.ordinal()],
                                fields[Field.witness1Comment.ordinal()]);
                        record.setWitness2(
                                fields[Field.witness2FirstName.ordinal()],
                                fields[Field.witness2LastName.ordinal()],
                                fields[Field.witness2Occupation.ordinal()],
                                fields[Field.witness2Comment.ordinal()]);
                        record.setWitness3(
                                fields[Field.witness3FirstName.ordinal()],
                                fields[Field.witness3LastName.ordinal()],
                                fields[Field.witness3Occupation.ordinal()],
                                fields[Field.witness3Comment.ordinal()]);
                        record.setWitness4(
                                fields[Field.witness4FirstName.ordinal()],
                                fields[Field.witness4LastName.ordinal()],
                                fields[Field.witness4Occupation.ordinal()],
                                fields[Field.witness4Comment.ordinal()]);

                        record.setGeneralComment(fields[Field.generalComment.ordinal()]);

                        try {
                            record.recordNo = Integer.valueOf(fields[Field.recordNo.ordinal()]);
                        } catch (NumberFormatException ex) {
                            record.recordNo = 0;
                        }
                        fileBuffer.loadRecord(record);
                        
                    } else {
                        fileBuffer.append("Line ").append(lineNumber).append(" ");
                        fileBuffer.append("Type d'acte inconnu").append(" ");
                        fileBuffer.append(fields[Field.eventType.ordinal()]);
                        fileBuffer.append("\n");
                    }

                } catch (Exception e) {
                    // je trace l'erreur dans le buffer de sortie
                    fileBuffer.append("Line ").append(lineNumber).append(" ").append(e.toString()).append("\n");
                }
            } // while

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

        StringBuilder sb = new StringBuilder();
        try {
            //create BufferedReader to read csv file
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileName, append), "UTF-8") ;
            for (int index = 0; index < recordModel.getRowCount(); index++) {
                Line line = new Line(fieldSeparator);
                Record record = recordModel.getRecord(index);
                try {
                    if ( record instanceof RecordBirth ) {
                        line.appendCsvFn(fileSignature);
                        line.appendCsvFn(dataManager.getCityName());
                        line.appendCsvFn(dataManager.getCityCode());
                        line.appendCsvFn(dataManager.getCountyName());
                        line.appendCsvFn(dataManager.getStateName());
                        line.appendCsvFn(dataManager.getCountryName());
                        line.appendCsvFn(record.getParish().toString());
                        line.appendCsvFn("N");
                        line.appendCsvFn(""); //eventTypeTag
                        line.appendCsvFn(""); //eventTypeName
                        line.appendCsvFn(record.getEventDateString());
                        line.appendCsvFn(record.getCote().toString());
                        line.appendCsvFn(record.getFreeComment().toString());
                        line.appendCsvFn(""); // notary

                        line.appendCsvFn(record.getIndiLastName().getValue());
                        line.appendCsvFn(record.getIndiFirstName().getValue());
                        line.appendCsvFn(record.getIndiSex().toString());
                        line.appendCsvFn(""); // place
                        line.appendCsvFn(record.getIndiBirthDate().toString()); // IndiBirthDate
                        line.appendCsvFn(""); // age
                        line.appendCsvFn(""); // occupation
                        line.appendCsvFn(record.getIndiComment().toString());

                        line.appendCsvFn(""); // IndiMarriedLastName
                        line.appendCsvFn(""); // IndiMarriedFirstName
                        line.appendCsvFn(""); // IndiMarriedDead
                        line.appendCsvFn(""); // IndiMarriedOccupation
                        line.appendCsvFn(""); // IndiMarriedComment

                        line.appendCsvFn(record.getIndiFatherLastName().toString());
                        line.appendCsvFn(record.getIndiFatherFirstName().toString());
                        line.appendCsvFn(record.getIndiFatherDead().getValue());
                        line.appendCsvFn(record.getIndiFatherOccupation().toString());
                        line.appendCsvFn(record.getIndiFatherComment().toString());
                        line.appendCsvFn(record.getIndiMotherLastName().toString());
                        line.appendCsvFn(record.getIndiMotherFirstName().toString());
                        line.appendCsvFn(record.getIndiMotherDead().getValue());
                        line.appendCsvFn(record.getIndiMotherOccupation().toString());
                        line.appendCsvFn(record.getIndiMotherComment().toString());

                        line.appendCsvFn(""); // WifeLastName
                        line.appendCsvFn(""); // WifeFirstName
                        line.appendCsvFn(""); // WifeSex
                        line.appendCsvFn(""); // WifePlace
                        line.appendCsvFn(""); // WifeBirthDate
                        line.appendCsvFn(""); // WifeAge
                        line.appendCsvFn(""); // WifeOccupation
                        line.appendCsvFn(""); // WifeComment

                        line.appendCsvFn(""); // WifeMarriedLastName
                        line.appendCsvFn(""); // WifeMarriedFirstName
                        line.appendCsvFn(""); // WifeMarriedDead
                        line.appendCsvFn(""); // WifeMarriedOccupation
                        line.appendCsvFn(""); // WifeMarriedComment

                        line.appendCsvFn(""); // WifeFatherLastName
                        line.appendCsvFn(""); // WifeFatherFirstName
                        line.appendCsvFn(""); // WifeFatherDead
                        line.appendCsvFn(""); // WifeFatherOccupation
                        line.appendCsvFn(""); // WifeFatherComment
                        line.appendCsvFn(""); // WifeMotherLastName
                        line.appendCsvFn(""); // WifeMotherFirstName
                        line.appendCsvFn(""); // WifeMotherDead
                        line.appendCsvFn(""); // WifeMotherOccupation
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
                        line.appendCsv(String.valueOf(record.recordNo)); // numero d'enregistrement

                    } if ( record instanceof RecordMarriage ) {

                        line.appendCsvFn(fileSignature);
                        line.appendCsvFn(dataManager.getCityName());
                        line.appendCsvFn(dataManager.getCityCode());
                        line.appendCsvFn(dataManager.getCountyName());
                        line.appendCsvFn(dataManager.getStateName());
                        line.appendCsvFn(dataManager.getCountryName());
                        line.appendCsvFn(record.getParish().toString());
                        line.appendCsvFn("M");
                        line.appendCsvFn(""); //eventTypeTag
                        line.appendCsvFn(""); //eventTypeName
                        line.appendCsvFn(record.getEventDateString());
                        line.appendCsvFn(record.getCote().toString());
                        line.appendCsvFn(record.getFreeComment().toString());
                        line.appendCsvFn(""); // notary

                        line.appendCsvFn(record.getIndiLastName().getValue());
                        line.appendCsvFn(record.getIndiFirstName().getValue());
                        line.appendCsvFn(""); // IndiSex
                        line.appendCsvFn(record.getIndiPlace().toString());
                        line.appendCsvFn(record.getIndiBirthDate().toString());
                        line.appendCsvFn(record.getIndiAge().toString());
                        line.appendCsvFn(record.getIndiOccupation().toString());
                        line.appendCsvFn(record.getIndiComment().toString());
                        
                        line.appendCsvFn(record.getIndiMarriedLastName().toString());
                        line.appendCsvFn(record.getIndiMarriedFirstName().toString());
                        line.appendCsvFn(record.getIndiMarriedDead().getValue());
                        line.appendCsvFn(record.getIndiMarriedOccupation().toString());
                        line.appendCsvFn(record.getIndiMarriedComment().toString());

                        line.appendCsvFn(record.getIndiFatherLastName().toString());
                        line.appendCsvFn(record.getIndiFatherFirstName().toString());
                        line.appendCsvFn(record.getIndiFatherDead().getValue());
                        line.appendCsvFn(record.getIndiFatherOccupation().toString());
                        line.appendCsvFn(record.getIndiFatherComment().toString());
                        
                        line.appendCsvFn(record.getIndiMotherLastName().toString());
                        line.appendCsvFn(record.getIndiMotherFirstName().toString());
                        line.appendCsvFn(record.getIndiMotherDead().getValue());
                        line.appendCsvFn(record.getIndiMotherOccupation().toString());
                        line.appendCsvFn(record.getIndiMotherComment().toString());
                        
                        line.appendCsvFn(record.getWifeLastName().toString());
                        line.appendCsvFn(record.getWifeFirstName().toString());
                        line.appendCsvFn(""); //WifeSex
                        line.appendCsvFn(record.getWifePlace().toString());
                        line.appendCsvFn(record.getWifeBirthDate().toString());
                        line.appendCsvFn(record.getWifeAge().toString());
                        line.appendCsvFn(record.getWifeOccupation().toString());
                        line.appendCsvFn(record.getWifeComment().toString());
                        
                        line.appendCsvFn(record.getWifeMarriedLastName().toString());
                        line.appendCsvFn(record.getWifeMarriedFirstName().toString());
                        line.appendCsvFn(record.getWifeMarriedDead().getValue());
                        line.appendCsvFn(record.getWifeMarriedOccupation().toString());
                        line.appendCsvFn(record.getWifeMarriedComment().toString());

                        line.appendCsvFn(record.getWifeFatherLastName().toString());
                        line.appendCsvFn(record.getWifeFatherFirstName().toString());
                        line.appendCsvFn(record.getWifeFatherDead().getValue());
                        line.appendCsvFn(record.getWifeFatherOccupation().toString());
                        line.appendCsvFn(record.getWifeFatherComment().toString());
                        line.appendCsvFn(record.getWifeMotherLastName().toString());
                        line.appendCsvFn(record.getWifeMotherFirstName().toString());
                        line.appendCsvFn(record.getWifeMotherDead().getValue());
                        line.appendCsvFn(record.getWifeMotherOccupation().toString());
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
                        line.appendCsv(String.valueOf(record.recordNo)); // numero d'enregistrement

                    } else if ( record instanceof RecordDeath ) {

                        line.appendCsvFn(fileSignature);
                        line.appendCsvFn(dataManager.getCityName());
                        line.appendCsvFn(dataManager.getCityCode());
                        line.appendCsvFn(dataManager.getCountyName());
                        line.appendCsvFn(dataManager.getStateName());
                        line.appendCsvFn(dataManager.getCountryName());
                        line.appendCsvFn(record.getParish().toString());
                        line.appendCsvFn("D");
                        line.appendCsvFn(""); //eventTypeTag
                        line.appendCsvFn(""); //eventTypeName
                        line.appendCsvFn(record.getEventDateString());
                        line.appendCsvFn(record.getCote().toString());
                        line.appendCsvFn(record.getFreeComment().toString());
                        line.appendCsvFn(""); // notary

                        line.appendCsvFn(record.getIndiLastName().toString());
                        line.appendCsvFn(record.getIndiFirstName().toString());
                        line.appendCsvFn(record.getIndiSex().toString());
                        line.appendCsvFn(record.getIndiPlace().toString());
                        line.appendCsvFn(record.getIndiBirthDate().toString());
                        line.appendCsvFn(record.getIndiAge().toString());
                        line.appendCsvFn(record.getIndiOccupation().toString());
                        line.appendCsvFn(record.getIndiComment().toString());

                        line.appendCsvFn(record.getIndiMarriedLastName().toString());
                        line.appendCsvFn(record.getIndiMarriedFirstName().toString());
                        line.appendCsvFn(record.getIndiMarriedDead().getValue());
                        line.appendCsvFn(record.getIndiMarriedOccupation().toString());
                        line.appendCsvFn(record.getIndiMarriedComment().toString());

                        line.appendCsvFn(record.getIndiFatherLastName().toString());
                        line.appendCsvFn(record.getIndiFatherFirstName().toString());
                        line.appendCsvFn(record.getIndiFatherDead().getValue());
                        line.appendCsvFn(record.getIndiFatherOccupation().toString());
                        line.appendCsvFn(record.getIndiFatherComment().toString());
                        line.appendCsvFn(record.getIndiMotherLastName().toString());
                        line.appendCsvFn(record.getIndiMotherFirstName().toString());
                        line.appendCsvFn(record.getIndiMotherDead().getValue());
                        line.appendCsvFn(record.getIndiMotherOccupation().toString());
                        line.appendCsvFn(record.getIndiMotherComment().toString());

                        line.appendCsvFn(""); // WifeLastName
                        line.appendCsvFn(""); // WifeFirstName
                        line.appendCsvFn(""); // WifeSex
                        line.appendCsvFn(""); // WifePlace
                        line.appendCsvFn(""); // WifeBirthDate
                        line.appendCsvFn(""); // WifeAge
                        line.appendCsvFn(""); // WifeOccupation
                        line.appendCsvFn(""); // WifeComment

                        line.appendCsvFn(""); // WifeMarriedLastName
                        line.appendCsvFn(""); // WifeMarriedFirstName
                        line.appendCsvFn(""); // WifeMarriedDead
                        line.appendCsvFn(""); // WifeMarriedOccupation
                        line.appendCsvFn(""); // WifeMarriedComment

                        line.appendCsvFn(""); // WifeFatherLastName
                        line.appendCsvFn(""); // WifeFatherFirstName
                        line.appendCsvFn(""); // WifeFatherDead
                        line.appendCsvFn(""); // WifeFatherOccupation
                        line.appendCsvFn(""); // WifeFatherComment
                        line.appendCsvFn(""); // WifeMotherLastName
                        line.appendCsvFn(""); // WifeMotherFirstName
                        line.appendCsvFn(""); // WifeMotherDead
                        line.appendCsvFn(""); // WifeMotherOccupation
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
                        line.appendCsv(String.valueOf(record.recordNo)); // numero d'enregistrement

                    } else if ( record instanceof RecordMisc ) {

                        line.appendCsvFn(fileSignature);
                        line.appendCsvFn(dataManager.getCityName());
                        line.appendCsvFn(dataManager.getCityCode());
                        line.appendCsvFn(dataManager.getCountyName());
                        line.appendCsvFn(dataManager.getStateName());
                        line.appendCsvFn(dataManager.getCountryName());
                        line.appendCsvFn(record.getParish().toString());
                        line.appendCsvFn("V");
                        line.appendCsvFn(record.getEventType().getTag());
                        line.appendCsvFn(record.getEventType().getName());
                        line.appendCsvFn(record.getEventDateString());
                        line.appendCsvFn(record.getCote().toString());
                        line.appendCsvFn(record.getFreeComment().toString());
                        line.appendCsvFn(record.getNotary().toString());

                        line.appendCsvFn(record.getIndiLastName().toString());
                        line.appendCsvFn(record.getIndiFirstName().toString());
                        line.appendCsvFn(record.getIndiSex().toString());
                        line.appendCsvFn(record.getIndiPlace().toString());
                        line.appendCsvFn(record.getIndiBirthDate().toString());
                        line.appendCsvFn(record.getIndiAge().toString());
                        line.appendCsvFn(record.getIndiOccupation().toString());
                        line.appendCsvFn(record.getIndiComment().toString());

                        line.appendCsvFn(record.getIndiMarriedLastName().toString());
                        line.appendCsvFn(record.getIndiMarriedFirstName().toString());
                        line.appendCsvFn(record.getIndiMarriedDead().getValue());
                        line.appendCsvFn(record.getIndiMarriedOccupation().toString());
                        line.appendCsvFn(record.getIndiMarriedComment().toString());

                        line.appendCsvFn(record.getIndiFatherLastName().toString());
                        line.appendCsvFn(record.getIndiFatherFirstName().toString());
                        line.appendCsvFn(record.getIndiFatherDead().getValue());
                        line.appendCsvFn(record.getIndiFatherOccupation().toString());
                        line.appendCsvFn(record.getIndiFatherComment().toString());

                        line.appendCsvFn(record.getIndiMotherLastName().toString());
                        line.appendCsvFn(record.getIndiMotherFirstName().toString());
                        line.appendCsvFn(record.getIndiMotherDead().getValue());
                        line.appendCsvFn(record.getIndiMotherOccupation().toString());
                        line.appendCsvFn(record.getIndiMotherComment().toString());

                        line.appendCsvFn(record.getWifeLastName().toString());
                        line.appendCsvFn(record.getWifeFirstName().toString());
                        line.appendCsvFn(record.getWifeSex().toString());
                        line.appendCsvFn(record.getWifePlace().toString());
                        line.appendCsvFn(record.getWifeBirthDate().toString());
                        line.appendCsvFn(record.getWifeAge().toString());
                        line.appendCsvFn(record.getWifeOccupation().toString());
                        line.appendCsvFn(record.getWifeComment().toString());

                        line.appendCsvFn(record.getWifeMarriedLastName().toString());
                        line.appendCsvFn(record.getWifeMarriedFirstName().toString());
                        line.appendCsvFn(record.getWifeMarriedDead().getValue());
                        line.appendCsvFn(record.getWifeMarriedOccupation().toString());
                        line.appendCsvFn(record.getWifeMarriedComment().toString());

                        line.appendCsvFn(record.getWifeFatherLastName().toString());
                        line.appendCsvFn(record.getWifeFatherFirstName().toString());
                        line.appendCsvFn(record.getWifeFatherDead().getValue());
                        line.appendCsvFn(record.getWifeFatherOccupation().toString());
                        line.appendCsvFn(record.getWifeFatherComment().toString());
                        line.appendCsvFn(record.getWifeMotherLastName().toString());
                        line.appendCsvFn(record.getWifeMotherFirstName().toString());
                        line.appendCsvFn(record.getWifeMotherDead().getValue());
                        line.appendCsvFn(record.getWifeMotherOccupation().toString());
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
                        line.appendCsv(String.valueOf(record.recordNo)); // numero d'enregistrement
                    }
                    line.appendCsv("\n");
                    writer.write(line.toString());

                } catch (Exception e) {
                    sb.append("Line ").append(" " ).append(e).append("\n");
                    sb.append("   ").append(line).append("\n");
                }
            }
            writer.close();

        } catch (Exception e) {
            sb.append("Error ").append(e).append("\n");
        }
        return sb;
    }

   
}
