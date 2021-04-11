package ancestris.modules.place.geonames;

import ancestris.api.place.Place;
import ancestris.api.place.PlaceFactory;
import genj.gedcom.PropertyPlace;
import genj.util.DirectAccessTokenizer;
import org.geonames.InsufficientStyleException;
import org.geonames.PostalCode;
import org.geonames.Toponym;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Frederic
 */
public class GeonamesPlace implements Place {

    private PostalCode postalCode = null;
    private Toponym toponym = null;

    public GeonamesPlace(Toponym toponym) {
        this.toponym = toponym;
    }

    public void setPostalCode(PostalCode set) {
        this.postalCode = set;
    }

    @Override
    public int compareTo(Place that) {
        return 0;  // unused
    }

    @Override
    public String getCity() {
        return postalCode == null ? toponym.getName() : postalCode.getPlaceName();
    }

    @Override
    public String getFirstAvailableJurisdiction() {
        return getJurisdiction(0);
    }

    @Override
    public String[] getFormat() {
        return new DirectAccessTokenizer(getPlaceFormat(), JURISDICTION_SEPARATOR).getTokens();  // unused
    }

    @Override
    public String getFormatAsString() {
        return getPlaceFormat();
    }

    /**
     * Defines geonames placeformat
     *
     * @return
     */
    public static String getPlaceFormat() {
        return NbBundle.getMessage(GeonamesPlace.class, "GeoNamesPlaceFormat"); // Example : "City, PostalCode, GeoCode, County, State, Country";
    }

    /**
     * The actual web service. Comment: assessment of the efficiency of the web
     * services involved - Two web services are available : - search -
     * postalCodeSearch : possibility to search on names but not reliable enough
     * for France - For France : Not OK : returned result do not work for Dijon,
     * Avignon unless postcode is provided (PlombiÃ¨res-lÃ¨s-Dijon,
     * Avignon-lÃ¨s-Saint-Claude) - For Poland : works well with postcode
     *
     * Need: - Get in one call : city, [postal code], code, county/dept,
     * state/region, country - We need all the elements in the first result of
     * the first response (maxResults = 1)
     *
     * Regarding both : - language is not necessry - both response times are
     * equivalent but calling both doubles the loading time in case of mass
     * research (ex: 1,5 min for 435 locations vs.Â 45 seconds) - Style.FULLÂ is
     * necessary to get all the admin codes - Works well with "city" only, but
     * better with "city country" and even better with "city code country" - If
     * country is available, better to add "countryBias=XX"
     *
     * Regarding search : - Criteria can include dept code or INSEE code or
     * Postcode as long as it is correct - Values returned in Style.FULL - city
     * = <toponymName>
     * - postal code = - France : usually correct :
     * <alternateName lang="post">21000</alternateName> ou
     * <alternateName lang="post">71640</alternateName> except for Paris which
     * always returns 75001 => but getAlternateNames does not return these
     * elements ! - Spain, UK, US, Germany, Portugal, Poland : not available -
     * code = - France : <adminCode4>
     * - Spain : <adminCode3>
     * - UK : <adminCode2>
     * - US : <adminCode2>
     * - Germany : <adminCode4>
     * - Portugal : <adminCode3>
     * - Poland : <adminCode3>
     * - county/dept = <adminName2>
     * - state/region = <adminName1>
     * - country = <countryName>
     *
     * Conclusion: - to make more efficient, do not retrieve postal codes,
     * except for France - use all place pieces avalable for search (and if not
     * found, only use city : to be confirmed)
     *
     */
    /**
     * Format is defined as : "City, PostalCode, GeoCode, County, State,
     * Country"
     *
     * <toponymName>Meudon</toponymName>
     * <alternateName isPreferredName="true" lang="post">92190</alternateName>
     * <adminCode4>92048</adminCode4>
     * <adminName2>Hauts-de-Seine</adminName2>
     * <adminName1>Île-de-France</adminName1>
     * <countryName>France</countryName>
     *
     * <lat>48.81381</lat>
     * <lng>2.235</lng>
     * <geonameId>2994144</geonameId>
     * <name>Meudon</name>
     * <countryCode>FR</countryCode>
     * <fcl>P</fcl>
     * <fcode>PPL</fcode>
     * <fclName>city, village,...</fclName>
     * <fcodeName>populated place</fcodeName>
     * <population>44652</population>
     * <asciiName>Meudon</asciiName>
     * <alternateNames>Medon,Meudon,Mjodon,Moldonium,Rabelais,mo
     * dong,moedong,mudon,mwdwn,mydwn,Медон,Мёдон,مودون,ميدون,ムードン,默东,뫼동</alternateNames>
     * <elevation/>
     * <srtm3>111</srtm3>
     * <continentCode>EU</continentCode>
     * <adminCode1 ISO3166-2="11">11</adminCode1>
     * <adminCode2>92</adminCode2>
     * <adminCode3>923</adminCode3>
     * <adminName3>Arrondissement de Boulogne-Billancourt</adminName3>
     * <adminName4>Meudon</adminName4>
     * <alternateName lang="ko">뫼동</alternateName
     * ><alternateName lang="ja">ムードン</alternateName>
     * <alternateName lang="post">92196 CEDEX</alternateName>
     * <alternateName lang="link">http://en.wikipedia.org/wiki/Meudon</alternateName>
     * <alternateName lang="fr">Meudon</alternateName>
     *
     *
     *
     * @return
     */
    @Override
    public String getJurisdiction(int hierarchyLevel) {
        return getJurisdictions()[hierarchyLevel];
    }

    @Override
    public String[] getJurisdictions() {
        String[] jurisdictions = new String[6];

        try {
            int index = 0;

            jurisdictions[index++] = toponym.getName(); // City
            jurisdictions[index++] = postalCode != null ? postalCode.getPostalCode() : ""; // Postal code    
            jurisdictions[index++] = (toponym.getAdminCode4() != null ? toponym.getAdminCode4() : (toponym.getAdminCode3() != null ? toponym.getAdminCode3() : toponym.getAdminCode2()));
            jurisdictions[index++] = toponym.getAdminName2(); // County
            jurisdictions[index++] = toponym.getAdminName1(); // State
            jurisdictions[index++] = toponym.getCountryName();// Country 
        } catch (InsufficientStyleException ex) {
            Exceptions.printStackTrace(ex);
        }
        return jurisdictions;

    }

    @Override
    public String getValueStartingWithCity() {
        return new DirectAccessTokenizer(toString(), JURISDICTION_SEPARATOR).toString();
    }

    @Override
    public void setFormatAsString(boolean global, String format) {
        return;  // unused
    }

    @Override
    public Double getLongitude() {
        return toponym.getLongitude();
    }

    @Override
    public Double getLatitude() {
        return toponym.getLatitude();
    }

    @Override
    public Long getPopulation() {
        if (toponym == null) {
            return 0L;
        }
        try {
            return toponym.getPopulation() == null ? 0L : toponym.getPopulation();
        } catch (InsufficientStyleException ex) {
            return 0L;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        int len = getJurisdictions().length;
        for (int i = 0; i < len; i++) {
            String str = getJurisdiction(i);
            sb.append(str);
            if (i + 1 < len) {
                sb.append(JURISDICTION_SEPARATOR + " ");
            }
        }
        return sb.toString();
    }

    @Override
    public String getPlaceToLocalFormat() {
        return toString().replaceAll(PropertyPlace.JURISDICTION_SEPARATOR, " ").replaceAll(" +", " ").trim();
    }

    @Override
    public String getName() {
        String ret = toponym.getName();
        return ret == null ? "" : ret;
    }

    @Override
    public String getCountryCode() {
        String ret = toponym.getCountryCode();
        return ret == null ? "" : ret;
    }

    @Override
    public String getCountryName() {
        String ret = toponym.getCountryName();
        return ret == null ? "" : ret;
    }

    @Override
    public String getAdminCode(int level) {
        String ret = "";
        try {
            switch (level) {
                case 5:
                    ret = toponym.getAdminCode5();
                    break;
                case 4:
                    ret = toponym.getAdminCode4();
                    break;
                case 3:
                    ret = toponym.getAdminCode3();
                    break;
                case 2:
                    ret = toponym.getAdminCode2();
                    break;
                case 1:
                default:
                    ret = toponym.getAdminCode1();
            }
        } catch (InsufficientStyleException ex) {
            return "";
        }
        if (ret == null) {
            ret = "";
        }
        return ret;
    }

    @Override
    public String getAdminName(int level) {
        String ret = "";
        switch (level) {
            case 5:
                ret = toponym.getAdminName5();
                break;
            case 4:
                ret = toponym.getAdminName4();
                break;
            case 3:
                ret = toponym.getAdminName3();
                break;
            case 2: {
                try {
                    ret = toponym.getAdminName2();
                } catch (InsufficientStyleException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            break;

            case 1:
            default: {
                try {
                    ret = toponym.getAdminName1();
                } catch (InsufficientStyleException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

        }
        if (ret == null) {
            ret = "";
        }
        return ret;
    }

    @Override
    public String getTimeZoneId() {
        String ret = "";
        try {
            if (toponym.getTimezone() != null) {
                ret = toponym.getTimezone().getTimezoneId();
            }
        } catch (InsufficientStyleException ex) {
            Exceptions.printStackTrace(ex);
        }
        return ret == null ? "" : ret;
    }

    @Override
    public String getTimeZoneGmtOffset() {
        String ret = "";
        try {
            if (toponym.getTimezone() != null) {
                ret = String.valueOf(toponym.getTimezone().getGmtOffset());
            }
        } catch (InsufficientStyleException ex) {
            Exceptions.printStackTrace(ex);
        }
        return ret == null ? "" : ret;
    }

    @Override
    public String getInfo() {
        return PlaceFactory.buildInfo(this);
    }

}
