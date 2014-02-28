package ancestris.modules.releve.dnd;

import ancestris.modules.releve.model.FieldPlace;
import ancestris.modules.releve.model.Record;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Cette classe encapsule un releve pour le rendre transportable par Drag and Drop
 * @author Michel
 */
public class TransferableRecord  implements Transferable {

    final protected static DataFlavor recordFlavor = new DataFlavor(MergeRecord.class, "MergeRecord");

    // liste des types transférables
    protected static DataFlavor[] supportedFlavors = {
        recordFlavor
    };

    TransferableData data = new TransferableData();

    /**
     * Le constructeur memoise le relevé et la source du DnD
     * Remarque : la connaissance de la source permettra de verifier si le
     * destinataire du DnD est bien différent de la source du DnD
     * @param record
     * @param sourceComponent
     */
    public TransferableRecord(MergeRecord mergeRecord, Component sourceComponent) {
        this.data.mergeRecord = mergeRecord;
        this.data.sourceComponent = sourceComponent;
    }

    /**
     * retourne les types de données tranférable
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return supportedFlavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (flavor.equals(recordFlavor) ) {
            return true;
        }
        return false;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return data;
    }

    /**
     * classe encapsulant le relevé et la source 
     * necessaire parce que  getTransferData() ne peut retourner qu'un objet
     */
    protected class TransferableData {
        protected MergeRecord mergeRecord;
        protected Component sourceComponent;
    }
}
