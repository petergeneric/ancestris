/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2017 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package modules.editors.gedcomproperties.utils;

import genj.gedcom.Gedcom;
import genj.gedcom.PropertyPlace;
import java.util.List;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class GedcomPlacesAligner {

    private final Gedcom gedcom;
    private Exception error = null;
    private int nbOfAlignedPlaces = 0;
    private int nbOfPlaces = 0;

    public GedcomPlacesAligner(Gedcom gedcom) {
        this.gedcom = gedcom;
    }

    public boolean convert() {
        error = null;
        nbOfAlignedPlaces = 0;
        nbOfPlaces = 0;

        try {
            String[] locs = null;
            List<PropertyPlace> places = (List<PropertyPlace>) gedcom.getPropertiesByClass(PropertyPlace.class);
            for (PropertyPlace place : places) {
                locs = place.getJurisdictions();
                if (!place.setJurisdictions(gedcom, locs)) {
                    nbOfAlignedPlaces++;
                }
                nbOfPlaces++;
            }
        } catch (Exception e) {
            String msg = new Exception(e).getLocalizedMessage();
            error = new Exception(NbBundle.getMessage(PlaceFormatConverterPanel.class, "ERR_Exception") + " " + msg + "!");
            Exceptions.printStackTrace(e);
            return false;
        }

        return true;
    }

    public int getNbOfPlacesAligned() {
        return nbOfAlignedPlaces;
    }

    public Object getNbOfPlaces() {
        return nbOfPlaces;
    }

    public Exception getError() {
        if (error == null) {
            error = new Exception(NbBundle.getMessage(GedcomPlacesAligner.class, "ERR_Unknown"));
        }
        return error;
    }

}
