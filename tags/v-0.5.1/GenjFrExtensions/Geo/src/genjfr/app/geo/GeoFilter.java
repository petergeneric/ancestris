/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.geo;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genjfr.app.EditTopComponent;
import genjfr.app.SearchTopComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.swing.JComboBox;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author frederic
 */
public class GeoFilter {

    private Gedcom gedcom;
    public String location = "";
    public boolean ascendants = false;
    public boolean cousins = false;
    public boolean otherAncestors = false;
    public boolean selectedIndividual = false;
    public boolean selectedSearch = false;
    public boolean males = false;
    public boolean females = false;
    public String yearStart = "";
    public String yearEnd = "";
    public boolean births = false;
    public boolean marriages = false;
    public boolean deaths = false;
    public boolean otherEvents = false;
    public Indi decujusIndi = null;
    private List<Indi> ancestorsList = null;
    private List<Indi> cousinsList = null;
    private List<Indi> othersAncestorsList = null;
    private List<Indi> searchedIndis = null;
    private Indi selectedIndi = null;
    private boolean checking = false;

    public void setGedcom(Gedcom gedcom) {
        this.gedcom = gedcom;
        calculatesIndividuals(gedcom, true);
    }

    public void setChecking(boolean flag) {
        this.checking = flag;
    }

    public void calculatesIndividuals(Gedcom gedcom, boolean gedcomToo) {
        decujusIndi = getRootIndi(gedcom);
        if (decujusIndi != null && gedcomToo) {
            ancestorsList = getAncestors(decujusIndi);
            cousinsList = getCousins(ancestorsList);
            othersAncestorsList = getOtherAncestorsList(ancestorsList, cousinsList);
        }
        searchedIndis = getSearchedIndis();
        selectedIndi = getSelectedIndi();
    }

    public boolean complies(GeoNodeObject node) {
        // Filter on location
        // Reject node if location not included in location's description
        if (node.toString().equals(NbBundle.getMessage(GeoListTopComponent.class, "GeoEmpty"))) {
            return false;
        }
        if (!location.isEmpty() && !node.toString().toLowerCase().contains(location.toLowerCase())) {
            return false;
        }

        // Filter on individuals: keep node if any individual matches criteria
        // get individuals related to node
        List<Indi> indis = node.getIndis();

        // Reject node if does not match ancestor relation
        if (ascendants || cousins || otherAncestors) {
            if (!((ascendants && exists(indis, ancestorsList)) || (cousins && exists(indis, cousinsList)) || (otherAncestors && exists(indis, othersAncestorsList)))) {
                return false;
            }
        }

        // Reject node if none of indis is the selected individual in the editor
        //TODO avoid recalculating each time
        if (selectedSearch && !exists(indis, searchedIndis)) {
            return false;
        }

        // Reject node if none of indis is a de-cujus other ancestors
        //TODO avoid recalculating each time
        if (selectedIndividual && !exists(indis, selectedIndi)) {
            return false;
        }

        // Reject node if sex does not match
        if (males || females) {
            if (!((males && sexOf(indis, PropertySex.MALE)) || (females && sexOf(indis, PropertySex.FEMALE)))) {
                return false;
            }
        }

        // Filter on events
        // Reject node if none of the events end dates is after yearStart
        if (!yearStart.isEmpty() && !after(node.getEventsMaxDate(), yearStart)) {
            return false;
        }

        // Reject node if none of the events start dates is before yearEnd
        if (!yearEnd.isEmpty() && !before(node.getEventsMinDate(), yearEnd)) {
            return false;
        }

        // Reject node if none of the events is of type required
        if (births || marriages || deaths || otherEvents) {
            HashSet<String> tags = node.getEventTypes();
            if (!((births && isBirth(tags)) || (marriages && isMarriage(tags)) || (deaths && isDeath(tags)) || (otherEvents && isOtherEvents(tags)))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Filters per se
     */
    private boolean exists(List<Indi> indis, List<Indi> personsList) {
        if (indis == null || personsList == null) {
            return true;
        }
        // returns false is non of indis is in anscestors
        for (Iterator<Indi> it = indis.iterator(); it.hasNext();) {
            Indi indi = it.next();
            if (personsList.contains(indi)) {
                return true;
            }
        }
        return false;
    }

    private boolean exists(List<Indi> indis, Indi selectedIndi) {
        if (indis == null || selectedIndi == null) {
            return true;
        }
        return indis.contains(selectedIndi);
    }

    private boolean sexOf(List<Indi> indis, int sex) {
        if (indis == null) {
            return true;
        }
        for (Iterator<Indi> it = indis.iterator(); it.hasNext();) {
            Indi indi = it.next();
            if (indi.getSex() == sex) {
                return true;
            }
        }
        return false;
    }

    private boolean isBirth(HashSet<String> tags) {
        if (tags == null) {
            return true;
        }
        for (Iterator<String> it = tags.iterator(); it.hasNext();) {
            String string = it.next();
            if (string.equals("BIRT") || string.equals("CHR")) {
                return true;
            }
        }
        return false;
    }

    private boolean isMarriage(HashSet<String> tags) {
        if (tags == null) {
            return true;
        }
        for (Iterator<String> it = tags.iterator(); it.hasNext();) {
            String string = it.next();
            if (string.equals("MARR") || string.equals("ENGA") || string.equals("MARB") || string.equals("MARC")) {
                return true;
            }
        }
        return false;
    }

    private boolean isDeath(HashSet<String> tags) {
        if (tags == null) {
            return true;
        }
        for (Iterator<String> it = tags.iterator(); it.hasNext();) {
            String string = it.next();
            if (string.equals("DEAT") || string.equals("BURI") || string.equals("CREM")) {
                return true;
            }
        }
        return false;
    }

    private boolean isOtherEvents(HashSet<String> tags) {
        if (tags == null) {
            return true;
        }
        return !isBirth(tags) && !isMarriage(tags) && !isDeath(tags);
    }

    private boolean after(int eventsMaxDate, String yearStart) {
        if (yearStart == null || yearStart.isEmpty()) {
            return true;
        }
        return eventsMaxDate != -99999 && eventsMaxDate >= getNb(yearStart);
    }

    private boolean before(int eventsMinDate, String yearEnd) {
        if (yearEnd == null || yearEnd.isEmpty()) {
            return true;
        }
        return eventsMinDate != +99999 && eventsMinDate <= getNb(yearEnd);
    }

    /**
     * Get root indi in two ways:
     * - first look for _SOSA=1
     * - if not found, ask user for an individual
     * @param gedcom
     * @return
     */
    private Indi getRootIndi(Gedcom gedcom) {
        if (decujusIndi != null) {
            return decujusIndi;
        }
        if (gedcom == null) {
            return null;
        }
        // Get all individuals and stop when sosa 1 is found
        Collection entities = gedcom.getEntities(Gedcom.INDI);
        Property[] props = null;
        int sosaNb = 0;
        String sosaStr = "";
        for (Iterator it = entities.iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
            props = indi.getProperties("_SOSA");
            if (props == null) {
                continue;
            }
            for (int i = 0; i < props.length; i++) {
                Property prop = props[i];
                sosaStr = prop.toString();
                if (getNb(sosaStr) == 1) {
                    return indi;
                }
            }
        }

        // If we are here, no sosa was found, take first element
        return (Indi) entities.iterator().next();
    }

    /**
     * Ask user for a temporary de-cujus (does not change the one in the file, just used temporarily for the filters)
     * Recalculate lists
     * @return
     */
    public Indi askRootIndi() {
        Indi rootSosa = null;
        Entity[] ents = gedcom.getEntities(Gedcom.INDI, "INDI:NAME");
        JComboBox jlist = new JComboBox(ents);

        // set selected item to current decujus
        if (decujusIndi != null) {
            jlist.setSelectedItem(decujusIndi);
        }

        // ask user
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(jlist, NbBundle.getMessage(GeoMapTopComponent.class, "sosa.input.title"), NotifyDescriptor.OK_CANCEL_OPTION);
        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
            rootSosa = (Indi) ents[jlist.getSelectedIndex()];
        } else {
            rootSosa = decujusIndi != null ? decujusIndi : (Indi) ents[0];
        }

        // Recalculates
        if (rootSosa != null) {
            ancestorsList = getAncestors(rootSosa);
            cousinsList = getCousins(ancestorsList);
            othersAncestorsList = getOtherAncestorsList(ancestorsList, cousinsList);
        } else {
            ancestorsList.clear();
            cousinsList.clear();
            othersAncestorsList = getOtherAncestorsList(ancestorsList, cousinsList);
        }
        decujusIndi = rootSosa;
//        printList("ancestor", ancestorsList);
//        printList("cousins", cousinsList);
//        printList("othersAncestorsList", othersAncestorsList);
//        printList("searchedIndis", searchedIndis);
        return rootSosa;
    }

    /**
     * Get nb from string removing left and right letters
     */
    private int getNb(String str) {

        if (str == null || str.isEmpty()) {
            return 0;
        }
        int sosaNb = 0;

        int start = 0, end = 0;
        while (start <= end && !Character.isDigit(str.charAt(start))) {
            start++;
        }
        end = start;
        while ((end <= str.length() - 1) && Character.isDigit(str.charAt(end))) {
            end++;
        }
        if (end == start) {
            return 0;
        } else {
            try {
                sosaNb = Integer.parseInt(str.substring(start, end));
            } catch (Exception e) {
                sosaNb = 0;
            }
        }
        return sosaNb;
    }

    /**
     * Get list of ancestors from the root indi
     * @param decujusIndi
     * @return
     */
    private List<Indi> getAncestors(Indi decujusIndi) {
        if (decujusIndi == null) {
            return null;
        }
        List<Indi> indis = new ArrayList<Indi>();
        indis.add(decujusIndi);
        HashSet<Indi> tempList = new HashSet<Indi>();
        for (ListIterator<Indi> it = indis.listIterator(); it.hasNext();) {
            Indi indi = it.next();
            if (tempList.contains(indi)) {
                continue;
            }
            // grab father and mother
            Fam famc = indi.getFamilyWhereBiologicalChild();
            if (famc != null) {
                Indi mother = famc.getWife();
                if (mother != null) {
                    it.add(mother);
                    it.previous();
                }
                Indi father = famc.getHusband();
                if (father != null) {
                    it.add(father);
                    it.previous();
                }
            }
            tempList.add(indi);
        }
        return indis;
    }

    /**
     * Get list of cousins leveraging the ancestors list
     * @param ancestorsList
     * @return
     */
    private List<Indi> getCousins(List<Indi> ancestorsList) {
        if (ancestorsList == null || gedcom == null) {
            return null;
        }
        
        Collection indis = gedcom.getEntities(Gedcom.INDI);
        HashSet<Indi> otherIndis = new HashSet<Indi>();

        // get all non ancestors
        for (Iterator it = indis.iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
            if (!ancestorsList.contains(indi)) {
                otherIndis.add(indi);
            }
        }

        // Get cousins now by flaging all non ancestors that are descendants of ancestors
        List<Indi> cousinsList = new ArrayList<Indi>();
        for (Iterator it = ancestorsList.iterator(); it.hasNext();) {
            Indi ancestor = (Indi) it.next();
            HashSet<Indi> descendants = new HashSet<Indi>();
            getDescendants(ancestor, otherIndis, descendants);
            cousinsList.addAll(descendants);
            otherIndis.removeAll(descendants);
        }
        return cousinsList;
    }

    private void getDescendants(Indi ancestor, HashSet<Indi> inSet, HashSet<Indi> descendants) {
        Indi[] children = ancestor.getChildren();
        for (int i = 0; i < children.length; i++) {
            Indi indi = children[i];
            if (!inSet.contains(indi)) {
                continue;
            }
            descendants.add(indi);
            inSet.remove(indi);
            getDescendants(indi, inSet, descendants);
        }
        return;
    }

    /**
     * Get others (non direct ancestors nor cousins) list
     * @param ancestorsList
     * @param cousinsList
     * @return
     */
    private List<Indi> getOtherAncestorsList(List<Indi> ancestorsList, List<Indi> cousinsList) {
        if (ancestorsList == null && cousinsList == null || gedcom == null) {
            return null;
        }
        Collection indis = gedcom.getEntities(Gedcom.INDI);
        List<Indi> otherIndisList = new ArrayList<Indi>();

        // get all non ancestors
        for (Iterator it = indis.iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
            if (ancestorsList != null && !ancestorsList.contains(indi) && cousinsList != null && !cousinsList.contains(indi)) {
                otherIndisList.add(indi);
            }
        }
        return otherIndisList;
    }

    /**
     * Get all individuals who are somewhere in the search dialog result
     * @return
     */
    private List<Indi> getSearchedIndis() {
        List<Indi> indis = new ArrayList<Indi>();
        Set<TopComponent> tcSet = TopComponent.getRegistry().getOpened();
        for (Iterator<TopComponent> it = tcSet.iterator(); it.hasNext();) {
            TopComponent topComponent = it.next();
            if (topComponent instanceof SearchTopComponent && gedcom.getOrigin().getFileName().equals(topComponent.getName())) {
                for (Iterator<Property> it1 = ((SearchTopComponent) topComponent).getResultProperties().iterator(); it1.hasNext();) {
                    Property prop = it1.next();
                    String tag = prop.getTag();
                    if (tag.equals("ASSO") || tag.equals("CHIL") || tag.equals("FAMC") || tag.equals("FAMS") || tag.equals("HUSB") || tag.equals("WIFE")) {
                        continue;
                    }
                    if (!(prop instanceof Indi) && !(prop instanceof Fam)) {
                        prop = prop.getEntity();
                    }
                    if (prop instanceof Indi) {
                        indis.add((Indi) prop);
                    } else if (prop instanceof Fam) {
                        Indi indi = ((Fam) prop).getHusband();
                        if (indi != null) {
                            indis.add(indi);
                        }
                        indi = ((Fam) prop).getWife();
                        if (indi != null) {
                            indis.add(indi);
                        }
                    }
                }
                return indis;
            }
        }
        return null;
    }

    /**
     * Get currently selected individual in the editor view
     * @return
     */
    private Indi getSelectedIndi() {
        if (gedcom == null) {
            return null;
        }
        Set<TopComponent> tcSet = TopComponent.getRegistry().getOpened();
        for (Iterator<TopComponent> it = tcSet.iterator(); it.hasNext();) {
            TopComponent topComponent = it.next();
            if (topComponent instanceof EditTopComponent && gedcom.getOrigin().getFileName().equals(topComponent.getName())) {
                Entity ent = ((EditTopComponent) topComponent).getContext().getEntity();
                if (ent instanceof Indi) {
                    return (Indi) ent;
                } else if (ent instanceof Fam) {
                    Indi indi = ((Fam) ent).getHusband();
                    if (indi != null) {
                        return indi;
                    }
                    indi = ((Fam) ent).getWife();
                    if (indi != null) {
                        return indi;
                    }
                }
            }
        }
        return null;
    }

    private void printList(String str, List<Indi> list) {
        int i = 0;
        if (list == null) {
            return;
        }
        for (Iterator<Indi> it = list.iterator(); it.hasNext();) {
            Indi indi = it.next();
            i++;
            System.out.println("DEBUG - " + str + "indi(" + i + ")=" + indi);
        }
    }
}
