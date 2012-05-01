package ancestris.modules.releve;

/**
 * Interface pour les listeners de la selection de releves dans la table
 * @author Michel
 */
public interface ReleveEditorListener {
    /**
     * retourn l'index du relevé en cours d'édition dans l'editeur
     * @return
     */
    public int getCurrentRecordIndex();
}
