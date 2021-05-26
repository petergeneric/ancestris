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
import static ancestris.util.swing.FileChooserBuilder.getExtension;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomOptions;
import genj.gedcom.Grammar;
import genj.gedcom.Indi;
import genj.gedcom.Media;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyAssociation;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyMedia;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyNote;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertyRelationship;
import genj.gedcom.PropertyRepository;
import genj.gedcom.PropertySource;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import genj.io.GedcomFileReader;
import genj.io.GedcomFileWriter;
import genj.io.GedcomFormatException;
import genj.io.InputSource;
import genj.io.input.URLInput;
import genj.util.Origin;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
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

    public final static Logger LOG = Logger.getLogger("ancestris.app", null);

    protected static String EOL = System.getProperty("line.separator");

    protected static final String INDI_TAG_YES = "BIRT|CHR|DEAT|BURI|CREM|"
            + "ADOP|BAPM|BARM|BASM|BLES|CHRA|CONF|FCOM|ORDN|NATU|EMIG|IMMI|"
            + "CENS|PROB|WILL|GRAD|RETI|";
    protected static final String FAM_TAG_YES = "ANUL|CENS|DIV|DIVF|"
            + "ENGA|MARR|MARB|MARC|MARL|MARS";
    protected static Pattern tag_y = Pattern.compile("(" + INDI_TAG_YES + FAM_TAG_YES + ")");

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
    private boolean revertAsso = false;
    
    
    // Header
    private boolean headerzone = false;
    private boolean grammarZone = false;
    private boolean noteFound = false;
    private boolean headNoteWritten = false;
    public String GEDCOM_VERSION = "5.5.1";
    private String software_name = "";
    private String software_vers = "";

    // Destination
    private final static int TAG_MISSING = 0;
    private final static int TAG_INVALID = 1;
    private final static int TAG_VALID = 2;
    private int destination_found = TAG_MISSING;

    // Place format
    private final static int PLACE_MAX_LENGTH = 20;
    private int place_found = TAG_MISSING;
    private int char_found = TAG_INVALID;
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
    public HashMap<String, String> summary = new HashMap<>();
    public String currentXref = "";
    public String previousXref = "";
    public TagPath currentPath = null;
    public TagPath previousPath = null;
    public List<ImportFix> fixes = new ArrayList<>();

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
     * Common variables
     */
    public Set<String> invalidPaths;
    
    
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


    ////////////////////////////////////////////////////////////////////////////
    // Overall launch panel and process
    //
    public void launch(File inputFile, File outputFile) {
        cancel = false;
        cancelProcess = false;
        fixes.clear();

        // 2021-05-12 FL : Turn off automatic lastname uppercase! Otherwise fixing name elements could change lastnames to uppercase against user's will
        final Boolean previousUppercaseFlag = GedcomOptions.getInstance().isUpperCaseNames();
        GedcomOptions.getInstance().setUpperCaseNames(false);


        ImportPanel importPanel = new ImportPanel(new Callable() {
            @Override
            public Object call() throws Exception {
                cancelProcess();
                GedcomOptions.getInstance().setUpperCaseNames(previousUppercaseFlag);
                return null;
            }
        });
        String title = NbBundle.getMessage(Import.class, "Import.progress.importing", inputFile.getName());
        DialogManager dialog = DialogManager.create(title, importPanel, false).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(new Object[]{}).setResizable(false);
        dialog.show();

        // Fix header and lines
        ImportRunner importTask = (ImportRunner) Spin.off(ImportFactory.createImport(this));
        ProgressListener.Dispatcher.processStarted(importTask);
        boolean taskOk = importTask.run(inputFile, outputFile);
        ProgressListener.Dispatcher.processStopped(importTask);
        if (cancelProcess || !taskOk) {
            dialog.cancel();
            GedcomOptions.getInstance().setUpperCaseNames(previousUppercaseFlag);
            return;
        }
        importPanel.increment();

        // Open Gedcom normally. Avoid potential error message by being quiet.
        GedcomMgr.getDefault().setQuiet(true);
        Context context = GedcomDirectory.getDefault().openAncestrisGedcom(FileUtil.toFileObject(outputFile));
        GedcomMgr.getDefault().setQuiet(false);
        if (cancelProcess || context == null) {
            closeProcess(dialog, previousUppercaseFlag);
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
            closeProcess(dialog, previousUppercaseFlag);
            return;
        }

        // Save gedcom as new name
        Origin o = GedcomMgr.getDefault().saveGedcomAs(context, null, FileUtil.toFileObject(outputFile));
        GedcomDirectory.getDefault().closeGedcom(context);
        if (cancelProcess || o == null) {
            closeProcess(dialog, previousUppercaseFlag);
            return;
        }
        importPanel.increment();

        // Reopen it
        Context newContext = GedcomDirectory.getDefault().openAncestrisGedcom(FileUtil.toFileObject(o.getFile()));
        if (cancelProcess) {
            closeProcess(dialog, previousUppercaseFlag);
            return;
        }
        importPanel.requestFocusInWindow();
        importPanel.increment();
        closeProcess(dialog, previousUppercaseFlag);

        // Ask user if he wants to see conversion stats, and adapt buttons depending on number of issues to display.
        // Over 15 000, it becomes difficult to display all.
        Object[] buttons;
        JButton buttonFull = new JButton(NbBundle.getMessage(Import.class, "mode.displayFullList"));
        JButton buttonExtract = new JButton(NbBundle.getMessage(Import.class, "mode.displayExtractOnly"));
        buttons = new Object[]{ buttonFull, buttonExtract, DialogManager.NO_OPTION };
        int nbI = newContext.getGedcom().getEntities("INDI").size();
        int nbF = newContext.getGedcom().getEntities("FAM").size();
        int nbN = newContext.getGedcom().getEntities("NOTE").size();
        int nbO = newContext.getGedcom().getEntities("OBJE").size();
        int nbS = newContext.getGedcom().getEntities("SOUR").size();
        int nbR = newContext.getGedcom().getEntities("REPO").size();
        int nbSu = newContext.getGedcom().getEntities("SUBM").size();
        Object rc = DialogManager.create(NbBundle.getMessage(Import.class, "Import.completed"),
                NbBundle.getMessage(Import.class, "cc.importResults?", inputFile.getName(), software_name,
                        nbI, nbF, nbN, nbO, nbS, nbR, nbSu, fixes.size()))
                .setMessageType(DialogManager.INFORMATION_MESSAGE).setOptions(buttons).show();
        if (rc != DialogManager.NO_OPTION) {
            summary.put("a.software", software_name + (software_vers.isEmpty() ? "" : " - " + software_vers));
            summary.put("b.gedcomversion", GEDCOM_VERSION);
            summary.put("c.oldgedcom", inputFile.getName());
            summary.put("e.newgedcom", outputFile.getName());
            summary.put("f.newnumlines", ""+newContext.getGedcom().getLines());
            summary.put("g.nbindis", ""+nbI);
            summary.put("h.nbfams", ""+nbF);
            summary.put("i.nbnotes", ""+nbN);
            summary.put("j.nbobjes", ""+nbO);
            summary.put("k.nbsources", ""+nbS);
            summary.put("l.nbrepos", ""+nbR);
            summary.put("m.nbsubms", ""+nbSu);
            summary.put("n.nbfixes", ""+fixes.size());
            importTask.showDetails(newContext, rc == buttonExtract);
        }

    }

    public void closeProcess(DialogManager dialog, boolean flag) {
        dialog.cancel();
        GedcomOptions.getInstance().setUpperCaseNames(flag);
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
            summary.put("d.oldnumlines", ""+count.getLineNumber());

        } catch (IOException ex) {
            LOG.log(Level.INFO, "Error during File Reading.", ex);
        }

        /////////////////////////////////////////////////////////// 1 /////////////////////////////////////////////////////////////////
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
                if (input != null) { // avoir NPE on close.
                    input.close();
                }
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

        /////////////////////////////////////////////////////////// 2 /////////////////////////////////////////////////////////////////
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
            input = GedcomFileReader.create(fileIn);
            currentXref = "";
            currentPath = null;
            boolean EOFfound = false;
            try {
                while (!cancel && input.getRawLine() != null) {
                    previousPath = currentPath;
                    currentPath = input.getPath();
                    if ((input.getLevel() == 0)) {
                        previousXref = currentXref;
                        currentXref = input.getXref();
                    }
                    if ((input.getLevel() == 0) && (input.getTag().equals("HEAD"))) {
                        currentXref = "HEAD";
                        output.writeLine(input);
                        continue;
                    }
                    if (process()) {
                        continue;
                    }

                    if (input.getTag().equals("TRLR")) {
                        EOFfound = true;
                        finalise();
                        output.writeLine(0, "TRLR", null);
                        continue;
                    }
                    output.writeLine(input);
                }
                if (!EOFfound) {
                    finalise();
                    output.writeLine(0, "TRLR", null);
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
            LOG.log(Level.SEVERE, "Error during new file reading.", e);
            return false;
        }

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
        char_found = TAG_INVALID;
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
        
        invalidPaths = new HashSet<>();
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

        if (headerzone && (input.getPath().toString().equals("HEAD:SOUR") || input.getPath().toString().equals("HEAD:SOUR:NAME"))) {  // overwrite previous SOUR tag if NAME is present
            software_name = input.getValue();
        }
        if (headerzone && (input.getPath().toString().equals("HEAD:SOUR:VERS"))) {
            software_vers = input.getValue();
        }


        if (headerzone && (input.getLevel() == 1) && "CHAR".equals(input.getTag())) {
            if (input.getValue().equals("UTF-8") && input.getValue().equals(input.getCharset().name())) {
                char_found = TAG_VALID; // all is good if everything is aligned to UFT-8, otherwise, message to user to fix.
            } else {
                char_found = TAG_INVALID;
            }
            grammarZone = false;
        }

        // Get gedcom version (default is 5.5 defined in declaration parameters)
        if (grammarZone && headerzone && (input.getLevel() == 2) && input.getTag().equals("VERS")) {
            GEDCOM_VERSION = input.getValue();
            summary.put("b.gedcomversion", GEDCOM_VERSION);
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
                    place_found = TAG_INVALID;  // missing place tag or invalid
                }
            }
        }

        // Memorize all different place ptag lengths
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
     * 
     * All these methods have to be processing distinct tags because if they are fixing the current line, a subsequent process will not happen in this case
     */
    protected boolean process() throws IOException {

        if (repairNonGedcomLines()) { // concatenate to previous TAG or add NOTE
            return true;
        }
        if (processHeader()) {  // header entity
            return true;
        }
        if (processInvalidTag(input.getTag())) {  // non existing tags
            return true;
        }
        if (processInvalidPath(input.getPath())) {  // non existing paths
            return true;
        }
        if (processEventValues(input.getPath())) {  // normal event tags
            return true;
        }
        if (processInvalidDates(input.getValue(), "")) {  // DATE tags
            return true;
        }
        if (processInvalidAges()) {  // AGE tags
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
        gedcom.setEncoding("UTF-8");
        boolean ret = false;
        ret |= fixNames(gedcom);
        incrementProgress();
        ret |= fixPlaces(gedcom);
        incrementProgress();
        ret |= fixEventsCardinality(gedcom);
        incrementProgress();
        ret |= fixObje(gedcom);
        incrementProgress();
        ret |= fixAddr(gedcom);
        incrementProgress();
        ret |= fixSources(gedcom);
        incrementProgress();
        ret |= fixSourceCitations(gedcom);
        incrementProgress();
        ret |= fixNoteCitations(gedcom);
        incrementProgress();
        ret |= fixConcCont(gedcom);
        incrementProgress();
        if (revertAsso) {
            ret |= convertAssociations(gedcom); 
        }
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
    }

    @Override
    public void showDetails(Context context, boolean extract) {
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
        boolean invalidTag = false;
        
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
                } else if (input_tag.length() > 5 && input_tag.length() < 15 && input_tag.matches("[A-Z]+")) { // capital words from 6 to 14 letters could be meant to be tags for Geneatique for instance
                    invalidTag = true;
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
            
            String xref = currentXref;
            // if broken line is a level 0 line, currentXref is invalid, so attach correction to last good entity, 
            // as well as all the following lines if any to the next entity.
            if (input.getLevel() == 0) {  
                xref = previousXref;
            }
            levelBeingRepaired = parentLevel + 1;
            if (ALLOW_CONT_TAGS.matcher(parentLevelTag).matches()) {
                output.writeLine(levelBeingRepaired, "CONT", input.getLine());
                String pathAfter = previousPath.toString().endsWith("CONC") ? previousPath.getParent().getShortName() + ":CONT" : previousPath.getShortName() + ":CONT";
                fixes.add(new ImportFix(xref, "repairLine.1", "", pathAfter, input.getLine(), input.getLine()));
            } else {
                levelBeingRepaired = parentLevel;
                String newtag = ":NOTE";
                if (levelBeingRepaired == 0) {
                    levelBeingRepaired = 1;
                    newtag = "NOTE";
                }
                if (!input.getLine().trim().isEmpty()) {
                    output.writeLine(levelBeingRepaired, "NOTE", input.getLine());
                    fixes.add(new ImportFix(xref, "repairLine.2", "", previousPath.getParent().getShortName()+newtag, input.getLine(), input.getLine()));
                } else {
                    fixes.add(new ImportFix(xref, "repairLine.3", "", previousPath.getParent().getShortName(), input.getLine(), input.getLine()));
                }
            }
            return true;
            
        } else if (invalidTag) {
            String newTag = "_"+input.getTag().replaceAll("[^A-Za-z0-9]", "");
            output.writeLine(input.getLevel(), input.getXref(), newTag, input.getValue());
            fixes.add(new ImportFix(currentXref, "invalidTag.1", input.getPath().getShortName(), input.getPath().getParent().getShortName()+":"+newTag, input.getValue(), input.getValue()));
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
                    fixes.add(new ImportFix("HEAD", "header.NoteInserted", input.getPath().getShortName(), "HEAD:NOTE", input.getValue(), getImportComment()+input.getValue()));
                    headNoteWritten = true;
                    output.writeLine(2, "CONT", input.getValue());
                    // Line written, next one.
                    return true;
                }
            } else {
                // Write the note
                output.writeLine(1, "NOTE", getImportComment());
                fixes.add(new ImportFix("HEAD", "header.NoteAdded", "", "HEAD:NOTE", "", getImportComment()));
                headNoteWritten = true;
            }
        }

        // DEST tag
        if (destination_found == TAG_MISSING) {
            if ((input.getLevel() == 1) && (input.getTag().equals("GEDC"))) {
                output.writeLine(1, "DEST", "ANY");
                output.writeLine(1, "GEDC", "");
                destination_found = TAG_VALID;
                fixes.add(new ImportFix("HEAD", "header.DestinationAdded", "", "HEAD:DEST", "", "ANY"));
                return true;
            }
        }
        if (destination_found == TAG_INVALID) {
            if ((input.getLevel() == 1) && (input.getTag().equals("DEST"))) {
                output.writeLine(1, "DEST", "ANY");
                destination_found = TAG_VALID;
                fixes.add(new ImportFix("HEAD", "header.DestinationChanged", input.getPath().getShortName(), "HEAD:DEST", input.getValue(), "ANY"));
                return true;
            }
        }

        // PLAC tag
        if (place_found == TAG_MISSING) {
            if ((input.getLevel() == 1) && (input.getTag().equals("PLAC"))) {   // case of PLAC tag present and missing FORM subtag
                output.writeLine(input.getLevel(), input.getTag(), input.getValue());
                output.writeLine(2, "FORM", getPlaceFormat(false));
                place_found = TAG_VALID;
                fixes.add(new ImportFix("HEAD", "header.PlaceAdded", "", "HEAD:PLAC:FORM", "", getPlaceFormat(false)));
                return true;
            }
        }
        if (place_found == TAG_INVALID) { // FOR NOW : do not modify place format in case of invalid characters
            if ((input.getLevel() == 1) && (input.getTag().equals("PLAC"))) {
                output.writeLine(input.getLevel(), input.getTag(), input.getValue());
                input.getNextLine(true);
                output.writeLine(input.getLevel(), input.getTag(), input.getValue());
                place_found = TAG_VALID;
                fixes.add(new ImportFix("HEAD", "header.PlaceInvalid", input.getPath().getShortName(), "HEAD:PLAC:FORM", input.getValue(), input.getValue()));
                return true;
            }
        }
        
        if ((input.getLevel() == 1) && (input.getTag().equals("CHAR"))) {
            boolean ret = false;
            if (char_found == TAG_INVALID) {
                String valueBefore = input.getValue();
                String valueAfter = "UTF-8";
                String correction = "";
                if (input.isCharsetDeterministic()) {
                    valueBefore += " (" + input.getCharset().name() + ")";
                    correction = "header.CharChanged";
                } else {
                    correction = "header.CharInvalid";
                }
                output.writeLine(input.getLevel(), input.getTag(), input.getValue());  // => change to UTF8 is to be done durng fixGedcom(), not now
                char_found = TAG_VALID;
                fixes.add(new ImportFix("HEAD", correction, input.getPath().getShortName(), "HEAD:CHAR", valueBefore, valueAfter));
                ret = true;
            }
            if (place_found == TAG_MISSING) { // if PLAC & FORM tags are both missing, add them after the CHAR
                output.writeLine(1, "PLAC", "");
                output.writeLine(2, "FORM", getPlaceFormat(false));
                output.writeLine(input.getLevel(), input.getXref(), input.getTag(), input.getValue());
                place_found = TAG_VALID;
                fixes.add(new ImportFix("HEAD", "header.PlaceAdded", "", "HEAD:PLAC:FORM", "", getPlaceFormat(false)));
                ret = true;
            }
            if (ret) {
                return ret;
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
    public boolean processEventValues(TagPath path) throws IOException {
        String tag = path.getLast();
        Matcher matcher = tag_y.matcher(tag);
        if (matcher.matches()) {
            int level = input.getLevel();
            String localxref = currentXref;
            String tagBefore = input.getTag();
            String pathBefore = input.getPath().getShortName();
            String valueBefore = input.getValue();
            if (!valueBefore.isEmpty()) {
                if (valueBefore.equalsIgnoreCase("y")) {
                    return false; // line is correct, no issue, let process continue as we are still on the same line // output.writeLine(input);
                } else { 
                    // There should not be any value there, but if there is a chance to move it to a TYPE, do it later in the process (value not empty, not Y and <= 90)
                    // because we want to make sure there is not a TYPE tag underneath already and we do not know that here.
                    // If value is longer than 90, we know for sure to move value to a NOTE because TYPE is limited to 90 char., so do it now.
                    if (valueBefore.length() > 90) {
                        output.writeLine(level, tag, null);
                        String newTag = "NOTE";
                        String correction = "eventValue.2";
                        output.writeLine(level + 1, newTag, valueBefore);
                        fixes.add(new ImportFix(localxref, correction, pathBefore, path.getShortName()+":"+newTag, valueBefore, valueBefore));
                    } else {
                        // We asume line is correct for now, no issue, we will check later if this short value can be moved to a TYPE
                        return false;
                    }
                }
            } else {
                String temp = input.getNextLine(false);  // here we change line
                if ((temp != null) && (input.getLevel() == level + 1)) { 
                    output.writeLine(level, tag, null); 
                    if (!tag.equals(tagBefore)) { // there was a change of tag at the caller level, so log the change
                        fixes.add(new ImportFix(localxref, "invalidTagLocation.2", pathBefore, path.getShortName(), valueBefore, valueBefore));
                    }
                    return true; // line is correct, no issue, but we have changed line just above so stop process by returning false
                    
                } else { // TAG is singleton with no value
                    output.writeLine(level, tag, "Y");
                    fixes.add(new ImportFix(localxref, "eventValue.3", pathBefore, path.getShortName(), "", "Y"));
                }
            }
            return true;
        } else if ("RESI".equals(tag)) {
            int level = input.getLevel();
            String localxref = currentXref;
            String valueBefore = input.getValue();
            String pathBefore = input.getPath().getShortName();
            if (!valueBefore.isEmpty()) {
                // Same as above
                if (valueBefore.length() > 90) {
                    output.writeLine(level, tag, null);
                    String newTag = "NOTE";
                    String correction = "eventValue.2";
                    output.writeLine(level + 1, newTag, valueBefore);
                    fixes.add(new ImportFix(localxref, correction, pathBefore, path.getShortName()+":"+newTag, valueBefore, valueBefore));
                } else {
                    // We asume line is correct for now, no issue, we will check later if this short value can be moved to a TYPE
                    return false;
                }
                return true;
            }
            return false;
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
    public boolean processInvalidTag(String tag) throws IOException {

        TagPath path = input.getPath();
        String valueBefore = input.getValue();
        
        //
        // Special cases common to several software 
        // It is better to include them here to simplify processing and coding
        // and also as they may be relevant for the generic situation
        //

        // Valid but there is better: 
        
        // Legacy & Heredis use same _SHAR tags which are actually ASSO tags.
        if ((input.getLevel() == 2) && (input.getTag().equals("_SHAR"))) {
            output.writeLine(2, "ASSO", input.getValue());
            fixes.add(new ImportFix(currentXref, "invalidTag.3", path.getShortName(), path.getParent().getShortName()+":ASSO", valueBefore, valueBefore));
            revertAsso = true;
            return true;
        }
        
        // Gramps and FTM use _MILT, Geneatique uses EVT, PCS (pacs), CCB (concubin) : an EVEN would be better
        if ((input.getLevel() == 1) && (input.getTag().equals("_MILT") || input.getTag().equals("_EVT") || input.getTag().equals("_PCS") || input.getTag().equals("_CCB"))) {
            String valueAfter = input.getTag().substring(1) + (valueBefore.isEmpty() ? "" : (" - " + valueBefore));
            output.writeLine(input.getLevel(), "EVEN", valueAfter);
            fixes.add(new ImportFix(currentXref, "invalidTag.3", path.getShortName(), path.getParent().getShortName()+":EVEN", valueBefore, valueAfter));
            return true;
        }
        
        // FTM uses _DNA instead of FACT ; Geneatique uses _DPC (implex ?), _REL (relation) for FACT
        if ((input.getLevel() == 1) && (input.getTag().equals("_DNA") || input.getTag().equals("_DPC") || input.getTag().equals("_REL"))) {
            output.writeLine(input.getLevel(), "FACT", valueBefore);
            fixes.add(new ImportFix(currentXref, "invalidTag.3", path.getShortName(), path.getParent().getShortName()+":FACT", valueBefore, valueBefore));
            return true;
        }
        
        // FTM uses _MDCL, _HEIG, _WEIG instead of DSCRs
        if ((input.getLevel() == 1) && ((input.getTag().equals("_MDCL") || input.getTag().equals("_HEIG") || input.getTag().equals("_WEIG")))) {
            output.writeLine(input.getLevel(), "DSCR", valueBefore);
            output.writeLine(input.getLevel()+1, "TYPE", input.getTag().substring(1));
            fixes.add(new ImportFix(currentXref, "invalidTag.3", path.getShortName(), path.getParent().getShortName()+":DSCR", valueBefore, valueBefore));
            return true;
        }
        
        
        
        
        // Invalid and there is better:
        
        // MFT uses DEED and MISE ; geneatique uses MILI : an EVEN would be better
        if ((input.getLevel() == 1) && ((input.getTag().equals("DEED") || input.getTag().equals("MISE") || input.getTag().equals("MILI")))) {
            String valueAfter = input.getTag() + (valueBefore.isEmpty() ? "" : (" - " + valueBefore));
            output.writeLine(input.getLevel(), "EVEN", valueAfter);
            fixes.add(new ImportFix(currentXref, "invalidTag.2", path.getShortName(), path.getParent().getShortName()+":EVEN", valueBefore, valueAfter));
            return true;
        }
        
        // Geneatique uses PACS instead of MARC
        if ((input.getLevel() == 1) && (input.getTag().equals("PACS"))) {
            output.writeLine(input.getLevel(), "MARC", valueBefore);
            fixes.add(new ImportFix(currentXref, "invalidTag.2", path.getShortName(), path.getParent().getShortName()+":MARC", valueBefore, valueBefore));
            return true;
        }
        
        // Ancestrologie uses INHU instead of BURI
        if ((input.getLevel() == 1) && (input.getTag().equals("INHU"))) {
            output.writeLine(input.getLevel(), "BURI", valueBefore);
            fixes.add(new ImportFix(currentXref, "invalidTag.2", path.getShortName(), path.getParent().getShortName()+":BURI", valueBefore, valueBefore));
            return true;
        }
        
        // MFT uses URL instead of FILE in OBJE entities
        if ((input.getLevel() == 1) && (input.getTag().equals("URL"))) {
            output.writeLine(input.getLevel(), "FILE", valueBefore);
            fixes.add(new ImportFix(currentXref, "invalidTag.2", path.getShortName(), path.getParent().getShortName()+":FILE", valueBefore, valueBefore));
            return true;
        }


        
        //
        // General case
        //
        
        // If other user-defined tag, it's ok
        if (tag.startsWith("_")) {
            return false;
        }
        
        // If tag is invalid, change it to a user-defined tag
        Pattern tag_valid = GEDCOM_VERSION.startsWith("5.5.1") ? tag551_valid : tag55_valid;
        if (!tag_valid.matcher(tag).matches()) {
            output.writeLine(input.getLevel(), "_" + tag, input.getValue());
            fixes.add(new ImportFix(currentXref, "invalidTag.1", path.getShortName(), path.getParent().getShortName()+":_"+tag, valueBefore, valueBefore));
            return true;
        }
        return false;
    }

    /**
     * Fix invalid paths by renaming the last tagwith "_".This is called at any import. Uses invalid tagpaths supplied by the specific import class
     * 
     * We are not using Grammar.isValid(tagPath) here as it would be too restrictive (remove HEAD TLTR, CONC, CONT, CHAN:DATE:TIME 
     * and we also need to convert XREF under _XXX tags which cannot be linked, etc)
     * As a result, we go for a "EVERYTHING is Allowed except list", 
     * rather than "everything is forbidden excep list".
     *
     * @param path
     * @return
     * @throws IOException
     */
    public boolean processInvalidPath(TagPath path) throws IOException {

        // Rename tags that are at invalid tag locations according to each import type
        for (String p : invalidPaths) {
            if (path.endsWith(new TagPath(p))) {
                renameTag();
                return true;
            }
        }
        
        //
        // Special cases common to several software
        //
        
        if ((input.getLevel() == 2) && (input.getTag().equals("ASSO"))) {
            revertAsso = true;
        }
        
        return false;
    }

    public void renameTag() throws IOException {
        String newTag = "_"+input.getTag();
        output.writeLine(input.getLevel(), newTag, input.getValue());
        fixes.add(new ImportFix(currentXref, "invalidTagLocation.1", input.getPath().getShortName(), input.getPath().getParent().getShortName()+":"+newTag, input.getValue(), input.getValue()));
    }
    


    /**
     * Normallize DATE tags. This is called at any import. Ensure dates are
     * formattedd as "dd month yyyy" instead of "dd/mm/yyyy"
     *
     * @return
     * @throws IOException
     */
    public boolean processInvalidDates(String date, String correction) throws IOException {
        if ("DATE".equals(input.getTag())) {
            String valueBefore = date;
            String valueAfter = valueBefore;
            
            if (valueBefore.contains("/")) {
                valueAfter = convertDate(valueBefore);
                if (!valueAfter.equals(valueBefore)) {
                    correction = "invalidDate.1";
                }
            }
            
            valueBefore = valueAfter;
            valueAfter = valueBefore.toUpperCase();
            if (!valueAfter.equals(valueBefore)) {
                correction = "invalidDate.2";
            }
            
            valueBefore = valueAfter;
            valueAfter = julianCalCheck(valueBefore);
            if (!valueBefore.equals(valueAfter)) {
                correction = "invalidDate.4";
            }
            
            valueBefore = valueAfter;
            valueAfter = frenchCalCheck(valueBefore);
            if (!valueBefore.equals(valueAfter)) {
                correction = "invalidDate.5";
            }
            
            PropertyDate pDate = new PropertyDate();
            pDate.setValue(valueAfter);
            if (!pDate.isValid()) {
                correction = "invalidDate.3";
            }
            
            if (!correction.isEmpty()) {
                output.writeLine(input.getLevel(), "DATE", valueAfter);
                String tagPathBefore = input.getPath().getShortName();
                fixes.add(new ImportFix(currentXref, correction, tagPathBefore, tagPathBefore, input.getValue(), valueAfter));
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

    private String julianCalCheck(String in) {
        final String JULIAN_TAG = "@#DJULIAN@";
        final Pattern julian_cal = Pattern.compile("(" + JULIAN_TAG + " )(.*)");
        final Pattern date_value = Pattern.compile("(FROM|BEF|AFT|BET|AND|TO|ABT|CAL|EST) (.*)");
        final Pattern date_range = Pattern.compile("(FROM|BEF|AFT|BET|AND|TO) (.*) (TO|AND) (.*)");

        String result = "";
        Matcher matcher = julian_cal.matcher(in);
        if (matcher.matches() && (matcher.groupCount() > 1)) {
            // C'est un cal julian, on essaie d'interpreter
            
            String date_parameter = matcher.group(2);
            Matcher m1 = date_range.matcher(date_parameter);
            if (m1.matches()) {
                result += m1.group(1) + " " + JULIAN_TAG + " " + m1.group(2);
                result += " " + m1.group(3) + " " + JULIAN_TAG + " " + m1.group(4);
                return result;
            }

            m1 = date_value.matcher(date_parameter);
            if (m1.matches()) {
                result += m1.group(1) + " " + JULIAN_TAG + " " + m1.group(2);
                return result;
            }
            result += JULIAN_TAG + " " + matcher.group(2);
            return result;
        } else {
            return in;
        }
    }

    private String frenchCalCheck(String in) {
        final String FRENCH_TAG = "@#DFRENCH R@";
        final Pattern french_cal = Pattern.compile("(" + FRENCH_TAG + " )(.*)");
        final Pattern date_value = Pattern.compile("(FROM|BEF|AFT|BET|AND|TO|ABT|CAL|EST) (.*)");
        final Pattern date_range = Pattern.compile("(FROM|BEF|AFT|BET|AND|TO) (.*) (TO|AND) (.*)");

        String result = "";
        Matcher matcher = french_cal.matcher(in);
        if (matcher.matches() && (matcher.groupCount() > 1)) {
            // C'est un cal republicain, on essaie d'interpreter
            String date_parameter = matcher.group(2);
            Matcher m1 = date_range.matcher(date_parameter);
            if (m1.matches()) {
                result += m1.group(1) + " " + FRENCH_TAG + " " 
                        + convDateFormat(m1.group(2));
                result += " " + m1.group(3) + " " + FRENCH_TAG + " "
                        + convDateFormat(m1.group(4));
                return result;
            }

            m1 = date_value.matcher(date_parameter);
            if (m1.matches()) {
                result += m1.group(1) + " " + FRENCH_TAG + " "
                        + convDateFormat(m1.group(2));
                return result;
            }
            result += FRENCH_TAG + " " + convDateFormat(matcher.group(2));
            return result;
        } else {
            return in;
        }
    }

    @SuppressWarnings("serial")
    static private String convDateFormat(String from) {
        final HashMap<String, String> repmonconvtable = new HashMap<String, String>() {

            {
                put("I", "1");
                put("II", "2");
                put("III", "3");
                put("IV", "4");
                put("V", "5");
                put("VI", "6");
                put("VII", "7");
                put("VIII", "8");
                put("IX", "9");
                put("X", "10");
                put("XI", "11");
                put("XII", "12");
            }
        };
        final Pattern french_date = Pattern.compile("(.*) an (\\w*)(.*)");
        Matcher m = french_date.matcher(from);
        if (m.matches() && m.groupCount() > 2) {
            String result = m.group(1) + " " + repmonconvtable.get(m.group(2));
            if (m.groupCount() > 3) {
                result += m.group(3);
            }
            return result;
        }
        return from;
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
                String tagPathBefore = input.getPath().getShortName();
                output.writeLine(input.getLevel(), "AGE", age);
                fixes.add(new ImportFix(currentXref, "invalidAge.1", tagPathBefore, tagPathBefore, input.getValue(), age));
                return true;
            }
        }

        return false;
    }

    
    /**
     * Add Entities whom ids where found in asso link but not found in Indis or
     * Fams.
     */
    private void checkAssociationEntities() {
        for (String k : hashAssos.keySet()) {
            // Check if asso is related to fam or indi. If id not found assume it's an indi.
            if (!hashIndis.containsKey(k) && !hashFams.containsKey(k)) {
                hashIndis.put(k, new ImportEnt());
            }
        }
    }

    private void addMissingEntities() throws IOException {
        for (String k : hashIndis.keySet()) {
            if (!hashIndis.get(k).seen) {
                output.writeLine(0, k, "INDI", null);
                fixes.add(new ImportFix(k, "missingEntity.Indi", "", "INDI", "", ""));
            }
        }
        for (String k : hashFams.keySet()) {
            if (!hashFams.get(k).seen) {
                output.writeLine(0, k, "FAM", null);
                fixes.add(new ImportFix(k, "missingEntity.Fam", "", "FAM", "", ""));
            }
        }
        for (String k : hashNotes.keySet()) {
            if (!hashNotes.get(k).seen) {
                output.writeLine(0, k, "NOTE", null);
                fixes.add(new ImportFix(k, "missingEntity.Note", "", "NOTE", "", ""));
            }
        }
        for (String k : hashObjes.keySet()) {
            if (!hashObjes.get(k).seen) {
                output.writeLine(0, k, "OBJE", null);
                fixes.add(new ImportFix(k, "missingEntity.Obje", "", "OBJE", "", ""));
            }
        }
        for (String k : hashSours.keySet()) {
            if (!hashSours.get(k).seen) {
                output.writeLine(0, k, "SOUR", null);
                fixes.add(new ImportFix(k, "missingEntity.Sour", "", "SOUR", "", ""));
            }
        }
        for (String k : hashRepos.keySet()) {
            if (!hashRepos.get(k).seen) {
                output.writeLine(0, k, "REPO", null);
                fixes.add(new ImportFix(k, "missingEntity.Repo", "", "REPO", "", ""));
            }
        }
        for (String k : hashSubms.keySet()) {
            if (!hashSubms.get(k).seen) {
                output.writeLine(0, k, "SUBM", null);
                fixes.add(new ImportFix(k, "missingEntity.Subm", "", "SUBM", "", ""));
            }
        }

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

        Set<String> nameTags = new HashSet<>(Arrays.asList("NPFX", "GIVN", "SPFX", "SURN", "NSFX"));

        Collection<Indi> indis = gedcom.getIndis();
        int increment = indis.size() / 10 + 1;
        int counter = 0;
        for (Indi indi : indis) {
            // increment progress
            counter++;
            if (counter % increment == 0 && progress < 100) {
                progress++;
            }
            
            Property[] rawNames = indi.getProperties("NAME", false);
            for (Property rawName : rawNames) {
                if (rawName instanceof PropertyName) {
                    final PropertyName propName = (PropertyName) rawName;

                    // If a name element TAG is empty and singleton, remove it
                    for (Property subprop : propName.getProperties()) {
                        String subTag = subprop.getTag();
                        String pathBefore = subprop.getPath(true).getShortName();
                        if (nameTags.contains(subTag) && subprop.getValue().isEmpty() && subprop.getNoOfProperties() == 0) {
                            propName.delProperty(subprop);
                            hasErrors = true;
                            fixes.add(new ImportFix(indi.getId(), "invalidName.2", pathBefore, "", "", ""));
                        }
                    }
                    
                    // If name is invalid, replace it
                    if (!propName.isValid() || propName.hasWarning()) {
                        String valueBefore = propName.getNameTagValue();
                        String pathBefore = propName.getPath(true).getShortName();
                        propName.fixNameValue();
                        String valueAfter = propName.getValue();
                        if (!valueBefore.equals(valueAfter)) {
                            hasErrors = true;
                            fixes.add(new ImportFix(indi.getId(), "invalidName.1", pathBefore, pathBefore, valueBefore, valueAfter));
                        }
                    }
                }
            }
        }


        return hasErrors;
    }

    /**
     * Fix places. This is called at any import. Makes sure that the PLAC tag
     * has the properly constructed string with the proper number of
     * jurisdictions
     * FL: 2021-05-11 : this control should only be a warning because Ancestris Table of Places can handle modifications much better 
     * and there is not only one way of fixing the number of jurisdictions.
     *
     * @param gedcom
     * @return
     */
    public boolean fixPlaces(Gedcom gedcom) {

        boolean hasErrors = false;
        String[] locs = null;

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
            // If nb of jurisdictions of correct length, return true
            if (locs.length == nbLocs) {
                continue;
            } else if (locs.length < nbLocs) {
                String valueBefore = place.getValue();
                String pathBefore = place.getPath(true).getShortName();
                place.setJurisdictions(gedcom, locs);
                String valueAfter = place.getValue();
                if (!valueAfter.equals(valueBefore)) {
                    hasErrors = true;
                    fixes.add(new ImportFix(place.getEntity().getId(), "invalidPlace.1", pathBefore, pathBefore, valueBefore, valueAfter));
                }
            } else {
                String valueBefore = place.getValue();
                String pathBefore = place.getPath(true).getShortName();
                String valueAfter = place.getValue();
                hasErrors = true;
                fixes.add(new ImportFix(place.getEntity().getId(), "invalidPlace.2", pathBefore, pathBefore, valueBefore, valueAfter));
            }
        }

        return hasErrors;
    }

    /**
     * Modify events which are supposed to happen once (relying on cardinality defined in LDS/gedcom-5-5-x files)
     * 
     * Additionnaly, for all EVEN created along the import process, move label to a TYPE tag even to be 100% compatible and because editors would not be able to edit it.
     * (eventhough it would "acceptable" to have a label there (Gedcom 5.5.1 annoted version))
    */
    public boolean fixEventsCardinality(Gedcom gedcom) {
        
        boolean ret = false;
        
        for (Entity entity : gedcom.getEntities()) {
            if (entity.getTag().equals("INDI") || entity.getTag().equals("FAM")) {
                
                // Get existing event tags with cardinality of 1
                Set<String> tags = new HashSet<>();
                for (Property event : entity.getAllProperties(null)) {
                    String tag = event.getTag();
                    Matcher matcher = tag_y.matcher(tag);
                    MetaProperty meta = event.getMetaProperty();
                    if (matcher.matches() && meta.isSingleton()) {
                        tags.add(tag);
                    }
                }
                
                // For each, rename cardinality greater than one to an EVEN
                for (String tag : tags) {
                    int n = 0;
                    Property[] props = entity.getProperties(tag, false);
                    for (Property event : props) {
                        if (n == 0) {
                            n++;
                            continue;
                        }
                        String valueBefore = event.getValue();
                        String pathBefore = event.getPath(true).getShortName();
                        Property host = entity.addProperty("EVEN", valueBefore);
                        String pathAfter = host.getPath(true).getShortName();
                        for (Property p : event.getProperties()) {
                            movePropertiesRecursively(p, host);
                        }
                        fixes.add(new ImportFix(entity.getId(), "eventsCardinality.1", pathBefore, pathAfter, valueBefore, valueBefore));
                        entity.delProperty(event);
                        ret = true;
                    }
                }
                
                
            }
        }
        return ret;
    }

    /**
     * Fixe OBJE (entity or property) based on grammar
     */
    public boolean fixObje(Gedcom gedcom) {
        
        boolean fixed = false;
        
        final boolean isV55 = Grammar.V55.equals(gedcom.getGrammar());
        String valueBefore = "", valueAfter = "";
        String tagBefore = "", tagAfter = "";
        
        for (Entity entity : gedcom.getEntities()) {
            
            String id = entity.getId();
            
            // Get the OBJE to analyse 
            if (entity instanceof Media) {
                Property obje = entity;
                
                /**
                 * Fixe OBJE (entity) based on grammar
                 * 
                 * ==============================================
                 * 5.5
                 * MULTIMEDIA_RECORD: =
                 * n @XREF:OBJE@ OBJE                   {1:1}
                 * +1 FORM <MULTIMEDIA_FORMAT>          {1:1}
                 * +1 TITL <DESCRIPTIVE_TITLE>          {0:1}
                 * ==============================================
                 * 5.5.1
                 * MULTIMEDIA_RECORD:=
                 * n @XREF:OBJE@ OBJE                   {1:1}
                 * +1 FILE <MULTIMEDIA_FILE_REFN>       {1:M}
                 *   +2 FORM <MULTIMEDIA_FORMAT>        {1:1}
                 *     +3 TYPE <SOURCE_MEDIA_TYPE>      {0:1}
                 *   +2 TITL <DESCRIPTIVE_TITLE>        {0:1}
                 * ==============================================
                 * 
                 */
                // Case of multimedia entity
                // - OBJE should have no value and we have not seen any so far so pass
                // - FILE : none in 5.5 and 1 to several in 5.5.1
                // - FORM : there should be at least 1 and only 1 under OBJE in 5.5, there should be 1 and only 1 under FILE in 5.5.1
                // - MEDI : none
                // - TITL : optional, only 1 and under OBJE in 5.5, under FILE in 5.5.1 but should not be empty
                //
                // The logic is to get the required tags and move them where they should be or add them if mandatory and non existing
                // When we move, we have to check the subordinates
                //
                if (isV55) {
                    fixed |= fixFileStructure(obje, "FORM");
                    // TITL : pass, we do not know the FILE
                } else { // V5.5.1
                    
                    // Take care of FILE & TITL
                    List<Property> files = obje.getAllProperties("FILE");
                    if (files.isEmpty()) { // no file to be found, create FILE and FILE:FORM
                        valueBefore = "";
                        valueAfter = "unknown";
                        Property file = obje.addProperty("FILE", valueAfter);
                        Property form = file.addProperty("FORM", valueAfter);
                        fixed = true;
                        fixes.add(new ImportFix(id, "invalidFileStructure.1", obje.getPath(true).getShortName(), form.getPath(true).getShortName(), valueBefore, valueAfter));
                    } else {
                        // First loop to make sure FILES are all under OBJE
                        for (Property file : files) {  
                            if (file.getParent() != obje) {
                                valueBefore = file.getValue();
                                valueAfter = valueBefore;
                                tagBefore = file.getPath(true).getShortName();
                                tagAfter = obje.getPath(true).getShortName()+":FILE";
                                movePropertiesRecursively(file, obje);
                                file.getParent().delProperty(file);
                                fixed = true;
                                fixes.add(new ImportFix(id, "invalidFileStructure.2", tagBefore, tagAfter, valueBefore, valueAfter));
                            }
                        }
                        // Second loop to check their structure
                        files = entity.getAllProperties("FILE");
                        for (Property file : files) {  
                            fixed |= fixFileStructure(file, "FORM");
                            fixed |= fixTitlStructure(file, file);
                        }
                    }
                    
                }
                
            } else {
                for (Property obje : entity.getAllProperties("OBJE")) {
                    
                    // Case of multimedia_link with reference to obje : property should be singleton but nothing to fix, so pass
                    if (obje instanceof PropertyMedia) {
                        continue;
                    }

                    // Case of multimedia_link as citation
                    // - OBJE should have no value and we have not seen any so far so pass
                    // - FILE : there should be at least 1 and only 1 in 5.5 and several in 5.5.1
                    // - FORM : there should be at least 1 and only 1 under OBJE in 5.5, there should be 1 and only 1 under FILE in 5.5.1 (same as entity)
                    // - MEDI : under FORM, optional and only one, in 5.5.1 only 
                    // - TITL : optional, only 1 and under OBJE
                    // The logic is to get the required tags and move them where they should be or add them if mandatory and non existing
                    //
                    /**
                     * Fixe OBJE (property) based on grammar
                     * 
                     * ==============================================
                     * 5.5
                     * MULTIMEDIA_LINK: =
                     * n  OBJE @<XREF:OBJE>@                {1:1}
                     * |         
                     * n  OBJE                              {1:1}
                     * +1 FILE <MULTIMEDIA_FILE_REFERENCE>  {1:1}
                     * +1 FORM <MULTIMEDIA_FORMAT>          {1:1}
                     * +1 TITL <DESCRIPTIVE_TITLE>          {0:1}
                     * ==============================================
                     * 5.5.1
                     * MULTIMEDIA_LINK:= 
                     * n OBJE @<XREF:OBJE>@                 {1:1}
                     * |
                     * n OBJE
                     * +1 FILE <MULTIMEDIA_FILE_REFN>       {1:M}
                     *   +2 FORM <MULTIMEDIA_FORMAT>        {1:1}
                     *     +3 MEDI <SOURCE_MEDIA_TYPE>      {0:1}
                     * +1 TITL <DESCRIPTIVE_TITLE>          {0:1}
                     * ==============================================
                     */
                    
                    if (isV55) {
                        fixed |= fixFileStructure(obje, "FILE");
                        fixed |= fixFileStructure(obje, "FORM");
                        fixed |= fixTitlStructure(obje, obje.getProperty("FILE", false));
                        
                    } else { // V5.5.1

                        List<Property> files = obje.getAllProperties("FILE");
                        if (files.isEmpty()) { // no file to be found, create FILE and FILE:FORM
                            valueBefore = "";
                            Property file = obje.addProperty("FILE", valueAfter);
                            Property form = file.getParent().getProperty("FORM", false);
                            tagAfter = "";
                            if (form != null) {  // found
                                valueAfter = form.getValue();
                                Property p = file.addProperty("FORM", valueAfter);
                                tagAfter = p.getPath(true).getShortName();
                                form.getParent().delProperty(form);
                            } else {
                                valueAfter = "unknown";
                                Property p = file.addProperty("FORM", valueAfter);
                                tagAfter = p.getPath(true).getShortName();
                            }
                            fixed = true;
                            fixes.add(new ImportFix(id, "invalidFileStructure.1", obje.getPath(true).getShortName(), tagAfter, valueBefore, valueAfter));
                        } else {
                            // First loop to make sure FILES are all under OBJE
                            for (Property file : files) {  
                                if (file.getParent() != obje) {
                                    valueBefore = file.getValue();
                                    valueAfter = valueBefore;
                                    tagBefore = file.getPath(true).getShortName();
                                    tagAfter = obje.getPath(true).getShortName()+":FILE";
                                    movePropertiesRecursively(file, obje);
                                    file.getParent().delProperty(file);
                                    fixed = true;
                                    fixes.add(new ImportFix(id, "invalidFileStructure.2", tagBefore, tagAfter, valueBefore, valueAfter));
                                }
                            }
                            // Second loop to check their structure
                            files = obje.getAllProperties("FILE");
                            Property firstFile = null;
                            for (Property file : files) {  
                                if (firstFile == null) {
                                    firstFile = file;
                                }
                                fixed |= fixFileStructure(file, "FORM");
                            }
                            fixed |= fixTitlStructure(obje, firstFile);
                        }
                    }
                    
                }
            }
            
            
        }        
        
        return fixed;
    }
    
    /**
     * Routine to check there is one and only one tag (FORM or FILE) underneath the host and fix it
     * (host can be OBJE or FILE)
     * @param host
     * @param tag : FORM (for entities OBJE) or FILE and FORM (for OBJE citation)
     * @return 
     */
    
    private boolean fixFileStructure(Property host, String tag) {
        
        String valueBefore = "", valueAfter = "";
        String tagBefore = "", tagAfter = "";
        boolean fixed = false;
        String id = host.getEntity().getId();
        
        List<Property> ptags = host.getAllProperties(tag);
        if (ptags.isEmpty()) { // no tag to be found under the host, create one by looking underneath host parent in the case of FILE
            Property ptag = null;
            if (host.getTag().equals("FILE")) {
                ptag = host.getParent().getProperty(tag, false);
            }
            if (ptag == null) {  // not found
                valueBefore = "";
                if (tag.equals("FORM")) { // in case of FORM, lets try to find the FILE property somewhere
                    Property file = null;
                    if (host.getTag().equals("FILE")) {
                        file = host;
                    } else if (host.getTag().equals("OBJE")) {
                        file = host.getProperty("FILE", false);
                    }
                    if (file != null) {
                        String name = file.getValue();
                        if (name.startsWith("http")) {
                            valueAfter = URLInput.WEB;
                        } else {
                            valueAfter = getExtension(file.getValue()); // do not call file.getSuffix as it creartes too much delay checking the file
                        }
                    }
                    if (valueAfter == null || valueAfter.isEmpty()) {
                        valueAfter = "?";
                    }
                } else {
                    valueAfter = "unknown";
                }
                tagBefore = host.getPath(true).getShortName();
                Property p = host.addProperty(tag, valueAfter);
                tagAfter = p.getPath(true).getShortName();
                fixed = true;
                fixes.add(new ImportFix(id, "invalidFileStructure.1", tagBefore, tagAfter, valueBefore, valueAfter));
            } else {  // found
                valueBefore = ptag.getValue();
                valueAfter = valueBefore;
                tagBefore = ptag.getPath(true).getShortName();
                Property p = host.addProperty(tag, valueAfter);
                tagAfter = p.getPath(true).getShortName();
                ptag.getParent().delProperty(ptag);
                fixed = true;
                fixes.add(new ImportFix(id, "invalidFileStructure.2", tagBefore, tagAfter, valueBefore, valueAfter));
            }
        } else {
            boolean found = false;
            for (Property ptag : ptags) {  // make sure the first one found is under file, else move it and rename the other ones to NOTE if more than one
                if (!found) {
                    found = true;
                    if (ptag.getParent() != host) {
                        valueBefore = ptag.getValue();
                        valueAfter = valueBefore;
                        tagBefore = ptag.getPath(true).getShortName();
                        Property p = host.addProperty(tag, valueAfter);
                        tagAfter = p.getPath(true).getShortName();
                        ptag.getParent().delProperty(ptag);
                        fixed = true;
                        fixes.add(new ImportFix(id, "invalidFileStructure.2", tagBefore, tagAfter, valueBefore, valueAfter));
                    } else {
                        // OK
                    }
                } else {
                    valueBefore = ptag.getValue();
                    valueAfter = valueBefore;
                    tagBefore = ptag.getPath(true).getShortName();
                    Property p = ptag.getParent().addProperty("NOTE", valueAfter);
                    tagAfter = p.getPath(true).getShortName();
                    ptag.getParent().delProperty(ptag);
                    fixed = true;
                    fixes.add(new ImportFix(id, "invalidFileStructure.3", tagBefore, tagAfter, valueBefore, valueAfter));
                }
            }
        }
        
        return fixed;
    
    }
    
    /**
     * Routine to check there is zero or one TITL underneath the host and fix it
     * 
     * @param host : Host can be OBJE (for citation) or FILE (for entity 5.5)
     * @return 
     */
    
    private boolean fixTitlStructure(Property host, Property pFile) {
        
        String valueBefore = "", valueAfter = "";
        String tagBefore = "", tagAfter = "";
        boolean fixed = false;
        String id = host.getEntity().getId();

        List<Property> titls = host.getAllProperties("TITL");
        if (titls.isEmpty() && host.getTag().equals("FILE") && host.getParent().getTag().equals("OBJE")) { // no titl to be found under the host, try to find it under obje in case host is not obje
            // we are necessarily in 5.5.1 because the call to this method is not made in this case for 5.5
            titls = host.getParent().getAllProperties("TITL");
        }
        // Try again
        if (titls.isEmpty()) {
            return fixed;
        } else {
            boolean found = false;
            for (Property titl : titls) {  // make sure the first one found is under host, else move it and rename the other ones to NOTE if more than one
                if (!found) {
                    found = true;
                    if (titl.getParent() != host) {
                        valueBefore = titl.getValue();
                        valueAfter = valueBefore;
                        String correction = "invalidFileStructure.2";
                        if (valueAfter.isEmpty() && pFile != null && pFile instanceof PropertyFile) {
                            PropertyFile filep = (PropertyFile) pFile;
                            InputSource is = filep.getInput().orElse(null);
                            String title = is != null ? is.getName() : "";
                            int i = title.indexOf(".");
                            valueAfter = (i == -1 ? title : title.substring(0, i));
                            correction = "invalidFileStructure.4";
                        }
                        tagBefore = titl.getPath(true).getShortName();
                        Property p = host.addProperty("TITL", valueAfter);
                        tagAfter = p.getPath(true).getShortName();
                        titl.getParent().delProperty(titl);
                        fixed = true;
                        fixes.add(new ImportFix(id, correction, tagBefore, tagAfter, valueBefore, valueAfter));
                    } else {
                        // OK
                    }
                } else {
                    valueBefore = titl.getValue();
                    valueAfter = valueBefore;
                    tagBefore = titl.getPath(true).getShortName();
                    Property p = titl.getParent().addProperty("NOTE", valueAfter);
                    tagAfter = p.getPath(true).getShortName();
                    titl.getParent().delProperty(titl);
                    fixed = true;
                    fixes.add(new ImportFix(id, "invalidFileStructure.3", tagBefore, tagAfter, valueBefore, valueAfter));
                }
            }
        }

        
        return fixed;
    
    }
        

    /**
     * Fix INDI:ADDR or FAM:ADDR and replace it to INDI:RESI or FAM:_RESI
     * 
     * - common to Legacy and Heredis
     */
    public boolean fixAddr(Gedcom gedcom) {
        
        Property resi = null;
        String newTag = "";
        
        for (Entity entity : gedcom.getEntities()) {
            
            if (entity.getTag().equals("INDI")) {
                newTag = "RESI";
            } else if (entity.getTag().equals("FAM")) {
                newTag = "_RESI";
            } else {
                continue;
            }
            
            resi = moveAddrField(entity, null, newTag, "ADDR");
            resi = moveAddrField(entity, resi, newTag, "PHON");
            resi = moveAddrField(entity, resi, newTag, "EMAIL");
            resi = moveAddrField(entity, resi, newTag, "WWW");
            
        }
        
        return resi != null;
    }

    private Property moveAddrField(Entity entity, Property resi, String newTag, String fieldTag) {

        Property newResi = resi;
        String id = entity.getId();
        
        for (Property prop : entity.getProperties(fieldTag, false)) {
            String valueBefore = prop.getValue();
            String pathBefore = prop.getPath(true).getShortName();
            if (newResi == null) {
                newResi = entity.addProperty(newTag, "");
            }
            
            
            // First move field:NOTE to RESI if any
            for (Property prop2 : prop.getProperties("NOTE", false)) {
                movePropertiesRecursively(prop2, newResi);
            }

            // Then move the rest of prop to RESI
            movePropertiesRecursively(prop, newResi);
            
            // Get result to display it
            Property p = newResi.getProperty(fieldTag, false);
            fixes.add(new ImportFix(id, "invalidTagLocation.3", pathBefore, p.getPath(true).getShortName(), valueBefore, valueBefore));
        }
        return newResi;
        
    }
    
    /**
     * Fix errors in sources entities
     */
    public boolean fixSources(Gedcom gedcom) {
        
        boolean fixed = false;
        boolean moved = false;
        
        Property prop = null;
        Property host = null;
        String valueBefore = "";
        String pathBefore = "";
        String pathAfter = "";

        for (Source source : gedcom.getSources()) {
            
            // Put SOUR:DATE as SOUR:_DATE (in citation, it is handled differently)
            Property dates[] = source.getProperties("DATE", false);
            for (Property date : dates) {
                valueBefore = date.getValue();
                pathBefore = date.getPath(true).getShortName();
                Property p = source.addProperty("_DATE", valueBefore);
                source.delProperty(date);
                fixes.add(new ImportFix(source.getId(), "invalidTagLocation.1", pathBefore, p.getPath(true).getShortName(), valueBefore, valueBefore));
                fixed = true;
            }
            
            
            // Put QUAY in SOURCE citation rather than SOUR, if not already there
            moved = false;
            prop = source.getProperty("QUAY");
            if (prop != null) {
                pathBefore = prop.getPath().getShortName();
                valueBefore = prop.getValue();
                if (prop != null) {
                    for (PropertyXRef xref : source.getProperties(PropertyXRef.class)) {
                        if (xref.isValid()) {
                            host = xref.getTarget();
                            if (host instanceof PropertySource) {
                                Property quay = host.getProperty("QUAY");
                                if (quay == null) {
                                    Property p = host.addProperty("QUAY", valueBefore);
                                    pathAfter = p.getPath().getShortName();
                                    fixes.add(new ImportFix(source.getId(), "invalidTagLocation.3", pathBefore, pathAfter, valueBefore, valueBefore));
                                    moved = true;
                                }
                            }
                        }
                    }
                    if (!moved) {
                        Property p = source.addProperty("_QUAY", prop.getValue());
                        pathAfter = p.getPath().getShortName();
                        fixes.add(new ImportFix(source.getId(), "invalidTagLocation.1", pathBefore, pathAfter, valueBefore, valueBefore));
                    }
                    source.delProperty(prop);
                    fixed = true;
                }
            }
            
            // Put Web site in REPO entity rather than SOUR, else change to _WWW
            fixed |= moveTag(source, "WWW");
            // Put MAIL in REPO entity rather than SOUR, else change to "_EMAIL"
            fixed |= moveTag(source, "EMAIL");

        }
         
        return fixed;
    }
    
    private boolean moveTag(Entity source, String tag) {
        
        String pathAfter = "";
        boolean hasErrors = false;
        String correction = "invalidTagLocation.3";
        
        Property prop = source.getProperty(tag);
        if (prop != null) {
            String pathBefore = prop.getPath().getShortName();
            String valueBefore = prop.getValue();
            Property host = source.getProperty("REPO");
            boolean found = false;
            if (host != null && host instanceof PropertyRepository) {
                PropertyRepository pRep = (PropertyRepository) host;
                Property target = pRep.getTargetEntity();
                if (target != null) {
                    Property[] ps = target.getProperties(tag, false); // we can create up to 3 WWW tags for instance
                    int card = 1;
                    for (Property p : ps) {
                        card = p.getMetaProperty().getMaxCardinality();
                        if (p.getValue().equals(valueBefore)) {
                            pathAfter = p.getPath().getShortName();
                            found = true;
                            break;
                        }
                    }
                    if (ps.length < card && !found) {
                        Property p = target.addProperty(tag, valueBefore);
                        pathAfter = p.getPath().getShortName();
                        found = true;
                        }
                }
                
            }
            if (!found) {
                Property p = source.addProperty("_"+tag, prop.getValue());
                pathAfter = p.getPath().getShortName();
                correction = "invalidTagLocation.1";
            }
            source.delProperty(prop);
            fixes.add(new ImportFix(source.getId(), correction, pathBefore, pathAfter, valueBefore, valueBefore));
            hasErrors = true;
            
        }
        
        return hasErrors;
    }
    
    /**
     * Fix errors in sources for all entities
     * 
     * - ...:SOUR:DATE not allowed, move it to DATA, create DATA if does not exist (we assume it is a source date)
     * - ...:SOUR:PAGE:CONC/CONT not allowed, replaced with SOUR:PAGE longer string
     * - ...:SOUR:REFN invalid in citation, move it to source record
     * - NOTE:DATE to be replaced with NOTE:_DATE as the date value appears to already be included in the event date tag (redundant)
     * - ....:TYPE:DATE : Fix invalid TYPE <value>:DATE by replacing with ..:EVEN <value>:DATE
     * - SOUR:DATE invalid when SOUR is an entity ; But in this case, no date exist underneath ; replace with _DATE
     * 
     * - common to Legacy and Heredis
     */
    public boolean fixSourceCitations(Gedcom gedcom) {
        
        boolean fixed = false;
        
        String valueBefore = "";
        String pathBefore = "";
        
        for (Entity entity : gedcom.getEntities()) {

            String id = entity.getId();

            // Fix SOUR
            for (Property source : entity.getAllProperties("SOUR")) {
                
                // SOUR:DATE not allowed, move it to DATA, create DATA if does not exist
                Property dates[] = source.getProperties("DATE", false);
                if (dates != null && dates.length > 0) {
                    Property data = source.getProperty("DATA");
                    if (data == null) {
                        data = source.addProperty("DATA", "");
                    }
                    for (Property date : dates) {
                        valueBefore = date.getValue();
                        pathBefore = date.getPath(true).getShortName();
                        Property p = data.addProperty("DATE", valueBefore);
                        source.delProperty(date);
                        fixes.add(new ImportFix(id, "invalidTagLocation.3", pathBefore, p.getPath(true).getShortName(), valueBefore, valueBefore));
                        fixed = true;
                    }
                }
                
                // SOUR:REFN invalid in citation, move it to source record if exists
                for (Property refn : source.getProperties("REFN", false)) {
                    valueBefore = refn.getValue();
                    pathBefore = refn.getPath(true).getShortName();
                    if (source instanceof PropertySource) {
                        PropertySource pSource = (PropertySource) source;
                        Source sourceEntity = (Source) pSource.getTargetEntity();
                        boolean valueExists = false;
                        Property p = null;
                        for (Property refn2 : sourceEntity.getProperties("REFN", false)) {
                            if (refn2.getValue().equals(valueBefore)) {
                                valueExists = true;
                                p = refn2;
                                break;
                            }
                        }
                        if (!valueExists) {
                            p = sourceEntity.addProperty("REFN", valueBefore);
                        }
                        fixes.add(new ImportFix(id, "invalidTagLocation.3", pathBefore, sourceEntity.getId()+":"+p.getPath(true).getShortName(), valueBefore, valueBefore));
                        fixed = true;
                    } else {
                        Property p = source.addProperty("_REFN", valueBefore);
                        fixes.add(new ImportFix(id, "invalidTagLocation.1", pathBefore, p.getPath(true).getShortName(), valueBefore, valueBefore));
                        fixed = true;
                    }
                    source.delProperty(refn);
                }
                
                
            }
        }
         
        return fixed;
    }

    /**
     * Fix errors in note citations for all entities
     */
    public boolean fixNoteCitations(Gedcom gedcom) {
        
        boolean fixed = false;
        
        String valueBefore = "";
        String pathBefore = "";
        
        for (Entity entity : gedcom.getEntities()) {

            String id = entity.getId();

            for (Property note : entity.getAllProperties("NOTE")) {
                
                // NOTE:SOUR not allowed, move it to ..:SOUR
                Property sources[] = note.getProperties("SOUR", false);
                if (sources != null && sources.length > 0) {
                    for (Property sour : sources) {
                        valueBefore = sour.getValue();
                        pathBefore = sour.getPath(true).getShortName();
                        movePropertiesRecursively(sour, note.getParent());
                        fixes.add(new ImportFix(id, "invalidTagLocation.3", pathBefore, note.getParent().getPath(true).getShortName()+":SOUR", valueBefore, valueBefore));
                        fixed = true;
                    }
                }
            }
        }
         
        return fixed;
    }



    /**
     * Remove invalid CONC and CONT and concatenate to previous tag value
     * All invalid CONC and CONT tags should be the CONC and CONT found, because all the other ones have been eliminated in the PropertyMultiLine reading of the file
     * AS LONG AS THE PATHS ARE VALID, so AS LONG AS previous fixes have been done before which is not always true.
     * 
     * "COPR|NOTE*|AUTH|TITL*|PUBL|TEXT*|SOUR*"
     *      *: with conditions
     * if NOTE, only if not a note link
     * if TITL, only under SOUR:TITL
     * if TEXT, only under SOUR or SOUR:DATA
     * if SOUR, only under SOUR citation (no a SOUR link, nor SOUR entity)
     */
    public boolean fixConcCont(Gedcom gedcom) {
        
        boolean fixed = false;
        
        String valueBefore = "";
        String pathBefore = "";
        
        for (Entity entity : gedcom.getEntities()) {

            String id = entity.getId();
            
            // Get the parents of all invalid CONC and CONT 
            Set<Property> parents = new HashSet<>();
            List<Property> concs = entity.getAllProperties("CONC");
            concs.addAll(entity.getAllProperties("CONT"));
            for (Property conc : concs) {
                boolean valid = false;
                Property parent = conc.getParent();
                String parentTag = parent.getTag();
                
                // Do not change if parent is not valid
                if (!parent.isValid() || parentTag.startsWith("_")) {
                    continue;
                }
                
                if (parentTag.equals("ADDR") && conc.getTag().equals("CONT")) {
                    valid = true;
                // 
                } else if (parentTag.equals("NOTE") && !(parent instanceof PropertyNote)) {
                    valid = true;
                // 
                } else if (parentTag.equals("TITL") && parent.getParent() != null && parent.getParent().getTag().equals("SOUR")) {
                    valid = true;
                // 
                } else if (parentTag.equals("TEXT") && parent.getParent() != null && (parent.getParent().getTag().equals("SOUR") || parent.getParent().getTag().equals("DATA"))) {
                    valid = true;
                // 
                } else if (parentTag.equals("SOUR") && !(parent instanceof PropertySource) && !(parent instanceof Source)) {
                    valid = true;
                }
                if (valid) {
                    continue;
                }
                
                parents.add(parent);
            }

            // Concatenate values to the parent for each parent
            for (Property parent : parents) {
                String value = parent.getValue();
                pathBefore = parent.getPath(true).getShortName();
                valueBefore = value;
                
                Property children[] = parent.getProperties();
                for (Property child : children) {
                    String childTag = child.getTag();
                    value += (childTag.equals("CONT") ? ", " : "") + child.getValue();
                    parent.delProperty(child);
                }
                if (!value.equals(valueBefore)) {
                    parent.setValue(value);
                    fixes.add(new ImportFix(id, "invalidTagLocation.5", pathBefore+":CONC/T", pathBefore, valueBefore, value));
                    fixed = true;
                }
            }
            
            
            // Now revisit all event labels and move them to TYPE if < 90 char and TYPE is free, or else NOTE (ONLY for (value not empty, not Y and <= 90))
            // Previously, check TYPE and move it to NOTE if it ios longer than 90 char.
            for (Property event : entity.getProperties()) {
                String tag = event.getTag();
                if (tag_y.matcher(tag).matches() || "RESI".equals(tag) || "EVEN".equals(tag)) {    
                    valueBefore = "";
                    Property type = event.getProperty("TYPE", false);

                    // First move TYPE to NOTE if longer than 90
                    if (type != null && type.getValue().length() > 90) {
                        Property p = null;
                        String correction = "eventValue.4";
                        valueBefore = type.getValue();
                        try {
                            p = event.addProperty("NOTE", valueBefore, event.getPropertyPosition(type));
                        } catch (GedcomException ex) {
                            p = event.addProperty("NOTE", valueBefore);
                        }
                        pathBefore = type.getPath(true).getShortName();
                        String pathAfter = p.getPath(true).getShortName();
                        event.delProperty(type);
                        fixes.add(new ImportFix(entity.getId(), correction, pathBefore, pathAfter, valueBefore, valueBefore));
                        fixed = true;
                    }

                    // Then move even value to type in the cases below
                    valueBefore = event.getValue();
                    if (!valueBefore.isEmpty() && valueBefore.length() <= 90 && !valueBefore.equalsIgnoreCase("y") && !"EVEN".equals(tag)) { // do not include EVEN, value can be edited
                        Property p = null;
                        String newTag = "TYPE";
                        String correction = "eventValue.1";
                        if (type != null) {
                            newTag = "NOTE";
                            correction = "eventValue.2";
                        }
                        try {
                            p = event.addProperty(newTag, valueBefore, 0);
                        } catch (GedcomException ex) {
                            p = event.addProperty(newTag, valueBefore);
                        }
                        event.setValue("");
                        pathBefore = event.getPath(true).getShortName();
                        String pathAfter = p.getPath(true).getShortName();
                        fixes.add(new ImportFix(entity.getId(), correction, pathBefore, pathAfter, valueBefore, valueBefore));
                        fixed = true;
                    }
                }
            }
        }
         
        return fixed;
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
            String valueBefore = prop.getValue();
            String id = prop.getValue().replace("@", "").trim();
            String idB = id;
            final Indi indiRela = (Indi) gedcom.getEntity(id);  // This will be the new individual A
            if (indiRela == null) {
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
            String tagA = id+":"+prop.getPath(true).getShortName();
            final PropertyAssociation propAsso = (PropertyAssociation) indiRela.addProperty("ASSO", "@" + id + "@");  // add 1 ASSO @A@
            String valueAfter = propAsso.getValue();
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

            // Add other sub-tags of A:ASSO to the new B:ASSO => copy resi/subtags to propAsso/ except RELA and TYPE
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
            prop.getParent().delProperty(prop);
            fixes.add(new ImportFix(id, "switchAssos.1", tagA, idB+":INDI:ASSO", valueBefore, valueAfter));
        }

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

    /**
     * Adds all the sub-tags from propertySrc after the last child property of
     * parentPropertyDest
     *
     * @param propertySrc
     * @param parentPropertyDest
     */
    public void movePropertiesRecursively(Property propertySrc, Property parentPropertyDest) {

        if (parentPropertyDest == null) {
            return;
        }

        int n = parentPropertyDest.getNoOfProperties();
        Property propertyDest = null;
        try {
            String tag = propertySrc.getTag();
            if (tag.equals("CONT") || tag.equals("CONC") || tag.startsWith("_")) {
                // ok: explicitely do nothing 
            } else if (!parentPropertyDest.getMetaProperty().allows(tag)) {
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
     * Calculates place format length based on all place sizes found (f)
     *
     * Use local language definition of jurisdictions (which will have n jurisdictions)
     * - if more are found (f .gt n), complete with jur1, 2, 3 on the left
     * - if less are found (f .let n), default to local language preference
     * 
     * @param freq : if true, length will be length with the maximum frequency (that corresponds to the length that fits most places),
     * otherwise the longuest place size
     * 
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
        
        String defaultPlaceFormat = GedcomOptions.getInstance().getPlaceFormat();
        String[] defaultPlaceFormatArray = PropertyPlace.getFormat(defaultPlaceFormat);
        int dpf_length = defaultPlaceFormatArray.length;
        
        if (place_format_length <= dpf_length) {
            return defaultPlaceFormat;
        }
        
        String place_format = "";
        for (int i = 0; i < place_format_length; i++) {
            if (i < place_format_length-dpf_length) {
                place_format += "Jur" + (i+1);
            } else {
                place_format += defaultPlaceFormatArray[i-place_format_length+dpf_length];
            }
            if (i != (place_format_length-1)) {
                place_format += PropertyPlace.JURISDICTION_SEPARATOR;
            }
        }
        return place_format;
    }
    

    private class ImportEnt {
        protected boolean seen = false;
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
