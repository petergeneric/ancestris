package ancestris.modules.releve.model;

/**
 *
 * @author Michel
 */
public class RecordDeath extends Record {

    
    public RecordDeath() {
        super();       
    }

    @Override
    public RecordType getType() {
        return RecordType.DEATH;
    }

}
