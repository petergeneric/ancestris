package ancestris.modules.gedcom.utilities;

import genj.gedcom.Gedcom;
import java.util.HashMap;

/**
 *
 * @author lemovice
 */
public class PropertyTag2Name {

    private static final HashMap<String, String> propertyTag2Name = new HashMap<String, String>() {

        {
            put("ABBR", Gedcom.getName("ABBR"));
            put("ADDR", Gedcom.getName("ADDR"));
            put("ADOP", Gedcom.getName("ADOP"));
            put("ADR1", Gedcom.getName("ADR1"));
            put("ADR2", Gedcom.getName("ADR2"));
            put("AFN", Gedcom.getName("AFN"));
            put("AGE", Gedcom.getName("AGE"));
            put("AGNC", Gedcom.getName("AGNC"));
            put("ALIA", Gedcom.getName("ALIA"));
            put("ANCE", Gedcom.getName("ANCE"));
            put("ANCI", Gedcom.getName("ANCI"));
            put("ANUL", Gedcom.getName("ANUL"));
            put("ASSO", Gedcom.getName("ASSO"));
            put("AUTH", Gedcom.getName("AUTH"));
            put("BAPL", Gedcom.getName("BAPL"));
            put("BAPM", Gedcom.getName("BAPM"));
            put("BARM", Gedcom.getName("BARM"));
            put("BASM", Gedcom.getName("BASM"));
            put("BIRT", Gedcom.getName("BIRT"));
            put("BLES", Gedcom.getName("BLES"));
            put("BLOB", Gedcom.getName("BLOB"));
            put("BURI", Gedcom.getName("BURI"));
            put("CALN", Gedcom.getName("CALN"));
            put("CAST", Gedcom.getName("CAST"));
            put("CAUS", Gedcom.getName("CAUS"));
            put("CENS", Gedcom.getName("CENS"));
            put("CHAN", Gedcom.getName("CHAN"));
            put("CHAR", Gedcom.getName("CHAR"));
            put("CHIL", Gedcom.getName("CHIL"));
            put("CHR", Gedcom.getName("CHR"));
            put("CHRA", Gedcom.getName("CHRA"));
            put("CITY", Gedcom.getName("CITY"));
            put("CONC", Gedcom.getName("CONC"));
            put("CONF", Gedcom.getName("CONF"));
            put("CONL", Gedcom.getName("CONL"));
            put("CONT", Gedcom.getName("CONT"));
            put("COPR", Gedcom.getName("COPR"));
            put("CORP", Gedcom.getName("CORP"));
            put("CREM", Gedcom.getName("CREM"));
            put("CTRY", Gedcom.getName("CTRY"));
            put("DATA", Gedcom.getName("DATA"));
            put("DATE", Gedcom.getName("DATE"));
            put("DEAT", Gedcom.getName("DEAT"));
            put("DESC", Gedcom.getName("DESC"));
            put("DESI", Gedcom.getName("DESI"));
            put("DEST", Gedcom.getName("DEST"));
            put("DIV", Gedcom.getName("DIV"));
            put("DIVF", Gedcom.getName("DIVF"));
            put("DSCR", Gedcom.getName("DSCR"));
            put("EDUC", Gedcom.getName("EDUC"));
            put("EMIG", Gedcom.getName("EMIG"));
            put("ENDL", Gedcom.getName("ENDL"));
            put("ENGA", Gedcom.getName("ENGA"));
            put("EVEN", Gedcom.getName("EVEN"));
            put("FAM", Gedcom.getName("FAM"));
            put("FAMC", Gedcom.getName("FAMC"));
            put("FAMF", Gedcom.getName("FAMF"));
            put("FAMS", Gedcom.getName("FAMS"));
            put("FCOM", Gedcom.getName("FCOM"));
            put("FILE", Gedcom.getName("FILE"));
            put("FORM", Gedcom.getName("FORM"));
            put("HUSB", Gedcom.getName("HUSB"));
            put("IMMI", Gedcom.getName("IMMI"));
            put("GEDC", Gedcom.getName("GEDC"));
            put("GIVN", Gedcom.getName("GIVN"));
            put("GRAD", Gedcom.getName("GRAD"));
            put("HEAD", Gedcom.getName("HEAD"));
            put("IDNO", Gedcom.getName("IDNO"));
            put("INDI", Gedcom.getName("INDI"));
            put("LANG", Gedcom.getName("LANG"));
            put("LEGA", Gedcom.getName("LEGA"));
            put("MARB", Gedcom.getName("MARB"));
            put("MARC", Gedcom.getName("MARC"));
            put("MARL", Gedcom.getName("MARL"));
            put("MARR", Gedcom.getName("MARR"));
            put("MARS", Gedcom.getName("MARS"));
            put("MEDI", Gedcom.getName("MEDI"));
            put("NAME", Gedcom.getName("NAME"));
            put("NATI", Gedcom.getName("NATI"));
            put("NATU", Gedcom.getName("NATU"));
            put("NCHI", Gedcom.getName("NCHI"));
            put("NICK", Gedcom.getName("NICK"));
            put("NMR", Gedcom.getName("NMR"));
            put("NOTE", Gedcom.getName("NOTE"));
            put("NPFX", Gedcom.getName("NPFX"));
            put("NSFX", Gedcom.getName("NSFX"));
            put("OBJE", Gedcom.getName("OBJE"));
            put("OCCU", Gedcom.getName("OCCU"));
            put("ORDI", Gedcom.getName("ORDI"));
            put("ORDN", Gedcom.getName("ORDN"));
            put("PAGE", Gedcom.getName("PAGE"));
            put("PEDI", Gedcom.getName("PEDI"));
            put("PLAC", Gedcom.getName("PLAC"));
            put("POST", Gedcom.getName("POST"));
            put("PROB", Gedcom.getName("PROB"));
            put("PROP", Gedcom.getName("PROP"));
            put("PUBL", Gedcom.getName("PUBL"));
            put("QUAY", Gedcom.getName("QUAY"));
            put("REFN", Gedcom.getName("REFN"));
            put("RELA", Gedcom.getName("RELA"));
            put("RELI", Gedcom.getName("RELI"));
            put("REPO", Gedcom.getName("REPO"));
            put("RESI", Gedcom.getName("RESI"));
            put("RESN", Gedcom.getName("RESN"));
            put("RETI", Gedcom.getName("RETI"));
            put("RFN", Gedcom.getName("RFN"));
            put("RIN", Gedcom.getName("RIN"));
            put("ROLE", Gedcom.getName("ROLE"));
            put("SEX", Gedcom.getName("SEX"));
            put("SLGC", Gedcom.getName("SLGC"));
            put("SLGS", Gedcom.getName("SLGS"));
            put("SOUR", Gedcom.getName("SOUR"));
            put("SPFX", Gedcom.getName("SPFX"));
            put("SSN", Gedcom.getName("SSN"));
            put("SUBM", Gedcom.getName("SUBM"));
            put("SUBN", Gedcom.getName("SUBN"));
            put("SURN", Gedcom.getName("SURN"));
            put("STAE", Gedcom.getName("STAE"));
            put("STAT", Gedcom.getName("STAT"));
            put("TIME", Gedcom.getName("TIME"));
            put("TRLR", Gedcom.getName("TRLR"));
            put("PHON", Gedcom.getName("PHON"));
            put("TEMP", Gedcom.getName("TEMP"));
            put("TEXT", Gedcom.getName("TEXT"));
            put("TITL", Gedcom.getName("TITL"));
            put("TYPE", Gedcom.getName("TYPE"));
            put("VERS", Gedcom.getName("VERS"));
            put("WIFE", Gedcom.getName("WIFE"));
            put("WILL", Gedcom.getName("WILL"));
            put("XREF", Gedcom.getName("XREF"));
        }
    };
    private static final HashMap<String, String> propertyName2Tag = new HashMap<String, String>() {

        {
            put(Gedcom.getName("ABBR"), "ABBR");
            put(Gedcom.getName("ADDR"), "ADDR");
            put(Gedcom.getName("ADOP"), "ADOP");
            put(Gedcom.getName("ADR1"), "ADR1");
            put(Gedcom.getName("ADR2"), "ADR2");
            put(Gedcom.getName("AFN"), "AFN");
            put(Gedcom.getName("AGE"), "AGE");
            put(Gedcom.getName("AGNC"), "AGNC");
            put(Gedcom.getName("ALIA"), "ALIA");
            put(Gedcom.getName("ANCE"), "ANCE");
            put(Gedcom.getName("ANCI"), "ANCI");
            put(Gedcom.getName("ANUL"), "ANUL");
            put(Gedcom.getName("ASSO"), "ASSO");
            put(Gedcom.getName("AUTH"), "AUTH");
            put(Gedcom.getName("BAPL"), "BAPL");
            put(Gedcom.getName("BAPM"), "BAPM");
            put(Gedcom.getName("BARM"), "BARM");
            put(Gedcom.getName("BASM"), "BASM");
            put(Gedcom.getName("BIRT"), "BIRT");
            put(Gedcom.getName("BLES"), "BLES");
            put(Gedcom.getName("BLOB"), "BLOB");
            put(Gedcom.getName("BURI"), "BURI");
            put(Gedcom.getName("CALN"), "CALN");
            put(Gedcom.getName("CAST"), "CAST");
            put(Gedcom.getName("CAUS"), "CAUS");
            put(Gedcom.getName("CENS"), "CENS");
            put(Gedcom.getName("CHAN"), "CHAN");
            put(Gedcom.getName("CHAR"), "CHAR");
            put(Gedcom.getName("CHIL"), "CHIL");
            put(Gedcom.getName("CHR"), "CHR");
            put(Gedcom.getName("CHRA"), "CHRA");
            put(Gedcom.getName("CITY"), "CITY");
            put(Gedcom.getName("CONC"), "CONC");
            put(Gedcom.getName("CONF"), "CONF");
            put(Gedcom.getName("CONL"), "CONL");
            put(Gedcom.getName("CONT"), "CONT");
            put(Gedcom.getName("COPR"), "COPR");
            put(Gedcom.getName("CORP"), "CORP");
            put(Gedcom.getName("CREM"), "CREM");
            put(Gedcom.getName("CTRY"), "CTRY");
            put(Gedcom.getName("DATA"), "DATA");
            put(Gedcom.getName("DATE"), "DATE");
            put(Gedcom.getName("DEAT"), "DEAT");
            put(Gedcom.getName("DESC"), "DESC");
            put(Gedcom.getName("DESI"), "DESI");
            put(Gedcom.getName("DEST"), "DEST");
            put(Gedcom.getName("DIV"), "DIV");
            put(Gedcom.getName("DIVF"), "DIVF");
            put(Gedcom.getName("DSCR"), "DSCR");
            put(Gedcom.getName("EDUC"), "EDUC");
            put(Gedcom.getName("EMIG"), "EMIG");
            put(Gedcom.getName("ENDL"), "ENDL");
            put(Gedcom.getName("ENGA"), "ENGA");
            put(Gedcom.getName("EVEN"), "EVEN");
            put(Gedcom.getName("FAM"), "FAM");
            put(Gedcom.getName("FAMC"), "FAMC");
            put(Gedcom.getName("FAMF"), "FAMF");
            put(Gedcom.getName("FAMS"), "FAMS");
            put(Gedcom.getName("FCOM"), "FCOM");
            put(Gedcom.getName("FILE"), "FILE");
            put(Gedcom.getName("FORM"), "FORM");
            put(Gedcom.getName("HUSB"), "HUSB");
            put(Gedcom.getName("IMMI"), "IMMI");
            put(Gedcom.getName("GEDC"), "GEDC");
            put(Gedcom.getName("GIVN"), "GIVN");
            put(Gedcom.getName("GRAD"), "GRAD");
            put(Gedcom.getName("HEAD"), "HEAD");
            put(Gedcom.getName("IDNO"), "IDNO");
            put(Gedcom.getName("INDI"), "INDI");
            put(Gedcom.getName("LANG"), "LANG");
            put(Gedcom.getName("LEGA"), "LEGA");
            put(Gedcom.getName("MARB"), "MARB");
            put(Gedcom.getName("MARC"), "MARC");
            put(Gedcom.getName("MARL"), "MARL");
            put(Gedcom.getName("MARR"), "MARR");
            put(Gedcom.getName("MARS"), "MARS");
            put(Gedcom.getName("MEDI"), "MEDI");
            put(Gedcom.getName("NAME"), "NAME");
            put(Gedcom.getName("NATI"), "NATI");
            put(Gedcom.getName("NATU"), "NATU");
            put(Gedcom.getName("NCHI"), "NCHI");
            put(Gedcom.getName("NICK"), "NICK");
            put(Gedcom.getName("NMR"), "NMR");
            put(Gedcom.getName("NOTE"), "NOTE");
            put(Gedcom.getName("NPFX"), "NPFX");
            put(Gedcom.getName("NSFX"), "NSFX");
            put(Gedcom.getName("OBJE"), "OBJE");
            put(Gedcom.getName("OCCU"), "OCCU");
            put(Gedcom.getName("ORDI"), "ORDI");
            put(Gedcom.getName("ORDN"), "ORDN");
            put(Gedcom.getName("PAGE"), "PAGE");
            put(Gedcom.getName("PEDI"), "PEDI");
            put(Gedcom.getName("PLAC"), "PLAC");
            put(Gedcom.getName("POST"), "POST");
            put(Gedcom.getName("PROB"), "PROB");
            put(Gedcom.getName("PROP"), "PROP");
            put(Gedcom.getName("PUBL"), "PUBL");
            put(Gedcom.getName("QUAY"), "QUAY");
            put(Gedcom.getName("REFN"), "REFN");
            put(Gedcom.getName("RELA"), "RELA");
            put(Gedcom.getName("RELI"), "RELI");
            put(Gedcom.getName("REPO"), "REPO");
            put(Gedcom.getName("RESI"), "RESI");
            put(Gedcom.getName("RESN"), "RESN");
            put(Gedcom.getName("RETI"), "RETI");
            put(Gedcom.getName("RFN"), "RFN");
            put(Gedcom.getName("RIN"), "RIN");
            put(Gedcom.getName("ROLE"), "ROLE");
            put(Gedcom.getName("SEX"), "SEX");
            put(Gedcom.getName("SLGC"), "SLGC");
            put(Gedcom.getName("SLGS"), "SLGS");
            put(Gedcom.getName("SOUR"), "SOUR");
            put(Gedcom.getName("SPFX"), "SPFX");
            put(Gedcom.getName("SSN"), "SSN");
            put(Gedcom.getName("SUBM"), "SUBM");
            put(Gedcom.getName("SUBN"), "SUBN");
            put(Gedcom.getName("SURN"), "SURN");
            put(Gedcom.getName("STAE"), "STAE");
            put(Gedcom.getName("STAT"), "STAT");
            put(Gedcom.getName("TIME"), "TIME");
            put(Gedcom.getName("TRLR"), "TRLR");
            put(Gedcom.getName("PHON"), "PHON");
            put(Gedcom.getName("TEMP"), "TEMP");
            put(Gedcom.getName("TEXT"), "TEXT");
            put(Gedcom.getName("TITL"), "TITL");
            put(Gedcom.getName("TYPE"), "TYPE");
            put(Gedcom.getName("VERS"), "VERS");
            put(Gedcom.getName("WIFE"), "WIFE");
            put(Gedcom.getName("WILL"), "WILL");
            put(Gedcom.getName("XREF"), "XREF");
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
