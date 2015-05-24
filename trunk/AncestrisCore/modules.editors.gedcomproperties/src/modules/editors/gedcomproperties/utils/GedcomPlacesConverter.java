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
import genj.util.ReferenceSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
    private Exception error = null;
    private final List<String> listOfIncorrectPlaces = new ArrayList<String>();
    private final List<Property> listOfProperty = new ArrayList<Property>();
    

    public GedcomPlacesConverter(Gedcom gedcom, String fromFormat, String toFormat, String conversionMap) {
        this.gedcom = gedcom;
        this.fromFormatLength = PropertyPlace.getFormat(fromFormat).length;
        this.toPlaceFormat = PropertyPlace.getFormat(toFormat);
        this.map = PropertyPlace.getFormat(conversionMap);
    }

    public boolean convert() {
        // prepare new place string
        String[] newPlace = new String[toPlaceFormat.length];
        
        // get all propertyplaces : list of unique string places with their associated place properties
        ReferenceSet<String, Property> allUniquePlaces = gedcom.getReferenceSet(PLACETAG);
        nbOfFoundPlaces = allUniquePlaces.getSize();
        nbOfDifferentFoundPlaces = allUniquePlaces.getKeys().size();
        
        // for each unique key wich is the location with its jurisdictions...
        for (Iterator iterator = allUniquePlaces.getKeys().iterator(); iterator.hasNext();) {
            
            String key = (String) iterator.next();
            String[] currentJurisdictions = PropertyPlace.getFormat(key); 
            
            // ...and if it respects the current formatting...
            if (currentJurisdictions.length != fromFormatLength) {
                listOfIncorrectPlaces.add(key);
                continue;
            }
                    
            // ...convert it according to mapping
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
                Set<Property> placeProperties = allUniquePlaces.getReferences(key);
                Property places[] = placeProperties.toArray(new Property[placeProperties.size()]); // convert to array as iterator on set not stable while changing properties
                for (int i = 0; i < places.length; i++) {
                    Property place = places[i];
                    if (!listOfProperty.contains(place)) {
                        listOfProperty.add(place);
                        place.setValue(getDisplayOfPlace(newPlace));
                        nbOfChangedPlaces++;
                    } else {
                        //error = new Exception("problème avec "+place.getDisplayValue());
                        //return false;
                    }
                }
                nbOfDifferentChangedPlaces++;
            } catch (Exception e) {
                String msg = new Exception(e).getLocalizedMessage();
                error = new Exception(NbBundle.getMessage(PlaceFormatConverterPanel.class, "ERR_Exception") + " " + msg + "!");
                //Exceptions.printStackTrace(e);
                return false;
            }
        }
        
        if (nbOfChangedPlaces < nbOfFoundPlaces) {
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
        listOfIncorrectPlaces.sort(null);
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
    
    public Exception getError() {
        if (error == null) {
            error = new Exception(NbBundle.getMessage(PlaceFormatConverterPanel.class, "ERR_Unknown"));
        }
        return error;
    }
    
}
