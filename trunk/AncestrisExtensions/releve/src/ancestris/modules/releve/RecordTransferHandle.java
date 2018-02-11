package ancestris.modules.releve;

import ancestris.modules.releve.dnd.TransferableRecord;
import ancestris.modules.releve.dnd.TransferableRecord.TransferableData;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.table.ReleveTable;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.RecordInfoPlace;
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

        data.locality = recordsInfoPlace.getLocality();
        data.cityName = recordsInfoPlace.getCityName();
        data.cityCode = recordsInfoPlace.getCityCode();
        data.countyName = recordsInfoPlace.getCountyName();
        data.stateName = recordsInfoPlace.getStateName();
        data.countryName = recordsInfoPlace.getCountryName();

        data.recordType = record.getType().name();
        if( record.getEventType() != null )                     data.eventType      = record.getEventType().toString();
        if( record.getCote() != null )                          data.cote           = record.getCote().toString();
        if( record.getFreeComment() != null )                   data.freeComment    = record.getFreeComment().toString();
        if( record.getEventDateProperty() != null )             data.eventDate      = record.getEventDateProperty();
        if( record.getEventSecondDateProperty() != null )       data.secondDate     = record.getEventSecondDateProperty();
        if( record.getGeneralComment() != null )                data.generalComment = record.getGeneralComment().toString();
        if( record.getNotary() != null )                        data.notary         = record.getNotary().toString();
        if( record.getParish() != null )                        data.parish         = record.getParish().toString();

        if( record.getIndi().getFirstName() != null )           data.participant1.firstName         = record.getIndi().getFirstName().toString();
        if( record.getIndi().getLastName() != null )            data.participant1.lastName          = record.getIndi().getLastName().toString();
        if( record.getIndi().getSex() != null )                 data.participant1.sex               = record.getIndi().getSex().toString();
        if( record.getIndi().getAge() != null )                 data.participant1.age               = record.getIndi().getAge().getDelta();
        if( record.getIndi().getBirthDate() != null )           data.participant1.birthDate         = record.getIndi().getBirthDate().getPropertyDate();
        if( record.getIndi().getBirthPlace() != null )          data.participant1.birthPlace        = record.getIndi().getBirthPlace().toString();
        if( record.getIndi().getBirthAddress() != null )        data.participant1.birthAddress      = record.getIndi().getBirthAddress().toString();
        if( record.getIndi().getOccupation() != null )          data.participant1.occupation        = record.getIndi().getOccupation().toString();
        if( record.getIndi().getResidence() != null )           data.participant1.residence         = record.getIndi().getResidence().toString();
        if( record.getIndi().getAddress() != null )             data.participant1.address           = record.getIndi().getAddress().toString();
        if( record.getIndi().getComment() != null )             data.participant1.comment           = record.getIndi().getComment().toString();
        if( record.getIndi().getMarriedFirstName() != null )    data.participant1.marriedFirstName  = record.getIndi().getMarriedFirstName().toString();
        if( record.getIndi().getMarriedLastName() != null )     data.participant1.marriedLastName   = record.getIndi().getMarriedLastName().toString();
        if( record.getIndi().getMarriedComment() != null )      data.participant1.marriedComment    = record.getIndi().getMarriedComment().toString();
        if( record.getIndi().getMarriedOccupation() != null )   data.participant1.marriedOccupation = record.getIndi().getMarriedOccupation().toString();
        if( record.getIndi().getMarriedResidence() != null )    data.participant1.marriedResidence  = record.getIndi().getMarriedResidence().toString();
        if( record.getIndi().getMarriedAddress() != null )      data.participant1.marriedAddress    = record.getIndi().getMarriedAddress().toString();
        if( record.getIndi().getMarriedDead() != null )         data.participant1.marriedDead       = record.getIndi().getMarriedDead().getState().name();
        if( record.getIndi().getFatherFirstName() != null )     data.participant1.fatherFirstName   = record.getIndi().getFatherFirstName().toString();
        if( record.getIndi().getFatherLastName() != null )      data.participant1.fatherLastName    = record.getIndi().getFatherLastName().toString();
        if( record.getIndi().getFatherOccupation() != null )    data.participant1.fatherOccupation  = record.getIndi().getFatherOccupation().toString();
        if( record.getIndi().getFatherResidence() != null )     data.participant1.fatherResidence   = record.getIndi().getFatherResidence().toString();
        if( record.getIndi().getFatherAddress() != null )       data.participant1.fatherAddress     = record.getIndi().getFatherAddress().toString();
        if( record.getIndi().getFatherAge() != null )           data.participant1.fatherAge         = record.getIndi().getFatherAge().getDelta();
        if( record.getIndi().getFatherDead() != null )          data.participant1.fatherDead        = record.getIndi().getFatherDead().getState().name();
        if( record.getIndi().getFatherComment() != null )       data.participant1.fatherComment     = record.getIndi().getFatherComment().toString();
        if( record.getIndi().getMotherFirstName() != null )     data.participant1.motherFirstName   = record.getIndi().getMotherFirstName().toString();
        if( record.getIndi().getMotherLastName() != null )      data.participant1.motherLastName    = record.getIndi().getMotherLastName().toString();
        if( record.getIndi().getMotherOccupation() != null )    data.participant1.motherOccupation  = record.getIndi().getMotherOccupation().toString();
        if( record.getIndi().getMotherResidence() != null )     data.participant1.motherResidence   = record.getIndi().getMotherResidence().toString();
        if( record.getIndi().getMotherAddress() != null )       data.participant1.motherAddress     = record.getIndi().getMotherAddress().toString();
        if( record.getIndi().getMotherAge() != null )           data.participant1.motherAge         = record.getIndi().getMotherAge().getDelta();
        if( record.getIndi().getMotherDead() != null )          data.participant1.motherDead        = record.getIndi().getMotherDead().getState().name();
        if( record.getIndi().getMotherComment() != null )       data.participant1.motherComment     = record.getIndi().getMotherComment().toString();

        if( record.getWife().getFirstName() != null )           data.participant2.firstName         = record.getWife().getFirstName().toString();
        if( record.getWife().getLastName() != null )            data.participant2.lastName          = record.getWife().getLastName().toString();
        if( record.getWife().getSex() != null )                 data.participant2.sex               = record.getWife().getSex().toString();
        if( record.getWife().getAge() != null )                 data.participant2.age               = record.getWife().getAge().getDelta();
        if( record.getWife().getBirthDate() != null )           data.participant2.birthDate         = record.getWife().getBirthDate().getPropertyDate();
        if( record.getWife().getBirthPlace() != null )          data.participant2.birthPlace        = record.getWife().getBirthPlace().toString();
        if( record.getWife().getBirthAddress() != null )        data.participant2.birthAddress      = record.getWife().getBirthAddress().toString();
        if( record.getWife().getOccupation() != null )          data.participant2.occupation        = record.getWife().getOccupation().toString();
        if( record.getWife().getResidence() != null )           data.participant2.residence         = record.getWife().getResidence().toString();
        if( record.getWife().getAddress() != null )             data.participant2.address           = record.getWife().getAddress().toString();
        if( record.getWife().getComment() != null )             data.participant2.comment           = record.getWife().getComment().toString();
        if( record.getWife().getMarriedFirstName() != null )    data.participant2.marriedFirstName  = record.getWife().getMarriedFirstName().toString();
        if( record.getWife().getMarriedLastName() != null )     data.participant2.marriedLastName   = record.getWife().getMarriedLastName().toString();
        if( record.getWife().getMarriedComment() != null )      data.participant2.marriedComment    = record.getWife().getMarriedComment().toString();
        if( record.getWife().getMarriedOccupation() != null )   data.participant2.marriedOccupation = record.getWife().getMarriedOccupation().toString();
        if( record.getWife().getMarriedResidence() != null )    data.participant2.marriedResidence  = record.getWife().getMarriedResidence().toString();
        if( record.getWife().getMarriedAddress() != null )      data.participant2.marriedAddress    = record.getWife().getMarriedAddress().toString();
        if( record.getWife().getMarriedDead() != null )         data.participant2.marriedDead       = record.getWife().getMarriedDead().getState().name();
        if( record.getWife().getFatherFirstName() != null )     data.participant2.fatherFirstName   = record.getWife().getFatherFirstName().toString();
        if( record.getWife().getFatherLastName() != null )      data.participant2.fatherLastName    = record.getWife().getFatherLastName().toString();
        if( record.getWife().getFatherOccupation() != null )    data.participant2.fatherOccupation  = record.getWife().getFatherOccupation().toString();
        if( record.getWife().getFatherResidence() != null )     data.participant2.fatherResidence   = record.getWife().getFatherResidence().toString();
        if( record.getWife().getFatherAddress() != null )       data.participant2.fatherAddress     = record.getWife().getFatherAddress().toString();
        if( record.getWife().getFatherAge() != null )           data.participant2.fatherAge         = record.getWife().getFatherAge().getDelta();
        if( record.getWife().getFatherDead() != null )          data.participant2.fatherDead        = record.getWife().getFatherDead().getState().name();
        if( record.getWife().getFatherComment() != null )       data.participant2.fatherComment     = record.getWife().getFatherComment().toString();
        if( record.getWife().getMotherFirstName() != null )     data.participant2.motherFirstName   = record.getWife().getMotherFirstName().toString();
        if( record.getWife().getMotherLastName() != null )      data.participant2.motherLastName    = record.getWife().getMotherLastName().toString();
        if( record.getWife().getMotherOccupation() != null )    data.participant2.motherOccupation  = record.getWife().getMotherOccupation().toString();
        if( record.getWife().getMotherResidence() != null )     data.participant2.motherResidence   = record.getWife().getMotherResidence().toString();
        if( record.getWife().getMotherAddress() != null )       data.participant2.motherAddress     = record.getWife().getMotherAddress().toString();
        if( record.getWife().getMotherAge() != null )           data.participant2.motherAge         = record.getWife().getMotherAge().getDelta();
        if( record.getWife().getMotherDead() != null )          data.participant2.motherDead        = record.getWife().getMotherDead().getState().name();
        if( record.getWife().getMotherComment() != null )       data.participant2.motherComment     = record.getWife().getMotherComment().toString();

        data.witness1.firstName     = record.getWitness1().getFirstName().toString();
        data.witness1.lastName      = record.getWitness1().getLastName().toString();
        data.witness1.occupation    = record.getWitness1().getOccupation().toString();
        data.witness1.comment       = record.getWitness1().getComment().toString();

        data.witness2.firstName     = record.getWitness2().getFirstName().toString();
        data.witness2.lastName      = record.getWitness2().getLastName().toString();
        data.witness2.occupation    = record.getWitness2().getOccupation().toString();
        data.witness2.comment       = record.getWitness2().getComment().toString();

        data.witness3.firstName     = record.getWitness3().getFirstName().toString();
        data.witness3.lastName      = record.getWitness3().getLastName().toString();
        data.witness3.occupation    = record.getWitness3().getOccupation().toString();
        data.witness3.comment       = record.getWitness3().getComment().toString();

        data.witness4.firstName     = record.getWitness4().getFirstName().toString();
        data.witness4.lastName      = record.getWitness4().getLastName().toString();
        data.witness4.occupation    = record.getWitness4().getOccupation().toString();
        data.witness4.comment       = record.getWitness4().getComment().toString();

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
