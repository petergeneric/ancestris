package ancestris.modules.place.geonames;

import ancestris.api.place.Place;
import java.util.logging.Logger;
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
    private final static Logger logger = Logger.getLogger(GeonamesPlacesList.class.getName(), null);

    public GeonamesPlace(Toponym toponym, PostalCode postalCode) {
        this.postalCode = postalCode;
        this.toponym = toponym;
    }

    @Override
    public int compareTo(Place that) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCity() {
        return postalCode == null ? toponym.getName() : postalCode.getPlaceName();
    }

    @Override
    public String getFirstAvailableJurisdiction() {
        return postalCode == null ? toponym.getName() : postalCode.getAdminCode1();
    }

    @Override
    public String[] getFormat() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getFormatAsString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

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
            jurisdictions[index++] = toponym.getAdminCode4();  // GeoID
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
        return toString();
    }

    @Override
    public void setFormatAsString(boolean global, String format) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Double getLongitude() {
        return toponym.getLongitude();
    }

    @Override
    public Double getLatitude() {
        return toponym.getLatitude();
    }

    /**
     * Format is defined as : "City, PostalCode, GeoCode, County, State, Country"
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
     * <alternateNames>Medon,Meudon,Mjodon,Moldonium,Rabelais,mo dong,moedong,mudon,mwdwn,mydwn,Медон,Мёдон,مودون,ميدون,ムードン,默东,뫼동</alternateNames>
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
    public String toString() {
        try {
            return toponym.getName()
                    + (postalCode != null ? postalCode.getPostalCode() : "")
                    + toponym.getAdminCode4() 
                    + toponym.getAdminName2()
                    + toponym.getAdminName1()
                    + toponym.getCountryName(); // Country 
        } catch (InsufficientStyleException ex) {
            return "";
        }
    }
    
    /**
     * Defines geonames placeformat
     * 
     * @return 
     */
    public static String getPlaceFormat() {
        return NbBundle.getMessage(GeonamesPlace.class, "GeoNamesPlaceFormat"); // Example : "City, PostalCode, GeoCode, County, State, Country";
    }


}
