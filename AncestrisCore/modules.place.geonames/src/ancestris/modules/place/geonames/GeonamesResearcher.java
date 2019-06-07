package ancestris.modules.place.geonames;

import ancestris.api.place.Place;
import ancestris.api.place.SearchPlace;
import ancestris.libs.geonames.GeonamesOptions;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Gedcom;
import genj.gedcom.PropertyPlace;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import modules.editors.gedcomproperties.utils.PlaceFormatConverterPanel;
import org.geonames.*;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;

/**
 *
 * @author frederic & dominique
 */
public class GeonamesResearcher implements SearchPlace {

    private final static Toponym DEFAULT_TOPONYM = defaultToponym();
    private final static int DEFAULT_LAT = 45; // in the middle of the sea
    private final static int DEFAULT_LON = -4; // in the middle of the sea
    
    private final static Logger LOG = Logger.getLogger(GeonamesResearcher.class.getName(), null);
    private RequestProcessor.Task theTask;
    private RequestProcessor RP = null;
    
    private final static String KEYMAP = "geonamesPlaceConversionMap";
    
    private final Map<String, PostalCode> tmpPostalCode = new HashMap<>();

    
    public GeonamesResearcher() {
        if (RP == null) {
            RP = new RequestProcessor("GeonamesResearcher", 1, true);
        }
    }
    
    public RequestProcessor.Task getTask() {
        return theTask;
    }

    /**
     * Look for all places on the internet matching searchPlace and add them to placeList
     * @param place      : searched location
     * @param city       : city looked for (optional)
     * @param code       : code looked for (optional)
     * @param placesList : list to which results will be added
     * @param maxResults : "0" for all results, the max number of results otherwise
     * @param taskListener       : if not null, search is performed through another thread and task is used when thread is finished
     */
    @Override
    public void searchPlace(String place, String city, String code, final List<Place> placesList, final int maxResults, TaskListener taskListener) {

        final String searchedPlace = clean(place);
        if (searchedPlace.isEmpty()) {
            return;
        }
        final String searchedCity;
        city = clean(city);
        if (city.isEmpty()) {
            searchedCity = getCity(searchedPlace);
        } else {
            searchedCity = city;
        }
        final String searchedCode;
        code = clean(code);
        if (code.isEmpty()) {
            searchedCode = getCode(searchedPlace);
        } else {
            searchedCode = code;
        }
        
        if (taskListener != null) {
            Runnable runnable = new Runnable() {
                @Override
                public synchronized void run() {
                    placesList.addAll(doSearch(searchedPlace, searchedCity, searchedCode, maxResults));
                }
            };

            theTask = RP.create(runnable); //the task is not started yet
            theTask.addTaskListener(taskListener);
            theTask.schedule(0); //start the task
        } else {
            placesList.addAll(doSearch(searchedPlace, searchedCity, searchedCode, maxResults));
        }

    }
    
    
    
    private List<Place> doSearch(String searchedPlace, String searchedCity, String searchedCode, int maxResults) {
        
        final List<Place> mPlacesList = new ArrayList<>();
        final Set<String> tmpListDedup = new HashSet<>();
    
        try {
            WebService.setUserName(GeonamesOptions.getInstance().getUserName());
            ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
            searchCriteria.setStyle(Style.FULL);
            searchCriteria.setLanguage(Locale.getDefault().toString());
            searchCriteria.setMaxRows(maxResults == 0 ? 99 : Math.min(maxResults, 99));
            // try search with all elements of place name to be more precise, separating the words
            if (!searchedPlace.isEmpty()) {
                searchCriteria.setQ(searchedPlace);
                searchPlace(searchCriteria, searchedPlace, tmpListDedup, mPlacesList, maxResults);
            }
            // try with numbers only (i.e. Martinique is not in France according to 'geonames' so country fails the search)
            if (!searchedCode.isEmpty() && (mPlacesList.isEmpty() || maxResults != 1)) { 
                searchCriteria.setQ(searchedCode);
                searchPlace(searchCriteria, searchedPlace, tmpListDedup, mPlacesList, maxResults);
            }
            // try without "q" so only with namestartswith
            if (!searchedCity.isEmpty() && (mPlacesList.isEmpty() || maxResults != 1)) { 
                searchCriteria.setNameStartsWith(searchedCity);
                searchCriteria.setQ(null);
                searchPlace(searchCriteria, searchedPlace, tmpListDedup, mPlacesList, maxResults);
            }
            if (mPlacesList.isEmpty()) { // if still not found, default topo
                mPlacesList.add(defaultPlace());
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error during geonames search.", e);
        }

        return mPlacesList;
    }

    private void searchPlace(ToponymSearchCriteria searchCriteria, String searchedPlace, Set<String> tmpListDedup, List<Place> mPlacesList, int maxResults) throws Exception {
        final ToponymSearchResult toponymSearchResult = WebService.search(searchCriteria);
        for (Toponym iTopo : toponymSearchResult.getToponyms()) {
            final PostalCode pc = lookForPostalCode(iTopo.getName());
            final Place place = new GeonamesPlace(iTopo, pc);
            String str = place.toString();
            if (matches(str, searchedPlace) && !tmpListDedup.contains(str)) {
                mPlacesList.add(place);
                tmpListDedup.add(str);
            }
            if (maxResults == 1) {
                break;
            }
        }
    }
    
    private PostalCode lookForPostalCode(String placeName) throws Exception {
        if (tmpPostalCode.containsKey(placeName)) {
            return tmpPostalCode.get(placeName);
        }
        final PostalCodeSearchCriteria pcsc = new PostalCodeSearchCriteria();
        pcsc.setPlaceName(placeName);
        pcsc.setMaxRows(1);
        final List<PostalCode> lpc = WebService.postalCodeSearch(pcsc);
        if (!lpc.isEmpty()) {
            final PostalCode retour = lpc.get(0);
            tmpPostalCode.put(placeName, retour);
            return retour;
        }
        return null;
    }
    
    public Place defaultPlace() {
        return new GeonamesPlace(DEFAULT_TOPONYM, null);
    }
    
    public Place searchNearestPlace(double latitude, double longitude) {
        WebService.setUserName(GeonamesOptions.getInstance().getUserName());        
        try {
            List<Toponym> results = WebService.findNearbyPlaceName(latitude, longitude);
            if (!results.isEmpty()) {
                int geonameId = results.get(0).getGeoNameId();
                Toponym toponym = WebService.get(geonameId, null, null);
                return new GeonamesPlace(toponym, null);
            }
        } catch (Exception ex) {
            //Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    
    public static String getGeonamesMapString(Gedcom gedcom) {
        return gedcom.getRegistry().get(KEYMAP, "");
    }
        
    public static String[] getGeonamesMap(Gedcom gedcom) {
        String[] format = null;
        String placeMap = getGeonamesMapString(gedcom);
        PlaceFormatConverterPanel pfc = new PlaceFormatConverterPanel(GeonamesPlace.getPlaceFormat(), gedcom.getPlaceFormat(), placeMap);
        pfc.setTextTitle(NbBundle.getMessage(GeonamesResearcher.class, "TITL_PlaceFormatConversionTitle"));
        pfc.setLeftTitle(NbBundle.getMessage(GeonamesResearcher.class, "TITL_PlaceFormatConversionLeftTitle"));
        pfc.setRightTitle(NbBundle.getMessage(GeonamesResearcher.class, "TITL_PlaceFormatConversionRightTitle"));
        // Display parameter panel asking user to map geonames fields to his/her gedcom fields
        Object o = DialogManager.create(NbBundle.getMessage(GeonamesResearcher.class, "TITL_PlaceFormatConversion"), pfc).setMessageType(DialogManager.PLAIN_MESSAGE).show();
        if (o == DialogManager.OK_OPTION) {
            final String map = pfc.getConversionMapAsString();
            if (!map.replace(PropertyPlace.JURISDICTION_SEPARATOR, "").trim().isEmpty()) {
                gedcom.getRegistry().put(KEYMAP, map);
                format = PropertyPlace.getFormat(map);
            } else {
                if (placeMap.isEmpty()) {
                    DialogManager.create(NbBundle.getMessage(GeonamesResearcher.class, "TITL_PlaceFormatConversion"), NbBundle.getMessage(GeonamesResearcher.class, "ERR_EmptyConversion"))
                            .setMessageType(DialogManager.ERROR_MESSAGE).show();
                } else {
                    DialogManager.create(NbBundle.getMessage(GeonamesResearcher.class, "TITL_PlaceFormatConversion"), NbBundle.getMessage(GeonamesResearcher.class, "ERR_NothingSaved"))
                            .setMessageType(DialogManager.ERROR_MESSAGE).show();
                }
            }
        }
        return format;
    }
    
    
    private String clean(String str) {
        return str.replaceAll(PropertyPlace.JURISDICTION_SEPARATOR, " ").replaceAll(" +", " ").trim();
    }

    /**
     * Extract first word
     * @param place
     * @return 
     */
    private String getCity(String text) {
        String[] bits = text.split(" ");
        for (String bit : bits) {
            String str = bit.replaceAll("[0-9]", "");
            if (str.length() > 1) {
                return str;
            }
        }
        return text;
    }

    /**
     * Extract numerical code from string
     * @param place
     * @return 
     */
    private String getCode(String text) {
        String[] bits = text.split(" ");
        for (String bit : bits) {
            String str = bit.replaceAll("[^0-9]", "");
            if (str.length() > 1) {
                return str;
            }
        }
        return "";
    }

    /**
     * Check if at least 2 elements of the searchedPlace bits exist in place
     * @param text
     * @param searchedPlace
     * @return 
     */
    private boolean matches(String text, String searchedPlace) {
        int matches = 0;
        String[] bits = searchedPlace.split(" ");
        for (String bit : bits) {
            if (text.contains(bit)) {
                matches++;
            }
            if (matches == 2) {
                return true;
            }
        }
        // There is only one element to match return true if there is a match
        return bits.length == matches && matches > 0;
    }


    
    
    /**
     * Defines a default toponym pointing in the middle of the sea
     *
     * @return
     */
    private static Toponym defaultToponym() {
        Toponym topo = new Toponym();
        topo.setLatitude(DEFAULT_LAT);
        topo.setLongitude(DEFAULT_LON);
        topo.setPopulation(Long.getLong("0"));
        return topo;
    }

    
 }
