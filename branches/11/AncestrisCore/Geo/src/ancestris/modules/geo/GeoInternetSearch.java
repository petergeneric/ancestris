/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.geo;

import ancestris.modules.place.geonames.GeonamesResearcher;
import genj.gedcom.Gedcom;
import genj.gedcom.PropertyPlace;
import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.JOptionPane;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.windows.WindowManager;

/**
 * Builds the geo objects, one object per "place string"
 *
 * @author frederic
 */
class GeoInternetSearch {

    private static boolean isBusy = false;
    private static Set<Gedcom> gedcomSearchingList = new HashSet<Gedcom>();
    private static RequestProcessor RP = null;

    private GeoPlacesList gplOwner;
    private List<PropertyPlace> placesProps;
    private GeoNodeObject[] result;
    private RequestProcessor.Task theTask = null;
    
    private GeonamesResearcher geonamesResearcher = null;
    
    
    public GeoInternetSearch(GeoPlacesList gplOwner, List<PropertyPlace> placesProps) {
        this.gplOwner = gplOwner;
        this.placesProps = placesProps;
        this.geonamesResearcher = new GeonamesResearcher();
    }

    public synchronized void executeSearch(Gedcom gedcom, final int internetSearchType) {

        // return if busy with this gedcom already
        if (isBusy) {
            if (gedcomSearchingList.contains(gedcom))  {
                return;
            }
        }
        isBusy = true;
        gedcomSearchingList.add(gedcom);
        
        // Define the key component of data, the nodeobject holding the locations and the events, sorted on key string displayed
        final SortedMap<String, GeoNodeObject> listOfCities = new TreeMap<>(sortString); // pointer from propertyplace to objects, to group events by location
        
        // the progress bar
        String paramMsg = NbBundle.getMessage(GeoInternetSearch.class, internetSearchType == GeoNodeObject.GEO_SEARCH_WEB_ONLY ? "TXT_SearchPlacesWeb" : "TXT_SearchPlacesLocal");
        String processMsg = NbBundle.getMessage(GeoInternetSearch.class, "TXT_SearchPlaces", placesProps.size(), paramMsg);
        final ProgressHandle ph = ProgressHandle.createHandle(processMsg, new Cancellable() {
            @Override
            public boolean cancel() {
                return handleCancel();
            }
        });

        // Define task to be launched
        Runnable runnable = new Runnable() {

            private final int NUM = placesProps.size();

            @Override
            public synchronized void run() {
                try {
                    StatusDisplayer.getDefault().setStatusText("");
                    listOfCities.clear();
                    ph.start(); //we must start the PH before we switch to determinate
                    ph.switchToDeterminate(NUM);
                    int i = 0;
                    for (PropertyPlace propertyPlace : placesProps) {
                        i++;
                        String key = gplOwner.getPlaceKey(propertyPlace);
                        GeoNodeObject obj = listOfCities.get(key);
                        // if place not in the list, "create object" or else, add the events
                        if (obj == null) {
                            // City is not in the list : create object, find geocoordinates, add events (all done at construction)
                            GeoNodeObject newObj = new GeoNodeObject(geonamesResearcher, gplOwner, propertyPlace, internetSearchType);
                            if (newObj.isInError) {
                                WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                                    public void run() {
                                        String msg = NbBundle.getMessage(GeoInternetSearch.class, "ERROR_cannotFindPlace");
                                        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg, NbBundle.getMessage(GeoInternetSearch.class, "ERROR_Title"), JOptionPane.ERROR_MESSAGE);
                                        StatusDisplayer.getDefault().setStatusText(msg, StatusDisplayer.IMPORTANCE_ANNOTATION * 10);
                                    }
                                });
                                break;
                            }
                            // Add city to the list
                            listOfCities.put(key, newObj);
                        } else {
                            // City is already in the list, just add the event
                            obj.addEvent(propertyPlace.getParent(), propertyPlace);
                        }
                        // Increment progress bar
                        ph.progress(i);
                        if (Thread.currentThread().isInterrupted()) {
                            throw new InterruptedException("");
                        }
                    }
                } catch (InterruptedException ex) {
                    isBusy = false;
                    String msg = NbBundle.getMessage(GeoInternetSearch.class, "TXT_SearchCancelled");
                    System.out.println(msg);
                    StatusDisplayer.getDefault().setStatusText(msg, StatusDisplayer.IMPORTANCE_ANNOTATION * 10);
                    return;
                }
            }
        };

        if (RP == null) {
            RP = new RequestProcessor("GeoInternetSearch", 1, true);
        }
        theTask = RP.create(runnable); //the task is not started yet

        theTask.addTaskListener(new TaskListener() {

            public void taskFinished(Task task) {
                ph.finish();
                Collection<GeoNodeObject> ret = listOfCities.values();
                result = ret.toArray(new GeoNodeObject[ret.size()]);
                if (result.length > 0) {
                    gplOwner.setPlaces(result);
                    gedcomSearchingList.remove(placesProps.get(0).getGedcom());
                }
                isBusy = false;
                callback();
            }
        });

        theTask.schedule(0); //start the task

            return;

    }

    private boolean handleCancel() {
        if (null == theTask) {
            return false;
        }
        return theTask.cancel();
    }
    
    /**
     * Comparator to sort places
     */
    public Comparator<String> sortString = new Comparator<String>() {

        public int compare(String o1, String o2) {
            if (o1 == null) {
                return +1;
            }
            if (o2 == null) {
                return -1;
            }
            Collator myCollator = Collator.getInstance();
            myCollator.setStrength(Collator.TERTIARY);
            myCollator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
            return myCollator.compare(o1, o2);
        }
    };

    
    public void callback() {
    }
}
