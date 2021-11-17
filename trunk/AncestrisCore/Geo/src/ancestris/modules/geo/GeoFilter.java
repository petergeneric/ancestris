/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.geo;

import ancestris.api.search.SearchCommunicator;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.util.Registry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
    public boolean showUnknown = false;
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
    private HashSet<Indi> ancestorsList = new HashSet<>();
    private HashSet<Indi> descendantsList = new HashSet<>();
    private HashSet<Indi> cousinsList = new HashSet<>();
    private HashSet<Indi> othersList = new HashSet<>();
    private HashSet<Indi> searchedIndis = new HashSet<>();
    private Indi selectedIndi = null;

    private boolean isBusy = false;

    public void setGedcom(Gedcom gedcom) {
        this.gedcom = gedcom;
        registry = gedcom.getRegistry();
        load();
    }

    public void load() {
        location = registry.get("GEO.geofilter.location", "");
        showUnknown = registry.get("GEO.geofilter.showUnknown", false);
        ascendants = registry.get("GEO.geofilter.ascendants", false);
        descendants = registry.get("GEO.geofilter.descendants", false);
        cousins = registry.get("GEO.geofilter.cousins", false);
        otherAncestors = registry.get("GEO.geofilter.otherAncestors", false);
        selectedIndividual = registry.get("GEO.geofilter.selectedIndividual", false);
        selectedSearch = registry.get("GEO.geofilter.selectedSearch", false);
        males = registry.get("GEO.geofilter.males", false);
        females = registry.get("GEO.geofilter.females", false);
        yearStart = registry.get("GEO.geofilter.yearStart", "");
        yearEnd = registry.get("GEO.geofilter.yearEnd", "");
        births = registry.get("GEO.geofilter.births", false);
        marriages = registry.get("GEO.geofilter.marriages", false);
        deaths = registry.get("GEO.geofilter.deaths", false);
        otherEvents = registry.get("GEO.geofilter.otherEvents", false);
    }

    public void save() {
        registry.put("GEO.geofilter.location", location);
        registry.put("GEO.geofilter.showUnknown", showUnknown);
        registry.put("GEO.geofilter.ascendants", ascendants);
        registry.put("GEO.geofilter.descendants", descendants);
        registry.put("GEO.geofilter.cousins", cousins);
        registry.put("GEO.geofilter.otherAncestors", otherAncestors);
        registry.put("GEO.geofilter.selectedIndividual", selectedIndividual);
        registry.put("GEO.geofilter.selectedSearch", selectedSearch);
        registry.put("GEO.geofilter.males", males);
        registry.put("GEO.geofilter.females", females);
        registry.put("GEO.geofilter.yearStart", yearStart);
        registry.put("GEO.geofilter.yearEnd", yearEnd);
        registry.put("GEO.geofilter.births", births);
        registry.put("GEO.geofilter.marriages", marriages);
        registry.put("GEO.geofilter.deaths", deaths);
        registry.put("GEO.geofilter.otherEvents", otherEvents);
    }

    public void calculatesIndividuals(Gedcom gedcom) {
        if (isBusy) {
            return;
        }
        isBusy = true;
        selectedIndi = getSelectedIndi();
        if (rootIndi == null) {
            rootIndi = getDeCujusIndi();
        }
        if (rootIndi == null) {
            rootIndi = selectedIndi;
        }
        if (rootIndi == null) {
            rootIndi = (Indi) gedcom.getFirstEntity("INDI");
        }
        ancestorsList = getAncestors(rootIndi);
        descendantsList = getDescendants(rootIndi);
        cousinsList = getCousins();
        othersList = getOthersList();
        searchedIndis = getSearchedIndis();
        isBusy = false;
    }

    public boolean compliesNode(GeoNodeObject node) {
        // Filter on location : reject node if location not included in location's description
        if (!location.isEmpty()) { // there is a location filter
            if (node.toString().equals(NbBundle.getMessage(GeoListTopComponent.class, "GeoEmpty"))) {   // exclude empty locations
                return false;
            }
            if (!node.toString().toLowerCase().contains(location.toLowerCase())) {  // exclude if location to search not in the location's name
                return false;
            }
        }

        // exclude default/unknown locations
        if (!showUnknown && node.areCoordinatesUnknown()) {
            return false;
        }

        // Make sure at least an event matches a criteria
        for (GeoNodeObject event : node.getAllEvents()) {
            if (compliesEvent(event)) {
                return true;
            }
        }

        return false;
    }

    public boolean compliesEvent(GeoNodeObject event) {

        // Reject if none of the individuals match
        boolean found = false;
        List<Indi> indis = event.getIndis();
        if (indis.isEmpty()) {
            return true;  // if no indi to comply to, it complies.
        }
        for (Indi indi : indis) {
            if (compliesIndi(indi)) {
                found = true;
                break;
            }
        }
        if (!found) {
            return false;
        }

        // Reject node if none of the events is of type required
        if (births || marriages || deaths || otherEvents) {
            String tag = event.getEventTag();
            if (!((births && isBirth(tag)) || (marriages && isMarriage(tag)) || (deaths && isDeath(tag)) || (otherEvents && isOtherEvents(tag)))) {
                return false;
            }
        }

        // Filter on event date and type
        // Reject node if none of the events end dates is after yearStart
        if (!yearStart.isEmpty() && !after(event.getEventsMaxDate(), yearStart)) {
            return false;
        }

        // Reject node if none of the events start dates is before yearEnd
        if (!yearEnd.isEmpty() && !before(event.getEventsMinDate(), yearEnd)) {
            return false;
        }

        return true;
    }

    public boolean compliesIndi(Indi indi) {
        if (ascendants || descendants || cousins || otherAncestors) {
            if (!((ascendants && ancestorsList.contains(indi))
                    || (descendants && descendantsList.contains(indi))
                    || (cousins && cousinsList.contains(indi))
                    || (otherAncestors && othersList.contains(indi)))) {
                return false;
            }
        }
        if (selectedSearch && !searchedIndis.contains(indi)) {
            return false;
        }
        if (selectedIndividual && selectedIndi != null && !selectedIndi.equals(indi)) {
            return false;
        }
        if (males || females) {
            if (!((males && indi.getSex() == PropertySex.MALE) || (females && indi.getSex() == PropertySex.FEMALE))) {
                return false;
            }
        }
        return true;
    }

    public boolean isBirth(String tag) {
        return ("BIRT".equals(tag));
    }

    public boolean isMarriage(String tag) {
        return ("MARR".equals(tag) || "ENGA".equals(tag) || "MARB".equals(tag) || "MARC".equals(tag));
    }

    public boolean isDeath(String tag) {
        return ("DEAT".equals(tag) || "BURI".equals(tag) || "CREM".equals(tag));
    }

    public boolean isOtherEvents(String tag) {
        return !isBirth(tag) && !isMarriage(tag) && !isDeath(tag);
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
     * Ask user for a temporary de-cujus (does not change the one in the file,
     * just used temporarily for the filters) Recalculate lists
     *
     * @return
     */
    public Indi askRootIndi() {
        final Indi chosenIndi;
        final Entity[] ents = gedcom.getEntities(Gedcom.INDI, "INDI:NAME");
        final JComboBox<Entity> jlist = new JComboBox<>(ents);

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
        int sosaNb;

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
            } catch (NumberFormatException e) {
                sosaNb = 0;
            }
        }
        return sosaNb;
    }

    /**
     * Get list of ancestors from the root indi
     *
     * @param rootIndi
     * @return
     */
    private HashSet<Indi> getAncestors(Indi rootIndi) {
        if (rootIndi == null) {
            return null;
        }

        HashSet<Indi> retList = new HashSet<>();

        List<Indi> indis = new ArrayList<>();
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
     *
     * @param rootIndi
     * @return
     */
    private HashSet<Indi> getDescendants(Indi rootIndi) {
        if (rootIndi == null) {
            return null;
        }

        HashSet<Indi> retList = new HashSet<>();

        List<Indi> indis = new ArrayList<>();
        indis.add(rootIndi);
        for (ListIterator<Indi> it = indis.listIterator(); it.hasNext();) {
            Indi indi = it.next();
            if (retList.contains(indi)) {
                continue;
            }
            // grab kids
            Indi[] children = indi.getChildren();
            for (Indi kid : children) {
                it.add(kid);
                it.previous();
            }
            retList.add(indi);
        }
        return retList;
    }

    /**
     * Get list of cousins leveraging the ancestors list
     *
     * @param ancestorsList
     * @return
     */
    private HashSet<Indi> getCousins() {
        if (gedcom == null) {
            return new HashSet<>();
        }

        HashSet<Indi> retList = new HashSet<>();

        HashSet<Indi> excludedIndis = new HashSet<>();

        // get all non ancestors and non descendants
        excludedIndis.addAll(ancestorsList);
        excludedIndis.addAll(descendantsList);

        // Get cousins now by flaging all non ancestors nor descendants of root that are descendants of an ancestor of root
        for (Indi ancestor : ancestorsList) {
            final HashSet<Indi> descendantslst = new HashSet<>();
            getDescendants(ancestor, excludedIndis, descendantslst); // get descendants of ancestor that are not to be excluded
            retList.addAll(descendantslst);
            excludedIndis.addAll(retList);
        }
        return retList;
    }

    private void getDescendants(Indi ancestor, HashSet<Indi> excludedSet, HashSet<Indi> descendants) {
        for (Indi indi : ancestor.getChildren()) {
            if (excludedSet.contains(indi)) {
                continue;
            }
            descendants.add(indi);
            getDescendants(indi, excludedSet, descendants);
        }
    }

    /**
     * Get others (non direct ancestors nor cousins) list
     *
     * @param ancestorsList
     * @param cousinsList
     * @return
     */
    private HashSet<Indi> getOthersList() {
        if (gedcom == null) {
            return new HashSet<>();
        }

        final HashSet<Indi> retList = new HashSet<>();

        final HashSet<Indi> indis = new HashSet<>((Collection<Indi>) gedcom.getEntities(Gedcom.INDI));
        indis.removeAll(ancestorsList);
        indis.removeAll(descendantsList);
        indis.removeAll(cousinsList);
        retList.addAll(indis);
        return retList;
    }

    /**
     * Get all individuals who are somewhere in the search dialog result
     *
     * @return
     */
    public HashSet<Indi> getSearchedIndis() {
        if (gedcom == null) {
            return new HashSet<>();
        }
        final HashSet<Indi> retList = new HashSet<>();

        final List<Property> results = SearchCommunicator.getResults(gedcom);
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
     *
     * @return
     */
    public Indi getSelectedIndi() {
        if (gedcom == null) {
            return null;
        }
        // Quick replace Editor search by ControlCenter api
        Context context = Utilities.actionsGlobalContext().lookup(Context.class);
        if (context == null) {
            return null;
        }
        Entity ent = context.getEntity();
        if (ent == null) {
            return null;
        }
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

    /**
     * Display description of filter depending on what filters are selected
     *
     * @return
     */
    public String getShortDescription() {
        String ret = "";

        // Are there any filters on location
        if (!location.isEmpty()) {
            ret += org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "filters.location", location);
        }

        // Are we hiding unknown places
        if (!showUnknown) {
            if (!ret.isEmpty()) {
                ret += " + ";
            }
            ret += org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "filters.unknown");
        }

        // Are there any filters on tree structure
        String indi = (rootIndi != null) ? rootIndi.toString(true) : "";
        if (ascendants | descendants | cousins | otherAncestors) {
            String tree = "";
            String asce = org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "SettingsPanel.jAncestorCheckBox.text");
            String desc = org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "SettingsPanel.jDescendantCheckBox.text");
            String cous = org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "SettingsPanel.jCousinCheckBox.text");
            String othe = org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "SettingsPanel.jOthersCheckBox.text");
            if (!ascendants) {
                asce = "";
            }
            tree += asce;

            if (!descendants) {
                desc = "";
            } else if (!tree.isEmpty()) {
                tree += "+";
            }
            tree += desc;

            if (!cousins) {
                cous = "";
            } else if (!tree.isEmpty()) {
                tree += "+";
            }
            tree += cous;

            if (!otherAncestors) {
                othe = "";
            } else if (!tree.isEmpty()) {
                tree += "+";
            }
            tree += othe;

            if (!tree.isEmpty()) {
                if (!ret.isEmpty()) {
                    ret += " + ";
                }
                ret += org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "filters.tree", tree, indi);
            }
        }

        // Are there any filters on events
        if (!yearStart.isEmpty() | !yearEnd.isEmpty() | births | marriages | deaths | otherEvents) {
            String event = "";
            String yeas = ">";
            String yeae = "<";
            String birt = org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "SettingsPanel.jBirthCheckBox.text");
            String marr = org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "SettingsPanel.jWeddingCheckBox.text");
            String deat = org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "SettingsPanel.jDeathCheckBox1.text");
            String othe = org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "SettingsPanel.jOtherEventCheckBox1.text");

            if (yearStart.isEmpty()) {
                yeas = "";
            } else {
                yeas += yeas + yearStart;
            }

            if (yearEnd.isEmpty()) {
                yeae = "";
            } else {
                yeae += yeae + yearEnd;
            }
            if (!event.isEmpty()) {
                event += "+";
            }
            event += yeae;

            if (!births) {
                birt = "";
            } else if (!event.isEmpty()) {
                event += "+";
            }
            event += birt;

            if (!marriages) {
                marr = "";
            } else if (!event.isEmpty()) {
                event += "+";
            }
            event += marr;

            if (!deaths) {
                deat = "";
            } else if (!event.isEmpty()) {
                event += "+";
            }
            event += deat;

            if (!otherEvents) {
                othe = "";
            } else if (!event.isEmpty()) {
                event += "+";
            }
            event += othe;

            if (!event.isEmpty()) {
                if (!ret.isEmpty()) {
                    ret += " + ";
                }
                ret += org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "filters.events", event);
            }

        }

        // Are there any filters on individuals
        String selected = (selectedIndi != null) ? selectedIndi.toString(true) : "";
        if (males | females | selectedIndividual | selectedSearch) {
            String selindi = "";
            String male = org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "SettingsPanel.jMenRadioButton.text");
            String fema = org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "SettingsPanel.jWomenRadioButton.text");
            String sind = org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "SettingsPanel.jSelectedRadioButton.text");
            String ssea = org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "SettingsPanel.jSearchedRadioButton.text");
            if (!males) {
                male = "";
            } else {
                selindi += male;
            }

            if (!females) {
                fema = "";
            } else if (!selindi.isEmpty()) {
                selindi += "+";
            }
            selindi += fema;

            if (!selectedIndividual) {
                sind = "";
            } else if (!selindi.isEmpty()) {
                selindi += "+";
            }
            selindi += sind;
            if (selectedIndividual) {
                selindi += " (" + selected + ")";
            }

            if (!selectedSearch) {
                ssea = "";
            } else if (!selindi.isEmpty()) {
                selindi += "+";
            }
            selindi += ssea;

            if (!selindi.isEmpty()) {
                if (!ret.isEmpty()) {
                    ret += " + ";
                }
                ret += org.openide.util.NbBundle.getMessage(GeoMapTopComponent.class, "filters.indi", selindi);
            }
        }

        return ret;
    }

    public Indi getDeCujusIndi() {
        return gedcom.getDeCujusIndi();
    }
}
