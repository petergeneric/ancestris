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
import ancestris.gedcom.GedcomDirectory;
import ancestris.gedcom.GedcomMgr;
import ancestris.modules.console.Console;
import ancestris.util.ProgressListener;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomOptions;
import genj.gedcom.Grammar;
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
import genj.util.Origin;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import org.apache.commons.lang.StringUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import spin.Spin;

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
public abstract class Import implements ImportRunner {

    private final static Logger LOG = Logger.getLogger("ancestris.app", null);

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
    protected static final String GEDCOM551_TAG = "|EMAIL|FAX|FACT|FONE|ROMN|WWW|MAP|LATI|LONG|ADR3|";

    protected static Pattern tag55_valid = Pattern.compile("(" + GEDCOM55_TAG + ")");
    protected static Pattern tag551_valid = Pattern.compile("(" + GEDCOM55_TAG + GEDCOM551_TAG + ")");

    protected static final String typerepo = "REPO";  // Debut de la cle REPO dans le gedcom

    // Invalid gedcom lines
    protected int parentLevel = 0;
    protected String parentLevelTag = "";
    protected static final Pattern ALLOW_CONT_TAGS = Pattern.compile("(NOTE|TEXT|AUTH|TITL|PUBL|DSCR|SOUR)");
    protected int levelBeingRepaired = 100;
    
    
    // Header
    private boolean headerzone = false;
    private boolean grammarZone = false;

    private boolean noteFound = false;
    private boolean headNoteWritten = false;

    // Destination
    private final static int TAG_MISSING = 0;
    private final static int TAG_INVALID = 1;
    private final static int TAG_VALID = 2;
    private int destination_found = TAG_MISSING;

    // Place format
    private final static int PLACE_MAX_LENGTH = 20;
    private int place_found = TAG_MISSING;
    private final Integer[] place_format_sizes = new Integer[PLACE_MAX_LENGTH];

    // Lists of entities
    private static HashMap<String, ImportEnt> hashIndis;
    private static HashMap<String, ImportEnt> hashFams;
    private static HashMap<String, ImportEnt> hashNotes;
    private static HashMap<String, ImportEnt> hashObjes;
    private static HashMap<String, ImportEnt> hashSours;
    private static HashMap<String, ImportEnt> hashRepos;
    private static HashMap<String, ImportEnt> hashSubms;
    // Get Ids related to Assossiations
    private static HashMap<String, ImportEnt> hashAssos;

    /**
     * our files
     */
    private String tabName = "";
    public GedcomFileReader input;
    public GedcomFileWriter output;
    protected Console console;
    public int nbChanges = 0;

    /**
     * Process
     */
    private boolean cancelProcess = false;
    private final static int READLINES = 0, CONVERTLINES = 1, CONVERTGEDCOM = 2;
    private int nbOfFileLines = 0;
    private int state = 0;
    private int progress;
    private boolean cancel = false;

    /**
     * Constructor
     */
    public Import() {
    }

    /**
     * Gives back import name
     */
    @Override
    public abstract String toString();

    /**
     * Identifies generic module
     *
     * @return trus if generic
     */
    public abstract boolean isGeneric();

    /**
     * Gives back output file name
     *
     * @return Import Comment
     */
    protected abstract String getImportComment();

    public void setTabName(String tabName) {
        console = new Console(tabName);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Overall launch panel and process
    //
    public void launch(File inputFile, File outputFile) {
        cancel = false;
        cancelProcess = false;
        ImportPanel importPanel = new ImportPanel(new Callable() {
            @Override
            public Object call() throws Exception {
                cancelProcess();
                return null;
            }
        });
        String title = NbBundle.getMessage(Import.class, "Import.progress.importing", inputFile.getName());
        DialogManager dialog = DialogManager.create(title, importPanel, false).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(new Object[]{}).setResizable(false);
        dialog.show();

        // Prepare console window
        setTabName(NbBundle.getMessage(Import.class, "OpenIDE-Module-Name") + " - " + toString());

        // Fix header and lines
        ImportRunner importTask = (ImportRunner) Spin.off(ImportFactory.createImport(this));
        ProgressListener.Dispatcher.processStarted(importTask);
        boolean taskOk = importTask.run(inputFile, outputFile);
        ProgressListener.Dispatcher.processStopped(importTask);
        if (cancelProcess || !taskOk) {
            dialog.cancel();
            return;
        }
        importPanel.increment();

        // Open Gedcom normally. Avoid potential error message by being quiet.
        GedcomMgr.getDefault().setQuiet(true);
        Context context = GedcomDirectory.getDefault().openAncestrisGedcom(FileUtil.toFileObject(outputFile));
        GedcomMgr.getDefault().setQuiet(false);
        if (cancelProcess || context == null) {
            dialog.cancel();
            return;
        }
        importPanel.increment();

        // Fix gedcom (it will need to be reopen afterwards to take into account all modifications, therefore it needs to be saved)
        final Gedcom importedGedcom = context.getGedcom();
        importedGedcom.setName(inputFile.getName());
        ProgressListener.Dispatcher.processStarted(importTask);
        importTask.fixGedcom(importedGedcom);
        importTask.complete();
        ProgressListener.Dispatcher.processStopped(importTask);
        importPanel.increment();
        if (cancelProcess) {
            dialog.cancel();
            return;
        }

        // Save gedcom as new name
        Origin o = GedcomMgr.getDefault().saveGedcomAs(context, null, FileUtil.toFileObject(outputFile));
        GedcomDirectory.getDefault().closeGedcom(context);
        if (cancelProcess || o == null) {
            dialog.cancel();
            return;
        }
        importPanel.increment();

        // Reopen it
        GedcomDirectory.getDefault().openAncestrisGedcom(FileUtil.toFileObject(o.getFile()));
        if (cancelProcess) {
            dialog.cancel();
            return;
        }
        importPanel.requestFocusInWindow();
        importPanel.increment();

        // Ask user if he wants to see conversion stats
        dialog.cancel();
        Object rc = DialogManager.create(NbBundle.getMessage(Import.class, "Import.completed"),
                NbBundle.getMessage(Import.class, "cc.importResults?", inputFile.getName(), toString(),
                        getIndisNb(), getFamsNb(), getNotesNb(), getObjesNb(),
                        getSoursNb(), getReposNb(), getSubmsNb(), getChangesNb()))
                .setMessageType(DialogManager.INFORMATION_MESSAGE).setOptionType(DialogManager.YES_NO_OPTION).show();
        if (rc == DialogManager.YES_OPTION) {
            showDetails();
        }

    }

    public void cancelProcess() {
        ProgressListener.Dispatcher.processStopAll();
        cancelProcess = true;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Trackable methods
    //
    @Override
    public void cancelTrackable() {
        cancel = true;
    }

    @Override
    public int getProgress() {

        switch (state) {
            case READLINES:
            case CONVERTLINES:
                if (nbOfFileLines == 0 || input == null) {
                    return 1;
                }
                progress = (state * 50 * nbOfFileLines + input.getLines()) / nbOfFileLines;
                return progress;

            case CONVERTGEDCOM:
                return progress;
            default:
                return 1;
        }
    }

    /**
     * Returns current read state as explanatory string
     *
     * @return
     */
    @Override
    public String getState() {
        switch (state) {
            case READLINES:
                return NbBundle.getMessage(Import.class, "Import.progress.readlines", tabName);
            case CONVERTLINES:
                return NbBundle.getMessage(Import.class, "Import.progress.convertlines", tabName);
            case CONVERTGEDCOM:
            default:
                return NbBundle.getMessage(Import.class, "Import.progress.convertgedcom", tabName);
        }
    }

    @Override
    public String getTaskName() {
        return NbBundle.getMessage(Import.class, "Import.progress.importing", tabName);
    }

    public void incrementProgress() {
        progress += 10;
        if (progress > 100) {
            progress = 90;
        }
    }
    ////////////////////////////////////////////////////////////////////////////

    /**
     * This runs the first 3 steps of the import process. This method is a file
     * filter.
     *
     * @param fileIn Gedcom to import
     * @param fileOut Temporary Gedcom file created
     * @return true if conversion is successful
     */
    @Override
    public boolean run(File fileIn, File fileOut) {
        this.tabName = fileIn.getName();
        init();

        // Get nb of file lines of file
        state = READLINES;
        try {
            FileReader in = new FileReader(fileIn);
            LineNumberReader count = new LineNumberReader(in);
            while (count.skip(Long.MAX_VALUE) > 0) {
                // Loop just in case the file is > Long.MAX_VALUE or skip() decides to not read the entire file
            }
            nbOfFileLines = (count.getLineNumber() + 1) / 50;   // 2 (we will read twice) * nblines / 100  (percent)
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Error during File Reading.", ex);
        }

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
                while (!cancel && (input.getRawLine()) != null) {
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
            String l = "" + e.getLine();
            JOptionPane.showMessageDialog(null, e.getMessage() + "\n" + NbBundle.getMessage(Import.class, "error.line", l));
            //Exceptions.printStackTrace(e);
            return false;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, NbBundle.getMessage(Import.class, "error.other", e.getMessage()));
            LOG.log(Level.INFO, "Error during new file creation.", e);
            return false;
        }

        // maintenant on effectue toutes les transformations
        state++;

        try {
            output = new GedcomFileWriter(fileOut, input.getCharset(), getEOL(fileIn));
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(null, NbBundle.getMessage(Import.class, "file.create.error", fileOut.getName()));
            LOG.log(Level.INFO, "Error during new file creation.", e1);
            return false;
        }
        try {
            console.println("=============================");
            console.println(NbBundle.getMessage(Import.class, "Import.starting"));
            console.println(NbBundle.getMessage(Import.class, "Import.version", GEDCOM_VERSION));
            console.println("=============================");
            input = GedcomFileReader.create(fileIn);
            try {
                while (!cancel && input.getRawLine() != null) {
                    if ((input.getLevel() == 0) && (input.getTag().equals("HEAD"))) {
                        output.writeLine(input);
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
            String l = "" + e.getLine();
            JOptionPane.showMessageDialog(null, e.getMessage() + "\n" + NbBundle.getMessage(Import.class, "error.line", l));
            //Exceptions.printStackTrace(e);
            return false;
        } catch (IOException | MissingResourceException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            LOG.log(Level.INFO, "Error during new file reading.", e);
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
        nbChanges = 0;
        headerzone = false;
        destination_found = TAG_MISSING;
        place_found = TAG_MISSING;
        for (int i = 0; i < PLACE_MAX_LENGTH; i++) {
            place_format_sizes[i] = 0;
        }
        hashIndis = new HashMap<>();
        hashFams = new HashMap<>();
        hashNotes = new HashMap<>();
        hashObjes = new HashMap<>();
        hashSours = new HashMap<>();
        hashRepos = new HashMap<>();
        hashSubms = new HashMap<>();
        hashAssos = new HashMap<>();

        parentLevel = 0;
        parentLevelTag = "";
        levelBeingRepaired = 100;
}

    /**
     * *** 1 *** This is the first step of import process. The file is analysed line by line. Purpose is to : 
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

        if (headerzone && (input.getLevel() == 1) && "GEDC".equals(input.getTag())) {
            grammarZone = true;
        }

        if (headerzone && (input.getLevel() == 1) && "SOUR".equals(input.getTag())) {
            grammarZone = false;
        }

        if (headerzone && (input.getLevel() == 1) && "CHAR".equals(input.getTag())) {
            grammarZone = false;
        }

        // Get gedcom version (default is 5.5 defined in declaration parameters)
        if (grammarZone && headerzone && (input.getLevel() == 2) && input.getTag().equals("VERS")) {
            GEDCOM_VERSION = input.getValue();
        }

        // Determine existing note
        if (headerzone && "NOTE".equals(input.getTag())) {
            noteFound = true;
        }

        // Determine destination type : missing or invalid or correct
        if ((input.getLevel() == 1) && input.getTag().equals("DEST")) {
            String value = input.getValue();
            // Add ANY as valid value : ancestris set this value, 
            // a file created by ancestris and imported will create a modification
            // and define this tag as invalid.....
            if (value != null && (value.equals("ANSTFILE") || value.equals("TempleReady") || value.equals("ANY"))) {
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
                place_format_sizes[length]++;
            }
        }

        // Remember all entities IDs
        memorizeEntities();
    }

    /**
     * *** 2 *** This is the second step of the import process. The file is
     * fixed line by line "on the fly". Purpose is to : - fix all main grammar
     * errors (yes tags, invalid tags) that can be fixed on the fly
     */
    protected boolean process() throws IOException {

        if (repairNonGedcomLines()) {
            return true;
        }
        
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
        if (processInvalidAges()) {
            return true;
        }
        return false;
    }

    /**
     * *** 3 *** This is the third step of the import process. Neccessary lines
     * are added at the end before the last TRLR line is written. Purpose is to
     * : - fix all main grammar errors (yes tags, invalid tags) that can be
     * fixed on the fly
     */
    protected void finalise() throws IOException {
        checkAssociationEntities();
        addMissingEntities();
        state = CONVERTGEDCOM;
        progress = 1;
    }

    /**
     * *** 4 *** This is the fourth step of the import process. The gedcom file
     * generated in step one as been loaded in memory and can be manipulated
     * using all ancestris core functionnalities. . This is called only after
     * first 3 steps are fine.
     *
     * @param gedcom
     * @return
     */
    @Override
    public boolean fixGedcom(Gedcom gedcom) {
        boolean ret = fixNames(gedcom);
        ret |= fixPlaces(gedcom);
        return ret;
    }

    /**
     * *** 5 *** This is the fifth and last step of the import process. The
     * gedcom has fully been repaired. This is mainly to let the opportunity to
     * conclude and write an end message. This is called from the import method.
     */
    @Override
    public void complete() {
        progress = 100;
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
        memorizeAssociation();

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

    /**
     * Add Entities whom ids where found in asso link but not found in Indis or
     * Fams.
     */
    private void checkAssociationEntities() {
        for (String k : hashAssos.keySet()) {
            // Check if asso is related ot fam or indi. If id not found assume it's an indi.
            if (!hashIndis.containsKey(k) && !hashFams.containsKey(k)) {
                hashIndis.put(k, new ImportEnt());
            }
        }
    }

    private void addMissingEntities() throws IOException {
        for (String k : hashIndis.keySet()) {
            if (!hashIndis.get(k).seen) {
                output.writeLine(0, k, "INDI", null);
                console.println(NbBundle.getMessage(Import.class, "Import.addMissingEntity", k));
                nbChanges++;
            }
        }
        for (String k : hashFams.keySet()) {
            if (!hashFams.get(k).seen) {
                output.writeLine(0, k, "FAM", null);
                console.println(NbBundle.getMessage(Import.class, "Import.addMissingEntity", k));
                nbChanges++;
            }
        }
        for (String k : hashNotes.keySet()) {
            if (!hashNotes.get(k).seen) {
                output.writeLine(0, k, "NOTE", null);
                console.println(NbBundle.getMessage(Import.class, "Import.addMissingEntity", k));
                nbChanges++;
            }
        }
        for (String k : hashObjes.keySet()) {
            if (!hashObjes.get(k).seen) {
                output.writeLine(0, k, "OBJE", null);
                console.println(NbBundle.getMessage(Import.class, "Import.addMissingEntity", k));
                nbChanges++;
            }
        }
        for (String k : hashSours.keySet()) {
            if (!hashSours.get(k).seen) {
                output.writeLine(0, k, "SOUR", null);
                console.println(NbBundle.getMessage(Import.class, "Import.addMissingEntity", k));
                nbChanges++;
            }
        }
        for (String k : hashRepos.keySet()) {
            if (!hashRepos.get(k).seen) {
                output.writeLine(0, k, "REPO", null);
                console.println(NbBundle.getMessage(Import.class, "Import.addMissingEntity", k));
                nbChanges++;
            }
        }
        for (String k : hashSubms.keySet()) {
            if (!hashSubms.get(k).seen) {
                output.writeLine(0, k, "SUBM", null);
                console.println(NbBundle.getMessage(Import.class, "Import.addMissingEntity", k));
                nbChanges++;
            }
        }

    }

    /**
     * Repair non gedcom lines (2021-02-02 : FL)
     * 
     * Example : NOTEs coming out of MyHeritage obviously do not have the "n CONT " for line returns ! So build them back
     * 
     * If a line is not a gedcom line, and follows a tag that allows CONT, then assume it is CONT
     * - After NOTE, TEXT, AUTH, TITL, PUBL, DSCR, SOUR : repair lines by ensuring "n CONT" upfront and injecting it if absent
     * 
     * If a line is not a gedcom line and does not follow one of the above tags, assume it is a NOTE line
     * 
     * A line is not a gedcom line if not in the format "n TAG [@ID@] Value" 
     * - where n follows the level above and TAG in capital letter with 3 or 4 letters, or else starting with underscore
     * (in case free text did include something that looks like a gedcom line : example: "9 children" looks like a gedcom line but is not !)
     * 
     * 
     * 
     */
    private boolean repairNonGedcomLines() throws IOException {
        
        int input_level = input.getLevel();
        String input_tag = input.getTag();
        
        // Check if line is invalid
        boolean invalidLine = false;
        
        if (input_level == -1) {
            invalidLine = true;
        } else {
            String input_xref = input.getXref();
            TagPath input_path = input.getPath();
            if (input_tag == null || input_tag.isEmpty() || input_xref == null || input_path == null) {
                invalidLine = true;
            } else {
                if (input_tag.startsWith("_")) {
                    if (!input_tag.substring(1).replaceAll("_", "").matches("[A-Z0-9]+")) {
                        invalidLine = true;
                    }
                } else {
                    if (input_tag.length() < 3 || input_tag.length() > 5 || !input_tag.replaceAll("_", "").matches("[A-Z0-9]+")) {
                        invalidLine = true;
                    }
                }
            }

        }
        
        // If the line is invalid, fix it !
        // - if it follows a "n" parent level which allows CONT, then fix it with "n+1 CONT ", 
        // - otherwise fix it with "n+1 NOTE "
        if (invalidLine) {
            String result;
            levelBeingRepaired = parentLevel + 1;
            if (ALLOW_CONT_TAGS.matcher(parentLevelTag).matches()) {
                result = output.writeLine(levelBeingRepaired, "CONT", input.getLine());
            } else {
                levelBeingRepaired = parentLevel;
                if (!input.getLine().trim().isEmpty()) {
                    result = output.writeLine(levelBeingRepaired, "NOTE", input.getLine());
                } else {
                    result = NbBundle.getMessage(Import.class, "Import.ignoringIsolatedEmptyLine");
                }
            }
            nbChanges++;
            console.println(NbBundle.getMessage(Import.class, "Import.repairLine", input.getLines() + " ==> " + result));
            return true;
        } else {
        // If line is valid, we always memorize it and reset the repairing level, except if current level is ge repairinglevel and if the tag is CONC or CONT
        // Example : a 2 CONC line following a line to be repaired : we should not memorize
            if (!input_tag.equals("CONC") && !input_tag.equals("CONT") && input_level < levelBeingRepaired) {
                parentLevel = input_level;
                parentLevelTag = input_tag;
                levelBeingRepaired = 100;
            }
        }

        return false;
        
    }

    
    
    
    public boolean processHeader() throws IOException {

        // HEAD:NOTE management GEDCOM gives only one NOTE in header.
        if (!headNoteWritten) {
            if (noteFound) {
                if ("NOTE".equals(input.getTag())) {
                    output.writeLine(1, "NOTE", getImportComment());
                    console.println(NbBundle.getMessage(Import.class, "Import.header"));
                    nbChanges++;
                    headNoteWritten = true;
                    output.writeLine(2, "CONT", input.getValue());
                    // Line written, next one.
                    return true;
                }
            } else {
                // Write the note
                output.writeLine(1, "NOTE", getImportComment());
                console.println(NbBundle.getMessage(Import.class, "Import.header"));
                nbChanges++;
                headNoteWritten = true;
            }
        }

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
                console.println(NbBundle.getMessage(Import.class, "Import.fixPlaceTag"));
                nbChanges++;
                return true;
            }
            if ((input.getLevel() == 0) && (!input.getTag().equals("HEAD"))) {   // case of PLAC tag missing and next entity reached
                output.writeLine(1, "PLAC", "");
                output.writeLine(2, "FORM", getPlaceFormat(false));
                output.writeLine(input.getLevel(), input.getXref(), input.getTag(), input.getValue());
                place_found = TAG_VALID;
                console.println(NbBundle.getMessage(Import.class, "Import.fixPlaceTag"));
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
                console.println(NbBundle.getMessage(Import.class, "Import.fixPlaceTag"));
                nbChanges++;
                return true;
            }
        }

        return false;
    }

    /**
     * Normallize YES_TAGS. This is called at any import. Convert all "YES_TAGS"
     * (eg BIRT, EVEN, ...) where value is not null and different from "Y" from
     * <pre>
     * n TAG value</pre> to
     * <pre>
     * n TAG
     * n+1 NOTE value</pre>
     *
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
                if (input.getValue().equalsIgnoreCase("y")) {
                    output.writeLine(input);
                } else {
                    result = output.writeLine(level, tag, null);
                    result += "\n" + output.writeLine(level + 1, "NOTE", input.getValue());
                }
            } else {
                String temp = input.getNextLine(false);
                if ((temp != null) && (input.getLevel() == level + 1)) {
                    output.writeLine(level, tag, null);
                } else {
                    result = output.writeLine(level, tag, "Y");
                }
            }
            if (result != null) {
                nbChanges++;
                console.println(NbBundle.getMessage(Import.class, "Import.fixYesTag", line + " ==> " + result));
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Fix invalid tags and prefix them with "_". This is called at any import.
     *
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
     * Normallize DATE tags. This is called at any import. Ensure dates are
     * formattedd as .. month year
     *
     * @return
     * @throws IOException
     */
    public boolean processInvalidDates() throws IOException {
        if ("DATE".equals(input.getTag())) {
            String date = input.getValue();
            if (date.contains("/")) {
                date = convertDate(date);
                String result = output.writeLine(input.getLevel(), "DATE", date);
                console.println(NbBundle.getMessage(Import.class, "Import.fixInvalidValue", input.getLine() + " ==> " + result));
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
     * Normallize AGE tags. This is called at any import. Ensure dates are
     * formattedd as "nn y"
     *
     * @return
     * @throws IOException
     */
    public boolean processInvalidAges() throws IOException {
        if ("AGE".equals(input.getTag())) {
            String age = input.getValue();
            
            if (StringUtils.isNumeric(age)) {
                age = age + "y";
                String result = output.writeLine(input.getLevel(), "AGE", age);
                console.println(NbBundle.getMessage(Import.class, "Import.fixInvalidValue", input.getLine() + " ==> " + result));
                nbChanges++;
                return true;
            }
        }

        return false;
    }

    /**
     * Fix names. This is called at any import. Makes sure that the NAME tag has
     * the properly constructed string from the provided subtags If not,
     * replaces NAME string and return false.
     *
     * @param gedcom
     * @return
     */
    public boolean fixNames(Gedcom gedcom) {

        boolean hasErrors = false;

        console.println(NbBundle.getMessage(Import.class, "Import.fixNames"));

        Collection<Indi> indis = gedcom.getIndis();
        int increment = indis.size() / 10 + 1;
        int counter = 0;
        for (Indi indi : indis) {
            // increment progress
            counter++;
            if (counter % increment == 0 && progress < 100) {
                progress++;
            }

            final Property rawName = indi.getProperty("NAME", false);
            if (rawName instanceof PropertyName) {
                final PropertyName propName = (PropertyName) rawName;

                // If name is invalid, replace it
                if (!propName.isValid() || propName.hasWarning()) {
                    propName.fixNameValue();
                    hasErrors = true;
                    console.println(NbBundle.getMessage(Import.class, "Import.fixInvalidValue", propName.getDisplayValue()));
                    nbChanges++;
                }
            }
        }

        console.println("=============================");

        return hasErrors;
    }

    /**
     * Fix places. This is called at any import. Makes sure that the PLAC tag
     * has the properly constructed string with the proper number of
     * jurisdictions
     *
     * @param gedcom
     * @return
     */
    public boolean fixPlaces(Gedcom gedcom) {

        boolean hasErrors = false;
        String[] locs = null;

        console.println(NbBundle.getMessage(Import.class, "Import.fixPlaces"));

        int nbLocs = PropertyPlace.getFormat(gedcom.getPlaceFormat()).length;
        List<PropertyPlace> places = (List<PropertyPlace>) gedcom.getPropertiesByClass(PropertyPlace.class);
        int increment = places.size() / 10 + 1;
        int counter = 0;
        for (PropertyPlace place : places) {
            // increment progress
            counter++;
            if (counter % increment == 0 && progress < 100) {
                progress++;
            }

            locs = place.getJurisdictions();
            // If nb of jurisdictions of correct length, set it and return true
            if (locs.length == nbLocs) {
                continue;
            }
            if (!place.setJurisdictions(gedcom, locs)) {
                hasErrors = true;
                console.println(NbBundle.getMessage(Import.class, "Import.fixInvalidValue", place.getDisplayValue()));
                nbChanges++;
            }
        }

        console.println("=============================");
        return hasErrors;
    }

    /**
     * ConvertAssociations. This is called only for specific imports. The way
     * associations work in ANCESTRIS is the following: If individual A is the
     * "relation" of B, then in A, we should have 1 ASSO B with RELA = relation
     * Example : if A is oncle of B, we should have in A the "1 ASSO @B@" tag
     * with "2 RELA Oncle" In gedcoms files from other software, it seems that :
     * - The 1 ASSO tags are in the same direction as Ancestris - But the 2 ASSO
     * tags underneath events of A are the other way around and should be
     * reversed to be put under individual B. So if A has got 2 ASSO @B@ ; 3
     * RELA Notary, the notary is actually B so then we need to go to individual
     * B, write 1 ASSO @A@ 2 RELA Notary
     *
     * The idea of this method is therefore to get all ASSOs tags that are level
     * 2 or more (PropertySimpleValue), and leave 1 ASSO as is
     * (PropertyAssociation)
     *
     * @param gedcom
     * @return
     */
    public boolean convertAssociations(Gedcom gedcom) {

        console.println(NbBundle.getMessage(Import.class, "Import.convertingAssos"));

        List<Property> list = new ArrayList<>();
        gedcom.getIndis().forEach((entity) -> {
            getPropertiesRecursively(list, "ASSO", entity);
        });
        gedcom.getFamilies().forEach((entity) -> {
            getPropertiesRecursively(list, "ASSO", entity);
        });
        final boolean isV55 = Grammar.V55.equals(gedcom.getGrammar());
        int increment = list.size() / 10 + 1;
        int counter = 0;
        for (Property prop : list) {
            // increment progress
            counter++;
            if (counter % increment == 0 && progress < 100) {
                progress++;
            }

            // Skip PropertyAssociation
            if (prop instanceof PropertyAssociation) {
                continue;
            }

            // Get initial individual B
            String id = prop.getValue().replace("@", "").trim();
            final Indi indiRela = (Indi) gedcom.getEntity(id);  // This will be the new individual A
            if (indiRela == null) {
                console.println(NbBundle.getMessage(Import.class, "Import.indiNotFound", id));
                continue;
            }

            // Get type, rela and tagpath
            final String type = prop.getEntity().getTag();
            final Property relaProp = prop.getProperty("RELA");
            String rela = "";
            if (relaProp != null) {
                rela = relaProp.getDisplayValue();
            }
            final TagPath tagpath = prop.getParent().getPath(true);

            // Create asso set in B
            id = prop.getEntity().getId();  // id of A
            final PropertyAssociation propAsso = (PropertyAssociation) indiRela.addProperty("ASSO", "@" + id + "@");  // add 1 ASSO @A@
            if (isV55) {
                propAsso.addProperty("TYPE", type);
            }
            final PropertyRelationship pship = (PropertyRelationship) propAsso.getProperty("RELA", false);
            rela += "@" + tagpath.toString();
            if (pship == null) {
                propAsso.addProperty("RELA", rela);
            } else {
                pship.setValue(rela);
            }

            // Add other sub-tags of A:ASSO to the new B:ASSO => copy prop/subtags to propAsso/ except RELA and TYPE
            for (Property child : prop.getProperties()) {
                if (child.getTag().equals("RELA")) {
                    continue;
                }
                if (child.getTag().equals("TYPE")) {
                    if (isV55) {
                        continue;
                    }

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
            console.println(NbBundle.getMessage(Import.class, "Import.adjustAssos", id));
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
        // if begin with 2@ this is an escape and note a key
        if (input.getTag().equals(tag) && input.getValue().startsWith("@") && input.getValue().endsWith("@") && !input.getValue().startsWith("@@")) {
            String value = input.getValue();
            value = value.substring(1, value.length() - 1);
            if (!hashEntities.containsKey(value)) {
                hashEntities.put(value, new ImportEnt());
            }
        }
    }

    private void memorizeAssociation() {
        // if begin with 2@ this is an escape and note a key
        if (input.getTag().equals("ASSO") && input.getValue().startsWith("@") && input.getValue().endsWith("@") && !input.getValue().startsWith("@@")) {
            String value = input.getValue();
            value = value.substring(1, value.length() - 1);
            if (!hashAssos.containsKey(value)) {
                hashAssos.put(value, new ImportEnt());
            }
        }
    }

    public void reduceEvents(Entity entity, String tag) {
        int n = 0;
        Property[] props = entity.getProperties(tag);
        for (Property event : props) {
            if (n == 0) {
                n++;
                continue;
            }
            Property host = entity.addProperty("EVEN", "");
            for (Property p : event.getProperties()) {
                movePropertiesRecursively(p, host);
            }
            console.println(NbBundle.getMessage(Import.class, "Import.reduceEvents", event.getTag() + " : " + entity.getId()));
            nbChanges++;
            entity.delProperty(event);
        }
    }

    /**
     * Adds all the sub-tags from propertySrc after the last child property of
     * parentPropertyDest
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
     *
     * @param freq : if true, length will be the maximum frequency length,
     * otherwise the longuest place size
     * @return
     */
    private String getPlaceFormat(boolean freq) {
        int place_format_length = 0;
        int max = 0;
        for (int i = 0; i < PLACE_MAX_LENGTH; i++) {
            if (freq && place_format_sizes[i] > max) {
                place_format_length = i;
                max = place_format_sizes[i];
            }
            if (!freq && place_format_sizes[i] > 0) {
                place_format_length = i;
            }
        }
        if (place_format_length == 0) {
            return GedcomOptions.getInstance().getPlaceFormat();
        }

        String place_format = "";
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
