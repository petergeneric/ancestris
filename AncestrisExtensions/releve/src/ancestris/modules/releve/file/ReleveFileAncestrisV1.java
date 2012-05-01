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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import org.openide.util.Exceptions;

/**
 *
 * @author Michel
 */
public class ReleveFileAncestrisV1 {

    final static private String fileSignature = "ANCESTRISV1";
    final static private String fieldSeparator = ";";

    public static boolean isValidFile( String strLine) {
        try {
            splitLine(strLine);
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
    private static String[] splitLine( String strLine) throws Exception {
            // exemple de code pour traiter le cas où les champs contiennent ";"
            //        Pattern p = Pattern.compile("\"([^\"]|\"\")*\"(;|$)|[^;]*(;|$)");
            //        Matcher m = p.matcher(test);
            //        while ( m.find() )
            //            System.out.println(m.group());

        String[] fields = strLine.split(fieldSeparator,100);
        if (fields.length == 78 ) {
            if ( fields[0].equals(fileSignature)) {
                return fields;
            } else {
                throw new Exception(String.format("signature must be %s", fileSignature));
            }
        } else {
            throw new Exception(String.format("Line contains %s fields. Must be %d fields", fields.length , 77));
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
        InputStreamReader inputStreamReader = null;
        FileBuffer fileBuffer = new FileBuffer();
        try {
            inputStreamReader = new FileReader(inputFile);
            fileBuffer = loadFile(inputStreamReader);
            inputStreamReader.close();
            return fileBuffer;
        } catch (Exception ex) {            
            Exceptions.printStackTrace(ex);
            fileBuffer.append(ex.toString()).append("\n");
            return fileBuffer;
        } 
    }

    public static FileBuffer loadFile( InputStreamReader inputStreamReader ) {
        FileBuffer fileBuffer = new FileBuffer();
        //create BufferedReader to read file
        BufferedReader br = new BufferedReader(inputStreamReader);
        try {

            String strLine = "";
            int lineNumber = 0;

            //read comma separated file line by line
            while ((strLine = br.readLine()) != null) {
                try {
                    lineNumber++;
                    String[] fields = splitLine(strLine);
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
                                fields[Field.wifeFirstName.ordinal()],
                                fields[Field.wifeLastName.ordinal()],
                                //fields[Field.indiSex.ordinal()].equals("M") ? "F" : "M",
                                fields[Field.wifeOccupation.ordinal()],
                                fields[Field.wifeComment.ordinal()],
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
                    fileBuffer.append("Line ").append(lineNumber).append(" ").append(e.toString()).append("\n");
                    fileBuffer.append("   ").append(strLine).append("\n");
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
    public static void saveFile(DataManager dataManager, ModelAbstract recordModel, File fileName, boolean append) {
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
                        line.appendSep(fileSignature);
                        line.appendSep(dataManager.getCityName());
                        line.appendSep(dataManager.getCityCode());
                        line.appendSep(dataManager.getCountyName());
                        line.appendSep(dataManager.getStateName());
                        line.appendSep(dataManager.getCountyName());
                        line.appendSep(record.getParish());
                        line.appendSep("N");
                        line.appendSep(""); //eventTypeTag
                        line.appendSep(""); //eventTypeName
                        line.appendSep(record.getEventDateString());
                        line.appendSep(record.getCote());
                        line.appendSep(record.getFreeComment());
                        line.appendSep(""); // notary

                        line.appendSep(record.getIndiLastName().getValue());
                        line.appendSep(record.getIndiFirstName().getValue());
                        line.appendSep(record.getIndiSex());
                        line.appendSep(""); // place
                        line.appendSep(record.getIndiBirthDate()); // IndiBirthDate
                        line.appendSep(""); // age
                        line.appendSep(""); // occupation
                        line.appendSep(record.getIndiComment());

                        line.appendSep(""); // IndiMarriedLastName
                        line.appendSep(""); // IndiMarriedFirstName
                        line.appendSep(""); // IndiMarriedDead
                        line.appendSep(""); // IndiMarriedOccupation
                        line.appendSep(""); // IndiMarriedComment

                        line.appendSep(record.getIndiFatherLastName().toString());
                        line.appendSep(record.getIndiFatherFirstName().toString());
                        line.appendSep(record.getIndiFatherDead().getValue());
                        line.appendSep(record.getIndiFatherOccupation().toString());
                        line.appendSep(record.getIndiFatherComment());                        
                        line.appendSep(record.getIndiMotherLastName().toString());
                        line.appendSep(record.getIndiMotherFirstName().toString());
                        line.appendSep(record.getIndiMotherDead().getValue());
                        line.appendSep(record.getIndiMotherOccupation().toString());
                        line.appendSep(record.getIndiMotherComment());

                        line.appendSep(""); // WifeLastName
                        line.appendSep(""); // WifeFirstName
                        line.appendSep(""); // WifeSex
                        line.appendSep(""); // WifePlace
                        line.appendSep(""); // WifeBirthDate
                        line.appendSep(""); // WifeAge
                        line.appendSep(""); // WifeOccupation
                        line.appendSep(""); // WifeComment

                        line.appendSep(""); // WifeMarriedLastName
                        line.appendSep(""); // WifeMarriedFirstName
                        line.appendSep(""); // WifeMarriedDead
                        line.appendSep(""); // WifeMarriedOccupation
                        line.appendSep(""); // WifeMarriedComment

                        line.appendSep(""); // WifeFatherLastName
                        line.appendSep(""); // WifeFatherFirstName
                        line.appendSep(""); // WifeFatherDead
                        line.appendSep(""); // WifeFatherOccupation
                        line.appendSep(""); // WifeFatherComment
                        line.appendSep(""); // WifeMotherLastName
                        line.appendSep(""); // WifeMotherFirstName
                        line.appendSep(""); // WifeMotherDead
                        line.appendSep(""); // WifeMotherOccupation
                        line.appendSep(""); // WifeMotherComment

                        line.appendSep(record.getWitness1LastName().toString());
                        line.appendSep(record.getWitness1FirstName().toString());
                        line.appendSep(record.getWitness1Occupation().toString());
                        line.appendSep(record.getWitness1Comment());

                        line.appendSep(record.getWitness2LastName().toString());
                        line.appendSep(record.getWitness2FirstName().toString());
                        line.appendSep(record.getWitness2Occupation().toString());
                        line.appendSep(record.getWitness2Comment());

                        line.appendSep(record.getWitness3LastName().toString());
                        line.appendSep(record.getWitness3FirstName().toString());
                        line.appendSep(record.getWitness3Occupation().toString());
                        line.appendSep(record.getWitness3Comment());

                        line.appendSep(record.getWitness4LastName().toString());
                        line.appendSep(record.getWitness4FirstName().toString());
                        line.appendSep(record.getWitness4Occupation().toString());
                        line.appendSep(record.getWitness4Comment());

                        line.appendSep(record.getGeneralComment());
                        line.appendln(String.valueOf(record.recordNo)); // numero d'enregistrement

                    } if ( record instanceof RecordMarriage ) {

                        line.appendSep(fileSignature);
                        line.appendSep(dataManager.getCityName());
                        line.appendSep(dataManager.getCityCode());
                        line.appendSep(dataManager.getCountyName());
                        line.appendSep(dataManager.getStateName());
                        line.appendSep(dataManager.getCountyName());
                        line.appendSep(record.getParish());
                        line.appendSep("M");
                        line.appendSep(""); //eventTypeTag
                        line.appendSep(""); //eventTypeName
                        line.appendSep(record.getEventDateString());
                        line.appendSep(record.getCote());
                        line.appendSep(record.getFreeComment());
                        line.appendSep(""); // notary

                        line.appendSep(record.getIndiLastName().getValue());
                        line.appendSep(record.getIndiFirstName().getValue());
                        line.appendSep(""); // IndiSex
                        line.appendSep(record.getIndiPlace().toString());
                        line.appendSep(record.getIndiBirthDate());
                        line.appendSep(record.getIndiAge());
                        line.appendSep(record.getIndiOccupation().toString());
                        line.appendSep(record.getIndiComment());
                        
                        line.appendSep(record.getIndiMarriedLastName().toString());
                        line.appendSep(record.getIndiMarriedFirstName().toString());
                        line.appendSep(record.getIndiMarriedDead().getValue());
                        line.appendSep(record.getIndiMarriedOccupation().toString());
                        line.appendSep(record.getIndiMarriedComment());

                        line.appendSep(record.getIndiFatherLastName().toString());
                        line.appendSep(record.getIndiFatherFirstName().toString());
                        line.appendSep(record.getIndiFatherDead().getValue());
                        line.appendSep(record.getIndiFatherOccupation().toString());
                        line.appendSep(record.getIndiFatherComment());
                        
                        line.appendSep(record.getIndiMotherLastName().toString());
                        line.appendSep(record.getIndiMotherFirstName().toString());
                        line.appendSep(record.getIndiMotherDead().getValue());
                        line.appendSep(record.getIndiMotherOccupation().toString());
                        line.appendSep(record.getIndiMotherComment());
                        
                        line.appendSep(record.getWifeLastName().toString());
                        line.appendSep(record.getWifeFirstName().toString());
                        line.appendSep(""); //WifeSex
                        line.appendSep(record.getWifePlace().toString());
                        line.appendSep(record.getWifeBirthDate());
                        line.appendSep(record.getWifeAge());
                        line.appendSep(record.getWifeOccupation().toString());
                        line.appendSep(record.getWifeComment());
                        
                        line.appendSep(record.getWifeMarriedLastName().toString());
                        line.appendSep(record.getWifeMarriedFirstName().toString());
                        line.appendSep(record.getWifeMarriedDead().getValue());
                        line.appendSep(record.getWifeMarriedOccupation().toString());
                        line.appendSep(record.getWifeMarriedComment());

                        line.appendSep(record.getWifeFatherLastName().toString());
                        line.appendSep(record.getWifeFatherFirstName().toString());
                        line.appendSep(record.getWifeFatherDead().getValue());
                        line.appendSep(record.getWifeFatherOccupation().toString());
                        line.appendSep(record.getWifeFatherComment());
                        line.appendSep(record.getWifeMotherLastName().toString());
                        line.appendSep(record.getWifeMotherFirstName().toString());
                        line.appendSep(record.getWifeMotherDead().getValue());
                        line.appendSep(record.getWifeMotherOccupation().toString());
                        line.appendSep(record.getWifeMotherComment());

                        line.appendSep(record.getWitness1LastName().toString());
                        line.appendSep(record.getWitness1FirstName().toString());
                        line.appendSep(record.getWitness1Occupation().toString());
                        line.appendSep(record.getWitness1Comment());

                        line.appendSep(record.getWitness2LastName().toString());
                        line.appendSep(record.getWitness2FirstName().toString());
                        line.appendSep(record.getWitness2Occupation().toString());
                        line.appendSep(record.getWitness2Comment());

                        line.appendSep(record.getWitness3LastName().toString());
                        line.appendSep(record.getWitness3FirstName().toString());
                        line.appendSep(record.getWitness3Occupation().toString());
                        line.appendSep(record.getWitness3Comment());

                        line.appendSep(record.getWitness4LastName().toString());
                        line.appendSep(record.getWitness4FirstName().toString());
                        line.appendSep(record.getWitness4Occupation().toString());
                        line.appendSep(record.getWitness4Comment());

                        line.appendSep(record.getGeneralComment());
                        line.appendln(String.valueOf(record.recordNo)); // numero d'enregistrement

                    } else if ( record instanceof RecordDeath ) {

                        line.appendSep(fileSignature);
                        line.appendSep(dataManager.getCityName());
                        line.appendSep(dataManager.getCityCode());
                        line.appendSep(dataManager.getCountyName());
                        line.appendSep(dataManager.getStateName());
                        line.appendSep(dataManager.getCountyName());
                        line.appendSep(record.getParish());
                        line.appendSep("D");
                        line.appendSep(""); //eventTypeTag
                        line.appendSep(""); //eventTypeName
                        line.appendSep(record.getEventDateString());
                        line.appendSep(record.getCote());
                        line.appendSep(record.getFreeComment());
                        line.appendSep(""); // notary

                        line.appendSep(record.getIndiLastName().toString());
                        line.appendSep(record.getIndiFirstName().toString());
                        line.appendSep(record.getIndiSex());
                        line.appendSep(record.getIndiPlace().toString());
                        line.appendSep(record.getIndiBirthDate());
                        line.appendSep(record.getIndiAge());
                        line.appendSep(record.getIndiOccupation().toString());
                        line.appendSep(record.getIndiComment());

                        line.appendSep(record.getIndiMarriedLastName().toString());
                        line.appendSep(record.getIndiMarriedFirstName().toString());
                        line.appendSep(record.getIndiMarriedDead().getValue());
                        line.appendSep(record.getIndiMarriedOccupation().toString());
                        line.appendSep(record.getIndiMarriedComment());

                        line.appendSep(record.getIndiFatherLastName().toString());
                        line.appendSep(record.getIndiFatherFirstName().toString());
                        line.appendSep(record.getIndiFatherDead().getValue());
                        line.appendSep(record.getIndiFatherOccupation().toString());
                        line.appendSep(record.getIndiFatherComment());
                        line.appendSep(record.getIndiMotherLastName().toString());
                        line.appendSep(record.getIndiMotherFirstName().toString());
                        line.appendSep(record.getIndiMotherDead().getValue());
                        line.appendSep(record.getIndiMotherOccupation().toString());
                        line.appendSep(record.getIndiMotherComment());

                        line.appendSep(""); // WifeLastName
                        line.appendSep(""); // WifeFirstName
                        line.appendSep(""); // WifeSex
                        line.appendSep(""); // WifePlace
                        line.appendSep(""); // WifeBirthDate
                        line.appendSep(""); // WifeAge
                        line.appendSep(""); // WifeOccupation
                        line.appendSep(""); // WifeComment

                        line.appendSep(""); // WifeMarriedLastName
                        line.appendSep(""); // WifeMarriedFirstName
                        line.appendSep(""); // WifeMarriedDead
                        line.appendSep(""); // WifeMarriedOccupation
                        line.appendSep(""); // WifeMarriedComment

                        line.appendSep(""); // WifeFatherLastName
                        line.appendSep(""); // WifeFatherFirstName
                        line.appendSep(""); // WifeFatherDead
                        line.appendSep(""); // WifeFatherOccupation
                        line.appendSep(""); // WifeFatherComment
                        line.appendSep(""); // WifeMotherLastName
                        line.appendSep(""); // WifeMotherFirstName
                        line.appendSep(""); // WifeMotherDead
                        line.appendSep(""); // WifeMotherOccupation
                        line.appendSep(""); // WifeMotherComment

                        line.appendSep(record.getWitness1LastName().toString());
                        line.appendSep(record.getWitness1FirstName().toString());
                        line.appendSep(record.getWitness1Occupation().toString());
                        line.appendSep(record.getWitness1Comment());

                        line.appendSep(record.getWitness2LastName().toString());
                        line.appendSep(record.getWitness2FirstName().toString());
                        line.appendSep(record.getWitness2Occupation().toString());
                        line.appendSep(record.getWitness2Comment());

                        line.appendSep(record.getWitness3LastName().toString());
                        line.appendSep(record.getWitness3FirstName().toString());
                        line.appendSep(record.getWitness3Occupation().toString());
                        line.appendSep(record.getWitness3Comment());

                        line.appendSep(record.getWitness4LastName().toString());
                        line.appendSep(record.getWitness4FirstName().toString());
                        line.appendSep(record.getWitness4Occupation().toString());
                        line.appendSep(record.getWitness4Comment());

                        line.appendSep(record.getGeneralComment());
                        line.appendln(String.valueOf(record.recordNo)); // numero d'enregistrement

                    } else if ( record instanceof RecordMisc ) {

                        line.appendSep(fileSignature);
                        line.appendSep(dataManager.getCityName());
                        line.appendSep(dataManager.getCityCode());
                        line.appendSep(dataManager.getCountyName());
                        line.appendSep(dataManager.getStateName());
                        line.appendSep(dataManager.getCountyName());
                        line.appendSep(record.getParish());
                        line.appendSep("V");
                        line.appendSep(record.getEventType().getTag());
                        line.appendSep(record.getEventType().getName());
                        line.appendSep(record.getEventDateString());
                        line.appendSep(record.getCote());
                        line.appendSep(record.getFreeComment());
                        line.appendSep(record.getNotary());

                        line.appendSep(record.getIndiLastName().toString());
                        line.appendSep(record.getIndiFirstName().toString());
                        line.appendSep(record.getIndiSex());
                        line.appendSep(record.getIndiPlace().toString());
                        line.appendSep(record.getIndiBirthDate());
                        line.appendSep(record.getIndiAge());
                        line.appendSep(record.getIndiOccupation().toString());
                        line.appendSep(record.getIndiComment());

                        line.appendSep(record.getIndiMarriedLastName().toString());
                        line.appendSep(record.getIndiMarriedFirstName().toString());
                        line.appendSep(record.getIndiMarriedDead().getValue());
                        line.appendSep(record.getIndiMarriedOccupation().toString());
                        line.appendSep(record.getIndiMarriedComment());

                        line.appendSep(record.getIndiFatherLastName().toString());
                        line.appendSep(record.getIndiFatherFirstName().toString());
                        line.appendSep(record.getIndiFatherDead().getValue());
                        line.appendSep(record.getIndiFatherOccupation().toString());
                        line.appendSep(record.getIndiFatherComment());

                        line.appendSep(record.getIndiMotherLastName().toString());
                        line.appendSep(record.getIndiMotherFirstName().toString());
                        line.appendSep(record.getIndiMotherDead().getValue());
                        line.appendSep(record.getIndiMotherOccupation().toString());
                        line.appendSep(record.getIndiMotherComment());

                        line.appendSep(record.getWifeLastName().toString());
                        line.appendSep(record.getWifeFirstName().toString());
                        line.appendSep(record.getWifeSex());
                        line.appendSep(record.getWifePlace().toString());
                        line.appendSep(record.getWifeBirthDate());
                        line.appendSep(record.getWifeAge());
                        line.appendSep(record.getWifeOccupation().toString());
                        line.appendSep(record.getWifeComment());

                        line.appendSep(record.getWifeMarriedLastName().toString());
                        line.appendSep(record.getWifeMarriedFirstName().toString());
                        line.appendSep(record.getWifeMarriedDead().getValue());
                        line.appendSep(record.getWifeMarriedOccupation().toString());
                        line.appendSep(record.getWifeMarriedComment());

                        line.appendSep(record.getWifeFatherLastName().toString());
                        line.appendSep(record.getWifeFatherFirstName().toString());
                        line.appendSep(record.getWifeFatherDead().getValue());
                        line.appendSep(record.getWifeFatherOccupation().toString());
                        line.appendSep(record.getWifeFatherComment());
                        line.appendSep(record.getWifeMotherLastName().toString());
                        line.appendSep(record.getWifeMotherFirstName().toString());
                        line.appendSep(record.getWifeMotherDead().getValue());
                        line.appendSep(record.getWifeMotherOccupation().toString());
                        line.appendSep(record.getWifeMotherComment());

                        line.appendSep(record.getWitness1LastName().toString());
                        line.appendSep(record.getWitness1FirstName().toString());
                        line.appendSep(record.getWitness1Occupation().toString());
                        line.appendSep(record.getWitness1Comment());

                        line.appendSep(record.getWitness2LastName().toString());
                        line.appendSep(record.getWitness2FirstName().toString());
                        line.appendSep(record.getWitness2Occupation().toString());
                        line.appendSep(record.getWitness2Comment());

                        line.appendSep(record.getWitness3LastName().toString());
                        line.appendSep(record.getWitness3FirstName().toString());
                        line.appendSep(record.getWitness3Occupation().toString());
                        line.appendSep(record.getWitness3Comment());

                        line.appendSep(record.getWitness4LastName().toString());
                        line.appendSep(record.getWitness4FirstName().toString());
                        line.appendSep(record.getWitness4Occupation().toString());
                        line.appendSep(record.getWitness4Comment());

                        line.appendSep(record.getGeneralComment());
                        line.appendln(String.valueOf(record.recordNo)); // numero d'enregistrement
                    }
                    line.appendln("\n");
                    writer.write(line.toString());

                } catch (Exception e) {
                    sb.append("Line ").append(" " ).append(e).append("\n");
                    sb.append("   ").append(line).append("\n");
                    e.printStackTrace();
                }
            }
            writer.close();

        } catch (Exception e) {
            sb.append("Error ").append(e).append("\n");
            e.printStackTrace();
        }
    }

   
}
