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

    public GedcomLink(Record record, Entity entity, Property property) {
        this.record = record;
        this.entity= entity;
        this.property = property;
    }
        
    private final Record record;
    private final Entity entity;
    private final Property property;
}
