/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.gedcom.history;

import genj.gedcom.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    public GedcomHistory() {
    }

    public GedcomHistory(String gedcomName) {
        this.gedcomName = gedcomName;
        this.historyList = new ArrayList<EntityHistory>();
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
    }

    @Override
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        log.log(Level.INFO, "Entity {0} id {1} deleted", new Object[]{entity.getTag(), entity.getId()});
        historyList.add(new EntityHistory(EntityHistory.DELETED, entity, "", ""));
    }

    @Override
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        log.log(Level.INFO, "Entity {0} id {1} Property {2} changed", new Object[]{property.getEntity().getTag(), property.getEntity().getId(), property.getTag()});
        // Do not archive PropertyChange modification
        if (!(property instanceof PropertyChange)) {
            historyList.add(new EntityHistory(EntityHistory.UPDATED, property, "", property.getValue()));
        }
    }

    @Override
    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        log.log(Level.INFO, "Entity {0} id {1} Property {2} added", new Object[]{added.getEntity().getTag(), added.getEntity().getId(), added.getTag()});
        // Do not archive PropertyChange modification
        if (!(property instanceof PropertyChange)) {
            historyList.add(new EntityHistory(EntityHistory.CREATED, added, "", added.getValue()));
        }
    }

    @Override
    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
        log.log(Level.INFO, "Entity {0} id {1} Property  {2} removed", new Object[]{deleted.getEntity().getTag(), deleted.getEntity().getId(), deleted.getTag()});
        // Do not archive PropertyChange modification
        if (!(property instanceof PropertyChange)) {
            historyList.add(new EntityHistory(EntityHistory.DELETED, deleted, "", ""));
        }
    }
}
