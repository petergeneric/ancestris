package ancestris.modules.releve.model;

import ancestris.explorer.GedcomExplorerTopComponent;
import ancestris.gedcom.GedcomDirectory;
import ancestris.modules.releve.file.FileBuffer;
import ancestris.gedcom.GedcomFileListener;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.time.Calendar;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 *
 * @author Michel
 */
public class DataManager implements PlaceManager, GedcomFileListener  {

    private final RecordModel   dataModel = new RecordModel();
    private final CompletionProvider completionProvider  = new CompletionProvider();
    private final GedcomLinkProvider gedcomLinkProvider = new GedcomLinkProvider();    
    Gedcom completionGedcom;
    File currentFile;
    
    //options de copie
    boolean copyCoteEnabled = true;
    boolean copyEventDateEnabled = true;
    boolean copyFreeCommentEnabled = true;
    boolean copyNotaryEnabled = true;
    // options de controle
    boolean duplicateControlEnabled = true;
    boolean valueControlEnabled = true;
    
    // données de la session
    private String defaultCote = "";
    private String defaultEventDate = "";
    private Calendar defaultEventCalendar = null;
    private String defaultFreeComment = "";
    private String defaultNotary = "";

    // previous record
    public enum RecordType { birth, marriage, death, misc }
    public enum ModelType { birth, marriage, death, misc, all }

    public DataManager () {
        initOptions();
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

    public Record createRecord(DataManager.RecordType recordType) {
        Record record;
        switch (recordType) {
            case birth:
                record = new RecordBirth();
                break;
            case death:
                record = new RecordDeath();
                break;
            case marriage:
                record = new RecordMarriage();
                break;
            case misc:
            default:
                record = new RecordMisc();
                break;
        }

        if (getCopyCoteEnabled() ) {
            record.setCote(defaultCote);
        }

        if (getCopyEventDateEnabled() ) {
            record.setEventDate(defaultEventDate);
            record.setEventCalendar(defaultEventCalendar);
        }

        if (getCopyFreeCommentEnabled() ) {
            record.setFreeComment(defaultFreeComment);
        }

        if (getCopyNotaryEnabled() && record instanceof RecordMisc ) {
            record.setNotary(defaultNotary);
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
        if (!append && dataModel.getRowCount()!=0) {
            removeAll();
            setPlace(defaultPlace);
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
    }

    /**
     * insère un nouveau relevé avant le relevé de référence
     * @param newRecord
     * @return
     */
    public void insertRecord(DataManager.RecordType recordType, int index) {
        Record newRecord = createRecord(recordType);
        dataModel.insertRecord(newRecord,index);
        completionProvider.addRecord(newRecord);
        gedcomLinkProvider.addRecord(newRecord);
    }

    public void removeRecord(Record record) {
        gedcomLinkProvider.addRecord(record);
        completionProvider.removeRecord(record);
        dataModel.removeRecord(record);
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
        setPlace("");
        
        // raz des données de la session
        defaultCote = "";
        defaultEventDate = "";
        defaultEventCalendar = null;
        defaultFreeComment = "";
        defaultNotary = "";
        
        resetDirty();
    }

    public Record getRecord( int recordIndex ) {
        return dataModel.getRecord(recordIndex);
    }

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

    public final void initOptions() {
        copyCoteEnabled = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("CopyCoteEnabled", "true"));
        copyEventDateEnabled = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("CopyEventDateEnabled", "true"));
        copyFreeCommentEnabled = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("CopyFreeCommentEnabled", "true"));
        copyNotaryEnabled = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("CopyNotaryEnabled", "true"));
        duplicateControlEnabled =  Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("DuplicateRecordControlEnabled", "true"));
        valueControlEnabled = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("ValueControlEnabled", "true"));
        boolean completion = Boolean.parseBoolean(NbPreferences.forModule(DataManager.class).get("GedcomCompletionEnabled", "true"));
        
        setGedcomCompletion(completion);
        
//        if ( completion ) {
//            //Context context = Utilities.actionsGlobalContext().lookup(Context.class);
//            //Context context = App.center.getSelectedContext(true);
//            Context context = getSelectedContext(true);
//            if (context != null && context.getGedcom() != null) {
//                completionProvider.addGedcomCompletion(context.getGedcom());
//            } else {
//                // rien a faire
//            }            
//        } 
    }

    public void updateOptions( boolean copyCoteEnabled,  boolean copyEventDateEnabled,
            boolean copyFreeCommentEnabled, boolean copyNotaryEnabled,
            boolean duplicateControlEnabled, boolean valueControlEnabled
            ) {
        this.copyCoteEnabled = copyCoteEnabled;
        this.copyEventDateEnabled = copyEventDateEnabled;
        this.copyFreeCommentEnabled = copyFreeCommentEnabled;
        this.copyNotaryEnabled = copyNotaryEnabled;
        this.duplicateControlEnabled =  duplicateControlEnabled;
        this.valueControlEnabled = valueControlEnabled;        
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
        List<Context> contextList =  GedcomDirectory.getDefault().getContexts();
        if ( contextList != null && contextList.size() >0) {
            return contextList.get(0);
        } else {
            return null; 
        }
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
    // accesseurs aux données par defaut de la session
    ///////////////////////////////////////////////////////////////////////////

    public void setDefaultEventDate(String text, Calendar calendar) {
        defaultEventDate = text;
        defaultEventCalendar = calendar;
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
    private final FieldPlace recordsInfoPlace = new FieldPlace();
    
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
        // rien à faire
    }

    public void setGedcomCompletion(boolean completion) {
        if (completionGedcom == null && completion) {
            Context context = getSelectedContext(true);
            if (context != null && context.getGedcom() != null) {
                addGedcomCompletion(context.getGedcom());
            } else {
                // rien a faire
            }
        } else if (completionGedcom != null && !completion) {
            removeGedcomCompletion();
        }
    }

    void addGedcomCompletion(Gedcom gedcom) {
        completionGedcom = gedcom;
        completionProvider.addGedcomCompletion(gedcom);        
    }

    void removeGedcomCompletion() {
        // je reinitialise la completion
        completionProvider.removeAll();
        // j'ajoute les releves
        for (Record record : dataModel.releveList) {
            completionProvider.addRecord(record);
        }
        completionGedcom = null;
    }

    
    ///////////////////////////////////////////////////////////////////////////
    // accesseurs gedcomToLink
    ///////////////////////////////////////////////////////////////////////////

    public void showGedcomLink(boolean state) {
        gedcomLinkProvider.init(dataModel, completionGedcom, state);        
    }
    
    public GedcomLink getGedcomLink(Record record) {
        return gedcomLinkProvider.getgedcomLink(record);
    }
    
}
