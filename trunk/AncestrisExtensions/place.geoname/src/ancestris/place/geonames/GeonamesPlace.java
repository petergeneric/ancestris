package ancestris.place.geonames;

import ancestris.api.place.Place;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geonames.InsufficientStyleException;
import org.geonames.Toponym;

/**
 *
 * @author dominique
 */
public class GeonamesPlace implements Place {

    private Toponym toponym = null;

    public GeonamesPlace(Toponym toponym) {
        this.toponym = toponym;
    }

    @Override
    public int compareTo(Place that) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCity() {
        return toponym.getName();
    }

    @Override
    public String getFirstAvailableJurisdiction() {
        try {
            return toponym.getAdminCode1();
        } catch (InsufficientStyleException ex) {
            Logger.getLogger(GeonamesPlace.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
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
        String[] jurisdictions = new String[10];
        int index = 0;
        try {
            jurisdictions[index++] = toponym.getName();
            jurisdictions[index++] = toponym.getAdminName1();
            jurisdictions[index++] = toponym.getAdminCode1();
            jurisdictions[index++] = toponym.getAdminName2();
            jurisdictions[index++] = toponym.getAdminCode2();
            jurisdictions[index++] = toponym.getAdminName3();
            jurisdictions[index++] = toponym.getAdminCode3();
            jurisdictions[index++] = toponym.getPostcode();
            jurisdictions[index++] = toponym.getCountryCode();

        } catch (InsufficientStyleException ex) {
            Logger.getLogger(GeonamesPlace.class.getName()).log(Level.SEVERE, null, ex);
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
        return toponym.getLongitude();
    }

    @Override
    public Double getLatitude() {
        return toponym.getLatitude();
    }

    @Override
    public String toString() {
        try {
            return toponym.getName() + "," + toponym.getAdminName1() + ","
                    + toponym.getAdminName2() + "," + toponym.getAdminName3() + ","
                    + toponym.getPostcode() + "," + toponym.getCountryCode();
        } catch (InsufficientStyleException ex) {
            Logger.getLogger(GeonamesPlace.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }
}
