/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2017 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.api.search;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Communication device used to communicate between modules for search results.
 * This is a two-way communication even if results are only within one/some of the modules but not all.
 * 
 * @author frederic
 */
public class SearchCommunicator {

    private static Set<SearchCommunicator> instances = null;
    private Gedcom gedcom;

    // Constructor
    public SearchCommunicator() {
        if (instances == null) {
            instances = new HashSet<>();
        }
        instances.add(this);
    }
    
    public static void unregister(SearchCommunicator sc) {
        sc.fireClosing();
        if (instances.contains(sc)) {
            instances.remove(sc);
        }
    }

    public static List<Property> getResults(Gedcom searchGedcom) {
        
        if (searchGedcom == null) {
            return null;
        }
        
        List<Property> ret = new ArrayList<>();
        if (instances == null) {
            return ret;
        }
        for (SearchCommunicator instance : instances) {
            Gedcom iGedcom = instance.getGedcom();
            if (iGedcom != null && searchGedcom.getOrigin() != null && iGedcom.getOrigin() != null && searchGedcom.getOrigin().getName().equals(iGedcom.getOrigin().getName())) {
                List<Property> list = instance.getResults();
                if (list != null) {
                    ret.addAll(list);
                }
            }
        }
        
        return ret;
    }
    
    
    /**
     * Get all individuals who are somewhere in the search dialog result
     *
     * @param gedcom
     * @return
     */
    public static List<Entity> getResultEntities(Gedcom gedcom) {
        final List<Entity> retList = new ArrayList<>();

        if (gedcom == null) {
            return retList;
        }

        final List<Property> results = SearchCommunicator.getResults(gedcom);
        if (results == null) {
            return retList;
        }
        for (Property prop : results) {
            String tag = prop.getTag();
            if (tag.equals("ASSO") || tag.equals("CHIL") || tag.equals("FAMC") || tag.equals("FAMS") || tag.equals("HUSB") || tag.equals("WIFE")) {
                prop = prop.getEntity();
            }
            if (!(prop instanceof Indi) && !(prop instanceof Fam)) {
                prop = prop.getEntity();
            }
            if (prop instanceof Indi || prop instanceof Fam) {
                retList.add((Entity)prop);
            }
        }
        return retList;
    }
    
    
    public void fireNewResults() {
        if (instances == null) {
            return;
        }
        for (SearchCommunicator instance : instances) {
            Gedcom iGedcom = instance.getGedcom();
            if (gedcom != null && iGedcom.getOrigin().getName().equals(gedcom.getOrigin().getName())) {
                instance.changedResults(gedcom);
            }
        }
    }
    
    public void fireClosing() {
        if (instances == null) {
            return;
        }
        for (SearchCommunicator instance : instances) {
            Gedcom iGedcom = instance.getGedcom();
            if (gedcom != null && iGedcom.getOrigin().getName().equals(gedcom.getOrigin().getName())) {
                instance.closing(gedcom);
            }
        }
    }
    
    
    
    
    public void setGedcom(Gedcom gedcom) {
        this.gedcom = gedcom;
    }
    
    public Gedcom getGedcom() {
        return gedcom;
    }
    
    
    public List<Property> getResults() {
        return null;
    }
    
    public void changedResults(Gedcom gedcom) {
    }

    public void closing(Gedcom gedcom) {
    }
    
}
