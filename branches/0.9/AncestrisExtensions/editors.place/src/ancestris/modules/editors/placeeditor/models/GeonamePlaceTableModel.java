package ancestris.modules.editors.placeeditor.models;

import ancestris.api.place.Place;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dominique
 */
public class GeonamePlaceTableModel extends AbstractTableModel implements List<Place> {

    String[] placesListColumsTitle = {"City","AdminName1", "AdminCode1", "AdminName2", "AdminCode2", "AdminName3", "AdminCode3", "Postal code", "Country code"};
    List<Place> placesList = Collections.synchronizedList(new ArrayList<Place>());

    public GeonamePlaceTableModel() {
    }

    @Override
    public int getRowCount() {
        return placesList == null?0:placesList.size();
    }

    @Override
    public int getColumnCount() {
        return placesListColumsTitle.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        return placesList.get(row).getJurisdictions()[col];
    }

    @Override
    public String getColumnName(int col) {
        return placesListColumsTitle[col];
    }

    /*
     * List implementation
     */
    @Override
    public void clear() {
        int oldSize = placesList.size();
        placesList.clear();
        this.fireTableRowsDeleted(0, oldSize);
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
        fireTableRowsDeleted(indexOf, indexOf);
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
        fireTableRowsInserted(oldSize, newSize);
        return addAll;
    }

    @Override
    public boolean addAll(int i, Collection<? extends Place> clctn) {
        int oldSize = placesList.size();
        boolean addAll = placesList.addAll(i, clctn);
        int newSize = placesList.size();
        fireTableRowsInserted(oldSize, newSize);
        return addAll;
    }

    @Override
    public boolean removeAll(Collection<?> clctn) {
        int oldSize = placesList.size();
        boolean removeAll = placesList.removeAll(clctn);
        fireTableRowsDeleted(0, oldSize);
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
        fireTableRowsInserted(i, i);
        return set;
    }

    @Override
    public boolean add(Place place) {
        int oldSize = placesList.size();
        boolean add = placesList.add(place);
        fireTableRowsInserted(oldSize, oldSize+1);
        return add;
    }

    @Override
    public void add(int i, Place e) {
        int oldSize = placesList.size();
        placesList.add(i, e);
        fireTableRowsInserted(oldSize, oldSize+1);
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
