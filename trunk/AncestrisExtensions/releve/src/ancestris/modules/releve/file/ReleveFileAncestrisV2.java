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
public class ReleveFileAncestrisV2 {

    final static private String fileSignature = "ANCESTRISV2";
    final static private char fieldSeparator = ';';
    final static private int nbFields = 82;

    /**
     * verifie si la premere ligne est conforme au format 
     * @param inputFile
     * @param sb  message d'erreur
     * @return
     */
    public static boolean isValidFile(File inputFile, StringBuilder sb) {
        try {
            BufferedReader br = new BufferedReader( new InputStreamReader(checkUtf8Bom(new FileInputStream(inputFile)),"UTF-8"));
            String[] fields = splitLine(br);
            if (fields == null) {
                sb.append(fileSignature + " ").append(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.EmptyFile"), inputFile.getName()));
                return false;
            }
        } catch (Exception ex) {
            sb.append(fileSignature + " ").append(ex.getMessage());
            return false;
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
            if (fields.length == nbFields) {
                if (fields[0].equals(fileSignature)) {
                    return fields;
                } else {
                    throw new Exception(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.LineBegin"), fileSignature, fields[0]));
                }
            } else {
                throw new Exception(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.FieldNb"), fields.length, nbFields));
            }
        } else {
            return null;
        }
    }

    // Format d'un releve
    enum Field {
        ancetris,
        nomCommune, codeCommune, nomDepartement, stateName, countryName, parish,
        eventType, eventTypeName, eventTypeComment,
        eventDate, cote, freeComment, notaryComment,
        indiLastName, indiFirstName, indiSex, indiBirthPlace, indiBirthDate, indiAge, indiOccupation, indiComment,
        indiMarriedLastName, indiMarriedFirstName, indiMarriedDead, indiMarriedOccupation, indiMarriedComment,
        indiFatherLastName, indiFatherFirstName, indiFatherAge, indiFatherDead, indiFatherOccupation, indiFatherComment,
        indiMotherLastName, indiMotherFirstName, indiMotherAge, indiMotherDead, indiMotherOccupation, indiMotherComment,
        wifeLastName, wifeFirstName, wifeSex, wifeBirthPlace, wifeBirthDate, wifeAge, wifeOccupation, wifeComment,
        wifeMarriedLastName, wifeMarriedFirstName, wifeMarriedDead, wifeMarriedOccupation, wifeMarriedComment,
        wifeFatherLastName, wifeFatherFirstName, wifeFatherAge, wifeFatherDead, wifeFatherOccupation, wifeFatherComment,
        wifeMotherLastName, wifeMotherFirstName, wifeMotherAge, wifeMotherDead, wifeMotherOccupation, wifeMotherComment,
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
        FileBuffer fileBuffer = new FileBuffer();
        try {
            fileBuffer = loadFile(checkUtf8Bom(new FileInputStream(inputFile)));
            return fileBuffer;
        } catch (IOException ex) {            
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
            String[] fields = new String[0];
            while (true) {
                try {
                    lineNumber++;
                    // je lis la ligne suivante
                    fields = splitLine(br);
                    if ( fields == null) {
                        break;
                    }
                    if (fields[Field.eventType.ordinal()].equals("N/M/D/V")) {
                        // je decompte l'entete
                        lineNumber--;
                        continue;
                    }
                    if ( lineNumber == 1) {
                        fileBuffer.setRegisterInfoPlace(fields[Field.nomCommune.ordinal()],
                                fields[Field.codeCommune.ordinal()],
                                fields[Field.nomDepartement.ordinal()],
                                fields[Field.stateName.ordinal()],
                                fields[Field.countryName.ordinal()] );
                    }

                    if (fields[Field.eventType.ordinal()].equals("N")) {
                        RecordBirth record = new RecordBirth();

                        record.setParish(fields[Field.parish.ordinal()]);
                        record.setEventDate(fields[Field.eventDate.ordinal()]);
                        record.setCote(fields[Field.cote.ordinal()]);
                        record.setFreeComment(fields[Field.freeComment.ordinal()]);

                        record.getIndi().set(
                                fields[Field.indiFirstName.ordinal()],
                                fields[Field.indiLastName.ordinal()],
                                fields[Field.indiSex.ordinal()],
                                "", // pas d'age a la naissance
                                fields[Field.indiBirthDate.ordinal()],
                                fields[Field.indiBirthPlace.ordinal()],
                                "", // pas d'adresse dans ce format
                                "", // pas de profession a la naissance
                                "", // pas de residence a la naissance
                                "", // pas d'adresse dans ce format
                                fields[Field.indiComment.ordinal()]);

                        record.getIndi().setFather(
                                fields[Field.indiFatherFirstName.ordinal()],
                                fields[Field.indiFatherLastName.ordinal()],
                                fields[Field.indiFatherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.indiFatherComment.ordinal()],
                                fields[Field.indiFatherDead.ordinal()],
                                fields[Field.indiFatherAge.ordinal()]);

                        record.getIndi().setMother(
                                fields[Field.indiMotherFirstName.ordinal()],
                                fields[Field.indiMotherLastName.ordinal()],
                                fields[Field.indiMotherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.indiMotherComment.ordinal()],
                                fields[Field.indiMotherDead.ordinal()],
                                fields[Field.indiMotherAge.ordinal()]);

                        record.getWitness1().setValue(
                                fields[Field.witness1FirstName.ordinal()],
                                fields[Field.witness1LastName.ordinal()],
                                fields[Field.witness1Occupation.ordinal()],
                                fields[Field.witness1Comment.ordinal()]);
                        record.getWitness2().setValue(
                                fields[Field.witness2FirstName.ordinal()],
                                fields[Field.witness2LastName.ordinal()],
                                fields[Field.witness2Occupation.ordinal()],
                                fields[Field.witness2Comment.ordinal()]);
                        record.getWitness3().setValue(
                                fields[Field.witness3FirstName.ordinal()],
                                fields[Field.witness3LastName.ordinal()],
                                fields[Field.witness3Occupation.ordinal()],
                                fields[Field.witness3Comment.ordinal()]);
                        record.getWitness4().setValue(
                                fields[Field.witness4FirstName.ordinal()],
                                fields[Field.witness4LastName.ordinal()],
                                fields[Field.witness4Occupation.ordinal()],
                                fields[Field.witness4Comment.ordinal()]);

                       
                        record.setGeneralComment(fields[Field.generalComment.ordinal()]);
                        fileBuffer.addRecord(record);

                    } else if (fields[Field.eventType.ordinal()].equals("M")) {
                        RecordMarriage record = new RecordMarriage();

                        record.setParish(fields[Field.parish.ordinal()]);
                        record.setEventDate(fields[Field.eventDate.ordinal()]);
                        record.setCote(fields[Field.cote.ordinal()]);
                        record.setFreeComment(fields[Field.freeComment.ordinal()]);

                        record.setEventDate(fields[Field.eventDate.ordinal()]);

                        record.getIndi().set(
                                fields[Field.indiFirstName.ordinal()],
                                fields[Field.indiLastName.ordinal()],
                                "M",
                                fields[Field.indiAge.ordinal()],
                                fields[Field.indiBirthDate.ordinal()],
                                fields[Field.indiBirthPlace.ordinal()],
                                "", // pas d'adresse dans ce format
                                fields[Field.indiOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.indiComment.ordinal()]);

                        record.getIndi().setMarried(
                                fields[Field.indiMarriedFirstName.ordinal()],
                                fields[Field.indiMarriedLastName.ordinal()],
                                //"F",
                                fields[Field.indiMarriedOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.indiMarriedComment.ordinal()],
                                fields[Field.indiMarriedDead.ordinal()]);

                        record.getIndi().setFather(
                                fields[Field.indiFatherFirstName.ordinal()],
                                fields[Field.indiFatherLastName.ordinal()],
                                fields[Field.indiFatherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.indiFatherComment.ordinal()],
                                fields[Field.indiFatherDead.ordinal()],
                                fields[Field.indiFatherAge.ordinal()]);

                        record.getIndi().setMother(
                                fields[Field.indiMotherFirstName.ordinal()],
                                fields[Field.indiMotherLastName.ordinal()],
                                fields[Field.indiMotherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.indiMotherComment.ordinal()],
                                fields[Field.indiMotherDead.ordinal()],
                                fields[Field.indiMotherAge.ordinal()]);

                        record.getWife().set(
                                fields[Field.wifeFirstName.ordinal()],
                                fields[Field.wifeLastName.ordinal()],
                                "F",
                                fields[Field.wifeAge.ordinal()],
                                fields[Field.wifeBirthDate.ordinal()],
                                fields[Field.wifeBirthPlace.ordinal()],
                                "", // pas d'adresse dans ce format
                                fields[Field.wifeOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.wifeComment.ordinal()]);

                        record.getWife().setMarried(
                                fields[Field.wifeMarriedFirstName.ordinal()],
                                fields[Field.wifeMarriedLastName.ordinal()],
                                //"M",
                                fields[Field.wifeMarriedOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.wifeMarriedComment.ordinal()],
                                fields[Field.wifeMarriedDead.ordinal()]);

                        record.getWife().setFather(
                                fields[Field.wifeFatherFirstName.ordinal()],
                                fields[Field.wifeFatherLastName.ordinal()],
                                fields[Field.wifeFatherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.wifeFatherComment.ordinal()],
                                fields[Field.wifeFatherDead.ordinal()],
                                fields[Field.wifeFatherAge.ordinal()]);

                        record.getWife().setMother(
                                fields[Field.wifeMotherFirstName.ordinal()],
                                fields[Field.wifeMotherLastName.ordinal()],
                                fields[Field.wifeMotherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.wifeMotherComment.ordinal()],
                                fields[Field.wifeMotherDead.ordinal()],
                                fields[Field.wifeMotherAge.ordinal()]);

                        record.getWitness1().setValue(
                                fields[Field.witness1FirstName.ordinal()],
                                fields[Field.witness1LastName.ordinal()],
                                fields[Field.witness1Occupation.ordinal()],
                                fields[Field.witness1Comment.ordinal()]);
                        record.getWitness2().setValue(
                                fields[Field.witness2FirstName.ordinal()],
                                fields[Field.witness2LastName.ordinal()],
                                fields[Field.witness2Occupation.ordinal()],
                                fields[Field.witness2Comment.ordinal()]);
                        record.getWitness3().setValue(
                                fields[Field.witness3FirstName.ordinal()],
                                fields[Field.witness3LastName.ordinal()],
                                fields[Field.witness3Occupation.ordinal()],
                                fields[Field.witness3Comment.ordinal()]);
                        record.getWitness4().setValue(
                                fields[Field.witness4FirstName.ordinal()],
                                fields[Field.witness4LastName.ordinal()],
                                fields[Field.witness4Occupation.ordinal()],
                                fields[Field.witness4Comment.ordinal()]);

                        record.setGeneralComment(fields[Field.generalComment.ordinal()]);
                        fileBuffer.addRecord(record);

                    } else if (fields[Field.eventType.ordinal()].equals("D")) {
                        RecordDeath record = new RecordDeath();

                        record.setParish(fields[Field.parish.ordinal()]);
                        record.setEventDate(fields[Field.eventDate.ordinal()]);
                        record.setCote(fields[Field.cote.ordinal()]);
                        record.setFreeComment(fields[Field.freeComment.ordinal()]);

                        record.setEventDate(fields[Field.eventDate.ordinal()]);

                        record.getIndi().set(
                                fields[Field.indiFirstName.ordinal()],
                                fields[Field.indiLastName.ordinal()],
                                fields[Field.indiSex.ordinal()],
                                fields[Field.indiAge.ordinal()],
                                fields[Field.indiBirthDate.ordinal()],
                                fields[Field.indiBirthPlace.ordinal()],
                                "", // pas d'adresse dans ce format
                                fields[Field.indiOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.indiComment.ordinal()]);

                        record.getIndi().setMarried(
                                fields[Field.indiMarriedFirstName.ordinal()],
                                fields[Field.indiMarriedLastName.ordinal()],
                                //fields[Field.indiMarriedSex.ordinal()].equals("M") ? "F" : "M",
                                fields[Field.indiMarriedOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.indiMarriedComment.ordinal()],
                                fields[Field.indiMarriedDead.ordinal()]); 

                        record.getIndi().setFather(
                                fields[Field.indiFatherFirstName.ordinal()],
                                fields[Field.indiFatherLastName.ordinal()],
                                fields[Field.indiFatherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.indiFatherComment.ordinal()],
                                fields[Field.indiFatherDead.ordinal()],
                                fields[Field.indiFatherAge.ordinal()]);

                        record.getIndi().setMother(
                                fields[Field.indiMotherFirstName.ordinal()],
                                fields[Field.indiMotherLastName.ordinal()],
                                fields[Field.indiMotherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.indiMotherComment.ordinal()],
                                fields[Field.indiMotherDead.ordinal()],
                                fields[Field.indiMotherAge.ordinal()]);

                        record.getWitness1().setValue(
                                fields[Field.witness1FirstName.ordinal()],
                                fields[Field.witness1LastName.ordinal()],
                                fields[Field.witness1Occupation.ordinal()],
                                fields[Field.witness1Comment.ordinal()]);
                        record.getWitness2().setValue(
                                fields[Field.witness2FirstName.ordinal()],
                                fields[Field.witness2LastName.ordinal()],
                                fields[Field.witness2Occupation.ordinal()],
                                fields[Field.witness2Comment.ordinal()]);
                        record.getWitness3().setValue(
                                fields[Field.witness3FirstName.ordinal()],
                                fields[Field.witness3LastName.ordinal()],
                                fields[Field.witness3Occupation.ordinal()],
                                fields[Field.witness3Comment.ordinal()]);
                        record.getWitness4().setValue(
                                fields[Field.witness4FirstName.ordinal()],
                                fields[Field.witness4LastName.ordinal()],
                                fields[Field.witness4Occupation.ordinal()],
                                fields[Field.witness4Comment.ordinal()]);

                        record.setGeneralComment(fields[Field.generalComment.ordinal()]);
                        fileBuffer.addRecord(record);

                    } else if (fields[Field.eventType.ordinal()].equals("V")) {
                        RecordMisc record = new RecordMisc();
                        record.setEventType(fields[Field.eventTypeName.ordinal()]);
                        record.setNotary(fields[Field.notaryComment.ordinal()]);

                        record.setParish(fields[Field.parish.ordinal()]);
                        record.setEventDate(fields[Field.eventDate.ordinal()]);
                        record.setCote(fields[Field.cote.ordinal()]);
                        record.setFreeComment(fields[Field.freeComment.ordinal()]);

                        record.setEventDate(fields[Field.eventDate.ordinal()]);

                        record.getIndi().set(
                                fields[Field.indiFirstName.ordinal()],
                                fields[Field.indiLastName.ordinal()],
                                fields[Field.indiSex.ordinal()],
                                fields[Field.indiAge.ordinal()],
                                fields[Field.indiBirthDate.ordinal()],
                                fields[Field.indiBirthPlace.ordinal()],
                                "", // pas d'adresse dans ce format
                                fields[Field.indiOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.indiComment.ordinal()]);

                        record.getIndi().setMarried(
                                fields[Field.indiMarriedFirstName.ordinal()],
                                fields[Field.indiMarriedLastName.ordinal()],
                                //fields[Field.indiSex.ordinal()].equals("M") ? "F" : "M",
                                fields[Field.indiMarriedOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.indiMarriedComment.ordinal()],
                                fields[Field.indiMarriedDead.ordinal()]);

                        record.getIndi().setFather(
                                fields[Field.indiFatherFirstName.ordinal()],
                                fields[Field.indiFatherLastName.ordinal()],
                                fields[Field.indiFatherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.indiFatherComment.ordinal()],
                                fields[Field.indiFatherDead.ordinal()],
                                fields[Field.indiFatherAge.ordinal()]);

                        record.getIndi().setMother(
                                fields[Field.indiMotherFirstName.ordinal()],
                                fields[Field.indiMotherLastName.ordinal()],
                                fields[Field.indiMotherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.indiMotherComment.ordinal()],
                                fields[Field.indiMotherDead.ordinal()],
                                fields[Field.indiMotherAge.ordinal()]);

                        record.getWife().set(
                                fields[Field.wifeFirstName.ordinal()],
                                fields[Field.wifeLastName.ordinal()],
                                fields[Field.wifeSex.ordinal()],
                                fields[Field.wifeAge.ordinal()],
                                fields[Field.wifeBirthDate.ordinal()],
                                fields[Field.wifeBirthPlace.ordinal()],
                                "", // pas d'adresse dans ce format
                                fields[Field.wifeOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.wifeComment.ordinal()]);  //décédé

                        record.getWife().setMarried(
                                fields[Field.wifeMarriedFirstName.ordinal()],
                                fields[Field.wifeMarriedLastName.ordinal()],
                                //fields[Field.wifeSex.ordinal()].equals("M") ? "F" : "M",
                                fields[Field.wifeMarriedOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.wifeMarriedComment.ordinal()],
                                fields[Field.wifeMarriedDead.ordinal()]);

                        record.getWife().setFather(
                                fields[Field.wifeFatherFirstName.ordinal()],
                                fields[Field.wifeFatherLastName.ordinal()],
                                fields[Field.wifeFatherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.wifeFatherComment.ordinal()],
                                fields[Field.wifeFatherDead.ordinal()],
                                fields[Field.wifeFatherAge.ordinal()]);

                        record.getWife().setMother(
                                fields[Field.wifeMotherFirstName.ordinal()],
                                fields[Field.wifeMotherLastName.ordinal()],
                                fields[Field.wifeMotherOccupation.ordinal()],
                                "", // pas de residence dans ce format
                                "", // pas d'adresse dans ce format
                                fields[Field.wifeMotherComment.ordinal()],
                                fields[Field.wifeMotherDead.ordinal()],
                                fields[Field.wifeMotherAge.ordinal()]);

                        record.getWitness1().setValue(
                                fields[Field.witness1FirstName.ordinal()],
                                fields[Field.witness1LastName.ordinal()],
                                fields[Field.witness1Occupation.ordinal()],
                                fields[Field.witness1Comment.ordinal()]);
                        record.getWitness2().setValue(
                                fields[Field.witness2FirstName.ordinal()],
                                fields[Field.witness2LastName.ordinal()],
                                fields[Field.witness2Occupation.ordinal()],
                                fields[Field.witness2Comment.ordinal()]);
                        record.getWitness3().setValue(
                                fields[Field.witness3FirstName.ordinal()],
                                fields[Field.witness3LastName.ordinal()],
                                fields[Field.witness3Occupation.ordinal()],
                                fields[Field.witness3Comment.ordinal()]);
                        record.getWitness4().setValue(
                                fields[Field.witness4FirstName.ordinal()],
                                fields[Field.witness4LastName.ordinal()],
                                fields[Field.witness4Occupation.ordinal()],
                                fields[Field.witness4Comment.ordinal()]);

                        record.setGeneralComment(fields[Field.generalComment.ordinal()]);
                        fileBuffer.addRecord(record);                                           
                    } else {
                        fileBuffer.append(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.LineNo"), lineNumber ));
                        fileBuffer.append("\n");
                        fileBuffer.append(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.UnknownEventType")).append(" ");
                        fileBuffer.append(fields[Field.eventType.ordinal()]);
                        fileBuffer.append("\n");
                    }

                } catch (Exception e) {
                    // je trace l'erreur dans le buffer de sortie
                    fileBuffer.append(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.LineNo"), lineNumber ));
                    fileBuffer.append("\n");
                    fileBuffer.append(e.toString()).append("\n");
                }

                    
            } // while
            br.close();
        } catch (IOException e) {
            fileBuffer.append(e.toString()).append("\n");
        }

        return fileBuffer;
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
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileName, append), "UTF-8") ;
            
            // j'ajoute l'entete si le mode append n'est pas demandé
            if( ! append) {
                writer.write(getHeader().toString());
            }
            
            // j'ajoute les autres lignes
            for (int index = 0; index < recordModel.getRowCount(); index++) {
                Line line = new Line(fieldSeparator);
                Record record = recordModel.getRecord(index);
                if( recordType != null && recordType != record.getType()) {
                    continue;
                }
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

                        line.appendCsvFn(record.getIndi().getLastName().getValue());
                        line.appendCsvFn(record.getIndi().getFirstName().getValue());
                        line.appendCsvFn(record.getIndi().getSex().toString());
                        line.appendCsvFn(record.getIndi().getBirthPlace().toString()); // IndiBirthDate
                        line.appendCsvFn(record.getIndi().getBirthDate().toString()); // IndiBirthDate
                        line.appendCsvFn(""); // age
                        line.appendCsvFn(""); // occupation
                        line.appendCsvFn(record.getIndi().getComment() .toString());

                        line.appendCsvFn(""); // IndiMarriedLastName
                        line.appendCsvFn(""); // IndiMarriedFirstName
                        line.appendCsvFn(""); // IndiMarriedDead
                        line.appendCsvFn(""); // IndiMarriedOccupation
                        line.appendCsvFn(""); // IndiMarriedComment

                        line.appendCsvFn(record.getIndi().getFatherLastName().toString());
                        line.appendCsvFn(record.getIndi().getFatherFirstName().toString());
                        line.appendCsvFn(record.getIndi().getFatherAge().getValue());
                        line.appendCsvFn(record.getIndi().getFatherDead().getValue());
                        line.appendCsvFn(record.getIndi().getFatherOccupation().toString());
                        line.appendCsvFn(record.getIndi().getFatherComment().toString());
                        line.appendCsvFn(record.getIndi().getMotherLastName().toString());
                        line.appendCsvFn(record.getIndi().getMotherFirstName().toString());
                        line.appendCsvFn(record.getIndi().getMotherAge().getValue());
                        line.appendCsvFn(record.getIndi().getMotherDead().getValue());
                        line.appendCsvFn(record.getIndi().getMotherOccupation().toString());
                        line.appendCsvFn(record.getIndi().getMotherComment().toString());

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
                        line.appendCsvFn(""); // WifeFatherAge
                        line.appendCsvFn(""); // WifeFatherDead
                        line.appendCsvFn(""); // WifeFatherOccupation
                        line.appendCsvFn(""); // WifeFatherComment
                        line.appendCsvFn(""); // WifeMotherLastName
                        line.appendCsvFn(""); // WifeMotherFirstName
                        line.appendCsvFn(""); // WifeMotherAge
                        line.appendCsvFn(""); // WifeMotherDead
                        line.appendCsvFn(""); // WifeMotherOccupation
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

                        line.appendCsvFn(record.getIndi().getLastName().getValue());
                        line.appendCsvFn(record.getIndi().getFirstName().getValue());
                        line.appendCsvFn(""); // IndiSex
                        line.appendCsvFn(record.getIndi().getBirthPlace().toString());
                        line.appendCsvFn(record.getIndi().getBirthDate().toString());
                        line.appendCsvFn(record.getIndi().getAge().getValue());
                        line.appendCsvFn(record.getIndi().getOccupation().toString());
                        line.appendCsvFn(record.getIndi().getComment() .toString());
                        
                        line.appendCsvFn(record.getIndi().getMarriedLastName().toString());
                        line.appendCsvFn(record.getIndi().getMarriedFirstName().toString());
                        line.appendCsvFn(record.getIndi().getMarriedDead().getValue());
                        line.appendCsvFn(record.getIndi().getMarriedOccupation().toString());
                        line.appendCsvFn(record.getIndi().getMarriedComment().toString());

                        line.appendCsvFn(record.getIndi().getFatherLastName().toString());
                        line.appendCsvFn(record.getIndi().getFatherFirstName().toString());
                        line.appendCsvFn(record.getIndi().getFatherAge().getValue());
                        line.appendCsvFn(record.getIndi().getFatherDead().getValue());
                        line.appendCsvFn(record.getIndi().getFatherOccupation().toString());
                        line.appendCsvFn(record.getIndi().getFatherComment().toString());
                        
                        line.appendCsvFn(record.getIndi().getMotherLastName().toString());
                        line.appendCsvFn(record.getIndi().getMotherFirstName().toString());
                        line.appendCsvFn(record.getIndi().getMotherAge().getValue());
                        line.appendCsvFn(record.getIndi().getMotherDead().getValue());
                        line.appendCsvFn(record.getIndi().getMotherOccupation().toString());
                        line.appendCsvFn(record.getIndi().getMotherComment().toString());
                        
                        line.appendCsvFn(record.getWife().getLastName().toString());
                        line.appendCsvFn(record.getWife().getFirstName().toString());
                        line.appendCsvFn(""); //WifeSex
                        line.appendCsvFn(record.getWife().getBirthPlace().toString());
                        line.appendCsvFn(record.getWife().getBirthDate().toString());
                        line.appendCsvFn(record.getWife().getAge().getValue());
                        line.appendCsvFn(record.getWife().getOccupation().toString());
                        line.appendCsvFn(record.getWife().getComment().toString());
                        
                        line.appendCsvFn(record.getWife().getMarriedLastName().toString());
                        line.appendCsvFn(record.getWife().getMarriedFirstName().toString());
                        line.appendCsvFn(record.getWife().getMarriedDead().getValue());
                        line.appendCsvFn(record.getWife().getMarriedOccupation().toString());
                        line.appendCsvFn(record.getWife().getMarriedComment().toString());

                        line.appendCsvFn(record.getWife().getFatherLastName().toString());
                        line.appendCsvFn(record.getWife().getFatherFirstName().toString());
                        line.appendCsvFn(record.getWife().getFatherAge().getValue());
                        line.appendCsvFn(record.getWife().getFatherDead().getValue());
                        line.appendCsvFn(record.getWife().getFatherOccupation().toString());
                        line.appendCsvFn(record.getWife().getFatherComment().toString());
                        line.appendCsvFn(record.getWife().getMotherLastName().toString());
                        line.appendCsvFn(record.getWife().getMotherFirstName().toString());
                        line.appendCsvFn(record.getWife().getMotherAge().getValue());
                        line.appendCsvFn(record.getWife().getMotherDead().getValue());
                        line.appendCsvFn(record.getWife().getMotherOccupation().toString());
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

                        line.appendCsvFn(record.getIndi().getLastName().toString());
                        line.appendCsvFn(record.getIndi().getFirstName().toString());
                        line.appendCsvFn(record.getIndi().getSex().toString());
                        line.appendCsvFn(record.getIndi().getBirthPlace().toString());
                        line.appendCsvFn(record.getIndi().getBirthDate().toString());
                        line.appendCsvFn(record.getIndi().getAge().getValue());
                        line.appendCsvFn(record.getIndi().getOccupation().toString());
                        line.appendCsvFn(record.getIndi().getComment() .toString());

                        line.appendCsvFn(record.getIndi().getMarriedLastName().toString());
                        line.appendCsvFn(record.getIndi().getMarriedFirstName().toString());
                        line.appendCsvFn(record.getIndi().getMarriedDead().getValue());
                        line.appendCsvFn(record.getIndi().getMarriedOccupation().toString());
                        line.appendCsvFn(record.getIndi().getMarriedComment().toString());

                        line.appendCsvFn(record.getIndi().getFatherLastName().toString());
                        line.appendCsvFn(record.getIndi().getFatherFirstName().toString());
                        line.appendCsvFn(record.getIndi().getFatherAge().getValue());
                        line.appendCsvFn(record.getIndi().getFatherDead().getValue());
                        line.appendCsvFn(record.getIndi().getFatherOccupation().toString());
                        line.appendCsvFn(record.getIndi().getFatherComment().toString());
                        line.appendCsvFn(record.getIndi().getMotherLastName().toString());
                        line.appendCsvFn(record.getIndi().getMotherFirstName().toString());
                        line.appendCsvFn(record.getIndi().getMotherAge().getValue());
                        line.appendCsvFn(record.getIndi().getMotherDead().getValue());
                        line.appendCsvFn(record.getIndi().getMotherOccupation().toString());
                        line.appendCsvFn(record.getIndi().getMotherComment().toString());

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
                        line.appendCsvFn(""); // WifeFatherAge
                        line.appendCsvFn(""); // WifeFatherDead
                        line.appendCsvFn(""); // WifeFatherOccupation
                        line.appendCsvFn(""); // WifeFatherComment
                        line.appendCsvFn(""); // WifeMotherLastName
                        line.appendCsvFn(""); // WifeMotherFirstName
                        line.appendCsvFn(""); // WifeMotherAge
                        line.appendCsvFn(""); // WifeMotherDead
                        line.appendCsvFn(""); // WifeMotherOccupation
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

                        line.appendCsvFn(fileSignature);
                        line.appendCsvFn(placeManager.getCityName());
                        line.appendCsvFn(placeManager.getCityCode());
                        line.appendCsvFn(placeManager.getCountyName());
                        line.appendCsvFn(placeManager.getStateName());
                        line.appendCsvFn(placeManager.getCountryName());
                        line.appendCsvFn(record.getParish().toString());
                        line.appendCsvFn("V");
                        line.appendCsvFn(record.getEventType().getName());
                        line.appendCsvFn("");
                        line.appendCsvFn(record.getEventDateString());
                        line.appendCsvFn(record.getCote().toString());
                        line.appendCsvFn(record.getFreeComment().toString());
                        line.appendCsvFn(record.getNotary().toString());

                        line.appendCsvFn(record.getIndi().getLastName().toString());
                        line.appendCsvFn(record.getIndi().getFirstName().toString());
                        line.appendCsvFn(record.getIndi().getSex().toString());
                        line.appendCsvFn(record.getIndi().getBirthPlace().toString());
                        line.appendCsvFn(record.getIndi().getBirthDate().toString());
                        line.appendCsvFn(record.getIndi().getAge().getValue());
                        line.appendCsvFn(record.getIndi().getOccupation().toString());
                        line.appendCsvFn(record.getIndi().getComment() .toString());

                        line.appendCsvFn(record.getIndi().getMarriedLastName().toString());
                        line.appendCsvFn(record.getIndi().getMarriedFirstName().toString());
                        line.appendCsvFn(record.getIndi().getMarriedDead().getValue());
                        line.appendCsvFn(record.getIndi().getMarriedOccupation().toString());
                        line.appendCsvFn(record.getIndi().getMarriedComment().toString());

                        line.appendCsvFn(record.getIndi().getFatherLastName().toString());
                        line.appendCsvFn(record.getIndi().getFatherFirstName().toString());
                        line.appendCsvFn(record.getIndi().getFatherAge().getValue());
                        line.appendCsvFn(record.getIndi().getFatherDead().getValue());
                        line.appendCsvFn(record.getIndi().getFatherOccupation().toString());
                        line.appendCsvFn(record.getIndi().getFatherComment().toString());

                        line.appendCsvFn(record.getIndi().getMotherLastName().toString());
                        line.appendCsvFn(record.getIndi().getMotherFirstName().toString());
                        line.appendCsvFn(record.getIndi().getMotherAge().getValue());
                        line.appendCsvFn(record.getIndi().getMotherDead().getValue());
                        line.appendCsvFn(record.getIndi().getMotherOccupation().toString());
                        line.appendCsvFn(record.getIndi().getMotherComment().toString());

                        line.appendCsvFn(record.getWife().getLastName().toString());
                        line.appendCsvFn(record.getWife().getFirstName().toString());
                        line.appendCsvFn(record.getWife().getSex().toString());
                        line.appendCsvFn(record.getWife().getBirthPlace().toString());
                        line.appendCsvFn(record.getWife().getBirthDate().toString());
                        line.appendCsvFn(record.getWife().getAge().getValue());
                        line.appendCsvFn(record.getWife().getOccupation().toString());
                        line.appendCsvFn(record.getWife().getComment().toString());

                        line.appendCsvFn(record.getWife().getMarriedLastName().toString());
                        line.appendCsvFn(record.getWife().getMarriedFirstName().toString());
                        line.appendCsvFn(record.getWife().getMarriedDead().getValue());
                        line.appendCsvFn(record.getWife().getMarriedOccupation().toString());
                        line.appendCsvFn(record.getWife().getMarriedComment().toString());

                        line.appendCsvFn(record.getWife().getFatherLastName().toString());
                        line.appendCsvFn(record.getWife().getFatherFirstName().toString());
                        line.appendCsvFn(record.getWife().getFatherAge().getValue());
                        line.appendCsvFn(record.getWife().getFatherDead().getValue());
                        line.appendCsvFn(record.getWife().getFatherOccupation().toString());
                        line.appendCsvFn(record.getWife().getFatherComment().toString());
                        line.appendCsvFn(record.getWife().getMotherLastName().toString());
                        line.appendCsvFn(record.getWife().getMotherFirstName().toString());
                        line.appendCsvFn(record.getWife().getMotherAge().getValue());
                        line.appendCsvFn(record.getWife().getMotherDead().getValue());
                        line.appendCsvFn(record.getWife().getMotherOccupation().toString());
                        line.appendCsvFn(record.getWife().getMotherComment().toString());

                        for(Record.Witness witness : record.getWitnesses()) {
                            line.appendCsvFn(witness.getLastName().toString());
                            line.appendCsvFn(witness.getFirstName().toString());
                            line.appendCsvFn(witness.getOccupation().toString());
                            line.appendCsvFn(witness.getComment().toString());
                        }

                        line.appendCsvFn(record.getGeneralComment().toString());
                        line.appendCsv(String.valueOf(index)); // numero d'enregistrement
                    }
                    line.appendCsv("\n");
                    writer.write(line.toString());

                } catch (IOException e) {
                    sb.append("Line ").append(" " ).append(e).append("\n");
                    sb.append("   ").append(line).append("\n");
                }
            }
            writer.close();

        } catch (IOException e) {
            sb.append(e).append("\n");
        }
        return sb;
    }
    
    
    /**
     * retourn la ligne d'entete
     * @return 
     */
    static private Line getHeader() {
        Line line = new Line(fieldSeparator);
        line.appendCsvFn(fileSignature);
        line.appendCsvFn("CityName");
        line.appendCsvFn("CityCode");
        line.appendCsvFn("County");
        line.appendCsvFn("State");
        line.appendCsvFn("Country");
        line.appendCsvFn("Parish");
        line.appendCsvFn("N/M/D/V");
        line.appendCsvFn("EventTypeTag"); 
        line.appendCsvFn("EventTypeName");
        line.appendCsvFn("EventDate");
        line.appendCsvFn("Cote");
        line.appendCsvFn("FreeComment");
        line.appendCsvFn("Notary"); 

        line.appendCsvFn("IndiLastName");
        line.appendCsvFn("IndiFirstName");
        line.appendCsvFn("IndiSex");
        line.appendCsvFn("IndiPlace"); 
        line.appendCsvFn("IndiBirthDate"); 
        line.appendCsvFn("IndiAge"); 
        line.appendCsvFn("IndiOccupation"); 
        line.appendCsvFn("IndiComment");

        line.appendCsvFn("IndiMarriedLastName"); 
        line.appendCsvFn("IndiMarriedFirstName"); 
        line.appendCsvFn("IndiMarriedDead");
        line.appendCsvFn("IndiMarriedOccupation"); 
        line.appendCsvFn("IndiMarriedComment"); 

        line.appendCsvFn("IndiFatherLastName");
        line.appendCsvFn("IndiFatherFirstName");
        line.appendCsvFn("IndiFatherAge");
        line.appendCsvFn("IndiFatherDead");
        line.appendCsvFn("IndiFatherOccupation");
        line.appendCsvFn("IndiFatherComment");
        line.appendCsvFn("IndiMotherLastName");
        line.appendCsvFn("IndiMotherFirstName");
        line.appendCsvFn("IndiMotherAge");
        line.appendCsvFn("IndiMotherDead");
        line.appendCsvFn("IndiMotherOccupation");
        line.appendCsvFn("IndiMotherComment");

        line.appendCsvFn("WifeLastName");
        line.appendCsvFn("WifeFirstName");
        line.appendCsvFn("WifeSex");
        line.appendCsvFn("WifePlace");
        line.appendCsvFn("WifeBirthDate");
        line.appendCsvFn("WifeAge");
        line.appendCsvFn("WifeOccupation");
        line.appendCsvFn("WifeComment");

        line.appendCsvFn("WifeMarriedLastName");
        line.appendCsvFn("WifeMarriedFirstName");
        line.appendCsvFn("WifeMarriedDead");
        line.appendCsvFn("WifeMarriedOccupation");
        line.appendCsvFn("WifeMarriedComment");

        line.appendCsvFn("WifeFatherLastName");
        line.appendCsvFn("WifeFatherFirstName");
        line.appendCsvFn("WifeFatherAge");
        line.appendCsvFn("WifeFatherDead");
        line.appendCsvFn("WifeFatherOccupation");
        line.appendCsvFn("WifeFatherComment");
        line.appendCsvFn("WifeMotherLastName");
        line.appendCsvFn("WifeMotherFirstName");
        line.appendCsvFn("WifeMotherAge");
        line.appendCsvFn("WifeMotherDead");
        line.appendCsvFn("WifeMotherOccupation");
        line.appendCsvFn("WifeMotherComment");

        line.appendCsvFn("Witness1LastName");
        line.appendCsvFn("Witness1FirstName");
        line.appendCsvFn("Witness1Occupation");
        line.appendCsvFn("Witness1Comment");

        line.appendCsvFn("Witness2LastName");
        line.appendCsvFn("Witness2FirstName");
        line.appendCsvFn("Witness2Occupation");
        line.appendCsvFn("Witness2Comment");

        line.appendCsvFn("Witness3LastName");
        line.appendCsvFn("Witness3FirstName");
        line.appendCsvFn("Witness3Occupation");
        line.appendCsvFn("Witness3Comment");

        line.appendCsvFn("Witness4LastName");
        line.appendCsvFn("Witness4FirstName");
        line.appendCsvFn("Witness4Occupation");
        line.appendCsvFn("Witness4Comment");

        line.appendCsvFn("GeneralComment");
        line.appendCsv("RecordNo"); 
        line.appendCsv("\n");
        return line;
    }

   
}
