package ancestris.modules.releve.model;

import ancestris.explorer.GedcomExplorerTopComponent;
import ancestris.gedcom.GedcomDirectory;
import ancestris.modules.releve.file.FileBuffer;
import genj.gedcom.Context;
import java.util.ArrayList;
import java.util.Collection;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

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

    //options de copie
    boolean copyCoteEnabled = true;
    boolean copyEventDateEnabled = true;
    boolean copyFreeCommentEnabled = true;
    boolean copyNotaryEnabled = true;
    // options de controle
    boolean duplicateControlEnabled = true;
    boolean valueControlEnabled = true;
    
    // données volatiles
    private int lastRecordNo = 0;
    private String defaultCote = "";
    private String defaultEventDate = "";
    private String defaultFreeComment = "";
    private String defaultNotary = "";

    // previous record
    public enum RecordType { birth, marriage, death, misc }
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

        if (getCopyCoteEnabled() ) {
            record.setCote(defaultCote);
        }

        if (getCopyEventDateEnabled() ) {
            record.setEventDate(defaultEventDate);
        }

        if (getCopyFreeCommentEnabled() ) {
            record.setFreeComment(defaultFreeComment);
        }

        if (getCopyNotaryEnabled() && record instanceof RecordMisc ) {
            record.setNotary(defaultNotary);
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
            setPlace(defaultPlace);
        }

        // je recupere le nombre de lignes
        int firstRowBirth = releveBirthModel.getRowCount();
        int firstRowMarriage = releveMarriageModel.getRowCount();
        int firstRowDeath = releveDeathModel.getRowCount();
        int firstRowMisc = releveMiscModel.getRowCount();
        int firstRowAll = releveAllModel.getRowCount();

        // j'ajoute les releves
        for (Record record : fileBuffer.getRecords()) {
            this.addRecord(record, false);
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
        copyCoteEnabled = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("CopyCoteEnabled", "true"));
        copyEventDateEnabled = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("CopyEventDateEnabled", "true"));
        copyFreeCommentEnabled = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("CopyFreeCommentEnabled", "true"));
        copyNotaryEnabled = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("CopyNotaryEnabled", "true"));
        duplicateControlEnabled =  Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("DuplicateRecordControlEnabled", "true"));
        valueControlEnabled = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("ValueControlEnabled", "true"));
        boolean completion = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("GedcomCompletionEnabled", "true"));
        if ( completion ) {
            //Context context = Utilities.actionsGlobalContext().lookup(Context.class);
            //Context context = App.center.getSelectedContext(true);
            Context context = getSelectedContext(true);
            if (context != null && context.getGedcom() != null) {
                completionProvider.addGedcomCompletion(context.getGedcom());
            } else {
                // rien a faire
            }            
        } else {
             completionProvider.removeGedcomCompletion();
        }
    }

     //XXX: GedcomExplorer must be actionGlobalContext provider: to be rewritten
    private Context getSelectedContext(boolean firstIfNoneSelected){
        Collection<? extends Context> selected = Utilities.actionsGlobalContext().lookupAll(Context.class);

        Context c;
        if (selected.isEmpty())
            c = GedcomExplorerTopComponent.getDefault().getContext();
        else {
            c = Utilities.actionsGlobalContext().lookup(Context.class);
        }
        if (!firstIfNoneSelected)
            return c;
        if (c!=null)
            return c;
        return GedcomDirectory.getInstance().getContext(0);
    }

    public boolean getDuplicateControlEnabled() {
        return duplicateControlEnabled;
    }

    public boolean getCopyCoteEnabled() {
        return copyCoteEnabled;
    }

    public boolean getCopyEventDateEnabled() {
        return copyEventDateEnabled;
    }

    public boolean getCopyFreeCommentEnabled() {
        return copyFreeCommentEnabled;
    }

    public boolean getCopyNotaryEnabled() {
        return copyNotaryEnabled;
    }


    public boolean getNewValueControlEnabled() {
        return valueControlEnabled;
    }

    ///////////////////////////////////////////////////////////////////////////
    // accesseurs numero de page
    ///////////////////////////////////////////////////////////////////////////
    
//    public String getDefaultCote() {
//        return defaultCote;
//    }
//
//    public String getDefaultEventDateComment() {
//        return defaultEventDate;
//    }
//
//    public String getDefaultFreeComment() {
//        return defaultFreeComment;
//    }
//
//    public String getDefaultNotary() {
//        return defaultNotary;
//    }

    public void setDefaultEventDate(String text) {
        defaultEventDate = text;
    }

    public void setDefaultFreeComment(String text) {
        defaultFreeComment = text;
    }

    public void setDefaultCote(String text) {
        defaultCote = text;
    }

    public void setDefaultNotary(String text) {
        defaultNotary = text;
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
    private FieldPlace recordsInfoPlace = new FieldPlace();
    private String sourceTitle = "";
    
    
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
       
       if (!cityName.equals(this.recordsInfoPlace.getCityName()) ||
               !cityCode.equals(this.recordsInfoPlace.getCityCode()) ||
               !county.equals(this.recordsInfoPlace.getCountyName()) ||
               !state.equals(this.recordsInfoPlace.getStateName()) ||
               !country.equals(this.recordsInfoPlace.getCountryName())
          )
       {
           String oldValue = recordsInfoPlace.getValue();
           this.recordsInfoPlace.setCityName(cityName);
           this.recordsInfoPlace.setCityCode(cityCode);
           this.recordsInfoPlace.setCountyName(county);
           this.recordsInfoPlace.setStateName(state);
           this.recordsInfoPlace.setCountryName(country);
           completionProvider.updatePlaces(recordsInfoPlace, oldValue);
           
           placeChanged = true;
            // je notifie les listeners
            for(PlaceListener placeListener : placeListeners) {
                placeListener.updatePlace(getPlace());
            }
       }      
    }

    @Override
    public void setPlace(String value) {
        String oldValue = recordsInfoPlace.getValue();
        recordsInfoPlace.setValue(value);
        placeChanged = true;
        completionProvider.updatePlaces(recordsInfoPlace, oldValue);

        // je notifie les listeners
        for (PlaceListener placeListener : placeListeners) {
            placeListener.updatePlace(getPlace());
        }
    }

    /**
     * retourne le lieu au format "cityName,cityCode,countyName,stateName,countryName,"
     * @return
     */
    @Override
    public String getPlace() {
        return recordsInfoPlace.getValue();
    }

    @Override
    public String getCityName() {
        return recordsInfoPlace.getCityName();
    }

    @Override
    public String getCityCode() {
        return recordsInfoPlace.getCityCode();
    }

    @Override
    public String getCountyName() {
        return recordsInfoPlace.getCountyName();
    }

    @Override
    public String getStateName() {
        return recordsInfoPlace.getStateName();
    }

    @Override
    public String getCountryName() {
        return recordsInfoPlace.getCountryName();
    }

    /**
     * @return the sourceTitle
     */
    @Override
    public String getSourceTitle() {
        return sourceTitle;
    }

    /**
     * @param sourceTitle the sourceTitle to set
     */
    public void setSourceTitle(String sourceTitle) {
        this.sourceTitle = sourceTitle;
    }



}
