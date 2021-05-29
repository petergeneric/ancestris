package ancestris.modules.releve.model;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.GedcomDirectory;
import ancestris.gedcom.GedcomFileListener;
import ancestris.modules.releve.file.FileBuffer;
import ancestris.modules.releve.model.Record.RecordType;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Michel
 */
@ServiceProvider(service = ancestris.core.pluginservice.PluginInterface.class)
public class DataManager extends AncestrisPlugin implements PlaceManager, GedcomFileListener  {

    private final RecordModel   dataModel = new RecordModel();
    private final CompletionProvider completionProvider  = new CompletionProvider();
    private final GedcomLinkProvider gedcomLinkProvider = new GedcomLinkProvider();    
    Gedcom completionGedcom = null;
    File currentFile;
    private final int[] previousRecordIndex = new int[2];
    
    // options de controle
    static boolean duplicateControlEnabled = true;
    static boolean valueControlEnabled = true;
    //options de copie
    static boolean copyParishEnabled = true;
    static boolean copyCoteEnabled = true;
    static boolean copyEventDateEnabled = true;
    static boolean copySecondDateEnabled = true;
    static boolean copyFreeCommentEnabled = true;
    static boolean copyNotaryEnabled = true;
    // options de completion avec les données du fichier gedcom
    static boolean gedcomCompletion = true;
    private boolean gedcomLinkState = false;
        
    public enum ModelType { MODEL_BIRTH, MODEL_MARRIAGE, DEATH, MISC, ALL }

    static {
        loadOptions();
    }
    
    public DataManager () {
        setGedcomCompletion(gedcomCompletion);
    }

    /**
     * indique si les données ont été modifiées depuis la dernière sauvegarde
     * @return
     */
    public boolean isDirty() {
        boolean result = false;
        result |= dataModel.isDirty();
        result |= placeChanged;
        return result;
    }

     /**
     * reinitialise l'indicateur des modifications
     * @return
     */
    public void resetDirty() {
        dataModel.resetDirty();
        placeChanged = false;
    }

    public Record createRecord(RecordType recordType) {
        Record record;
        switch (recordType) {
            case BIRTH:
                record = new RecordBirth();
                break;
            case DEATH:
                record = new RecordDeath();
                break;
            case MARRIAGE:
                record = new RecordMarriage();
                break;
            case MISC:
            default:
                record = new RecordMisc();
                break;
        }
       
        return record;
    }

    /**
     * ajoute un nouveau releve
     * le numero de releve est calculé automatiquement
     * @param newRecord
     * @return
     */
    public int addRecord(Record newRecord) {
        int recordIndex = dataModel.addRecord(newRecord);
        completionProvider.addRecord(newRecord);
        gedcomLinkProvider.addRecord(newRecord);
        previousRecordIndex[1] = previousRecordIndex[0];
        previousRecordIndex[0] = recordIndex;
        return recordIndex;
    }
   

    /**
     * ajouter les releves 
     * @param fileBuffer  buffer contenant les releves a ajouter
     * @param append  si false , vide les modeles avant d'ajouter les nouveaux relevés
     * @param defaultPlace lieu par defaut des releves
     * @param forceDefaultPlace 1=remplace les lieux des releves par le lieu par défaut, 0=n'ajoute que les releves dont le lieu est le lieu par defaut
     */
    public void addRecords(FileBuffer fileBuffer, boolean append) {
        if (!append && dataModel.getRowCount()!=0) {
            removeAll();
        }
        
        dataModel.addRecords(fileBuffer.getRecords());

        for (Record record : fileBuffer.getRecords()) {
            completionProvider.addRecord(record);
            gedcomLinkProvider.addRecord(record);
        }
        
        
        // RAZ de l'etat du modele
        if (!append) {
            resetDirty();
        }
        
        previousRecordIndex[1] = dataModel.getRowCount() -1;
        previousRecordIndex[0] = dataModel.getRowCount() -1;
    }

    /**
     * insère un nouveau relevé avant le relevé de référence
     * @param newRecord
     * @return
     */
    public void insertRecord(RecordType recordType, int index) {
        Record newRecord = createRecord(recordType);
        dataModel.insertRecord(newRecord,index);
        completionProvider.addRecord(newRecord);
        gedcomLinkProvider.addRecord(newRecord);
        
        previousRecordIndex[1] = previousRecordIndex[0];
        previousRecordIndex[0] = index;
    }

    public void removeRecord(Record record) {
        int recordIndex = dataModel.getIndex(record);

        gedcomLinkProvider.removeRecord(record);
        completionProvider.removeRecord(record);
        dataModel.removeRecord(record);

        if( recordIndex == previousRecordIndex[1]) {
            previousRecordIndex[1] = dataModel.getRowCount() -1;
        }
        if( recordIndex == previousRecordIndex[0]) {
            previousRecordIndex[0] = dataModel.getRowCount() -1;
        }
        
    }

     public void swapRecordNext(Record record) {
        dataModel.swapRecordNext(record);
    }

    public void swapRecordPrevious(Record record) {
        dataModel.swapRecordPrevious(record);
    }
    
    public void renumberRecords(Record record, int[] tableIndexList) {
        dataModel.renumberRecords(record, tableIndexList);
    }

    /**
     * supprime tout sauf les données de complétion du gedcom
     */
    public void removeAll() {
        getCompletionProvider().removeAll();
        dataModel.removeAll();
        gedcomLinkProvider.removeAll();
        
        // je restaure les donnees de completion du gedcom
        completionProvider.addGedcomCompletion(completionGedcom);
        //setPlace("","","","","");
      
        resetDirty();
    }

    public Record getRecord( int recordIndex ) {
        return dataModel.getRecord(recordIndex);
    }
    
    
    public int  getPreviousRecordIndex () {
        return previousRecordIndex[1];
    }

//    public void setPreviousRecordIndex (int index) {
//        previousRecordIndex[1] =  index;
//    }

    /*
     * verifie que les champs obligatoires sont renseignés
     * @return retourne un message d'eeur ou une chaine vide s'il n'y a pas d'erreur 
     */
    public String verifyRecord( Record record  ) {
        return dataModel.verifyRecord(record);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // accesseurs aux options
    ///////////////////////////////////////////////////////////////////////////

    static void loadOptions() {
        // options de copie des données dans les nouveaux releves
        copyCoteEnabled = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("CopyCoteEnabled", "true"));
        copyEventDateEnabled = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("CopyEventDateEnabled", "true"));
        copySecondDateEnabled = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("CopySecondDateEnabled", "true"));
        copyFreeCommentEnabled = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("CopyFreeCommentEnabled", "true"));
        copyNotaryEnabled = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("CopyNotaryEnabled", "true"));
        copyParishEnabled = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("CopyParishEnabled", "true"));
        //options de controle
        duplicateControlEnabled =  Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("DuplicateRecordControlEnabled", "true"));
        valueControlEnabled = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("ValueControlEnabled", "true"));
        // completion avec un Gedcom
        gedcomCompletion = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("GedcomCompletionEnabled", "true"));
    }
 
    static public boolean getDuplicateControlEnabled() {
        return duplicateControlEnabled;
    }

    static public boolean getCopyCoteEnabled() {
        return copyCoteEnabled;
    }

    static public boolean getCopyEventDateEnabled() {
        return copyEventDateEnabled;
    }

    static public boolean getCopySecondDateEnabled() {
        return copySecondDateEnabled;
    }

    static public boolean getCopyFreeCommentEnabled() {
        return copyFreeCommentEnabled;
    }

    static public boolean getCopyNotaryEnabled() {
        return copyNotaryEnabled;
    }

    static public boolean getCopyParishEnabled() {
        return copyParishEnabled;
    }

    static public boolean getNewValueControlEnabled() {
        return valueControlEnabled;
    }

    static public boolean getGecomCompletionEnabled() {
        return gedcomCompletion;
    }

    
    //options de controle
    static public void setDuplicateControlEnabled(boolean enabled) {
        NbPreferences.forModule(DataManager.class).put("DuplicateRecordControlEnabled", String.valueOf(enabled));
        duplicateControlEnabled = enabled;
    }
    static public void setNewValueControlEnabled(boolean enabled) {
        NbPreferences.forModule(DataManager.class).put("ValueControlEnabled", String.valueOf(enabled));
        valueControlEnabled= enabled;
    }
    
    // completion avec un Gedcom
    static public void setGedcomCompletionEnabled(boolean enabled) {
        NbPreferences.forModule(DataManager.class).put("GedcomCompletionEnabled", String.valueOf(enabled));
        gedcomCompletion= enabled;
    }

    // copie des champs du releve precedent 
    static public void setCopyCoteEnabled(boolean enabled) {
        NbPreferences.forModule(DataManager.class).put("CopyCoteEnabled", String.valueOf(enabled));
        copyCoteEnabled= enabled;
    }

    static public void setCopyEventDateEnabled(boolean enabled) {
        NbPreferences.forModule(DataManager.class).put("CopyEventDateEnabled", String.valueOf(enabled));
        copyEventDateEnabled= enabled;
    }

    static public void setCopySecondDateEnabled(boolean enabled) {
        NbPreferences.forModule(DataManager.class).put("CopySecondDateEnabled", String.valueOf(enabled));
        copySecondDateEnabled= enabled;
    }

    static public void setCopyFreeCommentEnabled(boolean enabled) {
        NbPreferences.forModule(DataManager.class).put("CopyFreeCommentEnabled", String.valueOf(enabled));
        copyFreeCommentEnabled= enabled;
    }

    static public void setCopyNotaryEnabled(boolean enabled) {
        NbPreferences.forModule(DataManager.class).put("CopyNotaryEnabled", String.valueOf(enabled));
        copyNotaryEnabled= enabled;
    }

    static public void setCopyParishEnabled(boolean enabled) {
        NbPreferences.forModule(DataManager.class).put("CopyParishEnabled", String.valueOf(enabled));
        copyParishEnabled= enabled;
    }

   ///////////////////////////////////////////////////////////////////////////
    // accesseurs aux modeles
    ///////////////////////////////////////////////////////////////////////////

    public RecordModel getDataModel() {
        return dataModel;
    }

    public CompletionProvider getCompletionProvider() {
        return completionProvider;
    }

    ///////////////////////////////////////////////////////////////////////////
    //place manager
    ///////////////////////////////////////////////////////////////////////////

    // listeners devant être prevenus du changement de lieu
    private final ArrayList<PlaceListener> placeListeners = new ArrayList<PlaceListener>(1);
    private final RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
    
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
       
       if (    !cityName.equals(this.recordsInfoPlace.getCityName()) ||
               !cityCode.equals(this.recordsInfoPlace.getCityCode()) ||
               !county.equals(this.recordsInfoPlace.getCountyName()) ||
               !state.equals(this.recordsInfoPlace.getStateName()) ||
               !country.equals(this.recordsInfoPlace.getCountryName())
          )
       {
            String oldValue = recordsInfoPlace.getValue();
            this.recordsInfoPlace.setValue(cityName, cityCode,county, state, country);
            //TODO
            completionProvider.updatePlaces(recordsInfoPlace.getValue(), oldValue);
           
           placeChanged = true;
            // je notifie les listeners
            for(PlaceListener placeListener : placeListeners) {
                placeListener.updatePlace(getPlace().getValue());
            }
       }      
    }

    
    @Override
    public void setPlace(String value) {
        String oldValue = recordsInfoPlace.getValue();
        recordsInfoPlace.setValue(value);
        placeChanged = true;
        // TODO
        //completionProvider.updatePlaces(recordsInfoPlace, oldValue);

        // je notifie les listeners
        for (PlaceListener placeListener : placeListeners) {
            placeListener.updatePlace(recordsInfoPlace.getValue());
        }
    }
    
    
    public void refreshPlaceListeners () {
        for (PlaceListener placeListener : placeListeners) {
            placeListener.updatePlace(recordsInfoPlace.getValue());
        }
    }
 

    /**
     * retourne le lieu au format "cityName,cityCode,countyName,stateName,countryName,"
     * @return
     */
    public RecordInfoPlace getPlace() {
        return recordsInfoPlace;
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

    public File getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(File currentFile) {
        this.currentFile =  currentFile;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Implement GedcomFileListener
    ///////////////////////////////////////////////////////////////////////////

    /**
     * desactive la completion avec gedcom si le fichier gedcom utilisé
     * est fermé par l'utilisateur
     * @param gedcom
     */
    @Override
    public void gedcomClosed(Gedcom gedcom) {
        if ( completionGedcom != null && completionGedcom.equals(gedcom) ){
            removeGedcomCompletion();
        }
    }

    @Override
    public void commitRequested(Context context) {
        //rien à faire
    }

    @Override
    public void gedcomOpened(Gedcom gedcom) {
        if (gedcomCompletion) {
            addGedcomCompletion(gedcom);
        }
    }

    public final void setGedcomCompletion(boolean completion) {
        if (completionGedcom == null && completion) {
            Context context = getSelectedContext();
            if (context != null && context.getGedcom() != null) {
                addGedcomCompletion(context.getGedcom());
            } else {
                // rien a faire
            }
        } else if (completionGedcom != null && !completion) {
            removeGedcomCompletion();
        }
    }
    
    //XXX: GedcomExplorer must be actionGlobalContext provider: to be rewritten
    private Context getSelectedContext(){

        // je cherche le gedcom selectionné dans GedcomExplorerTopComponent
        // TODO remplacer GedcomExplorerTopComponent.findInstance().getContext();
        //Context context = GedcomExplorerTopComponent.findInstance().getContext();
        Context context = null;
        if (context!=null) {
            return context;
        } else {
            // aucun contexte n'est sélectionné, alors je choisi le premier gedcom ouvert
            List<Context> contextList =  GedcomDirectory.getDefault().getContexts();
            if ( contextList != null && contextList.size() >0) {
                return contextList.get(0);
            } else {
                return null; 
            }
        }
    }

    void addGedcomCompletion(Gedcom gedcom) {
        completionGedcom = gedcom;
        completionProvider.addGedcomCompletion(gedcom);
        showGedcomLink(gedcomLinkState, true);        
    }

    void removeGedcomCompletion() {
        // je reinitialise la completion
        completionProvider.removeAll();
        // j'ajoute les releves
        for (Record record : dataModel.releveList) {
            completionProvider.addRecord(record);
        }
        completionGedcom = null;
        showGedcomLink(gedcomLinkState, true);        
    }

    
    ///////////////////////////////////////////////////////////////////////////
    // accesseurs gedcomToLink
    ///////////////////////////////////////////////////////////////////////////

    public void showGedcomLink(boolean state, boolean quiet) {
        gedcomLinkProvider.init(dataModel, completionGedcom, state, quiet);
        gedcomLinkState = state;
    }
    
    public GedcomLink getGedcomLink(Record record) {
        return gedcomLinkProvider.getGedcomLink(record);
    }
    
}
