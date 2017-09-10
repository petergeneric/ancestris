/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.gedcom.history;

import genj.gedcom.Entity;
import genj.gedcom.Property;
import java.util.GregorianCalendar;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author dbaron
 */
@XmlRootElement(name = "EntityHistory")
@XmlType(propOrder = {"date", "entityTag", "entityId", "action", "property", "propertyPath", "oldValue", "newValue"})
public class EntityHistory {

    private GregorianCalendar date;
    private String entityTag;
    private String entityId;
    private String action;
    private String property;
    private String propertyPath;
    private String oldValue;
    private String newValue;
    public final static String CREATED = "Created";
    public final static String UPDATED = "Updated";
    public final static String DELETED = "Deleted";

    public EntityHistory() {
    }

    public EntityHistory(String action, Entity entity, Property property, String oldValue, String NewValue) {
            this.action = action;
            this.date = new GregorianCalendar();
            this.entityTag = entity.getTag();
            this.entityId = entity.getId();
            this.property = property.getTag();
            this.propertyPath = property.getPath(true).toString();  
            this.newValue = NewValue;
            this.oldValue = oldValue;
    }

    /**
     * @return the EntityType
     */
    public String getEntityTag() {
        return entityTag;
    }

    /**
     * @return the PropertyPath
     */
    public String getPropertyPath() {
        return propertyPath;
    }

    /**
     * @return the oldValue
     */
    public String getOldValue() {
        return oldValue;
    }

    /**
     * @return the newVAlue
     */
    public String getNewValue() {
        return newValue;
    }

    /**
     * @return the entityId
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * @return the property
     */
    public String getProperty() {
        return property;
    }

    /**
     * @return the date
     */
    public GregorianCalendar getDate() {
        return date;
    }

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @param entityTag the entityTag to set
     */
    public void setEntityTag(String entityTag) {
        this.entityTag = entityTag;
    }

    /**
     * @param action the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @param entityId the entityId to set
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    /**
     * @param date the date to set
     */
    public void setDate(GregorianCalendar date) {
        this.date = date;
    }

    /**
     * @param propertyPath the propertyPath to set
     */
    public void setPropertyPath(String propertyPath) {
        this.propertyPath = propertyPath;
    }

    /**
     * @param oldValue the oldValue to set
     */
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    /**
     * @param newVAlue the newVAlue to set
     */
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    /**
     * @param property the property to set
     */
    public void setProperty(String property) {
        this.property = property;
    }
}
