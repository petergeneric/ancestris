package ancestris.modules.editors.genealogyeditor.models;

import ancestris.modules.gedcom.utilities.PropertyTag2Name;
import genj.gedcom.Property;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import org.openide.util.NbBundle;

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
            add("FACT");
        }
    };
    List<Property> eventsList = new ArrayList<Property>();
    String[] columnsName = {
        NbBundle.getMessage(EventsListModel.class, "EventsTableModel.column.ID.eventType"),
        NbBundle.getMessage(EventsListModel.class, "EventsTableModel.column.ID.place"),
        NbBundle.getMessage(EventsListModel.class, "EventsTableModel.column.ID.date")
    };

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
            if (propertyEvent.getTag().equals("EVEN") || propertyEvent.getTag().equals("FACT")) {
                Property eventType = propertyEvent.getProperty("TYPE");
                return eventType != null ? eventType.getValue() : "";
            } else if (mIndividualAttributesTags.contains(propertyEvent.getTag())) {
                return PropertyTag2Name.getTagName(propertyEvent.getTag()) + " " + propertyEvent.getValue();
            } else {
                return PropertyTag2Name.getTagName(propertyEvent.getTag());
            }
        }
        return "";
    }

    public void addAll(List<Property> eventsList) {
        this.eventsList.addAll(eventsList);
        fireIntervalAdded(this, 0, this.eventsList.size() - 1);
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
