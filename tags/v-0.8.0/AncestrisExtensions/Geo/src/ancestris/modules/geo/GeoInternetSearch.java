/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.geo;

import genj.gedcom.Gedcom;
import genj.gedcom.PropertyPlace;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.JOptionPane;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
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
    private final static RequestProcessor RP = new RequestProcessor("interruptible tasks", 1, true);

    private GeoPlacesList gplOwner;
    private List<PropertyPlace> placesProps;
    private GeoNodeObject[] result;
    private RequestProcessor.Task theTask = null;
    
    public GeoInternetSearch(GeoPlacesList gplOwner, List<PropertyPlace> placesProps) {
        this.gplOwner = gplOwner;
        this.placesProps = placesProps;
    }

    @SuppressWarnings("unchecked")
    public synchronized void executeSearch(Gedcom gedcom) {
        // return if busy with this gedcom already
        if (isBusy) {
            if (gedcomSearchingList.contains(gedcom))  {
                return;
            }
        }
        isBusy = true;
        gedcomSearchingList.add(gedcom);
        
        // Define the key component of data, the nodeobject holding the locations and the events, sorted on string displayed
        final SortedSet<GeoNodeObject> ret = new TreeSet<GeoNodeObject>(sortPlaces);
        
        // the progress bar
        final ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(GeoInternetSearch.class, "TXT_SearchPlaces"), new Cancellable() {

            public boolean cancel() {
                return handleCancel();
            }
        });

        // Define task to be launched
        Runnable runnable = new Runnable() {

            private final int NUM = placesProps.size();

            public synchronized void run() {
                try {
                    StatusDisplayer.getDefault().setStatusText("");
                    SortedMap<String, GeoNodeObject> listOfCities = new TreeMap<String, GeoNodeObject>(); // pointer from propertyplace to objects, to group events by location
                    ph.start(); //we must start the PH before we switch to determinate
                    ph.switchToDeterminate(NUM);
                    int i = 0;
                    for (Iterator<PropertyPlace> it = placesProps.iterator(); it.hasNext();) {
                        i++;
                        PropertyPlace propertyPlace = it.next();
                        String key = gplOwner.getPlaceKey(propertyPlace);
                        GeoNodeObject obj = listOfCities.get(key);
                        // if place not in the list, "create object" or else, add the events
                        if (obj == null) {
                            // City is not in the list : create object, find geocoordinates, add events (all done at construction)
                            GeoNodeObject newObj = new GeoNodeObject(gplOwner, propertyPlace, true);
                            if (newObj.isInError) {
                                WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                                    public void run() {
                                        String msg = NbBundle.getMessage(GeoInternetSearch.class, "ERROR_cannotFindPlace");
                                        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg, NbBundle.getMessage(GeoInternetSearch.class, "ERROR_Title"), JOptionPane.ERROR_MESSAGE);
                                        StatusDisplayer.getDefault().setStatusText(msg, StatusDisplayer.IMPORTANCE_ANNOTATION * 10);
                                        System.out.println(msg);
                                    }
                                });
                            }
                            // Add object to list of objects, and add new city to the list
                            ret.add(newObj);
                            listOfCities.put(key, newObj);
                        } else {
                            // City is already in the list, just add the events
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

        theTask = RP.create(runnable); //the task is not started yet

        theTask.addTaskListener(new TaskListener() {

            public void taskFinished(Task task) {
                ph.finish();
                result = ret.toArray(new GeoNodeObject[ret.size()]);
                gplOwner.setPlaces(result);
                gedcomSearchingList.remove(placesProps.get(0).getGedcom());
                isBusy = false;
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
    public Comparator<GeoNodeObject> sortPlaces = new Comparator<GeoNodeObject>() {

        public int compare(GeoNodeObject o1, GeoNodeObject o2) {
            if (o1 == null) {
                return +1;
            }
            if (o2 == null) {
                return -1;
            }
            Collator myCollator = Collator.getInstance();
            myCollator.setStrength(Collator.PRIMARY);
            myCollator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
            return myCollator.compare(o1.toString(), o2.toString());
        }
    };

}
