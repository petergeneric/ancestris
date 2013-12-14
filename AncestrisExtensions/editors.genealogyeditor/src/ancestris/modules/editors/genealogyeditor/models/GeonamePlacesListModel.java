package ancestris.modules.editors.genealogyeditor.models;

import ancestris.api.place.Place;
import java.util.*;
import javax.swing.AbstractListModel;

/**
 *
 * @author dominique
 */
public class GeonamePlacesListModel extends AbstractListModel<String> implements List<Place> {

    List<Place> placesList = Collections.synchronizedList(new ArrayList<Place>());
    ArrayList<String> placesListData = new ArrayList<String>();

    public GeonamePlacesListModel() {
    }

    public void update(int index0, int index1) {
        for (int index = index0; index < index1; index++) {
            Place place = placesList.get(index);

            String[] splitJurisdictions = place.getJurisdictions();
            String jurisdictions = "";

            // City
            jurisdictions += splitJurisdictions[0] != null ? splitJurisdictions[0] : "";

            //AdminName1
            jurisdictions += splitJurisdictions[1] != null ? ", " + splitJurisdictions[1] : "";
            //AdminCode1
            jurisdictions += splitJurisdictions[2] != null ? " (" + splitJurisdictions[2] + ")" : "";

            //AdminName2
            jurisdictions += splitJurisdictions[3] != null ? ", " + splitJurisdictions[3] : "";
            //AdminCode2
            jurisdictions += splitJurisdictions[4] != null ? " (" + splitJurisdictions[4] + ")" : "";

            //AdminName3
            jurisdictions += splitJurisdictions[5] != null ? ", " + splitJurisdictions[5] : "";
            //AdminCode3
            jurisdictions += splitJurisdictions[6] != null ? " (" + splitJurisdictions[6] + ")" : "";

            //Postal code
            jurisdictions += splitJurisdictions[7] != null ? ", " + splitJurisdictions[7] : "";

            //Country code
            jurisdictions += splitJurisdictions[8] != null ? ", " + splitJurisdictions[8] : "";

            placesListData.add(index, jurisdictions);
        }
    }

    /*
     * AbstractListModel implementation
     */
    @Override
    public String getElementAt(int index) {
        String jurisdictions = "";

        for (String jurisdiction : placesList.get(index).getJurisdictions()) {
            jurisdictions += jurisdiction != null ? jurisdiction + ", " : "";
        }

        return jurisdictions;
    }

    @Override
    public int getSize() {
        return placesListData.size();
    }

    /*
     * List implementation
     */
    @Override
    public void clear() {
        int oldSize = placesList.size();
        placesList.clear();
        update(0, oldSize);
        fireIntervalRemoved(placesListData, 0, oldSize);
    }

    @Override
    public int size() {
        return placesList.size();
    }

    public Place getPlaceAt(int index) {
        return placesList.get(index);
    }

    @Override
    public boolean isEmpty() {
        return placesList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return placesList.contains(o);
    }

    @Override
    public Iterator<Place> iterator() {
        return placesList.iterator();
    }

    @Override
    public Object[] toArray() {
        return placesList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return placesList.toArray(ts);
    }

    @Override
    public boolean remove(Object o) {
        return placesList.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> clctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addAll(Collection<? extends Place> clctn) {
        int oldSize = placesList.size();
        boolean addAll = placesList.addAll(clctn);
        int newSize = placesList.size();

        update(oldSize, newSize);
        fireIntervalAdded(placesListData, oldSize, newSize);
        return addAll;
    }

    @Override
    public boolean addAll(int i, Collection<? extends Place> clctn) {
        int oldSize = placesList.size();
        boolean addAll = placesList.addAll(i, clctn);
        int newSize = placesList.size();
        update(oldSize, newSize);
        fireIntervalAdded(placesListData, oldSize, newSize);
        return addAll;
    }

    @Override
    public boolean removeAll(Collection<?> clctn) {
        int oldSize = placesList.size();
        boolean removeAll = placesList.removeAll(clctn);
        update(oldSize, 0);
        return removeAll;
    }

    @Override
    public boolean retainAll(Collection<?> clctn) {
        return placesList.retainAll(clctn);
    }

    @Override
    public Place get(int i) {
        return placesList.get(i);
    }

    @Override
    public Place set(int i, Place e) {
        return placesList.set(i, e);
    }

    @Override
    public boolean add(Place place) {
        int oldSize = placesList.size();
        boolean add = placesList.add(place);
        update(oldSize, oldSize + 1);
        fireIntervalAdded(placesListData, oldSize, oldSize + 1);
        return add;
    }

    @Override
    public void add(int i, Place e) {
        int oldSize = placesList.size();
        placesList.add(i, e);
        update(oldSize, oldSize + 1);
        fireIntervalAdded(placesListData, oldSize, oldSize + 1);
    }

    @Override
    public Place remove(int i) {
        return placesList.remove(i);
    }

    @Override
    public int indexOf(Object o) {
        return placesList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return placesList.lastIndexOf(o);
    }

    @Override
    public ListIterator<Place> listIterator() {
        return placesList.listIterator();
    }

    @Override
    public ListIterator<Place> listIterator(int i) {
        return placesList.listIterator(i);
    }

    @Override
    public List<Place> subList(int i, int i1) {
        return placesList.subList(i1, i1);
    }
}
