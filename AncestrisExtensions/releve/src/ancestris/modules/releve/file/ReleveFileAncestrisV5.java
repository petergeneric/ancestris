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
public class ReleveFileAncestrisV5 {

    final static private String fileSignature = "ANCESTRISV5";
    final static private char fieldSeparator = ';';
    final static private int nbFields = 100;

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
        eventDate, secondDate, cote, freeComment, notaryComment,
        indiLastName, indiFirstName, indiSex, indiBirthPlace, indiBirthAddress, indiBirthDate, indiAge, indiOccupation, indiResidence, indiAddress, indiComment,
        indiMarriedLastName, indiMarriedFirstName, indiMarriedDead, indiMarriedOccupation, indiMarriedResidence, indiMarriedAddress, indiMarriedComment,
        indiFatherLastName, indiFatherFirstName, indiFatherAge, indiFatherDead, indiFatherOccupation, indiFatherResidence, indiFatherAddress, indiFatherComment,
        indiMotherLastName, indiMotherFirstName, indiMotherAge, indiMotherDead, indiMotherOccupation, indiMotherResidence, indiMotherAddress, indiMotherComment,
        wifeLastName, wifeFirstName, wifeSex, wifeBirthPlace, wifeBirthAddress, wifeBirthDate, wifeAge, wifeOccupation, wifeResidence, wifeAddress, wifeComment,
        wifeMarriedLastName, wifeMarriedFirstName, wifeMarriedDead, wifeMarriedOccupation, wifeMarriedResidence, wifeMarriedAddress, wifeMarriedComment,
        wifeFatherLastName, wifeFatherFirstName, wifeFatherAge, wifeFatherDead, wifeFatherOccupation, wifeFatherResidence, wifeFatherAddress, wifeFatherComment,
        wifeMotherLastName, wifeMotherFirstName, wifeMotherAge, wifeMotherDead, wifeMotherOccupation, wifeMotherResidence, wifeMotherAddress, wifeMotherComment,
        witness1LastName, witness1FirstName, witness1Occupation, witness1Comment,
        witness2LastName, witness2FirstName, witness2Occupation, witness2Comment,
        witness3LastName, witness3FirstName, witness3Occupation, witness3Comment,
        witness4LastName, witness4FirstName, witness4Occupation, witness4Comment,
        generalComment
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
            while (true) {
                try {
                    lineNumber++;
                    // je lis la ligne suivante
                    String[] fields = splitLine(br);
                    if ( fields == null) {
                        // fin du fichier atteinte
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
                    if( fields[Field.eventType.ordinal()].length() != 1 ) {
                        fileBuffer.append(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.LineNo"), lineNumber ));
                            fileBuffer.append("\n");
                            fileBuffer.append(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.UnknownEventType")).append(" ");
                            fileBuffer.append(fields[Field.eventType.ordinal()]);
                            fileBuffer.append("\n");
                            continue;
                    }
                    Record record; 
                    if (fields[Field.eventType.ordinal()].equals("N")) {
                        record = new RecordBirth();                        
                        
                    } else if (fields[Field.eventType.ordinal()].equals("M")) {
                        record = new RecordMarriage();                        
                    } else if (fields[Field.eventType.ordinal()].equals("D")) {
                        record = new RecordDeath();                        
                    } else if (fields[Field.eventType.ordinal()].equals("V")) {
                        record = new RecordMisc();
                    } else {
                        fileBuffer.append(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.LineNo"), lineNumber ));
                        fileBuffer.append("\n");
                        fileBuffer.append(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.UnknownEventType")).append(" ");
                        fileBuffer.append(fields[Field.eventType.ordinal()]);
                        fileBuffer.append("\n");
                        continue;
                    }
                        
                        record.setFieldValue(FieldType.eventType, fields[Field.eventTypeName.ordinal()]);
                        record.setFieldValue(FieldType.notary, fields[Field.notaryComment.ordinal()]);
                        record.setFieldValue(FieldType.parish, fields[Field.parish.ordinal()]);
                        record.setFieldValue(FieldType.eventDate, fields[Field.eventDate.ordinal()]);
                        record.setFieldValue(FieldType.secondDate, fields[Field.secondDate.ordinal()]);
                        record.setFieldValue(FieldType.cote, fields[Field.cote.ordinal()]);
                        record.setFieldValue(FieldType.freeComment,  fields[Field.freeComment.ordinal()]);

                        
                        record.setFieldValue(FieldType.indiFirstName,   fields[Field.indiFirstName.ordinal()]);
                        record.setFieldValue(FieldType.indiLastName,    fields[Field.indiLastName.ordinal()]);
                        record.setFieldValue(FieldType.indiSex,         fields[Field.indiSex.ordinal()]);
                        record.setFieldValue(FieldType.indiAge,         fields[Field.indiAge.ordinal()]);
                        record.setFieldValue(FieldType.indiBirthDate,   fields[Field.indiBirthDate.ordinal()]);
                        record.setFieldValue(FieldType.indiBirthPlace,  fields[Field.indiBirthPlace.ordinal()]);
                        record.setFieldValue(FieldType.indiBirthAddress, fields[Field.indiBirthAddress.ordinal()]);
                        record.setFieldValue(FieldType.indiOccupation,  fields[Field.indiOccupation.ordinal()]);
                        record.setFieldValue(FieldType.indiResidence,   fields[Field.indiResidence.ordinal()]);
                        record.setFieldValue(FieldType.indiAddress,     fields[Field.indiAddress.ordinal()]);
                        record.setFieldValue(FieldType.indiComment,     fields[Field.indiComment.ordinal()]);

                        record.setFieldValue(FieldType.indiMarriedFirstName, fields[Field.indiMarriedFirstName.ordinal()]);
                        record.setFieldValue(FieldType.indiMarriedLastName, fields[Field.indiMarriedLastName.ordinal()]);
                        //record.setFieldValue(FieldType.indiFirstName, //fields[Field.indiSex.ordinal()].equals("M") ? "F" : "M",
                        record.setFieldValue(FieldType.indiMarriedOccupation, fields[Field.indiMarriedOccupation.ordinal()]);
                        record.setFieldValue(FieldType.indiMarriedResidence, fields[Field.indiMarriedResidence.ordinal()]);
                        record.setFieldValue(FieldType.indiMarriedAddress, fields[Field.indiMarriedAddress.ordinal()]);
                        record.setFieldValue(FieldType.indiMarriedComment, fields[Field.indiMarriedComment.ordinal()]);
                        record.setFieldValue(FieldType.indiMarriedDead, fields[Field.indiMarriedDead.ordinal()]);

                        record.setFieldValue(FieldType.indiFatherFirstName, fields[Field.indiFatherFirstName.ordinal()]);
                        record.setFieldValue(FieldType.indiFatherLastName, fields[Field.indiFatherLastName.ordinal()]);
                        record.setFieldValue(FieldType.indiFatherOccupation, fields[Field.indiFatherOccupation.ordinal()]);
                        record.setFieldValue(FieldType.indiFatherResidence, fields[Field.indiFatherResidence.ordinal()]);
                        record.setFieldValue(FieldType.indiFatherAddress, fields[Field.indiFatherAddress.ordinal()]);
                        record.setFieldValue(FieldType.indiFatherComment, fields[Field.indiFatherComment.ordinal()]);
                        record.setFieldValue(FieldType.indiFatherDead, fields[Field.indiFatherDead.ordinal()]);
                        record.setFieldValue(FieldType.indiFatherAge, fields[Field.indiFatherAge.ordinal()]);
                        
                        record.setFieldValue(FieldType.indiMotherFirstName, fields[Field.indiMotherFirstName.ordinal()]);
                        record.setFieldValue(FieldType.indiMotherLastName, fields[Field.indiMotherLastName.ordinal()]);
                        record.setFieldValue(FieldType.indiMotherOccupation, fields[Field.indiMotherOccupation.ordinal()]);
                        record.setFieldValue(FieldType.indiMotherResidence, fields[Field.indiMotherResidence.ordinal()]);
                        record.setFieldValue(FieldType.indiMotherAddress, fields[Field.indiMotherAddress.ordinal()]);
                        record.setFieldValue(FieldType.indiMotherComment, fields[Field.indiMotherComment.ordinal()]);
                        record.setFieldValue(FieldType.indiMotherDead, fields[Field.indiMotherDead.ordinal()]);
                        record.setFieldValue(FieldType.indiMotherAge,  fields[Field.indiMotherAge.ordinal()]);

                        record.setFieldValue(FieldType.wifeFirstName, fields[Field.wifeFirstName.ordinal()]);
                        record.setFieldValue(FieldType.wifeLastName, fields[Field.wifeLastName.ordinal()]);
                        record.setFieldValue(FieldType.wifeSex, fields[Field.wifeSex.ordinal()]);
                        record.setFieldValue(FieldType.wifeAge, fields[Field.wifeAge.ordinal()]);
                        record.setFieldValue(FieldType.wifeBirthDate, fields[Field.wifeBirthDate.ordinal()]);
                        record.setFieldValue(FieldType.wifeBirthPlace, fields[Field.wifeBirthPlace.ordinal()]);
                        record.setFieldValue(FieldType.wifeBirthAddress, fields[Field.wifeBirthAddress.ordinal()]);
                        record.setFieldValue(FieldType.wifeOccupation, fields[Field.wifeOccupation.ordinal()]);
                        record.setFieldValue(FieldType.wifeResidence, fields[Field.wifeResidence.ordinal()]);
                        record.setFieldValue(FieldType.wifeAddress, fields[Field.wifeAddress.ordinal()]);
                        record.setFieldValue(FieldType.wifeComment, fields[Field.wifeComment.ordinal()]);

                        record.setFieldValue(FieldType.wifeMarriedFirstName, fields[Field.wifeMarriedFirstName.ordinal()]);
                        record.setFieldValue(FieldType.wifeMarriedLastName, fields[Field.wifeMarriedLastName.ordinal()]);
                        //record.setFieldValue(FieldType.wifeSex, //fields[Field.wifeSex.ordinal()].equals("M") ? "F" : "M",
                        record.setFieldValue(FieldType.wifeMarriedOccupation, fields[Field.wifeMarriedOccupation.ordinal()]);
                        record.setFieldValue(FieldType.wifeMarriedResidence, fields[Field.wifeMarriedResidence.ordinal()]);
                        record.setFieldValue(FieldType.wifeMarriedAddress, fields[Field.wifeMarriedAddress.ordinal()]);
                        record.setFieldValue(FieldType.wifeMarriedComment, fields[Field.wifeMarriedComment.ordinal()]);
                        record.setFieldValue(FieldType.wifeMarriedDead, fields[Field.wifeMarriedDead.ordinal()]);

                        record.setFieldValue(FieldType.wifeFatherFirstName, fields[Field.wifeFatherFirstName.ordinal()]);
                        record.setFieldValue(FieldType.wifeFatherLastName,  fields[Field.wifeFatherLastName.ordinal()]);
                        record.setFieldValue(FieldType.wifeFatherOccupation, fields[Field.wifeFatherOccupation.ordinal()]);
                        record.setFieldValue(FieldType.wifeFatherResidence, fields[Field.wifeFatherResidence.ordinal()]);
                        record.setFieldValue(FieldType.wifeFatherAddress,   fields[Field.wifeFatherAddress.ordinal()]);
                        record.setFieldValue(FieldType.wifeFatherComment,   fields[Field.wifeFatherComment.ordinal()]);
                        record.setFieldValue(FieldType.wifeFatherDead,      fields[Field.wifeFatherDead.ordinal()]);
                        record.setFieldValue(FieldType.wifeFatherAge,       fields[Field.wifeFatherAge.ordinal()]);

                        record.setFieldValue(FieldType.wifeMotherFirstName, fields[Field.wifeMotherFirstName.ordinal()]);
                        record.setFieldValue(FieldType.wifeMotherLastName, fields[Field.wifeMotherLastName.ordinal()]);
                        record.setFieldValue(FieldType.wifeMotherOccupation, fields[Field.wifeMotherOccupation.ordinal()]);
                        record.setFieldValue(FieldType.wifeMotherResidence, fields[Field.wifeMotherResidence.ordinal()]);
                        record.setFieldValue(FieldType.wifeMotherAddress, fields[Field.wifeMotherAddress.ordinal()]);
                        record.setFieldValue(FieldType.wifeMotherComment, fields[Field.wifeMotherComment.ordinal()]);
                        record.setFieldValue(FieldType.wifeMotherDead, fields[Field.wifeMotherDead.ordinal()]);
                        record.setFieldValue(FieldType.wifeMotherAge, fields[Field.wifeMotherAge.ordinal()]);


                        record.setFieldValue(FieldType.witness1FirstName,   fields[Field.witness1FirstName.ordinal()]);
                        record.setFieldValue(FieldType.witness1LastName,    fields[Field.witness1LastName.ordinal()]);
                        record.setFieldValue(FieldType.witness1Occupation,  fields[Field.witness1Occupation.ordinal()]);
                        record.setFieldValue(FieldType.witness1Comment,     fields[Field.witness1Comment.ordinal()]);
                     
                        record.setFieldValue(FieldType.witness2FirstName,   fields[Field.witness2FirstName.ordinal()]);
                        record.setFieldValue(FieldType.witness2LastName,    fields[Field.witness2LastName.ordinal()]);
                        record.setFieldValue(FieldType.witness2Occupation,  fields[Field.witness2Occupation.ordinal()]);
                        record.setFieldValue(FieldType.witness2Comment,     fields[Field.witness2Comment.ordinal()]);
                        
                        record.setFieldValue(FieldType.witness3FirstName,   fields[Field.witness3FirstName.ordinal()]);
                        record.setFieldValue(FieldType.witness3LastName,    fields[Field.witness3LastName.ordinal()]);
                        record.setFieldValue(FieldType.witness3Occupation,  fields[Field.witness3Occupation.ordinal()]);
                        record.setFieldValue(FieldType.witness3Comment,     fields[Field.witness3Comment.ordinal()]);
                        
                        record.setFieldValue(FieldType.witness4FirstName,   fields[Field.witness4FirstName.ordinal()]);
                        record.setFieldValue(FieldType.witness4LastName,    fields[Field.witness4LastName.ordinal()]);
                        record.setFieldValue(FieldType.witness4Occupation,  fields[Field.witness4Occupation.ordinal()]);
                        record.setFieldValue(FieldType.witness4Comment,     fields[Field.witness4Comment.ordinal()]);

                        record.setFieldValue(FieldType.generalComment, fields[Field.generalComment.ordinal()]);

                        fileBuffer.addRecord(record);




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
                Record record = recordModel.getRecord(index);
                if( recordType != null && recordType != record.getType()) {
                    continue;
                }
                String recordTypeString;
                switch ( record.getType() ) {
                    case BIRTH:
                        recordTypeString = "N";
                        break;
                    case MARRIAGE:
                        recordTypeString = "M";
                        break;
                    case DEATH:
                        recordTypeString = "D";
                        break;
                    case MISC:
                    default:
                        recordTypeString = "V";
                        break;
                 }
                            
                Line line = new Line(fieldSeparator);
                try {                     
                    line.appendCsvFn(fileSignature);
                    line.appendCsvFn(placeManager.getCityName());
                    line.appendCsvFn(placeManager.getCityCode());
                    line.appendCsvFn(placeManager.getCountyName());
                    line.appendCsvFn(placeManager.getStateName());
                    line.appendCsvFn(placeManager.getCountryName());
                    
                    line.appendCsvFn(record.getFieldValue(FieldType.parish));
                    line.appendCsvFn(recordTypeString);
                    line.appendCsvFn(record.getFieldValue(FieldType.eventType));
                    line.appendCsvFn("");     // unused field ( EventTypeName )
                    line.appendCsvFn(record.getFieldValue(FieldType.eventDate));
                    line.appendCsvFn(record.getFieldValue(FieldType.secondDate));
                    line.appendCsvFn(record.getFieldValue(FieldType.cote));
                    line.appendCsvFn(record.getFieldValue(FieldType.freeComment));
                    line.appendCsvFn(record.getFieldValue(FieldType.notary));

                    line.appendCsvFn(record.getFieldValue(FieldType.indiLastName));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiFirstName));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiSex));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiBirthPlace));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiBirthAddress));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiBirthDate));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiAge));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiOccupation));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiResidence));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiAddress));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiComment));

                    line.appendCsvFn(record.getFieldValue(FieldType.indiMarriedLastName));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiMarriedFirstName));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiMarriedDead));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiMarriedOccupation));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiMarriedResidence));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiMarriedAddress));                    
                    line.appendCsvFn(record.getFieldValue(FieldType.indiMarriedComment));

                    line.appendCsvFn(record.getFieldValue(FieldType.indiFatherLastName));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiFatherFirstName));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiFatherAge));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiFatherDead));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiFatherOccupation));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiFatherResidence));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiFatherAddress));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiFatherComment));

                    line.appendCsvFn(record.getFieldValue(FieldType.indiMotherLastName));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiMotherFirstName));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiMotherAge));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiMotherDead));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiMotherOccupation));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiMotherResidence));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiMotherAddress));
                    line.appendCsvFn(record.getFieldValue(FieldType.indiMotherComment));

                    line.appendCsvFn(record.getFieldValue(FieldType.wifeLastName));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeFirstName));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeSex));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeBirthPlace));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeBirthAddress));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeBirthDate));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeAge));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeOccupation));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeResidence));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeAddress));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeComment));

                    line.appendCsvFn(record.getFieldValue(FieldType.wifeMarriedLastName));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeMarriedFirstName));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeMarriedDead));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeMarriedOccupation));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeMarriedResidence));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeMarriedAddress));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeMarriedComment));

                    line.appendCsvFn(record.getFieldValue(FieldType.wifeFatherLastName));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeFatherFirstName));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeFatherAge));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeFatherDead));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeFatherOccupation));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeFatherResidence));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeFatherAddress));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeFatherComment));
                    
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeMotherLastName));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeMotherFirstName));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeMotherAge));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeMotherDead));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeMotherOccupation));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeMotherResidence));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeMotherAddress));
                    line.appendCsvFn(record.getFieldValue(FieldType.wifeMotherComment));

                    line.appendCsvFn(record.getFieldValue(FieldType.witness1LastName));
                    line.appendCsvFn(record.getFieldValue(FieldType.witness1FirstName));
                    line.appendCsvFn(record.getFieldValue(FieldType.witness1Occupation));
                    line.appendCsvFn(record.getFieldValue(FieldType.witness1Comment));

                    line.appendCsvFn(record.getFieldValue(FieldType.witness2LastName));
                    line.appendCsvFn(record.getFieldValue(FieldType.witness2FirstName));
                    line.appendCsvFn(record.getFieldValue(FieldType.witness2Occupation));
                    line.appendCsvFn(record.getFieldValue(FieldType.witness2Comment));

                    line.appendCsvFn(record.getFieldValue(FieldType.witness3LastName));
                    line.appendCsvFn(record.getFieldValue(FieldType.witness3FirstName));
                    line.appendCsvFn(record.getFieldValue(FieldType.witness3Occupation));
                    line.appendCsvFn(record.getFieldValue(FieldType.witness3Comment));

                    line.appendCsvFn(record.getFieldValue(FieldType.witness4LastName));
                    line.appendCsvFn(record.getFieldValue(FieldType.witness4FirstName));
                    line.appendCsvFn(record.getFieldValue(FieldType.witness4Occupation));
                    line.appendCsvFn(record.getFieldValue(FieldType.witness4Comment));
                    
                    line.appendCsv(record.getFieldValue(FieldType.generalComment));

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
        line.appendCsvFn("Country")
                ;
        line.appendCsvFn("Parish");
        line.appendCsvFn("N/M/D/V");
        line.appendCsvFn("EventTypeTag"); 
        line.appendCsvFn("EventTypeName");
        line.appendCsvFn("EventDate");
        line.appendCsvFn("SecondDate");
        line.appendCsvFn("Cote");
        line.appendCsvFn("FreeComment");
        line.appendCsvFn("Notary"); 

        line.appendCsvFn("IndiLastName");
        line.appendCsvFn("IndiFirstName");
        line.appendCsvFn("IndiSex");
        line.appendCsvFn("IndiBirthPlace"); 
        line.appendCsvFn("IndiBirthAddress"); 
        line.appendCsvFn("IndiBirthDate"); 
        line.appendCsvFn("IndiAge"); 
        line.appendCsvFn("IndiOccupation"); 
        line.appendCsvFn("IndiResidence"); 
        line.appendCsvFn("IndiAddress"); 
        line.appendCsvFn("IndiComment");

        line.appendCsvFn("IndiMarriedLastName"); 
        line.appendCsvFn("IndiMarriedFirstName"); 
        line.appendCsvFn("IndiMarriedDead");
        line.appendCsvFn("IndiMarriedOccupation"); 
        line.appendCsvFn("IndiMarriedResidence"); 
        line.appendCsvFn("IndiMarriedAddress"); 
        line.appendCsvFn("IndiMarriedComment"); 

        line.appendCsvFn("IndiFatherLastName");
        line.appendCsvFn("IndiFatherFirstName");
        line.appendCsvFn("IndiFatherAge");
        line.appendCsvFn("IndiFatherDead");
        line.appendCsvFn("IndiFatherOccupation");
        line.appendCsvFn("IndiFatherResidence"); 
        line.appendCsvFn("IndiFatherAddress"); 
        line.appendCsvFn("IndiFatherComment");
        
        line.appendCsvFn("IndiMotherLastName");
        line.appendCsvFn("IndiMotherFirstName");
        line.appendCsvFn("IndiMotherAge");
        line.appendCsvFn("IndiMotherDead");
        line.appendCsvFn("IndiMotherOccupation");
        line.appendCsvFn("IndiMotherResidence"); 
        line.appendCsvFn("IndiMotherAddress"); 
        line.appendCsvFn("IndiMotherComment");

        line.appendCsvFn("WifeLastName");
        line.appendCsvFn("WifeFirstName");
        line.appendCsvFn("WifeSex");
        line.appendCsvFn("WifeBirthPlace");
        line.appendCsvFn("WifeBirthAddress");
        line.appendCsvFn("WifeBirthDate");
        line.appendCsvFn("WifeAge");
        line.appendCsvFn("WifeOccupation");
        line.appendCsvFn("WifeResidence"); 
        line.appendCsvFn("WifeAddress"); 
        line.appendCsvFn("WifeComment");

        line.appendCsvFn("WifeMarriedLastName");
        line.appendCsvFn("WifeMarriedFirstName");
        line.appendCsvFn("WifeMarriedDead");
        line.appendCsvFn("WifeMarriedOccupation");
        line.appendCsvFn("WifeMarriedResidence"); 
        line.appendCsvFn("WifeMarriedAddress"); 
        line.appendCsvFn("WifeMarriedComment");

        line.appendCsvFn("WifeFatherLastName");
        line.appendCsvFn("WifeFatherFirstName");
        line.appendCsvFn("WifeFatherAge");
        line.appendCsvFn("WifeFatherDead");
        line.appendCsvFn("WifeFatherOccupation");
        line.appendCsvFn("WifeFatherResidence"); 
        line.appendCsvFn("WifeFatherAddress"); 
        line.appendCsvFn("WifeFatherComment");
        
        line.appendCsvFn("WifeMotherLastName");
        line.appendCsvFn("WifeMotherFirstName");
        line.appendCsvFn("WifeMotherAge");
        line.appendCsvFn("WifeMotherDead");
        line.appendCsvFn("WifeMotherOccupation");
        line.appendCsvFn("WifeMotherResidence"); 
        line.appendCsvFn("WifeMotherAddress"); 
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

        line.appendCsv("GeneralComment");
        line.appendCsv("\n");
        return line;
    }

   
}
