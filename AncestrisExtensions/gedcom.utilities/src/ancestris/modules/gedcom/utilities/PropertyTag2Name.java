package ancestris.modules.gedcom.utilities;

import java.util.HashMap;
import org.openide.util.NbBundle;

/**
 *
 * @author lemovice
 */
public class PropertyTag2Name {

    private static final HashMap<String, String> propertyTag2Name = new HashMap<String, String>() {

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
            put("IMMI", NbBundle.getMessage(this.getClass(), "PropertyTag.IMMI"));
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
    private static final HashMap<String, String> propertyName2Tag = new HashMap<String, String>() {

        {
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.ABBR"), "ABBR");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.ADDR"), "ADDR");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.ADOP"), "ADOP");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.ADR1"), "ADR1");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.ADR2"), "ADR2");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.AFN"), "AFN");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.AGE"), "AGE");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.AGNC"), "AGNC");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.ALIA"), "ALIA");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.ANCE"), "ANCE");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.ANCI"), "ANCI");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.ANUL"), "ANUL");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.ASSO"), "ASSO");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.AUTH"), "AUTH");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.BAPL"), "BAPL");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.BAPM"), "BAPM");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.BARM"), "BARM");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.BASM"), "BASM");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.BIRT"), "BIRT");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.BLES"), "BLES");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.BLOB"), "BLOB");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.BURI"), "BURI");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.CALN"), "CALN");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.CAST"), "CAST");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.CAUS"), "CAUS");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.CENS"), "CENS");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.CHAN"), "CHAN");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.CHAR"), "CHAR");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.CHIL"), "CHIL");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.CHR"), "CHR");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.CHRA"), "CHRA");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.CITY"), "CITY");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.CONC"), "CONC");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.CONF"), "CONF");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.CONL"), "CONL");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.CONT"), "CONT");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.COPR"), "COPR");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.CORP"), "CORP");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.CREM"), "CREM");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.CTRY"), "CTRY");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.DATA"), "DATA");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.DATE"), "DATE");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.DEAT"), "DEAT");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.DESC"), "DESC");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.DESI"), "DESI");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.DEST"), "DEST");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.DIV"), "DIV");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.DIVF"), "DIVF");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.DSCR"), "DSCR");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.EDUC"), "EDUC");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.EMIG"), "EMIG");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.ENDL"), "ENDL");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.ENGA"), "ENGA");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.EVEN"), "EVEN");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.FAM"), "FAM");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.FAMC"), "FAMC");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.FAMF"), "FAMF");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.FAMS"), "FAMS");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.FCOM"), "FCOM");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.FILE"), "FILE");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.FORM"), "FORM");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.HUSB"), "HUSB");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.IMMI"), "IMMI");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.GEDC"), "GEDC");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.GIVN"), "GIVN");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.GRAD"), "GRAD");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.HEAD"), "HEAD");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.IDNO"), "IDNO");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.INDI"), "INDI");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.LANG"), "LANG");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.LEGA"), "LEGA");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.MARB"), "MARB");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.MARC"), "MARC");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.MARL"), "MARL");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.MARR"), "MARR");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.MARS"), "MARS");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.MEDI"), "MEDI");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.NAME"), "NAME");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.NATI"), "NATI");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.NATU"), "NATU");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.NCHI"), "NCHI");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.NICK"), "NICK");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.NMR"), "NMR");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.NOTE"), "NOTE");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.NPFX"), "NPFX");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.NSFX"), "NSFX");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.OBJE"), "OBJE");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.OCCU"), "OCCU");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.ORDI"), "ORDI");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.ORDN"), "ORDN");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.PAGE"), "PAGE");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.PEDI"), "PEDI");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.PLAC"), "PLAC");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.POST"), "POST");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.PROB"), "PROB");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.PROP"), "PROP");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.PUBL"), "PUBL");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.QUAY"), "QUAY");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.REFN"), "REFN");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.RELA"), "RELA");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.RELI"), "RELI");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.REPO"), "REPO");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.RESI"), "RESI");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.RESN"), "RESN");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.RETI"), "RETI");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.RFN"), "RFN");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.RIN"), "RIN");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.ROLE"), "ROLE");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.SEX"), "SEX");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.SLGC"), "SLGC");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.SLGS"), "SLGS");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.SOUR"), "SOUR");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.SPFX"), "SPFX");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.SSN"), "SSN");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.SUBM"), "SUBM");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.SUBN"), "SUBN");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.SURN"), "SURN");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.STAE"), "STAE");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.STAT"), "STAT");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.TIME"), "TIME");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.TRLR"), "TRLR");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.PHON"), "PHON");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.TEMP"), "TEMP");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.TEXT"), "TEXT");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.TITL"), "TITL");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.TYPE"), "TYPE");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.VERS"), "VERS");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.WIFE"), "WIFE");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.WILL"), "WILL");
            put(NbBundle.getMessage(this.getClass(), "PropertyTag.XREF"), "XREF");
        }
    };

    static public String getTagName(String propertyTag) {
        if (propertyTag2Name.get(propertyTag) != null) {
            return propertyTag2Name.get(propertyTag);
        } else {
            return "U" + propertyTag;
        }
    }

    static public String getPropertyTag(String propertyName) {
        if (propertyTag2Name.containsValue(propertyName) == true) {
            return propertyName2Tag.get(propertyName);
        } else {
            return "UnkTag";
        }
    }
}
