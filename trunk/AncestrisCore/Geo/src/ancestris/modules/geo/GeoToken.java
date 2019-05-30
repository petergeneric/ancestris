/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.geo;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.geonames.Toponym;
import org.jxmapviewer.viewer.GeoPosition;

/**
 *
 * @author frederic
 */
public class GeoToken implements Transferable {

    static public DataFlavor geoPosFlavor = new DataFlavor(GeoPosition.class, "X-data/geocoord; class=<org.jxmapviewer.viewer.GeoPosition>; Geographic coordinates");
    static public DataFlavor topoFlavor = new DataFlavor(Toponym.class, "X-data/toponym; class=<org.geonames.Toponym>; Geographic location");
    private GeoPosition geoPosition = null;
    private Toponym topo = null;
    private DataFlavor[] flavors = new DataFlavor[] {
        DataFlavor.stringFlavor, 
        geoPosFlavor,
        topoFlavor
    };

    public GeoToken(GeoPosition geoPosition) {
        this.geoPosition = geoPosition;
    }

    public GeoToken(Toponym topo) {
        this.topo = topo;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor df) {
        for (int i = 0; i < flavors.length; i++) {
            DataFlavor dataFlavor = flavors[i];
            if (df.equals(dataFlavor)) {
                return true;
            }
        }
        return false;
    }

    public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
        if (df.equals(flavors[0])) {
            return topo != null ? topo.toString() : geoPosition != null ? geoPosition.toString() : "";
        } else if (df.equals(flavors[1])) {
            return geoPosition;
        } else if (df.equals(flavors[2])) {
            return topo;
        }
        return "";
    }
}
