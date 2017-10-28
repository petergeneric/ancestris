package ancestris.modules.flashlist;

/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import ancestris.util.swing.DialogManager;
import genj.fo.Document;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.TagPath;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

//
// Flash lists are structured lists with locations and individual lastnames
// The following report allows 3 keys to sort the items in the report.
// As described below, 2 of these keys will be location related, 1 will be the 
// lastname. 
// Design of the keys allow 1 of the keys to be made of 2 location
// elements to provide more possibilities, so as to use 3 location elements in total.
// The first key will be used for a potential table of content.
// So typically, one can sort by Country/Department, then city, then lastname.
// (or any combination of these 3).
// Rather than choosing Country/Department and City, users can chose any other 
// jurisdiction in the way they structure places. For instance, they can also choose 
// "City/District, Region, lastname" as long as city, district and region are
// jurisdiction levels in the gedcom.
//
// - Lastnames will be taken from the only available lastnames
//   If no lastname exist for a person, individual will not be reported
// - Choices exist insofar as locations depending on whether there is a PLAC tag
//   in the header or not in the Gedcom.
//
//   + If a PLAC tag is found in the header, user will be prompted to consider 
//   any of their components and ask which ones to use
//   For CITY tags, city will be CITY only.
//   For PLAC tags, key defined by user will be used. 
//
//   + If no PLAC tag is found, only 1 location key will be used, and the combined
//   location key will be empty and considered as the 3rd sorting key. In this case
//   the options related to pointing to the PLAC tag will be disregarded
//   and the sorting option will only be used to know if location precedes lastname
//   or the other way around. 
//   For CITY tags, city will be CITY only.
//   For PLAC tags, city will be first available jurisdiction only. 
//
// PLAC tags and CITY tags will only be extracted for events only.
// Tags considered as events in the gedcom norm are:
// Family - ANUL, CENS, DIV, DIVF, ENGA, MARR, MARB, MARC, MARL, MARS, EVEN
// Indiv - ADOP, BIRT, BAPM, BARM, BASM, BLES, BURI, CENS, CHR, CHRA, CONF
// CREM, DEAT, EMIG, FCOM, GRAD, IMMI, NATU, ORDN, RETI, PROB, WILL, EVEN
//
//
// Main logic for the report is to build records like this:
// | Key1 | Key2 | Key3 | Sosa nb | B | C | M | D | O | Ymin | Ymax |
//
// where
//   Key1, key2, key3 : any combination of 
//         Loc1/Loc2 :  location levels 1 and 2, e.g. Country/State, Region/City, etc
//         Name :       lastname of the person
//         Place :      location level 3, e.g. City or District within a city, etc
//   Sosa Nb : number of events in the Place where Name is an ancestor of the DeCujus
//   B :       number of Name born in that lace
//   C :       number of Name christened in that place
//   M :       number of Name married in that place
//   D :       number of Name dead in that place
//   O :       number of other events for Name in that place
//   Ymin :    first year of an event of Name in that Place
//   Ymax :    last year of an event of Name in that Place
//  
// Using MapTree (sorted mapping lists, this means 3 levels of pointers
//   primary -> Key ; secondary
//                    secondary -> Key ; tertiary
//                                       tertiary -> Key, Range
//   and Range holds the volumes and year range
//  
//    
// Options also include to highlight lines that have a lot of events, 
// or a large year span
//
//
/**
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 2.2
 */
@SuppressWarnings("unchecked")
public class ReportFlashList extends Report {

    private static final Logger LOG = Logger.getLogger("ReportFlashList");
    
    private final static TagPath CITY = new TagPath(".:ADDR:CITY");
    //private final static TagPath STAE = new TagPath(".:ADDR:STAE");
    //private final static TagPath CTRY = new TagPath(".:ADDR:CTRY");

    // formatting
    private final static String FORMAT_LNORMAL = "font-weight=normal,text-align=left";
    private final static String FORMAT_CNORMAL = "font-weight=normal,text-align=center";
    private final static String FORMAT_RNORMAL = "font-weight=normal,text-align=right";
    private final static String FORMAT_LSTRONG = "font-weight=bold,text-align=left";
    private final static String FORMAT_CSTRONG = "font-weight=bold,text-align=center";
    private final static String FORMAT_RSTRONG = "font-weight=bold,text-align=right";
    private final static String FORMAT_CBACKGROUND = "background-color=#ffffcc,font-weight=bold,text-align=center";
    private final static String FORMAT_RBACKGROUND = "background-color=#ffffcc,font-weight=bold,text-align=right";

    /** options panel */
    FlashListPanel flashListOptions = null;
    
    /** option - Position of the legend if any */
    private final static int LEGEND_NO = 0;
    private final static int LEGEND_TOP = 1;
    private final static int LEGEND_BOT = 2;

    /** option - Key structure */
    private boolean existPLACTag = true;       // Will be false if no PLAC tag found in header
    private final static int LOC12_SURN_LOC34 = 0;
    private final static int LOC12_LOC34_SURN = 1;
    private final static int SURN_LOC12_LOC34 = 2;
    private int recordKey = LOC12_SURN_LOC34;  // Will indicate sorting structure
    private int posLoc1 = 0;
    private int posLoc2 = 2;
    private int posLoc3 = 4;
    private int posLoc4 = 4;
    
    /** Filter on selection keys */
    public String filterKey1 = "";
    public String filterKey2 = "";
    public String filterKey3 = "";

    /**
     * the report's entry point Our main logic
     */
    public Document start(Gedcom gedcom, String documentName) {

        //Get location and lastname options based on PLAC tag in gedcom and user input
        // Updates values of recordKey, posLoc1, posLoc2, posLoc3, existPLACTag
        if (!getFlashOptions(gedcom)) {
            return null;
        }

        // Memorise options for quick access in analysis
        boolean repeatKeys = flashListOptions.getRepeatKeys();
        boolean displayZeros = flashListOptions.getDisplayZeros();
        existPLACTag = flashListOptions.existPlaceTag();
        recordKey = flashListOptions.getRecordKey();
        posLoc1 = flashListOptions.getPosLoc(1);
        posLoc2 = flashListOptions.getPosLoc(2);
        posLoc3 = flashListOptions.getPosLoc(3);
        posLoc4 = flashListOptions.getPosLoc(4);
        int nbEvents = flashListOptions.getNbEvents();
        int yearSpan = flashListOptions.getYearSpan();
        int minSosa = flashListOptions.getMinSosa();
        filterKey1 =  flashListOptions.getFilter1();
        filterKey2 = flashListOptions.getFilter2();
        filterKey3 = flashListOptions.getFilter3();

        // prepare our index
        Collection <Indi>indis = (Collection <Indi>) gedcom.getEntities(Gedcom.INDI);
        Map <String, Object>primary = new TreeMap<String, Object>();
        LOG.log(Level.INFO, NbBundle.getMessage(this.getClass(), "ReportFlashList.StartingAnalysis"));
        for (Iterator <Indi>it = indis.iterator(); it.hasNext();) {
            analyze(it.next(), primary, flashListOptions.getRootIndi());
        }
        LOG.log(Level.INFO, NbBundle.getMessage(this.getClass(), "ReportFlashList.NowWriting"));

        // write main file out
        Document doc = new Document(documentName);

        // Display toc
        if (flashListOptions.getTOC()) {
            doc.addTOC();
            if (primary.size() > 10) {
                doc.nextPage();
            }
        }

        // Display legend on top
        if (flashListOptions.getDisplayLegend() == LEGEND_TOP) {
            displayLegend(doc);
        }

        // Display one header if will not repeat them
        boolean repeatHeader = flashListOptions.getRepeatHeader();
        if (!repeatHeader) {
            displayHeader(doc, null, true);
            doc.endTable();
        }

        // loop on 3 main maps to perform a couple of operations:
        // - write the main file
        // - assign geo coordinates to group by lastnames into totals (if code included)
        for (Iterator <String>ps = primary.keySet().iterator(); ps.hasNext();) {
            String p = ps.next();

            // primary key (by default Cntry/State)
            // second parameter is a meaningful anchor for easier external referencing to this flash list
            doc.startSection(p, p.replaceAll(" ", "%").replaceAll("/", "%").replaceAll(",", "%"));
            displayHeader(doc, p, repeatHeader);
            String secondaryKey = "";

            Map <String, Object>secondary = (Map) lookup(primary, p, null);
            for (Iterator <String>ss = secondary.keySet().iterator(); ss.hasNext();) {
                String s = ss.next();
                Map <String, Object>tertiary = (Map) lookup(secondary, s, null);
                for (Iterator <String>ts = tertiary.keySet().iterator(); ts.hasNext();) {
                    String t = ts.next();
                    Range range = (Range) lookup(tertiary, t, null);

                    String lformat = FORMAT_LNORMAL;
                    String cformat = FORMAT_CNORMAL;
                    String rformat = FORMAT_RNORMAL;
                    if ((range.getNbEvents() >= nbEvents)
                            || (range.getYearSpan() >= yearSpan)
                            || (range.getValueSosa() >= minSosa)) {
                        lformat = FORMAT_LSTRONG;
                        cformat = FORMAT_CSTRONG;
                        rformat = FORMAT_RSTRONG;
                    }

                    doc.nextTableRow(lformat);
                    if (repeatKeys == true || !secondaryKey.equals(s)) {
                        doc.addText(s);
                    } else {
                        doc.addText(" ");
                    }
                    if (!secondaryKey.equals(s)) {
                        secondaryKey = s;
                    }
                    doc.nextTableCell(lformat);
                    doc.addText(t);
                    doc.nextTableCell(rformat);
                    doc.addText((!displayZeros && (range.nbSosa == 0)) ? "-" : range.getNbSosa());
                    doc.nextTableCell(rformat);
                    doc.addText((!displayZeros && (range.nbBirth == 0)) ? "-" : range.getNbBirth());
                    doc.nextTableCell(rformat);
                    doc.addText((!displayZeros && (range.nbChris == 0)) ? "-" : range.getNbChris());
                    doc.nextTableCell(rformat);
                    doc.addText((!displayZeros && (range.nbMarr == 0)) ? "-" : range.getNbMarr());
                    doc.nextTableCell(rformat);
                    doc.addText((!displayZeros && (range.nbDeath == 0)) ? "-" : range.getNbDeath());
                    doc.nextTableCell(rformat);
                    doc.addText((!displayZeros && (range.nbOther == 0)) ? "-" : range.getNbOther());
                    doc.nextTableCell(cformat);
                    doc.addText(range.getFirst());
                    doc.nextTableCell(cformat);
                    doc.addText(range.getLast());
                }
            }
            doc.endTable();
            // done
        }

        // Display legend on top
        if (flashListOptions.getDisplayLegend() == LEGEND_BOT) {
            displayLegend(doc);
        }

        // done with main output
        LOG.log(Level.INFO, NbBundle.getMessage(this.getClass(), "ReportFlashList.Completed"));

        return doc;

    } // end_of_start

    /**
     * Get location and sorting structure options from user
     */
    private boolean getFlashOptions(Gedcom gedcom) {
        
        // Get options from user
        flashListOptions = new FlashListPanel(gedcom);
        Object choice = DialogManager.create(NbBundle.getMessage(getClass(), "FlashListPanel.title"), flashListOptions)
                .setMessageType(DialogManager.PLAIN_MESSAGE)
                .setOptionType(DialogManager.OK_CANCEL_OPTION)
                .setDialogId("flashListWindow")
                .show();

        // Return if cancelled
        if (choice == DialogManager.CANCEL_OPTION) {
            return false;
        } else {
            flashListOptions.store();
        }
        
        return true;
        // done
    }

    /**
     * Header function
     */
    private void displayLegend(Document doc) {
        // Display legend
        // Display sorting key and location fields used
        doc.addText(" ");
        doc.nextParagraph();
        doc.addText(" ");
        doc.nextParagraph();
        doc.addText(" ");
        doc.nextParagraph();
        doc.addText(" ");
        doc.nextParagraph();
        doc.startSection(NbBundle.getMessage(this.getClass(), "ReportFlashList.legendText"), "legend");
        doc.addText(" ");
        doc.nextParagraph();
        doc.addText(NbBundle.getMessage(this.getClass(), "ReportFlashList.legendS"));
        doc.nextParagraph();
        doc.addText(NbBundle.getMessage(this.getClass(), "ReportFlashList.legendB"));
        doc.nextParagraph();
        doc.addText(NbBundle.getMessage(this.getClass(), "ReportFlashList.legendC"));
        doc.nextParagraph();
        doc.addText(NbBundle.getMessage(this.getClass(), "ReportFlashList.legendM"));
        doc.nextParagraph();
        doc.addText(NbBundle.getMessage(this.getClass(), "ReportFlashList.legendD"));
        doc.nextParagraph();
        doc.addText(NbBundle.getMessage(this.getClass(), "ReportFlashList.legendO"));
        doc.nextParagraph();
        doc.addText(NbBundle.getMessage(this.getClass(), "ReportFlashList.legendMin"));
        doc.nextParagraph();
        doc.addText(NbBundle.getMessage(this.getClass(), "ReportFlashList.legendMax"));
        doc.addText(" ");
        doc.nextParagraph();
        doc.addText(" ");
        doc.nextParagraph();
        doc.addText(" ");
        doc.nextParagraph();
        doc.addText(" ");
        doc.nextParagraph();
        // done
    }

    /**
     * Header function
     */
    private void displayHeader(Document doc, String name, boolean dHeader) {
        // Start table
        if (name != null) {
            doc.startTable("genj:csv=true,genj:csvprefix=" + name + ",width=100%");
        } else {
            doc.startTable("width=100%");
        }

        doc.addTableColumn("column-width=30%");     // secondary key (default is Names)
        doc.addTableColumn("column-width=23%");     // tertiary key (default is place)
        doc.addTableColumn("column-width=5%");      // Sosa nb
        doc.addTableColumn("column-width=5%");      // Birth nb
        doc.addTableColumn("column-width=5%");      // Christ. nb
        doc.addTableColumn("column-width=5%");      // Marriage nb
        doc.addTableColumn("column-width=5%");      // Death nb
        doc.addTableColumn("column-width=5%");      // Other nb
        doc.addTableColumn("column-width=10%");     // first year
        doc.addTableColumn("column-width=7%");     // last year

        // Display header row on top of the columns
        if (dHeader) {
            doc.nextTableRow();
            doc.addText(" ");
            doc.nextTableCell();
            doc.addText(" ");
            doc.nextTableCell(FORMAT_RBACKGROUND);
            doc.addText(NbBundle.getMessage(this.getClass(), "ReportFlashList.colS"));
            doc.nextTableCell(FORMAT_RBACKGROUND);
            doc.addText(NbBundle.getMessage(this.getClass(), "ReportFlashList.colB"));
            doc.nextTableCell(FORMAT_RBACKGROUND);
            doc.addText(NbBundle.getMessage(this.getClass(), "ReportFlashList.colC"));
            doc.nextTableCell(FORMAT_RBACKGROUND);
            doc.addText(NbBundle.getMessage(this.getClass(), "ReportFlashList.colM"));
            doc.nextTableCell(FORMAT_RBACKGROUND);
            doc.addText(NbBundle.getMessage(this.getClass(), "ReportFlashList.colD"));
            doc.nextTableCell(FORMAT_RBACKGROUND);
            doc.addText(NbBundle.getMessage(this.getClass(), "ReportFlashList.colO"));
            doc.nextTableCell(FORMAT_CBACKGROUND);
            doc.addText(NbBundle.getMessage(this.getClass(), "ReportFlashList.colMin"));
            doc.nextTableCell(FORMAT_CBACKGROUND);
            doc.addText(NbBundle.getMessage(this.getClass(), "ReportFlashList.colMax"));
        }
        // done
    }

    /**
     * Analyze an individual
     */
    private void analyze(Indi indi, Map <String, Object>primary, Indi indiDeCujus) {

        // consider non-empty last names only
        String name = indi.getLastName();
        if (name.length() == 0) {
            return;
        }
        name = name.trim();

        // determine is individual is an ancestor of De-Cujus
        boolean isSosa = indi.isAncestorOf(indiDeCujus);
        if (indi == indiDeCujus) {
            isSosa = true;
        }

        try {
            // loop over all dates in indi
            for (Iterator <PropertyDate>dates = indi.getProperties(PropertyDate.class).iterator(); dates.hasNext();) {
                // consider valid dates only
                PropertyDate date = dates.next();
                if (!date.isValid()) {
                    continue;
                }
                // compute first and last year
                int start = 0;
                start = date.getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
                int end = date.isRange() ? date.getEnd().getPointInTime(PointInTime.GREGORIAN).getYear() : start;
                if (start > end) {
                    continue;
                }
                // find all places for it
                analyzePlaces(name, start, end, date.getParent(), primary, isSosa);
                // find all cities for it
                analyzeCities(name, start, end, date.getParent(), primary, isSosa);

                // next date
            }

            // loop over all dates in family of indi
            for (Iterator <Fam>families = Arrays.asList(indi.getFamiliesWhereSpouse()).iterator();
                    families.hasNext();) {
                Fam family = families.next();
                for (Iterator <PropertyDate>dates = family.getProperties(PropertyDate.class).iterator(); dates.hasNext();) {
                    // consider valid dates only
                    PropertyDate date = dates.next();
                    if (!date.isValid()) {
                        continue;
                    }
                    // compute first and last year
                    int start = date.getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
                    int end = date.isRange() ? date.getEnd().getPointInTime(PointInTime.GREGORIAN).getYear() : start;
                    if (start > end) {
                        continue;
                    }
                    // find all places for it
                    analyzePlaces(name, start, end, date.getParent(), primary, isSosa);
                    // find all cities for it
                    analyzeCities(name, start, end, date.getParent(), primary, isSosa);
                } // next date
            } // next family

        } catch (Throwable t) {
            t.printStackTrace();
        }

        // done
    }

    /**
     * Analyze all cities for given indi, start, end & property
     */
    private void analyzeCities(String name, int start, int end, Property prop, Map <String, Object>primary, boolean isSosa) {
        // Consider places of events only
        if (!isEvent(prop)) {
            return;
        }

        Property[] cities = prop.getProperties(CITY);
        for (int c = 0; c < cities.length; c++) {
            // consider non-empty cities only
            String loc1 = cities[c].getDisplayValue().trim();
            if (loc1.length() == 0) {
                continue;
            }

            // if PLAC tag in the gedcom, use "-/-" for loc3/loc2 (we cannot guess the mapping
            // between jurisdictions and CITY,STAE,CTRY for sure!)
            // otherwise use "" for loc3/loc2
            String loc2 = "";
            String loc3 = "";
            String loc4 = "";
            if (existPLACTag) {
                loc2 = "-";
                loc3 = "-";
                loc4 = "-";
            }
            // keep it
            keep(loc1, loc2, loc3, loc4, name, start, end, primary, prop, isSosa);
            // next city
        }
        // done
    }

    /**
     * Analyze all places for given indi, start, end & property
     */
    private void analyzePlaces(String name, int start, int end, Property prop, Map <String, Object>primary, boolean isSosa) {
        // Consider places of events only
        if (!isEvent(prop)) {
            return;
        }

        // loop over places
        for (Iterator <PropertyPlace>places = prop.getProperties(PropertyPlace.class).iterator(); places.hasNext();) {
            // Get place
            PropertyPlace place = places.next();

            // if PLAC tag in the gedcom, use locations as per specified by user
            String loc1 = "";
            String loc2 = "";
            String loc3 = "";
            String loc4 = "";
            if (existPLACTag) {
                loc1 = place.getJurisdiction(posLoc1);
                loc2 = place.getJurisdiction(posLoc2);
                loc3 = place.getJurisdiction(posLoc3);
                loc4 = place.getJurisdiction(posLoc4);
                if (loc1 != null) {
                    loc1 = loc1.trim();
                }
                if (loc2 != null) {
                    loc2 = loc2.trim();
                }
                if (loc3 != null) {
                    loc3 = loc3.trim();
                }
                if (loc4 != null) {
                    loc4 = loc4.trim();
                }
                if ((loc1 == null) || (loc1.length() == 0)) {
                    loc1 = "-";
                }
                if ((loc2 == null) || (loc2.length() == 0)) {
                    loc2 = "-";
                }
                if ((loc3 == null) || (loc3.length() == 0)) {
                    loc3 = "-";
                }
                if ((loc4 == null) || (loc4.length() == 0)) {
                    loc4 = "-";
                }
                // consider non-empty places only
                if ((loc1 == "-") && (loc2 == "-") && (loc3 == "-") && (loc4 == "-")) {
                    continue;
                }
            } else {
                // if no PLAC tag in the gedcom, use only first available jurisdiction
                loc1 = place.getFirstAvailableJurisdiction().trim();
                // consider non-empty places only
                if (loc1.length() == 0) {
                    continue;
                }
            }
            // keep it
            keep(loc1, loc2, loc3, loc4, name, start, end, primary, prop, isSosa);
            // next place
        }
        // done
    }

    /**
     * Analyze all places for given indi, start, end & property
     */
    private boolean isEvent(Property prop) {
        // returns true if property is in list of events, false otherwise
        String strTable[] = {"ADOP", "ANUL", "BIRT", "BAPM", "BARM", "BASM", "BLES", "BURI", "CENS", "CHR", "CHRA", "CONF", "CREM", "DEAT", "DIV", "DIVF", "EMIG", "ENGA", "EVEN", "FCOM", "GRAD", "IMMI", "MARR", "MARB", "MARC", "MARL", "MARS", "NATU", "ORDN", "RETI", "PROB", "WILL"};
        List <String>listOfEvents = Arrays.asList(strTable);
        return listOfEvents.contains(prop.getTag());
        // done
    }

    private void keep(String loc1, String loc2, String loc3, String loc4, String name, int start, int end, Map <String, Object>primary, Property prop, boolean isSosa) {

        // calculate primary and secondary key
        String ps, ss, ts;
        switch (recordKey) {
            case LOC12_SURN_LOC34:
                ps = loc1 + (loc2.isEmpty() ? "" : "/" + loc2);
                ss = name;
                ts = loc3 + (loc4.isEmpty() ? "" : "/" + loc4);
                break;
            case LOC12_LOC34_SURN:
                ps = loc1 + (loc2.isEmpty() ? "" : "/" + loc2);
                ss = loc3 + (loc4.isEmpty() ? "" : "/" + loc4);
                ts = name;
                break;
            case SURN_LOC12_LOC34:
                ps = name;
                ss = loc1 + (loc2.isEmpty() ? "" : "/" + loc2);
                ts = loc3 + (loc4.isEmpty() ? "" : "/" + loc4);
                break;
            default:
                throw new IllegalArgumentException("no such report type");
        }

        // apply filters (if there is a filter and it does not match, return)
        if ((!filterKey1.trim().isEmpty()) && (!ps.toUpperCase().contains(filterKey1.toUpperCase()))) {
            return;
        }
        if ((!filterKey2.trim().isEmpty()) && (!ss.toUpperCase().contains(filterKey2.toUpperCase()))) {
            return;
        }
        if ((!filterKey3.trim().isEmpty()) && (!ts.toUpperCase().contains(filterKey3.toUpperCase()))) {
            return;
        }

        // remember
        if (!existPLACTag) {
            ts = "";
        }
        Map <String, Object>secondary = (Map) lookup(primary, ps, TreeMap.class);
        Map <String, Object>tertiary = (Map) lookup(secondary, ss, TreeMap.class);
        Range range = (Range) lookup(tertiary, ts, Range.class);
        range.add(start, end, isSosa, prop.getTag());
        // done
    }

    /**
     * Lookup an object in a map with a default class
     */
    private Object lookup(Map<String, Object> index, String key, Class <? extends Object>fallback) {
        // look up and create lazily if necessary
        Object result = index.get(key);
        if (result == null) {
            try {
                result = fallback.newInstance();
            } catch (Throwable t) {
                t.printStackTrace();
                throw new IllegalArgumentException("can't instantiate fallback " + fallback);
            }
            index.put(key, result);
        }
        // done
        return result;
    }

    /**
     * our ranges
     */
    static class Range {

        int firstYear = Integer.MAX_VALUE,
                lastYear = -Integer.MAX_VALUE,
                nbSosa = 0,
                nbBirth = 0,
                nbChris = 0,
                nbMarr = 0,
                nbDeath = 0,
                nbOther = 0;
        double geoLat = 0,
                geoLon = 0;

        void add(int start, int end, boolean isSosa, String tag) {
            // check for valid year - this might still be UNKNOWN even though a date was valid
            if (start != PointInTime.UNKNOWN) {
                firstYear = Math.min(firstYear, start);
            }
            if (end != PointInTime.UNKNOWN) {
                lastYear = Math.max(lastYear, end);
            }
            if (isSosa) {
                nbSosa++;
            }
            if (tag == "BIRT") {
                nbBirth++;
            } else if (tag == "CHR") {
                nbChris++;
            } else if (tag == "MARR") {
                nbMarr++;
            } else if (tag == "DEAT") {
                nbDeath++;
            } else {
                nbOther++;
            }
        }

        void add(Range rangeElt) {
            firstYear = Math.min(firstYear, rangeElt.getValueFirst());
            lastYear = Math.max(lastYear, rangeElt.getValueLast());
            nbSosa += rangeElt.getValueSosa();
            nbBirth += rangeElt.getValueBirth();
            nbChris += rangeElt.getValueChris();
            nbMarr += rangeElt.getValueMarr();
            nbDeath += rangeElt.getValueDeath();
            nbOther += rangeElt.getValueOther();
        }

        void setGeo(double lat, double lon) {
            geoLat = lat;
            geoLon = lon;
        }

        String getFirst() {
            // check for valid year - this might still be UNKNOWN even though a date was valid
            if (firstYear == Integer.MAX_VALUE || lastYear == Integer.MAX_VALUE) {
                return "";
            }
            return Integer.toString(firstYear);
        }

        String getLast() {
            return Integer.toString(lastYear);
        }

        String getNbSosa() {
            return Integer.toString(nbSosa);
        }

        String getNbBirth() {
            return Integer.toString(nbBirth);
        }

        String getNbChris() {
            return Integer.toString(nbChris);
        }

        String getNbMarr() {
            return Integer.toString(nbMarr);
        }

        String getNbDeath() {
            return Integer.toString(nbDeath);
        }

        String getNbOther() {
            return Integer.toString(nbOther);
        }

        int getNbEvents() {
            return (nbBirth + nbChris + nbMarr + nbDeath + nbOther);
        }

        int getYearSpan() {
            return (lastYear - firstYear);
        }

        int getValueFirst() {
            return (firstYear);
        }

        int getValueLast() {
            return (lastYear);
        }

        int getValueSosa() {
            return (nbSosa);
        }

        int getValueBirth() {
            return (nbBirth);
        }

        int getValueChris() {
            return (nbChris);
        }

        int getValueMarr() {
            return (nbMarr);
        }

        int getValueDeath() {
            return (nbDeath);
        }

        int getValueOther() {
            return (nbOther);
        }

        double getValueLat() {
            return (geoLat);
        }

        double getValueLon() {
            return (geoLon);
        }
    }
} // End_of_Report

