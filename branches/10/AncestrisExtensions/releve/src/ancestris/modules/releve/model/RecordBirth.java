package ancestris.modules.releve.model;

/**
 *
 * @author Michel
 */
public class RecordBirth extends Record {

    
    public RecordBirth() {
        super();
    }

    @Override
    public RecordType getType() {
        return RecordType.BIRTH;
    }
}
