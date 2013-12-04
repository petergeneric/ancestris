package ancestris.modules.editors.genealogyeditor.models;

import ancestris.modules.gedcom.utilities.GedcomUtilities;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import java.util.*;
import javax.swing.AbstractListModel;

/**
 *
 * @author dominique
 */
public class GedcomPlaceListModel extends AbstractListModel <String>{

    Map<String, Set<PropertyPlace>> gedcomPlacesMap = new HashMap<String, Set<PropertyPlace>> ();

    public GedcomPlaceListModel(Gedcom gedcom) {
        List<PropertyPlace> gedcomPlacesList = GedcomUtilities.searchProperties(gedcom, PropertyPlace.class, GedcomUtilities.ENT_ALL);
        for (Property property : gedcomPlacesList) {
            String gedcomPlace = property.getDisplayValue();
            Set<PropertyPlace> propertySet = gedcomPlacesMap.get(gedcomPlace);
            if (propertySet == null) {
                propertySet = new HashSet<PropertyPlace>();
                gedcomPlacesMap.put(gedcomPlace, null);
            }
            propertySet.add((PropertyPlace) property);
        }
    }

    @Override
    public int getSize() {
        return gedcomPlacesMap.size();
    }

    @Override
    public String getElementAt(int i) {
        return gedcomPlacesMap.keySet().toArray(new String[0])[i];
    }
}
