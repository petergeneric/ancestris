/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.geo;

import genj.gedcom.PropertyPlace;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
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

    private GeoPlacesList owner;
    private List<PropertyPlace> placesProps;
    private GeoNodeObject[] result;
    private final static RequestProcessor RP = new RequestProcessor("interruptible tasks", 1, true);
    private RequestProcessor.Task theTask = null;
    private static boolean isBusy = false;

    public GeoInternetSearch(GeoPlacesList owner, List<PropertyPlace> placesProps) {
        this.owner = owner;
        this.placesProps = placesProps;
    }

    @SuppressWarnings("unchecked")
    public void executeSearch() {
        if (isBusy) {
            return;
        }
        isBusy = true;
        final SortedSet<GeoNodeObject> ret = new TreeSet<GeoNodeObject>(sortPlaces);
        final ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(GeoInternetSearch.class, "TXT_SearchPlaces"), new Cancellable() {

            public boolean cancel() {
                return handleCancel();
            }
        });

        Runnable runnable = new Runnable() {

            private final int NUM = placesProps.size();

            public void run() {
                try {
                    StatusDisplayer.getDefault().setStatusText("");
                    SortedMap<String, GeoNodeObject> listOfCities = new TreeMap<String, GeoNodeObject>();
                    ph.start(); //we must start the PH before we switch to determinate
                    ph.switchToDeterminate(NUM);
                    int i = 0;
                    boolean localOnly = false;
                    for (Iterator<PropertyPlace> it = placesProps.iterator(); it.hasNext();) {
                        i++;
                        PropertyPlace propertyPlace = it.next();
                        String str = propertyPlace.toString().trim();
                        GeoNodeObject obj = listOfCities.get(str);
                        // if place not in the list, add it (it will search for it at construction)
                        if (obj == null) {
                            GeoNodeObject newObj = new GeoNodeObject(propertyPlace, localOnly);
                            if (newObj.getToponym() == null && newObj.isInError) {
                                localOnly = true;
                                WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

                                    public void run() {
                                        String msg = NbBundle.getMessage(GeoInternetSearch.class, "ERROR_cannotFindPlace");
                                        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg, NbBundle.getMessage(GeoInternetSearch.class, "ERROR_Title"), JOptionPane.ERROR_MESSAGE);
                                        StatusDisplayer.getDefault().setStatusText(msg, StatusDisplayer.IMPORTANCE_ANNOTATION * 10);
                                        System.out.println(msg);
                                    }
                                });
                            }
                            ret.add(newObj);
                            listOfCities.put(str, newObj);
                        } else {
                            // else add the event
                            obj.addEvent(propertyPlace.getParent(), propertyPlace);
                        }
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
                isBusy = false;
                ph.finish();
                result = ret.toArray(new GeoNodeObject[ret.size()]);
                owner.setPlaces(result);
                owner.notifyListeners();
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
     * Comparator to sort Lastnames
     */
    public Comparator sortPlaces = new Comparator() {

        public int compare(Object o1, Object o2) {
            GeoNodeObject obj1 = (GeoNodeObject) o1;
            GeoNodeObject obj2 = (GeoNodeObject) o2;
            if (obj1 == null) {
                return +1;
            }
            if (obj2 == null) {
                return -1;
            }
            return obj1.getPlaceAsString().toLowerCase().compareTo(obj2.getPlaceAsString().toLowerCase());
        }
    };
}
