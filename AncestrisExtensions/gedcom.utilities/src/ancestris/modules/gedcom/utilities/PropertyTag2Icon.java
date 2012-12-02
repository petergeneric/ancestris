package ancestris.modules.gedcom.utilities;

import java.awt.Image;
import java.util.HashMap;
import javax.swing.Icon;
import org.openide.util.ImageUtilities;

/**
 *
 * @author lemovice
 */
public class PropertyTag2Icon {

    private static final HashMap<String, String> property2Icon = new HashMap<String, String>() {

        {
            put("ABBR", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("ADDR", "ancestris/modules/gedcom/utilities/resources/Adress.png");
            put("ADOP", "ancestris/modules/gedcom/utilities/resources/Adoption.png");
            put("ADR1", "ancestris/modules/gedcom/utilities/resources/Adress.png");
            put("ADR2", "ancestris/modules/gedcom/utilities/resources/Adress.png");
            put("AFN", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("AGE", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("AGNC", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("ALIA", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("ANCE", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("ANCI", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("ANUL", "ancestris/modules/gedcom/utilities/resources/Annulment.png");
            put("ASSO", "ancestris/modules/gedcom/utilities/resources/Association.png");
            put("AUTH", "ancestris/modules/gedcom/utilities/resources/Author");
            put("BAPL", "ancestris/modules/gedcom/utilities/resources/Baptism.png");
            put("BAPM", "ancestris/modules/gedcom/utilities/resources/Baptism.png");
            put("BARM", "ancestris/modules/gedcom/utilities/resources/BarMitzvah.png");
            put("BASM", "ancestris/modules/gedcom/utilities/resources/BasMitzvah.png");
            put("BIRT", "ancestris/modules/gedcom/utilities/resources/Birth.png");
            put("BLES", "ancestris/modules/gedcom/utilities/resources/Blessing.png");
            put("BLOB", "ancestris/modules/gedcom/utilities/resources/Blob.png");
            put("BURI", "ancestris/modules/gedcom/utilities/resources/Burial.png");
            put("CALN", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("CAST", "ancestris/modules/gedcom/utilities/resources/Caste.png");
            put("CAUS", "ancestris/modules/gedcom/utilities/resources/Cause.png");
            put("CENS", "ancestris/modules/gedcom/utilities/resources/Census.png");
            put("CHAN", "ancestris/modules/gedcom/utilities/resources/Date.png");
            put("CHAR", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("CHIL", "ancestris/modules/gedcom/utilities/resources/Child.png");
            put("CHR", "ancestris/modules/gedcom/utilities/resources/Christening.png");
            put("CHRA", "ancestris/modules/gedcom/utilities/resources/AdultChristening.png");
            put("CITY", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("CONC", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("CONF", "ancestris/modules/gedcom/utilities/resources/Confirmation.png");
            put("CONL", "ancestris/modules/gedcom/utilities/resources/Confirmation.png");
            put("CONT", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("COPR", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("CORP", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("CREM", "ancestris/modules/gedcom/utilities/resources/Cremation.png");
            put("CTRY", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("DATA", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("DATE", "ancestris/modules/gedcom/utilities/resources/Date.png");
            put("DEAT", "ancestris/modules/gedcom/utilities/resources/Death.png");
            put("DESC", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("DESI", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("DEST", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("DIV", "ancestris/modules/gedcom/utilities/resources/Divorce.png");
            put("DIVF", "ancestris/modules/gedcom/utilities/resources/Divorce.png");
            put("DSCR", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("EDUC", "ancestris/modules/gedcom/utilities/resources/Education.png");
            put("EMIG", "ancestris/modules/gedcom/utilities/resources/Emigration.png");
            put("ENDL", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("ENGA", "ancestris/modules/gedcom/utilities/resources/Engagement.png");
            put("EVEN", "ancestris/modules/gedcom/utilities/resources/Event.png");
            put("FAM", "ancestris/modules/gedcom/utilities/resources/Family.png");
            put("FAMC", "ancestris/modules/gedcom/utilities/resources/Family.png");
            put("FAMF", "ancestris/modules/gedcom/utilities/resources/Family.png");
            put("FAMS", "ancestris/modules/gedcom/utilities/resources/Family.png");
            put("FCOM", "ancestris/modules/gedcom/utilities/resources/FirstCommunion.png");
            put("FILE", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("FORM", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("HUSB", "ancestris/modules/gedcom/utilities/resources/IndividualMale.png");
            put("IMM", "ancestris/modules/gedcom/utilities/resources/Immigration.png");
            put("GEDC", "ancestris/modules/gedcom/utilities/resources/Gedcom.png");
            put("GIVN", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("GRAD", "ancestris/modules/gedcom/utilities/resources/Graduation.png");
            put("HEAD", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("IDNO", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("INDI", "ancestris/modules/gedcom/utilities/resources/Individual.png");
            put("LANG", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("LEGA", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("MARB", "ancestris/modules/gedcom/utilities/resources/MarriageBann.png");
            put("MARC", "ancestris/modules/gedcom/utilities/resources/MarriageContract.png");
            put("MARL", "ancestris/modules/gedcom/utilities/resources/MarriageLicense.png");
            put("MARR", "ancestris/modules/gedcom/utilities/resources/Marriage.png");
            put("MARS", "ancestris/modules/gedcom/utilities/resources/MarriageSettlement.png");
            put("MEDI", "ancestris/modules/gedcom/utilities/resources/Media.png");
            put("NAME", "ancestris/modules/gedcom/utilities/resources/Name.png");
            put("NATI", "ancestris/modules/gedcom/utilities/resources/Nationality.png");
            put("NATU", "ancestris/modules/gedcom/utilities/resources/Naturalisation.png");
            put("NCHI", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("NICK", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("NMR", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("NOTE", "ancestris/modules/gedcom/utilities/resources/Note.png");
            put("NPFX", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("NSFX", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("OBJE", "ancestris/modules/gedcom/utilities/resources/Media.png");
            put("OCCU", "ancestris/modules/gedcom/utilities/resources/Occupation.png");
            put("ORDI", "ancestris/modules/gedcom/utilities/resources/Ordination.png");
            put("ORDN", "ancestris/modules/gedcom/utilities/resources/Ordination.png");
            put("PAGE", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("PEDI", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("PLAC", "ancestris/modules/gedcom/utilities/resources/Place.png");
            put("POST", "ancestris/modules/gedcom/utilities/resources/Postal Code");
            put("PROB", "ancestris/modules/gedcom/utilities/resources/Probate.png");
            put("PROP", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("PUBL", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("QUAY", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("REFN", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("RELA", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("RELI", "ancestris/modules/gedcom/utilities/resources/Religion.png");
            put("REPO", "ancestris/modules/gedcom/utilities/resources/Repository.png");
            put("RESI", "ancestris/modules/gedcom/utilities/resources/Residence.png");
            put("RESN", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("RETI", "ancestris/modules/gedcom/utilities/resources/Retirement.png");
            put("RFN", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("RIN", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("ROLE", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("SEX", "ancestris/modules/gedcom/utilities/resources/Sex.png");
            put("SLGC", "ancestris/modules/gedcom/utilities/resources/Sealing child");
            put("SLGS", "ancestris/modules/gedcom/utilities/resources/Sealing spouse");
            put("SOUR", "ancestris/modules/gedcom/utilities/resources/Source.png");
            put("SPFX", "ancestris/modules/gedcom/utilities/resources/Surname prefix");
            put("SSN", "ancestris/modules/gedcom/utilities/resources/SSN.png");
            put("SUBM", "ancestris/modules/gedcom/utilities/resources/Submitter.png");
            put("SUBN", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("SURN", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("STAE", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("STAT", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("TIME", "ancestris/modules/gedcom/utilities/resources/Time.png");
            put("TRLR", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("PHON", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("TEMP", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("TEXT", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("TITL", "ancestris/modules/gedcom/utilities/resources/Title.png");
            put("TYPE", "ancestris/modules/gedcom/utilities/resources/Type.png");
            put("VERS", "ancestris/modules/gedcom/utilities/resources/Version");
            put("WIFE", "ancestris/modules/gedcom/utilities/resources/IndividualFemale.png");
            put("WILL", "ancestris/modules/gedcom/utilities/resources/Will.png");
            put("XREF", "ancestris/modules/gedcom/utilities/resources/Property.png");
            put("Unknown", "ancestris/modules/gedcom/utilities/resources/Property.png");
        }
    };

    static public String getImageFileName(String entityTag) {
        if (property2Icon.get(entityTag) != null) {
            return property2Icon.get(entityTag);
        } else {
            return property2Icon.get("Unknown");
        }
    }

    static public Image getImage(String entityTag) {
        if (property2Icon.get(entityTag) != null) {
            return ImageUtilities.loadImage(property2Icon.get(entityTag));
        } else {
            return ImageUtilities.loadImage(property2Icon.get("Unknown"));
        }
    }

    static public Icon getIcon(String entityTag) {
        return ImageUtilities.image2Icon(getImage(entityTag));
    }
}
