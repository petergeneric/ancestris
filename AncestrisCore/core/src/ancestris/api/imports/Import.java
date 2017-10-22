/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 *
 * sur une base de gedrepohr.pl (Patrick TEXIER) pour la correction des REPO Le
 * reste des traitements par Daniel ANDRE
 */
package ancestris.api.imports;

import ancestris.core.TextOptions;
import ancestris.modules.console.Console;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomOptions;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyAssociation;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertyRelationship;
import genj.gedcom.TagPath;
import genj.io.GedcomFileReader;
import genj.io.GedcomFileWriter;
import genj.io.GedcomFormatException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import org.openide.util.NbBundle;

/**
 * The import function foreign gedcom. This abstract class must be used to
 * create a gedcom importer which can then be proposed in the Import wizard.
 * <p>
 * The import process has two main steps:
 * <ul><li/>First step is some sot of filter which process the input file and
 * produce another text file in a tmp directory. This filter process is done in
 * two passes: analyzing and writing.
 * <li/>Then this file is opened as an ordinary gedcom file in ancestris memory
 * and corrected to make the whole gedcom data as compliant as possible.
 * </ul>
 * <p/>
 * Note: The file produced in filter step is deleted by the caller to force the
 * user to save the new gedcom as a new file.
 *
 */
public abstract class Import {
    
    protected static String EOL = System.getProperty("line.separator");
    
    protected static final String INDI_TAG_YES = "BIRT|CHR|DEAT|BURI|CREM|"
            + "ADOP|BAPM|BARM|BASM|BLES|CHRA|CONF|FCOM|ORDN|NATU|EMIG|IMMI|"
            + "CENS|PROB|WILL|GRAD|RETI|";
    protected static final String FAM_TAG_YES = "ANUL|CENS|DIV|DIVF|"
            + "ENGA|MARR|MARB|MARC|MARL|MARS";
    protected static Pattern tag_y = Pattern.compile("(" + INDI_TAG_YES + FAM_TAG_YES + ")");

    protected static String GEDCOM_VERSION = "5.5";
    protected static final String GEDCOM55_TAG = "ABBR|ADDR|ADR1|ADR2|ADOP|AFN|AGE|AGNC|ALIA|ANCE|ANCI|ANUL|ASSO|AUTH|"
            + "BAPL|BAPM|BARM|BASM|BIRT|BLES|BLOB|BURI|CALN|CAST|CAUS|CENS|CHAN|CHAR|"
            + "CHIL|CHR|CHRA|CITY|CONC|CONF|CONL|CONT|COPR|CORP|CREM|CTRY|DATA|DATE|"
            + "DEAT|DESC|DESI|DEST|DIV|DIVF|DSCR|EDUC|EMIG|ENDL|ENGA|EVEN|FAM|FAMC|"
            + "FAMF|FAMS|FCOM|FILE|FORM|GEDC|GIVN|GRAD|HEAD|HUSB|IDNO|IMMI|INDI|LANG|"
            + "LEGA|MARB|MARC|MARL|MARR|MARS|MEDI|NAME|NATI|NATU|NCHI|NICK|NMR|NOTE|"
            + "NPFX|NSFX|OBJE|OCCU|ORDI|ORDN|PAGE|PEDI|PHON|PLAC|POST|PROB|PROP|PUBL|"
            + "QUAY|REFN|RELA|RELI|REPO|RESI|RESN|RETI|RFN|RIN|ROLE|SEX|SLGC|SLGS|"
            + "SOUR|SPFX|SSN|STAE|STAT|SUBM|SUBN|SURN|TEMP|TEXT|TIME|TITL|TRLR|TYPE|"
            + "VERS|WIFE|WILL";
    protected static final String GEDCOM551_TAG = "|EMAIL|FAX|FACT|FONE|ROMN|WWW|MAP|LATI|LONG|";

    protected static Pattern tag55_valid = Pattern.compile("(" + GEDCOM55_TAG + ")");
    protected static Pattern tag551_valid = Pattern.compile("(" + GEDCOM55_TAG + GEDCOM551_TAG + ")");

    protected static final String typerepo = "REPO";  // Debut de la cle REPO dans le gedcom
    // static Pattern pattern = Pattern.compile("^1 REPO (.*)");
    protected static Pattern gedcom_line = Pattern.compile("^(\\d) (_*\\w+)(.*)");

    // Header
    private boolean headerzone = false;
    
    // Destination
    private static int TAG_MISSING = 0;
    private static int TAG_INVALID = 1;
    private static int TAG_VALID = 2;
    private int destination_found = TAG_MISSING;
    
    // Place format
    private static int PLACE_MAX_LENGTH = 20;
    private int place_found = TAG_MISSING;
    private Integer[] place_format_sizes = new Integer[PLACE_MAX_LENGTH];
    
    // Lists of entities
    private static HashMap<String, ImportEnt> hashIndis;
    private static HashMap<String, ImportEnt> hashFams;
    private static HashMap<String, ImportEnt> hashNotes;
    private static HashMap<String, ImportEnt> hashObjes;
    private static HashMap<String, ImportEnt> hashSours;
    private static HashMap<String, ImportEnt> hashRepos;
    private static HashMap<String, ImportEnt> hashSubms;
    
    /**
     * our files
     */
    public GedcomFileReader input;
    public GedcomFileWriter output;
    protected Console console;
    public int nbChanges = 0;

    
    /**
     * Constructor
     */
    public Import() {
    }

    /**
     * Gives back import name
     */
    public abstract String toString();

    /**
     * Identifies generic module
     */
    public abstract boolean isGeneric();

    /**
     * Gives back output file name
     */
    protected abstract String getImportComment();

    public void setTabName(String tabName) {
        console = new Console(tabName);
    }

    /**
     * This runs the first 3 steps of the import process. This method is a file filter.
     *
     * @param fileIn Gedcom to import
     * @param fileOut Temporary Gedcom file created
     * @return true if conversion is successful
     */
    public boolean run(File fileIn, File fileOut) {
        init();
        
        // Read pass. No writing is made
        try {
            input = GedcomFileReader.create(fileIn);
            try {
                /*
                 * readLine is a bit quirky : it returns the content of a line
                 * MINUS the newline. it returns null only for the END of the
                 * stream. it returns an empty String if two newlines appear in
                 * a row.
                 */
                while ((input.getNextLine(true)) != null) {
                    firstPass();
                }
            } finally {
                input.close();
            }
        } catch (FileNotFoundException e1) {
            JOptionPane.showMessageDialog(null, NbBundle.getMessage(Import.class, "file.not.found", fileIn.getName()));
            //Exceptions.printStackTrace(e1);
            return false;
        } catch (GedcomFormatException e) {
            String l = ""+e.getLine();
            JOptionPane.showMessageDialog(null, e.getMessage() + "\n" + NbBundle.getMessage(Import.class, "error.line", l));
            //Exceptions.printStackTrace(e);
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, NbBundle.getMessage(Import.class, "error.other", e.getMessage()));
            e.printStackTrace();
            //Exceptions.printStackTrace(e);
            return false;
        }

        // maintenant on effectue toutes les transformations
        try {
            output = new GedcomFileWriter(fileOut, input.getCharset(), getEOL(fileIn));
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(null, NbBundle.getMessage(Import.class, "file.create.error", fileOut.getName()));
            return false;
        }
        try {
            console.println("=============================");
            console.println(NbBundle.getMessage(Import.class, "Import.starting"));
            console.println(NbBundle.getMessage(Import.class, "Import.version", GEDCOM_VERSION));
            console.println("=============================");
            input = GedcomFileReader.create(fileIn);
            try {
                while (input.getNextLine(true) != null) {
                    if ((input.getLevel() == 0) && (input.getTag().equals("HEAD"))) {
                        output.writeLine(input);
                        output.writeLine(1, "NOTE", getImportComment());
                        console.println(NbBundle.getMessage(Import.class, "Import.header"));
                        nbChanges++;
                        continue;
                    }
                    if (process()) {
                        continue;
                    }

                    if (input.getTag().equals("TRLR")) {
                        finalise();
                        output.writeLine(0, "TRLR", null);
                        continue;
                    }
                    output.writeLine(input);
                }
            } finally {
                input.close();
                output.close();
            }

        } catch (FileNotFoundException e1) {
            JOptionPane.showMessageDialog(null, NbBundle.getMessage(Import.class, "file.not.found", fileIn.getName()));
            return false;
        } catch (GedcomFormatException e) {
            String l = ""+e.getLine();
            JOptionPane.showMessageDialog(null, e.getMessage() + "\n" + NbBundle.getMessage(Import.class, "error.line", l));
            //Exceptions.printStackTrace(e);
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            return false;
        }

        console.println("=============================");
        return true;
    }

    ///////////////////////////// START OF LOGIC ///////////////////////////////
    
    
    /**
     * *** 0 *** Initialisation of variables
     */
    protected void init() {
        headerzone = false;
        destination_found = TAG_MISSING;
        place_found = TAG_MISSING;
        for (int i = 0 ; i < PLACE_MAX_LENGTH ; i++) {
            place_format_sizes[i] = 0;
        }
        hashIndis = new HashMap<String, ImportEnt>();
        hashFams  = new HashMap<String, ImportEnt>();
        hashNotes = new HashMap<String, ImportEnt>();
        hashObjes = new HashMap<String, ImportEnt>();
        hashSours = new HashMap<String, ImportEnt>();
        hashRepos = new HashMap<String, ImportEnt>();
        hashSubms = new HashMap<String, ImportEnt>();
    }

    /**
     * *** 1 *** This is the first step of import process. The file is analysed line by line.
     * Purpose is to :
     * - Get structure of gedcom file for later display somewhere (origin, number of entities of each type, number of user define labels and types, etc.)
     * - Get gedcom version (default is 5.5 defined in declaration parameters)
     * - Determine destination type : missing or invalid or correct
     * - Remember all entities IDs
     */
    protected void firstPass() {
        
        // Set headerzone
        if (headerzone && (input.getLevel() == 0) && !input.getTag().equals("HEAD")) {
            headerzone = false;
        }
        if ((input.getLevel() == 0) && input.getTag().equals("HEAD")) {
            headerzone = true;
        }
        
        
        // Get gedcom version (default is 5.5 defined in declaration parameters)
        if ((input.getLevel() == 2) && input.getTag().equals("VERS")) {
            GEDCOM_VERSION = input.getValue();
        }
        
        // Determine destination type : missing or invalid or correct
        if ((input.getLevel() == 1) && input.getTag().equals("DEST")) {
            String value = input.getValue();
            if (value != null && (value.equals("ANSTFILE") || value.equals("TempleReady"))) {
                destination_found = TAG_VALID;
            } else {
                destination_found = TAG_INVALID;
            }
        }
        
        // Determine place tag : missing or invalid or correct
        if (headerzone && (input.getLevel() == 1) && input.getTag().equals("PLAC")) {
            try {
                input.getNextLine(false);
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            }
            if (input.getTag().equals("FORM") && input.getValue() != null) {
                String form = input.getValue().replaceAll(" ", "").replaceAll(PropertyPlace.JURISDICTION_SEPARATOR, "");  // remove spaces and commas
                form = form.toUpperCase(TextOptions.getInstance().getOutputLocale(Locale.getDefault()));   // turn to uppercase using Ancestris or default locale
                if (form.matches("\\p{L}*")) {   // all unicode letters
                    place_found = TAG_VALID;
                } else {
                    place_found = TAG_INVALID;  // missing form tag or invalid
                }
            }
        }
        
        // Memorize all different place form lengths
        if (input.getTag().equals("PLAC") && !input.getValue().isEmpty()) {
            int length = input.getValue().split(PropertyPlace.JURISDICTION_SEPARATOR).length;
            if (length < PLACE_MAX_LENGTH) {
                place_format_sizes[length]++;;
            }
        }
        
        // Remember all entities IDs
        memorizeEntities();
    }

    
    /**
     * *** 2 *** This is the second step of the import process. The file is fixed line by line "on the fly".
     * Purpose is to :
     * - fix all main grammar errors (yes tags, invalid tags) that can be fixed on the fly
     */
    protected boolean process() throws IOException {
        if (processHeader()) {
            return true;
        }
        if (processYesTag()) {
            return true;
        }
        if (processInvalidTag()) {
            return true;
        }
        if (processInvalidDates()) {
            return true;
        }
        return false;
    }

    /**
     * *** 3 *** This is the third step of the import process. Neccessary lines are added at the end before the last TRLR line is written.
     * Purpose is to :
     * - fix all main grammar errors (yes tags, invalid tags) that can be fixed on the fly
     */
    protected void finalise() throws IOException {
        addMissingEntities();
    }

    /**
     * *** 4 *** This is the fourth step of the import process. The gedcom file generated in
     * step one as been loaded in memory and can be manipulated using all
     * ancestris core functionnalities. .
     * This is called only after first 3 steeps are fine.
     *
     * @param gedcom
     * @return
     */
    public boolean fixGedcom(Gedcom gedcom) {
        boolean ret = fixNames(gedcom);
        ret |= fixPlaces(gedcom);
        return ret;
    }

    /**
     * *** 5 *** This is the fifth and last step of the import process. The gedcom has fully been repaired.
     * This is mainly to let the opportunity to conclude and write an end message.
     * This is called from the import method.
     */
    public void complete() {
        console.println(NbBundle.getMessage(Import.class, "Import.completed"));
        console.println("=============================");
        //console.show();
    }
    
    
    ////////////////////////////  END OF LOGIC /////////////////////////////////

    
    
    

    
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    //                                FIXES                                   //
    ////////////////////////////////////////////////////////////////////////////

    
    private void memorizeEntities() {
        
        // Indis
        memorizeEntity("INDI", hashIndis);
        memorizeProperty("HUSB", hashIndis);
        memorizeProperty("WIFE", hashIndis);
        memorizeProperty("CHIL", hashIndis);
        memorizeProperty("ALIA", hashIndis);
        memorizeProperty("ASSO", hashIndis);
        
        // Fams
        memorizeEntity("FAM", hashFams);
        memorizeProperty("FAMS", hashFams);
        memorizeProperty("FAMC", hashFams);

        // Notes
        memorizeEntity("NOTE", hashNotes);
        memorizeProperty("NOTE", hashNotes);
        
        // Objes
        memorizeEntity("OBJE", hashObjes);
        memorizeProperty("OBJE", hashObjes);
        
        // Sours
        memorizeEntity("SOUR", hashSours);
        memorizeProperty("SOUR", hashSours);
        
        // Repos
        memorizeEntity("REPO", hashRepos);
        memorizeProperty("REPO", hashRepos);
        
        // Subms
        memorizeEntity("SUBM", hashSubms);
        memorizeProperty("SUBM", hashSubms);
        memorizeProperty("SUBN", hashSubms);
        memorizeProperty("ANCI", hashSubms);
        memorizeProperty("DESI", hashSubms);
        

    }

    private void addMissingEntities() throws IOException {
        for (String k : hashIndis.keySet()) {
            if (!hashIndis.get(k).seen) {
                output.writeLine(0, k, "INDI", null);
                nbChanges++;
            }
        }
        for (String k : hashFams.keySet()) {
            if (!hashFams.get(k).seen) {
                output.writeLine(0, k, "FAM", null);
                nbChanges++;
            }
        }
        for (String k : hashNotes.keySet()) {
            if (!hashNotes.get(k).seen) {
                output.writeLine(0, k, "NOTE", null);
                nbChanges++;
            }
        }
        for (String k : hashObjes.keySet()) {
            if (!hashObjes.get(k).seen) {
                output.writeLine(0, k, "OBJE", null);
                nbChanges++;
            }
        }
        for (String k : hashSours.keySet()) {
            if (!hashSours.get(k).seen) {
                output.writeLine(0, k, "SOUR", null);
                nbChanges++;
            }
        }
        for (String k : hashRepos.keySet()) {
            if (!hashRepos.get(k).seen) {
                output.writeLine(0, k, "REPO", null);
                nbChanges++;
            }
        }
        for (String k : hashSubms.keySet()) {
            if (!hashSubms.get(k).seen) {
                output.writeLine(0, k, "SUBM", null);
                nbChanges++;
            }
        }

    }

    public boolean processHeader() throws IOException {
        
        // DEST tag
        if (destination_found == TAG_MISSING) {
            if ((input.getLevel() == 1) && (input.getTag().equals("GEDC"))) {
                output.writeLine(1, "DEST", "ANY");
                output.writeLine(1, "GEDC", "");
                destination_found = TAG_VALID;
                console.println(NbBundle.getMessage(Import.class, "Import.fixDestination"));
                nbChanges++;
                return true;
            }
        }
        if (destination_found == TAG_INVALID) {
            if ((input.getLevel() == 1) && (input.getTag().equals("DEST"))) {
                output.writeLine(1, "DEST", "ANY");
                destination_found = TAG_VALID;
                console.println(NbBundle.getMessage(Import.class, "Import.fixDestination"));
                nbChanges++;
                return true;
            }
        }

        // PLAC tag
        if (place_found == TAG_MISSING) {
            if ((input.getLevel() == 1) && (input.getTag().equals("PLAC"))) {   // case of PLAC tag present and missing FORM subtag
                output.writeLine(input.getLevel(), input.getTag(), input.getValue());
                output.writeLine(2, "FORM", getPlaceFormat(false));
                place_found = TAG_VALID;
                nbChanges++;
                return true;
            }
            if ((input.getLevel() == 0) && (!input.getTag().equals("HEAD"))) {   // case of PLAC tag missing and next entity reached
                output.writeLine(1, "PLAC", "");
                output.writeLine(2, "FORM", getPlaceFormat(false));
                output.writeLine(input.getLevel(), input.getXref(), input.getTag(), input.getValue());
                place_found = TAG_VALID;
                nbChanges++;
                return true;
            }
        }
        if (place_found == TAG_INVALID) { // FOR NOW : do not modify place format in case of invalid characters
            if ((input.getLevel() == 1) && (input.getTag().equals("PLAC"))) {
                output.writeLine(input.getLevel(), input.getTag(), input.getValue());
                input.getNextLine(true);
                output.writeLine(input.getLevel(), input.getTag(), input.getValue());
                place_found = TAG_VALID;
                nbChanges++;
                return true;
            }
        }
        
        
        return false;
    }

    /**
     * Normallize YES_TAGS. This is called at any import.
     * Convert all "YES_TAGS" (eg BIRT, EVEN, ...) where value is not null 
     * and different from "Y" from
     * <pre>
     * n TAG value</pre>
     * to 
     * <pre>
     * n TAG
     * n+1 NOTE value</pre>
     * @return
     * @throws IOException 
     */
    public boolean processYesTag() throws IOException {
        Matcher matcher = tag_y.matcher(input.getTag());
        if (matcher.matches()) {
            String result = null;
            String tag = input.getTag();
            int level = input.getLevel();
            String line = input.getLine();
            if (input.getValue().length() != 0) {
                if (input.getValue().equalsIgnoreCase("y")){
                    output.writeLine(input);
                } else {
                    result = output.writeLine(level, tag, null);
                    result += "\n"+output.writeLine(level+1, "NOTE", input.getValue());
                }
            } else {
                String temp = input.getNextLine(false);
                if ((temp != null) && (input.getLevel() == level + 1)) {
                    output.writeLine(level, tag, null);
                } else {
                    result = output.writeLine(level, tag, "Y");
                }
            }
            if (result != null){
                nbChanges++;
                console.println(NbBundle.getMessage(Import.class, "Import.fixYesTag", line + " ==> " + result));
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Fix invalid tags and prefix them with "_".  This is called at any import.
     * @return
     * @throws IOException 
     */
    public boolean processInvalidTag() throws IOException {
        
        String lineTag = input.getTag();
        
        // C'est un tag perso: on ecrit telque
        if (lineTag.startsWith("_")) {
            return false;
        }
        // le tag n'est pas valide: on le prefixe par "_"
        Pattern tag_valid = GEDCOM_VERSION.startsWith("5.5.1") ? tag551_valid : tag55_valid;
        if (!tag_valid.matcher(lineTag).matches()) {
            String result = output.writeLine(input.getLevel(), "_" + lineTag, input.getValue());
            nbChanges++;
            console.println(NbBundle.getMessage(Import.class, "Import.fixInvalidTag", input.getLine() + " ==> " + result));
            return true;
        }
        return false;
    }

    /**
     * Normallize DATE tags. This is called at any import.
     * Ensure dates are formattedd as .. month year
     * @return
     * @throws IOException 
     */
    public boolean processInvalidDates() throws IOException {
        if ("DATE".equals(input.getTag())) {
            String date = input.getValue();
            if (date.contains("/")) {
                date = convertDate(date);
                output.writeLine(input.getLevel(), "DATE", date);
                nbChanges++;
                return true;
            }
        }
        
        return false;
    }

    public String convertDate(String date) {
        if (date.contains("@#DHEBREW@")) {  // { "TSH","CSH","KSL","TVT","SHV","ADR","ADS","NSN","IYR","SVN","TMZ","AAV","ELL" };
            date = date.replaceAll("/01/", " TSH ").replaceAll("/02/", " CSH ").replaceAll("/03/", " KSL ").replaceAll("/04/", " TVT ")
                    .replaceAll("/05/", " SHV ").replaceAll("/06/", " ADR ").replaceAll("/07/", " ADS ").replaceAll("/08/", " NSN ")
                    .replaceAll("/09/", " IYR ").replaceAll("/10/", " SVN ").replaceAll("/11/", " TMZ ").replaceAll("/12/", " AAV ").replaceAll("/13/", " ELL ")
                    .replaceAll("/1/", " TSH ").replaceAll("/2/", " CSH ").replaceAll("/3/", " KSL ").replaceAll("/4/", " TVT ")
                    .replaceAll("/5/", " SHV ").replaceAll("/6/", " ADR ").replaceAll("/7/", " ADS ").replaceAll("/8/", " NSN ")
                    .replaceAll("/9/", " IYR ");
        } else if (date.contains("@#DFRENCH R@")) { // { "VEND","BRUM","FRIM","NIVO","PLUV","VENT","GERM","FLOR","PRAI","MESS","THER","FRUC","COMP" };
            date = date.replaceAll("/01/", " VEND ").replaceAll("/02/", " BRUM ").replaceAll("/03/", " FRIM ").replaceAll("/04/", " NIVO ")
                    .replaceAll("/05/", " PLUV ").replaceAll("/06/", " VENT ").replaceAll("/07/", " GERM ").replaceAll("/08/", " FLOR ")
                    .replaceAll("/09/", " PRAI ").replaceAll("/10/", " MESS ").replaceAll("/11/", " THER ").replaceAll("/12/", " FRUC ").replaceAll("/12/", " COMP ")
                    .replaceAll("/1/", " VEND ").replaceAll("/2/", " BRUM ").replaceAll("/3/", " FRIM ").replaceAll("/4/", " NIVO ")
                    .replaceAll("/5/", " PLUV ").replaceAll("/6/", " VENT ").replaceAll("/7/", " GERM ").replaceAll("/8/", " FLOR ")
                    .replaceAll("/9/", " PRAI ");
        } else {
            date = date.replaceAll("/01/", " JAN ").replaceAll("/02/", " FEB ").replaceAll("/03/", " MAR ").replaceAll("/04/", " APR ")
                    .replaceAll("/05/", " MAY ").replaceAll("/06/", " JUN ").replaceAll("/07/", " JUL ").replaceAll("/08/", " AUG ")
                    .replaceAll("/09/", " SEP ").replaceAll("/10/", " OCT ").replaceAll("/11/", " NOV ").replaceAll("/12/", " DEC ")
                    .replaceAll("/1/", " JAN ").replaceAll("/2/", " FEB ").replaceAll("/3/", " MAR ").replaceAll("/4/", " APR ")
                    .replaceAll("/5/", " MAY ").replaceAll("/6/", " JUN ").replaceAll("/7/", " JUL ").replaceAll("/8/", " AUG ")
                    .replaceAll("/9/", " SEP ");
        }
        return date;
    }
    
    
    /**
     * Fix names.  This is called at any import.
     * Makes sure that the NAME tag has the properly constructed string from the provided subtags
     * If not, replaces NAME string and return false.
     * @param gedcom
     * @return 
     */
    public boolean fixNames(Gedcom gedcom) {

        Property rawName = null;
        PropertyName propName = null;
        boolean hasErrors = false;
        
        console.println(NbBundle.getMessage(Import.class, "Import.fixNames"));

        for (Indi indi : gedcom.getIndis()) {
            rawName = indi.getProperty("NAME", false);
            if (rawName instanceof PropertyName) {
                propName = (PropertyName) rawName;
            }
            // If name is invalid, replace it
            if (propName != null && !propName.isValid()) {
                propName.setName( // must have the same parameters as computeNameValue in PropertyName
                        propName.getNamePrefix(true), 
                        propName.getFirstName(true),  
                        propName.getSurnamePrefix(true), 
                        propName.getLastName(true), 
                        propName.getSuffix(true), 
                        false);
                hasErrors = true;
                nbChanges++;
            }
            
        }
        
        console.println("=============================");
        
        return hasErrors;
    }

    /**
     * Fix places.  This is called at any import.
     * Makes sure that the PLAC tag has the properly constructed string with the proper number of jurisdictions
     * @param gedcom
     * @return 
     */
    public boolean fixPlaces(Gedcom gedcom) {

        boolean hasErrors = false;
        String[] locs = null;
        
        console.println(NbBundle.getMessage(Import.class, "Import.fixPlaces"));

        List<PropertyPlace> places = (List<PropertyPlace>) gedcom.getPropertiesByClass(PropertyPlace.class);
        for (PropertyPlace place : places) {
            locs = place.getJurisdictions();
            if (!place.setJurisdictions(gedcom, locs)) {
                hasErrors = true;
                nbChanges++;
            }
        }
        
        console.println("=============================");
        return hasErrors;
    }

    /**
     * ConvertAssociations.  This is called only for specific imports.
     * The way associations work in ANCESTRIS is the following:
     *    If individual A is the "relation" of B, then in A, we should have 1 ASSO B with RELA = relation
     *    Example : if A is oncle of B, we should have in A the "1 ASSO @B@" tag with "2 RELA Oncle"
     * In gedcoms files from other software, it seems that :
     *    - The 1 ASSO tags are in the same direction as Ancestris
     *    - But the 2 ASSO tags underneath events of A are the other way around and should be reversed to be put under individual B.
     *      So if A has got 2 ASSO @B@ ; 3 RELA Notary, the notary is actually B
     *      so then we need to go to individual B, write 1 ASSO @A@ 2 RELA Notary
     * 
     * The idea of this method is therefore to get all ASSOs tags that are level 2 or more (PropertySimpleValue), and leave 1 ASSO as is (PropertyAssociation)
     * 
     * @param gedcom
     * @return 
     */
    public boolean convertAssociations(Gedcom gedcom) {

        String id = "";
        Indi indiRela = null;
        PropertyAssociation propAsso = null;
        String type = null;
        Property relaProp = null;
        String rela = null;
        PropertyRelationship pship = null;
        TagPath tagpath = null;

        console.println(NbBundle.getMessage(Import.class, "Import.convertingAssos"));

        List<Property> list = new ArrayList<Property>();
        for (Entity entity : gedcom.getEntities()) {
            getPropertiesRecursively(list, "ASSO", entity);
        }
        for (Property prop : list) {
            // Skip PropertyAssociation
            if (prop instanceof PropertyAssociation) {
                continue;
            }
            
            // Get initial individual B
            id = prop.getValue().replace("@", "");
            indiRela = (Indi) gedcom.getEntity(id);  // This will be the new individual A
            if (indiRela == null) {
                nbChanges++;
                console.println(NbBundle.getMessage(Import.class, "Import.indiNotFound", id));
                continue;
            }
            
            // Get type, rela and tagpath
            type = prop.getEntity().getTag();
            relaProp = prop.getProperty("RELA");
            if (relaProp != null) {
                rela = relaProp.getDisplayValue();
            }
            tagpath = prop.getParent().getPath(true);

            // Create asso set in B
            id = prop.getEntity().getId();  // id of A
            propAsso = (PropertyAssociation) indiRela.addProperty("ASSO", "@" + id + "@");  // add 1 ASSO @A@
            propAsso.addProperty("TYPE", type);
            pship = (PropertyRelationship) propAsso.getProperty("RELA", false);
            rela +=  "@" + tagpath.toString();
            if (pship == null) {
                propAsso.addProperty("RELA", rela);
            } else {
                pship.setValue(rela);
            }

            // Add other sub-tags of A:ASSO to the new B:ASSO => copy prop/subtags to propAsso/ except RELA and TYPE
            for (Property child : prop.getProperties()) {
                if (child.getTag().equals("RELA") || child.getTag().equals("TYPE")) {
                    continue;
                }
                movePropertiesRecursively(child, propAsso);
            }
            
            // Link A with B
            try {
                propAsso.link();
            } catch (GedcomException ex) {
                return false;
            }
            
            // Delete from first asso entity
            prop.getParent().delProperty(prop);
            nbChanges++;
        }
        
        console.println("=============================");
        
        return true;
    }

    
    
    
    ////////////////////////////////////////////////////////////////////////////
    //                            FIXING TOOLS                                //
    ////////////////////////////////////////////////////////////////////////////
    
    
    public <T> void getPropertiesRecursively(List<T> props, String tag, Property parent) {
        for (Property child : parent.getProperties()) {
            if (tag.equals(child.getTag())) {
                props.add((T) child);
            }
            getPropertiesRecursively(props, tag, child);
        }
    }

    private void memorizeEntity(String tag, HashMap<String, ImportEnt> hashEntities) {
        if (input.getTag().equals(tag)) {
            String xref = input.getXref();
            if (!xref.isEmpty()) {
                if (!hashEntities.containsKey(xref)) {
                    hashEntities.put(xref, new ImportEnt());
                }
                hashEntities.get(xref).seen = true;
            }
        }
    }

    private void memorizeProperty(String tag, HashMap<String, ImportEnt> hashEntities) {
        if (input.getTag().equals(tag) && input.getValue().startsWith("@") && input.getValue().endsWith("@")) {
            String value = input.getValue();
            value = value.substring(1, value.length() - 1);
            if (!hashEntities.containsKey(value)) {
                hashEntities.put(value, new ImportEnt());
            }
        }
    }


    /**
     * Adds all the sub-tags from propertySrc after the last child property of parentPropertyDest
     * 
     * @param propertySrc
     * @param parentPropertyDest 
     */
    private void movePropertiesRecursively(Property propertySrc, Property parentPropertyDest) {

        if (parentPropertyDest == null) {
            return;
        }
        
        int n = parentPropertyDest.getNoOfProperties();
        Property propertyDest = null;
        try {
            String tag = propertySrc.getTag();
            if (!parentPropertyDest.getMetaProperty().allows(tag)) {
                tag = "_" + tag;
            }
            propertyDest = parentPropertyDest.addProperty(tag, propertySrc.getValue(), n);  // add to the end
        } catch (GedcomException ex) {
            //Exceptions.printStackTrace(ex);
        }

        // Continue moving children properties
        for (Property children : propertySrc.getProperties()) {
            movePropertiesRecursively(children, propertyDest);
        }

        // Remove src property
        propertySrc.getParent().delProperty(propertySrc);

    }

    
    /**
     * Calculates place format length based on all place sizes found
     * @param freq : if true, length will be the maximum frequency length, otherwise the longuest place size
     * @return 
     */
    private String getPlaceFormat(boolean freq) {
        String place_format = "";
        int place_format_length = 0;
        int max = 0;
        for (int i = 0 ; i < PLACE_MAX_LENGTH ; i++) {
            if (freq && place_format_sizes[i] > max) {
                place_format_length = i;
                max = place_format_sizes[i];
            }
            if (!freq && place_format_sizes[i] > 0) {
                place_format_length = i;
            }
        }
        if (place_format_length == 0) {
            place_format = GedcomOptions.getInstance().getPlaceFormat();
            return place_format;
        }
        
        place_format = "";
        for (int i = 1; i <= place_format_length; i++) {
            place_format += "jur" + i;
            if (i != place_format_length) {
                place_format += PropertyPlace.JURISDICTION_SEPARATOR;
            }
        }
        return place_format;
    }


    private class ImportEnt {
        protected boolean seen = false;
    }

    
    
    ////////////////////////////////////////////////////////////////////////////
    //                         ACCESSORS                                      //
    ////////////////////////////////////////////////////////////////////////////
    
    public int getIndisNb() {
        return hashIndis.size();
    }
    public int getFamsNb() {
        return hashFams.size();
    }
    public int getNotesNb() {
        return hashNotes.size();
    }
    
    public int getSoursNb() {
        return hashSours.size();
    }
    
    public int getObjesNb() {
        return hashObjes.size();
    }
    
    public int getReposNb() {
        return hashRepos.size();
    }
    
    public int getSubmsNb() {
        return hashSubms.size();
    }
    
    public int getChangesNb() {
        return nbChanges;
    }
    
    public void showDetails() {
        console.show();
        return;
    }
    
    
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    //                         FILE READING TOOLS                             //
    ////////////////////////////////////////////////////////////////////////////
    
    public String getEOL(File input) {

        String eolMark = System.getProperty("line.separator");

        try {
            FileReader fr = new FileReader(input);
            char[] buffer = new char[200];
            fr.read(buffer);

            String line = new String(buffer);
            if (line.contains("\r\n")) {
                eolMark = "\r\n";
            } else if (line.contains("\n")) {
                eolMark = "\n";
            } else if (line.contains("\r")) {
                eolMark = "\r";
            }
        } catch (IOException e) {
        }

        return eolMark;
    }
    

}
