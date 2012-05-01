package ancestris.modules.releve;

/**
 * Interface pour les listeners de la selection de releves dans la table
 * @author Michel
 */
public interface TableSelectionListener {
    void rowSelected (int rowIndex, boolean isNew);
    public int getCurrentRecordIndex();
}
