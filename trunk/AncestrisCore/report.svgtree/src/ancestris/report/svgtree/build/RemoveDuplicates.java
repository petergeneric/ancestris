/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2022 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.report.svgtree.build;

import ancestris.report.svgtree.IndiBox;
import ancestris.report.svgtree.filter.TreeFilterBase;

/**
 *
 * @author frederic
 */
public class RemoveDuplicates extends TreeFilterBase {

    private boolean show_duplicates;
    
    public RemoveDuplicates(boolean show_duplicates) {
        this.show_duplicates = show_duplicates;
    }
    
    
    /**
     * If show_duplicates is true, shortcut links from FamBox that are marked with index >= 2
     *    - indiBox.parent = null;
     *    - indiBox.spouse.parent = null;
     *    - indiBox.children = null;
     * 
     */
    @Override
    protected void preFilter(IndiBox indibox) {
        
        if (!show_duplicates && indibox.family != null && indibox.family.index >= 2) {
            indibox.parent = null;
            indibox.spouse.parent = null;
            indibox.children = null;
        }
        
    }

    @Override
    protected void postFilter(IndiBox indibox) {
        IndiBox.netTotalBoxes++;
    }
    
}
