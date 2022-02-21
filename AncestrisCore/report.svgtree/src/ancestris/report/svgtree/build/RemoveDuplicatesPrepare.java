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

import ancestris.report.svgtree.FamBox;
import ancestris.report.svgtree.IndiBox;
import ancestris.report.svgtree.filter.TreeFilterBase;
import genj.gedcom.Fam;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author frederic
 */
public class RemoveDuplicatesPrepare extends TreeFilterBase {

    private boolean calculated = false;
    private Map<Fam, List<FamBox>> map;
    
    public RemoveDuplicatesPrepare() {
        map = new HashMap<>();
    }
    
    
    /**
     * Runs the pre-filter on the given individual to identify duplicates. 
     * We are only interested in family duplicates because we only want to remove *branch* duplicates.
     * 
     * Visit all indiBoxes 
     * For each IndiBox, get the FamilyBox, and map the fam to this FamBox
     */
    @Override
    protected void preFilter(IndiBox indibox) {
        if (indibox.family == null || indibox.family.family == null) {
            return;
        }
        List<FamBox> list = map.get(indibox.family.family);
        if (list == null) {
            list = new ArrayList<>();
            map.put(indibox.family.family, list);
        }
        list.add(indibox.family);
    }

    public Map<Fam, List<FamBox>> getMap() {
        return map;
    }
    
}
