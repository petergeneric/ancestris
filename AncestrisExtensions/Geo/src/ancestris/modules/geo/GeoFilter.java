/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.geo;

import ancestris.api.search.SearchCommunicator;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.*;
import genj.util.Registry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JComboBox;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author frederic
 */
public class GeoFilter {

    private Registry registry = null;
    
    private Gedcom gedcom;
    public String location = "";
    public boolean ascendants = false;
    public boolean descendants = false;
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
    public Indi rootIndi = null;
    private HashSet<Indi> ancestorsList = null;
    private HashSet<Indi> descendantsList = null;
    private HashSet<Indi> cousinsList = null;
    private HashSet<Indi> othersList = null;
    private HashSet<Indi> searchedIndis = null;
    private Indi selectedIndi = null;
    
    private boolean isBusy = false;

    public void setGedcom(Gedcom gedcom) {
        this.gedcom = gedcom;
        registry = Registry.get(gedcom);
        load();
    }

    public void load() {
        location = registry.get("geofilter.location", "");
        ascendants = registry.get("geofilter.ascendants", false);
        descendants = registry.get("geofilter.descendants", false);
        cousins = registry.get("geofilter.cousins", false);
        otherAncestors = registry.get("geofilter.otherAncestors", false);
        selectedIndividual = registry.get("geofilter.selectedIndividual", false);
        selectedSearch = registry.get("geofilter.selectedSearch", false);
        males = registry.get("geofilter.males", false);
        females = registry.get("geofilter.females", false);
        yearStart = registry.get("geofilter.yearStart", "");
        yearEnd = registry.get("geofilter.yearEnd", "");
        births = registry.get("geofilter.births", false);
        marriages = registry.get("geofilter.marriages", false);
        deaths = registry.get("geofilter.deaths", false);
        otherEvents = registry.get("geofilter.otherEvents", false);
    }

    public void save() {
        registry.put("geofilter.location", location);
        registry.put("geofilter.ascendants", ascendants);
        registry.put("geofilter.descendants", descendants);
        registry.put("geofilter.cousins", cousins);
        registry.put("geofilter.otherAncestors", otherAncestors);
        registry.put("geofilter.selectedIndividual", selectedIndividual);
        registry.put("geofilter.selectedSearch", selectedSearch);
        registry.put("geofilter.males", males);
        registry.put("geofilter.females", females);
        registry.put("geofilter.yearStart", yearStart);
        registry.put("geofilter.yearEnd", yearEnd);
        registry.put("geofilter.births", births);
        registry.put("geofilter.marriages", marriages);
        registry.put("geofilter.deaths", deaths);
        registry.put("geofilter.otherEvents", otherEvents);
    }

    public void calculatesIndividuals(Gedcom gedcom) {
        if (isBusy) {
            return;
        }
        isBusy = true;
        selectedIndi = getSelectedIndi();
        if (rootIndi == null) {
            rootIndi = selectedIndi;
        }
        if (rootIndi != null) {
            ancestorsList = getAncestors(rootIndi);
            descendantsList = getDescendants(rootIndi);
            cousinsList = getCousins();
            othersList = getOthersList();
        }
        searchedIndis = getSearchedIndis();
        isBusy = false;
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
        if (ascendants || descendants || cousins || otherAncestors) {
            if (!((ascendants && exists(indis, ancestorsList)) || (descendants && exists(indis, descendantsList)) || (cousins && exists(indis, cousinsList)) || (otherAncestors && exists(indis, othersList)))) {
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
    private boolean exists(List<Indi> indis, HashSet<Indi> personsList) {
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

    public Indi getRootIndi() {
        if (rootIndi == null) {
            rootIndi = getSelectedIndi();
        }
        return rootIndi;
    }
    
    /**
     * Get root indi in two ways:
     * - first look for _SOSA=1
     * - if not found, return null
     * @param gedcom
     * @return
     */
    public Indi getDeCujusIndi() {
        // Get all individuals and stop when sosa 1 is found
        Collection <Indi>entities = (Collection <Indi>) gedcom.getEntities(Gedcom.INDI);
        Property[] props = null;
        String sosaStr = "";
        for (Iterator <Indi>it = entities.iterator(); it.hasNext();) {
            Indi indi = it.next();
            props = indi.getProperties("_SOSA");
            if (props != null) {
                for (int i = 0; i < props.length; i++) {
                    Property prop = props[i];
                    sosaStr = prop.getDisplayValue();
                    if ("1".equals(sosaStr) || "1 ".equals(sosaStr.substring(0, 1))) {
                        return indi;
                    }
                }
            }
            props = indi.getProperties("_SOSADABOVILLE");
            if (props != null) {
                for (int i = 0; i < props.length; i++) {
                    Property prop = props[i];
                    sosaStr = prop.getDisplayValue();
                    if (sosaStr.startsWith("1")) {
                        String str = "";
                    }
                    if ("1".equals(sosaStr) || "1 ".equals(sosaStr.substring(0, 2))) {
                        return indi;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Ask user for a temporary de-cujus (does not change the one in the file, just used temporarily for the filters)
     * Recalculate lists
     * @return
     */
    public Indi askRootIndi() {
        Indi chosenIndi = null;
        Entity[] ents = gedcom.getEntities(Gedcom.INDI, "INDI:NAME");
        JComboBox <Entity>jlist = new JComboBox<Entity>(ents);

        // set selected item to current decujus
        if (rootIndi != null) {
            jlist.setSelectedItem(rootIndi);
        }

        // ask user
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(jlist, NbBundle.getMessage(GeoMapTopComponent.class, "sosa.input.title"), NotifyDescriptor.OK_CANCEL_OPTION);
        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
            chosenIndi = (Indi) ents[jlist.getSelectedIndex()];
        } else {
            chosenIndi = rootIndi != null ? rootIndi : (Indi) ents[0];
        }

        return chosenIndi;
    }

    /**
     * Get nb from string removing left and right letters
     */
    private int getNb(String str) {

        if (str == null || str.isEmpty()) {
            return 0;
        }
        int sosaNb = 0;

        int start = 0, end = str.length();
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
     * @param rootIndi
     * @return
     */
    private HashSet<Indi> getAncestors(Indi rootIndi) {
        if (rootIndi == null) {
            return null;
        }

        HashSet<Indi> retList = new HashSet<Indi>();

        List<Indi> indis = new ArrayList<Indi>();
        indis.add(rootIndi);
        for (ListIterator<Indi> it = indis.listIterator(); it.hasNext();) {
            Indi indi = it.next();
            if (retList.contains(indi)) {
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
            retList.add(indi);
        }
        return retList;
    }

    /**
     * Get list of descendants from the root indi
     * @param rootIndi
     * @return
     */
    private HashSet<Indi> getDescendants(Indi rootIndi) {
        if (rootIndi == null) {
            return null;
        }

        HashSet<Indi> retList = new HashSet<Indi>();

        List<Indi> indis = new ArrayList<Indi>();
        indis.add(rootIndi);
        for (ListIterator<Indi> it = indis.listIterator(); it.hasNext();) {
            Indi indi = it.next();
            if (retList.contains(indi)) {
                continue;
            }
            // grab kids
            Indi[] children = indi.getChildren();
            for (int i = 0; i < children.length; i++) {
                Indi kid = children[i];
                it.add(kid);
                it.previous();
            }
            retList.add(indi);
        }
        return retList;
    }

    /**
     * Get list of cousins leveraging the ancestors list
     * @param ancestorsList
     * @return
     */
    private HashSet<Indi> getCousins() {
        if (ancestorsList == null || descendantsList == null || gedcom == null) {
            return null;
        }
        
        HashSet<Indi> retList = new HashSet<Indi>();

        HashSet<Indi> excludedIndis = new HashSet<Indi>();

        // get all non ancestors and non descendants
        excludedIndis.addAll(ancestorsList);
        excludedIndis.addAll(descendantsList);

        // Get cousins now by flaging all non ancestors nor descendants of root that are descendants of an ancestor of root
        for (Indi ancestor : ancestorsList) {
            HashSet<Indi> descendantslst = new HashSet<Indi>();
            getDescendants(ancestor, excludedIndis, descendantslst); // get descendants of ancestor that are not to be excluded
            retList.addAll(descendantslst);
            excludedIndis.addAll(retList);
        }
        return retList;
    }

    private void getDescendants(Indi ancestor, HashSet<Indi> excludedSet, HashSet<Indi> descendants) {
        Indi[] children = ancestor.getChildren();
        for (int i = 0; i < children.length; i++) {
            Indi indi = children[i];
            if (excludedSet.contains(indi)) {
                continue;
            }
            descendants.add(indi);
            getDescendants(indi, excludedSet, descendants);
        }
    }

    /**
     * Get others (non direct ancestors nor cousins) list
     * @param ancestorsList
     * @param cousinsList
     * @return
     */
    private HashSet<Indi> getOthersList() {
        if (ancestorsList == null && descendantsList == null && cousinsList == null || gedcom == null) {
            return null;
        }

        HashSet<Indi> retList = new HashSet<Indi>();

        HashSet<Indi> indis = new HashSet<Indi>((Collection <Indi>) gedcom.getEntities(Gedcom.INDI));
        indis.removeAll(ancestorsList);
        indis.removeAll(descendantsList);
        indis.removeAll(cousinsList);
        retList.addAll(indis);
        return retList;
    }

    /**
     * Get all individuals who are somewhere in the search dialog result
     * @return
     */
    public HashSet<Indi> getSearchedIndis() {
        if (gedcom == null) {
            return null;
        }
        HashSet<Indi> retList = new HashSet<Indi>();
        
        List<Property> results = SearchCommunicator.getResults(gedcom);
        if (results == null) {
            return retList;
        }
        for (Property prop : results) {
            String tag = prop.getTag();
            if (tag.equals("ASSO") || tag.equals("CHIL") || tag.equals("FAMC") || tag.equals("FAMS") || tag.equals("HUSB") || tag.equals("WIFE")) {
                continue;
            }
            if (!(prop instanceof Indi) && !(prop instanceof Fam)) {
                prop = prop.getEntity();
            }
            if (prop instanceof Indi) {
                retList.add((Indi) prop);
            } else if (prop instanceof Fam) {
                Indi indi = ((Fam) prop).getHusband();
                if (indi != null) {
                    retList.add(indi);
                }
                indi = ((Fam) prop).getWife();
                if (indi != null) {
                    retList.add(indi);
                }
            }
        }
        return retList;
    }

    /**
     * Get currently selected individual in the editor view
     * @return
     */
    public Indi getSelectedIndi() {
        if (gedcom == null) {
            return null;
        }
        // Quick replace Editor search by ControlCenter api
        Context context = Utilities.actionsGlobalContext().lookup(Context.class);
        if (context == null){
            return null;
        }
        Entity ent = context.getEntity();
        if (ent == null) return null;
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
        return null;
    }

    private void printList(String str, HashSet<Indi> list) {
        int i = 0;
        if (list == null) {
            return;
        }
        for (Indi indi : list) {
            i++;
            System.out.println("DEBUG - " + str + "indi(" + i + ")=" + indi);
        }
    }
}
