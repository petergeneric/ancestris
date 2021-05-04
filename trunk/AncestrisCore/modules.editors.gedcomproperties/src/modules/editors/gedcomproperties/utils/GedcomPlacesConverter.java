/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package modules.editors.gedcomproperties.utils;

import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class GedcomPlacesConverter {
    
    private final String PLACETAG = "PLAC";
    private final Gedcom gedcom;
    private final int fromFormatLength;
    private final String[] toPlaceFormat, map; 
    
    private int nbOfDifferentFoundPlaces = 0;
    private int nbOfDifferentChangedPlaces = 0;
    private int nbOfFoundPlaces = 0;
    private int nbOfChangedPlaces = 0;
    private int nbOfEmptyPlaces = 0;
    private Exception error = null;
    private final Set<String> listOfCorrectPlaces = new TreeSet<String>();
    private final Set<String> listOfIncorrectPlaces = new TreeSet<String>();
    

    public GedcomPlacesConverter(Gedcom gedcom, String fromFormat, String toFormat, String conversionMap) {
        this.gedcom = gedcom;
        this.fromFormatLength = PropertyPlace.getFormat(fromFormat).length;
        this.toPlaceFormat = PropertyPlace.getFormat(toFormat);
        this.map = PropertyPlace.getFormat(conversionMap);
    }

    public boolean convert() {
        // Init variables
        nbOfDifferentFoundPlaces = 0;
        nbOfDifferentChangedPlaces = 0;
        nbOfFoundPlaces = 0;
        nbOfChangedPlaces = 0;
        nbOfEmptyPlaces = 0;
        
        // prepare new place string
        String[] newPlace = new String[toPlaceFormat.length];
        
        // get all propertyplaces : list of unique string places with their associated place properties
        List<Property> allPlaces = (List<Property>) gedcom.getPropertiesByClass(PropertyPlace.class);
        Set<String> allUniquePlaces = new TreeSet<String>();
        for (Property place : allPlaces) {
            allUniquePlaces.add(place.getValue());
        }

        nbOfFoundPlaces = allPlaces.size();
        nbOfDifferentFoundPlaces = allUniquePlaces.size();
        
        // For each place...
        for (Property place : allPlaces) {
            
            String key = place.getValue();

            // ...skip if empty...
            if (key.isEmpty()) {
                nbOfEmptyPlaces++;
                continue;
            }
                    
            String[] currentJurisdictions = PropertyPlace.getFormat(key); 
            
            // ...add to incorrect list if nb of jurisdictions does not match...
            if (currentJurisdictions.length != fromFormatLength) {
                listOfIncorrectPlaces.add(key);
                continue;
            }
                    
            // ...else convert it according to mapping and store new location in newPlace
            for (int i = 0; i < newPlace.length; i++) {
                if (!map[i].isEmpty()) {
                    // consider places where value found does not match the current place format
                    int j = Integer.valueOf(map[i]);
                    if (j<0 || j>currentJurisdictions.length-1) {
                        newPlace[i] = key;
                    } else {
                        newPlace[i] = currentJurisdictions[j];
                    }
                } else {
                    newPlace[i] = "";
                }
            }
            
            // ...and store it in all related properties as display of string array
            try {
                place.setValue(getDisplayOfPlace(newPlace));
                nbOfChangedPlaces++;
                listOfCorrectPlaces.add(key);
            } catch (Exception e) {
                String msg = new Exception(e).getLocalizedMessage();
                error = new Exception(NbBundle.getMessage(PlaceFormatConverterPanel.class, "ERR_Exception") + " " + msg + "!");
                Exceptions.printStackTrace(e);
                return false;
            }
        }
        
        nbOfDifferentChangedPlaces = listOfCorrectPlaces.size();
        
        if (listOfIncorrectPlaces.size() > 0) {
            error = new Exception(NbBundle.getMessage(PlaceFormatConverterPanel.class, "ERR_WrongFormat"));
            return false;
        }
        return true;
    }

    
    
    
    public boolean isWithError() {
        return error != null;
    }
    
    public String[] getIncorrectPlaces() {
        if (listOfIncorrectPlaces == null) {
            return null;
        }
        return listOfIncorrectPlaces.toArray(new String[listOfIncorrectPlaces.size()]);
    }
    

    
    
    private String getDisplayOfPlace(String[] newPlace) {
        String ret = "";
        String sep = PropertyPlace.JURISDICTION_SEPARATOR;
        
        for (int i = 0; i < newPlace.length; i++) {
            if (i == newPlace.length-1) {
                sep = "";
            }
            ret += newPlace[i] + sep;
        }
        return ret;
    }
    
    public int getNbOfDifferentFoundPlaces() {
        return nbOfDifferentFoundPlaces;
    }
    
    public int getNbOfDifferentChangedPlaces() {
        return nbOfDifferentChangedPlaces;
    }
    
    public int getNbOfFoundPlaces() {
        return nbOfFoundPlaces;
    }
    
    public int getNbOfChangedPlaces() {
        return nbOfChangedPlaces;
    }
    
    public int getNbOfEmptyPlaces() {
        return nbOfEmptyPlaces;
    }
    
    public Exception getError() {
        if (error == null) {
            error = new Exception(NbBundle.getMessage(PlaceFormatConverterPanel.class, "ERR_Unknown"));
        }
        return error;
    }
    
}
