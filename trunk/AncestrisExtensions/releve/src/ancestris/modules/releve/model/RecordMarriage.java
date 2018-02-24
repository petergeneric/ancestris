package ancestris.modules.releve.model;

/**
 *
 * @author Michel
 */
public class RecordMarriage extends Record {

    public RecordMarriage() {
        super();
    }

    @Override
    public RecordType getType() {
        return RecordType.MARRIAGE;
    }
}
