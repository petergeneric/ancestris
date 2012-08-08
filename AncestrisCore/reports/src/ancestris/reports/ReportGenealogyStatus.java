package ancestris.reports;

import genj.gedcom.*;
import genj.gedcom.time.PointInTime;
import genj.report.Report;
import java.io.File;
import java.util.*;
import org.openide.util.lookup.ServiceProvider;

/**
 * Report to display status of information documented for each person in the
 * tree List starts with de-cujus person and goes up the tree
 *
 * August 2012
 *
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 *
 */
@SuppressWarnings("unchecked")
@ServiceProvider(service = Report.class)
public class ReportGenealogyStatus extends Report {

    public boolean includeSiblings = false;
    public int sosaLimit = 512;
    public String directoryOption = "";
    private static String OUI = "1";
    private static String NON = ".";
    private static String ERR = "#";
    private static String SEQ = "o";
    private static String FIL = "S";
    private static String SPA = " ";
    private static String TAB = "\t";
    private String[] placeFormat = null;
    private int sizePlaces = (placeFormat != null) ? placeFormat.length : 0;
    private Map<String, Indi> sosaList = new TreeMap();
    private Map<Source, String> sourceMap = new TreeMap();
    private Map<String, String> entMap = new TreeMap();
    private int cntAnomaly = 0, // #
            cntOutOfSeq = 0, // o
            cntKnwnSrc = 0;   // 111.

    /**
     * Our main logic
     */
    public void start(Gedcom gedcom) {

        // Get all individuals in a sosa list
        Collection entities = gedcom.getEntities(Gedcom.INDI);
        Property[] props = null;
        String sosaStr = "";
        for (Iterator it = entities.iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
            props = indi.getProperties("_SOSA");
            if (props == null) {
                continue;
            }
            for (int i = 0; i < props.length; i++) {
                Property prop = props[i];
                sosaStr = prop.toString();
                if (!includeSiblings && (sosaStr.indexOf("+") != -1 || sosaStr.indexOf("-") != -1) && (sosaStr.indexOf("00000001") == -1)) {
                    continue;
                }
                sosaList.put(sosaStr, indi);
            }
        }

        // Display sosa list
        placeFormat = validatePlaceFormat(gedcom.getPlaceFormat());
        String line = "";
        String sosa = "";
        headerLine();

        int sosaCnt = 1;
        int sosaNb = 0;
        int i = 1;
        for (Iterator it = sosaList.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            Indi indi = sosaList.get(key);
            sosaNb = getSosaNb(key);
            if (sosaNb > 1) {
                sosaCnt++;
            }
            while (sosaNb > sosaCnt && sosaNb < sosaLimit) {
                println(emptyLine());
                sosaCnt++;
            }
            println(getLine(indi, i++, key));
            if ((i / 10) * 10 == i) {
                midLine();
            }
            line = "";
        }

        sosaList.clear();
        sourceMap.clear();
        println(" ");
        println(" ");
        println("========");
        println(" ");



        // List sources not used or used more than once
        println("Liste des entités inutilisées ou des sources utilisées plus d'une fois:");
        println(" ");
        List ents = gedcom.getEntities();
        for (Iterator it = ents.iterator(); it.hasNext();) {
            Entity ent = (Entity) it.next();
            List ps = ent.getProperties(PropertyXRef.class);
            for (Iterator it2 = ps.iterator(); it2.hasNext();) {
                PropertyXRef xref = (PropertyXRef) it2.next();
                Entity entity = xref.getTargetEntity();
                if (entity != null) {
                    String str = entMap.get(entity.getId());
                    if (str == null) {
                        entMap.put(entity.getId(), ent.getId());
                    } else {
                        entMap.put(entity.getId(), str + "," + ent.getId());
                    }
                }
            }
        }
        for (Iterator it = ents.iterator(); it.hasNext();) {
            Entity ent = (Entity) it.next();
            String str = entMap.get(ent.getId());
            if (str == null) {
                println("   Entity not used: " + ent.getId());
            }
            if (str != null && (numberOf(str, ",") > numberOf(str, "R")) && ent instanceof Source) {
                println("   Source used more than once: " + ent.getId() + " - " + str);
            }
        }

        entMap.clear();
        println(" ");
        println(" ");
        println("========");
        println(" ");



        // Check unused media files:
        //    - Get all obje of the gedcom file
        //    - Scan specific directories an if file not in list, decare it
        //
        List<String> gedcomFiles = new ArrayList();
        entities = gedcom.getEntities();
        for (Iterator it = entities.iterator(); it.hasNext();) {
            Entity ent = (Entity) it.next();
            for (Iterator fileIt = ent.getProperties(PropertyFile.class).iterator(); fileIt.hasNext();) {
                PropertyFile file = (PropertyFile) fileIt.next();
                //println(file.getFile().getName());
                gedcomFiles.add(file.getFile().getName());
            }
        }
        File directory = new File(directoryOption);
        boolean found = false;
        if (directory != null) {
            println("Liste des fichiers de '" + directory.toString() + "' non utilisés par le Gedcom:");
            println(" ");
            found = checkDirectory(directory, gedcomFiles);
        }
        if (!found) {
            println("   Aucun. Répertoire invalide ou bien alors tous les fichiers dans cette arborescence sont utilisés par le Gedcom.");
        }
        println(" ");
        println(" ");
        println("========");
        println(" ");

        // Print error counters
        //
        println("Compteurs d'erreurs:");
        println(" ");
        println("   Anomalies (" + ERR + ")                   : " + cntAnomaly);
        println("   Séquence dans les familles (" + SEQ + ")  : " + cntOutOfSeq);
        println("   Fichiers sources à attacher (" + FIL + ") : " + cntKnwnSrc);

        println(" ");
        println(" ");
        println("========");

    } // end_of_start

    /**
     * Print header line
     */
    private boolean checkDirectory(File directory, List<String> gedcomFiles) {

        boolean found = false;

        File[] files = directory.listFiles();
        if (files == null) {
            return false;
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                found = found || checkDirectory(files[i], gedcomFiles);
            } else {
                String filename = files[i].getName();
                if (!gedcomFiles.contains(filename)) {
                    println(files[i].toString());
                    found = true;
                }
            }
        }
        return found;
    }

    /**
     * Print header line
     */
    private void headerLine() {
        println("Au choix:");
        println("  Rapide: 1. Résoudre les anomalies '" + ERR + "'                             (tous les évènements)");
        println("  Rapide: 2. Résoudre l'ordre des étiquettes dans les familles '" + SEQ + "'  (familles uniquement)");
        println("  Rapide: 3. Attacher les fichiers des sources connues '" + FIL + "'          (tous les évènements)");
        println("  Long:   4. Rechercher les Photos si possible");
        println("  Long:   5. Rechercher dates et lieux pour '..  '                  (BIRT, DEAT, MARR et autres évènements si possible)");
        println("  Long:   6. Rechercher les actes      pour '  ..'                  (BIRT, DEAT, MARR et autres évènements si possible)");
        println(" ");
        println("Idx" + TAB + "Sosa              " + TAB + "Gene" + TAB + "Id  " + TAB + "Photo" + TAB + "Birth" + TAB + "Death" + TAB + "Marr" + TAB + "Fam " + TAB + "Marl" + SPA + "Marc" + SPA + "Div " + TAB + "Chr " + SPA + "Buri" + SPA + "Even" + SPA + "Natu" + SPA + "Adop" + SPA + "Grad" + SPA + "Immi" + TAB + "Nom                     ");
        midLine();
    }

    /**
     * Print mid line
     */
    private void midLine() {
        println("---" + TAB + "------------------" + TAB + "----" + TAB + "----" + TAB + "-----" + TAB + "-----" + TAB + "-----" + TAB + "----" + TAB + "----" + TAB + "----" + SPA + "----" + SPA + "----" + TAB + "----" + SPA + "----" + SPA + "----" + SPA + "----" + SPA + "----" + SPA + "----" + SPA + "----" + TAB + "------------------------");
    }

    /**
     * Print empty line
     */
    private String emptyLine() {
        return ".  " + TAB + ".                 " + TAB + ".    " + TAB + ".  " + TAB + ".    " + TAB + ".    " + TAB + ".    " + TAB + ".   " + TAB + "    " + TAB + "    " + SPA + "    " + SPA + "    " + TAB + "    " + SPA + "    " + SPA + "    " + SPA + "    " + SPA + "    " + SPA + "    " + SPA + "    " + TAB + ".                       ";
    }

    /**
     * Get line of text from indi
     */
    private String getLine(Indi indi, int index, String sosa) {

        Property[] props = null;
        String line = "";

        // Index
        line += index;
        line += TAB;

        // Sosa nb
        line += sosa + "                     ".substring(sosa.length());
        line += TAB;

        // Génération
        int sosaNb = getSosaNb(sosa);
        line += sosaNb == 0 ? "--" : (int) (Math.log(2 * sosaNb) / Math.log(2));
        line += TAB;

        // Id
        line += indi.getId();
        line += TAB;

        // Number of pictures
        props = indi.getProperties(new TagPath("INDI:OBJE:FILE"));
        line += props == null || props.length == 0 ? NON : props.length;
        line += TAB;

        // Birth
        line += getEvent(sosa, indi, "INDI:BIRT", sosaNb > 11);
        line += TAB;

        // Death
        line += getEvent(sosa, indi, "INDI:DEAT", sosaNb > 11);
        line += TAB;

        // Marriage
        line += getFamilies(sosa, indi, sosaNb > 11 && sosa.indexOf("-") == -1);
        line += TAB;

        // Christening
        line += getEvent(sosa, indi, "INDI:CHR", false);
        line += SPA;

        // Buri
        line += getEvent(sosa, indi, "INDI:BURI", false);
        line += SPA;

        // Even
        line += getEvent(sosa, indi, "INDI:EVEN", false);
        line += SPA;

        // Natu
        line += getEvent(sosa, indi, "INDI:NATU", false);
        line += SPA;

        // Adoption
        line += getEvent(sosa, indi, "INDI:ADOP", false);
        line += SPA;

        // Graduation
        line += getEvent(sosa, indi, "INDI:GRAD", false);
        line += SPA;

        // Immigration
        line += getEvent(sosa, indi, "INDI:IMMI", false);
        line += TAB;

        // Name
        line += indi.getLastName() + ", " + indi.getFirstName().substring(0, Math.min(10, indi.getFirstName().length()));

        return line;
    }

    /**
     * Get sosa nb from sos string
     */
    private int getSosaNb(String str) {

        int sosaNb = 0;

        int start = 0, end = 0;
        while (start <= end && !Character.isDigit(str.charAt(start))) {
            start++;
        }
        end = start;
        while ((end <= str.length() - 1) && Character.isDigit(str.charAt(end))) {
            end++;
        }
        if (end == start) {
            return 0;
        } else {
            sosaNb = (int) Integer.parseInt(str.substring(start, end));
        }

        return sosaNb;


    }

    /**
     * Get an event given by tag
     */
    private String getEvent(String sosa, Entity ent, String tag, boolean mandatory) {

        String line = "";

        Property prop = ent.getPropertyByPath(tag);
        if (prop == null) {
            return mandatory ? err() + err() + err() + err() : "    ";
        }

        // date
        line += isValidAndComplete(ent.getPropertyByPath(tag + ":DATE"));

        // place
        line += isValidAndComplete(ent.getPropertyByPath(tag + ":PLAC"));

        // source and text
        line += getSource(sosa, ent, tag + ":SOUR");

        return line;
    }

    /**
     * Get families of an individual
     */
    private String getFamilies(String sosa, Indi indi, boolean mandatory) {

        String line = "";
        Fam[] families = indi.getFamiliesWhereSpouse();

        if (families == null || families.length == 0) {
            return (mandatory ? err() + err() + err() + err() : "    ") + TAB + "    " + TAB + "    " + SPA + "    " + SPA + "    ";
        }

        Fam famFound = null;
        for (int i = 0; i < families.length; i++) {
            Fam family = families[i];
            Indi spouse = family.getHusband();
            if (spouse == indi) {
                spouse = family.getWife();
            }
            // if siblings, take that family else check spouse is a sosa
            if (spouse != null && spouse.getProperty("_SOSA") == null && (sosa.indexOf("-") == -1)) {
                continue;
            }
            famFound = family;
            break;
        }

        if (famFound != null) {
            line += getEvent(sosa, famFound, "FAM:MARR", mandatory);
            line += checkKidsOrder(famFound);
            line += TAB;
            line += famFound.getId();
            line += TAB;
            line += getEvent(sosa, famFound, "FAM:MARL", false);
            line += SPA;
            line += getEvent(sosa, famFound, "FAM:MARC", false);
            line += SPA;
            line += getEvent(sosa, famFound, "FAM:DIV", false);
            return line;
        }

        return NON + NON + NON + NON + TAB + NON + NON + NON + NON + TAB + "    " + SPA + "    " + SPA + "    ";
    }

    /**
     * Checks if place is valid and complete
     */
    private String isValidAndComplete(Property prop) {

        if (prop == null || (!(prop instanceof PropertyDate) && !(prop instanceof PropertyPlace))) {
            return NON;
        }

        if (prop instanceof PropertyDate) {
            PropertyDate pDate = (PropertyDate) prop;
            if (pDate == null || !pDate.isValid() || pDate.isRange()) {
                return NON;
            }
            PropertyDate.Format pf = pDate.getFormat();
            if (pf != PropertyDate.DATE) {
                return NON;
            }
            PointInTime pit = pDate.getStart();
            if (!pit.isValid() || !pit.isComplete()) {
                return NON;
            }
            return OUI;
        }

        if (prop instanceof PropertyPlace) {
            String[] place = prop.toString().split("\\,", -1);
            if ((sizePlaces > 0) && (place.length != sizePlaces)) {
                return NON;
            }
            if (isEmpty(place)) {
                return NON;
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
     * Get source and text of an event given by tag
     */
    private String getSource(String sosa, Entity ent1, String tag) {

        String src = "";
        Property[] props = ent1.getProperties(new TagPath(tag));

        if (props == null || props.length == 0) {
            return NON + NON;
        }
        if (props.length != 1) {
            return err() + err();
        }

        Property prop = props[0];
        if (prop == null || !(prop instanceof PropertyXRef)) {
            src += NON + NON;
        } else {
            Entity ent2 = ((PropertyXRef) prop).getTargetEntity();
            if (ent2 instanceof Source) {
                Source source = (Source) ent2;
                String sosa2 = sourceMap.get(source);
                if (sosa2 == null || sosa2.compareTo(sosa) != 0) {
                    sourceMap.put(source, sosa);
                    src += OUI;
                } else {
                    src += err();   // c'est une erreur si la source a déjà été utilisée pour cette entité
                }
                Property ptext = ent2.getProperty("TEXT");
                Property pobje = ent2.getPropertyByPath("SOUR:OBJE:FILE");
                src += ptext == null || ptext.toString().length() < 1 ? (pobje == null ? NON : err()) : (pobje == null ? fil() : OUI);
            } else {
                src += err() + NON;
            }
        }

        return src;
    }

    /**
     * Check kids order in a family
     */
    private String checkKidsOrder(Fam family) {

        String line = "";

        Indi[] kids = family.getChildren(false);
        if (kids == null || kids.length < 2) {
            return "";
        }
        PropertyDate pDate = null, pDatePrev = null;
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

        return "";
    }

    /**
     * Counts nb of same str in a string
     */
    private int numberOf(String str, String seq) {

        if (str == null || seq == null) {
            return 0;
        }
        int value = 0;
        int pos = 1;
        while (pos < str.length()) {
            pos = str.indexOf(seq, pos - 1) + 2;
            if (pos == 1) {
                break;
            }
            value++;
        }
        return value;
    }

    /**
     * Increments
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
} // End_of_Report