package ancestris.modules.releve;

import ancestris.modules.releve.dnd.TransferableRecord;
import ancestris.modules.releve.dnd.TransferableRecord.TransferableData;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.RecordInfoPlace;
import ancestris.modules.releve.table.ReleveTable;
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
    private final DataManager dataManager;

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
            // je recupere le releve
            Record record = model.getRecord(table.convertRowIndexToModel(table.getSelectedRow()));
            // je complete le lieu dans le releve
            RecordInfoPlace recordsInfoPlace = model.getPlace();
            String fileName;
            if (dataManager.getCurrentFile() != null) {
                fileName = dataManager.getCurrentFile().getName();
            } else {
                fileName = "";
            }

            TransferableRecord.TransferableData data = createTransferableData(component, recordsInfoPlace, fileName, record);
            
            return new TransferableRecord(data);                     
        } else {
            return null;
        }
    }
    
    static public TransferableRecord.TransferableData createTransferableData(JComponent component, RecordInfoPlace recordsInfoPlace, String fileName, Record record) {
        TransferableRecord.TransferableData data = new TransferableRecord.TransferableData();
        data.sourceComponent = component;
        data.fileName = fileName;

        data.cityName = recordsInfoPlace.getCityName();
        data.cityCode = recordsInfoPlace.getCityCode();
        data.countyName = recordsInfoPlace.getCountyName();
        data.stateName = recordsInfoPlace.getStateName();
        data.countryName = recordsInfoPlace.getCountryName();

        data.recordType = record.getType().name();
        data.eventType      = record.getFieldValue(FieldType.eventType);
        data.cote           = record.getFieldValue(FieldType.cote);
        data.freeComment    = record.getFieldValue(FieldType.freeComment);
        data.eventDate      = record.getFieldValue(FieldType.eventDate);
        data.secondDate     = record.getFieldValue(FieldType.secondDate);
        data.generalComment = record.getFieldValue(FieldType.generalComment);
        data.notary         = record.getFieldValue(FieldType.notary);
        data.parish         = record.getFieldValue(FieldType.parish);

        data.participant1.firstName         = record.getFieldValue(FieldType.indiFirstName);
        data.participant1.lastName          = record.getFieldValue(FieldType.indiLastName);
        data.participant1.sex               = record.getFieldValue(FieldType.indiSex);
        data.participant1.age               = record.getFieldValue(FieldType.indiAge);
        data.participant1.birthDate         = record.getFieldValue(FieldType.indiBirthDate);
        data.participant1.birthPlace        = record.getFieldValue(FieldType.indiBirthPlace);
        data.participant1.birthAddress      = record.getFieldValue(FieldType.indiBirthAddress);
        data.participant1.occupation        = record.getFieldValue(FieldType.indiOccupation);
        data.participant1.residence         = record.getFieldValue(FieldType.indiResidence);
        data.participant1.address           = record.getFieldValue(FieldType.indiAddress);
        data.participant1.comment           = record.getFieldValue(FieldType.indiComment);
        data.participant1.marriedFirstName  = record.getFieldValue(FieldType.indiMarriedFirstName);
        data.participant1.marriedLastName   = record.getFieldValue(FieldType.indiMarriedLastName);
        data.participant1.marriedComment    = record.getFieldValue(FieldType.indiMarriedComment);
        data.participant1.marriedOccupation = record.getFieldValue(FieldType.indiMarriedOccupation);
        data.participant1.marriedResidence  = record.getFieldValue(FieldType.indiMarriedResidence);
        data.participant1.marriedAddress    = record.getFieldValue(FieldType.indiMarriedAddress);
        data.participant1.marriedDead       = record.getFieldValue(FieldType.indiMarriedDead);
        data.participant1.fatherFirstName   = record.getFieldValue(FieldType.indiFatherFirstName);
        data.participant1.fatherLastName    = record.getFieldValue(FieldType.indiFatherLastName);
        data.participant1.fatherOccupation  = record.getFieldValue(FieldType.indiFatherOccupation);
        data.participant1.fatherResidence   = record.getFieldValue(FieldType.indiFatherResidence);
        data.participant1.fatherAddress     = record.getFieldValue(FieldType.indiFatherAddress);
        data.participant1.fatherAge         = record.getFieldValue(FieldType.indiFatherAge);  //
        data.participant1.fatherDead        = record.getFieldValue(FieldType.indiFatherDead);
        data.participant1.fatherComment     = record.getFieldValue(FieldType.indiFatherComment);
        data.participant1.motherFirstName   = record.getFieldValue(FieldType.indiMotherFirstName);
        data.participant1.motherLastName    = record.getFieldValue(FieldType.indiMotherLastName);
        data.participant1.motherOccupation  = record.getFieldValue(FieldType.indiMotherOccupation);
        data.participant1.motherResidence   = record.getFieldValue(FieldType.indiMotherResidence);
        data.participant1.motherAddress     = record.getFieldValue(FieldType.indiMotherAddress);
        data.participant1.motherAge         = record.getFieldValue(FieldType.indiMotherAge);
        data.participant1.motherDead        = record.getFieldValue(FieldType.indiMotherDead);
        data.participant1.motherComment     = record.getFieldValue(FieldType.indiMotherComment);

        data.participant2.firstName         = record.getFieldValue(FieldType.wifeFirstName);
        data.participant2.lastName          = record.getFieldValue(FieldType.wifeLastName);
        data.participant2.sex               = record.getFieldValue(FieldType.wifeSex);
        data.participant2.age               = record.getFieldValue(FieldType.wifeAge);
        data.participant2.birthDate         = record.getFieldValue(FieldType.wifeBirthDate);
        data.participant2.birthPlace        = record.getFieldValue(FieldType.wifeBirthPlace);
        data.participant2.birthAddress      = record.getFieldValue(FieldType.wifeBirthAddress);
        data.participant2.occupation        = record.getFieldValue(FieldType.wifeOccupation);
        data.participant2.residence         = record.getFieldValue(FieldType.wifeResidence);
        data.participant2.address           = record.getFieldValue(FieldType.wifeAddress);
        data.participant2.comment           = record.getFieldValue(FieldType.wifeComment);
        data.participant2.marriedFirstName  = record.getFieldValue(FieldType.wifeMarriedFirstName);
        data.participant2.marriedLastName   = record.getFieldValue(FieldType.wifeMarriedLastName);
        data.participant2.marriedComment    = record.getFieldValue(FieldType.wifeMarriedComment);
        data.participant2.marriedOccupation = record.getFieldValue(FieldType.wifeMarriedOccupation);
        data.participant2.marriedResidence  = record.getFieldValue(FieldType.wifeMarriedResidence);
        data.participant2.marriedAddress    = record.getFieldValue(FieldType.wifeMarriedAddress);
        data.participant2.marriedDead       = record.getFieldValue(FieldType.wifeMarriedDead);
        data.participant2.fatherFirstName   = record.getFieldValue(FieldType.wifeFatherFirstName);
        data.participant2.fatherLastName    = record.getFieldValue(FieldType.wifeFatherLastName);
        data.participant2.fatherOccupation  = record.getFieldValue(FieldType.wifeFatherOccupation);
        data.participant2.fatherResidence   = record.getFieldValue(FieldType.wifeFatherResidence);
        data.participant2.fatherAddress     = record.getFieldValue(FieldType.wifeFatherAddress);
        data.participant2.fatherAge         = record.getFieldValue(FieldType.wifeFatherAge);
        data.participant2.fatherDead        = record.getFieldValue(FieldType.wifeFatherDead);
        data.participant2.fatherComment     = record.getFieldValue(FieldType.wifeFatherComment);
        data.participant2.motherFirstName   = record.getFieldValue(FieldType.wifeMotherFirstName);
        data.participant2.motherLastName    = record.getFieldValue(FieldType.wifeMotherLastName);
        data.participant2.motherOccupation  = record.getFieldValue(FieldType.wifeMotherOccupation);
        data.participant2.motherResidence   = record.getFieldValue(FieldType.wifeMotherResidence);
        data.participant2.motherAddress     = record.getFieldValue(FieldType.wifeMotherAddress);
        data.participant2.motherAge         = record.getFieldValue(FieldType.wifeMotherAge);
        data.participant2.motherDead        = record.getFieldValue(FieldType.wifeMotherDead);
        data.participant2.motherComment     = record.getFieldValue(FieldType.wifeMotherComment);

        data.witness1.firstName     = record.getFieldValue(FieldType.witness1FirstName);
        data.witness1.lastName      = record.getFieldValue(FieldType.witness1LastName);
        data.witness1.occupation    = record.getFieldValue(FieldType.witness1Occupation);
        data.witness1.comment       = record.getFieldValue(FieldType.witness1Comment);

        data.witness2.firstName     = record.getFieldValue(FieldType.witness2FirstName);
        data.witness2.lastName      = record.getFieldValue(FieldType.witness2LastName);
        data.witness2.occupation    = record.getFieldValue(FieldType.witness2Occupation);
        data.witness2.comment       = record.getFieldValue(FieldType.witness2Comment);

        data.witness3.firstName     = record.getFieldValue(FieldType.witness3FirstName);
        data.witness3.lastName      = record.getFieldValue(FieldType.witness3LastName);
        data.witness3.occupation    = record.getFieldValue(FieldType.witness3Occupation);
        data.witness3.comment       = record.getFieldValue(FieldType.witness3Comment);

        data.witness4.firstName     = record.getFieldValue(FieldType.witness4FirstName);
        data.witness4.lastName      = record.getFieldValue(FieldType.witness4LastName);
        data.witness4.occupation    = record.getFieldValue(FieldType.witness4Occupation);
        data.witness4.comment       = record.getFieldValue(FieldType.witness4Comment);

        return data;
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
