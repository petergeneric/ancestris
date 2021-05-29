package ancestris.modules.releve.table;

/**
 * Interface pour les listeners de la sélection de relevés dans la table
 * @author Michel
 */
public interface ReleveTableListener {
    public void tableRecordSelected (int recordIndex, boolean isNew);
    public boolean verifyCurrentRecord();
    public void swapRecordNext();
    public void swapRecordPrevious();
}
