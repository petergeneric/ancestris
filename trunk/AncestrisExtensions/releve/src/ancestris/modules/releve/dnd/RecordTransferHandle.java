package ancestris.modules.releve.dnd;

import ancestris.modules.releve.dnd.TransferableRecord.TransferableData;
import ancestris.modules.releve.ReleveTable;
import ancestris.modules.releve.model.ModelAbstract;
import ancestris.modules.releve.model.Record;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * Cette classe traite le transfert de releve entre les tables et les editeurs
 * @author Michel
 */
public class RecordTransferHandle extends TransferHandler {

    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }

    @Override
    public Transferable createTransferable(JComponent c) {
        if (c instanceof ReleveTable ) {
            ReleveTable table = (ReleveTable) c ;
            // je recupere le clone du releve 
            Record record = ((ModelAbstract)table.getModel()).getRecord(table.convertRowIndexToModel(table.getSelectedRow()));
            record = (Record) record.clone();
            // je complete le lieu dans le releve
            record.setEventPlace(
                    table.getDataManager().getCityName(),
                    table.getDataManager().getCityCode(),
                    table.getDataManager().getCountyName(),
                    table.getDataManager().getStateName(),
                    table.getDataManager().getCountryName());
            return new TransferableRecord(record, c);
        } else {
            return null;
        }
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        //System.out.println(" canImport "+ support.getComponent().getClass().getName());
        if (!support.isDataFlavorSupported(TransferableRecord.recordFlavor)) {
            return false;
        }

        try {
            TransferableData data = (TransferableData)  support.getTransferable().getTransferData(TransferableRecord.recordFlavor);
            if ( data.source.equals(support.getComponent())){
                return false;
            }
        } catch (UnsupportedFlavorException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
//        if ( support.getComponent().equals(source)) {
//           return false;
//        }

        return true;
    }

    /**
     * import d'un relevé
     * @param support
     * @return
     */
    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        try {
            TransferableData data = null;
            data = (TransferableData) support.getTransferable().getTransferData(TransferableRecord.recordFlavor);
            if (support.getComponent() instanceof ReleveTable) {
                ReleveTable table = (ReleveTable) support.getComponent();
                table.dropRecord(data.record);
                return true;
            }
        } catch (UnsupportedFlavorException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return false;

    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {

    }

}
