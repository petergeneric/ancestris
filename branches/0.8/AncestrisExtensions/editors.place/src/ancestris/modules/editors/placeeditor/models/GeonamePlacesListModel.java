package ancestris.modules.editors.placeeditor.models;

import ancestris.api.place.Place;
import java.util.*;
import javax.swing.AbstractListModel;

/**
 *
 * @author dominique
 */
public class GeonamePlacesListModel extends AbstractListModel<String> implements List<Place> {

    List<Place> placesList = Collections.synchronizedList(new ArrayList<Place>());

    public GeonamePlacesListModel() {
    }

    /*
     * AbstractListModel implementation
     */
    @Override
    public String getElementAt(int index) {
        String sJurisdictions = "";
        String[] aJurisdictions = placesList.get(index).getJurisdictions();
        for (int index1 = 0; index1 < aJurisdictions.length; index1++) {
            sJurisdictions += index1 == 0 ? (aJurisdictions[index1] != null ? aJurisdictions[index1] : "") : ", " + (aJurisdictions[index1] != null ? aJurisdictions[index1] : "");
        }
        sJurisdictions += ", " + placesList.get(index).getLatitude().toString();
        sJurisdictions += ", " + placesList.get(index).getLongitude().toString();
        return sJurisdictions;
    }

    @Override
    public int getSize() {
        return placesList.size();
    }

    /*
     * List implementation
     */
    @Override
    public void clear() {
        int oldSize = placesList.size();
        placesList.clear();
        fireIntervalRemoved(placesList, 0, oldSize);
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
        int indexOf = placesList.indexOf(o);
        boolean remove = placesList.remove(o);
        fireIntervalRemoved(placesList, indexOf, indexOf);
        return remove;
    }

    @Override
    public boolean containsAll(Collection<?> clctn) {
        return placesList.containsAll(clctn);
    }

    @Override
    public boolean addAll(Collection<? extends Place> clctn) {
        int oldSize = placesList.size();
        boolean addAll = placesList.addAll(clctn);
        int newSize = placesList.size();
        fireIntervalAdded(placesList, oldSize, newSize);
        return addAll;
    }

    @Override
    public boolean addAll(int i, Collection<? extends Place> clctn) {
        int oldSize = placesList.size();
        boolean addAll = placesList.addAll(i, clctn);
        int newSize = placesList.size();
        fireIntervalAdded(placesList, oldSize, newSize);
        return addAll;
    }

    @Override
    public boolean removeAll(Collection<?> clctn) {
        int oldSize = placesList.size();
        boolean removeAll = placesList.removeAll(clctn);
        fireIntervalRemoved(placesList, 0, oldSize);
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
        Place set = placesList.set(i, e);
        fireIntervalAdded(placesList, i, i);
        return set;
    }

    @Override
    public boolean add(Place place) {
        int oldSize = placesList.size();
        boolean add = placesList.add(place);
        fireIntervalAdded(placesList, oldSize, oldSize + 1);
        return add;
    }

    @Override
    public void add(int i, Place e) {
        int oldSize = placesList.size();
        placesList.add(i, e);
        fireIntervalAdded(placesList, oldSize, oldSize + 1);
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
