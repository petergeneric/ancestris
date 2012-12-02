package ancestris.modules.gedcom.utilities;

import java.util.HashMap;
import org.openide.util.NbBundle;

/**
 *
 * @author lemovice
 */
public class PropertyTag2Name {

    private static final HashMap<String, String> property2Icon = new HashMap<String, String>() {

        {
            put("ABBR", NbBundle.getMessage(this.getClass(), "PropertyTag.ABBR"));
            put("ADDR", NbBundle.getMessage(this.getClass(), "PropertyTag.ADDR"));
            put("ADOP", NbBundle.getMessage(this.getClass(), "PropertyTag.ADOP"));
            put("ADR1", NbBundle.getMessage(this.getClass(), "PropertyTag.ADR1"));
            put("ADR2", NbBundle.getMessage(this.getClass(), "PropertyTag.ADR2"));
            put("AFN", NbBundle.getMessage(this.getClass(), "PropertyTag.AFN"));
            put("AGE", NbBundle.getMessage(this.getClass(), "PropertyTag.AGE"));
            put("AGNC", NbBundle.getMessage(this.getClass(), "PropertyTag.AGNC"));
            put("ALIA", NbBundle.getMessage(this.getClass(), "PropertyTag.ALIA"));
            put("ANCE", NbBundle.getMessage(this.getClass(), "PropertyTag.ANCE"));
            put("ANCI", NbBundle.getMessage(this.getClass(), "PropertyTag.ANCI"));
            put("ANUL", NbBundle.getMessage(this.getClass(), "PropertyTag.ANUL"));
            put("ASSO", NbBundle.getMessage(this.getClass(), "PropertyTag.ASSO"));
            put("AUTH", NbBundle.getMessage(this.getClass(), "PropertyTag.AUTH"));
            put("BAPL", NbBundle.getMessage(this.getClass(), "PropertyTag.BAPL"));
            put("BAPM", NbBundle.getMessage(this.getClass(), "PropertyTag.BAPM"));
            put("BARM", NbBundle.getMessage(this.getClass(), "PropertyTag.BARM"));
            put("BASM", NbBundle.getMessage(this.getClass(), "PropertyTag.BASM"));
            put("BIRT", NbBundle.getMessage(this.getClass(), "PropertyTag.BIRT"));
            put("BLES", NbBundle.getMessage(this.getClass(), "PropertyTag.BLES"));
            put("BLOB", NbBundle.getMessage(this.getClass(), "PropertyTag.BLOB"));
            put("BURI", NbBundle.getMessage(this.getClass(), "PropertyTag.BURI"));
            put("CALN", NbBundle.getMessage(this.getClass(), "PropertyTag.CALN"));
            put("CAST", NbBundle.getMessage(this.getClass(), "PropertyTag.CAST"));
            put("CAUS", NbBundle.getMessage(this.getClass(), "PropertyTag.CAUS"));
            put("CENS", NbBundle.getMessage(this.getClass(), "PropertyTag.CENS"));
            put("CHAN", NbBundle.getMessage(this.getClass(), "PropertyTag.CHAN"));
            put("CHAR", NbBundle.getMessage(this.getClass(), "PropertyTag.CHAR"));
            put("CHIL", NbBundle.getMessage(this.getClass(), "PropertyTag.CHIL"));
            put("CHR", NbBundle.getMessage(this.getClass(), "PropertyTag.CHR"));
            put("CHRA", NbBundle.getMessage(this.getClass(), "PropertyTag.CHRA"));
            put("CITY", NbBundle.getMessage(this.getClass(), "PropertyTag.CITY"));
            put("CONC", NbBundle.getMessage(this.getClass(), "PropertyTag.CONC"));
            put("CONF", NbBundle.getMessage(this.getClass(), "PropertyTag.CONF"));
            put("CONL", NbBundle.getMessage(this.getClass(), "PropertyTag.CONL"));
            put("CONT", NbBundle.getMessage(this.getClass(), "PropertyTag.CONT"));
            put("COPR", NbBundle.getMessage(this.getClass(), "PropertyTag.COPR"));
            put("CORP", NbBundle.getMessage(this.getClass(), "PropertyTag.CORP"));
            put("CREM", NbBundle.getMessage(this.getClass(), "PropertyTag.CREM"));
            put("CTRY", NbBundle.getMessage(this.getClass(), "PropertyTag.CTRY"));
            put("DATA", NbBundle.getMessage(this.getClass(), "PropertyTag.DATA"));
            put("DATE", NbBundle.getMessage(this.getClass(), "PropertyTag.DATE"));
            put("DEAT", NbBundle.getMessage(this.getClass(), "PropertyTag.DEAT"));
            put("DESC", NbBundle.getMessage(this.getClass(), "PropertyTag.DESC"));
            put("DESI", NbBundle.getMessage(this.getClass(), "PropertyTag.DESI"));
            put("DEST", NbBundle.getMessage(this.getClass(), "PropertyTag.DEST"));
            put("DIV", NbBundle.getMessage(this.getClass(), "PropertyTag.DIV"));
            put("DIVF", NbBundle.getMessage(this.getClass(), "PropertyTag.DIVF"));
            put("DSCR", NbBundle.getMessage(this.getClass(), "PropertyTag.DSCR"));
            put("EDUC", NbBundle.getMessage(this.getClass(), "PropertyTag.EDUC"));
            put("EMIG", NbBundle.getMessage(this.getClass(), "PropertyTag.EMIG"));
            put("ENDL", NbBundle.getMessage(this.getClass(), "PropertyTag.ENDL"));
            put("ENGA", NbBundle.getMessage(this.getClass(), "PropertyTag.ENGA"));
            put("EVEN", NbBundle.getMessage(this.getClass(), "PropertyTag.EVEN"));
            put("FAM", NbBundle.getMessage(this.getClass(), "PropertyTag.FAM"));
            put("FAMC", NbBundle.getMessage(this.getClass(), "PropertyTag.FAMC"));
            put("FAMF", NbBundle.getMessage(this.getClass(), "PropertyTag.FAMF"));
            put("FAMS", NbBundle.getMessage(this.getClass(), "PropertyTag.FAMS"));
            put("FCOM", NbBundle.getMessage(this.getClass(), "PropertyTag.FCOM"));
            put("FILE", NbBundle.getMessage(this.getClass(), "PropertyTag.FILE"));
            put("FORM", NbBundle.getMessage(this.getClass(), "PropertyTag.FORM"));
            put("HUSB", NbBundle.getMessage(this.getClass(), "PropertyTag.HUSB"));
            put("IMM", NbBundle.getMessage(this.getClass(), "PropertyTag.IMM"));
            put("GEDC", NbBundle.getMessage(this.getClass(), "PropertyTag.GEDC"));
            put("GIVN", NbBundle.getMessage(this.getClass(), "PropertyTag.GIVN"));
            put("GRAD", NbBundle.getMessage(this.getClass(), "PropertyTag.GRAD"));
            put("HEAD", NbBundle.getMessage(this.getClass(), "PropertyTag.HEAD"));
            put("IDNO", NbBundle.getMessage(this.getClass(), "PropertyTag.IDNO"));
            put("INDI", NbBundle.getMessage(this.getClass(), "PropertyTag.INDI"));
            put("LANG", NbBundle.getMessage(this.getClass(), "PropertyTag.LANG"));
            put("LEGA", NbBundle.getMessage(this.getClass(), "PropertyTag.LEGA"));
            put("MARB", NbBundle.getMessage(this.getClass(), "PropertyTag.MARB"));
            put("MARC", NbBundle.getMessage(this.getClass(), "PropertyTag.MARC"));
            put("MARL", NbBundle.getMessage(this.getClass(), "PropertyTag.MARL"));
            put("MARR", NbBundle.getMessage(this.getClass(), "PropertyTag.MARR"));
            put("MARS", NbBundle.getMessage(this.getClass(), "PropertyTag.MARS"));
            put("MEDI", NbBundle.getMessage(this.getClass(), "PropertyTag.MEDI"));
            put("NAME", NbBundle.getMessage(this.getClass(), "PropertyTag.NAME"));
            put("NATI", NbBundle.getMessage(this.getClass(), "PropertyTag.NATI"));
            put("NATU", NbBundle.getMessage(this.getClass(), "PropertyTag.NATU"));
            put("NCHI", NbBundle.getMessage(this.getClass(), "PropertyTag.NCHI"));
            put("NICK", NbBundle.getMessage(this.getClass(), "PropertyTag.NICK"));
            put("NMR", NbBundle.getMessage(this.getClass(), "PropertyTag.NMR"));
            put("NOTE", NbBundle.getMessage(this.getClass(), "PropertyTag.NOTE"));
            put("NPFX", NbBundle.getMessage(this.getClass(), "PropertyTag.NPFX"));
            put("NSFX", NbBundle.getMessage(this.getClass(), "PropertyTag.NSFX"));
            put("OBJE", NbBundle.getMessage(this.getClass(), "PropertyTag.OBJE"));
            put("OCCU", NbBundle.getMessage(this.getClass(), "PropertyTag.OCCU"));
            put("ORDI", NbBundle.getMessage(this.getClass(), "PropertyTag.ORDI"));
            put("ORDN", NbBundle.getMessage(this.getClass(), "PropertyTag.ORDN"));
            put("PAGE", NbBundle.getMessage(this.getClass(), "PropertyTag.PAGE"));
            put("PEDI", NbBundle.getMessage(this.getClass(), "PropertyTag.PEDI"));
            put("PLAC", NbBundle.getMessage(this.getClass(), "PropertyTag.PLAC"));
            put("POST", NbBundle.getMessage(this.getClass(), "PropertyTag.POST"));
            put("PROB", NbBundle.getMessage(this.getClass(), "PropertyTag.PROB"));
            put("PROP", NbBundle.getMessage(this.getClass(), "PropertyTag.PROP"));
            put("PUBL", NbBundle.getMessage(this.getClass(), "PropertyTag.PUBL"));
            put("QUAY", NbBundle.getMessage(this.getClass(), "PropertyTag.QUAY"));
            put("REFN", NbBundle.getMessage(this.getClass(), "PropertyTag.REFN"));
            put("RELA", NbBundle.getMessage(this.getClass(), "PropertyTag.RELA"));
            put("RELI", NbBundle.getMessage(this.getClass(), "PropertyTag.RELI"));
            put("REPO", NbBundle.getMessage(this.getClass(), "PropertyTag.REPO"));
            put("RESI", NbBundle.getMessage(this.getClass(), "PropertyTag.RESI"));
            put("RESN", NbBundle.getMessage(this.getClass(), "PropertyTag.RESN"));
            put("RETI", NbBundle.getMessage(this.getClass(), "PropertyTag.RETI"));
            put("RFN", NbBundle.getMessage(this.getClass(), "PropertyTag.RFN"));
            put("RIN", NbBundle.getMessage(this.getClass(), "PropertyTag.RIN"));
            put("ROLE", NbBundle.getMessage(this.getClass(), "PropertyTag.ROLE"));
            put("SEX", NbBundle.getMessage(this.getClass(), "PropertyTag.SEX"));
            put("SLGC", NbBundle.getMessage(this.getClass(), "PropertyTag.SLGC"));
            put("SLGS", NbBundle.getMessage(this.getClass(), "PropertyTag.SLGS"));
            put("SOUR", NbBundle.getMessage(this.getClass(), "PropertyTag.SOUR"));
            put("SPFX", NbBundle.getMessage(this.getClass(), "PropertyTag.SPFX"));
            put("SSN", NbBundle.getMessage(this.getClass(), "PropertyTag.SSN"));
            put("SUBM", NbBundle.getMessage(this.getClass(), "PropertyTag.SUBM"));
            put("SUBN", NbBundle.getMessage(this.getClass(), "PropertyTag.SUBN"));
            put("SURN", NbBundle.getMessage(this.getClass(), "PropertyTag.SURN"));
            put("STAE", NbBundle.getMessage(this.getClass(), "PropertyTag.STAE"));
            put("STAT", NbBundle.getMessage(this.getClass(), "PropertyTag.STAT"));
            put("TIME", NbBundle.getMessage(this.getClass(), "PropertyTag.TIME"));
            put("TRLR", NbBundle.getMessage(this.getClass(), "PropertyTag.TRLR"));
            put("PHON", NbBundle.getMessage(this.getClass(), "PropertyTag.PHON"));
            put("TEMP", NbBundle.getMessage(this.getClass(), "PropertyTag.TEMP"));
            put("TEXT", NbBundle.getMessage(this.getClass(), "PropertyTag.TEXT"));
            put("TITL", NbBundle.getMessage(this.getClass(), "PropertyTag.TITL"));
            put("TYPE", NbBundle.getMessage(this.getClass(), "PropertyTag.TYPE"));
            put("VERS", NbBundle.getMessage(this.getClass(), "PropertyTag.VERS"));
            put("WIFE", NbBundle.getMessage(this.getClass(), "PropertyTag.WIFE"));
            put("WILL", NbBundle.getMessage(this.getClass(), "PropertyTag.WILL"));
            put("XREF", NbBundle.getMessage(this.getClass(), "PropertyTag.XREF"));
        }
    };

    static public String getTagName(String propertyTag) {
        if (property2Icon.get(propertyTag) != null) {
            return property2Icon.get(propertyTag);
        } else {
            return "U"+propertyTag;
        }
    }
}
