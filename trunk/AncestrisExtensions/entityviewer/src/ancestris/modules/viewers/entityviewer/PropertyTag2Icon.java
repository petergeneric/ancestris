package ancestris.modules.viewers.entityviewer;

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
            put("ABBR", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("ADDR", "ancestris/modules/viewers/entityviewer/resources/Adress.png");
            put("ADOP", "ancestris/modules/viewers/entityviewer/resources/Adoption.png");
            put("ADR1", "ancestris/modules/viewers/entityviewer/resources/Adress.png");
            put("ADR2", "ancestris/modules/viewers/entityviewer/resources/Adress.png");
            put("AFN", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("AGE", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("AGNC", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("ALIA", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("ANCE", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("ANCI", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("ANUL", "ancestris/modules/viewers/entityviewer/resources/Annulment.png");
            put("ASSO", "ancestris/modules/viewers/entityviewer/resources/Association.png");
            put("AUTH", "ancestris/modules/viewers/entityviewer/resources/Author");
            put("BAPL", "ancestris/modules/viewers/entityviewer/resources/Baptism.png");
            put("BAPM", "ancestris/modules/viewers/entityviewer/resources/Baptism.png");
            put("BARM", "ancestris/modules/viewers/entityviewer/resources/BarMitzvah.png");
            put("BASM", "ancestris/modules/viewers/entityviewer/resources/BasMitzvah.png");
            put("BIRT", "ancestris/modules/viewers/entityviewer/resources/Birth.png");
            put("BLES", "ancestris/modules/viewers/entityviewer/resources/Blessing.png");
            put("BLOB", "ancestris/modules/viewers/entityviewer/resources/Blob.png");
            put("BURI", "ancestris/modules/viewers/entityviewer/resources/Burial.png");
            put("CALN", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("CAST", "ancestris/modules/viewers/entityviewer/resources/Caste.png");
            put("CAUS", "ancestris/modules/viewers/entityviewer/resources/Cause.png");
            put("CENS", "ancestris/modules/viewers/entityviewer/resources/Census.png");
            put("CHAN", "ancestris/modules/viewers/entityviewer/resources/Date.png");
            put("CHAR", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("CHIL", "ancestris/modules/viewers/entityviewer/resources/Child.png");
            put("CHR", "ancestris/modules/viewers/entityviewer/resources/Christening.png");
            put("CHRA", "ancestris/modules/viewers/entityviewer/resources/AdultChristening.png");
            put("CITY", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("CONC", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("CONF", "ancestris/modules/viewers/entityviewer/resources/Confirmation.png");
            put("CONL", "ancestris/modules/viewers/entityviewer/resources/Confirmation.png");
            put("CONT", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("COPR", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("CORP", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("CREM", "ancestris/modules/viewers/entityviewer/resources/Cremation.png");
            put("CTRY", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("DATA", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("DATE", "ancestris/modules/viewers/entityviewer/resources/Date.png");
            put("DEAT", "ancestris/modules/viewers/entityviewer/resources/Death.png");
            put("DESC", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("DESI", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("DEST", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("DIV", "ancestris/modules/viewers/entityviewer/resources/Divorce.png");
            put("DIVF", "ancestris/modules/viewers/entityviewer/resources/Divorce.png");
            put("DSCR", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("EDUC", "ancestris/modules/viewers/entityviewer/resources/Education.png");
            put("EMIG", "ancestris/modules/viewers/entityviewer/resources/Emigration.png");
            put("ENDL", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("ENGA", "ancestris/modules/viewers/entityviewer/resources/Engagement.png");
            put("EVEN", "ancestris/modules/viewers/entityviewer/resources/Event.png");
            put("FAM", "ancestris/modules/viewers/entityviewer/resources/Family.png");
            put("FAMC", "ancestris/modules/viewers/entityviewer/resources/Family.png");
            put("FAMF", "ancestris/modules/viewers/entityviewer/resources/Family.png");
            put("FAMS", "ancestris/modules/viewers/entityviewer/resources/Family.png");
            put("FCOM", "ancestris/modules/viewers/entityviewer/resources/FirstCommunion.png");
            put("FILE", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("FORM", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("HUSB", "ancestris/modules/viewers/entityviewer/resources/IndividualMale.png");
            put("IMM", "ancestris/modules/viewers/entityviewer/resources/Immigration.png");
            put("GEDC", "ancestris/modules/viewers/entityviewer/resources/Gedcom.png");
            put("GIVN", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("GRAD", "ancestris/modules/viewers/entityviewer/resources/Graduation.png");
            put("HEAD", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("IDNO", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("INDI", "ancestris/modules/viewers/entityviewer/resources/Individual.png");
            put("LANG", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("LEGA", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("MARB", "ancestris/modules/viewers/entityviewer/resources/MarriageBann.png");
            put("MARC", "ancestris/modules/viewers/entityviewer/resources/MarriageContract.png");
            put("MARL", "ancestris/modules/viewers/entityviewer/resources/MarriageLicense.png");
            put("MARR", "ancestris/modules/viewers/entityviewer/resources/Marriage.png");
            put("MARS", "ancestris/modules/viewers/entityviewer/resources/MarriageSettlement.png");
            put("MEDI", "ancestris/modules/viewers/entityviewer/resources/Media.png");
            put("NAME", "ancestris/modules/viewers/entityviewer/resources/Name.png");
            put("NATI", "ancestris/modules/viewers/entityviewer/resources/Nationality.png");
            put("NATU", "ancestris/modules/viewers/entityviewer/resources/Naturalisation.png");
            put("NCHI", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("NICK", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("NMR", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("NOTE", "ancestris/modules/viewers/entityviewer/resources/Note.png");
            put("NPFX", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("NSFX", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("OBJE", "ancestris/modules/viewers/entityviewer/resources/Media.png");
            put("OCCU", "ancestris/modules/viewers/entityviewer/resources/Occupation.png");
            put("ORDI", "ancestris/modules/viewers/entityviewer/resources/Ordination.png");
            put("ORDN", "ancestris/modules/viewers/entityviewer/resources/Ordination.png");
            put("PAGE", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("PEDI", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("PLAC", "ancestris/modules/viewers/entityviewer/resources/Place.png");
            put("POST", "ancestris/modules/viewers/entityviewer/resources/Postal Code");
            put("PROB", "ancestris/modules/viewers/entityviewer/resources/Probate.png");
            put("PROP", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("PUBL", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("QUAY", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("REFN", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("RELA", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("RELI", "ancestris/modules/viewers/entityviewer/resources/Religion.png");
            put("REPO", "ancestris/modules/viewers/entityviewer/resources/Repository.png");
            put("RESI", "ancestris/modules/viewers/entityviewer/resources/Residence.png");
            put("RESN", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("RETI", "ancestris/modules/viewers/entityviewer/resources/Retirement.png");
            put("RFN", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("RIN", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("ROLE", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("SEX", "ancestris/modules/viewers/entityviewer/resources/Sex.png");
            put("SLGC", "ancestris/modules/viewers/entityviewer/resources/Sealing child");
            put("SLGS", "ancestris/modules/viewers/entityviewer/resources/Sealing spouse");
            put("SOUR", "ancestris/modules/viewers/entityviewer/resources/Source.png");
            put("SPFX", "ancestris/modules/viewers/entityviewer/resources/Surname prefix");
            put("SSN", "ancestris/modules/viewers/entityviewer/resources/SSN.png");
            put("SUBM", "ancestris/modules/viewers/entityviewer/resources/Submitter.png");
            put("SUBN", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("SURN", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("STAE", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("STAT", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("TIME", "ancestris/modules/viewers/entityviewer/resources/Time.png");
            put("TRLR", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("PHON", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("TEMP", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("TEXT", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("TITL", "ancestris/modules/viewers/entityviewer/resources/Title.png");
            put("TYPE", "ancestris/modules/viewers/entityviewer/resources/Type.png");
            put("VERS", "ancestris/modules/viewers/entityviewer/resources/Version");
            put("WIFE", "ancestris/modules/viewers/entityviewer/resources/IndividualFemale.png");
            put("WILL", "ancestris/modules/viewers/entityviewer/resources/Will.png");
            put("XREF", "ancestris/modules/viewers/entityviewer/resources/Property.png");
            put("Unknown", "ancestris/modules/viewers/entityviewer/resources/Property.png");
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
