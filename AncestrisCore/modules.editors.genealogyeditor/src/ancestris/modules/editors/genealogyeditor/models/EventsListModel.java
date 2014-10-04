package ancestris.modules.editors.genealogyeditor.models;

import ancestris.modules.editors.genealogyeditor.utilities.PropertyTag2Name;
import ancestris.*;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 *
 * @author dominique
 */
public class EventsListModel extends AbstractListModel<String> {

    /*
     * INDIVIDUAL_ATTRIBUTE
     */
    private final ArrayList<String> mIndividualAttributesTags = new ArrayList<String>() {
        {
            add("CAST");
            add("DSCR");
            add("EDUC");
            add("IDNO");
            add("NATI");
            add("NCHI");
            add("NMR");
            add("OCCU");
            add("PROP");
            add("RELI");
            add("RESI");
            add("SSN");
            add("TITL");
//            add("FACT"); not defined in gedcom xml definition files
        }
    };
    List<Property> eventsList = new ArrayList<Property>();

    public EventsListModel() {
    }

    @Override
    public int getSize() {
        return eventsList.size();
    }

    @Override
    public String getElementAt(int row) {
        if (row < eventsList.size()) {
            final Property propertyEvent = eventsList.get(row);
            final PropertyDate propertyDate = (PropertyDate) propertyEvent.getProperty("DATE");
            final String date = propertyDate != null ? propertyDate.getDisplayValue() : null;
            if (propertyEvent.getTag().equals("EVEN") || propertyEvent.getTag().equals("FACT")) {
                Property eventType = propertyEvent.getProperty("TYPE");
                return PropertyTag2Name.getTagName(propertyEvent.getTag()) + (eventType != null ? " " + eventType.getValue() : "") + (date != null ? " - " + date : "");
            } else if (mIndividualAttributesTags.contains(propertyEvent.getTag())) {
                return PropertyTag2Name.getTagName(propertyEvent.getTag()) + " " + propertyEvent.getValue() + (date != null ? " - " + date : "");
            } else {
                return PropertyTag2Name.getTagName(propertyEvent.getTag()) + (date != null ? " - " + date : "");
            }
        }
        return "";
    }

    public int indexOf(Object o) {
        return this.eventsList.indexOf(o);
    }

    public void addAll(List<Property> eventsList) {
        if (!eventsList.isEmpty()) {
            this.eventsList.addAll(eventsList);
            fireIntervalAdded(this, 0, this.eventsList.size() - 1);
        }
    }

    public void add(Property event) {
        this.eventsList.add(event);
        fireIntervalAdded(this, 0, this.eventsList.size() - 1);
    }

    public Property getValueAt(int row) {
        return eventsList.get(row);
    }

    public Property remove(int row) {
        Property event = eventsList.remove(row);
        fireContentsChanged(this, 0, this.eventsList.size() - 1);
        return event;
    }

    public void clear() {
        eventsList.clear();
        fireContentsChanged(this, 0, this.eventsList.size() - 1);
    }
}
