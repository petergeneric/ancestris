package ancestris.modules.place.geonames;

import ancestris.api.place.Place;
import java.util.logging.Logger;
import org.geonames.InsufficientStyleException;
import org.geonames.PostalCode;
import org.geonames.Toponym;
import org.openide.util.Exceptions;

/**
 *
 * @author dominique
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
        return postalCode.getPlaceName();
    }

    @Override
    public String getFirstAvailableJurisdiction() {
        return postalCode.getAdminCode1();
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] getJurisdictions() {
        String[] jurisdictions = new String[6];

        try {
            int index = 0;

            jurisdictions[index++] = postalCode.getPlaceName(); // City
            jurisdictions[index++] = postalCode.getPostalCode(); // Postal code    
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setFormatAsString(boolean global, String format) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Double getLongitude() {
        return postalCode.getLongitude();
    }

    @Override
    public Double getLatitude() {
        return postalCode.getLatitude();
    }

    @Override
    public String toString() {
        return postalCode.getPlaceName() + "," + postalCode.getAdminName1() + ","
                + postalCode.getAdminName2() + "," + postalCode.getAdminName3() + ","
                + postalCode.getPostalCode() + "," + postalCode.getCountryCode();
    }
}
