/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.webbook.creator;

import ancestris.modules.webbook.WebBook;
import ancestris.modules.webbook.WebBookParams;
import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Ancestris
 * @author Frederic Lapeyre <frederic@ancestris.org>
 * @version 0.1
 */
public class WebCitiesDetails extends WebSection {

    /**
     * Constructor
     */
    public WebCitiesDetails(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    public void init() {
        init(trs("TXT_Citiesdetails"), "citiesdetails", "citiesdetails_", formatFromSize(wh.getNbIndis()), 1, sizeIndiSection);
        calcPages();
    }

    /**
     * Report's entry point
     */
    @Override
    public void create() {

        // Preliminary build of sources link for links from details to sources
        if (wb.sectionIndividualsDetails != null) {
            personPage = wb.sectionIndividualsDetails.getPagesMap();
            prefixPersonDetailsDir = buildLinkShort(this, wb.sectionIndividualsDetails);
        }

        // Generate detail pages
        File dir = wh.createDir(wh.getDir().getAbsolutePath() + File.separator + sectionDir, true);
        exportData(dir);

    }

    /**
     * Exports data for page
     */
    private void exportData(File dir) {

        List <String>cities = wh.getCities(wh.gedcom);
        // Go through cities
        String fileStr = "";
        File file = null;
        PrintWriter out = null;
        String cityfile = "";
        int cpt = 0;
        int nbCities = cities.size();
        int previousPage = 0,
                currentPage = 0,
                nextPage = 0,
                lastPage = (nbCities / nbPerPage) + 1;

        for (Iterator<String> it = cities.iterator(); it.hasNext();) {
            String city = it.next();
            cpt++;
            currentPage = (cpt / nbPerPage) + 1;
            previousPage = (currentPage == 1) ? 1 : currentPage - 1;
            nextPage = (currentPage == lastPage) ? currentPage : currentPage + 1;
            cityfile = sectionPrefix + String.format(formatNbrs, currentPage) + sectionSuffix;
            if (fileStr.compareTo(cityfile) != 0) {
                if (out != null) {
                    exportLinks(out, sectionPrefix + String.format(formatNbrs, currentPage - 1) + sectionSuffix, 1, Math.max(1, previousPage - 1), currentPage == lastPage ? lastPage : nextPage - 1, lastPage);
                    printCloseHTML(out);
                    out.close();
                    wh.log.write(fileStr + trs("EXEC_DONE"));
                }
                fileStr = cityfile;
                file = wh.getFileForName(dir, cityfile);
                out = wh.getWriter(file, UTF8);
                printOpenHTML(out, "TXT_Citiesdetails", this);
            }
            exportLinks(out, cityfile, 1, previousPage, nextPage, lastPage);
            exportCityDetails(out, city);
            // .. next city
        }
        if (out != null) {
            exportLinks(out, cityfile, 1, previousPage, nextPage, lastPage);
            printCloseHTML(out);
            wh.log.write(fileStr + trs("EXEC_DONE"));
        }

        // done
        if (out != null) {
            out.close();
        }
    }

    /**
     * Exports city details
     */
    @SuppressWarnings("unchecked")
    private void exportCityDetails(PrintWriter out, String city) {

        // City name
        out.println("<h2 class=\"unk\"><a name=\"" + htmlAnchorText(city) + "\"></a>" + htmlText(city) + "</h2>");

        // All city properties that have that city
        List<Property> listProps = wh.getCitiesProps(city);
        Collections.sort(listProps, sortEvents);
        String lastFullname = "";
        boolean first = true;
        for (Iterator<Property> p = listProps.iterator(); p.hasNext();) {
            Property prop = p.next();
            if ((prop == null) || (prop.getValue().length() == 0)) {
                continue;
            }
            String fullname = getFullname(prop);
            boolean change = false;
            if (fullname.compareTo(lastFullname) != 0) {
                change = true;
            }
            lastFullname = fullname;
            // Case of a change and a place, display formatted place
            if (change) {
                if (!first) {
                    out.println("<span class=\"spacer\">" + SPACE + "</span></div>");
                    out.println("<div class=\"spacer\">" + SPACE + "</div>");
                    out.println("</div>");
                }
                out.println("<div class=\"citycont\">");
                out.println("<div class=\"citycont1\">");
                first = false;
                if (prop instanceof PropertyPlace) {
                    displayPlace(out, city, prop);
                }
                displayHeader(out);
            }
            displayEvent(out, prop);
        }
        out.println("<span class=\"spacer\">" + SPACE + "</span></div>");
        out.println("<div class=\"spacer\">" + SPACE + "</div>");
        out.println("</div>");
    }
    /**
     * Comparator to sort events for a city
     */
    private Comparator<Property> sortEvents = new Comparator<Property>() {

        public int compare(Property prop1, Property prop2) {
            if ((prop1 == null) && (prop2 != null)) {
                return -1;
            }
            if ((prop1 != null) && (prop2 == null)) {
                return +1;
            }
            if ((prop1 == null) && (prop2 == null)) {
                return 0;
            }

            // If fullnames different, return sorted strings
            String fullname1 = getFullname(prop1);
            String fullname2 = getFullname(prop2);
            if (fullname1 == null && fullname2 != null) {
                return -1;
            }
            if (fullname1 != null && fullname2 == null) {
                return +1;
            }
            if (fullname1 == null && fullname2 == null) {
                return 0;
            }
            if (fullname1.compareTo(fullname2) != 0) {
                return fullname1.compareTo(fullname2);
            }

            // Otherwise, sort on dates
            PropertyDate date1 = getDate(prop1);
            PropertyDate date2 = getDate(prop2);
            if (date1 == null && date2 != null) {
                return -1;
            }
            if (date1 != null && date2 == null) {
                return +1;
            }
            if (date1 == null && date2 == null) {
                return 0;
            }
            if (date1.compareTo(date2) != 0) {
                return date1.compareTo(date2);
            }

            // Otherwise, sort on individuals
            Entity ent1 = prop1.getEntity();
            Entity ent2 = prop2.getEntity();

            if (ent1 == null && ent2 != null) {
                return -1;
            }
            if (ent1 != null && ent2 == null) {
                return +1;
            }
            if (ent1 == null && ent2 == null) {
                return 0;
            }
            if (ent1.toString().compareTo(ent2.toString()) != 0) {
                return ent1.toString().compareTo(ent2.toString());
            }
            return ent1.getId().compareTo(ent2.getId());
        }
    };

    /**
     * Get fullname of place
     */
    public String getFullname(Property prop) {
        return (prop instanceof PropertyPlace) ? prop.toString().trim() : "";
    }

    /**
     * Get city
     */
    public String getCity(Property prop) {
        return (prop instanceof PropertyPlace) ? ((PropertyPlace) prop).getCity().trim() : "";
    }

    /**
     * Get country
     */
    public String getCountry(Property prop) {
        String ctry = "";
        if (prop instanceof PropertyPlace) {
            String[] dataBits = prop.toString().split("\\,", -1);
            ctry = dataBits[dataBits.length - 1].trim();
        }
        return ctry;
    }

    /**
     * Get date of event of place
     */
    public PropertyDate getDate(Property prop) {
        Property date = null;
        if (prop instanceof PropertyPlace) {
            Property parent = prop.getParent();
            if (parent != null) {
                date = parent.getProperty("DATE");
            }
        } else {
            Property parent = prop.getParent();
            Property gparent = null;
            if (parent != null) {
                gparent = parent.getParent();
            }
            if (gparent != null) {
                date = gparent.getProperty("DATE");
            }
        }
        if (date instanceof PropertyDate) {
            return (PropertyDate) date;
        } else {
            return null;
        }
    }

    /**
     * Display formatted place
     */
    private void displayPlace(PrintWriter out, String city, Property prop) {
        out.println("<p class=\"cityloc\"><span class=\"gras\">" + htmlText(trs("place_loc")) + "</span></p>");
        out.println("<span class=\"cityloc1\">");
        //String[] placeBits = gedcom.getPlaceFormat().split("\\,", -1);
        String[] dataBits = prop.toString().split("\\,", -1);
        boolean display = false;
        for (int i = 0; i < dataBits.length; i++) {
            if (dataBits[i].length() > 0) {
                out.println(htmlText(dataBits[i]));
                if (!display) {
                    displayLink2Map(out, prop, city);
                    display = true;
                }
                out.println("<br />");
            }
        }
        out.println("</span></div>");
    }

    /**
     * Display link to map if needed
     */
    private void displayLink2Map(PrintWriter out, Property prop, String city) {
        if (wp.param_media_GeneMap.equals("1")) {
            out.println(SPACE + SPACE + "<a href=\"../map/map" + (wp.param_PHP_Support.equals("1") ? ".php" : ".html") + "?" + htmlAnchorText(getFullname(prop)) + "\"><img src=\"../" + themeDir + "/map.gif\" alt=\"" + htmlText(city) + "\" title=\"" + htmlText(trs("map_of", city)) + "\"/></a>");
        }
    }

    /**
     * Display event header
     */
    private void displayHeader(PrintWriter out) {
        out.println("<div class=\"citycont2\">");
        out.println("<p class=\"cityevt\"><span class=\"gras\">" + htmlText(trs("place_event")) + "</span></p>");
    }

    /**
     * Display event
     */
    private void displayEvent(PrintWriter out, Property prop) {
        Property parent = prop.getParent();
        Property event = null;
        if (parent == null) {
            return;
        }
        if (prop instanceof PropertyPlace) {
            event = parent;
        } else {
            event = parent.getParent();
        }
        if (event == null) {
            return;
        }
        out.println("<span class=\"cityevt1\">" + wrapEventDate(getDate(prop)) + "</span>");
        out.println("<span class=\"cityevt2\">" + wrapPropertyName(event) + "</span>");
        out.println("<span class=\"cityevt3\">" + wrapEntity(prop.getEntity()) + "</span>");
        out.println("<span class=\"spacer\">" + SPACE + "</span>");
    }

    /**
     * Calculate pages for city details
     */
    private void calcPages() {
        String cityfile = "", fileStr = "";
        int cpt = 0;
        for (Iterator<String> it = wh.getCities(wh.gedcom).iterator(); it.hasNext();) {
            String city = it.next();
            cpt++;
            cityfile = sectionPrefix + String.format(formatNbrs, (cpt / nbPerPage) + 1) + sectionSuffix;
            if (fileStr.compareTo(cityfile) != 0) {
                fileStr = cityfile;
            }
            cityPage.put(htmlAnchorText(city), cityfile);
        }
    }

    /**
     * Provide links map to outside caller
     */
    public Map<String, String> getPagesMap() {
        return cityPage;
    }
} // End_of_Report

