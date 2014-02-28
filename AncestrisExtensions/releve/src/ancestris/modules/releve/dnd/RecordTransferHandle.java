package ancestris.modules.releve.dnd;

import ancestris.modules.releve.dnd.TransferableRecord.TransferableData;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.table.ReleveTable;
import ancestris.modules.releve.model.FieldPlace;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.table.TableModelRecordAbstract;
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
    private DataManager dataManager;

    public RecordTransferHandle(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }

    @Override
    public Transferable createTransferable(JComponent component) {
        if (component instanceof ReleveTable ) {
            ReleveTable table = (ReleveTable) component ;
            TableModelRecordAbstract model = (TableModelRecordAbstract) table.getModel();
            // je recupere le clone du releve
            Record record = model.getRecord(table.convertRowIndexToModel(table.getSelectedRow()));
            Record clonedRecord = record.clone();
            // je complete le lieu dans le releve
            FieldPlace recordsInfoPlace = new FieldPlace();
            recordsInfoPlace.setValue(model.getPlace());
            String sourceTitle;
            if (dataManager.getCurrentFile() != null ) {
                sourceTitle = MergeOptionPanel.SourceModel.getModel().getSource(dataManager.getCurrentFile().getName());
            } else {
                sourceTitle = "";
            }
            MergeRecord mergeRecord = new MergeRecord(recordsInfoPlace, sourceTitle, clonedRecord);
            return new TransferableRecord(mergeRecord, component);
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
            if ( data.sourceComponent.equals(support.getComponent())){
                // je refuse d'importer un releve si la destination est identique a la source
                return false;
            }
        } catch (UnsupportedFlavorException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        return true;
    }

//    /**
//     * import d'un relevé
//     * @param support
//     * @return
//     */
//    @Override
//    public boolean importData(TransferHandler.TransferSupport support) {
//        if (!canImport(support)) {
//            return false;
//        }
//
//        try {
//            TransferableData data = null;
//            data = (TransferableData) support.getTransferable().getTransferData(TransferableRecord.recordFlavor);
//            if (support.getComponent() instanceof ReleveTable) {
//                ReleveTable table = (ReleveTable) support.getComponent();
//                table.dropRecord(data.record);
//                return true;
//            }
//        } catch (UnsupportedFlavorException e) {
//            return false;
//        } catch (IOException e) {
//            return false;
//        }
//        return false;
//
//    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {

    }

}
