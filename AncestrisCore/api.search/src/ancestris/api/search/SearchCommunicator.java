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

import genj.gedcom.Gedcom;
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
            instances = new HashSet<SearchCommunicator>();
        }
        instances.add(this);
    }
    
    public static void unregister(SearchCommunicator sc) {
        if (sc != null && instances.contains(sc)) {
            instances.remove(sc);
        }
    }

    public static List<Property> getResults(Gedcom searchGedcom) {
        
        if (searchGedcom == null) {
            return null;
        }
        
        List<Property> ret = new ArrayList<Property>();
        if (instances == null) {
            return ret;
        }
        for (SearchCommunicator instance : instances) {
            Gedcom iGedcom = instance.getGedcom();
            if (iGedcom != null && searchGedcom.getOrigin().getName().equals(iGedcom.getOrigin().getName())) {
                List<Property> list = instance.getResults();
                if (list != null) {
                    ret.addAll(list);
                }
            }
        }
        
        return ret;
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
    
    
}
