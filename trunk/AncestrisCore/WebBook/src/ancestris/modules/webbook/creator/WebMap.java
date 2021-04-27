/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.webbook.creator;

import ancestris.api.place.Place;
import ancestris.api.place.PlaceFactory;
import ancestris.core.pluginservice.PluginInterface;
import ancestris.modules.webbook.WebBook;
import ancestris.modules.webbook.WebBookParams;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.time.PointInTime;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import org.openide.util.Lookup;

/**
 * Ancestris
 * @author Frederic Lapeyre <frederic@ancestris.org>
 * @version 0.1
 */
public class WebMap extends WebSection {

    private Class<?> clazz = null;
    private int geoProblems = 0;

    /**
     * Constructor
     */
    public WebMap(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    public void init() {
        init(trs("TXT_Map"), "map", "map", "", 1, 0);
        if (!isModuleGeo()) {
            toBeGenerated = false;
            wh.log.write(trs("LOG_Geo_module_Not_Found"));
        }
    }

    private boolean isModuleGeo() {
        boolean found = false;
        for (PluginInterface sInterface : Lookup.getDefault().lookupAll(PluginInterface.class)) {
            try {
                if (sInterface.getPluginName().equals("ancestris.modules.geo")) {
                    found = true;
                    clazz = sInterface.getClass();
                }
            } catch (Throwable e) {
            }
        }
        return found;
    }

    /**
     * Section's entry point
     */
    @Override
    @SuppressWarnings("unchecked")
    public void create() {

        File dir = wh.createDir(wh.getDir().getAbsolutePath() + ((sectionDir.length() == 0) ? "" : File.separator + sectionDir), true);


        if (wb.sectionCitiesDetails != null) {
            cityPage = wb.sectionCitiesDetails.getPagesMap();
            prefixPersonDetailsDir = buildLinkShort(this, wb.sectionCitiesDetails);
        }

        exportPage(dir);

        exportXMLData(dir, wh.getIndiDeCujus(wp.param_decujus));
    }

    /**
     * Exports page
     */
    private void exportPage(File dir) {

        // Opens page
        String fileStr = sectionPrefix + String.format(formatNbrs, 1) + sectionSuffix;
        File file = wh.getFileForName(dir, fileStr);
        PrintWriter out = wh.getWriter(file, UTF8);
        if (out == null) {
            return;
        }

        printOpenHTMLHead(out, "TXT_Map", this, true);
        
        out.println("<script src=\"./map-markers.js\" ></script>");

        // include javascript
        String javascriptDir = "js/";
        try {
            String str = wh.readStream(javascriptDir + "map.js");
            out.println("<script type=\"text/javascript\">");
            out.println(filter(str));
            out.println("</script>");
        } catch (IOException e) {
            //e.printStackTrace();
            wb.log.write(wb.log.ERROR, "exportPage - " + e.getMessage());
        }

        // include body declaration and title
        out.println("<div class=\"title\"><a name=\"top\">" + SPACE + "</a>" + htmlText(trs("TXT_Map")) + "</div>");
        printHomeLink(out, this);

        // Include page itself
        out.println("<div id=\"map\" class=\"map\" style=\"height: 600px\"></div>");

        // Closes page
        printCloseHTML(out);

        wh.log.write(fileStr + trs("EXEC_DONE"));
        out.close();

    }

    /**
     * Exports XML data
     *
     */
    private class CityFlash {
        // key

        String fullName = "";
        // city info unique to city
        String city = "";
        String country = "";
        String linkToPage = "";
        String linkAnchor = "";
        double lng = 0;
        double lat = 0;
        int density = 0;
        // ancestors related to events
        Set<Indi> ascendants = new HashSet<Indi>();
        Set<Indi> cousins = new HashSet<Indi>();
        Set<Indi> others = new HashSet<Indi>();
        Map<String, Integer> names = new TreeMap<String, Integer>();
        // events information
        int nbBirths = 0;
        int nbMarriages = 0;
        int nbDeaths = 0;
        int nbOther = 0;
        PropertyDate minDate = null;
        PropertyDate maxDate = null;
        // derived
        String size = "";
        String ancestor = "";
        String type = "";
        String text = "";
        int min = 0;
        int max = 0;
    }
    private Map<String, CityFlash> citiesFlash = new TreeMap<String, CityFlash>();               // key is fullName
    private Set<Indi> ancestors = null;
    private Set<Indi> cousins = null;

    /**
     *  Main export function for the data
     */
    private void exportXMLData(File dir, Indi indi) {

        // Opens page
        String fileStr = "map-markers.js";
        File file = wh.getFileForName(dir, fileStr);
        PrintWriter out = wh.getWriter(file, UTF8);
        if (out == null) {
            return;
        }

        // Get ancestor of sosa #1 of webbook
        ancestors = wh.getAncestors(indi);
        wh.log.write("Number of ascendants: " + ancestors.size());

        // Get cousins of sosa #1 of webbook
        cousins = wh.getCousins(indi);
        wh.log.write("Number of cousins: " + cousins.size());

        // Calculate data
        calculateCitiesFlash();

        // Export data
        exportCitiesFlash(out);

        // Closes page
        wh.log.write(fileStr + trs("EXEC_DONE"));
        out.close();

    }

    /**
     *  Does all the calculations
     */
    private void calculateCitiesFlash() {

        geoProblems = 0;

        // Creates the citiesFlash records
        for (String city : wh.getCities(wh.gedcom)) {
            List<Property> listProps = wh.getCitiesProps(city);
            for (Property prop : listProps) {
                if ((prop == null) || (prop.getValue().length() == 0)) {
                    continue;
                }
                String fullname = wb.sectionCitiesDetails.getFullname(prop);
                CityFlash cityFlash = citiesFlash.get(fullname);
                if (cityFlash == null) {
                    cityFlash = createCityFlashRecord(fullname, prop);
                }
                addDetails2cityFlashRecord(cityFlash, prop);
                citiesFlash.put(fullname, cityFlash);
            }
        }
        if (geoProblems != 0) {
            wh.log.write(trs("LOG_LocationsUnfound", geoProblems));
        }

        // Calculates max volumes for later
        int maxVolume = 0;
        for (String city : citiesFlash.keySet()) {
            CityFlash cityFlash = citiesFlash.get(city);
            if (cityFlash != null) {
                Integer total = (Integer) (cityFlash.nbBirths + cityFlash.nbMarriages + cityFlash.nbDeaths + cityFlash.nbOther);
                if (total > maxVolume) {
                    maxVolume = total;
                }
            }
        }
        // Calculates derived measures
        for (String city : citiesFlash.keySet()) {
            CityFlash cityFlash = citiesFlash.get(city);
            if (cityFlash != null) {
                calculateMeasures(cityFlash, maxVolume);
            }
        }

        // Calculates density of points
        calculateDensity();

    }

    /**
     *  Creates cityFlash record
     */
    private CityFlash createCityFlashRecord(String fullname, Property prop) {
        CityFlash cityFlash = new CityFlash();
        cityFlash.fullName = fullname;
        cityFlash.city = wb.sectionCitiesDetails.getCity(prop);
        cityFlash.country = wb.sectionCitiesDetails.getCountry(prop);
        cityFlash.linkToPage = wb.sectionCitiesDetails.getPagesMap().get(htmlAnchorText(cityFlash.city));
        cityFlash.linkAnchor = htmlAnchorText(cityFlash.fullName);
        if (!findLocation(cityFlash, prop)) {
            geoProblems++;
        }
        return cityFlash;
    }

    /**
     *  Look for geoLocations from property
     */
    private boolean findLocation(CityFlash cf, Property prop) {
        // Init default value
        cf.lat = +45;
        cf.lng = -4;
        //
        // Check invalid values
        if ((clazz == null) || !(prop instanceof PropertyPlace)) {
            return false;
        }
        // Get location from gedcom itsel
        PropertyPlace pPlace = (PropertyPlace) prop;
        if (pPlace.getLongitude(true) != null && pPlace.getLatitude(true) != null) {
            cf.lat = pPlace.getLatitude(true).getDoubleValue();
            cf.lng = pPlace.getLongitude(true).getDoubleValue();
            return true;
        }
        
        // If not found, get location from local file
        Place place = PlaceFactory.getLocalPlace(pPlace);
        if (place == null) {
            return false;
        }
        cf.lat = place.getLatitude();
        cf.lng = place.getLongitude();
        return true;


    }

    /**
     *  Addd details of event to cityFlash Record
     */
    private void addDetails2cityFlashRecord(CityFlash cityFlash, Property prop) {

        // Get individuals related to this event
        Entity ent = prop.getEntity();
        if (ent instanceof Indi) {
            addIndividual(cityFlash, (Indi) ent);
        }
        if (ent instanceof Fam) {
            addIndividual(cityFlash, ((Fam) ent).getHusband());
            addIndividual(cityFlash, ((Fam) ent).getWife());
        }

        // Get event type
        Property parent = prop.getParent();
        Property event = null;
        if (parent != null) {
            if (prop instanceof PropertyPlace) {
                event = parent;
            } else {
                event = parent.getParent();
            }
        }
        if (event != null) {
            addEvent(cityFlash, event.getTag());
        }

        // Get date of event
        addDate(cityFlash, wb.sectionCitiesDetails.getDate(prop));

    }

    private void addIndividual(CityFlash cityFlash, Indi indi) {
        if (ancestors.contains(indi)) {
            cityFlash.ascendants.add(indi);
        } else if (cousins.contains(indi)) {
            cityFlash.cousins.add(indi);
        } else {
            cityFlash.others.add(indi);
        }
        Integer counter = cityFlash.names.get(wh.getLastName(indi, DEFCHAR));
        if (counter == null) {
            counter = 0;
        }
        counter++;
        cityFlash.names.put(wh.getLastName(indi, DEFCHAR), counter);
    }

    private void addEvent(CityFlash cityFlash, String tag) {
        if (tag.compareTo("BIRT") == 0) {
            cityFlash.nbBirths++;
        } else if (tag.compareTo("MARR") == 0) {
            cityFlash.nbMarriages++;
        } else if (tag.compareTo("DEAT") == 0) {
            cityFlash.nbDeaths++;
        } else {
            cityFlash.nbOther++;
        }
    }

    private void addDate(CityFlash cityFlash, PropertyDate pDate) {
        if (pDate == null) {
            return;
        }
        if (cityFlash.minDate == null) {
            cityFlash.minDate = pDate;
        }
        if (cityFlash.maxDate == null) {
            cityFlash.maxDate = pDate;
        }
        if (pDate.compareTo(cityFlash.minDate) < 0) {
            cityFlash.minDate = pDate;
        }
        if (pDate.compareTo(cityFlash.maxDate) > 0) {
            cityFlash.maxDate = pDate;
        }
    }

    /**
     *  Calculates measures
     */
    private void calculateMeasures(CityFlash cityFlash, int maxVolume) {
        // Size
        int total = cityFlash.nbBirths + cityFlash.nbMarriages + cityFlash.nbDeaths + cityFlash.nbOther;
        cityFlash.size = (total > (maxVolume / 3)) ? "h" : (total > (maxVolume / 9)) ? "m" : "l";

        // Ancestor
        if (cityFlash.ascendants.size() > 0) {
            cityFlash.ancestor = "s";
        } else if (cityFlash.cousins.size() > 0) {
            cityFlash.ancestor = "c";
        } else {
            cityFlash.ancestor = "o";
        }
        String lastnames = getLastNames(cityFlash.names);

        // type
        cityFlash.type = "";
        if (cityFlash.nbBirths > 0) {
            cityFlash.type += "b";
        }
        if (cityFlash.nbMarriages > 0) {
            cityFlash.type += "m";
        }
        if (cityFlash.nbDeaths > 0) {
            cityFlash.type += "d";
        }
        if (cityFlash.type.length() == 0) {
            cityFlash.type = "x";
        }

        // min and max year
        try {
            if (cityFlash.minDate != null) {
                cityFlash.min = cityFlash.minDate.getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
            }
            if (cityFlash.maxDate != null) {
                if (cityFlash.maxDate.isRange()) {
                    cityFlash.max = cityFlash.maxDate.getEnd().getPointInTime(PointInTime.GREGORIAN).getYear();
                } else {
                    cityFlash.max = cityFlash.maxDate.getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
                }
            }
        } catch (GedcomException e) {
            wb.log.write(wb.log.ERROR, "calculateMeasures - " + e.getMessage());
        }

        // text
        String text = "";
        text += trs("map_box_city") + " " + cityFlash.city + ", " + cityFlash.country + " (" + getCoordinateAsString(cityFlash.lng, cityFlash.lat) + ").;";

        text += Integer.toString(cityFlash.ascendants.size() + cityFlash.cousins.size() + cityFlash.others.size()) + " " + trs("map_box_individual") + ": ";
        int cpt = 0;
        if (cityFlash.ascendants.size() > 0) {
            text += Integer.toString(cityFlash.ascendants.size()) + " " + trs("map_box_ascendants");
        }
        cpt += cityFlash.ascendants.size();
        if (cityFlash.cousins.size() > 0) {
            text += (cpt > 0 ? ", " : "") + Integer.toString(cityFlash.cousins.size()) + " " + trs("map_box_cousins");
        }
        cpt += cityFlash.cousins.size();
        if (cityFlash.others.size() > 0) {
            text += (cpt > 0 ? ", " : "") + Integer.toString(cityFlash.others.size()) + " " + trs("map_box_others");
        }
        text += ".;";

        if (lastnames.trim().length() != 0) {
            text += trs("map_box_most") + ": " + lastnames + ".;";
        }

        text += Integer.toString(cityFlash.nbBirths + cityFlash.nbMarriages + cityFlash.nbDeaths + cityFlash.nbOther) + " " + trs("map_box_events") + ": ";
        cpt = 0;
        if (cityFlash.nbBirths > 0) {
            text += Integer.toString(cityFlash.nbBirths) + " " + trs("map_box_births");
        }
        cpt += cityFlash.nbBirths;
        if (cityFlash.nbMarriages > 0) {
            text += (cpt > 0 ? ", " : "") + Integer.toString(cityFlash.nbMarriages) + " " + trs("map_box_marriages");
        }
        cpt += cityFlash.nbMarriages;
        if (cityFlash.nbDeaths > 0) {
            text += (cpt > 0 ? ", " : "") + Integer.toString(cityFlash.nbDeaths) + " " + trs("map_box_deaths");
        }
        cpt += cityFlash.nbDeaths;
        if (cityFlash.nbOther > 0) {
            text += (cpt > 0 ? ", " : "") + Integer.toString(cityFlash.nbOther) + " " + trs("map_box_others");
        }
        text += ".;";

        if (cityFlash.minDate != null && cityFlash.minDate.compareTo(cityFlash.maxDate) == 0) {
            text += trs("map_box_occon") + " \"" + cityFlash.minDate.getDisplayValue() + "\".";
        } else if (cityFlash.minDate != null && cityFlash.maxDate != null) {
            text += trs("map_box_occbet") + " \"" + cityFlash.minDate.getDisplayValue() + "\" " + trs("map_box_occand") + " \"" + cityFlash.maxDate.getDisplayValue() + "\".";
        }
        cityFlash.text = text;
    }

    /**
     *  Get top 5 most frequent lastnames and take privacy into account
     */
    private String getLastNames(Map<String, Integer> map) {
        String output = "";
        for (int i = 0; i < 5; i++) {
            Integer max = 0;
            String maxname = "";
            for (String name : map.keySet()) {
                Integer counter = map.get(name);
                if (counter > max) {
                    max = counter;
                    maxname = name;
                }
            }
            if (maxname.length() != 0) {
                output += (i == 0 ? "" : ", ") + maxname;
                map.put(maxname, 0);
            }
        }
        return output;
    }

    /**
     *  Get coordinates as string
     */
    public String getCoordinateAsString(double lon, double lat) {
        if (Double.isNaN(lat) || Double.isNaN(lon)) {
            return "n/a";
        }
        char we = 'E', ns = 'N';
        if (lat < 0) {
            lat = -lat;
            ns = 'S';
        }
        if (lon < 0) {
            lon = -lon;
            we = 'W';
        }
        DecimalFormat format = new DecimalFormat("0.0");
        return ns + format.format(lat) + " " + we + format.format(lon);
    }

    /**
     *  Does the export to XML
     */
    private void exportCitiesFlash(PrintWriter out) {
        out.println("function getMarkers() {");
        out.println("var obj = [");
        
        

        for (String city : citiesFlash.keySet()) {
            CityFlash cityFlash = citiesFlash.get(city);
            if (cityFlash != null && (wp.param_media_DispUnknownLoc.equals("1") || cityFlash.lng != -45 || cityFlash.lat != 30)) {
                StringBuilder line = new StringBuilder();
                line.append("{\n ");
                line.append("\"x\": \"").append(cityFlash.lng).append("\", \n ");
                line.append("\"y\":\"").append(cityFlash.lat).append("\", \n ");
                line.append("\"s\":\"").append(cityFlash.size).append("\", \n ");
                line.append("\"a\":\"").append(cityFlash.ancestor).append("\", \n ");
                line.append("\"t\":\"").append(cityFlash.type).append("\", \n ");
                line.append("\"d\":\"").append(cityFlash.density).append("\", \n ");
                line.append("\"min\":\"").append(cityFlash.min).append("\", \n ");
                line.append("\"max\":\"").append(cityFlash.max).append("\", \n ");
                line.append("\"lkp\":\"").append(cityFlash.linkToPage).append("\", \n ");
                line.append("\"lki\":\"").append(cityFlash.linkAnchor).append("\", \n ");
                line.append("\"lko\":\"").append(htmlAnchorText(cityFlash.city)).append("\", \n ");
                line.append("\"cty\":\"").append(cityFlash.city).append("\", \n ");
                line.append("\"text\":\"").append(cityFlash.text.replaceAll("\"",  Matcher.quoteReplacement("\\\""))).append("\" \n ");
                line.append("},\n");
                
                out.println(line);
            }
        }
        out.println("];");
        out.println("return obj;\n}");
    }

    /**
     *  Calculates measures
     */
    private void calculateDensity() {
        // Build city[i]
        String[] city = new String[citiesFlash.keySet().size()];
        citiesFlash.keySet().toArray(city);

        // Build distance[i][j] between city[i] and city[j] and get average
        int totalDistance = 0;
        int counter = 0;
        double[][] distance = new double[city.length][city.length];
        for (int i = 0; i < city.length; i++) {
            if (city[i].compareTo("") == 0) {
                continue;
            }
            CityFlash cityFlashi = citiesFlash.get(city[i]);
            if (cityFlashi == null) {
                city[i] = "";
            }
            for (int j = 0; j < i; j++) {
                if (city[j].compareTo("") == 0) {
                    continue;
                }
                CityFlash cityFlashj = citiesFlash.get(city[j]);
                if (cityFlashj == null) {
                    city[j] = "";
                }
                distance[i][j] = Math.pow(cityFlashi.lng - cityFlashj.lng, 2) + Math.pow(cityFlashi.lat - cityFlashj.lat, 2);
                totalDistance += distance[i][j];
                counter++;
            }
        }
        double averageDistance = (counter == 0) ? 0 : totalDistance / counter;

        // For all distances lower than average distance, remove smallest volume city (city[i] = "")
        checkDistances(Math.sqrt(averageDistance) / 60, "1", city, distance);
        checkDistances(Math.sqrt(averageDistance) / 30, "2", city, distance);

        // Allocate resulting density
        int i = 0;
        for (String name : citiesFlash.keySet()) {
            CityFlash cityFlash = citiesFlash.get(name);
            if (city[i].compareTo("1") == 0) {
                cityFlash.density = 1;
            } else if (city[i].compareTo("2") == 0) {
                cityFlash.density = 2;
            } else {
                cityFlash.density = 3;
            }
            i++;
        }

    }

    /**
     *  Check distances
     */
    private void checkDistances(double threshold, String value, String[] city, double[][] distance) {
        for (int i = 0; i < city.length; i++) {
            if (city[i].length() == 1) {
                continue;
            }
            for (int j = 0; j < i; j++) {
                if (city[j].length() == 1) {
                    continue;
                }
                if (distance[i][j] < threshold) {
                    city[getBiggerCity(i, j, city[i], city[j])] = value;
                    continue;
                }
            }
        }
    }

    /**
     *  Get bigger city
     */
    private int getBiggerCity(int i, int j, String cityI, String cityJ) {
        CityFlash cityFlashi = citiesFlash.get(cityI);
        CityFlash cityFlashj = citiesFlash.get(cityJ);
        if (cityFlashi == null) {
            return i;
        }
        if (cityFlashj == null) {
            return j;
        }
        if (cityFlashi.ascendants.size() > cityFlashj.ascendants.size()) {
            return j;
        } else if (cityFlashi.ascendants.size() < cityFlashj.ascendants.size()) {
            return i;
        }
        if (cityFlashi.cousins.size() > cityFlashj.cousins.size()) {
            return j;
        } else if (cityFlashi.cousins.size() < cityFlashj.cousins.size()) {
            return i;
        }
        int totali = cityFlashi.nbBirths + cityFlashi.nbMarriages + cityFlashi.nbDeaths + cityFlashi.nbOther;
        int totalj = cityFlashj.nbBirths + cityFlashj.nbMarriages + cityFlashj.nbDeaths + cityFlashj.nbOther;
        if (totali > totalj) {
            return j;
        } else {
            return i;
        }
    }

    /**
     * Read input file and put into string
     */
    private String filter(String inputStr) {
        String text = inputStr.replaceAll("detailed_events", trs("map_detailed_events"));
        return text;
    }
} // End_of_Report

