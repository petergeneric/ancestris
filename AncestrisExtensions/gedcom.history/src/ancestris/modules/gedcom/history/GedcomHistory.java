/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.gedcom.history;

import genj.gedcom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author dbaron
 */
@XmlRootElement(name = "GedcomHistory", namespace = "ancestris.modules.gedcom.history")
public class GedcomHistory implements GedcomListener {

    private static final Logger log = Logger.getLogger(GedcomHistoryPlugin.class.getName());
    private String gedcomName = "";
    private ArrayList<EntityHistory> historyList = null;
    // listeners
    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();

    public GedcomHistory() {
    }

    public GedcomHistory(String gedcomName) {
        this.gedcomName = gedcomName;
        this.historyList = new ArrayList<EntityHistory>();
    }

    public void clear () {
        historyList.clear();
    }
    /**
     * @return the gedcomName
     */
    public String getGedcomName() {
        return gedcomName;
    }

    /**
     * @return the HistoryList
     */
    public ArrayList<EntityHistory> getHistoryList() {
        return historyList;
    }

    /**
     * @param gedcomName the gedcomName to set
     */
    public void setGedcomName(String gedcomName) {
        this.gedcomName = gedcomName;
    }

    /**
     * @param HistoryList the HistoryList to set
     */
    public void setHistoryList(ArrayList<EntityHistory> historyList) {
        this.historyList = historyList;
    }

    @Override
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
        log.log(Level.INFO, "Entity {0} id {1} added", new Object[]{entity.getTag(), entity.getId()});
        historyList.add(new EntityHistory(EntityHistory.CREATED, entity, "", ""));
        fireChange();
    }

    @Override
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        log.log(Level.INFO, "Entity {0} id {1} deleted", new Object[]{entity.getTag(), entity.getId()});
        historyList.add(new EntityHistory(EntityHistory.DELETED, entity, "", ""));
        fireChange();
    }

    @Override
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        log.log(Level.INFO, "Entity {0} id {1} Property {2} changed", new Object[]{property.getEntity().getTag(), property.getEntity().getId(), property.getTag()});
        // Do not archive PropertyChange modification
        if (!(property instanceof PropertyChange)) {
            historyList.add(new EntityHistory(EntityHistory.UPDATED, property, "", property.getValue()));
            fireChange();
        }
    }

    @Override
    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        log.log(Level.INFO, "Entity {0} id {1} Property {2} added", new Object[]{added.getEntity().getTag(), added.getEntity().getId(), added.getTag()});
        // Do not archive PropertyChange modification
        if (!(property instanceof PropertyChange)) {
            historyList.add(new EntityHistory(EntityHistory.CREATED, added, "", added.getValue()));
            fireChange();
        }
    }

    @Override
    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
        log.log(Level.INFO, "Entity {0} id {1} Property  {2} removed", new Object[]{deleted.getEntity().getTag(), deleted.getEntity().getId(), deleted.getTag()});
        // Do not archive PropertyChange modification
        if (!(property instanceof PropertyChange)) {
            historyList.add(new EntityHistory(EntityHistory.DELETED, deleted, "", ""));
            fireChange();
        }
    }

    /**
     * Adds a Listener which will be notified when data changes
     */
    public void addChangeListener(ChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener can't be null");
        }
        listeners.add(listener);
    }

    /**
     * Removes a Listener from receiving notifications
     */
    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    public void fireChange() {
        for (ChangeListener listener : listeners) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }
}
