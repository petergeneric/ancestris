package ancestris.place.geonames;

import ancestris.api.place.Place;
import org.geonames.PostalCode;

/**
 *
 * @author dominique
 */
public class GeonamesPlace implements Place {

    private PostalCode postalCode = null;

    public GeonamesPlace(PostalCode postalCode) {
        this.postalCode = postalCode;
    }

    @Override
    public int compareTo(Place that) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCity() {
            return postalCode.getAdminName1();
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
        String[] jurisdictions = new String[10];
        int index = 0;

        jurisdictions[index++] = postalCode.getPlaceName();
        jurisdictions[index++] = postalCode.getCountryCode();
        jurisdictions[index++] = postalCode.getAdminCode1();
        jurisdictions[index++] = postalCode.getAdminName1();
        jurisdictions[index++] = postalCode.getAdminCode2();
        jurisdictions[index++] = postalCode.getAdminName2();
        jurisdictions[index++] = postalCode.getAdminCode3();
        jurisdictions[index++] = postalCode.getAdminName3();
        jurisdictions[index++] = postalCode.getPostalCode();
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
}
