package ancestris.modules.releve.file;

import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySource;
import genj.gedcom.TagPath;
import java.util.Iterator;

/**
 *
 * @author Michel
 */
public class ReleveFileGedcom {

    public static boolean isValidFile(String strLine) {

        return true;
    }

    /**
     *
     * @param fileName
     * TODO gérer la dat iincomplete
     */
    public static FileBuffer loadFile(Gedcom gedcom) throws Exception {

        FileBuffer buffer = new FileBuffer();
        try {

            //create BufferedReader to read file
            int lineNumber = 0;

            //read comma separated file line by line
            for (Iterator it = gedcom.getEntities().iterator(); it.hasNext();) {
                Entity entity = (Entity) it.next();

                if (entity instanceof Indi) {
                    Indi indi = (Indi) entity;

                    Property birthProperty = indi.getProperty("BIRT");
                    if (birthProperty != null) {
                        RecordBirth record = new RecordBirth();

                        record.setEventPlace(
                                birthProperty.getProperty("PLAC") != null ? birthProperty.getProperty("PLAC").getValue() : "",
                                "", // cityCode
                                "", // countyName
                                "", // stateName
                                "" ); // countryName
                        record.setParish("");
                        //record.setEventDate( birthProperty.getProperty("DATE")!=null ? birthProperty.getProperty("DATE").toString() : "" );
                        record.getEventDateField().setValue(birthProperty.getProperty("DATE") != null ? birthProperty.getProperty("DATE").getValue() : "");
                        record.setCote("");
                        record.setFreeComment("");

                        record.setIndi(
                                indi.getFirstName(),
                                indi.getLastName(),
                                indi.getProperty("SEX") != null ? indi.getProperty("SEX").getValue() : "",
                                "", // pas d'age a la naissance
                                "", // pas de date de naissance a la naissance
                                "", // pas de lieu a la naissance
                                "", // pas de profession a la naissance
                                birthProperty.getPropertyValue("NOTE"));  // note

                        if (indi.getBiologicalFather() != null) {
                            record.setIndiFather(
                                    indi.getBiologicalFather().getFirstName(),
                                    indi.getBiologicalFather().getLastName(),
                                    indi.getBiologicalFather().getProperty("OCCU") != null ? indi.getBiologicalFather().getPropertyDisplayValue("OCCU") : "",
                                    indi.getBiologicalFather().getProperty("NOTE") != null ? indi.getBiologicalFather().getPropertyDisplayValue("NOTE") : "",
                                    indi.getBiologicalFather().getDeathDate(false) != null ? "true" : "false");
                        }

                        if (indi.getBiologicalMother() != null) {
                            record.setIndiMother(
                                    indi.getBiologicalMother().getFirstName(),
                                    indi.getBiologicalMother().getLastName(),
                                    indi.getBiologicalMother().getProperty("OCCU") != null ? indi.getBiologicalMother().getPropertyDisplayValue("OCCU") : "",
                                    indi.getBiologicalMother().getProperty("NOTE") != null ? indi.getBiologicalMother().getPropertyDisplayValue("NOTE") : "",
                                    indi.getBiologicalMother().getDeathDate(false) != null ? "true" : "false");
                        }

//                        record.setWitness1(
//                                fields[Field.witness1FirstName.ordinal()],
//                                fields[Field.witness1LastName.ordinal()],
//                                fields[Field.witness1Occupation.ordinal()],
//                                fields[Field.witness1Comment.ordinal()]);
//                        record.setWitness2(
//                                fields[Field.witness2FirstName.ordinal()],
//                                fields[Field.witness2LastName.ordinal()],
//                                fields[Field.witness2Occupation.ordinal()],
//                                fields[Field.witness2Comment.ordinal()]);
//                        record.setWitness3(
//                                fields[Field.witness3FirstName.ordinal()],
//                                fields[Field.witness3LastName.ordinal()],
//                                fields[Field.witness3Occupation.ordinal()],
//                                fields[Field.witness3Comment.ordinal()]);
//                        record.setWitness4(
//                                fields[Field.witness4FirstName.ordinal()],
//                                fields[Field.witness4LastName.ordinal()],
//                                fields[Field.witness4Occupation.ordinal()],
//                                fields[Field.witness4Comment.ordinal()]);

                        record.setGeneralComment(indi.getProperty("NOTE") != null ? indi.getProperty("NOTE").toString() : "");
                        record.recordNo = lineNumber++;
                        buffer.loadRecord(record);
                    }

                    Property deathProperty = indi.getProperty("DEAT");
                    if (deathProperty != null) {
                        RecordDeath record = new RecordDeath();

                        record.setEventPlace(
                                deathProperty.getProperty("PLAC") != null ? deathProperty.getProperty("PLAC").getValue() : "",
                                "", // cityCode
                                "", // countyName
                                "", // stateName
                                "" ); // countryName
                        record.setParish("");
                        //record.setEventDate( deathProperty.getProperty("DATE")!=null ? deathProperty.getProperty("DATE").toString() : "" );
                        record.getEventDateField().setValue(deathProperty.getProperty("DATE") != null ? deathProperty.getProperty("DATE").getValue() : "");
                        record.setCote("");
                        record.setFreeComment("");

                        record.setIndi(
                                indi.getFirstName(),
                                indi.getLastName(),
                                indi.getProperty("SEX") != null ? indi.getProperty("SEX").getValue() : "",
                                "", // pas d'age a la naissance
                                "", // pas de date de naossance a la naissance
                                "", // pas de lieu a la naissance
                                "", // pas de profession a la naissance
                                deathProperty.getPropertyValue("NOTE"));  // note

                        if (indi.getBiologicalFather() != null) {
                            record.setIndiFather(
                                    indi.getBiologicalFather().getFirstName(),
                                    indi.getBiologicalFather().getLastName(),
                                    indi.getBiologicalFather().getPropertyValue("OCCU"),
                                    indi.getBiologicalFather().getPropertyValue("NOTE"),
                                    indi.getBiologicalFather().getDeathDate(false) != null ? "true" : "false");
                        }

                        if (indi.getBiologicalMother() != null) {
                            record.setIndiMother(
                                    indi.getBiologicalMother().getFirstName(),
                                    indi.getBiologicalMother().getLastName(),
                                    indi.getBiologicalMother().getPropertyValue("OCCU"),
                                    indi.getBiologicalMother().getPropertyValue("NOTE"),
                                    indi.getBiologicalMother().getDeathDate(false) != null ? "true" : "false");
                        }
                        record.recordNo = lineNumber++;
                        buffer.loadRecord(record);
                    }
                } else if (entity instanceof Fam) {
                    Fam fam = (Fam) entity;

                    Indi husband = fam.getHusband();
                    Indi wife = fam.getWife();
                    PropertyDate marriageDate = (PropertyDate) fam.getProperty(new TagPath("FAM:MARR:DATE"));
                    PropertyPlace marriagePlace = (PropertyPlace) fam.getProperty(new TagPath("FAM:MARR:PLAC"));
                    PropertySource marriageSource = (PropertySource) fam.getProperty(new TagPath("FAM:MARR:SOUR"));

                    RecordMarriage record = new RecordMarriage();

                    record.setEventPlace(
                            marriagePlace != null ? marriagePlace.getCity() : "",
                            "", // cityCode
                            "", // countyName
                            "", // stateName
                            ""); // countryName
                    record.setParish("");
                    record.getEventDateField().setValue(marriageDate != null ? marriageDate.getValue() : "");
                    record.setCote("");
                    record.setFreeComment("");

                    if (husband != null) {
                        record.setIndi(
                                husband.getFirstName(),
                                husband.getLastName(),
                                husband.getPropertyValue("SEX"),
                                "", // age
                                "", //birth
                                "", //place
                                "", //occu
                                ""); // comment

                        Indi husbandFather = husband.getBiologicalFather();

                        if (husbandFather != null) {
                            record.setIndiFather(
                                    husbandFather.getFirstName(),
                                    husbandFather.getLastName(),
                                    husbandFather.getPropertyValue("OCCU"),
                                    husbandFather.getPropertyValue("NOTE"),
                                    "false");
                        }

                        Indi husbandMother = husband.getBiologicalMother();

                        if (husbandMother != null) {
                            record.setIndiFather(
                                    husbandMother.getFirstName(),
                                    husbandMother.getLastName(),
                                    husbandMother.getPropertyValue("OCCU"),
                                    husbandMother.getPropertyValue("NOTE"),
                                    "false");
                        }
                    }

                    if (wife != null) {
                        record.setWife(
                                wife.getFirstName(),
                                wife.getLastName(),
                                wife.getPropertyValue("SEX"),
                                "", // age
                                "", //birth
                                "", //place
                                "", //occu
                                ""); // comment

                        Indi wifeFather = wife.getBiologicalFather();

                        if (wifeFather != null) {
                            record.setWifeFather(
                                    wifeFather.getFirstName(),
                                    wifeFather.getLastName(),
                                    wifeFather.getPropertyValue("OCCU"),
                                    wifeFather.getPropertyValue("NOTE"),
                                    "false");
                        }

                        Indi wifeMother = wife.getBiologicalMother();

                        if (wifeMother != null) {
                            record.setWifeMother(
                                    wifeMother.getFirstName(),
                                    wifeMother.getLastName(),
                                    wifeMother.getPropertyValue("OCCU"),
                                    wifeMother.getPropertyValue("NOTE"),
                                    "false");
                        }
                    }
                    record.recordNo = lineNumber++;
                    buffer.loadRecord(record);
                }

            } // for

        } catch (Exception e) {
            System.out.println("Exception while reading file: " + e);
            throw e;
        }

        return buffer;
    }
}
