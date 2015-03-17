package ancestris.modules.releve.model;

import genj.gedcom.Entity;
import genj.gedcom.Property;

/**
 *
 * @author Michel
 */

public class GedcomLink {

    /**
     * @return the record
     */
    public Record getRecord() {
        return record;
    }

    /**
     * @return the entity
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * @return the property
     */
    public Property getProperty() {
        return property;
    }

    /**
     * @return the compareResult
     */
    public CompareResult getCompareResult() {
        return compareResult;
    }

    /**
     * @param record the record to set
     */
    public void setRecord(Record record) {
        this.record = record;
    }

    /**
     * @param entity the entity to set
     */
    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    /**
     * @param property the property to set
     */
    public void setProperty(Property property) {
        this.property = property;
    }

    /**
     * @param compareResult the compareResult to set
     */
    public void setCompareResult(CompareResult compareResult) {
        this.compareResult = compareResult;
    }
    static public enum CompareResult {
        EQUAL,
        COMPATIBLE,
        CONFLIT,
        NOT_APPLICABLE
    }
    
    public GedcomLink(Record record) {
        this.record = record;
        entity= null;
        property = null;
        compareResult = CompareResult.NOT_APPLICABLE;
    }
    
    private Record record;
    private Entity entity;
    private Property property;
    private CompareResult compareResult;
}
