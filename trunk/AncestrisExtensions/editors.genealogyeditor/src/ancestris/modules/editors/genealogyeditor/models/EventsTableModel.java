package ancestris.modules.editors.genealogyeditor.models;

import ancestris.modules.gedcom.utilities.PropertyTag2Name;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.UnitOfWork;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class EventsTableModel extends AbstractTableModel {

    List<Property> eventsList = new ArrayList<Property>();
    String[] columnsName = {
        NbBundle.getMessage(EventsTableModel.class, "EventsTableModel.column.ID.eventType"),
        NbBundle.getMessage(EventsTableModel.class, "EventsTableModel.column.ID.place"),
        NbBundle.getMessage(EventsTableModel.class, "EventsTableModel.column.ID.date")
    };

    public EventsTableModel() {
    }

    @Override
    public int getRowCount() {
        return eventsList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsName.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row < eventsList.size()) {
            final Property propertyEvent = eventsList.get(row);
            if (column == 0) {
                if (!propertyEvent.getTag().equals("EVEN")) {
                    return PropertyTag2Name.getTagName(propertyEvent.getTag());
                } else {
                    Property eventType = propertyEvent.getProperty("TYPE");
                    return eventType != null ? eventType.getValue() : "";
                }
            } else if (column == 1) {
                PropertyPlace place = (PropertyPlace) propertyEvent.getProperty("PLAC");
                if (place == null) {
                    // For sorting problem we need to create a PLAC tag
                    try {
                        propertyEvent.getGedcom().doUnitOfWork(new UnitOfWork() {

                            @Override
                            public void perform(Gedcom gedcom) throws GedcomException {
                                propertyEvent.addProperty("PLAC", "");
                            }
                        }); // end of doUnitOfWork
                        place = (PropertyPlace) propertyEvent.getProperty("PLAC");

                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                return place;
            } else if (column == 2) {
                PropertyDate date = (PropertyDate) propertyEvent.getProperty("DATE");
                return date != null ? date : new PropertyDate();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public String getColumnName(int col) {
        return columnsName[col];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getValueAt(0, columnIndex).getClass();
    }

    public void addAll(List<Property> eventsList) {
        this.eventsList.addAll(eventsList);
        fireTableDataChanged();
    }

    public void add(Property event) {
        this.eventsList.add(event);
        fireTableDataChanged();
    }

    public Property getValueAt(int row) {
        return eventsList.get(row);
    }

    public Property remove(int row) {
        Property event = eventsList.remove(row);
        fireTableDataChanged();
        return event;
    }

    public void update() {
        fireTableDataChanged();
    }
}
