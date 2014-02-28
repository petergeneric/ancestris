package ancestris.modules.releve.table;

/**
 * Interface pour les listeners de la selection de releves dans la table
 * @author Michel
 */
public interface TableSelectionListener {
    void tableRecordSelected (int recordIndex, boolean isNew);
    public int getCurrentRecordIndex();
    boolean verifyRecord();
    void swapRecordNext();
    void swapRecordPrevious();
}
