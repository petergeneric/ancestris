package ancestris.modules.gedcom.mergeentity.node;

import java.awt.Image;
import java.util.HashMap;
import org.openide.util.ImageUtilities;

/**
 *
 * @author lemovice
 */
public class PropertyTag2Icon {

    private static final HashMap<String, String> property2Icon = new HashMap<String, String>() {

        {
            put("ABBR", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("ADDR", "ancestris/modules/gedcom/mergeentity/resources/Adress.png");
            put("ADOP", "ancestris/modules/gedcom/mergeentity/resources/Adoption.png");
            put("ADR1", "ancestris/modules/gedcom/mergeentity/resources/Adress.png");
            put("ADR2", "ancestris/modules/gedcom/mergeentity/resources/Adress.png");
            put("AFN", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("AGE", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("AGNC", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("ALIA", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("ANCE", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("ANCI", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("ANUL", "ancestris/modules/gedcom/mergeentity/resources/Annulment.png");
            put("ASSO", "ancestris/modules/gedcom/mergeentity/resources/Association.png");
            put("AUTH", "ancestris/modules/gedcom/mergeentity/resources/Author");
            put("BAPL", "ancestris/modules/gedcom/mergeentity/resources/Baptism.png");
            put("BAPM", "ancestris/modules/gedcom/mergeentity/resources/Baptism.png");
            put("BARM", "ancestris/modules/gedcom/mergeentity/resources/BarMitzvah.png");
            put("BASM", "ancestris/modules/gedcom/mergeentity/resources/BasMitzvah.png");
            put("BIRT", "ancestris/modules/gedcom/mergeentity/resources/Birth.png");
            put("BLES", "ancestris/modules/gedcom/mergeentity/resources/Blessing.png");
            put("BLOB", "ancestris/modules/gedcom/mergeentity/resources/Blob.png");
            put("BURI", "ancestris/modules/gedcom/mergeentity/resources/Burial.png");
            put("CALN", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("CAST", "ancestris/modules/gedcom/mergeentity/resources/Caste.png");
            put("CAUS", "ancestris/modules/gedcom/mergeentity/resources/Cause.png");
            put("CENS", "ancestris/modules/gedcom/mergeentity/resources/Census.png");
            put("CHAN", "ancestris/modules/gedcom/mergeentity/resources/Date.png");
            put("CHAR", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("CHIL", "ancestris/modules/gedcom/mergeentity/resources/Child.png");
            put("CHR", "ancestris/modules/gedcom/mergeentity/resources/Christening.png");
            put("CHRA", "ancestris/modules/gedcom/mergeentity/resources/AdultChristening.png");
            put("CITY", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("CONC", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("CONF", "ancestris/modules/gedcom/mergeentity/resources/Confirmation.png");
            put("CONL", "ancestris/modules/gedcom/mergeentity/resources/Confirmation.png");
            put("CONT", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("COPR", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("CORP", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("CREM", "ancestris/modules/gedcom/mergeentity/resources/Cremation.png");
            put("CTRY", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("DATA", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("DATE", "ancestris/modules/gedcom/mergeentity/resources/Date.png");
            put("DEAT", "ancestris/modules/gedcom/mergeentity/resources/Death.png");
            put("DESC", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("DESI", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("DEST", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("DIV", "ancestris/modules/gedcom/mergeentity/resources/Divorce.png");
            put("DIVF", "ancestris/modules/gedcom/mergeentity/resources/Divorce.png");
            put("DSCR", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("EDUC", "ancestris/modules/gedcom/mergeentity/resources/Education.png");
            put("EMIG", "ancestris/modules/gedcom/mergeentity/resources/Emigration.png");
            put("ENDL", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("ENGA", "ancestris/modules/gedcom/mergeentity/resources/Engagement.png");
            put("EVEN", "ancestris/modules/gedcom/mergeentity/resources/Event.png");
            put("FAM", "ancestris/modules/gedcom/mergeentity/resources/Family.png");
            put("FAMC", "ancestris/modules/gedcom/mergeentity/resources/Family.png");
            put("FAMF", "ancestris/modules/gedcom/mergeentity/resources/Family.png");
            put("FAMS", "ancestris/modules/gedcom/mergeentity/resources/Family.png");
            put("FCOM", "ancestris/modules/gedcom/mergeentity/resources/FirstCommunion.png");
            put("FILE", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("FORM", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("HUSB", "ancestris/modules/gedcom/mergeentity/resources/IndividualMale.png");
            put("IMM", "ancestris/modules/gedcom/mergeentity/resources/Immigration.png");
            put("GEDC", "ancestris/modules/gedcom/mergeentity/resources/Gedcom.png");
            put("GIVN", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("GRAD", "ancestris/modules/gedcom/mergeentity/resources/Graduation.png");
            put("HEAD", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("IDNO", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("INDI", "ancestris/modules/gedcom/mergeentity/resources/Individual.png");
            put("LANG", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("LEGA", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("MARB", "ancestris/modules/gedcom/mergeentity/resources/MarriageBann.png");
            put("MARC", "ancestris/modules/gedcom/mergeentity/resources/MarriageContract.png");
            put("MARL", "ancestris/modules/gedcom/mergeentity/resources/MarriageLicense.png");
            put("MARR", "ancestris/modules/gedcom/mergeentity/resources/Marriage.png");
            put("MARS", "ancestris/modules/gedcom/mergeentity/resources/MarriageSettlement.png");
            put("MEDI", "ancestris/modules/gedcom/mergeentity/resources/Media.png");
            put("NAME", "ancestris/modules/gedcom/mergeentity/resources/Name.png");
            put("NATI", "ancestris/modules/gedcom/mergeentity/resources/Nationality.png");
            put("NATU", "ancestris/modules/gedcom/mergeentity/resources/Naturalisation.png");
            put("NCHI", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("NICK", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("NMR", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("NOTE", "ancestris/modules/gedcom/mergeentity/resources/Note.png");
            put("NPFX", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("NSFX", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("OBJE", "ancestris/modules/gedcom/mergeentity/resources/Media.png");
            put("OCCU", "ancestris/modules/gedcom/mergeentity/resources/Occupation.png");
            put("ORDI", "ancestris/modules/gedcom/mergeentity/resources/Ordination.png");
            put("ORDN", "ancestris/modules/gedcom/mergeentity/resources/Ordination.png");
            put("PAGE", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("PEDI", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("PLAC", "ancestris/modules/gedcom/mergeentity/resources/Place.png");
            put("POST", "ancestris/modules/gedcom/mergeentity/resources/Postal Code");
            put("PROB", "ancestris/modules/gedcom/mergeentity/resources/Probate.png");
            put("PROP", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("PUBL", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("QUAY", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("REFN", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("RELA", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("RELI", "ancestris/modules/gedcom/mergeentity/resources/Religion.png");
            put("REPO", "ancestris/modules/gedcom/mergeentity/resources/Repository.png");
            put("RESI", "ancestris/modules/gedcom/mergeentity/resources/Residence.png");
            put("RESN", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("RETI", "ancestris/modules/gedcom/mergeentity/resources/Retirement.png");
            put("RFN", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("RIN", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("ROLE", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("SEX", "ancestris/modules/gedcom/mergeentity/resources/Sex.png");
            put("SLGC", "ancestris/modules/gedcom/mergeentity/resources/Sealing child");
            put("SLGS", "ancestris/modules/gedcom/mergeentity/resources/Sealing spouse");
            put("SOUR", "ancestris/modules/gedcom/mergeentity/resources/Source.png");
            put("SPFX", "ancestris/modules/gedcom/mergeentity/resources/Surname prefix");
            put("SSN", "ancestris/modules/gedcom/mergeentity/resources/SSN.png");
            put("SUBM", "ancestris/modules/gedcom/mergeentity/resources/Submitter.png");
            put("SUBN", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("SURN", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("STAE", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("STAT", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("TIME", "ancestris/modules/gedcom/mergeentity/resources/Time.png");
            put("TRLR", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("PHON", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("TEMP", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("TEXT", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("TITL", "ancestris/modules/gedcom/mergeentity/resources/Title.png");
            put("TYPE", "ancestris/modules/gedcom/mergeentity/resources/Type.png");
            put("VERS", "ancestris/modules/gedcom/mergeentity/resources/Version");
            put("WIFE", "ancestris/modules/gedcom/mergeentity/resources/IndividualFemale.png");
            put("WILL", "ancestris/modules/gedcom/mergeentity/resources/Will.png");
            put("XREF", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
            put("Unknown", "ancestris/modules/gedcom/mergeentity/resources/Property.png");
        }
    };

    static String getIconFile(String entityTag) {
        if (property2Icon.get(entityTag) != null) {
            return property2Icon.get(entityTag);
        } else {
            return property2Icon.get("Unknown");
        }
    }
    static Image getIcon(String entityTag) {
        if (property2Icon.get(entityTag) != null) {
            return ImageUtilities.loadImage(property2Icon.get(entityTag));
        } else {
            return ImageUtilities.loadImage(property2Icon.get("Unknown"));
        }
    }}
