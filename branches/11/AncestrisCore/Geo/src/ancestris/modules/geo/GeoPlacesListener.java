/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.modules.geo;

/**
 *
 * @author frederic
 */
public interface GeoPlacesListener {

    /**
     *
     * @param gpl
     * @param change
     *  - coord  : change of coordinates of one location point
     *  - name   : change of name of one location point
     *  - gedcom : change of some properties in the gedcom
     */
  public void geoPlacesChanged(GeoPlacesList gpl, String change);
}
