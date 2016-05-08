/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.editors.geoplace;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Temp class to quickly fix dependency issue. 
 * This class should be removed by either updating Gedcom or PropertyPlace API.
 * @author daniel
 */
public class Utilities {
    private final static Logger LOG = Logger.getLogger(Utilities.class.getName(), null);

    private static <T> List<T> searchProperties(Gedcom gedcom, Class<T> type) {

        Collection<? extends Entity> entities;

        LOG.log(Level.FINE, "Searching for property {0}", type.getClass());

//        if (entityType == ENT_ALL) {
            entities = gedcom.getEntities();
//        } else {
//            entities = gedcom.getEntities(entityTypes[entityType]);
//        }

        List<T> foundProperties = new ArrayList<T>();
        for (Iterator<? extends Entity> it = entities.iterator(); it.hasNext();) {
            Entity entity = it.next();
            foundProperties.addAll(searchPropertiesRecursively(entity, type));
        }

        LOG.log(Level.FINE, "found  {0}", foundProperties.size());

        return foundProperties;
    }

    private static <T> List<T> searchPropertiesRecursively(Property parent, Class<T> type) {
        List<T> foundProperties = new ArrayList<T>();
        for (Property child : parent.getProperties()) {
            if (type.isAssignableFrom(child.getClass())) {
                foundProperties.add((T) child);
            }
            foundProperties.addAll(searchPropertiesRecursively(child, type));
        }
        return foundProperties;
    }
    
    
    
    /*package*/ static Map<String, Set<PropertyPlace>> getPropertyPlaceMap(Gedcom gedcom) {

        SortedMap<String, Set<PropertyPlace>> placesMap = new TreeMap<String, Set<PropertyPlace>>(gedcom.getCollator());
        List<PropertyPlace> gedcomPlacesList = searchProperties(gedcom, PropertyPlace.class);

        for (PropertyPlace propertyPlace : gedcomPlacesList) {
            String gedcomPlace = propertyPlace.getGeoValue();
            Set<PropertyPlace> propertySet = placesMap.get(gedcomPlace);
            if (propertySet == null) {
                propertySet = new HashSet<PropertyPlace>();
                placesMap.put(gedcomPlace, propertySet);
            }
            propertySet.add((PropertyPlace) propertyPlace);
        }

        return placesMap;
    }    
}
