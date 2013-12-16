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
        String[] jurisdictions = new String[13];

        try {
            int index = 0;

            jurisdictions[index++] = postalCode.getPlaceName();
            jurisdictions[index++] = postalCode.getPostalCode();
            jurisdictions[index++] = toponym.getAdminName1();
            jurisdictions[index++] = toponym.getAdminCode1();
            jurisdictions[index++] = toponym.getAdminName2();
            jurisdictions[index++] = toponym.getAdminCode2();
            jurisdictions[index++] = toponym.getAdminName3();
            jurisdictions[index++] = toponym.getAdminCode3();
            jurisdictions[index++] = toponym.getAdminName4();
            jurisdictions[index++] = toponym.getAdminCode4();
            jurisdictions[index++] = toponym.getAdminName5();
            jurisdictions[index++] = toponym.getAdminCode5();
            jurisdictions[index++] = postalCode.getCountryCode();
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
