package ancestris.modules.releve.model;


/**
 *
 * @author Michel
 */
public class RecordMisc extends Record {

    public RecordMisc() {
        super();
    }

    @Override
    public RecordType getType() {
        return RecordType.MISC;
    }
}
