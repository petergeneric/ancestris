package ancestris.modules.releve.model;

import ancestris.gedcom.GedcomDirectory;
import ancestris.modules.releve.file.FileBuffer;
import genj.gedcom.Context;
import java.util.ArrayList;
import org.openide.util.NbPreferences;

/**
 *
 * @author Michel
 */
public class DataManager implements PlaceManager {

    private ModelBirth releveBirthModel = new ModelBirth();
    private ModelMarriage releveMarriageModel = new ModelMarriage();
    private ModelDeath releveDeathModel  = new ModelDeath();
    private ModelMisc releveMiscModel = new ModelMisc();
    private ModelAll   releveAllModel = new ModelAll();
    private CompletionProvider completionProvider  = new CompletionProvider();

    //donnés de configuration
    boolean copyFreeComment = true;
    boolean duplicateControlEnabled = true;
    boolean valueControlEnabled = true;
    
    // données volatiles
    private int lastRecordNo = 0;
    private String freeComment = "";

    

    // previous record

    public enum ModelType { birth, marriage, death, misc, all }

    public DataManager () {
        loadOptions();
    }

    /**
     * indique si les données ont été modifiées depuis la dernière sauvegarde
     * @return
     */
    public boolean isDirty() {
        boolean result = false;
        result |= releveBirthModel.isDirty();
        result |= releveMarriageModel.isDirty();
        result |= releveDeathModel.isDirty();
        result |= releveMiscModel.isDirty();
        result |= releveAllModel.isDirty();
        result |= placeChanged;
        return result;
    }

     /**
     * reinitialise l'indicateur des modifications
     * @return
     */
    public void resetDirty() {
        releveBirthModel.resetDirty();
        releveMarriageModel.resetDirty();
        releveDeathModel.resetDirty();
        releveMiscModel.resetDirty();
        releveAllModel.resetDirty();
        placeChanged = false;
    }

    public int createRecord( ModelAbstract model) {
        Record record = model.createRecord();

        if (getCopyFreeCommentEnabled() ) {
            // je valorise le numero de photo avec la valeur par defaut
            String defaultValue = this.getDefaultFreeComment();
            record.setFreeComment(defaultValue);
        }
        
        return addRecord(record, true);
    }

    /**
     * ajoute un nouveau releve
     * le numero de releve est calculé automatiquement
     * @param record
     * @return
     */
    public int addRecord(Record record, boolean updateGui) {
        int recordIndex = 0;

        // j'ajoute le releve dans le modele correspondant
        if (record instanceof RecordBirth) {
            recordIndex = releveBirthModel.addRecord(record, updateGui);
        } else  if (record instanceof RecordMarriage) {
            recordIndex = releveMarriageModel.addRecord(record, updateGui);
        } else  if (record instanceof RecordDeath) {
            recordIndex = releveDeathModel.addRecord(record, updateGui);
        } else  if (record instanceof RecordMisc) {
            recordIndex = releveMiscModel.addRecord(record, updateGui);
        }

        releveAllModel.addRecord(record, updateGui);

        // j'attribue un numero au releve s'il n'est pas déjà renseigné
        if ( record.recordNo == 0 )  {
            record.recordNo = getNextRecordNo();
        }  else {
            // le numéro est déjà rensigné.
            // je mémorise ce numéro s'il est plus gand que ceux des relevés déjà
            // présents.
            if ( record.recordNo > lastRecordNo ) {
                setNextRecordNo(record.recordNo);
            }
        }
        //
        getCompletionProvider().addRecord(record);
        return recordIndex;
    }
   

    /**
     * ajouter les releves 
     * @param fileBuffer  buffer contenant les releves a ajouter
     * @param append  si false , vide les modeles avant d'ajouter les nouveaux relevés
     * @param defaultPlace lieu par defaut des releves
     * @param forceDefaultPlace 1=remplace les lieux des releves par le lieu par défaut, 0=n'ajoute que les releves dont le lieu est le lieu par defaut
     */
    public void addRecords(FileBuffer fileBuffer, boolean append, String defaultPlace, int forceDefaultPlace) {
        if (!append ) {
            removeAll();
        }

        // je recupere le nombre de lignes
        int firstRowBirth = releveBirthModel.getRowCount();
        int firstRowMarriage = releveMarriageModel.getRowCount();
        int firstRowDeath = releveDeathModel.getRowCount();
        int firstRowMisc = releveMiscModel.getRowCount();
        int firstRowAll = releveAllModel.getRowCount();

        // j'ajoute les releves
        for (Record record : fileBuffer.getRecords()) {
            FieldPlace fieldPlace = record.getEventPlace();
            String place = fieldPlace.getValue();
            if (!place.equals(defaultPlace)) {
                if (forceDefaultPlace == 1) {
                    // je supprime le lieu pour gagner de la place en mémoire
                    record.eventPlace = null;
                    this.addRecord(record, false);
                } else {
                    // j'ignore le releve
                }
            } else {
                // je supprime le lieu pour gagner de la place en mémoire
                record.eventPlace = null;
                this.addRecord(record, false);
            }
        }

        // si des lignes ont été ajoutées , je previens les listeners
        if (releveBirthModel.getRowCount() > firstRowBirth) {
            releveBirthModel.fireTableRowsInserted(firstRowBirth, releveBirthModel.getRowCount() - 1);
        }
        if (releveMarriageModel.getRowCount() > firstRowMarriage) {
            releveMarriageModel.fireTableRowsInserted(firstRowMarriage, releveMarriageModel.getRowCount() - 1);
        }
        if (releveDeathModel.getRowCount() > firstRowDeath) {
            releveDeathModel.fireTableRowsInserted(firstRowDeath, releveDeathModel.getRowCount() - 1);
        }
        if (releveMiscModel.getRowCount() > firstRowMisc) {
            releveMiscModel.fireTableRowsInserted(firstRowMisc, releveMiscModel.getRowCount() - 1);
        }
        if (releveAllModel.getRowCount() > firstRowAll) {
            releveAllModel.fireTableRowsInserted(firstRowMisc, releveAllModel.getRowCount() - 1);
        }

        // RAZ de l'etat du modele
        if (!append) {
            resetDirty();
        }
    }

    public void removeRecord(Record record) {

        // je libere le numero de record si c'est le dernier
        setPreviousRecordNo(record.recordNo);
        getCompletionProvider().removeRecord(record);

        if (record instanceof RecordBirth) {
            releveBirthModel.removeRecord(record);
        } else  if (record instanceof RecordMarriage) {
            releveMarriageModel.removeRecord(record);
        } else  if (record instanceof RecordDeath) {
            releveDeathModel.removeRecord(record);
        } else  if (record instanceof RecordMisc) {
            releveMiscModel.removeRecord(record);
        }

        releveAllModel.removeRecord(record);
    }

    public void removeAll() {
        getCompletionProvider().removeAll();
        releveBirthModel.removeAll();
        releveMarriageModel.removeAll();
        releveDeathModel.removeAll();
        releveMiscModel.removeAll();
        releveAllModel.removeAll();
        lastRecordNo = 0;
        setPlace("");
        resetDirty();
        loadOptions();
    }

//    public void addGedcomCompletion ( Gedcom gedcom) {
//        completionProvider.addGedcomCompletion(gedcom);
//    }
//
//    public void removeGedcomCompletion ( ) {
//        completionProvider.removeGedcomCompletion();
//    }

    ///////////////////////////////////////////////////////////////////////////
    // accesseurs aux options
    ///////////////////////////////////////////////////////////////////////////

    public final void loadOptions() {
        copyFreeComment = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("FreeCommentEnabled", "true"));
        duplicateControlEnabled =  Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("DuplicateRecordControlEnabled", "true"));
        valueControlEnabled = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("ValueControlEnabled", "true"));
        boolean completion = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("GedcomCompletionEnabled", "true"));
        if ( completion ) {
            Context context = GedcomDirectory.getInstance().getLastContext();
            if (context != null && context.getGedcom() != null) {
                completionProvider.addGedcomCompletion(context.getGedcom());
            } else {
                // rien a faire
            }            
        } else {
             completionProvider.removeGedcomCompletion();
        }
    }

    public boolean getDuplicateControlEnabled() {
        return duplicateControlEnabled;
    }

    public boolean getCopyFreeCommentEnabled() {
        return copyFreeComment;
    }

    public boolean getNewValueControlEnabled() {
        return valueControlEnabled;
    }

    ///////////////////////////////////////////////////////////////////////////
    // accesseurs numero de page
    ///////////////////////////////////////////////////////////////////////////

    public String getDefaultFreeComment() {
        return freeComment;
    }

    public void setDefaultFreeComment(String text) {
        freeComment = text;
    }

    ///////////////////////////////////////////////////////////////////////////
    // accesseurs aux modeles
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @return the releveBirthModel
     */
    public ModelAbstract getModel( ModelType modelType) {
        switch (modelType) {
            case birth:
                return releveBirthModel;
            case marriage:
                return releveMarriageModel;
            case death:
                return releveDeathModel;
            case misc:
                return releveMiscModel;
            case all:
                return releveAllModel;
            default:
                return null;
        }

    }

    /**
     * @return the releveBirthModel
     */
    public ModelBirth getReleveBirthModel() {
        return releveBirthModel;
    }

    /**
     * @return the releveMarriageModel
     */
    public ModelMarriage getReleveMarriageModel() {
        return releveMarriageModel;
    }

    /**
     * @return the releveDeathModel
     */
    public ModelDeath getReleveDeathModel() {
        return releveDeathModel;
    }

    /**
     * @return the releveMiscModel
     */
    public ModelMisc getReleveMiscModel() {
        return releveMiscModel;
    }

    /**
     * @return the releveMiscModel
     */
    public ModelAll getReleveAllModel() {
        return releveAllModel;
    }

    public CompletionProvider getCompletionProvider() {
        return completionProvider;
    }

    ///////////////////////////////////////////////////////////////////////////
    //gestion du numero de dernier releve
    ///////////////////////////////////////////////////////////////////////////

    /**
     * incremente puis retourne le numero de releve
     * @return
     */
    protected int getNextRecordNo() {
        return ++lastRecordNo;
    }

    /**
     * libere le numero de record si c'est le dernier
     * @param recordNo
     */
    public void setPreviousRecordNo(int recordNo) {
        if ( recordNo == lastRecordNo) {
            lastRecordNo--;
        }
    }

    /**
     * impose le numero de dernier releve s'il est superieur a la valeur actuelle
     * @param recordNo
     */
    void setNextRecordNo(int recordNo) {
        if ( recordNo > lastRecordNo) {
            lastRecordNo = recordNo;
        }
    }

     ///////////////////////////////////////////////////////////////////////////
    //place manager
    ///////////////////////////////////////////////////////////////////////////

    // listeners devant être prevenus du changement de lieu
    private ArrayList<PlaceListener> placeListeners = new ArrayList<PlaceListener>(1);
    private String cityName = "";
    private String cityCode = "";
    private String county = "";
    private String state = "";
    private String country = "";
    
    private boolean placeChanged = false;
    
    /**
     * @param listener
     */
    @Override
    public void addPlaceListener(PlaceListener listener) {
        placeListeners.add(listener);
    }

    /**
     * @param listener
     */
    @Override
    public void removePlaceListener(PlaceListener listener) {
        placeListeners.remove(listener);
    }

    @Override
    public void setPlace(String cityName, String cityCode, String county, String state, String country) {
       
       if (!cityName.equals(this.cityName) || 
               !cityCode.equals(this.cityCode) || 
               !county.equals(this.county) || 
               !state.equals(this.state) || 
               !country.equals(this.country)
          )
       {
           this.cityName = cityName;
           this.cityCode = cityCode;
           this.county = county;
           this.state = state;
           this.country = country;
           
           placeChanged = true;
            // je notifie les listeners
            for(PlaceListener placeListener : placeListeners) {
                placeListener.updatePlace(getPlace());
            }
       }
      
    }

    @Override
    public void setPlace(String value) {
       
        if (!value.equals(getPlace())) {

            String[] juridictions = value.split(",");
            if (juridictions.length > 0) {
                cityName = juridictions[0];
            } else {
                cityName = "";
            }
            if (juridictions.length > 1) {
                cityCode = juridictions[1];
            } else {
                cityCode = "";
            }
            if (juridictions.length > 2) {
                county = juridictions[2];
            } else {
                county = "";
            }
            if (juridictions.length > 3) {
                state = juridictions[3];
            } else {
                state = "";
            }
            if (juridictions.length > 4) {
                country = juridictions[4];
            } else {
                country = "";
            }

            placeChanged = true;

            // je notifie les listeners
            for (PlaceListener placeListener : placeListeners) {
                placeListener.updatePlace(getPlace());
            }
        }
    }

    /**
     * retourne le lieu au format "cityName,cityCode,countyName,stateName,countryName,"
     * @return
     */
    @Override
    public String getPlace() {
        return getCityName()+ ","+getCityCode()+ ","+getCountyName()+ ","+getStateName()+ ","+getCountryName()+",";
    }

    @Override
    public String getCityName() {
        return cityName;
    }

    @Override
    public String getCityCode() {
        return cityCode;
    }

    @Override
    public String getCountyName() {
        return county;
    }

    @Override
    public String getStateName() {
        return state;
    }

    @Override
    public String getCountryName() {
        return country;
    }
}
