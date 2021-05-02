package ancestris.modules.releve.dnd;

import ancestris.modules.releve.merge.MergeDialog;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.DelegatedTransferable;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.TransferHandler;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

/**
 * Cette classe encapsule un releve pour le rendre transportable par Drag and Drop
 * @author Michel
 */
public class TransferableRecord  implements DelegatedTransferable {

    public static final DataFlavor recordFlavor = new DataFlavor(TransferableData.class, "MergeRecord");

    // liste des types transférables
    protected static DataFlavor[] supportedFlavors = {
        DELEGATED_FLAVOR,
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
    public TransferableRecord(TransferableData data) {
        this.data = data;
    }


    /**
     * retourne les types de données transférable
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return supportedFlavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (DataFlavor df : supportedFlavors) {
            if (df.equals(flavor)) {
                return true;
            }
        } 
        return false;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor == recordFlavor) {
            return data;
        } else if (flavor == DELEGATED_FLAVOR) {
            return this;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }        
    }

    /**
     * Ancestris delegation
     */
    @Override
    public boolean runDelegation(TopComponent tc, Gedcom targetGedcom, Entity targetEntiry, TransferHandler.TransferSupport support) {
        
        if ((tc instanceof AncestrisTopComponent) && targetGedcom != null) {
            try {
                TransferableRecord.TransferableData data = (TransferableRecord.TransferableData) support.getTransferable().getTransferData(recordFlavor);
                MergeDialog.show(tc, targetGedcom, targetEntiry, data, true);
            } catch (UnsupportedFlavorException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return true;
        } 
        return false;
    }

    /**
     * classe encapsulant le relevé et la source 
     * necessaire parce que  getTransferData() ne peut retourner qu'un objet
     */
    static public class TransferableData {
        public Component sourceComponent;
        
        public String fileName = "";
        
        // info place
        public String hamlet = "";
        public String cityName = "";
        public String cityCode = "";
        public String countyName = "";
        public String stateName = "";
        public String countryName = "";

        public String recordType = "";
        public String eventType = "";
        //public FieldPlace eventPlace;
        public String cote = "";
        public String freeComment = "";
        public String eventDate;
        public String secondDate;
        public String generalComment = "";
        public String notary = "";
        public String parish = "";
        public Participant participant1 = new Participant();
        public Participant participant2 = new Participant();
        public Witness witness1 = new Witness();
        public Witness witness2 = new Witness();
        public Witness witness3 = new Witness();
        public Witness witness4 = new Witness();
        
        public class Participant {

            public String firstName = "";
            public String lastName = "";
            public String sex = "";
            public String age = "";
            public String birthDate;
            public String birthPlace = "";
            public String birthAddress = "";
            public String occupation = "";
            public String residence = "";
            public String address = "";
            public String comment = "";
            public String marriedFirstName = "";
            public String marriedLastName = "";
            public String marriedComment = "";
            public String marriedOccupation = "";
            public String marriedResidence = "";
            public String marriedAddress = "";
            public String marriedDead = "";
            public String fatherFirstName = "";
            public String fatherLastName = "";
            public String fatherOccupation = "";
            public String fatherResidence = "";
            public String fatherAddress = "";
            public String fatherAge = "";
            public String fatherDead = "";
            public String fatherComment = "";
            public String motherFirstName = "";
            public String motherLastName = "";
            public String motherOccupation = "";
            public String motherResidence = "";
            public String motherAddress = "";
            public String motherAge = "";
            public String motherDead = "";
            public String motherComment = "";
        }
        
        public class Witness {
            public String firstName = "";
            public String lastName = "";
            public String occupation = "";
            public String comment = "";
        }
        
        
   }
    
}
