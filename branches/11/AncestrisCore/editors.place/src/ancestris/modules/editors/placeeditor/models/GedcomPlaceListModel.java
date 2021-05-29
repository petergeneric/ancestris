package ancestris.modules.editors.placeeditor.models;

import ancestris.util.GedcomUtilities;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import java.util.*;
import javax.swing.AbstractListModel;

/**
 *
 * @author dominique
 */
public class GedcomPlaceListModel extends AbstractListModel {

    Map<String, Set<PropertyPlace>> gedcomPlacesMap = new HashMap<> ();

    public GedcomPlaceListModel(Gedcom gedcom) {
        List<PropertyPlace> gedcomPlacesList = GedcomUtilities.searchProperties(gedcom, PropertyPlace.class, null);
        for (Property property : gedcomPlacesList) {
            String gedcomPlace = property.getDisplayValue();
            Set<PropertyPlace> propertySet = gedcomPlacesMap.get(gedcomPlace);
            if (propertySet == null) {
                propertySet = new HashSet<>();
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
    public Object getElementAt(int i) {
        return gedcomPlacesMap.keySet().toArray(new String[0])[i];
    }
}
