package ancestris.reports;

import genj.gedcom.*;
import genj.gedcom.time.PointInTime;
import genj.io.InputSource;
import genj.io.input.FileInput;
import genj.report.Report;
import java.io.File;
import java.math.BigInteger;
import java.util.*;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * Ancestris - http://www.ancestris.org
 *
 * Report displays status of information found vs information to be searched for
 * each person in the tree.
 *
 * List starts with de-cujus person and goes up the tree
 *
 * This report relies on Sosa tag filled in with sosa numbers (generated by sosagenerator) 
 *
 * August 2012 - July 2017
 *
 * @author Frederic Lapeyre <frederic@ancestris.org>
 *
 */
@ServiceProvider(service = Report.class)
public class ReportGenealogyStatus extends Report {

    // Public parameters displayed in options.
    public int maxNbOfAncestors = 512;        // limit to avoid displaying too many empty ancestors
    public boolean includeSiblings = true;    // true means we display siblings
    public String directoryParam = System.getProperty("user.home") + "<insert media directory name here>";       // default to home directory
    
    
    private final static String OUI = "1";          // information found, valid and complete
    private final static String APX = "~";          // tag found, information found, valid, but not complete (date is a range, place is not complete)
    private final static String NON = ".";          // tag found, but information not found or not complete or invalid (date is a range for instance)
    private final static String ERR = "#";          // information found but erroneous or missing and mandatory
    private final static String SEQ = "@";          // for family event MARR, indicates that kids appearance is not chronological
    private final static String FIL = "§";          // source media found but not transcripted in a text tag
    private final static String SPA = " ";          // space
    private final static String TAB = "\t";         // tab
    private int maxSosaLength = 5;            // size of max sosa number in characters
    private final static String MAXSTR = "                                                                           ";
    private String[] placeFormat = null;
    private int sizePlaces = 0;
    private final Map<BigInteger, Indi> sosaList = new TreeMap();                      // stores and retrieves individuals by sosa key sorted by sosa nbr
    private final Map<String, String> entMap = new TreeMap();                          // stores and retrieves unreferenced sources 
    private int cntAnomaly = 0, // counter of errors
            cntOutOfSeq = 0,    // counter of out of sequence
            cntKnwnSrc = 0;     // counter of missing transcripted sources
    private final static int MAX_NB_OF_UNUSED_FILES = 100;
    private int nbOfUnusedFiles = 0;
    
    
    
    /**
     * Our main logic.
     */
    public void start(Gedcom gedcom) {

        // Reset
        cntAnomaly = 0;
        cntOutOfSeq = 0;
        cntKnwnSrc = 0;
        maxSosaLength = 5;
        
        // Get all individuals in a sosa list
        Collection entities = gedcom.getEntities(Gedcom.INDI);
        Property[] props;
        String sosaStr;
        String sib = "0";
        for (Iterator it = entities.iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
            props = indi.getProperties(Indi.TAG_SOSA);
            if (props.length == 0) {
                props = indi.getProperties(Indi.TAG_SOSADABOVILLE);
            }
            for (int i = 0; i < props.length; i++) {
                Property prop = props[i];
                // extract big integer from sosa number, grabing siblings of sosa, thus extracting the next number after '-', and stripping out generation number
                sosaStr = prop.getValue();
                int index = sosaStr.indexOf(".");
                if (index != -1 || sosaStr.matches(".*[a-z].*")) {
                    continue;
                }
                index = sosaStr.indexOf(" ");
                sosaStr = sosaStr.substring(0, index); // stripping end
                index = sosaStr.indexOf("-"); // in case there are siblings
                if (index != -1) {
                    sib = sosaStr.substring(index+1);
                    sosaStr = sosaStr.substring(0, index);
                }
                if (!includeSiblings && !sib.equals("0")) {
                    sib = "0";
                    continue;
                }
                BigInteger divider = BigInteger.ONE;
                if (!sib.equals("0")) {
                    divider = divider.add(BigInteger.ONE);
                }
                BigInteger bi = (new BigInteger(sosaStr+"00")).divide(divider).add(new BigInteger(sib));
                sib = "0";
                sosaList.put(bi, indi);
                if (bi.toString().length() > maxSosaLength) {
                    maxSosaLength = bi.toString().length() + 3;
                }
            }
        }
        
        // Quit if no sosa found
        if (sosaList.isEmpty()) {
            println(NbBundle.getMessage(ReportGenealogyStatus.class, "TXT_NoSosaFound"));
            println("========");
            return;
        }
                

        // A few initialisations
        placeFormat = validatePlaceFormat(gedcom.getPlaceFormat());
        sizePlaces = (placeFormat != null) ? placeFormat.length : 0;
        headerLine();

        // Display sosa list with the status of the data found for each entity
        int sosaCnt = 1;        // counts ancestors
        BigInteger sosaNb;      // represents the sosa number being considered
        int i = 1;              // counts the line
        boolean isSibling = false;
        for (Iterator it = sosaList.keySet().iterator(); it.hasNext();) {
            BigInteger item = (BigInteger) it.next();
            BigInteger[] items = item.divideAndRemainder(new BigInteger("100"));
            Indi indi = sosaList.get(item);
            sosaNb = items[0];
            isSibling = !items[1].equals(BigInteger.ZERO);

            // start counting ancestors from above decujus
            if (sosaNb.doubleValue() > 1 && !isSibling) {
                sosaCnt++;
            }

            // fill in emty lines for all ancestors that are still to be discovered 
            while (sosaNb.doubleValue() > sosaCnt && sosaCnt < maxNbOfAncestors) {
                println(emptyLine());
                sosaCnt++;
            }

            // print status of indi
            String space = "";
            BigInteger displaySosa = sosaNb;
            String suffix = "";
            if (isSibling) {
                BigInteger multiplyer = BigInteger.ONE.add(BigInteger.ONE);
                displaySosa = sosaNb.multiply(multiplyer);
                space += " ";
                suffix = "-" + items[1].toString();
            }
            println(getLine(indi, i++, sosaNb, space + displaySosa.toString() + suffix));

            // make it easier to read by putting a line every ten lines
            if ((i % 10) == 0) {
                midLine();
            }
        }

        sosaList.clear();
        println(" ");
        println(" ");
        println("========");
        println(" ");


        //
        // Print error counters
        //
        println(NbBundle.getMessage(ReportGenealogyStatus.class, "TXT_counters"));
        println(" ");
        println("   " + cntAnomaly + "\t " + NbBundle.getMessage(ReportGenealogyStatus.class, "TXT_anomalies") + " (" + ERR + ")");
        println("   " + cntOutOfSeq + "\t" + NbBundle.getMessage(ReportGenealogyStatus.class, "TXT_sequence") + " (" + SEQ + ")");
        println("   " + cntKnwnSrc + "\t" + NbBundle.getMessage(ReportGenealogyStatus.class, "TXT_source") + " (" + FIL + ")");

        println(" ");
        println(" ");
        println("========");

        
        /**
         * Grab data : 
         *  - Get list entities not used or used more than once
         *  - Get list of files
         * 
         */
        println(" ");
        List<Entity> ents = gedcom.getEntities();
        List<String> gedcomFiles = new ArrayList();
        for (Entity ent : ents) {
            for (PropertyXRef xref : ent.getProperties(PropertyXRef.class)) {
                Entity entity = xref.getTargetEntity();
                if (entity != null) {
                    String str = entMap.get(entity.getId());
                    if (str == null) {
                        entMap.put(entity.getId(), ent.getId());
                    } else {
                        entMap.put(entity.getId(), str + ", " + ent.getId());
                    }
                }
            }
            for (PropertyFile file : ent.getProperties(PropertyFile.class)) {
                if (file != null && file.getInput().orElse(null) != null) {
                    gedcomFiles.add(file.getInput().get().getName());
                }
            }
        }
        
        
        
        // Display entities not used
        println(NbBundle.getMessage(ReportGenealogyStatus.class, "TXT_entity_not_used"));
        println(" ");
        boolean none = true;
        for (Entity ent : ents) {
            if (ent.getTag().equals("HEAD") || ent instanceof Submitter) {
                continue;
            }
            String str = entMap.get(ent.getId());
            if (str == null) {
                String value = ent.toString(false);
                println("   " + ent.getId() + " " + value.substring(0, Math.min(value.length(), 50)));
                none = false;
            }
        }
        if (none) {
            println("   " + NbBundle.getMessage(ReportGenealogyStatus.class, "TXT_entity_not_used_none"));
        }

        println(" ");
        println(" ");
        println("========");
        println(" ");

        // Display sources used more than once
        println(NbBundle.getMessage(ReportGenealogyStatus.class, "TXT_multiple_source"));
        println(" ");
        none = true;
        for (Entity ent : ents) {
            String str = entMap.get(ent.getId());
            if (str != null && str.indexOf(",") > 0 && ent instanceof Source) {
                println("   " + ent.getId() + " " + ent.toString(false) + " ===> " + str);
                none = false;
            }
        }
        if (none) {
            println("   " + NbBundle.getMessage(ReportGenealogyStatus.class, "TXT_multiple_source_none"));
        }
        

        entMap.clear();
        println(" ");
        println(" ");
        println("========");
        println(" ");


        //
        // Check unused media files:
        //    - Get all obje of the gedcom file
        //    - Scan specific directories and if file not in list, declare it
        //
        File directory = new File(directoryParam);
        println(NbBundle.getMessage(ReportGenealogyStatus.class, "TXT_unused_media", directory.toString()));
        println(" ");
        if (directory.isDirectory()) {
            nbOfUnusedFiles = 0;
            checkDirectory(directory, gedcomFiles);
            if (nbOfUnusedFiles > MAX_NB_OF_UNUSED_FILES) {
                println(" ");
                println("   " + NbBundle.getMessage(ReportGenealogyStatus.class, "TXT_maxnbofunusedfilesreached"));
            } else if (nbOfUnusedFiles == 0){
                println("   " + NbBundle.getMessage(ReportGenealogyStatus.class, "TXT_directoryUsed"));
            }
        } else {
            println("   " + NbBundle.getMessage(ReportGenealogyStatus.class, "TXT_invalidDirectory"));
        }
        println(" ");
        println(" ");
        println("========");
        println(" ");

    } // end_of_start

    /**
     * Print header line.
     */
    private void headerLine() {
        String str = MAXSTR.substring(0, maxSosaLength-4);
        println("Nb " + TAB + "Sosa" + str + TAB + "Gen" + TAB + "Id       " + TAB + "Med" + TAB + "Birt" + TAB + "Chr " + TAB + "Marr " + TAB + "Fam      " + TAB + "Occu" + TAB + "Deat" + TAB + "Name                                                   ");
        midLine();
    }

    /**
     * Print mid line.
     */
    private void midLine() {
        String str = MAXSTR.substring(0, maxSosaLength-4);
        println("---" + TAB + "----" + str + TAB + "---" + TAB + "---------" + TAB + "---" + TAB + "----" + TAB + "----" + TAB + "---- " + TAB + "---------" + TAB + "----" + TAB + "----" + TAB + "------------------------------------------------------");
    }

    /**
     * Print empty line.
     */
    private String emptyLine() {
        String str = MAXSTR.substring(0, maxSosaLength-1);
        return ".  " + TAB + "." + str + TAB + ".   " + TAB + ".        " + TAB + ".  " + TAB + ".   " + TAB + ".   " + TAB + ".    " + TAB + "         " + TAB + "    " + TAB + "    " + TAB + ".                                                     ";
    }

    /**
     * Get line of text from indi.
     */
    private String getLine(Indi indi, int index, BigInteger sosaNb, String sosa) {

        String line = "";

        // Number of ancestor found
        line += index;
        line += TAB;

        // Sosa nb
        String str = MAXSTR.substring(0, maxSosaLength-sosa.length());
        line += sosa + str;
        line += TAB;

        // Génération
        if (sosaNb == BigInteger.ZERO) {
            line += "--";
        } else {
            int g = 1+ (int) (Math.log(sosaNb.doubleValue()) / Math.log(2));
            line += (""+g);
        }
        
        line += TAB;

        // Id
        line += indi.getId() + "         ".substring(indi.getId().length());
        line += TAB;

        // Media
        line += getMedia(sosa, indi, "INDI:OBJE");
        line += TAB;

        // Birth
        line += getEvent(sosa, indi, "INDI:BIRT", true);
        line += TAB;

        // Christening
        line += getEvent(sosa, indi, "INDI:CHR", false);
        line += TAB;

        // Marriage
        line += getFamilies(sosa, indi, sosaNb.doubleValue() > 1); // family mandatory from parents and above
        line += TAB;

        // Occupation
        line += getMultipleEvent(sosa, indi, "OCCU");
        line += TAB;

        // Death
        line += getEvent(sosa, indi, "INDI:DEAT", sosaNb.doubleValue() > 15); // mandatory from great-great-grand-father and above
        line += TAB;

        // Name
        line += indi.getLastName() + ", " + indi.getFirstName().substring(0, Math.min(40, indi.getFirstName().length()));

        return line;
    }

    /**
     * Get first event found given by tag. Code is made of 4 characters. - date
     * of event - place of event - source of event on 2 digits
     */
    private String getEvent(String sosa, Entity ent, String tag, boolean mandatory) {

        String line = "";

        Property prop = ent.getPropertyByPath(tag);
        if (prop == null) {
            return mandatory ? err() + err() + err() + err() : "    ";
        }    
        
        // date
        line += isValidAndComplete(ent.getPropertyByPath(tag + ":DATE"), mandatory);

        // place
        line += isValidAndComplete(ent.getPropertyByPath(tag + ":PLAC"), mandatory);

        // source and text
        line += getSource(sosa, ent, tag + ":SOUR", mandatory || line.contains("1"));

        return line;
    }

    /**
     * Get all events found given by tag. Code is made of 4 digits representing
     * the number of occurrences found (9 max). 
     * - number of occurrences found. 
     * - necessary element found. 
     * - source of event on 2 digits.
     * 
     */
    private String getMultipleEvent(String sosa, Entity ent, String tag) {

        String line = "";
        boolean isOccu = tag.equalsIgnoreCase("OCCU");

        Property[] prop = ent.getProperties(tag);
        if (prop == null || prop.length == 0) {
            return "    ";
        }

        int intNbFound = 0;
        String src = "  ";
        String tmpSrc;
        for (int i = 0; i < prop.length; i++) {
            Property property = prop[i];
            // look for necessary element depending on tag
            if (isOccu) {
                String str = property.toString();
                intNbFound += (str != null && str.length() > 0) ? 1 : 0;
            }
            // Source and text
            tmpSrc = getSource(sosa, ent, tag + ":SOUR", false);
            // Only overwrite if worse source. Ranking is : "OUI < NON < FIL < ERR"
            if (src.compareTo(ERR) != -1) {             // if error already detected, continue
                continue;
            }
            if (tmpSrc.compareTo(ERR) != -1) {          // if error just detected, assign to src and continue
                src = tmpSrc;
                continue;
            }
            if (src.compareTo(FIL) == -1) {             // else, if a file yet, continue;
                src = tmpSrc;
            }
            if (tmpSrc.compareTo(FIL) != -1) {          // if fil just detected, assign to src and continue
                src = tmpSrc;
                continue;
            }
            if (src.compareTo(NON) == -1) {             // else, if a not found yet, continue;
                src = tmpSrc;
            }
            if (tmpSrc.compareTo(NON) != -1) {          // if not found now just detected, assign to src and continue
                src = tmpSrc;
                continue;
            }
        }

        // Now, write codes
        line += prop.length == 0 ? NON : String.valueOf(prop.length);
        line += intNbFound == 0 ? NON : String.valueOf(intNbFound);
        line += src;
        return line;
    }

    /**
     * Get media of an indi. Code is made of 2 digits. 
     * - media : number of media found, else blank (1 digit)
     * - media proof : a valid file has been found (1 digit)
     */
    private String getMedia(String sosa, Entity ent, String tag) {

        String src = "";

        Property[] props = ent.getProperties(new TagPath(tag));
        boolean exists = props != null && props.length != 0;
        if (!exists) {
            return SPA+SPA+SPA;
        }
        
        src += props.length;
        
        String file = " ";
        Property fp = null;
        boolean found = true;
        for (Property p : props) {
            if (p instanceof PropertyMedia) {
                fp = ((PropertyMedia) p).getTargetEntity().getProperty("FILE");
            } else {
                fp = p.getProperty("FILE");
            }
            if (!isValidFile(fp)) {
                found = false;
                break;
            }
        }
        file = found ? OUI : fil();
        
        src += file + " ";

        return src;
    }

    /**
     * Get source code of an event. Code is made of 2 digits. 
     * - source : source entity exists and has not already been attached to another event
     * - source proof : a non empty text is found and a valid file, among all possibilities to attach a file, has been found
     * Rules: 
     * - If a source has a media then the text should be filled in 
     * - If two sources are attached to an event, only the first one is detected 
     * - Supports both 5.5 and 5.5.1 for an event source
     *      - 5.5: citation is a linked source: SOUR_target_entity:
     *          - text is SOUR_target_entity:TEXT
     *          - obje is 
     *              - PropertySource:OBJE
     *              - SOUR_target_entity:OBJE (linked)
     *              - Then:
     *                      - f1 file is PropertySource:OBJE:FILE
     *                      - f2 file is PropertySource:OBJE_target_entity:BLOB        // not supported
     *                      - f3 file is SOUR_target_entity:OBJE:FILE
     *                      - f4 file is SOUR_target_entity:OBJE_target_entity:BLOB    // not supported
     *      - 5.5.1: citation is a linked source: SOUR_target_entity: 
     *          - text is SOUR_target_entity:TEXT
     *          - obje is 
     *              - PropertySource:OBJE
     *              - SOUR_target_entity:OBJE (linked)
     *              - Then:
     *                      - f1 file is PropertySource:OBJE:FILE 
     *                      - f2 file is PropertySource:OBJE_target_entity:FILE
     *                      - f3 file is SOUR_target_entity:OBJE:FILE
     *                      - f4 file is SOUR_target_entity:OBJE_target_entity:FILE
     * 
     *      - 5.5: citation is not linked : SOUR
     *          - text is SOUR:TEXT
     *          - file does not exist
     *      - 5.5.1: citation is not linked : SOUR
     *          - text is SOUR:TEXT
     *          - file is SOUR:OBJE:FILE
     *          - file is SOUR:OBJE_target_entity:FILE
     */
    private String getSource(String sosa, Entity ent, String tag, boolean mandatory) {

        String src = "";

        Property[] props = ent.getProperties(new TagPath(tag));
        if (props == null || props.length == 0) {   // source missing
            return (mandatory ? err()+err() : SPA+SPA); 
        }
        
        src += props.length;

        String file = " ";
        Property f1 = null, f2 = null, f3 = null, f4 = null;
        Property textProp = null;
        boolean found = true;
        
        for (Property p : props) {
            if (p instanceof PropertySource) { // linked
                Entity sourceEnt = ((PropertySource) p).getTargetEntity();
                textProp = sourceEnt.getProperty("TEXT");
                Property p2 = p.getProperty("OBJE");
                if (p2 != null) {
                    f1 = p2.getProperty("FILE");
                }
                if (p2 != null && p2 instanceof PropertyMedia) {
                    Entity media = ((PropertyMedia) p2).getTargetEntity();
                    f2 = media.getProperty("FILE");
                }
                Property p4 = sourceEnt.getProperty("OBJE");
                if (p4 != null) {
                    f3 = p4.getProperty("FILE");
                }
                if (p4 != null && p4 instanceof PropertyMedia) {
                    Entity media = ((PropertyMedia) p4).getTargetEntity();
                    f4 = media.getProperty("FILE");
                }
                if ((isValidFile(f1) || isValidFile(f2) || isValidFile(f3) || isValidFile(f4)) && isValidText(textProp)) {
                    found = true;
                } else {
                    found = false;
                    break;
                }
            } else {  // not linked (OBJE expected only for grammar 5.5.1)
                textProp = p.getProperty("TEXT");
                boolean is551 = ent.getGedcom().getGrammar().equals(Grammar.V551);
                Property p2 = p.getProperty("OBJE");
                if (p2 != null) {
                    f1 = p2.getProperty("FILE");
                }
                if (p2 != null && p2 instanceof PropertyMedia) {
                    Entity media = ((PropertyMedia) p2).getTargetEntity();
                    f2 = media.getProperty("FILE");
                }
                if (!isValidText(textProp)) {
                    found = false;
                    break;
                }
                if (!is551) {
                    continue;
                }
                if (isValidFile(f1) || isValidFile(f2)) {
                    found = true;
                } else {
                    found = false;
                    break;
                }
            }
        }
        
        file = found ? OUI : fil();
        src += file;

        return src;
    }

    /**
     * Get family of an individual. Returned string is made of several elements:
     * - Event code of MARR event 
     * - family id 
     */
    private String getFamilies(String sosa, Indi indi, boolean mandatory) {

        String line = "";
        Fam[] families = indi.getFamiliesWhereSpouse();

        if (families == null || families.length == 0) {
            return (mandatory ? err() + err() + err() + err() + SPA : "     ") + TAB + "         " + SPA;
        }

        Fam famFound = null;
        for (int i = 0; i < families.length; i++) {
            Fam family = families[i];
            // make sure that spouse is not the same as indi
            Indi spouse = family.getHusband();
            if (spouse == indi) {
                spouse = family.getWife();
            }
            // continue if spouse not valid (null, not an ancestor or a siblings)
            if (spouse != null && spouse.getProperty(Indi.TAG_SOSA) == null && spouse.getProperty(Indi.TAG_SOSADABOVILLE) == null) {
                continue;
            }
            famFound = family;
            break;
        }

        if (famFound != null) {
            line += getEvent(sosa, famFound, "FAM:MARR", mandatory);
            line += checkKidsOrder(famFound);
            line += TAB;
            line += famFound.getId() + "         ".substring(famFound.getId().length());
            line += SPA;
            return line;
        }

        return NON + NON + NON + NON + SPA + TAB + "         " + SPA;
    }

    /**
     * Checks if place is valid and complete.
     */
    private String isValidAndComplete(Property prop, boolean mandatory) {

        if (prop == null) {
            return mandatory ? err() : SPA;
        }
        
        if (!(prop instanceof PropertyDate) && !(prop instanceof PropertyPlace)) {
            return mandatory ? err() : NON;
        }
        
        if (prop.getValue().isEmpty()) {
            return mandatory ? err() : NON;
        }

        if (prop instanceof PropertyDate) {
            PropertyDate pDate = (PropertyDate) prop;
            if (!pDate.isValid()) {
                return err();
            }
//            PropertyDate.Format pf = pDate.getFormat();
//            if (pf != PropertyDate.DATE) {
//                return err();
//            }
            if (pDate.isRange()) {
                return APX;
            }
            PointInTime pit = pDate.getStart();
            if (!pit.isValid()) {
                return err();
            }
            if (!pit.isComplete()) {
                return APX;
            }
            return OUI;
        }

        if (prop instanceof PropertyPlace) {
            String[] place = prop.getValue().trim().split("\\" + PropertyPlace.JURISDICTION_SEPARATOR, -1);
            if (isEmpty(place)) {
                return mandatory ? err() : NON;
            }
            if ((sizePlaces > 0) && (place.length != sizePlaces)) {
                return mandatory ? err() : NON;
            }
            PropertyPlace pPlace = (PropertyPlace) prop;
            if (pPlace.getCity().trim().isEmpty()) {
                return APX;
            }
            return OUI;
        }

        return NON;
    }

    private boolean isEmpty(String[] tags) {
        if (tags == null) {
            return true;
        }
        for (int i = 0; i < tags.length; i++) {
            if (tags[i].trim().length() > 0) {
                return false;
            }
        }
        return true;
    }

    private String[] validatePlaceFormat(String placeFormat) {
        String[] tags = null;
        if (placeFormat.length() != 0) {
            tags = placeFormat.split("\\,", -1);
            for (int i = 0; i < tags.length; i++) {
                tags[i] = tags[i].trim();
            }
        }
        return tags;
    }

    /**
     * Check kids order in a family.
     */
    private String checkKidsOrder(Fam family) {

        Indi[] kids = family.getChildren(false);
        if (kids == null || kids.length < 2) {
            return SPA;
        }
        PropertyDate pDate;
        PropertyDate pDatePrev = null;
        for (int i = 0; i < kids.length; i++) {
            pDate = kids[i].getBirthDate();
            if (pDate == null || !pDate.isValid()) {
                continue;
            }
            if ((pDatePrev != null) && (pDate.compareTo(pDatePrev) < 0)) {
                return seq();
            }
            pDatePrev = pDate;
        }

        return SPA;
    }


    /**
     * Increments.
     */
    private String err() {
        cntAnomaly++;
        return ERR;
    }

    private String seq() {
        cntOutOfSeq++;
        return SEQ;
    }

    private String fil() {
        cntKnwnSrc++;
        return FIL;
    }

    /**
     * Check if directory content is found in gedcom files.
     * returns: true if all files in directory tree (recursively) are used in Gedcom
     */
    private void checkDirectory(File directory, List<String> gedcomFiles) {

        if (nbOfUnusedFiles > MAX_NB_OF_UNUSED_FILES) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                checkDirectory(files[i], gedcomFiles);
            } else {
                String filename = files[i].getName();
                if (!gedcomFiles.contains(filename)) {
                    println("   " + files[i].toString());
                    nbOfUnusedFiles++;
                    if (nbOfUnusedFiles > MAX_NB_OF_UNUSED_FILES) {
                        return;
                    }
                }
            }
        }
        return;
    }
    

    private boolean isValidFile(Property fp) {
        if (fp != null && fp instanceof PropertyFile) {
            InputSource is = ((PropertyFile) fp).getInput().orElse(null);
            File f = ((FileInput) is).getFile();
            if (f != null && f.exists()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isValidText(Property textProp) {
        if (textProp == null) {
            return false;
        }
        String text = textProp.getValue().trim();
        return !text.isEmpty();
    }
}