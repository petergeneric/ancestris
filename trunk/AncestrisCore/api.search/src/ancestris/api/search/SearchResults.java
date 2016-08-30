/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
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
import java.util.List;
import java.util.Set;

/**
 *
 * @author frederic
 */
public interface SearchResults {
    
    public abstract Set<SearchResults> getInstances();
    public Gedcom getGedcom();
    public List<Property> getResultProperties();

}
