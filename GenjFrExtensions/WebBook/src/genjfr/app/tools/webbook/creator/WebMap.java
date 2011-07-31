/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package genjfr.app.tools.webbook.creator;

import genj.gedcom.Indi;
import genj.gedcom.Fam;
import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.time.PointInTime;
import genj.gedcom.GedcomException;

import ancestris.core.pluginservice.PluginInterface;
import genjfr.app.tools.webbook.WebBook;
import genjfr.app.tools.webbook.WebBookParams;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import java.text.DecimalFormat;
import java.util.StringTokenizer;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * Ancestris
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebMap extends WebSection {

    private Class clazz = null;
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
            wh.log.write(trs("Geo_module_Not_Found"));
        }
    }

    private boolean isModuleGeo() {
        boolean found = false;
        for (PluginInterface sInterface : Lookup.getDefault().lookupAll(PluginInterface.class)) {
            try {
                if (sInterface.getPluginDisplayName().equals("Geo")) {
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

        printOpenHTMLHead(out, "TXT_Map", this);

        // include style element to ensure vertical sizing of maps in conjunction with body height
        String mapKey = wp.param_media_GoogleKey.isEmpty() ? "abcdefg" : wp.param_media_GoogleKey;
        if (mapKey == null) {
            mapKey = "not_found";
        }
        out.println("<script src=\"http://maps.google.com/maps?file=api&amp;v=2&amp;key=" + mapKey + "\" type=\"text/javascript\"></script>");

        // include javascript
        String javascriptDir = "js/";
        try {
            String str = wh.readStream(javascriptDir + "map.js");
            out.println(filter(str));
        } catch (IOException e) {
            //e.printStackTrace();
            wb.log.write(wb.log.ERROR, "exportPage - " + e.getMessage());
        }

        // include body declaration and title
        out.println("<div class=\"title\"><a name=\"top\">" + SPACE + "</a>" + htmlText(trs("TXT_Map")) + "</div>");
        printHomeLink(out, this);

        // Include page itself
        out.println("<div id=\"map\" class=\"map\" style=\"height: 600px\"></div>");
        out.println("<div class=\"mapctrl\">");
        out.println("<p class=\"mapctrlbox\">");
        out.println("<span class=\"gras\">" + htmlText(trs("map_ancestors")) + "</span>&nbsp;&nbsp;<input id=\"anca\" name=\"ancestor\" type=\"radio\" value=\"all\" onclick=\"boxclick()\" checked=\"checked\" />" + htmlText(trs("map_all")));
        out.println("&nbsp;&nbsp;<input id=\"ancs\" name=\"ancestor\" type=\"radio\" value=\"sosa\" onclick=\"boxclick()\" />" + htmlText(trs("map_ascendants")));
        out.println("&nbsp;&nbsp;<input id=\"ancc\" name=\"ancestor\" type=\"radio\" value=\"cousins\" onclick=\"boxclick()\" />" + htmlText(trs("map_cousins")));
        out.println("&nbsp;&nbsp;<input id=\"anco\" name=\"ancestor\" type=\"radio\" value=\"others\" onclick=\"boxclick()\" />" + htmlText(trs("map_others")));
        out.println("</p>");
        out.println("<p class=\"mapctrlbox\">");
        out.println("<span class=\"gras\">" + htmlText(trs("map_events")) + "</span>&nbsp;&nbsp;<input id=\"evea\" name=\"event\" type=\"radio\" value=\"all\" onclick=\"boxclick()\"  checked=\"checked\" />" + htmlText(trs("map_all")));
        out.println("&nbsp;&nbsp;<input id=\"even\" name=\"event\" type=\"radio\" value=\"births\" onclick=\"boxclick()\" />" + htmlText(trs("map_birth")));
        out.println("&nbsp;&nbsp;<input id=\"evem\" name=\"event\" type=\"radio\" value=\"marriages\" onclick=\"boxclick()\" />" + htmlText(trs("map_marriages")));
        out.println("&nbsp;&nbsp;<input id=\"eved\" name=\"event\" type=\"radio\" value=\"deaths\" onclick=\"boxclick()\" />" + htmlText(trs("map_deaths")));
        out.println("</p>");
        out.println("<p class=\"mapctrlbox\">");
        out.println("<span class=\"gras\">" + htmlText(trs("map_years")) + "</span>&nbsp;<input id=\"min\" name=\"min\" type=\"text\" size=\"4\" value=\"0\" onchange=\"boxclick()\" style=\"text-align: center\" />");
        out.println("&nbsp;" + htmlText(trs("map_to")) + "&nbsp;<input id=\"max\" name=\"max\" type=\"text\" size=\"4\" value=\"2100\" onchange=\"boxclick()\" style=\"text-align: center\" />");
        out.println("</p>");
        out.println("<p class=\"mapctrlbox\">");
        out.println("<span class=\"gras\">" + htmlText(trs("map_volume")) + "</span>&nbsp;&nbsp;<input id=\"vola\" name=\"volume\" type=\"radio\" value=\"all\" onclick=\"boxclick()\"  checked=\"checked\" />" + htmlText(trs("map_all")));
        out.println("&nbsp;&nbsp;<input id=\"volh\" name=\"volume\" type=\"radio\" value=\"high\" onclick=\"boxclick()\" />" + htmlText(trs("map_high")));
        out.println("&nbsp;&nbsp;<input id=\"volm\" name=\"volume\" type=\"radio\" value=\"medium\" onclick=\"boxclick()\" />" + htmlText(trs("map_medium")));
        out.println("&nbsp;&nbsp;<input id=\"voll\" name=\"volume\" type=\"radio\" value=\"low\" onclick=\"boxclick()\" />" + htmlText(trs("map_low")));
        out.println("</p>");
        out.println("<p class=\"mapctrlbox\">");
        out.println("<span class=\"gras\">" + htmlText(trs("map_density")) + "</span>&nbsp;&nbsp;<input id=\"den1\" name=\"density\" type=\"radio\" value=\"dense\" onclick=\"boxclick()\"  checked=\"checked\" />" + htmlText(trs("map_high")));
        out.println("&nbsp;&nbsp;<input id=\"den2\" name=\"density\" type=\"radio\" value=\"spread\" onclick=\"boxclick()\" />" + htmlText(trs("map_medium")));
        out.println("&nbsp;&nbsp;<input id=\"den3\" name=\"density\" type=\"radio\" value=\"scarce\" onclick=\"boxclick()\" />" + htmlText(trs("map_low")));
        out.println("</p>");
        out.println("<p class=\"mapctrlbox\">");
        out.println("<span class=\"gras\">" + htmlText(trs("map_markersize")) + "</span>&nbsp;&nbsp;");
        out.println("<input type=\"button\" onclick=\"sub()\" style=\"font-weight: bold; height:15px; vertical-align: middle; background: url('../theme/p.gif')\"  />");
        out.println("<input type=\"text\" value=\"32\" size=\"3\" id=\"markersize\" name=\"markersize\" onchange=\"chg();\" style=\"text-align: center\" />");
        out.println("<input type=\"button\" onclick=\"add()\" style=\"font-weight: bold; height:15px; vertical-align: middle; background: url('../theme/n.gif')\"  />");
        out.println("</p>");
        out.println("</div>");
        out.println(" ");
        out.println("<script>");
        out.println("displayMap();");
        out.println("displayMarkers();");
        out.println("</script>");

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
    private Set ancestors = null;
    private Set cousins = null;

    /**
     *  Main export function for the data
     */
    private void exportXMLData(File dir, Indi indi) {

        // Opens page
        String fileStr = sectionPrefix + ".xml";
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
        for (Iterator it = wh.getCities(wh.gedcom).iterator(); it.hasNext();) {
            String city = (String) it.next();
            List<Property> listProps = wh.getCitiesProps(city);
            for (Iterator p = listProps.iterator(); p.hasNext();) {
                Property prop = (Property) p.next();
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
        for (Iterator it = citiesFlash.keySet().iterator(); it.hasNext();) {
            String city = (String) it.next();
            CityFlash cityFlash = citiesFlash.get(city);
            if (cityFlash != null) {
                Integer total = (Integer) (cityFlash.nbBirths + cityFlash.nbMarriages + cityFlash.nbDeaths + cityFlash.nbOther);
                if (total > maxVolume) {
                    maxVolume = total;
                }
            }
        }
        // Calculates derived measures
        for (Iterator it = citiesFlash.keySet().iterator(); it.hasNext();) {
            String city = (String) it.next();
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
        cityFlash.linkToPage = (String) (wb.sectionCitiesDetails.getPagesMap().get(htmlAnchorText(cityFlash.city)));
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
        // Get location from local file
        String code = NbPreferences.forModule(clazz).get(getPlaceAsLongString((PropertyPlace) prop, true, true), null);
        if (code == null || code.isEmpty()) {
            return false;
        }
        // Get coordinates
        String sep = ";";
        try {
            StringTokenizer tokens = new StringTokenizer(code, sep);
            if (tokens.hasMoreTokens()) {
                tokens.nextToken();
            }
            if (tokens.hasMoreTokens()) {
                cf.lat = Double.parseDouble(tokens.nextToken());
            }
            if (tokens.hasMoreTokens()) {
                cf.lng = Double.parseDouble(tokens.nextToken());
            }
        } catch (Throwable t) {
        }
        return true;


    }

    // FIXME: must be taken from gedcom preference
    //FIXME: duplicate from geo
    public String getPlaceAsLongString(PropertyPlace place, boolean compress, boolean complete) {
        if (place == null) {
            return "";
        }

        String format;
        if (complete)
            if (compress)
                format = "1,0,2,3,4,5,6";
            else
                // FIXME: should we use format.replaceall(',',', ') ?
                format = "1, 0, 2, 3, 4, 5, 6";
        else
            if (compress)
                format = "1,2,4,5,6";
            else
                format = "1, 2, 4, 5, 6";
        return place.format(format);
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

        return;
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
        return;
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
        return;
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
        return;
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
            text += trs("map_box_occon") + " \"" + cityFlash.minDate + "\".";
        } else if (cityFlash.minDate != null && cityFlash.maxDate != null) {
            text += trs("map_box_occbet") + " \"" + cityFlash.minDate + "\" " + trs("map_box_occand") + " \"" + cityFlash.maxDate + "\".";
        }
        cityFlash.text = text;
        return;
    }

    /**
     *  Get top 5 most frequent lastnames and take privacy into account
     */
    private String getLastNames(Map<String, Integer> map) {
        String output = "";
        for (int i = 0; i < 5; i++) {
            Integer max = 0;
            String maxname = "";
            for (Iterator it = map.keySet().iterator(); it.hasNext();) {
                String name = (String) it.next();
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
        out.println("<ls>");

        for (Iterator it = citiesFlash.keySet().iterator(); it.hasNext();) {
            String city = (String) it.next();
            CityFlash cityFlash = citiesFlash.get(city);
            if (cityFlash != null && (wp.param_media_DispUnknownLoc.equals("1") || cityFlash.lng != -45 || cityFlash.lat != 30)) {
                String line = "";
                line += "<l ";
                line += "x=\"" + cityFlash.lng + "\" ";
                line += "y=\"" + cityFlash.lat + "\" ";
                line += "s=\"" + cityFlash.size + "\" ";
                line += "a=\"" + cityFlash.ancestor + "\" ";
                line += "t=\"" + cityFlash.type + "\" ";
                line += "d=\"" + cityFlash.density + "\" ";
                line += "min=\"" + cityFlash.min + "\" ";
                line += "max=\"" + cityFlash.max + "\" ";
                line += "lkp=\"" + cityFlash.linkToPage + "\" ";
                line += "lki=\"" + cityFlash.linkAnchor + "\" ";
                line += "lko=\"" + htmlAnchorText(cityFlash.city) + "\" ";
                line += "cty=\"" + cityFlash.city + "\" ";
                line += ">";
                out.println(line);
                out.println(cityFlash.text);
                out.println("</l>");
            }
        }
        out.println("</ls>");
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
        for (Iterator it = citiesFlash.keySet().iterator(); it.hasNext();) {
            String name = (String) it.next();
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

