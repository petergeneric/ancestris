package ancestris.modules.releve.model;

/**
 *
 * @author Michel
 */
public interface RecordModelListener {

    void recordInserted(int firstIndex, int lastIndex);
    void recordDeleted(int firstIndex, int lastIndex);
    void recordUpdated(int firstIndex, int lastIndex);
    void recordUpdated(int recordIndex, Record.FieldType filedType);
    void allChanged();

}
