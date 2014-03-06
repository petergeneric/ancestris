package ancestris.modules.releve.table;

/**
 * Interface pour les listeners de la sélection de relevés dans la table
 * @author Michel
 */
public interface TableSelectionListener {
    public void tableRecordSelected (int recordIndex, boolean isNew);
    public int getCurrentRecordIndex();
    public boolean verifyRecord();
    public void swapRecordNext();
    public void swapRecordPrevious();
    public void renumberRecords();
}
